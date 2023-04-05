/**
 * 
 */
package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.text.DateFormat;
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

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class ResellerRevenueDataService {

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(ResellerRevenueDataService.class.getName());

	@SuppressWarnings("static-access")
	public Map<String, HashMap<String, String>> fetchResellerRevenueReport(User sessionUser, String merchantPayId, String resellerId,
			String fromDate, String toDate) {

		logger.info("inside ResellerRevenueDataService, fetchResellerRevenueReport ");
		Map<String, HashMap<String, String>> resellerRevenueReportMap = new HashMap<String, HashMap<String, String>>();
		/*fromDate = fromDate + " 00:00:00";
		toDate = toDate + " 23:59:59";*/
		
		
		try {
			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();

			if (!(StringUtils.isBlank(merchantPayId) || merchantPayId.equalsIgnoreCase("ALL"))) {
				String[] payId = merchantPayId.split(",");
				for (int i = 0; i < payId.length; i++) {
					paramList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId[i]));
				}
			}

			if (StringUtils.isNotBlank(resellerId) || (!merchantPayId.equalsIgnoreCase("ALL"))) {
				String[] payId = resellerId.split(",");
				for (int i = 0; i < payId.length; i++) {
					paramList.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), payId[i]));
				}
			}

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());

			List<BasicDBObject> dateIndexConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(toDate).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String dateIndex : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject("DATE_INDEX", dateIndex));
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("$or", dateIndexConditionList);
			}

			if (!dateIndexConditionQuery.isEmpty()) {
				paramList.add(dateIndexConditionQuery);
			}

			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName());

			if (!dateQuery.isEmpty()) {
				paramList.add(dateQuery);
			}

			if (!statusQuery.isEmpty()) {
				paramList.add(statusQuery);
			}

			/*
			 * if (!payIdQuery.isEmpty()) { paramList.add(payIdQuery); }
			 */

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			
			logger.info("Inside ResellerRevenueDataService , fetchResellerRevenueReport function , finalQuery = "+finalQuery);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbObj = cursor.next();
				BigDecimal resellerCharges;
				BigDecimal pgResellerCharges;
				BigDecimal resellerGst;
				BigDecimal pgResellerGst;
				if (dbObj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.RESELLER_CHARGES.getName()))) {
					resellerCharges = new BigDecimal(dbObj.getString(FieldType.RESELLER_CHARGES.getName()));
				} else {
					resellerCharges = new BigDecimal("0.00");
				}

				if (dbObj.containsKey(FieldType.PG_TDR_SC.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_TDR_SC.getName()))) {
					pgResellerCharges = new BigDecimal(dbObj.getString(FieldType.PG_TDR_SC.getName()));
				} else {
					pgResellerCharges = new BigDecimal("0.00");
				}

				if (dbObj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.RESELLER_GST.getName()))) {
					resellerGst = new BigDecimal(dbObj.getString(FieldType.RESELLER_GST.getName()));
				} else {
					resellerGst = new BigDecimal("0.00");
				}

				if (dbObj.containsKey(FieldType.PG_GST.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_GST.getName()))) {
					pgResellerGst = new BigDecimal(dbObj.getString(FieldType.PG_GST.getName()));
				} else {
					pgResellerGst = new BigDecimal("0.00");
				}

				// BigDecimal toatlGst = resellerGst.add(pgResellerGst);
				String revenueDay = dbObj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).trim();

				if (sessionUser.getUserType().equals(UserType.SUPERADMIN)
						|| sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

					if (resellerRevenueReportMap.containsKey(revenueDay)) {
						HashMap<String, String> revenueOnDay = resellerRevenueReportMap.get(revenueDay);

						String transactionCount = String
								.valueOf(Integer.valueOf(revenueOnDay.get("TOTAL_TRANSACTION")) + 1);
						BigDecimal totalResellerRevenue = new BigDecimal(revenueOnDay.get("TOTAL_REVENUE"))
								.add(resellerCharges);
						BigDecimal totalResellerGst = new BigDecimal(revenueOnDay.get("TOTAL_RESELLER_GST"))
								.add(resellerGst);
						BigDecimal totalPgResellerRevenue = new BigDecimal(revenueOnDay.get("TOTAL_PG_PROFIT"))
								.add(pgResellerCharges);
						BigDecimal totalPgGst = new BigDecimal(revenueOnDay.get("TOTAL_PG_GST")).add(pgResellerGst);

						revenueOnDay.put("TOTAL_TRANSACTION", transactionCount);
						revenueOnDay.put("TOTAL_REVENUE", String.valueOf(totalResellerRevenue));
						revenueOnDay.put("TOTAL_RESELLER_GST", String.valueOf(totalResellerGst));
						revenueOnDay.put("TOTAL_PG_PROFIT", String.valueOf(totalPgResellerRevenue));
						revenueOnDay.put("TOTAL_PG_GST", String.valueOf(totalPgGst));
						revenueOnDay.put("TRANSACTION_DATE", revenueDay);
						resellerRevenueReportMap.put(revenueDay, revenueOnDay);

					} else {

						HashMap<String, String> revenueOnDay = new HashMap<String, String>();
						revenueOnDay.put("TOTAL_TRANSACTION", "1");
						revenueOnDay.put("TOTAL_REVENUE", String.valueOf(resellerCharges));
						revenueOnDay.put("TOTAL_RESELLER_GST", String.valueOf(resellerGst));
						revenueOnDay.put("TOTAL_PG_PROFIT", String.valueOf(pgResellerCharges));
						revenueOnDay.put("TOTAL_PG_GST", String.valueOf(pgResellerGst));
						revenueOnDay.put("TRANSACTION_DATE", revenueDay);
						resellerRevenueReportMap.put(revenueDay, revenueOnDay);
					}
				} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {

					if (resellerRevenueReportMap.containsKey(revenueDay)) {
						HashMap<String, String> revenueOnDay = resellerRevenueReportMap.get(revenueDay);

						String transactionCount = String
								.valueOf(Integer.valueOf(revenueOnDay.get("TOTAL_TRANSACTION")) + 1);
						BigDecimal totalResellerRevenue = new BigDecimal(revenueOnDay.get("TOTAL_REVENUE"))
								.add(resellerCharges/*.add(pgResellerCharges)*/);
						BigDecimal totalResellerGst = new BigDecimal(revenueOnDay.get("TOTAL_RESELLER_GST"))
								.add(resellerGst/*.add(pgResellerGst)*/);
						BigDecimal totalPgResellerRevenue = new BigDecimal(revenueOnDay.get("TOTAL_PG_PROFIT"))
								.add(pgResellerCharges);
						BigDecimal totalPgGst = new BigDecimal(revenueOnDay.get("TOTAL_PG_GST")).add(pgResellerGst);

						revenueOnDay.put("TOTAL_TRANSACTION", transactionCount);
						revenueOnDay.put("TOTAL_REVENUE", String.valueOf(totalResellerRevenue));
						revenueOnDay.put("TOTAL_RESELLER_GST", String.valueOf(totalResellerGst));
						revenueOnDay.put("TOTAL_PG_PROFIT", String.valueOf(totalPgResellerRevenue));
						revenueOnDay.put("TOTAL_PG_GST", String.valueOf(totalPgGst));
						revenueOnDay.put("TRANSACTION_DATE", revenueDay);
						resellerRevenueReportMap.put(revenueDay, revenueOnDay);

					} else {

						HashMap<String, String> revenueOnDay = new HashMap<String, String>();
						revenueOnDay.put("TOTAL_TRANSACTION", "1");
						revenueOnDay.put("TOTAL_REVENUE", String.valueOf(resellerCharges/*.add(pgResellerCharges)*/));
						revenueOnDay.put("TOTAL_RESELLER_GST", String.valueOf(resellerGst/*.add(pgResellerGst)*/));
						revenueOnDay.put("TOTAL_PG_PROFIT", String.valueOf(pgResellerCharges));
						revenueOnDay.put("TOTAL_PG_GST", String.valueOf(pgResellerGst));
						revenueOnDay.put("TRANSACTION_DATE", revenueDay);
						resellerRevenueReportMap.put(revenueDay, revenueOnDay);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
		return resellerRevenueReportMap;
	}
}
