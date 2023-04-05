/** 
 * To be invoked from CRM for setting surcharge dynamic text which would be "Merchant" and "Payment Type" specific 
 */
package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.MerchantSurchargeText;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.crm.actionBeans.MerchantSurchargeTextFactory;

/**
 * @author Amitosh Aanand
 *
 */
public class MerchantSurchargeTextAction extends AbstractSecureAction {

	private String payId;
	private String merchantName;
	private String paymentType;
	private String paymentRegion;
	private String cardHolderType;
	private String mopType;
	private String amountSlab;
	private String surchargeText;

	@Autowired
	MerchantSurchargeTextFactory factory;

	@Autowired
	private DataEncoder dataEncoder;

	private User sessionUser = new User();
	private List<MerchantSurchargeText> aaData = new ArrayList<MerchantSurchargeText>();
	private static final long serialVersionUID = 2974975225012600323L;
	private static Logger logger = LoggerFactory.getLogger(MerchantSurchargeTextAction.class.getName());

	// For fetching all active text payId wise
	public String execute() {
		try {
			logger.info("Inside execute(), MerchantSurchargeTextAction");
			/*
			 * setAaData(dataEncoder.encodeMerchantSurchargeText(factory.
			 * getSpecificMerchantSurchargeText(payId, paymentType, paymentRegion,
			 * cardHolderType, mopType, amountSlab)));
			 */
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	// For creating or editing surcharge text
	public String createEditSurchargeText() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String loginUserEmailId = sessionUser.getEmailId();
			logger.info(
					"Inside createSurchargeText(), MerchantSurchargeTextAction Creating new Surcharge Text for PayID : "
							+ payId);
			factory.createEditSurchargeText(payId, paymentType, paymentRegion, cardHolderType, mopType, amountSlab,
					surchargeText, loginUserEmailId);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	// For deleting surcharge text
	public String deleteSurchargeText() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String loginUserEmailId = sessionUser.getEmailId();
			logger.info("Inside deleteSurchargeText(), MerchantSurchargeTextAction Deleting Surcharge Text for PayID : "
					+ payId);
			factory.deleteSurchargeText(payId, paymentType, paymentRegion, cardHolderType, mopType, amountSlab,
					loginUserEmailId);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getAmountSlab() {
		return amountSlab;
	}

	public void setAmountSlab(String amountSlab) {
		this.amountSlab = amountSlab;
	}

	public String getSurchargeText() {
		return surchargeText;
	}

	public void setSurchargeText(String surchargeText) {
		this.surchargeText = surchargeText;
	}

	public List<MerchantSurchargeText> getAaData() {
		return aaData;
	}

	public void setAaData(List<MerchantSurchargeText> aaData) {
		this.aaData = aaData;
	}
}