package com.paymentgateway.commons.util;

/**
 * @author Harpreet
 *
 */
public enum CrmActions {

	ADMIN_MERCHANT_LIST				 ("merchantList"),
	ADMIN_RESELLER_LISTS			 ("adminResellers"),				// old action name ("resellerLists"),
	ADMIN_MOP_SET_UP_ACTION			 ("mopSetUpAction"),
	ADMIN_RESELLER_MAPPING_ACTION	 ("resellerMappingAction"),
	ADMIN_CHARGING_PLATFORM			 ("chargingPlatform"),
	ADMIN_MERCHANT_CRM_SIGNUP		 ("merchantCrmSignup"),
	ADMIN_ANALYTICS					 ("analytics"),
	ADMIN_KOTAK_REFUND				 ("kotakRefund"),
	ADMIN_YES_BANK_REFUND			 ("yesBankRefund"),
	ADMIN_ADD_REMITTANCE			 ("addRemittance"),
	ADMIN_RESTRICTIONS				 ("adminRestrictions"),
	
	
	//common actions for MERCHANT and ADMIN
	HOME							 ("home"),
	AUTHORIZE_TRANSACTION			 ("authorizeTransaction"),
	CAPTURE_TRANSACTION				 ("captureTransaction"),
	INCOMPLETE_TRANSACTION			 ("incompleteTransaction"),
	FAILED_TRANSACTION				 ("failedTransaction"),
	CANCEL_TRANSACTION				 ("cancelTransaction"),
	INVALID_TRANSACTION				 ("invalidTransaction"),
	MERACHANT_ANALYTICS				 ("merachantAnalytics"),
	TRANSACTION_SEARCH				 ("transactionSearch"),
	SNAPSHOT_REPORT					 ("snapshotReport"),
	SUMMARY_REPORT					 ("summaryReport"),
	REFUND_REPORT					 ("refundReport"),
	VIEW_REMITTANCE					 ("viewRemittance"),
	INVOICE							 ("invoice"),
	INVOICE_EVENT					 ("invoiceEvent"),
	INVOICE_SEARCH					 ("invoiceSearch"),
	ADD_USER						 ("addUser"),
	SEARCH_USER						 ("searchUser"),
	LOGIN_HISTORY_REDIRECT_USER		 ("loginHistoryRedirectUser"),
	MERCHANT_PROFILE				 ("merchantProfile"),
	LOGIN_HISTORY_REDIRECT			 ("loginHistoryRedirect"),
	PASSWORD_CHANGE					 ("passwordChangesummary"),
	SUMMARY							 ("summary"),
	PAYMENT_PAGE_SETTING			 ("paymentPageSetting"),
	CHANGE_PASSWORD					 ("passwordChange"),
	SETTLEMENT_REPORT				 ("settlementReport"),
	WEEKLY_ANALYTICS				 ("weeklyAnalytics"),
	ANALYTICS_PERFORMANCE_REPORT 	 ("analyticsPerfomanceReport"),
	ANALYTICS_REVENUE				 ("analyticsRevenue"),
	MERCHANT_ADD_BULK_USERS			 ("merchantAddBulkUsers"),
	MERCHANT_SUB_USERS				 ("merchantSubUsers"),
	ROUTER_CONFIG_ACTION			 ("routerConfigurationAction"),
	PAYMENT_OPTIONS					 ("paymentOptions"),
	SUF_DETAILS						 ("sufDetails"),
	DISPLAY_BULK_CHARGES			 ("displayBulkChargesUpdate"),
	DISPLAY_MERCHANT_DEF_RATE		 ("displayMerchantDefaultRate"),
	PENDING_REQ						 ("pendingRequest"),
	DOWNLOAD_PAYMENTS_REPORT		 ("downloadPaymentsReport"),
	SALE_TXN_SEARCH					 ("saleTransactionSearch"),
	REFUND_TXN_SEARCH				 ("refundTransactionSearch"),
	SETTLED_TXN_SEARCH				 ("settledTransactionSearch"),
	DOWNLOAD_TXN_REPORT				 ("downloadTransactionsReport"),
	PAYMENT_ADVICE_REPORT			 ("paymentAdviseReport"),
	BULK_INVOICE					 ("bulkInvoice"),
	BULK_INVOICE_SEARCH				 ("bulkInvoiceSearch"),
	MANAGE_BIN_RANGE 				 ("manageBinRange"),
	MANAGE_EMI_BIN_RANGE			 ("manageEmiBinRange"),
	ADD_SUBADMIN					 ("addSubAdmin"),
	SEARCH_SUBADMIN					 ("searchSubAdmin"),
	ADD_AGENT						 ("addAgent"),
	SEARCH_AGENT					 ("searchAgent"),
	SUBADMIN_LIST					 ("subAdminList"),
	AGENT_SEARCH					 ("agentSearch"),
	SUMMARY_REPORTS					 ("summaryReports"),
	DOWNLOADS_SUMMARY_REPORT		 ("downloadSummaryReport"),
	REFUNDS_REPORT					 ("refundReports"),
	MPR_UPLOAD_DWN_REPORT			 ("mprUploadDownloadReport"),
	REFUND_SUMMARY_REPORT			 ("refundSummaryReport"),
	MIS_REPORTS						 ("misReports"),
	VIEW_CHARGEBACK					 ("viewChargeback");
	
	private final String value;
	
	private CrmActions(String value){
		this.value = value;
	}
	 
	public String getValue() {
		return value;
	}
	
}
