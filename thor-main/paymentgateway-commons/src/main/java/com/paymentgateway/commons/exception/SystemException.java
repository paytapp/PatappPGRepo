package com.paymentgateway.commons.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.paymentgateway.commons.util.FieldType;

public class SystemException extends Exception {

	private static final long serialVersionUID = 8826422388207613223L;

	private ErrorType errorType = ErrorType.UNKNOWN;
	private static Logger logger = LoggerFactory.getLogger(SystemException.class.getName());

	public SystemException(ErrorType errorType, Throwable cause, String message) {
		super(message, cause);
		this.errorType = errorType;
		log(cause, message, message);
	}

	public SystemException(){
		
	}
	public SystemException(String message) {
		super(message);
	}

	
	
	public void log(Throwable cause, String mdc, String message) {
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), mdc);
		logger.error(message, cause);
	}

	public SystemException(ErrorType errorType, String message) {
		super(message);
		this.errorType = errorType;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public void logMessage() {
		StringBuilder message = new StringBuilder();
		message.append("Message :");
		message.append(getMessage());
		message.append(", Code :");
		message.append(errorType.getCode());
		message.append(", Response Message : ");
		message.append(errorType.getResponseMessage());
		message.append(", Internal Response Message : ");
		message.append(errorType.getInternalMessage());

		logger.error("error" + message);
	}
}
