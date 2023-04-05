<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>VPA Verification</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/dataTables.buttons.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<link rel="stylesheet" href="../css/singleAccount.css">
<link rel="stylesheet" href="../css/vpa-style.css">

<style>
    .lpay_tabs_content{
        display: none;
    }
    .lpay_tabs_content.active-block{
        display: block;
    }
</style>

</head>
<!-- /.edit-permission -->
<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
    <input type="hidden" id="resellerTrue" value="true">
</s:if>
<body class="bodyColor">
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Validate VPA</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                        <li class="lpay-nav-item active">
                            <a href="#" class="lpay-nav-link" data-id="single">Single</a>
                        </li>
                    </s:if>

                    <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                        <li class="lpay-nav-item">
                            <a href="#" class="lpay-nav-link" data-id="bulk">Bulk</a>
                        </li>
                    </s:if>
                    
                    
                    <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" data-id="reporting">Reporting</a>
                    </li>

                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100 active-block" data-target="single">
                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant <span class="req">*</span></label>
                            <s:select 
                                name="merchantPayId" 
                                data-download="merchantPayId" 
                                data-var="merchantPayId" 
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
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant <span class="req">*</span></label>
                                <s:select 
                                    name="payId"
                                    data-required="true"
                                    data-download="merchantPayId" 
                                    class="selectpicker"
                                    id="merchant" 
                                    headerKey="" 
                                    data-var="merchantPayId" 
                                    data-submerchant="subMerchant" 
                                    data-user="subUser"  
                                    data-live-search="true" 
                                    headerValue="Select Merchant"
                                    list="merchantList" 
                                    listKey="payId" 
                                    listValue="businessName" 
                                    autocomplete="off"
                                />
                                <span class="error-field"></span>
                            </div>
                        </div>
                    </s:if>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none single-account-input">
                            <div class="lpay_select_group ">
                                <label for="">Select Merchant <span class="req">*</span></label>
                                <s:select 
                                    name="payId" 
                                    data-required="true"
                                    data-download="merchantPayId"
                                    data-var="merchantPayId" 
                                    data-live-search="true" 
                                    class="selectpicker" 
                                    id="merchant"
                                    list="merchantList" 
                                    data-submerchant="subMerchant" 
                                    data-user="subUser"
                                    listKey="payId"
                                    listValue="businessName" 
                                    autocomplete="off" 
                                    onchange="removeError(this)"
                                    
                                />
                                <span class="error-field"></span>
                            </div>
                        </div>
                    </s:else>
                </s:else>
                <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                    <div class="col-md-3 mb-20 single-account-input" data-target="subMerchant">
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant <span class="req">*</span></label>
                            <s:select
                                data-id="subMerchant"
                                data-download="subMerchantPayId" 
                                data-var="subMerchantId" 
                                data-submerchant="subMerchant" 
                                data-user="subUser"  
                                name="subMerchantIdNsds" 
                                class="selectpicker" 
                                id="subMerchant" 
                                headerKey=""
                                data-required="true"
                                list="subMerchantList" 
                                listKey="payId" 
                                headerValue="Select Sub-Merchant"
                                listValue="businessName" 
                                autocomplete="off" 
                                onchange="removeError(this)"
                            />
                            <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                <!-- /.col-md-3 -->	
                </s:if>
                <s:else>
                    <div class="col-md-3 mb-20 d-none single-account-input" data-hide="true" data-target="subMerchant"> 
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant <span class="req">*</span></label>
                            <select name="subMerchantId" data-download="subMerchantPayId" headerValue="ALL" onchange="removeError(this)" data-var="subMerchantId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
                            <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                </s:else>
                <!-- /.col-md-3 -->	
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_input_group">
                        <label for="">Mobile Number</label>
                        <input type="text" data-var="benePhone" onkeydown="onlyDigit(event)" data-regex="[0-9]{10}" maxLength='10' onkeyup="removeError(this)" onchange="checkRegEx(this)" class="lpay_input">
                        <span class="error-field"></span>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_input_group">
                        <label for="">Enter VPA <span class="req">*</span></label>
                        <input type="text" data-required="true" onkeypress="removeError(this)" data-var="beneVpa" class="lpay_input">
                        <span class="error-field"></span>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 input-btn-space">
                    <button class="lpay_button lpay_button-md lpay_button-primary" id="validateVpa">
                        Validate VPA
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                </div>
                <!-- /.col-md-3 -->
            </div>
            <!-- /.lpay_tabs_content -->
            <div class="lpay_tabs_content w-100" data-target="bulk">
                <div class="col-md-6">
                    <div class="row">
                        <s:form action="bulkVpaBeneVerification" id="merchantBulkIMPS" enctype="multipart/form-data" method="POST">
                            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                <div class="col-md-6 mb-20 single-account-input">
                                    <div class="lpay_select_group">
                                        <label for="">Select Merchant</label>
                                        <s:select 
                                            data-required="true"
                                            name="merchantPayId"
                                            data-download="merchantPayIdBulk" 
                                            class="selectpicker"
                                            id="merchantPayIdBulk" 
                                            headerKey="" 
                                            data-var="payIdBulk" 
                                            data-submerchant="subMerchantBulk" 
                                            data-user="subUser"  
                                            data-live-search="true" 
                                            headerValue="Select Merchant"
                                            list="merchantList" 
                                            listKey="payId" 
                                            listValue="businessName" 
                                            autocomplete="off"
                                            onchange="removeError(this)"
                                        />
                                        <span class="error-field"></span>
                                    </div>
                                </div>
                            </s:if>
                            <s:else>
                                <div class="col-md-6 mb-20 d-none single-account-input">
                                    <div class="lpay_select_group">
                                        <label for="">Select Merchant</label>
                                        <s:select 
                                            data-required="true"
                                            name="merchantPayId"
                                            data-download="merchantPayIdBulk" 
                                            class="selectpicker"
                                            id="merchantPayIdBulk"
                                            data-var="payIdBulk" 
                                            data-submerchant="subMerchantBulk" 
                                            data-user="subUser"  
                                            data-live-search="true"
                                            list="merchantList" 
                                            listKey="payId" 
                                            listValue="businessName" 
                                            autocomplete="off"
                                            onchange="removeError(this)"
                                        />
                                        <span class="error-field"></span>
                                    </div>
                                </div>
                            </s:else>
                            <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                                <div class="col-md-6 mb-20 single-account-input" data-target="subMerchantBulk">
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <s:select
                                            data-id="subMerchant"
                                            data-download="subMerchantBulk" 
                                            data-var="subMerchantBulk" 
                                            data-submerchant="subMerchantBulk" 
                                            data-user="subUser"  
                                            name="subMerchantId" 
                                            class="selectpicker" 
                                            id="subMerchantBulk" 
                                            headerKey=""
                                            data-required="true"
                                            list="subMerchantList" 
                                            listKey="payId" 
                                            headerValue="Select Sub-Merchant"
                                            listValue="businessName" 
                                            autocomplete="off" 
                                            onchange="removeError(this)"
                                        />
                                        <span class="error-field"></span>   
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            <!-- /.col-md-3 -->	
                            </s:if>
                            <s:else>
                                <div class="col-md-6 mb-20 d-none single-account-input" data-target="subMerchantBulk"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <select name="subMerchantId" data-download="subMerchantBulk" headerValue="ALL" data-var="subMerchantBulk" data-submerchant="subMerchantBulk" onchange="removeError(this)" data-user="subUser" id="subMerchantBulk" class=""></select>
                                        <span class="error-field"></span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            </s:else>
                            <!-- /.col-md-3 -->
                            <div class="clearfix"></div>
                            <!-- /.clearfix -->
                            <div class="col-md-12">
                                <label for="upload-input-new" class="lpay-upload single-account-input">
                                    <input type="file" accept=".csv" data-var='csvFile' onchange="removeError(this)" data-required="true" name="csvFile" id="upload-input-new"
                                        class="lpay_upload_input bulk-invoice">
                                    <div class="default-upload">
                                        <h3>Upload Your CSV File</h3>
                                        <img src="../image/image_placeholder.png" class="img-responsive"
                                            id="placeholder_img" alt="">
                                    </div>
                                    <!-- /.default-upload -->
                                    <div class="upload-status">
                                        <div class="success-wrapper upload-status-inner d-none">
                                            <div class="success-icon-box status-icon-box">
                                                <img src="../image/tick.png" alt="">
                                            </div>
                                            <div class="success-text-box">
                                                <h3>Upload Successfully</h3>
                                                <div class="fileInfo">
                                                    <span class="fileName"></span>
                                                </div>
                                                <!-- /.fileInfo -->
                                            </div>
                                            <!-- /.success-text-box -->
                                        </div>
                                        <!-- /.success-wraper -->
                                        <div class="error-wrapper upload-status-inner d-none">
                                            <div class="error-icon-box status-icon-box">
                                                <img src="../image/wrong-tick.png" alt="">
                                            </div>
                                            <div class="error-text-box">
                                                <h3>Upload Failed</h3>
                                                <div class="fileInfo">
                                                    <div class="fileName">File size too Long.</div>
                                                </div>
                                                <!-- /.fileInfo -->
                                            </div>
                                            <!-- /.success-text-box -->
                                        </div>
                                        <!-- /.success-wraper -->
                                    </div>
                                    <!-- /.upload-success -->
                                    <span class="error-field"></span>
                                </label>
                                <input type="hidden" class="hidden hideFields" id="hideFields" name="fileName" />
                                <div class="button-wrapper mt-20 d-flex justify-content-center text-center"
                                    style="align-items: center">
                                    <button id="bulkUpdateSubmit" class="lpay_button lpay_button-md lpay_button-secondary">Submit</button>
                                    <table id="download-format" class="display nowrap" style="display: none;">
                                        <thead>
                                            <tr>
                                                <th>VPA</th>
                                                <th>Phone No</th>
                                            </tr>
                                        </thead>
                                    </table>
                                    <!-- /.download-btn -->
                                </div>
                                <!-- /.button-wrapper -->
                            </div>
                            <!-- /.col-md-4 -->
                        </s:form>
                    </div>
                    <!-- /.row -->
                </div>
                <!-- /.col-md-6 --> 
                <div class="col-md-6 d-none" data-id='responseFileFormat'>
                    <div class="wrongFileFormat">
                        Wrong File Format
                    </div>
                    <!-- /.wrongFileFormat -->
                </div>
                <!-- /.col-md-6 -->
                <div class="col-md-6 d-none" data-id='responseFileEmpty'>
                    <div class="wrongFileFormat">
                        Empty File
                    </div>
                    <!-- /.wrongFileFormat -->
                </div>
                <!-- /.col-md-6 -->
                <div class="col-md-6 d-none" data-id="responseTable">
                    <div class="lpay_table_wrapper">
                        <table class="lpay_custom_table" width="100%">
                            <thead class="lpay_table_head text-center">
                                <tr>
                                    <th class="border-bottom-none text-center" colspan="2">Total Records</th>
                                </tr>
                            </thead>
                            <tbody class="">
                                <tr>
                                    <td>Total Row Count</td>
                                    <td>
                                        <s:property value="%{rowCount}" />
                                    </td>
                                </tr>
                                <tr>
                                    <td>Total Success Count</td>
                                    <td>
                                        <s:property value="%{SuccessData}" />
                                    </td>
                                </tr>
                                <tr>
                                    <td>Total Failed Count</td>
                                    <td>
                                        <s:property value="%{failedData}" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <!-- /.payment-options -->
                    </div>
                    <!-- /.lpay_table_wrapper -->
                </div>
                <!-- /.col-md-6 --> 
            </div>
            <!-- /.lpay_tabs_content -->
            <div class="lpay_tabs_content" data-target="reporting">
                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant <span class="req">*</span></label>
                            <s:select 
                                name="merchantEmailId" 
                                data-download="merchantPayId" 
                                data-var="merchantPayId" 
                                class="selectpicker"
                                id="merchantReporting" 
                                data-submerchant="subMerchantReporting"
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
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant <span class="req">*</span></label>
                                <s:select 
                                    name="payId"
                                    data-required="true"
                                    data-download="merchantPayId" 
                                    class="selectpicker"
                                    id="merchantReporting" 
                                    headerKey="ALL" 
                                    data-var="merchantPayId" 
                                    data-submerchant="subMerchantReporting" 
                                    data-user="subUser"  
                                    data-live-search="true" 
                                    headerValue="ALL"
                                    list="merchantList" 
                                    listKey="payId" 
                                    listValue="businessName" 
                                    autocomplete="off"
                                />
                                <span class="error-field"></span>
                            </div>
                        </div>
                    </s:if>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none single-account-input">
                            <div class="lpay_select_group ">
                                <label for="">Select Merchant <span class="req">*</span></label>
                                <s:select 
                                    name="payId" 
                                    data-required="true"
                                    data-download="merchantPayId"
                                    data-var="merchantPayId" 
                                    data-live-search="true" 
                                    class="selectpicker" 
                                    id="merchantReporting"
                                    list="merchantList" 
                                    data-submerchant="subMerchantReporting" 
                                    data-user="subUser"
                                    listKey="payId"
                                    listValue="businessName" 
                                    autocomplete="off" 
                                    onchange="removeError(this)"
                                    
                                />
                                <span class="error-field"></span>
                            </div>
                        </div>
                    </s:else>
                </s:else>
                <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                    <div class="col-md-3 mb-20 single-account-input" data-target="subMerchantReporting">
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant <span class="req">*</span></label>
                            
                            <s:select
                                data-required="true"
                                data-id="subMerchant"
                                data-download="subMerchantPayId" 
                                data-var="subMerchantId" 
                                data-submerchant="subMerchantReporting" 
                                data-user="subUser"  
                                name="subMerchantPayId" 
                                class="selectpicker" 
                                id="subMerchantReporting" 
                                list="subMerchantList" 
                                listKey="payId" 
                                headerValue="ALL"
                                listValue="businessName" 
                                headerKey="ALL"
                                autocomplete="off" 
                                onchange="removeError(this)"
                            />
                            <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                <!-- /.col-md-3 -->	
                </s:if>
                <s:else>
                    <div class="col-md-3 mb-20 d-none single-account-input" data-target="subMerchantReporting"> 
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant <span class="req">*</span></label>
                            <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" onchange="removeError(this)" data-var="subMerchantId" data-submerchant="subMerchantReporting" data-user="subUser" id="subMerchantReporting" class=""></select>
                            <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                </s:else>
                <!-- /.col-md-3 -->	
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_select_group">
                       <label for="">Account Type</label>
                       <select name="accountType" class="selectpicker" id="accountType" data-var="accountType">
                           <option value="ALL">ALL</option>
                           <option value="SAVINGS">Saving</option>
                           <option value="SALARY">Salary</option>
                           <option value="CURRENT ">Current</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_select_group">
                       <label for="">Status</label>
                       <select name="status" class="selectpicker" id="status" data-var="status">
                           <option value="ALL">ALL</option>
                           <option value="Captured">Verified</option>
                           <option value="other">Failed</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_input_group">
                        <label for="">From Date</label>
                        <input type="text" data-var="dateFrom" class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_input_group">
                        <label for="">To Date</label>
                        <input type="text" data-var="dateTo" class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="view">
                        View
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                    <button class="lpay_button lpay_button-md lpay_button-primary" id="download">
                        Download
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="vpaValidateTable" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Merchant</th>
                                    <th>Order ID</th>
                                    <th>Transaction ID</th>
                                    <th>Date</th>
                                    <th>VPA</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content d-none -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

    <div class="lpay_data-popup">
        <div class="lpay_data-container lpay-center">
            <div class="lpay_data-inner">
                <div class="lpay_popup_icon success-icon" data-status="success">
                    <img src="../image/tick.png" alt="">
                </div>
                <!-- /.lpay_data-icon -->
                <div class="lpay_popup_icon invalid-icon" data-status="error">
                    <img src="../image/wrong-tick.png" alt="">
                </div>
                <!-- /.lpay_data-icon -->
                <div class="lpay_data-heading" data-status="success">
                    <span>VPA Verified</span>
                </div>
                <!-- /.lpay_data-heading -->
                <div class="lpay_data-heading" data-status="error">
                    <span>VPA Not Verified</span>
                    <h4 id="responseMsgVpa"></h4>
                </div>
                <!-- /.lpay_data-heading -->
                <div class="lpay_data-box" data-status="success">
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Verified VPA</span>
                        <span data-vpaSuccess="payerAddress"></span>
                    </div>
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Verified Name</span>
                        <span data-vpaSuccess="payerName"></span>
                    </div>
                   
                    <!-- /.lpay_data-li lpay-spaceBetween -->
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Mobile Number</span>
                        <span data-vpaSuccess="phoneNo"></span>
                    </div>
                    <!-- /.lpay_data-li lpay-spaceBetween -->
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Account Type</span>
                        <span data-vpaSuccess="accountType"></span>
                    </div>
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Owner Type</span>
                        <span data-vpaSuccess="ownerType"></span>
                    </div>
                    <!-- /.lpay_data-li lpay-spaceBetween -->
                    <!-- <div class="lpay_data-li lpay-spaceBetween">
                        <span>Owner Type</span>
                        <span>Merchant</span>
                    </div> -->
                    <!-- /.lpay_data-li lpay-spaceBetween -->
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>IFSC Code</span>
                        <span data-vpaSuccess="bankIFSC"></span>
                    </div>
                    <!-- /.lpay_data-li lpay-spaceBetween -->
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Status</span>
                        <span data-vpaSuccess="status"></span>
                    </div>
                    <!-- /.lpay_data-li -->
                </div>
                <!-- /.lpay_data-box -->
                <div class="lpay_data-box" data-status="error">
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Verified VPA</span>
                        <span data-vpa="payerAddress"></span>
                    </div>
                    <div class="lpay_data-li lpay-spaceBetween">
                        <span>Status</span>
                        <span data-vpa="status"></span>
                    </div>
                </div>
                <!-- /.lpay_data-box -->
            </div>
            <!-- /.lpay_data-inner -->
        </div>
        <!-- /.lpay_data-container -->
    </div>
    <!-- /.lpay_data-popup -->

    <s:hidden id="successData" value="%{SuccessData}"></s:hidden>
    <s:hidden id="faledData" value="%{failedData}"></s:hidden>
    <s:hidden id="wrongCsv" value="%{wrongCsv}"></s:hidden>
    <s:hidden id="fileIsEmpty" value="%{fileIsEmpty}"></s:hidden>
    <s:hidden id="rowCount" value="%{rowCount}"></s:hidden>
    <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />

    <div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">

                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Amount has been transferred successfully.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
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
                        <h3 class="responseMsg">Nothing Found Try Again.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
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

    <form action="downloadVpaBeneVerifyData" id="downloadVpa"></form>
    <script src="../js/vpa-script.js"></script>
</body>
</html>