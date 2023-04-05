package com.paymentgateway.pgui.action;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

public class IndustryId {	

	public static List<String> getIndustryId(String industryCategory) {
		List<String> idName = new LinkedList<String>();
		Map<String, String> categories = PropertiesManager.getAllIndustryCategories();
		String industryIdString = categories.get(industryCategory);
		// String industryIdString = new
		// PropertiesManager().getIndustryidpropertiesfile(industryCategory);
		String[] industryIdArray = industryIdString.split(Constants.COMMA.getValue());

		for (String id : industryIdArray) {
			idName.add(id);
		}
		return idName;
	}

}
