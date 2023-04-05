
// tab javascript
var _button = document.querySelectorAll(".lpay-nav-link");
_button.forEach(function(e){
    e.addEventListener("click", function(f){
        var _getAttr = f.target.attributes["data-id"].nodeValue;
        var _getAllLink = document.querySelectorAll(".lpay-nav-link");
        var _getAll = document.querySelectorAll(".lpay_tabs-content");
        _getAllLink.forEach(function(c){
            c.closest(".lpay-nav-item").classList.remove("active");
        })
        _getAll.forEach(function(d){
            d.classList.add("d-none");
        })
        this.closest(".lpay-nav-item").classList.add("active");
        document.querySelector("[data-target="+_getAttr+"]").classList.remove("d-none");
    })
});

function hideColumn(_that){
    var _td = $(_that).DataTable();
    console.log(_td);
    var _userType = $("#userType").val();
    if(_userType == "ADMIN" || _userType == "SUBADMIN"){
        _td.columns(0).visible(true);
    } else {
        _td.columns(0).visible(false);
    }
}

var allowAlphaNumericSpecial = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
}

function removeSpaces(string) {
    return string.split(' ').join('');
}

$(document).ready(function(e){

    // supermerchant function
    		function getSubMerchant(_this, _url, _object){
		
			var _merchant = _this.target.value;
			var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
			var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
			if(_merchant != ""){
				document.querySelector("body").classList.remove("loader--inactive");
				var data = new FormData();
				data.append('payId', _merchant);
				data.append('vendorReportFlag', true);
				var _xhr = new XMLHttpRequest();
				_xhr.open('POST', _url, true);
				_xhr.onload = function(){
					if(_xhr.status === 200){
						var obj = JSON.parse(this.responseText);
						console.log(obj);
						var  _option = "";
						if(_object.isSuperMerchant == true){
							if(obj.superMerchant == true){
								document.querySelector("#"+_subMerchantAttr).innerHTML = "";
								_option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
								for(var i = 0; i < obj.subMerchantList.length; i++){
									_option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
								}
								document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
								document.querySelector("#"+_subMerchantAttr+" option[value='ALL']").selected = true;
								$("#"+_subMerchantAttr).selectpicker();
								$("#"+_subMerchantAttr).selectpicker('refresh');
							}else{
								document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
								document.querySelector("#"+_subMerchantAttr).value = "";
							}
						}
						if(_object.subUser == true){
							if(obj.subUserList.length > 0){
								
								_option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
								for(var i = 0; i < obj.subUserList.length; i++){
									_option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["payId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
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
						}else{

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

		document.querySelector("#merchant").addEventListener("change", function(e){
			getSubMerchant(e, "getSubMerchantList", {
				isSuperMerchant : true,
				subUser : true
			});
		});

		document.querySelector("#subMerchant").addEventListener("change", function(e){
			getSubMerchant(e, "vendorTypeSubUserListAction", {
				subUser : true
			});
		})


    // dataepicker
    var _today = new Date();
    $("#date").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        changeMonth : true,
        changeYear : true,
        dateFormat : 'yy-mm-dd',
        selectOtherMonths : false,
        maxDate : new Date()
    });
    $('#date').val($.datepicker.formatDate('yy-mm-dd', _today));

    $("#datePayOut").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        changeMonth : true,
        changeYear : true,
        dateFormat : 'yy-mm-dd',
        selectOtherMonths : false,
        maxDate : new Date()
    });
    $('#datePayOut').val($.datepicker.formatDate('yy-mm-dd', _today));

    // data table
    function transactionData(_obj, url){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#datatable").DataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "ajax" : {
                "url" : url,
                "type" : "POST",
                "data" : _obj,
            },
            "initComplete" : function() {
                hideColumn("#datatable");
                document.querySelector("body").classList.add("loader--inactive");
            },
            "destroy": true,
            "bSort": true,
            "columns" : [
                {"mData" : "merchant"},
                {"mData" : "vendorName"}, 
                {"mData" : "vendorPayId"},
                {"mData" : "txnType"},
                {"mData" : "txnId"}, 
                {"mData" : "pgRefNum"},
                {"mData" : "orderId"},
                {"mData" : "date"},
                {"mData" : "vendorPayoutDate"},
                {"mData" : "paymentMethod"},
                {"mData" : "paymentRegion"},
                {"mData" : "cardHolderType"}, 
                {"mData" : "cardMask"},
                {"mData" : "custName"},
                {"mData" : "custEmail"}, 
                {"mData" : "custMobile"},
                {"mData" : "paymentCycle"},
                {"mData" : "baseAmount"},
                {"mData" : "tdrSurcharge"},
                {"mData" : "gst"}, 
                {"mData" : "totalAmount"},
                {"mData" : "merchantAmount"}
            ]
        });
    }

    // payout table
    function payoutData(_obj, url){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#payoutTable").DataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "ajax" : {
                "url" : url,
                "type" : "POST",
                "data" : _obj,
            },
            "initComplete": function(settings, json) {
                hideColumn("#payoutTable");
                document.querySelector("body").classList.add("loader--inactive");
              },
              "destroy": true,
              "bSort": true,
            "columns" : [
                {"mData" : "merchant"},
                {"mData" : "vendor"}, 
                {"mData" : "paymentCycle"},
                {"mData" : "vendorPayoutDate"},
                {"mData" : "period"},
                {"mData" : "saleAmount"}, 
                {"mData" : "refundAmount"},
                {"mData" : "netPayout"}
            ]
        });
    }

    // Create variable on submit button
    function checkBlankValue(e){
        var _obj = {};
        var _getAllInput = document.querySelectorAll(".active-panel [data-var]");
        var _activeUrl = document.querySelector(".active-panel").getAttribute("data-target");
        _getAllInput.forEach(function(e){
            var _key = e.attributes["data-var"].nodeValue;
            _obj[_key] = e.value;
        })
        console.log(_obj);
        document.querySelector("[data-target]").classList.remove("active-panel");
        if(_obj.merchantPayId == ""){
            alert("Please select merchant");
            return false;
        }
        // if(_obj.vendorPayId == ""){
        //     alert("Please select vendor");
        //     return false;
        // }
        if(_activeUrl == "transactions"){
            transactionData(_obj, "viewVendorPayoutTransaction");
        }
        if(_activeUrl == "payout"){
            payoutData(_obj, "viewVendorPayout");
        }
    }

    document.querySelector("#transactionBtn").addEventListener("click", function(e){
        this.closest(".lpay_tabs-content").classList.add("active-panel");
        checkBlankValue();
    });

    document.querySelector("#payout").addEventListener("click", function(e){
        this.closest(".lpay_tabs-content").classList.add("active-panel");
        checkBlankValue();
    });

    // download functionality
    function downloadFile(_id){
        var _getAllInput = document.querySelectorAll(".active-panel [data-var]");
        var _createInput = "";
        _getAllInput.forEach(function(e){
            var _key = e.attributes["data-var"].nodeValue;
            var _that = e.value;
           document.querySelector(_id).innerHTML = _createInput += "<input type='hidden' name="+_key+" value="+_that+">";
        })
        document.querySelector("[data-target]").classList.remove("active-panel");
    }

    document.querySelector("#downloadTransactionBtn").addEventListener("click", function(e){
        this.closest(".lpay_tabs-content").classList.add("active-panel");
        downloadFile("#downloadTransaction");
        document.querySelector("#downloadTransaction").submit();
    });

    document.querySelector("#downloadPayout").addEventListener("click", function(e){
        this.closest(".lpay_tabs-content").classList.add("active-panel");
        downloadFile("#downloadTransactionPayout");
        document.querySelector("#downloadTransactionPayout").submit();
    })

    
    $("#payoutTable").DataTable({
        dom : 'BTftlpi',
        buttons: ['csv', 'print', 'pdf'],
        language: {
            search: "",
            searchPlaceholder: "Search records"
        },
        "destroy": true,
        "initComplete": function(settings, json) {
            hideColumn("#payoutTable");
            document.querySelector("body").classList.add("loader--inactive");
          },
    });    
   

    // transactionData();
    $("#datatable").DataTable({
        dom : 'BTftlpi',
        buttons: ['csv', 'print', 'pdf'],
        language: {
            search: "",
            searchPlaceholder: "Search records"
        },
        "destroy": true,
        "initComplete" : function() {
            hideColumn("#datatable");
            document.querySelector("body").classList.add("loader--inactive");
        },
    });
})
