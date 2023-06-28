package com.paymentgateway.pg.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class UpiSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(UpiSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);

		if (fields.get(FieldType.RESPONSE_CODE.getName())
				.equals(Constants.KOTAK_UPI_CHECKSUM_FAILURE_CODE.getValue())) {

			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			fields.put((FieldType.PAYMENTS_REGION.getName()), "DOMESTIC");
			fields.put((FieldType.CARD_HOLDER_TYPE.getName()), "CONSUMER");
			fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			ProcessManager.flow(updateProcessor, fields, true);
		} else {
			generalValidator.validate(fields);
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			fields.put((FieldType.PAYMENTS_REGION.getName()), "DOMESTIC");
			fields.put((FieldType.CARD_HOLDER_TYPE.getName()), "CONSUMER");
			fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			ProcessManager.flow(updateProcessor, fields, true);
		}
		return fields.getFields();
	}

}
