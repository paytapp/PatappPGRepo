package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.crm.actionBeans.LoginAuthenticator;

/**
 * @author Rahul
 *
 */
public class RouterConfigurationEditAction extends AbstractSecureAction {

	private static final long serialVersionUID = -127596067586594948L;
	private static Logger logger = LoggerFactory.getLogger(RouterConfigurationEditAction.class.getName());

	// @Autowired
	// private RouterRuleDao routerRuleDao;

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	/*@Autowired
	private SurchargeDao surchargeDao;*/
	
	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private LoginAuthenticator loginAuthenticator;

	// private List<RouterRule> listData;
	private String response;

	private String routerConfig;
	private String identifier;
	private String mode;
	private String onOffName;

	public String execute() {

		try {
			
			Object userObject = sessionMap.get(Constants.USER.getValue());
			User user = (User) userObject;
			
			String sessionMerchantEmailId=user.getEmailId();

			logger.info("Inside RouterConfigurationEditAction execute() ");
			
			Boolean pendingRequest = false;

			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			String identifierArray[] = getIdentifier().split("-");

			String paymentType = identifierArray[0];
			String mopType = identifierArray[1];
			String payId = identifierArray[2];
			String transactionType = identifierArray[3];
			String currency = identifierArray[4];
			String paymentsRegion = identifierArray[5];
			String cardHolderType = identifierArray[6];
			String slabId = identifierArray[7];
			String onOffVal = identifierArray[8];

			User merchant = userDao.findPayId(payId);
			
			setOnOffName(onOffVal);

			AccountCurrencyRegion acr;

			if (paymentsRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
				acr = AccountCurrencyRegion.DOMESTIC;
			} else {
				acr = AccountCurrencyRegion.INTERNATIONAL;
			}

			CardHolderType act;

			if (cardHolderType.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
				act = CardHolderType.COMMERCIAL;
			} else if (cardHolderType.equalsIgnoreCase(CardHolderType.PREMIUM.toString())) {
				act = CardHolderType.PREMIUM;
			} else {
				act = CardHolderType.CONSUMER;
			}

			String updatedIdentifier = payId + currency + paymentType + mopType + transactionType + paymentsRegion
					+ cardHolderType + slabId;

			if (getMode().equalsIgnoreCase("AUTO")) {

				deleteRouterRuleConfiguration(updatedIdentifier,user);
				// addRouterRuleConfiguration(slabId);
				return SUCCESS;
			}

			else {

				String routerConfigSet[] = getRouterConfig().split(";");

				for (String routerConfigBlock : routerConfigSet) {
					
					List<RouterConfiguration> newRcList = new ArrayList<RouterConfiguration>();

					String routerConfigBlockSet[] = routerConfigBlock.split(",");

					String acquirer = routerConfigBlockSet[0];
					String status = routerConfigBlockSet[1];
					String description = routerConfigBlockSet[2];
					String allowedFailureCount = routerConfigBlockSet[6];
					String alwaysOn = routerConfigBlockSet[7];
					String loadPercentage = routerConfigBlockSet[8];
					String priority = routerConfigBlockSet[9];
					String retryMinutes = routerConfigBlockSet[10];
					String onOff = routerConfigBlockSet[13];

					// Find Router Config from DB
					RouterConfiguration routerConfigFromDB = routerConfigurationDao
							.findActiveRulesByIdentifier(updatedIdentifier, onOff, acquirer);

					boolean isCurrentlyActive = false;
					boolean isAlwaysOn = false;

					if (status.equals("true")) {

						isCurrentlyActive = true;
						logger.info("Acquire currently active in manual mode == " + acquirer);
					}

					if (alwaysOn.equals("true")) {

						isAlwaysOn = true;
					}

					// if (routerConfigFromDB != null) {
					RouterConfiguration routerConfigurationToSave = routerConfigFromDB;

					if (permissions.toString().contains(PermissionType.SMART_ROUTER.getPermission())
							|| user.getUserType().equals(UserType.ADMIN)) {

						routerConfigurationToSave = createNewRouterConfiguration(routerConfigurationToSave,
								updatedIdentifier, allowedFailureCount, description, isCurrentlyActive, loadPercentage,
								priority, isAlwaysOn, retryMinutes, allowedFailureCount, TDRStatus.ACTIVE, user);

						newRcList.add(routerConfigurationToSave);

						deleteRouterRuleConfigurationByAcquirer(updatedIdentifier, acquirer, user);
						for (RouterConfiguration rc : newRcList) {
							routerConfigurationDao.create(rc);
						}
						setResponse(ErrorType.ROUTER_CONFIGURATION_SAVE.getResponseMessage());

					} else {
						
						if (checkPendingRequestForRouterConfigurationByIdentifier(updatedIdentifier, onOff, acquirer, TDRStatus.PENDING.getName())) {

							setResponse(ErrorType.ROUTER_CONFIGURATION_REQUEST_ALREADY_PENDING.getResponseMessage());
							return SUCCESS;
						}

						pendingRequest = true;
						routerConfigurationToSave = createNewRouterConfiguration(routerConfigurationToSave,
								updatedIdentifier, allowedFailureCount, description, isCurrentlyActive, loadPercentage,
								priority, isAlwaysOn, retryMinutes, allowedFailureCount, TDRStatus.PENDING, user);

						routerConfigurationDao.createForPendingRequest(routerConfigurationToSave);
						
						pendingRequestEmailProcessor.processRouterConfigEmail("Pending", user.getEmailId(), user.getUserType().toString(), merchant.getEmailId(),
								PermissionType.SMART_ROUTER.getPermission());
						
						setResponse(ErrorType.ROUTER_CONFIGURATION_REQUEST_SENT_FOR_APPROVAL.getResponseMessage());

					}
				}
				if (pendingRequest == true) {
				pendingRequestEmailProcessor.processRouterConfigEmail("Pending", user.getEmailId(), user.getUserType().toString(), merchant.getEmailId(),
						PermissionType.SMART_ROUTER.getPermission());
				}
				return SUCCESS;
			}
		} catch (Exception e) {

			logger.error("Unable to start auto mode in smart router " , e);
			return ERROR;
		}
	}

	private RouterConfiguration createNewRouterConfiguration(RouterConfiguration routerConfigurationToSave,
			String updatedIdentifier, String allowedFailureCount, String description, boolean isCurrentlyActive,
			String loadPercentage, String priority, boolean isAlwaysOn, String retryMinutes,
			String allowedFailureCount2, TDRStatus status, User user) {

		Date date = new Date();

		routerConfigurationToSave.setMode("Manual");
		routerConfigurationToSave.setIdentifier(updatedIdentifier);
		routerConfigurationToSave.setStatus(status);
		routerConfigurationToSave.setAllowedFailureCount(Integer.valueOf(allowedFailureCount));
		routerConfigurationToSave.setCreatedDate(date);
		routerConfigurationToSave.setUpdatedDate(date);
		routerConfigurationToSave.setStatusName(description);
		routerConfigurationToSave.setCurrentlyActive(isCurrentlyActive);
		if (isCurrentlyActive) {
			routerConfigurationToSave.setStatusName("On");
		}
		if (!isCurrentlyActive && !description.equalsIgnoreCase("StandBy")) {
			routerConfigurationToSave.setStatusName("Off");
		}
		routerConfigurationToSave.setLoadPercentage(Integer.valueOf(loadPercentage));
		routerConfigurationToSave.setPriority(priority);
		routerConfigurationToSave.setAlwaysOn(isAlwaysOn);
		routerConfigurationToSave.setRetryMinutes(retryMinutes);
		routerConfigurationToSave.setAllowedFailureCount(Integer.valueOf(allowedFailureCount));
		
		if (status.equals(TDRStatus.PENDING)) {
			routerConfigurationToSave.setRequestedBy(user.getEmailId());
		} else if (status.equals(TDRStatus.ACTIVE)) {
			routerConfigurationToSave.setRequestedBy(user.getEmailId());
		    routerConfigurationToSave.setUpdatedBy(user.getEmailId());
		}
		return routerConfigurationToSave;

	}

	private Boolean checkPendingRequestForRouterConfigurationByIdentifier(String identifier, String onOff, String acquirerName, String status) {

		logger.info("check for pending request for router configuration");
		RouterConfiguration routerConfig = routerConfigurationDao.pendingRequestForIdentifier(identifier, onOff, acquirerName, status);
		if(routerConfig != null) {
			return true;
		} else {
			return false;
		}
	}

	public void deleteRouterRuleConfiguration(String identifier) {

		try {

			logger.info("Remove existing  RouterConfiguration");
			routerConfigurationDao.deleteUsingIdentifier(identifier, getOnOffName());

		}

		catch (Exception e) {
			logger.error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   " , e);
		}

	}
	
	public void deleteRouterRuleConfigurationByAcquirer(String identifier, String acquirer) {

		try {

			logger.info("Remove existing  RouterConfiguration");
			routerConfigurationDao.deleteUsingIdentifier(identifier, getOnOffName(), acquirer);

		}

		catch (Exception e) {
			logger.error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   " , e);
		}

	}
	public void deleteRouterRuleConfigurationByAcquirer(String identifier, String acquirer, User user) {

		try {

			logger.info("Remove existing  RouterConfiguration");
			routerConfigurationDao.deleteUsingIdentifier(identifier, getOnOffName(), acquirer, user.getEmailId());

		}

		catch (Exception e) {
			logger.error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   " , e);
		}

	}
	
	

	public void deleteRouterRuleConfiguration(String identifier, String onOffName) {

		try {

			logger.info("Remove existing  RouterConfiguration");
			List<RouterConfiguration> routerConfigurationList = new ArrayList<RouterConfiguration>();

			routerConfigurationList = routerConfigurationDao.findRulesByIdentifier(identifier);

			if (routerConfigurationList.size() > 0) {

				for (RouterConfiguration routerConfiguration : routerConfigurationList) {
					routerConfigurationDao.delete(routerConfiguration.getId());
				}

			}

		}

		catch (Exception e) {
			logger.error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   " , e);
		}

	}
	
	public void deleteRouterRuleConfiguration(String identifier, User sessionUser) {

		try {

			logger.info("Remove existing  RouterConfiguration");
			List<RouterConfiguration> routerConfigurationList = new ArrayList<RouterConfiguration>();

			routerConfigurationList = routerConfigurationDao.findRulesByIdentifier(identifier);

			if (routerConfigurationList.size() > 0) {

				for (RouterConfiguration routerConfiguration : routerConfigurationList) {
					routerConfigurationDao.delete(routerConfiguration.getId(),sessionUser.getEmailId());
				}

			}

		}

		catch (Exception e) {
			logger.error("Exception occured in RouterRuleEditAction , cannot delete RouterRuleConfiguration   " , e);
		}

	}

	/*
	 * public void addRouterRuleConfiguration( String slabId) {
	 * 
	 * List<RouterConfiguration> routerConfigurationList = new
	 * ArrayList<RouterConfiguration>();
	 * 
	 * try {
	 * 
	 * String[] acquirerMap = routerRule.getAcquirerMap().split(","); String
	 * identifier = routerRule.getMerchant() + routerRule.getCurrency() +
	 * routerRule.getPaymentType() + routerRule.getMopType() +
	 * routerRule.getTransactionType()+routerRule.getPaymentsRegion()
	 * +routerRule.getCardHolderType()+slabId;
	 * 
	 * String slabAmountArrayString =
	 * PropertiesManager.propertiesMap.get(Constants.SWITCH_ACQUIRER_AMOUNT.
	 * getValue ());
	 * 
	 * // If no slab is set for this merchant , create a default slab with 00 as
	 * ID and limit from 0.01 to 1000000.00 if
	 * (!slabAmountArrayString.contains(routerRule.getMerchant())) {
	 * slabAmountArrayString =
	 * "00-0.01-1000000.00-"+routerRule.getMerchant()+"-ALL"; }
	 * 
	 * String[] slabAmountArray = slabAmountArrayString.split(",");
	 * 
	 * String minAmount = ""; String maxAmount = "";
	 * 
	 * for (String currentSlab : slabAmountArray) {
	 * 
	 * if (!currentSlab.contains(routerRule.getMerchant())) { continue; }
	 * 
	 * String[] slabSplit = currentSlab.split("-"); String paymentType = "ALL";
	 * String[] slabArray = currentSlab.split("-");
	 * 
	 * if (!StringUtils.isBlank(slabSplit[4])) { paymentType = slabSplit[4]; }
	 * 
	 * if (!paymentType.equalsIgnoreCase(routerRule.getPaymentType())) {
	 * 
	 * slabId = "00"; minAmount = "0.01"; maxAmount = "1000000.00";
	 * 
	 * }
	 * 
	 * if (slabId.equalsIgnoreCase(slabArray[0])) {
	 * 
	 * minAmount = slabArray[1]; maxAmount = slabArray[2]; } else { continue; }
	 * }
	 * 
	 * for (String acquirerString : acquirerMap) {
	 * 
	 * String[] acquirerMapString = acquirerString.split("-");
	 * 
	 * RouterConfiguration routerConfiguration = new RouterConfiguration(); Date
	 * date = new Date();
	 * 
	 * routerConfiguration.setIdentifier(identifier);
	 * routerConfiguration.setAcquirer(acquirerMapString[1]);
	 * routerConfiguration.setCurrency(routerRule.getCurrency());
	 * routerConfiguration.setPaymentType(routerRule.getPaymentType());
	 * routerConfiguration.setMopType(routerRule.getMopType());
	 * routerConfiguration.setTransactionType(routerRule.getTransactionType());
	 * routerConfiguration.setMode(getMode());
	 * routerConfiguration.setStatus(TDRStatus.ACTIVE);
	 * routerConfiguration.setAllowedFailureCount(5);
	 * routerConfiguration.setCreatedDate(date);
	 * routerConfiguration.setUpdatedDate(date);
	 * routerConfiguration.setMerchant(routerRule.getMerchant());
	 * routerConfiguration.setOnUsoffUs(routerRule.isOnUsFlag());
	 * routerConfiguration.setRetryMinutes("10");
	 * routerConfiguration.setRulePriority(acquirerMapString[0]);
	 * routerConfiguration.setFailureCount(0);
	 * routerConfiguration.setPaymentsRegion(routerRule.getPaymentsRegion());
	 * routerConfiguration.setCardHolderType(routerRule.getCardHolderType());
	 * routerConfiguration.setMinAmount(Double.valueOf(minAmount));
	 * routerConfiguration.setMaxAmount(Double.valueOf(maxAmount));
	 * routerConfiguration.setSlabId(slabId);
	 * 
	 * String surcharge =
	 * surchargeDao.findDetailsByRouterConfiguration(routerConfiguration);
	 * 
	 * if (surcharge.equalsIgnoreCase("NA")) { continue; }
	 * routerConfiguration.setSurcharge(surcharge);
	 * routerConfigurationList.add(routerConfiguration);
	 * 
	 * }
	 * 
	 * if (routerRule.getMerchant().equalsIgnoreCase("ALL MERCHANTS")) {
	 * 
	 * int count = 1;
	 * 
	 * for (RouterConfiguration routerConfiguration : routerConfigurationList) {
	 * 
	 * RouterConfiguration routerConfigurationToSave = new
	 * RouterConfiguration(); routerConfigurationToSave = routerConfiguration;
	 * 
	 * if (count == 1) { routerConfigurationToSave.setCurrentlyActive(true);
	 * routerConfigurationToSave.setLoadPercentage(100);
	 * 
	 * logger.
	 * info("Inside RouterConfigurationEditAction , currently active acquirer is  "
	 * +routerConfigurationToSave.getAcquirer()); }
	 * 
	 * else { routerConfigurationToSave.setCurrentlyActive(false);
	 * routerConfigurationToSave.setLoadPercentage(0); }
	 * 
	 * routerConfigurationToSave.setPriority(String.valueOf(count));
	 * routerConfigurationDao.create(routerConfigurationToSave); count++; }
	 * 
	 * }
	 * 
	 * else {
	 * 
	 * Comparator<RouterConfiguration> comp = (RouterConfiguration a,
	 * RouterConfiguration b) -> {
	 * 
	 * if (Double.valueOf(b.getSurcharge()) > Double.valueOf(a.getSurcharge()))
	 * { return -1; } else if (Double.valueOf(b.getSurcharge()) <
	 * Double.valueOf(a.getSurcharge())) { return 1; } else { if
	 * (Double.valueOf(b.getRulePriority()) >
	 * Double.valueOf(a.getRulePriority())) { return -1; } else { return 1; } }
	 * };
	 * 
	 * Collections.sort(routerConfigurationList, comp);
	 * 
	 * int count = 1;
	 * 
	 * for (RouterConfiguration entry : routerConfigurationList) {
	 * 
	 * RouterConfiguration routerConfigurationToSave = new
	 * RouterConfiguration(); routerConfigurationToSave = entry;
	 * 
	 * routerConfigurationToSave.setPriority(String.valueOf(count)); if (count
	 * == 1) { routerConfigurationToSave.setLoadPercentage(100);
	 * routerConfigurationToSave.setCurrentlyActive(true); }
	 * 
	 * else { routerConfigurationToSave.setLoadPercentage(0);
	 * routerConfigurationToSave.setCurrentlyActive(false); }
	 * routerConfigurationDao.create(routerConfigurationToSave); count++; }
	 * 
	 * }
	 * 
	 * } catch (Exception e) {
	 * logger.error("Error occured wile adding Router Configuration " + e); }
	 * 
	 * }
	 */

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getRouterConfig() {
		return routerConfig;
	}

	public void setRouterConfig(String routerConfig) {
		this.routerConfig = routerConfig;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getOnOffName() {
		return onOffName;
	}

	public void setOnOffName(String onOffName) {
		this.onOffName = onOffName;
	}

}
