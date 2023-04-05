package com.paymentgateway.pg.core.util;


import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.struts2.dispatcher.SessionMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.DynamicPaymentPage;
import com.paymentgateway.commons.user.DynamicPaymentPageDao;
import com.paymentgateway.commons.util.DynamicCssConstants;


@Service
public class FetchDynamicPaymentPage {
	@Autowired
	DynamicPaymentPageDao dynamicPaymentPageDao ;
	
	DynamicPaymentPage dynamicPaymentPage  = new  DynamicPaymentPage();
	
	private static Logger logger = LoggerFactory.getLogger(FetchDynamicPaymentPage.class.getName());

	public  void FetchDynamicPayID(String payId,SessionMap<String, Object> sessionMap ) {
		try
		{
		dynamicPaymentPage = new DynamicPaymentPageDao().findPayId(payId);
		if (dynamicPaymentPage==null){
			sessionMap.put(DynamicCssConstants.PAGE_TITTLE.toString(), DynamicCssConstants.PAGE_TITTLE.getValue());
			sessionMap.put(DynamicCssConstants.TAB_BACKGROUND_COLOR.toString(), DynamicCssConstants.TAB_BACKGROUND_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.BACKGROUND_COLOR.toString(), DynamicCssConstants.BACKGROUND_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.TEXT_STYLE.toString(), DynamicCssConstants.TEXT_STYLE.getValue());
			sessionMap.put(DynamicCssConstants.TEXT_COLOR.toString(), DynamicCssConstants.TEXT_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.HYPERLINK_COLOR.toString(), DynamicCssConstants.HYPERLINK_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.BOX_BACKGROUND_COLOR.toString(), DynamicCssConstants.BOX_BACKGROUND_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.TOP_BAR_COLOR.toString(),DynamicCssConstants.TOP_BAR_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.TAB_BACKGROUND_COLOR.toString(), DynamicCssConstants.TAB_BACKGROUND_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.TAB_TEXT_COLOR.toString(), DynamicCssConstants.TAB_TEXT_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.ACTIVE_TAB_COLOR.toString(), DynamicCssConstants.ACTIVE_TAB_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.ACTIVE_TAB_TEXT_COLOR.toString(), DynamicCssConstants.ACTIVE_TAB_TEXT_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.BUTTON_BACKGROUND_COLOR.toString(), DynamicCssConstants.BUTTON_BACKGROUND_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.BUTTON_TEXT_COLOR.toString(), DynamicCssConstants.BUTTON_TEXT_COLOR.getValue());
			sessionMap.put(DynamicCssConstants.BORDER_COLOR.toString(), DynamicCssConstants.BORDER_COLOR.getValue());
				}
				else{
		sessionMap.put(DynamicCssConstants.PAGE_TITTLE.toString(), dynamicPaymentPage.getPageTittle().toString());
		sessionMap.put(DynamicCssConstants.TAB_BACKGROUND_COLOR.toString(), dynamicPaymentPage.getTabBackgroundColor().toString());
		sessionMap.put(DynamicCssConstants.BACKGROUND_COLOR.toString(), dynamicPaymentPage.getBackgroundColor().toString());
		sessionMap.put(DynamicCssConstants.TEXT_STYLE.toString(), dynamicPaymentPage.getTextStyle().toString());
		sessionMap.put(DynamicCssConstants.TEXT_COLOR.toString(), dynamicPaymentPage.getTextColor().toString());
		sessionMap.put(DynamicCssConstants.HYPERLINK_COLOR.toString(), dynamicPaymentPage.getHyperlinkColor().toString());
		sessionMap.put(DynamicCssConstants.BOX_BACKGROUND_COLOR.toString(), dynamicPaymentPage.getBoxBackgroundColor().toString());
		sessionMap.put(DynamicCssConstants.TOP_BAR_COLOR.toString(), dynamicPaymentPage.getTopBarColor().toString());
		sessionMap.put(DynamicCssConstants.TAB_BACKGROUND_COLOR.toString(), dynamicPaymentPage.getTabBackgroundColor().toString());
		sessionMap.put(DynamicCssConstants.TAB_TEXT_COLOR.toString(), dynamicPaymentPage.getTabTextColor().toString());
		sessionMap.put(DynamicCssConstants.ACTIVE_TAB_COLOR.toString(), dynamicPaymentPage.getActiveTabColor().toString());
		sessionMap.put(DynamicCssConstants.ACTIVE_TAB_TEXT_COLOR.toString(), dynamicPaymentPage.getActiveTabTextColor().toString());
		sessionMap.put(DynamicCssConstants.BUTTON_BACKGROUND_COLOR.toString(), dynamicPaymentPage.getBackgroundColor().toString());
		sessionMap.put(DynamicCssConstants.BUTTON_TEXT_COLOR.toString(), dynamicPaymentPage.getButtonTextColor().toString());
		sessionMap.put(DynamicCssConstants.BORDER_COLOR.toString(), dynamicPaymentPage.getBorderColor().toString());
				}
		
		}
		catch (Exception exception) {
			logger.error("Exception", exception);
			
		}
	}
	
	public   DynamicPaymentPage FetchDynamicDesignPage (String payId) {
		try
		{
		dynamicPaymentPage = new DynamicPaymentPageDao().findPayId(payId);
		if (dynamicPaymentPage == null){
			dynamicPaymentPage = new DynamicPaymentPage();
			dynamicPaymentPage.setPageTittle(DynamicCssConstants.PAGE_TITTLE.getValue());
			dynamicPaymentPage.setTabBackgroundColor(DynamicCssConstants.TAB_BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setBackgroundColor(DynamicCssConstants.BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setTextStyle(DynamicCssConstants.TEXT_STYLE.getValue());
			dynamicPaymentPage.setTextColor(DynamicCssConstants.TEXT_COLOR.getValue());
			dynamicPaymentPage.setHyperlinkColor(DynamicCssConstants.HYPERLINK_COLOR.getValue());
			dynamicPaymentPage.setBoxBackgroundColor( DynamicCssConstants.BOX_BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setTopBarColor(DynamicCssConstants.TOP_BAR_COLOR.getValue());
			dynamicPaymentPage.setTabTextColor(DynamicCssConstants.TAB_TEXT_COLOR.getValue());
			dynamicPaymentPage.setActiveTabColor(DynamicCssConstants.ACTIVE_TAB_COLOR.getValue());
			dynamicPaymentPage.setActiveTabTextColor(DynamicCssConstants.ACTIVE_TAB_TEXT_COLOR.getValue());
			dynamicPaymentPage.setButtonBackgoundColor(DynamicCssConstants.BUTTON_BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setButtonTextColor(DynamicCssConstants.BUTTON_TEXT_COLOR.getValue());
			dynamicPaymentPage.setBorderColor( DynamicCssConstants.BORDER_COLOR.getValue());
				}
			else{
			dynamicPaymentPage.setPageTittle(dynamicPaymentPage.getPageTittle().toString());			
			dynamicPaymentPage.setTabBackgroundColor(dynamicPaymentPage.getTabBackgroundColor().toString());
			dynamicPaymentPage.setBackgroundColor(dynamicPaymentPage.getBackgroundColor().toString());
			dynamicPaymentPage.setTextStyle(dynamicPaymentPage.getTextStyle().toString());
			dynamicPaymentPage.setTextColor(dynamicPaymentPage.getTextColor().toString());
			dynamicPaymentPage.setHyperlinkColor(dynamicPaymentPage.getHyperlinkColor().toString());
			dynamicPaymentPage.setBoxBackgroundColor(dynamicPaymentPage.getBoxBackgroundColor().toString());
			dynamicPaymentPage.setTopBarColor(dynamicPaymentPage.getTopBarColor().toString());
			dynamicPaymentPage.setTabTextColor(dynamicPaymentPage.getTabTextColor().toString());
			dynamicPaymentPage.setActiveTabColor(dynamicPaymentPage.getActiveTabColor().toString());
			dynamicPaymentPage.setActiveTabTextColor(dynamicPaymentPage.getActiveTabTextColor().toString());
			dynamicPaymentPage.setButtonBackgoundColor(dynamicPaymentPage.getButtonBackgoundColor().toString());
			dynamicPaymentPage.setButtonTextColor(dynamicPaymentPage.getButtonTextColor().toString());
			dynamicPaymentPage.setBorderColor(dynamicPaymentPage.getBorderColor().toString());
				}
		return dynamicPaymentPage;
		}
		
		catch (Exception exception) {
			logger.error("Exception", exception);
			
		}
		return dynamicPaymentPage;
	}

	public   DynamicPaymentPage SetDefault (String payId) {
		try{
			dynamicPaymentPage.setPageTittle(DynamicCssConstants.PAGE_TITTLE.getValue());
			dynamicPaymentPage.setTabBackgroundColor(DynamicCssConstants.TAB_BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setBackgroundColor(DynamicCssConstants.BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setTextStyle(DynamicCssConstants.TEXT_STYLE.getValue());
			dynamicPaymentPage.setTextColor(DynamicCssConstants.TEXT_COLOR.getValue());
			dynamicPaymentPage.setHyperlinkColor(DynamicCssConstants.HYPERLINK_COLOR.getValue());
			dynamicPaymentPage.setBoxBackgroundColor( DynamicCssConstants.BOX_BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setTopBarColor(DynamicCssConstants.TOP_BAR_COLOR.getValue());
			dynamicPaymentPage.setTabTextColor(DynamicCssConstants.TAB_TEXT_COLOR.getValue());
			dynamicPaymentPage.setActiveTabColor(DynamicCssConstants.ACTIVE_TAB_COLOR.getValue());
			dynamicPaymentPage.setActiveTabTextColor(DynamicCssConstants.ACTIVE_TAB_TEXT_COLOR.getValue());
			dynamicPaymentPage.setButtonBackgoundColor(DynamicCssConstants.BUTTON_BACKGROUND_COLOR.getValue());
			dynamicPaymentPage.setButtonTextColor(DynamicCssConstants.BUTTON_TEXT_COLOR.getValue());
			dynamicPaymentPage.setBorderColor( DynamicCssConstants.BORDER_COLOR.getValue());
			
		}
		
		
		catch (Exception exception) {
			logger.error("Exception", exception);
			
		}
		return dynamicPaymentPage;
		
	}

}
