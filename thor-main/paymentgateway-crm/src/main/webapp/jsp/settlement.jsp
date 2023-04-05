<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Sale Captured</title>
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

<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>

<script type="text/javascript">
$(document).ready(function(){
 
  // Initialize select2
  $("#merchant").select2();
});
</script>


<%-- <script type="text/javascript">
function RefreshMetaTag()
{
	 var metaTag = document.getElementsByTagName("meta");
	    var i;
	    for (i = 0; i < metaTag.length; i++) {
	        if (metaTag[i].getAttribute("http-equiv")=='refresh')
	        {
              alert("metatag");
              $('meta[http-equiv=refresh]').remove();
               $('head').append('<meta http-equiv="refresh" content="25;url=redirectLogin" />' );
	           
	         }
	        
	    } 
}
</script> --%>

=======

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
			reloadTable();		
		});

		$(function(){
			var datepick = $.datepicker;
			var table = $('#txnResultDataTable').DataTable();
			$('#txnResultDataTable').on('click', 'td.my_class', function() {
				var rowIndex = table.cell(this).index().row;
				var rowData = table.row(rowIndex).data();
			
				popup(rowData.oId);
			});
		});
	});

	function renderTable() {
		  var merchantEmailId = document.getElementById("merchant").value;
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		
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

								// Remove the formatting to get integer data for summation
								var intVal = function(i) {
									return typeof i === 'string' ? i.replace(/[\,]/g, '') * 1: typeof i === 'number' ? i : 0;
								};

								// Total over all pages
								total = api.column(10).data().reduce(
										function(a, b) {
											return intVal(a) + intVal(b);
										}, 0);

								// Total over this page
								pageTotal = api.column(10, {
									page : 'current'
								}).data().reduce(function(a, b) {
									return intVal(a) + intVal(b);
								}, 0);

								// Update footer
								$(api.column(10).footer()).html(
										'' + pageTotal.toFixed(2) + ' ' + ' ');
										
										
								// Total over all pages
								total = api.column(9).data().reduce(
										function(a, b) {
											return intVal(a) + intVal(b);
										}, 0);

								// Total over this page
								pageTotal = api.column(9, {
									page : 'current'
								}).data().reduce(function(a, b) {
									return intVal(a) + intVal(b);
								}, 0);

								// Update footer
								$(api.column(9).footer()).html(
										'' + pageTotal.toFixed(2) + ' ' + ' ');
							},
							"columnDefs": [{ 
								className: "dt-body-right",
								"targets": [1,2,3,4,5,6,7,8,9,10,11,12]
							}],
								dom : 'BTrftlpi',
								buttons : [
										$.extend( true, {}, buttonCommon, {
											extend: 'copyHtml5',											
											exportOptions : {											
												columns : [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
											},
										} ),
									$.extend( true, {}, buttonCommon, {
											extend: 'csvHtml5',
											title : 'Sale Transaction Report',
											exportOptions : {
												
												columns : [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
											},
										} ),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize: 'legal',
										//footer : true,
										title : 'Sale Transaction Report',
										exportOptions : {
											columns: [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
										},
										customize: function (doc) {
										    doc.defaultStyle.alignment = 'center';
					     					doc.styles.tableHeader.alignment = 'center';
										  }
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Sale Transaction Report',
										exportOptions : {
											columns : [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
										}
									},
									{
										extend : 'colvis',
										columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
									} ],

							"ajax" :{
								
								"url" : "saleTransactionSearchAction",
								"type" : "POST",
								"data": function (d){
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {

                                    // `();

									 $("#submit").removeAttr("disabled");
							},
							 "searching": false,
							 "ordering": false,
							 "language": {
								"processing": `<div id="loader-wrapper">
												<div id="loader"></div>
												<div class="loader-section section-left"></div>
												<div class="loader-section section-right"></div>
												</div>`
								},
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
						            "targets": [0,1,2,3,4,5,6,7,8,9,10,11,12,13]
						            }
						        ], 

 
							"columns" : [ {
								"data" : "transactionId",
								"className" : "txnId my_class text-class",
								"width": "2%" 
							},  {
								"data" : "pgRefNum",
								"className" : "payId text-class"
								
							},{
								"data" : "merchants",
								"className" : "text-class"
							}, {
								"data" : "dateFrom",
								"className" : "text-class"
							}, {
								"data" : "orderId",
								"className" : "orderId text-class"
							}, {
								"data" : "mopType",
								"visible" : false,
								"className" : "displayNone text-class"
							}, {
								"data" : "paymentMethods",
								"render" : function(data, type, full) {
									return full['paymentMethods'] + ' ' + '-'
											+ ' ' + full['mopType'];
								},
								"className" : "text-class"
							}, {
								"data" : "txnType",
								"className" : "txnType text-class"
							}, {
								"data" : "status",
								"className" : "status text-class"
							}, {
								"data" : "amount",
								"className" : "text-class"
							}
							, {
								"data" : "totalAmount",
								"className" : "text-class"
							}, {
								"data" : "postSettledFlag",
								"className" : "text-class"
							},{
								"data" : "payId",
								"visible" : false
								
							},
							{
								"data" : null,
								"className" : "center",
								"width" : '8%',
								"orderable" : false,
								"mRender" : function(row) {

									return '<button class="btn btn-info btn-xs btn-block" id="btnChargeBack"  style="font-size:10px;" >Chargeback</button>';

								}
							},
							
							
							{
								"data" : "productDesc",
								"visible" : false
							},  {
								"data" : null,
								"visible" : false,
								"className" : "displayNone",
								"mRender" : function(row) {
									return "\u0027" + row.transactionId;
								}
							}, {
								"data" : "internalCardIssusserBank",
								"visible" : false,
								"className" : "displayNone"
							}, {
								"data" : "internalCardIssusserCountry",
								"visible" : false,
								"className" : "displayNone"
							},
							 {
								"data" : "oId",
								"visible" : false,
								"className" : "displayNone"
							}]
						});
						
		$(document).ready(function() {

					var table = $('#txnResultDataTable').DataTable();
				$('#txnResultDataTable').on('click','.center',function(){
					var columnIndex = table.cell(this).index().column;
					var rowIndex = table.cell(this).index().row;
					var rowNodes = table.row(rowIndex).node();
					var rowData = table.row(rowIndex).data();
					var txnType1 = rowData.txnType;
					var status1 = rowData.status;	
					
						var payId1 =  rowData.pgRefNum;										
						var orderId1 = rowData.orderId; 					 
						var txnId1 = Number(rowData.transactionId); 
						document.getElementById('payIdc').value = payId1;
						document.getElementById('orderIdc').value = orderId1;
						document.getElementById('txnIdc').value = txnId1;
					    document.chargeback.submit();
					
			});
		});
		
			
		
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
			$('#dateFrom').focus();
			return false;
		}
		if (transTo - transFrom > 31 * 86400000) {
			alert('No. of days can not be more than 31');
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
		var merchantEmailId = document.getElementById("merchant").value;
		var	transactionType = document.getElementById("transactionType").value;
		var	paymentType = document.getElementById("paymentMethod").value;
		var status = document.getElementById("status").value;
		var currency = document.getElementById("currency").value;
		if(merchantEmailId==''){
			merchantEmailId='ALL'
		}
		if(transactionType==''){
			transactionType='ALL'
		}
		if(paymentType==''){
			paymentType='ALL'
		}
		if(status==''){
			status='ALL'
		}
		if(currency==''){
			currency='ALL'
		}
		
		var obj = {
			transactionId : document.getElementById("pgRefNum").value,
			orderId : document.getElementById("orderId").value,
			customerEmail : document.getElementById("customerEmail").value,
			merchantEmailId : merchantEmailId,
			transactionType : transactionType,
			paymentType : paymentType,
			status : status,
			currency : currency,
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
	width: 24%!important;
    margin: 0 5px !important;
    /*font: bold 10px arial !important;*/
}
.btn {padding: 3px 7px!important; font-size: 12px!important; }
.samefnew-btn{
    width: 15%;
    float: left;
    font: bold 11px arial;
    color: #333;
    line-height: 22px;
    margin-left: 5px;
}
/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
tr td.my_class{
	cursor: pointer;
}
tr td.my_class:hover{
	cursor: pointer !important;
}

tr th.my_class:hover{
	color: #fff !important;
}

.cust .form-control, .samefnew .form-control{
	margin:0px !important;
	width: 100%;
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
    margin: 80px auto;
    background: #fff;
    padding: 3px 10px;
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
.text-class{
	text-align: center !important;
}
.odd{
	background-color: #e6e6ff !important;
}
 
</style>
</head>
<body id="mainBody">
   <div style="overflow:scroll !important;">
	<table id="mainTable" width="100%" border="0" align="center"
		cellpadding="0" cellspacing="0" class="txnf">
		<tr>
			<td colspan="5" align="left"><h2>Fund Transfer</h2></td>
		</tr>
		<tr>
			<td colspan="5" align="left" valign="top">
					
					<div class="cust">
						Acquirer:<br>
						<div class="txtnew">
							<s:select headerKey="" headerValue="Select Acquirer" class="form-control"
								list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
								listValue="name" listKey="code" name="paymentMethod"
								id="paymentMethod" autocomplete="off" value="" />
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
					<table id="txnResultDataTable" class="" cellspacing="0"
						width="100%">
						<thead>
							<tr class="boxheadingsmall">
								<th style='text-align: center;text-decoration:none!important;'>Txn Id</th>
								<th style='text-align: center'>Pg Ref Num</th>
								<th style='text-align: center'>Merchant</th>
								<th style='text-align: center'>Date</th>
								<th style='text-align: center'>Order Id</th>
								<th style='text-align: center'>Payment Method</th>
								<th style='text-align: center'>Payment Method</th>
								<th style='text-align: center'>Txn Type</th>
								<th style='text-align: center'>Status</th>
								
								<th style='text-align: center' >Base Amount</th>
							 <th style='text-align: center' >Total Amount</th>
							  <th style='text-align: center' >Post Settled Flag</th>
							 <th style='text-align: center'>Chargeback</th>	
						
								<th>Chargeback</th>
								<th></th>
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
								<th></th>
								<th></th>
								<th style="text-align: right" rowspan="1" colspan="2"></th>
								<th style='text-align: right; float: right; padding-right: 8px;'></th>
							
							</tr>
						</tfoot>
					</table>
				</div>
			</td>
		</tr>

	</table>
  </div>
		
		<s:form name="chargeback" action="chargebackAction">
		<s:hidden name="orderId" id="orderIdc" value="" />
		<s:hidden name="payId" id="payIdc" value="" />
		
		<s:hidden name="txnId" id="txnIdc" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
	
	
<%-- 	<s:form name="refundDetails" action="refundConfirmAction">
		<s:hidden name="orderId" id="orderIdr" value="" />
		<s:hidden name="payId" id="payIdr" value="" />
		<s:hidden name="transactionId" id="txnIdr" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form> --%>
	
<script type="text/javascript">
$(document).ready(function(){
	$('#closeBtn').click(function(){
		$('#popup').hide();
	});
});
</script>	
</body>
</html>