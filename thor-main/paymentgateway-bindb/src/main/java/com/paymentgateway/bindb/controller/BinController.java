package com.paymentgateway.bindb.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.bindb.core.BinRangeProvider;
import com.paymentgateway.commons.util.FieldType;
@RestController
public class BinController {
	
	@Autowired
	private BinRangeProvider binRangeProvider;
	
	@RequestMapping(method = RequestMethod.GET,value = "/findbin/{binRange}")
	public Map<String,String> findBin(@PathVariable String binRange){
		String[] paramaters = binRange.split("&");
		Map<String, String> paramMap = new HashMap<String, String>();
		for (String param : paramaters) {
			String[] parameterPair = param.split("=");
			if (parameterPair.length > 1) {
				paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
			}
		}
		return binRangeProvider.findBinRange(paramMap.get(FieldType.CARD_NUMBER.getName()), paramMap.get(FieldType.PAY_ID.getName()));	
	}
	
	@RequestMapping(method = RequestMethod.GET,value = "/findEmiBin/{binRange}")
	public Map<String,String> findEMIBin(@PathVariable String binRange){
		return binRangeProvider.findEMIBinRange(binRange);	
	}

}
