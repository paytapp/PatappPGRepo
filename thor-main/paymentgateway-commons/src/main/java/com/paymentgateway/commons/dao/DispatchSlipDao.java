package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.DispatchSlipDetails;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class DispatchSlipDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(DispatchSlipDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;
	
	public String insert(List<DispatchSlipDetails> slipListObj) {

		StringBuilder message = new StringBuilder();
	
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.DISPATCH_SLIP_COLLECTION_NAME.getValue()));
			
			List<Document> docList = new ArrayList<Document>();
			int count = 0 ;
			for (DispatchSlipDetails slipObj : slipListObj) {
				Document slip = new Document();
			
				slip.put("ORDER_ID", slipObj.getOrderId());
				slip.put("INVOICE_NO", slipObj.getInvoiceId());
				slip.put("CREATE_DATE", slipObj.getCreatedDate());
				slip.put("COURIER_SERVICE_PROVIDER", slipObj.getCourierServiceProvider());
				slip.put("DISPATCH_SLIP_NO", slipObj.getDispatchSlipNo());
				slip.put("PAY_ID", slipObj.getPayId());
				slip.put("PROCESSED_BY", slipObj.getProcessedBy());
				Document doc = new Document(slip);
				
				docList.add(doc);
				message.append((CrmFieldConstants.PROCESS_INITIATED_SUCCESSFULLY.getValue()));
				count = count + 1;
				if (count == 100000) {
					coll.insertMany(docList);
					docList.clear();
					logger.info("100000 slip inserted");
					count = 0;
				}
			}
			if (docList.size() > 0) {
				coll.insertMany(docList);
				logger.info(docList.size()+" slip inserted");
			}
			
			message.append("Inserted all slips Successfully");
		} catch (Exception exception) {
			message.append(ErrorType.CSV_NOT_SUCCESSFULLY_UPLOAD.getResponseMessage());
			logger.error("Error while processing dispatch slip: " , exception);
		}
		return message.toString();
	}
}
