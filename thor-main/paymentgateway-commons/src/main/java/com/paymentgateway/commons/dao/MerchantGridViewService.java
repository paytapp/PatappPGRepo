package com.paymentgateway.commons.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class MerchantGridViewService {

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantGridViewService.class.getName());
	private final static String query = "Select payId, businessName, emailId, mobile,registrationDate,userType,userStatus,checkerName,makerName from User where (userType='MERCHANT') order by industryCategory ";
	private final static String querySubUser = "select p.payId,p.emailId, p.userStatus,p.Mobile,p.registrationDate,p.userType,u.businessName from User p inner join User u ON (p.parentPayId = u.payId) where (p.userType='SUBUSER') order by p.payId ";
	private final static String querySubUserList = "select payId, emailId, userStatus,Mobile,registrationDate,userType, (select businessName from User  where (userType='MERCHANT' and payId =?)) As 'businessName' from User where (userType='SUBUSER' and parentPayId =?) order BY parentPayId ";
	//private final static String businesTypeQuery = "select payId, businessName, emailId, userStatus,Mobile,registrationDate,userType,checkerName,makerName,makerStatus,checkerStatus,makerStatusUpDate,checkerStatusUpDate from User u where u.userType='MERCHANT'";
	private final static String resellerListQuery = "select payId, resellerId, emailId, mobile, businessName, userStatus, registrationDate, userType from User where (userType='RESELLER') order by emailId ";
	private final static String reselerQuery = "select payId,resellerId, businessName, emailId, userStatus,Mobile,registrationDate,userType from User where userType='MERCHANT' and resellerId=? ";
	//private final static String CheckerMakerQueryBySubAdmin = "select payId, businessName, emailId, userStatus,Mobile,registrationDate,userType,checkerName,makerName,makerStatus,checkerStatus,makerStatusUpDate,checkerStatusUpDate from User u where u.userType='MERCHANT'";
	private final static String CheckerMakerQueryBySubAdmin = "from User u where u.userType='MERCHANT'";
	private final static String businesTypeQuery = "from User u where u.userType='MERCHANT'";
	
	public static Map<String, User> merchantListMap = new HashMap<String, User>();
	
	public MerchantGridViewService() {
	}

	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}

	@SuppressWarnings("unchecked")
	public List<MerchantDetails> getAllMerchants() throws SystemException {
		List<MerchantDetails> merchantsList = new ArrayList<MerchantDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			List<Object[]> merchantListRaw = (List<Object[]>) session.createQuery(query).getResultList();
			for (Object[] objects : merchantListRaw) {
				MerchantDetails merchant = new MerchantDetails();
				merchant.setPayId((String) objects[0]);
				merchant.setBusinessName((String) objects[1]);
				merchant.setEmailId((String) objects[2]);
				merchant.setMobile((String) objects[3]);
				merchant.setRegistrationDate((String) objects[4].toString());
				merchant.setUserType((String) objects[5].toString());
				String status = (String) objects[6].toString();
				merchant.setCheckerName((String) objects[7]);
				merchant.setMakerName((String) objects[8]);
				if (status != null) {
					UserStatusType userStatus = UserStatusType.valueOf(status);
					merchant.setStatus(userStatus);
				}
				merchantsList.add(merchant);
			}
			tx.commit();
			return merchantsList;
		} catch (Exception e) {
			logger.error("Exception caught : " , e);
		}
		return merchantsList;
	}

	public List<MerchantDetails> getAllReselerMerchants(String resellerId) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection.prepareStatement(reselerQuery)) {
				prepStmt.setString(1, resellerId);
				try (ResultSet rs = prepStmt.executeQuery()) {
					while (rs.next()) {
						MerchantDetails merchant = new MerchantDetails();
						merchant.setPayId(rs.getString("payId"));
						merchant.setResellerId(rs.getString("resellerId"));
						merchant.setBusinessName(rs.getString("businessName"));
						merchant.setEmailId(rs.getString("emailId"));
						merchant.setMobile(rs.getString("Mobile"));
						merchant.setRegistrationDate(rs.getString("registrationDate"));
						merchant.setUserType(rs.getString("userType"));
						String status = rs.getString("userStatus");

						if (status != null) {
							UserStatusType userStatus = UserStatusType.valueOf(status);
							merchant.setStatus(userStatus);
						}
						merchants.add(merchant);
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}

	/*
	 * public List<MerchantDetails> getAllReseller() throws SystemException {
	 * List<MerchantDetails> merchants = new ArrayList<MerchantDetails>(); try
	 * (Connection connection = getConnection()) { try (PreparedStatement prepStmt =
	 * connection.prepareStatement(resellerListQuery)) { try (ResultSet rs =
	 * prepStmt.executeQuery()) { while (rs.next()) {
	 * 
	 * MerchantDetails merchant = new MerchantDetails();
	 * merchant.setPayId(rs.getString("payId"));
	 * merchant.setResellerId(rs.getString("resellerId"));
	 * merchant.setBusinessName(rs.getString("businessName"));
	 * merchant.setEmailId(rs.getString("emailId"));
	 * merchant.setMobile(rs.getString("mobile"));
	 * merchant.setRegistrationDate(rs.getString("registrationDate"));
	 * merchant.setUserType(rs.getString("userType")); String status =
	 * rs.getString("userStatus");
	 * 
	 * if (status != null) { UserStatusType userStatus =
	 * UserStatusType.valueOf(status); merchant.setStatus(userStatus); }
	 * merchants.add(merchant); } } } } catch (SQLException exception) {
	 * logger.error("Database error", exception); throw new
	 * SystemException(ErrorType.DATABASE_ERROR,
	 * ErrorType.DATABASE_ERROR.getResponseMessage()); } return merchants; }
	 */
	
	@SuppressWarnings("unchecked")
	public List<MerchantDetails> getAllReseller() throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			List<Object[]> merchantListRaw = session.createQuery(resellerListQuery).getResultList();
			for (Object[] objects : merchantListRaw) {
				MerchantDetails merchant = new MerchantDetails();
				merchant.setPayId((String) objects[0]);
				merchant.setResellerId((String) objects[1]);
				merchant.setEmailId((String) objects[2]);
				merchant.setMobile((String) objects[3]);
				merchant.setBusinessName((String) objects[4]);
				String status = (String) objects[5].toString();
				merchant.setRegistrationDate((String) objects[6].toString());
				merchant.setUserType((String) objects[7].toString());
				
				if (status != null) {
					UserStatusType userStatus = UserStatusType.valueOf(status);
					merchant.setStatus(userStatus);
				}
				merchants.add(merchant);
			}
			tx.commit();
			return merchants;
		} catch (Exception e) {
			logger.error("Exception caught : " , e);
		}
		return merchants;
	}
	
	public void addUserInMap(User user) {
		merchantListMap.put(user.getPayId(), user);
	}
	public List<MerchantDetails> getAllMerchantsFromMap(String industryCategory, String status, String statusBy) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
		
			if(merchantListMap.isEmpty() || merchantListMap.size() < 10) {
				List<User> userList = userDao.getAllMerchantList();
				for(User user : userList) {
					merchantListMap.put(user.getPayId(), user);
				}
			}
				
				for(User user : merchantListMap.values()) {
					
					// Skip Sub Merchant Accounts
					if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
						continue;
					}
					if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") && statusBy.equalsIgnoreCase("ALL")) {
						
					}else if(!industryCategory.equalsIgnoreCase("ALL") && !industryCategory.equalsIgnoreCase(user.getIndustryCategory())){
						continue;
					}else if(!status.equalsIgnoreCase("ALL")) {
						if(!statusBy.equalsIgnoreCase("ALL")) {
							if(statusBy.equalsIgnoreCase("Checker") && !status.equalsIgnoreCase(user.getCheckerStatus()) && !status.equalsIgnoreCase(user.getUserStatus().getStatus())) {
								continue;
							}else if(statusBy.equalsIgnoreCase("Maker") && !status.equalsIgnoreCase(user.getMakerStatus()) && !status.equalsIgnoreCase(user.getUserStatus().getStatus())) {
								continue;
							}else if(statusBy.equalsIgnoreCase("Admin") && !status.equalsIgnoreCase(user.getAdminStatus()) && !status.equalsIgnoreCase(user.getUserStatus().getStatus())) {
								continue;
							}
						}else if(!status.equalsIgnoreCase(user.getUserStatus().getStatus())){
							continue;
						}
					}
					
					MerchantDetails merchant = new MerchantDetails();
					String registrationDate = formatter.format(user.getRegistrationDate());
					if(user.getUpdateDate() != null) {
						String updatedDate = formatter.format(user.getUpdateDate());
						merchant.setUpdatedDate(updatedDate);
					}else {
						merchant.setUpdatedDate("NA");
					}
					merchant.setPayId(user.getPayId());
					merchant.setBusinessName(user.getBusinessName());
					merchant.setEmailId(user.getEmailId());
					merchant.setMobile(user.getMobile());
					merchant.setRegistrationDate(registrationDate);
					merchant.setUserType(user.getUserType().name());
					merchant.setCheckerName(user.getCheckerName());
					merchant.setMakerName(user.getMakerName());
					merchant.setMakerStatus(user.getMakerStatus());
					merchant.setCheckerStatus(user.getCheckerStatus());
					merchant.setMakerStatusUpDate(user.getMakerStatusUpDate());
					merchant.setCheckerStatusUpDate(user.getCheckerStatusUpDate());
					
					String status1 = user.getUserStatus().getStatus();
	
				if (status != null) {
					UserStatusType userStatus = UserStatusType.valueOf(status1);
					merchant.setStatus(userStatus);
				}
				merchants.add(merchant);
			}
		}catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}
	
	public List<MerchantDetails> getAllMerchantsByType(String industryCategory, String status, String statusBy) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Query<User> query = null;
		
		List<User> users = new ArrayList<User>();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(businesTypeQuery);
		try {

			if(statusBy.equalsIgnoreCase("ALL")) {
				
				if (industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
					query = session.createQuery(sqlBuilder.toString(), User.class);
					
				} else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
					sqlBuilder.append(" and u.userStatus = :userStatus");
					query = session.createQuery(sqlBuilder.toString(), User.class);
					if(status.equalsIgnoreCase("ACTIVE"))
						query.setParameter("userStatus", UserStatusType.ACTIVE);
					if(status.equalsIgnoreCase("PENDING"))
						query.setParameter("userStatus", UserStatusType.PENDING);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", UserStatusType.APPROVED);
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", UserStatusType.REJECTED);
					if(status.equalsIgnoreCase("SUSPENDED"))
						query.setParameter("userStatus", UserStatusType.SUSPENDED);
					if(status.equalsIgnoreCase("TERMINATED"))
						query.setParameter("userStatus", UserStatusType.TERMINATED);
					if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
						query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);
					
				} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
					sqlBuilder.append(" and u.industryCategory = :industryCategory");
					query = session.createQuery(sqlBuilder.toString(), User.class);
					query.setParameter("industryCategory", industryCategory);
					
				}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
					sqlBuilder.append(" and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
					
					query = session.createQuery(sqlBuilder.toString(), User.class);
					query.setParameter("industryCategory", industryCategory);
					if(status.equalsIgnoreCase("ACTIVE"))
						query.setParameter("userStatus", UserStatusType.ACTIVE);
					if(status.equalsIgnoreCase("PENDING"))
						query.setParameter("userStatus", UserStatusType.PENDING);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", UserStatusType.APPROVED);
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", UserStatusType.REJECTED);
					if(status.equalsIgnoreCase("SUSPENDED"))
						query.setParameter("userStatus", UserStatusType.SUSPENDED);
					if(status.equalsIgnoreCase("TERMINATED"))
						query.setParameter("userStatus", UserStatusType.TERMINATED);
					if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
						query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);
				}
				
			} else {
					
					if(statusBy.equalsIgnoreCase("Checker")) {
						if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") ){
							sqlBuilder.append(" and u.checkerStatus = :checkerStatus");
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("checkerStatus", status);
							
						}else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" and u.checkerStatus = :checkerStatus and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("checkerStatus", status);
							if(status.equalsIgnoreCase("ACTIVE"))
								query.setParameter("userStatus", UserStatusType.ACTIVE);
							if(status.equalsIgnoreCase("PENDING"))
								query.setParameter("userStatus", UserStatusType.PENDING);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", UserStatusType.APPROVED);
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", UserStatusType.REJECTED);
							if(status.equalsIgnoreCase("SUSPENDED"))
								query.setParameter("userStatus", UserStatusType.SUSPENDED);
							if(status.equalsIgnoreCase("TERMINATED"))
								query.setParameter("userStatus", UserStatusType.TERMINATED);
							if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
								query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);					
		
						} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" and u.checkerStatus = :checkerStatus and u.industryCategory = :industryCategory");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("checkerStatus", status);
							query.setParameter("industryCategory", industryCategory);
		
						}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							
							sqlBuilder.append(" and u.checkerStatus = :checkerStatus and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("checkerStatus", status);
							query.setParameter("industryCategory", industryCategory);
							if(status.equalsIgnoreCase("ACTIVE"))
								query.setParameter("userStatus", UserStatusType.ACTIVE);
							if(status.equalsIgnoreCase("PENDING"))
								query.setParameter("userStatus", UserStatusType.PENDING);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", UserStatusType.APPROVED);
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", UserStatusType.REJECTED);
							if(status.equalsIgnoreCase("SUSPENDED"))
								query.setParameter("userStatus", UserStatusType.SUSPENDED);
							if(status.equalsIgnoreCase("TERMINATED"))
								query.setParameter("userStatus", UserStatusType.TERMINATED);
							if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
								query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);						
						}
					}else if(statusBy.equalsIgnoreCase("Maker")) {
						if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") ){
							sqlBuilder.append(" and u.makerStatus = :makerStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("makerStatus", status);
							
						}else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" and u.makerStatus = :makerStatus and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("makerStatus", status);
							if(status.equalsIgnoreCase("ACTIVE"))
								query.setParameter("userStatus", UserStatusType.ACTIVE);
							if(status.equalsIgnoreCase("PENDING"))
								query.setParameter("userStatus", UserStatusType.PENDING);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", UserStatusType.APPROVED);
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", UserStatusType.REJECTED);
							if(status.equalsIgnoreCase("SUSPENDED"))
								query.setParameter("userStatus", UserStatusType.SUSPENDED);
							if(status.equalsIgnoreCase("TERMINATED"))
								query.setParameter("userStatus", UserStatusType.TERMINATED);
							if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
								query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);						
		
						} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" and u.makerStatus = :makerStatus and u.industryCategory = :industryCategory");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("makerStatus", status);
							query.setParameter("industryCategory", industryCategory);
		
						}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							
							sqlBuilder.append(" and u.makerStatus = :makerStatus and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("makerStatus", status);
							query.setParameter("industryCategory", industryCategory);
							if(status.equalsIgnoreCase("ACTIVE"))
								query.setParameter("userStatus", UserStatusType.ACTIVE);
							if(status.equalsIgnoreCase("PENDING"))
								query.setParameter("userStatus", UserStatusType.PENDING);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", UserStatusType.APPROVED);
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", UserStatusType.REJECTED);
							if(status.equalsIgnoreCase("SUSPENDED"))
								query.setParameter("userStatus", UserStatusType.SUSPENDED);
							if(status.equalsIgnoreCase("TERMINATED"))
								query.setParameter("userStatus", UserStatusType.TERMINATED);
							if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
								query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);					
						}
					}else if(statusBy.equalsIgnoreCase("Admin")) {
						if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") ){
							sqlBuilder.append(" and u.adminStatus = :adminStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("adminStatus", status);
							
						}else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" and u.adminStatus = :adminStatus and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("adminStatus", status);
							if(status.equalsIgnoreCase("ACTIVE"))
								query.setParameter("userStatus", UserStatusType.ACTIVE);
							if(status.equalsIgnoreCase("PENDING"))
								query.setParameter("userStatus", UserStatusType.PENDING);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", UserStatusType.APPROVED);
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", UserStatusType.REJECTED);
							if(status.equalsIgnoreCase("SUSPENDED"))
								query.setParameter("userStatus", UserStatusType.SUSPENDED);
							if(status.equalsIgnoreCase("TERMINATED"))
								query.setParameter("userStatus", UserStatusType.TERMINATED);
							if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
								query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);						
		
						} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" and u.adminStatus = :adminStatus and u.industryCategory = :industryCategory");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("adminStatus", status);
							query.setParameter("industryCategory", industryCategory);
		
						}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							
							sqlBuilder.append(" and u.adminStatus = :adminStatus and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), User.class);
							query.setParameter("adminStatus", status);
							query.setParameter("industryCategory", industryCategory);
							if(status.equalsIgnoreCase("ACTIVE"))
								query.setParameter("userStatus", UserStatusType.ACTIVE);
							if(status.equalsIgnoreCase("PENDING"))
								query.setParameter("userStatus", UserStatusType.PENDING);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", UserStatusType.APPROVED);
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", UserStatusType.REJECTED);
							if(status.equalsIgnoreCase("SUSPENDED"))
								query.setParameter("userStatus", UserStatusType.SUSPENDED);
							if(status.equalsIgnoreCase("TERMINATED"))
								query.setParameter("userStatus", UserStatusType.TERMINATED);
							if(status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
								query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);					
						}
					}
			}
			users = query.getResultList();
			tx.commit();
			
			for(User user : users) {
				
				// Skip Sub Merchant Accounts
				if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
					continue;
				}
				
				String registrationDate = formatter.format(user.getRegistrationDate());
				MerchantDetails merchant = new MerchantDetails();
				merchant.setPayId(user.getPayId());
				merchant.setBusinessName(user.getBusinessName());
				merchant.setEmailId(user.getEmailId());
				merchant.setMobile(user.getMobile());
				merchant.setRegistrationDate(registrationDate);
				merchant.setUserType(user.getUserType().name());
				merchant.setCheckerName(user.getCheckerName());
				merchant.setMakerName(user.getMakerName());
				merchant.setMakerStatus(user.getMakerStatus());
				merchant.setCheckerStatus(user.getCheckerStatus());
				merchant.setMakerStatusUpDate(user.getMakerStatusUpDate());
				merchant.setCheckerStatusUpDate(user.getCheckerStatusUpDate());
				String status1 = user.getUserStatus().getStatus();

			if (status != null) {
				UserStatusType userStatus = UserStatusType.valueOf(status1);
				merchant.setStatus(userStatus);
			}
			merchants.add(merchant);
		}
			
		}catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}
		
	public List<MerchantDetails> getAllMerchants(String businessType) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();

		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection.prepareStatement(businesTypeQuery)) {
				prepStmt.setString(1, businessType);
				try (ResultSet rs = prepStmt.executeQuery()) {
					while (rs.next()) {

						MerchantDetails merchant = new MerchantDetails();
						merchant.setPayId(rs.getString("payId"));
						merchant.setBusinessName(rs.getString("businessName"));
						merchant.setEmailId(rs.getString("emailId"));
						merchant.setMobile(rs.getString("Mobile"));
						merchant.setRegistrationDate(rs.getString("registrationDate"));
						merchant.setUserType(rs.getString("userType"));
						String status = rs.getString("userStatus");

						if (status != null) {
							UserStatusType userStatus = UserStatusType.valueOf(status);
							merchant.setStatus(userStatus);
						}
						merchants.add(merchant);
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}

		return merchants;
	}

	public List<MerchantDetails> getAllMerchantSubUser() throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection.prepareStatement(querySubUser)) {
				try (ResultSet rs = prepStmt.executeQuery()) {
					while (rs.next()) {

						MerchantDetails merchant = new MerchantDetails();
						merchant.setPayId(rs.getString("payId"));
						merchant.setEmailId(rs.getString("emailId"));
						merchant.setBusinessName(rs.getString("businessName"));
						merchant.setMobile(rs.getString("Mobile"));
						merchant.setRegistrationDate(rs.getString("registrationDate"));
						merchant.setUserType(rs.getString("userType"));
						String status = rs.getString("userStatus");

						if (status != null) {
							UserStatusType userStatus = UserStatusType.valueOf(status);
							merchant.setStatus(userStatus);
						}
						merchants.add(merchant);
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}

	public List<MerchantDetails> getAllMerchantSubUserList(String emailId) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		User user = userDao.findPayIdByEmail(emailId);
		String parentPayId = user.getPayId();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection.prepareStatement(querySubUserList)) {
				prepStmt.setString(1, parentPayId);
				prepStmt.setString(2, parentPayId);
				try (ResultSet rs = prepStmt.executeQuery()) {
					while (rs.next()) {

						MerchantDetails merchant = new MerchantDetails();
						merchant.setPayId(rs.getString("payId"));
						merchant.setEmailId(rs.getString("emailId"));
						merchant.setBusinessName(rs.getString("businessName"));
						merchant.setMobile(rs.getString("Mobile"));
						merchant.setRegistrationDate(rs.getString("registrationDate"));
						merchant.setUserType(rs.getString("userType"));
						String status = rs.getString("userStatus");

						if (status != null) {
							UserStatusType userStatus = UserStatusType.valueOf(status);
							merchant.setStatus(userStatus);
						}
						merchants.add(merchant);
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}
	
}