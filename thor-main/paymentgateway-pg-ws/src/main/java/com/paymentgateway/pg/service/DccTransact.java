
package com.paymentgateway.pg.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.firstdata.Constants;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.security.SecurityProcessor;

@RestController
public class DccTransact {

	private static Logger logger = LoggerFactory.getLogger(DccTransact.class.getName());

	@Autowired
	private SecurityProcessor securityProcessor;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	public static final String XID_OPEN_TAG = "<c:xid>";
	public static final String XID_CLOSE_TAG = "</c:xid>";

	private static final String schemasV1 = "http://ipg-online.com/ipgapi/schemas/v1";
	private static final String schemasAPI = "http://ipg-online.com/ipgapi/schemas/ipgapi";
	private static final String schemasA1 = "http://ipg-online.com/ipgapi/schemas/a1";

	@RequestMapping(method = RequestMethod.POST, value = "/dccProcessor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> dccProcessPayment(@RequestBody Map<String, String> reqmap) {
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request DCC:");
			fields.removeInternalFields();
			fields.clean();
			fields.removeExtraFields();
			// To put request blob
			Map<String, String> responseMap = new HashMap<String, String>();
			fields.logAllFields("Refine Request DCC:");
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.ICICI_FIRSTDATA.getCode());
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			//securityProcessor.addAcquirerFields(fields);

			SOAPMessage request = dccRequest(fields);
			String response = sendSoapMessage(request);
			responseMap = dccResponseHandler(fields, response);
			return responseMap;
		} catch (Exception exception) { 
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null; // return reqmap;

		}

	}

	public Map<String, String> dccResponseHandler(Fields fields, String response) throws SystemException {
		toTransaction(response);
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}
		field.insertDCCTransaction(fields);

		return fields.getFields();

	}

	public void toTransaction(String xml) {
		// transaction.setXid(getTextBetweenTags(xml, XID_OPEN_TAG, XID_CLOSE_TAG));

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

	public SOAPMessage dccRequest(Fields fields) throws SOAPException, SystemException {

		SOAPBodyElement bodyRoot = null;
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		try {
			soapMsg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			soapMsg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();

			SOAPHeader header = envelope.getHeader();

			SOAPBody body = envelope.getBody();

			Name ipgapi = envelope.createName(Constants.IPGAPIACTIONREQUEST, Constants.NS5, schemasAPI);
			bodyRoot = body.addBodyElement(ipgapi);

			bodyRoot.addNamespaceDeclaration(Constants.NS2, schemasV1);
			bodyRoot.addNamespaceDeclaration(Constants.NS3, schemasA1);

			SOAPFactory soapFactory = SOAPFactory.newInstance();
			Name bodyName = soapFactory.createName(Constants.ACTION, Constants.NS3, schemasV1);
			SOAPElement bodyElement = bodyRoot.addChildElement(bodyName);

			// add the CreditCardTxType child elements
			soapFactory = SOAPFactory.newInstance();
			Name cardRateDcc = soapFactory.createName(Constants.REQUEST_CARD_RATE_DCC, Constants.NS3, schemasV1);
			Name StoreIdchild = soapFactory.createName(Constants.STORE_ID, Constants.NS3, schemasV1);
			Name Binchild = soapFactory.createName(Constants.BIN, Constants.NS3, schemasV1);
			Name amountchild = soapFactory.createName(Constants.BASE_AMOUNT, Constants.NS3, schemasV1);
			SOAPElement creditCardTxTypesymbol = bodyElement.addChildElement(cardRateDcc);
			SOAPElement StoreIdsymbol = creditCardTxTypesymbol.addChildElement(StoreIdchild);
			SOAPElement Binsymbol = creditCardTxTypesymbol.addChildElement(Binchild);
			SOAPElement Amountsymbol = creditCardTxTypesymbol.addChildElement(amountchild);
			StoreIdsymbol.addTextNode("3300000707");
			Binsymbol.addTextNode("420739");
			Amountsymbol.addTextNode("1000");

			soapMsg.writeTo(System.out);
		} catch (Exception exception) {
			// logger.error("Exception", exception);
		}
		return soapMsg;

	}

	public String sendSoapMessage(SOAPMessage request) {
		String soapResponse = "";
		SOAPConnection conn = null;
		try {
			// String url =
			// PropertiesManager.propertiesMap.get(Constants.REQUEST_URL_SEPARATOR);;

			String url = "https://test.ipg-online.com/connect/gateway/processing";
			// Create the connection
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			conn = scf.createConnection();

			// logRequest(request, url, fields);

			SOAPMessage rp = conn.call(request, url);

			soapResponse = prepareSoapString(rp);

			// logResponse(soapResponse, fields);

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

}
