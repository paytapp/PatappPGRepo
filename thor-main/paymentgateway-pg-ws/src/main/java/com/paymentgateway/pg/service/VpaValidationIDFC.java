package com.paymentgateway.pg.service;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.AWSRequestMetrics.Field;
import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.idfcUpi.IdfcUpiIntegrator;
import com.paymentgateway.pg.security.SecurityProcessor;

@Service
public class VpaValidationIDFC {

	@Autowired
	private SecurityProcessor securityProcessor;

	@Autowired
	private IdfcUpiIntegrator idfcIntegrator;

	@Autowired
	private UserDao userDao;

	public JSONObject validationResponse(Fields fields) {

		try {
			
			fields.put(FieldType.TXNTYPE.getName(),TransactionType.SALE.getName());
			if(StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))){
				fields.put(FieldType.ORDER_ID.getName(),"VPA VALIDATE");
			}
			fields.put(FieldType.CURRENCY_CODE.getName(),"356");
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			
			User user=userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			
			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}
			
			securityProcessor.addAcquirerFields(fields);
			JSONObject jsonRes = idfcIntegrator.vpaValidation(fields, fields.get(FieldType.ADF6.getName()));
			
			//removing subMerchant Field
			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}
			return jsonRes;
			
		}

		catch (Exception e) {

		}

		return null;
	}

}
