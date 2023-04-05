package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Enach;
import com.paymentgateway.commons.user.UpiAutoPay;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Rajit
 */
public class UpiAutoPayReportAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private TxnReports txnReport;

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private Fields field;

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	private static final long serialVersionUID = 5356090298471876833L;
	private static Logger logger = LoggerFactory.getLogger(UpiAutoPayReportAction.class.getName());

	private String orderId;
	private String umnNumber;
	private String merchantPayId;
	private String subMerchantPayId;
	private String status;
	private String dateFrom;
	private String dateTo;
	private String pgRefNum;
	private String emandateUrl;
	private String response;
	private String responseMessage;

	private InputStream fileInputStream;
	public String filename;

	private User sessionUser = new User();
	private List<UpiAutoPay> aaData = new ArrayList<UpiAutoPay>();

	public String registrationReport() {
		logger.info("inside upi autoPay registration report");
		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}

			}

			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			String txnType = TransactionType.REGISTRATION.getName();
			aaData = upiAutoPayDao.fetchRegistrationDetails(orderId, umnNumber, merchantPayId, subMerchantPayId,
					resellerId, status, txnType, dateFrom, dateTo);

		} catch (Exception ex) {
			logger.info("exception caught in registration report ", ex);
			return ERROR;
		}
		return SUCCESS;
	}

	public String transactionReport() {

		logger.info("inside upi autopay view transaction report");

		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}

			} else {
				/*
				 * if (StringUtils.isNotBlank(subMerchantPayId) &&
				 * !subMerchantPayId.equalsIgnoreCase("All")) { subMerchantPayId =
				 * userDao.getPayIdByEmailId(subMerchantPayId); }
				 */
			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			String txnType = TransactionType.SALE.getName();
			aaData = upiAutoPayDao.fetchAutopayTransactionReport(orderId, umnNumber, merchantPayId, subMerchantPayId,
					resellerId, status, txnType, dateFrom, dateTo);

		} catch (Exception ex) {
			logger.info("exception caught in view transaction report ", ex);
		}
		return SUCCESS;
	}

	public String debitReport() {

		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}
			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			String txnType = TransactionType.REGISTRATION.getName();

			aaData = upiAutoPayDao.fetchRegistrationDetails(orderId, umnNumber, merchantPayId, subMerchantPayId,
					resellerId, status, txnType, dateFrom, dateTo);
		} catch (Exception ex) {
			logger.info("exception caught in view debit report ", ex);
		}
		return SUCCESS;
	}

	public String autoPayDebitTransaction() {

		logger.info("inside UpiAutoPayReportAction debit transaction function ");
		try {
			aaData = upiAutoPayDao.fetchAutoPayDebitTransactionDetails(orderId, pgRefNum);
		} catch (Exception ex) {
			logger.info("exception caught in debitTransaction function ", ex);
		}
		return SUCCESS;
	}

	public String downloadTransactionReport() {

		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}
			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			String txnType = TransactionType.SALE.getName();
			aaData = upiAutoPayDao.fetchAutopayTransactionReport(orderId, umnNumber, merchantPayId, subMerchantPayId,
					resellerId, status, txnType, dateFrom, dateTo);

			logger.info("List generated successfully for upi autoPay Download Transaction Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Transaction Report AutoPay");
			row = sheet.createRow(0);

			if (StringUtils.isNotBlank(subMerchantPayId)) {
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Sub Merchant Name");
				row.createCell(4).setCellValue("Sub Merchant Pay Id");
				row.createCell(5).setCellValue("Order Id");
				row.createCell(6).setCellValue("Pg Ref Number");
				row.createCell(7).setCellValue("UMN Number");
				row.createCell(8).setCellValue("Payment Type");
				row.createCell(9).setCellValue("Registration Date");
				row.createCell(10).setCellValue("Notification Date");
				row.createCell(11).setCellValue("Create Date");
				row.createCell(12).setCellValue("Debit Date");
				row.createCell(13).setCellValue("Due Date");
				row.createCell(14).setCellValue("Cust Email");
				row.createCell(15).setCellValue("Cust Mobile");
				row.createCell(16).setCellValue("Amount");
				row.createCell(17).setCellValue("Debit Amount");
				row.createCell(18).setCellValue("Total Amount");
				row.createCell(19).setCellValue("Status");

				for (UpiAutoPay autoPay : aaData) {

					row = sheet.createRow(rownum++);
					autoPay.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = autoPay.methodDownloadAutoPayTransactionReportForSuperMerchant();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
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
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Order Id");
				row.createCell(4).setCellValue("Pg Ref Number");
				row.createCell(5).setCellValue("UMN Number");
				row.createCell(6).setCellValue("Payment Type");
				row.createCell(7).setCellValue("Registration Date");
				row.createCell(8).setCellValue("Create Date");
				row.createCell(9).setCellValue("Notification Date");
				row.createCell(10).setCellValue("Debit Date");
				row.createCell(11).setCellValue("Due Date");
				row.createCell(12).setCellValue("Cust Email");
				row.createCell(13).setCellValue("Cust Mobile");
				row.createCell(14).setCellValue("Amount");
				row.createCell(15).setCellValue("Debit Amount");
				row.createCell(16).setCellValue("Total Amount");
				row.createCell(17).setCellValue("Status");

				for (UpiAutoPay autoPay : aaData) {

					row = sheet.createRow(rownum++);
					autoPay.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = autoPay.methodDownloadAutoPayTransactionReport();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
			}
			try {
				String FILE_EXTENSION = ".xlsx";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Transaction_Report_AutoPay" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			} catch (Exception ex) {
				logger.info("Exception caught in download transaction report ", ex);
				return ERROR;
			}
		} catch (Exception ex) {
			logger.info("exception caught in view debit report ", ex);
		}
		return SUCCESS;
	}

	public String downloadRegistrationReport() {

		logger.info("inside downloadRegistrationReport download autoPay registration Details ");
		try {
			String resellerId = "";
			String userType = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				userType = sessionUser.getUserType().toString();
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				userType = sessionUser.getUserType().toString();
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				userType = sessionUser.getUserType().toString();
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}

			}
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));

			String txnType = TransactionType.REGISTRATION.getName();
			aaData = upiAutoPayDao.fetchRegistrationDetails(orderId, umnNumber, merchantPayId, subMerchantPayId,
					resellerId, status, txnType, dateFrom, dateTo);

			logger.info("List generated successfully for Download AutoPay Registration Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Registration Report AutoPay");
			row = sheet.createRow(0);

			if (StringUtils.isNotBlank(subMerchantPayId)) {
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Sub Merchant Name");
				row.createCell(4).setCellValue("Sub Merchant Pay Id");
				row.createCell(5).setCellValue("Order Id");
				row.createCell(6).setCellValue("Pg Ref Number");
				row.createCell(7).setCellValue("UMN Number");
				row.createCell(8).setCellValue("Payment Type");
				row.createCell(9).setCellValue("Create Date");

				row.createCell(10).setCellValue("Cust Email");
				row.createCell(11).setCellValue("Cust Mobile");
				row.createCell(12).setCellValue("Amount");
				row.createCell(13).setCellValue("Debit Amount");
				row.createCell(14).setCellValue("Total Amount");
				if (userType.equalsIgnoreCase(UserType.MERCHANT.toString())
						|| userType.equalsIgnoreCase(UserType.RESELLER.toString())
						|| userType.equalsIgnoreCase(UserType.SUBUSER.toString())) {
					row.createCell(15).setCellValue("Status");
				} else {
					row.createCell(15).setCellValue("Acquirer Charges");
					row.createCell(16).setCellValue("Status");
				}

				for (UpiAutoPay autoPay : aaData) {

					row = sheet.createRow(rownum++);
					autoPay.setSrNo(String.valueOf(rownum - 1));
					autoPay.setUserType(userType);
					Object[] objArr = autoPay.methodDownloadAutoPayRegistrationReportForSuperMerchant();

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
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Order Id");
				row.createCell(4).setCellValue("Pg Ref Number");
				row.createCell(5).setCellValue("UMRN Number");
				row.createCell(6).setCellValue("Payment Type");
				row.createCell(7).setCellValue("Create Date");
				row.createCell(8).setCellValue("Cust Email");
				row.createCell(9).setCellValue("Cust Mobile");
				row.createCell(10).setCellValue("Amount");
				row.createCell(11).setCellValue("Debit Amount");
				row.createCell(12).setCellValue("Total Amount");

				if (userType.equalsIgnoreCase(UserType.MERCHANT.toString())
						|| userType.equalsIgnoreCase(UserType.RESELLER.toString())
						|| userType.equalsIgnoreCase(UserType.SUBUSER.toString())) {
					row.createCell(13).setCellValue("Status");
				} else {
					row.createCell(13).setCellValue("Acquirer Charges");
					row.createCell(14).setCellValue("Status");
				}

				for (UpiAutoPay autoPay : aaData) {

					row = sheet.createRow(rownum++);
					autoPay.setSrNo(String.valueOf(rownum - 1));
					autoPay.setUserType(userType);
					Object[] objArr = autoPay.methodDownloadRegistrationReport();

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
				String FILE_EXTENSION = ".xlsx";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Registration_Autopay_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			} catch (Exception ex) {
				logger.info("Exception caught in download autoPay registration report ", ex);
				return ERROR;
			}
		} catch (Exception ex) {
			logger.info("exception caught in downloadRegistrationReport function ", ex);
		}
		return SUCCESS;
	}

	public String downloadAutoPayCapturedRegistrationDetail() {

		List<List<UpiAutoPay>> downloadData = new ArrayList<List<UpiAutoPay>>();
		logger.info("inside downloadAutoPayCapturedRegistrationDetail to download Captured Registration Details");
		String resellerId = "";
		List<String> orderIdList = new ArrayList<String>();

		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}

			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			String txnType = TransactionType.REGISTRATION.getName();
			status = StatusType.CAPTURED.getName();

			orderIdList = upiAutoPayDao.fetchAllCapturedRegistrationOrderId(orderId, umnNumber, merchantPayId,
					subMerchantPayId, resellerId, status, txnType, dateFrom, dateTo);
			for (String orderId : orderIdList) {
				downloadData.add(upiAutoPayDao.fetchAutoPayDebitTransactionDetails(orderId, ""));
			}

			logger.info("List generated successfully for Download Registration Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Debit Report");
			row = sheet.createRow(0);
			if (StringUtils.isNotBlank(subMerchantPayId)) {

				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Sub Merchant Name");
				row.createCell(4).setCellValue("Sub Merchant Pay Id");
				row.createCell(5).setCellValue("Order Id");
				row.createCell(6).setCellValue("Pg Ref Number(Registration)");
				row.createCell(7).setCellValue("Pg Ref Number(Transaction)");
				row.createCell(8).setCellValue("Payment Type");
				row.createCell(9).setCellValue("Registration Date");
				row.createCell(10).setCellValue("Create Date");
				row.createCell(11).setCellValue("Debit Date");
				row.createCell(12).setCellValue("Due Date");
				row.createCell(13).setCellValue("Cust Name");
				row.createCell(14).setCellValue("Cust Email");
				row.createCell(15).setCellValue("Cust Mobile");
				row.createCell(16).setCellValue("Amount");
				row.createCell(17).setCellValue("Debit Amount");
				row.createCell(18).setCellValue("Total Amount");
				row.createCell(19).setCellValue("Status");

				for (List<UpiAutoPay> downloadLst : downloadData) {
					for (UpiAutoPay autoPay : downloadLst) {

						row = sheet.createRow(rownum++);
						autoPay.setSrNo(String.valueOf(rownum - 1));
						Object[] objArr = autoPay.methodDownloadAutoPayRegistrationReportForSuperMerchant();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of that
							// row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}
				}

			} else {
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Order Id");
				row.createCell(4).setCellValue("Pg Ref Number(Registration)");
				row.createCell(5).setCellValue("Pg Ref Number(Transaction)");
				row.createCell(6).setCellValue("Payment Type");
				row.createCell(7).setCellValue("Registration Date");
				row.createCell(8).setCellValue("Create Date");
				row.createCell(9).setCellValue("Debit Date");
				row.createCell(10).setCellValue("Due Date");
				row.createCell(11).setCellValue("UMN Number");
				row.createCell(12).setCellValue("Cust Email");
				row.createCell(13).setCellValue("Cust Mobile");
				row.createCell(14).setCellValue("Amount");
				row.createCell(15).setCellValue("Debit Amount");
				row.createCell(16).setCellValue("Total Amount");
				row.createCell(17).setCellValue("Status");

				for (List<UpiAutoPay> downloadLst : downloadData) {
					for (UpiAutoPay autoPay : downloadLst) {

						row = sheet.createRow(rownum++);
						autoPay.setSrNo(String.valueOf(rownum - 1));
						Object[] objArr = autoPay.methodDownloadAutoPayCapturedRegistrationReport();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of that
							// row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}
				}
			}

			try {
				String FILE_EXTENSION = ".xlsx";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Debit_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");

			} catch (Exception ex) {
				logger.info("Exception caught in download registration report ", ex);
				return ERROR;
			}
		} catch (Exception ex) {
			logger.info("exception has been occured in Download autoPay Registration Report file ", ex);
			return ERROR;
		}
		return SUCCESS;
	}

	public String reSendUpiAutopayLink() {
		logger.info("inside reSendUpiAutopayLink() ");
		Map<String, String> fieldMap = new HashMap<String, String>();

		try {
			fieldMap = txnReport.getUpiAutopayRegistrationDataToResendLink(orderId, pgRefNum);

			String responseBody;
			StringBuilder responseMessageSB = new StringBuilder();

			// Sending Email
			try {
				responseBody = emailServiceProvider.upiAutoPayMandateSign(new Fields(fieldMap));
				//responseBody = emailServiceProvider.reSendUpiAutopayLink(fieldMap);

				if (responseBody != null) {
					responseMessageSB.append(responseBody);
					// responseMessage = responseBody;
				} else {
					responseMessageSB.append("Mail Not Send");
					// responseMessage = "Mail Not Send";
				}

				response = SUCCESS;
			} catch (Exception exception) {
				logger.error("exception is ", exception);
				responseMessageSB.append("Mail Not Send");
				// responseMessage = "Mail Not Send";
				response = "FAIL";
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to emailer");
			}

			responseMessageSB.append(" & ");

			// Sending sms
			try {
				StringBuilder smsBody = new StringBuilder();
				smsBody.append("Dear Customer" + "\n\n"
						+ "Please click on the link below to register for eNach mandate. INR 1 will be deducted from your account to verify your bank account details. ");

				smsBody.append(fieldMap.get(FieldType.EMANDATE_URL.getName()));

				smsBody.append("\n\n--\nTeam Payment Gateway");

				String smsInnuvisolutions = PropertiesManager.propertiesMap
						.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());

				if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
					responseBody = smsSender.sendSMSByInnvisSolution(fieldMap.get(FieldType.CUST_MOBILE.getName()),
							smsBody.toString());
				} else {
					responseBody = smsSender.sendSMS(fieldMap.get(FieldType.CUST_MOBILE.getName()), smsBody.toString());
				}
				if (responseBody == null) {
					// responseMessage = "SMS Not Send";
					responseMessageSB.append("SMS Not Send");
					response = "FAIL";
				} else {
					// responseMessage = ("SMS has been sent to " + custMobile);
					responseMessageSB
							.append("SMS sent to " + field.fieldMask(fieldMap.get(FieldType.CUST_MOBILE.getName())));
					response = SUCCESS;
				}
			} catch (Exception exception) {
				logger.error("exception is ", exception);
				// responseMessage = "SMS Not Send";
				responseMessageSB.append("SMS Not Send");
				response = "FAIL";
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
			}

			responseMessage = responseMessageSB.toString();
			response = SUCCESS;
		} catch (Exception ex) {
			logger.info("exception caught while re-sending EMandate Link ", ex);
			response = "FAIL";
			responseMessage = "Somthing went wrong.";
		}

		return SUCCESS;
	}

	public String downloadAutoPayIndividualTransactionDetail() {

		logger.info("inside downloadAutoPayIndividualTransactionDetail download debit transaction individual ");
		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						// subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}
			}
			aaData = upiAutoPayDao.fetchAutoPayDebitTransactionDetails(orderId, "");

			logger.info("List generated successfully for Download Transaction Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Debit Detail Report");
			row = sheet.createRow(0);

			if (StringUtils.isNotBlank(subMerchantPayId)
					&& !subMerchantPayId.equalsIgnoreCase(Constants.NA.getValue())) {
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Sub Merchant Name");
				row.createCell(4).setCellValue("Sub Merchant Pay Id");
				row.createCell(5).setCellValue("Order Id");
				row.createCell(6).setCellValue("Pg Ref Number(Transaction)");
				row.createCell(7).setCellValue("Payment Type");
				row.createCell(8).setCellValue("Registration Date");
				row.createCell(9).setCellValue("Create Date");
				row.createCell(10).setCellValue("Debit Date");
				row.createCell(11).setCellValue("Due Date");
				row.createCell(12).setCellValue("Cust Email");
				row.createCell(13).setCellValue("Cust Mobile");
				row.createCell(14).setCellValue("Amount");
				row.createCell(15).setCellValue("Debit Amount");
				row.createCell(16).setCellValue("Total Amount");
				row.createCell(17).setCellValue("Status");

				for (UpiAutoPay autopay : aaData) {

					row = sheet.createRow(rownum++);
					autopay.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = autopay.methodDownloadAutoPayTransactionReportIndividualForSuperMerchant();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
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
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Order Id");
				row.createCell(4).setCellValue("Pg Ref Number(Transaction)");
				row.createCell(5).setCellValue("Payment Type");
				row.createCell(6).setCellValue("Registration Date");
				row.createCell(7).setCellValue("Create Date");
				row.createCell(8).setCellValue("Debit Date");
				row.createCell(9).setCellValue("Due Date");
				row.createCell(10).setCellValue("Cust Email");
				row.createCell(11).setCellValue("Cust Mobile");
				row.createCell(12).setCellValue("Amount");
				row.createCell(13).setCellValue("Txn Amount");
				row.createCell(14).setCellValue("Total Amount");
				row.createCell(15).setCellValue("Status");

				for (UpiAutoPay autopay : aaData) {

					row = sheet.createRow(rownum++);
					autopay.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = autopay.methodDownloadAutoPayIndividualTransactionReport();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
			}
			try {
				String FILE_EXTENSION = ".xlsx";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Debit_Detail_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");

			} catch (Exception ex) {
				logger.info("Exception caught in download transaction report ", ex);
				return ERROR;
			}
		} catch (Exception ex) {
			logger.info("exception caught in debitTransaction function ", ex);
		}
		return SUCCESS;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getUmnNumber() {
		return umnNumber;
	}

	public void setUmnNumber(String umnNumber) {
		this.umnNumber = umnNumber;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
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

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
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

	public List<UpiAutoPay> getAaData() {
		return aaData;
	}

	public void setAaData(List<UpiAutoPay> aaData) {
		this.aaData = aaData;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public String getEmandateUrl() {
		return emandateUrl;
	}

	public void setEmandateUrl(String emandateUrl) {
		this.emandateUrl = emandateUrl;
	}


}
