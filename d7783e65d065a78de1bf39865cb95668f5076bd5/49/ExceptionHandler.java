package com.asynch.error;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler {

	private Throwable throwable;
	private boolean hasError;

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.hasError = true;
		this.throwable = throwable;
	}

	public boolean hasError() {
		return hasError;
	}
	
	public String getFullStackError(){
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		return stringWriter.toString();
	}

}
