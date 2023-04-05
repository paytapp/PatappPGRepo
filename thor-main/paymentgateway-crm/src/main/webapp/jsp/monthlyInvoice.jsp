<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<title>Invoice</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<!-- <script src="../js/jquery-latest.min.js" type="text/javascript"></script> -->
	<script src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/jquery-ui.js"></script>
	<link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/bootstrap-select.min.js"></script>
    <link rel="stylesheet" href="../css/singleAccount.css">
    <script src="../js/jquery.dataTables.js"></script>

    <style>
        .ui-datepicker-calendar{ display: none !important; }
        .ui-datepicker-header{ margin-bottom: 0 !important; }
        .lpay_popup-innerbox[data-type="GENERATING"] .cancel-button,
		.lpay_popup-innerbox[data-type="PROCESSING"] .cancel-button {
			display: none;
		}
        /* .lpay_table .dataTables_filter{ display: block !important; } */
        
    </style>

	

</head>
<body>

    <section class="invoice-page lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Monthly Invoice</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
             <!-- /.col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                <div class="col-md-3 mb-20 invoice-page-input">
                    <div class="lpay_select_group">
                    <label for="">Select Merchant</label>
                    <s:select name="merchantEmailId" data-download="merchantPayId" class="selectpicker"
                        id="merchant" headerKey="" data-var="payId" data-submerchant="subMerchant" data-user="subUser"  data-live-search="true" headerValue="Select Merchant"
                        list="merchantList" onchange="removeError(this)" listKey="payId"
                        listValue="businessName" data-required="true" autocomplete="off" />
                        <span class="error-field"></span>
                    </div>
                </div>
            </s:if>
            <s:else>
                <div class="col-md-3 mb-20 invoice-page-input d-none">
                    <div class="lpay_select_group ">
                        <label for="">Select Merchant</label>
                <s:select name="merchantEmailId" data-download="merchantPayId" data-var="payId" data-live-search="true" class="selectpicker" id="merchant"
                    list="merchantList" data-submerchant="subMerchant" data-user="subUser" listKey="payId"
                    listValue="businessName" onchange="removeError(this)" data-required="true" autocomplete="off" />
                    </div>
                    <span class="error-field"></span>
                    </div>
            </s:else>
            <s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20 invoice-page-input" data-target="subMerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" data-required="true" data-download="subMerchantPayId" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser"  name="subMerchantPayId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" headerValue="ALL"
							listValue="businessName" onchange="removeError(this)" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
                    <span class="error-field"></span>
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 invoice-page-input d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantPayId" onchange="removeError(this)" data-required="true" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
                    <span class="error-field"></span>
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">Invoice Month</label>
                    <input type="text" onclick="removeError(this);" oninput="removeError(this)" id="month" data-required="true" readonly="readonly" data-var="date" class="lpay_input month">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">Invoice Number</label>
                    <input type="text" value="21-22/" oninput="removeError(this);" onkeypress="mzOnlyNumbers(event);" data-regex="[0-9]{2}[-]{1}[0-9]{2}[/]{1}[0-9]{3}" onchange="checkRegEx(this)" maxlength="9" id="invoiceNumber" data-required="true" data-var="invoiceNo" class="lpay_input">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">HSN/SAC Number</label>
                    <input type="text" maxlength="20"  oninput="removeError(this);mzLettersAndAlphabet(event)" data-required="true" id="hsrNumber" data-var="hsnSac" class="lpay_input">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-primary m-0" id="download-btn" onclick="generateFile()">Generate File</button>
            </div>
            <!-- /.col-md-3 -->
            
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <section class="monthly-reporting lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Monthly Invoice Data</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                <div class="col-md-3 mb-20 invoice-page-input">
                    <div class="lpay_select_group">
                    <label for="">Select Merchant</label>
                    <s:select name="merchantEmailId" data-download="merchantPayId" class="selectpicker"
                        id="merchantReporting" headerKey="ALL" data-report="payId" data-submerchant="subMerchantReporting" data-user="subUser"  data-live-search="true" headerValue="ALL"
                        list="merchantList" onchange="removeError(this)" listKey="payId"
                        listValue="businessName" data-required="true" autocomplete="off" />
                        <span class="error-field"></span>
                    </div>
                </div>
            </s:if>
            <s:else>
                <div class="col-md-3 mb-20 invoice-page-input d-none">
                    <div class="lpay_select_group ">
                        <label for="">Select Merchant</label>
                <s:select name="merchantEmailId" data-download="merchantPayId" data-report="payId" data-live-search="true" class="selectpicker" id="merchantReporting"
                    list="merchantList" data-submerchant="subMerchantReporting" data-user="subUser" listKey="payId"
                    listValue="businessName" onchange="removeError(this)" data-required="true" autocomplete="off" />
                    </div>
                    <span class="error-field"></span>
                    </div>
            </s:else>
            <s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20 invoice-page-input" data-target="subMerchantReporting">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" data-required="true" data-download="subMerchantPayId" data-report="subMerchantPayId" data-submerchant="subMerchantReporting" data-user="subUser"  name="subMerchantPayId" class="selectpicker" id="subMerchantReporting" list="subMerchantList" listKey="payId" headerValue="ALL"
							listValue="businessName" onchange="removeError(this)" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
                    <span class="error-field"></span>
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 invoice-page-input d-none" data-target="subMerchantReporting"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantPayId" onchange="removeError(this)" data-required="true" data-download="subMerchantPayId" headerValue="ALL" data-report="subMerchantPayId" data-submerchant="subMerchantReporting" data-user="subUser" id="subMerchantReporting" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
                    <span class="error-field"></span>
				</div>
			</s:else>
            <!-- /.col-md-3 -->	
            <div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">Invoice Month From</label>
                    <input type="text" onclick="removeError(this);" oninput="removeError(this)" id="monthFrom" data-required="true" readonly="readonly" data-report="dateFrom" class="lpay_input month">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->	
            <div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">Invoice Month To</label>
                    <input type="text" onclick="removeError(this);" oninput="removeError(this)" id="monthTo" data-required="true" readonly="readonly" data-report="dateTo" class="lpay_input month">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">Invoice Number</label>
                    <input type="text" value="21-22/" oninput="removeError(this);" onkeypress="mzOnlyNumbers(event);" data-regex="[0-9]{2}[-]{1}[0-9]{2}[/]{1}[0-9]{3}" onchange="checkRegEx(this)" maxlength="9" id="invoiceNumberView" data-required="true" data-report="invoiceNo" class="lpay_input">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20 invoice-page-input">
                <div class="lpay_input_group">
                    <label for="">HSN/SAC Number</label>
                    <input type="text" maxlength="20"  oninput="removeError(this);mzLettersAndAlphabet(event)" data-required="true" id="hsrNumberView" data-report="hsnSac" class="lpay_input">
                    <span class="error-field"></span>
                </div>
                <!-- /.lpay_input_group -->
            </div>	
            <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="view-monthly" onclick="getInvoices()">View</button>
            </div>
            <!-- /.col-md-12 -->				

            <div class="col-md-12 lpay_table_style-2 ">
                <div class="lpay_table">
                    <table id="monthlyInvoice-table" class="display" cellspacing="0" width="100%">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Merchant</th>
                                <th>Sub Merchant</th>
                                <th>Creation Date</th>
                                <th>Invoice Month</th>
                                <th>Invoice Number</th>
                                <th>HSN/SAC Number</th>
                                <th>Filename</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

    <form method="POST" id="downloadInvoice" action="downloadMonthlyInvoice">

    </form>

    <div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">
                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg"></h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-secondary cancel-button">No</button>
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirm-button">Yes</button>
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
                        <h3 class="responseMsg"></h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton"></button>Ok</button>
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

    <script src="../js/common-validations.js"></script>
    <script src="../js/invoicePage.js"></script>

</body>
</html>