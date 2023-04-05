<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Refund Limit</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/user-script.js"></script>


</head>
<!-- /.edit-permission -->
<body class="bodyColor">
    <s:hidden value="%{#session.USER.UserType.name()}" id="userType"></s:hidden>
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Refund Limit</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active" onclick="tabShow(this, 'refunLimitTrailReport')">
                        <a href="#" class="lpay-nav-link" data-id="refunLimitTrailReport">Refund Limit Report</a>
                    </li>
                    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                        <li class="lpay-nav-item" onclick="tabShow(this, 'refundLimitAvail')">
                            <a href="#" class="lpay-nav-link" data-id="refundLimitAvail">Refund Limit</a>
                        </li>
                    </s:if>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->

            <div class="lpay_tabs_content w-100" data-target='refunLimitTrailReport'>
                <div class="row m-0">
                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                                <s:select
                                    name="merchantEmailId"
                                    data-download="merchantPayId"
                                    data-var="payId"
                                    class="selectpicker"
                                    id="merchant"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    headerKey="ALL"
                                    data-live-search="true"
                                    headerValue="ALL"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
                        </div>
                    </s:if>
                    <s:else>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="col-md-3 mb-20">
                                <div class="lpay_select_group">
                                    <label for="">Select Merchant</label>
                                    <s:select
                                        name="merchantEmailId"
                                        data-download="merchantPayId"
                                        class="selectpicker"
                                        id="merchant"
                                        headerKey="ALL"
                                        data-var="payId"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        data-live-search="true"
                                        headerValue="ALL"
                                        list="merchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                    />
                                </div>
                            </div>
                        </s:if>
                        <s:else>
                            <div class="col-md-3 mb-20 d-none">
                                <div class="lpay_select_group ">
                                    <label for="">Select Merchant</label>
                                    <s:select
                                        name="merchantEmailId"
                                        data-download="merchantPayId"
                                        data-var="payId"
                                        data-live-search="true"
                                        class="selectpicker"
                                        id="merchant"
                                        list="merchantList"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                    />
                                </div>
                            </div>
                        </s:else>
                    </s:else>
                    <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                        <div class="col-md-3 mb-20" data-target="subMerchant">
                            <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <s:select
                                    data-id="subMerchant"
                                    data-download="subMerchantPayId"
                                    data-var="subMerchantId"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    name="subMerchantEmailId"
                                    class="selectpicker"
                                    id="subMerchant"
                                    list="subMerchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    headerKey="ALL"
                                    headerValue="ALL" />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->	
                    </s:if>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
                            <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <select name="subMerchantEmailId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->							
                    </s:else>
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Date From</label>
                            <s:textfield type="text" data-download="dateFrom" data-var="dateFrom" id="dateFrom" name="dateFrom"
                            class="lpay_input" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Date To</label>
                            <s:textfield type="text" data-download="dateTo" data-var="dateTo" id="dateTo" name="dateTo"
                            class="lpay_input" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-12 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary" id="view-button">View</button>
                        <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-12 lpay_table_style-2 mt-10">
                        <div class="lpay_table">
                            <table id="refundLimitTrail-table" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>Merchant</th>
                                        <th>Date</th>
                                        <th>Credit</th>
                                        <th>Debit</th>
                                        <th>Balance</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>	
                            </table>
                        </div>
                        <!-- /.lpay_table -->
                    </div>
                    <!-- /.col-md-12 -->
                </div>
                <!-- /.row -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->
            <div class="lpay_tabs_content w-100 d-none" data-target='refundLimitAvail'>

                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant</label>
                            <s:select
                                name="merchantPayId"
                                data-download="merchantPayId"
                                data-var="merchantPayId"
                                class="selectpicker"
                                id="merchant-limit"
                                data-submerchant="subMerchant-limit"
                                headerKey="ALL"
                                data-live-search="true"
                                headerValue="ALL"
                                list="merchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                            />
                        </div>
                    </div>
                </s:if>
                <s:else>
                    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                                <s:select
                                    name="merchantPayId"
                                    data-download="merchantPayId"
                                    class="selectpicker"
                                    id="merchant-limit"
                                    headerKey=""
                                    data-var="payId"
                                    data-submerchant="subMerchant-limit"
                                    data-live-search="true"
                                    headerValue="Select Merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
                        </div>
                    </s:if>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none">
                            <div class="lpay_select_group ">
                                <label for="">Select Merchant</label>
                                <s:select
                                    name="merchantPayId"
                                    data-download="merchantPayId"
                                    data-var="payId"
                                    data-live-search="true"
                                    class="selectpicker"
                                    id="merchant-limit"
                                    list="merchantList"
                                    data-submerchant="subMerchant-limit"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
                        </div>
                    </s:else>
                </s:else>
                <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                    <div class="col-md-3 mb-20" data-target="subMerchant">
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <s:select
                                data-id="subMerchant"
                                data-download="subMerchantPayId"
                                data-var="subMerchantId"
                                data-submerchant="subMerchant-limit"
                                name="subMerchantPayId"
                                class="selectpicker"
                                id="subMerchant-limit"
                                list="subMerchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                                headerKey="ALL"
                                headerValue="ALL" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->	
                </s:if>
                <s:else>
                    <div class="col-md-3 mb-20 d-none" data-hide='true' data-target="subMerchant-limit"> 
                        <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantId" data-submerchant="subMerchant-limit" data-user="subUser" id="subMerchant-limit" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                </s:else>
                <!-- /.col-md-3 -->	
                <div class="clearfix"></div>
                <!-- /.clearfix -->
                <div class="col-md-3 mb-20">
                    <label for="extraRefundAmount" class="checkbox-label unchecked mb-10 lp-limit_checks">
                        Rolling Limit
                        <input type="checkbox" data-var='extraRefundAmount' id="extraRefundAmount"
                            name="rollingLimit">
                    </label>
                    <div class="lpay_input_group">
                        <input type="text" data-readonly="true" placeholder="0.00" data-var='extraRefundLimit' readonly class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <label for="oneTimeRefundAmount" class="checkbox-label unchecked mb-10 lp-limit_checks">
                        One Time Limit
                        <input type="checkbox" data-var='oneTimeRefundAmount' id="oneTimeRefundAmount"
                            name="oneTimeLimit">
                    </label>
                    <div class="lpay_input_group">
                        <input type="text" data-readonly="true" placeholder="0.00" oninput="remainingLimit()" id="oneTimeRefundLimit" data-var='oneTimeRefundLimit' readonly class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                  <div class="lpay_input_group mt-10">
                    <label for="">Remaining</label>
                    <input type="text" readonly data-readonly="true" placeholder="0.00" data-var='refundLimitRemains' id="remainingLimit" class="lpay_input">
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group mt-10">
                        <label for="">Remarks</label>
                        <input type="text" data-var='remarks' id="remarks" class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-4 -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-primary" id="saveRefundLimit">Submit</button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->

        </div>
        <!-- /.row -->
    </section>

    <div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">
                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h2 class="response-msg">Success</h2>
                        <h3 class="responseMsg">Refund Limit Updated Successfully</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" onclick="resetFields('isSelect')" id="confirmButton1">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h2 class="response-msg">Failed</h2>
                        <h3 class="responseMsg">Nothing Found Try Again.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" onclick="resetFields('isSelect')" id="confirmButton2">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->

    <script src="../js/common-scripts.js"></script>
    <script type="text/javascript" src="../js/refundLimit-script.js"></script>    
</body>
</html>