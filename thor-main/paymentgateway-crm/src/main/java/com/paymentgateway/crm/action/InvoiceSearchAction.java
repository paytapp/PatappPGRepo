package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.SearchInvoiceService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;

public class InvoiceSearchAction extends AbstractSecureAction {
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private DataEncoder encoder;
	
	@Autowired
	private SearchInvoiceService getInvoice;

	@Autowired
	private SessionUserIdentifier sessionUserIdentifier;
	
	@Autowired
	private UserDao userDao;
	
	private static Logger logger = LoggerFactory.getLogger(InvoiceSearchAction.class.getName());
	private static final long serialVersionUID = 8559806979618843084L;

	private List<Invoice> aaData = new ArrayList<Invoice>();
	//private String invoiceNo;
	private String productName;
	private String customerPhone;
	private String customerEmail;
	private String merchantPayId;
	private String subMerchantId;
	private String subUserId;
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String invoiceType;
	private String status;

	public String execute() {
		logger.info("Inside execute(); InvoiceSearchAction");
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		try {
			
				if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
					
					//currencyMap = Currency.getSupportedCurreny(user);
					String parentPayId = sessionUser.getParentPayId();
					User parentUser = userDao.findPayId(parentPayId);
					String subUserId = "";
					if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
						subUserId = sessionUser.getPayId();
					}
					if(!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
						merchantPayId=parentUser.getSuperMerchantId();
						subMerchantId=parentUser.getPayId();
					} else {
						merchantPayId=parentUser.getPayId();
					}
				}else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

					if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						
						merchantPayId=sessionUser.getSuperMerchantId();
						subMerchantId=sessionUser.getPayId();
					}
					
				}
			 
			setAaData(encoder.encodeInvoiceSearchObj(getInvoice.getInvoiceList(
					getDateFrom(), getDateTo(), merchantPayId, sessionUser
							.getUserType().toString(), getCustomerEmail(), getCurrency(),getInvoiceType(),getStatus(),getCustomerPhone(),getProductName(),subMerchantId,subUserId)));

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public void validate() {

		/*if (validator.validateBlankField(getInvoiceNo())) {
		} else if (!validator.validateField(CrmFieldType.INVOICE_NUMBER,
				getInvoiceNo())) {
			addFieldError("invoiceNo",
					ErrorType.INVALID_FIELD.getResponseMessage());
		}*/
		if (validator.validateBlankField(getProductName())) {
		} else if (!validator.validateField(CrmFieldType.PRODUCT_NAME,
				getCurrency())) {
			addFieldError(CrmFieldType.PRODUCT_NAME.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if (validator.validateBlankField(getCustomerPhone())) {
		} else if (!validator.validateField(CrmFieldType.INVOICE_PHONE,
				getCustomerPhone())) {
			addFieldError(CrmFieldType.INVOICE_PHONE.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY,
				getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateFrom())) {
		} else if (!validator.validateField(CrmFieldType.DATE_FROM,
				getDateFrom())) {
			addFieldError(CrmFieldType.DATE_FROM.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateTo())) {
		} else if (!validator.validateField(CrmFieldType.DATE_TO, getDateTo())) {
			addFieldError(CrmFieldType.DATE_TO.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if(!validator.validateBlankField(getDateTo())){
	 	       if(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom())).compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
	 	        	addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
	 	        }
	 	        else if(DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
	 	        	addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
	 	        }
	         }

		if (validator.validateBlankField(getMerchantPayId())
				|| getMerchantPayId()
						.equals(CrmFieldConstants.ALL.getValue())) {
		} else if (!validator.validateField(CrmFieldType.PAY_ID,
				getMerchantPayId())) {
			addFieldError(CrmFieldType.PAY_ID.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getCustomerEmail())) {
		} else if (!validator.validateField(CrmFieldType.CUSTOMER_EMAIL_ID,
				getCustomerEmail())) {
			addFieldError(CrmFieldType.CUSTOMER_EMAIL_ID.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public List<Invoice> getAaData() {
		return aaData;
	}

	public void setAaData(List<Invoice> aaData) {
		this.aaData = aaData;
	}
/*
	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}*/
	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCustomerPhone() {
		return customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getSubUserId() {
		return subUserId;
	}

	public void setSubUserId(String subUserId) {
		this.subUserId = subUserId;
	}
	

	
}
