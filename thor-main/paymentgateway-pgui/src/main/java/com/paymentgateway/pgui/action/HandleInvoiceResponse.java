package com.paymentgateway.pgui.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.opensymphony.xwork2.ActionContext;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shaiwal
 *
 */

@Service
public class HandleInvoiceResponse{


	private static Logger logger = LoggerFactory.getLogger(HandleInvoiceResponse.class.getName());

	private Map<String, String> responseMap = new HashMap<String, String>();

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TransactionDetailsService transactionServiceDao;

	private static final String prefix = "MONGO_DB_";

	public void invoiceResponseHAndler(HttpServletRequest httpRequest, HttpServletResponse httpResponse){

		logger.info("Inside Handle Pay Response");

		if (httpRequest == null) {

	//		httpRequest.put(Constants.STATUS.getValue(), "Transaction Failed");
			logger.info("httpRequest is null , sending failed response");
//			return SUCCESS;

		}
		
		Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
		Map<String, String> requestMap = new HashMap<String, String>();

		for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
			try {
				requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);
			} catch (ClassCastException classCastException) {
				logger.error("Exception", classCastException);
			}
		}

	
		String payId = null;
		String imageBase64 = null;

		if (StringUtils.isNotBlank(requestMap.get("PAY_ID"))) {

			payId = String.valueOf(requestMap.get("PAY_ID"));
		}

		if (StringUtils.isNotBlank(payId)) {

			try {
				String imageAddr = System.getenv("DTECH_PROPS") + "invoiceImage/" + payId + ".png";
				
				if (new File(imageAddr).exists()) {
					BufferedImage img = ImageIO.read(new File(imageAddr));
					ByteArrayOutputStream bos = new ByteArrayOutputStream();

					ImageIO.write(img, "png", bos);
					byte[] imageBytes = bos.toByteArray();

					com.paymentgateway.commons.util.BASE64Encoder encoder = new com.paymentgateway.commons.util.BASE64Encoder();
					imageBase64 = encoder.encode(imageBytes);
					bos.close();
				}
				
			} catch (IOException e) {
				logger.error("Exception in generating base 64 for merchant logo");
			}

		}
		
		Invoice invoice = getInvoice(requestMap.get("ORDER_ID"));
		TransactionSearch transactionSearch=new TransactionSearch();
		try {
			transactionSearch=transactionServiceDao.getTransactionForInvoicePdf(requestMap.get("ORDER_ID"));
		} catch (SystemException e) {
			logger.error("exception while getting Transaction");
		}
		
		String createDate = requestMap.get(FieldType.RESPONSE_DATE_TIME.getName());

		if (StringUtils.isBlank(createDate)) {
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			createDate = dateNow;
		}
		String date = createDate.substring(0, 10);
		String time = createDate.substring(11, createDate.length());

		//sessionMap = (SessionMap<String, Object>) ActionContext.getContext().getSession();

		if (StringUtils.isNotBlank(payId)) {
			httpRequest.setAttribute("PAY_ID", payId);
		} else {
			httpRequest.setAttribute("PAY_ID", "");
		}
		
		if (StringUtils.isNotBlank(imageBase64)) {
			httpRequest.setAttribute("IMAGE_MERCHANT", imageBase64);
		} else {
			httpRequest.setAttribute("IMAGE_MERCHANT", "");
		}

		if (StringUtils.isNotBlank(requestMap.get("ORDER_ID"))) {
			httpRequest.setAttribute(Constants.ORDER_ID.getValue(), requestMap.get("ORDER_ID"));
		} else {
			httpRequest.setAttribute(Constants.ORDER_ID.getValue(), "");
		}

		if (StringUtils.isNotBlank(requestMap.get("TOTAL_AMOUNT"))) {
			httpRequest.setAttribute("TOTAL_AMOUNT", Amount.toDecimal(requestMap.get("TOTAL_AMOUNT"), "356"));
		} else if (StringUtils.isNotBlank(requestMap.get("AMOUNT"))) {
			httpRequest.setAttribute("TOTAL_AMOUNT", Amount.toDecimal(requestMap.get("AMOUNT"), "356"));
		} else {
			httpRequest.setAttribute("TOTAL_AMOUNT", "");
		}

		if (StringUtils.isNotBlank(requestMap.get("STATUS"))) {
			httpRequest.setAttribute("STATUS", requestMap.get("STATUS"));
		} else {
			httpRequest.setAttribute("STATUS", "");
		}

		if (StringUtils.isNotBlank(invoice.getName())) {
			httpRequest.setAttribute(Constants.CUST_NAME.getValue(), invoice.getName());
		} else {
			httpRequest.setAttribute(Constants.CUST_NAME.getValue(), "");
		}

		if (StringUtils.isNotBlank(requestMap.get("PG_REF_NUM"))) {
			httpRequest.setAttribute(Constants.PG_REF_NUM.getValue(), requestMap.get("PG_REF_NUM"));
		} else {
			httpRequest.setAttribute(Constants.PG_REF_NUM.getValue(), "");
		}

		String merchantName = userDao.getBusinessNameByPayId(payId);
		if (StringUtils.isNotBlank(merchantName)) {
			httpRequest.setAttribute("MERCHANT_NAME", merchantName);
		} else {
			httpRequest.setAttribute("MERCHANT_NAME", "");
		}

		if (StringUtils.isNotBlank(invoice.getInvoiceNo())) {
			httpRequest.setAttribute("INVOICE_NO", invoice.getInvoiceNo());
		} else {
			httpRequest.setAttribute("INVOICE_NO", "");
		}

		if (StringUtils.isNotBlank(invoice.getAddress())) {
			httpRequest.setAttribute("ADDRESS", invoice.getAddress());
		} else {
			httpRequest.setAttribute("ADDRESS", "");
		}

		if (StringUtils.isNotBlank(invoice.getCity())) {
			httpRequest.setAttribute("CITY", invoice.getCity());
		} else {
			httpRequest.setAttribute("CITY", "");
		}

		if (StringUtils.isNotBlank(invoice.getState())) {
			httpRequest.setAttribute("STATE", invoice.getState());
		} else {
			httpRequest.setAttribute("STATE", "");
		}

		if (StringUtils.isNotBlank(invoice.getZip())) {
			httpRequest.setAttribute("PIN", invoice.getZip());
		} else {
			httpRequest.setAttribute("PIN", "");
		}

		if (StringUtils.isNotBlank(invoice.getEmail())) {
			httpRequest.setAttribute("EMAIL", invoice.getEmail());
		} else {
			httpRequest.setAttribute("EMAIL", "");
		}

		if (StringUtils.isNotBlank(invoice.getPhone())) {
			httpRequest.setAttribute("MOBILE", invoice.getPhone());
		} else {
			httpRequest.setAttribute("MOBILE", "");
		}

		if (StringUtils.isNotBlank(date)) {
			httpRequest.setAttribute("TXN_DATE", date);
		} else {
			httpRequest.setAttribute("TXN_DATE", "");
		}

		if (StringUtils.isNotBlank(time)) {
			httpRequest.setAttribute("TXN_TIME", time);
		} else {
			httpRequest.setAttribute("TXN_TIME", "");
		}

		if (StringUtils.isNotBlank(invoice.getProductName())) {
			httpRequest.setAttribute("PROD_NAME", invoice.getProductName());
		} else {
			httpRequest.setAttribute("PROD_NAME", "");
		}

		if (StringUtils.isNotBlank(invoice.getProductDesc())) {
			httpRequest.setAttribute("PROD_DESC", invoice.getProductDesc());
		} else {
			httpRequest.setAttribute("PROD_DESC", "");
		}

		if (StringUtils.isNotBlank(invoice.getQuantity())) {
			httpRequest.setAttribute("QUANTITY", invoice.getQuantity());
		} else {
			httpRequest.setAttribute("QUANTITY", "");
		}

		if (StringUtils.isNotBlank(invoice.getAmount())) {
			httpRequest.setAttribute("AMOUNT", invoice.getAmount());
		} else {
			httpRequest.setAttribute("AMOUNT", "");
		}
		if (StringUtils.isNotBlank(invoice.getServiceCharge())) {
			httpRequest.setAttribute(FieldType.SERVICE_CHARGE.getName(), invoice.getServiceCharge());
		} else {
			httpRequest.setAttribute(FieldType.SERVICE_CHARGE.getName(), "");
		}

		if (StringUtils.isNotBlank(invoice.getDurationFrom())) {
			httpRequest.setAttribute("DURATION_FROM", invoice.getDurationFrom());
		} else {
			httpRequest.setAttribute("DURATION_FROM", "");
		}

		if (StringUtils.isNotBlank(invoice.getDurationTo())) {
			httpRequest.setAttribute("DURATION_TO", invoice.getDurationTo());
		} else {
			httpRequest.setAttribute("DURATION_TO", "");
		}
		
		if (StringUtils.isNotBlank(transactionSearch.getPgRefNum())) {
			httpRequest.setAttribute(FieldType.PG_REF_NUM.getName(),transactionSearch.getPgRefNum());
		} else {
			httpRequest.setAttribute(FieldType.PG_REF_NUM.getName(), "");
		}
		
		if (StringUtils.isNotBlank(transactionSearch.getStatus())) {
			httpRequest.setAttribute(FieldType.STATUS.getName(),transactionSearch.getStatus());
		} else {
			httpRequest.setAttribute(FieldType.STATUS.getName(), "");
		}
		
		if (StringUtils.isNotBlank(transactionSearch.getMopType())) {
			httpRequest.setAttribute(FieldType.MOP_TYPE.getName(),transactionSearch.getMopType());
		} else {
			httpRequest.setAttribute(FieldType.MOP_TYPE.getName(), "");
		}
		
		if (StringUtils.isNotBlank(transactionSearch.getPaymentMethods())) {
			httpRequest.setAttribute(FieldType.PAYMENT_MODE.getName(),transactionSearch.getPaymentMethods());
		} else {
			httpRequest.setAttribute(FieldType.PAYMENT_MODE.getName(), "");
		}
		
		if (StringUtils.isNotBlank(transactionSearch.getCardNumber())) {
			httpRequest.setAttribute(FieldType.CARD_NUMBER.getName(),transactionSearch.getCardNumber());
		} else {
			httpRequest.setAttribute(FieldType.CARD_NUMBER.getName(), "");
		}
		
		if (StringUtils.isNotBlank(transactionSearch.gettDate())) {
			httpRequest.setAttribute(FieldType.CREATE_DATE.getName(),transactionSearch.gettDate());
		} else {
			httpRequest.setAttribute(FieldType.CREATE_DATE.getName(), "");
		}


		
	}

	public Invoice getInvoice(String ordeId) {

		Invoice invoice = new Invoice();
		BasicDBObject invoiceQuery = new BasicDBObject("INVOICE_ID", ordeId);
		BasicDBObject match = new BasicDBObject("$match", invoiceQuery);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		BasicDBObject limit = new BasicDBObject("$limit", 1);

		List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();

		while (cursor.hasNext()) {
			Document doc = cursor.next();

			if (doc.get("NAME") != null) {
				invoice.setName(String.valueOf(doc.get("NAME")));
			}

			if (doc.get("INVOICE_NO") != null) {
				invoice.setInvoiceNo(String.valueOf(doc.get("INVOICE_NO")));
			}

			if (doc.get("ADDRESS") != null) {
				invoice.setAddress(String.valueOf(doc.get("ADDRESS")));
			}

			if (doc.get("CITY") != null) {
				invoice.setCity(String.valueOf(doc.get("CITY")));
			}

			if (doc.get("STATE") != null) {
				invoice.setState(String.valueOf(doc.get("STATE")));
			}

			if (doc.get("ZIP") != null) {
				invoice.setZip(String.valueOf(doc.get("ZIP")));
			}

			if (doc.get("EMAIL") != null) {
				invoice.setEmail(String.valueOf(doc.get("EMAIL")));
			}

			if (doc.get("PHONE") != null) {
				invoice.setPhone(String.valueOf(doc.get("PHONE")));
			}

			if (doc.get("PRODUCT_NAME") != null) {
				invoice.setProductName(String.valueOf(doc.get("PRODUCT_NAME")));
			}

			if (doc.get("PRODUCT_DESC") != null) {
				invoice.setProductDesc(String.valueOf(doc.get("PRODUCT_DESC")));
			}

			if (doc.get("QUANTITY") != null) {
				invoice.setQuantity(String.valueOf(doc.get("QUANTITY")));
			}

			if (doc.get("AMOUNT") != null) {
				invoice.setAmount(String.valueOf(doc.get("AMOUNT")));
			}
			
			if (doc.get("SERVICE_CHARGE") != null) {
				invoice.setServiceCharge(String.valueOf(doc.get("SERVICE_CHARGE")));
			}

			if (doc.get("DURATION_FROM") != null) {
				invoice.setDurationFrom(String.valueOf(doc.get("DURATION_FROM")));
			}

			if (doc.get("DURATION_TO") != null) {
				invoice.setDurationTo(String.valueOf(doc.get("DURATION_TO")));
			}

		}
		return invoice;

	}

	public Map<String, String> getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Map<String, String> responseMap) {
		this.responseMap = responseMap;
	}

}
