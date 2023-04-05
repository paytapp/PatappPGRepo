package com.paymentgateway.crm.actionBeans;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PendingResellerMappingApproval;
import com.paymentgateway.commons.user.PendingResellerMappingDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Rahul
 *
 */
@Service
public class MerchantMappingFactory {
	
	private static Logger logger = LoggerFactory.getLogger(MerchantMappingFactory.class.getName());
	
	@Autowired
	private PendingResellerMappingDao pendingResellerMappingDao;
	
	@Autowired
	private UserDao userDao;
	
	private String actionMessage;

	public Map<String, List<Merchants>> getMerchantMappingFactory(String merchantEmailId, String reseller,
			String userType, StringBuilder permissions, User sessionUser) {
		
		try {
			
		Date date = new Date();
		Map<String, List<Merchants>> resellerMapList = new HashMap<String, List<Merchants>>();
		User userReseller = userDao.find(reseller);
		// user.getResellerId();
		String[] merchant = merchantEmailId.split(", ");
		for (String emailId : merchant) {

			User user = userDao.find(emailId);
			user.setResellerId(userReseller.getPayId());
			user.setUpdatedBy(sessionUser.getEmailId());
			user.setUpdateDate(date);
			if (userType.equals(UserType.ADMIN.toString())
					|| permissions.toString().contains("Create Reseller Mapping")) {
				//Cancel existing request before force update
				PendingResellerMappingApproval existingPendingRequest = new PendingResellerMappingApproval();
				existingPendingRequest = pendingResellerMappingDao.findExistingMappingRequest(emailId, reseller);
				if (existingPendingRequest != null){
					cancelExistingPendingRequest(existingPendingRequest , TDRStatus.CANCELLED);
				}
				userDao.update(user);
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				PendingResellerMappingApproval request = pendingResellerMappingDao.find(merchantEmailId);
				if (request != null) {
					setActionMessage(CrmFieldConstants.PENDING_REQUEST_EXIST.getValue());
					return resellerMapList;
				}
				String businessName = user.getBusinessName();
				String payId = user.getPayId();
				PendingResellerMappingApproval newPendingResellerMappingApproval = createPendingApprovalFields(
						merchantEmailId, reseller, sessionUser,businessName,payId);
				pendingResellerMappingDao.create(newPendingResellerMappingApproval);
				setActionMessage(CrmFieldConstants.DETAILS_UPDATE_REQUEST.getValue());
			}
		}
		return resellerMapList;
		
		}
		catch(Exception e) {
			
			logger.error("Exception occured in MerchantMappingFactory , getMerchantMappingFactory , exception = " , e);
			return null;
		}
		
	}

	public PendingResellerMappingApproval createPendingApprovalFields(String merchantEmailId, String reseller,
			User sessionUser, String businessName, String payId) {
		
		try {
			
			Date date = new Date();
			PendingResellerMappingApproval prma = new PendingResellerMappingApproval();
			prma.setCreateDate(date);
			prma.setMerchantEmailId(merchantEmailId);
			prma.setRequestedBy(sessionUser.getEmailId());
			prma.setRequestStatus(TDRStatus.PENDING.toString());
			prma.setResellerId(reseller);
			prma.setBusinessName(businessName);
			prma.setMerchantPayId(payId);
			
			return prma;
			
		}
		
		catch(Exception e) {
			logger.error("Exception occured in PendingResellerMappingApproval , exception =  " , e);
			return null;
		}
		
	}
	
	public void cancelExistingPendingRequest(PendingResellerMappingApproval existingPendingRequest, TDRStatus status) {

		try {
			Date currentDate = new Date();
			Session session = null;
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			Long id = existingPendingRequest.getId();
			session.load(existingPendingRequest, existingPendingRequest.getId());
			PendingResellerMappingApproval existingRequest = (PendingResellerMappingApproval) session.get(PendingResellerMappingApproval.class, id);
			existingRequest.setRequestStatus(status.toString());
			existingRequest.setUpdateDate(currentDate);
			//existingRequest.setProcessedBy(emailId);
			session.update(existingRequest);
			tx.commit();
			session.close();

		} catch (HibernateException e) {
			logger.error("Exception occured in cancelExistingPendingRequest , exception = " , e);
		} finally {

		}

	}

	public String getActionMessage() {
		return actionMessage;
	}

	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}

}
