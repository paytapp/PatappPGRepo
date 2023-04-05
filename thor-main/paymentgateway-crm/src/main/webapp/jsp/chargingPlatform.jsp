<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Charging Platform</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery-latest.min.js"></script>
	<!-- <script type="text/javascript" src="../js/jquery-old.min.js"></script> -->
	<script src="../js/jquery.popupoverlay.js"></script>
	<link rel="stylesheet" type="text/css" href="../css/popup.css" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">

	<link rel="stylesheet" href="../css/chargingPlatform.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<style>
		.has-scroll .table_arrow{
			display: flex;
		}
		.charging-platform{
			position: relative;
		}
	</style>
</head>
<body>
	<s:actionmessage class="error error-new-text" />

	<section class="charging-platform lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Charging Platform</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select User</label>
				   <s:select
				   headerKey="-1"
				   headerValue="Select User"
				   list="#{'1':'Merchant'}"
				   id="user"
				   name="user"
				   value="1"
				   class="selectpicker"
				   autocomplete="off"												
			   		/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:select
				   headerValue="Select Merchant"
				   headerKey=""
				   name="emailId"
				   data-live-search="true"
				   class="selectpicker selctList"
				   id="merchants"
				   list="listMerchant"
				   listKey="emailId"
				   listValue="businessName"
				   autocomplete="off"												
			   />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Acquirer</label>
				   <s:select
				   class="selectpicker selctList"
				   headerKey=""
				   data-live-search="true"
				   headerValue="Select Acquirer"
				   list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
				   listKey="code"
				   listValue="name"
				   name="acquirer"
				   id="acquirer"
				   autocomplete="off"
			   />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Payment Type</label>
				   <select name="paymentType" data-live-search="true" id="payment-type" class="selectpicker selctList">
					<option value="">Payment Type</option>
					<option value="Credit Card">Credit Card</option>
					<option value="Debit Card">Debit Card</option>
					<option value="Net Banking">Net Banking</option>
					<option value="Wallet">Wallet</option>
					<option value="UPI">UPI</option>
					<option value="EMI">EMI</option>
					<option value="COD">COD</option>
					<option value="EMI CC">EMI CC</option>	
					<option value="EMI DC">EMI DC</option>
					<option value="MQR">MQR</option>
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 select-list-outer d-none" data-type="paymentRegion">
				<div class="lpay_select_group">
				   <label for="">Select Payment Region</label>
				   <select name="paymentRegion" id="payment-region" class="selectpicker selctList">
					<option value="">Payment Region</option>
					<option value="DOMESTIC">Domestic</option>
					<option value="INTERNATIONAL">International</option>
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20" data-type="acquiringMode">
				<div class="lpay_select_group">
				   <label for="">Select Acquiring Mode</label>
				   <select name="acquiringMode" id="acquiring-mode" class="selectpicker selctList">
					<option value="">Select Acquiring mode</option>
					<option value="ON_US">On Us</option>
					<option value="OFF_US">Off Us</option>
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 select-list-outer d-none" data-type="cardHolderType">
				<div class="lpay_select_group">
				   <label for="">Select Cardholder Type</label>
				   <select name="cardholderType" id="cardholder-type" class="selectpicker selctList">
					<option value="">Select Cardholder Type</option>
					<option value="CONSUMER">Consumer</option>
					<option value="COMMERCIAL">Commercial</option>
					<option value="PREMIUM">Premium</option>
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	

	<s:hidden name="token" value="%{#session.customToken}"></s:hidden>

	<section class="charging-platform lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Set Charges</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table_wrapper d-none" id="netbankingAll">
					<table class="lpay_custom_table">
						<thead class="lpay_table_head">
							<tr>
								<th width="71">Currency</th>
								<th width="200">Mop</th>
								<th width="88">Transaction</th>
								<th width="65">Merchant TDR</th>
								<th width="65">Merchant FC</th>
								<th width="65">Bank TDR</th>
								<th width="65">Bank FC</th>
								<th width="65">Reseller TDR</th>
								<th width="65">Reseller FC</th>
								<th width="75">GST</th>
								<th>Min / Max</th>
								<th>Allow FC</th>
								<th>Charges Flag</th>
								<th width="90">Max Charge Merchant</th>
								<th width="90">Max Charge Acquirer</th>
								<th width="70">Same as above</th>
								<th width="93">Actions</th>
							</tr>
							<tr></tr>
						</thead>
						<tbody>
							<tr>
								<td data-key="currency">
									<span></span>
									<input type="hidden" value="">
								</td>
								<td data-key="mopType">
									<span>All</span>
									<input type="hidden" value="All">
								</td>
								<td data-key="transactionType">
									<span></span>
									<input type="hidden" value="">
								</td>
								<td class="px-0 py-0" data-key="tdrFcDetail" colspan="6">
									<table class="table">
										<tbody>
											<tr data-key="slab1">
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="75" height="44">
													<div>
														<span class="merchant-value"></span>
														<input type="hidden" class="form-control" value="">
													</div>
												</td>
												<td width="75" height="44">
													<div>
														<span class="merchant-value"></span>
														<input type="hidden" class="form-control" value="">
													</div>
												</td>
											</tr>
											<tr data-key="slab2">
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="75" height="44">
													<div>
														<span class="merchant-value"></span>
														<input type="hidden" class="form-control" value="">
													</div>
												</td>
												<td width="75" height="44">
													<div>
														<span class="merchant-value"></span>
														<input type="hidden" class="form-control" value="">
													</div>
												</td>
											</tr>
											<tr data-key="slab3">
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div>
														<span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="65" height="44">
													<div><span class="displayed-value"></span>
														<input type="text" class="form-control" onkeypress="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4)" onchange="merchantTotal(this, 4)" value="">
													</div>
												</td>
												<td width="75" height="44">
													<div>
														<span class="merchant-value"></span>
														<input type="hidden" class="form-control" value="">
													</div>
												</td>
												<td width="75" height="44">
													<div>
														<span class="merchant-value"></span>
														<input type="hidden" class="form-control" value="">
													</div>
												</td>
											</tr>
										</tbody>
									</table>
								</td>
								<td data-key="merchantGST">
									<span></span>
									<input type="hidden" value="">
								</td>
								<td data-key="limitDetail" class="px-0 py-0">
									<table class="table">
										<tbody>
											<tr data-key="limitSlab1">
												<td class="max-charge d-flex align-items-center" height="44">
													<span class="d-inline-block position-relative"></span>
													<span class="d-inline-block position-relative"></span>
												</td>
											</tr>
											<tr data-key="limitSlab2">
												<td class="max-charge d-flex align-items-center" height="44">
													<span class="d-inline-block position-relative"></span>
													<span class="d-inline-block position-relative"></span>
												</td>
											</tr>
											<tr data-key="limitSlab3">
												<td class="max-charge d-flex align-items-center" height="44">
													<span class="d-inline-block position-relative"></span>
													<span class="d-inline-block position-relative"></span>
												</td>
											</tr>
										</tbody>
									</table>
								</td>
								<td data-key="allowFC">
									<div class="slideSwitch" title="Allow FC">
										<input type="checkbox" id="allowFC" name="allowFC">
										<label for="allowFC"></label>
									</div>
								</td>
								<td data-key="chargesFlag">
									<div class="slideSwitch" title="Charges Flag">
										<input type="checkbox" id="chargesFlag" name="chargesFlag">
									</div>
								</td>
								<td data-key="maxChargeMerchant">
									<div>
										<span class="displayed-value"></span>
										<input type="text" class="form-control" onkeypress="onlyNumericKey(this, event, 0);" onkeyup="onlyNumericKey(this, event, 0);setDecimal(this, event)" onfocusout="updateValue(this, 0)" value="">
									</div>
								</td>
								<td data-key="maxChargeAcquirer">
									<div>
										<span class="displayed-value"></span>
										<input type="text" class="form-control" onkeypress="onlyNumericKey(this, event, 0);" onkeyup="onlyNumericKey(this, event, 0);setDecimal(this, event)" onfocusout="updateValue(this, 0)" value="">
									</div>
								</td>
								<td data-key="sameAsAbove">
									<div class="slideSwitch" title="Same as above">
										<input type="checkbox" id="sameAsAbove" name="sameAsAbove">
										<label for="sameAsAbove"></label>
									</div>
								</td>
								<td data-key="action">
									<div class="tdr-edit-btn-box">
										<button type="submit" onclick="editTDR(this)" class="tdr-edit-btn"><i class="fa fa-pencil-alt" aria-hidden="true"></i> Edit</button>
									</div>
									<div class="tdr-save-btn-box">
										<button type="submit" name="editAll" class="tdr-save-btn mr-10"><i class="fa fa-check" aria-hidden="true"></i>
										</button><a href="#" class="tdr-cancel-btn"><i class="fa fa-times" aria-hidden="true"></i></a>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<!-- /.lpay_table_wrapper -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
		<section class="charging-platform bg-color-white">		
			<!-- <ul class="charging-platform-nav p-0 list-unstyled d-flex flex-wrap mt-20"></ul> -->
			<div class="charging-detail-box" id="netbankingAll">
				<h3 class="mb-10 color-grey-dark">Charging detail for all bank</h3>
				<div class="table-responsive">
					
				</div>
				<h3 class="mt-30 mb-10 color-grey-dark">Charging detail for individual bank</h3>
			</div>
			<!-- /.netbanking-all-detail -->
			<div class="charging-platform-detail"></div>
		</section>
		<!-- /.charging-platform -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->




	<script src="../js/chargingPlatform.js"></script>
	<script src="../js/decimalLimit.js"></script>
</body>
</html>