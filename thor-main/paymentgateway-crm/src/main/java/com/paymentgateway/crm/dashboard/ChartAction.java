package com.paymentgateway.crm.dashboard;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class ChartAction extends AbstractSecureAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6230766457144695681L;
	
	@Autowired
	private UserDao userDao;
	private static Logger logger = LoggerFactory
			.getLogger(ChartAction.class.getName());

	@Autowired
	private PieChartService getPieChartService ;
	private String emailId;
	private String currency;
	private String dateFrom;
	private String dateTo;
	private PieChart pieChart= new PieChart();
	public String execute() {
		
		try {
		
			User user = (User) sessionMap.get(Constants.USER.getValue());
			if (user.getUserType().equals(UserType.MERCHANT)
					|| user.getUserType().equals(UserType.POSMERCHANT)) {
				setPieChart(getPieChartService.getDashboardValues(
						user.getPayId(), getCurrency(), getDateFrom(),
						getDateTo()));

			} else if (user.getUserType().equals(UserType.ACQUIRER)) {
				setPieChart(getPieChartService.getDashboardValues(
						user.getFirstName(), getCurrency(), getDateFrom(),
						getDateTo()));

				return SUCCESS;
			} else {
				if (getEmailId().equals(
						CrmFieldConstants.ALL_MERCHANTS.getValue())) {
					setPieChart(getPieChartService.getDashboardValues(
							getEmailId(), getCurrency(), getDateFrom(),
							getDateTo()));
				} else {
					User merchantPayId = userDao
					.findPayIdByEmail(getEmailId());
					setPieChart(getPieChartService.getDashboardValues(merchantPayId.getPayId(),
							getCurrency(), getDateFrom(), getDateTo()));
				}
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	
	
	public void validate(){
		
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


	public PieChart getPieChart() {
		return pieChart;
	}


	public void setPieChart(PieChart pieChart) {
		this.pieChart = pieChart;
	}

}