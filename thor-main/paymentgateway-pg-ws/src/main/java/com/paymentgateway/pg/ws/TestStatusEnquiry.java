package com.paymentgateway.pg.ws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Rajit
 */
public class TestStatusEnquiry implements Runnable {

	public static String statusEnquiryURL = "http://127.0.0.1:8083/pgws/transactStatusEnquiry";
	
	public static JSONObject saleRequestMap1 = new JSONObject();
	public static JSONObject saleRequestMap2 = new JSONObject();
	public static JSONObject saleRequestMap3 = new JSONObject();
	
	
	public TestStatusEnquiry(String payId, String orderId, String amount, String txnType, String currencyCode, String hash, JSONObject saleRequestName) {
		
		saleRequestName.put(FieldType.PAY_ID.getName(), payId);
		saleRequestName.put(FieldType.ORDER_ID.getName(), orderId);
		saleRequestName.put(FieldType.AMOUNT.getName(), amount);
		saleRequestName.put(FieldType.TXNTYPE.getName(), txnType);
		saleRequestName.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
		saleRequestName.put(FieldType.HASH.getName(), hash);
	}
	@Override
	public void run() {
		try {
			for (int i = 1; i < 5000; i++) {
				URL url = new URL(statusEnquiryURL);
				int timeout = 20000;
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(timeout);
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Accept", "application/json");

				OutputStream os = conn.getOutputStream();
				
				switch (Thread.currentThread().getName()) {

				case "Thread1":
					/*saleRequestMap1.remove(FieldType.ORDER_ID.getName());
					saleRequestMap1.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId()+"/"+saleRequestMap1.get(FieldType.PAY_ID.getName()));*/
					os.write(saleRequestMap1.toString().getBytes());
					//System.out.println("resquest "+saleRequestMap1);
					break;

				case "Thread2":
					saleRequestMap2.remove(FieldType.ORDER_ID.getName());
					saleRequestMap2.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId()+"/"+saleRequestMap2.get(FieldType.PAY_ID.getName()));
					os.write(saleRequestMap2.toString().getBytes());
					//System.out.println("resquest "+saleRequestMap2);
					break;

				case "Thread3":
					os.write(saleRequestMap3.toString().getBytes());
					break;
				
				default:
					break;
				}
				
				os.flush();
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				conn.disconnect();
				String serverResponse = sb.toString();
				JSONObject responseCode = new JSONObject(serverResponse);
				if(!(responseCode.getString("RESPONSE_CODE").equalsIgnoreCase("000") || responseCode.getString("RESPONSE_CODE").equalsIgnoreCase("302"))) {
					System.out.println("GOT");
					System.out.println(responseCode.getString("RESPONSE_CODE"));
					System.exit(0);
					
				}
				System.out.println(Thread.currentThread().getName()+" Status Enquiry Process Response : " + responseCode.getString("RESPONSE_CODE"));
			}
		} catch (Exception exception) {
			System.out.println("Exception in status enquiry process PGWS call : " + exception.getMessage());
			exception.printStackTrace();
		}
	}
		
	public static void main(String... s) {
		try {
			TestStatusEnquiry test1 = new TestStatusEnquiry("1004210402170443", "9636562/00016000022/05082021182245205", "84000", "STATUS",
					"356", "77F35FB7BA37EEAE54304EF855D1C3DA4A73456F78876B16914783C280C55851", saleRequestMap1);

			TestStatusEnquiry test2 = new TestStatusEnquiry("1000310206210440", "9636562/00016000022/05082021182245205", "59000", "STATUS",
					"356", "984BB0FEC5E0573E17E4CF2C4C0A831B07DA9BAAA2E54B86FD0CEF72715ADD05", saleRequestMap2);

				Thread t1 = new Thread(test1);
				t1.setName("Thread1");
				t1.start();
				
				Thread t2 = new Thread(test2);
				t2.setName("Thread2");
				t2.start();

			/*Thread t3 = new Thread(test3);
			t3.setName("Thread3");
			t3.start();*/

		} catch (Exception ex) {
			System.out.println("exception caught ion main class " + ex);
			ex.printStackTrace();
		}

	}

}
