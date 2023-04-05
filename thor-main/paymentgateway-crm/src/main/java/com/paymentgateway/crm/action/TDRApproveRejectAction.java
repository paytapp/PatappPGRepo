package com.paymentgateway.crm.action;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Shaiwal
 *
 */
public class TDRApproveRejectAction extends AbstractSecureAction {

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(TDRApproveRejectAction.class.getName());
	private static final long serialVersionUID = -6517340843571949786L;

	private String idString;
	private String emailId;
	private String userType;
	private String operation;
	private String response;
	private String responseStatus;

	private Date currentDate = new Date();

	public String execute() {

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		userType = sessionUser.getUserType().toString();
		emailId = sessionUser.getEmailId();

		String loginUserEmailId = sessionUser.getEmailId();

		try {
			if (permissions.toString().contains(PermissionType.CREATE_TDR.getPermission())
					|| userType.equals(UserType.ADMIN.toString())) {

				ChargingDetails chargingDetailsToUpdate = new ChargingDetails();
				ChargingDetails activeChargingDetail = new ChargingDetails();

				String[] ids = idString.split(",");

				for (String id : ids) {

					chargingDetailsToUpdate = chargingDetailsDao.find(Long.parseLong(id));

					activeChargingDetail = chargingDetailsDao.findActiveChargingDetail(
							chargingDetailsToUpdate.getMopType(), chargingDetailsToUpdate.getPaymentType(),
							chargingDetailsToUpdate.getTransactionType(), chargingDetailsToUpdate.getAcquirerName(),
							chargingDetailsToUpdate.getCurrency(), chargingDetailsToUpdate.getPayId(),
							chargingDetailsToUpdate.getPaymentsRegion(), chargingDetailsToUpdate.getCardHolderType(),
							chargingDetailsToUpdate.getAcquiringMode(), chargingDetailsToUpdate.getSlabId());

					if (chargingDetailsToUpdate != null) {

						String merchantName = userDao.getBusinessNameByPayId(chargingDetailsToUpdate.getPayId());
						if (operation.equalsIgnoreCase("accept")) {

							if (activeChargingDetail != null) {
								activeChargingDetail.setUpdateBy(loginUserEmailId);
								updateChargingDetails(activeChargingDetail, TDRStatus.INACTIVE);
							}
							chargingDetailsToUpdate.setUpdateBy(loginUserEmailId);
							updateChargingDetails(chargingDetailsToUpdate, TDRStatus.ACTIVE);

							pendingRequestEmailProcessor.processTDRApproveRejectEmail(TDRStatus.ACTIVE.getName(),
									emailId, userType, merchantName, chargingDetailsToUpdate.getPayId(),
									chargingDetailsToUpdate.getRequestedBy(),
									PermissionType.CREATE_TDR.getPermission());

							setResponseStatus("Success");
							setResponse(ErrorType.CHARGING_DETAILS_REQUEST_ACCEPT.getResponseMessage());

						} else {
							chargingDetailsToUpdate.setUpdateBy(loginUserEmailId);

							updateChargingDetails(chargingDetailsToUpdate, TDRStatus.REJECTED);

							pendingRequestEmailProcessor.processTDRApproveRejectEmail(TDRStatus.REJECTED.getName(),
									emailId, userType, merchantName, chargingDetailsToUpdate.getPayId(),
									chargingDetailsToUpdate.getRequestedBy(),
									PermissionType.CREATE_TDR.getPermission());
							setResponseStatus("Success");
							setResponse(ErrorType.CHARGING_DETAILS_REQUEST_REJECT.getResponseMessage());
						}

					}

				}
			}

			return SUCCESS;
		}

		catch (Exception e) {
			logger.error("Exception " , e);
			setResponseStatus("Failed");
			setResponse(ErrorType.CHARGINGDETAIL_NOT_SAVED.getResponseMessage());
			return ERROR;
		}
	}

	public void validate() {
		// if ((validator.validateBlankField(getUserType()))) {
		// addFieldError(CrmFieldType.USER_TYPE.getName(),
		// validator.getResonseObject().getResponseMessage());
		// } else if (!(validator.validateField(CrmFieldType.USER_TYPE,
		// getUserType()))) {
		// addFieldError(CrmFieldType.USER_TYPE.getName(),
		// validator.getResonseObject().getResponseMessage());
		// }
		// if ((validator.validateBlankField(getEmailId()))) {
		// addFieldError(CrmFieldType.EMAILID.getName(),
		// validator.getResonseObject().getResponseMessage());
		// } else if (!(validator.validateField(CrmFieldType.EMAILID,
		// getEmailId()))) {
		// addFieldError(CrmFieldType.EMAILID.getName(),
		// validator.getResonseObject().getResponseMessage());
		// }
		if ((validator.validateBlankField(getOperation()))) {
			addFieldError(CrmFieldType.OPERATION.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.OPERATION, getOperation()))) {
			addFieldError(CrmFieldType.OPERATION.getName(), validator.getResonseObject().getResponseMessage());
		}

	}

	public void updateChargingDetails(ChargingDetails chargingDetails, TDRStatus status) {

		try {

			Session session = null;
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			// Long id = chargingDetails.getId();
			// session.load(chargingDetails, chargingDetails.getId());
			// ChargingDetails cd = (ChargingDetails)
			// session.get(ChargingDetails.class, id);
			chargingDetails.setStatus(status);
			chargingDetails.setUpdatedDate(currentDate);
			session.update(chargingDetails);
			tx.commit();
			session.close();

		} catch (HibernateException e) {
			logger.error("Exception " , e);
		} finally {

		}
	}


	public String getIdString() {
		return idString;
	}

	public void setIdString(String idString) {
		this.idString = idString;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}
	
}
