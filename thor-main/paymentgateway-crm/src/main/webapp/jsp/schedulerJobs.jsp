<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Scheduler</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/loader.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="../css/bootstrap-flex.css">
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script type="text/javascript" src="../js/moment-with-locales.js"></script>
<link rel="stylesheet" href="../css/bootstrap-datetimepicker.css">
<link rel="stylesheet" href="../css/jquery.fancybox.min.css">


<script src="../js/jquery.js"></script>
<script src="../js/jquery-ui.js"></script>
<!-- <script src="../js/bootstrap-select.min.js"></script> -->
<script type="text/javascript" src="../js/bootstrap-datetimepicker.js"></script>
<script src="../js/jquery.fancybox.min.js"></script>
<link rel="stylesheet" href="../css/scheduler-style.css">
</head>
<body>
	<section class="block-div scheduler-section lpay-white-default-bg">
	<div class="scheduler-blocks block-div">

		<div class="save-status-div">
			<h4>Your job save successfully</h4>
		</div>
		<!-- /.success-div -->


		<div class="delete-status-div">
			<h4>Your job deleted successfully</h4>
		</div>
		<!-- /.success-div -->

		<div class="lpay-heading position-relative">
			<h3>Manage Scheduler Jobs</h3>
		</div>

		<div class="scheduler-manage-div" style="margin: 0 -10px">
			<div class="col-md-4">
				<s:select class="lpay-input link-edit" headerKey="All" headerValue="Select Jobs" list="@com.paymentgateway.commons.util.JobType@values()"
					listKey="code" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="jobType" id="jobType" autocomplete="off" />
			</div>

			<div class="col-md-4 create-job-btn">
				<button id="createJob" class="lpay-button link-edit">Create New Job</button>
			</div>
		</div>
		<!-- /.scheduler-manage-div -->
	</div>
	<!-- /.scheduler-blocks -->
	<div class="scheduler-blocks d-none block-div mt-30"
		id="scheduler-manage-div">
		<input type="hidden" data-id="jobId">
		<div class="lpay-heading position-relative">
			<h3 id="jobHeading">Create New Jobs</h3>
		</div>
		<div class="scheduler-blocks-input" style="margin: 0 -10px"
			id="scheduler-input-block">
			<div class="dynamically-added-checkbox w-100" style="margin-bottom: 20px;display: flex"></div>
			<div class="col-md-3 static-select d-none mb-30">
				<s:select name="merchant" class="lpay-input lpay-select" id="payId"
					list="merchantList" listKey="payId"
					listValue="businessName" autocomplete="off" />
				<div class="aqquirer-box">
					<div class="acquirer-input open-select-box">
						<input type="text" data-id="payId" class="lpay-input"
							placeholder="Select Merchant">
							<div class="showData"></div>
					</div>
					<!-- /.acquirer-input -->
					<div class="payId-div select-box-custom"></div>
					<!-- /#acquirerType -->
				</div>
				<!-- /.aqquirer-box -->
			</div>
			<!-- /.col-md-3 static-select d-none mb-30 -->

			<div class="col-md-3 static-select d-none mb-20">
				<s:select class="lpay-input lpay-select"
					list="@com.paymentgateway.commons.util.PaymentType@values()"
					listKey="code" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="paymentType" id="paymentType"
					autocomplete="off" />
				<div class="aqquirer-box">
					<div class="acquirer-input open-select-box">
						<input type="text" data-id="paymentType" class="lpay-input"
							placeholder="Payment Type">
						<div class="showData"></div>
						<!--class/#showData -->
					</div>
					<!-- /.acquirer-input -->
					<div class="paymentType-div select-box-custom"></div>
					<!-- /#acquirerType -->
				</div>
				<!-- /.aqquirer-box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 static-select d-none mb-20">
				<select name="status" id="status" class="lpay-select lpa-input">
					<option value="Declined">Declined</option>
					<option value="Rejected">Rejected</option>
					<option value="Pending">Pending</option>
					<option value="Timeout">Timeout</option>
					<option value="Error">Error</option>
					<option value="Cancelled">Cancelled</option>
					<option value="Denied by risk">Denied by risk</option>
					<option value="Duplicate">Duplicate</option>
					<option value="Failed">Failed</option>
					<option value="Invalid">Invalid</option>
					<option value="Authentication Failed">Authentication Failed</option>
					<option value="Sent to Bank">Sent to Bank</option>
					<option value="Denied due to fraud">Denied due to fraud</option>
					<option value="Acquirer down">Acquirer down</option>
					<option value="Processing">Processing</option>
					<option value="Failed at Acquirer">Failed at Acquirer</option>
					<option value="Timed out at Acquirer">Timed out at Acquirer</option>

				</select>
				<!-- <s:select class="lpay-input lpay-select"
					list="@com.paymentgateway.commons.util.StatusType@values()"
					listKey="name" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="status" id="status" autocomplete="off" /> -->
				<div class="aqquirer-box">
					<div class="acquirer-input open-select-box">
						<input type="text" data-id="status" class="lpay-input"
							placeholder="Status">
							<div class="showData"></div>
					</div>
					<!-- /.acquirer-input -->
					<div class="status-div select-box-custom"></div>
					<!-- /#acquirerType -->
				</div>
				<!-- /.aqquirer-box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 static-select d-none mb-20">
				<s:select class="lpay-input lpay-select"
					list="@com.paymentgateway.commons.util.TxnType@values()"
					listKey="code" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="TransactionType" id="txnType"
					autocomplete="off" />
				<div class="aqquirer-box">
					<div class="acquirer-input open-select-box">
						<input type="text" data-id="txnType" class="lpay-input"
							placeholder="Txn Type">
							<div class="showData"></div>
					</div>
					<!-- /.acquirer-input -->
					<div class="txnType-div select-box-custom"></div>
					<!-- /#acquirerType -->
				</div>
				<!-- /.aqquirer-box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 static-select d-none mb-20">
				<s:select class="lpay-input lpay-select"
					list="@com.paymentgateway.commons.util.TransactionType@values()"
					listKey="code" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="TransactionType" id="transactionType"
					autocomplete="off" />
				<div class="aqquirer-box">
					<div class="acquirer-input open-select-box">
						<input type="text" data-id="transactionType" class="lpay-input"
							placeholder="Transaction Type">
					</div>
					<!-- /.acquirer-input -->
					<div class="transactionType-div select-box-custom"></div>
					<!-- /#acquirerType -->
				</div>
				<!-- /.aqquirer-box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 static-select d-none mb-20">
				<s:select class="lpay-input lpay-select"
					list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
					listKey="code" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="AcquirerTypeUI" id="acquirerType"
					autocomplete="off" />
				<div class="aqquirer-box">
					<div class="acquirer-input open-select-box">
						<input type="text" data-id="acquirerType" class="lpay-input"
							placeholder="Acquirer Type">
							<div class="showData"></div>
					</div>
					<!-- /.acquirer-input -->
					<div class="acquirerType-div select-box-custom"></div>
					<!-- /#acquirerType -->
				</div>
				<!-- /.aqquirer-box -->
			</div>
			<!-- /.col-md-3 -->
			<div class="dynamically-added-input w-100"></div>
			<!-- /.dynamically-added-input -->
			<div class="col-md-3 mb-20 active-input">
				<div class="acquirer-input">
					<input type="text" id="jobTime" data-id="jobTime"
						placeholder="Start Date" class="lpay-input">
				</div>
				<!-- /.acquirer-input -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-3 mb-20">
				<s:select class="lpay-input"
					list="@com.paymentgateway.commons.util.JobFrequency@values()"
					listKey="code" data-style="form-control mt-10 ml-0 max-width-250"
					listValue="name" name="jobFrequency" id="jobFrequency"
					autocomplete="off" />
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20 active-input">
				<div class="acquirer-input">
					<input type="text" data-id="jobDetails" placeholder="Job Details"
						class="lpay-input">
						
				</div>
				<!-- /.acquirer-input -->
			</div>
			<!-- /.col-md-4 -->
			
			
		</div>
		<!-- /.scheduler-blocks-input -->
		<div class="scheduler-save w-100 text-center">
			<button class="lpay-button cancel-btn" id="cancel-btn">Cancel</button>
			<button class="lpay-button" id="save-btn">save job</button>
			<button class="lpay-button d-none" id="update-btn">update job</button>
		</div>

		<!-- /.scheduler-save -->
	</div>
	<!-- /.scheduler-blocks -->
	<div class="scheduler-blocks block-div mt-30">
		<table class="table table-striped">
			<thead class="merchnatStatus-head">
				<tr>
					<th>Job Type</th>
					<th>Job Time</th>
					<th>Job Frequency</th>
					<th>Job Details</th>
					<!-- <th>URL</th> -->
					<th>Edit</th>

				</tr>
			</thead>
			<tbody class="merchantStatus-data" id="jobData">

			</tbody>
			<tfoot class="merchantStatus-data table-foot">
				<tr>
					<td colspan="6" align="center">
						<span style="width: 100%;text-align: center;">
							No data available
						</span>
					</td>
				</tr>
			</tfoot>
		</table>
		<div id="id"></div>
	</div>
	<!-- /.scheduler-blocks --> </section>

	<div class="confrim-popup"  id="fancybox">
        <div class="confirm-popup-box text-center">
            <div class="confirm-delete-text">
                <h3>Are you sure ?</h3>
                <span>This process cannot be undone !</span>
            </div>
            <!-- /.confirm-delete-text -->
            <div class="confirm-delete-button">
                <button class="primary-btn cancel-btn" id="cancel-btn">cancel</button>
                <button  class="primary-btn" id="confirm-btn">confirm</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
	<!-- /.confrim-popup -->
	
	<!-- /.scheduler-section -->
	<s:hidden name="token" value="%{#session.customToken}" />
	<!-- <s:hidden name="payId" value="%{#session.payId}" /> -->

	<script src="../js/scheduler.js"></script>
</body>
</html>