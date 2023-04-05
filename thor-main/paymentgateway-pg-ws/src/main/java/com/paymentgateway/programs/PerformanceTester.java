package com.paymentgateway.programs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.paymentgateway.commons.util.PaymentType;

public class PerformanceTester implements Runnable {
	private String hostUrl = "http://localhost:8080/crm/services/paymentServices/transact";

	private String request = "{ \"CURRENCY_CODE\":\"356\","
			+ "\"CUST_NAME\":\"Surender Kumar\", \"TXNTYPE\":\"SALE\", \"AMOUNT\":\"100\", "
			+ "\"PASSWORD\":\"password1\", \"PAY_ID\":\"1704201656101001\"," 
			+ "\"CUST_EMAIL\":\"surender@paymentGateway.com\", \"CUST_PHONE\":\"9718162185\","
			+ "\"CUST_ZIP\":\"1\", \"HASH\":\"1234567890123456789012345678901234567890123456789012345678901234\","
			+ "\"PAYMENT_TYPE\":\"NB\",\"MOP_TYPE\":\"1005\",";

	private int threadCount;
	private int requestsPerThread;
	private Thread thread;
	private int number;
	private double totalTimeTaken;
	private boolean finished;
	private long successCount;

	public static void main(String[] args) throws InterruptedException {
		// Arguments
		// 1. hostUrl
		// 2. number of threads
		// 3. number of requests per thread
		double totalTimeTakenForAllRequests = 0;
		long totalSuccessFullTransactions = 0;

		PerformanceTester[] clients = new PerformanceTester[Integer
				.parseInt(args[1])];
		for (int i = 0; i < Integer.parseInt(args[1]); i++) {
			clients[i] = new PerformanceTester(args[0], args[1], args[2], i);
			clients[i].getThread().start();
		}

		for (int i = 0; i < Integer.parseInt(args[1]);) {
			if (!clients[i].isFinished()) {
				Thread.sleep(100);
				i = 0;
			} else {
				i++;
			}
		}

		for (int i = 0; i < Integer.parseInt(args[1]); i++) {
			totalTimeTakenForAllRequests = totalTimeTakenForAllRequests
					+ clients[i].getTotalTimeTaken();
			totalSuccessFullTransactions = totalSuccessFullTransactions
					+ clients[i].getSuccessCount();
		}

		totalTimeTakenForAllRequests = totalTimeTakenForAllRequests
				/ clients[0].getThreadCount();

		long totalReqs = clients[0].threadCount * clients[0].requestsPerThread;

		System.out.println("Total Requests = " + totalReqs);
		System.out.println("Total Sucessful Transactions = "
				+ totalSuccessFullTransactions);
		System.out.println("Total Seconds taken "
				+ totalTimeTakenForAllRequests);
		System.out.println("Total Minutes taken "
				+ totalTimeTakenForAllRequests / 60);

		if (totalTimeTakenForAllRequests != 0) {
			System.out.println("Transactions per second = " + totalReqs
					/ totalTimeTakenForAllRequests);
		}
	}

	public PerformanceTester(String hostUrl, String threadCount,
			String requestsPerThread, int number) {
		this.hostUrl = hostUrl;
		this.threadCount = Integer.parseInt(threadCount);
		this.requestsPerThread = Integer.parseInt(requestsPerThread);
		this.thread = new Thread(this, "Thread-" + number);
		this.number = number;
		this.finished = false;
	}

	public String getResponse() {
		try {
			

			UUID uuid = UUID.randomUUID();
			String orderId = uuid.toString();
			String orderIdPadding = "\"ORDER_ID\":\"" + orderId + "\" }";
			String createDate = getCreateDate();
			String createDatePadding = "\"CREATE_DATE\":\""+createDate+"\",\"UPDATE_DATE\":\""+createDate +"\",";
			//payment method padding

			URL url = new URL(hostUrl);
			URLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			DataOutputStream dataoutputstream = new DataOutputStream(
					connection.getOutputStream());

			String jsonRequest = request + createDatePadding + orderIdPadding;

			dataoutputstream.writeBytes(jsonRequest);
			dataoutputstream.flush();
			dataoutputstream.close();
			BufferedReader bufferedreader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String decodedString;
			String response = "";
			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}

			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	static int counter=0;
	static LocalDateTime date = LocalDateTime.of(2017, 04, 24, 00, 00, 01);
	public static String getCreateDate(){
		final String format = "yyyy-MM-dd HH:mm:ss";
		PaymentType paymentType = PaymentType.CREDIT_CARD;
		
		switch(paymentType){
		case CREDIT_CARD:
			break;
		case DEBIT_CARD:
			break;
		case NET_BANKING:
			break;
		case WALLET:
			break;
		default:
			break;		
		}

		if(!(date.getHour()==10 || date.getHour()==11)){
			 if (counter == 84) {
					counter = 0;
					date = date.plusSeconds(1);
			}
		}
		else{
			if (counter == 556) {
				counter = 0;
				date = date.plusSeconds(1);
			}	 
		}
		counter++;

		return date.format(DateTimeFormatter.ofPattern(format));
	}

	public void run() {
		System.out.println("Thread " + number + " started");
		String response;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < requestsPerThread; i++) {
			response = getResponse();
			if (response != null && response.contains("\"RESPONSE_CODE\":\"000\"")) {
				successCount++;
				System.out.println("success " + successCount);
			} else {
				System.out.println("Thread " + number + " Aboarted! Response = " + response);
				break;
			}
		}

		long endTime = System.currentTimeMillis();
		double start = startTime / 1000;
		double end = endTime / 1000;
		double duration = end - start;
		System.out.println("Thread " + number + " finished in " + duration
				+ " seconds");

		totalTimeTaken = duration;
		finished = true;
	}

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getRequestsPerThread() {
		return requestsPerThread;
	}

	public void setRequestsPerThread(int requestsPerThread) {
		this.requestsPerThread = requestsPerThread;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public double getTotalTimeTaken() {
		return totalTimeTaken;
	}

	public void setTotalTimeTaken(long totalTimeTaken) {
		this.totalTimeTaken = totalTimeTaken;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public long getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(long successCount) {
		this.successCount = successCount;
	}
}
