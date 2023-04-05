package com.paymentgateway.commons.util;

import java.math.BigDecimal;

public class PercentageCalculator {
	public static final BigDecimal ONEHUNDERD = new BigDecimal(100);
	
	public static BigDecimal getPercentage(BigDecimal amount,double deduction){
	
		return amount.multiply(BigDecimal.valueOf(deduction).divide(ONEHUNDERD));		
	}
}
