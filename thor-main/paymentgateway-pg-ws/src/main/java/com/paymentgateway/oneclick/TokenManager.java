package com.paymentgateway.oneclick;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Sunil
 *
 */
@Service
public class TokenManager {
	@Autowired
	private TokenDao tokenDao;
	private static Logger logger = LoggerFactory.getLogger(TokenManager.class.getName());  
	//Add new token
	
	public Token addToken(Fields fields) throws SystemException{
		Token token = null;//TokenFactory.instance(fields);
	/*//	String encryptedCard = token.getCardNumber();
		String payID = token.getPayId();
		String emailID = token.getEmail();
		Token tokenFromDB = tokenDao.getCardNumber(encryptedCard, payID, emailID);
		if(tokenFromDB == null ){
			tokenDao.create(token);
			logger.info("New token added successfully");
		}
		else{
			logger.info("Token exist for the same card");
		}*/
		return token;
	}

	// Fetching card for the Express payment page which already stored
	public void fetchCard(Fields fields) throws SystemException{
		// Fetching Save card
	/*	Token token = TokenFactory.instance(fields);

		String payID = token.getPayId();
		String emailID = token.getEmail();
		tokenDao.getAll(payID, emailID);
*/	}

	// Remove Save card for given tokenID
	public void removeSavedCard(Fields fields){
		try {
			Token token = TokenFactory.instanceDelete(fields);
			//logger.info("Removing token with Id = " + token.getId() + "and EmailID = " + token.getEmail());
			tokenDao.delete(token);
		} catch (SystemException exception) {
			//logger.error("Exception", exception);
		}
	}
	//Remove all tokens matching the cardHash
	public int removeTokensForCard(Fields fields){
		//logger.info("Removing all tokens for card");

		return 0;
	}

	public Map<String, Token> getAll(Map<String, String> fields){
		//logger.info("Get all tokens with email = " + fields.get(FieldType.CUST_EMAIL.getName()) + "and PayId =" + fields.get(FieldType.PAY_ID.getName()));
		Map<String, Token> token = tokenDao.getAll(fields.get(FieldType.PAY_ID.getName()), fields.get(FieldType.CUST_EMAIL.getName()));

		return token;
	}

	public Map<String, String> getToken(Fields fields){
		//logger.info("Get Token with ID = " + fields.get(FieldType.TOKEN_ID.getName()));
		
		Map<String, String> requestMap = new HashMap<String, String>();
		try {
			requestMap = tokenDao.getToken(fields.get(FieldType.TOKEN_ID.getName()));
			requestMap = TokenFactory.dcrypt(fields, requestMap);
		} catch (SystemException exception) {
			//logger.error("Exception", exception);
		}
		return requestMap;

	}
	
	public Map<String, String> getTokenMap(String  tokenId){
		//logger.info("Get Token with ID = " + fields.get(FieldType.TOKEN_ID.getName()));
		
		Map<String, String> requestMap = new HashMap<String, String>();
		try {
			requestMap = tokenDao.getToken(tokenId);
			requestMap = TokenFactory.dcryptToken(tokenId, requestMap);
		} catch (SystemException exception) {
			//logger.error("Exception", exception);
		}
		return requestMap;

	}
	public TokenDao getTokenDao() {
		return tokenDao;
	}

	public void setTokenDao(TokenDao tokenDao) {
		this.tokenDao = tokenDao;
	}

	/*public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		TokenManager.logger = logger;
	}*/
}
