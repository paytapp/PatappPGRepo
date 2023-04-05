package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.GstRSaleReport;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.GstSaleReports;

public class GstSaleReportAction extends AbstractSecureAction {
	

	private static final long serialVersionUID = -6864847076552167476L;

	@Autowired
	private SessionUserIdentifier userIdentifier;

	// private static Logger logger =
	// LoggerFactory.getLogger(GstSaleReportAction.class.getName());

	private String merchant;
	private String currency;
	private String month;
	private String year;
	private String dateFrom;
	private String dateTo;
	private String transactionType;
	private String paymentType;
	private String status;
	private int draw;
	private int length;
	
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;

	private List<GstRSaleReport> aaData;
	
	private User sessionUser;
	
	@Autowired
	private CrmValidator validator;

	@Autowired
	private GstSaleReports gstSaleReports;
	
	@Autowired
	private DataEncoder encoder;

	public String execute() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<GstRSaleReport> gstRSaleReportList = new ArrayList<GstRSaleReport>();

		try {
			

	
			setTransactionType("ALL");
			setPaymentType("ALL");
			//setStatus(StatusType.SETTLED.toString());
			setStatus("Captured");
			setLength(10);
			
			gstRSaleReportList = gstSaleReports.searchGstRSale(
					merchant, getPaymentType(), getStatus(), getCurrency(), getTransactionType(),
					getMonth(),getYear(), sessionUser, getStart(), getLength());

				BigInteger bigInt = BigInteger.valueOf(gstRSaleReportList.size());
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(gstRSaleReportList);
				return SUCCESS;

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String monthno(String month){
		int monthno = 1+Integer.parseInt(month);
	
		if(monthno < 9){
			month= "0"+String.valueOf(monthno);
		
		}
		else{
			month = String.valueOf(monthno);
		}
		return month;
	}
	public static int day(String month ,String year){
		  Calendar calendar = Calendar.getInstance();
		  
	        int yearno = Integer.parseInt(year);
	        
	       // int monthno = Calendar.SEPTEMBER;
	        int monthno = Integer.parseInt(month);
	        int date = 1;
	        calendar.set(yearno, monthno, date);
	        System.out.println(month);
	        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	       
	        return maxDay;
	}

	


	public void validate() {

		if (validator.validateBlankField(getMerchant())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT, getMerchant())) {
			addFieldError(CrmFieldType.MERCHANT.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if (validator.validateBlankField(getMonth())) {
		} else if (!validator.validateField(CrmFieldType.MONTH_YEAR, getMonth())) {
			addFieldError(CrmFieldType.MONTH_YEAR.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if (validator.validateBlankField(getYear())) {
		} else if (!validator.validateField(CrmFieldType.MONTH_YEAR, getYear())) {
			addFieldError(CrmFieldType.MONTH_YEAR.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

	}
	

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	
	public List<GstRSaleReport> getAaData() {
		return aaData;
	}

	public void setAaData(List<GstRSaleReport> aaData) {
		this.aaData = aaData;
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

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
