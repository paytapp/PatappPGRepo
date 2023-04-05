<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>View SUF Details</title>
    <link rel="icon" href="../image/favicon-32x32.png">

    <link href="../css/default.css" rel="stylesheet" type="text/css" />
    <link href="../css/loader.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" href="../css/common-style.css">
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
    <link rel="stylesheet" href="../css/paymentOptions.css">
    <link rel="stylesheet" href="../css/Jquerydatatable.css">
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link href="../css/Jquerydatatableview.css" rel="stylesheet" />

    <script src="../js/jquery.js"></script>
    <script src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
    <script type="text/javascript" src="../js/pdfmake.js"></script>
    <style>
        .viewSufDetailsTable{
            width: 100%;
            float: left;

        }
        .viewSufDetailsTable .dataTables_wrapper {
            margin-top: 0;
        }
        .viewSufDetailsTable a.dt-button{
            padding: 6px 20px;
            font-size: 12px;
            border-radius: 5px;
            font-weight: 300;
            margin-top: 7px;
        }
        .viewSufDetailsTable a.dt-button:hover{
            font-weight: 300;
        }

    </style>
</head>
<body>
    <section class="bg-color-white py-20 px-15 mt-10">
        <div class="row">
            <div class="col-md-12">
                <div class="inner-heading">
                    <h3>View SUF Details Filter</h3>
                </div>
                <!-- /.inner-heading -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-xs-12" id="paymentOptions-info">
                <div class="row">
                    <div class="col-md-3">
                        <div class="lpay_select_group">
                            <label for="">Select Merchant</label>
                            <s:select
                                name="payId"
                                class="selectpicker filter_suf"
                                data-style="form-control ml-0 max-width-250"
                                id="filter-payId"
                                headerKey="ALL"
                                headerValue="ALL"
                                list="merchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                                title="ALL"
                            />
                        </div>
                        <!-- /.lpay_select_group -->
                    </div>
                    <!-- /.col-md-3 -->
                    <div class="col-md-3">
                        <div class="lpay_select_group">
                            <label for="">Transaction Type</label>
                            <select name="txnType" id="filter_txnType" class="selectpicker filter_suf" data-style="form-control ml-0 max-width-250" title="Select Transaction Type">
                                <option value="">Select Txn Type</option>
                                <option value="Sale">Sale</option>
                                <option value="Refund">Refund</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->
                    </div>
                    <!-- /.col-md-3 -->

                    <div class="col-md-3">
                        <div class="lpay_select_group">
                            <label for="">Payment Region</label>
                            <select name="paymentRegion" id="paymentRegion" class="selectpicker filter_suf" data-style="form-control ml-0 max-width-250" title="Select Payment Region">
                                <option value="">Select Payment Region</option>
                                <option value="Domestic">Domestic</option>
                                <option value="International">International</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->
                    </div>
                    <!-- /.col-md-3 -->

                    <div class="col-md-3 d-none" data-id="filter_paymentType">
                        <div class="lpay_select_group">
                            <label for="">Payment Type</label>
                            <select name="paymentType" id="filter_paymentType" class="selectpicker filter_suf" data-style="form-control ml-0 max-width-250"></select>
                        </div>
                        <!-- /.lpay_select_group -->
                    </div>
                </div>
                <!-- /.row -->
                <div class="row">
                    <div class="col-md-12">
                        <div class="inner-heading mt-20">
                            <h3>View SUF Details Table</h3>
                        </div>
                        <!-- /.inner-heading -->
                    </div>
                    <!-- /.col-md-12 -->
                </div>
                <!-- /.row -->
                <div class="max-height-400 overflow-auto viewSufDetailsTable">
                    <table class="display mt-20" id="myTable">
                        <thead class="bg-color-primary color-white border-primary border-bottom-none ">
                            <tr>
                                <th class="border-bottom-none" width="150">Merchant Name</th>
                                <th class="border-bottom-none" width="100">Txn Type</th>
                                <th class="border-bottom-none">Mop Type</th>
                                <th class="border-bottom-none">Payment Type</th>
                                <th class="border-bottom-none">Payment Region</th>
                                <th class="border-bottom-none">Fixed Charge</th>
                                <th class="border-bottom-none">Percentage Amount</th>
                            </tr>
                        </thead>
                    </table>
                    <!-- /.payment-options -->
                </div>
            </div>
            <!-- /.col-xs-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.payment-payment-type-boxper -->

    <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    <script src="../js/viewSufDetails.js"></script>
</body>
</html>