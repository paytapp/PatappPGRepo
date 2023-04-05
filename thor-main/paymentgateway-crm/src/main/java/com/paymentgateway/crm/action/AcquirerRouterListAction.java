/*
 * package com.paymentgateway.crm.action;
 * 
 * import java.util.ArrayList; import java.util.Collections; import
 * java.util.Comparator; import java.util.List;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Autowired;
 * 
 * import com.paymentgateway.commons.dao.RouterRuleDao; import
 * com.paymentgateway.commons.user.RouterConfiguration; import
 * com.paymentgateway.commons.user.RouterRule; import
 * com.paymentgateway.commons.user.User; import
 * com.paymentgateway.commons.user.UserDao; import
 * com.paymentgateway.commons.util.Currency; import
 * com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType;
 * 
 *//**
	 * @author Rahul
	 *
	 *//*
		 * public class AcquirerRouterListAction extends AbstractSecureAction{ private
		 * static Logger logger =
		 * LoggerFactory.getLogger(AcquirerRouterListAction.class.getName()); private
		 * static final long serialVersionUID = 2396085939269105787L; private
		 * List<RouterRule> routerRules = new ArrayList<RouterRule>(); private
		 * List<RouterRule> dbrouterRules = new ArrayList<RouterRule>();
		 * 
		 * @Autowired private UserDao userDao;
		 * 
		 * @Autowired private RouterRuleDao routerRuleDao;
		 * 
		 * private String payId;
		 * 
		 * public String execute(){ try{ dbrouterRules =
		 * routerRuleDao.getActiveRules(payId); for(RouterRule routerRule :
		 * dbrouterRules){ String currency =
		 * Currency.getAlphabaticCode(routerRule.getCurrency()); String paymentType =
		 * PaymentType.getpaymentName(routerRule.getPaymentType()); String mopType =
		 * MopType.getmopName(routerRule.getMopType()); String merchantPayId =
		 * routerRule.getMerchant(); if(!merchantPayId.equals("ALL MERCHANTS")){
		 * 
		 * User merchantName = userDao.findPayId(routerRule.getMerchant());
		 * routerRule.setMerchant(merchantName.getBusinessName());
		 * routerRule.setPayId(merchantName.getPayId()); }
		 * routerRule.setCurrency(currency); routerRule.setPaymentType(paymentType);
		 * routerRule.setMopType(mopType);
		 * 
		 * routerRules.add(routerRule);
		 * 
		 * Comparator<RouterRule> comp = (RouterRule a, RouterRule b) -> {
		 * 
		 * if (b.getMerchant().compareTo(a.getMerchant()) < 0) { return -1; } else if
		 * (b.getMerchant().compareTo(a.getMerchant()) > 0) { return 1; } else { return
		 * 0; }
		 * 
		 * };
		 * 
		 * Collections.sort(routerRules, comp);
		 * 
		 * setRouterRules(routerRules); }
		 * 
		 * return SUCCESS; }catch(Exception exception){ logger.error("Exception",
		 * exception); return ERROR; }
		 * 
		 * }
		 * 
		 * public List<RouterRule> getDbrouterRules() { return dbrouterRules; }
		 * 
		 * public void setDbrouterRules(List<RouterRule> dbrouterRules) {
		 * this.dbrouterRules = dbrouterRules; }
		 * 
		 * public List<RouterRule> getRouterRules() { return routerRules; }
		 * 
		 * public void setRouterRules(List<RouterRule> routerRules) { this.routerRules =
		 * routerRules; }
		 * 
		 * public String getPayId() { return payId; }
		 * 
		 * public void setPayId(String payId) { this.payId = payId; }
		 * 
		 * }
		 */