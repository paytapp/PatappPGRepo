package com.paymentgateway.oneclick;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.CryptoManager;
import com.paymentgateway.pg.core.util.CryptoManagerFactory;

/**
 * @author Sunil,Rahul
 *
 */
@Service
public class TokenFactory {

	
	private static CryptoManager cryptoManager = CryptoManagerFactory.getCryptoManager();
	
	public static Token instanceDelete(Fields fields , User user, UserSettingData merchntSettings) throws SystemException{
		Token token = new Token();
		
		token.setCardSaveParam(fields.get(merchntSettings.getCardSaveParam()));
		token.setPayId(fields.get(FieldType.PAY_ID.getName()));
		token.setId(fields.get(FieldType.TOKEN_ID.getName()));
		
		return token;
	}
	
	public static VpaToken vpaInstanceDelete(Fields fields , User user, UserSettingData merchntSettings) throws SystemException{
		VpaToken token = new VpaToken();
		
		token.setVpaSaveParam(fields.get(merchntSettings.getVpaSaveParam()));
		token.setPayId(fields.get(FieldType.PAY_ID.getName()));
		token.setId(fields.get(FieldType.TOKEN_ID.getName()));
		
		return token;
	}
	
	public static NBToken nbInstanceDelete(Fields fields , User user, UserSettingData merchntSettings) throws SystemException{
		NBToken token = new NBToken();
		
		token.setSaveParam(fields.get(merchntSettings.getNbSaveParam()));
		token.setPayId(fields.get(FieldType.PAY_ID.getName()));
		token.setId(fields.get(FieldType.TOKEN_ID.getName()));
		
		return token;
	}
	
	public static WLToken wlInstanceDelete(Fields fields , User user, UserSettingData merchntSettings) throws SystemException{
		WLToken token = new WLToken();
		
		token.setSaveParam(fields.get(merchntSettings.getWlSaveParam()));
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
