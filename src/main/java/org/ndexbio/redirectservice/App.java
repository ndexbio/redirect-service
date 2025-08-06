package org.ndexbio.redirectservice;

import org.eclipse.jetty.server.Server;


import java.io.*;
import java.util.Arrays;
import java.util.List;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;


public class App {
	
	static Logger _logger = LoggerFactory.getLogger(App.class);

    public static final String DESCRIPTION = "\nRedirect service\n\n"
            + "For usage information visit:  https://github.com/ndexbio/redirect-service\n\n";

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

    public static int main(String[] args) throws Exception {

		
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
                return 1;
            }

            //help check
            for (String helpArgName : helpArgs) {
                if (optionSet.has(helpArgName)) {
                    System.out.println(DESCRIPTION);
                    parser.printHelpOn(System.out);
                    return 2;
                }
            }
			
			String mode = optionSet.valueOf(MODE).toString();

            if (mode.equals(EXAMPLE_CONF_MODE)){
                System.out.println(generateExampleConfiguration());
                System.out.flush();
                return 0;
            }
			
			if (mode.equals(RUNSERVER_MODE)){
				Properties props = getPropertiesFromConf(optionSet.valueOf(CONF).toString());
				ServerFactory factory = new ServerFactory();
				Server server = factory.getServer(props);
				server.start();
				server.join();
				return 0;
			}
			System.err.println("Invalid --mode: " + mode + " mode must be one of the "
                    + "following: " + SUPPORTED_MODES);
            return 3;
		}
		catch(Exception ex){
            ex.printStackTrace();
			return 4;
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
