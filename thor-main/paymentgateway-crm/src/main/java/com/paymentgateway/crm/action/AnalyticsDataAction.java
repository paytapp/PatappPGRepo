package com.paymentgateway.crm.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.AnalyticsData;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.AnalyticsDataService;

public class AnalyticsDataAction extends AbstractSecureAction {

	private static final long serialVersionUID = -6323553936458486881L;

	private static Logger logger = LoggerFactory.getLogger(AnalyticsDataAction.class.getName());

	private String dateFrom;
	private String dateTo;
	public String paymentMethods;
	public String acquirer;
	private String currency;
	private String merchantEmailId;
	private String txnType;
	private List finalList;
	private String mopType;

	AnalyticsData analyticsData = new AnalyticsData();

	@Autowired
	private AnalyticsDataService AnalyticsDataService;

	@Autowired
	private UserDao userDao;

	public String execute() {

		try {
			String datefrom="";
			String timeFrom="";
			String dateto="";
			String timeTo="";

			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}

			if (StringUtils.isBlank(txnType)) {
				txnType = "SALE";
			}

			if (StringUtils.isNotBlank(dateFrom)) {
				String[] sptitDateFrom = dateFrom.split(" ");
				datefrom = sptitDateFrom[0];
				timeFrom = sptitDateFrom[1];
			}
			if (StringUtils.isNotBlank(dateTo)) {
				String[] sptitDateTo = dateTo.split(" ");
				dateto = sptitDateTo[0];
				timeTo = sptitDateTo[1];
			}
			setDateFrom(DateCreater.dateTimeFormat(datefrom, timeFrom));
			setDateTo(DateCreater.dateTimeFormat(dateto, timeTo));

//			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
//			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.ASSOCIATE)) {

				String merchantPayId = null;

				if (!merchantEmailId.equalsIgnoreCase("All")) {
					User merchant = userDao.findPayIdByEmail(merchantEmailId);
					merchantPayId = merchant.getPayId();
				} else {
					merchantPayId = merchantEmailId;
				}

				setAnalyticsData(AnalyticsDataService.getTransactionCount(dateFrom, dateTo, merchantPayId,
						paymentMethods, acquirer, sessionUser, null, txnType, mopType, currency));

				setFinalList(AnalyticsDataService.finalList);
			}

		} catch (Exception e) {
			logger.error("Exception in getting transaction summary count data " , e);
		}

		return SUCCESS;
	}

	public void validate() {
		CrmValidator validator = new CrmValidator();

		if (validator.validateBlankField(getDateFrom())) {
		} else if (!validator.validateField(CrmFieldType.DATE_FROM, getDateFrom())) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateTo())) {
		} else if (!validator.validateField(CrmFieldType.DATE_TO, getDateTo())) {
			addFieldError(CrmFieldType.DATE_TO.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (!validator.validateBlankField(getDateTo())) {
			if (DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom()))
					.compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
			} else if (DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
			}
		}

		if (validator.validateBlankField(getMerchantEmailId())
				|| getMerchantEmailId().equals(CrmFieldConstants.ALL.getValue())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, getMerchantEmailId())) {
			addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public AnalyticsData getAnalyticsData() {
		return analyticsData;
	}

	public void setAnalyticsData(AnalyticsData analyticsData) {
		this.analyticsData = analyticsData;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public List getFinalList() {
		return finalList;
	}

	public void setFinalList(List finalList) {
		this.finalList = finalList;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
