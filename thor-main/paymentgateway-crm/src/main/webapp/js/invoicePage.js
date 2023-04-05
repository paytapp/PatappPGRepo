$(document).ready(function() {
    $('.month').datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'MM-yy',
        showOtherMonths : true,
        selectOtherMonths : false,
        maxDate : new Date(),
        
        onClose: function() {
            var iMonth = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var iYear = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).datepicker('setDate', new Date(iYear, iMonth, 1));
        },
        
        beforeShow: function() {
        if ((selDate = $(this).val()).length > 0) 
        {
            iYear = selDate.substring(selDate.length - 4, selDate.length);
            iMonth = jQuery.inArray(selDate.substring(0, selDate.length - 5), $(this).datepicker('option', 'monthNames'));
            $(this).datepicker('option', 'defaultDate', new Date(iYear, iMonth, 1));
            $(this).datepicker('setDate', new Date(iYear, iMonth, 1));
        }
        }
    });
    // $(".month").focus();

    // $("#hsrNumber").focus();
});


   

function getSubMerchant(_this, _url, _object){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
    if(_merchant != ""){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                // console.log(obj);
                var  _option = "";
                if(_object.isSuperMerchant == true){
                    if(document.querySelector("#"+_subMerchantAttr).closest(".hasError") != null){
                        console.log(document.querySelector("#"+_subMerchantAttr).closest(".invoice-page-input").classList);
                        document.querySelector("#"+_subMerchantAttr).closest(".invoice-page-input").classList.remove("hasError");   
                    }
                    if(obj.superMerchant == true){
                        document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                        
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subMerchantAttr+" option[value='ALL']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker('refresh');
                        $("#"+_subMerchantAttr).selectpicker();
                    }else{
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
                }
                if(_object.subUser == true){
                    if(obj.subUserList.length > 0){
                        _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subUserList.length; i++){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subUserAttr+" option[value='ALL']").selected = true;
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
        if(document.querySelector("#"+_subMerchantAttr).closest(".hasError") != null){
            console.log(document.querySelector("#"+_subMerchantAttr).closest(".invoice-page-input").classList);
            document.querySelector("#"+_subMerchantAttr).closest(".invoice-page-input").classList.remove("hasError");   
        }
        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
        document.querySelector("#"+_subMerchantAttr).value = "";

    }
}

document.querySelector("#merchant").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true,
    });
});

document.querySelector("#merchantReporting").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true,
    });
});



function checkRegEx(e){
    var _getRegEx = e.getAttribute("data-regex");
    var _newRegEx = new RegExp(_getRegEx);
    var _value = e.value;
    if(_value != ""){
        if(_newRegEx.test(_value) != true){
            var _getLabel = e.closest(".invoice-page-input").querySelector("label").innerText;
            e.closest(".invoice-page-input").querySelector(".error-field").innerText = "Invalid "+_getLabel;
            e.closest(".invoice-page-input").classList.add("hasError");
        }
    }else{
        e.closest(".invoice-page-input").classList.remove("hasError");
    }
}


var _input = "";
function createJsonData(){
    _input = "";
    var _obj = {};
    var _allInput = document.querySelectorAll("[data-var]");
   
    _allInput.forEach(function(index, array, element){
        var _getAttr = index.attributes['data-var'].nodeValue;
        var _closest = index.closest(".d-none");
        if(index.attributes['data-required'] != null){
            var _required = index.attributes['data-required'].nodeValue;
        }else{
            var _required = "false"; 
        }
        if(_closest == null && _required == "true"){
            if(index.value == ""){
                if(index.closest(".invoice-page-input").querySelector(".error-field") != null){
                    index.closest(".invoice-page-input").classList.add("hasError");
                    index.closest(".invoice-page-input").querySelector(".error-field").innerText = "Should not be blank";
                }
            }else{
                index.closest(".invoice-page-input").classList.remove("hasError");
                _obj[_getAttr] = index.value;
                _input += "<input type='hidden' name='"+_getAttr+"' value='"+index.value+"' />";
            }
        }
    })
    return _obj;
}

function removeError(e){
    e.closest(".invoice-page-input").classList.remove("hasError");
}

// document.querySelector("#download-btn").onclick = function(e){
// var _obj = createJsonData();
// var _setError = document.querySelector("#invoiceNumber");
// console.log(_setError.length);
// if(_setError.value.length != 9){
// document.querySelector("#invoiceNumber").closest(".invoice-page-input").querySelector(".error-field").innerText
// = "Invalid invoice number";
// document.querySelector("#invoiceNumber").closest(".invoice-page-input").classList.add("hasError");
// return false;
// }
// var _checkError = document.querySelector(".hasError");
// if(_checkError == null){
// document.querySelector("#downloadInvoice").innerHTML = _input;
// document.querySelector("#downloadInvoice").submit();
// }
// }

$("body").on("click", ".actButton", function(e){
    document.querySelector("#downloadInvoice").innerHTML = "";
    var table = new $.fn.dataTable.Api('#monthlyInvoice-table');
    var _getClosestTr = $(this).closest("tr");
	var _data = table.rows(_getClosestTr).data();
    // console.log(_data);
    var _option = "<input type='hidden' value='"+_data[0]['merchantPayId']+"' name='payId' />";
    _option += "<input type='hidden' name='subMerchantPayId' value='"+_data[0]['subMerchantPayId']+"' />";
    _option += "<input type='hidden' name='invoiceMonth' value='"+_data[0]['invoiceMonth']+"' />";
    _option += "<input type='hidden' name='invoiceNo' value='"+_data[0]['invoiceNo']+"' />";
    _option += "<input type='hidden' name='filename' value='"+_data[0]['fileName']+"' />";
    document.querySelector("#downloadInvoice").innerHTML = _option;
    // return false;
    document.querySelector("#downloadInvoice").submit();
})



function generateFile(_selector){
    var _obj = createJsonData();
    var _checkNull = document.querySelector(".hasError");
    var _setError = document.querySelector("#invoiceNumber");
    if(_selector == "Y"){
        _obj.newfile = _selector;
    }
    if(_checkNull == null){
        if(_setError.value.length == 9){
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "POST",
                url: "generateMonthlyInvoice",
                data: _obj,
                success: function(data){
    
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();                    
    
                    if(data.status == "READY") {
                        $(".responseMsg").text("File already exists. Please see in downloads. Do you want to create new file again?");
                        $(".lpay_popup-innerbox").attr("data-type", data.status);
                        $(".confirm-button").text("Yes");
                    } else if(data.status == "PROCESSING") {
                        $(".responseMsg").text("File is processing. Please see in downloads after some time.");
                        $(".lpay_popup-innerbox").attr("data-type", data.status);
                        $(".confirm-button").text("Ok");
                    } else {
                        $(".responseMsg").text("File is generating. Please see in downloads after some time.");
                        $(".lpay_popup-innerbox").attr("data-type", "GENERATING");
                        $(".confirm-button").text("Ok");
                    }
    
                    setTimeout(() => {
                        $("body").addClass("loader--inactive");
                    }, 1000);
                },
                error: function(){
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text("Something went wrong! Try again.");
                }
            })
        }else{
            alert("Enter valid invoice number");
        }
    }
}

$(".confirm-button").on("click", function(e) {

    var dataType = $(this).closest(".lpay_popup-innerbox").attr("data-type");
    if(dataType == "READY"){
        generateFile("Y");
    }
    reset();
    
    var $innerText = $(this).text();
    if($innerText == "Ok") {
        location.reload(true);
    }
    
    $(".lpay_popup").fadeOut();

});

$(".cancel-button").on("click", function(e){
    reset();
    $(".lpay_popup").fadeOut();
})


function reset(){
    document.querySelector("#month").value = "";
    document.querySelector("#invoiceNumber").value = "21-22/";
    document.querySelector("#hsrNumber").value = "";
    $("#merchant").selectpicker('val', 'default');
    $("#merchant").selectpicker('refresh');
    $("[data-target='subMerchant']").addClass("d-none");
}

function getInvoices(){
	var _payId = document.querySelector("#merchantReporting").value;
    var _subMerchant = document.querySelector("#subMerchantReporting").value;
    var _monthFrom = document.querySelector("#monthFrom").value;
    var _monthTo = document.querySelector("#monthTo").value;
    var _invoiceNumber = document.querySelector("#invoiceNumberView").value;
    var _hsrNumber = document.querySelector("#hsrNumberView").value;
    document.querySelector("body").classList.remove("loader--inactive");
    var data = {
        payId: _payId,
        subMerchantPayId: _subMerchant,
        monthFrom: _monthFrom,
        monthTo : _monthTo,
        invoiceNo: _invoiceNumber,
        hsnSac: _hsrNumber
    }
    $("#monthlyInvoice-table").DataTable({
        "destroy": true,
        "order" : [["2", "desc"]],
        "ajax" : {

            "url" : "gettingMonthlyInvoiceFileData",
            "type" : "POST",
            "data" : data,
        },
        "fnDrawCallback" : function(settings, json) {
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        },
        "aoColumns": [
            { "mData": "merchantName" },
            { "mData": "subMerchantName" },
            { "mData": "createDate" },
            { "mData": "invoiceMonth" },
            { "mData": "invoiceNo" },
            { "mData": "hsnNo" },
            { "mData": "fileName"},
            { 
               "mData": null,
               "mRender" : function(row){
                   return "<button class='lpay_button lpay_button-md lpay_button-primary actButton'>Download</button>";
               }
            },

           
        ]
    })
}

getInvoices();

