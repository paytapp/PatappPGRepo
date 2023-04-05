<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Merchant Payouts</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/dataTables.buttons.js"></script>
    <link rel="stylesheet" href="../css/singleAccount.css">
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <script src="../js/highcharts.js"></script>
	<script src="../js/exporting.js"></script>
	<script src="../js/export-data.js"></script>
    <link rel="stylesheet" href="../css/alertBoxes-styles.css">
    <script src="../js/wordToAmountPaise.js"></script>

    <style>

        .download-failed, .download-failed-duplicate{ 
            color: #6773ff;
            font-weight: bold;
            cursor: pointer;
         }

         .updateStatus{
             white-space: nowrap;
         }

        .empty-graph{

            width: 100%;
            height: 100%;
            position: absolute;
            top: 0;
            left: 0;
            display: none;
            align-items: center;
            justify-content: center;
            font-size: 20px;

        }

        .amount-show{ position: absolute;font-weight: 500; }

        .payNow{
            white-space: nowrap;
        }
        span.label-heading h3 {
            margin-left: 15px;
            margin-top: 20px;
            font-size: 14px !important;
            font-weight: 600;
        }
        .responseMsgDiv h2 {
            font-size: 14px;
            margin-left: 15px;
            margin-top: 15px;
            color: #f00;
        }

        [data-condition='amount']{
            margin-top: 15px;
        }

        [data-condition='amount'] .amount-left{
            display: none;

        }

        .update-status{ position: absolute !important;right: 0;top: -30px; }
        .lpay_popup-innerbox-success .response-msg{ color: #b1e096; }
        .response-msg{ font-size: 20px;margin-bottom: 10px;font-weight: 500; }
        .lpay_popup-innerbox-error .response-msg{ color: #f9958e; }

        .show-amount[data-condition='amount'] .amount-left{
            display: inline-flex;
            flex-direction: column;
        }

        .show-amount[data-condition='amount'] .amount-wrapper{
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .totalAmount{
            font-size: 16px;
            line-height: 1;
            font-weight: 600;
        }

        .dt-buttons { display: flex; }
        .dt-buttons a {
            padding-left: 10px;
            padding-right: 10px;
        }
    </style>
</head>
<body>
    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
        <input type="hidden" id="resellerTrue" value="true">
    </s:if>

    <s:if test="%{​​​​​​​​#session.USER.UserType.name() == 'SUBUSER'}​​​​​​​​">
        <input type="hidden" id="subUserTrue" value="true">
        <s:textfield type="text" id="userType" value="%{​​​​​​​​#session.USER.UserType.name()}" />
    </s:if>

    <s:hidden id="userTypeLogin" value="%{#session.USER.UserType.name()=='MERCHANT'}" />
    
    <s:hidden id="user" value="%{#session.USER.UserType.name()}"></s:hidden>
    <s:hidden id="allowPayoutUpdateStatus" value="%{#session.USER_SETTINGS.allowPayoutUpdateStatus}"></s:hidden>

    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
        <input type="hidden" id="user_type">
    </s:if>

    <s:if test="%{#session['USER'].superMerchant == true}">
        <input type="hidden" id="isSuperMerchantTrue">
    </s:if>

    <s:textfield type="hidden" id="isParentSuperMerchant" value="%{superMerchantFlag}" />
    <s:textfield type="hidden" id="isSuperMerchant" value="%{#session['USER'].superMerchant}" />
    <!-- <s:property value='%{topupFlag}'></s:property> -->
    <s:hidden value='%{#session["USER_SETTINGS"].topupFlag}' id="topupFlag"></s:hidden>
    <s:hidden value="%{#session['USER_SETTINGS'].statementFlag}" id="statementFlag"></s:hidden>
	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Merchant Payouts</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="horizontal-nav-wrapper mb-20">
                    <nav id="horizontal-nav" class="horizontal-nav">
                        <ul class="horizontal-nav-content nav nav-tabs list-unstyled font-size-10 merchant-config-tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
                            <s:if test="%{#session['USER'].superMerchant != true && #session.USER.UserType.name() !='RESELLER' && superMerchantFlag != true}">
                                <li class="nav-item merchant__tab_button active-tab" data-target="singleAccount">Single Account</li>
                                <li class="nav-item merchant__tab_button" data-target="bulkAccount">Bulk Account</li>
                            </s:if>
                            <li class="nav-item merchant__tab_button" data-target="compositeReporting">Reporting</li>
                            <li class="nav-item merchant__tab_button" data-target="accountLedger">Account Ledger</li>
                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <li class="nav-item merchant__tab_button" data-target="topup">Topup</li>
                            </s:if>
                            <s:else>
                                <s:if test='%{#session.USER_SETTINGS.topupFlag == true}'>
                                    <li class="nav-item merchant__tab_button" data-target="topup">Topup</li>
                                </s:if>
                            </s:else>
                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <li class="nav-item merchant__tab_button" data-target="accountStatement">Account Statement</li>
                            </s:if>
                            <s:else>
                                <s:if test='%{#session.USER_SETTINGS.statementFlag == true}'>
                                    <li class="nav-item merchant__tab_button" data-target="accountStatement">Account Statement</li>
                                </s:if>
                            </s:else>
                        </ul>
                    </nav>
                </div>
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
        <div class="merchant__forms m-0">
            <div class="merchant__forms_block active-block" data-active="singleAccount">
                <div class="row">
                    <div class="col-md-3 mb-20 single-account-input">
                        <label for="impsAccount" class="checkbox-label unchecked">
                            IMPS
                            <s:checkbox name="impsAccount" checked="checked" data-set="imps" data-checked="imps" id="impsAccount" class="signleAccountFlag" data-id="impsAccount" />
                        </label>
                    </div>
                    <div class="col-md-3 mb-20">
                        <label for="neftAccount" class="checkbox-label unchecked">
                            NEFT <s:checkbox name="neft"
                                id="neftAccount" class="signleAccountFlag" data-set="imps" data-checked="neft"  data-id="neftAccount" />
                        </label>
                    </div>
                    <div class="col-md-3 mb-20">
                        <label for="rtgsAccount" class="checkbox-label unchecked">
                            RTGS
                            <s:checkbox name="rtgsAccount" checked="checked" data-set="imps" data-checked="rtgs" id="rtgsAccount" class="signleAccountFlag" data-id="rtgsAccount" />
                        </label>
                    </div>
                    <div class="col-md-3 mb-20">
                        <label for="upiAccount" class="checkbox-label unchecked">
                            UPI <s:checkbox name="upiAccount"
                                id="upiAccount" class="signleAccountFlag" data-set="upi" data-checked="upi" data-id="upiAccount" />
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
                                        listKey="emailId"
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
                        <s:if test="%{#session['USER'].superMerchant == true}">
                            <div class="col-md-3 mb-20 single-account-input" data-target="subMerchant">
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
                                        id="subMerchant" 
                                        list="subMerchantList" 
                                        listKey="emailId" 
                                        headerValue="ALL"
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
                            <div class="col-md-3 mb-20 d-none single-account-input" data-target="subMerchant"> 
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant</label>
                                    <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" onchange="removeError(this)" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
                                    <span class="error-field"></span>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                        </s:else>
                        <!-- /.col-md-3 -->							
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Bene A/c Name <span class="req">*</span></label>
                                <input type="text" onchange="checkRegEx(this)" data-regex="[A-Za-z]$" oninput="removeError(this)" data-required="true" onkeypress="mzOnlyLetters(event)" data-var="beneficiaryAccountName" name="beneficiaryAccountName" data-single="imps" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Bene A/c Number <span class="req">*</span></label>
                                <input type="text" onchange="checkRegEx(this)" data-regex="[A-Za-z0-9]$" oninput="removeError(this)" data-required="true" onkeypress="mzLettersAndAlphabet(event)" data-var="bankAccountNumber" name="bankAccountNumber" data-single="imps" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">IFSC Code <span class="req">*</span></label>
                                <input type="text"  data-regex="[A-Za-z0-9]{11}$" data-required="true" onkeypress="mzLettersAndAlphabet(event)" onchange="checkRegEx(this)" id="ifsc" data-var="bankIFSCCode" name="bankIFSCCode" data-single="imps" oninput="removeError(this)" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Bank Name</label>
                                <input type="text" oninput="removeError(this)" data-required="false" onkeypress="mzOnlyLetters(event)" data-var="bankAccountName" name="bankAccountName" data-single="imps" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">VPA <span class="req">*</span></label>
                                <input type="text" data-regex="^[\w\.\-_]{3,}@[a-zA-Z]{3,}" oninput="removeError(this)" onchange="checkRegEx(this)" data-required="true" name="vpa" data-var='vpa' data-single="upi" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-div single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Payee Name <span class="req">*</span></label>
                                <input type="text" oninput="removeError(this)" data-required="true" onkeypress="mzOnlyLetters(event)" name="payeeName" data-var='payeeName' data-single="upi" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Mobile Number <span class="req">*</span></label>
                                <input type="text" data-regex="[0-9]{10}" maxlength="10" data-required="true" onkeypress="mzOnlyNumbers(event)" name="mobileNumber" oninput="removeError(this)" onchange="checkRegEx(this)" oninput="removeError(this)" data-var="mobileNumber" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Amount <span class="req">*</span></label>
                                <input type="text" onpaste="return false;" data-required="true" oninput="amount(event, this);removeError(this);RsPaise(Math.round(this.value*100)/100, 'single-amountShow');" onblur="roundOf(this)" onkeypress="mzDigitDot(event)" data-var="amount" name="amount" class="lpay_input payout_input">
                                <span class="error-field"></span>
                                <div id="single-amountShow" class="amount-show"></div>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
						<div class="col-md-3 mb-20 single-account-input">
							<div class="lpay_select_group">
								<label for="">Purpose <span class="req">*</span></label>
								<s:select headerKey="" data-required="true" headerValue="Select Purpose" class="selectpicker"
									list="@com.paymentgateway.commons.util.PayoutPupose@values()"
									listValue="name" listKey="name" name="purpose"
									id="purpose" data-var="purpose" onchange="removeError(this)" autocomplete="off" value="" />
                                    <span class="error-field"></span>
							</div>
							<!-- /.lpay_select_group -->
						</div>
						<div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_input_group">
                                <label for="">Remark</label>
                                <input type="text" data-required="false" data-var="remarks" name="remarks" class="lpay_input payout_input">
                                <span class="error-field"></span>
                            </div>
                          <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-12 text-center">
                            <button onclick="payFunction()" class="lpay_button lpay_button-md lpay_button-primary">
                                Pay Now
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
            <div class="merchant__forms_block" data-active="bulkAccount">
                <div class="row">
                    <div class="col-md-3 transaction-type">
                        <div class="row">
                            <div class="col-md-12 mb-20">
                                <label for="impsAccountBulk" class="checkbox-label unchecked">
                                    IMPS
                                    <s:checkbox name="impsAccountBulk" data-bulk="impsBulk" id="impsAccountBulk" class="signleAccountFlagBulk" data-id="impsAccountBulk" />
                                </label>
                            </div>
                            <!-- <div class="col-md-12">
                                <h5 class="mb-20">IMPS Bulk Upload</h5>
                            </div> -->
                            <!-- /.col-md-12 -->
                            <s:form action="getMerchantInitiatedBulkRequest" id="merchantBulkIMPS" enctype="multipart/form-data" method="POST">
                                <input type="hidden" name="txnType" value="IMPS">
                                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                    <div class="col-md-12 mb-20 single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
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
                                    <div class="col-md-12 mb-20 d-none single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
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
                                <s:if test="%{#session['USER'].superMerchant == true}">
                                    <div class="col-md-12 mb-20 single-account-input" data-target="subMerchantBulk">
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <s:select 
                                                data-id="subMerchantBulk"
                                                data-download="subMerchantBulk" 
                                                data-var="subMerchantBulk" 
                                                data-submerchant="subMerchantBulk" 
                                                data-user="subUser"  
                                                name="subMerchantPayId" 
                                                class="selectpicker" 
                                                id="subMerchantBulk" 
                                                list="subMerchantList" 
                                                listKey="emailId" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input" data-target="subMerchantBulk"> 
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <select name="subMerchantPayId" data-download="subMerchantBulk" headerValue="ALL" data-var="subMerchantBulk" data-submerchant="subMerchantBulk" onchange="removeError(this)" data-user="subUser" id="subMerchantBulk" class=""></select>
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
                                        <input type="file" data-var='csvFile' onchange="removeError(this)" data-required="true" name="csvFile" id="upload-input-new"
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
                                        <button id="bulkSubmit" class="lpay_button lpay_button-md lpay_button-secondary" onclick="submitBulkPayoutRequest(this)">Submit</button>
                                        <table id="download-format" class="display nowrap" style="display: none;">
                                            <thead>
                                                <tr>
                                                    <th>Order ID</th>
                                                    <th>Bene Account Name</th>
                                                    <th>Bank Account Number</th>
                                                    <th>IFSC</th>
                                                    <th>Bank Name</th>
                                                    <th>Phone Number</th>
                                                    <th>Amount</th>
                                                    <th>Purpose</th>
                                                    <th>Remark</th>
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
                    <div class="col-md-3 transaction-type">
                        <div class="row">
                            <div class="col-md-12 mb-20">
                                <label for="neftAccountBulk" class="checkbox-label unchecked">
                                    NEFT
                                    <s:checkbox name="neftAccountBulk" data-bulk="neftBulk" id="neftAccountBulk" class="signleAccountFlagBulk" data-id="neftAccountBulk" />
                                </label>
                            </div>
                            <!-- <div class="col-md-12">
                                <h5 class="mb-20">IMPS Bulk Upload</h5>
                            </div> -->
                            <!-- /.col-md-12 -->
                            <s:form action="getMerchantInitiatedBulkRequest" id="merchantBulkNEFT" enctype="multipart/form-data" method="POST">
                                <input type="hidden" name="txnType" value="NEFT">
                                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                    <div class="col-md-12 mb-20 single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
                                                data-download="merchantPayIdBulk" 
                                                class="selectpicker"
                                                id="merchantPayIdBulkNeft" 
                                                headerKey="" 
                                                data-var="payIdBulk" 
                                                data-submerchant="subMerchantBulkNeft" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
                                                data-download="merchantPayIdBulk" 
                                                class="selectpicker"
                                                id="merchantPayIdBulkNeft"
                                                data-var="payIdBulk" 
                                                data-submerchant="subMerchantBulkNeft" 
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
                                <s:if test="%{#session['USER'].superMerchant == true}">
                                    <div class="col-md-12 mb-20 single-account-input" data-target="subMerchantBulkNeft">
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <s:select 
                                                data-id="subMerchantBulkNeft"
                                                data-download="subMerchantBulk" 
                                                data-var="subMerchantBulk" 
                                                data-submerchant="subMerchantBulkNeft" 
                                                data-user="subUser"  
                                                name="subMerchantPayId" 
                                                class="selectpicker" 
                                                id="subMerchantBulkNeft" 
                                                list="subMerchantList" 
                                                listKey="emailId" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input" data-target="subMerchantBulkNeft"> 
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <select name="subMerchantPayId" data-download="subMerchantBulkNeft" headerValue="ALL" data-var="subMerchantBulk" data-submerchant="subMerchantBulk" onchange="removeError(this)" data-user="subUser" id="subMerchantBulkNeft" class=""></select>
                                            <span class="error-field"></span>
                                        </div>
                                        <!-- /.lpay_select_group -->  
                                    </div>
                                </s:else>
                                <!-- /.col-md-3 -->
                                <div class="clearfix"></div>
                                <!-- /.clearfix -->
                                <div class="col-md-12">
                                    <label for="upload-input-neft" class="lpay-upload single-account-input">
                                        <input type="file" data-var='csvFile' onchange="removeError(this)" data-required="true" name="csvFile" id="upload-input-neft"
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
                                        <button id="bulkSubmitNeft" class="lpay_button lpay_button-md lpay_button-secondary" onclick="submitBulkPayoutRequest(this)">Submit</button>
                                        <table id="download-format-neft" class="display nowrap" style="display: none;">
                                            <thead>
                                                <tr>
                                                    <th>Order ID</th>
                                                    <th>Bene Account Name</th>
                                                    <th>Bank Account Number</th>
                                                    <th>IFSC</th>
                                                    <th>Bank Name</th>
                                                    <th>Phone Number</th>
                                                    <th>Amount</th>
                                                    <th>Purpose</th>
                                                    <th>Remark</th>
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
                    <div class="col-md-3 transaction-type">
                        <div class="row">
                            <div class="col-md-12 mb-20">
                                <label for="rtgsAccountBulk" class="checkbox-label unchecked">
                                    RTGS
                                    <s:checkbox name="rtgsAccountBulk" data-bulk="rtgsBulk" id="rtgsAccountBulk" class="signleAccountFlagBulk" data-id="rtgsAccountBulk" />
                                </label>
                            </div>
                            <!-- <div class="col-md-12">
                                <h5 class="mb-20">IMPS Bulk Upload</h5>
                            </div> -->
                            <!-- /.col-md-12 -->
                            <s:form action="getMerchantInitiatedBulkRequest" id="merchantBulkRTGS" enctype="multipart/form-data" method="POST">
                                <input type="hidden" name="txnType" value="RTGS">
                                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                    <div class="col-md-12 mb-20 single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
                                                data-download="merchantPayIdBulk" 
                                                class="selectpicker"
                                                id="merchantPayIdBulkRtgs" 
                                                headerKey="" 
                                                data-var="payIdBulk" 
                                                data-submerchant="subMerchantBulkRtgs" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
                                                data-download="merchantPayIdBulk" 
                                                class="selectpicker"
                                                id="merchantPayIdBulkRtgs"
                                                data-var="payIdBulk" 
                                                data-submerchant="subMerchantBulkRtgs" 
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
                                <s:if test="%{#session['USER'].superMerchant == true}">
                                    <div class="col-md-12 mb-20 single-account-input" data-target="subMerchantBulkNeft">
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <s:select 
                                                data-id="subMerchantBulkRtgs"
                                                data-download="subMerchantBulk" 
                                                data-var="subMerchantBulk" 
                                                data-submerchant="subMerchantBulkRtgs" 
                                                data-user="subUser"  
                                                name="subMerchantPayId" 
                                                class="selectpicker" 
                                                id="subMerchantBulkRtgs" 
                                                list="subMerchantList" 
                                                listKey="emailId" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input" data-target="subMerchantBulkRtgs"> 
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <select name="subMerchantPayId" data-download="subMerchantBulkRtgs" headerValue="ALL" data-var="subMerchantBulk" data-submerchant="subMerchantBulkRtgs" onchange="removeError(this)" data-user="subUser" id="subMerchantBulkRtgs" class=""></select>
                                            <span class="error-field"></span>
                                        </div>
                                        <!-- /.lpay_select_group -->  
                                    </div>
                                </s:else>
                                <!-- /.col-md-3 -->
                                <div class="clearfix"></div>
                                <!-- /.clearfix -->
                                <div class="col-md-12">
                                    <label for="upload-input-rtgs" class="lpay-upload single-account-input">
                                        <input type="file" data-var='csvFile' onchange="removeError(this)" data-required="true" name="csvFile" id="upload-input-rtgs"
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
                                        <button id="bulkSubmitRtgs" class="lpay_button lpay_button-md lpay_button-secondary" onclick="submitBulkPayoutRequest(this)">Submit</button>
                                        <table id="download-format-rtgs" class="display nowrap" style="display: none;">
                                            <thead>
                                                <tr>
                                                    <th>Order ID</th>
                                                    <th>Bene Account Name</th>
                                                    <th>Bank Account Number</th>
                                                    <th>IFSC</th>
                                                    <th>Bank Name</th>
                                                    <th>Phone Number</th>
                                                    <th>Amount</th>
                                                    <th>Purpose</th>
                                                    <th>Remark</th>
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
                    <div class="col-md-3 transaction-type">

                        <div class="row">

                            <div class="col-md-12 mb-20">
                                <label for="upiAccountBulk" class="checkbox-label unchecked">
                                    UPI <s:checkbox name="upiAccountBulk"
                                        id="upiAccountBulk" class="signleAccountFlagBulk" data-bulk="upiBulk" data-id="upiAccountBulk" />
                                </label>
                            </div>
    
                            <!-- <div class="col-md-12">
                                <h5 class="mb-20">UPI Bulk Upload</h5>
                            </div> -->
                            <!-- /.col-md-12 -->
                            <s:form action="getMerchantInitiatedBulkUPIRequest" id="merchantBulkUpi" enctype="multipart/form-data" method="POST">
                                <input type="hidden" name="txnType" value='UPI'>
                                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                    <div class="col-md-12 mb-20 single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
                                                data-download="merchantPayIdBulkUpi" 
                                                class="selectpicker"
                                                id="merchantPayIdBulkUpi" 
                                                headerKey="" 
                                                data-var="payIdBulk" 
                                                data-submerchant="subMerchantBulkUpi" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="payId"
                                                data-download="merchantPayIdBulkUpi" 
                                                class="selectpicker"
                                                id="merchantPayIdBulkUpi"
                                                data-var="payIdBulk" 
                                                data-submerchant="subMerchantBulkUpi" 
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
                                <s:if test="%{#session['USER'].superMerchant == true}">
                                    <div class="col-md-12 mb-20 single-account-input" data-target="subMerchantBulkUpi">
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <s:select 
                                                data-id="subMerchantBulkUpi"
                                                data-download="subMerchantBulkUpi" 
                                                data-var="subMerchantBulkUpi" 
                                                data-submerchant="subMerchantBulkUpi" 
                                                data-user="subUser"  
                                                name="subMerchantPayId" 
                                                class="selectpicker" 
                                                id="subMerchantBulkUpi" 
                                                list="subMerchantList" 
                                                listKey="emailId" 
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
                                    <div class="col-md-12 mb-20 d-none single-account-input" data-target="subMerchantBulkUpi"> 
                                        <div class="lpay_select_group">
                                            <label for="">Sub Merchant</label>
                                            <select name="subMerchantPayId" data-download="subMerchantBulkUpi" headerValue="ALL" data-var="subMerchantBulkUpi" data-submerchant="subMerchantBulkUpi" onchange="removeError(this)" data-user="subUser" id="subMerchantBulkUpi" class=""></select>
                                            <span class="error-field"></span>
                                        </div>
                                        <!-- /.lpay_select_group -->  
                                    </div>
                                </s:else>
                                <!-- /.col-md-3 -->
                                <div class="clearfix"></div>
                                <!-- /.clearfix -->
                                <div class="col-md-12">
                                    <label for="upload-input" class="lpay-upload single-account-input">
                                        <input type="file" data-var='csvFile' accept=".csv" onchange="removeError(this)" data-required="true" name="csvFile" id="upload-input"
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
                                        <button id="bulkSubmitUpi" class="lpay_button lpay_button-md lpay_button-secondary" onclick="submitBulkPayoutRequest(this)">Submit</button>
                                        <table id="download-format-upi" class="display nowrap" style="display: none;">
                                            <thead>
                                                <tr>
                                                    <th>Order ID</th>
                                                    <th>Payer Address</th>
                                                    <th>Payer Name</th>
                                                    <th>Phone Number</th>
                                                    <th>Amount</th>
                                                    <th>Purpose</th>
                                                    <th>Remark</th>
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
                </div>
                <!-- /.row -->
                <div class="row">
                    <s:if test="%{txnType != null && txnType != ''}">
                        <span class="label-heading">
                            <h3><s:property value="%{txnType}"></s:property></h3>
                        </span>
                    </s:if>
                    <s:if test="%{responseMsg != null || responseMsg != ''}">
                        <div class="responseMsgDiv">
                            <h2><s:property value="%{responseMsg}"></s:property></h2>
                        </div>
                        <!-- /.responseMsgDiv -->
                    </s:if>
                    <s:if test="%{rowCount != null && rowCount != ''}">
                        <div class="col-md-3 mb-20">
                            <div class="">
                                <div class="accountTransferBox lpay_section  box-shadow-box p20">
                                    <div class="heading_with_icon">
                                        <h2 class="heading_text" data-settled="Refund Settlement Count" data-captured="Refund Count" id="dvRefundSettledCountHeader">Total Row</h2>
                                        <!--<p class="media-heading">0</p>-->
                                        <p id="dvRefundSettledCount" class="media-heading"><s:property value="%{rowCount}" /></p> 
                                    </div>
                                </div>
                                <!-- /.revenueBox -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- /.col-md-3 mb-20 -->
                    </s:if>
                    <s:if test="%{SuccessData != null && SuccessData != ''}">
                        <div class="col-md-3 mb-20">
                            <div class="">
                                <div class="accountTransferBox lpay_section box-shadow-box p20">
                                    <div class="heading_with_icon">
                                        <h2 class="heading_text" data-settled="Refund Settlement Count" data-captured="Refund Count" id="dvRefundSettledCountHeader">Total Success</h2>
                                        <!--<p class="media-heading">2</p>-->
                                        <!-- /.media-heading -->
                                        <p id="dvRefundSettledCount" class="media-heading"><s:property value="%{SuccessData}" /></p>
                                    </div>
                                </div>
                                <!-- /.accountTransferBox -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- /.col-md-3 mb-20 -->
                    </s:if>
                    
                    <s:if test="%{failedData != null && failedData != ''}"> 
                        <div class="col-md-3 mb-20">
                            <div class="">
                                <div class="accountTransferBox lpay_section box-shadow-box p20">
                                    <div class="heading_with_icon">
                                        <h2 class="heading_text" data-settled="Refund Settlement Count" data-captured="Refund Count" id="dvRefundSettledCountHeader">Total Failed</h2>
                                        <!-- <p class="media-heading">9</p> -->
                                      
                                        <!-- /.media-heading -->
                                        <p id="dvRefundSettledCount1" class="media-heading download-failed"><s:property value="%{failedData}" /></p>
                                    </div>
                                </div>
                                <!-- /.accountTransferBox -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- /.col-md-3 mb-20 -->
                    </s:if>

                    <s:if test="%{duplicateData != null && duplicateData != ''}">
                        <div class="col-md-3 mb-20">
                            <div class="">
                                <div class="accountTransferBox lpay_section box-shadow-box p20">
                                    <div class="heading_with_icon">
                                        <h2 class="heading_text" data-settled="Refund Settlement Count" data-captured="Refund Count" id="dvRefundSettledCountHeader">Total Duplicate</h2>
                                        
                                        <p id="dvRefundSettledCount" class="media-heading download-failed-duplicate" ><s:property value="%{duplicateData}" /></p>
                                    </div>
                                </div>
                                <!-- /.accountTransferBox -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- /.col-md-3 mb-20 -->
                    </s:if>
                </div>
                <!-- /.row -->
            </div>
            <!-- merchant__forms_block -->
            <div class="merchant__forms_block" data-active="compositeReporting">
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
                                <select
                                    name="subMerchantPayId"
                                    data-download="subMerchantPayId"
                                    headerValue="ALL"
                                    data-var="subMerchantPayId"
                                    data-submerchant="subMerchantComposite"
                                    data-user="subUser"
                                    id="subMerchantComposite"
                                    class="selectpicker">
                                </select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:else>
                    <!-- /.col-md-3 -->
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                        <label for="">Status</label>
                        <select data-var="status" name="paymentMode" class="selectpicker" id="paymentMode">
                            <option value="ALL">All</option>
                            <option value="Captured">Captured</option>
                            <option value="Processing">Processing</option>
                            <option value="Declined">Declined</option>
                            <option value="Rejected">Rejected</option>
                            <option value="Timeout">Timeout</option>
                            <option value="Failed at Acquirer">Failed at Acquirer</option>
                            <option value="Duplicate">Duplicate</option>
                            <option value="Invalid">Invalid</option>
                            <option value="Failed">Failed</option>
                            <option value="Pending">Pending</option>
                            <option value="Initiated">Initiated</option>
                            <option value="Invalid at acquirer">Invalid at Acquirer</option>
                            
                        </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                        <label for="">Final Status Flag</label>
                        <select data-var="finalStatusFLag" name="finalStatusFLag" class="selectpicker" id="finalStatusFLag">
                                <option value="ALL">ALL</option>
                                <option value="TRUE">TRUE</option>
                                <option value="FALSE">FALSE</option>
                        </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->

                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_select_group">
                        <label for="">Mode</label>
                        <select data-var="txnType" name="txnType" class="selectpicker" id="txnType">
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

                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_input_group">
                            <label for="">Order ID</label>
                            <input type="text" data-var="orderId" class="lpay_input">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->

                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_input_group">
                            <label for="">Bank Account Number</label>
                            <input type="text" data-var="beneAccountNumber" class="lpay_input">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_input_group">
                            <label for="">Payer Address</label>
                            <input type="text" data-var="payerAddress" class="lpay_input">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    
                    <div class="col-md-3 mb-20 single-account-input">
                        <div class="lpay_input_group">
                            <label for="">RRN</label>
                            <input type="text" data-var="rrn" class="lpay_input">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

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
                            <button class="lpay_button lpay_button-md lpay_button-primary d-none update-status">
                                Update All
                            </button>
                            <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                            <table id="compositeReportTabel" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>
                                            <label for="update_1">
                                                <input id="update_1" onchange="selectAll(this)" type="checkbox" >
                                            </label>
                                        </th>
                                        <th>Pay ID</th>
                                        <th>Merchant Name</th>
                                        <th>Transaction ID</th>
                                        <th>Transaction Date</th>
                                        <th>Order ID</th>
                                        <th>Amount</th>
                                        <th>Status</th>
                                        <th>Action</th>
                                        <th>Update</th>
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
            <!-- /.merchant__forms_block -->
            <div class="merchant__forms_block" data-active="accountLedger">
                <div class="row">
                    <div class="col-md-9">
                        <div class="ledger-account-div highcharts-figure-jd">
						    <div id="container" style="min-width: 270px; margin: 0 auto"></div>
                            <div class="empty-graph">
                                Hi! We didn't find any payouts in last seven (7) days.
                            </div>
                            <!-- /.empty-graph -->
                        </div>
                        <!-- /.ledger-account-div -->
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-3 check-balance-div">
                        <div class="row">
                            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                                <div class="col-md-12 mb-20 single-account-input">
                                    <div class="lpay_select_group">
                                        <label for="">Select Merchant</label>
                                        <s:select 
                                            name="payId" 
                                            data-download="payId" 
                                            data-var="payId" 
                                            class="selectpicker"
                                            id="merchantPayIdLedger" 
                                            data-submerchant="subMerchantLedger"
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
                                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                    <div class="col-md-12 mb-20 single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="merchantPayIdBulkUpi"
                                                data-download="merchantPayIdBulkUpi" 
                                                class="selectpicker"
                                                id="merchantPayIdLedger" 
                                                headerKey="ALL" 
                                                data-var="payId" 
                                                data-submerchant="subMerchantLedger" 
                                                data-user="subUser"  
                                                data-live-search="true" 
                                                headerValue="ALL"
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
                                    <div class="col-md-12 mb-20 d-none single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="merchantPayIdBulkUpi"
                                                data-download="merchantPayIdBulkUpi" 
                                                class="selectpicker"
                                                id="merchantPayIdLedger"
                                                data-var="payId" 
                                                data-submerchant="subMerchantLedger" 
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
                            </s:else>
                            <s:if test="%{#session['USER'].superMerchant == true}">
                                <div class="col-md-12 mb-20 single-account-input" data-target="subMerchantLedger">
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <s:select 
                                            data-id="subMerchantLedger"
                                            data-download="subMerchantReporting" 
                                            data-var="subMerchantPayId" 
                                            data-submerchant="subMerchantReport" 
                                            data-user="subUser"  
                                            name="subMerchantLedger" 
                                            class="selectpicker" 
                                            id="subMerchantLedger" 
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
                                <div class="col-md-12 mb-20 d-none single-account-input" data-target="subMerchantLedger"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <select name="subMerchantLedger" data-download="subMerchantLedger" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchantLedger" onchange="removeError(this)" data-user="subUser" id="subMerchantLedger" class=""></select>
                                        <span class="error-field"></span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            </s:else>
                            <!-- /.col-md-3 -->
                            <div class="col-md-12">
                                <div class="ledger-check_balance_div bg-gradient-primary d-flex-column box-shadow-section br-5 p20 d-flex-space-between">
                                    <div class="ledger-balance d-flex d-flex-column text-center mb-20">
                                        <span class="ledger-amount">INR <span id="ledgerAmount">10,00,00,000</span></span>
                                        <span class="ledger-text">Current Balance</span>
                                    </div>
                                    <!-- /.ledger-balance -->
                                    <div class="ledger-refresh d-flex-center br-50" id="checkBalance">
                                        <i class="fa fa-refresh" aria-hidden="true"></i>
                                    </div>
                                    <!-- /.ledger-refresh -->
                                </div>
                                <!-- /.ledger-check_balance_div -->
                            </div>
                            <!-- /.col-md-12 -->
                        </div>
                        <!-- /.row -->
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-12 mb-20">
                        <div class="ledger-filter bg-gradient-gray br-5 box-shadow-section pt-20 d-flex flex-wrap justify-content-center">
                            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                                <div class="single-account-input mb-20 ledger-account-input">
                                    <div class="lpay_select_group">
                                        <label for="">Select Merchant</label>
                                        <s:select 
                                            name="payId" 
                                            data-download="merchantPayIdLedgerFilter" 
                                            data-var="payId" 
                                            class="selectpicker"
                                            id="merchantPayIdLedgerFilter" 
                                            data-submerchant="subMerchantLedgerFilter"
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
                                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                                    <div class="single-account-input mb-20 ledger-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="merchantPayIdLedger"
                                                data-download="merchantPayIdLedgerFilter" 
                                                class="selectpicker"
                                                id="merchantPayIdLedgerFilter" 
                                                headerKey="ALL" 
                                                data-var="payId" 
                                                data-submerchant="subMerchantLedgerFilter" 
                                                data-user="subUser"  
                                                data-live-search="true" 
                                                headerValue="ALL"
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
                                    <div class="d-none ledger-account-input single-account-input">
                                        <div class="lpay_select_group">
                                            <label for="">Select Merchant</label>
                                            <s:select 
                                                data-required="true"
                                                name="merchantPayIdLedgerFilter"
                                                data-download="merchantPayIdLedgerFilter" 
                                                class="selectpicker"
                                                id="merchantPayIdLedgerFilter"
                                                data-var="payId" 
                                                data-submerchant="subMerchantLedger" 
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
                            </s:else>
                            <s:if test="%{#session['USER'].superMerchant == true}">
                                <div class="ledger-account-input mb-20 single-account-input" data-target="subMerchantLedgerFilter">
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <s:select 
                                            data-id="subMerchantLedgerFilter"
                                            data-download="subMerchantReporting" 
                                            data-var="subMerchantPayId" 
                                            data-submerchant="subMerchantLedgerFilter" 
                                            data-user="subUser"  
                                            name="subMerchantLedgerFilter" 
                                            class="selectpicker" 
                                            id="subMerchantLedgerFilter" 
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
                                <div class="ledger-account-input mb-20 d-none single-account-input" data-target="subMerchantLedgerFilter"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <select name="subMerchantLedgerFilter" data-download="subMerchantLedgerFilter" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchantLedgerFilter" onchange="removeError(this)" data-user="subUser" id="subMerchantLedgerFilter" class=""></select>
                                        <span class="error-field"></span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            </s:else>
                            <!-- /.col-md-3 -->
                            <div class="ledger-account-input mb-20 single-account-input ledgerDate">
                                <div class="lpay_input_group">
                                    <label for="">From Date</label>
                                    <input type="text" data-var="dateFrom" id="accountLedgerDateFrom-datepick" class="lpay_input datepick accountLedger-datepick">
                                </div>
                                <!-- /.lpay_input_group -->
                            </div>
                            <!-- /.col-md-4 -->
                            <div class="ledger-account-input mb-20 single-account-input ledgerDate">
                                <div class="lpay_input_group">
                                    <label for="">To Date</label>
                                    <input type="text" data-var="dateTo" id="accountLedgerDateTo-datepick" class="lpay_input datepick accountLedger-datepick">
                                </div>
                                <!-- /.lpay_input_group -->
                            </div>
                            <!-- /.col-md-4 -->
                            <div class="button-wrapper" style="margin-top: 17px;">
                                <button class="lpay_button lpay_button-md lpay_button-primary" id="viewLedger">
                                    View
                                </button>
                                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                                <button class="lpay_button lpay_button-md lpay_button-secondary" id="downloadLedgerReportButton">
                                    Download
                                </button>
                                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                            </div>
                            <!-- /.button-wrapper -->
                        </div>
                        <!-- /.ledger-filter -->
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-12 lpay_table_style-2">
                        <div class="lpay_table">
                            <table id="ledgerTable" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>Merchant Name</th>
                                        <th>Sub Merchant Name</th>
                                        <th>Acquirer Name</th>
                                        <th>Date</th>
                                        <th>Opening Balance</th>
                                        <th>Total Credit</th>
                                        <th>Total Debit</th>
                                        <th>Closing Balance</th>
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
            <!-- /.merchant__forms_block -->
            <div class="merchant__forms_block" data-active="topup">
                <div class="row">
                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                        <div class="col-md-3 mb-20 single-account-input">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                                <s:select 
                                    name="merchantEmailId" 
                                    data-download="merchantPayId" 
                                    data-var="payId" 
                                    class="selectpicker"
                                    id="merchantTopup" 
                                    data-submerchant="subMerchantTopup"
                                    data-user="subUser" 
                                    headerKey="ALL"
                                    data-live-search="true" 
                                    headerValue="ALL"
                                    list="merchantList" 
                                    listKey="emailId"
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
                                        id="merchantTopup" 
                                        headerKey="" 
                                        data-var="payId" 
                                        data-submerchant="subMerchantTopup" 
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
                                        id="merchantTopup"
                                        list="merchantList" 
                                        data-submerchant="subMerchantTopup" 
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
                    <s:if test="%{#session['USER'].superMerchant == true}">
                        <div class="col-md-3 mb-20 single-account-input" data-target="subMerchantTopup">
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
                                    id="subMerchantTopup" 
                                    list="subMerchantList" 
                                    listKey="emailId" 
                                    headerValue="ALL"
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
                        <div class="col-md-3 mb-20 d-none single-account-input" data-target="subMerchantTopup"> 
                            <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" onchange="removeError(this)" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchantTopup" class=""></select>
                                <span class="error-field"></span>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:else>
                    <!-- /.col-md-3 -->	
                    <div class="col-md-3 single-account-input mb-20">
                      <div class="lpay_input_group">
                        <label for="">Amount <span class="req">*</span></label>
                        <input type="text" oninput="amount(event, this);removeError(this);RsPaise(Math.round(this.value*100)/100, 'topup-amountShow');" data-id="amount-left" class="lpay_input" data-required="true" onblur="roundOf(this)" onkeypress="mzDigitDot(event)" data-var="amount">
                        <span class="error-field"></span>
                        <div id="topup-amountShow" class="amount-show"></div>
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-3 single-account-input mb-20">
                        <div class="lpay_input_group">
                          <label for="">Remarks</label>
                          <input type="text" data-id="remarks" class="lpay_input" data-var="remarks">
                          <span class="error-field"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                      </div>
                      <!-- /.col-md-4 -->
                    <div class="col-md-3 show-amount" data-condition="amount">
                        <div class="amount-wrapper">
                            <div class="amount-left">
                                <span class="amount-limit">Current Balance</span>
                                <span class="totalAmount">0.00</span>
                                
                            </div>
                            <!-- /.amount-left -->
                            <button class="lpay_button lpay_button-md lpay_button-primary" onclick="addTopup(this)">ADD BALANCE</button>
                            <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                        </div>
                        <!-- /.amount-wrapper -->
                    </div>
                    <!-- /.col-md-3 -->
                </div>
                <!-- /.row -->
            </div>
            <!-- /.merchant__forms_block -->
            <div class="merchant__forms_block imps-transferred" data-active="accountStatement">
                <div class="row">

                    <div class="col-md-12 mb-20">
                        <div class="row">
                            <div class="col-md-12 mb-20">
                                <h4 class="heading_text p-15">Download Account Statement</h4>
                            </div>
                        </div>
                        <div class="row d-flex flex-wrap">
                            <div class="col-md-3 mb-20">
                                <div class="lpay_input_group">
                                    <label for="as-dateFrom">Captured Date From</label>
                                    <div class="position-relative">
                                        <s:textfield
                                            type="text"
                                            id="as-dateFrom"
                                            name="dateFrom"
                                            class="lpay_input datepick"
                                            autocomplete="off"
                                            readonly="true"
                                            data-statement='dateFrom'
                                        />
                                        <span class="error font-size-12" data-error="as-dateFrom"></span>
                                    </div>
                                </div>
                                <!-- /.lpay_input_group -->
                            </div>
                            <!-- /.col-md-4 mb-20 -->
            
                            <div class="col-md-3 mb-20">
                                <div class="lpay_input_group">
                                    <label for="as-dateTo">Captured Date To</label>
                                    <div class="position-relative">
                                        <s:textfield
                                            type="text"
                                            id="as-dateTo"
                                            name="dateTo"
                                            class="lpay_input datepick"
                                            autocomplete="off"
                                            readonly="true"
                                            data-statement='dateTo'
                                        />
                                        <span class="error font-size-12" data-error="as-dateTo"></span>
                                    </div>
                                </div>
                                <!-- /.lpay_input_group -->
                            </div>
                            <!-- /.col-md-4 -->
                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <div class="col-md-3 mb-20">
                                    <div class="lpay_select_group">
                                       <label for="">User Type</label>
                                        <s:select 
                                            name="userType"
                                            data-statement='userType'
                                            class="selectpicker"
                                            id="reportType"
                                            data-var="userType"
                                            onchange="reportChange(this)" 
                                            data-live-search="true"
                                            list="@com.paymentgateway.commons.util.PayoutUserType@values()" 
                                            listKey="name" 
                                            listValue="name" 
                                            autocomplete="off"
                                        />
                                    </div> 
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-3 -->
                            </s:if>
                            <s:else>
                                <input type="hidden" value='NA' data-statement='reportType'>
                            </s:else>
                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <div class="col-md-3 mb-20">
                                    <div class="lpay_select_group">
                                       <label for="">File Type</label>
                                       <select name="fileType" data-statement='fileType' data-id='reportyTypeCommon' class="selectpicker" id="fileType">
                                           <option value="Current">Current</option>
                                           <option value="Nodal">Nodal</option>
                                       </select>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-3 -->
                            </s:if>
                            <s:else>
                                <input type="hidden" value='NA' data-statement='fileType'>
                            </s:else>
                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <div class="col-md-3 mb-20">
                                    <div class="lpay_select_group">
                                       <label for="">Select Report</label>
                                       <select name="downloadFileOf" data-statement='downloadFileOf' data-id='reportyTypeCommon' class="selectpicker" id="downloadFileOf">
                                           <option value="Composite">Composite</option>
                                           <option value="CIB">CIB</option>
                                       </select>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-3 -->
                            </s:if>
                            <s:else>
                                <input type="hidden" value='NA' data-statement='downloadFileOf'>
                            </s:else>

                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <div class="col-md-12 text-center">
                                    <button class="lpay_button lpay_button-md lpay_button-primary generateAccountStatement">Generate</button>
                                </div>
                                <!-- /.col-md-4 -->
                            </s:if>
                            <s:else>
                                <div class="col-md-3 mb-20 d-flex align-items-end">
                                    <button class="lpay_button lpay_button-md lpay_button-primary generateAccountStatement">Generate</button>
                                </div>
                                <!-- /.col-md-4 -->
                            </s:else>
                        </div>
                    </div>
                    <div class="clearfix"></div>
                    <!-- /.clearfix -->
                    <div class="col-md-12 mb-20">
                        <h4 class="heading_text">Account Statement Filter</h4>
                    </div>
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Date From</label>
                            <input type="text" id="dateFromFilter-statement" data-statementFilter='dateFrom' class="lpay_input datepick">
                        </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="">Date To</label>
                            <input type="text" data-statementFilter='dateTo' id="dateToFilter-statement" class="lpay_input datepick">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                        <div class="col-md-3">
                            <div class="lpay_select_group">
                                <label for="">User Type</label>
                                <s:select 
                                    name="userType"
                                    data-statementFilter='userType'
                                    class="selectpicker"
                                    id="reportTypeFilter" 
                                    headerKey="ALL" 
                                    data-var="reportTypeFilter"
                                    onchange="filterChange(this, '#downloadFileOfFilter')"
                                    data-live-search="true" 
                                    headerValue="ALL"
                                    list="@com.paymentgateway.commons.util.PayoutUserType@values()" 
                                    listKey="name" 
                                    listValue="name" 
                                    autocomplete="off"
                                />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->
                    </s:if>
                    <s:else>
                        <input type="hidden" value='NA' data-statementFilter='userType'>
                    </s:else>
                    <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                        <div class="col-md-3">
                            <div class="lpay_select_group">
                               <label for="">Select Report</label>
                               <select name="downloadFileOfFilter" data-statementFilter='downloadFileOf'  class="selectpicker" id="downloadFileOfFilter">
                                    <option value="ALL">ALL</option>
                                    <option value="Composite">Composite</option>
                                    <option value="CIB">CIB</option>
                               </select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->
                    </s:if>
                    <!-- /.col-md-4 -->
                    <s:else>
                        <input type="hidden" value='NA' data-statementFilter='downloadFileOf'>
                    </s:else>
                    <div class="col-md-12 lpay_table_style-2 ">
                        <div class="lpay_table">
                            <table id="accountStatement-table" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>From Date</th>
                                        <th>To Date</th>
                                        <th>Report Name</th>
                                        <th>Create Date</th>
                                        <th>Status</th>
                                        <th>File Name</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                </div>
                <!-- /.row -->
            </div>
            <!-- /.lpay_tabs_content d-none w-100 -->
        </div>
        <!-- merchant__forms -->
    </section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <!--  -->
    
    <div class="lpay-popup_wrapper">
        <div class="lpay-popup_inner">
            <div class="lpay-popup_box">
                <div class="row">
                    <div class="col-md-12 mb-20" data-rrn="">
                        <div class="lpay_input_group">
                            <label for="">RRN Number</label>
                            <input type="text" id="updateRrn" class="lpay_input" name="rrnNumber">
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-12 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Update Status</label>
                            <select data-var="updateStatus" name="updateStatus" class="selectpicker" id="updateStatus">
                                <option value="">Select Status</option>
                                <option value="Captured">Captured</option>
                                <option value="Declined">Declined</option>
                                <option value="Rejected">Rejected</option>
                                <option value="Failed at Acquirer">Failed at Acquirer</option>
                                <option value="Duplicate">Duplicate</option>
                                <option value="Invalid">Invalid</option>
                                <option value="Failed">Failed</option>
                                <option value="Pending">Pending</option>
                                <option value="Invalid at acquirer">Invalid at Acquirer</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-12 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary update-cancel_button">Cancel</button>
                        <button class="lpay_button lpay_button-md lpay_button-primary" id="updateStatus-btn">Update</button>
                    </div>
                    <!-- /.col-md-12 -->
                    <!-- /.row -->
                </div>
                <!-- /.row -->
            </div>
            <!-- /.lpay-popup_box -->
        </div>
        <!-- /.lpay-popup_inner -->
    </div>
    <!-- /.lpay-popup_wrapper -->

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
                        <h2 class="response-msg">Failed</h2>
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

    <s:form action="downloadFailedPayout" id="payoutLocationForm">
        <s:hidden value="%{payoutLocation}" name="payoutLocation" id="newId"></s:hidden>
        <s:hidden value="%{payoutFileName}" name="payoutFileName" id="newIdpayoutFileName"></s:hidden>
    </s:form>
    <!-- <a href="#" id="down" download>Download file</a> -->

    <s:form action="downloadFailedPayout" id="payoutLocationFormOrderId">
        <s:hidden value="%{payoutLocation}" name="payoutLocation" id="newId"></s:hidden>
        <s:hidden value="%{orderIdFileName}" name="payoutFileName" id="newIdpayoutFileName"></s:hidden>
    </s:form>
    <!-- <a href="#" id="down" download>Download file</a> -->

    <s:form id="downloadAccountStatement" action="downloadAccountStatement">
        <s:hidden name="dateFrom" id="statementDateFrom"></s:hidden>
        <s:hidden name="dateTo" id="statementDateTo"></s:hidden>
        <s:hidden name="fileName" id="statementFileName"></s:hidden>
        <s:hidden name="downloadFileOf" id="downloadFileOfInput"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <form action="downloadmerchantInitiatedDirectReport" id="downloadComposite"></form>
    <form action="downloadLedgerReport" id="downloadLedgerReport"></form>
    <form action="downloadLedgerReportTrail" id="ledgerDownloadIndividual"></form>
    <script src="../js/common-validations.js"></script>
	<script src="../js/singleAccount-script.js"></script>
    <script type="text/javascript">
        function filterChange(_this, _selector){
            var _value = _this.value;
            if(_value == "Payble"){
                $(_selector).prop('disabled', true);
                $(_selector).selectpicker('refresh');
                $(_selector).selectpicker('val', 'Composite');
            }else{
                $(_selector).prop('disabled', false);
                $(_selector).selectpicker('refresh');
            }
        }
        function reportChange(_this){
            var _value = _this.value;

            if(_value == "Payble"){
                $("[data-id='reportyTypeCommon']").prop('disabled', true);
                $("[data-id='reportyTypeCommon']").selectpicker('refresh');
                $("#fileType").selectpicker('val', 'Current');
                $("#downloadFileOf").selectpicker('val', 'Composite');
            }else{
                $("[data-id='reportyTypeCommon']").prop('disabled', false);
                $("[data-id='reportyTypeCommon']").selectpicker('refresh');
            }
        }
    </script>
</body>
</html>