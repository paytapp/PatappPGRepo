package com.paymentgateway.oneclick;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TokenStatus;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Sunil,Rahul
 *
 */
@Service
public class TokenManager {

	private static Logger logger = LoggerFactory.getLogger(TokenManager.class.getName());
	public static final String MASK_START_CHARS = "-XXXX-XXXX-";

	@Autowired
	private TokenDao tokenDao;

	@Autowired
	EncryptDecryptService encryptDecryptService;
	
	@Autowired
	private UserSettingDao userSettingDao;


	// Add new token
	public void addToken(Fields fields, User user) throws SystemException {
		BasicDBObject token = new BasicDBObject();
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			token.put("_id", TransactionManager.getNewTransactionId());
			token.put(FieldType.TOKEN_ID.getName(), token.get("_id"));
			token.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
			token.put(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			token.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			token.put(FieldType.STATUS.getName(), TokenStatus.ACTIVE.name().toString());
			token.put("CARD_SAVE_PARAM", fields.get(merchntSettings.getCardSaveParam()));
			token.put(FieldType.CARD_NUMBER.getName(),
					encryptDecryptService.encrypt(
							PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
							fields.get(FieldType.CARD_NUMBER.getName())));
			token.put(FieldType.CARD_EXP_DT.getName(),
					encryptDecryptService.encrypt(
							PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
							fields.get(FieldType.CARD_EXP_DT.getName())));
			token.put(FieldType.CARD_MASK.getName(), maskCardNumber(fields.get(FieldType.CARD_NUMBER.getName())));
			token.put(FieldType.KEY_ID.getName(), fields.get(FieldType.KEY_ID.getName()));
			token.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			token.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
					fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			token.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
					fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			token.put(FieldType.CARD_HOLDER_TYPE.getName(), fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
			token.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			token.put(FieldType.CREATE_DATE.getName(), dateNow);
			token.put(FieldType.UPDATE_DATE.getName(), dateNow);
			// token.put(FieldType.H_CARD_NUMBER.getName(),
			// Hasher.getHash(fields.get(FieldType.CARD_NUMBER.getName())));
			// check existing token

			// TODO check existing stored cards
			boolean tokenFromDB = tokenDao.getCardNumber(token.getString(FieldType.CARD_MASK.getName()),
					token.getString(FieldType.PAY_ID.getName()), token.getString("CARD_SAVE_PARAM"));
			if (tokenFromDB == false) {

				if (StringUtils.isNotBlank(token.getString(FieldType.CARD_NUMBER.getName()))
						&& StringUtils.isNotBlank(token.getString(FieldType.CARD_NUMBER.getName()))) {

					tokenDao.create(token);
					logger.info("New token added successfully");
				} else {
					logger.info("Unable to encrypt and save token ");
				}

			} else {
				logger.info("Token exist for the same card");
			}

		} catch (Exception ex) {
			logger.error("Caugth exception while Saving Card" + ex);
		}
	}

	// Add Netbanking new token
	public void addNBToken(Fields fields, User user) throws SystemException {
		BasicDBObject token = new BasicDBObject();
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			token.put("_id", TransactionManager.getNewTransactionId());
			token.put(FieldType.TOKEN_ID.getName(), token.get("_id"));
			token.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
			token.put(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			token.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			token.put(FieldType.STATUS.getName(), TokenStatus.ACTIVE.name().toString());
			token.put("SAVE_PARAM", fields.get(merchntSettings.getNbSaveParam()));
			/*
			 * token.put(FieldType.CARD_NUMBER.getName(), encryptDecryptService.encrypt(
			 * PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue(
			 * )), fields.get(FieldType.CARD_NUMBER.getName())));
			 * token.put(FieldType.CARD_EXP_DT.getName(), encryptDecryptService.encrypt(
			 * PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue(
			 * )), fields.get(FieldType.CARD_EXP_DT.getName())));
			 * token.put(FieldType.CARD_MASK.getName(),
			 * maskCardNumber(fields.get(FieldType.CARD_NUMBER.getName())));
			 */
			token.put(FieldType.KEY_ID.getName(), fields.get(FieldType.KEY_ID.getName()));
			token.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			/*
			 * token.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
			 * fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			 * token.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
			 * fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			 */
			token.put(FieldType.CARD_HOLDER_TYPE.getName(), fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
			token.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			token.put(FieldType.CREATE_DATE.getName(), dateNow);
			token.put(FieldType.UPDATE_DATE.getName(), dateNow);
			// token.put(FieldType.H_CARD_NUMBER.getName(),
			// Hasher.getHash(fields.get(FieldType.CARD_NUMBER.getName())));
			// check existing token

			// TODO check existing stored cards
			boolean tokenFromDB = tokenDao.getNBBank(token.getString(FieldType.MOP_TYPE.getName()),
					token.getString(FieldType.PAYMENT_TYPE.getName()), token.getString(FieldType.PAY_ID.getName()),
					token.getString("SAVE_PARAM"));
			if (tokenFromDB == false) {

				if (StringUtils.isNotBlank(token.getString(FieldType.MOP_TYPE.getName()))
						&& StringUtils.isNotBlank(token.getString(FieldType.MOP_TYPE.getName()))) {

					tokenDao.createNBToken(token);
					logger.info("New NB token added successfully");
				} else {
					logger.info("Unable to encrypt and save NB token ");
				}

			} else {
				logger.info("Token exist for the same mop type");
			}

		} catch (Exception ex) {
			logger.error("Caugth exception while Saving NB token" + ex);
		}
	}

	// Add Wallet new token
	public void addWalletToken(Fields fields, User user) throws SystemException {
		BasicDBObject token = new BasicDBObject();
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			token.put("_id", TransactionManager.getNewTransactionId());
			token.put(FieldType.TOKEN_ID.getName(), token.get("_id"));
			token.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
			token.put(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			token.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			token.put(FieldType.STATUS.getName(), TokenStatus.ACTIVE.name().toString());
			token.put("SAVE_PARAM", fields.get(merchntSettings.getWlSaveParam()));
			/*
			 * token.put(FieldType.CARD_NUMBER.getName(), encryptDecryptService.encrypt(
			 * PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue(
			 * )), fields.get(FieldType.CARD_NUMBER.getName())));
			 * token.put(FieldType.CARD_EXP_DT.getName(), encryptDecryptService.encrypt(
			 * PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue(
			 * )), fields.get(FieldType.CARD_EXP_DT.getName())));
			 * token.put(FieldType.CARD_MASK.getName(),
			 * maskCardNumber(fields.get(FieldType.CARD_NUMBER.getName())));
			 */
			token.put(FieldType.KEY_ID.getName(), fields.get(FieldType.KEY_ID.getName()));
			token.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			/*
			 * token.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
			 * fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			 * token.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
			 * fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			 */
			token.put(FieldType.CARD_HOLDER_TYPE.getName(), fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
			token.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			token.put(FieldType.CREATE_DATE.getName(), dateNow);
			token.put(FieldType.UPDATE_DATE.getName(), dateNow);
			// token.put(FieldType.H_CARD_NUMBER.getName(),
			// Hasher.getHash(fields.get(FieldType.CARD_NUMBER.getName())));
			// check existing token

			// TODO check existing stored cards
			boolean tokenFromDB = tokenDao.getWallet(token.getString(FieldType.MOP_TYPE.getName()),
					token.getString(FieldType.PAYMENT_TYPE.getName()), token.getString(FieldType.PAY_ID.getName()),
					token.getString("SAVE_PARAM"));
			if (tokenFromDB == false) {

				if (StringUtils.isNotBlank(token.getString(FieldType.MOP_TYPE.getName()))
						&& StringUtils.isNotBlank(token.getString(FieldType.MOP_TYPE.getName()))) {

					tokenDao.createWLToken(token);
					logger.info("New Wallet token added successfully");
				} else {
					logger.info("Unable to encrypt and save wallet token ");
				}

			} else {
				logger.info("Token exist for the same mop type");
			}

		} catch (Exception ex) {
			logger.error("Caugth exception while Saving wallet token" + ex);
		}
	}

	public void addVPAToken(Fields fields, User user) throws SystemException {

		BasicDBObject token = new BasicDBObject();
		try {
			
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());

			token.put("_id", TransactionManager.getNewTransactionId());
			token.put(FieldType.TOKEN_ID.getName(), token.get("_id"));
			token.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
			token.put(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			token.put(FieldType.PAYER_NAME.getName(), fields.get(FieldType.PAYER_NAME.getName()));
			token.put(FieldType.STATUS.getName(), TokenStatus.ACTIVE.name().toString());
			token.put("VPA_SAVE_PARAM", fields.get(merchntSettings.getVpaSaveParam()));
			token.put(FieldType.PAYER_ADDRESS.getName(), toLower(fields.get(FieldType.PAYER_ADDRESS.getName())));
			token.put(FieldType.VPA_MASK.getName(), toLower(maskVpa(fields.get(FieldType.PAYER_ADDRESS.getName()))));
			token.put(FieldType.KEY_ID.getName(), fields.get(FieldType.KEY_ID.getName()));
			token.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			token.put(FieldType.CARD_HOLDER_TYPE.getName(), fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
			token.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));

			// check existing token

			// TODO check existing stored VPA
			boolean tokenFromDB = tokenDao.getVPA(toLower(token.getString(FieldType.PAYER_ADDRESS.getName())),
					token.getString(FieldType.PAY_ID.getName()), token.getString("VPA_SAVE_PARAM"));
			if (tokenFromDB == false) {

				if (StringUtils.isNotBlank(token.getString(FieldType.PAYER_ADDRESS.getName()))
						&& StringUtils.isNotBlank(token.getString(FieldType.PAYER_ADDRESS.getName()))) {

					tokenDao.createVpa(token);
					logger.info("New token added successfully");
				} else {
					logger.info("Unable to encrypt and save token ");
				}

			} else {
				logger.info("Token exist for the same VPA");
			}

		} catch (Exception ex) {
			logger.error("Caugth exception while Saving VPA" + ex);
		}
	}

	public static String toLower(String vpa) {
		StringBuilder output = new StringBuilder(vpa);
		for (int i = 0; i < output.toString().length(); i++) {
			if (Character.isUpperCase(output.charAt(i))) {
				output.setCharAt(i, Character.toLowerCase(output.charAt(i)));
			}
		}
		return output.toString();
	}

	public Map<String, String> decryptToken(Token token) {
		Map<String, String> cardObj = new HashMap<String, String>();
		cardObj.put(FieldType.CARD_NUMBER.getName(),
				encryptDecryptService.decrypt(
						PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
						token.getCardNumber()));
		cardObj.put(FieldType.CARD_EXP_DT.getName(),
				encryptDecryptService.decrypt(
						PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
						token.getExpiryDate()));
		return cardObj;
	}

	public String maskCardNumber(String cardNumber) {
		StringBuilder mask = new StringBuilder();
		mask.append(cardNumber.subSequence(0, 4));
		mask.append(MASK_START_CHARS);
		mask.append(cardNumber.substring(cardNumber.length() - 4));
		return mask.toString();
	}

	public String maskVpa(String vpa) {
		StringBuilder maskVpa = new StringBuilder();
		String[] aa = vpa.split("@");
		String bb = aa[0];
		bb = bb.substring(0, bb.length() - 2);
		maskVpa.append(bb);
		maskVpa.append("**");
		maskVpa.append("@");
		maskVpa.append(aa[1]);

		return maskVpa.toString();
	}

	// Remove Save card for given tokenID
	public void removeSavedCard(Fields fields, User user) {
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			Token token = TokenFactory.instanceDelete(fields, user, merchntSettings);
			tokenDao.delete(token);
		} catch (SystemException exception) {
			// logger.error("Exception", exception);
		}
	}

	// Remove saved VPA for given token
	public void removeSavedVPA(Fields fields, User user) {
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			VpaToken token = TokenFactory.vpaInstanceDelete(fields, user, merchntSettings);
			tokenDao.deleteVpa(token);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
	}

	// Remove saved NB Bank for given token
	public void removeSavedNbBank(Fields fields, User user) {
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			NBToken token = TokenFactory.nbInstanceDelete(fields, user, merchntSettings);
			tokenDao.deleteNbBank(token);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
	}

	// Remove saved Wallet for given token
	public void removeSavedWallet(Fields fields, User user) {
		try {
			UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			WLToken token = TokenFactory.wlInstanceDelete(fields, user, merchntSettings);
			tokenDao.deleteSavedWallet(token);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
	}

	// Remove all tokens matching the cardHash
	public int removeTokensForCard(Fields fields) {
		// logger.info("Removing all tokens for card");

		return 0;
	}

	public Map<String, Token> getAll(Fields fields, User user) {
		
		UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
		Map<String, Token> token = new HashMap<String, Token>();
		if (StringUtils.isNotBlank(fields.get(merchntSettings.getCardSaveParam()))) {
			token = tokenDao.getAll(fields.get(FieldType.PAY_ID.getName()), fields.get(merchntSettings.getCardSaveParam()));
		}
		return token;
	}

	public Map<String, NBToken> getAllBank(Fields fields, User user) {

		UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
		Map<String, NBToken> token = new HashMap<String, NBToken>();
		if (StringUtils.isNotBlank(fields.get(merchntSettings.getNbSaveParam()))) {
			token = tokenDao.getAllNBBank(fields.get(FieldType.PAY_ID.getName()), fields.get(merchntSettings.getNbSaveParam()));
		}
		return token;
	}

	public Map<String, WLToken> getAllWallet(Fields fields, User user) {

		UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
		Map<String, WLToken> token = new HashMap<String, WLToken>();
		if (StringUtils.isNotBlank(fields.get(merchntSettings.getWlSaveParam()))) {
			token = tokenDao.getAllWallet(fields.get(FieldType.PAY_ID.getName()), fields.get(merchntSettings.getWlSaveParam()));
		}
		return token;
	}

	public Map<String, VpaToken> getAllVpa(Fields fields, User user) {

		UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
		Map<String, VpaToken> token = new HashMap<String, VpaToken>();
		if (StringUtils.isNotBlank(fields.get(merchntSettings.getVpaSaveParam()))) {
			token = tokenDao.getAllVpa(fields.get(FieldType.PAY_ID.getName()), fields.get(merchntSettings.getVpaSaveParam()));
		}
		return token;
	}

	public Map<String, String> getToken(Fields fields) {
		// logger.info("Get Token with ID = " +
		// fields.get(FieldType.TOKEN_ID.getName()));

		Map<String, String> requestMap = new HashMap<String, String>();
		try {
			requestMap = tokenDao.getToken(fields.get(FieldType.TOKEN_ID.getName()));
			requestMap = TokenFactory.dcrypt(fields, requestMap);
		} catch (SystemException exception) {
			// logger.error("Exception", exception);
		}
		return requestMap;

	}

	public Map<String, String> getTokenMap(String tokenId) {
		// logger.info("Get Token with ID = " +
		// fields.get(FieldType.TOKEN_ID.getName()));

		Map<String, String> requestMap = new HashMap<String, String>();
		try {
			requestMap = tokenDao.getToken(tokenId);
			requestMap = TokenFactory.dcryptToken(tokenId, requestMap);
		} catch (SystemException exception) {
			// logger.error("Exception", exception);
		}
		return requestMap;
	}

	public TokenDao getTokenDao() {
		return tokenDao;
	}

	public void setTokenDao(TokenDao tokenDao) {
		this.tokenDao = tokenDao;
	}
}
