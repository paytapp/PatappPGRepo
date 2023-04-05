package com.paymentgateway.commons.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.DataAccessObject;
import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Acquirer;
import com.paymentgateway.commons.util.Agent;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.SubAdmin;
import com.paymentgateway.commons.util.UserStatusType;

@Component("userDao")
//@Scope(value = "prototype", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class UserDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(UserDao.class.getName());

	public UserDao() {
		super();
	}

	private static final String getCompleteUserWithEmailIdQuery = "from User U where U.emailId = :emailId";
	private static final String getCompleteUserWithPayIdQuery = "from User U where U.payId = :payId";
	private static final String getCompleteResellerWithResellerIdQuery = "from User U where U.resellerId = :resellerId and U.userType = '"
			+ UserType.RESELLER + "'";
	private static final String getCompleteUserWithMobileQuery = "from User U where U.mobile = :mobile";
	private static final String getUserTableWithPayId = "select new map (emailId as emailId, password as password, payId as payId, accHolderName as accHolderName, "
			+ "accountNo as accountNo, firstName as firstName, lastName as lastName, accountValidationKey as accountValidationKey, activationDate as activationDate,"
			+ " address as address, amountOfTransactions as amountOfTransactions, bankName as bankName, branchName as branchName, businessModel as businessModel, businessName as businessName, cin as cin, comments as comments, companyName as companyName, "
			+ "contactPerson as contactPerson, merchantType as merchantType, resellerId as resellerId, productDetail as productDetail, registrationDate as registrationDate,mobile as mobile, transactionSmsFlag as transactionSmsFlag, telephoneNo as telephoneNo, fax as fax, address as address,"
			+ " city as city, state as state, country as country, postalCode as postalCode, modeType as modeType, whiteListIpAddress as whiteListIpAddress, ifscCode as ifscCode, currency as currency, panCard as panCard, "
			+ "uploadePhoto as uploadePhoto, uploadedPanCard as uploadedPanCard, uploadedPhotoIdProof as uploadedPhotoIdProof, uploadedContractDocument as uploadedContractDocument, emailValidationFlag as emailValidationFlag, organisationType as organisationType, website as website,"
			+ " multiCurrency as multiCurrency, businessModel as businessModel, operationAddress as operationAddress, operationState as operationState, operationCity as operationCity, operationPostalCode as operationPostalCode, dateOfEstablishment as dateOfEstablishment, pan as pan, panName as panName,"
			+ " noOfTransactions as noOfTransactions, attemptTrasacation as attemptTrasacation, transactionEmailId as transactionEmailId, transactionEmailerFlag as transactionEmailerFlag, expressPayFlag as expressPayFlag, merchantHostedFlag as merchantHostedFlag, "
			+ "iframePaymentFlag as iframePaymentFlag, transactionAuthenticationEmailFlag as transactionAuthenticationEmailFlag, transactionCustomerEmailFlag as transactionCustomerEmailFlag, refundTransactionCustomerEmailFlag as refundTransactionCustomerEmailFlag, refundTransactionMerchantEmailFlag as refundTransactionMerchantEmailFlag,"
			+ "retryTransactionCustomeFlag as retryTransactionCustomeFlag, surchargeFlag as surchargeFlag, parentPayId as parentPayId, userStatus as userStatus, userType as userType, industryCategory as industryCategory, industrySubCategory as industrySubCategory,"
			+ "extraRefundLimit as extraRefundLimit, defaultCurrency as defaultCurrency, amexSellerId as amexSellerId, mCC as mCC, defaultLanguage as defaultLanguage, "
			+ "emailExpiryTime as emailExpiryTime , lastActionName as lastActionName, merchantGstNo as merchantGstNo, updateDate as updateDate, updatedBy as updatedBy) "
			+ "from User U where U.payId = :payId1";
	private final static String queryAdminList = "select payId, businessName, emailId, userStatus,Mobile,registrationDate,userType from User where (userType='ADMIN') order by payId ";
	private final static String querymerchantList = "Select emailId from User U where ((U.userType = '"
			+ UserType.MERCHANT + "') or (U.userType = '" + UserType.RESELLER + "') ) and U.userStatus='"
			+ UserStatusType.ACTIVE + "' order by emailId";
	private final static String queryAllMerchantList = "Select emailId,payId,businessName from User U where ((U.userType = '"
			+ UserType.MERCHANT + "') or (U.userType = '" + UserType.SUPERMERCHANT + "') or (U.userType = '"
			+ UserType.SUBUSER + "') or (U.userType = '" + UserType.SUBMERCHANT + "') ) order by emailId";
	private static final String getNotificationEmailerUserDetail = "from NotificationEmailer N where N.payId = :payId";
	private static final String getchargingdetails = "from ChargingDetails ch where ch.status='ACTIVE'";
	private static final String getAllSubAdmins = "from User u where u.userType = '" + UserType.SUBADMIN + "'";
	private static final String getAllSubAdminsEmail = "Select emailId from User u where u.userType = '"
			+ UserType.SUBADMIN + "'";
	private static final String getAllAdminsEmail = "Select emailId from User u where u.userType = '" + UserType.ADMIN
			+ "'";
	private static final String getAllSuperMerchants = "Select emailId from User u where u.userType = '"
			+ UserType.MERCHANT + "' and u.userStatus = '" + UserStatusType.ACTIVE + "' and isSuperMerchant = 'true'";
	private static final String getAllSubMerchants = "Select emailId from User u where u.userType = '"
			+ UserType.MERCHANT + "' and u.userStatus = '" + UserStatusType.ACTIVE
			+ "' and isSuperMerchant = 'false' and superMerchantId IS NOT NULL";
	private static final String getAllSubUsers = "Select emailId from User u where u.userType = '" + UserType.SUBUSER
			+ "' and u.userStatus = '" + UserStatusType.ACTIVE + "'";
	private static final String getAllNormalMerchant = "Select emailId from User u where u.userType = '"
			+ UserType.MERCHANT + "' and u.userStatus = '" + UserStatusType.ACTIVE + "' and superMerchantId IS NULL";
	private static final String getAllResellersMerchant = "Select emailId from User u where u.userType = '"
			+ UserType.RESELLER + "' and u.userStatus = '" + UserStatusType.ACTIVE + "'";

	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}

	public void create(User user) throws DataAccessLayerException {
		super.save(user);
	}

	public void createEmailerFalg(NotificationEmailer userFE) throws DataAccessLayerException {
		super.save(userFE);
	}

	public void delete(User User) throws DataAccessLayerException {
		super.delete(User);
	}

	public User find(Long id) throws DataAccessLayerException {
		return (User) super.find(User.class, id);
	}

	/*
	 * findchChargingDetails for marchant
	 */
	@SuppressWarnings("unchecked")
	public List<ChargingDetails> findChargingDetail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> responsechrge = null;
		try {
			responsechrge = (List<ChargingDetails>) session.createQuery(getchargingdetails).getResultList();

			tx.commit();

			return responsechrge;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error" + hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error " + e);
		} finally {
			autoClose(session);
		}
		return responsechrge;

	}

	public User find(String name) throws DataAccessLayerException {
		return (User) super.find(User.class, name);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(User.class);
	}

	public void update(User user) throws DataAccessLayerException {
		super.saveOrUpdate(user);
	}

	public void updateNotificationEamiler(NotificationEmailer user) throws DataAccessLayerException {
		super.saveOrUpdate(user);
	}

	public void updateEmailValidation(String accountValidationKey, UserStatusType userStatus,
			boolean emailValidationFlag) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			session.createQuery(
					"update User U set U.userStatus = :userStatus, U.emailValidationFlag = :emailValidationFlag"
							+ " where U.accountValidationKey = :accountValidationKey")
					.setParameter("userStatus", userStatus).setParameter("emailValidationFlag", emailValidationFlag)
					.setParameter("accountValidationKey", accountValidationKey).executeUpdate();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void updateAccountValidationKey(String accountValidationKey, String payId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			session.createQuery("update User U set U.accountValidationKey = :accountValidationKey"
					+ ",U.emailValidationFlag=0 where U.payId = :payId")
					.setParameter("accountValidationKey", accountValidationKey).setParameter("payId", payId)
					.executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void enterEmailExpiryTime(Date emailExpiryTime, String payId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			session.createQuery("UPDATE User U set U.emailExpiryTime = :emailExpiryTime"
					+ ",U.emailValidationFlag=0 where U.payId = :payId")
					.setParameter("emailExpiryTime", emailExpiryTime).setParameter("payId", payId).executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public User findPayId(String payId1) {
		return (User) findByPayId(payId1);

	}

	public User findResellerId(String resellerId) {
		return (User) findByResellerId(resellerId);

	}

	public User findUserByPhone(String phoneNo) {
		return (User) findByPhone(phoneNo);

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantActive(String emailId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			if (emailId.equals("ALL")) {
				List<Object[]> merchantListRaw = session.createQuery(
						"Select emailId, payId, businessName  from User U where U.userType = '" + UserType.MERCHANT
								+ "' and U.userStatus='" + UserStatusType.ACTIVE + "' order by businessName")
						.getResultList();

				for (Object[] objects : merchantListRaw) {
					Merchants merchant = new Merchants();
					merchant.setEmailId((String) objects[0]);
					merchant.setPayId((String) objects[1]);
					merchant.setBusinessName((String) objects[2]);
					merchantsList.add(merchant);
				}
			} else {
				List<Object[]> merchantListRaw = session.createQuery(
						"Select emailId, payId, businessName from User U where U.emailId = :emailId and U.userType = '"
								+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE + "'")
						.setParameter("emailId", emailId).setCacheable(true).getResultList();

				for (Object[] objects : merchantListRaw) {
					Merchants merchant = new Merchants();
					merchant.setEmailId((String) objects[0]);
					merchant.setPayId((String) objects[1]);
					merchant.setBusinessName((String) objects[2]);
					merchantsList.add(merchant);
				}
			}

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	public NotificationEmailer findByEmailerByPayId(String payId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		NotificationEmailer responseUser = null;
		try {
			responseUser = (NotificationEmailer) session.createQuery(getNotificationEmailerUserDetail)
					.setParameter("payId", payId).setCacheable(true).getSingleResult();
			tx.commit();

			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	protected User findByPayId(String payId1) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseUser = null;
		try {
			responseUser = (User) session.createQuery(getCompleteUserWithPayIdQuery).setParameter("payId", payId1)
					.setCacheable(true).getSingleResult();

			tx.commit();

			// userMap.put(payId1, responseUser);
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error : " , e);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	protected User findByResellerId(String resellerId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseReseller = null;
		try {
			responseReseller = (User) session.createQuery(getCompleteResellerWithResellerIdQuery)
					.setParameter("resellerId", resellerId).setCacheable(true).getSingleResult();

			tx.commit();

			// userMap.put(payId1, responseUser);
			return responseReseller;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error " , e);
		} finally {
			autoClose(session);
		}
		return responseReseller;
	}

	public User findUserByResellerId(String resellerId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseReseller = null;
		try {
			responseReseller = (User) session.createQuery(getCompleteResellerWithResellerIdQuery)
					.setParameter("resellerId", resellerId).setCacheable(true).getSingleResult();

			tx.commit();

			// userMap.put(payId1, responseUser);
			return responseReseller;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error : " , e);
		} finally {
			autoClose(session);
		}
		return responseReseller;
	}

	protected User findByPhone(String mobile) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseUser = null;
		try {
			responseUser = (User) session.createQuery(getCompleteUserWithMobileQuery).setParameter("mobile", mobile)
					.setCacheable(true).getSingleResult();

			tx.commit();

			// userMap.put(payId1, responseUser);
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error : " , e);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	public List<User> getAllSubAdmin() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<User> userList = null;
		try {
			userList = session.createQuery(getAllSubAdmins).getResultList();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllAdminsEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();
		try {
			emailList = session.createQuery(getAllAdminsEmail).getResultList();

			tx.commit();
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllSubAdminsEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();
		try {
			emailList = session.createQuery(getAllSubAdminsEmail).getResultList();

			tx.commit();
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllSuperMerchantsEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();
		try {
			emailList = session.createQuery(getAllSuperMerchants).getResultList();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllSubMerchantsEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();

		try {
			emailList = session.createQuery(getAllSubMerchants).getResultList();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllMerchants() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Merchants> merchantsList = new ArrayList<Merchants>();

		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select payId, businessName, merchantVPA from User u where u.userType = '"
							+ UserType.MERCHANT + "' and superMerchantId IS NULL")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setBusinessName((String) objects[1]);
				merchant.setMerchantVPA((String) objects[2]);
				merchantsList.add(merchant);
			}

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllSubMerchants() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select payId, businessName, merchantVPA from User u where u.userType = '"
							+ UserType.MERCHANT + "' and isSuperMerchant = 'false' and superMerchantId IS NOT NULL")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setBusinessName((String) objects[1]);
				merchant.setMerchantVPA((String) objects[2]);
				merchantsList.add(merchant);
			}

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllSubUsersEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();

		try {
			emailList = session.createQuery(getAllSubUsers).getResultList();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllResellersEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();
		try {
			emailList = session.createQuery(getAllResellersMerchant).getResultList();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllNormalMerchantsEmail() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> emailList = new ArrayList<String>();

		try {
			emailList = session.createQuery(getAllNormalMerchant).getResultList();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailList;
	}

	@SuppressWarnings("unchecked")
	public List<User> getMerchantsByIndustryCatagory(String industryCategory) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<User> merchantList = null;
		try {
			merchantList = session.createQuery("from User u where u.industryCategory = :industryCategory")
					.setParameter("industryCategory", industryCategory).getResultList();

			tx.commit();
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantList;
	}

	protected Object getUserObj(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		Object userObject = null;
		try {
			userObject = session.createQuery(getUserTableWithPayId).setParameter("payId1", payId).setCacheable(true)
					.getSingleResult();

			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userObject;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getUserObjMap(String payId) {

		Map<String, Object> userDetailsMap = null;
		Object userObject = getUserObj(payId);

		if (null != userObject) {
			userDetailsMap = (Map<String, Object>) userObject;
		}
		return userDetailsMap;
	}

	public User getUserClass(String payId) {
		User responseUser = new User();
		Map<String, Object> userDetailsMap = getUserObjMap(payId);
		if (null == userDetailsMap) {
			return null;
		} else {
			responseUser.setEmailId((String) userDetailsMap.get(CrmFieldType.EMAILID.getName()));
			responseUser.setModeType((ModeType) userDetailsMap.get(CrmFieldConstants.MODE_TYPE.getValue()));
			responseUser.setAccHolderName((String) userDetailsMap.get(CrmFieldType.ACC_HOLDER_NAME.getName()));
			responseUser.setAccountNo((String) userDetailsMap.get(CrmFieldType.ACCOUNT_NO.getName()));
			responseUser.setAccountValidationKey(
					(String) userDetailsMap.get(CrmFieldType.ACCOUNT_VALIDATION_KEY.getName()));
			responseUser.setActivationDate((Date) userDetailsMap.get(CrmFieldType.ACTIVATION_DATE.getName()));
			responseUser.setAddress((String) userDetailsMap.get(CrmFieldType.ADDRESS.getName()));
			responseUser.setAmountOfTransactions(
					(String) userDetailsMap.get(CrmFieldType.AMOUNT_OF_TRANSACTIONS.getName()));
//			responseUser.setAttemptTrasacation((String) userDetailsMap.get(CrmFieldType.ATTEMPT_TRASACATION.getName()));
			responseUser.setBankName((String) userDetailsMap.get(CrmFieldType.BANK_NAME.getName()));
			responseUser.setBranchName((String) userDetailsMap.get(CrmFieldType.BRANCH_NAME.getName()));
			responseUser.setBusinessModel((String) userDetailsMap.get(CrmFieldType.BUSINESSMODEL.getName()));
			responseUser.setBusinessName((String) userDetailsMap.get(CrmFieldType.BUSINESS_NAME.getName()));
			/*
			 * responseUser.setBusinessType((BusinessType) userDetailsMap
			 * .get(CrmFieldType.BUSINESS_TYPE.getName()));
			 */
			responseUser.setCin((String) userDetailsMap.get(CrmFieldType.CIN.getName()));
			responseUser.setCity((String) userDetailsMap.get(CrmFieldType.CITY.getName()));
			responseUser.setComments((String) userDetailsMap.get(CrmFieldType.COMMENTS.getName()));
			responseUser.setCompanyName((String) userDetailsMap.get(CrmFieldType.COMPANY_NAME.getName()));
			responseUser.setContactPerson((String) userDetailsMap.get(CrmFieldType.CONTACT_PERSON.getName()));
			responseUser.setCountry((String) userDetailsMap.get(CrmFieldType.COUNTRY.getName()));
			responseUser.setCurrency((String) userDetailsMap.get(CrmFieldType.CURRENCY.getName()));
			responseUser
					.setDateOfEstablishment((String) userDetailsMap.get(CrmFieldType.DATE_OF_ESTABLISHMENT.getName()));
			responseUser.setEmailValidationFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.EMAIL_VALIDATION_FLAG.getValue()));
//			responseUser.setExpressPayFlag((boolean) userDetailsMap.get(CrmFieldConstants.EXPRESS_PAY_FLAG.getValue()));
			responseUser.setFax((String) userDetailsMap.get(CrmFieldType.FAX.getName()));
			responseUser.setFirstName((String) userDetailsMap.get(CrmFieldType.FIRSTNAME.getName()));
//			responseUser.setIframePaymentFlag(
//					(boolean) userDetailsMap.get(CrmFieldConstants.IFRAME_PAYMENT_FLAG.getValue()));
			responseUser.setIfscCode((String) userDetailsMap.get(CrmFieldType.IFSC_CODE.getName()));
			responseUser.setLastName((String) userDetailsMap.get(CrmFieldType.LASTNAME.getName()));
//			responseUser.setMerchantHostedFlag(
//					(boolean) userDetailsMap.get(CrmFieldConstants.MERCHANT_HOSTED_FALAG.getValue()));
			responseUser.setMerchantType((String) userDetailsMap.get(CrmFieldType.MERCHANT_TYPE.getName()));
			responseUser.setMobile((String) userDetailsMap.get(CrmFieldType.MOBILE.getName()));
			responseUser.setMultiCurrency((String) userDetailsMap.get(CrmFieldType.MULTICURRENCY.getName()));
			responseUser.setNoOfTransactions((String) userDetailsMap.get(CrmFieldType.NO_OF_TRANSACTIONS.getName()));
			responseUser.setOperationAddress((String) userDetailsMap.get(CrmFieldType.OPERATIONADDRESS.getName()));
			responseUser.setOperationCity((String) userDetailsMap.get(CrmFieldType.OPERATION_CITY.getName()));
			responseUser
					.setOperationPostalCode((String) userDetailsMap.get(CrmFieldType.OPERATION_POSTAL_CODE.getName()));
			responseUser.setOperationState((String) userDetailsMap.get(CrmFieldType.PPERATION_STATE.getName()));
			responseUser.setOrganisationType((String) userDetailsMap.get(CrmFieldType.ORGANIZATIONTYPE.getName()));
			responseUser.setPan((String) userDetailsMap.get(CrmFieldType.PAN.getName()));
			responseUser.setPanCard((String) userDetailsMap.get(CrmFieldType.PANCARD.getName()));
			responseUser.setPanName((String) userDetailsMap.get(CrmFieldType.PANNAME.getName()));
			responseUser.setParentPayId((String) userDetailsMap.get(CrmFieldType.PARENT_PAY_ID.getName()));
			responseUser.setPassword((String) userDetailsMap.get(CrmFieldType.PASSWORD.getName()));
			responseUser.setPayId((String) userDetailsMap.get(CrmFieldType.PAY_ID.getName()));
			responseUser.setPostalCode((String) userDetailsMap.get(CrmFieldType.POSTALCODE.getName()));
			responseUser.setProductDetail((String) userDetailsMap.get(CrmFieldType.PRODUCT_DETAIL.getName()));
			responseUser.setRegistrationDate((Date) userDetailsMap.get(CrmFieldType.REGISTRATION_DATE.getName()));
			responseUser.setResellerId((String) userDetailsMap.get(CrmFieldType.RESELLER_ID.getName()));
			responseUser.setState((String) userDetailsMap.get(CrmFieldType.STATE.getName()));
//			responseUser.setRetryTransactionCustomeFlag(
//					(boolean) userDetailsMap.get(CrmFieldConstants.RETRY_TRANSACTION_FLAG.getValue()));
			responseUser.setTelephoneNo((String) userDetailsMap.get(CrmFieldType.TELEPHONE_NO.getName()));
			responseUser.setTransactionAuthenticationEmailFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.TRANSACTION_AUTHENTICATION_EMAIL_FLAG.getValue()));
			responseUser.setTransactionCustomerEmailFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.TRANSACTION_CUSTOMER_EMAIL_FLAG.getValue()));
			responseUser.setRefundTransactionCustomerEmailFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.REFUND_TXN_CUSTOMER_EMAIL_FLAG.getValue()));
			responseUser.setRefundTransactionMerchantEmailFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.REFUND_TXN_MERCHANT_EMAIL_FLAG.getValue()));
			responseUser.setTransactionEmailerFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.TRANSACTION_EMAILER_FLAG.getValue()));
			responseUser
					.setTransactionEmailId((String) userDetailsMap.get(CrmFieldType.TRANSACTION_EMAIL_ID.getName()));
			responseUser.setTransactionSmsFlag(
					(boolean) userDetailsMap.get(CrmFieldConstants.TRANSACTION_SMS_FLAG.getValue()));
			responseUser.setUploadedContractDocument(
					(String) userDetailsMap.get(CrmFieldType.UPLOADE_CONTRACT_DOCUMENT.getName()));
			responseUser.setUploadedPanCard((String) userDetailsMap.get(CrmFieldType.UPLOADE_PAN_CARD.getName()));
			responseUser
					.setUploadedPhotoIdProof((String) userDetailsMap.get(CrmFieldType.UPLOADE_PHOTOID_PROOF.getName()));
			responseUser.setUploadePhoto((String) userDetailsMap.get(CrmFieldType.UPLOADE_PHOTO.getName()));
			responseUser.setUserStatus((UserStatusType) userDetailsMap.get(CrmFieldType.USERSTATUS.getName()));
			responseUser.setUserType((UserType) userDetailsMap.get(CrmFieldConstants.USER_TYPE.getValue()));
			responseUser.setWebsite((String) userDetailsMap.get(CrmFieldType.WEBSITE.getName()));
			responseUser.setWhiteListIpAddress((String) userDetailsMap.get(CrmFieldType.WHITE_LIST_IPADDRES.getName()));
			responseUser.setExtraRefundLimit((float) userDetailsMap.get(CrmFieldType.EXTRA_REFUND_LIMIT.getName()));
			responseUser.setDefaultCurrency((String) userDetailsMap.get(CrmFieldType.DEFAULT_CURRENCY.getName()));
			responseUser.setAmexSellerId((String) userDetailsMap.get(CrmFieldType.AMEX_SELLER_ID.getName()));
//			responseUser.setMCC((String) userDetailsMap.get(CrmFieldType.MCC.getName()));
//			responseUser.setSurchargeFlag((boolean) userDetailsMap.get(CrmFieldConstants.SURCHARGE_FLAG.getValue()));
			responseUser.setIndustryCategory((String) userDetailsMap.get(CrmFieldType.INDUSTRY_CATEGORY.getName()));
			responseUser
					.setIndustrySubCategory((String) userDetailsMap.get(CrmFieldType.INDUSTRY_SUB_CATEGORY.getName()));
			responseUser.setDefaultLanguage((String) userDetailsMap.get(CrmFieldType.DEFAULT_LANGUAGE.getName()));
			responseUser.setEmailExpiryTime((Date) userDetailsMap.get(CrmFieldType.EMAIL_EXPIRY_TIME.getName()));
			responseUser.setLastActionName((String) userDetailsMap.get(CrmFieldType.LAST_ACTION_NAME.getName()));
			responseUser.setMerchantGstNo((String) userDetailsMap.get(CrmFieldType.MERCHANT_GST_NUMBER.getName()));
			responseUser.setUpdateDate((Date) userDetailsMap.get(CrmFieldType.UPDATE_DATE.getName()));
			responseUser.setUpdatedBy((String) userDetailsMap.get(CrmFieldType.UPDATED_BY.getName()));

		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	public User findByAccountValidationKey(String accountValidationKey) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseUser = null;
		try {
			List<User> users = session.createQuery("from User U where U.accountValidationKey = :accountValidationKey")
					.setParameter("accountValidationKey", accountValidationKey).getResultList();
			for (User user : users) {
				responseUser = user;
				break;
			}
			tx.commit();

			return responseUser;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}

		return responseUser;
	}

	@SuppressWarnings("rawtypes")
	public List getMerchantActiveList() throws DataAccessLayerException {
		return getMerchantActive();
	}

	@SuppressWarnings("unchecked")
	protected List<Merchants> getMerchantActive() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,superMerchantId,isSuperMerchant from User U where U.userType = '"
							+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE + "' or U.userStatus='"
							+ UserStatusType.TRANSACTION_BLOCKED + "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {

				// Don't include sub-merchants in mapping list
				if (objects[3] != null && objects[4] != null) {
					if (StringUtils.isNotBlank((String) objects[3]) && !(boolean) objects[4]) {
						continue;
					}
				}

				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				merchant.setIsSuperMerchant((boolean) objects[4]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllStatusMerchantList() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,superMerchantId,isSuperMerchant from User U where U.userType = '"
							+ UserType.MERCHANT + "' and U.userStatus !='" + UserStatusType.PENDING
							+ "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {

				// Don't include sub-merchants in mapping list
				if (objects[3] != null && objects[4] != null) {
					if (StringUtils.isNotBlank((String) objects[3]) && !(boolean) objects[4]) {
						continue;
					}
				}

				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Merchants> getNormalMerchantList() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName from User U where U.userType = '" + UserType.MERCHANT
							+ "' and U.isSuperMerchant is false and U.superMerchantId is null and U.userStatus ='"
							+ UserStatusType.ACTIVE + "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {

				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}
	
	

	


	@SuppressWarnings("rawtypes")
	public List getMerchantList() throws DataAccessLayerException {
		return getMerchants();
	}

	@SuppressWarnings("unchecked")
	protected List<Merchants> getMerchants() {
		List<Merchants> merchantsList = new ArrayList<Merchants>();

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,superMerchantId,isSuperMerchant from User U where U.userType = '"
							+ UserType.MERCHANT + "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {

				// Don't include sub-merchants in mapping list
				if (objects[3] != null && objects[4] != null) {
					if (StringUtils.isNotBlank((String) objects[3]) && !(boolean) objects[4]) {
						continue;
					}
				}
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantListByResellerID(String resellerId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,superMerchantId,isSuperMerchant from User U where U.userType = '"
							+ UserType.MERCHANT + "' and resellerId='" + resellerId + "'order by businessName")
					.getResultList();
			for (Object[] objects : merchantListRaw) {
				if (objects[3] != null && objects[4] != null) {
					if (StringUtils.isNotBlank((String) objects[3]) && !(boolean) objects[4]) {
						continue;
					}
				}
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("rawtypes")
	public List getActiveMerchantList() throws DataAccessLayerException {
		return getActiveMerchants();
	}

	@SuppressWarnings("unchecked")
	protected List<Merchants> getActiveMerchants() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select emailId, payId, businessName from User U where ((U.userType = '"
							+ UserType.MERCHANT + "') or (U.userType = '" + UserType.RESELLER
							+ "') ) and U.userStatus='" + UserStatusType.ACTIVE + "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubUserList(String parentPayId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,subUserType,firstName,lastName from User U where U.userType = '"
							+ UserType.SUBUSER + "' and U.parentPayId = '" + parentPayId
							+ "' and U.userStatus = 'ACTIVE'")
					.getResultList();
			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSubUserType((String) objects[3]);
				merchant.setFirstName((String) objects[4]);
				merchant.setLastName((String) objects[5]);
				if (StringUtils.isEmpty(merchant.getBusinessName()))
					merchant.setBusinessName(merchant.getFirstName() + " " + merchant.getLastName());
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllSubUserList() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select emailId, payId, businessName,subUserType from User U where U.userType = '"
							+ UserType.SUBUSER + "' and U.userStatus = 'ACTIVE' and U.subUserType != 'eposType'")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSubUserType((String) objects[3]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getVendorDeatilByPayId(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select emailId, payId, businessName,subUserType from User U where U.userType = '"
							+ UserType.SUBUSER + "' and U.payId = '" + payId)
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSubUserType((String) objects[3]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getKhadiSubUserList(String parentPayId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,firstName,lastName,subUserType from User U where U.userType = '"
							+ UserType.SUBUSER + "' and U.parentPayId = '" + parentPayId
							+ "' and U.userStatus = 'ACTIVE' and U.khadiMerchant = TRUE ")
					.getResultList();
			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);

				String name = "";
				if (StringUtils.isBlank((String) objects[2])) {
					name = (String) objects[3] + " " + (String) objects[4];
				} else {
					name = (String) objects[2];
				}

				merchant.setBusinessName(name);
				merchant.setSubUserType((String) objects[5]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubUsers(String parentPayId, UserStatusType status) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			String hql = "Select payId, emailId, firstName, lastName, mobile,registrationDate, userStatus, subUserType from User U where U.userType = '"
					+ UserType.SUBUSER + "' and parentPayId='" + parentPayId + "'";
			if (status != null) {
				hql = "Select payId, emailId, firstName, lastName, mobile,registrationDate, userStatus, subUserType from User U where U.userType = '"
						+ UserType.SUBUSER + "' and parentPayId='" + parentPayId + "' and U.userStatus='" + status
						+ "'";
			}
			// logger.info("hibernate
			// query======================================================================================================="+hql);
			List<Object[]> merchantListRaw = session.createQuery(hql).getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setFirstName((String) objects[2]);
				merchant.setLastName((String) objects[3]);
				merchant.setMobile((String) objects[4]);
				if (objects[5] == null) {
					merchant.setRegistrationDate("NA");
				} else {
					Format formatter = new SimpleDateFormat("yyyy-MM-dd");
					String s = formatter.format(objects[5]);
					merchant.setRegistrationDate(s);
				}
				if (((UserStatusType) objects[6]).equals(UserStatusType.ACTIVE)) {
					merchant.setIsActive(true);
				} else if (((UserStatusType) objects[6]).equals(UserStatusType.PENDING)) {
					merchant.setIsActive(false);
				}
				if (StringUtils.isNotBlank(((String) objects[7]))) {
					merchant.setSubUserType((String) objects[7]);
				} else {
					merchant.setSubUserType("NA");
				}

				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	// get Agents
	@SuppressWarnings("unchecked")
	public List<SubAdmin> getUsers(String parentPayId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SubAdmin> agentsList = new ArrayList<SubAdmin>();
		try {
			List<Object[]> agentListRaw = session.createQuery(
					"Select payId, emailId, firstName, lastName, mobile, permissionType, userStatus from User U where U.userType = '"
							+ UserType.SUBADMIN + "' and parentPayId='" + parentPayId + "'")
					.getResultList();

			for (Object[] objects : agentListRaw) {
				SubAdmin subAdmin = new SubAdmin();
				subAdmin.setPayId((String) objects[0]);
				subAdmin.setAgentEmailId((String) objects[1]);
				subAdmin.setAgentFirstName((String) objects[2]);
				subAdmin.setAgentLastName((String) objects[3]);
				subAdmin.setAgentMobile((String) objects[4]);
				subAdmin.setPermissionType((String) objects[5]);
				if (((UserStatusType) objects[6]).equals(UserStatusType.ACTIVE)) {
					subAdmin.setAgentIsActive(true);
				} else if (((UserStatusType) objects[6]).equals(UserStatusType.PENDING)) {
					subAdmin.setAgentIsActive(false);
				}
				agentsList.add(subAdmin);
			}
			tx.commit();
			return agentsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return agentsList;
	}

	// get Acquirers
	@SuppressWarnings("unchecked")
	public List<Acquirer> getAcquirers() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Acquirer> acquirersList = new ArrayList<Acquirer>();
		try {
			List<Object[]> agentListRaw = session.createQuery(
					"Select payId, emailId, firstName, lastName, businessName, accountNo, userStatus from User U where U.userType = '"
							+ UserType.ACQUIRER + "' ")
					.getResultList();

			for (Object[] objects : agentListRaw) {
				Acquirer acquirer = new Acquirer();
				acquirer.setPayId((String) objects[0]);
				acquirer.setAcquirerEmailId((String) objects[1]);
				acquirer.setAcquirerFirstName((String) objects[2]);
				acquirer.setAcquirerLastName((String) objects[3]);
				acquirer.setAcquirerBusinessName((String) objects[4]);
				acquirer.setAcquirerAccountNo((String) objects[5]);
				if (((UserStatusType) objects[6]).equals(UserStatusType.ACTIVE)) {
					acquirer.setAcquirerIsActive(true);
				} else {
					acquirer.setAcquirerIsActive(false);
				}
				acquirersList.add(acquirer);
			}
			tx.commit();
			return acquirersList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return acquirersList;
	}

	@SuppressWarnings("unchecked")
	public List<Agent> getAgent() {
		List<Agent> agentsList = new ArrayList<Agent>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> agentListRaw = session.createQuery(
					"Select payId, emailId, firstName, lastName, mobile, userStatus from User U where U.userType = '"
							+ UserType.AGENT + "' ")
					.getResultList();

			for (Object[] objects : agentListRaw) {
				Agent agent = new Agent();
				agent.setPayId((String) objects[0]);
				agent.setAgentEmailId((String) objects[1]);
				agent.setAgentFirstName((String) objects[2]);
				agent.setAgentLastName((String) objects[3]);
				agent.setAgentMobile((String) objects[4]);
				if (((UserStatusType) objects[5]).equals(UserStatusType.ACTIVE)) {
					agent.setAgentIsActive(true);
				} else if (((UserStatusType) objects[5]).equals(UserStatusType.PENDING)) {
					agent.setAgentIsActive(false);
				}
				agentsList.add(agent);
			}
			tx.commit();
			return agentsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return agentsList;
	}

	public User findAcquirerByCode(String acquirerCode) {
		User user = getAcquirer(acquirerCode);
		return user;
	}

	protected User getAcquirer(String acqCode) {
		User responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			responseUser = (User) session
					.createQuery("from User U where U.userType='ACQUIRER' and U.firstName = :acqCode")
					.setParameter("acqCode", acqCode).setCacheable(true).getSingleResult();
			tx.commit();

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	public User findPayIdByEmail(String emailId) {
		User user = getPayId(emailId);
		return user;
	}

	public User findPayIdByPhoneNumber(String phoneNumber) {
		User user = getPayIdByPhoneNumber(phoneNumber);
		return user;
	}

	@SuppressWarnings("unchecked")
	protected User getPayId(String emailId) {
		User responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<User> users = session.createQuery("from User U where U.emailId = :emailId")
					.setParameter("emailId", emailId).getResultList();

			for (User user : users) {
				responseUser = user;
				break;
			}

			tx.commit();
			return responseUser;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	protected User getPayIdByPhoneNumber(String phoneNumber) {
		User responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<User> users = session.createQuery("from User U where U.mobile = :mobile")
					.setParameter("mobile", phoneNumber).getResultList();

			for (User user : users) {
				responseUser = user;
				break;
			}

			tx.commit();
			return responseUser;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("rawtypes")
	public List getResellerList() throws DataAccessLayerException {
		return getResellers();
	}

	@SuppressWarnings("unchecked")
	private List<Merchants> getResellers() {
		List<Merchants> resellerList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select emailId, payId, resellerId, businessName from User U where U.userType = '"
							+ UserType.RESELLER + "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setResellerId((String) objects[2]);
				merchant.setBusinessName((String) objects[3]);
				resellerList.add(merchant);
			}
			tx.commit();

			return resellerList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return resellerList;
	}

	public List<Merchants> getActiveResellerMerchantList(String reselleId) throws DataAccessLayerException {
		return getActiveResellerMerchants(reselleId);
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getActiveResellerMerchants(String resellerId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select emailId, payId, businessName from User U where U.userType = '"
							+ UserType.MERCHANT + "'and U.userStatus='" + UserStatusType.ACTIVE + "' and resellerId = '"
							+ resellerId
							+ "' and (isSuperMerchant = true and superMerchantId IS NOT NULL or isSuperMerchant = false and superMerchantId IS NULL) order by businessName")
					.getResultList();
			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("rawtypes")
	public List getResellerMerchantList(String resellerId) throws DataAccessLayerException {

		return getResellerMerchant(resellerId);
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getResellerMerchant(String resellerId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName, retailMerchantFlag from User U where U.userType = '"
							+ UserType.MERCHANT + "'and U.userStatus='" + UserStatusType.ACTIVE + "' and resellerId = '"
							+ resellerId + "'")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
//				merchant.setRetailMerchantFlag((boolean) objects[3]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAcquirerSubUsers(String parentPayId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select payId, emailId, firstName, lastName, mobile, userStatus from User U where U.userType = '"
							+ UserType.SUBACQUIRER + "' and parentPayId='" + parentPayId + "'")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setFirstName((String) objects[2]);
				merchant.setLastName((String) objects[3]);
				merchant.setMobile((String) objects[4]);
				if (((UserStatusType) objects[5]).equals(UserStatusType.ACTIVE)) {
					merchant.setIsActive(true);
				} else if (((UserStatusType) objects[5]).equals(UserStatusType.PENDING)) {
					merchant.setIsActive(false);
				}
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	public List<User> getUserActiveList() throws DataAccessLayerException {
		return getUserActive();
	}

	@SuppressWarnings("unchecked")
	private List<User> getUserActive() {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<User> userList = new ArrayList<User>();
		try {
			userList = session.createQuery(" from User U where U.userStatus='" + UserStatusType.ACTIVE + "'")
					.getResultList();
			tx.commit();

			return userList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userList;
	}

	@SuppressWarnings({ "unchecked" })
	public List<String> getMerchantEmailIdListByBusinessType(String businessType) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<String> merchantEmailList = new ArrayList<String>();
		try {
			merchantEmailList = session
					.createQuery(" Select U.emailId from User U where U.userStatus='" + UserStatusType.ACTIVE + "'"
							+ " and U.userType = 'MERCHANT' and U.industryCategory = :businessType")
					.setParameter("businessType", businessType).getResultList();
			tx.commit();

			return merchantEmailList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantEmailList;
	}

	public String getMerchantNameByPayId(String payId) {
		String name = null;
		String firstName = null;
		String lastName = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			firstName = (String) session.createQuery("Select firstName from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			lastName = (String) session.createQuery("Select lastName from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			name = firstName + " " + lastName;
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return name;
	}

	public String getMerchantByPayId(String payId) {

		String businessName = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			businessName = (String) session.createQuery("Select businessName from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return businessName;
	}

	@SuppressWarnings("unchecked")
	public List<MerchantDetails> getAllAdminList() {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery("Select payId, emailId, businessName, mobile, registrationDate, userType, userStatus from User U where U.userType = 'ADMIN' ").getResultList();
			tx.commit();

			for (Object[] objects : userList) {

				MerchantDetails merchant = new MerchantDetails();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setMobile((String) objects[3]);
				if ((String) objects[4] != null) {
					Format formatter = new SimpleDateFormat("yyyy-MM-dd");
					String s = formatter.format((String) objects[4]);
					merchant.setRegistrationDate(s);
				} else {
					merchant.setRegistrationDate("");
				}
				merchant.setUserType((String) objects[5]);
				merchant.setStatus((UserStatusType) objects[6]);

				merchants.add(merchant);

			}
			return merchants;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchants;
	}

	public List<Merchants> featchAllmerchant() throws SystemException {
		List<Merchants> merchants = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection.prepareStatement(querymerchantList)) {
				try (ResultSet rs = prepStmt.executeQuery()) {
					while (rs.next()) {
						Merchants merchant = new Merchants();
						merchant.setEmailId(rs.getString("emailId"));
						merchants.add(merchant);
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		} finally {
			autoClose(session);
		}
		return merchants;

	}

	// public List<Merchants> fetchAllmerchant() throws SystemException {
	// List<Merchants> merchants = new ArrayList<Merchants>();
	// Session session = HibernateSessionProvider.getSession();
	// Transaction tx = session.beginTransaction();
	// try (Connection connection = getConnection()) {
	// try (PreparedStatement prepStmt =
	// connection.prepareStatement(queryAllMerchantList)) {
	// try (ResultSet rs = prepStmt.executeQuery()) {
	// while (rs.next()) {
	// Merchants merchant = new Merchants();
	// merchant.setEmailId(rs.getString("emailId"));
	// merchant.setPayId(rs.getString("payId"));
	// merchant.setBusinessName(rs.getString("businessName"));
	// merchants.add(merchant);
	// }
	// }
	// }
	// } catch (SQLException exception) {
	// logger.error("Database error", exception);
	// throw new SystemException(ErrorType.DATABASE_ERROR,
	// ErrorType.DATABASE_ERROR.getResponseMessage());
	// } finally {
	// autoClose(session);
	// }
	// return merchants;
	//
	// }

	
	public String getMerchantPhoneNoByPayId(String payId) {
		String mobileNo = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			mobileNo = (String) session.createQuery("Select mobile  from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return mobileNo;
	}
	public String getBusinessNameByEmailId(String emailId) {
		String businessName = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			businessName = (String) session.createQuery("Select businessName from User U where U.emailId = :emailId")
					.setParameter("emailId", emailId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return businessName;
	}

	public String getBusinessNameByPayId(String payId) {
		String businessName = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			businessName = (String) session.createQuery("Select businessName from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return businessName;
	}

	public String getAdminEmail() {
		String emailId = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			emailId = (String) session.createQuery("Select emailId from User U where U.userType = :userType")
					.setParameter("userType", UserType.ADMIN).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailId;
	}

	public String getEmailIdByBusinessName(String businessName) {
		String emailId = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			emailId = (String) session.createQuery("Select emailId from User U where U.businessName = :businessName")
					.setParameter("businessName", businessName).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailId;
	}

	public String getIndustryCategoryByPayId(String payId) {
		String industryCategory = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			industryCategory = (String) session
					.createQuery("Select industryCategory from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return industryCategory;
	}

	public String getPayIdByEmailId(String emailId) {
		String payId = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			payId = (String) session.createQuery("Select payId from User U where U.emailId = :emailId")
					.setParameter("emailId", emailId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return payId;
	}

	public String getEmailIdByPayId(String payId) {
		String emailId = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			emailId = (String) session.createQuery("Select emailId from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emailId;
	}

	/*
	 * checkSurchargeFlagByPayId
	 */
	@SuppressWarnings("unchecked")
	public List<String> payIdWithTdrMode() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> payId = null;
		try {

			payId = (List<String>) session.createQuery("Select payId from User U where U.surchargeFlag=0")
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return payId;
	}

	public String getpayIdByBusinessName(String businessName) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		String payId = null;
		try {
			payId = (String) session.createQuery("select payId from User U where U.businessName = :businessName")
					.setParameter("businessName", businessName).getSingleResult();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return payId;
	}

	public String findAdminPayIdForSaveCardToken(String emailId) {
		String payId = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			payId = (String) session.createQuery(
					"Select payId from User U where U.emailId = :emailId and userType='ADMIN' and userStatus='ACTIVE'")
					.setParameter("emailId", emailId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return payId;
	}

	@SuppressWarnings("unchecked")
	public List<MerchantDetails> fetchMerchantsCreatedBySubAdmin(String createdBy) {

		List<MerchantDetails> merchantList = new ArrayList<MerchantDetails>();
		List<Object[]> userList = new ArrayList<Object[]>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			if (!StringUtils.isEmpty(createdBy)) {
				userList = session.createQuery(
						"Select registrationDate, payId, businessName, userStatus, merchantCreatorName, mpaStage from User U where U.merchantCreatedBy = :merchantCreatedBy"
								+ " and userType='MERCHANT'")
						.setParameter("merchantCreatedBy", createdBy).getResultList();
			} else {
				userList = session.createQuery(
						"Select registrationDate, payId, businessName, userStatus, merchantCreatorName, mpaStage from User U where userType='MERCHANT'")
						.getResultList();
			}
			tx.commit();

			for (Object[] objects : userList) {
				String registrationDate = null;
				if (null != (String) objects[0]) {
					registrationDate = formatter.format((String) objects[0]);
				}
				MerchantDetails merchant = new MerchantDetails();

				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setRegistrationDate(registrationDate);
				merchant.setStatus((UserStatusType) objects[3]);
				merchant.setCreatedBy((String) objects[4]);
				if(StringUtils.isNotBlank((String) objects[5])) {
					merchant.setMpaStage((String) objects[5]);	
				}else {
					merchant.setMpaStage("PENDING");
				}
				merchantList.add(merchant);
			}

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantList;
	}

	@SuppressWarnings("unchecked")
	public User findBySuperMerchantId(String superMerchantId) {
		boolean isSuperMerchant = true;
		User responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			List<User> users = session.createQuery(
					"from User U where U.superMerchantId = :superMerchantId and  U.isSuperMerchant = :isSuperMerchant and  U.userStatus = 'ACTIVE'")
					.setParameter("superMerchantId", superMerchantId).setParameter("isSuperMerchant", isSuperMerchant)
					.getResultList();

			for (User user : users) {
				responseUser = user;
				break;
			}
			tx.commit();

			return responseUser;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getActiveSubMerchants(User user) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName,superMerchantId, customerQrFlag from User U where U.userType = '"
							+ UserType.MERCHANT + "'and U.userStatus='" + UserStatusType.ACTIVE
							+ "' and superMerchantId = '" + user.getSuperMerchantId() + "' order by businessName")
					.getResultList();
			for (Object[] objects : merchantListRaw) {

				if (StringUtils.isNotBlank((String) objects[3])) {
					if (((String) objects[1]).equalsIgnoreCase(user.getPayId())) {
						continue;
					}
				}
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				merchant.setCustomerQrFlag((boolean) objects[4]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	public String getPayIdBySuperMerchId(String superMerchId) {
		String payId = null;
		boolean isSuperMerchant = true;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			payId = (String) session.createQuery(
					"Select payId from User U where U.superMerchantId = :superMerchantId and U.isSuperMerchant = :isSuperMerchant and U.userStatus = 'ACTIVE'")
					.setParameter("superMerchantId", superMerchId).setParameter("isSuperMerchant", isSuperMerchant)
					.getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return payId;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantAndSuperMerchantList() {

		List<Merchants> merchants = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery("Select payId, emailId, businessName from User U where U.userType = '" + UserType.MERCHANT
					+ "' and U.userStatus !='" + UserStatusType.PENDING + "' "
					+ "and ((U.isSuperMerchant = true) or (U.superMerchantId = null))").getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchants.add(merchant);
			}
			tx.commit();
			return merchants;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchants;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubMerchantListBySuperPayId(String superMerchantpayId) {

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session
					.createQuery("Select payId, emailId, businessName, virtualAccountNo from User U where U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
							+ UserStatusType.ACTIVE + "' "
							+ "and U.isSuperMerchant = false and U.superMerchantId = :superMerchantId")
					.setParameter("superMerchantId", superMerchantpayId).getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setVirtualAccountNo((String) objects[3]);
//				merchant.setRetailMerchantFlag(user.isRetailMerchantFlag());
//				merchant.setCustomerQrFlag(user.isCustomerQrFlag());
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	public List<User> getAllMerchantList() {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<User> users = null;

		try {
			Query<User> query = session.createQuery("from User U where U.userType = '" + UserType.MERCHANT
					+ "' or U.userType = '" + UserType.PARENTMERCHANT + "'", User.class);
			users = query.getResultList();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return users;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubMerchantListBySuperPayIdForKhadi(String superMerchantpayId) {

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<User> userList = new ArrayList<User>();
		try {
			userList = session
					.createQuery(" from User U where U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
							+ UserStatusType.ACTIVE + "' "
							+ "and U.isSuperMerchant = false and U.superMerchantId = :superMerchantId")
					.setParameter("superMerchantId", superMerchantpayId).getResultList();

			for (User user : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId(user.getPayId());
				merchant.setEmailId(user.getEmailId());
				merchant.setBusinessName(user.getBusinessName());
				if (user.getActivationDate() != null && StringUtils.isNotBlank(user.getActivationDate().toString())) {
					merchant.setRegistrationDate(user.getActivationDate().toString());
				}

				/*
				 * if(StringUtils.isNotBlank(user.getRegistrationDate().toString())) {
				 * merchant.setRegistrationDate(user.getRegistrationDate().toString()); }
				 */
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSuperMerchantList() {

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery("Select payId, emailId, businessName, superMerchantId from User U where U.userType = '" + UserType.MERCHANT
					+ "' and U.userStatus='" + UserStatusType.ACTIVE + "' " + "and U.isSuperMerchant = true")
					.getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubMerchantList() {

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery("Select payId, emailId, businessName, superMerchantId from User U where U.userType = '" + UserType.MERCHANT
					+ "and U.isSuperMerchant = false and U.superMerchantId is not null").getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getUserListbyUserType(String userType, String resellerUserId) {
		List<Merchants> userListByUserType = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		String query = "";

		if (userType.equalsIgnoreCase(UserType.MERCHANT.name())) {
			if (StringUtils.isNotBlank(resellerUserId)) {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate, modeType from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is null and resellerId = '"
						+ resellerUserId + "'";
			} else {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate, modeType from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is null";
			}
		} else if (userType.equalsIgnoreCase("SUB_MERCHANT")) {
			if (StringUtils.isNotBlank(resellerUserId)) {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is not null and resellerId = '"
						+ resellerUserId + "'";
			} else {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is not null";
			}
		} else if (userType.equalsIgnoreCase("SUPER_MERCHANT")) {
			if (StringUtils.isNotBlank(resellerUserId)) {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate, resellerId, modeType from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = true and U.superMerchantId is not null and resellerId = '"
						+ resellerUserId + "'";
			} else {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate, resellerId, modeType from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = true and U.superMerchantId is not null";
			}
		} else if (userType.equalsIgnoreCase(UserType.RESELLER.name())) {
			query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate, partnerFlag from User U where U.userType = '"
					+ userType + "'";
		} else if (userType.equalsIgnoreCase(UserType.SUBUSER.name())) {
			query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate, parentPayId from User U where U.userType = '"
					+ userType + "'";
		} else if (userType.equalsIgnoreCase(UserType.ACQUIRER.name())) {
			query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate from User U where U.userType = '"
					+ userType + "'";
		} else if (userType.equalsIgnoreCase(UserType.PARENTMERCHANT.name())) {
			query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate from User U where U.userType = '"
					+ userType + "'";
		}
		try {
			userList = session.createQuery(query).setCacheable(true).getResultList();
			for (Object[] objects : userList) {
				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				UserStatusType status = (UserStatusType) objects[4];
				merchant.setStatus(status.name());
				merchant.setMobile((String) objects[5]);
				if (StringUtils.isBlank((String) objects[6])) {
					merchant.setUpdatedBy("NA");
				} else {
					merchant.setUpdatedBy((String) objects[6]);
				}
				if (objects[7] == null) {
					merchant.setUpdationDate("NA");
				} else {
					Date date = (Date) objects[7];
					merchant.setUpdationDate(String.valueOf(date));
				}

				if (userType.equalsIgnoreCase(UserType.RESELLER.name())) {
					if ((boolean) objects[8] == false) {
						merchant.setUserTypeOrName("Normal Reseller");
					} else {
						merchant.setUserTypeOrName("Partner Reseller");
					}
				} else if (userType.equalsIgnoreCase(UserType.SUBUSER.name())) {

					String parentPayId = (String) objects[8];
					merchant.setUserTypeOrName(getBusinessNameByPayId(parentPayId));
				} else if (userType.equalsIgnoreCase("SUB_MERCHANT")) {

					merchant.setUserTypeOrName(getBusinessNameByPayId(merchant.getSuperMerchantId()));

				} else if (userType.equalsIgnoreCase("SUPER_MERCHANT")) {
					String resellerId = (String) objects[8];
					if (StringUtils.isNotBlank(resellerId)) {

						Merchants reseller = getResellerByResellerId(resellerId).get(0);
						if (reseller.isPartnerFlag()) {
							merchant.setUserTypeOrName("NA");
						} else {
							merchant.setUserTypeOrName(reseller.getBusinessName());
						}
					} else {
						merchant.setUserTypeOrName("NA");
					}
					ModeType mode = (ModeType) objects[9];
					if (mode != null)
						merchant.setModeType(mode.name());
					else
						merchant.setModeType(null);
				} else if (userType.equalsIgnoreCase(UserType.MERCHANT.name())) {
					ModeType mode = (ModeType) objects[8];
					if (mode != null)
						merchant.setModeType(mode.name());
					else
						merchant.setModeType(null);
				}
				userListByUserType.add(merchant);
			}
			return userListByUserType;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("Exception : " + ex);
		} finally {
			autoClose(session);
		}
		return userListByUserType;
	}

	@SuppressWarnings("unchecked")
	public List<UserTypeListObject> getUserListbyUserTypeForDownload(String userType, User sessionUser) {
		List<UserTypeListObject> userListByUserType = new ArrayList<UserTypeListObject>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		String query = "";

		if (userType.equalsIgnoreCase(UserType.MERCHANT.name())) {
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is null and resellerId = '" + sessionUser.getResellerId() + "'";
			} else {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is null";
			}
		} else if (userType.equalsIgnoreCase("SUB_MERCHANT")) {
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is not null and resellerId = '" + sessionUser.getResellerId() + "'";
			} else {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = false and U.superMerchantId is not null";
			}
		} else if (userType.equalsIgnoreCase("SUPER_MERCHANT")) {
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = true and U.superMerchantId is not null and resellerId = '" + sessionUser.getResellerId() + "'";
			} else {
				query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = 'MERCHANT' "
						+ "and U.isSuperMerchant = true and U.superMerchantId is not null";
			}
		} else if (userType.equalsIgnoreCase(UserType.RESELLER.name())) {
			query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate from User U where U.userType = '"
					+ userType + "'";
		} else if (userType.equalsIgnoreCase(UserType.SUBUSER.name())) {
			query = "select payId, emailId, businessName, superMerchantId, userStatus, mobile, updateDate, registrationDate, parentPayId, partnerFlag, makerName, makerStatus, makerStatusUpDate, checkerName, checkerStatus, checkerStatusUpDate, subUserType from User U where U.userType = '"
					+ userType + "'";
		}

		try {
			userList = session.createQuery(query).setCacheable(true).getResultList();
			for (Object[] objects : userList) {

				UserTypeListObject userObj = new UserTypeListObject();
				userObj.setPayId((String) objects[0]);
				userObj.setEmailId((String) objects[1]);
				userObj.setBusinessName((String) objects[2]);
				userObj.setSuperMerchantId((String) objects[3]);
				if (userType.equalsIgnoreCase("SUB_MERCHANT") && StringUtils.isNotBlank(userObj.getSuperMerchantId())) {
					userObj.setSuperMerchantName(getBusinessNameByPayId(userObj.getSuperMerchantId()));
				}

				UserStatusType status = (UserStatusType) objects[4];
				userObj.setStatus(status.name());
				userObj.setMobile((String) objects[5]);

				if (objects[6] == null) {
					userObj.setUpdatedDate("NA");
				} else {
					Date date = (Date) objects[6];
					userObj.setUpdatedDate(String.valueOf(date));
				}

				if (objects[7] == null) {
					userObj.setRegistrationDate("NA");
				} else {
					Date date = (Date) objects[7];
					userObj.setRegistrationDate(String.valueOf(date));
				}
				
				if (userType.equals("SUB_MERCHANT") || userType.equals("SUPER_MERCHANT")) {
					userObj.setUserType(UserType.MERCHANT.name());
				} else {
					userObj.setUserType(userType);
				}
				if (objects[8] != null && StringUtils.isNotBlank((String) objects[8])) {
					String parentPayId = (String) objects[8];
					userObj.setParentName(getBusinessNameByPayId(parentPayId));
				}
				if ((boolean) objects[9] == false) {
					userObj.setResellerType("Normal Reseller");
				} else {
					userObj.setResellerType("Partner Reseller");
				}
				userObj.setMakerName((String) objects[10]);
				userObj.setMakerStatus((String) objects[11]);
				userObj.setMakerStatusUpDate((String) objects[12]);
				userObj.setCheckerName((String) objects[13]);
				userObj.setCheckerStatus((String) objects[14]);
				userObj.setCheckerStatusUpDate((String) objects[15]);

				if (userType.equalsIgnoreCase(UserType.SUBUSER.name())) {
					userObj.setSubUserType((String) objects[16]);
				}
				userListByUserType.add(userObj);

			}

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("Exception : " + ex);
		} finally {
			autoClose(session);
		}
		return userListByUserType;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubMerchantList(String superMerchantId, String email, String mobile,
			UserStatusType status, int length, int start, String resellerId) {

		StringBuilder queryMaker = new StringBuilder();

		queryMaker.append("Select payId, emailId, businessName, superMerchantId, userStatus, mobile, updatedBy, updateDate from User U where U.userType = '" + UserType.MERCHANT
				+ "' and U.isSuperMerchant = false and U.superMerchantId is not null ");
		if (StringUtils.isNotBlank(superMerchantId) && !superMerchantId.equalsIgnoreCase("All")) {
			queryMaker.append(" and superMerchantId = '" + superMerchantId + "'");
		}
		if (StringUtils.isNotBlank(email) && !email.equalsIgnoreCase("All")) {
			queryMaker.append(" and emailId = '" + email + "'");
		}
		if (StringUtils.isNotBlank(mobile) && !mobile.equalsIgnoreCase("All")) {
			queryMaker.append(" and mobile = '" + mobile + "'");
		}
		if (status != null) {
			queryMaker.append(" and userStatus = '" + status + "'");
		}
		if (StringUtils.isNotBlank(resellerId)) {
			queryMaker.append(" and resellerId = '" + resellerId + "'");
		}

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery(queryMaker.toString()).setFirstResult(start).setMaxResults(length)
					.getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				UserStatusType userStatus = (UserStatusType) objects[4];					
				merchant.setStatus(userStatus.getStatus());
				merchant.setMobile((String) objects[5]);

				if (StringUtils.isBlank((String) objects[6])) {
					merchant.setUpdatedBy("NA");
				} else {
					merchant.setUpdatedBy((String) objects[6]);
				}
				if (objects[7] == null) {
					merchant.setUpdationDate("NA");
				} else {
					Date date = (Date) objects[7];
					merchant.setUpdationDate(String.valueOf(date));
				}
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	public int getSubMerchantCount(String superMerchantId, String email, String mobile, UserStatusType status,
			String resellerId) {

		int count = 0;
		StringBuilder queryMaker = new StringBuilder();

		queryMaker.append("select count(*) from User U where U.userType = '" + UserType.MERCHANT
				+ "' and U.isSuperMerchant = false and U.superMerchantId is not null ");
		if (StringUtils.isNotBlank(superMerchantId) && !superMerchantId.equalsIgnoreCase("All")) {
			queryMaker.append(" and superMerchantId = '" + superMerchantId + "'");
		}
		if (StringUtils.isNotBlank(email) && !email.equalsIgnoreCase("All")) {
			queryMaker.append(" and emailId = '" + email + "'");
		}
		if (StringUtils.isNotBlank(mobile) && !mobile.equalsIgnoreCase("All")) {
			queryMaker.append(" and mobile = '" + mobile + "'");
		}
		if (status != null) {
			queryMaker.append(" and userStatus = '" + status + "'");
		}
		if (StringUtils.isNotBlank(resellerId)) {
			queryMaker.append(" and resellerId = '" + resellerId + "'");
		}

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
            try {
                count = (int) (long) session.createQuery(queryMaker.toString()).getSingleResult();
            } catch (NonUniqueResultException e) {
                logger.error("Exception: " + e);
            }
            tx.commit();
			return count;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return count;

	}

	public User findByEmailId(String emailId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		User responseUser = null;
		try {
			responseUser = (User) session.createQuery(getCompleteUserWithEmailIdQuery).setParameter("emailId", emailId)
					.setCacheable(true).getSingleResult();
			tx.commit();
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error" + hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error " + e);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllActiveReseller() {
		List<Merchants> resellerList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select emailId, payId, resellerId, businessName from User U where U.userType = '"
							+ UserType.RESELLER + "' and userStatus = '" + UserStatusType.ACTIVE
							+ "' order by businessName")
					.getResultList();
			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setResellerId((String) objects[2]);
				merchant.setBusinessName((String) objects[3]);
				resellerList.add(merchant);
			}
			tx.commit();
			return resellerList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return resellerList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSubUserByParentPayId(String parentPayId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			List<Object[]> merchantListRaw = session
					.createQuery(
							"Select emailId, payId, businessName  from User U where U.payId = :payId and U.userType = '"
									+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE + "'")
					.setParameter("payId", parentPayId).setCacheable(true).getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getKhadiSubUserByParentPayId(String parentPayId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName  from User U where U.parentPayId = :parentPayId and U.userType = '"
							+ UserType.SUBUSER + "' and U.userStatus='" + UserStatusType.ACTIVE + "'")
					.setParameter("parentPayId", parentPayId).setCacheable(true).getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantsList.add(merchant);
			}

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	public User findByVirtualAcc(String virtualAccountNo) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseUser = null;
		try {
			responseUser = (User) session.createQuery("from User U where U.virtualAccountNo = :virtualAccountNo")
					.setParameter("virtualAccountNo", virtualAccountNo).setCacheable(true).getSingleResult();

			tx.commit();

			// userMap.put(payId1, responseUser);
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error" + hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error " + e);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getResellerByResellerId(String resellerId) {
		List<Merchants> resellerList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> resellerListRaw = session.createQuery(
					"Select emailId, payId, businessName, resellerId, partnerFlag from User U where U.resellerId = :resellerId and U.userType = '"
							+ UserType.RESELLER + "' and U.userStatus='" + UserStatusType.ACTIVE + "'")
					.setParameter("resellerId", resellerId).setCacheable(true).getResultList();

			for (Object[] objects : resellerListRaw) {
				Merchants reseller = new Merchants();
				reseller.setEmailId((String) objects[0]);
				reseller.setPayId((String) objects[1]);
				reseller.setBusinessName((String) objects[2]);
				reseller.setResellerId((String) objects[3]);
				reseller.setPartnerFlag((boolean) objects[4]);
				resellerList.add(reseller);
			}

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return resellerList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantListByResellerId(String resellerId) {

		List<Merchants> merchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session
					.createQuery("Select payId, emailId, businessName, mobile, registrationDate, userStatus from User U where U.resellerId = :resellerId and U.userType = '" + UserType.MERCHANT
							+ "' and U.userStatus='" + UserStatusType.ACTIVE + "'")
					.setParameter("resellerId", resellerId).getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setMobile((String) objects[3]);
				if (objects[4] == null) {
					merchant.setRegistrationDate("NA");
				} else {
					Format formatter = new SimpleDateFormat("yyyy-MM-dd");
					String s = formatter.format(objects[4]);
					merchant.setRegistrationDate(s);
				}
				UserStatusType userStatus = (UserStatusType) objects[5];					
				merchant.setStatus(userStatus.getStatus());
				
//				merchant.setCustomerQrFlag(user.isCustomerQrFlag());
				merchantList.add(merchant);
			}
			tx.commit();
			return merchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantList;
	}

	public int getMerchantCountForReseller(String resellerId, String merchantEmail, String mobile,
			UserStatusType status) {

		int count = 0;
		StringBuilder queryMaker = new StringBuilder();

		queryMaker.append("select count(*) from User U where U.userType = '" + UserType.MERCHANT + "' ");
		if (StringUtils.isNotBlank(merchantEmail) && !merchantEmail.equalsIgnoreCase("All")) {
			queryMaker.append(" and emailId = '" + merchantEmail + "'");
		}
		if (StringUtils.isNotBlank(mobile) && !mobile.equalsIgnoreCase("All")) {
			queryMaker.append(" and mobile = '" + mobile + "'");
		}
		if (status != null) {
			queryMaker.append(" and userStatus = '" + status + "'");
		}
		queryMaker.append("and resellerId = '" + resellerId + "'");

		queryMaker.append(
				"and (isSuperMerchant = true and superMerchantId IS NOT NULL or isSuperMerchant = false and superMerchantId IS NULL) order by businessName");

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			try {
				count = (int) (long) session.createQuery(queryMaker.toString()).getSingleResult();
			} catch (NonUniqueResultException e) {
				logger.error("Exception: " + e);
			}
			tx.commit();
			return count;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantListForReseller(String resellerId, String merchantEmail, String mobile,
			UserStatusType status, int length, int start) {

		StringBuilder queryMaker = new StringBuilder();

		queryMaker.append("Select payId, emailId, businessName, mobile, registrationDate, userStatus from User U where U.userType = '" + UserType.MERCHANT + "'");
		if (StringUtils.isNotBlank(merchantEmail) && !merchantEmail.equalsIgnoreCase("All")) {
			queryMaker.append(" and emailId = '" + merchantEmail + "'");
		}
		if (StringUtils.isNotBlank(mobile) && !mobile.equalsIgnoreCase("All")) {
			queryMaker.append(" and mobile = '" + mobile + "'");
		}
		if (status != null) {
			queryMaker.append(" and userStatus = '" + status + "'");
		}

		queryMaker.append("and resellerId = '" + resellerId + "'");
		queryMaker.append(
				"and (isSuperMerchant = true and superMerchantId IS NOT NULL or isSuperMerchant = false and superMerchantId IS NULL) order by businessName");

		List<Merchants> merchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery(queryMaker.toString()).setFirstResult(start).setMaxResults(length)
					.getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setMobile((String) objects[3]);
				if (objects[4] == null) {
					merchant.setRegistrationDate("NA");
				} else {
					Format formatter = new SimpleDateFormat("yyyy-MM-dd");
					String s = formatter.format(objects[4]);
					merchant.setRegistrationDate(s);
				}
				UserStatusType userStatus = (UserStatusType) objects[5];					
				merchant.setStatus(userStatus.getStatus());
				
				merchantList.add(merchant);
			}
			tx.commit();
			return merchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantList;
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllMobile() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<String> mobileList = null;
		try {
			mobileList = session.createQuery("SELECT mobile from User").getResultList();
			tx.commit();
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return mobileList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSuperOrSubMerchantByPayId(String payId) {

		List<Merchants> superMerchant = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session
					.createQuery("Select payId, emailId, businessName, superMerchantId from User U where U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
							+ UserStatusType.ACTIVE + "' " + "and U.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				superMerchant.add(merchant);
			}
			tx.commit();
			return superMerchant;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return superMerchant;
	}

	@SuppressWarnings("unchecked")
	public int getKhadiSubUserCount(String email, String mobile, UserStatusType status) {

		int count = 0;
		StringBuilder queryMaker = new StringBuilder();

		queryMaker.append("select count(*) from User U where U.userType = '" + UserType.SUBUSER
				+ "' and U.khadiMerchant = true and U.parentPayId is not null ");

		if (StringUtils.isNotBlank(email) && !email.equalsIgnoreCase("All")) {
			queryMaker.append(" and emailId = '" + email + "'");
		}
		if (StringUtils.isNotBlank(mobile) && !mobile.equalsIgnoreCase("All")) {
			queryMaker.append(" and mobile = '" + mobile + "'");
		}
		if (status != null) {
			queryMaker.append(" and userStatus = '" + status + "'");
		}

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		try {
			try {
				count = (int) (long) session.createQuery(queryMaker.toString()).getSingleResult();
			} catch (NonUniqueResultException e) {
				logger.error("Exception: " + e);
			}
			tx.commit();
			return count;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return count;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getKhadiSubUserList(String email, String mobile, UserStatusType status, int length,
			int start) {

		StringBuilder queryMaker = new StringBuilder();

		queryMaker.append("Select payId, emailId, businessName, userStatus, subUserType, mobile, updatedBy, updateDate from User U where U.userType = '" + UserType.SUBUSER
				+ "' and U.khadiMerchant = true and U.parentPayId is not null ");

		if (StringUtils.isNotBlank(email) && !email.equalsIgnoreCase("All")) {
			queryMaker.append(" and emailId = '" + email + "'");
		}
		if (StringUtils.isNotBlank(mobile) && !mobile.equalsIgnoreCase("All")) {
			queryMaker.append(" and mobile = '" + mobile + "'");
		}
		if (status != null) {
			queryMaker.append(" and userStatus = '" + status + "'");
		}

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery(queryMaker.toString()).setFirstResult(start).setMaxResults(length)
					.getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				UserStatusType userStatus = (UserStatusType) objects[3];					
				merchant.setStatus(userStatus.getStatus());
				merchant.setSubUserType((String) objects[4]);
				merchant.setMobile((String) objects[5]);

				if (StringUtils.isBlank((String) objects[6])) {
					merchant.setUpdatedBy("NA");
				} else {
					merchant.setUpdatedBy((String) objects[6]);
				}
				if (objects[7] == null) {
					merchant.setUpdationDate("NA");
				} else {
					Date date = (Date) objects[7];
					merchant.setUpdationDate(String.valueOf(date));
				}
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getActiveKhadiSubUsers(String khadiMerchantPayId) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			List<Object[]> merchantListRaw = session
					.createQuery("SELECT emailId, payId, businessName, parentPayId FROM User U "
							+ "WHERE U.userType = 'SUBUSER' AND U.khadiMerchant = TRUE "
							+ "AND U.userStatus = 'ACTIVE' AND parentPayId = '" + khadiMerchantPayId + "' "
							+ "ORDER BY businessName")
					.getResultList();
			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setParentPayId((String) objects[3]);
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	public User khadiMerchant() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		User responseUser = null;
		try {
			responseUser = (User) session.createQuery(
					"from User U where U.khadiMerchant = true AND U.userType='MERCHANT' AND U.userStatus='ACTIVE'")
					.setCacheable(true).getSingleResult();
			tx.commit();
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error" + hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error " + e);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	public boolean isSubUserPrevilageTypeAll(User sessionUser) {
		Set<Roles> roles = sessionUser.getRoles();

		for (Roles role : roles) {
			Set<Permissions> permissionSet = role.getPermissions();
			for (Permissions permission : permissionSet) {
				if (permission.getPermissionType().equals(PermissionType.SUB_USER_ALL)) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllSubUserListByAdminUsers(UserStatusType status) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			String hql = "Select payId, emailId, firstName, lastName, mobile, registrationDate, userStatus, subUserType, parentPayId from User U where U.userType = '"
					+ UserType.SUBUSER + "'";
			if (status != null) {
				hql = "Select payId, emailId, firstName, lastName, mobile, registrationDate, userStatus, subUserType, parentPayId from User U where U.userType = '"
						+ UserType.SUBUSER + "' and U.userStatus='" + status + "'";
			}
			List<Object[]> merchantListRaw = session.createQuery(hql).getResultList();

			for (Object[] objects : merchantListRaw) {
				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setFirstName((String) objects[2]);
				merchant.setLastName((String) objects[3]);
				merchant.setMobile((String) objects[4]);
				if (objects[5] == null) {
					merchant.setRegistrationDate("NA");
				} else {
					Format formatter = new SimpleDateFormat("yyyy-MM-dd");
					String s = formatter.format(objects[5]);
					merchant.setRegistrationDate(s);
				}
				if (((UserStatusType) objects[6]).equals(UserStatusType.ACTIVE)) {
					merchant.setIsActive(true);
				} else if (((UserStatusType) objects[6]).equals(UserStatusType.PENDING)) {
					merchant.setIsActive(false);
				}
				if (StringUtils.isNotBlank(((String) objects[7]))) {
					merchant.setSubUserType((String) objects[7]);
				} else {
					merchant.setSubUserType("NA");
				}
				if (StringUtils.isNotBlank(((String) objects[8]))) {
					merchant.setParentPayId((String) objects[8]);
				} else {
					merchant.setParentPayId("");
				}
				merchantsList.add(merchant);
			}

			tx.commit();
			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			logger.error("error" + objectNotFound);
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error" + hibernateException);
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	public void updateUserStatus(String payId, String userStatus, Date updateDate, String updatedBy) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery(
					"update User U set U.userStatus = :userStatus, U.updateDate = :updateDate, U.updatedBy = :updatedBy"
							+ "where U.payId = :payId")
					.setParameter("userStatus", userStatus).setParameter("payId", payId).executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getActiveMerchant() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName, superMerchantId, isSuperMerchant from User U where U.userType = '"
							+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE
							+ "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {

				// Don't include sub-merchants in mapping list
				/*
				 * if (objects[3] != null && objects[4] != null) { if
				 * (StringUtils.isNotBlank((String) objects[3]) && !(boolean) objects[4]) {
				 * continue; } }
				 */

				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				merchant.setIsSuperMerchant((boolean) objects[4]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getActiveMerchantByPaymentAdvice() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName, superMerchantId, isSuperMerchant, paymentAdviceFlag from User U where U.userType = '"
							+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE
							+ "' order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				if ((!((boolean) objects[4] == false && StringUtils.isNotBlank((String) objects[3]))
						|| (boolean) objects[4] == true) && (boolean) objects[5] == true) {

					Merchants merchant = new Merchants();
					merchant.setEmailId((String) objects[0]);
					merchant.setPayId((String) objects[1]);
					merchant.setBusinessName((String) objects[2]);
					merchant.setSuperMerchantId((String) objects[3]);
					merchant.setIsSuperMerchant((boolean) objects[4]);
					merchant.setPaymentAdviceFlag((boolean) objects[5]);
					merchantsList.add(merchant);
				}
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Merchants> getActiveMerchantByPaymentAdviceFromMerchantList(Map<String,Merchants> merchantMap) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		String payIds = null;
		
		try {
			//get payId from map
			for(String payId: merchantMap.keySet()){
				
				if(StringUtils.isBlank(payIds)){
					payIds = "'"+payId+"'";
				}else{
					payIds=payIds+",'"+payId+"'";
				}
			}
			
			
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName, superMerchantId, isSuperMerchant from User U where U.userType = '"
							+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE+ "' and U.payId IN ("+payIds
							+ ") order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				if ((!((boolean) objects[4] == false && StringUtils.isNotBlank((String) objects[3]))
						|| (boolean) objects[4] == true)) {
					if(merchantMap.get((String) objects[1])!=null){
						Merchants merchant = merchantMap.get((String) objects[1]);
						merchant.setEmailId((String) objects[0]);
						merchant.setBusinessName((String) objects[2]);
						merchant.setSuperMerchantId((String) objects[3]);
						merchant.setIsSuperMerchant((boolean) objects[4]);
						merchantsList.add(merchant);
					}
					
				}
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getSuperMerchantAndSubMerchantListBySuperPayId(String superMerchantpayId) {

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session
					.createQuery("Select payId, emailId, businessName, isSuperMerchant, superMerchantId from User U where U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
							+ UserStatusType.ACTIVE + "' "
							+ "and isSuperMerchant = false and U.superMerchantId = :superMerchantId")
					.setParameter("superMerchantId", superMerchantpayId).getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setIsSuperMerchant((boolean) objects[3]);
				merchant.setSuperMerchantId((String) objects[4]);
//				merchant.setRetailMerchantFlag(user.isRetailMerchantFlag());
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantsByPayId(String payId) {

		List<Merchants> superMerchant = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session
					.createQuery(
							"Select payId, emailId, businessName, superMerchantId from User U where U.userType = '" + UserType.MERCHANT + "' " + "and U.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				superMerchant.add(merchant);
			}
			tx.commit();
			return superMerchant;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return superMerchant;
	}

	@SuppressWarnings("unchecked")
	public List<User> getSubMerchantsBySuperPayId(String superMerchantpayId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<User> userList = new ArrayList<User>();
		try {
			userList = session
					.createQuery(" from User U where U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
							+ UserStatusType.ACTIVE + "' "
							+ "and U.isSuperMerchant = false and U.superMerchantId = :superMerchantId")
					.setParameter("superMerchantId", superMerchantpayId).getResultList();

			tx.commit();
			return userList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userList;

	}

	@SuppressWarnings("unchecked")
	public boolean checkDuplicateVirtualAccountNo(String virtualAccountNo) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<User> userList = new ArrayList<User>();
		try {
			userList = session.createQuery("from User U where U.virtualAccountNo = :virtualAccountNo")
					.setParameter("virtualAccountNo", virtualAccountNo).getResultList();

			tx.commit();

			if (!userList.isEmpty()) {
				return true;
			}

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getAllMerchantwithVirtualAccountNo() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			List<Object[]> merchantListRaw = session.createQuery(
					"Select emailId, payId, businessName, superMerchantId, isSuperMerchant,virtualAccountNo, merchantVPA from User U where U.virtualAccountNo IS NOT NULL order by businessName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {

				Merchants merchant = new Merchants();
				merchant.setEmailId((String) objects[0]);
				merchant.setPayId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				merchant.setIsSuperMerchant((boolean) objects[4]);
				merchant.setVirtualAccountNo((String) objects[5]);
				merchant.setMerchantVPA((String) objects[6]);
				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getMerchantAndSubMerchantList() {

		List<Merchants> merchantAndSubMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session
					.createQuery("Select payId, emailId, businessName from User U where U.userType = '" + UserType.MERCHANT
							+ "and U.isSuperMerchant = false and U.superMerchantId is not null " + "or U.userType = '"
							+ UserType.MERCHANT + "and U.isSuperMerchant = false and U.superMerchantId is null")
					.getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchantAndSubMerchantList.add(merchant);
			}
			tx.commit();
			return merchantAndSubMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantAndSubMerchantList;
	}

	public void updateDebitDuration(String debitDuration, String payId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery(
					"UPDATE User U set U.debitDuration = '" + debitDuration + "' where U.payId = '" + payId + "' ")
					.executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("exception caught while insert debit duration " + ex);
		} finally {
			autoClose(session);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, String>> fetchDebitDurationDetails(String merchantPayId, String subMerchantPayId,
			String debitDuration) {

		String query = "";
		if (merchantPayId.equalsIgnoreCase("ALL") && debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId is not null or U.superMerchantId is null and U.debitDuration is not null";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& subMerchantPayId.equalsIgnoreCase("ALL") && debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId is not null and U.superMerchantId = '"
					+ merchantPayId + "' and U.debitDuration is not null";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& !subMerchantPayId.equalsIgnoreCase("ALL") && debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId = '" + merchantPayId + "' and U.payId = '"
					+ subMerchantPayId + "' and U.debitDuration is not null";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& subMerchantPayId.equalsIgnoreCase("ALL") && !debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId = '" + merchantPayId
					+ "' and U.debitDuration = '" + debitDuration + "' ";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& !subMerchantPayId.equalsIgnoreCase("ALL") && !debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId = '" + merchantPayId
					+ "' and U.debitDuration = '" + debitDuration + "' " + "and U.payId = '" + subMerchantPayId + "' ";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantPayId)
				&& !debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and payId = '" + merchantPayId + "' and U.debitDuration = '"
					+ debitDuration + "' ";
		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantPayId)
				&& debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, debitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and payId = '" + merchantPayId
					+ "' and U.debitDuration is not null";
		}

		List<Map<String, String>> debitDurationList = new ArrayList<Map<String, String>>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery(query).getResultList();

			for (Object[] objects : userList) {

				Map<String, String> debitDurationMap = new HashMap<String, String>();
				if ((boolean) objects[2] == false && StringUtils.isNotBlank((String) objects[3])) {
					debitDurationMap.put(FieldType.PAY_ID.getName(), (String) objects[3]);
					debitDurationMap.put(FieldType.MERCHANT_NAME.getName(),
							getBusinessNameByPayId((String) objects[3]));
					debitDurationMap.put(FieldType.SUB_MERCHANT_ID.getName(), (String) objects[0]);
					debitDurationMap.put("SUB_MERCHANT_NAME", (String) objects[1]);

				} else {
					debitDurationMap.put(FieldType.PAY_ID.getName(), (String) objects[0]);
					debitDurationMap.put(FieldType.MERCHANT_NAME.getName(), (String) objects[1]);
					debitDurationMap.put(FieldType.SUB_MERCHANT_ID.getName(), Constants.NA.getValue());
					debitDurationMap.put("SUB_MERCHANT_NAME", Constants.NA.getValue());
				}

				if (StringUtils.isNotBlank((String) objects[4])) {
					debitDurationMap.put(FieldType.DEBIT_DAY.getName(), (String) objects[4]);
					debitDurationList.add(debitDurationMap);
				} else {
					debitDurationMap.put(FieldType.DEBIT_DAY.getName(), Constants.NA.getValue());
				}

			}
			tx.commit();
			return debitDurationList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return debitDurationList;
	}

	public void updateSubMerchanteNachFlag(String payId, boolean eNachReportFlag) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery("UPDATE User U set U.eNachReportFlag = :eNachReportFlag where U.payId = :payId ")
					.setParameter("eNachReportFlag", eNachReportFlag).setParameter("payId", payId).executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("exception caught while update eNach flag ", ex);
		} finally {
			autoClose(session);
		}
	}
	
	public void updateSubMerchantUpiAutoPayFlag(String payId, boolean upiAutoPayReportFlag) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery("UPDATE User U set U.upiAutoPayReportFlag = :upiAutoPayReportFlag where U.payId = :payId ")
					.setParameter("upiAutoPayReportFlag", upiAutoPayReportFlag).setParameter("payId", payId).executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("exception caught while update upiAutoPay flag ", ex);
		} finally {
			autoClose(session);
		}
	}
	
	

	public String getDebitDurationByPayId(String payId) {

		String debitDuration = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			debitDuration = (String) session.createQuery("Select debitDuration from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return debitDuration;
	}
	
	public String getUpiAutoPayDebitDurationByPayId(String payId) {

		String debitDuration = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			debitDuration = (String) session.createQuery("Select upiAutoPayDebitDuration from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return debitDuration;

	}

	@SuppressWarnings("unchecked")
	public List<Merchants> getResellerSuperMerchantList(String resellerId) {

		List<Merchants> subMerchantList = new ArrayList<Merchants>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery("Select payId, emailId, businessName, superMerchantId from User U where U.userType = '" + UserType.MERCHANT
					+ "' and U.userStatus='" + UserStatusType.ACTIVE + "' "
					+ "and U.isSuperMerchant = true and U.resellerId = '" + resellerId + "'").getResultList();

			for (Object[] objects : userList) {

				Merchants merchant = new Merchants();
				merchant.setPayId((String) objects[0]);
				merchant.setEmailId((String) objects[1]);
				merchant.setBusinessName((String) objects[2]);
				merchant.setSuperMerchantId((String) objects[3]);
				subMerchantList.add(merchant);
			}
			tx.commit();
			return subMerchantList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return subMerchantList;
	}

	public List<MerchantDetails> fetchMerchantsList() {

		logger.info("Inside fetchMerchantsList");
		List<MerchantDetails> merchantList = new ArrayList<MerchantDetails>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try (Connection connection = getConnection()) {
			logger.info("got the connection");
			String query = "Select payId, businessName, emailId, userStatus,registrationDate,userType,merchantCreatorName,mpaStage,checkerName,"
					+ "makerName,makerStatus,checkerStatus,makerStatusUpDate,checkerStatusUpDate,superMerchantId,isSuperMerchant,updateDate,industryCategory,mobile from User where (userType='MERCHANT') order by payId ";

			logger.info("Query : " + query);
			try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
				try (ResultSet rs = prepStmt.executeQuery()) {
					logger.info("Fetch data for merchant list");
					while (rs.next()) {
						String registrationDate = null;

						MerchantDetails merchant = new MerchantDetails();

						merchant.setPayId(rs.getString("payId"));
						merchant.setBusinessName(rs.getString("businessName"));
						if(rs.getString("userStatus") != null) {
							String status1 = rs.getString("userStatus");
							UserStatusType userStatus = UserStatusType.valueOf(status1);
							merchant.setStatus(userStatus);
						}
						if (rs.getString("merchantCreatorName") != null) {
							merchant.setCreatedBy(rs.getString("merchantCreatorName"));
						}
						if (StringUtils.isNotBlank(rs.getString("mpaStage"))) {
							merchant.setMpaStage(rs.getString("mpaStage"));
						} else {
							merchant.setMpaStage("PENDING");
						}
						if (null != rs.getTimestamp("registrationDate")) {
							registrationDate = formatter.format(rs.getTimestamp("registrationDate"));
						}
						
						if (rs.getString("checkerName") != null) {
							merchant.setCheckerName(rs.getString("checkerName"));
						}
						
						if (rs.getString("makerName") != null) {
							merchant.setMakerName(rs.getString("makerName"));
						}
						
						if (rs.getString("makerStatus") != null) {
							merchant.setMakerStatus(rs.getString("makerStatus"));
						}
						
						if (rs.getString("checkerStatus") != null) {
							merchant.setCheckerStatus(rs.getString("checkerStatus"));
						}
						
						if (rs.getString("makerStatusUpDate") != null) {
							merchant.setMakerStatusUpDate(rs.getString("makerStatusUpDate"));
						}
						
						if (rs.getString("checkerStatusUpDate") != null) {
							merchant.setCheckerStatusUpDate(rs.getString("checkerStatusUpDate"));
						}
						
						if (rs.getString("superMerchantId") != null) {
							merchant.setSuperMerchantId(rs.getString("superMerchantId"));
						}
						
						if (rs.getBoolean("isSuperMerchant")) {
							merchant.setSuperMerchant(rs.getBoolean("isSuperMerchant"));
						}
						
						if (rs.getString("industryCategory") != null) {
							merchant.setIndustryCategory(rs.getString("industryCategory"));
						}
						
						if (rs.getString("userType") != null) {
							merchant.setUserType(rs.getString("userType"));
						}
						
						if (rs.getTimestamp("updateDate") != null) {
							merchant.setUpdatedDate(formatter.format(rs.getTimestamp("updateDate")));
						}
						
						if (rs.getString("mobile") != null) {
							merchant.setMobile(rs.getString("mobile"));
						}
						merchant.setRegistrationDate(registrationDate);
						merchantList.add(merchant);
					}

				}
			}
			
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			logger.error("error",objectNotFound);
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error",hibernateException);
			handleException(hibernateException, tx);
		} catch (SQLException e) {
			logger.error("exception caught SQLException", e);
		} catch (Exception e) {
			logger.error("exception caught Exception", e);
		} finally {
			autoClose(session);
		}
		return merchantList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> fetchUpiAutoPayDebitDurationDetails(String merchantPayId, String subMerchantPayId,
			String debitDuration) {

		String query = "";
		if (merchantPayId.equalsIgnoreCase("ALL") && debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId is not null or U.superMerchantId is null and U.upiAutoPayDebitDuration is not null";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& subMerchantPayId.equalsIgnoreCase("ALL") && debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId is not null and U.superMerchantId = '"
					+ merchantPayId + "' and U.upiAutoPayDebitDuration is not null";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& !subMerchantPayId.equalsIgnoreCase("ALL") && debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId = '" + merchantPayId + "' and U.payId = '"
					+ subMerchantPayId + "' and U.upiAutoPayDebitDuration is not null";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& subMerchantPayId.equalsIgnoreCase("ALL") && !debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId = '" + merchantPayId
					+ "' and U.upiAutoPayDebitDuration = '" + debitDuration + "' ";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
				&& !subMerchantPayId.equalsIgnoreCase("ALL") && !debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and U.superMerchantId = '" + merchantPayId
					+ "' and U.upiAutoPayDebitDuration = '" + debitDuration + "' " + "and U.payId = '" + subMerchantPayId + "' ";

		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantPayId)
				&& !debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and payId = '" + merchantPayId + "' and U.upiAutoPayDebitDuration = '"
					+ debitDuration + "' ";
		} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantPayId)
				&& debitDuration.equalsIgnoreCase("ALL")) {

			query = "Select payId, businessName, isSuperMerchant, superMerchantId, upiAutoPayDebitDuration from User U where U.userType = '" + UserType.MERCHANT + "' "
					+ "and U.isSuperMerchant = false and payId = '" + merchantPayId
					+ "' and U.upiAutoPayDebitDuration is not null";
		}

		List<Map<String, String>> upiAutoPayDebitDurationList = new ArrayList<Map<String, String>>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> userList = new ArrayList<Object[]>();
		try {
			userList = session.createQuery(query).getResultList();

			for (Object[] objects : userList) {

				Map<String, String> upiAutoPayDebitDurationMap = new HashMap<String, String>();
				if ((boolean) objects[2] == false && StringUtils.isNotBlank((String) objects[3])) {
					upiAutoPayDebitDurationMap.put(FieldType.PAY_ID.getName(), (String) objects[3]);
					upiAutoPayDebitDurationMap.put(FieldType.MERCHANT_NAME.getName(),
							getBusinessNameByPayId((String) objects[3]));
					upiAutoPayDebitDurationMap.put(FieldType.SUB_MERCHANT_ID.getName(), (String) objects[0]);
					upiAutoPayDebitDurationMap.put("SUB_MERCHANT_NAME", (String) objects[1]);

				} else {
					upiAutoPayDebitDurationMap.put(FieldType.PAY_ID.getName(), (String) objects[0]);
					upiAutoPayDebitDurationMap.put(FieldType.MERCHANT_NAME.getName(), (String) objects[1]);
					upiAutoPayDebitDurationMap.put(FieldType.SUB_MERCHANT_ID.getName(), Constants.NA.getValue());
					upiAutoPayDebitDurationMap.put("SUB_MERCHANT_NAME", Constants.NA.getValue());
				}

				if (StringUtils.isNotBlank((String) objects[4])) {
					upiAutoPayDebitDurationMap.put(FieldType.DEBIT_DAY.getName(), (String) objects[4]);
					upiAutoPayDebitDurationList.add(upiAutoPayDebitDurationMap);
				} else {
					upiAutoPayDebitDurationMap.put(FieldType.DEBIT_DAY.getName(), Constants.NA.getValue());
				}

			}
			tx.commit();
			return upiAutoPayDebitDurationList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return upiAutoPayDebitDurationList;
	}
	
	public void updateUpiAutoPayDebitDuration(String debitDuration, String payId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery(
					"UPDATE User U set U.upiAutoPayDebitDuration = '" + debitDuration + "' where U.payId = '" + payId + "' ")
					.executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("exception caught while insert upi autoPay debit duration " ,ex);
		} finally {
			autoClose(session);
		}
	}
	
	public void updateSubMerchantAcceptPostSettledInEnquiryFlag(String payId, boolean acceptPostSettledInEnquiry) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery("UPDATE User U set U.acceptPostSettledInEnquiry = :acceptPostSettledInEnquiry where U.payId = :payId ")
					.setParameter("acceptPostSettledInEnquiry", acceptPostSettledInEnquiry).setParameter("payId", payId).executeUpdate();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception ex) {
			logger.info("exception caught while update acceptPostSettledInEnquiry flag ", ex);
		} finally {
			autoClose(session);
		}
	}
	
	public boolean getAcceptPostCaptureInStatus(String payId) {
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		boolean acceptPostCapturedFlag = false;
		try {
			acceptPostCapturedFlag = (boolean) session.createQuery("select acceptPostSettledInEnquiry from User U where U.payId = :payId")
					.setParameter("payId", payId).getSingleResult();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}

		return acceptPostCapturedFlag;
		}

	@SuppressWarnings("unchecked")
	public List<ECollectionObject> getVaDataList(String merchantPayId, String subMerchantId, User sessionUser,
			String virtualAccountNo) {
		List<ECollectionObject> VADataList = new ArrayList<ECollectionObject>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		String query = "";
		String merchantName = "";
		String resellerName = "";
		try {

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				if (StringUtils.isNotBlank(virtualAccountNo)) {
					query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.virtualAccountNo = '" + virtualAccountNo + "'";
				} else {

					if (merchantPayId.equalsIgnoreCase("ALL")) {
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.userType = '"
								+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE
								+ "' and U.superMerchantId IS NULL";
					} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantId)) {
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.payId = '"
								+ merchantPayId + "' and U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
								+ UserStatusType.ACTIVE + "'";
					} else if (!merchantPayId.equalsIgnoreCase("ALL") && subMerchantId.equalsIgnoreCase("ALL")) {
						User superMerchant = findPayId(merchantPayId);
						merchantName = superMerchant.getBusinessName();
						if (StringUtils.isNotBlank(superMerchant.getResellerId())) {
							resellerName = getBusinessNameByResellerId(superMerchant.getResellerId());
						}
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.superMerchantId = '"
								+ merchantPayId + "' and U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
								+ UserStatusType.ACTIVE
								+ "' and U.isSuperMerchant = false";
					} else if (!merchantPayId.equalsIgnoreCase("ALL") && !subMerchantId.equalsIgnoreCase("ALL")) {
						User superMerchant = findPayId(merchantPayId);
						merchantName = superMerchant.getBusinessName();
						if (StringUtils.isNotBlank(superMerchant.getResellerId())) {
							resellerName = getBusinessNameByResellerId(superMerchant.getResellerId());
						}
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.emailId = '"
								+ subMerchantId + "' and U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
								+ UserStatusType.ACTIVE + "'";
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerName = sessionUser.getBusinessName();
				if (StringUtils.isNotBlank(virtualAccountNo)) {
					query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.virtualAccountNo = '" + virtualAccountNo + "' and U.resellerId = '" + sessionUser.getResellerId() + "'";
				} else {
					
					if (merchantPayId.equalsIgnoreCase("ALL")) {
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.resellerId = '"
								+ sessionUser.getResellerId() + "' and U.userType = '" + UserType.MERCHANT
								+ "' and U.userStatus='" + UserStatusType.ACTIVE
								+ "' and U.superMerchantId IS NULL";
					} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantId)) {
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.payId = '" + merchantPayId
								+ "' and U.resellerId = '" + sessionUser.getResellerId() + "' and U.userType = '"
								+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE
								+ "'";
					} else if (!merchantPayId.equalsIgnoreCase("ALL") && subMerchantId.equalsIgnoreCase("ALL")) {
						merchantName = getBusinessNameByPayId(merchantPayId);
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.userType = '"
								+ UserType.MERCHANT + "' and U.superMerchantId = '" + merchantPayId
								+ "' and U.userStatus='" + UserStatusType.ACTIVE
								+ "' and U.isSuperMerchant = false";
					} else if (!merchantPayId.equalsIgnoreCase("ALL") && !subMerchantId.equalsIgnoreCase("ALL")) {
						merchantName = getBusinessNameByPayId(merchantPayId);
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.superMerchantId = '"
								+ merchantPayId + "' and U.emailId = '" + subMerchantId + "' and U.userType = '"
								+ UserType.MERCHANT + "' and U.userStatus='" + UserStatusType.ACTIVE
								+ "'";
					}
				}
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT) || sessionUser.isSuperMerchant()) {
				
				if (StringUtils.isNotBlank(sessionUser.getResellerId())) {
					resellerName = getBusinessNameByResellerId(sessionUser.getResellerId());
				}
				
				if (StringUtils.isNotBlank(virtualAccountNo)) {
					query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.virtualAccountNo = '" + virtualAccountNo + "' and U.superMerchantId = '" + sessionUser.getPayId() + "'";
				} else {
					
					merchantName = sessionUser.getBusinessName();
					if (subMerchantId.equalsIgnoreCase("ALL")) {
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.superMerchantId = '"
								+ sessionUser.getPayId() + "' and U.userType = '" + UserType.MERCHANT
								+ "' and U.userStatus='" + UserStatusType.ACTIVE
								+ "' and U.isSuperMerchant = false";
					} else {
						query = "Select businessName, virtualAccountNo, resellerId, virtualAccountFlag, isSuperMerchant, superMerchantId from User U where U.emailId = '"
								+ subMerchantId + "' and U.userType = '" + UserType.MERCHANT + "' and U.userStatus='"
								+ UserStatusType.ACTIVE + "'";
					}
				}
			}
			List<Object[]> merchantListRaw = session.createQuery(query).setCacheable(true).getResultList();

			for (Object[] objects : merchantListRaw) {
				ECollectionObject vaData = new ECollectionObject();

				if (StringUtils.isNotEmpty(merchantName)) {
					vaData.setMerchant(merchantName);
					vaData.setSubMerchant((String) objects[0]);
				} else if(StringUtils.isNotBlank(virtualAccountNo) && (boolean) objects[4]==false && StringUtils.isNotBlank((String) objects[5])){
					vaData.setMerchant(getBusinessNameByPayId((String) objects[5]));
					vaData.setSubMerchant((String) objects[0]);
				} else {
					vaData.setMerchant((String) objects[0]);
					vaData.setSubMerchant("NA");
				}
				
				vaData.setMerchantVirtualAccountNumber((String) objects[1]);

				if (StringUtils.isNotEmpty(resellerName)) {
					vaData.setReseller(resellerName);
				} else if (StringUtils.isNotBlank((String) objects[2])) {
					vaData.setReseller(getBusinessNameByResellerId((String) objects[2]));
				} else {
					vaData.setReseller("NA");
				}
				vaData.setVirtualAccountFlag(String.valueOf(objects[3]));
				VADataList.add(vaData);
			}

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
			return VADataList;
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
			return VADataList;
		} finally {
			autoClose(session);
		}
		return VADataList;
	}
	
	public String getBusinessNameByResellerId(String resellerId) {
		String businessName = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			businessName = (String) session.createQuery("Select businessName from User U where U.resellerId = :resellerId and U.userType = '" + UserType.RESELLER + "'")
					.setParameter("resellerId", resellerId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return businessName;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUserFetchOneColumn(String tablename , String collName) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<User> userList = new ArrayList<User>();

		try {
			 userList = session.createQuery("from User u where u.virtualAccountNo is not NULL").getResultList();
					tx.commit();
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userList;
	}

	
	
	@SuppressWarnings("unchecked")
	public boolean checkAcquirer(String name,String lastName , String businessName) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		boolean result = false ;
		List<User> userList = new ArrayList<User>();
		try {
			//String sqlRestul=null;
			  userList= session.createQuery("from User U where U.firstName = :firstName and U.lastName = :lastName and U.businessName = :businessName and U.userType = '" + UserType.ACQUIRER + "'")
					.setParameter("firstName", name).setParameter("lastName", lastName).setParameter("businessName", businessName).getResultList();
			tx.commit();
			if(userList.size()!=0) {
				result=true;
			}
			/*
			 * if(sqlRestul!=null) { result= true; };
			 */
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return result;
		
	}
	
	public Boolean checkValidPayId(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		String dbPayId = null;
		try {
			dbPayId = (String) session.createQuery("Select payId from User U where U.payId = :payId AND U.userType='MERCHANT' AND U.userStatus='ACTIVE'")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
			if(StringUtils.isNotBlank(dbPayId)) {
				return true;
			} else {
				return false;
			}
		} catch (NoResultException noResultException) {
			return false;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error" + hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			logger.error("error " + e);
		} finally {
			autoClose(session);
		}
		return false;
	}
	
	public boolean getCheckoutFlag(String payId) {
		boolean checkoutflag = false;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			checkoutflag = (boolean) session
					.createQuery("Select checkOutJsFlag from User U where U.payId = :payId and U.userType = '"
							+ UserType.MERCHANT + "'")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return checkoutflag;
	}
	
	public String getResellerIdByPayId(String payId) {
		String resellerId = "";
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			resellerId = (String) session
					.createQuery("Select resellerId from User U where U.payId = :payId and U.userType = '"
							+ UserType.MERCHANT + "'")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return resellerId;
	}

    public User findByMerchantName(String merchant, String subMerchant) {
        
        Session session = HibernateSessionProvider.getSession();
        Transaction tx = session.beginTransaction();
        User user =null;
        try {
            String businessName = null; 
            if(StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("NA")){
                businessName=subMerchant;
            }else{
                businessName=merchant;
            }

            user =  (User) session.createQuery("from User U where U.businessName = :businessName and U.userType = '"
                            + UserType.MERCHANT + "'")
                    .setParameter("businessName", businessName).getSingleResult();
            tx.commit();
        } catch (ObjectNotFoundException objectNotFound) {
            handleException(objectNotFound, tx);
        } catch (HibernateException hibernateException) {
            handleException(hibernateException, tx);
        } finally {
            autoClose(session);
        }
        return user;
    }

	// Added by shaiwal to replace method getMerchantAndSuperMerchantList where complete User object is being used for generating list
    // In the similar way we have to implement this change for other methods.
    
    @SuppressWarnings("unchecked")
    public List<Merchants> getMerchantAndSuperMerchantList2() {

        List<Merchants> merchantList = new ArrayList<Merchants>();
        String query = "Select payId, emailId, businessName from User U where U.userType = 'MERCHANT' and U.userStatus != 'PENDING' and ((U.isSuperMerchant = true) or (U.superMerchantId = null)) ";
    
        try (Connection connection = getConnection()){
            
            PreparedStatement prepStmt = connection.prepareStatement(query);
            ResultSet rs = prepStmt.executeQuery();
            
            while (rs.next()) {
                Merchants merchant = new Merchants();
                merchant.setPayId(rs.getString("payId"));
                merchant.setEmailId(rs.getString("emailId"));
                merchant.setBusinessName(rs.getString("businessName"));
                merchantList.add(merchant);
            }
            
            return merchantList;
        } catch (Exception e) {
            logger.error("Exception" ,e);
        }
        return merchantList;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<User> getUserProfileByMobile(String mobile) {

        List<User> responseUserList = new ArrayList<User>();
        Session session = HibernateSessionProvider.getSession();
        Transaction tx = session.beginTransaction();
        List<Object[]> userList = new ArrayList<Object[]>();
        try {
            userList = session
                    .createQuery("Select businessName,emailId,payId from User U where U.mobile = :mobile")
                    .setParameter("mobile", mobile).getResultList();
            
            for (Object[] objects : userList) {
                User user = new User();
                user.setBusinessName((String) objects[0]);
                user.setEmailId((String) objects[1]);
                user.setPayId((String) objects[2]);
                user.setMobile(mobile);
                responseUserList.add(user);
            }
            tx.commit();
        } catch (ObjectNotFoundException objectNotFound) {
            handleException(objectNotFound, tx);
        } catch (HibernateException hibernateException) {
            handleException(hibernateException, tx);
        } finally {
            autoClose(session);
        }
        return responseUserList;
    }
    
    public void resetUserPIN(String pin, String failLoginCount, String updateBy, String mobile, String payId) {
        Session session = HibernateSessionProvider.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createQuery(
                    "update User U set U.pin = :pin, U.failLoginCount = :failLoginCount, U.updatedBy = :updatedBy "
                            + "where U.mobile = :mobile and U.payId = :payId")
                    .setParameter("pin", pin).setParameter("failLoginCount", failLoginCount)
                    .setParameter("updatedBy", updateBy).setParameter("mobile", mobile)
                    .setParameter("payId", payId).executeUpdate();
            tx.commit();
        } catch (ObjectNotFoundException objectNotFound) {
            handleException(objectNotFound, tx);
        } catch (HibernateException hibernateException) {
            handleException(hibernateException, tx);
        } finally {
            autoClose(session);
        }
    }
   
}
