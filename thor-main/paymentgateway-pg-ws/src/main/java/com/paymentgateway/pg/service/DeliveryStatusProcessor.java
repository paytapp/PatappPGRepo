package com.paymentgateway.pg.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.requestrouter.RequestRouter;

@RestController
public class DeliveryStatusProcessor {

	@Autowired
	private Validator generalValidator;

	@Autowired
	private Fields field;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private RequestRouter router;
	
	@Autowired
	private ResponseCreator responseCreator;

	private static Logger logger = LoggerFactory.getLogger(DeliveryStatusProcessor.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/deliveryStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> deliveryStatus(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.removeExtraFields();
			// To put request blob
			String fieldsAsString = fields.getFieldsAsBlobString();
			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			fields.logAllFields("Refine Request:");
			Map<String, String> responseMap = updateStatus(fields);
			responseMap.remove(FieldType.INTERNAL_CUSTOM_MDC.getName());
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

	public Map<String, String> updateStatus(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		try {
			generalValidator.validate(fields);

			field.refreshPreviousForDelivery(fields);
			String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
			if (responseCode.equals("302")) {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_SUCH_TRANSACTION.getResponseMessage());
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				response.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
				response.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
				response.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				response.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
				responseCreator.create(fields);
				response.put(FieldType.HASH.getName(), fields.get(FieldType.HASH.getName()));
				response.put(FieldType.RESPONSE_DATE_TIME.getName(), fields.get(FieldType.RESPONSE_DATE_TIME.getName()));
				return response;
			}
			fields.remove(FieldType.RESPONSE_CODE.getName());
			Fields previousFields = field.getPrevious();
			fieldsDao.insertDeliveryStatus(fields, previousFields);
			if (((fields.get(FieldType.DELIVERY_STATUS.getName()).equalsIgnoreCase("NOT DELIVERED")))
					&& ((fields.get(FieldType.DELIVERY_CODE.getName()).equalsIgnoreCase("100")))) {
				Map<String, String> res = new HashMap<String, String>();
				String refunndOrderId = TransactionManager.getNewTransactionId();
				res.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				res.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
				res.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
				res.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				res.put(FieldType.REFUND_ORDER_ID.getName(), refunndOrderId);
				res.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
				res.put(FieldType.TXNTYPE.getName(), TransactionType.REFUND.toString());
				String hash =getHash(fields, refunndOrderId);
				res.put(FieldType.HASH.getName(), hash);
				
				response = transact(res);
				return response;
			} else if (((fields.get(FieldType.DELIVERY_STATUS.getName()).equalsIgnoreCase("DELIVERED")))
					&& ((fields.get(FieldType.DELIVERY_CODE.getName()).equalsIgnoreCase("000")))) {
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				response.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
				response.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
				response.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				response.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), "Delivery status marked");

			} else if (((fields.get(FieldType.DELIVERY_STATUS.getName()).equalsIgnoreCase("PENDING")))
					&& ((fields.get(FieldType.DELIVERY_CODE.getName()).equalsIgnoreCase("101")))) {
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				response.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
				response.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
				response.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				response.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), "Delivery status pending");

			} else {
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				response.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
				response.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
				response.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				response.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
				response.put(FieldType.RESPONSE_MESSAGE.getName(), "Delivery status and code mismatch");
			}

		} catch (SystemException exception) {
			response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			response.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			response.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			response.put(FieldType.RESPONSE_MESSAGE.getName(), exception.getMessage());
		}
		responseCreator.create(fields);
		response.put(FieldType.HASH.getName(), fields.get(FieldType.HASH.getName()));
		response.put(FieldType.RESPONSE_DATE_TIME.getName(), fields.get(FieldType.RESPONSE_DATE_TIME.getName()));
		return response;

	}

	public Map<String, String> transact(Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.removeExtraFields();
			// To put request blob
			String fieldsAsString = fields.getFieldsAsBlobString();
			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			fields.logAllFields("Refine Request:");
			Map<String, String> responseMap = router.route(fields);
			responseMap.remove(FieldType.INTERNAL_CUSTOM_MDC.getName());
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}
	
	public String getHash(Fields fields, String refunndOrderId) throws SystemException {
		Fields hashField = new Fields();
				hashField.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
		hashField.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
		hashField.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));
		hashField.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
		hashField.put(FieldType.REFUND_ORDER_ID.getName(), refunndOrderId);
		hashField.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
		hashField.put(FieldType.TXNTYPE.getName(), TransactionType.REFUND.toString());
		String hash = Hasher.getHash(hashField);
		return hash;
		
	}

}
