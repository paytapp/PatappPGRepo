// create input
function createAdf(_count){
    for(var i = 1; i < _count; i++){
        var _input = '<div class="col-md-3 mb-20"><div class="lpay_input_group"><label>ADF '+i+'</label><input type="text" data-dynamic="ADF_'+i+'" class="lpay_input" /></div></div>';
        document.querySelector(".adf-section").innerHTML += _input; 
    }
}
createAdf(21);

// add more fields
function addMoreFields(){
    var _existNumber = document.querySelectorAll("[data-dynamic]").length;
    var _existInput = document.querySelectorAll("[data-dynamic]");
    var _obj = {};
    _existInput.forEach(function(index, arrya, element){
        _obj[index.getAttribute("data-dynamic")] = index.value;
    })
   
    for(var i = 1; i < 5; i++){
        var _number = _existNumber + i;
        var _input = '<div class="col-md-3 mb-20"><div class="lpay_input_group"><label>ADF '+_number+'</label><input type="text" class="lpay_input" data-dynamic="ADF_'+_number+'" /></div></div>';
        document.querySelector(".adf-section").innerHTML += _input;
    }
    for(key in _obj){
        document.querySelector("[data-dynamic='"+key+"']").value = _obj[key];
    }
}

function removeErrorClass(_this){
    if(_this.closest(".check-blank") != null){
        _this.closest(".check-blank").classList.remove("hasError");
    }
}

// get sub merchant
function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
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

document.querySelector("#merchant").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

function saveMapping(){

    var _allInput = document.querySelectorAll("[data-target='acquirerMapping'] [data-var]");
    var _allDynamicInput = document.querySelectorAll("[data-dynamic]");
    var _isEmpty = true;

    var _obj = {};
    var _newObj = {};

    _allInput.forEach(function(index, array, element){
        if(index.value == ""){
            _isEmpty = false;
            index.closest(".check-blank").classList.add("hasError");
        }else{
            _obj[index.getAttribute("data-var")] = index.value;
        }
    })

    _allDynamicInput.forEach(function(index, array, element){
        _newObj[index.getAttribute('data-dynamic')] = index.value;
    })

    _obj['adfFields'] = JSON.stringify(_newObj);

    if(_isEmpty == true){
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "addPayoutAcquirerMappping",
            data: _obj,
            success: function(data){
                console.log(data);
                $(".responseMsg").text(data.responseMsg);
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                setTimeout(function(){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            }
        })
    }

}

function hideShowInput(_behavior){
    var _getPayout = document.querySelectorAll("[data-payout]");
    _getPayout.forEach(function(index, array, element){
        if(_behavior == "add"){
            index.classList.add("d-none");
        }else{
            index.classList.remove("d-none")
        }
    })
}

function fetchMappingDetails(_this){
    var _value = _this.value;
    var _subMerchant = "";
    document.querySelector("body").classList.remove("loader--inactive");
    setTimeout(function(e){
        var _isVisible = document.querySelector("#subMerchant");
        if(_isVisible.closest(".d-none") == null){
            _subMerchant = _isVisible.value;
        }else{
            _subMerchant = null
        }
        var _obj = {
            "payId" : _value,
            "subMerchantPayId" : _subMerchant
        };
        console.log(_obj);
        $.ajax({
            type: "POST",
            url: "fetchAllPayoutMerchantMapppingRecord",
            data: _obj,
            success: function(data){
                if(data.merchantMappedData.length > 0 || _value == "ALL"){
                    hideShowInput("add");
                    fetchMerchantMappingData(_value);
                    document.querySelector("[data-target='merchantMapping'] .lpay_table_style-2").classList.remove("d-none"); 
                }else{
                    hideShowInput("remove");
                    document.querySelector("[data-target='merchantMapping'] .lpay_table_style-2").classList.add("d-none");
    
                }
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            }
        })
    }, 700)
}

function merchantMapping(){
    var _allInput = document.querySelectorAll("[data-target='merchantMapping'] [data-var]");
    var _obj = {};
    var _isEmpty = true;
    _allInput.forEach(function(index, element, array){
        var _checkVisible = index.closest(".d-none");
        if(index.value == "" && _checkVisible == null){
            _isEmpty = false;
            index.closest(".check-blank").classList.add("hasError");
        }else{
            _obj[index.getAttribute("data-var")] = index.value;
        }
    })

    if(_isEmpty == true){
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "addPayoutMerchantMappping",
            data: _obj,
            success: function(data){
                console.log(data);
                $(".responseMsg").text(data.responseMsg);
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                setTimeout(function(){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            }
        })
    }
}

function closePopup(){
    $(".lpay_popup").fadeOut();
    resetTab();
    fetchMappingDetails(document.querySelector("#merchant"));
}

function tabShow(_this, _data){
    var _allTab = document.querySelectorAll(".lpay_tabs_content");
    var _allLink = document.querySelectorAll(".lpay-nav-item");
    _allTab.forEach(function(index, element, array){
        index.classList.add("d-none");

    })
    _allLink.forEach(function(index, array, element){
        index.classList.remove("active");
    })
    _this.classList.add("active");
    document.querySelector("[data-target='"+_data+"']").classList.remove("d-none");
    resetTab();
    fetchMappingDetails(document.querySelector("#merchant"));

}



function handleChangePayout(){
    var _bankName = document.querySelector("#bankName").value;
    var _userType = document.querySelector("#userType").value;
    var _accountType = document.querySelector("#accountType").value;
    if(_bankName != "" && _userType != "" && _accountType != ""){
        document.querySelector("body").classList.remove("loader--inactive");
        var _allDynamicInput = document.querySelectorAll("[data-dynamic]");
        _allDynamicInput.forEach(function(index, array, element){
            index.value = "";
            index.removeAttribute("readonly");
        })
        $.ajax({
            type: "POST",
            url: "fetchPayoutAcquirerMappping",
            data: {
                "bankName" : _bankName,
                "userType" : _userType,
                "accountType" : _accountType
            },
            success: function(data){
                var _jsonAdf = JSON.parse(data.adfFields);
                document.querySelector(".adf-section").innerHTML = "";
                if(_jsonAdf != null){
                    document.querySelector("#edit-fields").classList.remove("d-none");
                    for(key in _jsonAdf){
                        var _input = '<div class="col-md-3 mb-20"><div class="lpay_input_group"><label>'+key.replace("_", " ")+'</label><input type="text" data-dynamic="'+key+'" value="'+_jsonAdf[key]+'" readonly class="lpay_input" /></div></div>';
                        document.querySelector(".adf-section").innerHTML += _input;
                    }
                }else{
                    createAdf(21);
                    document.querySelector("#edit-fields").classList.add("d-none");
                }
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500)
            }
        })
    }else{
        // resetTab()
        document.querySelector(".adf-section").innerHTML = "";
        createAdf(21);
    }
}

function handleChangeBankName(_this){
    var _bankName = _this.value;
    var _getParent = _this.closest(".lpay_tabs_content").getAttribute("data-target");
    // console.log(_getParent);
    if(_bankName == "PAYTM"){
        $("[data-target='"+_getParent+"'] [data-var='userType']").prop("disabled", true);
        $("[data-target='"+_getParent+"'] [data-var='userType']").val("Payment Gateway");
        $("[data-target='"+_getParent+"'] [data-var='userType']").selectpicker('refresh');
        $("[data-target='"+_getParent+"'] [data-var='accountType']").prop("disabled", true);
        $("[data-target='"+_getParent+"'] [data-var='accountType']").val("Current");
        $("[data-target='"+_getParent+"'] [data-var='accountType']").selectpicker('refresh');
    }else{
        $("[data-target='"+_getParent+"'] [data-var='userType']").prop("disabled", false);
        $("[data-target='"+_getParent+"'] [data-var='userType']").val("default");
        $("[data-target='"+_getParent+"'] [data-var='userType']").selectpicker('refresh');
        $("[data-target='"+_getParent+"'] [data-var='accountType']").prop("disabled", false);
        $("[data-target='"+_getParent+"'] [data-var='accountType']").val("default");
        $("[data-target='"+_getParent+"'] [data-var='accountType']").selectpicker('refresh');
    }
}

function editFields(){
    var _getAllInput = document.querySelectorAll(".adf-section [readonly]");
    _getAllInput.forEach(function(index, array, element){
        index.removeAttribute("readonly");
    })
    document.querySelector("#cancel-fields").classList.remove("d-none");
}

function resetEdit(){
    var _getAllInput = document.querySelectorAll(".adf-section [data-dynamic]");
    _getAllInput.forEach(function(index, array, element){
        if(index.value != ""){
            var _createAtt = document.createAttribute("readonly");
            document.querySelector("[data-dynamic='"+index.getAttribute('data-dynamic')+"']").setAttributeNode(_createAtt);
        }
    })
    document.querySelector("#edit-fields").classList.remove("d-none");
    document.querySelector("#cancel-fields").classList.add("d-none");
}


function fetchMerchantMappingData(payId){
    var _payId = payId;
    var _subMerchant = "";
    var _isVisible = document.querySelector("#subMerchant");
        if(_isVisible.closest(".d-none") == null){
            _subMerchant = _isVisible.value;
        }else{
            _subMerchant = null
        }
    document.querySelector("body").classList.remove("loader--inactive");
    $('#payoutMapping_table').dataTable( {
        "ajax" : {

			"url" : "fetchAllPayoutMerchantMapppingRecord",
			"type" : "POST",
			"data" : {
                "payId" : _payId,
                "subMerchantPayId" : _subMerchant
            }

		},
        "fnDrawCallback" : function(settings, json) {  
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500)
        },
        "destroy" : true,
        "sAjaxDataProp" : "merchantMappedData",
        "aoColumns": [
            { "mData": "payId" },
            { "mData": "merchantName" },
            { "mData": null, "mRender" : function(row){ 
                if(row.subMerchantName == null){
                    return "NA"
                }else{
                    return row.subMerchantName
                }
            } },
            { "mData": null, "mRender" : function(row){
                if(row.subMerchantPayId == null){
                    return "NA"
                }else{
                    return row.subMerchantPayId
                }
            } },
            { "mData": "bankName" },
            { "mData": "van" },
            { "mData": "vanIfsc" },
            { "mData": "userType" },
            { "mData": "accountType"},
            { 
                "mData": null,
                "mRender" : function(row){
                        return "<div class='action-btn'><input type='hidden' class='table_payId' value='"+row.payId+"' /><input type='hidden' class='subMerchantPayId' value='"+row.subMerchantPayId+"' /><input type='hidden' class='subMerchantName' value='"+row.subMerchantName+"' /><span class='edit-button'><i class='fa fa-pencil-square-o' aria-hidden='true'></i></span><span class='dlt-button'><i class='fa fa-trash' aria-hidden='true'></i></span></div>"
                    
                }
            }
        ]
    })
}

fetchMerchantMappingData("ALL");

$("body").on("click", ".edit-button", function(e){
    var _value = $(this).closest("tr").find(".table_payId").val();
    var _subMerchant = $(this).closest("tr").find(".subMerchantPayId").val();
    var _subMerchanName = $(this).closest("tr").find(".subMerchantName").val();
    console.log(_subMerchanName);
    $.ajax({
        type: "POST",
        url: "fetchPayoutMerchantMappping",
        data: {
            "payId" : _value,
            "subMerchantPayId" : _subMerchant,
            "subMerchantName" : _subMerchanName
        },
        success: function(data){
            console.log(data);
            $("[data-payout]").removeClass("d-none");
            document.querySelector("#subMerchant").closest(".col-md-3").classList.add("d-none");
            for(key in data){
                var _ifAvailable = document.querySelector("[data-target='merchantMapping']").querySelector("[data-var='"+key+"']");
                if(_ifAvailable != null){
                    if(_ifAvailable.id != "subMerchant"){
                        if(_ifAvailable.nodeName == "SELECT" || _ifAvailable.nodeName == "select"){
                            $("#"+_ifAvailable.id).selectpicker("val", data[key]);
                            if(_ifAvailable.id == "merchant"){
                                $("#"+_ifAvailable.id).prop('disabled', true);
                            }
                            $("#"+_ifAvailable.id).selectpicker('refresh');

                        }
                    }else{
                        if(_subMerchanName != 'null'){
                            var _option = "<option value='"+data['subMerchantPayId']+"'>"+data['subMerchantName']+"</option>";
                            document.querySelector("#"+_ifAvailable.id).innerHTML = _option;
                            $("#"+_ifAvailable.id).prop('disabled', true);
                            $("#"+_ifAvailable.id).selectpicker('refresh');
                            document.querySelector("#"+_ifAvailable.id).closest(".col-md-3").classList.remove("d-none");
                        }
                    }
                }

            }
        }
    })
})


// $("body").on("click", ".dlt-button", function(e){
//     var _value = $(this).closest("tr").find("input").val();
//     $.ajax({
//         type: "POST",
//         url: "deletePayoutMerchantRecord",
//         data: {
//             "payId" : _value
//         },
//         success: function(data){

//         }
//     })
// })

function deleteAction(e){
        
    var _parent = $(".active-tr");
    var _payId = _parent.find("input").val();
    var _subMerchantId = _parent.find(".subMerchantPayId").val();
    $("body").removeClass("loader--inactive");
    $.ajax({
        type: "POST",
        url: "deletePayoutMerchantRecord",
        data: {"payId": _payId, "subMerchantPayId" : _subMerchantId},
        success: function(data){
            $(".responseMsg").text(data.responseMsg);
            if(data.response == "success"){
                $(".lpay_popup-innerbox").attr("data-status", "success");
            }else{
                $(".lpay_popup-innerbox").attr("data-status", "error");
            }
            $(".lpay_popup").fadeIn();
            setTimeout(function(){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        },
        error: function(data){
        }
    });
}

$("body").on("click", ".dlt-button", function(e){
    e.preventDefault();
    var _parent = $(this).closest("tr");
    $("#payoutMapping_table tr").removeClass("active-tr");
    _parent.addClass("active-tr");
    $("#fancybox").fancybox({
        'overlayShow': true
    }).trigger('click');
});

$("body").on("click", "#confirm-btn", function(e){
    deleteAction();
    $.fancybox.close();
});
$("body").on("click", "#cancel-btn", function(e){
    $.fancybox.close();
})


function reset(){
    var _getAllPayout = document.querySelectorAll("[data-payout]");
    _getAllPayout.forEach(function(index, array, element){
        index.classList.add("d-none");
    })
    $(".selectpicker").val('default');
    $(".selectpicker").prop('disabled', false);
    $(".selectpicker").selectpicker('refresh');
    fetchMerchantMappingData("ALL");
    // handleChangeBankName()
    // document.querySelector("#subMerchant").innerHTML = "";
    document.querySelector("#subMerchant").closest(".col-md-3").classList.add("d-none");
    document.querySelector("[data-target='merchantMapping'] .lpay_table_style-2").classList.remove("d-none");
}

// reset tab click 
function resetTab(){
    document.querySelector(".adf-section").innerHTML = "";
    var _hide = document.querySelectorAll("[data-hide]");
    _hide.forEach(function(index, array, element){
        index.classList.add("d-none");
    })
    createAdf(21);
    $(".selectpicker").val('default');
    $(".selectpicker").prop("disabled", false);
    $(".selectpicker").selectpicker('refresh');
    document.querySelector("#subMerchant").closest(".col-md-3").classList.add("d-none");
}