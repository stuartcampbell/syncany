/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2013 Philipp C. Heckel <philipp.heckel@gmail.com> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.tests.scenarios.longrunning;

import static org.junit.Assert.fail;
import static org.syncany.tests.util.TestAssertUtil.assertDatabaseFileEquals;
import static org.syncany.tests.util.TestAssertUtil.assertFileListEquals;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.syncany.config.Logging;
import org.syncany.connection.plugins.Connection;
import org.syncany.tests.util.TestClient;
import org.syncany.tests.util.TestConfigUtil;

public class LongRunningNewAndDeleteScenarioTest {
	private static final Logger logger = Logger.getLogger(LongRunningNewAndDeleteScenarioTest.class.getSimpleName());
	
	@Test
	public void testLongRunningNewAndDeleteFilesNoConflicts() throws Exception {
		// Setup 
		Connection testConnection = TestConfigUtil.createTestLocalConnection();
		
		TestClient clientA = new TestClient("A", testConnection);
		TestClient clientB = new TestClient("B", testConnection);
		
		// Disable logging
		logger.log(Level.INFO, "NOTE: This test can take several minutes!");
		logger.log(Level.INFO, "Disabling console logging for this test; Too much output/overhead.");
		
		Logging.disableLogging();
		
		// Run 
		for (int round=1; round<30; round++) {
			
			// A
			for (int i=1; i<100; i++) { clientA.createNewFile("A-file-with-size-"+i+".jpg", i); }	
			clientA.up();	
			
			// B 
			clientB.down();						
			assertFileListEquals(clientA.getLocalFilesExcludeLockedAndNoRead(), clientB.getLocalFilesExcludeLockedAndNoRead());
			assertDatabaseFileEquals(clientA.getLocalDatabaseFile(), clientB.getLocalDatabaseFile(), clientA.getConfig().getTransformer());
		
			for (int i=1; i<100; i++) { clientB.changeFile("A-file-with-size-"+i+".jpg"); }
			clientB.up();	
			
			// A 
			clientA.down();						
			assertFileListEquals(clientA.getLocalFilesExcludeLockedAndNoRead(), clientB.getLocalFilesExcludeLockedAndNoRead());
			assertDatabaseFileEquals(clientA.getLocalDatabaseFile(), clientB.getLocalDatabaseFile(), clientA.getConfig().getTransformer());
		
			for (int i=1; i<100; i++) { clientA.deleteFile("A-file-with-size-"+i+".jpg"); }
			clientA.up();	
			
			// B 
			clientB.down();						
			assertFileListEquals(clientA.getLocalFilesExcludeLockedAndNoRead(), clientB.getLocalFilesExcludeLockedAndNoRead());
			assertDatabaseFileEquals(clientA.getLocalDatabaseFile(), clientB.getLocalDatabaseFile(), clientA.getConfig().getTransformer());			
		}

		fail("No asserts yet.");
		
		// Tear down
		clientA.cleanup();
		clientB.cleanup();
	}
}
