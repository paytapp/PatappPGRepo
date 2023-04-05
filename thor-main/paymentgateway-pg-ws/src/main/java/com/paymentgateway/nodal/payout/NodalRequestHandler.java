package com.paymentgateway.nodal.payout;

import java.util.Map;

import javax.xml.soap.SOAPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.SettlementTransactionType;
import com.paymentgateway.pg.core.util.Processor;

/**
 * @author Rahul
 *
 */
@Service
public class NodalRequestHandler {

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private YesBankCBNodalApi yesBankNodalApi;

	@Autowired
	private FieldsDao fieldsDao;

	public Map<String, String> process(Fields fields) throws SystemException, SOAPException {

		//generalValidator.validate(fields);

		String acquire = fields.get(FieldType.NODAL_ACQUIRER.getName());
		if (acquire.equalsIgnoreCase(AcquirerType.YESBANKCB.getCode())) {
			yesBankNodalApi.process(fields);
		} else {

		}

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (!txnType.equals(SettlementTransactionType.ADD_BENEFICIARY.getName())) {
			fieldsDao.insertSettlementTransaction(fields);
		}
		return fields.getFields();
	}

}
