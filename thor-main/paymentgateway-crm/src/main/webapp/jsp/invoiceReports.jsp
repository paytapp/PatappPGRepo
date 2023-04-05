<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Invoice Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script type="text/javascript" src="../js/summaryReport.js"></script>
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

<!-------------------ajax call on button click--------------->
<script type="text/javascript">
	function invoiceReport() {
		$("body").removeClass("loader--inactive");
		 var merchant = document.getElementById("merchant").value;
		 var year = document.getElementById("year1").value;
		 var month = document.getElementById("month1").value;
		  
	
		 
		 var token  = document.getElementsByName("token")[0].value;
			$.ajax({
				type: "POST",
				url:"gstSaleReportAction",
				timeout: 0,
				data:
					 {"merchant":merchant, "year":year, "month":month,"token":token,"struts.token.name": "token",},
				success:function(response){
					alert("successfully done");

					setTimeout(function() {
						$("body").addClass("loader--inactive");
					}, 1000);
				},
				error:function(data){
					alert("wrong data");
				}
			});
	 }
	
</script>

</head>
<body>

	<section class="invoice-reports lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Invoice Reprts</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:if
						test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<s:select name="merchant" class="selectpicker" id="merchant"
							headerKey="" headerValue="ALL" list="merchantList"
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
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Year</label>
				   <select id="year1" class="selectpicker" onchange="yearChange()">
					</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4  mb-30">
				<div class="lpay_select_group">
				   <label for="">Month</label>
				   <select id="month1" class="selectpicker">
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="invoiceBtn" onclick="invoiceReport()">Submit</button>
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