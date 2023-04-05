package com.paymentgateway.crm.actionBeans;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.MerchantGridViewService;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.OrderIdType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.SaltFileManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Puneet
 *
 */

@Service
public class CreateNewUser {

	private static Logger logger = LoggerFactory.getLogger(CreateNewUser.class.getName());

	private static final int emailExpiredInTime = ConfigurationConstants.EMAIL_EXPIRED_HOUR.getValues();
	private final String fileupiqr = "/home/Properties/customerQr/upiqr/";

	@Autowired
	private Hasher hasher;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MerchantGridViewService merchantGridViewService;

	@Autowired
	@Qualifier("saltFileManager")
	private SaltFileManager saltFileManager;

	@Autowired
	private CheckExistingUser checkExistingUser;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	BitlyUrlShortener bitlyUrlShortener;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private QRCodeCreator qRCodeCreator;
	private static final String prefix = "MONGO_DB_";

	public ResponseObject createUser(User user, UserType userType, String parentPayId, User sessionUser,
			String partnerFlag) throws SystemException {
		logger.info("create user with usertype " + userType + "  and parent pay Id " + parentPayId);
		
		UserSettingData userSettings=new UserSettingData();
		ResponseObject responseObject = new ResponseObject();
		// ResponseObject responseActionObject = new ResponseObject();
		Date date = new Date();
		String salt = SaltFactory.generateRandomSalt();
		String saltEnc = SaltFactory.generateRandomSalt();
		logger.info("Checking user");
		responseObject = checkExistingUser.checkuser(user.getEmailId());
		logger.info("Response code " + responseObject.getResponseCode());
		if (ErrorType.USER_AVAILABLE.getResponseCode().equals(responseObject.getResponseCode())) {
			if (userType.equals(UserType.RESELLER)) {
				user.setResellerId(TransactionManager.getNewTransactionId());
				if (StringUtils.isNotBlank(partnerFlag)) {
					if (partnerFlag.equalsIgnoreCase("on")) {
						user.setPartnerFlag(true);
					}
				}
			}
			if (sessionUser != null && sessionUser.getUserType().equals(UserType.RESELLER)) {
				user.setSuperMerchant(false);
				user.setResellerId(sessionUser.getResellerId());
			}

			if (sessionUser != null && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
					&& sessionUser.isSuperMerchant()) {
				user.setSuperMerchantId(sessionUser.getSuperMerchantId());
				userSettings.setSuperMerchantId(sessionUser.getSuperMerchantId());
				userSettings.setBusinessName(user.getBusinessName());
				userSettings.setSuperMerchantName(sessionUser.getBusinessName());
			} 
			
			user.setUserType(userType);
			user.setUserStatus(UserStatusType.PENDING);
			user.setPayId(getpayId());
			
			userSettings.setPayId(user.getPayId());
			userSettings.setBusinessName(user.getBusinessName());

			// User Pay Id is also the super merchant id
			if (userType.equals(UserType.SUPERMERCHANT)) {
				user.setSuperMerchant(true);
				user.setUserType(UserType.MERCHANT);
				user.setSuperMerchantId(user.getPayId());
				userSettings.setSuperMerchantId(sessionUser.getSuperMerchantId());
				
			}
			user.setAccountValidationKey(TransactionManager.getNewTransactionId());
			user.setEmailValidationFlag(false);
			user.setRegistrationDate(date);
			user.setAllowDuplicateOrderId(OrderIdType.NEVER);
			user.setPaymentMessageSlab("0");
			userSettings.setPaymentMessageSlab("0");
			
			// This condition is created for subuser
			if (null != user.getPassword()) {
				user.setPassword(hasher.getHash(user.getPassword().concat(salt)));
			}
			if (null != user.getPin()) {
				user.setPin(hasher.getHash(user.getPin().concat(salt)));
				user.setDefaultLanguage("en");
				// This condition is created for subuser
				user.setParentPayId(parentPayId);
				if (sessionUser != null && (sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.ADMIN))) {
					if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						user.setMerchantCreatedBy(sessionUser.getPayId());
						user.setMerchantCreatorName(sessionUser.getFirstName() + " " + sessionUser.getLastName());
					}
				}
				setExpiryTime(user);
				userDao.create(user);
				if(userType.equals(UserType.MERCHANT) || userType.equals(UserType.SUBMERCHANT) || userType.equals(UserType.SUPERMERCHANT) || userType.equals(UserType.PARENTMERCHANT))
					userSettingDao.saveOrUpdate(userSettings);

				if (!userType.equals(UserType.SUPERMERCHANT) && !userType.equals(UserType.RESELLER))
					generatePgQrCode(user);

				if (userType.equals(UserType.SUPERMERCHANT) || userType.equals(UserType.MERCHANT))
					merchantGridViewService.addUserInMap(user);
				// Insert salt in salt.properties
				boolean isSaltInserted = saltFileManager.insertSalt(user.getPayId(), salt);

				// Insert Salt for Enc
				boolean isSaltEncInserted = saltFileManager.insertSaltEnc(user.getPayId(), saltEnc);

				if (!isSaltInserted) {
					// Rollback user creation
					userDao.delete(user);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				}

				if (!isSaltEncInserted) {
					// Rollback user creation if Enc Salt is used
					userDao.delete(user);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				}

				// Changes done by Shaiwal
				// After Merchant or Sub - Merchant or Super Merchant User is created, update
				// the merchantVPA in customerQR collection in
				// Mongo DB for enabling merchant VPA validation API

				if (user.getUserType().equals(UserType.MERCHANT)) {
					udpateCustomerQRCollection(user);
				}

				responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
				responseObject.setAccountValidationID(user.getAccountValidationKey());
				responseObject.setEmail(user.getEmailId());
			} else {
				responseObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
				responseObject.setResponseMessage(ErrorType.USER_UNAVAILABLE.getResponseMessage());
			}
		}
		return responseObject;
	}

	public ResponseObject createSubMerchant(User user, UserType userType, String parentPayId, User sessionUser,
			String superMerchantId) throws SystemException {
		logger.info("create user with usertype " + userType + "  and parent pay Id " + parentPayId);
		ResponseObject responseObject = new ResponseObject();
		User superMerchantUser = userDao.findBySuperMerchantId(superMerchantId);
		if (sessionUser.isSuperMerchant() && sessionUser.getUserStatus().equals(UserStatusType.ACTIVE)) {
			superMerchantId = sessionUser.getSuperMerchantId();
		} else {
			if (superMerchantUser != null && superMerchantUser.isSuperMerchant()
					&& superMerchantUser.getUserStatus().equals(UserStatusType.ACTIVE)) {
				superMerchantId = superMerchantUser.getSuperMerchantId();
			} else {
				responseObject.setResponseCode(ErrorType.SUPER_MERCHANT_UNAVAILABLE.getResponseCode());
				responseObject.setResponseMessage(ErrorType.SUPER_MERCHANT_UNAVAILABLE.getResponseMessage());
				return responseObject;
			}
		}

		Date date = new Date();
		String salt = SaltFactory.generateRandomSalt();
		String saltEnc = SaltFactory.generateRandomSalt();
		logger.info("Checking user");
		responseObject = checkExistingUser.checkuser(user.getEmailId());
		logger.info("Response code " + responseObject.getResponseCode());
		if (ErrorType.USER_AVAILABLE.getResponseCode().equals(responseObject.getResponseCode())) {

			if (sessionUser != null && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
					&& sessionUser.isSuperMerchant()) {
				user.setSuperMerchantId(sessionUser.getSuperMerchantId());
			} else {
				user.setSuperMerchantId(superMerchantId);
			}
			
//			
				user.setUserStatus(UserStatusType.ACTIVE);
//			


			if (superMerchantUser.isVendorPayOutFlag()) {
				user.setVendorPayOutFlag(true);
			}
//			if (superMerchantUser.isRetailMerchantFlag()) {
//				user.setRetailMerchantFlag(true);
//			}
			if (StringUtils.isNotBlank(superMerchantUser.getResellerId())) {
				user.setResellerId(superMerchantUser.getResellerId());
			}

			user.setSuperMerchant(false);
			user.setUserType(UserType.MERCHANT);
			user.setPayId(getpayId());
			user.setAccountValidationKey(TransactionManager.getNewTransactionId());
			user.setEmailValidationFlag(false);
//			user.setExpressPayFlag(false);
			user.setRegistrationDate(date);
//			user.setSkipOrderIdForRefund(false);
			user.setAllowDuplicateOrderId(OrderIdType.NEVER);
			user.setPaymentMessageSlab("0");
			// This condition is created for subuser
			if (null != user.getPassword()) {
				user.setPassword(hasher.getHash(user.getPassword().concat(salt)));
			}
			if (null != user.getPin()) {
				user.setPin(hasher.getHash(user.getPin().concat(salt)));
				user.setDefaultLanguage("en");
				// This condition is created for subuser
				user.setParentPayId(parentPayId);
				if (sessionUser != null && (sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.ADMIN))) {
					if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						user.setMerchantCreatedBy(sessionUser.getPayId());
						user.setMerchantCreatorName(sessionUser.getFirstName() + " " + sessionUser.getLastName());
					}
				}
				setExpiryTime(user);
				userDao.create(user);
				generatePgQrCode(user);

				// Insert salt in salt.properties
				boolean isSaltInserted = saltFileManager.insertSalt(user.getPayId(), salt);

				// Insert Salt for Enc
				boolean isSaltEncInserted = saltFileManager.insertSaltEnc(user.getPayId(), saltEnc);

				if (!isSaltInserted) {
					// Rollback user creation
					userDao.delete(user);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				}

				if (!isSaltEncInserted) {
					// Rollback user creation if Enc Salt is used
					userDao.delete(user);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				}

				// Changes done by Shaiwal
				// After Merchant or Sub - Merchant or Super Merchant User is created, update
				// the merchantVPA in customerQR collection in
				// Mongo DB for enabling merchant VPA validation API

				if (user.getUserType().equals(UserType.MERCHANT)) {
					udpateCustomerQRCollection(user);
				}

				responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
				responseObject.setAccountValidationID(user.getAccountValidationKey());
				responseObject.setEmail(user.getEmailId());
			} else {
				responseObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
				responseObject.setResponseMessage(ErrorType.USER_UNAVAILABLE.getResponseMessage());
			}
		}
		return responseObject;
	}

	public User setExpiryTime(User user) {
		Date currnetDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(currnetDate);
		c.add(Calendar.HOUR, emailExpiredInTime);
		currnetDate = c.getTime();
		user.setEmailExpiryTime(currnetDate);
		return user;
	}

	private String getpayId() {
		return TransactionManager.getNewTransactionId();
	}

	public void generatePgQrCode(User user) {
		String filepgqr = "/home/Properties/staticQr/pgqr/";
		try {
			Files.createDirectories(Paths.get(filepgqr));
		} catch (IOException e1) {
			logger.error("Error in creating Directorie " , e1);
		}
		String fileupiqr = "/home/Properties/staticQr/upiqr/";
		try {
			Files.createDirectories(Paths.get(fileupiqr));
		} catch (IOException e1) {
			logger.error("Error in creating Directorie " , e1);
		}
		String merchantPayId = user.getPayId();
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();
			String pgUrl = propertiesManager.propertiesMap.get(CrmFieldConstants.STATIC_PGQR_URL.getValue())
					+ merchantPayId;
			String pgShortUrl = bitlyUrlShortener.createShortUrlUsingBitly(pgUrl);
			BufferedImage pgImage = qRCodeCreator.generateStaticQRCode(pgShortUrl);
			File pgFile = new File(filepgqr + merchantPayId + ".png");
			ImageIO.write(pgImage, "png", pgFile);
			String pgBase64QrString = base64EncodeDecode.base64Encoder(pgFile);

			StringBuilder upiUrl = new StringBuilder();
			upiUrl.append("upi://pay?pa=");
			upiUrl.append(user.getMerchantVPA());
			upiUrl.append("&pn=");
			upiUrl.append(user.getBusinessName());
			upiUrl.append("&tr=&am=&cu=INR&mc=5411");

			BufferedImage upiImage = qRCodeCreator.generateStaticQRCode(upiUrl.toString());
			File upiFile = new File(fileupiqr + "upi" + merchantPayId + ".png");
			ImageIO.write(upiImage, "png", upiFile);
			String upiBase64QrString = base64EncodeDecode.base64Encoder(upiFile);

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.QR_COLLECTION.getValue()));

			newFieldsObj.put(FieldType.PAY_ID.getName(), merchantPayId);
			newFieldsObj.put(FieldType.PG_QR_CODE.getName(), pgBase64QrString);
			newFieldsObj.put(FieldType.UPI_QR_CODE.getName(), upiBase64QrString);
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);

			File filepgqrdelete = new File(filepgqr);
			File fileupiqrdelete = new File(fileupiqr);
			Arrays.stream(filepgqrdelete.listFiles((f, p) -> p.endsWith(".png"))).forEach(File::delete);
			Arrays.stream(fileupiqrdelete.listFiles((f, p) -> p.endsWith(".png"))).forEach(File::delete);

		} catch (Exception exception) {
			logger.error("Error Cought in generatePgQrCode() while generating QR", exception);
		}
	}

	public void udpateCustomerQRCollection(User user) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CUST_QR_COLLECTION.getValue()));

			String customerId = user.getMerchantVPA().replace("PaymnetGateway.", "");
			customerId = customerId.replace("@icici", "");

			String vpa = user.getMerchantVPA();

			StringBuilder sb = new StringBuilder();
			sb.append("upi://pay?pa=");
			sb.append(vpa);
			sb.append("&pn=");

			if (StringUtils.isNotBlank(user.getBusinessName())) {
				sb.append(user.getBusinessName());
			} else {
				sb.append("PaymnetGateway");
			}

			sb.append("&tr=&am=");
			sb.append("");
			sb.append("&cu=INR&mc=5411");

			String upiQrString = sb.toString();

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			Document doc = new Document();

			BufferedImage upiImage = qRCodeCreator.generateStaticQRCode(upiQrString);
			try {
				Files.createDirectories(Paths.get(fileupiqr));
			} catch (IOException e1) {
				logger.error("Error in creating Directorie " , e1);
			}
			File upiFile = new File(fileupiqr + "upi" + customerId + ".png");
			ImageIO.write(upiImage, "png", upiFile);
			String upiBase64QrString = base64EncodeDecode.base64Encoder(upiFile);
			upiFile.delete();

			doc.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), user.getVirtualAccountNo());
			doc.put(FieldType.CUST_NAME.getName(), user.getBusinessName());
			doc.put(FieldType.CUST_PHONE.getName(), user.getMobile());
			doc.put(FieldType.COMPANY_NAME.getName(), user.getBusinessName());
			doc.put("VPA", vpa);
			doc.put(FieldType.CUSTOMER_ID.getName(), customerId);
			doc.put(FieldType.PAY_ID.getName(), user.getPayId());
			doc.put(FieldType.UPI_QR_CODE.getName(), upiBase64QrString);
			doc.put(FieldType.STATUS.getName(), "Active");
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);

			collection.insertOne(doc);

		}

		catch (Exception e) {
			logger.error("Exception in updating customer QR collcetion for User  ", e);
		}

	}
}
