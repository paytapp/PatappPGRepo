package com.paymentgateway.oneclick;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TokenStatus;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.core.util.CryptoManager;
import com.paymentgateway.pg.core.util.CryptoManagerFactory;

/**
 * @author Sunil
 *
 */

public class TokenFactory {
	
	@Autowired
	private static UserDao userDao;
	
	private static CryptoManager cryptoManager = CryptoManagerFactory.getCryptoManager();
//	private static final KeyProvider keyProvider = KeyProviderFactory.getKeyProvider(); TODO

	public static Token instanceDelete(Fields fields) throws SystemException{
		Token token = new Token();
		
		token.setCardSaveParam(fields.get(userDao.findPayId(fields.get(FieldType.PAY_ID.getName())).getCardSaveParam()));
		token.setPayId(fields.get(FieldType.PAY_ID.getName()));
		token.setId(fields.get(FieldType.TOKEN_ID.getName()));
		
		return token;
	}
	
	public static Map<String, String> dcrypt(Fields fields, Map<String, String> requestMap) throws SystemException{
		
		//String key = keyProvider.getKey(fields); TODO
		//Scrambler scrambler = new Scrambler(key);
		//String pan = scrambler.decrypt(requestMap.get(FieldType.CARD_NUMBER.getName()));
		//String expDate = scrambler.decrypt(requestMap.get(FieldType.CARD_EXP_DT.getName()));
		//requestMap.put(FieldType.CARD_NUMBER.getName(),pan);
		//requestMap.put(FieldType.CARD_EXP_DT.getName(),expDate);
		requestMap.put(FieldType.MOP_TYPE.getName(),requestMap.get(FieldType.MOP_TYPE.getName()));
		requestMap.put(FieldType.PAYMENT_TYPE.getName(),requestMap.get(FieldType.PAYMENT_TYPE.getName()));
		return requestMap;
	}
	
	
public static Map<String, String> dcryptToken(String tokenId, Map<String, String> requestMap) throws SystemException{
		
		//String key = keyProvider.getKey(fields); TODO
		//Scrambler scrambler = new Scrambler(key);
		//String pan = scrambler.decrypt(requestMap.get(FieldType.CARD_NUMBER.getName()));
		//String expDate = scrambler.decrypt(requestMap.get(FieldType.CARD_EXP_DT.getName()));
		//requestMap.put(FieldType.CARD_NUMBER.getName(),pan);
		//requestMap.put(FieldType.CARD_EXP_DT.getName(),expDate);
		requestMap.put(FieldType.MOP_TYPE.getName(),requestMap.get(FieldType.MOP_TYPE.getName()));
		requestMap.put(FieldType.PAYMENT_TYPE.getName(),requestMap.get(FieldType.PAYMENT_TYPE.getName()));
		return requestMap;
	}
	public static CryptoManager getCryptoManager() {
		return cryptoManager;
	}

	public static void setCryptoManager(CryptoManager cryptoManager_) {
		cryptoManager = cryptoManager_;
	}
}
