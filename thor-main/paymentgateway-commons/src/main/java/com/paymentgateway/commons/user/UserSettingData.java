package com.paymentgateway.commons.user;

import java.io.File;

public class UserSettingData {
	
	private String payId;
	private String businessName;
	private String superMerchantId;
	private String superMerchantName;
	
	private String defaultCurrency;
	private String paymentMessageSlab;
	private String attemptTrasacation;
	private String wlSaveParam;
	private String cardSaveParam;
	private String merchantLogo;
	private String callBackUrl;
	private String logoName;
	private String deviation;
	private String codName;
	private String partAnnualTurnover;
	private String whiteListReturnUrl;
	private String customHostedUrl;
	private String nbSaveParam;
	private String vpaSaveParam;
	
	private boolean expressPayFlag;
	private boolean merchantHostedFlag;
	private boolean iframePaymentFlag;
	private boolean checkOutJsFlag;
	private boolean surchargeFlag;
	private boolean discountingFlag;
	private boolean loadWalletFlag;
	private boolean eposMerchant;
	private boolean bookingRecord;
	private boolean eNachReportFlag;
	private boolean upiAutoPayReportFlag;
	private boolean acceptPostSettledInEnquiry;
	private boolean customTransactionStatus;
	private boolean capturedMerchantFlag;
	private boolean paymentAdviceFlag;
	private boolean retailMerchantFlag;
	private boolean lyraPay;
	private boolean non3dsTxn;
	private boolean retryTransactionCustomeFlag;
	private boolean autoRefund;
	private boolean whiteListReturnUrlFlag;
	private boolean saveVPAFlag;
	private boolean saveWLFlag;
	private boolean expressPay;
	private boolean saveNBFlag;
	private boolean allCallBackFlag;
	private boolean netSettledFlag;
	
	private boolean merchantInitiatedDirectFlag;
	private boolean nodalReportFlag;
	private boolean virtualAccountFlag;
	private boolean topupFlag;
	private boolean statementFlag;
	private boolean allowNodalPayoutFlag;
	private boolean allowPayoutUpdateStatus;
	private boolean allowECollectionFee;
	private boolean accountVerificationFlag;
	private boolean vpaVerificationFlag;

	private boolean allowCustomHostedUrl;
	
	private boolean skipOrderIdForRefund;
	private boolean allowSaleDuplicate;
	private boolean allowRefundDuplicate;
	private boolean allowSaleInRefund;
	private boolean allowRefundInSale;
	private boolean allowDuplicateNot;
	
	private boolean smtMerchant;
	private boolean logoFlag;

	private boolean allowLogoInPgPage;
	
	private boolean allowPartSettle;
	
	private boolean allowSubtractValue;
//	private boolean sameLimitFlag;
	private String mCC;

	private boolean allowQRScanFlag;
	private boolean allowUpiQRFlag;
	private boolean customerQrFlag;
	private boolean callBackFlag;
	
	private File logoImageFile;
	
	private String payoutCallbackUrl;
	private boolean allowPayoutStatusEnquiryCallbackFlag;
	private boolean upiHostedFlag;
	private boolean configurableFlag;
	private String configurableTime;
	
	private boolean allowInvoiceSms;
	private boolean allowInvoiceEmail;
	
	private String isgPayMerchantID;
	
	private boolean enabledVa;
	private boolean vaValidation;
	private String vaCallbackUrl;
	private boolean vaCallBackSuccess;
	private boolean vaCallBackFail;
	private boolean randomAmount;


	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
	
	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public String getPaymentMessageSlab() {
		return paymentMessageSlab;
	}

	public void setPaymentMessageSlab(String paymentMessageSlab) {
		this.paymentMessageSlab = paymentMessageSlab;
	}

	public String getAttemptTrasacation() {
		return attemptTrasacation;
	}

	public void setAttemptTrasacation(String attemptTrasacation) {
		this.attemptTrasacation = attemptTrasacation;
	}

	public String getWlSaveParam() {
		return wlSaveParam;
	}

	public void setWlSaveParam(String wlSaveParam) {
		this.wlSaveParam = wlSaveParam;
	}

	public String getCardSaveParam() {
		return cardSaveParam;
	}

	public void setCardSaveParam(String cardSaveParam) {
		this.cardSaveParam = cardSaveParam;
	}

	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public String getLogoName() {
		return logoName;
	}

	public void setLogoName(String logoName) {
		this.logoName = logoName;
	}

	public String getDeviation() {
		return deviation;
	}

	public void setDeviation(String deviation) {
		this.deviation = deviation;
	}

	public boolean isMerchantHostedFlag() {
		return merchantHostedFlag;
	}

	public void setMerchantHostedFlag(boolean merchantHostedFlag) {
		this.merchantHostedFlag = merchantHostedFlag;
	}

	public boolean isIframePaymentFlag() {
		return iframePaymentFlag;
	}

	public void setIframePaymentFlag(boolean iframePaymentFlag) {
		this.iframePaymentFlag = iframePaymentFlag;
	}

	public boolean isCheckOutJsFlag() {
		return checkOutJsFlag;
	}

	public void setCheckOutJsFlag(boolean checkOutJsFlag) {
		this.checkOutJsFlag = checkOutJsFlag;
	}

	public boolean isSurchargeFlag() {
		return surchargeFlag;
	}

	public void setSurchargeFlag(boolean surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}

	public boolean isDiscountingFlag() {
		return discountingFlag;
	}

	public void setDiscountingFlag(boolean discountingFlag) {
		this.discountingFlag = discountingFlag;
	}

	public boolean isLoadWalletFlag() {
		return loadWalletFlag;
	}

	public void setLoadWalletFlag(boolean loadWalletFlag) {
		this.loadWalletFlag = loadWalletFlag;
	}

	public boolean isEposMerchant() {
		return eposMerchant;
	}

	public void setEposMerchant(boolean eposMerchant) {
		this.eposMerchant = eposMerchant;
	}

	public boolean isBookingRecord() {
		return bookingRecord;
	}

	public void setBookingRecord(boolean bookingRecord) {
		this.bookingRecord = bookingRecord;
	}

	public boolean iseNachReportFlag() {
		return eNachReportFlag;
	}

	public void seteNachReportFlag(boolean eNachReportFlag) {
		this.eNachReportFlag = eNachReportFlag;
	}

	public boolean isUpiAutoPayReportFlag() {
		return upiAutoPayReportFlag;
	}

	public void setUpiAutoPayReportFlag(boolean upiAutoPayReportFlag) {
		this.upiAutoPayReportFlag = upiAutoPayReportFlag;
	}

	public boolean isAcceptPostSettledInEnquiry() {
		return acceptPostSettledInEnquiry;
	}

	public void setAcceptPostSettledInEnquiry(boolean acceptPostSettledInEnquiry) {
		this.acceptPostSettledInEnquiry = acceptPostSettledInEnquiry;
	}

	public boolean isCustomTransactionStatus() {
		return customTransactionStatus;
	}

	public void setCustomTransactionStatus(boolean customTransactionStatus) {
		this.customTransactionStatus = customTransactionStatus;
	}

	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}

	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}

	public boolean isPaymentAdviceFlag() {
		return paymentAdviceFlag;
	}

	public void setPaymentAdviceFlag(boolean paymentAdviceFlag) {
		this.paymentAdviceFlag = paymentAdviceFlag;
	}

	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}

	public boolean isLyraPay() {
		return lyraPay;
	}

	public void setLyraPay(boolean lyraPay) {
		this.lyraPay = lyraPay;
	}

	public boolean isNon3dsTxn() {
		return non3dsTxn;
	}

	public void setNon3dsTxn(boolean non3dsTxn) {
		this.non3dsTxn = non3dsTxn;
	}

	public boolean isRetryTransactionCustomeFlag() {
		return retryTransactionCustomeFlag;
	}

	public void setRetryTransactionCustomeFlag(boolean retryTransactionCustomeFlag) {
		this.retryTransactionCustomeFlag = retryTransactionCustomeFlag;
	}

	public boolean isAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(boolean autoRefund) {
		this.autoRefund = autoRefund;
	}

	public boolean isWhiteListReturnUrlFlag() {
		return whiteListReturnUrlFlag;
	}

	public void setWhiteListReturnUrlFlag(boolean whiteListReturnUrlFlag) {
		this.whiteListReturnUrlFlag = whiteListReturnUrlFlag;
	}

	public boolean isSaveVPAFlag() {
		return saveVPAFlag;
	}

	public void setSaveVPAFlag(boolean saveVPAFlag) {
		this.saveVPAFlag = saveVPAFlag;
	}

	public boolean isSaveWLFlag() {
		return saveWLFlag;
	}

	public void setSaveWLFlag(boolean saveWLFlag) {
		this.saveWLFlag = saveWLFlag;
	}

	public boolean isExpressPay() {
		return expressPay;
	}

	public void setExpressPay(boolean expressPay) {
		this.expressPay = expressPay;
	}

	public boolean isMerchantInitiatedDirectFlag() {
		return merchantInitiatedDirectFlag;
	}

	public void setMerchantInitiatedDirectFlag(boolean merchantInitiatedDirectFlag) {
		this.merchantInitiatedDirectFlag = merchantInitiatedDirectFlag;
	}

	public boolean isNodalReportFlag() {
		return nodalReportFlag;
	}

	public void setNodalReportFlag(boolean nodalReportFlag) {
		this.nodalReportFlag = nodalReportFlag;
	}

	public boolean isVirtualAccountFlag() {
		return virtualAccountFlag;
	}

	public void setVirtualAccountFlag(boolean virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}

	public boolean isTopupFlag() {
		return topupFlag;
	}

	public void setTopupFlag(boolean topupFlag) {
		this.topupFlag = topupFlag;
	}

	public boolean isStatementFlag() {
		return statementFlag;
	}

	public void setStatementFlag(boolean statementFlag) {
		this.statementFlag = statementFlag;
	}

	public boolean isAllowNodalPayoutFlag() {
		return allowNodalPayoutFlag;
	}

	public void setAllowNodalPayoutFlag(boolean allowNodalPayoutFlag) {
		this.allowNodalPayoutFlag = allowNodalPayoutFlag;
	}

	public boolean isAllowPayoutUpdateStatus() {
		return allowPayoutUpdateStatus;
	}

	public void setAllowPayoutUpdateStatus(boolean allowPayoutUpdateStatus) {
		this.allowPayoutUpdateStatus = allowPayoutUpdateStatus;
	}

	public boolean isAllowECollectionFee() {
		return allowECollectionFee;
	}

	public void setAllowECollectionFee(boolean allowECollectionFee) {
		this.allowECollectionFee = allowECollectionFee;
	}

	public boolean isAccountVerificationFlag() {
		return accountVerificationFlag;
	}

	public void setAccountVerificationFlag(boolean accountVerificationFlag) {
		this.accountVerificationFlag = accountVerificationFlag;
	}

	public boolean isVpaVerificationFlag() {
		return vpaVerificationFlag;
	}

	public void setVpaVerificationFlag(boolean vpaVerificationFlag) {
		this.vpaVerificationFlag = vpaVerificationFlag;
	}

	public boolean isAllowCustomHostedUrl() {
		return allowCustomHostedUrl;
	}

	public void setAllowCustomHostedUrl(boolean allowCustomHostedUrl) {
		this.allowCustomHostedUrl = allowCustomHostedUrl;
	}

	public boolean isSkipOrderIdForRefund() {
		return skipOrderIdForRefund;
	}

	public void setSkipOrderIdForRefund(boolean skipOrderIdForRefund) {
		this.skipOrderIdForRefund = skipOrderIdForRefund;
	}

	public boolean isAllowSaleDuplicate() {
		return allowSaleDuplicate;
	}

	public void setAllowSaleDuplicate(boolean allowSaleDuplicate) {
		this.allowSaleDuplicate = allowSaleDuplicate;
	}

	public boolean isAllowRefundDuplicate() {
		return allowRefundDuplicate;
	}

	public void setAllowRefundDuplicate(boolean allowRefundDuplicate) {
		this.allowRefundDuplicate = allowRefundDuplicate;
	}

	public boolean isAllowSaleInRefund() {
		return allowSaleInRefund;
	}

	public void setAllowSaleInRefund(boolean allowSaleInRefund) {
		this.allowSaleInRefund = allowSaleInRefund;
	}

	public boolean isAllowRefundInSale() {
		return allowRefundInSale;
	}

	public void setAllowRefundInSale(boolean allowRefundInSale) {
		this.allowRefundInSale = allowRefundInSale;
	}

	public boolean isAllowDuplicateNot() {
		return allowDuplicateNot;
	}

	public void setAllowDuplicateNot(boolean allowDuplicateNot) {
		this.allowDuplicateNot = allowDuplicateNot;
	}

	public boolean isSmtMerchant() {
		return smtMerchant;
	}

	public void setSmtMerchant(boolean smtMerchant) {
		this.smtMerchant = smtMerchant;
	}

	public boolean isLogoFlag() {
		return logoFlag;
	}

	public void setLogoFlag(boolean logoFlag) {
		this.logoFlag = logoFlag;
	}

	public boolean isAllowLogoInPgPage() {
		return allowLogoInPgPage;
	}

	public void setAllowLogoInPgPage(boolean allowLogoInPgPage) {
		this.allowLogoInPgPage = allowLogoInPgPage;
	}

	public String getCodName() {
		return codName;
	}

	public void setCodName(String codName) {
		this.codName = codName;
	}

	public boolean isAllowPartSettle() {
		return allowPartSettle;
	}

	public void setAllowPartSettle(boolean allowPartSettle) {
		this.allowPartSettle = allowPartSettle;
	}

	public String getPartAnnualTurnover() {
		return partAnnualTurnover;
	}

	public void setPartAnnualTurnover(String partAnnualTurnover) {
		this.partAnnualTurnover = partAnnualTurnover;
	}

	public boolean isAllowSubtractValue() {
		return allowSubtractValue;
	}

	public void setAllowSubtractValue(boolean allowSubtractValue) {
		this.allowSubtractValue = allowSubtractValue;
	}

	public boolean isAllowQRScanFlag() {
		return allowQRScanFlag;
	}

	public void setAllowQRScanFlag(boolean allowQRScanFlag) {
		this.allowQRScanFlag = allowQRScanFlag;
	}

	public boolean isAllowUpiQRFlag() {
		return allowUpiQRFlag;
	}

	public void setAllowUpiQRFlag(boolean allowUpiQRFlag) {
		this.allowUpiQRFlag = allowUpiQRFlag;
	}

	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}

	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
	}

	public boolean isCallBackFlag() {
		return callBackFlag;
	}

	public void setCallBackFlag(boolean callBackFlag) {
		this.callBackFlag = callBackFlag;
	}

	public File getLogoImageFile() {
		return logoImageFile;
	}

	public void setLogoImageFile(File logoImageFile) {
		this.logoImageFile = logoImageFile;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getWhiteListReturnUrl() {
		return whiteListReturnUrl;
	}

	public void setWhiteListReturnUrl(String whiteListReturnUrl) {
		this.whiteListReturnUrl = whiteListReturnUrl;
	}

	public boolean isSaveNBFlag() {
		return saveNBFlag;
	}

	public void setSaveNBFlag(boolean saveNBFlag) {
		this.saveNBFlag = saveNBFlag;
	}

	public String getCustomHostedUrl() {
		return customHostedUrl;
	}

	public void setCustomHostedUrl(String customHostedUrl) {
		this.customHostedUrl = customHostedUrl;
	}

	public boolean isAllCallBackFlag() {
		return allCallBackFlag;
	}

	public void setAllCallBackFlag(boolean allCallBackFlag) {
		this.allCallBackFlag = allCallBackFlag;
	}

	public String getNbSaveParam() {
		return nbSaveParam;
	}

	public void setNbSaveParam(String nbSaveParam) {
		this.nbSaveParam = nbSaveParam;
	}

	public String getVpaSaveParam() {
		return vpaSaveParam;
	}

	public void setVpaSaveParam(String vpaSaveParam) {
		this.vpaSaveParam = vpaSaveParam;
	}

	public boolean isExpressPayFlag() {
		return expressPayFlag;
	}

	public void setExpressPayFlag(boolean expressPayFlag) {
		this.expressPayFlag = expressPayFlag;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}

	public String getSuperMerchantName() {
		return superMerchantName;
	}

	public void setSuperMerchantName(String superMerchantName) {
		this.superMerchantName = superMerchantName;
	}

	public boolean isNetSettledFlag() {
		return netSettledFlag;
	}

	public void setNetSettledFlag(boolean netSettledFlag) {
		this.netSettledFlag = netSettledFlag;
	}

	public String getmCC() {
		return mCC;
	}

	public void setmCC(String mCC) {
		this.mCC = mCC;
	}

	public String getPayoutCallbackUrl() {
		return payoutCallbackUrl;
	}

	public void setPayoutCallbackUrl(String payoutCallbackUrl) {
		this.payoutCallbackUrl = payoutCallbackUrl;
	}

	public boolean isAllowPayoutStatusEnquiryCallbackFlag() {
		return allowPayoutStatusEnquiryCallbackFlag;
	}

	public void setAllowPayoutStatusEnquiryCallbackFlag(boolean allowPayoutStatusEnquiryCallbackFlag) {
		this.allowPayoutStatusEnquiryCallbackFlag = allowPayoutStatusEnquiryCallbackFlag;
	}

	public boolean isUpiHostedFlag() {
		return upiHostedFlag;
	}

	public void setUpiHostedFlag(boolean upiHostedFlag) {
		this.upiHostedFlag = upiHostedFlag;
	}

	public String getConfigurableTime() {
		return configurableTime;
	}

	public void setConfigurableTime(String configurableTime) {
		this.configurableTime = configurableTime;
	}

	public boolean isConfigurableFlag() {
		return configurableFlag;
	}

	public void setConfigurableFlag(boolean configurableFlag) {
		this.configurableFlag = configurableFlag;
	}

	public boolean isAllowInvoiceSms() {
		return allowInvoiceSms;
	}

	public void setAllowInvoiceSms(boolean allowInvoiceSms) {
		this.allowInvoiceSms = allowInvoiceSms;
	}

	public boolean isAllowInvoiceEmail() {
		return allowInvoiceEmail;
	}

	public void setAllowInvoiceEmail(boolean allowInvoiceEmail) {
		this.allowInvoiceEmail = allowInvoiceEmail;
	}

	public String getIsgPayMerchantID() {
		return isgPayMerchantID;
	}

	public void setIsgPayMerchantID(String isgPayMerchantID) {
		this.isgPayMerchantID = isgPayMerchantID;
	}

	public boolean isEnabledVa() {
		return enabledVa;
	}

	public void setEnabledVa(boolean enabledVa) {
		this.enabledVa = enabledVa;
	}

	public boolean isVaValidation() {
		return vaValidation;
	}

	public void setVaValidation(boolean vaValidation) {
		this.vaValidation = vaValidation;
	}

	public String getVaCallbackUrl() {
		return vaCallbackUrl;
	}

	public void setVaCallbackUrl(String vaCallbackUrl) {
		this.vaCallbackUrl = vaCallbackUrl;
	}

	public boolean isVaCallBackSuccess() {
		return vaCallBackSuccess;
	}

	public void setVaCallBackSuccess(boolean vaCallBackSuccess) {
		this.vaCallBackSuccess = vaCallBackSuccess;
	}

	public boolean isVaCallBackFail() {
		return vaCallBackFail;
	}

	public void setVaCallBackFail(boolean vaCallBackFail) {
		this.vaCallBackFail = vaCallBackFail;
	}

	public boolean isRandomAmount() {
		return randomAmount;
	}

	public void setRandomAmount(boolean randomAmount) {
		this.randomAmount = randomAmount;
	}

}
