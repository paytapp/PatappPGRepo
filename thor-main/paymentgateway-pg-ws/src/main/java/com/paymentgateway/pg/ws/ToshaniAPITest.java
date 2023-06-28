package com.paymentgateway.pg.ws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.paymentgateway.commons.util.TransactionManager;

public class ToshaniAPITest {

	
	public static final String MESSAGE_OPEN_TAG = "<messagecode>";
	public static final String MESSAGE_CLOSE_TAG = "</messagecode>";
	public static final String STATUS_OPEN_TAG = "<status>";
	public static final String STATUS_CLOSE_TAG = "</status>";
	public static final String STATUS_CODE_OPEN_TAG = "<statuscode>";
	public static final String STATUS_CODE_CLOSE_TAG = "</statuscode>";
	public static final String STATUS_DESC_OPEN_TAG = "<statusdescription>";
	public static final String STATUS_DESC_CLOSE_TAG = "</statusdescription>";
	public static final String EMAIL_ADD_OPEN_TAG = "<emailaddress>";
	public static final String EMAIL_ADD_CLOSE_TAG = "</emailaddress>";
	public static final String NON_ZERO_OPEN_TAG = "<nonzeroflag>";
	public static final String NON_ZERO_CLOSE_TAG = "</nonzeroflag>";
	public static final String CHECKSUM_OPEN_TAG = "<checksum>";
	public static final String CHECKSUM_CLOSE_TAG = "</checksum>";
	public static final String BALANCE_AMT_OPEN_TAG = "<balanceamount>";
	public static final String BALANCE_AMT_CLOSE_TAG = "</balanceamount>";
	public static final String DEBITED_AMT_OPEN_TAG = "<debitedamount>";
	public static final String DEBITED_AMT_CLOSE_TAG = "</debitedamount>";
	public static final String ORDER_ID_OPEN_TAG = "<orderid>";
	public static final String ORDER_ID_CLOSE_TAG = "</orderid>";
	public static final String REF_ID_OPEN_TAG = "<refId>";
	public static final String REF_ID_CLOSE_TAG = "</refId>";
	public static final String ORDER_TYPE_OPEN_TAG = "<ordertype>";
	public static final String ORDER_TYPE_CLOSE_TAG = "</ordertype>";
	public static final String AMT_OPEN_TAG = "<amount>";
	public static final String AMT_CLOSE_TAG = "</amount>";
	public static final String STATUS_MSG_OPEN_TAG = "<statusmessage>";
	public static final String STATUS_MSG_CLOSE_TAG = "</statusmessage>";
	public static final String TXID_OPEN_TAG = "<txid>";
	public static final String TXID_CLOSE_TAG = "</txid>";
	public static final String REF_ID_SMALL_OPEN_TAG = "<refid>";
	public static final String REF_ID_SMALL_CLOSE_TAG = "</refid>";
	
	public static void main(String[] args) {

		try {

			// Generate Chcksum
			String action = "existingusercheck";
			String cell = "9896215750";
			String merchantname = "Shaiwal";
			String mid = "MBK9005";
			String msgcode = "500";
			String email = "shaiwalmallick0379@gmail.com";
			String amount = "100";
			String tokentype = "0";
			String otp = "";
			
			String orderID = TransactionManager.getId();
			createUser("7701998097", "sushma.saroj@paymentgateway.com", "Sushma", mid, "502", "826312");
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String login() {

		return null;
	}

	public static String checkUser(String action, String cell, String merchantName, String mid, String msgcode,
			String email) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + action + "'");
		sb.append("'" + cell + "'");
		sb.append("'" + merchantName + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + msgcode + "'");

		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/querywallet?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("msgcode=" + msgcode + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("merchantname=" + merchantName + "&");
			sbUrl.append("action=" + action);

			String url = sbUrl.toString();
			System.out.println(url);
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				String messagecode = getTextBetweenTags(response.toString(),MESSAGE_OPEN_TAG,MESSAGE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String statusdescription = getTextBetweenTags(response.toString(),STATUS_DESC_OPEN_TAG,STATUS_DESC_CLOSE_TAG);
				String emailaddress = getTextBetweenTags(response.toString(),EMAIL_ADD_OPEN_TAG,EMAIL_ADD_CLOSE_TAG);
				String nonzeroflag = getTextBetweenTags(response.toString(),NON_ZERO_OPEN_TAG,NON_ZERO_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("messagecode >> " + messagecode);
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusdescription >> " + statusdescription);
				System.out.println("emailaddress >> " + emailaddress);
				System.out.println("nonzeroflag >> " + nonzeroflag);
				System.out.println("checksum >> " + checksum);

				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	public static String getOtp(String amount, String cell, String tokentype, String msgcode, String mid,
			String merchantname) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + amount + "'");
		sb.append("'" + cell + "'");
		sb.append("'" + merchantname + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + msgcode + "'");
		sb.append("'" + tokentype + "'");

		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/otpgenerate?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("msgcode=" + msgcode + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("merchantname=" + merchantname + "&");
			sbUrl.append("amount=" + amount+ "&");
			sbUrl.append("tokentype=" + tokentype);
			String url = sbUrl.toString();

			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				String messagecode = getTextBetweenTags(response.toString(),MESSAGE_OPEN_TAG,MESSAGE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String statusdescription = getTextBetweenTags(response.toString(),STATUS_DESC_OPEN_TAG,STATUS_DESC_CLOSE_TAG);
				String emailaddress = getTextBetweenTags(response.toString(),EMAIL_ADD_OPEN_TAG,EMAIL_ADD_CLOSE_TAG);
				String nonzeroflag = getTextBetweenTags(response.toString(),NON_ZERO_OPEN_TAG,NON_ZERO_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("messagecode >> " + messagecode);
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusdescription >> " + statusdescription);
				System.out.println("emailaddress >> " + emailaddress);
				System.out.println("nonzeroflag >> " + nonzeroflag);
				System.out.println("checksum >> " + checksum);
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	public static String balance(String cell, String otp, String msgcode, String mid, String merchantname) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + cell + "'");
		sb.append("'" + merchantname + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + msgcode + "'");
		sb.append("'" + otp + "'");

		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/userbalance?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("msgcode=" + msgcode + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("merchantname=" + merchantname + "&");
			sbUrl.append("otp=" + otp);
			
			String url = sbUrl.toString();
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				String messagecode = getTextBetweenTags(response.toString(),MESSAGE_OPEN_TAG,MESSAGE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String statusdescription = getTextBetweenTags(response.toString(),STATUS_DESC_OPEN_TAG,STATUS_DESC_CLOSE_TAG);
				String balanceamount = getTextBetweenTags(response.toString(),BALANCE_AMT_OPEN_TAG,BALANCE_AMT_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("messagecode >> " + messagecode);
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusdescription >> " + statusdescription);
				System.out.println("balanceamount >> " + balanceamount);
				System.out.println("checksum >> " + checksum);
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	
	public static String createUser(String cell, String email, String merchantname, String mid, String msgcode, String otp) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + cell + "'");
		sb.append("'" + email + "'");
		sb.append("'" + merchantname + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + msgcode + "'");
		sb.append("'" + otp + "'");
		
		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/createwalletuser?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("email=" + email + "&");
			sbUrl.append("msgcode=" + msgcode + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("merchantname=" + merchantname + "&");
			sbUrl.append("otp=" + otp);
			
			String url = sbUrl.toString();
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}
				String messagecode = getTextBetweenTags(response.toString(),MESSAGE_OPEN_TAG,MESSAGE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String statusdescription = getTextBetweenTags(response.toString(),STATUS_DESC_OPEN_TAG,STATUS_DESC_CLOSE_TAG);
				String emailaddress = getTextBetweenTags(response.toString(),EMAIL_ADD_OPEN_TAG,EMAIL_ADD_CLOSE_TAG);
				String nonzeroflag = getTextBetweenTags(response.toString(),NON_ZERO_OPEN_TAG,NON_ZERO_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("messagecode >> " + messagecode);
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusdescription >> " + statusdescription);
				System.out.println("emailaddress >> " + emailaddress);
				System.out.println("nonzeroflag >> " + nonzeroflag);
				System.out.println("checksum >> " + checksum);
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	public static String loadMoney(String amount, String cell, String comment, String creditmethod, String merchantname, 
			String mid, String orderid, String typeofmoney, String walletid) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + amount + "'");
		sb.append("'" + cell + "'");
		sb.append("'" + comment + "'");
		sb.append("'" + creditmethod + "'");
		sb.append("'" + merchantname + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + orderid + "'");
		sb.append("'" + typeofmoney + "'");
		sb.append("'" + walletid + "'");
		
		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/loadmoney?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("amount=" + amount + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("comment=" + comment + "&");
			sbUrl.append("creditmethod=" + creditmethod + "&");
			sbUrl.append("merchantname=" + merchantname + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("orderid=" + orderid + "&");
			sbUrl.append("typeofmoney=" + typeofmoney + "&");
			sbUrl.append("walletid=" + walletid);
			
			String url = sbUrl.toString();
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				String messagecode = getTextBetweenTags(response.toString(),MESSAGE_OPEN_TAG,MESSAGE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String statusdescription = getTextBetweenTags(response.toString(),STATUS_DESC_OPEN_TAG,STATUS_DESC_CLOSE_TAG);
				String emailaddress = getTextBetweenTags(response.toString(),EMAIL_ADD_OPEN_TAG,EMAIL_ADD_CLOSE_TAG);
				String nonzeroflag = getTextBetweenTags(response.toString(),NON_ZERO_OPEN_TAG,NON_ZERO_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("messagecode >> " + messagecode);
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusdescription >> " + statusdescription);
				System.out.println("emailaddress >> " + emailaddress);
				System.out.println("nonzeroflag >> " + nonzeroflag);
				System.out.println("checksum >> " + checksum);
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static void addmoneytowallet(String amount, String cell, String orderid, String paymenttype, String mid, 
			String merchantname, String ccnumber, String expmonth, String expyear,String cvv,String otp,String redirecturl ) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + amount + "'");
		sb.append("'" + cell + "'");
		sb.append("'" + merchantname + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + orderid + "'");
		sb.append("'" + otp + "'");
		sb.append("'" + redirecturl + "'");
		
		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/addmoneytowallet?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("amount=" + amount + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("orderid=" + orderid + "&");
			sbUrl.append("paymenttype" + paymenttype + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("merchantname=" + merchantname + "&");
			sbUrl.append("otp=" + otp + "&");
			sbUrl.append("redirecturl=" + redirecturl);
			
			String url = sbUrl.toString();
			System.out.println("RedirectURL == " + url);
		} catch (Exception e) {
			e.printStackTrace();
			//return null;
		}

	}
	
	//debitWallet("100", "168383", "9910990887", "ORD000117856", "WalletDebit", "debit", "503", mid, merchantname);
	public static String debitWallet(String amount, String otp, String cell, String orderid, String comment, 
			String txntype, String msgcode, String mid, String merchantname) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + amount + "'");
		sb.append("'" + cell + "'");
		sb.append("'" + comment + "'");
		sb.append("'" + merchantname + "'");
		sb.append("'" + mid + "'");
		sb.append("'" + msgcode + "'");
		sb.append("'" + orderid + "'");
		sb.append("'" + otp + "'");
		sb.append("'" + txntype + "'");
		
		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/debitwallet?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("amount=" + amount + "&");
			sbUrl.append("otp=" + otp + "&");
			sbUrl.append("cell=" + cell + "&");
			sbUrl.append("orderid=" + orderid + "&");
			sbUrl.append("comment=" + comment + "&");
			sbUrl.append("txntype=" + txntype + "&");
			sbUrl.append("msgcode=" + msgcode + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("merchantname=" + merchantname );
			
			String url = sbUrl.toString();
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				System.out.println("Response  > "+ response.toString());
				String messagecode = getTextBetweenTags(response.toString(),MESSAGE_OPEN_TAG,MESSAGE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String statusdescription = getTextBetweenTags(response.toString(),STATUS_DESC_OPEN_TAG,STATUS_DESC_CLOSE_TAG);
				String debitedamount = getTextBetweenTags(response.toString(),DEBITED_AMT_OPEN_TAG,DEBITED_AMT_CLOSE_TAG);
				String balanceamount = getTextBetweenTags(response.toString(),BALANCE_AMT_OPEN_TAG,BALANCE_AMT_CLOSE_TAG);
				String refId = getTextBetweenTags(response.toString(),REF_ID_OPEN_TAG,REF_ID_CLOSE_TAG);
				String orderidRes = getTextBetweenTags(response.toString(),ORDER_ID_OPEN_TAG,ORDER_ID_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("messagecode >> " + messagecode);
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusdescription >> " + statusdescription);
				System.out.println("debitedamount >> " + debitedamount);
				System.out.println("balanceamount >> " + balanceamount);
				System.out.println("refId >> " + refId);
				System.out.println("orderid >> " + orderidRes);
				System.out.println("checksum >> " + checksum);
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static String checkstatus(String mid, String orderid) {

		StringBuilder sb = new StringBuilder();

		sb.append("'" + mid + "'");
		sb.append("'" + orderid + "'");
		
		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/checkstatus?");
			sbUrl.append("checksum=" + checkSum + "&");
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("orderid=" + orderid );
			
			String url = sbUrl.toString();
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				System.out.println("Response  > "+ response.toString());
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String orderidRes = getTextBetweenTags(response.toString(),ORDER_ID_OPEN_TAG,ORDER_ID_CLOSE_TAG);
				String refid = getTextBetweenTags(response.toString(),REF_ID_SMALL_OPEN_TAG,REF_ID_SMALL_CLOSE_TAG);
				String amount = getTextBetweenTags(response.toString(),AMT_OPEN_TAG,AMT_CLOSE_TAG);
				String statusmessage = getTextBetweenTags(response.toString(),STATUS_MSG_OPEN_TAG,STATUS_MSG_CLOSE_TAG);
				String ordertype = getTextBetweenTags(response.toString(),ORDER_TYPE_OPEN_TAG,ORDER_TYPE_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("statuscode >> " + statuscode);
				System.out.println("orderid >> " + orderidRes);
				System.out.println("refid >> " + refid);
				System.out.println("amount >> " + amount);
				System.out.println("statusmessage >> " + statusmessage);
				System.out.println("ordertype >> " + ordertype);
				System.out.println("checksum >> " + checksum);
				
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	public static String refund(String mid, String txid,String amount) {

		StringBuilder sb = new StringBuilder();

		
		sb.append("'" + mid + "'");
		sb.append("'" + txid + "'");
		sb.append("'" + amount + "'");
		
		try {
			String checkSum = calculateChecksum("ju6tygh7u7tdg554k098ujd5468o", sb.toString());
			
			StringBuilder sbUrl = new StringBuilder();
			sbUrl.append("https://test.mobikwik.com/walletrefund?");
			sbUrl.append("checksum=" + checkSum + "&");
			
			sbUrl.append("mid=" + mid + "&");
			sbUrl.append("txid=" + txid + "&" );
			sbUrl.append("amount=" + amount);
			
			String url = sbUrl.toString();
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
			httpClient.setRequestMethod("POST");
			httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				System.out.println("Response  > "+ response.toString());
				String statuscode = getTextBetweenTags(response.toString(),STATUS_CODE_OPEN_TAG,STATUS_CODE_CLOSE_TAG);
				String status = getTextBetweenTags(response.toString(),STATUS_OPEN_TAG,STATUS_CLOSE_TAG);
				String statusmessage = getTextBetweenTags(response.toString(),STATUS_MSG_OPEN_TAG,STATUS_MSG_CLOSE_TAG);
				String txidRes = getTextBetweenTags(response.toString(),TXID_OPEN_TAG,TXID_CLOSE_TAG);
				String refId = getTextBetweenTags(response.toString(),REF_ID_OPEN_TAG,REF_ID_CLOSE_TAG);
				String checksum = getTextBetweenTags(response.toString(),CHECKSUM_OPEN_TAG,CHECKSUM_CLOSE_TAG);
				
				System.out.println("status >> " + status);
				System.out.println("statuscode >> " + statuscode);
				System.out.println("statusmessage >> " + statusmessage);
				System.out.println("txid >> " + txidRes);
				System.out.println("refid >> " + refId);
				System.out.println("checksum >> " + checksum);
				
				return response.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	private static String toHex(byte[] bytes) {
		StringBuilder buffer = new StringBuilder(bytes.length * 2);

		byte[] arrayOfByte = bytes;
		int j = bytes.length;
		for (int i = 0; i < j; i++) {
			Byte b = Byte.valueOf(arrayOfByte[i]);
			String str = Integer.toHexString(b.byteValue());
			int len = str.length();
			if (len == 8) {
				buffer.append(str.substring(6));
			} else if (str.length() == 2) {
				buffer.append(str);
			} else {
				buffer.append("0" + str);
			}
		}
		return buffer.toString();
	}

	public static String calculateChecksum(String secretKey, String allParamValue) throws Exception {

		byte[] dataToEncryptByte = allParamValue.getBytes();
		byte[] keyBytes = secretKey.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(secretKeySpec);
		byte[] checksumByte = mac.doFinal(dataToEncryptByte);
		String checksum = toHex(checksumByte);
		return checksum;
	}
	
	public static String getTextBetweenTags(String text, String tag1, String tag2) {

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
	}
}
