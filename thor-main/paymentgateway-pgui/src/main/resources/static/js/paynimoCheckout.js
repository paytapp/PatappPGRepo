$(function() {
    const handleResponse = function(res) {
        if (typeof res != 'undefined' && typeof res.paymentMethod != 'undefined' && typeof res.paymentMethod.paymentTransaction != 'undefined' && typeof res.paymentMethod.paymentTransaction.statusCode != 'undefined' && res.paymentMethod.paymentTransaction.statusCode == '0300') {
            // success block
        } else if (typeof res != 'undefined' && typeof res.paymentMethod != 'undefined' && typeof res.paymentMethod.paymentTransaction != 'undefined' && typeof res.paymentMethod.paymentTransaction.statusCode != 'undefined' && res.paymentMethod.paymentTransaction.statusCode == '0398') {
            // initiated block
        } else {
            // error block
        }
    };

    const paynimoCheckout = obj => {
        clearInterval(checkoutInterval);

        const data = JSON.parse(obj);

        const configJson = {
            'tarCall': false,
            'features': {
                'showLoader' : false,
                'showPGResponseMsg': true,
                'enableNewWindowFlow': false,    //for hybrid applications please disable this by passing false
                'enableExpressPay':true,
                'siDetailsAtMerchantEnd': true,
                'payDetailsAtMerchantEnd':true,
                'enableSI':true,
            },
            'consumerData': {
                'deviceId': 'WEBSH1', //possible values 'WEBSH1', 'WEBSH2' and 'WEBMD5'
                'token': data.TOKEN,
                'returnUrl': data.RETURN_URL,
                'responseHandler': handleResponse,
                'paymentMode': 'netBanking',
                //'paymentMode': 'all',
                'merchantLogoUrl': data.PAYMENT_GATEWAY_LOGO,
                'merchantId': data.MERCHANT_ID,
                'currency': 'INR',				
                'bankCode' : data.BANK_CODE,				
                'consumerId': data.TXN_ID, //Your unique consumer identifier to register a SI
                'consumerMobileNo': data.CONSUMER_MOBILE_NO,
                'consumerEmailId': data.CONSUMER_EMAIL_ID,
                'txnId': data.PG_REF_NUM,   //Unique merchant transaction ID
                'items': [{ 'itemId' : data.ITEM_ID, 'amount' : data.AMOUNT, 'comAmt': '0'}],                                                                                                        
                'customStyle': {
                    'PRIMARY_COLOR_CODE': '#002663',   //merchant primary color code
                    'SECONDARY_COLOR_CODE': '#FFFFFF',   //provide merchant's suitable color code
                    'BUTTON_COLOR_CODE_1': '#002663',   //merchant's button background color code
                    'BUTTON_COLOR_CODE_2': '#FFFFFF'   //provide merchant's suitable color code for button text
                },        
                'accountHolderName' : data.ACCOUNT_HOLDER_NAME,
                'ifscCode': data.ifscCode,        //Pass this if ifscCode is captured at merchant side.                                                                                                
                'accountNo': data.ACCOUNT_NUMBER,
                'accountType': data.ACCOUNT_TYPE,
                'debitStartDate': data.START_DATE,
                'debitEndDate': data.END_DATE,
                'maxAmount': data.MAX_AMOUNT,
                'amountType': data.AMOUNT_TYPE,
                'frequency': data.FREQUENCY,              //  Available options DAIL, Week, MNTH, QURT, MIAN, YEAR, BIMN and ADHO
                //'saveInstrument': true  //mandatory to register SI
            }
        };

        $.pnCheckout(configJson);

        if(configJson.features.enableNewWindowFlow) {
            pnCheckoutShared.openNewWindow();
        }
    }

    var checkoutInterval = setInterval(() => {
        const data = $("#paynimoData").val();

        if(data !== "") {
            paynimoCheckout(data);
        }
    }, 500);
});