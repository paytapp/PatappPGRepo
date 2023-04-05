package com.paymentgateway.crypto.scrambler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.crypto.util.Validator;

@Service
public class CryptoService {
	
	@Autowired
	private ScramblerService scramblerService;
	
	@Autowired
	private Validator validator;

	
	public String decrypt(String payId,String data) throws SystemException {
		validator.validateRequest(payId, data);
		Scrambler scrambler = scramblerService.getScrambler(payId);
		return scrambler.decrypt(data);
	}

	public String encrypt(String payId, String data) throws SystemException {
		validator.validateRequest(payId, data);
		Scrambler scrambler = scramblerService.getScrambler(payId);
		return scrambler.encrypt(data);
	}
	
	public String hostedDecrypt(String payId,String data) throws SystemException {
		validator.validateRequest(payId, data);
		Scrambler scrambler = scramblerService.getHostedScrambler(payId);
		return scrambler.hostedDecrypt(data);
	}

	public String hostedEncrypt(String payId, String data) throws SystemException {
		validator.validateRequest(payId, data);
		Scrambler scrambler = scramblerService.getHostedScrambler(payId);
		return scrambler.hostedEncrypt(data);
	}
}