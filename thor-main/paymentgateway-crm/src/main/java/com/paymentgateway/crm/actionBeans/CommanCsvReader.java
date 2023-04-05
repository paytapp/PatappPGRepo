package com.paymentgateway.crm.actionBeans;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.BatchTransactionObj;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnChannel;
import com.paymentgateway.crm.action.StatusEnquiryParameters;

import au.com.bytecode.opencsv.CSVReader;

@Service
public class CommanCsvReader {

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	CrmValidator validator = new CrmValidator();
	private static final long serialVersionUID = 451478670043548529L;
	private static Logger logger = LoggerFactory.getLogger(CommanCsvReader.class.getName());

	// 1. parse batch file for refund
	public BatchResponseObject createRefundList(String fileName) {
		List<BatchTransactionObj> refundList = new LinkedList<BatchTransactionObj>();
		BatchResponseObject batchResponseObject = new BatchResponseObject();
		// Build reader instance
		CSVReader csvReader;
		StringBuilder message = new StringBuilder();
		String line = "";
		try {
			// Start reading from line number 2 (line numbers start from zero)
			csvReader = new CSVReader(new FileReader(fileName), '\t', '\'', 1);
			String[] nextLine;
			try {
				while ((nextLine = csvReader.readNext()) != null) {
					try {
						line = Arrays.toString(nextLine).replace("[", "").replace("]", "");
						String[] lineobj = line.split(",");
						// prepare refund fields
						BatchTransactionObj refund = createRefund(lineobj);
						// Verifying the file data here
						if (validateBatchFileRefund(refund)) {
							refundList.add(refund);
						} else {
							message.append(ErrorType.VALIDATION_FAILED.getResponseMessage() + " order id: "
									+ refund.getOrderId());
							continue;
						}
					} catch (Exception exception) {
						logger.error("Exception", exception);
					}
				}
			} catch (IOException exception) {
				logger.error("Exception", exception);
			}
		} catch (FileNotFoundException exception) {
			logger.error("Exception", exception);
		}
		batchResponseObject.setResponseMessage(message.toString());
		batchResponseObject.setBatchTransactionList(refundList);
		return batchResponseObject;
	}

	public boolean validateBatchFileRefund(BatchTransactionObj refund) {

		if ((validator.validateBlankField(refund.getPayId()))) {
		} else if (!validator.validateField(CrmFieldType.PAY_ID, refund.getPayId())) {
			return false;
		}

		if ((validator.validateBlankField(refund.getOrigTxnId()))) {
		} else if (!validator.validateField(CrmFieldType.TXN_ID, refund.getOrigTxnId())) {
			return false;
		}
		if ((validator.validateBlankField(refund.getAmount()))) {
		} else if (!validator.validateField(CrmFieldType.AMOUNT, refund.getAmount())) {
			return false;
		}
		if ((validator.validateBlankField(refund.getTxnType()))) {
		} else if (!validator.validateField(CrmFieldType.TXNTYPE, refund.getTxnType())) {
			return false;
		}
		if ((validator.validateBlankField(refund.getCurrencyCode()))) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, refund.getCurrencyCode())) {
			return false;
		}
		if ((validator.validateBlankField(refund.getOrderId()))) {
		} else if (!validator.validateField(CrmFieldType.ORDER_ID, refund.getOrderId())) {
			return false;
		}
		return true;

	}

	public Map<String, String> fieldsRequestMap(BatchTransactionObj obj, User sessionUser) {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(FieldType.ORIG_TXN_ID.getName(), obj.getOrigTxnId());
		requestMap.put(FieldType.TXNTYPE.getName(), obj.getTxnType());
		requestMap.put(FieldType.AMOUNT.getName(), obj.getAmount());
		requestMap.put(FieldType.CURRENCY_CODE.getName(), obj.getCurrencyCode());
		requestMap.put(FieldType.HASH.getName(), "1234567890123456789012345678901234567890123456789012345678901234");
		requestMap.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
		requestMap.put(FieldType.INTERNAL_TXN_CHANNEL.getName(), TxnChannel.EXTERNAL_BATCH_FILE.getCode());
		requestMap.put(FieldType.INTERNAL_USER_EMAIL.getName(), sessionUser.getEmailId());

		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
			requestMap.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
		} else {
			requestMap.put(FieldType.PAY_ID.getName(), obj.getPayId());
		}
		return requestMap;
	}

	private static BatchTransactionObj createRefund(String[] data) {
		return new BatchTransactionObj(data[0], data[1], data[2], data[3], data[4], data[5]);
	}

	// 2. batch file status update
	public LinkedList<StatusEnquiryParameters> prepareStatusUpdateList(String fileName) {

		List<StatusEnquiryParameters> statusEnquiryParametersList = new LinkedList<StatusEnquiryParameters>();
		/* CSVReader csvReader; */
		String line = "";
		try {
			// Start reading from line number 2 (line numbers start from zero)
			CSVReader csvReader = new CSVReader(new FileReader(fileName), '\t', '\'', 1);
			String[] nextLine;
			try {
				while ((nextLine = csvReader.readNext()) != null) {
					try {
						line = Arrays.toString(nextLine).replace("[", "").replace("]", "");
						String[] lineobj = line.split(",");
						StatusEnquiryParameters statusEnquiryParameters = statusEnquiryParameters(lineobj);
						statusEnquiryParametersList.add(statusEnquiryParameters);

					} catch (Exception exception) {
						logger.error("Exception", exception);
					}
				}

			} catch (IOException exception) {
				logger.error("Exception", exception);
			}
		} catch (FileNotFoundException exception) {
			logger.error("Exception", exception);
		}
		return (LinkedList<StatusEnquiryParameters>) statusEnquiryParametersList;
	}

	public static StatusEnquiryParameters statusEnquiryParameters(String data[]) {
		return new StatusEnquiryParameters(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7],
				data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17],
				data[18], data[19]);
	}

	public Map<String, String> prepareFields(StatusEnquiryParameters statusEnquiryParameters) {
		Map<String, String> requestMap = new HashMap<String, String>();

		requestMap.put(FieldType.PAY_ID.getName(), statusEnquiryParameters.getPayId());
		requestMap.put(FieldType.TXN_ID.getName(), statusEnquiryParameters.getTxnId());
		requestMap.put(FieldType.TXNTYPE.getName(), TransactionType.STATUS.getName());
		requestMap.put(FieldType.ACQUIRER_TYPE.getName(), statusEnquiryParameters.getAcquirerType());
		requestMap.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), statusEnquiryParameters.getTxnType());
		requestMap.put(FieldType.CURRENCY_CODE.getName(), statusEnquiryParameters.getCurrencyCode());
		requestMap.put(FieldType.OID.getName(), statusEnquiryParameters.getOrderId());
		requestMap.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), statusEnquiryParameters.getOrigTxnId());
		requestMap.put(FieldType.PG_DATE_TIME.getName(), statusEnquiryParameters.getPgDateTime());
		requestMap.put(FieldType.STATUS.getName(), statusEnquiryParameters.getStatus());
		requestMap.put(FieldType.RESPONSE_CODE.getName(), statusEnquiryParameters.getResponseCode());
		requestMap.put(FieldType.RESPONSE_MESSAGE.getName(), statusEnquiryParameters.getResponseMessage());
		requestMap.put(FieldType.ACQ_ID.getName(), statusEnquiryParameters.getAcqId());
		requestMap.put(FieldType.PG_REF_NUM.getName(), statusEnquiryParameters.getPgRefNum());
		requestMap.put(FieldType.AUTH_CODE.getName(), statusEnquiryParameters.getAuthCode());
		requestMap.put(FieldType.PG_RESP_CODE.getName(), statusEnquiryParameters.getPgRespCode());
		requestMap.put(FieldType.PG_TXN_MESSAGE.getName(), statusEnquiryParameters.getPgTxnMessage());
		requestMap.put(FieldType.PG_DATE_TIME.getName(), statusEnquiryParameters.getPgDateTime());
		requestMap.put(FieldType.CURRENCY_CODE.getName(), statusEnquiryParameters.getCurrencyCode());

		return requestMap;
	}

	// 3. batch file for invoice create
	public BatchResponseObject csvReaderForBatchEmailSend(String fileName) {
		// Build reader instance
		CSVReader csvReader;
		List<Invoice> emailPhoneList = new LinkedList<Invoice>();
		BatchResponseObject batchResponseObject = new BatchResponseObject();
		StringBuilder message = new StringBuilder();
		String line = "";

		try {
			// Start reading from line number 2 (line numbers start from zero)
			csvReader = new CSVReader(new FileReader(fileName), '\t', '\'', 1);
			String[] nextLine;

			try {
				while ((nextLine = csvReader.readNext()) != null) {
					try {
						line = Arrays.toString(nextLine).replace("[", "").replace("]", "");
						String lineobj[] = line.split(",");

						Invoice batchInvoice = batchInvoiceSend(lineobj);

						if (validator.validateBatchEmailPhone(batchInvoice)) {
							emailPhoneList.add(batchInvoice);
						} else {
							message.append(ErrorType.VALIDATION_FAILED.getResponseMessage() + " For This Email Id: "
									+ batchInvoice.getEmail());
							continue;
						}
					} catch (Exception exception) {
						message.append(ErrorType.VALIDATION_FAILED.getResponseMessage() + " Email Id: " + line);
						logger.error("Exception", exception);
					}
				}
			} catch (IOException exception) {
				logger.error("Exception", exception);
			}
		} catch (FileNotFoundException exception) {
			logger.error("Exception", exception);
		}
		batchResponseObject.setResponseMessage(message.toString());
		batchResponseObject.setInvoiceEmailList(emailPhoneList);
		return batchResponseObject;
	}

	private static Invoice batchInvoiceSend(String[] data) {

		return new Invoice(data[0], data[1]);
	}

	public BatchResponseObject csvReaderForBinRange(String fileName) {
		CSVReader csvReader;
		List<BinRange> binRangeList = new LinkedList<BinRange>();
		BatchResponseObject batchResponseObject = new BatchResponseObject();
		StringBuilder message = new StringBuilder();
		String line = "";
		try {
			// Start reading from line number 2 (line numbers start from zero)
			csvReader = new CSVReader(new FileReader(fileName), '\t', '\'', 1);
			String[] nextLine;

			try {

				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue()));

				while ((nextLine = csvReader.readNext()) != null) {
					try {
						line = Arrays.toString(nextLine).replace("[", "").replace("]", "");
						String lineobj[] = line.split(",");
						if (lineobj.length != 12) {
							continue;
						}
						BinRange binRangeObject = batchBinRange(lineobj);

						if (validator.validateBinRangeFile(binRangeObject)) {

							binRangeList.add(binRangeObject);

							/*
							 * BasicDBObject dupQuery = new
							 * BasicDBObject("BIN_CODE_HIGH",binRangeObject.getBinCodeHigh()); if
							 * (coll.count(dupQuery) < 1) { binRangeList.add(binRangeObject); } else {
							 * logger.info("Duplicate Bin Code High Skipped >>> "
							 * +binRangeObject.getBinCodeHigh()); }
							 */

						} else {
							message.append(ErrorType.VALIDATION_FAILED.getResponseMessage() + " Id: "
									+ binRangeObject.getId());
							continue;
						}
					} catch (Exception exception) {
						message.append(ErrorType.VALIDATION_FAILED.getResponseMessage() + " Id: " + line);
						logger.error("Exception", exception);
					}
				}
			} catch (IOException exception) {
				logger.error("Exception", exception);
			}
		} catch (FileNotFoundException exception) {
			logger.error("Exception", exception);
		}
		batchResponseObject.setResponseMessage(message.toString());
		batchResponseObject.setBinRangeResponseList(binRangeList);
		return batchResponseObject;
	}

	private BinRange batchBinRange(String[] data) {
		return new BinRange(data[0], data[1], data[2], data[3], PaymentType.getInstanceUsingCode(data[4]), data[5],
				data[6], data[7], MopType.getInstanceIgnoreCase(data[8]), data[9], data[10], data[11]);
	}

}
