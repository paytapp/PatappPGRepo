<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Hotel Bookings</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all"
	href="../css/daterangepicker-bs3.css" />
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

<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />



<script type="text/javascript">
	$(document).ready(function() {

		$(function() {
            var today = new Date();
            $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
            $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
            renderTable();
	 });

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
		
	});
	

	function renderTable() {
		var table = new $.fn.dataTable.Api('#bookingsDataTable');

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
		var token = document.getElementsByName("token")[0].value;
	}


	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var bookingId = document.getElementById("bookingId").value;
		var custMobile = document.getElementById("custMobile").value;
		var customerEmail = document.getElementById("customerEmail").value;
		var status = document.getElementById("status").value;

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

		var obj = {
			bookingId : bookingId,
			custMobile : custMobile,
			customerEmail : customerEmail,
			status : status,
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

</script>


<style type="text/css">

.form-control{
	width: 100% !important;
	margin-left: 0 !important;
}

.cust {
	width: 24% !important;
	margin: 0 5px !important; /*font: bold 10px arial !important;*/
}

.samefnew {
	width: 24% !important;
	margin: 0 5px !important;
	/*font: bold 10px arial !important;*/
}

.btn {
	padding: 3px 7px !important;
	font-size: 12px !important;
}

.samefnew-btn {
	width: 15%;
	float: left;
	font: bold 11px arial;
	color: #333;
	line-height: 22px;
	margin-left: 5px;
}
/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
tr td.my_class {
	cursor: pointer;
}

tr td.my_class:hover {
	cursor: pointer !important;
}

tr th.my_class:hover {
	color: #fff !important;
}

.cust .form-control, .samefnew .form-control {
	margin: 0px !important;
	width: 100%;
}

.select2-container {
	width: 100% !important;
}

.clearfix:after {
	display: block;
	visibility: hidden;
	line-height: 0;
	height: 0;
	clear: both;
	content: '.';
}

#popup {
	position: fixed;
	top: 0px;
	left: 0px;
	background: rgba(0, 0, 0, 0.7);
	width: 100%;
	height: 100%;
	z-index: 999;
	display: none;
}

.innerpopupDv {
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

#loader-wrapper .loader-section.section-left, #loader-wrapper .loader-section.section-right
	{
	background: rgba(225, 225, 225, 0.6) !important;
	width: 50% !important;
}

.invoicetable {
	float: none;
}

.innerpopupDv h2 {
	font-size: 12px;
	padding: 5px;
}

.text-class {
	text-align: center !important;
}

.odd {
	background-color: #e6e6ff !important;
}

table.dataTable thead th {
	padding: 10px 5px !important;
	white-space: nowrap;
}

#loading {
	width: 100%;
	height: 100%;
	top: 0px;
	left: 0px;
	position: fixed;
	display: block;
	z-index: 99
}

#loading-image {
	position: absolute;
	top: 35%;
	left: 55%;
	z-index: 100;
	width: 10%;
}

#mainTable{
	table-layout: fixed;
}

#txnResultDataTable_wrapper{
	overflow-y: auto;
	
}

.mt-20{
	margin-top: 20px !Important;
}

</style>
</head>
<body id="mainBody">

	<div id="sale-detail">
		<table id="mainTable" width="100%" border="0" align="center"
			cellpadding="0" cellspacing="0" class="txnf">
			<tr>
				<td colspan="5" align="left"><h2>Bookings</h2></td>
			</tr>
			<tr>
				<td colspan="5" align="left" valign="top"><div class="MerchBx">
						<div class="clearfix">
							<form action="bookingDownload" method="post" >
							<div class="row">
								<div class="col-md-3 col-sm-6 col-xs-12 mt-20">
									Booking ID:<br>
									<div class="txtnew">
										<s:textfield id="bookingId" class="merchant__form_control" name="bookingId" type="text" value="" autocomplete="off"
											onblur="this.value=removeSpaces(this.value);"></s:textfield>
									</div>
								</div>
								<div class="col-md-3 col-sm-6 col-xs-12 mt-20">
									Customer Mobile:<br>
									<div class="txtnew">
										<s:textfield id="custMobile" class="merchant__form_control" name="custMobile"
											type="text" value="" autocomplete="off"
											onblur="this.value=removeSpaces(this.value);"></s:textfield>
									</div>
								</div>
	
								<div class="col-md-3 col-sm-6 col-xs-12 mt-20" >
									Customer Email:<br>
									<div class="txtnew">
										<s:textfield id="customerEmail" class="merchant__form_control"
											name="customerEmail" type="text" value="" autocomplete="off"
											onblur="validateEmail(this);"></s:textfield>
									</div>
									<!-- <div class="error-mobile error-subadmin">
										<p>Please Enter Valid Mobile Number</p>
									  </div> -->
									  <!-- /.error-name -->
								</div>

								<div class="col-md-3 col-sm-6 col-xs-12 mt-20">
									Status:<br>
									<div class="txtnew">
										<s:select headerKey="" headerValue="ALL" class="merchant__form_control"
											list="lst" name="status" id="status" value="name"
											listKey="name" listValue="name" autocomplete="off" />
									</div>
								</div>
	
								<div class="col-md-3 col-sm-6 col-xs-12 mt-20">
									Date From:<br>
									<div class="txtnew">
										<s:textfield type="text" id="dateFrom" name="dateFrom"
											class="merchant__form_control" autocomplete="off" readonly="true" />
									</div>
								</div>
	
								<div class="col-md-3 col-sm-6 col-xs-12 mt-20">
									Date To:<br>
									<div class="txtnew">
										<s:textfield type="text" id="dateTo" name="dateTo"
											class="merchant__form_control" autocomplete="off" readonly="true" />
									</div>
								</div>
							</div>
							<!-- /.row -->
							<div class="clearfix">
								<div class="samefnew-btn col-md-12 text-center" style="width: 100%;margin-top: 20px">
									
									<div class="txtnew">
										<input type="submit" id="submit" style="width: auto;" value="Download"
											class="primary-btn">

									</div>
								</div>
							</div>
						<!-- /.clearfix -->
					</form>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="5" align="left"><h2>&nbsp;</h2></td>
			</tr>

		</table>
	</div>

	


	<script type="text/javascript">
		$(document).ready(function() {
			function checkBlankField(){
				$(".merchant__form_control").each(function(e){
					var _thisVal = $(this).val();
					if(_thisVal == ""){
						_checkClass = false;
						$("#btnEditUser").attr("disabled", true);
						return false;
					}else{
						_checkClass = true;
						$("#btnEditUser").attr("disabled", true);
					}
				});
    		}

			$(".acquirer-input").on("change", checkBlankField);

			$(".merchant__form_control").on("blur", function(e){
				var _thisVal = $(this).val();
				if(_thisVal.length > 0){
				}else{
					$(this).closest(".col-md-6").find(".error-subadmin").addClass("show");
				}
			});
		});
	</script>

	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}
	</script>

</body>
</html>