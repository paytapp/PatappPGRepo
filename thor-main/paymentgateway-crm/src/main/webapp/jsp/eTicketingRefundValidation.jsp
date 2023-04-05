<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>E-Ticketing Refund Validation</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<!-- stylesheet -->
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/fonts.css"/>
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<!-- javascripts -->
<script src="../js/jquery-1.9.0.js"></script>
<script src="../js/jquery-migrate-1.2.1.js"></script>
<script src="../js/jquery-ui.min.js"></script>
<script src="../js/jquery.select2.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<!-- searchable select option -->
<script src="../js/select2.min.js"></script>
<link href="../css/select2.min.css" rel="stylesheet" />
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<script type="text/javascript">
	$(document).ready(function(){
		$("body").addClass("loader--inactive");
		 
		  // Initialize select2
		 $("#merchants").select2();
	});
</script>

<script type="text/javascript">
var filePathDwn;
var filePathRefresh;
var rowCount;

$(document).ready(function() {
$(function() {
				$("#requestDate").datepicker({
					prevText : "click for previous months",
					nextText : "click for next months",
					showOtherMonths : true,
					dateFormat : 'dd-mm-yy',
					selectOtherMonths : false,
					maxDate : new Date()
				});
				
				$(function() {
				var today = new Date();
				$('#requestDate').val($.datepicker.formatDate('dd-mm-yy', today));
			    });
			});
			

			$('#eTicketingRefundValidation tbody').on( 'click', 'td', function () {
				var dataRow = $(this).closest('tr')[0];
				if($('#eTicketingRefundValidation thead tr th').eq($(this).index()).html().trim() == "Download") {
					var data = validationTable.row( $(this).parents('tr') ).data();
					var filePathDwn = data.fileName;
					downloadFile(data.fileName);
				}
				
			else if(($('#eTicketingRefundValidation thead tr th').eq($(this).index()).html().trim() == "Refresh") && 
			dataRow.lastChild.className == "showBtn")
			{
					var data = validationTable.row( $(this).parents('tr') ).data();
					var filePathRefresh = data.fileName;
					refreshFile(data.fileName, data.fileVersion, data.versionType);
				}
				return false;
			});
})

function downloadFile(fileDownload){                   
	var validationMerchant = document.getElementById("merchants").value;
	    var refundRequetDate = document.getElementById("requestDate").value;
		var fileName = fileDownload;
	    var token  = document.getElementsByName("token")[0].value;
		var form = document.getElementById("refundValidationDownload");
		
         form.action = "refundValidationTicketingDownload";
		 form.innerHTML = "";
         form.innerHTML += ('<input type="hidden" name="validationMerchant" value="'+ validationMerchant +'">');
         form.innerHTML += ('<input type="hidden" name="refundRequetDate" value="'+ refundRequetDate +'">');
		 form.innerHTML += ('<input type="hidden" name="filename" value="'+ fileName +'">');
		 form.innerHTML += ('<input type="hidden" name="token" value="'+ token +'">');
		 document.getElementById("refundValidationDownload").submit();
}

function refreshFile(fileName, fileVersion, versionType){                  //AJAX ON REFRESH BUTTON
	var merchant = document.getElementById("merchants").value;
	var requestDate = document.getElementById("requestDate").value;
	var token  = document.getElementsByName("token")[0].value;
	$("body").removeClass("loader--inactive");;
	document.getElementById("refreshButton").disabled = true;
	document.getElementById("refreshButton").classList.add("randomDisable");
	$.ajax({
			"url": "refundValidationTicketingRefresh",
			"type": "POST",
			"data": {
					"validationMerchant":merchant,
			  		"refundRequetDate":requestDate,
				    "filename":fileName,
				    "version" : fileVersion,
				    "versionType" : versionType,
				    token:token,
				    "struts.token.name": "token"
					},
			"success": function(response) {
				alert(response.response)
				$("body").addClass("loader--inactive");;
				document.getElementById("refreshButton").disabled = false;
				document.getElementById("refreshButton").classList.remove("randomDisable");
				createData();
			},
			"error": function (response) {
				//alert(response.response)
				$("body").addClass("loader--inactive");;
				document.getElementById("refreshButton").disabled = false;
				document.getElementById("refreshButton").classList.remove("randomDisable");
				createData();
			   
			}
	});
}
</script>

<style>
.downLoadBtn{
	background:none!important;
    border:none !important; 
    padding:0!important;
    color:#2b6fd4 !important;
    text-decoration:underline;
    cursor:pointer;
}
input:focus,
select:focus,
textarea:focus,
button:focus {
	outline: none;
}
.box2{
	margin-top: 11px;
	padding: 11px;
}
.box1, .box3, .box4{
	padding: 11px;
}
.box1 .media-heading{
	text-align: center;
	font-size: 14px;
}
.box1 .media-left{
	padding: 10px 14px;
	width: 70px;
	height: 58px;
}
.box1 .media-body{
	width: 80%;
	background: #f4f2f2;
	padding-top: 4%;
}
.box2 input, .box2 select, .box2 button{
	font-size: 13px;
}
.box4 button{
		background: #5db85b;
		color: #fff;
		border: none;
		display: block;
		width: 200px;
		padding: 6px;
		margin: 0 auto;
		font-size: 13px;
}
.box2 select, .box2 input{
	border:1px solid #d4d4d4;
}
.mytable, .mytable thead th{
	text-align: center;
	font-size: 13px;
}
.mytable thead th{
	font-size: 14px;
}
.mytable thead{
	background: #002163;
	color: #fff;
}
.mainDiv.txnf h3{
	color: #002163;
	font-size: 16px;
	margin-top: 20px;
}
#checkboxes label {
	 width: 74%;
}
#checkboxes input {
	 width:18%;
}
#loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}

#loading-image {position: absolute;top: 40%;left: 45%;z-index: 100} 

.selectBox select {
	 width: 100%;
}
.overSelect {
	 position: absolute;
	 left: 0;
	 right: 0;
	 top: 0;
	 bottom: 0;
}
label{
	color: #333;
	font-size: 13px;
	padding-left: 4%;
}
#PaymentTypePerformance{
	padding: 11px;
}
.loader {
	 border: 16px solid #f3f3f3; /* Light grey */
	 border-top: 16px solid #3498db; /* Blue */
	 border-radius: 50%;
	 width: 120px;
	 height: 120px;
	 animation: spin 2s linear infinite;
}

@keyframes spin {
	 0% { transform: rotate(0deg); }
	 100% { transform: rotate(360deg); }
}
.baseClass{
    width: 100%;
    float: left;
    text-align: center;
}
.baseClass button{
    display: inline-block;
}
.submit1{
    float:left;
	background: #46a145;
    color: #fff;
    border: none;
    display: block;
    width: 200px;
    padding: 5px;
    margin-left: 13%;
    margin-top: 8%;
    border-radius: 5px;
    font-size: 13px;
}
.buttonType{
    float:left;
	background: #2b6fd4;
    color: #fff;
    border: none;
    display: block;
    width: 180px;
    padding: 5px;
    margin-left: 5%;
    margin-top: 8%;
    border-radius: 5px;
    font-size: 13px;
}
.buttonType:hover{
	/*background: #6b9ae1;*/
}
table.dataTable thead .sorting_asc {
    background: none !important;
}
#eTicketingRefundValidation_wrapper{
	width:98% !important;
	margin-left:10px !important;
	margin-top: 15px !important;
}
.form-control{
	width: 90% !important;
	margin-left: 10% !important;
}
.select2 select2-container select2-container--default select2-container--below select2-container--focus{
	width: 285px !important;
	margin-left: 32px !important;
}
.noneBtn{
	background-color: white !important;
	color: white !important;
	border: none !important;
	cursor: default !important;
	display: none !important;
}
.showBtn{
        background:url("../image/refreshicon.png") no-repeat center center;
        cursor:pointer !important;
}
table.dataTable.display tbody td {
	 background-color: white !important;
 }
 .saleSettlementSec{
	padding: 12px;
}
.boxSec{
	border: 1px solid #517bce;
	text-align: center;
	margin-bottom: 12px;
	color: #002163 ;
	cursor: default;
	transition:all 0.6s ease;
	margin-top:5%;
}
.boxSec h4{
	font-size: 25px;
}
.boxSec p{
	color: #002163;
	font-size: 14px;
	transition:all 0.6s ease;
}
button:disabled,
button[disabled]{
  border: none;
  background-color: #c0d4f2;
  color: #666666;
}
.randomDisable{
  cursor: not-allowed;
  border: none;
  background-color: #c0d4f2;
  color: #666666;
}
#loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}
#loading-image {position: absolute;top: 40%;left: 55%;z-index: 100; width:10%;} 
#eTicketingRefundValidation_length{
	margin-left: -15px !important;
}
</style>

</head>

<body>

	<div class="mainDiv txnf">
	 <h2>E-Ticketing Refund Validation</h2>
		 <div class="container">
			<div class="row box2">
				<div class="col-sm-4">
					<label style="margin-left:6%;">Merchant</label>
					<div class="txtnew" style="margin-left: 10% !important;">
						<s:if
							test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select name="merchant" class="form-control" id="merchants"
								headerKey="" headerValue="Select a merchant" list="merchantList"
								listKey="emailId" listValue="businessName" autocomplete="off" />
						</s:if>
						<s:else>
							<s:select name="merchant" class="form-control" id="merchants"
								headerKey="" headerValue="ALL" list="merchantList"
								listKey="emailId" listValue="businessName" autocomplete="off" />
						</s:else>
					</div>
				</div>
				<div class="col-sm-4">
					<label style="margin-left:6%;">Request Date</label>
					<div class="txtnew">
						<s:textfield type="text" id="requestDate" name="dateFrom" class="form-control" autocomplete="off" readonly="true"/>
					</div>
				</div>
				
				<div class="col-sm-4">
					<div class="txtnew">
						<button class="submit1" id="getBtn" onClick="createData()">Submit</button>
					</div>
				</div>
			</div>
			
			<div style="margin-top:10px;">
			   <div class="col-sm-3">
				<button class="buttonType" id="capturedBtn" style="margin-left:15%;" onClick="capturedData()" disabled>Captured</button>
			   </div>
			   
			   <div class="col-sm-3">
				<button class="buttonType" id="otherBtn" onClick="otherData()" disabled>Others</button>
			   </div>
			   
			   <div class="col-sm-3">
				<button class="buttonType" id="postSettled" onClick="postSettledData()" disabled>Post Settled</button>
			   </div>
			   
			   <div class="col-sm-3">
				<button class="buttonType" style="margin-left:-8%;" id="finalVersion" onClick="finalVersionData()" disabled>Final Version </button>
			   </div>
			</div>
			
<div class="saleSettlementSec" id="saleSettlementSec">
 		<div class="row">
		   <div class="col-sm-2"></div>
 			<div class="col-sm-4">
 				<div class="boxSec">
 					<h2 style="font-size:17px !important;">Total Processed:</h2><p id = "totalProcessed" class="media-heading" style=" font-size:17px !important; color:black !important;">0</p>
 				</div>
 			</div>
 			<div class="col-sm-4">
 				<div class="boxSec">
 					<h2 style="font-size:17px !important;">Left:</h2><p id = "left" class="media-heading" style="font-size:17px !important; margin-left:48% !important; color:black !important;">0</p>		
 				</div>
 			</div>
			<div class="col-sm-2"></div>
 		</div>
 	</div>			
		 </div>
		 
		<table>
		    <tr>
				<div class="scrollD">
					<table id="eTicketingRefundValidation" align="center" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="boxheadingsmall" style="font-size: 11px;">
									<th style="text-align:left;" data-orderable="false">Version</th>
									<th style="text-align:left;" data-orderable="false">Version Type</th>
									<th style="text-align:left;" data-orderable="false">Date</th>
									<th style="text-align:left;" data-orderable="false">No. of Txns </th>
									<th style="text-align:center; background:none;" data-orderable="false">File Name </th>
									<th style="text-align:center; background:none;" data-orderable="false">Download</th>
									<th style="text-align:center; background:none;" data-orderable="false">Refresh</th>
									
											
								</tr>
							</thead>
							<tbody id="tableContent">
									
							</tbody>
							
							<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
					</table>
				</div>
						
			</tr>
		</table>
	</div>
	
<script type="text/javascript">
function capturedData(){                  //AJAX ON CAPTURED BUTTON 
  if (confirm("Do you want to continue?")) {
    var validationMerchant = document.getElementById("merchants").value;
	var refundRequetDate = document.getElementById("requestDate").value;
	var token  = document.getElementsByName("token")[0].value;
	if(validationMerchant == "Select Merchant") {
		 alert("Please select a merchant");
		 return;
	 }
	 $("body").removeClass("loader--inactive");
	document.getElementById("capturedBtn").disabled = true;
	document.getElementById("capturedBtn").classList.add("randomDisable");
    $.ajax({
			"url": "refundValidationTicketingCaptured",
			"type": "POST",
			"data": {
				      "validationMerchant":validationMerchant,
					  "refundRequetDate":refundRequetDate,
					  token:token,
					    "struts.token.name": "token"
					},
			"success": function(response) {
				alert(response.response)
				document.getElementById("capturedBtn").disabled = false;
				document.getElementById("capturedBtn").classList.remove("randomDisable");
				$("body").addClass("loader--inactive");
				createData();
			},
			"error": function (response) {
				//alert(response.response);
				document.getElementById("capturedBtn").disabled = false;
				document.getElementById("capturedBtn").classList.remove("randomDisable");
				$("body").addClass("loader--inactive");
				createData();			   
			}
	});	 
  }
    
};

function otherData() {
		var validationMerchant = document.getElementById("merchants").value;
	    var refundRequetDate = document.getElementById("requestDate").value;
	    var token  = document.getElementsByName("token")[0].value;
	    if(validationMerchant == "Select Merchant") {
			 alert("Please select a merchant");
			 return;
		 }
		 var form = document.getElementById("refundValidationOthers");
         form.action = "refundValidationTicketingOthers";
		 form.innerHTML = "";
         form.innerHTML += ('<input type="hidden" name="validationMerchant" value="'+ validationMerchant +'">');
         form.innerHTML += ('<input type="hidden" name="refundRequetDate" value="'+ refundRequetDate +'">');
		 form.innerHTML += ('<input type="hidden" name="token" value="'+ token +'">');
		 document.getElementById("refundValidationOthers").submit();
		 //createData();
	}

function postSettledData(){                      //AJAX ON POST SETTLED BUTTON
  if (confirm("Do you want to continue?")) { 
    var merchant = document.getElementById("merchants").value;
	var requestDate = document.getElementById("requestDate").value;
	var token  = document.getElementsByName("token")[0].value;
    $("body").removeClass("loader--inactive");
	document.getElementById("postSettled").disabled = true;
	document.getElementById("postSettled").classList.add("randomDisable");
    $.ajax({
			"url": "refundValidationTicketingPostSettled",
			"type": "POST",
			"data": {
				"validationMerchant":merchant,
				  "refundRequetDate":requestDate,
				  token:token,
				    "struts.token.name": "token"
					},
			"success": function(response) {
				alert(response.response)
				$("body").addClass("loader--inactive");
				document.getElementById("postSettled").disabled = false;
				document.getElementById("postSettled").classList.remove("randomDisable");
				createData();
			},
			"error": function (data) {
				//alert(response.response)
				$("body").addClass("loader--inactive");
				document.getElementById("postSettled").disabled = false;
				document.getElementById("postSettled").classList.remove("randomDisable");
				createData();				
			}
	});
  }
   
};


function finalVersionData(){                          //AJAX ON FINAL VERSION BUTTON
   if (confirm("Do you want to continue?")) {  
    var merchant = document.getElementById("merchants").value;
	var requestDate = document.getElementById("requestDate").value;
	var token  = document.getElementsByName("token")[0].value;
    $("body").removeClass("loader--inactive");
	document.getElementById("finalVersion").disabled = true;
	document.getElementById("finalVersion").classList.add("randomDisable");
    $.ajax({
			"url": "refundValidationTicketingFinalVersion",
			"type": "POST",
			"data": {
				"validationMerchant":merchant,
				  "refundRequetDate":requestDate,
				  token:token,
				    "struts.token.name": "token"
					},
			"success": function(response) {
				alert(response.response)
				$("body").addClass("loader--inactive");
				document.getElementById("finalVersion").disabled = false;
				document.getElementById("finalVersion").classList.remove("randomDisable");
				createData();
			},
			"error": function (data) {
				//alert(response.response)
				$("body").addClass("loader--inactive");
				document.getElementById("finalVersion").disabled = false;
				document.getElementById("finalVersion").classList.remove("randomDisable");
				createData();
			}
	});
   }
    
};
</script>

<script type="text/javascript">
var validationTable;
	 validationTable  = $('#eTicketingRefundValidation').DataTable({
		
		dom: 'BTrftlpi',
	               destroy : true,
	               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'E-Ticketing Refund Validation',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'E-Ticketing Refund Validation',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								}
							},
							{
								extend : 'print',
								title : 'E-Ticketing Refund Validation',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								}
							}
						],
				"searching": true,
				"paging": true,
                "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
                "pagingType": "full_numbers",
                "pageLength": 10,
	});
	
function createData(){
	    //alert("button clicked");
		var merchant = document.getElementById("merchants").value;
			if(merchant == "Select a merchant" || merchant == ""){
				alert("Please Select A merchant")
				return;
		};
		 var requestDate = document.getElementById("requestDate").value;
		 var token  = document.getElementsByName("token")[0].value;
		 document.getElementById("getBtn").disabled = true;
		 document.getElementById("getBtn").classList.add("randomDisable");
		 $("body").removeClass("loader--inactive");
    validationTable = $('#eTicketingRefundValidation').DataTable({
		"columnDefs": [
				        {
							"className": "dt-center", 
						"targets": "[0, 1, 2, 3, 4, 5]"
						}
				],
            dom: 'BTrftlpi',			
		               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'E-Ticketing Refund Validation',
								exportOptions : {
									
									columns : ['0, 1, 2, 3, 4']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'E-Ticketing Refund Validation',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								}
							},
							{
								extend : 'print',
								title : 'E-Ticketing Refund Validation',
								exportOptions : {
									columns : ['0, 1, 2, 3, 4']
								}
							}
						],
			"searching": true,
			"paging": true,
            "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
            "pagingType": "full_numbers",
            "pageLength": 10,
			destroy: true,                                  
			ajax: function (data, callback, settings) {
					$.ajax({
					  "url": "refundValidationTicketing",
					  "type": "POST",
					  "data": {
				    	"validationMerchant":merchant,
						 "refundRequetDate":requestDate,
						 	token:token,
						    "struts.token.name": "token"
					},
					  success:function(data){
						  document.getElementById("getBtn").disabled = false;
						  document.getElementById("getBtn").classList.remove("randomDisable");
						  $("body").addClass("loader--inactive");
						callback(data);
					    rowCount = validationTable.page.info().recordsTotal;
						//alert(rowCount);
						document.getElementById('eTicketingRefundValidation').rows[rowCount].cells[6].classList.add('showBtn');
						document.getElementById('eTicketingRefundValidation').rows[rowCount].cells[6].style.background.disabled = true;
						calculateTotal(data.totalNumOfTxns);
						
						    if((data.totalNumOfTxns == 0)){
								 document.getElementById("capturedBtn").disabled = true;
								 document.getElementById("otherBtn").disabled = true;
		                         document.getElementById("postSettled").disabled = true;
								 document.getElementById("finalVersion").disabled = true;
							}else{
								document.getElementById("capturedBtn").disabled = false;
								 document.getElementById("otherBtn").disabled = false;
		                         document.getElementById("postSettled").disabled = false;
								 document.getElementById("finalVersion").disabled = false;
							}
							
							 if((rowCount > 0) && (data.totalNumOfTxns >0)){
								 document.getElementById("capturedBtn").disabled = true;
								 document.getElementById("otherBtn").disabled = false;
		                         document.getElementById("postSettled").disabled = false;
								 document.getElementById("finalVersion").disabled = false;
							 } else{
								 document.getElementById("capturedBtn").disabled = false;
								 document.getElementById("postSettled").disabled = true;
								 document.getElementById("otherBtn").disabled = false;
		                         document.getElementById("finalVersion").disabled = true;
							 }
					    
						var getFinalVal = validationTable.row(rowCount-1).data();
						var finalValue = getFinalVal.versionType;
							if(finalValue == "FinalVersion"){
								document.getElementById("finalVersion").disabled = true;
								document.getElementById("postSettled").disabled = true;
								document.getElementById("capturedBtn").disabled = true;
								document.getElementById("otherBtn").disabled = false;
							}else{
								document.getElementById("finalVersion").disabled = false;
								document.getElementById("postSettled").disabled = false;
								document.getElementById("capturedBtn").disabled = true;
								document.getElementById("otherBtn").disabled = false;
							}
						
					  },
					  error:function(data) {
						  document.getElementById("getBtn").disabled = false;
						  document.getElementById("getBtn").classList.remove("randomDisable");
						  $("body").addClass("loader--inactive");
					  }
					});
				  },
				  
		"columns": [
				    { "data": "fileVersion",
					  "width": "10%"
					},
					{
					  "data": "versionType"
					},
					{
					  "data": "createdDate",
					  "width": "15%",
					  "type":  "datetime",
					  "format": "MM-DD-YYYY h:mm"
					},
		            { "data": "noOfTxns"
					},
		            { "data": "fileName"
					},		            
					{
						"data": null,
						render: function () {
						return `<button type="button"  class="downLoadBtn" id="downloadButton" style="width:50% !important; margin-left:25% !important; font-size:115%;">Download</button>`;
									
						}
					},
					{
						"data": null,
						"width": "5%",
						render: function () {
						return `<button type="button"  class="noneBtn" id="refreshButton" style="text-align:right !important;"></button>`;
									
						}
					}
					
		        ]
        });
    };
</script>

<script>

function calculateTotal(totalNumOfTxns){
	var sum = 0;
	var table = $('#eTicketingRefundValidation').DataTable();
	for(i=0; i<rowCount; i++){
		var total = table.cells(i,3).data()[0];
		sum = parseInt(sum) + parseInt(total);
	}
	document.getElementById("left").innerHTML =  parseInt(totalNumOfTxns) - parseInt(sum);
	document.getElementById("totalProcessed").innerHTML = totalNumOfTxns;
}
</script>

<s:form name="refundValidationOthers" id = "refundValidationOthers" action="" method="post" target="_parent"></s:form>

<s:form name="refundValidationDownload" id = "refundValidationDownload" action="" method="post" target="_parent"></s:form>
 
</body>
</html>	