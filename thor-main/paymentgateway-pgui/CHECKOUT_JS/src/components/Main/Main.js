import React, { PureComponent } from 'react';
import Header from '../Header/Header';
import Loader from '../Loader/Loader';
import Navigation from '../Navigation/Navigation';
import Cards from '../Cards/Cards';
import NetBanking from '../NetBanking/NetBanking';
import UpiSection from '../UpiSection/UpiSection';
import Wallet from '../Wallet/Wallet';
import CashOnDelivery from '../CashOnDelivery/CashOnDelivery';
import EMI from '../EMI/EMI';
import SavedVpa from '../QuickPay/SavedVpa';
import SavedNetBanking from '../QuickPay/SavedNetBanking';
import WalletLogin from '../Wallet/WalletLogin';
import WalletBalance from '../Wallet/WalletBalance';
import UpiQrBox from '../UpiSection/UpiQrBox';

import { isValidVpaOnFocusOut, startWorker } from '../../js/script';
import CryptoCurrency from '../CryptoCurrency/CryptoCurrency';
import AddPayCards from '../Cards/AddPayCards';
import AddPayNetBanking from '../NetBanking/AddPayNetBanking';

class Main extends PureComponent {
    constructor(props) {
        super(props);
  
        this.state = {
            data : null,
            isCheckoutJs: true,
            activeNavigation: null,
            activeQuickPayId: null,
            activeComponent: "navigation",
            paymentType: null,
            pageTitle: null,
            totalAmount: null,
            isActivePayBtn: false,
            isVisiblePayBtn: false,
            payBtnText: "Pay",
            selectedMopType : "",
            displayError: false,
            isWalletActive: false,
            links: ["SC", "CARD", "UPI_MERGED", "NB", "WL", "CD", "CR", "EM"],
            loader: {
                showLoader: false,
                defaultText: false,
                approvalNotification: false
            },
            walletObj: {
                wallet_ActiveWl_state: null,
                wallet_isActive_state: false,
                wallet_errorMobile_state: null,
                wallet_isOtpActive_state: false,
                wallet_otpMsgSent_state: null,
                wallet_showResendOtp_state: false,
                wallet_errorOtp_state: null,
                wallet_startTimer_state: false,
                wallet_showOtpTimer_state: false,
                wallet_otpTimer_state: "00:00",
                walletLoggedNumber: "",
                walletMobileDisabled: false,
                loadBalance: false,
                walletBalance: null,
                walletSufficientBalance: false,
                wallet_mobile_isActive: false,
                walletOtp: null,
                walletMobile: "",
                walletToken: "",
                paymentFlow: "NONE"
            },
            loggedMerchant: {
                kashiMerchant: false,
                blueWhaleMerchant: false,
                paybleMerchant: false,
                payTenseMerchant: false,
                smtSuperMerchant: false,
            }
        }
    }

    componentDidMount() {
        try {
            try {
                if(window.name == "add-and-pay") {
                    this.setState({ activeComponent: "addAndPay" });

                    this.setState({ isCheckoutJs: false });

                    // GETTING DATA FROM PARENT WINDOW
                    let dataObj = this.dataHandler(window.parent.document.getElementById("sessionObj").value);

                    // GETTING DATA FROM CURRENT WINDOW
                    let responseJson = JSON.parse(Window.id("sessionObj").value);
                    
                    dataObj = {...dataObj, ...responseJson};
                    this.setState({ data : dataObj });
                    Window.pageInfoObj = dataObj;
                } else {
                    const dataObj = this.dataHandler(Window.id("sessionObj").value);

                    this.setState({ data : dataObj });
                    Window.pageInfoObj = dataObj;
                }

                window.parent.postMessage({"isPageVisible" : true}, "*");
            } catch(err) {
                this.setState({data: "error"});
                console.error(err);
            }
        } catch(err) {
            this.setState({data: "error"});
            console.error(err);
        }
    }

    dataHandler = jsonId => {
        let responseJson = JSON.parse(jsonId),
            pageData = JSON.parse(responseJson.suportedPaymentTypeMap),
            userData = responseJson.userData;

        delete responseJson.suportedPaymentTypeMap;
        delete responseJson.userData;

        const dataObj = {...responseJson, ...pageData, ...userData};

        return dataObj;
    }
    

    // NAVIGATION HANDLER
    navigationHandler = event => {
        let that = event.target.tagName !== "BUTTON" ? event.target.closest("button") : event.target,
            paymentType = that.getAttribute("data-type"),
            dataId = that.getAttribute("data-id");

        this.setState(prevState => ({
            activeComponent: dataId,
            paymentType: paymentType,
            isActivePayBtn: false,
            selectedMopType: "",
            displayError: false,
            activeQuickPayId: null,
            payBtnText: "Pay",
            walletObj: {
                ...prevState.walletObj,
                walletLoggedNumber: "",
                walletMobile: "",
                walletOtp: null,
                walletBalance: null,
                walletSufficientBalance: false,
                walletMobileDisabled: false,
                wallet_ActiveWl_state: null,
                wallet_isActive_state: false,
                wallet_errorMobile_state: null,
                wallet_isOtpActive_state: false,
                wallet_otpMsgSent_state: null,
                wallet_showResendOtp_state: false,
                wallet_errorOtp_state: null,
                wallet_startTimer_state: false,
                wallet_showOtpTimer_state: false,
                wallet_otpTimer_state: "00:00",
                walletToken: "",
                paymentFlow: "NONE"
            }
        }));

        clearInterval(Window.addMoneyTimer);
        clearInterval(Window.otpInterval);

        if(this.state.isCheckoutJs) {
            this.convenienceFeeHandler(paymentType);
        }

        if(paymentType === "UPI_QR") {
            this.setState({isVisiblePayBtn: false});
        } else {
            this.setState({isVisiblePayBtn: true});
        }
    }

    navStateHandler = e => {
        e.preventDefault();

        let that = e.target.tagName !== "BUTTON" ? e.target.closest("button") : e.target;

        this.setState({activeNavigation: that.getAttribute("data-id")});
    }

    // RESTRICT COPY PASTE IN INPUT FIELD
    restrictCopyPasteHandler = e => {
        e.preventDefault();
    }
    
    loaderHandler = (obj) => {
        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                ...obj
            }
        }));
    }

    convenienceFeeHandler = (paymentType) => {
        if(paymentType !== null) {
            Object.keys(Window.surchargeMopType).map(element => {
                if(element === paymentType && element === "CC") {
                    if(Window.id("mopTypeCCDiv2") !== null && Window.id("mopTypeCCDiv2").value !== "") {
                        if(window.id("mopTypeCCDiv2").value == "AX") {
                            this.setFee({
                                showGST: true,
                                surchargeAmt: Window.pageInfoObj.surcharge_cc_amex
                            });
                        } else {
                            this.setFee({
                                showGST: true,
                                surchargeAmt: Window.pageInfoObj[Window.surchargeMopType[[paymentType]][[Window.id("cardHolderTypeId").value]]]
                            });
                        }
                    } else {
                        this.setFee({
                            showGST: true,
                            surchargeAmt: Window.pageInfoObj.surcharge_cc_consumer
                        });
                    }                    
                } else if(element === paymentType && element === "DC") {
                    if(Window.id("mopTypeCCDiv2") !== null && Window.id("mopTypeCCDiv2").value !== "") {
                        this.setFee({
                            showGST: false,
                            surchargeAmt: Window.pageInfoObj[Window.surchargeMopType[[paymentType]][[Window.id("mopTypeCCDiv2").value]]]
                        });
                    } else {
                        this.setFee({
                            showGST: false,
                            surchargeAmt: Window.pageInfoObj.surcharge_dc_visa
                        });
                    }
                } else if(element === paymentType) {
                    this.setFee({
                        showGST: false,
                        surchargeAmt: Window.pageInfoObj[Window.surchargeMopType[paymentType]]
                    });
                }
            });
        } else {            
            this.setState({totalAmount: (this.state.data.AMOUNT / 100).toFixed(2)});
        }
    }
    
    setFee = obj => {
        let amount = Number(Window.pageInfoObj["AMOUNT"]) / 100,
            surchargeAmt = Number(obj.surchargeAmt),
            TOTAL_AMT = (amount + surchargeAmt).toFixed(2);

        this.setState({totalAmount: TOTAL_AMT});
    }

    numberInputHandler = evt => {
        let elementValue = evt.target.value;
    
        if (!(/^[0-9]+$/.test(elementValue))) {
            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    walletMobile: elementValue.replace(/[^0-9]/g, "")
                }
            }));
        } else {
            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    walletMobile: elementValue
                }
            }));
        }
    }

    submitBtnHandler = obj => {
        this.setState({isActivePayBtn: obj.active});
    }

    labelSelectHandler = e => {
        this.setState({
            selectedMopType: e.target.value,
            displayError: false,
            isActivePayBtn: true
        });
    };

    selectDefaultBankHandler = e => {
        let selectedValue = e.target.value;

        this.setState({selectedMopType: selectedValue});

        if(selectedValue !== "") {
            this.setState({
                displayError: false,
                isActivePayBtn: true
            });
        } else {
            this.setState({
                displayError: true,
                isActivePayBtn: false
            });
            
            e.target.blur();
        }
    }

    activeComponentHandler = label => {
        this.setState({activeComponent: label});
    }

    tokenHandler = () => {
        if(this.state.data.nbToken === "NA" && this.state.data.wlToken === "NA" && this.state.data.vpaToken === "NA") {
            this.setState({
                paymentType: null,
                activeComponent: "navigation"
            });
        } else {
            this.setState({
                paymentType: "SC",
                activeComponent: "quickPay"
            });
        }
    }

    startTimerHandler = _ => {
        let sec = 50;        

        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                wallet_showOtpTimer_state: true
            }
        }));        
    
        Window.otpInterval = setInterval(() => {
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
                    wallet_otpTimer_state: min + ":" + remSec
                }
            }));
            
            if (sec > 0) {            
                sec = sec - 1;            
            } else {
                clearInterval(Window.otpInterval);

                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        wallet_showResendOtp_state: true,
                        wallet_otpTimer_state: "00:00",
                        wallet_showOtpTimer_state: false
                    }
                }));
            }
        }
    }

    activateWalletLogin = _ => {
        this.setState(prevState => ({
            activeComponent: "walletLogin",
            walletObj: {
                ...prevState.walletObj,
                wallet_isActive_state: true
            }
        }));
    }

    // WALLET SELECT HANDLER
    walletSelectHandler = event => {
        let that = event.target.closest(".wallet-list");

        if(!that.classList.contains("active")) {
            let _labelInput = that.querySelector(".custom-control-input"),
                isMobikwik = _labelInput.value === "MobikwikWallet" ? true : false,
                isMobikwikWallet = Window.pageInfoObj.IS_MOBIKWIK_WALLET,
                mobikwikWalletPayId = Window.pageInfoObj.MOBIKWIK_WALLET_PAY_ID,
                isPaytm = _labelInput.value === "PaytmWallet" ? true : false,
                isPaytmWallet = Window.pageInfoObj.IS_PAYTM_WALLET,
                paytmWalletPayId = Window.pageInfoObj.PAYTM_WALLET_PAY_ID,
                selectedWallet = that.querySelector(".custom-control-label").getAttribute("for");

            this.setState(prevState => ({
                displayError: false,
                selectedMopType: selectedWallet,
                walletObj: {
                    ...prevState.walletObj,
                    wallet_ActiveWl_state: selectedWallet
                }
            }));            
    
            if((isMobikwik && isMobikwikWallet === "Y" || (isPaytm && isPaytmWallet === "Y"))) {
                this.activateWalletLogin();
            } else if((isMobikwik && isMobikwikWallet === "N") || (isPaytm && isPaytmWallet === "N")) {
                if((isMobikwik && mobikwikWalletPayId.indexOf(Window.pageInfoObj.PAY_ID) > -1) || (isPaytm && paytmWalletPayId.indexOf(Window.pageInfoObj.PAY_ID) > -1)) {
                    this.activateWalletLogin();
                } else {
                    this.setState({isActivePayBtn: true});
                }
            } else {
                this.setState({isActivePayBtn: true});
            }            
        }
    }

    quickPayHandler = (e) => {
        this.setState({
            activeQuickPayId: e.target.getAttribute("id"),
            selectedMopType: e.target.value,
            displayError: false,
            isActivePayBtn: true
        });
    }

    deleteHandler = (key, action, confirmText, checkBoxId, tokenName, tokenAvailableName, event) => {
        event.preventDefault();
    
        if(Window.id(checkBoxId).checked) {
            if(window.confirm("Are you sure you want to delete saved " + confirmText + "??")) {
                this.loaderHandler({showLoader: true});

                const payload = {
                    "tokenId" : key,
                    "encSessionData" : this.state.data.encSessionData,
                    "CHECKOUT_JS_FLAG" : this.state.data.checkOutJsFlag
                };

                fetch(`${Window.baseUrl}/${action}`, {
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

                    this.setState({isActivePayBtn: false});

                    this.loaderHandler({showLoader: false});
                })
                .catch((error) => {
                    console.error(error);
                });
            }
        }
    }

    updateAvailableToken = (obj) => {
        let dataObj = {...this.state.data, ...obj};

        this.setState({data : dataObj});
    }

    editWalletNumberHandler = (action, e) => {
        if(e !== undefined) {
            e.preventDefault();
        }

        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                walletLoggedNumber: "",
                walletMobile: "",
                walletOtp: null,
                walletBalance: null,
                walletSufficientBalance: false,
                walletMobileDisabled: false,
                wallet_errorMobile_state: null,
                wallet_isOtpActive_state: false,
                wallet_otpMsgSent_state: null,
                wallet_showResendOtp_state: false,
                wallet_errorOtp_state: null,
                wallet_startTimer_state: false,
                wallet_showOtpTimer_state: false,
                wallet_otpTimer_state: "00:00"
            }
        }));

        if(action !== "edit") {
            this.setState({activeComponent: "walletLogin"});
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

            let payload = {
                phoneNo : phoneNumber,
                payId : Window.pageInfoObj.PAY_ID,
                walletName : this.state.selectedMopType
            }

            fetch(`${Window.baseUrl}/verifyUser`, {
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
                    wallet_errorMobile_state: obj.responseMsg,
                }
            }));
        }
    }

    mobikwikOtpHandler = e => {
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
            phoneNo: this.state.walletObj.walletLoggedNumber,
            payId: Window.pageInfoObj.PAY_ID,
            totalAmount : this.getTotalAmount("surcharge_wl"),
            walletName: this.state.selectedMopType
        }

        if(this.state.walletObj.walletToken !== "") {
            payload["token"] = this.state.walletObj.walletToken;
        }

        fetch(`${Window.baseUrl}/sendOtp`, {
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
                        wallet_isOtpActive_state: true,
                        walletMobileDisabled: true,
                        wallet_otpMsgSent_state: data.responseMsg,
                        wallet_showResendOtp_state: false,
                        wallet_errorOtp_state: null
                    }
                }));
                
                this.startTimerHandler();
                
            } else if(data.response === "Failed") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        wallet_errorMobile_state: data.responseMsg
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

    resetErrorHandler = event => {
        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                wallet_errorMobile_state: null
            }
        }));
    }

    activateSubmitBtn = obj => {
        if(Number(obj.walletBalance) < this.state.totalAmount) {
            if(this.state.selectedMopType === Window.walletToCompare) {
                this.setState(prevState => ({
                    isActivePayBtn: true,
                    isWalletActive: true,
                    payBtnText: "Add & Pay",
                    walletObj: {
                        ...prevState.walletObj,
                        walletSufficientBalance: true,
                        paymentFlow: "ADDANDPAY"
                    }
                }));
            } else {
                this.setState(prevState => ({
                    isActivePayBtn: false,
                    isWalletActive: false,
                    walletObj: {
                        ...prevState.walletObj,
                        walletSufficientBalance: false
                    }
                }));
            }
        } else {
            this.setState(prevState => ({
                isActivePayBtn: true,
                isWalletActive: true,
                walletObj: {
                    ...prevState.walletObj,
                    walletSufficientBalance: true,
                    paymentFlow: "NONE"
                }
            }));
        }
    }

    updateMobikwikState = obj => {
        this.setState(prevState => ({
            walletObj: {
                ...prevState.walletObj,
                ...obj
            }
        }));
    }

    loadWalletBalanceHandler = (obj, event) => {
        if(event !== undefined) {
            event.preventDefault();
        }

        if(obj.isOtpVerified && this.state.activeComponent === "walletBalance") {
            this.fetchWalletBalance(obj);
        } else if(!obj.isOtpVerified && this.state.activeComponent !== "walletBalance") {
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
            payId : Window.pageInfoObj.PAY_ID,
            phoneNo : obj.walletLoggedNumber,
            otp : obj.walletOtp,
            walletName : this.state.selectedMopType,
            isOtpVerified: obj.isOtpVerified
        };

        if(this.state.walletObj.walletToken !== "") {
            payload["token"] = this.state.walletObj.walletToken;
        }

        fetch(`${Window.baseUrl}/checkBalance`, {
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
                clearInterval(Window.otpInterval);

                this.setState(prevState => ({
                    activeComponent: "walletBalance",
                    walletObj: {
                        ...prevState.walletObj,
                        walletOtp: obj.walletOtp,
                        walletBalance: Number(data.walletBalance).toFixed(2),
                        walletSufficientBalance: Number(data.walletBalance) < this.getTotalAmount("surcharge_wl"),
                        loadBalance: true,
                        wallet_isOtpActive_state: false,
                        wallet_mobile_isActive: false
                    }
                }));

                this.activateSubmitBtn(data);
            } else if(data.response == "Failed" && data.responseMsg == "OTP mismatch or invalid") {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        wallet_errorOtp_state: data.responseMsg,
                        wallet_otpMsgSent_state: null,
                        walletOtp: ""
                    }
                }));
            } else {
                alert(data.responseMsg);

                this.editWalletNumberHandler();
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

    upiIconHandler = (isActive) => {
        let arrowIcon = null;
        if(isActive == true || isActive == undefined) {
            arrowIcon = <i className="pg-icon icon-check-circle d-inline-block font-weight-bold font-size-20"></i>
        } else {
            arrowIcon = <i className="pg-icon icon-right-arrow d-inline-block font-weight-bold font-size-14"></i>                    
        }

        return (
            <span className="toggle-icon">
                {arrowIcon}
            </span>
        );
    }

    upiToggleListHandler = e => {
        e.preventDefault();

        let toggleList = e.target.closest(".toggle-list"),
            isActive = toggleList.classList.contains("active"),
            mopType = toggleList.getAttribute("data-type");

        if(!isActive) {
            this.setState({activeComponent: mopType});
        }

        if(mopType === "UPI_QR") {
            this.setState(prevState => ({
                activeComponent: "UPI_QR",
                isVisiblePayBtn: false,
                loader: {
                    ...prevState.loader,
                    showLoader: true
                }
            }));
        } else {        
            this.setState({isVisiblePayBtn: true});
        }
    }

    validateMobikwik = () => {
        if(this.state.walletObj.walletLoggedNumber === "") {
            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    wallet_errorMobile_state: "Please enter mobile number"
                }
            }));

            return false;
        } else if(this.state.walletObj.walletLoggedNumber.length !== 10) {
            this.setState(prevState => ({
                walletObj: {
                    ...prevState.walletObj,
                    wallet_errorMobile_state: "Invalid mobile number"
                }
            }));
            
            return false;
        } else if(this.state.walletObj.wallet_errorMobile_state === null) {
            if(this.state.walletObj.walletOtp === null) {
                this.setState(prevState => ({
                    walletObj: {
                        ...prevState.walletObj,
                        wallet_otpMsgSent_state: null,
                        wallet_errorOtp_state: "Please enter valid OTP"
                    },
                    loader: {
                        ...prevState.loader,
                        showLoader: false
                    }
                }));
                
                return false;
            } else if(this.state.walletObj.wallet_errorOtp_state === "" && this.state.isActivePayBtn) {
                if(!this.state.isWalletActive) {
                    return false;
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
                return false;
            }		
        } else {            
            return false;
        }
    }

    validateFormHandler = (e, mopType, elementId) => {
        let _mopType = this.state.selectedMopType,
            isMobikwik = _mopType === "MobikwikWallet" ? true : false,
            isMobikwikWallet = Window.pageInfoObj.IS_MOBIKWIK_WALLET,
            mobikwikWalletPayId = Window.pageInfoObj.MOBIKWIK_WALLET_PAY_ID,
            isPaytm = _mopType === "PaytmWallet" ? true : false,
            isPaytmWallet = Window.pageInfoObj.IS_PAYTM_WALLET,
            paytmWalletPayId = Window.pageInfoObj.PAYTM_WALLET_PAY_ID;
    
        if(_mopType !== "") {
            this.setState({displayError: false});
    
            if(e.target.getAttribute("id") == "wallet-form") {
                if((isMobikwik && isMobikwikWallet == "Y") || (isPaytm && isPaytmWallet == "Y")) {
                    if(!this.validateMobikwik()) {
                        e.preventDefault();
                    }
                } else if((isMobikwik && isMobikwikWallet == "N") || (isPaytm && isPaytmWallet == "N")) {
                    if((isMobikwik && mobikwikWalletPayId.indexOf(Window.pageInfoObj.PAY_ID) > -1) || (isPaytm && paytmWalletPayId.indexOf(Window.pageInfoObj.PAY_ID) > -1)) {
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

                        e.preventDefault();

                        this.submitInNewWindow(Window.id(e.target.getAttribute("id")));
                    }
                } else {
                    this.setState(prevState => ({
                        loader: {
                            ...prevState.loader,
                            showLoader: true,
                            defaultText: true
                        }
                    }));

                    e.preventDefault();

                    this.submitInNewWindow(Window.id(e.target.getAttribute("id")));
                }
            } else if(e.target.getAttribute("id") == "form-CD") {
                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true,
                        defaultText: true
                    }
                }));

                return true;
            } else {
                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true,
                        defaultText: true
                    }
                }));

                e.preventDefault();

                this.submitInNewWindow(Window.id(e.target.getAttribute("id")));
            }
        } else {
            this.setState({displayError : true});

            e.preventDefault();
        }
    }

    submitUPIResponseForm(myMap) {
        this.setState(prevState => ({
            loader: {
                ...prevState.loader,
                showLoader: false,
                approvalNotification: false
            }
        }));
    
        let form = Window.id("upiResponseForm");
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
            Window.id("upiResponseForm").submit();
        } else if(myMap["RETRY_FLAG"] === "Y") {
            window.querySelector(".retry-popup").classList.remove("d-none");
            Window.id("returnMerchantAll").classList.add("d-none");
            Window.id("returnMerchantUPI").classList.remove("d-none");
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
                isActivePayBtn: false,
                loader: {
                    ...prevState.loader,
                    showLoader: false,
                    approvalNotification: false
                }
            }));

        	Window.id('red1').style.display = "block";
        	Window.id('vpaCheck').classList.add("redLine");
            
        	return false;
        } else if(responseCode === "031") {
            this.setState(prevState => ({
                loader: {
                    ...prevState.loader,
                    defaultText: true
                }
            }));

            Window.id("upi-encSessionData").value = obj.encSessionData;    
        	Window.id('upiRedirectForm').action = `${Window.baseUrl}/upiRedirect`;

            this.submitInNewWindow(Window.id("upiRedirectForm"));
        } else {
        	this.submitUPIResponseForm(myMap);
        }
    }
    
    upiSubmit = payload => {
        fetch(`${Window.baseUrl}/upiPay`, {
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

    getTotalAmount = surcharge => {
        return (Number(Window.pageInfoObj[surcharge]) + (Number(Window.pageInfoObj.AMOUNT) / 100)).toFixed(2);
    }

    submitUpiForm = _ => {
        let upiNumberProvided,
            paymentType = "UP",
            mopType = "UP",
            amount = this.getTotalAmount("surcharge_up");
    
        if(this.state.activeNavigation === "savedVpa") {
            this.setState(prevState => ({
                isActivePayBtn: false,
                loader: {
                    ...prevState.loader,
                    showLoader: true,
                    defaultText: true
                }
            }));

            this.upiSubmit({
                "vpa": this.state.selectedMopType,
                "paymentType": paymentType,
                "mopType": mopType,
                "amount": amount,
                "currencyCode": Window.pageInfoObj.currencyCode,
                "vpaSaveflag": false,
                "CHECKOUT_JS_FLAG": this.state.data.checkOutJsFlag,
                "encSessionData": this.state.data.encSessionData
            });
        } else {
            if (Window.id('vpaCheck').classList.contains('redLine')) {
                return false;
            } else if (isValidVpaOnFocusOut()) {
                this.setState(prevState => ({
                    isActivePayBtn: false,
                    loader: {
                        ...prevState.loader,
                        showLoader: true,
                        defaultText: true
                    }
                }));
                
                upiNumberProvided = Window.id("vpaCheck").value;
    
                let vpaSaveflag = Window.id("vpaSaveFlag").value;
    
                this.upiSubmit({
                    "vpa": upiNumberProvided,
                    "paymentType": paymentType,
                    "mopType": mopType,
                    "amount": amount,
                    "currencyCode": Window.pageInfoObj.currencyCode,
                    "vpaSaveflag": vpaSaveflag,
                    "CHECKOUT_JS_FLAG": this.state.data.checkOutJsFlag,
                    "encSessionData": this.state.data.encSessionData
                });
            } else {
                isValidVpaOnFocusOut();
            }
        }
    }

    submitHandler = e => {
        e.preventDefault();

        let that = e.target.tagName === "SPAN" || e.target.tagName === "I" ? e.target.closest("button") : e.target;

        if(this.state.paymentType !== "UP" && this.state.paymentType !== "UPI_QR" && this.state.paymentType !== "SC") {
            Window.id(that.getAttribute("data-id")).querySelector(".btn-payment").click();
        }

        if(this.state.paymentType === "UP") {
            this.submitUpiForm();
        }
    }

    submitInNewWindow = that => {

        Window.windownTransactionComplete = false;

        this.loaderHandler({
            showLoader: true,
            processing: true
        });

        that.target = "actionWindow";

        let w=900, h=550;
        const dualScreenLeft = window.screenLeft !==  undefined ? window.screenLeft : window.screenX;
        const dualScreenTop = window.screenTop !==  undefined   ? window.screenTop  : window.screenY;

        const width = window.screen.width;
        const height = window.screen.height;

        const systemZoom = width / window.screen.availWidth;
        const left = (width - w) / 2 / systemZoom + dualScreenLeft;
        let top = ((height - h) / 2 / systemZoom + dualScreenTop)-50;

        if(top < 0) {
            top = 0;
        }

        w = w / systemZoom;
        h = h / systemZoom;

        let _newWin = window.open("","actionWindow","scrollbars=yes,width="+w+",height="+h+",top="+top+",left="+left+",toolbar=0");

        Window.newWindowTimer = setInterval(() => {
            if(_newWin.closed) {
                // this.loaderHandler({
                //     showLoader: false,
                //     processing: false
                // });
    
                clearInterval(Window.newWindowTimer);

                if(window.name == "add-and-pay") {
                    if(window.location !== window.parent.location) {
                        window.parent.postMessage({"txncancel": true, "encSessionData" : this.state.data.encSessionData}, "*");
                    } else if(window.opener !== undefined) {
                        window.opener.postMessage({"txncancel": true, "encSessionData" : this.state.data.encSessionData}, "*");
                    }
                } else if(!Window.windownTransactionComplete) {
                    Window.id("cancel-form").submit();
                }
            }
    
            window.addEventListener("message", event => {
                let obj = event.data;

                clearInterval(Window.newWindowTimer);
    
                if(obj.closeIframe) {
                    Window.windownTransactionComplete = true;
                    this.sendDataToMerchant(obj);
                } else if(obj.PAYMENT_FlOW === "ADDANDPAY") {
                    Window.windownTransactionComplete = true;
    
                    if(window.location !== window.parent.location) {
                        window.parent.postMessage(obj, "*");
                    } else if(window.opener !== undefined) {
                        window.opener.postMessage(obj, "*");
                    }
                } 
            });
        }, 500);


        that.submit();
    }

    sendDataToMerchant = obj => {
        window.parent.postMessage(obj, "*");
    };

    cancelHandler = e => {
        e.preventDefault();

        this.loaderHandler({
            showLoader: true,
            processing: false
        });

        if(window.name == "add-and-pay") {
            window.parent.postMessage({"closeIframe" : true}, "*");
        } else {
            Window.id("cancel-form").submit();
        }

    }

    render() {
        const components = [
            {
                "item": Navigation,
                "label": "navigation",
                "title": "Navigation",
                "componentType": "default"
            },            
            {
                "item": Cards,
                "label": "cards",
                "title": "Cards",
                "componentType": "default"
            },
            {
                "item": NetBanking,
                "label": "netBanking",
                "title": "Net Banking",
                "componentType": "default"
            },
            {
                "item": UpiSection,
                "label": "upi",
                "title": "UPI",
                "componentType": "default"
            },
            {
                "item": UpiQrBox,
                "label": "UPI_QR",
                "title": "UPI QR",
                "componentType": "default"
            },
            {
                "item": Wallet,
                "label": "wallet",
                "title": "Wallet",
                "componentType": "default"
            },
            {
                "item": CashOnDelivery,
                "label": "cashOnDelivery",
                "title": this.state.data !== null ? this.state.data.codName !== "" && this.state.data.codName !== undefined && this.state.data.codName !== null ? this.state.data.codName : "Cash on Delivery" : "Cash on Delivery",
                "componentType": "default"
            },
            {
                "item": CryptoCurrency,
                "label": "crypto",
                "title": "Crypto",
                "componentType": "default"
            },
            {
                "item": EMI,
                "label": "emi",
                "title": "EMI",
                "componentType": "default"
            },
            {
                "item": WalletLogin,
                "label": "walletLogin",
                "title": "Mobikwik",
                "componentType": "default"
            },
            {
                "item": WalletBalance,
                "label": "walletBalance",
                "title": "Mobikwik",
                "componentType": "default"
            },
            {
                "item": Navigation,
                "label": "quickPay",
                "title": "Quick Pay",
                "componentType": "saved"
            },                        
            {
                "item": SavedVpa,
                "label": "savedVpa",
                "title": "Saved VPA",
                "componentType": "saved"
            },
            {
                "item": SavedNetBanking,
                "label": "savedNetBanking",
                "title": "Saved Banks",
                "componentType": "saved"
            },
            {
                "item": Wallet,
                "label": "savedWallet",
                "title": "Saved Wallet",
                "componentType": "saved"
            },
            {
                "item": Navigation,
                "label": "addAndPay",
                "title": "Add & Pay",
                "componentType": "addMoney"
            },
            {
                "item": AddPayCards,
                "label": "addPaycards",
                "title": "Cards",
                "componentType": "addMoney"
            },
            {
                "item": AddPayNetBanking,
                "label": "addPayNetBanking",
                "title": "Net Banking",
                "componentType": "addMoney"
            },
        ];

        const componentsToRender = components.map(component => {
            if(this.state.activeComponent === component.label) {
                let SpecificComponent = component.item;
                return <SpecificComponent
                    key={component.label}
                    activeComponent={this.state.activeComponent}
                    title={component.title}
                    data={this.state.data}
                    paymentType={this.state.paymentType}
                    navigationHandler={this.navigationHandler}
                    navStateHandler={this.navStateHandler}
                    restrictCopyPasteHandler={this.restrictCopyPasteHandler}
                    loaderHandler={this.loaderHandler}
                    convenienceFeeHandler={this.convenienceFeeHandler}
                    submitBtnHandler={this.submitBtnHandler}
                    walletSelectHandler={this.walletSelectHandler}
                    numberInputHandler={this.numberInputHandler}
                    verifyUserHandler={this.verifyUserHandler}
                    resetErrorHandler={this.resetErrorHandler}
                    loadWalletBalanceHandler={this.loadWalletBalanceHandler}
                    editWalletNumberHandler={this.editWalletNumberHandler}
                    activeComponentHandler={this.activeComponentHandler}
                    tokenHandler={this.tokenHandler}
                    upiIconHandler={this.upiIconHandler}
                    upiToggleListHandler={this.upiToggleListHandler}
                    validateFormHandler={this.validateFormHandler}
                    submitHandler={this.submitHandler}
                    selectDefaultBankHandler={this.selectDefaultBankHandler}
                    labelSelectHandler={this.labelSelectHandler}
                    submitInNewWindow={this.submitInNewWindow}
                    updateMobikwikState={this.updateMobikwikState}
                    getTotalAmount={this.getTotalAmount}
                    mobikwikOtpHandler={this.mobikwikOtpHandler}
                    deleteHandler={this.deleteHandler}
                    quickPayHandler={this.quickPayHandler}
                    isVisiblePayBtn={this.state.isVisiblePayBtn}
                    totalAmount={this.state.totalAmount}
                    componentType={component.componentType}
                    isActivePayBtn={this.state.isActivePayBtn}
                    walletObj={this.state.walletObj}
                    displayError={this.state.displayError}
                    selectedMopType={this.state.selectedMopType}
                    activeNavigation={this.state.activeNavigation}
                    activeQuickPayId={this.state.activeQuickPayId}
                    payBtnText={this.state.payBtnText}
                />
            }

            return null;
        });

        // VARIABLES
        let mainContent = <Loader processing={false} approvalNotification={false} />,
            loader = null;            

        if(this.state.loader.showLoader) {
            loader = <Loader processing={this.state.loader.defaultText} approvalNotification={this.state.loader.approvalNotification} />
        } else {
            loader = null;
        }

        if(this.state.data !== null) {
            mainContent = (
                <React.Fragment>
                    <div id="container-outer-wrap">
                        <Header data={this.state.data} totalAmount={this.state.totalAmount} cancelHandler={this.cancelHandler} />

                        { componentsToRender }
                    </div>

                    <form method="POST" action={`${Window.baseUrl}/txncancel`} id="cancel-form">
                        <input type="hidden" name="payId" value={this.state.data.PAY_ID} />
                        <input type="hidden" name="encSessionData" value={this.state.data.encSessionData} />
                        <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.state.data.checkOutJsFlag} />
                    </form>

                    { loader }
                </React.Fragment>
            );
        }

        return (
            <React.Fragment>
                { mainContent }
            </React.Fragment>
        );
    }
}

export default Main;