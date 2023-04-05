package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
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

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadTransactionsReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = -8129011751068997117L;
	private static Logger logger = LoggerFactory.getLogger(DownloadTransactionsReportAction.class.getName());
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String subMerchantPayId;
	private String customerEmail;
	private String merchantPayId;
	private String paymentType;
	private String transactionType;
	private String reportType;
	private String status;
	private String acquirer;
	private String partSettleFlag;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	private String paymentsRegion;
	private String cardHolderType;
	private String deliveryStatus;
	private User merchant = new User();
	private Set<String> orderIdSet = null;
	public String subUserPayId;
	private String transactionFlag;
	private String deltaFlag;
	private String orderId;
	private String transactionId;
	private String autoRefund;
	private boolean generateReport;
	private User subuser;

	List<Merchants> resellerMerchants = new ArrayList<Merchants>();

	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;

	@Autowired
	private DownloadReportGenerateService reportGenerateService;

	@SuppressWarnings({ "unchecked", "static-access" })
	public String execute() {

		boolean isGlocal = false;

		if (acquirer == null || acquirer.isEmpty()) {
			acquirer = "ALL";

		}

		if (paymentsRegion == null || paymentsRegion.isEmpty()) {
			paymentsRegion = "ALL";

		}

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String subMerchPayId = "";

		if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchPayId = sessionUser.getPayId();
		} else {
			if (sessionUser.isSuperMerchant() == false
					&& sessionUser.getUserType().name().equalsIgnoreCase("MERCHANT")) {
				merchantPayId = sessionUser.getPayId();
			} else {
				if (StringUtils.isNotEmpty(merchantPayId) && !merchantPayId.equalsIgnoreCase("All"))
					merchantPayId = userDao.getPayIdByEmailId(merchantPayId);
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				subMerchPayId = userDao.getPayIdByEmailId(subMerchantPayId);
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All")) {
				subMerchPayId = subMerchantPayId;
			}
			if (sessionUser.isSuperMerchant() == true) {
				merchantPayId = sessionUser.getPayId();
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
				merchantPayId = user.getSuperMerchantId();
				subMerchPayId = user.getPayId();
			} else {
				merchantPayId = user.getPayId();
			}

			if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

				String subUserId = "";
				if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
					subUserId = sessionUser.getPayId();
				}

				orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());
				sessionUser = user; // userdao.findPayId(sessionUser.getParentPayId());

			} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

				sessionUser = user; // userdao.findPayId(sessionUser.getParentPayId());
			}

		}

		if (!merchantPayId.equalsIgnoreCase("ALL")) {
			String identifierPayId = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
			if (StringUtils.isNotBlank(identifierPayId) && identifierPayId.contains(merchantPayId)) {
				isGlocal = true;
			}
		}

		if (getReportType().equalsIgnoreCase("saleCaptured")) {
			transactionType = TransactionType.SALE.getName();
			status = StatusType.CAPTURED.getName();
		} else if (getReportType().equalsIgnoreCase("refundCaptured")) {
			transactionType = TransactionType.REFUND.getName();
			status = StatusType.CAPTURED.getName();
		} else {
			status = StatusType.SETTLED.getName();
		}

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<PaymentSearchDownloadObject> transactionList = new ArrayList<PaymentSearchDownloadObject>();
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		transactionList = txnReports.searchPaymentForDownload(merchantPayId, subMerchPayId, customerEmail, paymentType,
				status, currency, transactionType, dateFrom, dateTo, sessionUser, paymentsRegion, acquirer,
				partSettleFlag, isGlocal, orderIdSet, transactionFlag, deltaFlag, transactionId, orderId, autoRefund);
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadTransactionsReportAction");

		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		Sheet sheet = wb.createSheet("Transaction Report");

		if (transactionType.equalsIgnoreCase(TxnType.SALE.getName())
				&& status.equalsIgnoreCase(StatusType.CAPTURED.getName()) && isGlocal == true
				&& StringUtils.isNotBlank(subMerchPayId)) {

			row = sheet.createRow(0);

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
			row.createCell(11).setCellValue("CardHolder Type");
			row.createCell(12).setCellValue("Txn Type");
			row.createCell(13).setCellValue("Transaction Mode");
			row.createCell(14).setCellValue("Status");
			row.createCell(15).setCellValue("Transaction Region");
			row.createCell(16).setCellValue("Base Amount");
			row.createCell(17).setCellValue("Total Amount");
			row.createCell(18).setCellValue("TDR / Surcharge");
			row.createCell(19).setCellValue("GST");
			row.createCell(20).setCellValue("Doctor");
			row.createCell(21).setCellValue("Glocal");
			row.createCell(22).setCellValue("Partner");
			row.createCell(23).setCellValue("Unique ID");
			row.createCell(24).setCellValue("Merchant Amount");
			row.createCell(25).setCellValue("Delivery Status");
			row.createCell(26).setCellValue("Transaction Flag");
			row.createCell(27).setCellValue("Part Settled Flag");
			row.createCell(28).setCellValue("UDF11");
			row.createCell(29).setCellValue("UDF12");
			row.createCell(30).setCellValue("UDF13");
			row.createCell(31).setCellValue("UDF14");
			row.createCell(32).setCellValue("UDF15");
			row.createCell(33).setCellValue("UDF16");
			row.createCell(34).setCellValue("UDF17");
			row.createCell(35).setCellValue("UDF18");
			if (merchantPayId.equalsIgnoreCase("ALL")) {
				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
					for (Merchants merchant : resellerMerchants) {
						if (merchant.isRetailMerchantFlag()) {
							row.createCell(36).setCellValue("Category Code");
							row.createCell(37).setCellValue("SKU Code");
							row.createCell(38).setCellValue("Refund Cycle");
							row.createCell(39).setCellValue("Product Price");
							row.createCell(40).setCellValue("Vendor Id");
							break;
						}
					}
				} else {
					row.createCell(36).setCellValue("Category Code");
					row.createCell(37).setCellValue("SKU Code");
					row.createCell(38).setCellValue("Refund Cycle");
					row.createCell(39).setCellValue("Product Price");
					row.createCell(40).setCellValue("Vendor Id");
				}
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findPayId(merchantPayId);

					if (merchant.isRetailMerchantFlag()) {
						row.createCell(36).setCellValue("Category Code");
						row.createCell(37).setCellValue("SKU Code");
						row.createCell(38).setCellValue("Refund Cycle");
						row.createCell(39).setCellValue("Product Price");
						row.createCell(40).setCellValue("Vendor Id");
					}
				} else {

					if (sessionUser.isRetailMerchantFlag()) {
						row.createCell(36).setCellValue("Category Code");
						row.createCell(37).setCellValue("SKU Code");
						row.createCell(38).setCellValue("Refund Cycle");
						row.createCell(39).setCellValue("Product Price");
						row.createCell(40).setCellValue("Vendor Id");
					}

				}
			}

		} else if (transactionType.equalsIgnoreCase(TxnType.SALE.getName())
				&& status.equalsIgnoreCase(StatusType.CAPTURED.getName()) && isGlocal == false
				&& StringUtils.isNotBlank(subMerchPayId)) {
			int n = 2;
			row = sheet.createRow(0);

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
			row.createCell(11).setCellValue("Cust Email");
			row.createCell(12).setCellValue("CardHolder Type");
			row.createCell(13).setCellValue("Txn Type");
			row.createCell(14).setCellValue("Transaction Mode");
			row.createCell(15).setCellValue("Status");
			row.createCell(16).setCellValue("Transaction Region");
			row.createCell(17).setCellValue("Base Amount");
			row.createCell(18).setCellValue("Total Amount");

			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				row.createCell(19).setCellValue("SUF CHARGES");
				row.createCell(20).setCellValue("SUF GST");

				row.createCell(19 + n).setCellValue("TDR / Surcharge");
				row.createCell(20 + n).setCellValue("GST");
				row.createCell(21 + n).setCellValue("Reseller Charges");
				row.createCell(22 + n).setCellValue("Reseller GST");
				row.createCell(23 + n).setCellValue("Merchant Amount");
				row.createCell(24 + n).setCellValue("Transaction Flag");
				row.createCell(25 + n).setCellValue("Part Settled Flag");
				row.createCell(26 + n).setCellValue("UDF11");
				row.createCell(27 + n).setCellValue("UDF12");
				row.createCell(28 + n).setCellValue("UDF13");
				row.createCell(29 + n).setCellValue("UDF14");
				row.createCell(30 + n).setCellValue("UDF15");
				row.createCell(31 + n).setCellValue("UDF16");
				row.createCell(32 + n).setCellValue("UDF17");
				row.createCell(33 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(34 + n).setCellValue("Category Code");
								row.createCell(35 + n).setCellValue("SKU Code");
								row.createCell(36 + n).setCellValue("Refund Cycle");
								row.createCell(37 + n).setCellValue("Product Price");
								row.createCell(38 + n).setCellValue("Vendor Id");
								break;
							}
						}
					} else {
						row.createCell(34 + n).setCellValue("Category Code");
						row.createCell(35 + n).setCellValue("SKU Code");
						row.createCell(36 + n).setCellValue("Refund Cycle");
						row.createCell(37 + n).setCellValue("Product Price");
						row.createCell(38 + n).setCellValue("Vendor Id");
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(34 + n).setCellValue("Category Code");
							row.createCell(35 + n).setCellValue("SKU Code");
							row.createCell(36 + n).setCellValue("Refund Cycle");
							row.createCell(37 + n).setCellValue("Product Price");
							row.createCell(38 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(34 + n).setCellValue("Category Code");
							row.createCell(35 + n).setCellValue("SKU Code");
							row.createCell(36 + n).setCellValue("Refund Cycle");
							row.createCell(37 + n).setCellValue("Product Price");
							row.createCell(38 + n).setCellValue("Vendor Id");
						}

					}
				}
			} else {
				row.createCell(19).setCellValue("SUF CHARGES");
				row.createCell(20).setCellValue("SUF GST");
				row.createCell(19 + n).setCellValue("TDR / Surcharge");
				row.createCell(20 + n).setCellValue("GST");
				row.createCell(21 + n).setCellValue("Merchant Amount");
				row.createCell(22 + n).setCellValue("Transaction Flag");
				row.createCell(23 + n).setCellValue("Part Settled Flag");
				row.createCell(24 + n).setCellValue("UDF11");
				row.createCell(25 + n).setCellValue("UDF12");
				row.createCell(26 + n).setCellValue("UDF13");
				row.createCell(27 + n).setCellValue("UDF14");
				row.createCell(28 + n).setCellValue("UDF15");
				row.createCell(29 + n).setCellValue("UDF16");
				row.createCell(30 + n).setCellValue("UDF17");
				row.createCell(31 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(32 + n).setCellValue("Category Code");
								row.createCell(33 + n).setCellValue("SKU Code");
								row.createCell(34 + n).setCellValue("Refund Cycle");
								row.createCell(35 + n).setCellValue("Product Price");
								row.createCell(36 + n).setCellValue("Vendor Id");
								break;
							}
						}
					} else {
						row.createCell(32 + n).setCellValue("Category Code");
						row.createCell(33 + n).setCellValue("SKU Code");
						row.createCell(34 + n).setCellValue("Refund Cycle");
						row.createCell(35 + n).setCellValue("Product Price");
						row.createCell(36 + n).setCellValue("Vendor Id");
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(32 + n).setCellValue("Category Code");
							row.createCell(33 + n).setCellValue("SKU Code");
							row.createCell(34 + n).setCellValue("Refund Cycle");
							row.createCell(35 + n).setCellValue("Product Price");
							row.createCell(36 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(32 + n).setCellValue("Category Code");
							row.createCell(33 + n).setCellValue("SKU Code");
							row.createCell(34 + n).setCellValue("Refund Cycle");
							row.createCell(35 + n).setCellValue("Product Price");
							row.createCell(36 + n).setCellValue("Vendor Id");
						}

					}
				}
			}

		} else if (transactionType.equalsIgnoreCase(TxnType.SALE.getName())
				&& status.equalsIgnoreCase(StatusType.CAPTURED.getName()) && isGlocal == true) {

			row = sheet.createRow(0);

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
			row.createCell(10).setCellValue("CardHolder Type");
			row.createCell(11).setCellValue("Txn Type");
			row.createCell(12).setCellValue("Transaction Mode");
			row.createCell(13).setCellValue("Status");
			row.createCell(14).setCellValue("Transaction Region");
			row.createCell(15).setCellValue("Base Amount");
			row.createCell(16).setCellValue("Total Amount");
			row.createCell(17).setCellValue("TDR / Surcharge");
			row.createCell(18).setCellValue("GST");
			row.createCell(19).setCellValue("Doctor");
			row.createCell(20).setCellValue("Glocal");
			row.createCell(21).setCellValue("Partner");
			row.createCell(22).setCellValue("Unique ID");
			row.createCell(23).setCellValue("Merchant Amount");
			row.createCell(24).setCellValue("Delivery Status");
			row.createCell(25).setCellValue("Transaction Flag");
			row.createCell(26).setCellValue("Part Settled Flag");
			row.createCell(27).setCellValue("UDF11");
			row.createCell(28).setCellValue("UDF12");
			row.createCell(29).setCellValue("UDF13");
			row.createCell(30).setCellValue("UDF14");
			row.createCell(31).setCellValue("UDF15");
			row.createCell(32).setCellValue("UDF16");
			row.createCell(33).setCellValue("UDF17");
			row.createCell(34).setCellValue("UDF18");

			if (merchantPayId.equalsIgnoreCase("ALL")) {
				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
					for (Merchants merchant : resellerMerchants) {
						if (merchant.isRetailMerchantFlag()) {
							row.createCell(35).setCellValue("Category Code");
							row.createCell(36).setCellValue("SKU Code");
							row.createCell(37).setCellValue("Refund Cycle");
							row.createCell(38).setCellValue("Product Price");
							row.createCell(39).setCellValue("Vendor Id");
							break;
						}
					}

				} else {
					row.createCell(35).setCellValue("Category Code");
					row.createCell(36).setCellValue("SKU Code");
					row.createCell(37).setCellValue("Refund Cycle");
					row.createCell(38).setCellValue("Product Price");
					row.createCell(39).setCellValue("Vendor Id");
				}

			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findPayId(merchantPayId);

					if (merchant.isRetailMerchantFlag()) {
						row.createCell(35).setCellValue("Category Code");
						row.createCell(36).setCellValue("SKU Code");
						row.createCell(37).setCellValue("Refund Cycle");
						row.createCell(38).setCellValue("Product Price");
						row.createCell(39).setCellValue("Vendor Id");
					}
				} else {

					if (sessionUser.isRetailMerchantFlag()) {
						row.createCell(35).setCellValue("Category Code");
						row.createCell(36).setCellValue("SKU Code");
						row.createCell(37).setCellValue("Refund Cycle");
						row.createCell(38).setCellValue("Product Price");
						row.createCell(39).setCellValue("Vendor Id");
					}

				}
			}

		} else if (transactionType.equalsIgnoreCase(TxnType.SALE.getName())
				&& status.equalsIgnoreCase(StatusType.CAPTURED.getName()) && isGlocal == false) {
			int n = 2;
			row = sheet.createRow(0);

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
			row.createCell(10).setCellValue("Cust Email");
			row.createCell(11).setCellValue("CardHolder Type");
			row.createCell(12).setCellValue("Txn Type");
			row.createCell(13).setCellValue("Transaction Mode");
			row.createCell(14).setCellValue("Status");
			row.createCell(15).setCellValue("Transaction Region");
			row.createCell(16).setCellValue("Base Amount");
			row.createCell(17).setCellValue("Total Amount");
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				row.createCell(18).setCellValue("TDR / Surcharge");
				row.createCell(19).setCellValue("GST");
				// add to column by vishal
				row.createCell(20).setCellValue("SUF CHARGES");
				row.createCell(21).setCellValue("SUF GST");

				row.createCell(20 + n).setCellValue("Reseller Charges");
				row.createCell(21 + n).setCellValue("Reseller GST");
				row.createCell(22 + n).setCellValue("Merchant Amount");
				row.createCell(23 + n).setCellValue("Transaction Flag");
				row.createCell(24 + n).setCellValue("Part Settled Flag");
				row.createCell(25 + n).setCellValue("UDF11");
				row.createCell(26 + n).setCellValue("UDF12");
				row.createCell(27 + n).setCellValue("UDF13");
				row.createCell(28 + n).setCellValue("UDF14");
				row.createCell(29 + n).setCellValue("UDF15");
				row.createCell(30 + n).setCellValue("UDF16");
				row.createCell(31 + n).setCellValue("UDF17");
				row.createCell(32 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(33 + n).setCellValue("Category Code");
								row.createCell(34 + n).setCellValue("SKU Code");
								row.createCell(35 + n).setCellValue("Refund Cycle");
								row.createCell(36 + n).setCellValue("Product Price");
								row.createCell(37 + n).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(33 + n).setCellValue("Category Code");
						row.createCell(34 + n).setCellValue("SKU Code");
						row.createCell(35 + n).setCellValue("Refund Cycle");
						row.createCell(36 + n).setCellValue("Product Price");
						row.createCell(37 + n).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(33 + n).setCellValue("Category Code");
							row.createCell(34 + n).setCellValue("SKU Code");
							row.createCell(35 + n).setCellValue("Refund Cycle");
							row.createCell(36 + n).setCellValue("Product Price");
							row.createCell(37 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(33 + n).setCellValue("Category Code");
							row.createCell(34 + n).setCellValue("SKU Code");
							row.createCell(35 + n).setCellValue("Refund Cycle");
							row.createCell(36 + n).setCellValue("Product Price");
							row.createCell(37 + n).setCellValue("Vendor Id");
						}

					}
				}
			} else {
				row.createCell(18).setCellValue("TDR / Surcharge");
				row.createCell(19).setCellValue("GST");
				// add to column by vishal
				row.createCell(20).setCellValue("SUF CHARGES");
				row.createCell(21).setCellValue("SUF GST");

				row.createCell(20 + n).setCellValue("Merchant Amount");
				row.createCell(21 + n).setCellValue("Transaction Flag");
				row.createCell(22 + n).setCellValue("Part Settled Flag");
				row.createCell(23 + n).setCellValue("UDF11");
				row.createCell(24 + n).setCellValue("UDF12");
				row.createCell(25 + n).setCellValue("UDF13");
				row.createCell(26 + n).setCellValue("UDF14");
				row.createCell(27 + n).setCellValue("UDF15");
				row.createCell(28 + n).setCellValue("UDF16");
				row.createCell(29 + n).setCellValue("UDF17");
				row.createCell(30 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(31 + n).setCellValue("Category Code");
								row.createCell(32 + n).setCellValue("SKU Code");
								row.createCell(33 + n).setCellValue("Refund Cycle");
								row.createCell(34 + n).setCellValue("Product Price");
								row.createCell(35 + n).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(31 + n).setCellValue("Category Code");
						row.createCell(32 + n).setCellValue("SKU Code");
						row.createCell(33 + n).setCellValue("Refund Cycle");
						row.createCell(34 + n).setCellValue("Product Price");
						row.createCell(35 + n).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(31 + n).setCellValue("Category Code");
							row.createCell(32 + n).setCellValue("SKU Code");
							row.createCell(33 + n).setCellValue("Refund Cycle");
							row.createCell(34 + n).setCellValue("Product Price");
							row.createCell(35 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(31 + n).setCellValue("Category Code");
							row.createCell(32 + n).setCellValue("SKU Code");
							row.createCell(33 + n).setCellValue("Refund Cycle");
							row.createCell(34 + n).setCellValue("Product Price");
							row.createCell(35 + n).setCellValue("Vendor Id");
						}

					}
				}
			}

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName()) && StringUtils.isNotBlank(subMerchPayId)) {

			row = sheet.createRow(0);

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
			row.createCell(11).setCellValue("Cust Email");
			row.createCell(12).setCellValue("CardHolder Type");
			row.createCell(13).setCellValue("Txn Type");
			row.createCell(14).setCellValue("Transaction Mode");
			row.createCell(15).setCellValue("Status");
			row.createCell(16).setCellValue("Refund Order Id");
			row.createCell(17).setCellValue("Transaction Region");
			row.createCell(18).setCellValue("Base Amount");
			row.createCell(19).setCellValue("Total Amount");
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				row.createCell(20).setCellValue("TDR / Surcharge");
				row.createCell(21).setCellValue("GST");
				row.createCell(22).setCellValue("Reseller Charges");
				row.createCell(23).setCellValue("Reseller GST");
				row.createCell(24).setCellValue("Merchant Amount");
				row.createCell(25).setCellValue("Transaction Flag");
				row.createCell(26).setCellValue("Part Settled Flag");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(27).setCellValue("Category Code");
								row.createCell(28).setCellValue("SKU Code");
								row.createCell(29).setCellValue("Refund Cycle");
								row.createCell(30).setCellValue("Product Price");
								row.createCell(31).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(27).setCellValue("Category Code");
						row.createCell(28).setCellValue("SKU Code");
						row.createCell(29).setCellValue("Refund Cycle");
						row.createCell(30).setCellValue("Product Price");
						row.createCell(31).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(27).setCellValue("Category Code");
							row.createCell(28).setCellValue("SKU Code");
							row.createCell(29).setCellValue("Refund Cycle");
							row.createCell(30).setCellValue("Product Price");
							row.createCell(31).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(27).setCellValue("Category Code");
							row.createCell(28).setCellValue("SKU Code");
							row.createCell(29).setCellValue("Refund Cycle");
							row.createCell(30).setCellValue("Product Price");
							row.createCell(31).setCellValue("Vendor Id");
						}

					}
				}
			} else {
				row.createCell(20).setCellValue("TDR / Surcharge");
				row.createCell(21).setCellValue("GST");
				row.createCell(22).setCellValue("Merchant Amount");
				row.createCell(23).setCellValue("Transaction Flag");
				row.createCell(24).setCellValue("Part Settled Flag");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(25).setCellValue("Category Code");
								row.createCell(26).setCellValue("SKU Code");
								row.createCell(27).setCellValue("Refund Cycle");
								row.createCell(28).setCellValue("Product Price");
								row.createCell(29).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(25).setCellValue("Category Code");
						row.createCell(26).setCellValue("SKU Code");
						row.createCell(27).setCellValue("Refund Cycle");
						row.createCell(28).setCellValue("Product Price");
						row.createCell(29).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(25).setCellValue("Category Code");
							row.createCell(26).setCellValue("SKU Code");
							row.createCell(27).setCellValue("Refund Cycle");
							row.createCell(28).setCellValue("Product Price");
							row.createCell(29).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(25).setCellValue("Category Code");
							row.createCell(26).setCellValue("SKU Code");
							row.createCell(27).setCellValue("Refund Cycle");
							row.createCell(28).setCellValue("Product Price");
							row.createCell(29).setCellValue("Vendor Id");
						}

					}
				}
			}

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {

			row = sheet.createRow(0);

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
			row.createCell(10).setCellValue("Cust Email");
			row.createCell(11).setCellValue("CardHolder Type");
			row.createCell(12).setCellValue("Txn Type");
			row.createCell(13).setCellValue("Transaction Mode");
			row.createCell(14).setCellValue("Status");
			row.createCell(15).setCellValue("Refund Order Id");
			row.createCell(16).setCellValue("Transaction Region");
			row.createCell(17).setCellValue("Base Amount");
			row.createCell(18).setCellValue("Total Amount");
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				row.createCell(19).setCellValue("TDR / Surcharge");
				row.createCell(20).setCellValue("GST");
				row.createCell(21).setCellValue("Reseller Charges");
				row.createCell(22).setCellValue("Reseller GST");
				row.createCell(23).setCellValue("Merchant Amount");
				row.createCell(24).setCellValue("Transaction Flag");
				row.createCell(25).setCellValue("Part Settled Flag");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(26).setCellValue("Category Code");
								row.createCell(27).setCellValue("SKU Code");
								row.createCell(28).setCellValue("Refund Cycle");
								row.createCell(29).setCellValue("Product Price");
								row.createCell(30).setCellValue("Vendor Id");
								row.createCell(31).setCellValue("Refund Flag");
								break;
							}
						}

					} else {
						row.createCell(26).setCellValue("Category Code");
						row.createCell(27).setCellValue("SKU Code");
						row.createCell(28).setCellValue("Refund Cycle");
						row.createCell(29).setCellValue("Product Price");
						row.createCell(30).setCellValue("Vendor Id");
						row.createCell(31).setCellValue("Refund Flag");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(26).setCellValue("Category Code");
							row.createCell(27).setCellValue("SKU Code");
							row.createCell(28).setCellValue("Refund Cycle");
							row.createCell(29).setCellValue("Product Price");
							row.createCell(30).setCellValue("Vendor Id");
							row.createCell(31).setCellValue("Refund Flag");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(26).setCellValue("Category Code");
							row.createCell(27).setCellValue("SKU Code");
							row.createCell(28).setCellValue("Refund Cycle");
							row.createCell(29).setCellValue("Product Price");
							row.createCell(30).setCellValue("Vendor Id");
							row.createCell(31).setCellValue("Refund Flag");
						}

					}
				}
			} else {
				row.createCell(19).setCellValue("TDR / Surcharge");
				row.createCell(20).setCellValue("GST");
				row.createCell(21).setCellValue("Merchant Amount");
				row.createCell(22).setCellValue("Transaction Flag");
				row.createCell(23).setCellValue("Part Settled Flag");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(24).setCellValue("Category Code");
								row.createCell(25).setCellValue("SKU Code");
								row.createCell(26).setCellValue("Refund Cycle");
								row.createCell(27).setCellValue("Product Price");
								row.createCell(28).setCellValue("Vendor Id");
								row.createCell(29).setCellValue("Refund Flag");
								break;
							}
						}

					} else {
						row.createCell(24).setCellValue("Category Code");
						row.createCell(25).setCellValue("SKU Code");
						row.createCell(26).setCellValue("Refund Cycle");
						row.createCell(27).setCellValue("Product Price");
						row.createCell(28).setCellValue("Vendor Id");
						row.createCell(29).setCellValue("Refund Flag");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(24).setCellValue("Category Code");
							row.createCell(25).setCellValue("SKU Code");
							row.createCell(26).setCellValue("Refund Cycle");
							row.createCell(27).setCellValue("Product Price");
							row.createCell(28).setCellValue("Vendor Id");
							row.createCell(29).setCellValue("Refund Flag");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(24).setCellValue("Category Code");
							row.createCell(25).setCellValue("SKU Code");
							row.createCell(26).setCellValue("Refund Cycle");
							row.createCell(27).setCellValue("Product Price");
							row.createCell(28).setCellValue("Vendor Id");
							row.createCell(29).setCellValue("Refund Flag");
						}

					}
				}
			}

		} else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal == true
				&& StringUtils.isNotBlank(subMerchPayId)) {
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Sub Merchant");
			row.createCell(5).setCellValue("Captured Date");
			row.createCell(6).setCellValue("Settled Date");
			row.createCell(7).setCellValue("Payout Date");
			row.createCell(8).setCellValue("UTR NO");
			row.createCell(9).setCellValue("Order Id");
			row.createCell(10).setCellValue("RRN");
			row.createCell(11).setCellValue("Payment Method");
			row.createCell(12).setCellValue("MopType");
			row.createCell(13).setCellValue("Mask");
			row.createCell(14).setCellValue("Cust Name");
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
			row.createCell(29).setCellValue("Delivery Status");
			row.createCell(30).setCellValue("Transaction Flag");
			row.createCell(31).setCellValue("Part Settled Flag");
			row.createCell(32).setCellValue("UDF11");
			row.createCell(33).setCellValue("UDF12");
			row.createCell(34).setCellValue("UDF13");
			row.createCell(35).setCellValue("UDF14");
			row.createCell(36).setCellValue("UDF15");
			row.createCell(37).setCellValue("UDF16");
			row.createCell(38).setCellValue("UDF17");
			row.createCell(39).setCellValue("UDF18");
			row.createCell(40).setCellValue("Refund Order ID");

			if (merchantPayId.equalsIgnoreCase("ALL")) {
				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
					for (Merchants merchant : resellerMerchants) {
						if (merchant.isRetailMerchantFlag()) {
							row.createCell(41).setCellValue("Category Code");
							row.createCell(42).setCellValue("SKU Code");
							row.createCell(43).setCellValue("Refund Cycle");
							row.createCell(44).setCellValue("Product Price");
							row.createCell(45).setCellValue("Vendor Id");
							break;
						}
					}

				} else {
					row.createCell(41).setCellValue("Category Code");
					row.createCell(42).setCellValue("SKU Code");
					row.createCell(43).setCellValue("Refund Cycle");
					row.createCell(44).setCellValue("Product Price");
					row.createCell(45).setCellValue("Vendor Id");
				}

			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findPayId(merchantPayId);

					if (merchant.isRetailMerchantFlag()) {
						row.createCell(41).setCellValue("Category Code");
						row.createCell(42).setCellValue("SKU Code");
						row.createCell(43).setCellValue("Refund Cycle");
						row.createCell(44).setCellValue("Product Price");
						row.createCell(45).setCellValue("Vendor Id");
					}
				} else {

					if (sessionUser.isRetailMerchantFlag()) {
						row.createCell(41).setCellValue("Category Code");
						row.createCell(42).setCellValue("SKU Code");
						row.createCell(43).setCellValue("Refund Cycle");
						row.createCell(44).setCellValue("Product Price");
						row.createCell(45).setCellValue("Vendor Id");
					}

				}
			}

		} else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal == false
				&& StringUtils.isNotBlank(subMerchPayId)) {
			int n = 2;
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Sub Merchant");
			row.createCell(5).setCellValue("Captured Date");
			row.createCell(6).setCellValue("Settled Date");
			row.createCell(7).setCellValue("Payout Date");
			row.createCell(8).setCellValue("UTR NO");
			row.createCell(9).setCellValue("Order Id");
			row.createCell(10).setCellValue("RRN");
			row.createCell(11).setCellValue("Payment Method");
			row.createCell(12).setCellValue("MopType");
			row.createCell(13).setCellValue("Mask");
			row.createCell(14).setCellValue("Cust Name");
			row.createCell(15).setCellValue("Cust Email");
			row.createCell(16).setCellValue("CardHolder Type");
			row.createCell(17).setCellValue("Txn Type");
			row.createCell(18).setCellValue("Transaction Mode");
			row.createCell(19).setCellValue("Status");
			row.createCell(20).setCellValue("Transaction Region");
			row.createCell(21).setCellValue("Base Amount");
			row.createCell(22).setCellValue("Total Amount");
			row.createCell(23).setCellValue("SUF Charges");
			row.createCell(24).setCellValue("SUF Gst");
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				row.createCell(23 + n).setCellValue("TDR / Surcharge");
				row.createCell(24 + n).setCellValue("GST");
				row.createCell(25 + n).setCellValue("Reseller Charges");
				row.createCell(26 + n).setCellValue("Reseller GST");
				row.createCell(27 + n).setCellValue("Merchant Amount");
				row.createCell(28 + n).setCellValue("Transaction Flag");
				row.createCell(29 + n).setCellValue("Part Settled Flag");
				row.createCell(30 + n).setCellValue("Refund Order ID");
				row.createCell(31 + n).setCellValue("UDF11");
				row.createCell(32 + n).setCellValue("UDF12");
				row.createCell(33 + n).setCellValue("UDF13");
				row.createCell(34 + n).setCellValue("UDF14");
				row.createCell(35 + n).setCellValue("UDF15");
				row.createCell(36 + n).setCellValue("UDF16");
				row.createCell(37 + n).setCellValue("UDF17");
				row.createCell(38 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(39 + n).setCellValue("Category Code");
								row.createCell(40 + n).setCellValue("SKU Code");
								row.createCell(41 + n).setCellValue("Refund Cycle");
								row.createCell(42 + n).setCellValue("Product Price");
								row.createCell(43 + n).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(39 + n).setCellValue("Category Code");
						row.createCell(40 + n).setCellValue("SKU Code");
						row.createCell(41 + n).setCellValue("Refund Cycle");
						row.createCell(42 + n).setCellValue("Product Price");
						row.createCell(43 + n).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(39 + n).setCellValue("Category Code");
							row.createCell(40 + n).setCellValue("SKU Code");
							row.createCell(41 + n).setCellValue("Refund Cycle");
							row.createCell(42 + n).setCellValue("Product Price");
							row.createCell(43 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(39 + n).setCellValue("Category Code");
							row.createCell(40 + n).setCellValue("SKU Code");
							row.createCell(41 + n).setCellValue("Refund Cycle");
							row.createCell(42 + n).setCellValue("Product Price");
							row.createCell(43 + n).setCellValue("Vendor Id");
						}

					}
				}

			} else {
				row.createCell(23 + n).setCellValue("TDR / Surcharge");
				row.createCell(24 + n).setCellValue("GST");
				row.createCell(25 + n).setCellValue("Merchant Amount");
				row.createCell(26 + n).setCellValue("Transaction Flag");
				row.createCell(27 + n).setCellValue("Part Settled Flag");
				row.createCell(28 + n).setCellValue("Refund Order ID");
				row.createCell(29 + n).setCellValue("UDF11");
				row.createCell(30 + n).setCellValue("UDF12");
				row.createCell(31 + n).setCellValue("UDF13");
				row.createCell(32 + n).setCellValue("UDF14");
				row.createCell(33 + n).setCellValue("UDF15");
				row.createCell(34 + n).setCellValue("UDF16");
				row.createCell(35 + n).setCellValue("UDF17");
				row.createCell(36 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(37 + n).setCellValue("Category Code");
								row.createCell(38 + n).setCellValue("SKU Code");
								row.createCell(39 + n).setCellValue("Refund Cycle");
								row.createCell(40 + n).setCellValue("Product Price");
								row.createCell(41 + n).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(37 + n).setCellValue("Category Code");
						row.createCell(38 + n).setCellValue("SKU Code");
						row.createCell(39 + n).setCellValue("Refund Cycle");
						row.createCell(40 + n).setCellValue("Product Price");
						row.createCell(41 + n).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(37 + n).setCellValue("Category Code");
							row.createCell(38 + n).setCellValue("SKU Code");
							row.createCell(39 + n).setCellValue("Refund Cycle");
							row.createCell(40 + n).setCellValue("Product Price");
							row.createCell(41 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(37 + n).setCellValue("Category Code");
							row.createCell(38 + n).setCellValue("SKU Code");
							row.createCell(39 + n).setCellValue("Refund Cycle");
							row.createCell(40 + n).setCellValue("Product Price");
							row.createCell(41 + n).setCellValue("Vendor Id");
						}

					}
				}

			}

		} else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal == true) {
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Captured Date");
			row.createCell(5).setCellValue("Settled Date");
			row.createCell(6).setCellValue("Payout Date");
			row.createCell(7).setCellValue("UTR NO");
			row.createCell(8).setCellValue("Order Id");
			row.createCell(9).setCellValue("RRN");
			row.createCell(10).setCellValue("Payment Method");
			row.createCell(11).setCellValue("MopType");
			row.createCell(12).setCellValue("Mask");
			row.createCell(13).setCellValue("Cust Name");
			row.createCell(14).setCellValue("CardHolder Type");
			row.createCell(15).setCellValue("Txn Type");
			row.createCell(16).setCellValue("Transaction Mode");
			row.createCell(17).setCellValue("Status");
			row.createCell(18).setCellValue("Transaction Region");
			row.createCell(19).setCellValue("Base Amount");
			row.createCell(20).setCellValue("Total Amount");
			row.createCell(21).setCellValue("TDR / Surcharge");
			row.createCell(22).setCellValue("GST");
			row.createCell(23).setCellValue("Doctor");
			row.createCell(24).setCellValue("Glocal");
			row.createCell(25).setCellValue("Partner");
			row.createCell(26).setCellValue("Unique ID");
			row.createCell(27).setCellValue("Merchant Amount");
			row.createCell(28).setCellValue("Delivery Status");
			row.createCell(29).setCellValue("Transaction Flag");
			row.createCell(30).setCellValue("Part Settled Flag");
			row.createCell(31).setCellValue("UDF11");
			row.createCell(32).setCellValue("UDF12");
			row.createCell(33).setCellValue("UDF13");
			row.createCell(34).setCellValue("UDF14");
			row.createCell(35).setCellValue("UDF15");
			row.createCell(36).setCellValue("UDF16");
			row.createCell(37).setCellValue("UDF17");
			row.createCell(38).setCellValue("UDF18");
			row.createCell(39).setCellValue("Refund Order ID");

			if (merchantPayId.equalsIgnoreCase("ALL")) {
				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
					for (Merchants merchant : resellerMerchants) {
						if (merchant.isRetailMerchantFlag()) {
							row.createCell(40).setCellValue("Category Code");
							row.createCell(41).setCellValue("SKU Code");
							row.createCell(42).setCellValue("Refund Cycle");
							row.createCell(43).setCellValue("Product Price");
							row.createCell(44).setCellValue("Vendor Id");
							break;
						}
					}

				} else {
					row.createCell(40).setCellValue("Category Code");
					row.createCell(41).setCellValue("SKU Code");
					row.createCell(42).setCellValue("Refund Cycle");
					row.createCell(43).setCellValue("Product Price");
					row.createCell(44).setCellValue("Vendor Id");
				}

			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchant = userDao.findPayId(merchantPayId);

					if (merchant.isRetailMerchantFlag()) {
						row.createCell(40).setCellValue("Category Code");
						row.createCell(41).setCellValue("SKU Code");
						row.createCell(42).setCellValue("Refund Cycle");
						row.createCell(43).setCellValue("Product Price");
						row.createCell(44).setCellValue("Vendor Id");
					}
				} else {

					if (sessionUser.isRetailMerchantFlag()) {
						row.createCell(40).setCellValue("Category Code");
						row.createCell(41).setCellValue("SKU Code");
						row.createCell(42).setCellValue("Refund Cycle");
						row.createCell(43).setCellValue("Product Price");
						row.createCell(44).setCellValue("Vendor Id");
					}

				}
			}

		} else {

			row = sheet.createRow(0);
			int n = 2;
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Captured Date");
			row.createCell(5).setCellValue("Settled Date");
			row.createCell(6).setCellValue("Payout Date");
			row.createCell(7).setCellValue("UTR NO");
			row.createCell(8).setCellValue("Order Id");
			row.createCell(9).setCellValue("RRN");
			row.createCell(10).setCellValue("Payment Method");
			row.createCell(11).setCellValue("MopType");
			row.createCell(12).setCellValue("Mask");
			row.createCell(13).setCellValue("Cust Name");
			row.createCell(14).setCellValue("Cust Email");
			row.createCell(15).setCellValue("CardHolder Type");
			row.createCell(16).setCellValue("Txn Type");
			row.createCell(17).setCellValue("Transaction Mode");
			row.createCell(18).setCellValue("Status");
			row.createCell(19).setCellValue("Transaction Region");
			row.createCell(20).setCellValue("Base Amount");
			row.createCell(21).setCellValue("Total Amount");
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				row.createCell(22).setCellValue("TDR / Surcharge");
				row.createCell(23).setCellValue("GST");
				// add 2 column by vishal
				row.createCell(24).setCellValue("SUF CHARGES");
				row.createCell(25).setCellValue("SUF GST");

				row.createCell(24 + n).setCellValue("Reseller Charges");
				row.createCell(25 + n).setCellValue("Reseller GST");
				row.createCell(26 + n).setCellValue("Merchant Amount");
				row.createCell(27 + n).setCellValue("Transaction Flag");
				row.createCell(28 + n).setCellValue("Part Settled Flag");
				row.createCell(29 + n).setCellValue("Refund Order ID");
				row.createCell(30 + n).setCellValue("UDF11");
				row.createCell(31 + n).setCellValue("UDF12");
				row.createCell(32 + n).setCellValue("UDF13");
				row.createCell(33 + n).setCellValue("UDF14");
				row.createCell(34 + n).setCellValue("UDF15");
				row.createCell(35 + n).setCellValue("UDF16");
				row.createCell(36 + n).setCellValue("UDF17");
				row.createCell(37 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(38 + n).setCellValue("Category Code");
								row.createCell(39 + n).setCellValue("SKU Code");
								row.createCell(40 + n).setCellValue("Refund Cycle");
								row.createCell(41 + n).setCellValue("Product Price");
								row.createCell(42 + n).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(38 + n).setCellValue("Category Code");
						row.createCell(39 + n).setCellValue("SKU Code");
						row.createCell(40 + n).setCellValue("Refund Cycle");
						row.createCell(41 + n).setCellValue("Product Price");
						row.createCell(42 + n).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(38 + n).setCellValue("Category Code");
							row.createCell(39 + n).setCellValue("SKU Code");
							row.createCell(40 + n).setCellValue("Refund Cycle");
							row.createCell(41 + n).setCellValue("Product Price");
							row.createCell(42 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(38 + n).setCellValue("Category Code");
							row.createCell(39 + n).setCellValue("SKU Code");
							row.createCell(40 + n).setCellValue("Refund Cycle");
							row.createCell(41 + n).setCellValue("Product Price");
							row.createCell(42 + n).setCellValue("Vendor Id");
						}

					}
				}
			} else {
				row.createCell(22).setCellValue("TDR / Surcharge");
				row.createCell(23).setCellValue("GST");
				// add 2 column by vishal
				row.createCell(24).setCellValue("SUF CHARGES");
				row.createCell(25).setCellValue("SUF GST");
				row.createCell(24 + n).setCellValue("Merchant Amount");
				row.createCell(25 + n).setCellValue("Transaction Flag");
				row.createCell(26 + n).setCellValue("Part Settled Flag");
				row.createCell(27 + n).setCellValue("Refund Order ID");
				row.createCell(28 + n).setCellValue("UDF11");
				row.createCell(29 + n).setCellValue("UDF12");
				row.createCell(30 + n).setCellValue("UDF13");
				row.createCell(31 + n).setCellValue("UDF14");
				row.createCell(32 + n).setCellValue("UDF15");
				row.createCell(33 + n).setCellValue("UDF16");
				row.createCell(34 + n).setCellValue("UDF17");
				row.createCell(35 + n).setCellValue("UDF18");

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
						for (Merchants merchant : resellerMerchants) {
							if (merchant.isRetailMerchantFlag()) {
								row.createCell(36 + n).setCellValue("Category Code");
								row.createCell(37 + n).setCellValue("SKU Code");
								row.createCell(38 + n).setCellValue("Refund Cycle");
								row.createCell(39 + n).setCellValue("Product Price");
								row.createCell(40 + n).setCellValue("Vendor Id");
								break;
							}
						}

					} else {
						row.createCell(36 + n).setCellValue("Category Code");
						row.createCell(37 + n).setCellValue("SKU Code");
						row.createCell(38 + n).setCellValue("Refund Cycle");
						row.createCell(39 + n).setCellValue("Product Price");
						row.createCell(40 + n).setCellValue("Vendor Id");
					}

				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						merchant = userDao.findPayId(merchantPayId);

						if (merchant.isRetailMerchantFlag()) {
							row.createCell(36 + n).setCellValue("Category Code");
							row.createCell(37 + n).setCellValue("SKU Code");
							row.createCell(38 + n).setCellValue("Refund Cycle");
							row.createCell(39 + n).setCellValue("Product Price");
							row.createCell(40 + n).setCellValue("Vendor Id");
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							row.createCell(36 + n).setCellValue("Category Code");
							row.createCell(37 + n).setCellValue("SKU Code");
							row.createCell(38 + n).setCellValue("Refund Cycle");
							row.createCell(39 + n).setCellValue("Product Price");
							row.createCell(40 + n).setCellValue("Vendor Id");
						}

					}
				}
			}

		}

		if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())
				&& transactionType.equalsIgnoreCase(TxnType.SALE.getName()) && isGlocal == true
				&& StringUtils.isNotBlank(subMerchPayId)) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportCapturedForSpecificSubMerchant(
						merchantPayId, sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())
				&& transactionType.equalsIgnoreCase(TxnType.SALE.getName()) && isGlocal == false
				&& StringUtils.isNotBlank(subMerchPayId)) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportCapturedForSubMerchant(
						merchantPayId, sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())
				&& transactionType.equalsIgnoreCase(TxnType.SALE.getName()) && isGlocal == true) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportCapturedForSpecificMerchant(
						merchantPayId, sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())
				&& transactionType.equalsIgnoreCase(TxnType.SALE.getName()) && isGlocal == false) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				// sale capture report
				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportSaleCaptured(merchantPayId,
						sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName()) && StringUtils.isNotBlank(subMerchPayId)) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportByViewCaputuredForSubMerchant(
						merchantPayId, sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportByViewCaputured(merchantPayId,
						sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal == true
				&& StringUtils.isNotBlank(subMerchPayId)) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentSettledReportForSpecificSubMerchant(
						merchantPayId, sessionUser, merchant, resellerMerchants);

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

		} else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal == false
				&& StringUtils.isNotBlank(subMerchPayId)) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentSettledReportForSubMerchant(merchantPayId,
						sessionUser, merchant, resellerMerchants);

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

		} /*
			 * else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal ==
			 * false && StringUtils.isNotBlank(subMerchPayId)) {
			 * 
			 * for (PaymentSearchDownloadObject transactionSearch : transactionList) {
			 * 
			 * row = sheet.createRow(rownum++);
			 * transactionSearch.setSrNo(String.valueOf(rownum-1));
			 * 
			 * Object[] objArr = transactionSearch.
			 * myCsvMethodDownloadPaymentSettledReportForSpecificSubMerchant();
			 * 
			 * int cellnum = 0; for (Object obj : objArr) { // this line creates a cell in
			 * the next column of that row Cell cell = row.createCell(cellnum++); if (obj
			 * instanceof String) cell.setCellValue((String) obj); else if (obj instanceof
			 * Integer) cell.setCellValue((Integer) obj);
			 * 
			 * } }
			 * 
			 * }
			 */ else if (status.equalsIgnoreCase(StatusType.SETTLED.getName()) && isGlocal == true) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentSettledReportForSpecificMerchant(
						merchantPayId, sessionUser, merchant, resellerMerchants);

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

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportByView(merchantPayId, sessionUser,
						merchant, resellerMerchants);

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
				if (getReportType().equals("saleCaptured")) {
					setReportType("Sale");
				} else if (getReportType().equals("refundCaptured")) {
					setReportType("Refund");
				} else {
					setReportType("Settled");
				}
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					if (sessionUser.getUserType().equals(UserType.ADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
					} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/ResellerCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
					}
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(subMerchPayId) && !subMerchPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-"
										+ subMerchPayId + "_" + DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "_"
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
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SuperMerchantCreated/"
								+ dateFolder + "/" + sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subMerchPayId) && !subMerchPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-"
										+ subMerchPayId + "_" + DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}

						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchantPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						}
					} else if (!sessionUser.isSuperMerchant()
							&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubMerchantCreated/"
								+ dateFolder + "/" + sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + subMerchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_"
									+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + subMerchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_"
									+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
						}
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/MerchantCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchantPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_"
									+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchantPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_"
									+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
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
				reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchantPayId,
						subMerchPayId, subUserPayId, dateFrom, dateTo, sessionUser.getPayId());
			} else {
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = status + "_Transactions_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
				logger.info("Files generated successfully for DownloadTransactionsReportAction");
			}
			// deleteFileFromBin();
		} catch (Exception exception) {
			setGenerateReport(false);
			logger.error("Exception", exception);
		}
		return SUCCESS;

	}

	public void deleteFileFromBin() {

		String root = FileSystems.getDefault().getPath("").toAbsolutePath().toString();

		File sourceFilePath = new File(root.toString());
		File[] files = sourceFilePath.listFiles();
		String[] filenameArray = filename.split(".");
		for (File file : files) {
			if (file.getName().equals(filename)) {
				file = new File(filenameArray[0]);
				if (file.delete()) {
					logger.info("Files deleted successfully in DownloadTransactionsReportAction");
				}
				break;
			}
		}
	}

	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					execute();
				} catch (Exception e) {
					logger.error("Exception while generating Transaction Report ", e);
					setGenerateReport(false);
				}
			}
		};

		propertiesManager.executorImpl(runnable);

		return SUCCESS;

	}

	public String downloadUnsettledCapturedTxns() {
		logger.info("Inside downloadUnsettledCapturedTxns() ");

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (acquirer == null || acquirer.isEmpty()) {
			acquirer = "ALL";
		}

		if (paymentsRegion == null || paymentsRegion.isEmpty()) {
			paymentsRegion = "ALL";
		}

		String subMerchPayId = "";

		if (StringUtils.isNotEmpty(merchantPayId) && !merchantPayId.equalsIgnoreCase("All"))
			merchantPayId = userDao.getPayIdByEmailId(merchantPayId);

		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
			subMerchPayId = userDao.getPayIdByEmailId(subMerchantPayId);
		}

		if (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All")) {
			subMerchPayId = subMerchantPayId;
		}

		if (StringUtils.isNotEmpty(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
			User subuser = userDao.findByEmailId(subUserPayId);
			orderIdSet = txnReports.findBySubuserId(subuser.getPayId(), sessionUser.getParentPayId());
		} else {
			orderIdSet = null;
		}

		List<PaymentSearchDownloadObject> transactionList = new ArrayList<PaymentSearchDownloadObject>();
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

			transactionList = txnReports.unsettledCapturedForDownload(merchantPayId, subMerchPayId, customerEmail,
					paymentType, currency, dateFrom, dateTo, sessionUser, paymentsRegion, acquirer, partSettleFlag,
					false, orderIdSet, transactionFlag, deltaFlag, transactionId, orderId, autoRefund);

			logger.info("List generated successfully for downloadUnsettledCapturedTxns()");
		}
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		Sheet sheet = wb.createSheet("Transaction Report");

		if (StringUtils.isNotBlank(subMerchPayId)) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Acquirer_Type");
			row.createCell(5).setCellValue("Rrn");
			row.createCell(6).setCellValue("Acq_Id");
			row.createCell(7).setCellValue("Sub Merchant");
			row.createCell(8).setCellValue("Captured Date");
			row.createCell(9).setCellValue("Order Id");
			row.createCell(10).setCellValue("Payment Method");
			row.createCell(11).setCellValue("MopType");
			row.createCell(12).setCellValue("Mask");
			row.createCell(13).setCellValue("Cust Name");
			row.createCell(14).setCellValue("Cust Email");
			row.createCell(15).setCellValue("CardHolder Type");
			row.createCell(16).setCellValue("Txn Type");
			row.createCell(17).setCellValue("Transaction Mode");
			row.createCell(18).setCellValue("Status");
			row.createCell(19).setCellValue("Refund Order Id");
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
					merchant = userDao.findPayId(merchantPayId);

					if (merchant.isRetailMerchantFlag()) {
						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					}
				} else {

					if (sessionUser.isRetailMerchantFlag()) {
						row.createCell(28).setCellValue("Category Code");
						row.createCell(29).setCellValue("SKU Code");
						row.createCell(30).setCellValue("Refund Cycle");
						row.createCell(31).setCellValue("Product Price");
						row.createCell(32).setCellValue("Vendor Id");
					}

				}
			}

		} else {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Acquirer_Type");
			row.createCell(5).setCellValue("Rrn");
			row.createCell(6).setCellValue("Acq_Id");
			row.createCell(7).setCellValue("Captured Date");
			row.createCell(8).setCellValue("Order Id");
			row.createCell(9).setCellValue("Payment Method");
			row.createCell(10).setCellValue("MopType");
			row.createCell(11).setCellValue("Mask");
			row.createCell(12).setCellValue("Cust Name");
			row.createCell(13).setCellValue("Cust Email");
			row.createCell(14).setCellValue("CardHolder Type");
			row.createCell(15).setCellValue("Txn Type");
			row.createCell(16).setCellValue("Transaction Mode");
			row.createCell(17).setCellValue("Status");
			row.createCell(18).setCellValue("Refund Order Id");
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
					merchant = userDao.findPayId(merchantPayId);

					if (merchant.isRetailMerchantFlag()) {
						row.createCell(27).setCellValue("Category Code");
						row.createCell(28).setCellValue("SKU Code");
						row.createCell(29).setCellValue("Refund Cycle");
						row.createCell(30).setCellValue("Product Price");
						row.createCell(31).setCellValue("Vendor Id");
					}
				} else {

					if (sessionUser.isRetailMerchantFlag()) {
						row.createCell(27).setCellValue("Category Code");
						row.createCell(28).setCellValue("SKU Code");
						row.createCell(29).setCellValue("Refund Cycle");
						row.createCell(30).setCellValue("Product Price");
						row.createCell(31).setCellValue("Vendor Id");
					}

				}
			}

		}

		if (StringUtils.isNotBlank(subMerchPayId)) {

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadUnsettledReportForSubMerchant(merchantPayId,
						sessionUser, merchant, resellerMerchants);

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

			for (PaymentSearchDownloadObject transactionSearch : transactionList) {

				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = transactionSearch.myCsvMethodDownloadUnsettledReport(merchantPayId, sessionUser,
						merchant, resellerMerchants);

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
			filename = "Unsettled_Captured_Transactions_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("Files generated successfully for downloadUnsettledCapturedTxns() : ");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getPartSettleFlag() {
		return partSettleFlag;
	}

	public void setPartSettleFlag(String partSettleFlag) {
		this.partSettleFlag = partSettleFlag;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
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

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}

	public String getDeltaFlag() {
		return deltaFlag;
	}

	public void setDeltaFlag(String deltaFlag) {
		this.deltaFlag = deltaFlag;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(String autoRefund) {
		this.autoRefund = autoRefund;
	}

	public boolean isGenerateReport() {
		return generateReport;
	}

	public void setGenerateReport(boolean generateReport) {
		this.generateReport = generateReport;
	}

	public User getSubuser() {
		return subuser;
	}

	public void setSubuser(User subuser) {
		this.subuser = subuser;
	}

}
