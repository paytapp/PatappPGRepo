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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.ImpsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * Shiva
 */
public class AccountVerificationAction extends AbstractSecureAction {

	private static final long serialVersionUID = 309979151811470379L;
	private static Logger logger = LoggerFactory.getLogger(AccountVerificationAction.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private ImpsDao impsDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	private File csvFile;
	private String merchantPayId;
	private String subMerchantId;
	private String currencyCode;
	private String beneName;
	private String beneAccountNumber;
	private String beneIfsc;
	private String benePhone;

	private String status;
	private String dateFrom;
	private String dateTo;

	private String beneVpa;
	private String accountType;
	
	private String verificationId;
	
	private String businessName;
	private String subMerchantBusinessName;
	private Map<String, String> currencyMap;
	private List<Merchants> merchantList;
	private List<ImpsDownloadObject> impsDataList;
	private List<Merchants> subMerchantList = new ArrayList<Merchants>();
	private Map<Long, String> failedListShow;
	private List<String> fileData;
	private String fileName;
	private InputStream fileInputStream;
	private ImpsDownloadObject impsData = new ImpsDownloadObject();

	private String response;
	private String responseMsg;
	private User sessionUser = new User();
	public boolean flag = false;
	public boolean superMerchant = false;

	int wrongCsv = 0;
	long rowCount = -1;
	long failedData = 0;
	long SuccessData = 0;
	int fileIsEmpty = 0;

	public String execute() {

		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;

		try {
			if (StringUtils.isNotBlank(subMerchantId)) {
				requestMap.put(FieldType.PAY_ID.getName(), subMerchantId);
			} else {
				requestMap.put(FieldType.PAY_ID.getName(), merchantPayId);
			}

			requestMap.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId());

			if (StringUtils.isNotBlank(beneAccountNumber)) {
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), beneAccountNumber);
				requestMap.put(FieldType.BENE_NAME.getName(), beneName);
				requestMap.put(FieldType.IFSC_CODE.getName(), beneIfsc);
			}

			if (StringUtils.isNotBlank(benePhone)) {
				requestMap.put(FieldType.PHONE_NO.getName(), benePhone);
			}

			requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
			requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));

			respMap = transactionControllerServiceProvider.beneVerificationTransact(requestMap);
			if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.VERIFIED.getName())) {
				setResponse("success");
				setResponseMsg("Bank Account Successfully Verified");
			} else if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PENDING.getName())) {
				setResponse("success");
				setResponseMsg("Bank Account Verification is Pending");
			} else {
				setResponse("failed");
				setResponseMsg("Bank Account Not Verified");
			}

		} catch (SystemException e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}

	@SkipValidation
	public String bulkVerificationRequest() {

		currencyMap = new HashMap<String, String>();
		merchantList = new ArrayList<Merchants>();
		impsDataList = new ArrayList<ImpsDownloadObject>();
		failedListShow = new HashMap<Long, String>();

		String line = "";
		try {
			String fileExtension = FilenameUtils.getExtension(fileName);

			if (fileExtension.equals("csv")) {
				fileData = filterCsvFile(csvFile);
			} else {
				setWrongCsv(1);
				return SUCCESS;
			}

			// Setting FileName for storing in DB Format(fileName_DateTime)
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date fileDate = new Date();
			String newFileName = fileName.concat("_").concat(dateFormat.format(fileDate));

			// Checking Merchant using Session or Input
			User user = (User) sessionMap.get(Constants.USER);
			UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS);
			
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.SUPERADMIN)) {
				if (StringUtils.isNotBlank(subMerchantId)) {
					businessName = userDao.getBusinessNameByPayId(merchantPayId);
					subMerchantBusinessName = userDao.getBusinessNameByPayId(subMerchantId);
				} else {
					businessName = userDao.getBusinessNameByPayId(merchantPayId);
				}

				currencyMap = Currency.getSupportedCurreny(userDao.findPayId(merchantPayId));
				setCurrencyMap(currencyMap);
				setMerchantList(userDao.getMerchantActiveList());
			} else if (user.getUserType().equals(UserType.SUBUSER)) {

				String parentPayId = user.getParentPayId();
				User parentUser = userDao.findPayId(parentPayId);
				if (!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(parentUser.getSuperMerchantId());
					merchantPayId = superMerchant.getPayId();
					businessName = superMerchant.getBusinessName();
					subMerchantBusinessName = parentUser.getBusinessName();
					subMerchantId = parentUser.getPayId();
					currencyMap = Currency.getSupportedCurreny(superMerchant);
				} else if (parentUser.getUserType().equals(UserType.MERCHANT) && parentUser.isSuperMerchant()) {
					setSuperMerchant(true);
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentUser.getPayId()));
					
					currencyMap = Currency.getSupportedCurreny(parentUser);
					merchantPayId = user.getParentPayId();
					businessName = parentUser.getBusinessName();
				} else{
					currencyMap = Currency.getSupportedCurreny(parentUser);
					merchantPayId = user.getParentPayId();
					businessName = parentUser.getBusinessName();

				}

				setMerchantList(userDao.getMerchantActive(parentUser.getEmailId()));

				setCurrencyMap(currencyMap);
			} else if (user.getUserType().equals(UserType.MERCHANT)) {
				setMerchantList(userDao.getMerchantActive(user.getEmailId()));

				if (user.isSuperMerchant()) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(user.getPayId()));
					businessName = user.getBusinessName();
					merchantPayId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(user.getSuperMerchantId());
					
					merchantPayId = user.getSuperMerchantId();
					subMerchantId = user.getPayId();
					businessName = superMerchant.getBusinessName();
					subMerchantBusinessName = user.getBusinessName();
					currencyMap = Currency.getSupportedCurreny(superMerchant);

					Merchants merchant = new Merchants();
					merchant.setPayId(user.getPayId());
					merchant.setEmailId(user.getEmailId());
					merchant.setBusinessName(user.getBusinessName());
					merchant.setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
					subMerchantList.add(merchant);
					setSubMerchantList(subMerchantList);

				} else {
					businessName = user.getBusinessName();
					merchantPayId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				}

				setCurrencyMap(currencyMap);
			}

			// Checking Data and Storing in DB
			if (fileData.size() != 0) {
				if (fileData.size() == 1) {
					setFileIsEmpty(1);
					return SUCCESS;
				}
				for (int i = 0; i < fileData.size(); i++) {
					line = fileData.get(i);
					ImpsDownloadObject impsData = new ImpsDownloadObject();
					rowCount++;
					String data[] = line.split(",", -1);
					if (rowCount == 0) {
						if (data[0].equalsIgnoreCase("Bene Name") && data[1].equalsIgnoreCase("Bene Account Number")
								&& data[2].equalsIgnoreCase("Ifsc") && data[3].equalsIgnoreCase("Phone Number")) {
							setWrongCsv(0);
							continue;
						} else {
							setWrongCsv(1);
							break;
						}
					}

					if (data.length > 4 || data.length < 4) {

						failedData++;
						continue;
					}

					// Checking mandatory Field is not Empty
					if (StringUtils.isBlank(data[0])) {
						failedData++;
						continue;
					}
					if (StringUtils.isBlank(data[1]) && StringUtils.isBlank(data[2])) {
						failedData++;
						continue;
					}

					if (StringUtils.isNotBlank(subMerchantId)) {
						impsData.setMerchantPayId(subMerchantId);
					} else {
						impsData.setMerchantPayId(merchantPayId);
					}

					impsData.setOrderId(TransactionManager.getNewTransactionId());
					impsData.setBankAccountName((String) data[0]);
					impsData.setBankAccountNumber((String) data[1]);
					impsData.setBankIFSC((String) data[2]);
					impsData.setCurrencyCode("356");
					if (StringUtils.isNotEmpty(data[3])) {
						impsData.setPhoneNo((String) data[3]);
					}

					if (StringUtils.isNotBlank(impsData.getPhoneNo())) {
						if (!(validator.validateField(CrmFieldType.MOBILE, impsData.getPhoneNo()))) {
							failedData++;
							continue;
						}
					}
					if (!(validator.validateField(CrmFieldType.ACC_HOLDER_NAME, impsData.getBankAccountName()))) {
						failedData++;
						continue;
					}
					if (!(validator.validateField(CrmFieldType.ACCOUNT_NO, impsData.getBankAccountNumber()))) {
						failedData++;
						continue;
					}
					if (!(validator.validateField(CrmFieldType.IFSC_CODE, impsData.getBankIFSC()))) {
						failedData++;
						continue;
					}

					impsDataList.add(impsData);
					SuccessData++;
				}

				Runnable r = new Runnable() {
					public synchronized void run() {
						List<ImpsDownloadObject> listOfImps = impsDataList;
						for (ImpsDownloadObject imps : listOfImps) {
							try {
								Map<String, String> requestMap = new HashMap<>();
								Map<String, String> respMap = new HashMap<>();
								requestMap.put(FieldType.ORDER_ID.getName(), imps.getOrderId());
								requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), imps.getBankAccountNumber());
								requestMap.put(FieldType.BENE_NAME.getName(), imps.getBankAccountName());
								requestMap.put(FieldType.IFSC_CODE.getName(), imps.getBankIFSC());
								requestMap.put(FieldType.PAY_ID.getName(), imps.getMerchantPayId());
								requestMap.put(FieldType.CURRENCY_CODE.getName(), imps.getCurrencyCode());
								if (StringUtils.isNotBlank(imps.getPhoneNo())) {
									requestMap.put(FieldType.PHONE_NO.getName(), imps.getPhoneNo());
								}
								requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
								respMap = transactionControllerServiceProvider.beneVerificationTransact(requestMap);
								logger.info("Imps Verification from Bulk Upload status is for order id "
										+ imps.getOrderId() + "" + " is " + respMap.get(FieldType.STATUS.getName()));
							} catch (Exception e) {
								logger.error("Exception " , e);
							}
						}
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
		} catch (Exception e) {
			logger.error("exception " , e);
		}
		return SUCCESS;
	}

	@SkipValidation
	public String beneVerificationReport() {
		try {
			User sessionUser=(User) sessionMap.get(Constants.USER);
			setImpsDataList(impsDao.fetchBeneVerificationReportData(dateFrom, dateTo, merchantPayId, subMerchantId,
					status, beneAccountNumber, sessionUser));
			if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				flag = true;
				setFlag(flag);
			} else if (sessionUser.isSuperMerchant() == true) {
				flag = true;
				setFlag(flag);
			} else if (StringUtils.isNotBlank(subMerchantId)) {
				flag = true;
				setFlag(flag);
			} else {
				flag = false;
				setFlag(flag);
			}
		} catch (Exception e) {
			logger.info("Exception " + e);
		}
		return SUCCESS;
	}

	@SkipValidation
	public String downloadBeneVerificationData() {
		try {
			User sessionUser=(User) sessionMap.get(Constants.USER);
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				List<ImpsDownloadObject> beneList = impsDao.fetchBeneVerificationReportData(dateFrom, dateTo,
						merchantPayId, subMerchantId, status, beneAccountNumber, sessionUser);

				logger.info("List generated successfully for Download Beneficiary Report");
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Added Beneficiary Report");
				row = sheet.createRow(0);

				if (StringUtils.isNotBlank(subMerchantId)) {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Sub Merchant Name");
					row.createCell(2).setCellValue("Pay ID");
					row.createCell(3).setCellValue("Transaction ID");
					row.createCell(4).setCellValue("Order ID");
					row.createCell(5).setCellValue("Bank Account Name");
					row.createCell(6).setCellValue("Bank Account Number");
					row.createCell(7).setCellValue("IFSC Code");
					row.createCell(8).setCellValue("Date");
					row.createCell(9).setCellValue("Mobile");
					row.createCell(10).setCellValue("Status");
					row.createCell(11).setCellValue("Bank Response Msg");
					row.createCell(12).setCellValue("Bene Request Name");

					for (ImpsDownloadObject bene : beneList) {
						row = sheet.createRow(rownum++);

						if (bene.getStatus().equalsIgnoreCase("Captured")) {
							bene.setStatus("Verified");
						} else if (bene.getStatus().equalsIgnoreCase("Timeout") || bene.getStatus().equalsIgnoreCase(StatusType.PROCESSING.getName())) {
							bene.setStatus("Pending");
						} else {
							bene.setStatus("Failed");
						}

						Object[] objArr = bene.csvForBeneVerificationSuperMerchant();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}

				} else {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Pay ID");
					row.createCell(2).setCellValue("Transaction ID");
					row.createCell(3).setCellValue("Order ID");
					row.createCell(4).setCellValue("Bank Account Name");
					row.createCell(5).setCellValue("Bank Account Number");
					row.createCell(6).setCellValue("IFSC Code");
					row.createCell(7).setCellValue("Date");
					row.createCell(8).setCellValue("Mobile");
					row.createCell(9).setCellValue("Status");
					row.createCell(10).setCellValue("Bank Response Msg");
					row.createCell(11).setCellValue("Bene Request Name");

					for (ImpsDownloadObject bene : beneList) {
						row = sheet.createRow(rownum++);

						if (bene.getStatus().equalsIgnoreCase("Captured")) {
							bene.setStatus("Verified");
						} else if (bene.getStatus().equalsIgnoreCase("Timeout")) {
							bene.setStatus("Pending");
						} else {
							bene.setStatus("Failed");
						}

						Object[] objArr = bene.csvForBeneVerificationMerchant();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}

				}

				String FILE_EXTENSION = ".csv";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				fileName = "Added Beneficiary Report " + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(fileName);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(fileName + " written successfully on disk.");
				logger.info("File generated successfully for Beneficiary Report");

			}
		} catch (Exception e) {
			logger.error("exception " , e);

		}

		return SUCCESS;
	}

	// VPA verification

	@SkipValidation
	public String upiVerification() {

		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;

		try {
			if (StringUtils.isNotBlank(subMerchantId)) {
				requestMap.put(FieldType.PAY_ID.getName(), subMerchantId);
			} else {
				requestMap.put(FieldType.PAY_ID.getName(), merchantPayId);
			}

			requestMap.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId());

			if (StringUtils.isNotBlank(beneVpa)) {
				requestMap.put(FieldType.PAYER_ADDRESS.getName(), beneVpa);

			}

			if (StringUtils.isNotBlank(benePhone)) {
				requestMap.put(FieldType.PHONE_NO.getName(), benePhone);
			}

			requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));

			respMap = transactionControllerServiceProvider.vpaBeneVerificationTransact(requestMap);

			if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.VERIFIED.getName())) {
				setResponse("success");

				if (StringUtils.isNotBlank(respMap.get(FieldType.PAYER_NAME.getName()))) {
					impsData.setPayerName(respMap.get(FieldType.PAYER_NAME.getName()));
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.PAYER_ADDRESS.getName()))) {
					impsData.setPayerAddress(respMap.get(FieldType.PAYER_ADDRESS.getName()));
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.PAYER_IFSC.getName()))) {
					impsData.setBankIFSC(respMap.get(FieldType.PAYER_IFSC.getName()));
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.PHONE_NO.getName()))) {
					impsData.setPhoneNo(respMap.get(FieldType.PHONE_NO.getName()));
				}else{
					impsData.setPhoneNo(Constants.NA.getValue());
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.ACCOUNT_TYPE.getName()))) {
					impsData.setAccountType(respMap.get(FieldType.ACCOUNT_TYPE.getName()));
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.USER_TYPE.getName()))) {
					impsData.setOwnerType(respMap.get(FieldType.USER_TYPE.getName()));
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.STATUS.getName()))) {
					impsData.setStatus(respMap.get(FieldType.STATUS.getName()));
				}
			} else {
				if (StringUtils.isNotBlank(respMap.get(FieldType.PAYER_ADDRESS.getName()))) {
					impsData.setPayerAddress(respMap.get(FieldType.PAYER_ADDRESS.getName()));
				}
				if (StringUtils.isNotBlank(respMap.get(FieldType.STATUS.getName()))) {
					impsData.setStatus(respMap.get(FieldType.STATUS.getName()));
				}
				setResponse("failed");
			}

			setImpsData(impsData);

			// if
			// (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.VERIFIED.getName()))
			// {
			// setResponse("success");
			// setResponseMsg("VPA Successfully Verified");
			// } else if
			// (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PENDING.getName()))
			// {
			// setResponse("success");
			// setResponseMsg("VPA Verification is Pending");
			// } else {
			// setResponse("failed");
			// setResponseMsg("VPA Not Verified");
			// }

		} catch (SystemException e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}

	@SkipValidation
	public String vpaBeneVerificationReport() {
		try {
			User sessionUser=(User) sessionMap.get(Constants.USER);
			
			setImpsDataList(impsDao.fetchVpaBeneVerificationReportData(dateFrom, dateTo, merchantPayId, subMerchantId,
					status, beneVpa, accountType,sessionUser));
			if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				flag = true;
				setFlag(flag);
			} else if (sessionUser.isSuperMerchant() == true) {
				flag = true;
				setFlag(flag);
			} else if (StringUtils.isNotBlank(subMerchantId)) {
				flag = true;
				setFlag(flag);
			} else {
				flag = false;
				setFlag(flag);
			}
		} catch (Exception e) {
			logger.info("Exception " + e);
		}
		return SUCCESS;
	}

	@SkipValidation
	public String downloadVpaBeneVerificationData() {
		try {
			User sessionUser=(User) sessionMap.get(Constants.USER);
			
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				List<ImpsDownloadObject> beneList = impsDao.fetchVpaBeneVerificationReportData(dateFrom, dateTo,
						merchantPayId, subMerchantId, status, beneVpa, accountType, sessionUser);

				logger.info("List generated successfully for Download Beneficiary Report");
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Beneficiary Validation Report");
				row = sheet.createRow(0);

				if (StringUtils.isNotBlank(subMerchantId)) {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Sub Merchant Name");
					row.createCell(2).setCellValue("Pay ID");
					row.createCell(3).setCellValue("Transaction ID");
					row.createCell(4).setCellValue("Order ID");
					row.createCell(5).setCellValue("Payer Name");
					row.createCell(6).setCellValue("VPA");
					row.createCell(7).setCellValue("IFSC Code");
					row.createCell(8).setCellValue("Date");
					row.createCell(9).setCellValue("Mobile");
					row.createCell(10).setCellValue("Status");
					row.createCell(11).setCellValue("Bank Response Msg");

					for (ImpsDownloadObject bene : beneList) {
						row = sheet.createRow(rownum++);
//
//						if (bene.getStatus().equalsIgnoreCase("Captured")) {
//							bene.setStatus("Verified");
//						} else if (bene.getStatus().equalsIgnoreCase("Timeout")) {
//							bene.setStatus("Pending");
//						} else {
//							bene.setStatus("Failed");
//						}

						Object[] objArr = bene.csvForVpaBeneVerificationSuperMerchant();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}

				} else {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Pay ID");
					row.createCell(2).setCellValue("Transaction ID");
					row.createCell(3).setCellValue("Order ID");
					row.createCell(4).setCellValue("Payer Name");
					row.createCell(5).setCellValue("VPA");
					row.createCell(6).setCellValue("IFSC Code");
					row.createCell(7).setCellValue("Date");
					row.createCell(8).setCellValue("Mobile");
					row.createCell(9).setCellValue("Status");
					row.createCell(10).setCellValue("Bank Response Msg");

					for (ImpsDownloadObject bene : beneList) {
						row = sheet.createRow(rownum++);

						if (bene.getStatus().equalsIgnoreCase("Captured")) {
							bene.setStatus("Verified");
						} else if (bene.getStatus().equalsIgnoreCase("Timeout")) {
							bene.setStatus("Pending");
						} else {
							bene.setStatus("Failed");
						}

						Object[] objArr = bene.csvForVpaBeneVerificationMerchant();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}

				}

				String FILE_EXTENSION = ".csv";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				fileName = "VPA Validation Report " + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(fileName);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(fileName + " written successfully on disk.");
				logger.info("File generated successfully for Beneficiary Report");

			}
		} catch (Exception e) {
			logger.error("exception " , e);

		}

		return SUCCESS;
	}

	@SkipValidation
	public String bulkVpaVerificationRequest() {

		currencyMap = new HashMap<String, String>();
		merchantList = new ArrayList<Merchants>();
		impsDataList = new ArrayList<ImpsDownloadObject>();
		failedListShow = new HashMap<Long, String>();

		String line = "";
		try {
			String fileExtension = FilenameUtils.getExtension(fileName);

			if (fileExtension.equals("csv")) {
				fileData = filterCsvFile(csvFile);
			} else {
				setWrongCsv(1);
				return SUCCESS;
			}

			// Setting FileName for storing in DB Format(fileName_DateTime)
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date fileDate = new Date();
			String newFileName = fileName.concat("_").concat(dateFormat.format(fileDate));

			// Checking Merchant using Session or Input
			User user = (User) sessionMap.get(Constants.USER);
			UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS);
			
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.SUPERADMIN)) {
				if (StringUtils.isNotBlank(subMerchantId)) {
					businessName = userDao.getBusinessNameByPayId(merchantPayId);
					subMerchantBusinessName = userDao.getBusinessNameByPayId(subMerchantId);
				} else {
					businessName = userDao.getBusinessNameByPayId(merchantPayId);
				}

				currencyMap = Currency.getSupportedCurreny(userDao.findPayId(merchantPayId));
				setCurrencyMap(currencyMap);
				setMerchantList(userDao.getMerchantActiveList());
			} else if (user.getUserType().equals(UserType.SUBUSER)) {

				String parentPayId = user.getParentPayId();
				User parentUser = userDao.findPayId(parentPayId);
				if (!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(parentUser.getSuperMerchantId());
					merchantPayId = superMerchant.getPayId();
					businessName = superMerchant.getBusinessName();
					subMerchantBusinessName = parentUser.getBusinessName();
					subMerchantId = parentUser.getPayId();
					currencyMap = Currency.getSupportedCurreny(superMerchant);
				} else if (parentUser.getUserType().equals(UserType.MERCHANT) && parentUser.isSuperMerchant()) {
					setSuperMerchant(true);
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentUser.getPayId()));
					
					currencyMap = Currency.getSupportedCurreny(parentUser);
					merchantPayId = user.getParentPayId();
					businessName = parentUser.getBusinessName();
				} else {
					currencyMap = Currency.getSupportedCurreny(parentUser);
					merchantPayId = user.getParentPayId();
					businessName = parentUser.getBusinessName();

				}

				setMerchantList(userDao.getMerchantActive(parentUser.getEmailId()));

				setCurrencyMap(currencyMap);
			} else if (user.getUserType().equals(UserType.MERCHANT)) {
				setMerchantList(userDao.getMerchantActive(user.getEmailId()));

				if (user.isSuperMerchant()) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(user.getPayId()));
					businessName = user.getBusinessName();
					merchantPayId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(user.getSuperMerchantId());
					merchantPayId = user.getSuperMerchantId();
					subMerchantId = user.getPayId();
					businessName = superMerchant.getBusinessName();
					subMerchantBusinessName = user.getBusinessName();
					currencyMap = Currency.getSupportedCurreny(superMerchant);

					Merchants merchant = new Merchants();
					merchant.setPayId(user.getPayId());
					merchant.setEmailId(user.getEmailId());
					merchant.setBusinessName(user.getBusinessName());
					merchant.setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
					subMerchantList.add(merchant);
					setSubMerchantList(subMerchantList);

				} else {
					businessName = user.getBusinessName();
					merchantPayId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				}

				setCurrencyMap(currencyMap);
			}

			// Checking Data and Storing in DB
			if (fileData.size() != 0) {
				if (fileData.size() == 1) {
					setFileIsEmpty(1);
					return SUCCESS;
				}
				for (int i = 0; i < fileData.size(); i++) {
					line = fileData.get(i);
					ImpsDownloadObject impsData = new ImpsDownloadObject();
					rowCount++;
					String data[] = line.split(",", -1);
					if (rowCount == 0) {
						if (data[0].equalsIgnoreCase("VPA") && data[1].equalsIgnoreCase("Phone No")) {
							setWrongCsv(0);
							continue;
						} else {
							setWrongCsv(1);
							break;
						}
					}

					if (data.length > 2 || data.length < 2) {

						failedData++;
						continue;
					}

					// Checking mandatory Field is not Empty
					if (StringUtils.isBlank(data[0])) {
						failedData++;
						continue;
					}

					if (StringUtils.isNotBlank(subMerchantId)) {
						impsData.setMerchantPayId(subMerchantId);
					} else {
						impsData.setMerchantPayId(merchantPayId);
					}

					impsData.setOrderId(TransactionManager.getNewTransactionId());
					impsData.setPayerAddress((String) data[0]);
					if (StringUtils.isNotBlank((String) data[1])) {
						impsData.setPhoneNo((String) data[1]);
					}

					if (StringUtils.isNotBlank(impsData.getPhoneNo())) {
						if (!(validator.validateField(CrmFieldType.MOBILE, impsData.getPhoneNo()))) {
							failedData++;
							continue;
						}
					}

					if (!(validator.validateField(CrmFieldType.VPA, impsData.getPayerAddress()))) {
						failedData++;
						continue;
					}

					impsDataList.add(impsData);
					SuccessData++;
				}

				Runnable r = new Runnable() {
					public synchronized void run() {
						List<ImpsDownloadObject> listOfImps = impsDataList;
						for (ImpsDownloadObject imps : listOfImps) {
							try {
								Map<String, String> requestMap = new HashMap<>();
								Map<String, String> respMap = new HashMap<>();
								requestMap.put(FieldType.ORDER_ID.getName(), imps.getOrderId());
								requestMap.put(FieldType.PAYER_ADDRESS.getName(), imps.getPayerAddress());
								if (StringUtils.isNotBlank(imps.getPhoneNo()))
									requestMap.put(FieldType.PHONE_NO.getName(), imps.getPhoneNo());
								requestMap.put(FieldType.PAY_ID.getName(), imps.getMerchantPayId());

								requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
								respMap = transactionControllerServiceProvider.vpaBeneVerificationTransact(requestMap);
								logger.info("Imps Verification from Bulk Upload status is for order id "
										+ imps.getOrderId() + "" + " is " + respMap.get(FieldType.STATUS.getName()));
							} catch (Exception e) {
								logger.error("Exception " , e);
							}
						}
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
		} catch (Exception e) {
			logger.error("exception " , e);
		}
		return SUCCESS;
	}

	private List<String> filterCsvFile(File file) throws IOException {
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

	public String verifyPayId() {
		try {
			User user = userDao.findPayId(verificationId);

			if (user != null) {
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					setSubMerchantId(verificationId);
					return SUCCESS;
				} else {
					setMerchantPayId(verificationId);
					return SUCCESS;
				}
	
			} else {
				logger.error("User Not found in BD ");
				return ERROR;
			}
		}catch(Exception ex) {
			logger.error("Exception cought in verifyPayId : " , ex);
			return ERROR;
		}
	}

	public String redirectToverification() {
		String response = SUCCESS;
		try {

			response = execute();

		} catch (Exception ex) {
			logger.error("Exception cought in redirectToverification : " , ex);
		}
		return response;
	}
	
	public void validator() {

		if ((validator.validateBlankField(getMerchantPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), getMerchantPayId());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getMerchantPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), getMerchantPayId());
		}

		if ((validator.validateBlankField(getCurrencyCode()))) {
			addFieldError(CrmFieldType.CURRENCY.getName(), getCurrencyCode());
		} else if (!(validator.validateField(CrmFieldType.CURRENCY, getCurrencyCode()))) {
			addFieldError(CrmFieldType.CURRENCY.getName(), getCurrencyCode());
		}
		if ((validator.validateBlankField(getBenePhone()))) {

		} else if (!(validator.validateField(CrmFieldType.MOBILE, getBenePhone()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), getBenePhone());
		}
		if ((validator.validateBlankField(getBeneName()))) {
			addFieldError(CrmFieldType.ACC_HOLDER_NAME.getName(), getBeneName());
		} else if (!(validator.validateField(CrmFieldType.ACC_HOLDER_NAME, getBeneName()))) {
			addFieldError(CrmFieldType.ACC_HOLDER_NAME.getName(), getBeneName());
		}
		if ((validator.validateBlankField(getBeneAccountNumber()))) {
			addFieldError(CrmFieldType.ACCOUNT_NO.getName(), getBeneAccountNumber());
		} else if (!(validator.validateField(CrmFieldType.ACCOUNT_NO, getBeneAccountNumber()))) {
			addFieldError(CrmFieldType.ACCOUNT_NO.getName(), getBeneAccountNumber());
		}
		if ((validator.validateBlankField(getBeneIfsc()))) {
			addFieldError(CrmFieldType.IFSC_CODE.getName(), getBeneIfsc());
		} else if (!(validator.validateField(CrmFieldType.IFSC_CODE, getBeneIfsc()))) {
			addFieldError(CrmFieldType.IFSC_CODE.getName(), getBeneIfsc());
		}

	}

	public int getWrongCsv() {
		return wrongCsv;
	}

	public void setWrongCsv(int wrongCsv) {
		this.wrongCsv = wrongCsv;
	}

	public Map<Long, String> getFailedListShow() {
		return failedListShow;
	}

	public void setFailedListShow(Map<Long, String> failedListShow) {
		this.failedListShow = failedListShow;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}

	public List<String> getFileData() {
		return fileData;
	}

	public void setFileData(List<String> fileData) {
		this.fileData = fileData;
	}

	public int getFileIsEmpty() {
		return fileIsEmpty;
	}

	public void setFileIsEmpty(int fileIsEmpty) {
		this.fileIsEmpty = fileIsEmpty;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getBeneName() {
		return beneName;
	}

	public void setBeneName(String beneName) {
		this.beneName = beneName;
	}

	public String getBeneAccountNumber() {
		return beneAccountNumber;
	}

	public void setBeneAccountNumber(String beneAccountNumber) {
		this.beneAccountNumber = beneAccountNumber;
	}

	public String getBeneIfsc() {
		return beneIfsc;
	}

	public void setBeneIfsc(String beneIfsc) {
		this.beneIfsc = beneIfsc;
	}

	public String getBenePhone() {
		return benePhone;
	}

	public void setBenePhone(String benePhone) {
		this.benePhone = benePhone;
	}

	public long getFailedData() {
		return failedData;
	}

	public void setFailedData(long failedData) {
		this.failedData = failedData;
	}

	public long getSuccessData() {
		return SuccessData;
	}

	public void setSuccessData(long successData) {
		SuccessData = successData;
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

	public List<ImpsDownloadObject> getImpsDataList() {
		return impsDataList;
	}

	public void setImpsDataList(List<ImpsDownloadObject> impsDataList) {
		this.impsDataList = impsDataList;
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

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public ImpsDownloadObject getImpsData() {
		return impsData;
	}

	public void setImpsData(ImpsDownloadObject impsData) {
		this.impsData = impsData;
	}

	public String getBeneVpa() {
		return beneVpa;
	}

	public void setBeneVpa(String beneVpa) {
		this.beneVpa = beneVpa;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getVerificationId() {
		return verificationId;
	}

	public void setVerificationId(String verificationId) {
		this.verificationId = verificationId;
	}

	public boolean isSuperMerchant() {
		return superMerchant;
	}

	public void setSuperMerchant(boolean superMerchant) {
		this.superMerchant = superMerchant;
	}

}
