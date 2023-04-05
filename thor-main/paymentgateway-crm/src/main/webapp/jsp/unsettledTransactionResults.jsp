<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Unsettled Sale Captured</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/common-scripts.js"></script>
<script src="../js/user-script.js"></script>
<script src="../js/tabs.js"></script>
<script src="../js/unsettledSaleCapture.js"></script>
<script src="../js/decimalLimit.js"></script>
<style>
	tr{ cursor: pointer; }
	.lp-refund_section{ position: fixed;top: 0;left: 0;right: 0;bottom: 0;z-index: 99;display: block;z-index: -99;transition: all .8s .8s ease; }
	.lp-refund_section:after{ content: "";width: 100%;height: 100%;bottom: 700px;background-color: rgba(0,0,0,.8);position: absolute;left: 0;transition: all .8s ease-in-out; }
	.lp-show_popup{ display: block;z-index: 99;transition: all .8s ease; }
	.lp-show_popup:after{ bottom: 0;transition: all .8s ease; }
	.lp-refund_inner{ position: absolute;display: flex;align-items: center;justify-content: center;width: 100%;height: 100%; }
	.lp-refund_box{ background-color: #fff;padding: 20px;width: 100%;max-width: 450px;border-radius: 5px;z-index: 99;opacity: 0; }
	.lp-show_popup .lp-refund_box{ transition: all .8s .8s ease-in-out;opacity: 1; }
	.lp-refund_box table{ width: 100%; }
	.lp-success_generate, .lp-error_generate { background-color: #c0f4b4;font-size: 15px;padding: 10px;text-align: center;;margin-top: 20px;border-radius: 5px;border: 1px solid #3b9f24; }
    .lp-error_generate{ background-color: #f79999;border: 1px solid #771313; }
    .lp-success_generate p{ color: #326626; }
    .lp-error_generate p{ color: #921919; }
	.lp-refund_heading { padding: 5px;border-left: 5px solid #e08310;padding-left: 15px; }
	button:disabled{ opacity: .7;pointer-events: none; }
	#amount_box{ border-radius: 5px;height: 30px;border: 1px  solid #ddd;text-indent: 10px; }
	.lp-refund_flex{ display: flex;width: 100%;max-width: 1024px; }
	.lp-refund_flex .lp-refund_box{ border-radius: 0; }
	.chargeback_div .lp-refund_flex{ flex-wrap: wrap; }
	.chargeback_div .lp-refund_box{ width: 100%;max-width: 50%; }
	.chargeback_div .case_details{ max-width: 100%; }
	.upload-case{ height: 28px;margin-bottom: 0;border: 1px solid #ddd;width: 100%;border-radius: 5px; }
	.upload-case .lpay_button{ padding: 4px 25px;margin: 0;margin-top: 2px;margin-left: 2px !important; }
	#uploadCase{ opacity: 0;position: absolute;bottom: 0; }
	.chargeback_div .lpay_custom_table tbody td{ padding: 12px 10px !important; }
	.chargeback_div .lp-refund_box{ padding-bottom: 0 !important; }
	#error-chargebackAmount{ font-size: 10px;white-space: break-spaces;text-align: left;display: block; }
	#ui-datepicker-div{ z-index: 999 !important; }
</style>

</head>
<body id="mainBody">
	<s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
	<s:hidden id="partnerFlag" value="%{#session.USER.partnerFlag}" />
	<s:hidden value="%{glocalFlag}" id="gloc" />
	<input type="hidden" id="deliveryStatusFlag" />
	<input type="hidden" id="setSuperMerchant">
	<input type="hidden" id="retailMerchantFlag">
	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Unsettled Sale Transaction Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			
			<!-- /.col-md-3 mb-20 -->
			<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
				   		<s:select
							name="merchantEmailId"
							data-download="merchantPayId"
							data-var="merchantEmailId"
							class="selectpicker"
							id="merchant"
							data-submerchant="subMerchant"
							data-user="subUser"
							headerKey=""
							data-live-search="true"
							headerValue="ALL"
					   		list="merchantList"
							listKey="emailId"
					   		listValue="businessName"
							autocomplete="off"
						/>
					</div>
				</div>
			</s:if>
			<s:else>
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select
								name="merchantEmailId"
								data-download="merchantPayId"
								class="selectpicker"
								id="merchant"
								headerKey=""
								data-var="merchantEmailId"
								data-submerchant="subMerchant"
								data-user="subUser"
								data-live-search="true"
								headerValue="ALL"
								list="merchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none">
						<div class="lpay_select_group ">
							<label for="">Select Merchant</label>
							<s:select
								name="merchantEmailId"
								data-download="merchantPayId"
								data-var="merchantEmailId"
								data-live-search="true"
								class="selectpicker"
								id="merchant"
								list="merchantList"
								data-submerchant="subMerchant"
								data-user="subUser"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:else>
			</s:else>
			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-target="subMerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select
					   		data-id="subMerchant"
							data-download="subMerchantPayId"
							data-var="subMerchantEmailId"
							data-submerchant="subMerchant"
							data-user="subUser"
							name="subMerchantEmailId"
							class="selectpicker"
							id="subMerchant"
							list="subMerchantList"
							listKey="emailId"
							listValue="businessName"
							autocomplete="off"
							headerKey="ALL"
							headerValue="ALL" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantEmailId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantEmailId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<s:if test="%{#session.SUBUSERFLAG == true}">
				<div class="col-md-3 mb-20" data-target="subUser">
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <s:select data-id="subUser" data-download="subUserPayId" headerKey="ALL" headerValue="ALL" data-var="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subUser"> 
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <select name="subUserPayId" data-download="subUserPayId" data-var="subUserPayId" id="subUser" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Payment Method</label>
				   <s:select headerKey="" data-live-search="true" data-download="paymentType" data-var="paymentMethod" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.PaymentType@values()"
				   listValue="name" listKey="code" name="paymentType"
				   id="paymentMethod" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
				  <label for="">Date From</label>
				  <s:textfield type="text" data-download="dateFrom" onchange="dateBaseDownload()" data-var="dateFrom" id="dateFrom" name="dateFrom"
				  class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 mb-20 -->
			  <div class="col-md-3 mb-20">
				<div class="lpay_input_group">
				  <label for="">Date To</label>
				  <s:textfield type="text" data-download="dateTo" onchange="dateBaseDownload()" data-var="dateTo" id="dateTo" name="dateTo"
				  class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
				  <label for="">PG REF Number</label>
				  <s:textfield id="pgRefNum" data-download="transactionId" data-var="transactionId" class="lpay_input" name="transactionId"
				  type="text" value="" autocomplete="off" onblur="dateBaseDownload()"
				  onkeypress="javascript:return isNumber (event)" maxlength="16" ></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 mb-20 -->
			  <div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
				  <label for="">Order ID</label>
				  <s:textfield id="orderId" data-download="orderId" data-var="orderId" class="lpay_input" name="orderId"
				  type="text" value="" autocomplete="off"
				  onkeypress="return Validate(event);"
				  onblur="this.value=removeSpaces(this.value);dateBaseDownload();"></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 mb-20 -->
			  <div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
				  <label for="">Cust Email</label>
				  <s:textfield id="customerEmail" data-download="customerEmail" data-var="customerEmail" class="lpay_input"
				  name="customerEmail" type="text" value="" autocomplete="off"
				  onblur="validateEmail(this);"></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			<div class="col-md-3 mb-20 d-none slide-form-element" data-id="deliveryStatus">
				<div class="lpay_select_group">
				   <label for="">Delivery Status</label>
				   <select class="selectpicker" data-download="deliveryStatus" data-var="deliveryStatus" name="deliveryStatus" id="deliveryStatus">
						<option value="">Select Delivery Status</option>
						<option value="All">ALL</option>
					   <option value="DELIVERED">Delivered</option>
					   <option value="NOT DELIVERED">Not Delivered</option>
					   <option value="PENDING">Pending</option>
				   </select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
				   <label for="">Currency</label>
				   <s:select name="currency" data-download="currency" data-var="currency" id="currency" headerValue="ALL"
					headerKey="" list="currencyMap" class="selectpicker" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
				   <label for="">Settlement Type</label>
				   <s:select headerKey="ALL" data-download="partSettleFlag" data-var="partSettleFlag" headerValue="ALL" class="selectpicker"
				   list="#{'N':'Normal','Y':'Part'}" name="partSettleFlag" id = "partSettleFlag" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
				   <label for="">Transaction Flag</label>
				   <select class="selectpicker" title="ALL" data-selected-text-format="count>2" data-actions-box="true" multiple data-download="transactionFlag" data-var="transactionFlag" name="transactionFlag" id="transactionFlag">
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
				<input type="button" id="submit" value="View" class="lpay_button lpay_button-md lpay_button-secondary" />
				<input type="button" id="downloadButton" value="Download" class="lpay_button lpay_button-md lpay_button-primary" />
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
		<div class="filter-icon">
			<span class="fa fa-angle-down"></span> 
		</div>
		<!-- /.filter-icon -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Unsettled Sale Transaction Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 lpay_table_style-2">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="display" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>Merchant</th>
								<th>PG REF Num</th>
								<th>Order ID</th>
								<th>Payment Method</th>
								<th>Payment Region</th>
								<th>Date</th>
								<th>Total Amount</th>
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





	<s:form id="downloadUnsettledTransactionsReportAction" name="downloadUnsettledTransactionsReportAction" action="downloadUnsettledTransactionsReportAction">

	</s:form>

	

	
	<script type="text/javascript">

		updateFormEnabled();

	</script>

</body>
</html>