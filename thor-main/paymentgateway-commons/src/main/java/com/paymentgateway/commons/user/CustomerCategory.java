package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.List;

public enum CustomerCategory {
	DEFAULT, SILVER, GOLD, DIAMOND, PLATINUM;

	public static String[] getCustomerCategory() {
		String customerCategoryType[] = null;
		List<String> list = new ArrayList<String>();
		for (CustomerCategory customerCategory : CustomerCategory.values()) {
			list.add(customerCategory.toString());
		}
		list.remove(list.size() - 1);
		customerCategoryType = list.toString().replace("[", "").replace("]", "").replaceAll(" ", "").split(",");
		return customerCategoryType;
	}
}
