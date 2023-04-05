<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix = "s" uri = "/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Issuer Details</title>
    <link rel="icon" href="../image/favicon-32x32.png">

    <link rel="stylesheet" href="../css/paymentOptions.css">
    <link rel="stylesheet" href="../css/common-style.css">

    <script type="text/javascript" src="../js/jquery.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>


<style>
    .input-edit{
        display: none !important;
        margin: 5px auto 0;
        text-align: center;
        position: absolute;
        left: 0;
        right: 0;
        top: -1px;
    }

    .issure-name table tr td{
        position: relative !important;
    }

    .editable--active .input-edit{
        display: block !important;
        margin: 5px auto 0 !important;
    }

    /* .slideSwitch */
.slideSwitch {
    width: 38px;
    height: 20px;
    background: #ccc;
    position: relative;
    border-radius: 50px;
    display: inline-block;
    cursor: pointer;
    pointer-events: none;
}

.issure-name tr.editable--active .slideSwitch { pointer-events: unset; }

.slideSwitch.active { background: #5CB85C; }

.slideSwitch label {
    display: block;
    width: 15px;
    height: 16px;
    cursor: pointer;
    position: absolute;
    top: 2px;
    left: 2px;
    z-index: 1;
    background: #fff;
    border-radius: 50px;
    transition: all 0.4s ease;
}

.slideSwitch input[type=checkbox] { visibility: hidden; }
.slideSwitch input[type=checkbox]:checked + label { left: 21px; }
    .d-flex { display: flex; }
    .justify-content-center { justify-content: center !important;}
    .justify-content-right { justify-content: end !important; }
    .justify-content-left { justify-content: left !important; }

    .mt-20 { margin-top: 20px; }
    .px-10 {
        padding-left: 10px !important;
        padding-right: 10px !important;
    }
    .py-5 {
        padding-top: 5px !important;
        padding-bottom: 5px !important;
    }
    .py-10 {
        padding-top: 10px !important;
        padding-bottom: 10px !important;
    }
    .pt-20 { padding-top: 20px !important; }
    .mb-10 { margin-bottom: 10px !important; }
    .mx-5{
        margin-left: 5px !important;
        margin-right: 5px !important;
    }

    .issure-name {
        padding: 20px;
        display: inline-block;
        width: 100%;
    }

    .buttons-csv {
        background: #000;
        display: inline-block;
        padding: 7px 20px;
        border-radius: 6px;
        color: #fff;
        font-size: 15px !important;
        text-decoration: none !important;
        cursor: pointer;
    }
    .buttons-csv:hover { color: #fff; }
    
    .font-size-14 { font-size: 14px !important; }
    table, th, td {
        border: 1px solid #ccc;
        text-align: center !important;
    }
    td, th { padding: 5px !important; }
    td:not(:last-child), th:not(:last-child) { border-right: 1px solid #ccc; }
    table {
        border-collapse: collapse;
        width: 100%;
    }
    tr:not(:last-child) {
        border-bottom: 1px solid #ccc;
    }
    .dataTables_wrapper { margin-top: 0 !important; }
    .border-left{ border-left: 1px solid #ccc !important; }

    .max-height-400 { max-height: 400px; }
    .overflow-y-auto { overflow-y: auto; }
</style>

</head>
<body>
    
    

<div class="container-fluid">
    <div class="row border text-center panel panel-default">        
        <div class="col-xs-6 pt-20 ">
            <h3 class="text-center mb-10 font-size-14 ">Download File Format</h3>
            <table id="example" class="display nowrap" style="display: none;">
                <thead>
                    <tr>
                        <th>UniqueNo</th>
                        <th>Name</th>
                        <th>EmailId</th>
                        <th>MobileNo</th>
                        <th>Address</th>
                        <th>amount</th>
                        <th>remarks</th>
                    </tr>
                </thead>
            </table>
        </div>

        <div class="col-xs-6 border-left ">
            <div class="row">
                <div class="col-sm-12">
                    <form action="eventPageAction" method="post" enctype="multipart/form-data">            	
            	        <s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}"></s:textfield>
                        <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                            <div class="position-relative d-flex justify-content-end">
                                <s:select
                                    name="payId"
                                    class="form-control py-5 px-10 height-auto full-width merchantAdmin"
                                    id="payId"
                                    headerKey="ALL"
                                    headerValue="ALL"
                                    list="merchantList"
                                    listKey="payId"
                                    listValue="businessName"
                                    autocomplete="off"
                                />
                                <span id="payIdErr" class="error"></span>
                            </div>
                        </s:if> 
                 
                        <div class="panel-body">
                            <h3 class="text-muted panel-heading pb-2 font-size-14" style="font-family: Arial, Helvetica, sans-serif;">Select CSV File</h3>                    
                        </div>
                        <div class="d-flex justify-content-center pt-2 file-field">
                            <input type="file" accept=".csv" name="csvfile" class="img-thumbnail py-10" onchange='triggerValidation(this)'>
                        </div>
                        <div class="d-flex justify-content-center mt-20">
                            <input type="submit" value="Submit" id="subBtn" class="btn btn-primary btn-md mx-5 px-10 py-5 font-size-14" disabled>
                        </div>
                    </form>
                </div>
            </div>

            <s:if test="wrongCsv!= null">
                <s:if test="wrongCsv!= 0">
                    <div class="row">
                        <div class="col-xs-12 text-center bg-danger">
                        <h3 class="text-white p-2 mb-2 text-white h10 py-5 font-size-14">Wrong File Format</h3>
                        </div>
                    </div>
                </s:if>
            </s:if>

            <s:if test="rowCount != null">
                <s:if test="rowCount!=0">    	
                    <div class="row" id="showData" >
                        <div class="col text-center">
                            <h4 class="bg-info p-2 mb-2 text-white h10 py-5 font-size-14">Total Numbers of Data In CSV</h4>
                            <p><s:property value="rowCount"/></p>            
                        </div>
    
                        <div class="row text-center">
                            <div class="col-sm-4">
                                <h5 class="bg-success text-white mx-5 px-10 py-5 font-size-14" >Successfully Stored</h5>
                                <p> <s:property value="storedRow" /></p>
                            </div>
                            <div class="col-sm-4">
                                <h5 class="bg-warning p-2 mb-2 text-white py-5 font-size-14">Duplicate Data</h5>                    
                                <table>
                                    <tr><th >Row No.</th><th>Email Id</th></tr>
                                    <s:iterator value="duplicate">
                                        <tr>
                                            <td><s:property value="key" /></td>
                                            <td><s:property value="value" /></td>
                                        </tr>
                                    </s:iterator>
                                </table>
                            </div>
                            
                            <div class="col-sm-4">
                                <h5 class="bg-danger p-2 mb-2 text-white py-5 font-size-14">Failed Data</h5>                    
                    	        <table>
                    		        <tr><th >Row No.</th><th>Email Id</th></tr>
                    		        <s:iterator value="skipedRow">    
                                        <tr>
                                            <td><s:property value="key" /></td>
                                            <td><s:property value="value" /></td>
                                        </tr>
                                    </s:iterator>
                                </table>
                            </div>
                        </div>
                    </div>
                </s:if>
            </s:if>
        </div>

        <div class="issure-name" id="issuerDetail">
            <div class="row"></div>

            <div class="issuer-filter row mb-20">
                <div class="col-sm-3 text-left">
                    <label for="merchantId">Merchant</label>
                    <s:select
                        name="merchantId"
                        class="form-control py-5 px-10 height-auto full-width view-emi-filter ml-0"
                        id="merchantId"
                        headerKey="SelectMerchant"
                        headerValue="SelectMerchant"
                        list="merchantList"
                        listKey="payId"
                        listValue="businessName"
                        autocomplete="off"
                    />
                </div>
                <div class="col-sm-3 text-left">
                    <label for="acquirer">Acquirer</label>
                    <s:select
                        class="form-control py-5 px-10 height-auto full-width view-emi-filter ml-0"
                        headerKey="ALL"
                        headerValue="ALL"                        
                        list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
                        listKey="code"
                        listValue="name"
                        name="acquirer"
                        id="acquirer"
                        autocomplete="off"
                    />
                </div>
                <!-- /.col-sm-3 -->
            </div>
            <!-- /.issuer-filter -->

            <div class="max-height-400 overflow-y-auto">
                <table class="table lpay_table mt-20">
                    <thead class="lpay_table_head">
                        <tr>
                            <th class="border-bottom-none" width="150">Merchant Name</th>
                            <th class="border-bottom-none" width="100">Issuer Name</th>
                            <th class="border-bottom-none">Payment Type</th>
                            <th class="border-bottom-none">Tenure</th>
                            <th class="border-bottom-none">Rate Of Interset</th>
                            <th class="border-bottom-none">always on</th>
                            <th class="border-bottom-none" width="100">Actions</th>
                        </tr>
                    </thead>
                    <tbody class="border-grey-lighter border-top-none"></tbody>
                </table>
                <!-- /.payment-options -->
            </div>
        </div>
        <!-- /.issure-detail -->
    </div>
</div>

<script src="../js/eventDetail.js"></script>
<script src="../js/decimalLimit.js"></script>

</body>
</html>