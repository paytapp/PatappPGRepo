package com.paymentgateway.pg.core.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentTypeProvider;
import com.paymentgateway.commons.util.PaymentTypeTransactionProvider;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;

public class AuthenticatorImpl implements Authenticator {

	private static Logger logger = LoggerFactory.getLogger(AuthenticatorImpl.class.getName());
	//private static Map<String, User> userMap = new HashMap<String, User>();

	@Autowired
	@Qualifier("paymentTypeProvider")
	private PaymentTypeProvider paymentTypeProvider;
	
	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;
	
	public AuthenticatorImpl() {
	}

	public ErrorType checkLogin(String userId, String password) {
		ErrorType errorType = ErrorType.UNKNOWN;
		return errorType;
	}

	public User getUserFromPayId(Fields fields) throws SystemException {
		
		User user = null;
		
		if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
			user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));

		} else {
			user = new UserDao().findPayId(fields.get(FieldType.PAY_ID.getName()));

		}
		
		return user;
	}

	public void authenticate(Fields fields) throws SystemException {

		String payId = fields.get(FieldType.PAY_ID.getName());
		User user_ = getUser(fields);

		// Check if user is found
		if (null == user_) {
			throw new SystemException(ErrorType.USER_NOT_FOUND, "No such user, PayId=" + payId);
		}

		// Check transaction status of user
		UserStatusType userStatus = user_.getUserStatus();

		// Add Reseller Id to fields

		if (StringUtils.isNotBlank(user_.getResellerId())) {
			fields.put(FieldType.RESELLER_ID.getName(), user_.getResellerId());
		}

		if (userStatus.equals(UserStatusType.PENDING) || userStatus.equals(UserStatusType.TRANSACTION_BLOCKED)
				|| userStatus.equals(UserStatusType.TERMINATED) || userStatus.equals(UserStatusType.APPROVED)
				|| userStatus.equals(UserStatusType.REJECTED)) {
			if (!(fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.STATUS.getName()))) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				logger.error("Merchant is not active with payId = " + payId);
				throw new SystemException(ErrorType.PERMISSION_DENIED, "User not allowed to transact, PayId=" + payId);
			}
		}
		String merchantHostedFlag = fields.get(FieldType.IS_MERCHANT_HOSTED.getName());

		UserSettingData userSetting = getUserSettings(fields);

		if (!StringUtils.isBlank(merchantHostedFlag) && merchantHostedFlag.equals(Constants.Y)) {
			if (!userSetting.isMerchantHostedFlag()) {
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				logger.error("Merchant not allowed to perform direct transactoin payId=  " + payId);
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED,
						"Merchant not allowed to perform direct transactoin payId= " + payId);
			}
		}
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.NEWORDER.getName())) {
			validateCurrency(fields);
		}

	}

	public void validatePaymentOptions(Fields fields) throws SystemException {

		User user = getUser(fields);
		PaymentTypeTransactionProvider paymentTypeTransactionProvider = paymentTypeProvider
				.setSupportedPaymentOptions((user.getPayId()));
		List<ChargingDetails> chargingDetailsList = paymentTypeTransactionProvider.getChargingDetailsList();
		List<ChargingDetails> supportedChargingDetailsList = new ArrayList<ChargingDetails>();

		String paymentTypeCode = fields.get(FieldType.PAYMENT_TYPE.getName());
		String mopTypeCode = fields.get(FieldType.MOP_TYPE.getName());

		for (ChargingDetails chargingDetails : chargingDetailsList) {
			if (chargingDetails.getPaymentType().getCode().equals(paymentTypeCode)
					&& chargingDetails.getMopType().getCode().equals(mopTypeCode)) {
				supportedChargingDetailsList.add(chargingDetails);
			}
		}
		if (supportedChargingDetailsList.isEmpty()) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			logger.error("Merchant not supported for this transactoin type payId= "
					+ fields.get(FieldType.PAY_ID.getName()));
			throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED,
					"Merchant not supported for this transactoin type payId= "
							+ fields.get(FieldType.PAY_ID.getName()));
		}
		// setSupportedChargingDetailsList(supportedChargingDetailsList);
	}

	public void validateCurrency(Fields fields) throws SystemException {

		User user = getUser(fields);
		Set<Account> accountList  = new HashSet<Account>();
		StringBuilder accCurrencySet = new StringBuilder();
		
		accountList = user.getAccounts();
		
		for (Account acount : accountList) {
			
			Set<AccountCurrency> accountCurrencyList  = new HashSet<AccountCurrency>();
			accountCurrencyList = acount.getAccountCurrencySet();
			
			for (AccountCurrency accountCurrency : accountCurrencyList) {
				accCurrencySet.append(accountCurrency.getCurrencyCode());
				accCurrencySet.append(",");
			}
		}
		
		//PendingMappingRequestDao pendingMappingRequestDao = new PendingMappingRequestDao();
		//String accountcurrencyset = pendingMappingRequestDao.findActiveMappingByEmailId(user.getEmailId());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

		if (!(accCurrencySet.toString().contains(currencyCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			logger.error("This currency not mapped with merchant, currnecy code = " + currencyCode);
			throw new SystemException(ErrorType.CURRENCY_NOT_SUPPORTED,
					"Merchant not supported for this currency type payId= " + fields.get(FieldType.PAY_ID.getName()));
		}

	}

	public void isUserExists(Fields fields) throws SystemException {
		String payId = fields.get(FieldType.PAY_ID.getName());
		User user_ = getUser(fields);

		// if user is found
		if (null == user_) {
			logger.error("No such user found with PayId = " + payId);
			throw new SystemException(ErrorType.USER_NOT_FOUND, "No such user, PayId=" + payId);

		}
	}

	public User getUser(Fields fields) {
		User user = null;

		// Decide whether to use static usermap or get data from DAO
		if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
			user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));

		} else {
			user = new UserDao().findPayId(fields.get(FieldType.PAY_ID.getName()));

		}

		return user;
	}
	
	public UserSettingData getUserSettings(Fields fields) {
		
		return userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		
	}

	
	  public void setUser(User user) {  }
	 

}
