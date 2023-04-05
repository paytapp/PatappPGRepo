package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.TransactionReconSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.mongoReports.ReconTxnReports;

public class TransactionReconSearchAction extends AbstractSecureAction {

	@Autowired
	private ReconTxnReports reconTxnReports;

	private static Logger logger = LoggerFactory.getLogger(TransactionReconSearchAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;

	private String reservationId;
	private String bankTxnId;
	private String sid;
	private String acquirer;
	private String transactionType;
	private String status;
	private String dateFrom;
	private String dateTo;
	private String operationFlag;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private InputStream fileInputStream;
	private String filename;
	private String merchant;
	private String bank;
	
	private List<TransactionReconSearch> aaData;
	private User sessionUser = new User();

	public String execute() {

		logger.info("Inside TransactionReconSearchAction, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				totalCount = reconTxnReports.searchPaymentCount(reservationId, bankTxnId, sid, acquirer,
						getTransactionType(), status, getDateFrom(), getDateTo(), operationFlag);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = reconTxnReports.searchPayment(reservationId, bankTxnId, sid, acquirer, getTransactionType(),
						status, getDateFrom(), getDateTo(), start, length, operationFlag);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception TransactionReconSearchAction ", exception);
			return ERROR;
		}
		return SUCCESS;

	}

	public String download() {

		try {

			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

			List<TransactionReconSearch> reconList = new ArrayList<TransactionReconSearch>();
			reconList = reconTxnReports.downloadRecon(reservationId, bankTxnId, sid, acquirer, getTransactionType(),
					status, getDateFrom(), getDateTo());
			recordsFiltered = recordsTotal;

			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Transaction_Report");
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Reservation Id");
			row.createCell(2).setCellValue("Bank Transaction Id");
			row.createCell(3).setCellValue("SID");
			row.createCell(4).setCellValue("Amount");
			row.createCell(5).setCellValue("TxnType");
			row.createCell(6).setCellValue("Status");
			row.createCell(7).setCellValue("Acquirer");
			row.createCell(8).setCellValue("Capture Date");
			row.createCell(9).setCellValue("Settlement Date");
			row.createCell(10).setCellValue("Settled Flag");
			row.createCell(11).setCellValue("Post Settled Flag");

			for (TransactionReconSearch transactionSearch : reconList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
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

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Transaction_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for download recon txn");

			return SUCCESS;
		}

		catch (Exception e) {
			logger.error("Exception in report download for recon", e);
			return SUCCESS;
		}

	}

	public String downloadExceptions() {

		try {

			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

			List<TransactionReconSearch> reconList = new ArrayList<TransactionReconSearch>();
			reconList = reconTxnReports.downloadRecon(reservationId, bankTxnId, sid, acquirer, getTransactionType(),
					status, getDateFrom(), getDateTo());
			recordsFiltered = recordsTotal;

			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Transaction_Report");
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Reservation Id");
			row.createCell(2).setCellValue("Bank Transaction Id");
			row.createCell(3).setCellValue("SID");
			row.createCell(4).setCellValue("Amount");
			row.createCell(5).setCellValue("TxnType");
			row.createCell(6).setCellValue("Status");
			row.createCell(7).setCellValue("Acquirer");
			row.createCell(8).setCellValue("Capture Date");
			row.createCell(9).setCellValue("Settlement Date");
			row.createCell(10).setCellValue("Settled Flag");
			row.createCell(11).setCellValue("Post Settled Flag");

			for (TransactionReconSearch transactionSearch : reconList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
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

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Transaction_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for download recon txn");

			return SUCCESS;
		}

		catch (Exception e) {
			logger.error("Exception in report download for recon", e);
			return SUCCESS;
		}

	}

	public String getExceptions() {

		logger.info("Inside get exceptions, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				totalCount = reconTxnReports.searchExceptionsCount(reservationId, bankTxnId, sid, acquirer,
						getTransactionType(), getDateFrom(), getDateTo());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = reconTxnReports.searchExceptions(reservationId, bankTxnId, sid, acquirer, getTransactionType(),
						getDateFrom(), getDateTo(), start, length);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}

	public String getAccountStatementSummary() {

		logger.info("Inside get exceptions, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummary(  acquirer, 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getAccountStatementSummaryIPAY() {

		acquirer = "IPAY";
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummaryIPAY(  bank,merchant, 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}

	public String getAccountStatementSummaryBOB() {

		acquirer = "IPAY";
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummaryBOB(  "BOB", 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	
	public String getAccountStatementSummaryAllahabad() {

		acquirer = "ALLAHABAD BANK";
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummaryAllahabad(  "ALLAHABAD BANK", 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	
	public String getAccountStatementSummaryCorporation() {

		acquirer = "CORPORATION BANK";
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummaryCorporation(  "CORPORATION BANK", 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getAccountStatementSummaryMaharashtra() {

		acquirer = "BANK OF MAHARASHTRA";
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummaryMaharashtra(  "BANK OF MAHARASHTRA", 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	
	public String getAccountStatementSummaryKarur() {

		acquirer = "KARUR BANK";
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchAccountSummaryKarur(  "KARUR BANK", 	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get exceptions  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	
	public String getAccountSettlementSummary() {

		logger.info("Inside get getAccountSettlementSummary, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchSettleSummary(  acquirer, getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get getAccountSettlementSummary  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	
	public String getAccountSettlementSummaryBob() {

		logger.info("Inside get getAccountSettlementSummary, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchSettleSummaryBob(  "BOB", getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get getAccountSettlementSummary  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getAccountSettlementSummaryAllahabad() {

		logger.info("Inside get getAccountSettlementSummaryAllahabad, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchSettleSummaryAllahabad(  "ALLAHABAD BANK", getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get getAccountSettlementSummary  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	
	public String getAccountSettlementSummaryCorporation() {

		logger.info("Inside get getAccountSettlementSummaryCorporation, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchSettleSummaryCorporation(  "CORPORATION BANK", getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get getAccountSettlementSummary  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getAccountSettlementSummaryMaharashtra() {

		logger.info("Inside get getAccountSettlementSummaryMaharashtra, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchSettleSummaryMaharashtra(  "BANK OF MAHARASHTRA", getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get getAccountSettlementSummaryMaharashtra  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getBobReversals() {

		logger.info("Inside get getAccountSettlementSummary, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.bobReversals(  "BOB", getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception get getAccountSettlementSummary  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getAccountSettlementSummaryKarur() {

		logger.info("Inside get getAccountSettlementSummaryKarur, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (!dateFrom.contains("00:00:00")) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		}

		try {

			if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
				
				aaData = reconTxnReports.searchSettleSummaryKarur(  "KARUR BANK", getTransactionType(),	getDateFrom(), getDateTo() );
				totalCount = aaData.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception in getAccountSettlementSummaryKarur  ", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	@SuppressWarnings("deprecation")
	public String getAgentTxn() {

		logger.info("Inside get getAgentTxn, execute()");
		/*
		 * int totalCount; sessionUser = (User)
		 * sessionMap.get(Constants.USER.getValue());
		 * 
		 * try {
		 * 
		 * if (sessionUser.getUserType().equals(UserType.RECONUSER)) {
		 * 
		 * if (reservationId.isEmpty() && bankTxnId.isEmpty()) { totalCount = 0;
		 * BigInteger bigInt = BigInteger.valueOf(totalCount); setRecordsTotal(bigInt);
		 * recordsFiltered = recordsTotal; return SUCCESS; }
		 * 
		 * aaData = reconTxnReports.searchAgent(reservationId,bankTxnId); totalCount =
		 * aaData.size(); BigInteger bigInt = BigInteger.valueOf(totalCount);
		 * setRecordsTotal(bigInt); recordsFiltered = recordsTotal; }
		 * 
		 * } catch (Exception exception) { logger.error("Exception   getAgentTxn  ",
		 * exception); return ERROR; }
		 */
		return SUCCESS;

	}
	

	public void validate() {

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
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

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getReservationId() {
		return reservationId;
	}

	public void setReservationId(String reservationId) {
		this.reservationId = reservationId;
	}

	public String getBankTxnId() {
		return bankTxnId;
	}

	public void setBankTxnId(String bankTxnId) {
		this.bankTxnId = bankTxnId;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public List<TransactionReconSearch> getAaData() {
		return aaData;
	}

	public void setAaData(List<TransactionReconSearch> aaData) {
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

	public String getOperationFlag() {
		return operationFlag;
	}

	public void setOperationFlag(String operationFlag) {
		this.operationFlag = operationFlag;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

}
