/*
 * package com.paymentgateway.crm.action;
 * 
 * import java.util.ArrayList; import java.util.Collections; import
 * java.util.Comparator; import java.util.Date; import java.util.List;
 * 
 * import org.apache.commons.lang3.StringUtils; import org.slf4j.Logger; import
 * org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Autowired;
 * 
 * import com.paymentgateway.commons.dao.RouterConfigurationDao; import
 * com.paymentgateway.commons.dao.RouterRuleDao; import
 * com.paymentgateway.commons.exception.ErrorType; import
 * com.paymentgateway.commons.user.RouterConfiguration; import
 * com.paymentgateway.commons.user.RouterRule; import
 * com.paymentgateway.commons.user.SurchargeDao; import
 * com.paymentgateway.commons.user.User; import
 * com.paymentgateway.commons.user.UserType; import
 * com.paymentgateway.commons.util.Constants; import
 * com.paymentgateway.commons.util.Currency; import
 * com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType; import
 * com.paymentgateway.commons.util.PropertiesManager; import
 * com.paymentgateway.commons.util.TDRStatus;
 * 
 *//**
	 * @author Rahul
	 *
	 *//*
		 * public class RouterRuleEditAction extends AbstractSecureAction {
		 * 
		 * private static final long serialVersionUID = -127596067586594948L; private
		 * static Logger logger =
		 * LoggerFactory.getLogger(RouterRuleEditAction.class.getName());
		 * 
		 * @Autowired private RouterRuleDao routerRuleDao;
		 * 
		 * @Autowired private RouterConfigurationDao routerConfigurationDao;
		 * 
		 * @Autowired private SurchargeDao surchargeDao;
		 * 
		 * private List<RouterRule> listData; private String response; private User
		 * sessionUser = new User();
		 * 
		 * public String editRules() { sessionUser = (User)
		 * sessionMap.get(Constants.USER.getValue()); StringBuilder permissions = new
		 * StringBuilder();
		 * permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue())); try
		 * { Date currentDate = new Date(); for (RouterRule routerRule : listData) {
		 * 
		 * if (sessionUser.getUserType().equals(UserType.ADMIN) ||
		 * permissions.toString().contains("Rule Engine Edit")) { String payId =
		 * routerRule.getPayId();
		 * 
		 * long id = routerRule.getId();
		 * 
		 * if (Long.valueOf(id) != null) {
		 * 
		 * // Set current active rule as inactive
		 * routerRuleDao.delete(routerRule,sessionUser.getEmailId());
		 * 
		 * 
		 * // Set any pending rule as inactive RouterRule pendingRouterRule =
		 * routerRuleDao.getPendingMatchingRule(routerRule); if (pendingRouterRule !=
		 * null) { routerRuleDao.delete(pendingRouterRule,sessionUser.getEmailId()); }
		 * 
		 * 
		 * // Delete all active and pending router configuration for this rule
		 * deleteRouterRuleConfiguration(routerRule);
		 * 
		 * 
		 * // Add new active router rule String currency =
		 * Currency.getNumericCode(routerRule.getCurrency()); MopType mopType =
		 * MopType.getInstance(routerRule.getMopType()); PaymentType paymentType =
		 * PaymentType.getInstance(routerRule.getPaymentType());
		 * routerRule.setCreatedDate(currentDate);
		 * routerRule.setMerchant(routerRule.getPayId());
		 * routerRule.setCurrency(currency); routerRule.setMerchant(payId);
		 * routerRule.setMopType(mopType.getCode());
		 * routerRule.setPaymentType(paymentType.getCode());
		 * routerRule.setStatus(TDRStatus.ACTIVE);
		 * routerRule.setRequestedBy(sessionUser.getEmailId());
		 * routerRuleDao.create(routerRule);
		 * 
		 * // Add new active router configuration for this rule
		 * addRouterRuleConfiguration(routerRule); }
		 * 
		 * 
		 * if (StringUtils.isEmpty(response)) {
		 * setResponse(ErrorType.ROUTER_RULE_UPDATED.getResponseMessage()); } }
		 * 
		 * else {
		 * 
		 * if (StringUtils.isEmpty(response)) {
		 * setResponse(ErrorType.ROUTER_RULE_UPDATE_DENIED.getResponseMessage()); }
		 * 
		 * 
		 * }
		 * 
		 * }
		 * 
		 * } catch (Exception exception) {
		 * setResponse(ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage()); } return
		 * SUCCESS; }
		 * 
		 * public void deleteRouterRuleConfiguration(RouterRule routerRule) {
		 * 
		 * try {
		 * 
		 * logger.info("Remove existing  RouterConfiguration"); Date date = new Date();
		 * 
		 * List<RouterConfiguration> routerConfigurationPendingList = new
		 * ArrayList<RouterConfiguration>(); routerConfigurationPendingList =
		 * routerConfigurationDao.getPendingRulesByRouterRule(routerRule);
		 * 
		 * 
		 * if (routerConfigurationPendingList.size() > 0) {
		 * 
		 * for (RouterConfiguration routerConfiguration :
		 * routerConfigurationPendingList) {
		 * routerConfiguration.setRequestedBy(sessionUser.getEmailId());
		 * routerConfiguration.setUpdatedBy(sessionUser.getEmailId());
		 * routerConfiguration.setUpdatedDate(date);
		 * routerConfigurationDao.delete(routerConfiguration); }
		 * 
		 * }
		 * 
		 * 
		 * List<RouterConfiguration> routerConfigurationList = new
		 * ArrayList<RouterConfiguration>(); routerConfigurationList =
		 * routerConfigurationDao.getActiveRulesByRouterRule(routerRule);
		 * 
		 * if (routerConfigurationList.size() > 0) {
		 * 
		 * for (RouterConfiguration routerConfiguration : routerConfigurationList) {
		 * routerConfiguration.setRequestedBy(sessionUser.getEmailId());
		 * routerConfiguration.setUpdatedBy(sessionUser.getEmailId());
		 * routerConfiguration.setUpdatedDate(date);
		 * routerConfigurationDao.delete(routerConfiguration); }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * catch (Exception e) { logger.
		 * error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   "
		 * ,e); }
		 * 
		 * }
		 * 
		 * public void addRouterRuleConfiguration(RouterRule routerRule) { User
		 * sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		 * 
		 * try {
		 * 
		 * String[] acquirerMap = routerRule.getAcquirerMap().split(","); // Present in
		 * YML file String slabAmountArrayString = PropertiesManager.propertiesMap
		 * .get(Constants.SWITCH_ACQUIRER_AMOUNT.getValue()); StringBuilder tempSlab =
		 * new StringBuilder(); // If no slab is set for this merchant , create a
		 * default slab with 00 as ID and // limit from 0.01 to 1000000.00
		 * 
		 * if (!slabAmountArrayString.contains(routerRule.getMerchant())) {
		 * slabAmountArrayString = "00-0.01-1000000.00-" + routerRule.getMerchant() +
		 * "-ALL"; }
		 * 
		 * // If any slab is found , check if slab is for this particular payment type
		 * else {
		 * 
		 * String[] slabArray = slabAmountArrayString.split(","); for (String
		 * currentSlab : slabArray) {
		 * 
		 * if (!currentSlab.contains(routerRule.getMerchant())) { continue; }
		 * 
		 * String[] slabSplit = currentSlab.split("-");
		 * 
		 * if (!StringUtils.isBlank(slabSplit[4]) &&
		 * slabSplit[4].equalsIgnoreCase(routerRule.getPaymentType())) {
		 * 
		 * tempSlab.append(currentSlab); tempSlab.append(","); continue; } else {
		 * continue; } }
		 * 
		 * }
		 * 
		 * if (StringUtils.isNotBlank(tempSlab.toString())) { slabAmountArrayString =
		 * tempSlab.toString(); } else { slabAmountArrayString = "00-0.01-1000000.00-" +
		 * routerRule.getMerchant() + "-ALL"; }
		 * 
		 * String[] slabArray = slabAmountArrayString.split(",");
		 * 
		 * for (String currentSlab : slabArray) {
		 * 
		 * if (StringUtils.isBlank(currentSlab)) { continue; }
		 * 
		 * if (!currentSlab.contains(routerRule.getMerchant())) { continue; }
		 * 
		 * String[] slabSplit = currentSlab.split("-"); String slabId = slabSplit[0];
		 * String minTransactionAmount = slabSplit[1]; String maxTransactionAmount =
		 * slabSplit[2]; String paymentType = "ALL";
		 * 
		 * if (!StringUtils.isBlank(slabSplit[4])) { paymentType = slabSplit[4]; }
		 * 
		 * if (!paymentType.equalsIgnoreCase(routerRule.getPaymentType())) {
		 * 
		 * slabId = "00"; minTransactionAmount = "0.01"; maxTransactionAmount =
		 * "1000000.00";
		 * 
		 * }
		 * 
		 * List<RouterConfiguration> routerConfigurationList = new
		 * ArrayList<RouterConfiguration>();
		 * 
		 * for (String acquirerString : acquirerMap) {
		 * 
		 * String identifier = routerRule.getMerchant() + routerRule.getCurrency() +
		 * routerRule.getPaymentType() + routerRule.getMopType() +
		 * routerRule.getTransactionType() + routerRule.getPaymentsRegion().toString() +
		 * routerRule.getCardHolderType().toString() + slabId;
		 * 
		 * String[] acquirerMapString = acquirerString.split("-");
		 * 
		 * RouterConfiguration routerConfiguration = new RouterConfiguration(); Date
		 * date = new Date();
		 * 
		 * routerConfiguration.setIdentifier(identifier);
		 * routerConfiguration.setAcquirer(acquirerMapString[1]);
		 * routerConfiguration.setRulePriority(acquirerMapString[0]);
		 * routerConfiguration.setCurrency(routerRule.getCurrency());
		 * routerConfiguration.setPaymentType(routerRule.getPaymentType());
		 * routerConfiguration.setMopType(routerRule.getMopType());
		 * routerConfiguration.setTransactionType(routerRule.getTransactionType());
		 * routerConfiguration.setMode("AUTO");
		 * routerConfiguration.setStatus(TDRStatus.ACTIVE);
		 * routerConfiguration.setAllowedFailureCount(5);
		 * routerConfiguration.setCreatedDate(date);
		 * routerConfiguration.setUpdatedDate(date);
		 * routerConfiguration.setMerchant(routerRule.getMerchant());
		 * routerConfiguration.setOnUsoffUs(routerRule.isOnUsFlag());
		 * routerConfiguration.setDown(false);
		 * routerConfiguration.setRequestedBy(sessionUser.getEmailId());
		 * routerConfiguration.setRetryMinutes("10");
		 * routerConfiguration.setFailureCount(0);
		 * routerConfiguration.setPaymentsRegion(routerRule.getPaymentsRegion());
		 * routerConfiguration.setCardHolderType(routerRule.getCardHolderType());
		 * routerConfiguration.setSlabId(slabId);
		 * routerConfiguration.setMinAmount(Double.valueOf(minTransactionAmount));
		 * routerConfiguration.setMaxAmount(Double.valueOf(maxTransactionAmount));
		 * 
		 * String surcharge =
		 * surchargeDao.findDetailsByRouterConfiguration(routerConfiguration);
		 * 
		 * if (surcharge.equalsIgnoreCase("NA")) {
		 * routerConfiguration.setSurcharge("1.00"); }
		 * 
		 * routerConfiguration.setSurcharge(surcharge);
		 * 
		 * routerConfigurationList.add(routerConfiguration);
		 * 
		 * }
		 * 
		 * 
		 * Comparator<RouterConfiguration> comp = (RouterConfiguration a,
		 * RouterConfiguration b) -> {
		 * 
		 * if (Double.valueOf(b.getSurcharge()) > Double.valueOf(a.getSurcharge())) {
		 * return -1; } else if (Double.valueOf(b.getSurcharge()) <
		 * Double.valueOf(a.getSurcharge())) { return 1; } else { if
		 * (Double.valueOf(b.getRulePriority()) > Double.valueOf(a.getRulePriority())) {
		 * return -1; } else { return 1; } } };
		 * 
		 * Collections.sort(routerConfigurationList, comp);
		 * 
		 * int count = 1;
		 * 
		 * for (RouterConfiguration entry : routerConfigurationList) {
		 * 
		 * RouterConfiguration routerConfigurationToSave = new RouterConfiguration();
		 * routerConfigurationToSave = entry;
		 * 
		 * routerConfigurationToSave.setPriority(String.valueOf(count)); if (count == 1)
		 * { routerConfigurationToSave.setLoadPercentage(100);
		 * routerConfigurationToSave.setCurrentlyActive(true); }
		 * 
		 * else { routerConfigurationToSave.setLoadPercentage(0);
		 * routerConfigurationToSave.setCurrentlyActive(false); }
		 * routerConfigurationDao.create(routerConfigurationToSave); count++; }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * catch (Exception e) {
		 * 
		 * logger.error("Error occured wile adding Router Configuration " +
		 * e.getMessage()); }
		 * 
		 * }
		 * 
		 * public List<RouterRule> getListData() { return listData; }
		 * 
		 * public void setListData(List<RouterRule> listData) { this.listData =
		 * listData; }
		 * 
		 * public String getResponse() { return response; }
		 * 
		 * public void setResponse(String response) { this.response = response; }
		 * 
		 * }
		 */