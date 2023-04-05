// global object
var _check = "";
function format ( d ) {

    d.button = function(e){

        if(d.status == "Processing" || d.status == "Timeout") {
            return "<button class='lpay_button lpay_button-md lpay_button-secondary checkTransStatus'>Get Status</button>"
        } else {
            return "<span>NA</span>"
        }

    }


    if(_check == "datatable"){

        var _obj = {
            "subMerchantName" : "Sub Merchant Name",
            "captureDateFrom" : "Captured Date From",
            "captureDateTo" : "Captured Date To",
            "payId" : "Pay ID",
            "bankAccountName" : "Payee Name",
            "bankIfsc" : "IFSC Code",
            "currency" : "Currency",
            "txnType" : "TXN Mode",
            "reponseMsgKey" : {
                "responseMsg" : "Response Message",
                "className" : "w-fluid"
            },
            "remarks" : "Remarks"
        }

    }

    if(_check == "st-datatable"){
        var _obj = {
            "subMerchantName" : "Sub Merchant Name",
            "alias" : "alias",
            "merchantPayId" : {
                "payId" : "Pay ID",
                "className" : "payId"
            },
            "createDate" : "Addition Date",
            "bankAccountName" : "Beneficiary Account Name",
            "accountNumber" : "Beneficiary Account Number",
            "bankIfsc" : "IFSC Code",
            "userType" : "Payee Type",
            "reponseMsgKey" : {
                "responseMsg" : "Response Message",
                "className" : "w-fluid"
            },
        }
    }

    _new = "<div class='main-div'>";
    
    for(key in _obj){
        if(_obj[key].hasOwnProperty("className")){
            var _getKey = Object.keys(_obj[key]);
            _new += '<div class="inner-div '+_obj[key]["className"]+'">'+
                    '<span>'+_obj[key][_getKey[0]]+'</span>'+
                    '<span>'+d[_getKey[0]]+'</span>'+
                '</div>'
        }else{
            _new += '<div class="inner-div">'+
                '<span>'+_obj[key]+'</span>'+
                '<span>'+d[key]+'</span>'+
            '</div>'
        }
    }
    
    if(_check == "st-datatable"){
        _new += '<div class="inner-div" style="width: 100%;text-align: center">'+
        '<span></span>'+'<span>'+d.button()+'</span>'+'</div>';
    }

    _new += "</div>";

    return _new;
   
    }

// GLOBAL BINDING
var _id = document.getElementById.bind(document),
    _selector = document.querySelector.bind(document),
    _selectorAll = document.querySelectorAll.bind(document),
    _createElement = document.createElement.bind(document);

var isBeneAvailable = false;

var objErrorMessage = {
    merchant : "Please Select Merchant",
    subMerchant : "Please select Sub Merchant",
    bankAccountNumber : "Please Enter Beneficiary Account No.",
    bankAccountName : "Please Enter Beneficiary Name",
    bankAccountNickName : "Please Enter Alias Name",
    payeeType : "Please Select Payee Type",
    bankIfsc : "Please enter IFSC Code",
    amount : "Please enter Amount",
    txnType : "Please select TXN Mode",
    currencyCode : "Please select Currency",
    mappedCurrency : "Please select Currency"
}

// LOADER ACTION
var loaderAction = function(action) {
    if(action == "show") {
        _selector("body").classList.remove("loader--inactive");
    } else if(action == "hide") {
        _selector("body").classList.add("loader--inactive");
    }
}

// AJAX ERROR MESSAGE
var ajaxErrorMsg = function(msg, status) {
    
        _selector(".lpay_popup-innerbox-"+status+" .responseMsg").innerHTML = msg;

    _selector(".lpay_popup-innerbox").setAttribute("data-status", status);
    $(".lpay_popup").fadeIn();

    setTimeout(function(e){
        loaderAction("hide");
    }, 1000);
}

// AJAX METHOD
var ajaxRequest = function(reffObj) {
    var jsonData = JSON.stringify(reffObj.payload);
    // var jsonData = reffObj.payload;
    
    var xhr = new XMLHttpRequest();
    xhr.open(reffObj.method, reffObj.actionName, true);
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.onload = function() {
        if (xhr.status === 200) {            
            var obj = JSON.parse(this.response);
            reffObj.success(obj);            
        } else {
            ajaxErrorMsg("Try again, Something went wrong!");
        }
    };

    xhr.send(jsonData);
}

// only digit
function digitDot(event) {
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46) {
    } else {
        event.preventDefault();
    }
}

var onlyNumberInput = function(that) {
    that.value = that.value.replace(/[^0-9]/g, '');
}

var _uppercase = function(that) {
    var _val = that.value;
    _val = _val.toUpperCase();
    that.value = _val;
}

var onlyAlphaNumeric = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9]/g, '');
}

var onlyAlpha = function(that) {
    if(that.value == " ") {
        that.value = "";
    } else {
        that.value = that.value.replace(/[^a-zA-Z &.]/g, '');
    }
}

var alphaNumericWithSpace = function(that) {
    if(that.value == " ") {
        that.value = "";
    } else {
        that.value = that.value.replace(/[^a-zA-Z0-9 ]/g, '');
    }
}

var removeErrorMsg = function(that) {
    var elementId = that.getAttribute("id");
    _selector('span[data-error="'+ elementId +'"]').classList.remove("show");
}

var validateInputField = function(that) {
    if(that.value !== "") {
        removeErrorMsg(that);
    } else {
        displayErrorMsg({
            errorMessage: objErrorMessage[that.getAttribute("id").slice(3)],
            elementId: that.getAttribute("id")
        });
    }
}

function checkAvailableAmount(_that){
    var _user_type = document.querySelector("#userType").value;
    var _amount = Number(_that.value);
    if(_user_type == "MERCHANT"){
        var _availableAmount = Number(document.querySelector(".tf-total_available_amount span").innerText);
        if(_amount > _availableAmount){
            _that.value = "";
            document.querySelector("#tf-amountShow").innerText = "";
            document.querySelector("[data-error='tf-amount']").innerText = "Amount must be less than Available Amount";
            
            document.querySelector("[data-error='tf-amount']").classList.add("show");
        }
    }

    if(_amount < 1){
        _that.value = "";
        document.querySelector("#tf-amountShow").innerText = "";
    }
}

var validateUTRInputField = function(that) {
    if(that.value !== "") {
        removeErrorMsg(that);
    } else {
        displayErrorMsg({
            errorMessage: "Please enter UTR No.",
            elementId: that.getAttribute("id")
        });
    }
}

var displayErrorMsg = function(obj) {
    var elementLabel = _selector('[data-error="'+ obj.elementId +'"]');
    elementLabel.innerHTML = obj.errorMessage;    
    elementLabel.classList.add("show");
}

function hideColumn(){
        var _isSuperMerchant = $("#isSuperMerchant").val();
        var _table = $("#datatable").DataTable();
        if(_isSuperMerchant == "N"){
          _table.columns(1).visible(false);
        }else{
            _table.columns(1).visible(true);
        } 
}

function hideColumnForTransaction(){
    var _isSuperMerchant = $("#isSuperMerchant").val();
    var _table = $("#st-datatable").DataTable();
    if(_isSuperMerchant == "N"){
      _table.columns(1).visible(false);
    }else{
        _table.columns(1).visible(true);
    } 
}
$(document).ready(function(e) {
    var updateMessage = function(that, parent, obj) {
        var submitButton = parent.find(".bulkSubmit"),
            inputLabel = that.closest("label");

        inputLabel.attr("data-status", obj.status);
        parent.find(".status-text-box .error-fileName").text(obj.errorMessage);
        parent.find(".input-fileName").val(obj.value);
        submitButton.attr("disabled", obj.isDisabled);
        loaderAction(obj.loader);
    }

    $(".lpay_upload_input").on("change", function(e) {
        var that = $(this),
            parent = that.closest(".lpay_tabs_content"),
            val = that.val(),            
            defaultUpload = parent.find(".default-upload"),
            placeHolder = parent.find(".placeholder_img");
        
        if(val !== '') {
            var fileSize = that[0].files[0].size,
                fileName = val.replace("C:\\fakepath\\", '');

            loaderAction("show");

            defaultUpload.addClass("d-none");
            placeHolder.addClass("d-none");
            
            if(fileSize < 2000000) {
                var fileExt = fileName.slice(fileName.indexOf('.') + 1, fileName.length),
                    dataTarget = parent.attr("data-target");
                
                if(dataTarget === 'bulkTransfer') {
                    if(fileExt === 'xlsx') {
                        updateMessage(that, parent, {status: 'success-status', errorMessage: fileName, value: fileName, isDisabled: false, loader: 'hide'});
                    } else {
                        updateMessage(that, parent, {status: 'error-status', errorMessage: 'Wrong file format', value: '', isDisabled: false, loader: 'hide'});
                    }
                } else if(dataTarget === 'bulkUTR') {
                    if(fileExt === 'csv' || fileExt === 'xlsx') {
                        updateMessage(that, parent, {status: 'success-status', errorMessage: fileName, value: fileName, isDisabled: false, loader: 'hide'});
                    } else {
                        updateMessage(that, parent, {status: 'error-status', errorMessage: 'Wrong file format', value: '', isDisabled: false, loader: 'hide'}); 
                    }
                }
            } else {
                updateMessage(that, parent, {status: 'error-status', errorMessage: 'File size too Long.', value: '', isDisabled: true, loader: 'hide'});
            }
        } else {
            defaultUpload.removeClass("d-none");
            placeHolder.removeClass("d-none");

            updateMessage(that, parent, {status: '', errorMessage: '', value: '', isDisabled: true, loader: 'hide'});
        }
    });

    bulkUpdateHandler = function(that, data) {
        var parent = that.closest(".lpay_tabs_content"),
            wrongFileBox = parent.find(".wrong-file"),
            successFileBox = parent.find(".success-file"),
            storedRow = parent.find(".storedRow"),
            totalData = parent.find(".totalData"),
            dataTarget = parent.attr("data-target");
        
        if(data.response === "success") {
            wrongFileBox.addClass("d-none");
            successFileBox.removeClass("d-none");
            totalData.html(data.totalData);
            storedRow.html(data.totalSuccess);

            fetchBulkTableData(dataTarget, dataTarget === "bulkTransfer" ? "PAYOUT_FILE" : "UTR_FILE");
        } else {
            if(data.wrongFile) {
                wrongFileBox.removeClass("d-none");
                successFileBox.addClass("d-none");
            }
        }
        
        loaderAction('hide');
    }

    $(".bulkSubmit").on("click", function(e) {
        e.preventDefault();

        var that = $(this),
            actionName = that.attr("data-action"),
            form = that.closest(".bulkUpload")[0];

        loaderAction("show");

        const payload = new FormData(form);

        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: actionName,
            data: payload,
            processData: false,
            contentType: false,
            success: function(data) {
                bulkUpdateHandler(that, data);
            }
        });
    });

    var today = new Date();
    $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
    $(".datepick").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date(),
        changeMonth: true,
        changeYear: true
    });

    
    // ADD BENEFICIARY
    $("#btn-add-beneficiary, #btn-transfer-funds").on("click", function(e) {

        e.preventDefault();

        var inputFields = $(this).closest(".lpay_tabs_content").find(".lpay-input"),
            count = 0;

        inputFields.each(function() {
            var inputId = $(this).attr("id");
            
            if(inputId !== undefined && inputId !== null && inputId !== "tf-remarks" && inputId !== "tf-currencyCode" && inputId !== "ab-defaultbene") {
                var trimmedId = inputId.slice(3);
                if(inputId == "ab-subMerchant" || inputId == "tf-subMerchant") {
                    if(!$("[data-id='ab-subMerchant']").hasClass("d-none") && $(this).val() == "") {
                        displayErrorMsg({
                            errorMessage: objErrorMessage[trimmedId],
                            elementId: inputId
                        });
        
                        count++;                        
                    }
                } else if($(this).val() == "") {                    
                    displayErrorMsg({
                        errorMessage: objErrorMessage[trimmedId],
                        elementId: inputId
                    });
    
                    count++;
                }
            }
        });

        var _that = $(this).attr("id"), _url = "";

        if(count == 0) {

            var formObj = {};
            inputFields.each(function() {

                var inputName = $(this).attr("name");
                if(inputName !== undefined) {
                    if(inputName == "defaultbene") {
                        formObj[inputName] = $(this).is(":checked");
                    } else {
                        formObj[inputName] = $(this).val();
                    }
                }

            });

            loaderAction("show");

            if(_userType != "MERCHANT"){
                $("#btn-transfer-funds").attr("disabled", true);
            }

            
            if(_that == "btn-add-beneficiary") {
                _url = "addBene";
            } else {
                _url = "sendTransaction";
            }

            $.ajax({
                type: "POST",
                url: _url,
                data: formObj,
                success: function(data) {
                    if(data.response== "success") {
                        setTimeout(function(e){
                            $(".lpay_popup-innerbox").attr("data-status", "success")
                            $(".lpay_popup").fadeIn();
                            $(".responseMsg").text(data.responseMsg);
                            $("body").addClass("loader--inactive");
                        }, 1000);
                    } else {
                        setTimeout(function(e){
                            $(".lpay_popup-innerbox").attr("data-status", "error");
                            $(".responseMsg").text(data.responseMsg);
                            $(".lpay_popup").fadeIn();
                            $("body").addClass("loader--inactive");
                        }, 1000);
                    }
                }
            });
        } else {
            if(_that == "btn-transfer-funds") {
                if(!isBeneAvailable && $("#tf-merchant").val() !== "") {
                    setTimeout(function(e) {
                        $(".lpay_popup-innerbox").attr("data-status", "error");
                        $(".responseMsg").text("Beneficiary not available!");
                        $(".lpay_popup").fadeIn();
                        $("body").addClass("loader--inactive");
                    }, 1000);

                    return false;
                }
            }
        }
    });

    var fetchBeneDetail = function(that) {
        var _val = that.val();

        if(_val !== "") {
            loaderAction("show");
            $.ajax({
                type: "POST",
                url: "fetchBeneForTransaction",
                data: {
                    "payId": _val
                },
                success: function(data) {
                    var _beneObj = data.beneObj;
                    if(_beneObj.bankAccountNumber !== null && _beneObj.defaultBene == true) {
                        $("#tf-bankAccountName").val(_beneObj.bankAccountName);
                        $("#tf-bankAccountNumber").val(_beneObj.bankAccountNumber);
                        $("#tf-bankIfsc").val(_beneObj.bankIfsc);
                        
                        $("[data-id='tf-bankAccountName']").removeClass("d-none");
                        $("[data-id='tf-bankAccountNumber']").removeClass("d-none");
                        $("[data-id='tf-bankIfsc']").removeClass("d-none");

                        $("[data-error='tf-bankAccountName']").removeClass("show");
                        $("[data-error='tf-bankAccountNumber']").removeClass("show");
                        $("[data-error='tf-bankIfsc']").removeClass("show");

                        $("#btn-transfer-funds").prop("disabled", false);

                        isBeneAvailable = true;

                        loaderAction("hide");

                    } else {
                        $("#btn-transfer-funds").prop("disabled", true);

                        setTimeout(function(e) {
                            $(".lpay_popup-innerbox").attr("data-status", "error");
                            $(".responseMsg").text("Beneficiary not available!");
                            $(".lpay_popup").fadeIn();
                            $("body").addClass("loader--inactive");
                        }, 1000);

                        isBeneAvailable = false;

                        // loaderAction("hide");

                    }

                    

                    
                },
                error: function() {
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();

                    setTimeout(function(e){
                        loaderAction("hide");
                    }, 1000);
                }
            });
        }
    }

    var fetchAccountDetail = function(that) {
        var _val = that.val();
        
        $("#ab-bankAccountName").val("");
        $("#ab-bankAccountNumber").val("");
        $("#ab-bankIfsc").val("");
        if(_val !== "") {
            loaderAction("show");
            $.ajax({
                type: "POST",
                url: "nodalFetchMerchantBankAccount",
                data: {
                    "payId": _val
                },
                success: function(data) {
                                      
                        var inputFields = that.closest(".lpay_tabs_content").find(".lpay-input");

                        inputFields.each(function(e) {
                            var inputId = $(this).attr("id");
                            
                            if(inputId !== undefined) {
                                var trimmedId = inputId.slice(3);
                                if(trimmedId == "defaultbene") {
                                    if(data[trimmedId] !== undefined && data[trimmedId]) {
                                        $("div[data-id='"+ trimmedId +"']").addClass("d-none");                                        
                                        $(this).prop("checked", true);
                                    } else if(data[trimmedId] !== undefined && !data[trimmedId]) {
                                        $("div[data-id='"+ trimmedId +"']").removeClass("d-none");
                                        $(this).prop("checked", false);
                                    }
                                } else if(data[trimmedId] !== undefined) {
                                    $(this).val(data[trimmedId]);
                                    $(".selectpicker").selectpicker("refresh");
                                }
                                
                                if($(this).val() !== "") {
                                    $('span[data-error="'+ inputId +'"]').removeClass("show");
                                }
                            }
                        });
                        
                        setTimeout(function(e) {
                            loaderAction("hide");
                        }, 1000);
                   
                },
                error:function(data) {
                    $(".lpay_popup-innerbox").attr("data-status", "error")
                    $(".lpay_popup").fadeIn();
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    }, 1000);
                }  
            });
            
        }
    }

    $("body").on("change", "#tf-subMerchant", function(e) {
        var _val = $(this).val();
        $("body").removeClass("loader--inactive");
        if(_val == "") {
            $("#wwgrp_tf-mappedCurrency").addClass("d-none");
            $("#wwgrp_tf-currencyCode").removeClass("d-none");
        } else {
            changeCurrencyMap(_val);
            fetchBeneDetail($(this));
        }
        setTimeout(function(e){
            $("body").addClass("loader--inactive");
        }, 1000)
    });

    $("body").on("change", "#ab-subMerchant", function(e) {   
        if($(this).val() !== "") {
            $("body").removeClass("loader--inactive");
            
            fetchAccountDetail($(this));
        } else {
            $("#ab-bankAccountName").val("");
            $("#ab-bankAccountNumber").val("");
            $("#ab-bankIfsc").val("");
        }
    });

    var fetchBalance = function() {
        $("body").removeClass("loader--inactive");
        var token = document.getElementsByName("token")[0].value;
    
        $.ajax({
            type : 'POST',
            url : 'fetchBalance',
            timeout: 0,
            data : {
                token : token
            },
            success : function(data) {
                if(data.amount !== null) {
                    var dataValue = data.amount;
                    $(".balanceAmount").text(dataValue);
                }

                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                }, 1000);
            },
            error : function(data) {
                alert("Something went wrong, so please try again.");
                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                }, 1000);
            }
        });
    }

    

    $(".checkBalance").on("click", function(e) {
        fetchBalance();
    });

    var getSubMerchant = function(that) {
        var _merchant = that.val(),
            inputId = that.attr("data-name"),
            _subMerchantBox = $("[data-id='"+ inputId +"']"),
            _currentId = that.attr("id"),
            _subMerchantField = $("#" + inputId);
        
        $("#ab-bankAccountName").val("");
        $("#ab-bankAccountNumber").val("");
        $("#ab-bankIfsc").val("");

        $("#tf-bankAccountName").val("");
        $("#tf-bankAccountNumber").val("");
        $("#tf-bankIfsc").val("");

        $('[data-id="tf-bankAccountName"]').addClass("d-none");
        $('[data-id="tf-bankAccountNumber"]').addClass("d-none");
        $('[data-id="tf-bankIfsc"]').addClass("d-none");

        if(_merchant != "") {
            loaderAction("show");
            
            $.ajax({
                type: "POST",
                url: "getSubMerchantListByPayId",
                data: {"payId": _merchant},
                success: function(data) {                    
                    if(data.superMerchant) {
                        _subMerchantField.html("");
                        $("#isSuperMerchant").val("Y");
                        if(_currentId == "ab-merchant" || _currentId == "tf-merchant") {
                            _subMerchantField.append("<option value=''>Select Sub Merchant</option>");
                        } else {
                            _subMerchantField.append("<option value=''>ALL</option>");
                        }
                        
                        for(var i = 0; i < data.subMerchantList.length; i++) {
                            _subMerchantField.append("<option value="+ data.subMerchantList[i]["payId"] +">"+ data.subMerchantList[i]["businessName"] +"</option>");
                        }

                        _subMerchantBox.removeClass("d-none");
                        $("#" + inputId + " option[value='']").attr("selected", "selected");
                        _subMerchantField.selectpicker();
                        _subMerchantField.selectpicker('refresh');
                        if(inputId == "tf-subMerchant") {
                            changeCurrencyMap(_merchant);

                            $("[data-id='tf-bankAccountName']").addClass("d-none");
                            $("[data-id='tf-bankAccountNumber']").addClass("d-none");
                            $("[data-id='tf-bankIfsc']").addClass("d-none");

                            $("#tf-bankAccountName").val("");
                            $("#tf-bankAccountNumber").val("");
                            $("#tf-bankIfsc").val("");
                        }

                        if(inputId == "ab-subMerchant") {
                            var defaultBene = $("[data-id='defaultbene']");
                            defaultBene.addClass("d-none");
                            var checkBox = defaultBene.find(".lpay-input");
                            checkBox.prop("checked", false);
                            checkBox.closest("label").removeClass("lpay_toggle_on");

                            $("#ab-payeeType").val("");
                            $("#ab-payeeType").selectpicker("refresh");
                        }

                        setTimeout(function(e) {
                            loaderAction("hide");
                        },1000);
                    } else {
                        $("#isSuperMerchant").val("N");
                        if(inputId == "ab-subMerchant") {
                            $("body").removeClass("loader--inactive");
                            
                            fetchAccountDetail(that);
                        } else if(inputId == "tf-subMerchant") {
                            fetchBeneDetail(that);
                            changeCurrencyMap(_merchant);
                        } else {
                            setTimeout(function(e) {
                                loaderAction("hide");
                            }, 1000);
                        }

                        if(!_subMerchantBox.hasClass("d-none")) {
                            _subMerchantBox.addClass("d-none");
                            _subMerchantField.val("");
                            _subMerchantField.selectpicker("refresh");
                        }
                    }
                }
            });
        } else {
            $("#isSuperMerchant").val("Y");
            if(!_subMerchantBox.hasClass("d-none")) {
                _subMerchantBox.addClass("d-none");
                _subMerchantField.html("");
               // _subMerchantField.selectpicker("refresh");
            }

            if(_currentId == "tf-merchant") {
                $("#wwgrp_tf-currencyCode").removeClass("d-none");
                $("#wwgrp_tf-mappedCurrency").addClass("d-none");

                $("[data-id='tf-bankAccountName']").addClass("d-none");
                $("[data-id='tf-bankAccountNumber']").addClass("d-none");
                $("[data-id='tf-bankIfsc']").addClass("d-none");

                $("#tf-bankAccountName").val("");
                $("#tf-bankAccountNumber").val("");
                $("#tf-bankIfsc").val("");
            }
        }

        if($("#ab-payeeType").val() == "") {
            $("#ab-payeeType").selectpicker("refresh");
        }
    }

    function changeCurrencyMap(payId) {
        var token = document.getElementsByName("token")[0].value;
        loaderAction("show");
        $.ajax({
            url : 'setMerchantCurrency',
            type : 'post',
            timeout: 0,
            data : {
                payId : payId,
                token : token
            },
            success : function(data) {
                var dataValue = data.currencyMap;
                var currenyMapDropDown = _id("tf-mappedCurrency");
                currenyMapDropDown.classList.remove("d-none");
                
                $("#tf-currencyCode").closest("#wwgrp_tf-currencyCode").addClass("d-none");
                $("#wwgrp_tf-mappedCurrency").removeClass("d-none");
    
                var test = "";
                var parseResponse = '<select>';
                for (index in dataValue) {
                    var key = dataValue[index];
                    parseResponse += "<option value = "+index+">" + key + "</option> ";
                
                }
                parseResponse += '</select>';
                test += key;
                currenyMapDropDown.innerHTML = parseResponse;
                
                $("#tf-mappedCurrency").selectpicker("refresh");
                $("#wwctrl_tf-mappedCurrency").find(".bootstrap-select").removeClass("d-none");

                $("[data-error='tf-mappedCurrency']").removeClass("show");

                loaderAction("hide");

            },
            error : function(data) {
                alert("Something went wrong, so please try again.");
            }
        });
    }

    // var tf_payId=$("#tf-merchant").val();
    // if(tf_payId !== "") {
    //     changeCurrencyMap(tf_payId);
    // }

    // MERCHANT SELECTION
    $(".merchant-selectbox").on("change", function() {
        getSubMerchant($(this));
    });

    // statusEnquiry
    $("body").on("click", ".statusEnquiry", function(e) {
        var _tr = $(this).closest("tr");
        var token = document.getElementsByName("token")[0].value;
        var _accountNumber = _tr.find(".accountNumber").text();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "beneStatusCheck",
            data: {
                "bankAccountNumber": _accountNumber,
                token : token,
                "struts.token.name" : "token",
            },
            success: function(data) {                
                if(data.response == "success") {
                    setTimeout(function(e) {
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                        $(".lpay_popup").fadeIn();
                        $(".responseMsg").text(data.responseMsg);
                        $(".viewData").click();
                        $("body").addClass("loader--inactive");

                    }, 1000);
                } else {
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }
            }
        });
    });

    // checkTransactionStatus

    $("body").on("click", ".checkTransStatus", function(e){
        var _tr = $(this).closest("tr").prev("tr");
        var _txnId = _tr.find(".txnId").text();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "post",
            url: "checkTransactionStatus",
            data: {
                "txnId": _txnId
            },
            success: function(data){
                if(data.response == "success") {
                    setTimeout(function(e) {
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                        $(".lpay_popup").fadeIn();
                        $(".responseMsg").text(data.responseMsg);
                        $("body").addClass("loader--inactive");
                    }, 1000);
                } else {
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }
            }
        })
    })



    $("body").on("click", ".downloadAccountStatement", function(e) {
        var _dateTo = $(this).closest("tr").find(".dateTo").text();
        var _dateFrom = $(this).closest("tr").find(".dateFrom").text();
        var _fileName = $(this).closest("tr").find(".fileName").text();
        var _downloadFileOf = $(this).closest("tr").find(".downloadFileOf").text();
        $("#statementDateFrom").val(_dateTo);
        $("#statementDateTo").val(_dateFrom);
        $("#statementFileName").val(_fileName);
        $("#downloadFileOfInput").val(_downloadFileOf);
        $("#downloadAccountStatement").submit();
    });

    $(".generateAccountStatement").on("click", function(e){
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "generateAccountStatement",
            data: {
                "dateFrom" : $("#as-dateFrom").val(),
                "dateTo" : $("#as-dateTo").val(),
                "downloadFileOf" : $("#downloadFileOf").val(),
                "userType" : $("#reportType").val(),
                "fileType" : $("#fileType").val()
            },
            success: function(data) {                
                setTimeout(function(e) {
                    if(data.status == "Processing"){
                        $(".responseMsg").text("File already in Process.");
                    }else{
                        $(".responseMsg").text("File is processing. Please see in downloads after some time."); 
                    }
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();
                    
                    $("body").addClass("loader--inactive");
                }, 3000);
            }
        })
    })

    function accountStatement(_data){
        document.querySelector("body").classList.remove("loader--inactive");
        if(_data == null){
            var _obj = {
                "dateFrom" : $("#dateFromFilter-statement").val(),
                "dateTo" : $("#dateToFilter-statement").val(),
                "downloadFileOf" : $("#downloadFileOfFilter").val(),
                "userType" : $("#reportTypeFilter").val()
            }
        }else{
            var _obj = _data;
        }
        $("#accountStatement-table").DataTable({
            "destroy": true,
            "order" : [["2", "desc"]],
            "ajax" : {
                "url" : "fetchAccountStatementData",
                "type" : "POST",
                "data" : _obj
            },
            "sAjaxDataProp" : "accountStatementData",
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "aoColumns": [
                { "mData": "dateFrom","className" : "dateFrom" },
                { "mData": "dateTo", "className" : "dateTo" },
                { "mData": "fileFor", "className" : "downloadFileOf"},
                { "mData": "createDate" },
                { "mData": "status" },
                { "mData": "fileName", "className" : "fileName"},
                { 
                   "mData": null,
                   "mRender" : function(row){
                       if(row.status == "Processing"){
                           return "<button class='lpay_button lpay_button-md lpay_button-primary refreshTable-data'><span class='glyphicon glyphicon-repeat'></span>Processing</button>"
                    }else if(row.status == "Failed"){
                        return "";
                    }else{
                        return "<button class='lpay_button lpay_button-md lpay_button-primary downloadAccountStatement'>Download</button>";
                    }
                   }
                },
            ]
        })
    }

    accountStatement(null);

    $("#dateFromFilter-statement, #dateToFilter-statement, #downloadFileOfFilter, #reportTypeFilter").on("change", function(e){
        accountStatement(null);
    })

    $(".viewData-refresh").on("click", function(e){
        accountStatement(null);
    })

    $("body").on("click", ".downloadBulkFile", function(e) {
        var that = $(this),
            fileType = that.attr("data-type"),
            table = $("#" + fileType + "-table").DataTable(),
            row = that.closest("tr");
            rowData = table.rows(row).data(),
            fileName = rowData[0].fileName;

        $("#bulk-downloadFileOf").val(fileType);
        $("#bulk-fileName").val(fileName);

        $("#downloadBulkFile").submit();
    });

    $("body").on("click", ".refreshTable-data", function(e){
        var _table = $("#accountStatement-table").DataTable();
        var _row = $(this).closest("tr");
        var _data = _table.rows(_row).data();
        var data = {
            "dateFrom" : _data[0]['dateFrom'],
            "dateTo" : _data[0]['dateTo'],
            "downloadFileOf" : _data[0]['fileFor'],
            "fileName" : _data[0]['fileName']
        }
        // accountStatement(data);
        $.ajax({
            type: "POST",
            url: "fetchAccountStatementFileStatus",
            data: data,
            success: function(data){
                if(data.fileStatus == "Processing"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text("File is Processing");
                }else if(data.fileStatus == "Ready"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text("File Ready");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text("File Not Found"); 
                }
                $(".lpay_popup").fadeIn();
            }
        })
        
    })

    $(".downloadTranData").on("click", function(e) {        
        var payId = $("#st-payId").val(),
            subMerchant = $("#st-subMerchant").val(),
            isSubMerchant = $('[data-id="st-subMerchant"]').hasClass("d-none");

        if(subMerchant == "" && !isSubMerchant) {
            subMerchant = "ALL";
        } else if(subMerchant == "" && isSubMerchant) {
            subMerchant = "";
        }
        
        $("#tranDateFrom").val($("#st-dateFrom").val());
        $("#tranDateTo").val($("#st-dateTo").val());
        $("#tranStatus").val($("#st-status").val());
        $("#tranTxnId").val($("#st-txnId").val());
        $("#tranUtr").val($("#st-utrNo").val());
        $("#tranPayeeType").val($("#st-payeeType").val());
        $("#tranpayId").val(payId);
        $("#tranSubMerchantPayId").val(subMerchant);
        $("#tranAccountNumber").val($("#st-bankAccountNumber").val());
        $("#downloadTranReport").submit();
    });

    $(".downloadBeneReport").on("click", function(e) {
        var payId = $("#sb-payId").val();
        $("#beneDateFrom").val($("#sb-dateFrom").val());
        $("#beneDateTo").val($("#sb-dateTo").val());
        $("#beneStatus").val($("#sb-status").val());
        $("#benePayeeType").val($("#sb-payeeType").val());
        $("#benePayId").val(payId);
        $("#beneSubMerchant").val($("#sb-subMerchant").val());
        $("#beneAccountNumber").val($("#sb-bankAccountNumber").val());
        // return false;
        $("#downloadBeneReport").submit();
    });

    var updateDateField = function(container, dateInput) {
        var $container = $('[data-target="'+ container +'"]'),
            today = new Date(),
            dateField = $container.find('[name="'+ dateInput +'"]');

        dateField.datepicker({
            prevText : "click for previous months",
            nextText : "click for next months",
            showOtherMonths : true,
            changeMonth : true,
            changeYear : true,
            dateFormat : 'dd-mm-yy',
            selectOtherMonths : false,
            maxDate : new Date()
        });

	    dateField.val($.datepicker.formatDate('dd-mm-yy', today));
    }

    var getFormattedDate = function(container, dateId) {
        return container.find('[name="'+ dateId +'"]').val();
    }

    var getCompleteDate = function(container, dateId) {
        return $.datepicker.parseDate('dd-mm-yy', getFormattedDate(container, dateId));
    }

    var validateBulkDate = function(container) {
        if (getCompleteDate(container, 'dateFrom') > getCompleteDate(container, 'dateTo')) {
            alert('From date must be before the to date');
            container.find('#dateFrom').focus();
            return false;
        } else if (getCompleteDate(container, 'dateTo') - getCompleteDate(container, 'dateFrom') > 7 * 86400000) {
            alert('No. of days can not be more than 7');
            container.find('#dateFrom').focus();
            return false;
        }

        return true;
    }

    var fetchBulkTableData = function(container, fileType) {
        var $container = $('[data-target="'+ container +'"]');
        if(validateBulkDate($container)) {
            loaderAction("show");

            $("#" + fileType + "-table").dataTable({
                "ajax": {
                    "type": "post",
                    "url": "bulkNodalFileList",
                    "data" : {
                        "downloadFileOf" : fileType,
                        "dateFrom" : getFormattedDate($container, 'dateFrom'),
                        "dateTo" : getFormattedDate($container, 'dateTo')
                    }
                },
                "destroy": true,
                "sAjaxDataProp" : "downloadBulkNodalListItems",
                "aoColumns": [                
                     {"mData" : "createDate"},
                     {"mData" : "fileName"},
                     {
                         "mData" : null,
                         "mRender": function(row) {
                             return '<button class="lpay_button lpay_button-md lpay_button-secondary downloadBulkFile" data-type="'+ fileType +'">Download</button>'
                         }
                     }            
                ]
            });

            loaderAction("hide");
        }
    }

    // tab creation 
    $(".lpay-nav-link").on("click", function(e) {
        e.preventDefault();
        var _this = $(this).attr("data-id");
        $(".lpay-nav-item").removeClass("active");
        $(this).closest(".lpay-nav-item").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+_this+"]").removeClass("d-none");

        if(_this == "search-beneficiary") {
            resetInputs("[data-target='search-beneficiary']", {
                input: true,
                select: true
            })
        } else if(_this === "bulkTransfer") {
            updateDateField(_this, "dateFrom");
            updateDateField(_this, "dateTo");
            fetchBulkTableData(_this, "PAYOUT_FILE");
        } else if(_this === "bulkUTR") {
            updateDateField(_this, "dateFrom");
            updateDateField(_this, "dateTo");
            fetchBulkTableData(_this, "UTR_FILE");
        }
    });

    $(".bulk-filter").on("click", function(e) {
        var that = $(this).closest(".lpay_tabs_content").attr("data-target");

        if(that === "bulkTransfer") {
            fetchBulkTableData(that, "PAYOUT_FILE");
        } else if(that === "bulkUTR") {
            fetchBulkTableData(that, "UTR_FILE");
        }
    });

    function checkBoxChecked(_selector){
        var _getAllCheck = document.querySelectorAll(_selector);
        _getAllCheck.forEach(function(index, array, element){
            _getAllCheck[array].addEventListener('click', function(e){
                var _this = e.target.id;
                var _isChecked = e.target.checked;
                if(_isChecked){
                    document.querySelector("#"+_this).closest("label").classList.add("checkbox-checked");
                }else{
                    document.querySelector("#"+_this).closest("label").classList.remove("checkbox-checked");
                }
            })
        })
    }    
    
    checkBoxChecked(".bulkUtrCheckbox");

    function resetInputs(_selector, _obj){
        var _parent = document.querySelector(_selector);
        if(_obj.input){
            var _input = _parent.querySelectorAll("input");
            _input.forEach(function(index, array, element){
                if(index.id == "sb-dateFrom" || index.id == "sb-dateTo"){
                }else{
                    index.value = "";
                }
            })
        }
        if(_obj.select){
            var _select = _parent.querySelectorAll("select");
            _select.forEach(function(index, array, elment){
                if(index.classList.toString().indexOf("selectpicker") != -1){
                    $("#"+index.id).val('default');
                    $("#"+index.id).selectpicker("refresh");
                }
            })
        }
        handleChange();
    }

    

    $(".viewData").on("click", function(e) {
        $("body").removeClass("loader--inactive");
        handleChange();
        setTimeout(function(e) {
            $("body").addClass("loader--inactive");
        }, 1500);
    });

    var _userType = document.querySelector("#userType").value;
    if(_userType != "MERCHANT"){
        fetchBalance();
        handleChange();
    }

    if(_userType == "MERCHANT"){
        getSubMerchant($("#tf-merchant"));
        fetchMerchantBalance(document.querySelector("#tf-merchant"));
        $(".lpay_tabs_content").addClass("d-none");
        $('[data-target="transfer-funds"]').removeClass("d-none");
    }

    function fetchMerchantBalance(_this){
        var _payId = _this.value;
        $.ajax({
            type : "POST",
            url : "nodalPayoutBalance",
            data : {
                "payId" : _payId
            },
            success: function(data){
                document.querySelector(".tf-total_amount span").innerText = data.topupBalance.TOTAL_BALANCE;
                document.querySelector(".tf-total_available_amount span").innerText = data.topupBalance.AVAILABLE_BALANCE;
                document.querySelector(".tf-balance_sheet").classList.remove("d-none");
            }
        });
     }


    function handleChange() {
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#sb-dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#sb-dateTo').val());
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            $('#sb-dateFrom').focus();
            return false;
        }
        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            $('#sb-dateFrom').focus();
            return false;
        }

        $("#datatable").dataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "ajax": {
                "type": "post",
                "url": "fetchBene",
                "data" : function (d){
                        return generatePostData(d);
                    }
                },
                "destroy": true,
                "initComplete" : function(setting,json){
                    //hideColumn();
                },
               
                "order": [[ 2, 'desc' ]],
                "aoColumns": [
                {"mDataProp": "merchantName"},
                {
                    "mData" : "bankAccountNumber",
                    "className" : "accountNumber"
                },
                {"mData" : "createDate"},
                {"mData" : "status"},
                {
                    "mData" : null,
                    "mRender" : function(row) {
                        if(row.defaultBene && (row.status == "SUCCESS" || row.status == "Processing")) {
                            return '<label class="lpay_toggle lpay_toggle_on" style="pointer-events: none;"><input type="hidden" id="defaultPayId" value="'+row.payId+'" /><input type="checkbox" class="lpay-input btn-change-default" name="defaultbene" id="checkbox-'+ row.bankAccountNumber +'" data-toggle="toggle" checked /></label>';
                        } else if(row.status == "Failed" || row.status == "Rejected") {
                            return 'NA';
                        } else {
                            return '<label class="lpay_toggle"><input type="hidden" id="defaultPayId" value="'+row.payId+'" /><input type="checkbox" class="lpay-input btn-change-default" name="defaultbene" id="checkbox-'+ row.bankAccountNumber +'" data-toggle="toggle" /></label>';
                        }
                    }
                },
                {
                    "mData" : null,
                    "mRender" : function(row) {
                        if(row.status == "Processing") {
                            return "<button class='lpay_button lpay_button-md lpay_button-secondary statusEnquiry'>Get Status</button>"
                        } else {
                            return "<span>NA</span>"
                        }
                    }
                } 
                                      
            ]
        });
    }

    $(".st-viewData").on("click", function(e) {
        $("body").removeClass("loader--inactive");

        searchTransaction();

        setTimeout(function(e) {
            $("body").addClass("loader--inactive");
        }, 1000);
    });

    searchTransaction();

    function searchTransaction() {
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#st-dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#st-dateTo').val());
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            $('#st-dateFrom').focus();
            return false;
        }
        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            $('#st-dateFrom').focus();
            return false;
        }

        $("#st-datatable").dataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            
            "ajax": {
                "type": "post",
                "url": "fetchTransaction",
                "data" : function (d) {
                    return generateSearchData(d);
                }
            },
            "destroy": true,
            "initComplete" : function(setting,json){
                //hideColumnForTransaction();
            },
            "order": [[ 2, 'desc' ]],
            "sAjaxDataProp" : "tranData",
            "aoColumns": [
                {
                    "mDataProp": "merchantName",
                    "className": "my_class"
                },
                {"mData" : "txnId", "className": "txnId"},
                {"mData" : "createDate"}, 
                {"mData" : "utrNo"}, 
                {"mData" : "amount"},
                {"mData" : "status"}
                
            ]
        });
    }

    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        _check = _currentTable;
		if(e.target.localName != "button" && e.target.localName != "input" && e.target.localName != "label"){
			var tr = $(this).closest('tr');
			var row = table.row(tr);
			if ( row.child.isShown() ) {
				tr.removeClass('shown');
				setTimeout(function(e){
					row.child()[0].children[0].classList.remove("active-row");
					row.child.hide();
				}, 600)
			}
			else {
				row.child( format(row.data()) ).show();
				row.child()[0].children[0].classList.add("active-row");
				getAllData();
				tr.addClass('shown');
			}
		}
		
	})

	function getAllData(){
		var _new = document.querySelectorAll(".inner-div");
		_new.forEach(function(index, array, element){
			var _getValue = _new[array].children[1].innerText;
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined' || _getValue == 'NA') {
				_new[array].classList.add("d-none");
			}
		})
	}

    // variable sent to backend function
    function generatePostData(d) {
        var obj = {
            bankAccountNumber : $("#sb-bankAccountNumber").val(),
            payId : $("#sb-payId").val(),
            subMerchant : $("#sb-subMerchant").val(),
            payeeType : $("#sb-payeeType").val(),
            status : $("#sb-status").val(),
            dateFrom : $("#sb-dateFrom").val(),
            dateTo : $("#sb-dateTo").val(),
            token : $("[name=token]").val(),
            "struts.token.name" : "token",
        };
        return obj;
    }

    function generateSearchData(d) {
        
        var obj = {
            txnId : $("#st-txnId").val(),
            utrNo : $("#st-utrNo").val(),
            payId : $("#st-payId").val(),
            subMerchant : $("#st-subMerchant").val(),
            status : $("#st-status").val(),
            dateFrom : $("#st-dateFrom").val(),
            dateTo : $("#st-dateTo").val(),
            token : $("[name=token]").val(),
            "struts.token.name" : "token",
        };

        return obj;
    }

    $(".confirmButton").on("click", function(e) {
        var _parentId = $(".lpay-nav-item.active a").attr("data-id");

        if(_parentId == "account-statement"){
            accountStatement(null);
        }

        if(_userType != "MERCHANT"){
            $(".lpay_tabs_content[data-target='"+ _parentId +"'] .lpay-input").each(function() {
                if($(this).prop("tagName") !== "DIV") {
                    var _currentId = $(this).attr("id");
                    if(_currentId == "ab-defaultbene") {
                        $(this).closest("[data-id='defaultbene']").addClass("d-none");
                        $(this).prop("checked", false);
                        $(this).closest("label").removeClass("lpay_toggle_on");
                    }
                    
                    if($(this).hasClass("datepick")) {
                        $(this).val($.datepicker.formatDate('dd-mm-yy', today));
                    } else {
                        $(this).val("");
                    }
    
                    if($(this).prop("tagName") == "SELECT") {
                        var _id = $(this)[0].id;
                        if(_id != "tf-mappedCurrency"){
                            $(this).selectpicker("refresh");
                        }
                    }
    
                    if(_parentId == "transfer-funds") {
                            $("#tf-currencyCode").closest("#wwgrp_tf-currencyCode").removeClass("d-none");
                            $("#wwgrp_tf-mappedCurrency").addClass("d-none");
                        
    
                        $("[data-id='tf-bankAccountName']").addClass("d-none");
                        $("[data-id='tf-bankAccountNumber']").addClass("d-none");
                        $("[data-id='tf-bankIfsc']").addClass("d-none");
                    }
    
                    if(_parentId == "update-utr") {
                        $("utr-utrNumber").val("");
                        $("[data-target='utr-subMerchant']").addClass("d-none");
                    }
                }
            });
    
            if(_parentId == "transfer-funds") {
                $("#btn-transfer-funds").attr("disabled", false);
            }
    
            var _merchant = $(".lpay_tabs_content[data-target='"+ _parentId +"'] .merchant-selectbox");
    
            if(_merchant.val() == "") {
                var _dataName = _merchant.attr("data-name");
                $("div[data-id='"+ _dataName +"']").addClass("d-none");
            }
        }else{
            document.querySelector("#tf-amount").value = "";
            document.querySelector("#tf-remarks").value = "";
            $("#tf-txnType").val('default');
            $("#tf-txnType").selectpicker('refresh');
            document.querySelector("#tf-amountShow").innerText = "";
            fetchMerchantBalance(document.querySelector("#tf-merchant"));
        }
        

        $(".lpay_popup").fadeOut();
    });

    // TOGGLE CHANGE
    $("body").on("change", ".lpay_toggle", function(e) {
        var _checkbox = $(this).find(".lpay-input"),
            _isChecked = _checkbox.is(":checked"),
            _label = $(this).closest("label");

        if(_isChecked) {
            _label.addClass("lpay_toggle_on");
            _checkbox.prop("checked", true);
        } else {
            _label.removeClass("lpay_toggle_on");
            _checkbox.prop("checked", false);
        }
    });

    $("body").on("change", ".btn-change-default", function() {
        $("body").removeClass("loader--inactive");
        var _parent = $(this).closest("tr"),
            payId = _parent.find("#defaultPayId").val(),
            accountNumber = _parent.find("td.accountNumber").text();

        var token = document.getElementsByName("token")[0].value;

        var payload = {
            bankAccountNumber: accountNumber,
            payId: payId,
            token : token,
            "struts.token.name" : "token"
        }

        var response_changeDefault = function(obj) {
            if(obj.response== "success") {
                handleChange();
            } else {
                handleChange();
            }
            setTimeout(function(e){
                loaderAction("hide");
            }, 1000);
        }

        // ajaxRequest({
        //     method: 'POST',
        //     actionName: 'setDefaultBene', // action name
        //     payload: payload, // payload object
        //     success: function(resObj) {
        //         response_changeDefault(resObj); // response method name
        //     }
        // });
        $.ajax({
            type: "POST",
            url: 'setDefaultBene',
            data: payload,
            success: function(data) {
                response_changeDefault(data); // response method name
            }
        });
    });

    var createList = function(element) {
        var _val = element.val();
        if(_val.length > 0) {
            return _val.join();
        } else {
            // _val = element.find("option").map(function() {return $(this).val();}).get();
            // return _val.join();

            return "ALL";
        }
    }

    $("#utr-submit").on("click", function(e) {
        var utrNumber = $("#utr-utrNumber").val(),
        merchant = $("#utr-payId").val();

        if(merchant == ""){
            displayErrorMsg({
                errorMessage: "Please select any merchant",
                elementId: "utr-payId"
            });
        } else if(utrNumber == "") {
            displayErrorMsg({
                errorMessage: "Please enter UTR No.",
                elementId: "utr-utrNumber"
            });
        } else {
            var dateFrom = $("#utr-dateFrom").val(),
                dateTo = $("#utr-dateTo").val(),
                payoutDate = $("#utr-payoutDate").val(),            
                acquirer = $("#utr-acquirer"),
                paymentType = $("#utr-paymentType"),
                mopType = $("#utr-mopType"),
                subMerchantId = $("#utr-subMerchant").val();
    
            $("body").removeClass("loader--inactive");
    
            $.ajax({
                type: "POST",
                url: "updateUtrNoAction",
                data: {
                    payId: merchant,
                    acquirerCode: createList(acquirer),
                    paymentType: createList(paymentType),
                    mopType: createList(mopType),
                    utrNo: utrNumber,
                    dateFrom: dateFrom,
                    dateTo: dateTo,
                    payOutDate: payoutDate,
                    subMerchantId: subMerchantId
                },
                success: function(data) {
                    if(data.response == "success") {
                        ajaxErrorMsg(data.responseMsg, "success");
                    } else {
                        ajaxErrorMsg(data.responseMsg, "error");
                    }
                },
                error: function() {
                    ajaxErrorMsg("Try again, Something went wrong!", "error");
                }
            });
        }
    });

    function createJsonVariable(){
        var _select = document.querySelectorAll("[data-topup]");
        var _obj = {};
        _select.forEach(function(index, array, element){
            _obj[index.getAttribute('data-topup')] = index.value;
        })
        _obj['token'] = document.getElementsByName("token")[0].value;
        return _obj;
    }

    

    function topupTableData(){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#topupReport-table").dataTable({
            "ajax" : {
                "url" : "topUpTxnReportAction",
                "type" : "POST",
                "data" : createJsonVariable(),
            },
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "destroy" : true,
            "sAjaxDataProp" : "topUpTranData",
            "aoColumns" : [
                { "mData" : "merchantName", "width" : "25%" },
                { "mData" : "subMerchantName", "width" : "15%" },
                { "mData" : "paymentType", "width" : "15%" },
                { "mData" : "mopType", "width" : "10%" },
                { "mData" : "amount", "width" : "10%" },
                { "mData" : "createDate", "width" : "15%" },
                { "mData" : "status", "width" : "10%" }
            ]
        })
    }

    topupTableData();

    $("#topup-view").on("click", function(e){
        topupTableData();
    })

    $("#topup-downloand").on("click", function(e){

        var _downloadParam = createJsonVariable();
        document.querySelector("#topup_download").innerHTML = "";
        for(key in _downloadParam){
            var _inputs = "<input type='hidden' name='"+key+"' value='"+_downloadParam[key]+"' />";
            document.querySelector("#topup_download").innerHTML += _inputs;
        }
        document.querySelector("#topup_download").submit();

    })

});

function getSubMerchantList(_this, _url, _object, _selectLabel, _selectValue){
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

document.querySelector("#utr-payId").addEventListener("change", function(_this){
    getSubMerchantList(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

document.querySelector("#tr-merchant").addEventListener("change", function(_this){
    getSubMerchantList(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});


function keyPress(_this){
    var _newValue = parseFloat(_this.value);
    _this.closest(".position-relative").querySelector(".amount-show").innerHTML = wordify(_newValue);
}
// console.log(wordify(12));