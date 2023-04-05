package com.paymentgateway.pg.service;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.cashfree.TransactionCommunicator;
import com.paymentgateway.cashfree.TransactionConverter;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.security.SecurityProcessor;

@Service
public class VpaValidationCASHFREE {

	Logger logger = LoggerFactory.getLogger(VpaValidationCASHFREE.class.getName());

	@Autowired
	private SecurityProcessor securityProcessor;

	@Autowired
	@Qualifier("cashfreeTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("cashfreeTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private UserDao userDao;

	public JSONObject validationResponse(Fields fields) {

		try {

			/*
			 * fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()); if
			 * (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
			 * fields.put(FieldType.AMOUNT.getName(), "100"); }
			 * fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			 * fields.put(FieldType.PG_REF_NUM.getName(),
			 * TransactionManager.getNewTransactionId());
			 * 
			 * User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			 * 
			 * if (StringUtils.isNotBlank(user.getSuperMerchantId()) &&
			 * !user.isSuperMerchant()) { fields.put(FieldType.SUB_MERCHANT_ID.getName(),
			 * fields.get(FieldType.PAY_ID.getName()));
			 * fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId()); }
			 * 
			 * securityProcessor.addAcquirerFields(fields);
			 */
			String response = communicator.vpaValidationResponse(fields);

			JSONObject jsonRes = new JSONObject(response);
			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}

			return jsonRes;

		}

		catch (Exception e) {
			logger.info("Exception in validating paytm vpa >>> ", e);
		}

		return null;
	}

}
