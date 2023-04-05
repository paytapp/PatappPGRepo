package com.paymentgateway.pgui.action;
/*package com.paymentgateway.pg.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.CalculateSurchargeAmount;

public class SurchargeCalculatorAction extends AbstractSecureAction{

	private static final long serialVersionUID = 6003754438615044289L;
	private String surcharge_cc;
	private String surcharge_dc;
	private String surcharge_nb;
	private String surcharge_up;
	private String surcharge_ad;
	private String surcharge_dcWthPin;
	private boolean express_pay = false ;
	private boolean creditCard ;
	private boolean debitCard ;
	private boolean debitWithPin ;
	private boolean netBanking ;
	private boolean upi ;
	private boolean tokenAvailable ;
	private String mopTypes [];
	private boolean emailId = false;
	private String firstTimeWaitSeconds;
	private String furtherWaitSeconds;
	private String currencyCode;
	
	@Autowired
	private CalculateSurchargeAmount calculateSurchargeAmount;
	
	private static Logger logger = LoggerFactory.getLogger(SurchargeCalculatorAction.class.getName());
	
	public String execute() {
		
		populateSessionMap();
		
		setSurcharge_cc(sessionMap.get(FieldType.CC_SURCHARGE.getName()).toString());
		setSurcharge_dc(sessionMap.get(FieldType.DC_SURCHARGE.getName()).toString());
		setSurcharge_up(sessionMap.get(FieldType.UP_SURCHARGE.getName()).toString());
		setSurcharge_nb(sessionMap.get(FieldType.NB_SURCHARGE.getName()).toString());
		setSurcharge_ad(sessionMap.get(FieldType.AD_SURCHARGE.getName()).toString());
		setCurrencyCode(sessionMap.get(FieldType.CURRENCY_CODE.getName()).toString());
		
		setSurcharge_dcWthPin(sessionMap.get(FieldType.DC_SURCHARGE.getName()).toString());
		setFirstTimeWaitSeconds(PropertiesManager.propertiesMap.get("firstTimeWaitSeconds"));
		setFurtherWaitSeconds(PropertiesManager.propertiesMap.get("furtherWaitSeconds"));
		
			if (sessionMap.get("TOKEN").toString().equals("NA")) {
				setTokenAvailable(false);
			}
			else {
				setTokenAvailable(true);
			}
		
		Object custEmailId = sessionMap.get(FieldType.CUST_EMAIL.getName());
		
		if (custEmailId != null && !String.valueOf(custEmailId).trim().equals("")) {
			setEmailId(true);
			if (sessionMap.get("EXPRESS_PAY_FLAG").toString().equals("true")) {
				setExpress_pay(true);
			}
		}
		
		String paymentTypeMops = sessionMap.get("PAYMENT_TYPE_MOP").toString();
		
		if (paymentTypeMops.contains("CC=")) {
			setCreditCard(true);
		}
		else {
			setCreditCard(false);
		}

		if (paymentTypeMops.contains("DC=")) {
			setDebitCard(true);
		}
		else {
			setDebitCard(false);
		}
		
		if (paymentTypeMops.contains("NB=")) {
			setNetBanking(true);
		}
		else {
			setNetBanking(false);
		}
		
		if (paymentTypeMops.contains("DP=")) {
			setDebitWithPin(true);
		}
		else {
			setDebitWithPin(false);
		}
		
		if (paymentTypeMops.contains("UP=")) {
			setUpi(true);
		}
		else {
			setUpi(false);
		}
			
		List<String > mopAvlList = new ArrayList<String>();
		
		if (paymentTypeMops.contains("RUPAY")) {
			mopAvlList.add("RU");
		}
		if (paymentTypeMops.contains("VISA")) {
			mopAvlList.add("VI");
		}
		if (paymentTypeMops.contains("MASTERCARD")) {
			mopAvlList.add("MC");
		}
		if (paymentTypeMops.contains("AMEX")) {
			mopAvlList.add("AX");
		}
		if (paymentTypeMops.contains("MAESTRO")) {
			mopAvlList.add("MS");
		}
		if (paymentTypeMops.contains("DINERS")) {
			mopAvlList.add("DN");
		}
		if (paymentTypeMops.contains("DISCOVER")) {
			mopAvlList.add("DI");
		}
		if (paymentTypeMops.contains("JCB")) {
			mopAvlList.add("JC");
		}
	
		// Give all options for Now , will update later
		//String mopTypesArray [] = {"RU","VI","MC","MS","AX","DV","DI","JC"}; 
		
		
		Object[] objectList = mopAvlList.toArray();
		String[] stringArray =  Arrays.copyOf(objectList,objectList.length,String[].class);
		setMopTypes(stringArray);
		
		return SUCCESS;
		
	}

	public void populateSessionMap() {
		
		String payId = sessionMap.get("PAY_ID").toString();
		logger.info("payId is "+payId);
		
		BigDecimal serviceTax = BigDecimal.ONE;
		String convertedamount = sessionMap.get("AMOUNT").toString();
		String currency = sessionMap.get("CURRENCY_CODE").toString();
		
		String paymentsRegion = sessionMap.get("PAYMENTS_REGION").toString();

		String amount = Amount.toDecimal(convertedamount, currency);

		BigDecimal[] surCCAmount = calculateSurchargeAmount.fetchCCSurchargeDetails(amount, payId,paymentsRegion);
		BigDecimal[] surDCAmount = calculateSurchargeAmount.fetchDCSurchargeDetails(amount, payId,paymentsRegion);
		BigDecimal[] surNBAmount = calculateSurchargeAmount.fetchNBSurchargeDetails(amount, payId,paymentsRegion);
		BigDecimal[] surUPAmount = calculateSurchargeAmount.fetchUPSurchargeDetails(amount, payId,paymentsRegion);
		BigDecimal[] surADAmount = calculateSurchargeAmount.fetchADSurchargeDetails(amount, payId,paymentsRegion);

		
		BigDecimal surchargeDetails[] = new BigDecimal[4];
		surchargeDetails[0]= BigDecimal.ONE;
		surchargeDetails[1] =  BigDecimal.ONE;
		surchargeDetails[2] =  BigDecimal.ONE;
		surchargeDetails[3] =  BigDecimal.ONE;
		
		
		BigDecimal[] surCCAmount = surchargeDetails;
		BigDecimal[] surDCAmount = surchargeDetails;
		BigDecimal[] surNBAmount = surchargeDetails;
		BigDecimal[] surUPAmount = surchargeDetails;
		BigDecimal[] surADAmount = surchargeDetails;
		
		
		BigDecimal ccTransSurcharge = surCCAmount[0];
		BigDecimal surchargeCCAmount = surCCAmount[1];
		BigDecimal ccSurchargeAmount = surCCAmount[2];
		BigDecimal ccSurchargePercentage = surCCAmount[3];

		BigDecimal dcTransSurcharge = surDCAmount[0];
		BigDecimal surchargeDCAmount = surDCAmount[1];
		BigDecimal dcSurchargeAmount = surDCAmount[2];
		BigDecimal dcSurchargePercentage = surDCAmount[3];

		BigDecimal nbTransSurcharge = surNBAmount[0];
		BigDecimal surchargeNBAmount = surNBAmount[1];
		BigDecimal nbSurchargeAmount = surNBAmount[2];
		BigDecimal nbSurchargePercentage = surNBAmount[3];

		BigDecimal upTransSurcharge = surUPAmount[0];
		BigDecimal surchargeUPAmount = surUPAmount[1];
		BigDecimal upSurchargeAmount = surUPAmount[2];
		BigDecimal upSurchargePercentage = surUPAmount[3];
		
		BigDecimal adTransSurcharge = surADAmount[0];
		BigDecimal surchargeADAmount = surADAmount[1];
		BigDecimal adSurchargeAmount = surADAmount[2];
		BigDecimal adSurchargePercentage = surADAmount[3];

		sessionMap.put(FieldType.SURCHARGE_CC_AMOUNT.getName(), surchargeCCAmount);
		sessionMap.put(FieldType.SURCHARGE_DC_AMOUNT.getName(), surchargeDCAmount);
		sessionMap.put(FieldType.SURCHARGE_NB_AMOUNT.getName(), surchargeNBAmount);
		sessionMap.put(FieldType.SURCHARGE_UP_AMOUNT.getName(), surchargeUPAmount);
		sessionMap.put(FieldType.SURCHARGE_AD_AMOUNT.getName(), surchargeADAmount);
		
		sessionMap.put(FieldType.CC_SURCHARGE.getName(), ccTransSurcharge);
		sessionMap.put(FieldType.DC_SURCHARGE.getName(), dcTransSurcharge);
		sessionMap.put(FieldType.NB_SURCHARGE.getName(), nbTransSurcharge);
		sessionMap.put(FieldType.UP_SURCHARGE.getName(), upTransSurcharge);
		sessionMap.put(FieldType.AD_SURCHARGE.getName(), adTransSurcharge);

		sessionMap.put(FieldType.CC_SURCHARGE_RATE_AMOUNT.getName(), ccSurchargeAmount);
		sessionMap.put(FieldType.DC_SURCHARGE_RATE_AMOUNT.getName(), dcSurchargeAmount);
		sessionMap.put(FieldType.NB_SURCHARGE_RATE_AMOUNT.getName(), nbSurchargeAmount);
		sessionMap.put(FieldType.UP_SURCHARGE_RATE_AMOUNT.getName(), upSurchargeAmount);
		sessionMap.put(FieldType.AD_SURCHARGE_RATE_AMOUNT.getName(), adSurchargeAmount);

		sessionMap.put(FieldType.CC_SURCHARGE_RATE_PERCENTAGE.getName(), ccSurchargePercentage);
		sessionMap.put(FieldType.DC_SURCHARGE_RATE_PERCENTAGE.getName(), dcSurchargePercentage);
		sessionMap.put(FieldType.NB_SURCHARGE_RATE_PERCENTAGE.getName(), nbSurchargePercentage);
		sessionMap.put(FieldType.UP_SURCHARGE_RATE_PERCENTAGE.getName(), upSurchargePercentage);
		sessionMap.put(FieldType.AD_SURCHARGE_RATE_PERCENTAGE.getName(), adSurchargePercentage);

		sessionMap.put(FieldType.SERVICE_TAX.getName(), serviceTax);

	}
	
	
	public String getSurcharge_dcWthPin() {
		return surcharge_dcWthPin;
	}

	public void setSurcharge_dcWthPin(String surcharge_dcWthPin) {
		this.surcharge_dcWthPin = surcharge_dcWthPin;
	}

	public boolean isExpress_pay() {
		return express_pay;
	}

	public void setExpress_pay(boolean express_pay) {
		this.express_pay = express_pay;
	}

	public boolean isCreditCard() {
		return creditCard;
	}

	public void setCreditCard(boolean creditCard) {
		this.creditCard = creditCard;
	}

	public boolean isDebitCard() {
		return debitCard;
	}

	public void setDebitCard(boolean debitCard) {
		this.debitCard = debitCard;
	}

	public boolean isDebitWithPin() {
		return debitWithPin;
	}

	public void setDebitWithPin(boolean debitWithPin) {
		this.debitWithPin = debitWithPin;
	}

	public boolean isNetBanking() {
		return netBanking;
	}

	public void setNetBanking(boolean netBanking) {
		this.netBanking = netBanking;
	}

	public boolean isUpi() {
		return upi;
	}

	public void setUpi(boolean upi) {
		this.upi = upi;
	}

	public String getSurcharge_cc() {
		return surcharge_cc;
	}

	public void setSurcharge_cc(String surcharge_cc) {
		this.surcharge_cc = surcharge_cc;
	}

	public String getSurcharge_dc() {
		return surcharge_dc;
	}

	public void setSurcharge_dc(String surcharge_dc) {
		this.surcharge_dc = surcharge_dc;
	}

	public String getSurcharge_nb() {
		return surcharge_nb;
	}

	public void setSurcharge_nb(String surcharge_nb) {
		this.surcharge_nb = surcharge_nb;
	}

	public String getSurcharge_up() {
		return surcharge_up;
	}

	public void setSurcharge_up(String surcharge_up) {
		this.surcharge_up = surcharge_up;
	}

	public String[] getMopTypes() {
		return mopTypes;
	}

	public void setMopTypes(String mopTypes[]) {
		this.mopTypes = mopTypes;
	}

	public String getFirstTimeWaitSeconds() {
		return firstTimeWaitSeconds;
	}

	public boolean isEmailId() {
		return emailId;
	}

	public void setEmailId(boolean emailId) {
		this.emailId = emailId;
	}

	public void setFirstTimeWaitSeconds(String firstTimeWaitSeconds) {
		this.firstTimeWaitSeconds = firstTimeWaitSeconds;
	}

	public String getFurtherWaitSeconds() {
		return furtherWaitSeconds;
	}

	public void setFurtherWaitSeconds(String furtherWaitSeconds) {
		this.furtherWaitSeconds = furtherWaitSeconds;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public boolean isTokenAvailable() {
		return tokenAvailable;
	}

	public void setTokenAvailable(boolean tokenAvailable) {
		this.tokenAvailable = tokenAvailable;
	}

	public String getSurcharge_ad() {
		return surcharge_ad;
	}

	public void setSurcharge_ad(String surcharge_ad) {
		this.surcharge_ad = surcharge_ad;
	}

	
}
*/