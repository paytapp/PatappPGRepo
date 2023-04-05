package com.paymentgateway.yesbankcb;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.CalculateSurchargeAmount;

@Service
public class YesbankUpiQrIntegrator {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private CalculateSurchargeAmount calculateSurchargeAmount;

	private static final String prefix = "MONGO_DB_";
	private static final Logger logger = LoggerFactory.getLogger(YesbankUpiQrIntegrator.class.getName());

	public void process(Fields fields) throws SystemException {
		try {
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));

			if (fields.contains(FieldType.EPOS_MERCHANT.getName())
					&& fields.contains(FieldType.SURCHARGE_FLAG.getName())) {

				if (fields.get(FieldType.EPOS_MERCHANT.getName()).equals("true")
						&& fields.get(FieldType.SURCHARGE_FLAG.getName()).equalsIgnoreCase("Y")) {

					BigDecimal[] surUPAmount = calculateSurchargeAmount.fetchUPSurchargeDetails(
							Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName())),
							fields.get(FieldType.PAY_ID.getName()), AccountCurrencyRegion.DOMESTIC,
							fields.get(FieldType.SLAB_ID.getName()), fields.get(FieldType.RESELLER_ID.getName()));

					BigDecimal surchargeUPAmount = surUPAmount[1];

					fields.put(FieldType.TOTAL_AMOUNT.getName(), surchargeUPAmount.toString());
				}

			}

			logger.info("Verifying existing OID for UPI QR");

//			if (isExistingRequest(fields)) {
//				fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
//				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
//				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
//				// TODO handle total amount in surcharge
//				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
//				return;
//			}

			logger.info("Generating UPI QR code for PAY_ID: " + fields.get(FieldType.PAY_ID.getName())
					+ " with ORDER_ID: " + fields.get(FieldType.ORDER_ID.getName()));
			String intentCallString = generateIntentCallString(fields);
			BufferedImage qrImage = generateQRCode(intentCallString);
			String base64QR = qrImageToBase64String(qrImage, Constants.QR_FILE_TYPE);
			fields.put(FieldType.UPI_QR_CODE.getName(), base64QR);
//			fields.put(FieldType.TXN_ID.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
	}

	private boolean isExistingRequest(Fields fields) {

		List<BasicDBObject> qrObjList = new ArrayList<BasicDBObject>();
		qrObjList.add(new BasicDBObject(FieldType.OID.getName(), fields.get(FieldType.PG_REF_NUM.getName())));
		qrObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
//		qrObjList.add(new BasicDBObject(FieldType.OID.getName(), fields.get(FieldType.OID.getName())));
//		qrObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));

		BasicDBObject qrQuery = new BasicDBObject("$and", qrObjList);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
				.get(prefix + com.paymentgateway.commons.util.Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(qrQuery).iterator();
		if (cursor.hasNext()) {
			return true;
		}
		return false;
	}

	public static String qrImageToBase64String(final RenderedImage img, final String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, formatName, os);
			return Base64.getEncoder().encodeToString(os.toByteArray());
		} catch (final IOException ioe) {
			logger.error("Exception caught, " , ioe);
			throw new UncheckedIOException(ioe);
		}
	}

	private BufferedImage generateQRCode(String content) {
		BufferedImage image = createQRImage(content, Constants.QR_SIZE, Constants.QR_FILE_TYPE);
		return image;
	}

	private BufferedImage createQRImage(String content, int qrSize, String qrFileType) {
		try {
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hintMap);
			int matrixWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			graphics.setColor(Color.BLACK);
			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			return image;
		} catch (Exception e) {
			logger.error("Exception caught while generating QR image, " , e);
		}
		return null;
	}

	private String generateIntentCallString(Fields fields) throws SystemException {
		logger.info("Generating Intent Call String for Yes UPI QR Code");
		try {
			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			StringBuilder intentCallString = new StringBuilder();
			intentCallString.append(Constants.UPI);
			intentCallString.append(Constants.COLON);
			intentCallString.append(Constants.SLASH_MARK);
			intentCallString.append(Constants.SLASH_MARK);
			intentCallString.append(Constants.PAY);
			intentCallString.append(Constants.QUESTION_MARK);
			intentCallString.append(Constants.PA);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append("paymentgateway@yesb");
			intentCallString.append(Constants.AMPERSAND);
			intentCallString.append(Constants.PN);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append("Rahul");
			intentCallString.append(Constants.AMPERSAND);
			intentCallString.append(Constants.TR);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append(fields.get(FieldType.PG_REF_NUM.getName()));
			intentCallString.append(Constants.AMPERSAND);
			intentCallString.append(Constants.TN);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append("PAY%20TO%20FSS");
			intentCallString.append(Constants.AMPERSAND);
			intentCallString.append(Constants.AM);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append(amount);
			intentCallString.append(Constants.AMPERSAND);
			intentCallString.append(Constants.CU);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append(Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName())));
			intentCallString.append(Constants.AMPERSAND);
			intentCallString.append(Constants.MAM);
			intentCallString.append(Constants.EQUATOR);
			intentCallString.append("10");
			logger.info("Intent call string generated for ICICI UPI QR is " + intentCallString.toString());
			return intentCallString.toString();
		} catch (Exception e) {
			logger.error("Exception, " , e);
		}
		return null;
	}

}
