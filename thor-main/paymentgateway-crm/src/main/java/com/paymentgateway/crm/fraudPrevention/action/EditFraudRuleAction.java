package com.paymentgateway.crm.fraudPrevention.action;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.FraudPreventionHistory;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.Weekdays;
import com.paymentgateway.crm.actionBeans.LoginAuthenticator;
import com.paymentgateway.crm.fraudPrevention.actionBeans.FraudRuleCreator;
import com.paymentgateway.pg.core.fraudPrevention.core.CheckExistingFraudRule;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudPreventionDao;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudRuleModel;

/**
 * @author Harpreet,Rahul
 *
 */

@Service
public class EditFraudRuleAction {

	@Autowired
	private CheckExistingFraudRule checkExistingFraudRule;

	@Autowired
	private FraudPreventionDao fraudPreventionDao;
	@Autowired
	private LoginAuthenticator loginAuthenticator;

	private static Logger logger = LoggerFactory.getLogger(FraudRuleCreator.class.getName());

	@Autowired
	private ResponseObject ruleCheckResponse;

	@Autowired
	private ResponseObject finalResponse;
	public static String servierIP = "";

	public ResponseObject EditFraudRule(String rowId, FraudRuleModel fraudRuleModel) {
		FraudPrevention fraudPrevention = new FraudPrevention();

		logger.info("FraudRuleCreator   createFraudRule");

		try {
			// generating rule id
			// fraudPrevention.setId(Long.parseLong(TransactionManager.getNewTransactionId()));
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
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_NO_OF_TXNS:
				fraudPrevention.setMinutesTxnLimit(fraudRuleModel.getMinutesTxnLimit());
				fraudPrevention.setNoOfTransactionAllowed(fraudRuleModel.getNoOfTransactionAllowed());
				fraudRuleModel.setAlwaysOnFlag(true);
				EditcommonActions(rowId, fraudPrevention, fraudRuleModel);

				break;
			case BLOCK_TXN_AMOUNT:
				fraudPrevention.setCurrency(fraudRuleModel.getCurrency());
				fraudPrevention.setMinTransactionAmount(fraudRuleModel.getMinTransactionAmount());
				fraudPrevention.setMaxTransactionAmount(fraudRuleModel.getMaxTransactionAmount());
				fraudRuleModel.setAlwaysOnFlag(true);
				EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				break;
			case BLOCK_IP_ADDRESS:
				String iPAddress = fraudRuleModel.getIpAddress();
				String[] iPAddressArray = iPAddress.split(",");
				for (String ipAddress : iPAddressArray) {
					fraudPrevention.setIpAddress(ipAddress);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_DOMAIN_NAME:
				String domains = fraudRuleModel.getDomainName();
				String[] domainArray = domains.split(",");
				for (String domain : domainArray) {
					fraudPrevention.setDomainName(domain);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_EMAIL_ID:
				String emailIds = fraudRuleModel.getEmail();
				String[] emailIdArray = emailIds.split(",");
				for (String emailId : emailIdArray) {
					fraudPrevention.setEmail(emailId);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_USER_COUNTRY:

				String userCountries = fraudRuleModel.getUserCountry();
				String[] userCountryArray = userCountries.split(",");
				for (String userCountry : userCountryArray) {
					fraudPrevention.setUserCountry(userCountry);
					fraudRuleModel.setAlwaysOnFlag(true);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_CARD_ISSUER_COUNTRY:
				String issuerCountries = fraudRuleModel.getIssuerCountry();
				String[] countryArray = issuerCountries.split(",");
				for (String country : countryArray) {
					fraudPrevention.setIssuerCountry(country);
					fraudRuleModel.setAlwaysOnFlag(true);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_CARD_BIN:
				String binValues = fraudRuleModel.getNegativeBin();
				String[] strArray = binValues.split(",");
				for (String str : strArray) {
					fraudPrevention.setNegativeBin(str);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_CARD_NO:
				String cardValues = fraudRuleModel.getNegativeCard();
				String[] cardArray = cardValues.split(",");
				for (String card : cardArray) {
					fraudPrevention.setNegativeCard(card);
					EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				}
				break;
			case BLOCK_CARD_TXN_THRESHOLD:
				fraudPrevention.setPerCardTransactionAllowed(fraudRuleModel.getPerCardTransactionAllowed());
				fraudPrevention.setNegativeCard(fraudRuleModel.getNegativeCard());
				EditcommonActions(rowId, fraudPrevention, fraudRuleModel);
				break;
			case BLOCK_TXN_VELOCITY:
				fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
				fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
				fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
				fraudPrevention.setNoOfTransactionAllowed(fraudRuleModel.getNoOfTransactionAllowed());
				break;
			case BLOCK_AMOUNT_VELOCITY:
				fraudPrevention.setPaymentType(fraudRuleModel.getPaymentType());
				fraudPrevention.setPaymentRegion(fraudRuleModel.getPaymentRegion());
				fraudPrevention.setTimePeriod(fraudRuleModel.getTimePeriod());
				fraudPrevention.setAmountAllowed(fraudRuleModel.getAmountAllowed());
				break;
			}
			

			finalResponse.setResponseCode(ruleCheckResponse.getResponseCode());
			finalResponse.setResponseMessage(ruleCheckResponse.getResponseMessage());
			logger.info("Fraud Rule creation response " + ruleCheckResponse.getResponseMessage());
			return finalResponse;

		} catch (Exception exception) {
			logger.error("error : " , exception);
			return ruleCheckResponse;
		}
	}// EditcreateFraudRule().close();

	public void EditcommonActions(String rowId, FraudPrevention fraudPrevention, FraudRuleModel fraudRuleModel) {

		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			String cleanIp = ip.getHostAddress();
			servierIP = cleanIp;

		} catch (UnknownHostException e) {

			logger.error("Exception in TransactionManager", e);

		}

		ruleCheckResponse = checkExistingFraudRule.exists(fraudPrevention);
		logger.info(" after checking the existing fraud rules " + ruleCheckResponse);
		if (ruleCheckResponse.getResponseCode().equals(ErrorType.FRAUD_RULE_ALREADY_EXISTS.getResponseCode())) {
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
			/*
			 * Maintain History IPBlockRule Before Update
			 */
			FraudPreventionHistory fraudPreventionHistory = new FraudPreventionHistory();
			User user = loginAuthenticator.getUser();
			fraudPreventionHistory.setDateActiveFrom(datefrom.toString());
			fraudPreventionHistory.setDateActiveTo(dateTo.toString());
			fraudPreventionHistory.setStartTime(formattedStartTime);
			fraudPreventionHistory.setEndTime(formattedEndTime);
			fraudPreventionHistory.setRepeatDays(dayCode);
			fraudPreventionHistory.setAlwaysOnFlag(fraudRuleModel.isAlwaysOnFlag());

			fraudPreventionHistory.setLoginEmailId(user.getEmailId());
			fraudPreventionHistory.setLoginIpAddress(servierIP);
			fraudPreventionDao.saveFraudrule(fraudPreventionHistory);
           /*
            * Update FraudRuleIPAddressBlock
            */
			fraudPrevention.setDateActiveFrom(datefrom.toString());
			fraudPrevention.setDateActiveTo(dateTo.toString());
			fraudPrevention.setStartTime(formattedStartTime);
			fraudPrevention.setEndTime(formattedEndTime);
			fraudPrevention.setRepeatDays(dayCode);
			fraudPrevention.setAlwaysOnFlag(fraudRuleModel.isAlwaysOnFlag());
			fraudPrevention.setId(Long.parseLong(rowId));
			fraudPreventionDao.updateFraudRule(fraudPrevention.getId(), fraudPrevention.getStatus(),
					fraudPrevention.getRepeatDays(), fraudPrevention.getDateActiveFrom(),
					fraudPrevention.getDateActiveTo(), fraudPrevention.getStartTime(), fraudPrevention.getEndTime(),
					fraudPrevention.getIpAddress(), fraudPrevention.getAlwaysOnFlag());
			ruleCheckResponse.setResponseCode(ErrorType.FRAUD_RULE_UPDATE_SUCCESS.getResponseCode());
			ruleCheckResponse.setResponseMessage(ErrorType.FRAUD_RULE_UPDATE_SUCCESS.getResponseMessage());

		} // if close();
	}// EditcommonActions close();

}
