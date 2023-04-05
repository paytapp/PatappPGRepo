<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Booking Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/commanValidate.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
<script src="../js/user-script.js"></script>
<script src="../js/bookingRecord.js"></script>
<script src="../js/tabs.js"></script>

<style>
    .lp-success_generate, .lp-error_generate {
		background-color: #c0f4b4;
		font-size: 15px;
		padding: 10px;
		text-align: center;
		margin-top: 20px;
		border-radius: 5px;
		border: 1px solid #3b9f24;
	}

	.lp-error_generate{
		background-color: #f79999;
    	border: 1px solid #771313;
	}

	.lp-success_generate p{ 
		color: #326626;
	}

	.lp-error_generate p{
		color: #921919;
	}
</style>

</head>
<body id="mainBody">
    <s:if test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId == null}">
		<s:hidden id="userMerchant" />
	</s:if>
    
	<s:form id="dis" action= "dispatchSlipPdfDownload">
		<s:hidden name="orderId" value="1234567890"></s:hidden>
	</s:form>
    <a href="" id="dispatchLink" download></a>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
    <input type="hidden" id="dispatchPayId">
	<input type="hidden" id="setGlobalData">
	<input type="hidden" id="pdfDownloadFlag">
    <input type="hidden" id="setSuperMerchant">
    <input type="hidden" id="dispatchSlipFlag">
    <input type="hidden" id="retailMerchantFlag">
    <section class="booking-record lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Booking Report Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            
            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Merchant</label>
                        <s:select
                            name="merchantEmailId"
                            data-var="merchantEmailId"
                            class="selectpicker adminMerchants"
                            id="merchant"
                            headerKey="ALL"
                            data-submerchant="subMerchant" 
						    data-user="subUser"
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
            <s:elseif test="%{superMerchant == true}">
                <div class="col-md-3 mb-20 d-none">
                    <div class="lpay_select_group">
                        <label for="">Merchant</label>
                        <s:select
                            name="merchantEmailId"
                            data-var="merchantEmailId"
                            class="selectpicker"
                            id="merchant"
                            list="merchantList"
                            listKey="emailId"
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
                            name="merchantEmailId"
                            data-var="merchantEmailId"
                            class="selectpicker"
                            id="merchant"
                            list="merchantList"
                            listKey="emailId"
                            listValue="businessName"
                            autocomplete="off"
                        />
                    </div>
                </div>
            </s:else>

            <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
                <div class="col-md-3 mb-20" data-target="subMerchant">
                    <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <s:select
                            data-var="subMerchantEmailId"
                            data-id="subMerchant"
                            data-submerchant="subMerchant" 
						    data-user="subUser"
                            name="subMerchantEmailId"
                            class="selectpicker"
                            id="subMerchant"
                            list="subMerchantList"
                            listKey="emailId"
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
                <div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
                    <div class="lpay_select_group">
                       <label for="">Sub Merchant</label>
                       <select name="subMerchantEmailId" data-submerchant="subMerchant" 
                       data-user="subUser" data-var="subMerchantEmailId"  id="subMerchant" class=""></select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->                         
            </s:else>
            <s:if test="%{#session.SUBUSERFLAG == true}">
				<div class="col-md-3 mb-20" data-target="subUser">
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <s:select data-id="subUser" headerKey="ALL" headerValue="ALL" data-var="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subUser"> 
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <select name="subUserPayId" data-var="subUserPayId" id="subUser" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
            </s:else>
            <!-- col-md-3  -->
            <div class="col-md-3 mb-20">
                <div class="lpay_select_group">
                   <label for="">Payment Method</label>
                   <s:select headerKey="ALL" data-var="paymentType" headerValue="ALL" class="selectpicker"
                    list="@com.paymentgateway.commons.util.PaymentType@values()"
                    listValue="name" listKey="code" name="PaymentType"
                    id="paymentMethod" autocomplete="off" value="" />
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                  <label for="">Date From</label>
                  <s:textfield type="text" onchange="dateBaseDownload()" data-var="dateFrom" id="dateFrom" name="dateFrom"
                      class="lpay_input" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
              </div>
              <!-- /.col-md-3 -->
              <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                  <label for="">Date To</label>
                  <s:textfield type="text" onchange="dateBaseDownload()" id="dateTo" data-var="dateTo" name="dateTo"
                  class="lpay_input" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
              </div>
              <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20 slide-form-element" id="txn-pgRefNum">
              <div class="lpay_input_group">
                <label for="">PG REF Number</label>
                <s:textfield id="pgRefNum" data-var="transactionId" class="lpay_input blank-space" name="transactionId"
                type="text" value="" autocomplete="off"
                onkeypress="javascript:return isNumber (event)" maxlength="16"
                onblur="dateBaseDownload()"></s:textfield> 
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 mb-20 -->
            <div class="col-md-3 mb-20 slide-form-element">
              <div class="lpay_input_group">
                <label for="">Order Id</label>
                <s:textfield id="orderId" data-var="orderId" class="lpay_input blank-space" name="orderId"
                type="text" value="" autocomplete="off"
                onkeypress="return Validate(event);"
                onblur="this.value=removeSpaces(this.value);dateBaseDownload()"></s:textfield>
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20 slide-form-element">
              <div class="lpay_input_group">
                <label for="">Customer Email</label>
                <s:textfield id="custEmail" data-var="custEmail" class="lpay_input blank-space"
                name="custEmail" type="text" value="" autocomplete="off"
                onblur="validateEmail(this);"></s:textfield>
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20 slide-form-element">
                <div class="lpay_select_group">
                   <label for="">Currency</label>
                   <s:select name="currency" data-var="currency" id="currency" headerValue="ALL"
                   headerKey="ALL" list="currencyMap" class="selectpicker" />
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20 slide-form-element">
                <div class="lpay_select_group">
                   <label for="">Settlement Type</label>
                   <s:select headerKey="ALL" data-var="partSettleFlag" headerValue="ALL" class="selectpicker"
                   list="#{'N':'Normal','Y':'Part'}" name="partSettleFlag" id = "partSettleFlag" />
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->
            
            <div class="col-md-12 text-center">
                <input type="button" id="submit" value="View" class="lpay_button lpay_button-md lpay_button-secondary">
                <input type="button" id="downloadSubmit" value="Download" onclick = "downloadSubmit(this)" class="lpay_button lpay_button-md lpay_button-primary">
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 d-none">
                <div class="lp-success_generate">
                    <p>Your file has been generate successfully please see after some time</p>
                </div>
                <!-- /.lp-success_generate -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 d-none">
                <div class="lp-error_generate">
                    <p>Please try again after some time</p>
                </div>
                <!-- /.lp-success_generate -->
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
        <div class="filter-icon">
			<span class="fa fa-angle-down"></span> 
		</div>
		<!-- /.filter-icon -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

    <section class="booking-record lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Booking Report Data</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="txnResultDataTable" class="" cellspacing="0"
                    width="100%">
                    <thead>
                        <tr class="lpay_table_head">
                            <th>Merchant</th>
                            <th>PG REF Num</th>
                            <th>Date</th>
                            <th>Order Id</th>
                            <th>Payment Method</th>
                            <th>Total Amount</th>
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

    

    <s:form name="chargeback" action="chargebackAction">
        <s:hidden name="orderId" id="orderIdc" value="" />
        <s:hidden name="payId" id="payIdc" value="" />
        <s:hidden name="refundedAmount" id="chargeback-refundedAmount" value="" />
        <s:hidden name="pgRefNum" id="chargeback-pgRefNum" value="" />
        <s:hidden name="refundAvailable" id="chargeback-refundAvailable" value="" />
        <s:hidden name="txnId" id="txnIdc" value="" />
        
        <s:hidden name="token" value="%{#session.customToken}" />
    </s:form> 

    <s:form name="refundDetails" action="refundConfirmAction">
        <s:hidden name="orderId" id="orderIdr" value="" />
        <s:hidden name="payId" id="payIdr" value="" />
        <s:hidden name="transactionId" id="txnIdr" value="" />
        <s:hidden name="amount" id="amountr" value="" />
        <s:hidden name="totalAmount" id="totalAmountr" value="" />
        
        <s:hidden name="token" value="%{#session.customToken}" />
    </s:form>

    <script type="text/javascript">
        $(document).ready(function() {
            $('#closeBtn').click(function() {
                $('#popup').hide();
            });
        });
    </script>

    <script>
        function removeSpaces(string) {
            return string.split(' ').join('');
        }
    </script>

    <s:form name="manualRefundProcess" id="manualRefundProcess" action="manualRefundProcess">
        <s:hidden name="payId" id="payId" value="" />
        <s:hidden name="pgRefNum" id="pg-ref" value="" />
        <s:hidden name="refundedAmount" id="refundedAmount" value="" />
        <s:hidden name="refundAvailable" id="refundAvailable" value="" />

        <s:hidden name="token" value="%{#session.customToken}" />
    </s:form>

    <s:form name="bookingDownloadForm" id="bookingDownloadForm" action="downloadBookingRecordReportAction">
        <s:hidden name="pgRefNum" id="pgRefNumForm" value="" />
        <s:hidden name="subMerchantPayId" id="subMerchantPayId" />
        <s:hidden name="orderId" id="orderIdForm" value="" />
<%--         <s:hidden name="custId" id="custIdForm" value="" /> --%>
        <s:hidden name="merchantPayId" id="merchantForm" value="" />
        <s:hidden name="paymentMethod" id="paymentMethodForm" value="" />
        <s:hidden name="currency" id="currencyForm" value="" />
        <s:hidden name="partSettleFlag" id="partSettleFlagForm" value="" />
        <s:hidden name="dateFrom" id="dateFromForm" value="" />
        <s:hidden name="dateTo" id="dateToForm" value="" />
        <s:hidden name="custMobile" id="custMobileDownload" value="" />
        <s:hidden name="custEmail" id="custEmailDownload" value="" />
        <s:hidden name="subUserPayId" id="subUserPayIdForm" value="" />
<%--         <s:hidden name="SKUCode" id="SKUCodeForm" value="" /> --%>
<%--         <s:hidden name="categoryCode" id="categoryCodeForm" value="" /> --%>
        
        <s:hidden name="token" value="%{#session.customToken}" />
    </s:form>
</body>
</html>