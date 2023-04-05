<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nodal Transfer</title>

    <link rel="stylesheet" href="../css/jquery-ui.css">
    <script src="../js/jquery-latest.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/daterangepicker.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
    <script type="text/javascript" src="../js/pdfmake.js"></script>
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/bootstrap-select.min.js"></script>
    <link rel="stylesheet" href="../css/common-style.css">
    <link rel="stylesheet" href="../css/horizontal-scrolling-nav.css">
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <!-- <script src="../js/numberToWord.js"></script> -->
    <script src="../js/wordToAmountPaise.js"></script>
    <script src="../js/common-scripts.js"></script>
    <style>
        .w-fluid{
            width: 100% !important;
        }
        .refreshTable-data{ white-space: nowrap; }
        .refreshTable-data span{ margin-right: 5px; }
        .amount-show{ font-size: 10px;font-weight: 500;margin-top: 4px; }
        .tf-balance_sheet{ position: absolute;top: 0;padding: 15px;right: 0;border-radius: 5px; }
        .tf-balance_sheet div span{ border-radius: 5px;padding: 5px 10px;border-radius: 10px;background-color: #fff;color: #333;font-weight: 600; }
        .tf-balance_sheet div { min-width: 200px;display: flex;align-items: center;justify-content: space-between; }
        .tf-balance_sheet div:last-child{ margin-top: 15px; }
    </style>
</head>
<body class="bodyColor">
    <div class="edit-permission"><s:property value="%{editingpermission}"/></div>
    <s:hidden value="%{#session.USER.UserType.name()}" id="userType" />
    <s:hidden id="isSuperMerchant"></s:hidden>
    <!-- /.edit-permission -->
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Nodal Transfer</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-12 mb-20">
                <div class="horizontal-nav-wrapper mb-20">
                    <nav id="horizontal-nav" class="horizontal-nav">
                        <ul class="horizontal-nav-content nav nav-tabs list-unstyled font-size-10 lpay_tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
                            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">
                                <li class="lpay-nav-item active"><a href="#" class="lpay-nav-link" data-id="add-beneficiary">Add Beneficiary</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="search-beneficiary">Search Beneficiary</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="transfer-funds">Transfer Funds</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="search-transaction">Payout Report</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="account-balance">Account Balance</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="update-utr">Update UTR No</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="account-statement">Account Statement</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="topupReport">Topup Report</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="bulkTransfer">Bulk Transfer</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="bulkUTR">Bulk UTR</a></li>
                            </s:if>
                            <s:elseif test="%{#session.USER.allowNodalPayoutFlag == true}">
                                <li class="lpay-nav-item active"><a href="#" class="lpay-nav-link" data-id="transfer-funds">Transfer Funds</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="search-transaction">Payout Report</a></li>
                                <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="topupReport">Topup Report</a></li>
                            </s:elseif>
                        </ul>
                    </nav>
                    <button type="button" id="btn-scroll-left" class="horizontal-nav-btn horizontal-nav-btn-left m-0 bg-color-white pr-5">
                        <svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
                        <path d="M445.44 38.183L-2.53 512l447.97 473.817 85.857-81.173-409.6-433.23v81.172l409.6-433.23L445.44 38.18z" /></svg>
                    </button>
                    <button type="button" id="btn-scroll-right" class="horizontal-nav-btn horizontal-nav-btn-right m-0 bg-color-white pl-5">
                        <svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
                        <path d="M105.56 985.817L553.53 512 105.56 38.183l-85.857 81.173 409.6 433.23v-81.172l-409.6 433.23 85.856 81.174z" /></svg>
                    </button>
                </div>
            </div>
            <!-- /.col-md-12 -->

            <div class="lpay_tabs_content w-100" data-target="add-beneficiary">                
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="ab-merchant">Merchant <span class="color-red">*</span></label>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-id="lpay-input"
                                    class="selectpicker lpay-input blankInput merchant-selectbox"
                                    id="ab-merchant"
                                    headerKey=""
                                    data-live-search="true"
                                    headerValue="Select Merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="ab-subMerchant"
                                    onchange="validateInputField(this);"
                                />
                                <span data-error="ab-merchant" class="error font-size-12"></span>
                            </div>
                        </s:if>
                        <s:else>
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-live-search="true"
                                    class="selectpicker lpay-input blankInput merchant-selectbox"
                                    id="ab-merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="ab-subMerchant"
                                    onchange="validateInputField(this);"
                                />
                                <span data-error="ab-merchant" class="error font-size-12"></span>
                            </div>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->

                <s:if test="%{#session['USER'].superMerchant == true}">
				    <div class="col-md-4 mb-20" data-id="ab-subMerchant">
					    <div class="lpay_select_group">
                            <label for="ab-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <s:select
                                    name="subMerchant"
                                    class="selectpicker lpay-input"
                                    id="ab-subMerchant"
                                    list="subMerchantList"
                                    listKey="emailId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    onchange="validateInputField(this);"
                                />
                                <span data-error="ab-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>
					    <!-- /.lpay_select_group -->  
				    </div>
				    <!-- /.col-md-3 -->	
			    </s:if>
			    <s:else>
				    <div class="col-md-4 mb-20 d-none" data-id="ab-subMerchant"> 
					    <div class="lpay_select_group">
                            <label for="ab-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <select name="subMerchant" id="ab-subMerchant" class="lpay-input" onchange="validateInputField(this);"></select>
                                <span data-error="ab-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>
				        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
			    </s:else>

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="ab-bankAccountNumber">Beneficiary Account Number <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="ab-bankAccountNumber"
                                name="bankAccountNumber"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                maxlength="20"
                                inputmode="numeric"
                                oninput="onlyAlphaNumeric(this); validateInputField(this);"
                            />
                            <span data-error="ab-bankAccountNumber" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->
                
                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="ab-bankAccountName">Beneficiary Name <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="ab-bankAccountName"
                                name="bankAccountName"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                oninput="alphaNumericWithSpace(this); validateInputField(this);"
                            />
                            <span data-error="ab-bankAccountName" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="ab-bankAccountNickName">Alias <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="ab-bankAccountNickName"
                                name="bankAccountNickName"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                oninput="alphaNumericWithSpace(this); validateInputField(this);"
                            />
                            <span data-error="ab-bankAccountNickName" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="ab-payeeType">Payee Type <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <select name="payeeType" id="ab-payeeType" class="selectpicker lpay-input" onchange="validateInputField(this);">
                                <option value="">Select Payee Type</option>
                                <option value="ONUS">ONUS</option>
                                <option value="OFFUS">OFFUS</option>
                            </select>
                            <span data-error="ab-payeeType" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="ab-bankIfsc">IFSC <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <input
                                type="text"
                                class="lpay_input lpay-input blankInput"
                                id="ab-bankIfsc"
                                maxlength="11"
                                name="bankIfsc"
                                autocomplete="off"
                                oninput="onlyAlphaNumeric(this); _uppercase(this); validateInputField(this);"
                            >
                            <span data-error="ab-bankIfsc" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20 d-none" data-id="defaultbene">
                    <div class="lpay_select_group d-flex flex-column">
                        <label for="ab-defaultbene" class="mb-10">Set As Default</label>
                        <label class="lpay_toggle">
                            <input type="checkbox" class="lpay-input" name="defaultbene" id="ab-defaultbene" data-toggle="toggle" />
                        </label>
                    </div>
                </div>
                
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="btn-add-beneficiary">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="bulkTransfer">
                <div class="col-xs-12">
                    <s:form method="post" enctype="multipart/form-data" class="bulkUpload">
                        <div class="col-md-6">
                            <label for="bulkTransfer-upload-input" class="lpay-upload">
                                <input type="file" name="csvFile" accept=".xlsx" data-type="bulkTransfer" id="bulkTransfer-upload-input" class="lpay_upload_input">
                                <div class="default-upload">
                                    <h3>Upload Your xlsx File</h3>
                                    <img src="../image/image_placeholder.png" class="img-responsive placeholder_img" alt="">
                                </div>
                                <!-- /.default-upload -->
                                <div class="upload-status">
                                    <div class="success-wrapper upload-status-inner d-none">
                                        <div class="success-icon-box status-icon-box">
                                            <img src="../image/tick.png" alt="">
                                        </div>
                                        <div class="mt-20 status-text-box">
                                            <h3 class="mb-8">Upload Successfully</h3>
                                            <div class="fileInfo">
                                                <span class="error-fileName"></span>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.mt-20 -->
                                    </div>
                                    <!-- /.success-wraper -->
                                    <div class="error-wrapper upload-status-inner d-none">
                                        <div class="error-icon-box status-icon-box">
                                            <img src="../image/wrong-tick.png" alt="">
                                        </div>
                                        <div class="mt-20 status-text-box">
                                            <h3 class="mb-8">Upload Failed</h3>
                                            <div class="fileInfo">
                                                <span class="error-fileName"></span>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.mt-20 -->
                                    </div>
                                    <!-- /.success-wraper -->
                                </div>
                                <!-- /.upload-success -->
                            </label>
                            <input type="hidden" class="input-fileName" name="fileName"/> 
        
                            <div class="button-wrapper mt-20 d-flex flex-wrap justify-content-center text-center">
                                <button class="lpay_button lpay_button-md lpay_button-secondary bulkSubmit" data-action="bulkNodalTransferProcess" disabled>Submit</button>                                
                            </div>
                            <!-- /.button-wrapper -->
                        </div>
                        <!-- /.col-md-4 -->
                        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
                    </s:form>
                </div>

                <div class="row d-none wrong-file">
                    <div class="col-md-12">
                        <section class="bulk-invoice lpay_section white-bg box-shadow-box mt-20 p20">
                            <div class="row">
                                <div class="col-md-12 text-center">
                                    <h3 class="text-snackbar-danger bg-snackbar-danger py-30 font-size-14 font-weight-medium m-0">Wrong File Format</h3>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>

                <div class="row success-file d-none">
                    <div class="col-md-6">
                        <section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20 pr-md-0">
                            <div class="box-shadow-common p-15 border-radius-6 border-grey-lightest">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="heading_with_icon mb-30">
                                            <h2 class="heading_text dashbaord_heading ml-0">Total Numbers of Data</h2>
                                        </div>
                                    </div>
                                    <div class="col-md-12 text-center lpay_xl">
                                        <span class="totalData"></span>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <!-- /.col-md-6 -->
    
                    <div class="col-md-6">
                        <section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20 pl-md-0">
                            <div class="box-shadow-common p-15 border-radius-6 border-grey-lightest">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="heading_with_icon mb-30">
                                            <h2 class="heading_text dashbaord_heading ml-0">Successfully Stored</h2>
                                        </div>
                                    </div>
                                    <div class="col-md-12 text-center lpay_xl">
                                        <span class="storedRow"></span>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <!-- /.col-md-6 -->
                </div>

                <div class="col-md-12 mt-30">
                    <div class="row">
                        <div class="col-md-3">
                            <div class="lpay_input_group">
                                <label for="">Date From</label>
                                <s:textfield type="text" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
                            </div>
                        </div>
                
                        <div class="col-md-3">
                            <div class="lpay_input_group">
                                <label for="">Date To</label>
                                <s:textfield type="text" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
                            </div>
                        </div>

                        <div class="col-md-3">
                            <div class="button-wrapper mt-20 d-flex flex-wrap">
                                <button class="lpay_button lpay_button-md lpay_button-secondary bulk-filter">Submit</button>                                
                            </div>
                        </div>
                    </div>                    
                </div>


                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="PAYOUT_FILE-table" class="display text-center" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th class="text-center">Created Date</th>
                                    <th class="text-center">File Name</th>
                                    <th class="text-center">Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>

            <div class="lpay_tabs_content d-none w-100" data-target="bulkUTR">
                <div class="col-xs-12">
                    <s:form method="post" enctype="multipart/form-data" class="bulkUpload">
                        <div class="col-md-12">
                            <label for="isIdfc" class="checkbox-label unchecked">
                                Is IDFC?
                                <s:checkbox
                                    name="idfcUpiSettlementFlag"
                                    id="isIdfc"
                                    class="bulkUtrCheckbox"
                                    data-set="imps"
                                    data-checked="idfcUpiSettlementFlag"
                                    data-id="isIdfc"
                                />
                            </label>
                        </div>
                        <div class="col-md-6">
                            <label for="bulkUTR-upload-input" class="lpay-upload">
                                <input type="file" name="csvFile" accept=".xlsx" data-type="bulkUTR" id="bulkUTR-upload-input" class="lpay_upload_input">
                                <div class="default-upload">
                                    <h3>Upload Your xlsx File</h3>
                                    <img src="../image/image_placeholder.png" class="img-responsive placeholder_img" alt="">
                                </div>
                                <!-- /.default-upload -->
                                <div class="upload-status">
                                    <div class="success-wrapper upload-status-inner d-none">
                                        <div class="success-icon-box status-icon-box">
                                            <img src="../image/tick.png" alt="">
                                        </div>
                                        <div class="mt-20 status-text-box">
                                            <h3 class="mb-8">Upload Successfully</h3>
                                            <div class="fileInfo">
                                                <span class="error-fileName"></span>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.mt-20 -->
                                    </div>
                                    <!-- /.success-wraper -->
                                    <div class="error-wrapper upload-status-inner d-none">
                                        <div class="error-icon-box status-icon-box">
                                            <img src="../image/wrong-tick.png" alt="">
                                        </div>
                                        <div class="mt-20">
                                            <h3 class="mb-8 status-text-box">Upload Failed</h3>
                                            <div class="fileInfo">
                                                <span class="error-fileName">File size too Long.</span>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.mt-20 -->
                                    </div>
                                    <!-- /.success-wraper -->
                                </div>
                                <!-- /.upload-success -->
                            </label>
                            <input type="hidden" class="input-fileName" name="fileName"/> 
        
                            <div class="button-wrapper mt-20 d-flex flex-wrap justify-content-center text-center">
                                <button class="lpay_button lpay_button-md lpay_button-secondary bulkSubmit" data-action="uploadBulkUtrFile" disabled="true">Submit</button>                                
                            </div>
                            <!-- /.button-wrapper -->
                        </div>
                        <!-- /.col-md-4 -->
                        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
                    </s:form>
                </div>

                <div class="row d-none wrong-file">
                    <div class="col-md-12">
                        <section class="bulk-invoice lpay_section white-bg box-shadow-box mt-20 p20">
                            <div class="row">
                                <div class="col-md-12 text-center">
                                    <h3 class="text-snackbar-danger bg-snackbar-danger py-30 font-size-14 font-weight-medium m-0">Wrong File Format</h3>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>

                <div class="row success-file d-none">
                    <div class="col-md-6">
                        <section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20 pr-md-0">
                            <div class="box-shadow-common p-15 border-radius-6 border-grey-lightest">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="heading_with_icon mb-30">
                                            <h2 class="heading_text dashbaord_heading ml-0">Total Numbers of Data</h2>
                                        </div>
                                    </div>
                                    <div class="col-md-12 text-center lpay_xl">
                                        <span class="totalData"></span>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <!-- /.col-md-6 -->
    
                    <div class="col-md-6">
                        <section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20 pl-md-0">
                            <div class="box-shadow-common p-15 border-radius-6 border-grey-lightest">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="heading_with_icon mb-30">
                                            <h2 class="heading_text dashbaord_heading ml-0">Successfully Stored</h2>
                                        </div>
                                    </div>
                                    <div class="col-md-12 text-center lpay_xl">
                                        <span class="storedRow"></span>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <!-- /.col-md-6 -->
                </div>

                <div class="col-md-12 mt-30">
                    <div class="row">
                        <div class="col-md-3">
                            <div class="lpay_input_group">
                                <label for="">Date From</label>
                                <s:textfield type="text" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
                            </div>
                        </div>
                
                        <div class="col-md-3">
                            <div class="lpay_input_group">
                                <label for="">Date To</label>
                                <s:textfield type="text" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
                            </div>
                        </div>

                        <div class="col-md-3">
                            <div class="button-wrapper mt-20 d-flex flex-wrap">
                                <button class="lpay_button lpay_button-md lpay_button-secondary bulk-filter">Submit</button>                                
                            </div>
                        </div>
                    </div>                    
                </div>

                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="UTR_FILE-table" class="display text-center" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th class="text-center">Created Date</th>
                                    <th class="text-center">File Name</th>
                                    <th class="text-center">Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>

            <div class="lpay_tabs_content d-none w-100" data-target="search-beneficiary">
                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="sb-bankAccountNumber">Account Number</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="sb-bankAccountNumber"
                                name="bankAccountNumber"
                                class="lpay_input"
                                autocomplete="off"
                                oninput="onlyAlphaNumeric(this);"
                            />
                            <span data-error="sb-bankAccountNumber" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="sb-payId">Merchant</label>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    class="selectpicker lpay-input merchant-selectbox"
                                    id="sb-payId"
                                    headerKey="ALL"
                                    data-live-search="true"
                                    headerValue="ALL"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="sb-subMerchant"
                                />
                            </div>
                        </s:if>
                        <s:else>
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-live-search="true"
                                    class="selectpicker lpay-input merchant-selectbox"
                                    id="sb-payId"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="sb-subMerchant"
                                />
                            </div>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->

                <s:if test="%{#session['USER'].superMerchant == true}">
				    <div class="col-md-4 mb-20" data-id="sb-subMerchant">
					    <div class="lpay_select_group">
                            <label for="sb-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <s:select
                                    name="subMerchant"
                                    class="selectpicker lpay-input"
                                    id="sb-subMerchant"
                                    list="subMerchantList"
                                    listKey="emailId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
					    </div>
					    <!-- /.lpay_select_group -->  
				    </div>
				    <!-- /.col-md-4 -->	
			    </s:if>
			    <s:else>
				    <div class="col-md-4 mb-20 d-none" data-id="sb-subMerchant"> 
					    <div class="lpay_select_group">
                            <label for="sb-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <select name="subMerchant" id="sb-subMerchant" class="lpay-input"></select>
                                <span data-error="sb-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>
				        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-4 -->
			    </s:else>

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="sb-payeeType">Payee Type</label>
                        <div class="position-relative">
                            <select class="selectpicker" name="payeeType" id="sb-payeeType">
                                <option value="ALL">All</option>
                                <option value="ONUS">ONUS</option>
                                <option value="OFFUS">OFFUS</option>
                            </select>
                            <span class="error font-size-12" data-error="sb-payeeType"></span>
                        </div>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 -->
                
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="sb-status">Status</label>
                        <div class="position-relative">
                            <select class="selectpicker" name="status" id="sb-status">
                                <option value="ALL">All</option>
                                <option value="Processing">Processing</option>
                                <option value="SUCCESS">Success</option>
                                <option value="Failed">Failed</option>
                            </select>
                            <span class="error font-size-12" data-error="sb-status"></span>
                        </div>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20 d-none">
                    <div class="lpay_input_group">
                        <label for="sb-dateFrom">Captured Date From</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="sb-dateFrom"
                                name="dateFrom"
                                class="lpay_input datepick"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="sb-dateFrom"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->

                <div class="col-md-4 mb-20 d-none">
                    <div class="lpay_input_group">
                        <label for="sb-dateTo">Captured Date To</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="sb-dateTo"
                                name="dateTo"
                                class="lpay_input datepick"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="sb-dateTo"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-12 mb-20 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary viewData">View</button>
                    <button class="lpay_button lpay_button-md lpay_button-primary downloadBeneReport">Download</button>
                </div>
                <!-- /.col-md-12 -->

                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="datatable" class="display text-center" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th class="text-center">Merchant Name</th>
                                    <th class="text-center">Bank Account Number</th>
                                    <th class="text-center">Addition Date</th>
                                    <th class="text-center">Status</th>
                                    <th class="text-center">Set as Default</th>
                                    <th class="text-center">Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="transfer-funds">                
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Merchant <span class="color-red">*</span></label>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    class="selectpicker lpay-input blankInput merchant-selectbox"
                                    id="tf-merchant"
                                    headerKey=""
                                    data-live-search="true"
                                    headerValue="Select Merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="tf-subMerchant"
                                    onchange="validateInputField(this);"
                                />
                                <span data-error="tf-merchant" class="error font-size-12"></span>
                            </div>
                        </s:if>
                        <s:else>
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-live-search="true"
                                    class="selectpicker lpay-input blankInput merchant-selectbox"
                                    id="tf-merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="tf-subMerchant"
                                    onchange="validateInputField(this);"
                                />
                                <span data-error="tf-merchant" class="error font-size-12"></span>
                            </div>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->

                <s:if test="%{#session['USER'].superMerchant == true}">
				    <div class="col-md-4 mb-20" data-id="tf-subMerchant">
					    <div class="lpay_select_group">
                            <label for="tf-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <s:select
                                    name="subMerchant"
                                    class="selectpicker lpay-input"
                                    id="tf-subMerchant"
                                    list="subMerchantList"
                                    listKey="emailId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    onchange="validateInputField(this);"
                                />
                                <span data-error="tf-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>
					    <!-- /.lpay_select_group -->  
				    </div>
				    <!-- /.col-md-3 -->	
			    </s:if>
			    <s:else>
				    <div class="col-md-4 mb-20 d-none" data-id="tf-subMerchant"> 
					    <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <div class="position-relative">
                                <select name="subMerchant" id="tf-subMerchant" class="lpay-input" onchange="validateInputField(this);"></select>
                                <span data-error="tf-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>
				        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
                </s:else>
                
                <div class="col-md-4 mb-20 d-none" data-id="tf-bankAccountName">
                    <div class="lpay_input_group">
                        <label for="tf-bankAccountName">Payee Name <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="tf-bankAccountName"
                                name="bankAccountName"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                readonly="true"
                                oninput="alphaNumericWithSpace(this); validateInputField(this);"
                            />
                            <span data-error="tf-bankAccountName" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20 d-none" data-id="tf-bankAccountNumber">
                    <div class="lpay_input_group">
                        <label for="tf-bankAccountNumber">Payee Account <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="tf-bankAccountNumber"
                                name="bankAccountNumber"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                inputmode="numeric"
                                readonly="true"
                                oninput="onlyNumberInput(this); validateInputField(this);"
                            />
                            <span data-error="tf-bankAccountNumber" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->

                <div class="col-md-4 mb-20 d-none" data-id="tf-bankIfsc">
                    <div class="lpay_input_group">
                        <label for="tf-bankIfsc">IFSC <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <input
                                type="text"
                                class="lpay_input lpay-input blankInput"
                                id="tf-bankIfsc"
                                name="bankIfsc"
                                autocomplete="off"
                                readonly="true"
                                oninput="onlyAlphaNumeric(this); _uppercase(this); validateInputField(this);"
                            >
                            <span data-error="tf-bankIfsc" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Currency <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                                <s:select
                                    name=""
                                    id="tf-currencyCode"
                                    headerValue="Select Currency"
                                    headerKey=""
                                    list="currencyMap"
                                    listKey="key"
                                    listValue="value"
                                    class="selectpicker lpay-input"
                                    autocomplete="off"
                                />
                                <s:select
                                    name="currency"
                                    id="tf-mappedCurrency"
                                    headerValue="Select Currency"
                                    headerKey=""
                                    list="currencyMap"
                                    listKey="key"
                                    listValue="value"
                                    class="selectpicker lpay-input d-none"
                                    autocomplete="off"
                                />
                            </s:if>
                            
                            <s:if test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
                                <s:select
                                    name="currency"
                                    id="tf-mappedCurrency"
                                    headerValue="Select Currency"
                                    headerKey="Select Currency"
                                    list="currencyMap"
                                    listKey="key"
                                    listValue="value"
                                    class="selectpicker lpay-input d-none"
                                    autocomplete="off"
                                />
                            </s:if>
                            <span data-error="tf-mappedCurrency" class="error font-size-12"></span>
                            <span data-error="tf-currencyCode" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="tf-txnMode">TXN Mode <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <select name="txnType" id="tf-txnType" class="selectpicker lpay-input" onchange="validateInputField(this);">
                                <option value="">Select TXN Mode</option>
                                <option value="IMPS">IMPS</option>
                                <option value="NEFT">NEFT</option>
                                <option value="RTGS">RTGS</option>
                               
                            </select>
                            <span data-error="tf-txnType" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="tf-amount">Amount <span class="color-red">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="tf-amount"
                                name="amount"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                oninput="validateInputField(this);RsPaise(Math.round(this.value*100)/100, 'tf-amountShow');"
                                onkeypress="digitDot(event)"
                                onchange="checkAvailableAmount(this)"
                            />
                            <span data-error="tf-amount" class="error font-size-12"></span>
                            <div id="tf-amountShow" class="amount-show"></div>
                            <!-- /.amount-show -->
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="tf-remarks">Remarks</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="tf-remarks"
                                name="remarks"
                                class="lpay_input lpay-input blankInput"
                                autocomplete="off"
                                oninput="onlyAlphaNumeric(this); validateInputField(this);"
                            />
                            <span data-error="tf-remarks" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="tf-balance_sheet d-none lpay_button-primary">
                    <div class="tf-total_amount">
                        Total Balance <span></span>
                    </div>
                    <!-- /.tf-total_amount -->
                    <div class="tf-total_available_amount">
                        Available Balance <span></span>
                    </div>
                    <!-- /.tf-total_amount -->
                </div>
                <!-- /.tf-balance_sheet -->

                
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="btn-transfer-funds">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="search-transaction">
                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="st-txnId">Txn ID</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"                                
                                id="st-txnId"
                                name="txnId"
                                class="lpay_input"
                                autocomplete="off"
                            />
                            <span data-error="st-txnId" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="st-utrNo">UTR</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"                                
                                id="st-utrNo"
                                name="utrNo"
                                class="lpay_input"
                                autocomplete="off"
                            />
                            <span data-error="st-utrNo" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="st-payId">Merchant</label>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    class="selectpicker lpay-input merchant-selectbox"
                                    id="st-payId"
                                    headerKey="ALL"
                                    data-live-search="true"
                                    headerValue="ALL"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="st-subMerchant"
                                />
                            </div>
                        </s:if>
                        <s:else>
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-live-search="true"
                                    class="selectpicker lpay-input merchant-selectbox"
                                    id="st-payId"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="st-subMerchant"
                                />
                            </div>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->

                <s:if test="%{#session['USER'].superMerchant == true}">
				    <div class="col-md-4 mb-20" data-id="st-subMerchant">
					    <div class="lpay_select_group">
                            <label for="st-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <s:select
                                    name="subMerchant"
                                    class="selectpicker lpay-input"
                                    id="st-subMerchant"
                                    list="subMerchantList"
                                    listKey="emailId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
					    </div>
					    <!-- /.lpay_select_group -->  
				    </div>
				    <!-- /.col-md-4 -->	
			    </s:if>
			    <s:else>
				    <div class="col-md-4 mb-20 d-none" data-id="st-subMerchant"> 
					    <div class="lpay_select_group">
                            <label for="st-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <select name="subMerchant" id="st-subMerchant" class="lpay-input"></select>
                                <span data-error="st-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>
				        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-4 -->
                </s:else>
                
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="st-status">Status</label>
                        <div class="position-relative">
                            <select class="selectpicker" name="status" id="st-status">
                                <option value="ALL">All</option>
                                <option value="Captured">Captured</option>
                                <option value="Failed">Failed</option>
                                <option value="Duplicate">Duplicate</option>
                                <option value="Pending">Pending</option>
                                <option value="Processing">Processing</option>
                            </select>
                            <span class="error font-size-12" data-error="st-status"></span>
                        </div>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 -->
                

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="st-dateFrom">Captured Date From</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="st-dateFrom"
                                name="dateFrom"
                                class="lpay_input datepick"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="st-dateFrom"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 mb-20 -->

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="st-dateTo">Captured Date To</label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="st-dateTo"
                                name="dateTo"
                                class="lpay_input datepick"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="st-dateTo"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-12 mb-20 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary st-viewData">View</button>
                    <button class="lpay_button lpay_button-md lpay_button-primary downloadTranData">Download</button>
                </div>
                <!-- /.col-md-12 -->

                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="st-datatable" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Merchant Name</th>
                                    <th>Txn ID</th>
                                    <th>Payout Date</th>
                                    <th>UTR Number</th>
                                    <th>Amount</th>
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

            <div class="lpay_tabs_content d-none w-100" data-target="account-balance">                
                <div class="col-md-12 mb-20 d-flex align-items-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary checkBalance">Check Balance</button>
                    <h3 class="balanceAmount ml-30 font-size-30 font-weight-medium">0.00</h3>
                </div>

                
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="update-utr">
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="st-payId">Merchant <span class="text-danger">*</span></label>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="position-relative">
                                <s:select
                                    data-required="true"
                                    name="payId"
                                    class="selectpicker lpay-input merchant-selectbox"
                                    id="utr-payId"
                                    headerKey=""
                                    data-user="subUser"  
                                    data-live-search="true"
                                    headerValue="Select Merchant"
                                    data-submerchant="utr-subMerchant" 
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    onchange="validateUTRInputField(this)"
                                />
                                <span data-error="utr-payId" class="error font-size-12"></span>
                            </div>
                        </s:if>
                        <s:else>
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-live-search="true"
                                    class="selectpicker lpay-input merchant-selectbox"
                                    id="utr-payId"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="utr-subMerchant"
                                />
                            </div>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>

                <s:if test="%{#session['USER'].superMerchant == true}">
				    <div class="col-md-4 mb-20" data-target="utr-subMerchant">
					    <div class="lpay_select_group">
                            <label for="utr-subMerchant">Sub Merchant <span class="text-danger">*</span></label>
                            <div class="position-relative">
                                <s:select
                                    name="subMerchant"
                                    class="selectpicker lpay-input"
                                    id="utr-subMerchant"
                                    list="subMerchantList"
                                    listKey="emailId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-submerchant="utr-subMerchant"
                                />
                            </div>
					    </div>					    
				    </div>				    
			    </s:if>
			    <s:else>
				    <div class="col-md-4 mb-20 d-none" data-target="utr-subMerchant"> 
					    <div class="lpay_select_group">
                            <label for="utr-subMerchant">Sub Merchant <span class="text-danger">*</span></label>
                            <div class="position-relative">
                                <select
                                    name="subMerchant"
                                    id="utr-subMerchant"
                                    class="lpay-input"
                                    data-user="subUser"
                                    data-submerchant="utr-subMerchant"
                                    autocomplete="off">
                                </select>
                                <span data-error="utr-subMerchant" class="error font-size-12"></span>
                            </div>
					    </div>				        
                    </div>
                </s:else>

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="utr-dateFrom">Captured Date From <span class="text-danger">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="utr-dateFrom"
                                name="dateFrom"
                                class="lpay_input datepick lpay-input"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="utr-dateFrom"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="utr-dateTo">Captured Date To <span class="text-danger">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="utr-dateTo"
                                name="dateTo"
                                class="lpay_input datepick lpay-input"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="utr-dateTo"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="utr-payoutDate">Payout Date <span class="text-danger">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="utr-payoutDate"
                                name="dateTo"
                                class="lpay_input datepick lpay-input"
                                autocomplete="off"
                                readonly="true"
                            />
                            <span class="error font-size-12" data-error="utr-payoutDate"></span>
                        </div>
                    </div>
                </div>

                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                        <label for="utr-utrNumber">UTR No. <span class="text-danger">*</span></label>
                        <div class="position-relative">
                            <s:textfield
                                type="text"
                                id="utr-utrNumber"
                                name="txnId"
                                class="lpay_input lpay-input"
                                autocomplete="off"
                                oninput="onlyAlphaNumeric(this); _uppercase(this); validateUTRInputField(this)"
                                maxlength="50"
                            />
                            <span data-error="utr-utrNumber" class="error font-size-12"></span>
                        </div>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>

                <div class="col-md-4 mb-20">
					<div class="lpay_select_group">
						<label for="utr-acquirer">Acquirer <span class="text-danger">*</span></label>
						<div class="position-relative">
							<s:select
								name="acquirer"
								class="selectpicker lpay-input"
								id="utr-acquirer"
								title="ALL"
								list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
								listKey="code"
								listValue="name"								
								autocomplete="off"
                                multiple="true"
                                data-live-search="true"
							/>
							<span data-error="utr-acquirer" class="error font-size-12"></span>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.lpay_select_group -->  
				</div>

                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                        <label for="utr-paymentType">Payment Type <span class="text-danger">*</span></label>
                        <s:select
                            title="ALL"
                            class="selectpicker lpay-input"
                            list="@com.paymentgateway.commons.util.PaymentType@values()"
                            listValue="name"
                            listKey="code"
                            name="paymentMethods"
                            id="utr-paymentType"
                            autocomplete="off"
                            multiple="true"
                            data-live-search="true"
                        />
                        <span data-error="utr-paymentType" class="error font-size-12"></span>
                    </div>
                    <!-- /.lpay_select_box -->
                </div>
                <!-- /.col-md-3 -->

                <div class="col-md-4 mb-20">                    
                    <div class="lpay_select_group">
                        <label for="utr-mopType">Mop Type <span class="text-danger">*</span></label>
                        <s:select
                            name="mopType"
                            id="utr-mopType"
                            title="ALL"
                            list="@com.paymentgateway.commons.util.MopType@values()"
                            listValue="name"
                            listKey="code"
                            class="selectpicker lpay-input"
                            multiple="true"
                            data-live-search="true"
                        />
                        <span data-error="utr-mopType" class="error font-size-12"></span>
                    </div>
                    <!-- /.lpay_select_box -->                    
                </div>

                <div class="col-xs-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="utr-submit">Submit</button>
                </div>
            </div>

            <div class="lpay_tabs_content d-none w-100" data-target="account-statement">
                <div class="col-md-12 mb-20">
                    <div class="row">
                        <div class="col-12">
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
                                    />
                                    <span class="error font-size-12" data-error="as-dateTo"></span>
                                </div>
                            </div>
                            <!-- /.lpay_input_group -->
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                               <label for="">User Type</label>
                               <select name="reportType" onchange="reportChange(this)" class="selectpicker" id="reportType">
                                   <option value="Payment Gateway">Payment Gateway</option>
                                   <option value="Payble">Payble</option>
                               </select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                               <label for="">File Type</label>
                               <select name="fileType" data-id='reportyTypeCommon' class="selectpicker" id="fileType">
                                   <option value="Current">Current</option>
                                   <option value="Nodal">Nodal</option>
                               </select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                               <label for="">Select Report</label>
                               <select name="downloadFileOf" data-id='reportyTypeCommon' class="selectpicker" id="downloadFileOf">
                                   <option value="Composite">Composite</option>
                                   <option value="CIB">CIB</option>
                               </select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->
                        
                        <div class="col-md-12 text-center">
                            <button class="lpay_button lpay_button-md lpay_button-primary generateAccountStatement">Generate</button>
                        </div>
                        <!-- /.col-md-4 -->
                    </div>
                </div>
                <div class="clearfix"></div>
                <!-- /.clearfix -->
                <div class="col-12">
                    <h4 class="heading_text p-15">Account Statement Filter</h4>
                </div>
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date From</label>
                        <input type="text" id="dateFromFilter-statement" class="lpay_input datepick">
                    </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date To</label>
                        <input type="text" id="dateToFilter-statement" class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3">
                    <div class="lpay_select_group">
                       <label for="">User Type</label>
                       <select name="reportTypeFilter" onchange="filterChange(this, '#downloadFileOfFilter')" class="selectpicker"  id="reportTypeFilter">
                            <option value="ALL">ALL</option>
                            <option value="Payment Gateway">Payment Gateway</option>
                            <option value="Payble">Payble</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3">
                    <div class="lpay_select_group">
                       <label for="">Select Report</label>
                       <select name="downloadFileOfFilter"  class="selectpicker" id="downloadFileOfFilter">
                            <option value="ALL">ALL</option>
                            <option value="Composite">Composite</option>
                            <option value="CIB">CIB</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <!-- /.col-md-4 -->
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
            <!-- /.lpay_tabs_content d-none w-100 -->
            
            <div class="lpay_tabs_content d-none w-100" data-target='topupReport'>
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="ab-merchant">Merchant</label>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-id="lpay-input"
                                    data-topup='payId'
                                    class="selectpicker"
                                    id="tr-merchant"
                                    headerKey="ALL"
                                    data-live-search="true"
                                    headerValue="ALL"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    data-submerchant="tr-subMerchant"
                                    data-user="tr-subUser"
                                    autocomplete="off"
                                    data-name="tr-subMerchant"
                                />
                            </div>
                        </s:if>
                        <s:else>
                            <div class="position-relative">
                                <s:select
                                    name="payId"
                                    data-live-search="true"
                                    class="selectpicker"
                                    data-submerchant="tr-subMerchant"
                                    data-user="tr-subUser"
                                    data-topup='payId'
                                    id="tr-merchant"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                    data-name="tr-subMerchant"
                                    onchange="validateInputField(this);"
                                />
                            </div>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->

                <s:if test="%{#session['USER'].superMerchant == true}">
				    <div class="col-md-3 mb-20" data-target="tr-subMerchant">
					    <div class="lpay_select_group">
                            <label for="ab-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <s:select
                                    data-topup='subMerchantId'
                                    name="subMerchant"
                                    class="selectpicker"
                                    id="tr-subMerchant"
                                    list="subMerchantList"
                                    listKey="emailId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                            </div>
					    </div>
					    <!-- /.lpay_select_group -->  
				    </div>
				    <!-- /.col-md-3 -->	
			    </s:if>
			    <s:else>
				    <div class="col-md-3 mb-20 d-none" data-target="tr-subMerchant"> 
					    <div class="lpay_select_group">
                            <label for="ab-subMerchant">Sub Merchant</label>
                            <div class="position-relative">
                                <select data-topup='subMerchantId' name="subMerchant" id="tr-subMerchant"></select>
                               
                            </div>
					    </div>
				        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
			    </s:else>

                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Payment Method</label>
                       <s:select headerKey="ALL" data-topup='paymentType' data-live-search="true" data-download="paymentType" data-var="paymentMethod" headerValue="ALL" class="selectpicker"
                       list="@com.paymentgateway.commons.util.PaymentType@values()"
                       listValue="name" listKey="code" name="paymentType"
                       id="paymentMethod" autocomplete="off" value="" />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 mb-20 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date From</label>
                        <input type="text" data-topup='dateFrom' readonly class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date To</label>
                        <input type="text" data-topup='dateTo' readonly class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Txn Type</label>
                       <select class="selectpicker" data-download="txnType" data-topup="txnType" name="txnType" id="txnType">
                            <option value="ALL">ALL</option>
                            <option value="payIn">Pay In</option>
                            <option value="payOut">Pay Out</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">RRN Search</label>
                        <input type="text" data-topup='rrnSearch' class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id='topup-view'>
                        View
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary --> 
                    <button class="lpay_button lpay_button-md lpay_button-primary" id="topup-downloand">
                        Download
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                </div>
                <!-- /.col-md-12 -->

                <div class="col-md-12 lpay_table_style-2 mt-10">
                    <div class="lpay_table">
                        <table id="topupReport-table" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Merchant Name</th>
                                    <th>Sub-Merchant Name</th>
                                    <th>Payment Method</th>
                                    <th>Mop Type</th>
                                    <th>Amount</th>
                                    <th>Create Date</th>
                                    <th>Status</th>
                                </tr>
                            </thead>	
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->

            </div>
            <!-- /.lpay_tabs_content d-none w-100 -->

        </div>
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

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

    <s:form id="topup_download" action="downloadTopUpTxnReportAction">

    </s:form>


    <s:form id="downloadBeneReport" action="downloadBeneReport">
        <s:hidden name="payId" id="benePayId"></s:hidden>
        <s:hidden name="bankAccountNumber" id="beneAccountNumber"></s:hidden>
        <s:hidden name="payeeType" id="benePayeeType"></s:hidden>
        <s:hidden name="status" id="beneStatus"></s:hidden>
        <s:hidden name="dateFrom" id="beneDateFrom"></s:hidden>
        <s:hidden name="dateTo" id="beneDateTo"></s:hidden>
        <s:hidden name="subMerchant" id="beneSubMerchant"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <s:form id="downloadTranReport" action="downloadTranReport">
        <s:hidden name="payId" id="tranpayId"></s:hidden>
        <s:hidden name="subMerchant" id="tranSubMerchantPayId"></s:hidden>
        <s:hidden name="bankAccountNumber" id="tranAccountNumber"></s:hidden>
        <s:hidden name="payeeType" id="tranPayeeType"></s:hidden>
        <s:hidden name="txnId" id="tranTxnId"></s:hidden>
        <s:hidden name="utrNo" id="tranUtr"></s:hidden>
        <s:hidden name="status" id="tranStatus"></s:hidden>
        <s:hidden name="dateFrom" id="tranDateFrom"></s:hidden>
        <s:hidden name="dateTo" id="tranDateTo"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <s:form id="downloadAccountStatement" action="downloadAccountStatement">
        <s:hidden name="dateFrom" id="statementDateFrom"></s:hidden>
        <s:hidden name="dateTo" id="statementDateTo"></s:hidden>
        <s:hidden name="fileName" id="statementFileName"></s:hidden>
        <s:hidden name="downloadFileOf" id="downloadFileOfInput"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <s:form id="downloadBulkFile" action="downloadBulkNodalFile">
        <s:hidden name="downloadFileOf" id="bulk-downloadFileOf"></s:hidden>
        <s:hidden name="fileName" id="bulk-fileName"></s:hidden>
    </s:form>

    <script src="../js/nodalTransfer.js"></script>
    <script src="../js/horizontal-scrolling-nav.js"></script>
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