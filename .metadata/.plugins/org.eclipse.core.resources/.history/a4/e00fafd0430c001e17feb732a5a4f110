package com.paymentgateway.phonePe;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import okhttp3.Response;


@RestController
@RequestMapping("/phonePe")
public class PhonePeController {
	
	@Autowired
	private PhonePeService phonePeService;
	
	@Autowired
	private RequestCreatorPhonpe requestCreaterPhonePe;
	

	@RequestMapping(method = RequestMethod.POST, value = "/UPIOpenIntentFlow")
	public String payRequestForUPIOpenIntentFlow(@RequestBody PayRequestDetailsBean payload) throws IOException {
		
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("UPIOpenIntent", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/UPICollect")
	public String payRequestForUPICollect(@RequestBody PayRequestDetailsBean payload) throws IOException {
		
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("UPICollect", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/UPIQR")
	public String payRequestForUPIQr(@RequestBody PayRequestDetailsBean payload) throws IOException {
		
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("UPIQR", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/newCard")
	public String payRequestForNewCardFlow(@RequestBody PayRequestDetailsBean payload) throws IOException {
		
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("CardNumber", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;

		
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/carId")
	public String payRequestWithCardId(@RequestBody PayRequestDetailsBean payload) throws IOException {
		
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("CardId", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/token")
	public String payRequestWithToken(@RequestBody PayRequestDetailsBean payload) throws IOException {
		
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("Token", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/netBanking")
	public String payRequestForNetBanking(@RequestBody PayRequestDetailsBean payload) throws IOException {
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("NetBanking", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/Web")
	public String payRequestForWeb(@RequestBody PayRequestDetailsBean payload) throws IOException {
		System.out.println("controller payload :::::::::::::::::: " + payload.getPurchaseId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getUserId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getTxnAmount());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPhNo());
		System.out.println("controller payload :::::::::::::::::: " + payload.getBankId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncCardNumber());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardHName());
		System.out.println("controller payload :::::::::::::::::: " + payload.getMonth());
		System.out.println("controller payload :::::::::::::::::: " + payload.getYear());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCvv());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine1());
		System.out.println("controller payload :::::::::::::::::: " + payload.getAddressLine2());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCity());
		System.out.println("controller payload :::::::::::::::::: " + payload.getState());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPin());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCountry());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCardId());
		System.out.println("controller payload :::::::::::::::::: " + payload.getCryptoGram());
		System.out.println("controller payload :::::::::::::::::: " + payload.getPanSuffix());
		System.out.println("controller payload :::::::::::::::::: " + payload.getEncryptedToken1());
		
		
		return requestCreaterPhonePe.initiatePhonepePayment("Web", payload.getPurchaseId(), 
				payload.getUserId(), payload.getTxnAmount(), payload.getPhNo(), 
				payload.getBankId(), payload.getEncCardNumber(), payload.getCardHName(), 
				payload.getMonth(), payload.getYear(), payload.getCvv(), payload.getAddressLine1(), 
				payload.getAddressLine2(), payload.getCity(), payload.getState(), payload.getPin(), 
				payload.getCountry(), payload.getCardId(), payload.getCryptoGram(), 
				payload.getPanSuffix(), payload.getEncryptedToken1()) ;
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/payment", produces = "application/json")
	public String payment(@RequestParam("purchaseId") String purchaseId, 
						  	@RequestParam("userId") String userId,
						  	@RequestParam("transactionAmount") int txnAmount) throws IOException {
//		String purchaseId = "1006";
//		String userId = "629388";
//		int txnAmount = 116;
		String responseStr = phonePeService.initiatePhonepePayment(purchaseId, userId, txnAmount);
		
		
		return responseStr;
		}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/transaction/status")
	public String getTransactionStatus() {
		return phonePeService.getTransactionStatus("1006");
		}
	
	
	
	
	
	
}
