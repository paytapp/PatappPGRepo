<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="X-UA-Compatible" content="ie=edge">
        <title>Khadi Product Wise Refund</title>
        <link rel="icon" href="../image/favicon-32x32.png">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <!--  loader scripts -->
        <script src="../js/loader/modernizr-2.6.2.min.js"></script>
        <script src="../js/loader/main.js"></script>
        <link rel="stylesheet" href="../css/loader/normalize.css" />
        <link rel="stylesheet" href="../css/loader/main.css" />
        <link rel="stylesheet" href="../css/loader/customLoader.css" />
        <style>
            :root {
                --color-light-blue-snackbar-success: #b1deb1;
                --color-light-blue-snackbar-success-text: green;
                --color-light-red-snackbar-failure: #fbe9eb;
                --color-light-red-snackbar-failure-text: #e34c5e;
            }
            .bg-snackbar-danger { background-color: var(--color-light-red-snackbar-failure); }
            .text-snackbar-danger { color: var(--color-light-red-snackbar-failure-text); }
            .bg-snackbar-success { background-color: var(--color-light-blue-snackbar-success); }
            .text-snackbar-success { color: var(--color-light-blue-snackbar-success-text); }
            .refund-detail-box {
                width: 100%;
                max-width: 50%;              
            }
            .d-flex { display: flex; }
            .justify-content-center { justify-content: center; }
            .py-20 {
                padding-top: 20px;
                padding-bottom: 20px;
            }
            .full-width { width: 100%; }
            .text-center { text-align: center; }
            .btn-info {
                width: 110px !important;
                font-size: 12px !important;
                text-transform: uppercase;
                padding: 5px !important;
                font-weight: bold !important;
            }
            #loading {
                width: 100%;
                height: 100%;
                top: 0px;
                left: 0px;
                position: fixed;
                display: none;
                z-index: 99;                
            }

            #loading-image {
                position: absolute;
                top: 35%;
                left: 55%;
                z-index: 100;
                width: 10%;
            }

            /* The snackbar - position it at the bottom and in the middle of the screen */
            .snackbar {
                visibility: hidden; /* Hidden by default. Visible on click */
                min-width: 250px; /* Set a default minimum width */
                text-align: center; /* Centered text */
                border-radius: 2px; /* Rounded borders */
                padding: 16px; /* Padding */
                position: fixed; /* Sit on top of the screen */
                z-index: 1; /* Add a z-index if needed */
                left: 50%; /* Center the snackbar */
                top: 50px; /* 30px from the bottom */
                transform: translateX(-50%);
                font-size: 16px;
            }
            
            /* Show the snackbar when clicking on a button (class added with JavaScript) */
            .snackbar.show {
                visibility: visible; /* Show the snackbar */
                /* Add animation: Take 0.5 seconds to fade in and out the snackbar.
                However, delay the fade out process for 2.5 seconds */
                -webkit-animation: fadein 0.5s, fadeout 0.5s 2.5s;
                animation: fadein 0.5s, fadeout 0.5s 2.5s;
                z-index: 99999;
            }
            
            /* Animations to fade the snackbar in and out */
            @-webkit-keyframes fadein {
                from {top: 0; opacity: 0;}
                to {top: 50px; opacity: 1;}
            }
            
            @keyframes fadein {
                from {top: 0; opacity: 0;}
                to {top: 50px; opacity: 1;}
            }
            
            @-webkit-keyframes fadeout {
                from {top: 50px; opacity: 1;}
                to {top: 0; opacity: 0;}
            }
            
            @keyframes fadeout {
                from {top: 50px; opacity: 1;}
                to {top: 0; opacity: 0;}
            }
        </style>
    </head>
    <body>
        <s:hidden id="reg" value = "%{manualRefundProcess.regNumber}"></s:hidden>
        <div class="snackbar bg-snackbar-danger text-snackbar-danger" id="error-snackbar"></div>
        <div class="snackbar bg-snackbar-success text-snackbar-success" id="success-snackbar"></div>
        <s:form id="refundProcess" method="post">
            <s:hidden name="token" value="%{#session.customToken}" />
            <div class="row">
                <div class="col-md-6">
                    <section class="manual-refund lapy_section white-bg box-shadow-box mt-70 p20">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="heading_with_icon mb-30">
                                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                    <h2 class="heading_text">Refund Detail</h2>
                                </div>
                                <!-- /.heading_icon -->
                            </div>
                            <!-- /.col-md-12 -->
                            <div class="col-md-12">
                                <div class="lpay_table_wrapper">
                                    <table class="lpay_custom_table" width="100%">
                                        <tr>
                                            <td>Order ID</td>
                                            <td>
                                                <s:property value="manualRefundProcess.orderId" />
                                                <s:textfield type="hidden" name="orderId" id="orderId" value='%{manualRefundProcess.orderId}' />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Merchant Name</td>
                                            <td>
                                                <s:property value="manualRefundProcess.merchantName" />
                                                <s:textfield type="hidden" name="payId" id="payId" value='%{manualRefundProcess.payId}' />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>PG REF NUM</td>
                                            <td>
                                                <s:property value="manualRefundProcess.pgRefNum" />
                                                <s:textfield type="hidden" name="pgRefNum" id="pgRefNum" value='%{manualRefundProcess.pgRefNum}' />
                                            </td>
                                        </tr>
                                        <s:if test="%{manualRefundProcess.regNumber != null}" >
                                            <tr>
                                                <td>Registration Number</td>
                                                <td>
                                                    <s:property value="manualRefundProcess.regNumber" />
                                                    <s:textfield type="hidden" name="regNumber" id="regNumber" value='%{manualRefundProcess.regNumber}' />
                                                </td>
                                            </tr>
                                        </s:if>
                                        <tr>
                                            <td>Currency</td>
                                            <td>
                                                <s:property value="manualRefundProcess.currencyCode" />
                                                <s:textfield type="hidden" name="currencyCode" id="currencyCode" value='%{manualRefundProcess.currencyCode}' />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Transaction Amount</td>
                                            <td>
                                                <s:property value="manualRefundProcess.amount" />
                                                <s:textfield type="hidden" name="amount" id="amount" value='%{manualRefundProcess.amount}' />
                                                <s:textfield type="hidden" name="objectId" id="objectId" value='%{manualRefundProcess.objectId}' />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Available For Refund</td>
                                            <td>
                                                <s:property value="manualRefundProcess.refundAvailable" />
                                                <s:textfield type="hidden" name="refundAvailable" id="refundAvailable" value='%{manualRefundProcess.refundAvailable}' />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Transaction Type</td>
                                            <td>
                                                <s:property value="manualRefundProcess.txnType" />
                                                <s:textfield type="hidden" name="txnType" id="txnType" value='%{manualRefundProcess.txnType}' />
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                <!-- /.lpay_table_wrapper -->
                            </div>
                            <!-- /.col-md-12 -->
                            <div class="col-md-12 text-center">
                                <button class="lpay_button lpay_button-md- lpay_button-secondary">Refund</button>
                            </div>
                            <!-- /.col-md-12 -->
                        </div>
                        <!-- /.row -->
                    </section>
                    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
                </div>
                <!-- /.col-md-6 -->
            </div>
            <!-- /.row -->
        </s:form>

        <script>
            var error_snackbar = document.getElementById("error-snackbar");
            var success_snackbar = document.getElementById("success-snackbar");

            // SNACKBAR
            function showSnackbar(id) {
                // Get the snackbar DIV
                var x = document.getElementById(id);
            
                // Add the "show" class to DIV
                x.classList.add("show");
            
                // After 3 seconds, remove the show class from DIV
                setTimeout(function(){
                    x.classList.remove("show");
                }, 3000);
            }

            $(document).ready(function() {
                // var _refundAmount = $("#refundAmount");
                // var _refundFlag = $("#refundFlag");
                var _amount = $("#amount");
                var _refundAvailable = $("#refundAvailable");

                // var chargebackAmount = $("#chargebackAmount").val();
                


                $("#refundProcess").on("submit", function(e) {
                    e.preventDefault();                    
                    var result = true;
                    if(result) {
                        $("body").removeClass("loader--inactive");
                        var token = document.getElementsByName("token")[0].value;
                        var _id = document.getElementById.bind(document);
                        
                        var myData = {
                            token : token,
                            "struts.token.name" : "token",
                            "orderId" : _id("orderId").value,
                            "payId" : _id("payId").value,
                            "pgRefNum" : _id("pgRefNum").value,
                            "currencyCode" : _id("currencyCode").value,
                            "amount" : _id("amount").value,
                            "objectId" : _id("objectId").value,
                            "refundAvailable" : _id("refundAvailable").value
                        }
                        if($("#reg").val() != ""){
                            myData.regNumber = _id("regNumber").value;
                        }

                        $.ajax({
                            url : "khadiManualRefundProcessAction",
                            type : "POST",
                            data : myData,
                            success: function(data) {
                                // alert(data.response);
                                if(data.response == "SUCCESS") {
                                    $("body").removeClass("loader--inactive");
                                    
                                    success_snackbar.innerHTML = data.response;
                                    showSnackbar("success-snackbar");

                                    setTimeout(() => {
                                        window.history.back();
                                    }, 3000);
                                } else {
                                    error_snackbar.innerHTML = data.response;
                                    showSnackbar("error-snackbar");
                                    $("body").addClass("loader--inactive");
                                }
                            },
                            error: function(data) {
                                error_snackbar.innerHTML = data.response;
                                showSnackbar("error-snackbar");

                                $("body").addClass("loader--inactive");
                                
                                setTimeout(() => {
                                    window.history.back();
                                }, 3000);
                            }
                        });
                    }
                });
                
                var isRefundFlag = $("#isRefundFlag").val();

                if(isRefundFlag == "NA") {
                    $(".isRefundFlag").css("display", "none");
                }

                

                const decimalCount = number => {
                    // Convert to String
                    const numberAsString = number.toString();
                    // String Contains Decimal
                    if (numberAsString.includes('.')) {
                        return numberAsString.split('.')[1].length;
                    }
                    // String Does Not Contain Decimal
                    return 0;
                }

                
            });
        </script>
    </body>
</html>
        