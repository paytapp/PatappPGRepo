package com.paymentgateway.googlePay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.Processor;

@Service("googlePayProcessor")
public class GooglePayProcessor implements Processor {
	@Autowired
	private GooglePayIntegrator googlePayIntegrator;

	public void preProcess(Fields fields) throws SystemException {
	}

	public void process(Fields fields) throws SystemException {

		if ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.STATUS.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.VERIFY.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.RECO.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName())
						.equals(TransactionType.REFUNDRECO.getName()))) {
			return;
		}
		if (!fields.get(FieldType.MOP_TYPE.getName()).equals(MopType.GOOGLEPAY.getCode())) {
			return;
		}

		googlePayIntegrator.process(fields);

	}

	public void postProcess(Fields fields) throws SystemException {
	}

}
