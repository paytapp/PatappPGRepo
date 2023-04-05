package com.paymentgateway.crm.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadSearchedTransactionAction extends AbstractSecureAction {

	/**
	 * @ Alam
	 */
	private static final long serialVersionUID = -8314975964251869093L;
	private static Logger logger = LoggerFactory.getLogger(DownloadSearchedTransactionAction.class.getName());
	
	@Autowired
	private TxnReports txnReports;
	
	private InputStream fileInputStream;
	private List<String> fileData;
	private String fileName;
	private String response;
	private String responseMsg;
	private File csvFile;
	private User sessionUser = new User();
	
	public String execute() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			try {
				fileData = filterFileData(csvFile);
				for (int i = 1; i < fileData.size(); i++) {
					String dataArray[] = fileData.get(i).split(",");
					if (dataArray.length != 0) {
						List<TransactionSearchDownloadObject> txnList = txnReports.downloadSearchTransactionData(dataArray, sessionUser);
						transactionList.addAll(txnList);
					}
				}

				logger.info("transactionList created and size = " + transactionList.size());
				Comparator<TransactionSearchDownloadObject> comp = (TransactionSearchDownloadObject a,
						TransactionSearchDownloadObject b) -> {

					if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
						return -1;
					} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
						return 1;
					} else {
						return 0;
					}
				};
				Collections.sort(transactionList, comp);
				logger.info("transactionList created and Sorted");

				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				int n =2;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Searched Data Report");
				row = sheet.createRow(0);

				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Txn Id");
				row.createCell(2).setCellValue("Pg Ref Num");
				row.createCell(3).setCellValue("Merchant");
				row.createCell(4).setCellValue("Sub Merchant");
				row.createCell(5).setCellValue("Acquirer Type");
				row.createCell(6).setCellValue("Date");
				row.createCell(7).setCellValue("Order Id");
				row.createCell(8).setCellValue("Payment Method");
				row.createCell(9).setCellValue("Mop Type");
				row.createCell(10).setCellValue("CardHolder Type");
				row.createCell(11).setCellValue("Mask");
				row.createCell(12).setCellValue("Txn Type");
				row.createCell(13).setCellValue("Transaction Mode");
				row.createCell(14).setCellValue("Status");
				row.createCell(15).setCellValue("Transaction Region");
				row.createCell(16).setCellValue("Base Amount");
				row.createCell(17).setCellValue("TDR / Surcharge (Merchant)");
				row.createCell(18).setCellValue("GST (Merchant)");
				row.createCell(19).setCellValue("SUF CHARGES");
				row.createCell(20).setCellValue("SUF GST");
				row.createCell(19+n).setCellValue("TDR / Surcharge (Acquirer)");
				row.createCell(20+n).setCellValue("GST (Acquirer)");
				row.createCell(21+n).setCellValue("TDR / Surcharge (PG)");
				row.createCell(22+n).setCellValue("GST (PG)");
				row.createCell(23+n).setCellValue("TDR / Surcharge (Reseller)");
				row.createCell(24+n).setCellValue("GST (Reseller)");
				row.createCell(25+n).setCellValue("Total Amount");
				row.createCell(26+n).setCellValue("ACQ ID");
				row.createCell(27+n).setCellValue("RRN");
				row.createCell(28+n).setCellValue("Transaction Flag");
				row.createCell(29+n).setCellValue("Refund Order ID");
				row.createCell(30+n).setCellValue("PG Response Message");
				row.createCell(31+n).setCellValue("Acquirer Response Message");
				row.createCell(32+n).setCellValue("UDF11");
				row.createCell(33+n).setCellValue("UDF12");
				row.createCell(34+n).setCellValue("UDF13");
				row.createCell(35+n).setCellValue("UDF14");
				row.createCell(36+n).setCellValue("UDF15");
				row.createCell(37+n).setCellValue("UDF16");
				row.createCell(38+n).setCellValue("UDF17");
				row.createCell(39+n).setCellValue("UDF18");
				row.createCell(40+n).setCellValue("Pay Id");
				row.createCell(41+n).setCellValue("Consumer No");

				for (TransactionSearchDownloadObject transactionSearch : transactionList) {
					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportForAdminAndSubMerchant();

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
				fileName = "Searched_Data_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(fileName);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(fileName + " written successfully on disk.");
				logger.info("File generated successfully for DownloadPaymentsReportAction");
				setResponse("success");
			} catch (IOException e) {
				logger.error("exception " , e);
				setResponse("failed");
				setResponseMsg("Something went wrong ! ");
			}
		}
		return SUCCESS;
	}
	
	private List<String> filterFileData(File file) throws IOException {
		List<String> csvData = new ArrayList<>();
		BufferedReader br = null;
		try {
			String line = "";
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				csvData.add(line);
			}
			return csvData;

		} catch (Exception e) {
			logger.error("exception " , e);
			return csvData;
		} finally {
			if (br != null)
				br.close();
		}

	}
	
	public InputStream getFileInputStream() {
		return fileInputStream;
	}
	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}
	public List<String> getFileData() {
		return fileData;
	}
	public void setFileData(List<String> fileData) {
		this.fileData = fileData;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}
	
	
	
	

}
