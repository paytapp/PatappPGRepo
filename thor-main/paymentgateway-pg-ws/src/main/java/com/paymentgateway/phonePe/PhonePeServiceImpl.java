package com.paymentgateway.phonePe;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.paymentgateway.commons.user.MPAMerchant;

import okhttp3.Response;

@Service
@Transactional
public class PhonePeServiceImpl implements PhonePeService {

	@Autowired
	private PhonePeDao phonePeDao;
	
	@Autowired
	private initiatePhonepe initiatePhonePe;
	
	@Autowired
	private PhonePeTransactionStatusCheck phonePeTransactionStatusCheck;
	
	@Override
	public String payRequestForUPIOpenIntentFlow() {
		return "payRequestForUPIOpenIntentFlow() PhonePe Service is working";
	}

	@Override
	public String payRequestForUPICollect() {
		return "payRequestForUPICollect() PhonePe Service is working";
	}

	@Override
	public String payRequestForUPIQr() {
		return "payRequestForUPIQr() PhonePe Service is working";
	}

	@Override
	public String payRequestForNewCardFlow() {
		return "payRequestForNewCardFlow() PhonePe Service is working";
	}

	@Override
	public String payRequestWithCardId() {
		return "payRequestWithCardId() PhonePe Service is working";
	}

	@Override
	public String payRequestWithToken() {
		return "payRequestWithToken() PhonePe Service is working";
	}

	@Override
	public String payRequestForNetBanking() {
		return "payRequestForNetBanking() PhonePe Service is working";
	}

	@Override
	public String initiatePhonepePayment(String purchaseId, String userId, int txnAmount) throws IOException {
		String result = initiatePhonePe.initiatePhonepePayment(purchaseId, userId, txnAmount);
//		String responseStr = result.body().string();
		System.out.println("Response body is ---::;   "+result);
//		System.out.println("====="+result.body());
//		System.out.println("====="+result.body().string());
//		System.out.println("====="+result.body().toString());
		
		return result;
	}
	
//	@Override
//	public void savePhonePeTransaction(Transaction entity) {
//		try {
//			phonePeDao.savePhonePeTransaction(entity);
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public String getTransactionStatus(String merchantTransactionId) {
		
			try {
				return phonePeTransactionStatusCheck.checkPaymentStatus(merchantTransactionId);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		
	}
	
	@Override
	public void savePhonePeRequest(ResponseBean bean, String purchaseId, String userId) {

		try {
		Transaction transaction = new Transaction();
		PaymentResponse response = new PaymentResponse();		
		
		response.setSuccess(bean.getSuccess());
		response.setCode(bean.getCode());
		response.setMessage(bean.getMessage());
		
		if(bean.getData() != null) {
			response.setMerchantId(bean.getData().getMerchantId());
			response.setMerchantTransactionId(bean.getData().getMerchantTransactionId());
			response.setTransactionId(bean.getData().getTransactionId());
			
			if(bean.getData().getInstrumentResponse() != null) {
				response.setType(bean.getData().getInstrumentResponse().getType());

				if(bean.getData().getInstrumentResponse().getRedirectInfo() != null) {
					response.setUrl(bean.getData().getInstrumentResponse().getRedirectInfo().getUrl());
					response.setMethod(bean.getData().getInstrumentResponse().getRedirectInfo().getMethod());
				}
			}
		}
		
		response.setCreatedDate(new Date());
		response.setCreatedBy("");
		
		response = phonePeDao.savePaymentResponse(response);
		 
		transaction.setPurchaseId(purchaseId);
		transaction.setUserId(userId);
		transaction.setTransactionAmount(bean.getAmount());
		transaction.setTransactionSuccess(bean.getSuccess());
		transaction.setPaymentResponseIdRef(response);
		transaction.setMerchantId(bean.getData().getMerchantId());
		transaction.setCreatedDate(new Date());
		transaction.setCreatedBy("");

		phonePeDao.savePhonePeTransaction(transaction);
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void saveTransactionStatusResponse(StatusCheckResponseBean bean) {
        StatusCheckResponse response = new StatusCheckResponse();

        response.setSuccess(bean.getSuccess());
        response.setCode(bean.getCode());
        response.setMessage(bean.getMessage());
        
        if(bean.getData() != null) {
	        response.setMerchantId(bean.getData().getMerchantId());
	        response.setMerchantTransactionId(bean.getData().getMerchantTransactionId());
	        response.setTransactionId(bean.getData().getTransactionId());
	        response.setAmount(bean.getData().getAmount());
	        response.setState(bean.getData().getState());
	        response.setResponseCode(bean.getData().getResponseCode());
	        response.setType(bean.getData().getState());
	        
	        if(bean.getData().getPaymentInstrument() != null) {
		        response.setUtr(bean.getData().getPaymentInstrument().getUtr());
		        response.setCardType(bean.getData().getPaymentInstrument().getCardType());
		        response.setPgTransactionId(bean.getData().getPaymentInstrument().getPgTransactionId());
		        response.setBankTransactionId(bean.getData().getPaymentInstrument().getBankTransactionId());
		        response.setPgAuthorizationCode(bean.getData().getPaymentInstrument().getPgAuthorizationCode());
		        response.setArn(bean.getData().getPaymentInstrument().getArn());
		        response.setBankId(bean.getData().getPaymentInstrument().getBankId());
		        response.setPgServiceTransactionId(bean.getData().getPaymentInstrument().getPgServiceTransactionId());
	        }
        }
        
        response.setCreatedDate(new Date());
        response.setCreatedBy("");
	        
        phonePeDao.saveTransactionStatusResponse(response);
        System.out.println(response.getId());
        
	}
	
	public void saveTransactionDetails(ResponseBean bean, String purchaseId, String userId, String payload, String responseStr) {
		TransactionDetailsEntity entity = new TransactionDetailsEntity();
		
		entity.setPurchaseId(purchaseId);
		entity.setUserId(userId);
		entity.setTransactionAmount(bean.getAmount());
		entity.setPaymentRequestSuccess(bean.getSuccess());
		entity.setMerchantId(bean.getData().getMerchantId());
		entity.setPaymentType(bean.getData().getInstrumentResponse().getType());
		entity.setPaymentRequestJson(payload);
		entity.setPaymentResponseJson(responseStr);
		entity.setCreatedDate(new Date());
		entity.setCreatedBy("");
		
		phonePeDao.saveTransactionDetails(entity);
		
	}



}
