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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.user.VendorPayouts;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Rajit
 */

public class VendorPayoutAction extends AbstractSecureAction {

	private static final long serialVersionUID = -3988109417890117050L;
	private static Logger logger = LoggerFactory.getLogger(VendorPayoutAction.class.getName());

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private UserDao userDao;

	private String merchantPayId;
	private String vendorPayId;
	private String date;
	private int length;
	private int start;
	private List<VendorPayouts> aaData;
	private String filename;
	private InputStream fileInputStream;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private Set<String> orderIdSet;

	private User sessionUser = new User();

	public String execute() {

		StringBuilder date2 = new StringBuilder();
		date2.append(date);
		date2.append(" 23:59:59");

		logger.info("inside VendorPayoutAction, execute function !!");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			int totalCount;
			List<Merchants> vendorList = new ArrayList<Merchants>();
			List<MerchantProcessingApplication> vendorMpaList = new ArrayList<MerchantProcessingApplication>();
			if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {
					merchantPayId = sessionUser.getParentPayId();
					String subUserId = "";
					if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
						subUserId = sessionUser.getPayId();
					}
					sessionUser = userDao.findPayId(sessionUser.getParentPayId());

					orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());

					setMerchantPayId(sessionUser.getParentPayId());
					totalCount = txnReports.vendorPayoutReportCount(merchantPayId, vendorMpaList, date2.toString(),
							start, length, vendorList, orderIdSet);

					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					aaData = txnReports.billingDetailsForVendorPayoutTransactions(txnReports.viewVendorPayoutReport(
							merchantPayId, vendorMpaList, date2.toString(), start, length, vendorList, orderIdSet));
					recordsFiltered = recordsTotal;

				} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

					setMerchantPayId(sessionUser.getParentPayId());
					totalCount = txnReports.vendorPayoutReportCount(merchantPayId, vendorMpaList, date2.toString(),
							start, length, vendorList, null);

					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					aaData = txnReports.billingDetailsForVendorPayoutTransactions(txnReports.viewVendorPayoutReport(
							merchantPayId, vendorMpaList, date2.toString(), start, length, vendorList, null));
					recordsFiltered = recordsTotal;
				} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("vendorType")) {

					setMerchantPayId(sessionUser.getParentPayId());
					totalCount = txnReports.vendorPayoutReportCount(merchantPayId, vendorMpaList, date2.toString(),
							start, length, vendorList, null);

					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					aaData = txnReports.billingDetailsForVendorPayoutTransactions(txnReports.viewVendorPayoutReport(
							merchantPayId, vendorMpaList, date2.toString(), start, length, vendorList, null));
					recordsFiltered = recordsTotal;
				}
				if (aaData == null) {
					aaData = new ArrayList<VendorPayouts>();
				}
			}
		} catch (Exception ex) {
			logger.error("Exception caught VendorPayoutAction, execute function " , ex);
			return ERROR;
		}
		return SUCCESS;
	}

	public String downloadVendorPayout() {

		StringBuilder date2 = new StringBuilder();
		date2.append(date);
		date2.append(" 23:59:59");

		logger.info("inside VendorPayoutAction, downloadVendorPayout function !!");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		List<Merchants> vendorList = new ArrayList<Merchants>();
		List<MerchantProcessingApplication> vendorMpaList = new ArrayList<MerchantProcessingApplication>();
		List<VendorPayouts> vendorPayoutList = new ArrayList<VendorPayouts>();

		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

			if (vendorPayId.equalsIgnoreCase("ALL")) {


							}

		} else if (sessionUser.getUserType().equals(UserType.ADMIN)
				|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

			if (vendorPayId.equalsIgnoreCase("ALL")) {
				List<String> payIdList = new ArrayList<String>();
			} else {
				vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
			}

			vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(txnReports.viewVendorPayoutReport(
					merchantPayId, vendorMpaList, date2.toString(), start, length, vendorList, null));
		}

		logger.info("List generated successfully for downloadVendorPayoutReport");

		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		Sheet sheet = wb.createSheet("Vendor Payout Report");

		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Vendor");
			row.createCell(2).setCellValue("Payment Cycle");
			row.createCell(3).setCellValue("Vendor Payout Date");
			row.createCell(4).setCellValue("Period (DateFrom - DateTo)");
			row.createCell(5).setCellValue("Sale Amount");
			row.createCell(6).setCellValue("Refund Amount");
			row.createCell(7).setCellValue("Net Payout (Sale Settled - Refund Settled)");

			for (VendorPayouts vnedorPayout : vendorPayoutList) {

				row = sheet.createRow(rownum++);
				vnedorPayout.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = vnedorPayout.DownloadVendorPayoutReportForMerchant();

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

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant");
			row.createCell(2).setCellValue("Vendor");
			row.createCell(3).setCellValue("Payment Cycle");
			row.createCell(4).setCellValue("Vendor Payout Date");
			row.createCell(5).setCellValue("Period (DateFrom - DateTo)");
			row.createCell(6).setCellValue("Sale Amount");
			row.createCell(7).setCellValue("Refund Amount");
			row.createCell(8).setCellValue("Net Payout (Sale Settled - Refund Settled)");

			for (VendorPayouts vnedorPayout : vendorPayoutList) {

				row = sheet.createRow(rownum++);
				vnedorPayout.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = vnedorPayout.DownloadVendorPayoutReport();

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
			filename = "Vendor_Payout_Report" + df.format(new Date()) + FILE_EXTENSION;
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
			return ERROR;
		}
		logger.info("Files generated successfully for DownloadVendorPayoutReport ");

		return SUCCESS;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getVendorPayId() {
		return vendorPayId;
	}

	public void setVendorPayId(String vendorPayId) {
		this.vendorPayId = vendorPayId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public List<VendorPayouts> getAaData() {
		return aaData;
	}

	public void setAaData(List<VendorPayouts> aaData) {
		this.aaData = aaData;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
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

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}
}
