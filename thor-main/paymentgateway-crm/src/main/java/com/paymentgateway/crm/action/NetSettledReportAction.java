package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.NetSettledReportDao;
import com.paymentgateway.commons.user.NodalTransactions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

public class NetSettledReportAction extends AbstractSecureAction {
	private static final long serialVersionUID = -1861401844974209291L;
	private static Logger logger = LoggerFactory.getLogger(NetSettledReportAction.class.getName());
	
	@Autowired
	private NetSettledReportDao netSettledReportDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	PropertiesManager propertiesManager;
	
	private String payId;
	private String subMerchantId;
	private String dateFrom;
	private String dateTo;
	private String createDate;
	private String payOutDate;
	private String response;
	private String responseMsg;
	private String otherAdjustmentCr;
	private String otherAdjustmentDr;
	private String fileName;
	private InputStream fileInputStream;
	private int length;
	private int start;
	private String status;
	private String fileLocation;
	private List<NodalTransactions> aaData = new ArrayList<NodalTransactions>();
	private List<NodalTransactions> netSettledDataFile;
	private User sessionUser;

	public String fetchNetSettledData() {
		logger.info("Inside fetchNetSettledData ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		dateTo = dateFrom;
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)) {
				setAaData(netSettledReportDao.fetchUpdatedData(payId, subMerchantId, dateFrom, dateTo, sessionUser));
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					setAaData(netSettledReportDao.fetchUpdatedData(sessionUser.getPayId(), subMerchantId, dateFrom,
							dateTo, sessionUser));
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					setAaData(netSettledReportDao.fetchUpdatedData(sessionUser.getSuperMerchantId(),
							sessionUser.getPayId(), dateFrom, dateTo, sessionUser));
				} else {
					setAaData(netSettledReportDao.fetchUpdatedData(sessionUser.getPayId(), "", dateFrom, dateTo,
							sessionUser));
				}
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
				User parentMerchant = userDao.findPayId(sessionUser.getParentPayId());
				if (parentMerchant.isSuperMerchant()) {
					setAaData(netSettledReportDao.fetchUpdatedData(parentMerchant.getPayId(), subMerchantId, dateFrom,
							dateTo, parentMerchant));
				} else {
					setAaData(netSettledReportDao.fetchUpdatedData(parentMerchant.getPayId(), "", dateFrom, dateTo,
							parentMerchant));
				}
			}
		} catch (Exception exp) {
			logger.error("Exception Caught in fetchNetSettledData ", exp);
		}
		return SUCCESS;
	}

	public String editOtherAdjustmentAnount() {
		logger.info("Inside editOtherAdjustmentAnount ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				netSettledReportDao.editOtherAdjustmentAmount(payId, subMerchantId, payOutDate, otherAdjustmentCr,
						otherAdjustmentDr);
				setResponse("success");
				setResponseMsg("Adjustment Amount Updated Successfully");
			} else {
				setResponse("failed");
				setResponseMsg("You Are not allowed to Edit.");
			}
		} catch (Exception exp) {
			logger.error("Exception Caught in NodalTxnUtrUpdateAction ", exp);
			setResponse("failed");
			setResponseMsg("Adjustment Amount Could't be Updated, Something went wrong");
		}
		return SUCCESS;
	}

	public String generateNetSettledReport() {
		logger.info("Inside generateNetSettledReport ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String sessionPayId = null;
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			fileLocation = propertiesManager.propertiesMap.get(Constants.NET_SETTLED_FILE_LOCATION_URL.getValue());
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
				fileName = "Net_Settled_Report _" + df.format(new Date()) + FILE_EXTENSION;
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
				sessionPayId = sessionUser.getParentPayId();
				fileName = "Net_Settled_Report _" + sessionPayId + "_" + df.format(new Date()) + FILE_EXTENSION;
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				sessionPayId = sessionUser.getPayId();
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
				fileName = "Net_Settled_Report _" + sessionPayId + "_" + df.format(new Date()) + FILE_EXTENSION;
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					fileLocation = fileLocation + "SUPERMERCHANT" + "/";
					sessionPayId = sessionUser.getPayId();
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					fileLocation = fileLocation + "SUBMERCHANT" + "/";
					sessionPayId = sessionUser.getPayId();
				} else {
					fileLocation = fileLocation + sessionUser.getUserType() + "/";
					sessionPayId = sessionUser.getPayId();
				}
				fileName = "Net_Settled_Report _" + sessionPayId + "_" + df.format(new Date()) + FILE_EXTENSION;
			}
			try {
				Files.createDirectories(Paths.get(fileLocation));
			} catch (IOException e1) {
				logger.error("Error in creating Directory ", e1);
			}
			String checkLatestStatus = netSettledReportDao.checkFileStatus(dateFrom, fileName);

			if (StringUtils.isNotBlank(checkLatestStatus)
					&& checkLatestStatus.equalsIgnoreCase(StatusType.PROCESSING.getName())) {
				setStatus(checkLatestStatus);
				return SUCCESS;
			}

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						generateNetSettledReport(dateFrom, sessionUser, fileLocation);
					} catch (Exception e) {
						logger.error("Exception while generating Net Settled Report ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);

		} catch (Exception e) {
			logger.error("Exception while Generating NetSettled Report ", e);
		}
		return SUCCESS;
	}

	public String generateNetSettledReport(String payOutDate, User sessionUser, String fileLocation) {
		logger.info("Inside generateNetSettledReport ");
		String sessionPayId = null;
		setDateFrom(DateCreater.toDateTimeformatCreater(payOutDate));
		setDateTo(DateCreater.formDateTimeformatCreater(payOutDate));
		try {
			
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)) {
				insertDatalInDb(dateFrom, fileName, fileLocation, sessionPayId);
				setAaData(netSettledReportDao.fetchUpdatedData(payId, subMerchantId, dateFrom, dateTo, sessionUser));
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				sessionPayId = sessionUser.getPayId();
				if (sessionUser.isSuperMerchant()) {
					insertDatalInDb(dateFrom, fileName, fileLocation, sessionPayId);
					setAaData(netSettledReportDao.fetchUpdatedData(sessionUser.getPayId(), subMerchantId, dateFrom,
							dateTo, sessionUser));
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					insertDatalInDb(dateFrom, fileName, fileLocation, sessionPayId);
					setAaData(netSettledReportDao.fetchUpdatedData(sessionUser.getSuperMerchantId(),
							sessionUser.getPayId(), dateFrom, dateTo, sessionUser));
				} else {
					insertDatalInDb(dateFrom, fileName, fileLocation, sessionPayId);
					setAaData(netSettledReportDao.fetchUpdatedData(sessionUser.getPayId(), "", dateFrom, dateTo,
							sessionUser));
				}
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
				sessionPayId = sessionUser.getPayId();
				User parentMerchant = userDao.findPayId(sessionUser.getParentPayId());
				if (parentMerchant.isSuperMerchant()) {
					insertDatalInDb(dateFrom, fileName, fileLocation, sessionPayId);
					setAaData(netSettledReportDao.fetchUpdatedData(parentMerchant.getPayId(), subMerchantId, dateFrom,
							dateTo, parentMerchant));
				} else {
					insertDatalInDb(dateFrom, fileName, fileLocation, sessionPayId);
					setAaData(netSettledReportDao.fetchUpdatedData(parentMerchant.getPayId(), "", dateFrom, dateTo,
							parentMerchant));
				}
			}
			if (!aaData.isEmpty()) {
				logger.info("List generated successfully for NetSettled Report");
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				int rownum = 1;
				Sheet sheet = wb.createSheet("Net Settled Report");
				Row row = sheet.createRow(0);
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant");
				row.createCell(2).setCellValue("Sub-Merchant");
				row.createCell(3).setCellValue("Captured date From");
				row.createCell(4).setCellValue("Captured Date To");
				row.createCell(5).setCellValue("Payout Date");
				row.createCell(6).setCellValue("Sale Capture (Txns)");
				row.createCell(7).setCellValue("Sale Capture(Amount)");
				row.createCell(8).setCellValue("Refund Capture (Txns)");
				row.createCell(9).setCellValue("Refund Capture(Amount)");
				row.createCell(10).setCellValue("Sale Settled(Txns)");
				row.createCell(11).setCellValue("Sale Settle(Amount)");
				row.createCell(12).setCellValue("Refund Settled(Txns)");
				row.createCell(13).setCellValue("Refund Settled(Amount)");
				row.createCell(14).setCellValue("Chargeback (Cr)");
				row.createCell(15).setCellValue("Chargeback (Dr)");
				row.createCell(16).setCellValue("Other Adjustments(Cr)");
				row.createCell(17).setCellValue("Other Adjustments(Dr)");
				row.createCell(18).setCellValue("Net Settled");
				for (NodalTransactions transaction : aaData) {
					row = sheet.createRow(rownum++);
					transaction.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = transaction.netSettledConsolidatedDownload();
					int cellnum = 0;
					for (Object obj : objArr) {
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String) {
							cell.setCellValue((String) obj);
						} else if (obj instanceof Integer) {
							cell.setCellValue((Integer) obj);
						}
					}
				}
				
				File file = new File(fileLocation, fileName);
				FileOutputStream out = new FileOutputStream(file);
				wb.write((OutputStream) out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info("Files generated successfully for generateNetSettledReport");
				netSettledReportDao.insertFileStatusInDB(dateFrom, fileName, fileLocation, sessionPayId);
			}
			
			if(aaData.isEmpty()) {
				netSettledReportDao.deleteFileStatus(dateFrom, fileName);
			}
			
		} catch (Exception exp) {
			logger.error("Exception Caught in generateNetSettledReport ", exp);
		}
		return SUCCESS;
	}

	public String netSettledFilesList() {
		logger.info("Inside netSettledFilesList ");
		String sessionPayId = null;
		String fileLocation = null;
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			fileLocation = propertiesManager.propertiesMap.get(Constants.NET_SETTLED_FILE_LOCATION_URL.getValue());
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
				sessionPayId = sessionUser.getParentPayId();
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				sessionPayId = sessionUser.getPayId();
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					fileLocation = fileLocation + "SUPERMERCHANT" + "/";
					sessionPayId = sessionUser.getPayId();
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					fileLocation = fileLocation + "SUBMERCHANT" + "/";
					sessionPayId = sessionUser.getPayId();
				} else {
					fileLocation = fileLocation + sessionUser.getUserType() + "/";
					sessionPayId = sessionUser.getPayId();
				}
			}
			
			setNetSettledDataFile(netSettledReportDao.fetchNetSettledFiles(createDate, sessionPayId, fileLocation));
			
		} catch (Exception ex) {
			logger.error("Exception in getNetSettledFilesList : ", ex);
		}
		return SUCCESS;
	}

	public String downloadNetSettledFile() {
		logger.info("Inside downloadNetSettledFile ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			List<String> filenames = new ArrayList<String>();
			boolean dbexist = netSettledReportDao.getFileStatus(createDate, fileName);
			String fileLocation = PropertiesManager.propertiesMap
					.get(Constants.NET_SETTLED_FILE_LOCATION_URL.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				fileLocation = fileLocation + sessionUser.getUserType() + "/";
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					fileLocation = fileLocation + "SUPERMERCHANT" + "/";
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					fileLocation = fileLocation + "SUBMERCHANT" + "/";
				} else {
					fileLocation = fileLocation + sessionUser.getUserType() + "/";
				}
			}

			File[] listFiles = new File(fileLocation).listFiles();
			for (File file : listFiles) {
				filenames.add(file.getName());
			}
			String location = fileLocation + fileName;
			File file2 = new File(location);
			if (filenames.contains(fileName) && dbexist) {
				try {
					FileInputStream inputStream = new FileInputStream(file2);
					setFileInputStream(inputStream);
				} catch (IOException e) {
					logger.error("Error in getting saved file: ", e);
				}
			} else {
				try {
					fileInputStream = new FileInputStream(file2);
				} catch (Exception e2) {
					logger.error("Exception : ", e2);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception caught while downloading file : ", ex);
		}
		return SUCCESS;
	}

	private synchronized void insertDatalInDb(String payOutDate, String fileName, String fileLocation, String sessionPayId) {
		logger.info("Inside insertDatalInDb ");
		try {
			
			File file = new File(fileLocation, fileName);
			
			File[] files = new File(fileLocation).listFiles();
			for (File savedFile : files) {
				if (savedFile.getName().equalsIgnoreCase(fileName)) {
					file.delete();
				}
			}
	//		netSettledReportDao.deleteFileStatus(dateTo, dateFrom, fileName);

			netSettledReportDao.insertFileStatusInDB(payOutDate, fileName, fileLocation, sessionPayId);
		} catch (Exception e) {
			logger.error("exception ", e);
		}
	}
	
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getPayOutDate() {
		return payOutDate;
	}

	public void setPayOutDate(String payOutDate) {
		this.payOutDate = payOutDate;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getOtherAdjustmentCr() {
		return otherAdjustmentCr;
	}

	public void setOtherAdjustmentCr(String otherAdjustmentCr) {
		this.otherAdjustmentCr = otherAdjustmentCr;
	}

	public String getOtherAdjustmentDr() {
		return otherAdjustmentDr;
	}

	public void setOtherAdjustmentDr(String otherAdjustmentDr) {
		this.otherAdjustmentDr = otherAdjustmentDr;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public List<NodalTransactions> getAaData() {
		return aaData;
	}

	public void setAaData(List<NodalTransactions> aaData) {
		this.aaData = aaData;
	}

	public List<NodalTransactions> getNetSettledDataFile() {
		return netSettledDataFile;
	}

	public void setNetSettledDataFile(List<NodalTransactions> netSettledDataFile) {
		this.netSettledDataFile = netSettledDataFile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

}