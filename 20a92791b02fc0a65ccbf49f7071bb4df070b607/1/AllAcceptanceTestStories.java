package de.codecentric.worblehat.acceptancetests.suite;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.*;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.junit.Before;

import org.junit.ClassRule;

import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.IOException;

import de.codecentric.worblehat.acceptancetests.adapter.SeleniumAdapter;

/**
 * <p>
 * {@link Embeddable} class to run multiple textual stories via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the
 * {@link LoadFromClasspath} story loader is configured.
 * </p>
 */

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@TestPropertySource
@EnableJpaRepositories("de.codecentric.psd.worblehat.domain")
@EntityScan("de.codecentric.psd.worblehat.domain")
@ComponentScan(basePackages = { "de.codecentric.worblehat.acceptancetests", "de.codecentric.psd.worblehat.domain" })
public class AllAcceptanceTestStories extends JUnitStories {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private SeleniumAdapter seleniumAdapter;

	static {
		Testcontainers.exposeHostPorts(8080);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AllAcceptanceTestStories.class);

	@ClassRule
	@SuppressWarnings("rawtypes")
	public static BrowserWebDriverContainer chromeContainer = new BrowserWebDriverContainer<>()
			.withCapabilities(new ChromeOptions()).withRecordingMode(RECORD_ALL, new File("./target/"));;

	@Before
	public void setup() {
		seleniumAdapter.setDriver(chromeContainer.getWebDriver());
		LOGGER.info("Connect to VNC via " + chromeContainer.getVncAddress());
		try {
			Runtime.getRuntime().exec("open " + chromeContainer.getVncAddress());
		} catch (IOException e) {
			// silently fail, if it's not working – e.printStackTrace();
		}
	}

	@Override
	public Configuration configuration() {

		if (!hasConfiguration()) {

			// prepare ReportBuilder
			StoryReporterBuilder reporterBuilder = new StoryReporterBuilder().withFailureTrace(true)
					.withFailureTraceCompression(true).withFormats(Format.CONSOLE, Format.HTML, Format.STATS);

			// necessary to match steps correctly that only differ after the last parameter
			// see http://jbehave.org/reference/stable/prioritising-steps.html
			StepFinder.PrioritisingStrategy prioritisingStrategy = new StepFinder.ByLevenshteinDistance();
			StepFinder stepFinder = new StepFinder(prioritisingStrategy);
			StepCollector usefulStepCollector = new MarkUnmatchedStepsAsPending(stepFinder);

			// general JBehave configuration
			Configuration configuration = new MostUsefulConfiguration().useStepCollector(usefulStepCollector)
					.useStoryControls(new StoryControls().doResetStateBeforeScenario(false))
					.useStoryReporterBuilder(reporterBuilder);

			useConfiguration(configuration);

		}

		return super.configuration();
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new SpringStepsFactory(configuration(), applicationContext);
	}

	@Override
	protected List<String> storyPaths() {
		return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
	}

}
