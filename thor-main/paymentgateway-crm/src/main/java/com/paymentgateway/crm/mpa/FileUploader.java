package com.paymentgateway.crm.mpa;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.MimeTypeFactory;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class FileUploader {

	@Autowired
	private MPAServicesFactory servicesFactory;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private MPAResponseCreatorUI responseCreatorUI;

	@Autowired
	MPAFileEncoder encoder;
	
	@Autowired
	PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(MPAUploadAction.class.getName());

	public Map<String, String> uploadFile(File uploadFile, String fileName, String fileType, User user, String payId, boolean setupFlag) {
		Map<String, String> uploadDataMap = new HashMap<String, String>();
		boolean fileNameExist=false;
		
		//Case For MPA filling from MPA panel.
		if(StringUtils.isBlank(payId)){
			payId=user.getPayId();
		}
		
		MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
		String fileTypeArr[] = fileType.split(",");
		logger.info("Uploading " + fileName + " of type " + fileTypeArr[fileTypeArr.length - 1].trim() + " by "
				+ user.getUserType() + " with PayId " + payId);
		String srcfileName = fileName + "_" + payId + "."
				+ MimeTypeFactory.getDefaultExt(fileTypeArr[fileTypeArr.length - 1].trim());
		try {
			
			String fileLocation = PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "//"
					+ payId + "//" + Constants.MPA_FILE_FOLDER_NAME;
			
			File directoryPath = new File(fileLocation);
	        //List of all files and directories
	        File filesArray[] = directoryPath.listFiles();
	        if(filesArray != null) {
		        for(File file : filesArray) {
		        	String name = file.getName();
		        	if(name.contains(fileName)) {
		        		file.delete();
		        		break;
		        	}
		        }
	        }
			
			if(!setupFlag) {
				
				File destFile = new File(
						PropertiesManager.propertiesMap.get(Constants.MPA_FILE_UPLOAD_LOCATION) + "//" + payId + "//" + Constants.MPA_FILE_FOLDER_NAME,
						srcfileName);
				FileUtils.copyFile(uploadFile, destFile);
				uploadFile.delete();
				uploadDataMap.put("filePath", destFile.getAbsolutePath());
				
				
				if (StringUtils.isEmpty(mpaData.getMpaFiles())) {
					mpaData.setMpaFiles(srcfileName);
				} else {
					String[] files=mpaData.getMpaFiles().split(",");
					StringBuilder filestr = new StringBuilder();
					for(int i= 0; i<files.length; i++){
						if(files[i].contains(fileName)){
							fileNameExist=true;
						}else {
							filestr.append(files[i]);
							filestr.append(",");
						}
					}
					
					if(!fileNameExist){
						mpaData.setMpaFiles(mpaData.getMpaFiles() + "," + srcfileName);
					}else {
						if(StringUtils.isBlank(filestr.toString())) {
							mpaData.setMpaFiles(srcfileName);
						}else {
							mpaData.setMpaFiles(filestr.toString() + srcfileName);
						}
					}
				}
				mpaDao.update(mpaData);
			}else {
				
				File destFile = new File(
						PropertiesManager.propertiesMap.get(Constants.MPA_FILE_UPLOAD_LOCATION) + "//" + payId + "//" + Constants.MPA_FILE_FOLDER_NAME,
						srcfileName);
				
				FileUtils.copyFile(uploadFile, destFile);
				uploadFile.delete();
				uploadDataMap.put("filePath", destFile.getAbsolutePath());
			}
			
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
		return uploadDataMap;
	}

	public Map<String, String> uploadCheckerFile(File uploadFile, String fileName, String fileType, User user,
			String payId) {
		Map<String, String> uploadDataMap = new HashMap<String, String>();
		MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
		String fileTypeArr[] = fileType.split(",");
		logger.info("Uploading " + fileName + " of type " + fileTypeArr[fileTypeArr.length - 1].trim() + " by "
				+ user.getUserType() + " with PayId " + user.getPayId());
		String srcfileName = fileName + "_" + user.getPayId() + "."
				+ MimeTypeFactory.getDefaultExt(fileTypeArr[fileTypeArr.length - 1].trim());
		try {
			File destFile = new File(propertiesManager.propertiesMap.get(Constants.MPA_FILE_UPLOAD_LOCATION) + "//"
					+ payId + "//" + Constants.MPA_CHECKER_FILE_UPLOAD_LOCATION, srcfileName);
			FileUtils.copyFile(uploadFile, destFile);
			uploadFile.delete();
			uploadDataMap.put("filePath", destFile.getAbsolutePath());
			if (StringUtils.isEmpty(mpaData.getCheckerFileName())) {
				mpaData.setCheckerFileName(srcfileName);
			} else {
				mpaData.setCheckerFileName(mpaData.getCheckerFileName() + "," + srcfileName);
			}
			mpaDao.update(mpaData);
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
		return uploadDataMap;
	}
	
	public Map<String, String> uploadImageFile(User user, String payId, File filePath, String fileContentType,
			String directorNumber) {
		String base64 = servicesFactory.base64Encoder(filePath, fileContentType);
		if (fileContentType.equalsIgnoreCase(Constants.IMAGE_CHEQUE)) {
			mpaDao.saveChequeBase64(user, payId, base64);
		} else if (fileContentType.equalsIgnoreCase(Constants.IMAGE_DRIVING_LICENSE)) {
			mpaDao.saveDLBase64(user, payId, base64, directorNumber);
		} else if (fileContentType.equalsIgnoreCase(Constants.IMAGE_LOGO)) {
			mpaDao.saveLogoBase64(user, payId, base64);
		} else if (fileContentType.equalsIgnoreCase(Constants.IMAGE_ESIGN)) {
			mpaDao.saveESignBase64(user, payId, base64);
		} else if (fileContentType.equalsIgnoreCase(Constants.IMAGE_DIRECTOR)) {
			String encodedData = base64;
			String imageDataBytes = encodedData.substring(encodedData.indexOf(",") + 1);
			byte[] bytes = Base64.decodeBase64(imageDataBytes);
			String mimeType = encoder.getImageType(bytes);
			String base64WithMime = "data:" + mimeType + ";base64," + encodedData;
			mpaDao.saveDirectorBase64(user, payId, base64WithMime, directorNumber);

			return responseCreatorUI.base64ResponseCreatorForDirector(base64, directorNumber);
		}
		if (StringUtils.isNotBlank(base64)) {
			return responseCreatorUI.base64ResponseCreator(base64);
		} else {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_UPLOAD);
		}
	}

	public void uploadRefundPolicyFile(User user, String payId, File filePath) {
		String base64 = servicesFactory.base64Encoder(filePath, "Refund Policy");
		mpaDao.saveRefundPolicyBase64(user, payId, base64);
	}
}
