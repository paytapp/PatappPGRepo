<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Performance Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- stylesheet -->
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/fonts.css" />
<!-- javascripts -->
<link rel="stylesheet" href="../css/bootstrap-datetimepicker.css">
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<!-- searchable select option -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script type="text/javascript" src="../js/moment-with-locales.js"></script>
<!-- <script type="text/javascript" src="../js/bootstrap.min.js"></script> -->
<script type="text/javascript" src="../js/bootstrap-datetimepicker.js"></script>
<!-- highcharts -->
<script src="../js/highcharts.js"></script>
<!-- <script src="../js/common-scripts.js"></script> -->


<style>


.w-35 {
	width: 35%;
}

input:focus, select:focus, textarea:focus, button:focus {
	outline: none;
}


.box2 {
	margin-top: 11px;
	padding: 11px;
}



.box1 .media-heading {
	text-align: center;
	font-size: 14px;
}





.box4 button {
	background: #5db85b;
	color: #fff;
	border: none;
	display: block;
	width: 200px;
	padding: 6px;
	margin: 0 auto;
	font-size: 13px;
}

.box2 select, .box2 input {
	border: 1px solid #d4d4d4;
}

.cards-count .media-left {
	min-width: 70px;
	text-align: center;
	font-size: 30px;
	color: #fff;
	line-height: 50px;
}

.cards-count .media-body>div {
	display: flex;
	justify-content: center;
}

.cards-count .media-body>div p {
	margin-left: 5px;
}

.cards-count .media-heading {
	line-height: normal;
	margin-bottom: 0;
	color: #7E7E7E;
}

.chartBox {
	background: #f8f8f8;
}

.cardTypeTxnDetails {
	background: #f8f8f8;
	border-radius: 5px;
	overflow: hidden;
}

.mytable, .mytable thead th {
	text-align: center;
	font-size: 13px;
}

.mytable thead th {
	font-size: 14px;
}


.mainDiv.txnf h3 {
	color: #002163;
	font-size: 16px;
	margin-top: 20px;
}

#showForParticular {
	display: none;
}

#checkboxes {
	display: none;
	border: 1px #dadada solid;
	height: 300px;
	overflow-y: scroll;
	position: Absolute;
	background: #fff;
	z-index: 1;
	margin-left: 5px;
}

#checkboxes label {
	width: 74%;
}

#checkboxes input {
	width: 18%;
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
	top: 40%;
	left: 45%;
	z-index: 100
}

.selectBox select {
	width: 100%;
}

.overSelect {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
}

label {
	color: #333;
	font-size: 13px;
}

.loader {
	border: 16px solid #f3f3f3; /* Light grey */
	border-top: 16px solid #3498db; /* Blue */
	border-radius: 50%;
	width: 120px;
	height: 120px;
	animation: spin 2s linear infinite;
}

.baseClass {
	width: 100%;
	float: left;
	text-align: center;
}

.form-control {
	margin-left: 0 !important;
	width: 100% !important;
}

.baseClass button {
	display: inline-block;
}

.baseClass1 {
	width: 100%;
	float: left;
	text-align: center;
	height: 50px !important;
}

.baseClass1 button {
	display: inline-block;
	height: 50px !important;
}

/* #submit1 {
	float: left;
	background: #46a145;
	color: #fff;
	border: none;
	display: block;
	max-width: 190px;
	width: 100%;
	padding: 6px;
	margin-left: 2%;
	margin-top: 3%;
	margin-bottom: 3%;
	border-radius: 5px;
	font-size: 13px;
} */

/* #submit1:hover {
	background: #5db85b;
} */

.sbmtBtn {
	float: left;
	background: #46a145;
	color: #fff;
	border: none;
	display: block;
	width: 190px;
	padding: 6px;
	margin-left: 1%;
	margin-top: 3%;
	margin-bottom: 3%;
	border-radius: 5px;
	font-size: 13px;
}

.sbmtBtn:hover {
	background: #5db85b;
}

/* #captured2 {
	background: #46a145;
	color: #fff;
	border: none;
	width: 200px;
	padding: 6px;
	margin-top: 3%;
	margin-bottom: 3%;
	border-radius: 5px;
	font-size: 13px;
} */

/* #captured2:hover {
	background: #5db85b;
} */
/* 
#settled3 {
	float: right;
	background: #46a145;
	color: #fff;
	border: none;
	display: block;
	width: 190px;
	padding: 6px;
	margin-right: 2%;
	margin-top: 3%;
	margin-bottom: 3%;
	border-radius: 5px;
	font-size: 13px;
	margin-left: 3%;
}

#settled3:hover {
	background: #5db85b;
} */

#settled4 {
	float: right;
	background: #46a145;
	color: #fff;
	border: none;
	display: block;
	width: 200px;
	padding: 6px;
	margin-top: 3%;
	margin-right: -16px;
	margin-bottom: 3%;
	border-radius: 5px;
	font-size: 13px;
}

#settled4:hover {
	background: #5db85b;
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

.mt-20 {
	margin-top: 20px !important;
}

#loading-image {
	position: absolute;
	top: 45%;
	left: 50%;
	z-index: 100;
	width: 15%;
}

.d-flex {
	display: flex;
}

.flex-wrap {
	flex-wrap: wrap;
}

.align-items-end {
	align-items: flex-end !important;
}

.align-items-start{
	align-items: flex-start !important;
}

.m-0 {
	margin: 0 !important;
}

.justify-content-center {
	justify-content: center;
}

.analytics-count .d-flex{	
	flex-direction: column;
	text-align: center;
}

.flex-row { flex-direction: row !important; }
.align-items-center { align-items: center !important; }
.ml-10 { margin-left: 10px !important; }
.cards-wrapper{
	display: flex;
}
</style>
</head>

<body>
	<!-- <div id="loading">
		<img id="loading-image" src="../image/loadingText.gif"
			alt="Sending SMS..." />
	</div> -->
	<div class="performance_report lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Performance Report Filter</h2>
				</div>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4">
				<div class="lpay_input_group">
					<label>Date From</label>
					<s:textfield type="text" id="dateFrom" name="dateFrom"
					class="lpay_input datePick" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4">
				<div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" id="dateTo" name="dateTo"
					class="lpay_input datePick" autocomplete="off" readonly="true" />
				</div>

				
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4">
				<div class="lpay_select_group">
					<label>Acquirer</label>
					<s:select
						name="merchant"
						class="selectpicker"
						multiple="true"
						id="acquirer"
						data-actions-box="true"
						list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
						listValue="name"
						data-live-search="true"
						listKey="code"
						title="Select Acquirer"
						autocomplete="off"
						data-selected-text-format="count"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mt-20">
				<div class="lpay_select_group">
					<label for="">Merchant</label>
					<s:if
						test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<s:select name="merchant" class="selectpicker"  id="merchants"
						headerKey="" data-live-search="true" headerValue="ALL" list="merchantList"
						listKey="emailId" listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
						<s:select name="merchant" class="selectpicker" id="merchants"
							headerKey="" data-live-search="true" headerValue="ALL" list="merchantList"
							listKey="emailId" listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mt-20">
				<div class="lpay_select_group">
					<label for="">Payment Type</label>
					<s:select headerKey="" data-live-search="true" headerValue="ALL" class="selectpicker"
					list="@com.paymentgateway.commons.util.PaymentType@values()"
					listValue="name" listKey="code" name="paymentMethod"
					id="paymentMethods" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mt-20">                    
				<div class="lpay_select_group">
					<label for="mopType">Mop Type <span class="text-danger">*</span></label>
					<s:select
						name="mopType"
						id="mopType"
						title="ALL"
						list="@com.paymentgateway.commons.util.MopType@values()"
						listValue="name"
						data-live-search="true"
						listKey="code"
						class="selectpicker lpay-input"
						multiple="true"
					/>
				</div>
				<!-- /.lpay_select_box -->                    
			</div>
			<div class="col-md-4 mt-20">
				<div class="lpay_select_group">
					<label for="">Txn Type</label>
					<s:select headerKey="" headerValue="Select Transaction Type"
					class="selectpicker"
					list="@com.paymentgateway.commons.util.TxnType@values()"
					listValue="name" listKey="code" name="txnType" id="txnTypes"
					autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mt-20">
				<div class="lpay_select_group">
					<label for="">Currency</label>
					<s:select name="currency" headerKey="ALL" headerValue="ALL" data-download="currency" data-var="currency" id="currency" list="currencyMap" class="selectpicker" />
				 </div>
				 <!-- /.lpay_select_group --> 
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-12 d-flex align-items-end justify-content-center mt-20">
				<button id="submit1" class="m-0 lpay_button lpay_button-md lpay_button-secondary">Submit</button>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- performance_report -->
	<div class="performance_report lpay_section white-bg box-shadow-box mt-70 p20">

		<div class="row box1">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Performance Report</h2>
				</div>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-sm-3 heading_style_2">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box green"><img class="media-object" src="../image/total-icon.png"
						alt="total"></span>
					<h2 class="heading_text"> <span id="totalTxnCount"><s:property value="%{statistics.totalTxnCount}"/></span> Total Transactions (Hits)</h2>
				</div>
			</div>
			<div class="col-sm-3 heading_style_2">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box blue"><img class="media-object" src="../image/success-icon.png"
						alt="total"></span>
					<h2 class="heading_text"> <span id="successTxnCount"><s:property value="%{statistics.successTxnCount}"/></span> Total Transactions (Captured)</h2>
				</div>
			</div>

			<div class="col-sm-3 heading_style_2">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box blue"><i class="fa fa-check"></i></span>
					<h2 class="heading_text"> <span id="totalCapturedTxnAmount"><s:property value="%{statistics.totalCapturedTxnAmount}"/></span> Total Amount (Captured)</h2>
				</div>
			</div>

			<div class="col-sm-3 heading_style_2">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box magenta"><img class="media-object" src="../image/successp-icon.png"
						alt="total"></span>
					<h2 class="heading_text"> <span id="successTxnPercent"><s:property value="%{statistics.successTxnPercent}"/></span> Captured Percentage</h2>
				</div>
			</div>
			<div class="col-sm-3 heading_style_2">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box brown"><img class="media-object" src="../image/avgtkt-icon.png"
						alt="total"></span>
					<h2 class="heading_text"> <span id="avgTkt"><s:property value="%{statistics.avgTkt}"/></span> Avg. Tkt Size</h2>
				</div>
			</div>

			<div class="col-sm-3 cards-count heading_style_2">
				<div class="heading_with_icon mb-30 align-items-start analytics-count">
					<span class="heading_icon_box"><i class="fa fa-home" aria-hidden="true"></i></span>
						<div class="heading_wrapper">
							<h2 class="heading_text">Domestic</h2>
							<div class="cards-wrapper">
								<h2 class="heading_text d-flex align-items-center"><span id="totalDomesticCapturedCount" class=""><b><s:property value="%{statistics.totalDomesticCapturedCount}"/></b></span>Captured</h2>
								<h2 class="heading_text d-flex align-items-center"><span id="totalDomesticCapturedPercentage" class=""><b><s:property value="%{statistics.totalDomesticCapturedPercentage}"/></b></span>Percentage</h2>
							</div>
							<!-- /.cards-wrapper -->
						</div>
						<!-- /.heading_wrapper -->
				</div>
			</div>
	
			<div class="col-sm-3 cards-count heading_style_2">
				<div class="heading_with_icon mb-30 align-items-start analytics-count">
					<span class="heading_icon_box green"><i class="fa fa-home" aria-hidden="true"></i></span>
					<div class="heading_wrapper">
						<h2 class="heading_text">International</h2>
						<div class="cards-wrapper">
							<h2 class="heading_text d-flex align-items-center"><span id="totalIntenationalCapturedCount" class=""><b><s:property value="%{statistics.totalIntenationalCapturedCount}"/></b></span>Captured</h2>
							<h2 class="heading_text d-flex align-items-center"><span id="totalIntenationalCapturedPercentage" class=""><b><s:property value="%{statistics.totalIntenationalCapturedPercentage}"/></b></span>Percentage</h2>
						</div>
						<!-- /.cards-wrapper -->
					</div>
					<!-- /.heading_wrapper -->
				</div>
			</div>
		</div>
	
		<div class="row box3">
			<div class="col-sm-5">
				<div class="chartBox" id="chartBox"></div>
			</div>
			<div class="col-sm-7">
				<div class="cardTypeTxnDetails" id="showForAll">
					<table class="table mytable table-hover lpay_table" class="">
						<thead class="lpay_table_head">
							<tr>
								<th>Payment Type</th>
								<th>Transactions Count</th>
								<th>Percentage Share</th>
								<th>Total Amount</th>
	
							</tr>
						</thead>
						<tbody>
							<tr>
								<td>Credit Card</td>
								<td><p id="CCTotalCount" class="media-heading"></p></td>
								<td><p id="CCTxnPercent" class="media-heading"></p></td>
								<td><p id="ccTotalAmount" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Debit Card</td>
								<td><p id="DCTotalCount" class="media-heading"></p></td>
								<td><p id="DCTxnPercent" class="media-heading"></p></td>
								<td><p id="dcTotalAmount" class="media-heading"></p></td>
							</tr>
							<!-- <tr>
								<td>International</td>
								<td><p id="INTotalCount" class="media-heading"></p></td>
								<td><p id="INTxnPercent" class="media-heading"></p></td>
								<td><p id="INTotalAmount" class="media-heading"></p></td>
							</tr> -->
							<tr>
								<td>UPI</td>
								<td><p id="UPTotalCount" class="media-heading"></p></td>
								<td><p id="UPTxnPercent" class="media-heading"></p></td>
								<td><p id="upTotalAmount" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Net Banking</td>
								<td><p id="NBTotalCount" class="media-heading"></p></td>
								<td><p id="NBTxnPercent" class="media-heading"></p></td>
								<td><p id="nbTotalAmount" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Wallet</td>
								<td><p id="WLTotalCount" class="media-heading"></p></td>
								<td><p id="WLxnPercent" class="media-heading"></p></td>
								<td><p id="wlTotalAmount" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>EMI</td>
								<td><p id="EMTotalCount" class="media-heading"></p></td>
								<td><p id="EMxnPercent" class="media-heading"></p></td>
								<td><p id="emTotalAmount" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Cash on Delivery</td>
								<td><p id="CDTotalCount" class="media-heading"></p></td>
								<td><p id="CDxnPercent" class="media-heading"></p></td>
								<td><p id="cdTotalAmount" class="media-heading"></p></td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="cardTypeTxnDetails" id="showForParticular">
					<table class="table mytable table-hover">
						<thead>
							<tr>
								<th>Status</th>
								<th>Transactions (Count)</th>
								<th>Transactions (%)</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td>Captured</td>
								<td><p id="captured" class="media-heading"></p></td>
								<td><p id="capturedPercent" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Failed</td>
								<td><p id="failed" class="media-heading"></p></td>
								<td><p id="failedPercent" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Cancelled</td>
								<td><p id="cancelled" class="media-heading"></p></td>
								<td><p id="cancelledPercent" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Invalid</td>
								<td><p id="invalid" class="media-heading"></p></td>
								<td><p id="invalidPercent" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Fraud</td>
								<td><p id="fraud" class="media-heading"></p></td>
								<td><p id="fraudPercent" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Dropped</td>
								<td><p id="dropped" class="media-heading"></p></td>
								<td><p id="droppedPercent" class="media-heading"></p></td>
							</tr>
							<tr>
								<td>Rejected</td>
								<td><p id="rejected" class="media-heading"></p></td>
								<td><p id="rejectedPercent" class="media-heading"></p></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

	</div>
	<!-- /.performance_report lpay_section white-bg box-shadow-box mt-70 p20 -->
	<div class="performance_report lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="paymentPerformanceType">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Payment Type Performance</h2>
					</div>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
			<div class="table-responsive lpay_table_wrapper">
				<table id="data-table"
					class="lpay_custom_table" width="100%">
					<thead class="lpay_table_head">
						<tr id="tableTh"></tr>
					</thead>
					<tbody id="tableData">

					</tbody>
				</table>
			</div>
			<!-- /.table-responsive -->
		</div>
		<!-- /.paymentPerformanceType -->
	</div>
	<!-- /.performance_report lpay_section white-bg box-shadow-box mt-70 p20 -->
	<div class="">
	
		<div class="container">
		
			

			<div id="PaymentTypePerformance"
				style="position: absolute; z-index: -1; opacity: 0; top: 0;">
				<h3>Payment Type Performance</h3>
				<div class="row box3">

					<div class="paymentPerfomance">
						<table class="table mytable table-striped table-hover">
							<thead>
								<tr>
									<th>Payment Type</th>
									<th>Total TXN</th>
									<th>Captured(%)</th>
									<th>Failed(%)</th>
									<th>Cancelled(%)</th>
									<th>Invalid(%)</th>
									<th>Fraud(%)</th>
									<th>Dropped(%)</th>
									<th>Rejected(%)</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td>Credit Card</td>
									<td><p id="totalCCTxn" class="media-heading"></p></td>
									<td><p id="totalCCSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCCFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCCCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalCCInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCCFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCCDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCCRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>Debit Card</td>
									<td><p id="totalDCTxn" class="media-heading"></p></td>
									<td><p id="totalDCSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalDCFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalDCCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalDCInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalDCFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalDCDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalDCRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>UPI</td>
									<td><p id="totalUPTxn" class="media-heading"></p></td>
									<td><p id="totalUPSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalUPFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalUPCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalUPInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalUPFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalUPDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalUPRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>Net Banking</td>
									<td><p id="totalNBTxn" class="media-heading"></p></td>
									<td><p id="totalNBSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalNBFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalNBCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalNBInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalNBFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalNBDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalNBRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>Wallet</td>
									<td><p id="totalWLTxn" class="media-heading"></p></td>
									<td><p id="totalWLSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalWLFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalWLCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalWLInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalWLFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalWLDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalWLRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>EMI</td>
									<td><p id="totalEMTxn" class="media-heading"></p></td>
									<td><p id="totalEMSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalEMFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalEMCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalEMInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalEMFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalEMDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalEMRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>Cash on Delivery</td>
									<td><p id="totalCDTxn" class="media-heading"></p></td>
									<td><p id="totalCDSuccessTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCDFailedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCDCancelledTxnPercent"
											class="media-heading"></p></td>
									<td><p id="totalCDInvalidTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCDFraudTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCDDroppedTxnPercent" class="media-heading"></p></td>
									<td><p id="totalCDRejectedTxnPercent"
											class="media-heading"></p></td>
								</tr>
								<tr>
									<td>NA</td>
									<td><p id="unknownTxnCount" class="media-heading"></p></td>
								</tr>
							</tbody>
						</table>
					</div>

				</div>
			</div>

		</div>
	</div>
	<script type="text/javascript">
		var _timeSetOneTime = "load";
		$(document).ready(function() {
			
			var _today = new Date();
			function setTime(){

				var _getDate = _today.getDate();
				var _getMonth = _today.getMonth() + 1;
				var _getYear = _today.getFullYear();
	
				if(_getDate < 10) {
					_getDate = '0' + _getDate;
				}
			
				if(_getMonth < 10) {
					_getMonth = '0' + _getMonth;
				}
				
				if($("#dateFrom").val() == "") {
					
					var _fullDate =  _getDate + '-' + _getMonth + '-' + _getYear + ' 00:00:00';
					$("#dateFrom").val(_fullDate);
					// $("#dateFrom").val(_getDate + '-' + _getMonth + '-' + _getYear + ' 00:00')
				}
			}
			
			$('#dateFrom').datetimepicker({
				format: 'DD-MM-YYYY HH:mm:ss', // HH = 00 01 ... 22 23
				showClose: true,
				ignoreReadonly: true,
				maxDate: _today
			});
			$('#dateTo').datetimepicker({
				format: 'DD-MM-YYYY HH:mm:ss', // HH = 00 01 ... 22 23
				showClose: true,
				ignoreReadonly: true,
				maxDate: _today,
			}).on('dp.change', function (e) {

				if(_timeSetOneTime == "load"){

					var _getDate = $(this).val().slice(0, $(this).val().indexOf(" "));
					var _getDate = _today.getDate();
					var _getMonth = _today.getMonth() + 1;
					var _getYear = _today.getFullYear();
		
					if(_getDate < 10) {
						_getDate = '0' + _getDate;
					}
				
					if(_getMonth < 10) {
						_getMonth = '0' + _getMonth;
					}

					$(this).val(_getDate + "-" + _getMonth + "-" + _getYear + " 00:00:00");

					_timeSetOneTime = "unload";
				}
			 });
	
			$("#dateFrom").val("");
	
			setTime();
		});
		
	
		function intChart(val1, val2, val3, val4, val5, val6, val7) {
			Highcharts.chart('chartBox', {
				chart : {
					plotBackgroundColor : null,
					plotBorderWidth : null,
					plotShadow : false,
					type : 'pie'
				},
				title : {
					text : 'Payment Types comparison'
				},
				tooltip : {
					pointFormat : '{series.name}: <b>{point.y:.2f}%</b>'
				},
				plotOptions : {
					pie : {
						allowPointSelect : true,
						cursor : 'pointer',
						dataLabels : {
							enabled : false
						},
						showInLegend : true
					}
				},
				series : [ {
					name : 'Brands',
					colorByPoint : true,
					data : [ {
						name : 'Credit Card',
						y : parseFloat(val1),
						sliced : false,
						selected : true
					}, {
						name : 'Debit Card',
						y : parseFloat(val2)
					}, {
						name : 'UPI',
						y : parseFloat(val3)
					}, {
						name : 'Net Banking',
						y : parseFloat(val4)
					}, {
						name : 'Wallet',
						y : parseFloat(val5)
					}, {
						name : 'EMI',
						y : parseFloat(val6)
					}, {
						name : 'Cash on Delivery',
						y : parseFloat(val7)
					} ]
				} ]
			}, function(chart) { // on complete
	
				chart.renderer.text('', 140, 120).css({
					color : '#4572A7',
					fontSize : '16px'
				}).add();
	
			});
		}
	
		function intChartPaymentWise(val1, val2, val3, val4, val5, val6, val7) {
			Highcharts.chart('chartBox', {
				chart : {
					plotBackgroundColor : null,
					plotBorderWidth : null,
					plotShadow : false,
					type : 'pie'
				},
				title : {
					text : 'Payment Types comparison'
				},
				tooltip : {
					pointFormat : '{series.name}: <b>{point.y:.2f}%</b>'
				},
				plotOptions : {
					pie : {
						allowPointSelect : true,
						cursor : 'pointer',
						dataLabels : {
							enabled : false
						},
						showInLegend : true
					}
				},
				series : [ {
					name : 'Brands',
					colorByPoint : true,
					data : [ {
						name : 'Captured',
						y : parseFloat(val1),
						sliced : false,
						selected : true
					}, {
						name : 'Failed',
						y : parseFloat(val2)
					}, {
						name : 'Cancelled',
						y : parseFloat(val3)
					}, {
						name : 'Invalid',
						y : parseFloat(val4)
					}, {
						name : 'Fraud',
						y : parseFloat(val5)
					}, {
						name : 'Dropped',
						y : parseFloat(val6)
					}, {
						name : 'Rejected',
						y : parseFloat(val7)
					} ]
				} ]
			}, function(chart) { // on complete
				chart.renderer.text('', 140, 120).css({
					color : '#4572A7',
					fontSize : '16px'
				}).add();
			});
		}
	
		var arrayVal = [ "Credit Card", "Debit Card","UPI","Net Banking",
				"Wallet", "EMI", "Cash on Delivery", "NA" ];
		function getReportDetail(data) {
			jQuery("#tableData").html("");
			var tableData = "";
	
			for (var i = 0; i < arrayVal.length; i++) {
				tableData += "<tr>";
				tableData += "<td>" + arrayVal[i] + "</td>";
				for (key in data[i]) {
					tableData += "<td>" + data[i][key] + "</td>";
				}
				tableData += "</tr>";
			}
			jQuery("#tableData").append(tableData);
	
			return true;
		}
		function getHeadingDetail(data) {
			jQuery("#tableTh").html("");
			var tableHeading = "";
			for (key in data) {
				tableHeading += "<th>" + data[key] + "</th>";
			}
			jQuery("#tableTh").append(tableHeading);
		}
	
		
	
		var expanded = false;
	
		var dt = new Date() - 1;
		$(document).ready(function(e){

			function statistics() {
			var acquirer = [];
	
			acquirer = $("#acquirer").val();
	
			var merchantEmailId = document.getElementById("merchants").value;
			var dateFrom = document.getElementById("dateFrom").value;
			var dateTo = document.getElementById("dateTo").value;
			var paymentMethods = document.getElementById("paymentMethods").value;
			var txnType = document.getElementById("txnTypes").value;
			var _mopType = $("#mopType").val();
			var _currency = $("#currency").val();
	
			
			
			if (merchantEmailId == '') {
				merchantEmailId = 'ALL'
			}
			if (paymentMethods == '') {
				paymentMethods = 'ALL'
			}
	
			if (acquirer == '' || acquirer == null) {
				acquirer = 'ALL'
			}else{
				acquirer = $("#acquirer").val().join();
			}
	
			if(_mopType == ''){
				_mopType = 'ALL'
			}else{
				_mopType = $("#mopType").val().join();
			}
	
			// document.getElementById("loading").style.display = "block";
			var _body = document.getElementsByTagName("body")[0];
			_body.classList.remove("loader--inactive");
			var token = document.getElementsByName("token")[0].value;
			$.ajax({
					url : "analyticsDataAction",
					type : "POST",
					timeout : 0,
					data : {
						paymentMethods : paymentMethods,
						dateFrom : dateFrom,
						dateTo : dateTo,
						merchantEmailId : merchantEmailId,
						token : token,
						acquirer : acquirer ,
						txnType : txnType,
						currency: _currency,
						mopType : _mopType.toString()
					},
					success : function(data) {
						getReportDetail(data.analyticsData.performanceData);
						getHeadingDetail(data.analyticsData.statusList);
	
						document.getElementById("totalTxnCount").innerHTML = data.analyticsData.totalTxnCount;
						document.getElementById("successTxnCount").innerHTML = data.analyticsData.successTxnCount;
						document.getElementById("totalCapturedTxnAmount").innerHTML = data.analyticsData.totalCapturedTxnAmount;
						document.getElementById("successTxnPercent").innerHTML = data.analyticsData.successTxnPercent;
						document.getElementById("avgTkt").innerHTML = data.analyticsData.avgTkt;
	
						document.getElementById("CCTxnPercent").innerHTML = data.analyticsData.CCTxnPercent;
						//document.getElementById("CCSuccessRate").innerHTML = data.analyticsData.CCSuccessRate;
						document.getElementById("DCTxnPercent").innerHTML = data.analyticsData.DCTxnPercent;
						//document.getElementById("DCSuccessRate").innerHTML = data.analyticsData.DCSuccessRate;
						document.getElementById("UPTxnPercent").innerHTML = data.analyticsData.UPTxnPercent;
						//document.getElementById("UPSuccessRate").innerHTML = data.analyticsData.UPSuccessRate;
						document.getElementById("NBTxnPercent").innerHTML = data.analyticsData.NBTxnPercent;
	
						document.getElementById("WLxnPercent").innerHTML = data.analyticsData.WLTxnPercent;
	
						document.getElementById("EMxnPercent").innerHTML = data.analyticsData.EMTxnPercent;
	
						document.getElementById("CDxnPercent").innerHTML = data.analyticsData.CDTxnPercent;
	
						// CREDIT CARD
						document.getElementById("CCTotalCount").innerHTML = data.analyticsData.totalCCCapturedCount;
	
						// DEBIT CARD
						document.getElementById("DCTotalCount").innerHTML = data.analyticsData.totalDCCapturedCount;
	
						// UPI 
						document.getElementById("UPTotalCount").innerHTML = data.analyticsData.totalUPCapturedCount;
	
						// NET BANKING 
						document.getElementById("NBTotalCount").innerHTML = data.analyticsData.totalNBCapturedCount;
	
						// Wallet
						document.getElementById("WLTotalCount").innerHTML = data.analyticsData.totalWLCapturedCount;
	
						//EMI 
						document.getElementById("EMTotalCount").innerHTML = data.analyticsData.totalEMCapturedCount;
	
						// CASH ON DELIVERY
						document.getElementById("CDTotalCount").innerHTML = data.analyticsData.totalCDCapturedCount;
	
						document.getElementById("captured").innerHTML = data.analyticsData.captured;
						document.getElementById("failed").innerHTML = data.analyticsData.failed;
						document.getElementById("cancelled").innerHTML = data.analyticsData.cancelled;
						document.getElementById("invalid").innerHTML = data.analyticsData.invalid;
						document.getElementById("fraud").innerHTML = data.analyticsData.fraud;
						document.getElementById("dropped").innerHTML = data.analyticsData.dropped;
						document.getElementById("rejected").innerHTML = data.analyticsData.rejected;
	
						document.getElementById("capturedPercent").innerHTML = data.analyticsData.capturedPercent;
						document.getElementById("failedPercent").innerHTML = data.analyticsData.failedPercent;
						document.getElementById("cancelledPercent").innerHTML = data.analyticsData.cancelledPercent;
						document.getElementById("invalidPercent").innerHTML = data.analyticsData.invalidPercent;
						document.getElementById("fraudPercent").innerHTML = data.analyticsData.fraudPercent;
						document.getElementById("droppedPercent").innerHTML = data.analyticsData.droppedPercent;
						document.getElementById("rejectedPercent").innerHTML = data.analyticsData.rejectedPercent;
	
						document.getElementById("unknownTxnCount").innerHTML = data.analyticsData.unknownTxnCount;
	
						document.getElementById("totalCCTxn").innerHTML = data.analyticsData.totalCCTxn;
						document.getElementById("totalDCTxn").innerHTML = data.analyticsData.totalDCTxn;
						document.getElementById("totalUPTxn").innerHTML = data.analyticsData.totalUPTxn;
	
						document.getElementById("totalNBTxn").innerHTML = data.analyticsData.totalNBTxn;
						document.getElementById("totalWLTxn").innerHTML = data.analyticsData.totalWLTxn;
						document.getElementById("totalEMTxn").innerHTML = data.analyticsData.totalEMTxn;
						document.getElementById("totalCDTxn").innerHTML = data.analyticsData.totalCDTxn;
	
						document.getElementById("ccTotalAmount").innerHTML = data.analyticsData.totalCCTxnAmount;
						document.getElementById("dcTotalAmount").innerHTML = data.analyticsData.totalDCTxnAmount;
						document.getElementById("upTotalAmount").innerHTML = data.analyticsData.totalUPTxnAmount;
						document.getElementById("nbTotalAmount").innerHTML = data.analyticsData.totalNBTxnAmount;
						document.getElementById("wlTotalAmount").innerHTML = data.analyticsData.totalWLTxnAmount;
						document.getElementById("emTotalAmount").innerHTML = data.analyticsData.totalEMTxnAmount;
						document.getElementById("cdTotalAmount").innerHTML = data.analyticsData.totalCDTxnAmount;
	
						document.getElementById("totalDomesticCapturedCount").innerHTML = data.analyticsData.totalDomesticCapturedCount;
						document
								.getElementById("totalDomesticCapturedPercentage").innerHTML = data.analyticsData.totalDomesticCapturedPercentage;
						document
								.getElementById("totalIntenationalCapturedCount").innerHTML = data.analyticsData.totalIntenationalCapturedCount;
						document
								.getElementById("totalIntenationalCapturedPercentage").innerHTML = data.analyticsData.totalIntenationalCapturedPercentage;
	
						if (paymentMethods == 'ALL') {
							intChartPaymentWise("0.00", "0.00", "0.00", "0.00",
									"0.00", "0.00", "0.00");
							intChart(data.analyticsData.CCTxnPercent,
									data.analyticsData.DCTxnPercent,
									data.analyticsData.UPTxnPercent,
									data.analyticsData.NBTxnPercent,
									data.analyticsData.WLTxnPercent,
									data.analyticsData.EMTxnPercent,
									data.analyticsData.CDTxnPercent);
						} else {
							intChart("0.00", "0.00", "0.00", "0.00", "0.00",
									"0.00", "0.00");
							intChartPaymentWise(
									data.analyticsData.capturedPercent,
									data.analyticsData.failedPercent,
									data.analyticsData.cancelledPercent,
									data.analyticsData.invalidPercent,
									data.analyticsData.fraudPercent,
									data.analyticsData.droppedPercent,
									data.analyticsData.rejectedPercent);
						}
	
						var _functionLoaded = intChartPaymentWise;
						if(_functionLoaded){
							_body.classList.add("loader--inactive");
						}
						
					},
					error : function(data) {
						setInterval(function() {
							_body.classList.add("loader--inactive");
						}, 1000);
					}
				});
	
		}

		statistics();

			$('#submit1').click(function() {
				var dateFrom = document.getElementById('dateFrom').value.split("-"), dateTo = document.getElementById('dateTo').value.split("-");
				var _newDateFrom = new Date(dateFrom[2].slice(0, dateFrom[2].indexOf(" ")).toString() + '-' + dateFrom[1].toString() + '-' + dateFrom[0].toString() + dateFrom[2].slice(dateFrom[2].indexOf(" ")));
				var _newDateTo = new Date(dateTo[2].slice(0, dateTo[2].indexOf(" ")).toString() + '-' + dateTo[1].toString() + '-' + dateTo[0].toString() + dateTo[2].slice(dateTo[2].indexOf(" ")));
				var txnTypes = $("#txnTypes").val();
				var _calculatTime = _newDateTo.getTime() - _newDateFrom.getTime();
				var _days = Math.floor(_calculatTime / (1000 * 3600 * 24));
				if (txnTypes != "") {
					if(_days > 31){
						alert('No. of days can not be more than 31');
					}else{
						if(_newDateTo >= _newDateFrom ){
							if ($('#paymentMethods')
									.val() == "") {
								$(
										'#showForParticular')
										.hide();
								$('#showForAll')
										.show();
								$(
										'#PaymentTypePerformance')
										.show();
							} else {
								$('#showForAll')
										.hide();
								$(
										'#PaymentTypePerformance')
										.hide();
								$(
										'#showForParticular')
										.show();
							}
			
							statistics();
						}else{
							alert("Date from should be lesser then date to")
						}
					}
						// var diffDays = Math.round(Math.abs((myDateFrom.getTime() - myDateTo.getTime())/ (oneDay)));
					// if (diffDays > 31) {
					// 	alert('No. of days can not be more than 31');
					// } else {
					// }
					
				} else {
					alert("Select tranasaction type.");
				}
			});
		})
	</script>
</body>
</html>

