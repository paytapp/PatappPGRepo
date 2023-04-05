<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Search Nodal Transactions</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>

<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>

<script type="text/javascript">
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
			renderTable();
		});

		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			reloadTable();		
		});

		$(function(){
			var datepick = $.datepicker;
			var table = $('#txnResultDataTable').DataTable();
			$('#txnResultDataTable').on('click', 'td.my_class1', function() {
				var rowIndex = table.cell(this).index().row;
				var rowData = table.row(rowIndex).data();
				
				
			});
		});
	});

	function renderTable() {
	    var txnId = document.getElementById("txnId").value;
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transFrom == null || transTo == null) {
			alert('Enter date value');
			return false;
		}

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
		var token = document.getElementsByName("token")[0].value;

		
		 var buttonCommon = {
        exportOptions: {
            format: {
                body: function ( data, column, row, node ) {
                    // Strip $ from salary column to make it numeric
                    return column === 0 ? "'"+data : (column === 1 ? "'" + data: data);
                }
            }
        }
    };
	
		$('#txnResultDataTable').dataTable(
						{
							"footerCallback" : function(row, data, start, end, display) {
								var api = this.api(), data;
			
										
							},
							"columnDefs": [{ 
								className: "dt-body-right",
								"targets": [0,1,2,3,4,5,6,7,8,9]
							}],
								dom : 'BTrftlpi',
								buttons : [
										$.extend( true, {}, buttonCommon, {
											extend: 'copyHtml5',											
											exportOptions : {											
												columns : [':visible']
											},
										} ),
									$.extend( true, {}, buttonCommon, {
											extend: 'csvHtml5',
											title : 'Search Nodal Transactions',
											exportOptions : {
												
												columns : [':visible']
											},
										} ),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize: 'legal',
										//footer : true,
										title : 'Search Nodal Transactions',
										exportOptions : {
											columns: [':visible']
										},
										customize: function (doc) {
										    doc.defaultStyle.alignment = 'center';
					     					doc.styles.tableHeader.alignment = 'center';
										  }
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Search Nodal Transactions',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10]
									} ],

							"ajax" :{
								
								"url" : "nodalTransactionsSearch",
								"type" : "POST",
								"data": function (d){
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {
									 $("#submit").removeAttr("disabled");
									 $("body").addClass("loader--inactive");
							},
							 "searching": false,
							 "ordering": false,
							
							 "processing": true,
						        "serverSide": true,
						        "paginationType": "full_numbers", 
						        "lengthMenu": [[10, 25, 50], [10, 25, 50]],
								"order" : [ [ 2, "desc" ] ],
						       
						        "columnDefs": [
						            {
						            "type": "html-num-fmt", 
						            "targets": 4,
						            "orderable": true, 
						            "targets": [0,1,2,3,4,5,6,7,8,9,10]
						            }
						        ],
 
							"columns" : [ {
								"data" : "txnId",
								"className" : "text-class"
								
							},  {
								"data" : "oid",
								"className" : "text-class"
								
							},{
								"data" : "customerId",
								"className" : "text-class"
							},{
								"data" : "srcAccNo",
								"className" : "text-class"
							},{
								"data" : "beneficiaryCode",
								"className" : "text-class"
							}, {
								"data" : "acquirer",
								"className" : "text-class"
							}, {
								"data" : "paymentType",
								"className" : "text-class"
							}, {
								"data" : "txnType",
								"className" : "text-class"
							}, {
								"data" : "amount",
								"className" : "text-class",
							}, {
								"data" : "status",
								"className" : "text-class"
							}, {
								"data" : "createdDate",
								"className" : "text-class"
							}, {
								"data" : "comments",
								"className" : "text-class"
							},{
								"data" : null,
								"className" : "center",
								"width" : '8%',
								"orderable" : false,
								"mRender" : function(row) {
									return renderButton(row)
								}
							}]
						});
						
			
		
	}
	
	function renderButton(row){
		if((row.txnType).toUpperCase() == "STATUS" && (((row.status).toUpperCase() == "FAILED") || ((row.status).toUpperCase() == "SETTLED"))) {
										return '<button class="btn btn-info btn-xs btn-block" id="btnRefresh" onclick="refreshStatus(this)" style="font-size:10px; display:none;">Get Status </button>';
		} else {
										return '<button class="btn btn-info btn-xs btn-block" id="btnRefresh" onclick="refreshStatus(this)" style="font-size:10px;">Get Status </button>';
		}
	}

	function reloadTable() {
		var datepick = $.datepicker;
		var transFrom = $.datepicker
				.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transFrom == null || transTo == null) {
			alert('Enter date value');
			return false;
		}

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
		$("#submit").attr("disabled", true);
		var tableObj = $('#txnResultDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var txnId = document.getElementsByName("txnId")[0].value.trim();
		var oid =   document.getElementsByName("oid")[0].value.trim();
		var	paymentType = document.getElementById("paymentType").value;
		var status = document.getElementById("status").value;
		
		
		if(paymentType==''){
			paymentType='ALL'
		}
		if(status==''){
			status='ALL'
		}
		if(txnId==''){
			txnId='ALL'
		}
		if(oid==''){
			oid='ALL'
		}

		
		var obj = {
			paymentType : paymentType,
			status : status,
			txnId : txnId,
			oid : oid,
			dateFrom : document.getElementById("dateFrom").value,
			dateTo : document.getElementById("dateTo").value,
			draw : d.draw,
			length :d.length,
			start : d.start, 
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}

	function popup(txnId) {
		
		var token = document.getElementsByName("token")[0].value;
		var myData = {
			token : token,
			"struts.token.name" : "token",
			"transactionId":txnId
		}
		$.ajax({
		    	url: "customerAddressAction",
		    	type : "POST",
		    	timeout: 0,
		    	data :myData,
		    	success: function(response){
					var responseObj =  response.aaData;
					
						
					$('#sec1 td').eq(0).text(responseObj.custName ? responseObj.custName : 'Not Available');
					$('#sec1 td').eq(1).text(responseObj.custPhone ? responseObj.custPhone : 'Not Available');
					$('#sec1 td').eq(2).text(responseObj.custCity ? responseObj.custCity : 'Not Available');

					$('#sec2 td').eq(0).text(responseObj.custState ? responseObj.custState : 'Not Available');
					$('#sec2 td').eq(1).text(responseObj.custCountry ? responseObj.custCountry : 'Not Available');
					$('#sec2 td').eq(2).text(responseObj.custZip ? responseObj.custZip : 'Not Available');

					$('#address1 td').text(responseObj.custStreetAddress1 ? responseObj.custStreetAddress1 : 'Not Available');
					$('#address2 td').text(responseObj.custStreetAddress2 ? responseObj.custStreetAddress2 : 'Not Available');
				
					$('#sec3 td').eq(0).text(responseObj.custShipName ? responseObj.custShipName : 'Not Available');
					$('#sec3 td').eq(1).text(responseObj.custShipPhone ? responseObj.custShipPhone : 'Not Available');
					$('#sec3 td').eq(2).text(responseObj.custShipCity ? responseObj.custShipCity : 'Not Available');

					$('#sec4 td').eq(0).text(responseObj.custShipState ? responseObj.custShipState : 'Not Available');
					$('#sec4 td').eq(1).text(responseObj.custShipCountry ? responseObj.custShipCountry : 'Not Available');
					$('#sec4 td').eq(2).text(responseObj.custShipZip ? responseObj.custShipZip : '');

					$('#address3 td').text(responseObj.custShipStreetAddress1 ? responseObj.custShipStreetAddress1 : 'Not Available');
					$('#address4 td').text(responseObj.custShipStreetAddress2 ? responseObj.custShipStreetAddress2 : 'Not Available');
					
					$('#auth td').text(responseObj.internalTxnAuthentication ? responseObj.internalTxnAuthentication : 'Not Available');
					
				$('#popup').show();
		    	},
		    	error: function(xhr, textStatus, errorThrown){
			       alert('request failed for bookers');
			    }
		});

	};
	
	
	function refreshStatus(val) {
		
		var rowData = val.parentElement.parentElement;
		var oid = rowData.children[1].innerText.trim();
		var custId = rowData.children[2].innerText.trim();
		var acquirer = rowData.children[5].innerText.trim();
		var token = document.getElementsByName("token")[0].value;
		$("body").removeClass("loader--inactive");
		document.getElementById("btnRefresh").disabled=true;
		$
		.ajax({
			url : "refreshTransactionStatus",
			type : "POST",
			data : {
				oid : oid,
				custId : custId,
				token : token,
				acquirer : acquirer,
				"struts.token.name" : "token",
			},
			success : function(data) {
			
			var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);
                    $("body").removeClass("loader--inactive");
                    window.location.reload();					
				}
				reloadTable();
			},
			error : function(data) {
				$("body").removeClass("loader--inactive");
                    window.location.reload();
				reloadTable();
			}
		});
	};
</script>

<script>
function checkRefNo(){
	var refValue = document.getElementById("pgRefNum").value;
	var refNoLength = refValue.length;
	if((refNoLength <16) && (refNoLength >0)){
		document.getElementById("submit").disabled = true;
		document.getElementById("validRefNo").style.display = "block";
	}
	else if(refNoLength == 0){
		document.getElementById("submit").disabled = false;
		document.getElementById("validRefNo").style.display = "none";
	}else{
		document.getElementById("submit").disabled = false;
        document.getElementById("validRefNo").style.display = "none";
	}
}
</script>

<style type="text/css">
.cust {width: 24%!important; margin:0 5px !important; /*font: bold 10px arial !important;*/}
.samefnew{
	width: 19.5%!important;
    margin: 5px 5px !important;
    /*font: bold 10px arial !important;*/
}
.btn {padding: 3px 7px!important; font-size: 12px!important; }
.samefnew-btn{
    width: 12%;
    float: left;
    font: bold 11px arial;
    color: #333;
    line-height: 22px;
    margin-top: 5px;
}
/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
tr td.my_class1{
	cursor: pointer;
	text-decoration: none !important;
}
tr td.my_class1:hover{
	cursor: pointer !important;
	text-decoration: none !important;
}

tr th.my_class1:hover{
	color: #fff !important;
}

.cust .form-control, .samefnew .form-control{
	margin:0px !important;
	width: 95%;
}
.select2-container{
	width: 100% !important;
}
.clearfix:after{
	display: block;
	visibility: hidden;
	line-height: 0;
	height: 0;
	clear: both;
	content: '.';
}
#popup{
	position: fixed;
	top:0px;
	left: 0px;
	background: rgba(0,0,0,0.7);
	width: 100%;
	height: 100%;
	z-index:999; 
	display: none;
}
.innerpopupDv{
	width: 600px;
    margin: 60px auto;
    background: #fff;
    padding-left: 5px;
    padding-right: 15px;
    border-radius: 10px;
}
.btn-custom {
    margin-top: 5px;
    height: 27px;
    border: 1px solid #5e68ab;
    background: #5e68ab;
    padding: 5px;
    font: bold 12px Tahoma;
    color: #fff;
    cursor: pointer;
    border-radius: 5px;
}
#loader-wrapper .loader-section.section-left, #loader-wrapper .loader-section.section-right{
	background: rgba(225,225,225,0.6) !important;
	width: 50% !important;
}
.invoicetable{
	float: none;
}
.innerpopupDv h2{
	font-size: 12px;
    padding: 5px;
}

table.dataTable.display tbody tr.odd {
    background-color: #e6e6ff !important;
}

.my_class1 {
    color: #0040ff !important;
    text-decoration: none !important;
    cursor: pointer;
    *cursor: hand;
}
.my_class {
    color: white !important;
}
.text-class{
	text-align: center !important;
}
.download-btn {
	background-color:#002163;
	display: block;
    width: 100%;
    height: 30px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
	margin-top:30px;
}

</style>
</head>
<body id="mainBody">
	
   <div style="overflow:scroll !important;">
	<table id="mainTable" width="100%" border="0" align="center"
		cellpadding="0" cellspacing="0" class="txnf">
		<tr>
			<td colspan="5" align="left"><h2>Search Nodal Transactions</h2></td>
		</tr>
		<tr>
			<td colspan="3" align="left" valign="top"><div class="MerchBx">
					<div class="clearfix">
						<div class="cust">
						Txn Id :<br>
						<div class="txtnew ">
							<s:textfield id="txnId" class="form-control"
								name="txnId" type="text" value="" autocomplete="off"
								onkeypress="javascript:return isNumber (event)" maxlength="16" onblur="checkRefNo()"></s:textfield>
						</div>
						<span id="validRefNo" style="color:red; display:none;">Please Enter 16 Digit Txn Id</span>
					</div>

					<div class="cust">
						OID:<br>
						<div class="txtnew">
							<s:textfield id="oid" class="form-control" name="oid"
								type="text" value="" autocomplete="off"
								onkeypress="return Validate(event);"></s:textfield>
						</div>
					</div>

					
					
					<div class="cust">
						Status:<br>
						<div class="txtnew">
								<s:select headerKey="" headerValue="ALL" class="form-control"
								list="@com.paymentgateway.commons.util.SettlementStatusType@values()"
								listValue="name" listKey="name" name="status"
								id="status" autocomplete="off" value="" />
						</div>
					</div>
					
					<div class="cust">
						Payment Type:<br>
						<div class="txtnew">
							<s:select headerKey="" headerValue="ALL" class="form-control"
								list="@com.paymentgateway.commons.util.NodalPaymentTypes@values()"
								listValue="name" listKey="name" name="paymentType"
								id="paymentType" autocomplete="off" value="" />
						</div>
					</div>
					
					</div>


					<div class="clearfix">

					<div class="cust">
						Date From:<br>
						<div class="txtnew">
							<s:textfield type="text" id="dateFrom" name="dateFrom" class="form-control" autocomplete="off" readonly="true" />
						</div>
					</div>

					<div class="cust">
						Date To:<br>
						<div class="txtnew">
							<s:textfield type="text" id="dateTo" name="dateTo" class="form-control" autocomplete="off" readonly="true" />
						</div>
					</div>

					<div class="samefnew-btn">
						&nbsp;<br>
						<div class="txtnew">
							<input type="button" id="submit" value="Submit"
								class="btn btn-sm btn-block btn-success" style="margin-left: 7% !important; margin-top: -2%;">
								
						</div>
					</div>
					</div>
				</div>

			</td>
		</tr>
		<tr>
			<td colspan="5" align="left"><h2>&nbsp;</h2></td>
		</tr>
		<tr>
			<td align="left" style="padding: 10px;">
				<div class="scrollD">
					<table id="txnResultDataTable" class="display nowrap" cellspacing="0"
						width="100%">
						<thead>
							<tr class="boxheadingsmall">
								<th style='text-align: center'>Txn Id</th>
								<th style='text-align: center'>OID</th>
								<th style='text-align: center'>Customer ID</th>
								<th style='text-align: center'>Nodal Acc No</th>
								<th style='text-align: center'>Bene Code</th>
								<th style='text-align: center'>Acquirer</th>
								<th style='text-align: center'>Payment Type</th>
								<th style='text-align: center'>Txn Type</th>
								<th style='text-align: center'>Amount</th>
								<th style='text-align: center'>Status</th>
								<th style='text-align: center'>Date</th>
								<th style='text-align: center'>Comments</th>
								<th style='text-align: center' >Action</th>
							
							</tr>
						</thead>
						<tfoot>
							<tr class="boxheadingsmall">
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								
							</tr>
						</tfoot>
					</table>
				</div>
			</td>
		</tr>

	</table>
  </div>
		


</body>
</html>