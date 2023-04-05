/**
 * 
 */
package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ResellerChargesDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ResellerCharges;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Amitosh Aanand
 *
 */
public class ViewResellerChargesAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private ResellerChargesDao resellerChargesDao;

	private String payId;
	private String resellerId;
	private User sessionUser = new User();
	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	public List<Merchants> listReseller = new ArrayList<Merchants>();
	static final long serialVersionUID = -9071731374033658236L;
	Map<String, List<Map<String, List<ResellerCharges>>>> resellerChargesData = new HashMap<String, List<Map<String, List<ResellerCharges>>>>();
	private static Logger logger = LoggerFactory.getLogger(ViewResellerChargesAction.class.getName());

	@SuppressWarnings("unchecked")
	public String execute() {
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				setListMerchant(userDao.getMerchantListByResellerID(sessionUser.getResellerId()));
			} else {
				setListMerchant(userDao.getMerchantList());
				setListReseller(userDao.getResellerList());
			}
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return SUCCESS;
		}
	}

	public String viewCharges() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.RESELLER)) {
			resellerId = sessionUser.getResellerId();
		}
		try {
			logger.info("Inside viewCharges of ViewResellerChargesAction");
			Map<String, List<Map<String, List<ResellerCharges>>>> acquirerTypeDataMap = new HashMap<String, List<Map<String, List<ResellerCharges>>>>();
			try {
				if (!StringUtils.isEmpty(payId)) {
					List<ResellerCharges> resellerChargesList = resellerChargesDao
							.fetchChargesByResellerAndMerchant(payId, resellerId);
					Set<String> uniqueKeySet = new HashSet<String>();
					for (ResellerCharges resellerCharges : resellerChargesList) {
						String paymentType = resellerCharges.getPaymentType().getName();
						if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
								|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
								|| paymentType.equalsIgnoreCase("Wallet")) {
							String uniqueKey = resellerCharges.getPaymentType().getName();
							uniqueKeySet.add(uniqueKey);
						} else {
							String uniqueKey = resellerCharges.getPaymentType().getName() + "-"
									+ resellerCharges.getPaymentsRegion().name() + "-"
									+ resellerCharges.getCardHolderType().name();
							uniqueKeySet.add(uniqueKey);
						}
					}
					Map<String, List<ResellerCharges>> resellerChargesDataMap = new HashMap<String, List<ResellerCharges>>();
					List<Map<String, List<ResellerCharges>>> resellerChargesDataMapList = new ArrayList<Map<String, List<ResellerCharges>>>();
					for (String uniqueKey : uniqueKeySet) {
						boolean flag = false;
						List<ResellerCharges> resellerObjectList = new ArrayList<ResellerCharges>();
						for (ResellerCharges resellerCharges : resellerChargesList) {
							String key = "";
							String paymentType = resellerCharges.getPaymentType().getName();
							if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
									|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
									|| paymentType.equalsIgnoreCase("Wallet")) {
								key = resellerCharges.getPaymentType().getName();
								flag = true;
							} else {
								key = resellerCharges.getPaymentType().getName() + "-"
										+ resellerCharges.getPaymentsRegion().name() + "-"
										+ resellerCharges.getCardHolderType().name();
							}
							if (key != "" && uniqueKey.equalsIgnoreCase(key)) {
								resellerObjectList.add(resellerCharges);
							}
							if (flag)
								resellerChargesDataMap.put(uniqueKey, resellerObjectList);
							else
								resellerChargesDataMap.put(uniqueKey, resellerObjectList);
						}
					}
					resellerChargesDataMap.remove("");
					resellerChargesDataMapList.add(resellerChargesDataMap);
					acquirerTypeDataMap.put("", resellerChargesDataMapList);
				}
			} catch (Exception exception) {
				logger.error("Exception", exception);
				addActionMessage(ErrorType.UNKNOWN.getResponseMessage());
			}
			setResellerChargesData(new TreeMap<String, List<Map<String, List<ResellerCharges>>>>(acquirerTypeDataMap));
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return SUCCESS;
		}
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public List<Merchants> getListReseller() {
		return listReseller;
	}

	public void setListReseller(List<Merchants> listReseller) {
		this.listReseller = listReseller;
	}

	public Map<String, List<Map<String, List<ResellerCharges>>>> getResellerChargesData() {
		return resellerChargesData;
	}

	public void setResellerChargesData(Map<String, List<Map<String, List<ResellerCharges>>>> resellerChargesData) {
		this.resellerChargesData = resellerChargesData;
	}
}
