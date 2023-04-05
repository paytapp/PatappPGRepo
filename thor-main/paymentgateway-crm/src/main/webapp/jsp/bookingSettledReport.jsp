<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Hotel Bookings Settled</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all"
	href="../css/daterangepicker-bs3.css" />
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<!-- <link href="../css/Jquerydatatable.css" rel="stylesheet" /> -->
<script src="../js/jquery.min.js" type="text/javascript"></script>
<!-- <script src="../js/moment.js" type="text/javascript"></script> -->
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<!-- <script src="../js/jquery.popupoverlay.js"></script> -->
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/common-scripts.js"></script>


<script type="text/javascript">

		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			
		});

		$(function() {
			var datepick = $.datepicker;
			var table = $('#bookingsDataTable').DataTable();
			$('#bookingsDataTable').on('click', 'td.my_class', function() {
				var rowIndex = table.cell(this).index().row;
				var rowData = table.row(rowIndex).data();

				popup(rowData.oId);
			});
		});
	

	function downloadSubmit() {
		var token = document.getElementsByName("token")[0].value;
		var hotelName = document.getElementById("hotelName").value;

		if(hotelName == ''){
			hotelName = 'ALL'
		}

	
		document.getElementById("hotelNameFrm").value =  hotelName;
		document.getElementById("dateFromFrm").value= document.getElementById("dateFrom").value;
		document.getElementById("dateToFrm").value = document.getElementById("dateTo").value;
		
		document.getElementById("bookingSettledForm").submit();
		
		
	}
</script>
</head>
<body id="mainBody">

	<section class="booking-settled-report lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Booking Settled Report</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Hotel Name</label>
				<s:textfield type="text" id="hotelName" name="hotelName" class="lpay_input" autocomplete="off" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" readonly="true" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off"  />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" readonly="true" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off"  />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<input type="button" id="downloadSubmit" style="width: auto;" value="Download" onclick = "downloadSubmit()"
				class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<script type="text/javascript">
		$(document).ready(function() {
			$('#closeBtn').click(function() {
				$('#popup').hide();
			});
		});
	</script>

	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}
	</script>
	
		<s:form name="bookingSettledForm" id="bookingSettledForm" action="downloadBookingSettled">
		<s:hidden name="hotelName" id="hotelNameFrm" value="" />
		<s:hidden name="dateFrom" id="dateFromFrm" value="" />
		<s:hidden name="dateTo" id="dateToFrm" value="" />	
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

</body>
</html>