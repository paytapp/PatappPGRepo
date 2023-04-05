package com.paymentgateway.crm.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.opensymphony.xwork2.ModelDriven;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PromotionalPaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UrlShortener;
import com.paymentgateway.crm.actionBeans.BatchResponseObject;
import com.paymentgateway.crm.actionBeans.CommanCsvReader;

/**
 * @author ISHA,CHANDAN
 *
 */
public class InvoiceTransactionEvent extends AbstractSecureAction implements ModelDriven<Invoice> {

	@Autowired
	private InvoiceTransactionDao invoiceTransactionDao;

	@Autowired
	private UrlShortener urlShortener;
	
	@Autowired
	private CrmEmailer crmEmailer;

	@Autowired
	EncryptDecryptService encryptDecryptService;

	//@Autowired
	//private EmailControllerServiceProvider emailControllerServiceProvider;
	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(InvoiceTransactionEvent.class.getName());
	private static final long serialVersionUID = 3857047834665047987L;
	private String fileName;

	private Invoice invoice = new Invoice();
	private String url;
	private String merchant;
	private boolean emailCheck;
	private String merchantPayId;
	private Map<String, String> currencyMap = new HashMap<String, String>();

	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private StringBuilder responseMessage = new StringBuilder();

	public String execute() {
		PropertiesManager propertyManager = new PropertiesManager();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			User user = (User) sessionMap.get(Constants.USER);
			invoice.setInvoiceId(TransactionManager.getNewTransactionId());
			invoice.setCreateDate(sdf.format(date));
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.SUPERADMIN)) {
				UserDao userDao = new UserDao();
				User merchant = userDao.findPayId(merchantPayId);
				setMerchantPayId(merchant.getPayId());
				invoice.setPayId(getMerchantPayId());
				invoice.setBusinessName(merchant.getBusinessName());
				currencyMap = Currency.getAllCurrency();
			} else {
				currencyMap = Currency.getSupportedCurreny(user);
				invoice.setPayId(user.getPayId());
			}
			invoice.setInvoiceType(PromotionalPaymentType.PROMOTIONAL_PAYMENT.getName());
			invoice.setSaltKey(invoice.getInvoiceId());
			if (invoice.getReturnUrl().isEmpty()) {
				invoice.setReturnUrl(
						propertyManager.getSystemProperty(CrmFieldConstants.INVOICE_RETURN_URL.getValue()));
			}

			url = propertyManager.getSystemProperty(CrmFieldConstants.INVOICE_PROMOTIONAL_URL.getValue())
					+ invoice.getInvoiceId();
			invoice.setShortUrl(url);
			invoiceTransactionDao.create(invoice);
			if (invoice.getEmail() != null) {
				//class isn't in use right now.
				//emailControllerServiceProvider.invoiceLink(url, invoice.getEmail(), invoice.getName());
			}
			/*batchEmail(getUrl(), invoice);*/
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("error generating invoice url", exception);
			return ERROR;
		}
	}

	public void validator() {

		if ((validator.validateBlankField(getFileName()))) {
			addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.FILE_NAME, getFileName()))) {
			addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getUrl()))) {
			addFieldError(CrmFieldType.INVOICE_URL.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_URL, getUrl()))) {
			addFieldError(CrmFieldType.INVOICE_URL.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getMerchant()))) {
			addFieldError(CrmFieldType.MERCHANT.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MERCHANT, getMerchant()))) {
			addFieldError(CrmFieldType.MERCHANT.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getMerchantPayId()))) {
			addFieldError(CrmFieldType.MERCHANT_ID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MERCHANT_ID, getMerchantPayId()))) {
			addFieldError(CrmFieldType.MERCHANT_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	@SkipValidation
	public String invoiceEmail() {
		InvoiceTransactionDao invoiceTransactionDao = new InvoiceTransactionDao();

		try {
			// custom validation
			if ((validator.validateBlankField(invoice.getInvoiceId()))) {
				addActionError(validator.getResonseObject().getResponseMessage());
				return ERROR;
			} else if (!(validator.validateField(CrmFieldType.INVOICE_ID, invoice.getInvoiceId()))) {
				return ERROR;
			}
			Invoice invoiceDB = invoiceTransactionDao.findByInvoiceId(invoice.getInvoiceId());
			PropertiesManager propertyManager = new PropertiesManager();
			setUrl(propertyManager.getSystemProperty(CrmFieldConstants.INVOICE_URL.getValue())
					+ invoiceDB.getInvoiceId());
			//emailControllerServiceProvider.invoiceLink(getUrl(), invoiceDB.getEmail(), invoiceDB.getName());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public void batchEmail(String url, Invoice invoiceDB) throws SystemException {
		try {
			/*
			 * String line = ""; List<Invoice> emailPhoneList = new LinkedList<Invoice>();
			 */
			BatchResponseObject batchResponseObject = new BatchResponseObject();
			CommanCsvReader commanCsvReader = new CommanCsvReader();

			if (!(StringUtils.isEmpty(fileName))) {

				batchResponseObject = commanCsvReader.csvReaderForBatchEmailSend(fileName);
				if (batchResponseObject.getInvoiceEmailList().isEmpty()) {
					addActionMessage(ErrorType.INVALID_FIELD.getResponseMessage());
				} else {
					for (Invoice emailphone : batchResponseObject.getInvoiceEmailList()) {
						try {
							//emailControllerServiceProvider.invoiceLink(url, emailphone.getEmail(), invoiceDB.getName());
						} catch (Exception exception) {
							responseMessage.append(ErrorType.EMAIL_ERROR.getResponseMessage());
							responseMessage.append(emailphone.getEmail());
							responseMessage.append(batchResponseObject.getResponseMessage());
							responseMessage.append("\n");
							logger.error("Error!! Unable to send email Emailer fail " , exception);
						}
					}
				}
				responseMessage.append(batchResponseObject.getResponseMessage());
				addActionMessage(responseMessage.toString());
			}
		} catch (Exception exception) {
			logger.error("sending email via batch file unsuccessfull! " , exception);
			addActionMessage("sending email via batch file unsuccessfull!" + exception);

		}
	}

	@SuppressWarnings("unchecked")
	public void validate() {

		CrmValidator validator = new CrmValidator();

		User user = (User) sessionMap.get(Constants.USER);
		merchantList = new UserDao().getActiveMerchantList();
		if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
			currencyMap = Currency.getAllCurrency();
			/*
			 * if
			 * (getMerchant().equalsIgnoreCase(CrmFieldConstants.SELECT_MERCHANT.getValue())
			 * ){ addFieldError(CrmFieldConstants.MERCHANT.getValue(),CrmFieldConstants.
			 * SELECT_MERCHANT.getValue()); }
			 */
		} else {
			currencyMap = Currency.getAllCurrency();
		}

		if ((validator.validateBlankField(invoice.getInvoiceNo()))) {
			addFieldError(CrmFieldType.INVOICE_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_NUMBER, invoice.getInvoiceNo()))) {
			addFieldError(CrmFieldType.INVOICE_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(invoice.getAmount()))) {
			addFieldError(CrmFieldType.INVOICE_AMOUNT.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_AMOUNT, invoice.getAmount()))) {
			addFieldError(CrmFieldType.INVOICE_AMOUNT.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getServiceCharge()))) {
			invoice.setServiceCharge("0.00");
		} else if (!(validator.validateField(CrmFieldType.INVOICE_AMOUNT, invoice.getServiceCharge()))) {
			addFieldError("serviceCharge", validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_AMOUNT, invoice.getTotalAmount()))) {
			addFieldError("totalAmount", validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getCity()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_CITY, invoice.getCity()))) {
			addFieldError(CrmFieldType.INVOICE_CITY.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getState()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_STATE, invoice.getState()))) {
			addFieldError(CrmFieldType.INVOICE_STATE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getBusinessName()))) {
		} else if (!(validator.validateField(CrmFieldType.BUSINESS_NAME, invoice.getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getProductName()))) {
		} else if (!(validator.validateField(CrmFieldType.PRODUCT_NAME, invoice.getProductName()))) {
			addFieldError("productName", validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getQuantity()))) {
		} else if (!(validator.validateField(CrmFieldType.QUANTITY, invoice.getQuantity()))) {
			addFieldError(CrmFieldType.QUANTITY.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getReturnUrl()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_RETURN_URL, invoice.getReturnUrl()))) {
			addFieldError(CrmFieldType.INVOICE_RETURN_URL.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getCountry()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_COUNTRY, invoice.getCountry()))) {
			addFieldError(CrmFieldType.INVOICE_COUNTRY.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getZip()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_ZIP, invoice.getZip()))) {
			addFieldError(CrmFieldType.INVOICE_ZIP.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getAddress()))) {
		} else if (!(validator.validateField(CrmFieldType.ADDRESS, invoice.getAddress()))) {
			addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getProductDesc()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_DESCRIPTION, invoice.getProductDesc()))) {
			addFieldError(CrmFieldType.INVOICE_DESCRIPTION.getName(),
					validator.getResonseObject().getResponseMessage());
		}

		if (invoice.getCurrencyCode().equals(CrmFieldConstants.SELECT_CURRENCY.getValue())
				|| validator.validateBlankField(invoice.getCurrencyCode())) {
			addFieldError(CrmFieldType.INVOICE_CURRENCY_CODE.getName(), CrmFieldConstants.SELECT_CURRENCY.getValue());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_CURRENCY_CODE, invoice.getCurrencyCode()))) {
			addFieldError(CrmFieldType.INVOICE_CURRENCY_CODE.getName(), CrmFieldConstants.SELECT_CURRENCY.getValue());
		}

		if (invoice.getExpiresDay().isEmpty()) {
			if (invoice.getExpiresHour().isEmpty()) {
				addFieldError(CrmFieldType.INVOICE_EXPIRES_DAY.getName(),
						validator.getResonseObject().getResponseMessage());
				addFieldError(CrmFieldType.INVOICE_EXPIRES_HOUR.getName(),
						ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			} else {
				invoice.setExpiresDay("0");
			}
		} else if (invoice.getExpiresHour().isEmpty()) {
			invoice.setExpiresHour("0");
		} else if ((Integer.parseInt(invoice.getExpiresDay().toString()) == 0
				&& Integer.parseInt(invoice.getExpiresHour().toString()) == 0)
				|| (Integer.parseInt(invoice.getExpiresDay().toString()) < 0
						|| Integer.parseInt(invoice.getExpiresHour().toString()) < 0)) {
			addFieldError(CrmFieldType.INVOICE_EXPIRES_DAY.getName(),
					validator.getResonseObject().getResponseMessage());
			addFieldError(CrmFieldType.INVOICE_EXPIRES_HOUR.getName(),
					validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(invoice.getInvoiceId()))) {
		} else if (!(validator.validateField(CrmFieldType.INVOICE_ID, invoice.getInvoiceId()))) {
			addFieldError(CrmFieldType.INVOICE_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	@Override
	public Invoice getModel() {
		return invoice;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isEmailCheck() {
		return emailCheck;
	}

	public void setEmailCheck(boolean emailCheck) {
		this.emailCheck = emailCheck;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public StringBuilder getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(StringBuilder responseMessage) {
		this.responseMessage = responseMessage;
	}

}
