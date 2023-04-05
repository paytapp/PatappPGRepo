package com.paymentgateway.pg.core.pageintegrator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldFormatType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessorValidatorFactory;

@Service
public class GeneralValidator implements Validator {

	@Autowired
	private ProcessorValidatorFactory processorValidatorFactory;

	@Autowired
	private Hasher hasher;

	private static Logger logger = LoggerFactory.getLogger(GeneralValidator.class.getName());
	public static final String panRegex = "[A-Z]{5}[0-9]{4}[A-Z]{1}";
	public static final String emailRegex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)";
	public static final String upiAddressRegex = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}";
	public static final Pattern numberPattern = Pattern.compile(".*\\D.*");
	public static final Pattern alphaNumPattern = Pattern.compile("^[[:alnum:]]*$");
	public static final String alphaWithWhiteSpace = "([a-zA-Z]+\\s+)*[a-zA-Z]+";
	public static final String urlRegex = "^(https?|http?|www.?)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	public static final String DATE_FORMAT = "yyyyMMdd";
	public static final int MAX_YEAR = 100;
	public static final int MAX_MONTH = 12;
	public static final int MIN_MONTH = 1;
	public final char REPLACEMENT_CHAR = ' ';
	public final char NUMBER_REPLACEMENT_CHAR = '0';
	public static final Map<String, FieldType> fieldTypeMap = FieldType.getFieldsMap();
	public static final Map<String, FieldType> mandatorySupportFields = FieldType.getMandatorSupportFields();
	public static final Map<String, FieldType> mandatoryRequestFields = FieldType.getMandatoryRequestFields();
	public static final Map<String, FieldType> mandatoryStatusRequestFields = FieldType.getMandatoryStatusRequestFields();
	public static final Map<String, FieldType> mandatoryRecoRequestFields = FieldType.getMandatoryRecoRequestFields();

	public GeneralValidator() {
	}

	public void validate(Fields fields) throws SystemException {
		// from field definitions to request fields
		backwardValidations(fields);

		// from request fields to field definitions
		forwardValidations(fields);

		customValidations(fields);

		// processorValidations(fields);
	}

	public void processorValidations(Fields fields) throws SystemException {

		// Do not do processor specific validations for new order transactions
		if (fields.get(FieldType.TXNTYPE.getName()).equals( // TODO... discuss
															// about the need
															// for acquirer
															// specific
															// validations for
															// refund and
															// capture
				TransactionType.NEWORDER.getName())
				|| fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())
				|| fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.CAPTURE.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.RECO.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.REFUNDRECO.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.STATUS.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.VERIFY.getName())){
			return;
		}
		// fields.put(FieldType.ACQUIRER_TYPE.getName(),
		// AcquirerType.getDefault(fields).getCode());
		Validator processorValidator = processorValidatorFactory.getInstance(fields);
		processorValidator.validate(fields);
	}

	public void backwardValidations(Fields fields) throws SystemException {
		String origTxnId = fields.get(FieldType.ORIG_TXN_ID.getName());
		if (null == origTxnId) {
			String txntype = fields.get(FieldType.TXNTYPE.getName());
			if (txntype.equals(TransactionType.STATUS.getName())) {
				// validate non support transactions
				validateMandatoryFields(fields, mandatoryStatusRequestFields);
				
			} else if (txntype.equals(TransactionType.RECO.getName()) || txntype.equals(TransactionType.REFUNDRECO.getName())){
				
				validateMandatoryFields(fields, mandatoryRecoRequestFields);
			}
			else {
				validateMandatoryFields(fields, mandatoryRequestFields);
			}
		} else {
			// Validate support transactions
			validateMandatoryFields(fields, mandatorySupportFields);
		}
	}

	public void validateMandatoryFields(Fields fields, Map<String, FieldType> fieldTypes) throws SystemException {
		for (FieldType fieldType : fieldTypes.values()) {
			String key = fieldType.getName();

			
			// Terminal Id and Service Id is sent only by pos merchant
			if (key.equalsIgnoreCase(FieldType.TERMINAL_ID.getName()) ||
					key.equalsIgnoreCase(FieldType.SERVICE_ID.getName()) ||
					key.equalsIgnoreCase(FieldType.SERVICE_CHARGE.getName())) {
				
				// Do not validate these fields
			}
			
			else {
				if (StringUtils.isBlank(fields.get(key))) {
					throw new SystemException(ErrorType.VALIDATION_FAILED, fieldType.getName() + " is a required field");
				}
				validateField(fieldTypeMap.get(key), key, fields);
			}
			
		}
	}

	public void customValidations(Fields fields) throws SystemException {

		validateTransactionType(fields);

		validateMopType(fields);

		validateCardNumber(fields);

		validateExpiryDate(fields);

		validateHash(fields);

		validateCurrency(fields);
	}

	public void validateCurrency(Fields fields) throws SystemException {
		Currency.validateCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
	}

	public void validateExpiryDate(Fields fields) throws SystemException {
		String requestedDate = fields.get(FieldType.CARD_EXP_DT.getName());
		if (null == requestedDate) {
			return;
		}

		final Date date = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
		String today = simpleDateFormat.format(date);
		int thisYear = Integer.parseInt(today.substring(0, 4));
		int thisMonth = Integer.parseInt(today.substring(4, 6));
		int requestedYear = Integer.parseInt(requestedDate.substring(2, 6));
		int requestedMonth = Integer.parseInt(requestedDate.substring(0, 2));

		// if requested year is less than present year
		if (requestedYear < thisYear) {
			throw new SystemException(ErrorType.VALIDATION_FAILED, FieldType.CARD_EXP_DT.getName() + " = '"
					+ fields.get(FieldType.CARD_EXP_DT.getName()) + ", is not a valid value");
		} else if (requestedYear == thisYear && requestedMonth < thisMonth) {
			// if requested year is present year, but requested month is expired
			throw new SystemException(ErrorType.VALIDATION_FAILED, FieldType.CARD_EXP_DT.getName() + " = '"
					+ fields.get(FieldType.CARD_EXP_DT.getName()) + ", is not a valid value");
		} else if (requestedYear > (thisYear + MAX_YEAR)) {
			// if requested year is too far in future
			throw new SystemException(ErrorType.VALIDATION_FAILED, FieldType.CARD_EXP_DT.getName() + " = '"
					+ fields.get(FieldType.CARD_EXP_DT.getName()) + ", is not a valid value");
		} else if (requestedMonth < MIN_MONTH || requestedMonth > MAX_MONTH) {
			// If month range is invalid
			throw new SystemException(ErrorType.VALIDATION_FAILED, FieldType.CARD_EXP_DT.getName() + " = '"
					+ fields.get(FieldType.CARD_EXP_DT.getName()) + ", is not a valid value");
		}
	}

	public void validateCardNumber(Fields fields) throws SystemException {

		/*
		 * String cardNumber = fields.get(FieldType.CARD_NUMBER.getName()); if
		 * (null == cardNumber) { return; }
		 * 
		 * boolean isValid = false;
		 * 
		 * int s1 = 0, s2 = 0; String reverse = new
		 * StringBuffer(cardNumber).reverse().toString(); for (int i = 0; i <
		 * reverse.length(); i++) { int digit =
		 * Character.digit(reverse.charAt(i), 10); if (i % 2 == 0) {// this is
		 * for odd digits, they are 1-indexed in // the algorithm s1 += digit; }
		 * else {// add 2 * digit for 0-4, add 2 * digit - 9 for 5-9 s2 += 2 *
		 * digit; if (digit >= 5) { s2 -= 9; } } }
		 * 
		 * isValid = (s1 + s2) % 10 == 0;
		 * 
		 * if (!isValid) { throw new
		 * SystemException(ErrorType.VALIDATION_FAILED,
		 * FieldType.CARD_NUMBER.getName() + " = '" +
		 * fields.get(FieldType.CARD_NUMBER.getName()) +
		 * ", is not a valid value"); }
		 */
	}

	public void validateMopType(Fields fields) throws SystemException {

		if (null == fields.get(FieldType.MOP_TYPE.getName())) {
			return;
		}

		MopType[] mopTypes = MopType.values();
		boolean flag = true;
		for (MopType mopType : mopTypes) {
			if (fields.get(FieldType.MOP_TYPE.getName()).equals(mopType.getCode())) {
				flag = false;
				break;
			}
		}
		if (flag) {
			throw new SystemException(ErrorType.VALIDATION_FAILED, FieldType.MOP_TYPE.getName() + " = '"
					+ fields.get(FieldType.MOP_TYPE.getName()) + ", is not a valid value");
		}
	}

	public void validateTransactionType(Fields fields) throws SystemException {
		String requestTransactionType = fields.get(FieldType.TXNTYPE.getName());
		if (null == requestTransactionType) {
			return;
		}

		TransactionType[] transactionTypes = TransactionType.values();
		boolean flag = true;
		for (TransactionType transactionType : transactionTypes) {
			if (requestTransactionType.equals(transactionType.getName())) {
				flag = false;
				break;
			}
		}

		if (flag) {
			throw new SystemException(ErrorType.VALIDATION_FAILED, FieldType.TXNTYPE.getName() + " = '"
					+ fields.get(FieldType.TXNTYPE.getName()) + ", is not a valid value");
		}
	}

	public void forwardValidations(Fields fields) throws SystemException {

			Iterator<Map.Entry<String, String>> iterator =  fields.getFields().entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String,String> entry = iterator.next();
				validateField(fieldTypeMap.get(entry.getKey()), entry.getKey(), fields);
			}
			
		}
	

	public void validateHash(Fields fields) throws SystemException {

		// Do not do hash validation, it has already been done
		String validateFlag = fields.get(FieldType.INTERNAL_VALIDATE_HASH_YN.getName());
		if (null != validateFlag && validateFlag.equals("N")) {
			return;
		}

		// Hash sent by merchant in request
		String merchantHash = fields.remove(FieldType.HASH.getName());
		if (StringUtils.isEmpty(merchantHash)) {
			handleInvalidHash(fields);
		}

		String calculatedHash = hasher.getHash(fields);
		if (!calculatedHash.equals(merchantHash)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(merchantHash);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculatedHash);
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			logger.error(hashMessage.toString());
			handleInvalidHash(fields);
		}
	}

	public void handleInvalidHash(Fields fields) throws SystemException {
		if (ConfigurationConstants.IS_DEBUG.getValue().equals(Constants.TRUE.getValue())
				&& ConfigurationConstants.ALLOW_FAILED_HASH.getValue().equals(Constants.TRUE.getValue())) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			logger.warn("Hash failed, continuing in debug mode!");
			return;
		}

		fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "Y");

		throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + FieldType.HASH.getName());
	}

	public boolean isValidEmailId(String email) {
		if (email.matches(emailRegex)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isValidPan(String pan) {
		if (pan.matches(panRegex)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isValidUpiAddress(String upiAddress) {
		if (upiAddress.matches(upiAddressRegex)) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void validateField(FieldType fieldType, String key, Fields fields) throws SystemException {

		String valueKey = fields.get(key);

		StringBuilder value = new StringBuilder(valueKey);

		if (null == fieldType) {
			// Check if validations are set for the requested field
			throw new SystemException(ErrorType.VALIDATION_FAILED, "No such field defined, field = " + key);
		} 

		// Validate type
		switch (fieldType.getType()) {
		case ALPHA:
			validateAlpha(value, fieldType, fields);
			break;

		case NUMBER:
			validateNumber(value, fieldType, fields);
			break;
			
		case PERIODNUM:
			validatePeriodNum(value, fieldType, fields);
			break;

		case ALPHANUM:
			validateAlphaNum(value, fieldType, fields);
			break;

		case SPECIAL:
			validateSpecialChar(value, fieldType, fields);
			break;

		case BOBSPECIAL:
			validateBobSpecialChar(value, fieldType, fields);
			break;	
		case DIRECPAYSPECIAL:
			validateDirecpaySpecialChar(value, fieldType, fields);
			break;
		case ISGPAYSPECIAL:
			validateIsgPaySpecialChar(value, fieldType, fields);
			break;
		case LYRASPECIAL:
			validateIsgPaySpecialChar(value, fieldType, fields);
			break;
		case GOOGLEPAYSPECIAL:
			validateGooglePaySpecialChar(value, fieldType, fields);
			break;
		case URL:
			// TODO: Put validations for urls
			break;
		case MOPTYPE:
			validateMopTypes(value, fieldType, fields);
			break;
		case NONE:
			// Ignore
			break;
		case AMOUNT:
			validateAmount(value, fieldType, fields);
			break;
		case EMAIL:
			if (!isValidEmailId(value.toString())) {
				if (fieldType.isSpecialCharReplacementAllowed()) {
					fields.remove(fieldType.getName());
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + key);
				}
			}
			break;
		case UPIADDRESS:
			if (!isValidUpiAddress(value.toString())) {
				if (fieldType.isSpecialCharReplacementAllowed()) {
					fields.remove(fieldType.getName());
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + key);
				}
			}
			break;	
		}

		if (value.length() < fieldType.getMinLength()) {

			// Tolerate if field optional
			if (!fieldType.isRequired()) {
				return;
			}

			if (fieldType.isSpecialCharReplacementAllowed()) {
				appendDefaultValue(value, fieldType);
			} else {
				throw new SystemException(ErrorType.VALIDATION_FAILED,
						"Minimum length of '" + key + "' is " + fieldType.getMinLength());
			}
		} else if (value.length() > fieldType.getMaxLength()) {

			if (fieldType.isSpecialCharReplacementAllowed()) {
				value.setLength(fieldType.getMaxLength());
				fields.put(fieldType.getName(), value.toString());
			} else {
				throw new SystemException(ErrorType.VALIDATION_FAILED,
						"Maximum length of '" + key + "' is " + fieldType.getMaxLength());
			}
		}
	}
	
	public void validatePeriodNum(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final char[] permittedSpecialChars = { '.' };

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				continue;
			} else {
				boolean foundFlag = false;
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}
				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}
			}
		}
		fields.put(fieldType.getName(), value.toString());
	}

	public void appendDefaultValue(StringBuilder value, FieldType fieldType) {
		int lengthDiff = fieldType.getMinLength() - value.length();
		FieldFormatType fieldFormatType = fieldType.getType();
		switch (fieldFormatType) {
		case NUMBER:
			append(value, NUMBER_REPLACEMENT_CHAR, lengthDiff);
			break;
		default:
			append(value, REPLACEMENT_CHAR, lengthDiff);
		}
	}

	public void append(StringBuilder value, char inputChar, int length) {
		for (int index = 0; index < length; ++index) {
			value.append(inputChar);
		}
	}

	public void validateAlpha(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + fieldType.getName());
				}
			}
		}

		fields.put(fieldType.getName(), value.toString());
	}

	public void validateAlphaNum(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';
		final int BLANK_SPACE = 32;

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else if (ascii == BLANK_SPACE) {
				// allow small char alphabets
				continue;
			}
			else {
				if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + fieldType.getName());
				}
			}
		}

		fields.put(fieldType.getName(), value.toString());
	}
	
	public void validateMopTypes(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';
		final int BLANK_SPACE = 32;
		final int UNDER_SCORE = 95;

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			}else if (ascii == UNDER_SCORE) {
				// allow underscore symbol
				continue;
			} else if (ascii == BLANK_SPACE) {
				// allow small char alphabets
				continue;
			}
			else {
				if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + fieldType.getName());
				}
			}
		}

		fields.put(fieldType.getName(), value.toString());
	}


	public void validateNumber(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				continue;
			} else {
				if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + fieldType.getName());
				}
			}
		}

		fields.put(fieldType.getName(), value.toString());
	}// validateNumber()

	public void validateAmount(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {
		if (StringUtils.isBlank(request.toString().replaceAll("0", ""))) {
			throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + fieldType.getName());
		}
		validateNumber(request, fieldType, fields);

	}

	public void validateSpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';
		final char[] permittedSpecialChars = { ' ', '@', ',', '-', '_', '+', '/', '=', '*', '.', ':', '\n', '\r', '?', '&', '|', '{', '}', '!', ';', '(', ')', '$', '#' };

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}// validateSpecialChar()

	
	
	public void validateBobSpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';
		final char[] permittedSpecialChars = { ' ', '@', ',', '-', '_', '+', '/', '=', '*', '.', ':', '\n', '\r', '?', '<', '>', '&', '!','~','|'};

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}// validateSpecialChar()
	
	public void validatePaytmSpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = '0';
		final int NUMBER_END = '9';
		final int CAPITAL_ALPHA_START = 'A';
		final int CAPITAL_ALPHA_END = 'Z';
		final int ALPHA_START = 'a';
		final int ALPHA_END = 'z';
		final char[] permittedSpecialChars = {'[',']',' ','~','$','{','}','&',';','@', ',', '-', '_', '+', '/', '=', '*', '.', ':','|', '\n', '\r', '?','\'','#'};

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}
	
	public void validateIsgPaySpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';
		final char[] permittedSpecialChars = { ' ', '@', ',', '-', '_', '+', '/', '=', '*', '.', ':', '\n', '\r', '?', '<', '>', '&', '|'};

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}// validateSpecialChar()
	
	public void validateDirecpaySpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = '0';
		final int NUMBER_END = '9';
		final int CAPITAL_ALPHA_START = 'A';
		final int CAPITAL_ALPHA_END = 'Z';
		final int ALPHA_START = 'a';
		final int ALPHA_END = 'z';
		final char[] permittedSpecialChars = {' ','~','$','{','}','(',')','&',';','@', ',', '-', '_', '+', '/', '=', '*', '.', ':','|', '\n', '\r', '?','\''};

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; 	++index) {
			char ch = request.charAt(index);																																														
			int ascii = ch;	

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}
	
	public void validateLyraSpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final int CAPITAL_ALPHA_START = (int) 'A';
		final int CAPITAL_ALPHA_END = (int) 'Z';
		final int ALPHA_START = (int) 'a';
		final int ALPHA_END = (int) 'z';
		final char[] permittedSpecialChars = { ' ', '@', ',', '-', '_', '+', '/', '=', '*', '.', ':', '\n', '\r', '?', '<', '>', '&', '|' , '{', '[', ']'};

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else if (ascii >= CAPITAL_ALPHA_START && ascii <= CAPITAL_ALPHA_END) {
				// allow capital alphabets
				continue;
			} else if (ascii >= ALPHA_START && ascii <= ALPHA_END) {
				// allow small char alphabets
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}// validateSpecialChar()
	
	public void validateGooglePaySpecialChar(StringBuilder request, FieldType fieldType, Fields fields) throws SystemException {

		final int NUMBER_START = (int) '0';
		final int NUMBER_END = (int) '9';
		final char[] permittedSpecialChars = { '+'};

		StringBuilder value = new StringBuilder(request);
		int length = request.length();
		for (int index = 0; index < length; ++index) {
			char ch = request.charAt(index);
			int ascii = (int) ch;

			if (ascii >= NUMBER_START && ascii <= NUMBER_END) {
				// allow numbers
				continue;
			} else {
				boolean foundFlag = false;
				// allow permitted special chars
				for (char specialChar : permittedSpecialChars) {
					if (specialChar == ch) {
						foundFlag = true;
						break;
					}
				}

				if (foundFlag) {
					continue;
				} else if (fieldType.isSpecialCharReplacementAllowed()) {
					// If special char replacement is allowed
					value.setCharAt(index, REPLACEMENT_CHAR);
					break;
				} else {
					throw new SystemException(ErrorType.VALIDATION_FAILED,
							"Invalid " + fieldType.getName() + ", Invalid char found = '" + ch + "'");
				}

			}
		}

		fields.put(fieldType.getName(), value.toString());
	}// validateSpecialChar()
	
	public void validateReturnUrl(Fields fields) throws SystemException {
		if (!isValidUrl(fields.get(FieldType.RETURN_URL.getName()))) {
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_RETURN_URL.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_RETURN_URL.getResponseMessage());
			fields.setValid(false);
			throw new SystemException(ErrorType.INVALID_RETURN_URL, "Invalid return url");
		}
	}

	public boolean isValidUrl(String url) {
		if (StringUtils.isEmpty(url) || url.trim().equals("") || !url.matches(urlRegex)) {
			return false;
		} else {
			return true;
		}
	}

	public void validatePaymentType(Fields fields) throws SystemException {
		String paymentTypeCode = fields.get(FieldType.PAYMENT_TYPE.getName());
		String mopTypeCode = fields.get(FieldType.MOP_TYPE.getName());
		PaymentType paymentType = PaymentType.getInstanceUsingCode(paymentTypeCode);
		MopType mopType = MopType.getmop(mopTypeCode);
		if (null == paymentType) {
			fields.setValid(false);
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + FieldType.PAYMENT_TYPE.getName());
		} else if (null == mopType) {
			fields.setValid(false);
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + FieldType.MOP_TYPE.getName());
		}
	}

	public static String getEmailregex() {
		return emailRegex;
	}

	public static String getPanregex() {
		return panRegex;
	}

	public static Pattern getNumberpattern() {
		return numberPattern;
	}

	public static Pattern getAlphanumpattern() {
		return alphaNumPattern;
	}

	public static String getAlphawithwhitespace() {
		return alphaWithWhiteSpace;
	}
}
