package com.paymentgateway.pg.core.fraudPrevention.util;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;

@Service
public class AccountPasswordScrambler {

	@Autowired
	EncryptDecryptService encryptDecryptService;

	private static Logger logger = LoggerFactory.getLogger(AccountPasswordScrambler.class.getName());

	public User retrieveAndDecryptPass(User user) {
		try {
			Set<Account> accounts = user.getAccounts();
			for (Account account : accounts) {
				Set<AccountCurrency> accountCurrencySet = account.getAccountCurrencySet();
				for (AccountCurrency accountCurrency : accountCurrencySet) {
					if (!StringUtils.isAnyEmpty(accountCurrency.getPassword())) {
						String decryptedPassword = encryptDecryptService.decrypt(user.getPayId(),
								accountCurrency.getPassword());
						accountCurrency.setPassword(decryptedPassword);
					}

					/*
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf1())){
					 * accountCurrency.setAdf1(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf1())); }
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf2())){
					 * accountCurrency.setAdf2(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf2())); }
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf3())){
					 * accountCurrency.setAdf3(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf3())); }
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf4())){
					 * accountCurrency.setAdf4(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf4())); }
					 * 
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf5())){
					 * accountCurrency.setAdf5(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf5())); }
					 */

					if (!StringUtils.isAnyEmpty(accountCurrency.getAdf6())) {
						accountCurrency.setAdf6(encryptDecryptService.decrypt(user.getPayId(),
								accountCurrency.getAdf6()));
					}

					if (!StringUtils.isAnyEmpty(accountCurrency.getAdf7())) {
						accountCurrency.setAdf7(encryptDecryptService.decrypt(user.getPayId(),
								accountCurrency.getAdf7()));
					}

					/*
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf8())){
					 * accountCurrency.setAdf8(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf8())); }
					 * 
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf9())){
					 * accountCurrency.setAdf9(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf9())); }
					 * 
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf10())){
					 * accountCurrency.setAdf10(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf10())); }
					 * 
					 * if(!StringUtils.isAnyEmpty(accountCurrency.getAdf11())){
					 * accountCurrency.setAdf11(awsEncryptDecryptService.decrypt(accountCurrency.
					 * getAdf11())); }
					 */
				}
			}
			return user;
		}
		catch (Exception e) {
			logger.error("Exception " , e);
			return null;
		}
	}
}