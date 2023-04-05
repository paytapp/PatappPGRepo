package com.paymentgateway.crm.fraudPrevention.action;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CountryCodes;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.Weekdays;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudPreventionDao;
import com.paymentgateway.pg.core.util.DefaultCryptoManager;

/**
 * @author Harpreet
 *
 */
public class ViewFraudRule extends AbstractSecureAction {

	@Autowired
	private FraudPreventionDao fraudPreventionDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DefaultCryptoManager defaultCryptoManager;

	private static final long serialVersionUID = -303816138858895252L;
	private static Logger logger = LoggerFactory.getLogger(ViewFraudRule.class.getName());
	private List<FraudPrevention> fraudRuleList = new ArrayList<>();
	// private List<FraudPrevention> fraudRule = new ArrayList<>();

	private String payId;

	// retrieving fraud rule list
	public String execute() {
		try {
			if (payId.equalsIgnoreCase(CrmFieldConstants.ALL.getValue())) {
				fraudRuleList = fraudPreventionDao.getFraudRuleList(CrmFieldConstants.ALL.getValue());
				
				for (FraudPrevention fraudRule : fraudRuleList) {
					String payId = fraudRule.getPayId();
					if(!(payId.equals("ALL"))){
						User merchant = userDao.findPayId(payId);
						fraudRule.setPayId(merchant.getBusinessName());
					}
					if(StringUtils.isNotBlank(fraudRule.getVpaEncrypted())){
						fraudRule.setVpa(defaultCryptoManager.decryptVpa(payId, fraudRule.getVpaEncrypted()));
					}
					if(StringUtils.isNotBlank(fraudRule.getIssuerCountry())){
						fraudRule.setIssuerCountry(CountryCodes.getCountryName(fraudRule.getIssuerCountry()));
					}
					if(StringUtils.isNotBlank(fraudRule.getUserCountry())){
						fraudRule.setUserCountry(CountryCodes.getCountryName(fraudRule.getUserCountry()));
					}
					boolean alwaysOnFlag = fraudRule.isAlwaysOnFlag();
					if (alwaysOnFlag == false) {

						String dateFrom = fraudRule.getDateActiveFrom();
						String dateTo = fraudRule.getDateActiveTo();
						SimpleDateFormat originalFormatter = new SimpleDateFormat("yyyyMMdd");
						SimpleDateFormat newFormatter = new SimpleDateFormat("dd/MM/yyyy");

						// parsing date string using original format
						ParsePosition pos = new ParsePosition(0);
						ParsePosition pos1 = new ParsePosition(0);
						Date dateFromString = originalFormatter.parse(dateFrom, pos);
						Date dateToString = originalFormatter.parse(dateTo, pos1);

						// Now you have a date object and you can convert it to
						// the
						// new format
						String dateStringInNewFormat = newFormatter.format(dateFromString);
						String dateToStringInNewFormat = newFormatter.format(dateToString);
						fraudRule.setDateActiveFrom(dateStringInNewFormat);
						fraudRule.setDateActiveTo(dateToStringInNewFormat);

						// Change Start time format
						String startTimeValue = fraudRule.getStartTime();
						StringBuilder startTime = new StringBuilder();
						String hour = startTimeValue.substring(0, 2);
						String mintnue = startTimeValue.substring(2, 4);
						String second = startTimeValue.substring(4, 6);

						startTime.append(hour);
						startTime.append(":");
						startTime.append(mintnue);
						startTime.append(":");
						startTime.append(second);

						fraudRule.setStartTime(startTime.toString());

						// Change End time format
						String endTimeValue = fraudRule.getEndTime();
						StringBuilder EndTime = new StringBuilder();
						String endtimehour = endTimeValue.substring(0, 2);
						String endtimemintnue = endTimeValue.substring(2, 4);
						String endtimesecond = endTimeValue.substring(4, 6);

						EndTime.append(endtimehour);
						EndTime.append(":");
						EndTime.append(endtimemintnue);
						EndTime.append(":");
						EndTime.append(endtimesecond);

						fraudRule.setEndTime(EndTime.toString());

						// Change repeat day format
						String repeatDays = fraudRule.getRepeatDays();
						String[] details = repeatDays.split(",");
						StringBuilder dayCodes = new StringBuilder();

						for (String dayname : details) {
							Weekdays dayInstance = Weekdays.getday(dayname);
							String dayName = dayInstance.getName();
							dayCodes.append(dayName);
							dayCodes.append(",");
						}

						String dayCode = dayCodes.toString().substring(0, dayCodes.length() - 1);
						fraudRule.setRepeatDays(dayCode);
					}
					String currency = fraudRule.getCurrency();
					if ((currency != null)) {
						fraudRule.setCurrencyCode(currency);
						currency = Currency.getAlphabaticCode(currency);
						fraudRule.setCurrency(currency);
						logger.info("Add Currency Name");
					}

				}
				return SUCCESS;
			} else {
				fraudRuleList = fraudPreventionDao.getFraudRuleListbyPayId(payId);
				for (FraudPrevention fraudRule : fraudRuleList) {
					User merchant = userDao.findPayId(payId);
					fraudRule.setPayId(merchant.getBusinessName());
					String currency = fraudRule.getCurrency();
					if(StringUtils.isNotBlank(fraudRule.getVpaEncrypted())){
						fraudRule.setVpa(defaultCryptoManager.decryptVpa(payId, fraudRule.getVpaEncrypted()));
					}
					if(StringUtils.isNotBlank(fraudRule.getIssuerCountry())){
						fraudRule.setIssuerCountry(CountryCodes.getCountryName(fraudRule.getIssuerCountry()));
					}
					if(StringUtils.isNotBlank(fraudRule.getUserCountry())){
						fraudRule.setUserCountry(CountryCodes.getCountryName(fraudRule.getUserCountry()));
					}
					if ((currency != null)) {
						fraudRule.setCurrencyCode(currency);
						currency = Currency.getAlphabaticCode(currency);
						fraudRule.setCurrency(currency);
						logger.info("Add Currency Name");
					}//IF CLOSE()
					
					
					 
					
					
					
				}
				return SUCCESS;
			}
		} catch (Exception excetpion) {
			logger.error("Fraud Prevention System - Exception :" , excetpion);
			return ERROR;
		}
	}

	public void validate() {
		// TODO
	}

	public List<FraudPrevention> getFraudRuleList() {
		return fraudRuleList;
	}

	public void setFraudRuleList(List<FraudPrevention> fraudRuleList) {
		this.fraudRuleList = fraudRuleList;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
}