package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.crm.actionBeans.PaymentOptionsFactory;

/**
 * @author Amitosh
 *
 */
public class PaymentOptionsAction extends AbstractSecureAction {

	@Autowired
	PaymentOptionsFactory paymentOptionsFactory;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private DataEncoder dataEncoder;

	@Autowired
	private UserDao userDao;

	private String payId;
	private String subMerchantId;
	private String creditCard;
	private String debitCard;
	private String netBanking;
	private String wallet;
	private String emi;
	private String recurringPayment;
	private String expressPay;
	private String upi;
	private String upiQr;
	private String mqr;
	private String prepaidCard;
	private String debitCardWithPin;
	private String cashOnDelivery;
	private String aamarPay;
	private String international;
	private String loginUserEmailId;
	private String merchantName;
	private String paymentOptionList;
	private String activationFlag;
	private String mopTypeString;
	private String paymentType;
	private String crypto;
	
	private String operation;
	private String responseStatus;


	public List<PaymentType> paymentList = new ArrayList<PaymentType>();
	public Map<String, Object> mopList = new LinkedHashMap<String, Object>();
	public Map<String, String> mopListCC = new HashMap<String, String>();
	public Map<String, String> mopListDC = new HashMap<String, String>();
	public Map<String, String> mopListNB = new HashMap<String, String>();
	public Map<String, String> mopListWL = new HashMap<String, String>();
	public Map<String, String> mopListUPI = new HashMap<String, String>();
	public Map<String, String> mopListCR = new HashMap<String, String>();
	public Map<String, String> mopListMQR = new HashMap<String, String>();
//	public List<MopType> mopListPC = new ArrayList<MopType>();
//	public List<MopType> mopListEMI = new ArrayList<MopType>();

	public Map<String, String> transList = new HashMap<String, String>();
	// public List<TransactionType> transList = new
	// ArrayList<TransactionType>();

	private User sessionUser = new User();
	private List<PaymentOptions> aaData = new ArrayList<PaymentOptions>();
	private String response;

	private static final long serialVersionUID = 8075688585953477261L;
	private static Logger logger = LoggerFactory.getLogger(PaymentOptionsAction.class.getName());

	public String execute() {
		try {
			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (StringUtils.isNotBlank(mopTypeString) && mopTypeString.equalsIgnoreCase("ALL")) {
				setMopTypeString(paymentOptionsFactory.getMopString());
			}
			loginUserEmailId = sessionUser.getEmailId();
			logger.info("Inside execute(), PaymentOptionsAction Creating new payment option for PayID : " + payId);

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				paymentOptionsFactory.createPaymentOptions(payId, merchantName, creditCard, debitCard, international,
						netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr, mqr, prepaidCard,
						debitCardWithPin, cashOnDelivery, crypto, loginUserEmailId, subMerchantId, mopTypeString,aamarPay, "ACTIVE");
				setResponse("Saved Successfully");
				
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| permissions.toString().contains(PermissionType.PAYMENT_OPTIONS.getPermission())) {
				paymentOptionsFactory.createPaymentOptions(payId, merchantName, creditCard, debitCard, international,
						netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr, mqr, prepaidCard,
						debitCardWithPin, cashOnDelivery, crypto, loginUserEmailId, subMerchantId, mopTypeString,aamarPay, "PENDING");
				
				setResponse("Configuration Saved! Request raised to Admin");
			}
			
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("Not Saved");
			return ERROR;
		}
	}

	public String showAllActive() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			loginUserEmailId = sessionUser.getEmailId();
			logger.info("Inside showAllActive(), PaymentOptionsAction displaying all active payment options");
			setAaData(dataEncoder.encodePaymentOptions(paymentOptionsFactory.getAllActivePaymentOptions()));
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String showSelectedPaymentOption() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			loginUserEmailId = sessionUser.getEmailId();
			logger.info("Inside showSelectedPaymentOption(), PaymentOptionsAction displaying selected payment option");
			User merchant = userDao.findPayId(payId);

			if (merchant != null && merchant.isSuperMerchant()) {
				if (subMerchantId != null && subMerchantId.equals("ALL")) {
					setAaData(dataEncoder
							.encodePaymentOptions(paymentOptionsFactory.getPaymentOptionsOfAllSubMerchant(payId)));
				} else {
					setAaData(dataEncoder
							.encodePaymentOptions(paymentOptionsFactory.getPaymentOptionsPerPayId(subMerchantId)));
				}
			} else {
				setAaData(dataEncoder.encodePaymentOptions(paymentOptionsFactory.getPaymentOptionsPerPayId(payId)));
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String deletePaymentOption() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			loginUserEmailId = sessionUser.getEmailId();
			logger.info(
					"Inside deletePaymentOption(), PaymentOptionsAction deleting payment option for PayID: " + payId);
			paymentOptionsFactory.deletePaymentOption(payId, loginUserEmailId, subMerchantId);
			setResponse("Deleted Successfully");
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("Not Delete");
			return ERROR;
		}
	}

	public String updatePaymentOption() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			loginUserEmailId = sessionUser.getEmailId();
			logger.info(
					"Inside updatePaymentOption(), PaymentOptionsAction updating payment options for PayID : " + payId);
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
			paymentOptionsFactory.updatePaymentOptions(payId, merchantName, creditCard, debitCard, international,
					netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr, mqr, prepaidCard, debitCardWithPin,
					cashOnDelivery, crypto, loginUserEmailId, mopTypeString, subMerchantId,aamarPay, "ACTIVE");
			setResponse("Updated Successfully");
			
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)){
				paymentOptionsFactory.updatePaymentOptions(payId, merchantName, creditCard, debitCard, international,
						netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr, mqr, prepaidCard, debitCardWithPin,
						cashOnDelivery, crypto, loginUserEmailId, mopTypeString, subMerchantId, aamarPay,"PENDING");
				setResponse("Configuration Saved & Pending for Approval");
			}
			
 			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("Not Update");
			return ERROR;
		}
	}

	public String updatePaymentOptionPerPaymentType() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			loginUserEmailId = sessionUser.getEmailId();
			logger.info(
					"Inside updatePaymentOption(), PaymentOptionsAction updating payment options for PayID : " + payId);

			if (Boolean.valueOf(activationFlag) == true) {
				paymentOptionsFactory.activatePaymentOptionPerPaymentType(payId, paymentOptionList, loginUserEmailId,
						mopTypeString);
			} else {
				paymentOptionsFactory.deactivatePaymentOptionPerPaymentType(payId, paymentOptionList, loginUserEmailId,
						mopTypeString);
			}
			setResponse("Updated Successfully");
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("Not Update");
			return ERROR;
		}
	}

	public String getMopTypeForPymentOption() {

		if (paymentType != null && !paymentType.equalsIgnoreCase("ALL")) {
			switch (paymentType) {
			case "CC":
			case "Credit Card":
				mopListCC = (MopType.getCCMopsForPayment());
				break;
			case "DC":
			case "Debit Card":
				mopListDC = (MopType.getDCMopsForPayment());
				break;
			case "NB":
			case "Net Banking":
				mopListNB = (MopType.getNBMopsForPayment());
				break;
			case "WL":
			case "Wallet":
				mopListWL = (MopType.getWLMopsForPayment());
				break;
			case "UPI":
				mopListUPI = (MopType.getUPMopsForPayment());
				break;
			case "CRYPTO":
			case "MQR":
				mopListMQR = (MopType.getMQRMopsForPayment());
				break;
			case "CR":
				mopListCR = (MopType.getCRMopsForPayment());
				break;
			default:
				break;
			}

			if (mopListCC.size() != 0) {
				mopList.put(PaymentType.CREDIT_CARD.getName(), mopListCC);
			}
			if (mopListDC.size() != 0) {
				mopList.put(PaymentType.DEBIT_CARD.getName(), mopListDC);
			}
			if (mopListUPI.size() != 0) {
				mopList.put(PaymentType.UPI.getName(), mopListUPI);
			}
			if (mopListNB.size() != 0) {
				mopList.put(PaymentType.NET_BANKING.getName(), mopListNB);
			}
			if (mopListWL.size() != 0) {
				mopList.put(PaymentType.WALLET.getName(), mopListWL);
			}
			if (mopListCR.size() != 0) {
				mopList.put(PaymentType.WALLET.getName(), mopListCR);
			}
		}

		return SUCCESS;
	}
	
	@SkipValidation
	public String updatePendingPaymentOption(){
		logger.info("inside updatePendingPaymentOption()");
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		loginUserEmailId = sessionUser.getEmailId();
		logger.info("Inside updatePendingPaymentOption(), PaymentOptionsAction Updating Pending payment option for PayID : " + payId);

		try{
		if (sessionUser.getUserType().equals(UserType.ADMIN)) {
			
			PaymentOptions pendingPaymentOption = paymentOptionsFactory.getPendingPaymentOptionByPayId(payId);
			
			
			if(operation.equalsIgnoreCase("Accept")){
				PaymentOptions extingActivePaymentOption = paymentOptionsFactory.getActivePaymentOptionByPayId(payId);
				
				if(extingActivePaymentOption!=null)
				{
					if(StringUtils.isNotBlank(extingActivePaymentOption.getSuperMerchantId())){
						paymentOptionsFactory.deletePaymentOption(extingActivePaymentOption.getSuperMerchantId(), loginUserEmailId, extingActivePaymentOption.getPayId());
					}else{
						paymentOptionsFactory.deletePaymentOption(payId, loginUserEmailId, null);
					}
				}
					
					pendingPaymentOption.setStatus("ACTIVE");
					pendingPaymentOption.setUpdatedDate(new Date());
					pendingPaymentOption.setUpdateBy(loginUserEmailId);
					
					paymentOptionsFactory.updatePendingPaymentOptionStatus(pendingPaymentOption,loginUserEmailId);
					
				
				setResponseStatus("Success");
				setResponse("Saved Successfully");
				
			}else{
				pendingPaymentOption.setUpdateBy(loginUserEmailId);
				pendingPaymentOption.setUpdatedDate(new Date());
				pendingPaymentOption.setStatus(StatusType.REJECTED.getName());
				
				paymentOptionsFactory.updatePendingPaymentOptionStatus(pendingPaymentOption,loginUserEmailId);
				
				setResponseStatus("Success");
				setResponse("Rejected Successfully");
			}
			
			
			
			
		}
		}catch (Exception e) {
			setResponseStatus(StatusType.FAILED.getName());
			setResponse("Payment option not saved");
			logger.info("exception while updating payment options ",e);
		}
		
		return SUCCESS;
	}

	public void validate() {
		if (!payId.equalsIgnoreCase("ALL")) {
			String payIdRaw[] = payId.split(",");
			for (int i = 0; i < payIdRaw.length; i++) {
				if ((validator.validateBlankField(payIdRaw[i]))) {
					addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
				} else if (!(validator.validateField(CrmFieldType.PAY_ID, payIdRaw[i]))) {
					addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
				}
			}
		}
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(String creditCard) {
		this.creditCard = creditCard;
	}

	public String getDebitCard() {
		return debitCard;
	}

	public void setDebitCard(String debitCard) {
		this.debitCard = debitCard;
	}

	public String getNetBanking() {
		return netBanking;
	}

	public void setNetBanking(String netBanking) {
		this.netBanking = netBanking;
	}

	public String getWallet() {
		return wallet;
	}

	public void setWallet(String wallet) {
		this.wallet = wallet;
	}

	public String getEmi() {
		return emi;
	}

	public void setEmi(String emi) {
		this.emi = emi;
	}

	public String getRecurringPayment() {
		return recurringPayment;
	}

	public void setRecurringPayment(String recurringPayment) {
		this.recurringPayment = recurringPayment;
	}

	public String getExpressPay() {
		return expressPay;
	}

	public void setExpressPay(String expressPay) {
		this.expressPay = expressPay;
	}

	public String getUpi() {
		return upi;
	}

	public void setUpi(String upi) {
		this.upi = upi;
	}

	public String getPrepaidCard() {
		return prepaidCard;
	}

	public void setPrepaidCard(String prepaidCard) {
		this.prepaidCard = prepaidCard;
	}

	public String getDebitCardWithPin() {
		return debitCardWithPin;
	}

	public void setDebitCardWithPin(String debitCardWithPin) {
		this.debitCardWithPin = debitCardWithPin;
	}

	public String getCashOnDelivery() {
		return cashOnDelivery;
	}

	public void setCashOnDelivery(String cashOnDelivery) {
		this.cashOnDelivery = cashOnDelivery;
	}

	public String getLoginUserEmailId() {
		return loginUserEmailId;
	}

	public void setLoginUserEmailId(String loginUserEmailId) {
		this.loginUserEmailId = loginUserEmailId;
	}

	public List<PaymentOptions> getAaData() {
		return aaData;
	}

	public void setAaData(List<PaymentOptions> aaData) {
		this.aaData = aaData;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getInternational() {
		return international;
	}

	public void setInternational(String international) {
		this.international = international;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getUpiQr() {
		return upiQr;
	}

	public void setUpiQr(String upiQr) {
		this.upiQr = upiQr;
	}

	public String getPaymentOptionList() {
		return paymentOptionList;
	}

	public void setPaymentOptionList(String paymentOptionList) {
		this.paymentOptionList = paymentOptionList;
	}

	public String getActivationFlag() {
		return activationFlag;
	}

	public void setActivationFlag(String activationFlag) {
		this.activationFlag = activationFlag;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getMopTypeString() {
		return mopTypeString;
	}

	public void setMopTypeString(String mopTypeString) {
		this.mopTypeString = mopTypeString;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public Map<String, Object> getMopList() {
		return mopList;
	}

	public void setMopList(Map<String, Object> mopList) {
		this.mopList = mopList;
	}

	public Map<String, String> getTransList() {
		return transList;
	}

	public void setTransList(Map<String, String> transList) {
		this.transList = transList;
	}

	public String getCrypto() {
		return crypto;
	}

	public void setCrypto(String crypto) {
		this.crypto = crypto;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getAamarPay() {
		return aamarPay;
	}

	public void setAamarPay(String aamarPay) {
		this.aamarPay = aamarPay;
	}

	public String getMqr() {
		return mqr;
	}

	public void setMqr(String mqr) {
		this.mqr = mqr;
	}
	

	
}
