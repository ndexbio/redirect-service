package org.ndexbio.redirectservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author churas
 */
public class RedirectServletFactory {
	private String _redirectsFile;
	
	public RedirectServletFactory(final Properties appConfiguration){
		_redirectsFile = appConfiguration.getProperty(App.REDIRECTS_FILE, "redirects.csv");
	}
	
	public RedirectServlet getRedirectServlet() throws IOException {
		Object[] redirectObjects = loadRedirects(_redirectsFile);
				
		ArrayList<String> crossReferences = (ArrayList<String>)redirectObjects[0];
		Map<String, String> redirectMap = (Map<String, String>)redirectObjects[1];
		return new RedirectServlet(redirectMap, crossReferences);
		
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
	
}
