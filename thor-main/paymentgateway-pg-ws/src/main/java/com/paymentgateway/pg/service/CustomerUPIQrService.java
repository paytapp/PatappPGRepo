package com.paymentgateway.pg.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.cashfree.CashFreeVPARequestHandler;
import com.paymentgateway.cashfree.CashFreeVPAService;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CustomerIdGenerator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;
import com.paymentgateway.commons.util.StatusType;

@Service
public class CustomerUPIQrService {

	@Autowired
	private CashFreeVPAService cashFreeService;

	@Autowired
	private CashFreeVPARequestHandler cashFreeRequestHandler;

	@Autowired
	private CustomerIdGenerator customerIdGenerator;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private QRCodeCreator qRCodeCreator;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	private static Logger logger = LoggerFactory.getLogger(CustomerUPIQrService.class.getName());
	private final String fileupiqr = "/home/Properties/customerQr/upiqr/";

	public Map<String, String> generateCustQR(Fields fields) {

		Map<String, String> custIdMap = new HashMap<String, String>();
		Fields responseFields = new Fields();

		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);

		try {
			String accountId = fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName());
			String customerName = fields.get(FieldType.CUST_NAME.getName());
			String customerPhone = fields.get(FieldType.CUST_PHONE.getName());
			String amount = fields.get(FieldType.AMOUNT.getName());
			String companyName = fields.get(FieldType.COMPANY_NAME.getName());

			if (StringUtils.isNotBlank(companyName)) {
				companyName = companyName.replace(" ", "");
			}

			String payId = fields.get(FieldType.PAY_ID.getName());
			String merchantHash = fields.get(FieldType.HASH.getName());

			fields.remove(FieldType.HASH.getName());

			if (StringUtils.isBlank(payId)) {

				custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INVALID_PAYID_ATTEMPT.getInternalMessage());
				custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				custIdMap.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);

				return custIdMap;
			}
			if (StringUtils.isBlank(accountId)) {

				custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Account No");
				custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				custIdMap.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
				custIdMap.put(FieldType.PAY_ID.getName(), payId);

				responseFields.putAll(custIdMap);
				String respHash = Hasher.getHash(responseFields);
				custIdMap.put(FieldType.HASH.getName(), respHash);

				return custIdMap;

			}
			if (StringUtils.isBlank(merchantHash)) {

				custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
				custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				custIdMap.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
				custIdMap.put(FieldType.PAY_ID.getName(), payId);

				responseFields.putAll(custIdMap);
				String respHash = Hasher.getHash(responseFields);
				custIdMap.put(FieldType.HASH.getName(), respHash);

				return custIdMap;

			}

			String calculatedHash = Hasher.getHash(fields);

			if (!merchantHash.equalsIgnoreCase(calculatedHash)) {

				logger.info("Merchant HASH >>>>>>  " + merchantHash);
				logger.info("Calculated Hash HASH >>>>>>  " + calculatedHash);

				custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getInternalMessage());
				custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				custIdMap.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
				custIdMap.put(FieldType.PAY_ID.getName(), payId);
				responseFields.putAll(custIdMap);
				String respHash = Hasher.getHash(responseFields);
				custIdMap.put(FieldType.HASH.getName(), respHash);

				return custIdMap;

			}

			// Check if account already present

			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.CUST_QR_COLLECTION.getValue()));

			BasicDBObject custIdQuery = new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(), accountId);
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(), payId);

			List<BasicDBObject> findqueryList = new ArrayList<BasicDBObject>();
			findqueryList.add(custIdQuery);
			findqueryList.add(payIdQuery);
			BasicDBObject findquery = new BasicDBObject("$and", findqueryList);

			long count = collection.countDocuments(findquery);

			if (count > 0) {
				logger.info("Customer account already added in DB , accountId = " + accountId);

				MongoCursor<Document> cursor = collection.find(findquery).iterator();
				while (cursor.hasNext()) {
					Document documentObj = cursor.next();

					custIdMap.put(FieldType.PAY_ID.getName(), documentObj.get(FieldType.PAY_ID.getName()).toString());
					custIdMap.put(FieldType.UPI_QR_CODE.getName(),
							documentObj.get(FieldType.UPI_QR_CODE.getName()).toString());
					custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
					custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					custIdMap.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
					custIdMap.put(FieldType.VPA.getName(), documentObj.getString(FieldType.VPA.getName()).toString());
					custIdMap.put(FieldType.VIRTUAL_ACC_NUM.getName(),
							"LTZ" + documentObj.getString(FieldType.CUSTOMER_ID.getName()).toString());
					custIdMap.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), accountId);

					responseFields.putAll(custIdMap);
					String respHash = Hasher.getHash(responseFields);
					custIdMap.put(FieldType.HASH.getName(), respHash);

					return custIdMap;

				}
			}

			// Add new QR in DB for new Account

			String customerId = customerIdGenerator.getNewCustomerId();
			String vpa = "PaymentGateway." + customerId + "@icici";

			StringBuilder sb = new StringBuilder();
			sb.append("upi://pay?pa=");
			sb.append(vpa);
			sb.append("&pn=");

			if (StringUtils.isNotBlank(companyName)) {
				sb.append(companyName);
			} else {
				sb.append("PaymentGateway");
			}

			sb.append("&tr=&am=");

			if (StringUtils.isNotBlank(amount)) {

				String formattedAmount = Amount.toDecimal(amount, "356");
				sb.append(formattedAmount);
			} else {
				sb.append("");
			}
			sb.append("&cu=INR&mc=5411");

			String upiQrString = sb.toString();

			Document doc = new Document();

			BufferedImage upiImage = qRCodeCreator.generateStaticQRCode(upiQrString);
			File upiFile = new File(fileupiqr + "upi" + customerId + ".png");
			ImageIO.write(upiImage, "png", upiFile);
			String upiBase64QrString = base64EncodeDecode.base64Encoder(upiFile);
			upiFile.delete();
			custIdMap.put(FieldType.PAY_ID.getName(), payId);
			custIdMap.put(FieldType.UPI_QR_CODE.getName(), upiBase64QrString);
			custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
			custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());

			// custIdMap.put(FieldType.VPA.getName(), );
			// custIdMap.put(FieldType.VIRTUAL_ACC_NUM.getName(), "LTZ"+customerId);
			// custIdMap.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), accountId);

			responseFields.putAll(custIdMap);
			String respHash = Hasher.getHash(responseFields);
			custIdMap.put(FieldType.HASH.getName(), respHash);

			doc.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), accountId);
			doc.put(FieldType.CUST_NAME.getName(), customerName);
			if (StringUtils.isNotBlank(amount)) {
				doc.put(FieldType.AMOUNT.getName(), Amount.toDecimal(amount, "356"));
			}
			doc.put(FieldType.CUST_PHONE.getName(), customerPhone);
			doc.put(FieldType.COMPANY_NAME.getName(), companyName);
			doc.put("VPA", vpa);
			doc.put(FieldType.CUSTOMER_ID.getName(), customerId);
			doc.put(FieldType.PAY_ID.getName(), payId);
			doc.put(FieldType.UPI_QR_CODE.getName(), upiBase64QrString);
			doc.put(FieldType.STATUS.getName(), "Active");
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);

			collection.insertOne(doc);

			return custIdMap;

		}

		catch (Exception e) {
			logger.error("Exception while generating customer qr code", e);

			custIdMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getInternalMessage());
			custIdMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
			custIdMap.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);

			return custIdMap;

		}

	}

	public Map<String, String> getCashfreeStaticUpiQr(Fields fields) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			// Check Hash
			Boolean isHash = cashFreeService.validateHash(fields);
			if (!isHash) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
				return responseMap;
			}
			// Validate fields
			Map<String, String> validationResponse = cashFreeService.validateGenrateCashFreeQRCode(fields);
			if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
					&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
				responseMap = validationResponse;
				return responseMap;
			}

			int reqid = Integer.parseInt(fields.get(FieldType.SLAB_ID.getName()));
			if (reqid == 1 || reqid == 2) {
				logger.info("generating Token for Case no  " + reqid);
				fields = cashFreeRequestHandler.genrateToken(fields);
				if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("ERROR")) {
					responseMap.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							fields.get(FieldType.RESPONSE_MESSAGE.getName()));
					responseMap.put(FieldType.RESPONSE_CODE.getName(), fields.get(FieldType.RESPONSE_CODE.getName()));
					return responseMap;
				}
			}

			responseMap = cashFreeRequestHandler.genrateCashFreeQRCode(fields);

			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
			return responseMap;
		}

	}

	
	public Map<String, String> getCashfreeDynamicUpiQr(Fields fields) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			// Check Hash
			Boolean isHash = cashFreeService.validateHash(fields);
			if (!isHash) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
				return responseMap;
			}
			// Validate fields
			Map<String, String> validationResponse = cashFreeService.validateGenrateCashFreeQRCode(fields);
			if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
					&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
				responseMap = validationResponse;
				return responseMap;
			}

			int reqid = Integer.parseInt(fields.get(FieldType.SLAB_ID.getName()));
			if (reqid == 1 || reqid == 2) {
				logger.info("generating Token for serial no  " + reqid);
				fields = cashFreeRequestHandler.genrateToken(fields);
				if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("ERROR")) {
					responseMap.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							fields.get(FieldType.RESPONSE_MESSAGE.getName()));
					responseMap.put(FieldType.RESPONSE_CODE.getName(), fields.get(FieldType.RESPONSE_CODE.getName()));
					return responseMap;
				}
			}

			responseMap = cashFreeRequestHandler.genrateCashFreeDynamicQRCode(fields);

			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
			return responseMap;
		}

	}
}
