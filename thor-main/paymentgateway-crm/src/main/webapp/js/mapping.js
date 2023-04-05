// GLOBAL VARIABLES
var _id = document.getElementById.bind(document),
	_querySelector = document.querySelector.bind(document),
	_querySelectorAll = document.querySelectorAll.bind(document);

function getMapping() {
	var merchantId = _id("merchants").value;
	var acquirer = _id("acquirer").value;

	if (merchantId == "" && acquirer == "") {
		return false;
	}

	if (acquirer == "" || merchantId == "") {
		_id("err").style.display = "block";
		return false;
	}

	if (merchantId != "" && acquirer != "") {
		var token = document.getElementsByName("token")[0].value;
		$.ajax({
			type : "POST",
			url : "mopSetUpDisplay",
			data : {
				merchantEmailId : merchantId,
				acquirer : acquirer,
				token : token,
				"struts.token.name" : "token"
			},
			success : function(data) {
				refresh();
				var mainDiv = _id('id+checkBoxes');
				mainDiv.style.display = "block";
				_querySelector(".mop-setup-box").classList.remove("d-none");
				var map = data.mappedString;
				
				var accountCurrencyArray = data.currencyString;
				if (map == null) {
					var mainDiv = _id('id+checkBoxes');
					mainDiv.style.display = "none";
					_querySelector(".mop-setup-box").classList.add("d-none");
					$("#check123").hide();
					if (data.response != null) {
						_id("btnsubmit").style.display="none";
						alert(data.response);
						_querySelector("body").classList.add("loader--inactive");
						return false;
					} else {
						alert("Network error please try again later!!");
						_querySelector("body").classList.add("loader--inactive");
						return false;
					}
				}
				
				if(map.charAt(0) == ","){
					map = map.substr(1);
				}
				
				else if (map == "") {
					_querySelector("body").classList.add("loader--inactive");
					//return false;
				}
				for (j = 0; j < accountCurrencyArray.length; j++) {
					selectCurrency(accountCurrencyArray[j]);
				}
				var tokens = map.split(",");
				for (i = 0; i < tokens.length; i++) {
					var token = tokens[i];
					selectCheckBoxes(token);
				}
			}
		});
	}
}

function selectCurrency(accountCurrencyObj) {
	var currencyCode = accountCurrencyObj.currencyCode;
	var currencyCheckBoxId = "";
	currencyCheckBoxId = currencyCheckBoxId.concat("id+", currencyCode);
	var fieldsDivId = ("boxdiv" + currencyCheckBoxId);
	var currencyCheckbox = _id(currencyCheckBoxId);
	
	if (currencyCheckbox == null) {
		return;
	}
	var fieldsDiv = _id(fieldsDivId);
	
	var passwordTextBox = _id("idpassword+".concat(currencyCode));
	var txnKeyTextBox = _id("idtxnkey+".concat(currencyCode));
	var merchantIdTextBox = _id("idmerchantid+".concat(currencyCode));
	
	var adf1IdTextBox = _id("idadf1+".concat(currencyCode));
	var adf2IdTextBox = _id("idadf2+".concat(currencyCode));
	var adf3IdTextBox = _id("idadf3+".concat(currencyCode));
	var adf4IdTextBox = _id("idadf4+".concat(currencyCode));
	var adf5IdTextBox = _id("idadf5+".concat(currencyCode));
	var adf6IdTextBox = _id("idadf6+".concat(currencyCode));
	var adf7IdTextBox = _id("idadf7+".concat(currencyCode));
	var adf8IdTextBox = _id("idadf8+".concat(currencyCode));
	var adf9IdTextBox = _id("idadf9+".concat(currencyCode));
	var adf10IdTextBox = _id("idadf10+".concat(currencyCode));
	var adf11IdTextBox = _id("idadf11+".concat(currencyCode));
	
	
	var threeDFlag = _id("id3dflag+".concat(currencyCode));

	passwordTextBox.value = accountCurrencyObj.password;
	txnKeyTextBox.value = accountCurrencyObj.txnKey;
	merchantIdTextBox.value = accountCurrencyObj.merchantId;
	
	adf1IdTextBox.value = accountCurrencyObj.adf1;
	adf2IdTextBox.value = accountCurrencyObj.adf2;
	adf3IdTextBox.value = accountCurrencyObj.adf3;
	adf4IdTextBox.value = accountCurrencyObj.adf4;
	adf5IdTextBox.value = accountCurrencyObj.adf5;
	adf6IdTextBox.value = accountCurrencyObj.adf6;
	adf7IdTextBox.value = accountCurrencyObj.adf7;
	adf8IdTextBox.value = accountCurrencyObj.adf8;
	adf9IdTextBox.value = accountCurrencyObj.adf9;
	adf10IdTextBox.value = accountCurrencyObj.adf10;
	adf11IdTextBox.value = accountCurrencyObj.adf11;
	
	currencyCheckbox.checked = true;
	currencyCheckbox.closest(".checkbox-label").classList.add("checkbox-checked");

	if(accountCurrencyObj.directTxn) {
		threeDFlag.checked = true;
		threeDFlag.closest(".checkbox-label").classList.add("checkbox-checked");
	}

	fieldsDiv.style.display = "block";
}

function selectCheckBoxes(token) {
	var splittedToken = token.split("-");

	var masterCheckBoxId = "";
	masterCheckBoxId = masterCheckBoxId.concat(splittedToken[0], "box")

	var masterCheckbox = _id(masterCheckBoxId);
	masterCheckbox.checked = true;

	masterCheckbox.closest(".checkbox-label").classList.add("checkbox-checked");

	var masterDiv = _id(splittedToken[0]);
	masterDiv.style.display = "block";

	if (splittedToken.length == 3) {
		var mopDivId = "";
		mopDivId = mopDivId.concat(splittedToken[0], "-", splittedToken[1]);
		var mopDiv = _id(mopDivId);
		mopDiv.style.display = "block";

		var mopCheckBoxId = "id+";
		mopCheckBoxId = mopCheckBoxId.concat(mopDivId);
		var mopCheckBox = _id(mopCheckBoxId);
		mopCheckBox.checked = true;

		mopCheckBox.closest(".checkbox-label").classList.add("checkbox-checked");
	}

	var txnCheckbox = _id(token);
	if (null != txnCheckbox) {
		txnCheckbox.checked = true;

		txnCheckbox.closest(".checkbox-label").classList.add("checkbox-checked");
	} else {
		alert("Conflict with mapping token fetched and present checkboxes");
		
		_querySelector("body").classList.add("loader--inactive");
		return false;
	}

}

// to Hide all the checkboxes and disable if no mapping present
function refresh() {
	var creditCard = _id('Credit Cardbox');
	if (creditCard != null) {
		creditCard.checked = false;
		creditCard.closest(".checkbox-label").classList.remove("checkbox-checked");
		deselectAllCheckboxesWithinDiv('Credit Card');
		hideAllCheckboxesWithinDiv('Credit Card');
	}

	var debitCard = _id('Debit Cardbox');
	if (debitCard != null) {
		debitCard.checked = false;
		debitCard.closest(".checkbox-label").classList.remove("checkbox-checked");
		deselectAllCheckboxesWithinDiv('Debit Card');
		hideAllCheckboxesWithinDiv('Debit Card');
	}

	var netBanking = _id('Net Bankingbox');
	if (netBanking != null) {
		netBanking.checked = false;
		netBanking.closest(".checkbox-label").classList.remove("checkbox-checked");
		deselectAllCheckboxesWithinDiv('Net Banking');
		hideAllCheckboxesWithinDiv('Net Banking');
	}

	var wallet = _id('Walletbox');
	if (wallet != null) {
		wallet.checked = false;
		wallet.closest(".checkbox-label").classList.remove("checkbox-checked");
		deselectAllCheckboxesWithinDiv('Wallet');
		hideAllCheckboxesWithinDiv('Wallet');
	}
	
		
	var upi = _id('UPIbox');
	if (upi != null) {
		upi.checked = false;
		upi.closest(".checkbox-label").classList.remove("checkbox-checked");
		deselectAllCheckboxesWithinDiv('UPI');
		hideAllCheckboxesWithinDiv('UPI');
	}

	var mainDiv = _id('id+checkBoxes');
	mainDiv.style.display = "none";
	_querySelector(".mop-setup-box").classList.add("d-none");
}

function hideAllCheckboxesWithinDiv(eleId) {
	var ele = _id(eleId);
	ele.style.display = "none";
}

function deselectAllCheckboxesWithinDiv(eleId) {
	var collection = _id(eleId).getElementsByTagName('INPUT');

	for (var x = 0; x < collection.length; x++) {
		if (collection[x].type.toUpperCase() == 'CHECKBOX') {
			collection[x].checked = false;
			collection[x].closest(".checkbox-label").classList.remove("checkbox-checked");
		}
	}
}

function selectAllCheckboxesWithinDiv(eleId) {
	var checkbox = _id('id+selectAllButton').checked;
	if (checkbox) {
		var collection = _id(eleId).getElementsByTagName('INPUT');

		for (var x = 0; x < collection.length; x++) {
			if (collection[x].type.toUpperCase() == 'CHECKBOX') {
				collection[x].checked = true;
				collection[x].closest(".checkbox-label").classList.add("checkbox-checked");
			}
		}
	} else {
		deselectAllCheckboxesWithinDiv(eleId);
	}
}




// =======================================================================


function showMe(paymentType, that) {
	if(that.checked) {
		if(paymentType == "Net Banking") {
			_id(paymentType).style.display = "flex";
		} else {
			_id(paymentType).style.display = "block";
		}
	} else {
		_id(paymentType).style.display = "none";
	}

	deselectAllCheckboxesWithinDiv(paymentType);
}

function hidefields(currencyBox) {
	var _currencyboxes = document.getElementsByName("currency");
	var _currencyCheckCounter = 0;

	if(currencyBox.checked) {
		_id('boxdiv' + currencyBox.id).style.display = "block";
	} else {
		_id('boxdiv' + currencyBox.id).style.display = "none";
	}

	for (_currencyIndex = 0; _currencyIndex < _currencyboxes.length; _currencyIndex++) {
		_currencyCheckbox = _currencyboxes[_currencyIndex];
		if (_currencyCheckbox.checked) {
			_currencyCheckCounter++;
		}

		if(_currencyIndex == _currencyboxes.length - 1 && !_currencyCheckCounter > 0) {
			_id("err2").classList.remove("d-none");
			return false;
		} else {
			_id("err2").classList.add("d-none");
		}
	}
}

$(document).ready(function() {
	// MERCHANT SELECT BOX
	$("#merchants").on("change", function(e) {
		var _val = $(this).val(),
			_currentId = $(this).attr("id");

		$(".mop-setup-box").addClass("d-none");
		$("#paymentCheck").css("display", "none");
		$("#merchants").css("border", "1px solid #ccc");
		$("#acquirer").selectpicker("val", "");
		
		if(_val == "") {			
			$('[error-id="'+ _currentId +'"]').addClass("show");
		} else {			
			$('[error-id="'+ _currentId +'"]').removeClass("show");
		}
		
		$('#acquirer').selectpicker('refresh');

		refresh();
	});

	$("#acquirer").on("change", function() {
		var _acquirer = $(this).val(),
			_merchant = $("#merchants").val();

		if(_merchant == "" && _acquirer == "") {
			$('[error-id="merchants"]').addClass("show");
			$('[error-id="acquirer"]').addClass("show");
		} else if(_merchant == "" && _acquirer !== "") {
			$('[error-id="merchants"]').addClass("show");
			$('[error-id="acquirer"]').removeClass("show");
		} else if(_merchant !== "" && _acquirer == "") {
			$('[error-id="merchants"]').removeClass("show");
			$('[error-id="acquirer"]').addClass("show");
			_querySelector(".mop-setup-box").classList.add("d-none");
		} else if(_merchant !== "" && _acquirer !== "") {
			$('[error-id="merchants"]').removeClass("show");
			$('[error-id="acquirer"]').removeClass("show");
			$("body").removeClass("loader--inactive");
			$("#mopSetupForm").submit();
		}
	});

	$(".checkbox-label input").on("change", function(e) {
		var _label = $(this).closest("label");

		if($(this).is(":checked")){
			_label.addClass("checkbox-checked");
		}else{
			_label.removeClass("checkbox-checked");
		}
	});			

	var merchantValue = _id("merchants").value;
	var acquirerValue = _id("acquirer").value;
	var userValue = _id("user").value;
	if(merchantValue == '' && acquirerValue == '' && userValue == '1') {
		_id("btnsubmit").style.display="none";
	} else {
		_id("btnsubmit").style.display="inline-block";
	}

	getMapping();
	
	var mainDiv = _id('id+checkBoxes');
	mainDiv.style.display = "none";
	_querySelector(".mop-setup-box").classList.add("d-none");

	var submitForm = function(accountCurrencySet, mappingString, internationalChecked, domesticChecked) {
		var token  = document.getElementsByName("token")[0].value;

		$.ajax({
			type : "POST",
			url : "mopSetUp",
			data : {
				merchantEmailId : _id("merchants").value,
				acquirer : _id("acquirer").value,
				userType : _id("userType").value,
				mapString : mappingString,
				emailId : _id("userEmail").value,
				international : internationalChecked,
				domestic : domesticChecked,
				accountCurrencySet : JSON.stringify(accountCurrencySet),
				token : token,
				"struts.token.name": "token",
			},
			success : function(data) {
				console.log(data);
				var response = data.response;
				
				setTimeout(function() {
					alert(response);
					_querySelector("body").classList.add("loader--inactive");
				}, 1000);				
			},
			error : function(status) {
				setTimeout(function() {
					alert("Error, mapping not saved!!!! ");
					_querySelector("body").classList.add("loader--inactive");
				}, 1000);
				// window.location.reload();
			}
		});
	}

	var validatePaymentType = function() {
		var paymentCheckCounter = 0;
		var _paymentTypeList = _id("paymentCheck").querySelectorAll("li");

		var mappingString = [];

		for(var _paymentIndex = 0; _paymentIndex < _paymentTypeList.length; _paymentIndex++) {
			var _paymentType = _paymentTypeList[_paymentIndex].querySelector(".payment-types input[type='checkbox']");

			if(_paymentType.checked) {
				paymentCheckCounter++;

				var elementId = _paymentType.id;
				if(elementId == "Net Bankingbox") {
					var _nbList = _paymentTypeList[_paymentIndex].querySelectorAll(".netbankinglabel");
					var nbCheckCounter = 0;
					for(var _nbIndex = 0; _nbIndex < _nbList.length; _nbIndex++) {
						var _nbElement = _nbList[_nbIndex].querySelector("input[type='checkbox']");

						if(_nbElement.checked) {
							nbCheckCounter++;
							mappingString.push(_nbElement.id);
						}

						if(_nbIndex == _nbList.length - 1 && !nbCheckCounter > 0) {
							alert("Please select atleast one bank");
							_querySelector("body").classList.add("loader--inactive");
							return false;
						}
					}
				} else {
					var mopCheckCounter = 0;
					var _mopTypeList = _paymentTypeList[_paymentIndex].querySelectorAll(".payment-boxes");
				
					for(var _mopIndex = 0; _mopIndex < _mopTypeList.length; _mopIndex++) {
						var _mopType = _mopTypeList[_mopIndex].querySelector(".mop-type input[type='checkbox']");
						
						if(_mopType.checked) {
							mopCheckCounter++;
				
							var txnCheckCounter = 0;
							var _txnTypeList = _mopTypeList[_mopIndex].querySelectorAll(".txn-type");
							
							
							for(var _txnIndex = 0; _txnIndex < _txnTypeList.length; _txnIndex++) {
								var _txnType = _txnTypeList[_txnIndex].querySelector("input[type='checkbox']");
								
								if(_txnType.checked) {
									txnCheckCounter++;
									mappingString.push(_txnType.id);
								}
	
								if(_txnIndex == _txnTypeList.length - 1 && !txnCheckCounter > 0) {
									alert("Please select any txn type");
									_querySelector("body").classList.add("loader--inactive");
									return false;
								}
							}
						}
	
						
					}
				}			
			}

			
		}

		return mappingString.join();
	}
	
	var validateCurrencyType = function() {
		var _currencyCheckCounter = 0;
		var currencyboxes = document.getElementsByName("currency");
		var accountCurrencySet = [];

		for (_currencyIndex = 0; _currencyIndex < currencyboxes.length; _currencyIndex++) {
			_currencyCheckbox = currencyboxes[_currencyIndex];
			if (_currencyCheckbox.checked) {
				_currencyCheckCounter++;

				var password = _id("idpassword+" + _currencyCheckbox.value).value;
				var txnKey = _id("idtxnkey+" + _currencyCheckbox.value).value;
				var merchant = _id("idmerchantid+" + _currencyCheckbox.value).value;													
				
				var adf1 = _id("idadf1+" + _currencyCheckbox.value).value;
				var adf2 = _id("idadf2+" + _currencyCheckbox.value).value;
				var adf3 = _id("idadf3+" + _currencyCheckbox.value).value;
				var adf4 = _id("idadf4+" + _currencyCheckbox.value).value;
				var adf5 = _id("idadf5+" + _currencyCheckbox.value).value;
				var adf6 = _id("idadf6+" + _currencyCheckbox.value).value;
				var adf7 = _id("idadf7+" + _currencyCheckbox.value).value;
				var adf8 = _id("idadf8+" + _currencyCheckbox.value).value;
				var adf9 = _id("idadf9+" + _currencyCheckbox.value).value;
				var adf10 = _id("idadf10+" + _currencyCheckbox.value).value;
				var adf11 = _id("idadf11+" + _currencyCheckbox.value).value;
				
				if (merchant == "" && adf5 == ""){
					alert("Merchant Id / ADF 5 , atleast one is mandatory for mapping currency");
					_querySelector("body").classList.add("loader--inactive");
					return false;
				}
				
				if (acquirer == "CITRUS" || acquirer =="FSS"){
					var directTxn = _id("id3dflag+" + _currencyCheckbox.value).checked;	
				}

				var currencyCode = _currencyCheckbox.value;
				var accountCurrency = {
					currencyCode	:	currencyCode,
					password		:	password,
					txnKey			:	txnKey,
					adf1			:	adf1,
					adf2			:	adf2,
					adf3			:	adf3,
					adf4			:	adf4,
					adf5			:	adf5,
					adf6			:	adf6,
					adf7			:	adf7,
					adf8			:	adf8,
					adf9			:	adf9,
					adf10			:	adf10,
					adf11			:	adf11,
					merchantId		:	merchant,
					directTxn		:	directTxn
				};

				accountCurrencySet.push(accountCurrency);
			}

			
		}

		return accountCurrencySet;
		
	}

	$("#btnsubmit").click(function(evt) {
		evt.preventDefault();

		var _body = document.getElementsByTagName("body")[0];
		_body.classList.remove("loader--inactive");

		var resultCurrency = validateCurrencyType();		

		if(typeof resultCurrency == "object") {
			var internationalChecked  = _id("international").checked;
			var domesticChecked  = _id("domestic").checked;
			
			if ( internationalChecked == false && domesticChecked == false){
				alert("Select Atleast one Transaction Region !");
				_querySelector("body").classList.add("loader--inactive");
				return false;
			}

			var resultPayment = validatePaymentType();

			if(typeof resultPayment == "string") {
				console.log(resultCurrency);
				console.log(resultPayment);				

				submitForm(resultCurrency, resultPayment, internationalChecked, domesticChecked);
			}
		}
	});
});

$(window).on("load", function(data) {
	$(".checkbox-label").each(function(e) {
		var that = $(this).find("input[type=checkbox]");
		if(that.prop("checked") == true) {
			$(this).addClass("checkbox-checked");
		}
	});
});