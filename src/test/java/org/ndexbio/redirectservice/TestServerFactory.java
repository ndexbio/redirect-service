
package org.ndexbio.redirectservice;

import ch.qos.logback.classic.Level;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.eclipse.jetty.server.Server;
import static org.junit.jupiter.api.Assertions.assertNotNull;



/**
 *
 * @author churas
 */
public class TestServerFactory {
	
	public TestServerFactory() {
	}

	@Test
	public void testsetLogLevelForClassesInThisPackage(){
		ServerFactory.setLogLevelForClassesInThisPackage(Level.toLevel("INFO"));
		ch.qos.logback.classic.Logger curLog = null;

        curLog =(ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(App.class);
        assertEquals(Level.INFO, curLog.getLevel());
	}
	
	@Test
	public void testGetServer(@TempDir Path tempDir) throws Exception {
		
		Path redirectsPath = tempDir.resolve("redirects.csv");
		try (FileWriter fw = new FileWriter(redirectsPath.toString())){
			fw.write("UNIPROT,ID,OPTIONAL_1,URL\n");
			fw.write("uni_id,ndex_id,blah,https://ndexbio.org\n");
		}
		
		ServerFactory factory = new ServerFactory();
		Properties props = new Properties();
		props.setProperty(App.RUNSERVER_LOGDIR, tempDir.toAbsolutePath().toString());
		props.setProperty(App.REDIRECTS_FILE, redirectsPath.toAbsolutePath().toString());
		Server server = factory.getServer(props);
		assertNotNull(server);
	}
}
