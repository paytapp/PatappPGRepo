package com.paymentgateway.commons.user;


import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

@Component
public class LoginHistoryDao extends HibernateAbstractDao {
	
	private LoginHistory loginHistory = new LoginHistory();
	
	private Boolean isValid = true;
	private String validOperatingSystem;
	private String validBrowser;
	private String validIp;
	private CrmValidator validator = new CrmValidator();
		
	public LoginHistoryDao() {
        super();
    }
					
	public void create(LoginHistory loginhistory) throws DataAccessLayerException {
        super.save(loginhistory);
    }
	
	 public void delete(LoginHistory loginhistory) throws DataAccessLayerException {
	        super.delete(loginhistory);
	    }
	
	 public LoginHistory find(Long id) throws DataAccessLayerException {
	        return (LoginHistory) super.find(LoginHistory.class, id);
	    }

	 public LoginHistory find(String name) throws DataAccessLayerException {
	        return (LoginHistory) super.find(LoginHistory.class, name);
	 }
	
	/* @SuppressWarnings("unchecked")
	public  List<LoginHistory> findAll() throws DataAccessLayerException{
	        return (List<LoginHistory>) super.findAll(LoginHistory.class);
	    }*/

	 public  List<LoginHistory> findAll(int draw, int length, int startFrom) throws DataAccessLayerException{
			 List<LoginHistory> loginhistoryList = new ArrayList<LoginHistory>();
			 Session session = HibernateSessionProvider.getSession();
			 Transaction tx = session.beginTransaction();
			 try {
					
					String sqlQuery = "select * from Login_History lh left join User su on lh.emailId = su.emailId where su.userType <> 'POSMERCHANT' order by Id desc  Limit " + startFrom + "," + length + "" ;
					loginhistoryList = session.createNativeQuery(sqlQuery, LoginHistory.class).getResultList();
					tx.commit();
				} catch (ObjectNotFoundException objectNotFound) {
					handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					handleException(hibernateException,tx);
				} finally {
					autoClose(session);
				}
				return loginhistoryList;
		}

		public  List<LoginHistory> findAllUsers(String merchantPayId) throws DataAccessLayerException{
			 List<LoginHistory> loginhistoryList = new ArrayList<LoginHistory>();
			 Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
			 try {
									          
					String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where su.parentPayid = :parentPayid and su.userType <> '"+ UserType.POSMERCHANT +"' ORDER BY ID DESC";				
					loginhistoryList = session.createNativeQuery(sqlQuery, LoginHistory.class)
												 .setParameter("parentPayid", merchantPayId).getResultList();
					tx.commit();
				} catch (ObjectNotFoundException objectNotFound) {
					handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					handleException(hibernateException,tx);
				} finally {
					autoClose(session);
				}
				
				return loginhistoryList;
			}

	 public List<LoginHistory>  findLoginHisAllSubUser(String emailId1, String parentPayId) {
		 List<LoginHistory> userLoginHistory = new ArrayList<LoginHistory>();
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				
				String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId and su.parentPayid = :parentPayId and su.userType = '"+ UserType.SUBUSER +"' ORDER BY ID DESC";
				userLoginHistory = session.createNativeQuery(sqlQuery, LoginHistory.class)
											   .setParameter("emailId", emailId1)
											   .setParameter("parentPayId", parentPayId).getResultList();
				tx.commit();
				return userLoginHistory;
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
			return userLoginHistory;
		}

	 public List<LoginHistory>  findLoginHisUser(String emailId1, UserType userType,int draw, int length, int startFrom , String firstName, String lastName) {
		 List<LoginHistory> userLoginHistory = new ArrayList<LoginHistory>();
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				
				String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId and su.userType = '"+ userType +"' ORDER BY ID DESC LIMIT " + startFrom + " ," + length + "";
				userLoginHistory = session.createNativeQuery(sqlQuery,LoginHistory.class)
											   .setParameter("emailId", emailId1).getResultList();
				tx.commit();
				for (LoginHistory lh : userLoginHistory){
					lh.setBusinessName(firstName +" "+lastName);
				}
				return userLoginHistory;
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
			
			return userLoginHistory;
		}

	public void update(LoginHistory loginhistory) throws DataAccessLayerException {
	        super.saveOrUpdate(loginhistory);
	    }

	public LoginHistory findLastLoginByUser(String emailId) throws DataAccessLayerException { //second last because last is the current attempt which is logged in
		LoginHistory responseloginHistory = new LoginHistory();

		List<LoginHistory> userLoginHistory = findLoginHisUser(emailId);
		// To get second result
		int counter = 0;
		for (LoginHistory loginHistory : userLoginHistory) {			
			if (counter == 1) {
				responseloginHistory = loginHistory;
				break;
			}
			counter++;
		}
		return responseloginHistory;
	}

	protected List<LoginHistory>  findLoginHisUser(String emailId1) {
		 List<LoginHistory> userLoginHistory = new ArrayList<LoginHistory>();
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				
				String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId and su.userType = '"+ UserType.MERCHANT +"' ORDER BY ID DESC";
				userLoginHistory = session.createNativeQuery(sqlQuery, LoginHistory.class)
											   .setParameter("emailId", emailId1)
											   .getResultList();
				tx.commit();
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
			return userLoginHistory;
		}

	 public List<LoginHistory> findLoginHisByUser(String emailId1,int draw, int length, int startFrom ) throws DataAccessLayerException {
			return (List<LoginHistory>) findLoginHisUser(emailId1,draw, length, startFrom);
	 }
	 
	 protected List<LoginHistory>  findLoginHisUser(String emailId1,int draw, int length, int startFrom ) {
		 List<LoginHistory> userLoginHistory = new ArrayList<LoginHistory>();
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				
				String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId  and su.userType = '"+ UserType.MERCHANT +"'  ORDER BY ID DESC LIMIT " + startFrom + " ," + length + "";
				userLoginHistory = session.createNativeQuery(sqlQuery,LoginHistory.class)
											 .setParameter("emailId", emailId1).getResultList();
				tx.commit();
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
			return userLoginHistory;
		}
	 
	 
	 
	 public List<LoginHistory> findLoginHisByAgent(String emailId1,int draw, int length, int startFrom ) throws DataAccessLayerException {
			return (List<LoginHistory>) findLoginHisAgent(emailId1,draw, length, startFrom);
	 }
	 
	 protected List<LoginHistory>  findLoginHisAgent(String emailId1,int draw, int length, int startFrom ) {
		 List<LoginHistory> userLoginHistory = new ArrayList<LoginHistory>();
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				
				String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId  and su.userType = '"+ UserType.AGENT +"'  ORDER BY ID DESC LIMIT " + startFrom + " ," + length + "";
				userLoginHistory = session.createNativeQuery(sqlQuery,LoginHistory.class)
											 .setParameter("emailId", emailId1).getResultList();
				tx.commit();
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
			return userLoginHistory;
		}
	 public List<LoginHistory> findLoginSubAdmin(String emailId1,int draw, int length, int startFrom , String firstName ,String lastName) throws DataAccessLayerException {
			return (List<LoginHistory>) findLoginHistorySubAdmin(emailId1,draw, length, startFrom,firstName,lastName);
	 }
	 protected List<LoginHistory>  findLoginHistorySubAdmin(String emailId1,int draw, int length, int startFrom, String firstName, String lastName ) {
		 List<LoginHistory> userLoginHistory = new ArrayList<LoginHistory>();
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				
				String sqlQuery = "select lh.* from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId  and su.userType = '"+ UserType.SUBADMIN +"'  ORDER BY ID DESC LIMIT " + startFrom + " ," + length + "";
				userLoginHistory = session.createNativeQuery(sqlQuery,LoginHistory.class)
											 .setParameter("emailId", emailId1).getResultList();
				tx.commit();
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
			
			for (LoginHistory lh : userLoginHistory){
				lh.setBusinessName(firstName +" "+lastName);
			}
			return userLoginHistory;
		}

	 public void saveLoginDetails(String request, Boolean status,User user, String ip, String message){
		  Date date = new Date(); 
			UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
			ReadableUserAgent agent = parser.parse(request);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDate = sdf.format(date);
			
			validateIp(ip);
			loginHistory.setIp(getValidIp());
						
			validateBrowser(agent.getName());
			loginHistory.setBrowser(getValidBrowser());
			
			validateOS(agent.getOperatingSystem().getName());
			loginHistory.setOs(getValidOperatingSystem());
			
			loginHistory.setBusinessName(user.getBusinessName());
			loginHistory.setEmailId(user.getEmailId());
			loginHistory.setTimeStamp(formattedDate);
			loginHistory.setStatus(status);
			loginHistory.setFailureReason(message);
	    	create(loginHistory);
			
		}	
	   
	 public BigInteger countTotalAdmin() throws DataAccessLayerException{
		 BigInteger total = null ;
		 Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
		 try {
				
				String sqlQuery = "select count(*)  from Login_History lh left join User su on lh.emailId = su.emailId where su.userType <> 'POSMERCHANT'";
				total=countTotal(sqlQuery);
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} finally {
				autoClose(session);
			}
		return total;
		 
	 }
	 public BigInteger countTotalAdminByUser() throws DataAccessLayerException{
		 BigInteger total = null ;
		 
		 try {        
				String sqlQuery = "select count(*)  from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId=:emailId and su.userType = '"+ UserType.MERCHANT +"' ORDER BY ID DESC";
				countTotal(sqlQuery);
			}  catch (HibernateException hibernateException) {
				//handleException(hibernateException,tx);
			} finally {
				//	autoClose(session);
			}
		return total;
		 
	 }
	   public BigInteger countTotalFindLoginHisByUser(String email) throws DataAccessLayerException{
			 BigInteger total = null ;
			 try {       
					String sqlQuery = "select count(*) from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId='" + email+ "' and su.userType = '"+ UserType.MERCHANT +"'  ORDER BY ID DESC ";
					total = countTotal(sqlQuery);
				} catch (ObjectNotFoundException objectNotFound) {
					//handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					//handleException(hibernateException,tx);
				} finally {
					//autoClose(session);
				}
			return total;
			 
		 }
	   public BigInteger countTotalFindLoginHisByAgent(String email) throws DataAccessLayerException{
			 BigInteger total = null ;
			 try {       
					String sqlQuery = "select count(*) from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId='" + email+ "' and su.userType = '"+ UserType.AGENT +"'  ORDER BY ID DESC ";
					total = countTotal(sqlQuery);
				} catch (ObjectNotFoundException objectNotFound) {
					//handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					//handleException(hibernateException,tx);
				} finally {
					//autoClose(session);
				}
			return total;
			 
		 }
	   public BigInteger countTotalfindLoginHisUser(UserType userType,String emailId) throws DataAccessLayerException{
			 BigInteger total = null ;
			 try {       
				 String sqlQuery = "select Count(*) from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId='" + emailId+ "'and su.userType = '"+ userType +"' ORDER BY ID DESC";
				 total = countTotal(sqlQuery);
				} catch (ObjectNotFoundException objectNotFound) {
					//handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					//handleException(hibernateException,tx);
				} finally {
					//autoClose(session);
				}
			return total;
			 
		 }
	   public BigInteger countTotalfindResellerAll(String resellerId) throws DataAccessLayerException{
			 BigInteger total = null ;
			 try {       
				 String sqlQuery = "select count(*) from Login_History lh left join User su on lh.emailId = su.emailId where su.userType <> 'POSMERCHANT' and su.resellerId = '"+ resellerId +"' ORDER BY ID DESC";
				 total = countTotal(sqlQuery);
				} catch (ObjectNotFoundException objectNotFound) {
				//	handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
				//	handleException(hibernateException,tx);
				} finally {
				//	autoClose(session);
				}
			return total;
			 
		 }
	   public BigInteger countTotalfindLoginHisByUser(String email) throws DataAccessLayerException{
			 BigInteger total = null ;
			 try {       
				 String sqlQuery = "select count(*) from Login_History lh left join User su on lh.emailId = su.emailId where lh.emailId='" + email +"' and su.userType = '"+ UserType.MERCHANT +"'  ORDER BY ID DESC ";
				 total = countTotal(sqlQuery);
				} catch (ObjectNotFoundException objectNotFound) {
				//	handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
				//	handleException(hibernateException,tx);
				} finally {
				//	autoClose(session);
				}
			return total;
			 
		 }

	   public BigInteger countTotal(String getQuery) throws DataAccessLayerException{
			 BigInteger total = null ;
			 Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
			 try {
					
					String sqlQuery  = getQuery;
					total = (BigInteger)session.createNativeQuery(sqlQuery).getSingleResult();
					tx.commit();
				} catch (ObjectNotFoundException objectNotFound) {
					handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					handleException(hibernateException,tx);
				} finally {
					autoClose(session);
				}
			return total;
			 
		 }
	   
	   
		public void validateIp(String value){
					
			if(validator.validateField(CrmFieldType.IP, value)){
				setValidIp(value);
			}
			else {
				setValidIp("unknown");
			}
		}
		
		public void validateBrowser(String value){
			
			if(validator.validateField(CrmFieldType.BROWSER, value)){
				setValidBrowser(value);
			}else{
				setValidBrowser("unknown");
			}
			
		}
		
		public void validateOS(String value){
			
			if(validator.validateField(CrmFieldType.OPERATINGSYSTEM, value)){
				setValidOperatingSystem(value);
			}else{
				setValidOperatingSystem("unknown");
			}

		}

		public List<LoginHistory> findResellerAll(String resellerId ,int draw, int length, int startFrom)throws DataAccessLayerException {
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			 List<LoginHistory> loginhistoryList = new ArrayList<LoginHistory>();
			 try {
					String sqlQuery = "select * from Login_History lh left join User su on lh.emailId = su.emailId where su.userType <> 'POSMERCHANT' and su.resellerId = '"+ resellerId +"' LIMIT "+ startFrom +","+ length +"";
					loginhistoryList = session.createNativeQuery(sqlQuery, LoginHistory.class).getResultList();					
					tx.commit();
				} catch (ObjectNotFoundException objectNotFound) {
					handleException(objectNotFound,tx);
				} catch (HibernateException hibernateException) {
					handleException(hibernateException,tx);
				} finally {
					autoClose(session);
				}
				
				return loginhistoryList;
			}
		public Boolean getIsValid() {
			return isValid;
		}

		public void setIsValid(Boolean isValid) {
			this.isValid = isValid;
		}
	
		public String getValidOperatingSystem() {
			return validOperatingSystem;
		}

		public void setValidOperatingSystem(String validOperatingSystem) {
			this.validOperatingSystem = validOperatingSystem;
		}

		public String getValidBrowser() {
			return validBrowser;
		}

		public void setValidBrowser(String validBrowser) {
			this.validBrowser = validBrowser;
		}

		public String getValidIp() {
			return validIp;
		}

		public void setValidIp(String validIp) {
			this.validIp = validIp;
		}

		
}
