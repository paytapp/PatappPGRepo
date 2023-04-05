package com.paymentgateway.cyberSource;

import java.util.Date;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service("cyberSourceTransactionConverter")
public class TransactionConverter {

	public static final String PAREQ_OPEN_TAG = "<c:paReq>";
	public static final String PAREQ_CLOSE_TAG = "</c:paReq>";
	public static final String ACS_URL_OPEN_TAG = "<c:acsURL>";
	public static final String ACS_URL_CLOSE_TAG = "</c:acsURL>";
	public static final String XID_OPEN_TAG = "<c:xid>";
	public static final String XID_CLOSE_TAG = "</c:xid>";
	public static final String RESPONSE_CODE_OPEN_TAG = "<c:reasonCode>";
	public static final String RESPONSE_CODE_CLOSE_TAG = "</c:reasonCode>";
	public static final String REQUEST_ID_OPEN_TAG = "<c:requestID>";
	public static final String REQUEST_ID_CLOSE_TAG = "</c:requestID>";
	public static final String RECO_ID_OPEN_TAG = "<c:reconciliationReferenceNumber>";
	public static final String RECO_ID_CLOSE_TAG = "</c:reconciliationReferenceNumber>";
	public static final String VERES_ENROLLED_OPEN_TAG = "<c:veresEnrolled>";
	public static final String VERES_ENROLLED_CLOSE_TAG = "</c:veresEnrolled>";

	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private CyberSourceEnrollRequest cyberSourceEnrollRequest;

	@Autowired
	private CyberSourceSaleRequest cyberSourceSaleRequest;

	@Autowired
	private CyberSourceRefundRequest cyberSourceRefundRequest;

	@Autowired
	private FieldsDao fieldsDao;

	@SuppressWarnings("incomplete-switch")
	public SOAPMessage perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		SOAPMessage request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
			break;
		case ENROLL:
			request = enrollRequest(fields, transaction);
			break;
		case REFUND:
			request = refundRequest(fields, transaction);
			break;
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		case CAPTURE:
			break;
		case STATUS:
			// request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public SOAPMessage enrollRequest(Fields fields, Transaction transaction) throws SystemException {
		SOAPMessage enrollRequest = null;
		try {
			if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())) {
				enrollRequest = cyberSourceEnrollRequest.cretaeRupayEnrollRequest(fields, transaction);
			} else {
				enrollRequest = cyberSourceEnrollRequest.cretaeEnrollRequest(fields, transaction);
			}
			fieldsDao.saveSessionParam(fields);
		} catch (SOAPException exception) {
			logger.error("Exception  " , exception);
		}
		return enrollRequest;

	}

	public SOAPMessage saleRequest(Fields fields, Transaction transaction) throws SystemException {
		SOAPMessage request = null;
		try {
			request = cyberSourceSaleRequest.cretaeSaleRequest(fields, transaction);
		} catch (SOAPException exception) {
			logger.error("Exception  " , exception);
		}
		return request;

	}

	public SOAPMessage refundRequest(Fields fields, Transaction transaction) throws SystemException {
		SOAPMessage request = null;
		try {
			request = cyberSourceRefundRequest.cretaeRefundRequest(fields, transaction);
		} catch (SOAPException exception) {
			logger.error("Exception  " , exception);
		}
		return request;

	}

	public Transaction toTransaction(String xml) {

		Transaction transaction = new Transaction();
		transaction.setXid(getTextBetweenTags(xml, XID_OPEN_TAG, XID_CLOSE_TAG));
		transaction.setPaReq(getTextBetweenTags(xml, PAREQ_OPEN_TAG, PAREQ_CLOSE_TAG));
		transaction.setAcsURL(getTextBetweenTags(xml, ACS_URL_OPEN_TAG, ACS_URL_CLOSE_TAG));
		transaction.setResponseCode(getTextBetweenTags(xml, RESPONSE_CODE_OPEN_TAG, RESPONSE_CODE_CLOSE_TAG));
		transaction.setRequestId(getTextBetweenTags(xml, REQUEST_ID_OPEN_TAG, REQUEST_ID_CLOSE_TAG));
		transaction.setRecoId(getTextBetweenTags(xml, RECO_ID_OPEN_TAG, RECO_ID_CLOSE_TAG));
		transaction.setVeresEnrolled(getTextBetweenTags(xml, VERES_ENROLLED_OPEN_TAG, VERES_ENROLLED_CLOSE_TAG));
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
