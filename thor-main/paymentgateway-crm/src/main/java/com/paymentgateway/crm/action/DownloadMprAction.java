package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentSearchDownloadObject;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadMprAction extends AbstractSecureAction {

	private static final long serialVersionUID = -8129011751068997117L;
	private static Logger logger = LoggerFactory.getLogger(DownloadTransactionsReportAction.class.getName());
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String subMerchantPayId;
	private String customerEmail;
	private String merchantPayId;
	private String paymentType;
	private String transactionType;
	private String reportType;
	private String status;
	private String acquirer;
	private String partSettleFlag;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	private String paymentsRegion;
	private String cardHolderType;
	private String deliveryStatus;
	private User merchant = new User();
	private Set<String> orderIdSet = null;
	public String subUserPayId;
	private String transactionFlag;
	private String deltaFlag;
	private String orderId;
	private String transactionId;
	private String autoRefund;
	private boolean generateReport;
	private User subuser;

	List<Merchants> resellerMerchants = new ArrayList<Merchants>();

	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;

	@Autowired
	private DownloadReportGenerateService reportGenerateService;

	@SuppressWarnings({ "unchecked", "static-access" })
	public String execute() {

		if (acquirer == null || acquirer.isEmpty()) {
			acquirer = "ALL";
		}

		if (paymentsRegion == null || paymentsRegion.isEmpty()) {
			paymentsRegion = "ALL";

		}

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String subMerchPayId = "";
	
		if (StringUtils.isNotEmpty(merchantPayId) && !merchantPayId.equalsIgnoreCase("All"))
			merchantPayId = userDao.getPayIdByEmailId(merchantPayId);

		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
			subMerchPayId = userDao.getPayIdByEmailId(subMerchantPayId);
		}

		if (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All")) {
			subMerchPayId = subMerchantPayId;
		}

		if (getReportType().equalsIgnoreCase("saleCaptured")) {
			transactionType = TransactionType.SALE.getName();
			status = StatusType.CAPTURED.getName();
		} else if (getReportType().equalsIgnoreCase("refundCaptured")) {
			transactionType = TransactionType.REFUND.getName();
			status = StatusType.CAPTURED.getName();
		}

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		transactionList = txnReports.searchMprPaymentForDownload(merchantPayId, subMerchPayId, customerEmail, paymentType,
				"Captured", currency, transactionType, dateFrom, dateTo, paymentsRegion, acquirer,
				partSettleFlag, orderIdSet, transactionFlag, deltaFlag, transactionId, orderId, autoRefund);
		BigDecimal st = null;

		logger.info("List generated successfully for searchMprPaymentForDownload");

		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		Sheet sheet = wb.createSheet("MPR Report");

		row = sheet.createRow(0);

		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {

			row.createCell(0).setCellValue("MERCHANT CODE");
			row.createCell(1).setCellValue("TERMINAL NUMBER");
			row.createCell(2).setCellValue("REC FMT");
			row.createCell(3).setCellValue("BAT NBR");
			row.createCell(4).setCellValue("CARD TYPE");
			row.createCell(5).setCellValue("CARD NUMBER");
			row.createCell(6).setCellValue("TRANS DATE");
			row.createCell(7).setCellValue("SETTLE DATE");
			row.createCell(8).setCellValue("APPROV CODE");
			row.createCell(9).setCellValue("INTNL AMT");
			row.createCell(10).setCellValue("DOMESTIC AMT");
			row.createCell(11).setCellValue("TRAN_ID");
			row.createCell(12).setCellValue("UPVALUE");
			row.createCell(13).setCellValue("MERCHANT_TRACKID");
			row.createCell(14).setCellValue("MSF");
			row.createCell(15).setCellValue("SERV TAX");
			row.createCell(16).setCellValue("SB Cess");
			row.createCell(17).setCellValue("KK Cess");
			row.createCell(18).setCellValue("CGST AMT");
			row.createCell(19).setCellValue("SGST AMT");
			row.createCell(20).setCellValue("IGST AMT");
			row.createCell(21).setCellValue("UTGST AMT");
			row.createCell(22).setCellValue("ACQUIRER_TDR_SC");
			row.createCell(23).setCellValue("ACQUIRER_GST");
			row.createCell(24).setCellValue("Net Amount");
			row.createCell(25).setCellValue("DEBITCREDIT_TYPE");
			row.createCell(26).setCellValue("UDF1");
			row.createCell(27).setCellValue("UDF2");
			row.createCell(28).setCellValue("UDF3");
			row.createCell(29).setCellValue("UDF4");
			row.createCell(30).setCellValue("UDF5");
			row.createCell(31).setCellValue("SEQUENCE NUMBER");
			row.createCell(32).setCellValue("ARN NO");
			row.createCell(33).setCellValue("INVOICE_NUMBER	");
			row.createCell(34).setCellValue("GSTN_TRANSACTION_ID");

			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date date = new Date();
			String Currentdate = formatter.format(date);

			for (TransactionSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadMprPaymentsReport(Currentdate);

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

			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String FILE_EXTENSION = ".csv";
				String dateForFile = dateFormat.format(new Date()).split(" ")[0];

				if (isGenerateReport()) {
					String fileLocation = "";
					if (sessionUser.getUserType().equals(UserType.ADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateForFile
								+ "/" + sessionUser.getPayId() + "/";
					} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateForFile
								+ "/" + sessionUser.getPayId() + "/";
					}
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					
					String[] dateArr = dateForFile.split("-");
					dateForFile = dateArr[2]+dateArr[1]+dateArr[0];
					
					if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
						filename = "MPR_" + acquirer + "_" + dateForFile + "_V1" + FILE_EXTENSION;
					} else {
						filename = "MPR_" + dateForFile + "_V1" + FILE_EXTENSION;
					}

					File file = new File(fileLocation, filename);
					FileOutputStream out = new FileOutputStream(file);
					wb.write(out);
					out.flush();
					out.close();
					wb.dispose();
					logger.info(filename + " File generated successfully");
					reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchantPayId,
							subMerchPayId, "", dateFrom, dateTo, sessionUser.getPayId());
				} else {
					
					String[] dateArr = dateForFile.split("-");
					dateForFile = dateArr[2]+dateArr[1]+dateArr[0];
					if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
						filename = "MPR_" + acquirer + "_" + dateForFile + "_V1" + FILE_EXTENSION;
					} else {
						filename = "MPR_" + dateForFile + "_V1" + FILE_EXTENSION;
					}
					File file = new File(filename);

					// this Writes the workbook
					FileOutputStream out = new FileOutputStream(file);
					wb.write(out);
					out.flush();
					out.close();
					wb.dispose();
					fileInputStream = new FileInputStream(file);
					addActionMessage(filename + " written successfully on disk.");
					logger.info("File generated successfully for MPR");
				}
			} catch (Exception exception) {
				setGenerateReport(false);
				logger.error("Exception", exception);
			}
		}
		
		return SUCCESS;
	}
	
	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setGenerateReport(true);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						execute();
					} catch (Exception e) {
						setGenerateReport(false);
						logger.error("Exception while generating MPR Report ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);
		}
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getPartSettleFlag() {
		return partSettleFlag;
	}

	public void setPartSettleFlag(String partSettleFlag) {
		this.partSettleFlag = partSettleFlag;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}

	public String getSubUserPayId() {
		return subUserPayId;
	}

	public void setSubUserPayId(String subUserPayId) {
		this.subUserPayId = subUserPayId;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}

	public String getDeltaFlag() {
		return deltaFlag;
	}

	public void setDeltaFlag(String deltaFlag) {
		this.deltaFlag = deltaFlag;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(String autoRefund) {
		this.autoRefund = autoRefund;
	}

	public boolean isGenerateReport() {
		return generateReport;
	}

	public void setGenerateReport(boolean generateReport) {
		this.generateReport = generateReport;
	}

	public User getSubuser() {
		return subuser;
	}

	public void setSubuser(User subuser) {
		this.subuser = subuser;
	}

}
