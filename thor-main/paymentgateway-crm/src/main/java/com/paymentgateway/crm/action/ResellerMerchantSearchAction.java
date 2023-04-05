package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Rajit
 */
public class ResellerMerchantSearchAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;
	
	private static Logger logger = LoggerFactory.getLogger(ResellerMerchantSearchAction.class.getName());
	private static final long serialVersionUID = 8663813244474721281L;
	private String merchantEmail;
	private String mobile;
	private String status;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;

	private List<Merchants> aaData =  new ArrayList<Merchants>();
	private User sessionUser = new User();
	
	public String execute() {
		
		List <Merchants> merchantList = new ArrayList<Merchants>();
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		UserStatusType userStatus = null;
		
		if (status.equalsIgnoreCase("All")) {
			userStatus = null;
		}
		else if (status.equalsIgnoreCase("ACTIVE")) {
			userStatus = UserStatusType.ACTIVE;
		}
		else if (status.equalsIgnoreCase("PENDING")) {
			userStatus = UserStatusType.PENDING;
		}
		else if (status.equalsIgnoreCase("TRANSACTION_BLOCKED")) {
			userStatus = UserStatusType.TRANSACTION_BLOCKED;
		}
		else if (status.equalsIgnoreCase("SUSPENDED")) {
			userStatus = UserStatusType.SUSPENDED;
		}
		else if (status.equalsIgnoreCase("TERMINATED")) {
			userStatus = UserStatusType.TERMINATED;
		}
		try {
			
			totalCount = userDao.getMerchantCountForReseller(sessionUser.getResellerId(), merchantEmail.trim(), mobile.trim(), userStatus);
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			recordsFiltered = recordsTotal;
			merchantList = userDao.getMerchantListForReseller(sessionUser.getResellerId(), merchantEmail.trim(), mobile.trim(), userStatus, length, start);
			
			setAaData(merchantList);
		} catch(Exception exception) {
			logger.error("Exception in getting sub merchant list ",exception);
			return ERROR;
		}
		return SUCCESS;
	}
	
	
	public String getMerchantEmail() {
		return merchantEmail;
	}
	public void setMerchantEmail(String merchantEmail) {
		this.merchantEmail = merchantEmail;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public List<Merchants> getAaData() {
		return aaData;
	}
	public void setAaData(List<Merchants> aaData) {
		this.aaData = aaData;
	}

}
