<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Complaint Raise</title>
    <link rel="icon" href="../image/favicon-32x32.png">
    <script src="../js/jquery.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/daterangepicker.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/bootstrap-select.min.js"></script>
    <link rel="stylesheet" href="../css/subAdmin.css">
    <script src="../js/common-scripts.js"></script>
    <style>

        .merchant__form_group .lable-default{ font-size: 10px; }
        .merchant__form_group .upload-pic:after{ min-width: 28px;height: 28px; }
        .upload-pic{ height: 28px; }
        .upload-pic img{ width: 18px;top: 6px;left: 6px; }
        .upload-text{ padding-left: 40px; }
        .uploaded-text{ padding-left: 40px; }
        .lpay_popup .lpay_popup-inner .lpay_popup-innerbox-success h3{ line-height: 30px; }
        .lpay_popup .lpay_popup-inner .lpay_popup-innerbox{ max-width: 450px !important; }
        .lpay-popup_complaint{ width: 100%;height: 100%;position: fixed;top: 0;left: 0;z-index: 9999;background-color:rgba(0,0,0,.8); }
        .lpay-popup_complaint_inner{ display: flex;align-items: center;justify-content: center;height: 100%; }
        .lpay-popup_complaint_box{ width: 100%;max-width: 1024px;background-color: #fff;border-radius: 8px;padding: 20px; }
        .single-account-input .default-upload h3{ font-size: 16px !important; }
        .lpay-popup_complaint{ display: none; }
        .lpay-popup_complaint_list{ display: flex;flex-direction: column;width: 100%; }
        .complaint_info{ display: flex;align-items: center;justify-content: space-between; }
        .complaint_msg{ padding: 15px;background-color: #f5f5f5;border-radius: 5px;margin-top: 5px;word-break: break-all; }
        td.complaintId{ color: #00f;text-decoration: underline;cursor: pointer;word-break: break-all; }
        .complaint_heading{ font-size: 17px !important; font-weight: 500 !important;margin-bottom: 20px; }
        .req{ color: #f00; }
        .error-field{ display: none; }
        .has-error-complaint .error-field{ position: absolute;top: 0;right: 0;color: #f00;display: block; }
        .complainer_attached{ font-weight: 500;color: #092cd7;cursor: pointer; }
        .status-btn{ display: inline-block;padding: 5px 10px 4px;border-radius: 4px;color: #fff;font-weight: 600; }
        .complete-status{ background-color: #89eb90;color: #0b5411 }
        .initiate-status{ background-color: #f59696;color: #6e0101; }
        .processed-status{ background-color: #fbe1a6;color: #b37d05 }
        #complaintList-table tbody td{ white-space: nowrap; }
        .status-update_div{ display: flex;align-items: center;justify-content: space-between;margin-top: 5px; }
        .common-status{ padding: 5px 15px 3px;border-radius: 4px;font-weight: 600; }

    </style>
</head>
<body>
    
    <s:hidden value="%{#session['USER'].superMerchant}" id="isSuperMerchant"></s:hidden>
    <s:hidden value="%{#session.USER.UserType.name()}" id="userType"></s:hidden>
    <section class="lp-complaint_raise lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Complaint Box</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active">
                        <a href="#" class="lpay-nav-link" onclick="tabShow('lodgeComplaint')" data-id="lodgeComplaint">Complaint Raise</a>
                    </li>
                    <!-- /.lpay-nav-item -->
                    <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" onclick="tabShow('complaintList')" data-id="complaintList">Complaint List</a>
                    </li>
                    <!-- /.lpay-nav-item -->
                </ul>
                <!-- /.lpay_tabs d-flex -->
            </div>
            <!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100" data-target='lodgeComplaint'>
                <div class="row">
                    <div class="col-md-12">
                        <form action="" id="complaintRaiseAction" method="POST" enctype="multipart/form-data">
                            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                                <div class="col-md-6 complaint-input single-account-input common-column mb-20">
                                    <div class="lpay_select_group">
                                        <label for="">Select Merchant</label>
                                        <s:select
                                            name="merchantEmailId"
                                            data-download="merchantPayId"
                                            data-var="merchantEmailId"
                                            class="selectpicker"
                                            id="merchant"
                                            data-submerchant="subMerchant"
                                            data-user="subUser"
                                            headerKey=""
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
                                    <div class="col-md-6 mb-20 complaint-input single-account-input common-column">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant <span class="req">*</span></label>
                                            <s:select 
                                                name="merchantId"
                                                data-required="true"
                                                data-download="merchantPayId" 
                                                class="selectpicker"
                                                id="merchant" 
                                                headerKey="" 
                                                data-var="merchantId" 
                                                data-submerchant="subMerchant" 
                                                data-user="subUser"  
                                                data-live-search="true" 
                                                headerValue="Select Merchant"
                                                list="merchantList" 
                                                listKey="payId" 
                                                listValue="businessName" 
                                                autocomplete="off" 
                                                onchange="removeError(this)"
                                            />
                                            <span class="error-field">Should not be blank</span>
                                        </div>
                                    </div>
                                </s:if>
                                <s:else>
                                    <div class="col-md-6 mb-20 complaint-input single-account-input common-column">
                                        <div class="lpay_select_group ">
                                            <label for="">Select Merchant <span class="req">*</span></label>
                                            <s:select 
                                                name="merchantId" 
                                                data-required="true"
                                                data-download="merchantPayId"
                                                data-var="merchantId"
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
                                            <span class="error-field">Should not be blank</span>
                                        </div>
                                    </div>
                                </s:else>
                            </s:else>
                            <!-- col-md-3 -->
                            <s:if test="%{#session['USER'].superMerchant == true}">
                                <div class="col-md-6 mb-20 complaint-input common-column" data-target="subMerchant">
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant <span class="req">*</span></label>
                                        <s:select
                                            data-id="subMerchant"
                                            data-download="subMerchantPayId" 
                                            data-var="subMerchantPayId" 
                                            data-submerchant="subMerchant" 
                                            data-user="subUser"  
                                            name="subMerchantId" 
                                            class="selectpicker" 
                                            id="subMerchant" 
                                            list="subMerchantList" 
                                            listKey="payId" 
                                            headerValue="ALL"
                                            headerKey="ALL"
                                            listValue="businessName" 
                                            autocomplete="off" 
                                        />
                                        <span class="error-field">Should not be blank</span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            <!-- /.col-md-3 -->	
                            </s:if>
                            <s:else>
                                <div class="col-md-6 mb-20 d-none complaint-input common-column" data-target="subMerchant"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant <span class="req">*</span></label>
                                        <select name="subMerchantId" onchange="removeError(this)" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
                                        <span class="error-field">Should not be blank</span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            </s:else>
                            <!-- /.col-md-3 -->	
                            <div class="col-md-6 complaint-input mb-20 common-column">
                                <div class="lpay_select_group">
                                    <label for="">Complaint Type <span class="req">*</span></label>
                                    <select class="selectpicker" onchange="removeError(this)" data-id="complaintType" data-var="complaintType" name="complaintType" id="complaintType">
                                        <option value="">Select Complaint Type</option>
                                        <option value="Chargebacks">Chargebacks</option>
                                        <option value="Commercials">Commercials</option>
                                        <option value="Reports">Reports</option>
                                        <option value="Settlement">Settlement</option>
                                        <option value="Technical">Technical</option>
                                        <option value="Suggestions">Suggestions</option>
                                    </select>
                                    <span class="error-field">Should not be blank</span>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-3 -->
                            <div class="col-md-6 complaint-input mb-20">
                                <div class="lpay_input_group">
                                    <label for="">Comment <span class="req">*</span></label>
                                    <textarea name="comments" oninput="removeError(this)" data-var="comments" id="comments" cols="30" rows="10" class="lpay_input" style="height: 184px;"></textarea>
                                    <span class="error-field">Should not be blank</span>
                                </div>
                                <!-- /.lpay_input_group -->
                            </div>
                            <!-- /.col-md-4 -->
                            <div class="col-md-6 complaint-input">
                                <label for="" style="font-weight: 400;">Upload File <span class="req">*</span></label>
                                <label for="upload-input-new" class="lpay-upload single-account-input" style="position: relative">
                                    <input type="file" data-var='uploadedFile' onchange="removeError(this)" accept=".png, .jpg, .jpeg, .xls, .xlxs, .csv" data-required="true" name="uploadedFile" multiple id="upload-input-new"
                                        class="lpay_upload_input bulk-invoice">
                                    <div class="default-upload">
                                        <h3>Upload Your PNG/JPG/CSV/XLS/XLSX File</h3>
                                        <img src="../image/image_placeholder.png" class="img-responsive" id="placeholder_img" alt="">
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
                                    <span class="error-field">Should not be blank</span>
                                </label>
                            </div>
                            <!-- /.col-md-3 -->
                            <div class="col-md-12 text-center">
                                
                            </div>
                            <!-- /.col-md-12 -->
                        </form>
                        <div class="col-md-12 text-center">
                            <button onclick="registerComplaintRequest()" class="lpay_button lpay_button-md lpay_button-secondary">Register</button>
                        </div>
                        <!-- /.col-md-12 text-center -->
                    </div>
                    <!-- /.col-md-6 -->
                    <div class="col-md-6">

                    </div>
                    <!-- /.col-md-6 -->
                </div>
                <!-- /.row -->
            </div>
            <!-- /.lpay_tabs_content -->
            <div class="lpay_tabs_content w-100 d-none" data-target='complaintList'>
                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 complaint-input single-account-input common-column mb-20">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant</label>
                            <s:select
                                name="merchantId"
                                data-download="merchantPayId"
                                data-var="merchantId"
                                class="selectpicker"
                                id="merchant-filter"
                                data-submerchant="subMerchant-filter"
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
                                    name="merchantId"
                                    data-required="true"
                                    data-download="merchantPayId" 
                                    class="selectpicker"
                                    id="merchant-filter" 
                                    headerKey="ALL" 
                                    data-var="merchantId" 
                                    data-submerchant="subMerchant-filter" 
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
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_select_group ">
                                <label for="">Select Merchant</label>
                                <s:select 
                                    name="merchantId" 
                                    data-required="true"
                                    data-download="merchantPayId"
                                    data-var="merchantId"
                                    class="selectpicker" 
                                    id="merchant-filter"
                                    list="merchantList" 
                                    data-submerchant="subMerchant-filter" 
                                    listKey="payId"
                                    listValue="businessName" 
                                    autocomplete="off" 
                                    
                                />
                                <span class="error-field"></span>
                            </div>
                        </div>
                    </s:else>
                    <!-- col-md-3 -->
                </s:else>
                <s:if test="%{#session['USER'].superMerchant == true}">
                    <div class="col-md-3 mb-20" data-target="subMerchant">
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <s:select
                                data-id="subMerchant"
                                data-download="subMerchantPayId" 
                                data-var="subMerchantPayId" 
                                data-submerchant="subMerchant-filter" 
                                data-user="subUser"  
                                name="subMerchantId" 
                                class="selectpicker" 
                                id="subMerchant-filter" 
                                list="subMerchantList" 
                                listKey="payId" 
                                headerValue="ALL"
                                listValue="businessName" 
                                autocomplete="off" 
                            />
                            <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                <!-- /.col-md-3 -->	
                </s:if>
                <s:else>
                    <div class="col-md-3 mb-20 d-none" data-target="subMerchant-filter"> 
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <select name="subMerchantId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant-filter" data-user="subUser" id="subMerchant-filter" class=""></select>
                            <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                </s:else>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Complaint Type</label>
                        <select class="selectpicker" data-id="complaintType-fitler" data-var="complaintType" name="complaintType" id="complaintType-fitler">
                            <option value="ALL">ALL</option>
                                 <option value="Chargebacks">Chargebacks</option>
                                 <option value="Commercials">Commercials</option>
                                 <option value="Reports">Reports</option>
                                 <option value="Settlement">Settlement</option>
                                 <option value="Technical">Technical</option>
                                 <option value="Suggestions">Suggestions</option>
                        </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Status</label>
                        <select class="selectpicker" data-id="status-fitler" data-var="status" name="status" id="status-fitler">
                            <option value="ALL">ALL</option>
                            <option value="Open">Open</option>
                            <option value="In-Process">In-Process</option>
                            <option value="Resolved">Resolved</option>
                        </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date From</label>
                        <s:textfield type="text" id="dateFrom" data-var="dateFrom" name="dateTo"
                        class="lpay_input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date To</label>
                        <s:textfield type="text" id="dateTo" data-var="dateTo" name="dateTo"
                        class="lpay_input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-12 text-center mb-10">
                    
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="viewComplaint">Submit</button>
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="complaintList-table" class="display" cellspacing="0" width="100%">
                            <thead class="">
                                <tr class="lpay_table_head">
                                    <th>Complaint ID</th>
                                    <th>Merchant</th>
                                    <th>Complaint Type</th>
                                    <th>Date</th>
                                    <th>Raised By</th>
                                    <th>Updated By</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->
            
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

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

    <div class="lpay-popup_complaint">
        <div class="lpay-popup_complaint_inner">
            <div class="lpay-popup_complaint_box">
                <div class="row">
                    <div class="col-md-6">
                        <div class="lpay-popup_complaint_status">
                            <div class="row">
                                <form action="" method="POST" id="complaintUpdateAction" enctype="multipart/form-data">
                                    <input type="hidden" name="complaintId" id="complaintId-update">
                                    <div class="col-md-12">
                                        <h3 class="complaint_heading">Update Details</h3>
                                    </div>
                                    <!-- /.col-md-12 -->
                                    <div class="col-md-12 mb-20">
                                        <div class="lpay_select_group">
                                            <label for="">Status <span class="req">*</span></label>
                                            <select class="selectpicker" data-id="status" data-var="status" name="status" id="complaintStatus">
                                                <option value="">Select Status</option>
                                                <option value="In-Process">In-Process</option>
                                                <option value="Resolved">Resolved</option>
                                            </select>
                                            <span class="error-field">Should not be blank</span>
                                        </div>
                                        <!-- /.lpay_select_group -->  
                                    </div>
                                    <!-- /.col-md-12 -->
                                    <div class="col-md-6 mb-20">
                                        <div class="lpay_input_group">
                                            <label for="">Comment <span class="req">*</span></label>
                                            <textarea name="comments" data-var="comments" id="comments-update" cols="30" rows="10" class="lpay_input" style="height: 184px;"></textarea>
                                            <span class="error-field">Should not be blank</span>
                                        </div>
                                        <!-- /.lpay_input_group -->
                                    </div>
                                    <!-- /.col-md-4 -->
                                    <div class="col-md-6">
                                        <label for="" style="font-weight: 400;">Upload File</label>
                                        <label for="upload-input-new-update" class="lpay-upload single-account-input">
                                            <input type="file" accept=".png, .jpg, .jpeg, .xls, .xlxs, .csv" data-required="true" name="uploadedFile" multiple id="upload-input-new-update"
                                                class="lpay_upload_input_update bulk-invoice">
                                            <div class="default-upload">
                                                <h3 style="font-size: 14px !important;">Upload Your PNG/JPG/CSV/XLS/XLSX File</h3>
                                                <img src="../image/image_placeholder.png" style="width: 105px" class="img-responsive"
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
                                    </div>
                                    <!-- /.col-md-3 -->
                                </form>
                                <div class="col-md-12 text-center">
                                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancelBtn">Cancel</button>
                                    <button class="lpay_button lpay_button-md lpay_button-primary" id="update-status">Update</button>
                                </div>
                                <!-- /.col-md-12 -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- /.lpay-popup_complaint_status -->
                    </div>
                    <!-- /.col-md-6 -->
                    <div class="col-md-6">  
                        <div class="lpay-popup_complaint_comments">
                            <h3 class="complaint_heading">Comments</h3>
                            <div id="complaint_div">
                                <div class="lpay-popup_complaint_list">
                                    <div class="complaint_info">
                                        <span class="complainer_name">Merchant</span>
                                        <span class="complainer_attached">Download</span>
                                    </div>
                                    <!-- /.complaint_info -->
                                    <div class="complaint_msg">
                                        this is new comment
                                    </div>
                                    <!-- /.complaint_msg -->
                                </div>
                                <!-- /.lpay-popup_complaint_list -->
                            </div>
                            <!-- /#complaint_div -->
                        </div>
                        <!-- /.lpay-popup_complaint_comments -->
                    </div>
                    <!-- /.col-md-6 -->
                </div>
                <!-- /.row -->
            </div>
            <!-- /.lpay-popup_complaint_box -->
        </div>
        <!-- /.lpay-popup_complaint_inner -->
    </div>
    <!-- /.lpay-popup_complaint -->

    <form id="fileDownloadComplaint" action="viewComplaintDetailDownloadAction">
        <input type="hidden" name="complaintId" id="download-complaintId">
        <input type="hidden" name="status" id="download-status">
        
    </form>


    <script src="../js/complaintRaise.js"></script>
</body>
</html>