package com.paymentgateway.crm.dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.action.AbstractSecureAction;


public class DownloadTransationByStatus extends AbstractSecureAction {
	
	@Autowired
	BarChartQuery barChartQuery;
	
	@Autowired
	private UserDao userDao;
	
	private String merchants;
	private String subMerchant;
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String transactionType;
	private String paymentsRegion;
	private InputStream fileInputStream;
	private String filename;
	private String inputDays;
	private boolean saleReportFlag;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadTransationByStatus.class.getName());
	
	public String execute() {
		// setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		// setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

//		if (inputDays.equalsIgnoreCase("custom") && dateFrom.equals(dateTo)) {
//			SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
//			Date dt = new Date();
//			String strdate = sdf2.format(dt);
//
//			if (strdate.equals(dateFrom)) {
//				inputDays = "day";
//			} else {
//				inputDays = "previousDay";
//			}
//
//		}

		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

			if (inputDays.equalsIgnoreCase("day")) {
				Calendar calendar = Calendar.getInstance();
				dateFrom = sdf1.format(calendar.getTime());
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

			} else if (inputDays.equalsIgnoreCase("week")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
					calendar.add(Calendar.DATE, -1);
				}
				dateFrom = sdf1.format(calendar.getTime());
			} else if (inputDays.equalsIgnoreCase("month")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

				calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				dateFrom = sdf1.format(calendar.getTime());
			} else if (inputDays.equalsIgnoreCase("year")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

				calendar.set(Calendar.DAY_OF_YEAR, 1);

				dateFrom = sdf1.format(calendar.getTime());

			} else if (inputDays.equalsIgnoreCase("custom")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);

				dateFrom = sdf1.format(fromDate);
				dateTo = sdf1.format(toDate);
			} else if (inputDays.equalsIgnoreCase("previousDay")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);
				dateFrom = sdf1.format(fromDate);
				dateTo = sdf1.format(toDate);

			}

			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			String payIdd = "";
			String subMerchantPayId = "";
			
			if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				payIdd = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			} else if (sessionUser.isSuperMerchant()) {
				payIdd = sessionUser.getSuperMerchantId();
				if (!merchants.equalsIgnoreCase("ALL MERCHANTS")) {
					subMerchantPayId = userDao.getPayIdByEmailId(merchants);
				} else {
					subMerchantPayId = subMerchant;
				}

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && !sessionUser.isSuperMerchant()) {
				payIdd = sessionUser.getPayId();
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					payIdd = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					payIdd = parentUser.getPayId();
					// subMerchantPayId = userDao.getPayIdByEmailId(merchants);
				}
			} else {
				if (!merchants.equalsIgnoreCase("ALL MERCHANTS")) {
					payIdd = userDao.getPayIdByEmailId(merchants);
					subMerchantPayId = subMerchant;
				} else {
					payIdd = merchants;
					subMerchantPayId = subMerchant;
				}
			}

			transactionList = barChartQuery.transactionListByTransactionType(payIdd, subMerchantPayId, paymentsRegion,
					getCurrency(), getTransactionType(), dateFrom, dateTo, sessionUser, saleReportFlag);

			BigDecimal st = null;

			logger.info("List generated successfully for Dashboard Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Dashboard Report By Status");
			row = sheet.createRow(0);

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if (StringUtils.isBlank(subMerchantPayId) && !sessionUser.isSuperMerchant()) {
					row.createCell(0).setCellValue("Txn ID");
					row.createCell(1).setCellValue("Pg Ref Num");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Acquirer Type");
					row.createCell(4).setCellValue("Date");
					row.createCell(5).setCellValue("Order ID");
					row.createCell(6).setCellValue("Payment Method");
					row.createCell(7).setCellValue("Mop Type");
					row.createCell(8).setCellValue("Mask");
					row.createCell(9).setCellValue("Cust Name");
					row.createCell(10).setCellValue("Cardholder Type");
					row.createCell(11).setCellValue("Txn Type");
					row.createCell(12).setCellValue("Transaction Mode");
					row.createCell(13).setCellValue("Status");
					row.createCell(14).setCellValue("Transaction Region");
					row.createCell(15).setCellValue("Base Amount");
					row.createCell(16).setCellValue("Total Amount");
					row.createCell(17).setCellValue("TDR/Surcharge");
					row.createCell(18).setCellValue("GST");
					row.createCell(19).setCellValue("Merchant Amount");
					row.createCell(20).setCellValue("Acquirer Response Message");
					row.createCell(21).setCellValue("Paymnet Gateway Response Message");
					if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
						row.createCell(22).setCellValue("Transaction Flag");
					} else {
						row.createCell(22).setCellValue("");
					}

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.myCsvMethodForDashBoardAdminMerchant();

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
					row.createCell(0).setCellValue("Txn ID");
					row.createCell(1).setCellValue("Pg Ref Num");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Sub Merchant Name");
					row.createCell(4).setCellValue("Acquirer Type");
					row.createCell(5).setCellValue("Date");
					row.createCell(6).setCellValue("Order ID");
					row.createCell(7).setCellValue("Payment Method");
					row.createCell(8).setCellValue("Mop Type");
					row.createCell(9).setCellValue("Mask");
					row.createCell(10).setCellValue("Cust Name");
					row.createCell(11).setCellValue("Cardholder Type");
					row.createCell(12).setCellValue("Txn Type");
					row.createCell(13).setCellValue("Transaction Mode");
					row.createCell(14).setCellValue("Status");
					row.createCell(15).setCellValue("Transaction Region");
					row.createCell(16).setCellValue("Base Amount");
					row.createCell(17).setCellValue("Total Amount");
					row.createCell(18).setCellValue("TDR/Surcharge");
					row.createCell(19).setCellValue("GST");
					row.createCell(20).setCellValue("Merchant Amount");
					row.createCell(21).setCellValue("Acquirer Response Message");
					row.createCell(22).setCellValue("Payment Gateway Response Message");
					if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
						row.createCell(23).setCellValue("Transaction Flag");
					} else {
						row.createCell(23).setCellValue("");
					}

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.myCsvMethodForDashBoardAdminSubMerchant();

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
			} else if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				if (StringUtils.isBlank(subMerchantPayId) && !sessionUser.isSuperMerchant()) {
					row.createCell(0).setCellValue("Txn ID");
					row.createCell(1).setCellValue("Pg Ref Num");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Date");
					row.createCell(4).setCellValue("Order ID");
					row.createCell(5).setCellValue("Payment Method");
					row.createCell(6).setCellValue("Mop Type");
					row.createCell(7).setCellValue("Mask");
					row.createCell(8).setCellValue("Cust Name");
					row.createCell(9).setCellValue("Cardholder Type");
					row.createCell(10).setCellValue("Txn Type");
					row.createCell(11).setCellValue("Transaction Mode");
					row.createCell(12).setCellValue("Status");
					row.createCell(13).setCellValue("Transaction Region");
					row.createCell(14).setCellValue("Base Amount");
					row.createCell(15).setCellValue("Total Amount");
					row.createCell(16).setCellValue("TDR/Surcharge");
					row.createCell(17).setCellValue("GST");
					row.createCell(18).setCellValue("Merchant Amount");
					row.createCell(19).setCellValue("Acquirer Response Message");
					row.createCell(20).setCellValue("Payment Gateway Response Message");
					if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
						row.createCell(21).setCellValue("Transaction Flag");
					} else {
						row.createCell(21).setCellValue("");
					}

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.myCsvMethodForDashBoardResellerMerchant();

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
					row.createCell(0).setCellValue("Txn ID");
					row.createCell(1).setCellValue("Pg Ref Num");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Sub Merchant Name");
					row.createCell(4).setCellValue("Date");
					row.createCell(5).setCellValue("Order ID");
					row.createCell(6).setCellValue("Payment Method");
					row.createCell(7).setCellValue("Mop Type");
					row.createCell(8).setCellValue("Mask");
					row.createCell(9).setCellValue("Cust Name");
					row.createCell(10).setCellValue("Cardholder Type");
					row.createCell(11).setCellValue("Txn Type");
					row.createCell(12).setCellValue("Transaction Mode");
					row.createCell(13).setCellValue("Status");
					row.createCell(14).setCellValue("Transaction Region");
					row.createCell(15).setCellValue("Base Amount");
					row.createCell(16).setCellValue("Total Amount");
					row.createCell(17).setCellValue("TDR/Surcharge");
					row.createCell(18).setCellValue("GST");
					row.createCell(19).setCellValue("Merchant Amount");
					row.createCell(20).setCellValue("Acquirer Response Message");
					row.createCell(21).setCellValue("Paymnet Gateway Response Message");
					if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
						row.createCell(22).setCellValue("Transaction Flag");
					} else {
						row.createCell(22).setCellValue("");
					}

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.myCsvMethodForDashBoardResellerSubMerchant();

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
			} else {
				if (StringUtils.isBlank(subMerchantPayId) && !sessionUser.isSuperMerchant()) {
					row.createCell(0).setCellValue("Txn ID");
					row.createCell(1).setCellValue("Pg Ref Num");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Date");
					row.createCell(4).setCellValue("Order ID");
					row.createCell(5).setCellValue("Payment Method");
					row.createCell(6).setCellValue("Mop Type");
					row.createCell(7).setCellValue("Mask");
					row.createCell(8).setCellValue("Cust Name");
					row.createCell(9).setCellValue("Cardholder Type");
					row.createCell(10).setCellValue("Txn Type");
					row.createCell(11).setCellValue("Transaction Mode");
					row.createCell(12).setCellValue("Status");
					row.createCell(13).setCellValue("Transaction Region");
					row.createCell(14).setCellValue("Base Amount");
					row.createCell(15).setCellValue("Total Amount");
					row.createCell(16).setCellValue("TDR/Surcharge");
					row.createCell(17).setCellValue("GST");
					row.createCell(18).setCellValue("Merchant Amount");
					row.createCell(19).setCellValue("Acquirer Response Message");
					row.createCell(20).setCellValue("Paymnet Gateway Response Message");
					if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
						row.createCell(21).setCellValue("Transaction Flag");
					} else {
						row.createCell(21).setCellValue("");
					}

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.myCsvMethodForDashBoardMerchant();

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
					row.createCell(0).setCellValue("Txn ID");
					row.createCell(1).setCellValue("Pg Ref Num");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Sub Merchant Name");
					row.createCell(4).setCellValue("Date");
					row.createCell(5).setCellValue("Order ID");
					row.createCell(6).setCellValue("Payment Method");
					row.createCell(7).setCellValue("Mop Type");
					row.createCell(8).setCellValue("Mask");
					row.createCell(9).setCellValue("Cust Name");
					row.createCell(10).setCellValue("Cardholder Type");
					row.createCell(11).setCellValue("Txn Type");
					row.createCell(12).setCellValue("Transaction Mode");
					row.createCell(13).setCellValue("Status");
					row.createCell(14).setCellValue("Transaction Region");
					row.createCell(15).setCellValue("Base Amount");
					row.createCell(16).setCellValue("Total Amount");
					row.createCell(17).setCellValue("TDR/Surcharge");
					row.createCell(18).setCellValue("GST");
					row.createCell(19).setCellValue("Merchant Amount");
					row.createCell(20).setCellValue("Acquirer Response Message");
					row.createCell(21).setCellValue("Payment Gateway Response Message");
					if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
						row.createCell(22).setCellValue("Transaction Flag");
					} else {
						row.createCell(22).setCellValue("");
					}

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.myCsvMethodForDashBoardSubMerchant();

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
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			if (StringUtils.isNotBlank(transactionType)) {
				filename = "Dashboard_Report_" + transactionType + "_" + df.format(new Date()) + FILE_EXTENSION;
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
			logger.info("File generated successfully for Dashboard report");
//			downloadPayoutReport();
		} catch (Exception e) {
			logger.error("Exception", e);
		}

		return SUCCESS;
	}

	public String downloadPayoutReport() {
		// setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		// setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		if (inputDays.equalsIgnoreCase("custom") && dateFrom.equals(dateTo)) {
			SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
			Date dt = new Date();
			String strdate = sdf2.format(dt);

			if (strdate.equals(dateFrom)) {
				inputDays = "day";
			} else {
				inputDays = "previousDay";
			}

		}

		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

			if (inputDays.equalsIgnoreCase("day")) {
				Calendar calendar = Calendar.getInstance();
				dateFrom = sdf1.format(calendar.getTime());
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

			} else if (inputDays.equalsIgnoreCase("week")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
					calendar.add(Calendar.DATE, -1);
				}
				dateFrom = sdf1.format(calendar.getTime());
			} else if (inputDays.equalsIgnoreCase("month")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

				calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				dateFrom = sdf1.format(calendar.getTime());
			} else if (inputDays.equalsIgnoreCase("year")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

				calendar.set(Calendar.DAY_OF_YEAR, 1);

				dateFrom = sdf1.format(calendar.getTime());

			} else if (inputDays.equalsIgnoreCase("custom")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);

				dateFrom = sdf1.format(fromDate);
				dateTo = sdf1.format(toDate);
			} else if (inputDays.equalsIgnoreCase("previousDay")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);
				dateFrom = sdf1.format(fromDate);
				dateTo = sdf1.format(toDate);

			}

			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			String payIdd = "";
			String subMerchantPayId = "";
			
			if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				payIdd = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			} else if (sessionUser.isSuperMerchant()) {
				payIdd = sessionUser.getSuperMerchantId();
				if (!merchants.equalsIgnoreCase("ALL MERCHANTS")) {
					subMerchantPayId = userDao.getPayIdByEmailId(merchants);
				} else {
					subMerchantPayId = subMerchant;
				}

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && !sessionUser.isSuperMerchant()) {
				payIdd = sessionUser.getPayId();
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					payIdd = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					payIdd = parentUser.getPayId();
					// subMerchantPayId = userDao.getPayIdByEmailId(merchants);
				}
			} else {
				if (!merchants.equalsIgnoreCase("ALL MERCHANTS")) {
					payIdd = userDao.getPayIdByEmailId(merchants);
					subMerchantPayId = subMerchant;
				} else {
					payIdd = merchants;
					subMerchantPayId = subMerchant;
				}
			}

			transactionList = barChartQuery.pauOutDataReportForDownload(payIdd, dateFrom, dateTo, sessionUser, subMerchantPayId, transactionType);

			BigDecimal st = null;

			logger.info("List generated successfully for Dashboard Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Dashboard Report By Status");
			row = sheet.createRow(0);

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if (StringUtils.isBlank(subMerchantPayId)) {
					row.createCell(0).setCellValue("Transaction Id");
					row.createCell(1).setCellValue("Merchant Name");
					row.createCell(2).setCellValue("Order ID");
					row.createCell(3).setCellValue("Status");
					row.createCell(4).setCellValue("Amount");
					row.createCell(5).setCellValue("Date Time");
				
					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.csvMethodForPayOutDashBoardAdminMerchant();

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
					row.createCell(0).setCellValue("Transaction Id");
					row.createCell(1).setCellValue("Merchant Name");
					row.createCell(2).setCellValue("Sub Merchant");
					row.createCell(3).setCellValue("Order ID");
					row.createCell(4).setCellValue("Status");
					row.createCell(5).setCellValue("Amount");
					row.createCell(6).setCellValue("Date Time");
					
					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.csvMethodForPayOutDashBoardAdminSubMerchant();

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
			} else if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				if (StringUtils.isBlank(subMerchantPayId)) {
					row.createCell(0).setCellValue("Transaction Id");
					row.createCell(1).setCellValue("Merchant Name");
					row.createCell(2).setCellValue("Order ID");
					row.createCell(3).setCellValue("Status");
					row.createCell(4).setCellValue("Amount");
					row.createCell(5).setCellValue("Date Time");
				
					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.csvMethodForPayOutDashBoardAdminMerchant();

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
					row.createCell(0).setCellValue("Transaction Id");
					row.createCell(1).setCellValue("Merchant Name");
					row.createCell(2).setCellValue("Sub Merchant");
					row.createCell(3).setCellValue("Order ID");
					row.createCell(4).setCellValue("Status");
					row.createCell(5).setCellValue("Amount");
					row.createCell(6).setCellValue("Date Time");

					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.csvMethodForPayOutDashBoardAdminSubMerchant();

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
			} else {
				if (StringUtils.isBlank(subMerchantPayId) && !sessionUser.isSuperMerchant()) {
					row.createCell(0).setCellValue("Transaction Id");
					row.createCell(1).setCellValue("Merchant Name");
					row.createCell(2).setCellValue("Order ID");
					row.createCell(3).setCellValue("Status");
					row.createCell(4).setCellValue("Amount");
					row.createCell(5).setCellValue("Date Time");
				
					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.csvMethodForPayOutDashBoardAdminMerchant();

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
					row.createCell(0).setCellValue("Transaction Id");
					row.createCell(1).setCellValue("Merchant Name");
					row.createCell(2).setCellValue("Sub Merchant");
					row.createCell(3).setCellValue("Order ID");
					row.createCell(4).setCellValue("Status");
					row.createCell(5).setCellValue("Amount");
					row.createCell(6).setCellValue("Date Time");
					
					for (TransactionSearch transaction : transactionList) {
						row = sheet.createRow(rownum++);
						Object[] objArr = transaction.csvMethodForPayOutDashBoardAdminSubMerchant();

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
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			if (StringUtils.isNotBlank(transactionType)) {
				filename = "Dashboard_PayOut_Report" + df.format(new Date()) + FILE_EXTENSION;
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
			logger.info("File generated successfully for Dashboard report");

		} catch (Exception e) {
			logger.error("Exception", e);
		}

		return SUCCESS;
	}
	public String getMerchants() {
		return merchants;
	}

	public void setMerchants(String merchants) {
		this.merchants = merchants;
	}

	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
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

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
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
	
	public String getInputDays() {
		return inputDays;
	}


	public void setInputDays(String inputDays) {
		this.inputDays = inputDays;
	}

	public boolean isSaleReportFlag() {
		return saleReportFlag;
	}

	public void setSaleReportFlag(boolean saleReportFlag) {
		this.saleReportFlag = saleReportFlag;
	}
	
}
