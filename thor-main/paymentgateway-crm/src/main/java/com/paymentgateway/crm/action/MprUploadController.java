package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MprUploadDetailsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.MprUploadDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;

public class MprUploadController extends AbstractSecureAction {

	private static final long serialVersionUID = 5320552864236786025L;

	private static Logger logger = LoggerFactory.getLogger(MprUploadController.class.getName());

	private String acquirerName;
	private String paymentType;
	private String mprDate;
	private File file;
	private String fileContentType;
	private String fileFileName;
	private String fileFilePath;
	private InputStream fileInputStream;
	private User sessionUser = null;
	private ServletContext context;
	private String filename;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MprUploadDetailsDao mprDao;

	@SuppressWarnings("unchecked")
	public String execute() {
		logger.info("Inside MprUploadController in execute method !!");
		String mprUploadPath = PropertiesManager.propertiesMap.get("MprFilePath");

		if (StringUtils.isBlank(acquirerName)) {
			addActionMessage("Please Select Acquirer Name");
			return SUCCESS;
		}

		if (StringUtils.isBlank(paymentType)) {
			addActionMessage("Please Select Payment Type");
			return SUCCESS;
		}

		if (file != null) {

			if (getFileContentType().equals("application/vnd.ms-excel")
					|| getFileContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					|| getFileContentType().equals("application/vnd.ms-excel.sheet.binary.macroEnabled.12")) {
				// Clear all permissions for all users
				file.setReadable(false, false);
				file.setWritable(false, false);
				file.setExecutable(false, false);

				file.setReadable(true, false);
				file.setWritable(true, false);
				file.setExecutable(true, false);

				// if (file.length() >= 0) {

				try {
					sessionUser = (User) sessionMap.get(Constants.USER.getValue());
					File srcFile = new File(mprUploadPath + fileFileName);
					MprUploadDetails existMpr = mprDao.existMprFile(acquirerName, paymentType, mprDate);

					if (existMpr != null) {

						if (srcFile.exists() && (existMpr.getAcquirerName().equals(acquirerName)
								&& existMpr.getPaymentType().equals(paymentType)
								&& existMpr.getMprDate().equals(mprDate))) {

							addActionMessage("FILE ALREADY UPLOADED!!");
						} else {
							addActionMessage("FILE ALREADY UPLOADED!!");
						}

					} else {
						MprUploadDetails upload = new MprUploadDetails();
						DateFormat df = new SimpleDateFormat("dd/MM/yy");
						Calendar calobj = Calendar.getInstance();
						Date date = new Date();
						// Clear all permissions for all users
						srcFile.setReadable(false, false);
						srcFile.setWritable(false, false);
						srcFile.setExecutable(false, false);

						srcFile.setReadable(true, false);
						srcFile.setWritable(true, false);
						srcFile.setExecutable(true, false);

						upload.setAcquirerName(acquirerName);
						upload.setPaymentType(paymentType);
						upload.setMprDate(mprDate);
						upload.setFileName(fileFileName);
						upload.setCreatedDate(calobj.getTime());
						mprDao.create(upload);
						FileUtils.copyFile(file, srcFile);
						addActionMessage("FILE UPLOADED SUCCESSFULLY!!");
					}

					return SUCCESS;
				} catch (Exception e) {
					return ERROR;
				}
				/*
				 * } else { return INPUT; }
				 */
			} else {
				addActionMessage("PLEASE CHOOSE .CSV or .xlsx or .xls  or .xlsb FILE FORMATE !!");
				return SUCCESS;
			}
		}

		else {
			addActionMessage("PLEASE CHOOSE .CSV or .xlsx or .xls or .xlsb FILE FORMATE !!");
			return SUCCESS;

		}

	}

	public String downloadReport() {
		logger.info("Inside MprUploadController Class download Method.. !!");

		try {
			MprUploadDetails existMpr = mprDao.existMprFile(acquirerName, paymentType, mprDate);

			if (existMpr != null) {
//				String path = "E:/DATA/mprFile/";

				String path = PropertiesManager.propertiesMap.get("MprFilePath");
				setFilename(existMpr.getFileName());
				File file = new File(path + getFilename());
				setFileInputStream(new FileInputStream(file));

			} else {
				addActionMessage("File Does Not Exist on Server!!");
				return INPUT;
			}

		} catch (Exception exception) {
			logger.error("Inside MprUploadController Class, in download method  : ", exception);
		}

		return SUCCESS;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public String getFileFileName() {
		return fileFileName;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public String getFileFilePath() {
		return fileFilePath;
	}

	public void setFileFilePath(String fileFilePath) {
		this.fileFilePath = fileFilePath;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getMprDate() {
		return mprDate;
	}

	public void setMprDate(String mprDate) {
		this.mprDate = mprDate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
