package com.paymentgateway.commons.api;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Component
public class Hasher {

	public Hasher() {
	}

	private static Logger logger = LoggerFactory.getLogger(Hasher.class.getName());

	public static String getHash(String input) throws SystemException {
		String response = null;

		MessageDigest messageDigest = MessageDigestProvider.provide();
		messageDigest.update(input.getBytes());
		MessageDigestProvider.consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response.toUpperCase();
	}// getSHA256Hex()

	public static String getHash(Fields fields) throws SystemException {

		// Append salt of merchant
		String salt = null;
		salt = PropertiesManager.saltStore.get(fields.get(FieldType.PAY_ID.getName()));
		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(fields.get(FieldType.PAY_ID.getName()));
			if (salt != null) {
				logger.info("Salt found from propertiesManager for payId = " + fields.get(FieldType.PAY_ID.getName()));
			}

		} else {
			logger.info("Salt found from static map in propertiesManager for payId = "
					+ fields.get(FieldType.PAY_ID.getName()));
		}

		if (null == salt) {
			logger.info("Inside Hasher , salt = null , fields = " + fields.getFieldsAsBlobString());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
			throw new SystemException(ErrorType.INVALID_PAYID_ATTEMPT, "Invalid " + FieldType.PAY_ID.getName());
		}
		// Sort the request map
		Fields internalFields = fields.removeInternalFields();
		Map<String, String> treeMap = new TreeMap<String, String>(fields.getFields());
		fields.put(internalFields);

		// Calculate the hash string
		StringBuilder allFields = new StringBuilder();
		for (String key : treeMap.keySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(key);
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(fields.get(key));
		}

		allFields.deleteCharAt(0); // Remove first FIELD_SEPARATOR
		allFields.append(salt);

		// Calculate hash at server side
		return getHash(allFields.toString());
	}

	public static String getHashWithSalt(Fields fields, String salt) throws SystemException {
		Fields internalFields = fields.removeInternalFields();
		Map<String, String> treeMap = new TreeMap<String, String>(fields.getFields());
		fields.put(internalFields);

		// Calculate the hash string
		StringBuilder allFields = new StringBuilder();
		for (String key : treeMap.keySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(key);
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(fields.get(key));
		}
		allFields.deleteCharAt(0);
		allFields.append(salt);

		// Calculate hash at server side
		return getHash(allFields.toString());
	}
}
