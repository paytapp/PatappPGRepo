<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Chargeback Refund Utility</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/dataTables.buttons.js"></script>
    <script src="../js/common-scripts.js"></script>

    <style>
        .lp-refund_success{ margin-bottom: 10px;font-size: 13px;font-weight: 500;color: #2bb743;text-align: left; }
        .lp-refund_error{ margin-bottom: 10px;font-size: 13px;font-weight: 500;color:  #d20e0e;text-align: left; }
        .lpay_popup .lpay_popup-inner .lpay_popup-innerbox > div{ display: block !important; }
        .lpay_popup .lpay_popup-inner .lpay_popup-innerbox{ max-width: 500px !important; }
        .error-utility .btn-default{ border: 1px solid #ffa3a3 }
    </style>
  
</head>
<body>
    <s:hidden value="%{response}" id="refundResponse"></s:hidden>
	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Chargeback Refund Utility</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 mb-20">
				<ul class="lpay_tabs d-flex">
					<li class="lpay-nav-item active">
						<a href="#" class="lpay-nav-link" data-id="uploadRefundBatchFile">Upload Batch File</a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="chargebackReporting">Chargeback Refund</a>
					</li>
                    <li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="chargebackCreation">Chargeback Creation</a>
					</li>
                    <li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="chargebackClosure">Chargeback Closure</a>
					</li>
				</ul>
				<!-- /.lpay_tabs -->
			</div>
			<!-- /.col-md-12 -->
            <div class="lpay_tabs_content w-100" data-target='uploadRefundBatchFile'>
                <div class="col-md-4">
                    
                    <form action="chargebackUtilAction" id="addBulkUsers" method="post" enctype="multipart/form-data">
                        <div class="lpay_select_group mb-20">
                            <label for="">Utility Type</label>
                            <select class="selectpicker" data-download="utilityType" data-var="utilityType" name="utilityType" id="utilityType">
                                <option value="">Select Utility Type</option>
                                <option value="chargebackRefund">Chargeback Refund</option>
                                <option value="chargebackCreation">Chargeback Creation</option>
                                <option value="chargebackClosure">Chargeback Closure</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                        <label for="upload-input" class="lpay-upload">
                            <input type="file" name="csvFile" accept=".csv" id="upload-input" class="lpay_upload_input">
                            <div class="default-upload">
                                <h3>Upload Your CSV File</h3>
                                <img src="../image/image_placeholder.png" class="img-responsive" id="placeholder_img" alt="">
                            </div>
                            <!-- /.default-upload -->
                            <div class="upload-status">
                                <div class="success-wrapper upload-status-inner d-none">
                                    <div class="success-icon-box status-icon-box">
                                        <img src="../image/tick.png" alt="">
                                    </div>
                                    <div class="success-text-box">
                                        <h3>Upload Successfully</h3>
                                        <div class="fileInfo">
                                            <span id="fileName"></span>
                                        </div>
                                        <!-- /.fileInfo -->
                                    </div>
                                    <!-- /.success-text-box -->
                                </div>
                                <!-- /.success-wraper -->
                                <div class="error-wrapper upload-status-inner d-none">
                                    <div class="error-icon-box status-icon-box">
                                        <img src="../image/wrong-tick.png" alt="">
                                    </div>
                                    <div class="error-text-box">
                                        <h3>Upload Failed</h3>
                                        <div class="fileInfo">
                                            <div id="fileName" class='fileTypeError'>File size too Long.</div>
                                        </div>
                                        <!-- /.fileInfo -->
                                    </div>
                                    <!-- /.success-text-box -->
                                </div>
                                <!-- /.success-wraper -->
                            </div>
                            <!-- /.upload-success -->
                        </label>
                        <div class="button-wrapper lpay-center mt-20">
                            <button class="lpay_button lpay_button-md lpay_button-secondary" disabled id="bulkUpdateSubmit">Submit</button>
                            <!-- create table for download csv format -->
                            <table id="example" class="display nowrap" style="display: none;">
                                <thead>
        
                                    <tr>
                                        <th>Pay Id</th>
                                        <th>Order Id</th>
                                        <th>Amount</th>
                                    </tr>
                                    
                                </thead>
                            </table>
                            <table id="chargebackClosure" class="display nowrap" style="display: none;">
                                <thead>
        
                                    <tr>
                                        <th>Pay Id</th>
                                        <th>Order Id</th>
                                    </tr>
                                    
                                </thead>
                            </table>
                        </div>
                        <!-- /.button-wrapper -->
                    </form>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- lpay_tabs_content -->
            <div class="lpay_tabs_content d-none w-100" data-target='chargebackReporting'>
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date From</label>
                      <s:textfield type="text"  data-download="dateFrom" data-var="dateFrom" id="dateFrom-chargebackReporting" name="dateFrom"
                      class="lpay_input date-refund_data" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                  <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date To</label>
                      <s:textfield type="text"  data-download="dateTo" data-var="dateTo" id="dateTo-chargebackReporting" name="dateTo"
                      class="lpay_input date-refund_data" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3" style="margin-top: 17px;">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="view-chargebackReport">
                        View
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12">
					<div class="lpay_table">
						<table id="refundReporting-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Total File Entries</th>
									<th>Valid Entries</th>
									<th>Invalid Entries</th>
									<th>Capture</th>
									<th>Rejected</th>
									<th>Declined</th>
									<th>Error</th>
									<th>Denied</th>
									<th>Failed</th>
									<th>Invalid</th>
									<th>Auth Failed</th>
									<th>Acquirer Down</th>
									<th>Failed at Acquirer</th>
									<th>Acquirer Time out</th>
                                    <th>Create Date</th>
                                    <th>Action</th>
								</tr>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				  </div>
				  <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->
            <div class="lpay_tabs_content d-none w-100" data-target='chargebackCreation'>
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date From</label>
                      <s:textfield type="text"  data-download="dateFrom" data-var="dateFrom" id="dateFrom-chargebackCreation" name="dateFrom"
                      class="lpay_input date-refund_data" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                  <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date To</label>
                      <s:textfield type="text"  data-download="dateTo" data-var="dateTo" id="dateTo-chargebackCreation" name="dateTo"
                      class="lpay_input date-refund_data" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3" style="margin-top: 17px;">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="view-chargebackCreation">
                        View
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12">
					<div class="lpay_table">
						<table id="chargebackCreation-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Total Count</th>
									<th>Valid Entries</th>
									<th>Invalid Entries</th>
									<th>Total Created</th>
									<th>Total Exception</th>
                                    <th>Action</th>
								</tr>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				  </div>
				  <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->
            <div class="lpay_tabs_content d-none w-100" data-target='chargebackClosure'>
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date From</label>
                      <s:textfield type="text"  data-download="dateFrom" data-var="dateFrom" id="dateFrom-chargebackClosure" name="dateFrom"
                      class="lpay_input date-refund_data" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                  <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                      <label for="">Date To</label>
                      <s:textfield type="text"  data-download="dateTo" data-var="dateTo" id="dateTo-chargebackClosure" name="dateTo"
                      class="lpay_input date-refund_data" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3" style="margin-top: 17px;">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="view-chargebackClosure">
                        View
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12">
					<div class="lpay_table">
						<table id="chargebackClosure-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Total Count</th>
									<th>Valid Entries</th>
									<th>Invalid Entries</th>
									<th>Total Closed</th>
									<th>Total Exception</th>
                                    <th>Action</th>
								</tr>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				  </div>
				  <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->
        </div>
        <!-- /.row -->
    </section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

    <form action='downloadChargebackRefundReportAction' id='lp-download_refund'>
        <s:hidden name='fileName' id="lp-refund_filename" />
        <s:hidden name="token" value="%{#session.customToken}" />
    </form>
    <!-- <form action='downloadChargebackCreationReportAction' id='lp-download_refund'>
        <s:hidden name='fileName' id="lp-refund_filename" />
        <s:hidden name="token" value="%{#session.customToken}" />
    </form>
    <form action='downloadChargebackClosureReportAction' id='lp-download_refund'>
        <s:hidden name='fileName' id="lp-refund_filename" />
        <s:hidden name="token" value="%{#session.customToken}" />
    </form> -->

    <s:if test="%{dataValidationMap.totalCount != ''}">
        <div class="lpay_popup" style="display: block;">
            <div class="lpay_popup-inner">
                <div class="lpay_popup-innerbox" data-status="default">
                    <div class="lpay_popup-innerbox-success lpay-center">
                        <div class="lpay_popup-content mb-20">
                            <s:if test="%{response == 'success'}">
                                <div class="refund_response lp-refund_success">
                                    <s:property value="%{responseMsg}" />
                                </div>
                                <!-- /.lp-refund_success -->
                            </s:if>
                            <s:if test="%{response == 'error'}">
                                <div class="refund_response lp-refund_error">
                                    <s:property value="%{responseMsg}" />
                                </div>
                                <!-- /.lp-refund_success -->
                            </s:if>
                            <table style="width: 100%;border: 1px solid #ddd;text-align: left;" class="lpay_custom_table">
                                <tbody>
                                    <tr>
                                        <td>Total Count</td>
                                        <td><s:property value="%{dataValidationMap.totalCount}" /></td>
                                    </tr>
                                    <tr>
                                        <td>Total Valid Entry</td>
                                        <td><s:property value="%{dataValidationMap.totalValid}" /></td>
                                    </tr>
                                    <tr>
                                        <td>Total Invalid Entry</td>
                                        <td><s:property value="%{dataValidationMap.totalInvalid}" /></td>
                                    </tr>
                                </tbody>
                            </table>
                            <!-- /.lpay_custom_table -->
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
    </s:if>
    
	<script src="../js/chargebackRefundUtility-script.js"></script>
    <script>
        if ( window.history.replaceState ) {
            window.history.replaceState( null, null, window.location.href );
        }
    </script>
</body>
</html>