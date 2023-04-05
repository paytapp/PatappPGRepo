package com.paymentgateway.crm.actionBeans;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;

@Service
public class MerchantLogUpdater {

	@Autowired
	EncryptDecryptService encryptDecryptService;

	private static Logger logger = LoggerFactory.getLogger(MerchantLogUpdater.class.getName());

	public void updateValue(User userFE, User adminUser, List<Account> newAccountsFE,
			List<AccountCurrency> accountCurrencyListFE, User userDB) {

		boolean userStatusChangedFlag = false;
		boolean modeTypeChangedFlag = false;
		boolean txnKeyChangedFlag = false;
		boolean currencyCodeChangedFlag = false;
		boolean merchantIdChangedFlag = false;
		boolean passwordChangedFlag = false;

		StringBuilder logString = new StringBuilder();

		if (!(userDB.getUserStatus().equals(userFE.getUserStatus()))) {
			userStatusChangedFlag = true;

			logString.append(" Merchant status is changed from:");
			logString.append(userDB.getUserStatus());
			logString.append(" to ");
			logString.append(userFE.getUserStatus());

		}
		if (userDB.getModeType() == null) {
			modeTypeChangedFlag = true;

			logString.append(" merchant  mode type is changed from:");
			logString.append("null");
			logString.append(" to ");
			logString.append(userFE.getModeType());

		} else if (!(userDB.getModeType().equals(userFE.getModeType()))) {
			modeTypeChangedFlag = true;

			logString.append(" merchant  mode type is changed from:");
			logString.append(userDB.getModeType());
			logString.append(" to ");
			logString.append(userFE.getModeType());
		}
		if (userStatusChangedFlag == true || modeTypeChangedFlag == true) {
			logger.info("Admin name is: " + adminUser.getFirstName() + " and Email id is:  " + adminUser.getEmailId()
					+ ". Merchant Emailid is: " + userFE.getEmailId() + logString.toString());
		}

		// get Accquirer name from FE
		Iterator<Account> accountFEIterator = newAccountsFE.iterator();

		StringBuilder logAccountString = new StringBuilder();

		while (accountFEIterator.hasNext()) {
			Account newAccontAcquirerFE = accountFEIterator.next();
			// get old data from db
			Set<Account> accountSetDB = userDB.getAccounts();
			Iterator<Account> accountDBIterator = accountSetDB.iterator();

			// get accountcurrency db

			while (accountDBIterator.hasNext()) {
				Account account = accountDBIterator.next();
				if ((newAccontAcquirerFE.getAcquirerName().equalsIgnoreCase(account.getAcquirerName()))) {

					// get accountCurrency from fe
					Iterator<AccountCurrency> accountCurrencyFEIterator = accountCurrencyListFE.iterator();
					while (accountCurrencyFEIterator.hasNext()) {
						AccountCurrency accountCurrencyFE = accountCurrencyFEIterator.next();

						// get accountcurrency from db
						Set<AccountCurrency> accountCurrencySet = account.getAccountCurrencySet();
						Iterator<AccountCurrency> accontCurrencyDBIterator = accountCurrencySet.iterator();
						while (accontCurrencyDBIterator.hasNext()) {
							AccountCurrency accountCurrencyDB = accontCurrencyDBIterator.next();

							if (accountCurrencyFE.getAcqPayId().equalsIgnoreCase(accountCurrencyDB.getAcqPayId())) {
								if (accountCurrencyFE.getCurrencyCode() != null && accountCurrencyFE.getCurrencyCode()
										.equalsIgnoreCase(accountCurrencyDB.getCurrencyCode())) {

									if (accountCurrencyFE.getTxnKey() != null
											&& !(accountCurrencyFE.getTxnKey().equals(accountCurrencyDB.getTxnKey()))) {
										txnKeyChangedFlag = true;
										logAccountString.append(" txnkey is changed from : ");
										logAccountString.append(accountCurrencyDB.getTxnKey());
										logAccountString.append(" to ");
										logAccountString.append(accountCurrencyFE.getTxnKey());
									}

									if (accountCurrencyFE.getMerchantId() != null && !(accountCurrencyFE.getMerchantId()
											.equalsIgnoreCase(accountCurrencyDB.getMerchantId()))) {
										merchantIdChangedFlag = true;
										logAccountString.append(" MerchantId  is changed from : ");
										logAccountString.append(accountCurrencyDB.getMerchantId());
										logAccountString.append(" to ");
										logAccountString.append(accountCurrencyFE.getMerchantId());
									}

									String password = "";
									String payId = "";
									if (accountCurrencyFE.getPassword() != null) {
										password = accountCurrencyFE.getPassword();
										payId = accountCurrencyFE.getAcqPayId();
									}
									String encrpytedPassword = encryptDecryptService.encrypt(payId, password);
									if (password != null && !password.isEmpty()) {
										if (!(encrpytedPassword.equals(accountCurrencyDB.getPassword()))) {
											passwordChangedFlag = true;
											logAccountString.append(" password  is changed ");
										}
									} else {
										encrpytedPassword = password;
									}
								}
							}
						}
					}
				}
			}
		}

		if (txnKeyChangedFlag == true || merchantIdChangedFlag == true || passwordChangedFlag == true
				|| currencyCodeChangedFlag == true) {
			logger.info("Admin name is: " + adminUser.getFirstName() + " and Email id is: " + adminUser.getEmailId()
					+ logAccountString.toString());
		}
	}
}
