package com.paymentgateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
	
	@Autowired
	ValueService valueService;
	
	public String getMsg(String input) {
		String output = input+"  "+"  email Id " +"  "+valueService.getVal(input);
		return output;
	}

}
