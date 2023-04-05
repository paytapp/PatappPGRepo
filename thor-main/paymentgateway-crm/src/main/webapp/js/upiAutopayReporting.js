
$(document).ready(function() {


    $("body").on("click", ".confirmButton", function(e){
        $(".lpay_popup").fadeOut();
        // renderTable();
        var _checkDebitTab = document.querySelector("[data-target='debitDuration']").classList.toString();
        var _checkLinkTab = document.querySelector("[data-target='quickLinks']").classList.toString();
        if(_checkDebitTab.indexOf("d-none") == -1){
            viewDebitDuration();
            $(".selectpicker").val('default');
            $(".selectpicker").selectpicker('refresh');
        }

        if(_checkLinkTab.indexOf("d-none") == -1){
            reset("quickLinks");
            $(".btn-mandate-link").attr("disabled", true);
        }

    })

    //reset input

    function reset(_selector){
        var _parent = document.querySelector("[data-target='"+_selector+"']");
        var _input = _parent.querySelectorAll("input");
        _input.forEach(function(index, array, element){
            var _array = index.classList.toString();
            if(_array.indexOf("datepick") == -1){
                index.value = "";
            }
        })
        $(".selectpicker").val('default');
        $(".selectpicker").selectpicker('refresh');
        document.querySelector("[data-hide='subMerchant']").classList.add("d-none");
    }

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

    function format ( d ) {
        console.log(_check);
        if(_check == "upiAutopay-transactionReport"){
            var _obj = {
                "subMerchantName" : "Sub Merchant Name",
                "rrn" : "RRN",
                "payId" : "Pay ID",
                "custEmail" : "Cust Email",
                "custPhone" : "Cust Phone",
                "payerAddress" : "Payer Address",
                "frequency" : "Frequency",
                "tenure" : "Tenure",
                "startDate" : "Debit Start Date",
                "endDate" : "Debit End Date",
                "totalAmount" : "Total Amount",
                "reponseMsgKey" : {
                    "responseMsg" : "Response Message",
                    "className" : "w-fluid"
                },
                "remarks" : "Remarks"
            }
        }else{
            var _obj = {
                "subMerchantName" : "Sub Merchant Name",
                "pgRefNum" : "Pg Reg Num",
                "rrn" : "RRN",
                "payId" : "Pay ID",
                "umnNumber" : "UMN",
                "custEmail" : "Cust Email",
                "custPhone" : "Cust Phone",
                "payerAddress" : "Payer Address",
                "frequency" : "Frequency",
                "tenure" : "Tenure",
                "startDate" : "Debit Start Date",
                "endDate" : "Debit End Date",
                "maxAmount" : "Debit Amount",
                "acquirerCharges" : "Acquirer Charges",
                "reponseMsgKey" : {
                    "responseMsg" : "Response Message",
                    "className" : "w-fluid"
                },
                "remarks" : "Remarks"
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

        _new += "</div>";
    
        return _new;
       
    }

    function createJson(_selector, _id, _url){
        var _input = document.querySelectorAll(_selector);
        var _obj = {};
        var _setAttribute = _selector.slice(1, _selector.indexOf("]"));
        _input.forEach(function(index,array,element){
            _obj[index.getAttribute(_setAttribute)] = index.value;
        })
        document.querySelector("body").classList.remove("loader--inactive");
        if(_id == "#upiAutopay-table"){
            var _data = [
                { "mData":"merchantName", "width": "30%" },
                { "mData":"orderId", "width": "10%" },
                { "mData":"createDate", "width": "15%" },
                { "mData":"amount", "width": "15%" },
                { "mData":"totalAmount", "width": "15%" },
                { "mData":"status", "width": "15%" },
                { 
                    "data" : null,
                    "mRender" : function(row){
                    	 if(row.status == "Captured" || row.status == "Failed"){
                             return "NA"
                         }else{
                        	 return "<button class='copy-btn' data-link='"+row.eMandateUrl+"'><i class='fa fa-files-o' aria-hidden='true'></i><span class='copy_txt'>Copy Link</span></button>";
                         }
                    }
                },
                { 
                    "data" : null,
                    "mRender" : function(row){
                        if(row.status == "Captured" || row.status == "Failed" || row.status == "Processing"){
                            return "NA"
                        }else{
                            return "<button class='lpay_button lpay_button-md lpay_button-primary act-btn'><i class='fa fa-share-square-o' aria-hidden='true'></i> Resend Link</button>";
                        }
                    }
                }
                
            ]   
        }else if(_id = "#upiAutopay-transactionReport"){
            var _data = [
                { "mData":"merchantName", "width" : "30%" },
                { "mData":"orderId", "width" : "10%" },
                { "mData":"pgRefNum", "width": "15%" },
                { "mData":"umnNumber", "width": "15%" },
                { "mData":"maxAmount", "width": "15%" },
                { "mData":"status", "width": "15%" }
            ] 
        }
        $(_id).DataTable({
            "ajax": {
                "type": "post",
                "url": _url,
                "data" : _obj,
            },
            "destroy": true,
            "fndrawcallback" : function(setting,json){
                document.querySelector("body").classList.add("loader--inactive");
            },
            "aoColumns" : _data
        })
        document.querySelector("body").classList.add("loader--inactive");
    }

    var _temp = $("<input>");

    $("body").on("click", ".copy-btn", function(e){
        $("body").append(_temp);
        var _url = $(this).attr("data-link");
        _temp.val(_url).select();
        document.execCommand("copy");
        $(".copy-btn").html("<i class='fa fa-clone' aria-hidden='true'></i> Copy Link");
        $(this).html("<i class='fa fa-clone' aria-hidden='true'></i> Copied");
        _temp.remove();
    })

    $("body").on("click", ".act-btn", function(e){
        var table = new $.fn.dataTable.Api('#upiAutopay-table');
		var _getClosestTr = $(this).closest("tr");
		var _data = table.rows(_getClosestTr).data();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "reSendUpiAutopayLink",
            data: {
                "orderId" : _data[0]['orderId'],
                "pgRefNum" : _data[0]['pgRefNum']
            },
            success: function(data){
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".responseMsg").text(data.responseMessage);
                $(".lpay_popup").fadeIn();
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500)
            }
        })
    })


    function dataTable(_selector, _url){
        document.querySelector("body").classList.remove("loader--inactive");
        if(_selector == "#datatable"){
            var _obj = [
                { 
                    "data": null,
                    "width" : "25%",
                    "mRender" : function(row){
                        return "<span>"+row.merchantName+"</span><input type='hidden' class='payId' value='"+row.payId+"' />"
                    }
                },
                
                { 
                    "data": null,
                    "width" : "20%",
                    "mRender" : function(row){
                        return "<span>"+row.subMerchantName+"</span><input type='hidden' class='subMerchantPayId' value='"+row.subMerchantPayId+"' />"
                    }
                },
                { "data": "orderId", "className" : "orderId", "width" : "10%", },
                { "data": "pgRefNum", "className" : "pgRefNum", "width" : "10%", },
                { "data": "umnNumber", "width" : "10%", },
                { "data": "createDate", "width" : "10%", },
                { "data": "maxAmount", "width" : "5%", },
                { 
                    "data": null,
                    "width" : "10%",
                    "mRender" : function(row){
                        return "<button class='lpay_button lpay_button-md lpay_button-primary' onclick='createDownloadForm(this)'>Download</button>";
                    }
                }
            ];
        }else{
            var _obj = [
                { 
                    "data": null,
                    "mRender" : function(row){
                        return "<span>"+row.merchantName+"</span><input type='hidden' class='payId' value='"+row.payId+"' />"
                    }
                },
                
                { 
                    "data": null,
                    "mRender" : function(row){
                        return "<span>"+row.subMerchantName+"</span><input type='hidden' class='subMerchantPayId' value='"+row.subMerchantPayId+"' />"
                    }
                },
                { "data": "orderId", "className" : "orderId" },
                { "data": "pgRefNum", "className" : "pgRefNum" },
                { "data": "regPgRefNum"},
                { "data": "umnNumber"},
                { "data": "regDate" },
                { "data": "notificationDate" },
                { "data": "dueDate" },
                { "data": "payerAddress" },
                { "data": "custEmail" },
                { "data": "custPhone" },
                { "data": "status" },
                { "data": "maxAmount" },
                {
                    "data": null,
                    "mRender" : function(row) {

                        if(row.status == "Notified"){
                            return "<button class='lpay_button lpay_button-md lpay_button-primary payNow'>Pay Now</button>";
                        }
                        else if(row.status == "Pending"){
                            return "<button class='lpay_button lpay_button-md lpay_button-primary payNow'>Notify Now</button>";
                        }else{
                            return "";
                        }
    
                    }
                }
            ];
        }
        $(_selector).dataTable( {
            "ajax" : {
    
                "url" : _url,
                "type" : "POST",
                "data" : function(d) {
                    return generateInputValue(d);
                }
            },
          "destroy" : true,
          "initComplete": function(settings, json) {
              document.querySelector("body").classList.add("loader--inactive");
            //return loaderInactive(settings._value, _selector);
          },
          "aoColumns": _obj,
        });
    }

    $("#datatable").dataTable();

    $("body").on("click", ".payNow",function(e){
        var _parent = $(this).closest("tr");
        var _text = $(this).text();
        var _url = "";
        if(_text == "Pay Now"){
            _url = "autoPayDebitTransactionSchedule"
        } else {
            _url = "autoPayDebitTransactionNotification"
        }
        //console.log(_url);
        var merchantPayId = _parent.find(".payId").val();
        var orderId = _parent.find(".orderId").text();
        var pgRefNumber = _parent.find(".pgRefNum").text();
        $.ajax({
            type: "POST",
            url: _url,
            data: {
                "merchantPayId" : merchantPayId,
                "orderId" : orderId,
                "pgRefNumber" : pgRefNumber
            },
            success: function(data){
                if(data.responseCode == "Failed"){
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.response);
            }
        })
    })

    function classToggle(_selector){
        console.log(_selector);
        document.querySelector(".debit_popup_container").classList[_selector]("d-none");
        document.querySelector(".button-wrapper-debit").classList[_selector]("d-none");
    }

    $("body").on("click", "#datatable tbody tr", function(e){
        if(e.target.localName != 'button'){
            document.querySelector("body").classList.remove("loader--inactive");    
            e.target.closest("tr").classList.add("edit-row");
            setTimeout(function(e){
                classToggle("remove");
                dataTable("#datatablePopup", "autoPayDebitTransactionAction"); //aad action here for debitTransaction payNow
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        }
    })

    $("body").on("click", ".close-btn", function(e){
        document.querySelector("body").classList.remove("loader--inactive");
        document.querySelector("#datatable .edit-row").classList.remove("edit-row");
        setTimeout(function(e){
            classToggle("add");
            document.querySelector("body").classList.add("loader--inactive");
        }, 500);
    });

    document.querySelector("#view").onclick = function(e){
        dataTable("#datatable", "upiAutoPayDebitReportAction"); // add action
    }

    function generateInputValue(d) {
        var _obj = {};
        if(event == undefined){
            var _parent = document.querySelector(".edit-row");
            _obj["orderId"] = _parent.querySelector(".orderId").innerText;
            _obj["pgRefNum"] = _parent.querySelector(".pgRefNum").innerText;
        }else{
            var _data = document.querySelectorAll("[data-debit]");
            _data.forEach(function(index, array, element){
                _obj[index.getAttribute("data-debit")] = index.value;
            })
        }
        
        _obj['status'] = "Captured";
    
        console.log(_obj);
    
        return _obj;
    }

    createJson("[data-var]", "#upiAutopay-table", "upiAutoPayRegistrationReportAction");
    createJson("[data-transaction]", "#upiAutopay-transactionReport", "upiAutoPayTransactionReportAction"); //add action here
   // createJson("[data-debit]", "#upiAutopay-transactionReport", "upiAutoPayDebitReportAction"); // add action here


    document.querySelector("#submit").onclick = function(e){
        createJson("[data-var]", "#upiAutopay-table", "upiAutoPayRegistrationReportAction");
    }

    document.querySelector("#transactionSubmit").onclick = function(e){
        createJson("[data-transaction]", "#upiAutopay-transactionReport", "upiAutoPayTransactionReportAction");
    }

    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        _check = _currentTable;
		if(e.target.localName != "button" && e.target.localName != "input" && e.target.localName != "label" && (_check == "upiAutopay-transactionReport" || _check == "upiAutopay-table")){
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

    // tab creation 
    $(".lpay-nav-link").on("click", function(e){
        var _this = $(this).attr("data-id");
        $(".lpay-nav-item").removeClass("active");
        $(this).closest(".lpay-nav-item").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+_this+"]").removeClass("d-none");
    })

})

// debit duration function

function createVariable(){
    var _parent = document.querySelectorAll(".debit_duration-input [data-duration]");
    var _obj = {};
    _parent.forEach(function(index, array, element){
        var _checkNone = index.closest(".d-none");
        if(_checkNone == null || index.value != ""){
            var _getAttr = index.getAttribute("data-duration");
            _obj[_getAttr] = index.value;
            
        }
    })
    return _obj;
}


function saveDebitDuration(_selector) {
    var _json = createVariable();
    var _checkAll = true;
    for(key in _json) {
        if(_json[key] == "ALL"){
            _checkAll = false;
        }
    }
    if(_checkAll == true){
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type : "POST",
            url: "saveUpiAutoPayDebitDuration",
            data: _json,
            success: function(data) {
                if(data.responseCode == "success") {
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.response);
                setTimeout(function(){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            }
        })
    } else {
        alert("Please select specific value you can't choose ALL");
    }
}

function viewDebitDuration(){
    var _json = createVariable();
    $('#debitDurationTabel').dataTable( {
        "destroy": true,
        "ajax" : {

            "url" : "viewUpiAutoPayDebitDuration",
            "type" : "POST",
            "data" : _json,
        },
        "fnDrawCallback" : function(settings, json) {
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        },
        "aoColumns": [
            { "mData": "PAY_ID", "className" : "PayId", "width" : "10%" },
            { "mData": "MERCHANT_NAME", "width" : "25%" },
            { "mData": "SUB_MERCHANT_ID", "width" : "10%" },
            { "mData": "SUB_MERCHANT_NAME", "width" : "25%"},
            { "mData": "DEBIT_DAY", "className": "debit-column lpay_select_group", "width" : "10%"},
            { 
                "mData": null,
                "width" : "20%",
                "mRender" : function(row){
                        return "<div class='action-btn'><span class='edit-btn' id='edit-row'><i class='fa fa-pencil' aria-hidden='true'></i></span></div><div class='action-btn-edit'><span class='edit-btn' id='save-row'><i class='fa fa-check' aria-hidden='true'></i></span><span class='delete-btn' id='cancel-row'><i class='fa fa-times' aria-hidden='true'></i></span></div>"
                    
                }
            }
        ]
    });
}

$("body").on("click", "#edit-row", function(e){
    $(this).closest("tr").addClass("edit-row");
    var _debitValue = $(this).closest("tr").find(".debit-column").text();
    var _selectpicker = document.querySelector("[data-duration='debitDuration']").innerHTML;
    var _createSelect = document.createElement("select");
    var _createAttr = document.createAttribute("id");
    _createAttr.value = "debitEditDuration";
    _createSelect.setAttributeNode(_createAttr);
    _createSelect.innerHTML = _selectpicker;
    
    $(".edit-row").find(".debit-column").append(_createSelect);
    $("#debitEditDuration").selectpicker();
    $('#debitEditDuration').find('[value=ALL]').remove();
    $("#debitEditDuration").selectpicker('refresh');
    $("#debitEditDuratioin").selectpicker('val', _debitValue);

})

$("body").on("click", "#save-row", function(e){
    var _payId = $(this).closest("tr").find(".PayId").text();
    var _debitDuration = $(this).closest("tr").find("#debitEditDuration").val();
    console.log(_payId);
    console.log(_debitDuration);
    document.querySelector("body").classList.remove("loader--inactive");
    $(".edit-row").find(".bootstrap-select").remove()
    $("#debitEditDuration").remove();
    $(this).closest("tr").removeClass("edit-row");
    $.ajax({
        type: "post",
        url: "editUpiAutoPayDebitDuration",
        data: {
            "merchantPayId" : _payId,
            "debitDuration" : _debitDuration
        },
        success: function(data){
            if(data.responseCode == "success"){
                $(".lpay_popup-innerbox").attr("data-status", "success");
            }else{
                $(".lpay_popup-innerbox").attr("data-status", "error");
            }
            $(".lpay_popup").fadeIn();
            $(".responseMsg").text(data.response);
            setTimeout(function(){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        }
    })
})

$("body").on("click", "#cancel-row", function(e){
    $(".edit-row").find(".bootstrap-select").remove()
    $("#debitEditDuration").remove();
    $(this).closest("tr").removeClass("edit-row");
})

if($("#userType").val() == "ADMIN" || $("#userType").val() == "SUBADMIN") {
    viewDebitDuration();
}

// create function for link
function sendLink(_that) {
    var _linkThroug = _that.getAttribute("data-info"),
        _obj = {},
        _selectInput = document.querySelectorAll("[data-link]");

    _selectInput.forEach(function(element) {
        if(element.value != "") {
            _obj[element.getAttribute("data-link")] = element.value;
        } else if(element.value == "" && element.closest(".d-none") == null) {
            element.closest(".single-account-input").querySelector(".error-field").innerText = "Should not be blank";
            element.closest(".single-account-input").classList.add("hasError");
        }
    });
    
    var _checkError = document.querySelector(".hasError");

    if(_checkError == null) {
        _obj['linkThrough'] = _linkThroug;
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "upiAutopayMandateThroughLinkAction",
            data: _obj,
            success: function(data) {
                if(data.response == "success") {
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                } else {
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }

                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.responseMessage);

                setTimeout(function(e) {
                    document.querySelector("body").classList.add("loader--inactive");
                }, 1000)
            }
        });
    }
}

// enable/disabled button on condition
function enableDisabledButton(_that) {
    var mandateLinkFields = document.querySelectorAll(".single-account-input:not(.d-none)"),
        count = 0;
    
    mandateLinkFields.forEach(function(element) {
        var field = element.querySelector(".mandate-link-field");

        if(field.tagName == "SELECT" || field.tagName == "INPUT") {
            if(field.value !== "") {
                count++;
            }
        }
    });

    if(count == mandateLinkFields.length) {
        $(".btn-mandate-link").attr("disabled", false);
    } else {
        $(".btn-mandate-link").attr("disabled", true);
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

function removeErrorLink(e){
    e.closest(".required").classList.remove("hasError");
}

function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue) {
    var _merchant = _this.target.value;        

    if(_merchant != "" && _merchant != "ALL"){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function() {
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                var  _option = "";
                if(_object.isSuperMerchant == true) {
                    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;

                    if(obj.superMerchant == true) {
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
                if(_object.subUser == true) {
                    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
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

document.querySelector("#merchant").addEventListener("change", function(_this) {
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

document.querySelector("#merchantTransaction").addEventListener("change", function(_this) {
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

if(document.querySelector("#merchantDebitDuration") !== null) {
    document.querySelector("#merchantDebitDuration").addEventListener("change", function(_this){
        getSubMerchant(_this, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });
}

if(document.querySelector("#merchantDebit") !== null) {
    document.querySelector("#merchantDebit").addEventListener("change", function(_this){
        getSubMerchant(_this, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });
}

if(document.querySelector("#merchantLink") != null) {
    document.querySelector("#merchantLink").addEventListener("change", function(_this) {
        getSubMerchant(_this, "getSubMerchantList", {
            isSuperMerchant : true
        }, "Select Sub-Merchant", "");
    })
}

function downloadInput(_selector){
    document.querySelector("#downloadForm").innerHTML = "";
    var _getAllInput = document.querySelectorAll(_selector);
    var _option = "";
    _getAllInput.forEach(function(index, element, array){
    var _new =  _getAllInput[element].closest(".col-md-3").classList;
    var _newVal = _new.toString().indexOf("d-none");
        if(_newVal == -1){
            _option += "<input type='hidden' name='"+_getAllInput[element].name+"' value='"+_getAllInput[element].value+"' />"; 
        }
    })
    if(_selector == "[data-var]"){
        document.querySelector("#downloadForm").action = "downloadRegistrationReportAction";
        
    }else if(_selector == "[data-transaction]"){
        document.querySelector("#downloadForm").action = "downloadTransactionReportAction";
    }
    document.querySelector("#downloadForm").innerHTML += _option;
    document.querySelector("#downloadForm").submit();
}

document.querySelector("#registrationDownload").onclick = function(e){
    downloadInput("[data-var]");
}

document.querySelector("#downloadTransaction").onclick = function(e){
    console.log("HI");
    downloadInput("[data-transaction]");
}


function createDownloadForm(e){
    var _input = "";
    var _checkWhichClick = e.closest("tr");
    // console.log(_checkWhichClick);
    if(_checkWhichClick == null){
        document.querySelector("#downloadForm").setAttribute("action", "downloadAutoPayDebitWithFilter");
        var _getAllInput = document.querySelectorAll("[data-debit]");
        _getAllInput.forEach(function(index, array, element){
            var _notVisible = index.closest(".d-none");
            if(_notVisible == null){
                _input += "<input type='hidden' name='"+index.getAttribute('data-debit')+"' value='"+index.value+"' />";
            }
        })
    }else{
        document.querySelector("#downloadForm").setAttribute("action", "downloadAutoPayDebitIndividual");
        var _orderId = _checkWhichClick.querySelector(".orderId").innerText;
        var _merchantPayId = _checkWhichClick.querySelector(".payId").value;
        if(_checkWhichClick.querySelector(".subMerchantPayId") != null){
            var _subMerchant = _checkWhichClick.querySelector(".subMerchantPayId").value;
            _input += "<input type='hidden' name='subMerchantPayId' value='"+_subMerchant+"' />";
        }
        _input += "<input type='hidden' name='orderId' value='"+_orderId+"' />";
        _input += "<input type='hidden' name='merchantPayId' value='"+_merchantPayId+"' />";
    }
    _input += "<input type='hidden' name='status' value='Captured' />";
    document.querySelector("#downloadForm").innerHTML = _input;
    // return false;
    document.querySelector("#downloadForm").submit();
}


function downloadDebitDuration(_selector){
    var _json = createVariable();
    document.querySelector("#downloadFormDebitDuration").innerHTML = "";
    for(key in _json){
        var _option = "<input type='hidden' name='"+key+"' value='"+_json[key]+"' />";
        document.querySelector("#downloadFormDebitDuration").innerHTML += _option;
    }
    document.querySelector("#downloadFormDebitDuration").submit();
}

var amount = function(event, _this){
    var _val = _this.value;
    if(_val.length == 1){
        if(_val.indexOf("0") != -1){
            _this.value = _val.slice(0, _val.length-1);
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