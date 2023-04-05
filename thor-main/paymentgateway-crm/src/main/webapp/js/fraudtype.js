var ipRuleColumns				=	['ipAddListBody', 'payId', 'ipAddress', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'];
	wlIpRuleColumns				=	['wlIpAddListBody', 'payId', 'whiteListIpAddress', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	issuerCountryColumns		=	['issuerCountryListBody', 'payId', 'issuerCountry'],
	userCountryColumns			=	['userCountryListBody', 'payId', 'userCountry'],
	emailRuleColumns			=	['emailListBody', 'payId', 'email', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	domainRuleColumns			=	['domainListBody', 'payId', 'domainName', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	txnAmountColumns			=	['txnAmountListBody', 'payId', 'currency', 'paymentType', 'paymentRegion', 'minTransactionAmount', 'maxTransactionAmount'],
	cardBinColumns				=	['cardBinListBody', 'payId', 'negativeBin', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	cardNoColumns				=	['cardNoListBody', 'payId', 'cardMask', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	perMerchantTxnsColumns		=	['noOfTxnsListBody', 'payId', 'minutesTxnLimit', 'perCardTransactionAllowed'],
	perCardTxnsColumns			=	['perCardTxnsListBody', 'payId', 'cardMask', 'perCardTransactionAllowed', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	txnVelocityColumns			= 	['txnVelocityListBody', 'payId', 'paymentType', 'paymentRegion', 'timePeriod', 'noOfTransactionAllowed'],
	amtVelocityColumns			= 	['amtVelocityListBody', 'payId', 'paymentType', 'paymentRegion', 'timePeriod', 'amountAllowed'],
	saleAmtVelocityColumns		= 	['saleAmtVelocityListBody', 'payId', 'paymentType', 'paymentRegion', 'timePeriod', 'amountAllowed'],
	vpaColumns					=	['vpaAddListBody', 'payId', 'vpa', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	vpaTransactionColumns		=	['vpaTransactionAddListBody', 'payId', 'vpa', 'vpaTotalTransactionAllowed', 'dateActiveFrom', 'dateActiveTo', 'startTime', 'endTime', 'repeatDays'],
	ajaxValidationFlag			=	false,
	ipAddressS					=	'BLOCK_IP_ADDRESS',
	whiteListIpAddressS			=	'WHITE_LIST_IP_ADDRESS',
	numberOfTransaction			=	'BLOCK_NO_OF_TXNS',
	transactionAmount			=	'BLOCK_TXN_AMOUNT',
	domainNameS					=	'BLOCK_DOMAIN_NAME',
	emailS						=	'BLOCK_EMAIL_ID',
	userCountry					=	'BLOCK_USER_COUNTRY',
	issuerCountry				=	'BLOCK_CARD_ISSUER_COUNTRY',
	negativeBinS				=	'BLOCK_CARD_BIN',
	negativeCardS				=	'BLOCK_CARD_NO',
	perCardTransactionAllowedS	=	'BLOCK_CARD_TXN_THRESHOLD',
	transactionVelocity			= 	'BLOCK_TXN_VELOCITY',
	amountVelocity				= 	'BLOCK_AMOUNT_VELOCITY',
	saleAmountVelocity			= 	'BLOCK_SALE_AMOUNT_VELOCITY',
	vpa							=	'BLOCK_VPA',
	vpaTransaction				=	'BLOCK_VPA_TXN';

var hideLoader = function() {
	setTimeout(function() {
		$("body").addClass('loader--inactive');
	}, 1000);
}

var setDate = function(parentId) { 
    // DATEPICKER
    $(".datepicker2").datepicker({
        prevText: "click for previous months",
        nextText: "click for next months",
        showOtherMonths: true,
        dateFormat: 'dd/mm/yy',
        selectOtherMonths: false,
        minDate: new Date()
    });
}

var addCountry = function(deleteBtn) {
	var _dataSrc = $(deleteBtn).closest("tr").find(".edit-rule").attr("data-src");
	
	if(_dataSrc == "#BLOCK_CARD_ISSUER_COUNTRY-rule" || _dataSrc == "#BLOCK_USER_COUNTRY-rule") {
		var _text = $(deleteBtn).closest("tr").find("td:nth-child(2) input").val();
		var _select = $(_dataSrc + " .selectpicker");
		
		_select.append('<option value="'+ _text +'">'+ _text +'</option>');

		_select.html(_select.find('option').sort(function(x, y) {
			// to change to descending order switch "<" for ">"
			return $(x).text() > $(y).text() ? 1 : -1;
		}));

		_select.selectpicker("refresh");
	}
}

function deleteFraudRule(event, that, ruleId) {
	event.preventDefault();

	var confirmationFlag = confirm("Do you want to delete this rule");

	if (confirmationFlag) {
		$("body").removeClass('loader--inactive');

		var payIdAlt = $("#payId").val();
		

		if(payIdAlt == '') {
			payIdAlt = 'ALL';
		}

		$.ajax({
			url: 'deleteFraudRule',
			type: 'post',
			data: {
				token: document.getElementsByName("token")[0].value,
				payId: fraudFieldValidate(that, 'payId', null), //TODO for merchant module
				ruleId: ruleId,
			},
			success: function (data) {
				if ((data.response) != null) {
					// addCountry(that);
					
					success_snackbar.innerHTML = data.response;
					showSnackbar("success-snackbar");

					clearRules();
					fetchFraudRuleList(payIdAlt);
				} else {
					error_snackbar.innerHTML = "Try again, Something went wrong!";
					showSnackbar("error-snackbar");
				}

				setTimeout(function() {
					$("body").addClass("loader--inactive");
				}, 1000);
			},
			error: function (data) {
				error_snackbar.innerHTML = data.response;
				showSnackbar("error-snackbar");

				setTimeout(function() {
					$("body").addClass("loader--inactive");
				}, 1000);
			}
		});
	} else {
		return false;
	}
}

function fillDynamicValues(rule, ruleColumns) {
	var tableHtml = '<tr';

	for (var i = 1; i < ruleColumns.length; i++) {
		tableHtml += ' data-flag="'+ rule['alwaysOnFlag'] +'" id="rowid-'+ rule['id'] +'" class="font-size-12 color-grey-light box-shadow-common merchant-config-row"><td class="border-none"><span class="visual-mode">' + rule[ruleColumns[i]] + '</span><input type="text" value="'+ rule[ruleColumns[i]] +'" class="form-control" data-id="'+ ruleColumns[i] +'" hidden></td>';
	}

	if(rule['status'] == 'ACTIVE') {
		var status = 'checked';
	} else {
		var status = '';
	}

	if(rule['fraudType'] == "BLOCK_CARD_ISSUER_COUNTRY" || rule['fraudType'] == "BLOCK_USER_COUNTRY") {
		var displayClass = 'd-none';
	} else {
		var displayClass = 'd-flex';
	}

	tableHtml += '<td class="border-none action-btns"><div class="visual-mode d-flex"><a data-fancybox="" data-src="#'+ rule['fraudType'] +'-rule" data-modal="true" href="javascript:;" title="Edit" data-btn="edit" class="d-none align-items-center justify-content-center bg-color-primary-light-2 color-primary hover-color-primary font-size-16 border-radius-4 px-5 edit-rule"><i class="fa fa-edit"></i></a><a href="#" onclick="deleteFraudRule(event, this, '+ rule['id'] +')" data-btn="remove" title="Delete" class="d-flex align-items-center justify-content-center bg-color-primary-light-2 color-primary hover-color-primary font-size-16 border-radius-4 px-5"><i class="fa fa-trash"></i></a></div></td>';

	$('#' + ruleColumns[0]).append(tableHtml);
}

function writeFraudTable(item, index) {
	var rule = item,
		fraudType = rule['fraudType'];

	//to genrate dynamic headers
	switch (fraudType) {
		case numberOfTransaction: {
			fillDynamicValues(rule, perMerchantTxnsColumns)
		} break;
		case transactionAmount: {
			fillDynamicValues(rule, txnAmountColumns)
		} break;
		case ipAddressS: {
			fillDynamicValues(rule, ipRuleColumns)
		} break;
		case whiteListIpAddressS: {
			fillDynamicValues(rule, wlIpRuleColumns)
		} break;
		case domainNameS: {
			fillDynamicValues(rule, domainRuleColumns)
		} break;
		case emailS: {
			fillDynamicValues(rule, emailRuleColumns)
		} break;
		case userCountry: {
			fillDynamicValues(rule, userCountryColumns)
		} break;
		case issuerCountry: {
			fillDynamicValues(rule, issuerCountryColumns)
		} break;
		case negativeBinS: {
			fillDynamicValues(rule, cardBinColumns)
		} break;
		case negativeCardS: {
			fillDynamicValues(rule, cardNoColumns)
		} break;
		case perCardTransactionAllowedS: {
			fillDynamicValues(rule, perCardTxnsColumns)
		} break;
		case transactionVelocity: {
			fillDynamicValues(rule, txnVelocityColumns)
		} break;
		case amountVelocity: {
			fillDynamicValues(rule, amtVelocityColumns)
		} break;
		case saleAmountVelocity: {
			fillDynamicValues(rule, saleAmtVelocityColumns)
		} break;
		case vpa: {
			fillDynamicValues(rule, vpaColumns)
		} break;
		case vpaTransaction: {
			fillDynamicValues(rule, vpaTransactionColumns)
		} break;
		default: {
			error_snackbar.innerHTML = "Try again, Something went wrong.";
			showSnackbar("error-snackbar");
		}
	}
}

function fetchFraudRuleList(payIdValue) {
	$.ajax({
		url: 'fetchFraudRules',
		type: 'post',
		data: {
			token: document.getElementsByName("token")[0].value,
			// payId: (payIdValue == 'ALL') ? payIdValue : getFieldValue('payId'),
			payId: payIdValue
		},
		success: function (data) {
			var fraudRuleList = data.fraudRuleList;
			fraudRuleList.forEach(function (item, index) {
				writeFraudTable(item, index);
			});

			setTimeout(function() {
				$("body").addClass('loader--inactive');
			}, 1000);
		},
		error: function (data) {
			error_snackbar.innerHTML = "soemthing went wrong! " + data.response;
			showSnackbar("error-snackbar");
			
			setTimeout(function() {
				$("body").addClass('loader--inactive');
			}, 1000);
		}
	});
}

//clear all the old displayed fraud rules on merchant changed
function clearFraudRules(tableNames) {
	for(var i = 0; i < tableNames.length; i++) {
		var getTable = document.getElementById(tableNames[i]);
		getTable.tBodies[0].innerHTML = '';
	}
}

var clearRules = function() {
	clearFraudRules([ipRuleColumns[0], wlIpRuleColumns[0], issuerCountryColumns[0], userCountryColumns[0], emailRuleColumns[0], domainRuleColumns[0], txnAmountColumns[0], cardBinColumns[0], cardNoColumns[0], perCardTxnsColumns[0], txnVelocityColumns[0], amtVelocityColumns[0], saleAmtVelocityColumns[0], vpaColumns[0], vpaTransactionColumns[0]]);
}

function makeCardMasklst() {
	var element = document.getElementById('negativeCard');
	var initialDigits = document.getElementById('cardIntialDigits').value;
	var lastDigits = document.getElementById('cardLastDigits').value;
	value = element.value;
	if (lastDigits.length == 4) {
		element.value = initialDigits + "******" + lastDigits;
		$('#validate_crdL').text('Valid Card Length');
		document.getElementById("validate_crdL").classList.add("success");
		document.getElementById("validate_crdL").classList.remove("error");
		return true;
	}
	else {
		$('#validate_crdL').text('Enter Card Length 4 digit Number Only');
		document.getElementById("validate_crdL").classList.add("error");
		document.getElementById("validate_crdL").classList.remove("success");
		return false;
	}
}

function iniCardMask() {
	var element = document.getElementById('prenegativeCard');
	var preinitialDigits = document.getElementById('precardIntialDigits').value;
	var prelastDigits = document.getElementById('precardLastDigits').value;
	value = element.value;
	element.value = preinitialDigits + "******" + prelastDigits;


	if (preinitialDigits.length == 6) {
		//element.value = initialDigits+"******"+lastDigits;
		//$('#validate_crdIn').text('Valid Card Length');	
		var arra = element.value.split("");
		if (arra[0] == "3" || arra[0] == "4" || arra[0] == "5") {
			$('#validate_crdInpre').text('Valid Card Length');
			document.getElementById("validate_crdInpre").classList.add("success");
			document.getElementById("validate_crdInpre").classList.remove("error");
			return true;
		}
	} else {
		$('#validate_crdInpre').text('Enter Card Number Starts With 3,4,5 only of 6 digits length');
		document.getElementById("validate_crdInpre").classList.add("error");
		document.getElementById("validate_crdInpre").classList.remove("success");
		return false;
	}
}

function lastCardMask() {
	var element = document.getElementById('prenegativeCard');
	var preinitialDigits = document.getElementById('precardIntialDigits').value;
	var prelastDigits = document.getElementById('precardLastDigits').value;
	value = element.value;
	element.value = preinitialDigits + "******" + prelastDigits;
	if (prelastDigits.length == 4) {
		$('#validate_crdLpre').text('Valid Card Length');
		document.getElementById("validate_crdLpre").classList.add("success");
		document.getElementById("validate_crdLpre").classList.remove("error");
		return true;
	}
	else {
		$('#validate_crdLpre').text('Enter Card Length 4 digit Number Only');
		document.getElementById("validate_crdLpre").classList.add("error");
		document.getElementById("validate_crdLpre").classList.remove("success");
		return false;
	}
}

function getFieldValue(fieldName) {
	var element = document.getElementById(fieldName);
	// console.log(element);
	if (element.tagName == 'INPUT') {
		var fieldValue = element.value;
		var finalValue = (fieldValue != '') ? fieldValue : ''; //TODO regex
		return finalValue;
	} else if (element.tagName == 'SELECT') {
		if(element.type == 'select-one') {
			var fieldValue = element.options[element.selectedIndex].value;
			var finalValue = (!fieldValue.match('SELECT')) ? fieldValue : '';
			return finalValue;
		} else if(element.type == 'select-multiple') {			
			let selectedValues = Array.from(element.selectedOptions).map(option => option.value);
			return selectedValues;
		}
		
	} else if (element.tagName == 'UL') {
		var optionName = element.getAttribute('data-name');
		var finalValue = [];
		$('input[name="' + optionName + '"]:checked').each(function () {
			finalValue.push($(this).val());
		});
		return finalValue;
	} else if (element.tagName == 'DIV') {
		var optionName = element.getAttribute('data-name');
		var finalValue = [];
		$('input[name="' + optionName + '"]').each(function () {
			finalValue.push($(this).val());
		});
		return finalValue;
	}
}

var validateTxnAmount = function(that) {
	var parentDiv = that.closest(".modal-box-new-rule"),
		minTxnAmt = Number(parentDiv.querySelector('[data-input="minTransactionAmount"]').value),
		maxTxnAmt = Number(parentDiv.querySelector('[data-input="maxTransactionAmount"]').value);

	if(minTxnAmt > maxTxnAmt) {
		error_snackbar.innerHTML = "Minimum Amount cannot be greater than Maximum Amount.";
		showSnackbar("error-snackbar");
		return true;
	} else if(minTxnAmt == maxTxnAmt) {
		error_snackbar.innerHTML = "Minimum Amount cannot be equal to Maximum Amount.";
		showSnackbar("error-snackbar");
		return true;
	}

	return false;
}

var dateValidate = function(that) {
	var parentDiv = that.closest('.modal-box-new-rule');

	var dateFrom = parentDiv.querySelector("[data-input='dateActiveFrom']").value;
	var dateTo = parentDiv.querySelector("[data-input='dateActiveTo']").value;
	var oneDay = 24 * 60 * 60 * 1000;

	dateFrom = dateFrom.split("/");
	dateFrom = new Date(dateFrom[2], dateFrom[1] - 1, dateFrom[0]);
	dateTo = dateTo.split("/");
	dateTo = new Date(dateTo[2], dateTo[1] - 1, dateTo[0]);

	dateFrom = dateFrom.getTime();
	dateTo = dateTo.getTime();

	if(dateFrom > dateTo) {
		error_snackbar.innerHTML = "Start date cannot be grater than End date.";
		showSnackbar("error-snackbar");
		return true;
	} else if(dateFrom == dateTo) {
		var startTime = parentDiv.querySelector("[data-input='startTime']").value;
		var endTime = parentDiv.querySelector("[data-input='endTime']").value;
		if(startTime !== '' && endTime !== '' && startTime >= endTime) {
			error_snackbar.innerHTML = "End time should be greater than Start time when date is same";
			showSnackbar("error-snackbar");
			return true;
		}
	} else {
		return false;
	}

	return false;
}

function fraudFieldValidate(that, fieldName, fraudType) {
	if (fieldName == 'payId') {
		return getFieldValue(fieldName);
	}
	switch (fraudType) {
		case ipAddressS : {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag1");			

			if (fieldName == "ipAddress") {
				var ipAddress = getFieldValue("ipAddress");
				if (ipAddress != '' && ipAddress != null) {
					ajaxValidationFlag = false;
					return ipAddress;
				} else {
					error_snackbar.innerHTML = "Please enter IP Address.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {					
					break;
				} else {
					var dateActiveFrom = getFieldValue("dateActiveFrom");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveTo = getFieldValue("dateActiveTo");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var startTime = getFieldValue("startTime");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var endTime = getFieldValue("endTime");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");

						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var value = getFieldValue('repeatDays').join(',');
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select Days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case whiteListIpAddressS: {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag2");

			if (fieldName == "whiteListIpAddress") {
				var whiteListIpAddress = getFieldValue("whiteListIpAddress");
				if (whiteListIpAddress != '' && whiteListIpAddress != null) {
					ajaxValidationFlag = false;
					return whiteListIpAddress;
				} else {
					error_snackbar.innerHTML = "Please enter IP Address.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveFrom = getFieldValue("dateActiveFrom1");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveTo = getFieldValue("dateActiveTo1");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var startTime = getFieldValue("startTime1");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var endTime = getFieldValue("endTime1");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var value = getFieldValue('repeatDays1').join(',');
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select Days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;
		
		case emailS: {
			if (ajaxValidationFlag) {
				break;
			}
			var alwaysOnFlag = getFieldValue("alwaysOnFlag4");

			if (fieldName == "email") {
				var email = getFieldValue("email");
				if (email != '' && email != null) {
					ajaxValidationFlag = false;
					return email;
				} else {
					error_snackbar.innerHTML = "Please enter email address";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveFrom = getFieldValue("dateActiveFrom3");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveTo = getFieldValue("dateActiveTo3");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var startTime = getFieldValue("startTime3");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var endTime = getFieldValue("endTime3");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var value = getFieldValue('repeatDays3').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case userCountry: {
			if (fieldName == "userCountry") {
				var value = getFieldValue(fieldName).join(",");
				if (value != '' && value != null) {
					ajaxValidationFlag = false;
					return value;
				} else {
					error_snackbar.innerHTML = "Please select country";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			}
		} break;

		case issuerCountry: {
			if (fieldName == "issuerCountry") {
				var value = getFieldValue(fieldName).join(",");
				// console.log(value.trim());
				if (value != '' && value != null) {
					ajaxValidationFlag = false;
					return value;
				} else {
					error_snackbar.innerHTML = "Please select country";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			}
		} break;

		case domainNameS: {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag5");

			if (fieldName == "domainName") {
				var domainName = getFieldValue("domainName");
				if (domainName != '' && domainName != null) {
					ajaxValidationFlag = false;
					return domainName;
				} else {
					error_snackbar.innerHTML = "Please enter domain name.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveFrom = getFieldValue("dateActiveFrom2");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveTo = getFieldValue("dateActiveTo2");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var startTime = getFieldValue("startTime2");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var endTime = getFieldValue("endTime2");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var value = getFieldValue('repeatDays2').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case transactionAmount: {
			if (ajaxValidationFlag) {
				break;
			}

			if(fieldName == "currency") {
				var currency = getFieldValue(fieldName);
				if(currency !== '' && currency !== null) {
					ajaxValidationFlag = false;
					return currency;
				} else {
					error_snackbar.innerHTML = "Please select currency";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "paymentType") {
				var txnpaymentType = getFieldValue("txnAmtpaymentType");
				if(txnpaymentType !== null && txnpaymentType !== "") {
					ajaxValidationFlag = false;
					return txnpaymentType;
				} else {
					error_snackbar.innerHTML = "Please select payment type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "regionType") {
				var txnpaymentRegion = getFieldValue("txnAmtpaymentRegion");
				if(txnpaymentRegion !== null && txnpaymentRegion !== "") {
					ajaxValidationFlag = false;
					return txnpaymentRegion;
				} else {
					error_snackbar.innerHTML = "Please select region type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "minTransactionAmount") {
				var minAmount = getFieldValue("minTransactionAmount");
				if (minAmount !== '' && minAmount !== null) {
					ajaxValidationFlag = validateTxnAmount(that);
					return minAmount;
				} else {
					error_snackbar.innerHTML = "Please enter minimum amount";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
				// parseFloat(minAmount.trim() <= parseFloat(maxAmount.trim()
			} else if(fieldName == "maxTransactionAmount") {
				var maxAmount = getFieldValue("maxTransactionAmount");
				if(maxAmount.trim() !== '' && maxAmount.trim() != null) {
					ajaxValidationFlag = validateTxnAmount(that);
					return maxAmount;
				} else {
					error_snackbar.innerHTML = "Please enter maximum amount";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			}
		} break;

		case negativeBinS: {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag3");

			if (fieldName == "negativeBin") {
				var negativeBin = getFieldValue("negativeBin");
				if (negativeBin != '' && negativeBin != null) {
					ajaxValidationFlag = false;
					return negativeBin;
				} else {
					error_snackbar.innerHTML = "Please enter card range";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveFrom = getFieldValue("dateActiveFrom4");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveTo = getFieldValue("dateActiveTo4");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var startTime = getFieldValue("startTime4");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var endTime = getFieldValue("endTime4");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var value = getFieldValue('repeatDays4').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;
		
		case negativeCardS: {
			if (ajaxValidationFlag) {
				break;
			}
			var alwaysOnFlag = getFieldValue("alwaysOnFlag6");

			if (fieldName == "negativeCard") {
				var negativeCard = getFieldValue("negativeCard");
				if (negativeCard != '' && negativeCard != null) {					
					negativeCard = negativeCard.replace(/ /g,'');					
					if(negativeCard.length < 13) {						
						error_snackbar.innerHTML = "Card no. should be minimum 13 digits";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					} else {
						ajaxValidationFlag = false;
						return negativeCard;
					}
				} else {					
					error_snackbar.innerHTML = "Please enter card number";
					showSnackbar("error-snackbar");
					
					ajaxValidationFlag = true;
					break;
				}

			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveFrom = getFieldValue("dateActiveFrom5");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveTo = getFieldValue("dateActiveTo5");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var startTime = getFieldValue("startTime5");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var endTime = getFieldValue("endTime5");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var value = getFieldValue('repeatDays5').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case perCardTransactionAllowedS: {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag7");

			if (fieldName == "negativeCard") {
				var prenegativeCard = getFieldValue("prenegativeCard");
				if (prenegativeCard != '' && prenegativeCard != null) {
					ajaxValidationFlag = false;
					prenegativeCard = prenegativeCard.replace(/ /g,'');
					if(prenegativeCard.length < 13) {						
						error_snackbar.innerHTML = "Card no. should be minimum 13 digits";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
					return prenegativeCard;
				} else {
					error_snackbar.innerHTML = "Please enter card number";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}

			} else if (fieldName == "perCardTransactionAllowed") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var perCardTransactionAllowed = getFieldValue("perCardTransactionAllowed");
					if (perCardTransactionAllowed != '' && perCardTransactionAllowed != null) {
						ajaxValidationFlag = false;
						return perCardTransactionAllowed;
					} else {
						error_snackbar.innerHTML = "Please enter no. of transaction.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveFrom = getFieldValue("dateActiveFrom7");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveTo = getFieldValue("dateActiveTo7");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var startTime = getFieldValue("startTime7");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var endTime = getFieldValue("endTime7");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var value = getFieldValue('repeatDays7').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case transactionVelocity: {
			if (ajaxValidationFlag) {
				break;
			}

			if (fieldName == "paymentType") {
				var txnpaymentType = getFieldValue("txnpaymentType");
				if(txnpaymentType !== null && txnpaymentType !== "") {
					ajaxValidationFlag = false;
					return txnpaymentType;
				} else {
					error_snackbar.innerHTML = "Please select payment type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "regionType") {
				var txnpaymentRegion = getFieldValue("txnpaymentRegion");
				if(txnpaymentRegion !== null && txnpaymentRegion !== "") {
					ajaxValidationFlag = false;
					return txnpaymentRegion;
				} else {
					error_snackbar.innerHTML = "Please select region type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "timePeriod") {
				var txntimePeriod = getFieldValue("txntimePeriod");
				if(txntimePeriod !== null && txntimePeriod !== "") {
					ajaxValidationFlag = false;
					return txntimePeriod;
				} else {
					error_snackbar.innerHTML = "Please select time period.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "allowedTxn") {
				var noOfTransactionAllowed = getFieldValue("noOfTransactionAllowed");
				if(noOfTransactionAllowed !== null && noOfTransactionAllowed !== "") {
					ajaxValidationFlag = false;
					return noOfTransactionAllowed;
				} else {
					error_snackbar.innerHTML = "Please enter no. of allowed transaction.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			}

		} break;

		case amountVelocity: {
			if (ajaxValidationFlag) {
				break;
			}

			if (fieldName == "paymentType") {
				var amtpaymentType = getFieldValue("amtpaymentType");
				if(amtpaymentType !== null && amtpaymentType !== "") {
					ajaxValidationFlag = false;
					return amtpaymentType;
				} else {
					error_snackbar.innerHTML = "Please select payment type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "regionType") {
				var amtpaymentRegion = getFieldValue("amtpaymentRegion");
				if(amtpaymentRegion !== null && amtpaymentRegion !== "") {
					ajaxValidationFlag = false;
					return amtpaymentRegion;
				} else {
					error_snackbar.innerHTML = "Please select region type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "timePeriod") {
				var amttimePeriod = getFieldValue("amttimePeriod");
				if(amttimePeriod !== null && amttimePeriod !== "") {
					ajaxValidationFlag = false;
					return amttimePeriod;
				} else {
					error_snackbar.innerHTML = "Please select time period.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "allowedAmt") {
				var amtVelocityAllowedCount = getFieldValue("amtVelocityAllowedCount");
				if(amtVelocityAllowedCount !== null && amtVelocityAllowedCount !== "") {
					ajaxValidationFlag = false;
					return amtVelocityAllowedCount;
				} else {
					error_snackbar.innerHTML = "Please enter total allowed amount.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			}
		} break;

		case saleAmountVelocity: {
			if (ajaxValidationFlag) {
				break;
			}

			if (fieldName == "paymentType") {
				var saleAmtpaymentType = getFieldValue("saleAmtpaymentType");
				if(saleAmtpaymentType !== null && saleAmtpaymentType !== "") {
					ajaxValidationFlag = false;
					return saleAmtpaymentType;
				} else {
					error_snackbar.innerHTML = "Please select payment type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "regionType") {
				var saleAmtpaymentRegion = getFieldValue("saleAmtpaymentRegion");
				if(saleAmtpaymentRegion !== null && saleAmtpaymentRegion !== "") {
					ajaxValidationFlag = false;
					return saleAmtpaymentRegion;
				} else {
					error_snackbar.innerHTML = "Please select region type.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "timePeriod") {
				var saleAmtTimePeriod = getFieldValue("saleAmtTimePeriod");
				if(saleAmtTimePeriod !== null && saleAmtTimePeriod !== "") {
					ajaxValidationFlag = false;
					return saleAmtTimePeriod;
				} else {
					error_snackbar.innerHTML = "Please select time period.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "allowedAmt") {
				var saleAmtVelocityAllowedCount = getFieldValue("saleAmtVelocityAllowedCount");
				if(saleAmtVelocityAllowedCount !== null && saleAmtVelocityAllowedCount !== "") {
					ajaxValidationFlag = false;
					return saleAmtVelocityAllowedCount;
				} else {
					error_snackbar.innerHTML = "Please enter total allowed amount.";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			}
		} break;

		case vpa: {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag9");

			if (fieldName == "vpa") {
				var _vpa = getFieldValue("vpa");
				if (_vpa != '' && _vpa != null) {
					ajaxValidationFlag = false;
					return _vpa;
				} else {
					error_snackbar.innerHTML = "Please enter valid VPA";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveFrom = getFieldValue("dateActiveFrom9");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var dateActiveTo = getFieldValue("dateActiveTo9");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var startTime = getFieldValue("startTime9");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var endTime = getFieldValue("endTime9");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				} else {
					var value = getFieldValue('repeatDays9').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case vpaTransaction: {
			if (ajaxValidationFlag) {
				break;
			}

			var alwaysOnFlag = getFieldValue("alwaysOnFlag8");

			if (fieldName == "vpa") {
				var _vpaTxn = getFieldValue("vpaTransaction");
				if (_vpaTxn != '' && _vpaTxn != null) {
					ajaxValidationFlag = false;
					return _vpaTxn;
				} else {
					error_snackbar.innerHTML = "Please enter valid VPA";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "vpaTotalTransactionAllowed") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var vpaTotalTransactionAllowed = getFieldValue("vpaTotalTransactionAllowed");
					if (vpaTotalTransactionAllowed != '' && vpaTotalTransactionAllowed != null) {
						ajaxValidationFlag = false;
						return vpaTotalTransactionAllowed;
					} else {
						error_snackbar.innerHTML = "Please enter no. of transaction.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "dateActiveFrom") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveFrom = getFieldValue("dateActiveFrom8");
					if (dateActiveFrom != '' && dateActiveFrom != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveFrom;
					} else {
						error_snackbar.innerHTML = "Please select start date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "dateActiveTo") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var dateActiveTo = getFieldValue("dateActiveTo8");
					if (dateActiveTo != '' && dateActiveTo != null) {
						ajaxValidationFlag = dateValidate(that);
						return dateActiveTo;
					} else {
						error_snackbar.innerHTML = "Please select end date.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "startTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var startTime = getFieldValue("startTime8");
					if (startTime != '' && startTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return startTime;
					} else {
						error_snackbar.innerHTML = "Please select start time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "endTime") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var endTime = getFieldValue("endTime8");
					if (endTime != '' && endTime != null) {
						ajaxValidationFlag = dateValidate(that);
						return endTime;
					} else {
						error_snackbar.innerHTML = "Please select end time.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}

			} else if (fieldName == "repeatDays") {
				if (alwaysOnFlag == "true") {
					break;
				}
				else {
					var value = getFieldValue('repeatDays8').join(",");
					if (value != '' && value != null) {
						ajaxValidationFlag = false;
						return value;
					} else {
						error_snackbar.innerHTML = "Please select days.";
						showSnackbar("error-snackbar");
						ajaxValidationFlag = true;
						break;
					}
				}
			} else if (fieldName == "alwaysOnFlag") {
				return alwaysOnFlag;
			}
		} break;

		case numberOfTransaction: {
			if (fieldName == "minutesTxnLimit") {
				var minutesTxnLimit = getFieldValue("minutesTxnLimit");
				if (minutesTxnLimit != '' && minutesTxnLimit != null) {
					ajaxValidationFlag = false;
					return minutesTxnLimit;
				} else {
					error_snackbar.innerHTML = "Please select country";
					showSnackbar("error-snackbar");
					ajaxValidationFlag = true;
					break;
				}
			} else if (fieldName == "perCardTransactionAllowed") {
				var perCardTransactionAllowed = getFieldValue("perCardTransactionAllowedvelo");
				if (perCardTransactionAllowed != '' && perCardTransactionAllowed != null) {
					ajaxValidationFlag = false;
					return perCardTransactionAllowed;
				} else {
					ajaxValidationFlag = true;
					break;
				}
			}
		} break;
	}
}

var _paymentType = [],
	_amtpaymentRegion = [];

var paymentTypeSelectBox = document.querySelectorAll(".paymentType-selectbox");

paymentTypeSelectBox.forEach(function(element) {
	element.addEventListener("change", function(e) {
		var _getValue = this.value;

		_paymentType = [];
		if(_getValue == "ALL") {
			var _getAllInput = this.querySelectorAll("option");
			for(var i = 0; i < _getAllInput.length; i++) {
				_paymentType.push(_getAllInput[i].value);
			}
		}
	});
});



$(".paymentRegion-selectbox").on("change", function(e) {
	var _getValue = $(this).val();
	_amtpaymentRegion = [];

	if(_getValue.length > 1) {
		_amtpaymentRegion = _getValue;
	}
});

function ajaxFraudRequest(that, fraudType) {
	console.log(fraudType);
	ajaxValidationFlag = false;
	var payId,
		whiteListIpAddress = fraudFieldValidate(that, 'whiteListIpAddress', fraudType),
		ipAddress = fraudFieldValidate(that, 'ipAddress', fraudType),
		token = document.getElementsByName("token")[0].value,
		issuerCountry = fraudFieldValidate(that, 'issuerCountry', fraudType),
		email = fraudFieldValidate(that, 'email', fraudType),
		domainName = fraudFieldValidate(that, 'domainName', fraudType),
		negativeBin = fraudFieldValidate(that, 'negativeBin', fraudType),
		negativeCard = fraudFieldValidate(that, 'negativeCard', fraudType),
		currency = fraudFieldValidate(that, 'currency', fraudType),
		paymentType = fraudFieldValidate(that, 'paymentType', fraudType),
		$paymentRegion = fraudFieldValidate(that, 'regionType', fraudType),
		minTransactionAmount = fraudFieldValidate(that, 'minTransactionAmount', fraudType),
		maxTransactionAmount = fraudFieldValidate(that, 'maxTransactionAmount', fraudType),
		userCountry = fraudFieldValidate(that, 'userCountry', fraudType),
		minutesTxnLimit = fraudFieldValidate(that, 'minutesTxnLimit', fraudType),
		perCardTransactionAllowed = fraudFieldValidate(that, 'perCardTransactionAllowed', fraudType),
		timePeriod = fraudFieldValidate(that, 'timePeriod', fraudType),
		noOfTransactionAllowed = fraudFieldValidate(that, 'allowedTxn', fraudType),
		amountAllowed = fraudFieldValidate(that, 'allowedAmt', fraudType),
		_vpa = fraudFieldValidate(that, 'vpa', fraudType),
		_vpaTxnAllowed = fraudFieldValidate(that, 'vpaTotalTransactionAllowed', fraudType),
		paymentTypeArray = _paymentType.toString(),
		paymentRegionArray = _amtpaymentRegion.join(","),
		alwaysOnFlag = fraudFieldValidate(that, 'alwaysOnFlag', fraudType),
		inputCheckbox = that.closest('.modal-box-new-rule').querySelector(".custom-control-input");
	
	if(inputCheckbox !== null) {		
		var inputCheckboxValue = inputCheckbox.getAttribute('id');
	}

	var flagCheckbox = that.closest('.modal-box-new-rule').querySelector("[data-checkbox='"+ inputCheckboxValue +"']");

	if(flagCheckbox != null) {		
		var flag = flagCheckbox.value;
	}

	if($paymentRegion !== undefined) {
		if($paymentRegion.length > 1) {
			$paymentRegion = "ALL";
		} else {
			$paymentRegion = $paymentRegion.toString();
		}
	}

	if(flag == "false") {
		var dateActiveFrom = fraudFieldValidate(that, 'dateActiveFrom', fraudType),
			dateActiveTo = fraudFieldValidate(that, 'dateActiveTo', fraudType),
			startTime = fraudFieldValidate(that, 'startTime', fraudType),
			endTime = fraudFieldValidate(that, 'endTime', fraudType),
			repeatDays = fraudFieldValidate(that, 'repeatDays', fraudType);
	}

	if (!ajaxValidationFlag) {
		$("body").removeClass('loader--inactive');

		var parentDivBox = that.closest('.modal-box-new-rule'),
			rowId = parentDivBox.querySelector("[data-input='rowId']").value;
		
		if(parentDivBox.classList.contains("edit-mode--active")) {
			var inputVal = parentDivBox.querySelector("[data-input='payId']").value,
				payIdBox = document.getElementById("payId"),
				payIdLength = payIdBox.options.length;

			for(var i = 0; i < payIdLength; i++) {
				var payIdText = payIdBox.options[i].text
				
				if(payIdText == inputVal) {
					payId = payIdBox.options[i].value;
				}				
			}

			$.ajax({
				url: 'editFraudRule',
				type: 'post',
				data: {
					rowId: rowId,
					whiteListIpAddress			:	whiteListIpAddress,
					ipAddress					:	ipAddress,
					token						:	token,
					payId						:	payId,
					issuerCountry				:	issuerCountry,
					email						:	email,
					domainName					:	domainName,
					negativeBin					:	negativeBin,
					dateActiveFrom				:	dateActiveFrom,
					dateActiveTo				:	dateActiveTo,
					startTime					:	startTime,
					endTime						:	endTime,
					repeatDays					:	repeatDays,
					alwaysOnFlag				:	flag,
					negativeCard				:	negativeCard,
					currency					:	currency,					
					minTransactionAmount		:	minTransactionAmount,
					maxTransactionAmount		:	maxTransactionAmount,
					userCountry					:	userCountry,
					fraudType					:	fraudType,
					perCardTransactionAllowed	:	perCardTransactionAllowed,
					paymentType					:	paymentType,
					paymentRegion				:	$paymentRegion,
					timePeriod					:	timePeriod,
					noOfTransactionAllowed		:	noOfTransactionAllowed,
					amountAllowed				:	amountAllowed,
					minutesTxnLimit				:	minutesTxnLimit,
					status						:	status,
					paymentTypeArray 			:   paymentTypeArray.slice(5, paymentTypeArray.length),
					paymentRegionArray			:   paymentRegionArray,
					vpa							:	_vpa,
					vpaTotalTransactionAllowed	:	_vpaTxnAllowed
					
				},
				success: function(data) {
					var result = data;
					// console.log(result);
					if (result != null) {
						var errorFieldMap = data["Invalid request"];
						if (errorFieldMap != null) {
							var error;
							for (key in errorFieldMap) {
								(error != null) ? (error + ',' + key) : (error = key);
							}
							error_snackbar.innerHTML = "Please provide valid value.";
							showSnackbar("error-snackbar");

							hideLoader();
						}

						var payIdAlt = $("#payId").val();

						if(payIdAlt == '') {
							payIdAlt = 'ALL';
						}							
	
						if (data.responseCode == '342') {
							success_snackbar.innerHTML = data.responseMsg;
							showSnackbar("success-snackbar");

							clearRules();
							fetchFraudRuleList(payIdAlt);
							$.fancybox.close();
							closeFancybox();

							hideLoader();
						} else if (data.responseCode == '340') {
							error_snackbar.innerHTML = data.responseMsg;

							showSnackbar("error-snackbar");

							hideLoader();
							return false;
						} else {
							if(data.responseMsg !== null) {
								error_snackbar.innerHTML = data.responseMsg;
								showSnackbar("error-snackbar");

								hideLoader();
								return false;
							} else {
								error_snackbar.innerHTML = "Try Again, Something went wrong!";
								showSnackbar("error-snackbar");

								hideLoader();
								return false;
							}
						}						
					}
				},
				error: function(data) {
					error_snackbar.innerHTML = "Try Again, Soemthing went wrong!";
					showSnackbar("error-snackbar");
					
					hideLoader();
				}
			});
		} else {
			payId = fraudFieldValidate(that, 'payId', fraudType);
			$.ajax({
				url: 'addFraudRule',
				type: 'post',
				data: {
					whiteListIpAddress			:	whiteListIpAddress,
					ipAddress					:	ipAddress,
					token						:	token,
					payId						:	payId,
					issuerCountry				:	issuerCountry,
					email						:	email,
					domainName					:	domainName,
					negativeBin					:	negativeBin,
					dateActiveFrom				:	dateActiveFrom,
					dateActiveTo				:	dateActiveTo,
					startTime					:	startTime,
					endTime						:	endTime,
					repeatDays					:	repeatDays,
					alwaysOnFlag				:	flag,
					negativeCard				:	negativeCard,
					currency					:	currency,
					minTransactionAmount		:	minTransactionAmount,
					maxTransactionAmount		:	maxTransactionAmount,
					userCountry					:	userCountry,
					fraudType					:	fraudType,
					perCardTransactionAllowed	:	perCardTransactionAllowed,
					paymentType					:	paymentType,
					paymentRegion				:	$paymentRegion,
					timePeriod					:	timePeriod,
					noOfTransactionAllowed		:	noOfTransactionAllowed,
					amountAllowed				:	amountAllowed,
					minutesTxnLimit				:	minutesTxnLimit,
					paymentTypeArray 			:   paymentTypeArray.slice(5, paymentTypeArray.length),
					paymentRegionArray			:   paymentRegionArray,
					vpa							:	_vpa,
					vpaTotalTransactionAllowed	:	_vpaTxnAllowed
				},
				success: function (data) {
					var result = data;
					console.log(result);
					if (result != null) {
						var errorFieldMap = data["Invalid request"];
						if (errorFieldMap != null && errorFieldMap !== undefined) {
							var error;
							for (key in errorFieldMap) {
								(error != null) ? (error + ',' + key) : (error = key);
							}
							error_snackbar.innerHTML = "Please provide valid value";
							// error_snackbar = error;
							showSnackbar("error-snackbar");
						} else if (data.responseCode == '342') {
							success_snackbar.innerHTML = data.responseMsg;
							showSnackbar("success-snackbar");

							clearRules();
							fetchFraudRuleList($("#payId").val());
							$.fancybox.close();
							closeFancybox();
						} else if (data.responseCode == '340') {								
							error_snackbar.innerHTML = data.responseMsg;
							showSnackbar("error-snackbar");
							hideLoader();
							$.fancybox.close();
							closeFancybox();
							return false;
						} else {
							if(data.responseMsg !== null) {
								error_snackbar.innerHTML = data.responseMsg;
								showSnackbar("error-snackbar");
								hideLoader();
								$.fancybox.close();
								closeFancybox();
								return false;
							} else {
								error_snackbar.innerHTML = "Try Again, Something went wrong!";
								showSnackbar("error-snackbar");
								hideLoader();
								$.fancybox.close();
								closeFancybox();
								return false;
							}
						}						
					}

					hideLoader();
				},
				error: function (data) {
					error_snackbar.innerHTML = "Try Again, Soemthing went wrong!";
					showSnackbar("error-snackbar");

					setInterval(function() {
						$("body").addClass('loader--inactive');
					}, 1000);
				}
			});
		}
	}
}

function isNumberKey(evt) {
	var charCode = (evt.which) ? evt.which : event.keyCode;
	if (charCode > 31 && (charCode < 48 || charCode > 57))
		return false;
	return true;
}

var closeFancybox = function() {
	var parentDiv = $(".modal-box-new-rule"); // Get popup
	parentDiv.removeClass("edit-mode--active"); // Remove active class from popup
	parentDiv.find('[data-role]').tagsinput('removeAll'); // remove all tagsinput

	$(parentDiv).find(".custom-control-input").prop('checked', false); // remove checked from checkbox

	$(parentDiv).find(".new-rule-detail-box").removeClass('new-rule-detail-box--inactive'); // remove inactive class | show detail box
	$(parentDiv).find('.error-msg').removeClass('text-success text-danger').html(''); // blank error div and remove class

	$(parentDiv).find("#currency").prop("disabled", false); // disable #currency selectbox
	$(parentDiv).find("select.paymentRegion-selectbox").prop("disabled", false);
	$(parentDiv).find(".selectpicker").selectpicker('deselectAll'); // deselect all from selectpicker
	$(parentDiv).find("#currency").selectpicker("refresh"); // refresh selectpicker
	$(parentDiv).find("select.paymentRegion-selectbox").selectpicker("refresh");

	$(parentDiv).find(".form-control").val('');
	$(parentDiv).find("[data-input='cardIntialDigits']").attr('readonly', false);
	$(parentDiv).find("[data-input='cardLastDigits']").attr('readonly', false);
	$(parentDiv).find("[data-input='negativeCard']").val('');
}

var resetCountry = function(parentId, tableId) {
	$("#" + tableId + " tbody tr").each(function() {
		var _text = $(this).find("td:nth-child(2) input").val();

		$(parentId + " .selectpicker").find("[value='"+ _text +"']").remove();
	});
	$(parentId + " .selectpicker").selectpicker("refresh");
}

function fourDigitSpace(e) {
	var field = e.target,
		position = field.selectionEnd,
		length = field.value.length;

	field.value = field.value.replace(/[^\dA-Z]/g, '').replace(/(.{4})/g, '$1 ').trim();
	field.selectionEnd = position += ((field.value.charAt(position - 1) === ' ' && field.value.charAt(length - 1) === ' ') ? 1 : 0);
}

$(document).ready(function () {
	// $(".cardNumber").on("keyup", function(e) {
	// 	var charCode = (e.which) ? e.which : e.keyCode;
	// 	if (charCode > 31 && (charCode < 48 || charCode > 57) && (charCode < 96 || charCode > 105) && charCode !== 46) {
	// 		return false;
	// 	} else {			
	// 		var parentDiv = $(this).closest('.modal-box-new-rule');
	// 		var element = parentDiv.find("input[data-input='negativeCard']");
			
	// 		var initCard = parentDiv.find("input[data-input='cardIntialDigits']");
	// 		var initCardDigit = initCard.val();

	// 		var lastCard = parentDiv.find("input[data-input='cardLastDigits']");
	// 		var lastCardDigit = lastCard.val();
	
	// 		if(initCardDigit.length == 6 && lastCardDigit.length == 4) {
	// 			var inputValue = initCardDigit + "******" + lastCardDigit;
	// 			element.val(inputValue);
	// 		} else {
	// 			element.val('');
	// 		}

	// 		if(initCardDigit.length < 6 && $(this).attr("data-input") == "cardIntialDigits") {
	// 			parentDiv.find("[data-error='validate-cardIntialDigits']").text("Please enter initial 6 digits of card.").addClass("text-danger").removeClass("text-success");
	// 		} else if(initCardDigit.length == 6 && $(this).attr("data-input") == "cardIntialDigits") {
	// 			parentDiv.find("[data-error='validate-cardIntialDigits']").text("Valid card no.").addClass("text-success").removeClass("text-danger");
	// 		}

	// 		if(lastCardDigit.length < 4 && $(this).attr("data-input") == "cardLastDigits") {
	// 			parentDiv.find("[data-error='validate-cardLastDigits']").text("Please enter last 4 digits of card.").addClass("text-danger").removeClass("text-success");
	// 		} else if(lastCardDigit.length == 4 && $(this).attr("data-input") == "cardLastDigits") {
	// 			parentDiv.find("[data-error='validate-cardLastDigits']").text("Valid card no.").addClass("text-success").removeClass("text-danger");
	// 		}
	// 	}
	// });


	$("select.paymentType-input").on("change", function() {
		var _val = $(this).val(),
			dataId = $(this).attr("data-id");
		
		if(_val !== "DC" && _val !== "CC") {
			$("#" + dataId).selectpicker("val", "DOMESTIC");

			$('#' + dataId).prop('disabled', true);
  			$('#' + dataId).selectpicker('refresh');
		} else {
			$('#' + dataId).prop('disabled', false);
  			$('#' + dataId).selectpicker('refresh');
		}
	});

	$("#tagipAddress input, #tagwhiteListIpAddress input").on("keypress", function(e) {
		var charCode = (e.which) ? e.which : e.keyCode;
		if (charCode != 46 && charCode > 31 && (charCode < 48 || charCode > 57)) {
			return false;
		}

        return true;
	});

	//by default fraud rules for ALL MERCHANTS will be displayed
	fetchFraudRuleList('ALL');

	// GET FRAUD RULES BY SELECTING MERCHANT
	$('#payId').change(function(e) {
		$("body").removeClass("loader--inactive");
		var that = $(this);
		setTimeout(function() {
			clearRules();
	
			if(that.val() == '') {
				fetchFraudRuleList('ALL');
			} else {
				fetchFraudRuleList(that.val());
			}		
		}, 1000);
	});

	var addFlagValue = function(parentId) {
		var inputCheckbox = $(parentId).find(".custom-control-input");
		var inputCheckboxId = inputCheckbox.attr("id");
		var inputCheckboxValue = inputCheckbox.is(":checked");
		$(parentId).find("[data-checkbox='"+ inputCheckboxId +"']").val(inputCheckboxValue);
	}

	$(".add-new-rule").on("click", function(e) {
        var payId = $("#payId").val();
        if(payId !== '') {
			var dataSrc = $(this).attr("data-src"); // Pop-up id
			var tableId = $(this).closest(".tab-pane").find("table").attr("id");
			var tagInput = $(dataSrc).find('.bootstrap-tagsinput'); // taginput div

			if(tagInput.length !== 0) {
				var tagInputId = tagInput.attr("id").slice(3); // taginput div id
				
				tagInput.attr("data-name", tagInputId); // set data-name to taginput div

				var inputMaxLength = $(dataSrc).find("#" + tagInputId).attr("maxlength"); // get maxlength of input
				var inputMinLength = $(dataSrc).find("#" + tagInputId).attr("minlength"); // get minlength of input

				if(inputMinLength !== undefined) {
					tagInput.find('input').attr("minlength", inputMinLength);
				}

				if(inputMaxLength !== undefined) {
					tagInput.find("input").attr("maxlength", inputMaxLength);
				}
			}

			addFlagValue(dataSrc); // add flag value to input box
			timePickerControl(); // update time

			setDate(dataSrc);

			if(dataSrc == "#BLOCK_CARD_ISSUER_COUNTRY-rule" || dataSrc == "#BLOCK_USER_COUNTRY-rule") {
				resetCountry(dataSrc, tableId); // reset country
			}
			
			return true;
        } else {
			error_snackbar.innerHTML = "Please select merchant";
			showSnackbar("error-snackbar");
			return false;
		}        
	});
	

	// SHOW HIDE DIVS
	$(".custom-control-input").on("change", function(e) {
		var parentDiv = $(this).closest(".modal-box-new-rule");
		var checkedValue = $(this).is(":checked");
		if(checkedValue) {
			parentDiv.find(".new-rule-detail-box").addClass('new-rule-detail-box--inactive');
		} else {
			parentDiv.find(".new-rule-detail-box").removeClass('new-rule-detail-box--inactive');
		}
		var checkboxId = $(this).attr('id');
		parentDiv.find("[data-checkbox='"+ checkboxId +"']").val(checkedValue);
	});

	$("body").on("click", ".edit-rule", function(e) {
		var $table = $(this).closest('table').attr('id'); // table id
		var parentDiv = $(this).closest('.merchant-config-row'); // // parent row
		var loopLength = parentDiv.children().length; // count td in row
		var dataSrc = $(this).attr('data-src'); // popup id
		var dataFlag = parentDiv.attr('data-flag'); // parent row attr

		$(dataSrc).addClass("edit-mode--active"); // add class to popup

		// SHOW HIDE DIV
		if(dataFlag == 'true') {
			$(dataSrc).find(".custom-control-input").prop('checked', true);
			$(dataSrc).find(".new-rule-detail-box").addClass('new-rule-detail-box--inactive');
		}

		addFlagValue(dataSrc);

		// ROW ID
		var rowId = parentDiv.attr('id');
		rowId = rowId.slice(6);
		$(dataSrc).find("[data-input='rowId']").val(rowId);
		
		// ADD DATA TO FORM
		for(var i = 0; i < loopLength - 2; i++) {
			var dataId = parentDiv.children().eq(i).find('input').attr('data-id');
			var dataIdVal = parentDiv.children().eq(i).find('input').val();
			var inputField = $(dataSrc).find("[data-input='"+ dataId +"']");

			if(inputField.is('input')) {
				if(dataIdVal !== "NA") {
					inputField.val(dataIdVal);
				}
				if(inputField.attr('data-role') == 'tagsinput') {
					inputField.tagsinput('add', dataIdVal);
				}
				// if(inputField.attr('id') == 'ipAddress') {
				// 	inputField.attr('readable', true);
				// }
			} else if(inputField.is('select')) {
				dataIdVal = dataIdVal.split(',');
				
				inputField.selectpicker('val', dataIdVal);
				inputField.selectpicker('refresh');
			}
			
			// if(inputField.is('div')) {
			// 	if(dataId == 'negativeCard') {
			// 		var initDigit = dataIdVal.slice(0, 6);
			// 		var lastDigit = dataIdVal.slice(dataIdVal.length - 4);
			// 	}

			// 	inputField.find("[data-input='cardIntialDigits']").val(initDigit).attr('readonly', true);
			// 	inputField.find("[data-input='cardLastDigits']").val(lastDigit).attr('readonly', true);
			// }
		}

		// DISABLE FIELD
		if($table == "txnAmountListBody") {
			// $(dataSrc).find("#currency").prop("disabled", true);
			$(dataSrc).find("#currency").selectpicker("refresh");
		}

		timePickerControl();

		setDate(dataSrc);
	});

	$(".fancybox-close-btn").on("click", function(e) {
		closeFancybox();
	});

	$(document).keydown(function(e) {
		// ESCAPE key pressed
		if (e.keyCode == 27) {
			closeFancybox();
			$.fancybox.close();
		}
	});	

	// TIME PICKER
	var timePickerControl = function() {
		var _today = new Date();

		$('.startTime').datetimepicker({
			format: 'HH:mm:ss', // HH = 00 01 ... 22 23
			showClose: true,
			minDate: _today,
			ignoreReadonly: true
		});

		$('.endTime').datetimepicker({
			format: 'HH:mm:ss',	// HH = 00 01 ... 22 23
			showClose: true,
			minDate: false,
			ignoreReadonly: true
		});
	}

	timePickerControl();
});
