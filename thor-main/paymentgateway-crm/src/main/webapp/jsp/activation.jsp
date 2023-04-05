<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Activation</title>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <script src="../js/jquery-latest.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/daterangepicker.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
    <script type="text/javascript" src="../js/pdfmake.js"></script>
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/bootstrap-select.min.js"></script>

    <style>
        .status .lpay_select_group { margin-top: -20px; }
        .lpay_table .dataTables_filter { display: block !important; }
        .status .lpay_select_group .bootstrap-select { background-color: #fff !important; }    
        .save-mode { display: none; }
        .edit-tr .edit-mode { display: none; }
        .edit-tr .save-mode { display: block; }
        .lpay_table { white-space: nowrap; }
        /* .lpay_table .dataTables_wrapper { overflow-y: inherit !important; } */
        .status .lpay_select_group{ display: flex;justify-content: space-between; }
        .status .lpay_select_group .wwgrp{ width: 49%; }
        .d-inline-block { display: inline-block !important; }
        .download-userlist:focus{ color: #fff; }
        .lpay_table .dataTables_wrapper{ overflow-y: visible !important; }
    </style>
    <link rel="stylesheet" href="../css/horizontal-scrolling-nav.css">
    <link rel="stylesheet" href="../css/common-style.css">
</head>
<body class="bodyColor">
    <div class="edit-permission"><s:property value="%{editingpermission}"/></div>
    <!-- /.edit-permission -->
    <div class="col-md-4 d-none" id="userStatus">
        <div class="lpay_select_group">
            <s:select
                class="selectpicker"
                list="@com.paymentgateway.commons.util.UserStatusType@values()"
                id="status"
                name="userStatus"
                value="%{MPAData.userStatus}"
            />
            <s:select
                class="selectpicker" 
                list="@com.paymentgateway.commons.util.ModeType@values()"
                id="processingmode"
                name="modeType"
                value="%{MPAData.modeType}"
            />
        </div>
        <!-- /.lpay_select_group -->
    </div>
    <!-- /.col-md-4 mb-20 -->
    <s:hidden id="setSuperMerchant"></s:hidden>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Users Details</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="horizontal-nav-wrapper mb-20">
                    <nav id="horizontal-nav" class="horizontal-nav">
                        <ul class="horizontal-nav-content lpay_tabs list-unstyled font-size-14 merchant-config-tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
                            <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                                <li class="lpay-nav-item">
                                    <a href="#" class="lpay-nav-link" data-active="listReseller" data-type="RESELLER" id="resellerList" onclick="tabChange('resellerList')" data-id="resellerList">Reseller List</a>
                                </li>
                            </s:if>
                            <li class="lpay-nav-item">
                                <a href="#" class="lpay-nav-link" data-active="listMerchant" data-type="MERCHANT" onclick="tabChange('merchantList')" id="merchantList"data-id="merchantList">Merchant List</a>
                            </li>
                            <li class="lpay-nav-item">
                                <a href="#" class="lpay-nav-link" data-active="listSuperMerchant" data-type="SUPER_MERCHANT" onclick="tabChange('superMerchantList')" id="superMerchantList" data-id="superMerchantList">Super Merchant List</a>
                            </li>
                            <li class="lpay-nav-item">
                                <a href="#" class="lpay-nav-link" data-active="listSubMerchant" data-type="SUB_MERCHANT" onclick="tabChange('subMerchantList')" id="subMerchantList" data-id="subMerchantList">Sub-Merchant List</a>
                            </li>
                            <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                                <li class="lpay-nav-item">
                                    <a href="#" class="lpay-nav-link" data-active="listSubUser" data-type="SUBUSER" onclick="tabChange('subUserList')" id="subUserList" data-id="subUserList">Sub-User List</a>
                                </li>
                            </s:if>
                            <s:if test="%{#session.USER.UserType.name() != 'RESELLER'}">
                                <li class="lpay-nav-item">
                                    <a href="#" class="lpay-nav-link" data-active="listParentMerchant" data-type="PARENTMERCHANT" onclick="tabChange('parentMerchantList')" id="parentMerchantList" data-id="parentMerchantList">Parent Merchant List</a>
                                </li>
                            </s:if>
                        </ul>
                    </nav>
                    <button type="button" id="btn-scroll-left" class="horizontal-nav-btn horizontal-nav-btn-left m-0 bg-color-white pr-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
							<path d="M445.44 38.183L-2.53 512l447.97 473.817 85.857-81.173-409.6-433.23v81.172l409.6-433.23L445.44 38.18z" />
						</svg>
					</button>
					<button type="button" id="btn-scroll-right" class="horizontal-nav-btn horizontal-nav-btn-right m-0 bg-color-white pl-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
							<path d="M105.56 985.817L553.53 512 105.56 38.183l-85.857 81.173 409.6 433.23v-81.172l-409.6 433.23 85.856 81.174z" />
						</svg>
					</button>
                </div>
            </div>
            <!-- /.col-md-12 -->

            <div class="lpay_tabs_content w-100" data-active="true" data-target="resellerList">
                <div class="col-md-12">
                    <a href="#" class="lpay_button lpay_button-sm lpay_button-secondary pointer ml-0 d-inline-block download-userlist" style="margin-left: 0 !important;" data-type="RESELLER">Download</a>
                    <div class="lpay_table">
                        <table id="resellerListDatatable" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th>Pay Id</th>
                                    <th>Business Name</th>
                                    <th>Email</th>
                                    <th>Reseller Type</th>
                                    <th>Status</th>
                                    <th>Transaction Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- lpay_table	 -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content w-100" data-target="merchantList">
                <div class="col-md-12 lpay_table_style-2">
                    <a href="#" class="lpay_button lpay_button-sm lpay_button-secondary pointer ml-0 d-inline-block download-userlist" style="margin-left: 0 !important;" data-type="MERCHANT">Download</a>
                    <div class="lpay_table">
                        <table id="merchantListDatatable" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Pay Id</th>
                                    <th>Business Name</th>
                                    <th>Email</th>
                                    <th>Merchant</th>
                                    <th style="width: 200px;">Status</th>
                                    <th>Transaction Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- lpay_table	 -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="superMerchantList">
                <div class="col-md-12">
                    <a href="#" class="lpay_button lpay_button-sm lpay_button-secondary pointer ml-0 d-inline-block download-userlist" style="margin-left: 0 !important;" data-type="SUPER_MERCHANT">Download</a>
                    <div class="lpay_table">
                        <table id="superMerchantListDatatable" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th>Pay Id</th>
                                    <th>Super Merchant</th>
                                    <th>Email</th>
                                    <th>Reseller Name</th>
                                    <th>Status</th>
                                    <th>Transaction Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- lpay_table	 -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="subMerchantList">
                <div class="col-md-12">
                    <a href="#" class="lpay_button lpay_button-sm lpay_button-secondary pointer ml-0 d-inline-block download-userlist" style="margin-left: 0 !important;" data-type="SUB_MERCHANT">Download</a>
                    <div class="lpay_table">
                        <table id="subMerchantListDatatable" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th>Pay Id</th>
                                    <th>Business Name</th>
                                    <th>Email</th>
                                    <th>Super Merchant Name</th>
                                    <th>Status</th>
                                    <th>Transaction Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- lpay_table	 -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content d-none w-100" data-target="subUserList">
                <div class="col-md-12">
                    <a href="#" class="lpay_button lpay_button-sm lpay_button-secondary pointer ml-0 d-inline-block download-userlist" style="margin-left: 0 !important;" data-type="SUBUSER">Download</a>
                    <div class="lpay_table">
                        <table id="subUserListDatatable" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th>Pay Id</th>
                                    <th>Business Name</th>
                                    <th>Email</th>
                                    <th>Parent Name</th>
                                    <th>Status</th>
                                    <th>Transaction Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- lpay_table	 -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->
            <div class="lpay_tabs_content w-100" data-target="parentMerchantList">
                <div class="col-md-12 lpay_table_style-2">
                    <a href="#" class="lpay_button lpay_button-sm lpay_button-secondary pointer ml-0 d-inline-block download-userlist" style="margin-left: 0 !important;" data-type="PARENTMERCHANT">Download</a>
                    <div class="lpay_table">
                        <table id="parentMerchantListDatatable" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Pay Id</th>
                                    <th>Business Name</th>
                                    <th>Email</th>
                                    <th>Merchant</th>
                                    <th style="width: 200px;">Status</th>
                                    <th>Transaction Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- lpay_table	 -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->
        </div>
        <!-- /.row -->
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
                        <h3 class="responseMsg">Data has been saved successfully.</h3>
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
                        <h3 class="responseMsg">Something went wrong.</h3>
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

    <s:form id="downloadUserList" action="downlodUserList">
        <s:hidden name="userType" id="list-userType"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <s:form id="downloadReport" action="downloadReport">
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
        <s:hidden name="reportStatus" id="reportStatus"></s:hidden>
        <s:hidden name="reportDateFrom" id="reportDateFrom"></s:hidden>
        <s:hidden name="reportDateTo" id="reportDateTo"></s:hidden>
        <s:hidden name="reportSubMerchant" id="reportSubMerchant"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <script src="../js/activation.js"></script>
    <script src="../js/horizontal-scrolling-nav.js"></script>
</body>
</html>