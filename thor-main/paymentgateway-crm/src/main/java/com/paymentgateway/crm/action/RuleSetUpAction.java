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
 * com.paymentgateway.commons.user.ChargingDetails; import
 * com.paymentgateway.commons.user.ChargingDetailsDao; import
 * com.paymentgateway.commons.user.RouterConfiguration; import
 * com.paymentgateway.commons.user.RouterRule; import
 * com.paymentgateway.commons.user.User; import
 * com.paymentgateway.commons.user.UserType; import
 * com.paymentgateway.commons.util.AcquirerType; import
 * com.paymentgateway.commons.util.Constants; import
 * com.paymentgateway.commons.util.Currency; import
 * com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType; import
 * com.paymentgateway.commons.util.TDRStatus; import
 * com.paymentgateway.commons.util.TransactionType; import
 * com.paymentgateway.commons.util.onUsOffUs;
 * 
 *//**
	 * @author Rahul
	 *
	 *//*
		 * public class RuleSetUpAction extends AbstractSecureAction {
		 * 
		 * private static final long serialVersionUID = -5819377796996242126L; private
		 * static Logger logger =
		 * LoggerFactory.getLogger(RuleSetUpAction.class.getName());
		 * 
		 * @Autowired private RouterRuleDao routerRuleDao;
		 * 
		 * private List<RouterRule> listData;
		 * 
		 * @Autowired private RouterConfigurationDao routerConfigurationDao;
		 * 
		 * @Autowired private ChargingDetailsDao chargingDetailsDao;
		 * 
		 * private String response; private User sessionUser = new User();
		 * 
		 * public String execute() {
		 * 
		 * sessionUser = (User) sessionMap.get(Constants.USER.getValue()); StringBuilder
		 * permissions = new StringBuilder();
		 * permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue())); try
		 * {
		 * 
		 * Date currentDate = new Date(); RouterRule routerRuleDb = null;
		 * 
		 * if (sessionUser.getUserType().equals(UserType.ADMIN) ||
		 * permissions.toString().contains("Rule Engine Edit")) {
		 * 
		 * for (RouterRule routerRule : listData) {
		 * routerRule.setCardHolderType(routerRule.getCardHolderType());
		 * routerRule.setPaymentsRegion(routerRule.getPaymentsRegion());
		 * routerRule.setAcquirerMap(routerRule.getAcquirerMap());
		 * 
		 * routerRuleDb = routerRuleDao.getMatchingRule(routerRule);
		 * 
		 * if (routerRuleDb != null) { if (StringUtils.isEmpty(response)) {
		 * setResponse(ErrorType.SOME_RULE_ALREADY_EXIST.getResponseMessage());
		 * addActionMessage(ErrorType.SOME_RULE_ALREADY_EXIST.getResponseMessage()); } }
		 * else {
		 * 
		 * if (routerRule.getId() != null) {
		 * removeAllPendingRulesAndConfig(routerRule.getId()); }
		 * 
		 * String currency = Currency.getNumericCode(routerRule.getCurrency());
		 * routerRule.setCreatedDate(currentDate);
		 * routerRule.setStatus(TDRStatus.ACTIVE); routerRule.setCurrency(currency);
		 * routerRule.setRequestedBy(sessionUser.getEmailId());
		 * routerRule.setApprovedBy(sessionUser.getEmailId());
		 * routerRuleDao.create(routerRule); if (routerRule.isOnUsFlag()) {
		 * addONUSRouterRuleConfiguration(routerRule); } else {
		 * addRouterRuleConfiguration(routerRule); } } } if
		 * (StringUtils.isEmpty(response)) {
		 * setResponse(ErrorType.ROUTER_RULE_CREATED.getResponseMessage());
		 * addActionMessage(ErrorType.ROUTER_RULE_CREATED.getResponseMessage()); } }
		 * 
		 * else {
		 * 
		 * if (StringUtils.isEmpty(response)) {
		 * setResponse(ErrorType.ROUTER_RULE_UPDATE_DENIED.getResponseMessage()); }
		 * 
		 * }
		 * 
		 * } catch (Exception exception) {
		 * setResponse(ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage()); } return
		 * SUCCESS; }
		 * 
		 * public void addONUSRouterRuleConfiguration(RouterRule routerRule) { User
		 * sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		 * 
		 * try {
		 * 
		 * String identifier = routerRule.getMerchant() + routerRule.getCurrency() +
		 * routerRule.getPaymentType() + routerRule.getMopType() +
		 * routerRule.getTransactionType() + routerRule.getPaymentsRegion().toString() +
		 * routerRule.getCardHolderType().toString() + routerRule.getSlabId();
		 * 
		 * RouterConfiguration routerConfiguration = new RouterConfiguration(); Date
		 * date = new Date(); String[] acquirerMapString =
		 * routerRule.getAcquirerMap().split("-");
		 * 
		 * routerConfiguration.setIdentifier(identifier);
		 * routerConfiguration.setAcquirer(acquirerMapString[1]);
		 * routerConfiguration.setRulePriority("1");
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
		 * routerConfiguration.setSlabId(routerRule.getSlabId());
		 * routerConfiguration.setMinAmount(Double.valueOf(routerRule.getMinAmount()));
		 * routerConfiguration.setMaxAmount(Double.valueOf(routerRule.getMaxAmount()));
		 * routerConfiguration.setLoadPercentage(100);
		 * routerConfiguration.setPriority("1");
		 * routerConfiguration.setCurrentlyActive(true);
		 * 
		 * routerConfigurationDao.create(routerConfiguration);
		 * 
		 * }
		 * 
		 * catch (Exception e) {
		 * 
		 * logger.error("Error occured while adding Router Configuration " +
		 * e.getMessage()); }
		 * 
		 * }
		 * 
		 * public void addRouterRuleConfiguration(RouterRule routerRule) { User
		 * sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		 * 
		 * try {
		 * 
		 * String[] acquirerMap = routerRule.getAcquirerMap().split(","); onUsOffUs
		 * onUsOffUsValue = onUsOffUs.OFF_US;
		 * 
		 * List<RouterConfiguration> routerConfigurationList = new
		 * ArrayList<RouterConfiguration>();
		 * 
		 * for (String acquirerString : acquirerMap) {
		 * 
		 * String identifier = routerRule.getMerchant() + routerRule.getCurrency() +
		 * routerRule.getPaymentType() + routerRule.getMopType() +
		 * routerRule.getTransactionType() + routerRule.getPaymentsRegion().toString() +
		 * routerRule.getCardHolderType().toString() + routerRule.getSlabId();
		 * 
		 * String[] acquirerMapString = acquirerString.split("-"); String acquirerName =
		 * AcquirerType.getAcquirerName(acquirerMapString[1]);
		 * 
		 * RouterConfiguration routerConfiguration = new RouterConfiguration(); Date
		 * date = new Date();
		 * 
		 * MopType mopType = MopType.getmop(routerRule.getMopType()); PaymentType
		 * paymentType = PaymentType.getInstanceUsingCode(routerRule.getPaymentType());
		 * // AccountCurrencyRegion acr = routerRule.getPaymentsRegion();
		 * TransactionType txnType =
		 * TransactionType.getInstanceFromCode(routerRule.getTransactionType());
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
		 * routerConfiguration.setSlabId(routerRule.getSlabId());
		 * routerConfiguration.setMinAmount(Double.valueOf(routerRule.getMinAmount()));
		 * routerConfiguration.setMaxAmount(Double.valueOf(routerRule.getMaxAmount()));
		 * 
		 * ChargingDetails chargingDetails =
		 * chargingDetailsDao.findActiveChargingDetail(mopType, paymentType, txnType,
		 * acquirerName, routerRule.getCurrency(), routerRule.getMerchant(),
		 * routerRule.getPaymentsRegion(), routerRule.getCardHolderType(),
		 * onUsOffUsValue, routerRule.getSlabId());
		 * 
		 * String pgCharge = Double.toString(chargingDetails.getPgTDR()); if
		 * (pgCharge.equalsIgnoreCase("NA")) { continue; }
		 * 
		 * routerConfiguration.setSurcharge(pgCharge);
		 * 
		 * routerConfigurationList.add(routerConfiguration);
		 * 
		 * }
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
		 * } // }
		 * 
		 * catch (Exception e) {
		 * 
		 * logger.error("Error occured wile adding Router Configuration " +
		 * e.getMessage()); }
		 * 
		 * }
		 * 
		 * public void removeAllPendingRulesAndConfig(Long id) {
		 * 
		 * RouterRule routerRule = routerRuleDao.findRule(id);
		 * 
		 * // Find pending Rules in Router Rule RouterRule routerRulePending =
		 * routerRuleDao.getPendingMatchingRule(routerRule); if (routerRulePending !=
		 * null) { routerRuleDao.delete(routerRulePending, sessionUser.getEmailId()); }
		 * 
		 * // Find pending Router Configuration for this router rule
		 * List<RouterConfiguration> routerConfigurationPending = routerConfigurationDao
		 * .getPendingRulesByRouterRule(routerRule);
		 * 
		 * if (routerConfigurationPending.size() > 0) {
		 * 
		 * for (RouterConfiguration routerConfiguration : routerConfigurationPending) {
		 * routerConfigurationDao.delete(routerConfiguration.getId()); }
		 * 
		 * } }
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