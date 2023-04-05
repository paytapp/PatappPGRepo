package com.paymentgateway.cyberSource;

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
public class CyberSourceRefundRequest {
	private static Logger logger = LoggerFactory.getLogger(CyberSourceEnrollRequest.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	private static final String schemasAPI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final String ns1 = "urn:schemas-cybersource-com:transaction-data-1.26";

	public SOAPMessage cretaeRefundRequest(Fields fields, Transaction transaction) throws SOAPException, SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);

		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		try {

			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();
			envelope.addNamespaceDeclaration("ns1", ns1);

			SOAPHeader header = envelope.getHeader();
			header.addNamespaceDeclaration("wsse", schemasAPI);

			Name headerSecurity = envelope.createName(Constants.SECURITY, "wsse", schemasAPI);
			SOAPHeaderElement securityElement = header.addHeaderElement(headerSecurity);
			securityElement.setAttribute("SOAP-ENV:mustUnderstand", "1");
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			SOAPElement userName = soapFactory.createElement(Constants.USER_NAME_TOKEN, "wsse", schemasAPI);
			SOAPElement userTokenElememt = securityElement.addChildElement(userName);

			soapFactory = SOAPFactory.newInstance();
			SOAPElement userNameToken = soapFactory.createElement(Constants.USER_NAME, "wsse", schemasAPI);
			SOAPElement userNameTokenElememt = userTokenElememt.addChildElement(userNameToken);
			userNameTokenElememt.addTextNode(fields.get(FieldType.MERCHANT_ID.getName()));

			soapFactory = SOAPFactory.newInstance();
			SOAPElement password = soapFactory.createElement(Constants.PASSWORD, "wsse", schemasAPI);
			SOAPElement passwordElememt = userTokenElememt.addChildElement(password);
			passwordElememt.setAttribute(Constants.TYPE,
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
			passwordElememt.addTextNode(fields.get(FieldType.PASSWORD.getName()));

			SOAPBody body = envelope.getBody();

			Name ipgapi = envelope.createName(Constants.REQUEST_MSG, "",
					"urn:schemas-cybersource-com:transaction-data-1.142");
			bodyRoot = body.addBodyElement(ipgapi);

			bodyRoot.addChildElement(Constants.MERCHANT_ID).addTextNode(fields.get(FieldType.MERCHANT_ID.getName()));
			bodyRoot.addChildElement(Constants.MERCHANT_REFERENCE_CODE)
					.addTextNode(fields.get(FieldType.PG_REF_NUM.getName()));
			// add Payment child element
			soapFactory = SOAPFactory.newInstance();
			Name paymentchild = soapFactory.createName("purchaseTotals", "", "");
			Name currencychild = soapFactory.createName("currency", "", "");
			Name amountchild = soapFactory.createName("grandTotalAmount", "", "");
			SOAPElement paymentsymbol = bodyRoot.addChildElement(paymentchild);
			SOAPElement currencysymbol = paymentsymbol.addChildElement(currencychild);
			SOAPElement amountsymbol = paymentsymbol.addChildElement(amountchild);
			currencysymbol.addTextNode(Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName())));
			amountsymbol.addTextNode(amount);

			soapFactory = SOAPFactory.newInstance();
			Name ccCreditServiceChild = soapFactory.createName("ccCreditService", "", "");
			Name captureRequestIdchild = soapFactory.createName("captureRequestID", "", "");
			Name reconciliationIdchild = soapFactory.createName("reconciliationID", "", "");
			SOAPElement creditService = bodyRoot.addChildElement(ccCreditServiceChild);
			creditService.setAttribute("run", "true");
			SOAPElement creditServiceSymbol = creditService.addChildElement(captureRequestIdchild);
			SOAPElement reconciliationIdSymbol = creditService.addChildElement(reconciliationIdchild);
			creditServiceSymbol.addTextNode(fields.get(FieldType.ACQ_ID.getName()));
			String refundFlag = fields.get(FieldType.REFUND_FLAG.getName()); 
			reconciliationIdSymbol.addTextNode(refundFlag.concat(fields.get(FieldType.PG_REF_NUM.getName())));
			soapMsg.writeTo(System.out);
		} catch (Exception exception) {
			logger.error("Exception is: " , exception);
		}
		return soapMsg;

	}
}
