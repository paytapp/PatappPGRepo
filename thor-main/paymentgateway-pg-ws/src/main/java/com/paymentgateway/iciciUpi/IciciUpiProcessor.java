package com.paymentgateway.iciciUpi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.Processor;

/**
 * @author Amitosh
 *
 */
@Service("iciciUpiProcessor")
public class IciciUpiProcessor implements Processor {

	@Autowired
	private IciciUpiIntegrator iciciUpiIntegrator;

	@Autowired
	private IciciUpiQrIntegrator iciciUpiQrIntegrator;

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

		if (!fields.get(FieldType.ACQUIRER_TYPE.getName()).equals(AcquirerType.ICICIUPI.getCode())) {
			return;
		}
		// if the MOP Type is GPay
		String mopType = fields.get(FieldType.MOP_TYPE.getName());

		if (mopType.equals(MopType.GOOGLEPAY.getCode())) {
			return;
		}

		if (!fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {
			if (mopType.equals(MopType.UPI_QR.getCode())) {
				iciciUpiQrIntegrator.process(fields);
			} else {
				iciciUpiIntegrator.process(fields);
			}
		}
		else {
			iciciUpiIntegrator.process(fields);
		}
	}

	public void postProcess(Fields fields) throws SystemException {
	}
}