package com.paymentgateway.crm.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
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

public class SmsAnalyticsDataAction extends AbstractSecureAction {

	private static final long serialVersionUID = -6323553936458486881L;

	private static Logger logger = LoggerFactory.getLogger(SmsAnalyticsDataAction.class.getName());
   
	private String txnType;
	private String smsParam;
	private String dateFrom;
	private String dateTo;
	public String paymentMethods;
	public String acquirer;
	private String merchantEmailId;
	private String mopType;
	private String currency;
	AnalyticsData analyticsData = new AnalyticsData();

	@Autowired
	private AnalyticsDataService AnalyticsDataService;
	
	@Autowired
	private UserDao userDao;
	
	public String execute() {
		
		try {
			
			if(smsParam.equals("captured"))
			{
				
			  DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
			  Calendar calobj = Calendar.getInstance();
			  calobj.add(Calendar.DATE, -1);
			  dateFrom=df.format(calobj.getTime());
			  dateTo=df.format(calobj.getTime());
			  merchantEmailId="axis@gmail.com";
			  
			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}
			if (StringUtils.isBlank(paymentMethods)) {
				paymentMethods = "ALL";
			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN) || sessionUser.getUserType().equals(UserType.ADMIN)
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
						paymentMethods, acquirer, sessionUser , null, txnType, mopType, currency));
			}

			}
			else
			{
				DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
				Calendar calobj = Calendar.getInstance();
				dateFrom=df.format(calobj.getTime());
				dateTo=df.format(calobj.getTime());
				merchantEmailId="axis@gmail.com";
				  
				if (StringUtils.isBlank(acquirer)) {
					acquirer = "ALL";
				}
				if (StringUtils.isBlank(paymentMethods)) {
					paymentMethods = "ALL";
				}
				setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
				setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
				
				User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
				if (sessionUser.getUserType().equals(UserType.SUPERADMIN) || sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.ASSOCIATE)) {
					
		
					String merchantPayId = null;
					
					if (!merchantEmailId.equalsIgnoreCase("All")) {
						User merchant = userDao.findPayIdByEmail(merchantEmailId);
						merchantPayId = merchant.getPayId();
					} else {
						merchantPayId = merchantEmailId;
					}
					
					setAnalyticsData(AnalyticsDataService.getSettledTransaction(dateFrom, dateTo, merchantPayId,
							paymentMethods, acquirer, sessionUser));
				}
			}
		}
		catch(Exception e) {
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

	public String getSmsParam() {
		return smsParam;
	}

	public void setSmsParam(String smsParam) {
		this.smsParam = smsParam;
	}
	
	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
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