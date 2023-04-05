var _id = document.getElementById.bind(document);
var userType = _id("userType").value;
if(userType == "ADMIN" || userType == "SUBADMIN" || userType == "SUPERADMIN") {
	_id("btnSave").disabled = true;
}

function removeSpace(e){
	if(e.keyCode == 32){
		e.preventDefault();
		return false;
	}
}

$(window).on("load", function() {
	var _today = new Date();
	if($("#expiresDay").val() == "") {
		var _getDate = _today.getDate();
		var _getMonth = _today.getMonth() + 1;
		var _getYear = _today.getFullYear();
	
		if(_getDate < 10) {
			_getDate = '0' + _getDate;
		}
	
		if(_getMonth < 10) {
			_getMonth = '0' + _getMonth;
		}
	
		var _fullDate = _getDate + '-' + _getMonth + '-' + _getYear + ' 23:59';
		$("#expiresDay").val(_fullDate);
	}

	$('#expiresDay').datetimepicker({
		format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
		showClose: true,
		minDate: _today,
		
		ignoreReadonly: true
	});

	var today = new Date();
	$(".date-input").each(function() {
        var dateInputId = $(this).attr("id");

        $("#" + dateInputId).datepicker({
            prevText : "click for previous months",
            nextText : "click for next months",
            showOtherMonths : true,
            changeMonth : true,
            changeYear : true,
            dateFormat : 'dd-mm-yy',
            selectOtherMonths : false,
            maxDate : new Date()
        });
        
        $("#" + dateInputId).val($.datepicker.formatDate('dd-mm-yy', today));
    });

	var splitDate = function(_date) {
		var $date = _date.split("-");
		$date = new Date(Number($date[2]), Number($date[1]) - 1, Number($date[0]));
	
		return $date.getTime();
	}

	$("#durationTo, #durationFrom").on("change", function(e){
		var _durationFrom = $("#durationFrom").val();
		var _durationTo = $("#durationTo").val();

		if(_durationFrom !== "" && _durationTo !== "") {
			if(splitDate(_durationFrom) > splitDate(_durationTo)) {
				error_snackbar.innerHTML = "Duration to date should be greater than duration from";
				showSnackbar("error-snackbar");
				$(this).val("");
				return false;
			}
		}
	});


	

	// COUNTRY DEFAULT SELECTED
	populateCountries("country", "state");
	var dataCountry = $("#dataCountry").val();	
	if(dataCountry !== "") {
		$("#country").val(dataCountry);
	}

	

	// STATE DEFAULT SELECTED
	populateStates("country", "state", true);
	var dataState = $("#dataState").val();
	if(dataState !== "") {
		$("#state").val(dataState);
	}

	$("#country").selectpicker();
	$("#state").selectpicker('refresh');

	$("#country").on("change", function(e){
		$("#state").next("div").removeClass("disabled");
		// populateStates("country", "state", true);
		$("#state").selectpicker('refresh');

	})
	// QUANTITY
	var _quantity = $("#quantity").val();
	if(_quantity == "") {
		$("#quantity").val(1);
	}
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
			var currenyMapDropDown = _id("mappedCurrency");
			currenyMapDropDown.classList.remove("d-none");
			// _id("currencyCode").classList.add("d-none");
			$("#currencyCode").closest("#wwgrp_currencyCode").addClass("d-none");
			$("#wwgrp_mappedCurrency").css("display", "block");

			var test = "";
			var parseResponse = '<select>';
			for (index in dataValue) {
				var key = dataValue[index];
				parseResponse += "<option value = "+index+">" + key + "</option> ";
			
			}
			parseResponse += '</select>';
			test += key;
			currenyMapDropDown.innerHTML = parseResponse;
			
			$("#mappedCurrency").selectpicker("refresh");
		},
		error : function(data) {
			alert("Something went wrong, so please try again.");
		}
	});
}

function sum() {
	var txtFirstNumberValue = _id('amount').value;
	if(txtFirstNumberValue == "") {
		txtFirstNumberValue = "0.00";
	}
	var txtSecondNumberValue = _id('serviceCharge').value;
	if(txtSecondNumberValue == "" || txtSecondNumberValue == ".") {
		txtSecondNumberValue = "0.00";
	}
	var txtQuantity = _id('quantity').value;
	var result =  parseInt(txtQuantity)* parseFloat(txtFirstNumberValue);
	if (!isNaN(result)) {
		_id('totalAmount').value =(result + parseFloat(txtSecondNumberValue)).toFixed(2);
	}
}

var _count = 0;
function createUDF(){
	if (_count < 8){
		if(_count == 7){
			document.querySelector("#addUdf").classList.add("d-none");
		}
		var _createDiv = document.createElement("div");
		var _createAttr = document.createAttribute("class");
		_count++;
		_createAttr.value = "col-md-3 mb-20 UDF"+_count;
		_createDiv.setAttributeNode(_createAttr);	
		document.querySelector("#UDF_div").append(_createDiv);
		var _createInner = "<div class='lpay_input_group'><input name='UDF1"+_count+"' placeholder='UDF 1"+_count+"' class='UDF"+_count+" lpay_input textFL_merch UDF_input' /></div>";
		document.querySelector(".UDF"+_count).innerHTML = _createInner;
	}
}

document.querySelector("#addUdf").onclick = function(){
	createUDF();
}

$(document).ready(function() {
	// Initialize select
	$('#btnReset').addClass('d-none');	

	var payId=$("#merchantPayId").val();
    if(payId!=""){
        changeCurrencyMap(payId);
    }

	if(window.location.pathname.substr(9,window.location.pathname.length) == "saveInvoice"){
		_id('invoiceLink').style.display="block";
		_id('copyBtn').style.display="block";
		_id('btnSave').style.display="none";
	}

	_id("copyBtn").disabled = !document.queryCommandSupported('copy');
	_id("copyBtn").addEventListener("click", function(event){
		var copiedLink = _id('invoiceLink');
		copiedLink.select();
		document.execCommand('copy');
	});	
	
	$('#btnSave').click(function(event) {
		// event.preventDefault();
		if(FieldValidator.valdAllFields(true)) {
			
		};
	});

	$('#serviceCharge').on('keyup', function() {
		if (this.value[0] === '.') {
			this.value = '0' + this.value;
		}
	});

	$('#amount').on('keyup', function() {
		if (this.value[0] === '.') {
			this.value = '0' + this.value;
		}
	});

	function getSubMerchant(){
			var _merchant = $("#merchantPayId").val();
			if(_merchant != ""){
				$("body").removeClass("loader--inactive");
				$.ajax({
					type: "POST",
					url: "getSubMerchantListByPayId",
					data: {"payId": _merchant},
					success: function(data){
						console.log(data);
						$("#subMerchant").html("");
						if(data.superMerchant == true){
							$("#btnSave").attr("disabled", true);
							$("#isSuperMerchant").val("Y");
							var _option = $("#subMerchant").append("<option value=''>Select Sub Merchant</option>");
							for(var i = 0; i < data.subMerchantList.length; i++){
								_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
							}
							$("[data-id=submerchant]").removeClass("d-none");
							$("#subMerchant option[value='']").attr("selected", "selected");
							$("#subMerchant").selectpicker();
							$("#subMerchant").selectpicker("refresh");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
						}else{
							$("#btnSave").attr("disabled", false);
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-id=submerchant]").addClass("d-none");
							$("#subMerchant").val("");
						}
					}
				});
			}else{
				$("#isSuperMerchant").val("N");
				$("[data-id=submerchant]").addClass("d-none");
				$("#subMerchant").val("");	
			}
	}

	$("#merchantPayId").on('change', function() {
		var _val = $(this).val();
		getSubMerchant();
		if(_val !== "") {
			$("#btnSave").attr('disabled', false);
			changeCurrencyMap(_val);
		} else {
			$("#btnSave").attr('disabled', true);
			$("#currencyCode").removeClass("d-none");
			$("#mappedCurrency").addClass("d-none");
		}
		// $('#spanMerchant').hide();
		// $('#currencyCodeloc').hide();
	});

	$("#subMerchant").on("change", function(e){
		var _val = $(this).val();
		if(_val !== ""){
			$("#btnSave").attr('disabled', false);
		}else{
			$("#btnSave").attr('disabled', true);
		}
	})

	$('#amount,#serviceCharge').on('input', function() {
		this.value = this.value
		.replace(/[^\d.]/g, '')             // numbers and decimals only
		.replace(/(\..*)\./g, '$1')         // decimal can't exist more than once
		.replace(/(\.[\d]{2})./g, '$1');    // not more than 4 digits after decimal
	});

	$('#merchant option').removeAttr('selected');
	$("#merchant option[value='11']").attr('selected', true);
});

	
function isNumberKeyWithDecimal(txt,evt) {
	var charCode = (evt.which) ? evt.which : event.keyCode
	if (charCode == 46) {
			//Check if the text already contains the . character
		if (txt.value.indexOf('.') === -1) {
		return true;
		} else {
		return false;
		}
	} else {
		if (charCode > 31 &&
		(charCode < 48 || charCode > 57))
		return false;
	}
	return true;
}

function isOnlyNumberKey(evt) {
	var charCode = (evt.which) ? evt.which : event.keyCode;
	if (charCode > 31 && (charCode < 48 || charCode > 57))
		return false;
	return true;
}
// $(function(){
// 	$("#expiresDay").val("0");
// });
// $(function(){
// 	$("#expiresHour").val("0");
// });

var specialKeys = new Array();
specialKeys.push(8); //Backspace
specialKeys.push(9); //Tab
specialKeys.push(46); //Delete
specialKeys.push(36); //Home
specialKeys.push(35); //End
specialKeys.push(37); //Left
specialKeys.push(39); //Right
function IsAlphaNumeric(e) {
	var keyCode = e.keyCode == 0 ? e.charCode : e.keyCode;
	var ret = ((keyCode >= 48 && keyCode <= 57) || (keyCode >= 65 && keyCode <= 90) || (keyCode >= 97 && keyCode <= 122) || (specialKeys.indexOf(e.keyCode) != -1 && e.charCode != e.keyCode));
	return ret;
}

function lettersOnly(e, t) {
	try {
		if (window.event) {
			var charCode = window.event.keyCode;
		}
		else if (e) {
			var charCode = e.which;
		}
		else { return true; }
		if ((charCode > 64 && charCode < 91) || (charCode > 96 && charCode < 123) || charCode == 8 || charCode == 32)
			return true;
		else
			return false;
	}
	catch (err) {
		alert(err.Description);
	}
}

function lettersNumbersTwoSpecialkey(e, t) {
	try {
		if (window.event) {
			var charCode = window.event.keyCode;
		}
		else if (e) {
			var charCode = e.which;
		}
		else { return true; }
		if ((charCode > 64 && charCode < 91) ||(charCode > 47 && charCode < 58)|| (charCode > 96 && charCode < 123) || charCode == 8 || charCode == 47 || charCode == 45)
			return true;
		else
			return false;
	}
	catch (err) {
		alert(err.Description);
	}
}

function forEmailSpecialKeyAndAlphaNumeric(e, t) {
	try {
		if (window.event) {
			var charCode = window.event.keyCode;
		}
		else if (e) {
			var charCode = e.which;
		}
		else { return true; }
		if ((charCode > 64 && charCode < 91) ||(charCode > 47 && charCode < 58)|| (charCode > 96 && charCode < 123) || charCode == 8 || charCode == 38 || charCode == 64 || charCode == 95 || charCode == 46)
			return true;
		else
			return false;
	}
	catch (err) {
		alert(err.Description);
	}
}

function productNameAndDescription(e, t) {
	try {
		if (window.event) {
			var charCode = window.event.keyCode;
		}
		else if (e) {
			var charCode = e.which;
		}
		else { return true; }
		if ((charCode > 64 && charCode < 91) ||(charCode > 44 && charCode < 58)|| (charCode > 96 && charCode < 123) || charCode == 8 || charCode == 32)
			return true;
		else
			return false;
	}
	catch (err) {
		alert(err.Description);
	}
}

function lettersNumbersOnly(e, t) {
	try {
		if (window.event) {
			var charCode = window.event.keyCode;
		} else if (e) {
			var charCode = e.which;
		} else {
			return true;
		}
		
		if ((charCode > 64 && charCode < 91) ||(charCode > 47 && charCode < 58)|| (charCode > 96 && charCode < 123) || charCode == 8 || charCode == 32)
			return true;
		else
			return false;
	}
	catch (err) {
		alert(err.Description);
	}
}