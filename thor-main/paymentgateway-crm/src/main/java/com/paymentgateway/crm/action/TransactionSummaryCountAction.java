package com.paymentgateway.crm.action;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.TransactionSummaryCountService;

@SuppressWarnings("serial")
public class TransactionSummaryCountAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(TransactionSummaryCountAction.class.getName());

	private String dateFrom;
	private String dateTo;
	public String paymentMethods;
	private String merchantEmailId;
	private String subMerchantEmailId;
	private String transactionType;
	private String paymentsRegion;
	private String cardHolderType;
	private String mopType;
	private String currency;
	private String acquirer;
	private String statusType;
	private String partSettleFlag;
	
	TransactionCountSearch transactionCountSearch = new TransactionCountSearch();

	@Autowired
	private TransactionSummaryCountService transactionSummaryCountService;
	
	@Autowired
	private UserDao userDao;
	
	public String execute() {
		
		try {
			
			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN) || sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.ASSOCIATE)) {
				
				
				String merchantPayId = null;
				String subMerchantPayId = null;
				
				if (!merchantEmailId.equalsIgnoreCase("All")) {
					User merchant = userDao.findPayIdByEmail(merchantEmailId);
					merchantPayId = merchant.getPayId();
				} else {
					merchantPayId = merchantEmailId;
				}
				
				if(StringUtils.isNotBlank(subMerchantEmailId)) {
					if (!subMerchantEmailId.equalsIgnoreCase("All")) {
						User merchant = userDao.findPayIdByEmail(subMerchantEmailId);
						subMerchantPayId = merchant.getPayId();
					} else {
						subMerchantPayId = subMerchantEmailId;
					}
				}
				if(StringUtils.isBlank(partSettleFlag)){
					partSettleFlag="ALL";
				}
				
				setTransactionCountSearch(transactionSummaryCountService.getTransactionCount(dateFrom, dateTo, merchantPayId,
						subMerchantPayId, paymentMethods, acquirer,sessionUser, 1, 1, getPaymentsRegion(),
						getCardHolderType(), mopType,transactionType,statusType, partSettleFlag, currency));
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


	public TransactionCountSearch getTransactionCountSearch() {
		return transactionCountSearch;
	}

	public void setTransactionCountSearch(TransactionCountSearch transactionCountSearch) {
		this.transactionCountSearch = transactionCountSearch;
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

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getStatusType() {
		return statusType;
	}

	public void setStatusType(String statusType) {
		this.statusType = statusType;
	}

	public String getPartSettleFlag() {
		return partSettleFlag;
	}

	public void setPartSettleFlag(String partSettleFlag) {
		this.partSettleFlag = partSettleFlag;
	}

	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
