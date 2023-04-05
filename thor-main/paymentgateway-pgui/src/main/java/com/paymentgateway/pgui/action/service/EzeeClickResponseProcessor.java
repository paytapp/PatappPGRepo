package com.paymentgateway.pgui.action.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.ResponseCreator;

@Service
public class EzeeClickResponseProcessor implements Processor {
	//@Autowired
	//@Qualifier("amexTransformer")
	//private AmexTransformer amexTransformer;
	
/*	@Autowired
	private AmexUtil amexUtil;*/
	
	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static Logger logger = LoggerFactory.getLogger(EzeeClickResponseProcessor.class.getName());

	String decryptedResponse = "";

	@Override
	public void preProcess(Fields fields) throws SystemException {
		/*String response =fields.get(Constants.EZEE_CLICK_RESPONSE);
		logger.info("Response recieved from amex" + response);
		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		fields.remove(Constants.EZEE_CLICK_RESPONSE);

		if(!StringUtils.isEmpty(response)){
			
			decryptedResponse = transactionControllerServiceProvider.decrypt(response,encryptionKey);
			logger.info("Decrypted response from amex" + decryptedResponse);		
		}*/
	}

	@Override
	public void process(Fields fields) throws SystemException { 
		
	/*	amexTransformer.updateEzeeClickResponse(fields, decryptedResponse);
		fields.updateTransactionDetails();
		fields.updateNewOrderDetails();		*/
	}

	@Override
	public void postProcess(Fields fields) throws SystemException {
		new ResponseCreator().create(fields);
		fields.removeVpcFields();
		fields.removeInternalFields();
		fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
	}
}
