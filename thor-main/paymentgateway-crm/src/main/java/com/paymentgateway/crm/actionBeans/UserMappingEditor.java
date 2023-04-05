package com.paymentgateway.crm.actionBeans;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Puneet
 *
 */
@Service
public class UserMappingEditor {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private AccountFactory accountFactory; 
	
	private static Logger logger = LoggerFactory.getLogger(UserMappingEditor.class.getName());
	
	private boolean accountPresent = false;

	public void decideAccountChange(String userEmail, String mappingString,
			String acquirerCode, AccountCurrency[] accountCurrencySetFE) {
		
		Session session = null;
		try {
			accountPresent = false;
			User user = new User();
			Account account = new Account();
			Account savedAccount = new Account();
			String acquirerPayId;
			User acquirer;
			acquirer = userDao.findAcquirerByCode(acquirerCode);
			acquirerPayId = (acquirer.getPayId());
			user = userDao.find(userEmail);

			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(user, userEmail);
			savedAccount = user.getAccountUsingAcquirerCode(acquirerCode);
			if (savedAccount != null) {
				setAccountPresent(true);
			}

			// create account
			if (!accountPresent) {
				account.setAcquirerPayId(acquirerPayId);
				account.setAcquirerName(acquirer.getBusinessName());
				account = accountFactory.editAccount(account, mappingString,user.getPayId());
				// add currency
				account = accountFactory.addAccountCurrency(account, accountCurrencySetFE, acquirer, user.getPayId());
				user.addAccount(account);
			} else {
				// edit if the account present
				savedAccount = accountFactory.editAccount(savedAccount,	mappingString, user.getPayId());
				// edit currency
				savedAccount = accountFactory.removeAccountCurrency(savedAccount, accountCurrencySetFE);
				savedAccount = accountFactory.addAccountCurrency(savedAccount, accountCurrencySetFE, acquirer, user.getPayId());
				
			}
			session.update(user);
			tx.commit();
		} 
		catch(Exception e) {
			logger.error("Exception in decideAccountChange " , e);
		}
		finally {
			HibernateSessionProvider.closeSession(session);
		}
	}

	public boolean isAccountPresent() {
		return accountPresent;
	}

	public void setAccountPresent(boolean accountPresent) {
		this.accountPresent = accountPresent;
	}
}
