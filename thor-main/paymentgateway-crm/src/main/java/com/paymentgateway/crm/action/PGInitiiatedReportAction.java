package com.paymentgateway.crm.action;

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
import com.paymentgateway.commons.dao.PGPayoutDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

public class PGInitiiatedReportAction extends AbstractSecureAction {
	
	@Autowired
	private PGPayoutDao vendorPayOutDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	private static final long serialVersionUID = 8502651169499776195L;
	private static Logger logger = LoggerFactory.getLogger(PGInitiiatedReportAction.class.getName());	

	private String payId;
	private String subMerchantPayId;
	private String orderId;
	private String status;
	private String dateFrom;
	private String dateTo;
	private String txnId;
	private List<ImpsDownloadObject> aaData;
	private User sessionUser = new User();
	public boolean flag = false;
	private String response;
	private String responseMsg;
	private String data;
	
	
	public String execute() {
		
		logger.info("Inside fetchReportData()");
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String merchantPayId = "";
			String subMerchantPayIdd = "";
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayIdd = sessionUser.getPayId();
			} else {
				merchantPayId =  payId;
				
				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
					subMerchantPayIdd = subMerchantPayId;
				}
				if(StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All")) {
					subMerchantPayIdd  = subMerchantPayId;
				}
			}
			
			if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dateTo)) {
				setAaData(vendorPayOutDao.fetchVendorPayOutReportData(merchantPayId, subMerchantPayIdd, orderId, status, getDateFrom(), getDateTo(),sessionUser));
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
			
		}catch(Exception e) {
			logger.error("exception " , e);
		}
		return SUCCESS;
	}
	
	@SkipValidation
	public String reInitiateTransaction(){
			Map<String, String> requestMap = new HashMap<>();
			Map<String, String> respMap;
			try {
				
				ImpsDownloadObject vendorData=vendorPayOutDao.getVendorTransactionWithTxnId(txnId);
				if(vendorData!=null){
				
					boolean duplicateOrderIdFlag=vendorPayOutDao.checkImpsDuplicateOrderId(vendorData.getOrderId());
				
				if (!duplicateOrderIdFlag) {

					requestMap.put(FieldType.ORDER_ID.getName(), vendorData.getOrderId());
					requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), vendorData.getBankAccountNumber());
					requestMap.put(FieldType.BENE_NAME.getName(), vendorData.getBankAccountName());
					requestMap.put(FieldType.IFSC_CODE.getName(), vendorData.getBankIFSC());
					requestMap.put(FieldType.AMOUNT.getName(), vendorData.getAmount());  
                    requestMap.put(FieldType.PAY_ID.getName(), vendorData.getMerchantPayId());
					requestMap.put(FieldType.PHONE_NO.getName(), vendorData.getPhoneNo());
					requestMap.put(FieldType.USER_TYPE.getName(), vendorData.getUserType());
					requestMap.put(FieldType.SETTLED_DATE.getName(), vendorData.getSystemSettlementDate());
					requestMap.put(FieldType.CAPTURED_DATE_FROM.getName(), vendorData.getTxnsCapturedFrom());
					requestMap.put(FieldType.CAPTURED_DATE_TO.getName(), vendorData.getTxnsCapturedTo());					
					
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
					
					String hashString = vendorData.getBankAccountNumber() +  vendorData.getBankAccountName() + vendorData.getBankIFSC() + vendorData.getAmount() + vendorData.getMerchantPayId() + vendorData.getTxnsCapturedFrom()
							+ vendorData.getTxnsCapturedTo() + vendorData.getSystemSettlementDate() + vendorData.getPhoneNo() +vendorData.getUserType()+vendorData.getOrderId()+ salt;
					String hash = Hasher.getHash(hashString);
					requestMap.put(FieldType.HASH.getName(), hash);
					respMap = transactionControllerServiceProvider.impsTransferTransact(requestMap);
					
					if(!requestMap.isEmpty()){
						vendorPayOutDao.insertResonseFieldsInDB(respMap);
					}
					
					if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
						setResponse("success");
						setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
					} else {
						setResponse("failed");
						setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
					}
					
					}
				}
			}catch (SystemException e) {
				logger.error("exception " , e);
				setResponse("failed");
				setResponseMsg("Failed Due To System Error");
			}
		
		return SUCCESS;
	}
	
	/*
	 * @SkipValidation public String fetchTxnStatus() {
	 * logger.info("Inside fetchTxnStatus()"); Map<String, String> respMap; if
	 * (StringUtils.isNotBlank(txnId)) { try { respMap =
	 * transactionControllerServiceProvider.impsStatusTransact(txnId);
	 * 
	 * if(!respMap.isEmpty()) {
	 * vendorPayOutDao.updateIMPSTransactionStatus(respMap); }
	 * 
	 * if
	 * (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED
	 * .getName())) { setResponse("success");
	 * setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName())); } else {
	 * setResponse("failed");
	 * setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName())); }
	 * 
	 * } catch (SystemException e) { logger.error("exception " + e);
	 * setResponse("failed"); setResponseMsg("Failed Due To System Error"); } }
	 * 
	 * return SUCCESS;
	 * 
	 * }
	 */
	
	@SkipValidation
	public String initiateTransaction(){
		logger.info("Inside initiateTransaction()");
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		String txnArr[] = null;
		
		if (StringUtils.isNotBlank(txnId)) {
			try {
			txnArr = txnId.split(",");
			for(String txn : txnArr ) {
				
					
					ImpsDownloadObject vendorData=vendorPayOutDao.getVendorTransactionWithTxnId(txn);
					if(vendorData!=null){
					
						boolean duplicateOrderIdFlag=vendorPayOutDao.checkImpsDuplicateOrderId(vendorData.getOrderId());
					
					if (!duplicateOrderIdFlag) {

						requestMap.put(FieldType.ORDER_ID.getName(), vendorData.getOrderId());
						requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), vendorData.getBankAccountNumber());
						requestMap.put(FieldType.BENE_NAME.getName(), vendorData.getBankAccountName());
						requestMap.put(FieldType.IFSC_CODE.getName(), vendorData.getBankIFSC());
						requestMap.put(FieldType.AMOUNT.getName(), vendorData.getAmount());  
	                    requestMap.put(FieldType.PAY_ID.getName(), vendorData.getMerchantPayId());
						requestMap.put(FieldType.PHONE_NO.getName(), vendorData.getPhoneNo());
						requestMap.put(FieldType.USER_TYPE.getName(), vendorData.getUserType());
						requestMap.put(FieldType.SETTLED_DATE.getName(), vendorData.getSystemSettlementDate());
						requestMap.put(FieldType.CAPTURED_DATE_FROM.getName(), vendorData.getTxnsCapturedFrom());
						requestMap.put(FieldType.CAPTURED_DATE_TO.getName(), vendorData.getTxnsCapturedTo());					
						
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
						
						String hashString = vendorData.getBankAccountNumber() +  vendorData.getBankAccountName() + vendorData.getBankIFSC() + vendorData.getAmount() + vendorData.getMerchantPayId() + vendorData.getTxnsCapturedFrom()
								+ vendorData.getTxnsCapturedTo() + vendorData.getSystemSettlementDate() + vendorData.getPhoneNo() +vendorData.getUserType()+vendorData.getOrderId()+ salt;
						String hash = Hasher.getHash(hashString);
						requestMap.put(FieldType.HASH.getName(), hash);
						respMap = transactionControllerServiceProvider.impsTransferTransact(requestMap);
						
						if(!requestMap.isEmpty()){
							vendorPayOutDao.insertResonseFieldsInDB(respMap);
						}
						
						}
					}
				
			}
			setResponse("success");
			setResponseMsg("Updated Transaction");
			
			}catch (SystemException e) {
				logger.error("exception " , e);
				setResponse("failed");
				setResponseMsg("Failed Due To System Error");
			}
		}
		
		return SUCCESS;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}


	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}


	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}
	
	public List<ImpsDownloadObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ImpsDownloadObject> aaData) {
		this.aaData = aaData;
	}
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
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
	
}
