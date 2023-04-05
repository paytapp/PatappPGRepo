// FLAG FOR REPORT TABLE


// set Attribute
function createAttr(_selector, _attribute, _attrValue){
    var _attr = document.createAttribute(_attribute);
    _attr.value = _attrValue;
    document.querySelector(_selector).setAttributeNode(_attr);
}

// get sub merchant
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
                        createAttr("#"+_subMerchantAttr, "data-required", "true");
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

var _checkSubMerchant = false;

document.querySelector("#merchant").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchant").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(_this);
        }
    }, 500);
});

document.querySelector("#subMerchant").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
})

document.querySelector("#merchantPayIdBulk").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchantBulk").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(_this);
        }
    }, 500);
});

document.querySelector("#merchantPayIdBulkNeft").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchantBulkNeft").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(_this);
        }
    }, 500);
});


document.querySelector("#merchantPayIdBulkRtgs").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchantBulkRtgs").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(_this);
        }
    }, 500);
});


document.querySelector("#subMerchantBulk").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
})

document.querySelector("#merchantPayIdBulkUpi").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchantBulkUpi").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(_this);
        }
    }, 500);
});

document.querySelector("#merchantComposite").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchantComposite").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(_this);
        }
    }, 500);
});

document.querySelector("#subMerchantBulkUpi").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
})

document.querySelector("#subMerchantBulkRtgs").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
})

document.querySelector("#subMerchantBulkNeft").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
})



document.querySelector("#merchantPayIdLedgerFilter").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
    setTimeout(function(f){
        var _checkSubMerchant = document.querySelector("#subMerchantLedgerFilter").closest(".single-account-input").classList.toString();
        console.log(_checkSubMerchant);
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(e);
        }
    }, 800);
});

document.querySelector("#subMerchantBulkUpi").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
})

document.querySelector("#merchantPayIdLedger").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
    setTimeout(function(f){

        var _checkSubMerchant = document.querySelector("#subMerchantLedger").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            checkMerchantAllow(e);
        }
    }, 800);
    checkBalance(".check-balance-div", "#ledgerAmount");
});

document.querySelector("#subMerchantLedger").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    // checkMerchantAllow(_this);
    checkBalance(".check-balance-div", "#ledgerAmount");
})

document.querySelector("#merchantTopup").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchantTopup").closest(".single-account-input").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            // checkMerchantAllow(_this);
        }
        checkBalance("[data-active='topup']", ".totalAmount");
    }, 500);
});

document.querySelector("#subMerchantTopup").addEventListener("change", function(_this){
    _checkSubMerchant = true;
    checkMerchantAllow(_this);
    checkBalance("[data-active='topup']", ".totalAmount");
})

// tab function




function checkRegEx(e){
    var _getRegEx = e.getAttribute("data-regex");
    var _newRegEx = new RegExp(_getRegEx);
    var _value = e.value;
    if(_value != ""){
        if(_newRegEx.test(_value) != true){
            var _getLabel = e.closest(".single-account-input").querySelector("label").innerText;
            e.closest(".single-account-input").querySelector(".error-field").innerText = "Invalid "+_getLabel;
            e.closest(".single-account-input").classList.add("hasError");
        }
    }else{
        e.closest(".col-md-3").classList.remove("has-error");
    }
}

function removeError(e){
    e.closest(".single-account-input").classList.remove("hasError");
}


function createJsonData(_selector){
    var _obj = {};
    var _parent = document.querySelector(_selector);
    var _allInput = _parent.querySelectorAll("[data-var]");
    var _checkbox = _parent.querySelectorAll("[data-set]");
    if(_checkbox !== null){
        _checkbox.forEach(function(index, array, element){
            if(index.checked == true){
                var _getAttr = index.attributes['name'].nodeValue;
                _obj[_getAttr] = true;
                _obj['txnType'] = index.getAttribute('data-checked');
            }
        })
    }
    _allInput.forEach(function(index, array, element){
        var _getAttr = index.attributes['data-var'].nodeValue;
        var _closest = index.closest(".d-none");
        if(index.attributes['data-required'] != null){
            var _required = index.attributes['data-required'].nodeValue;
        }else{
            var _required = "false"; 
        }
        if(_closest == null || index.value != ""){
            if(index.value == "" && _required == "true"){
                if(index.closest(".single-account-input").querySelector(".error-field") != null){
                    index.closest(".single-account-input").classList.add("hasError");
                    index.closest(".single-account-input").querySelector(".error-field").innerText = "Should not be blank";
                }
            }else{
                index.closest(".single-account-input").classList.remove("hasError");
                _obj[_getAttr] = index.value;
            }
        }
    })
    return _obj;
}

function payFunction(){

    var _obj = createJsonData("[data-active].active-block");
    var _getKeys =  Object.keys(_obj).toString();
    if(_getKeys.indexOf("bankAccountNumber") == -1){
        var _url = "getUPITransactionAction";
    }else{
        var _url = "merchantInitiatedDirectAction";
    }

    var _checkError = document.querySelector("[data-active]").querySelector(".hasError");

    if(_checkError == null){

        document.querySelector("body").classList.remove("loader--inactive");
    
        $.ajax({
            type: "POST",
            url: _url,
            data: _obj,
            success: function(data){
                
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }
    
                function checkPopUp(){
                    var _checkNull = document.querySelector(".lpay_popup");
                    // console.log(_checkNull);
                    if(_checkNull.style.display == 'block'){
                        document.querySelector("body").classList.add("loader--inactive");
                        clearInterval(_stopLoader);
                    }
                }
                var _stopLoader = setInterval(checkPopUp, 500);
            }
        })
    }

}



function checkMerchantAllow(_that){
    

    if(_checkSubMerchant == true){
        var _obj = {
            "subMerchantPayId" : _that.target.value,
        }
    }else{
        var _obj = {
            "payId" : _that.target.value,
        }
    }
        $.ajax({
            type: "POST",
            url: "getMerchantInitiatedDirectUserAction",
            data: _obj,
            success: function(data){
                _checkSubMerchant = false;
                if(data.response == "failed"){
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }
            }
        })
}

// checkbox
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


checkBoxChecked(".signleAccountFlag");

var amount = function(event, _this){
    var _val = _this.value;

    if(_val.length == 1) {
        if(_val.indexOf("0") != -1) {
            _this.value = _val.slice(0, _val.length - 1);
        } else if(_val.indexOf(".") != -1) {
            _this.value = _val.slice(0, _val.length - 1);
        }
    }

    var regex = /[.]/g;
    var _getPeriod = _val.match(regex);
    if(_getPeriod != null){
        if(_getPeriod.length > 1){
            _this.value = _val.slice(0, _val.length-1);
        }
        var _getString = _val.slice(_val.indexOf("."));
        if(_getString.length > 3){
            _this.value = _val.slice(0, _val.length-1);
        }
    }
}

function roundOf(_that){

    var _value = _that.value;
    if(_value.indexOf(".") != -1){
        var _num = Number(_value);
        _that.value = _num.toFixed(2);
    }
    
}

function createJsonDataReporting(_selector) {

    var _obj = {};
    var _allInput = document.querySelectorAll(_selector + " [data-var]");
    _allInput.forEach(function(index, array, element) {
        var _getAttr = index.attributes['data-var'].nodeValue,
            _closest = index.closest(".d-none");

        if(_closest == null || index.value != "") {
            _obj[_getAttr] = index.value;
        }
    });

    return _obj;
}

$(document).ready(function(e){

    var today = new Date();
    $(".datepick").attr("readonly", true);
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
        var _obj = {};
        var _allInput = document.querySelectorAll("[data-statement]");
        _allInput.forEach(function(index, array, element){
            _obj[index.getAttribute("data-statement")] = index.value;
        })
        $.ajax({
            type: "POST",
            url: "generateAccountStatement",
            data: _obj,
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
            var _obj = {};
            var _allInput = document.querySelectorAll("[data-statementFilter]");
            _allInput.forEach(function(index, array, element){
                _obj[index.getAttribute("data-statementFilter")] = index.value;
            })
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

    $(".download-failed").on("click", function(e){
         $("#payoutLocationForm").submit();
    })
    $(".download-failed-duplicate").on("click", function(e){
        $("#payoutLocationFormOrderId").submit();
   })

    var settlementFlagHandler = function(that) {
        var _activeInput = that.getAttribute("data-set");
        var _activeChecked = that.getAttribute("data-checked");
        if(that.checked == true) {
            hideDivOnSelect(_activeInput);
            _get.forEach(function(index, array, element) {
                _get[array].closest("label").classList.remove("checkbox-checked");
                _get[array].checked = false;
            });
            document.querySelector(".accountInput-div").classList.remove("d-none");
            document.querySelector("[data-checked="+_activeChecked+"]").closest("label").classList.add("checkbox-checked");
            document.querySelector("[data-checked="+_activeChecked+"]").checked = true;
        } else {
            document.querySelector(".accountInput-div").classList.add("d-none");
            document.querySelector("[data-checked="+_activeChecked+"]").closest("label").classList.remove("checkbox-checked");
            document.querySelector("[data-checked="+_activeChecked+"]").checked = false; 
        }
    }
    
    function hideDivOnSelect(_selector){
        var _parent = document.querySelector(".accountInput-div");
        var _allSelected = _parent.querySelectorAll("[data-single='"+_selector+"']");
        var _allInput = _parent.querySelectorAll("[data-single]");
        _allInput.forEach(function(index, array, element){
            index.closest(".single-account-div").classList.add("d-none");
        })
        _allSelected.forEach(function(index, array, element){
            index.closest(".single-account-div").classList.remove("d-none");
        })
        resetAllInput();
    }
    
    var _get = document.querySelectorAll(".signleAccountFlag");
    _get.forEach(function(index, array, element) {
        if(_get[array].checked == true){
            settlementFlagHandler(_get[array]);        
        }
        _get[array].addEventListener("change", function(e) {
            settlementFlagHandler(this);
            var _getDoc = document.querySelector("[data-target='subMerchant']");
            var _checkSubMechantNull = _getDoc.closest(".d-none");
            if(_checkSubMechantNull == null){
                _getDoc.classList.add("d-none");
            }
        })
    });
    
    var _getBulkData = document.querySelectorAll(".signleAccountFlagBulk");
    _getBulkData.forEach(function(index, array, element){
        _getBulkData[array].addEventListener('change', function(e){
            var _getActive = _getBulkData[array].getAttribute("data-bulk");
            var _isChecked = _getBulkData[array].checked;
            if(_isChecked){
                _getBulkData.forEach(function(index, array, element) {
                    _getBulkData[array].closest("label").classList.remove("checkbox-checked");
                    _getBulkData[array].closest(".transaction-type").classList.remove("active-transaction-type");
                    _getBulkData[array].checked = false;
                });
                document.querySelector("[data-bulk='"+_getActive+"']").closest(".transaction-type").classList.add("active-transaction-type");
                document.querySelector("[data-bulk="+_getActive+"]").closest("label").classList.add("checkbox-checked");
                document.querySelector("[data-bulk="+_getActive+"]").checked = true;
            }else{
                document.querySelector("[data-bulk='"+_getActive+"']").closest(".transaction-type").classList.remove("active-transaction-type");
                document.querySelector("[data-bulk="+_getActive+"]").closest("label").classList.remove("checkbox-checked");
                document.querySelector("[data-bulk="+_getActive+"]").checked = false; 
            }
        })
    })

    function resetAllInput(){
        document.querySelector("body").classList.remove("loader--inactive");
        var _parent = document.querySelector(".active-block");
        var _getAttr = _parent.getAttribute("data-active");
        var _inputElements = _parent.querySelectorAll("[data-var]");
        accountStatement(null);
        if(_getAttr == "topup") {
            checkBalance("[data-active='topup']", ".totalAmount");
            document.querySelector("[data-id='amount-left']").value = "";
            document.querySelector("[data-id='remarks']").value = "";
        }else if(_getAttr != "compositeReporting" && _getAttr != "accountStatement"){
            var _sub = _parent.querySelectorAll("[data-target]");
            _sub.forEach(function(index,array,element){
                var _isVisible = index.classList.toString();
                if(document.getElementById("isSuperMerchant").value !== "true") {
                    if(_isVisible.indexOf("d-none") == -1) {
                        index.classList.add("d-none");
                    }
                }
            })
            
           
            
            $(".selectpicker").val('default');
            $(".selectpicker").selectpicker('refresh');
            _inputElements.forEach(function(index, array, element){
                var _nullCheckErrorClass = index.closest(".hasError");
                var _checkSelectBox = index.closest(".lpay_select_group");
                if(_checkSelectBox == null){
                    index.value = "";
                }
                if(_nullCheckErrorClass != null){
                    _nullCheckErrorClass.classList.remove("hasError");
                }
                
            }) 
        }
        $(".lpay_popup").fadeOut();
        setTimeout(function(e){
            document.querySelector("body").classList.add("loader--inactive");
        }, 500);
    }
    
    document.querySelector("#confirmButton1").onclick = function(e){
        document.querySelector("body").classList.remove("loader--inactive");
        resetAllInput();
        $(".amount-show").text("");
        setTimeout(function(e){
            viewData();
            document.querySelector("body").classList.add("loader--inactive");
        }, 800);
        
    }

    document.querySelector("#confirmButton2").onclick = function(e){
        
        document.querySelector("body").classList.remove("loader--inactive");
        resetAllInput();
        $(".amount-show").text("");
        setTimeout(function(e){
            viewData();
            document.querySelector("body").classList.add("loader--inactive");
        }, 800);
    }   

    var _userType = document.querySelector("#userTypeLogin").value;
    if(_userType == "MERCHANT"){
        var _checkPermission = document.querySelector("#topupFlag").value;
        if(_checkPermission == "true"){
            checkBalance("[data-active='topup']", ".totalAmount");
        }
    }

   

    function tabChange(_selector) {

        var getClickTab = _selector;

        if(getClickTab == "compositeReporting") {

            var _superMerchant = document.querySelector("#isSuperMerchantTrue");
            var _resller = document.querySelector("#resellerTrue");
            var _subUser = document.querySelector("#subUserTrue");

            if(_superMerchant == null && _resller == null && _subUser == null && isParentSuperMerchant !== "true") {
                viewData();
            }

        }

        if(getClickTab == "topup"){

            $(".selectpicker").val('default');
            $(".selectpicker").selectpicker('refresh');
            document.querySelector("[data-id='amount-left']").closest(".single-account-input").classList.remove("hasError");
            document.querySelector("[data-target='subMerchantTopup']").classList.add("d-none");
            document.querySelector(".totalAmount").innerHTML = "0.00";
            var _userType = document.querySelector("#userTypeLogin").value;

            if(_userType == "true"){

                var _checkPermission = document.querySelector("#topupFlag").value;
                if(_checkPermission == "true"){
                    checkBalance("[data-active='topup']", ".totalAmount");
                }

            }
        }
        
        if(getClickTab == "accountLedger") {

            $(".accountLedger-datepick").val($.datepicker.formatDate('dd-mm-yy', today));
            $(".selectpicker").val('default');
            $(".selectpicker").selectpicker('refresh');

            if(isParentSuperMerchant !== "true" && document.getElementById("isSuperMerchant").value !== "true") {
                document.querySelector("[data-target='subMerchantLedger']").classList.add("d-none");
            }

            viewLedgerData();
            checkBalance(".check-balance-div", "#ledgerAmount");
            
        }

        $(".merchant__tab_button").removeClass("active-tab");
        $("[data-target='"+ getClickTab +"']").addClass("active-tab");
        $(".merchant__forms_block").removeClass("active-block");
        $("[data-active='"+ getClickTab +"']").addClass("active-block");
        if(getClickTab == "maker" || getClickTab == "checker" || getClickTab == "downloads" || getClickTab == "documents" || getClickTab == "eSign"){
            $("#btnSave").addClass("d-none");
        }else{
            $("#btnSave").removeClass("d-none");
        }
        if(getClickTab == "surchargeTextTab") {
            $(".merchant__buttons").addClass("d-none");
        } else {
            $(".merchant__buttons").removeClass("d-none");
        }
    }

    var ajaxRequest = function(reffObj) {
        var xhr = new XMLHttpRequest();
        xhr.open(reffObj.method, reffObj.actionName, true);
        xhr.onload = function() {
            if(xhr.status === 200) {
                var obj = JSON.parse(this.response);

                reffObj.success(obj);
            }
        }
        xhr.send(reffObj.payload);
    }

    var updateSubMerchant = function(reffObj) {
        // console.log(reffObj);

        var superMerchantPayId = document.getElementById(reffObj.superMerchantPayId).value,
            payload = new FormData();

            payload.append('payId', superMerchantPayId);

        ajaxRequest({
            method: 'POST',
            actionName: 'getSubMerchantList', // action name
            payload: payload, // payload object
            success: function(resObj) {
                var subMerchantList = resObj.subMerchantList,
                optionList = '<option value="'+ reffObj.firstIndexValue +'" selected="selected">'+ reffObj.firstIndexText +'</option>';

                for(var subMerchant in subMerchantList) {
                    optionList += '<option value="'+ subMerchantList[subMerchant].payId +'">'+ subMerchantList[subMerchant].businessName +'</option>';
                }

                document.getElementById(reffObj.targetElement).innerHTML = optionList;
                $("#" + reffObj.targetElement).selectpicker("refresh");
                $("#" + reffObj.targetElement).selectpicker();

                document.getElementById(reffObj.targetElement).selectedIndex = 0;

                document.querySelector('[data-target="'+ reffObj.targetElement +'"]').classList.remove("d-none");
            }
        });
    }
    
    $(".merchant__tab_button").on("click", function(e){
        var _selector = e.target.getAttribute("data-target");
        tabChange(_selector);
    });
    
    var _getActive = localStorage.getItem("active-tab"),
        _superMerchant = document.querySelector("#isSuperMerchantTrue"),
        _checkNull = document.querySelector("#resellerTrue"),
        isParentSuperMerchant = document.getElementById("isParentSuperMerchant").value;
    
    if(_superMerchant != null || _checkNull != null) {
        tabChange("compositeReporting");
    } else if(isParentSuperMerchant == "true") {
        tabChange("compositeReporting");

        updateSubMerchant({
            superMerchantPayId: "merchantComposite",
            targetElement: "subMerchantComposite",
            firstIndexValue: "ALL",
            firstIndexText: "ALL"
        });

        updateSubMerchant({
            superMerchantPayId: "merchantPayIdLedgerFilter",
            targetElement: "subMerchantLedgerFilter",
            firstIndexValue: "ALL",
            firstIndexText: "ALL"
        });

        updateSubMerchant({
            superMerchantPayId: "merchantPayIdLedger",
            targetElement: "subMerchantLedger",
            firstIndexValue: "ALL",
            firstIndexText: "ALL"
        });
    } else {
        if(_getActive !== null) {
            tabChange(_getActive);
        } else {
            var dataTarget = document.querySelector("#horizontal-nav li").getAttribute("data-target");
            tabChange(dataTarget);
        }
    }

    

    // var _superMerchant = document.querySelector("#isSuperMerchantTrue");

    var _superMerchant = "<s:property value='%{#session.USER.superMerchant}'/>";

    function graph(){

        var _arr = [];
        // var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var _month = "";
        var _day = "";
        for(var i = 0; i <= 6; i++){

            var dt = new Date();
            dt.setDate( dt.getDate() - 1 - i );
            var _newDate = new Date(dt);

            if(_newDate.getMonth() < 9 ){
                _month = "0"+Number(_newDate.getMonth() + 1);
            }else{
                _month = _newDate.getMonth() + 1;
            }

            if(_newDate.getDate() <= 9){
                _day = "0"+_newDate.getDate();
            }else{
                _day = _newDate.getDate();
            }

            _arr.push(_newDate.getFullYear() + "-" + _month + "-" + _day)
        }

        // console.log(_arr[0]);

        var _toDate = _arr[0];
        var _fromDate = _arr[6];

        var _limitLeft = [];
        var _limitConsumed = [];

        $.ajax({
            type: "POST",
            url: "graphLedgerAction",
            data: {
                "dateToGraph" : _toDate,
                "dateFromGraph" : _fromDate
            },
            success: function(data){


                if(data.aaGraphData.length > 0){

                    for(key in data.aaGraphData){
                        _limitLeft.push(Number(data.aaGraphData[key]['openingBalance']));
                        _limitConsumed.push(Number(data.aaGraphData[key]['totalDebit']));
                    }
    
                    Highcharts.chart('container', {
                        chart: {
                            type: 'bar'
                        },
                        title: {
                            text: null
                        },
                        xAxis: {
                            categories: _arr,
                            title: {
                                text: null
                            }
                        },
                        yAxis: {
                            min: 0,
                            title: {
                                text: 'All figures in INR',
                                align: 'high'
                            },
                            labels: {
                                overflow: 'justify'
                            }
                        },
                        tooltip: {
                            valueSuffix: ' INR'
                        },
                        plotOptions: {
                            bar: {
                                dataLabels: {
                                    enabled: true
                                }
                            }
                        },
                        legend: {
                            backgroundColor:
                                Highcharts.defaultOptions.legend.backgroundColor || '#FFFFFF',
                            shadow: true
                        },
                        credits: {
                            enabled: false
                        },
                        series: [
                            {
                              name: 'Limit Consumed',
                              data: _limitConsumed,
                              color: "#26a0da"
                            }, {
                              name: 'Limit Left',
                              data: _limitLeft,
                              color: "#041530"
                        }],
                        responsive: {
                            rules: [{
                                condition: {
                                    maxWidth: 500
                                },
                                chartOptions: {
                                    legend: {
                                        layout: 'horizontal',
                                        align: 'center',
                                        verticalAlign: 'bottom'
                                    },
                                    yAxis: {
                                        labels: {
                                            align: 'left',
                                            x: 0,
                                            y: -5
                                        },
                                        title: {
                                            text: null
                                        }
                                    }                                
                                }
                            }]
                        }
                    });
                }else{
                    document.querySelector(".empty-graph").style.display = "flex";
                }

            }
        })
    
    }

    graph();


    var _stopLoader = "";
    // reinitate action
    $("body").on("click", ".payNow", function(e){
        document.querySelector("body").classList.remove("loader--inactive");
        var _table = new $.fn.dataTable.Api('#compositeReportTabel');
        var _btnText = $(this).text();
        var _tr = $(this).closest("tr");
        var _data = _table.rows(_tr).data();
        if(_btnText == "Re-Initiate"){
            var _url = "getMerchantInitiatedDirectReInitiated";
        }else if(_btnText == "Get Status"){
            var _url = "getStatusMerchantInitiatedDirect";
        }
        $.ajax({
            type: "POST",
            url: _url,
            data: {
                "txnId": _data[0]['txnId'],
                "txnType" : _data[0]['txnType'],
                "payId" : _data[0]['merchantPayId'],
                "userType" : _data[0]['userType'],
                "amount" : _data[0]['amount'],
                "dateFrom" : _data[0]['date'],
                "orderId" : _data[0]['orderId']

            },
            success: function(data){
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.responseMsg);
                _stopLoader = setInterval(checkPopUp(".lpay_popup"), 500);
            }
        })
    })

    function checkPopUp(_selector){
        var _checkNull = document.querySelector(_selector);
        if(_checkNull.style.display == 'block'){
            document.querySelector("body").classList.add("loader--inactive");
            clearInterval(_stopLoader);
        }
    }

    

    function viewData() {
        var _obj = createJsonDataReporting("[data-active='compositeReporting']");
        function generateData(d){
            _obj.draw = d.draw;
            _obj.length = d.length;
            _obj.start = d.start;
            return _obj
        }
        
        document.querySelector("body").classList.remove("loader--inactive");

        var _aoColumnsArr = [
            {   "mData" : null,
                "mRender" : function(row){
                    if(row.status == "Pending" || row.status == "Initiated" || row.status == "Invalid"){
                        return ""
                    } else {
                        return "<input type='checkbox' />"
                    }
                }
            }, 
            { "mData": "merchantPayId", "className" : "merchantPayId" },
            { "mData": "merchant" },
            { "mData": "txnId", "className" : "txnID" },
            { "mData": "date", "className": "createDate" },
            { "mData": "orderId" , "className" : "orderId"},
            { "mData": "amount" , "className" : "amount"},
            { "mData": "status", "className" : "status"},
            { 
                "mData": null,
                "mRender" : function(row) {
                    if(row.finalStatus == "Y" || row.status == "Captured"  || row.status == "Invalid" || row.status == "Pending" || row.status == "Initiated") {
                        return "";
                    }else{
                        return "<button class='lpay_button lpay_button-md lpay_button-primary payNow'>Get Status</button>";
                    }

                }
            },
            {
                "mData" : null,
                "mRender" : function(row){
                    if(row.status == "Pending" || row.status == "Invalid" || row.status == "Initiated"){
                        return ""
                    }else{
                        return "<button class='lpay_button lpay_button-md lpay_button-primary updateStatus'>Update Status</button>";
                    }
                }
            }
        ];

        

        $('#compositeReportTabel').dataTable({
            "destroy": true,
            "serverSide" : true,
            "processing": true,
            "ordering": false,
            "ajax" : {    
                "url" : "merchantDirectReportData",
                "type" : "POST",
                "data" : function(d){
                   return generateData(d);
                },
            },
            "fnDrawCallback" : function(settings, json) {                
                hideColumnReport();
                
                document.querySelector("body").classList.add("loader--inactive");
            },
            "aoColumns": _aoColumnsArr
        });

        
    }

    window.addEventListener("load", function() {
        viewData();
    })
    
    function hideColumnReport() {
        var _checkLogin = document.querySelector("#user").value;
        var _allowPayoutUpdateStatus = document.querySelector("#allowPayoutUpdateStatus").value;
        var _table = $('#compositeReportTabel').DataTable();
        _table.columns(0).visible(false);
        _table.columns(8).visible(false);
        _table.columns(9).visible(false);
        if(_checkLogin == "ADMIN" || _checkLogin == "SUBADMIN") {
            _table.columns(0).visible(true);
            _table.columns(8).visible(true);
            _table.columns(9).visible(true);
        }
        if(_checkLogin == "MERCHANT"){
            if(_allowPayoutUpdateStatus == "true"){
                _table.columns(9).visible(true);
            }
        }
    }

    $(".update-status").on('click', function(){
        $(".lpay-popup_wrapper").fadeIn();
        $("[data-rrn]").addClass("d-none");
    })

    $("body").on("click", ".updateStatus", function(e){
        $(".lpay-popup_wrapper").fadeIn();
        $(this).closest("tr").addClass("edit_tr");
        $("[data-rrn]").removeClass("d-none");
        var _status = $(this).closest("tr").find(".status").text();
        $("#updateStatus").selectpicker('val', _status.trim());
        
    })

    $("body").on("click", ".update-cancel_button", function(e){
        $(".lpay-popup_wrapper").fadeOut();
        $("#compositeReportTabel_wrapper tr").removeClass("edit_tr");
        $("#updateRrn").val("");
        $("#updateStatus").selectpicker('refresh');
    })

    $("body").on("click", "#updateStatus-btn", function(e){
        var _par = $(".edit_tr");
        var _rrn = document.querySelector("#updateRrn").closest(".d-none");
        var _url = "";
        if(_rrn != null){
            var _data = {};
            var _table = new $.fn.dataTable.Api('#compositeReportTabel');
            var _array = [];
            var _checkboxLength = document.querySelectorAll("#compositeReportTabel_wrapper tbody input[type='checkbox']");
            _checkboxLength.forEach(function(index, array, element){
                if(index.checked == true){
                    var _row = index.closest("tr");
                    _array.push(_row.querySelector(".txnID").innerText);
                     
                }
            })
            _url = "updateAllStatusAction";
            _data['txnId'] = _array.toString();
            _data['updateStatus'] = $("#updateStatus").val();

        }else{
            var _data = {
                "payId" : _par.find(".merchantPayId").text(),
                "txnId" : _par.find(".txnID").text(),
                "orderId" : _par.find(".orderId").text(),
                "dateFrom" : _par.find(".createDate").text(),
                "amount" : _par.find(".amount").text(),
                "updateStatus": $("#updateStatus").val(),
                "status": _par.find(".status").text(),
                "updateRrn" : $("#updateRrn").val()
            }
            _url = "updatePayoutDataAction"
        }
        var _checkBlank = $("#updateStatus").val();
        if(_checkBlank != ""){
            document.querySelector(".update-status").classList.add("d-none");
            document.querySelector("#update_1").checked = false;
            $.ajax({
                type: "POST",
                url: _url,
                data: _data,
                success: function(data){
                    $(".lpay-popup_wrapper").fadeOut();
                    $("#compositeReportTabel_wrapper tr").removeClass("edit_tr");
                    $("#updateStatus").selectpicker('refresh');
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);                                
                }
            })
        }else{
            alert("Update status should not be blank");
        }
    })

    $("body").on("change", "#compositeReportTabel tbody input[type='checkbox']", function(e){
        var _checkboxLength = document.querySelectorAll("#compositeReportTabel_wrapper tbody input[type='checkbox']");
        var _checkboxChecked = false;
        _checkboxLength.forEach(function(index, array, element){
            if(index.checked == true){
                _checkboxChecked = true;
                document.querySelector(".update-status").classList.remove("d-none");
            }
        })
        if(_checkboxChecked == false){
            document.querySelector(".update-status").classList.add("d-none");
            document.querySelector("#update_1").checked = false;
        }
    })
    
    $("#ledgerTable").dataTable();

    function hideColumn(){
        var _table = $("#ledgerTable").DataTable();
        var _userType = document.querySelector("#user_type");
        var _getActive = document.querySelector("#subMerchantLedgerFilter").closest(".ledger-account-input").classList.toString().indexOf("d-none");
        // console.log(_getActive);
        if(_getActive != -1){
            _table.columns(1).visible(false);
        }

        if(_userType == null){
            _table.columns(2).visible(false);
        }

        setTimeout(function(e){
            document.querySelector("body").classList.add("loader--inactive");
        }, 500);
        // console.log(_getActive);
    }

    function viewLedgerData(){
        var _obj = createJsonData(".ledger-filter");
        var _check = document.querySelector(".ledger-filter .hasError");
        if(_check == null){
            document.querySelector("body").classList.remove("loader--inactive");
            $('#ledgerTable').dataTable( {
                "searching" :false,
                "ordering" :false,
                "destroy":true,
                "processing" :true,
                "ajax" : {
    
                    "url" : "viewLedgerReports",
                    "type" : "POST",
                    "data" : _obj,
                },
                "fnDrawCallback": function( oSettings ) {
                    hideColumn();
                },
                //"order": [[ 1, "desc" ]],
                "aoColumns": [
                    { "mData": "merchant" },
                    { "mData": "subMerchant" },
                    { "mData": "acquirerName" },
                    { "mData": "date" },
                    { "mData": "openingBalance" },
                    { "mData": "totalCredit"},
                    { "mData": "totalDebit"},
                    { "mData": "closingBalance"},
                    { 
                        "mData": null,
                        "mRender" : function(row) {
                            return "<button class='lpay_button lpay_button-md lpay_button-primary download'>Download</button>";
                            
                        }
                    }
                ]
            });
        }
    }
    
    viewLedgerData();
    
    document.querySelector("#viewLedger").onclick = viewLedgerData;

    function dowloadLedgerReport(_selector, _relateSelector){
        var _obj = createJsonDataReporting(_relateSelector);
        var _option = "";
        for(key in _obj){
            _option += "<input type='hidden' name='"+key+"' value='"+_obj[key]+"' />";
        }
        document.querySelector(_selector).innerHTML = _option;
        var _getSubMerchant = document.querySelector(_selector+" [name='subMerchantPayId']");
        if(_getSubMerchant != null && _getSubMerchant == ""){
            document.querySelector(_selector+" [name='subMerchantPayId']").value = "ALL";
        }
        document.querySelector(_selector).submit();
    }

    document.querySelector("#view").onclick = viewData;
    document.querySelector("#download").onclick = function(){
        dowloadLedgerReport("#downloadComposite", "[data-active='compositeReporting']");
    }

    document.querySelector("#downloadLedgerReportButton").onclick = function(){
        dowloadLedgerReport("#downloadLedgerReport", ".ledger-filter");
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
                row.child( dataTableMoreData(row.data())).show();
                row.child()[0].children[0].classList.add("active-row");
                getAllData();
                tr.addClass('shown');
            }
        }
        
    })

    $("body").on("click", ".download", function(e){
        var _table = new $.fn.dataTable.Api('#ledgerTable');
        var _tr = $(this).closest("tr");
        var _data = _table.rows(_tr).data();
        var _option = "<input type='hidden' name='payId' value='"+_data[0]['merchantPayId']+"' />";
        _option += "<input type='hidden' name='subMerchantPayId' value='"+_data[0]['subMerchantPayId']+"' />";
        _option +=  "<input type='hidden' name='dateTo' value='"+_data[0]['date']+"' />";
        _option +=  "<input type='hidden' name='openingBalance' value='"+_data[0]['openingBalance']+"' />";
        _option +=  "<input type='hidden' name='closingBalance' value='"+_data[0]['closingBalance']+"' />";
        _option += "<input type='hidden' name='acquirerName' value='"+_data[0]['acquirerName']+"' />";
        document.querySelector("#ledgerDownloadIndividual").innerHTML = _option;
        document.querySelector("#ledgerDownloadIndividual").submit();
    })

    //_option +=  "<input type='hidden' name='date' value='"+_data[0]['date']+"' />";
    function getAllData(){
        var _new = document.querySelectorAll(".inner-div");
        _new.forEach(function(index, array, element){
            var _getValue = _new[array].children[1].innerText;
            if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
                _new[array].classList.add("d-none");
            }
        })
    }
    


    
    function dataTableMoreData ( d ) {
        var _mainDiv = "<div class='main-div'>";
        var _obj = {
            "subMerchant" : "Sub Merchant Name",
            "impsRefNum": "TXN REF Number",
            "userType": "Channel",
            "rrn": "RRN Number",
            "txnType": "Mode",
            "phoneNo": "Mobile Number",
            "beneAccountName" : "Bene Name/Payee Name",
            "bankAccountNumber": "Bank Account Number",
            "bankAccountName": "Bank Name/VPA",
            "bankIFSC": "Bank IFSC",
            "purpose": "Purpose",
            "responseMsg": "Response Message",
            "commissionAmount" : "Commission Amount",
            "serviceTax" : "Service Tax"
        }
        for(key in _obj){
            if(_obj[key].hasOwnProperty("className")){
                var _getKey = Object.keys(_obj[key]);
                _mainDiv += '<div class="inner-div '+_obj[key]["className"]+'">'+
                        '<span>'+_obj[key][_getKey[0]]+'</span>'+
                        '<span>'+d[_getKey[0]]+'</span>'+
                    '</div>'
            }else{
                _mainDiv += '<div class="inner-div">'+
                    '<span>'+_obj[key]+'</span>'+
                    '<span>'+d[key]+'</span>'+
                '</div>'
            }
        }
        _mainDiv += "</div>";
        return _mainDiv;
            
    }

    function loadDataTable(){

        var _datatable = [
            { "id" : "download-format","text" : "Download CSV Format", "title" : "IMPS Bulk Payout" },
            { "id" : "download-format-upi","text" : "Download CSV Format", "title" : "UPI Bulk Payout" },
            { "id" : "download-format-rtgs","text" : "Download CSV Format", "title" : "RTGS Bulk Payout" },
            { "id" : "download-format-neft","text" : "Download CSV Format", "title" : "NEFT Bulk Payout" }
        ]
        
        for(key in _datatable){
            $('#'+_datatable[key]['id']).DataTable({
                dom: 'B',
                buttons: [
                    {
                        extend: 'csv',
                        text: 'Download CSV Format',
                        className: 'lpay_button lpay_button-md lpay_button-primary',
                        title: _datatable[key]['title'],
                        exportOptions: {
                            modifier: {
                                search: 'none'
                            }
                        }
                    }
                ]
            });
        }

        
    }

    loadDataTable();
    


    $(".lpay_upload_input").on("change", function (e) {
		var _val = $(this).val();
		var _fileSize = $(this)[0].files[0].size;
        var _fileType = $(this)[0].files[0].type;
		var _tmpName = _val.replace("C:\\fakepath\\", "");
		$(this).closest("label").next(".hideFields").val(_tmpName);
		if (_val != "") {
            if(_fileType == "application/vnd.ms-excel"){
                $("body").removeClass("loader--inactive");
                $(this).closest("label").find(".default-upload").addClass("d-none");
                $(this).closest("label").find("#placeholder_img").css({ "display": "none" });
                if (_fileSize < 2000000) {
                    $(this).closest("label").attr("data-status", "success-status");
                    $(this).closest("label").find(".fileName").text(_tmpName);
    
                    $("#bulkUpdateSubmit").attr("disabled", false);
                    setTimeout(function (e) {
                        $("body").addClass("loader--inactive");
                    }, 500);
                } else {
                    $(this).closest("label").attr("data-status", "error-status");
                    $("#bulkUpdateSubmit").attr("disabled", true);
                    setTimeout(function (e) {
                        $("body").addClass("loader--inactive");
                    }, 500);
                }
            }else{
                $(this).closest(".single-account-input").addClass("hasError");
                $(this).closest(".single-account-input").find(".error-field").text("Invalid file type");
            }
		}
	});
})


// 
$(".confirmButton").on('click', function(e){
    $(".lpay_popup").fadeOut();
})

function submitBulkPayoutRequest(e){
    console.log(e.closest("form").getAttribute("id"));
    var _obj = createJsonData("#"+e.closest("form").getAttribute("id"));
    var _checkError = e.closest("form").querySelector(".hasError");
    if(_checkError == null){
        var _active = document.querySelector(".merchant__tab_button.active-tab").getAttribute("data-target");
        localStorage.setItem("active-tab", _active);
    }else{
        event.preventDefault();
        return false;
    }
}

$("#ifsc").on("keyup", function(e){
    this.value = this.value.toUpperCase();
});



function checkBalance(_selector, _amount){
    var _obj = createJsonData(_selector);
    document.querySelector(_amount).closest("div").classList.add("loader-amount");
    var _subMerchantTopup = document.querySelector("#subMerchantTopup").closest("[data-target]").classList.toString();
    var _checkTab = document.querySelector(".active-tab").getAttribute("data-target");
    if(_checkTab == "topup"){
        if(_subMerchantTopup.indexOf("d-none") != -1){
            _obj['subMerchantPayId'] = null
        }
    }
    $.ajax({
        type: "POST",
        url: "currentBalanceLedger",
        data: _obj,
        success: function(data){
            setTimeout(function(e){
                document.querySelector(_amount).innerHTML = data.respMap.checkAmount;
                document.querySelector(_amount).closest("div").classList.remove("loader-amount");
            }, 1000);
        }
    })
}

function addTopup(e){
    var _obj = createJsonData("[data-active='topup']");
    var _checkNull = document.querySelector("[data-active='topup']").querySelector(".hasError");
    if(_checkNull == null){
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "addCurrentBalance",
            data: _obj,
            success: function(data){
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }
                document.querySelector("body").classList.add("loader--inactive");
            }
        })
    }
}

function selectAll(_this){
    var _check = _this.checked;
    var _checked = false;
    var _allTd = document.querySelectorAll("#compositeReportTabel tbody input[type='checkbox']");
    if(_allTd.length > 0){
        _allTd.forEach(function(index, array, element){
            if(_check == true){
                index.checked = true;
                _checked = true;
            }else{
                index.checked = false;
            }
        })
        if(_checked == true){
            document.querySelector(".update-status").classList.remove("d-none");
        }else{
            document.querySelector(".update-status").classList.add("d-none");
        }
    }else{
        _this.checked = false;
        alert("You don't have any row to select");
    }
}

checkBalance(".check-balance-div", "#ledgerAmount");
document.querySelector("#checkBalance").onclick = function(e){
    checkBalance(".check-balance-div", "#ledgerAmount");
};

