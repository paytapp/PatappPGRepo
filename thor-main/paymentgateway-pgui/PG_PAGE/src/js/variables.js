window.alreadyPopulated = false;
window.alreadyPopulatedEmi = false;
window.isBinChecked = false;
window.allowBinCheck = false;
window.tempCardBin = "";
window.pageInfoObj = null;
window.binPaymentType = {
	DC : "debitCard",
	CC : "creditCard",
	IN : "international"
};
window.cardPaymentType = {
	CC: "Credit Card",
	DC: "Debit Card"
};
window.objMopType = {
	"RU" : "surcharge_dc_rupay",
	"VI" : "surcharge_dc_visa",
	"MC" : "surcharge_dc_mastercard",
};

window.surchargeMopType = {
	"DC" : {
        "RU" : "surcharge_dc_rupay",
        "VI" : "surcharge_dc_visa",
	    "MC" : "surcharge_dc_mastercard"    
    },
    "CC" : {
        "CONSUMER" : "surcharge_cc_consumer",
        "COMMERCIAL" : "surcharge_cc_commercial",
        "PREMIUM" : "surcharge_cc_premium"
    },
    "IN" : "surcharge_in",
    "UP" : "surcharge_up",
    "UPI_QR" : "surcharge_up",
    "NB" : "surcharge_nb",
    "WL" : "surcharge_wl",
	"PPL" : "surcharge_wl",
	"PPWL" : "surcharge_wl",
    "CD" : "surcharge_cd",
	"CR" : "surcharge_cr",
    "EMICC" : "surcharge_em_cc",
    "EMIDC" : "surcharge_em_dc",
	"AP": "surcharge_ap"
};

window.mopCollection = {};
window._paymentBox = null;

window.walletText = {
	"PhonePayWallet" : "PhonePe",
	"JioMoneyWallet" : "Jio Money",
	"AirtelPayWallet" : "Airtel Money",
	"OlaMoneyWallet" : "Ola Money",
	"PayZappWallet" : "PayZapp",
	"FreeChargeWallet" : "Free Charge",
	"AmazonPayWallet" : "Amazon Pay",
	"MobikwikWallet" : "MobiKwik",
	"PaytmWallet" : "Paytm",
	"MatchMoveWallet" : "Match Move",
	"GooglePayWallet" : "Google Pay",
	"ItzCashWallet" : "ItzCash",
	"MPesaWallet" : "M-Pesa",
	"OxyzenWallet" : "Oxyzen Wallet",
	"SbiBuddyWallet" : "SBI Buddy",
	"ZipCashWallet" : "Zip Cash"
};

window.taxDeclarationObj = {
	DC: "Charges Applicable : 0.40% Convenience Fee plus applicable taxes on all transactions upto INR 2000 through Debit Cards. 0.90% Convenience Fee plus applicable taxes on all transactions above INR 2000 through Debit Cards.",
	CC: "Charges Applicable : 0.95% Convenience Fee plus applicable taxes on all transactions through Credit Cards.",
	IN: "Charges Applicable : 2.5% Convenience Fee plus applicable taxes on all transactions through International Cards.",
	WL: "Charges Applicable : 2.0% Convenience Fee plus applicable taxes on all transactions through any Online Wallet.",
	NB: "Charges Applicable : INR 15 Convenience Fee plus applicable taxes on all transactions through Net Banking.",
	UP: "Charges Applicable : No Convenience Fee applicable on UPI transactions.",
	UP_QR: "Charges Applicable : No Convenience Fee applicable on UPI transactions.",
	EM: "Charges Applicable : INR 15 Convenience Fee plus applicable taxes on all transactions through EMI.",
	CD: "Charges Applicable : INR 15 Convenience Fee plus applicable taxes on all transactions through Cash on Delivery.",
	AP: "Charges Applicable : INR 15 Convenience Fee plus applicable taxes on all transactions through Cash on Delivery."
};

window.otpInterval = null;
window.loginpin = null;
window.globalBtnData = null;
window.SURCHARGE = null;
window.GST = null;
window.TOTAL_AMT = null;
window.SHOW_GST = false;

window.globalVar = null;
window.coddatakey = null;

window.nbDisplayName = {
    "1005" : "AXIS Bank",
	"1043" : "BBK",
	"1009" : "Bank Of India",
	"1064" : "BOM",
	"1055" : "Canara Bank",
	"1063" : "CBI",
	"1010" : "CitiBank",
	"1060" : "CUB",
	"1034" : "CRB",
	"1026" : "Deutsche Bank",
	"1040" : "DCB Bank",
	"1027" : "Federal Bank",
	"1004" : "HDFC Bank",
	"1013" : "ICICI Bank",
	"1069" : "Indian Bank",
	"1022" : "YES BANK CB",
	"1049" : "IOB",
	"1054" : "IndusInd Bank",
	"1003" : "IDBI Bank",
	"1062" : "ING Vysya Bank",
	"1041" : "J&K Bank",
	"1032" : "Karnatka Bank",
	"1048" : "KVB Bank",
	"1012" : "Kotak Bank",
	"1042" : "OBC",
	"1053" : "Ratnakar Bank",
	"1045" : "SIB",
	"1050" : "SBBJ",
	"1039" : "SBH",
	"1030" : "SBI",
	"1037" : "SBM",
	"1068" : "SBP",
	"1061" : "SBT",
	"1065" : "TM Bank",
	"1038" : "Union Bank Of India",
	"1046" : "United Bank Of India",
	"1044" : "Vijaya Bank",
	"1001" : "Yes Bank",
	"1006" : "IdfcUpi Bank",
	"1105" : "Dhanalakshmi Bank",
	"1106" : "Saraswat Bank",
	"1107" : "PNB",
	"1108" : "PSB",
	"1109" : "Bandhan Bank",
	"1110" : "Dena Bank",
	"1111" : "IDFC Bank",
	"1112" : "DBS Bank",
	"1113" : "NKGSB Bank",
	"1114" : "RBL Bank",
	"1115" : "SVC Bank",
	"1116" : "JSB",
	"1117" : "Allahabad Bank",
	"1091" : "Andhra Bank",
	"1092" : "BOB Corporate",
	"1093" : "Bank of Baroda",
	"1094" : "CSB",
	"1095" : "LVB",
	"1096" : "PNB Corporate",
	"1097" : "SCB",
	"1098" : "Syndicate Bank",
	"1099" : "Axis Corporate Bank",
	"1100" : "ICICI Corporate Bank",
	"1101" : "PNB Corporate Bank",
	"1102" : "HSBC Bank",
	"1103" : "UCO Bank",
	"1104" : "COSMOS Bank",
	"1118" : "Andhra Bank Corporate",
	"1119" : "Karnataka Graima Bank",
	"1120" : "PMC Bank",
	"1121" : "TNSC Bank",
	"1122" : "TJSB Bank",
	"1123" : "KJS Bank",
	"1124" : "MUC Bank",
	"1125" : "RBL Bank Ltd Co. Bank",
	"1126" : "SVC Bank Corporate",
	"1127" : "DBC",
	"1128" : "BCC Bank",
	"1129" : "PNB Yuva NetBanking",
	"1130" : "KCC Bank",
	"1131" : "ESF Bank",
	"1132" : "TBS Bank Ltd",
	"1133" : "SSF Bank",
	"1134" : "ESAF Small Finance Bank",
	"1135" : "VC Bank Ltd",
	"1136" : "NESF Bank Ltd",
	"1137" : "CBC",
	"1138" : "BC Banking",
	"1139" : "ZC Bank",
	"1140" : "ASF Bank",
	"1141" : "BBCB",
	"1142" : "Fincare Bank",
	"1143" : "APGB",
	"1144" : "SMC Bank Ltd",
}