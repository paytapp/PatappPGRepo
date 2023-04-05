package com.paymentgateway.crm.mailers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.MailersObject;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class MailerAction extends AbstractSecureAction{

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private MailerService mailerService;
	
	private static final long serialVersionUID = 5930234093985924050L;
	private static Logger logger = LoggerFactory.getLogger(MailerAction.class.getName());
	
	
    private String settlementDate;
    private String mailType;
	private String userType;
	private String paymentType;
	private String dateFrom;
	private String dateTo;
	private String timeFrom;
	private String timeTo;
	private String settlementTime;
	private String isDateRequiredFlag;
	private String response;
	
	
	public String execute() {
		
		List<String> adminEmailId = new ArrayList<String>();;
		List<String> subAdminEmailList;
		List<String> superMerchantEmailList;
		List<String> merchantEmailList;
		List<String> subMerchantEmailList;
		List<String> subUserEmailList;
		List<String> resellersEmailList;
		
		DateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
		DateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy");
		
		String []dateTimeFromArray = dateFrom.split(" ");
		String []dateTimeToArray = dateTo.split(" ");
		
		timeFrom = dateTimeFromArray[1];
		timeTo = dateTimeToArray[1];
		
		
		try {
			if(StringUtils.isNotEmpty(settlementDate)) {
				String []dateTimeSettleArray = settlementDate.split(" ");
				settlementTime = dateTimeSettleArray[1];
				settlementDate = outFormat.format(inFormat.parse(dateTimeSettleArray[0]));
			}
		    
			dateFrom = outFormat.format(inFormat.parse(dateTimeFromArray[0]));
		    
		    dateTo = outFormat.format(inFormat.parse(dateTimeToArray[0]));
		    
		    
			
			
			
			if(mailType.equalsIgnoreCase("maintenanceActivity")) {
				logger.info("Inside 'maintenanceActivity' condition ");
				if(StringUtils.isNotEmpty(userType) && !userType.equalsIgnoreCase("ALL")) {
					String userListArray[] = userType.split(",");
					
					for(String usrTyp : userListArray) {
						MailersObject mailersObject = new MailersObject();
						if(usrTyp.equalsIgnoreCase("Admin")) {
							
							adminEmailId = userDao.getAllAdminsEmail();
							mailersObject.setToEmailList(adminEmailId);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SubAdmin")) {
							
							subAdminEmailList = userDao.getAllSubAdminsEmail();
							mailersObject.setToEmailList(subAdminEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SuperMerchant")) {
							superMerchantEmailList = userDao.getAllSuperMerchantsEmail();
							mailersObject.setToEmailList(superMerchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
							
						}else if(usrTyp.equalsIgnoreCase("Merchant")) {
							merchantEmailList = userDao.getAllNormalMerchantsEmail();
							mailersObject.setToEmailList(merchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SubMerchant")) {
							subMerchantEmailList = userDao.getAllSubMerchantsEmail();
							mailersObject.setToEmailList(subMerchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SubUser")) {
							subUserEmailList = userDao.getAllSubUsersEmail();
							mailersObject.setToEmailList(subUserEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
							
						}else if(usrTyp.equalsIgnoreCase("Reseller")) {
							resellersEmailList = userDao.getAllResellersEmail();
							mailersObject.setToEmailList(resellersEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
						}
					}
					
				}else {
					logger.info("Inside 'maintenanceActivity' condition for ALL");
					
					MailersObject mailersObject = new MailersObject();
					mailersObject.setToEmailList(userDao.getAllAdminsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubAdminsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSuperMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllNormalMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubUsersEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllResellersEmail());
					
					mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", "", "");
				}
			}else if(mailType.equalsIgnoreCase("settlementDelay")) {
				logger.info("Inside 'settlementDelay' condition ");
				if(StringUtils.isNotEmpty(userType) && !userType.equalsIgnoreCase("ALL")) {
					String userListArray[] = userType.split(",");
					
					for(String usrTyp : userListArray) {
						MailersObject mailersObject = new MailersObject();
						if(usrTyp.equalsIgnoreCase("Admin")) {
							
							adminEmailId = userDao.getAllAdminsEmail();
							mailersObject.setToEmailList(adminEmailId);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SubAdmin")) {
							
							subAdminEmailList = userDao.getAllSubAdminsEmail();
							mailersObject.setToEmailList(subAdminEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SuperMerchant")) {
							superMerchantEmailList = userDao.getAllSuperMerchantsEmail();
							mailersObject.setToEmailList(superMerchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
							
						}else if(usrTyp.equalsIgnoreCase("Merchant")) {
							merchantEmailList = userDao.getAllNormalMerchantsEmail();
							mailersObject.setToEmailList(merchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SubMerchant")) {
							subMerchantEmailList = userDao.getAllSubMerchantsEmail();
							mailersObject.setToEmailList(subMerchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
							
						}else if(usrTyp.equalsIgnoreCase("SubUser")) {
							subUserEmailList = userDao.getAllSubUsersEmail();
							mailersObject.setToEmailList(subUserEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
							
						}else if(usrTyp.equalsIgnoreCase("Reseller")) {
							resellersEmailList = userDao.getAllResellersEmail();
							mailersObject.setToEmailList(resellersEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
						}
					}
					
				}else {
					logger.info("Inside 'settlementDelay' condition for ALL");
					
					MailersObject mailersObject = new MailersObject();
					mailersObject.setToEmailList(userDao.getAllAdminsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubAdminsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSuperMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllNormalMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubUsersEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllResellersEmail());
					
					mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, settlementDate, settlementTime, "", "");
					
				}
			}else if(mailType.equalsIgnoreCase("paymentTypeDown")){
				logger.info("Inside 'paymentTypeDown' condition ");
				if(StringUtils.isNotEmpty(userType) && !userType.equalsIgnoreCase("ALL")) {
					String userListArray[] = userType.split(",");
					
					for(String usrTyp : userListArray) {
						MailersObject mailersObject = new MailersObject();
						if(usrTyp.equalsIgnoreCase("Admin")) {
							
							adminEmailId = userDao.getAllAdminsEmail();
							mailersObject.setToEmailList(adminEmailId);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
							
						}else if(usrTyp.equalsIgnoreCase("SubAdmin")) {
							
							subAdminEmailList = userDao.getAllSubAdminsEmail();
							mailersObject.setToEmailList(subAdminEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
							
						}else if(usrTyp.equalsIgnoreCase("SuperMerchant")) {
							superMerchantEmailList = userDao.getAllSuperMerchantsEmail();
							mailersObject.setToEmailList(superMerchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
							
						}else if(usrTyp.equalsIgnoreCase("Merchant")) {
							merchantEmailList = userDao.getAllNormalMerchantsEmail();
							mailersObject.setToEmailList(merchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
							
						}else if(usrTyp.equalsIgnoreCase("SubMerchant")) {
							subMerchantEmailList = userDao.getAllSubMerchantsEmail();
							mailersObject.setToEmailList(subMerchantEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
							
						}else if(usrTyp.equalsIgnoreCase("SubUser")) {
							subUserEmailList = userDao.getAllSubUsersEmail();
							mailersObject.setToEmailList(subUserEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
							
						}else if(usrTyp.equalsIgnoreCase("Reseller")) {
							resellersEmailList = userDao.getAllResellersEmail();
							mailersObject.setToEmailList(resellersEmailList);
							mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
						}
					}
					
				}else {
					logger.info("Inside 'paymentTypeDown' condition for ALL");
					
					MailersObject mailersObject = new MailersObject();
					mailersObject.setToEmailList(userDao.getAllAdminsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubAdminsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSuperMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllNormalMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubMerchantsEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllSubUsersEmail());
					mailersObject.getToEmailList().addAll(userDao.getAllResellersEmail());
					
					mailerService.emailGenerator(mailersObject, mailType, dateFrom, dateTo, timeFrom, timeTo, "", "", paymentType, isDateRequiredFlag);
				}
			}
			addActionMessage(CrmFieldConstants.MAIL_SENT_SUCCESSFULLY.getValue());
			return SUCCESS;
		}catch(Exception ex) {
			logger.error("Exception Cought in MailerAction : " , ex);
			addActionError(CrmFieldConstants.MAIL_NOT_SENT.getValue());
			return ERROR;
		}
		
	}
	
	
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getMailType() {
		return mailType;
	}
	public void setMailType(String mailType) {
		this.mailType = mailType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
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
	public String getTimeFrom() {
		return timeFrom;
	}
	public void setTimeFrom(String timeFrom) {
		this.timeFrom = timeFrom;
	}
	public String getTimeTo() {
		return timeTo;
	}
	public void setTimeTo(String timeTo) {
		this.timeTo = timeTo;
	}
	public String getSettlementDate() {
		return settlementDate;
	}
	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getSettlementTime() {
		return settlementTime;
	}
	public void setSettlementTime(String settlementTime) {
		this.settlementTime = settlementTime;
	}
	public String getIsDateRequiredFlag() {
		return isDateRequiredFlag;
	}
	public void setIsDateRequiredFlag(String isDateRequiredFlag) {
		this.isDateRequiredFlag = isDateRequiredFlag;
	}
	
	
		
}
