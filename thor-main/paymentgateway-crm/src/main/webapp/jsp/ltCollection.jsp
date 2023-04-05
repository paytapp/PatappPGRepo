<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<title>LT Collection</title>
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<link rel="stylesheet" href="../css/jquery-ui.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/common-scripts.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>

	<style>
		.lpay_tabs_content button{
			margin: 0 !important
		}
	</style>

	<script>
		$(document).ready(function(e){

			$("[data-id='download-btn']").on("click", function(e){

			document.querySelector("#txtFile").innerHTML = "";

			var _getAttr = $(this).attr("data-action");

			document.querySelector("#txtFile").setAttribute("action", _getAttr);

			var _parent = $(this).closest("[data-target]").attr("data-target");

			var _allInput = document.querySelector("[data-target='"+_parent+"']").querySelectorAll("[data-var]");



			var _input = "";

			console.log(_allInput);

			_allInput.forEach(function(index, array, element){
				
				_input += "<input type='hidden' value='"+index.value+"' name='"+index.getAttribute('data-var')+"' />";
			})

			document.querySelector("#txtFile").innerHTML = _input;

			var _dateFrom = document.querySelector("#txtFile [name='fromDate']").value;
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#txtFile [name="fromDate"]').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#txtFile [name="toDate"]').val());

			if (transFrom > transTo) {
				alert('From date must be less than to date');
				return false;
			}

			if(transTo - transFrom > 6 * 86400000){
				alert("Date should not be greater than 7 days");
				return false;
			}

			document.querySelector("#txtFile").submit();

			})

			var today = new Date();
			$(".datepick").attr("readonly", true);
			$('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
			$(".datepick").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date(),
				changeMonth: true,
				changeYear: true
			});
    	})
	</script>

</head>
<body id="mainBody">
	
	<section class="msedcl lpay_section white-bg box-shadow-box mt-70 p20">
	   <div class="row" data-target="ltCollection">
		  	<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">LT Collection</h2>
			 	</div>
			 <!-- /.heading_icon -->
		  	</div>
		  	<!-- /.col-md-12 -->
              <div class="col-md-3 mb-20">
					<div class="lpay_input_group">
						<label for="">From Date</label>
						<input type="text" data-var="fromDate" class="lpay_input datepick">
					</div>
					<!-- /.lpay_input_group -->
				</div>
				
				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
						<label for="">To Date</label>
						<input type="text" data-var="toDate" class="lpay_input datepick">
					</div>
					<!-- /.lpay_input_group -->
				</div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 input-btn-space">
                <button class="lpay_button lpay_button-md lpay_button-primary m-0" data-action="LTCollectionDownload" data-id="download-btn">DOWNLOAD</button>
            </div>
            <!-- /.col-md-3 -->
	   </div>
	   <!-- /.row -->
	</section>
	<!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->
	<form id="txtFile" method="POST" action="LTCollectionDownload">
		<!-- <input type="hidden" id="payoutDate" name="payoutDate"> -->
	</form>

</body>
</html>