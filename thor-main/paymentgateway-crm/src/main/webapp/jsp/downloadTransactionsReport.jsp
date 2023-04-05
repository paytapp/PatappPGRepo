<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Download Transactions Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>  
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
<script src="../js/user-script.js"></script>


<script type="text/javascript">
    function handleChange() {
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}
			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}
     }

	$(document).ready(function() {

		document.querySelector("#merchantPayId").addEventListener("change", function(e){
			getSubMerchant(e, "getSubMerchantList", {
				isSuperMerchant : true,
				subUser : true,
				retailMerchantFlag: true,
				glocal : true
			});
		});
		document.querySelector("#subMerchant").addEventListener("change", function(e){
			getSubMerchant(e, "vendorTypeSubUserListAction", {
				subUser : true
			});
		})


		

		// DISPLAY SUB MERCHANT
		var _select = "<option value='ALL'>ALL</option>"
		$("[data-id=subMerchant]").find('option:eq(0)').before(_select);
		$("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");		

		// $("#merchantPayId").on("change", function(e) {
		// 	var _merchant = $(this).val();

		// 	if(_merchant != "") {
		// 		$("body").removeClass("loader--inactive");
		// 		$.ajax({
		// 			type: "POST",
		// 			url: "getSubMerchantListByPayId",
		// 			data: {"payId": _merchant},
		// 			success: function(data){
		// 				console.log(data);
		// 				$("#subMerchant").html("");
		// 				if(data.superMerchant == true){
		// 					var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
		// 					for(var i = 0; i < data.subMerchantList.length; i++){
		// 						_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
		// 					}
		// 					$("[data-id=submerchant]").removeClass("d-none");
		// 					$("#subMerchant option[value='']").attr("selected", "selected");
		// 					$("#subMerchant").selectpicker();
		// 					$("#subMerchant").selectpicker("refresh");
		// 					setTimeout(function(e){
		// 						$("body").addClass("loader--inactive");
		// 					},500);
		// 				}else{
		// 					setTimeout(function(e){
		// 						$("body").addClass("loader--inactive");
		// 					},500);
		// 					$("[data-id=submerchant]").addClass("d-none");
		// 					$("[data-id=deliveryStatus]").addClass("d-none");
		// 					$("[data-id=deliveryStatus]").val("");
		// 					$("#subMerchant").val("");
		// 				}
					
		// 				if(data.glocalFlag == true){
		// 				$("[data-id=deliveryStatus]").removeClass("d-none");
		// 				$("[data-id=deliveryStatus] select").selectpicker('val', 'All');
		// 				}else{
		// 					$("[data-id=deliveryStatus]").addClass("d-none");
		// 				}
		// 			}
		// 		});
		// 	} else {
		// 		$("[data-id=submerchant]").addClass("d-none");
		// 		$("#subMerchant").val("");
		// 		$("[data-id=deliveryStatus]").addClass("d-none");
		// 		$("[data-id=deliveryStatus]").val("");
		// 	}
		// });

		$("#downloadTransactionsReportAction input[type='radio']").on("change", function(e){
			if($("#reportTyperefundCaptured").is(":checked")) {
				$("#settelmentType").addClass("d-none");
			} else {
				$("#settelmentType").removeClass("d-none");		
			}
		});	
		
		$(function() {
			var datepick = $.datepicker;
			var table = $('#chargebackDataTable').DataTable();
			$('#chargebackDataTable tbody').on('click', 'td', function() {
				submitForm(table, this);				
			});
		});


	});
	
	
</script>

</head>
<body>

	<s:hidden value="%{glocalFlag}" id="gloc" />



<section class="download-transaction lapy_section white-bg box-shadow-box mt-70 p20">
	<form id="downloadTransactionsReportAction" name="downloadTransactionsReportAction" action="downloadTransactionsReportAction">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
				<h2 class="heading_text">Download Transactions Report</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
					<s:select
						name="merchantPayId"
						data-live-search="true"
						class="selectpicker"
						id="merchantPayId"
						headerKey="ALL"
						data-submerchant="subMerchant" 
						data-user="subUser"
						headerValue="ALL"
						list="merchantList"
						listKey="payId"
						listValue="businessName"
						autocomplete="off"
					/>
					</div>
					</div>
				</s:if>
			<s:else>
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='SUPERADMIN'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
					<s:select
						name="merchantPayId"
						data-live-search="true"
						class="selectpicker"
						id="merchantPayId"
						headerKey="ALL"
						data-submerchant="subMerchant" 
					data-user="subUser"
						headerValue="ALL"
						list="merchantList"
						listKey="payId"
						listValue="businessName"
						autocomplete="off"
					/>
					</div>
					</div>
				</s:if>
				<s:elseif  test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
					<s:select
					name="merchantPayId"
					data-live-search="true"
					class="selectpicker"
					data-submerchant="subMerchant" 
					data-user="subUser"
					id="merchantPayId"
					list="subMerchantList"
					listKey="payId"
					listValue="businessName"
					autocomplete="off"
				/>	
				</div>
				</div>
                        </s:elseif>
				<s:else>
					<div class="col-md-3 mb-20 d-none">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
					<s:select
						name="merchantPayId"
						data-live-search="true"
						class="selectpicker"
						id="merchantPayId"
						data-submerchant="subMerchant" 
						data-user="subUser"
						list="merchantList"
						listKey="payId"
						listValue="businessName"
						autocomplete="off"
					/>
					</div>
					</div>
				</s:else>
			</s:else>

		<s:if test="%{#session['USER'].superMerchant == true}">
			<div class="col-md-3 mb-20" data-target="subMerchant">
				<div class="lpay_select_group">
					<label for="">Sub Merchant</label>
					<s:select
						data-submerchant="subMerchant" 
						data-user="subUser"
						data-id="subMerchant"
						data-live-search="true"
						headerKey="ALL" 
						headerValue="ALL"
						name="subMerchantPayId"
						class="selectpicker"
						id="subMerchant"
						list="subMerchantList"
						listKey="emailId"
						listValue="businessName"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->	
		</s:if>
		<s:else>
			<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
				<div class="lpay_select_group">
					<label for="">Sub Merchant</label>
					<select name="subMerchantPayId" data-submerchant="subMerchant" 
					data-user="subUser" id="subMerchant" class=""></select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->							
		</s:else>

		<s:if test="%{#session.SUBUSERFLAG == true}">
				<div class="col-md-3 mb-20" data-target="subUser">
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <s:select data-id="subUser" headerKey="ALL" headerValue="ALL" data-var="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subUser"> 
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <select name="subUserPayId" data-var="subUserPayId" id="subUser" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>

		<div class="col-md-3 d-none" data-id="deliveryStatus">
			<div class="lpay_select_group">
			   <label for="">Delivery Status</label>
			   <select class = "selectpicker" data-var="derliveryStatus" name="deliveryStatus" id="deliveryStatus">
				   <option value="All">ALL</option>
				   <option value="DELIVERED">Delivered</option>
				   <option value="NOT DELIVERED">Not Delivered</option>
				   <option value="PENDING">Pending</option>
			   </select>
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 -->

		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
			   <label for="">Payment Method</label>
			   <s:select headerKey="ALL" data-var="paymentMethod" headerValue="ALL" class="selectpicker"
			   list="@com.paymentgateway.commons.util.PaymentType@values()"
			   listValue="name" listKey="code" name="paymentType"
			   id="paymentType" autocomplete="off" value="" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
			   <label for="">Currency</label>
			   <s:select name="currency" data-var="currency" id="currency" headerValue="ALL" headerKey="ALL" list="currencyMap" class="selectpicker"/>
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
			   <label for="">Transaction Type</label>
			   <s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
			   list="txnTypelist" data-var="transactionType"
			   listValue="name" listKey="code" name="transactionType"
			   id="transactionType" autocomplete="off" value="name" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
			   <label for="">Transaction Region</label>
			   <s:select headerKey="ALL" data-var="transactionRegion" headerValue="ALL" class="selectpicker"
			   list="#{'INTERNATIONAL':'International','DOMESTIC':'Domestic'}" name="paymentsRegion" id = "paymentsRegion" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
		  <div class="lpay_input_group">
			<label for="">Date From</label>
			<s:textfield type="text" data-var="dateFrom" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" onchange="handleChange();"/>
		  </div>
		  <!-- /.lpay_input_group -->
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
		  <div class="lpay_input_group">
			<label for="">Date To</label>
			<s:textfield type="text" date-var="dateTo" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" onchange="handleChange();"/>
		  </div>
		  <!-- /.lpay_input_group -->
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
			   <label for="">Settlement Type</label>
			   <s:select headerKey="ALL" data-var="partSettleFlag" headerValue="ALL" class="selectpicker"
				list="#{'N':'Normal','Y':'Part'}" name="partSettleFlag" id = "partSettleFlag" onchange="handleChange();" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-12">
			<div class="OtherList">
				<div >
					<s:radio list="#{'saleCaptured':'Sale Captured Report','refundCaptured':'Refund Captured Report','settled':'Settled Report'}" name="reportType" id = "reportType"  />
				</div>
			</div>
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-12 text-center">
			<button class="lpay_button lpay_button-md lpay_button-secondary">Download</button>
		</div>
		<!-- /.col-md-12 -->
	</div>
	<!-- /.row -->
</form>
</section>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->


<script>
$(document).ready(function(){
	$("#reportTypesaleCaptured").attr('checked', 'checked');
	/* function myFunction(){
		$.ajax({
			type: "post",
			url: "downloadTransactionsReport",
			success: function(data){
				console.log(data);
			}
		})
	}
	myFunction(); */
	if($("#gloc").val() == "true"){
		$("[data-id=deliveryStatus]").removeClass("d-none");
		$("[data-id=deliveryStatus]").selectpicker('val', "All");
	}
});
</script>


</body>
</html>