package com.paymentgateway.crypto.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.crypto.scrambler.CryptoService;

@Controller
public class CryptoController {
	@Autowired
	private CryptoService cryptoService;

	@RequestMapping(method = RequestMethod.POST, value = "/decrypt" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> decrypt(@RequestBody  Map<String, String> reqmap) throws SystemException {
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(FieldType.ENCDATA.getName(), (cryptoService.decrypt(reqmap.get(FieldType.PAY_ID.getName()),reqmap.get(FieldType.ENCDATA.getName()))));
		return responseMap;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/encrypt" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> encrypt(@RequestBody  Map<String, String> reqmap) throws SystemException {
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(FieldType.ENCDATA.getName(), (cryptoService.encrypt(reqmap.get(FieldType.PAY_ID.getName()),reqmap.get(FieldType.ENCDATA.getName()))));
		return responseMap;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/hostedDecrypt" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> hostedDecrypt(@RequestBody  Map<String, String> reqmap) throws SystemException {
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(FieldType.ENCDATA.getName(), (cryptoService.hostedDecrypt(reqmap.get(FieldType.PAY_ID.getName()),reqmap.get(FieldType.ENCDATA.getName()))));
		return responseMap;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/hostedEncrypt" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> hostedEncrypt(@RequestBody  Map<String, String> reqmap) throws SystemException {
		Map<String,String> responseMap = new HashMap<String,String>();
		responseMap.put(FieldType.ENCDATA.getName(), (cryptoService.hostedEncrypt(reqmap.get(FieldType.PAY_ID.getName()),reqmap.get(FieldType.ENCDATA.getName()))));
		return responseMap;
	}
}
