<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Merchant Default Charges</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
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
<!-- <link rel="stylesheet" href="../css/common-style.css"> -->

<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>



<script type="text/javascript">

$(document).ready(function() {

	// payment region
	$("#payment-region").selectpicker();
	$("#payment-region").selectpicker('val', $("[data-id=paymentRegion]").val());
	$("#payment-region").selectpicker('refresh');

	// payment type
	$("#payment-type").selectpicker();
	$("#payment-type").selectpicker('val', $("[data-id=paymentTypeName]").val());
	$("#payment-type").selectpicker('refresh');

	// acquirer mode
	$("#acquiring-mode").selectpicker();
	$("#acquiring-mode").selectpicker('val', $("[data-id=acquiringModeName]").val());
	$("#acquiring-mode").selectpicker('refresh');
	



var getSlabArray = $("#slabFRm").val();

var newVal = getSlabArray.split(",");


// var getAllOption = $("#slab option");

if(newVal.length >= 0) {
	newVal.forEach(function(val) {
		var _val = val.trim();
		// $("#slab option[value='" + val + "']").prop("selected", true);

		$("#slab option[value='"+ _val +"']").prop("selected", true);
		$("#slab option[value='"+ _val +"']").attr("checked", "checked");
		$("#slab option[value='"+ _val +"']").css({"color": "#fff"});
		// $("#slab option[value='"+ _val +"']").css({"background-color":"#f00","color": "#fff"});
		
		console.group("slab value");
		console.groupEnd();
	});
}
	
});

var editMode;

function validatePayChange(){
	
	if (document.getElementById("payment-type").value == "CC" ||
			document.getElementById("payment-type").value == "DC" ) {		
			document.getElementById("payment-region").disabled = false;
			document.getElementById("acquiring-mode").disabled = false;
			$('#payment-region').selectpicker('val', '');
			$("#acquiring-mode").selectpicker('val', '');
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
			// val.parentElement.parentElement.parentElement.cells[i].style="background-color : none";
			
			
		}
	}
	
}	


function reloadValues(){
	event.preventDefault(); 
	var baseUrl = window.location.host;
	window.loaction.reload();
	
}

function updateValues(event){
	
	event.preventDefault();
	var businessType =  $('#businessType :selected').text();
	
	var checkboxArray = document.getElementsByName("selectInner");
	var length = checkboxArray.length;
	var allDetails = "";
	var anySelected = false;
	for (var i = 0;i<length;i++){
		
		var details = "";
		
		if (document.getElementsByName("selectInner")[i].checked){

			anySelected = true;
			var cells = document.getElementsByName("selectInner")[i].parentElement.parentElement.parentElement.cells;
			
							details = details + (cells[0].innerText) + ",";
							details = details + (cells[1].innerText) + ","; 
							details = details + (cells[2].innerText) + ","; 
							details = details + (cells[3].innerText) + ","; 
							details = details + (cells[4].innerText) + ","; 
							details = details + (cells[5].firstElementChild.firstElementChild.firstElementChild.value) + ",";
							details = details + (cells[6].firstElementChild.firstElementChild.firstElementChild.value) + ",";
							details = details + (cells[7].firstElementChild.firstElementChild.firstElementChild.value) + ",";
							details = details + (cells[8].firstElementChild.firstElementChild.firstElementChild.firstElementChild.value) + ",";
							

						var div = document.getElementsByName("selectInner")[i].parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.id;
						details = details + div.replace('Div' , '') + ";";
								
							allDetails = allDetails + details;
	
	}
	
}

	if (!anySelected){
		
		alert("Select atleast one row for update!");
		return false;
	}
	var baseUrl = window.location.host;
	
	$("body").removeClass("loader--inactive");
	var token  = document.getElementsByName("token")[0].value;
		$.ajax({
			type: "POST",
			url:"updateMerchantDefaultCharges",
			timeout: 0,
			data:{"allDetails":allDetails,"businessType":businessType, "token":token,"struts.token.name": "token",},
			success:function(data){
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);			
				}
				setTimeout(function() {
					$("body").addClass("loader--inactive");					
				}, 1000);

				window.loaction.reload();				
		    },
			error:function(data){
				setTimeout(function() {
					$("body").addClass("loader--inactive");					
				}, 1000);

				alert("Unable to update Default Charges");
				window.loaction.reload();
			}
		});

}
	
function fetchRows(event){
	
	
	var paymentType = document.getElementById("payment-type").value
	var paymentRegion = document.getElementById("payment-region").value;
	var onOff = document.getElementById("acquiring-mode").value;
	var slab = $('#slab :selected').text();
	
	
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

option, optgroup { -webkit-appearance: none; }

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

.custom {
	width: 100% !important;
}

.form-control{
	appearance: none;
}

.overSelect {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
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
	color: #fff !important;
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

#test{
	display: block;
	margin-bottom: 10px;
}

.selectBox {
	position: relative;
}

</style>
</head>
<body>
	<s:actionmessage class="error error-new-text" />
	<section class="merchant-default-rate lapy_section white-bg box-shadow-box mt-70 p20">
		<s:form id="merchantDefChargesForm" action="viewMerchantDefaultCharges" method="post">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant Default Charges</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<select name="paymentType" id="payment-type"
					class="selectpicker" autocomplete="off"
					onChange="validatePayChange()">
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
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <select name="paymentRegion" id="payment-region"
						class="selectpicker" data-live-search="true" autocomplete="off" onChange="validateRegionChange()">
						<option value="">Select Payment Region</option>
						<option value="ALL" style="display: none">All</option>
						<option value="DOMESTIC">Domestic</option>
						<option value="INTERNATIONAL">International</option>
					</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<select name="onOff" id="acquiring-mode" class="selectpicker"
					autocomplete="off">
						<option value="">Select Acquiring mode</option>
						<option value="ALL" style="display: none">All</option>
						<option value="ON_US">On Us</option>
						<option value="OFF_US">Off Us</option>
					</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<select name="slab" id="slab" class="selectpicker"
						autocomplete="off" title="Select Slabs" multiple="multiple">
						<option value="0.01-1000.00">0.01-1000.00</option>
						<option value="1000.01-2000.00">1000.01-2000.00</option>
						<option value="2000.01-1000000">2000.01-1000000</option>
					</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20" id="businessTypeDiv">
				<div class="lpay_select_group">
					<s:select headerKey="" headerValue="Select Industry" name="businessType" 
					id="businessType" list="industryCategoryList" class="selectpicker" autocomplete="off"/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="Download" onclick="fetchRows(event)">View</button>
				<button class="lpay_button lpay-button-md lpay_button-secondary" id="Download" onclick="updateValues(event)">Save</button>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<s:iterator value="bulkDataMap" status="pay">
					<div class="lpay_heading mt-20">
						<h3><s:property value="key" /></h3>
					</div>
					<!-- /.lpay_heading -->
					<div class="">
						<s:div id="%{key +'Div'}" value="" class="lpay_table_wrapper mt-10">
							<table class="lpay_custom_table" width="100%" border="0" align="center"
								class="product-spec">
								<tr class="lpay_table_head">
									<th width="4%" align="left" valign="middle">Card
										Brand</th>
									<th width="7%" align="left" valign="middle">Slab</th>
									<th width="7%" align="left" valign="middle">Payment Type</th>
									<th width="7%" align="left" valign="middle">Region</th>
									<th width="7%" align="left" valign="middle">Acq Mode</th>
									<th width="3%" align="left" valign="middle">Merchant TDR</th>
									<th width="3%" align="left" valign="middle">Merchant Fix Charge</th>
									<th width="3%" align="left" valign="middle">Max
										Charge Merchant</th>
									<th width="2%" align="left" valign="middle">Allow FC</th>
									<th width="2%" align="left" valign="middle">Status</th>
									<th width="1%" align="left" valign="middle">Select</th>
								
								</tr>
								<s:iterator value="value" status="itStatus"
									id="%{key +'Iterator'}">
									<tr class="boxtext" name = "innerCells">


										<td align="left" valign="middle" name = "cardHolderInner"><s:property 
												value="cardHolderType" /></td>
										<td align="left" valign="middle" name = "slabInner"><s:property
												value="slabDef" /></td>
												
										<td align="left" valign="middle" name = "paymentTypeNameInner"><s:property
												value="%{paymentTypeName}" /></td>
												
										<td align="left" valign="middle" name = "paymentRegionNameInner"><s:property
												value="%{paymentRegionName}" /></td>
												
										<td align="left" valign="middle" name = "acquiringModeNameInner"><s:property
												value="%{acquiringModeName}" /></td>
							
										<td align="left" valign="middle"><s:textfield type = 'number' class="form-control"  min= "0" max= "100"
												name = "merchantTdr" value="%{merchantTdr}"/></td>	
												
										<td align="left" valign="middle"><s:textfield type = 'number' class="form-control"  min= "0" max= "100"
												name = "merchantSuf" value="%{merchantSuf}"/></td>	
												
										<td align="left" valign="middle"><s:textfield type = 'number' class="form-control"  min= "0" max= "100"
												name = "merchantMaxCharge" value="%{merchantMaxCharge}"/></td>	
									
								
										<td align="left" valign="middle">
											<div title="Allow FC">
												
										<s:checkbox name = "checkbox1" value="allowFc" />
											</div>
										</td>
								
								<td align="left" valign="middle"><s:property
												value="status" /></td>
												
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
												value="paymentsRegion" /></td>

										<td align="left" valign="middle" style="display: none"><s:property
												value="acquiringMode" /></td>

										<td align="left" valign="middle" style="display: none"><s:property
												value="industryCategory" /></td>
										<td align="left" valign="middle" style="display: none"><s:property
												value="slab" /></td>
									</tr>
								</s:iterator>
							</table>
						</s:div>
					</div>
					<!-- /.lpay_table_wrapper -->
				</s:iterator>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
		</s:form>
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	

	<s:hidden name="slab" value="%{slab}" id="slabFRm"></s:hidden>
	<s:hidden value="%{paymentRegion}" data-id="paymentRegion"></s:hidden>
	<s:hidden value="%{paymentType}" data-id="paymentTypeName"></s:hidden>
	<s:hidden value="%{onOff}" data-id="acquiringModeName"></s:hidden>

	
</body>
</html>