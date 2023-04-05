package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.List;

public enum CardHolderType {
	COMMERCIAL,CONSUMER,PREMIUM,ALL;
	
	public static String[] getCardHolderType() {
		String cardHolderType[] = null;
		List<String> list = new ArrayList<String>();
		for(CardHolderType cardHolder : CardHolderType.values()) {
			list.add(cardHolder.toString());
		}
		list.remove(list.size()-1);
		cardHolderType = list.toString().replace("[", "").replace("]", "").replaceAll(" ", "").split(",");
		return cardHolderType;
	}
}
