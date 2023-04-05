<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>eNACH</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<link rel="stylesheet" href="../css/singleAccount.css">

<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<!-- <script src="../js/common-scripts.js"></script> -->
<!-- <script src="../js/user-script.js"></script> -->
<script src="../js/common-validations.js"></script>

<style>
	tr{
		cursor: pointer;
	}
	.common-status{ padding: 5px 10px 4px;border-radius: 5px;font-weight: 600;background-color:#6fa6ff;color: #082552; }
	.active-status{ background-color: #c5f196;color: #3c6411; }
	.suspended-status{ background-color: #ccc;color: #464646; }
	.pending-status{ background-color: #ffda70;color: #8f6c08 }
	.terminated-status{ background-color: #f9a7a7;color: #6a1111; }
	.rejected-status{ background-color: #9f0c0c;color: #fff; }

	button.copy-btn, button.act-btn {
		display: inline-block;
		padding: 6px 10px 4px;
		border-radius: 5px;
		border: none;
		background-color: #ddfdf7;
		border: 1px solid #87c5b9;
		font-weight: 600;
		text-transform: uppercase;
	}

	button.act-btn{
		background-color: #5cdf5a;
		border: 1px solid #529951;
		color: #0a5009;
	}

	#datatable tr{
		cursor: default;
	}

	td.debit-column.lpay_select_group .btn-group {
		margin-left: -6px;
	}

	td.debit-column .bootstrap-select.open .dropdown-toggle:focus, td.debit-column.lpay_select_group .bootstrap-select .dropdown-toggle:hover{ background-color: #fff !important; }

	.response-popup {
            position: fixed;
            top: 0px;
            right: 0;
            left: 0;
            bottom: 0px;
            background-color: rgba(0, 0, 0, 0.8);
            z-index: 99;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .lpay_table{
            white-space: nowrap;
        }

		.imp{
			color: #f32a2a;
			margin-left: 2px;
		}

        .response-popup--inner {
            width: 400px;
            background-color: #fff;
            padding: 30px;
            display: flex;
            align-items: center;
            flex-direction: column;
        }

        .response-popup--inner .response-msg {
            margin-top: 20px;
            margin-bottom: 20px;
            font-size: 12px;
            text-align: center;
        }

        .response-btn a {
            background: #23527c;
            padding: 8px 18px;
            color: #fff;
            text-decoration: none;
            font-size: 14px;
            border-radius: 5px;
        }
        .debit_popup_inner{
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
        }
        .debit_popup_container .debit_popup_box{
            position: static !important;
            max-height: 100% !important;

        }
        .debit_popup_container .button-wrapper{
            top: 15px;
        }

		.debit_duration-input{
			display: inline-block;
			width: 100%;
			text-align: center;
			border-left: 15px solid #fff;
			border-right: 15px solid #fff;
			padding: 20px;
			background-color: #f5f5f5;
		}

		.debit_duration-input label{
			text-align: left;
		}

		.debit_duration-input > div{
			float: none;
			display: inline-block;
		}

		.action-btn > *, .action-btn-edit > *{
			display: inline-flex;
			width: 35px;
			height: 35px;
			align-items: center;
			justify-content: center;
			border-radius: 50%;
			background-color: #fff;
			box-shadow: 0 0 4px rgb(0 0 0 / 20%);
			font-size: 13px;
		}

		.action-btn .delete-btn, .action-btn-edit .delete-btn{
			background-color: #f32a2a;
			color: #fff;
			margin-left: 10px;
		}

		.action-btn-edit{
			display: none;
		}

		.edit-row .action-btn{
			display: none;
		}

		.edit-row .action-btn-edit{
			display: block;
		}

		button:disabled{
			opacity: .5;
		}

</style>
<link rel="stylesheet" href="../css/horizontal-scrolling-nav.css">
<link rel="stylesheet" href="../css/common-style.css">
</head>
<body id="mainBody">
	<s:hidden id="userType" value='%{#session.USER.UserType.name()}'></s:hidden>
	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12 heading-div">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">eNach</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="horizontal-nav-wrapper mb-20">
					<nav id="horizontal-nav" class="horizontal-nav">
						<ul class="horizontal-nav-content lpay_tabs list-unstyled font-size-14 merchant-config-tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
							<s:if test="%{#session.USER.UserType.name()!='SUBUSER' && #session.USER.UserType.name()!='RESELLER' && #session['USER'].superMerchant != true}">
								<li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="quickLinks">eMandate Registration</a></li>
							</s:if>
							<li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="registrationReport">Registration Report</a></li>
							<li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="transactionReport">Transaction Report</a></li>
							<li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="debitReport">Debit Report</a></li>
							<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
								<li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="debitDuration">Debit Duration</a></li>
							</s:if>
						</ul>
					</nav>

					<button type="button" id="btn-scroll-left" class="horizontal-nav-btn horizontal-nav-btn-left m-0 bg-color-white pr-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
							<path d="M445.44 38.183L-2.53 512l447.97 473.817 85.857-81.173-409.6-433.23v81.172l409.6-433.23L445.44 38.18z" />
						</svg>
					</button>
					<button type="button" id="btn-scroll-right" class="horizontal-nav-btn horizontal-nav-btn-right m-0 bg-color-white pl-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
							<path d="M105.56 985.817L553.53 512 105.56 38.183l-85.857 81.173 409.6 433.23v-81.172l-409.6 433.23 85.856 81.174z" />
						</svg>
					</button>
				</div>
            </div>
			<!-- /.col-md-12 -->

			<div class="lpay_tabs_content w-100 d-none" data-target="registrationReport">
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Order ID</label>
					<s:textfield id="orderId" data-var="orderId" class="lpay_input" name="orderId"
					type="text" value="" autocomplete="off"
					onkeypress="return Validate(event);"
					onblur="this.value=removeSpaces(this.value);"></s:textfield>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">UMRN Number</label>
					  <s:textfield id="umrnNumber" onkeypress="lettersAndAlphabet(event)" data-var="umrnNumber" class="lpay_input" name="umrnNumber"
					  type="text" value="" autocomplete="off" ></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select name="merchantPayId" data-var="merchantPayId" class="selectpicker"
								id="merchant" data-submerchant="subMerchant" data-user="subUser" headerKey="ALL" data-live-search="true" headerValue="ALL"
								list="merchantList" listKey="payId"
								listValue="businessName" autocomplete="off" />
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
					   <div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select name="merchantPayId" class="selectpicker"
								id="merchant" headerKey="" data-var="merchantPayId" data-submerchant="subMerchant" data-user="subUser"  data-live-search="true" headerValue="ALL"
								list="merchantList" listKey="payId"
								listValue="businessName" autocomplete="off" />
						</div>
					   </div>
					</s:if>
						<s:else>
							<div class="col-md-3 mb-20 d-none">
								<div class="lpay_select_group ">
									<label for="">Select Merchant</label>
									<s:select name="merchantPayId" data-var="merchantPayId" data-live-search="true" class="selectpicker" id="merchant"
										list="merchantList" data-submerchant="subMerchant" data-user="subUser" listKey="payId"
										listValue="businessName" autocomplete="off" />
								</div>
							</div>
						</s:else>
					</s:else>
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20" data-target="subMerchant">
						<div class="lpay_select_group">
						   <label for="">Sub Merchant</label>
						   <s:select data-id="subMerchant" data-var="subMerchantPayId" headerKey="ALL" data-submerchant="subMerchant" data-user="subUser"  name="subMerchantPayId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" headerValue="ALL"
								listValue="businessName" autocomplete="off" />
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
						<div class="lpay_select_group">
						   <label for="">Sub Merchant</label>
						   <select name="subMerchantPayId" headerKey="ALL" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" headerValue="ALL"></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Status</label>
					   <select class="selectpicker" data-var="status" data-id="reportStatus" name="status" id="status">
							<option value="ALL">ALL</option>
							<option value="Pending">Pending</option>
							<option value="Cancelled">Canceled</option>
							<option value="Failed">Failed</option>
							<option value="Captured">Captured</option>
							<!-- <option value="Declined">Declined</option>
							<option value="Timeout">Timeout</option> 
							<option value="Failed at Acquirer">Failed at Acquirer</option>
							<option value="Duplicate">Duplicate</option>
							<option value="Invalid">Invalid</option>-->
					   </select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield type="text" data-var="fromDate" id="dateFrom" name="dateFrom"
					class="lpay_input datepick" autocomplete="off" readonly="true" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" data-var="toDate" id="dateTo" name="dateTo"
					class="lpay_input datepick" autocomplete="off" readonly="true" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-12 text-center">
					<input type="button" id="registrationSubmit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary" />
					<button class="lpay_button lpay_button-md lpay_button-primary" id="registrationDownload">
						Download
					</button>
					<!-- /.lpay_button lpay_button-md lpay_button-primary -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="txnResultDataTable" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Merchant</th>
									<th>Order ID</th>
									<th>PG REF Number</th>
									<th>UMRN Number</th>
									<!-- <th>Mop Type</th> -->
									<!-- <th>SUF</th> -->
									<th>Debit Amount</th>
									<th>Status</th>
									<th>Link</th>
									<th>Action</th>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- lpay tabs end -->
			<div class="lpay_tabs_content w-100 d-none" data-target="transactionReport">
				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">Order ID</label>
					  <s:textfield id="orderId" data-transaction="orderId" class="lpay_input" name="orderId"
					  type="text" value="" autocomplete="off"
					  onkeypress="return Validate(event);"
					  onblur="this.value=removeSpaces(this.value);"></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 mb-20 -->
				  <div class="col-md-3 mb-20">
					  <div class="lpay_input_group">
						<label for="">UMRN Number</label>
						<s:textfield id="umrnNumber" data-transaction="umrnNumber" onkeypress="lettersAndAlphabet(event)" class="lpay_input" name="umrnNumber"
						type="text" value="" autocomplete="off" ></s:textfield>
					  </div>
					  <!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 mb-20 -->
				  <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					  <div class="col-md-3 mb-20">
						  <div class="lpay_select_group">
							  <label for="">Select Merchant</label>
							  <s:select name="merchantPayId" data-transaction="merchantPayId" class="selectpicker"
								  id="merchantTransaction" data-submerchant="subMerchantTransaction" data-user="subUserTransaction" headerKey="ALL" data-live-search="true" headerValue="ALL"
								  list="merchantList" listKey="payId"
								  listValue="businessName" autocomplete="off" />
						  </div>
					  </div>
				  </s:if>
				  <s:else>
					  <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						 <div class="col-md-3 mb-20">
						  <div class="lpay_select_group">
							  <label for="">Select Merchant</label>
							  <s:select name="merchantPayId" class="selectpicker"
								  id="merchantTransaction" headerKey="" data-transaction="merchantPayId" data-submerchant="subMerchantTransaction" data-user="subUserTransaction"  data-live-search="true" headerValue="ALL"
								  list="merchantList" listKey="payId"
								  listValue="businessName" autocomplete="off" />
						  </div>
						 </div>
					  </s:if>
						  <s:else>
							  <div class="col-md-3 mb-20 d-none">
								  <div class="lpay_select_group ">
									  <label for="">Select Merchant</label>
									  <s:select name="merchantPayId" data-transaction="merchantPayId" data-live-search="true" class="selectpicker" id="merchantTransaction"
										  list="merchantList" data-submerchant="subMerchantTransaction" data-user="subUserTransaction" listKey="payId"
										  listValue="businessName" autocomplete="off" />
								  </div>
							  </div>
						  </s:else>
					  </s:else>
				  <s:if test="%{#session['USER'].superMerchant == true}">
					  <div class="col-md-3 mb-20" data-target="subMerchantTransaction">
						  <div class="lpay_select_group">
							 <label for="">Sub Merchant d</label>
							 <s:select data-id="subMerchant" data-transaction="subMerchantPayId" headerKey="ALL" data-submerchant="subMerchantTransaction" data-user="subUserTransaction"  name="subMerchantPayId" class="selectpicker" id="subMerchantTransaction" list="subMerchantList" listKey="payId" headerValue="ALL"
								  listValue="businessName" autocomplete="off" />
						  </div>
						  <!-- /.lpay_select_group -->  
					  </div>
					  <!-- /.col-md-3 -->	
				  </s:if>
				  <s:else>
					  <div class="col-md-3 mb-20 d-none" data-target="subMerchantTransaction"> 
						  <div class="lpay_select_group">
							 <label for="">Sub Merchant</label>
							 <select name="subMerchantPayId" headerKey="ALL" headerValue="ALL" data-transaction="subMerchantPayId" data-submerchant="subMerchantTransaction" data-user="subUserTransaction" id="subMerchantTransaction" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" headerValue="ALL"></select>
						  </div>
						  <!-- /.lpay_select_group -->  
					  </div>
					  <!-- /.col-md-3 -->							
				  </s:else>
				  <div class="col-md-3 mb-20">
					  <div class="lpay_select_group">
						 <label for="">Status</label>
						 <select class="selectpicker" data-transaction="status" data-id="reportStatus" name="status" id="status">
							<option value="ALL">ALL</option>
							<option value="Pending">Pending</option>
							<option value="Processing">Processing</option>
							<option value="Failed">Failed</option>
							<option value="Captured">Captured</option>
							<option value="Settled">Settled</option>
						 </select>
					  </div>
					  <!-- /.lpay_select_group -->  
				  </div>
				  <!-- /.col-md-3 -->
			  
				  <div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">Date From</label>
					  <s:textfield type="text" data-transaction="fromDate" id="fromDate" name="fromDate"
					  class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 mb-20 -->
				  <div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">Date To</label>
					  <s:textfield type="text" data-transaction="toDate" id="toDate" name="toDate"
					  class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 -->
				  <div class="col-md-12 text-center">
					  <input type="button" id="transactionSubmit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary" />
					  <button class="lpay_button lpay_button-md lpay_button-primary" id="downloadTransaction">
						  Download
					  </button>
					  <!-- /.lpay_button lpay_button-md lpay_button-primary -->
				  </div>
				  <!-- /.col-md-12 -->
				  <div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="transactionReport" class="display" cellspacing="0"
								width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Merchant</th>
									<th>Order Id</th>
									<th>PG REF Number</th>
									<th>UMRN Number</th>
									<!-- <th>Mop Type</th> -->
									<!-- <th>SUF</th> -->
									<th>Debit Amount</th>
									<th>Status</th>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.lpay_tabs_content w-100 -->
			<div class="lpay_tabs_content w-100 d-none" data-target="debitReport">
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20 debit-report-input">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select name="merchantEmailId" data-download="merchantPayId" data-debit="merchantPayId" class="selectpicker"
							id="merchantDebit" data-submerchant="subMerchantDebit" data-user="subUser" headerKey="ALL" data-live-search="true" headerValue="ALL"
							list="merchantList" listKey="emailId"
							listValue="businessName" autocomplete="off" />
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						<div class="col-md-3 mb-20 debit-report-input">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select name="merchantEmailId" onchange="removeError(this)" data-download="merchantPayId" class="selectpicker"
								id="merchantDebit" headerKey="ALL" data-debit="merchantPayId" data-submerchant="subMerchantDebit" data-user="subUser"  data-live-search="true" headerValue="ALL"
								list="merchantList" listKey="payId"
								listValue="businessName" autocomplete="off" />
							</div>
						</div>
					</s:if>
					<s:else>
						<div class="col-md-3 mb-20 d-none debit-report-input">
							<div class="lpay_select_group ">
								<label for="">Select Merchant</label>
								<s:select name="merchantEmailId" data-download="merchantPayId" data-debit="merchantEmailId" data-live-search="true" class="selectpicker" id="merchantDebit"
								list="merchantList" data-submerchant="subMerchantDebit" data-user="subUser" listKey="payId"
								listValue="businessName" autocomplete="off" />
							</div>
						</div>
					</s:else>
				</s:else>
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20 debit-report-input" data-target="subMerchantDebit">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<s:select data-id="subMerchant" data-download="subMerchantPayId" data-debit="subMerchantPayId" data-submerchant="subMerchantDebit" data-user="subUser"  name="subMerchantEmailId" class="selectpicker" id="subMerchantDebit" list="subMerchantList" listKey="payId" headerKey="ALL" headerValue="ALL"
								listValue="businessName" autocomplete="off" />
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none debit-report-input" data-target="subMerchantDebit"> 
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<select name="subMerchantEmailId" data-download="subMerchantPayId" headerValue="ALL" data-debit="subMerchantPayId" data-submerchant="subMerchantDebit" data-user="subUser" id="subMerchantDebit" class=""></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>
				<s:if test="%{#session.SUBUSERFLAG == true}">
					<div class="col-md-3 mb-20 debit-report-input" data-target="subUser">
						<div class="lpay_select_group">
							<label for="">Sub User</label>
							<s:select data-id="subUser" data-download="subUserPayId" headerValue="ALL" data-debit="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 debit-report-input d-none" data-target="subUser"> 
						<div class="lpay_select_group">
							<label for="">Sub User</label>
							<select name="subUserPayId" data-download="subUserPayId" data-debit="subUserPayId" id="subUser" class=""></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
				</s:else>
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">Date From</label>
						<s:textfield type="text" data-required="true" data-download="dateFrom" data-debit="dateFrom" id="dateFromDebit" name="dateFrom"
						class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">Date To</label>
						<s:textfield type="text" data-download="dateTo" data-debit="dateTo" id="dateToDebit" name="dateTo"
						class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->	
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">UMRN Number</label> 
						<s:textfield id="umrn" data-debit="umrnNumber" onkeypress="lettersAndAlphabet(event)" class="lpay_input" name="umrn"
						type="text" value="" autocomplete="off" ></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">Order ID</label>
						<s:textfield id="orderId" data-reg="^[0-9]{6}$" data-debit="orderId" class="lpay_input" name="orderId"
						type="text" value="" oninput="removeError(this)" autocomplete="off" ></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-12 text-center mb-10">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="view">View</button>
					<button class="lpay_button lpay_button-md lpay_button-primary" id="downlaod-button" onclick="createDownloadForm(this)">Download</button>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="datatable" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Merchant Name</th>
									<th>Sub Merchant Name</th>
									<th>Order ID</th>
									<th>Pg Ref Number</th>
									<th>UMRN Number</th>
									<th>Registration Date</th>
									<th>Debit Amount</th>
									<th>Download</th>
								</tr>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->				
			</div>
			<!-- /.row -->
			<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
				<div class="lpay_tabs_content w-100 d-none" data-target="debitDuration">
					<div class="debit_duration-input">
						<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
							<div class="col-md-3 mb-20 debit-report-input">
								<div class="lpay_select_group">
									<label for="">Select Merchant</label>
									<s:select name="merchantEmailId" data-download="merchantPayId" data-duration="merchantPayId" class="selectpicker"
									id="merchantDebitDuration" data-submerchant="subMerchantDebitDuration" data-user="subUser" headerKey="ALL" data-live-search="true" headerValue="ALL"
									list="merchantList" listKey="emailId"
									listValue="businessName" autocomplete="off" />
								</div>
							</div>
						</s:if>
						<s:else>
							<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
								<div class="col-md-3 mb-20 debit-report-input">
									<div class="lpay_select_group">
										<label for="">Select Merchant</label>
										<s:select name="merchantEmailId" onchange="removeError(this)" data-download="merchantPayId" class="selectpicker"
										id="merchantDebitDuration" headerKey="ALL" data-duration="merchantPayId" data-submerchant="subMerchantDebitDuration" data-user="subUser"  data-live-search="true" headerValue="ALL"
										list="merchantList" listKey="payId"
										listValue="businessName" autocomplete="off" />
									</div>
								</div>
							</s:if>
							<s:else>
								<div class="col-md-3 mb-20 d-none debit-report-input">
									<div class="lpay_select_group ">
										<label for="">Select Merchant</label>
										<s:select name="merchantEmailId" data-download="merchantPayId" data-duration="merchantEmailId" data-live-search="true" class="selectpicker" id="merchantDebitDuration"
										list="merchantList" data-submerchant="subMerchantDebitDuration" data-user="subUser" listKey="emailId"
										listValue="businessName" autocomplete="off" />
									</div>
								</div>
							</s:else>
						</s:else>
						<s:if test="%{#session['USER'].superMerchant == true}">
							<div class="col-md-3 mb-20 debit-report-input" data-target="subMerchantDebitDuration">
								<div class="lpay_select_group">
									<label for="">Sub Merchant</label>
									<s:select data-id="subMerchant" data-download="subMerchantPayId" data-duration="subMerchantPayId" data-submerchant="subMerchantDebitDuration" data-user="subUser"  name="subMerchantEmailId" class="selectpicker" id="subMerchantDebitDuration" list="subMerchantList" listKey="payId" headerKey="ALL" headerValue="ALL"
										listValue="businessName" autocomplete="off" />
								</div>
								<!-- /.lpay_select_group -->  
							</div>
							<!-- /.col-md-3 -->	
						</s:if>
						<s:else>
							<div class="col-md-3 mb-20 d-none debit-report-input" data-target="subMerchantDebitDuration"> 
								<div class="lpay_select_group">
									<label for="">Sub Merchant</label>
									<select name="subMerchantEmailId" data-download="subMerchantPayId" headerValue="ALL" data-duration="subMerchantPayId" data-submerchant="subMerchantDebitDuration" data-user="subUser" id="subMerchantDebitDuration" class=""></select>
								</div>
								<!-- /.lpay_select_group -->  
							</div>
							<!-- /.col-md-3 -->							
						</s:else>
						<s:if test="%{#session.SUBUSERFLAG == true}">
							<div class="col-md-3 mb-20 debit-report-input" data-target="subUserDuration">
								<div class="lpay_select_group">
									<label for="">Sub User</label>
									<s:select data-id="subUser" data-download="subUserPayId" headerValue="ALL" data-duration="subUserPayId" name="subUserPayId" class="selectpicker" id="subUserDuration" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
								</div>
								<!-- /.lpay_select_group -->  
							</div>
							<!-- /.col-md-3 -->	
						</s:if>
						<s:else>
							<div class="col-md-3 mb-20 debit-report-input d-none" data-target="subUserDuration"> 
								<div class="lpay_select_group">
									<label for="">Sub User</label>
									<select name="subUserPayId" data-download="subUserPayId" data-duration="subUserPayId" id="subUserDuration" class=""></select>
								</div>
								<!-- /.lpay_select_group -->  
							</div>
						</s:else>
						<div class="col-md-3 mb-20">
							<div class="lpay_select_group">
							   <label for="">Debit Frequency(Days)</label>
							   <select class="selectpicker" data-var="duration" data-duration="debitDuration" data-id="duration" name="duration" id="duration">
									<option value="ALL">ALL</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
									<option value="4">4</option>
									<option value="5">5</option>
									<option value="6">6</option>
									<option value="7">7</option>
									<option value="8">8</option>
									<option value="9">9</option>
									<option value="10">10</option>
									<option value="11">11</option>
									<option value="12">12</option>
									<option value="13">13</option>
									<option value="14">14</option>
									<option value="15">15</option>
									<option value="16">16</option>
									<option value="17">17</option>
									<option value="18">18</option>
									<option value="19">19</option>
									<option value="20">20</option>
									<option value="21">21</option>
									<option value="22">22</option>
									<option value="23">23</option>
									<option value="24">24</option>
									<option value="25">25</option>
									<option value="26">26</option>
									<option value="27">27</option>
									<option value="28">28</option>
									<option value="29">29</option>
									<option value="30">30</option>
							   </select>
							</div>
							<!-- /.lpay_select_group -->  
						</div>
						<!-- /.col-md-3 -->
						<div class="button-wrapper text-center" style="width: 100%">
							<button class="lpay_button lpay_button-md lpay_button-secondary debit-action-btn" onclick="saveDebitDuration(this)" id="saveDebitDuration">Save</button>
							<button class="lpay_button lpay_button-md lpay_button-primary debit-action-btn" onclick="viewDebitDuration()" id="viewDebitDuration">View</button>
							<button class="lpay_button lpay_button-md lpay_button-secondary debit-action-btn" onclick="downloadDebitDuration(this)" id="downloadDebitDuration">Download</button>
						</div>
						<!-- /.button-wrapper -->
					</div>
					<!-- /.debit_duration-input -->
					<div class="col-md-12 lpay_table_style-2" style="margin-top: 15px;">
						<div class="lpay_table">
							<table id="debitDurationTabel" class="display" cellspacing="0" width="100%">
								<thead>
									<tr class="lpay_table_head">
										<th>Merchant PayID</th>
										<th>Merchant Name</th>
										<th>Sub-Merchant PayID</th>
										<th>Sub-Merchant Name</th>
										<th>Debit Frequency(Days)</th>
										<th>Action</th>
									</tr>
								</thead>
							</table>
						</div>
						<!-- /.lpay_table -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.lpay_tabs_content w-100 d-none -->
			</s:if>

			<div class="lpay_tabs_content w-100 d-none" data-target="quickLinks">
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20 single-account-input">
						<div class="lpay_select_group">
							<label for="">Select Merchant <span class="imp">*</span></label>
							<s:select
								name="merchantEmailId"
								data-download="merchantPayId"
								data-link="merchantPayId"
								class="selectpicker mandate-link-field"
								id="merchantDebit"
								data-submerchant="subMerchantDebit"
								data-user="subUser"
								headerKey="ALL"
								data-live-search="true"
								headerValue="ALL"
								list="merchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
								onchange="enableDisabledButton(this);"
							/>
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						<div class="col-md-3 mb-20 single-account-input">
							<div class="lpay_select_group">
								<label for="">Select Merchant <span class="imp">*</span></label>
								<s:select
									name="merchantEmailId"
									onchange="removeErrorLink(this); enableDisabledButton(this);"
									data-download="merchantPayId"
									class="selectpicker mandate-link-field"
									id="merchantLink"
									headerKey=""
									data-link="merchantPayId"
									data-submerchant="subMerchantLink"
									data-user="subUser"
									data-live-search="true"
									headerValue="Select Merchant"
									list="merchantList"
									listKey="payId"
									listValue="businessName"
									autocomplete="off"
								/>
								<span class="error-field"></span>
							</div>
						</div>
					</s:if>
					<s:else>
						<div class="col-md-3 mb-20 d-none single-account-input">
							<div class="lpay_select_group ">
								<label for="">Select Merchant <span class="imp">*</span></label>
								<s:select
									name="merchantEmailId"
									data-download="merchantPayId"
									data-link="merchantEmailId"
									data-live-search="true"
									class="selectpicker mandate-link-field"
									id="merchantLink"
									list="merchantList"
									data-submerchant="subMerchantList"
									data-user="subUser"
									listKey="emailId"
									listValue="businessName"
									autocomplete="off"
									onchange="enableDisabledButton(this); removeErrorLink(this);"
								/>
							</div>
						</div>
					</s:else>
				</s:else>
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20 single-account-input" data-target="subMerchantLink">
						<div class="lpay_select_group">
							<label for="">Sub Merchant <span class="imp">*</span></label>
							<s:select
								data-id="subMerchant"
								data-download="subMerchantPayId"
								data-link="subMerchantEmailId"
								data-submerchant="subMerchantLink"
								data-user="subUser"
								name="subMerchantEmailId"
								class="selectpicker mandate-link-field"
								id="subMerchantLink"
								list="subMerchantList"
								listKey="payId"
								headerKey="ALL"
								headerValue="ALL"
								listValue="businessName"
								autocomplete="off"
								onchange="enableDisabledButton(this); removeErrorLink(this);"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none single-account-input" data-hide="subMerchant" data-target="subMerchantLink"> 
						<div class="lpay_select_group">
							<label for="">Sub Merchant <span class="imp">*</span></label>
							<select
								name="subMerchantEmailId"
								data-download="subMerchantPayId"
								headerValue="ALL"
								data-link="subMerchantEmailId"
								data-submerchant="subMerchantLink"
								data-user="subUser"
								id="subMerchantLink"
								class="mandate-link-field"
								onchange="enableDisabledButton(this); removeErrorLink(this);">
							</select>
							<span class="error-field"></span>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>
				<s:if test="%{#session.SUBUSERFLAG == true}">
					<div class="col-md-3 mb-20 single-account-input" data-target="subUser">
						<div class="lpay_select_group">
							<label for="">Sub User <span class="imp">*</span></label>
							<s:select
								data-id="subUser"
								data-download="subUserPayId"
								headerValue="ALL"
								data-link="subUserPayId"
								name="subUserPayId"
								class="selectpicker mandate-link-field"
								id="subuserLink"
								list="subUserList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
								onchange="enableDisabledButton(this); removeErrorLink(this);"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 single-account-input d-none" data-target="subUser"> 
						<div class="lpay_select_group">
							<label for="">Sub User <span class="imp">*</span></label>
							<select
								name="subUserPayId"
								data-download="subUserPayId"
								data-link="subUserPayId"
								id="subUserLink"
								class="mandate-link-field"
								onchange="enableDisabledButton(this); removeErrorLink(this);">
							</select>
							<span class="error-field"></span>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
				</s:else>
				<!-- merchant dropdown remove here -->
				<div class="col-md-3 mb-20 single-account-input">
					<div class="lpay_input_group">
						<label for="">Email ID <span class="imp">*</span></label>
						<input
							type="text"
							oninput="removeErrorLink(this); checkRegEx(this); enableDisabledButton(this);"
							onblur="enableDisabledButton(this);"
							data-regex="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
							class="lpay_input mandate-link-field"
							name="custEmailId"
							data-link="custEmailId"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20 single-account-input">
					<div class="lpay_input_group">
						<label for="">Mobile Number <span class="imp">*</span></label>
						<input
							type="text"
							oninput="removeErrorLink(this); checkRegEx(this); enableDisabledButton(this);"
							onblur="enableDisabledButton(this);"
							data-regex="[0-9]{10}"
							class="lpay_input mandate-link-field"
							maxlength="10"
							onkeypress="mzOnlyNumbers(event)"
							name="custMobile"
							data-link="custMobile"
							id="custMobile"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20 single-account-input">
					<div class="lpay_input_group">
						<label for="">Debit Amount <span class="imp">*</span></label>
						<input
							type="text"
							class="lpay_input mandate-link-field"
							oninput="removeErrorLink(this); enableDisabledButton(this); onlyNumericKey(this, event, 2);"
							onblur="enableDisabledButton(this);"
							name="monthlyAmount"
							data-link="monthlyAmount"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20 single-account-input">
					<div class="lpay_input_group">
						<label for="">Tenure <span class="imp">*</span></label>
						<input
							type="text"
							oninput="amount(event, this); removeErrorLink(this); enableDisabledButton(this);"
							onblur="enableDisabledButton(this);"
							onkeypress="mzDigitDot(event)"
							class="lpay_input mandate-link-field"
							name="tenure"
							data-link="tenure"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>

				<div class="col-md-3 single-account-input">
					<div class="lpay_select_group">
					   <label for="">Frequency <span class="imp">*</span></label>
					   <select name="frequency" class="selectpicker mandate-link-field" onchange="removeErrorLink(this); enableDisabledButton(this);" id="frequency" data-link="frequency">
						   <option value="">Select Frequency</option>
						   <option value="DAIL">Daily</option>
						   <option value="WEEK">Weekly</option>
						   <option value="BIMN">Bi- Monthly</option>
						   <option value="MNTH">Monthly</option>
						   <option value="QURT">Quarterly</option>
						   <option value="MIAN">Semi Annually</option>
						   <option value="YEAR">Yearly</option>
						   <option value="ADHO">As and when presented (ADHO)</option>
					   </select>
					   <span class="error-field"></span>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-12 text-center">
					<button onclick="sendLink(this)" disabled data-info="sms" data-connect="custMobile" class="lpay_button btn-mandate-link lpay_button-md lpay_button-primary">Send SMS</button>
					<button onclick="sendLink(this)" disabled data-info="email" data-connect="custEmailId" class="lpay_button btn-mandate-link lpay_button-md lpay_button-secondary">Send Email</button>
					<button onclick="sendLink(this)" disabled data-info="both" class="lpay_button btn-mandate-link lpay_button-md lpay_button-primary">Send Both</button>
				</div>
				<!-- /.col-md-12 -->
			</div>
		</div>
		<!-- /.lpay_tabs_content w-100 d-none -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<div class="debit_popup_container d-none">
        <div class="debit_popup_inner">
            <div class="debit_popup_box">
                <div class="default-heading">
                    <h3>eNACH Transaction Data</h3>
                </div>
                <!-- /.default-heading -->
                <div class="col-md-12">
                    <div class="lpay_table" id="lpay_table_popup">
                        <table id="datatablePopup" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th data-id="merchantName">Merchant Name</th>
                                    <th>Sub Merchant Name</th>
                                    <th data-id="orderId">Order ID</th>
                                    <th data-id="pgRefNum">Pg Ref Num</th>
                                    <th data-id="pgRefNum">Pg Ref Num (Registration)</th>
                                    <th data-id="pgRefNum">UMRN Number</th>
                                    <th data-id="registrationDate">Registration Date</th>
                                    <th data-id="dueDate">Due Date</th>
                                    <th data-id="registrationDate">Customer Name</th>
                                    <th data-id="registrationDate">Email</th>
                                    <th data-id="registrationDate">Mobile</th>
                                    <th data-id="registrationDate">Status</th>
                                    <th data-id="totalAmount">Debit Amount</th>
                                    <th data-id="action">Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.debit_popup_box -->
        </div>
        <!-- /.debit_popup_inner -->
        <div class="button-wrapper button-wrapper-debit d-none">
            <button class="lpay_button lpay_button-md lpay_button-secondary close-btn">Close</button>
            <!-- /.lpay_button lpay_button-md lpay_button-primary -->
        </div>
        <!-- /.button-wrapper -->
    </div>
    <!-- /.debit_popup_container -->

	<div class="response-popup d-none">
        <div class="response-popup--inner">
            <div class="response-icon">
                <img src="" alt="">
            </div>
            <div class="response-msg"></div>
            <div class="response-btn">
                <a href="#" id="btn-close-response">OK</a>
            </div>
        </div>
    </div>
    <!-- /.response-popup -->

    <form action="NA" method="post" id="downloadFormDebit"> 

    </form>

	<form action="downloadDebitDuration" method="post" id="downloadFormDebitDuration"> 

    </form>

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
	<form action="#" id="downloadForm">

	</form>
	<script src="../js/eNachReports.js"></script>
	<script src="../js/horizontal-scrolling-nav.js"></script>
	<script src="../js/decimalLimit.js"></script>
</body>
</html>