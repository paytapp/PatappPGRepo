package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.dao.PaymentOptionsDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.crm.action.SubMerchantEditAction;

/**
 * @author Amitosh
 *
 */
@Service
public class PaymentOptionsFactory {
	private static Logger logger = LoggerFactory.getLogger(PaymentOptionsFactory.class.getName());

	@Autowired
	PaymentOptionsDao paymentOptionsDao;
	
	@Autowired
	UserDao userDao;
	List<PaymentOptions> pmntOptionList = new ArrayList<PaymentOptions>();

	public List<PaymentOptions> getAllActivePaymentOptions() {
		pmntOptionList = paymentOptionsDao.fetchAllActivePaymentData();
		for(PaymentOptions pmntOption : pmntOptionList) {
			
			String mopTypeString = pmntOption.getMopTypeString();
			if(StringUtils.isNotBlank(mopTypeString)) {
				pmntOption.setMopTypeStringArray(mopTypeString.split(","));
			}
		}
		return pmntOptionList;
	}

	public void createPaymentOptions(String payId, String merchantName, String creditCard, String debitCard,
			String international, String netBanking, String wallet, String emi, String recurringPayment,
			String expressPay, String upi, String upiQr,String mqr, String prepaidCard, String debitCardWithPin,
			String cashOnDelivery, String crypto, String loginUserEmailId,String submerchantId, String mopTypeString, String aamarPay, String status) {

		String superMerchantId =null;
		if(StringUtils.isNotBlank(submerchantId)) {
			superMerchantId = payId;
			payId = submerchantId;
		}

		paymentOptionsDao.createNewPaymentOption(payId, merchantName, creditCard, debitCard, international, netBanking,
				wallet, emi, recurringPayment, expressPay, upi, upiQr,mqr, prepaidCard, debitCardWithPin, cashOnDelivery,crypto,
				loginUserEmailId,superMerchantId, mopTypeString,aamarPay, status);
	}

	public void deletePaymentOption(String payId, String loginUserEmailId, String subMerchantId) {
		
		String superMerchantId =null;
		if(StringUtils.isNotBlank(subMerchantId)) {
			superMerchantId = payId;
			payId = subMerchantId;
		}
		paymentOptionsDao.deletePaymentOptions(payId, loginUserEmailId, superMerchantId);
	}

	public void updatePaymentOptions(String payId, String merchantName, String creditCard, String debitCard,
			String international, String netBanking, String wallet, String emi, String recurringPayment,
			String expressPay, String upi, String upiQr, String mqr, String prepaidCard, String debitCardWithPin,
			String cashOnDelivery, String crypto, String loginUserEmailId, String mopTypeString, String subMerchantId,String aamarPay,String status) {
		String superMerchantId = null;
		if (StringUtils.isNotBlank(subMerchantId)) {
			if (subMerchantId.equalsIgnoreCase("ALL")) {
				List<Merchants> merchantsList = userDao.getSubMerchantListBySuperPayId(payId);
				superMerchantId = payId;
				for (Merchants subMerchants : merchantsList) {
					payId = subMerchants.getPayId();
					if(status.equals("PENDING")){
						paymentOptionsDao.updatePendingPaymentOptionsPerPayId(payId, merchantName, creditCard, debitCard,
								international, netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr,mqr, prepaidCard,
								debitCardWithPin, cashOnDelivery, crypto, loginUserEmailId, superMerchantId, mopTypeString,aamarPay, status);
					}else{
						paymentOptionsDao.updatePaymentOptionsPerPayId(payId, merchantName, creditCard, debitCard,
								international, netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr,mqr,
								prepaidCard, debitCardWithPin, cashOnDelivery,crypto, loginUserEmailId, superMerchantId,
								mopTypeString,aamarPay, status);
					}
				
				}
			} else {
				superMerchantId = payId;
				payId = subMerchantId;
				if(status.equals("PENDING")){
					paymentOptionsDao.updatePendingPaymentOptionsPerPayId(payId, merchantName, creditCard, debitCard,
							international, netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr,mqr, prepaidCard,
							debitCardWithPin, cashOnDelivery, crypto, loginUserEmailId, superMerchantId, mopTypeString,aamarPay, status);
				}else{
					paymentOptionsDao.updatePaymentOptionsPerPayId(payId, merchantName, creditCard, debitCard,
							international, netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr, mqr,prepaidCard,
							debitCardWithPin, cashOnDelivery, crypto, loginUserEmailId, superMerchantId, mopTypeString,aamarPay, status);
				}
				
			}
		} else {
			if(status.equals("PENDING")){
				paymentOptionsDao.updatePendingPaymentOptionsPerPayId(payId, merchantName, creditCard, debitCard,
						international, netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr, mqr, prepaidCard,
						debitCardWithPin, cashOnDelivery, crypto, loginUserEmailId, superMerchantId, mopTypeString,aamarPay, status);
			}else{
				paymentOptionsDao.updatePaymentOptionsPerPayId(payId, merchantName, creditCard, debitCard, international,
						netBanking, wallet, emi, recurringPayment, expressPay, upi, upiQr,mqr, prepaidCard, debitCardWithPin,
						cashOnDelivery, crypto, loginUserEmailId, superMerchantId, mopTypeString,aamarPay, status);
			}
			
		}
//			if(!merchant.isSuperMerchant() && StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
//				superMerchantId = merchant.getSuperMerchantId();
//			}

	}
	public void activatePaymentOptionPerPaymentType(String payId, String paymentOptionList, String loginUserEmailId, String mopTypeString) {

		paymentOptionsDao.activatePaymentOptionPerPaymentType(payId, paymentOptionList, loginUserEmailId, mopTypeString);

	}
	public void deactivatePaymentOptionPerPaymentType(String payId, String paymentOptionList, String loginUserEmailId, String mopTypeString) {

		paymentOptionsDao.deactivatePaymentOptionPerPaymentType(payId, paymentOptionList, loginUserEmailId, mopTypeString);

	}

	public List<PaymentOptions> getPaymentOptionsPerPayId(String payId) {
		pmntOptionList = paymentOptionsDao.fetchPaymentOptionsPerPayId(payId);
		for(PaymentOptions pmntOption : pmntOptionList) {
			StringBuilder builder = new StringBuilder();
			String mopTypeString = pmntOption.getMopTypeString();
			if(StringUtils.isNotBlank(mopTypeString)) {
				String mopTypeArray[] = mopTypeString.split(",");
				for(String mopType : mopTypeArray) {
					
					if(pmntOption.isCreditCard() && mopType.contains("Credit Card")) {
						builder.append(mopType);
						builder.append(",");
					}else if(pmntOption.isDebitCard() && mopType.contains("Debit Card")){
						builder.append(mopType);
						builder.append(",");
					}else if(pmntOption.isNetBanking() && mopType.contains("Net Banking")){
						builder.append(mopType);
						builder.append(",");
					}else if(pmntOption.isWallet() && mopType.contains("Wallet")){
						builder.append(mopType);
						builder.append(",");
					}else if(pmntOption.isUpi() && mopType.contains("UPI")){
						builder.append(mopType);
						builder.append(",");
					}
				}
				if(StringUtils.isNotBlank(builder.toString())) {
					pmntOption.setMopTypeStringArray(builder.toString().split(","));
				}else {
					pmntOption.setMopTypeStringArray(new String[0]);
				}
			}
		}
		
		return pmntOptionList;
	}
	
	public List<PaymentOptions> getPaymentOptionsOfAllSubMerchant(String superMerchantId) {
		
		pmntOptionList = paymentOptionsDao.fetchPaymentOptionsForAllSubMerchant(superMerchantId);
		for(PaymentOptions pmntOption : pmntOptionList) {
			
			String mopTypeString = pmntOption.getMopTypeString();
			if(StringUtils.isNotBlank(mopTypeString)) {
				pmntOption.setMopTypeStringArray(mopTypeString.split(","));
			}
		}
		
		return pmntOptionList;
	}
	
	public String getMopString() {
		StringBuilder stingBuilder = new StringBuilder();
		String []payTypeArray = {"CC","DC","NB","WL","UP"};
		for(String pay : payTypeArray) {
			switch(pay) {
			case "CC":
				String ccName = PaymentType.getpaymentName(pay);
				Map<String, String> ccMopMap = (MopType.getMopsForPayment("CCMOP"));
				for(String key : ccMopMap.keySet()) {
					stingBuilder.append(ccName);
					stingBuilder.append("-");
					stingBuilder.append(key);
					stingBuilder.append(",");
				}
				break;
			case "DC":
				String dcName = PaymentType.getpaymentName(pay);
				Map<String, String> dcMopMap = (MopType.getMopsForPayment("DCMOP"));
				for(String key : dcMopMap.keySet()) {
					stingBuilder.append(dcName);
					stingBuilder.append("-");
					stingBuilder.append(key);
					stingBuilder.append(",");
				}
				break;
			case "NB":
				String nbName = PaymentType.getpaymentName(pay);
				Map<String, String> nbMopMap = (MopType.getMopsForPayment("NBMOP"));
				for(String key : nbMopMap.keySet()) {
					stingBuilder.append(nbName);
					stingBuilder.append("-");
					stingBuilder.append(key);
					stingBuilder.append(",");
				}
				break;
			case "WL":
				String wlName = PaymentType.getpaymentName(pay);
				Map<String, String> wlMopMap = (MopType.getMopsForPayment("WALLET"));
				for(String key : wlMopMap.keySet()) {
					stingBuilder.append(wlName);
					stingBuilder.append("-");
					stingBuilder.append(key);
					stingBuilder.append(",");
				}
				break;
			case "UP":
				String upiName = PaymentType.getpaymentName(pay);
				Map<String, String> upMopMap = (MopType.getMopsForPayment("UPI"));
				for(String key : upMopMap.keySet()) {
					stingBuilder.append(upiName);
					stingBuilder.append("-");
					stingBuilder.append(key);
					stingBuilder.append(",");
				}
				break;
			}
//			Map<String, String> mopMap = (MopType.getMopsForPayment(mop));
//			for(String key : mopMap.keySet()) {
//				
//			}
//			setPaymentType(mop);
//			mopTyepSting.append("");
		}
		stingBuilder.deleteCharAt(stingBuilder.length()-1);
		return stingBuilder.toString();
	}

	public PaymentOptions getPendingPaymentOptionByPayId(String payId) {

		return paymentOptionsDao.getPendingPaymentOptionByPayId(payId);
		
	}
	
	public PaymentOptions getActivePaymentOptionByPayId(String payId) {

		return paymentOptionsDao.getPaymentOption(payId);
		
	}

	public void updatePendingPaymentOptionStatus(PaymentOptions pendingPaymentOption, String loginUserEmailId) {
		paymentOptionsDao.updatePendingPaymentOptionsStatus(pendingPaymentOption,loginUserEmailId);
	}
}
