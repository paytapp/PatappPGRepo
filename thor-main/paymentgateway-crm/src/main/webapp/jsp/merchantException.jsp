<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Merchant Exception</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>

<script type="text/javascript">
	$(document).ready(function() {

		$(function() {
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
				
			});
		});
	});

	function renderTable() {
		  var merchantEmailId = document.getElementById("merchant").value;
		// var table = new $.fn.dataTable.Api('#txnResultDataTable');
		
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
                    // Strip $ from digit column to make it numeric
                	return column === 0 ? "'"+data : (column === 1 ? "'" + data: column === 2 ? "'" + data:data);
                }
            }
        }
    };
	
		$('#txnResultDataTable').dataTable(
						{
							/* "footerCallback" : function(row, data, start, end, display) {
								var api = this.api(), data;

								// Remove the formatting to get integer data for summation
								var intVal = function(i) {
									return typeof i === 'string' ? i.replace(/[\,]/g, '') * 1: typeof i === 'number' ? i : 0;
								};

								// Total over all pages
								total = api.column(13).data().reduce(
										function(a, b) {
											return intVal(a) + intVal(b);
										}, 0);

								// Total over this page
								pageTotal = api.column(13, {
									page : 'current'
								}).data().reduce(function(a, b) {
									return intVal(a) + intVal(b);
								}, 0);

								// Update footer
								$(api.column(13).footer()).html(
										'' + pageTotal.toFixed(2) + ' ' + ' ');
							}, */
							 "columnDefs": [{ 
								className: "dt-body-right",
								"targets": [1,2,3,4,5,6]
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
											title : 'Merchant Exception',
											exportOptions : {
												
												columns : [':visible']
											},
										} ),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize: 'legal',
										//footer : true,
										title : 'Merchant Exception',
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
										title : 'Merchant Exception',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [ 0,1, 2, 3, 4, 5, 6]
									} ],

							"ajax" :{
								
								"url" : "merchantExceptionReportAction",
								"type" : "POST",
								"data": function (d){
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {
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
						            "targets": [0,1,2,3,4,5,6]
						            }
						        ], 

 
							"columns" : [ {
								"data" : "pgRefNo",
								"className" : "payId"
							},  {
								"data" : "txnId",
								"className" : "payId"
								
							},{
								"data" : "orderId",
								"className" : "orderId"
							}, {
								"data" : "acqId"
							}, {
								"data" : "createdDate"
							}, {
								"data" : "status"
							}, {
								"data" : "exception"
								
							}]
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
		var merchant = document.getElementById("merchant").value;
		 var acquirer = document.getElementById("acquirer").value;
		 var status = document.getElementById("status").value;
		 var dateFrom = document.getElementById("dateFrom").value;
		 var dateTo = document.getElementById("dateTo").value;
		 
		 var token  = document.getElementsByName("token")[0].value;
		if(merchant==''){
			merchant='ALL'
		}
		if(acquirer==''){
			acquirer='ALL'
		}
		if(status==''){
			status='ALL'
		}
		
		var obj = {
			merchant : merchant,
			acquirer : acquirer,
			status : status,
			dateFrom : dateFrom,
			dateTo : dateTo,
			draw : d.draw,
			length :d.length,
			start : d.start, 
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}
</script>

</head>
<body id="mainBody">
	
	<section class="merchant-exception lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant Exception Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:if
				   test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
				   <s:select name="merchant" data-live-search="true" class="selectpicker" id="merchant"
					   headerKey="" headerValue="ALL" list="merchantList"
					   listKey="payId" listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
						<s:select name="merchant" class="selectpicker" id="merchant"
							headerKey="" headerValue="ALL" list="merchantList"
							listKey="payId" listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Acquirer</label>
				   <s:select headerKey="" data-live-search="true" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
				   listValue="name" listKey="code" name="acquirer"
					   id="acquirer" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Status</label>
				   <s:select name="status" id="status" headerValue="ALL"
				   headerKey="ALL" list="#{'PENDING':'PENDING', 'RESOLVED':'RESOLVED'}" class="selectpicker"
				   autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit"
				class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="merchant-exception lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant Exception Report Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="display" cellspacing="0"
						width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th>Pg Ref Num</th>
								<th>Txn ID</th>
								<th>Order ID</th>
								<th>Acquirer</th>
								<th>Create Date</th>
								<th>Status</th>
								<th>Exception</th>							
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
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->	
</body>
</html>