<!DOCTYPE html>
<%@ taglib uri="/struts-tags" prefix="s"%>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="X-UA-Compatible" content="ie=edge">
	<title>Fraud Prevention System</title>

	<link rel="stylesheet" href="../css/jquery.fancybox.min.css" />
	<link rel="stylesheet" href="../css/bootstrap-tagsinput.css" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/bootstrap-datetimepicker.css">
	<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
	<!-- <link rel="stylesheet" href="../css/bootstrap-datetimepicker-standalone.min.css"> -->
	<link rel="stylesheet" href="../css/jquery-ui.css" />
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/horizontal-scrolling-nav.css">
	<link rel="stylesheet" href="../css/fraud-prevention.css">
	
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.fancybox.min.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/bootstrap-tagsinput.min.js"></script>
	
	<script src="../js/moment-with-locales.js"></script>
	<script src="../js/bootstrap-datetimepicker.js"></script>
	
	<script>
		var parentPage = 'Merchant Configuration';
		var currentPage = "Fraud Prevention";
	</script>
</head>
<body>
	<s:hidden name="token" value="%{#session.customToken}"></s:hidden>

	<div class="snackbar bg-snackbar-danger text-snackbar-danger font-size-14" id="error-snackbar"></div>
	<div class="snackbar bg-snackbar-success text-snackbar-success font-size-14" id="success-snackbar"></div>

	<section class="admin-restriction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Fraud Prevention System</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:select 
				   name="payId"
				   class="selectpicker"
				   id="payId"
				   headerKey=""
				   headerValue="Select Merchant"
				   list="merchantList"
				   listKey="payId"
				   listValue="businessName"
				   autocomplete="off"
				   data-tokens="businessName"
				   data-live-search="true"/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12">
				<div class="horizontal-nav-wrapper mb-20">
					<nav id="horizontal-nav" class="horizontal-nav">
						<ul class="horizontal-nav-content nav nav-tabs list-unstyled font-size-14 merchant-config-tabs lpay_tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
							
							<li class="nav-item lpay-nav-item active">
								<a class="nav-link lpay-nav-link"
									id="ip-addresses-tab"
									data-toggle="tab"
									href="#ip-addresses"
									role="tab"
									aria-controls="ip-addresses"
									aria-selected="true">IP Addresses</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="white-list-ip-tab"
									data-toggle="tab"
									href="#white-list-ip"
									role="tab"
									aria-controls="white-list-ip"
									aria-selected="false">White List IP</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="issuer-countries-tab"
									data-toggle="tab"
									href="#issuer-countries"
									role="tab"
									aria-controls="issuer-countries"
									aria-selected="false">Issuer Countries</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="user-countries-tab"
									data-toggle="tab"
									href="#user-countries"
									role="tab"
									aria-controls="user-countries"
									aria-selected="false">User Countries</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="email-addresses-tab"
									data-toggle="tab"
									href="#email-addresses"
									role="tab"
									aria-controls="email-addresses"
									aria-selected="false">Email Addresses</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="domains-tab"
									data-toggle="tab"
									href="#domains"
									role="tab"
									aria-controls="domains"
									aria-selected="false">Domains</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="transactional-amount-tab"
									data-toggle="tab"
									href="#transactional-amount"
									role="tab"
									aria-controls="transactional-amount"
									aria-selected="false">Transactional Amount</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="card-ranges-tab"
									data-toggle="tab"
									href="#card-ranges"
									role="tab"
									aria-controls="card-ranges"
									aria-selected="false">BIN Range</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="card-nos-tab"
									data-toggle="tab"
									href="#card-nos"
									role="tab"
									aria-controls="card-nos"
									aria-selected="false">Card Nos.</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="card-transaction-tab"
									data-toggle="tab"
									href="#card-transaction"
									role="tab"
									aria-controls="card-transaction"
									aria-selected="false">Card Transaction</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="transaction-velocity-tab"
									data-toggle="tab"
									href="#transaction-velocity"
									role="tab"
									aria-controls="transaction-velocity"
									aria-selected="false">Transaction Velocity</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="amount-velocity-tab"
									data-toggle="tab"
									href="#amount-velocity"
									role="tab"
									aria-controls="amount-velocity"
									aria-selected="false">Amount Velocity</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="sale-amount-velocity-tab"
									data-toggle="tab"
									href="#sale-amount-velocity"
									role="tab"
									aria-controls="sale-amount-velocity"
									aria-selected="false">Sale Amount Velocity (Total Sale)</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="vpa-tab"
									data-toggle="tab"
									href="#vpa-table"
									role="tab"
									aria-controls="vpa-table"
									aria-selected="true">VPA</a>
							</li>
							<li class="nav-item lpay-nav-item">
								<a class="nav-link lpay-nav-link"
									id="vpa-transaction-tab"
									data-toggle="tab"
									href="#vpa-transaction"
									role="tab"
									aria-controls="vpa-transaction"
									aria-selected="true">VPA Transaction</a>
							</li>
						</ul>
					</nav>
	
					<button type="button" id="btn-scroll-left" class="horizontal-nav-btn horizontal-nav-btn-left m-0 bg-color-white pr-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024"><path d="M445.44 38.183L-2.53 512l447.97 473.817 85.857-81.173-409.6-433.23v81.172l409.6-433.23L445.44 38.18z"/></svg>
					</button>
					<button type="button" id="btn-scroll-right" class="horizontal-nav-btn horizontal-nav-btn-right m-0 bg-color-white pl-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024"><path d="M105.56 985.817L553.53 512 105.56 38.183l-85.857 81.173 409.6 433.23v-81.172l-409.6 433.23 85.856 81.174z"/></svg>
					</button>
				</div>
	
				<div class="tab-content" id="merchantConfigContent">					
					<div class="tab-pane border-none active" id="ip-addresses" role="tabpanel" aria-labelledby="ip-addresses-tab">
						<div class="lpay_table_wrapper">
							<table class="table lpay_custom_table merchant-config-table" id="ipAddListBody">
								<thead class="lpay_table_head">
									<tr>
										<th width="180">Merchant</th>
										<th width="130">IP Address</th>
										<th width="130">Start Date</th>
										<th width="130">End Date</th>
										<th width="100">Start Time</th>
										<th width="100">End Time</th>
										<th width="180">Days</th>
										<th width="5%">Actions</th>
									</tr>
								</thead>
								<tbody></tbody>
							</table>
							<!-- /.table -->
						</div>
						<!-- /.lpay_table_wrapper -->
		
						<a data-fancybox=""
							data-src="#BLOCK_IP_ADDRESS-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_IP_ADDRESS-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
							<input type="text" data-input="status" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag1" id="alwaysOnFlag1" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag1"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag11" id="alwaysOnFlag11" data-checkbox="alwaysOnFlag1" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="ipAddress" class="font-size-12 color-grey-dark font-weight-medium">IP Address *</label>
										<input
											type="text"
											name="ipAddress"
											id="ipAddress"
											data-input="ipAddress"
											class="form-control"
											placeholder="192.168.100.1"
											data-role="tagsinput">
										<span class="font-size-10 error-msg" id="validate_err"></span>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom" class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, ipAddressS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="white-list-ip" role="tabpanel" aria-labelledby="white-list-ip-tab">
						<div class="lpay_table_wrapper">
							<table class="table lpay_custom_table merchant-config-table" id="wlIpAddListBody">
								<thead class="lpay_table_head">
									<tr>
										<th width="180">Merchant</th>
										<th width="130">IP Address</th>
										<th width="130">Start Date</th>
										<th width="130">End Date</th>
										<th width="100">Start Time</th>
										<th width="100">End Time</th>
										<th width="180">Days</th>									
										<th width="5%">Actions</th>
									</tr>
								</thead>
								<tbody></tbody>
							</table>
							<!-- /.table -->
						</div>
						<!-- /.lpay_table_wrapper -->
						<a data-fancybox=""
							data-src="#WHITE_LIST_IP_ADDRESS-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="WHITE_LIST_IP_ADDRESS-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag2" id="alwaysOnFlag2" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag2"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag22" id="alwaysOnFlag22" data-checkbox="alwaysOnFlag2" />
								</div>
								<!-- /.col-xs-12 -->
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="ipAddress" class="font-size-12 color-grey-dark font-weight-medium">IP Address *</label>
										<input
											type="text"
											name="whiteListIpAddress"
											id="whiteListIpAddress"
											data-input="whiteListIpAddress"
											class="form-control"
											placeholder="192.168.100.1"
											data-role="tagsinput">
										<span class="font-size-10 error-msg" id="validate_err1"></span>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom1"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo1"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime1"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime1"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays1" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, whiteListIpAddressS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="issuer-countries" role="tabpanel" aria-labelledby="issuer-countries-tab">
						<table class="table lpay_custom_table merchant-config-table" id="issuerCountryListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="40%">Merchant</th>
									<th class="border-none" width="50%">Issuer Country</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_CARD_ISSUER_COUNTRY-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_CARD_ISSUER_COUNTRY-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12">
									<div class="lpay_select_group mb-20">
										<label for="issuerCountry" class="font-size-12 color-grey-dark font-weight-medium">Issuer Country</label>
										<s:select 
											name="issuerCountry" 
											id="issuerCountry" 
											data-selected-text-format="count>2" 
											data-actions-box="true" 
											data-live-search="true" 
											data-input="issuerCountry" 
											class="selectpicker" 
											listKey="code" 
											listValue="name" 
											data-style="form-control" 
											list="@com.paymentgateway.commons.util.CountryCodes@values()"  
											title="Please select Issuer Country" 
											multiple='true' 
										/>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, issuerCountry)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="user-countries" role="tabpanel" aria-labelledby="user-countries-tab">
						<table class="table lpay_custom_table merchant-config-table" id="userCountryListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="40%">Merchant</th>
									<th class="border-none" width="50%">User Country</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_USER_COUNTRY-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_USER_COUNTRY-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12">
									<div class="lpay_select_group mb-20">
										<label for="userCountry"  class="font-size-12 color-grey-dark font-weight-medium">User Country</label>

										<s:select 
											name="userCountry"
											id="userCountry" 
											data-selected-text-format="count>2" 
											data-actions-box="true" 
											data-live-search="true" 
											data-input="userCountry" 
											class="selectpicker" 
											listKey="code" 
											listValue="name" 
											data-style="form-control" 
											list="@com.paymentgateway.commons.util.CountryCodes@values()"  
											title="Please select User Country" 
											multiple='true' 
										/>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, userCountry)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="email-addresses" role="tabpanel" aria-labelledby="email-addresses-tab">
						<table class="table lpay_custom_table merchant-config-table" id="emailListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="130">Email Address</th>
									<th class="border-none" width="130">Start Date</th>
									<th class="border-none" width="130">End Date</th>
									<th class="border-none" width="100">Start Time</th>
									<th class="border-none" width="100">End Time</th>
									<th class="border-none" width="180">Days</th>									
									<th class="border-none">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_EMAIL_ID-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_EMAIL_ID-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag4" id="alwaysOnFlag4" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag4"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag44" id="alwaysOnFlag44" data-checkbox="alwaysOnFlag4" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="ipAddress" class="font-size-12 color-grey-dark font-weight-medium">Email Address *</label>
										<input
											type="text"
											name="email"
											id="email"
											data-input="email"
											class="form-control"
											placeholder="user@domain.xyz"
											data-role="tagsinput">
										<span class="font-size-10 error-msg" id="validate_email"></span>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom3"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo3"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo3"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime3" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime3"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime3" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime3"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays3" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays3" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, emailS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="domains" role="tabpanel" aria-labelledby="domains-tab">
						<table class="table lpay_custom_table merchant-config-table" id="domainListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="130">Domain Name</th>
									<th class="border-none" width="130">Start Date</th>
									<th class="border-none" width="130">End Date</th>
									<th class="border-none" width="100">Start Time</th>
									<th class="border-none" width="100">End Time</th>
									<th class="border-none" width="180">Days</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_DOMAIN_NAME-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_DOMAIN_NAME-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag5" id="alwaysOnFlag5" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag5"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag55" id="alwaysOnFlag55" data-checkbox="alwaysOnFlag5" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="domainName" class="font-size-12 color-grey-dark font-weight-medium">Domain Name *</label>
										<input
											type="text"
											name="domainName"
											id="domainName"
											data-input="domainName"
											class="form-control"
											placeholder="www.domain.xyz"
											data-role="tagsinput">
										<span class="font-size-10 error-msg" id="validate_dmn"></span>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom2"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom2"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo2"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo2"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime2" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime2"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime2" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime2"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays2" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays2" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, domainNameS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="transactional-amount" role="tabpanel" aria-labelledby="transactional-amount-tab">
						<table class="table lpay_custom_table merchant-config-table" id="txnAmountListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="180">Currency</th>
									<th class="border-none" width="290">Payment Type</th>
									<th class="border-none" width="290">Payment Region</th>
									<th class="border-none" width="290">Minimum Amount</th>
									<th class="border-none" width="290">Maximum Amount</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_TXN_AMOUNT-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_TXN_AMOUNT-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12">
									<div class="form-group">
										<label for="dateActiveFrom2"
											class="font-size-12 color-grey-dark font-weight-medium">Currency *</label>
										<div class="position-relative">
											<s:select
												name="currency"
												class="selectpicker"
												data-style="form-control"
												id="currency"
												list="currencyMap"
												data-input="currency"
											/>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->

								<div class="col-xs-6">
									<div class="form-group">
										<label for="txnAmtpaymentType" class="font-size-12 color-grey-dark font-weight-medium">Payment Type *</label>
										<select name="paymentType" id="txnAmtpaymentType" data-input="paymentType" data-id="txnAmtpaymentRegion" class="selectpicker paymentType-input paymentType-selectbox" data-style="form-control">
											<option value="">Please select Payment Type</option>
											<option value="ALL">ALL</option>
											<option value="CC">Credit Card</option>
											<option value="DC">Debit Card</option>
											<option value="NB">Net Banking</option>
											<option value="WL">Wallet</option>
											<option value="UP">UPI</option>
											<option value="EM">EMI</option>
											<option value="CD">COD</option>
											<option value="EMCC">EMI CC</option>	
											<option value="EMDC">EMI DC</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->

								<div class="col-xs-6">
									<div class="form-group">
										<label for="txnAmtpaymentRegion" class="font-size-12 color-grey-dark font-weight-medium">Region Type *</label>
										<select name="paymentRegion" id="txnAmtpaymentRegion" data-input="paymentRegion" class="selectpicker paymentRegion-selectbox" data-style="form-control" title="Please select Region Type" multiple>
											<option value="DOMESTIC">Domestic</option>
											<option value="INTERNATIONAL">International</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo2"
											class="font-size-12 color-grey-dark font-weight-medium">Amount(Min Amount) *</label>
										<div class="position-relative">
											<input
												type="text"
												id="minTransactionAmount"
												placeholder="10"
												name="minTransactionAmount"
												data-input="minTransactionAmount"
												class="form-control"
												minlength="1"
												maxlength="12"
												onkeypress="return isNumberKey(event)" />
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime2" class="font-size-12 color-grey-dark font-weight-medium">Amount(Max Amount) *</label>
										<div class="position-relative">
											<input
												type="text"
												id="maxTransactionAmount"
												placeholder="110"
												name="maxTransactionAmount"
												data-input="maxTransactionAmount"
												class="form-control"
												minlength="1"
												maxlength="12"
												onkeypress="return isNumberKey(event)" />
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, transactionAmount)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="card-ranges" role="tabpanel" aria-labelledby="card-ranges-tab">
						<table class="table lpay_custom_table merchant-config-table" id="cardBinListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="130">Card Range</th>
									<th class="border-none" width="130">Start Date</th>
									<th class="border-none" width="130">End Date</th>
									<th class="border-none" width="100">Start Time</th>
									<th class="border-none" width="100">End Time</th>
									<th class="border-none" width="180">Days</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_CARD_BIN-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_CARD_BIN-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag3" id="alwaysOnFlag3" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag3"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag33" id="alwaysOnFlag33" data-checkbox="alwaysOnFlag3" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="negativeBin" class="font-size-12 color-grey-dark font-weight-medium">Card Range *</label>
										<input
											type="text"
											name="negativeBin"
											id="negativeBin"
											data-input="negativeBin"
											class="form-control"
											placeholder="6-digit bin range"
											minlength="6"
											maxlength="6"
											data-role="tagsinput">
										<span class="font-size-10 error-msg" id="validate_crd"></span>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom4"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom4"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo4"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo4"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime4" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime4"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime4" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime4"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays4" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays4" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, negativeBinS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="card-nos" role="tabpanel" aria-labelledby="card-nos-tab">
						<table class="table lpay_custom_table merchant-config-table" id="cardNoListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="130">Card No.</th>
									<th class="border-none" width="130">Start Date</th>
									<th class="border-none" width="130">End Date</th>
									<th class="border-none" width="100">Start Time</th>
									<th class="border-none" width="100">End Time</th>
									<th class="border-none" width="180">Days</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_CARD_NO-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_CARD_NO-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag6" id="alwaysOnFlag6" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag6"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag66" id="alwaysOnFlag66" data-checkbox="alwaysOnFlag6" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="cardNumber" class="font-size-12 color-grey-dark font-weight-medium">Card Number *</label>
										<div class="position-relative">
											<input
												id="negativeCard"
												data-input="negativeCard"
												placeholder="Enter card number"
												type="text"
												minlength="16"
												maxlength="23"
												class="form-control cardNumber"
												onkeyup="fourDigitSpace(event)" />
											<span class="font-size-10 error-msg" data-error="validate-cardNumber"></span>
										</div>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom5"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom5"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo5"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo5"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime5" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime5"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime5" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime5"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays5" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays5" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, negativeCardS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="card-transaction" role="tabpanel" aria-labelledby="card-transaction-tab">
						<table class="table lpay_custom_table merchant-config-table" id="perCardTxnsListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="130">Card No.</th>
									<th class="border-none" width="110">Allowed Txns</th>
									<th class="border-none" width="110">Start Date</th>
									<th class="border-none" width="110">End Date</th>
									<th class="border-none" width="100">Start Time</th>
									<th class="border-none" width="100">End Time</th>
									<th class="border-none" width="180">Days</th>									
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_CARD_TXN_THRESHOLD-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_CARD_TXN_THRESHOLD-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag7" id="alwaysOnFlag7" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag7"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag77" id="alwaysOnFlag77" data-checkbox="alwaysOnFlag7" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="preCardNumber" class="font-size-12 color-grey-dark font-weight-medium">Card Number *</label>
	
										<div class="row" data-input="negativeCard">
											<div class="col-xs-12">
												<div class="position-relative">
													<input
														id="prenegativeCard"
														name="prenegativeCard"
														data-input="prenegativeCard"
														placeholder="Enter card number"
														type="text"
														minlength="16"
														maxlength="23"
														class="form-control cardNumber"
														onkeyup="fourDigitSpace(event)" />
	
													<span class="font-size-10 error-msg" data-error="validate-prenegativeCard"></span>
												</div>
											</div>
											<!-- /.col-md-6 -->
										</div>
										<!-- /.row -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="perCardTransactionAllowed"
											class="font-size-12 color-grey-dark font-weight-medium">No. of Transactions *</label>
										<input
											id="perCardTransactionAllowed"
											data-input="perCardTransactionAllowed"
											name="perCardTransactionAllowed"
											type="text"
											placeholder="e.g 10"
											class="form-control"
											maxlength="8"
											onkeypress="return isNumberKey(event)" />
									</div>
								</div>
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom7"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom7"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo7"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo7"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime7" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime7"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime7" class="font-size-12 color-grey-dark font-weight-medium">End Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime7"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays7" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays7" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, perCardTransactionAllowedS)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="transaction-velocity" role="tabpanel" aria-labelledby="transaction-velocity-tab">
						<table class="table lpay_custom_table merchant-config-table" id="txnVelocityListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="170">Payment Type</th>
									<th class="border-none" width="220">Region Type</th>
									<th class="border-none" width="170">Time Period</th>
									<th class="border-none" width="190">Allowed Transactions</th>
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_TXN_VELOCITY-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_TXN_VELOCITY-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-6">
									<div class="form-group">
										<label for="txnpaymentType" class="font-size-12 color-grey-dark font-weight-medium">Payment Type *</label>
										<select name="paymentType" id="txnpaymentType" data-input="paymentType" data-id="txnpaymentRegion" class="selectpicker paymentType-input" data-style="form-control">
											<option value="">Please select Payment Type</option>
											<option value="CC">Credit Card</option>
											<option value="DC">Debit Card</option>
											<option value="UP">UPI</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="txnpaymentRegion" class="font-size-12 color-grey-dark font-weight-medium">Region Type *</label>
										<select name="paymentRegion" id="txnpaymentRegion" data-input="paymentRegion" class="selectpicker paymentRegion-selectbox" data-style="form-control" title="Please select Region Type" multiple>
											<option value="DOMESTIC">Domestic</option>
											<option value="INTERNATIONAL">International</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="txntimePeriod" class="font-size-12 color-grey-dark font-weight-medium">Time Period *</label>
										<select name="timePeriod" id="txntimePeriod" data-input="timePeriod" class="selectpicker" data-style="form-control">
											<option value="">Please select Time Period</option>
											<option value="DAILY">Daily</option>
											<option value="WEEKLY">Weekly</option>
											<option value="MONTHLY">Monthly</option>
											<option value="YEARLY">Yearly</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
								<div class="col-xs-6">
									<div class="form-group">
										<label for="noOfTransactionAllowed" class="font-size-12 color-grey-dark font-weight-medium">No. of Transactions *</label>
										<input
											id="noOfTransactionAllowed"
											data-input="noOfTransactionAllowed"
											name="noOfTransactionAllowed"
											type="text"
											placeholder="e.g 10"
											class="form-control"
											maxlength="8"
											onkeypress="return isNumberKey(event)" />
									</div>
								</div>
							</div>
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, transactionVelocity)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>
	
					<div class="tab-pane border-none" id="amount-velocity" role="tabpanel" aria-labelledby="amount-velocity-tab">
						<table class="table lpay_custom_table merchant-config-table" id="amtVelocityListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="170">Payment Type</th>
									<th class="border-none" width="220">Region Type</th>
									<th class="border-none" width="170">Time Period</th>
									<th class="border-none" width="190">Allowed Amount</th>
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_AMOUNT_VELOCITY-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_AMOUNT_VELOCITY-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-6">
									<div class="form-group">
										<label for="amtpaymentType" class="font-size-12 color-grey-dark font-weight-medium">Payment Type *</label>
										<select name="paymentType" id="amtpaymentType" data-input="paymentType" data-id="amtpaymentRegion" class="selectpicker paymentType-input paymentType-selectbox" data-style="form-control">
											<option value="">Please select Payment Type</option>
											<option value="CC">Credit Card</option>
											<option value="DC">Debit Card</option>
											<option value="UP">UPI</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="amtpaymentRegion" class="font-size-12 color-grey-dark font-weight-medium">Region Type *</label>
										<select name="paymentRegion" id="amtpaymentRegion" data-input="paymentRegion" class="selectpicker paymentRegion-selectbox" data-style="form-control" title="Please select Region Type" multiple>
											<option value="DOMESTIC">Domestic</option>
											<option value="INTERNATIONAL">International</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="amttimePeriod" class="font-size-12 color-grey-dark font-weight-medium">Time Period *</label>
										<select name="timePeriod" id="amttimePeriod" data-input="timePeriod" class="selectpicker" data-style="form-control">
											<option value="">Please select Time Period</option>
											<option value="DAILY">Daily</option>
											<option value="WEEKLY">Weekly</option>
											<option value="MONTHLY">Monthly</option>
											<option value="YEARLY">Yearly</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="amtVelocityAllowedCount" class="font-size-12 color-grey-dark font-weight-medium">Allowed Total Amount *</label>
										<input
											id="amtVelocityAllowedCount"
											data-input="velocityAllowedAmt"
											name="velocityAllowedAmt"
											type="text"
											placeholder="e.g 10"
											class="form-control"
											maxlength="8"
											onkeypress="return isNumberKey(event)" />
									</div>
								</div>
							</div>
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										data-id="amountVilocity"
										onclick="ajaxFraudRequest(this, amountVelocity)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>

					<div class="tab-pane border-none" id="sale-amount-velocity" role="tabpanel" aria-labelledby="sale-amount-velocity-tab">
						<table class="table lpay_custom_table merchant-config-table" id="saleAmtVelocityListBody">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-none" width="180">Merchant</th>
									<th class="border-none" width="170">Payment Type</th>
									<th class="border-none" width="220">Region Type</th>
									<th class="border-none" width="170">Time Period</th>
									<th class="border-none" width="190">Allowed Amount</th>
									<th class="border-none" width="5%">Actions</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
						<!-- /.table -->
		
						<a data-fancybox=""
							data-src="#BLOCK_SALE_AMOUNT_VELOCITY-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_SALE_AMOUNT_VELOCITY-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-6">
									<div class="form-group">
										<label for="saleAmtpaymentType" class="font-size-12 color-grey-dark font-weight-medium">Payment Type *</label>
										<select name="paymentType" id="saleAmtpaymentType" data-input="paymentType" data-id="saleAmtpaymentRegion" class="selectpicker paymentType-input paymentType-selectbox" data-style="form-control">
											<option value="">Please select Payment Type</option>
											<option value="ALL">ALL</option>
											<option value="CC">Credit Card</option>
											<option value="DC">Debit Card</option>
											<option value="NB">Net Banking</option>
											<option value="WL">Wallet</option>
											<option value="UP">UPI</option>
											<option value="EM">EMI</option>
											<option value="CD">COD</option>
											<option value="EMCC">EMI CC</option>	
											<option value="EMDC">EMI DC</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="saleAmtpaymentRegion" class="font-size-12 color-grey-dark font-weight-medium">Region Type *</label>
										<select name="paymentRegion" id="saleAmtpaymentRegion" data-input="paymentRegion" class="selectpicker paymentRegion-selectbox" data-style="form-control" title="Please select Region Type" multiple>
											<option value="DOMESTIC">Domestic</option>
											<option value="INTERNATIONAL">International</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="saleAmtTimePeriod" class="font-size-12 color-grey-dark font-weight-medium">Time Period *</label>
										<select name="timePeriod" id="saleAmtTimePeriod" data-input="timePeriod" class="selectpicker" data-style="form-control">
											<option value="">Please select Time Period</option>
											<option value="DAILY">Daily</option>
											<option value="WEEKLY">Weekly</option>
											<option value="MONTHLY">Monthly</option>
											<option value="YEARLY">Yearly</option>
										</select>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-xs-6">
									<div class="form-group">
										<label for="saleAmtVelocityAllowedCount" class="font-size-12 color-grey-dark font-weight-medium">Allowed Total Amount *</label>
										<input
											id="saleAmtVelocityAllowedCount"
											data-input="velocityAllowedAmt"
											name="velocityAllowedAmt"
											type="text"
											placeholder="e.g 10"
											class="form-control"
											maxlength="8"
											onkeypress="return isNumberKey(event)" />
									</div>
								</div>
							</div>
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										data-id="saleAmountVelocity"
										onclick="ajaxFraudRequest(this, saleAmountVelocity)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>

					<div class="tab-pane border-none" id="vpa-table" role="tabpanel" aria-labelledby="vpa-tab">
						<div class="lpay_table_wrapper">
							<table class="table lpay_custom_table merchant-config-table" id="vpaAddListBody">
								<thead class="lpay_table_head">
									<tr>
										<th width="180">Merchant</th>
										<th width="130">VPA</th>
										<th width="130">Start Date</th>
										<th width="130">End Date</th>
										<th width="100">Start Time</th>
										<th width="100">End Time</th>
										<th width="180">Days</th>
										<th width="5%">Actions</th>
									</tr>
								</thead>
								<tbody></tbody>
							</table>
							<!-- /.table -->
						</div>
						<!-- /.lpay_table_wrapper -->
		
						<a data-fancybox=""
							data-src="#BLOCK_VPA-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_VPA-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag9" id="alwaysOnFlag9" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag9"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag99" id="alwaysOnFlag99" data-checkbox="alwaysOnFlag9" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="vpa" class="font-size-12 color-grey-dark font-weight-medium">VPA *</label>
										<div class="position-relative">
											<input
												id="vpa"
												name="vpa"
												data-input="vpaInput"
												placeholder="Enter VPA"
												type="text"
												class="form-control"
												 />
											<span class="font-size-10 error-msg"></span>
										</div>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom9"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom9"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo9"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo9"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime9" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime9"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime9" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime9"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays9" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays9" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input
										type="submit"
										value="Block"
										onclick="ajaxFraudRequest(this, vpa)"
										class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>

					<div class="tab-pane border-none" id="vpa-transaction" role="tabpanel" aria-labelledby="vpa-transaction-tab">
						<div class="lpay_table_wrapper">
							<table class="table lpay_custom_table merchant-config-table" id="vpaTransactionAddListBody">
								<thead class="lpay_table_head">
									<tr>
										<th width="180">Merchant</th>
										<th width="130">VPA</th>
										<th width="130">Allowed Txns</th>
										<th width="130">Start Date</th>
										<th width="130">End Date</th>
										<th width="100">Start Time</th>
										<th width="100">End Time</th>
										<th width="180">Days</th>
										<th width="5%">Actions</th>
									</tr>
								</thead>
								<tbody></tbody>
							</table>
							<!-- /.table -->
						</div>
						<!-- /.lpay_table_wrapper -->
		
						<a data-fancybox=""
							data-src="#BLOCK_VPA_TXN-rule"
							data-modal="true"
							href="javascript:;"
							class="lpay_button lpay_button-md lpay_button-primary add-new-rule">
							<i class="fa fa-plus font-size-12 mr-5"></i>
							<span>Add New Rules</span>
						</a>
	
						<div class="display-none modal-box-new-rule" id="BLOCK_VPA_TXN-rule">
							<button data-fancybox-close="" class="border-none bg-white color-grey-dark hover-color-dark fancybox-close-btn position-absolute top-0 right-0 py-5 px-10 font-size-20 mr-0 mb-0"><i class="fa fa-close"></i></button>
							<h2 class="font-size-20 color-grey-dark font-weight-medium mb-20">Add New Rules</h2>
	
							<input type="text" data-input="payId" hidden>
							<input type="text" data-input="rowId" hidden>
	
							<div class="row">
								<div class="col-xs-12 d-flex align-items-center pb-10 mb-5 border-bottom-grey-light">
									<label class="mr-20 font-size-12 color-grey-dark font-weight-medium mb-minus-2">Always Block</label>
									<div class="custom-control custom-switch">
										<input type="checkbox" name="alwaysOnFlag8" id="alwaysOnFlag8" value="false" class="custom-control-input" />
										<label class="custom-control-label" for="alwaysOnFlag8"></label>
									</div>
									<input type="hidden" name="alwaysOnFlag88" id="alwaysOnFlag88" data-checkbox="alwaysOnFlag8" />
								</div>
								<!-- /.col-xs-12 -->
								<div class="col-xs-12">
									<div class="form-group">
										<label for="vpaTransaction" class="font-size-12 color-grey-dark font-weight-medium">VPA *</label>
										<div class="position-relative">
											<input
												id="vpaTransaction"
												name="vpaTransaction"
												data-input="vpaTransaction"
												placeholder="Enter VPA"
												type="text"
												class="form-control vpaTransaction" />
											<span class="font-size-10 error-msg"></span>
										</div>
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-xs-12 -->

								<div class="col-xs-12">
									<div class="form-group">
										<label for="vpaTotalTransactionAllowed"
											class="font-size-12 color-grey-dark font-weight-medium">No. of Transactions *</label>
										<input
											id="vpaTotalTransactionAllowed"
											data-input="vpaTotalTransactionAllowed"
											name="vpaTotalTransactionAllowed"
											type="text"
											placeholder="e.g 10"
											class="form-control"
											maxlength="8"
											onkeypress="return isNumberKey(event)" />
									</div>
								</div>
							</div>
	
							<div class="row new-rule-detail-box">
								<div class="col-md-6">
									<div class="form-group">
										<label for="dateActiveFrom8"
											class="font-size-12 color-grey-dark font-weight-medium">Start Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveFrom"
												id="dateActiveFrom8"
												data-input="dateActiveFrom"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label
											for="dateActiveTo8"
											class="font-size-12 color-grey-dark font-weight-medium">End Date *</label>
										<div class="position-relative">
											<input
												type="text"
												name="dateActiveTo"
												id="dateActiveTo8"
												data-input="dateActiveTo"
												placeholder="DD/MM/YYYY"
												class="form-control datepicker2"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-calendar position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
									</div>
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="startTime8" class="font-size-12 color-grey-dark font-weight-medium">Start Time *</label>
										<div class="position-relative">
											<input
												type="text"
												name="startTime"
												id="startTime8"
												data-input="startTime"
												class="form-control startTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->
	
								<div class="col-md-6">
									<div class="form-group">
										<label for="endTime8" class="font-size-12 color-grey-dark font-weight-medium">End Time</label>
										<div class="position-relative">
											<input
												type="text"
												name="endTime"
												id="endTime8"
												data-input="endTime"
												class="form-control endTime"
												placeholder="HH:MM:SS"
												autocomplete="off"
												readonly="readonly">
											<i class="fa fa-clock-o position-absolute font-size-12 color-grey-light top-50 right-12"></i>
										</div>
										<!-- /.position-relative -->
									</div>
									<!-- /.form-group -->
								</div>
								<!-- /.col-md-6 -->	
	
								<div class="col-xs-12">
									<div class="form-group">
										<label for="repeatDays8" class="font-size-12 color-grey-dark font-weight-medium">Days</label>
										<select name="repeatDays" id="repeatDays8" data-input="repeatDays" class="selectpicker" data-style="form-control" title="Please Select Days" multiple>
											<option value="SUN">Sunday</option>
											<option value="MON">Monday</option>
											<option value="TUE">Tuesday</option>
											<option value="WED">Wednesday</option>
											<option value="THU">Thursday</option>
											<option value="FRI">Friday</option>
											<option value="SAT">Saturday</option>
										</select>
									</div>
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.new-rule-detail-box -->
	
							<div class="row">
								<div class="col-xs-12 d-flex justify-content-end">
									<input type="submit" value="Block" onclick="ajaxFraudRequest(this, vpaTransaction)" class="btn btn-success btn-sm btn-block btn-max-width mb-0 mr-0" />
								</div>
								<!-- /.col-xs-12 -->
							</div>
							<!-- /.row -->
						</div>
					</div>

				</div>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<script src="../js/horizontal-scrolling-nav.js"></script>
	<script src="../js/snackbar.js"></script>
	<script src="../js/fraudtype.js"></script>
</body>
</html>