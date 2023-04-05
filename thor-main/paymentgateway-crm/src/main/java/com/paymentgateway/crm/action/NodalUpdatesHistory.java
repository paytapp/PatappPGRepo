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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SummaryReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.NodalPayoutUpdateService;

public class NodalUpdatesHistory extends AbstractSecureAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2756112336707910589L;
	private String merchant;
	private String acquirer;
	private String settlementDate;
	private String nodalSettlementDate;
	private String response;
	private String fromDate;
	private String toDate;
	private String paymentMethod;
	private String nodalType;
	private InputStream fileInputStream;
	private String filename;
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private User sessionUser = new User();

	private static Logger logger = LoggerFactory.getLogger(NodalPayoutsUpdateAction.class.getName());
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private NodalPayoutUpdateService nodalPayoutUpdateService;
	
	@Autowired
	private UserDao userDao;
	

	@SuppressWarnings("resource")
	public String execute() {
		
		logger.info("Inside NodalUpdatesHistory action , execute");
		if (acquirer == null || acquirer.isEmpty()) {
			acquirer = "ALL";

		}
		
		setFromDate((nodalSettlementDate + " 00:00:00"));
		setToDate((nodalSettlementDate + " 23:59:59"));
		String payId = userDao.getPayIdByEmailId(merchant);
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<SummaryReportObject> transactionList = new ArrayList<SummaryReportObject>();
		try {
			transactionList = nodalPayoutUpdateService.downloadNodalReport(payId, acquirer, 
	  				nodalSettlementDate, nodalType,paymentMethod, fromDate, toDate, sessionUser);
		} catch (Exception e) {
		logger.error("Exception",e);
		}
		logger.info("List generated successfully for Download nodal Settlement Report");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Nodal Settlement Report");

		row = sheet.createRow(0);

		row.createCell(0).setCellValue("Sr No");
		row.createCell(1).setCellValue("Txn Id");
		row.createCell(2).setCellValue("Pg Ref Num");
		row.createCell(3).setCellValue("Payment Method");
		row.createCell(4).setCellValue("Mop Type");
		row.createCell(5).setCellValue("Order Id");
		row.createCell(6).setCellValue("Business Name");
		row.createCell(7).setCellValue("Currency");	
		row.createCell(8).setCellValue("Transaction Type");
		row.createCell(9).setCellValue("Capture Date");	
		row.createCell(10).setCellValue("Settlement Date");	
		row.createCell(11).setCellValue("Transaction Region");
		row.createCell(12).setCellValue("Card Holder Type");
		row.createCell(13).setCellValue("Acquirer");
		row.createCell(14).setCellValue("Total Amount");
		row.createCell(15).setCellValue("TDR/SC (Acquirer)");
		row.createCell(16).setCellValue("TDR/SC (Pg)");
		row.createCell(17).setCellValue("TDR/SC (Payment Gateway)");
		row.createCell(18).setCellValue("GST(Acquirer)");
		row.createCell(19).setCellValue("GST(Pg)");
		row.createCell(20).setCellValue("GST(Payment Gateway)");
		row.createCell(21).setCellValue("Merchant Amount");
		row.createCell(22).setCellValue("ACQ ID");
		row.createCell(23).setCellValue("RRN");
		row.createCell(24).setCellValue("Post Settled Flag");
		row.createCell(25).setCellValue("Delta Refund flag");
		row.createCell(26).setCellValue("Status");
		row.createCell(27).setCellValue("Nodal Settled Date");

		for (SummaryReportObject transactionSearch : transactionList) {
			row = sheet.createRow(rownum++);
			transactionSearch.setSrNo(String.valueOf(rownum-1));
			Object[] objArr = transactionSearch.downloadNodalSettlement();

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
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "NodalSettlementReport" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);	

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		logger.info("File generated successfully for Download nodal Settlement Report");
		return SUCCESS;

	}

	

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	public String getAcquirer() {
		return acquirer;
	}


	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}


	public String getMerchant() {
		return merchant;
	}


	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}


	public String getSettlementDate() {
		return settlementDate;
	}


	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}


	public String getNodalSettlementDate() {
		return nodalSettlementDate;
	}


	public void setNodalSettlementDate(String nodalSettlementDate) {
		this.nodalSettlementDate = nodalSettlementDate;
	}


	public String getResponse() {
		return response;
	}


	public void setResponse(String response) {
		this.response = response;
	}


	public String getFromDate() {
		return fromDate;
	}


	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}


	public String getToDate() {
		return toDate;
	}


	public void setToDate(String toDate) {
		this.toDate = toDate;
	}


	public String getPaymentMethod() {
		return paymentMethod;
	}


	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}


	public String getNodalType() {
		return nodalType;
	}


	public void setNodalType(String nodalType) {
		this.nodalType = nodalType;
	}


	public List<Merchants> getMerchantList() {
		return merchantList;
	}


	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}


	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}


}
