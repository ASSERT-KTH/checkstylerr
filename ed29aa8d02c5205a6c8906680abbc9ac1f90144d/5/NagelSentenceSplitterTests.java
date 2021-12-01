package eu.excitementproject.eop.lap.biu.en.sentencesplit.nagel;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;

import eu.excitementproject.eop.lap.biu.sentencesplit.AbstractSentenceSplitter;
import eu.excitementproject.eop.lap.biu.sentencesplit.SentenceSplitterTests;
import eu.excitementproject.eop.lap.biu.test.BiuTestUtils;

public class NagelSentenceSplitterTests extends SentenceSplitterTests {

	@BeforeClass
	public static void beforeClass() throws IOException {
		
		// Run tests only under BIU environment
		BiuTestUtils.assumeBiuEnvironment();
	}
	
	public AbstractSentenceSplitter getSplitter() {
		return new NagelSentenceSplitter();
	}

	public static void main(String args[]) {
	    JUnitCore.runClasses(NagelSentenceSplitterTests.class);
	}

}
