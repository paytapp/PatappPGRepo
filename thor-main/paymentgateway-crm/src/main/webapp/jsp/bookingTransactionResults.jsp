<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Booking Record</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all"
	href="../css/daterangepicker-bs3.css" />
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
			var table = $('#bookingsDataTable').DataTable();
			$('#bookingsDataTable').on('click', 'td.my_class', function() {
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
		// var table = new $.fn.dataTable.Api('#bookingsDataTable');

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

		$('#bookingsDataTable').dataTable({
			language: { search: '', searchPlaceholder: "Search Hotel" },
			"footerCallback" : function(row, data, start, end, display) {
				var api = this.api(), data;

				// Remove the formatting to get integer data for summation
				var intVal = function(i) {
					return typeof i === 'string' ? i.replace(/[\,]/g, '') * 1 : typeof i === 'number' ? i : 0;
				};

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
					
			},
							"columnDefs" : [ {
								className : "dt-body-right",
								"targets" : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
							} ],
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
										title : 'Booking_Report',
										exportOptions : {

											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13]
										},
									}),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize : 'legal',
										//footer : true,
										title : 'Booking_Report',
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
										title : 'Booking_Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13]
										}
									},
									{
										extend : 'colvis',
										columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14]
									} ],

							"ajax" : {

								"url" : "bookingsSearchAction",
								"type" : "POST",
								"data" : function(d) {
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {

								
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
									"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
								},
								{
									"targets" : [14],
									"createdCell" : function(td, cellData, rowData, row, col) {
										if($("#userType").val() !== "ADMIN") {
											$(td).addClass("d-none");
										}
									}
								}
							],

							"columns" : [
									
									{
										"data" : "bookingId",
										"className" : "payId text-class"

									},
									{
										"data" : "contactName",
										"className" : "text-class"
									},
									{
										"data" : "contactNumber",
										"className" : "text-class",
									},
									{
										"data" : "contactEmail",
										"className" : "text-class",
									},
									{
										"data" : "hotelName",
										"className" : "text-class",
									},
									{
										"data" : "city",
										"className" : "text-class",
									},
									{
										"data" : "district",
										"className" : "text-class",
									},
									{
										"data" : "bookingDate",
										"className" : "text-class",
									},
									{
										"data" : "checkInDate",
										"className" : "text-class",
									},
									{
										"data" : "tariff",
										"className" : "text-class"								
									},
									{
										"data" : "roomsCategory",
										"className" : "txnType text-class"
									},
									{
										"data" : "noOfRoomsBooked",
										"className" : "status text-class"
									},
									{
										"data" : "guestRecord",
										"className" : "text-class"

									},{
										"data" : "status",
										"className" : "text-class"								
									},
									{
										"mData" : null,
										"sClass" : "column-booking-action center",
										"bSortable" : false,
										"mRender" : function(row) {
											var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
											if(userType == "ADMIN") {
												if(row.status == "Captured") {
													return '<button class="btn btn-info btn-xs cancelBooking">Cancel</button>';
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
		
		var tableObj = $('#bookingsDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var bookingId = document.getElementById("bookingId").value;
		var custMobile = document.getElementById("custMobile").value;
		var customerEmail = document.getElementById("customerEmail").value;
		var status = document.getElementById("status").value;
		var hotelName = document.getElementById("hotelName").value;


		if (bookingId == '') {
			bookingId = 'ALL'
		}
		if (custMobile == '') {
			custMobile = 'ALL'
		}
		if (customerEmail == '') {
			customerEmail = 'ALL'
		}
		if (status == '') {
			status = 'ALL'
		}
		if (hotelName == ''){
			hotelName = 'ALL'
		}

		var obj = {
			bookingId : bookingId,
			custMobile : custMobile,
			customerEmail : customerEmail,
			status : status,
			hotelName : hotelName,
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
		var bookingId = document.getElementById("bookingId").value;
		var custMobile = document.getElementById("custMobile").value;
		var customerEmail = document.getElementById("customerEmail").value;
		var status = document.getElementById("status").value;
		var hotelName = document.getElementById("hotelName").value;

		if (bookingId == '') {
			bookingId = 'ALL'
		}
		if (custMobile == '') {
			custMobile = 'ALL'
		}
		if (customerEmail == '') {
			customerEmail = 'ALL'
		}
		if (status == '') {
			status = 'ALL'
		}
		if(hotelName == ''){
			hotelName = 'ALL'
		}

	
		document.getElementById("bookingIdFrm").value = bookingId;
		document.getElementById("custMobileFrm").value = custMobile;
		document.getElementById("customerEmailFrm").value = customerEmail;
		document.getElementById("statusFrm").value =  status;
		document.getElementById("dateFromFrm").value= document.getElementById("dateFrom").value;
		document.getElementById("dateToFrm").value = document.getElementById("dateTo").value;
		document.getElementById("hotelNameHidden").value =  hotelName;
		
		document.getElementById("bookingDownloadForm").submit();
		
		
	}
</script>

</head>
<body id="mainBody">

	<section class="booking-transaction lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
					  <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Booking Record Filter</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Booking ID</label>
					<s:textfield id="bookingId" class="lpay_input" name="bookingId"
					type="text" value="" autocomplete="off"
					onblur="this.value=removeSpaces(this.value);"></s:textfield>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Customer Mobile</label>
						<s:textfield id="custMobile" class="lpay_input" name="custMobile"
						type="text" value="" autocomplete="off"
						onblur="this.value=removeSpaces(this.value);"></s:textfield>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Customer Email</label>
					<s:textfield id="customerEmail" class="lpay_input"
					name="customerEmail" type="text" value="" autocomplete="off"
					onblur="validateEmail(this);"></s:textfield>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Status</label>
					   <s:select headerKey="" headerValue="ALL" class="selectpicker"
						list="lst" name="status" id="status" value="name"
						listKey="name" listValue="name" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Hotel Name</label>
					<s:textfield type="text" id="hotelName" name="hotelName"
					class="lpay_input" autocomplete="off" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield type="text" id="dateFrom" name="dateFrom"
					class="lpay_input" autocomplete="off" readonly="true" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" id="dateTo" name="dateTo"
					class="lpay_input" autocomplete="off" readonly="true" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-12 text-center">
					<input type="button" id="submit" value="View" onclick = "reloadTable()" class="lpay_button lpay_button-md lpay_button-secondary">
					<input type="button" id="downloadSubmit" value="Download" onclick="downloadSubmit()" class="lpay_button lpay_button-md lpay_button-secondary">
				</div>
				<!-- /.col-md-12 text-center -->
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

		<section class="booking-report lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Booking Record Data</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="lpay_table">
						<table id="bookingsDataTable" class="" cellspacing="0"
						width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th style='text-align: center'>Booking Id</th>
								<th style='text-align: center'>Name</th>
								<th style='text-align: center'>Mobile</th>
								<th style='text-align: center'>Email</th>
								<th style='text-align: center'>Hotel</th>
								<th style='text-align: center'>City</th>
								<th style='text-align: center'>District</th>
								<th style='text-align: center'>Booking Date</th>
								<th style='text-align: center'>CheckIn Date</th>
								<th style='text-align: center'>Amount</th>
								<th style='text-align: center'>Room Type</th>
								<th style='text-align: center'>Total rooms</th>
								<th style='text-align: center'>Guests</th>
								<th style='text-align: center'>Status</th>									
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
								<th></th>
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
	
	<s:form name="bookingDownloadForm" id="bookingDownloadForm" action="bookingDownload">
		<s:hidden name="bookingId" id="bookingIdFrm" value="" />
		<s:hidden name="custMobile" id="custMobileFrm" value="" />
		<s:hidden name="customerEmail" id="customerEmailFrm" value="" />
		<s:hidden name="status" id="statusFrm" value="" />
		<s:hidden name="dateFrom" id="dateFromFrm" value="" />
		<s:hidden name="dateTo" id="dateToFrm" value="" />
		<s:hidden name="hotelName" id="hotelNameHidden" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

</body>
</html>