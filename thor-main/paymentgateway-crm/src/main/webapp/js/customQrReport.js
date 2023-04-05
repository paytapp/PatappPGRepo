


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


function generateData(d){
    var _obj = {};
    var _allInput = document.querySelectorAll("[data-active='customQrReport'] [data-var]");
    _allInput.forEach(function(index, array, element){
        var _getAttr = index.attributes['data-var'].nodeValue;
        var _closest = index.closest(".d-none");
        if(_closest == null || index.value != ""){
            _obj[_getAttr] = index.value;
        }
    })
    _obj.draw = d.draw;
	_obj.length = d.length;
	_obj.start = d.start;
    console.log(_obj);
    return _obj;
}



function createJsonDataReporting(_selector){
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
        document.querySelector("body").classList.remove("loader--inactive");
        $('#compositeReportTabel').dataTable( {
            "destroy": true,
            "serverSide" : true,
            "processing": true,

            "ajax" : {
    
                "url" : "viewCustomerQRReport",
                "type" : "POST",
                "data" : function(d){
                    return generateData(d)
                },
            },
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "aoColumns": [
                { "mData": "merchantName" },
                { "mData": "payId" },
                { "mData": "date" },
                { "mData": "customerAccountNumber" },
                { "mData": "customerId" },
                { "mData": "vpa" },
                { "mData": "status" },
                {
                    "mData" : null,
                    "mRender" : function(row) {
                        return "<a download href='data:image/png;base64,"+row.upiQrCode+"'><img src='data:image/png;base64,"+row.upiQrCode+"' width='40px'/></a> "
                    }
                },
                { 
                    "mData": null,
                    "className": "text-center",
                    "mRender" : function(row) {
                    	
                    	var _userType = document.querySelector("#user_typ").value;
                    	//if(_userType == "ADMIN" || _userType == "SUBADMIN") {
                    		 var downloadBtn = "<button class='lpay_button lpay_button-md lpay_button-primary btn-pdf-download'>Download PDF</button>";
                             if(row.status  == "Active") {
                                 return "<button class='lpay_button lpay_button-md lpay_button-primary actButton mb-5'>DeActivate</button>" + downloadBtn;
                             } else {
                                 return " <button class='lpay_button lpay_button-md lpay_button-primary actButton mb-5'>Activate</button>" + downloadBtn;
                             }
                    	/*} else {
                    		 var downloadBtn = "<button class='lpay_button lpay_button-md lpay_button-primary btn-pdf-download'>Download PDF</button>";
                             if(row.status  == "Active") {
                                 return "<button class='lpay_button lpay_button-md lpay_button-primary actButton mb-5'>DeActivate</button>" + downloadBtn;
                             } else {
                                 return " <button class='lpay_button lpay_button-md lpay_button-primary actButton mb-5'>Activate</button>" + downloadBtn;
                             }
                    	}*/
                    }
                }
            ]
        });
    }
    
    viewData();

    
    

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

    document.querySelector("#view").onclick = function(e){
        
        viewData();
    }
    
    document.querySelector("#download").onclick = function(){
        dowloadLedgerReport("#downloadComposite", "[data-active='customQrReport']");
    }

   
      
    $("body").on("click", ".actButton", function(){
        var _table = new $.fn.dataTable.Api('#compositeReportTabel'),
            _tr = $(this).closest("tr"),
            _data = _table.rows(_tr).data();

        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getActiveOrInActiveData",
            data : {
                "payId" : _data[0]['payId'],
                "customerAccountNumber" : _data[0]['customerAccountNumber'],
                "customerId" : _data[0]['customerId'],
                "status" : _data[0]['status'],
            },
            success: function(data){
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    document.querySelector("body").classList.add("loader--inactive");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    document.querySelector("body").classList.add("loader--inactive");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.responseMsg);
                }
            }
        });
    });

    $("body").on("click", ".btn-pdf-download", function(e) {
        e.preventDefault();
       // document.querySelector("body").classList.remove("loader--inactive");

        var _table = new $.fn.dataTable.Api('#compositeReportTabel'),
            _tr = $(this).closest("tr"),
            _data = _table.rows(_tr).data();

        $("#qr-payId").val(_data[0]['payId']);
        $("#qr-customerAccountNumber").val(_data[0]['customerAccountNumber']);
        $("#qr-customerId").val(_data[0]['customerId']);

        $("#downloadStaticQr").submit();
    });
 
    $(".confirmButton").on("click", function(e){
        resetInput("[data-active='customQrReport']");
        viewData();
        $(".lpay_popup").fadeOut();
    })
    
    
    

})


function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
    var _merchant = _this.target.value;
    console.log(_merchant);
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
                console.log(obj);
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

document.querySelector("#merchantComposite").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

var _userType = document.querySelector("#user_typ").value;
if(_userType == "RESELLER"){
    document.querySelector("#merchantComposite").addEventListener("change", function(_this){
        getSubMerchant(_this, "submerchantLisForQrReport", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });
}