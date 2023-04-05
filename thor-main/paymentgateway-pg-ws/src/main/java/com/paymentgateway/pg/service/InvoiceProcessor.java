package com.paymentgateway.pg.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.CountryCodes;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PromotionalPaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class InvoiceProcessor {

	@Autowired
	UserDao userDao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private QRCodeCreator qRCodeCreator;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private InvoiceTransactionDao invoiceTransactionDao;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;

	@Autowired
	private AWSSESEmailService awsSESEmailService;

	@Autowired
	private GeneralValidator validator;
	
	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;
	
	@Autowired
	private UserSettingDao userSettingDao;
	
	private static Logger logger = LoggerFactory.getLogger(InvoiceProcessor.class.getName());

	public Map<String, String> process(Fields fields) {

		User merchant = null;
		Map<String, String> responseMap = new HashMap<String, String>();
		BigDecimal LocaltotalAmount;
		String url = "";
		String superMerchantId = null;
		Boolean isSubMerchant = false;
		
		int MIN_AMOUNT_SIZE = 3;
		
		try {

			Invoice invoice = new Invoice();
			
			// Verify if its a Super-Merchant or a Sub-Merchant
			if (StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))) {
				validator.validateField(FieldType.PAY_ID, FieldType.PAY_ID.getName(), fields);
				
				
				if (StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {

					validator.validateField(FieldType.HASH, FieldType.HASH.getName(), fields);

					String providedHash = fields.get("HASH");
					fields.remove("HASH");
				
					String calculatedHash = Hasher.getHash(fields);
					if (!providedHash.equalsIgnoreCase(calculatedHash)) {
						logger.info("Invalid hash, providedHash=" + providedHash + "and calculatedHash=" + calculatedHash);
						responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
						responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
						return responseMap;
					}
				} else {
					logger.info("Hash is missing from the request");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Hash not present in request");
					return responseMap;
				}
				
				
				merchant = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				

				if (merchant == null) {
					logger.info("Invalid Pay Id , no such user found");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MERCHANT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid PAY_ID , no such user found");
					return responseMap;
				}
				
				
				if (!merchant.getUserStatus().equals(UserStatusType.ACTIVE)) {

					logger.info("Merchant is not active");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MERCHANT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid PAY_ID , Merchant is not active");
					return responseMap;
				}
				
				// This is a super merchant 
				if (merchant.isSuperMerchant() && StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					logger.info("Super Merchant Cannot initiate an invoice");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MERCHANT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Super Merchant Cannot initiate an invoice");
					return responseMap;
				}
				
				// This is a sub merchant 
				if (!merchant.isSuperMerchant() && StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					isSubMerchant = true;
					superMerchantId = merchant.getSuperMerchantId();
					invoice.setSubMerchantId(merchant.getPayId());
				}
				
			}
						
			
			

			if (StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))) {

				validator.validateField(FieldType.PAY_ID, FieldType.PAY_ID.getName(), fields);

				if (merchant == null) {
					logger.info("Invalid Pay Id , no such user found");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MERCHANT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid PAY_ID , no such user found");
					return responseMap;
				}

				if (!merchant.getUserStatus().equals(UserStatusType.ACTIVE)) {

					logger.info("Merchant is not active");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MERCHANT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid PAY_ID , Merchant is not active");
					return responseMap;
				}

			} else {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "PAY_ID is missing from the request");
				logger.info("PayId is missing from the request");
				return responseMap;
			}

			if (isSubMerchant) {
				String superMerchPayId = userDao.getPayIdBySuperMerchId(superMerchantId);
				
				if (StringUtils.isBlank(superMerchPayId)) {
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Super Merchant for this merchant is Inactive");
					logger.info("Super Merchant for this merchant is Inactive");
					return responseMap;
				}
				else {
					invoice.setPayId(superMerchPayId);
					merchant.setPayId(superMerchPayId);
				}
				
			}
			
			invoice.setBusinessName(merchant.getBusinessName());

			if (StringUtils.isNotBlank(fields.get(FieldType.NAME.getName()))) {

				validator.validateField(FieldType.NAME, FieldType.NAME.getName(), fields);
				invoice.setName(fields.get(FieldType.NAME.getName()));
			} else {
				logger.info("Name is missing from the request");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Name is missing from the request");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.PHONE.getName()))
					|| StringUtils.isNotBlank(fields.get(FieldType.EMAIL.getName()))) {

				if (StringUtils.isNotBlank(fields.get(FieldType.PHONE.getName()))) {
					validator.validateField(FieldType.PHONE, FieldType.PHONE.getName(), fields);
					if(validatePhoneLength(fields.get(FieldType.PHONE.getName())) == null) {
						logger.info("Invalid Phone Length from the request ");
						responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
						responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Phone Length from the request");
						return responseMap;
					}
					invoice.setPhone(fields.get(FieldType.PHONE.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.EMAIL.getName()))) {
					validator.validateField(FieldType.EMAIL, FieldType.EMAIL.getName(), fields);
					invoice.setEmail(fields.get(FieldType.EMAIL.getName()));
				}
			} else {
				logger.info("Phone, Email - Atleast one parameter is needed ");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						"Phone, Email - Atleast one parameter is needed ");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.PRODUCT_NAME.getName()))) {

				validator.validateField(FieldType.PRODUCT_NAME, FieldType.PRODUCT_NAME.getName(), fields);
				invoice.setProductName(fields.get(FieldType.PRODUCT_NAME.getName()));
			} else {
				logger.info("Product Name is missing from the request ");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Product Name is missing from the request");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.PRODUCT_DESCRIPTION.getName()))) {
				validator.validateField(FieldType.PRODUCT_DESCRIPTION, FieldType.PRODUCT_DESCRIPTION.getName(), fields);
				invoice.setProductDesc(fields.get(FieldType.PRODUCT_DESCRIPTION.getName()));
			} else {
				logger.info("Product Description is missing from the request");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						"Product Description is missing from the request");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.DURATION_FROM.getName()))) {

				validator.validateField(FieldType.DURATION_FROM, FieldType.DURATION_FROM.getName(), fields);
				if(validateDate(fields.get(FieldType.DURATION_FROM.getName())) == null) {
					logger.info("Invalid Duration_From Date from the request ");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Duration_From Date from the request");
					return responseMap;
				}
				invoice.setDurationFrom(fields.get(FieldType.DURATION_FROM.getName()));

			} else {
				invoice.setDurationFrom("");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.DURATION_TO.getName()))) {

				validator.validateField(FieldType.DURATION_TO, FieldType.DURATION_TO.getName(), fields);
				if(validateDate(fields.get(FieldType.DURATION_TO.getName())) == null) {
					logger.info("Invalid Duration_To Date from the request ");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Duration_To Date from the request");
					return responseMap;
				}
				invoice.setDurationTo(fields.get(FieldType.DURATION_TO.getName()));

			} else {
				invoice.setDurationTo("");

			}

			if (StringUtils.isNotBlank(fields.get(FieldType.EXPIRY_DATE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.EXPIRY_TIME.getName()))) {

				validator.validateField(FieldType.EXPIRY_DATE, FieldType.EXPIRY_DATE.getName(), fields);
				validator.validateField(FieldType.EXPIRY_TIME, FieldType.EXPIRY_TIME.getName(), fields);
				if(validateDate(fields.get(FieldType.EXPIRY_DATE.getName())) == null) {
					logger.info("Invalid Expiry Date from the request ");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Expiry Date from the request");
					return responseMap;
				}
				if(validateTime(fields.get(FieldType.EXPIRY_TIME.getName())) == null) {
					logger.info("Invalid Expiry Time from the request ");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Expiry Time from the request");
					return responseMap;
				}
				
				invoice.setExpiresDay(fields.get(FieldType.EXPIRY_DATE.getName()) + " "
						+ fields.get(FieldType.EXPIRY_TIME.getName()));
			} else {
				logger.info("Expiry Date or Expiry time is missing from the request");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						"EXPIRY_DATE or EXPIRY_TIME is missing from the request");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CURRENCY.getName()))) {
				validator.validateField(FieldType.CURRENCY, FieldType.CURRENCY.getName(), fields);
				invoice.setCurrencyCode(
						Currency.getNumericCode(fields.get(FieldType.CURRENCY.getName()).toUpperCase()));

			} else {
				logger.info("CURRENCY is missing from the request");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "CURRENCY is missing from the request");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.QUANTITY.getName()))) {
				validator.validateField(FieldType.QUANTITY, FieldType.QUANTITY.getName(), fields);
				invoice.setQuantity(fields.get(FieldType.QUANTITY.getName()));
			} else {
				invoice.setQuantity("1");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))) {
				
				if(fields.get(FieldType.AMOUNT.getName()).length() < MIN_AMOUNT_SIZE){
					logger.info("Invalid Amount in request");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid amount in request");
					return responseMap;
				}
				validator.validateField(FieldType.AMOUNT, FieldType.AMOUNT.getName(), fields);
				
				String amountToDec = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), "356");
				invoice.setAmount(amountToDec);
			} else {
				logger.info("AMOUNT is missing from the request");
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "AMOUNT is missing from the request");
				return responseMap;
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.SERVICE_CHARGE.getName()))) {
				
				if(fields.get(FieldType.SERVICE_CHARGE.getName()).length() < MIN_AMOUNT_SIZE){
					logger.info("Invalid Service Charge Amount size "+fields.get(FieldType.SERVICE_CHARGE.getName()));
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid service Charge amount in request");
					return responseMap;
				}
				
				validator.validateField(FieldType.SERVICE_CHARGE, FieldType.SERVICE_CHARGE.getName(), fields);
				
				String serviceChargeToDec = Amount.toDecimal(fields.get(FieldType.SERVICE_CHARGE.getName()), "356");
				invoice.setServiceCharge(serviceChargeToDec);
			} else {
				invoice.setServiceCharge("0.00");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.ADDRESS.getName()))) {
				validator.validateField(FieldType.ADDRESS, FieldType.ADDRESS.getName(), fields);
				invoice.setAddress(fields.get(FieldType.ADDRESS.getName()));
			} else {
				invoice.setAddress("");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.COUNTRY.getName()))) {
				validator.validateField(FieldType.COUNTRY, FieldType.COUNTRY.getName(), fields);
				if(CountryCodes.getInstanceIgnoreCase(fields.get(FieldType.COUNTRY.getName())) == null) {
					logger.info("Country Name is Not Valid ");
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Country Name is Not Valid in request");
					return responseMap;
				}
				invoice.setCountry(fields.get(FieldType.COUNTRY.getName()));
			} else {
				invoice.setCountry("");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.STATE.getName()))) {
				validator.validateField(FieldType.STATE, FieldType.STATE.getName(), fields);
				invoice.setState(fields.get(FieldType.STATE.getName()));
			} else {
				invoice.setState("");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CITY.getName()))) {
				validator.validateField(FieldType.CITY, FieldType.CITY.getName(), fields);
				invoice.setCity(fields.get(FieldType.CITY.getName()));
			} else {
				invoice.setCity("");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.PIN.getName()))) {
				validator.validateField(FieldType.PIN, FieldType.PIN.getName(), fields);
				invoice.setZip(fields.get(FieldType.PIN.getName()));
			} else {
				invoice.setZip("");
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			invoice.setFileName("API");
			invoice.setInvoiceType(PromotionalPaymentType.INVOICE_PAYMENT.getName());
			invoice.setInvoiceId(TransactionManager.getNewTransactionId());
			invoice.setCreateDate(sdf.format(date));
			invoice.setPayId(merchant.getPayId());
			invoice.setSaltKey(invoice.getInvoiceId());
			invoice.setStatus("Active");
			BigDecimal amount1 = new BigDecimal(invoice.getAmount());
			BigDecimal service1 = new BigDecimal(invoice.getServiceCharge());
			LocaltotalAmount = ((amount1.multiply(new BigDecimal(invoice.getQuantity()))
					.setScale(2, BigDecimal.ROUND_UP).add(service1.setScale(2, BigDecimal.ROUND_UP))));
			String AmountInString = String.valueOf(LocaltotalAmount.setScale(2, BigDecimal.ROUND_UP));
			invoice.setTotalAmount(AmountInString);
			invoice.setReturnUrl("");
			if (invoice.getReturnUrl().isEmpty()) {
				invoice.setReturnUrl(
						propertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_RETURN_URL.getValue()));
			}
			url = propertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_URL.getValue())
					+ invoice.getInvoiceId();
			invoice.setLongUrl(url);
			invoice.setShortUrl(bitlyUrlShortener.createShortUrlUsingBitly(url));

			BufferedImage image = qRCodeCreator.generateQRCode(invoice);
			File file = new File(invoice.getInvoiceId()+".png");
			ImageIO.write(image, "png", file);
			invoice.setQr(base64EncodeDecode.base64Encoder(file));
			file.delete();
			invoiceTransactionDao.create(invoice);
			
			UserSettingData userSetting = null;
			if(StringUtils.isNotBlank(invoice.getSubMerchantId())){
				userSetting = userSettingDao.fetchDataUsingPayId(invoice.getSubMerchantId());
			}else{
				userSetting = userSettingDao.fetchDataUsingPayId(invoice.getPayId());
			}
			
			invoice.setEmailStatus(userSetting.isAllowInvoiceEmail());
			invoice.setSmsStatus(userSetting.isAllowInvoiceSms());
			
			logger.info("invoice Id from API "+invoice.getInvoiceId()+ " email flag = "+userSetting.isAllowInvoiceEmail()+" SMS flag = "+userSetting.isAllowInvoiceSms());
			
			if(userSetting.isAllowInvoiceEmail() || userSetting.isAllowInvoiceSms()){
			// Thread For Sending Email & SMS for Invoice.
			Runnable r = new Runnable() {
				public synchronized void run() {	
					List<Invoice> pendingInvoiceList = new ArrayList<>();
					pendingInvoiceList.add(invoice);

					for (Invoice in : pendingInvoiceList) {
						try {
							boolean emailStatus = invoice.isEmailStatus();
							boolean smsStatus = invoice.isSmsStatus();
							
							if (StringUtils.isNotBlank(in.getPhone()) && smsStatus) {
								if (!(in.getPhone().equalsIgnoreCase("0"))) {
									if (smsControllerServiceProvider.invoiceSms(in.getShortUrl(), in)) {
										smsStatus = true;
									}
								} else {
									logger.info("SMS not sent to " + in.getPhone());
								}
							}
							if (StringUtils.isNotBlank(in.getEmail()) && emailStatus) {
								String subject = "Payment Gateway Smart Payment Link -- Invoice ID " + in.getInvoiceId();
								String emailBody = getInvoiceBodyWithQR(in);
								if (awsSESEmailService.invoiceEmail(emailBody, subject, in.getEmail(), in.getEmail(), false)) {
									emailStatus = true;
								} else {

									logger.info("Email not sent to " + in.getEmail());
								}
							}
						} catch (Exception e) {
							logger.error("Exception : " , e);
						}
					}
				}

				public String getInvoiceBodyWithQR(Invoice invoice) {
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
						content.append("<tr align=\"center\">");
						content.append("<td colspan=\"2\" style=\"border-bottom: 1px solid #ddd;\"><img src=\"data:image/jpg;base64,"+invoice.getQr()+"\" width=\"150px\" height=\"150px\" alt=\"/\"></td></tr>");
						content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");
						content.append("<a href=\""+invoice.getLongUrl()+"\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
						content.append("<img src=\""+propertiesManager.getSystemProperty("emailerPayButton")+"\"></a>");
						content.append("</td></tr></table></td></tr>");
						content.append("<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
						content.append("<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
						content.append("<span style=\"display: block;\">Team Payment GateWay</span></td></tr>");
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
			};
			Thread t = new Thread(r);
			t.start();
			}

			logger.info("Invoice generated and sent");
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
			responseMap.put(FieldType.INVOICE_URL.getName(), invoice.getShortUrl());
			responseMap.put(FieldType.ORDER_ID.getName(), invoice.getInvoiceId());
			return responseMap;
		}

		catch (SystemException se) {
			logger.error("Exception", se);
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), se.getMessage());
			return responseMap;
		} catch (Exception e) {
			logger.error("Exception", e);
		}
		return null;
	}
	public String validateDate(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		if(StringUtils.isNotBlank(date) && date.length()==10) {
			if(date.contains("/")) {
				String dateArray[] = date.split("/");
				String currDate = dateFormat.format(new Date());
				String currDateArray[] = currDate.split("-");
				if(Integer.valueOf(currDateArray[2]) > Integer.valueOf(dateArray[2])) {
					return null;
				} else if((Integer.valueOf(currDateArray[1]) > Integer.valueOf(dateArray[1])) || Integer.valueOf(dateArray[1]) > 12){
					return null;
				} else if(Integer.valueOf(currDateArray[0]) > Integer.valueOf(dateArray[0]) || Integer.valueOf(dateArray[0]) > 31){
					return null;
				}
				
			}else if(date.contains("-")) {
				String dateArray[] = date.split("-");
				String currDate = dateFormat.format(new Date());
				String currDateArray[] = currDate.split("-");
				if(Integer.valueOf(currDateArray[2]) > Integer.valueOf(dateArray[2])) {
					return null;
				} else if((Integer.valueOf(currDateArray[1]) > Integer.valueOf(dateArray[1])) || Integer.valueOf(dateArray[1]) > 12){
					return null;
				} else if(Integer.valueOf(currDateArray[0]) > Integer.valueOf(dateArray[0]) || Integer.valueOf(dateArray[0]) > 31){
					return null;
				}
			}else {
				return null;
			}
			
			
		}else {
			return null;
		}
		
		
		return "valid";
	}
	
	public String validateTime(String time) {

		if (time.contains(":")) {
			String timeArray[] = time.split(":");
			if (Integer.valueOf(timeArray[0]) > 23) {
				return null;
			} else if (Integer.valueOf(timeArray[1]) > 59) {
				return null;
			}

		} else {
			return null;
		}

		return "valid";
	}
	
	public String validatePhoneLength(String phone) {

//		if (phone.length() < 10 || phone.length() > 12) {
//			return null;
//		}
		if (phone.length() != 10) {
			return null;
		}

		return "valid";
	}
}
