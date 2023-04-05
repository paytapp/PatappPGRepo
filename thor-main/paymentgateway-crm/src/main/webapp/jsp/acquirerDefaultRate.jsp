<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Acquirer Default Charges</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<link rel="stylesheet" href="../css/paymentOptions.css">
<link rel="stylesheet" href="../css/chargingPlatform.css">
<link rel="stylesheet" href="../css/common-style.css">
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>



<script type="text/javascript">
$(window).on("load", function(data) {
	$(".checkbox-label").each(function(e) {
		var that = $(this).find("input[type=checkbox]");
		if(that.prop("checked") == true) {
			$(this).addClass("checkbox-checked");
		}
	});
});

$(document).ready(function() {
	$(".checkbox-label input").on("change", function(e) {
		var _label = $(this).closest("label");

		if($(this).is(":checked")){
			_label.addClass("checkbox-checked");
		}else{
			_label.removeClass("checkbox-checked");
		}
	});

var getSlabArray = $("#slabFRm").val();



var newVal = getSlabArray.split(",");

$("#slab").focus();

// var getAllOption = $("#slab option");

if(newVal.length >= 0) {
	newVal.forEach(function(val) {
		var _val = val.trim();
		// $("#slab option[value='" + val + "']").prop("selected", true);

		$("#slab option[value='"+ _val +"']").prop("selected", true);
		$("#slab option[value='"+ _val +"']").attr("checked", "checked");
		
		// $("#slab option[value='"+ _val +"']").css({"background-color":"#f00","color": "#fff"});
		
	});
}
	
});

var editMode;

function validatePayChange(){
	
	if (document.getElementById("payment-type").value == "CC" ||
		document.getElementById("payment-type").value == "DC" ||
		document.getElementById("payment-type").value == "EMCC" ||
		document.getElementById("payment-type").value == "EMDC"){
		
			   document.getElementById("payment-region").style="background-color : none";
		  document.getElementById("payment-region").disabled = false;
		   document.getElementById("payment-region").value = "";
		   document.getElementById("acquiring-mode").style="background-color : none";
		  document.getElementById("acquiring-mode").disabled = false;
			document.getElementById("acquiring-mode").value = "";
		}
		
		else {
			
			   document.getElementById("payment-region").style="background-color : #b1cbbb";
		  document.getElementById("payment-region").disabled = true;
		  document.getElementById("payment-region").value = "DOMESTIC";
		  document.getElementById("payment-region").children[2].selected = true;
		   document.getElementById("acquiring-mode").style="background-color : #b1cbbb";
		  document.getElementById("acquiring-mode").disabled = true;
			 document.getElementById("acquiring-mode").value = "OFF_US";
			 document.getElementById("acquiring-mode").children[3].selected = true;
		}
}


function validateRegionChange(){
	
	if (document.getElementById("payment-region").value == "DOMESTIC" ){
		
		  document.getElementById("acquiring-mode").style="background-color : none";
		  document.getElementById("acquiring-mode").disabled = false;
		  document.getElementById("acquiring-mode").value = "";
		}
		
		else {
			
		  
		  document.getElementById("acquiring-mode").style="background-color : #b1cbbb";
		  document.getElementById("acquiring-mode").disabled = true;
		  document.getElementById("acquiring-mode").value = "OFF_US";
		}
}

function checkboxSelect(val){
	
	var length = 11;
	if (val.checked){
		val.closest("tr").classList.add("active-tr");
		
		for(var i=0; i<length; i++){
			// val.parentElement.parentElement.parentElement.cells[i].style="background-color : #94FF52";
			
			
		}
	}
	else{
		val.closest("tr").classList.remove("active-tr");

		for(var i=0; i<length; i++){
			val.parentElement.parentElement.parentElement.cells[i].style="background-color : none";
			
			
		}
	}
	
}	


function reloadValues(){
	event.preventDefault(); 
	var baseUrl = window.location.host;
	window.open('http://' + baseUrl + '/crm/jsp/displayAcquirerDefaultRate', '_self');
	
}

function updateValues(event) {
	event.preventDefault();
	var acquirer =  document.getElementById("acquirer").value;
	var businessType =  $('#businessType :selected').text();
	
	var checkboxArray = document.getElementsByName("selectInner");
	var length = checkboxArray.length;
	var allDetails = "";
	var anySelected = false;

	for (var i = 0;i<length;i++) {		
		var details = "";
		
		if (document.getElementsByName("selectInner")[i].checked) {
			anySelected = true;
			var cells = document.getElementsByName("selectInner")[i].closest("tr").cells;
			console.log(cells[0]);
			details = details + (cells[0].innerText) + ",";
			console.log(cells[1]);
			details = details + (cells[1].innerText) + ","; 
			console.log(cells[2]);
			details = details + (cells[2].innerText) + ","; 
			console.log(cells[3]);
			details = details + (cells[3].innerText) + ","; 
			console.log(cells[4]);
			details = details + (cells[4].innerText) + ","; 
			console.log(cells[5]);
			details = details + (cells[5].firstElementChild.firstElementChild.firstElementChild.value) + ",";
			console.log(cells[6]);
			details = details + (cells[6].firstElementChild.firstElementChild.firstElementChild.value) + ",";
			console.log(cells[7]);
			details = details + (cells[7].firstElementChild.firstElementChild.firstElementChild.value) + ",";
			console.log(cells[8]);
			details = details + (cells[8].querySelector("input[type='checkbox']").checked) + ",";			
							

			var div = document.getElementsByName("selectInner")[i].closest(".lpay_table_wrapper").id;
			console.log(div);
			details = details + div.replace('Div' , '') + ";";
								
			allDetails = allDetails + details;

			console.log(allDetails);
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
		url:"updateAcquirerDefaultCharges",
		timeout: 0,
		data: {
			"allDetails" : allDetails,
			"acquirer" : acquirer,
			"businessType" : businessType,
			"token" : token,
			"struts.token.name" : "token"
		},
		success:function(data) {
			console.group("Success");
			// console.log(data);

			$("body").addClass("loader--inactive");
			var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
			if(null != response) {
				alert(response);
			}			
			// window.open('http://' + baseUrl + '/crm/jsp/displayAcquirerDefaultRate', '_self');
			console.groupEnd();
		},
		error:function(data) {
			console.group("Failed");
			$("body").addClass("loader--inactive");
			alert("Unable to update Charging Details");
			console.groupEnd();
			// window.open('http://' + baseUrl + '/crm/jsp/displayAcquirerDefaultRate', '_self');s
		}
	});
}
	
function fetchRows(event){

	var acquirer = document.getElementById("acquirer").value;
	var paymentType = document.getElementById("payment-type").value
	var paymentRegion = document.getElementById("payment-region").value;
	var onOff = document.getElementById("acquiring-mode").value;
	var slab = $('#slab :selected').text();
	
	
	if (acquirer == ''){
		
		alert ("Please select an Acquirer");
		event.preventDefault(); 
		return false;
	}
	
	if (paymentType == ''){
		
		alert ("Please select a Payment Type");
		event.preventDefault(); 
		return false;
	}
	
	if (paymentRegion == '' && (document.getElementById("payment-type").value == "CC" ||
		document.getElementById("payment-type").value == "DC" || document.getElementById("payment-type").value == "EMCC" ||
		document.getElementById("payment-type").value == "EMDC")){
		
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
	
	if (slab == ''){
		
		alert ("Please select a Slab");
		event.preventDefault(); 
		return false;
	}
	
	var slabArrary = slab.split(" ");
	
	if ( (slabArrary.length > 2) && (slab.includes("ALL") || slab.includes("All")) ){
		
		alert ("Slab option All Cannot be selected with other options");
		event.preventDefault(); 
		return false;
	}
	

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


function getCheckBoxValue(){
	 var allInputCheckBox = document.getElementsByClassName("myCheckBox");
  		
  		var allSelectedMerchant = [];
  		for(var i=0; i<allInputCheckBox.length; i++){
  			
  			if(allInputCheckBox[i].checked){
  				allSelectedMerchant.push(allInputCheckBox[i].value);	

  			}
  		}

  		document.getElementById('selectBox').setAttribute('title', allSelectedMerchant.join());
  		if(allSelectedMerchant.join().length>28){
  			var res = allSelectedMerchant.join().substring(0,27);
  			document.querySelector("#selectBox option").innerHTML = res+'...............';
  		}else if(allSelectedMerchant.join().length==0){
  			document.querySelector("#selectBox option").innerHTML = 'ALL';
  		}else{
  			document.querySelector("#selectBox option").innerHTML = allSelectedMerchant.join();
  		}
}

window.addEventListener("load", function() {	
	var acquirerBulkData = document.getElementById("acquirer-bulk-data").innerHTML;	
	if(acquirerBulkData.trim() == "") {
		document.getElementById("acquirer-data-container").classList.add("d-none");
	}
});

</script>
<style>
.selectBox {
	position: relative;
}

.selectBox select {
	width: 95%;
}

.product-spec input[type=text] {
	width: 35px;
}

.boxtext {
	margin-bottom: 15px !important;
}

.btn:focus {
	outline: 0 !important;
}

.download-btn {
	background-color: #002163;
    display: block;
    padding: 8px 18px 6px;
    font-size: 12px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-weight: 400;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.action-btn{
	display: flex;
	align-items: center;
	justify-content: center;
}


.form-control{
	width: 100% !important;
	margin-left: 0 !important;
}
tr.boxtext.active-tr td {
    background-color: #d6d6d6;
}

#slab option[checked='checked']
{
    position: relative;
	z-index: 1;
	
}

#slab option{
	padding: 5px;
}

#slab option[checked='checked']:after
{
	content: "";
	left: -10px;
	right: -10px;
	height: 100%;
	position: absolute;
    background-color: #ccc;
    background-color: #ccc!important; /* for IE */
	padding: 3px;
	z-index: -1;
	top: 0;
	border-bottom: 1px solid #ddd;
}



.selectBox {
	position: relative;
}

.custom {
	width: 100% !important;
}

.overSelect {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
}

[name="acqTdr"]{
	width: 90px !important;
}


#test{
	margin-bottom: 10px;
	display: block;
}

.w-100 { width: 100%; }
.d-flex { display: flex; }
.justify-content-between { justify-content: space-between; }
.align-items-center { align-items: center; }
.d-none { display: none; }
</style>
</head>
<body>
<s:actionmessage class="error error-new-text" />
<s:form id="acquirerDefChargesForm" action="viewAcquirerDefaultCharges" method="post">
	<section class="acquirer-default lapy_section white-bg box-shadow-box mt-70 p20">
		
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Acquirer Default Charges</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <s:select class="selectpicker" headerKey=""
					headerValue="Select Acquirer"
					list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
					listKey="code" listValue="name" name="acquirer" id="acquirer"
					autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<s:select
						headerKey=""
						headerValue="Select Payment Type"
						class="selectpicker"
						list="#{'CC':'Credit Card','DC':'Debit Card','NB':'Net Banking','WL':'Wallet','UP':'UPI','CD':'COD','EMCC':'EMI CC','EMDC':'EMI DC'}"
						name="paymentType"
						id="payment-type"
						autocomplete="off"
						onChange="validatePayChange()"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<s:select
						headerKey=""
						headerValue="Select Payment Region"
						class="selectpicker"
						list="#{'DOMESTIC':'Domestic','INTERNATIONAL':'International'}"
						name="paymentRegion"
						id="payment-region"
						autocomplete="off"
						onChange="validateRegionChange()"
					/>

					<!-- <select name="paymentRegion" id="payment-region" class="selectpicker" autocomplete="off" onChange="validateRegionChange()">
						<option value="">Select Payment Region</option>
						<option value="ALL" style="display: none">All</option>
						<option value="DOMESTIC">Domestic</option>
						<option value="INTERNATIONAL">International</option>
					</select> -->
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<s:select
						headerKey=""
						headerValue="Select Acquiring mode"
						class="selectpicker"
						list="#{'ON_US':'On Us','OFF_US':'Off Us'}"
						name="onOff"
						id="acquiring-mode"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<select name="slab" id="slab" title="Amount Slab" class="selectpicker" autocomplete="off" data-actions-box="true" multiple="multiple">
						<option value="0.01-1000.00">0.01-1000.00</option>
						<option value="1000.01-2000.00">1000.01-2000.00</option>
						<option value="2000.01-1000000">2000.01-1000000</option>
					</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<s:select
						headerKey=""
						headerValue="Select Industry"
						name="businessType"
						id="businessType"
						list="industryCategoryList"
						class="selectpicker"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" onclick="fetchRows(event)">View</button>
			</div>
			<!-- /.col-md-12 -->

		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="acquirer-default lapy_section white-bg box-shadow-box mt-70 p20" id="acquirer-data-container">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text w-100 d-flex justify-content-between align-items-center">
						Acquirer Default Charges Set
						<button class="lpay_button lpay_button-md lpay_button-secondary" onclick="updateValues(event)">Save</button>
					</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12" id="acquirer-bulk-data">
				<s:iterator value="bulkDataMap" status="pay">
					<div class="lpay_heading mt-20">
						<h3><s:property value="key" /></h3>
					</div>
					<!-- /.lpay_heading -->
						<s:div id="%{key +'Div'}" value="" class="lpay_table_wrapper mt-10">
							<table class="lpay_custom_table" width="100%" border="0" align="center"
								class="product-spec">
								<tr class="lpay_table_head">
									<th width="4%" align="left" valign="middle">Card Brand</th>
									<th width="7%" align="left" valign="middle">Slab</th>
									<th width="7%" align="left" valign="middle">Payment Type</th>
									<th width="5%" align="left" valign="middle">Region</th>
									<th width="3%" align="left" valign="middle">Acq Mode</th>
									<th width="5%" align="left" valign="middle">Acq TDR</th>
									<th width="5%" align="left" valign="middle">Acq Fix Charge</th>
									<th width="5%" align="left" valign="middle">Max Charge Acquirer</th>
									<th width="2%" align="left" valign="middle">Allow FC</th>
									<th width="2%" align="left" valign="middle">Status</th>
									<th width="1%" align="left" valign="middle">Select</th>								
								</tr>

								<s:iterator value="value" status="itStatus" id="%{key +'Iterator'}">
									<tr class="boxtext" name = "innerCells">
										<td align="left" valign="middle" name="cardHolderInner"><s:property value="cardHolderType" /></td>
										<td align="left" valign="middle" name="slabInner"><s:property value="slabDef" /></td>												
										<td align="left" valign="middle" name="paymentTypeNameInner">
											<s:property value="%{paymentTypeName}" />
										</td>												
										<td align="left" valign="middle" name="paymentRegionNameInner">
											<s:property value="%{paymentRegionName}" />
										</td>												
										<td align="left" valign="middle" name="acquiringModeNameInner">
											<s:property value="%{acquiringModeName}" />
										</td>
							
										<td align="left" valign="middle">
											<s:textfield
												type='number'
												class="form-control"
												min="0"
												max="100"
												name="acqTdr"
												value="%{acqTdr}"
											/>
										</td>
												
										<td align="left" valign="middle">
											<s:textfield
												type='number'
												class="form-control"
												min="0"
												max="100"
												name="acqSuf"
												value="%{acqSuf}"
											/>
										</td>
												
										<td align="left" valign="middle">
											<s:textfield
												type='number'
												class="form-control"
												min="0"
												max="100"
												name="acquirerMaxCharge"
												value="%{acquirerMaxCharge}"
											/>
										</td>									
								
										<td align="left" valign="middle">
											<div title="Allow FC">
												<label class="checkbox-label unchecked">
													<s:if test="%{allowFc == true}">
														<s:checkbox
															name="checkbox1"
															value="allowFc"
															checked="true"
														/>
													</s:if>
													<s:else>
														<s:checkbox
															name="checkbox1"
															value="allowFc"
															checked="false"
														/>
													</s:else>
												</label>
											</div>
										</td>
								
										<td align="left" valign="middle">
											<s:property value="status" />
										</td>
												
										<td align="center" valign="middle">
											<div title="Select">
												<label class="checkbox-label unchecked">
													<input type="checkbox" name="selectInner" onClick="checkboxSelect(this)">
													<label for="Select"></label>
												</label>
											</div>
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="paymentType" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="paymentsRegion" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="acquiringMode" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="acquirer" />
										</td>

										<td align="left" valign="middle" style="display: none">
											<s:property value="industryCategory" />
										</td>
										<td align="left" valign="middle" style="display: none">
											<s:property value="slab" />
										</td>
									</tr>
								</s:iterator>
							</table>
						</s:div>
				</s:iterator>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
</s:form>

<s:hidden name="slab" value="%{slab}" id="slabFRm"></s:hidden>
	
</body>
</html>