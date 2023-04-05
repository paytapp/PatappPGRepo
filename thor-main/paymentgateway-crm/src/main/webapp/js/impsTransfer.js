// to get data of IMPS
function format ( d ) {
// `d` is the original data object for the row
	d.new = function(){
		if(d.status == "Timeout"){
            return "<button class='lpay_button lpay_button-md lpay_button-secondary statusEnquiry'>Get Status</button>"
        } else if(d.status != "Captured" && (d.userType == "PG Initiated" || d.userType == "Merchant Initiated Indirect")){
            return "<button class='lpay_button lpay_button-md lpay_button-secondary reInitiate'>Re-Initiate</button>"
        }else if(d.status != "Captured" && d.userType == "Merchant Initiated Direct"){
            return "<button class='lpay_button lpay_button-md lpay_button-secondary merchantReInitiateDirect'>Re-Initiate</button>"
        }else{
            return "";
        }
	}
	return '<div class="main-div">'+
		'<div class="inner-div">'+
			'<span>Pay ID</span>'+
			'<span>'+d.merchantPayId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Sub Merchant</span>'+
			'<span>'+d.subMerchant+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>IMPS Ref Number</span>'+
			'<span class="">'+d.impsRefNum+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Captured Date From</span>'+
			'<span class="captureDateFrom">'+d.txnsCapturedFrom+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Capture Date To</span>'+
			'<span class="captureDateTo">'+d.txnsCapturedTo+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Settle Date</span>'+
			'<span class="settledDate">'+d.systemSettlementDate+'</span>'+
		'</div>'+
		// '<div class="inner-div">'+
		// 	'<span>Channel</span>'+
		// 	'<span>'+d.userType+'</span>'+
		// '</div>'+
		'<div class="inner-div">'+
			'<span>Mobile</span>'+
			'<span>'+d.phoneNo+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Beneficiary Name</span>'+
			'<span>'+d.bankAccountName+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Bank Account Number</span>'+
			'<span>'+d.bankAccountNumber+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Bank IFSC</span>'+
			'<span>'+d.bankIFSC+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Response Message</span>'+
			'<span>'+d.responseMsg+'</span>'+
		'</div>'+
		
		'<div class="inner-div" style="text-align: center;width: 100%">'+
			'<span></span>'+
			'<span>'+d.new()+'</span>'+
		'</div>'+
	'</div>';
}

// to get data of PG initiated
function formatPgInitiated ( d ) {
    // `d` is the original data object for the row
   
    return '<div class="main-div">'+
        '<div class="inner-div">'+
            '<span>Sub Merchant Name</span>'+
            '<span>'+d.subMerchant+'</span>'+
        '</div>'+
        '<div class="inner-div">'+
            '<span>Mobile</span>'+
            '<span class="">'+d.phoneNo+'</span>'+
        '</div>'+
        '<div class="inner-div">'+
            '<span>Beneficiary Name</span>'+
            '<span>'+d.bankAccountName+'</span>'+
        '</div>'+
        '<div class="inner-div">'+
            '<span>Bank Account Number</span>'+
            '<span>'+d.bankAccountNumber+'</span>'+
        '</div>'+
        '<div class="inner-div">'+
            '<span>Bank IFSC</span>'+
            '<span class="settledDate">'+d.bankIFSC+'</span>'+
        '</div>'+
        '<div class="inner-div">'+
            '<span>Response Message</span>'+
            '<span>'+d.responseMsg+'</span>'+
        '</div>'+
    '</div>';
}

$(document).ready(function(e){
    var _getLengthTh = document.querySelector("#selectRow");
    if(_getLengthTh != null){
        var _getTh = document.querySelector("#selectRow");
        _getTh.onclick = function(e){
            var _getLabel = [];
            var _count = 0;
            var _getTR = document.querySelectorAll("#datatablePayout tbody tr");
            _getTR.forEach(function(ind, arr, ele){
                _getLabel.push(_getTR[arr].querySelector("label"));
                var _checkLabel = _getTR[arr].querySelector("label");
                if(_checkLabel != null){
                    _count++;
                }
            })
            if(_count > 0){
                if(e.target.innerText == "Select All"){
                    document.querySelector("#selectAll").classList.remove("d-none");
                    _getTR.forEach(function(index, array, element){
                        if(_getTR[array].querySelector("label") != null){
                            _getTR[array].querySelector(".check input[type='checkbox']").checked = true;
                            _getTR[array].querySelector("label").classList.add("checkbox-checked");
                        }
                    })
                    document.querySelector("#selectRow").innerText = 'Deselect All';
                    return false;
                }
                if(e.target.innerText == "Deselect All"){
                    document.querySelector("#selectAll").classList.add("d-none");
                    _getTR.forEach(function(index, array, element){
                        if(_getTR[array].querySelector("label") != null){
                            _getTR[array].querySelector(".check input[type='checkbox']").checked = false;
                            _getTR[array].querySelector("label").classList.remove("checkbox-checked");
                        }
                    })
                    document.querySelector("#selectRow").innerText = 'Select All';
                }
            }
        }
}

    $("body").on("change", ".check input[type='checkbox']", function(e) {
        var getInput = $(this);
        var getId = getInput[0].id;
        if(getInput[0].checked == true) {
            $(this).closest("label").addClass("checkbox-checked");
        } else {
            $(this).closest("label").removeClass("checkbox-checked");
        }
    });

})

var _getLength = document.querySelector("#selectAll");
if(_getLength != null){
    document.querySelector("#selectAll").onclick = function(e) {
        var _getTr = document.querySelectorAll("#datatablePayout tbody tr");
        var _txnId = [];
        if(_getTr.length > 0) {
            $("body").removeClass("loader--inactive");
            _getTr.forEach(function(index, array, element){
                if(index.querySelector("td .selectCheckbox") != null){
                    var _get = index.querySelector("td .selectCheckbox").checked;
                    if(_get == true){
                        var _payId = index.querySelector("td.txnId").innerText;
                        _txnId.push(_payId);
                    }
                }
            });
            $.ajax({
                type : "POST",
                url : "allInitiatedTransaction",
                data : {
                    "txnId" : _txnId.toString()
                },
                success: function(data){
                    document.querySelector("#selectAll").classList.add("d-none");
                    document.querySelector("#selectRow").innerText = 'Select All';
                    if(data.response == "success"){
                        setTimeout(function(e){
                            $(".lpay_popup-innerbox").attr("data-status", "success");
                            $(".lpay_popup").fadeIn();
                            $(".responseMsg").text(data.responseMsg);
                            $("body").addClass("loader--inactive");
                        }, 2000);
                    }else{
                        $(".lpay_popup-innerbox").attr("data-status", "error");
                        $(".responseMsg").text(data.responseMsg);
                        $(".lpay_popup").fadeIn();
                        $("body").addClass("loader--inactive");
                    }
                }
            })
        }
    }
}


function hideColumnPayout(){
    var _userType = $("#userType").val();
    var _userLogin = $("#setSuperMerchant").val();
    var _isSuperMerchant = $("#isSuperMerchant").val();
    console.log(_userLogin);
    if(_userLogin == "true"){
        _isSuperMerchant = "Y"
    }            

    var _table = $("#datatablePayout").DataTable();

    _table.columns(2).visible(true);

    if(_userType == "MERCHANT" && _userLogin == "true"){

         _table.columns(13).visible(false);
         _table.columns(2).visible(true);
         _table.columns(0).visible(false); 

    }

    if(_userType == "MERCHANT" && _userLogin == "false"){

        

               _table.columns(13).visible(false);
                _table.columns(2).visible(false); 
                _table.columns(0).visible(false); 

         }
    
    
    if(_isSuperMerchant == "N"){

        _table.columns(2).visible(false);

    }
    
}




document.querySelector(".dataPayoutDownload").onclick = function(e){
    _getAllInput = document.querySelectorAll("[data-download]");
    _getAllInput.forEach(function(index, array, element){
        var _getId = index.attributes['data-download'].nodeValue;
        document.querySelector("#"+_getId).value = index.value;
    })
    $("#downloadPayout").submit(); 
}

// only letters
function onlyLetters(event) {
var x = event.keyCode;
if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
    
} else {
    event.preventDefault();
}
}

function lettersAndAlphabet(event) {
var x = event.keyCode;
if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
} else {
    event.preventDefault();
}
}

// only digit 

function onlyDigit(event){
var x = event.keyCode;
if (x > 47 && x < 58 || x == 32) {
} else {
    event.preventDefault();
}
}

// only digit 



function digitDot(event) {
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46) {
    } else {
        event.preventDefault();
    }
}
$(document).ready(function(e){

    function hideColumn(){
        var _userType = $("#userType").val();
        var _table = $("#datatablePayout").DataTable();
        console.log(_table);
        console.log(_userType);
        if(_userType == "MERCHANT"){
            _table.columns(0).visible(false);
            _table.columns(7).visible(false);
        }
    }

    var _userType = $("#userType").val();
    console.log(_userType);

    if($("#userType").val() != "SUBADMIN" && $("#userType").val() != "ADMIN"){
        $("[data-id='impsTransfer']").closest("li").removeClass("active").addClass("d-none");
        $("[data-target='impsTransfer']").addClass("d-none");
        $("[data-target='pgPayout']").removeClass("d-none");
        $("[data-id='pgPayout']").closest("li").addClass("active");
    }

    // supermerchant function
    function getSubMerchant(_this){
        var _merchant = _this.target.value;
        var _subMerchantAttr = _this.target.attributes["data-get"].nodeValue;
        if(_merchant != ""){
            document.querySelector("body").classList.remove("loader--inactive");
            var data = new FormData();
            data.append('payId', _merchant);
            var _xhr = new XMLHttpRequest();
            _xhr.open('POST', "getSubMerchantListByPayId", true);
            _xhr.onload = function(){
                if(_xhr.status === 200){
                    var obj = JSON.parse(this.responseText);
                    document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                    var  _option = "";
                    if(obj.superMerchant == true){
                        $("#isSuperMerchant").val("Y");
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subMerchantAttr+" option[value='ALL']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker();
                        $("#"+_subMerchantAttr).selectpicker("refresh");
                        setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                    }else{
                        $("#isSuperMerchant").val("N");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                    }
                    
                }
            }
            _xhr.send(data);
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 1000);
        }else{
            $("#isSuperMerchant").val("Y");
            document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
            document.querySelector("#"+_subMerchantAttr).value = "";
        }
    }

    var _getMerchantId = document.querySelector("#merchantReportPayId");
    var _merchantPayoutId = document.querySelector("#merchantPayoutPayId");
    if(_getMerchantId != null){
        document.querySelector("#merchantReportPayId").addEventListener("change", function(e){
            getSubMerchant(e);
        });
    }
    if(_merchantPayoutId != null){
        document.querySelector("#merchantPayoutPayId").addEventListener("change", function(e){
            getSubMerchant(e);
        });
    }



    $("#amount").on("input", function(event){
        var _this = $(this).val();
        var _count = 0;
        if(_this.indexOf(".") != -1){
            for(var i = 0; i < _this.length; i++){
                var _pat = /[.]/;
                if(_this[i].match(_pat)){
                    _count++;
                }
            }
            if(_count > 1){
                $(this).val(_this.slice(0, _this.length-1));
            }
            var _activePos = _this.indexOf(".");
            var _endPos = _this.substring(_activePos);
            if(_endPos.length > 5){
                $(this).val(_this.slice(0, _this.length-1));
            }
        }
    })

    // statusEnquiry
    $("body").on("click", ".statusEnquiry", function(e){
        var _tr = $(this).closest("tr").prev("tr");
        var _txnId = _tr.find(".txnId").text();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getImpsStatus",
            data: {
                "txnId": _txnId
            },
            success: function(data){
                console.log(data);
                if(data.response == "success"){
                    setTimeout(function(e){
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                        $(".lpay_popup").fadeIn();
                        $(".responseMsg").text(data.responseMsg);
                        $("body").addClass("loader--inactive");
                    }, 2000);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }
            }
        })
    })

    // status enquiry payout
    $("body").on("click", ".statusEnquiryPayout", function(e){
        var _tr = $(this).closest("tr");
        var _txnId = _tr.find(".txnId").text();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getPGPayoutStatus",
            data: {
                "txnId": _txnId
            },
            success: function(data){
                console.log(data);
                if(data.response == "success"){
                    setTimeout(function(e){
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                        $(".lpay_popup").fadeIn();
                        $(".responseMsg").text(data.responseMsg);
                        $("body").addClass("loader--inactive");
                    }, 2000);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }
            }
        })
    })

    $(".downloadData").on("click", function(e){
        if($("[data-id=reportMerchant]").val() == ""){
            $("#reportMerchant").val("All");
        }else{
            $("#reportMerchant").val($("[data-id=reportMerchant]").val());
        }
        $("#reportStatus").val($("[data-id=reportStatus]").val());
        $("#reportChannel").val($("[data-id=reportChannel]").val());
        $("#reportDateFrom").val($("[data-id=reportDateFrom]").val());
        $("#reportDateTo").val($("[data-id=reportDateTo]").val());
        $("#reportSubMerchant").val($("#subMerchantReport").val());
        $("#downloadReport").submit();
    })

    // tab creation 
    $(".lpay-nav-link").on("click", function(e){
        var _this = $(this).attr("data-id");
        $(".lpay-nav-item").removeClass("active");
        $(this).closest(".lpay-nav-item").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+_this+"]").removeClass("d-none");
    })

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

    $(".viewData").on("click", function(e){
        $("body").removeClass("loader--inactive");
        handleChange();
        setTimeout(function(e){
            $("body").addClass("loader--inactive");
        }, 1500);
    })
    
    if(_userType != "MERCHANT"){
        handleChange();
    }
                

    function handleChange(){
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            $('#dateFrom').focus();
            return false;
        }
        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            $('#dateFrom').focus();
            return false;
        }

        $("#datatable").dataTable({

            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },

            "destroy": true,
            "ajax": {
                "type": "post",
                "url": "impsTransferReport",
                "data" : function (d){
                        return generatePostData(d);
                    }
                },
                "initComplete" : function(settings, json) {
                    //$("#setSuperMerchant").val(json.flag);
                   // hideColumn();
                },

                "destroy": true,
                "order": [[ 3, 'desc' ]],
                "aoColumns": [
                {
                    "mDataProp": "merchant",
                    "className": "my_class"
                },
    
                {"mData" : "txnId", "className": "txnId"},
                {"mData" : "userType"},
                {"mData" : "orderId", "className": "orderId"}, 
                {"mData" : "date"},	
                {"mData" : "amount" , "className": "amount"},
                {"mData" : "status", "className" : "status"},     
            ]
        });

    }

    $("body").on("click", "#datatable tbody td", function(e){
		var table = new $.fn.dataTable.Api('#datatable');
		if(e.target.localName != "button"){
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
    
    $("body").on("click", "#datatablePayout tbody td", function(e){
		var table = new $.fn.dataTable.Api('#datatablePayout');
        console.log(e.target.localName);
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
				row.child( formatPgInitiated(row.data()) ).show();
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
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined' || _getValue == 'NA'){
				_new[array].classList.add("d-none");
			}
		})
	}

    // variable sent to backend function
    function generatePostData(d) {
        var obj = {
            payId : $("#merchantReportPayId").val(),
            status : $("#status").val(),
            channel : $("#channel").val(),
            subMerchant: $("#subMerchantReport").val(),
            dateFrom : $("#dateFrom").val(),
            dateTo : $("#dateTo").val(),
            draw : d.draw,
            length : d.length,
            start : d.start,
            token : $("[name=token]").val(),
            "struts.token.name" : "token",
        };
        return obj;
    }

    $("#merchant").on("change", function(e){
        
    var _merchant = $(this).val();
    $("#bankAccountName").val("");
    $("#bankAccountNumber").val("");
    $("#bankIfsc").val("");
    if(_merchant != ""){
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getSubMerchantListByPayId",
            data: {"payId": _merchant},
            success: function(data){
                $("[data-id=submerchant]").addClass("d-none");
                $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
                $(".selectpicker").selectpicker("refresh");
                $("#subMerchant").html("");
                if(data.superMerchant == true){
                    var _option = $("#subMerchant").append("<option value=''>Select Sub Merchant</option>");
                    for(var i = 0; i < data.subMerchantList.length; i++){
                        _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
                    }
                    $("[data-id=submerchant]").removeClass("d-none");
                    $("#subMerchant option[value='']").attr("selected", "selected");
                    $("[data-id=subMerchant]").selectpicker();
                    $("[data-id=subMerchant]").selectpicker("refresh");
                    $("[data-id=subMerchant]").attr("id", "merchant");
                    $(".merchant-class").attr("id",  "");
                    $(".merchant-class").addClass("super-merchant");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                }else{
                    getBankDetails();
                    $("[data-id=subMerchant]").attr("id", "subMerchant");
                    $(".merchant-class").attr("id",  "merchant");
                    $(".merchant-class").removeClass("super-merchant");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                    $("[data-id=submerchant]").addClass("d-none");
                    $("[data-id=deliveryStatus]").addClass("d-none");
                    $("[data-id=deliveryStatus]").val("");
                    $("#subMerchant").val("");
                }
            }
        });
    }else{
        $("[data-id=submerchant]").addClass("d-none");
        $("#subMerchant").val("");
        $("[data-id=deliveryStatus]").addClass("d-none");
        $("[data-id=deliveryStatus]").val("");	
    }
})

$("body").on("change", ".super-merchant", function(e){
    var _merchant = $(".super-merchant[data-id=lpay-input]").val();
    $("#bankAccountName").val("");
    $("#bankAccountNumber").val("");
    $("#bankIfsc").val("");
    if(_merchant != ""){
        
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getSubMerchantListByPayId",
            data: {"payId": _merchant},
            success: function(data){
                
                $("[data-id=submerchant]").addClass("d-none");
                $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
                $(".selectpicker").selectpicker("refresh");
                $("#subMerchant").html("");
                if(data.superMerchant == true){
                    var _option = $("#subMerchant").append("<option value=''>Select Sub Merchant</option>");
                    for(var i = 0; i < data.subMerchantList.length; i++){

                        _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")

                    }
                    $("[data-id=submerchant]").removeClass("d-none");
                    $("#subMerchant option[value='']").attr("selected", "selected");
                    $("[data-id=subMerchant]").selectpicker();
                    $("[data-id=subMerchant]").selectpicker("refresh");
                    $("[data-id=subMerchant]").attr("id", "merchant");
                    $(".merchant-class").attr("id",  "");
                    $(".merchant-class").addClass("super-merchant");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                }else{
                    getBankDetails();
                    $("[data-id=subMerchant]").attr("id", "subMerchant");
                    $(".merchant-class").attr("id",  "merchant");
                    $(".merchant-class").removeClass("super-merchant");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                    $("[data-id=submerchant]").addClass("d-none");
                    $("[data-id=deliveryStatus]").addClass("d-none");
                    $("[data-id=deliveryStatus]").val("");
                    $("#subMerchant").val("");
                }
            }
        });
    }else{
        $("[data-id=submerchant]").addClass("d-none");
        $("#subMerchant").val("");
        $("[data-id=deliveryStatus]").addClass("d-none");
        $("[data-id=deliveryStatus]").val("");  
    }
})

$("[data-id=submerchant]").on("change", function(e){
    getBankDetails();
})

$("body").on("change", ".super-merchant", function(e){
    getBankDetails();
})

$(".super-merchant").on("change", function(e){
    
})


$(".viewDataPayout").on("click", function(e){
        $("body").removeClass("loader--inactive");
        handleChangePayout();
        setTimeout(function(e){
            $("body").addClass("loader--inactive");
        }, 1500);
    })


   
    handleChangePayout();

    function handleChangePayout(){
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFromPayout').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateToPayout').val());
        
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            $('#dateFromPayout').focus();
            return false;
        }
        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            $('#dateFromPayout').focus();
            return false;
        }

        $("#datatablePayout").dataTable({

            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },

            "destroy": true,
            "ajax": {
                "type": "post",
                "url": "pgPayOutAction",
                "data" : function (d){
                        return generatePostDataPayout(d);
                    }
                },
                "fnDrawCallback" : function(settings, json) {
                        // $("#setSuperMerchant").val(json.flag);
                        hideColumn();
                    },

                "destroy": true,
                "order": [[ 3, 'desc' ]],
                "aoColumns": [
                {
                    "mData" : null,
                    "className" : "check",
                    "mRender" : function(row){
                        if(row.status == "Pending"){
                            return '<label class="checkbox-label unchecked mb-10"><input class="selectCheckbox" type="checkbox" ></label>'
                        }else{
                            return " "
                        }
                    }
                },
                {
                    "mDataProp": "merchant",
                    "className": "my_class"
                },
               
                {"mData" : "txnId", "className": "txnId"},
                {"mData" : "orderId", "className": "orderId"},
                {"mData" : "date"},	
                {"mData" : "amount" , "className": "amount"},
                {"mData" : "status", "className" : "status"},
                {"mData" : null,
                "mRender" : function(row){
                    if(row.status == "Timeout"){
                        return "<button class='lpay_button lpay_button-md lpay_button-secondary statusEnquiryPayout'>Status</button><button class='lpay_button lpay_button-md lpay_button-secondary reInitiatePayout'>Re-Initiate</button>"
                    }
                    if(row.status == "Pending"){
                        return "<button class='lpay_button lpay_button-md lpay_button-secondary reInitiatePayout'>Initiate</button>"
                    }
                    if(row.status != "Captured"){
                        return "<button class='lpay_button lpay_button-md lpay_button-secondary reInitiatePayout'>Re-Initiate</button>"
                    }else{
                        return "";
                    }
                }
            },
                
                        
            ]
        });

    }

    // variable sent to backend function
    function generatePostDataPayout(d) {
        var payId = document.getElementById("merchantPayoutPayId").value;
        var status = $("#statusPG").val();
        if(payId == '') {
            payId = 'ALL';
        }
        
        if(status == '') {
            status = 'ALL';
        }



        var obj = {
            payId : payId,
            subMerchantPayId : $("#subMerchantPayoutPayId").val(), 
            orderId : $("#orderIdPayout").val(),
            status : status,
            dateFrom : $("#dateFromPayout").val(),
            dateTo : $("#dateToPayout").val(),
            draw : d.draw,
            length : d.length,
            start : d.start,
            token : $("[name=token]").val(),
            "struts.token.name" : "token",
        };
        return obj;
    }


    
    

function getBankDetails(){
    var _this = $("#merchant").val();
    if(_this != ""){
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "post",
            url: "impsFetchMerchantBankAccount",
            data: {
                "payId": _this
            },
            success: function(data){
                $("#bankAccountName").val(data.bankAccountName);
                $("#bankAccountNumber").val(data.bankAccountNumber);
                $("#bankIfsc").val(data.bankIfsc);
                $("#amount").attr("readonly", false);
                $("#submit").attr("disabled", false);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 2000);
            },
            error:function(data){
                $(".lpay_popup-innerbox").attr("data-status", "error")
                $(".lpay_popup").fadeIn();
                $("#submit").attr("disabled", true);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 2000);
            }  
        })
    }
}

   

    $(".confirmButton").on("click", function(e){
        $("body").removeClass("loader--inactive");
        handleChange();
        handleChangePayout();
        $("[data-id=submerchant]").addClass("d-none");
        $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
        $(".blankInput").val("");
        $(".selectpicker").selectpicker("refresh");
        $(".lpay_popup").fadeOut();
        setTimeout(function(e){
            $("body").addClass("loader--inactive");
        }, 2000);
    })

    // reInitiate
    $("body").on("click", ".reInitiate", function(e){
        var _tr = $(this).closest("tr").prev("tr");
        $("body").removeClass("loader--inactive");
        var _data = {
            "txnId" : _tr.find(".txnId").text(),
            "orderId" : _tr.find(".orderid").text(),
            "status" : _tr.find(".status").text()
        }
        $.ajax({
            type :"post",
            url : "impsReinitiateTransaction",
            data: _data,
            success: function(data){
                if(data.response == "success"){
                    handleChange();
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }else{
                    handleChange();
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn(); 
                    $("body").addClass("loader--inactive");
                }
            }
        })
    });

    //Merchant ReInitiate direct
    $("body").on("click", ".merchantReInitiateDirect", function(e){
        var _tr = $(this).closest("tr").prev("tr");
        $("body").removeClass("loader--inactive");
        var _data = {
            "txnId" : _tr.find(".txnId").text(),
            "orderId" : _tr.find(".orderid").text(),
            "status" : _tr.find(".status").text()
        }
        $.ajax({
            type :"post",
            url : "merchantReInitiateDirectTransaction",
            data: _data,
            success: function(data){
                if(data.response == "success"){
                    handleChange();
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }else{
                    handleChange();
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn(); 
                    $("body").addClass("loader--inactive");
                }
            }
        })
    });

    // reInitiate
    $("body").on("click", ".reInitiatePayout", function(e){
        var _tr = $(this).closest("tr");
        $("body").removeClass("loader--inactive");
        var _data = {
            "txnId" : _tr.find(".txnId").text(),
            "orderId" : _tr.find(".orderid").text(),
            "status" : _tr.find(".status").text()
        }
        $.ajax({
            type :"post",
            url : "reInitiateMerchantTransaction",
            data: _data,
            success: function(data){
                if(data.response == "success"){
                    handleChangePayout();
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }else{
                    handleChangePayout();
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn(); 
                    $("body").addClass("loader--inactive");
                }
            }
        })
    })
    

    $("#submit").on("click", function(e){
        var val = false;
        var _subMerchant = $("[data-id=submerchant]");
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#capturedDateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#capturedDateTo').val());
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            $('#capturedDateFrom').focus();
            return false;
        }
        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            $('#capturedDateTo').focus();
            return false;
        }
        if(!_subMerchant.hasClass("d-none")){
            if($("[data-id=subMerchant]").val() == ""){
                alert("Please select sub merchant");
                return false;
            }
        }else{
            
        }
        $("[data-id=lpay-input]").each(function(e){

            var _that = $(this).attr("id");
            var _this = $(this).val();

            if(_this == ""){
                $(this).next().removeClass("d-none");
                val = false;
                return false;
            }else{
                val = true;
            }

        });
        
        if(val == true){

            $("body").removeClass("loader--inactive");
            var obj = {};

           $(".lpay-input").each(function(e){
               var _id = $(this).attr("id");
               var _val = $(this).val();
               if(_id=="merchant"){
                   _id="payId";
               }
               obj[_id] = _val;
           });

           obj.payId = $("#merchant").val();

           $.ajax({

               type: "post",
               url: "impsTransferedDataAction",
               data: obj,

               success: function(data){

                   if(data.response== "success"){
                    setTimeout(function(e){

                        $(".lpay_popup-innerbox").attr("data-status", "success")
                        $(".lpay_popup").fadeIn();
                        $("#phoneNo").val("");
                        $(".responseMsg").text(data.responseMsg);
                        $("body").addClass("loader--inactive");

                    }, 2000);
                   }else{
                       setTimeout(function(e){
                        $("#phoneNo").val("");
                           $(".lpay_popup-innerbox").attr("data-status", "error");
                           $(".responseMsg").text(data.responseMsg);
                           $(".lpay_popup").fadeIn();
                           $("body").addClass("loader--inactive");

                       }, 2000);
                   }

                }
            })
        }
    });

    $(".lpay_input").on("keyup", function(e){
        $(".default_error").addClass("d-none");
    })

})