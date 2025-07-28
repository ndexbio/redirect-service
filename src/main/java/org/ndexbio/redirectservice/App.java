package org.ndexbio.redirectservice;

import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.module.Configuration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.OutputStreamAppender;
import jakarta.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jetty.ee10.servlet.ErrorHandler;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.util.RolloverFileOutputStream;



public class App {
	
	static Logger _logger = LoggerFactory.getLogger(App.class);

    public static final String DESCRIPTION = "\nRedirect service\n\n"
            + "For usage information visit:  https://github.com/ndexbio/redirectservice\n\n";

	/**
     * Sets logging level valid values DEBUG INFO WARN ALL ERROR
     */
    public static final String RUNSERVER_LOGLEVEL = "runserver.log.level";

    /**
     * Sets root logger logging level values DEBUG INFO WARN ALL ERROR
     */
    public static final String ROOT_LOGLEVEL = "root.log.level";

    /**
     * Sets log directory for embedded Jetty
     */
    public static final String RUNSERVER_LOGDIR = "runserver.log.dir";

    /**
     * Sets port for embedded Jetty
     */
    public static final String RUNSERVER_PORT = "runserver.port";
	
	public static final String REDIRECTS_FILE = "redirects.file";

	public static final String MODE = "mode";
    public static final String CONF = "conf";
    public static final String EXAMPLE_CONF_MODE = "exampleconf";
	public static final String RUNSERVER_MODE = "runserver";
	
	public static final String SUPPORTED_MODES = EXAMPLE_CONF_MODE + ", "
                                                    + RUNSERVER_MODE;

    public static void main(String[] args) throws Exception {

		
		final List<String> helpArgs = Arrays.asList("h", "help", "?");
        try {
            OptionParser parser = new OptionParser() {

                {
                    accepts(MODE, "Mode to run. Supported modes: " + SUPPORTED_MODES).withRequiredArg().ofType(String.class).required();
                    accepts(CONF, "Configuration file")
                            .withRequiredArg().ofType(String.class);
                    acceptsAll(helpArgs, "Show Help").forHelp();
                }
            };

            OptionSet optionSet = null;
            try {
                optionSet = parser.parse(args);
            } catch (OptionException oe) {
                System.err.println("\nThere was an error parsing arguments: "
                        + oe.getMessage() + "\n\n");
                parser.printHelpOn(System.err);
                System.exit(1);
            }

            //help check
            for (String helpArgName : helpArgs) {
                if (optionSet.has(helpArgName)) {
                    System.out.println(DESCRIPTION);
                    parser.printHelpOn(System.out);
                    System.exit(2);
                }
            }
			
			String mode = optionSet.valueOf(MODE).toString();

            if (mode.equals(EXAMPLE_CONF_MODE)){
                System.out.println(generateExampleConfiguration());
                System.out.flush();
                return;
            }

			if (mode.equals(RUNSERVER_MODE)){
				Properties props = getPropertiesFromConf(optionSet.valueOf(CONF).toString());
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

				Object[] redirectObjects = loadRedirects(props.getProperty(App.REDIRECTS_FILE, "redirects.csv"));
				
				ArrayList<String> crossReferences = (ArrayList<String>)redirectObjects[0];
				Map<String, String> redirectMap = (Map<String, String>)redirectObjects[1];
				
				Server server = new Server(port);
				ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
				context.setContextPath("/");

				context.addServlet(new ServletHolder(new RedirectServlet(redirectMap, crossReferences)), "/*");

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
				server.start();
				server.join();
				return;
			}
			System.err.println("Invalid --mode: " + mode + " mode must be one of the "
                    + "following: " + SUPPORTED_MODES);
            System.exit(3);
		}
		catch(Exception ex){
            ex.printStackTrace();
        }
    }
	
	/**
     * Sets logging level for specific classes in this package
     * @param logLevel 
     */
    public static void setLogLevelForClassesInThisPackage(Level logLevel){
        ch.qos.logback.classic.Logger curLog = null;
        Class[] classPkgs = {App.class,  Configuration.class};
        for (Class c : classPkgs){
            curLog =(ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(c);
            curLog.setLevel(logLevel);
        }
    }
	
	/**
     * Loads properties from configuration file specified by {@code path}
     * @param path Path to configuration file
     * @return Properties found in configuration file passed in
     * @throws IOException thrown by {@link java.util.Properties#load(java.io.InputStream)}
     * @throws FileNotFoundException thrown by {@link java.util.Properties#load(java.io.InputStream)}
     */
    public static Properties getPropertiesFromConf(final String path) throws IOException, FileNotFoundException {
        Properties props = new Properties();
        props.load(new FileInputStream(path));
        return props;
    }

    public static Object[] loadRedirects(String filename) throws IOException {
        Map<String, String> map = new HashMap<>();
		ArrayList<String> uniProtTable = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
			Iterable<CSVRecord> records = CSVFormat.RFC4180.builder()
            .setHeader()
			.setSkipHeaderRecord(true)
			.get()
			.parse(br);
            for (CSVRecord record : records) {
               map.put(record.get("ID").trim().toUpperCase(), record.get("URL").trim());
			   uniProtTable.add(record.get("UNIPROT").trim().toUpperCase() + "\t" + record.get("ID").trim() + "\t" + record.get("OPTIONAL_1").trim());

            }
        }
        return new Object[]{uniProtTable,map};
    }

    public static class RedirectServlet extends HttpServlet {
        private final Map<String, String> redirectMap;
		private final List<String> uniprotMapping;

        public RedirectServlet(Map<String, String> redirectMap, List<String> uniprotMapping) {
            this.redirectMap = redirectMap;
			this.uniprotMapping = uniprotMapping;
			Collections.sort(this.uniprotMapping);

        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String path = req.getPathInfo();
            if (path == null || path.length() <= 1) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No ID provided.");
                return;
            }

            String id = path.substring(1).toUpperCase();
			if (id.equals("STATUS")){
				resp.setContentType("text/plain");
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.getWriter().println("OK");
				return;
			}
			if (id.equals("UNIPROT_MAPPING_FILE")){
				resp.setContentType("text/plain");
				resp.setStatus(HttpServletResponse.SC_OK);

				for (String item : uniprotMapping) {
					resp.getWriter().println(item);
				}
				return;
			}
            String redirectTo = redirectMap.get(id);
            if (redirectTo != null) {
                resp.setStatus(HttpServletResponse.SC_FOUND);
                resp.setHeader("Location", redirectTo);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown ID: " + id);
            }
        }
    }
	
	/**
     * Generates example Configuration file writing to standard out
     * @throws Exception if there is an error
     * @return example configuration
     */
    public static String generateExampleConfiguration() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("# Example configuration file for Redirect service\n\n");

        sb.append("# Sets directory where log files will be written for Jetty web server\n");
        sb.append(App.RUNSERVER_LOGDIR + " = /tmp/logs\n\n");

        sb.append("# Sets port Jetty web service will be run under\n");
        sb.append(App.RUNSERVER_PORT + " = 8081\n\n");

        sb.append("# Path to redirects CSV file with following columns in this order:\n");
        sb.append("# UNIPROT,ID,OPTIONAL_1,URL\n");
		sb.append("# UNIPROT = Uniprot ID\n");
		sb.append("# ID = NDEx ID\n");
		sb.append("# OPTIONAL_1 = description of entry\n");
		sb.append("# URL = URL to redirect to\n");
		
        sb.append(App.REDIRECTS_FILE + " = redirects.csv\n\n");

        sb.append("# App log level. Valid log levels DEBUG INFO WARN ERROR ALL\n");
        sb.append(App.RUNSERVER_LOGLEVEL + " = INFO\n");

        sb.append("# Root log level. Valid log levels DEBUG INFO WARN ERROR ALL\n");
        sb.append(App.ROOT_LOGLEVEL + " = INFO\n");

        return sb.toString();
    }
}
