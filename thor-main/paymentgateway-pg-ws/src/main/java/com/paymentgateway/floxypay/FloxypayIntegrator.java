package com.paymentgateway.floxypay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class FloxypayIntegrator {

	@Autowired
	@Qualifier("floxypayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("floxypayTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("floxypayFactory")
	private TransactionFactory transactionFactory;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private FloxypayTransformer floxypayTransformer = null;

	public void process(Fields fields) throws SystemException {
		send(fields);
	}

	public void send(Fields fields) throws SystemException {

		// Fetch merchant businessName
		UserSettingData userSettingData = new UserSettingData();
		userSettingData = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		
		Transaction transactionRequest = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields,userSettingData);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			
			String paymentResponse = communicator.getPaymentResponse(request,fields,transactionRequest);
			String paymentUrl = converter.getPaymentUrl(paymentResponse,fields);
			communicator.updateSaleResponse(fields, paymentUrl);
			
		} 
	}

}
