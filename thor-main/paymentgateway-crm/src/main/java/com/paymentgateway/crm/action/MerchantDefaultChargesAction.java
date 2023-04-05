package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.dao.RatesDefaultDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.RatesDefault;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.onUsOffUs;

public class MerchantDefaultChargesAction extends AbstractSecureAction {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private RatesDefaultDao ratesDefaultDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantDefaultChargesAction.class.getName());
	private static final long serialVersionUID = -6879974923614009981L;

	private String businessType;
	private String paymentType;
	private String paymentRegion;
	private String onOff;
	private String slab;
	private boolean showMerchant;
	private boolean showSaveButton;
	private String allDetails;
	private String response;

	private Map<String, List<RatesDefault>> bulkDataMap = new HashMap<String, List<RatesDefault>>();
	private Map<String, String> industryCategoryList = new TreeMap<String, String>();

	public String execute() {

		try {

			Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryCategoryList.putAll(industryCategoryLinkedMap);

			onUsOffUs acquiringMode = onUsOffUs.OFF_US;
			PaymentType payType = PaymentType.getInstanceUsingCode(paymentType);
			AccountCurrencyRegion acr = AccountCurrencyRegion.DOMESTIC;

			if (!payType.equals(PaymentType.CREDIT_CARD) && !payType.equals(PaymentType.DEBIT_CARD)
					&& !payType.equals(PaymentType.EMI_CC) && !payType.equals(PaymentType.EMI_DC)) {
				paymentRegion = AccountCurrencyRegion.DOMESTIC.toString();
				onOff = onUsOffUs.OFF_US.toString();
			}
			List<String> slabIdList = new ArrayList<String>();

			for (String slab : slab.replace(" ", "").split(",")) {

				if (slab.equalsIgnoreCase("0.01-1000.00")) {
					slabIdList.add("01");
				} else if (slab.equalsIgnoreCase("1000.01-2000.00")) {
					slabIdList.add("02");
				} else if (slab.equalsIgnoreCase("2000.01-1000000")) {
					slabIdList.add("03");
				}
			}

			if (onOff.equalsIgnoreCase(onUsOffUs.ON_US.toString())) {
				acquiringMode = onUsOffUs.ON_US;
			}

			if (paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
				acr = AccountCurrencyRegion.INTERNATIONAL;
			}

			String mopString = "";

			if (payType.equals(PaymentType.CREDIT_CARD)) {
				mopString = propertiesManager.propertiesMap.get("CCMOP");
			} else if (payType.equals(PaymentType.DEBIT_CARD)) {
				mopString = propertiesManager.propertiesMap.get("DCMOP");
			} else if (payType.equals(PaymentType.UPI)) {
				mopString = propertiesManager.propertiesMap.get("UPI");
			} else if (payType.equals(PaymentType.WALLET)) {
				mopString = propertiesManager.propertiesMap.get("WALLET");
			} else if (payType.equals(PaymentType.NET_BANKING)) {
				mopString = propertiesManager.propertiesMap.get("NBMOP");
			} else if (payType.equals(PaymentType.COD)) {
				mopString = propertiesManager.propertiesMap.get("COD");
			} else if (payType.equals(PaymentType.EMI_CC)) {
				mopString = propertiesManager.propertiesMap.get("EMCC");
			} else if (payType.equals(PaymentType.EMI_DC)) {
				mopString = propertiesManager.propertiesMap.get("EMDC");
			}

			for (String mopdc : mopString.split(",")) {

				List<RatesDefault> ratesDefaultList = new ArrayList<RatesDefault>();
				MopType mopType = MopType.getmop(mopdc);

				if (mopType == null) {
					continue;
				}
				List<CardHolderType> cardHolderTypeList = new ArrayList<CardHolderType>();

				if (payType.equals(PaymentType.CREDIT_CARD) || payType.equals(PaymentType.DEBIT_CARD)
						|| payType.equals(PaymentType.EMI_CC) || payType.equals(PaymentType.EMI_DC)) {

					if (acr == AccountCurrencyRegion.INTERNATIONAL) {
						cardHolderTypeList.add(CardHolderType.COMMERCIAL);

					} else {

						cardHolderTypeList.add(CardHolderType.CONSUMER);
						cardHolderTypeList.add(CardHolderType.COMMERCIAL);
						cardHolderTypeList.add(CardHolderType.PREMIUM);
					}
				} else {
					cardHolderTypeList.add(CardHolderType.CONSUMER);
					acr = AccountCurrencyRegion.DOMESTIC;
				}

				for (CardHolderType cardType : cardHolderTypeList) {

					for (String slabId : slabIdList) {
						RatesDefault ratesDef = ratesDefaultDao.findMerchantDefaultRates(payType, mopType, acr,
								acquiringMode, slabId, industryCategoryList.get(businessType), UserType.MERCHANT, cardType);

						if (ratesDef == null) {

							RatesDefault ratesDefault = new RatesDefault();
							ratesDefault.setPaymentType(payType);
							ratesDefault.setPaymentTypeName(payType.getName());
							ratesDefault.setMopType(mopType);
							ratesDefault.setPaymentsRegion(acr);
							ratesDefault.setPaymentRegionName(acr.toString());
							ratesDefault.setAcquiringMode(acquiringMode);
							ratesDefault.setAcquiringModeName(acquiringMode.getCode());
							ratesDefault.setMerchantTdr(BigDecimal.ZERO.setScale(2));
							ratesDefault.setMerchantSuf(BigDecimal.ZERO.setScale(2));
							ratesDefault.setMerchantMaxCharge(BigDecimal.ZERO.setScale(2));
							ratesDefault.setIndustryCategory(businessType);
							ratesDefault.setStatus(TDRStatus.INACTIVE);
							ratesDefault.setAllowFc(false);
							ratesDefault.setCardHolderType(cardType);
							ratesDefault.setSlab(slabId);
							if (slabId.equalsIgnoreCase("01")) {
								ratesDefault.setSlabDef("0.01-1000.00");
							} else if (slabId.equalsIgnoreCase("02")) {
								ratesDefault.setSlabDef("1000.01-2000.00");
							} else {
								ratesDefault.setSlabDef("2000.01-1000000");
							}

							ratesDefaultList.add(ratesDefault);
						} else {

							ratesDef.setPaymentTypeName(payType.getName());
							ratesDef.setPaymentRegionName(acr.toString());
							ratesDef.setAcquiringModeName(acquiringMode.getCode());
							
							if (ratesDef.getSlab().equalsIgnoreCase("01")) {
								ratesDef.setSlabDef("0.01-1000.00");
							} else if (ratesDef.getSlab().equalsIgnoreCase("02")) {
								ratesDef.setSlabDef("1000.01-2000.00");
							} else {
								ratesDef.setSlabDef("2000.01-1000000");
							}
							
							ratesDefaultList.add(ratesDef);
						}

					}

				}

				bulkDataMap.put(mopType.getName(), ratesDefaultList);
			}

		}

		catch (Exception e) {

			logger.error("Exception in generating list for merchant default rates", e);
		}

		return SUCCESS;
	}

	public String update() {

		if (StringUtils.isBlank(allDetails)) {
			setResponse("Unable to update default rates for Merchant!");
			return SUCCESS;
		}

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		try {

			String detailsArr[] = allDetails.split(";");

			for (String detailsRow : detailsArr) {

				String detailsCells[] = detailsRow.split(",");

				PaymentType paymentType = PaymentType.getInstance(detailsCells[2]);
				MopType mopType = MopType.getInstance(detailsCells[9]);
				AccountCurrencyRegion paymentsRegion = AccountCurrencyRegion.DOMESTIC;
				if (detailsCells[3].equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
					paymentsRegion = AccountCurrencyRegion.INTERNATIONAL;
				}

				onUsOffUs acquiringMode = onUsOffUs.OFF_US;
				if (detailsCells[4].equalsIgnoreCase("On Us")) {
					acquiringMode = onUsOffUs.ON_US;
				}

				String slabId = null;
				if (detailsCells[1].split("-")[1].equalsIgnoreCase("1000.00")) {
					slabId = "01";
				}

				else if (detailsCells[1].split("-")[1].equalsIgnoreCase("2000.00")) {
					slabId = "02";
				} else {
					slabId = "03";
				}
				UserType userType = UserType.MERCHANT;
				CardHolderType cardHolderType = null;

				if (detailsCells[0].equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
					cardHolderType = CardHolderType.CONSUMER;
				}

				else if (detailsCells[0].equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
					cardHolderType = CardHolderType.COMMERCIAL;
				} else {
					cardHolderType = CardHolderType.PREMIUM;
				}

				RatesDefault rdFromDb = ratesDefaultDao.findMerchantDefaultRates( paymentType, mopType, paymentsRegion,
						acquiringMode, slabId, businessType, userType, cardHolderType);

				if (rdFromDb == null) {

					RatesDefault rd = new RatesDefault();
					rd.setStatus(TDRStatus.ACTIVE);
					rd.setMerchantTdr(new BigDecimal(detailsCells[5]));
					rd.setMerchantSuf(new BigDecimal(detailsCells[6]));
					rd.setMerchantMaxCharge(new BigDecimal(detailsCells[7]));
					if (detailsCells[7].equalsIgnoreCase("true")) {
						rd.setAllowFc(true);
					} else {
						rd.setAllowFc(false);
					}

					rd.setAcquiringMode(acquiringMode);
					rd.setCardHolderType(cardHolderType);
					rd.setCreatedDate(new Date());
					rd.setIndustryCategory(businessType);
					rd.setMopType(mopType);
					rd.setPaymentsRegion(paymentsRegion);
					rd.setPaymentType(paymentType);

					rd.setSlab(slabId);
					rd.setUserType(userType);
					rd.setUpdatedDate(new Date());

					rd.setProcessedBy(sessionUser.getEmailId());
					rd.setRequestedBy(sessionUser.getEmailId());

					ratesDefaultDao.create(rd);
					
				} else {

					// Deactivate old rates

					try {
						Session session = null;
						session = HibernateSessionProvider.getSession();
						Transaction txn = session.beginTransaction();
						Long id = rdFromDb.getId();
						session.load(rdFromDb, rdFromDb.getId());
						RatesDefault rdFromDbToUpdate = (RatesDefault) session.get(RatesDefault.class, id);

						rdFromDbToUpdate.setStatus(TDRStatus.INACTIVE);
						rdFromDbToUpdate.setUpdatedDate(new Date());
						rdFromDbToUpdate.setProcessedBy(sessionUser.getEmailId());

						session.update(rdFromDbToUpdate);
						txn.commit();
						session.close();
					} catch (HibernateException e) {
						logger.error("Merchant Default rates Edit Failed " , e);
						return ERROR;
					}

					// Save new rates
					
					RatesDefault rd = new RatesDefault();
					rd.setStatus(TDRStatus.ACTIVE);
					rd.setMerchantTdr(new BigDecimal(detailsCells[5]));
					rd.setMerchantSuf(new BigDecimal(detailsCells[6]));
					rd.setMerchantMaxCharge(new BigDecimal(detailsCells[7]));
					if (detailsCells[7].equalsIgnoreCase("true")) {
						rd.setAllowFc(true);
					} else {
						rd.setAllowFc(false);
					}

					rd.setAcquiringMode(acquiringMode);
					rd.setCardHolderType(cardHolderType);
					rd.setCreatedDate(new Date());
					rd.setIndustryCategory(businessType);
					rd.setMopType(mopType);
					rd.setPaymentsRegion(paymentsRegion);
					rd.setPaymentType(paymentType);

					rd.setSlab(slabId);
					rd.setUserType(userType);
					rd.setUpdatedDate(new Date());

					rd.setProcessedBy(sessionUser.getEmailId());
					rd.setRequestedBy(sessionUser.getEmailId());

					ratesDefaultDao.create(rd);
				}

			}

			setResponse("Merchant Default Rates update successfully.");
		}

		catch (Exception e) {
			logger.error("Exception while setting merchant default rates", e);
			setResponse("Error While Updating Merchant Default Rates");
			return ERROR;
		}

		return SUCCESS;
	}

	public String display() {
		return INPUT;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
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

	public String getOnOff() {
		return onOff;
	}

	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}

	public Map<String, List<RatesDefault>> getBulkDataMap() {
		return bulkDataMap;
	}

	public void setBulkDataMap(Map<String, List<RatesDefault>> bulkDataMap) {
		this.bulkDataMap = bulkDataMap;
	}

	public boolean isShowMerchant() {
		return showMerchant;
	}

	public void setShowMerchant(boolean showMerchant) {
		this.showMerchant = showMerchant;
	}

	public boolean isShowSaveButton() {
		return showSaveButton;
	}

	public void setShowSaveButton(boolean showSaveButton) {
		this.showSaveButton = showSaveButton;
	}

	public String getSlab() {
		return slab;
	}

	public void setSlab(String slab) {
		this.slab = slab;
	}

	public String getAllDetails() {
		return allDetails;
	}

	public void setAllDetails(String allDetails) {
		this.allDetails = allDetails;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Map<String, String> getIndustryCategoryList() {
		return industryCategoryList;
	}

	public void setIndustryCategoryList(Map<String, String> industryCategoryList) {
		this.industryCategoryList = industryCategoryList;
	}

}
