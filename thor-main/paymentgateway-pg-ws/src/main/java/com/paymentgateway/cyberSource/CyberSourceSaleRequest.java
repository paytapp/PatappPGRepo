package com.paymentgateway.cyberSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service
public class CyberSourceSaleRequest {
	
private static Logger logger = LoggerFactory.getLogger(CyberSourceSaleRequest.class.getName());
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	private CyberSourceEnrollRequest cyberSourceEnrollRequest;

	private static final String schemasAPI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	//private static final String 
	public SOAPMessage cretaeSaleRequest(Fields fields, Transaction transaction) throws SOAPException, SystemException {
		
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String cardType = cyberSourceEnrollRequest.getCardType(fields);
		
		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		SOAPMessage request = factory.createMessage();
		soapMsg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
		soapMsg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
		try {

			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();

			SOAPHeader header = envelope.getHeader();
			Name headerSecurity = envelope.createName(Constants.SECURITY, "wsse", schemasAPI);
			SOAPHeaderElement securityElement = header.addHeaderElement(headerSecurity);
			securityElement.setAttribute("soapenv:mustUnderstand", "1");
			
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			SOAPElement userName= soapFactory.createElement(Constants.USER_NAME_TOKEN, "wsse", schemasAPI);
			SOAPElement userTokenElememt = securityElement.addChildElement(userName);
			userTokenElememt.setAttribute(Constants.ID, "uuid-e55489fa6444-1");
			
			soapFactory = SOAPFactory.newInstance();
			SOAPElement userNameToken = soapFactory.createElement(Constants.USER_NAME, "wsse", schemasAPI);
			SOAPElement userNameTokenElememt = userTokenElememt.addChildElement(userNameToken);
			userNameTokenElememt.addTextNode(fields.get(FieldType.MERCHANT_ID.getName()));
			
			soapFactory = SOAPFactory.newInstance();
			SOAPElement password = soapFactory.createElement(Constants.PASSWORD, "wsse", schemasAPI);
			SOAPElement passwordElememt = userTokenElememt.addChildElement(password);
			passwordElememt.setAttribute(Constants.TYPE, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
			passwordElememt.addTextNode(fields.get(FieldType.PASSWORD.getName()));
			
			SOAPBody body = envelope.getBody();

			Name ipgapi = envelope.createName(Constants.REQUEST_MSG,"", "urn:schemas-cybersource-com:transaction-data-1.142");
			bodyRoot = body.addBodyElement(ipgapi);
			
			bodyRoot.addChildElement(Constants.MERCHANT_ID).addTextNode(fields.get(FieldType.MERCHANT_ID.getName()));
			bodyRoot.addChildElement(Constants.MERCHANT_REFERENCE_CODE).addTextNode(fields.get(FieldType.PG_REF_NUM.getName()));
			
			soapFactory = SOAPFactory.newInstance();
			Name billTochild = soapFactory.createName("billTo", "", "");
			Name firstNamechild = soapFactory.createName("firstName", "", "");
			Name lastNameChild = soapFactory.createName("lastName", "", "");
			Name streetChild = soapFactory.createName("street1", "", "");
			Name cityChild = soapFactory.createName("city", "", "");
			Name stateChild = soapFactory.createName("state", "", "");
			Name postalCodeChild = soapFactory.createName("postalCode", "", "");
			Name countryChild = soapFactory.createName("country", "", "");
			Name phoneChild = soapFactory.createName("phoneNumber", "", "");
			Name emailChild = soapFactory.createName("email", "", "");
			SOAPElement billsymbol = bodyRoot.addChildElement(billTochild);
			SOAPElement firstNamesymbol = billsymbol.addChildElement(firstNamechild);
			SOAPElement lastNamesymbol = billsymbol.addChildElement(lastNameChild);
			SOAPElement streetsymbol = billsymbol.addChildElement(streetChild);
			SOAPElement citysymbol = billsymbol.addChildElement(cityChild);
			SOAPElement statesymbol = billsymbol.addChildElement(stateChild);
			SOAPElement postalsymbol = billsymbol.addChildElement(postalCodeChild);
			SOAPElement countrysymbol = billsymbol.addChildElement(countryChild);
			SOAPElement phonesymbol = billsymbol.addChildElement(phoneChild);
			SOAPElement emailsymbol = billsymbol.addChildElement(emailChild);
			firstNamesymbol.addTextNode("Letz");
			lastNamesymbol.addTextNode("Pay");
			streetsymbol.addTextNode("3FCS 53, Ansal Plaza, Vaishali");
			citysymbol.addTextNode("Ghaziabad ");
			statesymbol.addTextNode("UP");
			postalsymbol.addTextNode("201010");
			countrysymbol.addTextNode("India");
			phonesymbol.addTextNode("0120-4344884");
			emailsymbol.addTextNode("support@paymentGateway.com");
			
			// add the CreditCardTxType child elements
			soapFactory = SOAPFactory.newInstance();
			Name creditCardTxTypechild = soapFactory.createName(Constants.ITEM, "",
					"");
			Name pricechild = soapFactory.createName(Constants.UNIT_PRICE, "", "");
			SOAPElement creditCardTxTypesymbol = bodyRoot.addChildElement(creditCardTxTypechild);
			creditCardTxTypesymbol.setAttribute("id", "0");
			SOAPElement pricesymbol = creditCardTxTypesymbol.addChildElement(pricechild);
			pricesymbol.addTextNode(amount);

			// add Payment child element
			soapFactory = SOAPFactory.newInstance();
			Name paymentchild = soapFactory.createName(Constants.PURCHASE_TOTALS, "", "");
			Name currencychild = soapFactory.createName(Constants.CURRENCY, "", "");
			SOAPElement paymentsymbol = bodyRoot.addChildElement(paymentchild);
			SOAPElement currencysymbol = paymentsymbol.addChildElement(currencychild);
			currencysymbol.addTextNode(Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName())));

			// add TransactionDetails child element
			soapFactory = SOAPFactory.newInstance();
			Name TransactionDetailsechild = soapFactory.createName(Constants.CARD, "", "");
			Name OrderIdchild = soapFactory.createName(Constants.CARD_NUMBER, "", "");
			Name expMonthChild = soapFactory.createName(Constants.EXP_MONTH, "", "");
			Name expYearhChild = soapFactory.createName(Constants.EXP_YEAR, "", "");
			Name cvvChild = soapFactory.createName(Constants.CVV, "", "");
			Name cardTypeChild = soapFactory.createName(Constants.CARD_TYPE, "", "");
			SOAPElement TransactionDetailsymbol = bodyRoot.addChildElement(TransactionDetailsechild);
			SOAPElement OrderIdsymbol = TransactionDetailsymbol.addChildElement(OrderIdchild);
			SOAPElement expMonthsymbol = TransactionDetailsymbol.addChildElement(expMonthChild);
			SOAPElement expYearsymbol = TransactionDetailsymbol.addChildElement(expYearhChild);
			SOAPElement cvvsymbol = TransactionDetailsymbol.addChildElement(cvvChild);
			SOAPElement cardTypesymbol = TransactionDetailsymbol.addChildElement(cardTypeChild);
			OrderIdsymbol.addTextNode(fields.get(FieldType.CARD_NUMBER.getName()));
			expMonthsymbol.addTextNode(transaction.getExpMonth());
			expYearsymbol.addTextNode(transaction.getExpYear());
			cvvsymbol.addTextNode(fields.get(FieldType.CVV.getName()));
			cardTypesymbol.addTextNode(cardType);
			
			soapFactory = SOAPFactory.newInstance();
			Name ccAuthChild = soapFactory.createName(Constants.CC_AUTH, "",
					"");
			Name recochild = soapFactory.createName(Constants.RECO_ID, "", "");
			SOAPElement ccAuth = bodyRoot.addChildElement(ccAuthChild);
			ccAuth.setAttribute("run", "true");
			SOAPElement reconciliationID = ccAuth.addChildElement(recochild);
			reconciliationID.addTextNode(fields.get(FieldType.PG_REF_NUM.getName()));
			
			
			soapFactory = SOAPFactory.newInstance();
			Name ccCaptureChild = soapFactory.createName(Constants.CC_CAPTURE_SERVICE, "",
					"");
			SOAPElement ccCapture = bodyRoot.addChildElement(ccCaptureChild);
			ccCapture.setAttribute("run", "true");
			
			soapFactory = SOAPFactory.newInstance();
			Name payerAuthChild = soapFactory.createName(Constants.PAYER_AUTH_VALIDATE_SERVICE, "",
					"");
			Name paReschild = soapFactory.createName(Constants.SIGNED_PARES, "", "");
			SOAPElement payerAuth = bodyRoot.addChildElement(payerAuthChild);
			payerAuth.setAttribute("run", "true");
			SOAPElement signedPaRes = payerAuth.addChildElement(paReschild);
			signedPaRes.addTextNode(fields.get(FieldType.PARES.getName()));
					
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapMsg.writeTo(out);
			String strMsg = new String(out.toByteArray());
			strMsg = strMsg.replaceAll("SOAP-ENV", "soapenv");
			InputStream is = new ByteArrayInputStream(strMsg.getBytes());
			request = MessageFactory.newInstance().createMessage(null, is);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return request;

	}
}
