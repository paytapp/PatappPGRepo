package com.paymentgateway.pg.ws;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.util.TransactionManager;

public class InsertTransactions {

	static String[] status = {};
	static String paymenttypeArray[] = new String[] { "CC", "DC", "CD", "UP", "WL", "NB", "NEFT" };
	static String nbsubpaymentArray[] = new String[] { "1005", "1009", "1013", "1030" };
	static String wlsubpaymentArray[] = new String[] { "PPL", "APWL", "GPWL" };
	static String custNameArray[] = new String[] { "Rahul", "Shaiwal", "Sandeep", "Rajit", "Shiva", "Zakir", "Alam",
			"Sunil", "Pooja", "Anamika", "Sunil", "Ankit" };
	static String dbcol_name = "settledTestCollection";
	static int count = 0;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		SimpleDateFormat sdfin = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfcreatedate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat sdfinsertdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss'Z'");
		Date datein = null;
		String custName = "";
		String payid = "";
		Scanner in = new Scanner(System.in);

		System.out.println("Enter from date in dd-mm-yyyy formate");
		String fromdate = in.nextLine();

		System.out.println("Enter to date in dd-mm-yyyy formate");
		String todate = in.nextLine();

		System.out.println("How Many Transaction Status do you want to enter");
		int totatlstatus = Integer.parseInt(in.nextLine());

		status = new String[totatlstatus];

		System.out.println("Enter transaction Status values (like - Captured, Timeout, Rejected etc)");
		for (int i = 0; i < status.length; i++) {
			status[i] = in.nextLine();
		}

		System.out.println("Enter No. of transaction you have to insert");
		int totatlTxnCount = Integer.parseInt(in.nextLine());

		System.out.println("Enter PayId of the Merchant ");
		payid = in.nextLine();

		int rndAmount = 0;

//"05-04-2021" , "10-04-2021"
		List<String> dates = dateIndex(fromdate, todate);
		System.out.println("Total inserted transaction((difference b/w Dates)*3*(no. of transactions)) = "+ dates.size()*3*totatlTxnCount);
		System.out.println("Please Wait While Your Transaction Is Inserted Into Db...");

		for (String date : dates) {
			// System.out.println("Data inserted for date "+date);
			try {
				datein = sdfin.parse(date);
			} catch (ParseException e) {
				System.out.println("date parse exception = " + e);
			}
			String createDate = sdfcreatedate.format(datein);
			String insertDate = sdfinsertdate.format(datein);

			for (int i = 0; i < totatlTxnCount; i++) {
				rndAmount = 100 + (int) (Math.random() * (10000));
				custName = getRandCustName();
				try {
					newOrder(rndAmount, date, custName, createDate, insertDate, payid);
				} catch (Exception e) {
					System.out.println("Total inserted txns = "+ count);
					System.out.println("Please terminate the java application.. ");
					System.out.println("Exception in transaction"+e);
				}
			}
		}
		System.out.println("total no of recoreds inserted into db " + count);
	}

	public static List<String> dateIndex(String fromdate, String todate) {
		List<String> datesIndex = new ArrayList<>();

		try {
			String startString = new SimpleDateFormat(fromdate).toLocalizedPattern();
			String endString = new SimpleDateFormat(todate).toLocalizedPattern();
			DateFormat formatdate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			Date dateStart = null;
			Date dateEnd = null;
			try {
				dateStart = formatdate.parse(startString);
				dateEnd = formatdate.parse(endString);
			} catch (ParseException e) {
				System.out.println("Exception in date parsing " + e);
			}

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				datesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
		} catch (Exception e) {
			System.out.println("Exception in finding dates " + e);
		}
		return datesIndex;
	}

	public static void newOrder(int rndAmount, String date, String custName, String createDate, String insertDate,
			String payid) {

		try {
			MongoDatabase db = getMongoConnetion();
			MongoCollection<Document> coll = db.getCollection(dbcol_name);

			// Add sleep to avoid duplicate txn_id
			Thread.sleep(2);
			String txn_id = TransactionManager.getNewTransactionId();

			Document document = new Document();
			document.append("_id", txn_id);
			document.append("AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("TOTAL_AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("ORIG_TXN_ID", "0");
			document.append("ORIG_TXNTYPE", "SALE");
			document.append("PG_REF_NUM", "0");
			document.append("ACCT_ID", "0");
			document.append("ACQ_ID", "0");
			document.append("OID", txn_id);
			document.append("TXN_CAPTURE_FLAG", "Real-Time");
			document.append("TXN_DATE", date);
			document.append("CARD_MASK", null);
			document.append("TXN_ID", txn_id);
			document.append("TXNTYPE", "NEWORDER");
			document.append("CUST_NAME", custName);
			document.append("ORDER_ID", "ORD" + txn_id);
			document.append("PAY_ID", payid);
			document.append("MOP_TYPE", null);
			document.append("CURRENCY_CODE", "356");
			document.append("STATUS", "Pending");
			document.append("RESPONSE_CODE", "000");
			document.append("RESPONSE_MESSAGE", "SUCCESS");
			document.append("CUST_EMAIL", null);
			document.append("PAYMENT_TYPE", null);
			document.append("ACQUIRER_TYPE", null);
			document.append("PRODUCT_DESC", null);
			document.append("AUTH_CODE", null);
			document.append("PG_DATE_TIME", null);
			document.append("PG_RESP_CODE", null);
			document.append("PG_TXN_MESSAGE", null);
			document.append("INTERNAL_TXN_AUTHENTICATION", "Pending");
			document.append("INTERNAL_CARD_ISSUER_BANK", null);
			document.append("INTERNAL_CARD_ISSUER_COUNTRY", null);
			document.append("INTERNAL_USER_EMAIL", null);
			document.append("INTERNAL_CUST_COUNTRY_NAME", null);
			document.append("RRN", null);
			document.append("SURCHARGE_FLAG", null);
			document.append("CREATE_DATE", createDate);
			document.append("UPDATE_DATE", createDate);
			document.append("REFUND_FLAG", null);
			document.append("REFUND_ORDER_ID", null);
			document.append("INSERTION_DATE", insertDate);
			document.append("DATE_INDEX", date);
			document.append("ACQUIRER_TDR_SC", "0.00");
			document.append("ACQUIRER_GST", "0.00");
			document.append("PG_GST", "0.00");
			document.append("PG_TDR_SC", "0.00");
			document.append("RESELLER_ID", null);
			document.append("PART_SETTLE", null);
			document.append("UDF7", null);
			document.append("UDF8", null);
			document.append("UDF9", null);
			document.append("UDF10", null);
			document.append("UDF11", null);
			document.append("UDF12", null);
			document.append("SUB_MERCHANT_ID", null);
			document.append("RESELLER_CHARGES", "0.00");
			document.append("RESELLER_GST", "0.00");
			document.append("CATEGORY_CODE", null);
			document.append("SKU_CODE", null);
			document.append("PROD_DESC", null);
			document.append("REFUND_DAYS", null);
			document.append("VENDOR_ID", null);
			document.append("PRODUCT_PRICE", null);
			document.append("UDF13", null);
			document.append("UDF14", null);
			document.append("UDF15", null);
			document.append("UDF16", null);
			document.append("UDF17", null);
			document.append("UDF18", null);
			document.append("MERCHANT_TDR_SC", "0.00");
			document.append("MERCHANT_GST", "0.00");

			document.append("Z_NAME", null);
			document.append("C_NAME", null);
			document.append("D_NAME", null);

			document.append("RECIEPT_NO", null);
			document.append("DELTA_REFUND_FLAG", null);

			//coll.insertOne(document);
			//count++;
			// System.out.println("NEWORDER entry Inserted");

			sentToBank(rndAmount, txn_id, date, custName, createDate, insertDate, payid);

		} catch (Exception e) {
			System.out.println("Total inserted txns = "+ count);
			System.out.println("Please terminate the java application.. ");
			System.out.println("Exception in newOrder insertion = " + e);
		}

	}

	public static void sentToBank(int rndAmount, String txn_id, String date, String custName, String createDate,
			String insertDate, String payid) {

		try {
			MongoDatabase db = getMongoConnetion();
			MongoCollection<Document> coll = db.getCollection(dbcol_name);

			// Add sleep to avoid duplicate txn_id
			Thread.sleep(2);
			String stbtxn_id = TransactionManager.getNewTransactionId();
			String paymenttype = getRandArraypaymentElement();
			String mopType = "";
			if (paymenttype.equalsIgnoreCase("nb")) {
				mopType = getRandnbcode();
			} else if (paymenttype.equalsIgnoreCase("wl")) {
				mopType = getRandwlcode();
			} else if (paymenttype.equalsIgnoreCase("cd")) {
				mopType = "CD";
			} else if (paymenttype.equalsIgnoreCase("up")) {
				mopType = "UP";
			} else if (paymenttype.equalsIgnoreCase("neft")) {
				mopType = "NEFT";
			} else {
				mopType = "VI";
			}

			Document document = new Document();
			document.append("_id", stbtxn_id);
			document.append("AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("TOTAL_AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("ORIG_TXN_ID", stbtxn_id);
			document.append("ORIG_TXNTYPE", "SALE");
			document.append("PG_REF_NUM", stbtxn_id);
			document.append("ACCT_ID", "0");
			document.append("ACQ_ID", "678" + stbtxn_id + "677");
			document.append("OID", txn_id);
			document.append("PAYMENTS_REGION", "DOMESTIC");
			document.append("CARD_HOLDER_TYPE", "CONSUMER");
			document.append("ACQUIRER_MODE", "OFF_US");
			document.append("TXN_CAPTURE_FLAG", "Real-Time");
			document.append("TXN_DATE", date);
			document.append("CARD_MASK", "400000*******0002");
			document.append("TXN_ID", stbtxn_id);
			document.append("TXNTYPE", "ENROLL");
			document.append("CUST_NAME", custName);
			document.append("ORDER_ID", "ORD" + txn_id);
			document.append("PAY_ID", payid);
			document.append("PAYMENT_TYPE", paymenttype);
			document.append("MOP_TYPE", mopType);

			document.append("CURRENCY_CODE", "356");
			document.append("STATUS", "Enrolled");
			document.append("RESPONSE_CODE", "000");
			document.append("RESPONSE_MESSAGE", "SUCCESS");
			document.append("CUST_EMAIL", null);
			document.append("ACQUIRER_TYPE", "AXISBANKCB");
			document.append("PRODUCT_DESC", null);
			document.append("AUTH_CODE", null);
			document.append("PG_DATE_TIME", null);
			document.append("PG_RESP_CODE", "475");
			document.append("PG_TXN_MESSAGE", "SUCCESS");
			document.append("INTERNAL_TXN_AUTHENTICATION", null);
			document.append("INTERNAL_CARD_ISSUER_BANK", "AXIS");
			document.append("INTERNAL_CARD_ISSUER_COUNTRY", "IN");
			document.append("INTERNAL_USER_EMAIL", null);
			document.append("INTERNAL_CUST_COUNTRY_NAME", "Namibia");
			document.append("RRN", null);
			document.append("SURCHARGE_FLAG", null);
			document.append("CREATE_DATE", createDate);
			document.append("UPDATE_DATE", createDate);
			document.append("REFUND_FLAG", null);
			document.append("REFUND_ORDER_ID", null);
			document.append("INSERTION_DATE", insertDate);
			document.append("DATE_INDEX", date);
			document.append("ACQUIRER_TDR_SC", "10.00");
			document.append("ACQUIRER_GST", "1.80");
			document.append("PG_GST", "0.90");
			document.append("PG_TDR_SC", "5.00");
			document.append("RESELLER_ID", null);
			document.append("PART_SETTLE", "N");
			document.append("UDF7", null);
			document.append("UDF8", null);
			document.append("UDF9", null);
			document.append("UDF10", null);
			document.append("UDF11", null);
			document.append("UDF12", null);
			document.append("SUB_MERCHANT_ID", null);
			document.append("RESELLER_CHARGES", "0.00");
			document.append("RESELLER_GST", "0.00");
			document.append("CATEGORY_CODE", null);
			document.append("SKU_CODE", null);
			document.append("PROD_DESC", null);
			document.append("REFUND_DAYS", null);
			document.append("VENDOR_ID", null);
			document.append("PRODUCT_PRICE", null);
			document.append("UDF13", null);
			document.append("UDF14", null);
			document.append("UDF15", null);
			document.append("UDF16", null);
			document.append("UDF17", null);
			document.append("UDF18", null);
			document.append("MERCHANT_TDR_SC", "15.00");
			document.append("MERCHANT_GST", "2.70");

			document.append("Z_NAME", null);
			document.append("C_NAME", null);
			document.append("D_NAME", null);

			document.append("RECIEPT_NO", null);
			document.append("DELTA_REFUND_FLAG", null);

			//coll.insertOne(document);
		//	count++;
			// System.out.println("Enrolled entry Inserted");

			lastEntry(rndAmount, stbtxn_id, txn_id, paymenttype, mopType, date, custName, createDate, insertDate,
					payid);

		} catch (Exception e) {
			System.out.println("Total inserted txns = "+ count);
			System.out.println("Please terminate the java application.. ");
			System.out.println("Exception in Enrolled insertion = " + e);
		}
	}

	public static void lastEntry(int rndAmount, String txn_id, String txnid, String paymenttype, String moptype,
			String date, String custName, String createDate, String insertDate, String payid) {
		try {
			MongoDatabase db = getMongoConnetion();
			MongoCollection<Document> coll = db.getCollection(dbcol_name);

			// Add sleep to avoid duplicate txn_id
			Thread.sleep(2);
			String stbtxn_id = TransactionManager.getNewTransactionId();
			String inputstatus = getRandArrayElement();
			Document document = new Document();
			document.append("_id", stbtxn_id);
			document.append("AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("TOTAL_AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("ORIG_TXN_ID", txn_id);
			document.append("ORIG_TXNTYPE", "SALE");
			document.append("PG_REF_NUM", txn_id);
			document.append("ACCT_ID", "0");
			document.append("ACQ_ID", "678" + txn_id + "677");
			document.append("OID", txnid);
			document.append("PAYMENTS_REGION", "DOMESTIC");
			document.append("CARD_HOLDER_TYPE", "CONSUMER");
			document.append("ACQUIRER_MODE", "OFF_US");
			document.append("TXN_CAPTURE_FLAG", "Real-Time");
			document.append("TXN_DATE", date);
			document.append("CARD_MASK", "400000*******0002");
			document.append("TXN_ID", stbtxn_id);
			document.append("TXNTYPE", "SALE");
			document.append("CUST_NAME", custName);
			document.append("ORDER_ID", "ORD" + txnid);
			document.append("PAY_ID", payid);
			document.append("PAYMENT_TYPE", paymenttype);
			document.append("MOP_TYPE", moptype);

			document.append("CURRENCY_CODE", "356");
			document.append("STATUS", inputstatus);
			document.append("RESPONSE_CODE", "000");
			document.append("RESPONSE_MESSAGE", "SUCCESS");
			document.append("CUST_EMAIL", null);
			document.append("ACQUIRER_TYPE", "AXISBANKCB");
			document.append("PRODUCT_DESC", null);
			document.append("AUTH_CODE", null);
			document.append("PG_DATE_TIME", null);
			document.append("PG_RESP_CODE", "475");
			document.append("PG_TXN_MESSAGE", "SUCCESS");
			document.append("INTERNAL_TXN_AUTHENTICATION", null);
			document.append("INTERNAL_CARD_ISSUER_BANK", "AXIS");
			document.append("INTERNAL_CARD_ISSUER_COUNTRY", "IN");
			document.append("INTERNAL_USER_EMAIL", null);
			document.append("INTERNAL_CUST_COUNTRY_NAME", "Namibia");
			document.append("RRN", null);
			document.append("SURCHARGE_FLAG", null);
			document.append("CREATE_DATE", createDate);
			document.append("UPDATE_DATE", createDate);
			document.append("REFUND_FLAG", null);
			document.append("REFUND_ORDER_ID", null);
			document.append("INSERTION_DATE", insertDate);
			document.append("DATE_INDEX", date);
			document.append("ACQUIRER_TDR_SC", "10.00");
			document.append("ACQUIRER_GST", "1.80");
			document.append("PG_GST", "0.90");
			document.append("PG_TDR_SC", "5.00");
			document.append("RESELLER_ID", "1053410708110956");
			document.append("PART_SETTLE", "N");
			document.append("UDF7", null);
			document.append("UDF8", null);
			document.append("UDF9", null);
			document.append("UDF10", null);
			document.append("UDF11", null);
			document.append("UDF12", null);
			document.append("SUB_MERCHANT_ID", null);
			document.append("RESELLER_CHARGES", "0.00");
			document.append("RESELLER_GST", "0.00");
			document.append("CATEGORY_CODE", null);
			document.append("SKU_CODE", null);
			document.append("PROD_DESC", null);
			document.append("REFUND_DAYS", null);
			document.append("VENDOR_ID", null);
			document.append("PRODUCT_PRICE", null);
			document.append("UDF13", null);
			document.append("UDF14", null);
			document.append("UDF15", null);
			document.append("UDF16", null);
			document.append("UDF17", null);
			document.append("UDF18", null);
			document.append("MERCHANT_TDR_SC", "15.00");
			document.append("MERCHANT_GST", "2.70");

			document.append("Z_NAME", null);
			document.append("C_NAME", null);
			document.append("D_NAME", null);

			document.append("RECIEPT_NO", null);
			document.append("DELTA_REFUND_FLAG", null);

			coll.insertOne(document);
			
//			MongoCollection<Document> coll1 = db.getCollection("transactionStatus");
//			document.append("SETTLEMENT_FLAG", "Y");
//			document.append("SETTLEMENT_DATE", createDate);
//			document.append("SETTLEMENT_DATE_INDEX", date);
//			coll1.insertOne(document);
			
			count++;
			// System.out.println(inputstatus + " entry Ins05erted");
			settledEntry(rndAmount, txn_id, txnid, paymenttype, moptype, date, custName, createDate, insertDate, payid);
		} catch (Exception e) {
			System.out.println("Total inserted txns = "+ count);
			System.out.println("Please terminate the java application.. ");
			System.out.println("Exception in Enrolled insertion = " + e);
		}
	}
	
	public static void settledEntry(int rndAmount, String txn_id, String txnid, String paymenttype, String moptype,
			String date, String custName, String createDate, String insertDate, String payid) {
		try {
			MongoDatabase db = getMongoConnetion();
			MongoCollection<Document> coll = db.getCollection(dbcol_name);

			// Add sleep to avoid duplicate txn_id
			Thread.sleep(2);
			String stbtxn_id = TransactionManager.getNewTransactionId();
			String inputstatus = getRandArrayElement();
			Document document = new Document();
			document.append("_id", stbtxn_id);
			document.append("AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("TOTAL_AMOUNT", String.valueOf(rndAmount) + ".00");
			document.append("ORIG_TXN_ID", txn_id);
			document.append("ORIG_TXNTYPE", "SALE");
			document.append("PG_REF_NUM", txn_id);
			document.append("ACCT_ID", "0");
			document.append("ACQ_ID", "678" + txn_id + "677");
			document.append("OID", txnid);
			document.append("PAYMENTS_REGION", "DOMESTIC");
			document.append("CARD_HOLDER_TYPE", "CONSUMER");
			document.append("ACQUIRER_MODE", "OFF_US");
			document.append("TXN_CAPTURE_FLAG", "Real-Time");
			document.append("TXN_DATE", date);
			document.append("CARD_MASK", "400000*******0002");
			document.append("TXN_ID", stbtxn_id);
			document.append("TXNTYPE", "RECO");
			document.append("CUST_NAME", custName);
			document.append("ORDER_ID", "ORD" + txnid);
			document.append("PAY_ID", payid);
			document.append("PAYMENT_TYPE", paymenttype);
			document.append("MOP_TYPE", moptype);

			document.append("CURRENCY_CODE", "356");
			document.append("STATUS", "Settled");
			document.append("RESPONSE_CODE", "000");
			document.append("RESPONSE_MESSAGE", "SUCCESS");
			document.append("CUST_EMAIL", null);
			document.append("ACQUIRER_TYPE", "AXISBANKCB");
			document.append("PRODUCT_DESC", null);
			document.append("AUTH_CODE", null);
			document.append("PG_DATE_TIME", null);
			document.append("PG_RESP_CODE", "475");
			document.append("PG_TXN_MESSAGE", "SUCCESS");
			document.append("INTERNAL_TXN_AUTHENTICATION", null);
			document.append("INTERNAL_CARD_ISSUER_BANK", "AXIS");
			document.append("INTERNAL_CARD_ISSUER_COUNTRY", "IN");
			document.append("INTERNAL_USER_EMAIL", null);
			document.append("INTERNAL_CUST_COUNTRY_NAME", "Namibia");
			document.append("RRN", null);
			document.append("SURCHARGE_FLAG", null);
			document.append("CREATE_DATE", createDate);
			document.append("UPDATE_DATE", createDate);
			document.append("REFUND_FLAG", null);
			document.append("REFUND_ORDER_ID", null);
			document.append("INSERTION_DATE", insertDate);
			document.append("DATE_INDEX", date);
			document.append("ACQUIRER_TDR_SC", "10.00");
			document.append("ACQUIRER_GST", "1.80");
			document.append("PG_GST", "0.90");
			document.append("PG_TDR_SC", "5.00");
			document.append("RESELLER_ID", "1053410708110956");
			document.append("PART_SETTLE", "N");
			document.append("UDF7", null);
			document.append("UDF8", null);
			document.append("UDF9", null);
			document.append("UDF10", null);
			document.append("UDF11", null);
			document.append("UDF12", null);
			document.append("SUB_MERCHANT_ID", null);
			document.append("RESELLER_CHARGES", "0.00");
			document.append("RESELLER_GST", "0.00");
			document.append("CATEGORY_CODE", null);
			document.append("SKU_CODE", null);
			document.append("PROD_DESC", null);
			document.append("REFUND_DAYS", null);
			document.append("VENDOR_ID", null);
			document.append("PRODUCT_PRICE", null);
			document.append("UDF13", null);
			document.append("UDF14", null);
			document.append("UDF15", null);
			document.append("UDF16", null);
			document.append("UDF17", null);
			document.append("UDF18", null);
			document.append("MERCHANT_TDR_SC", "15.00");
			document.append("MERCHANT_GST", "2.70");

			document.append("Z_NAME", null);
			document.append("C_NAME", null);
			document.append("D_NAME", null);

			document.append("RECIEPT_NO", null);
			document.append("DELTA_REFUND_FLAG", null);

			coll.insertOne(document);
			
			count++;
			// System.out.println(inputstatus + " entry Ins05erted");

		} catch (Exception e) {
			System.out.println("Total inserted txns = "+ count);
			System.out.println("Please terminate the java application.. ");
			System.out.println("Exception in Enrolled insertion = " + e);
		}
	}

	public static MongoDatabase getMongoConnetion() {
		MongoDatabase db = null;

		try {
			String auth_user = "mahboob", auth_pwd = "mongo123", host_name = "127.0.0.1", db_name = "paymentGateway",
					db_col_name = dbcol_name;

			String encoded_pwd = URLEncoder.encode(auth_pwd, "UTF-8");

			String client_url = "mongodb://" + auth_user + ":" + encoded_pwd + "@" + host_name + ":" + "27017" + "/"
					+ db_name;
			MongoClientURI uri = new MongoClientURI(client_url);

			MongoClient mongo_client = new MongoClient(uri);
			db = mongo_client.getDatabase(db_name);
		} catch (Exception e) {
			System.out.println("Exception in getMongoConnetion = " + e);
		}
		return db;

	}

	public static String getRandArrayElement() {
		Random rand = new Random();
		return status[rand.nextInt(status.length)];
	}

	public static String getRandArraypaymentElement() {
		Random rand = new Random();
		return paymenttypeArray[rand.nextInt(paymenttypeArray.length)];
	}

	public static String getRandnbcode() {
		Random rand = new Random();
		return nbsubpaymentArray[rand.nextInt(nbsubpaymentArray.length)];
	}

	public static String getRandwlcode() {
		Random rand = new Random();
		return wlsubpaymentArray[rand.nextInt(wlsubpaymentArray.length)];
	}

	public static String getRandCustName() {
		Random rand = new Random();
		return custNameArray[rand.nextInt(custNameArray.length)];
	}
//	public static String getRandPayId() {
//		Random rand = new Random();
//		return payIdArray[rand.nextInt(payIdArray.length)];
//	}
}
