
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Reseller Charges Update</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<link rel="stylesheet" type="text/css" href="../css/popup.css" />
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />
<link rel="stylesheet" href="../css/paymentOptions.css">
<link rel="stylesheet" href="../css/chargingPlatform.css">
<link rel="stylesheet" href="../css/common-style.css">

<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>

<style>

	#allEditDiv[disabled="disabled"]{
		pointer-events: none;
	}

</style>

<script type="text/javascript">
$(document).ready(function() {
	// Reseller ID
	/* function getReseller(){
        var _reseller = $("#resellerList").val();
        if(_reseller != ""){
            $.ajax({
                    type: "post",
                    url: "getMerchantListByReseller",
                    data: {
                        "resellerId": _reseller
                    },
                    success: function(data){
                        console.log("this is inside function");
                        console.log(data);
                        if(data.listMerchant.length > 0){
                            $("#merchant").html("");
                            var _option = $("#merchant").append("<option value='ALL'>select Merchant</option>");
                            for(var i = 0; i < data.listMerchant.length; i++){
                                _option += $("#merchant").append("<option value="+data.listMerchant[i]["payId"]+">"+data.listMerchant[i]["businessName"]+"</option>")
                            }
                            $("#merchant").selectpicker("refresh");
                            $("#merchant").selectpicker();
                            $("boby").addClass("loader--inactive");
                        }else{
                            var _option = $("#merchant").append("<option value=''>No merchant exist</option>");
                            $("#merchant").selectpicker("refresh");
                            $("#merchant").selectpicker();
                            $("boby").addClass("loader--inactive");
                        }
                    }
            })
        }
    }
 
	getReseller(); */

	// payment type action
	$("#payment-type").on("change", function(e){
		var _paymentType = $(this).val();
		var _merchant = $("#merchant").val();
		$.ajax({
			type: "post",
			url: "checkPaymentTypeMapedAction",
			data: {
				"merchantList": _merchant,"paymentType": _paymentType
			},
			success: function(data){
				if(data.response != null){
					$("[data-id=responseMsg]").removeClass("d-none");
					$("#responseMsg").text(data.response);
					$("#Download").attr("disabled", true);
					
				}else{
					$("[data-id=responseMsg]").addClass("d-none");
					$("#Download").attr("disabled", false);
				}
			},
			error: function(data){
			}
		})
	})
	
	// catd holder type function
	$("#payment-region").on("change", function(e){
		var _this = $(this).val();
		if(_this == "DOMESTIC"){
			$("[data-id=cardHolderType]").removeClass("d-none");
		}else{
			$("[data-id=cardHolderType]").addClass("d-none");
		}
	})

	$("[name=selectInner]").on("change", function(e){
		$("[name=selectInner]").each(function(e){
			if($(this).is(":checked")){
				$("#allEditDiv").attr("disabled", true);
				return false;
			}else{
				$("#allEditDiv").attr("disabled", false);
			}
		})
	})
	
	$("#resellerList").on("change", function(e){
        var _this = $(this).val();
        if(_this != ""){
            $("boby").removeClass("loader--inactive");
            $.ajax({
                type: "post",
                url: "getMerchantListByReseller",
                data: {
                    "resellerId": _this
                },
                success: function(data){
                    console.log(data);
					$("#merchant").html("");
                    if(data.listMerchant.length > 0){
                        var _option = $("#merchant").append("<option value='ALL'>Select Merchant</option>");
                        for(var i = 0; i < data.listMerchant.length; i++){
                            _option += $("#merchant").append("<option value="+data.listMerchant[i]["payId"]+">"+data.listMerchant[i]["businessName"]+"</option>")
                        }
                        $("#merchant").selectpicker("refresh");
                        $("#merchant").selectpicker();
                        $("boby").addClass("loader--inactive");
                    }else{
                        var _option = $("#merchant").append("<option value=''>No merchant exist</option>");
                        $("#merchant").selectpicker("refresh");
                        $("#merchant").selectpicker();
                        $("boby").addClass("loader--inactive");
                    }
                }
            })
        }
    })
					var getSlabArray = $("#slabFRm").val();
					var _getChargesFromFrm = $("#chargesFromFrm").val();
					var _getMerchantName = $("#merchantName").val();
					var _getMerchantPayId = $("#merchantListFrm").val();
					var _paymentRegion = $("#paymentRegionFrm").val();
					var _paymentType = $("#paymentTypeFrm").val();
					console.log(_getChargesFromFrm);
					var _getSlab = getSlabArray.split(" ").join("");
					// var newVal = getSlabArray.split(",");
					var _flag = getSlabArray.indexOf(",");
					if(_flag != -1){
						var _slab = _getSlab.split(",");
						$("#slab").selectpicker("refresh");
						$("#slab").selectpicker();
						$("#slab").selectpicker('val', _slab);
						
					} else {
						$("#slab").selectpicker("refresh");
						$("#slab").selectpicker();
						$("#slab").selectpicker('val', _getSlab);
					}

					if(_paymentType != ""){
						
						$("#payment-type").selectpicker("refresh");
						$("#payment-type").selectpicker();
						$("#payment-type").selectpicker('val', _paymentType);

					}

					if(_paymentRegion != ""){
						
						$("#payment-region").selectpicker("refresh");
						$("#payment-region").selectpicker();
						$("#payment-region").selectpicker('val', _paymentRegion);

					}

					if(_getChargesFromFrm != ""){
					$("#chargeFrom").selectpicker("refresh");
					$("#chargeFrom").selectpicker();
					$("#chargeFrom").selectpicker('val', _getChargesFromFrm);
					}

					if(_getMerchantName != ""){
						$("#merchant").html("");
					$("#merchant").append("<option value="+_getMerchantPayId+">"+_getMerchantName+"</option>");
					$("#merchant").selectpicker("refresh");
                        $("#merchant").selectpicker();
					}

					
					

					document.getElementById("editAllSlab").innerText = document
							.getElementById("slabFRm").value;
					document.getElementById("saveBtn").style.display = "none";
					document.getElementById("reload").style.display = "none";

					document.getElementById("allEditDiv").style.display = "none";

					if (document.getElementById("showMerchantFrm").value == "true") {
						document.getElementById("allEditDiv").style.display = "block";
						document.getElementById("reload").style.display = "block";
						document.getElementById("Download").style.display = "none";
						document.getElementById("saveBtn").style.display = "block";

						document.getElementById("payment-type").style = "background-color : #dedede";
						document.getElementById("payment-type").disabled = true;

						document.getElementById("payment-region").style = "background-color : #dedede";
						document.getElementById("payment-region").disabled = true;

						document.getElementById("slab").style = "background-color : #dedede";
						document.getElementById("slab").disabled = true;

						if (document.getElementById("payment-type").value != "CC"
								&& document.getElementById("payment-type").value != "DC") {
							document.getElementById("payment-region").value = "DOMESTIC";
						}
					}

					if (document.getElementById("showSaveButtonFrm").value == "true") {
						document.getElementById("saveBtn").style.display = "block";
						document.getElementById("reload").style.display = "block";
					}

					if (document.getElementById("paymentTypeFrm").value != "") {
						document.getElementById("payment-type").value = document
								.getElementById("paymentTypeFrm").value;
					}

					if (document.getElementById("paymentRegionFrm").value != "") {
						document.getElementById("payment-region").value = document
								.getElementById("paymentRegionFrm").value;
					}

					if (document.getElementById("slabFRm").value != "") {
						document.getElementById("slab").value = document
								.getElementById("slabFRm").value;
					}
				});

var editMode;

function validatePayChange() {
	if (document.getElementById("payment-type").value == "CC" ||
		document.getElementById("payment-type").value == "DC" || document.getElementById("payment-type").value == "NB") {		
		document.getElementById("payment-region").disabled = false;
		$('#payment-region').selectpicker("refresh");
	} else {
		document.getElementById("payment-region").disabled = true;
		$('#payment-region').selectpicker('val', 'DOMESTIC');
	}
}



function checkboxSelect(val) {
	var length = 13;
	if (val.checked) {
		val.closest("tr").classList.add("active-tr");
	} else {
		val.closest("tr").classList.remove("active-tr");
		// }
	}
}

function reloadValues() {
	event.preventDefault();
	var baseUrl = window.location.host;
	window.open('http://' + baseUrl + '/crm/jsp/resellerChargesUpdate',
			'_self');
}

function updateValues(event) {
	event.preventDefault();

	var merchantList = $("#merchant").val();
	var _cardHolderType = $("#cardHoldertype").val();

	

	var resellerList = "";
	var rsellerList = $("#resellerList").val();

	if (rsellerList == null) {

		alert("Please select Reseller(s)");
		event.preventDefault();
		return false;

	} else {

		var rsellerListArray = rsellerList.toString().split(",");
		for (var i = 0; i < rsellerListArray.length; i++) {
			resellerList = resellerList + rsellerListArray[i] + ",";
		}

	}

	var paymentType = document.getElementById("payment-type").value;
	var paymentRegion = document.getElementById("payment-region").value;
	var slab = document.getElementById("slab").value;
	var checkboxArray = document.getElementsByName("selectInner");
	var length = checkboxArray.length;
	var allDetails = "";
	var anySelected = false;

	for (var i = 0; i < length; i++) {

		var details = "";

		if (document.getElementsByName("selectInner")[i].checked) {
			
			anySelected = true;
			var cells = document.getElementsByName("selectInner")[i].parentElement.parentElement.parentElement.cells;

			details = details + (cells[0].innerText) + ",";
			details = details + (cells[1].innerText) + ",";
			details = details + (cells[2].children[0].value) + ",";
			details = details + (cells[3].children[0].value) + ",";
			details = details + (cells[4].children[0].value) + ",";
			details = details + (cells[5].children[0].value) + ",";
			details = details + (cells[6].innerText) + ",";

			if (paymentType == "CC" || paymentType == "DC") {
				var div = document.getElementsByName("selectInner")[i].parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.id;
				details = details + div.replace('Div', '') + ";";
			} else {
				details = details + " " + ";";
			}

			allDetails = allDetails + details;

		}
	}

	if (!anySelected) {
		alert("Select atleast one row for update!");
		return false;
	}

	var baseUrl = window.location.host;

	$("body").removeClass("loader--inactive");
	var token = document.getElementsByName("token")[0].value;

	$
	.ajax({
		type : "POST",
		url : "updateResellerCharges",
		timeout : 0,
		data : {
			"allDetails" : allDetails,
			"merchantList" : merchantList,
			"resellerList" : resellerList,
			"paymentType" : paymentType,
			"chargeFrom" : $("#chargeFrom").val(),
			"paymentRegion" : paymentRegion,
			"slab" : slab,
			"cardHolderType": _cardHolderType,
			"token" : token,
			"struts.token.name" : "token",
		},
		success : function(data) {
			console.log("hi");
			console.log(data);
			var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0])
					: (data.response));
			if (null != response) {
				alert(response);
			}
			setTimeout(function() {
				$("body").addClass("loader--inactive");
			}, 1000);

			window.open('http://' + baseUrl
					+ '/crm/jsp/resellerChargesUpdate', '_self');
		},
		error : function(data) {
			setTimeout(function() {
				$("body").addClass("loader--inactive");
			}, 1000);

			alert("Unable to update Charging Details");
			window.open('http://' + baseUrl
					+ '/crm/jsp/resellerChargesUpdate', '_self');
		}
	});

}

function fetchRows(event) {
	var resellerList = "";
	var rsellerList = $("#resellerList").val();
	var merchantList = document.getElementById("merchant").value;
	var _cardHolderType = $("#cardHolderType").val();
	console.log(rsellerList);
	if (rsellerList == "") {
		alert("Please select Reseller(s)");
		event.preventDefault();
		return false;
	} else {
		var rsellerListArray = rsellerList.toString().split(",");
		for (var i = 0; i < rsellerListArray.length; i++) {
			resellerList = resellerList + rsellerListArray[i] + ",";
		}
	}

	if(merchantList == ""){
		alert("Please select merchant");
		event.preventDefault();
		return false;
	}

	var paymentType = document.getElementById("payment-type").value
	var paymentRegion = document.getElementById("payment-region").value;
	var slab = $('#slab :selected').text();

	if (paymentType == '') {
		alert("Please select a Payment Type");
		event.preventDefault();
		return false;
	}

	

	if(paymentRegion == "DOMESTIC"){
		// if(_cardHolderType == ""){
		// 	alert("Please select cardholder type");
		// 	event.preventDefault();
		// 	return false;
		// }
	}

	if (paymentRegion == ''
			&& (document.getElementById("payment-type").value == "CC" || document
					.getElementById("payment-type").value == "DC")) {

		alert("Please select a Payment Region");
		event.preventDefault();
		return false;
	}

	if (slab == '') {

		alert("Please select a Slab");
		event.preventDefault();
		return false;
	}

	



	var slabArrary = slab.split(" ");

	if ((slabArrary.length > 2)
			&& (slab.includes("ALL") || slab.includes("All"))) {

		alert("Slab option All Cannot be selected with other options");
		event.preventDefault();
		return false;
	}

	document.getElementById("resellerListFrm").value = resellerList;
	document.getElementById("paymentTypeFrm").value = paymentType;
	document.getElementById("paymentRegionFrm").value = paymentRegion;
	document.getElementById("slabFRm").value = slab;
	document.getElementById("merchantFrm").value = merchantList;
}

var expanded = false;

function showCheckboxes(e) {
	var checkboxes = document.getElementById("checkboxes");
	if (!expanded) {
		checkboxes.style.display = "block";
		expanded = true;
	} else {
		checkboxes.style.display = "none";
		expanded = false;
	}

	e.stopPropagation();

}

function getCheckBoxValue() {
	var allInputCheckBox = document.getElementsByClassName("myCheckBox");

	var allSelectedMerchant = [];
	for (var i = 0; i < allInputCheckBox.length; i++) {

		if (allInputCheckBox[i].checked) {
			allSelectedMerchant.push(allInputCheckBox[i].value);
		}
	}

	document.getElementById('selectBox').setAttribute('title',
			allSelectedMerchant.join());
	if (allSelectedMerchant.join().length > 28) {
		var res = allSelectedMerchant.join().substring(0, 27);
		document.querySelector("#selectBox option").innerHTML = res
				+ '...............';
	} else if (allSelectedMerchant.join().length == 0) {
		document.querySelector("#selectBox option").innerHTML = 'ALL';
	} else {
		document.querySelector("#selectBox option").innerHTML = allSelectedMerchant
				.join();
	}
}

function tdrAdd(val) {

	var val1 = val.parentElement.cells[2].firstElementChild.value
	var val2 = val.parentElement.cells[4].firstElementChild.value

	if (val1 == '') {

		val1 = '0.00';
	}

	if (val2 == '') {

		val2 = '0.00';
	}
	var sum = parseFloat(val1) + parseFloat(val2);
	sum = parseFloat(sum).toFixed(4);

	val.parentElement.cells[6].firstElementChild.value = parseFloat(sum);

}

function sufAdd(val) {

	var val1 = val.parentElement.cells[3].firstElementChild.value
	var val2 = val.parentElement.cells[5].firstElementChild.value

	if (val1 == '') {

		val1 = '0.00';
	}

	if (val2 == '') {

		val2 = '0.00';
	}

	var sum = parseFloat(val1) + parseFloat(val2);
	sum = parseFloat(sum).toFixed(4);
	val.parentElement.cells[7].firstElementChild.value = parseFloat(sum);
}

function tdrAddInner(val) {

	var val1 = val.parentElement.parentElement.cells[2].firstElementChild.value
	var val2 = val.parentElement.parentElement.cells[4].firstElementChild.value

	if (val1 == '') {

		val1 = '0.00';
	}

	if (val2 == '') {

		val2 = '0.00';
	}

	var sum = parseFloat(val1) + parseFloat(val2);
	sum = parseFloat(sum).toFixed(4);
	val.parentElement.parentElement.cells[6].firstElementChild.value = parseFloat(sum);

}

function sufAddInner(val) {

	var val1 = val.parentElement.parentElement.cells[3].firstElementChild.value
	var val2 = val.parentElement.parentElement.cells[5].firstElementChild.value

	if (val1 == '') {

		val1 = '0.00';
	}

	if (val2 == '') {

		val2 = '0.00';
	}

	var sum = parseFloat(val1) + parseFloat(val2);
	sum = parseFloat(sum).toFixed(4);

	val.parentElement.parentElement.cells[7].firstElementChild.value = parseFloat(sum);
}

function copyToAll(val) {
	$("body").removeClass("loader--inactive");
	var getAllTr = document.querySelectorAll(".boxtext");
	if (!val.checked) {
		setTimeout(function() {
			$("body").addClass("loader--inactive");
		}, 1000);

		for (var j = 0; j < document.getElementsByName("selectInner").length; j++){
			getAllTr[j].classList.remove("active-tr");
			document.getElementsByName("pgTdrInner")[j].value = "0.00";
			document.getElementsByName("pgSufInner")[j].value = "0.00";
			document.getElementsByName("acqTdrInner")[j].value = "0.00";
			document.getElementsByName("acqSufInner")[j].value = "0.00";
			document.getElementsByName("selectInner")[j].checked = false;
		}
		val.closest("tr").classList.remove("active-tr");
		return false;
	}

	var pgTdrAll = val.parentElement.parentElement.parentElement.cells[2].firstElementChild.value;
	var pgSufAll = val.parentElement.parentElement.parentElement.cells[3].firstElementChild.value;
	var acqTdrAll = val.parentElement.parentElement.parentElement.cells[4].firstElementChild.value;
	var acqSufAll = val.parentElement.parentElement.parentElement.cells[5].firstElementChild.value;
	var allowFcAll = val.parentElement.parentElement.parentElement.cells[7].firstElementChild.firstElementChild.checked;
	

	for (var i = 0; i < document.getElementsByName("pgTdrInner").length; i++) {
		getAllTr[i].classList.add("active-tr");
		document.getElementsByName("pgTdrInner")[i].value = pgTdrAll;
		document.getElementsByName("pgSufInner")[i].value = pgSufAll;
		document.getElementsByName("acqTdrInner")[i].value = acqTdrAll;
		document.getElementsByName("acqSufInner")[i].value = acqSufAll;
		document.getElementsByName("selectInner")[i].checked = true;
		// document.getElementsByName("selectInner")[i].parentElement.style="background-color : #94FF52";

	}

	setTimeout(function() {
		$("body").addClass("loader--inactive");
	}, 1000);
}
</script>
</head>
<body>
<s:actionmessage class="error error-new-text" />



<s:form id="resellerChargesUpdateForm" action="resellerChargesUpdateAction" method="post">
	<section class="reseller-charges lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
					  <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Reseller Charges Filter</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 d-none" data-id="responseMsg">
					<div class="lpay_error-msg mb-20 lpay_error-custom">
						<p id="responseMsg"></p>
					  </div>
					  <!-- /.lpay_error -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Reseller</label>
					   <s:select class="selectpicker"
						id="resellerList" data-live-search="true"
						headerKey="Select Reseller(s)" list="listReseller"
						listKey="resellerId" listValue="businessName"
						multiple="false" title="Select Reseller"
						name="resellerList" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Merchant <span class="text-danger">*</span></label>
					   <select id="merchant" name = "merchantList" class="selectpicker">
						   <option value="">Select Merchant</option>
					   </select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Payment Type</label>
						<select name="paymentType" id="payment-type"
						class="selectpicker" autocomplete="off"
						onChange="validatePayChange()" >
						<option value="">Payment Type</option>
						<option value="CC">Credit Card</option>
						<option value="DC">Debit Card</option>
						<option value="NB">Net Banking</option>
						<option value="WL">Wallet</option>
						<option value="UP">UPI</option>
						<option value="CD">COD</option>
						<option value="EMCC">EMI CC</option>	
						<option value="EMDC">EMI DC</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Select Payment Region</label>
					   <select name="paymentRegion" id="payment-region"
						class="selectpicker" autocomplete="off" onChange="validateRegionChange()">
							<option value="">Select Payment Region</option>
							<option value="ALL" style="display: none">All</option>
							<option value="DOMESTIC">Domestic</option>
							<option value="INTERNATIONAL">International</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20 -->

				<div class="col-md-4 mb-20 d-none" data-id="cardHolderType">
					<div class="lpay_select_group">
					   <label for="">Select Cardholder Type</label>
					   <select name="cardHolderType" id="cardHolderType"
						class="selectpicker" autocomplete="off">
							<option value="">Select Cardholder Type</option>
							<option value="CONSUMER">Consumer</option>
							<option value="COMMERCIAL">Commercial</option>
							<option value="PREMIUM">Premium</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Slab</label>
						<select name="slab" id="slab" data-actions-box="true" class="selectpicker" title="Select Slab" multiple="true">
						<option value="0.01-1000.00">0.01-1000.00</option>
						<option value="1000.01-2000.00">1000.01-2000.00</option>
						<option value="2000.01-1000000">2000.01-1000000</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Charges From</label>
					   <select name="chargeFrom" id="chargeFrom" class="selectpicker">
							<option value="Merchant">Merchant</option>
							<option value="PG">PG</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				
				<div class="col-md-12 mb-30 text-center lpay-center">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="Download"	onclick="return fetchRows(event)">Submit</button>
					<button id="saveBtn" onclick="updateValues(event)" class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="reload" onclick="reloadValues(event)">Reset</button>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 mb-30">
					<div class="lpay_table_wrapper" id="allEditDiv">
						<span class="text-primary" id="test"><strong>Edit All</strong></span>
						<table width="100%" border="0" align="center" class="lpay_custom_table">
							<tr class="lpay_table_head">
								<th width="4%" align="left" valign="middle">Card Brand</th>
								<th width="7%" align="left" valign="middle">Slab</th>
								<th width="5%" align="left" valign="middle">Reseller %</th>
								<th width="5%" align="left" valign="middle">Reseller
									FC</th>
								<th width="5%" align="left" valign="middle">PG % From
									Reseller</th>
								<th width="5%" align="left" valign="middle">PG FC From
									Reseller</th>
								<th width="3%" align="left" valign="middle">GST</th>
								<th width="1%" align="left" valign="middle">Select All</th>
							</tr>
														
							<tr class="boxtext">
								<td align="left" valign="middle">All</td>
								<td align="left" valign="middle" id="editAllSlab"></td>
								<td align="left" valign="middle" onchange="tdrAdd(this)"
									onkeyup="tdrAdd(this)"><input type="number" min="0"
									value="0.00" class="custom form-control"></td>

								<td align="left" valign="middle"><input type="number"
									min="0" value="0.00" class="custom form-control"></td>
									<s:if test="%{chargeFrom == 'PG'}">
										<td align="left" valign="middle" onchange="tdrAdd(this)"
											onkeyup="tdrAdd(this)"><input type="number" readonly="true" min="0"
											value="0.00" class="custom form-control"></td>
	
										<td align="left" valign="middle"><input readonly="true" type="number"
											value="0.00" class="custom form-control"></td>
									</s:if>
									<s:else>
										<td align="left" valign="middle" onchange="tdrAdd(this)"
											onkeyup="tdrAdd(this)"><input type="number" min="0"
											value="0.00" class="custom form-control"></td>
	
										<td align="left" valign="middle"><input type="number"
											value="0.00" class="custom form-control"></td>
									</s:else>

								<td align="left" valign="middle">18.00</td>
								<td align="center" valign="middle">
									<div title="Select">
										<input type="checkbox" name="select"
											onClick="copyToAll(this)"> <label for="Select"></label>
									</div>
								</td>
							</tr>
						</table>
					</div>
					<!-- /.lpay_table_wrapper -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 mb-30">
					<s:iterator value="resellerChargesDataMap" status="pay">
						<span class="text-primary" id="test">
							<strong><s:property value="key" /></strong>
						</span>
						<s:div id="%{key +'Div'}" value="" class="lpay_table_wrapper">
							<table width="100%" border="0" align="center"
								class="lpay_custom_table">
								<tr class="lpay_table_head">
									<th width="4%" align="left" valign="middle">Card
										Brand</th>
									<th width="7%" align="left" valign="middle">Slab</th>
									<th width="5%" align="left" valign="middle">Reseller
										%</th>
									<th width="5%" align="left" valign="middle">Reseller
										FC</th>
									<th width="5%" align="left" valign="middle">PG % From
										Reseller</th>
									<th width="5%" align="left" valign="middle">PG FC
										From Reseller</th>
									<th width="3%" align="left" valign="middle">GST</th>
									<th width="1%" align="left" valign="middle">Select</th>
									<th width="1%" align="left" valign="middle"
										style="display: none">payment-type</th>
									<th width="1%" align="left" valign="middle"
										style="display: none">payment-region</th>
								</tr>
								<s:iterator value="value" status="itStatus"
									id="%{key +'Iterator'}">
									<tr class="boxtext" name="innerCells">
										<td align="left" valign="middle" name="cardHolderInner"><s:property
												value="cardHolderType" /></td>
										<td align="left" valign="middle" name="slabInner"><s:property
												value="slab" /></td>
										
											<td align="left" valign="middle">
												<input type="number" name="pgTdrInner" min="0"
												onchange="tdrAddInner(this)" onkeyup="tdrAddInner(this)"
												value="0.00" class="custom form-control">
											</td>
											<td align="left" valign="middle">
												<input type="number" name="pgSufInner" min="0" value="0.00"
												class="custom form-control">
											</td>
											<s:if test="%{chargeFrom == 'PG'}">
												<td align="left" valign="middle"><input
													type="number" name="acqTdrInner" min="0"
													onchange="tdrAddInner(this)" readonly="true" onkeyup="tdrAddInner(this)"
													value="0.00" class="custom form-control"></td>
												<td align="left" valign="middle"><input
													type="number" readonly="true" name="acqSufInner" min="0"
													onchange="sufAddInner(this)" onkeyup="sufAddInner(this)"
													value="0.00" class="custom form-control"></td>
											</s:if>
											<s:else>
												<td align="left" valign="middle"><input
													type="number" name="acqTdrInner" min="0"
													onchange="tdrAddInner(this)" onkeyup="tdrAddInner(this)"
													value="0.00" class="custom form-control"></td>
												<td align="left" valign="middle"><input
													type="number" name="acqSufInner" min="0"
													onchange="sufAddInner(this)" onkeyup="sufAddInner(this)"
													value="0.00" class="custom form-control"></td>
											</s:else>
										<td align="left" valign="middle" name="gstInner"><s:property
												value="gst" /></td>
										<td align="center" valign="middle">
											<div title="Select">
												<input type="checkbox" name="selectInner"
													onClick="checkboxSelect(this)"> <label
													for="Select"></label>
											</div>
										</td>

										<td align="left" valign="middle" style="display: none"><s:property
												value="paymentType" /></td>

										<td align="left" valign="middle" style="display: none"><s:property
												value="paymentRegion" /></td>
									</tr>
								</s:iterator>
							</table>
						</s:div>
					</s:iterator>
				</div>
				<!-- /.lpay_table_wrapper -->
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
</s:form>
<s:hidden name="showMerchant" value="%{showMerchant}"
	id="showMerchantFrm"></s:hidden>
<s:hidden name="showSaveButton" value="%{showSaveButton}"
	id="showSaveButtonFrm"></s:hidden>
<s:hidden name="merchantList" value="%{merchantList}"
	id="merchantListFrm"></s:hidden>
<s:hidden name="resellerList" value="%{resellerList}"
	id="resellerListFrm"></s:hidden>
<s:hidden name="paymentType" value="%{paymentType}" id="paymentTypeFrm"></s:hidden>
<s:hidden name="paymentRegion" value="%{paymentRegion}"
	id="paymentRegionFrm"></s:hidden>
	<s:hidden name="slab" value="%{slab}" id="slabFRm"></s:hidden>
<s:hidden name="chargeForm" value="%{chargeFrom}" id="chargesFromFrm"></s:hidden>
<s:hidden name="merchantFrm" value="%{merchantFrm}" id="merchantFrm"></s:hidden>
<s:hidden name="merchantName" value="%{merchantName}" id="merchantName"></s:hidden>
<s:hidden name="response" value="%{response}" id="response"></s:hidden>
</body>
</html>