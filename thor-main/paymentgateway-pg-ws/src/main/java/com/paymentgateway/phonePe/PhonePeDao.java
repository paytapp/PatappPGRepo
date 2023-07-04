package com.paymentgateway.phonePe;

public interface PhonePeDao {
	
	public void savePhonePeTransaction(Transaction entity);

	public String payRequestForUPIOpenIntentFlow();
	
	public String payRequestForUPICollect();
	
	public String payRequestForUPIQr();
	
	public String payRequestForNewCardFlow();
	
	public String payRequestWithCardId();
	
	public String payRequestWithToken();
	
	public String payRequestForNetBanking();
	
	public PaymentResponse savePaymentResponse(PaymentResponse response);
	
	public StatusCheckResponse saveTransactionStatusResponse(StatusCheckResponse response);
	
}
