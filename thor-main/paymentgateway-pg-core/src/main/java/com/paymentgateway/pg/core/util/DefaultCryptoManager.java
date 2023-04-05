package com.paymentgateway.pg.core.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudRuleModel;

@Service
public class DefaultCryptoManager implements CryptoManager {
	public static final String MASK_START_CHARS = "-XXXX-XXXX-";
	
	@Autowired
	EncryptDecryptService encryptDecryptService;

	public DefaultCryptoManager() {
	}

	public void secure(Fields fields) throws SystemException {
		try {
			hashCardDetails(fields);
		} finally {
			removeSecureFields(fields);
		}
	}

	public void hashCardDetails(Fields fields) throws SystemException {
		String cardNumber = fields.get(FieldType.CARD_NUMBER.getName());
		if (null != cardNumber) {
			fields.put(FieldType.H_CARD_NUMBER.getName(),
					Hasher.getHash(cardNumber));
		}
	}

	public void removeSecureFields(Fields fields) {
		// Remove CVV, if present - Do not ever store CVV
		fields.remove(FieldType.CVV.getName());
		fields.remove(FieldType.CARD_EXP_DT.getName());
		CryptoUtil.truncateCardNumber(fields);
	}

	public void encryptCardDetails(Fields fields) {
		// Encrypt Card number
		String cardNumber = fields.get(FieldType.CARD_NUMBER.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		if (null != cardNumber) {
			cardNumber = encryptDecryptService.encrypt(payId,cardNumber);
			fields.put(FieldType.S_CARD_NUMBER.getName(), cardNumber);
		}

		// Encrypt Expiry date
		String expiryDate = fields.get(FieldType.CARD_EXP_DT.getName());
		if (null != expiryDate) {
			expiryDate = encryptDecryptService.encrypt(payId,expiryDate);
			fields.put(FieldType.S_CARD_EXP_DT.getName(), expiryDate);
		}
	}
	
	public String maskCardNumber(String cardNumber){
		StringBuilder mask = new StringBuilder();
		mask.append(cardNumber.subSequence(0, 4));
		mask.append(MASK_START_CHARS);
		mask.append(cardNumber.substring(cardNumber.length() - 4));
		
		return mask.toString();
	}
	
	public String hashCardNumber(String cardNumber) throws SystemException {
		String cardHash = null;
		if (null != cardNumber) {
			cardHash =	Hasher.getHash(cardNumber);
		}
		return cardHash;
	}
	
	public String hashVpa(String vpaName) throws SystemException {
		String cardHash = null;
		if (null != vpaName) {
			cardHash =	Hasher.getHash(vpaName);
		}
		return cardHash;
	}
	
	public String encryptVpa(String payId, String vpa) {
		// Encrypt vpa number
		String encrytedVpa=null;
		
		if (StringUtils.isNotBlank(payId) && StringUtils.isNotBlank(vpa)) {
			encrytedVpa = encryptDecryptService.encrypt(payId,vpa);
		}
		
		return encrytedVpa;
	}
	
	public String decryptVpa(String payId, String EncryptedVpa) {
		// decrypt vpa number
		String decryptedVpa=null;
		
		if (StringUtils.isNotBlank(payId) && StringUtils.isNotBlank(EncryptedVpa)) {
			decryptedVpa = encryptDecryptService.decrypt(payId,EncryptedVpa);
		}
		
		return decryptedVpa;
	}
	
}
