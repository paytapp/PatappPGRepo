package com.paymentgateway.crm.action;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.mpa.Constants;

public class ReconDataManager extends AbstractSecureAction {

	@Autowired
	private ReconFileDao reconFileDao;

	private static final long serialVersionUID = -4486785339968262228L;
	private static Logger logger = LoggerFactory.getLogger(ReconDataManager.class.getName());
	private String response;
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private String fileType;
	private String fileContentType;
	private String fileFileName;
	private File file;
	private InputStream fileInputStream;
	String acquirer = null;

	public String execute() {

		try {

			if (reconFileDao.checkAlreadyProcessing()) {
				logger.info("A MPR File is Processing");
				setResponse("A MPR File is Processing, Please Wait. ");
				addActionMessage(response);
				return SUCCESS;
			}

				if (StringUtils.isBlank(fileFileName)) {
					logger.info("No File selected");
					setResponse("Please select a file before upload");
					addActionMessage(response);
					return SUCCESS;
				}

				if (!fileFileName.startsWith("Booking") && !fileFileName.startsWith("BOOKING")) {
					logger.info("Booking File name is invalid. " + fileFileName);
					setResponse("Booking File name is invalid");
					addActionMessage(response);
					return SUCCESS;
				}

				if (fileFileName.contains("MPR") || fileFileName.contains("Refund") || fileFileName.contains("REFUND")) {
					logger.info("Please upload Booking file only. " + fileFileName);
					setResponse("Please upload Booking file only");
					addActionMessage(response);
					return SUCCESS;
				}

				if (reconFileDao.checkFilePresent(fileFileName)) {
					logger.info("File already uploaded with same name. " + fileFileName);
					setResponse((CrmFieldConstants.FILE_ALREADY_PRESENT.getValue()));
					addActionMessage(response);
					return SUCCESS;
				}

				String resp = reconFileDao.validateBookingFile(file);
				if (resp.equalsIgnoreCase("ERROR")) {
					setResponse("One or more entry found invalid, please check and upload again");
					addActionMessage("One or more entry found invalid, please check and upload again!");
					return SUCCESS;
				}

				file.setReadable(false, false);
				file.setWritable(false, false);
				file.setExecutable(false, false);

				file.setReadable(true, false);
				file.setWritable(true, false);
				file.setExecutable(true, false);
				
				File srcFile = new File(PropertiesManager.propertiesMap.get(Constants.RECON_FILE_UPLOAD_LOCATION) + "//"
						+ fileType + "//" + fileFileName);

				srcFile.setReadable(false, false);
				srcFile.setWritable(false, false);
				srcFile.setExecutable(false, false);

				srcFile.setReadable(true, false);
				srcFile.setWritable(true, false);
				srcFile.setExecutable(true, false);
				FileUtils.copyFile(file, srcFile);
				file.delete();
				
				String fileNameSplit[] = fileFileName.split("_");
				
				
				if (fileFileName.contains("ALLAHABAD")) {
					acquirer = "ALLAHABAD BANK";
				}
				else if(fileFileName.contains("MAHARASHTRA")) {
					acquirer = "BANK OF MAHARASHTRA";
				}
				else if(fileFileName.contains("CORPORATION")) {
					acquirer = "CORPORATION BANK";
				}
				else if(fileFileName.contains("INDUSIND")) {
					acquirer = "INDUSIND BANK";
				}
				else if(fileFileName.contains("KARUR")) {
					acquirer = "KARUR BANK";
				}
				else if(fileFileName.contains("PUNJAB")) {
					acquirer = "PUNJAB NATIONAL BANK";
				}
				else {
					acquirer = fileNameSplit[1];
				}
				reconFileDao.insert(fileFileName, fileType, acquirer);

				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							reconFileDao.uploadBookings(srcFile, fileFileName, acquirer);

						} catch (Exception e) {
							logger.error("exception in uploading booking file ", e);
						}
					}

				});
				thread.start();


		} catch (Exception exception) {
			logger.error("Error while processing hotel Inv:  ", exception);
			addActionMessage("Error while processing hotel inventory:  " + exception);
		}
		return SUCCESS;
	}

	public String uploadRefund() {

		try {

			
			if (reconFileDao.checkAlreadyProcessing()) {
				logger.info("Another File is Processing");
				setResponse("Another File is Processing, Please Wait. ");
				addActionMessage(response);
				return SUCCESS;
			}

			if (StringUtils.isBlank(fileFileName)) {
				logger.info("No File selected");
				setResponse("Please select a file before upload");
				addActionMessage(response);
				return SUCCESS;
			}

			if (!getFileContentType().equals("application/vnd.ms-excel")
					&& !getFileContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					&& !getFileContentType().equals("application/vnd.ms-excel.sheet.binary.macroEnabled.12")) {

				logger.info("Please choose .xlsx or .xls  or .xlsb File Format." + fileFileName);
				setResponse("Please choose .xlsx or .xls  or .xlsb File Format.");
				addActionMessage(response);
				return SUCCESS;

			}

			if (!fileFileName.startsWith("Refund") && !fileFileName.startsWith("REFUND")) {
				logger.info("Refund File name is invalid. " + fileFileName);
				setResponse("Refund File name is invalid");
				addActionMessage(response);
				return SUCCESS;
			}

			if (fileFileName.contains("Booking") || fileFileName.contains("BOOKING") || fileFileName.contains("MPR")) {
				logger.info("Please upload Refund file only. " + fileFileName);
				setResponse("Please upload Refund file only");
				addActionMessage(response);
				return SUCCESS;
			}

			if (reconFileDao.checkFilePresent(fileFileName)) {
				logger.info("File already uploaded with same name. " + fileFileName);
				setResponse((CrmFieldConstants.FILE_ALREADY_PRESENT.getValue()));
				addActionMessage(response);
				return SUCCESS;
			}

			String resp = reconFileDao.validateRefundFile(file);
			if (resp.equalsIgnoreCase("ERROR")) {

				setResponse("One or more entry found invalid, please check and upload again");
				addActionMessage("One or more entry found invalid, please check and upload again!");
				return SUCCESS;
			}

			file.setReadable(false, false);
			file.setWritable(false, false);
			file.setExecutable(false, false);

			file.setReadable(true, false);
			file.setWritable(true, false);
			file.setExecutable(true, false);

			File srcFile = new File(PropertiesManager.propertiesMap.get(Constants.RECON_FILE_UPLOAD_LOCATION) + "//"
					+ fileType + "//" + fileFileName);

			srcFile.setReadable(false, false);
			srcFile.setWritable(false, false);
			srcFile.setExecutable(false, false);

			srcFile.setReadable(true, false);
			srcFile.setWritable(true, false);
			srcFile.setExecutable(true, false);
			FileUtils.copyFile(file, srcFile);
			file.delete();

			String fileNameSplit[] = fileFileName.split("_");
			
			if (fileFileName.contains("ALLAHABAD")) {
				acquirer = "ALLAHABAD BANK";
			}
			else if(fileFileName.contains("MAHARASHTRA")) {
				acquirer = "BANK OF MAHARASHTRA";
			}
			else if(fileFileName.contains("CORPORATION")) {
				acquirer = "CORPORATION BANK";
			}
			else if(fileFileName.contains("INDUSIND")) {
				acquirer = "INDUSIND BANK";
			}
			else if(fileFileName.contains("KARUR")) {
				acquirer = "KARUR BANK";
			}
			else if(fileFileName.contains("PUNJAB")) {
				acquirer = "PUNJAB NATIONAL BANK";
			}
			else {
				acquirer = fileNameSplit[1];
			}

			reconFileDao.insert(fileFileName, fileType, acquirer);

			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						reconFileDao.uploadRefunds(srcFile, fileFileName, acquirer);

					} catch (Exception e) {
						logger.error("exception in uploading booking file ", e);
					}
				}

			});
			thread.start();

		} catch (Exception exception) {
			logger.error("Error while processing refund file:  ", exception);
			addActionMessage("Error while processing refund file");
		}
		return SUCCESS;
	}

	public String uploadMpr() {

		try {

			if (reconFileDao.checkAlreadyProcessing()) {
				logger.info("Another File is Processing");
				setResponse("Another File is Processing, Please Wait. ");
				addActionMessage(response);
				return SUCCESS;
			}

			if (StringUtils.isBlank(fileFileName)) {
				logger.info("No File selected");
				setResponse("Please select a file before upload");
				addActionMessage(response);
				return SUCCESS;
			}

			if (!fileFileName.startsWith("MPR") && !fileFileName.startsWith("Mpr")) {
				logger.info("MPR File name is invalid. " + fileFileName);
				setResponse("MPR File name is invalid");
				addActionMessage(response);
				return SUCCESS;
			}

			if (reconFileDao.checkFilePresent(fileFileName)) {
				logger.info("File already uploaded with same name. " + fileFileName);
				setResponse((CrmFieldConstants.FILE_ALREADY_PRESENT.getValue()));
				addActionMessage(response);
				return SUCCESS;
			}

			file.setReadable(false, false);
			file.setWritable(false, false);
			file.setExecutable(false, false);

			file.setReadable(true, false);
			file.setWritable(true, false);
			file.setExecutable(true, false);

			File srcFile = new File(PropertiesManager.propertiesMap.get(Constants.RECON_FILE_UPLOAD_LOCATION) + "//"
					+ fileType + "//" + fileFileName);

			srcFile.setReadable(false, false);
			srcFile.setWritable(false, false);
			srcFile.setExecutable(false, false);

			srcFile.setReadable(true, false);
			srcFile.setWritable(true, false);
			srcFile.setExecutable(true, false);
			FileUtils.copyFile(file, srcFile);
			file.delete();

			String fileNameSplit[] = fileFileName.split("_");

			if (fileFileName.contains("ALLAHABAD")) {
				acquirer = "ALLAHABAD BANK";
			}
			else if(fileFileName.contains("MAHARASHTRA")) {
				acquirer = "BANK OF MAHARASHTRA";
			}
			else if(fileFileName.contains("CORPORATION")) {
				acquirer = "CORPORATION BANK";
			}
			else if(fileFileName.contains("INDUSIND")) {
				acquirer = "INDUSIND BANK";
			}
			else if(fileFileName.contains("KARUR")) {
				acquirer = "KARUR BANK";
			}
			else if(fileFileName.contains("PUNJAB")) {
				acquirer = "PUNJAB NATIONAL BANK";
			}
			else {
				acquirer = fileNameSplit[1];
			}
			
			reconFileDao.insert(fileFileName, fileType, acquirer);

			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {

						if (acquirer.equalsIgnoreCase("AMEX")) {
							reconFileDao.uploadAmexMpr(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("RUPAY")) {
							reconFileDao.uploadRupayMpr(srcFile, fileFileName);
						}

					} catch (Exception e) {
						logger.error("exception in uploading booking file ", e);
					}
				}

			});
			thread.start();

		} catch (Exception exception) {
			logger.error("Error while processing hotel Inv:  ", exception);
			addActionMessage("Error while processing hotel inventory:  " + exception);
		}
		return SUCCESS;
	}

	public String uploadStatement() {

		try {

			if (reconFileDao.checkAlreadyProcessing()) {
				logger.info("Another File is Processing");
				setResponse("Another File is Processing, Please Wait. ");
				addActionMessage(response);
				return SUCCESS;
			}

			if (StringUtils.isBlank(fileFileName)) {
				logger.info("No File selected");
				setResponse("Please select a file before upload");
				addActionMessage(response);
				return SUCCESS;
			}

			if (!fileFileName.startsWith("STATEMENT") && !fileFileName.startsWith("Statement")) {
				logger.info("STATEMENT File name is invalid. " + fileFileName);
				setResponse("STATEMENT File name is invalid");
				addActionMessage(response);
				return SUCCESS;
			}

			if (fileFileName.contains("BOOKING") || fileFileName.contains("REFUND") || fileFileName.contains("Booking")
					|| fileFileName.contains("Refund") || fileFileName.contains("Mpr")
					|| fileFileName.contains("MPR")) {
				logger.info("Please upload STATEMENT file only. " + fileFileName);
				setResponse("Please upload STATEMENT file only");
				addActionMessage(response);
				return SUCCESS;
			}

			if (reconFileDao.checkFilePresent(fileFileName)) {
				logger.info("File already uploaded with same name. " + fileFileName);
				setResponse((CrmFieldConstants.FILE_ALREADY_PRESENT.getValue()));
				addActionMessage(response);
				return SUCCESS;
			}

			file.setReadable(false, false);
			file.setWritable(false, false);
			file.setExecutable(false, false);

			file.setReadable(true, false);
			file.setWritable(true, false);
			file.setExecutable(true, false);

			File srcFile = new File(PropertiesManager.propertiesMap.get(Constants.RECON_FILE_UPLOAD_LOCATION) + "//"
					+ fileType + "//" + fileFileName);

			srcFile.setReadable(false, false);
			srcFile.setWritable(false, false);
			srcFile.setExecutable(false, false);

			srcFile.setReadable(true, false);
			srcFile.setWritable(true, false);
			srcFile.setExecutable(true, false);
			FileUtils.copyFile(file, srcFile);
			file.delete();

			String fileNameSplit[] = fileFileName.split("_");

			if (fileFileName.contains("ALLAHABAD")) {
				acquirer = "ALLAHABAD BANK";
			}
			else if(fileFileName.contains("MAHARASHTRA")) {
				acquirer = "BANK OF MAHARASHTRA";
			}
			else if(fileFileName.contains("CORPORATION")) {
				acquirer = "CORPORATION BANK";
			}
			else if(fileFileName.contains("INDUSIND")) {
				acquirer = "INDUSIND BANK";
			}
			else if(fileFileName.contains("KARUR")) {
				acquirer = "KARUR BANK";
			}
			else if(fileFileName.contains("PUNJAB")) {
				acquirer = "PUNJAB NATIONAL BANK";
			}
			else {
				acquirer = fileNameSplit[1];
			}
			
			reconFileDao.insert(fileFileName, fileType, acquirer);

			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {

						if (acquirer.equalsIgnoreCase("AMEX")) {
							reconFileDao.uploadAmexStatement(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("RUPAY")) {
							reconFileDao.uploadRupayStatement(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("BOB")) {
							reconFileDao.uploadBOBStatement(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("ALLAHABAD BANK")) {
							reconFileDao.uploadAllahabadStatement(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("BANK OF MAHARASHTRA")) {
							reconFileDao.uploadMaharashtraStatement(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("CORPORATION BANK")) {
							reconFileDao.uploadCorporationStatement(srcFile, fileFileName);
						}
						if (acquirer.equalsIgnoreCase("KARUR BANK")) {
							reconFileDao.uploadKarurStatement(srcFile, fileFileName);
						}

					} catch (Exception e) {
						logger.error("exception in uploading booking file ", e);
					}
				}

			});
			thread.start();

		} catch (Exception exception) {
			logger.error("Error while processing hotel Inv:  ", exception);
			addActionMessage("Error while processing hotel inventory:  " + exception);
		}
		return SUCCESS;
	}

	public void validate() {

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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

}
