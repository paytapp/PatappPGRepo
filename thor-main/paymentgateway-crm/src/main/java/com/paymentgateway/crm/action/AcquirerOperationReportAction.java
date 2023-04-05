package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.AcquirerOperationReportService;

public class AcquirerOperationReportAction extends AbstractSecureAction {

	/**
	 * Alam
	 */
	private static final long serialVersionUID = 2220553826076179048L;
	private static Logger logger = LoggerFactory.getLogger(AcquirerOperationReportAction.class.getName());

	@Autowired
	private AcquirerOperationReportService acquireOperationReportService;

	private String dateFrom;
	private String acquirerCode;
	private String dateTo;
	private String fileName;
	private String paymentType;
	private InputStream fileInputStream;

	List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();

	User sessionUser = new User();

	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd.MM.yyyy");
		try {
			
			transactionList = acquireOperationReportService.getRefundData(dateFrom, dateTo, acquirerCode, paymentType);
			
			Date date = new Date();
			fileName = "PAYMENT_GATEWAY_ SBI_Refund_" + outputDateFormat.format(date) + ".txt";

			File file = new File(fileName);
			file.createNewFile();

			FileWriter write = new FileWriter(file);
			PrintWriter dataWriter = new PrintWriter(write);

			for (TransactionSearch txnObject : transactionList) {

				dataWriter.println("20|" + DateCreater.formatFileDate(txnObject.getTransactionCaptureDate()) + "|"
						+ DateCreater.formatFileDate(txnObject.getRefundDate()) + "|" + txnObject.getAcqId() + "|"
						+ txnObject.getAmount() + "|" + txnObject.getRefundAmount());
			}
			FileInputStream inputStream = new FileInputStream(file);
			setFileInputStream(inputStream);
			dataWriter.close();
			dataWriter.flush();

		} catch (Exception ex) {

			logger.error("Exception Caught : ", ex);
		}

		return SUCCESS;
	}
	
	
	
	public String downloadSBIBusinessFile() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			
			transactionList = acquireOperationReportService.getRefundData(dateFrom, dateTo, acquirerCode, paymentType);
			
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			
			fileName = "PAYMENT_GATEWAY_ 00" +  ".txt";

			File file = new File(fileName);
			file.createNewFile();

			FileWriter write = new FileWriter(file);
			PrintWriter dataWriter = new PrintWriter(write);
			int srNo = 1;
			for (TransactionSearch txnObject : transactionList) {

				dataWriter.println(txnObject.getAcqId() + "|" + txnObject.getPgRefNum() + "|" + srNo + "|" + txnObject.getRefundAmount());
				srNo++;
			}
			FileInputStream inputStream = new FileInputStream(file);
			
			String checksum = acquireOperationReportService.getFileChecksum(md5Digest, inputStream);
			fileName = "PAYMENT_GATEWAY_ 00_" + checksum + ".txt";
			setFileInputStream(inputStream);
			dataWriter.close();
			dataWriter.flush();

		} catch (Exception ex) {
			logger.error("Exception Caught : ", ex);
		}

		return SUCCESS;
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

	public List<TransactionSearch> getAaData() {
		return transactionList;
	}

	public void setAaData(List<TransactionSearch> aaData) {
		this.transactionList = aaData;
	}

	public String getAcquirerCode() {
		return acquirerCode;
	}

	public void setAcquirerCode(String acquirerCode) {
		this.acquirerCode = acquirerCode;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

}
