package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;

public class ViewChargingDetailsAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(ViewChargingDetailsAction.class.getName());
	private static final long serialVersionUID = 2436315965146702783L;
	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	Map<String, List<Map<String, List<ChargingDetails>>>> chargingDetailsData = new HashMap<String, List<Map<String, List<ChargingDetails>>>>();
	private String payId;

	public String execute() {
		Map<String, List<Map<String, List<ChargingDetails>>>> acquirerTypeDataMap = new HashMap<String, List<Map<String, List<ChargingDetails>>>>();
		try {
			if (!StringUtils.isEmpty(payId)) {

				List<ChargingDetails> chargingDetailsList = chargingDetailsDao.getMerchantActiveChargingDetails(payId);
				String businessName = userDao.getBusinessNameByPayId(payId);
				Set<String> acquirerNameSet = new HashSet<String>();
				Map<String, Set<String>> acquirerTypeKeyMap = new HashMap<String, Set<String>>();

				for (ChargingDetails chargingDetails : chargingDetailsList) {

					acquirerNameSet.add(chargingDetails.getAcquirerName());
				}

				for (String aquirerName : acquirerNameSet) {
					Set<String> uniqueKeySet = new HashSet<String>();

					for (ChargingDetails chargingDetails : chargingDetailsList) {
						String paymentType = chargingDetails.getPaymentType().getName();
						if (aquirerName.equalsIgnoreCase(chargingDetails.getAcquirerName())) {

							if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
									|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
									|| paymentType.equalsIgnoreCase("Wallet")) {

								String uniqueKey = chargingDetails.getPaymentType().getName();
								uniqueKeySet.add(uniqueKey);
								acquirerTypeKeyMap.put(chargingDetails.getAcquirerName(), uniqueKeySet);

							} else {

								String uniqueKey = chargingDetails.getPaymentType().getName() + "-"
										+ chargingDetails.getMopType().name() + "-"
										+ chargingDetails.getPaymentsRegion().name() + "-"
										+ chargingDetails.getSlabId() + "-"
										+ chargingDetails.getAcquiringMode().name() + "-"
										+ chargingDetails.getCardHolderType().name();
								uniqueKeySet.add(uniqueKey);
								acquirerTypeKeyMap.put(chargingDetails.getAcquirerName(), uniqueKeySet);
							}
						}
					}
				}
				for (String acquirerType : acquirerTypeKeyMap.keySet()) {
					Set<String> uniqueKeySet = acquirerTypeKeyMap.get(acquirerType);
					Map<String, List<ChargingDetails>> chargingDetailsDataMap = new HashMap<String, List<ChargingDetails>>();
					List<Map<String, List<ChargingDetails>>> chargingDetailsDataMapList = new ArrayList<Map<String, List<ChargingDetails>>>();
					for (String uniqueKey : uniqueKeySet) {
						boolean flag=false;
						List<ChargingDetails> chargingObjectList = new ArrayList<ChargingDetails>();

						for (ChargingDetails chargingDetails : chargingDetailsList) {
							if (!acquirerType.equalsIgnoreCase(chargingDetails.getAcquirerName())) {
								continue;
							}
							String key = "";
							String paymentType = chargingDetails.getPaymentType().getName();
							if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
									|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
									|| paymentType.equalsIgnoreCase("Wallet")) {
								key = chargingDetails.getPaymentType().getName();
								//flag=true;
							} else {
								key = chargingDetails.getPaymentType().getName() + "-"
										+ chargingDetails.getMopType().name() + "-"
										+ chargingDetails.getPaymentsRegion().name() + "-"
										+ chargingDetails.getSlabId() + "-"
										+ chargingDetails.getAcquiringMode().name() + "-"
										+ chargingDetails.getCardHolderType().name();
							}
							if (key != "" && uniqueKey.equalsIgnoreCase(key)) {
								chargingDetails.setBusinessName(businessName);
								chargingObjectList.add(chargingDetails);
							}
						}
						if(flag)
							chargingDetailsDataMap.put("", chargingObjectList);
						else
							chargingDetailsDataMap.put(uniqueKey, chargingObjectList);
					}
					chargingDetailsDataMapList.add(chargingDetailsDataMap);
					acquirerTypeDataMap.put(acquirerType, chargingDetailsDataMapList);
				}
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			addActionMessage(ErrorType.UNKNOWN.getResponseMessage());
		}
		setChargingDetailsData(acquirerTypeDataMap);
		return SUCCESS;
	}

	@SuppressWarnings("unchecked")
	public String displayList() {
		setListMerchant(userDao.getMerchantList());
		return INPUT;
	}

	public Map<String, List<Map<String, List<ChargingDetails>>>> getChargingDetailsData() {
		return chargingDetailsData;
	}

	public void setChargingDetailsData(Map<String, List<Map<String, List<ChargingDetails>>>> chargingDetailsData) {
		this.chargingDetailsData = chargingDetailsData;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

}
