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
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;

/**
 * @author Rahul
 *
 */
public class DisplaySmartRouterPage extends AbstractSecureAction {

	private static final long serialVersionUID = -3054395368649186178L;
	private static Logger logger = LoggerFactory.getLogger(DisplaySmartRouterPage.class.getName());

	private Map<String, String> merchantList = new TreeMap<String, String>();
	private Map<String, String> regionTypeList = new TreeMap<String, String>();
	private Map<String, String> cardTypeList = new TreeMap<String, String>();
	@Autowired
	private UserDao userDao;
	
	//For smart router Display and Download
	@Autowired
	private RouterConfigurationDao routerConfigurationDao;
	private String payId;
	
	private Map<String, List<RouterConfiguration>> routerRuleData = new HashMap<String, List<RouterConfiguration>>();

	public String execute() {

		try {

			List<Merchants> merchantsList = new ArrayList<Merchants>();
			Map<String, String> merchantMap = new HashMap<String, String>();

			merchantsList = userDao.getMerchantActiveList();
			
			for (CardHolderType cardType :  CardHolderType.values()) {
				cardTypeList.put(cardType.name(), cardType.name());
			}
			
			for (AccountCurrencyRegion regionType :  AccountCurrencyRegion.values()) {
				regionTypeList.put(regionType.name(), regionType.name());
			}
			
			for (Merchants merchant : merchantsList) {

				merchantMap.put(merchant.getPayId(),merchant.getBusinessName());
			}
			merchantList.putAll(merchantMap);
			return SUCCESS;

		}

		catch (Exception e) {

			logger.error("Exception in execute()  ==  " , e);
		}
		return ERROR;

	}
	
	public String smartRouterDisplayAndDownload() {
		
		Map<String, List<RouterConfiguration>> routerRuleDataMap = new HashMap<String, List<RouterConfiguration>>();
		try {
			List<RouterConfiguration> activeRouterList = new ArrayList<RouterConfiguration>();
			
			activeRouterList = routerConfigurationDao.getActiveRulesByMerchant(payId);
			
			Set<String> identifierKeySet = new HashSet<String>();

			for (RouterConfiguration routerConfiguration : activeRouterList) {

				String identifier = routerConfiguration.getPaymentType() + "-" + routerConfiguration.getMopType() + "-"
						+ routerConfiguration.getMerchant() + "-" + routerConfiguration.getTransactionType() + "-"
						+ routerConfiguration.getCurrency()+ "-"+routerConfiguration.getPaymentsRegion()+ "-"+routerConfiguration.getCardHolderType()+ "-"+routerConfiguration.getSlabId();

				identifierKeySet.add(identifier);
			}

			for (String uniqueKey : identifierKeySet) {
				List<RouterConfiguration> routerConfigurationList = new ArrayList<RouterConfiguration>();

				for (RouterConfiguration routerConfig : activeRouterList) {

					String key = routerConfig.getPaymentType() + "-" + routerConfig.getMopType() + "-"
							+ routerConfig.getMerchant() + "-" + routerConfig.getTransactionType() + "-"
							+ routerConfig.getCurrency()+ "-"+routerConfig.getPaymentsRegion()+ "-"+routerConfig.getCardHolderType()+ "-"+routerConfig.getSlabId();
					
					
					if (key.equalsIgnoreCase(uniqueKey)) {

						String paymentTypeName = PaymentType.getpaymentName(routerConfig.getPaymentType());
						String mopTypeName = MopType.getmopName(routerConfig.getMopType());

						routerConfig.setPaymentTypeName(paymentTypeName);
						routerConfig.setMopTypeName(mopTypeName);

						routerConfig.setStatusName(routerConfig.getStatusName());
						routerConfigurationList.add(routerConfig);
					}

				}

				routerRuleDataMap.put(uniqueKey, routerConfigurationList);
			}
			
		} catch (Exception ex) {
			logger.info("exception occur while display smart router" +ex);
		}
		
		setRouterRuleData(routerRuleDataMap);
		return SUCCESS;
	}

	public Map<String, String> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(Map<String, String> merchantList) {
		this.merchantList = merchantList;
	}

	public Map<String, String> getRegionTypeList() {
		return regionTypeList;
	}

	public void setRegionTypeList(Map<String, String> regionTypeList) {
		this.regionTypeList = regionTypeList;
	}

	public Map<String, String> getCardTypeList() {
		return cardTypeList;
	}

	public void setCardTypeList(Map<String, String> cardTypeList) {
		this.cardTypeList = cardTypeList;
	}
	
	public Map<String, List<RouterConfiguration>> getRouterRuleData() {
		return routerRuleData;
	}

	public void setRouterRuleData(Map<String, List<RouterConfiguration>> routerRuleData) {
		this.routerRuleData = routerRuleData;
	}
	
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
}