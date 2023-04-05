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
	$(document)
			.ready(
					function(e) {

						document.querySelector("#menuIcon").onclick = function(
								e) {
							document.querySelector(".side-wrapper").classList
									.add("active-sidebar-mobile");
						}

						document.querySelector("#cancel-btn").onclick = function(
								e) {
							document.querySelector(".side-wrapper").classList
									.remove("active-sidebar-mobile");
						}

						function resizeHeight() {
							if (screen.width >= 768) {
								var _height = window.innerHeight;
								document.querySelector(".right_col").style.minHeight = _height
										+ "px";
							} else {
								document.querySelector(".right_col").style.minHeight = "auto";
							}
						}

						resizeHeight();

						window.addEventListener("resize", function(e) {
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

						$("[data-page=" + _setWord + "]").closest("ul").prev(
								".head").addClass("selected");
						$("[data-page=" + _setWord + "]").closest("ul").prev(
								".head").closest("li").addClass("parentDiv");
						$("[data-page=" + _setWord + "]").closest("li")
								.addClass("selected");
						$("[data-page=" + _setWord + "]").addClass("current");

						$(".collapse-nav").on("click", function(e) {
							$(".collapse-nav").toggleClass("collapse-icon");
							$(".side-wrapper").toggleClass("active-sidebar");
							$(".side-wrapper").toggleClass("active-collapse");
							$(".right_col").toggleClass("right_col_extend");
						});

						$(".side-wrapper").hover(
								function(e) {
									var _isSidebarActive = $(this).hasClass(
											"active-sidebar");
									if (_isSidebarActive == true) {
										$(".side-wrapper").toggleClass(
												"active-collapse");
										$(".right_col").toggleClass(
												"right_col_extend");
									}
								})

						if (document.querySelector(".head.selected") != null) {
							var _scroll = document.querySelector(
									".head.selected").getBoundingClientRect();
							var _sideWrapper = document
									.querySelector(".side-wrapper_navigation");
							var _viewPort = window.innerHeight;
							var _scrollTop = _scroll.top;
							if (_scroll != null && _scrollTop > _viewPort) {
								document
										.querySelector(".side-wrapper_navigation").scrollTop = _scroll.top / 2;
							}
						}

					})
</script>

<style>
span#arrow {
	margin-left: 0 !important;
	float: right;
	margin-top: 11px;
}
</style>

<div id="fade"></div>
<!-- <button id="checkButton" style="position: absolute;z-index: 9999">button</button> -->

<aside class="side-wrapper">
	<span id="cancel-btn" class="close-sidebar"><i
		class="fa fa-times" aria-hidden="true"></i></span>
	<header class="side-wrapper_logo">
		<a class="logo_link" href="home"> <img
			src="../image/white-logo.png" alt="Pg" class="white-logo-png">
			<img src="../image/white-logo-abr.png" alt="Pg"
			class="white-logo-abr">
		</a>
	</header>
	<!-- /.side-wrapper_logo -->
	<nav class="side-wrapper_navigation">

		<ul id="navigation" class="nav">
			<li><s:a action='home' data-page="Dashboard" class="head1">
					<span class="nav_icon"><i class="fa fa-home"></i></span>
					<span class="menu-text">Dashboard</span>
				</s:a></li>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-line-chart" aria-hidden="true"></i></span><span
					class="menu-text">Analytics</span> <span class="fa fa-angle-down"
					id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Performance-Report"
							action='analyticsPerfomanceReport'>
							<span class="nav_icon">PR</span>
							<span class="menu-text">Performance Report</span>
						</s:a></li>
					<li><s:a data-page="Revenue-Report" action='analyticsRevenue'>
							<span class="nav_icon">RR</span>
							<span class="menu-text">Revenue Report</span>
						</s:a></li>

				</ul></li>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-list-alt"></i></span><span class="menu-text">Merchant
						Setup</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Registration" action='merchantCrmSignup'>
							<span class="nav_icon">UR</span>
							<span class="menu-text">User Registration</span>
						</s:a></li>
					<li><s:a data-page="Merchant-Accounts" action='merchantList'>
							<span class="nav_icon">MA</span>
							<span class="menu-text">Merchant Account</span>
						</s:a></li>
					<li><s:a data-page="Merchant Setup" action='merchantSetup'
							class="sublinks" onclick='return false'>
							<span class="nav_icon">MS</span>Merchant Setup</s:a></li>
					<!-- <li><s:a data-page="Merchant-Underwriting"
							action='merchantUnderWriter'>
							<span class="nav_icon">MU</span>
							<span class="menu-text">Merchant Underwriting</span>
						</s:a></li>
						<li><s:a data-page="User-Audit-Trail"
							action='userAuditTrail'>
							<span class="nav_icon">UAT</span>
							<span class="menu-text">User Audit Trail</span>
						</s:a></li> -->


					<li><s:a data-page="New-Merchants-List"
							action='merchantsForMPA'>
							<span class="nav_icon">MO</span>
							<span class="menu-text">Merchant List For MPA</span>
						</s:a></li>

					<li><s:a data-page="Activation" action='activation'>
							<span class="nav_icon">US</span>
							<span class="menu-text">User Status</span>
						</s:a></li>

					<li><s:a data-page="User-settings"
							action='userSettingMerchant'>
							<span class="nav_icon">US</span>
							<span class="menu-text">User Settings</span>
						</s:a></li>

				</ul></li>

			<!-- Merchant Setup Ends -->

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-list-alt"></i></span><span class="menu-text"><span
						class="menu-text">Merchant Config</span></span> <span
					class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Merchant-Mapping" action='mopSetUpAction'>
							<span class="nav_icon">MM</span>
							<span class="menu-text">Merchant Mapping</span>
						</s:a></li>
					<li><s:a data-page="Payment-Options" action="paymentOptions">
							<span class="nav_icon">PO</span>
							<span class="menu-text">Payment Options</span>
						</s:a></li>
					<li><s:a data-page="SUF-Details" action="sufDetails">
							<span class="nav_icon">SD</span>
							<span class="menu-text">SUF Details</span>
						</s:a></li>
					<!--<li><s:a action='resellerMappingAction'>Reseller Mapping</span></s:a></li>-->
					<li><s:a data-page="Charging-Platform"
							action='chargingPlatform'>
							<span class="nav_icon">CP</span>
							<span class="menu-text">Charging Platform</span>
						</s:a></li>
					<li><s:a data-page="Bulk-Update-Charges"
							action='displayBulkChargesUpdate'>
							<span class="nav_icon">BUC</span>
							<span class="menu-text">Bulk Update Charges</span>
						</s:a></li>
					<li><s:a data-page="Router-Configuration-Platform"
							action='routerConfigurationAction'>
							<span class="nav_icon">SR</span>
							<span class="menu-text">Smart Router</span>
						</s:a></li>
					<li><s:a data-page="Merchant-Default-Charges"
							action='displayMerchantDefaultRate'>
							<span class="nav_icon">MDC</span>
							<span class="menu-text">Mer Default Charges</span>
						</s:a></li>
					<%-- <li><s:a action='surchargePlatform'><span class="nav_icon">SS</span><span class="menu-text">Surcharge Setting</span></s:a></li> --%>
					<li><s:a data-page="Pending-Request" action='pendingRequest'>
							<span class="nav_icon">PR</span>
							<span class="menu-text">Pending Request</span>
						</s:a></li>
				</ul></li>

			<!-- Merchant Config Ends -->

			<li><a style="cursor: pointer" class="head"><span
					class="nav_icon"><i class="fa fa-list-alt"></i></span><span
					class="menu-text">Reseller</span><span class="fa fa-angle-down"
					id="arrow"></span> </a> <!--<ul class="nav child_menu" style="display: none">-->

				<ul>
					<li><s:a data-page="Reseller-Accounts" action='resellerList'>
							<span class="nav_icon">RA</span>
							<span class="menu-text">Reseller Account</span>
						</s:a></li>
					<li><s:a action='resellerSetup' class="sublinks"
							onclick='return false' style="text-indent:68px !important;">Reseller Setup</s:a>
					</li>
					<%-- <li><s:a data-page="Reseller-Charges-Update"
							action='resellerChargesUpdate'>
							<span class="nav_icon">RCU</span>
							<span class="menu-text">Reseller Charges Update</span>
						</s:a></li> --%>
					<li><s:a data-page="Reseller-Merchant-List"
							action='resellerMerchantList'>
							<span class="nav_icon">RML</span>
							<span class="menu-text">Reseller Merchant List</span>
						</s:a></li>
					<li><s:a data-page="Reseller-Revenue-Report"
							action='resellerRevenue'>
							<span class="nav_icon">RR</span>
							<span class="menu-text">Reseller Revenue</span>
						</s:a></li>
				</ul></li>

			<!-- Reseller Ends Here -->

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-list"></i></span><span class="menu-text">View
						Configuration</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="View-Surcharge-Report"
							action='viewSurchargeReport'>
							<span class="nav_icon">SR</span>
							<span class="menu-text">Smart Router</span>
						</s:a></li>
					<li><s:a data-page="View-Charging-Details"
							action='viewChargingDetails'>
							<span class="nav_icon">CD</span>
							<span class="menu-text">Charging Details</span>
						</s:a></li>
					<li><s:a data-page="View-Production-Details"
							action='viewProductionDetails'>
							<span class="nav_icon">PD</span>
							<span class="menu-text">Production Details</span>
						</s:a></li>
					<li><s:a data-page="View-Virtual-Details"
							action='viewVirtualDetails'>
							<span class="nav_icon">VD</span>
							<span class="menu-text">View Virtual Details</span>
						</s:a></li>
				</ul></li>
			<!------------------commented------------------------------------------>


			<!--------------------------Search Payment------------------------------------>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">Quick
						Search</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Search-Payment" action='transactionSearch'>
							<span class="nav_icon">ST</span>
							<span class="menu-text">Search Transaction</span>
						</s:a></li>
					<li><s:a data-page="Download-Payments-Report"
							action='downloadPaymentsReport'>
							<span class="nav_icon">DT</span>
							<span class="menu-text">Download Transactions</span>
						</s:a></li>
					<li><s:a data-page="Download-Search" action='downloadSearch'>
							<span class="nav_icon">DTT</span>
							<span class="menu-text">Download Txn Trails</span>
						</s:a></li>
				</ul></li>
			<!--------------------------Search Payment------------------------------------>

			<!--------------------------Transaction Report ------------------------------------>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">Reporting</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Sale-Captured"
							action='saleTransactionSearch'>
							<span class="nav_icon">SC</span>
							<span class="menu-text">Sale Captured</span>
						</s:a></li>

					<li><s:a data-page="Refund-Captured"
							action='refundTransactionSearch'>
							<span class="nav_icon">RC</span>
							<span class="menu-text">Refund Captured</span>
						</s:a></li>
					<li><s:a data-page="Settled-Transaction"
							action='settledTransactionSearch'>
							<span class="nav_icon">S</span>
							<span class="menu-text">Settled</span>
						</s:a></li>
					<li><s:a data-page="Unsettled-Captured-Transaction-Report"
							action='unsettledTransactionSearch'>
							<span class="nav_icon">UC</span>
							<span class="menu-text">Unsettled Captured Transactions</span>
						</s:a></li>
					<li><s:a data-page="Download-Payment-Advise-Report"
							action="paymentAdviseReport">
							<span class="nav_icon">PA</span>
							<span class="menu-text">Payment Advice</span>
						</s:a></li>
					<li><s:a data-page="Custom-Capture-Report"
							action='customCaptureReport'>
							<span class="nav_icon">CC</span>
							<span class="menu-text">Custom Capture</span>
						</s:a></li>
					<li><s:a data-page="Download-MPR" action='downloadMpr'>
							<span class="nav_icon">DM</span>
							<span class="menu-text">Download MPR</span>
						</s:a></li>

					<li><s:a data-page="Summary-Report" action='summaryReports'>
							<span class="nav_icon">SR</span>
							<span class="menu-text">Summary Report</span>
						</s:a></li>
					<li><s:a data-page="Refund-Report" action='refundReports'>
							<span class="nav_icon">RR</span>
							<span class="menu-text">Refund Report</span>
						</s:a></li>

					<li><s:a data-page="Acquirer-MPR-Report"
							action='mprUploadDownloadReport'>
							<span class="nav_icon">AM</span>
							<span class="menu-text">Acquirer MPR</span>
						</s:a></li>
					<li><s:a data-page="Refund-Summary-Report"
							action='refundSummaryReport'>
							<span class="nav_icon">RS</span>
							<span class="menu-text">Refund Summary</span>
						</s:a></li>

					<li><s:a data-page="MIS-Report" action='misReports'>
							<span class="nav_icon">MR</span>
							<span class="menu-text">MIS Report</span>
						</s:a></li>
					<li><s:a data-page="Bank-Exception"
							action='bankExceptionReport'>
							<span class="nav_icon">BE</span>
							<span class="menu-text">Bank Exception</span>
						</s:a></li>
					<li><s:a data-page="Merchant-Exception"
							action='merchantExceptionReport'>
							<span class="nav_icon">ME</span>
							<span class="menu-text">Merchant Exception</span>
						</s:a></li>
					<li><s:a data-page="GST-Report" action='gstReports'>
							<span class="nav_icon">GS</span>
							<span class="menu-text">GSTR Sale</span>
						</s:a></li>

					<li><s:a data-page="Net-Settled-Report"
							action='settledConsolidatedReport'>
							<span class="nav_icon">NS</span>
							<span class="menu-text">Net Settled</span>
						</s:a></li>
					<li><s:a data-page="Historical-Generated-Report"
							action='historicalGenerateAction'>
							<span class="nav_icon">HR</span>
							<span class="menu-text">Historical Report</span>
						</s:a></li>
					<li><s:a data-page="Refund-Limit-Report"
							action='refundLimitTrails'>
							<span class="nav_icon">RL</span>
							<span class="menu-text">Refund Limit Report</span>
						</s:a></li>
				</ul></li>

			<!-- Reports -->
			<!--------------------------Transaction Report ------------------------------------>

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-file-o"></i></span><span class="menu-text">Quick Pay</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Invoice-Payment" action="invoice">
							<span class="nav_icon">IP</span>
							<span class="menu-text">Invoice Payment </span>
						</s:a></li>
					<li><s:a data-page="Bulk-Invoice" action="bulkInvoice">
							<span class="nav_icon">BI</span>
							<span class="menu-text">Bulk Invoice </span>
						</s:a></li>
					<%-- <li><s:a action="invoiceEvent">Promotional Payment </span></s:a></li> --%>
					<li><s:a data-page="Invoice-Search" action="invoiceSearch">
							<span class="nav_icon">QPS</span>
							<span class="menu-text">Quick Payment Search </span>
						</s:a></li>
					<li><s:a data-page="Bulk-Invoice-Search"
							action="bulkInvoiceSearch">
							<span class="nav_icon">BIS</span>
							<span class="menu-text">Bulk Invoice Search</span>
						</s:a></li>
				</ul></li>


			<li><a class="head"> <span class="nav_icon"><i
						class="fa fa-th"></i></span> <span class="menu-text">Batch
						Operations</span> <span class="fa fa-angle-down" id="arrow"></span>
			</a>
				<ul>
					<li><s:a data-page="BinRange-Summary" action="manageBinRange">
							<span class="nav_icon">BR</span>
							<span class="menu-text">Bin Ranges</span>
						</s:a></li>
					<li><s:a data-page="EMI-BinRange-Summary"
							action="manageEmiBinRange">
							<span class="nav_icon">ER</span>
							<span class="menu-text">EMI Bin Ranges</span>
						</s:a></li>
					<li><s:a data-page="Hotel-Inventory" action="manageHotelInv">
							<span class="nav_icon">HI</span>
							<span class="menu-text">Hotel Inventory</span>
						</s:a></li>
					<li><s:a data-page="Invoice" action="monthlyInvoice">
							<span class="nav_icon">MI</span>
							<span class="menu-text">Monthly Invoice</span>
						</s:a></li>
					<li><s:a data-page="Refund-Utility" action='refundTxnUtil'>
							<span class="nav_icon">RU</span>
							<span class="menu-text">Refund Utility</span>
						</s:a></li>
				</ul></li>

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-users"></i></span><span class="menu-text">Bene
						Verification</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Account-Verification"
							action="impsBeneVerification">
							<span class="nav_icon">AV</span>
							<span class="menu-text">Account Verification</span>
						</s:a></li>
					<li><s:a data-page="VPA-Verification" action="vpaValidate">
							<span class="nav_icon">VV</span>
							<span class="menu-text">VPA Verification</span>
						</s:a></li>
				</ul></li>
			<!------------------------Fraud tab----------------------------------->

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">Subscription</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="eNACH" action='eNachReports'>
							<span class="nav_icon">E</span>
							<span class="menu-text">eNACH</span>
						</s:a></li>

					<li><s:a data-page="UPI-AutoPay-Report"
							action='upiAutoPayReports'>
							<span class="nav_icon">UA</span>
							<span class="menu-text">UPI AutoPay</span>
						</s:a></li>

				</ul></li>

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-ban"></i></span><span class="menu-text">Fraud
						Prevention</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Fraud-Prevention-System"
							action="adminRestrictions">
							<span class="nav_icon">FC</span>
							<span class="menu-text">FPS Configuration</span>
						</s:a></li>

					<li><s:a data-page="Fraud-Analytics" action="fraudAnalytics">
							<span class="nav_icon">FA</span>
							<span class="menu-text">Fraud Analytics</span>
						</s:a></li>
				</ul></li>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-users"></i></span><span class="menu-text">Manage
						Users</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Add-SubAdmin" action="addSubAdmin">
							<span class="nav_icon">AS</span>
							<span class="menu-text">Add Sub-Admin</span>
						</s:a></li>
					<li><s:a data-page="SubAdmin-List" action="searchSubAdmin">
							<span class="nav_icon">SL</span>
							<span class="menu-text">Sub-Admin List</span>
						</s:a></li>
					<li><s:a data-page="Sub-Merchant-List"
							action="subMerchantSearch">
							<span class="nav_icon">SL</span>
							<span class="menu-text">Sub-Merchant List</span>
						</s:a></li>

					<li><s:a data-page="Add-Agent" action="addAgentAction">
							<span class="nav_icon">AA</span>
							<span class="menu-text">Add Agent </span>
						</s:a></li>
					<li><s:a data-page="Search-Agent" action="searchAgent">
							<span class="nav_icon">AL</span>
							<span class="menu-text">Agent List</span>
						</s:a></li>
					<li><s:a data-page="Assign-MPA" action="assignMpa">
							<span class="nav_icon">AM</span>
							<span class="menu-text">Assign MPA</span>
						</s:a></li>
					<li><s:a action="khadiSubMerchantSearch"
							data-page="Sub-Merchant-List">
							<span class="nav_icon">KL</span>
							<span class="menu-text">Khadi Vendor List</span>
						</s:a></li>

					<li><s:a action="addKhadiUser" data-page="Add-Khadi-User">
							<span class="nav_icon">AK</span>
							<span class="menu-text">Add Khadi Vendor</span>
						</s:a></li>

					<li><s:a action="userList" data-page="Admin-User-List">
							<span class="nav_icon">SUL</span>
							<span class="menu-text">Sub-User List</span>
						</s:a></li>
				</ul></li>


			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-users"></i></span><span class="menu-text">Manage
						Acquirers</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Add-Acquirer" action="addAcquirer">
							<span class="nav_icon">AA</span>
							<span class="menu-text">Add Acquirer</span>
						</s:a></li>
					<li><s:a data-page="Search-Acquirer" action="searchAcquirer">
							<span class="nav_icon">AL</span>
							<span class="menu-text">Acquirers List</span>
						</s:a></li>
					<li><s:a data-page="Acquirer-Default-Charges"
							action='displayAcquirerDefaultRate'>
							<span class="nav_icon">AC</span>
							<span class="menu-text">Acq Default Charges</span>
						</s:a></li>
					<li><s:a data-page="Acquirer-Operations"
							action='acquirerOperationAction'>
							<span class="nav_icon">OP</span>
							<span class="menu-text">Operations</span>
						</s:a></li>
				</ul></li>

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-user"></i></span><span class="menu-text">Customer
						Ops.</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Agent-Search" action='agentSearch'>
							<span class="nav_icon">AS</span>
							<span class="menu-text">Agent Search</span>
						</s:a></li>
					<li><s:a action='complaintRaise' data-page="Complaint-Raise"
							class="head">
							<span class="nav_icon">CR</span>
							<span class="menu-text">Complaint Raise</span>
						</s:a></li>
					<li><s:a data-page="DB-Update" action='agentUpdate'>
							<span class="nav_icon">DB</span>
							<span class="menu-text">DB Update</span>
						</s:a></li>
					<li><s:a data-page="Reset-User-PIN" action='resetUserPin'>
							<span class="nav_icon">RUP</span>
							<span class="menu-text">Reset User PIN</span>
						</s:a></li>


				</ul></li>



			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-circle-o-notch"></i></span><span class="menu-text">Chargeback
						Case</span> <span class="fa fa-angle-down" id="arrow"
					style="margin-left: 24%;"></span> </a>
				<ul>
					<li><s:a data-page="View-Chargeback" action="viewChargeback">
							<span class="nav_icon">VC</span>View Chargeback</s:a></li>

					<li><s:a data-page="View-Chargeback" action="chargebackUtil">
							<span class="nav_icon">CU</span>Chargeback Utility</s:a></li>
				</ul></li>

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-user"></i></span><span class="menu-text">My Account</span> <span
					class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a data-page="Admin-Profile" action="adminProfile">
							<span class="nav_icon">MP</span>My Profile </s:a></li>
					<li><s:a data-page="Login-History"
							action="loginHistoryRedirect">
							<span class="nav_icon">LH</span>Login History</s:a></li>
					<li><s:a data-page="Change-Password" action='passwordChange'>
							<span class="nav_icon">CP</span>Change PIN</s:a></li>
				</ul></li>
		</ul>
	</nav>
	<!-- /.side-wrapper_navigation -->
</aside>
<!-- /.side-wrapper -->


<!-- /. WRAPPER  -->
<!-- JS Scripts-->
<!-- jQuery Js -->

<script src="../js/bootstrap.min.js"></script>
<script>
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

	});
</script>
<!-- /. WRAPPER  -->
<!-- JS Scripts-->
<!-- jQuery Js -->

<!-- <script src="../js/bootstrap.min.js"></script> -->
<script>
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