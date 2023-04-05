
function removeSpaces(string) {
    return string.split(' ').join('');
}

var allowAlphaNumericSpecial = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
}

function updateFormEnabled() {
    if (verifyAdSettings()) {
        $('#chargebackSubmit').removeAttr("disabled");
    } else {
        $('#chargebackSubmit').attr('disabled', true);
    }
}



function verifyAdSettings() {
    var chargeBackTyp = document.getElementById("chargebackType").value;
    // var chargeBackStats = document.getElementById("chargebackStatus").value; 
    var targtDate = document.getElementById("targetDate").value;
    
    if (chargeBackTyp != '' && targtDate != '') {
        return true;
    } else {
        return false
    }
}

function validPgRefNum() {
    var pgRefValue = document.getElementById("pgRefNum").value;
    var regex = /^[0-9\b]{16}$/;
    if (pgRefValue.trim() != "") {
        if (!regex.test(pgRefValue)) {
            document.getElementById("validValue").style.display = "block";
            document.getElementById("submit").disabled = true;
        } else {
            document.getElementById("submit").disabled = false;
            document.getElementById("validValue").style.display = "none";
        }
    } else {
        document.getElementById("submit").disabled = false;
        document.getElementById("validValue").style.display = "none";
    }
}

function showPopup(){
	document.querySelector(".lp-refund_section").classList.add("lp-show_popup");
}

function removePopup(){
	$(".lp-refund_section").removeClass("lp-show_popup");
}

$(document).ready(function() {
	document.querySelector("#merchant").addEventListener("change", function(e){
		getSubMerchant(e, "getSubMerchantList", {
			isSuperMerchant : true,
			subUser : true,
			retailMerchantFlag: true,
			glocal : true
		});
	});
	document.querySelector("#subMerchant").addEventListener("change", function(e){
		getSubMerchant(e, "vendorTypeSubUserListAction", {
			subUser : true
		});
	})

	if($("#gloc").val() == "true") {
		$("[data-id=deliveryStatus]").removeClass("d-none");
		$("[data-id=deliveryStatus] select").selectpicker();
		$("[data-id=deliveryStatus] select").selectpicker('val', "All");
	}

	// $('[data-id="subMerchant"]').prepend('<option value="ALL" selected="selected">ALL</option>');
	// $('[data-id="subMerchant"]').selectpicker("refresh");

	renderTable();

	$("#submit").click(function(env) {
		
		$("#setSuperMerchant").val('');
		reloadTable();
		// hideColumn();
	});

});

function format ( d ) {
// `d` is the original data object for the row
	d.new = function(){
			var userType = document.querySelector("#userType").value;		
			var chargebackBtn = "";
			if(userType == "ADMIN" || userType == "SUBADMIN") {
				if(d.btnchargebacktext !== "close") {
					chargebackBtn = '<button class="lpay_button lpay_button-md lpay_button-secondary btnChargeBack" style="font-size:10px;" >Chargeback</button>';
				}
			}
			if (userType == "ADMIN"|| userType == "SUBADMIN" || userType == "MERCHANT") {
				if(d.refundBtnText == "Refunded") {
					/* chargebackBtn = '<button class="btn btn-info btn-xs btn-block btnChargeBack" style="font-size:10px;" disabled>Chargeback</button>'; */
					return '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;" disabled>Refunded</button>';
				} else if(d.refundBtnText == "Partial Refund") {
					return chargebackBtn + '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;">Partial Refund</button>';
				} else {
					return chargebackBtn + '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;">Refund</button>';
				}
			} else {
				return "";
			}
	}
	return '<div class="main-div">'+
		'<div class="inner-div">'+
			'<span>Txn Id</span>'+
			'<span>'+d.transactionId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Sub-Merchant</span>'+
			'<span>'+d.subMerchantId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Delivery Status</span>'+
			'<span>'+d.deliveryStatus+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Transaction Mode</span>'+
			'<span>'+d.transactionMode+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Cust Name</span>'+
			'<span>'+d.customerName+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Customer Email</span>'+
			'<span>'+d.customerEmail+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Mask</span>'+
			'<span>'+d.cardNumber+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Cardholder Type</span>'+
			'<span>'+d.cardHolderType+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Txn Type</span>'+
			'<span>'+d.txnType+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Status</span>'+
			'<span>'+d.status+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Base Amount</span>'+
			'<span>'+d.amount+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
		    '<span>TDR / Surcharge</span>'+
		    '<span>'+d.tdr_Surcharge+'</span>'+
	    '</div>'+
	    '<div class="inner-div">'+
	        '<span>GST</span>'+
    	    '<span>'+d.gst_charge+'</span>'+
	    '</div>'+ 
	    '<div class="inner-div">'+
		    '<span>Reseller Charges</span>'+
		    '<span>'+d.resellerCharges+'</span>'+
	    '</div>'+
	    '<div class="inner-div">'+
	        '<span>Reseller GST</span>'+
	        '<span>'+d.resellerGST+'</span>'+
        '</div>'+
		'<div class="inner-div">'+
			'<span>Merchant Amount</span>'+
			'<span>'+d.totalAmtPayable+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Doctor</span>'+
			'<span>'+d.doctor+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Glocal</span>'+
			'<span>'+d.glocal+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Partner</span>'+
			'<span>'+d.partner+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Unique ID</span>'+
			'<span>'+d.uniqueId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Transaction Flag</span>'+
			'<span>'+d.txnSettledType+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Part Settled Flag</span>'+
			'<span>'+d.partSettle+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF11</span>'+
			'<span>'+d.UDF11+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF12</span>'+
			'<span>'+d.UDF12+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF13</span>'+
			'<span>'+d.UDF13+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF14</span>'+
			'<span>'+d.UDF14+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF15</span>'+
			'<span>'+d.UDF15+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF16</span>'+
			'<span>'+d.UDF16+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF17</span>'+
			'<span>'+d.UDF17+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF18</span>'+
			'<span>'+d.UDF18+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Category Code</span>'+
			'<span>'+d.categoryCode+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>SKU Code</span>'+
			'<span>'+d.SKUCode+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Refund Cycle</span>'+
			'<span>'+d.refundCycle+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Product Price</span>'+
			'<span>'+d.productPrice+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Vendor ID</span>'+
			'<span>'+d.vendorID+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
		'<span>SUF GST</span>'+
		'<span>'+d.sufGst+'</span>'+
	'</div>'+
	'<div class="inner-div">'+
		'<span>SUF TDR</span>'+
		'<span>'+d.sufTdr+'</span>'+
	'</div>'+
		'<div class="inner-div">'+
			'<span>Payout Date</span>'+
			'<span>'+d.payOutDate+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UTR No</span>'+
			'<span>'+d.utrNo+'</span>'+
		'</div>'+
		'<div class="inner-div" style="width: 100%;text-align: center">'+
			'<span></span>'+
			'<span>'+d.new()+'</span>'+
		'</div>'+
	'</div>';
	// document.querySelector(".selector")
}
	

function renderTable() {
	var merchantEmailId = document.getElementById("merchant").value;
	var table = new $.fn.dataTable.Api('#txnResultDataTable');

	var transFrom = $.datepicker
			.parseDate('dd-mm-yy', $('#dateFrom').val());
	var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
	if (transFrom == null || transTo == null) {
		alert('Enter date value');
		return false;
	}

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
	var token = document.getElementsByName("token")[0].value;

	$('#txnResultDataTable').dataTable({
		"ajax" : {

			"url" : "saleTransactionSearchAction",
			"type" : "POST",
			"data" : function(d) {
				return generatePostData(d);
			}
		},
		"fnDrawCallback" : function(settings, json) {
			if(settings.json != undefined){
				$("#retailMerchantFlag").val(settings.json.retailMerchantFlag);
			}
			$("body").addClass("loader--inactive");
		},
		"searching" : false,
		"ordering" : false,
		"destroy": true,
		"serverSide" : true,
		"paginationType" : "full_numbers",
		"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
		"order" : [ [ 2, "desc" ] ],


		"columns" : [
		
		{
			"data" : "merchants",
			"className" : "payId text-class"

		},
		{
			"data" : "pgRefNum",
			"className" : "text-class"
		},
		{
			"data" : "orderId",
			"className" : "text-class"
		},
		{
			"data" : "paymentMethods",
			"render" : function(data, type, full) {
				return full['paymentMethods'] + ' '
						+ '-' + ' '
						+ full['mopType'];
			},
			"className" : "text-class"
		},
		{
			"data" : "paymentRegion",
			"className" : "text-class",
			"width" : "10%"
		},
		{
			"data" : "dateFrom",
			"className" : "text-class",
			"width" : "10%"
		},
		{
			"data" : "totalAmount",
			"className" : "text-class"
		},]
	});

}

$(document).ready(function() {
	var _obj = "";
	
	$('body').on('click','#txnResultDataTable .btnRefund',function() {
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		var _btn = $(this).text();
		var _getClosestTr = $(this).closest("tr").prev("tr");
		var _data = table.rows(_getClosestTr).data();
		if(_btn !== "Refunded") {
			$("body").removeClass("loader--inactive");
			$.ajax({
				type: "POST",
				url: "manualRefundProcess",
				data: {
					"payId" : _data[0]['payId'],
					"pgRefNum" : _data[0]['pgRefNum'],
					"refundedAmount" : _data[0]['refundedAmount'],
					"refundAvailable" : _data[0]['refundAvailable'],
					"chargebackAmount" : _data[0]['chargebackAmount'],
					"chargebackStatus" : _data[0]['chargebackStatus']
				},
				success: function(data){
					var _data = data.manualRefundProcess;
					_obj = _data;
					for(key in _data){
						if(_data[key] != "" && _data[key] != "NA" && _data[key] != null){
							if(document.querySelector("[data-refund='"+key+"']") != null){
								document.querySelector("[data-refund='"+key+"']").innerText = _data[key];
								document.querySelector("[data-refund='"+key+"']").closest("tr").classList.remove("d-none");
							}
						}
					}
					document.querySelector("#amount_box").value = _data['refundAvailable'];
					$("body").addClass("loader--inactive");
					document.querySelector(".refund_div").classList.add("lp-show_popup");
				}
			})
		}
	});

	function checkRefundAmount(_this){
		var _value = Number(_this.value);
		var _availableRefund = Number(document.querySelector("[data-refund='refundAvailable']").innerText);
		if(_value > _availableRefund){
			document.querySelector("#refund-submit").disabled = true;
		}else{
			document.querySelector("#refund-submit").disabled = false;
		}
	}

	$("#amount_box").on("input", function(e){
		checkRefundAmount(e.target);
	})



	$("#refund-submit").on("click", function(e){

		var _getRefundAmount = document.querySelector("#amount_box").value;
		if(_getRefundAmount == ""){
			return false;
		}else{
			_obj['refundAmount'] = _getRefundAmount;
			document.querySelector("body").classList.remove("loader--inactive");
			$.ajax({
				type: "POST",
				url: "manualRefundProcessAction",
				data : _obj,
				success : function(data){
					$(".responseMsg").text(data.response);
					if(data.response == "SUCCESS"){
						$(".lpay_popup-innerbox").attr("data-status", "success");
					}else{
						$(".lpay_popup-innerbox").attr("data-status", "error");
					}
					document.querySelector(".lp-refund_section").classList.remove("lp-show_popup");
					document.querySelector("body").classList.add("loader--inactive");
					$(".lpay_popup").fadeIn();
				}
			})
		}

	})

	$("body").on("click", "#txnResultDataTable tbody td", function(e){
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
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

	function getAllData(){
		var _new = document.querySelectorAll(".inner-div");
		_new.forEach(function(index, array, element){
			var _getValue = _new[array].children[1].innerText;
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
				_new[array].classList.add("d-none");
			}
		})
	}

	$(".confirmButton").on("click", function(e){
		reloadTable();
		$(".lp-refund_section").removeClass("lp-show_popup");
		$(".lpay_popup").fadeOut();
	})

	$('body').on('click','#txnResultDataTable .btnChargeBack',function() {

		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		var _getClosestTr = $(this).closest("tr").prev("tr");
		var _data = table.rows(_getClosestTr).data();
		document.querySelector("body").classList.remove("loader--inactive");
		
		$.ajax({
			type: "POST",
			url: "chargebackAction",
			data: {
				"payId": _data[0]['payId'],
				"orderId" : _data[0]['orderId'],
				"refundedAmount" : _data[0]['refundedAmount'],
				"pgRefNum" : _data[0]['pgRefNum'],
				"refundAvailable" : _data[0]['refundAvailable'],
				"txnId" : _data[0]['transactionId']
			},
			success: function(data){
				// console.log(data);
				document.querySelector(".chargeback-input").innerHTML = "";
				for(key in data.transDetails){

					var _checkNull = document.querySelector("[data-chargeback='"+key+"']");
					if(_checkNull != null){
						if(data.transDetails[key] == ""){
							_checkNull.innerText = "Not Applicable";
						}else if(data.transDetails[key] == null){
							_checkNull.closest("tr").classList.add("d-none");
						}else{
							_checkNull.innerText = data.transDetails[key];
						}
					}

					if(_checkNull =! null){
						if(data.transDetails[key] != null){
							var _option = "<input id='"+key+"' type='hidden' name='"+key+"' value='"+data.transDetails[key]+"' />";
							document.querySelector(".chargeback-input").innerHTML += _option;

						}
					}

				}

				document.querySelector("body").classList.add("loader--inactive");
				document.querySelector(".chargeback_div").classList.add("lp-show_popup");

			}
		})

	});

	$("#uploadCase").on("change", function(e){
		// console.log($(this));
		var _value = $(this).val();
		var _name = $(this)[0].files[0].name;
		var _extension = _name.slice(_name.lastIndexOf(".")+1).toLowerCase();
		// console.log(_extension);
		if(_value != ""){
			if(_extension == "csv" || _extension == "pdf"){
				$(this).closest("label").find("span").text(_name);
			}else{
				$(this).closest("label").find("span").text("PDF or CSV");
				alert("Please select valid file format");
			}
		}
		
	})

	$(".checkbox-label input").on("change", function(e){
        if($(this).is(":checked")){
          $(this).closest("label").addClass("checkbox-checked");
        }else{
          $(this).closest("label").removeClass("checkbox-checked");
        }
      });

	$("#chargebackAmount, #otherCharges").on("blur", function(e) {
        var val = $(this).val();
        if(val !== "") {
            val = Number(val);
            $(this).val(val.toFixed(2));
        }
    });

	var validateChargebackAmount = function() {
        
        var refundAvailable = document.getElementById("refundAvailable").value,
            chargebackAmount = document.getElementById("chargebackAmount").value,
            refundAvailableFloat =  parseFloat(refundAvailable),
            chargebackAmountFloat =  parseFloat(chargebackAmount),
            errorBox = $("#error-chargebackAmount");

        if(chargebackAmount == "") {
            errorBox.text("Chargeback amount cannot be blank.");
            errorBox.removeClass("d-none");
            return false;
        } else if(chargebackAmountFloat > refundAvailableFloat) {
            errorBox.text("Chargeback amount cannot be greater than available amount.");
            errorBox.removeClass("d-none");
            return false;
        } else if(chargebackAmount <= 0) {
            errorBox.text("Chargeback amount cannot be less than or equals to zero.");
            errorBox.removeClass("d-none");
            return false;
        } else {
            errorBox.text("");
            errorBox.addClass("d-none");
            return true;
        }
    }

	$("#comments").on("keyup", function(e) {
        var _val = $(this).val();
        
        if(_val == "") {
            $("#error-commentId").removeClass("invisible");
        } else {
            $("#error-commentId").addClass("invisible");
        }
    });

	$("#comments").on("change", function(e){
		var _val = $(this).val();
		if(_val == ""){
			$("#error-commentId").addClass("invisible");
		}
	})

	$("form#files").submit(function(e) {
        e.preventDefault();
        
        var validateChargeback = validateChargebackAmount();
        var _comment = document.getElementById("comments").value;
        
        if(!validateChargeback) {
            return false;
        } else if(_comment == "") {
            document.getElementById("error-commentId").classList.remove("invisible");
            return false;
        } else {
            var formData = new FormData($(this)[0]);

			$("body").removeClass("loader--inactive");

			$("#targetDate").val("");
			$("#chargebackType").val('default');
            $("#chargebackType").selectpicker('refresh');
			$("#comments").val("");
			$("#uploadCase").val("");
			$("#uploadCase").closest("label").find("span").text("PDF or CSV");
			$("#chargebackAmount").val("");
			$("#holdAmountFlag").prop("checked", false);
			$("#holdAmountFlag").closest("label").removeClass("checkbox-checked");
			$("#error-chargebackAmount").addClass("d-none");
			$("#error-commentId").removeClass("invisible");
			

			$.ajax({
				url:'saveChargebackAction',
				type: 'POST',
				timeout: 0,
				data: formData,
				async: true,
				success: function (data) { 
					$(".responseMsg").text("Successfull Created.");
					$(".lpay_popup-innerbox").attr("data-status", "success");
					$(".lpay_popup").fadeIn();
					$("body").addClass("loader--inactive");
				},
				error: function (data) {
					$(".responseMsg").text("Unable to save chargeback.");
					$(".lpay_popup-innerbox").attr("data-status", "error");
					$(".lpay_popup").fadeIn();
					$("body").addClass("loader--inactive");
				},
				cache: false,
				contentType: false,
				processData: false
			});
			return true;
        }
    });

	var dateToday = new Date(); 
    $("#targetDate").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : true,
        minDate: dateToday,			
    });  

    $("#chargebackAmount").on("blur", function() {
        validateChargebackAmount();
    });

	var decimalCount = function(number) {
        // Convert to String
        var numberAsString = number.toString();
        // String Contains Decimal
        if (numberAsString.includes('.')) {
            return numberAsString.split('.')[1].length;
        }
        // String Does Not Contain Decimal
        return 0;
    }
    
    $("#chargebackAmount, #otherCharges").on("keyup", function(e) {
        var val = $(this).val();
        if(isNaN(val)){
            val = val.replace(/[^0-9\.]/g,'');
            if(val.split('.').length>2) 
                val = val.replace(/\.+$/,"");
        }

        var countDecimal = decimalCount(val);

        if(countDecimal > 2) {
            val = Number(val);
            var enteredVal = "0.00" + e.key;
            enteredVal = Number(enteredVal);                        
            if(enteredVal >= 0.000) {
                val = val - enteredVal;
            }

            $(this).val(val.toFixed(2));
        } else {
            $(this).val(val);
        }
    });

});

function reloadTable() {
	var datepick = $.datepicker;
	var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
	var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());

	if (transFrom == null || transTo == null) {
		alert('Enter date value');
		return false;
	}

	var _pgRef = document.querySelector("#pgRefNum").value;
	if(_pgRef != ""){
		var letters = /^[0-9]+$/;
		var _match = letters.test(_pgRef);
		if(_match == true){
			
		}else{
			alert("Please enter valid PG REF Number")
			return false;
		}
		
	}

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

	$("body").removeClass("loader--inactive");
	
	var tableObj = $('#txnResultDataTable');
	var table = tableObj.DataTable();
	table.ajax.reload();
}
	
function generatePostData(d) {

	var obj = {};

	// console.log($("#transactionFlag").val().toString());

	var _getAllInput = document.querySelectorAll("[data-var]");
	_getAllInput.forEach(function(index, element, array){
	var _new =  _getAllInput[element].closest(".col-md-3").classList;
	var _newVal = _new.toString().indexOf("d-none");
	if(_newVal == -1){
		obj[_getAllInput[element].name] = _getAllInput[element].value
	}
	})

	obj.token = document.getElementsByName("token")[0].value;
	obj.draw = d.draw;
	obj.length = d.length;
	obj.start = d.start;
	obj["struts.token.name"] = "token";

	if(obj.merchantEmailId == ""){
		obj.merchantEmailId = "ALL"
	}

	if(obj.paymentType == ""){
		obj.paymentType = "ALL"
	}

	if(obj.currency == ""){
		obj.currency = "ALL";
	}

	if(obj.transactionFlag == ""){
		obj.transactionFlag = "ALL"
	}else{
		obj.transactionFlag = $("#transactionFlag").val().toString();
	}

	// console.log(obj);

	return obj;
}

// download function

$(document).ready(function(e){
	document.querySelector("#downloadButton").onclick = createDownloadForm;
	function createDownloadForm(e){
		var _checkButton = e.target.value;
		var _get = document.querySelectorAll("[data-download]");
		var _input = "";
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transTo - transFrom > 61 * 86400000) {
			alert('No. of days can not be more than 60 days');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		
		_get.forEach(function(index, array, element){
			var _dNone = index.closest(".d-none");
			if(_dNone == null){
				var _value = index.value;
				if(_value == "" && index.id == "merchant"){
					_value = "ALL"
				}
				_input += "<input type='hidden' name='"+index.getAttribute("data-download")+"' value='"+_value+"' />"
			}
		})
		if(_checkButton == "Download"){
			_input += "<input type='hidden' name='paymentsRegion' value='ALL' />";
			_input += "<input type='hidden' name='transactionType' value='SALE' />";
			_input += "<input type='hidden' name='reportType' value='saleCaptured' />";
			document.querySelector("#downloadTransactionsReportAction").innerHTML = _input;
			document.querySelector("#downloadTransactionsReportAction").submit();	
		}else{
			document.querySelector("body").classList.remove("loader--inactive");
			var _obj = {};
			_get.forEach(function(ind, arr, ele){
				_obj[ind.getAttribute("data-download")] = ind.value;
			})
			if(_obj['merchantPayId'] == ""){
				_obj['merchantPayId'] = 'ALL'
			}
			_obj['reportType'] = "saleCaptured";
			$.ajax({
				type: "POST",
				url: "generateTxnReportFileAction",
				data: _obj,
				success: function(data){
					setTimeout(function(e){
						if(data.generateReport == true){
							document.querySelector(".lp-success_generate").closest(".col-md-12").classList.remove("d-none");
						}else{
							document.querySelector(".lp-error_generate").closest(".col-md-12").classList.remove("d-none");
						}
						document.querySelector("body").classList.add("loader--inactive");
					}, 500)
					setTimeout(function(e){
						removeError();
					}, 4000);
				}
			})
		}
	}

	function removeError(){
		document.querySelector(".lp-error_generate").closest(".col-md-12").classList.add("d-none");
		document.querySelector(".lp-success_generate").closest(".col-md-12").classList.add("d-none");
	}
})

function dateBaseDownload(){
	var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
	var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
	if (transTo - transFrom > 30 * 86400000) {
		if(checkBlankPgRefOrderId(['#pgRefNum', '#orderId'])){
			document.querySelector("#downloadButton").value = "Generate";
		}else{
			document.querySelector("#downloadButton").value = "Download";
		}
	}else{
		document.querySelector("#downloadButton").value = "Download";
	}

}

function checkBlankPgRefOrderId(_selector){
	var _val = _selector;
	var _checkBoolean = true;
	for(var i = 0; i < _val.length; i++){
		var _isValue = document.querySelector(_val[i]).value;
		if(_isValue != ""){
			_checkBoolean = false;
			break;
		} 
	}
	return _checkBoolean;
}