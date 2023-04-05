package com.paymentgateway.commons.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DynamicPaymentPage implements Serializable {
	
	private static final long serialVersionUID = 3366581935223005142L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Column(unique=true)
	private String payId;
	private String userLogo;
	private String pageTittle;
	private String backgroundImage;
	private String backgroundColor;
	private String textStyle;
	private String textColor;
	private String hyperlinkColor;
	private String boxBackgroundColor;
	private String topBarColor;
	private String tabBackgroundColor;
	private String tabTextColor;
	private String activeTabColor;
	private String activeTabTextColor;
	private String buttonBackgoundColor;
	private String buttonTextColor;	
	private String borderColor;
	
	public String string() {
		return pageTittle;
	}
	public void setPageTittle(String pageTittle) {
		this.pageTittle = pageTittle;
	}
	public String getBackgroundImage() {
		return backgroundImage;
	}
	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}
	public String getTextStyle() {
		return textStyle;
	}
	public void setTextStyle(String textStyle) {
		this.textStyle = textStyle;
	}
	public String getTextColor() {
		return textColor;
	}
	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}
	public String getHyperlinkColor() {
		return hyperlinkColor;
	}
	public void setHyperlinkColor(String hyperlinkColor) {
		this.hyperlinkColor = hyperlinkColor;
	}

	public String getBoxBackgroundColor() {
		return boxBackgroundColor;
	}
	public void setBoxBackgroundColor(String boxBackgroundColor) {
		this.boxBackgroundColor = boxBackgroundColor;
	}
	public String getTabBackgroundColor() {
		return tabBackgroundColor;
	}
	public void setTabBackgroundColor(String tabBackgroundColor) {
		this.tabBackgroundColor = tabBackgroundColor;
	}

	public String getActiveTabColor() {
		return activeTabColor;
	}
	public void setActiveTabColor(String activeTabColor) {
		this.activeTabColor = activeTabColor;
	}
	public String getTabTextColor() {
		return tabTextColor;
	}
	public void setTabTextColor(String tabTextColor) {
		this.tabTextColor = tabTextColor;
	}
	public String getActiveTabTextColor() {
		return activeTabTextColor;
	}
	public void setActiveTabTextColor(String activeTabTextColor) {
		this.activeTabTextColor = activeTabTextColor;
	}
	public String getButtonBackgoundColor() {
		return buttonBackgoundColor;
	}
	public void setButtonBackgoundColor(String buttonBackgoundColor) {
		this.buttonBackgoundColor = buttonBackgoundColor;
	}
	
	public String getBorderColor() {
		return borderColor;
	}
	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}
	public String getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public String getButtonTextColor() {
		return buttonTextColor;
	}
	public void setButtonTextColor(String buttonTextColor) {
		this.buttonTextColor = buttonTextColor;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getTopBarColor() {
		return topBarColor;
	}
	public void setTopBarColor(String topBarColor) {
		this.topBarColor = topBarColor;
	}
	public String getPageTittle() {
		return pageTittle;
	}
	public String getUserLogo() {
		return userLogo;
	}
	public void setUserLogo(String userLogo) {
		this.userLogo = userLogo;
	}
}
