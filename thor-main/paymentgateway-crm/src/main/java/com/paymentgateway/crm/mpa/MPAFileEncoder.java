package com.paymentgateway.crm.mpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MPAFileEncoder {

	private static Logger logger = LoggerFactory.getLogger(MPAUploadAction.class.getName());

	public String base64Encoder(File file, String fileContentType) {
		logger.info("Encoding file type: " + fileContentType + " to Base64");
		String base64File = "";
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			byte fileData[] = new byte[(int) file.length()];
			imageInFile.read(fileData);
			base64File = Base64.getEncoder().encodeToString(fileData);
		} catch (FileNotFoundException e) {
			logger.error("Exception caught while encoding into Base64, " , e);
		} catch (IOException e) {
			logger.error("Exception caught while encoding into Base64, " , e);
		} catch (Exception e) {
			logger.error("Exception caught while encoding into Base64, " , e);
		}
		return base64File;
	}

	public static void decoder(String base64Image, String pathFile) {
		logger.info("Decoding base64Image : " + base64Image + " file path: "+pathFile);
		try (FileOutputStream imageOutFile = new FileOutputStream(pathFile)) {
			byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
			imageOutFile.write(imageByteArray);
		} catch (FileNotFoundException e) {
			logger.error("Exception caught while encoding into Base64, " , e);
		} catch (IOException e) {
			logger.error("Exception caught while encoding into Base64, " , e);
		} catch (Exception e) {
			logger.error("Exception caught while encoding into Base64, " , e);
		}
	}
	
	private boolean isMatch(byte[] pattern, byte[] data) {
		if (pattern.length <= data.length) {
			for (int idx = 0; idx < pattern.length; ++idx) {
				if (pattern[idx] != data[idx])
					return false;
			}
			return true;
		}
		return false;
	}

	String getImageType(byte[] data) {

		final byte[] pdfPattern = new byte[] { 0x25, 0x50, 0x44, 0x46 };
		final byte[] pngPattern = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
		final byte[] jpgPattern = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
		final byte[] gifPattern = new byte[] { 0x47, 0x49, 0x46, 0x38 };
		final byte[] bmpPattern = new byte[] { 0x42, 0x4D };
		final byte[] tiffLEPattern = new byte[] { 0x49, 0x49, 0x2A, 0x00 };
		final byte[] tiffBEPattern = new byte[] { 0x4D, 0x4D, 0x00, 0x2A };
		if (isMatch(pngPattern, data))
			return "image/png";

		if (isMatch(jpgPattern, data))
			return "image/jpg";

		if (isMatch(gifPattern, data))
			return "image/gif";

		if (isMatch(bmpPattern, data))
			return "image/bmp";

		if (isMatch(tiffLEPattern, data))
			return "image/tif";

		if (isMatch(tiffBEPattern, data))
			return "image/tif";
		
		if (isMatch(pdfPattern, data))
			return "application/pdf";

		return "image/png";
	}

}
