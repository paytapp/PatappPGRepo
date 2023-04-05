package com.paymentgateway.crm.dashboard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MerchantInitiatedDirectDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class StatisticsAction extends AbstractSecureAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2179649888987858770L;

	@Autowired
	StatisticsService getStatistics;

	@Autowired
	private CrmValidator validator;
	@Autowired
	BarChartQuery barChartQuery;
	@Autowired
	private UserDao userDao;
	@Autowired
	private MerchantInitiatedDirectDao merchantInitiatedDirectDao;

	private static Logger logger = LoggerFactory.getLogger(StatisticsAction.class.getName());

	private Statistics statistics = new Statistics();
	private Statistics payOut = new Statistics();
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

/*			DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			String startDate = sdf1.format(df.parse(dateFrom));
			String endDate = sdf1.format(df.parse(dateTo));*/
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			
			if(inputDays.equalsIgnoreCase("day")){
				Calendar calendar = Calendar.getInstance();
				dateFrom=sdf1.format(calendar.getTime());
				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
			}else if(inputDays.equalsIgnoreCase("week")){
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			        calendar.add(Calendar.DATE, -1);
			    }
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("month")){
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH,1);
				dateFrom=sdf1.format(calendar.getTime());	
			}
			else if(inputDays.equalsIgnoreCase("year")){	
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar.set(Calendar.DAY_OF_YEAR,1);
			     
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("lastMonth")){	
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH,-1);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DATE));
				dateFrom=sdf1.format(calendar.getTime());
 
		        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));  
		        dateTo=sdf1.format(calendar.getTime());
			}
			
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.MERCHANT)
					|| sessionUser.getUserType().equals(UserType.SUBUSER) || sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				statistics = barChartQuery.statisticsSummary(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo,
						sessionUser, statistics, subMerchantId,paymentRegion,saleReportFlag);
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummary(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics, subMerchantId,paymentRegion,saleReportFlag);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummary(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics, subMerchantId ,paymentRegion,saleReportFlag);
				}
			} else {
				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummary(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics, subMerchantId,paymentRegion,saleReportFlag);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummary(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics, subMerchantId ,paymentRegion,saleReportFlag);
				}
			}
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	public String capture() {
		
		User sessionUser = null;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		try {
			/*Statistics statistics = new Statistics();
			DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			String startDate = sdf1.format(df.parse(dateFrom));
			String endDate = sdf1.format(df.parse(dateTo));*/
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			
			if(inputDays.equalsIgnoreCase("day")){
				Calendar calendar = Calendar.getInstance();
				dateFrom=sdf1.format(calendar.getTime());
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
			}else if(inputDays.equalsIgnoreCase("week")){
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			        calendar.add(Calendar.DATE, -1);
			    }
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("month")){
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH,1);
				dateFrom=sdf1.format(calendar.getTime());	
			}
			else if(inputDays.equalsIgnoreCase("year")){	
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar.set(Calendar.DAY_OF_YEAR,1);
			     
				dateFrom=sdf1.format(calendar.getTime());
				
			}else if(inputDays.equalsIgnoreCase("custom")){	
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate=sdf.parse(dateFrom);
				Date toDate=sdf.parse(dateTo);
				
				dateFrom=sdf1.format(fromDate);
				dateTo=sdf1.format(toDate);
			}else if(inputDays.equalsIgnoreCase("previousDay")){
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate=sdf.parse(dateFrom);
				Date toDate=sdf.parse(dateTo);
				dateFrom=sdf1.format(fromDate)+" "+"00:00:00";
				dateTo=sdf1.format(toDate)+" "+"23:59:59";
				
			}
			/*else if(inputDays.equalsIgnoreCase("lastMonth")){	
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH,-1);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DATE));
				dateFrom=sdf1.format(calendar.getTime());
 
		        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));  
		        dateTo=sdf1.format(calendar.getTime());
			}*/
			
			if (sessionUser.getUserType().equals(UserType.MERCHANT)
					|| sessionUser.getUserType().equals(UserType.POSMERCHANT)) {
				
				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					
					if (sessionUser.isSuperMerchant()) {
						statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId, paymentRegion);
					}
					else if  (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics, sessionUser.getPayId(), paymentRegion);
					}
					else {
						statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,null, paymentRegion);
					}
					
					
				}
				else {
					
					if (sessionUser.isSuperMerchant()) {
						statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,userDao.findPayIdByEmail(getEmailId()).getPayId(), paymentRegion);
					}
					else if  (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,sessionUser.getPayId(), paymentRegion);
					}
					else {
						statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId, paymentRegion);
					}
					
				}
				
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummaryCapture(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,null, paymentRegion);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummaryCapture(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId, paymentRegion);
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
				
					statistics = barChartQuery.statisticsSummaryCapture(PayId, getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId, paymentRegion);
				
			}else if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				statistics = barChartQuery.statisticsSummaryCapture(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId, paymentRegion);
				
			}else {
				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummaryCapture(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId, paymentRegion);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummaryCapture(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId, paymentRegion);
				}
			}
			statisticsAll(statistics);
			//refund(statistics);
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	//public String refund(Statistics statistics) {
	public String refund() {
		try {
			/*DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			String startDate = sdf1.format(df.parse(dateFrom));
			String endDate = sdf1.format(df.parse(dateTo));*/
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			
			if(inputDays.equalsIgnoreCase("day")){
				Calendar calendar = Calendar.getInstance();
				dateFrom=sdf1.format(calendar.getTime());
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
			}else if(inputDays.equalsIgnoreCase("week")){
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			        calendar.add(Calendar.DATE, -1);
			    }
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("month")){
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH,1);
				dateFrom=sdf1.format(calendar.getTime());	
			}
			else if(inputDays.equalsIgnoreCase("year")){	
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar.set(Calendar.DAY_OF_YEAR,1);
			     
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("lastMonth")){	
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH,-1);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DATE));
				dateFrom=sdf1.format(calendar.getTime());
 
		        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));  
		        dateTo=sdf1.format(calendar.getTime());
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
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)
					|| sessionUser.getUserType().equals(UserType.POSMERCHANT)) {
				
				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					
					if (sessionUser.isSuperMerchant()) {
						statistics = barChartQuery.statisticsSummaryRefund(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId,paymentRegion);
					}
					else if  (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						statistics = barChartQuery.statisticsSummaryRefund(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,sessionUser.getPayId(),paymentRegion);
					}
					else {
						statistics = barChartQuery.statisticsSummaryRefund(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId,paymentRegion);
					}
					
					
				}
				else {
					
					if (sessionUser.isSuperMerchant()) {
						statistics = barChartQuery.statisticsSummaryRefund(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,userDao.findPayIdByEmail(getEmailId()).getPayId(),paymentRegion);
					}
					else if  (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						statistics = barChartQuery.statisticsSummaryRefund(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,sessionUser.getPayId(),paymentRegion);
					}
					else {
						statistics = barChartQuery.statisticsSummaryRefund(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId,paymentRegion);
					}
					
				}
				
			}

			else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummaryRefund(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId,paymentRegion);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummaryRefund(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId,paymentRegion);
				}
			}
			else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				String PayId=null;
				String subMerchantId=null;
				
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				
				//superMerchant
				if(StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && parentUser.isSuperMerchant() ){
					PayId=parentUser.getPayId();
				}else if(StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && !parentUser.isSuperMerchant() ){
					PayId=parentUser.getSuperMerchantId();
					subMerchantId=parentUser.getPayId();
				}else{
					PayId=parentUser.getPayId();
				}
				
					statistics = barChartQuery.statisticsSummaryRefund(PayId, getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId, paymentRegion);
				
				
			}else if(sessionUser.getUserType().equals(UserType.PARENTMERCHANT)){
				
				statistics = barChartQuery.statisticsSummaryRefund("", getCurrency(), dateFrom, dateTo,
						sessionUser, statistics,subMerchantId, paymentRegion);
				
			}else {

				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummaryRefund(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId,paymentRegion);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummaryRefund(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId,paymentRegion);
				}
			}
			statisticsAll(statistics);
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	public String statisticsAll(Statistics statistics) {
		try {
			/*DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			String startDate = sdf1.format(df.parse(dateFrom));
			String endDate = sdf1.format(df.parse(dateTo));*/
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			
			if(inputDays.equalsIgnoreCase("day")){
				Calendar calendar = Calendar.getInstance();
				dateFrom=sdf1.format(calendar.getTime());
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
			}else if(inputDays.equalsIgnoreCase("week")){
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			        calendar.add(Calendar.DATE, -1);
			    }
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("month")){
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH,1);
				dateFrom=sdf1.format(calendar.getTime());	
			}
			else if(inputDays.equalsIgnoreCase("year")){	
				Calendar calendar = Calendar.getInstance();
				//calendar.add(Calendar.DATE, 1);
				dateTo=sdf1.format(calendar.getTime());
				
				calendar.set(Calendar.DAY_OF_YEAR,1);
			     
				dateFrom=sdf1.format(calendar.getTime());
			}
			else if(inputDays.equalsIgnoreCase("lastMonth")){	
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH,-1);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DATE));
				dateFrom=sdf1.format(calendar.getTime());
 
		        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));  
		        dateTo=sdf1.format(calendar.getTime());
		        
			}else if(inputDays.equalsIgnoreCase("custom")){	
				
			}
			
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			UserType userType = sessionUser.getUserType();
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				
				if (sessionUser.isSuperMerchant()) {
					User subMerchant = userDao.findPayIdByEmail(getEmailId());
					String subMerchantId = null;
					if(subMerchant != null)
						subMerchantId = subMerchant.getPayId();
					statistics = barChartQuery.statisticsSummary(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,subMerchantId,paymentRegion, saleReportFlag);
				}
				else if  (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					statistics = barChartQuery.statisticsSummary(sessionUser.getSuperMerchantId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics,sessionUser.getPayId(),paymentRegion, saleReportFlag);
				}
				else {
					statistics = barChartQuery.statisticsSummary(sessionUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics, subMerchantId,paymentRegion, saleReportFlag);
				}
			}

			else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummary(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId,paymentRegion, saleReportFlag);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummary(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics, subMerchantId,paymentRegion, saleReportFlag);
				}

			}
			else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
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
				
				statistics = barChartQuery.statisticsSummary(PayId, getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId, paymentRegion, saleReportFlag);
				
			}  else if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				statistics = barChartQuery.statisticsSummary("", getCurrency(), dateFrom, dateTo,
						sessionUser, statistics,subMerchantId, paymentRegion, saleReportFlag);
			} else {
				if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					statistics = barChartQuery.statisticsSummary(getEmailId(), getCurrency(), dateFrom, dateTo,
							sessionUser, statistics,subMerchantId,paymentRegion, saleReportFlag);
				} else {
					User emailUser = userDao.findPayIdByEmail(getEmailId());
					statistics = barChartQuery.statisticsSummary(emailUser.getPayId(), getCurrency(), dateFrom, dateTo, sessionUser, statistics, subMerchantId,paymentRegion, saleReportFlag);
				}
			}
			setStatistics(statistics);
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	
	public String summaryData() {
		String response = SUCCESS;
		try {
			if(inputDays.equalsIgnoreCase("custom") && dateFrom.equals(dateTo)) {
				SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
				Date dt = new Date();
				String strdate = sdf2.format(dt);
				
				if(strdate.equals(dateFrom)) {
					inputDays = "day";
				}else {
					inputDays = "previousDay";
				}
				
			}
			
			if(saleReportFlag) {
				response = capture();
			}else {
				response = refund();
			}
//			payOutAllData();
		}catch(Exception e) {
			logger.info("Ecxeption in SummaryData : "  , e);
		}
		return response;
	}

	public String payOutAllData() {

		User sessionUser = null;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

			if (inputDays.equalsIgnoreCase("day")) {
				Calendar calendar = Calendar.getInstance();
				dateFrom = sdf1.format(calendar.getTime());
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

			} else if (inputDays.equalsIgnoreCase("week")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
					calendar.add(Calendar.DATE, -1);
				}
				dateFrom = sdf1.format(calendar.getTime());
			} else if (inputDays.equalsIgnoreCase("month")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

				calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				dateFrom = sdf1.format(calendar.getTime());
			} else if (inputDays.equalsIgnoreCase("year")) {
				Calendar calendar = Calendar.getInstance();
				// calendar.add(Calendar.DATE, 1);
				dateTo = sdf1.format(calendar.getTime());

				calendar.set(Calendar.DAY_OF_YEAR, 1);

				dateFrom = sdf1.format(calendar.getTime());

			} else if (inputDays.equalsIgnoreCase("custom")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);

				dateFrom = sdf1.format(fromDate);
				dateTo = sdf1.format(toDate);
			}

			dateFrom = dateFrom + " " + "00:00:00";
			dateTo = dateTo + " " + "23:59:59";

			String merchantPayId = null;
			String subMerchantPayId = null;
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					if (StringUtils.isNotBlank(getEmailId())
							&& getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
						merchantPayId = sessionUser.getPayId();
						subMerchantPayId = "ALL";
					} else {
						merchantPayId = sessionUser.getPayId();
						subMerchantPayId = userDao.getPayIdByEmailId(emailId);
					}
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();
				} else {
					merchantPayId = sessionUser.getPayId();
					subMerchantPayId = subMerchantId;
				}

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				// superMerchant
				if (parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					merchantPayId = parentUser.getPayId();
					subMerchantPayId = this.subMerchantId;
				} else if (!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					merchantPayId = parentUser.getPayId();
				}

			} else {

				if (StringUtils.isNotBlank(getEmailId())
						&& getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					merchantPayId = "ALL";
					subMerchantPayId = subMerchantId;
				} else {
					merchantPayId = userDao.getPayIdByEmailId(emailId);
					subMerchantPayId = subMerchantId;
				}

			}
			setPayOut(barChartQuery.pauOutDataReportSummary(merchantPayId, dateFrom, dateTo, sessionUser,
					subMerchantPayId));

			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}
	
	public void validate() {

		if (validator.validateBlankField(getEmailId())) {
		} else if (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
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

	public Statistics getPayOut() {
		return payOut;
	}

	public void setPayOut(Statistics payOut) {
		this.payOut = payOut;
	}	

}
