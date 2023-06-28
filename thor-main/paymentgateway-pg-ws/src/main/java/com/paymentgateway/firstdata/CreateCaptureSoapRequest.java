package com.paymentgateway.firstdata;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service
public class CreateCaptureSoapRequest {

	private static Logger logger = LoggerFactory.getLogger(CreateCaptureSoapRequest.class.getName());

	private static final String schemasV1 = ConfigurationConstants.FIRSTDATA_SCHEMAS_V1.getValue();
	private static final String schemasAPI = ConfigurationConstants.FIRSTDATA_SCHEMAS_API.getValue();
	private static final String schemasA1 = ConfigurationConstants.FIRSTDATA_SCHEMAS_API.getValue();

	public SOAPMessage cretaeCaptureRequest(Fields fields, Transaction transaction) throws SOAPException {
		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		try {

			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();

			SOAPHeader header = envelope.getHeader();

			SOAPBody body = envelope.getBody();

			Name ipgapi = envelope.createName(Constants.IPGAPIORDERREQUEST, Constants.NS4, schemasAPI);
			bodyRoot = body.addBodyElement(ipgapi);

			bodyRoot.addNamespaceDeclaration(Constants.NS4, schemasAPI);
			bodyRoot.addNamespaceDeclaration(Constants.NS2, schemasA1);
			bodyRoot.addNamespaceDeclaration(Constants.NS3, schemasV1);

			SOAPFactory soapFactory = SOAPFactory.newInstance();
			Name bodyName = soapFactory.createName(Constants.TRANSACTION, Constants.NS3, schemasV1);
			SOAPElement bodyElement = bodyRoot.addChildElement(bodyName);

			// add the CreditCardTxType child elements
			soapFactory = SOAPFactory.newInstance();
			Name creditCardTxTypechild = soapFactory.createName(Constants.CCTXTYPE, Constants.NS3, schemasV1);
			Name StoreIdchild = soapFactory.createName(Constants.STORE_ID, Constants.NS3, schemasV1);
			Name Typechild = soapFactory.createName(Constants.TYPE, Constants.NS3, schemasV1);
			SOAPElement creditCardTxTypesymbol = bodyElement.addChildElement(creditCardTxTypechild);
			SOAPElement StoreIdsymbol = creditCardTxTypesymbol.addChildElement(StoreIdchild);
			SOAPElement Typesymbol = creditCardTxTypesymbol.addChildElement(Typechild);
			StoreIdsymbol.addTextNode(fields.get(FieldType.MERCHANT_ID.getName()));
			Typesymbol.addTextNode(fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).toLowerCase());

			// add CreditCardData child element
			soapFactory = SOAPFactory.newInstance();
			Name creditcardDataChild = soapFactory.createName(Constants.CCDATA, Constants.NS3, schemasV1);
			Name cardCodeChild = soapFactory.createName(Constants.CVV, Constants.NS3, schemasV1);
			SOAPElement ccDatasymbol = bodyElement.addChildElement(creditcardDataChild);
			SOAPElement cardCodeymbol = ccDatasymbol.addChildElement(cardCodeChild);
			cardCodeymbol.addTextNode(fields.get(FieldType.CVV.getName()));

			// add CreditCard3DSecure child element
			soapFactory = SOAPFactory.newInstance();

			Name creditCard3DSecure = soapFactory.createName(Constants.CREDITCARD3DSECURE, Constants.NS3, schemasV1);
			Name Secure3DRequest = soapFactory.createName(Constants.SECURE3DREQUEST, Constants.NS3, schemasV1);
			Name Secure3DAuthenticationRequest = soapFactory.createName(Constants.SECURE3DAUTHENTICATIONREQUEST,
					Constants.NS3, schemasV1);
			Name AcsResponse = soapFactory.createName(Constants.ACS_RESPONSE, Constants.NS3, schemasV1);
			Name MD = soapFactory.createName(Constants.MD, Constants.NS3, schemasV1);
			Name PaRes = soapFactory.createName(Constants.PARES, Constants.NS3, schemasV1);
			SOAPElement creditCard3DSecureElement = bodyElement.addChildElement(creditCard3DSecure);
			SOAPElement Secure3DRequestElement = creditCard3DSecureElement.addChildElement(Secure3DRequest);
			SOAPElement Secure3DAuthenticationRequestElement = Secure3DRequestElement
					.addChildElement(Secure3DAuthenticationRequest);
			SOAPElement AcsResponseElement = Secure3DAuthenticationRequestElement.addChildElement(AcsResponse);
			SOAPElement MDElement = AcsResponseElement.addChildElement(MD);
			SOAPElement PaResElement = AcsResponseElement.addChildElement(PaRes);
			MDElement.addTextNode(fields.get(FieldType.MD.getName()));
			PaResElement.addTextNode(transaction.getPaRes());

			// add TransactionDetails child element
			soapFactory = SOAPFactory.newInstance();
			Name transactionDetailschild = soapFactory.createName(Constants.TXN_DETAILS, Constants.NS3, schemasV1);
			Name ipgTransactionIdchild = soapFactory.createName(Constants.IPG_TXN_ID, Constants.NS3, schemasV1);
			Name transactionOriginchild = soapFactory.createName(Constants.TXN_ORIGIN, Constants.NS3, schemasV1);
			SOAPElement txnDetailssymbol = bodyElement.addChildElement(transactionDetailschild);
			SOAPElement transactionIdsymbol = txnDetailssymbol.addChildElement(ipgTransactionIdchild);
			SOAPElement transactionOriginsymbol = txnDetailssymbol.addChildElement(transactionOriginchild);
			transactionIdsymbol.addTextNode(fields.get(FieldType.ACQ_ID.getName()));
			transactionOriginsymbol.addTextNode(Constants.ECI);
		
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return soapMsg;

	}

}
