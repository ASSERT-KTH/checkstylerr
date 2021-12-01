package com.synaptix.toast.core.runtime;

import com.synaptix.toast.core.report.TestResult;

public class ErrorResultReceivedException extends Exception {

	private static final long serialVersionUID = 1L;

	private TestResult result;

	public ErrorResultReceivedException(final TestResult result) {
		this.result = result;
	}

	public TestResult getResult() {
		return result;
	}
}