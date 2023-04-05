<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
	<title>Revenue Report</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<!-- stylesheet -->
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<!-- <link href="../css/select2.min.css" rel="stylesheet" /> -->
	<link href="../css/fonts.css" />
	<script src="../js/jquery.min.js"></script>
	<!-- javascripts -->
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/common-scripts.js"></script>
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<!-- <script src="../js/jquery.select2.js" type="text/javascript"></script> -->
	<script src="../js/user-script.js"></script>
	<script src="../js/tabs.js"></script>
	<script type="text/javascript">
		var acquirerString = "";
		$(document).ready(function() {
			// Initialize select2
			// $("#merchant").select2();
			statisticsAction();

			$("#acquirer").on("change", function(e) {
				var _val = $(this).val();
				acquirerString = _val.join();
			});
		
		});
	
		function statisticsAction() {
			var merchantEmailId = document.getElementById("merchant").value;
			var subMerchantEmailId = document.getElementById("subMerchant").value;
			var dateFrom = document.getElementById("dateFrom").value;
			var dateTo = document.getElementById("dateTo").value;
			var paymentMethods = document.getElementById("paymentMethods").value;
			var mopType = document.getElementById("mopType").value;
			var _currency = document.querySelector("#currency").value;
			//var currency = document.getElementById("currency").value;
			var paymentsRegion = document.getElementById("paymentsRegion").value;
			var cardHolderType = document.getElementById("cardHolderType").value;
			//var pgRefNum = document.getElementById("pgRefNum").value;
			// var transactionType = document.getElementById("transactionType").value;
			var transactionType="ALL";
			var statusType = document.getElementById("statusType").value;

			if (merchantEmailId == '') {
				merchantEmailId = 'ALL'
			}
			if (paymentMethods == '') {
				paymentMethods = 'ALL'
			}

			if (paymentsRegion == '') {
				paymentsRegion = 'ALL'
			}
			if (cardHolderType == '') {
				cardHolderType = 'ALL'
			}
		/* 	if (transactionType == '') {
				transactionType = 'ALL'
			} */

			if (acquirerString == '') {
				acquirerString = 'ALL'
			}
			
			if(statusType=='Settled') {
				var _settled = document.querySelectorAll("[data-settled]");
				_settled.forEach(function(index, array, element){
					index.innerText = index.attributes["data-settled"].nodeValue;
				})
			} else {
				var _settled = document.querySelectorAll("[data-captured]");
				_settled.forEach(function(index, array, element){
					index.innerText = index.attributes["data-captured"].nodeValue;
				})
			}

			var token = document.getElementsByName("token")[0].value;
			var _body = document.getElementsByTagName("body")[0];
			_body.classList.remove("loader--inactive");

			$.ajax({
				url : "summaryReportCountAction",
				type : "POST",
				timeout : 0,
				data : {
					paymentMethods : paymentMethods,
					dateFrom : document.getElementById("dateFrom").value,
					dateTo : document.getElementById("dateTo").value,
					merchantEmailId : merchantEmailId,
					currency: _currency,
					subMerchantEmailId : subMerchantEmailId,
					mopType : mopType,
					transactionType : transactionType,
					paymentsRegion : paymentsRegion,
					cardHolderType : cardHolderType,
					acquirer : acquirerString,
					statusType : statusType,
					draw : 1,
					length : 1,
					start : 1,
					token : token,
				},
				success : function(data) {	
					if(data.transactionCountSearch !== null) {
						//document.getElementById("dvMerchantName").innerHTML = data.transactionCountSearch.merchantName;
						//document.getElementById("dvAcquirerName").innerHTML = data.transactionCountSearch.acquirer;
						//document.getElementById("dvPaymentMethod").innerHTML = data.transactionCountSearch.paymentMethod;

						document.getElementById("dvSaleSettledCount").innerHTML = data.transactionCountSearch.saleSettledCount;
						document.getElementById("dvSaleSettledAmount").innerHTML = data.transactionCountSearch.saleSettledAmount;
						document.getElementById("dvPgSaleSurcharge").innerHTML = data.transactionCountSearch.pgSaleSurcharge;
						document.getElementById("dvAcquirerSaleSurcharge").innerHTML = data.transactionCountSearch.acquirerSaleSurcharge;
						document.getElementById("dvPgSaleGst").innerHTML = data.transactionCountSearch.pgSaleGst;
						document.getElementById("dvAcquirerSaleGst").innerHTML = data.transactionCountSearch.acquirerSaleGst;
	
						document.getElementById("dvRefundSettledCount").innerHTML = data.transactionCountSearch.refundSettledCount;
						document.getElementById("dvRefundSettledAmount").innerHTML = data.transactionCountSearch.refundSettledAmount;
						document.getElementById("dvPgRefundSurcharge").innerHTML = data.transactionCountSearch.pgRefundSurcharge;
						document.getElementById("dvAcquirerRefundSurcharge").innerHTML = data.transactionCountSearch.acquirerRefundSurcharge;
						document.getElementById("dvPgRefundSurcharge").innerHTML = data.transactionCountSearch.pgRefundSurcharge;
						document.getElementById("dvPgRefundGst").innerHTML = data.transactionCountSearch.pgRefundGst;
						document.getElementById("dvAcquirerRefundGst").innerHTML = data.transactionCountSearch.acquirerRefundGst;
						//document.getElementById("dvTotalMerchantAmount").innerHTML = data.transactionCountSearch.totalMerchantAmount;				
						document.getElementById("dvMerchantSaleSettledAmount").innerHTML = data.transactionCountSearch.merchantSaleSettledAmount;
						document.getElementById("dvMerchantRefundSettledAmount").innerHTML = data.transactionCountSearch.merchantRefundSettledAmount;
						document.getElementById("dvTotalProfit").innerHTML = data.transactionCountSearch.totalProfit;	
					}
					setTimeout(function() {
						_body.classList.add("loader--inactive");
					}, 1000);
				},
				error : function(data) {
					alert("Something went wrong!");
					setTimeout(function() {
						_body.classList.add("loader--inactive");
					}, 1000);
				}
			});
		}
	</script>

	<style>

		.media-heading {
			font-size: 18px;
			display: block;
			width: 100%;
			text-align: center;
		}

		.heading_with_icon{
			flex-wrap: wrap;
		}

		input:focus, select:focus, textarea:focus, button:focus { outline: none; }
		.box2 { padding: 5px 11px; }
		.box1, .box3 { padding: 11px; }
		.box2 input, .box2 select, .box2 button { font-size: 13px; }
		.btnSec button {
			background: #5db85b;
			color: #fff;
			border: none;
			display: block;
			width: 200px;
			padding: 6px;
			margin: 0 auto;
			font-size: 13px;
		}

		.box2 select, .box2 input { border: 1px solid #d4d4d4; }

		.mainDiv.txnf h3 {
			color: #002163;
			font-size: 16px;
			margin-top: 20px;
			margin: 0px;
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

		#checkboxes label { width: 74%; }
		#checkboxes input { width: 18%; }
		.selectBox select { width: 95%; }
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

		.boxSec {
			text-align: center;
			margin-bottom: 12px;
			background-color: #f8f8fc;
			cursor: pointer;
			transition: all 0.6s ease;
			padding: 10px 0;
			border-radius: 7px;
			box-shadow: 0 2px 2px 0 rgba(153, 153, 153, 0.14), 0 3px 1px -2px rgba(153, 153, 153, 0.2), 0 1px 5px 0 rgba(153, 153, 153, 0.12);

		}

		.boxSec h4 { margin: 0; }

		.boxSec:hover {
			background: #002163;
			color: #fff;
			transition: all 0.6s ease;
		}

		.boxSec:hover p {
			color: #fff;
			transition: all 0.6s ease;
		}

		.boxSec h4 { font-size: 25px; }

		.boxSec p {
			color: #002163;
			font-size: 14px;
			transition: all 0.6s ease;
		}

		.txnf h3 { margin-bottom: 15px !important; }

		label {
			margin-bottom: 0px !important;
			margin-left: -2px !important;
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
			top: 45%;
			left: 50%;
			z-index: 100;
			width: 15%;
		}

		.revenvueData-box span {
			display: block;
			text-align: center;
			font-size: 14px;
			padding-bottom: 10px;
			margin-bottom: 10px;
			border-bottom: 1px solid #ddd;
		}

		.revenvueData-box h3 {
			font-size: 14px !important;
			font-weight: 500;
			color: #242424;
		}

		.revenvueData-box {
			padding: 20px;
			font-size: 11px;
			background-color: #f5f5f5;
			border-radius: 5px;
			box-shadow: 0 0 5px rgb(0 0 0 / 10%);
			margin-bottom: 20px;
			text-align: center;
		}

	</style>
</head>

<body>
	<!-- <div id="loading">
		<img id="loading-image" src="../image/loadingText.gif"
			alt="Sending SMS..." />
	</div> -->
	

	<!-- /.dashboard_div lpay_section white-bg box-shadow-box mt-70 p20 -->
	<section class="revenueReport lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-20">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Revenue Report Filter</h2>
				</div>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="merchant">Merchant</label>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<s:select
							name="merchant"
							class="selectpicker"
							id="merchant"
							data-submerchant="subMerchant"
							headerKey=""
							data-live-search="true"
							headerValue="ALL"
							list="merchantList"
							listKey="emailId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:if>
					<s:else>
						<s:select
							name="merchant"
							class="selectpicker"
							id="merchants"
							headerKey=""
							data-live-search="true"
							headerValue="ALL"
							list="merchantList"
							listKey="emailId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:else>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-3 mb-20 d-none" data-target="subMerchant">
				<div class="lpay_select_group">
					<label for="subMerchant">Sub Merchant</label>
					<select
						headerKey=""
						headerValue="ALL"
						class="selectpicker"
						name="subMerchantEmailId"
						id="subMerchant"
						autocomplete="off"
						value="">
					</select>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="paymentMethods">Payment Method</label>
					<s:select
						headerKey=""
						headerValue="ALL"
						class="selectpicker"
						list="@com.paymentgateway.commons.util.PaymentType@values()"
						listValue="name"
						listKey="code"
						name="paymentMethods"
						id="paymentMethods"
						autocomplete="off"
						value=""
					/>
				</div>
				<!-- /.lpay_select_box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label>Date From</label>
					<s:textfield
						type="text"
						id="dateFrom"
						name="dateFrom"
						class="lpay_input"
						autocomplete="off"
						readonly="true"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label>Date To</label>
					<s:textfield
						type="text"
						id="dateTo"
						name="dateTo"
						class="lpay_input"
						autocomplete="off"
						readonly="true"
					/>
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label>Acquirer</label>
					<s:select
						name="merchant"
						class="selectpicker"
						multiple="true"
						data-actions-box="true"
						id="acquirer"
						list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
						title="Select Acquirer"
						listValue="name"
						listKey="code"
						autocomplete="off"
						data-selected-text-format="count"
					/>
				</div>
				<!-- /.lpay_select_box -->
			</div>
			<!-- /.col-md-3 -->
			
			
			<!-- Replace MopTypeUI to MopType after discussed with Shaiwal sir -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label>Mop Type</label>
					<s:select name="mopType" id="mopType" headerValue="ALL"
						headerKey="ALL"
						list="@com.paymentgateway.commons.util.MopType@values()"
						listValue="name" listKey="code" class="selectpicker" />
				</div>
				<!-- /.lpay_select_box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label>Card Holder Type</label>
					<s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
					list="#{'CONSUMER':'Consumer','COMMERCIAL':'Commercial', 'PREMIUM':'Premium'}"
					name="cardHolderType" id="cardHolderType" />
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label>TXN Region</label>
					<s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
					list="#{'INTERNATIONAL':'International','DOMESTIC':'Domestic'}"
					name="paymentsRegion" id="paymentsRegion" />
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label>Status</label>
					<s:select class="selectpicker"
					list="#{'Settled':'Settled','Captured':'Captured'}"
					name="statusType" id="statusType" />
				</div>
				<!-- /.lpay_select_box -->
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
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
		<div class="filter-icon">
			<span class="fa fa-angle-down"></span> 
		</div>
		<!-- /.filter-icon -->
	</section>
	<!-- /.revenueReport lpay_section white-bg box-shadow-box mb-70 p20 -->
<div class="row">
	<div class="col-md-4">
		<div class="">
			<div class="revenueBox lpay_section white-bg box-shadow-box mt-70 p20">
				<div class="heading_with_icon">
					<span class="heading_icon_box">S</span>
					<h2 class="heading_text" data-settled="Sale Settlement Count" data-captured="Sale Count" id="dvSaleSettledCountHeader">Sale Settlement Count</h2>
					<p id="dvSaleSettledCount" class="media-heading"></p>
				</div>
			</div>
			<!-- /.revenueBox -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.col-md-4 -->
	<div class="col-md-4">
		<div class="">
			<div class="revenueBox lpay_section white-bg box-shadow-box mt-70 p20">
				<div class="heading_with_icon">
					<span class="heading_icon_box">R</span>
					<h2 class="heading_text" data-settled="Refund Settlement Count" data-captured="Refund Count" id="dvRefundSettledCountHeader">Refund Settlement Count</h2>
					<p id="dvRefundSettledCount" class="media-heading"></p>
				</div>
			</div>
			<!-- /.revenueBox -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.col-md-4 -->

	<div class="col-md-4">
		<div class="">
			<div class="revenueBox lpay_section white-bg box-shadow-box mt-70 p20">
				<div class="heading_with_icon">
					<span class="heading_icon_box">T</span>
					<h2 class="heading_text" id="dvTotalProfitHeader">Total Profit</h2>
					<p id="dvTotalProfit" class="media-heading"></p>
				</div>
			</div>
			<!-- /.revenueBox -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.col-md-4 -->
</div>
<section class="revenueReport lapy_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
				<h2 class="heading_text" id="saleContainerHeader" data-settled="Sale Settlement Transaction" data-captured="Sale Transaction">Sale Settlement Transaction</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvSaleSettledAmount"></span>
				<h3 id="dvSaleSettledAmountHeader" data-settled="Sale Settlement Amount" data-captured="Sale Amount">Sale Settlement Amount</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvMerchantSaleSettledAmount"></span>
				<h3 id="dvMerchantSaleSettledAmountHeader" data-settled="Merchant Sale Settlement Amount" data-captured="Merchant Sale Amount">Merchant Sale Settlement Amount</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvPgSaleSurcharge"></span>
				<h3 id="dvPgSaleSurchargeHeader">Total Commission (Payment Gateway)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvPgSaleGst"></span>
				<h3 id="dvPgSaleGstHeader">Total GST (Payment Gateway)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvAcquirerSaleSurcharge"></span>
				<h3 id="dvAcquirerSaleSurchargeHeader">Total Commission (Acquirer)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvAcquirerSaleGst"></span>
				<h3 id="dvAcquirerSaleGstHeader">Total GST (Acquirer)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
	</div>
	<!-- /.row -->
</section>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

<section class="revenueReport lapy_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
				<h2 class="heading_text" id="refundContainerHeader" data-settled="Refund Settlement Transaction" data-captured="Refund Transaction">Refund Settlement Transaction</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvRefundSettledAmount"></span>
				<h3 id="dvRefundSettledAmountHeader" data-settled="Refund Settlement Amount" data-captured="Refund Amount">Refund Settlement Amount</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvMerchantRefundSettledAmount"></span>
				<h3 id="dvMerchantRefundSettledAmountHeader" data-settled="Merchant Refund Settlement Amount" data-captured="Merchant Refund Amount">Merchant Refund Settlement Amount</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvPgRefundSurcharge"></span>
				<h3 id="dvPgRefundSurchargeHeader">Total Commission (Payment Gateway)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvPgRefundGst"></span>
				<h3 id="dvPgRefundGstHeader">Total GST (Payment Gateway)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvAcquirerRefundSurcharge"></span>
				<h3 id="dvAcquirerRefundSurchargeHeader">Total Commission (Acquirer)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-4">
			<div class="revenvueData-box">
				<span id="dvAcquirerRefundGst"></span>
				<h3 id="dvAcquirerRefundGstHeader">Total GST (Acquirer)</h3>
			</div>
			<!-- /.revenvueData-box -->
		</div>
		<!-- /.col-md-4 -->
	</div>
	<!-- /.row -->
</section>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	
		
	<script type="text/javascript">
		$(document).ready(function() {
			$('#submit').click(function() {
				var dateFrom = document.getElementById('dateFrom').value.split('-'),
					dateTo = document.getElementById('dateTo').value.split('-'),
					myDateFrom = new Date(dateFrom[2], dateFrom[1], dateFrom[0]), //Year, Month, Date
					myDateTo = new Date(dateTo[2], dateTo[1], dateTo[0]), //Year, Month, Date
					oneDay = 24 * 60 * 60 * 1000; // hours*minutes*seconds*milliseconds

				if (myDateTo >= myDateFrom) {
					var diffDays = Math.round(Math.abs((myDateFrom.getTime() - myDateTo.getTime()) / (oneDay)));
					if (diffDays > 31) {
						alert('No. of days can not be more than 31');
					} else {
						statisticsAction();
					}
				} else {
					alert("From date must be before the To date.");
				}
			});

			document.querySelector("#merchant").addEventListener("change", function(e) {
				getSubMerchant(e, "getSubMerchantList", {
					isSuperMerchant : true,
					subUser : false,
					retailMerchantFlag: false,
					glocal : false
				});
			});
		});
	</script>
</body>
</html>