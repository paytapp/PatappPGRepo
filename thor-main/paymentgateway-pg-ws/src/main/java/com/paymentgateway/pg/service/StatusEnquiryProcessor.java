package com.paymentgateway.pg.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.P2PTSP.P2PTSPService;
import com.paymentgateway.airPay.AirPayStatusEnquiryProcessor;
import com.paymentgateway.apexPay.ApexPayStatusEnquiryProcessor;
import com.paymentgateway.axisbank.upi.AxisBankUpiStatusEnquiryProcessor;
import com.paymentgateway.billDesk.BillDeskStatusEnquiryProcessor;
import com.paymentgateway.bob.BobStatusEnquiryProcessor;
import com.paymentgateway.cashfree.CashfreeStatusEnquiryProcessor;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.floxypay.FloxypayStatusEnquiryProcessor;
import com.paymentgateway.fssPay.FssPayStatusEnquiryProcessor;
import com.paymentgateway.globalpay.GlobalpayStatusEnquiryProcessor;
import com.paymentgateway.grezpay.GrezpayStatusEnquiryProcessor;
import com.paymentgateway.hdfc.upi.HdfcUpiStatusEnquiryProcessor;
import com.paymentgateway.iciciUpi.IciciUpiStatusEnquiryProcessor;
import com.paymentgateway.idfcUpi.IdfcUpiEnquiryProcessor;
import com.paymentgateway.ipint.IpintStatusEnquiryProcessor;
import com.paymentgateway.isgpay.ISGPayStatusEnquiryProcessor;
import com.paymentgateway.payphi.PayphiStatusEnquiryProcessor;
import com.paymentgateway.payu.PayuStatusEnquiryProcessor;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.security.SecurityProcessor;
import com.paymentgateway.qaicash.QaicashStatusEnquiryProcessor;
import com.paymentgateway.razorpay.RazorpayStatusEnquiryProcessor;
import com.paymentgateway.toshanidigital.ToshanidigitalStatusEnquiryProcessor;
import com.paymentgateway.upigateway.UpigatewayStatusEnquiryProcessor;
import com.paymentgateway.vepay.VepayStatusEnquiryProcessor;
import com.paymentgateway.yesbankcb.YesBankUpiStatusEnquiryProcessor;

@Service
public class StatusEnquiryProcessor {

	private static Logger logger = LoggerFactory.getLogger(StatusEnquiryProcessor.class.getName());
	private static final String prefix = "MONGO_DB_";
	private static final Collection<String> allDBRequestFields = SystemProperties.getAllDBRequestFields();
	private static final Collection<String> aLLDB_Fields = SystemProperties.getDBFields();

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private SecurityProcessor securityProcessor;

	@Autowired
	private BobStatusEnquiryProcessor bobStatusEnquiryProcessor;

	@Autowired
	private HdfcUpiStatusEnquiryProcessor hdfcUpiStatusEnquiryProcessor;

	@Autowired
	private ISGPayStatusEnquiryProcessor iSGPayStatusEnquiryProcessor;

	@Autowired
	private FssPayStatusEnquiryProcessor fssPayStatusEnquiryProcessor;

	@Autowired
	private IciciUpiStatusEnquiryProcessor iciciUpiStatusEnquiryProcessor;

	@Autowired
	private IdfcUpiEnquiryProcessor idfcUpiEnquiryProcessor;

	@Autowired
	private BillDeskStatusEnquiryProcessor billDeskStatusEnquiryProcessor;

	@Autowired
	private PayphiStatusEnquiryProcessor payphiStatusEnquiryProcessor;

	@Autowired
	private PayuStatusEnquiryProcessor payuStatusEnquiryProcessor;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private YesBankUpiStatusEnquiryProcessor yesBankUpiStatusEnquiryProcessor;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private CashfreeStatusEnquiryProcessor cashfreeStatusEnquiryProcessor;

	@Autowired
	private AxisBankUpiStatusEnquiryProcessor axisBankUpiStatusEnquiryProcessor;
	
	@Autowired
	private ApexPayStatusEnquiryProcessor apexPayStatusEnquiryProcessor;

	@Autowired
	private VepayStatusEnquiryProcessor vepayStatusEnquiryProcessor;
	
	@Autowired
	private AirPayStatusEnquiryProcessor airPayStatusEnquiryProcessor;
	
	@Autowired
	private RazorpayStatusEnquiryProcessor razorpayStatusEnquiryProcessor;
	
	@Autowired
	private QaicashStatusEnquiryProcessor qaicashStatusEnquiryProcessor;
	
	@Autowired
	private FloxypayStatusEnquiryProcessor floxypayStatusEnquiryProcessor;
	
	@Autowired
	private GrezpayStatusEnquiryProcessor grezpayStatusEnquiryProcessor;
	
	@Autowired
	private UpigatewayStatusEnquiryProcessor upigatewayStatusEnquiryProcessor;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private IpintStatusEnquiryProcessor ipintStatusEnquiryProcessor;

	@Autowired
	private ToshanidigitalStatusEnquiryProcessor toshaniStatusEnquiryProcessor;
	
	@Autowired
	private GlobalpayStatusEnquiryProcessor globalpayStatusEnquiryProcessor;
	
	@Autowired
	private P2PTSPService p2ptspService;
	
	public Map<String, String> process(Fields fields) throws SystemException {
		
		boolean iSTxnFound = getTransactionFields(fields);

		if (StringUtils.isBlank(fields.get(FieldType.ACQUIRER_TYPE.getName()))) {

			logger.info(
					"Acquirer not found for status enquiry , pgRef == " + fields.get(FieldType.PG_REF_NUM.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "No Such Transaction found");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getCode());

			if (StringUtils.isNotBlank(fields.get(FieldType.PG_REF_NUM.getName()))
					&& !fields.get(FieldType.PG_REF_NUM.getName()).equalsIgnoreCase("0")) {
				updateStatusFinal(fields);
			}

			return fields.getFields();
		}

		String stausOFTxn = fields.get(FieldType.STATUS.getName());

		if (stausOFTxn.equalsIgnoreCase(StatusType.CAPTURED.getName())
				|| stausOFTxn.equalsIgnoreCase(StatusType.SETTLED.getName())) {

			logger.info("Transaction already captured , pgRef == " + fields.get(FieldType.PG_REF_NUM.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.PROCESSED.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Transaction already success");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ALREADY_CAPTURED_TRANSACTION.getCode());
			return fields.getFields();
		}

		// Check if the Order Id has Captured Entry

		if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))
				&& verifyOrderCapture(fields.get(FieldType.ORDER_ID.getName()))) {

			logger.info("Transaction already captured , ORDER_ID == " + fields.get(FieldType.ORDER_ID.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.PROCESSED.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Transaction already success");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ALREADY_CAPTURED_TRANSACTION.getCode());
			return fields.getFields();
		}

		if (!iSTxnFound) {

			logger.info("Transaction not found for status enquiry , pgRef == "
					+ fields.get(FieldType.PG_REF_NUM.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "No Such Transaction found");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getCode());
			return fields.getFields();
		}
		securityProcessor.addAcquirerFields(fields);

		AcquirerType acquirerType = AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()));
		switch (acquirerType) {
		case BOB:
			bobStatusEnquiryProcessor.enquiryProcessor(fields); // BOB
			break;
		case FSS:
			hdfcUpiStatusEnquiryProcessor.enquiryProcessor(fields); // HDFC
			break;
		case ISGPAY:
			iSGPayStatusEnquiryProcessor.enquiryProcessor(fields); // ISGPay
			break;
		case FSSPAY:
			fssPayStatusEnquiryProcessor.enquiryProcessor(fields); // FSSPAY
			break;
		case ICICIUPI:
			iciciUpiStatusEnquiryProcessor.enquiryProcessor(fields); // ICICI_UPI
			break;
		case IDFCUPI:
			idfcUpiEnquiryProcessor.enquiryProcessor(fields); // IDFC UPI
			break;
		case BILLDESK:
			billDeskStatusEnquiryProcessor.enquiryProcessor(fields); // Billdesk
			break;
		case PAYPHI:
			payphiStatusEnquiryProcessor.enquiryProcessor(fields); // Payphi
			break;
		case PAYU:
			payuStatusEnquiryProcessor.enquiryProcessor(fields); // PayU
			break;
		case CASHFREE:
			cashfreeStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case YESBANKCB:
			yesBankUpiStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case AXISBANK:
			axisBankUpiStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case APEXPAY:
			apexPayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case VEPAY:
			vepayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;	
		case AIRPAY:
			airPayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case RAZORPAY:
			razorpayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case QAICASH:	
			qaicashStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case FLOXYPAY:
			floxypayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case GREZPAY:
			grezpayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case UPIGATEWAY:
			upigatewayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case TOSHANIDIGITAL:
			toshaniStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case GLOBALPAY:
			globalpayStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case P2PTSP:
			try {
				p2ptspService.enquiryProcessor(fields);
			} catch (IOException e) {
				logger.info("Exception while udpating P2PTSPservice status enquiry", e);
			}
			break;
			
		case IPINT:
			try {
				ipintStatusEnquiryProcessor.enquiryProcessor(fields);
			} catch (Exception e) {
				logger.info("Exception while udpating ipintStatusEnquiryProcessor status enquiry", e);
			}
		break;

		default:

			Map<String, String> failedTxnField = fields.getFields();
			failedTxnField.put(FieldType.STATUS.getName(), "");
			return fields.getFields();
		}

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.removeSecureFields();

		if (StringUtils.isNotBlank(fields.getFields().get(FieldType.STATUS.getName()))
				&& !fields.getFields().get(FieldType.STATUS.getName()).equalsIgnoreCase(stausOFTxn)) {

			if (!fields.getFields().get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.INVALID.getName())) {
				updateTxnData(fields);
			}

		}

		// Update Is Status Final Flag after Status Enquiry
		fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");
		updateFinalFlag(fields);
		return fields.getFields();
	}

	public void updateTxnData(Fields fields) {

		try {

			Fields newFields = createAllForRefund(fields);
			String txnId = TransactionManager.getNewTransactionId();
			newFields.put(FieldType.TXN_ID.getName(), txnId);
			newFields.put("_id", txnId);

			Map<String, String> fieldsMap = fields.getFields();

			for (String key : fieldsMap.keySet()) {

				if (key.equalsIgnoreCase(FieldType.TXN_ID.getName()) || key.equalsIgnoreCase("_id")
						|| key.equalsIgnoreCase(FieldType.TXNTYPE.getName())) {
					continue;
				}

				newFields.put(key, fieldsMap.get(key));
			}

			newFields.put(FieldType.ORIG_TXNTYPE.getName(), newFields.get(FieldType.TXNTYPE.getName()));
			newFields.put(FieldType.TXN_CAPTURE_FLAG.getName(), Constants.TXN_ENQUIRY.getValue());

			// This has been done to enter the same date as the enroll / sent to bank date.
			// If Fields Dao is used , it will generate a new date and enter that date
			// If status enquiry is done after 12 AM for transaction before 12 AM , dates on
			// enroll / sent to bank and Capture will be different.
			insertTransaction(newFields);
		} catch (Exception e) {
			logger.error("Exception while udpating new txn data from status enquiry", e);
		}
	}

	public void updateFinalFlag(Fields fields) {

		try {

			String _id = null;

			// Fetch Final Entry in transaction for SALE
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			String oid = fields.get(FieldType.OID.getName());

			BasicDBObject oidQuery = new BasicDBObject(FieldType.OID.getName(), oid);
			BasicDBObject saleQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(saleQuery);
			condList.add(oidQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", condList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				_id = dbobj.get("_id").toString();
			}

			// Update Entry with Is Status Final Flag

			Document query = new Document();
			query.append("_id", _id);
			Document setData = new Document();
			setData.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
			Document update = new Document();
			update.append("$set", setData);
			coll.updateOne(query, update);

		} catch (Exception e) {
			logger.error("Exception while udpating is status final flag", e);
		}
	}

	public void updateStatusFinal(Fields fields) {

		try {

			String _id = null;

			// Fetch Final Entry in transaction for SALE
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			String oid = fields.get(FieldType.OID.getName());

			BasicDBObject oidQuery = new BasicDBObject(FieldType.OID.getName(), oid);

			BasicDBObject saleQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject newOrderQuery = new BasicDBObject(FieldType.TXNTYPE.getName(),
					TransactionType.NEWORDER.getName());

			List<BasicDBObject> txnTypeList = new ArrayList<BasicDBObject>();

			txnTypeList.add(saleQuery);
			txnTypeList.add(newOrderQuery);

			BasicDBObject txnTypeQuery = new BasicDBObject("$or", txnTypeList);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(txnTypeQuery);
			condList.add(oidQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", condList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			// Update Entry with Is Status Final Flag as Y
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				_id = dbobj.get("_id").toString();

				Document query = new Document();
				query.append("_id", _id);
				Document setData = new Document();
				setData.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
				Document update = new Document();
				update.append("$set", setData);
				coll.updateOne(query, update);

			}

		} catch (Exception e) {
			logger.error("Exception while udpating is status final flag", e);
		}
	}

	public boolean getTransactionFields(Fields fields) {

		try {
			BasicDBObject finalquery = new BasicDBObject(FieldType.PG_REF_NUM.getName(),
					fields.get(FieldType.PG_REF_NUM.getName()));

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			if (!cursor.hasNext()) {
				return false;
			} else {
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
						fields.put(FieldType.PAYMENT_TYPE.getName(),
								dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					}

					if (null != dbobj.getString(FieldType.TXNTYPE.toString())) {
						fields.put(FieldType.ORIG_TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.toString()));
					}

					if (null != dbobj.getString(FieldType.PAY_ID.toString())) {
						fields.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.toString()));
					}

					if (null != dbobj.getString(FieldType.ORDER_ID.toString())) {
						fields.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.toString()));
					}

					if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
						fields.put(FieldType.ACQUIRER_TYPE.getName(),
								dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
					}

					if (null != dbobj.getString(FieldType.CURRENCY_CODE.toString())) {
						fields.put(FieldType.CURRENCY_CODE.getName(),
								dbobj.getString(FieldType.CURRENCY_CODE.toString()));
					}

					if (null != dbobj.getString(FieldType.AMOUNT.toString())) {
						fields.put(FieldType.AMOUNT.getName(), dbobj.getString(FieldType.AMOUNT.toString()));
					}

					if (null != dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) {
						fields.put(FieldType.TOTAL_AMOUNT.getName(),
								dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					}

					if (null != dbobj.getString(FieldType.CREATE_DATE.toString())) {
						fields.put(FieldType.CREATE_DATE.getName(), dbobj.getString(FieldType.CREATE_DATE.toString()));
					}

					fields.put(FieldType.TXNTYPE.getName(), TransactionType.STATUS.getName());

					if (null != dbobj.getString(FieldType.STATUS.toString())) {
						fields.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.toString()));
					}

					if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
						fields.put(FieldType.MOP_TYPE.getName(), dbobj.get(FieldType.MOP_TYPE.toString()).toString());
					}

					if (null != dbobj.getString(FieldType.ORIG_TXN_ID.toString())) {
						fields.put(FieldType.ORIG_TXN_ID.getName(), dbobj.getString(FieldType.ORIG_TXN_ID.toString()));
					}

					if (null != dbobj.getString(FieldType.ACQ_ID.toString())) {
						fields.put(FieldType.ACQ_ID.getName(), dbobj.getString(FieldType.ACQ_ID.toString()));
					}

					if (null != dbobj.getString(FieldType.OID.toString())) {
						fields.put(FieldType.OID.getName(), dbobj.getString(FieldType.OID.toString()));
					}

					fields.put(FieldType.TRANSACTION_MODE.getName(),
							dbobj.getString(FieldType.TRANSACTION_MODE.toString()));

				}

				return true;
			}

		} catch (Exception e) {
			logger.error("Exception while fetching transaction for status enquiry", e);
		}
		return false;
	}

	private Fields createAllForRefund(Fields field) {

		Fields fields = new Fields();

		try {
			BasicDBObject finalquery = new BasicDBObject(FieldType.PG_REF_NUM.getName(),
					field.get(FieldType.PG_REF_NUM.getName()));

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					for (int j = 0; j < documentObj.size(); j++) {
						for (String columnName : aLLDB_Fields) {
							if (documentObj.get(columnName) != null) {
								fields.put(columnName, documentObj.get(columnName).toString());
							} else {

							}

						}
					}
				}
				fields.logAllFields("Previous fields");
			}
			cursor.close();
			return fields;
		}

		catch (Exception e) {
			logger.error("Exception while getting previous fields", e);
		}
		return null;

	}

	public void insertTransaction(Fields fields) throws SystemException {
		try {

			MongoDatabase dbIns = null;
			BasicDBObject newFieldsObj = new BasicDBObject();

			if (StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {

				if (StringUtils.isNotBlank(fields.get(FieldType.PG_REF_NUM.getName()))
						&& fields.get(FieldType.PG_REF_NUM.getName()).equalsIgnoreCase("0")) {

					logger.info("Skipping update of txn with 0 Pg Ref Num in DB");
					return;

				}
			}

			// Check to see if a transaction is already added in DB with Same RRN and status
			// as Captured and Skip
			// that entry
			// Check to see if a transaction is already added in DB with Same RRN and status
			// as Captured and Skip
			// that entry
			if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.RRN.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.MOP_TYPE.getName()))) {

				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
						&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
						&& fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())
						&& fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.STATIC_UPI_QR.getCode())) {

					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
					saleConList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
					saleConList.add(new BasicDBObject(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName())));
					saleConList
							.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

					BasicDBObject saleCaptureQuery = new BasicDBObject("$and", saleConList);

					dbIns = mongoInstance.getDB();
					MongoCollection<Document> collection = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

					long count = collection.count(saleCaptureQuery);

					if (count > 0) {
						logger.info(
								"From Status Enquiry Processor , skipping duplicate capture entry for transaction with Same RRN = "
										+ fields.get(FieldType.RRN.getName()) + " and Order ID = "
										+ fields.get(FieldType.ORDER_ID.getName()));
						fields.logAllFields("Skipped Entry of this field because of duplicate RRN");
						return;
					}

				}

			}

			String amountString = fields.get(FieldType.AMOUNT.getName());
			String surchargeAmountString = fields.get(FieldType.SURCHARGE_AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String totalAmountString = fields.get(FieldType.TOTAL_AMOUNT.getName());

			if (!amountString.contains(".")) {
				amountString = Amount.toDecimal(amountString, fields.get(FieldType.CURRENCY_CODE.getName()));
			}

			if (StringUtils.isNotBlank(surchargeAmountString)) {
				if (!surchargeAmountString.contains(".")) {
					surchargeAmountString = Amount.toDecimal(surchargeAmountString,
							fields.get(FieldType.CURRENCY_CODE.getName()));
				}
			}

			if (StringUtils.isNotBlank(amountString)) {
				newFieldsObj.put(FieldType.AMOUNT.getName(), amountString);
			}

			if (StringUtils.isNotBlank(totalAmountString)) {
				newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(), totalAmountString);
			}

			String surchargeAmount = "0";
			if (!StringUtils.isEmpty(surchargeAmountString) && !StringUtils.isEmpty(currencyString)) {

				if (!surchargeAmountString.contains(".")) {
					surchargeAmount = Amount.toDecimal(surchargeAmountString, currencyString);
					newFieldsObj.put(FieldType.SURCHARGE_AMOUNT.getName(), surchargeAmount);
				} else {
					newFieldsObj.put(FieldType.SURCHARGE_AMOUNT.getName(), surchargeAmount);
				}
			}

			String origTxnId = "0";
			String origTxnStr = fields.get(FieldType.ORIG_TXN_ID.getName());
			if (StringUtils.isEmpty(origTxnStr)) {
				String internalOrigTxnStr = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
				if (StringUtils.isEmpty(internalOrigTxnStr)) {
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
				}
				if (!StringUtils.isEmpty(internalOrigTxnStr)) {
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), internalOrigTxnStr);
				}
			}

			if (!StringUtils.isEmpty(origTxnStr)) {
				newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), origTxnStr);
			}

			String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());

			if (origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
				origTxnType = fields.get(FieldType.TXNTYPE.getName());
			}

			if (!StringUtils.isEmpty(origTxnType)) {
				String txnType = fields.get(FieldType.TXNTYPE.getName());
				if ((txnType.equals(TransactionType.REFUND.getName()))
						|| (txnType.equals(TransactionType.REFUNDRECO.getName()))) {
					newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
				} else {
					newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
				}

			}
			String pgRefNo = "0";
			String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			if (StringUtils.isEmpty(pgRefNum)) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNo);
			}

			if (!StringUtils.isEmpty(pgRefNum)) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			}
			String acctId = "0";
			String acctIdStr = fields.get(FieldType.ACCT_ID.getName());
			if (acctIdStr != null && acctIdStr.length() > 0) {

				newFieldsObj.put(FieldType.ACCT_ID.getName(), acctIdStr);
			}
			if (acctIdStr == null) {
				newFieldsObj.put(FieldType.ACCT_ID.getName(), acctId);
			}
			String acqId = "0";
			String acqIdStr = fields.get(FieldType.ACQ_ID.getName());
			if (acqIdStr != null && acqIdStr.length() > 0) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), acqIdStr);
			}
			if (acqIdStr == null) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), acqId);
			}
			String oid = fields.get(FieldType.OID.getName());
			String longOid = "0";
			if (!StringUtils.isEmpty(oid)) {

				newFieldsObj.put(FieldType.OID.getName(), oid);
			}
			if (StringUtils.isEmpty(oid)) {
				newFieldsObj.put(FieldType.OID.getName(), longOid);
			}
			String udf1 = fields.get(FieldType.UDF1.getName());
			if (!StringUtils.isEmpty(udf1)) {
				newFieldsObj.put(FieldType.UDF1.getName(), udf1);
			}
			String udf2 = fields.get(FieldType.UDF2.getName());
			if (!StringUtils.isEmpty(udf2)) {
				newFieldsObj.put(FieldType.UDF2.getName(), udf2);
			}
			String udf3 = fields.get(FieldType.UDF3.getName());
			if (!StringUtils.isEmpty(udf3)) {
				newFieldsObj.put(FieldType.UDF3.getName(), udf3);
			}
			String udf4 = fields.get(FieldType.UDF4.getName());
			if (!StringUtils.isEmpty(udf4)) {
				newFieldsObj.put(FieldType.UDF4.getName(), udf4);
			}
			String udf5 = fields.get(FieldType.UDF5.getName());
			if (!StringUtils.isEmpty(udf5)) {
				newFieldsObj.put(FieldType.UDF5.getName(), udf5);
			}
			String udf6 = fields.get(FieldType.UDF6.getName());
			if (!StringUtils.isEmpty(udf6)) {
				newFieldsObj.put(FieldType.UDF6.getName(), udf6);
			}

			String paymentsRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
			if (!StringUtils.isEmpty(paymentsRegion)) {
				newFieldsObj.put(FieldType.PAYMENTS_REGION.getName(), paymentsRegion);
			}

			String cardHolderType = fields.get(FieldType.CARD_HOLDER_TYPE.getName());
			if (!StringUtils.isEmpty(cardHolderType)) {
				newFieldsObj.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);
			}

			String requestDate = fields.get(FieldType.REQUEST_DATE.getName());
			if (!StringUtils.isEmpty(requestDate)) {
				newFieldsObj.put(FieldType.REQUEST_DATE.getName(), requestDate);
			}

			String txnEnquiry = fields.get(FieldType.TXN_CAPTURE_FLAG.getName());
			if (!StringUtils.isEmpty(txnEnquiry)) {
				newFieldsObj.put(FieldType.TXN_CAPTURE_FLAG.getName(), txnEnquiry);
			}

			newFieldsObj.put(FieldType.TRANSACTION_MODE.getName(), fields.get(FieldType.TRANSACTION_MODE.getName()));

			String saleAmount = fields.get(FieldType.SALE_AMOUNT.getName());

			if ((!StringUtils.isEmpty(saleAmount) && !StringUtils.isEmpty(currencyString))) {

				if (!saleAmount.contains(".")) {
					saleAmount = Amount.toDecimal(saleAmount, currencyString);
					newFieldsObj.put(FieldType.SALE_AMOUNT.getName(), saleAmount);
				} else {
					newFieldsObj.put(FieldType.SALE_AMOUNT.getName(), saleAmount);
				}

			}

			String totalSaleAmount = fields.get(FieldType.SALE_TOTAL_AMOUNT.getName());
			if ((!StringUtils.isEmpty(totalSaleAmount) && !StringUtils.isEmpty(currencyString))) {

				if (!totalSaleAmount.contains(".")) {
					totalSaleAmount = Amount.toDecimal(totalSaleAmount, currencyString);
					newFieldsObj.put(FieldType.SALE_TOTAL_AMOUNT.getName(), totalSaleAmount);
				} else {
					newFieldsObj.put(FieldType.SALE_TOTAL_AMOUNT.getName(), totalSaleAmount);
				}

			}

			Date createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(fields.get(FieldType.CREATE_DATE.getName()));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(createDate);

			calendar.add(Calendar.SECOND, 1);

			Date later = calendar.getTime();
			// System.out.println(later);

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String createDateUpdated = dateFormat.format(later);

			Date dNow = new Date();

			for (int i = 0; i < fields.size(); i++) {

				for (String columnName : aLLDB_Fields) {

					if (columnName.equals(FieldType.CREATE_DATE.getName())) {
						newFieldsObj.put(columnName, createDateUpdated);
					} else if (columnName.equals(FieldType.DATE_INDEX.getName())) {
						newFieldsObj.put(columnName, createDateUpdated.substring(0, 10).replace("-", ""));
					} else if (columnName.equals(FieldType.UPDATE_DATE.getName())) {
						newFieldsObj.put(columnName, createDateUpdated);
					} else if (columnName.equals("_id")) {
						newFieldsObj.put(columnName, fields.get(FieldType.TXN_ID.getName()));
					} else if (columnName.equals(FieldType.INSERTION_DATE.getName())) {
						newFieldsObj.put(columnName, dNow);
					} else if (columnName.equals(FieldType.ACQUIRER_TDR_SC.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.ACQUIRER_TDR_SC.getName()));
					} else if (columnName.equals(FieldType.ACQUIRER_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.ACQUIRER_GST.getName()));
					} else if (columnName.equals(FieldType.PG_TDR_SC.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_TDR_SC.getName()));
					} else if (columnName.equals(FieldType.PG_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_GST.getName()));
					} else if (columnName.equals(FieldType.AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SURCHARGE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXN_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PG_REF_NUM.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACCT_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACQ_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.OID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF1.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF2.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF3.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF4.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF5.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF6.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PAYMENTS_REGION.getName())) {
						continue;
					} else if (columnName.equals(FieldType.CARD_HOLDER_TYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXNTYPE.getName())) {
						continue;
					} else {
						newFieldsObj.put(columnName, fields.get(columnName));
					}
				}
			}

			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);

			// Change by Sandeep, callback to the merchant of the transaction which are
			// captured by transaction equiry by scheduler
			if (fields.get(FieldType.TXN_CAPTURE_FLAG.getName()).equalsIgnoreCase("TXN Enquiry")
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("Captured")) {
				try {
					Runnable runnable = new Runnable() {

						@Override
						public void run() {
							logger.info("Callback After Status Enquiry for orderID = "
									+ fields.get(FieldType.ORDER_ID.getName()) + " And pgRef = "
									+ fields.get(FieldType.PG_REF_NUM.getName()));
							if (!fields.get(FieldType.PAY_ID.getName())
									.equalsIgnoreCase(propertiesManager.propertiesMap.get("MSEDCL_PAY_ID"))) {
								if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
										&& fields.get(FieldType.AMOUNT.getName()).contains(".")) {
									String amount = Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
											fields.get(FieldType.CURRENCY_CODE.getName()));
									fields.put(FieldType.AMOUNT.getName(), amount);
								}
								if (StringUtils.isNotBlank(fields.get(FieldType.TOTAL_AMOUNT.getName()))
										&& fields.get(FieldType.TOTAL_AMOUNT.getName()).contains(".")) {
									String amount = Amount.formatAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()),
											fields.get(FieldType.CURRENCY_CODE.getName()));
									fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
								}
							}
							fieldsDao.upiHostedBlock(fields);
							fieldsDao.sendCallbackAfterTxnEquiry(fields);

						}
					};
					propertiesManager.executorImpl(runnable);
				} catch (Exception e) {
					logger.error("Callback of Txn Status failed after status enquiry for Order Id "
							+ fields.get(FieldType.ORDER_ID.getName()));
					logger.error("Exception in Callback ", e);

				}
			}
			// Change by Shaiwal , whole transaction status update block moved inside a
			// separate try/catch
			try {
				logger.info("calling update service for order id " + fields.get(FieldType.ORDER_ID.getName()));
				Runnable runnable = new Runnable() {
					public void run() {
						logger.info("Updating Status Collection for Order Id after Status Enquiry "
								+ fields.get(FieldType.ORDER_ID.getName()));
						updateStatusColl(fields, doc);

					}
				};

				propertiesManager.executorImpl(runnable);
			} catch (Exception e) {
				logger.error("Txn Status Collection failed after status enquiry for Order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
				logger.error("Exception in updating Order status", e);

			}

		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	// New method to only show status enquiry result without updating the database,
	// this will respond even if status was success previously
	// No HASH required for this transaction

	@SuppressWarnings("incomplete-switch")
	public Map<String, String> getStatus(Fields fields) throws SystemException {

		boolean iSTxnFound = getTransactionFields(fields);

		if (StringUtils.isBlank(fields.get(FieldType.ACQUIRER_TYPE.getName()))) {

			logger.info(
					"Acquirer not found for status enquiry , pgRef == " + fields.get(FieldType.PG_REF_NUM.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "No Such Transaction found");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getCode());
			return fields.getFields();
		}

		if (!iSTxnFound) {

			logger.info("Transaction not found for status enquiry , pgRef == "
					+ fields.get(FieldType.PG_REF_NUM.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "No Such Transaction found");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getCode());
			return fields.getFields();
		}
		securityProcessor.addAcquirerFields(fields);

		AcquirerType acquirerType = AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()));
		switch (acquirerType) {
		case BOB:
			bobStatusEnquiryProcessor.enquiryProcessor(fields);
			break;
		case FSS:
			hdfcUpiStatusEnquiryProcessor.enquiryProcessor(fields); // HDFC UPI
			break;
		case ISGPAY:
			iSGPayStatusEnquiryProcessor.enquiryProcessor(fields); // ISGPay
			break;
		default:

			Map<String, String> failedTxnField = fields.getFields();
			failedTxnField.put(FieldType.STATUS.getName(), "");
			return fields.getFields();
		}

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.removeSecureFields();

		return fields.getFields();
	}

	public boolean verifyOrderCapture(String orderId) {

		boolean isCaptured = false;

		try {

			logger.info("Verify if Order Id has been captured for this orderId == " + orderId);
			BasicDBObject orderIdQuery = new BasicDBObject(FieldType.ORDER_ID.getName(), orderId);
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject statusTypeQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.CAPTURED.getName());
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(orderIdQuery);
			conditionList.add(txnTypeQuery);
			conditionList.add(statusTypeQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", conditionList);
			logger.info("Verify if Order Id has been captured for this finalquery == " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			long count = coll.countDocuments(finalquery);

			if (count > 0) {
				isCaptured = true;
			}
		}

		catch (Exception e) {
			logger.error("Exception in getting Order Status by orderId", e);
			return isCaptured;
		}

		return isCaptured;

	}

	public void updateStatusColl(Fields fields, Document document) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();

			String orderId = document.get(FieldType.ORDER_ID.getName()).toString();
			String oid = document.get(FieldType.OID.getName()).toString();
			String origTxnType = fields.get(FieldType.ORIG_TXNTYPE.getName());
			String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			String txnType = fields.get(FieldType.TXNTYPE.getName());

			if (StringUtils.isBlank(txnType)) {
				txnType = TransactionType.SALE.getName();
			}

			if (txnType.equalsIgnoreCase(TransactionType.INVALID.getName())
					|| txnType.equalsIgnoreCase(TransactionType.NEWORDER.getName())
					|| txnType.equalsIgnoreCase(TransactionType.RECO.getName())
					|| txnType.equalsIgnoreCase(TransactionType.ENROLL.getName())
					|| txnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
				txnType = TransactionType.SALE.getName();
			}

			if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())
					|| txnType.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())) {
				txnType = TransactionType.REFUND.getName();
			}

			if (StringUtils.isBlank(origTxnType)) {
				origTxnType = txnType;
			}

			if (origTxnType.equalsIgnoreCase(TransactionType.INVALID.getName())) {
				origTxnType = TransactionType.SALE.getName();
			}

			if (origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
				origTxnType = TransactionType.SALE.getName();
			}

			origTxnType = txnType;

			// SALE
			if (origTxnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
				if (StringUtils.isBlank(orderId) || StringUtils.isBlank(oid) || StringUtils.isBlank(origTxnType)) {

					logger.info("Cannot update transaction status collection for combination " + " Order Id = "
							+ orderId + " OID = " + oid + " orig Txn Type = " + origTxnType);
					logger.info("Txn cannot be added , moving to transactionStatusException ");

					Document doc = new Document();
					doc.put(FieldType.ORDER_ID.getName(), orderId);
					doc.put(FieldType.OID.getName(), oid);
					doc.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);

					MongoCollection<Document> excepColl = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_EXCEP_COLLECTION.getValue()));

					excepColl.insertOne(doc);
				} else {

					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
					dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
					dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));

					dbObjList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), origTxnType));

					BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

					FindIterable<Document> cursor = coll.find(andQuery);

					if (cursor.iterator().hasNext()) {

						String transactionStatusFields = PropertiesManager.propertiesMap.get("TransactionStatusFields");
						String transactionStatusFieldsArr[] = transactionStatusFields.split(",");

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						BasicDBObject searchQuery = andQuery;
						BasicDBObject updateFields = new BasicDBObject();

						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);

						// Added to avoid exception in report
						if (StringUtils.isBlank(statusALias)) {
							logger.info("Alias Status not resolved for order Id = " + orderId);
							statusALias = "Cancelled";
						}

						for (String key : transactionStatusFieldsArr) {

							if (status.equalsIgnoreCase(StatusType.SETTLED.getName())) {

								if ((key.equalsIgnoreCase(FieldType.DATE_INDEX.getName()))
										|| (key.equalsIgnoreCase(FieldType.CREATE_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.UPDATE_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.INSERTION_DATE.getName()))) {
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_DATE.getName()))) {
									updateFields.put(key, document.get(FieldType.CREATE_DATE.getName()));
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_FLAG.getName()))) {
									updateFields.put(key, "Y");
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_DATE_INDEX.getName()))) {
									updateFields.put(key, document.get(FieldType.DATE_INDEX.getName()));
									continue;
								}

							}

							if (document.get(key) != null) {
								updateFields.put(key, document.get(key).toString());
							} else {
								updateFields.put(key, document.get(key));
							}

						}

						updateFields.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						updateFields.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));

					} else {

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);

						// Added to avoid exception in report
						if (StringUtils.isBlank(statusALias)) {
							logger.info("Alias Status not resolved for order Id = " + orderId);
							statusALias = "Cancelled";
						}

						document.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						document.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							txnStatusColl.insertOne(dataEncDecTool.encryptDocument(document));
						} else {
							txnStatusColl.insertOne(document);
						}
						// txnStatusColl.insertOne(document);

					}

				}
			}

			// REFUND

			if (origTxnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				if (StringUtils.isBlank(pgRefNum) || StringUtils.isBlank(oid) || StringUtils.isBlank(origTxnType)) {

					logger.info("Cannot update transaction status collection for combination " + " PG REF NUM = "
							+ pgRefNum + " OID = " + oid + " orig Txn Type = " + origTxnType);
					logger.info("Txn cannot be added , moving to transactionStatusException ");

					Document doc = new Document();
					doc.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
					doc.put(FieldType.OID.getName(), oid);
					doc.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);

					MongoCollection<Document> excepColl = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_EXCEP_COLLECTION.getValue()));

					excepColl.insertOne(doc);
				} else {

					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
					dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
					dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));
					dbObjList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), origTxnType));

					BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

					FindIterable<Document> cursor = coll.find(andQuery);

					if (cursor.iterator().hasNext()) {

						String transactionStatusFields = PropertiesManager.propertiesMap.get("TransactionStatusFields");
						String transactionStatusFieldsArr[] = transactionStatusFields.split(",");

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						BasicDBObject searchQuery = andQuery;
						BasicDBObject updateFields = new BasicDBObject();

						for (String key : transactionStatusFieldsArr) {

							if (document.get(key) != null) {
								updateFields.put(key, document.get(key).toString());
							} else {
								updateFields.put(key, document.get(key));
							}

						}

						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);
						updateFields.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						updateFields.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));

					} else {

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);
						document.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						document.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							txnStatusColl.insertOne(dataEncDecTool.encryptDocument(document));
						} else {
							txnStatusColl.insertOne(document);
						}
						// txnStatusColl.insertOne(document);

					}

				}
			}

		}

		catch (Exception e) {
			logger.error("Exception in adding txn to transaction status", e);
		}

	}

	public String resolveStatus(String status) {

		if (StringUtils.isBlank(status)) {
			return status;
		} else {
			if (status.equals(StatusType.CAPTURED.getName())) {
				return "Captured";

			} else if (status.equals(StatusType.SETTLED.getName())) {
				return "Settled";

			} else if (status.equals(StatusType.PENDING.getName()) || status.equals(StatusType.SENT_TO_BANK.getName())
					|| status.equals(StatusType.ENROLLED.getName())) {
				return "Pending";

			} else if (status.equals(StatusType.BROWSER_CLOSED.getName())
					|| status.equals(StatusType.CANCELLED.getName())) {
				return "Cancelled";

			} else if (status.equals(StatusType.INVALID.getName()) || status.equals(StatusType.DUPLICATE.getName())) {
				return "Invalid";

			} else {
				return "Failed";
			}

		}
	}

}
