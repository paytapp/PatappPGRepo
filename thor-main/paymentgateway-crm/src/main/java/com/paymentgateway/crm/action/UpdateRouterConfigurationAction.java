package com.paymentgateway.crm.action;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Rajit
 *
 */
public class UpdateRouterConfigurationAction extends AbstractSecureAction {

	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;
	
	@Autowired
	private RouterConfigurationDao routerConfigurationDao;
	
	private static final long serialVersionUID = -5778378721052569122L;

	private static Logger logger = LoggerFactory.getLogger(UpdateRouterConfigurationAction.class.getName());
	
	private String response;
	private String acquirerName;
	private String allowedFailureCount;
	private String alwaysOn;
	private String identifier;
	private String loadPercentage;
	private String maxAmount;
	private String minAmount;
	private String mode;
	private String onUsOffUsName;
	private String paymentType;
	private String priority;
	private String retryMinutes;
	private String status;
	private String transactionType;
	private String updatedDate;
	private String mopType;
	private String statusName;
	private String currentlyActive;
	private String rowCount;
	private String operation;
	private String merchant;
	private String requestedBy;

	public String execute() {
		
		String acquirerNameArr[] = acquirerName.split(",");
		String allowedFailureCountArr[] = allowedFailureCount.split(",");
		String alwaysOnArr[] = alwaysOn.split(",");
		String identifierArr[] = identifier.split(",");
		String loadPercentageArr[] = loadPercentage.split(",");
		String maxAmountArr[] = maxAmount.split(",");
		String minAmountArr[] = minAmount.split(",");
		String modeArr[] = mode.split(",");
		String onUsOffUsNameArr[] = onUsOffUsName.split(",");
		String paymentTypeArr[] = paymentType.split(",");
		
		String priorityArr[] = priority.split(",");
		//String requestedByArr[] = requestedBy.split(",");
		String retryMinutesArr[] = retryMinutes.split(",");
		String mopTypeArr[] = mopType.split(",");
		String statusNameArr[] = statusName.split(",");
		String currentlyActiveArr[] = currentlyActive.split(",");
		String merchantArr[] = merchant.split(",");
		
		Date date = new Date();
		String currentDate = DateCreater.formatDateForDb(date);
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		Object userObject = sessionMap.get(Constants.USER.getValue());
		User sessionUser = (User) userObject;
		
		int totalCount = Integer.parseInt(rowCount);
		int count = 0;
		String merchantPayId = identifierArr[count].substring(0, 16);
		try {
			if (permissions.toString().contains(PermissionType.SMART_ROUTER.getPermission())
					|| sessionUser.getUserType().equals(UserType.ADMIN)) {

				if (operation.equalsIgnoreCase("Reject")) {
					while (count < totalCount) {
						routerConfigurationDao.statusUpdateRouterConfigForPendingRequest(identifierArr[count],
								onUsOffUsNameArr[count], acquirerNameArr[count], currentDate, sessionUser.getEmailId(),
								operation);
						
						count++;
					}
					
					requestedBy = routerConfigurationDao.getRequestedByEmailIdFromPendingRequest(identifierArr[0],
							onUsOffUsNameArr[0], acquirerNameArr[0]);
					
					pendingRequestEmailProcessor.processRouterConfigApproveRejectEmail("Rejected", sessionUser.getEmailId(), sessionUser.getUserType().toString(),
							merchantArr[0], merchantPayId, requestedBy, "");
					
					setResponse(ErrorType.ROUTER_CONFIGURATION_REQUEST_REJECT.getResponseMessage());
					return SUCCESS;

				} else if (operation.equalsIgnoreCase("Accept")) {

					while (count < totalCount) {

						RouterConfiguration activeRouterConfig = routerConfigurationDao.findActiveRulesByIdentifier(
								identifierArr[count], onUsOffUsNameArr[count], acquirerNameArr[count]);
						String requestedByEmail=activeRouterConfig.getRequestedBy();

						activeRouterConfig.setAcquirer(acquirerNameArr[count]);
						activeRouterConfig.setStatus(TDRStatus.ACTIVE);
						activeRouterConfig.setMode(modeArr[count]);
						activeRouterConfig.setPaymentType(paymentTypeArr[count]);
						activeRouterConfig.setMopType(mopTypeArr[count]);
						activeRouterConfig.setAllowedFailureCount(Integer.parseInt(allowedFailureCountArr[count]));
						activeRouterConfig.setAlwaysOn(Boolean.parseBoolean(alwaysOnArr[count]));
						activeRouterConfig.setLoadPercentage(Integer.parseInt(loadPercentageArr[count]));
						activeRouterConfig.setPriority(priorityArr[count]);
						activeRouterConfig.setRetryMinutes(retryMinutesArr[count]);
						activeRouterConfig.setMinAmount(Double.parseDouble(minAmountArr[count]));
						activeRouterConfig.setMaxAmount(Double.parseDouble(maxAmountArr[count]));
						activeRouterConfig.setOnUsoffUsName(onUsOffUsNameArr[count]);
						activeRouterConfig.setUpdatedBy(sessionUser.getEmailId());
						activeRouterConfig.setStatusName(statusNameArr[count]);
						activeRouterConfig.setCurrentlyActive(Boolean.parseBoolean(currentlyActiveArr[count]));

						routerConfigurationDao.delete(activeRouterConfig.getId(), sessionUser);
						routerConfigurationDao.create(activeRouterConfig);

						routerConfigurationDao.statusUpdateRouterConfigForPendingRequest(identifierArr[count],
								onUsOffUsNameArr[count], acquirerNameArr[count], currentDate, sessionUser.getEmailId(),
								operation);
						
						requestedBy = routerConfigurationDao.getRequestedByEmailIdFromPendingRequest(identifierArr[count],
								onUsOffUsNameArr[count], acquirerNameArr[count]);
						count++;
					}
					
					pendingRequestEmailProcessor.processRouterConfigApproveRejectEmail("Active", sessionUser.getEmailId(), sessionUser.getUserType().toString(),
							merchantArr[0], merchantPayId, requestedBy, PermissionType.SMART_ROUTER.getPermission());
					setResponse(ErrorType.ROUTER_CONFIGURATION_REQUEST_ACCEPT.getResponseMessage());
					return SUCCESS;
				}
			}
		} catch(Exception ex) {
			logger.info("caught exception in updateRouterConfigurationAction while accept or reject pending request");
		}
		setResponse(ErrorType.USER_NOT_FOUND.getResponseMessage());
		return SUCCESS;
	}


	public String getResponse() {
		return response;
	}


	public void setResponse(String response) {
		this.response = response;
	}


	public String getAcquirerName() {
		return acquirerName;
	}


	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}


	public String getAllowedFailureCount() {
		return allowedFailureCount;
	}


	public void setAllowedFailureCount(String allowedFailureCount) {
		this.allowedFailureCount = allowedFailureCount;
	}


	public String getAlwaysOn() {
		return alwaysOn;
	}


	public void setAlwaysOn(String alwaysOn) {
		this.alwaysOn = alwaysOn;
	}


	public String getIdentifier() {
		return identifier;
	}


	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	public String getLoadPercentage() {
		return loadPercentage;
	}


	public void setLoadPercentage(String loadPercentage) {
		this.loadPercentage = loadPercentage;
	}


	public String getMaxAmount() {
		return maxAmount;
	}


	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}


	public String getMinAmount() {
		return minAmount;
	}


	public void setMinAmount(String minAmount) {
		this.minAmount = minAmount;
	}


	public String getMode() {
		return mode;
	}


	public void setMode(String mode) {
		this.mode = mode;
	}


	public String getOnUsOffUsName() {
		return onUsOffUsName;
	}


	public void setOnUsOffUsName(String onUsOffUsName) {
		this.onUsOffUsName = onUsOffUsName;
	}


	public String getPaymentType() {
		return paymentType;
	}


	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}


	public String getPriority() {
		return priority;
	}


	public void setPriority(String priority) {
		this.priority = priority;
	}


	public String getRetryMinutes() {
		return retryMinutes;
	}


	public void setRetryMinutes(String retryMinutes) {
		this.retryMinutes = retryMinutes;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getTransactionType() {
		return transactionType;
	}


	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}


	public String getUpdatedDate() {
		return updatedDate;
	}


	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}


	public String getMopType() {
		return mopType;
	}


	public void setMopType(String mopType) {
		this.mopType = mopType;
	}


	public String getStatusName() {
		return statusName;
	}


	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}


	public String getCurrentlyActive() {
		return currentlyActive;
	}


	public void setCurrentlyActive(String currentlyActive) {
		this.currentlyActive = currentlyActive;
	}


	public String getRowCount() {
		return rowCount;
	}


	public void setRowCount(String rowCount) {
		this.rowCount = rowCount;
	}


	public String getOperation() {
		return operation;
	}


	public void setOperation(String operation) {
		this.operation = operation;
	}


	public String getMerchant() {
		return merchant;
	}


	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}


	public String getRequestedBy() {
		return requestedBy;
	}


	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

}