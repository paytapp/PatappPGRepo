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

@Service
public class CreateStatusEnquiryRequest {
	
	private static Logger logger = LoggerFactory.getLogger(CreateStatusEnquiryRequest.class.getName());

	private static final String schemasA1 = ConfigurationConstants.FIRSTDATA_SCHEMAS_A1
			.getValue();
	private static final String schemasAPI = ConfigurationConstants.FIRSTDATA_SCHEMAS_API
			.getValue();
	private static final String schemasV1 = ConfigurationConstants.FIRSTDATA_SCHEMAS_V1
			.getValue();
	
	public static SOAPMessage cretaeStatusEnquiryRequest() throws SOAPException {
		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		try {

			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();

			SOAPHeader header = envelope.getHeader();
			
			SOAPBody body = envelope.getBody();

			Name ipgapi = envelope.createName(Constants.IPGAPIACTIONREQUEST, Constants.IPGAPI,
					schemasAPI);
			bodyRoot = body.addBodyElement(ipgapi);

			bodyRoot.addNamespaceDeclaration(Constants.VI, schemasA1);
			bodyRoot.addNamespaceDeclaration(Constants.IPGAPI, schemasAPI);

			SOAPFactory soapFactory = SOAPFactory.newInstance();
			Name bodyName = soapFactory.createName(Constants.ACTION, Constants.VI, schemasV1);
			SOAPElement bodyElement = bodyRoot.addChildElement(bodyName);

			// add the CreditCardTxType child elements
			soapFactory = SOAPFactory.newInstance();
			Name creditCardTxTypechild = soapFactory.createName(Constants.INQUIRY_ORDER, Constants.VI,
					schemasV1);
			Name StoreIdchild = soapFactory.createName(Constants.STORE_ID, Constants.VI, schemasV1);
			Name orderIdchild = soapFactory.createName(Constants.ORDER_ID, Constants.VI, schemasV1);
			SOAPElement creditCardTxTypesymbol = bodyElement.addChildElement(creditCardTxTypechild);
			SOAPElement StoreIdsymbol = creditCardTxTypesymbol.addChildElement(StoreIdchild);
			SOAPElement OrderIdsymbol = creditCardTxTypesymbol.addChildElement(orderIdchild);
			StoreIdsymbol.addTextNode("4564646");
			OrderIdsymbol.addTextNode("46543416543416465");
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return soapMsg;

	}

}

