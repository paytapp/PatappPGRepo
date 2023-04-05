/*
 * package com.paymentgateway.commons.dao;
 * 
 * import java.util.ArrayList; import java.util.Date; import java.util.List;
 * 
 * import javax.persistence.Query;
 * 
 * import org.apache.commons.lang3.StringUtils; import
 * org.hibernate.HibernateException; import
 * org.hibernate.ObjectNotFoundException; import org.hibernate.Session; import
 * org.hibernate.Transaction; import org.springframework.stereotype.Service;
 * 
 * import com.paymentgateway.commons.exception.DataAccessLayerException; import
 * com.paymentgateway.commons.user.AccountCurrencyRegion; import
 * com.paymentgateway.commons.user.CardHolderType; import
 * com.paymentgateway.commons.user.RouterRule; import
 * com.paymentgateway.commons.util.Currency; import
 * com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType; import
 * com.paymentgateway.commons.util.TDRStatus;
 * 
 *//**
	 * @author Rahul
	 *
	 *//*
		 * @Service public class RouterRuleDao extends HibernateAbstractDao {
		 * 
		 * public void create(RouterRule routerRule) throws DataAccessLayerException {
		 * super.save(routerRule); }
		 * 
		 * public void delete(Long id) { try { Date date = new Date(); RouterRule rule =
		 * findRule(id); if (null != rule) { rule.setStatus(TDRStatus.INACTIVE);
		 * rule.setUpdatedDate(date); } super.saveOrUpdate(rule); } catch
		 * (HibernateException e) { //handleException(e); } finally {
		 * //autoClose(session); } }
		 * 
		 * public void delete(RouterRule rule , String approvedBy) { try { Date date =
		 * new Date(); if (null != rule) { rule.setStatus(TDRStatus.INACTIVE);
		 * rule.setUpdatedDate(date); rule.setApprovedBy(approvedBy); }
		 * super.saveOrUpdate(rule); } catch (HibernateException e) {
		 * //handleException(e); } finally { //autoClose(session); } }
		 * 
		 * public RouterRule findRule(Long id) { RouterRule routerRule = null; Session
		 * session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try { routerRule = (RouterRule) session
		 * .createQuery("from RouterRule RR where RR.id = :id and RR.status = 'ACTIVE' "
		 * ) .setParameter("id", id).setCacheable(true).uniqueResult(); tx.commit(); }
		 * catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRule;
		 * 
		 * }
		 * 
		 * 
		 * public RouterRule findPendingRule(Long id) { RouterRule routerRule = null;
		 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try { routerRule = (RouterRule) session
		 * .createQuery("from RouterRule RR where RR.id = :id and RR.status = 'PENDING' "
		 * ) .setParameter("id", id).setCacheable(true).uniqueResult(); tx.commit(); }
		 * catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRule;
		 * 
		 * }
		 * 
		 * 
		 * @SuppressWarnings("unchecked") public List<RouterRule>
		 * findAllRuleByMerchant(String payId) { List<RouterRule> routerRuleList = new
		 * ArrayList<RouterRule>(); Session session =
		 * HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try {
		 * 
		 * routerRuleList = session
		 * .createQuery("from RouterRule RR where RR.merchant = :payId and RR.status = 'ACTIVE' "
		 * ) .setCacheable(true).setParameter("payId",
		 * payId).setCacheable(true).getResultList(); tx.commit();
		 * 
		 * } catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRuleList;
		 * 
		 * }
		 * 
		 * @SuppressWarnings("unchecked") public List<RouterRule> getActiveRules(String
		 * payId) { List<RouterRule> rulesList = new ArrayList<RouterRule>(); rulesList
		 * = null; Session session = HibernateSessionProvider.getSession(); Transaction
		 * tx = session.beginTransaction(); try {
		 * 
		 * rulesList = session.
		 * createQuery("from RouterRule RR where RR.merchant = :payId and RR.status='ACTIVE'"
		 * ) .setCacheable(true).setParameter("payId", payId).setCacheable(true)
		 * .getResultList(); tx.commit(); return rulesList; } catch
		 * (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); } return rulesList; }
		 * 
		 * 
		 * @SuppressWarnings("unchecked") public List<RouterRule> getPendingRules() {
		 * List<RouterRule> rulesList = new ArrayList<RouterRule>(); rulesList = null;
		 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try {
		 * 
		 * rulesList =
		 * session.createQuery("from RouterRule RR where RR.status='PENDING'")
		 * .setCacheable(true).setCacheable(true) .getResultList(); tx.commit(); return
		 * rulesList; } catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); } return rulesList; }
		 * 
		 * 
		 * public RouterRule findRuleByFields(String paymentType, String mopType, String
		 * currency, String transactionType) { RouterRule routerRule = null; Session
		 * session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try { routerRule = (RouterRule)
		 * session.createQuery(
		 * "from RouterRule RR where RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency  and RR.transactionType = :transactionType and RR.status = 'ACTIVE' "
		 * ) .setParameter("paymentType", paymentType).setParameter("mopType", mopType)
		 * .setParameter("currency", currency).setParameter("transactionType",
		 * transactionType) .setCacheable(true).uniqueResult(); tx.commit(); } catch
		 * (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRule;
		 * 
		 * }
		 * 
		 * public RouterRule findRuleByFieldsByPayId(String payId, String paymentType,
		 * String mopType, String currency, String transactionType, String
		 * paymentsRegion, String cardHolderType) {
		 * 
		 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); AccountCurrencyRegion acr;
		 * 
		 * if
		 * (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString()))
		 * { acr = AccountCurrencyRegion.DOMESTIC; } else { acr =
		 * AccountCurrencyRegion.INTERNATIONAL; }
		 * 
		 * CardHolderType act;
		 * 
		 * if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
		 * act = CardHolderType.COMMERCIAL; } if
		 * (cardHolderType.equalsIgnoreCase(CardHolderType.PREMIUM.toString())) { act =
		 * CardHolderType.PREMIUM; } else { act = CardHolderType.CONSUMER; } boolean
		 * onUsFlag = false; RouterRule routerRule = null; try { routerRule =
		 * (RouterRule) session.createQuery(
		 * "from RouterRule RR where RR.merchant = :payId and RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency  and RR.transactionType = :transactionType"
		 * +
		 * " and RR.paymentsRegion = :acr and RR.cardHolderType = :act and RR.onUsFlag = :onUsFlag and RR.status = 'ACTIVE' "
		 * ) .setParameter("payId", payId).setParameter("paymentType", paymentType)
		 * .setParameter("mopType", mopType).setParameter("currency", currency)
		 * .setParameter("transactionType", transactionType) .setParameter("acr",
		 * acr).setParameter("act", act) .setParameter("onUsFlag",
		 * onUsFlag).setCacheable(true).uniqueResult(); tx.commit(); } catch
		 * (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRule;
		 * 
		 * }
		 * 
		 * 
		 * public RouterRule findPendingRuleByFieldsByPayId(String payId, String
		 * paymentType, String mopType, String currency, String transactionType, String
		 * paymentsRegion, String cardHolderType) {
		 * 
		 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); AccountCurrencyRegion acr;
		 * 
		 * if
		 * (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString()))
		 * { acr = AccountCurrencyRegion.DOMESTIC; } else { acr =
		 * AccountCurrencyRegion.INTERNATIONAL; }
		 * 
		 * CardHolderType act;
		 * 
		 * if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
		 * act = CardHolderType.COMMERCIAL; } else { act = CardHolderType.CONSUMER; }
		 * 
		 * RouterRule routerRule = null; try { routerRule = (RouterRule)
		 * session.createQuery(
		 * "from RouterRule RR where RR.merchant = :payId and RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency  and RR.transactionType = :transactionType"
		 * +
		 * " and RR.paymentsRegion = :acr and RR.cardHolderType = :act and RR.status = 'PENDING' "
		 * ) .setParameter("payId", payId).setParameter("paymentType", paymentType)
		 * .setParameter("mopType", mopType).setParameter("currency", currency)
		 * .setParameter("transactionType", transactionType) .setParameter("acr",
		 * acr).setParameter("act", act) .setCacheable(true).uniqueResult();
		 * tx.commit(); } catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRule;
		 * 
		 * }
		 * 
		 * 
		 * public RouterRule findRuleByFieldsByPayId(String payId, String paymentType,
		 * String mopType, String currency, String transactionType) {
		 * 
		 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); RouterRule routerRule = null; try { routerRule =
		 * (RouterRule) session.createQuery(
		 * "from RouterRule RR where RR.merchant = :payId and RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency  and RR.transactionType = :transactionType"
		 * + " and RR.status = 'ACTIVE' ") .setParameter("payId",
		 * payId).setParameter("paymentType", paymentType) .setParameter("mopType",
		 * mopType).setParameter("currency", currency) .setParameter("transactionType",
		 * transactionType) .setCacheable(true).uniqueResult(); tx.commit(); } catch
		 * (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); // return responseUser; } return routerRule;
		 * 
		 * }
		 * 
		 * public RouterRule getMatchingRule(RouterRule routerRule) {
		 * 
		 * String currency = ""; if (Currency.getNumericCode(routerRule.getCurrency())
		 * != null ) { currency = Currency.getNumericCode(routerRule.getCurrency()); }
		 * else{ currency = routerRule.getCurrency(); }
		 * 
		 * RouterRule routerRule1 = null; Session session =
		 * HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); AccountCurrencyRegion paymentsRegion =
		 * routerRule.getPaymentsRegion(); CardHolderType cardHolderType =
		 * routerRule.getCardHolderType(); Boolean onUsFlag = routerRule.isOnUsFlag();
		 * try { routerRule1 = (RouterRule) session.createQuery(
		 * "from RouterRule RR where RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency and RR.transactionType = :transactionType and RR.merchant = :merchant and RR.paymentsRegion = :paymentsRegion and RR.cardHolderType = :cardHolderType and RR.onUsFlag = :onUsFlag and RR.slabId = :slabId and RR.status='ACTIVE'  "
		 * ) .setParameter("paymentType", routerRule.getPaymentType())
		 * .setParameter("mopType", routerRule.getMopType()).setParameter("currency",
		 * currency) .setParameter("transactionType", routerRule.getTransactionType())
		 * .setParameter("cardHolderType", cardHolderType)
		 * .setParameter("paymentsRegion", paymentsRegion) .setParameter("onUsFlag",
		 * onUsFlag) .setParameter("slabId", routerRule.getSlabId())
		 * .setParameter("merchant",
		 * routerRule.getMerchant()).setCacheable(true).uniqueResult(); tx.commit(); }
		 * catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } catch
		 * (Exception ex) { System.out.println(ex.getMessage()); } finally {
		 * autoClose(session); // return responseUser; } return routerRule1;
		 * 
		 * }
		 * 
		 * public RouterRule getPendingMatchingRule(RouterRule routerRule) {
		 * 
		 * String currency = "";
		 * 
		 * if (Currency.getNumericCode(routerRule.getCurrency()) != null) { currency =
		 * Currency.getNumericCode(routerRule.getCurrency()); } else { currency =
		 * routerRule.getCurrency(); } RouterRule routerRule1 = null; Session session =
		 * HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); AccountCurrencyRegion paymentsRegion =
		 * routerRule.getPaymentsRegion(); CardHolderType cardHolderType =
		 * routerRule.getCardHolderType();
		 * 
		 * try { routerRule1 = (RouterRule) session.createQuery(
		 * "from RouterRule RR where RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency and RR.transactionType = :transactionType and RR.merchant = :merchant and RR.paymentsRegion = :paymentsRegion and RR.cardHolderType = :cardHolderType and RR.status='PENDING'  "
		 * ) .setParameter("paymentType", routerRule.getPaymentType())
		 * .setParameter("mopType", routerRule.getMopType()).setParameter("currency",
		 * currency) .setParameter("transactionType", routerRule.getTransactionType())
		 * .setParameter("cardHolderType", cardHolderType)
		 * .setParameter("paymentsRegion", paymentsRegion) .setParameter("merchant",
		 * routerRule.getMerchant()).setCacheable(true).uniqueResult(); tx.commit(); }
		 * catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } catch
		 * (Exception ex) { System.out.println(ex.getMessage()); } finally {
		 * autoClose(session); // return responseUser; } return routerRule1;
		 * 
		 * }
		 * 
		 * 
		 * public RouterRule getMatchingRuleByAcquirer(RouterRule routerRule) {
		 * 
		 * String currency = "";
		 * 
		 * if (Currency.getNumericCode(routerRule.getCurrency()) != null) { currency =
		 * Currency.getNumericCode(routerRule.getCurrency()); } else { currency =
		 * routerRule.getCurrency(); } RouterRule routerRule1 = null; Session session =
		 * HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try {
		 * 
		 * routerRule1 = (RouterRule) session.createQuery(
		 * "from RouterRule RR where RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency and RR.acquirerMap = :acquirerMap and RR.transactionType = :transactionType and RR.merchant = :merchant and RR.status='ACTIVE'  "
		 * ) .setParameter("paymentType", routerRule.getPaymentType())
		 * .setParameter("mopType", routerRule.getMopType()).setParameter("currency",
		 * currency) .setParameter("transactionType", routerRule.getTransactionType())
		 * .setParameter("acquirerMap", routerRule.getAcquirerMap())
		 * .setParameter("merchant",
		 * routerRule.getMerchant()).setCacheable(true).uniqueResult(); tx.commit(); }
		 * catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } catch
		 * (Exception ex) { System.out.println(ex.getMessage()); } finally {
		 * autoClose(session); // return responseUser; } return routerRule1;
		 * 
		 * }
		 * 
		 * public RouterRule getMatchingRuleByAcquirerForEdit(RouterRule routerRule) {
		 * 
		 * String currency = ""; if (Currency.getNumericCode(routerRule.getCurrency())
		 * != null) { currency = Currency.getNumericCode(routerRule.getCurrency()); }
		 * else { currency = routerRule.getCurrency(); }
		 * 
		 * String paymentType = ""; if
		 * (PaymentType.getInstance(routerRule.getPaymentType()) != null) { paymentType
		 * = PaymentType.getInstance(routerRule.getPaymentType()).getCode(); }
		 * 
		 * else { paymentType = routerRule.getPaymentType(); }
		 * 
		 * String mopType = ""; if (MopType.getInstance(routerRule.getMopType()) !=
		 * null) { mopType = MopType.getInstance(routerRule.getMopType()).getCode(); }
		 * else { mopType = routerRule.getMopType(); } Session session =
		 * HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); RouterRule routerRule1 = null; try {
		 * 
		 * routerRule1 = (RouterRule) session.createQuery(
		 * "from RouterRule RR where RR.paymentType = :paymentType and  RR.mopType = :mopType and RR.currency = :currency and RR.acquirerMap = :acquirerMap and RR.transactionType = :transactionType and RR.merchant = :merchant and RR.status='ACTIVE'  "
		 * ) .setParameter("paymentType", paymentType).setParameter("mopType", mopType)
		 * .setParameter("currency", currency).setParameter("acquirerMap",
		 * routerRule.getAcquirerMap()) .setParameter("transactionType",
		 * routerRule.getTransactionType()) .setParameter("merchant",
		 * routerRule.getMerchant()).setCacheable(true).uniqueResult(); tx.commit(); }
		 * catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } catch
		 * (Exception ex) { System.out.println(ex.getMessage()); } finally {
		 * autoClose(session); // return responseUser; } return routerRule1;
		 * 
		 * }
		 * 
		 * @SuppressWarnings("unchecked") public List<RouterRule>
		 * getActiveRulesByAcquirer(String merchant , String paymentType , int start,
		 * int length, String type) { List<RouterRule> rulesList = new
		 * ArrayList<RouterRule>();
		 * 
		 * rulesList = null; Session session = HibernateSessionProvider.getSession();
		 * Transaction tx = session.beginTransaction(); try { Query query; if
		 * (StringUtils.isNotBlank(type)) { CardHolderType cardHolderType ; if
		 * (type.equalsIgnoreCase("CONSUMER")) { cardHolderType =
		 * CardHolderType.CONSUMER; } else { cardHolderType = CardHolderType.COMMERCIAL;
		 * } query=session.
		 * createQuery("from RouterRule RC where RC.merchant = :merchant and RC.paymentType = :paymentType and RC.cardHolderType = :cardHolderType order by id desc"
		 * ) .setParameter("merchant", merchant) .setParameter("paymentType",
		 * paymentType) .setParameter("cardHolderType",
		 * cardHolderType).setCacheable(true); } else { query=session.
		 * createQuery("from RouterRule RC where RC.merchant = :merchant and RC.paymentType = :paymentType order by id desc"
		 * ) .setParameter("merchant", merchant) .setParameter("paymentType",
		 * paymentType).setCacheable(true); }
		 * 
		 * 
		 * query.setFirstResult(start); query.setMaxResults(length);
		 * rulesList=query.getResultList(); tx.commit();
		 * 
		 * return rulesList; } catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); } return rulesList; }
		 * 
		 * 
		 * @SuppressWarnings("unchecked") public int
		 * getActiveRulesCountByAcquirer(String merchant , String paymentType , int
		 * start, int length, String type) { List<RouterRule> rulesList = new
		 * ArrayList<RouterRule>(); Session session =
		 * HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); try { Query query; if
		 * (StringUtils.isNotBlank(type)) { CardHolderType cardHolderType ; if
		 * (type.equalsIgnoreCase("CONSUMER")) { cardHolderType =
		 * CardHolderType.CONSUMER; } else { cardHolderType = CardHolderType.COMMERCIAL;
		 * } query=session.
		 * createQuery("from RouterRule RC where RC.merchant = :merchant and RC.paymentType = :paymentType and RC.cardHolderType = :cardHolderType order by id desc"
		 * ) .setParameter("merchant", merchant) .setParameter("paymentType",
		 * paymentType) .setParameter("cardHolderType",
		 * cardHolderType).setCacheable(true); } else { query=session.
		 * createQuery("from RouterRule RC where RC.merchant = :merchant and RC.paymentType = :paymentType order by id desc"
		 * ) .setParameter("merchant", merchant) .setParameter("paymentType",
		 * paymentType).setCacheable(true); }
		 * 
		 * rulesList=query.getResultList(); tx.commit();
		 * 
		 * return rulesList.size(); } catch (ObjectNotFoundException objectNotFound) {
		 * handleException(objectNotFound,tx); } catch (HibernateException
		 * hibernateException) { handleException(hibernateException,tx); } finally {
		 * autoClose(session); } return rulesList.size(); }
		 * 
		 * 
		 * }
		 */