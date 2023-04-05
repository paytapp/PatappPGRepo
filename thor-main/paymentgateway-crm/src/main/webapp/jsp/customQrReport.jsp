<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Customer QR Report</title>
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

  
</head>
<body>
    <s:hidden id="user_typ" value="%{#session.USER.UserType.name()}"></s:hidden>

	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Static UPI QR Report</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="row m-0" data-active="customQrReport">
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
                            <div class="lpay_select_group">
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

                <s:if test="%{(#session['USER'].superMerchant == true || superMerchant == true) || (#session.USER.UserType.name()=='SUBUSER' && #session['USER'].subUserType == 'normalType' && superMerchantFlag == true)}">
                    <div class="col-md-3 mb-20" data-target="subMerchantComposite">
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <s:select
                                data-id="subMerchant"
                                data-download="subMerchantPayId"
                                data-var="subMerchantId"
                                data-submerchant="subMerchantComposite"
                                data-user="subUser"
                                name="subMerchantEmailId"
                                class="selectpicker"
                                id="subMerchantComposite"
                                list="subMerchantsForQr"
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
                    <div class="col-md-3 mb-20 d-none" data-target="subMerchantComposite"> 
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <select name="subMerchantId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantId" data-submerchant="subMerchantComposite" data-user="subUser" id="subMerchantComposite" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                </s:else>

                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_select_group">
                        <label for="">Status</label>
                        <select data-var="status" name="status" class="selectpicker" id="status">
                            <option value="ALL">ALL</option>
                            <option value="Active">Active</option>
                            <option value="InActive">InActive</option>
                        </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                
                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_input_group">
                        <label for="">Customer Account Number</label>
                        <input type="text" data-var="customerAccountNumber" name="customerAccountNumber" id="customerAccountNumber" class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20 single-account-input">
                    <div class="lpay_input_group">
                        <label for="">Customer Id</label>
                        <input type="text" data-var="customerId" name="customerId" id="customerId" class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
            </div>
            <!-- /.col-md-4 -->
    
            <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="view">View</button>
                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                <button class="lpay_button lpay_button-md lpay_button-primary" id="download">Download</button>
                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="compositeReportTabel" class="display" cellspacing="0" width="100%">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Merchant Name</th>
                                <th>Pay Id</th>
                                <th>Date</th>
                                <th>Customer Account Number</th>
                                <th>Customer Id</th>
                                <th>VPA</th>
                                <th>Status</th>
                                <th>UPI QR Code</th>
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
    <form action="downloadCustomerQRReport" id="downloadComposite"></form>

    <form action="downloadStaticUpiQrPdf" id="downloadStaticQr">
        <input type="hidden" name="payId" id="qr-payId">
        <input type="hidden" name="customerAccountNumber" id="qr-customerAccountNumber">
        <input type="hidden" name="customerId" id="qr-customerId">
    </form>
    
	<script src="../js/customQrReport.js"></script>
</body>
</html>