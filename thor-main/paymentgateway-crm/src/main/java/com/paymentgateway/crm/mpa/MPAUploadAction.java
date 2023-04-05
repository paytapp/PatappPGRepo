package com.paymentgateway.crm.mpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Amitosh Aanand
 *
 */
public class MPAUploadAction extends AbstractSecureAction {

	@Autowired
	private FileUploader uploader;

	@Autowired
	private CrmValidator validator;

	private File chequeFile;
	private File logoFile;
	private File dlFile;
	private File file;
	private File directorImage;
	private File eSignFile;
	private String fileName;

	private String fileContentType;
	private String directorNumber;

	private String payId;

	private Object mpaData;

	private String docFile;
	private boolean setupFlag;
	
	private InputStream fileInputStream;

	private User user = new User();
	private static final long serialVersionUID = 1006697569311252710L;
	private static Logger logger = LoggerFactory.getLogger(MPAUploadAction.class.getName());

	public String execute() {
		user = (User) sessionMap.get("USER");
		if (user.getUserType().equals(UserType.MERCHANT)) {
			setMpaData(uploader.uploadFile(file, fileName, fileContentType, user, payId, setupFlag));
		}else if (user.getUserType().equals(UserType.SUBUSER)) {
			setMpaData(uploader.uploadFile(file, fileName, fileContentType, user, user.getParentPayId(), setupFlag));
		} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
			setMpaData(uploader.uploadFile(file, fileName, fileContentType, user, payId, setupFlag));
		}
		return SUCCESS;
	}

	public String uploadCheckerDocument() {
		user = (User) sessionMap.get("USER");
		if (user.getUserType().equals(UserType.MERCHANT)) {
			setMpaData(uploader.uploadCheckerFile(file, fileName, fileContentType, user, payId));
		} else if (user.getUserType().equals(UserType.SUBUSER)) {
			setMpaData(uploader.uploadCheckerFile(file, fileName, fileContentType, user, user.getParentPayId()));
		} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
			setMpaData(uploader.uploadCheckerFile(file, fileName, fileContentType, user, payId));
		}
		return SUCCESS;
	}
	
	public String downloadTnCPolicy() {
		try {
			if (docFile.equalsIgnoreCase("privacyPolicy")) {
				fileInputStream = new FileInputStream(new File(PropertiesManager.propertiesMap.get(Constants.MPA_POLICY_FILE_LOCATION) + "privacyPolicy.pdf"));
				fileName = "privacyPolicy.pdf";
			} else if (docFile.equalsIgnoreCase("refundPolicy")) {
				fileInputStream = new FileInputStream(new File(PropertiesManager.propertiesMap.get(Constants.MPA_POLICY_FILE_LOCATION) + "refundPolicy.pdf"));
				fileName = "refundPolicy.pdf";
			} else {
				fileInputStream = new FileInputStream(new File(PropertiesManager.propertiesMap.get(Constants.MPA_POLICY_FILE_LOCATION) + "tnc.pdf"));
				fileName = "tnc.pdf";
			}
		}catch (Exception exception) {
			logger.error("Exception Cought while Downloading policy file : ", exception);
		}
		return SUCCESS;
	}

	public String processImage() {
		String fileContentType = "";
		File file = null;
		if (chequeFile != null) {
			fileContentType = Constants.IMAGE_CHEQUE;
			file = chequeFile;
		} else if (logoFile != null) {
			fileContentType = Constants.IMAGE_LOGO;
			file = logoFile;
		} else if (dlFile != null) {
			fileContentType = Constants.IMAGE_DRIVING_LICENSE;
			file = dlFile;
		} else if (directorImage != null) {
			fileContentType = Constants.IMAGE_DIRECTOR;
			file = directorImage;
		}
		logger.info("Uploading " + fileContentType + " in Base64 in DB");
		user = (User) sessionMap.get("USER");
		if (user.getUserType().equals(UserType.MERCHANT)) {
			setMpaData(uploader.uploadImageFile(user, user.getPayId(), file, fileContentType, directorNumber));
		} else if (user.getUserType().equals(UserType.SUBUSER)) {
			setMpaData(uploader.uploadImageFile(user, user.getParentPayId(), file, fileContentType, directorNumber));
		} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
			setMpaData(uploader.uploadImageFile(user, payId, file, fileContentType, directorNumber));
		}
		return SUCCESS;
	}

	public void validate() {
		if (!(validator.validateBlankField(getFileName()))) {
			if (!(validator.validateField(CrmFieldType.FILE_NAME, getFileName()))) {
				addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		if (!(validator.validateBlankField(getDirectorNumber()))) {
			if (!(validator.validateField(CrmFieldType.DIRECTOR_NUMBER, getDirectorNumber()))) {
				addFieldError(CrmFieldType.DIRECTOR_NUMBER.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}
		if (!(validator.validateBlankField(getFileName()))) {
			String fileNameArray[] = getFileName().split(("\\."));
			if (fileNameArray.length > 1) {
				if (!(fileNameArray[1].trim().equalsIgnoreCase("pdf"))
						|| (fileNameArray[1].trim().equalsIgnoreCase("jpg"))
						|| (fileNameArray[1].trim().equalsIgnoreCase("png")
								|| (fileNameArray[1].trim().equalsIgnoreCase("jpeg")))) {
					addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
				}
			}
		}
		/*
		 * if (!(validator.validateBlankField(getFileContentType()))) { if
		 * (!(validator.validateField(CrmFieldType.FILE_CONTENT_TYPE,
		 * getFileContentType()))) {
		 * addFieldError(CrmFieldType.FILE_CONTENT_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
	}

	public File geteSignFile() {
		return eSignFile;
	}

	public void seteSignFile(File eSignFile) {
		this.eSignFile = eSignFile;
	}

	public File getChequeFile() {
		return chequeFile;
	}

	public void setChequeFile(File chequeFile) {
		this.chequeFile = chequeFile;
	}

	public File getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(File logoFile) {
		this.logoFile = logoFile;
	}

	public File getDlFile() {
		return dlFile;
	}

	public void setDlFile(File dlFile) {
		this.dlFile = dlFile;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDirectorNumber() {
		return directorNumber;
	}

	public void setDirectorNumber(String directorNumber) {
		this.directorNumber = directorNumber;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public Object getMpaData() {
		return mpaData;
	}

	public void setMpaData(Object mpaData) {
		this.mpaData = mpaData;
	}

	public File getDirectorImage() {
		return directorImage;
	}

	public void setDirectorImage(File directorImage) {
		this.directorImage = directorImage;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getDocFile() {
		return docFile;
	}

	public void setDocFile(String docFile) {
		this.docFile = docFile;
	}

	public boolean isSetupFlag() {
		return setupFlag;
	}

	public void setSetupFlag(boolean setupFlag) {
		this.setupFlag = setupFlag;
	}
	
}