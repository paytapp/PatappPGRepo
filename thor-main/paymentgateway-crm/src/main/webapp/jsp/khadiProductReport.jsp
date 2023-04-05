<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Product Wise Report</title>
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
</head>
<body id="mainBody">
	<s:hidden value="%{#session.USER.UserType}" data-id="userType"></s:hidden>
	<s:hidden value="%{glocalFlag}" id="gloc" />
	<input type="hidden" id="deliveryStatusFlag" />
	<input type="hidden" id="setSuperMerchant">
	<s:hidden type="hidden" value="%{#session.SUBUSERFLAG}" />
	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">

			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Product Wise Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">PG REF Number</label>
				<s:textfield id="transactionId" class="lpay_input" data-var="transactionId" name="transactionId"
				type="text" value="" autocomplete="off"
				onkeypress="javascript:return isNumber (event)" maxlength="16" ></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Order ID</label>
				<s:textfield id="orderId" class="lpay_input" data-var="orderId" name="orderId"
				type="text" value="" autocomplete="off"
				onkeypress="return Validate(event);"
				onblur="this.value=removeSpaces(this.value);"></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">SKU Code</label>
					<s:textfield
						id="SKUCode"
						class="lpay_input"
						data-var="SKUCode"
						name="SKUCode"
						maxlength="40"
						type="text"
						oninput="allowAlphaNumericSpecial(this)"
						autocomplete="off">
					</s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Category Code</label>
					<s:textfield
						id="categoryCode"
						class="lpay_input"
						data-var="categoryCode"
						name="categoryCode"
						maxlength="40"
						type="text"
						oninput="allowAlphaNumericSpecial(this)"
						autocomplete="off">
					</s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>			
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Merchant</label>

				<s:if
				   test="%{#session.USER.UserType.name()=='RESELLER'}">
				   <s:select name="merchantEmailId" class="selectpicker"
					   id="merchant" headerKey="" data-submerchant="subMerchant" data-user="subUser" data-var="merchantEmailId" data-live-search="true" headerValue="ALL"
					   list="merchantList" listKey="emailId"
					   listValue="businessName" autocomplete="off" />
				</s:if>
				<s:else>
				<s:if
				   test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
				   <s:select name="merchantEmailId" class="selectpicker"
					   id="merchant" headerKey="ALL" data-var="merchantEmailId" data-submerchant="subMerchant" data-user="subUser" data-live-search="true" headerValue="ALL"
					   list="merchantList" listKey="emailId"
					   listValue="businessName" autocomplete="off" />
				</s:if>
				
					<s:else>
						<s:select name="merchantEmailId" data-submerchant="subMerchant" data-user="subUser" class="selectpicker" id="merchant"
							list="merchantList" data-var="merchantEmailId" listKey="emailId"
							listValue="businessName" autocomplete="off" />
					</s:else>
				</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->

			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-target="subMerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" data-submerchant="subMerchant" data-user="subUser" data-var="subMerchantEmailId" name="subMerchantEmailId" class="selectpicker" id="subMerchant"
							list="subMerchantList" listKey="emailId" headerKey="ALL" headerValue="ALL"
							listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>			
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantEmailId" data-submerchant="subMerchant" data-user="subUser" data-var="subMerchantEmailId" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>

			<s:if test="%{#session.SUBUSERFLAG == true}">
				<div class="col-md-3 mb-20" data-target="subUser">
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <s:select data-id="subUser" data-var="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser"
							list="subUserList" listKey="emailId" headerKey="ALL" headerValue="ALL"
							listValue="businessName" autocomplete="off" />
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

			<div class="col-md-3 mb-20 d-none" data-id="deliveryStatus">
				<div class="lpay_select_group">
				   <label for="">Delivery Status</label>
				   <select class="selectpicker" data-var="deliveryStatus" name="deliveryStatus" id="deliveryStatus">
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

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Payment Method</label>
				   <s:select headerKey="" data-var="paymentType" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.PaymentType@values()"
				   listValue="name" listKey="code" name="paymentType"
				   id="paymentType" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<!-- <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Transaction Type</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="txnTypelist" listValue="name" listKey="code"
				   name="transactionType" id="transactionType" autocomplete="off"
				   value="name" />
				</div>				
			</div> -->
			
			
			<!-- <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Status</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="lst" name="status" id="status" value="name"
				   listKey="name" listValue="name" autocomplete="off" />
				</div>				
			</div>			 -->

			<s:if test="%{#session.USER.UserType.name()=='MERCHANT'}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Currency</label>
					   <s:select name="currency" data-var="currency" id="currency"  list="currencyMap" class="selectpicker" />
					</div>
					<!-- /.lpay_select_group --> 
				</div>
				<!-- /.col-md-3 mb-20 -->
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Currency</label>
					   <s:select name="currency" data-var="currency" id="currency" headerValue="ALL"
						headerKey="" list="currencyMap" class="selectpicker" />
					</div>
					<!-- /.lpay_select_group --> 
				</div>
				<!-- /.col-md-3 mb-20 -->
			</s:else>
			

			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" id="dateFrom" data-var="dateFrom" name="dateFrom"
				class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" id="dateTo" data-var="dateTo" name="dateTo"
				class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary" />
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Product Wise Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="display" cellspacing="0"
							width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th style='text-align: center; text-decoration: none !important;'>Txn Id</th>
									<th style='text-align: center'>Pg Ref Num</th>
									<th style='text-align: center'>Order ID</th>
									<th style='text-align: center'>Vendor Name</th>
									<th style='text-align: center'>Vendor ID</th>
									<th style='text-align: center'>Product ID</th>
									<th style='text-align: center'>SKU Code</th>
									<th style='text-align: center'>Category Code</th>
									<th style='text-align: center'>Txn Type</th>
									<th style='text-align: center'>Payment Type</th>
									<th style='text-align: center'>Mop Type</th>
									<th style='text-align: center'>Payment Region</th>
									<th style='text-align: center'>Card Holder Type</th>
									<th style='text-align: center'>Product Price</th>
									<th style='text-align: center'>Total Amount</th>
									<th style='text-align: center'>Currency</th>
									<th style='text-align: center'>Refund Days</th>
									<th style='text-align: center'>Action</th>
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
	<script src="../js/product-report.js"></script>

	<s:form name="manualRefundProcess" id="manualRefundProcess" action="khadiManualRefundProcess">
		<s:hidden name="payId" id="payId" value="" />
		<s:hidden name="pgRefNum" id="pg-ref" value="" />
		<s:hidden name="refundedAmount" id="refundedAmount" value="" />
		<s:hidden name="refundAvailable" id="refundAvailable" value="" />
		<s:hidden name="objectId" id="setId" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}

		var allowAlphaNumericSpecial = function(that) {
			that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
		}
	</script>
</body>
</html>