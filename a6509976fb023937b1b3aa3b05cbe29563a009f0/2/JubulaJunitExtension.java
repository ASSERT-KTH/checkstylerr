package extension.junit;
/*******************************************************************************
 * Copyright (c) 2019 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.eclipse.jubula.autagent.Embedded;
import org.eclipse.jubula.autagent.common.agent.AutAgent;
import org.eclipse.jubula.client.AUT;
import org.eclipse.jubula.client.AUTAgent;
import org.eclipse.jubula.client.AUTRegistry;
import org.eclipse.jubula.client.launch.AUTConfiguration;
import org.eclipse.jubula.toolkit.ToolkitInfo;
import org.eclipse.jubula.toolkit.enums.ValueSets.Operator;
import org.eclipse.jubula.toolkit.swing.SwingComponents;
import org.eclipse.jubula.tools.AUTIdentifier;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

/**
 * Handles the lifecycle of the AUT in between tests. If there was an Problem
 * during a test, it is most likely that the AUT should be restarted because we
 * do not know the exact state of the Application
 * 
 * this class needs adaption to use the correct toolkit / filter for error messages
 * @author BREDEX GmbH
 */
public class JubulaJunitExtension implements BeforeTestExecutionCallback, TestExecutionExceptionHandler {

	/** user information */
	private ToolkitInfo toolkit = SwingComponents.getToolkitInformation(); // the Toolkit used
	private AUTConfiguration configuration = AUTs.SIMPLEADDER.getConfig(); // the configuration which should be started
	private String applicationFirstWindow = ".*"; // uses matches

	/** settings */
	private static final boolean shouldRestartAfterFail = true;
	private static final boolean shouldTakeScreenshot = true;
	
	/** implementation */
	private static AUT aut;
	private static AUTIdentifier startAUT;

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		Optional<Method> testMethod = context.getTestMethod();
		testMethod.ifPresent(m -> System.err.println("Error in: " + m));
		List<StackTraceElement> collect = Arrays.stream(throwable.getStackTrace()).filter(v -> {
			String className = v.getClassName();
			boolean contains = className.contains("test")
					|| (className.contains("jubula") && className.contains("toolkit"));
			return contains;
		}).collect(Collectors.toList());
		collect.forEach(v -> System.err.println(v));
		if (shouldTakeScreenshot) {
			if (aut.isConnected()) {
				try {
					BufferedImage screenshot = aut.getScreenshot();
					String name = testMethod.isPresent() ? testMethod.get().getName() : "screenshot";
					File file = new File("./result/" + name + ".png");
					file.getParentFile().mkdirs();
					ImageIO.write(screenshot, "png", file);
				} catch (Exception e) {
					System.err.println("Error during creation of screenshot" + e);
				}
			}
		}
		if (shouldRestartAfterFail) {
			AUTAgent agent = Embedded.INSTANCE.agent();
			agent.stopAUT(startAUT);
			if (agent.isConnected()) {
				startAut(agent);
			}
		}
		throw throwable;
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		if (aut == null) {
			AUTAgent agent = Embedded.INSTANCE.agent();
			agent.connect();
			startAut(agent);
		}
	}

	/**
	 * starts the AUT
	 * 
	 * @param agent the {@link AutAgent}
	 */
	private void startAut(AUTAgent agent) {
		try {
			Thread.interrupted();
			Thread.sleep(5000);
		} catch (Exception e) {
		}
		startAUT = agent.startAUT(configuration);
		aut = agent.getAUT(startAUT, toolkit);
		aut.connect();
		AUTRegistry.INSTANCE.register(aut);
		SwingComponents.createApplicationActionHandler().waitForWindow(applicationFirstWindow, Operator.matches, 500);
	}

}
