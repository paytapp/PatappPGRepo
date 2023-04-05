package com.paymentgateway.crm.fraudPrevention.actionBeans;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.SystemConstants;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Weekdays;
import com.paymentgateway.pg.core.fraudPrevention.core.CheckExistingFraudRule;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudPreventionDao;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudRuleModel;
import com.paymentgateway.pg.core.util.DefaultCryptoManager;

/**
 * @author Harpreet, Rahul
 *
 */

@Service
public class FraudRuleCreator {

	@Autowired
	private CheckExistingFraudRule checkExistingFraudRule;

	@Autowired
	private FraudPreventionDao fraudPreventionDao;

	private static Logger logger = LoggerFactory.getLogger(FraudRuleCreator.class.getName());

	@Autowired
	private ResponseObject ruleCheckResponse;

	@Autowired
	private ResponseObject finalResponse;

	@Autowired
	private DefaultCryptoManager defaultCryptoManager;

	public ResponseObject createFraudRule(FraudRuleModel fraudRuleModel, User sessionUser) {
		FraudPrevention fraudPrevention = new FraudPrevention();

		logger.info("FraudRuleCreator   createFraudRule");

		try {
			// generating rule id
			fraudPrevention.setId(Long.parseLong(TransactionManager.getNewTransactionId()));
			fraudPrevention.setPayId(fraudRuleModel.getPayId());
			FraudRuleType fraudRuleType = FraudRuleType.getInstance(fraudRuleModel.getFraudType());
			fraudPrevention.setFraudType(fraudRuleType);
			logger.info(" before switch fraudRuleType   " + fraudRuleType);
			switch (fraudRuleType) {
			case WHITE_LIST_IP_ADDRESS:
				String whiteListIP = fraudRuleModel.getWhiteListIpAddress();
				String[] whiteListIPArray = whiteListIP.split(",");
				for (String ip : whiteListIPArray) {
					fraudPrevention.setWhiteListIpAddress(ip);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_NO_OF_TXNS:
				fraudPrevention.setMinutesTxnLimit(fraudRuleModel.getMinutesTxnLimit());
				fraudPrevention.setNoOfTransactionAllowed(fraudRuleModel.getNoOfTransactionAllowed());
				fraudRuleModel.setAlwaysOnFlag(true);
				commonActions(fraudPrevention, fraudRuleModel, sessionUser);

				break;
			case BLOCK_TXN_AMOUNT:
				if (fraudRuleModel.getPaymentType().equalsIgnoreCase("ALL")
						|| fraudRuleModel.getPaymentRegion().equalsIgnoreCase("ALL")) {

					fraudPrevention.setCurrency(fraudRuleModel.getCurrency());
					fraudPrevention.setMinTransactionAmount(fraudRuleModel.getMinTransactionAmount());
					fraudPrevention.setMaxTransactionAmount(fraudRuleModel.getMaxTransactionAmount());
					fraudRuleModel.setAlwaysOnFlag(true);
					createForAllType(fraudPrevention, fraudRuleModel, sessionUser);

				} else {
					fraudPrevention.setCurrency(fraudRuleModel.getCurrency());
					fraudPrevention.setMinTransactionAmount(fraudRuleModel.getMinTransactionAmount());
					fraudPrevention.setMaxTransactionAmount(fraudRuleModel.getMaxTransactionAmount());
					fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
					fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
					fraudRuleModel.setAlwaysOnFlag(true);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}

				break;
			case BLOCK_IP_ADDRESS:
				String iPAddress = fraudRuleModel.getIpAddress();
				String[] iPAddressArray = iPAddress.split(",");
				for (String ipAddress : iPAddressArray) {
					fraudPrevention.setIpAddress(ipAddress);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_DOMAIN_NAME:
				String domains = fraudRuleModel.getDomainName();
				String[] domainArray = domains.split(",");
				for (String domain : domainArray) {
					fraudPrevention.setDomainName(domain);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_EMAIL_ID:
				String emailIds = fraudRuleModel.getEmail();
				String[] emailIdArray = emailIds.split(",");
				for (String emailId : emailIdArray) {
					fraudPrevention.setEmail(emailId);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_USER_COUNTRY:

				String userCountries = fraudRuleModel.getUserCountry();
				String[] userCountryArray = userCountries.split(",");
				for (String userCountry : userCountryArray) {
					fraudPrevention.setUserCountry(userCountry);
					fraudRuleModel.setAlwaysOnFlag(true);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_CARD_ISSUER_COUNTRY:
				String issuerCountries = fraudRuleModel.getIssuerCountry();
				String[] countryArray = issuerCountries.split(",");
				for (String country : countryArray) {
					fraudPrevention.setIssuerCountry(country);
					fraudRuleModel.setAlwaysOnFlag(true);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_CARD_BIN:
				String binValues = fraudRuleModel.getNegativeBin();
				String[] strArray = binValues.split(",");
				for (String str : strArray) {
					fraudPrevention.setNegativeBin(str);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_CARD_NO:
				String cardValues = fraudRuleModel.getNegativeCard();
				String[] cardArray = cardValues.split(",");
				for (String card : cardArray) {
					String cardHash = defaultCryptoManager.hashCardNumber(card);
					StringBuilder truncatedCardNumber = new StringBuilder();
					truncatedCardNumber.append(card.substring(0, SystemConstants.CARD_BIN_LENGTH));
					truncatedCardNumber.append(Constants.CARD_STARS.getValue());
					truncatedCardNumber.append(
							card.subSequence(card.length() - SystemConstants.CARD_BIN_LENGTH + 2, card.length()));
					fraudPrevention.setNegativeCard(cardHash);
					fraudPrevention.setCardMask(truncatedCardNumber.toString());
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_CARD_TXN_THRESHOLD:
				fraudPrevention.setPerCardTransactionAllowed(fraudRuleModel.getPerCardTransactionAllowed());
				String card = fraudRuleModel.getNegativeCard();
				String cardHash = defaultCryptoManager.hashCardNumber(card);
				fraudPrevention.setNegativeCard(cardHash);
				StringBuilder truncatedCardNumber = new StringBuilder();
				truncatedCardNumber.append(card.substring(0, SystemConstants.CARD_BIN_LENGTH));
				truncatedCardNumber.append(Constants.CARD_STARS.getValue());
				truncatedCardNumber
						.append(card.subSequence(card.length() - SystemConstants.CARD_BIN_LENGTH + 2, card.length()));
				fraudPrevention.setNegativeCard(cardHash);
				fraudPrevention.setCardMask(truncatedCardNumber.toString());
				commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				break;
			case BLOCK_TXN_VELOCITY:
				if (fraudRuleModel.getPaymentType().equalsIgnoreCase("ALL")
						|| fraudRuleModel.getPaymentRegion().equalsIgnoreCase("ALL")) {

					fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
					fraudPrevention.setNoOfTransactionAllowed(fraudRuleModel.getNoOfTransactionAllowed());
					fraudRuleModel.setAlwaysOnFlag(true);
					createForAllType(fraudPrevention, fraudRuleModel, sessionUser);
				} else {

					fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
					fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
					fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
					fraudPrevention.setNoOfTransactionAllowed(fraudRuleModel.getNoOfTransactionAllowed());
					fraudRuleModel.setAlwaysOnFlag(true);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_AMOUNT_VELOCITY:
				if (fraudRuleModel.getPaymentType().equalsIgnoreCase("ALL")
						|| fraudRuleModel.getPaymentRegion().equalsIgnoreCase("ALL")) {

					fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
					fraudPrevention.setAmountAllowed(fraudRuleModel.getAmountAllowed());
					fraudRuleModel.setAlwaysOnFlag(true);
					createForAllType(fraudPrevention, fraudRuleModel, sessionUser);

				} else {
					fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
					fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
					fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
					fraudPrevention.setAmountAllowed(fraudRuleModel.getAmountAllowed());
					fraudRuleModel.setAlwaysOnFlag(true);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_SALE_AMOUNT_VELOCITY:
				if (fraudRuleModel.getPaymentType().equalsIgnoreCase("ALL")
						|| fraudRuleModel.getPaymentRegion().equalsIgnoreCase("ALL")) {

					fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
					fraudPrevention.setAmountAllowed(fraudRuleModel.getAmountAllowed());
					fraudRuleModel.setAlwaysOnFlag(true);
					createForAllType(fraudPrevention, fraudRuleModel, sessionUser);

				} else {
					fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
					fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
					fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
					fraudPrevention.setAmountAllowed(fraudRuleModel.getAmountAllowed());
					fraudRuleModel.setAlwaysOnFlag(true);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_VPA:
				String vpaValue = fraudRuleModel.getVpa();
				String payId = fraudRuleModel.getPayId();
				String[] vpaArray = vpaValue.split(",");
				for (String vpa : vpaArray) {
					String vpaHash = defaultCryptoManager.hashVpa(vpa.toLowerCase());
					String vpaEncrypted = defaultCryptoManager.encryptVpa(payId, vpa.toLowerCase());

					fraudPrevention.setVpaHash(vpaHash);
//					fraudPrevention.setVpa(vpa);
					fraudPrevention.setVpaEncrypted(vpaEncrypted);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;
			case BLOCK_VPA_TXN:
				String vpa = fraudRuleModel.getVpa();
				String merchantPayId = fraudRuleModel.getPayId();
				String vpaTotalTransactions = fraudRuleModel.getVpaTotalTransactionAllowed();
				String[] vpaArrayList = vpa.split(",");
				for (String getVpa : vpaArrayList) {

					String vpaHash = defaultCryptoManager.hashVpa(getVpa);
					String vpaEncrypted = defaultCryptoManager.encryptVpa(merchantPayId, getVpa);

					fraudPrevention.setVpaHash(vpaHash);
					fraudPrevention.setVpaTotalTransactionAllowed(vpaTotalTransactions);
					fraudPrevention.setVpaEncrypted(vpaEncrypted);
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
				break;

			}

			finalResponse.setResponseCode(ruleCheckResponse.getResponseCode());
			finalResponse.setResponseMessage(ruleCheckResponse.getResponseMessage());
			logger.info("Fraud Rule creation response " + ruleCheckResponse.getResponseMessage());
			return finalResponse;

		} catch (Exception exception) {
			logger.error("error : ", exception);
			return ruleCheckResponse;
		}
	}

	private void createForAllType(FraudPrevention fraudPrevention, FraudRuleModel fraudRuleModel, User sessionUser) {
		String[] paymentRegionArray = fraudRuleModel.getPaymentRegionArray().split(",");
		String[] paymentTypeArray = fraudRuleModel.getPaymentTypeArray().split(",");

		if (fraudRuleModel.getPaymentRegion().equalsIgnoreCase("ALL")) {
			for (String paymentRegion : paymentRegionArray) {
				fraudPrevention.setPaymentRegion(paymentRegion);

				if (fraudRuleModel.getPaymentType().equalsIgnoreCase("ALL")) {
					for (String paymentType : paymentTypeArray) {
						fraudPrevention.setPaymentType(paymentType);
						commonActions(fraudPrevention, fraudRuleModel, sessionUser);
					}
				} else {
					fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
					commonActions(fraudPrevention, fraudRuleModel, sessionUser);
				}
			}
		} else {

			fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
			for (String paymentType : paymentTypeArray) {
				fraudPrevention.setPaymentType(paymentType);
				commonActions(fraudPrevention, fraudRuleModel, sessionUser);
			}
		}
	}

	public void commonActions(FraudPrevention fraudPrevention, FraudRuleModel fraudRuleModel, User sessionUser) {

		ruleCheckResponse = checkExistingFraudRule.exists(fraudPrevention);
		logger.info(" after checking the existing fraud rules " + ruleCheckResponse);
		if (ruleCheckResponse.getResponseCode().equals(ErrorType.FRAUD_RULE_NOT_EXIST.getResponseCode())) {
			logger.info("FRAUD_RULE_NOT_EXIST");
			// by default status is ACTIVE --> TdrStatusType enum refactor
			fraudPrevention.setStatus(TDRStatus.ACTIVE);

			boolean alwaysOnFlag = fraudRuleModel.isAlwaysOnFlag();
			StringBuilder datefrom = new StringBuilder();
			StringBuilder dateTo = new StringBuilder();
			String formattedStartTime = "";
			String formattedEndTime = "";
			String dayCode = "";
			if (alwaysOnFlag == false) {

				// Date from and date to format changed
				String dateActiveFrom = fraudRuleModel.getDateActiveFrom();
				String dateActiveTo = fraudRuleModel.getDateActiveTo();
				datefrom = datefrom.append(dateActiveFrom.substring(6)).append("")
						.append(dateActiveFrom.substring(3, 5)).append("").append(dateActiveFrom.substring(0, 2));

				dateTo = dateTo.append(dateActiveTo.substring(6)).append("").append(dateActiveTo.substring(3, 5))
						.append("").append(dateActiveTo.substring(0, 2));

				// Start time and end time format changes
				String startTime = fraudRuleModel.getStartTime();
				String endTime = fraudRuleModel.getEndTime();
				formattedStartTime = startTime.replaceAll(":", "");
				formattedEndTime = endTime.replaceAll(":", "");

				// weekdays format changed
				String days = fraudRuleModel.getRepeatDays();
				String[] details = days.split(",");
				StringBuilder dayCodes = new StringBuilder();

				for (String dayname : details) {
					Weekdays dayInstance = Weekdays.getInstanceIgnoreCase(dayname);
					dayCode = dayInstance.getCode();
					dayCodes.append(dayCode);
					dayCodes.append(",");
				}

				dayCode = dayCodes.toString().substring(0, dayCodes.length() - 1);

			} else {
				dayCode = "NA";
				formattedEndTime = "NA";
				formattedStartTime = "NA";
				dateTo.append("NA");
				datefrom.append("NA");

			}
			Date currentDate = new Date();
			fraudPrevention.setDateActiveFrom(datefrom.toString());
			fraudPrevention.setDateActiveTo(dateTo.toString());
			fraudPrevention.setStartTime(formattedStartTime);
			fraudPrevention.setEndTime(formattedEndTime);
			fraudPrevention.setRepeatDays(dayCode);
			fraudPrevention.setAlwaysOnFlag(fraudRuleModel.isAlwaysOnFlag());
			fraudPrevention.setRequestedBy(sessionUser.getEmailId());
			fraudPrevention.setCreateDate(currentDate);

			fraudPreventionDao.create(fraudPrevention);

			ruleCheckResponse.setResponseCode(ErrorType.FRAUD_RULE_SUCCESS.getResponseCode());
			ruleCheckResponse.setResponseMessage(ErrorType.FRAUD_RULE_SUCCESS.getResponseMessage());

		}
	}
}