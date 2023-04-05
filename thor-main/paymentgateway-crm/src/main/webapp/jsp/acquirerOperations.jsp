<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Acquirer Operations</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
</head>
<body class="bodyColor">
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Operational</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active">
                        <a href="#" class="lpay-nav-link" data-id="sbi">SBI</a>
                    </li>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100" data-target="sbi">
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date From</label>
                        <input type="text" readonly id="dateFrom" data-var="dateFrom" class="datepick lpay_input">
                    </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date To</label>
                        <input type="text" readonly id="dateTo" data-var="dateTo" class="datepick lpay_input">
                    </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Payment Type</label>
                        <s:select headerKey="ALL" data-live-search='true' headerValue="ALL" class="lpay_input selectpicker" data-var="paymentType" list="@com.paymentgateway.commons.util.PaymentType@values()" listValue="name" listKey="code" name="paymentMethod" id="paymentMethods" autocomplete="off" />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Acquirer Type</label>
                        <input type="text" data-live-search='true' data-var="acquirerCode" value="SBI" readonly class="datepick lpay_input">
                    </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-12 text-center ">
                    <button onclick="downloadForm(this)" data-click="download" class="lpay_button lpay_button-md lpay_button-primary">
                        Download
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                    <button onclick="downloadForm(this)" data-click="download-2" class="lpay_button lpay_button-md lpay_button-secondary">
                        Download-2
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content -->
            
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <form action="downloadOperationalSBIReportAction" id="downloadSBI"></form>
    <script src="../js/acquirerOperation-script.js"></script>
</body>
</html>