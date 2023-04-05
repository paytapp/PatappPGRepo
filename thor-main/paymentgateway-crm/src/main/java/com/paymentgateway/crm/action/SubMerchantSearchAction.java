package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;

public class SubMerchantSearchAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(SubMerchantSearchAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;
	private String superMerchant;
	private String subMerchantEmail;
	private String mobile;
	private String status;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;

	private List<Merchants> aaData = new ArrayList<Merchants>();
	private User sessionUser = new User();

	private static Map<String, User> userMap = new HashMap<String, User>();

	public String execute() {

		List<Merchants> merchList = new ArrayList<Merchants>();
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		UserStatusType userStatus = null;

		if (status.equalsIgnoreCase("All")) {
			userStatus = null;
		} else if (status.equalsIgnoreCase("ACTIVE")) {
			userStatus = UserStatusType.ACTIVE;
		} else if (status.equalsIgnoreCase("PENDING")) {
			userStatus = UserStatusType.PENDING;
		} else if (status.equalsIgnoreCase("TRANSACTION_BLOCKED")) {
			userStatus = UserStatusType.TRANSACTION_BLOCKED;
		} else if (status.equalsIgnoreCase("SUSPENDED")) {
			userStatus = UserStatusType.SUSPENDED;
		} else if (status.equalsIgnoreCase("TERMINATED")) {
			userStatus = UserStatusType.TERMINATED;
		}

		try {

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				totalCount = userDao.getSubMerchantCount(superMerchant, subMerchantEmail, mobile, userStatus,"");
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
				merchList = userDao.getSubMerchantList(superMerchant, subMerchantEmail, mobile, userStatus, length,
						start,"");
			}

			else if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant()) {

				totalCount = userDao.getSubMerchantCount(superMerchant, subMerchantEmail, mobile, userStatus,"");
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;

				merchList = userDao.getSubMerchantList(sessionUser.getSuperMerchantId(), subMerchantEmail, mobile,
						userStatus, length, start,"");
			}

			else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser != null && parentUser.getUserType().equals(UserType.MERCHANT)
						&& parentUser.isSuperMerchant()) {

					totalCount = userDao.getSubMerchantCount(superMerchant, subMerchantEmail, mobile, userStatus,"");
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					recordsFiltered = recordsTotal;
					merchList = userDao.getSubMerchantList(parentUser.getSuperMerchantId(), subMerchantEmail, mobile,
							userStatus, length, start,"");
				}
			}else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

				String resellerId=sessionUser.getResellerId();
				
				totalCount = userDao.getSubMerchantCount(superMerchant, subMerchantEmail, mobile, userStatus, resellerId);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				recordsFiltered = recordsTotal;
				merchList = userDao.getSubMerchantList(superMerchant, subMerchantEmail, mobile, userStatus, length,
						start, resellerId);
			}

			for (Merchants merhnt : merchList) {

				if (userMap.get(merhnt.getSuperMerchantId()) != null) {
					merhnt.setSuperMerchantName(userMap.get(merhnt.getSuperMerchantId()).getBusinessName());
				} else {
					User superUser = userDao.findBySuperMerchantId(merhnt.getSuperMerchantId());
					if (superUser != null) {
						merhnt.setSuperMerchantName(superUser.getBusinessName());
						userMap.put(merhnt.getSuperMerchantId(), superUser);
					}
				}
			}

			setAaData(merchList);
			return SUCCESS;
		}

		catch (Exception e) {
			logger.error("Exception in getting sub merchant list ", e);
			return SUCCESS;
		}

	}

	

	

	public List<Merchants> getAaData() {
		return aaData;
	}

	public void setAaData(List<Merchants> aaData) {
		this.aaData = aaData;
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

	public String getSuperMerchant() {
		return superMerchant;
	}

	public void setSuperMerchant(String superMerchant) {
		this.superMerchant = superMerchant;
	}

	public String getSubMerchantEmail() {
		return subMerchantEmail;
	}

	public void setSubMerchantEmail(String subMerchantEmail) {
		this.subMerchantEmail = subMerchantEmail;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}
