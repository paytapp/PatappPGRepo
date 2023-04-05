package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MerchantAcquirerPropertiesDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.MerchantAcquirerProperties;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.crm.action.SurchargePlatformAction;

@Service
public class SurchargeAcquirerDetailsFactory {

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MerchantAcquirerPropertiesDao merchantAcquirerPropertiesDao;

	private static Logger logger = LoggerFactory.getLogger(SurchargeAcquirerDetailsFactory.class.getName());

	public Map<String, List<Surcharge>> getSurchargeAcquirerDetails(String payId, String paymentTypeName) {

		BigDecimal surchargeAmountDomestic = BigDecimal.ZERO;
		BigDecimal surchargePercentageDomestic = BigDecimal.ZERO;
		BigDecimal minTransactionAmountDomestic = BigDecimal.ZERO;

		BigDecimal surchargeAmountInternational = BigDecimal.ZERO;
		BigDecimal surchargePercentageInternational = BigDecimal.ZERO;
		BigDecimal minTransactionAmountInternational = BigDecimal.ZERO;

		SurchargeDetails surchargeDetailDomestic = null;
		SurchargeDetails surchargeDetailInternational = null;
		User user = null;
		surchargeDetailDomestic = surchargeDetailsDao.findDetailsByRegion(payId, paymentTypeName,
				AccountCurrencyRegion.DOMESTIC.toString());
		surchargeDetailInternational = surchargeDetailsDao.findDetailsByRegion(payId, paymentTypeName,
				AccountCurrencyRegion.INTERNATIONAL.toString());

		user = userDao.findPayId(payId);

		if (surchargeDetailDomestic != null) {

			surchargeAmountDomestic = surchargeDetailDomestic.getSurchargeAmount();
			surchargePercentageDomestic = surchargeDetailDomestic.getSurchargePercentage();
		}

		if (surchargeDetailInternational != null) {

			surchargeAmountInternational = surchargeDetailInternational.getSurchargeAmount();
			surchargePercentageInternational = surchargeDetailInternational.getSurchargePercentage();
		}

		List<Surcharge> acquirerDetails = new ArrayList<Surcharge>();

		acquirerDetails = surchargeDao.findSurchargeListByPayid(payId, paymentTypeName);

		Map<String, List<Surcharge>> detailsMap = new HashMap<String, List<Surcharge>>();

		for (AcquirerType acquirerType : AcquirerType.values()) {
			List<Surcharge> surchargeDetailsList = new ArrayList<Surcharge>();
			String acquirerName = acquirerType.getName();

			MerchantAcquirerProperties merchantAcquirerProperties = merchantAcquirerPropertiesDao
					.getMerchantAcquirerProperties(payId, acquirerType.getCode());

			for (Surcharge acquirerDetail : acquirerDetails) {

				if (acquirerDetail.getAcquirerName().equals(acquirerName)) {

					if (acquirerDetail.getPaymentsRegion().equals(AccountCurrencyRegion.DOMESTIC)) {

						if (merchantAcquirerProperties != null && (merchantAcquirerProperties.getPaymentsRegion()
								.equals(AccountCurrencyRegion.DOMESTIC)
								|| merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL))) {

							logger.info("Merchant Acquirer Properties Found for acquirer " + acquirerType.getName()
									+ "  for DOMESTIC Region");
							acquirerDetail.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
							acquirerDetail.setPgSurchargeAmountCommercial(surchargeAmountDomestic
									.subtract(acquirerDetail.getBankSurchargeAmountCommercial()));

							acquirerDetail.setPgSurchargeAmountCustomer(
									surchargeAmountDomestic.subtract(acquirerDetail.getBankSurchargeAmountCustomer()));

							acquirerDetail.setPgSurchargePercentageCommercial(surchargePercentageDomestic
									.subtract(acquirerDetail.getBankSurchargePercentageCommercial()));

							acquirerDetail.setPgSurchargePercentageCustomer(surchargePercentageDomestic
									.subtract(acquirerDetail.getBankSurchargePercentageCustomer()));

							acquirerDetail.setMerchantIndustryType(user.getIndustryCategory());
							acquirerDetail.setMerchantSurchargeAmountCommercial(surchargeAmountDomestic);
							acquirerDetail.setMerchantSurchargeAmountCustomer(surchargeAmountDomestic);

							acquirerDetail.setMerchantSurchargePercentageCommercial(surchargePercentageDomestic);
							acquirerDetail.setMerchantSurchargePercentageCustomer(surchargePercentageDomestic);

							if (acquirerDetail.getOnOff().equalsIgnoreCase("2")) {
								acquirerDetail.setOnOff("OFF US");
							} else if (acquirerDetail.getOnOff().equalsIgnoreCase("1")) {
								acquirerDetail.setOnOff("ON US");
							} else {
								acquirerDetail.setOnOff("NONE");
							}

							surchargeDetailsList.add(acquirerDetail);
						}

						else {

							logger.info("Merchant Acquirer Properties  Not  Found for acquirer "
									+ acquirerType.getName() + "  for DOMESTIC Region");
						}

					} else {

						if (merchantAcquirerProperties != null && (merchantAcquirerProperties.getPaymentsRegion()
								.equals(AccountCurrencyRegion.INTERNATIONAL)
								|| merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL))) {

							logger.info("Merchant Acquirer Properties Found for acquirer " + acquirerType.getName()
									+ "  for INTERNATIONAL Region");

							acquirerDetail.setPaymentsRegion(AccountCurrencyRegion.INTERNATIONAL);
							acquirerDetail.setPgSurchargeAmountCommercial(surchargeAmountInternational
									.subtract(acquirerDetail.getBankSurchargeAmountCommercial()));

							acquirerDetail.setPgSurchargeAmountCustomer(surchargeAmountInternational
									.subtract(acquirerDetail.getBankSurchargeAmountCustomer()));

							acquirerDetail.setPgSurchargePercentageCommercial(surchargeAmountInternational
									.subtract(acquirerDetail.getBankSurchargePercentageCommercial()));

							acquirerDetail.setPgSurchargePercentageCustomer(surchargeAmountInternational
									.subtract(acquirerDetail.getBankSurchargePercentageCustomer()));

							acquirerDetail.setMerchantIndustryType(user.getIndustryCategory());
							acquirerDetail.setMerchantSurchargeAmountCommercial(surchargeAmountInternational);
							acquirerDetail.setMerchantSurchargeAmountCustomer(surchargeAmountInternational);

							acquirerDetail.setMerchantSurchargePercentageCommercial(surchargeAmountInternational);
							acquirerDetail.setMerchantSurchargePercentageCustomer(surchargeAmountInternational);

							if (acquirerDetail.getOnOff().equalsIgnoreCase("2")) {
								acquirerDetail.setOnOff("OFF US");
							} else if (acquirerDetail.getOnOff().equalsIgnoreCase("1")) {
								acquirerDetail.setOnOff("ON US");
							} else {
								acquirerDetail.setOnOff("NONE");
							}

							surchargeDetailsList.add(acquirerDetail);

						}

						else {

							logger.info("Merchant Acquirer Properties  Not  Found for acquirer "
									+ acquirerType.getName() + "  for INTERNATIONAL Region");

						}

					}

				}
			}
			if (surchargeDetailsList.size() != 0) {
				detailsMap.put(acquirerName, surchargeDetailsList);
			}
		}

		return detailsMap;
	}

}
