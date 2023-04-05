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
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service
public class CreateEnrollmentSoapRequest {
	
	private static Logger logger = LoggerFactory.getLogger(CreateEnrollmentSoapRequest.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	private static final String schemasV1 = ConfigurationConstants.FIRSTDATA_SCHEMAS_V1
			.getValue();
	private static final String schemasAPI = ConfigurationConstants.FIRSTDATA_SCHEMAS_API
			.getValue();
	
	public SOAPMessage cretaeEnrollRequest(Fields fields, Transaction transaction) throws SOAPException, SystemException {
		
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		
		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
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
			Typesymbol.addTextNode(fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).toLowerCase());

			// add CreditCardData child element
			soapFactory = SOAPFactory.newInstance();
			Name creditcardDataChild = soapFactory.createName(Constants.CCDATA, Constants.VI,
					schemasV1);
			Name cardnumberChild = soapFactory.createName(Constants.CARDNUMBER, Constants.VI,
					schemasV1);
			Name expMonthChild = soapFactory.createName(Constants.EXP_MONTH, Constants.VI, schemasV1);
			Name expYearChild = soapFactory.createName(Constants.EXPY_YEAR, Constants.VI, schemasV1);
			Name cardCodeChild = soapFactory.createName(Constants.CVV, Constants.VI,
					schemasV1);
			SOAPElement ccDatasymbol = bodyElement.addChildElement(creditcardDataChild);
			SOAPElement ccnumbersymbol = ccDatasymbol.addChildElement(cardnumberChild);
			SOAPElement expmonthsymbol = ccDatasymbol.addChildElement(expMonthChild);
			SOAPElement expyYearsymbol = ccDatasymbol.addChildElement(expYearChild);
			SOAPElement cardCodeymbol = ccDatasymbol.addChildElement(cardCodeChild);
			ccnumbersymbol.addTextNode(transaction.getCard());
			expmonthsymbol.addTextNode(transaction.getExpmonth());
			expyYearsymbol.addTextNode(transaction.getExpyear().substring(2));
			cardCodeymbol.addTextNode(transaction.getCvv2());

			// add CreditCard3DSecure child element
			soapFactory = SOAPFactory.newInstance();
			Name creditCard3DSecurechild = soapFactory.createName(Constants.CREDITCARD3DSECURE, Constants.VI,
					schemasV1);
			Name authenticateTransactionchild = soapFactory.createName(Constants.AUTHENTICATE_TRANSACTION, Constants.VI,
					schemasV1);
			SOAPElement creditCard3DSecuresymbol = bodyElement.addChildElement(creditCard3DSecurechild);
			SOAPElement authenticateTransactionsymbol = creditCard3DSecuresymbol
					.addChildElement(authenticateTransactionchild);
			authenticateTransactionsymbol.addTextNode(Constants.AUTHENTICATOR_TRANSACTION);

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
			currencysymbol.addTextNode(Currency.getAlphabaticCode(transaction.getCurrencycode()));

			// add TransactionDetails child element
			soapFactory = SOAPFactory.newInstance();
			Name TransactionDetailsechild = soapFactory.createName(Constants.TXN_DETAILS, Constants.VI,
					schemasV1);
			Name OrderIdchild = soapFactory.createName(Constants.ORDER_ID, Constants.VI, schemasV1);
			SOAPElement TransactionDetailsymbol = bodyElement.addChildElement(TransactionDetailsechild);
			SOAPElement OrderIdsymbol = TransactionDetailsymbol.addChildElement(OrderIdchild);
			OrderIdsymbol.addTextNode(fields.get(FieldType.TXN_ID.getName()));
			//soapMsg.writeTo(System.out);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return soapMsg;

	}

}
