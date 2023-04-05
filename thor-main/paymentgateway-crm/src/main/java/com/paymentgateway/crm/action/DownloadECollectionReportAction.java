package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.ECollectionObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadECollectionReportAction extends AbstractSecureAction{

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TxnReports txnReports;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadImpsReportAction.class.getName());
	
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	
	private String reportMerchant;
	private String subMerchant;
	private String reportPaymentMode;
	private String reportStatus;
	private String reportTxnType;
	private String reportDateFrom;
	private String reportDateTo;
	private InputStream fileInputStream;
	private String filename;
	private String virtualAccountNo;
	private User sessionUser = new User();
	private boolean generateReport;
	private String reportType;
	public String subUserPayId;
	private User subuser = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@SuppressWarnings("static-access")
	public String execute() {
	
		logger.info("Inside DownloadECollectionReportAction, execute()");
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ECollectionObject> eCollectionList = new ArrayList<ECollectionObject>();
		setReportDateFrom(DateCreater.toDateTimeformatCreater(reportDateFrom));
		setReportDateTo(DateCreater.formDateTimeformatCreater(reportDateTo));
		
		String merchantPayId = "";
		String subMerchantPayId = "";
		User user = null;
		if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
		if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
			user = userDao.findPayId(sessionUser.getParentPayId());
			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				merchantPayId = user.getSuperMerchantId();
				subMerchantPayId = user.getPayId();
			} else {
				if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
					//subMerchantPayIdd = subMerchantPayId;
					subMerchantPayId = userDao.getPayIdByEmailId(subMerchant);
				} else {
					merchantPayId = sessionUser.getParentPayId();
				}
			}
		}
		}else {
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		} else {
			merchantPayId =  reportMerchant;
			
			if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId = userDao.getPayIdByEmailId(subMerchant);
			}
			if(StringUtils.isNotBlank(subMerchant) && subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId  = subMerchant;
			}
		}
		}
		eCollectionList = txnReports.eCollectionForDownload( merchantPayId, subMerchantPayId, reportPaymentMode, reportStatus, reportTxnType, reportDateFrom, reportDateTo ,sessionUser);
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("ECollection Report");
		row = sheet.createRow(0);
		
		if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			
            if(StringUtils.isBlank(subMerchantPayId) && !(merchantPayId.equalsIgnoreCase("ALL"))) {
			
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Pay ID");
			row.createCell(2).setCellValue("Merchant Virtual Account Number");
			row.createCell(3).setCellValue("Transaction Date");
			row.createCell(4).setCellValue("Payment Gateway Code");
			row.createCell(5).setCellValue("Payment Gateway Account Number");
			row.createCell(6).setCellValue("Payment Mode");
			row.createCell(7).setCellValue("Transaction Type");
			row.createCell(8).setCellValue("Payee Name");
			row.createCell(9).setCellValue("Payee Account number");
			row.createCell(10).setCellValue("Payee Bank IFSC");
			row.createCell(11).setCellValue("Bank TXN Number");
			row.createCell(12).setCellValue("Total PA Commission");
			row.createCell(13).setCellValue("Amount");
			row.createCell(14).setCellValue("Txn Amount");
			row.createCell(15).setCellValue("Status");
			row.createCell(16).setCellValue("Sender Remark");
			
			for (ECollectionObject eCollectionTransaction: eCollectionList) {
				row = sheet.createRow(rownum++);
				Object[] objArr = eCollectionTransaction.myCsvMethodDownloadECollectionReportByAdminView();

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
					
				}
			}

			
		} else {

			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub-Merchant Name");
			row.createCell(2).setCellValue("Pay ID");
			row.createCell(3).setCellValue("Merchant Virtual Account Number");
			row.createCell(4).setCellValue("Transaction Date");
			row.createCell(5).setCellValue("Payment Gateway Code");
			row.createCell(6).setCellValue("Payment Gateway Account Number");
			row.createCell(7).setCellValue("Payment Mode");
			row.createCell(8).setCellValue("Transaction Type");
			row.createCell(9).setCellValue("Payee Name");
			row.createCell(10).setCellValue("Payee Account number");
			row.createCell(11).setCellValue("Payee Bank IFSC");
			row.createCell(12).setCellValue("Bank TXN Number");
			row.createCell(13).setCellValue("Total PA Commission");
			row.createCell(14).setCellValue("Amount");
			row.createCell(15).setCellValue("Txn Amount");
			row.createCell(16).setCellValue("Status");
			row.createCell(17).setCellValue("Sender Remark");

			
		
		for (ECollectionObject eCollectionTransaction : eCollectionList) {
			row = sheet.createRow(rownum++);
			Object[] objArr = eCollectionTransaction.myCsvMethodDownloadECollectionReportByAdminViewSubMerchant();

			int cellnum = 0;
			for (Object obj : objArr) {
				// this line creates a cell in the next column of that row
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);
				
			}
		  }
		}
	}else {

		if(StringUtils.isBlank(subMerchantPayId) && !(merchantPayId.equalsIgnoreCase("ALL"))) {
			
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Pay ID");
			row.createCell(2).setCellValue("Merchant Virtual Account Number");
			row.createCell(3).setCellValue("Transaction Date");
			row.createCell(4).setCellValue("Payment Mode");
			row.createCell(5).setCellValue("Transaction Type");
			row.createCell(6).setCellValue("Payee Name");
			row.createCell(7).setCellValue("Payee Account number");
			row.createCell(8).setCellValue("Payee Bank IFSC");
			row.createCell(9).setCellValue("Bank TXN Number");
			row.createCell(10).setCellValue("Total PA Commission");
			row.createCell(11).setCellValue("Amount");
			row.createCell(12).setCellValue("Txn Amount");
			row.createCell(13).setCellValue("Status");
			row.createCell(14).setCellValue("Sender Remark");
	
			
			for (ECollectionObject eCollectionTransaction : eCollectionList) {
				row = sheet.createRow(rownum++);
				Object[] objArr = eCollectionTransaction.myCsvMethodDownloadECollectionReportByMerchantView();

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
					
				}
			}

			
		} else {

			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub-Merchant Name");
			row.createCell(2).setCellValue("Pay ID");
			row.createCell(3).setCellValue("Merchant Virtual Account Number");
			row.createCell(4).setCellValue("Transaction Date");
			row.createCell(5).setCellValue("Payment Mode");
			row.createCell(6).setCellValue("Transaction Type");
			row.createCell(7).setCellValue("Payee Name");
			row.createCell(8).setCellValue("Payee Account number");
			row.createCell(9).setCellValue("Payee Bank IFSC");
			row.createCell(10).setCellValue("Bank TXN Number");
			row.createCell(11).setCellValue("Total PA Commission");
			row.createCell(12).setCellValue("Amount");
			row.createCell(13).setCellValue("Txn Amount");
			row.createCell(14).setCellValue("Status");
			row.createCell(15).setCellValue("Sender Remark");

		
		for (ECollectionObject eCollectionTransaction : eCollectionList) {
			row = sheet.createRow(rownum++);
			Object[] objArr = eCollectionTransaction.myCsvMethodDownloadECollectionReportBySubMerchantView();

			int cellnum = 0;
			for (Object obj : objArr) {
				// this line creates a cell in the next column of that row
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);
				
			}
		  }
		}
      }
	try {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String FILE_EXTENSION = ".xlsx";
		String dateFolder = dateFormat.format(new Date()).split(" ")[0];
		
		if (isGenerateReport()) {
			
			String fileLocation = "";
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)) {
				if (sessionUser.getUserType().equals(UserType.ADMIN)) {
					fileLocation = PropertiesManager.propertiesMap
							.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateFolder + "/"
							+ sessionUser.getPayId() + "/";
				} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
					fileLocation = PropertiesManager.propertiesMap
							.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateFolder + "/"
							+ sessionUser.getPayId() + "/";
				} else {
					fileLocation = PropertiesManager.propertiesMap
							.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/ResellerCreated/" + dateFolder + "/"
							+ sessionUser.getPayId() + "/";
				}
				try {
					Files.createDirectories(Paths.get(fileLocation));
				} catch (IOException e1) {
					logger.error("Error in creating Directory ", e1);
				}
				if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
					if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + "-" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						}
					} else {
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						}
					}
				} else {
					filename = getReportType() + "_Report_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
				}
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					fileLocation = PropertiesManager.propertiesMap
							.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SuperMerchantCreated/" + dateFolder + "/"
							+ sessionUser.getPayId() + "/";
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						}

					} else {
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						}
					}
				} else if (!sessionUser.isSuperMerchant()
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					fileLocation = PropertiesManager.propertiesMap
							.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubMerchantCreated/" + dateFolder + "/"
							+ sessionUser.getPayId() + "/";
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
						filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
					} else {
						filename = getReportType() + "_Report_" + subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
					}
				} else {
					fileLocation = PropertiesManager.propertiesMap
							.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/MerchantCreated/" + dateFolder + "/"
							+ sessionUser.getPayId() + "/";
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
						filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
					} else {
						filename = getReportType() + "_Report_" + merchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
					}

				}
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "/SubUserCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
				try {
					Files.createDirectories(Paths.get(fileLocation));
				} catch (IOException e1) {
					logger.error("Error in creating Directory ", e1);
				}
				filename = getReportType() + "_Report_" + sessionUser.getPayId() + "_" + DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
			}
			
			
			File file = new File(fileLocation, filename);
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			logger.info(filename + " File generated successfully");
			reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchantPayId,
					subMerchantPayId, "",reportDateFrom, reportDateTo, sessionUser.getPayId());
		} else {
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");

			filename = "ECollection_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for DownloadECollectionReportAction");
		}
	} catch (Exception exception) {
		setGenerateReport(false);
		logger.error("Exception", exception);
	}

	return SUCCESS;

}

	
	public String downloadVirtaulAccountList() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ECollectionObject> VAMerchantsList = new ArrayList<ECollectionObject>();
		boolean resellerFlag = false;
		boolean superMerchant = false;

		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
				|| sessionUser.getUserType().equals(UserType.RESELLER)) {
			VAMerchantsList = userDao.getVaDataList(reportMerchant, subMerchant, sessionUser, virtualAccountNo);
		} else if (sessionUser.getUserType().equals(UserType.MERCHANT) || sessionUser.isSuperMerchant()) {
			VAMerchantsList = userDao.getVaDataList(reportMerchant, subMerchant, sessionUser, virtualAccountNo);
		}

		logger.info("VA Merchant List generated successfully");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Virtual Account List");
		row = sheet.createRow(0);

		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
				|| sessionUser.getUserType().equals(UserType.RESELLER)) {

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerFlag = true;
			}
			if(StringUtils.isNotBlank(virtualAccountNo)) {
				for (ECollectionObject VAMerchant : VAMerchantsList) {
					if (StringUtils.isNotBlank(VAMerchant.getReseller()) && !VAMerchant.getReseller().equals("NA")) {
						resellerFlag = true;
					}
					
					if (StringUtils.isNotBlank(VAMerchant.getSubMerchant()) && !VAMerchant.getSubMerchant().equals("NA")) {
						superMerchant = true;
					}
				}
				if (resellerFlag) {
					if (superMerchant) {
						row.createCell(0).setCellValue("Reseller");
						row.createCell(1).setCellValue("Merchants");
						row.createCell(2).setCellValue("Sub-Merchant");
						row.createCell(3).setCellValue("Virtual Account Number");
						row.createCell(4).setCellValue("Virtual Account Flag");
					} else {
						row.createCell(0).setCellValue("Reseller");
						row.createCell(1).setCellValue("Merchants");
						row.createCell(2).setCellValue("Virtual Account Number");
						row.createCell(3).setCellValue("Virtual Account Flag");
					}
				} else {

					if (superMerchant) {
						row.createCell(0).setCellValue("Merchants");
						row.createCell(1).setCellValue("Sub-Merchant");
						row.createCell(2).setCellValue("Virtual Account Number");
						row.createCell(3).setCellValue("Virtual Account Flag");
					} else {
						row.createCell(0).setCellValue("Merchants");
						row.createCell(1).setCellValue("Virtual Account Number");
						row.createCell(2).setCellValue("Virtual Account Flag");
					}
				}
				
				for (ECollectionObject VAMerchant : VAMerchantsList) {
					row = sheet.createRow(rownum++);
					Object[] objArr = VAMerchant.myCsvMethodDownloadVAListByAdminView(resellerFlag, superMerchant);

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
				
			} else if (StringUtils.isNotBlank(reportMerchant) && reportMerchant.equalsIgnoreCase("ALL")) {
				
				row.createCell(0).setCellValue("Reseller");
				row.createCell(1).setCellValue("Merchants");
				row.createCell(2).setCellValue("Virtual Account Number");
				row.createCell(3).setCellValue("Virtual Account Flag");

				for (ECollectionObject VAMerchant : VAMerchantsList) {
					row = sheet.createRow(rownum++);
					Object[] objArr = VAMerchant.myCsvMethodDownloadVAListByAdminView(true, superMerchant);

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}

			} else if (StringUtils.isNotBlank(reportMerchant) && !reportMerchant.equalsIgnoreCase("ALL")
					&& StringUtils.isBlank(subMerchant)) {
				for (ECollectionObject VAMerchant : VAMerchantsList) {
					if (StringUtils.isNotBlank(VAMerchant.getReseller()) && !VAMerchant.getReseller().equals("NA")) {
						resellerFlag = true;
						break;
					}
				}
				if (resellerFlag) {
					row.createCell(0).setCellValue("Reseller");
					row.createCell(1).setCellValue("Merchants");
					row.createCell(2).setCellValue("Virtual Account Number");
					row.createCell(3).setCellValue("Virtual Account Flag");
				} else {
					row.createCell(0).setCellValue("Merchants");
					row.createCell(1).setCellValue("Virtual Account Number");
					row.createCell(2).setCellValue("Virtual Account Flag");
				}

				for (ECollectionObject VAMerchant : VAMerchantsList) {
					row = sheet.createRow(rownum++);
					Object[] objArr = VAMerchant.myCsvMethodDownloadVAListByAdminView(resellerFlag,
							superMerchant);

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
			} else if (StringUtils.isNotBlank(reportMerchant) && !reportMerchant.equalsIgnoreCase("ALL")
					&& StringUtils.isNotBlank(subMerchant)) {

				for (ECollectionObject VAMerchant : VAMerchantsList) {
					if (StringUtils.isNotBlank(VAMerchant.getReseller()) && !VAMerchant.getReseller().equals("NA")) {
						resellerFlag = true;
						break;
					}
				}
				superMerchant = true;
				if (resellerFlag) {
					row.createCell(0).setCellValue("Reseller");
					row.createCell(1).setCellValue("Merchants");
					row.createCell(2).setCellValue("Sub-Merchants");
					row.createCell(3).setCellValue("Virtual Account Number");
					row.createCell(4).setCellValue("Virtual Account Flag");
				} else {
					row.createCell(0).setCellValue("Merchants");
					row.createCell(1).setCellValue("Sub-Merchants");
					row.createCell(2).setCellValue("Virtual Account Number");
					row.createCell(3).setCellValue("Virtual Account Flag");
				}

				for (ECollectionObject VAMerchant : VAMerchantsList) {
					row = sheet.createRow(rownum++);
					Object[] objArr = VAMerchant.myCsvMethodDownloadVAListByAdminView(resellerFlag,
							superMerchant);

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
			}
		} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant()) {

			if (StringUtils.isNotBlank(sessionUser.getResellerId())) {
				resellerFlag = true;
			}
			superMerchant = true;

			if (resellerFlag) {
				row.createCell(0).setCellValue("Reseller");
				row.createCell(1).setCellValue("Merchants");
				row.createCell(2).setCellValue("Sub-Merchants");
				row.createCell(3).setCellValue("Virtual Account Number");
				row.createCell(4).setCellValue("Virtual Account Flag");
			} else {
				row.createCell(0).setCellValue("Merchants");
				row.createCell(1).setCellValue("Sub-Merchants");
				row.createCell(2).setCellValue("Virtual Account Number");
				row.createCell(3).setCellValue("Virtual Account Flag");
			}

			for (ECollectionObject VAMerchant : VAMerchantsList) {
				row = sheet.createRow(rownum++);
				Object[] objArr = VAMerchant.myCsvMethodDownloadVAListByAdminView(resellerFlag,
						superMerchant);

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}
		}
		try {
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "VA_Merchants_" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for VA List");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return SUCCESS;
	}
	
	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					setReportType("eCollection");
					execute();
				} catch (Exception e) {
					setGenerateReport(false);
					logger.error("Exception while generating ECollection Report ", e);
				}
			}
		};

		propertiesManager.executorImpl(runnable);

		return SUCCESS;

	}
	
	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public String getReportMerchant() {
		return reportMerchant;
	}

	public void setReportMerchant(String reportMerchant) {
		this.reportMerchant = reportMerchant;
	}
	
	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}

	public String getReportPaymentMode() {
		return reportPaymentMode;
	}

	public void setReportPaymentMode(String reportPaymentMode) {
		this.reportPaymentMode = reportPaymentMode;
	}

	public String getReportDateFrom() {
		return reportDateFrom;
	}

	public void setReportDateFrom(String reportDateFrom) {
		this.reportDateFrom = reportDateFrom;
	}

	public String getReportDateTo() {
		return reportDateTo;
	}

	public void setReportDateTo(String reportDateTo) {
		this.reportDateTo = reportDateTo;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}
	
	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}
	public String getReportTxnType() {
		return reportTxnType;
	}
	public void setReportTxnType(String reportTxnType) {
		this.reportTxnType = reportTxnType;
	}
	
	public String getVirtualAccountNo() {
		return virtualAccountNo;
	}
	
	public void setVirtualAccountNo(String virtualAccountNo) {
		this.virtualAccountNo = virtualAccountNo;
	}


	public boolean isGenerateReport() {
		return generateReport;
	}


	public void setGenerateReport(boolean generateReport) {
		this.generateReport = generateReport;
	}


	public String getReportType() {
		return reportType;
	}


	public void setReportType(String reportType) {
		this.reportType = reportType;
	}


	public String getSubUserPayId() {
		return subUserPayId;
	}


	public void setSubUserPayId(String subUserPayId) {
		this.subUserPayId = subUserPayId;
	}


	public User getSubuser() {
		return subuser;
	}


	public void setSubuser(User subuser) {
		this.subuser = subuser;
	}

}
