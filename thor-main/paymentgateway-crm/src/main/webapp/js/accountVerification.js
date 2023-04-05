 // merchant function
 var _id = document.getElementById.bind(document),
 	_querySelector = document.querySelector.bind(document);

 function removeError(_this) {
	 _this.closest(".col-md-4").classList.remove("redLine");
 }

 var removeErrorAlt = function(that) {
	 if(that.value !== "") {
		 var _elementId = that.getAttribute("id");

		 document.querySelector('[data-error="'+ _elementId +'"]').innerHTML = "";
	 }
 }

 function format ( d ) {	
    d.new = function() {		
        if (d.status == "Timeout" || d.status == "Processing") {
            return "<button class='lpay_button lpay_button-md lpay_button-secondary statusEnquiry'>Status</button>";
        } else {
            return '';
        }
    }

    var _obj = {
        "merchantPayId" : "Pay ID",
        "subMerchant" : "Sub Merchant Name",
        "bankAccountNumber" : "Bank Account Number",
        "bankIFSC" : "IFSC Code",
		"phoneNo" : "Mobile",
		"bankAccountNameReq" : "Bene Request Name",
		"responseMsg":"Bank Response Msg"
    }

    _new = "<div class='main-div'>";
    
    for(key in _obj){
        if(_obj[key].hasOwnProperty("className")){
            var _getKey = Object.keys(_obj[key]);
            _new += '<div class="inner-div '+_obj[key]["className"]+'">'+
                    '<span>'+_getKey[0]+'</span>'+
                    '<span>'+d[_getKey[0]]+'</span>'+
                '</div>'
        }else{
            _new += '<div class="inner-div">'+
                '<span>'+_obj[key]+'</span>'+
                '<span>'+d[key]+'</span>'+
            '</div>'
        }
    }

    _new += '<div class="inner-div" style="width: 100%;text-align: center">'+
    '<span></span>'+
    '<span>'+d.new()+'</span>'+
'</div>';



    _new += "</div>";

    return _new;
   
}

var displayLabelErrorMsg = function(obj) {
    var elementLabel = _querySelector('[data-error="'+ obj.elementId +'"]');

    elementLabel.innerHTML = obj.errorMessage;

    _id(obj.elementId).focus();
}

var removeLabelErrorMsg = function(that) {
    var elementId = that.getAttribute("id");

	_querySelector('[data-error="'+ elementId +'"]').innerHTML = "";
}

var validateMerchant = {
	isSubMerchantHidden : true,
	payId : null,
	validate: function() {
		this.isSubMerchantHidden = _querySelector('[data-target="verify-subMerchant"]').classList.contains("d-none");
		this.payId = $("#verify-merchant").val();

		if(!this.isSubMerchantHidden) {
			this.payId = $("#verify-subMerchant").val();
	
			if(this.payId == "") {
				$('[data-error="verify-subMerchant"]').html("Please select any Sub Merchant");
	
				return false;
			}
		} else {
			if(this.payId == "") {
				$('[data-error="verify-merchant"]').html("Please select any Merchant");
	
				return false;
			}
		}
	
		return true;
	}
}

var validateMobileEmailField = function(that) {
    var currentId = that.getAttribute("id");

    if(currentId == "verify-phone" && that.value !== "") {
        if(_id("verify-email").value == "") {
            removeLabelErrorMsg(_id("verify-email"));
        }
    } else if(currentId == "verify-email" && that.value !== "") {
        if(_id("verify-phone").value == "") {
            removeLabelErrorMsg(_id("verify-phone"));
        }
    }
}

var validateEmailInput = function(that) {
    var emailRegex = /^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$/,
        _value = that.value.trim();

    if(_value !== "") {
        removeLabelErrorMsg(that);
    }
}

var validateEmail = function(that) {
    var emailRegex = /^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$/,
        _value = that.value.trim();

    if(_value !== "") {
        if(!_value.match(emailRegex)) {
            displayLabelErrorMsg({
                errorMessage: "Please enter valid email id.",
                elementId: that.getAttribute("id")
            });
            return false;
        } else {
            removeLabelErrorMsg(that);
            return true
        }
    } else {
        displayLabelErrorMsg({
            errorMessage: "Please enter email id.",
            elementId: that.getAttribute("id")
        });
        return false;
    }
}

var validateMobileNumber = function(that) {
    if(that.value !== "") {
        if(that.value.length !== 10) {
            displayLabelErrorMsg({
                errorMessage: "Please enter valid mobile number.",
                elementId: that.getAttribute("id")
            });
            return false;
        }
    } else {
        displayLabelErrorMsg({
            errorMessage: "Please enter mobile number.",
            elementId: that.getAttribute("id")
        });
        return false;
    }

    return true;
}

var validateMobileEmail = function(mobile, email) {
	if(mobile.value == "" && email.value == "") {
        alert("Please fill any one email or phone number.");

        validateMobileNumber(mobile);
        validateEmail(email);

        mobile.focus();

        return false;
    } else if(mobile.value !== "" && email.value !== "") {
        if(validateMobileNumber(mobile) && validateEmail(email)) {
            return true;
        }
        return false;
    } else if(mobile.value !== "" && email.value == "") {
        return validateMobileNumber(mobile);
    } else if(mobile.value == "" && email.value !== "") {
        return validateEmail(email);
    }

    return true;
}


$(document).ready(function (e) {
	$("#btn-verify-link").on("click", function(e) {
		e.preventDefault();
		
		var flag = validateMerchant.validate();
		flag = flag && validateMobileEmail(_id("verify-phone"), _id("verify-email"));
		
		if(flag) {
			$("body").removeClass("loader--inactive");

			$.ajax({
				method: "POST",
				url: "sendVerificationLink",
				data: {
					mobileNo: $("#verify-phone").val(),
					emailId: $("#verify-email").val(),
					payId: validateMerchant.payId,
					token: $("[name=token]").val(),
					"struts.token.name": "token"
				},
				success: function(data) {
					alert(data.responseMessage);

					if(data.responseMessage == "Link has been sent successfully") {
						$("#verify-phone").val("");
						$("#verify-email").val("");
						$('[data-target="verify-subMerchant"]').addClass("d-none");
						$("#verify-merchant").selectpicker("deselectAll");
						$("#verify-subMerchant").selectpicker("deselectAll");
					}
	
					setTimeout(function() {
						$("body").addClass("loader--inactive");
					}, 1000);
				},
				error: function() {
					alert("Try again, Something went wrong!");
	
					setTimeout(function() {
						$("body").addClass("loader--inactive");
					}, 1000);
				}
			});
		}
	});
	function getSubMerchant(_this, _url, _firstKey, _object) {
		var _merchant = _this.target.value;
		var _merchantId = _this.target.id;
		var _key = _firstKey;
		var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
		var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
		if (_merchant != "") {
			document.querySelector("body").classList.remove("loader--inactive");
			var data = new FormData();
			data.append('payId', _merchant);
			var _xhr = new XMLHttpRequest();
			_xhr.open('POST', _url, true);
			_xhr.onload = function () {
				if (_xhr.status === 200) {
					var obj = JSON.parse(this.responseText);
					var _option = "";
					if (_object.isSuperMerchant == true) {
						if (obj.superMerchant == true) {
							document.querySelector("#"+_subMerchantAttr).setAttribute("data-id", "lpay-input");
							document.querySelector("#" + _subMerchantAttr).innerHTML = "";
							if (_key !== null) {
								_option += document.querySelector("#" + _subMerchantAttr).innerHTML = "<option value=''>Select Sub Merchant</option>";
							} else {
								_option += document.querySelector("#" + _subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
							}
							for (var i = 0; i < obj.subMerchantList.length; i++) {
								_option += document.querySelector("#" + _subMerchantAttr).innerHTML += "<option value=" + obj.subMerchantList[i]["payId"] + ">" + obj.subMerchantList[i]["businessName"] + "</option>";
							}
							document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.remove("d-none");
							if (_key !== null) {
								document.querySelector("#" + _subMerchantAttr + " option[value='']").selected = true;
							} else {
								document.querySelector("#" + _subMerchantAttr + " option[value='ALL']").selected = true;
							}
							$("#" + _subMerchantAttr).selectpicker();
							$("#" + _subMerchantAttr).selectpicker('refresh');
						} else {
							document.querySelector("#"+_subMerchantAttr).removeAttribute("data-id");
							document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.add("d-none");
							document.querySelector("#" + _subMerchantAttr).value = "";
						}
					}
					if (_object.subUser == true) {
						if (obj.subUserList.length > 0) {
							_option += document.querySelector("#" + _subUserAttr).innerHTML = "<option value=''>Select Sub-User</option>";
							for (var i = 0; i < obj.subUserList.length; i++) {
								_option += document.querySelector("#" + _subUserAttr).innerHTML += "<option value=" + obj.subUserList[i]["emailId"] + ">" + obj.subUserList[i]["businessName"] + "</option>";
							}
							document.querySelector("[data-target=" + _subUserAttr + "]").classList.remove("d-none");
							document.querySelector("#" + _subUserAttr + " option[value='']").selected = true;
							$("#" + _subUserAttr).selectpicker();
							$("#" + _subUserAttr).selectpicker('refresh');
						} else {
							document.querySelector("[data-target=" + _subUserAttr + "]").classList.add("d-none");
							document.querySelector("#" + _subUserAttr).value = "";
						}
					}
					if (_object.glocal == true) {
						if (obj.glocalFlag == true) {
							document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
							$("[data-id=deliveryStatus] select").selectpicker('val', 'All');
						} else {
							document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
						}
					}

					if (_object.retailMerchantFlag == true) {
						$("#retailMerchantFlag").val(data.retailMerchantFlag);
						document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
					}
				}
			}
			_xhr.send(data);
			setTimeout(function (e) {
				document.querySelector("body").classList.add("loader--inactive");
			}, 1000);
		} else {
			document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.add("d-none");
			document.querySelector("#" + _subMerchantAttr).value = "";
			document.querySelector("[data-target=" + _subUserAttr + "]").classList.add("d-none");
			document.querySelector("#" + _subUserAttr).value = "";

		}
	}

	if(document.querySelector("#merchant") != null){
		document.querySelector("#merchant").addEventListener("change", function (e) {
			getSubMerchant(e, "getSubMerchantList", 'select key', {
				isSuperMerchant: true,				
			});
		});
	}

	if(document.querySelector("#merchantReportPayId") != null) {
		document.querySelector("#merchantReportPayId").addEventListener("change", function (e) {
			getSubMerchant(e, "getSubMerchantList", null, {
				isSuperMerchant: true,				
			});
		});
	}

	if(document.querySelector("#bulkAccountMerchant") != null){
		document.querySelector("#bulkAccountMerchant").addEventListener("change", function (e) {
			getSubMerchant(e, "getSubMerchantList", "select key", {
				isSuperMerchant: true,				
			});
		});
	}

	if(document.querySelector("#verify-merchant") != null) {
		document.querySelector("#verify-merchant").addEventListener("change", function (e) {
			getSubMerchant(e, "getSubMerchantList", 'select key', {
				isSuperMerchant: true,				
			});
		});
	}
});

function hideColumn() {
	var _userType = $("#userType").val();
	var _userLogin = $("#setSuperMerchant").val();
	var _isSuperMerchant = $("#isSuperMerchant").val();
	

	var _table = $("#datatable").DataTable();
	if (_userLogin == "true") {

		_table.columns(1).visible(true);

	}
	if (_userLogin == "false") {

		_table.columns(1).visible(false);

	}

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
function onlyDigit(event) {
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

$(document).ready(function (e) {

	if ($("#rowCount").val() != "" && $("#rowCount").val() != "-1" && $("#wrongCsv").val() == "0") {
		$(".lpay-nav-item").removeClass("active");
		$("[data-id='bulkAccount']").closest("li").addClass("active");
		$("[data-target='impsTransfer']").addClass("d-none");
		$("[data-target='bulkAccount']").removeClass("d-none");
		$("[data-id='responseTable']").removeClass("d-none");
	}

	if ($("#wrongCsv").val() != "0" && $("#wrongCsv").val() != "") {

		$(".lpay-nav-item").removeClass("active");
		$("[data-id='bulkAccount']").closest("li").addClass("active");
		$("[data-target='impsTransfer']").addClass("d-none");
		$("[data-target='bulkAccount']").removeClass("d-none");
		$("[data-id='responseFileFormat']").removeClass("d-none");
	}

	if ($("#fileIsEmpty").val() != "0" && $("#fileIsEmpty").val() != "") {
		$(".lpay-nav-item").removeClass("active");
		$("[data-id='bulkAccount']").closest("li").addClass("active");
		$("[data-target='impsTransfer']").addClass("d-none");
		$("[data-target='bulkAccount']").removeClass("d-none");
		$("[data-id='responseFileEmpty']").removeClass("d-none");
	}

	setTimeout(function (e) {
		$(".selectpicker").selectpicker();
		$(".selectpicker").selectpicker('refresh');
	}, 1000);
	var _userType = $("#userType").val();

	// if($("#userType").val() != "SUBADMIN" && $("#userType").val() != "ADMIN"){
	//     $("[data-id='impsTransfer']").closest("li").removeClass("active").addClass("d-none");
	//     $("[data-target='impsTransfer']").addClass("d-none");
	//     $("[data-target='report']").removeClass("d-none");
	//     $("[data-id='report']").closest("li").addClass("active");
	// }

	$(".downloadData").on("click", function (e) {
        if ($("[data-id=reportMerchant]").val() == "") {
            $("#reportMerchant").val("All");
        } else {
            $("#reportMerchant").val($("[data-id=reportMerchant]").val());
        }
        $("#reportStatus").val($("#status").val());
        $("#downloadSearchAccountNumber").val($("#searchAccountNumber").val());
        $("#reportDateFrom").val($("#dateFrom").val());
        $("#reportDateTo").val($("#dateTo").val());
        if($("#reportMerchant").val() == "All"){
            $("#downLoadReportSubMerchant").val("All")
        }else{
            $("#downLoadReportSubMerchant").val($("#reportSubMerchant").val());
        }
        $("#downloadReport").submit();
    })

	// tab creation 
	function tabChange(_selector){
        var _getClickTab = _selector;
        $(".lpay-nav-item").removeClass("active");
        $("[data-id='"+_getClickTab+"']").closest("li").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target='"+_getClickTab+"']").removeClass("d-none");
    }

	var _allTab = document.querySelectorAll(".lpay-nav-item");
    _allTab.forEach(function(index, array, element){
        _allTab[array].addEventListener('click', function(e){
            e.preventDefault();
            var _getAttr = e.target.getAttribute("data-id");
            tabChange(_getAttr);
        })
    })

	var _isReseller = document.querySelector("#resellerTrue");

	if(_isReseller == null){
		tabChange("impsTransfer");
	}else{
		tabChange("report");
	}



	var today = new Date();
	$('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
	$(".datepick").datepicker({
		prevText: "click for previous months",
		nextText: "click for next months",
		showOtherMonths: true,
		dateFormat: 'dd-mm-yy',
		selectOtherMonths: false,
		maxDate: new Date(),
		changeMonth: true,
		changeYear: true
	});

	$(".viewData").on("click", function (e) {
		$("body").removeClass("loader--inactive");
		handleChange();
		setTimeout(function (e) {
			$("body").addClass("loader--inactive");
		}, 1500);
	})

	handleChange();

	function handleChange() {
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

			dom: 'BTftlpi',
			buttons: ['csv', 'print', 'pdf'],
			language: {
				search: "",
				searchPlaceholder: "Search records"
			},

			"destroy": true,
			"ajax": {
				"type": "post",
				"url": "beneVerificationReportData",
				"data": function (d) {
					return generatePostData(d);
				}
			},
			"initComplete": function (settings, json) {
				// $("#setSuperMerchant").val(json.flag);
				//  hideColumn();
			},

			"destroy": true,
			"order": [[5, 'desc']],

			"sAjaxDataProp": "impsDataList",
			"aoColumns": [
				{
					"mDataProp": "merchant",
					"className": "my_class"
				},
				{ "mData": "txnId", "className": "txnId" },
				{ "mData": "orderId", "className": "orderId" },
				{ "mData": "bankAccountName" },
				{ "mData": "date", "className": "date" },
				{
					"mData": null,
					"mRender": function (row) {
						if (row.status == "Timeout") {
							return "Pending"
						} else if (row.status == "Captured") {
							return "Verified"
						} else {
							return "Failed"
						}
					}
				}
			]
		});

	}

	$("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        _check = _currentTable;
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

	// variable sent to backend function
	function generatePostData(d) {
		var obj = {
			merchantPayId: $("#merchantReportPayId").val(),
			status: $("#status").val(),
			beneAccountNumber: $("#searchAccountNumber").val(),
			subMerchantId: $("#reportSubMerchant").val(),
			dateFrom: $("#dateFrom").val(),
			dateTo: $("#dateTo").val(),
			draw: d.draw,
			length: d.length,
			start: d.start,
			token: $("[name=token]").val(),
			"struts.token.name": "token",
		};
		return obj;
	}

	// reInitiate
	$("body").on("click", ".reInitiate", function (e) {
		var _tr = $(this).closest("tr");
		$("body").removeClass("loader--inactive");
		var _data = {
			"txnId": _tr.find(".txnId").text(),
			"orderId": _tr.find(".orderid").text(),
			"status": _tr.find(".status").text()
		}
		$.ajax({
			type: "post",
			url: "impsReinitiateTransaction",
			data: _data,
			success: function (data) {
				if (data.response == "success") {
					handleChange();
					$(".lpay_popup-innerbox").attr("data-status", "success");
					$(".responseMsg").text(data.responseMsg);
					$(".lpay_popup").fadeIn();
					$("body").addClass("loader--inactive");
				} else {
					handleChange();
					$(".lpay_popup-innerbox").attr("data-status", "error");
					$(".responseMsg").text(data.responseMsg);
					$(".lpay_popup").fadeIn();
					$("body").addClass("loader--inactive");
				}
			}
		})
	});

	$(".confirmButton").on("click", function (e) {
		$("body").removeClass("loader--inactive");
		handleChange();
		$("[data-id=submerchant]").addClass("d-none");
		$('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
		$(".blankInput").val("");
		$(".selectpicker").selectpicker("refresh");
		if($("#userType").val() != "MERCHANT"){
			$("[data-target='subMerchant']").addClass("d-none");
			$("[data-target='subUser']").addClass("d-none");
		}
		$(".lpay_popup").fadeOut();
		setTimeout(function (e) {
			$("body").addClass("loader--inactive");
		}, 2000);
	})

	$("body").on("click", ".statusEnquiry", function (e) {
		// var _tr = $(this).closest("tr");
		var _table = new $.fn.dataTable.Api('#datatable');
		// var _txnId = _tr.find(".txnId").text();
		var _tr = $(this).closest("tr").prev("tr");
		var _data = _table.rows(_tr).data();
		
		$("body").removeClass("loader--inactive");
		$.ajax({
			type: "POST",
			url: "getImpsStatus",
			data: {
				"txnId": _data[0]['txnId'],
				"payId" : _data[0]['merchantPayId'],
				"amount" : _data[0]['amount']
			},
			success: function (data) {
				if (data.response == "success") {
					setTimeout(function (e) {
						$(".lpay_popup-innerbox").attr("data-status", "success");
						$(".lpay_popup").fadeIn();
						$(".responseMsg").text(data.responseMsg);
						$("body").addClass("loader--inactive");
					}, 2000);
				} else {
					$(".lpay_popup-innerbox").attr("data-status", "error");
					$(".responseMsg").text(data.responseMsg);
					$(".lpay_popup").fadeIn();
					$("body").addClass("loader--inactive");
				}
			}
		})
	})


	$("#submit").on("click", function (e) {

		var _getClosestActive = e.target.closest(".lpay_tabs_content").getAttribute('data-target');
		var val = true;
		var obj = {};

		var _getAllInput = document.querySelectorAll("[data-target="+_getClosestActive+"] [data-id='lpay-input']");
		_getAllInput.forEach(function (index, element, array) {
				_getAllInput[element].classList.remove("redLine");
				
				if (_getAllInput[element].value == "" && _getAllInput[element].id != "phoneNo" && _getAllInput[element].closest(".d-none") == null) {
					_getAllInput[element].closest(".col-md-4").classList.add("redLine");
					val = false;
				}
				obj[_getAllInput[element].name] = _getAllInput[element].value;
		});

		if (val == true) {
			$("body").removeClass("loader--inactive");
			$.ajax({
				type: "post",
				url: "initiateBeneVerification",
				data: obj,
				success: function (data) {
					if (data.response == "success") {
						setTimeout(function (e) {
							$(".lpay_popup-innerbox").attr("data-status", "success")
							$(".lpay_popup").fadeIn();
							$("#phoneNo").val("");
							$(".responseMsg").text(data.responseMsg);
							$("body").addClass("loader--inactive");
						}, 2000);
					} else {
						setTimeout(function (e) {
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

	$(".lpay_input").on("keyup", function (e) {
		$(".default_error").addClass("d-none");
	})
})

$(document).ready(function () {
	$("#merchantPayId").selectpicker();
	$("#merchantPayId").selectpicker("refresh");
	$(".bulk-invoice").on("change", function (e) {
		var _merchant = $("#merchantPayId").val();
		var _fileChange = $("#upload-input").val();
		var _getRow = $("[data-id=submerchant]").hasClass("d-none");
		var _isSuperMerchant = $("#isSuperMerchant").val();
		var _getSubMerchant = $("#subMerchant").val();
		// triggerValidation(this);
		if (_merchant == "" || _fileChange == "" || (_isSuperMerchant == "Y" && _getSubMerchant == "")) {
			$("#bulkSubmit").attr("disabled", true);
		} else {
			$("#bulkSubmit").attr("disabled", false);
		}
	});

	$("#bulkSubmit").on("click", function () {
		$("body").removeClass("loader--inactive");
	});

	$(".lpay_upload_input").on("change", function (e) {
		var _val = $(this).val();
		var _fileSize = $(this)[0].files[0].size;
		var _tmpName = _val.replace("C:\\fakepath\\", "");
		$("#hideFields").val(_tmpName);
		if (_val != "") {
			$("body").removeClass("loader--inactive");
			$(".default-upload").addClass("d-none");
			$("#placeholder_img").css({ "display": "none" });
			if (_fileSize < 2000000) {
				$(this).closest("label").attr("data-status", "success-status");
				$("#fileName").text(_tmpName);

				$("#bulkUpdateSubmit").attr("disabled", false);
				setTimeout(function (e) {
					$("body").addClass("loader--inactive");
				}, 500);
			} else {
				$(this).closest("label").attr("data-status", "error-status");
				$("#bulkUpdateSubmit").attr("disabled", true);
				setTimeout(function (e) {
					$("body").addClass("loader--inactive");
				}, 500);
			}
		}
	});



	$('#example').DataTable({
		dom: 'B',
		buttons: [
			{
				extend: 'csv',
				text: 'Failed Data CSV',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
	});
	$("#merchantPayId").on('change', function () {
		getSubMerchant();
	});

	$('#download-format').DataTable({
		dom: 'B',
		buttons: [
			{
				extend: 'csv',
				text: 'Download CSV Format',
				className: 'lpay_button lpay_button-md lpay_button-primary',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
	});
});