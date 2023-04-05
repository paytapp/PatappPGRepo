package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.NodalTransferDao;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.email.PepipostEmailSender;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountStatement;
import com.paymentgateway.commons.user.CibNodalTransaction;
import com.paymentgateway.commons.user.CibNodalTransferBene;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.ExcelReaderUtils;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * Shiva
 */
public class NodalTransferAction extends AbstractSecureAction {

	private static final long serialVersionUID = -2004145619249084246L;
	private static Logger logger = LoggerFactory.getLogger(NodalTransferAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private NodalTransferDao nodalTransferDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private PayoutAcquirerMappingDao PayoutAcquirerMappingDao;

	@Autowired
	private ExcelReaderUtils excelReaderUtils;

	@Autowired
	private PepipostEmailSender pepipostEmailSender;

	private String payId;
	private String subMerchant;
	private String bankAccountName;
	private String bankAccountNumber;
	private String bankIfsc;
	private String amount;
	private String response;
	private String responseMsg;
	private String status;
	private String dateFrom;
	private String dateTo;
	private String txnId;
	private String bankAccountNickName;
	private String payeeType;
	private String currency;
	private String txnType;
	private String remarks;
	private boolean defaultbene;
	private String utrNo;
	private String fileName;
	private InputStream fileInputStream;
	private CibNodalTransferBene beneObj = new CibNodalTransferBene();
	private List<CibNodalTransferBene> aaData;
	private List<CibNodalTransaction> tranData;
	private List<CibNodalTransaction> accountStatementData = new ArrayList<CibNodalTransaction>();
	private List<CibNodalTransaction> topUpTranData = new ArrayList<CibNodalTransaction>();
	private String downloadFileOf;
	private String fileStatus;
	private String userType;
	private String fileType;
	private String mopType;
	private String paymentType;
	private Map<String, String> topupBalance;
	private String rrnSearch;

	private User sessionUser = new User();
	public boolean flag = false;
	private final String fileLocation = "/home/Properties/cibAccountStatement/";
	private final String Composite = "Composite";
	private final String Current = "Current";

	long totalSuccess = 0;
	long totalData = 0;
	boolean wrongFile = false;
	private File csvFile;
	
	private List<CibNodalTransaction> downloadBulkNodalListItems=new ArrayList<CibNodalTransaction>();

	@SkipValidation
	public String addBene() {

		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		try {
			if (StringUtils.isNotBlank(bankAccountNumber)) {

				requestMap.put(FieldType.REQUEST_TYPE.getName(), FieldType.REQ_ADDBENE.getName());
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
				requestMap.put(FieldType.BENE_NAME.getName(), bankAccountName);
				requestMap.put(FieldType.IFSC.getName(), bankIfsc);
				requestMap.put(FieldType.BENE_ALIAS.getName(), bankAccountNickName);
				requestMap.put(FieldType.BENE_PAYEE_TYPE.getName(), payeeType);
				requestMap.put(FieldType.PAY_ID.getName(), payId);
				requestMap.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchant);
				requestMap.put(FieldType.BENE_DEFAULT.getName(), String.valueOf(defaultbene));

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
				String salt = PropertiesManager.saltStore.get(adminPayId);
				if (StringUtils.isBlank(salt)) {
					salt = (new PropertiesManager()).getSalt(adminPayId);
					if (salt != null) {
						logger.info("Salt found from propertiesManager for payId ");
					}

				} else {
					logger.info("Salt found from static map in propertiesManager");
				}
				String hashString = FieldType.REQ_ADDBENE.getName() + payId + subMerchant + bankAccountNumber
						+ bankAccountName + bankIfsc + bankAccountNickName + payeeType + defaultbene + salt;
				String hash = Hasher.getHash(hashString);

				requestMap.put(FieldType.HASH.getName(), hash);

				respMap = transactionControllerServiceProvider.nodalTransferTransact(requestMap);

				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.getName())
						|| respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(ErrorType.SUCCESS.name())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				}

			} else {
				setResponse("failed");
				setResponseMsg("Account Number is Empty");
			}
		} catch (SystemException e) {
			logger.error("exception in addBene() ", e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}

		return SUCCESS;

	}

	@SkipValidation
	public String fetchMerchantAccountDetails() {
		boolean isBeneAlreadyExist = false;

		if (StringUtils.isNotBlank(payId)) {

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				String submerchantId = payId;
				String merchantId = user.getSuperMerchantId();
				subMerchant = payId;

				isBeneAlreadyExist = nodalTransferDao.isBeneExist(merchantId, submerchantId);
			} else {
				isBeneAlreadyExist = nodalTransferDao.isBeneExist(payId, subMerchant);
			}

			if (!isBeneAlreadyExist) {
				setDefaultbene(true);

					MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
					if (mpaData != null) {
						setBankAccountName(mpaData.getAccountHolderName());
						setBankAccountNumber(mpaData.getAccountNumber());
						setBankIfsc(mpaData.getAccountIfsc());
					}

			} else {

				setDefaultbene(false);

				setBankAccountName("");
				setBankAccountNumber("");
				setBankIfsc("");
			}

		}

		return SUCCESS;

	}

	@SkipValidation
	public String fetchBene() {

		try {
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				setAaData(nodalTransferDao.fetchBeneficiaryData(payId, subMerchant, payeeType, status,
						bankAccountNumber));
			}
		} catch (Exception e) {
			logger.error("exception in fetchBene() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String downloadBeneReport() {

		try {
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				List<CibNodalTransferBene> beneList = nodalTransferDao.fetchBeneficiaryData(payId, subMerchant,
						payeeType, status, bankAccountNumber);

				logger.info("List generated successfully for Download Beneficiary Report");
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Added Beneficiary Report");
				row = sheet.createRow(0);
				if (payId.equalsIgnoreCase("All")) {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Sub Merchant Name");
					row.createCell(2).setCellValue("Alias");
					row.createCell(3).setCellValue("Pay ID");
					row.createCell(4).setCellValue("Addition Date");
					row.createCell(5).setCellValue("Beneficiary Account Name");
					row.createCell(6).setCellValue("Beneficiary Account Number");
					row.createCell(7).setCellValue("IFSC Code");
					row.createCell(8).setCellValue("Payee Type");
					row.createCell(9).setCellValue("Status");
					row.createCell(10).setCellValue("Response Message");

					for (CibNodalTransferBene bene : beneList) {
						row = sheet.createRow(rownum++);

						Object[] objArr = bene.csvMethodForSubMerchatDownloadFile();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);

							// Setting NA in blank values
							if (StringUtils.isBlank((String) obj)) {
								obj = "NA";
							} else {
								if (obj instanceof String)
									cell.setCellValue((String) obj);
								else if (obj instanceof Integer)
									cell.setCellValue((Integer) obj);
							}
						}
					}

				} else if (StringUtils.isNotBlank(subMerchant)) {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Sub Merchant Name");
					row.createCell(2).setCellValue("Alias");
					row.createCell(3).setCellValue("Pay ID");
					row.createCell(4).setCellValue("Addition Date");
					row.createCell(5).setCellValue("Beneficiary Account Name");
					row.createCell(6).setCellValue("Beneficiary Account Number");
					row.createCell(7).setCellValue("IFSC Code");
					row.createCell(8).setCellValue("Payee Type");
					row.createCell(9).setCellValue("Status");
					row.createCell(10).setCellValue("Response Message");

					for (CibNodalTransferBene bene : beneList) {
						row = sheet.createRow(rownum++);

						Object[] objArr = bene.csvMethodForSubMerchatDownloadFile();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);

							// Setting NA in blank values
							if (StringUtils.isBlank((String) obj)) {
								obj = "NA";
							} else {

								if (obj instanceof String)
									cell.setCellValue((String) obj);
								else if (obj instanceof Integer)
									cell.setCellValue((Integer) obj);
							}
						}
					}

				} else {
					User user = userDao.findPayId(payId);

					if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
						row.createCell(0).setCellValue("Merchant Name");
						row.createCell(1).setCellValue("Sub Merchant Name");
						row.createCell(2).setCellValue("Alias");
						row.createCell(3).setCellValue("Pay ID");
						row.createCell(4).setCellValue("Addition Date");
						row.createCell(5).setCellValue("Beneficiary Account Name");
						row.createCell(6).setCellValue("Beneficiary Account Number");
						row.createCell(7).setCellValue("IFSC Code");
						row.createCell(8).setCellValue("Payee Type");
						row.createCell(9).setCellValue("Status");
						row.createCell(10).setCellValue("Response Message");

						for (CibNodalTransferBene bene : beneList) {
							row = sheet.createRow(rownum++);

							Object[] objArr = bene.csvMethodForSubMerchatDownloadFile();

							int cellnum = 0;
							for (Object obj : objArr) {
								// this line creates a cell in the next column
								// of that row
								Cell cell = row.createCell(cellnum++);

								// Setting NA in blank values
								if (StringUtils.isBlank((String) obj)) {
									obj = "NA";
								} else {

									if (obj instanceof String)
										cell.setCellValue((String) obj);
									else if (obj instanceof Integer)
										cell.setCellValue((Integer) obj);
								}

							}
						}

					} else {
						row.createCell(0).setCellValue("Merchant Name");
						row.createCell(1).setCellValue("Alias");
						row.createCell(2).setCellValue("Pay ID");
						row.createCell(3).setCellValue("Addition Date");
						row.createCell(4).setCellValue("Beneficiary Account Name");
						row.createCell(5).setCellValue("Beneficiary Account Number");
						row.createCell(6).setCellValue("IFSC Code");
						row.createCell(7).setCellValue("Payee Type");
						row.createCell(8).setCellValue("Status");
						row.createCell(9).setCellValue("Response Message");

						for (CibNodalTransferBene bene : beneList) {
							row = sheet.createRow(rownum++);

							Object[] objArr = bene.csvMethodForDownloadFile();

							int cellnum = 0;
							for (Object obj : objArr) {
								// this line creates a cell in the next column
								// of that row
								Cell cell = row.createCell(cellnum++);

								// Setting NA in blank values
								if (StringUtils.isBlank((String) obj)) {
									obj = "NA";
								} else {

									if (obj instanceof String)
										cell.setCellValue((String) obj);
									else if (obj instanceof Integer)
										cell.setCellValue((Integer) obj);
								}

							}
						}
					}
				}

				String FILE_EXTENSION = ".csv";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				fileName = "Added Beneficiary Report" + df.format(new Date()) + FILE_EXTENSION;
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
			logger.error("exception in downloadBeneReport() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String beneStatusCheck() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		try {
			if (StringUtils.isNotBlank(bankAccountNumber)) {

				requestMap.put(FieldType.REQUEST_TYPE.getName(), FieldType.REQ_VALIDBENE.getName());
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
				String salt = PropertiesManager.saltStore.get(adminPayId);

				if (StringUtils.isBlank(salt)) {
					salt = (new PropertiesManager()).getSalt(adminPayId);
					if (salt != null) {
						logger.info("Salt found from propertiesManager for payId ");
					}

				} else {
					logger.info("Salt found from static map in propertiesManager");
				}
				String hashString = FieldType.REQ_VALIDBENE.getName() + bankAccountNumber + salt;
				String hash = Hasher.getHash(hashString);

				requestMap.put(FieldType.HASH.getName(), hash);
				respMap = transactionControllerServiceProvider.nodalTransferTransact(requestMap);

				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(ErrorType.SUCCESS.name())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				}

			} else {
				setResponse("failed");
				setResponseMsg("Account Number is Empty");
			}

		} catch (Exception e) {
			logger.error("exception in beneStatusCheck() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String fundTransfer() {

		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		try {
			if (StringUtils.isNotBlank(bankAccountNumber)) {

				requestMap.put(FieldType.REQUEST_TYPE.getName(), FieldType.REQ_TRANSACTION.getName());
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
				requestMap.put(FieldType.PAYEE_NAME.getName(), bankAccountName);
				requestMap.put(FieldType.IFSC.getName(), bankIfsc);
				requestMap.put(FieldType.AMOUNT.getName(), amount);
				requestMap.put(FieldType.CURRENCY_CODE.getName(), currency);
				requestMap.put(FieldType.TXNTYPE.getName(), txnType);
				requestMap.put(FieldType.REMARKS.getName(), remarks);
				requestMap.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchant);
				requestMap.put(FieldType.PAY_ID.getName(), payId);

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
				String salt = PropertiesManager.saltStore.get(adminPayId);
				if (StringUtils.isBlank(salt)) {
					salt = (new PropertiesManager()).getSalt(adminPayId);
					if (salt != null) {
						logger.info("Salt found from propertiesManager for payId ");
					}

				} else {
					logger.info("Salt found from static map in propertiesManager");
				}
				String hashString = FieldType.REQ_TRANSACTION.getName() + payId + subMerchant + bankAccountNumber
						+ bankAccountName + bankIfsc + amount + currency + txnType + remarks + salt;
				String hash = Hasher.getHash(hashString);

				requestMap.put(FieldType.HASH.getName(), hash);

				if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					respMap = transactionControllerServiceProvider.nodalTopupTransferTransact(requestMap);
				} else {
					respMap = transactionControllerServiceProvider.nodalTransferTransact(requestMap);
				}

				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.name())) {
					setResponse("success");
				} else {
					setResponse("failed");
				}

				if (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName()))) {
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				}

			} else {
				setResponse("failed");
				setResponseMsg("Account Number is Empty");
			}
		} catch (SystemException e) {
			logger.error("exception in fundTransfer() ", e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}

		return SUCCESS;

	}

	@SkipValidation
	public String tranStatusCheck() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;

		try {
			if (StringUtils.isNotBlank(txnId)) {

				requestMap.put(FieldType.REQUEST_TYPE.getName(), FieldType.REQ_TRANSACTION_INQUIRY.getName());
				requestMap.put(FieldType.TXN_ID.getName(), txnId);

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
				String salt = PropertiesManager.saltStore.get(adminPayId);

				if (StringUtils.isBlank(salt)) {
					salt = (new PropertiesManager()).getSalt(adminPayId);
					if (salt != null) {
						logger.info("Salt found from propertiesManager for payId ");
					}

				} else {
					logger.info("Salt found from static map in propertiesManager");
				}

				String hashString = FieldType.REQ_TRANSACTION_INQUIRY.getName() + txnId + salt;
				String hash = Hasher.getHash(hashString);

				requestMap.put(FieldType.HASH.getName(), hash);

				respMap = transactionControllerServiceProvider.nodalTransferTransact(requestMap);

				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					setResponse("success");
				} else {
					setResponse("failed");
				}

				if (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName()))) {
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				}

			} else {
				setResponse("failed");
				setResponseMsg("Transaction Id is Empty");
			}

		} catch (Exception e) {
			logger.error("exception in tranStatusCheck() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String fetchTransaction() {

		try {

			logger.info("Inside fetchReportData()");
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String merchantPayId = "";
			String subMerchantPayId = "";
			if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			} else {
				merchantPayId = payId;

				if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
					subMerchantPayId = subMerchant;
				}
				if (StringUtils.isNotBlank(subMerchant) && subMerchant.equalsIgnoreCase("All")) {
					subMerchantPayId = subMerchant;
				}
			}
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				setTranData(nodalTransferDao.fetchTransactionData(dateFrom, dateTo, merchantPayId, subMerchantPayId,
						status, txnId, utrNo));
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					flag = true;
					setFlag(flag);
				} else if (sessionUser.isSuperMerchant() == true) {
					flag = true;
					setFlag(flag);
				} else {
					flag = false;
					setFlag(flag);
				}
			}
		} catch (Exception e) {
			logger.error("exception in fetchTransaction() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String fetchBeneForTransaction() {
		User user = null;
		try {
			if (StringUtils.isNotBlank(payId)) {
				user = userDao.findPayId(payId);
				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					subMerchant = payId;
					payId = user.getSuperMerchantId();
				}
				setBeneObj(nodalTransferDao.getDefaultBene(payId, subMerchant));
			}
		} catch (Exception e) {
			logger.error("exception in fetchBeneForTransaction() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String downloadTranReport() {

		try {
			logger.info("Inside fetchReportData()");
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String merchantPayId = "";
			String subMerchantPayId = "";
			if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			} else {
				merchantPayId = payId;

				if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
					subMerchantPayId = subMerchant;
				}
				if (StringUtils.isNotBlank(subMerchant) && subMerchant.equalsIgnoreCase("All")) {
					subMerchantPayId = subMerchant;
				}
			}
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				List<CibNodalTransaction> tranList = nodalTransferDao.fetchTransactionData(dateFrom, dateTo,
						merchantPayId, subMerchantPayId, status, txnId, utrNo);

				logger.info("List generated successfully for Download Beneficiary Report");
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Nodal Transaction Report");
				row = sheet.createRow(0);
				if (payId.equalsIgnoreCase("All")) {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Sub Merchant Name");
					row.createCell(2).setCellValue("Txn Id");
					row.createCell(3).setCellValue("Payout Date");
					row.createCell(4).setCellValue("Captured Date To");
					row.createCell(5).setCellValue("Captured Date From");
					row.createCell(6).setCellValue("UTR No");
					row.createCell(7).setCellValue("Pay ID");
					row.createCell(8).setCellValue("Payee Name");
					row.createCell(9).setCellValue("Payee Account Number");
					row.createCell(10).setCellValue("IFSC Code");
					row.createCell(11).setCellValue("Currency");
					row.createCell(12).setCellValue("TXN Mode");
					row.createCell(13).setCellValue("Amount");
					row.createCell(14).setCellValue("Status");
					row.createCell(15).setCellValue("Response Message");
					row.createCell(16).setCellValue("Remarks");

					for (CibNodalTransaction tran : tranList) {
						row = sheet.createRow(rownum++);

						Object[] objArr = tran.csvMethodForSubMerchatDownloadFile();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);

							// Setting NA in blank values
							if (StringUtils.isBlank((String) obj)) {
								obj = "NA";
							} else {

								if (obj instanceof String)
									cell.setCellValue((String) obj);
								else if (obj instanceof Integer)
									cell.setCellValue((Integer) obj);
							}
						}
					}

				} else if (StringUtils.isNotBlank(subMerchant)) {

					row.createCell(0).setCellValue("Merchant Name");
					row.createCell(1).setCellValue("Sub Merchant Name");
					row.createCell(2).setCellValue("Txn Id");
					row.createCell(3).setCellValue("Payout Date");
					row.createCell(4).setCellValue("Captured Date To");
					row.createCell(5).setCellValue("Captured Date From");
					row.createCell(6).setCellValue("UTR No");
					row.createCell(7).setCellValue("Pay ID");
					row.createCell(8).setCellValue("Payee Name");
					row.createCell(9).setCellValue("Payee Account Number");
					row.createCell(10).setCellValue("IFSC Code");
					row.createCell(11).setCellValue("Currency");
					row.createCell(12).setCellValue("TXN Mode");
					row.createCell(13).setCellValue("Amount");
					row.createCell(14).setCellValue("Status");
					row.createCell(15).setCellValue("Response Message");
					row.createCell(16).setCellValue("Remarks");

					for (CibNodalTransaction tran : tranList) {
						row = sheet.createRow(rownum++);

						Object[] objArr = tran.csvMethodForSubMerchatDownloadFile();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of
							// that row
							Cell cell = row.createCell(cellnum++);

							// Setting NA in blank values
							if (StringUtils.isBlank((String) obj)) {
							} else {

								if (obj instanceof String)
									cell.setCellValue((String) obj);
								else if (obj instanceof Integer)
									cell.setCellValue((Integer) obj);
							}
						}
					}

				} else {
					User user = userDao.findPayId(payId);

					if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
						row.createCell(0).setCellValue("Merchant Name");
						row.createCell(1).setCellValue("Sub Merchant Name");
						row.createCell(2).setCellValue("Txn Id");
						row.createCell(3).setCellValue("Payout Date");
						row.createCell(4).setCellValue("Captured Date To");
						row.createCell(5).setCellValue("Captured Date From");
						row.createCell(6).setCellValue("UTR No");
						row.createCell(7).setCellValue("Pay ID");
						row.createCell(8).setCellValue("Payee Name");
						row.createCell(9).setCellValue("Payee Account Number");
						row.createCell(10).setCellValue("IFSC Code");
						row.createCell(11).setCellValue("Currency");
						row.createCell(12).setCellValue("TXN Mode");
						row.createCell(13).setCellValue("Amount");
						row.createCell(14).setCellValue("Status");
						row.createCell(15).setCellValue("Response Message");
						row.createCell(16).setCellValue("Remarks");

						for (CibNodalTransaction tran : tranList) {
							row = sheet.createRow(rownum++);

							Object[] objArr = tran.csvMethodForSubMerchatDownloadFile();

							int cellnum = 0;
							for (Object obj : objArr) {
								// this line creates a cell in the next column
								// of that row
								Cell cell = row.createCell(cellnum++);

								// Setting NA in blank values
								if (StringUtils.isBlank((String) obj)) {
									obj = "NA";
								} else {

									if (obj instanceof String)
										cell.setCellValue((String) obj);
									else if (obj instanceof Integer)
										cell.setCellValue((Integer) obj);
								}
							}
						}

					} else {
						row.createCell(0).setCellValue("Merchant Name");
						row.createCell(1).setCellValue("Txn Id");
						row.createCell(2).setCellValue("Payout Date");
						row.createCell(3).setCellValue("Captured Date To");
						row.createCell(4).setCellValue("Captured Date From");
						row.createCell(5).setCellValue("UTR No");
						row.createCell(6).setCellValue("Pay ID");
						row.createCell(7).setCellValue("Payee Name");
						row.createCell(8).setCellValue("Payee Account Number");
						row.createCell(9).setCellValue("IFSC Code");
						row.createCell(10).setCellValue("Currency");
						row.createCell(11).setCellValue("TXN Mode");
						row.createCell(12).setCellValue("Amount");
						row.createCell(13).setCellValue("Status");
						row.createCell(14).setCellValue("Response Message");
						row.createCell(15).setCellValue("Remarks");

						for (CibNodalTransaction tran : tranList) {
							row = sheet.createRow(rownum++);

							Object[] objArr = tran.csvMethodForDownloadFile();

							int cellnum = 0;
							for (Object obj : objArr) {
								// this line creates a cell in the next column
								// of that row
								Cell cell = row.createCell(cellnum++);

								// Setting NA in blank values
								if (StringUtils.isBlank((String) obj)) {
									obj = "NA";
								} else {

									if (obj instanceof String)
										cell.setCellValue((String) obj);
									else if (obj instanceof Integer)
										cell.setCellValue((Integer) obj);
								}
							}
						}
					}
				}

				String FILE_EXTENSION = ".csv";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				fileName = "Nodal Transaction Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(fileName);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(fileName + " written successfully on disk.");
				logger.info("File generated successfully for Nodal Transaction Report");

			}
		} catch (Exception e) {
			logger.error("exception in downloadTranReport()", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String updateDefaultBene() {

		try {
			User user = null;
			if (StringUtils.isNoneBlank(payId) && StringUtils.isNoneBlank(bankAccountNumber)) {
				user = userDao.findPayId(payId);
				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					subMerchant = payId;
					payId = user.getSuperMerchantId();
				}
				nodalTransferDao.changeDefaultBene(payId, subMerchant, bankAccountNumber);
			} else {
				setResponse("Failed");
				setResponseMsg("Empty Pay Id or Bank Account Number");
			}
			setResponse("success");

		} catch (Exception e) {
			logger.error("exception in updateDefaultBene() ", e);
			setResponse("failed");
			setResponseMsg("System Exception");
		}

		return SUCCESS;

	}

	@SkipValidation
	public String balanceInq() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			requestMap.put(FieldType.REQUEST_TYPE.getName(), FieldType.REQ_BALANCE_INQUIRY.getName());

			String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
			String salt = PropertiesManager.saltStore.get(adminPayId);

			if (StringUtils.isBlank(salt)) {
				salt = (new PropertiesManager()).getSalt(adminPayId);
				if (salt != null) {
					logger.info("Salt found from propertiesManager for payId ");
				}

			} else {
				logger.info("Salt found from static map in propertiesManager");
			}

			String hashString = FieldType.REQ_BALANCE_INQUIRY.getName() + salt;
			String hash = Hasher.getHash(hashString);

			requestMap.put(FieldType.HASH.getName(), hash);

			respMap = transactionControllerServiceProvider.nodalTransferTransact(requestMap);

			if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(ErrorType.SUCCESS.name())) {
				setResponse("success");
				setAmount(respMap.get(FieldType.AMOUNT.getName()));
			} else {
				setResponse("failed");
				setAmount(respMap.get(FieldType.AMOUNT.getName()));
				setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
			}

		} catch (Exception e) {
			logger.error("exception in balanceInq() ", e);

		}

		return SUCCESS;

	}

	@SkipValidation
	public String accountStatement() {

		try {

			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				PayoutAcquireMapping merchantMapping = PayoutAcquirerMappingDao
						.fetchMerchantMappingByPayId(sessionUser.getPayId());

				if (StringUtils.isNotBlank(merchantMapping.getUserType())) {
					setUserType(merchantMapping.getUserType());
					setFileType(merchantMapping.getAccountType());
					setDownloadFileOf(Composite);
				} else {
					logger.info("NO Mapping found for merchant");

					return SUCCESS;
				}

			}

			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)
					&& StringUtils.isNotBlank(downloadFileOf) && StringUtils.isNotBlank(fileType)) {
				String filename = userType + "_" + fileType + "_" + downloadFileOf + "_Account_Statement_Report_"
						+ dateTo + ".csv";
				String checkLatestStatus = nodalTransferDao.checkFileStatus(dateTo, dateFrom, downloadFileOf, filename,
						userType, fileType);

				if (StringUtils.isNotBlank(checkLatestStatus)
						&& checkLatestStatus.equalsIgnoreCase(StatusType.PROCESSING.getName())) {
					setStatus(checkLatestStatus);
					return SUCCESS;
				}

			}

			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					try {
						if (StringUtils.isNotBlank(downloadFileOf) && downloadFileOf.equalsIgnoreCase("Composite")) {
							generateCompositeAccountStatementExcel(dateFrom, dateTo, userType, fileType, sessionUser);
						} else {
							generateAccountStatementExcel(dateFrom, dateTo, userType, fileType);
						}
					} catch (Exception e) {
						logger.error("Exception while generating Account statement ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);

		} catch (Exception e) {
			logger.info("Exception while generating Account statement ", e);
		}

		return SUCCESS;

	}

	public List<AccountStatement> getAllRecordsforAccountStatement(Map<String, String> respMap) {
		logger.info("Creating Account Statement");
		List<AccountStatement> dataList = new ArrayList<>();
		try {
			if (respMap.containsKey("records")) {

				JSONObject responseRecordsArray = new JSONObject(respMap.get("records"));

				int totalRecords = responseRecordsArray.length();
				logger.info("Total Record in Account Statement " + totalRecords);

				for (int i = 0; i < totalRecords; i++) {

					JSONObject record = new JSONObject(responseRecordsArray.get(String.valueOf(i)).toString());

					AccountStatement accountStatement = new AccountStatement();
					accountStatement.setAmount(record.getString("AMOUNT"));
					accountStatement.setRemarks(record.getString("REMARKS"));
					accountStatement.setTxnDate(record.getString("TXNDATE"));
					accountStatement.setTxnId(record.getString("TRANSACTIONID"));
					accountStatement.setTxnType(record.getString("TYPE"));
					accountStatement.setValueDate(record.getString("VALUEDATE"));
					accountStatement.setBalance(record.getString("BALANCE"));

					dataList.add(accountStatement);

				}
			}
		} catch (Exception e) {
			logger.error("exception in getting account statement records ", e);
		}
		return dataList;

	}

	public void generateAccountStatementExcel(String dateFrom, String dateTo, String userType, String fileType) {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap = null;

		try {
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {

				insertDatalInDb("CIB", userType, fileType);

				requestMap.put(FieldType.REQUEST_TYPE.getName(), FieldType.REQ_ACCOUNT_STATEMENT.getName());
				requestMap.put(FieldType.DATEFROM.getName(), dateFrom);
				requestMap.put(FieldType.DATETO.getName(), dateTo);

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
				String salt = PropertiesManager.saltStore.get(adminPayId);
				if (StringUtils.isBlank(salt)) {
					salt = (new PropertiesManager()).getSalt(adminPayId);
					if (salt != null) {
						logger.info("Salt found from propertiesManager for payId ");
					}

				} else {
					logger.info("Salt found from static map in propertiesManager");
				}

				String hashString = FieldType.REQ_ACCOUNT_STATEMENT.getName() + dateFrom + dateTo + salt;
				String hash = Hasher.getHash(hashString);

				requestMap.put(FieldType.HASH.getName(), hash);

				try {
					respMap = transactionControllerServiceProvider.nodalTransferTransact(requestMap);
				} catch (Exception e) {
					logger.info("Exception found in getting Response in Account Statement ", e);
					updateFailedStatus(dateFrom, dateTo, "CIB", userType, fileType);
				}

				List<AccountStatement> dataList = getAllRecordsforAccountStatement(respMap);

				addFileInDir(dataList, "CIB", userType, fileType);

			}

		} catch (Exception e) {
			logger.error("exception in accountStatement() ", e);
		}
	}

	public void generateCompositeAccountStatementExcel(String dateFrom, String dateTo, String userType, String fileType,
			User sessionUser) {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap = null;

		try {
			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo) && StringUtils.isNotBlank(userType)
					&& StringUtils.isNotBlank(fileType)) {

				insertDatalInDb("Composite", userType, fileType);

				requestMap.put(FieldType.DATEFROM.getName(), dateFrom);
				requestMap.put(FieldType.DATETO.getName(), dateTo);
				requestMap.put(FieldType.USER_TYPE.getName(), userType);
				requestMap.put(FieldType.FILE_TYPE.getName(), fileType);

				if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						requestMap.put(FieldType.PAY_ID.getName(), sessionUser.getSuperMerchantId());
					} else {
						requestMap.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
					}

				}

				try {
					respMap = transactionControllerServiceProvider.compositeAccountStatementTransact(requestMap);
				} catch (Exception e) {
					logger.info("Exception found in getting Response in Account Statement ", e);
					updateFailedStatus(dateFrom, dateTo, "Composite", userType, fileType);
				}

				List<AccountStatement> dataList = getAllRecordsforAccountStatement(respMap);

				addFileInDir(dataList, "Composite", userType, fileType);

			}

		} catch (Exception e) {
			logger.error("exception in accountStatement() ", e);
		}
	}

	@SkipValidation
	public String downloadAccountStatementFile() {

		List<String> filenames = new ArrayList<String>();
		long contentLength;
		boolean dbexist = nodalTransferDao.getFileStatus(dateFrom, dateTo, fileName);

		File[] files = new File(fileLocation).listFiles();
		for (File file : files) {
			filenames.add(file.getName());
		}
		if (filenames.contains(fileName) && dbexist == true) {
			try {

				String location = fileLocation + fileName;
				File file = new File(location);
				FileInputStream inputStream = new FileInputStream(file);
				contentLength = file.length();

				setFileInputStream(inputStream);

			} catch (IOException e) {
				logger.error("Error in getting saved file: ", e);
			}

		}

		return SUCCESS;

	}

	private void addFileInDir(List<AccountStatement> dataList, String FileDownloadFor, String userType,
			String fileType) {
		if (!dataList.isEmpty()) {

			try {
				String FILE_EXTENSION = ".csv";

				String fileName = userType + "_" + fileType + "_" + FileDownloadFor + "_Account_Statement_Report_"
						+ dateTo + FILE_EXTENSION;
				File file = new File(fileLocation, fileName);

				logger.info("file name " + fileName);
				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);

				logger.info("List generated successfully for Download Account Statement");
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Account Statement Report");
				row = sheet.createRow(0);

				row.createCell(0).setCellValue("TXN ID");
				row.createCell(1).setCellValue("TXN DATE");
				row.createCell(2).setCellValue("TXN TYPE");
				row.createCell(3).setCellValue("VALUE DATE");
				row.createCell(4).setCellValue("AMOUNT");
				row.createCell(5).setCellValue("REMARKS");
				row.createCell(6).setCellValue("BALANCE");

				for (AccountStatement accState : dataList) {
					row = sheet.createRow(rownum++);

					Object[] objArr = accState.csvMethodForDownloadFile();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of
						// that
						// row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);
						else if (obj instanceof Double)
							cell.setCellValue((Double) obj);
					}
				}

				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();

				logger.info("File generated successfully for Nodal Transaction Report");

				nodalTransferDao.updateFileStatus(dateFrom, dateTo, fileName, fileLocation, FileDownloadFor, userType,
						fileType);
			} catch (Exception e) {
				logger.error("exception while adding account statement file ", e);
				updateFailedStatus(dateFrom, dateTo, FileDownloadFor, userType, fileType);
			}

		} else {
			logger.info("empty dataList for CIB Account Statement");
			updateFailedStatus(dateFrom, dateTo, FileDownloadFor, userType, fileType);
		}

	}

	private synchronized void insertDatalInDb(String FileDownloadFor, String UserType, String fileType) {
		try {
			Files.createDirectories(Paths.get(fileLocation));
		} catch (IOException e1) {
			logger.error("Error in creating Directorie ", e1);
		}

		try {

			String FILE_EXTENSION = ".csv";

			fileName = UserType + "_" + fileType + "_" + FileDownloadFor + "_Account_Statement_Report_" + dateTo
					+ FILE_EXTENSION;
			File file = new File(fileLocation, fileName);

			File[] files = new File(fileLocation).listFiles();
			for (File savedFile : files) {
				if (savedFile.getName().equalsIgnoreCase(fileName)) {
					file.delete();
				}
			}
			nodalTransferDao.deleteFileStatus(dateTo, dateFrom, fileName);

			nodalTransferDao.insertFileStatus(dateFrom, dateTo, fileName, fileLocation, FileDownloadFor, UserType,
					fileType);
		} catch (Exception e) {
			logger.info("exception ", e);
		}
	}

	private void updateFailedStatus(String dateFrom, String dateTo, String fileDownloadFor, String userType,
			String fileType) {
		try {

			String FILE_EXTENSION = ".csv";

			fileName = userType + "_" + fileType + "_" + fileDownloadFor + "_Account_Statement_Report_" + dateTo
					+ FILE_EXTENSION;
			File file = new File(fileLocation, fileName);

			File[] files = new File(fileLocation).listFiles();
			for (File savedFile : files) {
				if (savedFile.getName().equalsIgnoreCase(fileName)) {
					file.delete();
				}
			}
			nodalTransferDao.updateFailedFileStatus(dateTo, dateFrom, fileName, userType, fileType);

		} catch (Exception e) {
			logger.info("exception ", e);
		}

	}

	@SkipValidation
	public String getAccountStatementList() {

		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			PayoutAcquireMapping merchantMapping;
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

				if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantMapping = PayoutAcquirerMappingDao
							.fetchMerchantMappingByPayId(sessionUser.getSuperMerchantId());
				} else {
					merchantMapping = PayoutAcquirerMappingDao.fetchMerchantMappingByPayId(sessionUser.getPayId());
				}

				if (StringUtils.isNotBlank(merchantMapping.getUserType())) {
					setUserType(merchantMapping.getUserType());
					setFileType(merchantMapping.getAccountType());
					setDownloadFileOf(Composite);
				} else {
					logger.info("NO Mapping found for merchant");

					return SUCCESS;
				}

			}

			if (StringUtils.isNotBlank(dateTo) && StringUtils.isNotBlank(dateFrom))
				setAccountStatementData(nodalTransferDao.fetchAccountStatementData(dateTo, dateFrom, downloadFileOf,
						fileName, userType, fileType));
		} catch (Exception e) {
			logger.info("Exception while fetching the Account statement Records ", e);
		}
		return SUCCESS;
	}

	@SkipValidation
	public String getProcessingDataStatus() {

		if (StringUtils.isNotBlank(dateTo) && StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(downloadFileOf)
				&& StringUtils.isNotBlank(fileName))
			setFileStatus(
					nodalTransferDao.checkFileStatus(dateTo, dateFrom, downloadFileOf, fileName, userType, fileType));

		return SUCCESS;
	}

	public String nodalTopUpTransactionReport() {
		logger.info("Inside nodalTopUpTransactionReport : ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				topUpTranData = nodalTransferDao.fetchNodalTopUpTransactionReport(payId, subMerchant, paymentType,
						dateFrom, dateTo, txnType, rrnSearch);
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				topUpTranData = nodalTransferDao.fetchNodalTopUpTransactionReport(sessionUser.getPayId(), subMerchant,
						paymentType, dateFrom, dateTo, txnType, rrnSearch);
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}

		return SUCCESS;
	}

	public String nodalPayoutBalance() {
		logger.info("Inside nodalPayoutBalance() : ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			UserSettingData userSettings = (UserSettingData) sessionMap.get(Constants.USER_SETTINGS);

			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (StringUtils.isNotBlank(payId) && userSettings.isAllowNodalPayoutFlag()) {
					setTopupBalance(nodalTransferDao.fetchNodalPayoutBalance(payId));
				}
			}
		} catch (Exception ex) {
			logger.error("Exception : nodalPayoutBalance()", ex);
		}

		return SUCCESS;
	}

	public String downloadNodalTopUpReport() {

		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			List<CibNodalTransaction> topUpTransactionData = new ArrayList<CibNodalTransaction>();
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				topUpTransactionData = nodalTransferDao.fetchNodalTopUpTransactionReport(payId, subMerchant,
						paymentType, dateFrom, dateTo, txnType, rrnSearch);
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				topUpTransactionData = nodalTransferDao.fetchNodalTopUpTransactionReport(sessionUser.getPayId(),
						subMerchant, paymentType, dateFrom, dateTo, txnType, rrnSearch);
			}

			logger.info("List generated successfully for downloadNodalTopUpReport");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Account Statement Report");
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Sub-Merchant Name");
			row.createCell(3).setCellValue("Payment Method");
			row.createCell(4).setCellValue("Mop Type");
			row.createCell(5).setCellValue("AMOUNT");
			row.createCell(6).setCellValue("Create Date");
			row.createCell(7).setCellValue("Status");

			for (CibNodalTransaction topUpTransData : topUpTransactionData) {

				row = sheet.createRow(rownum++);
				topUpTransData.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = topUpTransData.methodForTopUpReportDownloadFile();

				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
					else if (obj instanceof Double)
						cell.setCellValue((Double) obj);
				}
			}

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "Nodal_TopUp_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(fileName + " written successfully on disk.");
			logger.info("Files generated successfully for downloadNodalTopUpoReport");
		} catch (Exception e) {
			logger.error("Exception while generating file for downloadNodalTopUpoReport ", e);
		}
		return SUCCESS;
	}

	public String processBulkNodalTransferFile() {

		String HEADER_STATUS = "Status";

		String headerRow = "S. No,Merchant Name,Sub Merchant Name,Beneficiary Name,BeneficiaryAccountNumber,IFSC Code,"
				+ "Transfer Amount,Settlement Date,Transaction Mode,Remarks";

		List<String> successData = new ArrayList<>();
		List<String> totalData = new ArrayList<>();
		
		String fileHeader = null;
 
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				if (csvFile != null) {
					
					if(!fileName.contains(".xlsx")) {
						logger.info("Wrong extention found for fileName "+fileName);
						wrongFile=true;
						setResponse("failed");
						setResponseMsg("Please upload xlsx file");
						return SUCCESS;
					}

					List<String> fileData = excelReaderUtils.readXlsxFile(csvFile);

					if (fileData.size() >= 2) {

						fileHeader = fileData.get(0);

						if (!fileHeader.equalsIgnoreCase(headerRow)) {
							logger.info("header mismatch processBulkNodalTransferFile()");
							setWrongFile(true);
							setResponse("failed");
							setResponseMsg("Header Mismatch for File");
							return SUCCESS;
						}

						fileHeader = fileHeader + "," + HEADER_STATUS + ",payId,subMerchantId";
						successData.add(fileHeader);
						fileData.remove(0);

						for (String row : fileData) {
							String[] col = row.split(",");

							if (col.length != 10) {
								if (col.length == 0) {
									logger.info("empty Record  " + row);
									continue;
								} else {
									logger.info("Invalid Record  " + row);
									row += "," + "Invalid Record," + "NA" + "NA";
									totalData.add(row);
								}
								continue;
							}

							String srNo = col[0];
							String merchant = col[1];
							String subMerchant = col[2];
							String beneName = col[3];
							String beneAccountNo = col[4];
							String ifsc = col[5];
							String amount = col[6];
							String settlementDate = col[7];
							String txnMode = col[8];
							String remarks = col[9];

							String subMerchantId = "NA";
							String payId = "";

							// Getting merchant
							User user = userDao.findByMerchantName(merchant, subMerchant);

							if (user == null) {
								logger.info("no User Found with merchant Name = " + merchant
										+ " and subMerchant Name = " + subMerchant + " row " + row);
								row += "," + "Merchant Not Found," + "NA" + "NA";
								totalData.add(row);
								
								continue;
							}

							if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
								payId = user.getSuperMerchantId();
								subMerchantId = user.getPayId();
							} else {
								payId = user.getPayId();
							}

							if (StringUtils.isBlank(beneAccountNo) && StringUtils.isBlank(ifsc)
									&& StringUtils.isBlank(beneName)) {
								logger.info("empty Beneficiary details A/c , ifsc or beneName " + row);

								row += "," + "Empty Beneficiary details," + payId + "," + subMerchantId;
								totalData.add(row);
								continue;
							}
							
							if (StringUtils.isBlank(txnMode) && !txnMode.contains("IMPS,UPI,NEFT,RTGS")) {
								logger.info("Invalid Transaction Mode " + row);

								row += "," + "Invalid Transaction Mode," + payId + "," + subMerchantId;
								totalData.add(row);
								continue;
							}

							// check beneficiary default
							if (subMerchantId.equalsIgnoreCase("NA")) {
								setBeneObj(nodalTransferDao.getDefaultBene(payId, null));
							} else {
								setBeneObj(nodalTransferDao.getDefaultBene(payId, subMerchantId));
							}

							logger.info("defaul bene account no : " + beneObj.getBankAccountNumber());

							if (StringUtils.isNotBlank(beneObj.getBankAccountNumber())
									&& beneObj.getBankAccountNumber().equalsIgnoreCase(beneAccountNo)
									&& StringUtils.isNotBlank(beneObj.getBankIfsc())
									&& beneObj.getBankIfsc().equalsIgnoreCase(ifsc)) {

								// if true then process or update status false
								row += "," + "Successful," + payId + "," + subMerchantId;
								successData.add(row);
								totalData.add(row);

							} else {
								logger.info("Default Bene Not matched " + row);
								row += "," + "Default Bene Not matched," + payId + "," + subMerchantId;
								totalData.add(row);
							}

						}
						
						setTotalSuccess(successData.size()-1);
						setTotalData(totalData.size());

						try {

							String saveFileLocation = PropertiesManager.propertiesMap.get("NODAL_BULK_FILE_PATH")
									+ "/NodalTransferFiles";
							File location = new File(saveFileLocation);
							if (!location.exists()) {
								location.mkdirs();
							}

							// Setting FileName for storing in DB
							// Format(fileName_DateTime)
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
							Date fileDate = new Date();
							fileName = fileName.replace(".xlsx", "");
							String newFileName = fileName.concat("_").concat(dateFormat.format(fileDate)) + ".xlsx";
							File file = new File(saveFileLocation, newFileName);

							SXSSFWorkbook wb = new SXSSFWorkbook(100);
							Row row;
							int rownum = 0;
							// Create a blank sheet
							Sheet sheet = wb.createSheet("Payout Report");
							row = sheet.createRow(rownum);

							int colIndex = 0;
							for (String header : fileHeader.split(",")) {
								row.createCell(colIndex).setCellValue(header);
								colIndex++;
							}

							for (String rowData : totalData) {
								rownum++;
								row = sheet.createRow(rownum);
								colIndex = 0;
								for (String col : rowData.split(",")) {
									row.createCell(colIndex).setCellValue(col);
									colIndex++;
								}
							}

							FileOutputStream fileOut = new FileOutputStream(file);
							wb.write(fileOut);
							fileOut.flush();
							fileOut.close();
							wb.dispose();

							logger.info("file generated Successfully , fileName = " + newFileName);

							logger.info(
									"total success validated records for Nodal Transactions are " + successData.size());
							if (successData.size() >= 2) {

								String header = successData.get(0) + ",UTR";
								logger.info("inside the thread for process Bulk nodal transaction " + header);
								successData.remove(0);

								// Thread For bank calls
								Runnable r = new Runnable() {
									public synchronized void run() {
										String sendEmaiOfFileCompletion = PropertiesManager.propertiesMap
												.get("NODAL_BULK_UPDATE_EMAIL");

										List<String> sucessRecordsList = successData;

										String saveFileLocation = PropertiesManager.propertiesMap
												.get("NODAL_BULK_FILE_PATH") + "/UTRUpdatedFiles";
										File location = new File(saveFileLocation);
										if (!location.exists()) {
											location.mkdirs();
										}

										// Setting FileName for storing in DB
										// Format(fileName_DateTime)
										SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
										Date fileDate = new Date();
										String fileName = "Bulk_Utr_Udate";
										String newFileName = fileName.concat("_").concat(dateFormat.format(fileDate))
												+ ".xlsx";
										File file = new File(saveFileLocation, newFileName);

										SXSSFWorkbook wb = new SXSSFWorkbook(100);
										Row row;
										int rownum = 0;
										// Create a blank sheet
										Sheet sheet = wb.createSheet("UTR Report");
										row = sheet.createRow(rownum);

										int colIndex = 0;
										for (String value : header.split(",")) {
											row.createCell(colIndex).setCellValue(value);
											colIndex++;
										}

										logger.info("running total bulk nodal transfer file with total records "
												+ sucessRecordsList.size());
										for (String rows : sucessRecordsList) {
											try {
												rownum++;
												row = sheet.createRow(rownum);

												String[] col = rows.split(",");

												String merchant = col[1];
												String subMerchant = col[2];
												String beneName = col[3];
												String beneAccountNo = col[4];
												String ifsc = col[5];
												String amount = col[6];
												String settlementDate = col[7];
												String txnMode = col[8];
												String remarks = col[9];
												String status = col[10];
												String payId = col[11];
												String subMerchantId = col[12];
												String utr = "";

												Map<String, String> requestMap = new HashMap<>();
												Map<String, String> respMap = new HashMap<>();

												requestMap.put(FieldType.REQUEST_TYPE.getName(),
														FieldType.REQ_TRANSACTION.getName());
												requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), beneAccountNo);
												requestMap.put(FieldType.PAYEE_NAME.getName(), beneName);
												requestMap.put(FieldType.IFSC.getName(), ifsc);
												requestMap.put(FieldType.AMOUNT.getName(), amount);
												requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
												requestMap.put(FieldType.TXNTYPE.getName(), txnMode);
												requestMap.put(FieldType.REMARKS.getName(), remarks);
												requestMap.put(FieldType.PAY_ID.getName(), payId);

												if (StringUtils.isNotBlank(subMerchantId)
														&& !subMerchantId.equalsIgnoreCase("NA")) {
													requestMap.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
												} else {
													requestMap.put(FieldType.SUB_MERCHANT_ID.getName(), "");
												}

												String adminPayId = PropertiesManager.propertiesMap
														.get("SAVE_CARD_ADMIN_PAYID");
												String salt = PropertiesManager.saltStore.get(adminPayId);
												if (StringUtils.isBlank(salt)) {
													salt = (new PropertiesManager()).getSalt(adminPayId);
													if (salt != null) {
														logger.info("Salt found from propertiesManager for payId ");
													}

												} else {
													logger.info("Salt found from static map in propertiesManager");
												}
												String hashString = FieldType.REQ_TRANSACTION.getName()
														+ requestMap.get(FieldType.PAY_ID.getName())
														+ requestMap.get(FieldType.SUB_MERCHANT_ID.getName())
														+ beneAccountNo + beneName + ifsc + amount
														+ requestMap.get(FieldType.CURRENCY_CODE.getName()) + txnMode
														+ remarks + salt;
												String hash = Hasher.getHash(hashString);

												requestMap.put(FieldType.HASH.getName(), hash);

												respMap = transactionControllerServiceProvider
														.nodalTransferTransact(requestMap);

												status = respMap.get(FieldType.STATUS.getName());
												if (StringUtils.isNotBlank(respMap.get(FieldType.UTR.getName())))
													utr = respMap.get(FieldType.UTR.getName());
												else
													utr = "NA";

												row.createCell(0).setCellValue(rownum);
												row.createCell(1).setCellValue(merchant);
												row.createCell(2).setCellValue(subMerchant);
												row.createCell(3).setCellValue(beneName);
												row.createCell(4).setCellValue(beneAccountNo);
												row.createCell(5).setCellValue(ifsc);
												row.createCell(6).setCellValue(amount);
												row.createCell(7).setCellValue(settlementDate);
												row.createCell(8).setCellValue(txnMode);
												row.createCell(9).setCellValue(remarks);
												row.createCell(10).setCellValue(status);
												row.createCell(11).setCellValue(payId);
												row.createCell(12).setCellValue(subMerchantId);
												row.createCell(13).setCellValue(utr);

												logger.info("Transaction Finished For Bulk Nodal Transaction TXN_ID : "
														+ respMap.get(FieldType.TXN_ID.getName()));
											} catch (Exception e) {
												logger.error("Exception ", e);
											}
										}

										try {
											FileOutputStream fileOut = new FileOutputStream(file);
											wb.write(fileOut);
											fileOut.flush();
											fileOut.close();
											wb.dispose();

											String body = "PFA";

											pepipostEmailSender.sendEmailWithAttachment(body,
													"UTR Update File " + dateFormat.format(fileDate),
													sendEmaiOfFileCompletion, null, file);
											logger.info("email sent to " + sendEmaiOfFileCompletion);

										} catch (Exception e) {
											logger.info("exception in saving UTR update File " + file.getName());
										}

									}
								};
								Thread t = new Thread(r);
								t.start();
							}

						} catch (Exception e) {
							logger.info("exception in processBulkNodalTransferFile()", e);
							setResponse("failed");
							setResponseMsg("Something went wrong");
							return SUCCESS;
						}

					} else {
						logger.info("file Empty processBulkNodalTransferFile(), " + fileName);
						setResponse("failed");
						setResponseMsg("Empty File Uploaded");
						return SUCCESS;
					}

				} else {
					setWrongFile(false);
					logger.info("file is NULL processBulkNodalTransferFile()");
					setResponse("failed");
					setResponseMsg("file is NULL");
					return SUCCESS;
				}

			} else {
				logger.info("Bulk Nodal transfer permission only for ADMIN & SUBADMIN");
				setResponse("failed");
				setResponseMsg("Permission Denied");
				return SUCCESS;
			}

		} catch (Exception e) {
			logger.info("Exception inside processBulkNodalTransferFile() ", e);
			setResponse("failed");
			setResponseMsg("Something went wrong");
			return SUCCESS;
		}
		
		setResponse("success");
		setResponseMsg("File processed Successfully");

		return SUCCESS;
	}

	public String fetchBulkFiles() {
		
		try {
			

			String saveFileLocation = PropertiesManager.propertiesMap.get("NODAL_BULK_FILE_PATH");

			String utrFilePath = saveFileLocation + "/UTRUpdatedFiles";
			String nodalTransactionFilePath = saveFileLocation + "/NodalTransferFiles";

			File folder = null;
			if (downloadFileOf.equalsIgnoreCase("UTR_FILE")) {
				folder = new File(utrFilePath);
			} else {
				folder = new File(nodalTransactionFilePath);
			}
			
			SimpleDateFormat sdfInput = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			
			File[] listOfFiles = folder.listFiles();
			
			Date fromDate = sdfInput.parse(dateFrom);
			Date toDate = sdfInput.parse(dateTo);
			
			for (File file : listOfFiles) {
				if (file.isFile()) {
					
					Date fileDate = sdfInput.parse(sdfInput.format(new Date(file.lastModified())));
					
					if(fileDate.compareTo(toDate)<=0 && fileDate.compareTo(fromDate)>=0){
						CibNodalTransaction cibNodalTransaction = new CibNodalTransaction();
						cibNodalTransaction.setFileName(file.getName());
						cibNodalTransaction.setCreateDate(sdf.format(new Date(file.lastModified())));
						downloadBulkNodalListItems.add(cibNodalTransaction);
					}
				}
			}
			
		} catch (Exception e) {
			logger.info("exception in fetchBulkFiles()", e);
		}
		return SUCCESS;
	}
	
	public String downloadBulkFile() {

		try {

			String saveFileLocation = PropertiesManager.propertiesMap.get("NODAL_BULK_FILE_PATH");

			String utrFilePath = saveFileLocation + "UTRUpdatedFiles/";
			String nodalTransactionFilePath = saveFileLocation + "NodalTransferFiles/";

			File folder = null;
			if (downloadFileOf.equalsIgnoreCase("UTR_FILE")) {
				folder = new File(utrFilePath);
			} else {
				folder = new File(nodalTransactionFilePath);
			}

			String location = folder +"/"+ fileName;
			File file = new File(location);
			FileInputStream inputStream = new FileInputStream(file);

			setFileInputStream(inputStream);

		} catch (Exception e) {
			logger.info("exception in downloadBulkFile()", e);
		}

		return SUCCESS;
	}
	
	
	private static void displayFiles(File directory, FileFilter fileFilter) {
		File[] files = directory.listFiles(fileFilter);
		for (File file : files) {
			Date lastMod = new Date(file.lastModified());
			System.out.println("File: " + file.getName() + ", Date: " + lastMod + "");
		}
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getBankIfsc() {
		return bankIfsc;
	}

	public void setBankIfsc(String bankIfsc) {
		this.bankIfsc = bankIfsc;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
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

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public List<CibNodalTransferBene> getAaData() {
		return aaData;
	}

	public void setAaData(List<CibNodalTransferBene> aaData) {
		this.aaData = aaData;
	}

	public String getBankAccountNickName() {
		return bankAccountNickName;
	}

	public void setBankAccountNickName(String bankAccountNickName) {
		this.bankAccountNickName = bankAccountNickName;
	}

	public String getPayeeType() {
		return payeeType;
	}

	public void setPayeeType(String payeeType) {
		this.payeeType = payeeType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public boolean isDefaultbene() {
		return defaultbene;
	}

	public void setDefaultbene(boolean defaultbene) {
		this.defaultbene = defaultbene;
	}

	public CibNodalTransferBene getBeneObj() {
		return beneObj;
	}

	public void setBeneObj(CibNodalTransferBene beneObj) {
		this.beneObj = beneObj;
	}

	public String getUtrNo() {
		return utrNo;
	}

	public void setUtrNo(String utrNo) {
		this.utrNo = utrNo;
	}

	public List<CibNodalTransaction> getTranData() {
		return tranData;
	}

	public void setTranData(List<CibNodalTransaction> tranData) {
		this.tranData = tranData;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public void setAccountStatementData(List<CibNodalTransaction> accountStatementData) {
		this.accountStatementData = accountStatementData;
	}

	public List<CibNodalTransaction> getAccountStatementData() {
		return accountStatementData;
	}

	public String getDownloadFileOf() {
		return downloadFileOf;
	}

	public void setDownloadFileOf(String downloadFileOf) {
		this.downloadFileOf = downloadFileOf;
	}

	public String getFileStatus() {
		return fileStatus;
	}

	public void setFileStatus(String fileStatus) {
		this.fileStatus = fileStatus;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public List<CibNodalTransaction> getTopUpTranData() {
		return topUpTranData;
	}

	public void setTopUpTranData(List<CibNodalTransaction> topUpTranData) {
		this.topUpTranData = topUpTranData;
	}

	public Map<String, String> getTopupBalance() {
		return topupBalance;
	}

	public void setTopupBalance(Map<String, String> topupBalance) {
		this.topupBalance = topupBalance;
	}

	public String getRrnSearch() {
		return rrnSearch;
	}

	public void setRrnSearch(String rrnSearch) {
		this.rrnSearch = rrnSearch;
	}

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}

	public long getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(long totalSuccess) {
		this.totalSuccess = totalSuccess;
	}


	public List<CibNodalTransaction> getDownloadBulkNodalListItems() {
		return downloadBulkNodalListItems;
	}

	public void setDownloadBulkNodalListItems(List<CibNodalTransaction> downloadBulkNodalListItems) {
		this.downloadBulkNodalListItems = downloadBulkNodalListItems;
	}

	public long getTotalData() {
		return totalData;
	}

	public void setTotalData(long totalData) {
		this.totalData = totalData;
	}

	public boolean isWrongFile() {
		return wrongFile;
	}

	public void setWrongFile(boolean wrongFile) {
		this.wrongFile = wrongFile;
	}


}
