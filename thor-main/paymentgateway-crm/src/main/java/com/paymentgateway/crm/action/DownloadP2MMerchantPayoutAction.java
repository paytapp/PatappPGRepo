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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadP2MMerchantPayoutAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(DownloadP2MMerchantPayoutAction.class.getName());
	private static final long serialVersionUID = 8031826739195944732L;
	private String dateFrom;
	private String dateTo;
	private String payId;
	private String rrn;
	private String orderId;
	private InputStream fileInputStream;
	private String filename;

	@Autowired
	private TxnReports txnReport;

	public String execute() {

		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		transactionList = txnReport.fetchP2MPayoutData(payId, orderId, rrn, dateFrom, dateTo);
		
		logger.info("List generated successfully for DownloadP2MMerchantPayoutAction");

		try {
			SXSSFWorkbook wb = new SXSSFWorkbook(1000000);
			logger.info("creating workbook");
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("P2M Payout Report");
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Create Date");
			row.createCell(2).setCellValue("RRN");
			row.createCell(3).setCellValue("Order Id");
			row.createCell(4).setCellValue("Payer VPA");
			row.createCell(5).setCellValue("Payee VPA");
			row.createCell(6).setCellValue("Amount");
			row.createCell(7).setCellValue("Status");

			logger.info("Header Created");

			for (TransactionSearch collectionList : transactionList) {
				row = sheet.createRow(rownum++);
				Object[] objArr = collectionList.myCsvMethodForP2MPayoutData();

				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}

			}
			logger.info("Data inserted");
		
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Merchant_P2M_Payout_Data" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			setFileInputStream(new FileInputStream(file));
			addActionMessage(filename + " written successfully on disk.");
			logger.info("Files generated successfully for DownloadP2MMerchantPayoutAction");
			// deleteFileFromBin();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;

	}


	public InputStream getFileInputStream() {
		return fileInputStream;
	}


	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}


	public String getPayId() {
		return payId;
	}


	public void setPayId(String payId) {
		this.payId = payId;
	}


	public String getRrn() {
		return rrn;
	}


	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

}
