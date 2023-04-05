<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Bene Verification Report</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/dataTables.buttons.js"></script>
    <script src="../js/jquery.fancybox.min.js"></script>
    <link rel="stylesheet" href="../css/singleAccount.css">
    <style>

        .dlt-button{
            color: #fff;
            width: 30px;
            height: 30px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background-color: #be3535;
            border-radius: 4px;
        }

        .payNow{
            white-space: nowrap;
        }
    </style>
  
</head>
<body>

    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
        <input type="hidden" id="resellerTrue" value="true">
    </s:if>
    <s:if test="%{#session.USER.UserType.name()=='SUBUSER'}">
        <input type="hidden" id="subUserTrue" value="true">
    </s:if>

    <input type="hidden" id="isSubMerchant" value='' />
    <s:hidden value="%{superMerchantFlag}" data-id="superMerchantFlag"></s:hidden>
    <s:hidden value="%{subMerchantFlag}" data-id="subMerchantFlag"></s:hidden>
    <s:if test="%{#session['USER'].superMerchantId != '' && #session['USER'].superMerchantId != null}">
        <input type="hidden" id="subMerchantLogin" value="true">
    </s:if>  
    <s:hidden value="%{#session.USER.UserType.name()}" id="isMerchant" />
    <s:if test="%{#session['USER'].superMerchant == true}">
        <input type="hidden" id="isSuperMerchantTrue">
    </s:if>  
	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Bene Registration</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="horizontal-nav-wrapper mb-20">
                    <nav id="horizontal-nav" class="horizontal-nav">
                    <ul class="horizontal-nav-content nav nav-tabs  list-unstyled font-size-10 merchant-config-tabs border-none mt-0"
                        id="horizontal-nav-content" role="tablist">
                        <s:if test="%{#session.USER.UserType.name() !='RESELLER'}">
                            <li class="nav-item merchant__tab_button active-tab" data-target="addBeneficiary">Add Beneficiary</li>
                        </s:if>
                        <li class="nav-item merchant__tab_button" data-target="reporting">Reporting</li>
                    </ul>
                    </nav>
                </div>
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
        <div class="merchant__forms m-0">
            <div class="merchant__forms_block active-block" data-active="addBeneficiary">
                <div class="row">
                    <div class="col-md-3 mb-20 single-account-input">
                        <label for="impsAccount" class="checkbox-label unchecked">
                            IMPS
                            <s:checkbox name="impsAccount" checked="checked" data-set="imps" id="impsAccount" class="signleAccountFlag" data-id="impsAccount" />
                        </label>
                    </div>
                    <div class="col-md-3 mb-20">
                        <label for="upiAccount" class="checkbox-label unchecked">
                            UPI <s:checkbox name="upiAccount" id="upiAccount" class="signleAccountFlag" data-set="upi" data-id="upiAccount" />
                        </label>
                    </div>
                    <div class="accountInput-div w-100 col-md-12 d-none" style="padding: 0;">
                        <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                            <div class="col-md-3 mb-20 single-account-input">
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
                                            data-var="payId" 
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
                                        <span class="error-field"></span>
                                    </div>
                                </div>
                            </s:if>
                            <s:else>
                                <div class="col-md-3 mb-20 d-none single-account-input">
                                    <div class="lpay_select_group ">
                                        <label for="">Select Merchant</label>
                                        <s:select 
                                            name="payId" 
                                            data-required="true"
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
                                    <label for="">Sub Merchant</label>
                                    <s:select
                                        name="subMerchantPayId"
                                        data-download="subMerchantPayId" 
                                        onchange="removeError(this)"
                                        data-var="subMerchantPayId"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        data-required="true"
                                        id="subMerchant"
                                        class="selectpicker" 
                                        data-id="subMerchant"
                                        list="subMerchantList" 
                                        listKey="payId" 
                                        headerKey=""
                                        headerValue="Select Sub Merchant"
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
                            <div class="col-md-3 mb-20 d-none single-account-input" data-target="subMerchant"> 
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant <span class="req">*</span></label>
                                    <select
                                        name="subMerchantPayId"
                                        data-download="subMerchantPayId"
                                        headerValue="ALL"
                                        onchange="removeError(this)"
                                        data-var="subMerchantPayId"
                                        data-submerchant="subMerchant"
                                        data-user="subUser"
                                        data-required="true"
                                        id="subMerchant"
                                        class="selectpicker">
                                    </select>
                                    <span class="error-field"></span>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                        </s:else>
                        <!-- /.col-md-3 -->							
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Bene A/c Name <span class="req">*</span></label>
                                <input type="text" onpaste="return false;" oninput="removeError(this)" data-required="true" onkeypress="mzOnlyLetters(event)" data-var="beneficiaryName" name="beneficiaryAccountName" data-single="imps" class="lpay_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Bene A/c Number <span class="req">*</span></label>
                                <input type="text" onpaste="return false;" oninput="removeError(this)" data-required="true" onkeypress="mzOnlyNumbers(event)" data-var="beneficiaryAccountNumber" name="bankAccountNumber" data-single="imps" class="lpay_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">IFSC Code <span class="req">*</span></label>
                                <input type="text" onpaste="return false;" data-regex="[A-Za-z0-9]{11}" data-required="true" onkeypress="mzLettersAndAlphabet(event)" onchange="checkRegEx(this)" data-var="ifscCode" name="bankIFSCCode" data-single="imps" oninput="removeError(this)" class="lpay_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">VPA <span class="req">*</span></label>
                                <input type="text" onpaste="return false;" oninput="removeError(this)" data-required="true" name="vpa" data-var='vpa' data-single="upi" class="lpay_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Mobile Number <span class="req">*</span></label>
                                <input type="text" onpaste="return false;" data-regex="[0-9]{10}" maxlength="10" data-required="true" onkeypress="mzOnlyNumbers(event)" name="mobileNumber" oninput="removeError(this)" onchange="checkRegEx(this)" oninput="removeError(this)" data-var="mobileNumber" class="lpay_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Email ID</label>
                                <input type="text" data-required="false" onchange="checkRegEx(this)" oninput="removeError(this)" data-regex="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$" data-var="email" name="remarks" class="lpay_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-12 text-center">
                            <button onclick="payFunction()" class="lpay_button lpay_button-md lpay_button-primary">
                                Verify
                            </button>
                            <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.accountInput-div -->
                </div>
                <!-- /.row -->
            </div>
            <!-- merchant__forms_block -->
            <div class="merchant__forms_block" data-active="reporting">
                <div class="row">
        
                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                                <s:select 
                                    name="payId" 
                                    data-download="payId" 
                                    data-var="payId" 
                                    class="selectpicker"
                                    id="merchantComposite" 
                                    data-submerchant="subMerchantComposite"
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
                            <div class="col-md-3 single-account-input">
                                <div class="lpay_select_group">
                                    <label for="">Select Merchant</label>
                                    <s:select 
                                        name="payId"
                                        data-download="payId" 
                                        class="selectpicker"
                                        id="merchantComposite" 
                                        headerKey="ALL" 
                                        data-var="payId" 
                                        data-submerchant="subMerchantComposite" 
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
                            <div class="col-md-3 d-none single-account-input">
                                <div class="lpay_select_group ">
                                    <label for="">Select Merchant</label>
                                    <s:select 
                                        name="payId" 
                                        data-download="payId"
                                        data-var="payId" 
                                        data-live-search="true" 
                                        class="selectpicker" 
                                        id="merchantComposite"
                                        list="merchantList" 
                                        data-submerchant="subMerchantComposite" 
                                        data-user="subUser"
                                        listKey="payId"
                                        listValue="businessName" 
                                        autocomplete="off" 
                                    />
                                </div>
                            </div>
                        </s:else>
                    </s:else>
                    <s:if test="%{#session['USER'].superMerchant == true}">
                        <div class="col-md-3 single-account-input" data-target="subMerchantComposite">
                            <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <s:select 
                                    data-id="subMerchant"
                                    data-download="subMerchantPayId" 
                                    data-var="subMerchantPayId" 
                                    data-submerchant="subMerchant" 
                                    data-user="subUser"  
                                    name="subMerchantPayId" 
                                    class="selectpicker" 
                                    id="subMerchantComposite" 
                                    list="subMerchantList" 
                                    listKey="payId"
                                    headerKey="ALL" 
                                    headerValue="ALL"
                                    listValue="businessName" 
                                    autocomplete="off" 
                                />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    <!-- /.col-md-3 -->	
                    </s:if>
                    <s:else>
                        <div class="col-md-3 d-none single-account-input" data-target="subMerchantComposite"> 
                            <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchantComposite" data-user="subUser" id="subMerchantComposite" class=""></select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:else>
                    <!-- /.col-md-3 -->	
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                        <label for="">Status</label>
                        <select data-var="status" name="status" class="selectpicker" id="status">
                            <option value="ALL">ALL</option>
                            <option value="SUCCESS">Success</option>
                            <option value="Failed">Fail</option>
                        </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                        <label for="">Channel</label>
                        <select data-var="channel" name="channel" class="selectpicker" id="channel">
                                <option value="ALL">ALL</option>
                                <option value="IMPS">IMPS</option>
                                <option value="UPI">UPI</option>
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
                            <table id="compositeReportTabel" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>Merchant Name</th>
                                        <th>Sub-Merchant</th>
                                        <th>Order Id</th>
                                        <th>Channel</th>
                                        <th>Phone Number</th>
                                        <th>Bene Name/NA</th>
                                        <th>Bank Account Number/VPA</th>
                                        <th>IFSC Code/NA</th>
                                        <th>Status</th>
                                        <th>Action</th>
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
            <!-- merchant__forms_block -->
        </div>
        <!-- merchant__forms -->
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
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" id="confirmButton1">Ok</button>
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
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" id="confirmButton2">Ok</button>
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

    <s:textfield type="hidden" id="isParentSuperMerchant" value="%{superMerchantFlag}" />
    
    <form action="downloadBeneRegistrationReport" id="downloadComposite"></form>
    <script src="../js/common-validations.js"></script>
	<script src="../js/beneVerificationReport.js"></script>
</body>
</html>