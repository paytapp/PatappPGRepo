package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
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

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.PaymentSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadBookingRecordReportAction extends AbstractSecureAction {

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;
	
	private static final long serialVersionUID = 9215632414506656748L;
	private static Logger logger = LoggerFactory.getLogger(DownloadBookingRecordReportAction.class.getName());

	private String pgRefNum;
	private String payId;
	private String transactionId;
	private String orderId;
	private String categoryCode;
	private String SKUCode;
	private String subMerchantPayId;
	private String merchantPayId;
	private String paymentMethod;
	private String cardNumber;
	private String status;
	private String currency;
	private String partSettleFlag;
	private String dateFrom;
	private String dateTo;
	private String custId;
	private String custMobile;
	private String custEmail;
	private User sessionUser = new User();
	private String transactionType;
	private InputStream fileInputStream;
	private String filename;
	private Set<String> orderIdSet;
	public String subUserPayId;
	private boolean generateReport;
	private String reportType;
	private User subuser = new User();
	
	private User merchant = new User();

	@SuppressWarnings("static-access")
	public String execute() {

		List<PaymentSearchDownloadObject> bookingRecordList = new ArrayList<PaymentSearchDownloadObject>();
		boolean isGlocal = false;
		boolean dispatchSlipFlag = false;

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS);

		String merchPayId = "";
		String subMerchPayId = "";
		if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchPayId = sessionUser.getSuperMerchantId();
			subMerchPayId = sessionUser.getPayId();
		} else {
			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("All")) {
				merchPayId = userDao.getPayIdByEmailId(merchantPayId);
			} else {
				merchPayId = merchantPayId;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				subMerchPayId = userDao.getPayIdByEmailId(subMerchantPayId);
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All")) {
				subMerchPayId = subMerchantPayId;
			}
		}

		if (StringUtils.isNotEmpty(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
			User subuser = userDao.findByEmailId(subUserPayId);
			orderIdSet = txnReports.findBySubuserId(subuser.getPayId(), sessionUser.getParentPayId());
		} else {
			orderIdSet = null;
		}

		if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
			User user = userDao.findPayId(sessionUser.getParentPayId());

			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				merchPayId = user.getSuperMerchantId();
				subMerchPayId = user.getPayId();
			} else {
				merchPayId = user.getPayId();
			}

			String subUserId = sessionUser.getPayId();

			if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

				orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());
				boolean isPgfNumber = txnReports.getPgfNumberForeposSubuser(orderIdSet, pgRefNum);

				boolean orderIdflag = false;
				for (String eposTxnOrderId : orderIdSet) {
					if (StringUtils.isNotEmpty(getOrderId()) && eposTxnOrderId.equalsIgnoreCase(getOrderId())) {
						setOrderId(eposTxnOrderId);
						orderIdflag = true;
						break;
					}
				}

				if (!orderIdflag) {
					if (!getOrderId().isEmpty())
						setOrderId(" ");
				}
				if (!isPgfNumber) {
					if (!pgRefNum.isEmpty())
						setPgRefNum(" ");
				}
				sessionUser = user; 

			} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

				sessionUser = user; 
			}

		}

		if (!merchPayId.equalsIgnoreCase("ALL")) {
			String identifierPayId = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
			if (StringUtils.isNotBlank(identifierPayId) && identifierPayId.contains(merchPayId)) {
				isGlocal = true;
			}
		}

		if (!merchPayId.equalsIgnoreCase("ALL")) {
			String dispatchIdentifierKey = propertiesManager.propertiesMap
					.get(Constants.DISPATCH_SLIP_MERCHANT_PAYID.getValue());
			if (StringUtils.isNotBlank(dispatchIdentifierKey) && dispatchIdentifierKey.contains(merchPayId)) {
				dispatchSlipFlag = true;
			}
		}

		transactionType = TransactionType.SALE.getName();
		status = StatusType.CAPTURED.getName();

		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		bookingRecordList = txnReports.getBillingDetailsForDownload(
				txnReports.getDispatchDetailsForDownloadBookingRecord(txnReports.searchPaymentDownloadForBookingRecord(
						merchPayId, subMerchPayId, pgRefNum.trim(), orderId.trim(), custMobile, custEmail.trim(),
						paymentMethod, status, currency, transactionType, dateFrom, dateTo, partSettleFlag, isGlocal,
						sessionUser, dispatchSlipFlag, orderIdSet)));

		logger.info("List generated successfully for Booking Record Report");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Booking Record Report");
		row = sheet.createRow(0);

		if (isGlocal == true && StringUtils.isNotBlank(subMerchPayId) && dispatchSlipFlag == true) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Sub Merchant");
			row.createCell(5).setCellValue("Invoice Number");
			row.createCell(6).setCellValue("Dispatch Slip ID");
			row.createCell(7).setCellValue("Courier Name");
			row.createCell(8).setCellValue("Captured Date");
			row.createCell(9).setCellValue("Order Id");
			row.createCell(10).setCellValue("Payment Method");
			row.createCell(11).setCellValue("MopType");
			row.createCell(12).setCellValue("Mask");
			row.createCell(13).setCellValue("Cust Name");
			row.createCell(14).setCellValue("Cust Mobile");
			row.createCell(15).setCellValue("Cust Email");
			row.createCell(16).setCellValue("CardHolder Type");
			row.createCell(17).setCellValue("Txn Type");
			row.createCell(18).setCellValue("Transaction Mode");
			row.createCell(19).setCellValue("Status");
			row.createCell(20).setCellValue("Transaction Region");
			row.createCell(21).setCellValue("Base Amount");
			row.createCell(22).setCellValue("Total Amount");
			row.createCell(23).setCellValue("TDR / Surcharge");
			row.createCell(24).setCellValue("GST");
			row.createCell(25).setCellValue("Doctor");
			row.createCell(26).setCellValue("Glocal");
			row.createCell(27).setCellValue("Partner");
			row.createCell(28).setCellValue("Unique ID");
			row.createCell(29).setCellValue("Merchant Amount");
			row.createCell(30).setCellValue("Transaction Flag");
			row.createCell(31).setCellValue("Part Settled Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(32).setCellValue("Category Code");
				row.createCell(33).setCellValue("SKU Code");
				row.createCell(34).setCellValue("Refund Cycle");
				row.createCell(35).setCellValue("Product Price");
				row.createCell(36).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(32).setCellValue("Category Code");
						row.createCell(33).setCellValue("SKU Code");
						row.createCell(34).setCellValue("Refund Cycle");
						row.createCell(35).setCellValue("Product Price");
						row.createCell(36).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(32).setCellValue("Category Code");
						row.createCell(33).setCellValue("SKU Code");
						row.createCell(34).setCellValue("Refund Cycle");
						row.createCell(35).setCellValue("Product Price");
						row.createCell(36).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch
						.myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchantWithGlocalAndDispatchSlip(
								merchantPayId, sessionUser, merchant);

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

		} else if (isGlocal == true && StringUtils.isNotBlank(subMerchPayId)) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Sub Merchant");
			row.createCell(5).setCellValue("Captured Date");
			row.createCell(6).setCellValue("Order Id");
			row.createCell(7).setCellValue("Payment Method");
			row.createCell(8).setCellValue("MopType");
			row.createCell(9).setCellValue("Mask");
			row.createCell(10).setCellValue("Cust Name");
			row.createCell(11).setCellValue("Cust Mobile");
			row.createCell(12).setCellValue("Cust Email");
			row.createCell(13).setCellValue("CardHolder Type");
			row.createCell(14).setCellValue("Txn Type");
			row.createCell(15).setCellValue("Transaction Mode");
			row.createCell(16).setCellValue("Status");
			row.createCell(17).setCellValue("Transaction Region");
			row.createCell(18).setCellValue("Base Amount");
			row.createCell(19).setCellValue("Total Amount");
			row.createCell(20).setCellValue("TDR / Surcharge");
			row.createCell(21).setCellValue("GST");
			row.createCell(22).setCellValue("Doctor");
			row.createCell(23).setCellValue("Glocal");
			row.createCell(24).setCellValue("Partner");
			row.createCell(25).setCellValue("Unique ID");
			row.createCell(26).setCellValue("Merchant Amount");
			row.createCell(27).setCellValue("Transaction Flag");
			row.createCell(28).setCellValue("Part Settled Flag");
			// row.createCell(27).setCellValue("Custom Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(29).setCellValue("Category Code");
				row.createCell(30).setCellValue("SKU Code");
				row.createCell(31).setCellValue("Refund Cycle");
				row.createCell(32).setCellValue("Product Price");
				row.createCell(33).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
					UserSettingData merchantSetting = userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSetting.isRetailMerchantFlag()) {
						row.createCell(29).setCellValue("Category Code");
						row.createCell(30).setCellValue("SKU Code");
						row.createCell(31).setCellValue("Refund Cycle");
						row.createCell(32).setCellValue("Product Price");
						row.createCell(33).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(29).setCellValue("Category Code");
						row.createCell(30).setCellValue("SKU Code");
						row.createCell(31).setCellValue("Refund Cycle");
						row.createCell(32).setCellValue("Product Price");
						row.createCell(33).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch
						.myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchantWithGlocal(merchantPayId,
								sessionUser, merchant);

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

		} else if (isGlocal == false && StringUtils.isNotBlank(subMerchPayId) && dispatchSlipFlag == true) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Sub Merchant");
			row.createCell(5).setCellValue("Invoice Number");
			row.createCell(6).setCellValue("Dispatch Slip ID");
			row.createCell(7).setCellValue("Courier Name");
			row.createCell(8).setCellValue("Captured Date");
			row.createCell(9).setCellValue("Order Id");
			row.createCell(10).setCellValue("Payment Method");
			row.createCell(11).setCellValue("MopType");
			row.createCell(12).setCellValue("Mask");
			row.createCell(13).setCellValue("Cust Name");
			row.createCell(14).setCellValue("Cust Mobile");
			row.createCell(15).setCellValue("Cust Email");
			row.createCell(16).setCellValue("CardHolder Type");
			row.createCell(17).setCellValue("Txn Type");
			row.createCell(18).setCellValue("Transaction Mode");
			row.createCell(19).setCellValue("Status");
			row.createCell(20).setCellValue("Transaction Region");
			row.createCell(21).setCellValue("Base Amount");
			row.createCell(22).setCellValue("Total Amount");
			row.createCell(23).setCellValue("TDR / Surcharge");
			row.createCell(24).setCellValue("GST");
			row.createCell(25).setCellValue("Merchant Amount");
			row.createCell(26).setCellValue("Transaction Flag");
			row.createCell(27).setCellValue("Part Settled Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(28).setCellValue("Category Code");
				row.createCell(29).setCellValue("SKU Code");
				row.createCell(30).setCellValue("Refund Cycle");
				row.createCell(31).setCellValue("Product Price");
				row.createCell(32).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
					
					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch
						.myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchantAndDispatchSlip(merchantPayId,
								sessionUser, merchant);

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
		} else if (isGlocal == false && StringUtils.isNotBlank(subMerchPayId)) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Sub Merchant");
			row.createCell(5).setCellValue("Captured Date");
			row.createCell(6).setCellValue("Order Id");
			row.createCell(7).setCellValue("Payment Method");
			row.createCell(8).setCellValue("MopType");
			row.createCell(9).setCellValue("Mask");
			row.createCell(10).setCellValue("Cust Name");
			row.createCell(11).setCellValue("Cust Mobile");
			row.createCell(12).setCellValue("Cust Email");
			row.createCell(13).setCellValue("CardHolder Type");
			row.createCell(14).setCellValue("Txn Type");
			row.createCell(15).setCellValue("Transaction Mode");
			row.createCell(16).setCellValue("Status");
			row.createCell(17).setCellValue("Transaction Region");
			row.createCell(18).setCellValue("Base Amount");
			row.createCell(19).setCellValue("Total Amount");
			row.createCell(20).setCellValue("TDR / Surcharge");
			row.createCell(21).setCellValue("GST");
			row.createCell(22).setCellValue("Merchant Amount");
			row.createCell(23).setCellValue("Transaction Flag");
			row.createCell(24).setCellValue("Part Settled Flag");
			// row.createCell23).setCellValue("Custom Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(25).setCellValue("Category Code");
				row.createCell(26).setCellValue("SKU Code");
				row.createCell(27).setCellValue("Refund Cycle");
				row.createCell(28).setCellValue("Product Price");
				row.createCell(29).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(25).setCellValue("Category Code");
						row.createCell(26).setCellValue("SKU Code");
						row.createCell(27).setCellValue("Refund Cycle");
						row.createCell(28).setCellValue("Product Price");
						row.createCell(29).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(25).setCellValue("Category Code");
						row.createCell(26).setCellValue("SKU Code");
						row.createCell(27).setCellValue("Refund Cycle");
						row.createCell(28).setCellValue("Product Price");
						row.createCell(29).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchant(
						merchantPayId, sessionUser, merchant);

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
		} else if (isGlocal == false && dispatchSlipFlag == true
				&& (StringUtils.isNotBlank(orderId) || StringUtils.isNotBlank(pgRefNum))) {

			for (PaymentSearchDownloadObject bookingRecord : bookingRecordList) {
				if (StringUtils.isNotBlank(bookingRecord.getSubMerchantId())) {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Sub Merchant");
					row.createCell(5).setCellValue("Invoice Number");
					row.createCell(6).setCellValue("Dispatch Slip ID");
					row.createCell(7).setCellValue("Courier Name");
					row.createCell(8).setCellValue("Captured Date");
					row.createCell(9).setCellValue("Order Id");
					row.createCell(10).setCellValue("Payment Method");
					row.createCell(11).setCellValue("MopType");
					row.createCell(12).setCellValue("Mask");
					row.createCell(13).setCellValue("Cust Name");
					row.createCell(14).setCellValue("Cust Mobile");
					row.createCell(15).setCellValue("Cust Email");
					row.createCell(16).setCellValue("CardHolder Type");
					row.createCell(17).setCellValue("Txn Type");
					row.createCell(18).setCellValue("Transaction Mode");
					row.createCell(19).setCellValue("Status");
					row.createCell(20).setCellValue("Transaction Region");
					row.createCell(21).setCellValue("Base Amount");
					row.createCell(22).setCellValue("Total Amount");
					row.createCell(23).setCellValue("TDR / Surcharge");
					row.createCell(24).setCellValue("GST");
					row.createCell(25).setCellValue("Merchant Amount");
					row.createCell(26).setCellValue("Transaction Flag");
					row.createCell(27).setCellValue("Part Settled Flag");

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = bookingRecord.getUser();
						merchantPayId = merchant.getPayId();
						
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchantPayId);
						
						if (merchantSettings.isRetailMerchantFlag()) {
							row.createCell(28).setCellValue("Category Code");
							row.createCell(29).setCellValue("SKU Code");
							row.createCell(30).setCellValue("Refund Cycle");
							row.createCell(31).setCellValue("Product Price");
							row.createCell(32).setCellValue("Vendor Id");
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							row.createCell(28).setCellValue("Category Code");
							row.createCell(29).setCellValue("SKU Code");
							row.createCell(30).setCellValue("Refund Cycle");
							row.createCell(31).setCellValue("Product Price");
							row.createCell(32).setCellValue("Vendor Id");
						}

					}

				} else {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Invoice Number");
					row.createCell(5).setCellValue("Dispatch Slip ID");
					row.createCell(6).setCellValue("Courier Name");
					row.createCell(7).setCellValue("Captured Date");
					row.createCell(8).setCellValue("Order Id");
					row.createCell(9).setCellValue("Payment Method");
					row.createCell(10).setCellValue("MopType");
					row.createCell(11).setCellValue("Mask");
					row.createCell(12).setCellValue("Cust Name");
					row.createCell(13).setCellValue("Cust Mobile");
					row.createCell(14).setCellValue("Cust Email");
					row.createCell(15).setCellValue("CardHolder Type");
					row.createCell(16).setCellValue("Txn Type");
					row.createCell(17).setCellValue("Transaction Mode");
					row.createCell(18).setCellValue("Status");
					row.createCell(19).setCellValue("Transaction Region");
					row.createCell(20).setCellValue("Base Amount");
					row.createCell(21).setCellValue("Total Amount");
					row.createCell(22).setCellValue("TDR / Surcharge");
					row.createCell(23).setCellValue("GST");
					row.createCell(24).setCellValue("Merchant Amount");
					row.createCell(25).setCellValue("Transaction Flag");
					row.createCell(26).setCellValue("Part Settled Flag");

					if (merchantPayId.equalsIgnoreCase("ALL")) {

						row.createCell(27).setCellValue("Category Code");
						row.createCell(28).setCellValue("SKU Code");
						row.createCell(29).setCellValue("Refund Cycle");
						row.createCell(30).setCellValue("Product Price");
						row.createCell(31).setCellValue("Vendor Id");
					} else {
						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)
								|| sessionUser.getUserType().equals(UserType.RESELLER)) {
							merchant = userDao.findByEmailId(merchantPayId);
							UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

							if (merchantSettings.isRetailMerchantFlag()) {
								row.createCell(27).setCellValue("Category Code");
								row.createCell(28).setCellValue("SKU Code");
								row.createCell(29).setCellValue("Refund Cycle");
								row.createCell(30).setCellValue("Product Price");
								row.createCell(31).setCellValue("Vendor Id");
							}
						} else {

							if (userSettings.isRetailMerchantFlag()) {
								row.createCell(27).setCellValue("Category Code");
								row.createCell(28).setCellValue("SKU Code");
								row.createCell(29).setCellValue("Refund Cycle");
								row.createCell(30).setCellValue("Product Price");
								row.createCell(31).setCellValue("Vendor Id");
							}

						}
					}
				}
				break;
			}
			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr;
				if (StringUtils.isNotBlank(transactionSearch.getSubMerchantId())) {
					objArr = transactionSearch
							.myCsvMethodDownloadBookingPaymentsReportCapturedForDispatchSlipAndOrderIdOrPgRefNum(
									merchantPayId, sessionUser, merchant);
				} else {
					objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedForDispatchSlip(
							merchantPayId, sessionUser, merchant);
				}

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
		} else if (isGlocal == false && dispatchSlipFlag == true) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Invoice Number");
			row.createCell(5).setCellValue("Dispatch Slip ID");
			row.createCell(6).setCellValue("Courier Name");
			row.createCell(7).setCellValue("Captured Date");
			row.createCell(8).setCellValue("Order Id");
			row.createCell(9).setCellValue("Payment Method");
			row.createCell(10).setCellValue("MopType");
			row.createCell(11).setCellValue("Mask");
			row.createCell(12).setCellValue("Cust Name");
			row.createCell(13).setCellValue("Cust Mobile");
			row.createCell(14).setCellValue("Cust Email");
			row.createCell(15).setCellValue("CardHolder Type");
			row.createCell(16).setCellValue("Txn Type");
			row.createCell(17).setCellValue("Transaction Mode");
			row.createCell(18).setCellValue("Status");
			row.createCell(19).setCellValue("Transaction Region");
			row.createCell(20).setCellValue("Base Amount");
			row.createCell(21).setCellValue("Total Amount");
			row.createCell(22).setCellValue("TDR / Surcharge");
			row.createCell(23).setCellValue("GST");
			row.createCell(24).setCellValue("Merchant Amount");
			row.createCell(25).setCellValue("Transaction Flag");
			row.createCell(26).setCellValue("Part Settled Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(27).setCellValue("Category Code");
				row.createCell(28).setCellValue("SKU Code");
				row.createCell(29).setCellValue("Refund Cycle");
				row.createCell(30).setCellValue("Product Price");
				row.createCell(31).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(27).setCellValue("Category Code");
						row.createCell(28).setCellValue("SKU Code");
						row.createCell(29).setCellValue("Refund Cycle");
						row.createCell(30).setCellValue("Product Price");
						row.createCell(31).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(27).setCellValue("Category Code");
						row.createCell(28).setCellValue("SKU Code");
						row.createCell(29).setCellValue("Refund Cycle");
						row.createCell(30).setCellValue("Product Price");
						row.createCell(31).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedForDispatchSlip(
						merchantPayId, sessionUser, merchant);

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
		} else if (isGlocal == true && dispatchSlipFlag == true
				&& (StringUtils.isNotBlank(orderId) || StringUtils.isNotBlank(pgRefNum))) {

			for (PaymentSearchDownloadObject bookingRecord : bookingRecordList) {
				if (StringUtils.isNotBlank(bookingRecord.getSubMerchantId())) {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Sub Merchant");
					row.createCell(5).setCellValue("Invoice Number");
					row.createCell(6).setCellValue("Dispatch Slip ID");
					row.createCell(7).setCellValue("Courier Name");
					row.createCell(8).setCellValue("Captured Date");
					row.createCell(9).setCellValue("Order Id");
					row.createCell(10).setCellValue("Payment Method");
					row.createCell(11).setCellValue("MopType");
					row.createCell(12).setCellValue("Mask");
					row.createCell(13).setCellValue("Cust Name");
					row.createCell(14).setCellValue("Cust Mobile");
					row.createCell(15).setCellValue("Cust Email");
					row.createCell(16).setCellValue("CardHolder Type");
					row.createCell(17).setCellValue("Txn Type");
					row.createCell(18).setCellValue("Transaction Mode");
					row.createCell(19).setCellValue("Status");
					row.createCell(20).setCellValue("Transaction Region");
					row.createCell(21).setCellValue("Base Amount");
					row.createCell(22).setCellValue("Total Amount");
					row.createCell(23).setCellValue("TDR / Surcharge");
					row.createCell(24).setCellValue("GST");
					row.createCell(25).setCellValue("Doctor");
					row.createCell(26).setCellValue("Glocal");
					row.createCell(27).setCellValue("Partner");
					row.createCell(28).setCellValue("Unique ID");
					row.createCell(29).setCellValue("Merchant Amount");
					row.createCell(30).setCellValue("Transaction Flag");
					row.createCell(31).setCellValue("Part Settled Flag");

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = bookingRecord.getUser();
						merchantPayId = merchant.getPayId();
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchantPayId);
						
						if (merchantSettings.isRetailMerchantFlag()) {
							row.createCell(32).setCellValue("Category Code");
							row.createCell(33).setCellValue("SKU Code");
							row.createCell(34).setCellValue("Refund Cycle");
							row.createCell(35).setCellValue("Product Price");
							row.createCell(36).setCellValue("Vendor Id");
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							row.createCell(32).setCellValue("Category Code");
							row.createCell(33).setCellValue("SKU Code");
							row.createCell(34).setCellValue("Refund Cycle");
							row.createCell(35).setCellValue("Product Price");
							row.createCell(36).setCellValue("Vendor Id");
						}

					}
				} else {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Invoice Number");
					row.createCell(5).setCellValue("Dispatch Slip ID");
					row.createCell(6).setCellValue("Courier Name");
					row.createCell(7).setCellValue("Captured Date");
					row.createCell(8).setCellValue("Order Id");
					row.createCell(9).setCellValue("Payment Method");
					row.createCell(10).setCellValue("MopType");
					row.createCell(11).setCellValue("Mask");
					row.createCell(12).setCellValue("Cust Name");
					row.createCell(13).setCellValue("Cust Mobile");
					row.createCell(14).setCellValue("Cust Email");
					row.createCell(15).setCellValue("CardHolder Type");
					row.createCell(16).setCellValue("Txn Type");
					row.createCell(17).setCellValue("Transaction Mode");
					row.createCell(18).setCellValue("Status");
					row.createCell(19).setCellValue("Transaction Region");
					row.createCell(20).setCellValue("Base Amount");
					row.createCell(21).setCellValue("Total Amount");
					row.createCell(22).setCellValue("TDR / Surcharge");
					row.createCell(23).setCellValue("GST");
					row.createCell(24).setCellValue("Doctor");
					row.createCell(25).setCellValue("Glocal");
					row.createCell(26).setCellValue("Partner");
					row.createCell(27).setCellValue("Unique ID");
					row.createCell(28).setCellValue("Merchant Amount");
					row.createCell(29).setCellValue("Transaction Flag");
					row.createCell(30).setCellValue("Part Settled Flag");

					if (merchantPayId.equalsIgnoreCase("ALL")) {

						row.createCell(31).setCellValue("Category Code");
						row.createCell(32).setCellValue("SKU Code");
						row.createCell(33).setCellValue("Refund Cycle");
						row.createCell(34).setCellValue("Product Price");
						row.createCell(35).setCellValue("Vendor Id");
					} else {
						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)
								|| sessionUser.getUserType().equals(UserType.RESELLER)) {
							merchant = userDao.findByEmailId(merchantPayId);
							
							UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

							if (merchantSettings.isRetailMerchantFlag()) {
								row.createCell(31).setCellValue("Category Code");
								row.createCell(32).setCellValue("SKU Code");
								row.createCell(33).setCellValue("Refund Cycle");
								row.createCell(34).setCellValue("Product Price");
								row.createCell(35).setCellValue("Vendor Id");
							}
						} else {

							if (userSettings.isRetailMerchantFlag()) {
								row.createCell(31).setCellValue("Category Code");
								row.createCell(32).setCellValue("SKU Code");
								row.createCell(33).setCellValue("Refund Cycle");
								row.createCell(34).setCellValue("Product Price");
								row.createCell(35).setCellValue("Vendor Id");
							}

						}
					}
				}
				break;
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr;
				if (StringUtils.isNotBlank(transactionSearch.getSubMerchantId())) {
					objArr = transactionSearch
							.myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndDispatchSlipAndOrderIdOrPgRefNum(
									merchantPayId, sessionUser, merchant);
				} else {
					objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndDispatchSlip(
							merchantPayId, sessionUser, merchant);
				}

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
		} else if (isGlocal == true && dispatchSlipFlag == true) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Invoice Number");
			row.createCell(5).setCellValue("Dispatch Slip ID");
			row.createCell(6).setCellValue("Courier Name");
			row.createCell(7).setCellValue("Captured Date");
			row.createCell(8).setCellValue("Order Id");
			row.createCell(9).setCellValue("Payment Method");
			row.createCell(10).setCellValue("MopType");
			row.createCell(11).setCellValue("Mask");
			row.createCell(12).setCellValue("Cust Name");
			row.createCell(13).setCellValue("Cust Mobile");
			row.createCell(14).setCellValue("Cust Email");
			row.createCell(15).setCellValue("CardHolder Type");
			row.createCell(16).setCellValue("Txn Type");
			row.createCell(17).setCellValue("Transaction Mode");
			row.createCell(18).setCellValue("Status");
			row.createCell(19).setCellValue("Transaction Region");
			row.createCell(20).setCellValue("Base Amount");
			row.createCell(21).setCellValue("Total Amount");
			row.createCell(22).setCellValue("TDR / Surcharge");
			row.createCell(23).setCellValue("GST");
			row.createCell(24).setCellValue("Doctor");
			row.createCell(25).setCellValue("Glocal");
			row.createCell(26).setCellValue("Partner");
			row.createCell(27).setCellValue("Unique ID");
			row.createCell(28).setCellValue("Merchant Amount");
			row.createCell(29).setCellValue("Transaction Flag");
			row.createCell(30).setCellValue("Part Settled Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(31).setCellValue("Category Code");
				row.createCell(32).setCellValue("SKU Code");
				row.createCell(33).setCellValue("Refund Cycle");
				row.createCell(34).setCellValue("Product Price");
				row.createCell(35).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(31).setCellValue("Category Code");
						row.createCell(32).setCellValue("SKU Code");
						row.createCell(33).setCellValue("Refund Cycle");
						row.createCell(34).setCellValue("Product Price");
						row.createCell(35).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(31).setCellValue("Category Code");
						row.createCell(32).setCellValue("SKU Code");
						row.createCell(33).setCellValue("Refund Cycle");
						row.createCell(34).setCellValue("Product Price");
						row.createCell(35).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch
						.myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndDispatchSlip(merchantPayId,
								sessionUser, merchant);

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
		} else if (isGlocal == false && (StringUtils.isNotBlank(orderId) || StringUtils.isNotBlank(pgRefNum))) {

			for (PaymentSearchDownloadObject bookingRecord : bookingRecordList) {
				if (StringUtils.isNotBlank(bookingRecord.getSubMerchantId())) {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Sub Merchant");
					row.createCell(5).setCellValue("Captured Date");
					row.createCell(6).setCellValue("Order Id");
					row.createCell(7).setCellValue("Payment Method");
					row.createCell(8).setCellValue("MopType");
					row.createCell(9).setCellValue("Mask");
					row.createCell(10).setCellValue("Cust Name");
					row.createCell(11).setCellValue("Cust Mobile");
					row.createCell(12).setCellValue("Cust Email");
					row.createCell(13).setCellValue("CardHolder Type");
					row.createCell(14).setCellValue("Txn Type");
					row.createCell(15).setCellValue("Transaction Mode");
					row.createCell(16).setCellValue("Status");
					row.createCell(17).setCellValue("Transaction Region");
					row.createCell(18).setCellValue("Base Amount");
					row.createCell(19).setCellValue("Total Amount");
					row.createCell(20).setCellValue("TDR / Surcharge");
					row.createCell(21).setCellValue("GST");
					row.createCell(22).setCellValue("Merchant Amount");
					row.createCell(23).setCellValue("Transaction Flag");
					row.createCell(24).setCellValue("Part Settled Flag");

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = bookingRecord.getUser();
						merchantPayId = merchant.getPayId();
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchantPayId);
						if (merchantSettings.isRetailMerchantFlag()) {
							row.createCell(25).setCellValue("Category Code");
							row.createCell(26).setCellValue("SKU Code");
							row.createCell(27).setCellValue("Refund Cycle");
							row.createCell(28).setCellValue("Product Price");
							row.createCell(29).setCellValue("Vendor Id");
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							row.createCell(25).setCellValue("Category Code");
							row.createCell(26).setCellValue("SKU Code");
							row.createCell(27).setCellValue("Refund Cycle");
							row.createCell(28).setCellValue("Product Price");
							row.createCell(29).setCellValue("Vendor Id");
						}

					}

				} else {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Captured Date");
					row.createCell(5).setCellValue("Order Id");
					row.createCell(6).setCellValue("Payment Method");
					row.createCell(7).setCellValue("MopType");
					row.createCell(8).setCellValue("Mask");
					row.createCell(9).setCellValue("Cust Name");
					row.createCell(10).setCellValue("Cust Mobile");
					row.createCell(11).setCellValue("Cust Email");
					row.createCell(12).setCellValue("CardHolder Type");
					row.createCell(13).setCellValue("Txn Type");
					row.createCell(14).setCellValue("Transaction Mode");
					row.createCell(15).setCellValue("Status");
					row.createCell(16).setCellValue("Transaction Region");
					row.createCell(17).setCellValue("Base Amount");
					row.createCell(18).setCellValue("Total Amount");
					row.createCell(19).setCellValue("TDR / Surcharge");
					row.createCell(20).setCellValue("GST");
					row.createCell(21).setCellValue("Merchant Amount");
					row.createCell(22).setCellValue("Transaction Flag");
					row.createCell(23).setCellValue("Part Settled Flag");

					if (merchantPayId.equalsIgnoreCase("ALL")) {

						row.createCell(24).setCellValue("Category Code");
						row.createCell(25).setCellValue("SKU Code");
						row.createCell(26).setCellValue("Refund Cycle");
						row.createCell(27).setCellValue("Product Price");
						row.createCell(28).setCellValue("Vendor Id");
					} else {
						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)
								|| sessionUser.getUserType().equals(UserType.RESELLER)) {
							merchant = userDao.findByEmailId(merchantPayId);UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

							if (merchantSettings.isRetailMerchantFlag()) {
								row.createCell(24).setCellValue("Category Code");
								row.createCell(25).setCellValue("SKU Code");
								row.createCell(26).setCellValue("Refund Cycle");
								row.createCell(27).setCellValue("Product Price");
								row.createCell(28).setCellValue("Vendor Id");
							}
						} else {

							if (userSettings.isRetailMerchantFlag()) {
								row.createCell(24).setCellValue("Category Code");
								row.createCell(25).setCellValue("SKU Code");
								row.createCell(26).setCellValue("Refund Cycle");
								row.createCell(27).setCellValue("Product Price");
								row.createCell(28).setCellValue("Vendor Id");
							}

						}
					}
				}
				break;
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr;
				if (StringUtils.isNotBlank(transactionSearch.getSubMerchantId())) {
					objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedAndOrderIdOrPgRefNum(
							merchantPayId, sessionUser, merchant);
				} else {
					objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCaptured(merchantPayId,
							sessionUser, merchant);
				}

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
		} else if (isGlocal == false) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Captured Date");
			row.createCell(5).setCellValue("Order Id");
			row.createCell(6).setCellValue("Payment Method");
			row.createCell(7).setCellValue("MopType");
			row.createCell(8).setCellValue("Mask");
			row.createCell(9).setCellValue("Cust Name");
			row.createCell(10).setCellValue("Cust Mobile");
			row.createCell(11).setCellValue("Cust Email");
			row.createCell(12).setCellValue("CardHolder Type");
			row.createCell(13).setCellValue("Txn Type");
			row.createCell(14).setCellValue("Transaction Mode");
			row.createCell(15).setCellValue("Status");
			row.createCell(16).setCellValue("Transaction Region");
			row.createCell(17).setCellValue("Base Amount");
			row.createCell(18).setCellValue("Total Amount");
			row.createCell(19).setCellValue("TDR / Surcharge");
			row.createCell(20).setCellValue("GST");
			row.createCell(21).setCellValue("Merchant Amount");
			row.createCell(22).setCellValue("Transaction Flag");
			row.createCell(23).setCellValue("Part Settled Flag");
			// row.createCell43).setCellValue("Custom Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(24).setCellValue("Category Code");
				row.createCell(25).setCellValue("SKU Code");
				row.createCell(26).setCellValue("Refund Cycle");
				row.createCell(27).setCellValue("Product Price");
				row.createCell(28).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(24).setCellValue("Category Code");
						row.createCell(25).setCellValue("SKU Code");
						row.createCell(26).setCellValue("Refund Cycle");
						row.createCell(27).setCellValue("Product Price");
						row.createCell(28).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(24).setCellValue("Category Code");
						row.createCell(25).setCellValue("SKU Code");
						row.createCell(26).setCellValue("Refund Cycle");
						row.createCell(27).setCellValue("Product Price");
						row.createCell(28).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCaptured(merchantPayId,
						sessionUser, merchant);

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
		} else if (isGlocal == true && dispatchSlipFlag == false
				&& (StringUtils.isNotBlank(orderId) || StringUtils.isNotBlank(pgRefNum))) {

			for (PaymentSearchDownloadObject bookingRecord : bookingRecordList) {
				if (StringUtils.isNotBlank(bookingRecord.getSubMerchantId())) {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Sub Merchant");
					row.createCell(5).setCellValue("Captured Date");
					row.createCell(6).setCellValue("Order Id");
					row.createCell(7).setCellValue("Payment Method");
					row.createCell(8).setCellValue("MopType");
					row.createCell(9).setCellValue("Mask");
					row.createCell(10).setCellValue("Cust Name");
					row.createCell(11).setCellValue("Cust Mobile");
					row.createCell(12).setCellValue("Cust Email");
					row.createCell(13).setCellValue("CardHolder Type");
					row.createCell(14).setCellValue("Txn Type");
					row.createCell(15).setCellValue("Transaction Mode");
					row.createCell(16).setCellValue("Status");
					row.createCell(17).setCellValue("Transaction Region");
					row.createCell(18).setCellValue("Base Amount");
					row.createCell(19).setCellValue("Total Amount");
					row.createCell(20).setCellValue("TDR / Surcharge");
					row.createCell(21).setCellValue("GST");
					row.createCell(22).setCellValue("Doctor");
					row.createCell(23).setCellValue("Glocal");
					row.createCell(24).setCellValue("Partner");
					row.createCell(25).setCellValue("Unique ID");
					row.createCell(26).setCellValue("Merchant Amount");
					row.createCell(27).setCellValue("Transaction Flag");
					row.createCell(28).setCellValue("Part Settled Flag");

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = bookingRecord.getUser();
						merchantPayId = merchant.getPayId();
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchantPayId);
						if (merchantSettings.isRetailMerchantFlag()) {
							row.createCell(29).setCellValue("Category Code");
							row.createCell(30).setCellValue("SKU Code");
							row.createCell(31).setCellValue("Refund Cycle");
							row.createCell(32).setCellValue("Product Price");
							row.createCell(33).setCellValue("Vendor Id");
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							row.createCell(29).setCellValue("Category Code");
							row.createCell(30).setCellValue("SKU Code");
							row.createCell(31).setCellValue("Refund Cycle");
							row.createCell(32).setCellValue("Product Price");
							row.createCell(33).setCellValue("Vendor Id");
						}

					}

				} else {
					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Captured Date");
					row.createCell(5).setCellValue("Order Id");
					row.createCell(6).setCellValue("Payment Method");
					row.createCell(7).setCellValue("MopType");
					row.createCell(8).setCellValue("Mask");
					row.createCell(9).setCellValue("Cust Name");
					row.createCell(10).setCellValue("Cust Mobile");
					row.createCell(11).setCellValue("Cust Email");
					row.createCell(12).setCellValue("CardHolder Type");
					row.createCell(13).setCellValue("Txn Type");
					row.createCell(14).setCellValue("Transaction Mode");
					row.createCell(15).setCellValue("Status");
					row.createCell(16).setCellValue("Transaction Region");
					row.createCell(17).setCellValue("Base Amount");
					row.createCell(18).setCellValue("Total Amount");
					row.createCell(19).setCellValue("TDR / Surcharge");
					row.createCell(20).setCellValue("GST");
					row.createCell(21).setCellValue("Doctor");
					row.createCell(22).setCellValue("Glocal");
					row.createCell(23).setCellValue("Partner");
					row.createCell(24).setCellValue("Unique ID");
					row.createCell(25).setCellValue("Merchant Amount");
					row.createCell(26).setCellValue("Transaction Flag");
					row.createCell(27).setCellValue("Part Settled Flag");

					if (merchantPayId.equalsIgnoreCase("ALL")) {

						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					} else {
						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)
								|| sessionUser.getUserType().equals(UserType.RESELLER)) {
							merchant = userDao.findByEmailId(merchantPayId);UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

							if (merchantSettings.isRetailMerchantFlag()) {
								row.createCell(28).setCellValue("Category Code");
								row.createCell(29).setCellValue("SKU Code");
								row.createCell(30).setCellValue("Refund Cycle");
								row.createCell(31).setCellValue("Product Price");
								row.createCell(32).setCellValue("Vendor Id");
							}
						} else {

							if (userSettings.isRetailMerchantFlag()) {
								row.createCell(28).setCellValue("Category Code");
								row.createCell(29).setCellValue("SKU Code");
								row.createCell(30).setCellValue("Refund Cycle");
								row.createCell(31).setCellValue("Product Price");
								row.createCell(32).setCellValue("Vendor Id");
							}

						}
					}
				}
				break;
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr;
				if (StringUtils.isNotBlank(transactionSearch.getSubMerchantId())) {
					objArr = transactionSearch
							.myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndOrderIdOrPgRefNum(
									merchantPayId, sessionUser, merchant);
				} else {
					objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedForGlocal(merchantPayId,
							sessionUser, merchant);
				}

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
		} else if (isGlocal == true && dispatchSlipFlag == false) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Captured Date");
			row.createCell(5).setCellValue("Order Id");
			row.createCell(6).setCellValue("Payment Method");
			row.createCell(7).setCellValue("MopType");
			row.createCell(8).setCellValue("Mask");
			row.createCell(9).setCellValue("Cust Name");
			row.createCell(10).setCellValue("Cust Mobile");
			row.createCell(11).setCellValue("Cust Email");
			row.createCell(12).setCellValue("CardHolder Type");
			row.createCell(13).setCellValue("Txn Type");
			row.createCell(14).setCellValue("Transaction Mode");
			row.createCell(15).setCellValue("Status");
			row.createCell(16).setCellValue("Transaction Region");
			row.createCell(17).setCellValue("Base Amount");
			row.createCell(18).setCellValue("Total Amount");
			row.createCell(19).setCellValue("TDR / Surcharge");
			row.createCell(20).setCellValue("GST");
			row.createCell(21).setCellValue("Doctor");
			row.createCell(22).setCellValue("Glocal");
			row.createCell(23).setCellValue("Partner");
			row.createCell(24).setCellValue("Unique ID");
			row.createCell(25).setCellValue("Merchant Amount");
			row.createCell(26).setCellValue("Post Settled Flag");
			row.createCell(27).setCellValue("Part Settled Flag");
			// row.createCell(23).setCellValue("Custom Flag");

			if (merchantPayId.equalsIgnoreCase("ALL")) {

				row.createCell(28).setCellValue("Category Code");
				row.createCell(29).setCellValue("SKU Code");
				row.createCell(30).setCellValue("Refund Cycle");
				row.createCell(31).setCellValue("Product Price");
				row.createCell(32).setCellValue("Vendor Id");
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findByEmailId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (merchantSettings.isRetailMerchantFlag()) {
						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					}
				} else {

					if (userSettings.isRetailMerchantFlag()) {
						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					}

				}
			}

			for (PaymentSearchDownloadObject transactionSearch : bookingRecordList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadBookingPaymentsReportCapturedForGlocal(
						merchantPayId, sessionUser, merchant);

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
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String FILE_EXTENSION = ".xlsx";
			String dateFolder = dateFormat.format(new Date()).split(" ")[0];
			
			if (isGenerateReport()) {

				
				
				
				String fileLocation = "";
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					if (sessionUser.getUserType().equals(UserType.ADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/ResellerCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					}
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(subMerchPayId) && !subMerchPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchPayId + "-" + subMerchPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
								
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "-" + subMerchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchPayId + "-"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						}
					} else {
						filename = getReportType() + "_Report_" + DateCreater.changeDateString(dateFrom) + "_"
								+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
					}
				} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					if (sessionUser.isSuperMerchant()) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SuperMerchantCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subMerchPayId) && !subMerchPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchPayId + "-"
										+ subMerchPayId + "_" + DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "-" + subMerchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}

						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						}
					} else if (!sessionUser.isSuperMerchant()
							&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubMerchantCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + subMerchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + subMerchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						}
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/MerchantCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						}

					}
				} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
					fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "/SubUserCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					filename = getReportType() + "_Report_" + sessionUser.getPayId() + "_"
							+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
							+ FILE_EXTENSION;
				}
				
				File file = new File(fileLocation, filename);
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info(filename + " File generated successfully");
				reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchPayId, subMerchPayId,subUserPayId,
						dateFrom, dateTo, sessionUser.getPayId());
			} else {
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Booking_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			}
		} catch (Exception exception) {
			setGenerateReport(false);
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	
	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					setReportType("Booking");
					execute();
				} catch (Exception e) {
					setGenerateReport(false);
					logger.error("Exception while generating Booking Report ", e);
				}
			}
		};

		propertiesManager.executorImpl(runnable);

		return SUCCESS;
	
	}
	
	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPartSettleFlag() {
		return partSettleFlag;
	}

	public void setPartSettleFlag(String partSettleFlag) {
		this.partSettleFlag = partSettleFlag;
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

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
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

	public String getCustMobile() {
		return custMobile;
	}

	public void setCustMobile(String custMobile) {
		this.custMobile = custMobile;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getSKUCode() {
		return SKUCode;
	}

	public void setSKUCode(String sKUCode) {
		SKUCode = sKUCode;
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


	public boolean isGenerateReport() {
		return generateReport;
	}


	public void setGenerateReport(boolean generateReport) {
		this.generateReport = generateReport;
	}


	public String getReportType() {
		return reportType;
	}


	public void setReportType(String reportType) {
		this.reportType = reportType;
	}


	public User getSubuser() {
		return subuser;
	}


	public void setSubuser(User subuser) {
		this.subuser = subuser;
	}

}
