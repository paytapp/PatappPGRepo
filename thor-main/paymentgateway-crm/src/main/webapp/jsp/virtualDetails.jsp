<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>View Virtual Details</title>
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/jquery.min.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/bootstrap-select.min.js"></script>
    <!-- <script src="../js/user-script.js"></script> -->
</head>
<body>
    <input type="hidden" data-id='isSubMerchant' value='false' />
    <section class="virtual-detail lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Virtual Account Details</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Select Merchant</label>
                        <s:select 
                            name="merchantEmailId" 
                            data-download="merchantPayId" 
                            data-var="merchantEmailId" 
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
                                data-download="merchantPayId" 
                                class="selectpicker"
                                id="merchant" 
                                headerKey="" 
                                data-var="merchantEmailId" 
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
                                data-download="merchantPayId"
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
            <div class="col-md-3 input-btn-space">
                <button class="lpay_button lpay_button-md lpay_button-primary m-0" id="virtualDetail-submit">
                    Submit
                </button>
                <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                <button class="lpay_button lpay_button-md lpay_button-secondary m-0" id="virtualDetail-download">
                    Download
                </button>
                <!-- /.lpay_button lpay_button-md lpay_button-primary -->
            </div>
            <!-- /.col-md-3 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

    <section class="virtual-details lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Virtual Account Data</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="virtualDetailsTable" class="display" cellspacing="0" width="100%">
                       
                        <thead class="">
                            <tr class="lpay_table_head">
                                <th>Merchant Name</th>
                                <th>Sub-Merchant Name</th>
                                <th>Virtual Account Number</th>
                                <th>Virtual IFSC Code</th>
                                <th>Virtual Beneficiary Name</th>
                                <th>Create Date</th>
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
    <form method="POST" action="virtualDetailsDownloadAction" id="virtualDetailsDownloadAction">

    </form>
    <script src="../js/virtualDetail-script.js"></script>
    <script type="text/javascript">

        document.querySelector("body").classList.add("loader--inactive");
        document.querySelector("#merchant").addEventListener("change", function(e){
            getSubMerchant(e, "getSubMerchantList", {
                isSuperMerchant : true
            });
        });



    </script>
</body>
</html>