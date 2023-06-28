package com.paymentgateway.cyberSource;

import javax.xml.soap.SOAPMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class CyberSourceIntegrator {
	
	@Autowired
	@Qualifier("cyberSourceTransactionConverter")
	private TransactionConverter converter;
	
	@Autowired
	@Qualifier("cyberSourceTransactionCommunicator")
	private TransactionCommunicator communicator;
	
	@Autowired
	private TransactionFactory TransactionFactory;
	
	private CyberSourceTransformer cyberSourceTransformer = null;
	
	public void process(Fields fields) throws SystemException {

		send(fields);

		//resend(fields);		
				
		//cryptoManager.secure(fields);
	}//process
	
	public void resend(Fields fields) throws SystemException{
		
		//TODO: Put a merchant specific flag
		
		// If card was not enrolled, CyberSource suggests to perform authorization
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.ENROLL.getName())) {
			String status = fields.get(FieldType.STATUS.getName());
			if (null != status && status.equals(StatusType.PENDING.getName())) {
				fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				
				//send(fields);

				// If transaction not authorized
				if (fields.get(FieldType.STATUS.getName()).equals(StatusType.PENDING.getName())) {
					fields.put(FieldType.STATUS.getName(),	StatusType.DECLINED.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DECLINED.getResponseMessage());
				}//if
			}//if
		}//if
	}

	public void send(Fields fields) throws SystemException {
		
		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();
		
		transactionRequest = TransactionFactory.getInstance(fields);

		SOAPMessage request = converter.perpareRequest(fields, transactionRequest);

		String response = communicator.sendSoapMessage(request, fields);

		transactionResponse = converter.toTransaction(response);

		cyberSourceTransformer = new CyberSourceTransformer(transactionResponse);
		cyberSourceTransformer.updateResponse(fields);
	}

	public TransactionConverter getConverter() {
		return converter;
	}

	public void setConverter(TransactionConverter converter) {
		this.converter = converter;
	}

	public TransactionCommunicator getCommunicator() {
		return communicator;
	}
	

	public void setCommunicator(TransactionCommunicator communicator) {
		this.communicator = communicator;
	}

}
