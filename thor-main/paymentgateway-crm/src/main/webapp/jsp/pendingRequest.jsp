<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Pending Request</title>
	<link rel="icon" href="../image/favicon-32x32.png">

	<link rel="stylesheet" href="../css/Jquerydatatableview.css" />
	<!-- <link rel="stylesheet" href="../css/Jquerydatatable.css" /> -->
	<link rel="stylesheet" href="../css/loader.css">
	<!-- <link rel="stylesheet" href="../css/default.css" /> -->
	<!-- <link rel="stylesheet" href="../css/jquery-ui.css" /> -->
	<link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
	<link rel="stylesheet" href="../css/messi.css" />
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/pendingRequest.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery.fancybox.min.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>

	<script src="../js/messi.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
</head>
<body id="mainBody">

	<s:textfield type="hidden" value="%{#session.USER.UserType.name()}" id="userType" />
	
	<section class="pending_request lpay_section white-bg box-shadow-box mt-70 p20">
	   <div class="row">
		  	<div class="col-md-12">
			 <div class="heading_with_icon mb-30">
			   <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
			   <h2 class="heading_text">Pending Request</h2>
			 </div>
			 <!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
		  	<div class="col-md-12 mb-20">
				<ul class="lpay_tabs d-flex">
					<li class="lpay-nav-item active">
						<a href="#" class="lpay-nav-link" data-id="merchantMapping">Merchant Mapping</a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="paymentOptions">Payment Options</a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="chargingDetails">Charging Details</a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="bulkUpdate">Bulk Charges Update</a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-id="reporting">Reporting</a>
					</li>
				</ul>
				<!-- /.lpay_tabs -->
			</div>
			<!-- /.col-md-12 -->
			<div class="lpay_tabs_content w-100" data-target='merchantMapping'>
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="merchantMapping-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Business Name</th>
									<th>Acquirer Name</th>
									<th>Currency</th>
									<th>Created Date</th>
									<th>Status</th>
									<th>Requested By</th>
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
			<div class="lpay_tabs_content d-none w-100" data-target='paymentOptions'>
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="paymentOptions-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Business Name</th>
									<th>Created Date</th>
									<th>Status</th>
									<th>Requested By</th>
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
			<div class="lpay_tabs_content d-none w-100" data-target='chargingDetails'>
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="chargingDetails-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Business Name</th>
									<th>Acquirer Name</th>
									<th>Currency</th>
									<th>Created Date</th>
									<th>Status</th>
									<th>Requested By</th>
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
			<div class="lpay_tabs_content w-100 d-none" data-target='bulkUpdate'>
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="bulkUpdate-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Business Name</th>
									<th>Acquirer Name</th>
									<th>Currency</th>
									<th>Created Date</th>
									<th>Status</th>
									<th>Requested By</th>
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
			<div class="lpay_tabs_content w-100 d-none"  data-target='reporting'>
				<div class="col-md-3 mb-20 ">
					<div class="lpay_select_group">
					   <label for="">Report Name</label>
					   <select class="selectpicker" data-actions-box="true" data-download="reportType" data-var="reportType" name="reportType" id="reportType">
						   <option value="">Select Report</option>
						   <option value="bulkCharges">Bulk Charges</option>
						   <option value="chargingDetails">Charging Details</option>
						   <option value="merchantMapping">Merchant Mapping</option>
						   <option value="paymentOption">Payment Options</option>
					   </select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
				<div class="col-md-3 mb-20 ">
					<div class="lpay_select_group">
					   <label for="">Status</label>
					   <select class="selectpicker" data-actions-box="true" data-download="reportStatus" data-var="reportStatus" name="reportStatus" id="reportStatus">
							<option value="">Select Status</option>
							<option value="ALL">ALL</option>
							<option value="ACTIVE">APPROVED</option>
							<!-- <option value="INACTIVE">INACTIVE</option> -->
							<option value="REJECTED">REJECTED</option>
							<!-- <option value="CANCELLED">CANCELLED</option> -->
					   </select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="pendingRequest-table" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Business Name</th>
									<th>Acquirer Name</th>
									<th>Create Date</th>
									<th>Status</th>
									<th>Requested By</th>
									<th>Updated Date</th>
									<th>Accepted By</th>
								</tr>
							</thead>	
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.lpay_tabs_content -->
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
                <span>Do you really want to perform this action ? This process cannot be undone.</span>
            </div>
            <!-- /.confirm-delete-text -->
            <div class="confirm-delete-button">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn-fancy">No</button>
                <button class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">Yes</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
    <!-- /.confrim-popup -->

	<script src="../js/pendingRequest.js"></script>
</body>
</html>