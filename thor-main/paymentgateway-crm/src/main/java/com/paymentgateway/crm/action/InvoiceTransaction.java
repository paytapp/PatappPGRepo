package com.paymentgateway.crm.action;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.email.PepipostEmailSender;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PromotionalPaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author ISHA,CHANDAN,Shiva
 *
 */
public class InvoiceTransaction extends AbstractSecureAction implements ModelDriven<Invoice> {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private QRCodeCreator qRCodeCreator;

	//@Autowired
	//private EmailControllerServiceProvider emailControllerServiceProvider;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;
	
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private CrmEmailer crmEmailer;
	
	@Autowired
	private Base64EncodeDecode base64EncodeDecode;
	
	@Autowired
	private InvoiceTransactionDao invoiceTransactionDao;
	
	@Autowired
	BitlyUrlShortener bitlyUrlShortener;
	
	@Autowired
	AWSSESEmailService awsSESEmailService;
	
	@Autowired
	PepipostEmailSender pepipostEmailSender;
	
	private static Logger logger = LoggerFactory.getLogger(InvoiceTransaction.class.getName());
	private static final long serialVersionUID = 3857047834665047987L;

	private Invoice invoice = new Invoice();
	private String url;
	private String merchant;
	private boolean emailCheck;
	private boolean smsCheck;
	private String merchantPayId; 
	private boolean smsStatus;
	private boolean emailStatus;
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private static Map<String, User> userMap = new HashMap<String, User>();
	
	public String execute() {
			smsStatus=false;
			emailStatus=false;
		try {
			User user = (User) sessionMap.get(Constants.USER);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date =  new Date();
			
			invoice.setInvoiceId(TransactionManager.getNewTransactionId());
			invoice.setCreateDate(sdf.format(date));
			
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.SUPERADMIN)) {
				
				merchantList = userDao.getActiveMerchantList();
				currencyMap = Currency.getAllCurrency();
				
				invoice.setPayId(getMerchantPayId());
				
				if (userMap.get(invoice.getPayId()) != null) {
					if(StringUtils.isNotBlank(invoice.getSubMerchantId())){
						invoice.setBusinessName(userDao.getBusinessNameByPayId(invoice.getPayId()));
						invoice.setSubMerchantbusinessName(userDao.getBusinessNameByPayId(invoice.getSubMerchantId()));
					}else{
						invoice.setBusinessName(userMap.get(invoice.getPayId()).getBusinessName());
					}
				}
				else {
					if(StringUtils.isNotBlank(invoice.getSubMerchantId())){
						userMap.put(invoice.getPayId(), userDao.findPayId(invoice.getSubMerchantId()));
						invoice.setBusinessName(userDao.getBusinessNameByPayId(invoice.getPayId()));
						invoice.setSubMerchantbusinessName(userDao.getBusinessNameByPayId(invoice.getSubMerchantId()));
					}else{
						userMap.put(invoice.getPayId(), userDao.findPayId(invoice.getPayId()));
						invoice.setBusinessName(userDao.getBusinessNameByPayId(invoice.getPayId()));
					}	
				}
				
			} else {
				
				if (user.getUserType().equals(UserType.SUBUSER)) {
					
					//currencyMap = Currency.getSupportedCurreny(user);
					String parentPayId = user.getParentPayId();
					User parentUser = userDao.findPayId(parentPayId);
					merchantList=userDao.getMerchantActive(parentUser.getEmailId());
					
					if(!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
						
						User superMerchant = userDao.findPayId(parentUser.getSuperMerchantId());
						invoice.setPayId(superMerchant.getPayId());
						invoice.setBusinessName(superMerchant.getBusinessName());
						invoice.setSubMerchantbusinessName(parentUser.getBusinessName());
						invoice.setSubMerchantId(parentUser.getPayId());
						currencyMap = Currency.getSupportedCurreny(superMerchant);
					} else {
						currencyMap = Currency.getSupportedCurreny(parentUser);
						invoice.setPayId(user.getParentPayId());
						invoice.setBusinessName(parentUser.getBusinessName());
					}
					invoice.setSubUserId(user.getPayId());
				} else if (user.getUserType().equals(UserType.MERCHANT)) {
					merchantList=userDao.getMerchantActive(user.getEmailId());

					if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						
						User superMerchant = userDao.findPayId(user.getSuperMerchantId());
						invoice.setPayId(user.getSuperMerchantId());
						invoice.setSubMerchantId(user.getPayId());
						currencyMap = Currency.getSupportedCurreny(superMerchant);
						invoice.setBusinessName(superMerchant.getBusinessName());
						invoice.setSubMerchantbusinessName(user.getBusinessName());
					}else if(user.isSuperMerchant()){
						User subMerchant = userDao.findPayId(invoice.getSubMerchantId());
						invoice.setPayId(subMerchant.getSuperMerchantId());
						invoice.setSubMerchantId(subMerchant.getPayId());
						currencyMap = Currency.getSupportedCurreny(user);
						invoice.setBusinessName(user.getBusinessName());
						invoice.setSubMerchantbusinessName(subMerchant.getBusinessName());
					}else{
						invoice.setPayId(user.getPayId());
						currencyMap = Currency.getSupportedCurreny(user);
						invoice.setBusinessName(user.getBusinessName());
					}
					
				}
			}
			
			
			invoice.setInvoiceType(PromotionalPaymentType.INVOICE_PAYMENT.getName());
			invoice.setSaltKey(invoice.getInvoiceId());
			
			invoice.setReturnUrl(propertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_RETURN_URL.getValue()));
			
			url = propertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_URL.getValue()) + invoice.getInvoiceId();
			invoice.setLongUrl(url);
			invoice.setShortUrl(bitlyUrlShortener.createShortUrlUsingBitly(url));
			invoice.setStatus("Active");
			
			BufferedImage image = qRCodeCreator.generateQRCode(invoice);
			File file = new File(invoice.getInvoiceId()+".png");
			ImageIO.write(image, "png", file);
			invoice.setQr(base64EncodeDecode.base64Encoder(file));
			file.delete();
			if (isEmailCheck()) {
				if(StringUtils.isNotBlank(invoice.getEmail()))
				{
				String subject = "Payment Gateway Smart Payment Link -- Invoice ID "+invoice.getInvoiceId();
				String emailBody = getInvoiceBodyWithQR(url,invoice,invoice.getQr());
				emailStatus=pepipostEmailSender.invoiceEmail(emailBody, subject, invoice.getEmail(), invoice.getEmail(), false);

				}
				else{
					emailStatus=false;
				}
				//emailControllerServiceProvider.invoiceLink(url, invoice.getEmail(), invoice.getName());
			} if (isSmsCheck()) {
				if(StringUtils.isNotBlank(invoice.getPhone())){
					smsStatus=smsControllerServiceProvider.invoiceSms(url, invoice);
				}
				else{
					smsStatus=false;
				}
			}
			invoice.setEmailStatus(emailStatus);
			invoice.setSmsStatus(smsStatus);
			invoiceTransactionDao.create(invoice);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("error generating invoice url", exception);
			return ERROR;
		}
	}

	@SkipValidation
	public String invoiceEmail() {
		

		try {
			// custom validation
			if ((validator.validateBlankField(invoice.getInvoiceId()))) {
				addActionError(validator.getResonseObject().getResponseMessage());
				return ERROR;
			} else if (!(validator.validateField(CrmFieldType.INVOICE_ID, invoice.getInvoiceId()))) {
				return ERROR;
			}
			Invoice invoiceDB = invoiceTransactionDao.findByInvoiceId(invoice.getInvoiceId());
			setUrl(propertiesManager.getSystemProperty(CrmFieldConstants.INVOICE_URL.getValue())
					+ invoiceDB.getInvoiceId());
			String subject = "Payment GateWay Smart Payment Link -- Invoice ID "+invoice.getInvoiceId();
			String emailBody = getInvoiceBodyWithQR(invoiceDB.getShortUrl(),invoiceDB,invoiceDB.getQr());
			awsSESEmailService.invoiceEmail(emailBody, subject, invoiceDB.getEmail(), invoiceDB.getEmail(), false);
			
			//emailControllerServiceProvider.invoiceLink(getUrl(), invoiceDB.getEmail(), invoiceDB.getName());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	@SkipValidation
	public String invoiceSMS() {
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
//			setUrl(propertyManager.getSystemProperty(CrmFieldConstants.INVOICE_URL.getValue())
//					+ invoiceDB.getInvoiceId());
//			invoiceTransactionDao.update(invoiceDB);
			smsControllerServiceProvider.invoiceLink(invoiceDB.getShortUrl(),invoiceDB);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	@SuppressWarnings("unchecked")
	public void validate() {
		User user = (User) sessionMap.get(Constants.USER.getValue());
		merchantList = new UserDao().getActiveMerchantList();
		if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
			currencyMap = Currency.getAllCurrency();
			
		} else {
			currencyMap = Currency.getSupportedCurreny(user);
			if (user.getUserType().equals(UserType.SUBUSER)) {
				String parentPayId = user.getParentPayId();
				User parentUser = userDao.findPayId(parentPayId);
				currencyMap = Currency.getSupportedCurreny(parentUser);
			}
		}
		/*if ((validator.validateBlankField(invoice.getInvoiceNo()))) {
			addFieldError(CrmFieldType.INVOICE_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_NUMBER, invoice.getInvoiceNo()))) {
			addFieldError(CrmFieldType.INVOICE_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
		}*/
		if ((validator.validateBlankField(invoice.getName()))) {
			addFieldError(CrmFieldType.INVOICE_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_NAME, invoice.getName()))) {
			addFieldError(CrmFieldType.INVOICE_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(invoice.getPhone()))) {
			
		} else if (!(validator.validateField(CrmFieldType.INVOICE_PHONE, invoice.getPhone()))) {
			addFieldError(CrmFieldType.INVOICE_PHONE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(invoice.getEmail())) {
			
		} else if (!(validator.isValidEmailId(invoice.getEmail()))) {
			addFieldError(CrmFieldType.INVOICE_EMAIL.getName(), validator.getResonseObject().getResponseMessage());
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
		}
		if ((validator.validateBlankField(invoice.getTotalAmount()))) {
			addFieldError("totalAmount", validator.getResonseObject().getResponseMessage());
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
//		if ((validator.validateBlankField(invoice.getProductDesc()))) {
//		} else if (!(validator.validateField(CrmFieldType.INVOICE_DESCRIPTION, invoice.getProductDesc()))) {
//			addFieldError(CrmFieldType.INVOICE_DESCRIPTION.getName(),
//					validator.getResonseObject().getResponseMessage());
//		}
		if (invoice.getCurrencyCode().equals(CrmFieldConstants.SELECT_CURRENCY.getValue())
				|| validator.validateBlankField(invoice.getCurrencyCode())) {
			addFieldError(CrmFieldType.INVOICE_CURRENCY_CODE.getName(), CrmFieldConstants.SELECT_CURRENCY.getValue());
		} else if (!(validator.validateField(CrmFieldType.INVOICE_CURRENCY_CODE, invoice.getCurrencyCode()))) {
			addFieldError(CrmFieldType.INVOICE_CURRENCY_CODE.getName(), CrmFieldConstants.SELECT_CURRENCY.getValue());
		}
		if ((validator.validateBlankField(invoice.getDurationFrom()))) {
		} else if (!(validator.validateField(CrmFieldType.DATE_FROM, invoice.getDurationFrom()))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(invoice.getDurationTo()))) {
		} else if (!(validator.validateField(CrmFieldType.DATE_TO, invoice.getDurationTo()))) {
			addFieldError(CrmFieldType.DATE_TO.getName(), validator.getResonseObject().getResponseMessage());
		}
		/*if (invoice.getExpiresDay().isEmpty()) {
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
		}*/
		
	}
	
	public String getInvoiceBodyWithQR(String url, Invoice invoice, String base64String) {
		String body = null;
		StringBuilder content = new StringBuilder();
	
		
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append("<table width=\"350\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\""+propertiesManager.getSystemProperty("logoForEmail")+"\" alt=\"\" style=\"padding-top: 5px\" ></td></tr>");
			content.append("<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Invoice</td></tr>");
			content.append("<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Name</td>");
			content.append("<td>"+invoice.getName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Product Name</td>");
			content.append("<td>"+invoice.getProductName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Mobile</td>");
			if(invoice.getPhone()!=null){
				content.append("<td>"+invoice.getPhone()+"</td></tr>");
			}else{
				content.append("<td>"+""+"</td></tr>");
			}
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Email</td>");
			content.append("<td>"+invoice.getEmail()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
			content.append("<td>"+ invoice.getTotalAmount() +"</td></tr>");
			/*content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Payment Link</td>");
			content.append("<td>"+url+"</td></tr>");*/
			content.append("<tr align=\"center\">");
			//content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\"></td>");
			content.append("<td colspan=\"2\" style=\"border-bottom: 1px solid #ddd;\"><img src=\"data:image/jpg;base64,"+base64String+"\" width=\"150px\" height=\"150px\" alt=\"/\"></td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");
			content.append("<a href=\""+url+"\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
			content.append("<img src=\""+propertiesManager.getSystemProperty("emailerPayButton")+"\"></a>");
			content.append("</td></tr></table></td></tr>");
			content.append("<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append("<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Payment Gateway</span></td></tr>");
			content.append("<tr><td style=\"font-size: 12px;\">");
			content.append("For any queries feel free to connect with us at +91 120 433 4884. You may also drop your query to us at "
					+ "<a href=\"mailto:support@paymentgateway.com\">support@paymentgateway.com</a></td></tr>");
			content.append("<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
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

	public boolean isSmsCheck() {
		return smsCheck;
	}

	public void setSmsCheck(boolean smsCheck) {
		this.smsCheck = smsCheck;
	}
	
	
}
