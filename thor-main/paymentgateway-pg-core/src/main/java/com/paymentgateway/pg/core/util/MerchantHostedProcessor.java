package com.paymentgateway.pg.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
@Service("merchantHostedProcessor")
public class MerchantHostedProcessor implements Processor {
	@Autowired
	private FieldsDao fieldsDao;
	@Override
	public void preProcess(Fields fields) throws SystemException {
	}

	@Override
	public void process(Fields fields) throws SystemException {
		String merchantHostedFlag = fields.get(FieldType.IS_MERCHANT_HOSTED.getName());
		/*if(StringUtils.isBlank(merchantHostedFlag) || !merchantHostedFlag.equals(Constants.Y)){
			return;
		}*/ 
	//	fields.put(FieldType.OID.getName(),fields.get(FieldType.TXN_ID.getName()));  TODO
		
	//	fields.insertNewOrder();
		//for inserting customer billing details  
	//	fieldsDao.insertCustomerInfoQuery(fields);
		
/*		fields.put(FieldType.ORIG_TXN_ID.getName(),fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(),fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.TXN_ID.getName(),TransactionManager.getNewTransactionId());
		fields.remove(FieldType.INTERNAL_REQUEST_FIELDS.getName());*/
	}

	@Override
	public void postProcess(Fields fields) throws SystemException {
	}

}
