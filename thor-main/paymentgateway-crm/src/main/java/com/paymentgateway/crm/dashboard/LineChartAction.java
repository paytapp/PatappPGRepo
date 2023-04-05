package com.paymentgateway.crm.dashboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class LineChartAction extends AbstractSecureAction {

	@Autowired
	private LineChartService getlineChartService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;
	
	private static Logger logger = LoggerFactory.getLogger(LineChartAction.class.getName());
	private static final long serialVersionUID = 7154448717124485623L;
	private List<PieChart> pieChart = new ArrayList<PieChart>();
	private String emailId;
	private String subMerchantId;
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String inputDays;
	private String paymentRegion;
	private boolean saleReportFlag;
	
	public String execute() {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			
//			if(inputDays.equalsIgnoreCase("custom") && dateFrom.equals(dateTo)) {
//				SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
//				Date dt = new Date();
//				String strdate = sdf2.format(dt);
//				
//				if(strdate.equals(dateFrom)) {
//					inputDays = "day";
//				}else {
//					inputDays = "previousDay";
//				}
//				
//			}
			
			
			
			if(inputDays.equalsIgnoreCase("day")){
				Calendar calendar = Calendar.getInstance();
				dateFrom=sdf1.format(calendar.getTime())+" "+"00:00:00";
//				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime())+" "+"23:59:59";
				
			}else if(inputDays.equalsIgnoreCase("week")){
				Calendar calendar = Calendar.getInstance();
//				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime())+" "+"23:59:59";
//				calendar = Calendar.getInstance();
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			        calendar.add(Calendar.DATE, -1);
			    }
				dateFrom=sdf1.format(calendar.getTime())+" "+"00:00:00";
			}
			else if(inputDays.equalsIgnoreCase("month")){
				Calendar calendar = Calendar.getInstance();
//				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime())+" "+"23:59:59";
				
				calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH,1);
				dateFrom=sdf1.format(calendar.getTime())+" "+"00:00:00";	
			}
			else if(inputDays.equalsIgnoreCase("year")){	
				Calendar calendar = Calendar.getInstance();
//				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime())+" "+"23:59:59";
				
				calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_YEAR,1);
				dateFrom=sdf1.format(calendar.getTime())+" "+"00:00:00";
				
			/*}else if(inputDays.equalsIgnoreCase("lastMonth")){	
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH,-1);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				dateFrom=sdf1.format(calendar.getTime());
				 
		        calendar.add(Calendar.MONTH, 1);  
		        calendar.set(Calendar.DAY_OF_MONTH, 1);  
		        //calendar.add(Calendar.DATE, -1);  
		        dateTo=sdf1.format(calendar.getTime());*/
			}else if(inputDays.equalsIgnoreCase("custom")){	
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate=sdf.parse(dateFrom);
				Date toDate=sdf.parse(dateTo);
				
				dateFrom=sdf1.format(fromDate)+" "+"00:00:00";
				dateTo=sdf1.format(toDate)+" "+"23:59:59";
				
			}else if(inputDays.equalsIgnoreCase("previousDay")){
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate=sdf.parse(dateFrom);
				Date toDate=sdf.parse(dateTo);
				dateFrom=sdf1.format(fromDate)+" "+"00:00:00";
				dateTo=sdf1.format(toDate)+" "+"23:59:59";
				
			}
			
			/*DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			String startDate = sdf1.format(df.parse(dateFrom));
			String endDate = sdf1.format(df.parse(dateTo));*/
			
			/*Calendar date = Calendar.getInstance();
			date.set(Calendar.DAY_OF_MONTH, 1);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			dateFrom = df.format(date.getTime());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			Date date2 = cal.getTime();
			dateTo = df.format(date2);*/

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					setPieChart(getlineChartService.preparelist(getEmailId(), getCurrency(), dateFrom,
							dateTo,sessionUser,inputDays, subMerchantId, paymentRegion, saleReportFlag));
				} else {
					setPieChart(getlineChartService.preparelist(userDao.findPayIdByEmail(getEmailId()).getPayId(), getCurrency(), dateFrom,
							dateTo, sessionUser,inputDays, subMerchantId, paymentRegion, saleReportFlag));
				}
				return SUCCESS;
			} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				// setPieChart(getlineChartService.linePreparelist(user.getFirstName(),
				// getCurrency(),getDateFrom(),getDateTo()));

				return SUCCESS;
			} else {
				if (sessionUser.getUserType().equals(UserType.MERCHANT) || sessionUser.getUserType().equals(UserType.POSMERCHANT)) {
					
					if (sessionUser.isSuperMerchant()) {
						User subMerchant = userDao.findPayIdByEmail(getEmailId());
						String subMerchantId = null;
						if(subMerchant != null)
							subMerchantId = subMerchant.getPayId();
						setPieChart(getlineChartService.preparelist(sessionUser.getPayId(), getCurrency(), dateFrom,dateTo, sessionUser,inputDays,
								subMerchantId,paymentRegion, saleReportFlag));
					}
//					else if  (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
//						setPieChart(getlineChartService.preparelist(sessionUser.getPayId(), getCurrency(), dateFrom,dateTo, sessionUser,inputDays, null));
//					}
					else {
						setPieChart(getlineChartService.preparelist(sessionUser.getPayId(), getCurrency(), dateFrom,dateTo, sessionUser,inputDays, subMerchantId,
								paymentRegion, saleReportFlag));
					}
				}else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
					String PayId=null;
					String subMerchantId=null;
					
					User parentUser = userDao.findPayId(sessionUser.getParentPayId());
					
					//superMerchant
					if(StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && parentUser.isSuperMerchant() ){
						PayId=parentUser.getPayId();
						subMerchantId=this.subMerchantId;
					}else if(StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && !parentUser.isSuperMerchant() ){
						PayId=parentUser.getSuperMerchantId();
						subMerchantId=parentUser.getPayId();
					}else{
						PayId=parentUser.getPayId();
					}
			
						setPieChart(getlineChartService.preparelist(PayId, getCurrency(), dateFrom,dateTo, sessionUser,inputDays, subMerchantId,
								paymentRegion, saleReportFlag));
					
				}else if(sessionUser.getUserType().equals(UserType.PARENTMERCHANT)){
					setPieChart(getlineChartService.preparelist("", getCurrency(), dateFrom,dateTo, sessionUser,inputDays, subMerchantId,
							paymentRegion, saleReportFlag));
				}else {
					
					if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
						setPieChart(getlineChartService.preparelist(getEmailId(), getCurrency(), dateFrom,
								dateTo, sessionUser,inputDays, subMerchantId, paymentRegion, saleReportFlag));
					} else {
						setPieChart(getlineChartService.preparelist(userDao.getPayIdByEmailId(getEmailId()),
								getCurrency(), dateFrom, dateTo, sessionUser,inputDays, subMerchantId, paymentRegion, saleReportFlag));
					}
				}
				return SUCCESS;
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	
	public void validate() {

		if (!(validator.validateBlankField(getEmailId())) &&  !getEmailId().equalsIgnoreCase("ALL MERCHANTS")) {
			if (!(validator.validateField(CrmFieldType.EMAILID, getEmailId()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getSubMerchantId()))) {
			if (!(validator.validateField(CrmFieldType.PAY_ID, getSubMerchantId()))) {
				addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getCurrency()))) {
			if (!(validator.validateField(CrmFieldType.CURRENCY, getCurrency()))) {
				addFieldError(CrmFieldType.CURRENCY.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getDateFrom()))) {
			if (!(validator.validateField(CrmFieldType.DATE_FROM, getDateFrom()))) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!validator.validateBlankField(getDateTo())) {
			if (DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom()))
					.compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
			} else if (DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
			}
		}

		if (!(validator.validateBlankField(getInputDays()))) {
			if (!(validator.validateField(CrmFieldType.MERCHANT, getInputDays()))) {
				addFieldError(CrmFieldType.MERCHANT.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPaymentRegion()))) {
			if (!(validator.validateField(CrmFieldType.PAYMENTS_REGION, getPaymentRegion()))) {
				addFieldError(CrmFieldType.PAYMENTS_REGION.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		
	}
	public List<PieChart> getPieChart() {
		return pieChart;
	}

	public void setPieChart(List<PieChart> pieChart) {
		this.pieChart = pieChart;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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

	public LineChartService getGetlineChartService() {
		return getlineChartService;
	}

	public void setGetlineChartService(LineChartService getlineChartService) {
		this.getlineChartService = getlineChartService;
	}

	public String getInputDays() {
		return inputDays;
	}

	public void setInputDays(String inputDays) {
		this.inputDays = inputDays;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public boolean isSaleReportFlag() {
		return saleReportFlag;
	}

	public void setSaleReportFlag(boolean saleReportFlag) {
		this.saleReportFlag = saleReportFlag;
	}


}
