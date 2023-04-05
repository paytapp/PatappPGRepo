<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <title>Refund Captured</title>
    <link rel="icon" href="../image/favicon-32x32.png">
    <link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
    <link href="../css/jquery-ui.css" rel="stylesheet" />
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/jquery.min.js" type="text/javascript"></script>
    <script src="../js/daterangepicker.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/jquery.popupoverlay.js"></script>
    <script src="../js/dataTables.buttons.js" type="text/javascript"></script>
    <script src="../js/pdfmake.js" type="text/javascript"></script>
    <script src="../js/bootstrap-select.min.js"></script>
    <script src="../js/common-scripts.js"></script>
    <script src="../js/user-script.js"></script>
    <script src="../js/commanValidate.js"></script>
    <script src="../js/refundTransaction.js"></script>
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
            <s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
	        <s:hidden id="partnerFlag" value="%{#session.USER.partnerFlag}" />
            <input type="hidden" id="setSuperMerchant">
            <input type="hidden" id="retailMerchantFlag">
            <section class="refund-transaction lapy_section white-bg box-shadow-box mt-70 p20">
                <div class="row">
                    <div class="col-md-12">
                        <div class="heading_with_icon mb-30">
                            <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                            <h2 class="heading_text">Refund Transaction Filter</h2>
                        </div>
                        <!-- /.heading_icon -->
                    </div>
                    <!-- /.col-md-12 -->
                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                        <s:select data-live-search="true" data-download="merchantPayId" data-var="merchantEmailId" data-submerchant="subMerchant" data-user="subUser" name="merchantEmailId" class="selectpicker" id="merchant" headerKey="" headerValue="ALL" list="merchantList" listKey="emailId" listValue="businessName" autocomplete="off" />
                        </div>
                        </div>
                    </s:if>
                    <s:else>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Select Merchant</label>
                        <s:select name="merchantEmailId" data-download="merchantPayId" data-var="merchantEmailId" data-submerchant="subMerchant" data-user="subUser" data-live-search="true" class="selectpicker" id="merchant" headerKey="" headerValue="ALL" list="merchantList" listKey="emailId" listValue="businessName" autocomplete="off" />
                        </div>
                        </div>
                        </s:if>
                        <s:else>
                            <div class="col-md-3 mb-20 d-none">
                                <div class="lpay_select_group">
                                    <label for="">Select Merchant</label>
                            <s:select name="merchantEmailId" data-download="merchantPayId" data-var="merchantEmailId" data-submerchant="subMerchant" data-user="subUser" data-live-search="true" class="selectpicker" id="merchant" list="merchantList" listKey="emailId" listValue="businessName" autocomplete="off" />
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
                                    data-var="subMerchantEmailId"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    data-live-search="true"
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
                                <select
                                    name="subMerchantEmailId"
                                    data-download="subMerchantPayId"
                                    data-var="subMerchantEmailId"
                                    data-submerchant="subMerchant"
                                    data-user="subUser"
                                    id="subMerchant">
                                </select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->								
                    </s:else>
                    <s:if test="%{#session.SUBUSERFLAG == true}">
                        <div class="col-md-3 mb-20" data-target="subUser">
                            <div class="lpay_select_group">
                                <label for="">Sub User</label>
                                <s:select data-id="subUser" data-download="subUserPayId" headerKey="ALL" headerValue="ALL" data-var="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->	
                    </s:if>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none" data-target="subUser"> 
                            <div class="lpay_select_group">
                                <label for="">Sub User</label>
                                <select name="subUserPayId" data-download="subUserPayId" data-var="subUserPayId" id="subUser" class=""></select>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->							
                    </s:else>
                    <!-- col-md-3 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Payment Method</label>
                           <s:select headerKey="" data-live-search="true" data-download="paymentType" data-var="paymentType" headerValue="ALL" class="selectpicker" list="@com.paymentgateway.commons.util.PaymentType@values()" listValue="name" listKey="code" name="paymentType" id="paymentMethod" autocomplete="off" value="" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                          <label for="">Date From</label>
                          <s:textfield type="text" onchange="dateBaseDownload()" data-download="dateFrom" id="dateFrom" data-var="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                      </div>
                      <!-- /.col-md-3 mb-20 -->
                      <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                          <label for="">Date To</label>
                          <s:textfield type="text" onchange="dateBaseDownload()" data-download="dateTo" data-var="dateTo" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                      </div>
                      <!-- /.col-md-3 -->
                    <div class="col-md-3 mb-20 slide-form-element">
                      <div class="lpay_input_group">
                        <label for="">PG REF Number</label>
                        <s:textfield id="pgRefNum" onchange="dateBaseDownload()" data-var="transactionId" class="lpay_input" data-download="transactionId" name="transactionId" type="text" value="" autocomplete="off" onkeypress="javascript:return isNumber (event)" maxlength="16" ></s:textfield>
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20 slide-form-element">
                      <div class="lpay_input_group">
                        <label for="">Order ID</label>
                        <s:textfield id="orderId" onchange="dateBaseDownload()" class="lpay_input" data-download="orderId" data-var="orderId" name="orderId" type="text" value="" autocomplete="off" onkeypress="return Validate(event);"></s:textfield>
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20 slide-form-element">
                      <div class="lpay_input_group">
                        <label for="">Cust Email</label>
                        <s:textfield id="customerEmail" data-download="customerEmail" class="lpay_input" data-var="customerEmail" name="customerEmail" type="text" value="" autocomplete="off" onblur="validateEmail(this);"></s:textfield>
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                           
                    <div class="col-md-3 mb-20 slide-form-element">
                        <div class="lpay_select_group">
                           <label for="">Currency</label>
                           <s:select name="currency" data-download="currency" data-var="currency" id="currency" headerValue="ALL" headerKey="" list="currencyMap" class="selectpicker" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    
                    <!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20 slide-form-element">
						<div class="lpay_select_group">
						   <label for="">Delta Flag</label>
				 	 	 <select class="selectpicker" data-download="deltaFlag" data-var="deltaFlag" name="deltaFlag" id="deltaFlag">
						    <option value="ALL">ALL</option>
					   		<option value="Y">Y</option>
					   		<option value="N">N</option>
				   	  	 </select>
						</div>
					<!-- /.lpay_select_group -->  
					</div>
                    
                    <div class="col-md-12 text-center">
                        <input type="button" id="submit" value="View" class="lpay_button lpay_button-md lpay_button-secondary">
                        <input type="button" id="downloadButton" value="Download" class="lpay_button lpay_button-md lpay_button-primary">
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

            <section class="refund-transaction lapy_section white-bg box-shadow-box mt-70 p20">
                <div class="row">
                    <div class="col-md-12">
                        <div class="heading_with_icon mb-30">
                            <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                            <h2 class="heading_text">Refund Transaction Data</h2>
                        </div>
                        <!-- /.heading_icon -->
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-12 lpay_table_style-2">
                        <div class="lpay_table">
                            <table id="txnResultDataTable" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>Merchant Name</th>
                                        <th>Transaction ID</th>
                                        <th>PG REF Num</th>
                                        <th>Order ID</th>
                                        <th>Payment Method</th>
                                        <th>Date</th>
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

        <s:form id="downloadTransactionsReportAction" name="downloadTransactionsReportAction" action="downloadTransactionsReportAction">

	    </s:form>
    </body>

</html>