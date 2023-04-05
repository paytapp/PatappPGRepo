package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.SearchTransaction;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.SearchTransactionReport;

public class AgentSearchAction extends AbstractSecureAction {

	private static final long serialVersionUID = -5956533558995482980L;
	private static Logger logger = LoggerFactory.getLogger(AgentSearchAction.class.getName());

	@Autowired
	private CrmValidator validator;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private SearchTransactionReport searchTransactionReport;

	private String orderId;
	private String pgRefNum;
	private String rrn;
	private String consumerNumber;
	private String acqId;
	private InputStream fileInputStream;
	private String fileName;
	private List<SearchTransaction> aaData;
	private List<SearchTransaction> aaDataStatus;
	private User sessionUser = new User();

	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			aaData = encoder.encodeSearchTransactionObj(searchTransactionReport.searchPayment(getOrderId(),
					getPgRefNum(), sessionUser, getConsumerNumber(), getRrn(), getAcqId()));

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {

				aaDataStatus = encoder.encodeSearchTransactionObj(searchTransactionReport.searchPaymentFromStatus(
						getOrderId(), getPgRefNum(), sessionUser, getConsumerNumber(), getRrn(), getAcqId()));
			}

			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String downloadAgentSearchReport() {
		logger.info("Inside downloadAgentSearchReport() ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			List<SearchTransaction> transactionList = searchTransactionReport.searchPayment(getOrderId(), getPgRefNum(),
					sessionUser, getConsumerNumber(), getRrn(), getAcqId());

			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Agent Search Report");
			row = sheet.createRow(0);

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Pay Id");
				row.createCell(2).setCellValue("TXN ID");
				row.createCell(3).setCellValue("PG Ref No");
				row.createCell(4).setCellValue("Merchant Name");
				row.createCell(5).setCellValue("Sub Merchant Name");
				row.createCell(6).setCellValue("Order ID");
				row.createCell(7).setCellValue("Refund Order Id");
				row.createCell(8).setCellValue("Date");
				row.createCell(9).setCellValue("TXN Type");
				row.createCell(10).setCellValue("Transaction Flag");
				row.createCell(11).setCellValue("Status");
				row.createCell(12).setCellValue("Acquirer Name");
				row.createCell(13).setCellValue("Acquiring Mode");
				row.createCell(14).setCellValue("Payment Type");
				row.createCell(15).setCellValue("MOP");
				row.createCell(16).setCellValue("Payment Region");
				row.createCell(17).setCellValue("Card Holder Type");
				row.createCell(18).setCellValue("Card Number");
				row.createCell(19).setCellValue("Customer Name");
				row.createCell(20).setCellValue("Amount");
				row.createCell(21).setCellValue("Total Amount");
				row.createCell(22).setCellValue("Total TDR SC");
				row.createCell(23).setCellValue("Total GST");
				row.createCell(24).setCellValue("PG TDR SC");
				row.createCell(25).setCellValue("PG GST");
				row.createCell(26).setCellValue("Acquirer TDR SC");
				row.createCell(27).setCellValue("Acquirer GST");
				row.createCell(28).setCellValue("Reseller TDR SC");
				row.createCell(29).setCellValue("Reseller GST");
				row.createCell(30).setCellValue("Payment Gateway Response MSG");
				row.createCell(31).setCellValue("Payment Gateway Response Code");
				row.createCell(32).setCellValue("RRN");
				row.createCell(33).setCellValue("ACQ ID");
				row.createCell(34).setCellValue("Acquirer Response MSG");
				row.createCell(35).setCellValue("Consumer No");
				row.createCell(36).setCellValue("UDF10");

				for (SearchTransaction transactionSearch : transactionList) {
					if (StringUtils.isBlank(transactionSearch.getSubMerchantId())) {
						transactionSearch.setSubMerchantId("NA");
					}
					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));

					Object[] objArr = transactionSearch.downloadAgentSearchReport(sessionUser);

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

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "AgentSearch_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(fileName + " written successfully on disk.");
			logger.info("File generated successfully for downloadAgentSearchReport");

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public void validate() {

		if (validator.validateBlankField(getOrderId())) {
		} else if (!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())) {
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if (validator.validateBlankField(getPgRefNum())) {
		} else if (!validator.validateField(CrmFieldType.PG_REF_NUM, getPgRefNum())) {
			addFieldError(CrmFieldType.PG_REF_NUM.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if (validator.validateBlankField(getConsumerNumber())) {
		} else if (!validator.validateField(CrmFieldType.CUST_ID, getConsumerNumber())) {
			addFieldError(CrmFieldType.CUST_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if (validator.validateBlankField(getRrn())) {
		} else if (!validator.validateField(CrmFieldType.RRN, getRrn())) {
			addFieldError(CrmFieldType.RRN.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

	}

	public List<SearchTransaction> getaaData() {
		return aaData;
	}

	public void setaaData(List<SearchTransaction> setaaData) {
		this.aaData = setaaData;
	}

	public List<SearchTransaction> getAaDataStatus() {
		return aaDataStatus;
	}

	public void setAaDataStatus(List<SearchTransaction> aaDataStatus) {
		this.aaDataStatus = aaDataStatus;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getConsumerNumber() {
		return consumerNumber;
	}

	public void setConsumerNumber(String consumerNumber) {
		this.consumerNumber = consumerNumber;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

}
