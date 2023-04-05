package com.paymentgateway.pgui.action.service;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
@Service
public class CardBinProcessor {
	
	public String getCardBin(Fields fields) {
		String cardNumber = fields.get(FieldType.CARD_NUMBER.getName());
		String cardBin = cardNumber.replace(" ", "").replace(",", "").substring(0, 6);
		
		return cardBin;
	}
	

}
