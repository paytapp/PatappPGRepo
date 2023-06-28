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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service
public class CreateRefundRequest {
	
	private static Logger logger = LoggerFactory.getLogger(CreateRefundRequest.class.getName());

	private static final String schemasV1 = ConfigurationConstants.FIRSTDATA_SCHEMAS_V1
			.getValue();
	private static final String schemasAPI = ConfigurationConstants.FIRSTDATA_SCHEMAS_API
			.getValue();
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	public SOAPMessage cretaeRefundRequest(Fields fields, Transaction transaction) throws SOAPException, SystemException {
		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		try {

			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();

			SOAPHeader header = envelope.getHeader();
			
			SOAPBody body = envelope.getBody();

			Name ipgapi = envelope.createName(Constants.IPGAPIORDERREQUEST, Constants.IPGAPI,
					schemasAPI);
			bodyRoot = body.addBodyElement(ipgapi);

			bodyRoot.addNamespaceDeclaration(Constants.VI, schemasV1);
			bodyRoot.addNamespaceDeclaration(Constants.IPGAPI, schemasAPI);

			SOAPFactory soapFactory = SOAPFactory.newInstance();
			Name bodyName = soapFactory.createName(Constants.TRANSACTION, Constants.VI, schemasV1);
			SOAPElement bodyElement = bodyRoot.addChildElement(bodyName);

			// add the CreditCardTxType child elements
			soapFactory = SOAPFactory.newInstance();
			Name creditCardTxTypechild = soapFactory.createName(Constants.CCTXTYPE, Constants.VI,
					schemasV1);
			Name StoreIdchild = soapFactory.createName(Constants.STORE_ID, Constants.VI, schemasV1);
			Name Typechild = soapFactory.createName(Constants.TYPE, Constants.VI, schemasV1);
			SOAPElement creditCardTxTypesymbol = bodyElement.addChildElement(creditCardTxTypechild);
			SOAPElement StoreIdsymbol = creditCardTxTypesymbol.addChildElement(StoreIdchild);
			SOAPElement Typesymbol = creditCardTxTypesymbol.addChildElement(Typechild);
			StoreIdsymbol.addTextNode(transaction.getId());
			Typesymbol.addTextNode(Constants.REFUND);

			// add Payment child element
			soapFactory = SOAPFactory.newInstance();
			Name paymentchild = soapFactory.createName(Constants.PAYMENT, Constants.VI, schemasV1);
			Name chargeTotalchild = soapFactory.createName(Constants.CHARGE_AMOUNT, Constants.VI,
					schemasV1);
			Name currencychild = soapFactory.createName(Constants.CURRENCY, Constants.VI, schemasV1);
			SOAPElement paymentsymbol = bodyElement.addChildElement(paymentchild);
			SOAPElement chargeTotalsymbol = paymentsymbol.addChildElement(chargeTotalchild);
			SOAPElement currencysymbol = paymentsymbol.addChildElement(currencychild);
			chargeTotalsymbol.addTextNode(amount);
			currencysymbol.addTextNode(transaction.getCurrencycode());

			// add TransactionDetails child element
			soapFactory = SOAPFactory.newInstance();
			Name TransactionDetailsechild = soapFactory.createName(Constants.TXN_DETAILS, Constants.VI,
					schemasV1);
			Name OrderIdchild = soapFactory.createName(Constants.ORDER_ID, Constants.VI, schemasV1);
			SOAPElement TransactionDetailsymbol = bodyElement.addChildElement(TransactionDetailsechild);
			SOAPElement OrderIdsymbol = TransactionDetailsymbol.addChildElement(OrderIdchild);
			OrderIdsymbol.addTextNode(fields.get(FieldType.ORIG_TXN_ID.getName()));
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return soapMsg;

	}

}
