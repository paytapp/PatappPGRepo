package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class FraudAnalyticsReportDataAction extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TxnReports txnReports;
	
	private static Logger logger = LoggerFactory.getLogger(FraudAnalyticsReportDataAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;
	
	private String payId;
	private String subMerchantPayId;
	private String status;
	private String paymentRegion;
	private String countryCodes;
	private String dateFrom;
	private String dateTo;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	public boolean flag = false;
	
    private List<TransactionSearch> aaData;
	
	private User sessionUser = new User();
	
	public String execute() {
		logger.info("Inside FraudAnalyticsReportDataAction , execute()");
		int totalCount;
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			String merchantPayId = "";
			String subMerchantPayIdd = "";
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayIdd = sessionUser.getPayId();
			} else {
				

				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
					//subMerchantPayIdd = subMerchantPayId;
					subMerchantPayIdd = userDao.getPayIdByEmailId(subMerchantPayId);
				}
                if(StringUtils.isNotBlank(payId)) {
                	merchantPayId = userDao.getPayIdByEmailId(payId);
				}
			}
			
			setAaData(txnReports.fraudAnalyticsReportData(merchantPayId, subMerchantPayIdd, getPaymentRegion(), getCountryCodes(), getStatus(), getDateFrom(), getDateTo(), sessionUser));
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				flag = true;
				setFlag(flag);
			} else if(sessionUser.isSuperMerchant() == true ) {
				flag = true;
				setFlag(flag);
			}else if(StringUtils.isNotBlank(subMerchantPayId)){
				flag = true;
				setFlag(flag);
			} else {
				flag = false;
				setFlag(flag);
			}
			logger.info("Inside FraudAnalyticsReportDataAction" + getAaData());
			recordsFiltered = recordsTotal;
		}catch(Exception e) {
			logger.error("exception " , e);
		}
		return SUCCESS;
	}
	
	
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
	public String getCountryCodes() {
		return countryCodes;
	}
	public void setCountryCodes(String countryCodes) {
		this.countryCodes = countryCodes;
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
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public List<TransactionSearch> getAaData() {
		return aaData;
	}

	public void setAaData(List<TransactionSearch> aaData) {
		this.aaData = aaData;
	}
	
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}

}
