package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
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

import com.paymentgateway.commons.dao.CustomerQRDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.CustomerQR;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Pooja Pancholi
 *
 */
public class CustomerQRCodeReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2811346574562571717L;

	private static Logger logger = LoggerFactory.getLogger(CustomerQRCodeReportAction.class.getName());

	@Autowired
	private CustomerQRDao customerQRDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PDFCreator pdfCreator;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private String payId;
	private String subMerchantId;
	private String customerAccountNumber;
	private String customerId;
	private String status;
	private String response;
	private String responseMsg;
	private InputStream fileInputStream;
	private String filename;
	private List<String> PayIdList = new ArrayList<String>();
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	List<Merchants> subMerchantList = new ArrayList<Merchants>();
	private boolean superMerchant = false;
	private List<CustomerQR> aaData = new ArrayList<CustomerQR>();
	private User sessionUser = new User();
	private boolean batuwaMerchant;
	private String resellerId;

	public String execute() {

		logger.info("Inside CustomerQRCodeReportAction");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {

			String payIdReport = "";
			if (StringUtils.isNotBlank(payId)) {
				payIdReport = payId;
			}
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				if (StringUtils.isNotBlank(payId) && payId.equalsIgnoreCase("ALL")) {
					List<Merchants> merchantList = userDao.getMerchantListByResellerId(sessionUser.getResellerId());
					for (Merchants merchant : merchantList) {
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
						if (merchantSettings.isCustomerQrFlag())
							PayIdList.add(merchant.getPayId());
					}

				} else if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					PayIdList.add(subMerchantId);
					payIdReport = "";
				} else if (StringUtils.isNotBlank(payId)) {
					User user = userDao.findPayId(payId);
					UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(payId);
					
					if (user.isSuperMerchant()) {
						List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(payId);
						for (Merchants merchant : merchantList) {
							UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
							if (merchantSettings.isCustomerQrFlag())
								PayIdList.add(merchant.getPayId());
						}
						payIdReport = "";
					} else {
						if (userSettings.isCustomerQrFlag())
							PayIdList.add(user.getPayId());
					}

				}
				if (!PayIdList.isEmpty()) {
					totalCount = customerQRDao.CustomerQRReportCount(payIdReport, customerAccountNumber, customerId,
							status, sessionUser, PayIdList);
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					setAaData(customerQRDao.CustomerQRReportData(payIdReport, customerAccountNumber, customerId, status,
							sessionUser, PayIdList, getStart(), getLength()));
					recordsFiltered = recordsTotal;
				}else {
					setRecordsTotal(BigInteger.valueOf(0));
					recordsFiltered = recordsTotal;
				}
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant()) {
				payIdReport = "";
				List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(sessionUser.getPayId());

				if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
					for (Merchants merchant : subMerchantList) {
						UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
						if (userSettings.isCustomerQrFlag())
							PayIdList.add(merchant.getPayId());
					}
				} else if (StringUtils.isNotBlank(subMerchantId)) {
					PayIdList.add(subMerchantId);
				}
				totalCount = customerQRDao.CustomerQRReportCount(payIdReport, customerAccountNumber, customerId, status,
						sessionUser, PayIdList);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(customerQRDao.CustomerQRReportData(payIdReport, customerAccountNumber, customerId, status,
						sessionUser, PayIdList, getStart(), getLength()));
				recordsFiltered = recordsTotal;
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				if (sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
					payIdReport = "";
					User user = userDao.findPayId(sessionUser.getParentPayId());

					if (user.isSuperMerchant()) {
						List<Merchants> subMerchantList = userDao
								.getSubMerchantListBySuperPayId(sessionUser.getParentPayId());

						if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
							for (Merchants merchant : subMerchantList) {
								UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
								if (userSettings.isCustomerQrFlag())
									PayIdList.add(merchant.getPayId());
							}
						} else if (StringUtils.isNotBlank(subMerchantId)) {
							PayIdList.add(subMerchantId);
						}

					} else {
						PayIdList.add(sessionUser.getParentPayId());
					}

					totalCount = customerQRDao.CustomerQRReportCount(payIdReport, customerAccountNumber, customerId,
							status, sessionUser, PayIdList);
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					setAaData(customerQRDao.CustomerQRReportData(payIdReport, customerAccountNumber, customerId, status,
							sessionUser, PayIdList, getStart(), getLength()));
					recordsFiltered = recordsTotal;
				}
			} else {
				
				/*if(sessionUser.getUserType().equals(UserType.MERCHANT) && StringUtils.isNotBlank(sessionUser.getResellerId())) {
					String rslrId = PropertiesManager.propertiesMap.get("BATUWA_RESELLER_ID");
					if(StringUtils.isNotBlank(rslrId) && rslrId.equals(sessionUser.getResellerId())) {
						setBatuwaMerchant(true);
						batuwaMerchant = true;
						setResellerId(rslrId);
						resellerId = rslrId;
					}
				}*/
				
				if (StringUtils.isNotBlank(payId) && payId.equalsIgnoreCase("ALL")) {
					payIdReport = "";
				} else if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					PayIdList.add(subMerchantId);
					payIdReport = "";
				} else if (StringUtils.isNotBlank(payId)) {
					User user = userDao.findPayId(payId);

					if (user.isSuperMerchant()) {
						List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(payId);
						for (Merchants merchant : merchantList) {
								PayIdList.add(merchant.getPayId());
						}
						payIdReport = "";
					} else {
							PayIdList.add(user.getPayId());
					}

				}
				totalCount = customerQRDao.CustomerQRReportCount(payIdReport, customerAccountNumber, customerId, status,
						sessionUser, PayIdList);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(customerQRDao.CustomerQRReportData(payIdReport, customerAccountNumber, customerId, status,
						sessionUser, PayIdList, getStart(), getLength()));
				recordsFiltered = recordsTotal;
			}
			return SUCCESS;

		} catch (Exception e) {
			logger.error("Exception occured in CustomerQR Report , Exception = " , e);
		}
		return SUCCESS;
	}

	public String customerQRReportDownload() {

		logger.info("Inside customerQRReportDownload()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<CustomerQR> customerQRList = new ArrayList<CustomerQR>();
		String payIdReport = "";
		if (StringUtils.isNotBlank(payId)) {
			payIdReport = payId;
		}

		if (sessionUser.getUserType().equals(UserType.RESELLER)) {
			if (StringUtils.isNotBlank(payId) && payId.equalsIgnoreCase("ALL")) {
				List<Merchants> merchantList = userDao.getMerchantListByResellerId(sessionUser.getResellerId());
				for (Merchants merchant : merchantList) {
					UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
					if (userSettings.isCustomerQrFlag())
						PayIdList.add(merchant.getPayId());
				}

			} else if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				PayIdList.add(subMerchantId);
				payIdReport = "";
			} else if (StringUtils.isNotBlank(payId)) {
				User user = userDao.findPayId(payId);

				if (user.isSuperMerchant()) {
					List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(payId);
					for (Merchants merchant : merchantList) {
						UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
						if (userSettings.isCustomerQrFlag())
							PayIdList.add(merchant.getPayId());
					}
					payIdReport = "";
				}

			}
			customerQRList = customerQRDao.CustomerQRDownloadReportData(payIdReport, customerAccountNumber, customerId,
					status, sessionUser, PayIdList);
		} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant()) {
			payIdReport = "";
			List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(sessionUser.getPayId());

			if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
				for (Merchants merchant : merchantList) {
					UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
					if (userSettings.isCustomerQrFlag())
						PayIdList.add(merchant.getPayId());
				}
			} else if (StringUtils.isNotBlank(subMerchantId)) {
				PayIdList.add(subMerchantId);
			}
			customerQRList = customerQRDao.CustomerQRDownloadReportData(payIdReport, customerAccountNumber, customerId,
					status, sessionUser, PayIdList);
		} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
			if (sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
				payIdReport = "";
				User user = userDao.findPayId(sessionUser.getParentPayId());

				if (user.isSuperMerchant()) {
					List<Merchants> subMerchantList = userDao
							.getSubMerchantListBySuperPayId(sessionUser.getParentPayId());

					if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
						for (Merchants merchant : subMerchantList) {
							UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
							if (userSettings.isCustomerQrFlag())
								PayIdList.add(merchant.getPayId());
						}
					} else if (StringUtils.isNotBlank(subMerchantId)) {
						PayIdList.add(subMerchantId);
					}

				} else {
					PayIdList.add(sessionUser.getParentPayId());
				}
				customerQRList = customerQRDao.CustomerQRDownloadReportData(payIdReport, customerAccountNumber,
						customerId, status, sessionUser, PayIdList);
			}
		} else {
			
			if (StringUtils.isNotBlank(payId) && payId.equalsIgnoreCase("ALL")) {
				payIdReport = "";
			} else if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				PayIdList.add(subMerchantId);
				payIdReport = "";
			} else if (StringUtils.isNotBlank(payId)) {
				User user = userDao.findPayId(payId);

				if (user.isSuperMerchant()) {
					List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(payId);
					for (Merchants merchant : merchantList) {
							PayIdList.add(merchant.getPayId());
					}
					payIdReport = "";
				} else {
						PayIdList.add(user.getPayId());
				}

			}
			customerQRList = customerQRDao.CustomerQRDownloadReportData(payIdReport, customerAccountNumber, customerId,
					status, sessionUser, PayIdList);
		}
		BigDecimal st = null;

		logger.info("List generated successfully for customerQRReportDownload");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Customer QR Report");
		row = sheet.createRow(0);

		row.createCell(0).setCellValue("Merchant Name");
		row.createCell(1).setCellValue("Pay Id");
		row.createCell(2).setCellValue("Date");
		row.createCell(3).setCellValue("Customer Account Number");
		row.createCell(4).setCellValue("Customer Id");
		row.createCell(5).setCellValue("Customer Name");
		row.createCell(6).setCellValue("Customer Phone");
		row.createCell(7).setCellValue("Amount");
		row.createCell(8).setCellValue("VPA");
		row.createCell(9).setCellValue("Status");

		for (CustomerQR customerQRSearch : customerQRList) {
			row = sheet.createRow(rownum++);
			// transactionSearch.setSrNo(String.valueOf(rownum-1));
			Object[] objArr = customerQRSearch.csvForCustomerQRMerchant();

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
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Static_UPI_QR_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for customerQRReportDownload");
		} catch (Exception exception) {
			logger.error("Exception occured in CustomerQRReportDownload() , Exception = " , exception);
		}

		return SUCCESS;
	}

	public String activeOrInactiveCustomerQR() {
		logger.info("Inside activeOrInactiveCustomerQR()");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (customerQRDao.activeOrInactiveCustomerData(payId, customerAccountNumber, customerId, status,
					sessionUser)) {
				setResponse("success");
				setResponseMsg("Successfully Done");
			} else {
				setResponse("failed");
				setResponseMsg("UnSuccessful");
			}
		} catch (Exception e) {
			logger.error("Exception occured in activeOrInactiveCustomerQR() , Exception = " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");
		}
		return SUCCESS;

	}

	public String submerchantListBySuperId() {

		try {
			User user = userDao.findPayId(payId);
			if (user.isSuperMerchant()) {
				List<Merchants> subMerchans = userDao.getSubMerchantListBySuperPayId(payId);
				for (Merchants merchant : subMerchans) {
					UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
					if (userSettings.isCustomerQrFlag())
						subMerchantList.add(merchant);
				}
				setSuperMerchant(true);
			} else {
				setSuperMerchant(false);
			}

		} catch (Exception ex) {
			logger.info("Exception cought in getSubmerchantListBySuperId : ", ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String downloadStaticUpiQRPdf() {
		logger.info("inside download static upi qr pdf ");
		try {
			CustomerQR customerQR = customerQRDao.downloadStaticUpiQrPDFData(payId, customerAccountNumber, customerId);
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Static_Upi_QR_" + df.format(new Date()) + ".pdf";
			File file = new File(filename);
			
			fileInputStream = pdfCreator.createStaticUpiQrPdf(customerQR, file, payId);
		} catch(Exception ex) {
			logger.error("exception cought in download static upi qr pdf ", ex);
		}
		
		return SUCCESS;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getCustomerAccountNumber() {
		return customerAccountNumber;
	}

	public void setCustomerAccountNumber(String customerAccountNumber) {
		this.customerAccountNumber = customerAccountNumber;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<CustomerQR> getAaData() {
		return aaData;
	}

	public void setAaData(List<CustomerQR> list) {
		this.aaData = list;
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

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
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

	public List<String> getPayIdList() {
		return PayIdList;
	}

	public void setPayIdList(List<String> payIdList) {
		PayIdList = payIdList;
	}

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public boolean isSuperMerchant() {
		return superMerchant;
	}

	public void setSuperMerchant(boolean superMerchant) {
		this.superMerchant = superMerchant;
	}

	public boolean isBatuwaMerchant() {
		return batuwaMerchant;
	}

	public void setBatuwaMerchant(boolean batuwaMerchant) {
		this.batuwaMerchant = batuwaMerchant;
	}
	
	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}
}
