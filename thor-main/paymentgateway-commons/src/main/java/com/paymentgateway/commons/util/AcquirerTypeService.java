package com.paymentgateway.commons.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;

@Service
public class AcquirerTypeService {

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	private static Logger logger = LoggerFactory.getLogger(AcquirerTypeService.class.getName());

	private static BigDecimal minAmountSlab2 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab2MinAmount"));
	private static BigDecimal minAmountSlab3 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab3MinAmount"));

	public AcquirerType getDefault(Fields fields, User user) throws SystemException {
		AcquirerType acquirer = null;
		acquirer = getAcquirer(fields.getFields(), user, fields);
		return acquirer;
	}

	private AcquirerType getAcquirer(Map<String, String> fields, User user, Fields txnFields) throws SystemException {
		logger.info("Finding acquirer");
		String acquirerName = "";
		PaymentType paymentType = PaymentType.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()));
		String mopType = fields.get(FieldType.MOP_TYPE.getName());
		String currency = fields.get(FieldType.CURRENCY_CODE.getName());
		String paymentTypeCode = fields.get(FieldType.PAYMENT_TYPE.getName());
		String payId = user.getPayId();
		String transactionType = TransactionType.SALE.getName();
		// String onUsOffUsValue = "ONUS";
		String paymentsRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
		String cardHolderType = fields.get(FieldType.CARD_HOLDER_TYPE.getName());

		String slabId = "";
		BigDecimal txnAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));

		if (txnAmount.compareTo(minAmountSlab3) >= 0) {
			slabId = "03";
		} else if (txnAmount.compareTo(minAmountSlab2) >= 0) {
			slabId = "02";
		} else {
			slabId = "01";
		}

		txnFields.put(FieldType.SLAB_ID.getName(), slabId);

		if (StringUtils.isEmpty(fields.get(FieldType.PAYMENT_TYPE.getName())) || StringUtils.isEmpty(mopType)
				|| StringUtils.isEmpty(currency)) {
			return null;
		}

		String identifier = payId + currency + paymentTypeCode + mopType + transactionType + paymentsRegion
				+ cardHolderType + slabId;
		logger.info("Acqurier identifier = " + identifier);
		switch (paymentType) {

		case CREDIT_CARD:
		case DEBIT_CARD:
		case PREPAID_CARD:
		case EMI_CC:
		case EMI_DC:
			List<RouterConfiguration> rulesList = null;
			rulesList = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			if (rulesList.size() == 0) {
				logger.info("No acquirer found for identifier = " + identifier + " Order id "
						+ fields.get(FieldType.ORDER_ID.getName()));
				break;

			}

			if (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {

				if (rulesList.size() == 0) {
					logger.info("No acquirer found for identifier = " + identifier + " Order id "
							+ fields.get(FieldType.ORDER_ID.getName()));
					break;

				}

				if (rulesList.size() > 1) {
					int randomNumber = getRandomNumber();
					int min = 1;
					int max = 0;
					for (RouterConfiguration routerConfiguration : rulesList) {
						int loadPercentage = routerConfiguration.getLoadPercentage();
						min = 1 + max;
						max = max + loadPercentage;
						if (randomNumber >= min && randomNumber <= max) {
							acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
							txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
							break;
						}
					}
				} else {

					for (RouterConfiguration routerConfiguration : rulesList) {
						acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
						txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
						break;
					}
				}

			}

			else {

				if (rulesList.size() == 0) {
					logger.info("No acquirer found for identifier = " + identifier + " Order id "
							+ fields.get(FieldType.ORDER_ID.getName()));
					break;

				}

				if ((StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName())))) {

					for (RouterConfiguration routerConfiguration : rulesList) {

						if (routerConfiguration.getOnUsoffUsName().equalsIgnoreCase(onUsOffUs.ON_US.toString())
								&& routerConfiguration.getAcquirer()
										.equalsIgnoreCase(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {

							acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
							txnFields.put(FieldType.ACQUIRER_MODE.getName(), "ON_US");
							break;
						}

					}
				}

				int randomNumber = getRandomNumber();

				if (rulesList.size() > 1) {

					int min = 1;
					int max = 0;
					for (RouterConfiguration routerConfiguration : rulesList) {

						if (routerConfiguration.getOnUsoffUsName().equalsIgnoreCase(onUsOffUs.ON_US.toString())) {
							continue;
						}

						int loadPercentage = routerConfiguration.getLoadPercentage();
						min = 1 + max;
						max = max + loadPercentage;
						if (randomNumber >= min && randomNumber <= max) {
							acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
							txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
							break;
						}
					}
				} else {

					for (RouterConfiguration routerConfiguration : rulesList) {
						acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
						txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
					}
				}

			}

			break;

		case NET_BANKING:
			List<RouterConfiguration> rulesListNB = new ArrayList<RouterConfiguration>();
			rulesListNB = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			if (rulesListNB.size() == 0) {
				logger.info("No acquirer found for identifier = " + identifier + " Order id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			} else if (rulesListNB.size() > 1) {
				int randomNumberUpi = getRandomNumber();
				int min = 1;
				int max = 0;
				for (RouterConfiguration routerConfiguration : rulesListNB) {
					int loadPercentage = routerConfiguration.getLoadPercentage();
					min = 1 + max;
					max = max + loadPercentage;
					if (randomNumberUpi >= min && randomNumberUpi <= max) {
						acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
						txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
						break;
					}
				}
			} else {

				for (RouterConfiguration routerConfiguration : rulesListNB) {
					acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
					txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");

				}
			}
			break;
		case WALLET:
			List<RouterConfiguration> rulesListWL = new ArrayList<RouterConfiguration>();
			rulesListWL = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			if (rulesListWL.size() == 0) {
				logger.info("No acquirer found for identifier = " + identifier + " Order id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			} else if (rulesListWL.size() > 1) {
				int randomNumberUpi = getRandomNumber();
				int min = 1;
				int max = 0;
				for (RouterConfiguration routerConfiguration : rulesListWL) {
					int loadPercentage = routerConfiguration.getLoadPercentage();
					min = 1 + max;
					max = max + loadPercentage;
					if (randomNumberUpi >= min && randomNumberUpi <= max) {
						acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
						txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
						break;
					}
				}
			} else {

				for (RouterConfiguration routerConfiguration : rulesListWL) {
					acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
					txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");

				}
			}
			break;
		case EMI:
		//	acquirerName = AcquirerType.CITRUS_PAY.getName();
			break;
		case RECURRING_PAYMENT:
			//acquirerName = AcquirerType.CITRUS_PAY.getName();
			break;
		case COD:
			//acquirerName = AcquirerType.COD.getName();
			txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			break;
		case AAMARPAY:
			//acquirerName = AcquirerType.AAMARPAY.getName();
			txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			break;
		case CRYPTO:
			acquirerName = AcquirerType.IPINT.getName();
			txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			break;
		case UPI:
			List<RouterConfiguration> rulesListUpi = new ArrayList<RouterConfiguration>();
			rulesListUpi = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			if (rulesListUpi.size() == 0) {
				logger.info("No acquirer found for identifier = " + identifier + " Order id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			} else if (rulesListUpi.size() > 1) {
				int randomNumberUpi = getRandomNumber();
				int min = 1;
				int max = 0;
				for (RouterConfiguration routerConfiguration : rulesListUpi) {
					int loadPercentage = routerConfiguration.getLoadPercentage();
					min = 1 + max;
					max = max + loadPercentage;
					if (randomNumberUpi >= min && randomNumberUpi <= max) {
						acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
						txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
						break;
					}
				}
			} else {

				for (RouterConfiguration routerConfiguration : rulesListUpi) {
					acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
					txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");

				}
			}

			break;
			
		case MQR:
			List<RouterConfiguration> rulesListMqr = new ArrayList<RouterConfiguration>();
			rulesListMqr = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			if (rulesListMqr.size() == 0) {
				logger.info("No acquirer found for identifier = " + identifier + " Order id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			} else if (rulesListMqr.size() > 1) {
				int randomNumberUpi = getRandomNumber();
				int min = 1;
				int max = 0;
				for (RouterConfiguration routerConfiguration : rulesListMqr) {
					int loadPercentage = routerConfiguration.getLoadPercentage();
					min = 1 + max;
					max = max + loadPercentage;
					if (randomNumberUpi >= min && randomNumberUpi <= max) {
						acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
						txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
						break;
					}
				}
			} else {

				for (RouterConfiguration routerConfiguration : rulesListMqr) {
					acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
					txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");

				}
			}

			break;
		case AD:
			/*
			 * RouterRuleDao ruleDaoAtl = new RouterRuleDao(); RouterRule atlRules =
			 * ruleDaoAtl.findRuleByFieldsByPayId(payId, paymentTypeCode, mopType, currency,
			 * transactionType); if (atlRules == null) { String allPayId = "ALL MERCHANTS";
			 * atlRules = ruleDaoAtl.findRuleByFieldsByPayId(allPayId, paymentTypeCode,
			 * mopType, currency, transactionType); } if (atlRules == null) { throw new
			 * SystemException(ErrorType.ROUTER_RULE_NOT_FOUND,
			 * ErrorType.ROUTER_RULE_NOT_FOUND.getResponseCode()); } String atlAcquirerList
			 * = atlRules.getAcquirerMap(); Collection<String> atlAcqList =
			 * Helper.parseFields(atlAcquirerList); Map<String, String> atlAcquirerMap = new
			 * LinkedHashMap<String, String>(); for (String atlAcquirer : atlAcqList) {
			 * String[] acquirerPreference = atlAcquirer.split("-");
			 * atlAcquirerMap.put(acquirerPreference[0], acquirerPreference[1]); }
			 * 
			 * String atlPrimaryAcquirer = atlAcquirerMap.get("1"); // String
			 * primaryAcquirer ="FIRSTDATA"; acquirerName =
			 * AcquirerType.getInstancefromCode(atlPrimaryAcquirer).getName();
			 * txnFields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			 */
			break;
		default:
			break;
		}
		if (StringUtils.isEmpty(acquirerName))

		{
			throw new SystemException(ErrorType.ACQUIRER_NOT_FOUND, ErrorType.ACQUIRER_NOT_FOUND.getResponseCode());
		}
		fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.getInstancefromName(acquirerName).getCode());
		logger.info("Acquirer for identifier = " + identifier + " Order id " + fields.get(FieldType.ORDER_ID.getName())
				+ " Pay id " + fields.get(FieldType.PAY_ID.getName()) + " Acquirer = " + acquirerName);
		return AcquirerType.getInstancefromName(acquirerName);
	}

	private static int getRandomNumber() {
		Random rnd = new Random();
		int randomNumber = (int) (rnd.nextInt(100)) + 1;
		return randomNumber;
	}

}
