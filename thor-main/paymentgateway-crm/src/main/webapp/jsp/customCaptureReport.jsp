<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Custom Capture Report</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
<script src="../js/custom-capture-report.js"></script>
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
<!-- /.edit-permission -->
<body class="bodyColor">
    <s:if test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId == null}">
		<s:hidden id="userMerchant" />
	</s:if>

    <s:hidden id="setSuperMerchant"></s:hidden>
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Custom Capture Report Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            
            
            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Merchant</label>
                        <s:select
                            name="merchant"
                            data-id="reportMerchant"
                            data-live-search="true"
                            class="selectpicker lpay-input"
                            id="merchantReportPayId"
                            headerKey=""
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
                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Merchant</label>
                            <s:select
                                name="merchant"
                                data-id="reportMerchant"
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
                <s:elseif test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Merchant</label>
                            <s:select
                                name="merchant"
                                data-id="reportMerchant"
                                data-live-search="true"
                                class="selectpicker lpay-input"
                                id="merchantReportPayId"
                                list="subMerchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                            />
                        </div>
                    </div>
                </s:elseif>
                <s:elseif test="%{superMerchant == true}">
                    <div class="col-md-3 mb-20 d-none">
                        <div class="lpay_select_group">
                            <label for="">Merchant</label>
                            <s:select
                                name="merchant"
                                data-id="reportMerchant"
                                data-live-search="true"
                                class="selectpicker lpay-input"
                                id="merchantReportPayId"
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
                                data-id="reportMerchant"
                                data-live-search="true"
                                class="selectpicker lpay-input"
                                id="merchantReportPayId"
                                list="merchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                            />
                        </div>
                    </div>
                </s:else>
            </s:else>

            <s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					    <s:select
                            data-id="subMerchant"
                            name="subMerchant"
                            class="selectpicker"
                            id="subMerchant"
							list="subMerchantList"
                            listKey="emailId"
                            headerValue="ALL"
                            headerKey="ALL"
							listValue="businessName"
                            autocomplete="off"
                        />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-id="submerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchant" id="subMerchant" data-id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">Date From</label> 
                    <s:textfield type="text" id="dateFrom" onchange="dateBaseDownload()" data-id="reportDateFrom" name="dateFrom"
                    class="lpay_input datepick" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 mb-20 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">Date To</label>
                    <s:textfield type="text" id="dateTo" onchange="dateBaseDownload()" data-id="reportDateTo" name="dateTo"
                    class="lpay_input datepick" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Payment Method</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.PaymentType@values()"
				   listValue="name" listKey="code" name="paymentMethod"
				   id="paymentMethod" data-id="reportPaymentMethod" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
            <div class="col-md-3 mb-20 slide-form-element" id="txn-pgRefNum">
				<div class="lpay_input_group">
					<label for="">PG REF Number</label>
					<s:textfield
						id="pgRefNum"
						data-id="reportPgRefNum"
						onkeyup="onlyAlphaNumeric(this)"
                        onblur="dateBaseDownload()"
						class="lpay_input"
						name="pgRefNum"
						type="text"
						autocomplete="off"
						maxlength="16">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
					<label for="">Order Id</label>
					<s:textfield
						id="orderId"
						data-id="reportOrderId"
						class="lpay_input"
						name="orderId"
						type="text"
                        onblur="dateBaseDownload()"
						autocomplete="off"
						onkeypress="return Validate(event);">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			
			
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
				   <label for="">Currency</label>
				   <s:select name="currency" id="currency" data-id="reportCurrency" headerValue="ALL"
					headerKey="" list="currencyMap" class="selectpicker" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<div class="col-md-3 mb-20 slide-form-element">
					<div class="lpay_select_group">
					   <label for="">Late Capture</label>
					   <s:select name="postSettleFlag" id="postSettleFlag" data-id="reportPostSettleFlag" headerValue="ALL"
					   headerKey="" list="#{'Y':'Yes','N':'No'}"
					 	class="selectpicker"/>
					</div>
					<!-- /.lpay_select_group -->  
			</div>
				
            
                <div class="col-md-12 mb-20 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary viewData">View</button>
                    <button class="lpay_button lpay_button-md lpay_button-primary downloadData" id="downloadButton">Download</button>
                    <div class="filter-icon filter-col">
                        <span class="fa fa-angle-down"></span> 
                    </div>
                    <!-- /.filter-icon -->
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
                            

                <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    
                    <table id="datatable" class="display" cellspacing="0" width="100%">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Merchant Name</th>
                                <th>Pg Ref Num</th>
                                <th>Order Id</th>
                                <th>Captured Date</th>
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
    <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />
    <s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>

    <s:form id="capturedDataDownload"  action="capturedDataDownload" >
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
    <!-- <s:hidden name="subMerchant" id="subMerchant"></s:hidden> -->
        <s:hidden name="reportPaymentMethod" id="reportPaymentMethod"></s:hidden>
        <s:hidden name="reportPgRefNum" id="reportPgRefNum"></s:hidden>
        <s:hidden name="reportOrderId" id="reportOrderId"></s:hidden>
        <s:hidden name="reportCurrency" id="reportCurrency"></s:hidden>
        <s:hidden name="reportPostSettleFlag" id="reportPostSettleFlag"></s:hidden>
        <s:hidden name="reportDateFrom" id="reportDateFrom"></s:hidden>
        <s:hidden name="reportDateTo" id="reportDateTo"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
        <s:hidden name="subMerchant" id="reportSubMerchant" value=""></s:hidden>
    </s:form>
    
    
</body>
</html>