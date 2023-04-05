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
	<header class="side-wrapper_logo">
		<a class="logo_link" href="home">
			<img src="../image/white-logo.png" alt="Pg" class="white-logo-png">
			<img src="../image/white-logo-abr.png" alt="Pg" class="white-logo-abr">
		</a>
	</header>
	<!-- /.side-wrapper_logo -->
	<nav class="side-wrapper_navigation">
		<ul id="navigation" class="nav side-menu">
			<!---	<li>
				<s:a action='home' class="head1" data-page="Dashboard"><span class="nav_icon"><i class="fa fa-home"></i></span><span class="menu-text">Dashboard</span></s:a>
			</li> ------>

			<!--------------------------File Upload ------------------------------------>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-upload"></i></span><span class="menu-text">Upload Files</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='bookingTxnUpload' data-page="Booking-Upload"><span class="nav_icon">UBR</span><span class="menu-text">Upload Booking Report</span></s:a></li>
					<li><s:a action='refundTxnUpload' data-page="Refund-Upload"><span class="nav_icon">URR</span><span class="menu-text">Upload Refund Report</span></s:a></li>
					<li><s:a action='mprTxnUpload' data-page="MPR-Upload"><span class="nav_icon">UM</span><span class="menu-text">Upload MPR</span></s:a></li>
					<li><s:a action='statementUpload' data-page="Statement-Upload"><span class="nav_icon">US</span><span class="menu-text">Upload Statement</span></s:a></li>
				</ul>
			</li>
			<!--------------------------File Upload ------------------------------------>	

			<!--------------------------Search Payment------------------------------------>
			<li>
				<a style="cursor: pointer" class="head">
					<span class="nav_icon"><i class="fa fa-search"></i></span><span class="menu-text">Search Payments</span><span
					class="fa fa-angle-down" id="arrow"></span></a>
				<ul>
					<li><s:a action='transactionReconSearch' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Search</span></s:a></li>
				</ul>
			</li>
			<!--------------------------Search Payment------------------------------------>
			<!--------------------------Transaction Report ------------------------------------>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-bar-chart"></i></span><span class="menu-text">Transaction Report</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='saleReconTransactionSearch' data-page="Sale-Captured"><span class="nav_icon">B</span><span class="menu-text">Bookings</span></s:a></li>
					<li><s:a action='refundReconTransactionSearch' data-page="Refund-Captured"><span class="nav_icon">R</span><span class="menu-text">Refunds</span></s:a></li>
					<li><s:a action='settledReconTransactionSearch' data-page="Settled-Transaction"><span class="nav_icon">S</span><span class="menu-text">Settlements</span></s:a></li>
					<li><s:a action='reconExceptionSearch' data-page="Exceptions"><span class="nav_icon">EX</span><span class="menu-text">Exceptions</span></s:a></li>
				</ul>
			</li>
			<!--------------------------Transaction Report ------------------------------------>
			
			<!--------------------------Accounts ------------------------------------>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-bar-chart-o"></i></span><span class="menu-text">Acquirer Wise Report</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
				
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Rupay</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchRupay' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchRupay' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconSearchRupay' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Amex</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchAmex' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchAmex' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconResultsAmex' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">IPAY</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchIPAY' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearch' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">BOB</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchBob' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchBob' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconResultsBob' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
					<li><s:a action='reversalsBob' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Reversals</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Allahabad Bank</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchAllahabad' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchAllahabad' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconResultsAllahabad' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Bank Of Maharashtra</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchMaharashtra' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchMaharashtra' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconResultsMaharashtra' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Corporation Bank</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchCorporation' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchCorporation' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconResultsCorporation' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Karur Bank</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearchKarur' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearchKarur' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
					<li><s:a action='transactionReconResultsKarur' data-page="Search-Payment"><span class="nav_icon">S</span><span class="menu-text">Transactions</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">OBC</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearch' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearch' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
				</ul>
			</li>
			<li><a class="head">
				<span class="nav_icon"><i class="fa fa-university"></i></span><span class="menu-text">Punjab National Bank</span><span
					class="fa fa-angle-down" id="arrow" style="margin-left:18%;"></span></a>
				<ul>
					<li><s:a action='accountStatementSearch' data-page="Statement"><span class="nav_icon">SWR</span><span class="menu-text">Statement Wise Report</span></s:a></li>
					<li><s:a action='settleSummarySearch' data-page="Statement"><span class="nav_icon">SS</span><span class="menu-text">Settlement Summary</span></s:a></li>
				</ul>
			</li>
			
			</ul>
			</li>
			<!--------------------------Accounts  ------------------------------------>
			

		<!---	<li><a style="cursor: pointer" class="head">
				<span class="nav_icon"><i class="fa fa-users"></i></span><span class="menu-text">Manage User</span><span
					class="fa fa-angle-down" id="arrow"></span></a>
				<ul>
					<li><s:a action="addUser" data-page="Add-User"><span class="nav_icon">AU</span><span class="menu-text">Add User </span></s:a></li>
					<li><s:a action="searchUser" data-page="Search-User"><span class="nav_icon">UL</span><span class="menu-text">User List</span></s:a></li>
					
				</ul>
			</li>
-->
			<li><a style="cursor: pointer" class="head">
				<span class="nav_icon"><i class="fa fa-user"></i></span><span class="menu-text">My Account</span><span
					class="fa fa-angle-down" id="arrow"></span></a>
				<ul>
					<li><s:a action='dispatchSlip' data-page="Dispatch-Details"><span class="nav_icon">DD</span><span class="menu-text">Dispatch Details</span>
					<li><s:a action="merchantProfile" data-page="Merchant-Profile"><span class="nav_icon">MP</span><span class="menu-text">My Profile </span></s:a></li>
					</s:a></li>
					<li><s:a action="loginHistoryRedirect" data-page="Login-History"><span class="nav_icon">LH</span><span class="menu-text">Login History</span></s:a>
					</li>
					<li><s:a action='passwordChange' data-page="Change-Password"><span class="nav_icon">CP</span><span class="menu-text">Change PIN</span></s:a></li>

				</ul></li>
				
			
				<li>
					<a style="cursor: pointer" class="head">
						<span class="nav_icon"><i class="fa fa-file"></i></span><span class="menu-text">Agent Access</span><span class="fa fa-angle-down" id="arrow"></span></a>
						 <ul>
							<li><s:a action='agentReconSearch' data-page="Agent-Search"><span class="nav_icon">AS</span><span class="menu-text">Agent Search</span></s:a></li>
						 </ul>
					</li> 
			<!-- ticketing -->
		</ul>
	</nav>
	<!-- /.side-wrapper_navigation -->
 </aside>
<!-- /.side-wrapper -->


<script src="../js/bootstrap.min.js"></script>
<script>
	;(function ($, window, document, undefined) {

	    var pluginName = "metisMenu",
	        defaults = {
	            toggle: true
	        };
	        
	    function Plugin(element, options) {
	        this.element = element;
	        this.settings = $.extend({}, defaults, options);
	        this._defaults = defaults;
	        this._name = pluginName;
	        this.init();
	    }

	    Plugin.prototype = {
	        init: function () {

	            var $this = $(this.element),
	                $toggle = this.settings.toggle;

	            $this.find('li.active').has('ul').children('ul').addClass('collapse in');
	            $this.find('li').not('.active').has('ul').children('ul').addClass('collapse');

	            $this.find('li').has('ul').children('a').on('click', function (e) {
	                e.preventDefault();

	                $(this).parent('li').toggleClass('active').children('ul').collapse('toggle');

	                if ($toggle) {
	                    $(this).parent('li').siblings().removeClass('active').children('ul.in').collapse('hide');
	                }
	            });
	        }
	    };

	    $.fn[ pluginName ] = function (options) {
	        return this.each(function () {
	            if (!$.data(this, "plugin_" + pluginName)) {
	                $.data(this, "plugin_" + pluginName, new Plugin(this, options));
	            }
	        });
	    };

	})(jQuery, window, document);
</script>
<script>

(function ($) {
"use strict";
var mainApp = {

    initFunction: function () {
        /*MENU 
        ------------------------------------*/
        $('#main-menu').metisMenu();
		
        $(window).bind("load resize", function () {
            if ($(this).width() < 768) {
                $('div.sidebar-collapse').addClass('collapse')
            } else {
                $('div.sidebar-collapse').removeClass('collapse')
            }
        });

 
    },

    initialization: function () {
        mainApp.initFunction();

    }

}
// Initializing ///

$(document).ready(function () {
    mainApp.initFunction();
});

}(jQuery));
</script>
