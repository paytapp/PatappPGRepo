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
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">

    <script type="text/javascript" src="../js/jquery.js"></script>
    <!-- <script src="../js/jquery.dataTables.js"></script> -->
    <!-- <script type="text/javascript" src="../js/dataTables.buttons.js"></script> -->
    <script src="../js/bootstrap-select.min.js"></script>
    <script type="text/javascript" src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.flash.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.1.3/jszip.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/pdfmake.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/vfs_fonts.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.print.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.html5.min.js"></script>

    <style>
        .input-edit {
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

        .download-btn{
			background-color: #002163 !important;
			display: inline-block;
			padding: 8px 20px;
			font-size: 12px !important;
			line-height: 1.42857143;
			color: #fff;
			border: 1px solid #ccc;
			border-radius: 4px;
		}

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

        .buttons-csv {
            background: #000;
            display: inline-block;
            padding: 7px 20px;
            border-radius: 6px;
            color: #fff;
            font-size: 12px !important;
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

        .form-outer.row {
            margin-left: -15px;
            margin-right: -15px;
        }
    </style>
</head>
<body>
    
   

<div class="container-fluid bg-color-white mt-20 border-grey-lightest box-shadow-common pb-20">
    <div class="row py-20">
        <div class="col-xs-12">
            <h1 class="text-center color-primary">Manage EMI Issuer</h1>
        </div>
    </div>
    <div class="row d-flex border-bottom-grey-lightest border-top-grey-lightest form-outer">
        <div class="col-md-4 d-flex flex-column justify-content-center align-items-center py-20">
            <h3 class="mb-10 font-size-14">Download File Format</h3>
            <table id="example" class="display nowrap" style="display: none;">
                <thead>
                    <tr>
                        <th>BankName</th>
                        <th>PaymentType</th>
                        <th>Tenure</th>
                        <th>Percentage</th>
                    </tr>
                </thead>
            </table>
            <table id="excelTable" class="display nowrap" style="display: none;">
                <thead>
                    <tr>
                        <th>BankName</th>
                        <th>PaymentType</th>
                        <th>Tenure</th>
                        <th>Percentage</th>
                    </tr>
                </thead>
            </table>
        </div>

        <div class="col-xs-8 border-left-grey-lightest py-20">
            <form method="POST" enctype="multipart/form-data" id="fileUploadForm" class="px-20">
                <input type="hidden" class="hidden" id="hideFields" name="fileName"/> 
                <div class="row">
                    <div class="col-xs-12 col-md-6">
                        <div class="form-group">
                            <label for="payId">Select Merchant</label>
                            <s:select
                                name="payId"
                                class="selectpicker"
                                data-style="form-control py-5 px-10 height-auto full-width m-0 merchantAdmin"
                                id="payId"
                                headerKey="ALL"
                                headerValue="ALL"
                                list="merchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                            />
                        </div>
                    </div>
                    <div class="col-xs-12 col-md-6">
                        <div class="form-group">
                            <label for="csvfile">Select CSV / XLSX File</label>
                            <input type="file" accept=".xlsx, .csv" name="csvfile" id="csvfile" class="form-control full-width py-5 height-auto m-0" onchange='sendfileName(this);'>
                        </div>
                    </div>
                </div>
                
                <div class="d-flex justify-content-center mt-10">
                    <input type="submit" value="Submit" id="subBtn" class="download-btn lpay_button lpay_button-secondary px-13 mt-15 mx-0" disabled>
                </div>
            </form>
        </div>
    </div>

    <div class="row border-bottom-grey-lightest py-10 text-center d-none" id="result-wrapper">
        <div class="col-xs-12">
            <div id="error-count" class="font-size-16"></div>
            <div id="error-invalid" class="font-size-16"></div>
        </div>        
    </div>

    <div class="row mt-40">
        <div class="col-sm-3 text-left">
            <label for="merchantId">Merchant</label>
            <s:select
                name="merchantId"
                class="selectpicker view-emi-filter"
                data-style="form-control py-5 px-10 height-auto full-width ml-0"
                id="merchantId"
                headerKey="ALL"
                headerValue="ALL"
                list="merchantList"
                listKey="payId"
                listValue="businessName"
                autocomplete="off"
            />
        </div>
        <div class="col-sm-3 text-left">
            <label for="acquirer">Issuer</label>
            <s:select
                class="selectpicker view-emi-filter"
                data-style="form-control py-5 px-10 height-auto full-width ml-0"
                headerKey="ALL"
                headerValue="ALL"                        
                list="@com.paymentgateway.commons.util.IssuerType@values()"
                listKey="code"
                listValue="name"
                name="acquirer"
                id="acquirer"
                autocomplete="off"
            />
        </div>        
    </div>

    <div class="row mt-20">
        <div class="col-xs-12 max-height-400 overflow-y-auto">
            <table class="table lpay_table" id="issuerDetail">
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
</div>

<script src="../js/issuerDetail.js"></script>
<script src="../js/decimalLimit.js"></script>

</body>
</html>