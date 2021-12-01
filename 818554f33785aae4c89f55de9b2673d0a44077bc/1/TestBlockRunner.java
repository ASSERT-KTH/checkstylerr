package com.synaptix.toast.runtime.block;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.synaptix.toast.adapter.ActionAdapterCollector;
import com.synaptix.toast.adapter.FixtureService;
import com.synaptix.toast.core.adapter.ActionAdapterKind;
import com.synaptix.toast.core.agent.inspection.ISwingAutomationClient;
import com.synaptix.toast.core.annotation.Action;
import com.synaptix.toast.core.net.request.CommandRequest;
import com.synaptix.toast.core.report.TestResult;
import com.synaptix.toast.core.report.TestResult.ResultKind;
import com.synaptix.toast.core.runtime.ErrorResultReceivedException;
import com.synaptix.toast.dao.domain.impl.test.block.TestBlock;
import com.synaptix.toast.dao.domain.impl.test.block.line.TestLine;
import com.synaptix.toast.runtime.IActionItemRepository;
import com.synaptix.toast.runtime.bean.ActionCommandDescriptor;
import com.synaptix.toast.runtime.bean.TestLineDescriptor;
import com.synaptix.toast.runtime.constant.Property;
import com.synaptix.toast.runtime.utils.ArgumentHelper;

public class TestBlockRunner implements IBlockRunner<TestBlock> {

	private static final Logger LOG = LogManager.getLogger(BlockRunnerProvider.class);

	@Inject
	private IActionItemRepository objectRepository;
	private Injector injector;
	private List<FixtureService> fixtureApiServices;

	@Override
	public void run(TestBlock block) throws IllegalAccessException, ClassNotFoundException {
		for (TestLine line : block.getBlockLines()) {
			long startTime = System.currentTimeMillis();
			TestLineDescriptor descriptor = new TestLineDescriptor(block, line);
			TestResult result = invokeActionAdapterAction(descriptor);
			line.setExcutionTime(System.currentTimeMillis() - startTime);
			if (ResultKind.FATAL.equals(result.getResultKind())) {
				throw new IllegalAccessException("Test execution stopped, due to fail fatal error: "+ line + " - Failed !");
			}
			finaliseResultKind(line, result);
			line.setTestResult(result);
		}
	}


	private void finaliseResultKind(TestLine line, TestResult result) {
		if (isFailureExpected(line, result)) {
			result.setResultKind(ResultKind.SUCCESS);
		} 
		else if (isExpectedResult(line, result)) {
			result.setResultKind(ResultKind.SUCCESS);
		}
	}

	private boolean isFailureExpected(TestLine line, TestResult result) {
		return "KO".equals(line.getExpected())
				&& ResultKind.FAILURE.equals(result.getResultKind());
	}

	private boolean isExpectedResult(TestLine line, TestResult result) {
		return result.getMessage() != null
				&& line.getExpected() != null
				&& result.getMessage().equals(line.getExpected());
	}

	/**
	 * invoke the method matching the test line descriptor
	 * 
	 * @param descriptor: descriptor of current test line
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private TestResult invokeActionAdapterAction(
			TestLineDescriptor descriptor) throws IllegalAccessException,
			ClassNotFoundException {
		TestResult result = null;
		Class<?> actionAdapter = locateActionAdapter(descriptor);
		if (hasFoundActionAdapter(actionAdapter)) {
			result = runThroughLocalActionAdapter(descriptor, actionAdapter);
			updateFatal(result, descriptor);
		} 
		else if (isRequestFromToastStudio()) {
			result = runThroughRemoteAgent(descriptor);
			updateFatal(result, descriptor);
		} 
		else {
			return new TestResult(String.format("Action Implementation - Not Found"),ResultKind.ERROR);
		}
		return result;
	}

	private boolean hasFoundActionAdapter(Class<?> actionAdapter) {
		return actionAdapter != null;
	}

	private boolean isRequestFromToastStudio() {
		return getClassInstance(ISwingAutomationClient.class) != null;
	}

	private void updateFatal(TestResult result, TestLineDescriptor descriptor) {
		if (descriptor.isFailFatalCommand()) {
			if (!result.isSuccess()) {
				result.setResultKind(ResultKind.FATAL);
			}
		}		
	}

	/**
	 * If no class is implementing the command then
	 * process it as a custom command action request sent through Kryo
	 * 
	 * @param descriptor
	 * @return
	 */
	private TestResult runThroughRemoteAgent(TestLineDescriptor descriptor) {
		TestResult result;
		final String command = descriptor.getCommand();
		result = doRemoteActionCall(command, descriptor);
		result.setContextualTestSentence(command);
		return result;
	}

	private TestResult runThroughLocalActionAdapter(
			TestLineDescriptor descriptor, Class<?> actionAdapter) {
		TestResult result;
		final String command = descriptor.getCommand();
		LOG.info(actionAdapter + " : " + command);
		Object connector = getClassInstance(actionAdapter);
		ActionCommandDescriptor commandMethodImpl = findMethodInClass(command, actionAdapter);
		if (commandMethodImpl == null) {
			commandMethodImpl = findMethodInClass(command, actionAdapter);
		}
		result = doLocalActionCall(command, connector, commandMethodImpl);
		return result;
	}

	/**
	 * Locate among registered ActionAdapters the best match to execute
	 * the action command
	 * 
	 * @param action adapter kind (swing, web, service)
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */
	private Class<?> locateActionAdapter(TestLineDescriptor descriptor) throws ClassNotFoundException,
			IllegalAccessException {
		ActionAdapterKind actionAdapterKind = descriptor.getTestLineFixtureKind();
		String actionAdapterName = descriptor.getTestLineFixtureName(); 
		String command = descriptor.getCommand();
		Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
		
		serviceClasses.addAll(collectActionAdaptersByNameAndKind(actionAdapterKind, actionAdapterName, command));
		serviceClasses.addAll(collectActionAdaptersByKind(actionAdapterKind, command));
		
		if (serviceClasses.size() == 0) {
			LOG.error("No Connector found for command: " + command);
			return null;
		} 
		else if (serviceClasses.size() > 1) {
			LOG.warn("Multiple Services of same kind found implementing the same command: " + command);
		}
		return serviceClasses.iterator().next();
	}

	private Set<Class<?>> collectActionAdaptersByKind(
			ActionAdapterKind fixtureKind,
			String command) {
		Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
		for (FixtureService fixtureService : fixtureApiServices) {
			if (fixtureService.fixtureKind.equals(fixtureKind)) {
				ActionCommandDescriptor methodAndMatcher = findMethodInClass(command, fixtureService.clazz);
				if (methodAndMatcher != null) {
					serviceClasses.add(fixtureService.clazz);
				}
			}
		}
		return serviceClasses;
	}

	private Set<Class<?>> collectActionAdaptersByNameAndKind(
			ActionAdapterKind fixtureKind, 
			String fixtureName, 
			String command) {
		Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
		for (FixtureService fixtureService : fixtureApiServices) {
			if (fixtureService.fixtureKind.equals(fixtureKind) && fixtureService.fixtureName.equals(fixtureName)) {
				ActionCommandDescriptor methodAndMatcher = findMethodInClass(command, fixtureService.clazz);
				if (methodAndMatcher != null) {
					serviceClasses.add(fixtureService.clazz);
				}
			}
		}
		return serviceClasses;
	}

	private TestResult doRemoteActionCall(String command,
			TestLineDescriptor descriptor) {
		TestResult result;
		ISwingAutomationClient swingClient = (ISwingAutomationClient) getClassInstance(ISwingAutomationClient.class);
		swingClient.processCustomCommand(buildCommandRequest(command,descriptor));
		if (LOG.isDebugEnabled()) {
			LOG.debug("Client Plugin Mode: Delegating command interpretation to server plugins !");
		}
		result = new TestResult("Commande Inconnue !", ResultKind.INFO);
		return result;
	}

	private TestResult doLocalActionCall(String command, Object instance,
			ActionCommandDescriptor execDescriptor) {
		TestResult result;
		Object[] args = buildArgumentList(execDescriptor);
		final String updatedCommand = updateCommandWithVarValues(command, execDescriptor);
		try {
			result = (TestResult) execDescriptor.method.invoke(instance, args);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			if (e instanceof ErrorResultReceivedException) {
				result = ((ErrorResultReceivedException) e).getResult();
			} else {
				result = new TestResult(ExceptionUtils.getRootCauseMessage(e), ResultKind.FAILURE);
			}
		}
		result.setContextualTestSentence(updatedCommand);
		return result;
	}

	private Object[] buildArgumentList(
			ActionCommandDescriptor execDescriptor) {
		Matcher matcher = execDescriptor.matcher;
		matcher.matches();
		int groupCount = matcher.groupCount();
		Object[] args = new Object[groupCount];
		for (int i = 0; i < groupCount; i++) {
			String group = matcher.group(i + 1);
			args[i] = ArgumentHelper.buildActionAdapterArgument(objectRepository, group);
		}
		return args;
	}

	private String updateCommandWithVarValues(String inCommand, ActionCommandDescriptor execDescriptor) {
		Matcher matcher = execDescriptor.matcher;
		matcher.matches();
		String outCommand = inCommand;
		int groupCount = matcher.groupCount();
		Object[] args = new Object[groupCount];
		for (int i = 0; i < groupCount; i++) {
			String group = matcher.group(i + 1);
			args[i] = ArgumentHelper.buildActionAdapterArgument(objectRepository, group);
			if (isVariable(args, i, group)) {
				outCommand = outCommand.replaceFirst("\\" + group + "\\b", ((String) args[i]).replace("$", "\\$"));
			}
		}
		return outCommand;
	}

	/**
	 * find the method in the action adapter class
	 * matching the command
	 * 
	 * @param command
	 * @param Action Adapter Class
	 * @return
	 */
	public ActionCommandDescriptor findMethodInClass(final String command,
			final Class<?> actionAdapterClass) {
		ActionCommandDescriptor actionCommandWrapper = null;
		Method[] methods = actionAdapterClass.getMethods();
		for (Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(Action.class)) {
					String actionSentence = ((Action) annotation).action();
					String actionAsRegex = ArgumentHelper.convertActionSentenceToRegex(actionSentence);
					Pattern regexPattern = Pattern.compile(actionAsRegex);
					Matcher matcher = regexPattern.matcher(command);
					boolean matches = matcher.matches();
					if (matches) {
						actionCommandWrapper = new ActionCommandDescriptor(method, matcher);
					}
				}
			}
		}
		if (actionCommandWrapper == null && actionAdapterClass.getSuperclass() != null) {
			return findMethodInClass(command, actionAdapterClass.getSuperclass());
		}
		return actionCommandWrapper;
	}

	private boolean isVariable(Object[] args, int i, String group) {
		return group.startsWith("$") && args[i] != null
				&& 
				!group.contains(Property.DEFAULT_PARAM_SEPARATOR);
	}

	private CommandRequest buildCommandRequest(String command,
			TestLineDescriptor descriptor) {
		final CommandRequest commandRequest;
		switch (descriptor.getTestLineFixtureKind()) {
			case service:
				commandRequest = new CommandRequest.CommandRequestBuilder(null)
						.ofType(ActionAdapterKind.service.name())
						.asCustomCommand(command).build();
				break;
			default:
				commandRequest = new CommandRequest.CommandRequestBuilder(null)
						.asCustomCommand(command).build();
				break;
		}
		return commandRequest;
	}



	private Object getClassInstance(Class<?> clz) {
		if (injector != null) {
			try {
				return injector.getInstance(clz);
			} catch (ConfigurationException _ce) {
				return null;
			}
		}
		return null;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;	
		this.objectRepository = injector.getInstance(IActionItemRepository.class);
		this.fixtureApiServices = ActionAdapterCollector.listAvailableServicesByInjection(injector);
	}

}
