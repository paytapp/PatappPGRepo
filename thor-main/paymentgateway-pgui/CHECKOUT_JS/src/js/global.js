Window.basePath = process.env.PUBLIC_URL;
Window.baseUrl = process.env.REACT_APP_BASE_URL;

Window.pageInfoObj = null;

Window.addMoneyTimer = null;
Window.newWindowTimer = null;

// GLOBAL METHODS
Window.querySelector = document.querySelector.bind(document);
Window.querySelectorAll = document.querySelectorAll.bind(document);
Window.id = document.getElementById.bind(document);
Window.byClass = document.getElementsByClassName.bind(document);

Window.worker = "";
Window.oid = "";
Window.worker = undefined;

// Window.walletToCompare = "PaytmWallet";
Window.walletToCompare = "";

Window.windownTransactionComplete = false;

Window.binPaymentType = {
	DC : "debitCard",
	CC : "creditCard",
	IN : "international"
};

Window.cardPaymentType = {
	CC: "Credit Card",
	DC: "Debit Card"
};

Window.surchargeMopType = {
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
    "CD" : "surcharge_cd",
	"CR" : "surcharge_cr",
    "EMICC" : "surcharge_em_cc",
    "EMIDC" : "surcharge_em_dc"
}

Window.walletText = {
	"PhonePayWallet" : "PhonePe",
	"JioMoneyWallet" : "Jio Money",
	"AirtelPayWallet" : "Airtel Money",
	"OlaMoneyWallet" : "Ola Money",
	"PayZappWallet" : "PayZapp",
	"FreeChargeWallet" : "Free Charge",
	"AmazonPayWallet" : "Amazon Pay",
	"MobikwikWallet" : "MobiKwik",
	"PaytmWallet" : "Paytm",
	"GooglePayWallet" : "Google Pay",
	"ItzCashWallet" : "ItzCash",
	"MPesaWallet" : "M-Pesa",
	"OxyzenWallet" : "Oxyzen Wallet",
	"SbiBuddyWallet" : "SBI Buddy",
	"ZipCashWallet" : "Zip Cash",
	"YesPayWallet" : "Yes Pay",
	"PayCashWallet" : "Pay Cash"
};