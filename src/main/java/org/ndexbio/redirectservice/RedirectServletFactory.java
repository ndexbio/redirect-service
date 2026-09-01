package org.ndexbio.redirectservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class RedirectServletFactory {
    private static final Logger _logger = LoggerFactory.getLogger(RedirectServletFactory.class);
    
    private final String _redirectsFile;
    private final String _pathwayRedirectsFile;
	
    public RedirectServletFactory(final Properties appConfiguration){
            _redirectsFile = appConfiguration.getProperty(App.REDIRECTS_FILE, "redirects.csv");
            _pathwayRedirectsFile = appConfiguration.getProperty(App.PATHWAY_REDIRECTS_FILE, "pathway_redirects.csv");
    }

    public RedirectServlet getRedirectServlet() throws Exception {
        Object[] redirectObjects = loadRedirects(_redirectsFile);

        ArrayList<String> crossReferences = (ArrayList<String>)redirectObjects[0];
        Map<String, String> redirectMap = (Map<String, String>)redirectObjects[1];

        Map<String, String> pathwayRedirectMap = null;
        if (doesPathwayRedirectsFileExist(_pathwayRedirectsFile)){
           pathwayRedirectMap = loadPathwayRedirects(_pathwayRedirectsFile);
        }

        return new RedirectServlet(redirectMap, crossReferences, pathwayRedirectMap);

    }
	
    protected boolean doesPathwayRedirectsFileExist(final String filename){
        Path path = Paths.get(filename);
        try {
            return Files.isRegularFile(path) && Files.size(path) > 0;
        } catch(IOException ioe){
            _logger.error("Unable to read pathway redirects file", ioe);
        }
        return false;
    }
    
    protected  Object[] loadRedirects(String filename) throws IOException {
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

    protected  Map<String, String> loadPathwayRedirects(String filename) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
			Iterable<CSVRecord> records = CSVFormat.RFC4180.builder()
            .setHeader()
			.setSkipHeaderRecord(true)
			.get()
			.parse(br);
            for (CSVRecord record : records) {
               map.put(record.get("ID").trim().toUpperCase(), record.get("URL").trim());

            }
        }
        return map;
    }
	
}
