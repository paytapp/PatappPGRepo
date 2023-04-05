package com.paymentgateway.commons.exception;

public enum Messages {
	
	//Notification Message
	PENDING_RESELLER_MAPPING_	("401", "Request to update Reseller mapping"),
	MERCHANT_MAPPING_MESSAGE	("402", "Request to update Merchant mapping"),
	TDR_MESSAGE					("403", "Request to update TDR"),
	USER_UPDATE_MESSAGE			("404", "Request to update User profile"),
	PENDING_SERVICE_TAX			("405", "Request to update Service tax"),
	MERCHANT_SURCHARGE_MESSAGE	("406", "Request to update Merchant Surcharge"),
	BANK_SURCHARGE_MESSAGE		("407", "Request to update Bank Surcharge"),
	ACTIVE_RESELLER_MAPPING		("408", "Reseller Mapping changed"),
	ACTIVE_SERVICE_TAX			("409", "Service tax has changed"),
	RESELLER_MAPPING_MESSAGE	("402", "Request to update Reseller mapping");
	
	//Response code for user
		private final String responseCode;
		
		//This code contains more details about this error - it may be internal
		private final String code;
		
		//This message contains more details about the error - it may be internal
		private final String internalMessage;
		
		//message for displaying to user
		private final String responseMessage;
		
		private Messages(String code, String responseCode, String internalMessage, String userMessage){
			this.code = code;
			this.responseCode = responseCode;
			this.internalMessage = internalMessage;
			this.responseMessage = userMessage;
		}
		private Messages(String code, String userMessage){
			this.code = this.responseCode = code;
			this.internalMessage = this.responseMessage = userMessage;
		}
		private Messages(String code, String responseCode, String userMessage){
			this.code = code;
			this.responseCode = responseCode;
			this.internalMessage = this.responseMessage = userMessage;
		}

		public String getCode() {
			return code;
		}

		public String getInternalMessage() {
			return internalMessage;
		}

		public String getResponseMessage() {
			return responseMessage;
		}

		public String getResponseCode() {
			return responseCode;
		}
}
