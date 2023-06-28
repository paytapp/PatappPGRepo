package com.paymentgateway.cyberSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.yesbankcb.YesBankCbIntegrator;

@Service("cyberSourceProcessor")
public class CyberSourceProcessor implements Processor {

	@Autowired
	private YesBankCbIntegrator yesBankCbIntegrator;

	@Autowired
	private CyberSourceIntegrator cyberSourceIntegrator;

	public void preProcess(Fields fields) throws SystemException {
	}

	public void process(Fields fields) throws SystemException {

		if ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.STATUS.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.VERIFY.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.RECO.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName())
						.equals(TransactionType.REFUNDRECO.getName()))) {
			// New Order Transactions are not processed by FSS
			return;
		}

		if (fields.get(FieldType.ACQUIRER_TYPE.getName()).equals(AcquirerType.YESBANKCB.getCode())
				|| fields.get(FieldType.ACQUIRER_TYPE.getName()).equals(AcquirerType.AXISBANKCB.getCode())) {
			// if the MOP Type is GPay
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			String mopType = fields.get(FieldType.MOP_TYPE.getName());

			if (paymentType.equals(PaymentType.UPI.getCode()) && mopType.equals(MopType.GOOGLEPAY.getCode())) {
				return;
			} else if (paymentType.equals(PaymentType.UPI.getCode())) {
				yesBankCbIntegrator.process(fields);
			} else {
				cyberSourceIntegrator.process(fields);
			}
		} else {
			return;
		}

	}

	public void postProcess(Fields fields) throws SystemException {
	}

}
