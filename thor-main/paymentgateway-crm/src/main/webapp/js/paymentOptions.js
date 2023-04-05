
function _reset(){

    var _checkbox = document.querySelectorAll(".permission-checkbox input[type='checkbox']");
    _checkbox.forEach(function(index, array, element){
        index.checked = false;
        index.closest("label").classList.remove("checkbox-checked");
    })
}


function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
    if(_merchant != "" && _merchant != "ALL"){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                var  _option = "";
                if(_object.isSuperMerchant == true){
                    if(obj.superMerchant == true){
                        document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value="+_selectValue+">"+_selectLabel+"</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subMerchantAttr+" option[value='"+_selectValue+"']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker('refresh');
                        $("#"+_subMerchantAttr).selectpicker();
                    }else{
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
                }
                if(_object.subUser == true){
                    if(obj.subUserList.length > 0){
                        _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value=''>Select Sub-Merchant</option>";
                        for(var i = 0; i < obj.subUserList.length; i++){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subUserAttr+" option[value='']").selected = true;
                        $("#"+_subUserAttr).selectpicker();
                        $("#"+_subUserAttr).selectpicker('refresh');
                    }else{
                        document.querySelector("[data-target="+_subUserAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subUserAttr).value = "";
                    }
                }
                if(_object.glocal == true){
                    if(obj.glocalFlag == true){
                        document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
                        $("[data-id=deliveryStatus] select").selectpicker('val', 'All');
                    }else{
                        document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
                    }
                }

                if(_object.retailMerchantFlag == true){
                    $("#retailMerchantFlag").val(data.retailMerchantFlag);
                    document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
                }
            }
        }
        _xhr.send(data);
        setTimeout(function(e){
            document.querySelector("body").classList.add("loader--inactive");
        }, 1000);
    }else{
        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
        document.querySelector("#"+_subMerchantAttr).value = "";

    }
}



$(document).ready(function() {
    var keyObj = {
        creditCard:         "Credit Card",
        debitCard:          "Debit Card",
        netBanking:         "Net Banking",
        wallet:             "Wallet",
        emi:                "EMI",
        recurringPayment:   "Recurring Payment",
        expressPay:         "Express Pay",
        upi:                "UPI",
        upiQr:              "UPI QR",
        prepaidCard:        "Prepaid Card",
        debitCardWithPin:   "Debit Card With Pin",
        cashOnDelivery:     "Cash on Delivery",
        international:      "International",
        saveVpa:            "Save VPA"
    };

    // save VPA opton forcefully hide
    
    document.querySelector("#payId").addEventListener("change", function(e){
        getSubMerchant(e, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", 'ALL');
        resetAll();
        if(e.target.value == "ALL"){
            document.querySelector(".payment-action").classList.remove("d-none");
        }else{
            document.querySelector(".payment-action").classList.add("d-none");
        }
        showAllPaymentOptions(e.target.value, "showSelectedPaymentOption");
            
    });

    document.querySelector("#subMerchant").addEventListener("change", function(e){
        resetAll();
        showAllPaymentOptions(document.querySelector("#payId").value, "showSelectedPaymentOption");
    })

    var _checkAction = "";
    
    var _creditCard = [], _debitCard = [], _upi = [], _wallet = [], _netBanking = [], _emi = [];

    var showAllPaymentOptions = function(payId, url) {
        var _checkbox = $(".payment-options input[type='checkbox']");
        _checkbox.removeAttr("checked");

        document.querySelector("body").classList.remove("loader--inactive");

        let token  = document.getElementsByName("token")[0].value;
    
        $.ajax({
            type: "POST",
            url: url,
            data: {
                payId: payId,
                subMerchantId: $("#subMerchant").val(),
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data) {
                _creditCard = [];
                _debitCard = [];
                _netBanking = [];
                _upi = [];
                _wallet = [];
                _emi = [];

                if(data.aaData.length != 0){

                    _checkAction = "edit";

                    var _setJson = data.aaData[0]['mopTypeStringArray'];
    
                    for (key in data.aaData[0]){
                        var _typeOfVariable = typeof data.aaData[0][key];
                        if(_typeOfVariable == "boolean"){
                            if(data.aaData[0][key] == true){
                                var _checkNull = document.querySelector("[data-id='"+key+"']");
                                if(_checkNull != null){
                                    document.querySelector("[data-id='"+key+"']").closest(".lp-payment_options_main").classList.add("collapse-mop");
                                    document.querySelector("[data-id='"+key+"']").querySelector("input").checked = true;
                                    var _checkNull = document.querySelector("[data-mop='"+key+"']");
                                    if(_checkNull != null){
                                        mopType(document.querySelector("#"+key), document.querySelector("#"+key).value);
                                    }
                                }
                            }
                        }
                    }
    
                    setTimeout(function(){
    
                        for(var i =0; i < _setJson.length; i++){
        
                            if(_setJson[i].includes("Debit Card")){
                                _debitCard.push(_setJson[i].slice(_setJson[i].indexOf("-")+1));
                            }
        
                            if(_setJson[i].includes("Credit Card")){
                                _creditCard.push(_setJson[i].slice(_setJson[i].indexOf("-")+1));
                            }
        
                            if(_setJson[i].includes("UPI")){
                                _upi.push(_setJson[i].slice(_setJson[i].indexOf("-")+1));
                            }
        
                            if(_setJson[i].includes("Wallet")){
                                _wallet.push(_setJson[i].slice(_setJson[i].indexOf("-")+1));
                            }
        
                            if(_setJson[i].includes("EMI")){
                                _emi.push(_setJson[i].slice(_setJson[i].indexOf("-")+1));
                            }
        
                            if(_setJson[i].includes("Net Banking")){
                                _netBanking.push(_setJson[i].slice(_setJson[i].indexOf("-")+1));
                            }
        
                        }

        
                        if(_debitCard.length > 0){
                            for(var i = 0; i < _debitCard.length; i++){
                                document.querySelector("[data-append='debitCard']").querySelector("input[value='"+_debitCard[i]+"']").closest("label").classList.add("checkbox-checked");
                                document.querySelector("[data-append='debitCard']").querySelector("input[value='"+_debitCard[i]+"']").checked = true;
    
                            }
                            makeArray(document.querySelectorAll("[data-append='debitCard'] .moptype_checkboxes input[type='checkbox']"), "new");
                        }
                        if(_creditCard.length > 0){
                            for(var i = 0; i < _creditCard.length; i++){
                                if(document.querySelector("[data-append='creditCard']").querySelector("input[value='"+_creditCard[i]+"']") != null){
                                    document.querySelector("[data-append='creditCard']").querySelector("input[value='"+_creditCard[i]+"']").closest("label").classList.add("checkbox-checked");
                                    document.querySelector("[data-append='creditCard']").querySelector("input[value='"+_creditCard[i]+"']").checked = true;
                                }
    
                            }
                            makeArray(document.querySelectorAll("[data-append='creditCard'] .moptype_checkboxes input[type='checkbox']"), "new");
                        }
                        if(_netBanking.length > 0){
                            for(var i = 0; i < _netBanking.length; i++){
                                document.querySelector("[data-append='netBanking']").querySelector("input[value='"+_netBanking[i]+"']").closest("label").classList.add("checkbox-checked");
                                document.querySelector("[data-append='netBanking']").querySelector("input[value='"+_netBanking[i]+"']").checked = true;
    
                            }
                            makeArray(document.querySelectorAll("[data-append='netBanking'] .moptype_checkboxes input[type='checkbox']"), "new");
                        }
                        if(_wallet.length > 0){
                            for(var i = 0; i < _wallet.length; i++){
                                document.querySelector("[data-append='wallet']").querySelector("input[value='"+_wallet[i]+"']").closest("label").classList.add("checkbox-checked");
                                document.querySelector("[data-append='wallet']").querySelector("input[value='"+_wallet[i]+"']").checked = true;
    
                            }
                            makeArray(document.querySelectorAll("[data-append='wallet'] .moptype_checkboxes input[type='checkbox']"), "new");
                        }
                        if(_upi.length > 0){
                            for(var i = 0; i < _upi.length; i++){
                                document.querySelector("[data-append='upi']").querySelector("input[value='"+_upi[i]+"']").closest("label").classList.add("checkbox-checked");
                                document.querySelector("[data-append='upi']").querySelector("input[value='"+_upi[i]+"']").checked = true;
    
                            }
                            makeArray(document.querySelectorAll("[data-append='upi'] .moptype_checkboxes input[type='checkbox']"), "new");
                        }
                    }, 500);
    
                    setTimeout(() => {                    
                        document.querySelector("body").classList.add("loader--inactive");
                    }, 1000);
                }else{
                    _checkAction = "create";
                    // document.querySelector(".payment-action").classList.remove("d-none");
                    document.querySelector("body").classList.add("loader--inactive");
                }
            

            },
            error: function(data) {
                alert("Something went wrong!");
            }
        });
    }

    // showAllPaymentOptions("ALL", "showAllPaymentOptions");

    function resetAll(){
        var _allCheckbox = document.querySelectorAll(".lp-payment_options_main");
        
        _allCheckbox.forEach(function(index, array, element){
            if(index.querySelector("[data-append]") != null){
                index.querySelector("[data-append]").innerHTML = "";
            }
            index.classList.remove("collapse-mop");
            index.querySelector("[data-check]").checked = false;
            if(index.querySelector(".lp-payment_options_lable .selected_moptype span").innerHTML != ""){
                index.querySelector(".lp-payment_options_lable .selected_moptype span").innerHTML = "Nothing Selected";
            }
        })
        
    }

    var getData = function(_mopType, payId, merchantName, url) {

        
       
        $("body").removeClass("loader--inactive");
        var subMerchantId = $("#subMerchant").val();
        var isCreditCard = document.querySelector("#creditCard").checked;
        let isDebitCard = document.querySelector("#debitCard").checked;
        let isNetBanking = document.querySelector("#netBanking").checked;
        let wallet = document.querySelector("#wallet").checked;
        let isEMI = document.querySelector("#emi").checked;
        let isRecurringPayment = document.querySelector("#recurringPayment").checked;
        let isExpressPay = document.querySelector("#expressPay").checked;
        let isUPI = document.querySelector("#upi").checked;
        let isUPIQR = document.querySelector("#upiQr").checked;
        let isMqr = document.querySelector("#mqr").checked;
        let isPrepaidCard = document.querySelector("#prepaidCard").checked;
        let isDebitCardWithPin = document.querySelector("#debitCardWithPin").checked;
        let isCashOnDelivery = document.querySelector("#cashOnDelivery").checked;
        let isInternational = document.querySelector("#international").checked;
        let isCrypto = document.querySelector("#crypto").checked;
        let aamarPay = document.querySelector("#aamarPay").checked;
        let token  = document.getElementsByName("token")[0].value;

        var _checkMerchantClass = document.querySelector("#subMerchant").closest("[data-target='subMerchant']").classList.value.indexOf("d-none");
        if(_checkMerchantClass != -1){
            subMerchantId = null
        }
        $.ajax({
            type: "POST",
            url: url,
            data: {
                payId: payId,
                subMerchantId: subMerchantId,
                merchantName: merchantName,
                creditCard: isCreditCard,
                debitCard: isDebitCard,
                netBanking: isNetBanking,
                wallet: wallet,
                emi: isEMI,
                recurringPayment: isRecurringPayment,
                expressPay: isExpressPay,
                upi: isUPI,
                upiQr: isUPIQR,
                mqr: isMqr,
                prepaidCard: isPrepaidCard,
                debitCardWithPin: isDebitCardWithPin,
                cashOnDelivery: isCashOnDelivery,
                international: isInternational,
                mopTypeString : _mopType,
                crypto: isCrypto,
                aamarPay: aamarPay,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data) {

                window.scrollTo(0, 0);
                setTimeout(function(e){
                    $("#responseMsg").text(data.response);
                    $("[data-alert=response]").slideDown();
                    $("body").addClass("loader--inactive");
                }, 500);

                setTimeout(function(e){
                    $("[data-alert=response]").slideUp();
                }, 2000)

            },
            error: function(data) {
                alert("Something went wrong!");
                $("body").removeClass("loader--inactive");
            }
        });
    }



    // ACTIVATE / DE-ACTIVATE PAYMENT OPTIONS
    var activateDeactivate = function(_mopType, payId, merchantName, task, url) {
        $("body").removeClass("loader--inactive");

        let token  = document.getElementsByName("token")[0].value;
        var _paymentOptionList = [];
        var count = 0;

        $(".lp-payment_options_lable_div").find("input[type='checkbox']").each(function() {
            var isChecked = $(this).is(":checked");
            if(isChecked) {
                var inputName = $(this).closest("label").attr("for");
                _paymentOptionList[count] = inputName;
                count++;
            }
        });
        

        $.ajax({
            type: "POST",
            url: url,
            data: {
                payId: payId,
                merchantName: merchantName,
                mopTypeString : _mopType,
                paymentOptionList: _paymentOptionList.join(","),
                activationFlag: task == "activate" ? true : false,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data) {
                $("#responseMsg").text(data.response);
                $("[data-alert=response]").slideDown();
                $(".payment-options").find("input[type=checkbox]").prop("checked", false);
                $(".payment-options").find("label").removeClass("checkbox-checked");

                let filterPayId = $("#filter-payId").val();

                if(filterPayId == "" || filterPayId == null) {
                    filterPayId = "ALL";
                }
                
                if(filterPayId == "ALL") {
                    showAllPaymentOptions(filterPayId, "showAllPaymentOptions");
                } else {
                    showAllPaymentOptions(filterPayId, "showSelectedPaymentOption");
                }
                setTimeout(function(e){
                    $("[data-alert=response]").slideUp();
                    $("body").addClass("loader--inactive");
                    //  location.reload();
                }, 1000);
                _checkAction = "create";
            },
            error: function(data) {
                alert("Something went wrong!");
                $("body").removeClass("loader--inactive");
            }
        })
    }

    $("#task").on("change", function(e){
        var _val = e.target.value;
        if(_val == "activate" || _val == "deactivate"){
            _checkAction = "activate";
        }else{
            _checkAction = "create";
        }
    })

    // CREATE / REMOVE PAYMENT OPTIONS
    $("#btn-payment-options").on("click", function(e) {
        e.preventDefault();
        let payId   =   $("#payId").val();
        let parent  =   $(this).closest(".options-parent");
        let merchantName = $("button[data-id='payId']").attr("title");
        let _mopArr = [];
        var _false = true;
        var _getMoptypeDivs = document.querySelectorAll("[data-mop]");
        _getMoptypeDivs.forEach(function(index, array, element){
            var _checkbox = index.querySelector("#"+index.getAttribute("data-id")).checked;
            if(_checkbox == true){
                if(index.querySelector(".selected_moptype span").innerHTML == "Nothing Selected"){
                    _false = false;
                    alert("Please choose moptype of "+index.querySelector(".lp-payment_options_lable .font-size-16").innerText);
                }
                var _getInnerCheck = index.closest(".lp-payment_options_main").querySelectorAll(".moptype_checkboxes input[type='checkbox']");
                _getInnerCheck.forEach(function(ind, arr, ele){
                    if(ind.checked == true){
                        _mopArr.push(ind.getAttribute("id"));
                    }
                })
            }
        })
        if(_false == false){
            return false;
        }
        if(payId == null || payId == ""){
            alert("Please select merchant first");
            return false;
        }

        var _checkMerchant = document.querySelector("#subMerchant");
        var _checkMerchantClass = _checkMerchant.closest("[data-target='subMerchant']").classList.value.indexOf("d-none");
        if(_checkMerchantClass == -1){
            if(_checkMerchant.value == ""){
                alert("Please select sub-merchant first");
                return false;
            }else{
                merchantName = $("button[data-id='subMerchant']").attr("title");

            }
        }

        let task    =   $("#task").val();
        var _isPaymentOption = $("#payment-options").find("input[type='checkbox']:checked").length;
        if(_checkAction == "create") {
            getData(_mopArr.toString(), payId, merchantName, "createPaymentOption");
        } else if(task == "remove") {
            getData(null, payId, merchantName, "deletePaymentOption");
        } else if(task == "activate" || task == "deactivate") {
            activateDeactivate(_mopArr.toString(), payId, merchantName, task, "updatePaymentOptionPerPaymentType");
        }else{
            getData(_mopArr.toString(), payId, merchantName, "updatePaymentOption");
        }
    });

    // ajax call for mopType
    function mopType (e,_value){
        var _payId = document.querySelector("#payId").value;
        var _selector = e.getAttribute("id");
        $.ajax({
            type: "POST",
            url: "getMopTypeAction",
            data: {
                "paymentType": _value,
                "payId" : _payId
            },
            success: function(data){
                if(Object.keys(data.mopList).length != 0){
                    _resolves = true;
                    var _options = {};
                    for(key in data.mopList){

                        for(key2 in data.mopList[key]){
                            _options[key2] = data.mopList[key][key2];
                        }
                    }
                    createListElement("[data-append='"+_selector+"']", "input", _options);
                }else{
                    document.querySelector(".lpay_popup").style.display = "none";
                }
            }
        })
    }


    // function loopFunction(_array, _selector, _condition){
    //     document.querySelector("[data-id='"+_selector+"-edit'] .selected_moptype span").innerHTML = "";
    //     for(var i = 0; i < _array.length; i++){
    //         var _slice = _array[i].slice(0, _array[i].indexOf("-"));
    //         if(_slice == _condition){
    //             var _value = _array[i].slice(_array[i].indexOf("-")+1);
    //             var _getCheck = document.querySelector("[data-append='"+_selector+"-edit']");
    //             _getCheck.querySelector("input[value='"+_value+"']").closest("label").classList.add("checkbox-checked");
    //             document.querySelector("[data-id='"+_selector+"-edit'] .selected_moptype span").innerHTML +=  _getCheck.querySelector("input[value='"+_value+"']").closest("label").innerText
    //         }
    //     }
    // }
    

    // create function for check 
    function getCheckBoxChecked(_checkClick){

       
        var _checkboxes = document.querySelectorAll(".lp-payment_options_wrapper [data-check]");
        _checkboxes.forEach(function(index, array, element){
            _checkboxes[array].addEventListener('click', function(e){
                if(_checkClick != "filter"){
                    var _checkRequiredField = true;
                }else{
                    _checkRequiredField = true;
                }
                _checkboxes.forEach(function(ind, arr, ele){
                    ind.closest(".lp-payment_options_main").classList.remove("active-mop");
                })
                if(_checkRequiredField == true){
                    if(index.checked == true){
                        index.closest(".lp-payment_options_main").classList.add("collapse-mop");
                        index.closest(".lp-payment_options_main").classList.add("active-mop");
                        if(index.closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML == "Nothing Selected"){
                            mopType(e.target, index.value);
                        }
                        index.closest(".lp-payment_options_main").focus();
                        getDynamicCheckboxes();
                    }else{
                        index.removeAttribute("checked");
                    if(index.closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML == "Nothing Selected" || index.closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML == ""){
                            index.closest(".lp-payment_options_main").classList.remove("collapse-mop");
                            index.closest(".lp-payment_options_main").classList.remove("active-mop");
                            index.checked = false;
                            
                        }else{
                            index.closest(".lp-payment_options_main").classList.add("collapse-mop");
                            index.closest(".lp-payment_options_main").classList.add("active-mop");
                            index.checked = true;
                        }
                    }
                }
            })
        })
    }

    getCheckBoxChecked("load");

    // remove all boxes
    window.onclick = function(e){
        var _checkCheckboxes = e.target.closest(".lp-payment_options_main");
        if(_checkCheckboxes == null){
            var _checkboxes = document.querySelectorAll(".lp-payment_options_wrapper [data-check]");
            _checkboxes.forEach(function(ind, arr, ele){
                ind.closest(".lp-payment_options_main").classList.remove("active-mop");
            })
        }
    }

    // function for checkbox
    function makeArray(_selector, _checked){
        // console.log(_selector);
        _mopTypeCheckbox = [];
        if(_checked == "selectAll"){
            _selector.forEach(function(index, array, element){
                index.checked = true;
                index.closest("label").classList.add("checkbox-checked");
                _mopTypeCheckbox.push(index.closest("label").innerText);
            })
        }else if(_checked == "new"){
            _selector.forEach(function(ind, arr, ele){
                if(ind.checked == true){
                    ind.closest("label").classList.add("checkbox-checked");
                    _mopTypeCheckbox.push(ind.closest("label").innerText);
                }else{
                    ind.closest("label").classList.remove("checkbox-checked");
                }
            })
        }else{
            _selector.forEach(function(ind, arr, ele){
                if(ind.checked == true){
                    _mopTypeCheckbox.push(ind.closest("label").innerText);
                }
            })
        }
        if(_mopTypeCheckbox.length > 0 && _mopTypeCheckbox.length < 3){
            _selector[0].closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML = _mopTypeCheckbox;
        }else if(_mopTypeCheckbox.length >= 2){
            _selector[0].closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML =_mopTypeCheckbox[0]+","+_mopTypeCheckbox[1]+" & "+ Number(_mopTypeCheckbox.length - 2) + " More";
        }else {
            _selector[0].closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML = "Nothing Selected";
        }
    }

    var _mopTypeCheckbox = [];
    // find checkboxes 
    function getDynamicCheckboxes(){
        var _checkboxes = document.querySelectorAll(".lp-payment_options_mopType input[type='checkbox']");
        _checkboxes.forEach(function(index, array, element){
            _checkboxes[array].addEventListener("click", function(e){
                var _activeCheckboxes = e.target.closest(".lp-payment_options_main").querySelectorAll(".moptype_checkboxes input[type='checkbox']");
                if(e.target.checked == true){
                    e.target.closest("label").classList.add("checkbox-checked");
                    makeArray(_activeCheckboxes, e.target.value);
                }else{
                    if(e.target.value == "selectAll"){
                        _activeCheckboxes.forEach(function(ind, arr, ele){
                            ind.checked = false;
                            ind.closest("label").classList.remove("checkbox-checked");
                        })
                        _activeCheckboxes[0].closest(".lp-payment_options_main").querySelector(".selected_moptype span").innerHTML = "Nothing Selected";
                    }else{
                        makeArray(_activeCheckboxes, e.target.value);
                    }
                    e.target.closest("label").classList.remove("checkbox-checked");
                }
            })
        })
    }


    // remove class if click anywher
    
    // $(".checkbox-label input").on("change", function(e){
    //     var _payId = $("#payId").val();
    //     var _valueNew = $(this).val();
    //     var _checkEdit = $(this).hasClass("mopType-set");
    //     var _value = $(this).attr("data-check");
    //     _selected = [];
        
    //     if(_checkEdit == false){
    //         if($(this).is(":checked")){
    //             if(_payId != ""){
    //                 checked(e);
    //                 $(this).closest("label").addClass("checkbox-checked");
    //             }else{
    //                 alert("Please select merchant");
    //             }
    //         }else{
    //             checked(e);
    //             $(this).closest("label").removeClass("checkbox-checked");
    //         }
    //     }else{
    //         $(this).prop("checked", true);
    //         var _checkSetMode = document.querySelector("[data-id='"+_value+"']");
    //         console.log(_checkSetMode);
    //         if(_checkSetMode != null){
    //             checked(e);
    //             var _getValu = _checkSetMode.value.split(",");
    //             for(var i = 0; i < _getValu.length; i++){
    //                 var _new = _getValu[i];
    //                 _selected.push(_new.slice(_new.indexOf("-")+1));
    //             }
    //         }
    //         console.log(_selected);
    //     }   
    //   });





    function deleteRow(){
        var parent = $(".deleted-row");
        let payId = parent.find("input[name='payId']").val();
        let token  = document.getElementsByName("token")[0].value;

        $.ajax({
            type: "POST",
            url: "deletePaymentOption",
            data: {
                payId: payId,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data) {
                let filterPayId = $("#filter-payId").val();
                if(filterPayId == "ALL") {
                    showAllPaymentOptions(filterPayId, "showAllPaymentOptions");
                } else {
                    showAllPaymentOptions(filterPayId, "showSelectedPaymentOption");
                }
                setTimeout(function(e){
                    $(".selectpicker").val('default');
                    $(".selectpicker").selectpicker('refresh');
                    $("#responseMsg").text(data.response);
                    $("[data-alert=response]").slideDown();
                    $("body").addClass("loader--inactive");
                }, 500);

                setTimeout(function(e){
                    $("[data-alert=response]").slideUp();
                }, 2000)
            },
            error: function(data) {
                alert("Something went wrong!");
            }
        });
    }

    $("body").on("click", "#confirm-btn", function(e){
        deleteRow();
        $.fancybox.close();
    });
    $("body").on("click", "#cancel-btn", function(e){
        $.fancybox.close();
    })

    function createListElement(_selector, _listType, _value){
        // console.log(_selector);
        var _unique = document.querySelector(_selector).closest(".lp-payment_options_mopType_div").getAttribute("data-set");
        document.querySelector(_selector).innerHTML = "";
        var _option = "<div class='select_all_div'><label class='checkbox-label unchecked mb-10 mr-10'>Select All<input type='checkbox' value='selectAll' id='selectAll' /></label></div><div class='moptype_checkboxes'>";
    
        for(key in _value){
            _option += '<label for="'+_unique+'-'+key+'" class="checkbox-label unchecked mb-10">'+_value[key];
            _option += "<"+_listType+" type='checkbox' value='"+key+"' id='"+_unique+"-"+key+"' /></label>";
        }
        _option += "</div>";
        document.querySelector(_selector).innerHTML = _option;
        getDynamicCheckboxes();
    }
    
    

    function uncheck(_selector, _selector2){
        $(_selector+" [data-check='"+_selector2+"']").prop("checked", false);
        $(_selector+" [data-check='"+_selector2+"']").closest("label").removeClass("checkbox-checked");
    }

});

function removeErrorPop(e){
    e.closest(".lpay_select_group").classList.remove("hasError");
}

function createInput(_selector){
    var _arr = [];
    var _getAttr = document.querySelector(_selector).getAttribute("data-set");
    var _get = document.querySelector(_selector).selectedOptions;
    for(var i = 0; i < _get.length; i++){
        _arr.push(_getAttr+"-"+_get[i].value);
    }
    var _value = _arr.join();
    if(document.querySelector("[data-id='"+_getAttr+"']") != null){
        document.querySelector("[data-id='"+_getAttr+"']").value = _value;
    }else{
        var _input = "<input type='hidden' data-id='"+_getAttr+"' value='"+_value+"' />";
        document.querySelector("#mopTypeDiv").innerHTML += _input;
    }
    $("#mopTypeSelect").val('default');
    $("#mopTypeSelect").selectpicker('refresh');

}


// // remove error
function removeError(_that){
    _that.closest(".payment-input").classList.remove("hasError-class");
    if(_that.closest(".payment-input").querySelector(".error-class") != null){
        _that.closest(".payment-input").querySelector(".error-class").remove();
    }
}

// // manage form via JSON
function customToggleClass(_selector, _closest, _work, _errorTarget, _msg){
    _selector.closest(_closest).classList[_work]("hasError-class");
    createErrorMsg(_selector, _closest, _errorTarget);
    _selector.closest(_closest).querySelector(_errorTarget).innerText = _msg;
}

function createErrorMsg(_selector, _closest, _errorClass){
    var _createSpan = document.createElement("span");
    var _createSpanAttr = document.createAttribute("class");
    _createSpanAttr.value = _errorClass.slice(1, _errorClass.length);
    _createSpan.setAttributeNode(_createSpanAttr);
    _selector.closest(_closest).appendChild(_createSpan);
}

function createJson(_parentSelector, _commonSelector){
    var _json = {};
    var _test = true;
    _selectorParent = document.querySelectorAll("["+_parentSelector+"]");
    _selectorParent.forEach((index, array, element) => {
        if(element[array].attributes[_parentSelector] != undefined){
            if(element[array].attributes[_commonSelector] != undefined){
                _json[element[array].attributes[_commonSelector].nodeValue] = {
                    text : element[array].attributes[_commonSelector].nodeValue,
                }
            }
            if(element[array].attributes["data-required"] != undefined){
                _json[element[array].attributes[_commonSelector].nodeValue].required = true;
                _json[element[array].attributes[_commonSelector].nodeValue].requiredMsg = "Should not be blank"; 
            }
            if(element[array].attributes["data-reg"] != undefined){
                _json[element[array].attributes[_commonSelector].nodeValue].regular = true;
                _json[element[array].attributes[_commonSelector].nodeValue].regularMsg = "Invalid Value"; 
            }

        }
    });
    for(key in _json){
        var _selector = document.querySelector("[name='"+key+"']");
        var _errorClass = ".error-class";
        var _closestClass = ".payment-input";
        if(_json[key].required != undefined){
            if(_selector.value == ''){
                customToggleClass(_selector, _closestClass, "add", _errorClass, _json[key].requiredMsg);
                _test = false;
                continue;
            }
        }
        if(_json[key].regular != undefined){
            var _newReg = new RegExp(_selector.attributes["data-reg"].nodeValue);
            if(_newReg.test(_selector.value) == false){
                customToggleClass(_selector, _closestClass, "add", _errorClass, _json[key].regularMsg);
                _test = false;
                continue;
            }
        }
    }
    return _test;
}
