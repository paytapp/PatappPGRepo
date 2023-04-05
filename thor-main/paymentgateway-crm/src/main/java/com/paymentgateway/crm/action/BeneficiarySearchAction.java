package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.dao.BeneficiaryAccountsDao;
import com.paymentgateway.commons.user.BeneficiaryAccounts;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shaiwal
 *
 */

public class BeneficiarySearchAction extends AbstractSecureAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2820507225850677667L;


	@Autowired
	private BeneficiaryAccountsDao beneficiaryAccountsDao;
	
	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;
	
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private CrmEmailer crmEmailer;
	
	@Autowired
	BitlyUrlShortener bitlyUrlShortener;
	
	@Autowired
	AWSSESEmailService awsSESEmailService;
	
	private static Logger logger = LoggerFactory.getLogger(BeneficiarySearchAction.class.getName());

	private String acquirer;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private String response;
	private List<BeneficiaryAccounts> aaData;
	private User sessionUser = new User();
	
	private String beneficiaryCd;
	private String srcAccountNo;
	private String beneName;
	private String beneAccountNo;
	private String ifscCode;
	private String paymentType;
	private String beneType;
	private String currencyCode;
	private String custId;
	private String bankName;
	private String emailId;
	private String mobileNo;
	private String payId;
	private String responseMessage;
	
	public String execute() {

		logger.info("Inside BeneficiarySearchAction , execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		
		try {
			if (StringUtils.isNotBlank(acquirer)) {
				
				if (acquirer.equalsIgnoreCase("")) {
					acquirer = "ALL";
				}
			}
			else {
				acquirer = "ALL";
			}
			setAcquirer(acquirer);
			if (sessionUser.getUserType().equals(UserType.ADMIN ) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				if (acquirer.equalsIgnoreCase("ALL")) {
					
					beneficiaryAccountsList = beneficiaryAccountsDao.getAllBeneficiaryAccountsList();
				}
				else {
					beneficiaryAccountsList = beneficiaryAccountsDao.getBeneficiaryAccountsListByAcquirer(acquirer);
				}
				totalCount = beneficiaryAccountsList.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = beneficiaryAccountsList;
				recordsFiltered = recordsTotal;
				
				return SUCCESS;
			}


		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;

	}

	public String getActiveBeneficiaries() {
		
		logger.info("Inside getActiveBeneficiaries , execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		
		try {
			if (StringUtils.isNotBlank(acquirer)) {
				
				if (acquirer.equalsIgnoreCase("")) {
					acquirer = "ALL";
				}
			}
			else {
				acquirer = "ALL";
			}
			setAcquirer(acquirer);
			if (sessionUser.getUserType().equals(UserType.ADMIN ) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				if (acquirer.equalsIgnoreCase("ALL")) {
					
					//beneficiaryAccountsList = beneficiaryAccountsDao.getAllActiveBeneficiaryAccountsList();
				}
				else {
					beneficiaryAccountsList = beneficiaryAccountsDao.getActiveBeneficiaryAccountsListByAcquirer(acquirer);
				}
				totalCount = beneficiaryAccountsList.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = beneficiaryAccountsList;
				recordsFiltered = recordsTotal;
				
				return SUCCESS;
			}


		} catch (Exception exception) {
			logger.error("Exception", exception);
			return SUCCESS;
		}
		return SUCCESS;
		
	}
	
	public String sendVerificationLinkToCustomer() {
		
		String longUrl = "";
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.ADMIN ) || sessionUser.getUserType().equals(UserType.SUBADMIN)
				||(sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant())) {
			longUrl = propertiesManager.propertiesMap.get(CrmFieldConstants.BENE_VERIFICATION_LINK.getValue()) + payId;
		}else {
			longUrl = propertiesManager.propertiesMap.get(CrmFieldConstants.BENE_VERIFICATION_LINK.getValue()) + sessionUser.getPayId();
		}
		
		String shortUrl = bitlyUrlShortener.createShortUrlUsingBitly(longUrl);
		String subject = "Account Verification Link";
		try {
			if(StringUtils.isNotBlank(emailId) && StringUtils.isNotBlank(mobileNo)) {
				
				String emailBody = getVerificationLinkEmailBody(shortUrl, longUrl);
				awsSESEmailService.invoiceEmail(emailBody, subject, emailId, emailId, false);
				
				smsControllerServiceProvider.sendVerificationLinkSMS(mobileNo, shortUrl);
			}else if(StringUtils.isNotBlank(emailId)) {
				
				String emailBody = getVerificationLinkEmailBody(shortUrl, longUrl);
				awsSESEmailService.invoiceEmail(emailBody, subject, emailId, emailId, false);
				
			}else if(StringUtils.isNotBlank(mobileNo)){
				
				smsControllerServiceProvider.sendVerificationLinkSMS(mobileNo, shortUrl);
				
			}
			setResponseMessage("Link has been sent successfully");
			return SUCCESS;
		}catch(Exception ex) {
			logger.info("Exception cought while sending mail or sms : " + ex);
			setResponseMessage("Some Thing Went wrong !");
			return ERROR;
		}
		
	}
	
	
	public String getVerificationLinkEmailBody(String shortUrl, String longUrl) throws Exception{
		//TODO Add custName if available into the email
		StringBuilder body = new StringBuilder();

		body.append(
				"<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		body.append(
				"<title>Account Verification Link</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
		body.append(
				"<body><table width=\"350\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\"><tbody><tr>");
		body.append("<td align=\"center\"><img src=" + propertiesManager.getSystemProperty("logoForEmail")
				+ " alt=\"\" ></td></tr>");
		body.append("<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Account Verification Link</td></tr>");
		body.append("<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
		body.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
		body.append("Dear Customer, Please click on the below link to verify your Bank Account </tr>");
		body.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");
		if(StringUtils.isNotBlank(shortUrl)) {
			body.append("<a href=\""+shortUrl+"\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
		}else {
			body.append("<a href=\""+longUrl+"\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
		}
		body.append("<img src=" + propertiesManager.getSystemProperty("acVerifyButton") + " alt=\"\" ></td></tr></table></td></tr>");
		body.append("<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\">");
		body.append("<tr><td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br><span style=\"display: block;\">Team Payment Gateway</span></td>");
		body.append("</tr><tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2021 www.paymentGateway.com All rights reserved.</td>");
		body.append("</tr><tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">Please reach us at support@paymentGateway.com in case of queries.</td>");
		body.append("</tr></table></td></tr></tbody></table></body></html>");

		return body.toString();
	}
	
	
//	public String getVerificationLinkSmsBody(String shortUrl, String longUrl) {
//		StringBuilder message = new StringBuilder();
//		
//		message.append("Dear User \n");
//		message.append("Please click on the link below To verify your account \n ");
//		if (StringUtils.isNotBlank(shortUrl)) {
//			message.append(shortUrl);
//		} else {
//			message.append(longUrl);
//		}
//		logger.info("Bene verification link SMS : " + message.toString());
//		
//		return message.toString();
//	}
	
	public void validate() {

	}

	public List<BeneficiaryAccounts> getAaData() {
		return aaData;
	}

	public void setAaData(List<BeneficiaryAccounts> aaData) {
		this.aaData = aaData;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getBeneficiaryCd() {
		return beneficiaryCd;
	}

	public void setBeneficiaryCd(String beneficiaryCd) {
		this.beneficiaryCd = beneficiaryCd;
	}

	public String getSrcAccountNo() {
		return srcAccountNo;
	}

	public void setSrcAccountNo(String srcAccountNo) {
		this.srcAccountNo = srcAccountNo;
	}

	public String getBeneName() {
		return beneName;
	}

	public void setBeneName(String beneName) {
		this.beneName = beneName;
	}

	public String getBeneAccountNo() {
		return beneAccountNo;
	}

	public void setBeneAccountNo(String beneAccountNo) {
		this.beneAccountNo = beneAccountNo;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getBeneType() {
		return beneType;
	}

	public void setBeneType(String beneType) {
		this.beneType = beneType;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

}
