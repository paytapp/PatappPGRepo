package com.paymentgateway.commons.exception;

public class DataAccessLayerException extends RuntimeException{

	
	private static final long serialVersionUID = 4673730105560516979L;

	public DataAccessLayerException() {
    }

    public DataAccessLayerException(String message) {
        super(message);
    }

    public DataAccessLayerException(Throwable cause) {
        super(cause);
    }

    public DataAccessLayerException(String message, Throwable cause) {
        super(message, cause);
    }
}
