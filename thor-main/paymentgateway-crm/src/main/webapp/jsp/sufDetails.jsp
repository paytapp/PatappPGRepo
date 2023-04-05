<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>SUF Details</title>
    <link rel="icon" href="../image/favicon-32x32.png">
    <link rel="stylesheet" href="../css/common-style.css">
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
    <link rel="stylesheet" href="../css/paymentOptions.css">

    <script src="../js/jquery.min.js"></script>
    <script src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.fancybox.min.js"></script>

    <style>
        .d-none{
            display: none;
        }
        .bootstrap-select.btn-group.show-tick .dropdown-menu li.selected a span.check-mark { margin-top: 0 !important; top: 8px; }
    </style>

</head>
<body>
    <section class="sufDetail_div lpay_section white-bg box-shadow-box mt-70 p20">
            <div class="row">
                <div class="col-md-12">
                    <div class="heading_with_icon mb-30">
                      <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Create / Remove SUF Details</h2>
                    </div>
                    <!-- /.heading_icon -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 d-none" data-respose="message">
					<div class="lpay_success">
                        <p id="responseText"></p>
                    </div>
                </div>
                <div class="col-md-3 mb-20" data-hide="paymentRegion">
                    <div class="lpay_select_group">
                       <label for="">SUF Type</label>
                       <select name="sufSetType" id="sufSetType" class="selectpicker">
                           <option value="">Select SUF Type</option>
                           <option value="bulk">BULK</option>
                           <option value="normal">NORMAL</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 d-none">
                    <div class="lpay_select_group">
                        <label for="">Select Merchant</label>
                        <s:select
                            name="payId"
                            class="selectpicker"
                            id="payId"
                            data-live-search="true"
                            list="merchantList"
                            listKey="payId"
                            data-submerchant="subMerchant"
                            listValue="businessName"
                            autocomplete="off"
                        />
                    </div>
                    <!-- /.lpay_select_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
                    <div class="lpay_select_group">
                       <label for="">Sub Merchant</label>
                       <select name="subMerchantId" title="ALL" data-var="subMerchantId" data-submerchant="subMerchant" multiple data-actions-box="true" data-user="subUser" id="subMerchant" class=""></select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->	
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Transaction Type</label>
                       <select name="txnType" id="txnType" class="selectpicker" title="Select Txn Type">
                        <option value="">Select Txn Type</option>
                        <option value="Sale">Sale</option>
                        <option value="Refund">Refund</option>
                        <option value="eNACH">eNACH</option>
                        <option value="Merchant Txn Email">Merchant Txn Email</option>
                        <option value="Merchant Txn Sms">Merchant Txn SMS</option>
                        <option value="Customer Txn Email">Customer Txn Email</option>
                        <option value="Customer Txn Sms">Customer Txn SMS</option>
                        <option value="Invoice Payments">Invoice Payments</option>
                        <option value="ePOS">ePOS</option>
                        <option value="IMPS Payout">IMPS Payout</option>
                        <option value="UPI Payout">UPI Payout</option>
                    </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                 <!-- /.col-md-3 -->
                 <div class="col-md-3 mb-20" data-hide="paymentRegion">
                     <div class="lpay_select_group">
                        <label for="">Payment Region</label>
                        <select name="paymentRegion" id="paymentRegion" class="selectpicker" title="Select Payment Region">
                            <option value="">Select Payment Region</option>
                            <option value="Domestic">Domestic</option>
                            <option value="International">International</option>
                        </select>
                     </div>
                     <!-- /.lpay_select_group -->  
                 </div>
                 <!-- /.col-md-3 -->
                 <div class="col-md-3 mb-20 d-none" data-hide="paymentType" id="suf-paymentType">
                     <div class="lpay_select_group">
                        <label for="">Select Payment Type</label>
                        <select name="paymentType" id="paymentType" class="selectpicker" title="Select Payment Type"></select>
                     </div>
                     <!-- /.lpay_select_group -->  
                 </div>
                 <!-- /.col-md-3 -->
                 <div class="col-md-3 mb-20 d-none" data-hide="mopType" data-id="mopType">
                    <div class="lpay_select_group">
                        <label for="">Mop Type</label>
                        <select name="mopType" class="selectpicker" id="payment-options" title="Select Mop Type" multiple data-actions-box="true"></select>
                    </div>
                    <!-- /.pg_select_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="sufCharge lpay_input_group">
                          <label for="">Fix Charges</label>
                          <input type="text" name="charges" id="charges" class="lpay_input" placeholder="Fix Charges" onkeyup="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);" onkeypress="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);">
                    </div>
                    <!-- /.lpay__form_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="sufCharges lpay_input_group">
                        <label for="">Percentage</label>
                        <input type="text" name="percantage" id="percentageAmount" class="lpay_input" placeholder="Percentage %" onkeyup="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);" onkeypress="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);">
                    </div>
                    <!-- /.sufCharges -->
                </div>
                <!-- col-md-3 ends -->
                <div class="col-md-3 mb-20" data-hide="minimumSlab">
                    <div class="sufCharges slab_input lpay_input_group">
                        <label for="">Min. Slab</label>
                        <input type="text" name="slabMinimum" id="minimumSlab" class="lpay_input" placeholder="Minimum Slab" onkeypress="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);" onkeyup="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);">
                    </div>
                    <!-- /.sufCharges -->
                </div>
                <!-- col-md-3 ends -->
                <div class="col-md-3 mb-20" data-hide="maximumSlab">
                    <div class="sufCharges slab_input lpay_input_group">
                        <label for="">Max. Slab</label>
                        <input type="text" name="slabMinimum" id="maximumSlab" class="lpay_input" placeholder="Maximum Slab" onkeypress="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);" onkeyup="onlyNumericKey(this, event, 2); limitAmount_alt(this, event);">
                    </div>
                    <!-- /.sufCharges -->
                </div>
                <!-- col-md-3 ends -->
                <div class="col-md-12 text-center">
                    <button type="submit" id="btn-sufDetails" class="lpay_button lpay_button-md lpay_button-secondary">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.row -->
        </section>
        <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
        <section class="sufWrapper lpay_section white-bg box-shadow-box mt-70 p20">
           <div class="row">
              <div class="col-md-12">
                 <div class="heading_with_icon mb-30">
                   <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                   <h2 class="heading_text">SUF Detail List</h2>
                 </div>
                 <!-- /.heading_icon -->
              </div>
              <!-- /.col-md-12 -->
              <div class="col-md-3 mb-20">
                  <div class="lpay_select_group">
                     <label for="">Select Merchant</label>
                     <s:select
                            name="payId"
                            class="selectpicker filter_suf"
                            id="filter-payId"
                            headerKey="ALL"
                            data-live-search="true"
                            data-submerchant="filter-subMerchant"
                            headerValue="ALL"
                            list="merchantList"
                            listKey="payId"
                            listValue="businessName"
                            autocomplete="off"
                        />
                  </div>
                  <!-- /.lpay_select_group -->  
              </div>
              <!-- /.col-md-3 -->
              <div class="col-md-3 mb-20 d-none" data-target="filter-subMerchant"> 
                <div class="lpay_select_group">
                   <label for="">Sub Merchant</label>
                   <select name="subMerchantId" data-var="subMerchantId" data-submerchant="subMerchant" data-user="subUser" id="filter-subMerchant" class="filter_suf"></select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->	
              <div class="col-md-3 mb-20">
                  <div class="lpay_select_group">
                     <label for="">Transaction Type</label>
                     <select name="txnType" id="filter_txnType" class="selectpicker filter_suf" title="Select Transaction Type">
                        <option value="">Select Txn Type</option>
                        <option value="Sale">Sale</option>
                        <option value="Refund">Refund</option>
                        <option value="eNACH">eNACH</option>
                        <option value="Merchant Txn Email">Merchant Txn Email</option>
                        <option value="Merchant Txn SMS">Merchant Txn SMS</option>
                        <option value="Customer Txn Email">Customer Txn Email</option>
                        <option value="Customer Txn SMS">Customer Txn SMS</option>
                        <option value="Invoice Payments">Invoice Payments</option>
                        <option value="ePOS">ePOS</option>
                        <option value="IMPS Payout">IMPS Payout</option>
                        <option value="UPI Payout">UPI Payout</option>
                    </select>
                  </div>
                  <!-- /.lpay_select_group -->  
              </div>
              <!-- /.col-md-3 -->
              <div class="col-md-3 mb-20">
                  <div class="lpay_select_group">
                     <label for="">Payment Region</label>
                     <select name="paymentRegion" id="filter_paymentRegion" class="selectpicker filter_suf" title="Select Payment Region">
                        <option value="">Select Payment Region</option>
                        <option value="Domestic">Domestic</option>
                        <option value="International">International</option>
                    </select>
                  </div>
                  <!-- /.lpay_select_group -->  
              </div>
              <!-- /.col-md-3 -->
              <div class="col-md-3 d-none mb-20" id="filter-paymentType">
                  <div class="lpay_select_group">
                     <label for="">Payment Type</label>
                     <select name="paymentType" id="filter_paymentType" class="selectpicker filter_suf"  title="Select Payment Type"></select>
                  </div>
                  <!-- /.lpay_select_group -->  
              </div>
              <!-- /.col-md-3 -->
              <div class="col-md-12" id="paymentOptions-info">
                <div class="max-height-400 overflow-auto">
                    <div class="lpay_table_wrapper">
                        <table class="lpay_custom_table" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th width="150">Merchant Name</th>
                                    <th width="150">Sub Merchant Name</th>
                                    <th width="100">Txn Type</th>
                                    <th>Mop Type</th>
                                    <th>Payment Type</th>
                                    <th>Payment Region</th>
                                    <th>Slab</th>
                                    <th>Fixed Charge</th>
                                    <th>Percentage Amount</th>
                                    <th width="100">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr><td colspan="9">No data avaiable</td></tr>
                            </tbody>
                        </table>
                        <!-- /.payment-options -->
                    </div>
                    <!-- /.lpay_table_wrapper -->
                </div>
            </div>
            <!-- /.col-xs-12 -->
           </div>
           <!-- /.row -->
        </section>
        <!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->

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



    <s:hidden name="token" value="%{#session.customToken}"></s:hidden>

    <script src="../js/sufDetails.js"></script>
    <script src="../js/decimalLimit.js"></script>
    <script src="../js/jquery.fancybox.min.js"></script>
</body>
</html>