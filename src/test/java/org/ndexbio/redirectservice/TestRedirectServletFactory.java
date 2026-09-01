package org.ndexbio.redirectservice;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;



/**
 *
 * @author churas
 */
public class TestRedirectServletFactory {
	
	public TestRedirectServletFactory() {
	}
	
	@Test
	public void testLoadRedirects(@TempDir Path tempDir) throws Exception {
		Path redirectsPath = tempDir.resolve("redirects.csv");
		try (FileWriter fw = new FileWriter(redirectsPath.toString())){
			fw.write("UNIPROT,ID,OPTIONAL_1,URL\n");
			fw.write("uni_id,ndex_id,blah,https://ndexbio.org\n");
		}
		Properties props = new Properties();
		props.setProperty(App.REDIRECTS_FILE, redirectsPath.toAbsolutePath().toString());
		RedirectServletFactory rsf = new RedirectServletFactory(props);
		Object[] res = rsf.loadRedirects(redirectsPath.toAbsolutePath().toString());
		assertEquals(2, res.length);
		
		ArrayList<String> uniProtTable = (ArrayList<String>)res[0];
		Map<String, String> map = (Map<String, String>)res[1];
		
		assertEquals("UNI_ID\tndex_id\tblah",uniProtTable.get(0));
		assertEquals("https://ndexbio.org", map.get("NDEX_ID"));
	}
        
        @Test
	public void testLoadPathwayRedirects(@TempDir Path tempDir) throws Exception {
		
                Path pathwayRedirectsPath = tempDir.resolve("pathway_redirects.csv");
                try (FileWriter fw = new FileWriter(pathwayRedirectsPath.toString())){
			fw.write("ID,URL\n");
			fw.write("some_id,https://someid.ndexbio.org\n");
		}
		Properties props = new Properties();
		
                props.setProperty(App.PATHWAY_REDIRECTS_FILE, pathwayRedirectsPath.toAbsolutePath().toString());
		RedirectServletFactory rsf = new RedirectServletFactory(props);
		Map<String, String> res = rsf.loadPathwayRedirects(pathwayRedirectsPath.toAbsolutePath().toString());
		assertNotNull(res);
                assertEquals(1, res.size());
		
		assertEquals("https://someid.ndexbio.org", res.get("SOME_ID"));
	}

	@Test
	public void testgetRedirectServletNoPathwayRedirect(@TempDir Path tempDir) throws Exception {
		Path redirectsPath = tempDir.resolve("redirects.csv");
		try (FileWriter fw = new FileWriter(redirectsPath.toString())){
			fw.write("UNIPROT,ID,OPTIONAL_1,URL\n");
			fw.write("uni_id,ndex_id,blah,https://ndexbio.org\n");
		}
		Properties props = new Properties();
		props.setProperty(App.REDIRECTS_FILE, redirectsPath.toAbsolutePath().toString());
		RedirectServletFactory rsf = new RedirectServletFactory(props);
		RedirectServlet servlet = rsf.getRedirectServlet();
		
		assertNotNull(servlet);
	}
        
        @Test
	public void testgetRedirectServlet(@TempDir Path tempDir) throws Exception {
		Path redirectsPath = tempDir.resolve("redirects.csv");
		try (FileWriter fw = new FileWriter(redirectsPath.toString())){
			fw.write("UNIPROT,ID,OPTIONAL_1,URL\n");
			fw.write("uni_id,ndex_id,blah,https://ndexbio.org\n");
		}
                Path pathwayRedirectsPath = tempDir.resolve("pathway_redirects.csv");
                try (FileWriter fw = new FileWriter(pathwayRedirectsPath.toString())){
			fw.write("ID,URL\n");
			fw.write("some_id,https://someid.ndexbio.org\n");
		}
		Properties props = new Properties();
		props.setProperty(App.REDIRECTS_FILE, redirectsPath.toAbsolutePath().toString());
                props.setProperty(App.PATHWAY_REDIRECTS_FILE, pathwayRedirectsPath.toAbsolutePath().toString());
		RedirectServletFactory rsf = new RedirectServletFactory(props);
		RedirectServlet servlet = rsf.getRedirectServlet();
		
		assertNotNull(servlet);
	}

	
}
