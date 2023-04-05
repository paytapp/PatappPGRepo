package com.paymentgateway.notification.sms.smsCreater;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;

@Service
public class UpdateMerchantVPA {

	private static Logger logger = LoggerFactory.getLogger(UpdateMerchantVPA.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private QRCodeCreator qRCodeCreator;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	private static final String prefix = "MONGO_DB_";
	private final String fileupiqr = "/home/Properties/customerQr/upiqr/";

	public String updatePreviousVpa() {

		try {

			logger.info("Fetching list of all users with usertype as merchant");
			List<Merchants> merchantsList = new ArrayList<Merchants>();
			merchantsList = userDao.getMerchantList();

			logger.info("Total users found == " + merchantsList.size());

			for (Merchants merchant : merchantsList) {

				logger.info("Current User being updated, Pay Id  == " + merchant.getPayId() + " and business Name == "
						+ merchant.getBusinessName());

				User user = userDao.findPayId(merchant.getPayId());

				Long randomNumer = getRandomNumber();
				String randomNumberString = "9" + randomNumer;
				randomNumberString = randomNumberString.substring(0, 8);

				String customerId = randomNumberString;
				String customerVpa = "PaymentGateWay." + randomNumberString + "@icici";

				user.setMerchantVPA("PaymentGateWay." + randomNumberString + "@icici");

				udpateCustomerQRCollection(merchant, customerId, user.getBusinessName(), customerVpa);

				logger.info("Updating User table with new merchant VPA for merchant  >> " + user.getBusinessName());
				userDao.update(user);
				
				logger.info("Updating Static PG QR Code and STATIC UPI QR code in QR Collection for merchant >> " + user.getBusinessName());
				generatePgQrCode(user);
			
			}

		}

		catch (Exception e) {
			logger.error("Exception in updating VPA for Merchants ", e);
			return "ERROR";
		}

		return "SUCCESS";

	}

	private Long getRandomNumber() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("LETZ" + randomNum);
			logger.info(
					"virtual Account is " + "LETZ" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}

	public void udpateCustomerQRCollection(Merchants merchant, String customerId, String businessName,
			String merchantVpa) {

		try {

			logger.info("Updating customerQR collection for merchant  >> " + merchant.getBusinessName());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CUST_QR_COLLECTION.getValue()));

			String vpa = merchantVpa;

			StringBuilder sb = new StringBuilder();
			sb.append("upi://pay?pa=");
			sb.append(vpa);
			sb.append("&pn=");

			if (StringUtils.isNotBlank(businessName)) {
				sb.append(businessName);
			} else {
				sb.append("PaymentGateWay");
			}

			sb.append("&tr=&am=");
			sb.append("");
			sb.append("&cu=INR&mc=5411");

			String upiQrString = sb.toString();

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			Document doc = new Document();

			BufferedImage upiImage = qRCodeCreator.generateStaticQRCode(upiQrString);
			File upiFile = new File(fileupiqr + "upi" + customerId + ".png");
			ImageIO.write(upiImage, "png", upiFile);
			String upiBase64QrString = base64EncodeDecode.base64Encoder(upiFile);
			upiFile.delete();

			doc.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), merchant.getPayId());
			doc.put(FieldType.COMPANY_NAME.getName(), businessName);
			doc.put("VPA", vpa);
			doc.put(FieldType.CUSTOMER_ID.getName(), customerId);
			doc.put(FieldType.PAY_ID.getName(), merchant.getPayId());
			doc.put(FieldType.UPI_QR_CODE.getName(), upiBase64QrString);
			doc.put(FieldType.STATUS.getName(), "Active");
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);

			collection.insertOne(doc);

		}

		catch (Exception e) {
			logger.error("Exception in updating merchant ", e);
		}
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
			String pgShortUrl = pgUrl;
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
	
}
