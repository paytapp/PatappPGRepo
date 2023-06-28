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
	
}
