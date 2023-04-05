<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Payment Options</title>
    <link rel="icon" href="../image/favicon-32x32.png">
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <link rel="stylesheet" href="../css/paymentOptions.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <link rel="stylesheet" href="../css/common-style.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
    
    <script src="../js/jquery.min.js"></script>
    <script src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.fancybox.min.js"></script>

    

</head>
<body>  

    <section class="paymentOption_div set_scroll lpay_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Create / Remove Payment Options</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12" data-alert="response">
                <div class="lpay_response-msg">
                    <span id="responseMsg"></span>
                </div>
                <!-- /.lpay_response-msg -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-4 mb-20 payment-input">
                <div class="lpay_select_group">
                    <label for="">Select Merchant</label>
                    <s:select name="payId" data-rules="ON" data-required="true" class="selectpicker form-control"
                    id="payId" data-var="payId" onchange="removeError(this)" data-submerchant="subMerchant" data-user="subUser"  data-live-search="true"
                    list="merchantList" listKey="payId" headerKey="ALL" headerValue="ALL"
                    listValue="businessName" autocomplete="off" />
                </div>
            </div>
            <!-- col-md-3 -->
            <div class="col-md-4 mb-20 d-none" data-target="subMerchant"> 
                <div class="lpay_select_group">
                   <label for="">Sub Merchant</label>
                   <select name="subMerchantEmailId"  headerValue="ALL" data-var="subMerchantEmailId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->	
            <div class="col-md-4 mb-20 payment-input payment-action">
                <div class="lpay_select_group">
                    <label for="">Select Action</label>
                    <select name="task" data-rules="ON" onchange="removeError(this)" data-var="task" data-required="true" id="task" class="selectpicker form-control" title="Select Task">
                        <option value="">Select Action</option>
                        <!-- <option value="create">Create payment options</option>
                        <option value="remove">Remove payment options</option> -->
                        <option value="activate">Activate payment options</option>
                        <option value="deactivate">Deactivate payment options</option>
                    </select>
                </div>
                <!-- /.lpay_select_group form-group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-12" id="mopTypeDiv">

            </div>
            <!-- /.col-md-12 -->
            <div class="lp-payment_options_wrapper">
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-mop="creditCard" data-id="creditCard">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="creditCard">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-cc" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Credit Card</span> 
                                    <div class="selected_moptype font-weight-medium">
                                        <span>Nothing Selected</span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <div class="lp-payment_options_arrow font-size-18">
                                <i class="fa fa-angle-down" aria-hidden="true"></i>
                            </div>
                            <!-- /.lp-payment_options_arrow -->
                            <input type="checkbox" data-check="Credit Card" value='CC' id="creditCard">
                            <!-- <s:checkbox name="creditCard" data-check="Credit Card" fieldValue="CC" id="creditCard"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                    <div class="lp-payment_options_mopType_div" data-set="Credit Card" data-target="creditCard">
                        <div class="lp-payment_options_mopType" data-append="creditCard">

                        </div>
                        <!-- /.lp-payment_options_mopType_div -->
                    </div>
                    <!-- /.lp-payment_options_mopType_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-mop="debitCard" data-id="debitCard">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="debitCard">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card-alt" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Debit Card</span> 
                                    <div class="selected_moptype font-weight-medium">
                                        <span>Nothing Selected</span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <div class="lp-payment_options_arrow font-size-18">
                                <i class="fa fa-angle-down" aria-hidden="true"></i>
                            </div>
                            <!-- /.lp-payment_options_arrow -->
                            <input type="checkbox" data-check="Debit Card" value='DC' id="debitCard">
                            <!-- <s:checkbox name="debitCard" data-check="Credit Card" fieldValue="CC" id="debitCard"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                    <div class="lp-payment_options_mopType_div" data-set="Debit Card" data-target="debitCard">
                        <div class="lp-payment_options_mopType" data-append='debitCard'>

                        </div>
                        <!-- /.lp-payment_options_mopType_div -->
                    </div>
                    <!-- /.lp-payment_options_mopType_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-mop="netBanking" data-id="netBanking">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="netBanking">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-inr" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Net Banking</span> 
                                    <div class="selected_moptype font-weight-medium">
                                        <span>Nothing Selected</span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <div class="lp-payment_options_arrow font-size-18">
                                <i class="fa fa-angle-down" aria-hidden="true"></i>
                            </div>
                            <!-- /.lp-payment_options_arrow -->
                            <input type="checkbox" data-check="Net Banking" value='NB' id="netBanking">
                            <!-- <s:checkbox name="debitCard" data-check="Credit Card" fieldValue="CC" id="debitCard"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                    <div class="lp-payment_options_mopType_div" data-set="Net Banking" data-target="netBanking">
                        <div class="lp-payment_options_mopType" data-append='netBanking'>

                        </div>
                        <!-- /.lp-payment_options_mopType_div -->
                    </div>
                    <!-- /.lp-payment_options_mopType_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-mop="wallet" data-id="wallet">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="wallet">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-google-wallet" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Wallet</span> 
                                    <div class="selected_moptype font-weight-medium">
                                        <span>Nothing Selected</span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <div class="lp-payment_options_arrow font-size-18">
                                <i class="fa fa-angle-down" aria-hidden="true"></i>
                            </div>
                            <!-- /.lp-payment_options_arrow -->
                            <input type="checkbox" data-check="Wallet" value='WL' id="wallet">
                            <!-- <s:checkbox name="wallet" data-check="Credit Card" fieldValue="CC" id="wallet"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                    <div class="lp-payment_options_mopType_div" data-set="Wallet" data-target="wallet">
                        <div class="lp-payment_options_mopType" data-append='wallet'>

                        </div>
                        <!-- /.lp-payment_options_mopType_div -->
                    </div>
                    <!-- /.lp-payment_options_mopType_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-mop="upi" data-id="upi">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="upi">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">UPI</span> 
                                    <div class="selected_moptype font-weight-medium">
                                        <span>Nothing Selected</span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <div class="lp-payment_options_arrow font-size-18">
                                <i class="fa fa-angle-down" aria-hidden="true"></i>
                            </div>
                            <!-- /.lp-payment_options_arrow -->
                            <input type="checkbox" data-check="UPI" value='UPI' id="upi">
                            <!-- <s:checkbox name="upi" data-check="Credit Card" fieldValue="CC" id="upi"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                    <div class="lp-payment_options_mopType_div" data-set="UPI" data-target="upi">
                        <div class="lp-payment_options_mopType"  data-append='upi'>

                        </div>
                        <!-- /.lp-payment_options_mopType_div -->
                    </div>
                    <!-- /.lp-payment_options_mopType_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="emi">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="emi">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">EMI</span> 
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="EMI" value='EMI' id="emi">
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="recurringPayment">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="recurringPayment">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Recurring Payment</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="Recurring Payment" value='RC' id="recurringPayment">
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="expressPay">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="expressPay">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Express Pay</span>
                                    <!-- /.selected_moptype -->
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="Express Pay" value='EP' id="expressPay">
                            <!-- <s:checkbox name="recurringPayment" data-check="Credit Card" fieldValue="CC" id="recurringPayment"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="upiQr">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="upiQr">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">UPI QR</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="Express Pay" value='UPIQR' id="upiQr">
                            <!-- <s:checkbox name="recurringPayment" data-check="Credit Card" fieldValue="CC" id="recurringPayment"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="mqr">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="mqr">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>

                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">MQR</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                </div>
                            </div>
                            <input type="checkbox" data-check="Express Pay" value='MQR' id="mqr">
                        </label>
                    </div>
                </div>
                
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="prepaidCard">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="prepaidCard">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Prepaid Card</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="Prepaid Card" value='prepaidCard' id="prepaidCard">
                            <!-- <s:checkbox name="recurringPayment" data-check="Credit Card" fieldValue="CC" id="recurringPayment"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="debitCardWithPin">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="debitCardWithPin">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Debit Card With PIN</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="debitCardWithPin" value='DCWP' id="debitCardWithPin">
                            <!-- <s:checkbox name="recurringPayment" data-check="Credit Card" fieldValue="CC" id="recurringPayment"></s:checkbox> -->
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="cashOnDelivery">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="cashOnDelivery">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-money" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Cash on Delivery</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="cashOnDelivery" value='COD' id="cashOnDelivery">
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="international">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="international">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-credit-card" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">International</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="international" value='international' id="international">
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="crypto">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="crypto">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-money" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">Crypto</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="crypto" value='CR' id="crypto">
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 lp-payment_options_main">
                    <div class="lp-payment_options_div" data-id="aamarPay">
                        <label class="lp-payment_options_lable_div d-flex-space-between" for="aamarPay">
                            <div class="lp-payment_options_contain d-flex-space-between">
                                <div class="lp-payment_options_icon d-flex-center">
                                    <i class="fa fa-money" aria-hidden="true"></i>
                                </div>
                                <!-- /.lp-payment_options_icon -->
                                <div class="lp-payment_options_lable font-weight-light">
                                    <span class="font-size-16">AAMARPAY</span>
                                    <div class="selected_moptype font-weight-medium">
                                        <span></span>
                                    </div>
                                    <!-- /.selected_moptype -->
                                </div>
                                <!-- /.lp-payment_options_lable -->
                            </div>
                            <!-- /.lp-payment_options_contain -->
                            <input type="checkbox" data-check="aamarPay" value='AAMARPAY' id="aamarPay">
                        </label>
                        <!-- /.lp-payment_options_lable_div -->
                    </div>
                    <!-- /.lp-payment_options_div -->
                </div>
                <!-- /.col-md-4 -->
            </div>
            <!-- /.lp-payment_options -->
            <div class="col-md-12 permission-checkbox options-parent">
                <div class="col-md-12 text-center">
                    <button type="submit" id="btn-payment-options" class="lpay_button lpay_button-md lpay_button-secondary">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.paymentOption_div lpay_section box-shadow-box mt-70 p20 -->



    <div class="lpay_popup_confirm"  id="fancybox">
        <div class="lpay_popup_confirm_box text-center">
            <div class="lpay_popup_box_icon">
                <span class="lpay_popup_icon">!</span>
            </div>
            <!-- /.confirm-box-icon -->
            <div class="lpay_confirm_delete_text">
                <h3>Are you sure ?</h3>
                <span>Do you really want to delete these records? This process cannot be undone.</span>
            </div>
            <!-- /.confirm-delete-text -->
            <div class="confirm-delete-button">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn">Cancel</button>
                <button  class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">Delete</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
    <!-- /.confrim-popup -->


    <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    
    <script src="../js/paymentOptions.js"></script>


</body>
</html>