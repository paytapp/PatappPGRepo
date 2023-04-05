package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.ReportGenerateObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;

public class DownloadReportGenerateAction extends AbstractSecureAction {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -4706754258226108403L;
	private static Logger logger = LoggerFactory.getLogger(DownloadReportGenerateAction.class.getName());
	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;
	
	
	private String dateFrom;
	private String dateTo;
	private String createDate;
	private String reportTypeName;
	private String reportFileName;
	private String merchantPayId;
	private String subMerchantPayId;
	private String subUserPayId;
	private InputStream fileInputStream;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	
	private List<ReportGenerateObject> reportFileDataList;
	
	private User sessionUser = new User();
	public String fetchGeneratedReportFilesList() {
		
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
//			boolean current = false;
//			if(dateFrom.equalsIgnoreCase(dateTo)) {
//				current = true;
//			}
			setDateFrom(DateCreater.toDateTimeformatCreater(createDate));
			setDateTo(DateCreater.formDateTimeformatCreater(createDate));
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)) {
				
			} else if(sessionUser.getUserType().equals(UserType.MERCHANT) && !sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())){
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			} else if(sessionUser.getUserType().equals(UserType.SUBUSER) && sessionUser.getSubUserType().equalsIgnoreCase("normalType")){
				merchantPayId = sessionUser.getParentPayId();
			} else {
				merchantPayId = sessionUser.getPayId();
			}
			int totalCount = reportGenerateService.fetchFileListCount(dateFrom, dateTo, merchantPayId, subMerchantPayId, subUserPayId, reportTypeName, sessionUser);
			setRecordsTotal(BigInteger.valueOf(totalCount));
			if (getLength() == -1) {
				setLength(getRecordsTotal().intValue());
			}
			setReportFileDataList(reportGenerateService.fetchFileList(dateFrom, dateTo, merchantPayId, subMerchantPayId, subUserPayId, reportTypeName, start, length, sessionUser));
			recordsFiltered = recordsTotal;
		} catch (Exception ex) {
			logger.error("Exception in getNetSettledFilesList : ", ex);
		}
		return SUCCESS;
		
	}
	
	public String downloadGeneratedReportFile() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			String dateFolder = createDate.split(" ")[0];
			
			List<String> filenames = new ArrayList<String>();
			boolean dbexist = reportGenerateService.getFileStatus(dateFrom, dateTo, reportFileName, merchantPayId,
					subMerchantPayId, subUserPayId, sessionUser.getPayId());
			String fileLocation = "";
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "AdminCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "SubAdminCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "ResellerCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "SuperMerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "SubMerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
				} else {
					fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "MerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
				}
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "SubUserCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
			}
			File[] listFiles = new File(fileLocation).listFiles();
			for (File file : listFiles) {
				filenames.add(file.getName());
			}
			String location = fileLocation + reportFileName;
			File file2 = new File(location);
			if (filenames.contains(reportFileName) && dbexist) {
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
			logger.error("Exception : ", ex);
		}
		return SUCCESS;
	}
	
	
	public String getReportTypeName() {
		return reportTypeName;
	}
	public void setReportTypeName(String reportTypeName) {
		this.reportTypeName = reportTypeName;
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
	
	public String getReportFileName() {
		return reportFileName;
	}

	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public List<ReportGenerateObject> getReportFileDataList() {
		return reportFileDataList;
	}

	public void setReportFileDataList(List<ReportGenerateObject> reportFileDataList) {
		this.reportFileDataList = reportFileDataList;
	}

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
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

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getSubUserPayId() {
		return subUserPayId;
	}

	public void setSubUserPayId(String subUserPayId) {
		this.subUserPayId = subUserPayId;
	}
	
}
