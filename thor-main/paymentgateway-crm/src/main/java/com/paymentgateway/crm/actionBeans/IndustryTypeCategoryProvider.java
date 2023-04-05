package com.paymentgateway.crm.actionBeans;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class IndustryTypeCategoryProvider {
	private static Logger logger = LoggerFactory.getLogger(IndustryTypeCategoryProvider.class.getName());

	public Map<String, String> industryTypes(User user) {
		Map<String, String> tempMap;
		String industryKey = user.getIndustryCategory();

		Map<String, String> industryTypesMap = new LinkedHashMap<String, String>();
		if (user.getUserType().equals(UserType.MERCHANT)) {
			tempMap = BusinessType.getIndustryCategoryList();
			if (tempMap.containsKey(industryKey)) {
				industryTypesMap.put("Default", tempMap.get(industryKey));
			}
			for (Entry<String, String> entry : tempMap.entrySet()) {
				try {
					industryTypesMap.put(entry.getKey(), entry.getValue());
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}
		} 
		return industryTypesMap;
	}
}
