<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>MIS Report</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/jquery.popupoverlay.js"></script> 
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>  
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

	<style>
        .lp-success_generate, .lp-error_generate {
            background-color: #c0f4b4;
            font-size: 15px;
            padding: 10px;
            text-align: center;
            margin-top: 20px;
            border-radius: 5px;
            border: 1px solid #3b9f24;
        }
    
        .lp-error_generate{
            background-color: #f79999;
            border: 1px solid #771313;
        }
    
        .lp-success_generate p{ 
            color: #326626;
        }
    
        .lp-error_generate p{
            color: #921919;
        }
    </style>


</head>
<body>
	<section class="mis-report lapy_section white-bg box-shadow-box mt-70 p20">
		<!-- <form id="misReportQuery" name="misReportQuery" action="misReportQuery"> -->
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">MIS Report</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
						<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select
							data-live-search="true"
								name="merchant" class="selectpicker" data-mis='merchant' id="merchant"
								title="ALL" multiple="true" data-selected-text-format="count>2" data-actions-box="true" list="merchantList"
								listKey="payId" listValue="businessName" autocomplete="off" />
						</s:if>
						<s:else>
							<s:select name="merchant" data-mis='merchant' class="selectpicker" id="merchant"
								headerKey="" headerValue="" list="merchantList"
								listKey="payId" listValue="businessName" autocomplete="off" />
						</s:else>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20 -->

				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20" data-id="submerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<s:select
								data-id="subMerchant"
								name="subMerchant"
								data-mis='subMerchant'
								class="selectpicker"
								id="subMerchant"
								list="subMerchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none" data-id="submerchant"> 
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<select name="subMerchant" data-mis='subMerchant' id="subMerchant" class=""></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>

				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Select Acquirer</label>
					   <s:select data-actions-box="true" data-mis='name' data-live-search="true" multiple="true" title="Select Acquirer" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" 
					   listValue="name" listKey="code" name="name" class="selectpicker" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20-->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Currency</label>
					   <s:select name="currency" data-mis='currency' id="currency" headerValue="ALL" headerKey="ALL" list="currencyMap" class="selectpicker" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20-->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Settlement Type</label>
					   <s:select name="partSettle" data-mis='partSettle' id="partSettle" headerValue="ALL"
					   headerKey="ALL" list="#{'N':'Normal','Y':'Part'}" class="selectpicker" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 mb-20 -->
				<div class="col-md-4 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield type="text" data-mis='dateFrom' readonly="true" id="dateFrom"
					name="dateFrom" class="lpay_input" onchange="dateBaseDownload();"
					autocomplete="off" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 mb-20 -->
				<div class="col-md-4 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" data-mis='dateTo' readonly="true" id="dateTo" name="dateTo"
					class="lpay_input" onchange="dateBaseDownload();" autocomplete="off" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 mb-20 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					   <label for="">Transaction Flag</label>
					   <select class="selectpicker" title="ALL" data-selected-text-format="count>2" data-actions-box="true" multiple data-mis='transactionFlag' data-download="transactionFlag" data-var="transactionFlag" name="transactionFlag" id="transactionFlag">
						   <!--<option value="ALL" selected>ALL</option> -->
						   <option value="Real-Time">Real Time</option>
						   <option value="Post Captured">Post Captured</option>
						   <option value="TXN Enquiry">TXN Enquiry</option>
					   </select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-12 text-center">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="download-mis">Download</button>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 d-none">
					<div class="lp-success_generate">
						<p>Your file has been generate successfully please see after some time</p>
					</div>
					<!-- /.lp-success_generate -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 d-none">
					<div class="lp-error_generate">
						<p>Please try again after some time</p>
					</div>
					<!-- /.lp-success_generate -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		<!-- </form> -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<form action="" id="downloadForm" method="post"></form>
<script type="text/javascript">
	   

	$(document).ready(function() {

		// DISPLAY SUB MERCHANT	

		
		function downloadForm(e){
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transTo - transFrom > 61 * 86400000) {
				alert('No. of days can not be more than 60 days');
				$("body").addClass("loader--inactive");
				$('#dateFrom').focus();
				return false;
			}
			var _text = e.target.innerText;
			document.querySelector("#downloadForm").innerHTML = "";
			var _getAllinput = document.querySelectorAll("[data-mis]");
			var _obj = {};
			var _option = "";
			_getAllinput.forEach(function(index, array, element){
				let _name = index.getAttribute("data-mis");
				if(_name === "merchant" || _name === "name"){
					_obj[_name] = $("[data-mis='"+_name+"']").val();
					
				}else{
					_obj[_name] = index.value;
				}
			})
			for(key in _obj){
				_option += "<input type='hidden' name='"+key+"' id='mis-"+key+"' value='"+_obj[key]+"' />";
			}
			if(_text == "Download"){
				document.querySelector("#downloadForm").innerHTML = _option;
				document.querySelector("#downloadForm").setAttribute("action", "misReportQuery");
				$("#mis-transactionFlag").val($("#transactionFlag").val());
				document.querySelector("#downloadForm").submit();
			}else{
				_obj['reportType'] = "misReport";
				_obj['name'] = $("#name").val().toString();
				_obj['transactionFlag'] = $("#transactionFlag").val().toString();
				document.querySelector("body").classList.remove("loader--inactive");
				
				$.ajax({
					type: "POST",
					url: "generateMisReportFileAction",
					data: _obj,
					success: function(data){
						setTimeout(function(e){
							document.querySelector("body").classList.add("loader--inactive");
							if(data.generateReport == true){
								document.querySelector(".lp-success_generate").closest(".col-md-12").classList.remove("d-none");
							}else{
								document.querySelector(".lp-error_generate").closest(".col-md-12").classList.remove("d-none");
							}
						}, 500)
						setTimeout(function(e){
							removeError()
						}, 4000);
					}
				})
			}
		}
		document.querySelector("#download-mis").onclick = function(e){
			downloadForm(e);
		}
		function removeError(){
			document.querySelector(".lp-error_generate").closest(".col-md-12").classList.add("d-none");
			document.querySelector(".lp-success_generate").closest(".col-md-12").classList.add("d-none");
		}
	});	
	function dateBaseDownload(){
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transTo - transFrom > 30 * 86400000) {
			document.querySelector("#download-mis").innerText = "Generate";
		}else{
			document.querySelector("#download-mis").innerText = "Download";
		}
	}



</script>
<script src="../js/common-scripts.js"></script>

</body>
</html>