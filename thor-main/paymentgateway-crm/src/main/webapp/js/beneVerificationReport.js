   
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

document.querySelector("#merchantComposite").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

document.querySelector("#merchant").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
});

function payFunction(){
    var _obj = createJsonData("[data-active].active-block");
    var _checkError = document.querySelector(".hasError");
    if(_checkError == null) {
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getAddBeneficiaryImpsVpa",
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


function createJsonData(_selector) {
    var _obj = {};
    var _parent = document.querySelector(_selector);
    var _allInput = _parent.querySelectorAll("[data-var]");

    _allInput.forEach(function(index, array, element) {
        var _getAttr = index.attributes['data-var'].nodeValue,
            _closest = index.closest(".d-none");
        
        if(index.attributes['data-required'] != null){
            var _required = index.attributes['data-required'].nodeValue;
        } else {
            var _required = "false"; 
        }

        if(_closest == null || index.value != ""){
            if(index.value == "" && _required == "true") {
                if(index.closest(".single-account-input").querySelector(".error-field") != null){
                    index.closest(".single-account-input").classList.add("hasError");
                    index.closest(".single-account-input").querySelector(".error-field").innerText = "Should not be blank";
                }
            }else{
                index.closest(".single-account-input").classList.remove("hasError");
                _obj[_getAttr] = index.value;
            }
        }
    });
    return _obj;
}






function createJsonDataReporting(_selector) {
    var _obj = {};
    var _allInput = document.querySelectorAll(_selector + " [data-var]");
    _allInput.forEach(function(index, array, element){
        var _getAttr = index.attributes['data-var'].nodeValue;
        var _closest = index.closest(".d-none");
        if(_closest == null || index.value != ""){
            _obj[_getAttr] = index.value;
        }
    })
    return _obj;
}

$(document).ready(function(e){


    var _stopLoader = "";
    // reinitate action
    $("body").on("click", ".payNow", function(e){
        // document.querySelector("body").classList.remove("loader--inactive");
        var _table = new $.fn.dataTable.Api('#compositeReportTabel');
        var _btnText = $(this).text();
        var _tr = $(this).closest("tr");
        var _data = _table.rows(_tr).data();
        $.ajax({
            type: "POST",
            url: _url,
            data: {
                "txnId": _data[0]['txnId'],
                "txnType" : _data[0]['txnType'],
                "payId" : _data[0]['merchantPayId'],
                "userType" : _data[0]['userType'],
                "amount" : _data[0]['amount']

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

    function resetInput(_parentSelector){
        var _parent = document.querySelectorAll(_parentSelector + " [data-var]");
        var _dataHide = document.querySelectorAll(_parentSelector + " [data-hide]");
        _parent.forEach(function(index, array, element){
            var _localName = index.localName;
            if(_localName == "input" || _localName == "INPUT"){
                index.value = "";
            }else{
                $("#"+index.id).val('default');
                $("#"+index.id).selectpicker('refresh');
            }
        })
        _dataHide.forEach(function(index, array, element){
            index.classList.add("d-none");
        })
    }

    $(".confirmButton").click(function(e){
        $(".lpay_popup").fadeOut();
        resetInput("[data-active].active-block");
    })

    $("body").on("click", ".dlt-button", function(e){
        var _parent = $(this).closest("tr");
        $("#compositeReportTabel tr").removeClass("active-tr");
        _parent.addClass("active-tr");
        $("#fancybox").fancybox({
            'overlayShow': true
        }).trigger('click');
    });

    function deleteAction(e){
        var _parent = $(".active-tr");
        var _table = new $.fn.dataTable.Api('#compositeReportTabel');
        var _data = _table.rows(_parent).data();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "post",
            url: "getdeleteBeneficiaryData",
            data: {"payId": _data[0]['merchantPayId'], "subMerchantPayId": _data[0]['subMerchantPayId'], "bankIFSC" : _data[0]['bankIFSC'], "bankAccountNumber" : _data[0]['bankAccountNumber']},
            success: function(data){
                var table = $("#compositeReportTabel").DataTable();
                table.ajax.reload();
                $("body").addClass("loader--inactive");
            },
            error: function(data){
            }
        });
    }

    $("body").on("click", "#confirm-btn", function(e){
        deleteAction();
        $.fancybox.close();
    });
    $("body").on("click", "#cancel-btn", function(e){
        $.fancybox.close();
    });

    function checkPopUp(_selector){
        var _checkNull = document.querySelector(_selector);
        if(_checkNull.style.display == 'block'){
            document.querySelector("body").classList.add("loader--inactive");
            clearInterval(_stopLoader);
        }
    }

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

   
    function viewData(){
        var _obj = createJsonDataReporting("[data-active='reporting']");
        document.querySelector("body").classList.remove("loader--inactive");
        $('#compositeReportTabel').dataTable( {
            "destroy": true,
            "ajax" : {
    
                "url" : "viewBeneRegistrationReport",
                "type" : "POST",
                "data" : _obj,
            },
            "fnDrawCallback" : function(settings, json) {
                hideColumn();
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "aoColumns": [
                { "mData": "merchant" },
                { "mData": "subMerchant" },
                { "mData": "orderId" },
                { "mData": "txnType" },
                { "mData": "phoneNo" },
                { "mData": "beneAccountName" },
                { "mData": "bankAccountNumber" },
                { "mData": "bankIFSC"},
                { "mData": "status"},
                { 
                    "mData": null,
                    "mRender" : function(row) {
                        if(row.beneRegistration  == "Active"){
                            return "<span class='dlt-button'><i class='fa fa-trash' aria-hidden='true'></i></span>"
                        }else{
                            return ""
                        }
    
                    }
                }
            ]
        });
    }
    
    viewData();

    function hideColumn(){
        var _superMerchantFlag = $("[data-id=superMerchantFlag]").val();
        var _subMerchantFlag = $("[data-id=subMerchantFlag]").val();
        var _table = $("#compositeReportTabel").DataTable();
        var _subMerchant = document.querySelector("#subMerchantLogin");
        var _isMerchant = document.querySelector("#isMerchant").value;
        var _merchantValue = document.querySelector("#merchantComposite").value;
        var _isSubUser = document.querySelector("#subUserTrue");
        var _visibileSubMerchant = document.querySelector("#subMerchantComposite");
        if(_subMerchant != null || _merchantValue == "ALL" || _visibileSubMerchant.closest(".d-none") == null){
            _table.columns(1).visible(true)
        }else if(_isSubUser != null && _subMerchant == null){
            console.log(_merchantValue);
            console.log(_superMerchantFlag);
            if(_superMerchantFlag == "true"){
                console.log("Hi");
                _table.columns(1).visible(true);
            }else if(_subMerchantFlag == "true"){
           	 console.log("subMerchantFlag");
             _table.columns(1).visible(true);
        }else{
            _table.columns(1).visible(false)
            }
        }
        else if(_isMerchant == "MERCHANT"){
            _table.columns(1).visible(false)
        }else{
            _table.columns(1).visible(false)
        }
    }
    

    function dowloadLedgerReport(_selector, _relateSelector) {
        var _obj = createJsonDataReporting(_relateSelector);
        var _option = "";
        for(key in _obj) {
            _option += "<input type='hidden' name='"+key+"' value='"+_obj[key]+"' />";
        }
        document.querySelector(_selector).innerHTML = _option;
        var _getSubMerchant = document.querySelector(_selector+" [name='subMerchantPayId']");
        if(_getSubMerchant != null && _getSubMerchant == ""){
            document.querySelector(_selector+" [name='subMerchantPayId']").value = "ALL";
        }
        document.querySelector(_selector).submit();
    }

    document.querySelector("#view").onclick = function(e){
        
        viewData();
    }
    
    document.querySelector("#download").onclick = function(){
        dowloadLedgerReport("#downloadComposite", "[data-active='reporting']");
    }

      

    $("body").on("click", ".download", function(e){
        var _table = new $.fn.dataTable.Api('#ledgerTable');
        var _tr = $(this).closest("tr");
        var _data = _table.rows(_tr).data();
        var _option = "<input type='hidden' name='payId' value='"+_data[0]['merchantPayId']+"' />";
        _option += "<input type='hidden' name='subMerchantPayId' value='"+_data[0]['subMerchantPayId']+"' />";
        _option +=  "<input type='hidden' name='dateTo' value='"+_data[0]['date']+"' />";
        _option +=  "<input type='hidden' name='openingBalance' value='"+_data[0]['openingBalance']+"' />";
        _option +=  "<input type='hidden' name='closingBalance' value='"+_data[0]['closingBalance']+"' />";
        document.querySelector("#ledgerDownloadIndividual").innerHTML = _option;
        document.querySelector("#ledgerDownloadIndividual").submit();
    })

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

    var settlementFlagHandler = function(that) {
        var _activeInput = that.getAttribute("data-set");
        if(that.checked == true) {
            hideDivOnSelect(_activeInput);
            _get.forEach(function(index, array, element) {
                _get[array].closest("label").classList.remove("checkbox-checked");
                _get[array].checked = false;
            });
            document.querySelector(".accountInput-div").classList.remove("d-none");
            document.querySelector("[data-set="+_activeInput+"]").closest("label").classList.add("checkbox-checked");
            document.querySelector("[data-set="+_activeInput+"]").checked = true;
        } else {
            document.querySelector(".accountInput-div").classList.add("d-none");
            document.querySelector("[data-set="+_activeInput+"]").closest("label").classList.remove("checkbox-checked");
            document.querySelector("[data-set="+_activeInput+"]").checked = false; 
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
    }
    
    var _get = document.querySelectorAll(".signleAccountFlag");
    _get.forEach(function(index, array, element) {
        if(_get[array].checked == true){
            settlementFlagHandler(_get[array]);        
        }
        _get[array].addEventListener("change", function(e) {
            settlementFlagHandler(this);

            var _checkSubmerchant = document.querySelector("#subMerchantLogin");
            if(_checkSubmerchant == null && isParentSuperMerchant != "true") {
                var _getDoc = document.querySelector("[data-target='subMerchant']");
                var _checkSubMechantNull = _getDoc.closest(".d-none");
                if(_checkSubMechantNull == null) {
                    _getDoc.classList.add("d-none");
                }
            }

            $("#merchant").selectpicker("deselectAll");
        });
    });

    function tabChange(_selector) {
        var getClickTab = _selector;
        $(".merchant__tab_button").removeClass("active-tab");
        $("[data-target='"+ getClickTab +"']").addClass("active-tab");
        $(".merchant__forms_block").removeClass("active-block");
        $("[data-active='"+ getClickTab +"']").addClass("active-block");

        if(getClickTab == "reporting") {
            viewData();
        }
    }
    
    $(".merchant__tab_button").on("click", function(e){
        var _selector = e.target.getAttribute("data-target");
        tabChange(_selector);
    });


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
        var superMerchantPayId = document.getElementById(reffObj.superMerchantPayId).value,
            payload = new FormData();

            payload.append('payId', superMerchantPayId);

        ajaxRequest({
            method: 'POST',
            actionName: 'getSubMerchantList', // action name
            payload: payload, // payload object
            success: function(resObj) {
                var subMerchantList = resObj.subMerchantList,
                    optionList = '<option value="'+ reffObj.firstIndexValue +'">'+ reffObj.firstIndexText +'</option>';

                for(var subMerchant in subMerchantList) {
                    optionList += '<option value="'+ subMerchantList[subMerchant].payId +'">'+ subMerchantList[subMerchant].businessName +'</option>';
                }

                document.getElementById(reffObj.targetElement).innerHTML = optionList;
                $("#" + reffObj.targetElement).selectpicker("refresh");
                $("#" + reffObj.targetElement).selectpicker();

                document.querySelector('[data-target="'+ reffObj.targetElement +'"]').classList.remove("d-none");
            }
        });
    }

    var _reseller = document.querySelector("#resellerTrue"),
        isParentSuperMerchant = document.getElementById("isParentSuperMerchant").value;

    
    
    if(isParentSuperMerchant == "true") {
        updateSubMerchant({
            superMerchantPayId: "merchant",
            targetElement: "subMerchant",
            firstIndexValue: "",
            firstIndexText: "Select Sub Merchant"
        });

        updateSubMerchant({
            superMerchantPayId: "merchant",
            targetElement: "subMerchantComposite",
            firstIndexValue: "ALL",
            firstIndexText: "ALL"
        });
    }

    if(_reseller == null) {
        tabChange("addBeneficiary");
    } else {
        tabChange("reporting");
    }

})
