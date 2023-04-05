<%@taglib prefix="s" uri="/struts-tags"%>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link href="../css/welcomePage.css" rel="stylesheet">
<script src="../js/jquery.min.js"></script>
<link rel="stylesheet" href="../css/styles.css">
<script>
	$.noConflict();
</script>

<script>

	$(document).ready(function(e){
		
		document.querySelector("#menuIcon").onclick = function(e){
			document.querySelector(".side-wrapper").classList.add("active-sidebar-mobile");
		}

		document.querySelector("#cancel-btn").onclick = function(e){
			document.querySelector(".side-wrapper").classList.remove("active-sidebar-mobile");
		}
		
		function resizeHeight() {
			if(screen.width >= 768) {
				var _height = window.innerHeight;
				document.querySelector(".right_col").style.minHeight = _height+"px";
			} else {
				document.querySelector(".right_col").style.minHeight = "auto";
			}
		}
	
		resizeHeight();
	
		window.addEventListener("resize", function(e){
			resizeHeight()
		})
	
		$(".head").click(function(e) {
			$(this).next().slideToggle();
			$(this).closest('li').toggleClass("parentDiv");
		});
	
		var _getActive = $(document).attr('title');
		// console.log(_getActive);
		var _setWord = "";
		for (var i = 0; i < _getActive.length; i++) {
			if (_getActive[i].indexOf(" ") != -1) {
				_setWord += _getActive[i].replace(" ", "-");
			} else {
				_setWord += _getActive[i];
			}
		}
	
		$("[data-page=" + _setWord + "]").closest("ul").prev(".head").addClass("selected");
		$("[data-page=" + _setWord + "]").closest("ul").prev(".head").closest("li").addClass("parentDiv");
		$("[data-page=" + _setWord + "]").closest("li").addClass("selected");
		$("[data-page=" + _setWord + "]").addClass("current");
	
		$(".collapse-nav").on("click", function(e) {
			$(".collapse-nav").toggleClass("collapse-icon");
			$(".side-wrapper").toggleClass("active-sidebar");
			$(".side-wrapper").toggleClass("active-collapse");
			$(".right_col").toggleClass("right_col_extend");
		});
	
		$(".side-wrapper").hover(function(e) {
			var _isSidebarActive = $(this).hasClass("active-sidebar");
			if (_isSidebarActive == true) {
				$(".side-wrapper").toggleClass("active-collapse");
				$(".right_col").toggleClass("right_col_extend");
			}
		})
	
		if(document.querySelector(".head.selected") != null){
			var _scroll = document.querySelector(".head.selected").getBoundingClientRect();
			var _sideWrapper = document.querySelector(".side-wrapper_navigation");
			var _viewPort = window.innerHeight;
			var _scrollTop = _scroll.top;
			if(_scroll != null && _scrollTop > _viewPort ){
				document.querySelector(".side-wrapper_navigation").scrollTop = _scroll.top / 2;
			}
		}
	
	})
	
</script>


<style>
span#arrow {
	margin-left: 0 !important;
	float: right;
	margin-top: 7px;
}
</style>


<div id="fade"></div>

<s:hidden value="%{#session['USER_PERMISSION']}"></s:hidden>

<aside class="side-wrapper">
	<span id="cancel-btn" class="close-sidebar"><i class="fa fa-times" aria-hidden="true"></i></span>
	<header class="side-wrapper_logo">
		<a class="logo_link" href="home"> <img
			src="../image/white-logo.png" alt="Payment Gateway" class="white-logo-png">
			<img src="../image/white-logo-abr.png" alt="Payment Gateway"
			class="white-logo-abr">
		</a>
	</header>
	<!-- /.side-wrapper_logo -->
	<nav class="side-wrapper_navigation">
		<ul id="navigation" class="nav side-menu">
			<li><s:a action='home' data-page="Dashboard" class="head1">
					<span class="nav_icon"><i class="fa fa-home"></i></span>
					<span class="menu-text">Dashboard</span>
				</s:a></li>
			<li><a class="head"> <span class="nav_icon"><i
						class="fa fa-line-chart" aria-hidden="true"></i></span><span
					class="menu-text">Analytics</span><span class="fa fa-angle-down"
					id="arrow"></span>
			</a>
				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Performance Report')}">
						<li><s:a action='analyticsPerfomanceReport'
								data-page="Performance-Report">
								<span class="nav_icon">PR</span>
								<span class="menu-text">Performance Report</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Revenue Report')}">
						<li><s:a action='analyticsRevenue' data-page="Revenue-Report">
								<span class="nav_icon">RR</span>
								<span class="menu-text">Revenue Report</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Reseller Revenue Report')}">
						<li><s:a data-page="Reseller-Revenue-Report"
								action='resellerRevenue'>
								<span class="nav_icon">RR</span>
								<span class="menu-text">Reseller Revenue</span>
							</s:a></li>
					</s:if>

				</ul></li>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-list-alt"></i></span><span class="menu-text">Merchant
						Setup</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('User Registration')}">

						<li><s:a data-page="Registration" action='merchantCrmSignup'>
								<span class="nav_icon">UR</span>
								<span class="menu-text">User Registration</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Add Bulk Users')}">

						<li><s:a data-page="Upload-Bulk-Users"
								action='merchantAddBulkUsers'>
								<span class="nav_icon">ABU</span>
								<span class="menu-text">Add Bulk Users</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Merchant Account')}">
						<li><s:a data-page="Merchant-Accounts" action='merchantList'>
								<span class="nav_icon">MA</span>
								<span class="menu-text">Merchant Account</span>
							</s:a></li>
						<li><s:a data-page="Merchant Setup" action='merchantSetup'
								class="sublinks" onclick='return false'>
								<span class="nav_icon">MS</span>Merchant Setup</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Merchant Underwriting')}">

						<li><s:a data-page="MPA-Merchants-List"
								action='merchantUnderWriter'>
								<span class="nav_icon">MU</span>
								<span class="menu-text">Merchant Underwriting</span>
							</s:a></li>
					</s:if>

					<li><s:a data-page="User-Audit-Trail"
						action='userAuditTrail'>
						<span class="nav_icon">UAT</span>
						<span class="menu-text">User Audit Trail</span>
					</s:a></li>

					<s:if
						test="%{#session['USER_PERMISSION'].contains('Merchant List For MPA')}">
						<li><s:a data-page="New-Merchants-List"
								action='merchantsForMPA'>
								<span class="nav_icon">MO</span>
								<span class="menu-text">Merchant List For MPA</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('User Status')}">
						<li><s:a data-page="Activation" action='activation'>
								<span class="nav_icon">US</span>
								<span class="menu-text">User Status</span>
								</s:a>
							</li>
					</s:if>
					<s:if test="%{#session['USER_PERMISSION'].contains('CMS')}">
						<li><s:a data-page="New-Merchants-List"
								action='customPageAction'>
								<span class="nav_icon">CP</span>
								<span class="menu-text">View Custom Page</span>
							</s:a></li>
					</s:if>
				</ul></li>
			<!-- Fill MPA FORM -->

			<!-- Merchant Setup Ends -->

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-list-alt"></i></span><span class="menu-text"><span
						class="menu-text">Merchant Config</span></span> <span
					class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Merchant Mapping')}">
						<li><s:a data-page="Merchant-Mapping" action='mopSetUpAction'>
								<span class="nav_icon">MM</span>
								<span class="menu-text">Merchant Mapping</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Payment Options')}">
						<li><s:a data-page="Payment-Options" action="paymentOptions">
								<span class="nav_icon">PO</span>
								<span class="menu-text">Payment Options</span>
							</s:a></li>
					</s:if>
					<s:if test="%{#session['USER_PERMISSION'].contains('SUF Details')}">
						<li><s:a data-page="SUF-Details" action="sufDetails">
								<span class="nav_icon">SD</span>
								<span class="menu-text">SUF Details</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Discount Details')}">
						<li><s:a data-page="Discount-Details"
								action="discountDetails">
								<span class="nav_icon">DD</span>
								<span class="menu-text">Discount Details</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Dispatch Details')}">
						<li><s:a data-page="Dispatch-Details" action='dispatchSlip'>
								<span class="nav_icon">DD</span>
								<span class="menu-text">Dispatch Details</span>
							</s:a></li>
					</s:if>
					<!--<li><s:a action='resellerMappingAction'>Reseller Mapping</span></s:a></li>-->
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Charging Platform')}">
						<li><s:a data-page="Charging-Platform"
								action='chargingPlatform'>
								<span class="nav_icon">CP</span>
								<span class="menu-text">Charging Platform</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Bulk Update Charges')}">
						<li><s:a data-page="Bulk-Update-Charges"
								action='displayBulkChargesUpdate'>
								<span class="nav_icon">BUC</span>
								<span class="menu-text">Bulk Update Charges</span>
							</s:a></li>
					</s:if>
					<s:if test="%{#session['USER_PERMISSION'].contains('Mailer')}">
						<li><s:a data-page="Mailer" action="mailer">
								<span class="nav_icon">M</span>
								<span class="menu-text">Mailer</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Configure Smart Router')}">
						<li><s:a data-page="Router-Configuration-Platform"
								action='routerConfigurationAction'>
								<span class="nav_icon">SR</span>
								<span class="menu-text">Smart Router</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Merchant Underwriting')}">
						<li><s:a data-page="Merchant-Default-Charges"
								action='displayMerchantDefaultRate'>
								<span class="nav_icon">MDC</span>
								<span class="menu-text">Mer Default Charges</span>
							</s:a></li>
					</s:if>
				</ul></li>

			<!-- Merchant Config Ends -->


			<li><a style="cursor: pointer" class="head"><span
					class="nav_icon"><i class="fa fa-list-alt"></i></span><span
					class="menu-text">Reseller</span><span class="fa fa-angle-down"
					id="arrow"></span> </a> <!--<ul class="nav child_menu" style="display: none">-->

				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Reseller Account')}">
						<li><s:a data-page="Reseller-Accounts" action='resellerList'>
								<span class="nav_icon">RA</span>
								<span class="menu-text">Reseller Account</span>
							</s:a></li>
						<li><s:a action='resellerSetup' class="sublinks"
								onclick='return false' style="text-indent:68px !important;">Reseller Setup</s:a>
						</li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Reseller Merchant List')}">
						<li><s:a data-page="Reseller-Merchant-List"
								action='resellerMerchantList'>
								<span class="nav_icon">RML</span>
								<span class="menu-text">Reseller Merchant List</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Reseller Revenue Report')}">
						<li><s:a data-page="Reseller-Revenue-Report"
								action='resellerRevenue'>
								<span class="nav_icon">RR</span>
								<span class="menu-text">Reseller Revenue Report</span>
							</s:a></li>
					</s:if>
				</ul></li>

			<!-- Reseller Ends Here -->



			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-list"></i></span><span class="menu-text">View
						Configuration</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('View Smart Router Configuration')}">
						<li><s:a data-page="View-Surcharge-Report"
								action='viewSurchargeReport'>
								<span class="nav_icon">SR</span>
								<span class="menu-text">Smart Router</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('View Charging Details Configuration')}">
						<li><s:a data-page="View-Charging-Details"
								action='viewChargingDetails'>
								<span class="nav_icon">CD</span>
								<span class="menu-text">Charging Details</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('View Production Details Configurations')}">
						<li><s:a data-page="View-Production-Details"
								action='viewProductionDetails'>
								<span class="nav_icon">PD</span>
								<span class="menu-text">Production Details</span>
							</s:a></li>
					</s:if>
					
					
				</ul></li>

			<!-- View Configuration Ends -->

			<!--------------------------Search Payment------------------------------------>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">Quick
						Search</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Search Transaction')}">
						<li><s:a data-page="Search-Payment"
								action='transactionSearch'>
								<span class="nav_icon">ST</span>
								<span class="menu-text">Search Transaction</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('Download Transaction')}">
						<li><s:a data-page="Download-Payments-Report"
								action='downloadPaymentsReport'>
								<span class="nav_icon">DT</span>
								<span class="menu-text">Download Transactions</span>
							</s:a></li>
					</s:if>
					<li><s:a data-page="download-search" action='downloadSearch'>
							<span class="nav_icon">DTT</span>
							<span class="menu-text">Download Txn Trails</span>
						</s:a>
					</li>
				</ul></li>

			<!------------------------------------MSEDCL---------------------------------->
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">Subscription</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('eNACH Report')}">
						<li><s:a data-page="ENach-Report" action='eNachReports'>
								<span class="nav_icon">ER</span>
								<span class="menu-text">eNACH Report</span>
							</s:a></li>
					</s:if>
					<s:if
						test="%{#session['USER_PERMISSION'].contains('UPI AutoPay Report')}">
						<li><s:a data-page="UPI-AutoPay-Report"
								action='upiAutoPayReports'>
								<span class="nav_icon">UAR</span>
								<span class="menu-text">UPI AutoPay Report</span>
							</s:a></li>
					</s:if>
				</ul></li>


			<!--------------------------Search Payment------------------------------------>
				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-bar-chart-o"></i></span><span class="menu-text">Reporting</span>
						<span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Sale Capture')}">
							<li><s:a data-page="Sale-Captured"
									action='saleTransactionSearch'>
									<span class="nav_icon">SC</span>
									<span class="menu-text">Sale Captured</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Refund Capture')}">
							<li><s:a data-page="Refund-Captured"
									action='refundTransactionSearch'>
									<span class="nav_icon">RC</span>
									<span class="menu-text">Refund Captured</span>
								</s:a></li>
						</s:if>
						<s:if test="%{#session['USER_PERMISSION'].contains('Settled')}">
							<li><s:a data-page="Settled"
									action='settledTransactionSearch'>
									<span class="nav_icon">S</span>
									<span class="menu-text">Settled</span>
								</s:a></li>
						</s:if>
						<li><s:a data-page="Unsettled-Captured-Transaction-Report"
							action='unsettledTransactionSearch'>
							<span class="nav_icon">UC</span>
							<span class="menu-text">Unsettled Captured Transactions</span>
						</s:a></li>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Payment Advice')}">
							<li><s:a data-page="Download-Payment-Advise-Report"
									action="paymentAdviseReport">
									<span class="nav_icon">PA</span>
									<span class="menu-text">Payment Advice</span>
								</s:a></li>
						</s:if>
						<li>
							<s:a data-page="Download-MPR" action='downloadMpr'>
								<span class="nav_icon">DM</span>
								<span class="menu-text">Download MPR</span>
							</s:a>
						</li>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Booking Record')}">
							<li><s:a data-page="Booking-Record"
									action='bookingReportSearch'>
									<span class="nav_icon">BR</span>
									<span class="menu-text">Booking Record</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Booking Report')}">
							<li><s:a data-page="Booking-Report"
									action='bookingRecordSearch'>
									<span class="nav_icon">BR</span>
									<span class="menu-text">Booking Report</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('eCollection Report')}">
							<li><s:a data-page="eCollection-Report" action='eCollection'>
									<span class="nav_icon">C</span>
									<span class="menu-text">eCollection</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Summary Report')}">
							<li><s:a data-page="Summary-Report" action='summaryReports'>
									<span class="nav_icon">SR</span>
									<span class="menu-text">Summary Report</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Download Summary')}">
							<li><s:a data-page="Download-Summary-Report"
									action='downloadSummaryReport'>
									<span class="nav_icon">DS</span>
									<span class="menu-text">Download Summary</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Settled Bookings')}">
							<li><s:a data-page="Hotel-Bookings-Settled"
									action='bookingsSettled'>
									<span class="nav_icon">SB</span>
									<span class="menu-text">Settled Bookings</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Refund Report')}">
							<li><s:a data-page="Refund-Report" action='refundReports'>
									<span class="nav_icon">RR</span>
									<span class="menu-text">Refund Report</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Acquirer MPR')}">
							<li><s:a data-page="Acquirer-MPR-Report"
									action='mprUploadDownloadReport'>
									<span class="nav_icon">AM</span>
									<span class="menu-text">Acquirer MPR</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Refund Summary')}">
							<li><s:a data-page="Refund-Summary-Report"
									action='refundSummaryReport'>
									<span class="nav_icon">RS</span>
									<span class="menu-text">Refund Summary</span>
								</s:a></li>
						</s:if>
						<s:if test="%{#session['USER_PERMISSION'].contains('MIS Report')}">
							<li><s:a data-page="MIS-Report" action='misReports'>
									<span class="nav_icon">MR</span>
									<span class="menu-text">MIS Report</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Bank Exception')}">
							<li><s:a data-page="Bank-Exception"
									action='bankExceptionReport'>
									<span class="nav_icon">BE</span>
									<span class="menu-text">Bank Exception</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Merchant Exception')}">
							<li><s:a data-page="Merchant-Exception"
									action='merchantExceptionReport'>
									<span class="nav_icon">ME</span>
									<span class="menu-text">Merchant Exception</span>
								</s:a></li>
						</s:if>
						<s:if test="%{#session['USER_PERMISSION'].contains('GSTR Sale')}">
							<li><s:a data-page="GST-Report" action='gstReports'>
									<span class="nav_icon">GS</span>
									<span class="menu-text">GSTR Sale</span>
								</s:a></li>
						</s:if>
						
						<s:if test="%{#session['USER_PERMISSION'].contains('Static UPI QR Report')}">
						<li><s:a data-page="Customer-QR-Report" action='customQrReport'>
							<span class="nav_icon">SQR</span>
							<span class="menu-text">Static UPI QR Report</span>
						</s:a></li>
						</s:if>
						<li><s:a data-page="Historical-Generated-Report" action='historicalGenerateAction'>
							<span class="nav_icon">HR</span>
							<span class="menu-text">Historical Report</span>
						</s:a></li>
						<li><s:a data-page="Settled-Consolidated-Report" action='settledConsolidatedReport'>
							<span class="nav_icon">NS</span>
							<span class="menu-text">Net Settled</span>
						</s:a></li>
						<li><s:a data-page="Refund-Limit-Report" action='refundLimitTrails'>
							<span class="nav_icon">RL</span>
							<span class="menu-text">Refund Limit Report</span>
						</s:a></li>
					</ul></li>

				<!-- Reports -->

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-bar-chart-o"></i></span><span class="menu-text">Vendor
							Payout</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Product Report')}">
							<li><s:a action="khadiProductReport"
									data-page="Khadi-Product-Wise-Report">
									<span class="nav_icon">PR</span>
									<span class="menu-text"> Product Report</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Payout Report')}">
							<li><s:a action="vendorPayoutReportAction"
									data-page="Vendor-Payout">
									<span class="nav_icon">VP</span>
									<span class="menu-text"> Payout Report</span>
								</s:a></li>
						</s:if>
					</ul></li>

				<!-- Vendor Payout -->

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-file-o"></i></span><span class="menu-text">Quick Pay</span>
						<span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Invoice Payments')}">
							<li><s:a data-page="Invoice-Payment" action="invoice">
									<span class="nav_icon">IP</span>
									<span class="menu-text">Invoice Payment </span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Bulk Invoice')}">
							<li><s:a data-page="Bulk-Invoice" action="bulkInvoice">
									<span class="nav_icon">BI</span>
									<span class="menu-text">Bulk Invoice </span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Quick Payment Search')}">
							<li><s:a data-page="Invoice-Search" action="invoiceSearch">
									<span class="nav_icon">QPS</span>
									<span class="menu-text">Quick Payment Search </span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Bulk Invoice Search')}">
							<li><s:a data-page="Bulk-Invoice-Search"
									action="bulkInvoiceSearch">
									<span class="nav_icon">BIS</span>
									<span class="menu-text">Bulk Invoice Search</span>
								</s:a></li>
						</s:if>
					</ul></li>

				<!-- Quick Pay -->

				<s:if
					test="%{#session['USER_PERMISSION'].contains('School Fee Manager')}">
					<li><a style="cursor: pointer" class="head"> <span
							class="nav_icon"><i class="fa fa-university"></i></span><span
							class="menu-text">School Fee Manager</span> <span
							class="fa fa-angle-down" id="arrow"></span>
					</a>
						<ul>
							<li><s:a data-page="Student-List" action="schoolInventory">
									<span class="nav_icon">SL</span>
									<span class="menu-text">Student List</span>
								</s:a></li>
							<li><s:a data-page="Student-Fee-Payment-Report"
									action="schoolReport">
									<span class="nav_icon">PR</span>
									<span class="menu-text">Payment Report</span>
								</s:a></li>
							<li><s:a data-page="Fee-Detail-Report"
									action="feeDetailsSearch">
									<span class="nav_icon">FDR</span>
									<span class="menu-text">Fee Detail Report</span>
								</s:a></li>
						</ul></li>
				</s:if>

				<!-- Student Fee  -->

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-th"></i></span><span class="menu-text">Batch
							Operations</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<s:if test="%{#session['USER_PERMISSION'].contains('BIN Ranges')}">
							<li><s:a data-page="BinRange-Summary"
									action="manageBinRange">
									<span class="nav_icon">BR</span>
									<span class="menu-text">Bin Ranges</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Monthly Invoice')}">
							<li><s:a data-page="Monthly-Invoice" action="monthlyInvoice">
							<span class="nav_icon">MI</span>
							<span class="menu-text">Monthly Invoice</span>
								</s:a></li>
						</s:if>
						<li>
							<s:a data-page="Refund-Utility" action='refundTxnUtil'>
								<span class="nav_icon">RU</span>
								<span class="menu-text">Refund Utility</span>
							</s:a>
						</li>
						<li>
							<s:a data-page="Initiated-Refund-Data" action="initiatedRefundAction">
								<span class="nav_icon">IRD</span>
								<span class="menu-text">Initiated Refund Data</span>
							</s:a>
						</li>
					</ul>
				</li>





				<s:if
					test="%{#session['USER_PERMISSION'].contains('Create BulkEmail')}">
					<li><a style="cursor: pointer" class="head"> <span
							class="nav_icon"><i class="fa fa-users"></i></span><span
							class="menu-text">Bulk Email</span><span class="fa fa-angle-down"
							id="arrow"></span></a>
						<ul>
							<li><s:a action="bulkEmail" data-page="Bulk-Email">
									<span class="nav_icon">SBE</span>
									<span class="menu-text">Send Bulk Email</span>
								</s:a></li>
						</ul></li>
				</s:if>

				<s:if
					test="%{#session['USER_PERMISSION'].contains('Nodal Payouts')}">
					<!--------------------Nodal Payout Tab Start------->
					<li><a style="cursor: pointer" class="head"> <span
							class="nav_icon"><i class="fa fa-line-chart"
								aria-hidden="true"></i></span><span class="menu-text">Nodal
								Payout</span><span class="fa fa-angle-down" id="arrow"></span>
					</a>
						<ul>
							<li><s:a action='addBeneficiary' data-page="Add-Beneficiary">
									<span class="nav_icon">AB</span>
									<span class="menu-text">Add Beneficiary</span>
								</s:a></li>
							<li><s:a action='beneficiaryList'
									data-page="Beneficiary-List">
									<span class="nav_icon">BL</span>
									<span class="menu-text">Beneficiary List</span>
								</s:a></li>
							<li><s:a action='nodalTransactions'
									data-page="Automated-Fund-Transfer">
									<span class="nav_icon">FT</span>
									<span class="menu-text">Fund Transfer</span>
								</s:a></li>
							<li><s:a action='nodalTransactionsHistory'
									data-page="Search-Nodal-Transactions">
									<span class="nav_icon">TH</span>
									<span class="menu-text">Transaction history</span>
								</s:a></li>
						</ul></li>
					<!------	Nodal Payout Tab Close-------------------------------------------->
				</s:if>

				<s:if
					test="%{#session['USER_PERMISSION'].contains('Settlement Data Refresh')}">
					<li><s:a action='refreshSettlementData' class="head1">
							<i class="fa fa-refresh"></i>Refresh Data</s:a></li>
				</s:if>

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-users"></i></span><span class="menu-text">Manage
							Users</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Add Sub Admin')}">
							<li><s:a data-page="Add-SubAdmin" action="addSubAdmin">
									<span class="nav_icon">AS</span>
									<span class="menu-text">Add Sub-Admin</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Sub Admin List')}">
							<li><s:a data-page="SubAdmin-List" action="searchSubAdmin">
									<span class="nav_icon">SL</span>
									<span class="menu-text">Sub-Admin List</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Sub Merchant List')}">
							<li><s:a data-page="Sub-Merchant-List"
									action="subMerchantSearch">
									<span class="nav_icon">SL</span>
									<span class="menu-text">Sub-Merchant List</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Add Sub User')}">
							<li><s:a data-page="Merchant-Subusers"
									action='merchantSubUsers'>
									<span class="nav_icon">MS</span>
									<span class="menu-texy">Merchant Sub-Users</span>
								</s:a></li>
						</s:if>
						<s:if test="%{#session['USER_PERMISSION'].contains('Add Agent')}">
							<li><s:a data-page="Add-Agent" action="addAgentAction">
									<span class="nav_icon">AA</span>
									<span class="menu-text">Add Agent </span>
								</s:a></li>
						</s:if>
						<s:if test="%{#session['USER_PERMISSION'].contains('Agent List')}">
							<li><s:a data-page="Search-Agent" action="searchAgent">
									<span class="nav_icon">AL</span>
									<span class="menu-text">Agent List</span>
								</s:a></li>
						</s:if>
						<s:if test="%{#session['USER_PERMISSION'].contains('Assign MPA')}">
							<li><s:a data-page="Assign-MPA" action="assignMpa">
									<span class="nav_icon">AM</span>
									<span class="menu-text">Assign MPA</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Khadi Vendor List')}">
							<li><s:a action="khadiSubMerchantSearch"
									data-page="Sub-Merchant-List">
									<span class="nav_icon">KL</span>
									<span class="menu-text">Khadi Vendor List</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Add Khadi Vendor')}">
							<li><s:a action="addKhadiUser" data-page="Add-Khadi-User">
									<span class="nav_icon">AK</span>
									<span class="menu-text">Add Khadi Vendor</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Sub User List')}">
							<li><s:a action="userList" data-page="Admin-User-List">
									<span class="nav_icon">SUL</span>
									<span class="menu-text">Sub-User List</span>
								</s:a></li>
						</s:if>

					</ul></li>

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-users"></i></span><span class="menu-text">Manage
							Acquirers</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Add Acquirer')}">
							<li><s:a data-page="Add-Acquirer" action="addAcquirer">
									<span class="nav_icon">AA</span>
									<span class="menu-text">Add Acquirer</span>
								</s:a></li>
						</s:if>
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Acquirer List')}">
							<li><s:a data-page="Search-Acquirer" action="searchAcquirer">
									<span class="nav_icon">AL</span>
									<span class="menu-text">Acquirers List</span>
								</s:a></li>
						</s:if>
						
						<s:if
							test="%{#session['USER_PERMISSION'].contains('Acquirer Operations')}">
							<li><s:a data-page="Acquirer-Operations-Report"
								action='acquirerOperationAction'>
								<span class="nav_icon">OP</span>
								<span class="menu-text">Operations</span>
							</s:a></li>
						</s:if>
						
					</ul></li>

				<s:if test="%{#session['USER_PERMISSION'].contains('Agent Search')}">
					<li><a style="cursor: pointer" class="head"> <span
							class="nav_icon"><i class="fa fa-user"></i></span><span
							class="menu-text">Customer Operations</span><span
							class="fa fa-angle-down" id="arrow"></span></a>

						<ul>
							<li><s:a action='agentSearch' data-page="Agent-Search">
									<span class="nav_icon">AS</span>
									<span class="menu-text">Agent Search</span>
								</s:a></li>
								<li>
									<s:a action='complaintRaise' data-page="Complaint-Raise" class="head">
										<span class="nav_icon">CR</span>
										<span class="menu-text">Complaint Raise</span>
									</s:a>
								</li>
						</ul>
					</li>
				</s:if>



				<s:if
					test="%{#session['USER_PERMISSION'].contains('View ChargeBack')}">
					<li><a style="cursor: pointer" class="head"> <span
							class="nav_icon"><i class="fa fa-circle-o-notch"></i></span><span
							class="menu-text">Chargeback Case</span><span
							class="fa fa-angle-down" id="arrow"></span></a>
						<ul>
							<li><s:a action="viewChargeback" data-page="View-Chargeback">
									<span class="nav_icon">VC</span>
									<span class="menu-text">View ChargeBack</span>
								</s:a></li>
							<li><s:a data-page="View-Chargeback" action="chargebackUtil">
								<span class="nav_icon">CU</span>Chargeback Utility</s:a></li>
						</ul></li>
				</s:if>
				<s:if
					test="%{#session['USER_PERMISSION'].contains('Scheduler Jobs')}">
					<li><a style="cursor: pointer" class="head"> <span
							class="nav_icon"><i class="fa fa-circle-o-notch"></i></span><span
							class="menu-text">Scheduler</span><span class="fa fa-angle-down"
							id="arrow"></span>
					</a>
						<ul>
							<li><s:a action="scheduler" data-page="Scheduler">
									<span class="nav_icon">MS</span>
									<span class="menu-text">Manage Scheduler</span>
								</s:a></li>
						</ul></li>
				</s:if>


				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-ban"></i></span><span class="menu-text">Fraud
							Prevention</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>

						<s:if
							test="%{#session['USER_PERMISSION'].contains('FPS Configuration')}">
							<li><s:a data-page="Fraud-Prevention-System"
									action="adminRestrictions">
									<span class="nav_icon">FC</span>
									<span class="menu-text">FPS Configuration</span>
								</s:a></li>
						</s:if>

						<s:if
							test="%{#session['USER_PERMISSION'].contains('Fraud Analytics')}">
							<li><s:a data-page="Fraud-Analytics" action="fraudAnalytics">
									<span class="nav_icon">FA</span>
									<span class="menu-text">Fraud Analytics</span>
								</s:a></li>
						</s:if>
					</ul></li>

				<s:if test="%{#session['USER_PERMISSION'].contains('Manage Agent')}">
					<li><a class="head"><span class="nav_icon"><i
								class="fa fa-users"></i></span><span class="menu-text">Manage
								Agent</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
						<ul>
							<li><s:a action="addAgentAction" data-page="Add-Agent">
									<span class="nav_icon">AA</span>
									<span class="menu-text">Add Agent </span>
								</s:a></li>
							<li><s:a action="searchAgent" data-page="Search-Agent">
									<span class="nav_icon">AL</span>
									<span class="menu-text">Agent List</span>
								</s:a></li>
						</ul></li>
				</s:if>


				<li><a style="cursor: pointer" class="head"> <span
						class="nav_icon"><i class="fa fa-user"></i></span><span
						class="menu-text">My Account</span><span class="fa fa-angle-down"
						id="arrow"></span></a>
					<ul>
						<li><s:a action="adminProfile" data-page="Admin-Profile">
								<span class="nav_icon">MP</span>
								<span class="menu-text">My Profile </span>
							</s:a></li>
						<li><s:a action="loginHistoryRedirect"
								data-page="Login-History">
								<span class="nav_icon">LH</span>
								<span class="menu-text">Login History</span>
							</s:a></li>
						<li><s:a action='passwordChange' data-page="Change-Password">
								<span class="nav_icon">CP</span>
								<span class="menu-text">Change PIN</span>
							</s:a></li>
					</ul></li>
				<!-- ticketing -->
		</ul>
	</nav>
	<!-- /.side_wrapper-navigation -->
</aside>
<!-- /.side-wrapper -->


<script src="../js/bootstrap.min.js"></script>
<script>
	;
	(function($, window, document, undefined) {

		var pluginName = "metisMenu", defaults = {
			toggle : true
		};

		function Plugin(element, options) {
			this.element = element;
			this.settings = $.extend({}, defaults, options);
			this._defaults = defaults;
			this._name = pluginName;
			this.init();
		}

		Plugin.prototype = {
			init : function() {

				var $this = $(this.element), $toggle = this.settings.toggle;

				$this.find('li.active').has('ul').children('ul').addClass(
						'collapse in');
				$this.find('li').not('.active').has('ul').children('ul')
						.addClass('collapse');

				$this.find('li').has('ul').children('a').on(
						'click',
						function(e) {
							e.preventDefault();

							$(this).parent('li').toggleClass('active')
									.children('ul').collapse('toggle');

							if ($toggle) {
								$(this).parent('li').siblings().removeClass(
										'active').children('ul.in').collapse(
										'hide');
							}
						});
			}
		};

		$.fn[pluginName] = function(options) {
			return this.each(function() {
				if (!$.data(this, "plugin_" + pluginName)) {
					$.data(this, "plugin_" + pluginName, new Plugin(this,
							options));
				}
			});
		};

	})(jQuery, window, document);
</script>
<script>

	var _navigation = document.querySelectorAll("#navigation ul");
	for(var i = 0;i < _navigation.length; i++ ){
		var _length = _navigation[i].children.length;
		if(_length == 0){
			_navigation[i].closest("li").classList.add("d-none");
		}
	}

	(function($) {
		"use strict";
		var mainApp = {

			initFunction : function() {
				/*MENU 
				------------------------------------*/
				$('#main-menu').metisMenu();

				$(window).bind("load resize", function() {
					if ($(this).width() < 768) {
						$('div.sidebar-collapse').addClass('collapse')
					} else {
						$('div.sidebar-collapse').removeClass('collapse')
					}

				})
				// Initializing ///

				$(document).ready(function() {
					mainApp.initFunction();
				});

			},

			initialization : function() {
				mainApp.initFunction();

			}

		}
		// Initializing ///

		$(document).ready(function() {
			mainApp.initFunction();
		});

	}(jQuery));
</script>
