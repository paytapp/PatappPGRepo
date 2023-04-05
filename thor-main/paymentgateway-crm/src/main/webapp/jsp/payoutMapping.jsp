<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Payout Mapping</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script type="text/javascript" src="../js/jquery.fancybox.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>

    <style>
        .imp { color: #f00;margin-right: 5px; }
        .error-field{ position: absolute;top: 0;right: -15px;color: #f00;opacity: 0; }
        .hasError .error-field{ opacity: 1;right: 0;transition: all .5s ease; }
        .adf-section input[readonly]{ background-color: #f8f8f8; }
        .action-btn{ display: flex; }
        .action-btn span{ width: 35px;height: 35px;display: inline-block;line-height: 35px;text-align: center;font-size: 14px;border-radius: 4px;margin-right: 5px;box-shadow: 0 0 5px rgba(0,0,0,.2);background-color: #fff;cursor: pointer; }
        .action-btn .dlt-button { background-color: #d88383;color: #fff; }

    </style>

</head>
<body>

    
	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Mapping</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active" onclick="tabShow(this, 'acquirerMapping')">
                        <a href="#" class="lpay-nav-link" data-id="acquirerMapping">Acquirer Mapping</a>
                    </li>
                    <li class="lpay-nav-item" onclick="tabShow(this, 'merchantMapping')">
                        <a href="#" class="lpay-nav-link" data-id="merchantMapping">Merchant Mapping</a>
                    </li>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100" data-target="acquirerMapping">
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group check-blank">
                       <label for="">Bank Name <span class="imp">*</span></label>
                       <s:select 
                            class="selectpicker"
                            onchange='removeErrorClass(this);handleChangeBankName(this);handleChangePayout()'
                            data-download="bankName"
                            data-var="bankName"
                            name="bankName" 
                            id="bankName"
                            headerKey="" 
                            headerValue="Select Bank Type"
                            list="@com.paymentgateway.commons.user.PayoutAcquirer@values()" 
                            listKey="name()" 
                            listValue="name" 
                            autocomplete="off"
                       />
                            <!-- <option value="">Select Bank</option>
                            <option value="iciciBank">ICICI Bank</option>
                           <option value="yesBank">YES Bank</option> -->
                       
                       <span class="error-field">Field should not be blank</span>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group check-blank">
                        <label for="">User Type <span class="imp">*</span></label>
                        <s:select 
                            name="payId"
                            data-download="userType" 
                            class="selectpicker"
                            id="userType" 
                            headerKey="" 
                            data-var="userType"
                            onchange='handleChangePayout();removeErrorClass(this);' 
                            data-live-search="true" 
                            headerValue="Select User Type"
                            list="@com.paymentgateway.commons.util.PayoutUserType@values()" 
                            listKey="name" 
                            listValue="name" 
                            autocomplete="off"
                        />
                       <span class="error-field">Field should not be blank</span>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group check-blank">
                       <label for="">Account Type <span class="imp">*</span></label>
                       <select class="selectpicker" onchange='handleChangePayout();removeErrorClass(this)' data-download="accountType" data-var="accountType" name="accountType" id="accountType">
                            <option value="">Select Account Type</option>
                            <option value="Current">Current</option>
                           <option value="Nodal">Nodal</option>
                       </select>
                       <span class="error-field">Field should not be blank</span>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                
                <div class="adf-section">
    
                </div>
                <!-- /.adf-section -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary d-none" data-hide='hide' id='edit-fields' onclick="editFields()">Edit</button>
                    <button class="lpay_button lpay_button-md lpay_button-primary d-none" data-hide='hide' onclick="resetEdit()" id='cancel-fields'>Cancel</button>
                    <button class="lpay_button lpay_button-md lpay_button-secondary" onclick="addMoreFields()">
                        Add More Fields
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                    <button class="lpay_button lpay_button-md lpay_button-primary" onclick="saveMapping()">
                        Submit
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- lpay_tabs_content. -->
            <div class="lpay_tabs_content w-100 d-none" data-target='merchantMapping'>
                <s:if test="%{#session.USER.UserType.name()=='ADMIN'}">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant</label>
                            <s:select 

                                name="payId"
                                data-download="payId" 
                                class="selectpicker"
                                id="merchant" 
                                headerKey="ALL" 
                                data-var="payId" 
                                data-submerchant="subMerchant"
                                onchange="fetchMappingDetails(this)" 
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
                <div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group check-blank">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" onchange="fetchMappingDetails(document.querySelector('#merchant'))" data-var="subMerchantPayId" data-submerchant="subMerchant" id="subMerchant" class="selectpicker"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 d-none" data-payout='bankName'>
                    <div class="lpay_select_group check-blank">
                       <label for="">Bank Name</label>
                       <s:select 
                            class="selectpicker"
                            onchange='handleChangePayout();removeErrorClass(this);handleChangeBankName(this)'
                            data-download="bankNamePermission"
                            data-var="bankName"
                            name="bankNamePermission" 
                            id="bankNamePermission"
                            headerKey="" 
                            headerValue="Select Bank Name"
                            list="@com.paymentgateway.commons.user.PayoutAcquirer@values()" 
                            listKey="name()" 
                            listValue="name" 
                            autocomplete="off"
                       />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 d-none" data-payout='userType'>
                    <div class="lpay_select_group check-blank">
                       <label for="">User Type</label>
                       <s:select 
                            name="userTypePermission"
                            data-download="userTypePermission" 
                            class="selectpicker"
                            id="userTypePermission" 
                            headerKey="" 
                            data-var="userType" 
                            data-live-search="true" 
                            headerValue="Select User Type"
                            list="@com.paymentgateway.commons.util.PayoutUserType@values()" 
                            listKey="name" 
                            listValue="name" 
                            autocomplete="off"
                        />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 d-none" data-payout='accountType'>
                    <div class="lpay_select_group check-blank">
                       <label for="">Account Type</label>
                       <select class="selectpicker" data-download="accountTypeFilter" data-var="accountType" name="accountTypeFilter" id="accountTypeFilter">
                            <option value="">Select Account Type</option>
                            <option value="Current">Current</option>
                           <option value="Nodal">Nodal</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-12 mb-20 d-none text-center" data-payout='payoutButton'>

                    <button class="lpay_button lpay_button-md lpay_button-secondary" onclick="reset()" id='cancel-edit'>
                        Cancel
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->

                    <button onclick="merchantMapping()" class="lpay_button lpay_button-md lpay_button-primary">
                        Save Mapping
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                </div>
                <!-- /.col-md-12 -->

                <div class="col-md-12 lpay_table_style-2">
                    <div class="lpay_table">
                        <table id="payoutMapping_table" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Merchant PayID</th>
                                    <th>Merchant Name</th>
                                    <th>Sub-Merchant Name</th>
                                    <th>Sub-Merchant PayID</th>
                                    <th>Bank Name</th>
                                    <th>Virtual Account Number</th>
                                    <th>Virtual IFSC</th>
                                    <th>User Type</th>
                                    <th>Account Type</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
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
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" onclick="closePopup()" id="confirmButton1">Ok</button>
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
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" onclick="closePopup()" id="confirmButton2">Ok</button>
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
                <span>Do you really want to delete these record? This process cannot be undone.</span>
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
    
	<script src="../js/payoutMapping.js"></script>
</body>
</html>