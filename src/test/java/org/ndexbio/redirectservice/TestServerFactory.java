
package org.ndexbio.redirectservice;

import ch.qos.logback.classic.Level;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


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
}
