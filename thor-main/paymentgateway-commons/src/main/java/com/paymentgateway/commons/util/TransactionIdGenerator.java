package com.paymentgateway.commons.util;

import org.springframework.stereotype.Service;

@Service
public class TransactionIdGenerator {

	protected int value;
	protected final int initialValue;
	protected final int maxValue;

	public TransactionIdGenerator() {
		this(1, Integer.MAX_VALUE);
	}
	
	public TransactionIdGenerator(int initValue, int maxValue) {
		if (initValue < 0) {
			throw new IllegalArgumentException("Initial value '" + initValue
					+ "' must be a postive number.");
		} else if (maxValue <= initValue) {
			throw new IllegalArgumentException("Max value '" + maxValue
					+ "' is less than or equals to initial value '" + initValue
					+ "'");
		}
		this.value = initValue;
		this.initialValue = initValue;
		this.maxValue = maxValue;
	}

	public TransactionIdGenerator(int initValue) {
		this(initValue, Integer.MAX_VALUE);
	}
	
	public synchronized int next(){
		
		if(value > maxValue){
			value = initialValue;
		}
		
		return value++;
	}
}
