package org.deidentifier.arx.test.ui.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import extension.junit.AUTs;

public class JubulaRunner extends BlockJUnit4ClassRunner {


	/** user information */
	private static ToolkitInfo toolkit = SwingComponents.getToolkitInformation(); // the Toolkit used
	private static AUTConfiguration configuration = AUTs.SIMPLEADDER.getConfig(); // the configuration which should be started
	private static String applicationFirstWindow = ".*"; // uses matches

	/** settings */
	private static final boolean shouldRestartAfterFail = true;
	private static final boolean shouldTakeScreenshot = true;
	
	/** implementation */
	private static AUT aut;
	private static AUTIdentifier startAUT;

	public JubulaRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
	}

	@Override
	public void run(RunNotifier notifier) {
		if (aut == null) {
			AUTAgent agent = Embedded.INSTANCE.agent();
			agent.connect();
			startAut(agent);
		}
        EachTestNotifier testNotifier = new EachTestNotifier(notifier,
                getDescription());
        try {
            Statement statement = classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(handleTestExecutionException(e, getDescription()));
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
        	
            testNotifier.addFailure(handleTestExecutionException(e, getDescription()));
        }
	}
	
	public static <T extends Throwable> T handleTestExecutionException(T throwable, Description description) {
		String testMethod = description.getMethodName();
		System.err.println("Error in: " + testMethod);
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
					String name = testMethod;
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

	/**
	 * starts the AUT
	 * 
	 * @param agent the {@link AutAgent}
	 */
	private static void startAut(AUTAgent agent) {
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
