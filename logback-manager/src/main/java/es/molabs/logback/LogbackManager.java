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
package es.molabs.logback;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class LogbackManager 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static String LOGBACK_DEFAULT_CONFIGURATION = "/es/molabs/logback/logback-default.xml";

	private URL configurationFile = null;
	private boolean redirectSystem;
	
	private boolean initialized;
	
	public LogbackManager()
	{
		this(LogbackManager.class.getResource(LOGBACK_DEFAULT_CONFIGURATION), false);
	}
	
	public LogbackManager(URL configurationFile, boolean redirectSystem)
	{
		this.configurationFile = configurationFile;
		this.redirectSystem = redirectSystem;
		
		initialized = false;
	}
	
	public void init()
	{
		if (!initialized)
		{			
			// Initializes logback with its configuration file
			initLogback(configurationFile);
			
			// Installs the JulToSlf4j bridge
			installJulToSlf4j();
					
			// If SystemOut and SystemErr redirection is active
			if (redirectSystem) SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
			
			// Sets the manager as initialized
			initialized = true;
			
			logger.info("Initialized.");
		}
		else
		{
			logger.warn("Already initialized.");
		}
	}
	
	public void destroy()
	{
		if (initialized)
		{
			logger.info("Destroyed.");
			
			// If SystemOut and SystemErr redirection is active
			if (redirectSystem) SysOutOverSLF4J.restoreOriginalSystemOutputs();
			
			// Uninstall the JulToSlf4j bridge
			uninstallJulToSlf4j();
			
			// Clears the logback configuration
			destroyLogback();
			
			// Clears the MDC
			MDC.clear();
			
			// Sets the manager as not initialized
			initialized = false;
		}
		else
		{
			System.err.println(getClass().getSimpleName() + ": Already destroyed.");
		}
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}	
	
	private void initLogback(URL file)
	{
		// Gets the context
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		// Resets the context to remove any auto loaded xml file in the classpath
		context.reset();
				
		try
		{
			// Creates the configurator
			JoranConfigurator configurator = new JoranConfigurator();
			
			// Sets the context
			configurator.setContext(context);
			
			// Configures with the configuration file
			configurator.doConfigure(file);			
		}
		catch (JoranException je) 
		{
			// StatusPrinter will shot this exception
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		// Adds the context to the level propagator for JUL
		context.addListener(new LevelChangePropagator());
		
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);			
	}
	
	private void destroyLogback()
	{
		// Gets the context
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		// Resets the context to remove any loaded logger
		context.reset();
	}
	
	private void installJulToSlf4j()
	{
		// Removes any JUL handler
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		
		// Installs the JUL -> SLF4J bridge
		SLF4JBridgeHandler.install();
		
		// Sets the level of the JUL global logger
		//java.util.logging.Logger.getLogger("").setLevel(Level.ALL);
	}
	
	private void uninstallJulToSlf4j()
	{
		// Uninstall the JUL -> SLF4J bridge
		SLF4JBridgeHandler.uninstall();
	}
}
