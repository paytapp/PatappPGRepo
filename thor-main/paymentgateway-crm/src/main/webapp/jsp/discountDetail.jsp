<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Discount Details</title>
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

        [data-id=mopType] .btn-default.active{
            background-color: transparent !important;
            border-color: #ccc !important;
            box-shadow: none;
        }

        .input-edit{
            max-width: 55px !important;
        }
        .mopType-select .lpay_select_group .dropdown-menu.open{
            overflow: hidden !important;
        }

        .mopType-select .lpay_select_group .dropdown-menu.inner{
            max-height: 300px !important;
            overflow-y: auto !important;
        }

        .discount-success span{
            width: 100%;
            display: block;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
        }

        .discount-success .error-text{
            background-color: #f68282;
            color: #7d1414;
        }

        .pagination-wrapper{
            text-align: right;

        }

        .pagination-wrapper span {
            display: inline-block;
            width: 25px;
            height: 25px;
            border-radius: 50%;
            line-height: 25px;
            text-align: center;
            cursor: pointer;
            margin-top: 10px;
            
        }

        .page-active{
            text-decoration: none;
            background-color: #002163 !important;
            border: none;
            font-weight: 300;
            background-image: none;
            color: #fff;
            box-shadow: 0 4px 20px 0px rgba(0, 0, 0, 0.14), 0 7px 10px -5px rgba(156, 39, 176, 0.4);
        }
        
    </style>
</head>
<body>
    
    <section class="discount-detail lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row discount-filter-div">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Discount Detail</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 discount-success">
                <span class="lpay_success d-none">Your data has been save successfully.</span>
                <span class="lpay_error d-none">Something went wrong please try again.</span>
            </div>
            <!-- col-md-12 -->
            <div class="col-md-3 active discount-applicable">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Discount Applicable</label>
                    <select name="discountApplicable" title="Select Discount Applicable" class="selectpicker discount-input" id="discountApplicableOn">
                        <option data-id="discount_pg" value="demoPage">Payment Gateway</option>
                        <option data-id="discount_category" value="Category">Category</option>
                        <option data-id="discount_merchant" value="Merchant">Merchant</option>
                    </select>
                </div>
                <!-- /.lpay_select_group mb-20 -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-active="Merchant">
                  <div class="lpay_select_group mb-20">
                    <label for="">Select Merchant</label>
                    <s:select
                            name="dicsount"
                            class="selectpicker discount-input"
                            headerKey="ALL"
                            data-live-search="true"
                            list="merchantList"
                            listKey="payId"
                            listValue="businessName"
                            autocomplete="off"
                            multiple="true"
                            data-selected-text-format="count"
                            title="ALL"
                            data-text="Merchant"
                        />
                  </div>
                  <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-active="Category">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Category</label>
                    <select name="discount" data-id="industryCategory" title="Select Category"  multiple="true" class="selectpicker discount-input" data-text="Category" data-selected-text-format="count"></select>
                </div>
                <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-active="discountType">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Discount Type</label>
                    <select name="" id="discountType" data-text="Discout Type" class="selectpicker discount-input" title="Discount Type">
                        <option value="Instant">Instant</option>
                        <option value="Cashback">Cashback</option>
                    </select>
                </div>
                <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-active="paymentRegion">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Payment Region</label>
                    <select name="paymentRegion" data-text="Payment Region" id="paymentRegion" class="selectpicker discount-input" title="Payment Region">
                        <!-- <option value="">Select Payment Region</option> -->
                        <option value="Domestic">Domestic</option>
                        <option value="International">International</option>
                    </select>
                </div>
                <!-- /.lpay_select_group -->
            </div>
            <!-- col-md-3 -->
            <div class="col-md-3 d-none" data-active id="suf-paymentType">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Payment Type</label>
                    <select name="paymentType" id="paymentType" data-text="Payment Type" class="selectpicker discount-input" title="Payment Type"></select>
                </div>
                <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-active data-id="mopType">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Mop Type</label>
                    <select name="paymentType" id="mopType" data-text="Mop Type" class="selectpicker discount-input" multiple="true" title="Mop Type" data-selected-text-format="count"></select>
                </div>
                <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 --> 
            <div class="col-md-3 common-card d-none" data-active="issuerType">
                <div class="lpay_select_group mb-20" >
                    <label for="">Select Issuer Bank</label>
                    <s:select
                        class="selectpicker discount-input"
                        headerKey="ALL"
                        headerValue="ALL"                        
                        list="@com.paymentgateway.commons.util.IssuerType@values()"
                        listKey="name"
                        listValue="name"
                        name="acquirer"
                        id="issuerBank"
                        data-text="Issuer Type"
                        autocomplete="off"
                    />
                </div>
                <!-- /.lpay_select_group mb-20 -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 common-card d-none" data-active="cardHolderType">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Card Holder Type</label>
                    <select name="cardHolderType" data-text="Cardholder Type" class="selectpicker discount-input" id="cardHolderType" title="Card Holder Type">  
                        <option value="ALL">ALL</option>
                         <option value="Consumer">Consumer</option>
                        <option value="Commercial">Commercial</option>
                        <option value="Premium">Premium</option>
                    </select>
                </div>
                <!-- /.lpay_select_group mb-20 -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-active="EMI">
                <div class="lpay_select_group mb-20">
                    <label for="">Select EMI Duration</label>
                    <select name="emiDuration" data-text="EMI" class="selectpicker discount-input" id="emiDuration" title="EMI Duration">
                        <option value="3">3 Month</option>
                        <option value="6">6 Month</option>
                        <option value="9">9 Month</option>
                    </select>
                </div>
                <!-- /.lpay_select_group mb-20 -->
            </div>
            <!-- /.col-md-3 d-none -->
            <div class="col-md-3 common-card d-none" data-active>
                <div class="lpay_select_group mb-20">
                    <label for="">Select Slab Amount</label>
                    <select name="amountSlab" data-text="Slab Amount" class="selectpicker discount-input" id="amountSlab" title="Slab Amount">
                        <!-- <option value="0-1000,0-3000,0-5000">ALL</option>
                        <option value="0.01-1000">0.01 - 1000</option>
                        <option value="1000.01-2000.0">1000.01 - 2000.0</option>
                        <option value="2000.01-1000000.0">2000.01 - 1000000.0</option> -->
                    </select>
                </div>
                <!-- /.lpay_select_group mb-20 -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 common-card active d-none" data-active> 
                <div class="lpay_input_group mb-20">
                    <label for="">Fixed Charges</label>
                    <input type="text" data-text="Fixed Charge" name="fixedCharges" id="fixedCharges" class="form-control percent-amount lpay_input discount-input" onkeyup="onlyNumericKey(this, event, 2);" onkeypress="onlyDigit(event)">
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 common-card active d-none" data-active>
                <div class="lpay_input_group mb-20">
                    <label for="">Percentage Charges</label>
                    <input type="text" data-text="Percentage Charges" name="percentageCharges" id="percentageCharges" class="form-control percent-amount lpay_input discount-input" onkeyup="onlyNumericKey(this, event, 2);" onkeypress="onlyDigit(event)">
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-12 text-center submit-wrapper d-none" >
                <button type="submit" id="btn-sufDetails" class="lpay_button lpay_button-md lpay_button-secondary">Submit</button>
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

    <section class="discount-detail lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row discount-filter-list">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Active Discount List</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-3 active-filter">
                <div class="lpay_select_group mb-20">
                    <label for="">Discount Applicable</label>
                    <select name="discountApplicableOn" title="Select Discount Applicable" class="selectpicker discount-input-filter" id="discountApplicableOnFilter">
                       
                        <option data-id="discount_pg" value="demoPage">Payment Gateway</option>
                        <option data-id="discount_category" value="CATEGORY">Category</option>
                        <option data-id="discount_merchant" value="MERCHANT">Merchant</option>
                    </select>
                </div>
                <!-- /.lpay_select_group mb-20 -->
            </div>
            <!-- /.col-md-3 -->
            
            <div class="col-md-3 d-none" data-filter="MERCHANT">
                <div class="lpay_select_group mb-20">
                    <label for="">Select Merchant</label>
                    <s:select
                            name="discount"
                            class="selectpicker discount-input-filter"
                            id="filter-payId"
                            headerKey="ALL"
                            data-live-search="true"
                            headerValue="ALL"
                            list="merchantList"
                            listKey="payId"
                            listValue="businessName"
                            autocomplete="off"
                            title="ALL"
                        />
                </div>
                <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 d-none" data-filter="CATEGORY">
                <div class="lpay_select_group mb-20">
                  <label for="">Select Category</label>
                  <select name="discount" data-id="industryCategory" class="selectpicker discount-input-filter" title="Select Industry Category">
                     
                  </select>
                  </div>
                <!-- /.merchant__form_group -->
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-12 discount-success">
                <span class="success-text d-none">Your data has been updated successfully.</span>
                <span class="error-text d-none">Something went wrong please try again.</span>
            </div>
            <!-- /.col-md-12 success-div -->
            <div class="col-xs-12 mb-20" id="paymentOptions-info">
                <div class="lpay_table_wrapper">
                    <table class="lpay_custom_table discount-table" style="white-space: nowrap;" id="discountDataTable">
                        <tbody class="border-grey-lighter border-top-none">
                            <tr><td colspan="7" class="color-red text-center font-size-14">No data avaiable</td></tr>
                        </tbody>
                    </table>
                    <!-- /.payment-options -->

                </div>
                <div class="col-md-4">
                    <div class="show-filter-select d-none" style="color: #888888">
                        <span>Show</span>
                        <select name="pagination" id="maxRows">
                            <option value="10">10</option>
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                        </select>
                        <span>entries</span>
                    </div>
                    <!-- /.show-filter -->
                    <div class="show-data-show d-none">
                        <p>Showing 1 to <span id="current-show"></span> of <span id="totalEntries"></span> entries</p>
                    </div>
                    <!-- /.show-data-show -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-8">
                    <div class="pagination-wrapper"></div>
                    <!-- /.pagination-wrapper -->
                </div>
                <!-- /.col-md-8 -->
            </div>
            <!-- /.col-xs-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
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
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn">cancel</button>
                <button  class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">confirm</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
	<!-- /.confrim-popup -->


    <s:hidden name="token" value="%{#session.customToken}"></s:hidden>

    <script src="../js/discountDetail.js"></script>
    <script src="../js/decimalLimit.js"></script>
</body>
</html>