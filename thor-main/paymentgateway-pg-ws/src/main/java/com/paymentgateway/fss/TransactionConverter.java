package com.paymentgateway.fss;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service("fssTransactionConverter")
public class TransactionConverter {

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	public static final String TRANSACTION_NAME = "Transaction";
	public static final String TRANSACTION_OPEN_TAG = "<Transaction>";
	public static final String TRANSACTION_CLOSE_TAG = "</Transaction>";
	public static final String RESULT_OPEN_TAG = "<result>";
	public static final String RESULT_CLOSE_TAG = "</result>";
	public static final String ERROR_TEXT_OPEN_TAG = "<error_text>";
	public static final String ERROR_TEXT_CLOSE_TAG = "</error_text>";
	public static final String URL_OPEN_TAG = "<url>";
	public static final String URL_CLOSE_TAG = "</url>";
	public static final String PAREQ_OPEN_TAG = "<PAReq>";
	public static final String PAREQ_CLOSE_TAG = "</PAReq>";
	public static final String PAYMENT_ID_OPEN_TAG = "<paymentid>";
	public static final String PAYMENT_ID_CLOSE_TAG = "</paymentid>";
	public static final String ECI_OPEN_TAG = "<eci>";
	public static final String ECI_CLOSE_TAG = "</eci>";
	public static final String AUTH_OPEN_TAG = "<auth>";
	public static final String AUTH_CLOSE_TAG = "</auth>";
	public static final String REF_OPEN_TAG = "<ref>";
	public static final String REF_CLOSE_TAG = "</ref>";
	public static final String AVR_OPEN_TAG = "<avr>";
	public static final String AVR_CLOSE_TAG = "</avr>";
	public static final String POST_DATE_OPEN_TAG = "<postdate>";
	public static final String POST_DATE_CLOSE_TAG = "</postdate>";
	public static final String TRANID_OPEN_TAG = "<tranid>";
	public static final String TRANID_CLOSE_TAG = "</tranid>";
	public static final String ERROR_CODE_OPEN_TAG = "<error_code_tag>";
	public static final String ERROR_CODE_CLOSE_TAG = "</error_code_tag>";
	public static final String ERROR_SERVICE_OPEN_TAG = "<error_service_tag>";
	public static final String ERROR_SERVICE_CLOSE_TAG = "</error_service_tag>";
	public static final String TERM_URL_OPEN_TAG = "<TermURL>";
	public static final String ID = "id";
	public static final String PASSWORD = "password";
	public static final String ACTION = "action";
	public static final String AMT = "amt";
	public static final String CURRENCYCODE = "currencycode";
	public static final String TRACKID = "trackId";
	public static final String CARD = "card";
	public static final String EXPMONTH = "expmonth";
	public static final String EXPYEAR = "expyear";
	public static final String CVV2 = "cvv2";
	public static final String MEMBER = "member";
	public static final String UDF1 = "udf1";
	public static final String UDF2 = "udf2";
	public static final String UDF3 = "udf3";
	public static final String UDF4 = "udf4";
	public static final String UDF5 = "udf5";
	public static final String RESULT = "result";
	public static final String URL = "url";
	public static final String PAREQ = "PAReq";
	public static final String PAYMENTID = "paymentid";
	public static final String ECI = "eci";
	public static final String PARES = "PaRes";
	public static final String AUTH = "auth";
	public static final String REF = "ref";
	public static final String AVR = "avr";
	public static final String POSTDATE = "postdate";
	public static final String TRANID = "tranid";
	public static final String MD = "MD";
	public static final String TERMURL = "TermURL";
	public static final String ERROR_TEXT = "error_text";
	public static final String ERROR_CODE_TAG = "error_code_tag";
	public static final String ERROR_SERVICE_TAG = "error_service_tag";
	public static final String ZIP = "zip";
	public static final String ADDR = "addr";
	public static final String TRANSID = "transid";

	// for rupay cards
	public static final String ERROR_URL_OPEN_TAG = "<merchantErrorUrl>";
	public static final String ERROR_URL_CLOSE_TAG = "</merchantErrorUrl>";
	public static final String RETURN_URL_OPEN_TAG = "<merchantResponseUrl>";
	public static final String RETURN_URL_CLOSE_TAG = "</merchantResponseUrl>";
	public static final String RUPAY_RETURN_URL = "merchantResponseUrl";
	public static final String ERROR_URL = "merchantErrorUrl";

	public String toXml(Transaction transaction, Fields fields) throws SystemException {
		StringBuilder xml = new StringBuilder();

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		getElement(ID, transaction.getId(), xml);
		getElement(PASSWORD, transaction.getPassword(), xml);
		getElement(ACTION, transaction.getAction(), xml);
		getElement(AMT, amount, xml);
		getElement(CURRENCYCODE, transaction.getCurrencycode(), xml);
		getElement(TRACKID, transaction.getTrackId(), xml);
		getElement(CARD, transaction.getCard(), xml);
		getElement(EXPMONTH, transaction.getExpmonth(), xml);
		getElement(EXPYEAR, transaction.getExpyear(), xml);
		getElement(CVV2, transaction.getCvv2(), xml);
		getElement(MEMBER, transaction.getMember(), xml);
		getElement(UDF1, transaction.getUdf1(), xml);
		getElement(UDF2, transaction.getUdf2(), xml);
		getElement(UDF3, transaction.getUdf3(), xml);
		getElement(UDF4, transaction.getUdf4(), xml);
		getElement(UDF5, transaction.getUdf5(), xml);
		getElement(RESULT, transaction.getResult(), xml);
		getElement(URL, transaction.getUrl(), xml);
		getElement(PAREQ, transaction.getPAReq(), xml);
		getElement(PAYMENTID, transaction.getPaymentid(), xml);
		getElement(ECI, transaction.getEci(), xml);
		getElement(PARES, transaction.getPaRes(), xml);
		getElement(AUTH, transaction.getAuth(), xml);
		getElement(REF, transaction.getRef(), xml);
		getElement(AVR, transaction.getAvr(), xml);
		getElement(POSTDATE, transaction.getPostdate(), xml);
		getElement(TRANID, transaction.getTranid(), xml);
		getElement(MD, transaction.getMD(), xml);
		getElement(TERMURL, transaction.getTermURL(), xml);
		getElement(ERROR_TEXT, transaction.getError_text(), xml);
		getElement(ERROR_CODE_TAG, transaction.getError_code_tag(), xml);
		getElement(ERROR_SERVICE_TAG, transaction.getError_service_tag(), xml);
		getElement(ZIP, transaction.getZip(), xml);
		getElement(ADDR, transaction.getAddr(), xml);
		getElement(TRANSID, transaction.getTransId(), xml);

		// FOR RUPAY CARD
		if (transaction.getMopType() != null && transaction.getMopType().equals(MopType.RUPAY.getCode())) {
			String returnUrl = PropertiesManager.propertiesMap.get(Constants.RUPAY_RETURN_URL_NAME.getValue());
			getElement(RUPAY_RETURN_URL, returnUrl, xml);
			getElement(ERROR_URL, returnUrl, xml);
		}
		return xml.toString();
	}

	public void getElement(String name, String value, StringBuilder xml) {
		if (null == value) {
			return;
		}

		xml.append("<");
		xml.append(name);
		xml.append(">");
		xml.append(value);
		xml.append("</");
		xml.append(name);
		xml.append(">");
	}

	public Transaction toTransaction(String xml) {

		Transaction transaction = new Transaction();

		transaction.setError_code_tag(getTextBetweenTags(xml, ERROR_CODE_OPEN_TAG, ERROR_CODE_CLOSE_TAG));
		transaction.setError_service_tag(getTextBetweenTags(xml, ERROR_SERVICE_OPEN_TAG, ERROR_SERVICE_CLOSE_TAG));
		String result = getTextBetweenTags(xml, RESULT_OPEN_TAG, RESULT_CLOSE_TAG);
		transaction.setResult(result);
		transaction.setError_text(getTextBetweenTags(xml, ERROR_TEXT_OPEN_TAG, ERROR_TEXT_CLOSE_TAG));
		transaction.setUrl(getTextBetweenTags(xml, URL_OPEN_TAG, URL_CLOSE_TAG));

		// When pareq contains other fields in it, ignore, as this also contains
		// card details
		String pareq = getTextBetweenTags(xml, PAREQ_OPEN_TAG, PAREQ_CLOSE_TAG);
		if (null != pareq && !pareq.contains("<")) {
			transaction.setPAReq(pareq);
		}

		transaction.setPaymentid(getTextBetweenTags(xml, PAYMENT_ID_OPEN_TAG, PAYMENT_ID_CLOSE_TAG));
		transaction.setEci(getTextBetweenTags(xml, ECI_OPEN_TAG, ECI_CLOSE_TAG));
		transaction.setAuth(getTextBetweenTags(xml, AUTH_OPEN_TAG, AUTH_CLOSE_TAG));
		transaction.setRef(getTextBetweenTags(xml, REF_OPEN_TAG, REF_CLOSE_TAG));
		transaction.setAvr(getTextBetweenTags(xml, AVR_OPEN_TAG, AVR_CLOSE_TAG));
		transaction.setPostdate(getTextBetweenTags(xml, POST_DATE_OPEN_TAG, POST_DATE_CLOSE_TAG));
		transaction.setTranid(getTextBetweenTags(xml, TRANID_OPEN_TAG, TRANID_CLOSE_TAG));

		return transaction;
	}// toTransaction()

	public TransactionConverter() {
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