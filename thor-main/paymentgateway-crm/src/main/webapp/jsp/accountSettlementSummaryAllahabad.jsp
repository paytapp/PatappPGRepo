<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Allahabad Bank Settlement Summary</title>
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
			var acquirer = "ALLAHABAD BANK";
			var dateFrom = document.getElementById("dateFrom").value;
			var dateTo = document.getElementById("dateTo").value;
			
			document.getElementById("acquirerFrm").value = acquirer;
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
						total = api.column(1).data().reduce(
								function(a, b) {
									return intVal(a) + intVal(b);
								}, 0);

						// Total over this page
						pageTotal = api.column(1, {
							page : 'current'
						}).data().reduce(function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

						// Update footer
						$(api.column(1).footer()).html(
								'' + pageTotal.toFixed(2) + ' ' + ' ');
								
								
					},
					"columnDefs": [{ 
						className: "dt-body-right",
						"targets": [0,1,2,3,4,5,6]
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
									title : 'Transactions',
									exportOptions : {
										
										columns : [':visible']
									},
								} ),
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								pageSize: 'legal',
								//footer : true,
								title : 'Transactions',
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
								title : 'Transactions',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'colvis',
								columns : [ 0,1,2,3,4,5,6 ]
							} ],

					"ajax" :{
						
						"url" : "accountSettlementSummaryAllahabad",
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
							"targets": [0,1,2,3,4,5,6 ]
							},
							{
						'targets': 0,
						'createdCell':  function (td, cellData, rowData, row, col) {
						}
						}
						],

					"columns" : [ {
						"data" : "acquirer",
						"className" : " text-class"
					}, {
						"data" : "settlementDate",
						"className" : " text-class"
						
					},
					{
						"data" : "saleCount",
						"className" : "text-class"
					},
					
					{
						"data" : "saleAmount",
						"className" : "text-class"
					}, {
						"data" : "refundCount",
						"className" : " text-class"
					},{
						"data" : "refundAmount",
						"className" : " text-class"
					},
					{
						"data" : "totalAmount",
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
			var acquirer = "ALLAHABAD BANK";
			var obj = {
				acquirer : acquirer,
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
					<h2 class="heading_text">Allahabad Bank Settlement Summary</h2>
				</div>
				<!-- /.heading_icon -->
			</div>

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
					<!-- <input type="button" id="download" value="Download" onClick = "downloadFile()" class="lpay_button lpay_button-md lpay_button-secondary"> -->
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
					<h2 class="heading_text">Allahabad Bank Settlement Summary </h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" cellpadding="0" cellspacing="0" class="display" style="white-space: nowrap;" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th style='text-align: center'>Acquirer</th>
								<th style='text-align: center'>Capture Date</th>
								<th style='text-align: center'>Sale Count</th>
								<th style='text-align: center'>Sale Amount</th>
								<th style='text-align: center'>Refund Count</th>
								<th style='text-align: center'>Refund Amount</th>
								<th style='text-align: center'>Settlement Amount</th>
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
		
	<s:form name="reconDownloadForm" id="reconDownloadForm" action="transactionReconExceptionDownloadAction">
		<s:hidden name="transactionType" id="transactionTypeFrm" value="" />
		<s:hidden name="dateFrom" id="dateFromFrm" value="" />
		<s:hidden name="dateTo" id="dateToFrm" value="" />
		<s:hidden name="acquirer" id="acquirerFrm" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
	
</body>
</html>