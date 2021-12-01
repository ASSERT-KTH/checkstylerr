package com.synaptix.toast.adapter.swing.component;

import java.util.UUID;

import com.synaptix.toast.adapter.swing.SwingAutoElement;
import com.synaptix.toast.adapter.web.HasStringValue;
import com.synaptix.toast.adapter.web.HasTextInput;
import com.synaptix.toast.adapter.web.HasValueBase;
import com.synaptix.toast.core.driver.IRemoteSwingAgentDriver;
import com.synaptix.toast.core.net.request.CommandRequest;
import com.synaptix.toast.core.report.TestResult;
import com.synaptix.toast.core.runtime.ISwingElement;

public class SwingDateElement extends SwingAutoElement implements HasTextInput, HasValueBase<TestResult> {

	public SwingDateElement(
		ISwingElement element,
		IRemoteSwingAgentDriver driver) {
		super(element, driver);
	}

	public SwingDateElement(
		ISwingElement element) {
		super(element);
	}

	@Override
	public TestResult setInput(
		String e)
		throws Exception {
		exists();
		final String requestId = UUID.randomUUID().toString();
		TestResult res = frontEndDriver.processAndWaitForValue(new CommandRequest.CommandRequestBuilder(requestId)
				.with(wrappedElement.getLocator())
				.ofType(wrappedElement.getType().name()).sendKeys(e).build());
		return res;
	}

	public TestResult setDateText(
		String e)
		throws Exception {
		exists();
		final String requestId = UUID.randomUUID().toString();
		TestResult res = frontEndDriver.processAndWaitForValue(new CommandRequest.CommandRequestBuilder(requestId)
			.with(wrappedElement.getLocator())
			.ofType("date_text").sendKeys(e).build());
		return res;
	}

	@Override
	public TestResult getValue()
		throws Exception {
		exists();
		final String requestId = UUID.randomUUID().toString();
		CommandRequest request = new CommandRequest.CommandRequestBuilder(requestId).with(wrappedElement.getLocator())
			.ofType(wrappedElement.getType().name()).getValue().build();
		return frontEndDriver.processAndWaitForValue(request);
	}
}
