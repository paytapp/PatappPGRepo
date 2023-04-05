package com.paymentgateway.crm.dashboard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class PieChartAction extends AbstractSecureAction{

	private static final long serialVersionUID = -7403810827653949879L;

	private static Logger logger = LoggerFactory.getLogger(PieChartAction.class.getName());

	@Autowired
	private PieChartService getPieChartService;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserDao userDao;
	
	private String payId;
	private String emailId;
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String subMerchantId;
	private String paymentRegion;
	private String inputDays;
	private PieChart pieChart= new PieChart();
//	private PieChart pieChartRefund= new PieChart();
	private boolean saleReportFlag;
	private MerchantTransaction merchantTxnAndAmt=new MerchantTransaction();

	public String execute() {
		String response;
		if(saleReportFlag) {
			response = salePieChart();
		}else {
			response = refundPieChart();
		}
		
		return response;
	}
	
	public String salePieChart() {
		
		
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
			} else if (inputDays.equalsIgnoreCase("previousDay")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);
				dateFrom = sdf1.format(fromDate) + " " + "00:00:00";
				dateTo = sdf1.format(toDate) + " " + "23:59:59";

			}

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if (StringUtils.isNotBlank(emailId) && !emailId.equalsIgnoreCase("ALL MERCHANTS")) {
					User user = userDao.findPayIdByEmail(emailId);
					setPayId(user.getPayId());
				}
				setPieChart(getPieChartService.getSaleDataForPieChart(dateFrom, dateTo, sessionUser, payId,
						subMerchantId, paymentRegion, currency));
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				setPieChart(getPieChartService.getSaleDataForPieChart(dateFrom, dateTo, sessionUser, null, null,
						paymentRegion, currency));

//				return SUCCESS;

			} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				setPieChart(getPieChartService.getSaleDataForPieChart(dateFrom, dateTo, sessionUser,
						sessionUser.getPayId(), null, paymentRegion, currency));
//				return SUCCESS;
			} else {
				if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					String PayId = null;
					String subMerchantId = null;

					if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId()) && !sessionUser.isSuperMerchant()) {
						PayId = sessionUser.getSuperMerchantId();
						subMerchantId = sessionUser.getPayId();
					} else {
						PayId = sessionUser.getPayId();
					}

					setPieChart(getPieChartService.getSaleDataForPieChart(dateFrom, dateTo, sessionUser, PayId,
							subMerchantId, paymentRegion, currency));

				} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
					String PayId = null;
					String subMerchantId = null;

					User parentUser = userDao.findPayId(sessionUser.getParentPayId());

					// superMerchant
					if (StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && parentUser.isSuperMerchant()) {
						PayId = parentUser.getPayId();
						subMerchantId = this.subMerchantId;
					} else if (StringUtils.isNotBlank(parentUser.getSuperMerchantId())
							&& !parentUser.isSuperMerchant()) {
						PayId = parentUser.getSuperMerchantId();
						subMerchantId = parentUser.getPayId();
					} else {
						PayId = parentUser.getPayId();
					}

					setPieChart(getPieChartService.getSaleDataForPieChart(dateFrom, dateTo, sessionUser, PayId,
							subMerchantId, paymentRegion, currency));

				}else if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
					
					setPieChart(getPieChartService.getSaleDataForPieChart(dateFrom, dateTo, sessionUser, "",
							subMerchantId, paymentRegion, currency));
				}
			}

			// refundPieChart();
		}catch (Exception e) {
			logger.info("exception in salePieChartAction " , e);
		}
		return SUCCESS;
	}
	public String refundPieChart() {
		
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
			} else if (inputDays.equalsIgnoreCase("previousDay")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);
				dateFrom = sdf1.format(fromDate) + " " + "00:00:00";
				dateTo = sdf1.format(toDate) + " " + "23:59:59";

			}
			
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if(StringUtils.isNotBlank(emailId) && !emailId.equalsIgnoreCase("ALL MERCHANTS")) {
					User user =userDao.findPayIdByEmail(emailId);
					setPayId(user.getPayId());
				}
				setPieChart(getPieChartService.getRefundDataForPieChart(dateFrom, dateTo, sessionUser, payId, subMerchantId, paymentRegion, currency));
			}
			else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				setPieChart(getPieChartService.getRefundDataForPieChart(dateFrom, dateTo, sessionUser,null,null,paymentRegion, currency));
			
				return SUCCESS;
			}else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				setPieChart(getPieChartService.getRefundDataForPieChart(dateFrom, dateTo, sessionUser, sessionUser.getPayId(),null, paymentRegion, currency));
				return SUCCESS;
			} else {
				if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					String PayId=null;
					String subMerchantId=null;
					
					
					if(StringUtils.isNotBlank(sessionUser.getSuperMerchantId())  && !sessionUser.isSuperMerchant()){
						PayId=sessionUser.getSuperMerchantId();
						subMerchantId=sessionUser.getPayId();
					}else{
						PayId=sessionUser.getPayId();
					}
					
					setPieChart(getPieChartService.getRefundDataForPieChart(dateFrom, dateTo, sessionUser, PayId,subMerchantId, paymentRegion, currency));
					
				}else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
					String PayId=null;
					String subMerchantId=null;
					
					User parentUser = userDao.findPayId(sessionUser.getParentPayId());
					
					//superMerchant
					if(StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && parentUser.isSuperMerchant() ){
						PayId=parentUser.getSuperMerchantId();
						subMerchantId=this.subMerchantId;
					}else if(StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && !parentUser.isSuperMerchant() ){
						PayId=parentUser.getSuperMerchantId();
						subMerchantId=parentUser.getPayId();
					}else{
						PayId=parentUser.getPayId();
					}
					setPieChart(getPieChartService.getRefundDataForPieChart(dateFrom, dateTo, sessionUser, PayId, subMerchantId, paymentRegion, currency));
					
					}else if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
						
						setPieChart(getPieChartService.getRefundDataForPieChart(dateFrom, dateTo, sessionUser, "", subMerchantId, paymentRegion, currency));
					}
				}
				
		
			
		}catch (Exception e) {
			logger.info("exception in refundPieChartAction " , e);
		}
		return SUCCESS;
	}
	
	public String highestTotalMerchants() {
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if (StringUtils.isNotBlank(emailId) && !emailId.equalsIgnoreCase("ALL MERCHANTS")) {
				User user = userDao.findPayIdByEmail(emailId);
				setPayId(user.getPayId());
			}
			
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
			} else if (inputDays.equalsIgnoreCase("previousDay")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);
				dateFrom = sdf1.format(fromDate) + " " + "00:00:00";
				dateTo = sdf1.format(toDate) + " " + "23:59:59";

			}
			
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)
					|| (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant())) {
				
			setMerchantTxnAndAmt(getPieChartService.getHigestMerchantData(dateFrom, dateTo, payId, saleReportFlag, sessionUser, currency));
			}
			
			

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}
	
	public String lowestTotalMerchants() {
		try {
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if (StringUtils.isNotBlank(emailId) && !emailId.equalsIgnoreCase("ALL MERCHANTS")) {
				User user = userDao.findPayIdByEmail(emailId);
				setPayId(user.getPayId());
			}
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
			} else if (inputDays.equalsIgnoreCase("previousDay")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDate = sdf.parse(dateFrom);
				Date toDate = sdf.parse(dateTo);
				dateFrom = sdf1.format(fromDate) + " " + "00:00:00";
				dateTo = sdf1.format(toDate) + " " + "23:59:59";

			}
			
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)
					|| (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant())) {
				
			setMerchantTxnAndAmt(getPieChartService.getLowestMerchantData(dateFrom, dateTo, payId, saleReportFlag, sessionUser, currency));
			}
			

		} catch (Exception exception) {
			logger.error("Exception", exception);
			
		}
		return SUCCESS;
	}

	
	public void validate() {

		if (validator.validateBlankField(getDateFrom())) {
		} else if (!validator.validateField(CrmFieldType.DATE_FROM, getDateFrom())) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateTo())) {
		} else if (!validator.validateField(CrmFieldType.DATE_TO, getDateTo())) {
			addFieldError(CrmFieldType.DATE_TO.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}


	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
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

	public PieChart getPieChart() {
		return pieChart;
	}

	public void setPieChart(PieChart pieChart) {
		this.pieChart = pieChart;
	}

	public MerchantTransaction getMerchantTxnAndAmt() {
		return merchantTxnAndAmt;
	}

	public void setMerchantTxnAndAmt(MerchantTransaction merchantTxnAndAmt) {
		this.merchantTxnAndAmt = merchantTxnAndAmt;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public boolean isSaleReportFlag() {
		return saleReportFlag;
	}

	public void setSaleReportFlag(boolean saleReportFlag) {
		this.saleReportFlag = saleReportFlag;
	}

	public String getInputDays() {
		return inputDays;
	}

	public void setInputDays(String inputDays) {
		this.inputDays = inputDays;
	}

//	public PieChart getPieChartRefund() {
//		return pieChartRefund;
//	}
//
//	public void setPieChartRefund(PieChart pieChartRefund) {
//		this.pieChartRefund = pieChartRefund;
//	}
	
	
}
