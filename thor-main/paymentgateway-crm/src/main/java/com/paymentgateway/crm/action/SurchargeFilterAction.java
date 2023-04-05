package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class SurchargeFilterAction extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private CrmValidator validator;
	
	
	private static Logger logger = LoggerFactory.getLogger(SurchargeFilterAction.class.getName());

	private static final long serialVersionUID = -7123662871783720276L;

	private String payId;
	private String acquirer;
	Set<Account> acquirerSet;
	private List<String> acquirerList = new ArrayList<String>();
	Set<Payment> paymentTypeSet;
	private List<String> paymentTypeList = new ArrayList<String>();
	private List<Merchants> listMerchant = new ArrayList<Merchants>();

	@SuppressWarnings("unchecked")
	public String execute() {
		setListMerchant(userDao.getActiveMerchantList());
		try {
			User user = new User();
			user = userDao.findPayId(getPayId());

			acquirerSet = user.getAccounts();
			for (Account account : acquirerSet) {
				acquirerList.add(account.getAcquirerName());
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	public void validate() {
	if ((validator.validateBlankField(getPayId()))) {
		addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PAY_ID,getPayId()))) {
		addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getAcquirer()))) {
		addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.ACQUIRER,getAcquirer()))) {
		addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
	}
	}

	public String fetchPaymentType() {

		User user = new User();
		user = userDao.findPayId(payId);
		user.getAccounts();
		AcquirerType atype = AcquirerType.getInstancefromName(acquirer);
		String acquirerCode = atype.getCode();
		Account account = user.getAccountUsingAcquirerCode(acquirerCode);

		if (account == null) {
			return null;
		}

		Session session = null;
		try {
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(account, account.getId());
			paymentTypeSet = account.getPayments();

			for (Payment payment : paymentTypeSet) {

				if (payment.getPaymentType() != null && payment.getPaymentType().toString() != "") {
					paymentTypeList.add(payment.getPaymentType().getName());
				}
			}

			tx.commit();
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
		return SUCCESS;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public List<String> getAcquirerList() {
		return acquirerList;
	}

	public void setAcquirerList(List<String> acquirerList) {
		this.acquirerList = acquirerList;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public List<String> getPaymentTypeList() {
		return paymentTypeList;
	}

	public void setPaymentTypeList(List<String> paymentTypeList) {
		this.paymentTypeList = paymentTypeList;
	}
}
