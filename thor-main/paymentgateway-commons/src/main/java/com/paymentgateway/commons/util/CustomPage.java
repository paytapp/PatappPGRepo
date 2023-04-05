package com.paymentgateway.commons.util;

import java.io.File;

import com.paymentgateway.commons.user.UserType;

public class CustomPage {

	private String payId;	
	private String createDate;
	private String createdBy;
	private UserType userType;
	
	private String pageTitle;
	private File merchantLogoImage;
	private File backGroundImage;
	private File merchantBannerImage;
	private File merchantTnCFile;
	private File merchantPrivacyPolicyFile;
	private String merchantSlogan;
	private String paymentGatewayLogoFlag;
	
	private String formDetailsName;
	private String formDetailsPhone;
	private String formDetailsEmail;
	private String formDetailsAmount;
	private String formDetailsNameLebel;
	private String formDetailsPhoneLebel;
	private String formDetailsEmailLebel;
	private String formDetailsAmountLebel;
	private String formDetailsNewFields;
	private String formDetailsPayButton;
	private String formBackgroundColor;
	private String formInputFields;
	
	private String contactPhoneLebel;
	private String contactEmailLebel;
	private String contactAddressLebel;
	private String contactPhone;
	private String contactEmail;
	private String contactAddress;
	private String contactWebsiteLebel;
	private String contactWebsite;
	
	private String aboutContent;
	
	private String footerTnCLink;
	private String footerContact;
	
	private boolean allowInvoiceFlag;
	
	private String headingFontSize;
	private String paragraphFontSize;
	private String parahFontFamily;
	private String headingFontFamily;
	private String headingFontFamily_link;
	private String parahFontFamily_link;
	
	private String headerBackgroundColor;
	private String footerBackgroundColor;
	private String headingColor;
	private String headingBackgroundColor;
	private String paragraphColor;
	private String buttonColor;
	
	private String response;
	private String tnCFilesLocation;
	private String htmlString;
	private String fileName;
	
	
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getHtmlString() {
		return htmlString;
	}
	public void setHtmlString(String htmlString) {
		this.htmlString = htmlString;
	}
	public String getTnCFilesLocation() {
		return tnCFilesLocation;
	}
	public void setTnCFilesLocation(String tnCFilesLocation) {
		this.tnCFilesLocation = tnCFilesLocation;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public UserType getUserType() {
		return userType;
	}
	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public File getMerchantLogoImage() {
		return merchantLogoImage;
	}
	public void setMerchantLogoImage(File merchantLogoImage) {
		this.merchantLogoImage = merchantLogoImage;
	}
	public String getMerchantSlogan() {
		return merchantSlogan;
	}
	public void setMerchantSlogan(String merchantSlogan) {
		this.merchantSlogan = merchantSlogan;
	}	
	
	public File getBackGroundImage() {
		return backGroundImage;
	}
	public void setBackGroundImage(File backGroundImage) {
		this.backGroundImage = backGroundImage;
	}
	public File getMerchantBannerImage() {
		return merchantBannerImage;
	}
	public void setMerchantBannerImage(File merchantBannerImage) {
		this.merchantBannerImage = merchantBannerImage;
	}
	
	public String getPaymentGatewayLogoFlag() {
		return paymentGatewayLogoFlag;
	}
	public void setPaymentGatewayLogoFlag(String paymentGatewayLogoFlag) {
		this.paymentGatewayLogoFlag = paymentGatewayLogoFlag;
	}
	public boolean isAllowInvoiceFlag() {
		return allowInvoiceFlag;
	}
	public void setAllowInvoiceFlag(boolean allowInvoiceFlag) {
		this.allowInvoiceFlag = allowInvoiceFlag;
	}
	public String getFormDetailsName() {
		return formDetailsName;
	}
	public void setFormDetailsName(String formDetailsName) {
		this.formDetailsName = formDetailsName;
	}
	
	public String getFormDetailsEmail() {
		return formDetailsEmail;
	}
	public void setFormDetailsEmail(String formDetailsEmail) {
		this.formDetailsEmail = formDetailsEmail;
	}
	public String getFormDetailsAmount() {
		return formDetailsAmount;
	}
	public void setFormDetailsAmount(String formDetailsAmount) {
		this.formDetailsAmount = formDetailsAmount;
	}
	public String getFormDetailsPhone() {
		return formDetailsPhone;
	}
	public void setFormDetailsPhone(String formDetailsPhone) {
		this.formDetailsPhone = formDetailsPhone;
	}
	public String getFormDetailsNewFields() {
		return formDetailsNewFields;
	}
	public void setFormDetailsNewFields(String formDetailsNewFields) {
		this.formDetailsNewFields = formDetailsNewFields;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	public String getContactAddress() {
		return contactAddress;
	}
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}
	public String getAboutContent() {
		return aboutContent;
	}
	public void setAboutContent(String aboutContent) {
		this.aboutContent = aboutContent;
	}
	public String getFooterTnCLink() {
		return footerTnCLink;
	}
	public void setFooterTnCLink(String footerTnCLink) {
		this.footerTnCLink = footerTnCLink;
	}
	public String getFormDetailsPayButton() {
		return formDetailsPayButton;
	}
	public void setFormDetailsPayButton(String formDetailsPayButton) {
		this.formDetailsPayButton = formDetailsPayButton;
	}
	public String getFormDetailsNameLebel() {
		return formDetailsNameLebel;
	}
	public void setFormDetailsNameLebel(String formDetailsNameLebel) {
		this.formDetailsNameLebel = formDetailsNameLebel;
	}
	public String getFormDetailsPhoneLebel() {
		return formDetailsPhoneLebel;
	}
	public void setFormDetailsPhoneLebel(String formDetailsPhoneLebel) {
		this.formDetailsPhoneLebel = formDetailsPhoneLebel;
	}
	public String getFormDetailsEmailLebel() {
		return formDetailsEmailLebel;
	}
	public void setFormDetailsEmailLebel(String formDetailsEmailLebel) {
		this.formDetailsEmailLebel = formDetailsEmailLebel;
	}
	public String getFormDetailsAmountLebel() {
		return formDetailsAmountLebel;
	}
	public void setFormDetailsAmountLebel(String formDetailsAmountLebel) {
		this.formDetailsAmountLebel = formDetailsAmountLebel;
	}
	public String getContactPhoneLebel() {
		return contactPhoneLebel;
	}
	public void setContactPhoneLebel(String contactPhoneLebel) {
		this.contactPhoneLebel = contactPhoneLebel;
	}
	public String getContactEmailLebel() {
		return contactEmailLebel;
	}
	public void setContactEmailLebel(String contactEmailLebel) {
		this.contactEmailLebel = contactEmailLebel;
	}
	public String getContactAddressLebel() {
		return contactAddressLebel;
	}
	public void setContactAddressLebel(String contactAddressLebel) {
		this.contactAddressLebel = contactAddressLebel;
	}
	public String getContactWebsiteLebel() {
		return contactWebsiteLebel;
	}
	public void setContactWebsiteLebel(String contactWebsiteLebel) {
		this.contactWebsiteLebel = contactWebsiteLebel;
	}
	public String getContactWebsite() {
		return contactWebsite;
	}
	public void setContactWebsite(String contactWebsite) {
		this.contactWebsite = contactWebsite;
	}
	public String getFooterContact() {
		return footerContact;
	}
	public void setFooterContact(String footerContact) {
		this.footerContact = footerContact;
	}
	
	public String getParagraphFontSize() {
		return paragraphFontSize;
	}
	public void setParagraphFontSize(String paragraphFontSize) {
		this.paragraphFontSize = paragraphFontSize;
	}
	public String getHeaderBackgroundColor() {
		return headerBackgroundColor;
	}
	public void setHeaderBackgroundColor(String headerBackgroundColor) {
		this.headerBackgroundColor = headerBackgroundColor;
	}
	public String getFooterBackgroundColor() {
		return footerBackgroundColor;
	}
	public void setFooterBackgroundColor(String footerBackgroundColor) {
		this.footerBackgroundColor = footerBackgroundColor;
	}
	public String getHeadingColor() {
		return headingColor;
	}
	public void setHeadingColor(String headingColor) {
		this.headingColor = headingColor;
	}
	public String getParagraphColor() {
		return paragraphColor;
	}
	public void setParagraphColor(String paragraphColor) {
		this.paragraphColor = paragraphColor;
	}
	public String getButtonColor() {
		return buttonColor;
	}
	public void setButtonColor(String buttonColor) {
		this.buttonColor = buttonColor;
	}
	public String getFormBackgroundColor() {
		return formBackgroundColor;
	}
	public void setFormBackgroundColor(String formBackgroundColor) {
		this.formBackgroundColor = formBackgroundColor;
	}
	public String getHeadingFontSize() {
		return headingFontSize;
	}
	public void setHeadingFontSize(String headingFontSize) {
		this.headingFontSize = headingFontSize;
	}
	public String getHeadingBackgroundColor() {
		return headingBackgroundColor;
	}
	public void setHeadingBackgroundColor(String headingBackgroundColor) {
		this.headingBackgroundColor = headingBackgroundColor;
	}
	public File getMerchantTnCFile() {
		return merchantTnCFile;
	}
	public void setMerchantTnCFile(File merchantTnCFile) {
		this.merchantTnCFile = merchantTnCFile;
	}
	public File getMerchantPrivacyPolicyFile() {
		return merchantPrivacyPolicyFile;
	}
	public void setMerchantPrivacyPolicyFile(File merchantPrivacyPolicyFile) {
		this.merchantPrivacyPolicyFile = merchantPrivacyPolicyFile;
	}
	public String getFormInputFields() {
		return formInputFields;
	}
	public void setFormInputFields(String formInputFields) {
		this.formInputFields = formInputFields;
	}
	public String getParahFontFamily() {
		return parahFontFamily;
	}
	public void setParahFontFamily(String parahFontFamily) {
		this.parahFontFamily = parahFontFamily;
	}
	public String getHeadingFontFamily() {
		return headingFontFamily;
	}
	public void setHeadingFontFamily(String headingFontFamily) {
		this.headingFontFamily = headingFontFamily;
	}
	public String getHeadingFontFamily_link() {
		return headingFontFamily_link;
	}
	public void setHeadingFontFamily_link(String headingFontFamily_link) {
		this.headingFontFamily_link = headingFontFamily_link;
	}
	public String getParahFontFamily_link() {
		return parahFontFamily_link;
	}
	public void setParahFontFamily_link(String parahFontFamily_link) {
		this.parahFontFamily_link = parahFontFamily_link;
	}
	
	
}
