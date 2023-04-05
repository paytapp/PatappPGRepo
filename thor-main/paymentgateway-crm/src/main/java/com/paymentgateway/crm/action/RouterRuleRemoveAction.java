/*
 * package com.paymentgateway.crm.action;
 * 
 * import java.util.ArrayList; import java.util.List;
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
 * com.paymentgateway.commons.user.User; import
 * com.paymentgateway.commons.user.UserType; import
 * com.paymentgateway.commons.util.Constants;
 * 
 *//**
	 * @author Rahul
	 *
	 *//*
		 * public class RouterRuleRemoveAction extends AbstractSecureAction {
		 * 
		 * private static Logger logger =
		 * LoggerFactory.getLogger(RouterRuleRemoveAction.class.getName()); private
		 * static final long serialVersionUID = -4833112333995653577L;
		 * 
		 * @Autowired private RouterRuleDao routerRuleDao;
		 * 
		 * @Autowired private RouterConfigurationDao routerConfigurationDao;
		 * 
		 * private Long id; private String response;
		 * 
		 * public String deleteRules() {
		 * 
		 * User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		 * StringBuilder permissions = new StringBuilder();
		 * permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		 * 
		 * if (sessionUser.getUserType().equals(UserType.ADMIN) ||
		 * permissions.toString().contains("Rule Engine Edit")) {
		 * 
		 * try { // Check and remove any pending router rule RouterRule routerRule =
		 * routerRuleDao.findRule(id); RouterRule pendingRouterRule = null;
		 * 
		 * if (routerRule != null) { pendingRouterRule =
		 * routerRuleDao.getPendingMatchingRule(routerRule); }
		 * 
		 * if (pendingRouterRule != null) {
		 * pendingRouterRule.setApprovedBy(sessionUser.getEmailId());
		 * routerRuleDao.delete(pendingRouterRule, sessionUser.getEmailId()); }
		 * 
		 * // Check any pending router configurations related to this router rule
		 * 
		 * List<RouterConfiguration> pendingRouterConfigurationList = new
		 * ArrayList<RouterConfiguration>(); if (routerRule != null) {
		 * pendingRouterConfigurationList =
		 * routerConfigurationDao.getPendingRulesByRouterRule(routerRule); }
		 * 
		 * if (pendingRouterConfigurationList.size() > 0) {
		 * 
		 * for (RouterConfiguration routerConfiguration :
		 * pendingRouterConfigurationList) {
		 * routerConfigurationDao.delete(routerConfiguration.getId()); }
		 * 
		 * }
		 * 
		 * if (routerRule != null) {
		 * routerRule.setRequestedBy(sessionUser.getEmailId());
		 * routerRuleDao.delete(routerRule, sessionUser.getEmailId()); }
		 * 
		 * 
		 * // Remove router configuration related to this rule if (routerRule != null) {
		 * deleteRouterRuleConfiguration(routerRule); }
		 * 
		 * setResponse("Router Rule Deleted"); addActionMessage("Router Rule Deleted");
		 * return SUCCESS; }
		 * 
		 * catch (Exception e) { logger.error("Exception in deleting router rule " + e);
		 * setResponse("Router Rule Delete action Failed");
		 * addActionMessage("Router Rule Delete action Failed"); return SUCCESS; }
		 * 
		 * }
		 * 
		 * else {
		 * 
		 * if (StringUtils.isEmpty(response)) {
		 * setResponse(ErrorType.ROUTER_RULE_UPDATE_DENIED.getResponseMessage()); }
		 * 
		 * 
		 * return SUCCESS;
		 * 
		 * }
		 * 
		 * }
		 * 
		 * 
		 * public void deleteRouterRuleConfiguration(RouterRule routerRule) {
		 * 
		 * try {
		 * 
		 * logger.info("Remove existing  RouterConfiguration");
		 * 
		 * String identifier = routerRule.getMerchant() + routerRule.getCurrency() +
		 * routerRule.getPaymentType() + routerRule.getMopType() +
		 * routerRule.getTransactionType() + routerRule.getPaymentsRegion().toString() +
		 * routerRule.getCardHolderType().toString() + routerRule.getSlabId();
		 * 
		 * List<RouterConfiguration> routerConfigurationList = new
		 * ArrayList<RouterConfiguration>();
		 * 
		 * 
		 * List<RouterConfiguration> routerConfigurationListBySlab =
		 * routerConfigurationDao .findRulesByIdentifier(identifier);
		 * routerConfigurationList.addAll(routerConfigurationListBySlab);
		 * 
		 * 
		 * if (routerConfigurationList.size() > 0) {
		 * 
		 * for (RouterConfiguration routerConfiguration : routerConfigurationList) {
		 * routerConfigurationDao.delete(routerConfiguration.getId()); }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * catch (Exception e) { logger.
		 * error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   "
		 * + e.getMessage()); }
		 * 
		 * }
		 * 
		 * public Long getId() { return id; }
		 * 
		 * public void setId(Long id) { this.id = id; }
		 * 
		 * public String getResponse() { return response; }
		 * 
		 * public void setResponse(String response) { this.response = response; } }
		 */