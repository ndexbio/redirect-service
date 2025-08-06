package org.ndexbio.redirectservice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.OutputStreamAppender;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Properties;
import org.eclipse.jetty.ee10.servlet.ErrorHandler;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class ServerFactory {
	public ServerFactory(){
	}
	
	public Server getServer(Properties props) throws Exception {
		ch.qos.logback.classic.Logger rootLog =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLog.setLevel(Level.toLevel(props.getProperty(App.ROOT_LOGLEVEL, "INFO")));
		setLogLevelForClassesInThisPackage(Level.toLevel(props.getProperty(App.RUNSERVER_LOGLEVEL, "INFO")));

		String logDir = props.getProperty(App.RUNSERVER_LOGDIR, ".");
		RolloverFileOutputStream os = new RolloverFileOutputStream(logDir
				+ File.separator + "redirectservice_yyyy_mm_dd.log", true);

		RolloverFileOutputStream requestOS = new RolloverFileOutputStream(logDir + File.separator + "requests_yyyy_mm_dd.log", true);

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
		logEncoder.setContext(lc);
		logEncoder.setPattern("[%date]\t%msg%n");
		logEncoder.start();

		OutputStreamAppender osa = new OutputStreamAppender();
		osa.setOutputStream(requestOS);
		osa.setContext(lc);
		osa.setEncoder(logEncoder);
		osa.setName(RequestLoggingFilter.REQUEST_LOGGER_NAME + "appender");
		osa.start();
		ch.qos.logback.classic.Logger requestLog =
			  (ch.qos.logback.classic.Logger) lc.getLogger(RequestLoggingFilter.REQUEST_LOGGER_NAME);
		requestLog.setLevel(Level.toLevel("INFO"));

		requestLog.setAdditive(false);
		requestLog.addAppender(osa);
		PrintStream logStream = new PrintStream(os);

				//We are redirecting system out and system error to our print stream.
		System.setOut(logStream);
		System.setErr(logStream);

		final int port = Integer.valueOf(props.getProperty(App.RUNSERVER_PORT, "8081"));

		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");
		RedirectServletFactory rsf = new RedirectServletFactory(props);
		context.addServlet(new ServletHolder(rsf.getRedirectServlet()), "/*");

		FilterHolder reqFilterHolder = new FilterHolder(new RequestLoggingFilter());
		context.addFilter(reqFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

		// Set a custom ErrorHandler
		context.setErrorHandler(new ErrorHandler() {
			@Override
			protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
				if (code == HttpServletResponse.SC_NOT_FOUND) {
					writer.write("<html><body><h1>Not Found</h1><p>" + message + "</p></body></html>");
				} else {
					super.handleErrorPage(request, writer, code, message);
				}
			}
		});

		server.setHandler(context);
		return server;
	}
	
	/**
     * Sets logging level for specific classes in this package
     * @param logLevel 
     */
    public static void setLogLevelForClassesInThisPackage(Level logLevel){
        ch.qos.logback.classic.Logger curLog = null;
        Class[] classPkgs = {App.class};
        for (Class c : classPkgs){
            curLog =(ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(c);
            curLog.setLevel(logLevel);
        }
    }
}
