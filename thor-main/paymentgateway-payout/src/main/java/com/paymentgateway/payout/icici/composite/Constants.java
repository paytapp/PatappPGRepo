package com.paymentgateway.payout.icici.composite;

/**
 * @author Rahul
 *
 */
public class Constants {

	public static final String BENEFICIARY_ACC_NO = "BeneAccNo";
	public static final String BENEFICIARY_IFSC = "BeneIFSC";
	public static final String AMOUNT = "Amount";
	public static final String TRAN_REF_NO = "TranRefNo";
	public static final String PAYMENT_REF = "PaymentRef";
	public static final String REMITTER_NAME = "RemName";
	public static final String REMITTER_MOBILE = "RemMobile";
	public static final String RETAILER_CODE = "RetailerCode";
	public static final String PASSCODE = "PassCode";
	public static final String IMPS_RETAILER_CODE_VALUE = "";
	public static final String IMPS_PASSCODE_VALUE = "";
	public static final String EQUATOR = "=";
	public static final String SEPARATOR = "&";
	public static final String PAYMENT_TYPE = "FTTransferP2A";
	
	//ADD BENE
	public static final String BENE_ACC_NO = "BnfAccNo";
	public static final String BENE_NAME = "BnfName";
	public static final String BENE_NICKNAME = "BnfNickName";
	public static final String BENE_PAYEE_TYPE = "PayeeType";
	public static final String BENE_IFSC = "IFSC";
	public static final String BENE_CORP_ID = "CrpId";
	public static final String BENE_CORP_USER = "CrpUsr";
	public static final String BENE_AGGR_ID = "AGGR_ID";
	public static final String BENE_NETWORK_ID = "NetworkId";
	public static final String BENE_VPA = "VPA";
	

	
	//REGISTRATION
	public static final String AGGRID = "AGGRID";
	public static final String AGGRNAME = "AGGRNAME";
	public static final String CORPID = "CORPID";
	public static final String USERID = "USERID";
	public static final String URN = "URN";
	public static final String ALIASID = "ALIASID";
	
	//TRANSACTION
	public static final String DEBITACC = "DEBITACC";
	public static final String CREDITACC = "CREDITACC";
	public static final String IFSC = "IFSC";
	public static final String TRANSCTION_AMOUNT = "AMOUNT";
	public static final String CURRENCY = "CURRENCY";
	public static final String TXNTYPE = "TXNTYPE";
	public static final String PAYEENAME = "PAYEENAME";
	public static final String REMARKS = "REMARKS";
	public static final String UNIQUEID = "UNIQUEID";
	
	//ACCOUNT STATEMENT
	public static final String ACCOUNTNO = "ACCOUNTNO";
	public static final String FROMDATE = "FROMDATE";
	public static final String TODATE = "TODATE";
	public static final String CONFLG = "CONFLG";
	public static final String LASTTRID = "LASTTRID";

	//REQUEST TYPE
	public static final String REQ_REGISTER = "registration";
	public static final String REQ_REGISTER_STATUS = "registrationStatus";
	public static final String REQ_ADD_BENE = "addBene";
	public static final String REQ_VALIDATE_BENE = "validateBene";
	public static final String REQ_BALANCE_INQUIRY = "balanceInquiry";
	public static final String REQ_ACCOUNT_STATEMENT = "accountStatement";
	public static final String REQ_TRANSACTION_INQUIRY = "transactionInquiry";
	public static final String REQ_TRANSACTION = "transaction";
	
	//Transaction Messages
	public static final String TRANSACTION_SUCCESS = "Transaction Successful";
	public static final String TRANSACTION_FAILED = "Transaction Failed";
	public static final String TRANSACTION_PENDING = "Transaction Pending, Please Check Status After Sometimes";
	public static final String TRANSACTION_PROCESSING = "Transaction Processing, Please Check Status After Sometimes";
	public static final String TRANSACTION_DUPICATE = "Duplicate Transaction Id";
	
	//Composite IMPS Api
	public static final String localTxnDtTime = "localTxnDtTime";
	public static final String beneAccNo = "beneAccNo";
	public static final String beneIFSC = "beneIFSC";
	public static final String amount = "amount";
	public static final String tranRefNo = "tranRefNo";
	public static final String paymentRef = "paymentRef";
	public static final String senderName = "senderName";
	public static final String mobile = "mobile";
	public static final String retailerCode = "retailerCode";
	public static final String passCode = "passCode";
	public static final String bcID = "bcID";
	public static final String crpId = "crpId";
	public static final String crpUsr = "crpUsr";
	public static final String transRefNo = "transRefNo";
	public static final String aggrId = "aggrId";
	public static final String R_CODE = "rcode";
	
	//Composite UPI API
	public static final String DEVICE_ID = "device-id";
	public static final String MOBILE = "mobile";
	public static final String CHANNEL_CODE = "channel-code";
	public static final String PROFILE_ID = "profile-id";
	public static final String SEQ_NO = "seq-no";
	public static final String ACCOUNT_PROVIDER = "account-provider";
	
	public static final String ACCOUNT_NUMBER = "account-number";
	public static final String USE_DEFAULT_ACC = "use-default-acc";
	public static final String ACCOUNT_TYPE = "account-type";
	public static final String PAYEE_VA = "payee-va";
	public static final String PAYER_VA = "payer-va";
	public static final String PAYEE_NAME = "payee-name";
	public static final String UPI_AMOUNT = "amount";
	public static final String PRE_APPROVED = "pre-approved";
	public static final String DEFAULT_DEBIT = "default-debit";
	public static final String DEFAULT_CREDIT = "default-credit";
	public static final String UPI_CURRENCY = "currency";
	public static final String TXN_TYPE = "txn-type";
	public static final String UPI_REMARKS = "remarks";
	public static final String MCC = "mcc";
	public static final String MERCHANT_TYPE = "merchant-type";
	public static final String UPI_CORP_ID = "crpID";
	public static final String UPI_USER_ID = "userID";
	public static final String UPI_AGGR_ID = "aggrID";
	public static final String UPI_URN = "urn";
	public static final String GLOBAL_ADDRESS_TYPE = "global-address-type";
	public static final String PAYEE_ACCOUNT = "payee-account";
	public static final String PAYEE_IFSC = "payee-ifsc";
	public static final String VPA = "vpa";
	public static final String ORI_SEQ_NO = "ori-seq-no";
	
	//UPI BENE ADDITION
	public static final String ADD_BENE_VPA = "vpa";
	public static final String UPI_BENE_CORPID = "corpID";
	
	
	//TRANSACTION
	
	public static final String senderAcctNo = "senderAcctNo";
	public static final String beneName = "beneName";
	public static final String narration1 = "narration1";
	public static final String narration2 = "narration2";
	public static final String WORKFLOW_REQD = "WORKFLOW_REQD";
	public static final String txnType = "txnType";
	
	// PAYBLE IMPS
	
	public static final String PayMethod = "payMethod";
	
	public static final String RTGS = "RTGS";
	public static final String NEFT_AGGR_ID = "aggrId";
	public static final String NEFT_AGGR_NAME = "aggrName";
	public static final String NEFT_CORP_ID = "crpId";
	public static final String NEFT_CORP_USER = "crpUsr";
	public static final String NEFT_URN = "urn";
	
	//Composite priority
	
	public static final String UPI_PRIORITY = "1000";
	public static final String IMPS_PRIORITY = "0100";
	public static final String NEFT_PRIORITY = "0010";
	public static final String RTGS_PRIORITY = "0001";
	
	//ADF Fields
	public static final String ADF_1 = "ADF_1";
	public static final String ADF_2 = "ADF_2";
	public static final String ADF_3 = "ADF_3";
	public static final String ADF_4 = "ADF_4";
	public static final String ADF_5 = "ADF_5";
	public static final String ADF_6 = "ADF_6";
	public static final String ADF_7 = "ADF_7";
	public static final String ADF_8 = "ADF_8";
	public static final String ADF_9 = "ADF_9";
	public static final String ADF_10 = "ADF_10";
	public static final String ADF_11 = "ADF_11";
	public static final String ADF_12 = "ADF_12";
	public static final String ADF_13 = "ADF_13";
	public static final String ADF_14 = "ADF_14";
	public static final String ADF_15 = "ADF_15";
	public static final String ADF_16 = "ADF_16";
	public static final String ADF_17 = "ADF_17";
	public static final String ADF_18 = "ADF_18";
	public static final String ADF_19 = "ADF_19";
	public static final String ADF_20 = "ADF_20";
	public static final String ADF_21 = "ADF_21";
	public static final String ADF_22 = "ADF_22";
	public static final String ADF_23 = "ADF_23";
	public static final String ADF_24 = "ADF_24";
	public static final String ADF_25 = "ADF_25";
	public static final String ADF_26 = "ADF_26";
	public static final String ADF_27 = "ADF_27";
	public static final String ADF_28 = "ADF_28";
	public static final String ADF_29 = "ADF_29";
	public static final String ADF_30 = "ADF_30";
	public static final String ADF_31 = "ADF_31";
	public static final String ADF_32 = "ADF_32";
	public static final String ADF_33 = "ADF_33";
	public static final String ADF_34 = "ADF_34";
	public static final String ADF_35 = "ADF_35";
	public static final String ADF_36 = "ADF_36";
	public static final String ADF_37 = "ADF_37";
	public static final String ADF_38 = "ADF_38";
	public static final String ADF_39 = "ADF_39";
	public static final String ADF_40 = "ADF_40";
	public static final String ADF_41 = "ADF_41";
	public static final String ADF_42 = "ADF_42";
	public static final String ADF_43 = "ADF_43";
	public static final String ADF_44 = "ADF_44";
	public static final String ADF_45 = "ADF_45";
	public static final String ADF_46 = "ADF_46";
	public static final String ADF_47 = "ADF_47";
	public static final String ADF_48 = "ADF_48";
	public static final String ADF_49 = "ADF_49";
	public static final String ADF_50 = "ADF_50";
	
	
	public static final String iciciBank = "iciciBank";
	public static final String Current = "Current";
	public static final String Nodal = "Nodal";
	
	
}
