// GLOBAL BINDING
const _id = document.getElementById.bind(document),
    _selector = document.querySelector.bind(document),
    _selectorAll = document.querySelectorAll.bind(document),
    _createElement = document.createElement.bind(document);

// LOADER ACTION
var loaderAction = function(action) {
    if(action == "show") {
        _selector("body").classList.remove("loader--inactive");
    } else if(action == "hide") {
        _selector("body").classList.add("loader--inactive");
    }
}

// AJAX ERROR MESSAGE
var ajaxErrorMsg = function(msg) {
    // error_snackbar.innerHTML = msg;

    _selector(".responseMsg").innerHTML = msg;

    _selector(".lpay_popup-innerbox").setAttribute("data-status", "error");
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
    var _userType = $("#userType").val();
    var _userLogin = $("#setSuperMerchant").val();

    var _isSuperMerchant = $("#isSuperMerchant").val();
    var _table = $("#st-datatable").DataTable();


    if(_userLogin == "true"){

        _isSuperMerchant = "Y"

    }

    console.log(_userLogin);
    console.log(_userType);

    if(_userType == "MERCHANT" && _userLogin == "true"){

console.log("isha");
        _table.columns(1).visible(true);
        _table.columns(17).visible(false);

    }

    if(_userType == "MERCHANT" && _userLogin == "false"){

        console.log("kiren");
        _table.columns(1).visible(false);
        _table.columns(17).visible(false);

    }
    
    
    if(_isSuperMerchant == "N"){

        _table.columns(1).visible(false);
    }
}
$(document).ready(function(e) {    
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
                if(!isBeneAvailable) {
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

    var isBeneAvailable = false;

    var fetchBeneDetail = function(that) {
        var _val = that.val();

        if(_val !== "") {
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
                    } else {
                        $("#btn-transfer-funds").prop("disabled", true);

                        setTimeout(function(e) {
                            $(".lpay_popup-innerbox").attr("data-status", "error");
                            $(".responseMsg").text("Beneficiary not available!");
                            $(".lpay_popup").fadeIn();
                            $("body").addClass("loader--inactive");
                        }, 1000);

                        isBeneAvailable = false;
                    }

                    setTimeout(function(e){
                        loaderAction("hide");
                    }, 1000);
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



    

    $(".checkBalance").on("click", function(e) {
        fetchBalance();
    });

    function changeCurrencyMap(payId) {
        var token = document.getElementsByName("token")[0].value;
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
            },
            error : function(data) {
                alert("Something went wrong, so please try again.");
            }
        });
    }

    

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
        var _tr = $(this).closest("tr");
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

    $(".downloadAccountStatement").on("click", function(e) {
        $("#statementDateFrom").val($("#as-dateFrom").val());
        $("#statementDateTo").val($("#as-dateTo").val());
        $("#downloadAccountStatement").submit();
    });

    $(".downloadTranData").on("click", function(e) {
        
        $("#tranPayId").val($("#st-payId").val());
        $("#transubPayId").val($("#st-subMerchant").val());
        
        $("#tranDateFrom").val($("#st-dateFrom").val());
        $("#tranDateTo").val($("#st-dateTo").val());
        $("#tranStatus").val($("#st-status").val());
        $("#tranTxnId").val($("#st-txnId").val());
        $("#tranUtr").val($("#st-utrNo").val());
        $("#tranPayeeType").val($("#st-payeeType").val());
        $("#tranAccountNumber").val($("#st-bankAccountNumber").val());
        $("#downloadTranReport").submit();
    });

    $(".downloadBeneReport").on("click", function(e) {
        
        var payId = $("#sb-payId").val();
        var subMerchant = $("#sb-subMerchant").val();

        // console.log("payId: " + payId);
        // console.log("subMerchant: " + subMerchant);

        var benePayId = "";

        if(subMerchant !== null) {
            benePayId = subMerchant;
        } else {
            benePayId = payId;
        }

        // console.log("benePayId: " + benePayId);
        
        $("#beneDateFrom").val($("#sb-dateFrom").val());
        $("#beneDateTo").val($("#sb-dateTo").val());
        $("#beneStatus").val($("#sb-status").val());
        $("#benePayeeType").val($("#sb-payeeType").val());
        $("#benePayId").val(benePayId);
        $("#beneAccountNumber").val($("#sb-bankAccountNumber").val());
        $("#downloadBeneReport").submit();
    });

    // tab creation 
    $(".lpay-nav-link").on("click", function(e) {
        e.preventDefault();
        var _this = $(this).attr("data-id");
        $(".lpay-nav-item").removeClass("active");
        $(this).closest(".lpay-nav-item").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+_this+"]").removeClass("d-none");
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

    $(".viewData").on("click", function(e) {
        $("body").removeClass("loader--inactive");
        handleChange();
        setTimeout(function(e) {
            $("body").addClass("loader--inactive");
        }, 1500);
    });
    
    // handleChange();

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
                    hideColumn();
                },
               
                "order": [[ 2, 'desc' ]],
                "aoColumns": [
                {"mDataProp": "merchantName"},
                {"mData" : null,
                "mRender" : function(row){
                        if(row.subMerchantName != null){
                            
                            return row.subMerchantName;
                        }else{
                            return "<span>NA</span>"
                        }
                    }
                },
                {"mData" : "alias"},
                {
                    "mData" : "payId",
                    "className" : "payId"
                }, 
                {"mData" : "createDate"}, 
                {"mData" : "bankAccountName"}, 
                {
                    "mData" : "bankAccountNumber",
                    "className" : "accountNumber"
                }, 
                {"mData" : "bankIfsc"}, 
                {"mData" : "payeeType"}, 
                {"mData" : "status"},
                {
                    "mData" : null,
                    "mRender" : function(row) {
                        if(row.defaultBene && (row.status == "Success" || row.status == "Processing")) {
                            return '<label class="lpay_toggle lpay_toggle_on" style="pointer-events: none;"><input type="checkbox" class="lpay-input btn-change-default" name="defaultbene" id="checkbox-'+ row.bankAccountNumber +'" data-toggle="toggle" checked /></label>';
                        } else if(row.status == "Failed" || row.status == "Rejected") {
                            return 'NA';
                        } else {
                            return '<label class="lpay_toggle"><input type="checkbox" class="lpay-input btn-change-default" name="defaultbene" id="checkbox-'+ row.bankAccountNumber +'" data-toggle="toggle" /></label>';
                        }
                    }
                },
                {"mData" : "responseMsg"},
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
                $("#setSuperMerchant").val(json.flag);
                hideColumnForTransaction();
            },
            "order": [[ 2, 'desc' ]],
            "sAjaxDataProp" : "tranData",
            "aoColumns": [
                {
                    "mDataProp": "merchantName",
                    "className": "my_class"
                },
                {"mData" : null,
                "mRender" : function(row){
                        if(row.subMerchantName != null){
                            
                            return row.subMerchantName;
                        }else{
                            return "<span>NA</span>"
                        }
                    }
                },
                {"mData" : "createDate"}, 
                {"mData" : "txnId", "className": "txnId"},
                {"mData" : "capturedDateFrom"}, 
                {"mData" : "capturedDateTo"}, 
                {"mData" : "utrNo"},
                {"mData" : "payId"},
                {"mData" : "bankAccountName"}, 
                {"mData" : "bankAccountNumber"}, 
                {"mData" : "bankIfsc"}, 
                {"mData" : "currency"},
                {"mData" : "txnType"}, 
                {"mData" : "amount"},
                {"mData" : "status"},
                {"mData" : "responseMsg"},
                {"mData" : "remarks"},
                {
                    "mData" : null,
                    "mRender" : function(row) {
                        if(row.status == "Processing") {
                            return "<button class='lpay_button lpay_button-md lpay_button-secondary checkTransStatus'>Get Status</button>"
                        } else {
                            return "<span>NA</span>"
                        }
                    }
                }
            ]
        });
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
        var _currentId = $(".lpay-nav-item.active a").attr("data-id");
        
        $(".lpay_tabs_content[data-target='"+ _currentId +"'] .lpay-input").each(function() {
            if($(this).prop("tagName") !== "DIV") {
                var _currentId = $(this).attr("id");
                if(_currentId == "ab-defaultbene") {
                    $(this).closest("[data-id='defaultbene']").addClass("d-none");
                    $(this).prop("checked", false);
                    $(this).closest("label").removeClass("lpay_toggle_on");
                }
                
                $(this).val("");
                if($(this).prop("tagName") == "SELECT") {
                    $(this).selectpicker("refresh");
                }
            }
        });

        var _merchant = $(".lpay_tabs_content[data-target='"+ _currentId +"'] .merchant-selectbox");

        if(_merchant.val() == "") {
            var _dataName = _merchant.attr("data-name");
            $("div[data-id='"+ _dataName +"']").addClass("d-none");
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
            payId = _parent.find("td.payId").text(),
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
});