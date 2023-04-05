<%@page
	import="com.paymentgateway.crm.actionBeans.SessionUserIdentifier"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
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

<aside class="side-wrapper">
	<span id="cancel-btn" class="close-sidebar"><i class="fa fa-times" aria-hidden="true"></i></span>
	<header class="side-wrapper_logo">
        <a class="logo_link" href="home"> 
            <img src="../image/white-logo.png" alt="PayTapp" class="white-logo-png">
            <img src="../image/white-logo-abr.png" alt="PayTapp"
            class="white-logo-abr">
        </a>
    </header>
	<!-- /.side-wrapper_logo -->
	<nav class="side-wrapper_navigation">
		<ul id="navigation" class="nav side-menu">
			<li><s:a action='home' class="head1" data-page="Dashboard">
					<span class="nav_icon"><i class="fa fa-home"></i></span>
					<span class="menu-text">Dashboard</span>
				</s:a></li>
			<!--------------------------Search Payment------------------------------------>
			<li><a style="cursor: pointer" class="head"> <span
					class="nav_icon"><i class="fa fa-bar-chart-o"></i></span><span
					class="menu-text">Quick Search</span><span class="fa fa-angle-down"
					id="arrow"></span></a>
				<ul>
					<li><s:a action='transactionSearch' data-page="Search-Payment">
							<span class="nav_icon">ST</span>
							<span class="menu-text">Search Transaction</span>
						</s:a></li>
					<li><s:a action='downloadPaymentsReport'
							data-page="Download-Payments-Report">
							<span class="nav_icon">DT</span>
							<span class="menu-text">Download Transactions</span>
						</s:a></li>
				</ul></li>
			<!--------------------------Search Payment------------------------------------>

			<% String payid = new PropertiesManager().getSystemProperty("MSEDCL_PAY_ID"); %>
			<s:set var="MSEDCLPAYID"><%=payid%></s:set>

			<s:if test="%{#session['USER'].payId.equals(#MSEDCLPAYID)}">
				<li>
					<a class="head">
						<span class="nav_icon"><i class="fa fa-bar-chart-o"></i></span>
						<span class="menu-text">MSEDCL</span>
						<span class="fa fa-angle-down" id="arrow"></span>
					</a>
					<ul>
						<li>
							<s:a data-page="B60-Download" action='b60Download'>
								<span class="nav_icon">BD</span>
								<span class="menu-text">B60 Download</span>
							</s:a>
						</li>
						<li>
							<s:a data-page="LT-Collection" action='ltCollection'>
								<span class="nav_icon">LC</span>
								<span class="menu-text">LT Collection</span>
							</s:a>
						</li>
					</ul>
				</li>
			</s:if>
			
			<%
				String coinSwitchPayId = new PropertiesManager().getSystemProperty("CoinSwitch_Merchant_PayId");
			%>
			<s:set var="COINSWITCHPAYID"><%=coinSwitchPayId%></s:set>

			<s:if test="%{#session['USER'].payId.equals(#COINSWITCHPAYID)}">

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-bar-chart-o"></i></span><span class="menu-text">Customer VA Reports</span>
						<span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<li><s:a data-page="Customer-Info-Report" action='customerInfoReport'>
								<span class="nav_icon">CR</span>
								<span class="menu-text">Customer Report</span>
							</s:a></li>
						<li><s:a data-page="Transaction-Info-Report" action='transactionInfoReport'>
								<span class="nav_icon">TR</span>
								<span class="menu-text">Transaction Report</span>
							</s:a></li>
					</ul>
				</li>
			</s:if>

			<!--------------------------Transaction Report ------------------------------------>
			<li><a class="head"> <span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">Reporting</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left: 18%;"></span></a>
				<ul>
					<li><s:a action='saleTransactionSearch'
							data-page="Sale-Captured">
							<span class="nav_icon">SC</span>
							<span class="menu-text">Sale Captured</span>
						</s:a></li>
					<li><s:a action='refundTransactionSearch'
							data-page="Refund-Captured">
							<span class="nav_icon">RC</span>
							<span class="menu-text">Refund Captured</span>
						</s:a></li>
					<li><s:a action='settledTransactionSearch'
							data-page="Settled-Transaction">
							<span class="nav_icon">S</span>
							<span class="menu-text">Settled</span>
						</s:a></li>
					<li><s:a action="paymentAdviseReport"
							data-page="Download-Payment-Advise-Report">
							<span class="nav_icon">PA</span>
							<span class="menu-text">Payment Advice</span>
						</s:a>
					</li>

					

					<s:if test="%{#session['USER_SETTINGS'].virtualAccountFlag == true}">
						<li>
							<s:a data-page="eCollection-Report" action='eCollection'>
								<span class="nav_icon">CR</span>
								<span class="menu-text">eCollection Report</span>
							</s:a>
						</li>
					</s:if>
					

					<li><s:a data-page="Historical-Generated-Report" action='historicalGenerateAction'>
							<span class="nav_icon">HR</span>
							<span class="menu-text">Historical Report</span>
						</s:a></li>
						
					<s:if test="%{#session.USER.UserType.name() == 'MERCHANT' && #session['USER'].superMerchantId != null && #session['USER'].superMerchant == false}">
						<s:if test="%{#session['USER_SETTINGS'].netSettledFlag}">
							<li>
								<s:a data-page="Net-Settled-Report" action='settledConsolidatedReport'>
									<span class="nav_icon">NS</span>
									<span class="menu-text">Net Settled</span>
								</s:a>
							</li>
						</s:if>
					</s:if>
					<s:else>
						<li>
							<s:a data-page="Net-Settled-Report" action='settledConsolidatedReport'>
								<span class="nav_icon">NS</span>
								<span class="menu-text">Net Settled</span>
							</s:a>
						</li>
					</s:else>
					<li><s:a data-page="Refund-Limit-Report" action='refundLimitTrails'>
							<span class="nav_icon">RL</span>
							<span class="menu-text">Refund Limit Report</span>
						</s:a></li>
					
						
					<%--
					<li>
						<s:a data-page="Vendor-Payout" action='vendorPayoutReportAction'>
							<span class="nav_icon">VP</span>
							<span class="menu-text">Vendor Payout</span>
						</s:a>
					</li>
					--%>
				</ul>
			</li>

			<!--------------------------Transaction Report ------------------------------------>

			

			 <s:if test="%{#session['USER_SETTINGS'].eNachReportFlag == true || #session['USER_SETTINGS'].upiAutoPayReportFlag == true}">
			<li><a class="head"><span class="nav_icon"><i
				class="fa fa-bar-chart-o"></i></span><span class="menu-text">Subscription</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
				 <s:if test="%{#session['USER_SETTINGS'].eNachReportFlag == true}">
					<li>
						<s:a data-page="eNACH"
						action='eNachReports'>
						<span class="nav_icon">E</span>
						<span class="menu-text">eNACH</span>
						</s:a>
					</li>
					</s:if>
					<s:if test="%{#session['USER_SETTINGS'].upiAutoPayReportFlag == true}">
					<li>
						<s:a data-page="UPI-AutoPay"
						action='upiAutoPayReports'>
						<span class="nav_icon">UA</span>
						<span class="menu-text">UPI AutoPay</span>
						</s:a>
					</li>
					</s:if>
				</ul>
			</li>
			</s:if>

			<s:if test="%{#session['USER_SETTINGS'].retailMerchantFlag == true}">
				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-bar-chart-o"></i></span><span class="menu-text">Vendor
							Payout</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>

						<li><s:a action="khadiProductReport"
								data-page="Product-Wise-Report">
								<span class="nav_icon">PR</span>
								<span class="menu-text"> Product Report</span>
							</s:a></li>

						<li><s:a data-page="Vendor-Payout"
								action='vendorPayoutReportAction'>
								<span class="nav_icon">PR</span>
								<span class="menu-text">Payout Report</span>
							</s:a></li>

					</ul></li>
			</s:if>

			<%-- <s:if
					test="%{#session['USER'].superMerchant == false}"> --%>
			<li><a style="cursor: pointer" class="head"> <span
					class="nav_icon"><i class="fa fa fa-money"></i></span><span
					class="menu-text">Quick Pay</span><span class="fa fa-angle-down"
					id="arrow"></span></a>
				<ul>
					<li><s:a action="invoice" data-page="Invoice-Payment">
							<span class="nav_icon">IP</span>
							<span class="menu-text">Invoice Payment</span>
						</s:a></li>
					<li><s:a action="bulkInvoice" data-page="Bulk-Invoice">
							<span class="nav_icon">BI</span>
							<span class="menu-text">Bulk Invoice</span>
						</s:a></li>
					<li><s:a action="invoiceSearch" data-page="Invoice-Search">
							<span class="nav_icon">SQP</span>
							<span class="menu-text">Search Quick Payment</span>
						</s:a></li>
					<li><s:a action="bulkInvoiceSearch"
							data-page="Bulk-Invoice-Search">
							<span class="nav_icon">BIS</span>
							<span class="menu-text">Bulk Invoice Search</span>
						</s:a></li>
					<s:if test="%{#session['USER_SETTINGS'].loadWalletFlag == true}">
						<li><s:a data-page="Load-Wallet" action="uploadBalance">
								<span class="nav_icon">LW</span>
								<span class="menu-text">Load Wallet</span>
							</s:a></li>
					</s:if>
				</ul></li>
			<%-- </s:if> --%>

			<s:if
				test="%{#session['USER_SETTINGS'].nodalReportFlag == true || #session['USER_SETTINGS'].merchantInitiatedDirectFlag == true || #session.USER_SETTINGS.allowNodalPayoutFlag == true}">

				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-users"></i></span><span class="menu-text">Banking</span>
						<span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>

                       <s:if test="%{#session['USER_SETTINGS'].merchantInitiatedDirectFlag == true}">
						<li><s:a data-page="Merchant-Payouts" action="singleAccountTransfer">
							<span class="nav_icon">P</span>
							<span class="menu-text">Payout</span>
						</s:a></li>
						
						
						<li><s:a data-page="Bene-Verification-Report" action="beneVerificationReport">
							<span class="nav_icon">AB</span>
							<span class="menu-text">Add Beneficiary</span>
						</s:a></li>
						</s:if>

						<s:if test="%{#session.USER_SETTINGS.allowNodalPayoutFlag == true}">
							<li>
								<s:a data-page="Nodal-Transfer" action="nodalTransfer">
									<span class="nav_icon">NT</span>
									<span class="menu-text">Nodal Transfer</span>
								</s:a>
							</li>
						</s:if>

						<s:if test="%{#session['USER_SETTINGS'].nodalReportFlag == true}">
							<li><s:a data-page="Nodal-Report" action="nodalReport">
									<span class="nav_icon">NS</span>
									<span class="menu-text">Nodal Settlement</span>
								</s:a></li>
						</s:if>

					</ul></li>
			</s:if>

			<s:if
			test="%{#session['USER_SETTINGS'].vpaVerificationFlag == true || #session['USER_SETTINGS'].accountVerificationFlag == true}">

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-users"></i></span><span class="menu-text">Bene Verification</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if test="%{#session['USER_SETTINGS'].accountVerificationFlag == true}">
						<li><s:a data-page="Account-Verification"
								action="impsBeneVerification">
								<span class="nav_icon">AV</span>
								<span class="menu-text">Account Verification</span>
							</s:a></li>
					</s:if>
			  	  <s:if test="%{#session['USER_SETTINGS'].vpaVerificationFlag == true}">
						<li><s:a data-page="VPA-Verification" action="vpaValidate">
							<span class="nav_icon">VV</span>
							<span class="menu-text">VPA Verification</span>
						</s:a></li>
					</s:if>

				</ul></li>
		</s:if>



			<li><a style="cursor: pointer" class="head"> <span
					class="nav_icon"><i class="fa fa-circle-o-notch"></i></span><span
					class="menu-text">Chargeback Case</span><span
					class="fa fa-angle-down" id="arrow"></span></a>
				<ul>
					<li><s:a action="viewChargeback" data-page="View-Chargeback">
							<span class="nav_icon">VC</span>
							<span class="menu-text">View Chargeback </span>
						</s:a></li>
				</ul></li>

			<li><a style="cursor: pointer" class="head"> <span
					class="nav_icon"><i class="fa fa-users"></i></span><span
					class="menu-text">Manage User</span><span class="fa fa-angle-down"
					id="arrow"></span></a>
				<ul>
					<s:if test="%{#session['USER'].superMerchant == true}">
						<li><s:a action="merchantCrmSignup" data-page="Sign-Up">
								<span class="nav_icon">AS</span>
								<span class="menu-text">Add Sub-Merchant </span>
							</s:a></li>
						<li><s:a action="subMerchantSearch"
								data-page="Sub-Merchant-List">
								<span class="nav_icon">SL</span>
								<span class="menu-text">Sub-Merchant List</span>
							</s:a></li>
					</s:if>

					<li>
					<s:a action="userList" data-page="Sub-User-List">
                            <span class="nav_icon">SUL</span>
                            <span class="menu-text">Sub-User List</span>
                    </s:a>
						<%-- <s:a action="addUser" data-page="Add-User">
							<span class="nav_icon">AU</span>
							<span class="menu-text">Add User </span>
						</s:a>
					</li>
					<li>
						<s:a action="searchUser" data-page="Search-User">
							<span class="nav_icon">UL</span>
							<span class="menu-text">User List</span>
						</s:a> --%>
					</li>

					<!-- <li><s:a
						 action='loginHistoryRedirectUser' data-page="Login-History"><span class="nav_icon">LH</span><span class="menu-text">Login History</span></s:a>
					</li> -->
				</ul></li>

				<li><a style="cursor: pointer" class="head"> <span
					class="nav_icon"><i class="fa fa-file"></i></span><span
					class="menu-text">Customer Ops.</span><span class="fa fa-angle-down"
					id="arrow"></span></a>
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
			<!-- ticketing -->

			<li><a style="cursor: pointer" class="head"> <span
					class="nav_icon"><i class="fa fa-user"></i></span><span
					class="menu-text">My Account</span><span class="fa fa-angle-down"
					id="arrow"></span></a>
				<ul>
					<li><s:a action="merchantProfile" data-page="Merchant-Profile">
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


			
		</ul>
	</nav>
	<!-- /.side-wrapper_navigation -->
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
