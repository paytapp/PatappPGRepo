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
	$(document).ready(function(e) {

		
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
		});
		

		$(".head").click(function(e) {
			// $(".head").next().slideUp();
			$(this).next().slideToggle();
		});
		

		var _getActive = $(document).attr('title');
		// console.log(_getActive);
		var _setWord = "";
		for(var i = 0; i < _getActive.length; i++){
			if(_getActive[i].indexOf(" ") != -1){
				_setWord += _getActive[i].replace(" ", "-");
			}else{
				_setWord += _getActive[i];
			}
		}

		$("[data-page="+_setWord+"]").closest("ul").prev(".head").addClass("selected");

		$("[data-page="+_setWord+"]").closest("li").addClass("selected");
		$("[data-page="+_setWord+"]").addClass("current");

		$(".collapse-nav").on("click", function(e) {
			$(".collapse-nav").toggleClass("collapse-icon");
			$(".side-wrapper").toggleClass("active-sidebar");
			$(".side-wrapper").toggleClass("active-collapse");
			$(".right_col").toggleClass("right_col_extend");
		});

		$(".side-wrapper").on("hover", function(e) {
			var _isSidebarActive = $(this).hasClass("active-sidebar");
			if (_isSidebarActive == true) {
				$(".side-wrapper").toggleClass("active-collapse");
				$(".right_col").toggleClass("right_col_extend");
			}
		})

		var _navScroll = document.querySelector(".side-wrapper_navigation");
		function navScroll(){
			var _selectPosition = $(".selected").offset().top;
			var _windowSize = $(window).height();
			console.log(_selectPosition);
			console.log(_windowSize);
			if(_selectPosition > _windowSize){
				_navScroll.scrollTo(0, _windowSize);	
				console.log("hello");
			}
		}

		navScroll();

	})
</script>

<style>
span#arrow {
	margin-left: 0 !important;
	float: right;
	margin-top: 7px;
}
</style>

<aside class="side-wrapper">
	<span id="cancel-btn" class="close-sidebar"><i class="fa fa-times" aria-hidden="true"></i></span>
	<header class="side-wrapper_logo">
        <a class="logo_link" href="home"> 
                <img src="../image/white-logo.png" alt="Payment Gateway" class="white-logo-png">
                <img src="../image/white-logo-abr.png" alt="Payment Gateway"
                class="white-logo-abr">
        </a>
    </header>
	<!-- /.side-wrapper_logo -->
	<div class="side-wrapper_navigation">
		<ul id="navigation" class="nav side-menu">
			<li><s:a action='home' class="head1" data-page="Dashboard">
					<span class="nav_icon"><i class="fa fa-home"></i></span>
					<span class="menu-text">Dashboard</span>
				</s:a></li>

			<!-- <li><a class="head"><span class="nav_icon"><i
						class="fa fa-line-chart" aria-hidden="true"></i></span><span
					class="menu-text">Analytics</span> <span class="fa fa-angle-down"
					id="arrow"></span> </a>
				<ul>
					<li><s:a action='resellerRevenue' data-page="Reseller-Revenue-Report">
							<span class="nav_icon">RR</span>
							<span class="menu-text">Reseller Revenue</span>
						</s:a></li>
				</ul></li> -->


			<!-- <li>
				<a class="head"><span class="nav_icon"><i class="fa fa-list-alt"></i></span><span class="menu-text">Reseller Setup</span> 
					<span class="fa fa-angle-down" id="arrow"></span>
				</a>

				<ul>
					<li><s:a action='resellerList'><span class="nav_icon">RML</span><span class="menu-text">Reseller Merchant List</span></s:a></li>
					<li><s:a action='merchantSetup' class="sublinks"
						onclick='return false'><span class="nav_icon">MS</span><span class="menu-text">Merchant Setup</span></s:a></li>
				</ul>
			</li> -->

			<li>
				<a class="head">
					<span class="nav_icon"><i class="fa fa-list-alt"></i></span>
					<span class="menu-text">Merchant Setup</span> 
					<span class="fa fa-angle-down" id="arrow"></span> 
				</a>
				<ul>
					<s:if test="%{#session['USER'].resellerMerchantSignupFlag == true}">
						<li>
							<s:a action='merchantCrmSignup' data-page="Registration">
								<span class="nav_icon">UR</span>
								<span class="menu-text">User Registration</span>
							</s:a>
						</li>
					</s:if>
					<li>
						<s:a action='merchantSearch' data-page="Merchant-List">
							<span class="nav_icon">ML</span>
							<span class="menu-text">Merchant List</span>
						</s:a>
					</li>
					<li>
						<s:a action='merchantDetailsUpdate' data-page="Merchant-Details">
							<span class="nav_icon">MD</span>
							<span class="menu-text">Merchant Details</span>
						</s:a>
					</li>						
					<s:if test="%{#session['USER'].resellerUserStatusFlag == true}">
                    	<li>
							<s:a data-page="Activation" action='activation'>
								<span class="nav_icon">US</span>
								<span class="menu-text">User Status</span>
                            </s:a>
                        </li>
                    </s:if>
					<li>
						<s:a data-page="Sub-Merchant-List"
						action="subMerchantSearch">
						<span class="nav_icon">SL</span>
						<span class="menu-text">Sub-Merchant List</span>
						</s:a>
					</li>
				</ul>
			</li>

			<!--------------------------Search Payment------------------------------------>
			<s:if test="%{#session['USER_PERMISSION'].contains('Quick Search')}">
				<li><a class="head"><span class="nav_icon"><i
							class="fa fa-bar-chart-o"></i></span><span class="menu-text">Quick
							Search </span> <span class="fa fa-angle-down" id="arrow"></span> </a>
					<ul>
						<li><s:a action='transactionSearch'
								data-page="Search-Payment">
								<span class="nav_icon">ST</span>
								<span class="menu-text">Search Transaction</span>
							</s:a></li>
						<li><s:a action='downloadPaymentsReport'
								data-page="Download-Payments-Report">
								<span class="nav_icon">DT</span>
								<span class="menu-text">Download Transaction</span>
							</s:a></li>
					</ul></li>
			</s:if>

			<!--------------------------Search Payment------------------------------------>
			<!--------------------------Transaction Report ------------------------------------>
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-bar-chart-o"></i></span><span class="menu-text">
						Reporting</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if test="%{#session['USER_PERMISSION'].contains('Sale Capture')}">
					<li><s:a action='saleTransactionSearch' data-page="Sale-Captured">
							<span class="nav_icon">SC</span>
							<span class="menu-text">Sale Captured</span>
						</s:a></li>
					</s:if>
					<s:if test="%{#session['USER_PERMISSION'].contains('Refund Capture')}">
					<li><s:a action='refundTransactionSearch' data-page="Refund-Captured">
							<span class="nav_icon">RC</span>
							<span class="menu-text">Refund Captured</span>
						</s:a></li>
					</s:if>
					<s:if test="%{#session['USER_PERMISSION'].contains('Settled')}">
					<li><s:a action='settledTransactionSearch' data-page="Settled-Transaction">
							<span class="nav_icon">S</span>
							<span class="menu-text">Settled</span>
						</s:a></li>
					</s:if>
						<li><s:a action='resellerRevenue' data-page="Reseller-Revenue-Report">
							<span class="nav_icon">RR</span>
							<span class="menu-text">Reseller Revenue</span>
						</s:a></li>
					<s:if test="%{#session['USER_PERMISSION'].contains('Payment Advice')}">
					<li><s:a action="paymentAdviseReport" data-page="Download-Payment-Advise-Report">
							<span class="nav_icon">PA</span>
							<span class="menu-text">Payment Advise</span>
						</s:a></li>
					</s:if>
					<s:if test="%{#session['USER_PERMISSION'].contains('Custom Capture Report')}">
					<li><s:a data-page="Custom-Capture-Report"
							action='customCaptureReport'>
							<span class="nav_icon">CC</span>
							<span class="menu-text">Custom Capture</span>
						</s:a></li>
					</s:if>	
					<s:if test="%{#session['USER_PERMISSION'].contains('Booking Report')}">
					<li><s:a data-page="Booking-Report"
							action='bookingRecordSearch'>
							<span class="nav_icon">BR</span>
							<span class="menu-text">Booking Report</span>
						</s:a></li>
					</s:if>						
					<s:if test="%{#session['USER_PERMISSION'].contains('eCollection Transaction') || #session['USER_PERMISSION'].contains('Virtual Account List')}">
					<li><s:a data-page="eCollection-Report" action='eCollection'>
						<span class="nav_icon">EC</span>
						<span class="menu-text">eCollection</span>
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
			<!--------------------------Transaction Report ------------------------------------>
			
			<s:if
					test="%{#session['USER'].partnerFlag == false}">
					<s:if
			test="%{#session['USER'].merchantInitiatedDirectFlag == true}">
			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-users"></i></span><span class="menu-text">Banking</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
						<li><s:a data-page="Single-Account-Transfer" action="singleAccountTransfer">
							<span class="nav_icon">P</span>
							<span class="menu-text">Payout</span>
						</s:a></li>
						
						<li><s:a data-page="Bene-Verification-Report" action="beneVerificationReport">
							<span class="nav_icon">AB</span>
							<span class="menu-text">Add Beneficiary</span>
						</s:a></li>
				</ul>
			</li>
			</s:if>
			</s:if>
			
			<s:if test="%{#session['USER'].partnerFlag == false}">
			<s:if
			test="%{#session['USER'].vpaVerificationFlag == true || #session['USER'].accountVerificationFlag == true}">

			<li><a class="head"><span class="nav_icon"><i
						class="fa fa-users"></i></span><span class="menu-text">Bene Verification</span>
					<span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<s:if test="%{#session['USER'].accountVerificationFlag == true}">
						<li><s:a data-page="Account-Verification"
								action="impsBeneVerification">
								<span class="nav_icon">AV</span>
								<span class="menu-text">Account Verification</span>
							</s:a></li>
					</s:if>
			  	<s:if test="%{#session['USER'].vpaVerificationFlag == true}">
					<li>
						<s:a data-page="VPA-Verification" action="vpaValidate">
							<span class="nav_icon">VV</span>
							<span class="menu-text">VPA Verification</span>
						</s:a>
					</li>
				</s:if>

				</ul></li>
		</s:if>
		</s:if>
			
			<s:if test="%{#session['USER'].eNachReportFlag == true}">
			<li><a class="head"><span class="nav_icon"><i
				class="fa fa-bar-chart-o"></i></span><span class="menu-text">Subscription</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li>
						<s:a data-page="eNACH-Report" action='eNachReports'>
							<span class="nav_icon">ER</span>
							<span class="menu-text">eNACH Report</span>
						</s:a>
					</li>
					
					<li>
						<s:a data-page="Debit-Report" action='eNachDebitReport'>
							<span class="nav_icon">EDR</span>
							<span class="menu-text">eNACH Debit Report</span>
						</s:a>
					</li>
					
				</ul>
			</li>
			</s:if>
			

			<%-- <li><a class="head"><span class="nav_icon"><i class="fa fa-list"></i></span><span class="menu-text">View Configuration</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a action='viewResellerCharges' data-page="View-Reseller-Charges"><span class="nav_icon">RC</span><span class="menu-text">Reseller Charges</span></s:a>
					</li>
				</ul>
			</li> --%>

			<li>
				<a class="head"><span class="nav_icon"><i class="fa fa-user"></i></span><span class="menu-text">Customer Ops.</span> <span
					class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li>
						<s:a action='complaintRaise' data-page="Complaint-Raise" class="head">
							<span class="nav_icon">CR</span>
							<span class="menu-text">Complaint Raise</span>
						</s:a>
					</li>
				</ul>
			</li>

			<li>
				<a class="head"><span class="nav_icon"><i class="fa fa-user"></i></span><span class="menu-text">My Account</span> <span
					class="fa fa-angle-down" id="arrow"></span> </a>
				<ul>
					<li><s:a action="adminProfile" data-page="Admin-Profile"><span class="nav_icon">MP</span>My Profile </s:a></li>
					<li><s:a action="loginHistoryRedirect" data-page="Login-History"><span class="nav_icon">LH</span>Login History</s:a></li>
					<li><s:a action='passwordChange' data-page="Change-Password"><span class="nav_icon">CP</span>Change PIN</s:a></li>
				</ul>
			</li>
		</ul>
	</div>
	<!-- /.side-wrapper_navigation -->
</aside>

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
