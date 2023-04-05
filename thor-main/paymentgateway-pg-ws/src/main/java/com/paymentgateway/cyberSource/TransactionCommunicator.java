package com.paymentgateway.cyberSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Rahul
 *
 */
@Service("cyberSourceTransactionCommunicator")
public class TransactionCommunicator {
	
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());
	
	public String sendSoapMessage(SOAPMessage request, Fields fields) {
		String soapResponse = "";
		SOAPConnection conn = null;
		try {
			String url = PropertiesManager.propertiesMap.get(Constants.REQUEST_URL_SEPARATOR);;
			// Create the connection
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			conn = scf.createConnection();

			logRequest(request, url, fields);

			SOAPMessage rp = conn.call(request, url);

			soapResponse = prepareSoapString(rp);

			logResponse(soapResponse, fields);

			// Close connection
			conn.close();

		} catch (Exception exception) {
			logger.error("Exception  " , exception);
		} 
		return soapResponse;
	}

	private String prepareSoapString(SOAPMessage message) {
		ByteArrayOutputStream req = new ByteArrayOutputStream();
		try {
			message.writeTo(req);

			String reqMsg = new String(req.toByteArray());
			reqMsg = reqMsg.replaceAll(Constants.AMP, Constants.SEPARATOR);
			return reqMsg;
		} catch (SOAPException e) {
			logger.error("Exception in prepareSoapString , exsception = " , e);
		} catch (IOException e) {
			logger.error("Exception in prepareSoapString , exsception = " , e);
		}
		return "";
	}
	
	public void logRequest(SOAPMessage soapRequest, String url, Fields fields) {
		log("Request message to cyber source: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + "Url= " + url + " " + prepareSoapString(soapRequest));
	}

	public void logResponse(String responseMessage, String url, Fields fields) {
		log("Response message from cyber source: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + "Url= " + url + " " + responseMessage);
	}

	public void logRequest(String requestMessage, Fields fields) {
		log("Request message to cyber source: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + requestMessage);
	}

	public void logResponse(String responseMessage, Fields fields) {
		log("Response message from cyber source: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + responseMessage);
	}
	
	private void log(String message) {

		message = Pattern.compile("(accountNumber>)([\\s\\S]*?)(accountNumber>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(expirationMonth>)([\\s\\S]*?)(expirationMonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(expirationYear>)([\\s\\S]*?)(expirationYear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(cvNumber>)([\\s\\S]*?)(cvNumber>)").matcher(message).replaceAll("$1$3");
		//message = Pattern.compile("(Password>)([\\s\\S]*?)(Password>)").matcher(message).replaceAll("$1$3");
		// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
		// fields.getCustomMDC());
		logger.info(message);
	}

}
