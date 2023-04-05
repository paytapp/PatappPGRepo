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
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargingDetailsFactory;
import com.paymentgateway.commons.user.MISReportObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;

public class XssFormatFileDownload extends AbstractSecureAction {

	/**
	 * 
	 */
	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = -1471532495305994799L;
	private static Logger logger = LoggerFactory.getLogger(XssFormatFileDownload.class.getName());
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String name;
	private String merchant;
	private String subMerchant;
	private String partSettle;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	private boolean generateReport;
	private String reportType;
	public String subUserPayId;
	private String transactionFlag;
	private User subuser = new User();
	
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	ChargingDetailsFactory cdf;

	@Autowired
	private SettlementReportQuery srq;
	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;

	@Autowired
	private PropertiesManager propertiesManager;

	@SuppressWarnings("resource")
	public String execute() {
		if (name.isEmpty()) {
			name = "ALL";

		}
		
		
		List<MISReportObject> transactionList = new ArrayList<MISReportObject>();
		boolean subMerchantFlag = false;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String merchantPayId = "";
		String subMerchantPayId = "";
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			
			/*if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			
			} else {*/
				
			if (merchant.equalsIgnoreCase("All")) {
				List<Merchants> al = userDao.getActiveMerchant();
				for (Merchants merchant : al) {
					if (merchant.getIsSuperMerchant()) {
						subMerchantFlag = true;
						break;
					}
				}
			}
			merchantPayId = merchant;
			
			if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId = subMerchant;
				subMerchantFlag = true;
			} else {
				subMerchantPayId = subMerchant;
			}
			subMerchantFlag = true;			
			// }
			transactionList.clear();
			transactionList = srq.settlementReportDownload(merchantPayId, subMerchantPayId, name, currency, dateFrom,
					dateTo, partSettle, transactionFlag);
			
		logger.info("List generated successfully for MIS Report");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("MIS Report");
		row = sheet.createRow(0);
		
		if(StringUtils.isNotBlank(subMerchantPayId) || subMerchantFlag == true) {
			
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Pay ID");
			row.createCell(3).setCellValue("Sub Merchant Name");
			row.createCell(4).setCellValue("Sub Merchant Pay ID");
			row.createCell(5).setCellValue("Transaction ID");
			row.createCell(6).setCellValue("Order_ID");
			row.createCell(7).setCellValue("Transaction Date");
			row.createCell(8).setCellValue("Settlement Date");
			row.createCell(9).setCellValue("Transaction type(Sale/Refund)");
			row.createCell(10).setCellValue("Gross Transaction Amt");
			row.createCell(11).setCellValue("Total Aggregator Commission Amt Payable(Including GST)");
			row.createCell(12).setCellValue("Total Aggregator SUF (Incl GsT)");
			row.createCell(13).setCellValue("Total Acquirer Commission Amt Payable(Including GST)");
			row.createCell(14).setCellValue("Total Amt Payable to Merchant A/c");
			row.createCell(15).setCellValue("Total Payout from Nodal Account");
			row.createCell(16).setCellValue("BankName_Receive_Funds");
			row.createCell(17).setCellValue("Nodal a/c no");
			row.createCell(18).setCellValue("Aggregator name");
			row.createCell(19).setCellValue("Acquirer Mode");
			row.createCell(20).setCellValue("Refund Flag");
			row.createCell(21).setCellValue("Payments Type");
			row.createCell(22).setCellValue("MOP Type");
			row.createCell(23).setCellValue("CardHolder Type");
			row.createCell(24).setCellValue("Payment Region");
			row.createCell(25).setCellValue("Refund Order ID");
			row.createCell(26).setCellValue("Transaction Flag");

			for (MISReportObject transactionSearch : transactionList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodForSuperMerchant();

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
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Pay ID");
			row.createCell(3).setCellValue("Transaction ID");
			row.createCell(4).setCellValue("Order_ID");
			row.createCell(5).setCellValue("Transaction Date");
			row.createCell(6).setCellValue("Settlement Date");
			row.createCell(7).setCellValue("Transaction type(Sale/Refund)");
			row.createCell(8).setCellValue("Gross Transaction Amt");
			row.createCell(9).setCellValue("Total Aggregator Commision Amt Payable(Including GST)");
			row.createCell(10).setCellValue("Total Aggregator SUF (Incl GsT)");
			row.createCell(11).setCellValue("Total Acquirer Commision Amt Payable(Including GST)");
			row.createCell(12).setCellValue("Total Amt Payable to Merchant A/c");
			row.createCell(13).setCellValue("Total Payout from Nodal Account");
			row.createCell(14).setCellValue("BankName_Receive_Funds");
			row.createCell(15).setCellValue("Nodal a/c no");
			row.createCell(16).setCellValue("Aggregator name");
			//row.createCell(16).setCellValue("Acquirer Name");
			row.createCell(17).setCellValue("Acquirer Mode");
			row.createCell(18).setCellValue("Refund Flag");
			row.createCell(19).setCellValue("Payments Type");
			row.createCell(20).setCellValue("MOP Type");
			row.createCell(21).setCellValue("CardHolder Type");
			row.createCell(22).setCellValue("Payment Region");
			row.createCell(23).setCellValue("Refund Order ID");
			row.createCell(24).setCellValue("Transaction Flag");

			for (MISReportObject transactionSearch : transactionList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethod();

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
								
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						}
					} else {
						filename = getReportType() + "_Report_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
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
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}

						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
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
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + subMerchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + subMerchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
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
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchantPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
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
					filename = getReportType() + "_Report_" + sessionUser.getPayId() + "_"
							+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
							+ FILE_EXTENSION;
				}
				
				File file = new File(fileLocation, filename);
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info(filename + " File generated successfully");
				reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchantPayId,
						subMerchantPayId,"", dateFrom, dateTo, sessionUser.getPayId());
			} else {
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "MIS_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			}
		} catch (Exception exception) {
			setGenerateReport(false);
			logger.error("Exception", exception);
		}

		return SUCCESS;

	} catch (Exception e) {
		setGenerateReport(false);
		logger.error("Exception1 ", e);
		return SUCCESS;
	}
}

	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					setReportType("MIS");
					execute();
				} catch (Exception e) {
					setGenerateReport(false);
					logger.error("Exception while generating MIS Report ", e);
				}
			}
		};

		propertiesManager.executorImpl(runnable);

		return SUCCESS;

	}
	
	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMerchant() {
		return merchant;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getPartSettle() {
		return partSettle;
	}

	public void setPartSettle(String partSettle) {
		this.partSettle = partSettle;
	}
	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
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

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}

	
}
