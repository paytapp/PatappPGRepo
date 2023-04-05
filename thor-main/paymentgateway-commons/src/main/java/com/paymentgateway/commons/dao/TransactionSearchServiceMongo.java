package com.paymentgateway.commons.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Component
public class TransactionSearchServiceMongo {
	private static Logger logger = LoggerFactory.getLogger(TransactionSearchServiceMongo.class.getName());

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	public CustomerAddress getDumyCustAddress(String txnId) throws SystemException {

		CustomerAddress custAddress1 = getCustAddressBillingColl(txnId);
		CustomerAddress custAddress2 = getCustAddressTxnColl(txnId, custAddress1);
		return custAddress2;

	}

	public CustomerAddress getCustAddressBillingColl(String txnId) throws SystemException {
		// List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
		CustomerAddress custAddress = new CustomerAddress();
		MongoDatabase dbIns = mongoInstance.getDB();

		MongoCollection<Document> collection = dbIns.getCollection("billingDetails");
		MongoCursor<Document> cursor = collection.find(new BasicDBObject(FieldType.TXN_ID.getName(), txnId)).iterator();

		while (cursor.hasNext()) {
			Document mydata = cursor.next();
			 
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				mydata = dataEncDecTool.decryptDocument(mydata);
			} 

			if (mydata.getString(FieldType.CUST_NAME.toString()) != null) {
				custAddress.setCustName(mydata.getString(FieldType.CUST_NAME.toString()));
			} else {
				custAddress.setCustName(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_PHONE.toString()) != null) {
				custAddress.setCustPhone(mydata.getString(FieldType.CUST_PHONE.toString()));
			} else {
				custAddress.setCustPhone(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_STREET_ADDRESS1.toString()) != null) {
				custAddress.setCustStreetAddress1((mydata.getString(FieldType.CUST_STREET_ADDRESS1.toString())));
			} else {
				custAddress.setCustStreetAddress1(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_STREET_ADDRESS2.toString()) != null) {
				custAddress.setCustStreetAddress2(mydata.getString(FieldType.CUST_STREET_ADDRESS2.toString()));
			} else {
				custAddress.setCustStreetAddress2(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_CITY.toString()) != null) {
				custAddress.setCustCity(mydata.getString(FieldType.CUST_CITY.toString()));
			} else {
				custAddress.setCustCity(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_STATE.toString()) != null) {
				custAddress.setCustState(mydata.getString(FieldType.CUST_STATE.toString()));
			} else {
				custAddress.setCustState(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_COUNTRY.toString()) != null) {
				custAddress.setCustCountry(mydata.getString(FieldType.CUST_COUNTRY.toString()));
			} else {
				custAddress.setCustCountry(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_ZIP.toString()) != null) {
				custAddress.setCustZip(mydata.getString(FieldType.CUST_ZIP.toString()));
			} else {
				custAddress.setCustZip(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_NAME.toString()) != null) {
				custAddress.setCustShipName((mydata.getString(FieldType.CUST_SHIP_NAME.toString())));
			} else {
				custAddress.setCustShipName(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_STREET_ADDRESS1.toString()) != null) {
				custAddress
						.setCustShipStreetAddress1((mydata.getString(FieldType.CUST_SHIP_STREET_ADDRESS1.toString())));
			} else {
				custAddress.setCustShipStreetAddress1(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_STREET_ADDRESS2.toString()) != null) {
				custAddress.setCustShipStreetAddress2(mydata.getString(FieldType.CUST_SHIP_STREET_ADDRESS2.toString()));
			} else {
				custAddress.setCustShipStreetAddress2(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_CITY.toString()) != null) {
				custAddress.setCustShipCity(mydata.getString(FieldType.CUST_SHIP_CITY.toString()));
			} else {
				custAddress.setCustShipCity(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_STATE.toString()) != null) {
				custAddress.setCustShipState(mydata.getString(FieldType.CUST_SHIP_STATE.toString()));
			} else {
				custAddress.setCustShipState(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_COUNTRY.toString()) != null) {
				custAddress.setCustShipCountry(mydata.getString(FieldType.CUST_SHIP_COUNTRY.toString()));
			} else {
				custAddress.setCustShipCountry(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			if (mydata.getString(FieldType.CUST_SHIP_ZIP.toString()) != null) {
				custAddress.setCustShipZip(mydata.getString(FieldType.CUST_SHIP_ZIP.toString()));
			} else {
				custAddress.setCustShipZip(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			// customerAddressConditionLst.add(custAddress);
		}

		return custAddress;
	}

	public CustomerAddress getCustAddressTxnColl(String txnId, CustomerAddress custAddress1) throws SystemException {
		BasicDBObject conditionQuery = null;

		if (!txnId.isEmpty()) {

			conditionQuery = new BasicDBObject(FieldType.TXN_ID.getName(), txnId);
		}

		MongoDatabase dbIns = mongoInstance.getDB();

		MongoCollection<Document> collection = dbIns.getCollection("finalTest");
		MongoCursor<Document> cursor = collection.find(conditionQuery).iterator();

		while (cursor.hasNext()) {
			Document mydata = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				mydata = dataEncDecTool.decryptDocument(mydata);
			} 
			
			if (mydata.getString(FieldType.INTERNAL_TXN_AUTHENTICATION.toString()) != null) {
				custAddress1.setInternalTxnAuthentication(
						mydata.getString(FieldType.INTERNAL_TXN_AUTHENTICATION.toString()));
			} else {
				custAddress1.setInternalTxnAuthentication(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}

		}

		return custAddress1;
	}
}
