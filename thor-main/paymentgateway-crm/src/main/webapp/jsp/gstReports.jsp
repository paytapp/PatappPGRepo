<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>GST Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script type="text/javascript" src="../js/summaryReport.js"></script>
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

	
<script type="text/javascript">

$(function() {
	
	var table = $('#gstReportDatatable').DataTable({
		
		dom: 'Bfrtip',
	               destroy : true,
	               buttons : [
									{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'csvHtml5',
										title : 'GST Purchase Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										title : 'GST Purchase Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'print',
										title : 'GST Purchase Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]
									}
								],
				"searching": false,
	});
	
    $('#gstButton').on('click', function() {
		$("body").removeClass("loader--inactive");

		 var merchant = document.getElementById("merchant").value;
		 var currency = document.getElementById("currency").value;
		 var year = document.getElementById("year1").value;
		 var month = document.getElementById("month1").value;
		  
		
		 var token  = document.getElementsByName("token")[0].value;
		 
		 //table.destroy();
         //$('#gstReportDatatable').empty();
		 
        var table = $('#gstReportDatatable').DataTable({
			dom: 'Bfrtip',
	               buttons : [
									{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ ':visible']
										}
									},
									{
										extend : 'csvHtml5',
										title : 'GST Purchase Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										title : 'GST Purchase Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'print',
										title : 'GST Purchase Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]
									}
								],
			"searching": false,
			destroy: true,
			"ajax": {
				    "url": "gstSaleReportAction",
				    "type": "POST",
				    "data": {
						"merchant":merchant,
					    "currency":currency,
						"year":year,
						"month":month,
						"token":token,
						"struts.token.name": "token",},
				   
				  },
			"columns": [
			            { "data": "createdDate" },
			            { "data": "invoiceNo" },
			            { "data": "businessName" },
			            { "data": "gstNo" },
			            { "data": "state" },
			            { "data": "goodOrService" },
						{ "data": "servicesDescription" },
			            { "data": "hsn_code" },
			            { "data": "txn_value" },
			            { "data": "cGst" },
			            { "data": "SGst" },
			            { "data": "iGst" },
						{ "data": "cEss" },
			            { "data": "tds" },
			            { "data": "pgGstNo" },
			            { "data": "address" },
			            { "data": "city" },
			            { "data": "state" },
						{ "data": "netAmt" }  
			        ]
			
		 
        });
	
		setTimeout(function() {
			$("body").addClass("loader--inactive");
		}, 1000);
	});
});

</script>

</head>
<body>

	<section class="gst-report lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">GST Reports Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<s:select name="merchant" data-live-search="true" class="selectpicker" id="merchant"
							headerKey="ALL" headerValue="ALL" list="merchantList"
							listKey="emailId" listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
						<s:select name="merchant" class="selectpicker" id="merchant"
							headerKey="" headerValue="ALL" list="merchantList"
							listKey="emailId" listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Currency</label>
				   <s:select name="currency" id="currency" headerValue="ALL"
				   headerKey="ALL" list="currencyMap" class="selectpicker" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Year</label>
				   <select id="year1" class="selectpicker" onchange="yearChange()">
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Month</label>
				   <select id="month1" class="selectpicker">
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="gstButton" >Submit</button>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="gst-report lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">GST Report Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="gstReportDatatable" align="center" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th style="text-align:center;">Invoice Date</th>
								<th style="text-align:center;">Invoice Number</th>
								<th style="text-align:center;">Customer Name</th>
								<th style="text-align:center;">Customer GSTIN</th>
								<th style="text-align:center;">State of Supply</th>
								<th style="text-align:center;">GOOD (G) or Service (S)</th>
								<th style="text-align:center;">Services Description</th>
								<th style="text-align:center;">HSN or SAC</th>
								<th style="text-align:center;">Txn Value</th>
								<th style="text-align:center;">CGST Amt</th>
								<th style="text-align:center;">SGST Amt</th>
								<th style="text-align:center;">IGST Amt</th>
								<th style="text-align:center;">CESS Amt</th>
								<th style="text-align:center;">TDS</th>
								<th style="text-align:center;">My GSTIN</th>
								<th style="text-align:center;">Cust Address</th>
								<th style="text-align:center;">Cust City</th>
								<th style="text-align:center;">Cust State</th>
								<th style="text-align:center;">NET Amt</th>
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

	
<script>

	
///-------------------------------------------GETTING MONTHS OF YEAR-----------------------------------------------------/////
var selectYear = document.getElementById("year1");
var selectMonth = document.getElementById("month1");
var currentYear = new Date().getFullYear(); //getting current year 5
var currentMonth = new Date().getMonth(); //getting current month month 5

function init(){
var start = 2018,
	options = "";
	for(var year = start ; year <=currentYear; year++){
	  options += "<option>"+ year +"</option>";
	}
	selectYear.innerHTML = options;
	yearChange();
}
function yearChange(){
	var afterChangeYear = selectYear.value;
	var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October","November", "December"];
	
	if(afterChangeYear != currentYear){
		currentMonth = 11;
	}else{
		currentMonth = new Date().getMonth();
	}
	
	$('#month1').empty();
	for (var i = 0; i <= currentMonth; i++){
           $('#month1').append('<option value="'+i+'">'+monthNames[i]+'</option>');
	}
}
init();

</script>


</body>
</html>