
<%@taglib prefix="s" uri="/struts-tags" %>
    <link rel="icon" href="../image/favicon-32x32.png">
    <link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
    <link href="../css/welcomePage.css" rel="stylesheet">
    <script src="../js/jquery.min.js"></script>
    <link rel="stylesheet" href="../css/styles.css">
    <script>
        $.noConflict();
    </script>
    <script>

        $(document).ready(function (e) {

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

            window.addEventListener("resize", function (e) {
                resizeHeight()
            })

            $(".head").click(function (e) {
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

            $(".collapse-nav").on("click", function (e) {
                $(".collapse-nav").toggleClass("collapse-icon");
                $(".side-wrapper").toggleClass("active-sidebar");
                $(".side-wrapper").toggleClass("active-collapse");
                $(".right_col").toggleClass("right_col_extend");
            });

            $(".side-wrapper").hover(function (e) {
                var _isSidebarActive = $(this).hasClass("active-sidebar");
                if (_isSidebarActive == true) {
                    $(".side-wrapper").toggleClass("active-collapse");
                    $(".right_col").toggleClass("right_col_extend");
                }
            })

            var _scroll = document.querySelector(".head.selected").getBoundingClientRect();
            var _sideWrapper = document.querySelector(".side-wrapper_navigation");
            var _viewPort = window.innerHeight;
            var _scrollTop = _scroll.top;
            if (_scroll != null && _scrollTop > _viewPort) {
                document.querySelector(".side-wrapper_navigation").scrollTop = _scroll.top / 2;
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

    <s:hidden id="userType" value="%{#session['subUserType']}"></s:hidden>

    <aside class="side-wrapper">
        <span id="cancel-btn" class="close-sidebar"><i class="fa fa-times" aria-hidden="true"></i></span>
        <header class="side-wrapper_logo">
            <a class="logo_link" href="h 543123ome">
                <img src="../image/white-logo.png" alt="Payment Gateway" class="white-logo-png">
                <img src="../image/white-logo-abr.png" alt="Payment Gateway" class="white-logo-abr">
            </a>
        </header>
        <!-- /.side-wrapper_logo -->
        <div class="side-wrapper_navigation">
            <ul id="navigation" class="nav">
                <s:hidden id="userType" value="%{#session['SUBUSERTYPE']}"></s:hidden>
                <s:if test="%{#session['SUBUSERTYPE'] != 'vendorType'}">
                    <li>
                        <s:a action='home' data-page="Dashboard" class="head1">
                            <span class="nav_icon"><i class="fa fa-home"></i></span>
                            <span class="menu-text">Dashboard</span>
                        </s:a>
                    </li>
                </s:if>
                <s:if test="%{#session['SUBUSERTYPE'].indexOf('vendorType') === -1}">
                    <li>
                        <s:a action='transactionSearch' class="head1"><span class="nav_icon"><i
                                    class="fa fa-search"></i></span><span class="menu-text">Search Payment</span></s:a>
                    </li>
                </s:if>
                <s:if test="%{#session['USER_PERMISSION'].contains('View SearchPayment')}">
                    <li>
                        <a style="cursor: pointer" class="head">
                            <span class="nav_icon"><i class="fa fa-bar-chart-o"></i></span><span class="menu-text">Quick
                                Search</span><span class="fa fa-angle-down" id="arrow"></span></a>
                        <ul>
                            <li>
                                <s:a action='transactionSearch' data-page="Search-Payment"><span
                                        class="nav_icon">S</span><span class="menu-text">Search Transaction</span></s:a>
                            </li>
                            <li>
                                <s:a action='downloadPaymentsReport' data-page="Download-Payments-Report"><span
                                        class="nav_icon">DR</span><span class="menu-text">Download Transactions</span>
                                </s:a>
                            </li>
                        </ul>
                    </li>
                </s:if>

                    <s:if test="%{#session['USER_PERMISSION'].contains('View Transaction Reports')}">
                        <li>
                            <a style="cursor: pointer" class="head">
                                <span class="nav_icon"><i class="fa fa-bar-chart-o"></i></span><span
                                    class="menu-text">Reporting</span><span class="fa fa-angle-down"
                                    id="arrow"></span></a>
                            <ul>
                                <li>
                                    <s:a action='saleTransactionSearch'><span class="nav_icon">SC</span><span
                                            class="menu-text">Sale Captured</span></s:a>
                                </li>
                                <li>
                                    <s:a action='refundTransactionSearch'><span class="nav_icon">RC</span><span
                                            class="menu-text">Refund Captured</span></s:a>
                                </li>
                                <li>
                                    <s:a action='settledTransactionSearch'><span class="nav_icon">S</span><span
                                            class="menu-text">Settled</span></s:a>
                                </li>
                                
                            <s:if test="%{#session['USER_PERMISSION'].contains('Payment Advice')}">
                         	<li>
                            	<s:a action="paymentAdviseReport"><span class="nav_icon">PA</span><span
                                    class="menu-text">Payment Advise</span></s:a>
                            </li>
                            </s:if>

                            <s:if test="%{#session['USER_PERMISSION'].contains('Booking Report')}">
                                <li>
                                    <s:a action='bookingRecordSearch'><span class="nav_icon">BR</span>Booking Report
                                    </s:a>
                                </li>
                            </s:if>
                            <s:if test="%{#session['USER'].schoolManagement == true}">
                                <li>
                                    <s:a action="schoolInventory"><span class="nav_icon">SL</span><span
                                            class="menu-text">Student List</span></s:a>
                                </li>
                                <li>
                                    <s:a action="schoolReport"><span class="nav_icon">PR</span><span
                                            class="menu-text">Payment Report</span></s:a>
                                </li>
                            </s:if>
                            <s:if test="%{#session['USER_PERMISSION'].contains('Custom Capture Report')}">
                                <li>
                                    <s:a data-page="Custom-Capture-Report" action='customCaptureReport'>
                                        <span class="nav_icon">CCR</span>
                                        <span class="menu-text">Custom Capture Report</span>
                                    </s:a>
                                </li>
                            </s:if>
                            <s:if test="%{#session['USER_PERMISSION'].contains('eCollection Report')}">
                               <li><s:a data-page="eCollection-Report" action='eCollection'>
									<span class="nav_icon">CR</span>
									<span class="menu-text">eCollection Report</span>
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
                            <s:if test="%{#session['USER_PERMISSION'].contains('Net Settled Report')}">
                            <li><s:a data-page="Settled-Consolidated-Report" action='settledConsolidatedReport'>
								<span class="nav_icon">NS</span>
								<span class="menu-text">Net Settled</span>
							</s:a></li>
							</s:if>
            </ul>
            </li>

            </s:if>

            <li>
                <s:if
                    test="%{#session['USER_PERMISSION'].contains('Create Invoice') || #session['USER_PERMISSION'].contains('View Invoice')}">
                    <a style="cursor:pointer" class="head"><span class="nav_icon"><i
                                class="fa fa fa-money"></i></span><span class="menu-text">Quick Pay</span><span
                            class="fa fa-angle-down" id="arrow"></span></a>
                </s:if>
                <ul>
                    <s:if test="%{#session['USER_PERMISSION'].contains('Create Invoice')}">
                        <li>
                            <s:a action="invoice"><span class="nav_icon">IP</span><span class="menu-text">Invoice
                                    Payment </span></s:a>
                        </li>
                        <li>
                            <s:a action="bulkInvoice"><span class="nav_icon">BI</span><span class="menu-text">Bulk
                                    Invoice </span></s:a>
                        </li>
                    </s:if>
                    <s:if test="%{#session['USER_PERMISSION'].contains('View Invoice')}">
                        <li>
                            <s:a action="invoiceSearch"><span class="nav_icon">QPS</span><span class="menu-text">Quick
                                    Payment Search </span></s:a>
                        </li>
                        <li>
                            <s:a action="bulkInvoiceSearch"><span class="nav_icon">BIS</span><span
                                    class="menu-text">Bulk Invoice Search</span></s:a>
                        </li>
                    </s:if>
                    <s:if test="%{#session['USER_SETTINGS'].loadWalletFlag == true}">
                        <li>
                            <s:a data-page="Upload-Balance" action="uploadBalance"><span class="nav_icon">LW</span><span
                                    class="menu-text">Load Wallet</span></s:a>
                        </li>
                    </s:if>
                </ul>
            </li>
            <s:if
                test="%{#session['USER_PERMISSION'].contains('Create Surcharge') || #session['USER_PERMISSION'].contains('View ChargeBack')}">
                <li><a style="cursor:pointer" class="head"><span class="nav_icon"><i
                                class="fa fa-circle-o-notch"></i></span><span class="menu-text">Chargeback
                            Case</span><span class="fa fa-angle-down" id="arrow"></span></a>
                    <ul>
                        <li>
                            <s:a action="viewChargeback"><span class="nav_icon">VC</span>View Chargeback</s:a>
                        </li>
                    </ul>
                </li>
            </s:if>

            <s:if
                test="%{#session['USER_PERMISSION'].contains('Vendor Report') || #session['USER'].retailMerchantFlag == true}">
                <li><a class="head"><span class="nav_icon"><i class="fa fa-bar-chart-o"></i></span><span
                            class="menu-text">Vendor
                            Payout Report</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
                    <ul>
                        <li>
                            <s:a action="khadiProductReport" data-page="Product-Wise-Report">
                                <span class="nav_icon">PWR</span>
                                <span class="menu-text"> Product Wise Report</span>
                            </s:a>
                        </li>
                        <li>
                            <s:a data-page="Vendor-Payout" action='vendorPayoutReportAction'>
                                <span class="nav_icon">VP</span>
                                <span class="menu-text">Vendor Payout Report</span>
                            </s:a>
                        </li>
                    </ul>
                </li>
            </s:if>

            <s:if test="%{#session['USER'].eNachReportFlag == true}">
                <li><a class="head"><span class="nav_icon"><i class="fa fa-bar-chart-o"></i></span><span
                            class="menu-text">Subscription</span> <span class="fa fa-angle-down" id="arrow"></span> </a>
                    <ul>
                        <li>
                            <s:a data-page="eNACH-Report" action='eNachReports'>
                                <span class="nav_icon">ER</span>
                                <span class="menu-text">eNACH Report</span>
                            </s:a>
                        </li>

                        <%-- <li>
                            <s:a data-page="Debit-Report" action='eNachDebitReport'>
                                <span class="nav_icon">EDR</span>
                                <span class="menu-text">eNACH Debit Report</span>
                            </s:a>
                        </li> --%>

                    </ul>
                </li>
            </s:if>
            <s:if
                test="%{#session['USER'].accountVerificationFlag == true || #session['USER'].vpaVerificationFlag == true}">
                <s:if
                    test="%{#session['USER_PERMISSION'].contains('Account Verification') || #session['USER_PERMISSION'].contains('VPA Verification')}">

                    <li><a class="head"><span class="nav_icon"><i class="fa fa-users"></i></span><span
                                class="menu-text">Bene Verification</span>
                            <span class="fa fa-angle-down" id="arrow"></span> </a>
                        <ul>
                            <s:if test="%{#session['USER_PERMISSION'].contains('Account Verification') && #session['USER'].accountVerificationFlag == true}">
                                <li>
                                    <s:a data-page="Account-Verification" action="impsBeneVerification">
                                        <span class="nav_icon">AV</span>
                                        <span class="menu-text">Account Verification</span>
                                    </s:a>
                                </li>
                            </s:if>
                            <s:if test="%{#session['USER_PERMISSION'].contains('VPA Verification') && #session['USER'].vpaVerificationFlag == true}">
                                <li>
                                    <s:a data-page="VPA-Verification" action="vpaValidate">
                                        <span class="nav_icon">VV</span>
                                        <span class="menu-text">VPA Verification</span>
                                    </s:a>
                                </li>
                            </s:if>
                        </ul>
                    </li>
                </s:if>
            </s:if>
            
                <s:if
                    test="%{#session['USER_PERMISSION'].contains('Payout')}">

                    <li><a class="head"><span class="nav_icon"><i class="fa fa-users"></i></span><span
                                class="menu-text">Banking</span>
                            <span class="fa fa-angle-down" id="arrow"></span> </a>
                        <ul>
                                <li><s:a data-page="Single-Account-Transfer" action="singleAccountTransfer">
							<span class="nav_icon">P</span>
							<span class="menu-text">Payout</span>
						   </s:a></li>
                        </ul>
                    </li>
                </s:if>

            <s:if test="%{#session['USER_PERMISSION'].contains('Agent Search')}">
                <li>
                    <a style="cursor: pointer" class="head">
                        <span class="nav_icon"><i class="fa fa-user"></i></span><span class="menu-text"></span>Agent
                        Access<span class="fa fa-angle-down" id="arrow"></span>
                    </a>

                    <ul>
                        <li>
                            <s:a action='agentSearch'><span class="nav_icon">AS</span><span class="menu-text">Agent
                                    Search</span></s:a>
                        </li>
                        <li>
                            <s:a action='complaintRaise' data-page="Complaint-Raise" class="head">
                                <span class="nav_icon">CR</span>
                                <span class="menu-text">Complaint Raise</span>
                            </s:a>
                        </li>
                    </ul>
                </li>
            </s:if>
            <li>
                <a class="head"><span class="nav_icon"><i class="fa fa-user"></i></span><span class="menu-text">My
                        Account</span>
                    <span class="fa fa-angle-down" id="arrow"></span>
                </a>
                <ul>
                    <li>
                        <s:a action='dispatchSlip'><span class="nav_icon">DD</span><span class="menu-text">Dispatch
                                Details</span></s:a>
                    </li>
                    <li>
                        <s:a action="subUserProfile"><span class="nav_icon">MP</span>My Profile </s:a>
                    </li>
                    <li>
                        <s:a action="loginHistoryRedirect"><span class="nav_icon">LH</span>Login History</s:a>
                    </li>
                    <li>
                        <s:a action='passwordChange'><span class="nav_icon">CP</span>Change PIN</s:a>
                    </li>
                </ul>
            </li>
            </ul>
        </div>
    </aside>


    <script src="../js/bootstrap.min.js"></script>
    <script>
            ; (function ($, window, document, undefined) {

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

                $.fn[pluginName] = function (options) {
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
