package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;

@Service
public class PendingRequestEmailProcessor {

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private UserDao userDao;
	
	private String parentEmailId;
	private String body;

	PropertiesManager propertiesManager = new PropertiesManager();
	private static Logger logger = LoggerFactory.getLogger(PendingRequestEmailProcessor.class.getName());

	public void processServiceTaxEmail(String requestStatus, String loginEmailId, String loginUserType,
			String businessType) {

		logger.info("Process email");
		User user = new User();
		user = userDao.findPayIdByEmail(loginEmailId);
		String subject = "Service tax update notification";
		if (loginUserType.equals(UserType.SUBADMIN.toString())) {
			String parentUser = userDao.getEmailIdByPayId(user.getParentPayId());
			setParentEmailId(parentUser);
		}

		switch (requestStatus) {
		case "Active":
			setBody(propertiesManager.getPendingMessages("ServiceTaxUpdated") + businessType);

			if (loginUserType.equals(UserType.SUBADMIN.toString())) {

				logger.info("Process email active , parentEmailid = " + getParentEmailId() + "  subject = " + subject
						+ "     body = " + getBody());
				//emailControllerServiceProvider.emailPendingRequest(getParentEmailId(), subject, getBody());
			} else {

			}
			break;

		case "Pending":
			setBody(propertiesManager.getPendingMessages("ServiceTaxUpdateRequest") + businessType);
			logger.info("Process email pending , parentEmailid = " + getParentEmailId() + "  subject = " + subject
					+ "     body = " + getBody());
			//emailControllerServiceProvider.emailPendingRequest(getParentEmailId(), subject, getBody());

			break;

		}
	}

	public void processServiceTaxApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType,
			String businessType, String requestedByEmailId) {

		logger.info("processServiceTaxApproveRejectEmails");

		String subject = "Service Tax Update notification";
		logger.info("processServiceTaxApproveRejectEmails requested by " + requestedByEmailId);

		switch (requestStatus) {

		case "Approved":
			setBody(propertiesManager.getPendingMessages("ServiceTaxUpdateApprove") + businessType + " by Admin.");

			logger.info("Process email active , requestedByEmailId = " + requestedByEmailId + "  subject = " + subject
					+ "     body = " + getBody());
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);
			
			break;

		case "Rejected":
			setBody(propertiesManager.getPendingMessages("ServiceTaxUpdateReject") + businessType + " by Admin.");
			logger.info("Process email pending , parentEmailid = " + loginEmailId + "  subject = " + subject
					+ "     body = " + getBody());

			logger.info("processServiceTaxApproveRejectEmails requested by " + requestedByEmailId);
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody());

			break;

		}
	}
	
	public void processTDRRequestEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantEmailId, String permission) {

		logger.info("processTDRRequestEmail");

		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {

				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();

				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>();

				for (User user : subAdminActiveList) {

					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}

				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();

				for (Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {

					Set<Roles> rol = role.getValue();
					for (Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}

				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {

					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());

					for (Permissions perm : permissionList) {

						if (perm.getPermissionType().toString()
								.equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
								sendEmailList.add(role.getKey());
								break;
						}
					}
				}

				sendEmailList.add(userDao.getAdminEmail());
				String currentDate = DateCreater.defaultCurrentDateTime();
				User maker = new User();
				User merchant = new User();
				User checker = new User();
				maker = userDao.findPayIdByEmail(loginEmailId);
				merchant = userDao.findPayIdByEmail(merchantEmailId);
				
				for (String emailId : sendEmailList) {

					checker = userDao.findPayIdByEmail(emailId);

					String subject = "Merchant TDR Update Notification";
					if (loginUserType.equals(UserType.SUBADMIN.toString()) && maker != null) {
						String parentUser = userDao.getEmailIdByPayId(maker.getParentPayId());
						setParentEmailId(parentUser);
					}

					switch (requestStatus) {
					case "Active":
						setBody(propertiesManager.getPendingMessages("TDRUpdated"));

						if (loginUserType.equals(UserType.SUBADMIN.toString())) {

							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),
									requestStatus, loginEmailId, checker.getUserType().toString(),
									merchant.getBusinessName(), maker.getBusinessName(), "",
									currentDate);

						} else if(loginUserType.equals(UserType.ADMIN.toString())) {
							
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),
									requestStatus, loginEmailId, checker.getUserType().toString(),
									merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(),
									currentDate);

						}
						break;

					case "Pending":
						setBody(propertiesManager.getPendingMessages("TDRUpdateRequest"));
						logger.info("getParentEmailId() " + getParentEmailId() + " subject " + subject + " getBody() "
								+ getBody());

						emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus,
								loginEmailId, checker.getUserType().toString(), merchant.getBusinessName(),
								maker.getBusinessName(), checker.getBusinessName(), currentDate);
						break;

					}

				}
			}
		};

		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread thread = new Thread(runnable); thread.start();
		 */
	}

	public void processMappingEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantEmailId, String permission) {

		logger.info(" process Merchant Mapping Email");
		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {

				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();
				
				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>(); 
				
				for(User user : subAdminActiveList) { 
					
					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}				
				
				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
				
				for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
					
					Set<Roles> rol = role.getValue();
					for(Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}				
				
				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
					
					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());
					
					for(Permissions perm : permissionList) {
					
					if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
						sendEmailList.add(role.getKey());
						break;
						}	
					}
				}
				
				sendEmailList.add(userDao.getAdminEmail());
				String currentDate = DateCreater.defaultCurrentDateTime();
				User maker = new User();
				User merchant = new User();
				User checker = new User();
				maker = userDao.findPayIdByEmail(loginEmailId);
				merchant = userDao.findPayIdByEmail(merchantEmailId);
				
				for (String emailId : sendEmailList) {
					
					checker = userDao.findPayIdByEmail(emailId);
					
					String subject = "Merchant Mapping Update Notification";

					if (loginUserType.equals(UserType.SUBADMIN.toString())) {
						String parentUser = userDao.getEmailIdByPayId(maker.getParentPayId());
						setParentEmailId(parentUser);
					}

					switch (requestStatus) {
					case "Active":

						if (loginUserType.equals(UserType.SUBADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("MerchantMappingUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),	requestStatus, loginEmailId, checker.getUserType().toString(),
									merchant.getBusinessName(), maker.getBusinessName(), "", currentDate);

						} else if (loginUserType.equals(UserType.ADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("MerchantMappingUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),
									requestStatus, loginEmailId, loginUserType, merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(), currentDate);
						}

						break;

					case "Pending":
						setBody(propertiesManager.getPendingMessages("MerchantMappingUpdateRequest"));
						logger.info("Process email pending , parentEmailid = "
								+ getParentEmailId());
						emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus,
								loginEmailId, loginUserType, merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(), currentDate);

						break;

					}
				}

			}
		};
		
		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread emailSendThread = new Thread(runnable); emailSendThread.start();
		 */
	}
	
	
	public void processRouterConfigEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantEmailId, String permission) {

		logger.info(" process Router Configuration Email");
		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {

				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();
				
				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>(); 
				
				for(User user : subAdminActiveList) { 
					
					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}				
				
				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
				
				for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
					
					Set<Roles> rol = role.getValue();
					for(Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}				
				
				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
					
					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());
					
					for(Permissions perm : permissionList) {
					
					if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
						sendEmailList.add(role.getKey());
						break;
						}	
					}
				}
				
				sendEmailList.add(userDao.getAdminEmail());
				String currentDate = DateCreater.defaultCurrentDateTime();
				User maker = new User();
				User merchant = new User();
				User checker = new User();
				maker = userDao.findPayIdByEmail(loginEmailId);
				merchant = userDao.findPayIdByEmail(merchantEmailId);
				
				for (String emailId : sendEmailList) {
					
					checker = userDao.findPayIdByEmail(emailId);
					
					String subject = "Router Configuration Update Notification";

					if (loginUserType.equals(UserType.SUBADMIN.toString()) && maker != null) {
						String parentUser = userDao.getEmailIdByPayId(maker.getParentPayId());
						setParentEmailId(parentUser);
					}

					switch (requestStatus) {
					case "Active":

						if (loginUserType.equals(UserType.SUBADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("RouterConfigUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),	requestStatus, loginEmailId, checker.getUserType().toString(),
									merchant.getBusinessName(), maker.getBusinessName(), "", currentDate);

						} else if (loginUserType.equals(UserType.ADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("RouterConfigUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),
									requestStatus, loginEmailId, loginUserType, merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(), currentDate);
						}

						break;

					case "Pending":
						setBody(propertiesManager.getPendingMessages("RouterConfigUpdateRequest"));
						logger.info("Process email pending , parentEmailid = "
								+ getParentEmailId());
						emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus,
								loginEmailId, loginUserType, merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(), currentDate);

						break;

					}
				}

			}
		};
		
		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread emailSendThread = new Thread(runnable); emailSendThread.start();
		 */
	}
	
	
	public void BulkUserAddEmail(String requestStatus, String loginEmailId, String loginUserType, String permission) {

		logger.info(" process Bulk User Email");
		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {

				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();
				
				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>(); 
				
				for(User user : subAdminActiveList) { 
					
					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}				
				
				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
				
				for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
					
					Set<Roles> rol = role.getValue();
					for(Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}				
				
				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
					
					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());
					
					for(Permissions perm : permissionList) {
					
					if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
						sendEmailList.add(role.getKey());
						break;
						}	
					}
				}
				
				sendEmailList.add(userDao.getAdminEmail());
				String currentDate = DateCreater.defaultCurrentDateTime();
				User maker = new User();
				User checker = new User();
				maker = userDao.findPayIdByEmail(loginEmailId);
				
				for (String emailId : sendEmailList) {
					
					checker = userDao.findPayIdByEmail(emailId);
					
					String subject = "Bulk User Update Notification";

					if (loginUserType.equals(UserType.SUBADMIN.toString()) && maker != null) {
						String parentUser = userDao.getEmailIdByPayId(maker.getParentPayId());
						setParentEmailId(parentUser);
					}

					switch (requestStatus) {
					case "Active":

						if (loginUserType.equals(UserType.SUBADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("BulkUserUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),	requestStatus, loginEmailId, checker.getUserType().toString(),
									"", maker.getBusinessName(), "", currentDate);

						} else if (loginUserType.equals(UserType.ADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("BulkUserUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),
									requestStatus, loginEmailId, loginUserType,"", maker.getBusinessName(), checker.getBusinessName(), currentDate);
						}

						break;

					case "Pending":
						setBody(propertiesManager.getPendingMessages("BulkUserUpdateRequest"));
						logger.info("Process email pending , parentEmailid = "
								+ getParentEmailId());
						emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus,
								loginEmailId, loginUserType, "", maker.getBusinessName(), checker.getBusinessName(), currentDate);

						break;

					}
				}

			}
		};
		
		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread emailSendThread = new Thread(runnable); emailSendThread.start();
		 */
	}
	
	public void processEditMerchantDetailsEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantEmailId) {

		logger.info(" process Edit Merchant Details Email");
		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {

				List<String> sendEmailList = new ArrayList<String>();
				
				sendEmailList.add(userDao.getAdminEmail());
				String currentDate = DateCreater.defaultCurrentDateTime();
				User maker = new User();
				User merchant = new User();
				User checker = new User();
				maker = userDao.findPayIdByEmail(loginEmailId);
				merchant = userDao.findPayIdByEmail(merchantEmailId);
				
				for (String emailId : sendEmailList) {
					
					checker = userDao.findPayIdByEmail(emailId);
					
					String subject = "Edit Merchant Details Update Notification";

					if (loginUserType.equals(UserType.SUBADMIN.toString())) {
						String parentUser = userDao.getEmailIdByPayId(maker.getParentPayId());
						setParentEmailId(parentUser);
					}

					switch (requestStatus) {
					case "Active":

						if (loginUserType.equals(UserType.SUBADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("MerchantEditDetailsUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),	requestStatus, loginEmailId, checker.getUserType().toString(),
									merchant.getBusinessName(), maker.getBusinessName(), "", currentDate);

						} else if (loginUserType.equals(UserType.ADMIN.toString())) {

							setBody(propertiesManager.getPendingMessages("MerchantEditDetailsUpdated"));
							emailServiceProvider.emailPendingRequest(emailId, subject, getBody(),
									requestStatus, loginEmailId, loginUserType, merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(), currentDate);
						}

						break;

					case "Pending":
						setBody(propertiesManager.getPendingMessages("MerchantEditDetailsUpdateRequest"));
						logger.info("Process email pending , parentEmailid = "
								+ getParentEmailId());
						emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus,
								loginEmailId, loginUserType, merchant.getBusinessName(), maker.getBusinessName(), checker.getBusinessName(), currentDate);

						break;

					}
				}

			}
		};
		
		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread emailSendThread = new Thread(runnable); emailSendThread.start();
		 */
	}
	
	
	public void processBulkUserApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType, String permission) {

		logger.info("inside processBulkUserApproveRejectEmail");

		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {
			
				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();
				
				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>(); 
				
				for(User user : subAdminActiveList) {
					
					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}				
				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
				
				for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
					
					Set<Roles> rol = role.getValue();
					for (Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}				
				
				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
					
					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());
					
					for(Permissions perm : permissionList) {
					
					if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
						sendEmailList.add(role.getKey());
							break;
						}	
					}
				}
			
			sendEmailList.add(userDao.getAdminEmail());
			String currentDate = DateCreater.defaultCurrentDateTime();
			User checker = new User();
			User maker = new User();
			checker = userDao.findPayIdByEmail(loginEmailId);
			
			for (String emailId : sendEmailList) {
			
			String subject = "Bulk User Update notification";
			switch (requestStatus) {

			case "Active":
				setBody(propertiesManager.getPendingMessages("BulkUserUpdateApprove"));
				
				emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType,"",/* merchantName,*/
						maker.getBusinessName(), checker.getBusinessName(), currentDate);

				break;

			case "Rejected":
				setBody(propertiesManager.getPendingMessages("BulkUserUpdateReject"));
				emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, "",/*merchantName,*/
						maker.getBusinessName(), checker.getBusinessName(), currentDate);

				break;
			  }
			}
		  }
		};
		
		propertiesManager.executorImpl(runnable);
		/*
		 * Thread thread = new Thread(runnable); thread.start();
		 */
	}
	

	public void processRouterConfigApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName, String merchantPayId, String requestedByEmailId, String permission) {

		logger.info("inside processRouterConfigApproveRejectEmail");
		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {
				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();
				
				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>(); 
				
				for(User user : subAdminActiveList) {
					
					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}				
				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
				
				for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
					
					Set<Roles> rol = role.getValue();
					for (Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}				
				
				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
					
					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());
					
					for(Permissions perm : permissionList) {
					
					if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
						sendEmailList.add(role.getKey());
							break;
						}	
					}
				}
			
				sendEmailList.add(userDao.getAdminEmail());
				sendEmailList.add(requestedByEmailId);
			String currentDate = DateCreater.defaultCurrentDateTime();
			User checker = new User();
			User maker = new User();
			checker = userDao.findPayIdByEmail(loginEmailId);
			maker = userDao.findPayIdByEmail(requestedByEmailId);
			
			for (String emailId : sendEmailList) {
			
			String subject = "Merchant Router Configuration Update notification";
			logger.info("processRouterConfigApproveRejectEmail requested by " + requestedByEmailId);

			switch (requestStatus) {

			case "Active":
				setBody(propertiesManager.getPendingMessages("RouterConfigUpdateApprove"));
				emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName,
						maker.getBusinessName(), checker.getBusinessName(), currentDate);
				break;

			case "Rejected":
				setBody(propertiesManager.getPendingMessages("RouterConfigUpdateReject"));
				emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName, 
						maker.getBusinessName(), checker.getBusinessName(), currentDate);
				break;
			  }
			}
		  }
		};
		
		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread thread = new Thread(runnable); thread.start();
		 */
	}
	
	public void processTDRApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName, String merchantPayId, String requestedByEmailId, String permission) {

		logger.info("inside processTDRApproveRejectEmail");

		Runnable runnable = new Runnable() {

			@Override
			public synchronized void run() {
			
				List<String> sendEmailList = new ArrayList<String>();
				List<User> subAdminActiveList = new ArrayList<User>();
				
				Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
				subAdminActiveList = userDao.getAllSubAdmin();
				List<Roles> userPermission = new ArrayList<Roles>(); 
				
				for(User user : subAdminActiveList) {
					
					userMap.put(user.getEmailId(), user.getRoles());
					userPermission.addAll(userMap.get(user.getEmailId()));
				}				
				Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
				
				for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
					
					Set<Roles> rol = role.getValue();
					for (Roles roll : rol) {
						permissionMap.put(role.getKey(), roll.getPermissions());
					}
				}				
				
				for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
					
					Set<Permissions> permissionList = new HashSet<Permissions>();
					permissionList.addAll(role.getValue());
					
					for(Permissions perm : permissionList) {
					
					if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
						sendEmailList.add(role.getKey());
							break;
						}	
					}
				}
			
			sendEmailList.add(userDao.getAdminEmail());
			sendEmailList.add(requestedByEmailId);
			String currentDate = DateCreater.defaultCurrentDateTime();
			User checker = new User();
			User maker = new User();
			checker = userDao.findPayIdByEmail(loginEmailId);
			maker = userDao.findPayIdByEmail(requestedByEmailId);
			
			for (String emailId : sendEmailList) {
			
			String subject = "Merchant TDR Update notification";
			logger.info("processTDRApproveRejectEmail requested by " + requestedByEmailId);

			switch (requestStatus) {

			case "Active":
				setBody(propertiesManager.getPendingMessages("TDRUpdateApprove") + " "+merchantName + " with payId "
						+ merchantPayId + " by Admin.");
				
				emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName,
						maker.getBusinessName(), checker.getBusinessName(), currentDate);

				break;

			case "Rejected":
				setBody(propertiesManager.getPendingMessages("TDRUpdateReject") + " "+merchantName + " with payId "
						+ merchantPayId + " by Admin.");
				emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName,
						maker.getBusinessName(), checker.getBusinessName(), currentDate);

				break;
			  }
			}
		  }
		};
		
		propertiesManager.executorImpl(runnable);
		
		/*
		 * Thread thread = new Thread(runnable); thread.start();
		 */
	}
	
	
	public void processMappingApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName, String merchantPayId, String requestedByEmailId, String permission) {

		logger.info("inside processMappingApproveRejectEmail");
		
		Runnable runnable = new Runnable() {

		@Override
		public synchronized void run() {
		
			List<String> sendEmailList = new ArrayList<String>();
			List<User> subAdminActiveList = new ArrayList<User>();
			
			Map<String, Set<Roles>> userMap = new HashMap<String, Set<Roles>>();
			subAdminActiveList = userDao.getAllSubAdmin();
			List<Roles> userPermission = new ArrayList<Roles>(); 
			
			for(User user : subAdminActiveList) {
				userMap.put(user.getEmailId(), user.getRoles());
				userPermission.addAll(userMap.get(user.getEmailId()));
			}				
			
			Map<String, Set<Permissions>> permissionMap = new HashMap<String, Set<Permissions>>();
			
			for(Map.Entry<String, Set<Roles>> role : userMap.entrySet()) {
				
				Set<Roles> rol = role.getValue();
				for (Roles roll : rol) {
					permissionMap.put(role.getKey(), roll.getPermissions());
				}
			}
			for (Map.Entry<String, Set<Permissions>> role : permissionMap.entrySet()) {
				
				Set<Permissions> permissionList = new HashSet<Permissions>();
				permissionList.addAll(role.getValue());
				
				for(Permissions perm : permissionList) {
				if (perm.getPermissionType().toString().equalsIgnoreCase(permission.replace(" ", "_").toUpperCase())) {
					sendEmailList.add(role.getKey());
						break;
					}	
				}
			}
			sendEmailList.add(userDao.getAdminEmail());
			sendEmailList.add(requestedByEmailId);
		
		String currentDate = DateCreater.defaultCurrentDateTime();
		User checker = new User();
		User maker = new User();
		checker = userDao.findPayIdByEmail(loginEmailId);
		maker = userDao.findPayIdByEmail(requestedByEmailId);
		
		for (String emailId : sendEmailList) {
		
		String subject = "Merchant Mapping Details Update Notification";
		logger.info("processMappingApproveRejectEmail requested by " + requestedByEmailId);

		switch (requestStatus) {

		case "Approved":
			setBody(propertiesManager.getPendingMessages("MerchantMappingUpdateApprove"));
			emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName, 
					maker.getBusinessName(), checker.getBusinessName(), currentDate);
			
			break;

		case "Rejected":
			setBody(propertiesManager.getPendingMessages("MerchantMappingUpdateReject"));
			emailServiceProvider.emailPendingRequest(emailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName, 
					maker.getBusinessName(), checker.getBusinessName(), currentDate);

			break;

			}
		}
		}
		};
		
		propertiesManager.executorImpl(runnable);
		/*
		 * Thread emailSendThread = new Thread(runnable); emailSendThread.start();
		 */
		
	}
	
	public void processMerchantSurchargeRequestEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName ,String merchantPayId) {

		User user = new User();
		user = userDao.findPayIdByEmail(loginEmailId);
		String subject = "Merchant Surcharge Update";
		if (loginUserType.equals(UserType.SUBADMIN.toString())) {
			String parentUser = userDao.getEmailIdByPayId(user.getParentPayId());
			setParentEmailId(parentUser);
		}

		switch (requestStatus) {
		case "Active":
			setBody(propertiesManager.getPendingMessages("MerchantSurchargeUpdated") + " "+merchantName +" with Pay Id "+merchantPayId + " by "+loginEmailId);

			if (loginUserType.equals(UserType.SUBADMIN.toString())) {

				//emailControllerServiceProvider.emailPendingRequest(getParentEmailId(), subject, getBody());
			} else {

			}
			break;

		case "Pending":
			setBody(propertiesManager.getPendingMessages("MerchantSurchargeUpdateRequest") + "  "+merchantName +"  with Pay Id  "+merchantPayId + " by Sub admin "+loginEmailId);
			logger.info("getParentEmailId() " + getParentEmailId() +" subject "+ subject +" getBody() "+ getBody());
			//emailControllerServiceProvider.emailPendingRequest(getParentEmailId(), subject, getBody());

			break;

		}
	}
	
	
	public void processMerchantSurchargeApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName, String merchantPayId, String requestedByEmailId) {


		String subject = "Merchant Surcharge Update Notification";

		switch (requestStatus) {

		case "Approved":
			setBody(propertiesManager.getPendingMessages("MerchantSurchargeUpdateApprove") + "  "+merchantName + "  with payId "
					+ merchantPayId + " by Admin.");
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);
			
			break;

		case "Rejected":
			setBody(propertiesManager.getPendingMessages("MerchantSurchargeUpdateReject") + "  "+merchantName + "  with payId  "
					+ merchantPayId + " by Admin.");
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);

			break;

		}
	}
	
	
	public void processBankSurchargeRequestEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName ,String merchantPayId) {

		User user = new User();
		user = userDao.findPayIdByEmail(loginEmailId);
		String subject = "Bank Surcharge Update";
		if (loginUserType.equals(UserType.SUBADMIN.toString())) {
			String parentUser = userDao.getEmailIdByPayId(user.getParentPayId());
			setParentEmailId(parentUser);
		}

		switch (requestStatus) {
		case "Active":
			setBody(propertiesManager.getPendingMessages("BankSurchargeUpdated") + " "+merchantName +" with Pay Id "+merchantPayId + " by "+loginEmailId);

			if (loginUserType.equals(UserType.SUBADMIN.toString())) {

				//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);
			} else {

			}
			break;

		case "Pending":
			setBody(propertiesManager.getPendingMessages("BankSurchargeUpdateRequest") + "  "+merchantName +"  with Pay Id  "+merchantPayId + " by Sub admin "+loginEmailId);
			logger.info("getParentEmailId() " + getParentEmailId() +" subject "+ subject +" getBody() "+ getBody());
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);

			break;

		}
	}
	
	
	public void processBankSurchargeApproveRejectEmail(String requestStatus, String loginEmailId, String loginUserType,
			String merchantName, String merchantPayId, String requestedByEmailId) {


		String subject = "Bank Surcharge Update Notification";

		switch (requestStatus) {

		case "Approved":
			setBody(propertiesManager.getPendingMessages("BankSurchargeUpdateApprove") + "  "+merchantName + "  with payId "
					+ merchantPayId + " by Admin.");
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);

			break;

		case "Rejected":
			setBody(propertiesManager.getPendingMessages("BankSurchargeUpdateReject") + "  "+merchantName + "  with payId  "
					+ merchantPayId + " by Admin.");
			//emailControllerServiceProvider.emailPendingRequest(requestedByEmailId, subject, getBody(), requestStatus, loginEmailId, loginUserType, merchantName);

			break;

		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAllSubAdminByPermission(String permission) {
		
		permission = permission.replace(" ", "_").toUpperCase();
		List<String> emailIdByPermission = new ArrayList<String>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		try {
			emailIdByPermission = (List<String>) session.createQuery(
					"select User_emailId from user u, user_roles ur, roles_permissions rp, permissions p where p.permission = :permission and" +
					" (p.id = rp.permissions_id and rp.Roles_id = ur.roles_id and ur.User_emailId = u.emailId and u.userStatus = 'Active')")
					.setParameter("permission", permission).getResultList();

			tx.commit();
		} catch (Exception ex) {
			logger.error("exception while get email id by permission " , ex);
		} finally {
			logger.info("finally block");
		}
		
		return emailIdByPermission;
	}

	public String getParentEmailId() {
		return parentEmailId;
	}

	public void setParentEmailId(String parentEmailId) {
		this.parentEmailId = parentEmailId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
