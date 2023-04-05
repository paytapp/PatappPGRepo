<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Summary Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script type="text/javascript" src="../js/summaryReport.js"></script>
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
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
	<input type="hidden" id="setSuperMerchant">
	<section class="summary-report lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Summary Report Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">PG REF Num</label>
					<s:textfield
						id="pgRefNum"
						class="lpay_input"
						name="pgRefNum"
						type="text"
						value=""
						autocomplete="off"
						onkeypress="return isNumber(event)"
						maxlength="16"
						onblur="checkRefNo()">
					</s:textfield>
					<span id="validRefNo" style="color:red; display:none; margin-left:5px; margin-bottom: 5px;">Please Enter 16 Digit PG Ref No.</span>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Select Merchant</label>
					<s:select
						name="merchants"
						class="selectpicker"
						id="merchants"
						headerKey=""
						headerValue="ALL"
						list="merchantList"
						listKey="emailId"
						listValue="businessName"
						autocomplete="off"
						data-live-search="true"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
						<label>Sub Merchant</label>
						<div class="txtnew">
							<s:select
								data-id="subMerchant"
								name="subMerchant"
								class="selectpicker"
								id="subMerchant"
								list="subMerchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-id="submerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<div class="txtnew">
							<select name="subMerchant" id="subMerchant">
							<!-- <option value="">Select Sub Merchant</option> -->
							</select>
						</div>
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 -->								
			</s:else>

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Acquirer</label>
					<s:select
						data-size="5"
						title="ALL"
						multiple="true"
						data-selected-text-format="count>2"
						data-actions-box="true"
						list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
						listValue="name"
						listKey="code"
						id="acquirer"
						name="acquirer"
						class="selectpicker"
						value="acquirer"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Transaction Type</label>
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'SALE':'SALE','REFUND':'REFUND'}"
						name="transactionType"
						id="transactionType"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Payment Method</label>
					<s:select
						name="paymentMethods"
						id="paymentMethods"
						headerValue="ALL"
						headerKey="ALL"
						list="@com.paymentgateway.commons.util.PaymentType@values()"
						listValue="name"
						listKey="code" 
						class="selectpicker"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Currency</label>
					<s:select
						name="currency"
						id="currency"
						headerValue="ALL"
						headerKey="ALL"
						list="currencyMap"
						class="selectpicker"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Settlement Type</label>
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'N':'Normal','Y':'Part'}"
						name="partSettleFlag"
						id="partSettleFlag"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield
						type="text"
						readonly="true"
						id="dateFrom"
						name="dateFrom"
						class="lpay_input"
						autocomplete="off"
						onchange="dateBaseDownload();"
					/>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield
						type="text"
						readonly="true"
						id="dateTo"
						name="dateTo"
						class="lpay_input"
						autocomplete="off"
						onchange="dateBaseDownload();"
					/>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Transaction Region</label>
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'INTERNATIONAL':'International','DOMESTIC':'Domestic'}"
						name="paymentsRegion"
						id="paymentsRegion"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Card Holder Type</label>
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						class="selectpicker"
						list="#{'CONSUMER':'Consumer','COMMERCIAL':'Commercial','PREMIUM':'Premium'}"
						name="cardHolderType"
						id="cardHolderType"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Mop Type</label>
					<s:select
						name="mopType"
						id="mopType"
						headerValue="ALL"
						headerKey="ALL"
						list="@com.paymentgateway.commons.util.MopTypeUI@values()"
						listValue="name"
						listKey="code" 
						class="selectpicker"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Transaction Flag</label>
				   <select class="selectpicker" title="ALL" multiple data-selected-text-format="count>2" data-actions-box="true" data-download="transactionFlag" data-var="transactionFlag" name="transactionFlag" id="transactionFlag">
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
				<input type="submit" id="submit" value="View" class="lpay_button lpay_button-md lpay_button-secondary">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="downloadSummaryReport" onclick="downloadSummaryReport(this, event);">Download</button>
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
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="summary-report lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Summary Report Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 lpay_table_style-2">
				<div class="lpay_table">
					<table id="summaryReportDataTable" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>PG Ref Num</th>
								<th>Capture Date</th>
								<th>Payment Method</th>
								<th>Mop Type</th>
								<th>Order Id</th>
								<th>Business Name</th>
								<th>Total Amount</th>
								<th>Merchant Amount</th>
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

	<form id="downloadSummaryReportAction" name="downloadSummaryReportAction" action="downloadSummaryReportAction">
		<input type="hidden" name="merchantEmailId" id="merchants-summary">
		<input type="hidden" name="subMerchantEmailId" id="subMerchant-summary">
		<input type="hidden" name="transactionType" id="transactionType-summary">
		<input type="hidden" name="paymentMethods" id="paymentMethods-summary">
		<input type="hidden" name="acquirer" id="acquirer-summary">
		<input type="hidden" name="currency" id="currency-summary">
		<input type="hidden" name="dateFrom" id="dateFrom-summary">
		<input type="hidden" name="dateTo" id="dateTo-summary">
		<input type="hidden" name="paymentsRegion" id="paymentsRegion-summary">
		<input type="hidden" name="cardHolderType" id="cardHolderType-summary">
		<input type="hidden" name="mopType" id="mopType-summary">
		<input type="hidden" name="partSettleflag" id="partSettleFlag-summary">
		<input type="hidden" name='transactionFlag' id="transactionFlag-summary">
	</form>

	<script src="../js/summary-report-script.js"></script>
</body>
</html>
