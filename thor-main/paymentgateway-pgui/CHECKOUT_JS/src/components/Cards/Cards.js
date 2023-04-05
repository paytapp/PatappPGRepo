import React, { Component } from "react";
import { numOnly, numberEnterPhone } from "../../js/script";
import Checkbox from "../Checkbox/Checkbox";
import PaymentBtn from "../PaymentBtn/PaymentBtn";
import TitleSection from "../TitleSection/TitleSection";
import Fade from 'react-reveal/Fade';


class Card extends Component {
    state = {
        checked: true
    }

    componentDidMount() {
        this.createMopTypeImages(this.props.data.ccMopTypes, "cc", "ccMopIcon", "credit_cards");
        this.createMopTypeImages(this.props.data.dcMopTypes, "dc", "dcMopIcon", "debit_cards");
        this.createMopTypeImages(this.props.data.emCCMopType, "emicc", "emiccMopIcon", "emi_credit_cards");
        this.createMopTypeImages(this.props.data.emDCMopType, "emidc", "emidcMopIcon", "emi_debit_cards");
    }

    // CREATE MOP IMAGE
    
    createMopTypeImages = (mopType, paymentType, className, id) => {
        if(mopType !== undefined) {
            let collection = [];
            mopType.forEach((mop, index) => {
                let currentMobType = mop.toLowerCase();
                collection.push('<img src="'+ Window.basePath + '/img/' + currentMobType + '.png" alt="' + currentMobType + '" id="' + currentMobType + paymentType +'" class="'+ className +'">');
            });
            
            Window.id(id).innerHTML = collection.join('');
        }
    }

    checkFields= (e, formId) => {
        let _cardNumber = Window.id("card-number"),
            cvvNumber = Window.id('cvvNumber'),
            cardName = Window.id('cardName');
    
        if(!this.checkFirstLetter()) {
            e.preventDefault();
        }
    
        if(!this.checkLuhnInner(_cardNumber)) {
            e.preventDefault();
        } else if(!this.checkCardSupported()) {
            e.preventDefault();
        }
    
        if (cvvNumber.value.length !== Number(cvvNumber.getAttribute("maxlength"))) {
            Window.id('emptyCvv').style.display = 'none';
            Window.id('cvvValidate').style.display = "block";
            cvvNumber.classList.add("redLine");
            e.preventDefault();
        }
    
        if (cvvNumber.value.length === 0) {
            Window.id('cvvValidate').style.display = "none";
            Window.id('emptyCvv').style.display = 'block';
            cvvNumber.classList.add("redLine");
            e.preventDefault();
        }
        if (!cardName.value) {
            Window.id('nameError').style.display = 'block';
            cardName.classList.add("redLine");
            e.preventDefault();
        }
        if (!this.CheckExpiry()) {
            let paymentDate = Window.id('paymentDate'),
                emptyExpiry = Window.id('emptyExpiry'),
                validExpDate = Window.id('validExpDate');
    
            if (paymentDate.value) {
                emptyExpiry.style.display = 'none';
                validExpDate.style.display = 'block';
            } else {
                emptyExpiry.style.display = 'block';
                validExpDate.style.display = 'none';
            }

            paymentDate.classList.add("redLine");
            e.preventDefault();
        }
    
        Window.id('setExpiryMonth').value = Window.id('paymentDate').value.split('/')[0];
        Window.id('setExpiryYear').value = '20' + Window.id('paymentDate').value.split('/')[1];

        let $cardNumber = Window.querySelector('.cardNumber').value.replace(/\s/g, '');
        Window.id('cardNumber').value = Window.querySelector('.cardNumber').value;
        Window.id("cardBin").value = $cardNumber.slice(0, 9);
    
        if (this.checkFirstLetter() && this.CheckExpiryBoolean() && this.checkCvv() && this.nameCheckKeyUp() && this.checkMopTypeValidForUser() && this.checkLuhnInner(_cardNumber) && this.checkLuhnBooleanVal() && this.checkCardSupported()) {
            this.props.loaderHandler({showLoader: true});

            this.props.submitInNewWindow(Window.id(formId));
			e.preventDefault();
        } else {
            e.preventDefault();
        }
    }


    updateCheckBoxValue = checkboxId => {
        Window.id(checkboxId).value = Window.id(checkboxId + '1').checked;
    }

    fourDigitSpace = e => {
        let that = e.target,
            position = that.selectionEnd,
            length = that.value.length;
    
        that.value = that.value.replace(/[^0-9]/g, '').replace(/(.{4})/g, '$1 ').trim();
        that.selectionEnd = position += ((that.value.charAt(position - 1) === ' ' && that.value.charAt(length - 1) === ' ') ? 1 : 0);
    }
    
    enterCardNum = _ => {
        let inputLength = Window.querySelector('.cardNumber').value.replace(/\s/g, '').length;

        if (inputLength < 9) {
            Window.id('emptyCardNumber').style.display = "none";
            Window.id('notSupportedCard').style.display = "none";
            Window.id('checkStartNo').style.display = "none";
            Window.id("cvvNumber").maxLength = 3;
            Window.querySelector(".cardNumber").maxLength = 23;
    
            this.checkErrorMsgShowOrNot();
            this.mopTypeIconShow("bc");
    
            Window.id("supported-payment-type").innerHTML = "";
            Window.id("supported-payment-type").setAttribute("data-type", "NA");
            Window.id("supported-payment-type").setAttribute("data-region", "NA");
            Window.id("supported-payment-type").classList.add("d-none");
    
            Window.id("paymentType2").value = "";
            Window.id("mopTypeCCDiv2").value = "";
            Window.id("cardHolderTypeId").value = "";
            Window.id("paymentsRegionId").value = "";
            
            let paymentType = this.props.paymentType;
            if(paymentType !== "EM") {
                this.props.convenienceFeeHandler(paymentType);
            }
    
            let ccMopIcon = document.getElementsByClassName('ccMopIcon'),
                dcMopIcon = document.getElementsByClassName('dcMopIcon');
    
            for (let mobIconElement = 0; mobIconElement < ccMopIcon.length; mobIconElement++) {
                ccMopIcon[mobIconElement].classList.remove("opacityMob");
                ccMopIcon[mobIconElement].classList.remove("activeMob");
            }
    
            for (let mobIconElement = 0; mobIconElement < dcMopIcon.length; mobIconElement++) {
                dcMopIcon[mobIconElement].classList.remove("opacityMob");
                dcMopIcon[mobIconElement].classList.remove("activeMob");
            }
            
            Window.alreadyPopulated = false;
            Window.isBinChecked = false;
        }
    
        if (inputLength >= 9 && inputLength <= 10 && !Window.alreadyPopulated) {
            Window.alreadyPopulated = true;
            Window.tempCardBin = Window.querySelector('.cardNumber').value.replace(/\s/g, '');
            this.binCheck();
        } else if(inputLength > 10 && !Window.alreadyPopulated) {
            Window.alreadyPopulated = true;
            Window.tempCardBin = Window.querySelector('.cardNumber').value.replace(/\s/g, '').substring(0, 9);
            this.binCheck();
        }
    
        if (Window.alreadyPopulated) {
            this.decideBinCheck(Window.querySelector('.cardNumber').value.replace(/\s/g, "").substring(0, 9));
        }
    
        let cardNumberElement = document.getElementsByClassName('pField masked')[0];
    
        if (this.checkFirstLetter() && this.CheckExpiryBoolean() && this.checkCvv() && this.nameCheckKeyUp() && this.checkMopTypeValidForUser() && this.checkLuhnInner(cardNumberElement) && this.checkLuhnBooleanVal()) {
            this.props.submitBtnHandler({active: true});
        } else {
            this.props.submitBtnHandler({active: false});
        }
    }

    checkErrorMsgShowOrNot = _ => {
        let checkStartNoDisplay = Window.id('checkStartNo').style.display,
            validCardCheckDisplay = Window.id('validCardCheck').style.display,
            emptyCardNumberDisplay = Window.id('emptyCardNumber').style.display,
            notSupportedCardDisplay = Window.id('notSupportedCard').style.display,
            cardNumber = Window.querySelector('.cardNumber');

        if (checkStartNoDisplay === "block" || validCardCheckDisplay === "block" || emptyCardNumberDisplay === "block" || notSupportedCardDisplay === "block") {
            cardNumber.classList.add("redLine");
        } else {
            cardNumber.classList.remove("redLine");
        }
    }

    mopTypeIconShow = icon => {
        let mopTypeIcon = icon.toLowerCase();

        Window.id('userMoptypeIcon').src = Window.basePath + "/img/" + mopTypeIcon + ".png";
    }

    binCheck = _ => {
        let substr = Window.querySelector('.cardNumber').value.replace(/\s/g, "").substring(0, 9),
            returnByBean = false,
            payload = {
                "bin": substr
            };

        fetch(`${Window.baseUrl}/binResolver`, {
            crossDomain: true,
            method: 'POST',
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        .then((response) => response.json())
        .then(obj => {
            Window.isBinChecked = true;
    
            // convient fee for cc and DC
            Window.id("mopTypeCCDiv2").value = obj.mopType;
            Window.id("paymentsRegionId").value = obj.paymentsRegion;
            Window.id("cardHolderTypeId").value = obj.cardHolderType;    
            
            let paymentTypeUI = this.props.paymentType;
            
            if(paymentTypeUI === "EM") {
                let emiPaymentType = Window.id("emiPaymentType").value;
                paymentTypeUI = emiPaymentType.slice(3);
            } else {
                if(Window.pageInfoObj[Window.binPaymentType[obj.paymentType]]) {
                    paymentTypeUI = obj.paymentType;
                }
            }
    
            let ccMopIcon = document.getElementsByClassName('ccMopIcon'),
                dcMopIcon = document.getElementsByClassName('dcMopIcon'),
                ccEmiMobIcon = document.getElementsByClassName('emiccMopIcon'),
                dcEmiMobIcon = document.getElementsByClassName('emidcMopIcon');
    
            // CHECK IF EMI IS ACTIVE
            let isEmiActive = false;
            if(Window.id("emiLi") !== null) {
                isEmiActive = Window.id("emiLi").classList.contains("active");
            }
    
            let allowedMopObj = {
                "CC" : "ccMopTypes",
                "DC" : "dcMopTypes"
            };
            
            let allowedMopTypes = Window.pageInfoObj[allowedMopObj[obj.paymentType]],
                isValidMopType = false;
    
            if(allowedMopTypes !== undefined) {
                isValidMopType = allowedMopTypes.indexOf(obj.mopType) > -1 ? true : false;
            }
    
            if(obj.paymentsRegion === "DOMESTIC") {
                if (obj.mopType !== null && obj.paymentType !== null && obj.paymentType === paymentTypeUI && isValidMopType) {
                    if(isEmiActive) {
                        let emiBankName = Window.id("emiBankName").value;
        
                        if(emiBankName !== obj.issuerBankName) {
                            returnByBean = this.cardNotSupportedError({
                                ccIcon: ccMopIcon,
                                dcIcon: dcMopIcon,
                                ccEmiIcon: ccEmiMobIcon,
                                dcEmiIcon: dcEmiMobIcon,
                                errorMsg: "Please enter" + emiBankName + "Card Number"
                            });
                        } else {
                            returnByBean = this.showMopImage({
                                ccIcon: ccMopIcon,
                                dcIcon: dcMopIcon,
                                ccEmiIcon: ccEmiMobIcon,
                                dcEmiIcon: dcEmiMobIcon,
                                mopType: obj.mopType,
                                paymentType: obj.paymentType,
                                paymentsRegion: obj.paymentsRegion
                            });
                        }
                    } else {
                        returnByBean = this.showMopImage({
                            ccIcon: ccMopIcon,
                            dcIcon: dcMopIcon,
                            ccEmiIcon: ccEmiMobIcon,
                            dcEmiIcon: dcEmiMobIcon,
                            mopType: obj.mopType,
                            paymentType: obj.paymentType,
                            paymentsRegion: obj.paymentsRegion
                        });
                    }
                } else if(obj.mopType !== null && obj.paymentType !== null && obj.paymentType !== paymentTypeUI) {
                    if(isEmiActive) {
                        let _emiPaymentType = Window.id("emiPaymentType");
                        let _emiPaymentTypeName = _emiPaymentType.options[_emiPaymentType.selectedIndex].text;
                        returnByBean = this.cardNotSupportedError({
                            ccIcon: ccMopIcon,
                            dcIcon: dcMopIcon,
                            ccEmiIcon: ccEmiMobIcon,
                            dcEmiIcon: dcEmiMobIcon,
                            errorMsg: "Please enter" + _emiPaymentTypeName + "Card Number"
                        });
                    } else {
                        returnByBean = this.cardNotSupportedError({
                            ccIcon: ccMopIcon,
                            dcIcon: dcMopIcon,
                            ccEmiIcon: ccEmiMobIcon,
                            dcEmiIcon: dcEmiMobIcon
                        });
                    }
                } else {
                    returnByBean = this.cardNotSupportedError({
                        ccIcon: ccMopIcon,
                        dcIcon: dcMopIcon,
                        ccEmiIcon: ccEmiMobIcon,
                        dcEmiIcon: dcEmiMobIcon
                    });
                }
            } else if(obj.paymentsRegion === "INTERNATIONAL") {
                if (obj.mopType !== null && obj.paymentType !== null && obj.paymentType === paymentTypeUI && Window.pageInfoObj[Window.binPaymentType['IN']] && !isEmiActive && isValidMopType) {
                    returnByBean = this.showMopImage({
                        ccIcon: ccMopIcon,
                        dcIcon: dcMopIcon,
                        ccEmiIcon: ccEmiMobIcon,
                        dcEmiIcon: dcEmiMobIcon,
                        mopType: obj.mopType,
                        paymentType: obj.paymentType,
                        paymentsRegion: obj.paymentsRegion
                    });
                } else if(obj.mopType !== null && obj.paymentType !== null && Window.pageInfoObj[Window.binPaymentType['IN']] && isEmiActive) {
                    returnByBean = this.cardNotSupportedError({
                        ccIcon: ccMopIcon,
                        dcIcon: dcMopIcon,
                        ccEmiIcon: ccEmiMobIcon,
                        dcEmiIcon: dcEmiMobIcon,
                        errorMsg: "Please enter Domestic Card number"
                    });
                } else {
                    returnByBean = this.cardNotSupportedError({
                        ccIcon: ccMopIcon,
                        dcIcon: dcMopIcon,
                        ccEmiIcon: ccEmiMobIcon,
                        dcEmiIcon: dcEmiMobIcon,
                    });
                }
            } else {
                returnByBean = this.cardNotSupportedError({
                    ccIcon: ccMopIcon,
                    dcIcon: dcMopIcon,
                    ccEmiIcon: ccEmiMobIcon,
                    dcEmiIcon: dcEmiMobIcon
                });
            }
    
            this.checkErrorMsgShowOrNot();
        });

        return returnByBean;
    }

    decideBinCheck = newBin => {
        if (Window.tempCardBin === newBin && newBin.length > 8) {
    
        } else {
            this.binCheck();
            Window.tempCardBin = newBin;
        }
    }

    checkFirstLetter = _ => {
        let inputVal = Window.querySelector('.cardNumber').value,
            firstDigit = Number(inputVal.substr(0, 1));
        if (inputVal !== '') {
            if (firstDigit === 3 || firstDigit === 4 || firstDigit === 5 || firstDigit === 6 || firstDigit === 7 || firstDigit === 8) {
                Window.id("checkStartNo").style.display = 'none';
                this.checkErrorMsgShowOrNot();
                return true;
            } else {
                Window.id("emptyCardNumber").style.display = 'none';
                Window.id("checkStartNo").style.display = 'block';
                Window.id('validCardCheck').style.display = "none";
                Window.id('notSupportedCard').style.display = "none";
                this.checkErrorMsgShowOrNot();
                return false;
            }
        } else {
            Window.id("emptyCardNumber").style.display = 'block';
            Window.id('validCardCheck').style.display = "none";
            Window.id('notSupportedCard').style.display = "none";
            Window.id("checkStartNo").style.display = 'none';
            Window.id("mopTypeCCDiv2").value = "";
            Window.id("cardHolderTypeId").value = "";
            Window.id("paymentType2").value = "";
            Window.id("paymentsRegionId").value = "";
    
            // let paymentType = Window.querySelector(".active").getAttribute("data-type");
            let paymentType = this.props.paymentType;
            if(paymentType !== "EM") {
                this.props.convenienceFeeHandler(paymentType);
            }
    
            this.checkErrorMsgShowOrNot();
            return false;
        }
    }

    CheckExpiryBoolean = _ => {
        let today = new Date(), someday = new Date(),
            paymentDate = Window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        if (!paymentDateVal) {
            return false;
        } else if (paymentDateVal.length < 5) {
            return false;
        } else if (paymentDateVal.length === 5) {
            let exMonth = paymentDateVal.split('/')[0];
            let exYear = paymentDateVal.split('/')[1];
            someday.setFullYear(20 + exYear, exMonth, 1);
            if (someday > today && exMonth < 13 && exMonth > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    checkCvv = _ => {
        let cvvNumber = Window.id('cvvNumber');
        let cvvNumberLength = cvvNumber.value.length;
        let maxLength = cvvNumber.maxLength;
        if (cvvNumber.value && cvvNumberLength === maxLength) {
            return true;
        } else {
            return false;
        }
    }

    nameCheckKeyUp = _ => {
        let getName = (Window.id('cardName').value).trim();
        let cardName = Window.id('cardName');
        if (getName.length > 0) {
            Window.id('nameError').style.display = 'none';
            cardName.classList.remove("redLine");
            return true;
        } else {
            return false;
        }
    }

    checkMopTypeValidForUser = _ => {
        let cardNumber = Window.querySelector('.cardNumber'),
            cardNumberVal = cardNumber.value.replace(/\s/g, '');
    
        if (cardNumberVal.length < 9) {
            return false;
        }
    
        let cardLabel = Window.id("supported-payment-type"),
            paymentType = cardLabel.getAttribute("data-type"),
            paymentRegion = cardLabel.getAttribute("data-region");
    
        if(paymentRegion === "DOMESTIC") {
            if(paymentType === "CC") {
                return this._checkMop("ccMopIcon");
            } else if(paymentType === "DC") {
                return this._checkMop("dcMopIcon");
            }
        } else if(paymentRegion === "INTERNATIONAL") {
            if(paymentType === "CC") {
                return this._checkMop("ccMopIcon");
            } else if(paymentType === "DC") {
                return this._checkMop("dcMopIcon");
            }
        } else {
            return false;
        }
    }

    cardNotSupportedError = data => {
        if (Window.id('checkStartNo').style.display === "block") {
            Window.id('notSupportedCard').style.display = 'none';
            Window.id('validCardCheck').style.display = 'none';
        } else {
            if(data.errorMsg !== undefined) {
                Window.id("notSupportedCard").innerHTML = data.errorMsg;
            }
    
            Window.id('notSupportedCard').style.display = 'block';
            Window.id('validCardCheck').style.display = 'none';
            Window.id("checkStartNo").style.display = 'none';
        }
    
        this.mopTypeIconShow('bc');
        this.checkErrorMsgShowOrNot();
    
        this.removeActiveClass(data.ccIcon);
        this.removeActiveClass(data.dcIcon);
        this.removeActiveClass(data.ccEmiIcon);
        this.removeActiveClass(data.dcEmiIcon);
    
        Window.id("supported-payment-type").innerHTML = "";
        Window.id("supported-payment-type").setAttribute("data-type", "NA");
        Window.id("supported-payment-type").setAttribute("data-region", "NA");
        Window.id("supported-payment-type").classList.add("d-none");
    
        return false;
    }

    showMopImage = data => {
        Window.id('notSupportedCard').style.display = 'none';
    
        this.mopTypeIconShow(data.mopType);
    
        this.addActiveClass(data.ccIcon, data.paymentType, data.mopType);
        this.addActiveClass(data.dcIcon, data.paymentType, data.mopType);
        this.addActiveClass(data.ccEmiIcon, "emi" + data.paymentType, data.mopType);
        this.addActiveClass(data.dcEmiIcon, "emi" + data.paymentType, data.mopType);
    
    
        if (this.checkFirstLetterBooleanVal() && this.checkLuhnBooleanVal() && this.CheckExpiryBoolean() && this.checkCvv() && this.nameCheckKeyUp()) {
            this.props.submitBtnHandler({active: true});
        } else {
            this.props.submitBtnHandler({active: false});
        }
    
        if(data.mopType === "AX") {
            Window.id("cvvNumber").maxLength = 4;
            Window.querySelector(".cardNumber").maxLength = 18;
        }
    
        
        Window.id("supported-payment-type").innerHTML = Window.cardPaymentType[data.paymentType];
        Window.id("supported-payment-type").setAttribute("data-type", data.paymentType);
        Window.id("supported-payment-type").setAttribute("data-region", data.paymentsRegion);
        Window.id("supported-payment-type").classList.remove("d-none");
    
    
        // SET ACTIVE CLASS TO RELEVANT TAB
        if(data.paymentsRegion === "INTERNATIONAL") {
            switch (data.paymentType) {
                case "DC":
                    this.setPaymentType({elementId: "paymentType2", paymentType: data.paymentType, feeType: "IN"});
                break;
                case "CC":
                    this.setPaymentType({elementId: "paymentType2", paymentType: data.paymentType, feeType: "IN"});
                break;

                default:
                    return null;                    
            }
        } else {			
            switch (data.paymentType) {
                case "DC":
                    this.setPaymentType({elementId: "paymentType2", paymentType: data.paymentType, feeType: data.paymentType});
                break;
                case "CC":
                    this.setPaymentType({elementId: "paymentType2", paymentType: data.paymentType, feeType: data.paymentType});
                break;

                default:
                    return null;
            }
        }
        
    
        return true;
    }

    _checkMop = eleClassName => {
        let mopIcon = document.getElementsByClassName(eleClassName);
        for (let i = 0; i < mopIcon.length; i++) {
            let isActiveMop = mopIcon[i].classList.contains("activeMob");
            if(isActiveMop) {
                return true;
            }
        }
        return false;
    }

    removeActiveClass = mopIcon => {
        for (let i = 0; i < mopIcon.length; i++) {
            mopIcon[i].classList.add("opacityMob");
            mopIcon[i].classList.remove("activeMob");
        }
    }

    addActiveClass = (mopIcon, paymentType, mopType) => {
        for (let i = 0; i < mopIcon.length; i++) {
            mopIcon[i].classList.add("opacityMob");
        }
        
        if(Window.id(mopType.toLowerCase() + paymentType.toLowerCase()) !== null) {
            Window.id(mopType.toLowerCase() + paymentType.toLowerCase()).classList.remove("opacityMob");
            Window.id(mopType.toLowerCase() + paymentType.toLowerCase()).classList.add("activeMob");
        }
    }

    checkFirstLetterBooleanVal = _ => {
        let inputVal = Window.querySelector('.cardNumber').value, firstDigit = Number(inputVal.substr(0, 1));
        if (inputVal !== '') {
            if (firstDigit === 3 || firstDigit === 4 || firstDigit === 5 || firstDigit === 6 || firstDigit === 7 || firstDigit === 8) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    checkLuhnBooleanVal = _ => {
        let cardNumber = Window.querySelector('.cardNumber');
        let cardvalue = cardNumber.value;
        let ipt = cardvalue.replace(/\s/g, '');
    
        let sum = 0;
        let cnumber = ipt.replace(/\s/g, '');
        let numdigits = cnumber.length;
        let parity = numdigits % 2;
        for (let i = 0; i < numdigits; i++) {
            let digit = parseInt(cnumber.charAt(i));
            if (i % 2 === parity)
                digit *= 2;
            if (digit > 9)
                digit -= 9;
            sum += digit;
        }
    
        let booleanResultOfLuhn = ((sum % 10) === 0);
        
        return booleanResultOfLuhn;
    }

    setPaymentType = reffObj => {
        let emiLi = Window.id("emiLi");
        
        if(emiLi !== null) {
            let isEmiActive = Window.id("emiLi").classList.contains("active");
            if(isEmiActive) {
                Window.id(reffObj.elementId).value = "EM" + reffObj.paymentType;
                this.props.convenienceFeeHandler("EMI" + reffObj.feeType);
            } else {
                Window.id(reffObj.elementId).value = reffObj.paymentType;
                this.props.convenienceFeeHandler(reffObj.feeType);
            }
        } else {
            Window.id(reffObj.elementId).value = reffObj.paymentType;
            this.props.convenienceFeeHandler(reffObj.feeType);
        }
    }

    enterCardNumRmvErrMsg = _ => {
        if (this.checkFirstLetter()) {
            Window.id('validCardCheck').style.display = "none";
        }
        this.checkErrorMsgShowOrNot();
    }

    tabChangeKeyPress = (e, nextTabID) => {
        let _keyCode = e.keyCode;
        if (_keyCode !== 8 && _keyCode !== 46 && _keyCode !== 37 && _keyCode !== 38 && _keyCode !== 39 && _keyCode !== 40) {
             if(e.target.value.length >= e.target.maxLength) {
                Window.id(nextTabID).focus();
            }
        }
    }

    removeEnterCardMsg = _ => {
        if (Window.querySelector('.cardNumber').value.length > 0) {
            Window.id('emptyCardNumber').style.display = "none";
        }
        
        this.checkErrorMsgShowOrNot();
    }

    restrictSpaceRemoval = e => {
        let that = e.target;
        let cursorPosition = this.getCursorPosition(that);
        let _value = that.value;
        let _keyCode = e.keyCode;
    
        if(_keyCode !== 37 && _keyCode !== 38 && _keyCode !== 39 && _keyCode !== 40) {
            if(_keyCode !== 8) {
                if((cursorPosition === 4 || cursorPosition === 5 || cursorPosition === 9 || cursorPosition === 10 || cursorPosition === 14 || cursorPosition === 15 || cursorPosition === 19 || cursorPosition === 20) && _value.length > cursorPosition) {
                    this.setCursorPosition(that, cursorPosition + 1);
                }
            }
        }
    }

    getCursorPosition = that => {
        let pos = 0;
    
        // IE Support
        if (document.selection) {
            let Sel = document.selection.createRange();
            let SelLength = document.selection.createRange().text.length;
            Sel.moveStart('character', -that.value.length);
            pos = Sel.text.length - SelLength;
        }
        // Firefox support
        else if (that.selectionStart || that.selectionStart === '0') {
            pos = that.selectionStart;
        }
        return pos;
    }

    setCursorPosition = (ctrl, pos) => {
        // Modern browsers
        if (ctrl.setSelectionRange) {
            ctrl.setSelectionRange(pos, pos);
        // IE8 and below
        } else if (ctrl.createTextRange) {
            let range = ctrl.createTextRange();
            range.collapse(true);
            range.moveEnd('character', pos);
            range.moveStart('character', pos);
            range.select();
        }
    }

    checkCardSupported = _ => {
        let containCard = Window.querySelector('.cardNumber').value.replace(/\s/g, "").length,
            checkStartNo = Window.id('checkStartNo');
    
        if (containCard >= 9 && Window.isBinChecked) {
            if (this.checkMopTypeValidForUser() === false) {
                if (checkStartNo.style.display === "none") {
                    Window.id('notSupportedCard').style.display = 'block';
                }
    
                Window.id('validCardCheck').style.display = 'none';
                this.checkErrorMsgShowOrNot();
                return false;
            } else {
                Window.id('notSupportedCard').style.display = 'none';
                this.checkErrorMsgShowOrNot();
                return true;
            }
        }
    }

    checkLuhnInner = element => {
        let cardvalue = element.value,
            ipt = cardvalue.replace(/\s/g, ''),
            flag = false;
    
        if (ipt.length === 0) {
            Window.id('validCardCheck').style.display = 'none';
            Window.id('notSupportedCard').style.display = "none";
            Window.id("checkStartNo").style.display = 'none';
            Window.id("emptyCardNumber").style.display = "block";
            this.checkErrorMsgShowOrNot();
            return flag;
        } else {
            let sum = 0,
                cnumber = ipt.replace(/\s/g, ''),
                numdigits = cnumber.length,
                parity = numdigits % 2;
    
            for (let i = 0; i < numdigits; i++) {
                let digit = parseInt(cnumber.charAt(i));
                if (i % 2 === parity)
                    digit *= 2;
                if (digit > 9)
                    digit -= 9;
                sum += digit;
            }
        
            let result = ((sum % 10) === 0);
        
            if (ipt.length > 12 && result) {
                Window.id('validCardCheck').style.display = 'none';
                Window.id('pay-now').disabled = false;
                flag = true;
            } else {
                Window.id('validCardCheck').style.display = 'block';
                Window.id('notSupportedCard').style.display = "none";
                Window.id("checkStartNo").style.display = 'none';
                Window.id("emptyCardNumber").style.display = "none";
                
                this.props.submitBtnHandler({active: false});
                Window.id('pay-now').disabled = false;
                flag = false;
            }
        
            this.checkErrorMsgShowOrNot();
        
            return flag;
        }
    }

    checkLuhn = e => {
        this.checkLuhnInner(e.target);
    }

    monthYearEnter = e => {
        var monthYearVal = e.target.value,
            temp_val = "";
	 
        if (!isNaN(monthYearVal)) {
            if (monthYearVal > 2 && monthYearVal < 10 && monthYearVal.length === 1) {
                temp_val = "0" + monthYearVal + "/";
                e.target.value = temp_val;
            } else if (monthYearVal >= 0 && monthYearVal < 10 && monthYearVal.length === 2 && e.keyCode !== 8) {
                temp_val = monthYearVal + "/";
                e.target.value = temp_val;
            } else if (monthYearVal > 9 && monthYearVal.length === 2 && e.keyCode !== 8) {
                temp_val = monthYearVal + "/";
                e.target.value = temp_val;
            }
        } else {
            var beforeSlashVal = monthYearVal.split('/')[0];
            if (beforeSlashVal.length > 2) {
                var elemntValwithoutSlash = monthYearVal.replace('/', '');
                e.target.value = elemntValwithoutSlash.substring(0, 2) + '/' + elemntValwithoutSlash.substring(2, 4);
            }
        }
    }

    // RESRICT SLASH REMOVAL
    restrictSlashRemoval = evt => {
        let that = evt.target;
        var cursorPosition = this.getCursorPosition(that);
        var _keyCode = evt.keyCode;
        var _eventType = evt.type;
        var _value = that.value;
        var slashIndex = _value.indexOf("/");

        if(_keyCode !== 37 && _keyCode !== 38 && _keyCode !== 39 && _keyCode !== 40) {
            if(_keyCode === 8) {
                if((cursorPosition === 3 && slashIndex === 2) || (cursorPosition === 2 && slashIndex === 1) || (cursorPosition === 1 && slashIndex === 0)) {
                    this.setCursorPosition(that, cursorPosition - 1);
                }
            }
        
            if(cursorPosition === 2 && _eventType === "keyup") {
                this.setCursorPosition(that, cursorPosition + 1);
            }
        
            if(that.value === "/") {
                that.value = "";
            }
        }
    }

    removeMmDdError = _ => {
        var today = new Date(),
            someday = new Date(),
            paymentDate = Window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        Window.id("emptyExpiry").style.display = 'none';
        Window.id("validExpDate").style.display = 'none';
        Window.id('paymentDate').classList.remove("redLine");
    
        if (!paymentDateVal) {
            return false;
        } else if (paymentDateVal.length < 5) {
            return false;
        } else if (paymentDateVal.length === 5) {
            var exMonth = paymentDateVal.split('/')[0];
            var exYear = paymentDateVal.split('/')[1];		
            someday.setFullYear(20 + exYear, exMonth, 1);
            if (someday > today && exMonth < 13 && exMonth > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    numberEnterCvv = e => {
        
            var elementValue = e.target.value;
            if (!(/^[0-9]+$/.test(elementValue))) {
                e.target.value = elementValue.replace(/[^0-9/]/g, "");
            }
        
    }
    
    CheckExpiryOnBlur = _ => {
        var today = new Date(),
            someday = new Date(),
            paymentDate = Window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        Window.id("emptyExpiry").style.display = 'none';
        Window.id("validExpDate").style.display = 'none';
        paymentDate.classList.remove("redLine");
    
        if (paymentDateVal) {
            if (paymentDateVal.length < 5) {
                Window.id("validExpDate").style.display = 'block';
                paymentDate.classList.add("redLine");
            } else if (paymentDateVal.length === 5) {
                var exMonth = paymentDateVal.split('/')[0];
                var exYear = paymentDateVal.split('/')[1];
                someday.setFullYear(20 + exYear, exMonth, 1);
                if (someday > today && exMonth < 13 && exMonth > 0) {
                    return true;
                } else {
                    Window.id("validExpDate").style.display = 'block';
                    paymentDate.classList.add("redLine");
                }
            }
        } else {
            Window.id("emptyExpiry").style.display = 'block';
            paymentDate.classList.add("redLine");
        }
    }

    CheckExpiry = _ => {
        var today = new Date(),
            someday = new Date(),
            paymentDate = Window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        Window.id("emptyExpiry").style.display = 'none';
        Window.id("validExpDate").style.display = 'none';
        paymentDate.classList.remove("redLine");
    
        if (paymentDateVal) {
            if (paymentDateVal.length < 5) {
                Window.id("validExpDate").style.display = 'block';
                paymentDate.classList.add("redLine");
                return false;
            } else if (paymentDateVal.length === 5) {
                var exMonth = paymentDateVal.split('/')[0];
                var exYear = paymentDateVal.split('/')[1];
                someday.setFullYear(20 + exYear, exMonth, 1);
                if (someday > today && exMonth < 13 && exMonth > 0) {
                    return true;
                } else {
                    Window.id("validExpDate").style.display = 'block';
                    paymentDate.classList.add("redLine");
                    return false;
                }
            }
        } else {
            Window.id("emptyExpiry").style.display = 'block';
            paymentDate.classList.add("redLine");
        }
    }

    checkCvvFocusOut = _ => {
        var cvvNumber = Window.id('cvvNumber'),
        cvvNumberLength = cvvNumber.value.length,
        maxLength = cvvNumber.maxLength;
    
        Window.id('cvvValidate').style.display = "none";
        Window.id('emptyCvv').style.display = 'none';
        cvvNumber.classList.remove("redLine");
    
        if (cvvNumber.value) {
            if (cvvNumberLength === maxLength) {
                Window.id('cvvValidate').style.display = "none";
                Window.id('emptyCvv').style.display = 'none';
                cvvNumber.classList.remove("redLine");
            } else {
                Window.id('cvvValidate').style.display = "block";
                cvvNumber.classList.add("redLine");
            }
        } else {
            Window.id('emptyCvv').style.display = 'block';
            cvvNumber.classList.add("redLine");
        }
    }

    isCharacterKeyWithSpace = e => {
        var k;
        document.all ? k = e.keyCode : k = e.which;
        return ((k > 64 && k < 91) || (k > 96 && k < 123) || (k === 8) || (k === 32));
    }

    alphabetEnterPhone = event => {
        if ((event.target.value).trim()) {
            event.target.value = event.target.value.replace(/[^a-zA-Z ]/g, '').replace(/ +/g, ' ');
        } else {
            event.target.value = event.target.value.replace(/[^a-zA-Z]/g, '');
        }
    }

    nameCheck = _ => {
        var cardName = Window.id('cardName'),
        getName = cardName.value,
        nameError = Window.id('nameError');
    
        if (getName) {
            cardName.classList.remove("redLine");
            nameError.style.display = 'none';
        } else {
            cardName.classList.add("redLine");
            nameError.style.display = 'block';
        }
    }

    removeCvvError = _ => {
        let _cvvInput = Window.id('cvvNumber'),
            cvvNumberLength = _cvvInput.value.length,
            maxLength = _cvvInput.maxLength;

        Window.id('cvvValidate').style.display = "none";
        Window.id('emptyCvv').style.display = 'none';
        _cvvInput.classList.remove("redLine");
    
        if (cvvNumberLength === maxLength) {
            return true;
        } else {
            return false;
        }
    }

    render() {
        return (            
            <React.Fragment>
                <TitleSection title={this.props.title} navigationHandler={this.props.navigationHandler} btnDataId="navigation" />

                <div className="container custom-container mt-170">
                    <div className="tabBox debitWithPinBox" id="debitWithPin">                        
                        <form autoComplete="off" name="creditcard-form" method="post" target="_self" action={`${Window.baseUrl}/pay`} id="creditCard" onSubmit={e => this.checkFields(e, "creditCard")}>
                            <div className="row cardSection">
                                <div className="allCreditCard" id="credit_cards"></div>
                                <div className="allCreditCard" id="debit_cards"></div>
                                <div className="allCreditCard" id="emi_credit_cards"></div>
                                <div className="allCreditCard" id="emi_debit_cards"></div>

                                <Fade bottom duration={300}>
                                    <div className="col-lg-6 cardNumber1 card-number pb-10" id="divCardNumber">
                                        <input type="hidden" id="paymentType2" name="paymentType" />
                                        <input type="hidden" id="mopTypeCCDiv2" name="mopType" />
                                        <input type="hidden" id="issuerBankName2" name="issuerBankName" />
                                        <input type="hidden" id="issuerCountry2" name="issuerCountry" />
                                        <input type="hidden" id="cardHolderTypeId" name="cardHolderType" />
                                        <input type="hidden" id="paymentsRegionId" name="paymentsRegion" />
            
                                        {/* PARAMETERS FOR EMI */}
                                        <input type="hidden" id="emi-tenure" name="tenure" />
                                        <input type="hidden" id="emi-issuerName" name="issuerName" />
                                        <input type="hidden" id="emi-rateOfInterest" name="rateOfInterest" />
                                        <input type="hidden" id="emi-perMonthEmiAmount" name="perMonthEmiAmount" />
                                        <input type="hidden" id="emi-totalEmiAmount" name="totalEmiAmount" />
                                        <input type="hidden" id="emi-interest" name="emiInterest" />

                                        <input type="text" id="cardNumber" name="cardNumber" className="d-none" />

                                        <label htmlFor="card-number" className="placeHolderText placeHolderTextCardNum w-100 text-grey-light font-size-12 line-height-15 d-inline-flex justify-content-between mb-5 field-title"><span className="lang" data-key="cardNumber">Card Number</span><span id="supported-payment-type" className="d-none" data-type="NA" data-region="NA"></span></label>

                                        <div className="position-relative">
                                            <div className="userMoptype d-flex justify-content-end">
                                                <img id="userMoptypeIcon" className="align-self-center" src={Window.basePath + '/img/bc.png'} alt="" />
                                            </div>
                                            
                                            <input
                                                autoComplete="false"
                                                className="cardNumber pField masked inputField form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                                type="text"
                                                id="card-number"
                                                placeholder=" "
                                                onInput={(e) => {this.enterCardNum(); this.fourDigitSpace(e); this.enterCardNumRmvErrMsg(); this.tabChangeKeyPress(e,'paymentDate'); this.removeEnterCardMsg();}}
                                                onKeyUp={(e) => {this.restrictSpaceRemoval(e); this.enterCardNum(); this.enterCardNumRmvErrMsg(); this.tabChangeKeyPress(e,'paymentDate');}}
                                                onBlur={(e) => {this.checkCardSupported(); this.checkLuhn(e);}}
                                                onKeyDown={(e) => {this.removeEnterCardMsg()}}
                                                onPaste={this.props.restrictCopyPasteHandler}
                                                maxLength="23"
                                                inputMode="numeric"                                                
                                            />

                                            <div className="error position-absolute left-0 bottom-n-15">
                                                <p id="checkStartNo" className="text-danger1 font-size-11">Invalid Card Number</p>
                                                <p id="validCardCheck" className="text-danger1 font-size-11">Invalid Card Number</p>
                                                <p id="emptyCardNumber" className="text-danger1 font-size-11">Please Enter Card Number</p>
                                                <p id="notSupportedCard" className="text-danger1 font-size-11">Card Not Supported</p>
                                            </div>
                                        </div>
                                    </div>
                                </Fade>

                                <Fade bottom duration={600}>
                                    <div className="col-6 col-sm-4 col-lg-3 validityMain pb-10">
                                        <div className="validity position-relative" id="validity">
                                            <label htmlFor="paymentDate" className="placeHolderText placeHolderTextExpDate w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang" data-key="expiry">Expiry</label>
                                            
                                            <input
                                                type="text"
                                                id="paymentDate"
                                                inputMode="numeric"
                                                autoComplete="new-password"
                                                placeholder=" "
                                                className="inputField validityDateCard form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                                maxLength="5"
                                                onKeyUp={(e) => {this.monthYearEnter(e); this.restrictSlashRemoval(e); this.enterCardNum(); this.removeMmDdError(); this.numberEnterCvv(e); this.tabChangeKeyPress(e,'cvvNumber');}}
                                                onKeyDown={(e) => {this.monthYearEnter(e); this.restrictSlashRemoval(e); return numOnly(e);}}
                                                onBlur={this.CheckExpiryOnBlur}
                                                onPaste={this.props.restrictCopyPasteHandler}
                                                onDrop={this.props.restrictCopyPasteHandler} />
            
                                            <input type="hidden" name="expiryYear" id="setExpiryYear" />
                                            <input type="hidden" name="expiryMonth" id="setExpiryMonth" />
                                            
                                            <div className="error position-absolute left-0 bottom-n-15">
                                                <p id="validExpDate" className="text-danger1 font-size-11">Invalid Date</p>
                                                <p id="emptyExpiry" className="text-danger1 font-size-11">Please Enter Expiry</p>                                                
                                            </div>
                                        </div>
                                    </div>
                                </Fade>

                                <Fade bottom duration={900}>
                                    <div className="col-6 col-sm-4 col-lg-3 cvv-info cvv pb-10" id="divCvv">
                                        <label className="placeHolderText placeHolderTextCvv w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5">CVV</label>
                                        
                                        <div className="position-relative">
                                            <input
                                                autoComplete="new-password"
                                                className="pField inputField form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                                type="text"
                                                inputMode="numeric"									
                                                name="cvvNumber"
                                                placeholder=" "
                                                id="cvvNumber"
                                                onInput={this.props.numberInputHandler}
                                                onKeyPress={this.props.numberInputHandler}
                                                onKeyUp={(e) => {this.enterCardNum(); this.removeCvvError(); this.props.numberInputHandler(e); this.tabChangeKeyPress(e,'cardName');}}
                                                onBlur={this.checkCvvFocusOut}
                                                onPaste={this.props.restrictCopyPasteHandler}
                                                maxLength="3"
                                                onDrop={this.props.restrictCopyPasteHandler} />

                                            <div className="error position-absolute left-0 bottom-n-15">
                                                <p id="cvvValidate" className="text-danger1 font-size-11">Invalid CVV</p>
                                                <p id="emptyCvv" className="text-danger1 font-size-11">Please Enter CVV</p>
                                            </div>
                                        </div>
                                    </div>
                                </Fade>

                                <Fade bottom duration={1200}>
                                    <div className="col-sm-4 col-lg-12 card-holder-name pb-10" id="divName">
                                        <label className="placeHolderText placeHolderTextNameOnCard w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title" data-key="nameOnCard">Name on Card</label>
                                        <div className="position-relative">
                                            <input
                                                autoComplete="new-password"
                                                className="pField inputField card_holder_name form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                                type="text"
                                                name="cardName"
                                                id="cardName"
                                                placeholder=" "
                                                onKeyPress={(e) => {return this.isCharacterKeyWithSpace(e)}}
                                                onKeyUp={(e) => {this.nameCheckKeyUp(); this.alphabetEnterPhone(e); this.enterCardNum();}}
                                                onKeyDown={(e) => {this.alphabetEnterPhone(e)}}
                                                onBlur={(e) => {this.nameCheck(); e.target.scrollLeft = 0;}}
                                                onCopy={this.props.restrictCopyPasteHandler}
                                                onPaste={this.props.restrictCopyPasteHandler}
                                                onDrop={this.props.restrictCopyPasteHandler} />
                                            
                                            <div className="error position-absolute left-0 bottom-n-15">
                                                <p id="nameError" className="text-danger1 font-size-11">Please Enter Name</p>
                                                <p id="invalidError" className="text-danger1 font-size-11">Invalid Name</p>
                                            </div>
                                        </div>
                                    </div>
                                </Fade>

                                <Fade bottom>
                                    {/* CHECKBOX STARTED */}
                                    <Checkbox
                                        columnId="divSaveCard"
                                        checked={false}
                                        name="cardsaveflag"
                                        dataKey="saveCardCheckboxText"
                                        checkboxText="Save this card For future payments"
                                    />
                                    {/* CHECKBOX ENDED */}
                                </Fade>
                            </div>

                            <input type="hidden" id="ccdc-amount" name="amount" />

                            <input type="hidden" name="encSessionData" value={this.props.data.encSessionData} />
                            <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.props.data.checkOutJsFlag} />
                            <input type="hidden" name="payId" value={this.props.data.PAY_ID} />
                            <input type="hidden" name="bin" id="cardBin" />
                            
                            <button type="submit" className="btn-payment d-none"></button>
                    
                            <PaymentBtn payBtnText={this.props.payBtnText} totalAmount={this.props.totalAmount} formId="creditCard" submitHandler={this.props.submitHandler} isActivePayBtn={this.props.isActivePayBtn} />
                        </form>
                    </div>
                </div>
            </React.Fragment>            
        );
    }
}

export default Card;