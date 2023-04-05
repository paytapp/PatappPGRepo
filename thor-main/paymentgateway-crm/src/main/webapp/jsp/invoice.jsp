<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
	<title>Invoice Payment</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/bootstrap-datetimepicker.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/invoice.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<!-- <script src="../js/bootstrap.min.js"></script> -->
	<script src="../js/city_state.js" type="text/javascript"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<!-- BOOTSTRAP DATETIME PICKER -->
	<script type="text/javascript" src="../js/moment-with-locales.js"></script>
	<!-- <script type="text/javascript" src="../js/bootstrap.min.js"></script> -->
	<script type="text/javascript" src="../js/bootstrap-datetimepicker.js"></script>

	<style>

		.ui-dialog-buttonset button{
			margin-top: 10px !important;
		}

		.error {
			left: auto;
			top: 2px;
			right: 0;
		}
		.textFL_merch_invalid { border-bottom-color: #f00; }
		.ui-datepicker .ui-datepicker-title {
			color: black;
			font-weight: bold;
		}
		.text-danger { color: red; }
		#wwgrp_mappedCurrency { display: none; }
	</style>

</head>
<body>
	<div id="footer-html" title="Send invoice link to customer"></div>

	<div class="snackbar bg-snackbar-danger text-snackbar-danger font-size-14" id="error-snackbar"></div>
	<div class="snackbar bg-snackbar-success text-snackbar-success font-size-14" id="success-snackbar"></div>

	<section class="invoice-page lapy_section white-bg box-shadow-box mt-70 p20">
		<input type="hidden" id="emailCheck" name="emailCheck" class="textFL_merch invoice-input" value="false"></input>
		<input type="hidden" id="smsCheck" name="smsCheck" value="false" class="textFL_merch invoice-input" ></input>
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Invoice Payment</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}"></s:textfield>
			<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<s:select
							name="merchantPayId"
							class="selectpicker textFL_merch "
							id="merchantPayId"
							headerKey=""
							data-live-search="true"
							headerValue="Select Any Merchant"
							list="merchantList"
							listKey="payId"
							listValue="businessName"
							autocomplete="off"
						/>
						<span id="merchantPayIdErr" class="error"></span>
					</div>
					</div>
				</s:if>
				<s:if test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
					<div class="col-md-3 mb-20 d-none">

						<div class="lpay_select_group" >
								<s:select
									name="merchantPayId"
									class="selectpicker textFL_merch"
									id="merchantPayId"
									list="merchantList"
									data-live-search="true"
									listKey="payId"
									listValue="businessName"
									autocomplete="off"
								/>
								<span id="merchantPayIdErr" class="error"></span>
							</div>
					</div>
					<!-- /.col-md-3 mb-20 -->
				</s:if>
			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <!-- <label for="">Sub Merchant</label> -->
					   <s:select data-id="subMerchant" name="subMerchantId" headerKey="" headerValue="Select Sub Merchant" class="selectpicker textFL_merch" id="subMerchant"
							list="subMerchantList" listKey="payId"
							listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-id="submerchant"> 
					<div class="lpay_select_group">
					   <!-- <label for="">Sub Merchant</label> -->
					   <select name="subMerchantId" id="subMerchant" class="textFL_merch"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			
			<div class="col-md-12 mb-20">
				<h3>Detail Information</h3>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Name <span class="text-danger">*</span></label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="name"
					onkeyup="FieldValidator.valdName(false)"
					name="name"
					autocomplete="off"
					onkeypress="return lettersOnly(event,this);"
				/>
				<span id="nameErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="phone">Phone <span class="text-danger">*</span></label>
				<s:textfield
					type="text"
					id="phone"
					name="phone"
					value="%{invoice.phone}"
					maxlength="10"
					class="textFL_merch lpay_input invoice-input"
					autocomplete="off"
					onkeyup="FieldValidator.valdPhoneNo(false, event); FieldValidator.validateEmailPhone(false, event)" onkeypress="return isOnlyNumberKey(event)"
				/>
				<span id="phoneErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="emailId">Email <span class="text-danger">*</span></label>
				<s:textfield
					type="text"
					id="emailId"
					name="email"
					value="%{invoice.email}"
					class="textFL_merch lpay_input invoice-input"
					onkeypress="return forEmailSpecialKeyAndAlphaNumeric(event,this);"
					onkeyup="FieldValidator.valdEmail(false, event); FieldValidator.validateEmailPhone(false, event)"
					autocomplete="off"
				/>
				<span id="emailIdErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="address">Address</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="address"
					onkeyup="FieldValidator.valdAddress(false)"
					name="address"
					autocomplete="off"
				/>
				<span id="addressErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="country">Country</label>
					<select
						class="textFL_merch invoice-input"
						id="country"
						name="country">
					</select>
					<s:hidden id="dataCountry" value="%{invoice.country}" />
					<span id="countryErr" class="error"></span>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="state">State</label>
					<select
						class="textFL_merch invoice-input"
						id="state"
						name="state">
					</select>
					<s:hidden id="dataState" value="%{invoice.state}" />
					<span id="stateErr" class="error"></span>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="state">City</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="city"
					onkeyup="FieldValidator.valdCity(false)"
					name="city"
					autocomplete="off"
					onkeypress="return lettersOnly(event,this);"
				/>
				<span id="cityErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="zip">Zip</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="zip"
					maxlength="6"
					name="zip"
					autocomplete="off"
					onkeyup="FieldValidator.valdZip(false)"
					onkeypress="return isOnlyNumberKey(event)"
				/>
				<span id="zipErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="zip">Duration From</label>
				<s:textfield 
					type="text"
					id="durationFrom"
					name="durationFrom"
					class="textFL_merch lpay_input invoice-input date-input"
					autocomplete="off"
					readonly="true" />
				<span id="durationFromErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="zip">Duration To</label>
				<s:textfield 
					type="text"
					id="durationTo"
					name="durationTo"
					class="textFL_merch lpay_input invoice-input date-input"
					autocomplete="off"
					readonly="true" />
				<span id="durationToErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-12 mb-20">
				<h3>Product Information</h3>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="productName">Name <span class="text-danger">*</span></label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="productName"
					name="productName"
					autocomplete="off"
					onkeypress="return productNameAndDescription(event,this);"
					onkeyup="FieldValidator.valdProductName(false)"
				/>
				<span id="productNameErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="productDesc">Description</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="productDesc"
					onkeypress="return productNameAndDescription(event,this);"
					name="productDesc"
					autocomplete="off"
				/>
				<span id="productDescErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="expiresDay">Expiry <span class="text-danger">*</span></label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					id="expiresDay"
					name="expiresDay"
					autocomplete="off"
					readonly="true"
					onkeyup="FieldValidator.valdExpDayAndHour(false)"
					onkeypress="return isOnlyNumberKey(this,event)"
				/>
				<span id="expiresDayErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="currencyCode">All prices are in <span class="text-danger">*</span></label>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<s:select
							name=""
							id="currencyCode"
							headerValue="Select Currency"
							headerKey="Select Currency"
							list="currencyMap"
							listKey="key"
							listValue="value"
							class="textFL_merch selectpicker"
							autocomplete="off"
						/>
						<s:select
							name="currencyCode"
							id="mappedCurrency"
							headerValue="Select Currency"
							headerKey="Select Currency"
							list="currencyMap"
							listKey="key"
							listValue="value"
							class="textFL_merch selectpicker"
							onchange="FieldValidator.valdCurrCode(false)"
							autocomplete="off"
						/>
					</s:if>
					
					<s:if test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
						<s:select
							name="currencyCode"
							id="mappedCurrency"
							headerValue="Select Currency"
							headerKey="Select Currency"
							list="currencyMap"
							listKey="key"
							listValue="value"
							class="textFL_merch selectpicker"
							onchange="FieldValidator.valdCurrCode(false)"
							autocomplete="off"
						/>
					</s:if>
					<span id="mappedCurrencyErr" class="error"></span>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="quantity">Quantity</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					value="%{invoice.quantity}"
					id="quantity"
					name="quantity"
					onkeyup="sum();
					FieldValidator.valdQty(false);"
					autocomplete="off"
					onkeypress="return isOnlyNumberKey(event)"
				/>
				<span id="quantityErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="amount">Amount <span class="text-danger">*</span></label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					onkeyup="sum();FieldValidator.valdAmount(false);"
					id="amount"
					name="amount"
					value="%{invoice.amount}"
					autocomplete="off"
					placeholder="0.00"
					onkeypress="return isNumberKeyWithDecimal(this,event);"
				/>
				<span id="amountErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="serviceCharge">Service Charge</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					value="%{invoice.serviceCharge}"
					id="serviceCharge"
					name="serviceCharge"
					placeholder="0.00"
					onkeyup="sum();FieldValidator.valdSrvcChrg(false);"
					autocomplete="off"
					onkeypress="return isNumberKeyWithDecimal(this,event)"
				/>
				<span id="serviceChargeErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="totalAmount">Total Amount</label>
				<s:textfield
					type="text"
					class="textFL_merch lpay_input invoice-input"
					readonly="true"
					placeholder="0.00"
					id="totalAmount"
					name="totalAmount"
					autocomplete="off"
				/>
				<span id="amountErr" class="error"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<div class="col-md-12 mb-20">
				<h3>Add Additional Fields</h3>
			</div>
			<!-- /.col-md-12 -->
			<div id="UDF_div" class="UDF_div">

			</div>
			<!-- /.UDF_div -->
			<div class="col-md-3 mb-20">
				<button class="lpay_button lpay_button-md lpay_button-secondary m-0" style="box-shadow: inset 1px 1px 5px rgba(0,0,0,.7)" id="addUdf">
					<i class="fa fa-plus font-size-12 mr-5" aria-hidden="true"></i> ADD FIELD
				</button> 
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12">
				<div class="lpay_input_group d-flex">
					<input
						type="text"
						id="invoiceLink"
						onkeydown="document.getElementById('copyBtn').focus();"
						class="textFL_merch lpay_input d-none"
						value="<s:property value="url" />"
					/>
					<input type="button" id="copyBtn" class="lpay_button lpay_button-md lpay_button-secondary d-none" value="Copy Payment Link "/>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<div class="col-md-12 submit-wrapper text-center">
				<input type="button" id="btnSave" name="btnSave" class="lpay_button lpay_button-md lpay_button-secondary" value="Save" />
				
				<!-- /.lpay_button lpay_button-md lpay_button-primary -->
				<input type="button" id="btnReset" name="btnReset" style="margin-top: 20px !important" class="lpay_button lpay_button-md lpay_button-primary d-none" value="Create New Invoice" onclick="location.reload(true);" />
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

		
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>

<script src="../js/FieldValidator.js"></script>
<script src="../js/snackbar.js"></script>
<script src="../js/invoice.js"></script>
</body>
</html>