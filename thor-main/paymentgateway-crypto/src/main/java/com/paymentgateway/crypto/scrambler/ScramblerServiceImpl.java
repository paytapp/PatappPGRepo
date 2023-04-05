package com.paymentgateway.crypto.scrambler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.crypto.key.KeyProvider;

@Service("scramblerService")
public class ScramblerServiceImpl implements ScramblerService{

	private static Map<String,Scrambler> scramblerMap = new ConcurrentHashMap<String,Scrambler>();
	private static Map<String,Scrambler> hostedScramblerMap = new ConcurrentHashMap<String,Scrambler>();
	private static Logger logger = LoggerFactory.getLogger(ScramblerServiceImpl.class.getName());

	@Autowired
	private KeyProvider keyProvider;
	
	@Override
	public Scrambler getScrambler(String payId) throws SystemException {
		//create scrambler using the key and put in map or vice versa
		Scrambler scrambler = scramblerMap.get(payId);
		if(scrambler!=null) {
			return scrambler;
		}else {
			logger.info("Creating Scrambler instance for PAY_ID: " + payId);
			//Generate Key for that pay ID 
			String key = keyProvider.generateKey(payId);
			scrambler = new Scrambler(key);
			scramblerMap.put(payId, scrambler);
			return scrambler;
		}
	}
	
	@Override
	public Scrambler getHostedScrambler(String payId) throws SystemException {
		//create scrambler using the key and put in map or vice versa
		Scrambler scrambler = hostedScramblerMap.get(payId);
		if(scrambler!=null) {
			return scrambler;
		}else {
			logger.info("Creating Scrambler instance for PAY_ID: " + payId);
			//Generate Key for that pay ID 
			String key = keyProvider.generateHostedKey(payId);
			scrambler = new Scrambler(key);
			hostedScramblerMap.put(payId, scrambler);
			return scrambler;
		}
	}

	//RFU stub
	@Override
	public Scrambler getScramblerWithKey(String keyId) {
		//get Key for that pay ID from DB/map
		//create scrambler using the key
		return null;
	}

}
