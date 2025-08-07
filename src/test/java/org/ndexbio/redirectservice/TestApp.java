package org.ndexbio.redirectservice;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author churas
 */
public class TestApp {
	
	public TestApp() {
	}

	@Test
	public void testGetPropertiesFromConf(@TempDir Path tempDir) throws IOException {
		Path configFile = tempDir.resolve("test.configuration");
		try (FileWriter fw = new FileWriter(configFile.toFile())){
			fw.write(App.REDIRECTS_FILE + "=foo\n");
		}
		Properties props = App.getPropertiesFromConf(configFile.toFile().getAbsolutePath());
		assertEquals("foo", props.getProperty(App.REDIRECTS_FILE));
		assertEquals(1, props.entrySet().size());
	}
	
	@Test
	public void testgenerateExampleConfiguration() throws Exception {
		String exampleConfig = App.generateExampleConfiguration();
		assertNotNull(exampleConfig);
		String[] rows = exampleConfig.split("\n");
		assertEquals(20, rows.length);
	}
	
	
	@Test
	public void testMainGenerateExampleConfig() throws Exception {
		String[] args = {"--mode", App.EXAMPLE_CONF_MODE};
		App.main(args);
	}
}
