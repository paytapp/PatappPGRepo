<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
    <html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Account Verification</title>
        <link rel="stylesheet" href="../css/jquery-ui.css">
        <script src="../js/jquery-latest.min.js"></script>
        <link rel="stylesheet" href="../css/common-style.css">
        <!-- <link rel="stylesheet" href="../css/invoice.css" /> -->
        <script src="../js/jquery-ui.js"></script>
        <script src="../js/daterangepicker.js" type="text/javascript"></script>
        <script src="../js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
        <script type="text/javascript" src="../js/pdfmake.js"></script>
        <link rel="stylesheet" href="../css/bootstrap-select.min.css">
        <script src="../js/bootstrap-select.min.js"></script>
        <!-- <script src="../js/user-script.js"></script> -->

        <style>

            button#selectAll {
                position: absolute;
                padding: 5px 10px;
                left: 195px;
            }

            .wrongFileFormat {
                padding: 10px;
                background-color: #feaeae;
                text-align: center;
                border-radius: 5px;
                padding: 17px;
            }

            .lpay_input_group::after, .lpay_select_group::after{
                content: "";
                position: absolute;
                bottom: 1px;
                left: 0;
                width: 100%;
                height: 2px;
                background-color: #f55145;
                transform: scale(0);
                transition: all .5s ease;
            }

            .redLine .lpay_input_group::after, .redLine .lpay_select_group::after{
                transform: scale(1);
            }

            .imp { color: #f00; }
            .error-msg {
                color: #f00;
                position: absolute;
                bottom: -17px;
                left: 0;
            }


        </style>

    </head>
    <div class="edit-permission">
        <s:property value="%{editingpermission}" />
    </div>
    <!-- /.edit-permission -->

    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
        <input type="hidden" id="resellerTrue" value="true">
    </s:if>

    <body class="bodyColor">
        <s:hidden id="setSuperMerchant"></s:hidden>
        <s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
        <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
            <div class="row">
                <div class="col-md-12">
                    <div class="heading_with_icon mb-30">
                        <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Bank Account Verification</h2>
                    </div>
                    <!-- /.heading_icon -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 mb-20">
                    <ul class="lpay_tabs d-flex">
                        <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                            <li class="lpay-nav-item active">
                                <a href="#" class="lpay-nav-link" data-id="impsTransfer">Single Account</a>
                            </li>
                        </s:if>
                        <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                            <li class="lpay-nav-item">
                                <a href="#" class="lpay-nav-link" data-id="bulkAccount">Bulk Account</a>
                            </li>
                        </s:if>
                        <li class="lpay-nav-item">
                            <a href="#" class="lpay-nav-link" data-id="report">Report</a>
                        </li>
                        <li class="lpay-nav-item">
                            <a href="#" class="lpay-nav-link" data-id="verify-link">Verify through link</a>
                        </li>
                    </ul>
                    <!-- /.lpay_tabs -->
                </div>
                <!-- /.col-md-12 -->

                <div class="lpay_tabs_content w-100" data-target="impsTransfer">                    
                    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                        <div class="col-md-4 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant <span class="imp">*</span></label>
                                <s:select
                                    name="merchantPayId"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    data-id="lpay-input"
                                    onchange="removeError(this)"
                                    class="selectpicker lpay-input merchant-class blankInput"
                                    id="merchant"
                                    headerKey=""
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
                        <div class="col-md-4 mb-20 d-none">
                            <div class="lpay_select_group">
                                <label for="">Merchant <span class="imp">*</span></label>
                                <s:select
                                    name="merchantPayId"
                                    onchange="removeError(this)"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    data-id="lpay-input"
                                    data-live-search="true"
                                    class="selectpicker merchant-class lpay-input blankInput"
                                    id="merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
                        </div>
                    </s:else>
                    <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                        <div class="col-md-4 mb-20" data-target="subMerchant">
                            <div class="lpay_select_group">
                                <label for="">Sub Merchant <span class="imp">*</span></label>
                                <s:select
                                    data-id="lpay-input"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    data-var="vendorPayId"
                                    onchange="removeError(this)"
                                    name="subMerchantId"
                                    class="selectpicker"
                                    id="subMerchant"
                                    list="subMerchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    headerKey="ALL"
                                    headerValue="Select Merchant"
                                />
                            </div>
                            <!-- /.lpay_select_group -->
                        </div>
                        <!-- /.col-md-3 -->
                    </s:if>
                    <s:else> 
                        <s:if test="%{#session['USER'].superMerchantId !=null}">
                            <div class="col-md-4 mb-20 d-none" data-target="subMerchant"> 
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant <span class="imp">*</span></label>
                                    <s:select
                                        data-id="lpay-input"
                                        onchange="removeError(this)"
                                        data-var="vendorPayId"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        name="subMerchantId"
                                        class="selectpicker"
                                        id="subMerchant"
                                        list="subMerchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                    />
                                </div>
                                <!-- /.lpay_select_group -->
                            </div>
                            <!-- /.col-md-3 -->
                        </s:if>
                        <s:else>
                            <div class="col-md-4 mb-20 d-none" data-target="subMerchant">
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant <span class="imp">*</span></label>
                                    <select
                                        name="subMerchantId"
                                        onchange="removeError(this)"
                                        data-var="vendorPayId"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        id="subMerchant"
                                        class="">
                                    </select>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-3 -->							
			            </s:else>
                    </s:else>

                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Mobile Number</label>
                            <input type="text" data-id="lpay-input" name="benePhone" maxlength="10"
                                class="lpay_input lpay-input" onkeypress="onlyDigit(event)" id="phoneNo">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Account Holder Name <span class="imp">*</span></label>
                            <input
                                type="text"
                                oninput="removeError(this)" onkeypress="onlyLetters(event)" name="beneName" data-id="lpay-input"
                                class="lpay_input lpay-input blankInput" id="bankAccountName">
                            <span class="default_error d-none">Field should not blank</span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Account Number <span class="imp">*</span></label>
                            <input
                                type="text"
                                oninput="removeError(this)"
                                name="beneAccountNumber"
                                data-id="lpay-input"
                                class="lpay_input lpay-input blankInput"
                                onkeypress="onlyDigit(event)"
                                id="bankAccountNumber">
                            <span class="default_error d-none">Field should not blank</span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Bank IFSC Code <span class="imp">*</span></label>
                            <input
                                type="text"
                                oninput="removeError(this)"
                                name="beneIfsc"
                                onkeypress="lettersAndAlphabet(event)"
                                data-id="lpay-input"
                                class="lpay_input lpay-input blankInput"
                                id="bankIfsc">
                            <span class="default_error d-none">Field should not blank</span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->

                    <div class="col-md-12 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Verify</button>
                    </div>
                    <!-- /.col-md-12 -->
                </div>
                <!-- /.lpay_tabs_content -->
                
                <div class="lpay_tabs_content d-none w-100" data-target="report">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Bank Account Number</label>
                            <input type="text" data-id="" name="searchAccountNumber"
                            class="lpay_input" onkeypress="onlyDigit(event)" id="searchAccountNumber">
                        </div>
                        <!-- /.lpay_select_group -->
                    </div>
                    <!-- /.col-md-3 -->

                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select
                                    name="merchant"
                                    data-id="reportMerchant"
                                    data-submerchant="reportSubMerchant"
                                    data-user="reportSubUser"
                                    data-get="subMerchantReport"
                                    class="selectpicker lpay-input"
                                    id="merchantReportPayId"
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
                    <s:elseif test="%{#session.USER.UserType.name() =='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select
                                    name="merchant"
                                    data-id="reportMerchant"
                                    data-submerchant="reportSubMerchant"
                                    data-user="reportSubUser"
                                    data-get="subMerchantReport"
                                    class="selectpicker lpay-input"
                                    id="merchantReportPayId"
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
                    </s:elseif>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select
                                    name="merchant"
                                    data-submerchant="reportSubMerchant"
                                    data-user="reportSubUser"
                                    data-id="reportMerchant"
                                    class="selectpicker lpay-input"
                                    id="merchantReportPayId"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                                <p class="account-error"></p>
                            </div>
                        </div>
                    </s:else>

                    <!-- /.col-md-3 mb-20 -->

                   
                    <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                        <div class="col-md-4 mb-20" data-target="reportSubMerchant">
                            <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <s:select
                                    data-id="lpay-input"
                                    data-submerchant="reportSubMerchant"
                                    data-user="reportSubUser"
                                    data-var="vendorPayId"
                                    name="subMerchantId"
                                    class="selectpicker"
                                    id="reportSubMerchant"
                                    list="subMerchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    headerKey="ALL"
                                    headerValue="ALL"
                                />
                            </div>
                            <!-- /.lpay_select_group -->
                        </div>
                        <!-- /.col-md-3 -->
                    </s:if>
                    <s:else> 
                        <s:if test="%{#session['USER'].superMerchantId !=null}">
                            <div class="col-md-3 mb-20 d-none" data-target="reportSubMerchant"> 
                                <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <s:select data-id="lpay-input" data-var="vendorPayId" data-submerchant="reportSubMerchant" data-user="reportSubUser"  name="subMerchantId" class="selectpicker" id="reportSubMerchant" list="subMerchantList" listKey="payId"
                                listValue="businessName" autocomplete="off" />
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-3 -->	
                        </s:if>	
                        <s:else>
                            <div class="col-md-3 mb-20 d-none" data-target="reportSubMerchant"> 
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant</label>
                                    <select name="subMerchantId" data-var="vendorPayId" data-submerchant="reportSubMerchant" data-user="reportSubUser" id="reportSubMerchant" class=""></select>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-3 -->							
                        </s:else>	
                    </s:else>

                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Status</label>
                            <select class="selectpicker" data-id="reportStatus" name="status" id="status">
                                <option value="ALL">ALL</option>
                                <option value="Captured">Verified</option>
                                <option value="Other">Failed</option>
                                <option value="Timeout">Pending</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->
                    </div>
                    <!-- /.col-md-3 -->
                   
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Date From</label>
                            <s:textfield type="text" id="dateFrom" data-id="reportDateFrom" name="dateFrom"
                                class="lpay_input datepick" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Date To</label>
                            <s:textfield type="text" id="dateTo" data-id="reportDateTo" name="dateTo"
                                class="lpay_input datepick" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-12 mb-20 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary viewData">View</button>
                        <button class="lpay_button lpay_button-md lpay_button-primary downloadData">Download</button>
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-12 lpay_table_style-2">
                        <div class="lpay_table">
                            <table id="datatable" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>Merchant Name</th>
                                        <th>Transaction ID</th>
                                        <th>Order ID</th>
                                        <th>Bank Account Name</th>
                                        <th>Date</th>
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

                <div class="lpay_tabs_content w-100 d-none" data-target="bulkAccount">
                    <s:form action="impsBulkBeneVerification" method="post" enctype="multipart/form-data">
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                            <div class="col-md-3 mb-20">
                                <div class="lpay_select_group">
                                    <input type="hidden" name="upload-file-name" id="upload-file">
                                    <s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}">
                                    </s:textfield>
                                    <div class="position-relative">
                                        <s:select name="merchantPayId" class="selectpicker bulk-invoice"
                                            id="bulkAccountMerchant" headerKey=""
                                            data-submerchant="bulkAccountSubMerchant" data-user="bulkAccountSubUser"
                                            data-live-search="true" headerValue="Select Any Merchant"
                                            list="merchantList" listKey="payId" listValue="businessName"
                                            autocomplete="off" />
                                        <span id="merchantPayIdErr" class="error"></span>
                                    </div>
                                </div>
                            </div>
                        </s:if>
                        <s:if
                            test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
                            <div class="col-md-3 d-none">
                                <div class="lpay_select_group">
                                    <input type="hidden" name="upload-file-name" id="upload-file">
                                    <s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}">
                                    </s:textfield>
                                    <div class="position-relative">
                                        <s:select data-submerchant="bulkAccountSubMerchant"
                                            data-user="bulkAccountSubUser" name="merchantPayId"
                                            class="selectpicker bulk-invoice" id="bulkAccountMerchant"
                                            list="merchantList" data-live-search="true" listKey="payId"
                                            listValue="businessName" autocomplete="off" />
                                        <span id="merchantPayIdErr" class="error"></span>
                                    </div>
                                </div>
                            </div>
                        </s:if>
                        <!-- /.col-3 -->
                        
                        <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                            <div class="col-md-3 mb-20" data-target="bulkAccountSubMerchant">
                                <div class="lpay_select_group">
                                    <s:select data-id="lpay-input" data-submerchant="bulkAccountSubMerchant" data-user="reportSubUser"
                                        data-var="vendorPayId" name="subMerchantId" class="selectpicker" id="bulkAccountSubMerchant"
                                        list="subMerchantList" listKey="payId" listValue="businessName" autocomplete="off"
                                        headerKey="ALL" headerValue="Select Merchant" />
                                </div>
                                <!-- /.lpay_select_group -->
                            </div>
                            <!-- /.col-md-3 -->
                        </s:if>
                        <s:else> 
                            <s:if test="%{#session['USER'].superMerchantId !=null}">
                                <div class="col-md-3 mb-20 d-none" data-target="bulkAccountSubMerchant"> 
                                    <div class="lpay_select_group">
                                    <!-- <label for="">Sub Merchant</label> -->
                                    <s:select data-id="lpay-input" data-var="vendorPayId" data-submerchant="bulkAccountSubMerchant" data-user="reportSubUser"  name="subMerchantId" class="selectpicker" id="bulkAccountSubMerchant" list="subMerchantList" listKey="payId"
                                    listValue="businessName" autocomplete="off" />
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-3 -->	
                            </s:if>	
                            <s:else>
                            <div class="col-md-3 mb-20 d-none" data-target="bulkAccountSubMerchant"> 
                                <div class="lpay_select_group">
                                <!-- <label for="">Sub Merchant</label> -->
                                <select name="subMerchantId" data-var="vendorPayId" data-submerchant="bulkAccountSubMerchant" data-user="reportSubUser" id="bulkAccountSubMerchant" class=""></select>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-3 -->							
                        </s:else>	
                    </s:else>

                

                        <div class="clearfix"></div>
                        <!-- /.clearfix -->
                        <div class="col-md-6">
                            <label for="upload-input" class="lpay-upload">
                                <input type="file" name="csvFile" accept=".csv" id="upload-input"
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
                                                <span id="fileName"></span>
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
                                                <div id="fileName">File size too Long.</div>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.success-text-box -->
                                    </div>
                                    <!-- /.success-wraper -->
                                </div>
                                <!-- /.upload-success -->
                            </label>
                            <input type="hidden" class="hidden" id="hideFields" name="fileName" />
                            <div class="button-wrapper mt-20 d-flex justify-content-center text-center"
                                style="align-items: center">
                                <button id="bulkSubmit" class="lpay_button lpay_button-md lpay_button-secondary"
                                    disabled>Submit</button>
                                <table id="download-format" class="display nowrap" style="display: none;">
                                    <thead>
                                        <tr>
                                            <th>Bene Name</th>
                                            <th>Bene Account Number</th>
                                            <th>IFSC</th>
                                            <th>Phone Number</th>
                                        </tr>
                                    </thead>
                                </table>
                                <!-- /.download-btn -->

                            </div>
                            <!-- /.button-wrapper -->
                        </div>
                        <!-- /.col-md-4 -->
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
                    </s:form>
                </div>
                <!-- /.lpay_tabs_content -->

                <div class="lpay_tabs_content w-100 d-none" data-target="verify-link">
                    <s:form action="sendVerificationLink" method="POST" autocomplete="off">
                        <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                            <div class="col-md-4 mb-20">
                                <div class="lpay_select_group">
                                    <label for="">Merchant <span class="imp">*</span></label>
                                    <s:select
                                        name="merchantPayId"
                                        data-submerchant="verify-subMerchant"
                                        data-user="subUser"
                                        data-id="lpay-input"
                                        onchange="removeErrorAlt(this);"
                                        class="selectpicker lpay-input merchant-class blankInput"
                                        id="verify-merchant"
                                        headerKey=""
                                        data-live-search="true"
                                        headerValue="Select Merchant"
                                        list="merchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                    />
                                    <span class="error-msg" data-error="verify-merchant"></span>
                                </div>
                            </div>
                        </s:if>
                        <s:elseif test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="col-md-4 mb-20">
                                <div class="lpay_select_group">
                                    <label for="">Merchant <span class="imp">*</span></label>
                                    <s:select
                                        name="merchantPayId"
                                        data-submerchant="verify-subMerchant"
                                        data-user="subUser"
                                        data-id="lpay-input"
                                        onchange="removeErrorAlt(this);"
                                        class="selectpicker lpay-input merchant-class blankInput"
                                        id="verify-merchant"
                                        headerKey=""
                                        data-live-search="true"
                                        headerValue="Select Merchant"
                                        list="merchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                    />
                                    <span class="error-msg" data-error="verify-merchant"></span>
                                </div>
                            </div>
                        </s:elseif>
                        <s:else>
                            <div class="col-md-4 mb-20 d-none">
                                <div class="lpay_select_group">
                                    <label for="">Merchant <span class="imp">*</span></label>
                                    <s:select
                                        name="merchantPayId"
                                        onchange="removeErrorAlt(this);"
                                        data-submerchant="verify-subMerchant"
                                        data-user="subUser"
                                        data-id="lpay-input"
                                        data-live-search="true"
                                        class="selectpicker merchant-class lpay-input blankInput"
                                        id="verify-merchant"
                                        list="merchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                    />
                                    <span class="error-msg" data-error="verify-merchant"></span>
                                </div>
                            </div>
                        </s:else>
                        <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                            <div class="col-md-4 mb-20" data-target="verify-subMerchant">
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant <span class="imp">*</span></label>
                                    <s:select
                                        data-id="lpay-input"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        data-var="vendorPayId"
                                        onchange="removeErrorAlt(this);"
                                        name="subMerchantId"
                                        class="selectpicker"
                                        id="verify-subMerchant"
                                        list="subMerchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                        headerKey="ALL"
                                        headerValue="Select Merchant"
                                    />
                                    <span class="error-msg" data-error="verify-subMerchant"></span>
                                </div>
                                <!-- /.lpay_select_group -->
                            </div>
                            <!-- /.col-md-3 -->
                        </s:if>
                        <s:else> 
                            <s:if test="%{#session['USER'].superMerchantId !=null}">
                                <div class="col-md-4 mb-20 d-none" data-target="verify-subMerchant"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant <span class="imp">*</span></label>
                                        <s:select
                                            data-id="lpay-input"
                                            onchange="removeErrorAlt(this);"
                                            data-var="vendorPayId"
                                            data-submerchant="subMerchant"
                                            data-user="subUser"
                                            name="subMerchantId"
                                            class="selectpicker"
                                            id="verify-subMerchant"
                                            list="subMerchantList"
                                            listKey="payId"
                                            listValue="businessName"
                                            autocomplete="off"
                                        />
                                        <span class="error-msg" data-error="verify-subMerchant"></span>
                                    </div>
                                    <!-- /.lpay_select_group -->
                                </div>
                                <!-- /.col-md-3 -->
                            </s:if>
                            <s:else>
                                <div class="col-md-4 mb-20 d-none" data-target="verify-subMerchant">
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant <span class="imp">*</span></label>
                                        <select
                                            name="subMerchantId"
                                            onchange="removeErrorAlt(this);"
                                            data-var="vendorPayId"
                                            data-submerchant="subMerchant"
                                            data-user="subUser"
                                            id="verify-subMerchant"
                                            class="">
                                        </select>
                                        <span class="error-msg" data-error="verify-subMerchant"></span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-3 -->							
                            </s:else>
                        </s:else>

                        <div class="col-md-4 mb-20">
                            <div class="lpay_input_group">
                                <label for="">Mobile Number</label>
                                <input
                                    type="text"
                                    data-id="lpay-input"
                                    name="mobileNo"
                                    maxlength="10"
                                    class="lpay_input lpay-input"
                                    oninput="onlyDigit(event); removeErrorAlt(this); validateMobileEmailField(this);"
                                    id="verify-phone"
                                    autocomplete="off"
                                />
                                <span class="error-msg" data-error="verify-phone"></span>
                            </div>
                        </div>

                        <div class="col-md-4 mb-20">
                            <div class="lpay_input_group">
                                <label for="">Email Id</label>
                                <input
                                    type="text"
                                    data-id="lpay-input"
                                    name="emailId"
                                    class="lpay_input lpay-input"
                                    id="verify-email"
                                    oninput="validateEmailInput(this); validateMobileEmailField(this);"
                                    autocomplete="off"
                                />
                                <span class="error-msg" data-error="verify-email"></span>
                            </div>
                        </div>

                        <div class="col-md-12 text-center">
                            <button class="lpay_button lpay_button-md lpay_button-secondary mt-15" type="submit" id="btn-verify-link">Send</button>
                        </div>
                    </s:form>
                </div>
            </div>
            <!-- /.row -->
        </section>
        <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
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


        <s:form id="downloadReport" action="downloadBeneVerificationData">
            <s:hidden name="merchantPayId" id="reportMerchant"></s:hidden>
            <s:hidden name="status" id="reportStatus"></s:hidden>
            <s:hidden name="dateFrom" id="reportDateFrom"></s:hidden>
            <s:hidden name="dateTo" id="reportDateTo"></s:hidden>
            <s:hidden name="beneAccountNumber" id="downloadSearchAccountNumber"></s:hidden>
            <s:hidden name="subMerchantId" id="downLoadReportSubMerchant"></s:hidden>
            <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
        </s:form>

    <script src="../js/accountVerification.js"></script>
</body>
</html>