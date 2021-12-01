package org.stjs.testing.driver.browser;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.stjs.generator.BridgeClass;
import org.stjs.generator.ClassWithJavascript;
import org.stjs.testing.driver.AsyncProcess;
import org.stjs.testing.driver.DriverConfiguration;
import org.stjs.testing.driver.HttpLongPollingServer;
import org.stjs.testing.driver.JUnitSession;
import org.stjs.testing.driver.MultiTestMethod;
import org.stjs.testing.driver.TestClassAttributes;
import org.stjs.testing.driver.TestClassAttributesRepository;
import org.stjs.testing.driver.TestResult;

import com.google.common.base.Strings;
import com.sun.net.httpserver.HttpExchange;

/**
 * Represents a testing session opened with one instance of a browser that uses long-polling to fetch new tests to execute from the HTTP server.
 * LongPollingBrowser handles multithreading synchronization between the browser, the HTTP server and the JUnit runner. The JUnit runner
 * notifies
 * this browser that a new test method must be executed by calling executeTest(MultiTestMethod), or that it has finished executing all the tests
 * by calling notifyNoMoreTests(). The HTTP server waits for a new test to send to the browser by calling awaitNewTestReady(). <br>
 * <br>
 * On top of that, LongPollinBrowser delegates the details of starting and stopping the browser itself to its concrete subclasses.
 *
 * @author lordofthepigs
 */
@SuppressWarnings({"restriction", "deprecation"})
public abstract class LongPollingBrowser extends AbstractBrowser {

	private final Exchanger<MultiTestMethod> exchanger = new Exchanger<MultiTestMethod>();
	private final TestClassAttributesRepository testClasses;
	private volatile MultiTestMethod methodUnderExecution = null;
	private long id;
	private volatile boolean isDead = false;

	public LongPollingBrowser(DriverConfiguration config) {
		super(config);
		testClasses = new TestClassAttributesRepository(config.getStjsClassResolver(), config.getDependencyCollector());
	}

	protected String getStartPageUri(long browserId, boolean persistent) {
		return "start.html?browserId=" + browserId + "&persistent=" + persistent;
	}

	protected String getStartPageUrl(long browserId, boolean persistent) {
		return getConfig().getServerURL() + getStartPageUri(browserId, persistent);
	}

	protected void registerWithLongPollingServer() {
		this.id = JUnitSession.getInstance().getDependency(HttpLongPollingServer.class).registerBrowserSession(this);
		if (getConfig().isDebugEnabled()) {
			System.out.println("Browser " + id + " is " + this.getClass().getSimpleName());
		}
	}

	/**
	 * Starts the browser session. This will open a browser and navigate it to some page where the unit testing procedure can be started. The
	 * decision about exactly which browser binary is started, how it is started and which page is opened is delegated to the Browser
	 * implementation that this AsynBrowserSession was constructed with. This method performs some error handling, and the real implementation
	 * of
	 * the browser starting procedure is delegated to doStart().
	 */
	@Override
	public void start() throws InitializationError {
		try {
			this.doStart();
		}
		catch (InitializationError ie) {
			this.markAsDead();
			throw ie;
		}
		catch (Throwable t) {
			this.markAsDead();
			throw new InitializationError(t);
		}
	}

	protected abstract void doStart() throws InitializationError;

	/**
	 * Blocks until JUnit notifies this browser session that either a new test must be executed (ie: executeTest() is called), or there are no
	 * more tests (ie: notifyNoMoreTests() is called). If there is a new test to execute, then this method returns it. If there are no more
	 * tests, this method returns null.<br>
	 * <br>
	 * This method is typically called right after the results of the previous test were reported.
	 *
	 * @return The next test to execute, or null if there isn't any
	 */
	public MultiTestMethod awaitNextTest() {
		try {
			if (getConfig().isDebugEnabled()) {
				System.out.println("Browser " + this.id + " is waiting for a new test");
			}

			// We now wait for the JUnit thread to supply our thread with the next test.
			// there is no need to put a timeout here, because if JUnit fails to deliver a new test,
			// this means that something really bad has happened and that the JUnit JVM will probably terminate very
			// soon, executing all cleanup actions.
			methodUnderExecution = exchanger.exchange(null);

			if (getConfig().isDebugEnabled()) {
				if (methodUnderExecution != null) {
					System.out.println("Browser " + this.id + " has picked up the test " + methodUnderExecution.getMethod().getMethod());
				} else {
					System.out.println("Browser " + this.id + " has no more tests");
				}
			}
			return methodUnderExecution;
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Notifies this browser that the specified test must be executed. This method blocks until this browser picks up the test by calling
	 * awaitNextTest(). If the browser does not pick up the test within the timeout specified in DriverConfiguration.getTestTimeout(), then the
	 * browser is assumed to be dead. The test is failed, and the browser does not receive any more tests at all.
	 *
	 * @param method The test to execute.
	 */
	@Override
	public void executeTest(MultiTestMethod method) {
		if (this.isDead) {
			this.reportAsDead(method);
			return;
		}
		try {
			if (getConfig().isDebugEnabled()) {
				System.out.println("Test " + method.getMethod().getMethod() + " is available for browser " + this.id);
			}
			exchanger.exchange(method, getConfig().getTestTimeout(), TimeUnit.SECONDS);
			if (getConfig().isDebugEnabled()) {
				System.out.println("Browser " + this.id + " has picked up the new test");
			}
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (TimeoutException e) {
			// the browser failed to pick up the test in time.
			this.markAsDead();
			this.reportAsDead(method);
		}
	}

	/**
	 * Reports this browser as dead to the specified test method. The test will be failed.
	 */
	private void reportAsDead(MultiTestMethod method) {
		method.notifyExecutionResult(TestResult.deadBrowser(this.getClass().getSimpleName(),
				getConfig().getTestTimeout() + " seconds passed and the browser didn't contact back the ST-JS JUnit runner"));
	}

	/**
	 * Notifies this browser that there are no more tests to execute. This method blocks until this browser attempts to pick up a new test by
	 * calling awaitNewTestReady(). If the browser does not attempt to pick up a new test within the timeout specified in
	 * DriverConfiguration.getTestTimeout(), then the browser is assumed to be dead.
	 */
	@Override
	public void notifyNoMoreTests() {
		if (this.isDead) {
			return;
		}
		try {
			if (getConfig().isDebugEnabled()) {
				System.out.println("Browser " + this.id + " has been notified that no more tests are coming");
			}
			exchanger.exchange(null, getConfig().getTestTimeout(), TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (TimeoutException e) {
			// the browser failed to pick up the test in time.
			this.markAsDead();
		}
	}

	/**
	 * Returns the test is currently being executed by this browser.
	 */
	public MultiTestMethod getMethodUnderExecution() {
		return methodUnderExecution;
	}

	/**
	 * Writes to the HTTP response the HTML and/or javascript code that is necessary for the browser to execute the specified test.
	 *
	 * @param meth     The test to send to the browser
	 * @param exchange contains the HTTP response that must be written to
	 */
	public void sendTestFixture(MultiTestMethod meth, HttpExchange exchange) throws Exception {
		TestClassAttributes attr = testClasses.getAttributes(meth.getTestClass());
		final Test test = meth.getMethod().getAnnotation(Test.class);

		StringBuilder resp = new StringBuilder(8192);
		resp.append("<html>\n");
		resp.append("<head>\n");
		appendScriptTag(resp, httpPath(new URI("webjar:/stjs.js")));
		appendScriptTag(resp, "/junit.js");

		resp.append("<script language='javascript'>stjs.mainCallDisabled=true;</script>\n");

		// scripts added explicitly
		for (String script : attr.getScripts()) {
			appendScriptTag(resp, script);
		}
		// scripts before - new style
		for (String script : attr.getScriptsBefore()) {
			appendScriptTag(resp, script);
		}

		Set<URI> jsUris = new LinkedHashSet<>();
		for (ClassWithJavascript dep : attr.getDependencies()) {

			if (!attr.getScripts().isEmpty() && dep instanceof BridgeClass) {
				// bridge dependencies are not added when using @Scripts
				System.out.println(
						"WARNING: You're using @Scripts deprecated annotation that disables the automatic inclusion of the Javascript files of "
								+ "the bridges you're using! " + "Please consider using @ScriptsBefore and/or @ScriptsAfter instead.");
				continue;
			}
			for (URI file : dep.getJavascriptFiles()) {
				jsUris.add(file);
			}
		}

		for (URI uri : jsUris) {
			appendScriptTag(resp, httpPath(uri));
		}

		// scripts after - new style
		for (String script : attr.getScriptsAfter()) {
			appendScriptTag(resp, script);
		}

		resp.append("<script language='javascript'>\n");
		if (getConfig().isDebugJavaScript()) {
			resp.append(" function runTest() {\n");
			resp.append("    (elem=document.getElementById('startSection')).parentNode.removeChild(elem);\n");
		} else {
			resp.append("  window.onload=function(){\n");
		}
		// resp.append("    console.error(document.getElementsByTagName('html')[0].innerHTML);\n");

		// Adapter between generated assert (not global) and JS-test-driver assert (which is a
		// set of global methods)
		resp.append("    Assert=window;\n");
		resp.append("    try{\n");

		String testedClassName = attr.getStjsClass().getJavascriptClassName();
		resp.append("        parent.startingTest('" + testedClassName + "', '" + meth.getName() + "');\n");
		resp.append("        var stjsTest = new " + testedClassName + "();\n");
		resp.append("        var stjsResult = 'OK';\n");

		String expectedExceptionConstructor = "null";
		if (test.expected() != Test.None.class) {
			ClassWithJavascript exceptionClass = getConfig().getStjsClassResolver().resolve(test.expected().getName());
			expectedExceptionConstructor = exceptionClass.getJavascriptClassName();
		}
		resp.append("        var expectedException = " + expectedExceptionConstructor + ";\n");

		// call before methods
		for (FrameworkMethod beforeMethod : attr.getBeforeMethods()) {
			resp.append("      stjsTest." + beforeMethod.getName() + "();\n");
		}
		// call the test's method
		resp.append("      stjsTest." + meth.getName() + "();\n");
		resp.append("      if(expectedException){\n");
		resp.append("        stjsResult = 'Expected an exception, but none was thrown';\n");
		resp.append("      }\n");
		resp.append("    }catch(ex){\n");

		// an exception was caught while executing the test method
		resp.append("      if(!expectedException){\n");
		resp.append("        stjsResult = ex;\n");
		resp.append("      } else if (!stjs.isInstanceOf(ex.constructor,expectedException)){\n");
		resp.append("        stjsResult = ex;\n");
		resp.append("      }\n");
		resp.append("    }finally{\n");
		// call after methods
		for (FrameworkMethod afterMethod : attr.getAfterMethods()) {
			resp.append("     stjsTest." + afterMethod.getName() + "();\n");
		}
		resp.append("      parent.reportResultAndRunNextTest(stjsResult, stjsResult.location);\n");
		resp.append("     }\n");
		resp.append("  }\n");
		resp.append("</script>\n");
		resp.append("</head>\n");
		resp.append("<body>\n");
		if (getConfig().isDebugJavaScript()) {
			resp.append("<div id='startSection'>\n");
			resp.append("  <h2>JavaScript debugging mode</h2>\n");
			resp.append("  <ul>\n");
			resp.append("    <li>Open your developer tools</li>\n");
			resp.append("    <li>Setup your breakpoints and debugging options</li>\n");
			resp.append("    <li>Start the test</li>\n");
			resp.append("  </ul>\n");
			resp.append("  <button onclick='runTest()'>\n");
			resp.append("    Start " + attr.getStjsClass().getJavaClass().getSimpleName() + "." + meth.getName() + "\n");
			resp.append("  </button>\n");
			resp.append("</div>\n");
		}
		if (attr.getHtmlFixture() != null) {
			if (!Strings.isNullOrEmpty(attr.getHtmlFixture().value())) {
				resp.append(attr.getHtmlFixture().value());

			} else if (!Strings.isNullOrEmpty(attr.getHtmlFixture().url())) {
				StringWriter writer = new StringWriter();
				getConfig().getResource(attr.getHtmlFixture().url()).copyTo(writer);
				resp.append(writer.toString());
			}
		}
		resp.append("</body>\n");
		resp.append("</html>\n");

		sendResponse(resp.toString(), exchange);
	}

	protected String httpPath(URI uri) {
		if ("webjar".equals(uri.getScheme())) {
			return "/webjars" + uri.getPath();

		} else if ("classpath".equals(uri.getScheme())) {
			return uri.getPath();
		}

		// all the other cases (.war packaging, old .jar packaging)
		return uri.toString();
	}

	/**
	 * Writes to the HTTP response the HTML and/or javascript code that is necessary for the browser understand that there will be no more
	 * tests.
	 *
	 * @param exchange contains the HTTP response that must be written to
	 */
	public void sendNoMoreTestFixture(HttpExchange exchange) throws IOException {
		sendResponse("<html><body><h1>Tests completed!</h1></body></html>", exchange);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Class<? extends AsyncProcess>> getSharedDependencies() {
		return processSet(HttpLongPollingServer.class);
	}

	protected void markAsDead() {
		this.isDead = true;
	}

	public void markAsDead(Throwable throwable, String userAgent) {
		this.isDead = true;
		this.methodUnderExecution.notifyExecutionResult(TestResult.deadBrowser(userAgent, throwable.getMessage()));
	}

	public long getId() {
		return this.id;
	}

	private static class TestClassCache {

	}
}
