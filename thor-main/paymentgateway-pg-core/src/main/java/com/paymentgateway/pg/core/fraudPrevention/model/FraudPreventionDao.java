package com.paymentgateway.pg.core.fraudPrevention.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.DataAccessObject;
import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.FraudPreventionHistory;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.pg.core.fraudPreention.dao.FraudPreventionMongoDao;

/**
 * @author Harpreet,Rahul
 *
 */

@Service
 public class FraudPreventionDao extends HibernateAbstractDao {
	private static Logger logger = LoggerFactory.getLogger(FraudPreventionDao.class.getName());
	
	@Autowired
	FraudPreventionMongoDao fraudPreventionMongoDao;
	
	public FraudPreventionDao() {
		super();
	}

	public void create(FraudPrevention fraudPrevention)
			throws DataAccessLayerException {
		super.save(fraudPrevention);
	}

	public void delete(FraudPrevention fraudPrevention)
			throws DataAccessLayerException {
		super.delete(fraudPrevention);
	}

	public void update(FraudPrevention fraudPrevention)
			throws DataAccessLayerException {
		super.saveOrUpdate(fraudPrevention);
	}

	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudRuleList(String payId) throws ParseException {
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			String query = "from FraudPrevention where status = 'ACTIVE'";
			fraudPreventionRuleList = session.createQuery(query)
									  .getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return fraudPreventionRuleList;
	}
	// to fetch rule with specific payId from fraudprevention->vj
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudRuleListbyPayId(String payId) throws ParseException {
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
         try{ 
        	 
        	 String query = "from FraudPrevention where payId =:payId and status = 'ACTIVE' ";
        	 fraudPreventionRuleList = session.createQuery(query)
 					.setParameter("payId", payId).getResultList();
        		tx.commit();
 		} catch (ObjectNotFoundException objectNotFound) {
 			handleException(objectNotFound,tx);
 		} catch (HibernateException hibernateException) {
 			handleException(hibernateException,tx);
 		} finally {
 			autoClose(session);
 		}
 		return fraudPreventionRuleList;
 	}
	
	
	public FraudPrevention getFraudRuleListbyRuleId(Long id) throws ParseException {
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		FraudPrevention fraudRule = null;
		try {
			fraudRule = (FraudPrevention) session
					.createQuery("from FraudPrevention FP where FP.id = :id and FP.status = 'ACTIVE' ")
					.setParameter("id", id).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return fraudRule;

	}
	
 	// to fetch rules with specific FraudRuleType and payId
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudRuleList(String payId, FraudRuleType fraudType) throws ParseException {
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			String query = "from FraudPrevention where payId = :payId or payId = 'ALL' AND fraudType = :fraudType  AND status = 'ACTIVE'" ;
			fraudPreventionRuleList = session.createQuery(query)
					.setParameter("payId", payId)
					.setParameter("fraudType", fraudType).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return fraudPreventionRuleList;
	}
	
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudActiveRule(String payId, FraudRuleType fraudType,TDRStatus status) throws ParseException {
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			String query = "from FraudPrevention where payId = :payId AND fraudType = :fraudType  AND status = :status";
			fraudPreventionRuleList = session.createQuery(query)
					.setParameter("payId", payId)
					.setParameter("fraudType", fraudType)
					.setParameter("status", status).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return fraudPreventionRuleList;
	}
	
	
	// list of fraud rules including 'ALL MERCHANTS'
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFullFraudRuleList(String payId) throws ParseException {
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			fraudPreventionRuleList = session.createQuery("from FraudPrevention where payId = :payId and status = 'ACTIVE'")
			.setParameter("payId", payId)
			.setCacheable(true)
			.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return fraudPreventionRuleList;
	}
	
	// to fetch total no of txn per merchant 
	public int getPerMerchantTransactions(String payId, String startTimeStamp, String endTimeStamp){
		int noOfTransactions = 0;
		try(Connection connecton = DataAccessObject.getBasicConnection() ){
			String sqlQuery = "SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE between ? and ?)";
			PreparedStatement statement = connecton.prepareStatement(sqlQuery);
			statement.setString(1, payId);
			statement.setString(2, startTimeStamp);
			statement.setString(3, endTimeStamp);
			try(ResultSet resultSet = statement.executeQuery()){
				while(resultSet.next()){
					noOfTransactions = Integer.parseInt(resultSet.getString(1));
				}
			}
		}catch (SQLException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : " , exception);
		}
		return noOfTransactions;
	}
	
	//check txns between specific interval
	public LinkedList<Long> getSpecificIPandIntervalTransactions(String ipAddress, String payId, Map<String, String> timeStampMap){
		LinkedList<Long> noOfTxnList = new LinkedList<Long>();
		try(Connection connecton = DataAccessObject.getBasicConnection() ){
			String sqlQuery = "(SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))"
					+ "UNION (SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))"
					+ "UNION (SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))"
					+ "UNION (SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))";
			PreparedStatement statement = connecton.prepareStatement(sqlQuery);
			statement.setString(1, payId);
			statement.setString(2, timeStampMap.get("hrlyStartStamp"));
			statement.setString(3, timeStampMap.get("currentStamp"));
			statement.setString(4, ipAddress);
			
			statement.setString(5, payId);
			statement.setString(6, timeStampMap.get("dailyStartStamp"));
			statement.setString(7, timeStampMap.get("currentStamp"));
			statement.setString(8, ipAddress);
			
			statement.setString(9, payId);
			statement.setString(10, timeStampMap.get("weekhlyStartStamp"));
			statement.setString(11, timeStampMap.get("currentStamp"));
			statement.setString(12, ipAddress);
			
			statement.setString(13, payId);
			statement.setString(14, timeStampMap.get("monthlyStartStamp"));
			statement.setString(15, timeStampMap.get("currentStamp"));
			statement.setString(16, ipAddress);
			
			try(ResultSet resultSet = statement.executeQuery()){
				while(resultSet.next()){
					noOfTxnList.add(Long.parseLong(resultSet.getString(1)));
				}
			}
		}catch (SQLException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : " , exception);
		}
		return noOfTxnList;
	}

	public void updateFraudRule(Long id, TDRStatus status, 
			 String repetedays, String dateActiveFrom, String dateActiveTo, String startTime, String endTime,String ipAddress,
			Boolean alwaysOnFlag) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			session.createQuery(
					"update FraudPrevention f set f.status=:status,f.repeatDays=:repeatDays,f.dateActiveFrom=:dateActiveFrom,"
							+ "f.dateActiveTo=:dateActiveTo,f.startTime=:startTime,f.endTime=:endTime,f.ipAddress=:ipAddress,f.alwaysOnFlag=:alwaysOnFlag where id=:id")
					.setParameter("status",status)
					.setParameter("repeatDays",repetedays)
					.setParameter("dateActiveFrom", dateActiveFrom)
					.setParameter("dateActiveTo",dateActiveTo)
					.setParameter("startTime", startTime)
					.setParameter("endTime", endTime)
					.setParameter("ipAddress",ipAddress)
					.setParameter("alwaysOnFlag", alwaysOnFlag)
					.setParameter("id", id).executeUpdate();

			tx.commit();
		} catch (HibernateException e) {
			handleException(e, tx);
		} finally {

			autoClose(session);
		}
	}
	
	
	public void saveFraudrule(FraudPreventionHistory fraudPreventionHistory) throws DataAccessLayerException {
		super.save(fraudPreventionHistory);
	}

	
	// duplicate fraud rule checker
	public   boolean duplicateChecker(String query){
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try{
			
			List<FraudPrevention> list = session.createQuery(query).getResultList();
			tx.commit();
			if(list.size()>0){
				return true;
			}else{	
				return false;
			}
		}catch(ObjectNotFoundException exception){
			logger.error("error : " , exception);
		}catch(HibernateException exception){
			logger.error("error : " , exception);
		}finally{
			autoClose(session);
		}
		// if something went wrong ,it will stop from creating new rule
		return true;
	}

	public void addFraudRulesInMongo() {
		try{
			List<FraudPrevention> fraudRules = getFraudRuleList("");
			logger.info("total fraud rules found "+fraudRules.size());
			
			for(FraudPrevention fraudRule :fraudRules){
				fraudPreventionMongoDao.create(fraudRule);
			}
			
		}catch (Exception e) {
			logger.info("exception in addFraudRulesInMongo() ",e);
		}
		
	}
}