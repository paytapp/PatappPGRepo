package com.paymentgateway.crm.action;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MerchantInitiatedDirectDao;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;

/**
 * @author Pooja Pancholi
 *
 */

public class MerchantInitiatedLedgerReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2519554337725531304L;
	private static Logger logger = LoggerFactory.getLogger(MerchantInitiatedLedgerReportAction.class.getName());

	@Autowired
	private MerchantInitiatedDirectDao merchantInitiatedDirectDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;

	private String payId;
	private String subMerchantPayId;
	private String dateTo;
	private String dateFrom;
	private String dateToGraph;
	private String dateFromGraph;
	private String openingBalance;
	private String closingBalance;
	private String amount;
	private InputStream fileInputStream;
	private String filename;
	private BigDecimal currentData;
	private Map<String, String> respMap;
	private List<ImpsDownloadObject> aaData;
	private List<ImpsDownloadObject> aaGraphData;
	private User sessionUser = new User();
	private String response;
    private String responseMsg;
    private String remarks;
    private String acquirerName;

	public String execute() {

		logger.info("Inside MerchantInitiatedLedgerReportAction, execute()");
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside merchantInitiatedDirectReport()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		try {
			String payIdReport = "";
			String subMerchantPayIdReport = "";

			User userByPayId = null;
			if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User user = null;
				if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
					user = userDao.findPayId(sessionUser.getParentPayId());
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						payIdReport = user.getSuperMerchantId();
						subMerchantPayIdReport = user.getPayId();
					} else {
						if (StringUtils.isNotBlank(subMerchantPayId)) {
							payIdReport = payId;
							subMerchantPayIdReport = subMerchantPayId;
						} else {
							payIdReport = payId;
						}
					}
				}
			} else if (sessionUser.isSuperMerchant() == false
					&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				payIdReport = sessionUser.getSuperMerchantId();
				subMerchantPayIdReport = sessionUser.getPayId();
			} else if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				userByPayId = userDao.findPayId(subMerchantPayId);
				payIdReport = userByPayId.getSuperMerchantId();
				subMerchantPayIdReport = subMerchantPayId;

			} else {
				payIdReport = payId;
				subMerchantPayIdReport = subMerchantPayId;
			}
			boolean downloadFlag = false;
			setAaData(merchantInitiatedDirectDao.viewLedgerReportData(dateFrom, dateTo, payIdReport,
					subMerchantPayIdReport, sessionUser, downloadFlag, acquirerName));

			return SUCCESS;

		} catch (Exception e) {
			logger.error("exception " , e);
		}

		return SUCCESS;
	}

	public String currentBalance() {
		logger.info("Inside MerchantInitiatedLedgerReportAction, currentBalance()");

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		Map<String, String> respMap;
		try {
			String payIdReport = "";
			String subMerchantPayIdReport = "";

			User userByPayId = null;
			if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User user = null;
				if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
					user = userDao.findPayId(sessionUser.getParentPayId());
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						payIdReport = user.getSuperMerchantId();
						subMerchantPayIdReport = user.getPayId();
					} else {
						if (StringUtils.isNotBlank(subMerchantPayId)) {
							payIdReport = user.getPayId();
							subMerchantPayIdReport = subMerchantPayId;
						} else {
							payIdReport = user.getPayId();
						}
					}
				}
			} else if(sessionUser.getUserType().equals(UserType.MERCHANT)){
				if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					payIdReport = sessionUser.getSuperMerchantId();
					subMerchantPayIdReport = sessionUser.getPayId();
				}else
				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
					userByPayId = userDao.findPayId(subMerchantPayId);
					payIdReport = userByPayId.getSuperMerchantId();
					subMerchantPayIdReport = subMerchantPayId;

				}else {
					payIdReport = sessionUser.getPayId();
					subMerchantPayIdReport = subMerchantPayId;
				}
			}else { 
				if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					payIdReport = sessionUser.getSuperMerchantId();
					subMerchantPayIdReport = sessionUser.getPayId();
				}else
				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				userByPayId = userDao.findPayId(subMerchantPayId);
				payIdReport = userByPayId.getSuperMerchantId();
				subMerchantPayIdReport = subMerchantPayId;

			}else {
				payIdReport = payId;
				subMerchantPayIdReport = subMerchantPayId;
			}
			}

			setRespMap(merchantInitiatedDirectDao.getCurrentBalanceData(dateFrom, dateTo, payIdReport,
					subMerchantPayIdReport, sessionUser));

			return SUCCESS;

		} catch (Exception e) {
			logger.error("exception " , e);
		}

		return SUCCESS;
	}

	// Report Download
	public String merchantInitiatedDirectLedgerDownloadReport() {
		logger.info("Inside MerchantInitiatedLedgerReportAction, merchantInitiatedDirectLedgerDownloadReport()");
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside merchantInitiatedDirectLedgerDownloadReport()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ImpsDownloadObject> merchantInitiatedDirectList = new ArrayList<ImpsDownloadObject>();
		String payIdReport = "";
		String subMerchantPayIdReport = "";

		User userByPayId = null;
		if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
			User user = null;
			if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
				user = userDao.findPayId(sessionUser.getParentPayId());
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					payIdReport = user.getSuperMerchantId();
					subMerchantPayIdReport = user.getPayId();
				} else {

					if (user.isSuperMerchant() == true && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						if (StringUtils.isNotBlank(subMerchantPayId)) {
							payIdReport = payId;
							subMerchantPayIdReport = subMerchantPayId;
						} else {
							payIdReport = payId;
							subMerchantPayIdReport = "ALL";
						}
					} else {
						payIdReport = payId;
					}
				}
			}
		} else if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			payIdReport = sessionUser.getSuperMerchantId();
			subMerchantPayIdReport = sessionUser.getPayId();
		} else if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			userByPayId = userDao.findPayId(subMerchantPayId);
			payIdReport = userByPayId.getSuperMerchantId();
			subMerchantPayIdReport = subMerchantPayId;

		} else {
			payIdReport = payId;
			subMerchantPayIdReport = subMerchantPayId;
		}

		boolean downloadFlag = true;
		merchantInitiatedDirectList = merchantInitiatedDirectDao.viewLedgerReportData(dateFrom, dateTo, payIdReport,
				subMerchantPayIdReport, sessionUser, downloadFlag, acquirerName);
		BigDecimal st = null;

		logger.info("List generated successfully for PayoutLedgerDownloadReport");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Payout Ledger Report");
		row = sheet.createRow(0);

		if (StringUtils.isNotBlank(subMerchantPayIdReport) || ((payIdReport.equalsIgnoreCase("ALL")))) {
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub Merchant Name");
			row.createCell(2).setCellValue("Date");
			row.createCell(3).setCellValue("Opening Balance");
			row.createCell(4).setCellValue("Total Credit");
			row.createCell(5).setCellValue("Total Debit");
			row.createCell(6).setCellValue("Closing Balance");

			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				// transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadMerchantInitiateDirectLedgerReportSub();

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
			row.createCell(1).setCellValue("Date");
			row.createCell(2).setCellValue("Opening Balance");
			row.createCell(3).setCellValue("Total Credit");
			row.createCell(4).setCellValue("Total Debit");
			row.createCell(5).setCellValue("Closing Balance");

			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				// transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadMerchantInitiateDirectLedgerReportMerch();

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
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Payout_ledger_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for PayoutLedgerDownloadReport");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return SUCCESS;
	}

	// Report Download
	public String merchantInitiatedDirectLedgerTrailReportDownload() {
		// setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		// setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside MerchantInitiatedLedgerReportAction, merchantInitiatedDirectLedgerTrailReportDownload()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ImpsDownloadObject> merchantInitiatedDirectList = new ArrayList<ImpsDownloadObject>();
		String payIdReport = "";
		String subMerchantPayIdReport = "";

		User userByPayId = null;
		if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			payIdReport = sessionUser.getSuperMerchantId();
			subMerchantPayIdReport = sessionUser.getPayId();
		} else {
			payIdReport = payId;
			subMerchantPayIdReport = subMerchantPayId;
		}

		merchantInitiatedDirectList = merchantInitiatedDirectDao.downloadLedgerReportData(dateFrom, dateTo, payIdReport,
				subMerchantPayIdReport, sessionUser, closingBalance, openingBalance, acquirerName);
		BigDecimal st = null;

		logger.info("List generated successfully for Payout Summary Report");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Payout Summary Report");
		row = sheet.createRow(0);

		if (!(subMerchantPayIdReport).equalsIgnoreCase("null") && StringUtils.isNotBlank(payIdReport)) {
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub Merchant Name");
			row.createCell(2).setCellValue("UTR");
			row.createCell(3).setCellValue("Date");
			row.createCell(4).setCellValue("Opening Balance");
			row.createCell(5).setCellValue("Credit");
			row.createCell(6).setCellValue("Topup");
			row.createCell(7).setCellValue("Debit");
			row.createCell(8).setCellValue("Current Balance");

			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				// transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadMerchantInitiateDirectLedgerSub();

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
			row.createCell(1).setCellValue("UTR");
			row.createCell(2).setCellValue("Date");
			row.createCell(3).setCellValue("Opening Balance");
			row.createCell(4).setCellValue("Credit");
			row.createCell(5).setCellValue("Topup");
			row.createCell(6).setCellValue("Debit");
			row.createCell(7).setCellValue("Current Balance");

			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				// transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadMerchantInitiateDirectLedgerMerch();

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
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Payout_Summary_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for Payout Summary Report");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return SUCCESS;
	}

	// Graph
	public String graphMerchantInitiatedDirectLedger() {
		
		logger.info("Inside MerchantInitiatedLedgerReportAction, graphMerchantInitiatedDirectLedger()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			String payIdReport = "";
			String subMerchantPayIdReport = "";

			if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User user = null;
				if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
					user = userDao.findPayId(sessionUser.getParentPayId());
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						payIdReport = user.getSuperMerchantId();
						subMerchantPayIdReport = user.getPayId();
					} else {
						if (StringUtils.isNotBlank(subMerchantPayId)) {
							payIdReport = payId;
							subMerchantPayIdReport = subMerchantPayId;
						} else {
							payIdReport = sessionUser.getParentPayId();
						}
					}
				}
			} else if (sessionUser.isSuperMerchant() == false
					&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				payIdReport = sessionUser.getSuperMerchantId();
				subMerchantPayIdReport = sessionUser.getPayId();
			} else {

				if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					payIdReport = sessionUser.getPayId();
				}
			}

			setAaGraphData(merchantInitiatedDirectDao.viewGrahData(dateFromGraph, dateToGraph, payIdReport,
					subMerchantPayIdReport, sessionUser));

			return SUCCESS;

		} catch (Exception e) {
			logger.error("exception " , e);
		}

		return SUCCESS;
	}

	public String payoutTopUp() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			
			String payoutAcqName = payoutAcquirerMappingDao.findPayoutAcquirerNameByPayId(payId,subMerchantPayId);
			
			if(StringUtils.isNotBlank(payoutAcqName)){
				merchantInitiatedDirectDao.addTopup(payId, subMerchantPayId, amount, sessionUser, remarks);
				
				setResponse("success");
	            setResponseMsg("Success");
				return SUCCESS;
			}
			setResponse("failed");
            setResponseMsg("No Acquirer Mapped With Merchant");
			return SUCCESS;
		} catch (Exception e) {
			logger.error("exception while payout topup " , e);
			setResponse("failed");
            setResponseMsg("failed");
			return SUCCESS;
		}

	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public BigDecimal getCurrentData() {
		return currentData;
	}

	public void setCurrentData(BigDecimal currentData) {
		this.currentData = currentData;
	}

	public Map<String, String> getRespMap() {
		return respMap;
	}

	public void setRespMap(Map<String, String> respMap) {
		this.respMap = respMap;
	}

	public List<ImpsDownloadObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ImpsDownloadObject> aaData) {
		this.aaData = aaData;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(String openingBalance) {
		this.openingBalance = openingBalance;
	}

	public String getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(String closingBalance) {
		this.closingBalance = closingBalance;
	}

	public String getDateToGraph() {
		return dateToGraph;
	}

	public void setDateToGraph(String dateToGraph) {
		this.dateToGraph = dateToGraph;
	}

	public String getDateFromGraph() {
		return dateFromGraph;
	}

	public void setDateFromGraph(String dateFromGraph) {
		this.dateFromGraph = dateFromGraph;
	}

	public List<ImpsDownloadObject> getAaGraphData() {
		return aaGraphData;
	}

	public void setAaGraphData(List<ImpsDownloadObject> aaGraphData) {
		this.aaGraphData = aaGraphData;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
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

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	

}
