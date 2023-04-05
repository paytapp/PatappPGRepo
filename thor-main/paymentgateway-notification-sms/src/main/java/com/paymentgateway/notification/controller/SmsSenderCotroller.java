package com.paymentgateway.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.notification.sms.sendSms.SmsSenderData;
@RestController
public class SmsSenderCotroller {
	@Autowired
	private SmsSenderData smsSender ;
	
	@RequestMapping(method = RequestMethod.POST,value = "/sendPromoSMS/{shortUrl}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Invoice  sendPromoSMS(@RequestBody  Invoice invoiceDB, @PathVariable String shortUrl){
		smsSender.sendPromoSMS(invoiceDB, shortUrl);
		return invoiceDB;
		
	}
}
