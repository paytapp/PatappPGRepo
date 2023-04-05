package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Shaiwal
 *
 */

@Service
public class StaticDataProvider {

	@Autowired
	private UserDao userDao;

	/*
	 * @Autowired private ChargingDetailsDao chargingDetailsDao;
	 */

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Map<String, User> userMap = new ConcurrentHashMap<String, User>();
	private static Map<String, ChargingDetails> chargingDetailsMap = new ConcurrentHashMap<String, ChargingDetails>();
	private static Map<String, List<RouterConfiguration>> routerConfigurationMap = new ConcurrentHashMap<String, List<RouterConfiguration>>();
	private static Map<String, List<ChargingDetails>> chargingDetailsListMap = new ConcurrentHashMap<String, List<ChargingDetails>>();
	private static Map<String, List<ChargingDetails>> surchargeMap = new ConcurrentHashMap<String, List<ChargingDetails>>();

	private static Logger logger = LoggerFactory.getLogger(StaticDataProvider.class.getName());

	public User getUserData(String payId) {

		if (StringUtils.isBlank(payId)) {
			return null;
		}

		if (PropertiesManager.propertiesMap.get("useStaticData") != null
				&& PropertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {

			if (userMap.get(payId) != null) {

				return userMap.get(payId);

			} else {
				User user = new UserDao().findPayId(payId);
				if (user != null) {
					userMap.put(payId, user);
					return user;
				} else {
					return null;
				}

			}

		} else {
			return userDao.findPayId(payId);
		}

	}

	public void setUserData(User user) {
		userMap.put(user.getPayId(), user);
	}

	public ChargingDetails getChargingDetailsData(String payId, String paymentType, String acquirer, String mopType,
			String transactionType, String currency, String paymentRegion, String cardHolderType, String acquiringMode,
			String slabId) {

		PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);
		MopType mopTypeIns = MopType.getmop(mopType);
		TransactionType transactionTypeIns = TransactionType.getInstanceFromCode(transactionType);
		String acquirerName = AcquirerType.getAcquirerName(acquirer);
		onUsOffUs onOff = null;
		AccountCurrencyRegion acr = null;
		CardHolderType cht = null;
		

		if (paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		} else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}

		if (cardHolderType.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
			cht = CardHolderType.CONSUMER;
		} else if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
			cht = CardHolderType.COMMERCIAL;
		} else {
			cht = CardHolderType.PREMIUM;
		}

		if (acquiringMode.equalsIgnoreCase("0") || acquiringMode.equalsIgnoreCase("OFF_US")
				|| acquiringMode.equalsIgnoreCase("Off Us")) {
			onOff = onUsOffUs.OFF_US;
		} else if (acquiringMode.equalsIgnoreCase("1") || acquiringMode.equalsIgnoreCase("ON_US")
				|| acquiringMode.equalsIgnoreCase("On Us")) {
			onOff = onUsOffUs.ON_US;
		} else {
			onOff = onUsOffUs.ALL;
		}

		if (propertiesManager.propertiesMap.get("useStaticData") != null
				&& propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {

			String identifier = payId + "&" + paymentType + "&" + acquirer + "&" + mopType + "&" + transactionType + "&"
					+ currency + "&" + paymentRegion + "&" + cardHolderType + "&" + acquiringMode + "&" + slabId;
			if (chargingDetailsMap.get(identifier) != null) {

				return chargingDetailsMap.get(identifier);

			} else {

				ChargingDetails chargingDetails = new ChargingDetailsDao().findActiveChargingDetail(mopTypeIns,
						paymentTypeIns, transactionTypeIns, acquirerName, currency, payId, acr, cht, onOff, slabId);
				if (chargingDetails != null) {
					chargingDetailsMap.put(identifier, chargingDetails);
					return chargingDetails;
				} else {
					return null;
				}

			}
		}

		else {

			return new ChargingDetailsDao().findActiveChargingDetail(mopTypeIns, paymentTypeIns, transactionTypeIns,
					acquirerName, currency, payId, acr, cht, onOff, slabId);
		}

	}

	public List<ChargingDetails> getSurchargeData(String payId, String paymentType, String paymentsRegion,
			String slabId, String cardHolderType) {

		PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);

		AccountCurrencyRegion acr = null;
		CardHolderType cht = null;

		if (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		} else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}

		if (cardHolderType.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
			cht = CardHolderType.CONSUMER;
		} else if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
			cht = CardHolderType.COMMERCIAL;
		} else {
			cht = CardHolderType.PREMIUM;
		}

		if (propertiesManager.propertiesMap.get("useStaticData") != null
				&& propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {

			String identifier = payId + "&" + paymentType + "&" + paymentsRegion + "&" + slabId + "&" + cardHolderType;
			if (surchargeMap.get(identifier) != null) {

				return surchargeMap.get(identifier);

			} else {

				List<ChargingDetails> chargingDetailsList = new ChargingDetailsDao()
						.getCreditChargingDetailsList(paymentTypeIns, payId, acr, slabId, cht);
				if (chargingDetailsList.size() > 0) {
					surchargeMap.put(identifier, chargingDetailsList);
					return chargingDetailsList;
				} else {
					return null;
				}

			}
		}

		else {

			return new ChargingDetailsDao().getCreditChargingDetailsList(paymentTypeIns, payId, acr, slabId, cht);
		}

	}
	
	public List<ChargingDetails> getSurchargeData(String payId, String paymentType, String paymentsRegion,
			String slabId, String cardHolderType, MopType mopType) {

		PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);

		AccountCurrencyRegion acr = null;
		CardHolderType cht = null;

		if (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		} else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}

		if (cardHolderType.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
			cht = CardHolderType.CONSUMER;
		} else if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
			cht = CardHolderType.COMMERCIAL;
		} else {
			cht = CardHolderType.PREMIUM;
		}

		if (propertiesManager.propertiesMap.get("useStaticData") != null
				&& propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {

			String identifier = payId + "&" + paymentType + "&" + paymentsRegion + "&" + slabId + "&" + cardHolderType+ "&" + mopType.toString();
			if (surchargeMap.get(identifier) != null) {

				return surchargeMap.get(identifier);

			} else {

				List<ChargingDetails> chargingDetailsList = new ChargingDetailsDao()
						.getCreditChargingDetailsList(paymentTypeIns, payId, acr, slabId, cht, mopType);
				if (chargingDetailsList.size() > 0) {
					surchargeMap.put(identifier, chargingDetailsList);
					return chargingDetailsList;
				} else {
					return null;
				}

			}
		}

		else {

			return new ChargingDetailsDao().getCreditChargingDetailsList(paymentTypeIns, payId, acr, slabId, cht, mopType);
		}

	}

	public List<RouterConfiguration> getRouterConfigData(String identifier) {

		if (propertiesManager.propertiesMap.get("useStaticData") != null
				&& propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {

			if (routerConfigurationMap.get(identifier) != null) {

				return routerConfigurationMap.get(identifier);

			} else {
				List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
				rulesList = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

				if (rulesList != null && rulesList.size() > 0) {
					routerConfigurationMap.put(identifier, rulesList);
					return rulesList;
				} else {
					return null;
				}

			}
		}

		else {
			return routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);
		}

	}

	public List<ChargingDetails> getChargingDetailsList(String payId) {

		if (propertiesManager.propertiesMap.get("useStaticData") != null
				&& propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {

			List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
			if (chargingDetailsListMap.get(payId) != null) {

				return chargingDetailsListMap.get(payId);

			} else {

				chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(payId);

				if (chargingDetailsList != null && chargingDetailsList.size() > 0) {
					chargingDetailsListMap.put(payId, chargingDetailsList);
					return chargingDetailsList;
				} else {
					return chargingDetailsList;
				}

			}
		}

		else {
			List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
			chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(payId);
			return chargingDetailsList;
		}

	}

	public static void updateMapValues() {

		try {

			logger.info("Updating user map");
			for (String key : userMap.keySet()) {
				User user = new UserDao().findPayId(key);
				if (user != null) {
					userMap.put(key, user);
				} else {
					userMap.remove(key);
				}
			}

			for (String key : chargingDetailsMap.keySet()) {

				String keyArray[] = key.split("&");
				String payId = keyArray[0];
				String paymentType = keyArray[1];
				String acquirer = keyArray[2];
				String mopType = keyArray[3];
				String transactionType = keyArray[4];
				String currency = keyArray[5];
				String paymentRegion = keyArray[6];
				String cardHolderType = keyArray[7];
				String acquiringMode = keyArray[8];
				String slabId = keyArray[9];

				PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);
				MopType mopTypeIns = MopType.getmop(mopType);
				TransactionType transactionTypeIns = TransactionType.getInstanceFromCode(transactionType);
				String acquirerName = AcquirerType.getAcquirerName(acquirer);

				AccountCurrencyRegion acr = null;
				CardHolderType cht = null;
				onUsOffUs onOff = null;

				if (paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
					acr = AccountCurrencyRegion.DOMESTIC;
				} else {
					acr = AccountCurrencyRegion.INTERNATIONAL;
				}

				if (cardHolderType.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
					cht = CardHolderType.CONSUMER;
				} else if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
					cht = CardHolderType.COMMERCIAL;
				} else {
					cht = CardHolderType.PREMIUM;
				}

				if (acquiringMode.equalsIgnoreCase("0") || acquiringMode.equalsIgnoreCase("OFF_US")
						|| acquiringMode.equalsIgnoreCase("Off Us")) {
					onOff = onUsOffUs.OFF_US;
				} else if (acquiringMode.equalsIgnoreCase("1") || acquiringMode.equalsIgnoreCase("ON_US")
						|| acquiringMode.equalsIgnoreCase("On Us")) {
					onOff = onUsOffUs.ON_US;
				} else {
					onOff = onUsOffUs.ALL;
				}

				if (paymentTypeIns != null && mopTypeIns != null && transactionTypeIns != null
						&& StringUtils.isNotBlank(acquirerName)) {
					ChargingDetails chargingDetails = new ChargingDetailsDao().findActiveChargingDetail(mopTypeIns,
							paymentTypeIns, transactionTypeIns, acquirerName, currency, payId, acr, cht, onOff, slabId);

					if (chargingDetails != null) {
						chargingDetailsMap.put(key, chargingDetails);
					} else {
						chargingDetailsMap.remove(key);
					}

				}
			}

			for (String key : chargingDetailsListMap.keySet()) {

				List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
				chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(key);

				if (chargingDetailsList != null && chargingDetailsList.size() > 0) {
					chargingDetailsListMap.put(key, chargingDetailsList);
					logger.info("chargingDetailsListMap updated for payId == " + key);
				} else {
					logger.info("No charging details found for payId , removing from map , payId == " + key);
					chargingDetailsListMap.remove(key);
				}
			}

			for (String key : surchargeMap.keySet()) {

				String keySplit[] = key.split("&");
				
				String payId = keySplit[0];
				String paymentType = keySplit[1];
				String paymentsRegion = keySplit[2];
				String slabId = keySplit[3];
				String cardHolderType = keySplit[4];
				
				PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);
				AccountCurrencyRegion acr = null;
				CardHolderType cht = null;
				
				if (cardHolderType.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
					cht = CardHolderType.CONSUMER;
				} else if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
					cht = CardHolderType.COMMERCIAL;
				} else {
					cht = CardHolderType.PREMIUM;
				}
				

				if (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
					acr = AccountCurrencyRegion.DOMESTIC;
				} else {
					acr = AccountCurrencyRegion.INTERNATIONAL;
				}
				
				List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
				chargingDetailsList = new ChargingDetailsDao().getCreditChargingDetailsList(paymentTypeIns, payId, acr,
						slabId, cht);

				if (chargingDetailsList != null && chargingDetailsList.size() > 0) {
					
					surchargeMap.put(key, chargingDetailsList);
					logger.info("SurchargeMap updated for identifier == " + key);
					
				} else {
					logger.info("No charging details found for identifier , removing from map , identifier == " + key);
					chargingDetailsListMap.remove(key);
				}
			}

		} catch (Exception e) {
			logger.error("Exception in updating static map provider", e);
		}

	}

}
