package com.paymentgateway.phonePe;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.paymentgateway.commons.user.MPAMerchant;

import okhttp3.Response;

@Service
//@Transactional
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
	
	@Override
	public void savePhonePeTransaction(Transaction entity) {
		try {
			phonePeDao.savePhonePeTransaction(entity);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

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
	
	


}
