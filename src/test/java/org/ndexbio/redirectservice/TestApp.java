package org.ndexbio.redirectservice;

import ch.qos.logback.classic.Level;
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
		assertEquals(0, App.main(args));
	}
	
	@Test
	public void testMainHelp() throws Exception {
		String[] args = {"--help"};
		assertEquals(2, App.main(args));
	}
	
	@Test
	public void testMainInvalidArgument() throws Exception {
		String[] args = {"--invalid"};
		assertEquals(1, App.main(args));
	}
	
	@Test
	public void testMainInvalidMode() throws Exception {
		String[] args = {"--mode", "invalid"};
		assertEquals(3, App.main(args));
	}
	
	@Test
	public void testMainMissingConfiguration(@TempDir Path tempDir) throws Exception {
		Path configFile = tempDir.resolve("test.configuration");
		String[] args = {"--mode", App.RUNSERVER_MODE, "--conf", configFile.toAbsolutePath().toString()};
		assertEquals(4, App.main(args));
	}
}
