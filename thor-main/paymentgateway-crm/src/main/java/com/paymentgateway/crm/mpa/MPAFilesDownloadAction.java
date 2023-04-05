package com.paymentgateway.crm.mpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

public class MPAFilesDownloadAction extends ActionSupport {

	private static final long serialVersionUID = 4792797733851154458L;
	private static Logger logger = LoggerFactory.getLogger(MPAFilesDownloadAction.class.getName());

	private String payId;
	private String checkerFileName;
	private String makerFileName;
	private String adminFileName;
	private String filePath;
	private InputStream fileInputStream;
	private String zipFileName;
	private String fileName;
	private String fileNameType;
	
	private List<File> filesList = new ArrayList<File>();

	@Autowired
	private MPAServicesFactory mpaServicesFactory;

	public String execute() {

		String fileLocation = "";
		String[] srcFiles = null;
		try {
			if (!StringUtils.isEmpty(getCheckerFileName())) {
				fileLocation = PropertiesManager.propertiesMap.get("CheckerMakerStatusUpdateFilePath") + getPayId()
						+ "/" + Constants.CHECKER_FILES.getValue();
				zipFileName = Constants.CHECKER_FILES.getValue() + ".zip";
				srcFiles = getCheckerFileName().split(",");
			} else if (!StringUtils.isEmpty(getMakerFileName())) {

				fileLocation = PropertiesManager.propertiesMap.get("CheckerMakerStatusUpdateFilePath") + getPayId()
						+ "/" + Constants.MAKER_FILES.getValue();
				zipFileName = Constants.MAKER_FILES.getValue() + ".zip";
				srcFiles = getMakerFileName().split(",");
			} else {
				fileLocation = PropertiesManager.propertiesMap.get("CheckerMakerStatusUpdateFilePath") + getPayId()
						+ "/" + Constants.ADMIN_FILES.getValue();
				zipFileName = Constants.ADMIN_FILES.getValue() + ".zip";
				srcFiles = getAdminFileName().split(",");
			}

			byte[] buffer = new byte[1024];
			FileOutputStream fos = new FileOutputStream(zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			if (srcFiles != null) {
				for (int i = 0; i < srcFiles.length; i++) {
					File srcFile = new File(fileLocation, srcFiles[i]);
					FileInputStream fis = new FileInputStream(srcFile);
					zos.putNextEntry(new ZipEntry(srcFile.getName()));

					int length;

					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
				}
				File file = new File(zipFileName);
				fileInputStream = new FileInputStream(file);
			}
			zos.close();
			if (fileInputStream != null) {
				logger.info("File Downloaded Successfully");
			} else if (fileInputStream == null) {
				logger.error("File Not Found");
				return ERROR;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return SUCCESS;
	}

	public String merchantDocumentsDownload() {
		try {
			MerchantProcessingApplication mpaData = mpaServicesFactory.getMPADataByPayId(getPayId());
			StringBuilder allfileNames = new StringBuilder();
			String[] srcFiles;
			if (StringUtils.isNotBlank(fileNameType) && fileNameType.equalsIgnoreCase("MPA")) {

				String fileLocation = PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "//"
						+ getPayId() + "//" + "mpaFiles";
 				if (StringUtils.isNotBlank(mpaData.getMerchantName())) {
					zipFileName = mpaData.getMerchantName() + ".zip";
				} else {
					zipFileName = "mpaFiles_" + getPayId() + ".zip";
				}

				if (StringUtils.isNotBlank(mpaData.getMpaFiles())) {
					srcFiles = mpaData.getMpaFiles().split(",");
					fileInputStream = zipFileDownloader(srcFiles, zipFileName, fileLocation);
				} else {
					File folder = new File(fileLocation);
					File[] listOfFiles = folder.listFiles();

					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							allfileNames.append(listOfFiles[i].getName());
							allfileNames.append(",");
						}
					}
					String name = allfileNames.toString();
					srcFiles = name.substring(0, name.length() - 1).split(",");

					fileInputStream = zipFileDownloader(srcFiles, zipFileName, fileLocation);
				}
			} else if (StringUtils.isNotBlank(fileNameType) && fileNameType.equalsIgnoreCase("CHECKER_FILE")) {

				String fileLocation = PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "/" + getPayId()
						+ "/" + "checkerFiles";
				zipFileName = "checkerFiles" + "_" + getPayId() + ".zip";
				File folder = new File(fileLocation);
				File[] listOfFiles = folder.listFiles();

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						allfileNames.append(listOfFiles[i].getName());
						allfileNames.append(",");
					}
				}
				String name = allfileNames.toString();
				srcFiles = name.substring(0, name.length() - 1).split(",");

				fileInputStream = zipFileDownloader(srcFiles, zipFileName, fileLocation);
			} else if (StringUtils.isNotBlank(fileNameType) && fileNameType.equalsIgnoreCase("PRIVACY_POLICY")) {
				String fileLocation = PropertiesManager.propertiesMap.get("MPA_TNC_FILE_LOCATION");
				zipFileName = "privacyPolicy.pdf";
				fileInputStream = fileDownloader(zipFileName, fileLocation);
			} else if (StringUtils.isNotBlank(fileNameType) && fileNameType.equalsIgnoreCase("TNC")) {
				String fileLocation = PropertiesManager.propertiesMap.get("MPA_TNC_FILE_LOCATION");
				zipFileName = "checkerFiles" + "_" + getPayId() + ".pdf";
				fileInputStream = fileDownloader(zipFileName, fileLocation);
			} else {
				String fileLocation = PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION");
				zipFileName = "tnc.pdf";
				fileInputStream = fileDownloader(zipFileName, fileLocation);
			}

			if (fileInputStream != null) {
				logger.info("File Downloaded SuccessFully");
			} else if (fileInputStream == null) {
				throw new SystemException("File Not Found");
			}
		} catch (IOException | SystemException e) {
			logger.error("File Not Found : " , e);	
			return INPUT;
		}

		return SUCCESS;
	}

	public InputStream fileDownloader(String fileName, String fileLocation) throws FileNotFoundException {
		InputStream inputfileStream = null;

		File destFile = new File(fileLocation, fileName);
		inputfileStream = new FileInputStream(destFile);

		return inputfileStream;
	}

	public InputStream zipFileDownloader(String[] srcFiles, String ZipName, String fileLocation) throws IOException {

		byte[] buffer = new byte[1024];
		FileOutputStream fos = new FileOutputStream(zipFileName);
		ZipOutputStream zos = new ZipOutputStream(fos);
		InputStream inputfileStream = null;
		if (srcFiles != null) {

			for (int i = 0; i < srcFiles.length; i++) {
				File srcFile = new File(fileLocation, srcFiles[i]);
				FileInputStream fis = new FileInputStream(srcFile);
				zos.putNextEntry(new ZipEntry(srcFile.getName()));

				int length;

				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
			}
			File file = new File(zipFileName);
			inputfileStream = new FileInputStream(file);
		}
		zos.close();
		return inputfileStream;
	}

	public String getMpaFilesList() {
		
		try {
			String fileLocation = PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "//"
					+ getPayId() + "//" + "mpaFiles";
			
			
			File directoryPath = new File(fileLocation);
	        //List of all files and directories
	        File filesArray[] = directoryPath.listFiles();
	        for(File file : filesArray) {
	        	filesList.add(file);
	        }
		}catch(Exception e) {
			logger.error("File Not Found : " , e);
		}
		return SUCCESS;
	}
	
	public String downLoadSingleMPAFile() {
		
		try {
			File file = new File(filePath);
			fileInputStream = new FileInputStream(file);
			fileName = file.getName();
		} catch (FileNotFoundException e) {
			logger.error("File Not Found : " , e);
		}
		return SUCCESS;
	}
	
	
	public String getFileNameType() {
		return fileNameType;
	}

	public void setFileNameType(String fileNameType) {
		this.fileNameType = fileNameType;
	}

	public String getAdminFileName() {
		return adminFileName;
	}

	public void setAdminFileName(String adminFileName) {
		this.adminFileName = adminFileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getCheckerFileName() {
		return checkerFileName;
	}

	public void setCheckerFileName(String checkerFileName) {
		this.checkerFileName = checkerFileName;
	}

	public String getMakerFileName() {
		return makerFileName;
	}

	public void setMakerFileName(String makerFileName) {
		this.makerFileName = makerFileName;
	}

	public List<File> getFilesList() {
		return filesList;
	}

	public void setFilesList(List<File> filesList) {
		this.filesList = filesList;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
