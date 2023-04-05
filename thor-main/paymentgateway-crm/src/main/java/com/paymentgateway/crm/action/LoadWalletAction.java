package com.paymentgateway.crm.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.OrderIdGenerator;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 *  Shiva
 */

//Note: LyraDirect is 3rd party page not working on UAT & Local.it redirect on lyraDirect not our PG page

public class LoadWalletAction extends AbstractSecureAction{

	private static final long serialVersionUID = -5238816358987392424L;
	private static Logger logger = LoggerFactory.getLogger(LoadWalletAction.class.getName());
	

	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private OrderIdGenerator orderIdGenerator;
	
	private String payId;
	private String subMerchantPayId;
	private String amount;
	private String currencyCode;
	private String orderId;
	private String txnType;
	private String returnUrl;
	private String hash;

	
	public String execute() {
		
		Map<String, String> requestMap=new HashMap<String,String>();
		
		try {
			
		setCurrencyCode("356");
		setAmount(Amount.formatAmount(amount,currencyCode));
		setTxnType("SALE");
		setOrderId(orderIdGenerator.getNewOrderId());
		setReturnUrl(PropertiesManager.propertiesMap.get(CrmFieldConstants.CUSTOM_RETURN_URL.getValue()));
		
		requestMap.put(FieldType.TXNTYPE.getName(), getTxnType());
		requestMap.put(FieldType.AMOUNT.getName(), getAmount());
		requestMap.put(FieldType.CURRENCY_CODE.getName(), getCurrencyCode());
		requestMap.put(FieldType.ORDER_ID.getName(), getOrderId());
		requestMap.put(FieldType.RETURN_URL.getName(), getReturnUrl());
		
		if(StringUtils.isNotBlank(subMerchantPayId)){
			requestMap.put(FieldType.PAY_ID.getName(), getSubMerchantPayId());
		}else{
			requestMap.put(FieldType.PAY_ID.getName(), getPayId());
		}
		
		
			setHash(Hasher.getHash(new Fields(requestMap)));
			requestMap.put(FieldType.HASH.getName(), getHash());
		
		} catch (Exception e) {
			logger.error("exception " , e);
		}
		return SUCCESS;
	}
	
	public void validator() {

		if ((validator.validateBlankField(getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), getPayId());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), getPayId());
		}

		if ((validator.validateBlankField(getAmount()))) {
			addFieldError(CrmFieldType.AMOUNT.getName(), getAmount());
		} else if (!(validator.validateField(CrmFieldType.AMOUNT, getAmount()))) {
			addFieldError(CrmFieldType.AMOUNT.getName(), getAmount());
		}

	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	

}
