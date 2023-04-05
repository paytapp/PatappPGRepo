package com.paymentgateway.commons.util;

public enum CrmFieldType {

	// Personal details
	    PIN                     ("pin",6,6,FieldFormatType.NUMBER),
		PASSWORD				("password",8,32,FieldFormatType.SPECIAL,CrmSpecialCharacters.PASSWORD),
		PAY_ID					("payId",10,20,FieldFormatType.NUMBER),
		OTP					    ("otp",10,20,FieldFormatType.NUMBER),
		FIRSTNAME				("firstName", 2, 32, FieldFormatType.ALPHASPACENUM),
		MIDDLENAME				("middleName", 2, 32, FieldFormatType.ALPHASPACE),
		LASTNAME 				("lastName", 2, 32, FieldFormatType.ALPHASPACENUM),
		COMPANY_NAME			("companyName", 2, 256,FieldFormatType.SPECIAL,CrmSpecialCharacters.COMPANY),
		WEBSITE					("website", 10, 256, FieldFormatType.SPECIAL,CrmSpecialCharacters.WEBSITE),
		CONTACT_PERSON			("contactPerson", 5,35,FieldFormatType.ALPHASPACE),
		EMAILID 				("emailId", 5, 100, FieldFormatType.EMAIL),
		INTERNAL_USER_EMAIL		("internalUserEmail", 5, 100, FieldFormatType.EMAIL),
		BUSINESS_TYPE			("businessType", 2, 250, FieldFormatType.SPECIAL,CrmSpecialCharacters.INDUSTRYCATEGORY),
		MERCHANT_TYPE			("merchantType",4,34,FieldFormatType.SPECIAL),
		MERCHANT			    ("merchant",2,150,FieldFormatType.ALPHASPACENUM),
		NO_OF_TRANSACTIONS		("noOfTransactions", 1, 32, FieldFormatType.NUMBER), 
		AMOUNT_OF_TRANSACTIONS	("amountOfTransactions", 3, 32, FieldFormatType.NUMBER),
		RESELLER_ID				("resellerId", 10,20,FieldFormatType.NUMBER),
		PRODUCT_DETAIL			("productDetail",3,23,FieldFormatType.ALPHA),
		REGISTRATION_DATE		("registrationDate", 2, 32, FieldFormatType.DATE),
		BUSINESS_NAME			("businessName",2,256,FieldFormatType.SPECIAL,CrmSpecialCharacters.BUSINESS_NAME),
	    USERSTATUS				("userStatus",4,30,FieldFormatType.ALPHA),
	    ORGANIZATIONTYPE		("organisationType",1,30,FieldFormatType.ALPHASPACENUM),
	    ACTIVATION_DATE		    ("activationDate",4,30,FieldFormatType.DATE),
	    ATTEMPT_TRASACATION		("attemptTrasacation",0,5,FieldFormatType.NUMBER),
	    MERCHANT_NAME			("merchantName",0,50,FieldFormatType.ALPHASPACE),
	    POST_SETTLED_FLAG		("postSettledFlag",0,20,FieldFormatType.ALPHASPACE),

	    MULTICURRENCY		    ("multiCurrency",2,30,FieldFormatType.ALPHA),
	    BUSINESSMODEL		    ("businessModel",2,100,FieldFormatType.SPECIAL,CrmSpecialCharacters.BUSINESSMODEL),
	    OPERATIONADDRESS	    ("operationAddress",3, 250, FieldFormatType.SPECIAL,CrmSpecialCharacters.ADDRESS),
	    OPERATION_CITY	        ("operationCity",3, 150, FieldFormatType.SPECIAL,CrmSpecialCharacters.ADDRESS),
	    PPERATION_STATE	        ("operationState",3, 150, FieldFormatType.SPECIAL,CrmSpecialCharacters.ADDRESS),
	    CIN		                ("cin",2,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.BUSINESSMODEL),
	    PAN		                ("pan",2,30,FieldFormatType.ALPHANUM),
	    PANNAME		            ("panName",2,30,FieldFormatType.ALPHASPACE),
	    DATE_OF_ESTABLISHMENT   ("dateOfEstablishment",6,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.DATE),
	    STATUS				    ("status",4,30,FieldFormatType.ALPHA),
	    INDUSTRY_CATEGORY		("industryCategory",2,350,FieldFormatType.SPECIAL,CrmSpecialCharacters.INDUSTRYCATEGORY),
	    INDUSTRY_SUB_CATEGORY	("industrySubCategory",2,350,FieldFormatType.SPECIAL,CrmSpecialCharacters.INDUSTRYSUBCATEGORY),

		// Contact Details	
		MOBILE 					("mobile", 10, 15, FieldFormatType.NUMBER ),
		TELEPHONE_NO 			("telephoneNo", 8, 15, FieldFormatType.SPECIAL,CrmSpecialCharacters.TELEPHONE ),
		FAX 					("fax", 8, 15, FieldFormatType.NUMBER),
		ADDRESS					("address", 3, 150, FieldFormatType.SPECIAL,CrmSpecialCharacters.ADDRESS),
		CITY					("city", 2, 32, FieldFormatType.ALPHASPACE),
		STATE					("state", 2, 32, FieldFormatType.ALPHASPACE),
		COUNTRY					("country", 2, 32, FieldFormatType.ALPHASPACE),
		POSTALCODE				("postalCode", 4, 20, FieldFormatType.NUMBER),
		OPERATION_POSTAL_CODE 	("operationPostalCode", 4, 20, FieldFormatType.NUMBER),
		
		// Action	
		COMMENTS				("comments", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		WHITE_LIST_IPADDRES 	("whiteListIpAddress", 6, 15, FieldFormatType.NUMBER),
		CLASS				 ("class", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		
		// Bank Details
		BANK_NAME			("bankName", 3, 200, FieldFormatType.ALPHASPACE),
		IFSC_CODE			("ifscCode", 11, 11, FieldFormatType.ALPHANUM),
		ACC_HOLDER_NAME		("accHolderName", 4, 200, FieldFormatType.ALPHASPACE),
		CURRENCY			("currency", 3, 3, FieldFormatType.ALPHANUM),       
		BRANCH_NAME			("branchName", 3, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.SPACE),
		PANCARD				("pancard", 6, 10, FieldFormatType.ALPHANUM),
		ACCOUNT_NO			("accountNo", 6, 200, FieldFormatType.NUMBER),
		
		// Account class
		RESPONSE		       ("response", 4, 200, FieldFormatType.ALPHASPACE),
		MERCHANT_EMAILID 	   ("merchantEmailId", 5, 100, FieldFormatType.EMAIL),
		NUMBER			       ("number", 6, 200, FieldFormatType.NUMBER),
		MERCHANT_ID		       ("MERCHANT_ID", 6, 200, FieldFormatType.NUMBER),
		ACQUIRER_PAY_ID	       ("ACQUIRER_PAY_ID", 6, 200, FieldFormatType.NUMBER),
		ACCOUNT_VALIDATION_KEY ("accountValidationKey", 6, 200, FieldFormatType.NUMBER),
		//Invoice Fields
		
		INVOICE_NUMBER        ("invoiceNo", 2, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.INVOICE_NUMBER),
		INVOICE_NAME          ("name", 2, 64, FieldFormatType.ALPHASPACE),
		INVOICE_PHONE         ("phone", 10, 10, FieldFormatType.NUMBER),
		INVOICE_EMAIL         ("email", 8, 100, FieldFormatType.EMAIL),
	    INVOICE_AMOUNT        ("amount", 1, 32, FieldFormatType.AMOUNT),
	    INVOICE_EXPIRES_DAY   ("expiresDay", 1, 31, FieldFormatType.DATETIME, CrmSpecialCharacters.INVOICE_DATE),
	    INVOICE_EXPIRES_HOUR  ("expiresHour", 1, 24, FieldFormatType.DATE),
	    INVOICE_CURRENCY_CODE ("currencyCode", 3, 32, FieldFormatType.ALPHANUM),
	    INVOICE_URL           ("invoiceURL", 8, 200, FieldFormatType.URL),
	    INVOICE_RETURN_URL    ("returnUrl", 8, 300, FieldFormatType.URL),
	    INVOICE_CITY          ("city", 2, 32, FieldFormatType.ALPHASPACE),
	    INVOICE_COUNTRY       ("country", 2, 100, FieldFormatType.SPECIAL, CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
	    INVOICE_STATE         ("state", 2, 32, FieldFormatType.SPECIAL, CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
	    INVOICE_ZIP           ("zip", 5, 6, FieldFormatType.NUMBER),
	    INVOICE_DESCRIPTION   ("productDesc", 2, 250, FieldFormatType.SPECIAL,CrmSpecialCharacters.ADDRESS),
	    INVOICE_ID            ("invoiceId", 2, 20, FieldFormatType.NUMBER),
	    PRODUCT_NAME          ("productName", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ADDRESS),
		QUANTITY			  ("quantity", 1, 200, FieldFormatType.NUMBER),
		SERVICE_CHARGE        ("serviceCharge", 1, 32, FieldFormatType.AMOUNT),
		SALT_KEY              ("saltKey", 2, 200, FieldFormatType.ALPHANUM),
		PARENT_PAY_ID		  ("parentPayId",10,20,FieldFormatType.NUMBER),
		TRANSACTION_EMAIL_ID  ("transactionEmailId", 5, 100, FieldFormatType.EMAIL),
		INVOICE_DURATION_FROM ("DURATION_FROM", 1, 24, FieldFormatType.DATE, CrmSpecialCharacters.INVOICE_DATE),
		INVOICE_DURATION_TO ("DURATION_TO", 1, 24, FieldFormatType.DATE, CrmSpecialCharacters.INVOICE_DATE),
		
		//Login Details
		IP					  ("IPADDRESS",5,20,FieldFormatType.SPECIAL, CrmSpecialCharacters.IP),
		INTERNAL_CUST_IP 	  ("internalCustIp",5,20,FieldFormatType.SPECIAL, CrmSpecialCharacters.IP),
		OPERATINGSYSTEM		  ("OPERATINGSYSTEM",4,20,FieldFormatType.SPECIAL,CrmSpecialCharacters.OPERATINGSYSTEM),
		BROWSER				  ("BROWSER",2,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.BROWSER),
		//Remittance Field
		UTR 			      ("utr", 2, 20, FieldFormatType.ALPHANUM),
		CAPTCHA			  	  ("captcha", 8, 8, FieldFormatType.ALPHANUM),
		NET_AMOUNT         	  ("netAmount", 1, 32, FieldFormatType.AMOUNT),
		REMITTED_DATE		  ("remittedDate", 1, 32, FieldFormatType.AMOUNT),
		
		// Account 
		MERCHANTID		      ("merchantId", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		ACQUIRER_PAYID	      ("acquirerPayId", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		ACQ_PAYID	          ("acqPayId", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		ACCOUNT_PASSWORD      ("password", 1, 500, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		TXN_KEY		          ("txnKey", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		ACQUIRER_NAME	      ("acquirerName", 1, 200, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		//Transaction reporting/search
		MAP_STRING            ("mapString", 2, 4000, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		MERCHANT_EMAIL_ID     ("merchantEmailId",5,100,FieldFormatType.EMAIL),
		CUSTOMER_EMAIL_ID     ("customerEmail",5,100,FieldFormatType.EMAIL),
		DATE_FROM             ("dateFrom",2,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.DATE),
		DATE_TO               ("dateTo",2,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.DATE),
		ACQUIRER              ("acquirer",2,30,FieldFormatType.ALPHASPACENUM),
		PAYMENT_TYPE          ("paymentType",2,30,FieldFormatType.ALPHASPACE),
		SUCCESS_MESSAGE       ("successMessage",2,100,FieldFormatType.ALPHASPACE),
		ORDER_ID              ("orderId",1,100,FieldFormatType.SPECIAL,CrmSpecialCharacters.ORDER_ID),
		CARD_NUMBER_MASK      ("cardNumber",2,100,FieldFormatType.SPECIAL,CrmSpecialCharacters.CARD_MASK),
		CATEGORY_CODE      	  ("categoryCode",2,100,FieldFormatType.SPECIAL,CrmSpecialCharacters.CATEGORY_CODE),
		SKU_CODE      		  ("SKUCode",2,100,FieldFormatType.SPECIAL,CrmSpecialCharacters.SKU_CODE),
		TXN_STATUS            ("status",2,30,FieldFormatType.ALPHASPACE),
		TRANSACTION_ID        ("transactionId",2,20,FieldFormatType.NUMBER),
		AMOUNT                ("amount",1,20,FieldFormatType.AMOUNT),
		GENERAL_STRING        ("messageString",1,6000,FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		UPLOADE_PHOTO         ("uploadePhoto",1,6000,FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		UPLOADE_PAN_CARD	  ("uploadedPanCard",1,6000,FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		UPLOADE_PHOTOID_PROOF      ("uploadedPhotoIdProof",1,6000,FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		UPLOADE_CONTRACT_DOCUMENT  ("uploadedContractDocument",1,6000,FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		//Extra Refund Amount
		EXTRA_REFUND_LIMIT		   ("extraRefundLimit",1,32,FieldFormatType.AMOUNT),
		DEFAULT_CURRENCY		   ("defaultCurrency",3,3,FieldFormatType.ALPHANUM),
		//Amex field43
		AMEX_SELLER_ID			   ("amexSellerId",1,40,FieldFormatType.ALPHANUM),
		MCC		   			       ("mCC",1,10,FieldFormatType.ALPHANUM),
		
		//Fraud Prevention System
		FRAUD_IP_ADDRESS					("IP address",7,15, FieldFormatType.SPECIAL, CrmSpecialCharacters.IP),
		FRAUD_SUBNET_MASK					("Subnet mask",7,15, FieldFormatType.SPECIAL, CrmSpecialCharacters.IP),
		FRAUD_DOMAIN_NAME					("Domain name", 10, 256,  FieldFormatType.SPECIAL, CrmSpecialCharacters.WEBSITE),
		FRAUD_NEGATIVE_BIN					("Card bin", 6, 6,  FieldFormatType.NUMBER),	
		FRAUD_NEGATIVE_CARD					("Card no.", 15, 19,  FieldFormatType.SPECIAL, CrmSpecialCharacters.CARD_MASK),
		FRAUD_MIN_TRANSACTION_AMOUNT		("Min. txn amount", 1, 32,  FieldFormatType.AMOUNT),
		FRAUD_MAX_TRANSACTION_AMOUNT		("Max. txn amount", 1, 32,  FieldFormatType.AMOUNT),
		FRAUD_ISSUER_COUNTRY				("Card issuer country", 2, 32,  FieldFormatType.ALPHASPACE),
		FRAUD_USER_COUNTRY					("User country", 2, 32,  FieldFormatType.ALPHASPACE),
		FRAUD_PER_CARD_TRANSACTION_ALLOWED	("Per card allowed txns", 1, 32,FieldFormatType.NUMBER),
		FRAUD_MINUTE_TXN_LIMIT				("Minute txn limit", 1, 32, FieldFormatType.NUMBER),
		FRAUD_CURRENCY						("Currency", 3, 3, FieldFormatType.ALPHANUM), 
		FRAUD_EMAIL							("EMAIL ID", 5, 100, FieldFormatType.EMAIL),
		
		//Ticketing System
		TICKET_ID							("ticketId",10,20,FieldFormatType.NUMBER),
		TICKET_MESSAGE						("messageBody",3,300,FieldFormatType.SPECIAL ,CrmSpecialCharacters.SPEICIAL_CHARACTERS_FOR_TICKET),
		TICKET_ASSIGNED_TO					("ticketAsssignedTo",2, 32,  FieldFormatType.ALPHA),
		TICKET_STATUS						("ticketStauts",4,30,FieldFormatType.ALPHA),
		TICKET_TYPE							("ticketType",4,30,FieldFormatType.ALPHA),
		TICKET_SUBJECT						("subject",3,60,FieldFormatType.SPECIAL ,CrmSpecialCharacters.SPEICIAL_CHARACTERS_FOR_TICKET),
		TICKET_MOBILE						("mobileNo",10, 15, FieldFormatType.NUMBER ),
		
		//Response fields
		AUTH_CODE							("authCode", 0, 6, FieldFormatType.ALPHANUM),
		RRN									("rrn", 0, 50, FieldFormatType.ALPHANUM),
		RESPONSE_CODE						("responseCode", 1, 10,  FieldFormatType.ALPHANUM),		
		RESPONSE_MESSAGE					("responseMessage", 1, 256, FieldFormatType.ALPHANUM),
		
		//ORDER INFO
		PG_TXN_MESSAGE						("pg TxnMessage", 1, 255, FieldFormatType.ALPHANUM),
		PG_RESP_CODE						("pg RespCode", 1, 10, FieldFormatType.ALPHANUM),
		PG_REF_NUM							("pg RefNum", 1, 100, FieldFormatType.ALPHANUM),
		PG_DATE_TIME						("pg DateTime", 1, 50, FieldFormatType.SPECIAL),
		TXN_ID								("txn Id", 16, 16,FieldFormatType.NUMBER),
		TXNTYPE								("txn Type", 4, 50, FieldFormatType.ALPHA),
		
		//For Settlement and Refund Validation Naming Convention
		SETTLEMENT							("Settlement",1,20,FieldFormatType.ALPHASPACE),
		REFUND_VALIDATION					("Refund Validation",1,20,FieldFormatType.ALPHASPACE),
		
		//BIN RANGE
		BIN_CODE							("bin code",6,9,FieldFormatType.NUMBER),
		BIN_RANGE_HIGH						("bin range high",1,24,FieldFormatType.NUMBER),
		BIN_RANGE_LOW						("bin range low",1,24,FieldFormatType.NUMBER),
		CARD_TYPE							("card type",2,30,FieldFormatType.SPECIAL ,CrmSpecialCharacters.CARD_TYPE),
		GROUP_CODE							("group code",1,50,FieldFormatType.ALPHASPACENUM),
		ISSUER_BANK							("issuer bank",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.SPEICIAL_CHARACTERS_FOR_TICKET),
		ISSUER_COUNTRY						("issuer country",1,50,FieldFormatType.ALPHASPACE),
		MOP_TYPE							("mop type",2,15,FieldFormatType.ALPHA),
		
		//pending Request
		Special_Fields            ("special Fields", 1, 4000, FieldFormatType.SPECIAL,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),

		
		//by VJ for additional validations
		
		USER_TYPE                           ("userType",1,200,FieldFormatType.ALPHASPACENUM),
		OPERATION                           ("operation",1,200,FieldFormatType.ALPHASPACENUM),
		FILE_NAME                           ("fileName",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		DEST_PATH                           ("destPath",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		FILE_NAME_STRUTS                    ("fileNameStruts",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
		FILE_FORMAT                         ("fileFormat",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
        ACCOUNT_CURRENCY_SET                ("accountCurrencySet",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
        RESELLER                            ("reseller",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
        SUBJECT								("subject",1,200,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
        MESSAGE								("Mmessage",1,1500,FieldFormatType.SPECIAL ,CrmSpecialCharacters.ALL_SPEICIAL_CHARACTERS),
        ACQ_ID								("acqId", 1, 100, FieldFormatType.ALPHANUM),
        RFU1								("rfu1", 1, 100, FieldFormatType.ALPHASPACENUM),
        RFU2								("rfu2", 1, 100, FieldFormatType.ALPHASPACENUM),
        MERCHANT_SERVICE_TAX                ("merchantServiceTax", 1, 32, FieldFormatType.AMOUNT),
        CARD_HOLDER_TYPE					("CARD_HOLDER_TYPE", 1, 100, FieldFormatType.ALPHASPACENUM),
        PAYMENTS_REGION						("PAYMENTS_REGION", 1, 100, FieldFormatType.ALPHASPACENUM),
        
		DEFAULT_LANGUAGE                    ("defaultLanguage",1,100,FieldFormatType.ALPHASPACENUM),
		EMAIL_EXPIRY_TIME                   ("emailExpiryTime",2,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.DATE),
		LAST_ACTION_NAME                    ("lastActionName",1,100,FieldFormatType.ALPHASPACENUM),
		MERCHANT_GST_NUMBER                 ("merchantGstNo",1,100,FieldFormatType.ALPHASPACENUM),
		UPDATE_DATE                         ("updateDate",2,30,FieldFormatType.SPECIAL,CrmSpecialCharacters.DATE),
		TENURE								("tenure", 1, 2, FieldFormatType.NUMBER),
		RATE_OF_INTERESET 					("rateOfInterest",1,10, FieldFormatType.NUMBER, CrmSpecialCharacters.DECIMAL),
		UPDATED_BY			                ("updatedBy",1,100,FieldFormatType.ALPHASPACENUM),
		DISPATCH_SLIP_NO					("dispatchSlipNo",1,100,FieldFormatType.ALPHANUM),
		COURIER_SERVICE_PROVIDER_NO			("courierServiceProviderNo",1,100,FieldFormatType.ALPHANUM),
		TYPE_OF_ENTITY						("typeOfEntity",2,30,FieldFormatType.ALPHASPACE), 
		DATE_OF_INCORPORATION				("dateOfIncorporation", 2, 32, FieldFormatType.DATE),   
		REGISTRATION_NUMBER					("registrationNumber",2,30,FieldFormatType.ALPHANUM), 
		DIRECTOR_NAME						("directorName",2,100,FieldFormatType.ALPHASPACE),
		DIRECTOR_NUMBER						("directorNumber",1,30,FieldFormatType.ALPHANUM), 
		CONSUMER_NUMBER						("consumerNumber",2,40,FieldFormatType.ALPHANUM),
		ELECTRICITY_PROVIDER				("electricityProvider",2,100,FieldFormatType.ALPHASPACE), 
		FILE_PATH							("filePath",2,200,FieldFormatType.SPECIAL),
		PAN_NUMBER							("panNumber",2,30,FieldFormatType.ALPHANUM),
		GSTIN_USERNAME						("gstinUsername",2,50,FieldFormatType.EMAIL),
		APP_KEY								("appKey",2,100,FieldFormatType.SPECIAL), 
		FILE_CONTENT_TYPE					("fileContentType",2,100,FieldFormatType.SPECIAL),
		FULL_NAME							("fullName",2,100,FieldFormatType.ALPHASPACE),
		DOB									("dob", 2, 32, FieldFormatType.DATE),
		ANNUAL_TURNOVER						("annualTurnover", 2, 32, FieldFormatType.NUMBER),
		ANNUAL_TURNOVER_ONLINE				("annualTurnover", 2, 32, FieldFormatType.AMOUNT),
		PERCENTAGE							("percentage", 2, 6, FieldFormatType.SPECIAL),
		THIRD_PARTY_NAME					("thirdPartyName",2,100,FieldFormatType.ALPHASPACENUM),
		REFUNDS_ALLOWED						("refundsAllowed",2,100,FieldFormatType.SPECIAL),
		SERVER_DETAILS						("serverDetails",2,100,FieldFormatType.ALPHASPACENUM),
		OS_ARCHITECTURE						("osArchitecture", 2, 3, FieldFormatType.NUMBER),
		BACKEND_TECHNOLOGY					("backendTechnology",2,100,FieldFormatType.ALPHASPACENUM),
		APPLICATION_SERVER_TECHNOLOGY		("applicationServerTechnology",2,100,FieldFormatType.ALPHASPACENUM),
		TRUE_FALSE_STRING					("trueFalse",2,5,FieldFormatType.ALPHA),
		INTEGRATION_TYPE					("integrationType",2,15,FieldFormatType.ALPHASPACENUM),
		YES_NO_NOTSURE						("yesNoNotSure",2,15,FieldFormatType.ALPHASPACE),
		EXPRESS_PAY_PARAMETER				("expressPayParameter",2,15,FieldFormatType.ALPHASPACE),
		MPA_STAGE							("mpaStage",2,3,FieldFormatType.ALPHASPACE),
		GST_OTP					    		("gstOtp",4,20,FieldFormatType.NUMBER),
		ISSUER_NAME							("issuerName",2, 50, FieldFormatType.ALPHASPACE),
	    CUST_ID								("CUST_ID", 2, 256, FieldFormatType.ALPHANUM),
		AADHAR_NUMBER						("AADHAR_NUMBER", 12, 12, FieldFormatType.NUMBER),
		MONTH_YEAR							("monthYear", 1, 32, FieldFormatType.ALPHASPACENUM),
		SLAB								("slab", 1, 3, FieldFormatType.ALPHASPACENUM),
		VPA									("vpa", 3, 100, FieldFormatType.SPECIAL,CrmSpecialCharacters.VPA),
		PAYER_ADDRESS				        ("PAYER_ADDRESS", 1, 255, FieldFormatType.UPIADDRESS),
		PAYER_NAME					        ("PAYER_NAME", 1, 90, FieldFormatType.ALPHASPACE),
		UTR_NO					        	("utrNo", 1, 50, FieldFormatType.ALPHANUM),
		VIRTUAL_ACCOUNT_NO					("virtualAccountNo",10,15,FieldFormatType.ALPHANUM);
	   
		
		private final String name;
		private final int minLength;
		private final int maxLength;
		private final FieldFormatType type;
		private final CrmSpecialCharacters specialChars;
		
		
		private CrmFieldType(String name,int minLength,int maxLength, FieldFormatType type, CrmSpecialCharacters specialChars)
		{
			this.name = name;
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.type = type;
			this.specialChars = specialChars;
		}
		
		private CrmFieldType(String name, int minLength, int maxLength, FieldFormatType type)
		{
			this.name = name;
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.type = type;
			this.specialChars=null;
		}

		public String getName() {
			return name;
		}
		public int getMinLength() {
			return minLength;
		}
		public int getMaxLength() {
			return maxLength;
		}
		public FieldFormatType getType(){
			return type;
		}

		public CrmSpecialCharacters getSpecialChars() {
			return specialChars;
		}
	}
