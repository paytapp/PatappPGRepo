package com.paymentgateway.notification.email.sendEmailer;

import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.notification.email.emailBuilder.EmailBuilder;

@Component("emailSender")
public class EmailSender {
	private final Logger logger = LoggerFactory.getLogger(EmailSender.class.getName());

	@Autowired
	@Qualifier("userDao")
	private UserDao userDao;

	@Autowired
	private EmailBuilder emailBuilder;

	public void sendTransactionEmail(Map<String, String> responseMap) {
		String transactionType = responseMap.get(FieldType.TXNTYPE.getName());
		switch (TransactionType.getInstance(transactionType)) {
		case AUTHORISE:
		case SALE:
			postTransactionEmail(responseMap);
			break;
		case REFUND:
			postRefundTransactionEmail(responseMap);
			break;
		default:
			break;
		}
	}

	// SALE/AUTH transaction email
	public void postTransactionEmail(Map<String, String> responseMap) {
		User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));
		String countryCode = responseMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName());
		// Sending transaction email to merchant
		if (user.isTransactionEmailerFlag() == true) {
			try {
				emailBuilder.transactionEmailer(responseMap, UserType.MERCHANT.toString(), user);
			} catch (Exception exception) {

				logger.error("Transactional Emailer Failed for Merchant:" , exception);
			}
			// Sending transaction email to Customer
		} else if (user.isTransactionCustomerEmailFlag() == true) {
			try {
				emailBuilder.transactionCustomerEmail(responseMap);
			} catch (Exception exception) {

				logger.error("Transactional Emailer Failed for Customer:" , exception);
			}
			// Sending Authentication transaction email
		} else {
			if (!countryCode.equals(CrmFieldConstants.INDIA_REGION_CODE.getValue())) {
				if (user.isTransactionAuthenticationEmailFlag()) {
					try {
						emailBuilder.transactionAuthenticationEmail(responseMap);
					} catch (Exception exception) {

						logger.error("Authentication Emailer Failed for customer:" , exception);
					}
				}
			}
		}
	}

	// Refund transaction Email
	public void postRefundTransactionEmail(Map<String, String> responseMap) {
		User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));
		// Sending refund transaction email to customer
		if (user.isRefundTransactionCustomerEmailFlag() == true) {
			try {
				emailBuilder.transactionRefundEmail(responseMap, user.getUserType().toString(),
						responseMap.get(FieldType.CUST_EMAIL.getName()), user.getBusinessName());
			} catch (Exception exception) {
				logger.error("Refund Transactional Emailer  for Customer:" , exception);
			}
			// Sending refund transaction email to Merchant
		} else if (user.isRefundTransactionMerchantEmailFlag() == true) {
			try {
				emailBuilder.transactionRefundEmail(responseMap, user.getUserType().toString(),
						responseMap.get(FieldType.CUST_EMAIL.getName()), user.getBusinessName());
			} catch (Exception exception) {
				logger.error(" Refund Transactional Emailer  for Merchant:" , exception);
			}
		}

	}
}
