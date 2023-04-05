<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Surcharge Platform</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<link rel="stylesheet" type="text/css" href="../css/popup.css" />
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<style type="text/css">
.card-list-toggle{cursor: pointer; padding: 8px 12px; border: 1px solid #ccc; position: relative; background:#ddd;}
.card-list-toggle:before{position: absolute; right: 10px; top: 7px; content:"\f078"; font-family:'FontAwesome'; font-size: 15px;}
.card-list-toggle.active:before{content:"\f077";}
.card-list{display: none;}
.btn:focus{
		outline: 0 !important;
}
</style>

<script type="text/javascript">
$(document).ready(function(){
 
  // Initialize select2
  $("#merchants").select2();
});
</script>


<script type="text/javascript">
$(document).ready(function() {
	$('.card-list-toggle').on('click', function(){
		$(this).toggleClass('active');
		$(this).next('.card-list').slideToggle();
	});
	 //var paymentVal = document.getElementById("paymentType").value;
	 
	   $('#paymentType').change(function(event){
				
		   var merchants = $("select#merchants").val();
		   var acquirer = document.getElementById("paymentType").value;
		   var emailId = merchants;
		   var paymentType = acquirer;

		   if(merchants==null ||merchants=="" || acquirer==null||acquirer==""){
			   document.getElementById("datatable").style.display="none";
			   document.getElementById("datatable1").style.display="none";
			   document.getElementById("datatable2").style.display="none";
			  return false;
			  
			 }
		   else{
			   document.getElementById("datatable").style.display="block";
			   document.getElementById("datatable1").style.display="block";
			   document.getElementById("datatable2").style.display="none";     
			   
		   }
		   document.getElementById("surchargedetailform").submit();
		   
	   });

   $('#merchants').change(function(event){
	   var acquirer = document.getElementById("paymentType").value;
	   var merchants = $("select#merchants").val();
	   var emailId = merchants;
	   var paymentType = acquirer;
	   
	   if(merchants==null ||merchants=="" || acquirer==null||acquirer==""){
		   document.getElementById("datatable").style.display="none";
		   document.getElementById("datatable1").style.display="none";
		   document.getElementById("datatable2").style.display="none";
		  return false;
		  
		 }
	   else{
		   document.getElementById("datatable").style.display="block";
		   document.getElementById("datatable1").style.display="block";
		   document.getElementById("datatable2").style.display="none";
	   }
	   document.getElementById("surchargedetailform").submit();	
    });
    
	
		var btnArray = document.getElementsByName("cancelBtn");
		//cancelBtnArray;
			for (var i=0;i<btnArray.length ; i++){
				//if ((btnArray[i].id).indexof('cancelBtn') !== -1){
				var cancelBtnCurrent = btnArray[i];
				cancelBtnCurrent.disabled = true;
				//}
	}

});

var editMode;

function editCurrentRow(divId,curr_row,ele,cancelBtnId){
	 var cancelButton = document.getElementById(cancelBtnId);
	   cancelButton.disabled = false;
	var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
	var div = document.getElementById(divId);

	var table = div.getElementsByTagName("table")[0];

	var merchantId = document.getElementById("merchants").value;
	var paymentType = document.getElementById("paymentType").value;
	var rows = table.rows;
	var currentRowNum = Number(curr_row);
	var currentRow = rows[currentRowNum];
	var cells = currentRow.cells;
	var cell0 = cells[0];
	var cell1 = cells[1];
	var cell2 = cells[2];
	var cell3 = cells[3]; 

	var cell4 =  cells[4];
	var cell5 =  cells[5];
	var cell6 =  cells[6];
	
	var cell0Val = cell0.innerText.trim();
	var cell1Val = cell1.innerText.trim();
	var cell2Val = cell2.innerText.trim();
	var cell3Val = cell3.innerText.trim();
	var cell4Val = cell4.innerText.trim();
	var cell5Val = cell5.innerText.trim();
	var cell6Val = cell6.innerText.trim();
	
	if(ele.value=="Edit"){
		if(editMode) 
		{
				alert('Please edit the current row to proceed');
				return;
		}
		ele.value="save";
		ele.className ="btn btn-success btn-xs";
		cell2.innerHTML = "<input type='number' id='cell2Val'   class='chargingplatform' min='0' step='0.0' value="+cell2Val+"></input>";
		cell3.innerHTML = "<input type='number' id='cell3Val'   class='chargingplatform' min='0' step='0.0' value="+cell3Val+"></input>";
		cell4.innerHTML = "<input type='number' id='cell4Val'   class='chargingplatform' min='0' step='0.0' value="+cell4Val+"></input>";
		editMode = true;
	}
	else{
		var surchargePercentage = document.getElementById('cell2Val').value;
		var surchargeAmount = document.getElementById('cell3Val').value;
		var minTransactionAmount = document.getElementById('cell4Val').value;
		
		if (minTransactionAmount == '' || minTransactionAmount < 0){
			alert('Blank / Negative values not allowed for Min Transaction amount.');
			return false;
		}
		if (surchargePercentage == '' || surchargePercentage < 0){
			alert('Blank / Negative values not allowed for Surcharge Percentage.');
			return false;
		}
		if (surchargeAmount == '' || surchargeAmount < 0){
			alert('Blank / Negative values not allowed for Surcharge Amount.');
			return false;
		}
		
		
		
		var paymentsRegion = cell5Val;
		var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
		var loginEmailId = "<s:property value='%{#session.USER.EmailId}'/>";
		cell2.innerHTML = surchargePercentage;
		cell3.innerHTML = surchargeAmount;
		var merchantId = document.getElementById("merchants").value;
		var paymentType = document.getElementById("paymentType").value;
		editMode = false;
		$("body").removeClass("loader--inactive");

		ele.value="Edit";
		ele.className ="btn btn-info btn-xs";		
		var token  = document.getElementsByName("token")[0].value;
		
		$.ajax({
			type: "POST",
			url:"editSurchargeDetail",
			timeout: 0,
			data:{"emailId":merchantId,"minTransactionAmount":minTransactionAmount,"paymentType":paymentType,"surchargePercentage":surchargePercentage, "surchargeAmount":surchargeAmount, "userType":userType, "loginEmailId":loginEmailId,"paymentsRegion":paymentsRegion,"token":token,"struts.token.name": "token",},
			success:function(data){
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);			
				}
				//TODO....clean values......using script to avoid page refresh
				window.location.reload();
		    },
			error:function(data){
				window.location.reload();
				alert("Invalid Input , surcharge data not saved");
			},
			input:function(data){
				alert("Invalid Input , please correct and try again");
				
			}
		});
	}
}



function editSurchargeCurrentRow(divId,curr_row,ele,cancelBtnId){
	var cancelButton = document.getElementById(cancelBtnId);
	   cancelButton.disabled = false;
	var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
	var loginUserEmailId = "<s:property value='%{#session.USER.EmailId}'/>";
	var div = document.getElementById(divId);

	var table = div.getElementsByTagName("table")[0];

	var merchantId = document.getElementById("merchants").value;
	var paymentType = document.getElementById("paymentType").value;
	
	var acq = divId;
	var acquirer =  acq.substring(0, acq.length - 3);
	
	var rows = table.rows;
	var currentRowNum = Number(curr_row);
	var currentRow = rows[currentRowNum];
	var cells = currentRow.cells;
	var cell0 = cells[0];
	var cell1 = cells[1];
	var cell2 = cells[2];
	var cell3 = cells[3];
	var cell4 =  cells[4].children[0];
	var cell5 =  cells[4].children[1];
	var cell6 =  cells[5].children[0];
	var cell7 =  cells[5].children[1];
	
	var cell8 =  cells[6].children[0];
	var cell9 =  cells[6].children[1];
	var cell10 =  cells[7].children[0];
	var cell11 =  cells[7].children[1];
	
	
	var cell12 =  cells[8];
	var cell13 =  cells[9];
	
	
	var cell0Val = cell0.innerText.trim();
	var cell1Val = cell1.innerText.trim();
	var cell2Val = cell2.innerText.trim();
	var cell3Val = cell3.innerText.trim();
	var cell4Val = cell4.innerText.trim();
	var cell5Val = cell5.innerText.trim();
	var cell6Val = cell6.innerText.trim();
	var cell7Val = cell7.innerText.trim();
	var cell8Val = cell8.innerText.trim();
	var cell9Val = cell9.innerText.trim();
	var cell10Val = cell10.innerText.trim();
	var cell11Val = cell11.innerText.trim();
	var cell12Val = cell12.querySelector('input[type=checkbox]').checked;
	var cell13Val = cell13.innerText.trim();

	if(ele.value=="Edit"){
		if(editMode) 
		{
				alert('Please edit the current row to proceed');
				return;
		}
		ele.value="save";
		ele.className ="btn btn-success btn-xs";
		cell4.innerHTML = "<input type='number' id='cell4Val'   class='chargingplatform' min='0' step='0.0' value="+cell4Val+"></input>";
		cell5.innerHTML = "<input type='number' id='cell5Val'   class='chargingplatform' min='0' step='0.0' value="+cell5Val+"></input>";
		cell6.innerHTML = "<input type='number' id='cell6Val'   class='chargingplatform' min='0' step='0.0' value="+cell6Val+"></input>";
		cell7.innerHTML = "<input type='number' id='cell7Val'   class='chargingplatform' min='0' step='0.0' value="+cell7Val+"></input>";
		
		cell8.innerHTML = "<input type='number' id='cell8Val'   class='chargingplatform' min='0' step='0.0' value="+cell8Val+"></input>";
		cell9.innerHTML = "<input type='number' id='cell9Val'   class='chargingplatform' min='0' step='0.0' value="+cell9Val+"></input>";
		cell10.innerHTML = "<input type='number' id='cell10Val'   class='chargingplatform' min='0' step='0.0' value="+cell10Val+"></input>";
		cell11.innerHTML = "<input type='number' id='cell11Val'   class='chargingplatform' min='0' step='0.0' value="+cell11Val+"></input>";
		
		cell12.innerHTML = "";
		if(cell12Val){
			cell12.innerHTML = "<input type='checkbox' id='cell12Val' checked='true'></input>";
		}else{
		    cell12.innerHTML = "<input type='checkbox' id='cell12Val'></input>";
		}
		editMode = true;
	}
	else{
		var bankSurchargePercentageOnCommercial = document.getElementById('cell4Val').value;
		var bankSurchargePercentageOffCommercial = document.getElementById('cell5Val').value;
		var bankSurchargeAmountOnCommercial = document.getElementById('cell6Val').value;
		var bankSurchargeAmountOffCommercial = document.getElementById('cell7Val').value;
		var bankSurchargePercentageOnCustomer = document.getElementById('cell8Val').value;
		var bankSurchargePercentageOffCustomer = document.getElementById('cell9Val').value;
		var bankSurchargeAmountOnCustomer = document.getElementById('cell10Val').value;
		var bankSurchargeAmountOffCustomer = document.getElementById('cell11Val').value;
		
		if (bankSurchargePercentageOnCommercial == '' || bankSurchargePercentageOnCommercial < 0 
				|| bankSurchargePercentageOffCommercial == '' || bankSurchargePercentageOffCommercial < 0 
				|| bankSurchargeAmountOnCommercial == '' || bankSurchargeAmountOnCommercial < 0 
				|| bankSurchargeAmountOffCommercial == '' || bankSurchargeAmountOffCommercial < 0 
				|| bankSurchargePercentageOnCustomer == '' || bankSurchargePercentageOnCustomer < 0 
				|| bankSurchargePercentageOffCustomer == '' || bankSurchargePercentageOffCustomer < 0 
				|| bankSurchargeAmountOnCustomer == '' || bankSurchargeAmountOnCustomer < 0 
				|| bankSurchargeAmountOffCustomer == '' || bankSurchargeAmountOffCustomer < 0 ){
			
			alert ('Blank / Negative values are not allowed.');
			return false;
			
		}
		var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
		cell4.innerHTML = bankSurchargePercentageOnCommercial;
		cell5.innerHTML = bankSurchargePercentageOffCommercial;
		cell6.innerHTML = bankSurchargeAmountOnCommercial;
		cell7.innerHTML = bankSurchargeAmountOffCommercial;
		
		cell8.innerHTML = bankSurchargePercentageOnCustomer;
		cell9.innerHTML = bankSurchargePercentageOffCustomer;
		cell10.innerHTML = bankSurchargeAmountOnCustomer;
		cell11.innerHTML = bankSurchargeAmountOffCustomer;
		
		var merchantId = document.getElementById("merchants").value;
		var paymentType = document.getElementById("paymentType").value;
		editMode = false;
		$("body").removeClass("loader--inactive");

		ele.value="Edit";
		ele.className ="btn btn-info btn-xs";		
		var token  = document.getElementsByName("token")[0].value;
		$.ajax({
			type: "POST",
			url:"editSurchargeMappingDetail",
			timeout: 0,
			data:{"emailId":merchantId, "paymentType":paymentType, "bankSurchargePercentageOnCommercial":bankSurchargePercentageOnCommercial, "bankSurchargePercentageOnCustomer":bankSurchargePercentageOnCustomer,
			"bankSurchargePercentageOffCommercial":bankSurchargePercentageOffCommercial,"bankSurchargePercentageOffCustomer":bankSurchargePercentageOffCustomer,
				"bankSurchargeAmountOnCommercial":bankSurchargeAmountOnCommercial,"bankSurchargeAmountOnCustomer":bankSurchargeAmountOnCustomer,
				"bankSurchargeAmountOffCommercial":bankSurchargeAmountOffCommercial,"bankSurchargeAmountOffCustomer":bankSurchargeAmountOffCustomer,"allowOnOff":cell12Val,"allowOnOff":cell12Val,"acquirer":acquirer,
				"paymentsRegion":cell1Val,"merchantIndustryType":cell3Val,"status":cell13Val,"mopType":cell2Val,"userType":userType,"loginUserEmailId":loginUserEmailId,"token":token,"struts.token.name": "token",},
			success:function(data){
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);			
				}
				//TODO....clean values......using script to avoid page refresh
				window.location.reload();
		    },
			error:function(data){
				window.location.reload();
				alert("Invalid Input , surcharge data not saved");
			}
		});
	}
}


function cancel(curr_row,ele){
	var parentEle = ele.parentNode;
	
	if(editMode){
		$("body").removeClass("loader--inactive");
	 	window.location.reload();
	}
}

</script>
<script type="text/javascript">

$(window).on("load", function(){

	/*if($("#paymentType").value == undefined){
		$(".surcharge-bank").removeClass('active');
	}*/
});

  	$(document).ready(function(){

  		if($("#paymentType").value != ""){
			$(".surcharge-bank").addClass('active');
		}
		
  		$("#paymentType").on("change", function(){
			if(this.value == ""){
				$(".surcharge-bank").removeClass('active');
			}
		});
	});    
</script>
<script type="text/javascript">
$(document).ready(function(){
    $(".surcharge-bank").click(function(){
        $("#datatable1").hide();
        $("#datatable2").show();
        $("#datatable").show();
        $(".surcharge-bank").addClass("active");
        $(".surcharge-report").removeClass("active");
    });
    $(".surcharge-report").click(function(){
        $("#datatable1").show();
        $("#datatable2").hide();
        $("#datatable").hide();
        $(".surcharge-report").addClass("active");
        $(".surcharge-bank").removeClass("active");
    });
});
</script>

<style>
.product-spec input[type=text] {
	width: 35px;
}
.btn-tab{
	width: 17%;
	padding: 6px;
	font-size: 14px;
	color: #2c2c2c!important;
	background-color: #eaeaea!important;
	border: 1px solid #eaeaea!important;
	border-radius: 5px;
}
.btn-primary.active{
	background-color: #002163!important;
    border-color: #002163!important;
    color: #ffffff!important;
    border-radius: 5px;
	}
.uper-input{
	width: 98% !important;
	margin-left: -10px !important;
}
</style>
</head>
<body>
	
<s:actionmessage class="error error-new-text" />

	<s:form id="surchargedetailform" action="surchargePlatformAction"
		method="post">
	<div style="overflow:scroll !important;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="txnf" style="margin-top:1%;">
		<tr>
		<td style="padding: 9px;"><button type="button" class="btn-primary btn-tab surcharge-bank">Set Surcharge for Bank</button>
		<button type="button" class="btn-primary btn-tab surcharge-report">Surcharge Report</button></td>
		</tr>
			<tr>
				<td width="21%"><h2>Surcharge Platform</h2></td>
			</tr>
			<tr>
				<td align="center" valign="top"><table width="98%" border="0"
						cellspacing="0" cellpadding="0">
						<tr>
							<td align="left">
								<div class="container">
									<div class="form-group col-md-4 txtnew col-sm-4 col-xs-6">
										<s:select headerKey="-1" headerValue="Select User"
											list="#{'1':'Merchant'}" id="user" name="user" value="1"
											class="form-control uper-input" autocomplete="off" />
									</div>
									<div class="form-group col-md-4 txtnew col-sm-4 col-xs-6">
										<s:select headerValue="Select Merchant" headerKey=""
											name="emailId" class="form-control uper-input" id="merchants"
											list="listMerchant" listKey="emailId"
											listValue="businessName" autocomplete="off" />
									</div>
									<div class="form-group col-md-4 txtnew col-sm-4 col-xs-6">
										<s:select class="form-control uper-input" headerKey=""
											headerValue="Payment Type"
											list="@com.paymentgateway.commons.util.PaymentType@values()"
											listKey="code" listValue="name" name="paymentType" id="paymentType"
											autocomplete="off" />
									</div>
								</div>
							</td>
						</tr>
						<tr>
							<td align="left"><div id="datatable" class="scrollD">
									<s:iterator value="aaData" status="pay">
										<br>
										<div class="text-primary card-list-toggle"><strong><s:property
													value="key" /></strong></div> 
										<div class="scrollD card-list">
											<s:div id="%{key +'Div'}">
												<table width="100%" border="0" align="center"
													class="product-spec">
													<tr class="boxheading">
														<th width="5%" height="25" valign="middle" style="display: none">Payment</th>
														<th width="6%" align="left" valign="middle">GST</th>
														<th width="8%" align="left" valign="middle">Surcharge %</th>
														<th width="7%" align="left" valign="middle">Surcharge FC</th>
														<th width="10%" align="left" valign="middle">Minimum Transaction Amount</th>
														<th width="10%" align="left" valign="middle">Region</th>
														<th width="5%" align="left" valign="middle">Update</th>
														<th width="2%" align="left" valign="middle" style="display: none">id</th>
														<th width="5%" align="left" valign="middle">
														           <span id="cancelLabel">Cancel</span>
														</th>
													</tr>
													<s:iterator value="value" status="itStatus">
														<tr class="boxtext">
															<td align="left" valign="middle" style="display: none">
															    <s:property value="paymentType" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="serviceTax" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="surchargePercentage" />
															</td>
															<td align="left" valign="middle">
															  <s:property value="surchargeAmount" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="minTransactionAmount" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="paymentsRegion" />
															</td>
															<td align="center" valign="middle">
															    <s:div>
																	<s:textfield   id="edit%{#itStatus.count}" value="Edit"
																		type="button"
																		onclick="editCurrentRow('%{key +'Div'}','%{#itStatus.count}', this,'cancelBtn%{key +#itStatus.count}')"
																		class="btn btn-info btn-xs" autocomplete="off"></s:textfield>
																</s:div>
															</td>
															<td align="center" valign="middle" style="display: none">
															    <s:property value="id" />
															</td>
															<td align="center" valign="middle">
															    <s:textfield 
																	id="cancelBtn%{key +#itStatus.count}" value="Cancel"
																	type="button" name = "cancelBtn"
																	onclick="cancel('%{#itStatus.count}',this)"
																	class="btn btn-danger btn-xs" autocomplete="off">
														        </s:textfield>
															</td>
														</tr>
													</s:iterator>
												</table>
											</s:div>
										</div>
									</s:iterator>
								</div></td>
						</tr>
					</table></td>
			</tr>
			
			<tr>
				<td align="center" valign="top">
				<table width="98%" border="0" cellspacing="0" cellpadding="0">
						<tr>
						
							<td align="left">							
							<div id="datatable2" class="scrollD">	
								<br>													
									<s:iterator value="surchargeMapData" status="pay">
									<h3 align="center">Set Surcharge for Bank </h3>
										<br>
										<div class="text-primary card-list-toggle" id = "test"><strong><s:property
												value="key" /></strong>
										</div>
										<div class="scrollD card-list">
											<s:div id="%{key +'Div'}" value = "key">
												<table width="100%" border="0" align="center" class="product-spec">
													<tr class="boxheading">
														<th width="5%" height="25" valign="middle" style="display: none">Payment</th>
														<th width="4%" align="left" valign="middle">Region</th>
														<th width="4%" align="left" valign="middle">MOP Type</th>
														<th width="6%" align="left" valign="middle">Merchant Type</th>
														<th width="4%" align="left" valign="middle">Bank %(Commercial)</th>
														<th width="4%" align="left" valign="middle">Bank FC(Commercial)</th>
														<th width="4%" align="left" valign="middle">Bank %(Consumer)</th>
														<th width="4%" align="left" valign="middle">Bank FC(Consumer)</th>
														<th width="4%" align="left" valign="middle">OnUs/OffUs</th>
														<th width="4%" align="left" valign="middle">Status</th>	
														<th width="5%" align="left" valign="middle">Update</th>
														<th width="2%" align="left" valign="middle" style="display: none">id</th>
														<th width="5%" align="left" valign="middle">
														    <span id="cancelLabel">Cancel</span>
														</th>
													</tr>
													<s:iterator value="value" status="itStatus">
													<tr class="boxtext">
															<td align="left" valign="middle" style="display: none">
															    <s:property value="paymentType" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="paymentsRegion" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="mopType" />
															</td>
															<td align="left" valign="middle">
															    <s:property value="merchantIndustryType" />
															</td>
															<td align="left" valign="middle" class="nomarpadng">
															    <div title="Surcharge on us">
																	&nbsp;
																	<s:property value="bankSurchargePercentageOnCommercial" />
																</div>
																<div class="cellborder" title="Surcharge off us">
																	&nbsp;
																	<s:property value="bankSurchargePercentageOffCommercial" />
																</div>
															</td>
																	
															<td align="left" valign="middle" class="nomarpadng">
															    <div title="Surcharge on us">
																	&nbsp;
																	<s:property value="bankSurchargeAmountOnCommercial" />
																</div>
																<div class="cellborder" title="Surcharge off us">
																	&nbsp;
																	<s:property value="bankSurchargeAmountOffCommercial" />
																</div>
															</td>
																
															<td align="left" valign="middle" class="nomarpadng">
															    <div title="Surcharge on us">
																	&nbsp;
																   <s:property value="bankSurchargePercentageOnCustomer" />
																</div>
																<div class="cellborder" title="Surcharge off us">
																	&nbsp;
																	<s:property value="bankSurchargePercentageOffCustomer" />
																</div>
														    </td>
																	
															<td align="left" valign="middle" class="nomarpadng">
															    <div title="Surcharge on us">
																	&nbsp;
																	<s:property value="bankSurchargeAmountOnCustomer" />
																</div>
																<div class="cellborder" title="Surcharge off us">
																	&nbsp;
																	<s:property value="bankSurchargeAmountOffCustomer" />
																</div>
															</td>
															<td align="center" valign="middle"><s:checkbox
																	name="allowOnOff" value="allowOnOff"
																	onclick="return false" /></td>
																	
																	<td align="left" valign="middle"><s:property
																	value="status" /></td>
																	<td align="center" valign="middle"><s:div>
																	
																	<s:textfield id="edit%{#itStatus.count}" value="Edit"
																		type="button"
																		onclick="editSurchargeCurrentRow('%{key +'Div'}','%{#itStatus.count}', this,'cancelBtn%{key + #itStatus.count}')"
																		class="btn btn-info btn-xs" autocomplete="off"></s:textfield>

																
																</s:div></td>
															<td align="center" valign="middle" style="display: none"><s:property
																	value="id" /></td>
															<td align="center" valign="middle"><s:textfield
																	id="cancelBtn%{key + #itStatus.count}" value="Cancel"
																	type="button"
																	onclick="cancel('%{#itStatus.count}',this)" name = "cancelBtn"
																	class="btn btn-danger btn-xs" autocomplete="off"></s:textfield></td>
														</tr>
													</s:iterator>
												</table>
											</s:div>
										</div>
									</s:iterator>
								</div></td>
						</tr>
					</table></td>
			</tr>
			
			
			
			<tr>
				<td align="center" valign="top">
				<table width="98%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom: 2%;">
						<tr>
							<td align="left">
							<div id="datatable1" class="scrollD" style="display:none;">
						
									<s:iterator value="acquirerData" status="pay">
									<br>
									<h3 align="center">Surcharge Report</h3>
										<br>
										<div class="text-primary card-list-toggle"><strong><s:property
													value="key" /></strong></div> 
										<div class="scrollD card-list">
											<s:div id="%{key +'Div'}">
												<table width="100%" border="0" align="center"
													class="product-spec">
													<tr class="boxheading">
														<th width="5%" height="25" valign="middle"
															style="display: none">Payment</th>
															
														<th width="7%" align="left" valign="middle">MOP Type</th>
														<th width="7%" align="left" valign="middle">MOP Type</th>	
														<th width="7%" align="left" valign="middle">ON US/ OFF US</th>
														<th width="6%" align="left" valign="middle">Merchant Type</th>
														<th width="7%" align="left" valign="middle">PG % Commercial</th>
														<th width="7%" align="left" valign="middle">PG % Customer</th>
														<th width="6%" align="left" valign="middle">PG FC Commercial</th>
														<th width="6%" align="left" valign="middle">PG FC Customer</th>
														<th width="7%" align="left" valign="middle">Bank % Commercial</th>
														<th width="7%" align="left" valign="middle">Bank % Customer</th>
														<th width="6%" align="left" valign="middle">Bank FC Commercial</th>
														<th width="6%" align="left" valign="middle">Bank FC Customer</th>
														<th width="7%" align="left" valign="middle">Merchant % Commercial</th>
														<th width="7%" align="left" valign="middle">Merchant % Customer</th>
														<th width="7%" align="left" valign="middle">Merchant FC Commercial</th>
														<th width="7%" align="left" valign="middle">Merchant FC Customer</th>
														
													</tr>
													<s:iterator value="value" status="itStatus">
														<tr class="boxtext">
															<td align="left" valign="middle" style="display: none"><s:property
																	value="paymentType" /></td>
															<td align="left" valign="middle"><s:property
																	value="paymentsRegion" /></td>
															<td align="left" valign="middle"><s:property
																	value="mopType" /></td>
															<td align="left" valign="middle"><s:property
																	value="onOff" /></td>
															<td align="left" valign="middle"><s:property
																	value="merchantIndustryType" /></td>
															<td align="left" valign="middle"><s:property
																	value="pgSurchargePercentageCommercial" /></td>
															<td align="left" valign="middle"><s:property
																	value="pgSurchargePercentageCustomer" /></td>
															<td align="left" valign="middle"><s:property
																	value="pgSurchargeAmountCommercial" /></td>
															<td align="left" valign="middle"><s:property
																	value="pgSurchargeAmountCustomer" /></td>
															<td align="left" valign="middle"><s:property
																	value="bankSurchargePercentageCommercial" /></td>
															<td align="left" valign="middle"><s:property
																	value="bankSurchargePercentageCustomer" /></td>
															<td align="left" valign="middle"><s:property
																	value="bankSurchargeAmountCommercial" /></td>
															<td align="left" valign="middle"><s:property
																	value="bankSurchargeAmountCustomer" /></td>
															<td align="left" valign="middle"><s:property
																	value="merchantSurchargePercentageCommercial" /></td>
															<td align="left" valign="middle"><s:property
																	value="merchantSurchargePercentageCustomer" /></td>
															<td align="left" valign="middle"><s:property
																	value="merchantSurchargeAmountCommercial" /></td>
															<td align="left" valign="middle"><s:property
																	value="merchantSurchargeAmountCustomer" /></td>
															
														
														</tr>
													</s:iterator>
												</table>
											</s:div>
										</div>
									</s:iterator>
								</div></td>
						</tr>
					</table></td>
			</tr>
		</table>
	</div>
		
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
	
</body>
</html>