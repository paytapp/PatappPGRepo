<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>View Virtual Details</title>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/jquery.min.js" type="text/javascript"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/bootstrap-select.min.js"></script>
    
    <!-- <script src="../js/user-script.js"></script> -->
</head>
<body>
    
    <section class="composite-reporting lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Reporting</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Select Merchant</label>
                        <s:select 
                            name="payId" 
                            data-download="payId" 
                            data-var="payId" 
                            class="selectpicker"
                            id="merchant" 
                            data-submerchant="subMerchant"
                            data-user="subUser" 
                            headerKey="ALL"
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
            <s:else>
                <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                    <div class="col-md-3">
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
                    <div class="col-md-3 d-none">
                        <div class="lpay_select_group ">
                            <label for="">Select Merchant</label>
                            <s:select 
                                name="payId" 
                                data-download="payId"
                                data-var="payId" 
                                data-live-search="true" 
                                class="selectpicker" 
                                id="merchant"
                                list="merchantList" 
                                data-submerchant="subMerchant" 
                                data-user="subUser"
                                listKey="emailId"
                                listValue="businessName" 
                                autocomplete="off" 
                            />
                        </div>
                    </div>
                </s:else>
            </s:else>
            <s:if test="%{#session['USER'].superMerchant == true}">
                <div class="col-md-3" data-target="subMerchant">
                    <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <s:select 
                            data-id="subMerchant"
                            data-download="subMerchantPayId" 
                            data-var="subMerchantPayId" 
                            data-submerchant="subMerchant" 
                            data-user="subUser"  
                            name="subMerchantPayId" 
                            class="selectpicker" 
                            id="subMerchant" 
                            list="subMerchantList" 
                            listKey="emailId" 
                            headerValue="ALL"
                            listValue="businessName" 
                            autocomplete="off" 
                        />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
            <!-- /.col-md-3 -->	
            </s:if>
            <s:else>
                <div class="col-md-3 d-none" data-target="subMerchant"> 
                    <div class="lpay_select_group">
                        <label for="">Sub Merchant</label>
                        <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
            </s:else>
            <!-- /.col-md-3 -->	
            <div class="col-md-3 mb-20">
                <div class="lpay_select_group">
                   <label for="">Status</label>
                   <select data-var="status" name="paymentMode" class="selectpicker" id="paymentMode">
                        <option value="ALL">ALL</option>
                        <option value="success">Success</option>
                        <option value="pending">Pending</option>
                   </select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_select_group">
                   <label for="">Mode</label>
                   <select data-var="txnType" name="txnType" class="selectpicker" id="txnType">
                        <option value="ALL">ALL</option>
                        <option value="success">IMPS</option>
                        <option value="pending">UPI</option>
                   </select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">From Date</label>
                    <input type="text" data-var="dateFrom" class="lpay_input datepick">
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">To Date</label>
                    <input type="text" data-var="dateTo" class="lpay_input datepick">
                </div>
                <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="view">
                    View
                </button>
                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                <button class="lpay_button lpay_button-md lpay_button-primary" id="download">
                    Download
                </button>
                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="compositeReportTabel" class="display" cellspacing="0" width="100%">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Pay ID</th>
                                <th>Merchant Name</th>
                                <th>Transaction ID</th>
                                <th>Order ID</th>
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
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <form action="downloadmerchantInitiatedDirectReport" id="downloadComposite">
        
    </form>
    <script src="../js/compositeReporting-script.js"></script>
    <script type="text/javascript">
        document.querySelector("#merchant").addEventListener("change", function(e){
            getSubMerchant(e, "getSubMerchantList", {
                isSuperMerchant : true
            });
        });
    </script>
</body>
</html>