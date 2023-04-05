package com.paymentgateway.commons.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BusinessType {

	private static final String categoryPropertyFile = "industry_category.properties";
//	private static final String prefix = "INDUSTRY_SUB_CATEGORY_";
	public static List<String> getIndustrySubcategory(String industryCategory){
		
		// Shaiwal - Sub Category is no longer being maintained for a User , so send an empty list instead
		List<String> subCategories = new LinkedList<String>();
		/*
		 * String industrySubCategoryString =
		 * PropertiesManager.propertiesMap.get(prefix+industryCategory); String[]
		 * industrySubcategoryArray =
		 * industrySubCategoryString.split(Constants.COMMA.getValue());
		 * 
		 * for(String subCategoey: industrySubcategoryArray){
		 * subCategories.add(subCategoey); }
		 */
		return subCategories;
	}

	public static Map<String,String> getIndustryCategoryList(){
		Map<String,String> categories = new LinkedHashMap<String, String>();
		categories =	new PropertiesManager().getAllProperties(categoryPropertyFile);
		return categories;
	}
}
