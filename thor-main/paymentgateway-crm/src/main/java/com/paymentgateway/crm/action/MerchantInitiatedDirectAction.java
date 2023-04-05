package com.paymentgateway.crm.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.ImpsDao;
import com.paymentgateway.commons.dao.MerchantInitiatedDirectDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PayoutPupose;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Pooja Pancholi, Shiva
 *
 */

public class MerchantInitiatedDirectAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2811346574562571717L;

	private static Logger logger = LoggerFactory.getLogger(MerchantInitiatedDirectAction.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private ImpsDao impsDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private MerchantInitiatedDirectDao merchantInitiatedDirectDao;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private CrmValidator validator;

	// IMPS
	String payId;
	String subMerchantPayId;
	String beneficiaryAccountName;
	String bankAccountNumber;
	String bankAccountName;
	String bankIFSCCode;
	String mobileNumber;
	String amount;
	String remarks;
	private String response;
	private String responseMsg;
	private String businessName;
	private String subMerchantBusinessName;
	private boolean subMerchantFlag;
	private String purpose;

	// UPI
	private String vpa;
	private String payeeName;

	// bulk
	private Map<String, String> currencyMap;
	private List<Merchants> merchantList;
	private List<ImpsDownloadObject> impsDataList;
	private Map<Long, String> failedListShow;
	private List<String> fileData;
	private File csvFile;
	private String fileName;
	private List<Merchants> subMerchantList = new ArrayList<Merchants>();

	int wrongCsv = 0;
	long rowCount = -1;
	long failedData = 0;
	long SuccessData = 0;
	int fileIsEmpty = 0;
	long duplicateData = 0;
	String payoutLocation;
	String payoutFileName;
	String orderIdFileName;

	// report
	private String txnId;
	private String dateTo;
	private String dateFrom;
	private String txnType;
	private String status;
	private String orderId;
	private String beneAccountNumber;
	private String payerAddress;
	private String rrn;
	private InputStream fileInputStream;
	private String filename;
	private List<ImpsDownloadObject> aaData;
	private String finalStatus;

	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	private User sessionUser = new User();

	public String execute() {

		logger.info("Inside MerchantInitiatedDirectAction, execute()");
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		try {
			String autoOrderId = "LP" + sdf.format(new Date());
			if (StringUtils.isNotBlank(amount)) {
				BigDecimal amt = new BigDecimal((String) getAmount()).setScale(2);
				requestMap.put(FieldType.ORDER_ID.getName(), autoOrderId);
				if (StringUtils.isNotBlank(beneficiaryAccountName)) {
					requestMap.put(FieldType.BENE_NAME.getName(), beneficiaryAccountName);
				}
				if (StringUtils.isNotBlank(bankAccountNumber)) {
					requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
				}
				if (StringUtils.isNotBlank(bankAccountName)) {
					requestMap.put(FieldType.BANK_NAME.getName(), bankAccountName);
				}
				if (StringUtils.isNotBlank(bankIFSCCode)) {
					requestMap.put(FieldType.IFSC_CODE.getName(), bankIFSCCode);
				}
				if (StringUtils.isNotBlank(mobileNumber)) {
					requestMap.put(FieldType.PHONE_NO.getName(), mobileNumber);
				}
				requestMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amt.toString(), "356"));
				if (StringUtils.isNotBlank(subMerchantPayId)) {
					requestMap.put(FieldType.PAY_ID.getName(), subMerchantPayId);
				} else {
					requestMap.put(FieldType.PAY_ID.getName(), payId);
				}
				if (StringUtils.isNotBlank(remarks)) {
					requestMap.put(FieldType.REMARKS.getName(), remarks);
				}
				// requestMap.put(FieldType.USER_TYPE.getName(), "Merchant
				// Initiated Direct");
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
				
				
				if(txnType.equalsIgnoreCase("IMPS")){
					 requestMap.put(FieldType.TXNTYPE.getName(), "IMPS");
				}else if(txnType.equalsIgnoreCase("NEFT")){
					 requestMap.put(FieldType.TXNTYPE.getName(), "NEFT");
				}else{
					 requestMap.put(FieldType.TXNTYPE.getName(), "RTGS");
				}
				
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
				
				
				if (StringUtils.isNotBlank(purpose)) {
					requestMap.put(FieldType.PURPOSE.getName(), purpose);
				}
				if (!merchantInitiatedDirectDao.isDailyLimitExceeded(requestMap)) {
					
					requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
					logger.info(
							"orginal request for IMPS transaction by CRM : order Id= " + autoOrderId + " and pay Id = "
									+ requestMap.get(FieldType.PAY_ID.getName()) + " request, " + requestMap);
					respMap = transactionControllerServiceProvider.MerchantDirectInitiateTransact(requestMap);
					logger.info("response for IMPS transaction by CRM : order Id= " + autoOrderId + " response, "
							+ respMap);
					if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
							|| respMap.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.PROCESSING.getName())) {
						setResponse("success");
						setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
					} else {
						setResponse("failed");
						if (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName()))) {
							setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
						} else {
							setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
						}

					}

				} else {
					setResponse("failed");
					setResponseMsg("Declined due to insufficient balance");
				}

			} else {
				setResponse("failed");
				setResponseMsg("Amount is Empty");
			}

			// requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new
			// Fields(requestMap)));
			// logger.info("orginal request for IMPS transaction by CRM : order
			// Id= " + autoOrderId + " and pay Id = " +
			// requestMap.get(FieldType.PAY_ID.getName()) + " request, " +
			// requestMap);
			// respMap =
			// transactionControllerServiceProvider.MerchantDirectInitiateTransact(requestMap);
			// logger.info("response for IMPS transaction by CRM : order Id= " +
			// autoOrderId + " response, " + respMap);
			// if
			// (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
			// {
			// setResponse("success");
			// if(StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName())))
			// setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
			// else
			// setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
			// } else {
			// setResponse("failed");
			// if
			// (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName())))
			// {
			// setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
			// } else {
			// setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
			// }
			//
			// }
		} catch (Exception e) {
			logger.error("exception occured in impsSingleTransaction ", e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");
		}
		return SUCCESS;

	}

	public String upiSingleTransaction() {

		logger.info("Inside MerchantInitiatedDirectAction, upiSingleTransaction()");
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		try {
			String autoOrderId = "LP" + sdf.format(new Date());
			if (StringUtils.isNotBlank(amount)) {
				BigDecimal amt = new BigDecimal((String) getAmount()).setScale(2);
				requestMap.put(FieldType.ORDER_ID.getName(), autoOrderId);

				if (StringUtils.isNotBlank(vpa)) {
					requestMap.put(FieldType.PAYER_ADDRESS.getName(), vpa);
				}
				if (StringUtils.isNotBlank(payeeName)) {
					requestMap.put(FieldType.PAYER_NAME.getName(), payeeName);
				}
				if (StringUtils.isNotBlank(mobileNumber)) {
					requestMap.put(FieldType.PHONE_NO.getName(), mobileNumber);
				}
				requestMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amt.toString(), "356"));
				if (StringUtils.isNotBlank(subMerchantPayId)) {
					requestMap.put(FieldType.PAY_ID.getName(), subMerchantPayId);
				} else {
					requestMap.put(FieldType.PAY_ID.getName(), payId);
				}
				if (StringUtils.isNotBlank(remarks)) {
					requestMap.put(FieldType.REMARKS.getName(), remarks);
				}
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
				requestMap.put(FieldType.TXNTYPE.getName(), "UPI");
				// requestMap.put(FieldType.USER_TYPE.getName(), "Merchant
				// Initiated Direct");
				if (StringUtils.isNotBlank(purpose)) {
					requestMap.put(FieldType.PURPOSE.getName(), purpose);
				}
				if (!merchantInitiatedDirectDao.isDailyLimitExceeded(requestMap)) {
					
					requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
					logger.info(
							"orginal request for UPI transaction by CRM : order Id= " + autoOrderId + " and pay Id = "
									+ requestMap.get(FieldType.PAY_ID.getName()) + " request, " + requestMap);
					respMap = transactionControllerServiceProvider.MerchantDirectInitiateTransact(requestMap);
					logger.info("response for UPI transaction by CRM : order Id= " + autoOrderId + " response, "
							+ respMap);
					if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
							|| respMap.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.PROCESSING.getName())) {
						setResponse("success");
						setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
					} else {
						setResponse("failed");
						if (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName()))) {
							setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
						} else {
							setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
						}
					}

				} else {
					setResponse("failed");
					setResponseMsg("Declined due to insufficient balance");
				}

			} else {
				setResponse("failed");
				setResponseMsg("Amount is Empty");
			}

			/*
			 * requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new
			 * Fields(requestMap))); logger.
			 * info("orginal request for UPI transaction by CRM : order Id= " +
			 * autoOrderId + " and pay Id = " +
			 * requestMap.get(FieldType.PAY_ID.getName()) + " request, " +
			 * requestMap); respMap = transactionControllerServiceProvider.
			 * MerchantDirectInitiateTransact(requestMap);
			 * logger.info("response for IMPS transaction by CRM : order Id= " +
			 * autoOrderId + " response, " + respMap); if
			 * (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(
			 * StatusType.CAPTURED.getName())) { setResponse("success");
			 * setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
			 * } else { setResponse("failed"); if
			 * (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.
			 * getName()))) {
			 * setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
			 * } else {
			 * setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()))
			 * ; } }
			 */
		} catch (Exception e) {
			logger.error("exception occured in upiSingleTransaction ", e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");
		}
		return SUCCESS;
	}

	public String merchantInitiatedBulkTransaction() {

		logger.info("Inside MerchantInitiatedDirectAction, merchantInitiatedBulkTransaction()");
		currencyMap = new HashMap<String, String>();
		merchantList = new ArrayList<Merchants>();
		impsDataList = new ArrayList<ImpsDownloadObject>();
		List<ImpsDownloadObject> payoutFailedDataList = new ArrayList<ImpsDownloadObject>();
		List<ImpsDownloadObject> payoutDuplicateOrderIdDataList = new ArrayList<ImpsDownloadObject>();
		failedListShow = new HashMap<Long, String>();
		String fileLocation = PropertiesManager.propertiesMap.get(Constants.PAYOUT_FILE_LOCATION.getValue());
		String line = "";
		long totalSucces;
		long totalFailed;
		long totalDuplicate;

		try {
			String fileExtension = FilenameUtils.getExtension(fileName);

			if (fileExtension.equals("csv")) {
				fileData = filterCsvFile(csvFile);
			} else {
				logger.info("Wrong Csv");
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
				if (StringUtils.isNotBlank(subMerchantPayId)) {
					businessName = userDao.getBusinessNameByPayId(payId);
					subMerchantBusinessName = userDao.getBusinessNameByPayId(subMerchantPayId);
				} else {
					businessName = userDao.getBusinessNameByPayId(payId);
				}

				currencyMap = Currency.getSupportedCurreny(userDao.findPayId(payId));
				setCurrencyMap(currencyMap);
				setMerchantList(userDao.getMerchantActiveList());
			} else if (user.getUserType().equals(UserType.MERCHANT)) {
				setMerchantList(userDao.getMerchantActive(user.getEmailId()));

				if (user.isSuperMerchant()) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(user.getPayId()));
					businessName = user.getBusinessName();
					payId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(user.getSuperMerchantId());
					payId = user.getSuperMerchantId();
					subMerchantPayId = user.getPayId();
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
					payId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				}

				setCurrencyMap(currencyMap);
			}

			// Checking Data and Storing in DB
			if (fileData.size() != 0) {
				if (fileData.size() == 1) {
					logger.info("Bulk file is Empty, merchantInitiatedBulkTransaction()");
					setFileIsEmpty(1);
					return SUCCESS;
				}
				for (int i = 0; i < fileData.size(); i++) {
					line = fileData.get(i);
					ImpsDownloadObject impsData = new ImpsDownloadObject();
					rowCount++;
					String data[] = line.split(",", -1);
					if (rowCount == 0) {
						if (data[0].equalsIgnoreCase("Order ID") && data[1].equalsIgnoreCase("Bene Account Name")
								&& data[2].equalsIgnoreCase("Bank Account Number") && data[3].equalsIgnoreCase("IFSC")
								&& data[4].equalsIgnoreCase("Bank Name") && data[5].equalsIgnoreCase("Phone Number")
								&& data[6].equalsIgnoreCase("Amount") && data[7].equalsIgnoreCase("Purpose")
								&& data[8].equalsIgnoreCase("Remark")) {
							setWrongCsv(0);
							continue;
						} else {
							logger.info("Header not matched, merchantInitiatedBulkTransaction()");
							setWrongCsv(1);
							setResponse("failed");
							setResponseMsg("Invalid Field Format");
							break;
						}
					}

					/*
					 * if (data.length > 4 || data.length < 4) {
					 * 
					 * failedData++; continue; }
					 */

					// Checking mandatory Field is not Empty
					/*
					 * if (StringUtils.isBlank(data[0])) { failedData++;
					 * continue; }
					 */

					BigDecimal amt = new BigDecimal((String) data[6]).setScale(2);
					User userByPayId = null;
					if (StringUtils.isNotBlank(subMerchantPayId)) {
						userByPayId = userDao.findPayId(subMerchantPayId);
						impsData.setMerchantPayId(userByPayId.getSuperMerchantId());
						impsData.setSubMerchant(subMerchantPayId);
						impsData.setVirtualAccount(userByPayId.getVirtualAccountNo());
					} else {
						userByPayId = userDao.findPayId(payId);
						impsData.setMerchantPayId(payId);
						impsData.setVirtualAccount(userByPayId.getVirtualAccountNo());
					}

					if (StringUtils.isNotBlank(data[0])) {
						impsData.setOrderId((String) data[0]);
					} else {
						logger.info("Empty Order Id, merchantInitiatedBulkTransaction()");
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[1])) {
						impsData.setBeneAccountName((String) data[1]);
					} else {
						logger.info("Empty Bene Account Name for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[2])) {
						impsData.setBankAccountNumber((String) data[2]);
					} else {
						logger.info("Empty Bene Account No for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[3])) {
						impsData.setBankIFSC((String) data[3]);
					} else {
						logger.info("Empty bank Ifsc No for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[4])) {
						impsData.setBankAccountName((String) data[4]);
					}
					if (StringUtils.isNotBlank(data[5])) {
						impsData.setPhoneNo((String) data[5]);
					} else {
						logger.info("Empty Phone No for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[6])) {
						impsData.setAmount(amt.toString());
					} else {
						logger.info("Empty Amount for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[7])) {
						impsData.setPurpose((String) data[7]);
					} else {
						logger.info("Empty Purpose for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[8])) {
						impsData.setRemarks((String) data[8]);
					}

					impsData.setCurrencyCode("356");
					
					if(StringUtils.isNotBlank(txnType)){
						if(txnType.equalsIgnoreCase("IMPS"))
							impsData.setTxnType("IMPS");
						else if(txnType.equalsIgnoreCase("RTGS"))
							impsData.setTxnType("RTGS");
						else
							impsData.setTxnType("NEFT");
					}

					if (!(validator.validateField(CrmFieldType.ORDER_ID, impsData.getOrderId()))) {
						logger.info("Invalid orderId for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (!(validator.validateField(CrmFieldType.ACC_HOLDER_NAME, impsData.getBeneAccountName()))) {
						logger.info("Invalid bene Account Name for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (!(validator.validateField(CrmFieldType.ACCOUNT_NO, impsData.getBankAccountNumber()))) {
						logger.info("Invalid bene Account No for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (!(validator.validateField(CrmFieldType.IFSC_CODE, impsData.getBankIFSC()))) {
						logger.info("Invalid bene Ifsc code for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(impsData.getBankAccountName())) {
						if (impsData.getBankAccountName().length() >= 3) {
							if (!(validator.validateField(CrmFieldType.BANK_NAME, impsData.getBankAccountName()))) {
								logger.info("Invalid Bank Account Name for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
								failedData++;
								ImpsDownloadObject addData = addBulkData(data);
								payoutFailedDataList.add(addData);
								continue;
							}
						}
					}
					if (StringUtils.isNotBlank(impsData.getPhoneNo())) {
						if (!(validator.validateField(CrmFieldType.MOBILE, impsData.getPhoneNo()))) {
							logger.info("Invalid Mobile No for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
							failedData++;
							ImpsDownloadObject addData = addBulkData(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}

					if (!(validator.validateField(CrmFieldType.AMOUNT, impsData.getAmount()))) {
						logger.info("Invalid Amount for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						failedData++;
						ImpsDownloadObject addData = addBulkData(data);
						payoutFailedDataList.add(addData);
						continue;
					}

					if (StringUtils.isNotBlank(impsData.getRemarks())) {
						if (!(validator.validateField(CrmFieldType.COMMENTS, impsData.getRemarks()))) {
							logger.info("Invalid Remarks for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
							failedData++;
							ImpsDownloadObject addData = addBulkData(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}

					if (StringUtils.isNotBlank(impsData.getPurpose())) {
						if (!(validator.validateField(CrmFieldType.COMMENTS, impsData.getPurpose()))) {
							logger.info("Invalid Purpose for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
							failedData++;
							ImpsDownloadObject addData = addBulkData(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}

					
					if (merchantInitiatedDirectDao.isDuplicateOrderId(payId, subMerchantPayId, impsData.getOrderId(),
							impsData.getVirtualAccount(), impsData.getTxnType())) {
						logger.info("Duplicate OrderId found in DB for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
						duplicateData++;
						ImpsDownloadObject addDataOrderId = addBulkData(data);
						payoutDuplicateOrderIdDataList.add(addDataOrderId);
						continue;
					}

					if (StringUtils.isNoneBlank(impsData.getPurpose())) {
						PayoutPupose payoutInstance = PayoutPupose.getInstance(impsData.getPurpose());
						if (payoutInstance == null) {
							logger.info("Invalid Purpose Found for OrderId , merchantInitiatedBulkTransaction() ",data[0]);
							merchantInitiatedDirectDao.insertImpsUPIBulkDataForPurpose(impsData);
							failedData++;
							ImpsDownloadObject addData = addBulkData(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}
					
					impsDataList.add(impsData);
					merchantInitiatedDirectDao.insertImpsBulkData(impsData);
					SuccessData++;
				}

			}
			if (!payoutFailedDataList.isEmpty()) {
				String mode = "failed";
				String name = downloadBulkFailed(payoutFailedDataList, fileLocation, mode);
				setPayoutFileName(name);
				setPayoutLocation(fileLocation);
			}
			if (!payoutDuplicateOrderIdDataList.isEmpty()) {
				String mode = "orderId";
				String name = downloadBulkFailed(payoutDuplicateOrderIdDataList, fileLocation, mode);
				setOrderIdFileName(name);
				setPayoutLocation(fileLocation);
			}
			
			//Inserting File Information in DB
			merchantInitiatedDirectDao.insertBulkFileRecordInfo(newFileName,rowCount,SuccessData,failedData,duplicateData,payId,subMerchantPayId);

		} catch (Exception e) {
			logger.error("exception occured in merchantInitiatedBulkTransaction ", e);
		}
		
		return SUCCESS;
	}

	public ImpsDownloadObject addBulkData(String data[]) {
		ImpsDownloadObject payoutObject = new ImpsDownloadObject();
		BigDecimal amt = new BigDecimal((String) data[6]).setScale(2);
		if (StringUtils.isNotBlank(data[0])) {
			payoutObject.setOrderId((String) data[0]);
		}
		if (StringUtils.isNotBlank(data[1])) {
			payoutObject.setBeneAccountName((String) data[1]);
		}
		if (StringUtils.isNotBlank(data[2])) {
			payoutObject.setBankAccountNumber((String) data[2]);
		}
		if (StringUtils.isNotBlank(data[3])) {
			payoutObject.setBankIFSC((String) data[3]);
		}
		if (StringUtils.isNotBlank(data[4])) {
			payoutObject.setBankAccountName((String) data[4]);
		}
		if (StringUtils.isNotBlank(data[5])) {
			payoutObject.setPhoneNo((String) data[5]);
		}
		if (StringUtils.isNotBlank(data[6])) {
			payoutObject.setAmount(amt.toString());
		}
		if (StringUtils.isNotBlank(data[7])) {
			payoutObject.setPurpose((String) data[7]);
		}
		if (StringUtils.isNotBlank(data[8])) {
			payoutObject.setRemarks((String) data[8]);
		}
		return payoutObject;
	}

	public ImpsDownloadObject addBulkDataUPI(String data[]) {
		ImpsDownloadObject payoutObject = new ImpsDownloadObject();
		BigDecimal amt = new BigDecimal((String) data[4]).setScale(2);
		if (StringUtils.isNotBlank(data[0])) {
			payoutObject.setOrderId((String) data[0]);
		}
		if (StringUtils.isNotBlank(data[1])) {
			payoutObject.setPayerAddress((String) data[1]);
		}
		if (StringUtils.isNotBlank(data[2])) {
			payoutObject.setPayerName((String) data[2]);
		}
		if (StringUtils.isNotBlank(data[3])) {
			payoutObject.setPhoneNo((String) data[3]);
		}
		if (StringUtils.isNotBlank(data[4])) {
			payoutObject.setAmount(amt.toString());
		}
		if (StringUtils.isNotBlank(data[5])) {
			payoutObject.setPurpose((String) data[5]);
		}
		if (StringUtils.isNotBlank(data[6])) {
			payoutObject.setRemarks((String) data[6]);
		}
		return payoutObject;
	}

	public String downloadBulkFailed(List<ImpsDownloadObject> payoutFailedDataList, String fileLocation, String mode) {
		logger.info("List generated successfully for downloadBulkFailed");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = null;
		if (mode.equalsIgnoreCase("failed")) {
			sheet = wb.createSheet("Payout Failed Report");
		}
		if (mode.equalsIgnoreCase("orderId")) {
			sheet = wb.createSheet("Payout Duplicate Report");
		}

		row = sheet.createRow(0);

		row.createCell(0).setCellValue("Order ID");
		row.createCell(1).setCellValue("Bene Account Name");
		row.createCell(2).setCellValue("Bank Account Number");
		row.createCell(3).setCellValue("IFSC");
		row.createCell(4).setCellValue("Bank Name");
		row.createCell(5).setCellValue("Phone Number");
		row.createCell(6).setCellValue("Amount");
		row.createCell(7).setCellValue("Purpose");
		row.createCell(8).setCellValue("Remark");

		for (ImpsDownloadObject payoutFailedData : payoutFailedDataList) {
			row = sheet.createRow(rownum++);
			Object[] objArr = payoutFailedData.csvForPayoutFailed();

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
			if (mode.equalsIgnoreCase("failed")) {
				filename = "Payout_Failed_IMPS_Report" + df.format(new Date()) + FILE_EXTENSION;
			}
			if (mode.equalsIgnoreCase("orderId")) {
				filename = "Payout_Duplicate_IMPS_Report" + df.format(new Date()) + FILE_EXTENSION;
			}
			try {
				Files.createDirectories(Paths.get(fileLocation));
			} catch (IOException e1) {
				logger.error("Error in creating Directorie ", e1);
			}
			File file = new File(fileLocation + filename);
			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for Failed Payout");
			return filename;
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return filename;
	}

	public String downloadBulkFailedUpi(List<ImpsDownloadObject> payoutFailedDataList, String fileLocation,
			String mode) {
		logger.info("List generated successfully for downloadBulkFailed");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = null;
		if (mode.equalsIgnoreCase("failed")) {
			sheet = wb.createSheet("Payout Failed Report");
		}
		if (mode.equalsIgnoreCase("orderId")) {
			sheet = wb.createSheet("Payout Duplicate Report");
		}
		row = sheet.createRow(0);

		row.createCell(0).setCellValue("Order ID");
		row.createCell(1).setCellValue("Payer Address");
		row.createCell(2).setCellValue("Payer Name");
		row.createCell(3).setCellValue("Phone Number");
		row.createCell(4).setCellValue("Amount");
		row.createCell(5).setCellValue("Purpose");
		row.createCell(6).setCellValue("Remark");

		for (ImpsDownloadObject payoutFailedData : payoutFailedDataList) {
			row = sheet.createRow(rownum++);
			Object[] objArr = payoutFailedData.csvForPayoutFailedUpi();

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
			if (mode.equalsIgnoreCase("failed")) {
				filename = "Payout_Failed_UPI_Report" + df.format(new Date()) + FILE_EXTENSION;
			}
			if (mode.equalsIgnoreCase("orderId")) {
				filename = "Payout_Duplicate_UPI_Report" + df.format(new Date()) + FILE_EXTENSION;
			}
			try {
				Files.createDirectories(Paths.get(fileLocation));
			} catch (IOException e1) {
				logger.error("Error in creating Directorie ", e1);
			}
			File file = new File(fileLocation + filename);
			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for Failed Payout");
			return filename;
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return filename;
	}

	public String downloadFailedPayoutData() {
		String loc = payoutLocation;
		String name = payoutFileName;

		try {
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = payoutFileName;
			File file = new File(loc + name);
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for Payout Failed UPI and IMPS Report");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public String merchantInitiatedBulkUPITransaction() {

		logger.info("Inside MerchantInitiatedDirectAction, merchantInitiatedBulkUPITransaction()");
		currencyMap = new HashMap<String, String>();
		merchantList = new ArrayList<Merchants>();
		impsDataList = new ArrayList<ImpsDownloadObject>();
		failedListShow = new HashMap<Long, String>();
		List<ImpsDownloadObject> payoutFailedDataList = new ArrayList<ImpsDownloadObject>();
		List<ImpsDownloadObject> payoutDuplicateOrderIdDataList = new ArrayList<ImpsDownloadObject>();
		String fileLocation = PropertiesManager.propertiesMap.get(Constants.PAYOUT_FILE_LOCATION.getValue());

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
				if (StringUtils.isNotBlank(subMerchantPayId)) {
					businessName = userDao.getBusinessNameByPayId(payId);
					subMerchantBusinessName = userDao.getBusinessNameByPayId(subMerchantPayId);
				} else {
					businessName = userDao.getBusinessNameByPayId(payId);
				}

				currencyMap = Currency.getSupportedCurreny(userDao.findPayId(payId));
				setCurrencyMap(currencyMap);
				setMerchantList(userDao.getMerchantActiveList());
			} else if (user.getUserType().equals(UserType.MERCHANT)) {
				setMerchantList(userDao.getMerchantActive(user.getEmailId()));

				if (user.isSuperMerchant()) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(user.getPayId()));
					businessName = user.getBusinessName();
					payId = user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(user.getSuperMerchantId());
					payId = user.getSuperMerchantId();
					subMerchantPayId = user.getPayId();
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
					payId = user.getPayId();
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
						if (data[0].equalsIgnoreCase("Order ID") && data[1].equalsIgnoreCase("Payer Address")
								&& data[2].equalsIgnoreCase("Payer Name") && data[3].equalsIgnoreCase("Phone Number")
								&& data[4].equalsIgnoreCase("Amount") && data[5].equalsIgnoreCase("Purpose")
								&& data[6].equalsIgnoreCase("Remark")) {
							setWrongCsv(0);
							continue;
						} else {
							setWrongCsv(1);
							setResponse("failed");
							setResponseMsg("Invalid Field Format");
							break;
						}
					}

					/*
					 * if (data.length > 4 || data.length < 4) {
					 * 
					 * failedData++; continue; }
					 */

					// Checking mandatory Field is not Empty
					/*
					 * if (StringUtils.isBlank(data[0])) { failedData++;
					 * continue; }
					 */

					BigDecimal amt = new BigDecimal((String) data[4]).setScale(2);
					User userByPayId = null;
					if (StringUtils.isNotBlank(subMerchantPayId)) {
						userByPayId = userDao.findPayId(subMerchantPayId);
						impsData.setMerchantPayId(userByPayId.getSuperMerchantId());
						impsData.setSubMerchant(subMerchantPayId);
						impsData.setVirtualAccount(userByPayId.getVirtualAccountNo());
					} else {
						userByPayId = userDao.findPayId(payId);
						impsData.setMerchantPayId(payId);
						impsData.setVirtualAccount(userByPayId.getVirtualAccountNo());
					}

					if (StringUtils.isNotBlank(data[0])) {
						impsData.setOrderId((String) data[0]);
					} else {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[1])) {
						impsData.setPayerAddress((String) data[1]);
					} else {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[2])) {
						impsData.setPayerName((String) data[2]);
					} else {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[3])) {
						impsData.setPhoneNo((String) data[3]);
					} else {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[4])) {
						impsData.setAmount(amt.toString());
					} else {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[5])) {
						impsData.setPurpose((String) data[5]);
					} else {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(data[6])) {
						impsData.setRemarks((String) data[6]);
					}

					impsData.setCurrencyCode("356");
					impsData.setTxnType("IMPS");

					if (!(validator.validateField(CrmFieldType.ORDER_ID, impsData.getOrderId()))) {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (!(validator.validateField(CrmFieldType.PAYER_ADDRESS, impsData.getPayerAddress()))) {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (!(validator.validateField(CrmFieldType.PAYER_NAME, impsData.getPayerName()))) {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}

					if (StringUtils.isNotBlank(impsData.getPhoneNo())) {
						if (!(validator.validateField(CrmFieldType.MOBILE, impsData.getPhoneNo()))) {
							failedData++;
							ImpsDownloadObject addData = addBulkDataUPI(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}

					if (!(validator.validateField(CrmFieldType.AMOUNT, impsData.getAmount()))) {
						failedData++;
						ImpsDownloadObject addData = addBulkDataUPI(data);
						payoutFailedDataList.add(addData);
						continue;
					}
					if (StringUtils.isNotBlank(impsData.getPurpose())) {
						if (!(validator.validateField(CrmFieldType.COMMENTS, impsData.getPurpose()))) {
							failedData++;
							ImpsDownloadObject addData = addBulkDataUPI(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}
					if (StringUtils.isNotBlank(impsData.getRemarks())) {
						if (!(validator.validateField(CrmFieldType.COMMENTS, impsData.getRemarks()))) {
							failedData++;
							ImpsDownloadObject addData = addBulkDataUPI(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}

					impsData.setTxnType("UPI");
					if (merchantInitiatedDirectDao.isDuplicateOrderId(payId, subMerchantPayId, impsData.getOrderId(),
							impsData.getVirtualAccount(), impsData.getTxnType())) {
						duplicateData++;
						ImpsDownloadObject addDataOrderId = addBulkDataUPI(data);
						payoutDuplicateOrderIdDataList.add(addDataOrderId);
						continue;
					}

					if (StringUtils.isNoneBlank(impsData.getPurpose())) {
						PayoutPupose payoutInstance = PayoutPupose.getInstance(impsData.getPurpose());
						if (payoutInstance == null) {
							merchantInitiatedDirectDao.insertImpsUPIBulkDataForPurpose(impsData);
							failedData++;
							ImpsDownloadObject addData = addBulkDataUPI(data);
							payoutFailedDataList.add(addData);
							continue;
						}
					}

					impsDataList.add(impsData);
					merchantInitiatedDirectDao.insertUPIBulkData(impsData);
					SuccessData++;
				}
			}
			if (!payoutFailedDataList.isEmpty()) {
				String mode = "failed";
				String name = downloadBulkFailedUpi(payoutFailedDataList, fileLocation, mode);
				setPayoutFileName(name);
				setPayoutLocation(fileLocation);
			}
			if (!payoutDuplicateOrderIdDataList.isEmpty()) {
				String mode = "orderId";
				String name = downloadBulkFailedUpi(payoutDuplicateOrderIdDataList, fileLocation, mode);
				setOrderIdFileName(name);
				setPayoutLocation(fileLocation);
			}

		} catch (Exception e) {
			logger.error("exception occured in merchantInitiatedBulkUPITransaction ", e);
		}
		setTxnType("UPI");
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
			logger.error("exception ", e);
			return csvData;
		} finally {
			if (br != null)
				br.close();
		}

	}

	public String getUserOfMIDPayId() {

		logger.info("Inside MerchantInitiatedDirectAction, getUserOfMIDPayId()");

		try {
			User user = null;
			if (StringUtils.isNotBlank(subMerchantPayId)) {
				if (StringUtils.isNotEmpty(subMerchantPayId) && subMerchantPayId.contains("@")) {
					user = userDao.findByEmailId(subMerchantPayId);
				} else {
					user = userDao.findPayId(subMerchantPayId);
				}
			} else {
				if (!StringUtils.equalsIgnoreCase(payId, "All")) {
					if (StringUtils.isNotEmpty(payId) && payId.contains("@")) {
						user = userDao.findByEmailId(payId);
					} else {
						user = userDao.findPayId(payId);
					}
				}

			}
			
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());

			if (merchantSettings.isMerchantInitiatedDirectFlag()) {
				setSubMerchantFlag(true);
				setResponse("success");
				setResponseMsg("Success");
				return SUCCESS;
			} else {
				setSubMerchantFlag(false);
				setResponse("failed");
				setResponseMsg("Permission Failed");
				return SUCCESS;
			}
		} catch (Exception e) {
			logger.error("exception ", e);
		}
		return SUCCESS;

	}

	// Report Download
	public String merchantInitiatedDirectReportDownload() {

		logger.info("Inside MerchantInitiatedDirectAction, merchantInitiatedDirectReportDownload()");

		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside merchantInitiatedDirectReportDownload()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ImpsDownloadObject> merchantInitiatedDirectList = new ArrayList<ImpsDownloadObject>();
		String payIdReport = "";
		String subMerchantPayIdReport = "";

		User userByPayId = null;

		if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
			User user = null;
			if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
				user = userDao.findPayId(sessionUser.getParentPayId());
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					payIdReport = user.getSuperMerchantId();
					subMerchantPayIdReport = user.getPayId();
				} else {

					if (user.isSuperMerchant() == true && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						if (StringUtils.isNotBlank(subMerchantPayId)) {
							payIdReport = payId;
							subMerchantPayIdReport = subMerchantPayId;
						} else {
							payIdReport = payId;
							subMerchantPayIdReport = "ALL";
						}
					} else {
						payIdReport = payId;
					}
				}
			}
		} else if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			payIdReport = sessionUser.getSuperMerchantId();
			subMerchantPayIdReport = sessionUser.getPayId();
		} else if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			userByPayId = userDao.findPayId(subMerchantPayId);
			payIdReport = userByPayId.getSuperMerchantId();
			subMerchantPayIdReport = subMerchantPayId;

		} else {
			payIdReport = payId;
			subMerchantPayIdReport = subMerchantPayId;
		}

		merchantInitiatedDirectList = merchantInitiatedDirectDao.merchantInitiatedDirectReportDataDownload(dateFrom,
				dateTo, payIdReport, subMerchantPayIdReport, status, orderId, beneAccountNumber, payerAddress, rrn,
				txnType, sessionUser, finalStatus);
		BigDecimal st = null;

		logger.info("List generated successfully for merchantInitiatedDirectReportDownload");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Payout Report");
		row = sheet.createRow(0);

		if (StringUtils.isNotBlank(subMerchantPayIdReport) || ((payIdReport.equalsIgnoreCase("ALL")))) {
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub Merchant Name");
			row.createCell(2).setCellValue("Transaction ID");
			row.createCell(3).setCellValue("Txn Reference");
			row.createCell(4).setCellValue("Pay ID");
			row.createCell(5).setCellValue("Order ID");
			row.createCell(6).setCellValue("Transaction Date");
			row.createCell(7).setCellValue("Mobile No");
			row.createCell(8).setCellValue("Channel");
			row.createCell(9).setCellValue("RRN");
			row.createCell(10).setCellValue("Bene Name/Payee Name");
			row.createCell(11).setCellValue("Bank Name/VPA");
			row.createCell(12).setCellValue("Bank Account Number");
			row.createCell(13).setCellValue("Bank IFSC");
			row.createCell(14).setCellValue("Amount");
			row.createCell(15).setCellValue("Mode");
			row.createCell(16).setCellValue("Response Message");
			row.createCell(17).setCellValue("Status");
			row.createCell(18).setCellValue("Purpose");

			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				// transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadMerchantInitiateDirectSub();

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
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Transaction ID");
			row.createCell(2).setCellValue("Txn Reference");
			row.createCell(3).setCellValue("Pay ID");
			row.createCell(4).setCellValue("Order ID");
			row.createCell(5).setCellValue("Transaction Date");
			row.createCell(6).setCellValue("Mobile No");
			row.createCell(7).setCellValue("Channel");
			row.createCell(8).setCellValue("RRN");
			row.createCell(9).setCellValue("Bene Name/Payee Name");
			row.createCell(10).setCellValue("Bank Name/VPA");
			row.createCell(11).setCellValue("Bank Account Number");
			row.createCell(12).setCellValue("Bank IFSC");
			row.createCell(13).setCellValue("Amount");
			row.createCell(14).setCellValue("Mode");
			row.createCell(15).setCellValue("Response Message");
			row.createCell(16).setCellValue("Status");
			row.createCell(17).setCellValue("Purpose");

			for (ImpsDownloadObject payoutSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				// transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = payoutSearch.myCsvMethodDownloadMerchantInitiateDirectMerch();

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
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Payout_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for merchantInitiatedDirectReportDownload");
		} catch (Exception exception) {
			logger.error(
					"Exception occured in MerchantInitiatedDirectAction merchantInitiatedDirectReportDownload(), Exception = ",
					exception);
		}

		return SUCCESS;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getBeneficiaryAccountName() {
		return beneficiaryAccountName;
	}

	public void setBeneficiaryAccountName(String beneficiaryAccountName) {
		this.beneficiaryAccountName = beneficiaryAccountName;
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}

	public String getBankIFSCCode() {
		return bankIFSCCode;
	}

	public void setBankIFSCCode(String bankIFSCCode) {
		this.bankIFSCCode = bankIFSCCode;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public List<ImpsDownloadObject> getImpsDataList() {
		return impsDataList;
	}

	public void setImpsDataList(List<ImpsDownloadObject> impsDataList) {
		this.impsDataList = impsDataList;
	}

	public Map<Long, String> getFailedListShow() {
		return failedListShow;
	}

	public void setFailedListShow(Map<Long, String> failedListShow) {
		this.failedListShow = failedListShow;
	}

	public List<String> getFileData() {
		return fileData;
	}

	public void setFileData(List<String> fileData) {
		this.fileData = fileData;
	}

	public int getWrongCsv() {
		return wrongCsv;
	}

	public void setWrongCsv(int wrongCsv) {
		this.wrongCsv = wrongCsv;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
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

	public int getFileIsEmpty() {
		return fileIsEmpty;
	}

	public void setFileIsEmpty(int fileIsEmpty) {
		this.fileIsEmpty = fileIsEmpty;
	}

	public File getCsvFile() {
		return csvFile;
	}

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getBusinessName() {
		return businessName;
	}

	public String getSubMerchantBusinessName() {
		return subMerchantBusinessName;
	}

	public void setSubMerchantBusinessName(String subMerchantBusinessName) {
		this.subMerchantBusinessName = subMerchantBusinessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getVpa() {
		return vpa;
	}

	public void setVpa(String vpa) {
		this.vpa = vpa;
	}

	public String getPayeeName() {
		return payeeName;
	}

	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}

	public long getDuplicateData() {
		return duplicateData;
	}

	public void setDuplicateData(long duplicateData) {
		this.duplicateData = duplicateData;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public List<ImpsDownloadObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ImpsDownloadObject> aaData) {
		this.aaData = aaData;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getBeneAccountNumber() {
		return beneAccountNumber;
	}

	public void setBeneAccountNumber(String beneAccountNumber) {
		this.beneAccountNumber = beneAccountNumber;
	}

	public boolean isSubMerchantFlag() {
		return subMerchantFlag;
	}

	public void setSubMerchantFlag(boolean subMerchantFlag) {
		this.subMerchantFlag = subMerchantFlag;
	}

	public String getPayerAddress() {
		return payerAddress;
	}

	public void setPayerAddress(String payerAddress) {
		this.payerAddress = payerAddress;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getPayoutLocation() {
		return payoutLocation;
	}

	public void setPayoutLocation(String payoutLocation) {
		this.payoutLocation = payoutLocation;
	}

	public String getPayoutFileName() {
		return payoutFileName;
	}

	public void setPayoutFileName(String payoutFileName) {
		this.payoutFileName = payoutFileName;
	}

	public String getOrderIdFileName() {
		return orderIdFileName;
	}

	public void setOrderIdFileName(String orderIdFileName) {
		this.orderIdFileName = orderIdFileName;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}
	

}
