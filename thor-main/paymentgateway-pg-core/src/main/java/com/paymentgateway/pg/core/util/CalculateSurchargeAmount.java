package com.paymentgateway.pg.core.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.onUsOffUs;

@Service
public class CalculateSurchargeAmount {

	private static Logger logger = LoggerFactory.getLogger(CalculateSurchargeAmount.class.getName());

	@Autowired
	SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private StaticDataProvider staticDataProvider;

	private static Map<String, BigDecimal> serviceTaxMap = new HashMap<String, BigDecimal>();

	public BigDecimal[] fetchCCConsumerSurchargeDetails(String amount, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.CREDIT_CARD;
			CardHolderType cardHolderType = CardHolderType.CONSUMER;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, cardHolderType.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, cardHolderType);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Credit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {
				logger.error("Unable to calculate Credit Card consumer surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
						"Unable to fetch Credit Card consumer surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchCCConsumerSurchargeDetails ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
					"Unable to fetch Credit Card consumer surcharge details");
		}
	}

	public BigDecimal[] fetchCCCommercialSurchargeDetails(String amount, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.CREDIT_CARD;
			CardHolderType cardHolderType = CardHolderType.COMMERCIAL;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, cardHolderType.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, cardHolderType);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Credit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Credit Card commercial surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
						"Unable to fetch Credit Card commercial surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchCCCommercialSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
					"Unable to fetch Credit Card commercial surcharge details");
		}
	}

	public BigDecimal[] fetchCCPremiumSurchargeDetails(String amount, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.CREDIT_CARD;
			CardHolderType cardHolderType = CardHolderType.PREMIUM;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, cardHolderType.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, cardHolderType);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Credit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Credit Card premium surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
						"Unable to fetch Credit Card premium surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchCCPremiumSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
					"Unable to fetch Credit Card premium surcharge details");
		}
	}

	public BigDecimal[] fetchCCAmexSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.CREDIT_CARD;
			CardHolderType cardHolderType = CardHolderType.CONSUMER;
			MopType mopType = MopType.AMEX;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, cardHolderType.toString(), mopType).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, cardHolderType, mopType);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Credit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {
				transSurchargeAmount = BigDecimal.ZERO;
				actualTransactionAmount = BigDecimal.ZERO;

				/*
				 * logger.error("Unable to calculate Credit Card premium surcharge for payment "
				 * ); throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
				 * "Unable to fetch Credit Card premium surcharge details");
				 */
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchCCPremiumSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
					"Unable to fetch Credit Card premium surcharge details");
		}
	}

	public BigDecimal[] fetchDCVisaSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.DEBIT_CARD;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Debit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Debit Card surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Debit Card surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchDCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Debit Card surcharge details");
		}
	}

	public BigDecimal[] fetchDCRupaySurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.DEBIT_CARD;
			MopType mop = MopType.RUPAY;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Debit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Debit Card surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Debit Card surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchDCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Debit Card surcharge details");
		}
	}

	public BigDecimal[] fetchDCMasterSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.DEBIT_CARD;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Debit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Debit Card surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Debit Card surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchDCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Debit Card surcharge details");
		}
	}

	public BigDecimal[] fetchPCSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.PREPAID_CARD;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Prepaid card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Prepaid card surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
						"Unable to fetch Prepaid card surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchPCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Prepaid card surcharge details");
		}

	}

	public BigDecimal[] fetchCDSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.COD;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch COD surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}

				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate COD surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch COD surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchPCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch COD surcharge details");
		}

	}

	public BigDecimal[] fetchAPSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.AAMARPAY;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Aamarpay surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}

				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate Aamarpay surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Aamarpay surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchAPSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch Aamarpay surcharge details");
		}

	}

	public BigDecimal[] fetchCRSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.CRYPTO;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch CRYPTO surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}

				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate CRYPTO surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch CRYPTO surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchCryptoSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch CRYPTO surcharge details");
		}

	}

	public BigDecimal[] fetchNBSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {
		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.NET_BANKING;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch NetBanking surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate NetBanking surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch NetBanking surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchNBSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch NetBanking surcharge details");
		}
	}

	public BigDecimal[] fetchUPSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.UPI;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch UPI surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate UPI surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch UPI surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchUPSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch UPI surcharge details");
		}

	}

	public BigDecimal[] fetchSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId, PaymentType paymentType, CardHolderType cardHolderType, MopType mopType)
			throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {

			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, cardHolderType.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = null;

				if (mopType != null) {
					chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId, paymentsRegion,
							slabId, cardHolderType, mopType);
				} else {
					chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId, paymentsRegion,
							slabId, cardHolderType);
				}

				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Credit Card surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				if (mopType.equals(MopType.AMEX) || mopType.equals(MopType.RUPAY)) {
					transSurchargeAmount = BigDecimal.ZERO;
					actualTransactionAmount = BigDecimal.ZERO;
				} else {
					logger.error("Unable to calculate surcharge for payment ");
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch surcharge details");
				}
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchSurchargeDetails ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch surcharge details");
		}
	}

	public BigDecimal[] fetchWLSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.WALLET;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch Wallet surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate wallet surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch wallet surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchWLSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch wallet surcharge details");
		}

	}

	public BigDecimal[] fetchEMCCSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.EMI_CC;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch EMI CC surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate EMI CC surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch EMI CC surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchEMCCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch EMI CC surcharge details");
		}

	}

	public BigDecimal[] fetchEMDCSurchargeDetails(String amount, String payId, AccountCurrencyRegion paymentsRegion,
			String slabId, String resellerId) throws SystemException {

		BigDecimal actualTransactionAmount = null;
		BigDecimal transSurchargeAmount = null;

		try {
			PaymentType paymentType = PaymentType.EMI_DC;
			ChargingDetails surchargeDetailsFromDb = null;
			// Decide whether to use static usermap or get data from DAO
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				surchargeDetailsFromDb = staticDataProvider.getSurchargeData(payId, paymentType.getCode(),
						paymentsRegion.toString(), slabId, CardHolderType.CONSUMER.toString()).get(0);

			} else {
				List<ChargingDetails> chargesList = chargingDetailsDao.getCreditChargingDetailsList(paymentType, payId,
						paymentsRegion, slabId, CardHolderType.CONSUMER);
				for (ChargingDetails ChargingDetail : chargesList) {
					if (ChargingDetail.getAcquiringMode().equals(onUsOffUs.OFF_US)) {
						surchargeDetailsFromDb = ChargingDetail;
						break;
					}
				}
			}

			BigDecimal servicetax = null;
			BigDecimal transAmount = new BigDecimal(amount);

			if (surchargeDetailsFromDb != null) {
				servicetax = getServiceTax(payId);

				if (servicetax == null) {
					throw new SystemException(ErrorType.SURCHARGE_NOT_SET,
							"Unable to fetch EMI DC surcharge details , service tax not found");
				}

				BigDecimal percentagevalue = new BigDecimal(100);

				BigDecimal totalsurcharge = transAmount.multiply(
						new BigDecimal(surchargeDetailsFromDb.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				if (surchargeDetailsFromDb.isAllowFixCharge()) {
					totalsurcharge = totalsurcharge.add(new BigDecimal(surchargeDetailsFromDb.getMerchantFixCharge()));
				}
				totalsurcharge = totalsurcharge.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal calculatedServiceTax = totalsurcharge.multiply(servicetax).divide(percentagevalue);
				calculatedServiceTax = calculatedServiceTax.setScale(2, BigDecimal.ROUND_HALF_UP);

				actualTransactionAmount = (transAmount.add(totalsurcharge).add(calculatedServiceTax));
				transSurchargeAmount = actualTransactionAmount.subtract(transAmount);

			} else {

				logger.error("Unable to calculate EMI DC surcharge for payment ");
				throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch EMI DC surcharge details");
			}

			BigDecimal surchargeDetails[] = new BigDecimal[2];
			surchargeDetails[0] = transSurchargeAmount;
			surchargeDetails[1] = actualTransactionAmount;

			return surchargeDetails;
		} catch (SystemException exception) {
			logger.error("Exception occured in fetchEMDCSurchargeDetails : ", exception);
			throw new SystemException(ErrorType.SURCHARGE_NOT_SET, "Unable to fetch EMI DC surcharge details");
		}

	}

	public BigDecimal getServiceTax(String payid) {
		try {
			BigDecimal servicetax = null;
			if (serviceTaxMap.get(payid) != null) {
				servicetax = serviceTaxMap.get(payid);
			} else {
				servicetax = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
				if (servicetax != null) {
					serviceTaxMap.put(payid, servicetax);
				}
			}

			return servicetax;
		} catch (Exception exception) {
			logger.error("Exception in getServiceTax : ", exception);
			return null;
		}

	}

}
