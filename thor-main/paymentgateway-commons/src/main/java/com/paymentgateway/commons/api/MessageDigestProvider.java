package com.paymentgateway.commons.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.Stack;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;
/**
 * @author Surender
 *
 */
public class MessageDigestProvider {
	private static Stack<MessageDigest> stack = new Stack<MessageDigest>();

	public static MessageDigest provide() throws SystemException{
		MessageDigest digest = null;
		try{
			digest = stack.pop();
		} catch (EmptyStackException emptyStackException){
			try {
				digest = MessageDigest.getInstance(ConfigurationConstants.HASHING_ALGORITHAM.getValue());
			} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, noSuchAlgorithmException, "Hashing algoritham not found");
			}
		}
		
		return digest;
	}
	
	public static void consume(MessageDigest digest){
		stack.push(digest);
	}
}
