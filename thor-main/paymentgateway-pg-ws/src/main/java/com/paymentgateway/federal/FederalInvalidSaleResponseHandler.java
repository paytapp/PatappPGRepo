package com.paymentgateway.federal;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class FederalInvalidSaleResponseHandler {
	
	@Autowired
	private Validator generalValidator;
	
	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;
	
	@Autowired
	private FederalMapper federalMapper;
	
	public Map<String, String> process(Fields fields) throws SystemException {
		
		generalValidator.validate(fields);
		String mpiResponseCode = fields.get(FieldType.FEDERAL_MPIERROR_CODE.getName());
		String receivedStatus = fields.get(FieldType.FEDERAL_STATUS.getName());
		if(!mpiResponseCode.equals(ErrorType.INVALID_HASH.getResponseCode())){
			StatusType status = federalMapper.getStatusType(mpiResponseCode, receivedStatus);
			ErrorType errorType = federalMapper.getErrorType(mpiResponseCode, receivedStatus);
			fields.put(FieldType.STATUS.getName(), status.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		} else {
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());	
			fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			
		}
		String pgMsg = federalMapper.getMesaage(mpiResponseCode, receivedStatus);
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgMsg);
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();
	}

}
