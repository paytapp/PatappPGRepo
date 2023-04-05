<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Student Fee Payment Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		renderTable();
	});
		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			reloadTable();
		});

		$(function() {
			var datepick = $.datepicker;
			var table = $('#studentDataTable').DataTable();
			$('#studentDataTable').on('click', 'td.my_class', function() {
				var rowIndex = table.cell(this).index().row;
				var rowData = table.row(rowIndex).data();
				popup(rowData.oId);
			});

			$("body").on("click", ".cancelBooking", function(e) {
				e.preventDefault();
				var result = confirm("Do you want to cancel this booking?");
				if(result) {
					$("body").removeClass("loader--inactive");
					var _parent = $(this).closest("td");
					var rowIndex = table.cell(_parent).index().row;
					var rowData = table.row(rowIndex).data();
	
					var token = document.getElementsByName("token")[0].value;
					var myData = {
						token : token,
						"struts.token.name" : "token",
						"bookingId" : rowData.bookingId
					}
	
					$.ajax({
						url : "cancelBookingAction",
						type : "POST",
						data : myData,
						success: function(data) {
							reloadTable();
							alert("Booking has been cancelled. SMS or email sent to customer & hotel.");
							$("body").addClass("loader--inactive");
						},
						error: function() {
							alert("Try Again, Something went wrong!");
							$("body").addClass("loader--inactive");
						}
					});
				}
			});
		});
	

	function renderTable() {
		// var table = new $.fn.dataTable.Api('#studentDataTable');

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
			exportOptions : {
				format : {
					body : function(data, column, row, node) {
						// Strip $ from salary column to make it numeric
						return column === 0 ? "'" + data : (column === 1 ? "'" + data : data);
					}
				}
			}
		};

		$('#studentDataTable').dataTable({
			language: { search: '', searchPlaceholder: "Student Search" },
			"footerCallback" : function(row, data, start, end, display) {
				console.log(data);
				var api = this.api(), data;

				// Remove the formatting to get integer data for summation
				var intVal = function(i) {
					return typeof i === 'string' ? i.replace(/[\,]/g, '') * 1 : typeof i === 'number' ? i : 0;
				};

				total = api.column(8).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Total over this page
				pageTotal = api.column(8, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(8).footer()).html('' + pageTotal.toFixed(2) + ' ' + ' ');

				// Total over all pages
				total = api.column(9).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Total over this page
				pageTotal = api.column(9, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(9).footer()).html('' + pageTotal.toFixed(2) + ' ' + ' ');

				total = api.column(10).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Total over this page
				pageTotal = api.column(10, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(10).footer()).html('' + pageTotal.toFixed(2) + ' ' + ' ');

				total = api.column(11).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Total over this page
				pageTotal = api.column(11, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(11).footer()).html('' + pageTotal.toFixed(2) + ' ' + ' ');
					
			},
							// "columnDefs" : [ {
							// 	className : "dt-body-right",
							// 	"targets" : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
							// } ],
							dom : 'BTrftlpi',
							buttons : [
									$.extend(true, {}, buttonCommon, {
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13]
										},
									}),
									$.extend(true, {}, buttonCommon, {
										extend : 'csvHtml5',
										title : 'School_Report',
										exportOptions : {

											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13]
										},
									}),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize : 'legal',
										//footer : true,
										title : 'School_Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13]
										},
										customize : function(doc) {
											doc.defaultStyle.alignment = 'center';
											doc.styles.tableHeader.alignment = 'center';
										}
									},
									{
										extend : 'print',
										//footer : true,
										title : 'School_Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13]
										}
									},
									{
										extend : 'colvis',
										columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13]
									} ],

							"ajax" : {

								"url" : "studentFeeDetailSearchAction",
								"type" : "POST",
								"data" : function(d) {
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function(data) {
								console.log(data);
								
								$("body").addClass("loader--inactive");
							},

							"processing" : true,
							"searching" : false,
							"serverSide" : true,
							"paginationType" : "full_numbers",
							"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
							"order" : [ [ 2, "desc" ] ],

							"columnDefs" : [
								{
									"type" : "html-num-fmt",
									"targets" : 4,
									"orderable" : true,
									"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
								},
								// {
								// 	"targets" : [14],
								// 	"createdCell" : function(td, cellData, rowData, row, col) {
								// 		if($("#userType").val() !== "ADMIN") {
								// 			$(td).addClass("d-none");
								// 		}
								// 	}
								// }
							],

							"columns" : [
									
									{
										"data" : "ORDER_ID",
										"className" : "payId text-class"

									},
									{
										"data" : "REG_NUMBER",
										"className" : "text-class"
									},
									{
										"data" : "PG_REF_NUM",
										"className" : "text-class",
									},
									{
										"data" : "CREATE_DATE",
										"className" : "text-class",

									},
									{
										"data" : "STUDENT_NAME",
										"className" : "text-class",
									},
									{
										"data" : "FATHER_NAME",
										"className" : "text-class",
									},
									{
										"data" : "STANDARD",
										"className" : "text-class",
									},
									{
										"data" : "MOBILE",
										"className" : "text-class",
									},
									{
										"data" : "AMOUNT",
										"className" : "text-class",
									},
									{
										"data" : "TOTAL_AMOUNT",
										"className" : "text-class"								
									},
									{
										"data" : "TDR_OR_SURCHARGE",
										"className" : "txnType text-class"
									},
									{
										"data" : "GST",
										"className" : "status text-class"
									},
									{
										"data" : "TXN_STATUS",
										"className" : "text-class"

									},
									{
										"mData" : null,
										"sClass" : "column-booking-action center",
										"bSortable" : false,
										"mRender" : function(row) {
											var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
										if (userType == "ADMIN"|| userType == "SUBADMIN" || userType == "MERCHANT") {
												if(row.REFUND_BTN_TEXT == "Refunded") {
						
													return '<button class="btn btn-info btn-xs btn-block refund-btn" style="font-size:10px;" disabled>Refunded</button>';
												} else if(row.REFUND_BTN_TEXT == "Partial Refund") {
													return '<button class="btn btn-info btn-xs btn-block refund-btn" style="font-size:10px;">Partial Refund</button>';
												} else if(row.REFUND_BTN_TEXT == "Refund") {
													return '<button class="btn btn-info btn-xs btn-block refund-btn" style="font-size:10px;">Refund</button>';
												} else {
													return "";
												}
										} else {
											return "";
										}

										}
									}
								]
						});

			$(document).ready(function(){
				var table = $('#studentDataTable').DataTable();
				$('#studentDataTable').on('click','.refund-btn',function() {
				var _btn = $(this).text();
				var _parent = $(this).closest("td");
				var columnIndex = table.cell(_parent).index().column;
				var rowIndex = table.cell(_parent).index().row;
				var rowNodes = table.row(rowIndex).node();
				var rowData = table.row(rowIndex).data();	
				$("#refundPayId").val(rowData.PAY_ID);
				$("#refundRefNum").val(rowData.PG_REF_NUM);
				$("#refundedAmount").val(rowData.REFUNDED_AMOUNT);
				$("#refundAvailable").val(rowData.REFUND_AVAILABLE);
				$("#refundRegNum").val(rowData.REG_NUMBER);
				//return false;
				$("#manualRefundProcess").submit();
				});
			});

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
		
		var tableObj = $('#studentDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var orderId = document.getElementById("orderId").value;
		var mobile = document.getElementById("mobile").value;
		var regNo = document.getElementById("regNo").value;
		var status = document.getElementById("status").value;
		var txnType = document.getElementById("txnType").value;


		/* if (orderId == '') {
			orderId = 'ALL'
		}
		if (mobile == '') {
			mobile = 'ALL'
		}
		if (regNo == '') {
			regNo = 'ALL'
		} */
		if (status == '') {
			status = 'ALL'
		}
		if (txnType == ''){
			txnType = 'ALL'
		}

		var obj = {
			orderId : orderId,
			mobile : mobile,
			regNo : regNo,
			status : status,
			txnType : txnType,
			dateFrom : document.getElementById("dateFrom").value,
			dateTo : document.getElementById("dateTo").value,
			draw : d.draw,
			length : d.length,
			start : d.start,
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}

	
	function downloadSubmit() {
		var token = document.getElementsByName("token")[0].value;
		var orderId = document.getElementById("orderId").value;
		var mobile = document.getElementById("mobile").value;
		var regNo = document.getElementById("regNo").value;
		var status = document.getElementById("status").value;
		var txnType = document.getElementById("txnType").value;

		/* if (orderId == '') {
			orderId = 'ALL'
		}
		if (mobile == '') {
			mobile = 'ALL'
		}
		if (regNo == '') {
			regNo = 'ALL'
		} */
		if (status == '') {
			status = 'ALL'
		}
		if(txnType == ''){
			txnType = 'ALL'
		}

	
		document.getElementById("orderIdFrm").value = orderId;
		document.getElementById("mobileFrm").value = mobile;
		document.getElementById("regNoFrm").value = regNo;
		document.getElementById("statusFrm").value =  status;
		document.getElementById("dateFromFrm").value= document.getElementById("dateFrom").value;
		document.getElementById("dateToFrm").value = document.getElementById("dateTo").value;
		document.getElementById("txnTypeHidden").value =  txnType;
		
		document.getElementById("studentFeeDownloadForm").submit();
		
		
	}
</script>

</head>
<body id="mainBody">
	<section class="student-fee lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Student Fee Data Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Order ID</label>
				<s:textfield id="orderId" class="lpay_input" name="orderId"
				type="text" value="" autocomplete="off" onkeypress="return Validate(event);"></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20-->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Mobile</label>
				<s:textfield id="mobile" class="lpay_input" name="mobile" type="text" value="" autocomplete="off"
				></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 mb-20-->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Registration Number</label>
				<s:textfield id="regNo" class="lpay_input" name="regNo" type="text" value="" autocomplete="off"
				></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Status</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="lst" name="status" id="status" value="name" listKey="name"
				   listValue="name" autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-30-->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Transaction Type</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker" list="txnTypelist"
				   listValue="name" listKey="code" name="txnType"
				   id="txnType" autocomplete="off" value="name" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20-->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="View" onclick = "reloadTable()" class="lpay_button lpay_button-md lpay_button-secondary">
				<input type="button" id="downloadSubmit" value="Download" onclick="downloadSubmit()" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="student-fee lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Student Fee Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="studentDataTable" class="" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th style='text-align: center'>Order Id</th>
								<th style='text-align: center'>Registration No</th>
								<th style='text-align: center'>PgRefNum</th>
								<th style='text-align: center'>Date</th>
								<th style='text-align: center'>Student Name</th>
								<th style='text-align: center'>Father Name</th>
								<th style='text-align: center'>Standard</th>
								<th style='text-align: center'>Mobile</th>
								<th style='text-align: center'>Amount</th>
								<th style='text-align: center'>Total Amount</th>
								<th style='text-align: center'>TDR/Surcharge</th>
								<th style='text-align: center'>GST</th>
								<th style='text-align: center'>Status</th>
								<!-- <th style='text-align: center'>Status</th> -->									
								<th style="text-align: center;" class="column-booking-action">Action</th>									
							</tr>
						</thead>
						<tfoot>
							<tr class="lpay_table_head">
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
								<!-- <th></th> -->
								<th></th>
								<th></th>
							</tr>
						</tfoot>
					</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<s:textfield type="hidden" value='%{#session.USER.UserType.name()}' id="userType" />

	<script type="text/javascript">
		$(document).ready(function() {
			$('#closeBtn').click(function() {
				$('#popup').hide();
			});

			if($("#userType").val() !== "ADMIN") {
				$(".column-booking-action").addClass("d-none");
			}

			
			
			
		});
	</script>

	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}
	</script>
	
	<s:form name="studentFeeDownloadForm" id="studentFeeDownloadForm" action="downloadStudentFeeAction">
		<s:hidden name="orderId" id="orderIdFrm" value="" />
		<s:hidden name="mobile" id="mobileFrm" value="" />
		<s:hidden name="regNo" id="regNoFrm" value="" />
		<s:hidden name="status" id="statusFrm" value="" />
		<s:hidden name="dateFrom" id="dateFromFrm" value="" />
		<s:hidden name="dateTo" id="dateToFrm" value="" />
		<s:hidden name="txnType" id="txnTypeHidden" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

	<s:form name="manualRefundProcess" id="manualRefundProcess" action="manualRefundProcessForStudentFee">
		<s:hidden name="payId" id="refundPayId" value="" />
		<s:hidden name="pgRefNum" id="refundRefNum" value="" />
		<s:hidden name="refundedAmount" id="refundedAmount" value="" />
		<s:hidden name="refundAvailable" id="refundAvailable" value="" />
		<s:hidden name="regNumber" id="refundRegNum" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

</body>
</html>