<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Sale Captured</title>
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
<script src="../js/saleCapture.js"></script>
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
					<h2 class="heading_text">Sale Transaction Filter</h2>
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
					<h2 class="heading_text">Sale Transaction Data</h2>
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

	<section class="lp-refund_section refund_div">
		<div class="lp-refund_inner">
			<div class="lp-refund_box">
				<div class="lp-refund_heading">
					<h3>Refund Details</h3>
				</div>
				<!-- /.lp-refund_heading -->
				<div class="lpay_table_wrapper">
					<table class="lpay_custom_table">
						<tr class="d-none">
							<td>Order Id</td>
							<td data-refund='orderId'></td>
						</tr>
						<tr class="d-none">
							<td>Merchant Name</td>
							<td data-refund='merchantName'></td>
						</tr>
						<tr class="d-none">
							<td>PG REF NUM</td>
							<td data-refund="pgRefNum"></td>
						</tr>
						<tr class="d-none">
							<td>Currency</td>
							<td data-refund='currencyCode'></td>
						</tr>
						<tr class="d-none">
							<td>Transaction Amount</td>
							<td data-refund='amount'></td>
						</tr>
						<tr class="d-none">
							<td>Chargeback Amount</td>
							<td data-refund='chargebackAmount'></td>
						</tr>
						<tr class="d-none">
							<td>Available For Refund</td>
							<td data-refund="refundAvailable"></td>
						</tr>
						<tr>
							<td>Transaction Type</td>
							<td data-refund="txnType"></td>
						</tr>
						<tr>
							<td>Refund Amount</td>
							<td><input id="amount_box" type="text" onkeyup="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);" onkeypress="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);" /></td>
						</tr>
						<tr>
							<td colspan="2" class="text-center">
								<button class="lpay_button lpay_button-md lpay_button-secondary"  onclick="removePopup()">Cancel</button>
								<!-- /.lpay_button lpay_button-md lpay_button-primary -->
								<button class="lpay_button lpay_button-md lpay_button-primary" id="refund-submit">Submit</button>
								<!-- /.lpay_button lpay_button-md lpay_button-primary -->
							</td>
						</tr>
					</table>
					<!-- /.lp-refund_details_table -->
				</div>
				<!-- /.lpay_table_wrapper -->
			</div>
			<!-- /.lp-refund_box -->
		</div>
		<!-- /.lp-refund_inner -->
	</section>
	<!-- /.lp-refund_section -->

	<s:form id="files" method="post" enctype="multipart/form-data">
		<section class="lp-refund_section chargeback_div">
			<div class="lp-refund_inner">
				<div class="lp-refund_flex">
					<div class="lp-refund_box">
						<div class="lp-refund_heading">
							<h3>Order Details</h3>
						</div>
						<!-- /.lp-refund_heading -->
						<div class="lpay_table_wrapper">
							<table class="lpay_custom_table">
								<tr>
									<td>Order Id</td>
									<td data-chargeback='orderId'></td>
								</tr>
								<tr>
									<td>Date</td>
									<td data-chargeback='createDate'>29-03-2021 12:00:00</td>
								</tr>
								<tr>
									<td>Merchat Name</td>
									<td data-chargeback="businessName">GandaMerchant</td>
								</tr>
								<tr>
									<td>Sub-Merchant Name</td>
									<td data-chargeback="subMerchantName"></td>
								</tr>
								<tr>
									<td>Card Number Mask</td>
									<td data-chargeback='cardNumber'>400000*******0002 </td>
								</tr>
								<tr>
									<td>Payment Method</td>
									<td data-chargeback='paymentType'>Debit Card (Visa) </td>
								</tr>
								<tr>
									<td>Card Issuer Info</td>
									<td data-chargeback='internalCardIssusserBank'>AXIS</td>
								</tr>
								<tr>
									<td>Email</td>
									<td data-chargeback="custEmail">md.zakaullah@Pg.com</td>
								</tr>
								<tr>
									<td>Country</td>
									<td data-chargeback="internalCustCountryName">India</td>
								</tr>
							</table>
							<!-- /.lp-refund_details_table -->
						</div>
						<!-- /.lpay_table_wrapper -->
					</div>
					<!-- /.lp-refund_box -->
					<div class="lp-refund_box">
						<div class="lp-refund_heading">
							<h3>Account Summary</h3>
						</div>
						<!-- /.lp-refund_heading -->
						<div class="lpay_table_wrapper">
							<table class="lpay_custom_table">
								<tr>
									<td>Currency</td>
									<td data-chargeback="currencyNameCode"></td>
								</tr>
								<tr class="">
									<td>Sale Amount</td>
									<td data-chargeback='authorizedAmount'>1349.00</td>
								</tr>
								<tr class="">
									<td>TDR (15.00% of B + D) [F]</td>
									<td data-chargeback='merchantTDR'>15.00</td>
								</tr>
								<tr class="">
									<td>Refunded Amount</td>
									<td data-chargeback="refundedAmount">0.00</td>
								</tr>
								<tr class="">
									<td>Available For Refund</td>
									<td data-chargeback='refundAvailable'>400000*******0002 </td>
								</tr>
								<tr>
									<td>Hold Amount Flag</td>
									<td>
										<label for="holdAmountFlag" class="checkbox-label unchecked mb-10">
											<input type="checkbox" id="holdAmountFlag" name="holdAmountFlag">
										</label>
									</td>
								</tr>
								<tr>
									<td>Chargeback Amount</td>
									<td>
										<input type="text" name="chargebackAmount" id="chargebackAmount" placeholder="Enter Chargeback Amount">
										<span id="error-chargebackAmount" class="text-danger text-right font-size-12 d-none"></span>
									</td>
								</tr>
							</table>
							<!-- /.lp-refund_details_table -->
						</div>
						<!-- /.lpay_table_wrapper -->
					</div>
					<!-- /.lp-refund_box -->
					<div class="lp-refund_box case_details">
						<div class="lp-refund_heading mb-20">
							<h3>Case Details</h3>
						</div>
						<!-- /.lp-refund_heading -->
						<div class="row">
							<div class="col-md-3 mb-20">
								<div class="lpay_input_group">
									<label for="">Target Date</label>
									<input type="text" onchange="updateFormEnabled();" name='targetDate' data-required='true' readonly placeholder="Please select date" id="targetDate" class="lpay_input">
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="col-md-3 mb-20">
								<div class="lpay_select_group">
									<label for="">Type <span class="text-danger">*</span></label>
									<s:select
										data-required='true'
										name="chargebackType"
										onchange="updateFormEnabled();"
										class="selectpicker"
										id="chargebackType"
										headerKey=""
										headerValue="Select Chargeback Type"
										list="@com.paymentgateway.crm.chargeback.util.ChargebackType@values()"
										listKey="code"
										listValue="name"
										autocomplete="off"
									/>
								</div>
								<!-- /.lpay_select_group -->
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<div class="lpay_input_group">
									<label for="">Add Comment</label>
									<input type="text" name="comments" data-required='true' placeholder="Please enter comment" id="comments" class="lpay_input">
									<span class="text-danger invisible d-block text-right font-size-12" id="error-commentId">This field is required.</span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="col-md-3 mb-20">
								<div class="lpay_input_group">
									<label for="">Upload Case</label>
									<label for="uploadCase" class='upload-case'>
										<button class="lpay_button lpay_button-sm lpay_button-primary">Browse</button>
										<input type="file" data-required='true' name="image" id="uploadCase" class="lpay_input">
										<span>PDF or CSV</span>
									</label>
									<!-- <input type="text" placeholder="Please enter comment" class="lpay_input"> -->
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="chargeback-input">

							</div>
							<!-- /.chargeback-input -->
							<div class="col-md-12 text-center mb-20">
								<span class="lpay_button lpay_button-md lpay_button-secondary" onclick="removePopup()">Cancel</span>
								<!-- /.lpay_button lpay_button-md lpay_button-secondary -->
								<button class="lpay_button lpay_button-md lpay_button-primary" id="chargebackSubmit">Generate Chargeback</button>
								<!-- /.lpay_button lpay_button-md lpay_button-primary -->
							</div>
							<!-- /.col-md-12 -->
						</div>
						<!-- /.row -->
					</div>
					<!-- /.lp-refund_box -->
				</div>
				<!-- /.lp-refund_flex -->
			</div>
			<!-- /.lp-refund_inner -->
		</section>
		<!-- /.lp-refund_section -->
	</s:form>

	<s:form name="chargeback" action="chargebackAction">
		<s:hidden name="orderId" id="orderIdc" value="" />
		<s:hidden name="payId" id="payIdc" value="" />
		<s:hidden name="refundedAmount" id="chargeback-refundedAmount" value="" />
		<s:hidden name="pgRefNum" id="chargeback-pgRefNum" value="" />
		<s:hidden name="refundAvailable" id="chargeback-refundAvailable" value="" />
		<s:hidden name="txnId" id="txnIdc" value="" />
		
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form> 


	<s:form name="refundDetails" action="refundConfirmAction">
		<s:hidden name="orderId" id="orderIdr" value="" />
		<s:hidden name="payId" id="payIdr" value="" />
		<s:hidden name="transactionId" id="txnIdr" value="" />
		<s:hidden name="amount" id="amountr" value="" />
		<s:hidden name="totalAmount" id="totalAmountr" value="" />
		
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

	<!-- download form -->
	<s:form id="downloadTransactionsReportAction" name="downloadTransactionsReportAction" action="downloadTransactionsReportAction">

	</s:form>

	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}

		var allowAlphaNumericSpecial = function(that) {
			that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
		}
	</script>

	<s:form name="manualRefundProcess" id="manualRefundProcess" action="manualRefundProcess">
		<s:hidden name="payId" id="payId" value="" />
		<s:hidden name="pgRefNum" id="pg-ref" value="" />
		<s:hidden name="refundedAmount" id="refundedAmount" value="" />
		<s:hidden name="refundAvailable" id="refundAvailable" value="" />
		<s:hidden name="chargebackAmount" id="chargebackAmount" value="" />
		<s:hidden name="chargebackStatus" id="chargebackStatus" value="" />

		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>

	<div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">

                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Amount has been transferred successfully.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->

                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Nothing Found Try Again.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->
	<script type="text/javascript">

		updateFormEnabled();

	</script>

</body>
</html>