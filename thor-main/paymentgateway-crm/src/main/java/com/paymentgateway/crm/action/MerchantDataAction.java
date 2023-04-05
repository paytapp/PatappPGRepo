package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.ECollectionObject;
import com.paymentgateway.commons.user.MerchantObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ProductionReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;

public class MerchantDataAction extends AbstractSecureAction{
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserDao userDao;
	
	private SessionUserIdentifier userIdentifier;
	private static Logger logger = LoggerFactory.getLogger(ECollectionReportAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;
	
	private String payId;
	private String subMerchant;
	private int draw;
	private int length;
	private int start;
	
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	
	private List<MerchantObject> aaData;
	private User sessionUser = new User();
	
	public String execute() {
		List<MerchantObject> merchantListUpdated = new ArrayList<MerchantObject>();
	logger.info("Inside TransactionSearchAction , execute()");
	try {
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		User user = new User();
		String merchantPayId = "";
		String subMerchantPayIdd = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayIdd = sessionUser.getPayId();
		} else {
			merchantPayId = payId;

			if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
				//subMerchantPayIdd = subMerchantPayId;
				subMerchantPayIdd = userDao.getPayIdByEmailId(subMerchant);
			}else {
				subMerchantPayIdd=subMerchant;
			}
		}
		if((merchantPayId.equalsIgnoreCase("ALL")) && (subMerchantPayIdd.equalsIgnoreCase(""))) {
	    //String emailId = userDao.getEmailIdByPayId(merchantPayId);
		//user = new UserDao().findByEmailId(emailId);s
			List<Merchants> merchantList1 = new UserDao().getActiveResellerMerchants(sessionUser.getResellerId());
		List<Merchants> merchantList = new UserDao().getActiveResellerMerchants(sessionUser.getResellerId());
		for(Merchants merchants : merchantList) {
			String emailId = userDao.getEmailIdByPayId(merchants.getPayId());
			user = new UserDao().findByEmailId(emailId);
			MerchantObject user1 = new MerchantObject();
			user1.setRegistrationDate(user.getRegistrationDate().toString());
			user1.setBusinessName(merchants.getBusinessName());
			user1.setPayId(merchants.getPayId());
			user1.setStatus(user.getUserStatus().toString());
			merchantListUpdated.add(user1);
		}
		totalCount = merchantListUpdated.size();
		BigInteger bigInt = BigInteger.valueOf(totalCount);
		setRecordsTotal(bigInt);
		recordsFiltered = recordsTotal;
		
		}else if(!(merchantPayId.equalsIgnoreCase("ALL")) && (subMerchantPayIdd.equalsIgnoreCase(""))) {
			String emailId = userDao.getEmailIdByPayId(merchantPayId);
			user = new UserDao().findByEmailId(emailId);
			MerchantObject user1 = new MerchantObject();
			user1.setRegistrationDate(user.getRegistrationDate().toString());
			user1.setBusinessName(user.getBusinessName());
			user1.setPayId(user.getPayId());
			user1.setStatus(user.getUserStatus().toString());
			merchantListUpdated.add(user1);
			totalCount = merchantListUpdated.size();
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			recordsFiltered = recordsTotal;
			
			
		}else if(!(merchantPayId.equalsIgnoreCase("ALL")) && (subMerchantPayIdd.equalsIgnoreCase("ALL"))) {
			
			List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(merchantPayId);
			for(Merchants merchants : merchantList) {
				MerchantObject user1 = new MerchantObject();
				String emailId = userDao.getEmailIdByPayId(merchants.getPayId());
				user = new UserDao().findByEmailId(emailId);
				user1.setRegistrationDate(user.getRegistrationDate().toString());
				user1.setBusinessName(userDao.getBusinessNameByPayId(merchantPayId));
				user1.setSubMerchant(userDao.getBusinessNameByPayId(merchants.getPayId()));
				user1.setPayId(user.getPayId());
				user1.setStatus(user.getUserStatus().toString());
				merchantListUpdated.add(user1);
			}
			totalCount = merchantListUpdated.size();
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			recordsFiltered = recordsTotal;
		}else {
			MerchantObject user1 = new MerchantObject();
			String emailId = userDao.getEmailIdByPayId(merchantPayId);
			user = new UserDao().findByEmailId(emailId);
			user1.setRegistrationDate(user.getRegistrationDate().toString());
			user1.setBusinessName(user.getBusinessName());
			user1.setSubMerchant(userDao.getBusinessNameByPayId(subMerchantPayIdd));
			user1.setPayId(user.getPayId());
			user1.setStatus(user.getUserStatus().toString());
			merchantListUpdated.add(user1);
			totalCount = merchantListUpdated.size();
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			recordsFiltered = recordsTotal;
			
		}
		
		setAaData(merchantListUpdated);
		return SUCCESS;	
	
	}catch(Exception e) {
		logger.info("Exception Caught "+e);
		}
	return SUCCESS;
	}
	
	public List<MerchantObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<MerchantObject> aadata) {
		this.aaData = aadata;
	}
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
	
	public String getsubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
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

}
