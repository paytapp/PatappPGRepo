<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Vendor Payout</title>
        <link rel="stylesheet" href="../css/jquery-ui.css">
        <script src="../js/jquery-latest.min.js"></script>
        <script src="../js/jquery-ui.js"></script>
        <script src="../js/daterangepicker.js" type="text/javascript"></script>
        <script src="../js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
        <script type="text/javascript" src="../js/pdfmake.js"></script>
        <link rel="stylesheet" href="../css/bootstrap-select.min.css">
        <script src="../js/commanValidate.js"></script>
        <script src="../js/bootstrap-select.min.js"></script>
    </head>
    <body class="bodyColor">
        <s:hidden value="%{#session.USER.UserType}" id="userType"></s:hidden>
        <section class="vendor_payout lapy_section white-bg box-shadow-box mt-70 p20">
            <div class="row">
                <div class="col-md-12">
                    <div class="heading_with_icon mb-30">
                        <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Vendor Payout</h2>
                    </div>
                    <!-- /.heading_icon -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 mb-20">
                    <ul class="lpay_tabs d-flex">
                        <li class="lpay-nav-item active">
                            <a href="#" class="lpay-nav-link" data-id="transactions">Transactions</a>
                        </li>
                        
                    </ul>
                    <!-- /.lpay_tabs -->
                </div>
                <!-- /.col-md-12 -->
                <div class="lpay_tabs-content w-100" data-target="transactions">
                    <div class="col-md-3 mb-20">
                      <div class="lpay_input_group">
                        <label for="">PG Ref Num</label>
                        <input type="text" class="lpay_input lpay-input" onkeypress="javascript:return isNumber (event)" maxlength="16" data-id="lpay-input" data-var="pgRefNum">
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-3 mb-20">
                      <div class="lpay_input_group">
                        <label for="">Order ID</label>
                        <input type="text" data-var="orderId" class="lpay_input lpay-input" onkeypress="return Validate(event);"
                        onblur="this.value=removeSpaces(this.value);" data-id="lpay-input">
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <!-- <div class="col-md-3 mb-20">
                      <div class="lpay_input_group">
                        <label for="">SKU Code</label>
                        <input type="text" data-var="skuCode" class="lpay_input lpay-input" oninput="allowAlphaNumericSpecial(this)" maxlength="40" data-id="lpay-input">
                      </div>
                    </div> -->
                    <!-- /.col-md-4 -->
                    <!-- <div class="col-md-3 mb-20">
                      <div class="lpay_input_group">
                        <label for="">Category Code</label>
                        <input type="text" data-var="categoryCode" oninput="allowAlphaNumericSpecial(this)" maxlength="40" class="lpay_input lpay-input" data-id="lpay-input">
                      </div>
                    </div> -->
                    <!-- /.col-md-4 -->
                    <s:if
                         test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                    <div class="col-md-3 mb-20">
                      <div class="lpay_select_group">
                         <label for="">Merchant</label>
                         <s:select name="merchant" class="selectpicker"
                           id="merchant" headerKey="" data-var="merchantPayId" data-submerchant="subMerchant" data-user="subUser" data-id="subMerchant" data-live-search="true" headerValue="Select Merchant"
                           list="merchantList" listKey="payId"
                           listValue="businessName" autocomplete="off" />
                      </div>
                      <!-- /.lpay_select_group -->  
                    </div>
                  </s:if>
                  <s:else>
                    <input type="hidden" id="merchant">
                  </s:else>
                    <!-- /.col-md-3 mb-20 -->
                    <s:if test="%{#session['USER'].superMerchant == true && #session.USER.UserType.name()=='MERCHANT' }">
                      <div class="col-md-3 mb-20" data-target="subMerchant">
                        <div class="lpay_select_group">
                          <label for="">Sub Merchants</label>
                          <s:select data-id="subMerchant" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" name="subMerchantPayId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" listValue="businessName" autocomplete="off" headerKey="ALL" headerValue="ALL" />
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->	
                    </s:if>
                    <s:elseif  test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session['USER'].superMerchantId !== null}" >
                      <div class="col-md-3 mb-20" data-target="subMerchant">
                        <div class="lpay_select_group">
                          <label for="">Sub Merchants</label>
                          <s:select data-id="subMerchant" data-submerchant="subMerchant" data-user="subUser" data-var="subMerchantPayId" name="subMerchantPayId" class="selectpicker" id="subMerchant"
                            list="merchantList" listKey="payId"
                            listValue="businessName" autocomplete="off" />
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->	
                    </s:elseif>
                    <s:else>
                      <div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
                        <div class="lpay_select_group">
                          <label for="">Merchants</label>
                          <select name="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" data-var="subMerchantPayId" id="subMerchant" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->							
                    </s:else>

                    <s:if test="%{#session.SUBUSERFLAG == true}">
                      <div class="col-md-3 mb-20" data-target="subUser">
                        <div class="lpay_select_group">
                          <label for="">Vendor Type Sub Users</label>
                          <s:select data-id="subUser" data-var="vendorPayId" name="vendorPayId" class="selectpicker" id="subUser"
                            list="subUserList" listKey="emailId" headerKey="ALL" headerValue="ALL"
                            listValue="businessName" autocomplete="off" />
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->	
                    </s:if>

                    <s:else>
                      <div class="col-md-3 mb-20 d-none" data-target="subUser"> 
                        <div class="lpay_select_group">
                          <label for="">Vendor Type Sub Users</label>
                          <select name="vendorPayId" data-var="vendorPayId" id="subUser" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->							
                    </s:else>
                    
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Payment Method</label>
                           <s:select headerKey="" data-id="lpay-input" data-var="paymentMethod" headerValue="ALL" class="selectpicker lpay-input"
                           list="@com.paymentgateway.commons.util.PaymentType@values()"
                           listValue="name" listKey="code" name="paymentMethod"
                           id="paymentMethod" autocomplete="off" value="" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Currency</label>
                           <s:select name="currency" data-id="lpay-input" data-var="currency" id="currency" headerValue="ALL"
                            headerKey="" list="currencyMap" class="selectpicker lpay-input" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                      <div class="lpay_input_group">
                        <label for="">Date</label>
                        <input type="text" id="date" data-id="lpay-input" data-var="date" class="lpay_input lpay-input" readonly="true">
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-12 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary" id="transactionBtn">Submit</button>
                        <button class="lpay_button lpay_button-md lpay_button-primary" id="downloadTransactionBtn">Download</button>
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-12 mt-20">
                        <div class="lpay_table">
                            <table id="datatable" class="display" cellspacing="0" width="100%">
                                <thead class="lpay_table_head">
                                    <tr>
                                        <th>Merchant</th>
                                        <th>Vendor Name</th>
                                        <th>Vendor Pay ID</th>
                                        <th>Txn Type</th>
                                        <th>Txn ID</th>
                                        <th>PG Ref Num</th>
                                        <th>Order ID</th>
                                        <th>Date</th>
                                        <th>Vendor Payout Date</th>
                                        <th>Payment Method</th>
                                        <th>Payment Region</th>
                                        <th>CardHolder Type</th>
                                        <th>Card Mask</th>
                                        <th>Cust Name</th>
                                        <th>Cust Email</th>
                                        <th>Cust Mobile</th>
                                        <th>Payment Cycle</th>
                                        <th>Base Amount</th>
                                        <th>TDR/Surcharge</th>
                                        <th>GST</th>
                                        <th>Total Amount</th>
                                        <th>Merchant Amount</th>
                                    </tr>
                                </thead>
                            </table>
                        </div>
                        <!-- /.lpay_table -->
                    </div>
                      <!-- /.col-md-12 -->
                </div>
                <!-- /.lpay_tabs-content -->
                <div class="lpay_tabs-content w-100 d-none" data-target="payout">
                  <s:if
                       test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                  <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Merchant</label>
                       <s:select name="merchant" class="selectpicker"
                         id="merchantPayOut" data-var="merchantPayId" headerKey="" data-id="subMerchantPayOut" data-live-search="true" headerValue="Select Merchant"
                         list="merchantList" listKey="payId"
                         listValue="businessName" autocomplete="off" />
                    
                    </div>
                    <!-- /.lpay_select_group -->  
                  </div>
                  <!-- /.col-md-3 mb-20 -->
                </s:if>
              <s:else>
                <input type="hidden" id="merchantPayOut">
              </s:else>
			  <s:if test="%{#session['USER'].superMerchant == true && #session.USER.UserType.name()=='MERCHANT' }">
                      <div class="col-md-3 mb-20" data-id="subMerchantPayOut">
                        <div class="lpay_select_group">
                          <label for="">Vendor</label>
                          <s:select data-id="subMerchantPayOut" data-var="vendorPayId" name="subMerchantPayOut" class="selectpicker" id="subMerchantPayOut"
                            list="subMerchantList" listKey="payId"
                            listValue="businessName" autocomplete="off" headerKey="ALL" headerValue="ALL"/>
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->	
                    </s:if>

                    <s:elseif  test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT'}" >
                      <div class="col-md-3 mb-20" data-id="subMerchantPayOut">
                        <div class="lpay_select_group">
                          <label for="">Vendor</label>
                          <s:select data-id="subMerchantPayOut" data-var="vendorPayId" name="subMerchantPayOut" class="selectpicker" id="subMerchantPayOut"
                            list="subMerchantList" listKey="payId"
                            listValue="businessName" autocomplete="off" />
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->	
                    </s:elseif>
                    <s:else>
                      <div class="col-md-3 mb-20 d-none" data-target="subMerchantPayOut"> 
                        <div class="lpay_select_group">
                          <label for="">Vendor</label>
                          <select name="subMerchant" data-var="vendorPayId" id="subMerchantPayOut" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                      </div>
                      <!-- /.col-md-3 -->							
                    </s:else>
                    <div class="col-md-3 mb-20">
                      <div class="lpay_input_group">
                        <label for="">Date</label>
                        <input type="text" id="datePayOut" data-var="date" class="lpay_input lpay-input">
                      </div>
                      <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
                    <div class="col-md-12 text-center mb-20">
                      <button class="lpay_button lpay_button-md lpay_button-secondary" id="payout">Submit</button>
                      <button class="lpay_button lpay_button-md lpay_button-primary" id="downloadPayout">Download</button>
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-12">
                      <div class="lpay_table">
                          <table id="payoutTable" class="display" cellspacing="0" width="100%">
                              <thead class="lpay_table_head">
                                  <tr>
                                      <th>Merchant</th>
                                      <th>Vendor Name</th>
                                      <th>Payment Cycle</th>
                                      <th>Payout Date</th>
                                      <th>Period</th>
                                      <th>Sale Amount</th>
                                      <th>Refund Amount</th>
                                      <th>Net Payout </th>
                                  </tr>
                              </thead>
                          </table>
                      </div>
                      <!-- /.lpay_table -->
                  </div>
                    <!-- /.col-md-12 -->
                </div>
                <!-- /.lpay_tabs-content -->
            </div>
            <!-- /.row -->
        </section>
        <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
        <s:form class="downloadForm" id="downloadTransaction" action="downloadVendorPayoutTransactionsReport"></s:form>
        <s:form id="downloadTransactionPayout" action="downloadVendorPayoutReport"></s:form>
        <s:hidden name="token" value="%{#session.customToken}" />
        <script src="../js/vendorPayout.js"></script>
    </body>
</html>