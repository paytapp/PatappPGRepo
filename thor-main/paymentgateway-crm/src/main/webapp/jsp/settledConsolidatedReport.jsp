<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Net Settled Report</title>
        <link rel="icon" href="../image/favicon-32x32.png">
        <link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
        <link href="../css/jquery-ui.css" rel="stylesheet" />
        <link rel="stylesheet" href="../css/bootstrap-select.min.css">
        <script src="../js/jquery.min.js" type="text/javascript"></script>
        <script src="../js/jquery-ui.js"></script>
        <script src="../js/bootstrap-select.min.js"></script>
        <script src="../js/commanValidate.js"></script>
        <script src="../js/jquery.dataTables.js"></script>
        <script src="../js/common-scripts.js"></script>
        <script src="../js/user-script.js"></script>
        <style>
            #edit-button{ margin-left: 0 !important; }
            .lpay_table .dataTable tbody td{ padding: 8px 10px !important }
            tr{
                cursor: pointer;
            }
			.edit-tr .edit_button-div{
				display: none;
			}
			.edit-tr .save_button-div{
				display: flex;
			}
			.save_button-div {
				display: none;
			}
			.edit-tr .adjustment{
				display: flex;
			}
			.adjustment{ display: none; }
        </style>
        
    </head>
<body>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
    <section class="settled_consolidate-report lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Net Settled Report</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active">
                        <a href="#" class="lpay-nav-link" data-id="view">View</a>
                    </li>
                    <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" data-id="download">Download</a>
                    </li>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100" data-target="view">
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
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                                <s:select
                                    name="merchantEmailId"
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
                </s:else>
                <!-- /.col-md-3 -->							
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Payout Date</label>
                        <input type="text" data-var="dateFrom" class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="consolidate-view">view</button>
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 mt-10">
                    <div class="lpay_table">
                        <table id="consolidate-table" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Merchant</th>
                                    <th>Sub Merchant</th>
                                    <th>Capture Date From</th>
                                    <th>Capture Date To</th>
                                    <th>Payout Date</th>
                                    <th>Sale Capture (Txns)</th>
                                    <th>Sale Capture (Amount)</th>
                                    <th>Refund Capture (Txns)</th>
                                    <th>Refund Capture (Amount)</th>
                                    <th>Sale Settled (Txns)</th>
                                    <th>Sale Settled (Amount)</th>
                                    <th>Refund Settled (Txns)</th>
                                    <th>Refund Settled (Amount)</th>
                                    <th>Chargeback (Cr)</th>
                                    <th>Chargeback (Dr)</th>
                                    <th>Other Adjustments (Cr)</th>
                                    <th>Other Adjustments (Dr)</th>
                                    <th>Net Settled</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                </div>
                <!-- lpay_table_style-2 -->
            </div>
            <!-- lpay_tabs_content -->
            <div class="lpay_tabs_content w-100 d-none" data-target="download">
                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant</label>
                            <s:select
                                name="merchantEmailId"
                                data-download="merchantPayId"
                                data-down="payId"
                                class="selectpicker"
                                id="merchantDownload"
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
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                                <s:select
                                    name="merchantEmailId"
                                    data-download="merchantPayId"
                                    class="selectpicker"
                                    id="merchantDownload"
                                    headerKey="ALL"
                                    data-down="payId"
                                    data-submerchant="subMerchantDownload"
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
                                    data-down="payId"
                                    data-live-search="true"
                                    class="selectpicker"
                                    id="merchantDownload"
                                    list="merchantList"
                                    data-submerchant="subMerchantDownload"
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
                                data-down="subMerchantId"
                                data-submerchant="subMerchantDownload"
                                data-user="subUser"
                                name="subMerchantEmailId"
                                class="selectpicker"
                                id="subMerchantDownload"
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
                    <div class="col-md-3 mb-20 d-none" data-target="subMerchantDownload"> 
                        <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <select name="subMerchantEmailId" data-download="subMerchantDownload" headerValue="ALL" data-down="subMerchantId" data-submerchant="subMerchant" data-user="subUser" id="subMerchantDownload" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                </s:else>
                <!-- /.col-md-3 -->							
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Payout Date</label>
                        <input type="text" data-down="dateFrom" class="lpay_input datepick">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3" style="margin-top: 16px">
                    <button class="lpay_button lpay_button-md lpay_button-primary"  id="consolidateDownload-generate">Generate</button>
                    
                </div>
                <!-- /.col-md-12 -->
                <div class="clearfix"></div>
                <div class="col-md-12 mb-20">
                    <h4 class="heading_text p-15">Created File Date</h4>
                </div>
                <!-- /.clearfix -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Create Date</label>
                        <input type="text" data-filter="createDate" class="lpay_input datepick">
                    </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3" style="margin-top: 16px;">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="consolidateDownload-view">Refresh</button>
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-12 mt-10">
                    <div class="lpay_table">
                        <table id="consolidateDownload-table" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Create Date</th>
                                    <th>File Name</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                </div>
                <!-- lpay_table_style-2 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->
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

    <form action="downlodConsolidatedReport" id="consolidateDownloadForm">
        <s:hidden name="token" value="%{#session.customToken}" />
        <s:hidden name="createDate" id="payoutDate" />
        <s:hidden name="fileName" id="fileNameTable" />
    </form>
    <script src="../js/consolidated-script.js"></script>
</body>
</html>