package com.paymentgateway.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.commons.user.UserSettingData;

@Service
public class UserSettingUtils {

	private static final Logger logger = LoggerFactory.getLogger(UserSettingUtils.class);

	@SuppressWarnings("unchecked")
	public Document objectToDocument(UserSettingData userSetting) {

		Document document = new Document();

		try {

			ObjectMapper objMapper = new ObjectMapper();
			Map<String, Object> objMap = objMapper.convertValue(userSetting, Map.class);

			for (Map.Entry<String, Object> entry : objMap.entrySet()) {
				document.put(entry.getKey(), entry.getValue());
			}

		} catch (Exception e) {
			logger.info("exception while converting objectToDocument, ", e);
		}

		return document;

	}

	@SuppressWarnings("unchecked")
	public UserSettingData documentToObject(Document doc) {

		UserSettingData userSettingData = new UserSettingData();

		try {
			doc.remove("_id");
			ObjectMapper objMapper = new ObjectMapper();
			userSettingData = objMapper.convertValue(doc, UserSettingData.class);

		} catch (Exception e) {
			logger.info("exception while converting documentToObject, ", e);
		}

		return userSettingData;

	}

	public void saveMerchantLogo(UserSettingData userSettingData) {
		String srcfileName = userSettingData.getPayId() + ".png";
		try {
			if (userSettingData.getLogoImageFile() != null && userSettingData.getPayId() != null) {
				File destFile = new File(
						PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//"
								+ userSettingData.getPayId(),
						srcfileName);
				FileUtils.copyFile(userSettingData.getLogoImageFile(), destFile);
				userSettingData.setMerchantLogo(getBase64LogoPerMerchant(userSettingData));
			}
		} catch (IOException e) {
			logger.error("Exception cought Wile saving logoImage File : ", e);
		}
	}

	public String getBase64LogoPerMerchant(UserSettingData userSettingData) {
		String base64File = "";
		File file = null;
		if (userSettingData.isLogoFlag()) {
			file = new File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//"
					+ userSettingData.getPayId(), userSettingData.getPayId() + ".png");

			if (file.exists()) {
				try (FileInputStream imageInFile = new FileInputStream(file)) {
					byte fileData[] = new byte[(int) file.length()];
					imageInFile.read(fileData);
					base64File = Base64.getEncoder().encodeToString(fileData);
				} catch (FileNotFoundException e) {
					logger.error("Exception caught while encoding into Base64, ", e);
					return "";
				} catch (IOException e) {
					logger.error("Exception caught while encoding into Base64, ", e);
					return "";
				} catch (Exception e) {
					logger.error("Exception caught while encoding into Base64, ", e);
					return "";
				}
			}
		}
		return base64File;
	}

}
