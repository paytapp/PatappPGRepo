package com.paymentgateway.pg.core.util;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Rahul
 *
 */
@Service
public class MerchantHostedUtils {
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	public String encryptMerchantResponse(Fields fields) throws SystemException {
		StringBuilder allFields = new StringBuilder();
		for(Entry<String, String> entry : fields.getFields().entrySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(entry.getKey());
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(entry.getValue());
		}
		allFields.deleteCharAt(0);
		String encryptedString = transactionControllerServiceProvider.encrypt(fields.get(FieldType.PAY_ID.getName()), allFields.toString()).get(FieldType.ENCDATA.getName());
		
		return encryptedString;
		
	}
	
	public String hostedEncryptMerchantResponse(Fields fields) throws SystemException {
		StringBuilder allFields = new StringBuilder();
		for(Entry<String, String> entry : fields.getFields().entrySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(entry.getKey());
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(entry.getValue());
		}
		allFields.deleteCharAt(0);
		String encryptedString = transactionControllerServiceProvider.hostedEncrypt(fields.get(FieldType.PAY_ID.getName()), allFields.toString()).get(FieldType.ENCDATA.getName());
		
		return encryptedString;
		
	}
	
	public String hostedEncryptMerchantResponseS2S(Fields fields) throws SystemException {
		StringBuilder allFields = new StringBuilder();
		allFields.append("{");
		for(Entry<String, String> entry : fields.getFields().entrySet()) {
			allFields.append("\"");
			allFields.append(entry.getKey());
			allFields.append("\"");
			allFields.append(":");
			allFields.append("\"");
			allFields.append(entry.getValue());
			allFields.append("\"");
			allFields.append(",");
		}
		allFields.deleteCharAt(allFields.length()-1);
		allFields.append("}");
		String encryptedString = transactionControllerServiceProvider.hostedEncrypt(fields.get(FieldType.PAY_ID.getName()), allFields.toString()).get(FieldType.ENCDATA.getName());
		
		return encryptedString;
		
	}


}
