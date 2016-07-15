/**
 * Copyright (C) 2016 Luis Moral Guerrero <luis.moral@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.molabs.logback.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import es.molabs.logback.LogbackManager;

@RunWith(MockitoJUnitRunner.class)
public class LogbackManagerTest 
{
	private ByteArrayOutputStream outContent = null;
	private ByteArrayOutputStream errContent = null;
	
	@Test
	public void testInitialization() throws Throwable
	{
		// Without configuration file
		LogbackManager manager = new LogbackManager();		
		
		testInitialization(manager);
		
		// Destroys the manager
		manager.destroy();
		
		// With configuration file
		manager = new LogbackManager(getClass().getResource("/es/molabs/logback/test/manager/logback-test.xml"), false);		
				
		testInitialization(manager);
		
		// Destroys the manager
		manager.destroy();
	}
	
	@Test
	public void testSystemRedirection() throws Throwable
	{
		// Creates the manager with system redirection
		LogbackManager manager = new LogbackManager(getClass().getResource("/es/molabs/logback/test/manager/logback-test.xml"), true);
		manager.init();
		
		// Resets the streams
		outContent.reset();
		errContent.reset();
		
		// Sends a log to system out
		System.out.println("Test System.out.");
		
		// Checks that the console output is using logback
		testLogOutput("[%s] INFO  LogbackManagerTest: Test System.out.", false);
		
		// Sends a log to system err
		System.err.println("Test System.err.");
		
		// Checks that the console output is using logback
		testLogOutput("[%s] ERROR LogbackManagerTest: Test System.err.", false);
		
		// Destroys the manager
		manager.destroy();
	}
	
	@Test
	public void testJulRedirection() throws Throwable
	{
		// Creates the manager
		LogbackManager manager = new LogbackManager(getClass().getResource("/es/molabs/logback/test/manager/logback-test.xml"), true);
		manager.init();
		
		Logger julLogger = Logger.getLogger(getClass().getName());
		
		// Resets the streams
		outContent.reset();
		errContent.reset();
		
		julLogger.info("Test.");
		
		// Checks that the console output is using logback
		testLogOutput("[%s] INFO  LogbackManagerTest: Test.", false);
		
		// Finalizamos el manager
		manager.destroy();
	}
	
	private void testInitialization(LogbackManager manager)
	{
		// Initializes the manager
		manager.init();		
		
		// Checks that is initialized
		testInitialized(manager, true, "[%s] INFO  LogbackManager: Initialized.");		
		
		// Initializes the manager again
		manager.init();
		
		// Checks that is still initialized
		testInitialized(manager, true, "[%s] WARN  LogbackManager: Already initialized.");
		
		// Destroys the manager
		manager.destroy();
		
		// Checks that is not initialized
		testInitialized(manager, false, "[%s] INFO  LogbackManager: Destroyed.");		
		
		// Destroys the manager again
		manager.destroy();
		
		// Checks that is still not initialized
		testInitialized(manager, false, "LogbackManager: Already destroyed.", true);
		
		// Initializes the manager again
		manager.init();
		
		// Checks that is initialized
		testInitialized(manager, true, "[%s] INFO  LogbackManager: Initialized.");
		
		// Destroys the manager again
		manager.destroy();
		
		// Checks that is not initialized
		testInitialized(manager, false, "[%s] INFO  LogbackManager: Destroyed.");
	}
	
	private void testInitialized(LogbackManager manager, boolean initalized, String consoleExpectedValue)
	{
		testInitialized(manager, initalized, consoleExpectedValue, false);
	}
	
	private void testInitialized(LogbackManager manager, boolean initalized, String consoleExpectedValue, boolean errStream)
	{
		// Checks that is initialized
		boolean expectedValue = initalized;
		boolean value = manager.isInitialized();
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue, value);
		
		// Checks that the console output is using logback
		testLogOutput(consoleExpectedValue, errStream);
	}
	
	private void testLogOutput(String expectedValue, boolean errStream)
	{
		expectedValue = String.format(expectedValue, Thread.currentThread().getName());
		
		// Checks that the console output
		String value = (errStream ? errContent.toString().replace("\n", "").replace("\r", "") + " " : outContent.toString().substring(13, outContent.size()).replace("\n", "").replace("\r", ""));
		Assert.assertEquals("Value must be [" + expectedValue + "].", expectedValue + " ", value);
				
		// Resets the streams
		outContent.reset();
		errContent.reset();
	}
	
	@Before
	public void setUp() throws Throwable
	{		
		outContent = new ByteArrayOutputStream();
		errContent = new ByteArrayOutputStream();
		
		System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}
	
	@After
	public void tearDown() throws Throwable
	{
		outContent.close();
		errContent.close();
		
		System.setOut(null);
	    System.setErr(null);
	}
}