package com.paymentgateway.pg.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.iciciUpi.Constants;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pg.security.SecurityProcessor;

@RestController
public class PgQRTransact {
	
	@Autowired
	TransactionResponser transactionResponser;

	@Autowired
	private Fields field;

	@Autowired
	private UserDao userDao;
	
	private static Logger logger = LoggerFactory.getLogger(PgQRTransact.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/pgQRProcessor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> pgProcessPayment(@RequestBody Map<String, String> reqmap) {

		try {
			String url = "";
			Fields fields = new Fields(reqmap);
			fields.logAllFields("PG QR Request:");
			fields.clean();
			boolean currency = validateCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
			if(!currency) {
				fields.put(FieldType.STATUS.getName(), "Invalid currency code");
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_CURRENCY_CODE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_CURRENCY_CODE.getResponseCode());
				transactionResponser.addHash(fields);
				return fields.getFields();
			}
			boolean hashResult = validateHash(fields);
			String txnId = TransactionManager.getNewTransactionId();
			String payId = fields.get(FieldType.PAY_ID.getName());
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
			String amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));
			fields.put(FieldType.AMOUNT.getName(), amount);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				field.insertPgQRRequest(fields);
				transactionResponser.addHash(fields);
				return fields.getFields();
			}
			User user = userDao.findPayId(payId);
			if (user.getUserStatus() != UserStatusType.ACTIVE) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				transactionResponser.addHash(fields);
				return fields.getFields();
			}

			boolean duplicateCheck = field.validateDuplicatePgQRRequest(orderId);
			if (duplicateCheck) {
				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());
				transactionResponser.addHash(fields);
				return fields.getFields();
			}

			fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), txnId);
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");

			url = PropertiesManager.propertiesMap.get("PG_QR_URL")+txnId;
			BufferedImage qrImage = generateQRCode(url);
			String base64QR = qrImageToBase64String(qrImage, Constants.QR_FILE_TYPE);
			fields.put(FieldType.PG_QR_CODE.getName(), base64QR);

			fields.put(FieldType.RETURN_URL.getName(), PropertiesManager.propertiesMap.get("invoiceReturnUrl"));
			fields.put(FieldType.MOP_TYPE.getName(), "PG_QR");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
			

			field.insertPgQRRequest(fields);
			fields.removeInternalFields();
			fields.remove(FieldType.RETURN_URL.getName());
			fields.remove(FieldType.MOP_TYPE.getName());
			
			
			transactionResponser.addHash(fields);
			return fields.getFields();

		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			Fields res = new Fields();
			res.put(FieldType.RESPONSE_MESSAGE.getName(), exception.getMessage());
			res.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
			return res.getFields();
		}
	}

	public boolean validateHash(Fields fields) throws SystemException {
		String merchantHash = fields.remove(FieldType.HASH.getName());
		if (StringUtils.isEmpty(merchantHash)) {
			return false;
		}
		String calculateHash = Hasher.getHash(fields);
		if (!calculateHash.equalsIgnoreCase(merchantHash)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(merchantHash);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculateHash);
			logger.error(hashMessage.toString());
			return false;
		}
		return true;

	}
	public boolean validateCurrency(String currency) throws SystemException{
		if(StringUtils.isEmpty(currency)) {
			logger.error("Currency code is empty..");
			return false;
		}
		if(!StringUtils.isNumeric(currency)) {
			logger.error("Currency code is not in numeric...");
			return false;
		}
		return true;
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
}
