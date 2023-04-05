package com.paymentgateway.scheduler.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("paymentgateway.demo")
public class ConfigurationProvider {

	// Master job
	private String masterJobStatus;
	private String masterJobCron;

	// PendingBookingUpdates
	private String pendingBookingJobStatus;
	private String pendingBookingJobCron;

	// Mongo
	private String MONGO_DB_dbName;
	private String MONGO_DB_collectionName;
	private String MONGO_DB_host;
	private String MONGO_DB_port;
	private String MONGO_DB_mongoURIprefix;
	private String MONGO_DB_mongoURIsuffix;
	private String MONGO_DB_username;
	private String MONGO_DB_password;
	private String MONGO_DB_reportingCollectionName;
	private String MONGO_DB_settlementCollectionName;
	private String MONGO_DB_statusEnquiryCollectionName;
	private String MONGO_DB_summaryTransactionsCollectionName;
	private String MONGO_DB_schedulerLogsCollection;
	private String MONGO_DB_E_Collection;
	private String MONGO_DB_ImpsSettlementCollection;
	private String MONGO_DB_closingAmountCollection;
	private String MONGO_DB_impsUpiBulkCollection;
	private String MONGO_DB_eNachCollectionName;
	private String MONGO_DB_upiAutoPayCollectionName;
	private String MONGO_DB_transactionStatus;
	
	private String transactionStatusEnquiry;
	private String acquirerName;
	private String enquiryApiUrl;
	private String pendingTransactionStatusUpdateCron;
	private String merchantPayId;
	
	private String hoursBefore;
	private String hoursInterval;
	private String minutesBefore;
	private String minutesInterval;
	private String transactionBankStatusEnquiryCron;
	
	private String orderConfirmation;
	private String orderMinutesBefore;
	private String orderMinutesInterval;
	private String orderConfirmationCron;
	private String orderConfirmApiUrl;
	private String orderMerchantPayId;
	// URL
	private String smsApiUrl;
	private String statusEnquiryApiUrl;
	private String refundApiUrl;
	private String orderConfirmApi;
	
	//eCollection
	private String eCollectionData;
	private String eColletionCron;
	
	//Bulk IMPS UPI
	private String bulkImpsUpiData;
	private String impsUrl;
	private String upiUrl;
	private String impsUpiCron;
	
	//Merchant Initiated Direct
	private String merchantInititatedDirectUrl;
	private String merchantInititatedDirectStatusEnqUrl;
	
	// Closing Amount Params
	private String closingAmount;
	private String closingAmountCron;
	private String CLOSING_AMOUNT_COLLECTION;
	
	//payout Status enquiry
	private String payoutMinutesBefore;
	private String payoutMinutesInterval;
	private String payoutStatusEnquiry;
	private String payoutStatusEnquiryCron;
	private String impsStatusEnquiryUrl;
	private String upiStatusEnquiryUrl;
	
	//eNach
	private String transactionSchedule;
	private String transactionScheduleCron;
	private String iciciEnachTransactionScheduleUrl;
	
	private String statusEnquiryTransactionSchedule;
	private String statusEnquiryTransactionScheduleCron;
	private String iciciEnachTransactionStatusEnquiryUrl;
	
	//Upi AutoPay
	private String transactionNotificationSchedule;
	private String transactionNotificationScheduleCron;
	private String transactionNotificationScheduleUrl;
	
	private String upiAutoPayDebitTransactionSchedule;
	private String upiAutoPayDebitTransactionScheduleCron;
	private String upiAutoPayDebitTransactionScheduleUrl;
	
	private String upiAutoPayStatusEnquiryCriteriaSchedule;
	private String upiAutoPayStatusEnquiryCriteriaScheduleCron; 
	private String upiAutoPayStatusEnquiryCriteriaScheduleUrl;
	
	
	private String retryCallback;
	private String retryCallbackCron;
	private String retryCallbackApiUrl;
	private String MONGO_DB_callbackRetryCollection;
	
	private String autoRefund;
	private String autoRefundCron;
	private String autoRefundUrl;
	
	private String autoDeleteHistoricDataFolders;
	private String autoDeleteHistoricDataFoldersCron;
	
	private String healthCheckUp;
	private String healthCheckUpCron;
	
	private String cryptoStatusCheck;
	private String cryptoStatusCheckCron;
	private String  CRYPTO_STATUS;
	private String cryptoTransactionStatusEnquiryUrl;
	
	private String msedclRetryCallback;
    private String msedclRetryCallbackCron;
    
 // pending transaction
 	private String pendingMinutesBefore;
 	private String pendingMinutesInterval;
 	private String pendingTransactionsStatusUpdate;
 	private String pendingTransactionsStatusUpdateCron;
	
	private String enquiryApiKey;
	
	public String getCRYPTO_STATUS() {
		return CRYPTO_STATUS;
	}

	public void setCRYPTO_STATUS(String cRYPTO_STATUS) {
		CRYPTO_STATUS = cRYPTO_STATUS;
	}

	public String getUpiAutoPayStatusEnquiryCriteriaSchedule() {
		return upiAutoPayStatusEnquiryCriteriaSchedule;
	}

	public void setUpiAutoPayStatusEnquiryCriteriaSchedule(String upiAutoPayStatusEnquiryCriteriaSchedule) {
		this.upiAutoPayStatusEnquiryCriteriaSchedule = upiAutoPayStatusEnquiryCriteriaSchedule;
	}

	public String getUpiAutoPayStatusEnquiryCriteriaScheduleCron() {
		return upiAutoPayStatusEnquiryCriteriaScheduleCron;
	}

	public void setUpiAutoPayStatusEnquiryCriteriaScheduleCron(String upiAutoPayStatusEnquiryCriteriaScheduleCron) {
		this.upiAutoPayStatusEnquiryCriteriaScheduleCron = upiAutoPayStatusEnquiryCriteriaScheduleCron;
	}

	public String getUpiAutoPayStatusEnquiryCriteriaScheduleUrl() {
		return upiAutoPayStatusEnquiryCriteriaScheduleUrl;
	}

	public void setUpiAutoPayStatusEnquiryCriteriaScheduleUrl(String upiAutoPayStatusEnquiryCriteriaScheduleUrl) {
		this.upiAutoPayStatusEnquiryCriteriaScheduleUrl = upiAutoPayStatusEnquiryCriteriaScheduleUrl;
	}
	
	public String getUpiAutoPayDebitTransactionSchedule() {
		return upiAutoPayDebitTransactionSchedule;
	}

	public void setUpiAutoPayDebitTransactionSchedule(String upiAutoPayDebitTransactionSchedule) {
		this.upiAutoPayDebitTransactionSchedule = upiAutoPayDebitTransactionSchedule;
	}

	public String getUpiAutoPayDebitTransactionScheduleCron() {
		return upiAutoPayDebitTransactionScheduleCron;
	}

	public void setUpiAutoPayDebitTransactionScheduleCron(String upiAutoPayDebitTransactionScheduleCron) {
		this.upiAutoPayDebitTransactionScheduleCron = upiAutoPayDebitTransactionScheduleCron;
	}

	public String getUpiAutoPayDebitTransactionScheduleUrl() {
		return upiAutoPayDebitTransactionScheduleUrl;
	}

	public void setUpiAutoPayDebitTransactionScheduleUrl(String upiAutoPayDebitTransactionScheduleUrl) {
		this.upiAutoPayDebitTransactionScheduleUrl = upiAutoPayDebitTransactionScheduleUrl;
	}
	
	public String getTransactionNotificationSchedule() {
		return transactionNotificationSchedule;
	}

	public void setTransactionNotificationSchedule(String transactionNotificationSchedule) {
		this.transactionNotificationSchedule = transactionNotificationSchedule;
	}

	public String getTransactionNotificationScheduleCron() {
		return transactionNotificationScheduleCron;
	}

	public void setTransactionNotificationScheduleCron(String transactionNotificationScheduleCron) {
		this.transactionNotificationScheduleCron = transactionNotificationScheduleCron;
	}

	public String getTransactionNotificationScheduleUrl() {
		return transactionNotificationScheduleUrl;
	}

	public void setTransactionNotificationScheduleUrl(String transactionNotificationScheduleUrl) {
		this.transactionNotificationScheduleUrl = transactionNotificationScheduleUrl;
	}
	
	public String getIciciEnachTransactionStatusEnquiryUrl() {
		return iciciEnachTransactionStatusEnquiryUrl;
	}

	public void setIciciEnachTransactionStatusEnquiryUrl(String iciciEnachTransactionStatusEnquiryUrl) {
		this.iciciEnachTransactionStatusEnquiryUrl = iciciEnachTransactionStatusEnquiryUrl;
	}

	public String getStatusEnquiryTransactionScheduleCron() {
		return statusEnquiryTransactionScheduleCron;
	}

	public void setStatusEnquiryTransactionScheduleCron(String statusEnquiryTransactionScheduleCron) {
		this.statusEnquiryTransactionScheduleCron = statusEnquiryTransactionScheduleCron;
	}

	public String getStatusEnquiryTransactionSchedule() {
		return statusEnquiryTransactionSchedule;
	}

	public void setStatusEnquiryTransactionSchedule(String statusEnquiryTransactionSchedule) {
		this.statusEnquiryTransactionSchedule = statusEnquiryTransactionSchedule;
	}

	public String getIciciEnachTransactionScheduleUrl() {
		return iciciEnachTransactionScheduleUrl;
	}

	public void setIciciEnachTransactionScheduleUrl(String iciciEnachTransactionScheduleUrl) {
		this.iciciEnachTransactionScheduleUrl = iciciEnachTransactionScheduleUrl;
	}

	public String getMasterJobStatus() {
		return masterJobStatus;
	}

	public void setMasterJobStatus(String masterJobStatus) {
		this.masterJobStatus = masterJobStatus;
	}

	public String getMasterJobCron() {
		return masterJobCron;
	}

	public void setMasterJobCron(String masterJobCron) {
		this.masterJobCron = masterJobCron;
	}

	public String getMONGO_DB_dbName() {
		return MONGO_DB_dbName;
	}

	public void setMONGO_DB_dbName(String mONGO_DB_dbName) {
		MONGO_DB_dbName = mONGO_DB_dbName;
	}

	public String getMONGO_DB_collectionName() {
		return MONGO_DB_collectionName;
	}

	public void setMONGO_DB_collectionName(String mONGO_DB_collectionName) {
		MONGO_DB_collectionName = mONGO_DB_collectionName;
	}

	public String getMONGO_DB_host() {
		return MONGO_DB_host;
	}

	public void setMONGO_DB_host(String mONGO_DB_host) {
		MONGO_DB_host = mONGO_DB_host;
	}

	public String getMONGO_DB_port() {
		return MONGO_DB_port;
	}

	public void setMONGO_DB_port(String mONGO_DB_port) {
		MONGO_DB_port = mONGO_DB_port;
	}

	public String getMONGO_DB_mongoURIprefix() {
		return MONGO_DB_mongoURIprefix;
	}

	public void setMONGO_DB_mongoURIprefix(String mONGO_DB_mongoURIprefix) {
		MONGO_DB_mongoURIprefix = mONGO_DB_mongoURIprefix;
	}

	public String getMONGO_DB_mongoURIsuffix() {
		return MONGO_DB_mongoURIsuffix;
	}

	public void setMONGO_DB_mongoURIsuffix(String mONGO_DB_mongoURIsuffix) {
		MONGO_DB_mongoURIsuffix = mONGO_DB_mongoURIsuffix;
	}

	public String getMONGO_DB_username() {
		return MONGO_DB_username;
	}

	public void setMONGO_DB_username(String mONGO_DB_username) {
		MONGO_DB_username = mONGO_DB_username;
	}

	public String getMONGO_DB_password() {
		return MONGO_DB_password;
	}

	public void setMONGO_DB_password(String mONGO_DB_password) {
		MONGO_DB_password = mONGO_DB_password;
	}

	public String getMONGO_DB_reportingCollectionName() {
		return MONGO_DB_reportingCollectionName;
	}

	public void setMONGO_DB_reportingCollectionName(String mONGO_DB_reportingCollectionName) {
		MONGO_DB_reportingCollectionName = mONGO_DB_reportingCollectionName;
	}

	public String getMONGO_DB_settlementCollectionName() {
		return MONGO_DB_settlementCollectionName;
	}

	public void setMONGO_DB_settlementCollectionName(String mONGO_DB_settlementCollectionName) {
		MONGO_DB_settlementCollectionName = mONGO_DB_settlementCollectionName;
	}

	public String getMONGO_DB_statusEnquiryCollectionName() {
		return MONGO_DB_statusEnquiryCollectionName;
	}

	public void setMONGO_DB_statusEnquiryCollectionName(String mONGO_DB_statusEnquiryCollectionName) {
		MONGO_DB_statusEnquiryCollectionName = mONGO_DB_statusEnquiryCollectionName;
	}

	public String getMONGO_DB_summaryTransactionsCollectionName() {
		return MONGO_DB_summaryTransactionsCollectionName;
	}

	public void setMONGO_DB_summaryTransactionsCollectionName(String mONGO_DB_summaryTransactionsCollectionName) {
		MONGO_DB_summaryTransactionsCollectionName = mONGO_DB_summaryTransactionsCollectionName;
	}

	public String getMONGO_DB_schedulerLogsCollection() {
		return MONGO_DB_schedulerLogsCollection;
	}

	public void setMONGO_DB_schedulerLogsCollection(String mONGO_DB_schedulerLogsCollection) {
		MONGO_DB_schedulerLogsCollection = mONGO_DB_schedulerLogsCollection;
	}

	public String getSmsApiUrl() {
		return smsApiUrl;
	}

	public void setSmsApiUrl(String smsApiUrl) {
		this.smsApiUrl = smsApiUrl;
	}

	public String getStatusEnquiryApiUrl() {
		return statusEnquiryApiUrl;
	}

	public void setStatusEnquiryApiUrl(String statusEnquiryApiUrl) {
		this.statusEnquiryApiUrl = statusEnquiryApiUrl;
	}

	public String getRefundApiUrl() {
		return refundApiUrl;
	}

	public void setRefundApiUrl(String refundApiUrl) {
		this.refundApiUrl = refundApiUrl;
	}

	public String getPendingBookingJobStatus() {
		return pendingBookingJobStatus;
	}

	public void setPendingBookingJobStatus(String pendingBookingJobStatus) {
		this.pendingBookingJobStatus = pendingBookingJobStatus;
	}

	public String getPendingBookingJobCron() {
		return pendingBookingJobCron;
	}

	public void setPendingBookingJobCron(String pendingBookingJobCron) {
		this.pendingBookingJobCron = pendingBookingJobCron;
	}

	public String getTransactionStatusEnquiry() {
		return transactionStatusEnquiry;
	}

	public void setTransactionStatusEnquiry(String transactionStatusEnquiry) {
		this.transactionStatusEnquiry = transactionStatusEnquiry;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}

	public String getEnquiryApiUrl() {
		return enquiryApiUrl;
	}

	public void setEnquiryApiUrl(String enquiryApiUrl) {
		this.enquiryApiUrl = enquiryApiUrl;
	}

	public String getPendingTransactionStatusUpdateCron() {
		return pendingTransactionStatusUpdateCron;
	}

	public void setPendingTransactionStatusUpdateCron(String pendingTransactionStatusUpdateCron) {
		this.pendingTransactionStatusUpdateCron = pendingTransactionStatusUpdateCron;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getHoursBefore() {
		return hoursBefore;
	}

	public void setHoursBefore(String hoursBefore) {
		this.hoursBefore = hoursBefore;
	}

	public String getHoursInterval() {
		return hoursInterval;
	}

	public void setHoursInterval(String hoursInterval) {
		this.hoursInterval = hoursInterval;
	}

	public String getMinutesBefore() {
		return minutesBefore;
	}

	public void setMinutesBefore(String minutesBefore) {
		this.minutesBefore = minutesBefore;
	}

	public String getMinutesInterval() {
		return minutesInterval;
	}

	public void setMinutesInterval(String minutesInterval) {
		this.minutesInterval = minutesInterval;
	}

	public String getTransactionBankStatusEnquiryCron() {
		return transactionBankStatusEnquiryCron;
	}

	public void setTransactionBankStatusEnquiryCron(String transactionBankStatusEnquiryCron) {
		this.transactionBankStatusEnquiryCron = transactionBankStatusEnquiryCron;
	}

	public String getOrderConfirmApi() {
		return orderConfirmApi;
	}

	public void setOrderConfirmApi(String orderConfirmApi) {
		this.orderConfirmApi = orderConfirmApi;
	}

	public String getOrderMinutesBefore() {
		return orderMinutesBefore;
	}

	public void setOrderMinutesBefore(String orderMinutesBefore) {
		this.orderMinutesBefore = orderMinutesBefore;
	}

	public String getOrderMinutesInterval() {
		return orderMinutesInterval;
	}

	public void setOrderMinutesInterval(String orderMinutesInterval) {
		this.orderMinutesInterval = orderMinutesInterval;
	}

	public String getOrderConfirmationCron() {
		return orderConfirmationCron;
	}

	public void setOrderConfirmationCron(String orderConfirmationCron) {
		this.orderConfirmationCron = orderConfirmationCron;
	}

	public String getOrderConfirmation() {
		return orderConfirmation;
	}

	public void setOrderConfirmation(String orderConfirmation) {
		this.orderConfirmation = orderConfirmation;
	}

	public String getOrderConfirmApiUrl() {
		return orderConfirmApiUrl;
	}

	public void setOrderConfirmApiUrl(String orderConfirmApiUrl) {
		this.orderConfirmApiUrl = orderConfirmApiUrl;
	}

	public String getOrderMerchantPayId() {
		return orderMerchantPayId;
	}

	public void setOrderMerchantPayId(String orderMerchantPayId) {
		this.orderMerchantPayId = orderMerchantPayId;
	}

	public String geteCollectionData() {
		return eCollectionData;
	}

	public void seteCollectionData(String eCollectionData) {
		this.eCollectionData = eCollectionData;
	}

	public String geteColletionCron() {
		return eColletionCron;
	}

	public void seteColletionCron(String eColletionCron) {
		this.eColletionCron = eColletionCron;
	}

	public String getMONGO_DB_E_Collection() {
		return MONGO_DB_E_Collection;
	}

	public void setMONGO_DB_E_Collection(String mONGO_DB_E_Collection) {
		MONGO_DB_E_Collection = mONGO_DB_E_Collection;
	}

	public String getMONGO_DB_ImpsSettlementCollection() {
		return MONGO_DB_ImpsSettlementCollection;
	}

	public void setMONGO_DB_ImpsSettlementCollection(String mONGO_DB_ImpsSettlementCollection) {
		MONGO_DB_ImpsSettlementCollection = mONGO_DB_ImpsSettlementCollection;
	}

	public String getMONGO_DB_closingAmountCollection() {
		return MONGO_DB_closingAmountCollection;
	}

	public void setMONGO_DB_closingAmountCollection(String mONGO_DB_closingAmountCollection) {
		MONGO_DB_closingAmountCollection = mONGO_DB_closingAmountCollection;
	}

	public String getBulkImpsUpiData() {
		return bulkImpsUpiData;
	}

	public void setBulkImpsUpiData(String bulkImpsUpiData) {
		this.bulkImpsUpiData = bulkImpsUpiData;
	}

	public String getMONGO_DB_impsUpiBulkCollection() {
		return MONGO_DB_impsUpiBulkCollection;
	}

	public void setMONGO_DB_impsUpiBulkCollection(String mONGO_DB_impsUpiBulkCollection) {
		MONGO_DB_impsUpiBulkCollection = mONGO_DB_impsUpiBulkCollection;
	}

	public String getImpsUrl() {
		return impsUrl;
	}

	public void setImpsUrl(String impsUrl) {
		this.impsUrl = impsUrl;
	}

	public String getUpiUrl() {
		return upiUrl;
	}

	public void setUpiUrl(String upiUrl) {
		this.upiUrl = upiUrl;
	}

	public String getImpsUpiCron() {
		return impsUpiCron;
	}

	public void setImpsUpiCron(String impsUpiCron) {
		this.impsUpiCron = impsUpiCron;
	}
	
	public String getClosingAmount() {
		return closingAmount;
	}

	public void setClosingAmount(String closingAmount) {
		this.closingAmount = closingAmount;
	}

	public String getClosingAmountCron() {
		return closingAmountCron;
	}

	public void setClosingAmountCron(String closingAmountCron) {
		this.closingAmountCron = closingAmountCron;
	}

	public String getCLOSING_AMOUNT_COLLECTION() {
		return CLOSING_AMOUNT_COLLECTION;
	}

	public void setCLOSING_AMOUNT_COLLECTION(String cLOSING_AMOUNT_COLLECTION) {
		CLOSING_AMOUNT_COLLECTION = cLOSING_AMOUNT_COLLECTION;
	}
	public String getTransactionSchedule() {
		return transactionSchedule;
	}

	public void setTransactionSchedule(String transactionSchedule) {
		this.transactionSchedule = transactionSchedule;
	}

	public String getTransactionScheduleCron() {
		return transactionScheduleCron;
	}

	public void setTransactionScheduleCron(String transactionScheduleCron) {
		this.transactionScheduleCron = transactionScheduleCron;
	}
	public String getMONGO_DB_eNachCollectionName() {
		return MONGO_DB_eNachCollectionName;
	}

	public void setMONGO_DB_eNachCollectionName(String mONGO_DB_eNachCollectionName) {
		MONGO_DB_eNachCollectionName = mONGO_DB_eNachCollectionName;
	}
	public String getMONGO_DB_upiAutoPayCollectionName() {
		return MONGO_DB_upiAutoPayCollectionName;
	}

	public void setMONGO_DB_upiAutoPayCollectionName(String mONGO_DB_upiAutoPayCollectionName) {
		MONGO_DB_upiAutoPayCollectionName = mONGO_DB_upiAutoPayCollectionName;
	}

	public String getRetryCallback() {
		return retryCallback;
	}

	public void setRetryCallback(String retryCallback) {
		this.retryCallback = retryCallback;
	}


	public String getRetryCallbackApiUrl() {
		return retryCallbackApiUrl;
	}

	public void setRetryCallbackApiUrl(String retryCallbackApiUrl) {
		this.retryCallbackApiUrl = retryCallbackApiUrl;
	}

	public String getRetryCallbackCron() {
		return retryCallbackCron;
	}

	public void setRetryCallbackCron(String retryCallbackCron) {
		this.retryCallbackCron = retryCallbackCron;
	}

	public String getMONGO_DB_callbackRetryCollection() {
		return MONGO_DB_callbackRetryCollection;
	}

	public void setMONGO_DB_callbackRetryCollection(String mONGO_DB_callbackRetryCollection) {
		MONGO_DB_callbackRetryCollection = mONGO_DB_callbackRetryCollection;
	}

	public String getAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(String autoRefund) {
		this.autoRefund = autoRefund;
	}

	public String getAutoRefundCron() {
		return autoRefundCron;
	}

	public void setAutoRefundCron(String autoRefundCron) {
		this.autoRefundCron = autoRefundCron;
	}

	public String getAutoRefundUrl() {
		return autoRefundUrl;
	}

	public void setAutoRefundUrl(String autoRefundUrl) {
		this.autoRefundUrl = autoRefundUrl;
	}

	public String getPayoutStatusEnquiry() {
		return payoutStatusEnquiry;
	}

	public void setPayoutStatusEnquiry(String payoutStatusEnquiry) {
		this.payoutStatusEnquiry = payoutStatusEnquiry;
	}

	public String getPayoutStatusEnquiryCron() {
		return payoutStatusEnquiryCron;
	}

	public void setPayoutStatusEnquiryCron(String payoutStatusEnquiryCron) {
		this.payoutStatusEnquiryCron = payoutStatusEnquiryCron;
	}

	public String getImpsStatusEnquiryUrl() {
		return impsStatusEnquiryUrl;
	}

	public void setImpsStatusEnquiryUrl(String impsStatusEnquiryUrl) {
		this.impsStatusEnquiryUrl = impsStatusEnquiryUrl;
	}

	public String getUpiStatusEnquiryUrl() {
		return upiStatusEnquiryUrl;
	}

	public void setUpiStatusEnquiryUrl(String upiStatusEnquiryUrl) {
		this.upiStatusEnquiryUrl = upiStatusEnquiryUrl;
	}

	public String getPayoutMinutesInterval() {
		return payoutMinutesInterval;
	}

	public void setPayoutMinutesInterval(String payoutMinutesInterval) {
		this.payoutMinutesInterval = payoutMinutesInterval;
	}

	public String getPayoutMinutesBefore() {
		return payoutMinutesBefore;
	}

	public void setPayoutMinutesBefore(String payoutMinutesBefore) {
		this.payoutMinutesBefore = payoutMinutesBefore;
	}

	public String getAutoDeleteHistoricDataFolders() {
		return autoDeleteHistoricDataFolders;
	}

	public void setAutoDeleteHistoricDataFolders(String autoDeleteHistoricDataFolders) {
		this.autoDeleteHistoricDataFolders = autoDeleteHistoricDataFolders;
	}

	public String getAutoDeleteHistoricDataFoldersCron() {
		return autoDeleteHistoricDataFoldersCron;
	}

	public void setAutoDeleteHistoricDataFoldersCron(String autoDeleteHistoricDataFoldersCron) {
		this.autoDeleteHistoricDataFoldersCron = autoDeleteHistoricDataFoldersCron;
	}
	
	public String getHealthCheckUp() {
		return healthCheckUp;
	}

	public void setHealthCheckUp(String healthCheckUp) {
		this.healthCheckUp = healthCheckUp;
	}

	public String getHealthCheckUpCron() {
		return healthCheckUpCron;
	}

	public void setHealthCheckUpCron(String healthCheckUpCron) {
		this.healthCheckUpCron = healthCheckUpCron;
	}

	public String getCryptoStatusCheck() {
		return cryptoStatusCheck;
	}

	public void setCryptoStatusCheck(String cryptoStatusCheck) {
		this.cryptoStatusCheck = cryptoStatusCheck;
	}

	public String getCryptoStatusCheckCron() {
		return cryptoStatusCheckCron;
	}

	public void setCryptoStatusCheckCron(String cryptoStatusCheckCron) {
		this.cryptoStatusCheckCron = cryptoStatusCheckCron;
	}

	public String getMerchantInititatedDirectUrl() {
		return merchantInititatedDirectUrl;
	}

	public String getMerchantInititatedDirectStatusEnqUrl() {
		return merchantInititatedDirectStatusEnqUrl;
	}

	public void setMerchantInititatedDirectUrl(String merchantInititatedDirectUrl) {
		this.merchantInititatedDirectUrl = merchantInititatedDirectUrl;
	}

	public void setMerchantInititatedDirectStatusEnqUrl(String merchantInititatedDirectStatusEnqUrl) {
		this.merchantInititatedDirectStatusEnqUrl = merchantInititatedDirectStatusEnqUrl;
	}

	public String getCryptoTransactionStatusEnquiryUrl() {
		return cryptoTransactionStatusEnquiryUrl;
	}

	public void setCryptoTransactionStatusEnquiryUrl(String cryptoTransactionStatusEnquiryUrl) {
		this.cryptoTransactionStatusEnquiryUrl = cryptoTransactionStatusEnquiryUrl;
	}

	public String getMsedclRetryCallback() {
		return msedclRetryCallback;
	}

	public void setMsedclRetryCallback(String msedclRetryCallback) {
		this.msedclRetryCallback = msedclRetryCallback;
	}

	public String getMsedclRetryCallbackCron() {
		return msedclRetryCallbackCron;
	}

	public void setMsedclRetryCallbackCron(String msedclRetryCallbackCron) {
		this.msedclRetryCallbackCron = msedclRetryCallbackCron;
	}
	
	public String getPendingMinutesBefore() {
		return pendingMinutesBefore;
	}

	public void setPendingMinutesBefore(String pendingMinutesBefore) {
		this.pendingMinutesBefore = pendingMinutesBefore;
	}

	public String getPendingMinutesInterval() {
		return pendingMinutesInterval;
	}

	public void setPendingMinutesInterval(String pendingMinutesInterval) {
		this.pendingMinutesInterval = pendingMinutesInterval;
	}

	public String getMONGO_DB_transactionStatus() {
		return MONGO_DB_transactionStatus;
	}

	public void setMONGO_DB_transactionStatus(String mONGO_DB_transactionStatus) {
		MONGO_DB_transactionStatus = mONGO_DB_transactionStatus;
	}
	
	public String getPendingTransactionsStatusUpdate() {
		return pendingTransactionsStatusUpdate;
	}

	public void setPendingTransactionsStatusUpdate(String pendingTransactionsStatusUpdate) {
		this.pendingTransactionsStatusUpdate = pendingTransactionsStatusUpdate;
	}

	public String getPendingTransactionsStatusUpdateCron() {
		return pendingTransactionsStatusUpdateCron;
	}

	public void setPendingTransactionsStatusUpdateCron(String pendingTransactionsStatusUpdateCron) {
		this.pendingTransactionsStatusUpdateCron = pendingTransactionsStatusUpdateCron;
	}

	public String getEnquiryApiKey() {
		return enquiryApiKey;
	}

	public void setEnquiryApiKey(String enquiryApiKey) {
		this.enquiryApiKey = enquiryApiKey;
	}

}