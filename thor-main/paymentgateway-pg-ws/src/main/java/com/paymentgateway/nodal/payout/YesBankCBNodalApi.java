package com.paymentgateway.nodal.payout;

import javax.xml.soap.SOAPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.SettlementTransactionType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.history.Historian;

/**
 * @author Rahul
 *
 */
@Service
public class YesBankCBNodalApi {
	
	@Autowired
	@Qualifier("yesBankCBTransactionConverter")
	private YesBankCBTransactionConverter yesBankCBTransactionConverter;
	
	@Autowired
	@Qualifier("yesbankCBTransactionCommunicator")
	private YesBankCBTransactionCommunicator yesBankCBTransactionCommunicator;
	
	private SettlementTransformer settlementTransformer = null;
	
	@Autowired
	private Historian historian;

	public void process(Fields fields) throws SystemException, SOAPException {

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		fields.put(FieldType.TXNTYPE.getName(), txnType);
		if (txnType.equalsIgnoreCase(SettlementTransactionType.FUND_TRANSFER.getName())) {
			fundTransferIntegrator(fields);
		} else if (txnType.equalsIgnoreCase(SettlementTransactionType.ADD_BENEFICIARY.getName())) {
			addBeneficiaryIntegrator(fields);
		} else if (txnType.equalsIgnoreCase(SettlementTransactionType.STATUS.getName())) {
			statusEnqIntegrator(fields);
		}

	}

	public void fundTransferIntegrator(Fields fields) throws SystemException {
		Transaction transactionResponse = new Transaction();
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.OID.getName(), newTxnId);
		String request = yesBankCBTransactionConverter.fundTransferRequest(fields);
		String response = yesBankCBTransactionCommunicator.getFundTransferResponse(fields, request);
		transactionResponse = yesBankCBTransactionConverter.toTransaction(response);
		settlementTransformer = new SettlementTransformer(transactionResponse);
		settlementTransformer.updateResponse(fields);
		
	}

	public void addBeneficiaryIntegrator(Fields fields) throws SOAPException, SystemException {
		Transaction transactionResponse = new Transaction();
		String request = yesBankCBTransactionConverter.addBeneficiaryRequest(fields);
		String response = yesBankCBTransactionCommunicator.getAddBeneficiaryResponse(request);
		transactionResponse = yesBankCBTransactionConverter.addBeneficiaryToTransaction(response);
		settlementTransformer = new SettlementTransformer(transactionResponse);
		settlementTransformer.updateResponse(fields);
	}
	
	public void statusEnqIntegrator(Fields fields) throws SystemException {
		Transaction transactionResponse = new Transaction();
		historian.findPreviousForSettlement(fields);
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		String request = yesBankCBTransactionConverter.getStatusRequest(fields);
		String response = yesBankCBTransactionCommunicator.getFundTransferResponse(fields, request);
		transactionResponse = yesBankCBTransactionConverter.statusToTransaction(response);
		settlementTransformer = new SettlementTransformer(transactionResponse);
		settlementTransformer.updateResponse(fields);
		
	}

}
