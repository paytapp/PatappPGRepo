import React, { Component } from 'react';
import loadable from '@loadable/component';

import "../../css/surchargePaymentPage.css";
import "../../css/style.css";
import "../../css/multilingual.css";
import { multilingualText } from '../../js/multilingualText';

import { startWorker, stopWorker, isValidVpaOnFocusOut, addConvenienceFee, wrapperPosition, adjustOrderSummaryWidth } from '../../js/script';

const Loader = loadable(() => import('../Loader/Loader'));
const ErrorPage = loadable(() => import('../ErrorPage/ErrorPage'));
const PageData = loadable(() => import('../PageData/PageData'));

class Main extends Component {
    constructor(props) {
        super(props);
  
        this.state = {
            data : null,
            paymentType : null,
            activeNavId : 0,
            walletMopType : null,
            isWalletActive: false,
            links: ["SC", "CARD", "UPI_MERGED", "MQR", "NB", "WL", "CD", "CR", "EM", "AP"],
            walletObj: {
                mobikwik_ActiveWl_state: "",
                mobikwik_isActive_state: false,
                mobikwik_errorMobile_state: null,
                mobikwik_isOtpActive_state: false,
                mobikwik_otpMsgSent_state: null,
                mobikwik_showResendOtp_state: false,
                mobikwik_errorOtp_state: null,
                mobikwik_startTimer_state: false,
                mobikwik_showOtpTimer_state: false,
                mobikwik_otpTimer_state: "00:00",
                walletLoggedNumber: "",
                walletMobileDisabled: false,                
                loadBalance: false,
                walletBalance: null,
                walletSufficientBalance: false,
                wallet_mobile_isActive: false,
                walletOtp: "",
                walletMobile: "",
                walletToken: "",
                paymentFlow: "NONE"
            },
            loader: {
                showLoader: false,
                defaultText: false,
                approvalNotification: false
            },
            displayError: false
        };
    }
    
    componentDidMount() {
        try {
            try {
                let responseJson = JSON.parse(window.id("sessionObj").value),
                    pageData = JSON.parse(responseJson.suportedPaymentTypeMap),
                    userData = responseJson.userData;

                delete responseJson.suportedPaymentTypeMap;
                delete responseJson.userData;

                const dataObj = {...responseJson, ...pageData, ...userData};
                
                this.setState({ data : dataObj });
                window.pageInfoObj = dataObj;
            } catch(err) {
                this.setState({data: "error"});
                console.error(err);
            }
        } catch(err) {
            this.setState({data: "error"});
            console.error(err);
        }

        window.addEventListener("resize", this.resizeHandler);
    }

    componentWillUnmount() {
        window.removeEventListener("resize", this.resizeHandler);
    }

    resizeHandler = () => {
        wrapperPosition();
    }

    startTimerHandler = _ => {
        let sec = 50;

        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                mobikwik_showOtpTimer_state: true
            }
        }));
    
        window.otpInterval = setInterval(() => {
            secpass();
        }, 1000);
    
        const secpass = _ => {
            let min     = Math.floor(sec / 60),
                remSec  = sec % 60;
            
            if (remSec < 10) {
                remSec = '0' + remSec;
            }
    
            if (min < 10) {
                min = '0' + min;        
            }      

            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    mobikwik_otpTimer_state: min + ":" + remSec
                }
            }));
            
            if (sec > 0) {            
                sec = sec - 1;            
            } else {
                clearInterval(window.otpInterval);

                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        mobikwik_showResendOtp_state: true,
                        mobikwik_otpTimer_state: "00:00",
                        mobikwik_showOtpTimer_state: false
                    }
                }));
            }
        }
    }
    
    getPaymentType = (id, e) => {
        let that = e.target.tagName === "BUTTON" ? e.target : e.target.closest("button");

        this.setState({paymentType: that.getAttribute("data-type")});
        
        this.setState({activeNavId: id});
    }

    
    submitUPIResponseForm(myMap) {
        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                showLoader: false,
                approvalNotification: false
            }
        }));
    
        let form = window.id("upiResponseForm");
        form.innerHTML = "";
        form.action = myMap.RETURN_URL;
    
        if (myMap.encdata) {
            form.innerHTML += ('<input type="hidden" name="encdata" value="' + myMap.encdata + '">');
        } else {
            for (let key in myMap) {
                if(key !== "RETRY_FLAG") {
                    form.innerHTML += ('<input type="hidden" name="' + key + '" value="' + myMap[key] + '">');
                }
            }
        }
    
        if(myMap["RETRY_FLAG"] === undefined) {
            window.id("upiResponseForm").submit();
        } else if(myMap["RETRY_FLAG"] === "Y") {
            window.querySelector(".retry-popup").classList.remove("d-none");
            window.id("returnMerchantAll").classList.add("d-none");
            window.id("returnMerchantUPI").classList.remove("d-none");
        }
    }

    upiSubmitAPI = obj => {
        let pgRefNum = "",
            responseCode = "",
            myMap = "",
            transactionStatus;
    
        if (null != obj) {
            transactionStatus = obj.transactionStatus;
            pgRefNum = obj.pgRefNum;
            responseCode = obj.responseCode;
            
            myMap = JSON.parse(obj.responseFields);
        }
    
        if (responseCode === "000") {
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    defaultText: false,
                    approvalNotification: true
                }
            }));
    
        	if (transactionStatus === "Sent to Bank") {
                startWorker({
                    pgRefNum: pgRefNum,
                    requestType: "UPI"
                });                
        	} else {
        		this.submitUPIResponseForm(myMap);
        	}
        } else if (responseCode === "366" || responseCode === "U17" || responseCode === "047" || responseCode === "110" || responseCode === "111" || responseCode === "148" || responseCode === "149") {
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    showLoader: false,
                    approvalNotification: false
                }
            }));

        	window.id('red1').style.display = "block";
        	window.id('vpaCheck').classList.add("redLine");
        	window.id('pay-now').classList.add("btn-disabled");
            
        	return false;
        } else if(responseCode === "031") {
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    defaultText: true
                }
            }));
    
        	document.getElementById('upiRedirectForm').action = `${window.basePath}/jsp/upiRedirect`;
        	document.getElementById('upiRedirectForm').submit();
        } else {
        	this.submitUPIResponseForm(myMap);
        }
    }
    
    upiSubmit = payload => {
        fetch(`${window.basePath}/jsp/upiPay`, {
            method : "POST",
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }      
        })
        .then((response) => response.json())
        .then((responseJson) => {
            this.upiSubmitAPI(responseJson);
        })
        .catch((error) => {
            console.error(error);
        });
    }

    submitUpiForm = _ => {
        let upiNumberProvided,
            paymentType = "UP",
            mopType = "UP",
            amount = (Number(window.pageInfoObj.surcharge_up) + (Number(window.pageInfoObj.AMOUNT) / 100)).toFixed(2);
    
        if(window.querySelector(".saveVpaDetails.active") !== null) {            
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    showLoader: true,
                    defaultText: true
                }
            }));
    
            let activeSavedVpa = window.querySelector(".saveVpaDetails.active");
    
            upiNumberProvided = activeSavedVpa.querySelector(".payerAddress").value;
    
            window.id('pay-now').classList.add("btn-disabled");
    
            this.upiSubmit({
                "vpa": upiNumberProvided,
                "paymentType": paymentType,
                "mopType": mopType,
                "amount": amount,
                "currencyCode": window.pageInfoObj.currencyCode,
                "vpaSaveflag": false
            });
        } else {
            if (window.id('vpaCheck').classList.contains('redLine')) {
                return false;
            } else if (isValidVpaOnFocusOut()) {
                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true
                    }
                }));
                
                upiNumberProvided = window.id("vpaCheck").value;
    
                var vpaSaveflag = window.id("vpaSaveFlag").value;
                window.id('pay-now').classList.add("btn-disabled");
    
                this.upiSubmit({
                    "vpa": upiNumberProvided,
                    "paymentType": paymentType,
                    "mopType": mopType,
                    "amount": amount,
                    "currencyCode": window.pageInfoObj.currencyCode,
                    "vpaSaveflag": vpaSaveflag
                });
            } else {
                isValidVpaOnFocusOut();
            }
        }
    }

    // SUBMIT FORM
    submitHandler = e => {
        e.preventDefault();
        
        let activeLi = window.querySelector("#navigation .tabLi.active"),
            _dataId = activeLi.getAttribute("data-id"),
            _dataType = activeLi.getAttribute("data-type");

        if(_dataType !== "UP" && _dataType !== "UPI_QR" && _dataType !== "SC" && _dataType !== "EM") {
            window.id(_dataId).querySelector(".btn-payment").click();
        }

        if(_dataType === 'EM') {
            let paymentType = window.id("emiPaymentType").value;
            let issuerBank = window.id("emiBankName");
            let emiRow = window.querySelector("li.row-emi-detail.active");

            if(paymentType === '') {
                window.id("error-emiPaymentType").classList.remove("d-none");
            } else if(issuerBank !== null && issuerBank.value === '') {
                window.id("error-emiBankName").classList.remove("d-none");
            } else if(emiRow === null) {
                window.id("error-emi-detail").classList.remove("d-none");
            } else if(emiRow !== null) {
                window.id(_dataId).querySelector(".btn-payment").click();
            }
        }

        if(_dataType === "UP") {
            this.submitUpiForm();
        }

        if(_dataType === "SC") {
        	let isVpaActive = window.querySelector(".saveVpaDetails.active"),
        		isNbActive = window.querySelector(".saveNbDetails.active"),
                isWlActive = window.querySelector(".saveWlDetails.active");
            
            if(isVpaActive !== null) {
        		this.submitUpiForm();
        	} else if(isNbActive !== null) {
                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true,
                        defaultText: true
                    }
                }));

        		window.id("save-netbanking-form").submit();
        	} else if(isWlActive !== null) {
        		window.id("save-wallet-form").submit();
        	}
        }
    }

    generateFrame = () => {
        let popupOverlay = window.createElement("div");
            popupOverlay.setAttribute("id", "checkout-popup-overlay");
        
        let popupInner = window.createElement("div");
            popupInner.setAttribute("id", "checkout-popup-inner");
    
        let checkoutLoader = window.createElement("div");
            checkoutLoader.setAttribute("id", "checkout-loader");
        
        let loaderImg = window.createElement("img");
            loaderImg.setAttribute("src", `${window.basePath}/img/loader.gif`);
        
        checkoutLoader.appendChild(loaderImg);
        popupOverlay.appendChild(checkoutLoader);
    
        let _frame = window.createElement("iframe");
        _frame.setAttribute("id", "add-and-pay");
        _frame.setAttribute("name", "add-and-pay");
        _frame.setAttribute("frameborder", 0);
        _frame.setAttribute("allowpaymentrequest", true);
    
        popupInner.appendChild(_frame);
    
        popupOverlay.appendChild(popupInner);
    
        document.querySelector("body").appendChild(popupOverlay);
    }

    iframeAction = status => {
        if(status.showIframe) {
            document.querySelector("body").classList.add("add-and-pay--active");
        } else {
            document.querySelector("body").classList.remove("add-and-pay--active");
        }
    }

    closeIframe = function() {
        let _body = document.querySelector("body");
        _body.classList.remove("checkout-popup--active");

        this.iframeAction({showIframe: false});

        window.id("checkout-popup-overlay").remove();
    }

    createFormInput = obj => {
        const inputField = document.createElement("input");
        inputField.type = "hidden";
        inputField.name = obj.name;
        inputField.value = obj.value;

        return inputField;
    }

    paytmHandler = () => {                        
        window.id("wallet-form").setAttribute("target", "add-and-pay");

        window.id("wallet-form").setAttribute("action", "addAndPayRequest");
        document.querySelector("body").classList.add("checkout-popup--active");

        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                showLoader: true,
                defaultText: false
            }
        }));

        window.addEventListener("message", e => {
            let obj = e.data;
        
            if(obj.isPageVisible) {
                this.iframeAction({showIframe: true});

                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: false,
                        defaultText: false
                    }
                }));
            } else if(obj.closeIframe) {
                // this.setState(prevState => ({
                //     loader: {
                //         ...prevState.loader,
                //         showLoader: true,
                //         defaultText: false
                //     }
                // }));

                this.closeIframe();

                // window.location.reload();
            } else if(obj.PAYMENT_FlOW === "ADDANDPAY") {
                window.id("checkout-popup-overlay").remove();
                const responseForm = window.id("response-form");

                Object.keys(obj).forEach(key => {
                    if(key !== "PAYMENT_FlOW") {
                        const result = this.createFormInput({name: key, value: obj[key]});
                        responseForm.appendChild(result);
                    }
                });

                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true
                    }
                }));

                responseForm.setAttribute("action", this.state.data.RETURN_URL);
                responseForm.submit();
            } else if(obj.txncancel) {
                const result = this.createFormInput({name: "encSessionData", value: obj.encSessionData});
                window.id("cancel-form").appendChild(result);
                this.cancelHandler();
            }
        });

        this.generateFrame();
    }

    validateMobikwik = () => {
        var isSubmitDisabled = window.id("pay-now").classList.contains("btn-disabled");

        if(this.state.walletObj.walletLoggedNumber === "") {
            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    mobikwik_errorMobile_state: "Please enter mobile number"
                }
            }));            

            return false;
        } else if(this.state.walletObj.walletLoggedNumber.length !== 10) {
            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    mobikwik_errorMobile_state: "Invalid mobile number"
                }
            }));            
            
            return false;
        } else if(this.state.walletObj.mobikwik_errorMobile_state === null) {
            if(this.state.walletObj.walletOtp === "") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        mobikwik_otpMsgSent_state: null,
                        mobikwik_errorOtp_state: "Please enter valid OTP"
                    },
                    loader: {
                        ...prevState.loader,
                        showLoader: false
                    }
                }));
                
                return false;
            } else if(this.state.walletObj.mobikwik_errorOtp_state === "" && !isSubmitDisabled) {
                if(!this.state.isWalletActive) {
                    return false;
                } else {
                    let payBtnKey = window.id("payBtnKey").getAttribute("data-key");

                    if(payBtnKey === "payBtnPaytm") {
                        this.paytmHandler();

                        return true;
                    } else {
                        this.setState(prevState => ({
                            loader: {
                                ...prevState.loader,
                                showLoader: true,
                                defaultText: true
                            }
                        }));
    
                        return true;
                    }                    
                }
            } else {
                return false;
            }		
        } else {            
            return false;
        }
    }

    validateForm = (e, mopType, elementId) => {
        var _mopType = window.id(mopType).value,
            isMobikwik = _mopType === "MobikwikWallet" ? true : false,
            isMobikwikWallet = window.pageInfoObj.IS_MOBIKWIK_WALLET,
            mobikwikWalletPayId = window.pageInfoObj.MOBIKWIK_WALLET_PAY_ID,
            isPaytm = _mopType === "PaytmWallet" ? true : false,
            isPaytmWallet = window.pageInfoObj.IS_PAYTM_WALLET,
            paytmWalletPayId = window.pageInfoObj.PAYTM_WALLET_PAY_ID;
    
        if(_mopType !== "") {
            if(window.id(elementId) !== null) {
                window.id(elementId).classList.remove("redLine");
            }
            
            window.id("error-" + elementId).style.display = "none";

            this.setState({displayError: false});
    
            if(e.target.getAttribute("id") === "wallet-form") {
                if((isMobikwik && isMobikwikWallet === "Y") || (isPaytm && isPaytmWallet === "Y")) {
                    if(!this.validateMobikwik()) {
                        e.preventDefault();
                    }
                } else if((isMobikwik && isMobikwikWallet === "N") || (isPaytm && isPaytmWallet === "N")) {
                    if((isMobikwik && mobikwikWalletPayId.indexOf(window.pageInfoObj.PAY_ID) > -1) || (isPaytm && paytmWalletPayId.indexOf(window.pageInfoObj.PAY_ID) > -1)) {
                        if(!this.validateMobikwik()) {
                            e.preventDefault();
                        }
                    } else {                        
                        this.setState(prevState => ({
                            loader: {
                                ...prevState.loader,
                                showLoader: true,
                                defaultText: true
                            }
                        }));
                        return true;                        
                    }
                } else {
                    this.setState(prevState => ({
                        loader: {
                            ...prevState.loader,
                            showLoader: true,
                            defaultText: true
                        }
                    }));

                    return true;
                }
            } else {
                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true,
                        defaultText: true
                    }
                }));

                return true;
            }
        } else {
            if(window.id(elementId) !== null) {
                window.id(elementId).classList.add("redLine");
            }
    
            if(window.id("error-" + elementId) !== null) {
                window.id("error-" + elementId).style.display = "block";
            }

            this.setState({displayError : true});

            e.preventDefault();
        }
    }

    activateMobikwikWallet = that => {
        let _screenSize = window.innerWidth,
            _walletSubmit = window.id("pay-now");

        _walletSubmit.classList.add("btn-disabled");

        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                mobikwik_isActive_state: true,
                wallet_mobile_isActive: true
            }
        }));

        window.id("wallet").querySelector(".tabbox-inner").classList.remove("custom-scroll");

        if(_screenSize < 768) {
            that.closest(".tabBox").closest("li").scrollIntoView({
                behavior: 'smooth'
            });
        }
    }

    walletSelectHandler = event => {
        let that = event.target.tagName !== "LABEL" ? event.target.closest(".bank_list_label") : event.target;

        if(!that.classList.contains("active")) {
            let _labelInput = that.querySelector(".custom-control-input"),
                _walletSubmit = window.id("pay-now");

            if(_labelInput.checked) {
                let _mopType = _labelInput.value,
                    wlList = window.id("wallet-list").querySelectorAll(".custom-control-label"),
                    isMobikwik = _mopType === "MobikwikWallet" ? true : false,
                    isMobikwikWallet = window.pageInfoObj.IS_MOBIKWIK_WALLET,
                    mobikwikWalletPayId = window.pageInfoObj.MOBIKWIK_WALLET_PAY_ID,
                    isPaytm = _mopType === "PaytmWallet" ? true : false,
                    isPaytmWallet = window.pageInfoObj.IS_PAYTM_WALLET,
                    paytmWalletPayId = window.pageInfoObj.PAYTM_WALLET_PAY_ID;

                wlList.forEach(element => {
                    let walletBoxLabel = element.getAttribute("for");
                    
                    element.classList.remove("active");

                    if((isMobikwik && isMobikwikWallet === "Y") || (isPaytm && isPaytmWallet === "Y")) {
                        if(_mopType !== walletBoxLabel) {
                            element.closest(".bankList").classList.add("d-none");
                        }
                    } else if((isMobikwik && isMobikwikWallet === "N") || (isPaytm && isPaytmWallet === "N")) {
                        if((isMobikwik && mobikwikWalletPayId.indexOf(window.pageInfoObj.PAY_ID) > -1) || (isPaytm && paytmWalletPayId.indexOf(window.pageInfoObj.PAY_ID) > -1)) {
                            if(_mopType !== walletBoxLabel) {
                                element.closest(".bankList").classList.add("d-none");
                            }                            
                        }
                    }
                });
        
                if((isMobikwik && isMobikwikWallet === "Y") || (isPaytm && isPaytmWallet === "Y")) {
                    this.activateMobikwikWallet(that);
                } else if((isMobikwik && isMobikwikWallet === "N") || (isPaytm && isPaytmWallet === "N")) {
                    if((isMobikwik && mobikwikWalletPayId.indexOf(window.pageInfoObj.PAY_ID) > -1) || (isPaytm && paytmWalletPayId.indexOf(window.pageInfoObj.PAY_ID) > -1)) {
                        this.activateMobikwikWallet(that);
                    } else {
                        _walletSubmit.classList.remove("btn-disabled");
                    }
                } else {
                    _walletSubmit.classList.remove("btn-disabled");
                }
            }

            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    mobikwik_ActiveWl_state: that.getAttribute("for"),
                }
            }));
        }
    }

    tabHandler = (id, e) => {
        e.preventDefault();
        
        let that = e.target.tagName === "BUTTON" ? e.target : e.target.closest("button");
    
        let paymentType = that.getAttribute("data-type"),
            isSpanClicked = that.classList.contains("tab-span"),
            isAnchorClicked = that.classList.contains("tabLi");
    
        if(isSpanClicked || isAnchorClicked) {
            let tabDataId = that.getAttribute("data-id"),
                tabId = that.getAttribute("id");
    
            if(!that.classList.contains("active")) {
                let tabLi = document.getElementsByClassName('tabLi');

                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        mobikwik_ActiveWl_state: "",
                        mobikwik_isActive_state: false,
                        mobikwik_errorMobile_state: null,
                        mobikwik_isOtpActive_state: false,
                        mobikwik_otpMsgSent_state: null,
                        mobikwik_showResendOtp_state: false,
                        mobikwik_errorOtp_state: null,
                        mobikwik_startTimer_state: false,
                        mobikwik_showOtpTimer_state: false,
                        mobikwik_otpTimer_state: "00:00",
                        walletLoggedNumber: "",
                        walletMobileDisabled: false,
                        loadBalance: false,
                        walletBalance: null,
                        walletSufficientBalance: false,
                        wallet_mobile_isActive: false,
                        walletOtp: "",
                        paymentFlow: "NONE"
                        // walletToken: "",
                    }
                }));

                clearInterval(window.otpInterval);
                clearInterval(window.addMoneyTimer);
    
                let _currentLi = window.querySelector(".active");
                if(_currentLi !== null) {
                    let _currentDataId = _currentLi.getAttribute("data-type");
        
                    if(_currentDataId === "UPI_QR" || _currentDataId === "UP") {                        
                        stopWorker();
                    }
                }
        
                // REMOVE ACTIVE CLASS FROM ALL TAB
                for (let j = 0; j < tabLi.length; j++) {
                    tabLi[j].classList.remove("active");
                }
            
                // SET ACTIVE CLASS TO CURRENT TAB
                that.classList.add("active");
    
                window.querySelector("body").setAttribute("data-navigation", tabDataId);
                
                addConvenienceFee(paymentType);
                wrapperPosition();
                adjustOrderSummaryWidth();                
    
                if(window.id("pay-now") !== null) {
                    window.id("pay-now").classList.add("btn-disabled");
                    window.id("pay-now").classList.remove("d-none");
                    window.id("payBtnKey").setAttribute("data-key", "payBtnText");
                }
            }
    
            window.id(tabId).scrollIntoView({
                behavior: 'smooth'
            });
        }
    }

    resetErrorHandler = event => {
        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                mobikwik_errorMobile_state: null
            }
        }));
    }

    veryfyUser = (obj, that) => {
        if(obj.response === "Success") {
            if(obj.token !== undefined && obj.token !== null && obj.token !== "") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        walletToken: obj.token
                    }
                }));                
            }

            this.mobikwikOtpHandler();
        } else {
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    showLoader: false
                },
                walletObj: {
                    ...prevState.walletObj,
                    mobikwik_errorMobile_state: obj.responseMsg,
                }
            }));
        }
    }

    verifyUserHandler = evt => {
        let that = evt.target,
            phoneNumber = that.value;
    
        if(phoneNumber.length === 10) {
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    showLoader: true
                },
                walletObj: {
                    ...prevState.walletObj,
                    walletLoggedNumber: phoneNumber
                }
            }));

            if(this.state.walletObj.walletToken !== "" && window.id("wallet-moptype").value === window.walletToCompare) {
                // payload["token"] = this.state.walletObj.walletToken;

                this.mobikwikOtpHandler(evt, phoneNumber);
            } else {
                let payload = {
                    phoneNo : phoneNumber,
                    payId : window.pageInfoObj.PAY_ID,
                    walletName : window.id("wallet-moptype").value
                }
                
                fetch(`${window.basePath}/jsp/verifyUser`, {
                    method : 'POST',
                    body: JSON.stringify(payload),
                    headers : {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    }
                })
                .then((response) => response.json())
                .then((responseJson) => {
                    this.veryfyUser(responseJson, that);
                })
                .catch((error) => {
                    console.error(error);
                });
            }
        }
    }

    getTotalAmount = _ => {
        let _totalAmount = window.id("totalAmount").querySelector(".value-block").innerHTML;
        _totalAmount = Number(_totalAmount).toFixed(2);
        return _totalAmount;
    }

    mobikwikOtpHandler = (e, phoneNumber) => {
        if(e !== undefined) {
            e.preventDefault();
        }

        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                showLoader: true
            }
        }));

        let payload = {
            phoneNo: this.state.walletObj.walletLoggedNumber !== "" ? this.state.walletObj.walletLoggedNumber : phoneNumber,
            payId: window.pageInfoObj.PAY_ID,
            totalAmount : this.getTotalAmount(),
            walletName : window.id("wallet-moptype").value
        }

        if(this.state.walletObj.walletToken !== "" && window.id("wallet-moptype").value === window.walletToCompare) {
            payload["token"] = this.state.walletObj.walletToken;
        }

        fetch(`${window.basePath}/jsp/sendOtp`, {
            method : 'POST',
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        .then((response) => response.json())
        .then((data) => {
            if(data.response === "Success") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        mobikwik_isOtpActive_state: true,
                        walletMobileDisabled: true,
                        mobikwik_otpMsgSent_state: data.responseMsg,
                        mobikwik_showResendOtp_state: false,
                        mobikwik_errorOtp_state: null
                    }
                }));
                
                this.startTimerHandler();
            } else if(data.response === "Failed") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        mobikwik_errorMobile_state: data.responseMsg
                    }
                }));
            }

            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    showLoader: false
                }
            }));
        })
        .catch((error) => {
            console.error(error);
        });
    }    

    activateSubmitBtn = obj => {
        if(Number(obj.walletBalance) < Number(this.getTotalAmount())) {
            if(window.id("wallet-moptype").value === window.walletToCompare) {
                window.id("pay-now").classList.remove("btn-disabled");

                window.id("payBtnKey").setAttribute("data-key", "payBtnPaytm");
                window.id("payBtnKey").innerHTML = multilingualText(window.id("translate").value, "payBtnPaytm");
                
                this.setState(prevState => ({
                    isWalletActive: true,
                    walletObj: {
                        ...prevState.walletObj,
                        paymentFlow: "ADDANDPAY"
                    }
                }));
            } else {
                window.id('pay-now').classList.add("btn-disabled");    
                this.setState({isWalletActive: false});
            }
        } else {
            window.id('pay-now').classList.remove("btn-disabled");

            this.setState(prevState => ({
                isWalletActive: true,
                walletObj: {
                    ...prevState.walletObj,
                    paymentFlow: "NONE"
                }
            }));
        }
    }

    loadWalletBalanceHandler = (obj, event) => {
        if(event !== undefined) {
            event.preventDefault();
        }

        if(obj.isOtpVerified && ((this.state.walletObj.walletSufficientBalance && window.id("wallet-moptype").value !== window.walletToCompare) || this.state.walletObj.loadBalance)) {
            this.fetchWalletBalance(obj);
        } else if(!obj.isOtpVerified && ((!this.state.walletObj.walletSufficientBalance && window.id("wallet-moptype").value !== window.walletToCompare) || !this.state.walletObj.loadBalance)) {
            this.fetchWalletBalance(obj);
        }
    }

    fetchWalletBalance = (obj) => {
        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                showLoader: true
            }
        }));        
    
        let payload = {
            payId : window.pageInfoObj.PAY_ID,
            phoneNo : obj.walletLoggedNumber,
            otp : obj.walletOtp,
            walletName : window.id("wallet-moptype").value,
            isOtpVerified: obj.isOtpVerified
        }

        if(this.state.walletObj.walletToken !== "") {
            payload["token"] = this.state.walletObj.walletToken;
        }

        fetch(`${window.basePath}/jsp/checkBalance`, {
            method : "POST",
            body: JSON.stringify(payload),
            headers : { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        .then((response) => response.json())
        .then((data) => {
            if(data.response === "Success") {
                clearInterval(window.otpInterval);

                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        walletOtp: obj.walletOtp,
                        walletBalance: Number(data.walletBalance).toFixed(2),
                        walletSufficientBalance: Number(data.walletBalance) < Number(this.getTotalAmount()),
                        loadBalance: true,
                        mobikwik_isOtpActive_state: false,
                        wallet_mobile_isActive: false,
                    }
                }));
                
				this.activateSubmitBtn(data);
            } else if(data.response === "Failed" && data.responseMsg === "OTP mismatch or invalid") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        mobikwik_errorOtp_state: data.responseMsg,
                        mobikwik_otpMsgSent_state: null,
                        walletOtp: ""
                    }
                }));
            } else {
                if(data.responseMsg !== undefined) {
                    alert(data.responseMsg);
                } else {
                    alert("Something went wrong! Try again.");
                }

                this.editWalletNumberHandler('edit');
            }

            if(obj.hideLoader) {
                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: false
                    }
                }));
            }
        })
        .catch((error) => {
            console.error(error);
        });
    }

    editWalletNumberHandler = (action, e) => {
        if(e !== undefined) {
            e.preventDefault();
        }

        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                mobikwik_errorMobile_state: null,
                mobikwik_isOtpActive_state: false,
                mobikwik_otpMsgSent_state: null,
                mobikwik_showResendOtp_state: false,
                mobikwik_errorOtp_state: null,
                mobikwik_startTimer_state: false,
                mobikwik_showOtpTimer_state: false,
                mobikwik_otpTimer_state: "00:00",
                walletLoggedNumber: "",
                walletMobile: "",
                walletMobileDisabled: false,
                loadBalance: false,
                walletBalance: null,
                walletSufficientBalance: false,
                wallet_mobile_isActive: true,
                walletOtp: ""
            }
        }));

        if(window.id("pay-now") !== null) {
            window.id("pay-now").classList.add("btn-disabled");
            window.id("pay-now").classList.remove("d-none");
            window.id("payBtnKey").setAttribute("data-key", "payBtnText");
            window.id("payBtnKey").innerHTML = multilingualText(window.id("translate").value, "payBtnText");
        }

        clearInterval(window.otpInterval);
    }

    cancelHandler = e => {
        if(e !== undefined) {
            e.preventDefault();
        }

        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                showLoader: true
            }
        }));

        window.id("cancel-form").submit();
    }

    loaderHandler = obj => {
        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                ...obj
            }
        }));
    }

    updateMobikwikState = obj => {
        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                ...obj
            }
        }));
    }

    // updateAvailableToken = (obj) => {
    //     if(!this.state.data.vpaTokenAvailable && !this.state.data.nbTokenAvailable && !this.state.data.wlTokenAvailable) {
            
    //     }
    // }

    deleteHandler = (key, action, confirmText, checkBoxId, tokenName, tokenAvailableName, event) => {
        event.preventDefault();
    
        if (window.id(checkBoxId).checked) {
            if (window.confirm("Are you sure you want to delete saved " + confirmText + "??")) {
                this.loaderHandler({showLoader: true});

                const payload = {
                    "tokenId" : key
                };

                fetch(`${window.basePath}/jsp/${action}`, {
                    method : "POST",
                    body: JSON.stringify(payload),
                    headers : {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    }      
                })
                .then((response) => response.json())
                .then((responseJson) => {
                    let tokenObj = {
                        [tokenName] : responseJson[tokenName],
                        [tokenAvailableName] : responseJson[tokenName] !== "NA"
                    }

                    let dataObj = {...this.state.data, ...tokenObj};

                    this.setState({data : dataObj});

                    if(window.id('pay-now') !== null) {
                        window.id('pay-now').classList.add("btn-disabled");
                    }

                    this.setState({paymentType: window.querySelector(".tabLi").getAttribute("data-type")});
                    window.querySelector(".tabLi").classList.add("active");

                    this.loaderHandler({showLoader: false});
                })
                .catch((error) => {
                    console.error(error);
                });
            }
        }
    }

    render() {
        let mainContent = <Loader processing={false} approvalNotification={false} />;

        if(this.state.data !== null) {
            if(this.state.data === "error") {
                mainContent = <ErrorPage />
            } else {
                mainContent = <PageData
                    klass={this}
                    loader={this.state.loader}
                    walletObj={this.state.walletObj}
                    data={this.state.data}
                    links={this.state.links}
                    paymentType={this.state.paymentType}
                    tabHandler={this.tabHandler}
                    getPaymentType={this.getPaymentType}
                    walletSelectHandler={this.walletSelectHandler}
                    verifyUserHandler={this.verifyUserHandler}
                    mobikwikOtpHandler={this.mobikwikOtpHandler}
                    resetErrorHandler={this.resetErrorHandler}
                    startTimerHandler={this.startTimerHandler}
                    loadWalletBalanceHandler={this.loadWalletBalanceHandler}
                    editWalletNumberHandler={this.editWalletNumberHandler}
                    submitHandler={this.submitHandler}
                    cancelHandler={this.cancelHandler}
                    validateForm={this.validateForm}
                    loaderHandler={this.loaderHandler}
                    getTotalAmount={this.getTotalAmount}
                    updateMobikwikState={this.updateMobikwikState}
                    deleteHandler={this.deleteHandler}
                />
            }
        }

        return (
            <React.Fragment>
                { mainContent }
            </React.Fragment>
        );
    }
}

export default Main;
