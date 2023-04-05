<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Bulk Update Charges</title>
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
	input[type=number]{
		width: 90px;
	}
	.w-77 { width: 77px; }
	input[readonly].input-unstyled {
		background: none !important;
		border: none;
		outline: none;
		box-shadow: none;
	}
</style>

<script type="text/javascript">
	$(document).ready(function() {
		// add function for all slab
		// function slapAll(){
		// 	var _selectAll = document.querySelector(".bs-select-all");
		// }
		

		var getSlabArray = $("#slabFRm").val();
		var _getSlab = getSlabArray.split(" ").join("");
		// var newVal = getSlabArray.split(",");
		var _flag = getSlabArray.indexOf(",");
		if(_flag != -1){
			var _slab = _getSlab.split(",");
			$("#slab").selectpicker();
			$("#slab").selectpicker('val', _slab);
			$("#slab").selectpicker("refresh");
		}
		if(getSlabArray == "ALL"){
			$('#slab').selectpicker('selectAll');
		}

		// var getAllOption = $("#slab option");		

		document.getElementById("editAllSlab").innerText = document.getElementById("slabFRm").value;
		document.getElementById("merchantSelect").style.display = "none";
		document.getElementById("saveBtn").style.display = "none";
		document.getElementById("reload").style.display = "none";		
		document.getElementById("allEditDiv").style.display = "none";

	  	if (document.getElementById("showMerchantFrm").value == "true") {		
			document.getElementById("allEditDiv").style.display = "block";
			document.getElementById("merchantSelect").style.display = "block";
			document.getElementById("reload").style.display = "inline-block";
			document.getElementById("Download").style.display = "none";
			document.getElementById("saveBtn").style.display = "inline-block";
			
			document.getElementById("acquirer").style="background-color : #dedede";
			document.getElementById("acquirer").disabled = true;
			
			document.getElementById("payment-type").style="background-color : #dedede";
			document.getElementById("payment-type").disabled = true;
			
			document.getElementById("payment-region").style="background-color : #dedede";
			document.getElementById("payment-region").disabled = true;
			
			document.getElementById("acquiring-mode").style="background-color : #dedede";
			document.getElementById("acquiring-mode").disabled = true;
			
			document.getElementById("slab").style="background-color : #dedede";
			document.getElementById("slab").disabled = true;
		  
			if (document.getElementById("payment-type").value != "CC" && document.getElementById("payment-type").value != "DC" ) {
				document.getElementById("acquiring-mode").value = "OFF_US";
				document.getElementById("payment-region").value = "DOMESTIC";
			}
		}

		if (document.getElementById("showSaveButtonFrm").value == "true") {		  
			document.getElementById("saveBtn").style.display = "inline-block";
			document.getElementById("reload").style.display = "inline-block";
		}
	  
		if (document.getElementById("paymentTypeFrm").value != "") {
			document.getElementById("payment-type").value = document.getElementById("paymentTypeFrm").value;
		}
		  
		if (document.getElementById("paymentRegionFrm").value != ""){
			document.getElementById("payment-region").value = document.getElementById("paymentRegionFrm").value;
		}	  
			 
		if (document.getElementById("onOffFRm").value != "") {
			document.getElementById("acquiring-mode").value = document.getElementById("onOffFRm").value;
		}
	  
		if (document.getElementById("slabFRm").value != ""){
			document.getElementById("slab").value = document.getElementById("slabFRm").value;
		}
	});

	var editMode;

	function validatePayChange() {	
		if (document.getElementById("payment-type").value == "CC" ||
			document.getElementById("payment-type").value == "DC" ) {		
			document.getElementById("payment-region").disabled = false;
			document.getElementById("acquiring-mode").disabled = false;
			$('#payment-region').selectpicker("refresh");
			$("#acquiring-mode").selectpicker("refresh");
		} else {
			document.getElementById("payment-region").disabled = true;
			$('#payment-region').selectpicker('val', 'DOMESTIC');
			document.getElementById("acquiring-mode").disabled = true;
			$("#acquiring-mode").selectpicker("val", "OFF_US");
		}
	}

	function validateRegionChange() {	
		if (document.getElementById("payment-region").value == "DOMESTIC" ){		
			document.getElementById("acquiring-mode").disabled = false;
			$("#acquiring-mode").selectpicker("refresh");
		} else {
			document.getElementById("acquiring-mode").disabled = true;
			$("#acquiring-mode").selectpicker("val", "OFF_US");
		}
	}

	function checkboxSelect(val) {
		var length = 13;
		if (val.checked) {
			val.closest("tr").classList.add("active-tr");
			
			// for(var i=0; i<length; i++){
			// 	val.parentElement.parentElement.parentElement.cells[i].style="background-color : #94FF52"
			// }
		}
		else {
			val.closest("tr").classList.remove("active-tr");
			// for(var i=0; i<length; i++){
			// 	val.parentElement.parentElement.parentElement.cells[i].style="background-color : none"
			// }
		}
	}

	function reloadValues() {
		event.preventDefault(); 
		var baseUrl = window.location.host;
		window.open('http://' + baseUrl + '/crm/jsp/displayBulkChargesUpdate', '_self');	
	}

	function updateValues(event) {	
		event.preventDefault();
		
		var merchantList = "";
		var merchList = $("#merchantList").val();
		
		if (merchList == null || merchList == "") {
			alert ("Please select Merchant(s)");
			event.preventDefault(); 
			return false;			
		} else {		
			var merchListArray = merchList.toString().split(",");
			for (var i = 0;i < merchListArray.length ; i++) {
				merchantList = merchantList + merchListArray[i] + ",";
			}
		}	
	
		var acquirer =  document.getElementById("acquirer").value;
		var paymentType = document.getElementById("payment-type").value;
		var paymentRegion =	document.getElementById("payment-region").value;
		var onOff = document.getElementById("acquiring-mode").value;
		var slab = document.getElementById("slab").value;

		var checkboxArray = document.getElementsByName("selectInner");
		var length = checkboxArray.length;
		var allDetails = "";
		var anySelected = false;

		for (var i = 0;i<length;i++) {		
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
				details = details + (cells[6].children[0].value) + ",";
				details = details + (cells[7].children[0].value) + ",";
				details = details + (cells[8].innerText) + ",";
				details = details + (cells[9].firstElementChild.firstElementChild.checked) + ",";
				details = details + (cells[10].firstElementChild.firstElementChild.checked) + ",";
				details = details + (cells[11].children[0].value) + ",";
				details = details + (cells[12].children[0].value) + ",";
				details = details + (cells[13].firstElementChild.firstElementChild.checked) + ",";
				details = details + paymentType + ",";
				details = details + paymentRegion + ",";
				details = details + onOff + ",";
				details = details + acquirer + ",";

				if (paymentType == "CC" || paymentType == "DC"){
					var div = document.getElementsByName("selectInner")[i].parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.id;
					details = details + div.replace('Div' , '') + ";";
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
		var token  = document.getElementsByName("token")[0].value;

		$.ajax({
			type: "POST",
			url:"bulkChargesUpdate",
			timeout: 0,
			data:{"allDetails":allDetails, "merchantList":merchantList,"acquirer":acquirer, "paymentType":paymentType, "paymentRegion":paymentRegion, "slab":slab,"onOff":onOff, "token":token,"struts.token.name": "token",},
			success:function(data) {				
				console.log(data);
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null != response) {
					alert(response);			
				}
				
				setTimeout(function() {
					$("body").addClass("loader--inactive");					
				}, 1000);

				window.open('http://' + baseUrl + '/crm/jsp/displayBulkChargesUpdate', '_self');				
		    },
			error:function(data){
				setTimeout(function() {
					$("body").addClass("loader--inactive");					
				}, 1000);

				alert("Unable to update Charging Details");
				window.open('http://' + baseUrl + '/crm/jsp/displayBulkChargesUpdate', '_self');
			}
		});

}
	
function fetchRows(event) {
	var acquirer = document.getElementById("acquirer").value;
	var paymentType = document.getElementById("payment-type").value
	var paymentRegion = document.getElementById("payment-region").value;
	var onOff = document.getElementById("acquiring-mode").value;
	var slab = $('#slab :selected').text();
	
	if (acquirer == '') {		
		alert ("Please select an Acquirer");
		event.preventDefault(); 
		return false;
	}
	
	if (paymentType == '') {		
		alert ("Please select a Payment Type");
		event.preventDefault(); 
		return false;
	}
	
	if (paymentRegion == '' && (document.getElementById("payment-type").value == "CC" ||
		document.getElementById("payment-type").value == "DC")){
		
		alert ("Please select a Payment Region");
		event.preventDefault(); 
		return false;
	}
	
	if (onOff == '' && (document.getElementById("payment-type").value == "CC" ||
		document.getElementById("payment-type").value == "DC")){
		
		alert ("Please select an Acquiring Mode");
		event.preventDefault(); 
		return false;
	}
	
	if (slab == '') {		
		alert ("Please select a Slab");
		event.preventDefault(); 
		return false;
	}
	
	var slabArrary = slab.split(" ");

	if ( (slabArrary.length > 2) && (slab.includes("ALL") || slab.includes("All")) ) {
		alert ("Slab option All Cannot be selected with other options");
		event.preventDefault(); 
		return false;
	}	
	
	document.getElementById("acquirerFrm").value = acquirer;
	document.getElementById("paymentTypeFrm").value = paymentType;
	document.getElementById("paymentRegionFrm").value = paymentRegion;
	document.getElementById("onOffFRm").value = onOff;
	document.getElementById("slabFRm").value = slab;
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
	for(var i=0; i<allInputCheckBox.length; i++) {
		if(allInputCheckBox[i].checked) {
			allSelectedMerchant.push(allInputCheckBox[i].value);	
		}
	}

	document.getElementById('selectBox').setAttribute('title', allSelectedMerchant.join());
	if(allSelectedMerchant.join().length>28) {
		var res = allSelectedMerchant.join().substring(0,27);
		document.querySelector("#selectBox option").innerHTML = res+'...............';
	} else if(allSelectedMerchant.join().length==0) {
		document.querySelector("#selectBox option").innerHTML = 'ALL';
	} else {
		document.querySelector("#selectBox option").innerHTML = allSelectedMerchant.join();
	}
}


function copyToAll(that) {
	$("body").removeClass("loader--inactive");

	var getAllTr = document.querySelectorAll(".boxtext");
	
	if (that.checked) {
		var merchTdrAll = that.parentElement.parentElement.parentElement.cells[2].firstElementChild.value,
			merchSufAll = that.parentElement.parentElement.parentElement.cells[3].firstElementChild.value,
			acqTdrAll = that.parentElement.parentElement.parentElement.cells[4].firstElementChild.value,
			acqSufAll = that.parentElement.parentElement.parentElement.cells[5].firstElementChild.value,
			resellerTdrAll = that.parentElement.parentElement.parentElement.cells[6].firstElementChild.value,
			resellerFcAll = that.parentElement.parentElement.parentElement.cells[7].firstElementChild.value,
			allowFcAll =  that.parentElement.parentElement.parentElement.cells[9].firstElementChild.firstElementChild.checked,
			chargesFlagAll =  that.parentElement.parentElement.parentElement.cells[10].firstElementChild.firstElementChild.checked,
			maxChargeMerchantAll = that.parentElement.parentElement.parentElement.cells[11].firstElementChild.value,
			maxChargeAcqAll = that.parentElement.parentElement.parentElement.cells[12].firstElementChild.value;
		
		for (var i = 0; i < document.getElementsByName("merchantTdrInner").length; i++) {
			getAllTr[i].classList.add("active-tr");
			document.getElementsByName("merchantTdrInner")[i].value = merchTdrAll;
			document.getElementsByName("merchantSufInner")[i].value = merchSufAll;
			document.getElementsByName("acqTdrInner")[i].value = acqTdrAll;
			document.getElementsByName("acqSufInner")[i].value = acqSufAll;
			document.getElementsByName("resellerTdr")[i].value = resellerTdrAll;
			document.getElementsByName("resellerFc")[i].value = resellerFcAll;
			document.getElementsByName("maxMerchChInner")[i].value = maxChargeMerchantAll;
			document.getElementsByName("maxAcqChInner")[i].value = maxChargeAcqAll;
	
			if (allowFcAll) {
				document.getElementsByName("allowFC")[i].checked = true;			
			} else {
				document.getElementsByName("allowFC")[i].checked = false;
			}
			
			if (chargesFlagAll) {
				document.getElementsByName("chargesFlag")[i].checked = true;			
			} else {
				document.getElementsByName("chargesFlag")[i].checked = false;
			}
			
			
			document.getElementsByName("selectInner")[i].checked = true;		
		}
	} else {
		setTimeout(function() {
			$("body").addClass("loader--inactive");					
		}, 1000);

		that.closest("tr").classList.remove("active-tr");
		// return false;

		for (var i = 0; i < document.getElementsByName("merchantTdrInner").length; i++) {
			getAllTr[i].classList.remove("active-tr");

			document.getElementsByName("selectInner")[i].checked = false;
			document.getElementsByName("allowFC")[i].checked = false;
			document.getElementsByName("chargesFlag")[i].checked = false;
			
			document.getElementsByName("merchantTdrInner")[i].value = 0.00;
			document.getElementsByName("merchantSufInner")[i].value = 0.00;
			document.getElementsByName("acqTdrInner")[i].value = 0.00;
			document.getElementsByName("acqSufInner")[i].value = 0.00;
			document.getElementsByName("resellerTdr")[i].value = 0.00;
			document.getElementsByName("resellerFc")[i].value = 0.00;
			document.getElementsByName("maxMerchChInner")[i].value = 0.00;
			document.getElementsByName("maxAcqChInner")[i].value = 0.00;
		}
	}

	
	
	setTimeout(function() {
		$("body").addClass("loader--inactive");					
	}, 1000);
}



</script>
</head>
<body>
	<s:actionmessage class="error error-new-text" />

	<s:form id="bulkChargesUpdateForm" action="bulkChargesUpdateAction"
		method="post">

	<section class="bulk-charges-update lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Bulk Update Charges</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Acquirer</label>
				   <s:select class="selectpicker" headerKey="" data-live-search="true" headerValue="Select Acquirer"
				   list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
				   listKey="code" listValue="name" name="acquirer" id="acquirer"
				   autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 mb-20 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Payment Type</label>
					<select name="paymentType" data-live-search="true" id="payment-type"
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
			<div class="col-md-3">
				<div class="lpay_select_group">
				   <label for="">Acquiring Mode</label>
				   <select name="onOff" id="acquiring-mode" class="selectpicker mb-20"
						autocomplete="off">
						<option value="">Select Acquiring mode</option>
						<option value="ALL" style="display: none">All</option>
						<option value="ON_US">On Us</option>
						<option value="OFF_US">Off Us</option>
					</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
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
				<input type="hidden" id="slabHidden"> 
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20" id="merchantSelect">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:select class="selectpicker" id="merchantList" data-live-search="true" headerKey="Select Merchant(s)"
				   list="listMerchant" listKey="payId" listValue="businessName"
				   multiple="true" title="Select Merchant(s)" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 mb-30 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="Download"	onclick="fetchRows(event)">Submit</button>
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
							<th width="5%" align="left" valign="middle">Merchant TDR</th>
							<th width="5%" align="left" valign="middle">Merchant FC</th>
							<th width="5%" align="left" valign="middle">Bank TDR</th>
							<th width="5%" align="left" valign="middle">Bank FC</th>
							<th width="5%" align="left" valign="middle">Reseller TDR</th>
							<th width="5%" align="left" valign="middle">Reseller FC</th>
							<th width="3%" align="left" valign="middle">GST</th>
							<th width="2%" align="left" valign="middle">Allow FC</th>
							<th width="2%" align="left" valign="middle">Higher Charge</th>
							<th width="3%" align="left" valign="middle">Max Charge Merchant</th>
							<th width="3%" align="left" valign="middle">Max Charge Acquirer</th>
							<th width="1%" align="left" valign="middle" >Select All</th>
						</tr>
													
						<tr class="boxtext">
							<td align="left" valign="middle">All</td>
							<td align="left" valign="middle" id="editAllSlab"></td>

							<td align="left" valign="middle" >
								<input type="text" value="0.00" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);">
							</td>

							<td align="left" valign="middle" >
								<input type="text" value="0.00" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);">
							</td>

							<td align="left" valign="middle"   >
								<input type="text" value="0.00" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);">
							</td>

							<td align="left" valign="middle"  >
								<input type="text" value="0.00" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);">
							</td>

							<td align="left" valign="middle"   >
								<input type="text" value="0.00" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);">
							</td>

							<td align="left" valign="middle"  >
								<input type="text" value="0.00" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);">
							</td>

							

							<td align="left" valign="middle">18.00</td>

							<td align="left" valign="middle">
								<div title="Allow FC">
									<input type="checkbox" name="allowFCAll">
								</div>
							</td>
							<td align="left" valign="middle">
								<div title="Higher Charge">
									<input type="checkbox" name="chargesFlagAll">
								</div>
							</td>

							<td align="left" valign="middle">
								<input type="text" value="0" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 0);" onkeypress="onlyNumericKey(this, event, 0);">
							</td>

							<td align="left" valign="middle">
								<input type="text" value="0" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 0);" onkeypress="onlyNumericKey(this, event, 0);">
							</td>

							<td align="center" valign="middle">
								<div title="Select">
									<input type="checkbox" name="select" onclick="copyToAll(this)"> <label for="Select"></label>
								</div>
							</td>
						</tr>
					</table>
				</div>
				<!-- /.lpay_table_wrapper -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 mb-30">
					<s:iterator value="bulkDataMap" status="pay">
						<span class="text-primary" id="test">
							<strong><s:property value="key" /></strong>
						</span>
						<s:div id="%{key +'Div'}" value="" class="lpay_table_wrapper">
							<table width="100%" border="0" align="center" class="lpay_custom_table">
								<tr class="lpay_table_head">
									<th width="4%" align="left" valign="middle">Card Brand</th>
									<th width="7%" align="left" valign="middle">Slab</th>
									<th width="5%" align="left" valign="middle">Merchant TDR</th>
									<th width="5%" align="left" valign="middle">Merchant FC</th>
									<th width="5%" align="left" valign="middle">Bank TDR</th>
									<th width="5%" align="left" valign="middle">Bank FC</th>
									<th width="5%" align="left" valign="middle">Reseller TDR</th>
									<th width="5%" align="left" valign="middle">Reseller FC</th>
									<th width="3%" align="left" valign="middle">GST</th>
									<th width="2%" align="left" valign="middle">Allow FC</th>
									<th width="2%" align="left" valign="middle">Higher Charge</th>
									<th width="3%" align="left" valign="middle">Max Charge Merchant</th>
									<th width="3%" align="left" valign="middle">Max Charge Acquirer</th>
									<th width="1%" align="left" valign="middle">Select</th>
									<th width="1%" align="left" valign="middle" style="display: none">payment-type</th>
									<th width="1%" align="left" valign="middle" style="display: none">payment-region</th>
									<th width="1%" align="left" valign="middle" style="display: none">onOff</th>
									<th width="1%" align="left" valign="middle" style="display: none">acquirer</th>
								</tr>
								<s:iterator value="value" status="itStatus" id="%{key +'Iterator'}">
									<tr class="boxtext" name = "innerCells">
										<td align="left" valign="middle" name = "cardHolderInner">
											<s:property value="cardHolderType" />
										</td>

										<td align="left" valign="middle" name="slabInner">
											<s:property value="slab" />
										</td>

										<td align="left" valign="middle">
											<input type="text" name="merchantTdrInner" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);" value="0.00" class="custom form-control w-77">
										</td>

										<td align="left" valign="middle">
											<input type="text" name="merchantSufInner" onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);" value="0.00" class="custom form-control w-77">
										</td>

										<td align="left" valign="middle">
											<input type="text" name="acqTdrInner"  onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);" value="0.00" class="custom form-control w-77">
										</td>

										<td align="left" valign="middle">
											<input type="text" name="acqSufInner" onkeyup=" onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);" value="0.00" class="custom form-control w-77">
										</td>

										<td align="left" valign="middle">
											<input type="text" name="resellerTdr"  onkeyup="onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);" value="0.00" class="custom form-control w-77">
										</td>

										<td align="left" valign="middle">
											<input type="text" name="resellerFc" onkeyup=" onlyNumericKey(this, event, 4);" onkeypress="onlyNumericKey(this, event, 4);" value="0.00" class="custom form-control w-77">
										</td>

										<td align="left" valign="middle" name="gstInner">
											<s:property value="gst" />
										</td>

										<td align="left" valign="middle">
											<div title="Allow FC">
												<input type="checkbox" name="allowFC">
											</div>
										</td>
										<td align="left" valign="middle">
											<div title="Higher Charge">
												<input type="checkbox" name="chargesFlag">
											</div>
										</td>

										<td align="left" valign="middle">
											<input type="text" name="maxMerchChInner" value="0" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 0);" onkeypress="onlyNumericKey(this, event, 0);">
										</td>

										<td align="left" valign="middle">
											<input type="text" name="maxAcqChInner" value="0" class="custom form-control w-77" onkeyup="onlyNumericKey(this, event, 0);" onkeypress="onlyNumericKey(this, event, 0);">
										</td>

										<td align="center" valign="middle">
											<div title="Select">
												<input type="checkbox" name="selectInner" onClick="checkboxSelect(this)"> <label for="Select"></label>
											</div>
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="paymentType" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="paymentRegion" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="onOff" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="acquirer" />
										</td>
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
	<s:hidden name="acquirer" value="%{acquirer}" id="acquirerFrm"></s:hidden>
	<s:hidden name="paymentType" value="%{paymentType}" id="paymentTypeFrm"></s:hidden>
	<s:hidden name="paymentRegion" value="%{paymentRegion}"
		id="paymentRegionFrm"></s:hidden>
	<s:hidden name="onOff" value="%{onOff}" id="onOffFRm"></s:hidden>
	<s:hidden name="slab" value="%{slab}" id="slabFRm"></s:hidden>
	<script type="text/javascript">
	
	$(window).on("load", function(e){

		function deselectAll(){
			$("#slabHidden").val("");
			$("#slabHidden").removeAttr("name");
			$("#slab").attr("name", "slab");
		}

		$(".bs-select-all").on("click", function(e){
			$("#slab").removeAttr("name");
			$("#slabHidden").val("ALL");
			$("#slabHidden").attr("name", "slab");
		});
		
		$(".bs-deselect-all").on("click", function(e){
			deselectAll();
		});

		$(".dropdown-menu.inner li").on("click", function(e){
			deselectAll();
		});		
	});

	</script>
	<script src="../js/decimalLimit.js"></script>
</body>
</html>