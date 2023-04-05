package com.paymentgateway.crypto.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;

@ControllerAdvice
public class ErrorController {

	private static Logger logger = LoggerFactory.getLogger(ErrorController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handle(Exception ex, HttpServletRequest request, HttpServletResponse response) {
		
		 HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
         response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
         JSONObject jsonObj  = new JSONObject();
		if (ex instanceof SystemException) {
			jsonObj.put(FieldType.RESPONSE_CODE.getName(), ((SystemException) ex).getErrorType().getResponseCode());
			jsonObj.put(FieldType.RESPONSE_MESSAGE.getName(), ((SystemException) ex).getErrorType().getResponseMessage());
			return new ResponseEntity<>(jsonObj.toString(),headers,HttpStatus.OK);
		}else {
			jsonObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.UNKNOWN.getResponseCode());
			jsonObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.UNKNOWN.getResponseMessage());
			logger.error("Unexpcted exception in crypto: ", ex);
			return new ResponseEntity<>(jsonObj.toString(),headers,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
