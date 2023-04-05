package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MerchantAcquirerPropertiesDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.MerchantAcquirerProperties;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.SurchargeMappingPopulator;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;

@Service
public class SurchargeMappingDetailsFactory {

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private MerchantAcquirerPropertiesDao merchantAcquirerPropertiesDao;
	
	private static Logger logger = LoggerFactory.getLogger(SurchargeMappingDetailsFactory.class.getName());

	public Map<String, List<SurchargeMappingPopulator>> getSurchargeAcquirerDetails(String payId,
			PaymentType paymentType) {

		User user = null;
		user = userDao.findPayId(payId);
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		chargingDetailsList = chargingDetailsDao.getAllActiveSaleChargingDetails(payId, paymentType);

		List<SurchargeMappingPopulator> acquirerDetails1 = new ArrayList<SurchargeMappingPopulator>();

		for (ChargingDetails chargingDetail : chargingDetailsList) {

			logger.info("Charging details , chargingDetail.getPayId() = "+chargingDetail.getPayId());
			logger.info("Charging details , chargingDetail.getAcquirerName() = "+chargingDetail.getAcquirerName());
			logger.info("Charging details , chargingDetail.getPaymentType() = "+chargingDetail.getPaymentType());
			logger.info("Charging details , chargingDetail.getMopType() = "+chargingDetail.getMopType());
			
			// Initialize surcharge with Zero value
			SurchargeMappingPopulator surchargeMappingPopulator = new SurchargeMappingPopulator();
			surchargeMappingPopulator.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
			surchargeMappingPopulator.setBankSurchargeAmountOnCommercial(BigDecimal.ZERO);
			surchargeMappingPopulator.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
			surchargeMappingPopulator.setBankSurchargePercentageOnCommercial(BigDecimal.ZERO);

			surchargeMappingPopulator.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
			surchargeMappingPopulator.setBankSurchargeAmountOnCustomer(BigDecimal.ZERO);
			surchargeMappingPopulator.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
			surchargeMappingPopulator.setBankSurchargePercentageOnCustomer(BigDecimal.ZERO);

			// Dummy surcharge for initial display on page when ALL Region selected from mapping
			SurchargeMappingPopulator surchargeMappingPopulator2 = new SurchargeMappingPopulator();

			// Check if Acquire is mapped to merchant for showing on page
			if (surchargeDao.isChargingDetailsMappedWithSurcharge(chargingDetail.getPayId(),
					chargingDetail.getAcquirerName(), chargingDetail.getMopType(), chargingDetail.getPaymentType())) {

				List<Surcharge> surchargeList = surchargeDao.findDetailsOnUsOffUs(chargingDetail.getPayId(),
						chargingDetail.getAcquirerName(), chargingDetail.getMopType(), chargingDetail.getPaymentType());
				
				

				MerchantAcquirerProperties merchantAcquirerProperties = new MerchantAcquirerProperties();
				merchantAcquirerProperties = merchantAcquirerPropertiesDao.getMerchantAcquirerPropertiesByName(payId,
						chargingDetail.getAcquirerName());
				
				
				List<Surcharge> surchargeListForRegion = new ArrayList<Surcharge>();
				
				for (Surcharge surcharge : surchargeList) {
					
					if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL)) {
						
						surchargeListForRegion.add(surcharge);
					}
					
					else if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.DOMESTIC)) {
						
						if (surcharge.getPaymentsRegion().equals(AccountCurrencyRegion.DOMESTIC)) {
							
							surchargeListForRegion.add(surcharge);
						}
					}
					
					else if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.INTERNATIONAL)) {
						
						if (surcharge.getPaymentsRegion().equals(AccountCurrencyRegion.INTERNATIONAL)) {
							
							surchargeListForRegion.add(surcharge);
							
						}
					}
				}

				// Only one surcharge found without onus off config , for any one region
				if (surchargeListForRegion.size() == 1) {

					for (Surcharge surcharge : surchargeListForRegion) {

						surchargeMappingPopulator.setAcquirerName(surcharge.getAcquirerName());
						surchargeMappingPopulator.setMopType(surcharge.getMopType().getName());
						surchargeMappingPopulator.setPaymentType(surcharge.getPaymentType().getName());
						surchargeMappingPopulator.setStatus(surcharge.getStatus().getName());
						surchargeMappingPopulator.setPaymentsRegion(surcharge.getPaymentsRegion());

						surchargeMappingPopulator.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
						surchargeMappingPopulator
								.setBankSurchargeAmountOnCommercial(surcharge.getBankSurchargeAmountCommercial());
						surchargeMappingPopulator.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
						surchargeMappingPopulator.setBankSurchargePercentageOnCommercial(
								surcharge.getBankSurchargePercentageCommercial());
						surchargeMappingPopulator.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
						surchargeMappingPopulator
								.setBankSurchargeAmountOnCustomer(surcharge.getBankSurchargeAmountCustomer());
						surchargeMappingPopulator.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
						surchargeMappingPopulator
								.setBankSurchargePercentageOnCustomer(surcharge.getBankSurchargePercentageCustomer());
						surchargeMappingPopulator.setAllowOnOff(false);

						acquirerDetails1.add(surchargeMappingPopulator);
						
						if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL)) {

							AccountCurrencyRegion acr;

							if (surcharge.getPaymentsRegion()
									.equals(AccountCurrencyRegion.INTERNATIONAL)) {
								acr = AccountCurrencyRegion.DOMESTIC;
							} else {
								acr = AccountCurrencyRegion.INTERNATIONAL;
							}

							// Put one blank surcharge incase ALL regions are selected from mapping
							SurchargeMappingPopulator blankSurcharge = new SurchargeMappingPopulator();
							
							blankSurcharge.setAcquirerName(surcharge.getAcquirerName());
							blankSurcharge.setMopType(surcharge.getMopType().getName());
							blankSurcharge.setPaymentType(surcharge.getPaymentType().getName());
							blankSurcharge.setStatus(TDRStatus.INACTIVE.getName());
							blankSurcharge.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargeAmountOnCommercial(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargePercentageOnCommercial(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargeAmountOnCustomer(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
							blankSurcharge.setBankSurchargePercentageOnCustomer(BigDecimal.ZERO);
							blankSurcharge.setPaymentsRegion(acr);
							blankSurcharge.setAllowOnOff(false);
							acquirerDetails1.add(blankSurcharge);
						}
						
					}


				} 
				
				
				// 2 surcharges found without onus off config , but for INTL and DOM both
				else if ((surchargeListForRegion.size() == 2 && merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL))) {
					
					
					for (Surcharge surcharge : surchargeListForRegion) {
						
						SurchargeMappingPopulator surchargeMappingPopulatorAllRegion = new SurchargeMappingPopulator();
						surchargeMappingPopulatorAllRegion.setAcquirerName(surcharge.getAcquirerName());
						surchargeMappingPopulatorAllRegion.setMopType(surcharge.getMopType().getName());
						surchargeMappingPopulatorAllRegion.setPaymentType(surcharge.getPaymentType().getName());
						surchargeMappingPopulatorAllRegion.setStatus(surcharge.getStatus().getName());
						surchargeMappingPopulatorAllRegion.setPaymentsRegion(surcharge.getPaymentsRegion());

						surchargeMappingPopulatorAllRegion.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
						surchargeMappingPopulatorAllRegion
								.setBankSurchargeAmountOnCommercial(surcharge.getBankSurchargeAmountCommercial());
						surchargeMappingPopulatorAllRegion.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
						surchargeMappingPopulatorAllRegion.setBankSurchargePercentageOnCommercial(
								surcharge.getBankSurchargePercentageCommercial());
						surchargeMappingPopulatorAllRegion.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
						surchargeMappingPopulatorAllRegion
								.setBankSurchargeAmountOnCustomer(surcharge.getBankSurchargeAmountCustomer());
						surchargeMappingPopulatorAllRegion.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
						surchargeMappingPopulatorAllRegion
								.setBankSurchargePercentageOnCustomer(surcharge.getBankSurchargePercentageCustomer());
						surchargeMappingPopulatorAllRegion.setAllowOnOff(false);

						acquirerDetails1.add(surchargeMappingPopulatorAllRegion);
					}
				}
				else {

					for (Surcharge surcharge : surchargeListForRegion) {
						// Onus surcharge
						if (surcharge.getOnOff().equalsIgnoreCase("1")) {

							surchargeMappingPopulator.setAcquirerName(surcharge.getAcquirerName());
							surchargeMappingPopulator.setMopType(surcharge.getMopType().getName());
							surchargeMappingPopulator.setPaymentType(surcharge.getPaymentType().getName());
							surchargeMappingPopulator.setStatus(surcharge.getStatus().getName());
							surchargeMappingPopulator.setPaymentsRegion(surcharge.getPaymentsRegion());

							surchargeMappingPopulator
									.setBankSurchargeAmountOnCommercial(surcharge.getBankSurchargeAmountCommercial());
							surchargeMappingPopulator.setBankSurchargePercentageOnCommercial(
									surcharge.getBankSurchargePercentageCommercial());
							surchargeMappingPopulator
									.setBankSurchargeAmountOnCustomer(surcharge.getBankSurchargeAmountCustomer());
							surchargeMappingPopulator.setBankSurchargePercentageOnCustomer(
									surcharge.getBankSurchargePercentageCustomer());

							surchargeMappingPopulator.setAllowOnOff(true);
							
							// Offus surcharge
						} else if (surcharge.getOnOff().equalsIgnoreCase("2")) {

							surchargeMappingPopulator.setAcquirerName(surcharge.getAcquirerName());
							surchargeMappingPopulator.setMopType(surcharge.getMopType().getName());
							surchargeMappingPopulator.setPaymentType(surcharge.getPaymentType().getName());
							surchargeMappingPopulator.setStatus(surcharge.getStatus().getName());
							surchargeMappingPopulator.setPaymentsRegion(surcharge.getPaymentsRegion());

							surchargeMappingPopulator
									.setBankSurchargeAmountOffCommercial(surcharge.getBankSurchargeAmountCommercial());
							surchargeMappingPopulator.setBankSurchargePercentageOffCommercial(
									surcharge.getBankSurchargePercentageCommercial());
							surchargeMappingPopulator
									.setBankSurchargeAmountOffCustomer(surcharge.getBankSurchargeAmountCustomer());
							surchargeMappingPopulator.setBankSurchargePercentageOffCustomer(
									surcharge.getBankSurchargePercentageCustomer());

							surchargeMappingPopulator.setAllowOnOff(true);
						} else {

							surchargeMappingPopulator.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
							surchargeMappingPopulator.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
							surchargeMappingPopulator.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
							surchargeMappingPopulator.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
							surchargeMappingPopulator.setAllowOnOff(false);
							surchargeMappingPopulator.setPaymentsRegion(surcharge.getPaymentsRegion());
						}

					}
					acquirerDetails1.add(surchargeMappingPopulator);

				}
			}

			else {

				 
				MerchantAcquirerProperties merchantAcquirerProperties = new MerchantAcquirerProperties();
				merchantAcquirerProperties = merchantAcquirerPropertiesDao.getMerchantAcquirerPropertiesByName(payId,
						chargingDetail.getAcquirerName());
				
				// Blank surcharge for display only when both regions are configured from mapping
				if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL)) {

					surchargeMappingPopulator.setAcquirerName(chargingDetail.getAcquirerName());
					surchargeMappingPopulator.setMopType(chargingDetail.getMopType().getName());
					surchargeMappingPopulator.setPaymentType(chargingDetail.getPaymentType().getName());
					surchargeMappingPopulator.setStatus(TDRStatus.INACTIVE.getName());

					surchargeMappingPopulator.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargeAmountOnCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOnCommercial(BigDecimal.ZERO);

					surchargeMappingPopulator.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargeAmountOnCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOnCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setAllowOnOff(false);
					surchargeMappingPopulator.setPaymentsRegion(AccountCurrencyRegion.INTERNATIONAL);

					acquirerDetails1.add(surchargeMappingPopulator);

					surchargeMappingPopulator2.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
					surchargeMappingPopulator2.setAcquirerName(chargingDetail.getAcquirerName());
					surchargeMappingPopulator2.setMopType(chargingDetail.getMopType().getName());
					surchargeMappingPopulator2.setPaymentType(chargingDetail.getPaymentType().getName());
					surchargeMappingPopulator2.setStatus(TDRStatus.INACTIVE.getName());

					surchargeMappingPopulator2.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator2.setBankSurchargeAmountOnCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator2.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator2.setBankSurchargePercentageOnCommercial(BigDecimal.ZERO);

					surchargeMappingPopulator2.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator2.setBankSurchargeAmountOnCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator2.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator2.setBankSurchargePercentageOnCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator2.setAllowOnOff(false);

					acquirerDetails1.add(surchargeMappingPopulator2);
					
					// Blank surcharge for display only when any one region is configured from mapping
				} else {
					surchargeMappingPopulator.setPaymentsRegion(merchantAcquirerProperties.getPaymentsRegion());
					surchargeMappingPopulator.setAcquirerName(chargingDetail.getAcquirerName());
					surchargeMappingPopulator.setMopType(chargingDetail.getMopType().getName());
					surchargeMappingPopulator.setPaymentType(chargingDetail.getPaymentType().getName());
					surchargeMappingPopulator.setStatus(TDRStatus.INACTIVE.getName());

					surchargeMappingPopulator.setBankSurchargeAmountOffCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargeAmountOnCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOffCommercial(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOnCommercial(BigDecimal.ZERO);

					surchargeMappingPopulator.setBankSurchargeAmountOffCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargeAmountOnCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOffCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setBankSurchargePercentageOnCustomer(BigDecimal.ZERO);
					surchargeMappingPopulator.setAllowOnOff(false);

					acquirerDetails1.add(surchargeMappingPopulator);
				}

			}

		}

		Map<String, List<SurchargeMappingPopulator>> detailsMap = new HashMap<String, List<SurchargeMappingPopulator>>();

		for (AcquirerType acquirerType : AcquirerType.values()) {
			List<SurchargeMappingPopulator> surchargeDetailsList = new ArrayList<SurchargeMappingPopulator>();
			String acquirerName = acquirerType.getName();
			for (SurchargeMappingPopulator acquirerDetail : acquirerDetails1) {

				if (StringUtils.isNoneBlank(acquirerDetail.getAcquirerName()) && acquirerDetail.getAcquirerName().equals(acquirerName)) {

					acquirerDetail.setMerchantIndustryType(user.getIndustryCategory());

					surchargeDetailsList.add(acquirerDetail);
				}
			}
			if (surchargeDetailsList.size() != 0) {
				// Collections.sort(surchargeDetailsList);
				detailsMap.put(acquirerName, surchargeDetailsList);
			}
		}

		return detailsMap;
	}

}
