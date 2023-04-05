package com.paymentgateway.pg.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.user.DynamicPaymentPage;
import com.paymentgateway.commons.user.DynamicPaymentPageDao;
import com.paymentgateway.commons.util.DynamicCssConstants;
/**
 *  @Isha
 */

@Service
public class DynamicPageUpdator extends AbstractSecureAction {
	
	@Autowired
	DynamicPaymentPageDao dynamicPaymentPageDao;

	private static final long serialVersionUID = -8589317522232234558L;
	private DynamicPaymentPage userFromDB = null;
	DynamicPaymentPage dynamicPaymentPage = new  DynamicPaymentPage();
	
	public DynamicPaymentPage updateUserDetails(DynamicPaymentPage dp, String payId){
		
		userFromDB = dynamicPaymentPageDao.findPayId(payId);
		if (userFromDB == null){
			userFromDB = new DynamicPaymentPage(); ////its taken new as for the first time there no details
			userFromDB.setPayId(payId);
			}
		
		userFromDB.setPageTittle(dp.getPageTittle());
		userFromDB.setBackgroundImage(dp.getBackgroundImage());
		userFromDB.setBackgroundColor(dp.getBackgroundColor());
		userFromDB.setTextStyle(dp.getTextStyle());
		userFromDB.setTextColor(dp.getTextColor());
		userFromDB.setHyperlinkColor(dp.getHyperlinkColor());
		userFromDB.setBoxBackgroundColor(dp.getBoxBackgroundColor());
		userFromDB.setTopBarColor(dp.getTopBarColor());
		userFromDB.setTabBackgroundColor(dp.getTabBackgroundColor());
		userFromDB.setTabTextColor(dp.getTabTextColor());
		userFromDB.setActiveTabColor(dp.getActiveTabColor());
		userFromDB.setActiveTabTextColor(dp.getActiveTabTextColor());
		userFromDB.setButtonBackgoundColor(dp.getButtonBackgoundColor());
		userFromDB.setButtonTextColor(dp.getButtonTextColor());
		userFromDB.setBorderColor(dp.getBorderColor());
				
		dynamicPaymentPageDao.update(userFromDB);
		
		return dynamicPaymentPage;
		
	}
	public DynamicPaymentPage insertDefaultPaymentPageCss(String payId){
		
		dynamicPaymentPage.setPayId(payId);
		dynamicPaymentPage.setPageTittle(DynamicCssConstants.PAGE_TITTLE.getValue());
		dynamicPaymentPage.setBackgroundColor(DynamicCssConstants.BACKGROUND_COLOR.getValue());
		dynamicPaymentPage.setTextStyle(DynamicCssConstants.TEXT_STYLE.getValue());
		dynamicPaymentPage.setTextColor(DynamicCssConstants.TEXT_COLOR.getValue());
		dynamicPaymentPage.setHyperlinkColor(DynamicCssConstants.HYPERLINK_COLOR.getValue());
		dynamicPaymentPage.setBoxBackgroundColor(DynamicCssConstants.BOX_BACKGROUND_COLOR.getValue());
		dynamicPaymentPage.setTopBarColor(DynamicCssConstants.TOP_BAR_COLOR.getValue());
		dynamicPaymentPage.setTabBackgroundColor(DynamicCssConstants.TAB_BACKGROUND_COLOR.getValue());
		dynamicPaymentPage.setTabTextColor(DynamicCssConstants.TAB_TEXT_COLOR.getValue());
		dynamicPaymentPage.setActiveTabColor(DynamicCssConstants.ACTIVE_TAB_COLOR.getValue());
		dynamicPaymentPage.setActiveTabTextColor(DynamicCssConstants.ACTIVE_TAB_TEXT_COLOR.getValue());
		dynamicPaymentPage.setButtonBackgoundColor(DynamicCssConstants.BUTTON_BACKGROUND_COLOR.getValue());
		dynamicPaymentPage.setButtonTextColor(DynamicCssConstants.BUTTON_TEXT_COLOR.getValue());
		dynamicPaymentPage.setBorderColor(DynamicCssConstants.BORDER_COLOR.getValue());
		
		dynamicPaymentPageDao.create(dynamicPaymentPage);
		return dynamicPaymentPage;
		
	}
}
