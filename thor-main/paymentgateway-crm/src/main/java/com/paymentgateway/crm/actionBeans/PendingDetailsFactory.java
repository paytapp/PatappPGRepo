package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.SessionMap;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.paymentgateway.commons.dao.PaymentOptionsDao;
import com.paymentgateway.commons.dao.PendingBulkUserDao;
import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.MapList;
import com.paymentgateway.commons.user.MerchantCurrencyPopulator;
import com.paymentgateway.commons.user.MerchantMopPopulator;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.PendingBulkCharges;
import com.paymentgateway.commons.user.PendingBulkUserRequest;
import com.paymentgateway.commons.user.PendingMappingRequest;
import com.paymentgateway.commons.user.PendingResellerMappingApproval;
import com.paymentgateway.commons.user.PendingResellerMappingDao;
import com.paymentgateway.commons.user.PendingUserApproval;
import com.paymentgateway.commons.user.PendingUserApprovalDao;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.SurchargeMappingPopulator;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

/**
 * @author Rahul, Shaiwal
 *
 */
@Service
public class PendingDetailsFactory {

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private PendingResellerMappingDao pendingResellerMappingDao;

	@Autowired
	private PendingUserApprovalDao pendingUserApprovalDao;

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PendingMappingRequestDao pendingMappingRequestDao;

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	@Autowired
	private PendingBulkUserDao pendingBulkUserDao;
	
	@Autowired
	private PaymentOptionsDao paymentOptionsDao;

	public Map<String, List<SurchargeDetails>> getPendingSurchargeDetails(SessionMap<String, Object> sessionMap) {
		List<SurchargeDetails> pendingSurchargeDetails = new ArrayList<SurchargeDetails>();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_SURCHARGE.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {
			pendingSurchargeDetails = surchargeDetailsDao.findPendingDetails();
			for (SurchargeDetails surchargeDet : pendingSurchargeDetails) {
				String merchantName = userDao.getMerchantNameByPayId(surchargeDet.getPayId());
				surchargeDet.setMerchantName(merchantName);
			}
		}
		Map<String, List<SurchargeDetails>> detailsMap = new HashMap<String, List<SurchargeDetails>>();

		if (pendingSurchargeDetails.size() != 0) {
			detailsMap.put("Merchant Surcharge", pendingSurchargeDetails);
			return detailsMap;
		} else {
			return detailsMap;
		}
	}

	public Map<String, List<SurchargeMappingPopulator>> getPendingPGSurchargeDetails(
			SessionMap<String, Object> sessionMap) {

		List<Surcharge> pendingSurchargeList = null;
		ArrayList<SurchargeMappingPopulator> details = new ArrayList<SurchargeMappingPopulator>();
		Map<String, List<SurchargeMappingPopulator>> detailsMap = new HashMap<String, List<SurchargeMappingPopulator>>();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_SURCHARGE.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingSurchargeList = surchargeDao.findPendingSurchargeList();
			for (Surcharge surcharge : pendingSurchargeList) {

				List<Surcharge> tempList = new ArrayList<Surcharge>();
				for (Surcharge surchargeComp : pendingSurchargeList) {
					if (surchargeComp.getPayId().equals(surcharge.getPayId())
							&& surchargeComp.getPaymentType().equals(surcharge.getPaymentType())
							&& surchargeComp.getMopType().equals(surcharge.getMopType())
							&& surchargeComp.getAcquirerName().equals(surcharge.getAcquirerName())) {
						tempList.add(surchargeComp);

					}
				}
				if (tempList.size() == 1) {
					SurchargeMappingPopulator smp = new SurchargeMappingPopulator();
					for (Surcharge srch : tempList) {
						smp = generateSurchargeMap(srch, false, BigDecimal.ZERO, BigDecimal.ZERO);
						details.add(smp);
					}
				} else {
					SurchargeMappingPopulator smp = new SurchargeMappingPopulator();
					for (Surcharge srch : tempList) {
						if (srch.getOnOff().equals("1")) {
							smp.setAcquirerName(srch.getAcquirerName());
							smp.setMopType(srch.getMopType().getName());
							smp.setPaymentType(srch.getPaymentType().getName());
							smp.setStatus(srch.getStatus().getName());

							smp.setBankSurchargeAmountOnCommercial(srch.getBankSurchargeAmountCommercial());
							smp.setBankSurchargePercentageOnCommercial(srch.getBankSurchargePercentageCommercial());
							smp.setBankSurchargeAmountOnCustomer(srch.getBankSurchargeAmountCustomer());
							smp.setBankSurchargePercentageOnCustomer(srch.getBankSurchargePercentageCustomer());

							smp.setPaymentsRegion(srch.getPaymentsRegion());
							smp.setPayId(srch.getPayId());
							smp.setPaymentType(srch.getPaymentType().getName());
							smp.setAcquirerName(srch.getAcquirerName());
							smp.setMerchantName(userDao.getMerchantNameByPayId(srch.getPayId()));
							smp.setAllowOnOff(true);
							smp.setRequestedBy(srch.getRequestedBy());
						} else {
							smp.setBankSurchargeAmountOffCommercial(srch.getBankSurchargeAmountCommercial());
							smp.setBankSurchargePercentageOffCommercial(srch.getBankSurchargePercentageCommercial());
							smp.setBankSurchargeAmountOffCustomer(srch.getBankSurchargeAmountCustomer());
							smp.setBankSurchargePercentageOffCustomer(srch.getBankSurchargePercentageCustomer());
						}
					}
					details.add(smp);
				}
			}
		}

		List<SurchargeMappingPopulator> uniques = new ArrayList<SurchargeMappingPopulator>();
		uniques = removeDuplicateFromList(details);

		detailsMap.put("Bank Surcharge", uniques);
		return detailsMap;
	}

	public Map<String, List<ChargingDetails>> getPendingChargingDetails(SessionMap<String, Object> sessionMap) {

		Map<String, List<ChargingDetails>> chargingDetailsMap = new HashMap<String, List<ChargingDetails>>();
		List<ChargingDetails> data = new ArrayList<ChargingDetails>();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_TDR.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			data = chargingDetailsDao.getPendingChargingDetailList();

			if (data.size() > 0) {
				for (PaymentType paymentType : PaymentType.values()) {
					List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
					String paymentName = paymentType.getName();

					for (ChargingDetails cDetail : data) {
						if (cDetail.getPaymentType().getName().equals(paymentName)) {
							String businessName = userDao.getBusinessNameByPayId(cDetail.getPayId());
							cDetail.setBusinessName(businessName);
							chargingDetailsList.add(cDetail);
						}
					}

					if (chargingDetailsList.size() != 0) {
						Collections.sort(chargingDetailsList);
						chargingDetailsMap.put(paymentName, chargingDetailsList);
					}
				}
			}
			return chargingDetailsMap;
		}
		return chargingDetailsMap;
	}
	
	public List<ChargingDetails> getChargingDetails(SessionMap<String, Object> sessionMap) {

		List<ChargingDetails> chargingDetailsMap = new ArrayList<ChargingDetails>();
		List<ChargingDetails> data = new ArrayList<ChargingDetails>();
		
		List<ChargingDetails> totalFetchedData = new ArrayList<ChargingDetails>();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_TDR.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			data = chargingDetailsDao.getPendingChargingDetailList();
			
			String acqName=null;
			onUsOffUs acqMode=null;
			CardHolderType cardHolderType=null;
			String currency=null;
			MopType mopType=null;
			String payId=null;
			PaymentType paymentType=null;
			AccountCurrencyRegion paymentRegion=null;

			for (ChargingDetails cDetail : data) {
				
				if(StringUtils.isNotBlank(acqName)){
					if(acqName.equals(cDetail.getAcquirerName()) && payId.equals(cDetail.getPayId()) && acqMode.equals(cDetail.getAcquiringMode())
							&& cardHolderType.equals(cDetail.getCardHolderType()) && currency.equals(cDetail.getCurrency()) && mopType.equals(cDetail.getMopType())
							&& paymentType.equals(cDetail.getPaymentType()) && paymentRegion.equals(cDetail.getPaymentsRegion())){
						continue;
					}
				}
				
				acqName=cDetail.getAcquirerName();
				acqMode=cDetail.getAcquiringMode();
				cardHolderType=cDetail.getCardHolderType();
				currency=cDetail.getCurrency();
				mopType=cDetail.getMopType();
				payId=cDetail.getPayId();
				paymentType=cDetail.getPaymentType();
				paymentRegion=cDetail.getPaymentsRegion();
				
				List<ChargingDetails> chargingDetailsForMerchantSlab = new ArrayList<ChargingDetails>();
				
				chargingDetailsForMerchantSlab = chargingDetailsDao.getAllSlabGroupForPendingChargingDetailList(acqName, acqMode, cardHolderType, currency,
						mopType, payId, paymentType, paymentRegion, TDRStatus.PENDING);
				
				ChargingDetails singleChargingDetailsData = null;
				
				for(ChargingDetails chargingDetails: chargingDetailsForMerchantSlab){
					
					if(singleChargingDetailsData==null){
						singleChargingDetailsData = chargingDetails;
						singleChargingDetailsData.setMerchantTdrString(String.valueOf(chargingDetails.getMerchantTDR()));
						singleChargingDetailsData.setMerchantFixChargeString(String.valueOf(chargingDetails.getMerchantFixCharge()));
						singleChargingDetailsData.setBankTDRString(String.valueOf(chargingDetails.getBankTDR()));
						singleChargingDetailsData.setBankFixChargeString(String.valueOf(chargingDetails.getBankFixCharge()));
						singleChargingDetailsData.setResellerTDRString(String.valueOf(chargingDetails.getResellerTDR()));
						singleChargingDetailsData.setResellerFixChargeString(String.valueOf(chargingDetails.getResellerFixCharge()));
						singleChargingDetailsData.setSlabString(String.valueOf(chargingDetails.getMinTxnAmount()+"-"+chargingDetails.getMaxTxnAmount()));
						singleChargingDetailsData.setIdString(String.valueOf(chargingDetails.getId()));
						continue;
					}
					
					String merchantTdr=singleChargingDetailsData.getMerchantTdrString()+","+chargingDetails.getMerchantTDR();
					String merchantfixChage=singleChargingDetailsData.getMerchantFixChargeString()+","+chargingDetails.getMerchantFixCharge();
					String bankTdr=singleChargingDetailsData.getBankTDRString()+","+chargingDetails.getBankTDR();
					String bankFixCharge=singleChargingDetailsData.getBankFixChargeString()+","+chargingDetails.getBankFixCharge();
					String resellerTdr=singleChargingDetailsData.getResellerTDRString()+","+chargingDetails.getResellerTDR();
					String resellerFixCharge=singleChargingDetailsData.getResellerFixChargeString()+","+chargingDetails.getResellerFixCharge();
					String slab=singleChargingDetailsData.getSlabString()+","+(chargingDetails.getMinTxnAmount()+"-"+chargingDetails.getMaxTxnAmount());
					String id=singleChargingDetailsData.getIdString()+","+String.valueOf(chargingDetails.getId());
					
					singleChargingDetailsData.setMerchantTdrString(merchantTdr);
					singleChargingDetailsData.setMerchantFixChargeString(merchantfixChage);
					singleChargingDetailsData.setBankTDRString(bankTdr);
					singleChargingDetailsData.setBankFixChargeString(bankFixCharge);
					singleChargingDetailsData.setResellerTDRString(resellerTdr);
					singleChargingDetailsData.setResellerFixChargeString(resellerFixCharge);
					singleChargingDetailsData.setSlabString(slab);
					singleChargingDetailsData.setIdString(id);
					
					
					singleChargingDetailsData.setBusinessName(userDao.getBusinessNameByPayId(chargingDetails.getPayId()));
					singleChargingDetailsData.setRequestedBy(userDao.getBusinessNameByEmailId(chargingDetails.getRequestedBy()));
					
				}
				totalFetchedData.add(singleChargingDetailsData);
				Collections.sort(chargingDetailsForMerchantSlab);

			}
		}
		return totalFetchedData;
	}
	
	public List<Object> getChargingDetailsForPendingReport(SessionMap<String, Object> sessionMap, String status) {

		List<ChargingDetails> chargingDetailsMap = new ArrayList<ChargingDetails>();
		List<ChargingDetails> data = new ArrayList<ChargingDetails>();
		
		List<Object> totalFetchedData = new ArrayList<Object>();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_TDR.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			data = chargingDetailsDao.getChargingDetailListForPendingReport(status);
			
			String acqName=null;
			onUsOffUs acqMode=null;
			CardHolderType cardHolderType=null;
			String currency=null;
			MopType mopType=null;
			String payId=null;
			PaymentType paymentType=null;
			AccountCurrencyRegion paymentRegion=null;

			for (ChargingDetails cDetail : data) {
				
				if(StringUtils.isNotBlank(acqName)){
					if(acqName.equals(cDetail.getAcquirerName()) && payId.equals(cDetail.getPayId()) && acqMode.equals(cDetail.getAcquiringMode())
							&& cardHolderType.equals(cDetail.getCardHolderType()) && currency.equals(cDetail.getCurrency()) && mopType.equals(cDetail.getMopType())
							&& paymentType.equals(cDetail.getPaymentType()) && paymentRegion.equals(cDetail.getPaymentsRegion())){
						continue;
					}
				}
				
				acqName=cDetail.getAcquirerName();
				acqMode=cDetail.getAcquiringMode();
				cardHolderType=cDetail.getCardHolderType();
				currency=cDetail.getCurrency();
				mopType=cDetail.getMopType();
				payId=cDetail.getPayId();
				paymentType=cDetail.getPaymentType();
				paymentRegion=cDetail.getPaymentsRegion();
				
				List<ChargingDetails> chargingDetailsForMerchantSlab = new ArrayList<ChargingDetails>();
				
				chargingDetailsForMerchantSlab = chargingDetailsDao.getAllSlabGroupForPendingChargingDetailReportList(acqName, acqMode, cardHolderType, currency,
						mopType, payId, paymentType, paymentRegion, status);
				
				ChargingDetails singleChargingDetailsData = null;
				
				for(ChargingDetails chargingDetails: chargingDetailsForMerchantSlab){
					
					if(singleChargingDetailsData==null){
						singleChargingDetailsData = chargingDetails;
						singleChargingDetailsData.setMerchantTdrString(String.valueOf(chargingDetails.getMerchantTDR()));
						singleChargingDetailsData.setMerchantFixChargeString(String.valueOf(chargingDetails.getMerchantFixCharge()));
						singleChargingDetailsData.setBankTDRString(String.valueOf(chargingDetails.getBankTDR()));
						singleChargingDetailsData.setBankFixChargeString(String.valueOf(chargingDetails.getBankFixCharge()));
						singleChargingDetailsData.setResellerTDRString(String.valueOf(chargingDetails.getResellerTDR()));
						singleChargingDetailsData.setResellerFixChargeString(String.valueOf(chargingDetails.getResellerFixCharge()));
						singleChargingDetailsData.setSlabString(String.valueOf(chargingDetails.getMinTxnAmount()+"-"+chargingDetails.getMaxTxnAmount()));
						singleChargingDetailsData.setIdString(String.valueOf(chargingDetails.getId()));
						continue;
					}
					
					String merchantTdr=singleChargingDetailsData.getMerchantTdrString()+","+chargingDetails.getMerchantTDR();
					String merchantfixChage=singleChargingDetailsData.getMerchantFixChargeString()+","+chargingDetails.getMerchantFixCharge();
					String bankTdr=singleChargingDetailsData.getBankTDRString()+","+chargingDetails.getMerchantFixCharge();
					String bankFixCharge=singleChargingDetailsData.getBankFixChargeString()+","+chargingDetails.getBankFixCharge();
					String resellerTdr=singleChargingDetailsData.getResellerTDRString()+","+chargingDetails.getResellerTDR();
					String resellerFixCharge=singleChargingDetailsData.getResellerFixChargeString()+","+chargingDetails.getResellerFixCharge();
					String slab=singleChargingDetailsData.getSlabString()+","+(chargingDetails.getMinTxnAmount()+"-"+chargingDetails.getMaxTxnAmount());
					String id=singleChargingDetailsData.getIdString()+","+String.valueOf(chargingDetails.getId());
					
					singleChargingDetailsData.setMerchantTdrString(merchantTdr);
					singleChargingDetailsData.setMerchantFixChargeString(merchantfixChage);
					singleChargingDetailsData.setBankTDRString(bankTdr);
					singleChargingDetailsData.setBankFixChargeString(bankFixCharge);
					singleChargingDetailsData.setResellerTDRString(resellerTdr);
					singleChargingDetailsData.setResellerFixChargeString(resellerFixCharge);
					singleChargingDetailsData.setSlabString(slab);
					singleChargingDetailsData.setIdString(id);
					
					singleChargingDetailsData.setBusinessName(userDao.getBusinessNameByPayId(chargingDetails.getPayId()));
					singleChargingDetailsData.setRequestedBy(userDao.getBusinessNameByEmailId(chargingDetails.getRequestedBy()));
					
					if(StringUtils.isNotBlank(chargingDetails.getUpdateBy()))
						singleChargingDetailsData.setUpdateBy(userDao.getBusinessNameByEmailId(chargingDetails.getUpdateBy()));
					
					
				}
				//Collections.sort(totalFetchedData);
				totalFetchedData.add(singleChargingDetailsData);
				
			}
		}
		return totalFetchedData;
	}

	public SurchargeMappingPopulator generateSurchargeMap(Surcharge surcharge, boolean allowOnOff,
			BigDecimal bankSurchargeAmountOff, BigDecimal bankSurchargePercentageOff) {

		SurchargeMappingPopulator smp = new SurchargeMappingPopulator();
		smp.setAcquirerName(surcharge.getAcquirerName());
		smp.setMopType(surcharge.getMopType().getName());
		smp.setPaymentType(surcharge.getPaymentType().getName());
		smp.setStatus(surcharge.getStatus().getName());

		smp.setBankSurchargeAmountOffCommercial(bankSurchargeAmountOff);
		smp.setBankSurchargeAmountOnCommercial(surcharge.getBankSurchargeAmountCommercial());
		smp.setBankSurchargePercentageOffCommercial(bankSurchargePercentageOff);
		smp.setBankSurchargePercentageOnCommercial(surcharge.getBankSurchargePercentageCommercial());

		smp.setBankSurchargeAmountOffCustomer(bankSurchargeAmountOff);
		smp.setBankSurchargeAmountOnCustomer(surcharge.getBankSurchargeAmountCustomer());
		smp.setBankSurchargePercentageOffCustomer(bankSurchargePercentageOff);
		smp.setBankSurchargePercentageOnCustomer(surcharge.getBankSurchargePercentageCustomer());

		smp.setPayId(surcharge.getPayId());
		smp.setPaymentType(surcharge.getPaymentType().getName());
		smp.setAcquirerName(surcharge.getAcquirerName());
		smp.setAllowOnOff(allowOnOff);
		smp.setPaymentsRegion(surcharge.getPaymentsRegion());
		smp.setMerchantName(userDao.getMerchantNameByPayId(surcharge.getPayId()));

		return smp;
	}

	public List<SurchargeMappingPopulator> removeDuplicateFromList(List<SurchargeMappingPopulator> list) {
		int s = 0;
		List<SurchargeMappingPopulator> list2 = new ArrayList<SurchargeMappingPopulator>();
		for (SurchargeMappingPopulator us1 : list) {
			for (SurchargeMappingPopulator us2 : list2) {
				if (us1.getPayId().equals(us2.getPayId()) && us1.getPaymentType().equals(us2.getPaymentType())
						&& us1.getMopType().equals(us2.getMopType())
						&& us1.getAcquirerName().equals(us2.getAcquirerName())) {
					s = 1;
				} else {
					s = 0;
				}

			}
			if (s == 0) {
				list2.add(us1);
			}

		}
		return list2;
	}

	public List<PaymentOptions> getPaymentOptions(SessionMap<String, Object> sessionMap) {

		List<PaymentOptions> pendingPaymentOptionRequestList = new ArrayList<PaymentOptions>();
		List<PaymentOptions> pendingPaymentOptionDataList = new ArrayList<PaymentOptions>();
		
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_MERCHANT_MAPPING.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingPaymentOptionRequestList = paymentOptionsDao.getAllPendingPaymentOption();

			for (PaymentOptions paymentOptions : pendingPaymentOptionRequestList) {
				
				String mopType = paymentOptions.getMopTypeString();
				
				List<String> mopTypeStringList = new ArrayList<String>(Arrays.asList(mopType.split(",")));
				
				String mopTypeString = null;
				String paymentTypeString = null;

				for (String mapStrings : mopTypeStringList) {

					String[] tokens = mapStrings.split("-");

					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(tokens[0])) {
							paymentTypeString = paymentTypeString + "," + tokens[0];
						}
					} else {
						paymentTypeString = tokens[0];
					}

					switch (tokens[0]) {

					case "Credit Card":

						String mopCC = "CC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopCC;
						} else {
							mopTypeString = mopCC;
						}

						break;
					case "Debit Card":

						String mopDC = "DC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopDC;
						} else {
							mopTypeString = mopDC;
						}
						break;

					case "UPI":
						String mopUP = "UP-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopUP;
						} else {
							mopTypeString = mopUP;
						}

						break;

					case "Net Banking":
						String mopNb = "NB-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopNb;
						} else {
							mopTypeString = mopNb;
						}
						break;

					case "Wallet":
						String mopWl = "WL-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopWl;
						} else {
							mopTypeString = mopWl;
						}
						break;
					}

				}
				
				if(paymentOptions.isCashOnDelivery()){
					String cod="Cash on Delivery";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(cod)) {
							paymentTypeString = paymentTypeString + "," + cod;
						}
					} else {
						paymentTypeString = cod;
					}
				}
				if(paymentOptions.isUpiQr()){
					String upiQr="UPI QR";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(upiQr)) {
							paymentTypeString = paymentTypeString + "," + upiQr;
						}
					} else {
						paymentTypeString = upiQr;
					}
				}
				if(paymentOptions.isCrypto()){
					String crypto="Crypto";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(crypto)) {
							paymentTypeString = paymentTypeString + "," + crypto;
						}
					} else {
						paymentTypeString = crypto;
					}
				}
				if(paymentOptions.isRecurringPayment()){
					String recurringPayment="Recurring Payment";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(recurringPayment)) {
							paymentTypeString = paymentTypeString + "," + recurringPayment;
						}
					} else {
						paymentTypeString = recurringPayment;
					}
				}
				
				if(paymentOptions.isEmi()){
					String emi="EMI";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(emi)) {
							paymentTypeString = paymentTypeString + "," + emi;
						}
					} else {
						paymentTypeString = emi;
					}
				}
				
				if(paymentOptions.isExpressPay()){
					String expressPay="Express Pay";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(expressPay)) {
							paymentTypeString = paymentTypeString + "," + expressPay;
						}
					} else {
						paymentTypeString = expressPay;
					}
				}
				
				if(paymentOptions.isPrepaidCard()){
					String prepaidCard="Prepaid Card";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(prepaidCard)) {
							paymentTypeString = paymentTypeString + "," + prepaidCard;
						}
					} else {
						paymentTypeString = prepaidCard;
					}
				}
				
				if(paymentOptions.isDebitCardWithPin()){
					String debitCardWithPin="Debit Card With Pin";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(debitCardWithPin)) {
							paymentTypeString = paymentTypeString + "," + debitCardWithPin;
						}
					} else {
						paymentTypeString = debitCardWithPin;
					}
				}
				
				if(paymentOptions.isInternational()){
					String international="International";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(international)) {
							paymentTypeString = paymentTypeString + "," + international;
						}
					} else {
						paymentTypeString = international;
					}
				}

				paymentOptions.setMopTypeString(mopTypeString);
				paymentOptions.setPaymentTypeString(paymentTypeString);
				if(StringUtils.isNotBlank(paymentOptions.getSuperMerchantId()))
					paymentOptions.setSuperMerchantName(userDao.getBusinessNameByPayId(paymentOptions.getSuperMerchantId()));
				
				paymentOptions.setRequestedBy(userDao.getBusinessNameByEmailId(paymentOptions.getRequestedBy()));

				pendingPaymentOptionDataList.add(paymentOptions);
			}

		}
		return pendingPaymentOptionDataList;
	}
	
	public List<Object> getPaymentOptionsForPendingReport(SessionMap<String, Object> sessionMap, String status) {

		List<PaymentOptions> pendingPaymentOptionRequestList = new ArrayList<PaymentOptions>();
		List<Object> pendingPaymentOptionDataList = new ArrayList<Object>();
		
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_MERCHANT_MAPPING.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingPaymentOptionRequestList = paymentOptionsDao.getAllPendingPaymentOption(status);

			for (PaymentOptions paymentOptions : pendingPaymentOptionRequestList) {
				
				String mopType = paymentOptions.getMopTypeString();
				
				if(StringUtils.isNotBlank(paymentOptions.getSuperMerchantId()))
					paymentOptions.setSuperMerchantName(userDao.getBusinessNameByPayId(paymentOptions.getSuperMerchantId()));
				
				paymentOptions.setRequestedBy(userDao.getBusinessNameByEmailId(paymentOptions.getRequestedBy()));
				paymentOptions.setUpdateBy(userDao.getBusinessNameByEmailId(paymentOptions.getUpdateBy()));
				
				List<String> mopTypeStringList = new ArrayList<String>(Arrays.asList(mopType.split(",")));
				
				String mopTypeString = null;
				String paymentTypeString = null;

				for (String mapStrings : mopTypeStringList) {

					String[] tokens = mapStrings.split("-");

					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(tokens[0])) {
							paymentTypeString = paymentTypeString + "," + tokens[0];
						}
					} else {
						paymentTypeString = tokens[0];
					}

					switch (tokens[0]) {

					case "Credit Card":

						String mopCC = "CC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopCC;
						} else {
							mopTypeString = mopCC;
						}

						break;
					case "Debit Card":

						String mopDC = "DC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopDC;
						} else {
							mopTypeString = mopDC;
						}
						break;

					case "UPI":
						String mopUP = "UP-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopUP;
						} else {
							mopTypeString = mopUP;
						}

						break;

					case "Net Banking":
						String mopNb = "NB-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopNb;
						} else {
							mopTypeString = mopNb;
						}
						break;

					case "Wallet":
						String mopWl = "WL-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopWl;
						} else {
							mopTypeString = mopWl;
						}
						break;
					}

				}
				
				if(paymentOptions.isCashOnDelivery()){
					String cod="Cash on Delivery";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(cod)) {
							paymentTypeString = paymentTypeString + "," + cod;
						}
					} else {
						paymentTypeString = cod;
					}
				}
				if(paymentOptions.isUpiQr()){
					String upiQr="UPI QR";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(upiQr)) {
							paymentTypeString = paymentTypeString + "," + upiQr;
						}
					} else {
						paymentTypeString = upiQr;
					}
				}
				if(paymentOptions.isCrypto()){
					String crypto="Crypto";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(crypto)) {
							paymentTypeString = paymentTypeString + "," + crypto;
						}
					} else {
						paymentTypeString = crypto;
					}
				}
				if(paymentOptions.isRecurringPayment()){
					String recurringPayment="Recurring Payment";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(recurringPayment)) {
							paymentTypeString = paymentTypeString + "," + recurringPayment;
						}
					} else {
						paymentTypeString = recurringPayment;
					}
				}
				
				if(paymentOptions.isEmi()){
					String emi="EMI";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(emi)) {
							paymentTypeString = paymentTypeString + "," + emi;
						}
					} else {
						paymentTypeString = emi;
					}
				}
				
				if(paymentOptions.isExpressPay()){
					String expressPay="Express Pay";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(expressPay)) {
							paymentTypeString = paymentTypeString + "," + expressPay;
						}
					} else {
						paymentTypeString = expressPay;
					}
				}
				
				if(paymentOptions.isPrepaidCard()){
					String prepaidCard="Prepaid Card";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(prepaidCard)) {
							paymentTypeString = paymentTypeString + "," + prepaidCard;
						}
					} else {
						paymentTypeString = prepaidCard;
					}
				}
				
				if(paymentOptions.isDebitCardWithPin()){
					String debitCardWithPin="Debit Card With Pin";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(debitCardWithPin)) {
							paymentTypeString = paymentTypeString + "," + debitCardWithPin;
						}
					} else {
						paymentTypeString = debitCardWithPin;
					}
				}
				
				if(paymentOptions.isInternational()){
					String international="International";
					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(international)) {
							paymentTypeString = paymentTypeString + "," + international;
						}
					} else {
						paymentTypeString = international;
					}
				}

				paymentOptions.setMopTypeString(mopTypeString);
				paymentOptions.setPaymentTypeString(paymentTypeString);

				pendingPaymentOptionDataList.add(paymentOptions);
			}

		}
		return pendingPaymentOptionDataList;
	}
	

	public Map<String, List<MapList>> getTestData(SessionMap<String, Object> sessionMap) {
		Map<String, List<MapList>> pendingServiceTaxMap = new HashMap<String, List<MapList>>();

		List<PendingMappingRequest> pendingMappingRequestList = new ArrayList<PendingMappingRequest>();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.CREATE_MERCHANT_MAPPING.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingMappingRequestList = pendingMappingRequestDao.getPendingMappingRequest();

			for (PendingMappingRequest pendingrequest : pendingMappingRequestList) {

				List<MapList> pendingList = new ArrayList<MapList>();
				MapList mapList = new MapList();
				List<MerchantMopPopulator> mmpList = new ArrayList<MerchantMopPopulator>();
				List<MerchantCurrencyPopulator> mcpList = new ArrayList<MerchantCurrencyPopulator>();
				String txnType = null;
				String mopType = null;
				String paymentType = null;

				String merchantEmailId = pendingrequest.getMerchantEmailId();
				String acquirer = pendingrequest.getAcquirer();
				String accountCurrencySet = pendingrequest.getAccountCurrencySet();
				String mapString = pendingrequest.getMapString();
				String businessName = userDao.getBusinessNameByEmailId(merchantEmailId);
				String requestBy = pendingrequest.getRequestedBy();

				Gson gson = new Gson();
				AccountCurrency[] accountCurrencies = gson.fromJson(accountCurrencySet, AccountCurrency[].class);
				for (AccountCurrency accountCurrency : accountCurrencies) {

					MerchantCurrencyPopulator mcp = new MerchantCurrencyPopulator();
					mcp.setAcquirer(AcquirerType.getInstancefromCode(acquirer).getName());
					mcp.setBusinessType(merchantEmailId);
					mcp.setCurrency(accountCurrency.getCurrencyCode());
					mcp.setMerchantId(accountCurrency.getMerchantId());
					mcp.setPassword(accountCurrency.getPassword());
					mcp.setStatus(TDRStatus.PENDING);
					mcp.setTxnKey(accountCurrency.getTxnKey());
					mcp.setNon3ds(accountCurrency.isDirectTxn());

					mcpList.add(mcp);
				}

				List<String> mapStringlist = new ArrayList<String>(Arrays.asList(mapString.split(",")));

				for (String mapStrings : mapStringlist) {

					MerchantMopPopulator mmp = new MerchantMopPopulator();

					String[] tokens = mapStrings.split("-");

					switch (tokens[0]) {

					case "Credit Card":

						String txnTypeCC = TransactionType.getInstanceFromCode(tokens[2]).getName();
						String mopCC = MopType.getmopName(tokens[1]);
						boolean foundCCEntry = false;
						paymentType = tokens[0];
						mopType = tokens[1];
						txnType = tokens[2];

						if (mmpList.size() > 0) {
							for (MerchantMopPopulator m : mmpList) {
								if (m.getPaymentType().equalsIgnoreCase("Credit Card")
										&& m.getMopType().equalsIgnoreCase(mopCC)) {

									if (txnTypeCC.equalsIgnoreCase("AUTHORISE")) {
										m.setAuth(true);
									} else if (txnTypeCC.equalsIgnoreCase("SALE")) {
										m.setSale(true);
									}

									else if (txnTypeCC.equalsIgnoreCase("REFUND")) {
										m.setRefund(true);
									}

									foundCCEntry = true;
								}
							}
						}

						if (foundCCEntry) {
							break;
						}

						mmp.setPaymentType("Credit Card");
						mmp.setMopType(mopCC);

						if (txnTypeCC.equalsIgnoreCase("AUTHORISE")) {
							mmp.setAuth(true);
						} else if (txnTypeCC.equalsIgnoreCase("SALE")) {
							mmp.setSale(true);
						}

						else if (txnTypeCC.equalsIgnoreCase("REFUND")) {
							mmp.setRefund(true);
						}
						mmp.setStatus(TDRStatus.PENDING);
						mmpList.add(mmp);

						break;
					case "Debit Card":

						String txnTypeDC = TransactionType.getInstanceFromCode(tokens[2]).getName();
						String mopDC = MopType.getmopName(tokens[1]);
						boolean foundDCEntry = false;
						paymentType = tokens[0];
						mopType = tokens[1];
						txnType = tokens[2];

						if (mmpList.size() > 0) {
							for (MerchantMopPopulator m : mmpList) {
								if (m.getPaymentType().equalsIgnoreCase("Debit Card")
										&& m.getMopType().equalsIgnoreCase(mopDC)) {

									if (txnTypeDC.equalsIgnoreCase("AUTHORISE")) {
										m.setAuth(true);
									} else if (txnTypeDC.equalsIgnoreCase("SALE")) {
										m.setSale(true);
									}

									else if (txnTypeDC.equalsIgnoreCase("REFUND")) {
										m.setRefund(true);
									}
									foundDCEntry = true;
								}
							}
						}

						if (foundDCEntry) {
							break;
						}

						mmp.setPaymentType("Debit Card");
						mmp.setMopType(mopDC);

						if (txnTypeDC.equalsIgnoreCase("AUTHORISE")) {
							mmp.setAuth(true);
						} else if (txnTypeDC.equalsIgnoreCase("SALE")) {
							mmp.setSale(true);
						}

						else if (txnTypeDC.equalsIgnoreCase("REFUND")) {
							mmp.setRefund(true);
						}
						mmp.setStatus(TDRStatus.PENDING);
						mmpList.add(mmp);
						break;

					case "UPI":

						String txnTypeUPI = TransactionType.getInstanceFromCode(tokens[2]).getName();
						String mopUP = MopType.getmopName(tokens[1]);
						boolean foundUPIEntry = false;
						paymentType = tokens[0];
						mopType = tokens[1];
						txnType = tokens[2];

						if (mmpList.size() > 0) {
							for (MerchantMopPopulator m : mmpList) {
								if (m.getPaymentType().equalsIgnoreCase("UPI")
										&& m.getMopType().equalsIgnoreCase(mopUP)) {

									if (txnTypeUPI.equalsIgnoreCase("AUTHORISE")) {
										m.setAuth(true);
									} else if (txnTypeUPI.equalsIgnoreCase("SALE")) {
										m.setSale(true);
									}

									else if (txnTypeUPI.equalsIgnoreCase("REFUND")) {
										m.setRefund(true);
									}
									foundDCEntry = true;
								}
							}
						}

						if (foundUPIEntry) {
							break;
						}

						mmp.setPaymentType("UPI");
						mmp.setMopType(mopUP);

						if (txnTypeUPI.equalsIgnoreCase("AUTHORISE")) {
							mmp.setAuth(true);
						} else if (txnTypeUPI.equalsIgnoreCase("SALE")) {
							mmp.setSale(true);
						}

						else if (txnTypeUPI.equalsIgnoreCase("REFUND")) {
							mmp.setRefund(true);
						}
						mmp.setStatus(TDRStatus.PENDING);
						mmpList.add(mmp);
						break;

					case "Net Banking":
						paymentType = tokens[0];
						mopType = tokens[1];
						// txnType = tokens[2];
						mmp.setPaymentType("Net Banking");
						mmp.setStatus(TDRStatus.PENDING);
						mmp.setNbBank(MopType.getmopName(tokens[1]));
						mmpList.add(mmp);
						break;

					case "Wallet":
						String txnTypeWL = TransactionType.getInstanceFromCode(tokens[2]).getName();
						String mopWL = MopType.getmopName(tokens[1]);
						boolean foundWLEntry = false;
						paymentType = tokens[0];
						mopType = tokens[1];
						txnType = tokens[2];

						if (mmpList.size() > 0) {
							for (MerchantMopPopulator m : mmpList) {
								if (m.getPaymentType().equalsIgnoreCase("Wallet")
										&& m.getMopType().equalsIgnoreCase(mopWL)) {

									if (txnTypeWL.equalsIgnoreCase("AUTHORISE")) {
										m.setAuth(true);
									} else if (txnTypeWL.equalsIgnoreCase("SALE")) {
										m.setSale(true);
									}

									else if (txnTypeWL.equalsIgnoreCase("REFUND")) {
										m.setRefund(true);
									}
									foundDCEntry = true;
								}
							}
						}

						if (foundWLEntry) {
							break;
						}

						mmp.setPaymentType("Wallet");
						mmp.setMopType(mopWL);

						if (txnTypeWL.equalsIgnoreCase("AUTHORISE")) {
							mmp.setAuth(true);
						} else if (txnTypeWL.equalsIgnoreCase("SALE")) {
							mmp.setSale(true);
						}

						else if (txnTypeWL.equalsIgnoreCase("REFUND")) {
							mmp.setRefund(true);
						}
						mmp.setStatus(TDRStatus.PENDING);
						mmpList.add(mmp);
						break;

					}

				}

				mapList.setMcpList(mcpList);
				mapList.setMmpList(mmpList);
				pendingList.add(mapList);

				if (pendingList.size() > 0) {
					pendingServiceTaxMap
							.put(businessName + " --- " + AcquirerType.getInstancefromCode(acquirer).getName() + ","
									+ mapString/*
												 * ","+paymentType+","+mopType+
												 * ","+txnType
												 */, pendingList);
				}
			}
			return pendingServiceTaxMap;
		}
		return pendingServiceTaxMap;
	}
	
	public List<PendingMappingRequest> getMerchantMapping(SessionMap<String, Object> sessionMap) {

		List<PendingMappingRequest> pendingMappingRequestList = new ArrayList<PendingMappingRequest>();
		List<PendingMappingRequest> pendingMappingDataList = new ArrayList<PendingMappingRequest>();
		
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.MERCHANT_MAPPING.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingMappingRequestList = pendingMappingRequestDao.getPendingMappingRequest();

			for (PendingMappingRequest pendingrequest : pendingMappingRequestList) {
				

				String accountCurrencySet = pendingrequest.getAccountCurrencySet();
				String mapString = pendingrequest.getMapString();
				
				String businessName = userDao.getBusinessNameByEmailId(pendingrequest.getMerchantEmailId());

				pendingrequest.setAcquirer(AcquirerType.getInstancefromCode(pendingrequest.getAcquirer()).getName());
				pendingrequest.setBusinessName(businessName);
				
				pendingrequest.setRequestedBy(userDao.getBusinessNameByEmailId(pendingrequest.getRequestedBy()));
				
				Gson gson = new Gson();
				AccountCurrency[] accountCurrencies = gson.fromJson(accountCurrencySet, AccountCurrency[].class);
				for (AccountCurrency accountCurrency : accountCurrencies) {

					pendingrequest.setCurrency(accountCurrency.getCurrencyCode());
					pendingrequest.setMerchantId(accountCurrency.getMerchantId());
					pendingrequest.setPassword(accountCurrency.getPassword());
					pendingrequest.setTxnKey(accountCurrency.getTxnKey());
					pendingrequest.setAdf1(accountCurrency.getAdf1());
					pendingrequest.setAdf2(accountCurrency.getAdf2());
					pendingrequest.setAdf3(accountCurrency.getAdf3());
					pendingrequest.setAdf4(accountCurrency.getAdf4());
					pendingrequest.setAdf5(accountCurrency.getAdf5());
					pendingrequest.setAdf6(accountCurrency.getAdf6());
					pendingrequest.setAdf7(accountCurrency.getAdf7());
					pendingrequest.setAdf8(accountCurrency.getAdf8());
					pendingrequest.setAdf9(accountCurrency.getAdf9());
					pendingrequest.setAdf10(accountCurrency.getAdf10());
					pendingrequest.setAdf11(accountCurrency.getAdf11());

				}

				List<String> mapStringlist = new ArrayList<String>(Arrays.asList(mapString.split(",")));

				String mopTypeString = null;
				String paymentTypeString = null;

				for (String mapStrings : mapStringlist) {

					String[] tokens = mapStrings.split("-");

					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(tokens[0])) {
							paymentTypeString = paymentTypeString + "," + tokens[0];
						}
					} else {
						paymentTypeString = tokens[0];
					}

					switch (tokens[0]) {

					case "Credit Card":

						String mopCC = "CC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopCC;
						} else {
							mopTypeString = mopCC;
						}

						break;
					case "Debit Card":

						String mopDC = "DC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopDC;
						} else {
							mopTypeString = mopDC;
						}
						break;

					case "UPI":
						String mopUP = "UP-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopUP;
						} else {
							mopTypeString = mopUP;
						}

						break;

					case "Net Banking":
						String mopNb = "NB-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopNb;
						} else {
							mopTypeString = mopNb;
						}
						break;

					case "Wallet":
						String mopWl = "WL-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopWl;
						} else {
							mopTypeString = mopWl;
						}
						break;
					}

				}

				pendingrequest.setMopTypeString(mopTypeString);
				pendingrequest.setPaymentTypeString(paymentTypeString);

				pendingMappingDataList.add(pendingrequest);
			}

		}
		return pendingMappingDataList;
	}
	
	public List<Object> getMerchantMappingForPendingReport(SessionMap<String, Object> sessionMap, String status) {

		List<PendingMappingRequest> pendingMappingRequestList = new ArrayList<PendingMappingRequest>();
		List<Object> pendingMappingDataList = new ArrayList<Object>();
		
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		if (permissions.toString().contains(PermissionType.MERCHANT_MAPPING.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingMappingRequestList = pendingMappingRequestDao.getPendingMappingRequest(status);

			for (PendingMappingRequest pendingrequest : pendingMappingRequestList) {
				
				String accountCurrencySet = pendingrequest.getAccountCurrencySet();
				String mapString = pendingrequest.getMapString();
				
				String businessName = userDao.getBusinessNameByEmailId(pendingrequest.getMerchantEmailId());

				pendingrequest.setAcquirer(AcquirerType.getInstancefromCode(pendingrequest.getAcquirer()).getName());
				pendingrequest.setBusinessName(businessName);
				
				Gson gson = new Gson();
				AccountCurrency[] accountCurrencies = gson.fromJson(accountCurrencySet, AccountCurrency[].class);
				for (AccountCurrency accountCurrency : accountCurrencies) {

					pendingrequest.setCurrency(accountCurrency.getCurrencyCode());
					pendingrequest.setMerchantId(accountCurrency.getMerchantId());
					pendingrequest.setPassword(accountCurrency.getPassword());
					pendingrequest.setTxnKey(accountCurrency.getTxnKey());
					pendingrequest.setAdf1(accountCurrency.getAdf1());
					pendingrequest.setAdf2(accountCurrency.getAdf2());
					pendingrequest.setAdf3(accountCurrency.getAdf3());
					pendingrequest.setAdf4(accountCurrency.getAdf4());
					pendingrequest.setAdf5(accountCurrency.getAdf5());
					pendingrequest.setAdf6(accountCurrency.getAdf6());
					pendingrequest.setAdf7(accountCurrency.getAdf7());
					pendingrequest.setAdf8(accountCurrency.getAdf8());
					pendingrequest.setAdf9(accountCurrency.getAdf9());
					pendingrequest.setAdf10(accountCurrency.getAdf10());
					pendingrequest.setAdf11(accountCurrency.getAdf11());

				}

				List<String> mapStringlist = new ArrayList<String>(Arrays.asList(mapString.split(",")));

				String mopTypeString = null;
				String paymentTypeString = null;

				for (String mapStrings : mapStringlist) {

					String[] tokens = mapStrings.split("-");

					if (StringUtils.isNotBlank(paymentTypeString)) {
						if (!paymentTypeString.contains(tokens[0])) {
							paymentTypeString = paymentTypeString + "," + tokens[0];
						}
					} else {
						paymentTypeString = tokens[0];
					}

					switch (tokens[0]) {

					case "Credit Card":

						String mopCC = "CC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopCC;
						} else {
							mopTypeString = mopCC;
						}

						break;
					case "Debit Card":

						String mopDC = "DC-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopDC;
						} else {
							mopTypeString = mopDC;
						}
						break;

					case "UPI":
						String mopUP = "UP-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopUP;
						} else {
							mopTypeString = mopUP;
						}

						break;

					case "Net Banking":
						String mopNb = "NB-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopNb;
						} else {
							mopTypeString = mopNb;
						}
						break;

					case "Wallet":
						String mopWl = "WL-" + MopType.getmopName(tokens[1]);

						if (StringUtils.isNotBlank(mopTypeString)) {
							mopTypeString = mopTypeString + "," + mopWl;
						} else {
							mopTypeString = mopWl;
						}
						break;
					}

				}

				pendingrequest.setMopTypeString(mopTypeString);
				pendingrequest.setPaymentTypeString(paymentTypeString);

				pendingMappingDataList.add(pendingrequest);
			}

		}
		return pendingMappingDataList;
	}

	public Map<String, User> getPendingUserProfile(SessionMap<String, Object> sessionMap) {

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		Map<String, User> pendingUserProfileMap = new HashMap<String, User>();
		if (permissions.toString().contains(PermissionType.MERCHANT_EDIT/* VIEW_MERCHANT_SETUP */.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			List<PendingUserApproval> pendingList = pendingUserApprovalDao.getPendingUserProfileList();
			if (pendingList == null) {
				return pendingUserProfileMap;
			}

			for (PendingUserApproval obj : pendingList) {
				User newObj = new User();
				newObj.setModeType(obj.getModeType());
				newObj.setComments(obj.getComments());
				newObj.setWhiteListIpAddress(obj.getWhiteListIpAddress());
				newObj.setUserStatus(obj.getUserStatus());

				newObj.setBusinessName(obj.getBusinessName());
				newObj.setFirstName(obj.getFirstName());
				newObj.setLastName(obj.getLastName());
				newObj.setCompanyName(obj.getCompanyName());
				newObj.setWebsite(obj.getWebsite());
				newObj.setContactPerson(obj.getContactPerson());
				newObj.setEmailId(obj.getEmailId());
				newObj.setRegistrationDate(obj.getRegistrationDate());

				newObj.setMerchantType(obj.getMerchantType());
				newObj.setNoOfTransactions(obj.getNoOfTransactions());
				newObj.setAmountOfTransactions(obj.getAmountOfTransactions());
				newObj.setResellerId(obj.getResellerId());
				newObj.setProductDetail(obj.getProductDetail());

				newObj.setMobile(obj.getMobile());
				newObj.setTransactionSmsFlag(obj.isTransactionSmsFlag());
				newObj.setTelephoneNo(obj.getTelephoneNo());
				newObj.setFax(obj.getFax());
				newObj.setAddress(obj.getAddress());
				newObj.setCity(obj.getCity());
				newObj.setState(obj.getState());
				newObj.setCountry(obj.getCountry());
				newObj.setPostalCode(obj.getPostalCode());

				newObj.setBankName(obj.getBankName());
				newObj.setIfscCode(obj.getIfscCode());
				newObj.setAccHolderName(obj.getAccHolderName());
				newObj.setCurrency(obj.getCurrency());
				newObj.setBranchName(obj.getBranchName());
				newObj.setPanCard(obj.getPanCard());
				newObj.setAccountNo(obj.getAccountNo());

				newObj.setOrganisationType(obj.getOrganisationType());
				newObj.setMultiCurrency(obj.getMultiCurrency());
				newObj.setBusinessModel(obj.getBusinessModel());
				newObj.setOperationAddress(obj.getOperationAddress());
				newObj.setOperationState(obj.getOperationState());
				newObj.setOperationCity(obj.getOperationCity());
				newObj.setOperationPostalCode(obj.getOperationPostalCode());
				newObj.setDateOfEstablishment(obj.getDateOfEstablishment());

				newObj.setCin(obj.getCin());
				newObj.setPan(obj.getPan());
				newObj.setPanName(obj.getPanName());
				newObj.setTransactionEmailerFlag(obj.isTransactionEmailerFlag());
				newObj.setTransactionEmailId(obj.getTransactionEmailId());
//				newObj.setExpressPayFlag(obj.isExpressPayFlag());
				//newObj.setMerchantHostedFlag(obj.isMerchantHostedFlag());
				//newObj.setIframePaymentFlag(obj.isIframePaymentFlag());
//				newObj.setSurchargeFlag(obj.isSurchargeFlag());
				newObj.setTransactionAuthenticationEmailFlag(obj.isTransactionAuthenticationEmailFlag());
				newObj.setTransactionCustomerEmailFlag(obj.isTransactionCustomerEmailFlag());
				newObj.setRefundTransactionCustomerEmailFlag(obj.isRefundTransactionCustomerEmailFlag());
				newObj.setRefundTransactionMerchantEmailFlag(obj.isRefundTransactionMerchantEmailFlag());
//				newObj.setRetryTransactionCustomeFlag(obj.isRetryTransactionCustomeFlag());
//				newObj.setAttemptTrasacation(obj.getAttemptTrasacation());
				newObj.setExtraRefundLimit(obj.getExtraRefundLimit());
				newObj.setUpdateDate(obj.getUpdateDate());
				newObj.setDefaultCurrency(obj.getDefaultCurrency());
//				newObj.setMCC(obj.getMCC());
				newObj.setAmexSellerId(obj.getAmexSellerId());
				newObj.setDefaultLanguage(obj.getDefaultLanguage());
				newObj.setIndustryCategory(obj.getIndustryCategory());
				newObj.setIndustrySubCategory(obj.getIndustrySubCategory());
				newObj.setRequestedBy(obj.getRequestedBy());
				newObj.setPayId(obj.getPayId());
				newObj.setSettlementNamingConvention(obj.getSettlementNamingConvention());
				newObj.setRefundValidationNamingConvention(obj.getRefundValidationNamingConvention());

				String payId = String.valueOf(obj.getPayId());
				User userFromDB = userDao.findPayId(payId);
				String merchantName = userFromDB.getBusinessName();

				pendingUserProfileMap.put(merchantName, userFromDB);
				pendingUserProfileMap.put(merchantName, newObj);
			}

			/*
			 * if (pendingUserProfileMap.size() == 0){ String testUsr = ""; User
			 * usr = new User(); pendingUserProfileMap.put(testUsr, usr); }
			 */
			return pendingUserProfileMap;
		}
		return pendingUserProfileMap;
	}

	public Map<String, User> getPendingResellerMapping(SessionMap<String, Object> sessionMap) {

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		Map<String, User> pendingUserProfileMap = new HashMap<String, User>();
		if (permissions.toString().contains(PermissionType.CREATE_RESELLER_MAPPING.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			List<PendingResellerMappingApproval> pendingList = pendingResellerMappingDao
					.getPendingResellerMappingList();

			for (PendingResellerMappingApproval obj : pendingList) {
				User newObj = new User();
				newObj.setResellerId(obj.getResellerId());
				newObj.setEmailId(obj.getMerchantEmailId());
				newObj.setRequestedBy(obj.getRequestedBy());
				String payId = String.valueOf(obj.getMerchantPayId());
				User userFromDB = userDao.findPayId(payId);
				String merchantName = userFromDB.getBusinessName();

				pendingUserProfileMap.put(merchantName, userFromDB);
				pendingUserProfileMap.put(merchantName, newObj);
			}

			/*
			 * if (pendingUserProfileMap.size() == 0){ String testUsr = ""; User
			 * usr = new User(); pendingUserProfileMap.put(testUsr, usr); }
			 */
			return pendingUserProfileMap;
		}
		return pendingUserProfileMap;
	}

	public Map<String, List<RouterConfiguration>> getPendingSmartRouterData(SessionMap<String, Object> sessionMap) {

		String identifier = "";
		String pendingIdentifier;
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		Map<String, List<RouterConfiguration>> pendingRouterConfigMap = new HashMap<String, List<RouterConfiguration>>();

		if (permissions.toString().contains(PermissionType.SMART_ROUTER.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			List<RouterConfiguration> pendingRouterConfigList = routerConfigurationDao.getPendingRCList();

			if (pendingRouterConfigList.size() == 0) {
				return pendingRouterConfigMap;
			}

			for (RouterConfiguration routerIdentifier : pendingRouterConfigList) {
				pendingIdentifier = routerIdentifier.getIdentifier();

				if (pendingRouterConfigMap.containsKey(pendingIdentifier)) {
					continue;
				}
				List<RouterConfiguration> routerConfigList = new ArrayList<RouterConfiguration>();
				for (RouterConfiguration router : pendingRouterConfigList) {

					if (pendingIdentifier.equalsIgnoreCase(router.getIdentifier())) {

						RouterConfiguration newObj = new RouterConfiguration();

						newObj.setAcquirer(router.getAcquirer());
						newObj.setAllowedFailureCount(router.getAllowedFailureCount());
						newObj.setAlwaysOn(router.isAlwaysOn());
						newObj.setIdentifier(router.getIdentifier());
						newObj.setLoadPercentage(router.getLoadPercentage());
						newObj.setMaxAmount(router.getMaxAmount());
						newObj.setMinAmount(router.getMinAmount());
						newObj.setMode(router.getMode());
						newObj.setMopType(router.getMopType());
						newObj.setOnUsoffUsName(router.getOnUsoffUsName());
						newObj.setMerchant(userDao.getBusinessNameByPayId(router.getMerchant()));
						newObj.setPaymentType(router.getPaymentType());
						newObj.setPriority(router.getPriority());
						newObj.setRequestedBy(router.getRequestedBy());
						newObj.setRetryMinutes(router.getRetryMinutes());
						newObj.setStatus(router.getStatus());
						newObj.setTransactionType(router.getTransactionType());
						newObj.setUpdatedBy(router.getUpdatedBy());
						newObj.setUpdatedDate(router.getUpdatedDate());
						newObj.setCurrentlyActive(router.isCurrentlyActive());
						newObj.setStatusName(router.getStatusName());
						identifier = router.getIdentifier();

						routerConfigList.add(newObj);
					}
				}

				pendingRouterConfigMap.put(identifier, routerConfigList);
			}
		}
		return pendingRouterConfigMap;
	}

	public Map<String, PendingBulkUserRequest> getPendingBulkUser(SessionMap<String, Object> sessionMap) {

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		Map<String, PendingBulkUserRequest> pendingBulkUserMap = new HashMap<String, PendingBulkUserRequest>();

		if (permissions.toString().contains(PermissionType.CREATE_BULK_USER.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			List<PendingBulkUserRequest> pendingList = pendingBulkUserDao.getPendingBulkUserList();

			for (PendingBulkUserRequest obj : pendingList) {
				PendingBulkUserRequest newObj = new PendingBulkUserRequest();
				newObj.setBusinessName(obj.getBusinessName());
				newObj.setEmailId(obj.getEmailId());
				newObj.setRequestedBy(obj.getRequestedBy());
				newObj.setMobileNumber(obj.getMobileNumber());
				newObj.setIndustryCategory(obj.getIndustryCategory());
				newObj.setIndustrySubCategory(obj.getIndustrySubCategory());
				newObj.setCreatedDate(obj.getCreatedDate());

				String mobile = obj.getMobileNumber();
				pendingBulkUserMap.put(mobile, newObj);
			}

			return pendingBulkUserMap;
		}
		return pendingBulkUserMap;
	}

	public List<PendingBulkCharges> getPendingBulkCharges(SessionMap<String, Object> sessionMap) {
	
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		List<PendingBulkCharges> pendingBulkChargesList = new ArrayList<PendingBulkCharges>();
		List<PendingBulkCharges> filteredPendingBulkChargesList = new ArrayList<PendingBulkCharges>();

		if (permissions.toString().contains(PermissionType.CREATE_BULK_USER.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingBulkChargesList = chargingDetailsDao.getPendingBulkChargingDetailList();
			
			
			for(PendingBulkCharges pendingBulkCharges : pendingBulkChargesList){
				pendingBulkCharges.setBusinessName(userDao.getBusinessNameByPayId(pendingBulkCharges.getPayId()));
				pendingBulkCharges.setRequestedBy(userDao.getBusinessNameByEmailId(pendingBulkCharges.getRequestedBy()));
				filteredPendingBulkChargesList.add(pendingBulkCharges);
			}

		}
		return filteredPendingBulkChargesList;
	}

	public List<Object> getBulkChargesForPendingReport(SessionMap<String, Object> sessionMap, String Status) {
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User user = (User) userObject;

		List<PendingBulkCharges> pendingBulkCharges = new ArrayList<PendingBulkCharges>();
		List<Object> pendingBulkChargesObjectList = new ArrayList<Object>();
	
		if (permissions.toString().contains(PermissionType.CREATE_BULK_USER.getPermission())
				|| user.getUserType().equals(UserType.ADMIN)) {

			pendingBulkCharges = chargingDetailsDao.getPendingBulkChargingDetailList(Status);
			for(PendingBulkCharges pbc:pendingBulkCharges){
				
				pbc.setBusinessName(userDao.getBusinessNameByPayId(pbc.getPayId()));
				pbc.setRequestedBy(userDao.getBusinessNameByEmailId(pbc.getRequestedBy()));
				pbc.setUpdateBy(userDao.getBusinessNameByEmailId(pbc.getUpdateBy()));
				pendingBulkChargesObjectList.add(pbc);
			}

		}
		return pendingBulkChargesObjectList;
	}
	
}
