<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Transaction Info Report</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/common-scripts.js"></script>

    <style>
        .error-field{ display: none; }
        .hasError .error-field{ position: absolute;top: 0;right: 0;color: #f00;display: block; }
    </style>
  
</head>
<body>

	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Transaction Info Report</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="row m-0" data-active="customQrReport">

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Customer Name</label>
                        <input type="text" data-var="custName" name="custName" id="custName" class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Customer Email</label>
                        <input type="text" oninput="removeErrorField(this)" onchange="checkRegEx(this)" data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$" data-var="custEmail" name="custEmail" id="custEmail" class="lpay_input">
                        <span class="error-field"></span>
                        <!-- /.error-field -->
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Customer Mobile</label>
                        <input type="text" data-var="custPhone" onkeypress="onlyDigit(event)" oninput="removeErrorField(this)" onchange="checkRegEx(this)" data-reg="[0-9]{10}" name="custPhone" id="custPhone" class="lpay_input">
                        <span class="error-field"></span>
                        <!-- /.error-field -->
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">RRN Number</label>
                        <input type="text" data-var="rrn" name="rrn" id="rrn" class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date From</label>
                      <s:textfield type="text" data-download="dateFrom" data-var="dateFrom" id="dateFrom" name="dateFrom"
                      class="lpay_input" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>

                  <!-- /.col-md-3 mb-20 -->
                  <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date To</label>
                      <s:textfield type="text" data-download="dateTo" data-var="dateTo" id="dateTo" name="dateTo"
                      class="lpay_input" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                  </div>
                  <!-- /.col-md-3 -->

                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Virtual Account Number</label>
                        <input type="text" onkeypress="lettersAndAlphabet(event)" maxlength="12" data-var="virtualAccountNo" name="virtualAccountNo" id="virtualAccountNo" class="lpay_input">
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <!-- /.col-md-4 -->
                
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Txn Type</label>
                        <select data-var="txnType" name="txnType" class="selectpicker" id="txnType">
                            <option value="ALL">ALL</option>
                            <option value="DEBIT">Debit</option>
                            <option value="CREDIT">Credit</option>
                        </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
    
            <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="view">View</button>
                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                <button class="lpay_button lpay_button-md lpay_button-primary" onclick="downloadReport()" id="download">Download</button>
                <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="transactionInfoReport" class="display" cellspacing="0" width="100%">
                        <thead>
                             <tr class="lpay_table_head">
                                <th>Virtaul Account Number</th>
                                <th>Customer Name</th>
                                <th>RRN</th>
                                <th>Txn Type</th>
                                <th>Amount</th>
                                <th>Create Date</th>
                                <th>Status</th>
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
    <s:form id="downloadForm">

    </s:form>
    
	<script src="../js/transactionInfoReport.js"></script>
</body>
</html>