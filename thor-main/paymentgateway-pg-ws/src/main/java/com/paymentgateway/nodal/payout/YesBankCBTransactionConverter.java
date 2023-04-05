package com.paymentgateway.nodal.payout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Rahul
 *
 */
@Service("yesBankCBTransactionConverter")
public class YesBankCBTransactionConverter {
	
	public static final String REQUEST_STATUS_OPEN_TAG = "<RequestStatus>";
	public static final String REQUEST_STATUS_CLOSE_TAG = "</RequestStatus>";
	public static final String RRN_OPEN_TAG = "<ReqRefNo>";
	public static final String RRN_CLOSE_TAG = "</ReqRefNo>";
	
	public String addBeneficiaryRequest(Fields fields) {
		
		try {

			MessageFactory factory = MessageFactory.newInstance();
			SOAPMessage soapMsg = factory.createMessage();
			SOAPMessage request = factory.createMessage();
			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();
			envelope.removeNamespaceDeclaration(envelope.getPrefix());
			envelope.addNamespaceDeclaration("soap", "http://www.w3.org/2003/05/soap-envelope");
			envelope.addNamespaceDeclaration("ben", "http://BeneMaintenanceService");

			SOAPHeader header = envelope.getHeader();

			// Two ways to extract body
			SOAPBody soapBody = envelope.getBody();
			soapBody = soapMsg.getSOAPBody();

			// To add some element
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			Name bodyName = soapFactory.createName(Constants.MAINTAIN_BENE, Constants.BEN, "");
			SOAPBodyElement beneficiaryElement = soapBody.addBodyElement(bodyName);

			Name childName = soapFactory.createName(Constants.CUST_ID);
			SOAPElement order = beneficiaryElement.addChildElement(childName);
			order.addTextNode(fields.get(FieldType.CUST_ID_BENEFICIARY.getName()));

			Name beneficiaryCd = soapFactory.createName(Constants.BENEFICIARY_CD);
			SOAPElement beneCd = beneficiaryElement.addChildElement(beneficiaryCd);
			beneCd.addTextNode(fields.get(FieldType.BENEFICIARY_CD.getName()));

			Name srcAccountNo = soapFactory.createName(Constants.SRC_ACCOUNT_NO);
			SOAPElement nodalAC = beneficiaryElement.addChildElement(srcAccountNo);
			nodalAC.addTextNode(fields.get(FieldType.SRC_ACCOUNT_NO.getName()));

			Name paymentType = soapFactory.createName(Constants.PAYMENT_TYPE);
			SOAPElement payment = beneficiaryElement.addChildElement(paymentType);
			payment.addTextNode(fields.get(FieldType.PAYMENT_TYPE.getName()));

			Name beneName = soapFactory.createName(Constants.BENE_NAME);
			SOAPElement name = beneficiaryElement.addChildElement(beneName);
			name.addTextNode(fields.get(FieldType.BENE_NAME.getName()));

			Name beneType = soapFactory.createName(Constants.BENE_TYPE);
			SOAPElement type = beneficiaryElement.addChildElement(beneType);
			type.addTextNode(fields.get(FieldType.BENE_TYPE.getName()));

			Name currencyCd = soapFactory.createName(Constants.CURRENCY_CD);
			SOAPElement currency = beneficiaryElement.addChildElement(currencyCd);
			currency.addTextNode(fields.get(FieldType.CURRENCY_CD.getName()));

			Name bankName = soapFactory.createName(Constants.BANK_NAME);
			SOAPElement bank = beneficiaryElement.addChildElement(bankName);
			bank.addTextNode(fields.get(FieldType.BANK_NAME.getName()));

			Name ifscCode = soapFactory.createName(Constants.IFSC_CODE);
			SOAPElement ifsc = beneficiaryElement.addChildElement(ifscCode);
			ifsc.addTextNode(fields.get(FieldType.IFSC_CODE.getName()));

			Name beneAccountNo = soapFactory.createName(Constants.BENE_ACCOUNT_NO);
			SOAPElement beneAC = beneficiaryElement.addChildElement(beneAccountNo);
			beneAC.addTextNode(fields.get(FieldType.BENE_ACCOUNT_NO.getName()));

			Name action = soapFactory.createName(Constants.ACTION);
			SOAPElement actionNode = beneficiaryElement.addChildElement(action);
			actionNode.addTextNode(Constants.ADD);

			// soapMsg.writeTo(System.out);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapMsg.writeTo(out);
			String strMsg = new String(out.toByteArray());
			strMsg = strMsg.replaceAll("SOAP-ENV", "soap");
			strMsg = strMsg.replace("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"", "");
			InputStream is = new ByteArrayInputStream(strMsg.getBytes());

			request = MessageFactory.newInstance().createMessage(null, is);
			request.writeTo(System.out);
			return strMsg;
		} catch (Exception e) {
			return null;
		}
	}

	public String fundTransferRequest(Fields fields) {

		String amount = fields.get(FieldType.AMOUNT.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		amount = Amount.toDecimal(amount, currencyCode);
		String version = PropertiesManager.propertiesMap.get("YesBankCBVersion");
		String purposeCode = PropertiesManager.propertiesMap.get("YesBankCBPurposeCode");
		String appId = PropertiesManager.propertiesMap.get("YesBankCBAppId");
		fields.put(FieldType.APP_ID.getName(), appId);
		
		StringBuilder request = new StringBuilder();
		request.append("{");
		request.append(" \"startTransfer\": {");
		request.append("\"version\"");
		request.append(":");
		request.append("\"");
		request.append(version);
		request.append("\" ,");
		request.append("\"uniqueRequestNo\":\"");
		request.append(fields.get(FieldType.TXN_ID.getName()));
		request.append("\" ,");
		request.append("\"appID\":\"");
		request.append(appId);
		request.append("\" ,");
		request.append("\"purposeCode\":\"");
		request.append(purposeCode);
		request.append("\" ,");
		request.append("\"customerID\":\"");
		request.append(appId);
		request.append("\" ,");

		request.append("\"debitAccountNo\":\"");
		request.append(fields.get(FieldType.SRC_ACCOUNT_NO.getName()));
		request.append("\" ,");

		request.append("\"beneficiary\": {\"");
		request.append("beneficiaryCode\": \"");
		request.append(fields.get(FieldType.BENEFICIARY_CD.getName()));
		request.append("\" },");

		request.append(" \"transferType\": \"");
		request.append(fields.get(FieldType.PAYMENT_TYPE.getName()));
		request.append("\" ,");

		request.append(" \"transferCurrencyCode\": \"");
		request.append(fields.get(FieldType.CURRENCY_CD.getName()));
		request.append("\" ,");

		request.append(" \"transferAmount\": \"");
		request.append(amount);
		request.append("\" ,");

		request.append(" \"remitterToBeneficiaryInfo\": \"");
		request.append("FUND TRANSFER");
		request.append("\"");
		request.append("}}");

		return request.toString();

	}

	public String getStatusRequest(Fields fields) {
		StringBuilder request = new StringBuilder();
		String version = PropertiesManager.propertiesMap.get("YesBankCBVersion");
		String appId = PropertiesManager.propertiesMap.get("YesBankCBAppId");
		
		request.append("{");
		request.append(" \"getStatus\": {");
		request.append("\"version\"");
		request.append(":");
		request.append("\"");
		request.append(version);
		request.append("\" ,");
		request.append("\"appID\":\"");
		request.append(appId);
		request.append("\" ,");
		//request.append("\" ,");
		request.append("\"customerID\":\"");
		request.append(appId);
		request.append("\" ,");
		request.append("\"requestReferenceNo\":\"");
		request.append(fields.get(FieldType.OID.getName()));
		request.append("\"");
		request.append("}}");

		return request.toString();

	}

	public Transaction toTransaction(String response) {
		JSONObject res = new JSONObject(response);
		JSONObject txnRes = res.getJSONObject("startTransferResponse");;
		
		Transaction transaction = new Transaction();
		if(txnRes.toString().contains("statusCode")) {
			transaction.setStatusCode(txnRes.getString("statusCode"));
		}
		if(txnRes.toString().contains("uniqueResponseNo")) {
			transaction.setUniqueResponseNo(txnRes.getString("uniqueResponseNo"));
		}
		if(txnRes.toString().contains("subStatusCode")) {
			transaction.setSubStatusCode(txnRes.getString("subStatusCode"));
		}
		if(txnRes.toString().contains("subStatusText")) {
			transaction.setResponeMessage(txnRes.getString("subStatusText"));
		}
				
		return transaction;
	}

	public Transaction statusToTransaction(String response) {
		JSONObject res = new JSONObject(response);

		JSONObject statusJson = res.getJSONObject("getStatusResponse");
		JSONObject txnStatusJson = statusJson.getJSONObject("transactionStatus");
		Transaction transaction = new Transaction();

		if(txnStatusJson.toString().contains("bankReferenceNo")) {
			transaction.setUniqueResponseNo(String.valueOf(txnStatusJson.get("bankReferenceNo")));
		}
		if(txnStatusJson.toString().contains("beneficiaryReferenceNo")) {
			transaction.setBeneficiaryReferenceNo(String.valueOf(txnStatusJson.get("beneficiaryReferenceNo")));
		}
		if(txnStatusJson.toString().contains("statusCode")) {
			transaction.setStatusCode(String.valueOf(txnStatusJson.get("statusCode")));
		}
		if(statusJson.toString().contains("transactionDate")) {
			transaction.setTransactionDate(String.valueOf(statusJson.get("transactionDate")));
		}
		/*if(statusJson.toString().contains("transferAmount")) {
			transaction.setTransferAmount((String.valueOf(statusJson.getInt("transferAmount"))));
		}*/
		
		return transaction;
	}

	public Transaction addBeneficiaryToTransaction(String xml) {

		Transaction transaction = new Transaction();
		transaction.setUniqueResponseNo(getTextBetweenTags(xml, RRN_OPEN_TAG, RRN_CLOSE_TAG));
		transaction.setStatus(getTextBetweenTags(xml, REQUEST_STATUS_OPEN_TAG, REQUEST_STATUS_CLOSE_TAG));
		return transaction;
	}
	
	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex, rightIndex);
		}

		return null;
	}// getTextBetweenTags()
}
