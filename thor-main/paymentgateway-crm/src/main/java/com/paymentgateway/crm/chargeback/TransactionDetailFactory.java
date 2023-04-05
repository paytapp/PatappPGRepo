package com.paymentgateway.crm.chargeback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.mongo.MongoInstance;

public class TransactionDetailFactory {
	@Autowired
	MongoInstance mongoInstance;
	
	
	public  static TransactionDetailProvider getTransactionDetail(){
		return new DefaultTransactionDetailProvider();
	}
}
