package com.paymentgateway.phonePe;

import java.io.IOException;

public interface PhonePeService {
	
	
	
	public String payRequestForUPIOpenIntentFlow();
	
	public String payRequestForUPICollect();
	
	public String payRequestForUPIQr();
	
	public String payRequestForNewCardFlow();
	
	public String payRequestWithCardId();
	
	public String payRequestWithToken();
	
	public String payRequestForNetBanking();
	
	public String initiatePhonepePayment(String purchaseId, String userId, int txnAmount) throws IOException;
	
//	public void savePhonePeTransaction(Transaction entity);
	
	public String getTransactionStatus(String merchantTransactionId);

	public void savePhonePeRequest(ResponseBean bean, String purchaseId, String userId);
	
	public void saveTransactionStatusResponse(StatusCheckResponseBean bean);
	
}
