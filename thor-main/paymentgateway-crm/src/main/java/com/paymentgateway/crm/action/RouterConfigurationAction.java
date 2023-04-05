package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;

public class RouterConfigurationAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	/*
	 * @Autowired private RouterRuleDao routerRuleDao;
	 */

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	private static Logger logger = LoggerFactory.getLogger(RouterConfigurationAction.class.getName());
	private static final long serialVersionUID = -6879974923614009981L;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	private Map<String, String> merchantList = new TreeMap<String, String>();
	
	public String merchantName;
	public String paymentMethod;
	public String cardHolderType;
	public String acquiringMode;
	
	private Map<String, List<RouterConfiguration>> routerRuleData = new HashMap<String, List<RouterConfiguration>>();

	@SuppressWarnings("unchecked")
	public String execute() {

		try {
			
			List<Merchants> merchantsList = new ArrayList<Merchants>();
			Map<String, String> merchantMap = new HashMap<String, String>();

			merchantsList = userDao.getActiveMerchantList();

			for (Merchants merchant : merchantsList) {

				merchantMap.put(merchant.getPayId(),merchant.getBusinessName());
			}
			merchantList.putAll(merchantMap);

			Map<String, List<RouterConfiguration>> routerRuleDataMap = new HashMap<String, List<RouterConfiguration>>();

			List<RouterConfiguration> routerConfigurationListAll = new ArrayList<RouterConfiguration>();

			routerConfigurationListAll = routerConfigurationDao.getActiveRulesByMerchant(getMerchantName(),getCardHolderType(),getPaymentMethod(),getAcquiringMode());

			Set<String> uniqueKeySet = new HashSet<String>();

			for (RouterConfiguration routerConfiguration : routerConfigurationListAll) {

				String key = routerConfiguration.getPaymentType() + "-" + routerConfiguration.getMopType() + "-"
						+ routerConfiguration.getMerchant() + "-" + routerConfiguration.getTransactionType() + "-"
						+ routerConfiguration.getCurrency()+ "-"+routerConfiguration.getPaymentsRegion()+ "-"+routerConfiguration.getCardHolderType()+ "-"+routerConfiguration.getSlabId();

				uniqueKeySet.add(key);
			}

			for (String uniqueKey : uniqueKeySet) {
				List<RouterConfiguration> routerConfigurationList = new ArrayList<RouterConfiguration>();

				for (RouterConfiguration routerConfiguration : routerConfigurationListAll) {

					String key = routerConfiguration.getPaymentType() + "-" + routerConfiguration.getMopType() + "-"
							+ routerConfiguration.getMerchant() + "-" + routerConfiguration.getTransactionType() + "-"
							+ routerConfiguration.getCurrency()+ "-"+routerConfiguration.getPaymentsRegion()+ "-"+routerConfiguration.getCardHolderType()+ "-"+routerConfiguration.getSlabId();
					
					//User user = userDao.findPayId(routerConfiguration.getMerchant());
					
					if (key.equalsIgnoreCase(uniqueKey)) {

						String paymentTypeName = PaymentType.getpaymentName(routerConfiguration.getPaymentType());
						String mopTypeName = MopType.getmopName(routerConfiguration.getMopType());

						routerConfiguration.setPaymentTypeName(paymentTypeName);
						routerConfiguration.setMopTypeName(mopTypeName);

							routerConfiguration.setStatusName(routerConfiguration.getStatusName());
						routerConfigurationList.add(routerConfiguration);
					}

				}

				routerRuleDataMap.put(uniqueKey, routerConfigurationList);
			}

			setRouterRuleData(routerRuleDataMap);
			setAcquiringMode(acquiringMode);
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			addActionMessage(ErrorType.UNKNOWN.getResponseMessage());
		}

		return INPUT;
	}

	public void validate() {

	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public Map<String, List<RouterConfiguration>> getRouterRuleData() {
		return routerRuleData;
	}

	public void setRouterRuleData(Map<String, List<RouterConfiguration>> routerRuleData) {
		this.routerRuleData = routerRuleData;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public Map<String, String> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(Map<String, String> merchantList) {
		this.merchantList = merchantList;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getAcquiringMode() {
		return acquiringMode;
	}

	public void setAcquiringMode(String acquiringMode) {
		this.acquiringMode = acquiringMode;
	}


}
