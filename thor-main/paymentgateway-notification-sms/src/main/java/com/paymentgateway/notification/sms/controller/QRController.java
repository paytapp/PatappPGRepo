package com.paymentgateway.notification.sms.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;

@RestController
public class QRController {

	private static Logger logger = LoggerFactory.getLogger(QRController.class.getName());

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;
	@Autowired
	private UserDao userDao;
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private QRCodeCreator qRCodeCreator;
	private static final String prefix = "MONGO_DB_";

	@RequestMapping(method = RequestMethod.POST, value = "/updateQR")
	public @ResponseBody String updateQR(@RequestBody String reqmap) {

		try {
			List<Merchants> merchantList = userDao.getAllMerchants();
			List<Merchants> submerchantPayidList = userDao.getAllSubMerchants();
			for (Merchants merchant : merchantList) {
				if (checkIfQrGenerated(merchant))
					generatePgQrCode(merchant);
			}

			for (Merchants submerchant : submerchantPayidList) {
				if (checkIfQrGenerated(submerchant))
					generatePgQrCode(submerchant);
			}

			return "Updated Successfully ";
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return "Exception Cought";
		}

	}

	public void generatePgQrCode(Merchants merchant) throws Exception {

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

		String merchantPayId = merchant.getPayId();

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
		upiUrl.append(merchant.getMerchantVPA());
		upiUrl.append("&pn=");
		upiUrl.append(merchant.getBusinessName());
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

	}

	public boolean checkIfQrGenerated(Merchants merchant) throws Exception {
		String fileupiqr = "/home/Properties/staticQr/upiqr/";
		try {
			Files.createDirectories(Paths.get(fileupiqr));
		} catch (IOException e1) {
			logger.error("Error in creating Directorie " , e1);
		}
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.QR_COLLECTION.getValue()));
		MongoCursor<Document> cursor = collection
				.find(new BasicDBObject(FieldType.PAY_ID.getName(), merchant.getPayId())).iterator();
		while (cursor.hasNext()) {
			Document oldDoc = cursor.next();

			BasicDBObject newFieldsObj = new BasicDBObject();
			newFieldsObj.put(FieldType.UPI_QR_CODE.getName(), updateUpiQr(merchant, fileupiqr));
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			Document newDoc = new Document(updateObj);
			collection.updateOne(oldDoc, newDoc);
			File fileupiqrdelete = new File(fileupiqr);
			Arrays.stream(fileupiqrdelete.listFiles((f, p) -> p.endsWith(".png"))).forEach(File::delete);

			return false;
		}
		return true;
	}

	public String updateUpiQr(Merchants merchant, String fileupiqr) throws Exception {
		StringBuilder upiUrl = new StringBuilder();
		upiUrl.append("upi://pay?pa=");
		upiUrl.append(merchant.getMerchantVPA());
		upiUrl.append("&pn=");
		upiUrl.append(merchant.getBusinessName());
		upiUrl.append("&tr=&am=&cu=INR&mc=5411");
		BufferedImage upiImage = qRCodeCreator.generateStaticQRCode(upiUrl.toString());
		File upiFile = new File(fileupiqr + "upi" + merchant.getPayId() + ".png");
		ImageIO.write(upiImage, "png", upiFile);

		return base64EncodeDecode.base64Encoder(upiFile);
	}
}
