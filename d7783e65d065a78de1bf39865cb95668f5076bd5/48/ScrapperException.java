package com.asynch.error;

public class ScrapperException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public ScrapperException(final String message) {
		super(message);
	}
	
	public ScrapperException(final Throwable throwable) {
		super(throwable);
	}
	
	public ScrapperException(final String message, final Throwable throwable){
		super(message, throwable);
	}
}
