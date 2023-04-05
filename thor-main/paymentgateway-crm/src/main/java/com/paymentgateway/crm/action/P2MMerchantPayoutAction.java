package com.paymentgateway.crm.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;

public class P2MMerchantPayoutAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static Logger logger = LoggerFactory.getLogger(P2MMerchantPayoutAction.class.getName());

	private static final long serialVersionUID = 2775038263489661555L;
	private static final String prefix = "MONGO_DB_";

	private List<TransactionSearch> aaData = new ArrayList<TransactionSearch>();
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private User sessionUser = null;
	private String dateFrom;
	private String dateTo;
	private String orderId;
	private String payId;
	private String rrn;

	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setMerchantList(userDao.getNormalMerchantList());
				currencyMap = Currency.getAllCurrency();
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && !sessionUser.isSuperMerchant()
					&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
				Merchants merchant = new Merchants();
				merchant.setEmailId(sessionUser.getEmailId());
				merchant.setBusinessName(sessionUser.getBusinessName());
				merchant.setPayId(sessionUser.getPayId());
				merchantList.add(merchant);
				currencyMap = currencyMapProvider.currencyMap(sessionUser);
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return INPUT;
	}

	public String fetchP2MPayoutData() {
		Map<String, String> userMap = new HashMap<String, String>();
		List<TransactionSearch> listdata = new ArrayList<TransactionSearch>();
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			DateFormat formatdate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			Date dateStart = null;
			Date dateEnd = null;
			try {
				dateStart = formatdate.parse(dateFrom);
				dateEnd = formatdate.parse(dateTo);
			} catch (ParseException e) {
				logger.error("Exception in date parsing ", e);
			}

			String startString = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 00:00:00";
			String endString = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 23:59:59";

			BasicDBObject dateQuery = new BasicDBObject();
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startString).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endString).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
			BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndexIn);
			paramConditionLst.add(dateIndexQuery);

			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("All")) {
				BasicDBObject payIdObj = new BasicDBObject(FieldType.PAY_ID.getName(), payId);
				paramConditionLst.add(payIdObj);
			}
			if (StringUtils.isNotBlank(orderId)) {
				BasicDBObject orderIdObj = new BasicDBObject(FieldType.ORDER_ID.getName(), orderId);
				paramConditionLst.add(orderIdObj);
			}
			if (StringUtils.isNotBlank(rrn)) {
				BasicDBObject rrnObj = new BasicDBObject(FieldType.RRN.getName(), rrn);
				paramConditionLst.add(rrnObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final query for P2M Merchant Payout data = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_P2M_PAYOUT.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				
				TransactionSearch data = new TransactionSearch();
				String payId = dbobj.getString(FieldType.PAY_ID.getName());
				String user;

				if (userMap.get(payId) != null && !userMap.get(payId).isEmpty()) {
					user = userMap.get(payId);
				} else {
					user = userDao.getBusinessNameByPayId(payId);
					userMap.put(payId, user);
				}
				data.setMerchantName(user);
				data.setTransactionCaptureDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setRrn(dbobj.getString(FieldType.RRN.getName()));
				data.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				data.setPayerName(dbobj.getString(FieldType.PAYER_NAME.getName()));
				data.setPayeeAddress(dbobj.getString(FieldType.PAYEE_ADDRESS.getName()));
				data.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				data.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				listdata.add(data);
			}
			cursor.close();
		} catch (Exception e) {
			logger.error("exception", e);
		}
		setAaData(listdata);
		return SUCCESS;

	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public List<TransactionSearch> getAaData() {
		return aaData;
	}

	public void setAaData(List<TransactionSearch> aaData) {
		this.aaData = aaData;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
}
