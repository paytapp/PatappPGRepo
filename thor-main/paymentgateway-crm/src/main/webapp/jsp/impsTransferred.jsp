<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<s:if test="%{#session.USER.vendorPayOutFlag== true && #session.USER.UserType.name()=='MERCHANT'}">
<title>Merchant Initiated</title>
</s:if>
<s:else>
  <title>IMPS Transfer</title> 
</s:else>
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
    button#selectAll {
        position: absolute;
        padding: 5px 10px;
        left: 10px;
        top: -24px
    }
    span#selectRow {
    padding: 5px 8px;
    background-color: #fff;
    border-radius: 5px;
    color: #000;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    }
</style>
</head>
<div class="edit-permission"><s:property value="%{editingpermission}"/></div>
<!-- /.edit-permission -->
<body class="bodyColor">
    <s:hidden id="setSuperMerchant"></s:hidden>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <s:if test="%{#session.USER.vendorPayOutFlag== true && #session.USER.UserType.name()=='MERCHANT'}">
                        <h2 class="heading_text">Merchant Initiated Details</h2>
                    </s:if>
                    <s:else>
                        <h2 class="heading_text">IMPS Transfer Details</h2>
                    </s:else>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active">
                        <a href="#" class="lpay-nav-link" data-id="impsTransfer">IMPS Transfer</a>
                    </li>
                    
                    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                    <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" data-id="report">Report</a>
                    </li>
                    </s:if>
            
                   <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                       <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" data-id="pgPayout">Merchant Initiated-Indirect</a>
                    </li>
                    </s:if>

                    <s:elseif test="%{#session.USER.vendorPayOutFlag== true && #session.USER.UserType.name()=='MERCHANT'}">
                       <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" data-id="pgPayout">Merchant Initiated-Indirect</a>
                    </li>
                    </s:elseif>

                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100" data-target="impsTransfer">
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Merchant</label>
                       <s:if
                       test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                       <s:select name="merchant" data-get="subMerchant" data-id="lpay-input" class="selectpicker lpay-input merchant-class blankInput"
                           id="merchant" headerKey="" data-live-search="true" headerValue="Select Merchant"
                           list="merchantList" listKey="payId" listValue="businessName" autocomplete="off" />
                        </s:if>
                        <s:else>
                            <s:select name="merchant" data-get="subMerchant" data-live-search="true" class="selectpicker merchant-class lpay-input blankInput" id="merchant"
                            list="merchantList" listKey="payId" listValue="businessName" autocomplete="off" />
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->
                <s:if test="%{#session['USER'].superMerchant == true}">
                    <div class="col-md-3 mb-20" data-id="submerchant">
                      <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <s:select data-id="subMerchant" data-var="vendorPayId" name="subMerchant" class="selectpicker" id="subMerchant"
                          list="subMerchantList" listKey="payId"
                          listValue="businessName" autocomplete="off" headerKey="ALL" headerValue="ALL" />
                      </div>
                      <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->	
                  </s:if>
                  <s:else>
                    <div class="col-md-4 mb-20 d-none" data-id="submerchant"> 
                      <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <select name="subMerchant" data-id="subMerchant"  data-var="vendorPayId" id="subMerchant" class=""></select>

                      </div>
                      <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                  </s:else>
                <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Captured Date From</label>
                      <s:textfield type="text" id="capturedDateFrom" name="capturedDateFrom"
                      class="lpay_input lpay-input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-4 mb-20 -->
                  <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Captured Date To</label>
                      <s:textfield type="text" id="capturedDateTo" name="capturedDateTo"
                      class="lpay_input lpay-input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-4 -->
                  <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Payout Date</label>
                      <s:textfield type="text" id="settledDate" name="settledDate"
                      class="lpay_input lpay-input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-3 -->
                  <div class="col-md-4 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Mobile Number</label>
                      <input type="text" data-id="lpay-input" maxlength="10" class="lpay_input lpay-input" onkeypress="onlyDigit(event)" id="phoneNo">
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-4 -->
                <div class="col-md-4 mb-20">
                  <div class="lpay_input_group">
                    <label for="">Bank Account Name</label>
                    <input type="text" onkeypress="onlyLetters(event)" data-id="lpay-input" class="lpay_input lpay-input blankInput"  id="bankAccountName">
                    <span class="default_error d-none">Field should not blank</span>
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 mb-20">
                  <div class="lpay_input_group">
                    <label for="">Bank Account Number</label>
                    <input type="text" data-id="lpay-input" class="lpay_input lpay-input blankInput" onkeypress="onlyDigit(event)" id="bankAccountNumber">
                    <span class="default_error d-none">Field should not blank</span>
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 mb-20">
                  <div class="lpay_input_group">
                    <label for="">Bank IFSC Code</label>
                    <input type="text" onkeypress="lettersAndAlphabet(event)" data-id="lpay-input" class="lpay_input lpay-input blankInput" id="bankIfsc">
                    <span class="default_error d-none">Field should not blank</span>
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-4 mb-20">
                  <div class="lpay_input_group">
                    <label for="">Amount</label>
                    <input type="text" data-id="lpay-input" class="lpay_input lpay-input blankInput" onkeypress="digitDot(event)" id="amount">
                    <span class="default_error d-none">Please enter amount</span>
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->
            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN'}">

                <div class="lpay_tabs_content d-none w-100" data-target="report">
                           <s:if
                           test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                           <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                               <label for="">Merchant</label>
                           <s:select name="merchant" data-id="reportMerchant" data-get="subMerchantReport" class="selectpicker lpay-input"
                               id="merchantReportPayId" headerKey="" data-live-search="true" headerValue="ALL"
                               list="merchantList" listKey="payId"
                               listValue="businessName" autocomplete="off" />
                               </div>
                               </div>
                            </s:if>
                            <s:else>
                                <div class="col-md-3 mb-20 d-none">
                                    <div class="lpay_select_group">
                                       <label for="">Merchant</label>
                                <s:select name="merchant" data-id="reportMerchant" class="selectpicker lpay-input" id="merchantReportPayId" list="merchantList" listKey="payId"
                                    listValue="businessName" autocomplete="off" />
                                    </div>
                                    </div>
                            </s:else>
                        
                    <!-- /.col-md-3 mb-20 -->
                    <s:if test="%{#session['USER'].superMerchant == true}">
                        <div class="col-md-3 mb-20" data-target="subMerchantReport">
                          <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <s:select data-id="subMerchant" data-var="vendorPayId" name="subMerchant" class="selectpicker" id="subMerchantReport"
                              list="subMerchantList" listKey="payId"
                              listValue="businessName" autocomplete="off" headerKey="ALL" headerValue="ALL" />
                          </div>
                          <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->	
                      </s:if>
                      <s:else>
                        <div class="col-md-3 mb-20 d-none" data-target="subMerchantReport"> 
                          <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <select name="subMerchant" data-var="vendorPayId" id="subMerchantReport" class=""></select>
    
                          </div>
                          <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-3 -->							
                      </s:else>
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Status</label>
                           <select class="selectpicker" data-id="reportStatus" name="status" id="status">
                                <option value="ALL">All</option>
                                <option value="Captured">Captured</option>
                                <option value="Declined">Declined</option>
                                <option value="Timeout">Timeout</option>
                                <option value="Failed at Acquirer">Failed at Acquirer</option>
                                <option value="Duplicate">Duplicate</option>
                                <option value="Invalid">Invalid</option>
                           </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Channel</label>
                           <select class="selectpicker" data-id="reportChannel" name="channel" id="channel">
                                <option value="ALL">All</option>
                                <option value="PG Initiated">PG Initiated</option>
                                <option value="Merchant Initiated Indirect">Merchant Initiated - Indirect</option>
                                <option value="Merchant Initiated Direct">Merchant Initiated - Direct</option>  
                           </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                          <label for="">Captured Date From</label> 
                          <s:textfield type="text" id="dateFrom" data-id="reportDateFrom" name="dateFrom"
                          class="lpay_input datepick" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                      </div>
                      <!-- /.col-md-3 mb-20 -->
                      <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                          <label for="">Captured Date To</label>
                          <s:textfield type="text" id="dateTo" data-id="reportDateTo" name="dateTo"
                          class="lpay_input datepick" autocomplete="off" readonly="true" />
                        </div>
                        <!-- /.lpay_input_group -->
                      </div>
                      <!-- /.col-md-3 -->
                      <div class="col-md-12 mb-20 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary viewData">View</button>
                        <button class="lpay_button lpay_button-md lpay_button-primary downloadData">Download</button>
                      </div>
                      <!-- /.col-md-12 -->
                      <div class="col-md-12 lpay_table_style-2">
                        <div class="lpay_table">
                            <table id="datatable" class="display" cellspacing="0" width="100%">
                                <thead>
                                    <tr class="lpay_table_head">
                                        <th>Merchant Name</th>
                                        <th>Transaction ID</th>
                                        <th>Channel</th>
                                        <th>Order Id</th>
                                        <th>Payout Date</th>
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
            </s:if>
            <div class="lpay_tabs_content d-none" data-target="pgPayout">
                
                       <s:if
                       test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                       <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Merchant</label>
                       <s:select name="merchant" data-download="reportPGMerchant" data-id="reportMerchant" data-get="subMerchantPayoutPayId" class="selectpicker lpay-input"
                           id="merchantPayoutPayId" headerKey="" data-live-search="true" headerValue="ALL"
                           list="merchantList" listKey="payId"
                           listValue="businessName" autocomplete="off" />
                           </div>
                           </div>
                        </s:if>
                        <s:elseif  test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
                            <div class="col-md-3 mb-20  d-none">
                                <div class="lpay_select_group">
                                   <label for="">Merchant</label>
						<s:select data-download="reportPGMerchant" name="merchant" data-id="reportMerchant" data-live-search="true" class="selectpicker lpay-input" id="merchantPayoutPayId"
                            list="subMerchantList" listKey="payId"
                            listValue="businessName" autocomplete="off" />	
                            </div>
                            </div>
                    </s:elseif>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none">
                            <div class="lpay_select_group">
                               <label for="">Merchant</label>
                        <s:select name="merchant" data-download="reportPGMerchant" data-id="reportMerchant" data-live-search="true" class="selectpicker lpay-input" id="merchantPayoutPayId"
                            list="merchantList" listKey="payId"
                            listValue="businessName" autocomplete="off" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    </s:else>
                    
                <s:if test="%{#session['USER'].superMerchant == true}">
                    <div class="col-md-3 mb-20" data-target="subMerchantPayoutPayId">
                      <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <s:select data-id="subMerchant" data-download="reportPGSubMerchant" data-var="vendorPayId" name="subMerchant" class="selectpicker" id="subMerchantPayoutPayId"
                          list="subMerchantList" listKey="payId"
                          listValue="businessName" autocomplete="off" headerKey="ALL" headerValue="ALL" />
                      </div>
                      <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->	
                  </s:if>
                  <s:else>
                    <div class="col-md-3 mb-20 d-none" data-target="subMerchantPayoutPayId"> 
                      <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <select name="subMerchant" data-download="reportPGSubMerchant" data-var="vendorPayId" id="subMerchantPayoutPayId" class=""></select>

                      </div>
                      <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                  </s:else>

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Order ID</label>
                        <s:textfield
                            id="orderIdPayout"
                            class="lpay_input"
                            name="orderId"
                            data-download="reportPGOrderId"
                            type="text"
                            autocomplete="off"
                            onkeypress="return Validate(event);">
                        </s:textfield>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Status</label>
                       <select class="selectpicker" data-download="reportPGStatus" name="status"  id="statusPG">
                            <option value="ALL">All</option>
                            <option value="Captured">Captured</option>
                            <option value="Pending">Pending</option>
                            <option value="Declined">Declined</option>
                            <option value="Timeout">Timeout</option>
                            <option value="Failed at Acquirer">Failed at Acquirer</option>
                            <option value="Duplicate">Duplicate</option>
                            <option value="Invalid">Invalid</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Request Date From</label> 
                      <s:textfield type="text" data-download="reportPGDateFrom" id="dateFromPayout" data-id="reportDateFrom" name="dateFrom"
                      class="lpay_input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-3 mb-20 -->
                  <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Request Date To</label>
                      <s:textfield type="text" data-download="reportPGDateTo" id="dateToPayout" data-id="reportDateTo" name="dateTo"
                      class="lpay_input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>

                  <!-- /.col-md-3 -->
                  <div class="col-md-12 mb-20 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary viewDataPayout">View</button>
                    <button class="lpay_button lpay_button-md lpay_button-primary dataPayoutDownload">Download</button>
                  </div>
                  <!-- /.col-md-12 -->
                  <div class="col-md-12 lpay_table_style-2">
                    <s:if  test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
                        <button class="lpay_button lpay_button-sm lpay_button-primary d-none" id="selectAll">Initiate All</button>
                    </s:if>
                    <div class="lpay_table">
                        <table id="datatablePayout" class="display" cellspacing="0" width="100%">
                           
                            <thead class="">
                                <tr class="lpay_table_head">
                                    <th><span id="selectRow">Select All</span></th>
                                    <th>Merchant Name</th>
                                    <th>Transaction ID</th>
                                    <th>Order ID</th>
                                    <th>Date</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                  </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content d-none -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />
    <s:hidden name="vendorPayOutFlag" value="%{#session.vendorPayOutFlag}" />
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

    <s:form id="downloadPayout"  action="downloadPayoutAction" >
        <s:hidden name="reportPGMerchant" id="reportPGMerchant"></s:hidden>
        <s:hidden name="reportPGSubMerchant" id="reportPGSubMerchant"></s:hidden>
        <s:hidden name="reportPGStatus" id="reportPGStatus"></s:hidden>
        <s:hidden name="reportPGDateFrom" id="reportPGDateFrom"></s:hidden>
        <s:hidden name="reportPGDateTo" id="reportPGDateTo"></s:hidden>
        <s:hidden name="reportPGOrderId" id="reportPGOrderId"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>

    </s:form>

    <s:form id="downloadReport"  action="downloadReport" >
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
        <s:hidden name="reportStatus" id="reportStatus"></s:hidden>
        <s:hidden name="reportChannel" id="reportChannel"></s:hidden>
        <s:hidden name="reportDateFrom" id="reportDateFrom"></s:hidden>
        <s:hidden name="reportDateTo" id="reportDateTo"></s:hidden>
        <s:hidden name="reportSubMerchant" id="reportSubMerchant"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <script src="../js/impsTransfer.js"></script>
</body>
</html>