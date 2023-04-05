package com.paymentgateway.crm.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.ImpsDao;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/**
 * @ Shiva
 */
public class ImpsDataAction extends AbstractSecureAction {

	private static final long serialVersionUID = 8502651169499776195L;
	private static Logger logger = LoggerFactory.getLogger(ImpsDataAction.class.getName());

	@Autowired
	private CrmValidator validator;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ImpsDao impsDao;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	private String payId;
	private String subMerchant;
	private String bankAccountName;
	private String bankAccountNumber;
	private String bankIfsc;
	private String amount;
	private String settledDate;
	private String capturedDateFrom;
	private String capturedDateTo;
	private String response;
	private String responseMsg;
	private String status;
	private String dateFrom;
	private String dateTo;
	private String txnId;
	private String mobileNo;
	private String userType;
	private String orderId;
	private List<ImpsDownloadObject> aaData;
	private User sessionUser = new User();
	public boolean flag = false;
	private String phoneNo;
	private String channel;

	public String execute() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmssSSS");
		try {
			
			String autoOrderId="LP"+sdf.format(new Date());
			if (StringUtils.isNotBlank(amount)) {
				requestMap.put(FieldType.ORDER_ID.getName(),autoOrderId);
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
				requestMap.put(FieldType.BENE_NAME.getName(), bankAccountName);
				requestMap.put(FieldType.IFSC_CODE.getName(), bankIfsc);
				requestMap.put(FieldType.AMOUNT.getName(), amount);
				requestMap.put(FieldType.PAY_ID.getName(), payId);
				requestMap.put(FieldType.CAPTURED_DATE_FROM.getName(), capturedDateFrom);
				requestMap.put(FieldType.CAPTURED_DATE_TO.getName(), capturedDateTo);
				requestMap.put(FieldType.SETTLED_DATE.getName(), settledDate);
				requestMap.put(FieldType.PHONE_NO.getName(), phoneNo);
				requestMap.put(FieldType.USER_TYPE.getName(), "PG Initiated");
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");

//				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
//				String salt = PropertiesManager.saltStore.get(adminPayId);
//				if (StringUtils.isBlank(salt)) {
//					salt = (new PropertiesManager()).getSalt(adminPayId);
//					if (salt != null) {
//						logger.info("Salt found from propertiesManager for payId ");
//					}
//
//				} else {
//					logger.info("Salt found from static map in propertiesManager");
//				}
				
/*				String hashString = bankAccountNumber + bankAccountName + bankIfsc + amount + payId + capturedDateFrom
						+ capturedDateTo + settledDate + phoneNo +"PG Initiated"+autoOrderId+ salt;*/
				String hash = Hasher.getHash(new Fields(requestMap));
				requestMap.put(FieldType.HASH.getName(), hash);
				respMap = transactionControllerServiceProvider.impsTransferTransact(requestMap);
				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				}

			} else {
				setResponse("failed");
				setResponseMsg("Amount is Empty");
			}
		} catch (SystemException e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}

	@SkipValidation
	public String fetchMerchantAccountDetails() {
		logger.info("inside the fetchMerchantAccountDetails()");
		if (StringUtils.isNotBlank(payId)) {

			User user = userDao.findPayId(payId);

			
				logger.info("getting Bank account details from MPA Table");
				MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
				if (mpaData != null) {
					setBankAccountName(mpaData.getAccountHolderName());
					setBankAccountNumber(mpaData.getAccountNumber());
					setBankIfsc(mpaData.getAccountIfsc());
				
			}

		}

		return SUCCESS;

	}

	@SuppressWarnings("unlikely-arg-type")
	@SkipValidation
	public String fetchReportData() {
		logger.info("Inside fetchReportData()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String merchantPayId = "";
		String subMerchantPayId = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		} else {
			merchantPayId =  payId;
			
			if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId = subMerchant;
			}
			if(StringUtils.isNotBlank(subMerchant) && subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId  = subMerchant;
			}
		}
		
	
		if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {
			setAaData(impsDao.fetchImpsReportData(dateFrom, dateTo, merchantPayId,subMerchantPayId , status, channel, sessionUser));
			
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				flag = true;
				setFlag(flag);
			} else if(sessionUser.isSuperMerchant() == true ) {
				flag = true;
				setFlag(flag);
			} else {
				flag = false;
				setFlag(flag);
			}
		}

		return SUCCESS;

	}

	@SkipValidation
	public String fetchTxnStatus() {
		logger.info("Inside fetchTxnStatus()");
		Map<String, String> respMap = new HashMap<String,String>();
		if (StringUtils.isNotBlank(txnId)) {
			try {
				
				respMap.put(FieldType.TXN_ID.getName(), txnId);
				respMap.put(FieldType.PAY_ID.getName(), payId);
				respMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount,"356"));
				respMap.put(FieldType.TXNTYPE.getName(), "IMPS");
				respMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(respMap)));
				
				respMap = transactionControllerServiceProvider.MerchantDirectInitiateStatusEnq(respMap);

				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				}
			} catch (SystemException e) {
				logger.error("exception " , e);
				setResponse("failed");
				setResponseMsg("Failed Due To System Error");
			}
		}

		return SUCCESS;

	}
	
	@SkipValidation
	public String reinitiateTransaction() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		try {
			
			ImpsDownloadObject impsData=impsDao.getImpsTransactionWithTxnId(txnId);
			if(impsData!=null){
			
				boolean duplicateOrderIdFlag=impsDao.checkImpsDuplicateOrderId(impsData.getOrderId());
			
			if (!duplicateOrderIdFlag) {

				requestMap.put(FieldType.ORDER_ID.getName(), impsData.getOrderId());
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), impsData.getBankAccountNumber());
				requestMap.put(FieldType.BENE_NAME.getName(), impsData.getBankAccountName());
				requestMap.put(FieldType.IFSC_CODE.getName(), impsData.getBankIFSC());
				requestMap.put(FieldType.AMOUNT.getName(), impsData.getAmount());
				requestMap.put(FieldType.PAY_ID.getName(), impsData.getMerchantPayId());
				requestMap.put(FieldType.CAPTURED_DATE_FROM.getName(), impsData.getTxnsCapturedFrom());
				requestMap.put(FieldType.CAPTURED_DATE_TO.getName(), impsData.getTxnsCapturedTo());
				requestMap.put(FieldType.SETTLED_DATE.getName(), impsData.getSystemSettlementDate());
				requestMap.put(FieldType.PHONE_NO.getName(), impsData.getPhoneNo());
				requestMap.put(FieldType.USER_TYPE.getName(), impsData.getUserType());
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
				/*String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
				String salt = PropertiesManager.saltStore.get(adminPayId);
				if (StringUtils.isBlank(salt)) {
					salt = (new PropertiesManager()).getSalt(adminPayId);
					if (salt != null) {
						logger.info("Salt found from propertiesManager for payId ");
					}

				} else {
					logger.info("Salt found from static map in propertiesManager");
				}
				
				String hashString = impsData.getBankAccountNumber() +  impsData.getBankAccountName() + impsData.getBankIFSC() + impsData.getAmount() + impsData.getMerchantPayId() + impsData.getTxnsCapturedFrom()
						+ impsData.getTxnsCapturedTo() + impsData.getSystemSettlementDate() + impsData.getPhoneNo() +impsData.getUserType()+impsData.getOrderId()+ salt;*/
				String hash = Hasher.getHash(new Fields(requestMap));
				requestMap.put(FieldType.HASH.getName(), hash);
				respMap = transactionControllerServiceProvider.impsTransferTransact(requestMap);
				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				}

			} else {
				setResponse("failed");
				setResponseMsg("Order Id already Exists");
			}
			}
		} catch (SystemException e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}
	
	@SkipValidation
	public String merchantReInitiatedDirect() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		try {
			
			ImpsDownloadObject impsData=impsDao.getImpsTransactionWithTxnId(txnId);
			 
			boolean dailyLimit = impsDao.isDailyLimitExceed(impsData);
			if(impsData!=null && !dailyLimit){
			
				requestMap.put(FieldType.ORDER_ID.getName(), impsData.getOrderId());
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), impsData.getBankAccountNumber());
				requestMap.put(FieldType.BENE_NAME.getName(), impsData.getBankAccountName());
				requestMap.put(FieldType.IFSC_CODE.getName(), impsData.getBankIFSC());
				requestMap.put(FieldType.AMOUNT.getName(), impsData.getAmount());
				requestMap.put(FieldType.PAY_ID.getName(), impsData.getMerchantPayId());
				requestMap.put(FieldType.PHONE_NO.getName(), impsData.getPhoneNo());
				requestMap.put(FieldType.USER_TYPE.getName(), impsData.getUserType());
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
//				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
//				String salt = PropertiesManager.saltStore.get(adminPayId);
//				if (StringUtils.isBlank(salt)) {
//					salt = (new PropertiesManager()).getSalt(adminPayId);
//					if (salt != null) {
//						logger.info("Salt found from propertiesManager for payId ");
//					}
//
//				} else {
//					logger.info("Salt found from static map in propertiesManager");
//				}
//				
//				String hashString = impsData.getBankAccountNumber() +  impsData.getBankAccountName() + impsData.getBankIFSC() + impsData.getAmount() + impsData.getMerchantPayId()
//						+ impsData.getPhoneNo() +impsData.getUserType()+impsData.getOrderId()+ salt;
				String hash = Hasher.getHash(new Fields(requestMap));
				requestMap.put(FieldType.HASH.getName(), hash);
				respMap = transactionControllerServiceProvider.impsTransferTransact(requestMap);
				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				}

			}else {
				if(dailyLimit) {
					setResponse("failed");
					setResponseMsg("Daily Limit Excced");
				}
				
			}
			logger.info("Inside IMPS Action");
			logger.info("response mg "+ getResponseMsg());
		} catch (SystemException e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}


	public void validate() {


		if ((validator.validateBlankField(settledDate))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.DATE_FROM, settledDate))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(capturedDateFrom))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.DATE_FROM, capturedDateFrom))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(capturedDateTo))) {
			addFieldError(CrmFieldType.DATE_TO.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.DATE_TO, capturedDateTo))) {
			addFieldError(CrmFieldType.DATE_TO.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(txnId))) {
		} else if (!(validator.validateField(CrmFieldType.TRANSACTION_ID, txnId))) {
			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
	
	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
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

	public String getSettledDate() {
		return settledDate;
	}

	public void setSettledDate(String settledDate) {
		this.settledDate = settledDate;
	}

	public String getCapturedDateFrom() {
		return capturedDateFrom;
	}

	public void setCapturedDateFrom(String capturedDateFrom) {
		this.capturedDateFrom = capturedDateFrom;
	}

	public String getCapturedDateTo() {
		return capturedDateTo;
	}

	public void setCapturedDateTo(String capturedDateTo) {
		this.capturedDateTo = capturedDateTo;
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

	public List<ImpsDownloadObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ImpsDownloadObject> aaData) {
		this.aaData = aaData;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
