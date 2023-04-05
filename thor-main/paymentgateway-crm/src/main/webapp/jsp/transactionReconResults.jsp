
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Search Transaction</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/defualt-style.css">
	<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<script src="../js/jquery.min.js" type="text/javascript"></script>
	<script src="../js/moment.js" type="text/javascript"></script>
	<script src="../js/daterangepicker.js" type="text/javascript"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
	<script src="../js/pdfmake.js" type="text/javascript"></script>
	<script src="../js/jquery.fancybox.min.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/common-scripts.js"></script>
	<style>
		.d-none { display: none !important; }
		.flex-column { flex-direction: column; }
		.d-flex { display: flex; }
		.fancybox-content { border-radius: 6px !important; }
	</style>

	<script type="text/javascript">
		

		$(document).ready(function() {

			$(function() {
				renderTable();
			});
			
				$("#submit").click(function(env) {
				$("body").removeClass("loader--inactive");
				$("#setData").val("");
				reloadTable();		
			});
				
				$("#downlaod").click(function(env) {
					$("body").removeClass("loader--inactive");
					$("#setData").val("");
					reloadTable();		
				});

		}); 

		
		function downloadFile() {
			
			var token = document.getElementsByName("token")[0].value;
			var acquirer = document.getElementById("acquirer").value;
			var	transactionType = document.getElementById("transactionType").value;
			var status = document.getElementById("status").value;
			var reservationId = document.getElementById("reservationId").value;
			var bankTxnId = document.getElementById("bankTxnId").value;
			var dateFrom = document.getElementById("dateFrom").value;
			var dateTo = document.getElementById("dateTo").value;
			
			
			if(transactionType == '') {
				transactionType = 'ALL';
			}

			if(status == ''){
				status = 'ALL';
			}
			
			document.getElementById("acquirerFrm").value = acquirer;
			document.getElementById("transactionTypeFrm").value = transactionType;
			document.getElementById("statusFrm").value = status;
			document.getElementById("reservationIdFrm").value = reservationId;
			document.getElementById("bankTxnIdFrm").value = bankTxnId;
			document.getElementById("dateFromFrm").value = dateFrom;
			document.getElementById("dateToFrm").value = dateTo;
			
			document.getElementById("reconDownloadForm").submit();
			
			
		}
		
		
		function renderTable() {
			var table = new $.fn.dataTable.Api('#txnResultDataTable');
			
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				$("body").addClass("loader--inactive");
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}
			if (transTo - transFrom > 31 * 86400000) {
				$("body").addClass("loader--inactive");
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}
			var token = document.getElementsByName("token")[0].value;
			//$("body").addClass("loader--inactive");
			
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
								
								
					},
					"columnDefs": [{ 
						className: "dt-body-right",
						"targets": [0,1,2,3,4,5,6,7,8,9,10]
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
									title : 'Search Transactions',
									exportOptions : {
										
										columns : [':visible']
									},
								} ),
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								pageSize: 'legal',
								//footer : true,
								title : 'Search Transactions',
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
								title : 'Search Transactions',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'colvis',
								columns : [ 0,1,2,3,4,5,6,7,8,9,10]
							} ],

					"ajax" :{
						
						"url" : "transactionReconSearchAction",
						"type" : "POST",
						"data": function (d){
							return generatePostData(d);
						},
						"async": true,
						"error": function (xhr, error, code)
						{
						},
					},
					"fnDrawCallback" : function() {
							$("#submit").removeAttr("disabled");
							
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
							},
							{
						'targets': 0,
						'createdCell':  function (td, cellData, rowData, row, col) {
						}
						}
						],

					"columns" : [ {
						"data" : "reservationId",
						"className" : " text-class"
					},  {
						"data" : "bankTxnId",
						"className" : " text-class"
						
					},
					{
						"data" : "sid",
						"className" : "text-class"
					},
					
					{
						"data" : "amount",
						"className" : "text-class"
					}, {
						"data" : "txnType",
						"className" : " text-class"
					},{
						"data" : "status",
						"className" : " text-class"
					},
					{
						"data" : "acquirer",
						"className" : " text-class"
					},
					{
						"data" : "createDate",
						"className" : " text-class"
					},
					{
						"data" : "settlementDate",
						"className" : " text-class"
					},
					{
						"data" : "settlementFlag",
						"className" : " text-class"
					},
					{
						"data" : "postSettledFlag",
						"className" : " text-class"
					}]
				});
							

			setTimeout(function(e){
				$("body").addClass("loader--inactive");
			}, 1000);		
		}

		function reloadTable() {
			var datepick = $.datepicker;
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
			$("#submit").attr("disabled", false);
			var tableObj = $('#txnResultDataTable');
			var table = tableObj.DataTable();
			table.ajax.reload();
			setTimeout(function(e){
				$("body").addClass("loader--inactive");
			}, 1000);
		}

		function generatePostData(d) {
			var token = document.getElementsByName("token")[0].value;
			var acquirer = document.getElementById("acquirer").value;
			var	transactionType = document.getElementById("transactionType").value;
			var status = document.getElementById("status").value;
			if(transactionType == '') {
				transactionType = 'ALL';
			}

			
			if(status == ''){
				status = 'ALL';
			}
			
			var obj = {
				reservationId : document.getElementById("reservationId").value,
				bankTxnId : document.getElementById("bankTxnId").value,
				sid : document.getElementById("sid").value,
				acquirer : acquirer,
				transactionType : transactionType,
				status : status,
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

		
	</script>

	<style type="text/css">
		/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
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
			margin-top: 20px;
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


		.fancybox-content{
			padding: 0 !important;
		}


		td.my_class1 {
			color: #0040ff !important;
			text-decoration: none !important;
			cursor: pointer;
			*cursor: hand;
		}

		.text-class{
			text-align: center !important;
		}
	</style>
</head>
<body data-id="mainBody">
	<input type="hidden" id="setData">
	<section class="transaction-result lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Search Payment Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Bank Txn Id</label>
					<s:textfield
						id="bankTxnId"
						onkeyup="onlyAlphaNumeric(this)"
						class="lpay_input"
						name="bankTxnId"
						type="text"
						autocomplete="off"
						>
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Reservation Id</label>
					<s:textfield
						id="reservationId"
						class="lpay_input"
						name="reservationId"
						type="text"
						autocomplete="off">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">SID</label>
					<s:textfield
						id="sid"
						class="lpay_input"
						name="sid"
						type="text"
						autocomplete="off">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Acquirer</label>
					
					
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'AMEX':'AMEX','RUPAY':'RUPAY','IPAY':'IPAY','BOB':'BOB','AALAHABAD BANK':'AALAHABAD BANK','BANK OF MAHARASHTRA':'BANK OF MAHARASHTRA','CORPORATION BANK':'CORPORATION BANK','HDFC':'HDFC','INDUSIND BANK':'INDUSIND BANK','KARUR BANK':'KARUR BANK','OBC':'OBC','PUNJAB NATIONAL BANK':'PUNJAB NATIONAL BANK'}"
						name="acquirer"
						id="acquirer"
						autocomplete="off"
						value="All"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>

			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Transaction Type</label>
					<s:select
					
						headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'SALE':'SALE','REFUND':'REFUND'}"
						name="transactionType"
						id="transactionType"
						autocomplete="off"
						value="All"
						
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Status</label>
					<s:select
					
					headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'Captured':'Captured','Settled':'Settled','Captured and Settled':'Captured and Settled','Captured-Not Settled':'Captured-Not Settled','Post Settle Captured':'Post Settle Captured'}"
						name="status"
						id="status"
						autocomplete="off"
						value="All"
						
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			
			
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Capture Date From</label>
					<s:textfield type="text" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Capture Date To</label>
					<s:textfield type="text" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
					<input type="button" id="download" value="Download" onClick = "downloadFile()" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="transaction-result lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Search Transaction List </h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" cellpadding="0" cellspacing="0" class="display" style="white-space: nowrap;" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th style='text-align: center'>Reservation Id</th>
								<th style='text-align: center'>Bank Txn Id</th>
								<th style='text-align: center'>SID</th>
								<th style='text-align: center'>Amount</th>
								<th style='text-align: center'>TxnType</th>
								<th style='text-align: center'>Status</th>
								<th style='text-align: center'>Acquirer</th>
								<th style='text-align: center'>Capture Date</th>
								<th style='text-align: center'>Settle Date</th>
								<th style='text-align: center'>Settled Flag</th>
								<th style='text-align: center'>Post Settled Flag</th>
							</tr>
						</thead>
					</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
		
	<s:form name="reconDownloadForm" id="reconDownloadForm" action="transactionReconDownloadAction">
		<s:hidden name="reservationId" id="reservationIdFrm" value="" />
		<s:hidden name="bankTxnId" id="bankTxnIdFrm" value="" />
		<s:hidden name="transactionType" id="transactionTypeFrm" value="" />
		<s:hidden name="status" id="statusFrm" value="" />
		<s:hidden name="dateFrom" id="dateFromFrm" value="" />
		<s:hidden name="dateTo" id="dateToFrm" value="" />
		<s:hidden name="acquirer" id="acquirerFrm" value="" />
		<s:hidden name="sid" id="sidFrm" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
	
</body>
</html>