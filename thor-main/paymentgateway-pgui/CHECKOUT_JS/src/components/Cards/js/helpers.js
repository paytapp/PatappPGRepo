const helpers = {
    updateCheckBoxValue: function(checkboxId) {
        window.id(checkboxId).value = window.id(checkboxId + '1').checked;
    },    

    fourDigitSpace: function(e) {
        let that = e.target,
            position = that.selectionEnd,
            length = that.value.length;
    
        that.value = that.value.replace(/[^0-9]/g, '').replace(/(.{4})/g, '$1 ').trim();
        that.selectionEnd = position += ((that.value.charAt(position - 1) === ' ' && that.value.charAt(length - 1) === ' ') ? 1 : 0);
    },
    
    enterCardNum: function() {
        let inputLength = window.querySelector('.cardNumber').value.replace(/\s/g, '').length;
        if (inputLength < 9) {
            window.id('emptyCardNumber').style.display = "none";
            window.id('notSupportedCard').style.display = "none";
            window.id('checkStartNo').style.display = "none";
            window.id("cvvNumber").maxLength = 3;
            window.querySelector(".cardNumber").maxLength = 23;
    
            this.checkErrorMsgShowOrNot();
            this.mopTypeIconShow("bc");
    
            window.id("supported-payment-type").innerHTML = "";
            window.id("supported-payment-type").setAttribute("data-type", "NA");
            window.id("supported-payment-type").setAttribute("data-region", "NA");
            window.id("supported-payment-type").classList.add("d-none");
    
            window.id("paymentType2").value = "";
            window.id("mopTypeCCDiv2").value = "";
            window.id("cardHolderTypeId").value = "";
            window.id("paymentsRegionId").value = "";
    
            let paymentType = window.querySelector(".active").getAttribute("data-type");
            if(paymentType !== "EM") {
                addConvenienceFee(paymentType);
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
            
            window.alreadyPopulated = false;
            window.isBinChecked = false;
        }
    
        if (inputLength >= 9 && inputLength <= 10 && !window.alreadyPopulated) {
            window.alreadyPopulated = true;
            window.tempCardBin = window.querySelector('.cardNumber').value.replace(/\s/g, '');
            this.binCheck();
        } else if(inputLength > 10 && !window.alreadyPopulated) {
            window.alreadyPopulated = true;
            window.tempCardBin = window.querySelector('.cardNumber').value.replace(/\s/g, '').substring(0, 9);
            this.binCheck();
        }
    
        if (window.alreadyPopulated) {
            this.decideBinCheck(window.querySelector('.cardNumber').value.replace(/\s/g, "").substring(0, 9));
        }
    
        let cardNumberElement = document.getElementsByClassName('pField masked')[0];
    
        if (this.checkFirstLetter() && this.CheckExpiryBoolean() && this.checkCvv() && this.nameCheckKeyUp() && this.checkMopTypeValidForUser() && this.checkLuhnInner(cardNumberElement) && this.checkLuhnBooleanVal()) {
            window.id('pay-now').classList.remove("btn-disabled");
        } else {
            window.id('pay-now').classList.add("btn-disabled");
        }
    },

    checkErrorMsgShowOrNot: function() {
        let checkStartNoDisplay = window.id('checkStartNo').style.display,
            validCardCheckDisplay = window.id('validCardCheck').style.display,
            emptyCardNumberDisplay = window.id('emptyCardNumber').style.display,
            notSupportedCardDisplay = window.id('notSupportedCard').style.display,
            cardNumber = window.querySelector('.cardNumber');

        if (checkStartNoDisplay === "block" || validCardCheckDisplay === "block" || emptyCardNumberDisplay === "block" || notSupportedCardDisplay === "block") {
            cardNumber.classList.add("redLine");
        } else {
            cardNumber.classList.remove("redLine");
        }
    },

    mopTypeIconShow: function(getMopType) {
        let getMopTypeLowerCase = getMopType.toLowerCase();

        window.id('userMoptypeIcon').src = window.basePath + "/img/" + getMopTypeLowerCase + ".png";
    },

    binCheck: function() {
        let substr = window.querySelector('.cardNumber').value.replace(/\s/g, "").substring(0, 9),
            returnByBean = false,
            payload = {
                "bin": substr
            };

        ajaxRequest({
            actionName: '/pgui/jsp/binResolver',
            payload: payload,
            success: obj => {
                window.isBinChecked = true;

                // let obj = resObj;
        
                // convient fee for cc and DC
                window.id("mopTypeCCDiv2").value = obj.mopType;
                window.id("paymentsRegionId").value = obj.paymentsRegion;
                window.id("cardHolderTypeId").value = obj.cardHolderType;
        
                // // CHANGE THE TAB ACCORDING TO USER CARD NUMBER
                // checkPaymentTypeAndSelectedTab(obj.paymentType, obj.paymentsRegion);
        
                let activeTabId = window.querySelector(".active"),
                    paymentTypeUI = null;
                if(activeTabId !== undefined && activeTabId !== null) {
                    paymentTypeUI = activeTabId.getAttribute('data-type');
                    
                    if(paymentTypeUI === "EM") {
                        let emiPaymentType = window.id("emiPaymentType").value;
                        paymentTypeUI = emiPaymentType.slice(3);
                    } else {
                        if(window.pageInfoObj[window.binPaymentType[obj.paymentType]]) {
                            paymentTypeUI = obj.paymentType;
                        }
                    }
                }
        
                let ccMopIcon = document.getElementsByClassName('ccMopIcon'),
                    dcMopIcon = document.getElementsByClassName('dcMopIcon'),
                    ccEmiMobIcon = document.getElementsByClassName('emiccMopIcon'),
                    dcEmiMobIcon = document.getElementsByClassName('emidcMopIcon');
        
                // CHECK IF EMI IS ACTIVE
                let isEmiActive = false;
                if(window.id("emiLi") !== null) {
                    isEmiActive = window.id("emiLi").classList.contains("active");
                }
        
                let allowedMopObj = {
                    "CC" : "ccMopTypes",
                    "DC" : "dcMopTypes"
                };
                
                let allowedMopTypes = window.pageInfoObj[allowedMopObj[obj.paymentType]],
                    isValidMopType = false;
        
                if(allowedMopTypes !== undefined) {
                    isValidMopType = allowedMopTypes.indexOf(obj.mopType) > -1 ? true : false;
                }
        
                if(obj.paymentsRegion === "DOMESTIC") {
                    if (obj.mopType !== null && obj.paymentType !== null && obj.paymentType === paymentTypeUI && isValidMopType) {
                        if(isEmiActive) {
                            let emiBankName = window.id("emiBankName").value;
            
                            if(emiBankName !== obj.issuerBankName) {
                                returnByBean = this.cardNotSupportedError({
                                    ccIcon: ccMopIcon,
                                    dcIcon: dcMopIcon,
                                    ccEmiIcon: ccEmiMobIcon,
                                    dcEmiIcon: dcEmiMobIcon,
                                    errorMsg: multilingualText(window.id("translate").value, "emiBankErrorBefore") + emiBankName + multilingualText(window.id("translate").value, "emiBankErrorAfter")
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
                            let _emiPaymentType = window.id("emiPaymentType");
                            let _emiPaymentTypeName = _emiPaymentType.options[_emiPaymentType.selectedIndex].text;
                            returnByBean = this.cardNotSupportedError({
                                ccIcon: ccMopIcon,
                                dcIcon: dcMopIcon,
                                ccEmiIcon: ccEmiMobIcon,
                                dcEmiIcon: dcEmiMobIcon,
                                errorMsg: multilingualText(window.id("translate").value, "emiBankErrorBefore") + _emiPaymentTypeName + multilingualText(window.id("translate").value, "emiBankErrorAfter")
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
                    if (obj.mopType !== null && obj.paymentType !== null && obj.paymentType === paymentTypeUI && window.pageInfoObj[window.binPaymentType['IN']] && !isEmiActive && isValidMopType) {
                        returnByBean = this.showMopImage({
                            ccIcon: ccMopIcon,
                            dcIcon: dcMopIcon,
                            ccEmiIcon: ccEmiMobIcon,
                            dcEmiIcon: dcEmiMobIcon,
                            mopType: obj.mopType,
                            paymentType: obj.paymentType,
                            paymentsRegion: obj.paymentsRegion
                        });
                    } else if(obj.mopType !== null && obj.paymentType !== null && window.pageInfoObj[window.binPaymentType['IN']] && isEmiActive) {
                        returnByBean = this.cardNotSupportedError({
                            ccIcon: ccMopIcon,
                            dcIcon: dcMopIcon,
                            ccEmiIcon: ccEmiMobIcon,
                            dcEmiIcon: dcEmiMobIcon,
                            errorMsg: multilingualText(window.id("translate").value, "errorCardDomestic")
                        });
                    } else {
                        returnByBean = this.cardNotSupportedError({
                            ccIcon: ccMopIcon,
                            dcIcon: dcMopIcon,
                            ccEmiIcon: ccEmiMobIcon,
                            dcEmiIcon: dcEmiMobIcon,
                            // errorMsg: multilingualText[window.id("translate").value]["errorCardDomestic"]
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
            }
        });            

        return returnByBean;
    },

    decideBinCheck: function(newBin) {
        if (window.tempCardBin === newBin && newBin.length > 8) {
    
        } else {
            this.binCheck();
            window.tempCardBin = newBin;
        }
    },

    checkFirstLetter: function() {
        let inputVal = window.querySelector('.cardNumber').value,
            firstDigit = Number(inputVal.substr(0, 1));
        if (inputVal !== '') {
            if (firstDigit === 3 || firstDigit === 4 || firstDigit === 5 || firstDigit === 6 || firstDigit === 7 || firstDigit === 8) {
                window.id("checkStartNo").style.display = 'none';
                this.checkErrorMsgShowOrNot();
                return true;
            } else {
                window.id("emptyCardNumber").style.display = 'none';
                window.id("checkStartNo").style.display = 'block';
                window.id('validCardCheck').style.display = "none";
                window.id('notSupportedCard').style.display = "none";
                this.checkErrorMsgShowOrNot();
                return false;
            }
        } else {
            window.id("emptyCardNumber").style.display = 'block';
            window.id('validCardCheck').style.display = "none";
            window.id('notSupportedCard').style.display = "none";
            window.id("checkStartNo").style.display = 'none';
            window.id("mopTypeCCDiv2").value = "";
            window.id("cardHolderTypeId").value = "";
            window.id("paymentType2").value = "";
            window.id("paymentsRegionId").value = "";
    
            let paymentType = window.querySelector(".active").getAttribute("data-type");
            if(paymentType !== "EM") {
                addConvenienceFee(paymentType);
            }
    
            this.checkErrorMsgShowOrNot();
            return false;
        }
    },

    CheckExpiryBoolean: function() {
        let today = new Date(), someday = new Date(),
            paymentDate = window.id('paymentDate'),
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
    },

    checkCvv: function() {
        let cvvNumber = window.id('cvvNumber');
        let cvvNumberLength = cvvNumber.value.length;
        let maxLength = cvvNumber.maxLength;
        if (cvvNumber.value && cvvNumberLength === maxLength) {
            return true;
        } else {
            return false;
        }
    },

    nameCheckKeyUp: function() {
        let getName = (window.id('cardName').value).trim();
        let cardName = window.id('cardName');
        if (getName.length > 0) {
            window.id('nameError').style.display = 'none';
            cardName.classList.remove("redLine");
            return true;
        } else {
            return false;
        }
    },

    checkMopTypeValidForUser: function() {
        let cardNumber = window.querySelector('.cardNumber'),
            cardNumberVal = cardNumber.value.replace(/\s/g, '');
    
        if (cardNumberVal.length < 9) {
            return false;
        }
    
        let cardLabel = window.id("supported-payment-type"),
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
    },

    cardNotSupportedError: function(data) {
        if (window.id('checkStartNo').style.display === "block") {
            window.id('notSupportedCard').style.display = 'none';
            window.id('validCardCheck').style.display = 'none';
        } else {
            if(data.errorMsg !== undefined) {
                window.id("notSupportedCard").innerHTML = data.errorMsg;
            }
    
            window.id('notSupportedCard').style.display = 'block';
            window.id('validCardCheck').style.display = 'none';
            window.id("checkStartNo").style.display = 'none';
        }
    
        this.mopTypeIconShow('bc');
        this.checkErrorMsgShowOrNot();
    
        this.removeActiveClass(data.ccIcon);
        this.removeActiveClass(data.dcIcon);
        this.removeActiveClass(data.ccEmiIcon);
        this.removeActiveClass(data.dcEmiIcon);
    
        window.id("supported-payment-type").innerHTML = "";
        window.id("supported-payment-type").setAttribute("data-type", "NA");
        window.id("supported-payment-type").setAttribute("data-region", "NA");
        window.id("supported-payment-type").classList.add("d-none");
    
        return false;
    },

    showMopImage: function(data) {
        window.id('notSupportedCard').style.display = 'none';
    
        this.mopTypeIconShow(data.mopType);
    
        this.addActiveClass(data.ccIcon, data.paymentType, data.mopType);
        this.addActiveClass(data.dcIcon, data.paymentType, data.mopType);
        this.addActiveClass(data.ccEmiIcon, "emi" + data.paymentType, data.mopType);
        this.addActiveClass(data.dcEmiIcon, "emi" + data.paymentType, data.mopType);
    
    
        if (this.checkFirstLetterBooleanVal() && this.checkLuhnBooleanVal() && this.CheckExpiryBoolean() && this.checkCvv() && this.nameCheckKeyUp()) {
            window.id('pay-now').classList.remove("btn-disabled");
        } else {
            window.id('pay-now').classList.add("btn-disabled");
        }
    
        if(data.mopType === "AX") {
            window.id("cvvNumber").maxLength = 4;
            window.querySelector(".cardNumber").maxLength = 18;
        }
    
        
        window.id("supported-payment-type").innerHTML = window.cardPaymentType[data.paymentType];
        window.id("supported-payment-type").setAttribute("data-type", data.paymentType);
        window.id("supported-payment-type").setAttribute("data-region", data.paymentsRegion);
        window.id("supported-payment-type").classList.remove("d-none");
    
    
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
    },

    _checkMop: function(eleClassName) {
        let mopIcon = document.getElementsByClassName(eleClassName);
        for (let i = 0; i < mopIcon.length; i++) {
            let isActiveMop = mopIcon[i].classList.contains("activeMob");
            if(isActiveMop) {
                return true;
            }
        }
        return false;
    },

    removeActiveClass: function(mopIcon) {
        for (let i = 0; i < mopIcon.length; i++) {
            mopIcon[i].classList.add("opacityMob");
            mopIcon[i].classList.remove("activeMob");
        }
    },

    addActiveClass: function(mopIcon, paymentType, mopType) {
        for (let i = 0; i < mopIcon.length; i++) {
            mopIcon[i].classList.add("opacityMob");
        }
        
        if(window.id(mopType.toLowerCase() + paymentType.toLowerCase()) !== null) {
            window.id(mopType.toLowerCase() + paymentType.toLowerCase()).classList.remove("opacityMob");
            window.id(mopType.toLowerCase() + paymentType.toLowerCase()).classList.add("activeMob");
        }
    },

    checkFirstLetterBooleanVal: function() {
        let inputVal = window.querySelector('.cardNumber').value, firstDigit = Number(inputVal.substr(0, 1));
        if (inputVal !== '') {
            if (firstDigit === 3 || firstDigit === 4 || firstDigit === 5 || firstDigit === 6 || firstDigit === 7 || firstDigit === 8) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    },

    checkLuhnBooleanVal: function() {
        let cardNumber = window.querySelector('.cardNumber');
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
    },

    setPaymentType: function(reffObj) {
        let emiLi = window.id("emiLi");
        
        if(emiLi !== null) {
            let isEmiActive = window.id("emiLi").classList.contains("active");
            if(isEmiActive) {
                window.id(reffObj.elementId).value = "EM" + reffObj.paymentType;
                addConvenienceFee("EMI" + reffObj.feeType);
            } else {
                window.id(reffObj.elementId).value = reffObj.paymentType;
                addConvenienceFee(reffObj.feeType);
            }
        } else {
            window.id(reffObj.elementId).value = reffObj.paymentType;
            addConvenienceFee(reffObj.feeType);
        }
    },

    enterCardNumRmvErrMsg: function() {
        if (this.checkFirstLetter()) {
            window.id('validCardCheck').style.display = "none";
        }
        this.checkErrorMsgShowOrNot();
    },

    tabChangeKeyPress: function(e, nextTabID)  {
        let _keyCode = e.keyCode;
        if (_keyCode !== 8 && _keyCode !== 46 && _keyCode !== 37 && _keyCode !== 38 && _keyCode !== 39 && _keyCode !== 40) {
             if(e.target.value.length >= e.target.maxLength) {
                window.id(nextTabID).focus();
            }
        }
    },

    removeEnterCardMsg: function() {
        if (window.querySelector('.cardNumber').value.length > 0) {
            window.id('emptyCardNumber').style.display = "none";
        }
        
        this.checkErrorMsgShowOrNot();
    },

    restrictSpaceRemoval: function(e) {
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
    },

    getCursorPosition: function(that) {
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
    },

    setCursorPosition: function(ctrl, pos) {
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
    },

    checkCardSupported: function() {
        let containCard = window.querySelector('.cardNumber').value.replace(/\s/g, "").length,
            checkStartNo = window.id('checkStartNo');
    
        if (containCard >= 9 && window.isBinChecked) {
            if (this.checkMopTypeValidForUser() === false) {
                if (checkStartNo.style.display === "none") {
                    window.id('notSupportedCard').style.display = 'block';
                }
    
                window.id('validCardCheck').style.display = 'none';
                this.checkErrorMsgShowOrNot();
                return false;
            } else {
                window.id('notSupportedCard').style.display = 'none';
                this.checkErrorMsgShowOrNot();
                return true;
            }
        }
    },

    checkLuhnInner: function(element) {
        let cardvalue = element.value,
            ipt = cardvalue.replace(/\s/g, ''),
            flag = false;
    
        if (ipt.length === 0) {
            window.id('validCardCheck').style.display = 'none';
            window.id('notSupportedCard').style.display = "none";
            window.id("checkStartNo").style.display = 'none';
            window.id("emptyCardNumber").style.display = "block";
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
                window.id('validCardCheck').style.display = 'none';
                window.id('pay-now').disabled = false;
                flag = true;
            } else {
                window.id('validCardCheck').style.display = 'block';
                window.id('notSupportedCard').style.display = "none";
                window.id("checkStartNo").style.display = 'none';
                window.id("emptyCardNumber").style.display = "none";
    
                window.id("pay-now").classList.add("btn-disabled");
                window.id('pay-now').disabled = false;
                flag = false;
            }
        
            this.checkErrorMsgShowOrNot();
        
            return flag;
        }
    },

    checkLuhn: function(e) {
        this.checkLuhnInner(e.target);
    },

    monthYearEnter: function(e) {
        var monthYearVal = e.target.value,
            temp_val = "";
	 
        if (!isNaN(monthYearVal)) {
            if (monthYearVal > 1 && monthYearVal < 10 && monthYearVal.length === 1) {
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
    },

    // RESRICT SLASH REMOVAL
    restrictSlashRemoval: function(evt) {
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
    },

    removeMmDdError: function() {
        var today = new Date(),
            someday = new Date(),
            paymentDate = window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        window.id("emptyExpiry").style.display = 'none';
        window.id("validExpDate").style.display = 'none';
        window.id('paymentDate').classList.remove("redLine");
    
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
    },

    numberEnterCvv: function(e) {
        if (window.matchMedia("(max-width: 680px)")) {
            var elementValue = e.target.value;
            if (!(/^[0-9]+$/.test(elementValue))) {
                e.target.value = elementValue.replace(/[^0-9/]/g, "");
            }
        }
    },
    
    CheckExpiryOnBlur: function() {
        var today = new Date(),
            someday = new Date(),
            paymentDate = window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        window.id("emptyExpiry").style.display = 'none';
        window.id("validExpDate").style.display = 'none';
        paymentDate.classList.remove("redLine");
    
        if (paymentDateVal) {
            if (paymentDateVal.length < 5) {
                window.id("validExpDate").style.display = 'block';
                paymentDate.classList.add("redLine");
            } else if (paymentDateVal.length === 5) {
                var exMonth = paymentDateVal.split('/')[0];
                var exYear = paymentDateVal.split('/')[1];
                someday.setFullYear(20 + exYear, exMonth, 1);
                if (someday > today && exMonth < 13 && exMonth > 0) {
                    return true;
                } else {
                    window.id("validExpDate").style.display = 'block';
                    paymentDate.classList.add("redLine");
                }
            }
        } else {
            window.id("emptyExpiry").style.display = 'block';
            paymentDate.classList.add("redLine");
        }
    },

    CheckExpiry: function() {
        var today = new Date(),
            someday = new Date(),
            paymentDate = window.id('paymentDate'),
            paymentDateVal = paymentDate.value;
    
        window.id("emptyExpiry").style.display = 'none';
        window.id("validExpDate").style.display = 'none';
        paymentDate.classList.remove("redLine");
    
        if (paymentDateVal) {
            if (paymentDateVal.length < 5) {
                window.id("validExpDate").style.display = 'block';
                paymentDate.classList.add("redLine");
                return false;
            } else if (paymentDateVal.length === 5) {
                var exMonth = paymentDateVal.split('/')[0];
                var exYear = paymentDateVal.split('/')[1];
                someday.setFullYear(20 + exYear, exMonth, 1);
                if (someday > today && exMonth < 13 && exMonth > 0) {
                    return true;
                } else {
                    window.id("validExpDate").style.display = 'block';
                    paymentDate.classList.add("redLine");
                    return false;
                }
            }
        } else {
            window.id("emptyExpiry").style.display = 'block';
            paymentDate.classList.add("redLine");
        }
    },

    checkCvvFocusOut: function() {
        var cvvNumber = window.id('cvvNumber'),
        cvvNumberLength = cvvNumber.value.length,
        maxLength = cvvNumber.maxLength;
    
        window.id('cvvValidate').style.display = "none";
        window.id('emptyCvv').style.display = 'none';
        cvvNumber.classList.remove("redLine");
    
        if (cvvNumber.value) {
            if (cvvNumberLength === maxLength) {
                window.id('cvvValidate').style.display = "none";
                window.id('emptyCvv').style.display = 'none';
                cvvNumber.classList.remove("redLine");
            } else {
                window.id('cvvValidate').style.display = "block";
                cvvNumber.classList.add("redLine");
            }
        } else {
            window.id('emptyCvv').style.display = 'block';
            cvvNumber.classList.add("redLine");
        }
    },

    isCharacterKeyWithSpace: function(e) {
        var k;
        document.all ? k = e.keyCode : k = e.which;
        return ((k > 64 && k < 91) || (k > 96 && k < 123) || (k === 8) || (k === 32));
    },

    alphabetEnterPhone: function(event) {
        if ((event.target.value).trim()) {
            event.target.value = event.target.value.replace(/[^a-zA-Z ]/g, '').replace(/ +/g, ' ');
        } else {
            event.target.value = event.target.value.replace(/[^a-zA-Z]/g, '');
        }
    },

    nameCheck: function() {
        var cardName = window.id('cardName'),
        getName = cardName.value,
        nameError = window.id('nameError');
    
        if (getName) {
            cardName.classList.remove("redLine");
            nameError.style.display = 'none';
        } else {
            cardName.classList.add("redLine");
            nameError.style.display = 'block';
        }
    },

    removeCvvError: function() {
        let _cvvInput = window.id('cvvNumber'),
            cvvNumberLength = _cvvInput.value.length,
            maxLength = _cvvInput.maxLength;

        window.id('cvvValidate').style.display = "none";
        window.id('emptyCvv').style.display = 'none';
        _cvvInput.classList.remove("redLine");
    
        if (cvvNumberLength === maxLength) {
            return true;
        } else {
            return false;
        }
    }
};

export default helpers;