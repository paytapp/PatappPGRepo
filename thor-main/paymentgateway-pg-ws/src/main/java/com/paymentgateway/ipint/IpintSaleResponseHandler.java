package com.paymentgateway.ipint;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class IpintSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(IpintSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private IpintTransformer ipintTransformer;


	public Map<String, String> process(Fields fields) throws SystemException {
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.IPINT_RESPONSE_FIELD.getName());
		transactionResponse = toTransaction(response);
		
		ipintTransformer = new IpintTransformer(transactionResponse);
		ipintTransformer.updateResponse(fields);

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.IPINT_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();
		
	}

	public Transaction toTransaction(String ipitResponse) {
		
		Transaction transaction = new Transaction();
		//pgrefNum=1077311224180529?invoice_id=DtbTXA3My6haEAWKHmvAST5FrLZxWb7MD;invoice_creation_time=1640349370;transaction_status=CHECKING;transaction_hash=;transaction_onclick=;transaction_time=;transaction_crypto=BTC;invoice_crypto_amount=0.00042078;invoice_amount_in_usd=19.98;invoice_amount_in_local_currency=1500.00;received_crypto_amount=;received_amount_in_usd=;received_amount_in_local_currency=;wallet_address=3A6KDbhA45ZjWrpX1Shnkg8PFHQsA6c1ek;blockchain_transaction_status=PENDING;blockchain_confirmations=;company_name=Test Merchant Letzpay;merchant_website=;
		String[] arrOfStr = ipitResponse.split(";");
		JSONObject responseJson = new JSONObject();
		String[] jsonInputData=null;
		for (String a : arrOfStr) {
			  jsonInputData = a.split("=", 2);
			  responseJson.put(jsonInputData[0],jsonInputData[1]);
	    }
		//{"pgrefNum":"1081411209213432?invoice_id=BSc9QAESpQG7EPSd5bbhEYQQzoc3eVbdB"}
		
		
		 if(responseJson.has("pgrefNum")) {
			 String [] invoiceId =(responseJson.getString("pgrefNum")).split("=",2);
			 transaction.setInvoiceId(invoiceId[1]);}
		 if(responseJson.has("invoice_creation_time")) {
			 transaction.setInvoiceCreationTime(responseJson.getString("invoice_creation_time"));}
		 if(responseJson.has("transaction_status")) {
			 transaction.setTransactionStatus(responseJson.getString("transaction_status"));}
		 if(responseJson.has("transaction_hash")) {
			 transaction.setTransactionHash(responseJson.getString("transaction_hash"));}
		 if(responseJson.has("transaction_onclick")) {
			 transaction.setTransactionOnClick(responseJson.getString("transaction_onclick"));}
		 if(responseJson.has("transaction_time")) {
			 transaction.setTransactionTime(responseJson.getString("transaction_time"));}
		 if(responseJson.has("transaction_crypto")) {
			 transaction.setTransactionCrypto(responseJson.getString("transaction_crypto"));}
		 if(responseJson.has("invoice_crypto_amount")) {
			 transaction.setInvoiceCryptoAmount(responseJson.getString("invoice_crypto_amount"));}
		 if(responseJson.has("received_amount_in_local_currency")) {
			 transaction.setReceivedAmountInLocalCurrency(responseJson.getString("received_amount_in_local_currency"));}
		  if(responseJson.has("wallet_address")) {
			  transaction.setWalletAddress(responseJson.getString("wallet_address"));}
		  if(responseJson.has("blockchain_transaction_status")) {
			  transaction.setBlockchainTransactionStatus(responseJson.getString("blockchain_transaction_status"));}
		  if(responseJson.has("blockchain_confirmations")) {
			  transaction.setBlockchainConfirmations(responseJson.getString("blockchain_confirmations"));}
		  if(responseJson.has("company_name")) {
			  transaction.setCompanyName(responseJson.getString("company_name"));}
		 if(responseJson.has("merchant_website")) {
			 transaction.setMerchantWebsite(responseJson.getString("merchant_website"));}
		
		 if(responseJson.has("invoice_amount_in_local_currency")) {
			 transaction.setInvoiceAmountInLocalCurrency(responseJson.getString("invoice_amount_in_local_currency"));}
		 if(responseJson.has("invoice_crypto_amount")) {
			 transaction.setInvoiceCryptoAmount(responseJson.getString("invoice_crypto_amount"));}
		 if(responseJson.has("invoice_amount_in_usd")) {
			 transaction.setInvoiceAmountInUsd(responseJson.getString("invoice_amount_in_usd"));}
		 
		 
		 
		return transaction;
	}// toTransaction()

	
	public Transaction toTransactionStatusEnquiry(String ipitResponse) {
		Transaction transaction = new Transaction();
		try {
			if(ipitResponse!=null) {
			
			JSONObject json  = new JSONObject(ipitResponse);
			if(json.has("data")) {
				JSONObject jsonData =json.getJSONObject("data");
				if(jsonData.has("transaction_status")) {
					transaction.setTransactionStatus(jsonData.getString("transaction_status"));
				}
			   if(jsonData.has("blockchain_transaction_status")) {
				   transaction.setBlockchainTransactionStatus("blockchain_transaction_status");
			   }
			   if(jsonData.has("invoice_id")) {
				   transaction.setInvoiceId("invoice_id");
			   }
				
			}
			}
		} catch (Exception e) {
			logger.error("IpintSaleResponseHandler Totransaction status Enquiry",e);
		}
		
		
		
		
		return transaction;
	
	}

}
