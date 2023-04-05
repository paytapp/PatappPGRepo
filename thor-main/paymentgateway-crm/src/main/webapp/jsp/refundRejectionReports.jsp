<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>
.heading{
   text-align: center;
    color: #002163;
    font-weight: bold;
    font-size: 22px;
}
.samefnew {
    width: 18.9% !important;
    float: left;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 10px;
}
.cust {
    width: 20% !important;
    float: left;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 0px !important;
}
.submit-button{
	width:10% !important;
	height:28px !important;
	margin-top:-4px !important;
}
.MerchBx {
    min-width: 92%;
    margin: 15px;
    margin-top: 25px !important;
    padding: 0;
}
table.dataTable thead .sorting {
    background: none !important;
}
.sorting_asc {
    background:none !important;
}
table.dataTable thead .sorting_desc {
    background: none !important;
}
table.dataTable thead .sorting {
     cursor: default !important;
}
table.dataTable thead .sorting_desc, table.dataTable thead .sorting {
    cursor: default !important;
}
.form-control {
    display: block;
    width: 100% !important;
    height: 28px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #555;
    background-color: #fff;
    background-image: none;
    border: 1px solid #ccc;
    border-radius: 4px;
    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    margin-left: 0% !important;
}
table.dataTable.display tbody tr.odd {
    background-color: #e6e6ff !important;
}
table.dataTable.display tbody tr.odd > .sorting_1{
	 background-color: #e6e6ff !important;
}	
.multiselect {
    width: 170px;
	display:block;
	margin-left:-4px;	
 }
  .selectBox {
  position: relative;
 }

#checkboxes {
  display: none;
  border: 1px #dadada solid;
  height:300px;
  overflow-y: scroll;
  position:Absolute;
  background:#fff;
  z-index:1;
  margin-left:-2px;
  width: 145% !important;
}

#checkboxes label {
  width: 74%;
}
#checkboxes input {
  width:18%;

}
.selectBox select {
  width: 145% !important;
  margin-top: -7px;
}
.download-btn {
	background-color:#002163;
	display: block;
    width: 100%;
    height: 30px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: white;
    border: 1px solid #ccc;
    border-radius: 4px;
	margin-top:65px;
}
#save-link:hover { 
   color: white; 
   text-decoration: none; 
   font-weight: none
 }
 #save-link:visited { 
   color: white; 
   text-decoration: none; 
   font-weight: none
 }
 .refundButtonCls{
	 background: #002163;
    border: none;
    text-align: right !important;
    color: white;
    border-radius: 3px;
    font-size: 12px;
 }
 #loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}
#loading-image {position: absolute;top: 40%;left: 55%;z-index: 100; width:10%;} 
</style>

<title>Refund Rejection Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/loader.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/moment.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<link href="../css/loader.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.popupoverlay.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<script src="../js/commanValidate.js"></script>
	<link href="../css/select2.min.css" rel="stylesheet" />
    <script src="../js/jquery.select2.js" type="text/javascript"></script>
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />
	
<script type="text/javascript">
$(document).ready(function(){
  $("body").addClass("loader--inactive");
	
  $("#merchant").select2();
});
</script>


</head>
<body id="mainBody">
	
	<h2 class="heading">Refund Rejection Report</h2>
	<br>
	<br>
	<table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    
	
     
            <table id="mainTable" width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="txnf">
		
		<tr>
			    <td colspan="5" align="left" valign="top">
				<div class="MerchBx">
	
					<div class="cust">
						Order ID:<br>
						<div class="txtnew">
							<s:textfield id="orderId" class="form-control" name="orderId"
								type="text" value="" autocomplete="off"
								onkeypress="return Validate(event);" style="width:95% !important;"></s:textfield>
						</div>
					</div>
					
					<div class="cust">
						Refund Order ID:<br>
						<div class="txtnew">
							<s:textfield id="refundOrderId" class="form-control" name="refundOrderId"
								type="text" value="" autocomplete="off" style="width:95% !important;" onkeypress="return Validate(event);">
						    </s:textfield>
						</div>
					</div>
					
					<div class="cust">
						Merchant:<br>
						<div class="txtnew">
							<s:if
								test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="ALL" headerValue="ALL" list="merchantList"
									listKey="payId" listValue="businessName" autocomplete="off" style="width:95% !important;"/>
							</s:if>
							<s:else>
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="ALL" headerValue="ALL" list="merchantList"
									listKey="emailId" listValue="businessName" autocomplete="off" style="width:95% !important;"/>
							</s:else>
						</div>
					</div>
					
					<div class="cust">
						Acquirer:<br>
						<div class="txtnew">
						        <s:select name="acquirer" class="form-control" id="acquirer"
									headerKey="ALL" headerValue="ALL" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
									listKey="code" listValue="name" autocomplete="off" style="width:95% !important;"/>
									
						</div>
					</div>
					
					<div class="cust">
						Payment Method:<br>
						<div class="txtnew">
							<s:select headerKey="ALL" headerValue="ALL" class="form-control"
										list="@com.paymentgateway.commons.util.PaymentType@values()"
										listValue="name" listKey="code" name="paymentType"
										id="paymentType" autocomplete="off" style="width:95% !important;"/>
						</div>
					</div>
					
					<div class="cust">
						Date From:<br>
						<div class="txtnew">
							<s:textfield type="text" id="dateFrom" name="dateFrom" class="form-control" autocomplete="off" readonly="true" style="width:95% !important;"/>
						</div>
					</div>
					
					<div class="cust">
						Date To:<br>
						<div class="txtnew">
							<s:textfield type="text" id="dateTo" name="dateTo" class="form-control" autocomplete="off" readonly="true" style="width:95% !important;"/>
						</div>
					</div>
					
				</div>
				
				<div>
					<div class="samefnew-btn">
							&nbsp;<br>
							<div class="form-group col-md-2 col-sm-3 txtnew col-xs-6">
								   <button class="download-btn" id="Submit" onClick="generateData()" >Submit</button>
								</div>
						</div>
				</div>
				
			</td>
		</tr>
		
		<tr>
				<td align="center" valign="top">
				<s:actionmessage id="success" class="success success-text" />
			
		</tr>
		
		            <tr>
						<td align="left" valign="top" style="padding: 10px; border-right: 1px solid #e0d2d2c4;"><br>
							<div class="scrollD">
								<table id="refundRejectionReport" align="center" class="display" cellspacing="0" width="100%">
									<thead>
										<tr class="boxheadingsmall" style="font-size: 11px;">
											<th style="text-align:left;" data-orderable="false">Order Id</th>
											<th style="text-align:left;" data-orderable="false">Pg Ref Num</th>
											<th style="text-align:left;" data-orderable="false">Refund Date</th>
											<th style="text-align:left;" data-orderable="false">Processed Date</th>
											<th style="text-align:left;" data-orderable="false">Refunded Amount</th>
											<th style="text-align:left;" data-orderable="false">Total Amount</th>
											<th style="text-align:left;" data-orderable="false">Refund Flag</th>
											<th style="text-align:left;" data-orderable="false">Refund Request Id</th>
											<th style="text-align:left;" data-orderable="false">Status</th>
											<th style="text-align:left; display:none;" data-orderable="false">Currency Code</th>
											<th style="text-align:left; display:none;" data-orderable="false">Pay Id</th>
											<th style="text-align:left;" data-orderable="false">Refund</th>
											
										</tr>
									</thead>
								<tbody id="tableContent">
									
								</tbody>
								<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
								</table>
							</div>
						</td>
					</tr>
		
		
	    </table>
		
                
    
        </td>
    </table>
	
<script type="text/javascript">
	$("#selectAll").click(function () {
     $('input:checkbox').not(this).prop('checked', this.checked);
 });
 </script>
 
<!--<script>
var tableData = [];
var table1 = $('#refundRejectionReport').DataTable();
 $('#refundRejectionReport tbody').on('click','td', function() {
		var columnIndex = table.cell(this).index().column;
		var rowIndex = table.cell(this).index().row;
		var rowNodes = table.row(rowIndex).node();
		var rowData = table.row(rowIndex).data();
		
		if(this.children[0].checked==true){
			tableData.push(rowData);
		}
		else{
			var oldTableData = tableData;
			tableData = [];
			for(index in oldTableData){
				var row = oldTableData[index];
				if(rowData[3]!=row[3]){
				tableData.push(row);
				}
			}
		}
	});

$('#save-link').click(function ()
{
  var customData = document.getElementById("myName").value;
  let exportData = tableData.join('\n');
  var file = new Blob([exportData], {type: 'text/plain'});
  var btn = $('#save-link');
  btn.attr("href", URL.createObjectURL(file));
  btn.prop("download", customData+".txt");
  
})

</script>---->


<script type="text/javascript">
var orderId;
var refundFlag;
var refundAmount;
var pgRefNum;
var refundDate;
var totalAmount;
var refundOrderId;

$(document).ready(function() {
			$('#refundRejectionReport tbody').on( 'click', 'td', function () {
				var dataRow = $(this).closest('tr')[0];
				if($('#refundRejectionReport thead tr th').eq($(this).index()).html().trim() == "Refund") {
					var data = table.row( $(this).parents('tr') ).data();
					var orderId = data.orderId;
					var refundFlag = data.refundFlag;
					var refundAmount = data.refundAmount;
					var pgRefNum = data.pgRefNum;
					var refundDate = data.refundDate;
					var totalAmount = data.totalAmount;
					var refundOrderId= data.refundOrderId;
					var currencyCode = data.currencyCode;
					var merchant = data.payId;
					refundDialog(data.orderId, data.refundFlag, data.refundAmount, data.pgRefNum, data.refundDate,data.totalAmount, data.refundOrderId,data.currencyCode, merchant);
				}
				else{
					return false;
				}
				
			});
})
</script>

<script type="text/javascript">
var table;
var rowValue;
	
	 table = $('#refundRejectionReport').DataTable({
		
		dom: 'BTrftlpi',
	               destroy : true,
	               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'Refund Rejection Report',
								exportOptions : {
									
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'Refund Rejection Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'Refund Rejection Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
				"searching": false,
				"paging": true,
                "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
                "pagingType": "full_numbers",
                "pageLength": 10,
	});
	
    function generateData()
    {
		if(!checkDate()) {
					return;
		}
		
		var orderId = document.getElementById("orderId").value;
		var refundOrderId = document.getElementById("refundOrderId").value;
		var paymentType = document.getElementById("paymentType").value;
		var acquirer = document.getElementById("acquirer").value;
		var merchant = document.getElementById("merchant").value;
		var dateFrom = document.getElementById("dateFrom").value;
		var dateTo = document.getElementById("dateTo").value;
		var token  = document.getElementsByName("token")[0].value;
		document.getElementById("loading").style.display = "block";
        table = $('#refundRejectionReport').DataTable({
            dom: 'BTrftlpi',			
		               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'Refund Rejection Report',
								exportOptions : {
									
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'Refund Rejection Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'Refund Rejection Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
			"searching": true,
			"paging": true,
            "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
            "pagingType": "full_numbers",
            "pageLength": 10,
			destroy: true,                                  
			/* "ajax": {                                    
				    "url": "refundRejectionReportAction",
				    "type": "POST",
				    "data": {
						"orderId":orderId,
					    "merchant":merchant,
					    "dateFrom": dateFrom,
					    "dateTo": dateTo,
					    token:token,
					    "struts.token.name": "token",
					},
						
				  }, */
			ajax: function (data, callback, settings) {
					$.ajax({
					 "url": "refundRejectionReportAction",
				    "type": "POST",
				    "timeout" : 0,
				    "data": {
						"orderId":orderId,
						"refundOrderId":refundOrderId,
					    "merchant":merchant,
					    "paymentType":paymentType,
					    "acquirer":acquirer,
					    "dateFrom": dateFrom,
					    "dateTo": dateTo,
					    token:token,
					    "struts.token.name": "token",
					},
					  success:function(data){
						callback(data);
						$("body").addClass("loader--inactive");;
					  },
					  error:function(data) {
						 $("body").addClass("loader--inactive");;
					  }
					});
				  },
				  
		"columns": [
				    { "data": "orderId" },
		            { "data": "pgRefNum" },
		            { "data": "refundDate"},
		            { "data": "processedDate"},
		            { "data": "refundAmount"},
		            { "data": "totalAmount" },
		            { "data": "refundFlag" },
					{ "data": "refundOrderId" },
					{ "data": "status" },
					{ "data": "currencyCode",
					  "visible": false
					},
					{ "data": "payId",
						  "visible": false
						},
					{
						"data": null,
						render: function () {
						return `<button type="button" id="refundButton" style="text-align:right !important;" class="refundButtonCls" >Refund</button>`;
									
						}
					}
					
		        ]
        });
      };   
		

</script>

<script>
function refundDialog(orderId, refundFlag, refundAmount, pgRefNum, refundDate,totalAmount,refundOrderId, currencyCode, merchant){
	if (confirm("Are you sure you want to refund the transaction?")) {
		var token  = document.getElementsByName("token")[0].value;
		document.getElementById("loading").style.display = "block";
		$.ajax({
			"url": "refundRejectionRefund",
			"type": "POST",
			"timeout:" : 0,
			"data": {
					"orderId" : orderId,
					"refundFlag" : refundFlag,
					"refundAmount" : refundAmount,
					"pgRefNum" : pgRefNum,
					"refundDate" : refundDate,
					"totalAmount" : totalAmount,
					"refundOrderId" : refundOrderId,
					"currencyCode" : currencyCode,
					"merchant" : merchant,
					token:token,
				    "struts.token.name": "token"
					},
			"success": function(data) {
				alert(data.response)
				$("body").addClass("loader--inactive");;
				generateData();
			},
			"error": function (data) {
				alert("Refund not initiated !!")
				$("body").addClass("loader--inactive");;
				generateData();
			}
	    });
		 
	}
}
</script>
<script type="text/javascript">
	   
	$(document).ready(function() {
		
		$(function() {
			$("#dateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#dateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));	

		});	
	});	
</script>

<script>
function checkDate(){
	var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
				var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}
			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}
			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}
			return true;
}
</script>

<script type="text/javascript">
$(document).ready(function(){
	$(document).click(function(){
		expanded = false;
		$('#checkboxes').hide();
	});
	$('#checkboxes').click(function(e){
		e.stopPropagation();
	});


});
</script>


</body>
</html> 