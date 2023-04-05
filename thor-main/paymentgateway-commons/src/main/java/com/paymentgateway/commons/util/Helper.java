package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Helper {

	public static Collection<String> parseFields(String commaSepratedList){
		String[] fieldNames = commaSepratedList.split(",");

		List<String> list = new ArrayList<String>();
		for (String name : fieldNames) {
			name.trim();
			if (!name.isEmpty()) {
				list.add(name);
			}
		}

		return list;
	}
}
