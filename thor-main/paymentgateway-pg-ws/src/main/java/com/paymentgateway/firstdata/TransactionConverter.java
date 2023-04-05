package com.paymentgateway.firstdata;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service("firstDataTransactionConverter")
public class TransactionConverter {
	
	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private CreateEnrollmentSoapRequest createEnrollmentSoapRequest;

	@Autowired
	private CreateCaptureSoapRequest createCaptureSoapRequest;
	
	@Autowired
	private CreateRefundRequest createRefundRequest;
	
	@Autowired
	private CreateStatusEnquiryRequest createStatusEnquiryRequest;

	public static final String MD_OPEN_TAG = "<v1:MD>";
	public static final String MD_CLOSE_TAG = "</v1:MD>";
	public static final String TERM_URL_OPEN_TAG = "<v1:TermUrl>";
	public static final String TERM_URL_CLOSE_TAG = "</v1:TermUrl>";
	public static final String PAREQ_OPEN_TAG = "<v1:PaReq>";
	public static final String PAREQ_CLOSE_TAG = "</v1:PaReq>";
	public static final String ACS_URL_OPEN_TAG = "<v1:AcsURL>";
	public static final String ACS_URL_CLOSE_TAG = "</v1:AcsURL>";
	public static final String ERROR_MESSAGE_OPEN_TAG = "<ipgapi:ErrorMessage>";
	public static final String ERROR_MESSAGE_CLOSE_TAG = "</ipgapi:ErrorMessage>";
	public static final String APPROVAL_CODE_OPEN_TAG = "<ipgapi:ApprovalCode>";
	public static final String APPROVAL_CODE_CLOSE_TAG = "</ipgapi:ApprovalCode>";
	public static final String TRANSACTION_RESULT_OPEN_TAG = "<ipgapi:TransactionResult>";
	public static final String TRANSACTION_RESULT_CLOSE_TAG = "</ipgapi:TransactionResult>";
	public static final String IPG_TRANSACTION_ID_OPEN_TAG = "<ipgapi:IpgTransactionId>";
	public static final String IPG_TRANSACTION_ID_CLOSE_TAG = "</ipgapi:IpgTransactionId>";
	public static final String TRANSACTION_TIME_OPEN_TAG = "<ipgapi:TransactionTime>";
	public static final String TRANSACTION_TIME_CLOSE_TAG = "</ipgapi:TransactionTime>";
	public static final String T_DATE_FORMAT_OPEN_TAG = "<ipgapi:TDateFormatted>";
	public static final String T_DATE_FORMAT_CLOSE_TAG = "</ipgapi:TDateFormatted>";

	public static final String PROCESSOR_APPROVAL_CODE_OPEN_TAG = "<ipgapi:ProcessorApprovalCode>";
	public static final String PROCESSOR_APPROVAL_CODE_CLOSE_TAG = "</ipgapi:ProcessorApprovalCode>";
	public static final String PROCESSOR_RESPONSE_CODE_OPEN_TAG = "<ipgapi:ProcessorResponseCode>";
	public static final String PROCESSOR_RESPONSE_CODE_CLOSE_TAG = "</ipgapi:ProcessorResponseCode>";
	public static final String PROCESSOR_RESPONSE_MESSAGE_OPEN_TAG = "<ipgapi:ProcessorResponseMessage>";
	public static final String PROCESSOR_RESPONSE_MESSAGE_CLOSE_TAG = "</ipgapi:ProcessorResponseMessage>";

	@SuppressWarnings("incomplete-switch")
	public SOAPMessage perpareRequest(Fields fields, Transaction transaction) throws SystemException {
		
		SOAPMessage xml = null;
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
			break;
		case ENROLL:
			xml = enrollRequest(fields, transaction);
			break;
		case REFUND:
			xml = refundRequest(fields, transaction);
			break;
		case SALE:
			// Authorization and Sale messaging format is same, just action code
			// changes
			xml = saleRequest(fields, transaction);
			break;
		case CAPTURE:
			break;
		case STATUS:
		//	xml = statusEnquiryRequest(fields, transaction);
			break;
		}
		return xml;
	}

	public SOAPMessage enrollRequest(Fields fields, Transaction transaction) throws SystemException {
		SOAPMessage enrollRequest = null;

		try {
			enrollRequest = createEnrollmentSoapRequest.cretaeEnrollRequest(fields, transaction);
		} catch (SOAPException exception) {
			logger.error("Exception  " , exception);
		}
		return enrollRequest;
	}

	public SOAPMessage saleRequest(Fields fields, Transaction transaction) {
		SOAPMessage saleRequest = null;

		try {
			saleRequest = createCaptureSoapRequest.cretaeCaptureRequest(fields, transaction);
		} catch (SOAPException exception) {
			logger.error("Exception  " , exception);
		}
		return saleRequest;
	}
	
	public SOAPMessage refundRequest(Fields fields, Transaction transaction) throws SystemException {
		SOAPMessage refundRequest = null;

		try {
			refundRequest = createRefundRequest.cretaeRefundRequest(fields, transaction);
		} catch (SOAPException exception) {
			logger.error("Exception  " , exception);
		}
		return refundRequest;
	}
	
	/*public SOAPMessage statusEnquiryRequest(Fields fields, Transaction transaction) {
		SOAPMessage saleRequest = null;

		try {
			saleRequest = createStatusEnquiryRequest.cretaeStatusEnquiryRequest(fields, transaction);
		} catch (SOAPException exception) {
			logger.error("Exception  " + exception);
		}

		return saleRequest;
	}*/

	public Transaction toTransaction(String xml) {

		Transaction transaction = new Transaction();
		transaction.setMD(getTextBetweenTags(xml, MD_OPEN_TAG, MD_CLOSE_TAG));
		transaction.setTermURL(getTextBetweenTags(xml, TERM_URL_OPEN_TAG, TERM_URL_CLOSE_TAG));
		transaction.setPAReq(getTextBetweenTags(xml, PAREQ_OPEN_TAG, PAREQ_CLOSE_TAG));
		transaction.setAcsUrl(getTextBetweenTags(xml, ACS_URL_OPEN_TAG, ACS_URL_CLOSE_TAG));
		transaction.setError_message(getTextBetweenTags(xml, ERROR_MESSAGE_OPEN_TAG, ERROR_MESSAGE_CLOSE_TAG));
		transaction.setApprovalCode(getTextBetweenTags(xml, APPROVAL_CODE_OPEN_TAG, APPROVAL_CODE_CLOSE_TAG));
		transaction.setTransactionResult(
				getTextBetweenTags(xml, TRANSACTION_RESULT_OPEN_TAG, TRANSACTION_RESULT_CLOSE_TAG));
		transaction.setIpgTransactionId(
				getTextBetweenTags(xml, IPG_TRANSACTION_ID_OPEN_TAG, IPG_TRANSACTION_ID_CLOSE_TAG));
		transaction.setTransactionTime(getTextBetweenTags(xml, TRANSACTION_TIME_OPEN_TAG, TRANSACTION_TIME_CLOSE_TAG));
		transaction.settDateFormat(getTextBetweenTags(xml, T_DATE_FORMAT_OPEN_TAG, T_DATE_FORMAT_CLOSE_TAG));
		transaction.setProcessorApprovalCode(
				getTextBetweenTags(xml, PROCESSOR_APPROVAL_CODE_OPEN_TAG, PROCESSOR_APPROVAL_CODE_CLOSE_TAG));
		transaction.setProcessorResponseCode(
				getTextBetweenTags(xml, PROCESSOR_RESPONSE_CODE_OPEN_TAG, PROCESSOR_RESPONSE_CODE_CLOSE_TAG));
		transaction.setProcessorResponseMessage(
				getTextBetweenTags(xml, PROCESSOR_RESPONSE_MESSAGE_OPEN_TAG, PROCESSOR_RESPONSE_MESSAGE_CLOSE_TAG));

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