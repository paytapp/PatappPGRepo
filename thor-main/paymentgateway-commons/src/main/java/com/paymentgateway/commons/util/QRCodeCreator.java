package com.paymentgateway.commons.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.paymentgateway.commons.user.Invoice;

@Service
public class QRCodeCreator {

	public BufferedImage generateQRCode(Invoice invoice) throws WriterException, IOException {

		String qrCodeText = invoice.getShortUrl();

		int size = 200;

		String fileType = "png";

		BufferedImage image = createQRImage(qrCodeText, size, fileType);

		return image;

	}

	public BufferedImage createQRImage(String qrCodeText, int size, String fileType)
			throws WriterException, IOException {

		// Create the ByteMatrix for the QR-Code that encodes the given String

		Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();

		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

		QRCodeWriter qrCodeWriter = new QRCodeWriter();

		BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText,

				BarcodeFormat.QR_CODE, size, size, hintMap);

		// Make the BufferedImage that are to hold the QRCode

		int matrixWidth = byteMatrix.getWidth();

		BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,

				BufferedImage.TYPE_INT_RGB);

		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();

		graphics.setColor(Color.WHITE);

		graphics.fillRect(0, 0, matrixWidth, matrixWidth);

		// Paint and save the image using the ByteMatrix

		graphics.setColor(Color.BLACK);

		for (int i = 0; i < matrixWidth; i++) {

			for (int j = 0; j < matrixWidth; j++) {

				if (byteMatrix.get(i, j)) {

					graphics.fillRect(i, j, 1, 1);

				}

			}

		}
		return image;

	}
	public BufferedImage generateStaticQRCode(String shortUrl) throws WriterException, IOException {

		int size = 200;

		String fileType = "png";

		BufferedImage image = createQRImage(shortUrl, size, fileType);

		return image;

	}
}
