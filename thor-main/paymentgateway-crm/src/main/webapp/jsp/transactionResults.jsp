<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Search Payment</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/defualt-style.css">
	<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<script src="../js/jquery.min.js" type="text/javascript"></script>
	<script src="../js/moment.js" type="text/javascript"></script>
	<script src="../js/daterangepicker.js" type="text/javascript"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
	<script src="../js/pdfmake.js" type="text/javascript"></script>
	<script src="../js/jquery.fancybox.min.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/common-scripts.js"></script>
	<script src="../js/transactionResult.js"></script>
	<script src="../js/tabs.js"></script>
	<style>
		.d-none { display: none !important; }
		.flex-column { flex-direction: column; }
		.d-flex { display: flex; }
		.fancybox-content { border-radius: 6px !important; }
	</style>

	<style type="text/css">
		/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
		#popup{
			position: fixed;
			top:0px;
			left: 0px;
			background: rgba(0,0,0,0.7);
			width: 100%;
			height: 100%;
			z-index:999; 
			display: none;
		}
		.innerpopupDv{
			width: 600px;
			margin: 60px auto;
			background: #fff;
			padding-left: 5px;
			padding-right: 15px;
			border-radius: 10px;
			margin-top: 20px;
		}
		.btn-custom {
			margin-top: 5px;
			height: 27px;
			border: 1px solid #5e68ab;
			background: #5e68ab;
			padding: 5px;
			font: bold 12px Tahoma;
			color: #fff;
			cursor: pointer;
			border-radius: 5px;
		}
		#loader-wrapper .loader-section.section-left, #loader-wrapper .loader-section.section-right{
			background: rgba(225,225,225,0.6) !important;
			width: 50% !important;
		}
		.invoicetable{
			float: none;
		}
		.innerpopupDv h2{
			font-size: 12px;
			padding: 5px;
		}


		.fancybox-content{
			padding: 0 !important;
		}


		td.my_class1 {
			color: #0040ff !important;
			text-decoration: none !important;
			cursor: pointer;
			*cursor: hand;
		}

		.text-class{
			text-align: center !important;
		}
	</style>
</head>
<body data-id="mainBody">
	<s:if test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId == null}">
		<s:hidden id="userMerchant" />
	</s:if>
	
	<s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
	<s:hidden value="%{#session.['USER'].superMerchant}"></s:hidden>

	<input type="hidden" id="setData">
	<section class="transaction-result lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Search Payment Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Merchant</label>
						<s:select
						name="merchant"
						data-live-search="true"
						class="selectpicker adminMerchants"
						id="merchant"
						headerKey=""
						headerValue="ALL"
						list="merchantList"
						listKey="emailId"
						listValue="businessName"
						autocomplete="off"
						/>
					</div>
				</div>
			</s:if>

			<s:else>
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Merchant</label>
						<s:select
							name="merchant"
							data-live-search="true"
							class="selectpicker adminMerchants"
							id="merchant"
							headerKey=""
							headerValue="ALL"
							list="merchantList"
							listKey="emailId"
							listValue="businessName"
							autocomplete="off"
						/>
					</div>
				</div>
				</s:if>
				<s:elseif test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Merchant</label>
							<s:select
							name="merchant"
							class="selectpicker"
							data-live-search="true"
							id="merchant"
							list="subMerchantList"
							listKey="emailId"
							listValue="businessName"
							autocomplete="off"
						/>
						</div>
					</div>
				</s:elseif>
				<s:elseif test="%{superMerchant == true}">
					<div class="col-md-3 mb-20 d-none">
						<div class="lpay_select_group">
							<label for="">Merchant</label>
							<s:select
								name="merchant"
								class="selectpicker"
								data-live-search="true"
								id="merchant"
								list="merchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:elseif>
				<s:else>
					<div class="col-md-3 mb-20 d-none">
						<div class="lpay_select_group">
							<label for="">Merchant</label>
							<s:select
								name="merchant"
								class="selectpicker"
								data-live-search="true"
								id="merchant"
								list="merchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:else>
			</s:else>
			<!-- merchant drop down  -->
			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select
							data-id="subMerchant"
							name="subMerchant"
							class="selectpicker"
							id="subMerchant"
							list="subMerchantList"
							listKey="emailId"
							data-live-search="true"
							listValue="businessName"
							autocomplete="off"
							headerKey="ALL"
							headerValue="ALL"
						/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:if>
			<s:else>
				<div class="col-md-3 d-none mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchant" id="subMerchant"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<!-- sub Merchant end -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Payment Method</label>
					<s:select
						headerKey=""
						headerValue="ALL"
						class="selectpicker"
						list="@com.paymentgateway.commons.util.PaymentType@values()"
						listValue="name"
						data-live-search="true"
						listKey="code"
						name="paymentMethod"
						id="paymentMethod"
						autocomplete="off"
						value=""
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield type="text" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<div class="col-md-3 mb-20 slide-form-element" id="txn-pgRefNum">
				<div class="lpay_input_group">
					<label for="">PG REF Number</label>
					<s:textfield
						id="pgRefNum"
						onkeyup="onlyAlphaNumeric(this)"
						class="lpay_input"
						name="pgRefNum"
						type="text"
						autocomplete="off"
						maxlength="16">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
					<label for="">Order Id</label>
					<s:textfield
						id="orderId"
						class="lpay_input"
						name="orderId"
						type="text"
						autocomplete="off"
						onkeypress="return Validate(event);">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->

			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
					<label for="">Cust Email</label>
					<s:textfield
						id="customerEmail"
						class="lpay_input"
						name="customerEmail"
						maxlength="40"
						type="text"
						autocomplete="off"
						onblur="validateEmail(this);">
					</s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_input_group">
					<label for="">RRN</label>
					<s:textfield
						id="rrn"
						onkeyup="onlyAlphaNumeric(this)"
						class="lpay_input"
						name="rrn"
						type="text"
						autocomplete="off">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>

			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label for="">Payment Region</label>
					<s:select
						headerKey=""
						headerValue="ALL"
						class="selectpicker"
						list="#{'DOMESTIC':'Domestic','INTERNATIONAL':'International'}"
						name="paymentRegion"
						id="paymentRegion"
						onchange="getLatestDataHandler();"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label for="">Transaction Type</label>
					<s:select
						headerKey=""
						headerValue="ALL"
						class="selectpicker"
						list="txnTypelist"
						listValue="name"
						listKey="code"
						name="transactionType"
						id="transactionType"
						autocomplete="off"
						value="name"
						onchange="getLatestDataHandler();"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<s:if test="%{(#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER') && #session.USER.customTransactionStatus==true}">
				<div class="col-md-3 mb-20 slide-form-element">
					<div class="lpay_select_group">
						<label for="">Status</label>
						<s:select
							class="selectpicker"
							list="#{'Pending':'Pending','Captured':'Captured','Timeout':'Timeout','Settled':'Settled','Sent to Bank':'Sent to Bank','Enrolled':'Enrolled',
							'Failed,Approved,Declined,Rejected,Error,Browser Closed,Cancelled,Denied by risk,Duplicate,Invalid,Authentication Failed,Denied due to fraud,
							Reconciled,Acquirer down,Processing,Failed at Acquirer,Timed out at Acquirer,Rejected by PG,PROCESSED':'Failed'}"
							name="status"
							title="ALL"
							id="status"
							value="name"
	                        autocomplete="off"
							onchange="getLatestDataHandler();"
							data-selected-text-format="count"
						    data-actions-box="true"
							multiple="true"
						/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
			</s:if>
			<s:else>
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label for="">Status</label>
					<s:select
						class="selectpicker"
						list="lst"
						name="status"
						title="ALL"
						id="status"
						value="name"
						listKey="name"
						listValue="name"
						autocomplete="off"
						onchange="getLatestDataHandler();"
						data-selected-text-format="count"
						data-actions-box="true"
						multiple="true"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			</s:else>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group">
					<label for="">Currency</label>
					<s:select
						name="currency"
						id="currency"
						headerValue="ALL"
						headerKey=""
						list="currencyMap"
						class="selectpicker"
						onchange="getLatestDataHandler();"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 slide-form-element">
				<div class="lpay_select_group d-flex flex-column">
					<label for="getLatest" class="mb-10">Latest Status</label>
					<label class="lpay_toggle lpay_toggle_on">
						<input type="checkbox" name="getLatest" id="getLatest" data-toggle="toggle" checked="true" />
					</label>
				</div>
            </div>
			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
		<div class="filter-icon">
			<span class="fa fa-angle-down"></span> 
		</div>
		<!-- /.filter-icon -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="transaction-result lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Search Transaction List </h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 lpay_table_style-2">
				<div class="lpay_table">
					<table id="txnResultDataTable" cellpadding="0" cellspacing="0" class="display" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>Transaction ID</th>
								<th>Merchant</th>
								<th>Txn Type</th>
								<th>Order ID</th>
								<th>Payment Method</th>
								<th>Acq ID</th>
								<th>RRN</th>
								<th>Status</th>
								<th>Date</th>
								<th>Total Amount</th>
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
		
	<s:form name="chargeback" action="chargebackAction">
		<s:hidden name="orderId" id="orderIdc" value="" />
		<s:hidden name="payId" id="payIdc" value="" />
		<s:hidden name="txnId" id="txnIdc" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>	
	
	<s:form name="refundDetails" action="refundConfirmAction">
		<s:hidden name="orderId" id="orderIdr" value="" />
		<s:hidden name="payId" id="payIdr" value="" />
		<s:hidden name="transactionId" id="txnIdr" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
	
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
	
	<section class="transaction-popup" id="fancybox">
		<div class="transaction-wrapper">
			<div class="transaction-tab">
				<span class="active-tab" data-src="customer-detail">Customer Detail</span>
				<span data-src="transaction-detail">Transaction Detail</span>
				<span data-src="shipping-detail">Shipping Detail</span>
				<span data-src="new-data">Status</span>
			</div>
			<!-- /.transaction-tab -->
			<div class="customer-detail transaction-box active-box mt-20">
				<div class="detail-wrapper">
					<div class="detail-box d-none">
						<div class="detail-lable">Name</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custName"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Mobile Number</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custMobileNum"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Address</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custAddress"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">City</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custCity"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">State</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custState"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Pin</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custPin"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Country</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custCountry"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
				</div>
				<!-- /.detail-wrapper -->
			</div>
			<!-- /.customer-detail -->
			<div class="transaction-detail transaction-box mt-20">
				<div class="detail-wrapper">
					<div class="detail-box d-none">
						<div class="detail-lable">Total Amount</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="totalAmount"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->

					<div class="detail-box d-none">
						<div class="detail-lable">Amount</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="amount"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->

					<div class="detail-box d-none">
						<div class="detail-lable">PG Commission</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="pgCommission"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->

					<div class="detail-box d-none">
						<div class="detail-lable">PG GST</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="pgGST"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">TDR/Surcharge</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="tdrORSurcharge"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">GST</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="GST"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Acquirer Commission</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="acquirerCommission"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					
					<div class="detail-box d-none">
						<div class="detail-lable">Acquirer GST</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="acquirerGST"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Acquirer Name</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="acquirerName"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Payment Type</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="paymentType"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Bank Name</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="bankName"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Region</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="region"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Cardholder Type</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="cardHolderType"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Order Id</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="orderId2"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">PG Ref Num</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="pgRefNum2"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">RRN</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="rrn"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Status</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="captureResponseMessage"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Settled Status</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="settleResponseMessage">India</div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
				</div>
				<!-- /.detail-wrapper -->
			</div>
			<!-- /.customer-detail -->
			<div class="shipping-detail transaction-box mt-20">
				<div class="detail-wrapper">
					<div class="detail-box d-none">
						<div class="detail-lable">Name</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingName"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Mobile Number</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingMobileNum"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Address</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingAddress"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">City</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingCity"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">State</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingState"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Pin</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingPin"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box d-none -->
					<div class="detail-box d-none">
						<div class="detail-lable">Country</div>
						<!-- /.detail-lable -->
						<div class="detail-value" id="custShippingCountry"></div>
						<!-- /.detail-value -->
					</div>
					<!-- /.detail-box -->
				</div>
				<!-- /.detail-wrapper -->
			</div>
			<!-- /.customer-detail -->
			<div class="new-data transaction-box mt-20">
				<div class="detail-wrapper">
					<div class="lpay_table_wrapper">
						<table class="lpay_custom_table" width="100%">
							<thead class="lpay_table_head">
								<tr>
									<th class="border-bottom-none" width="150">Order id</th>
									<th class="border-bottom-none">PG REF Num</th>
									<th class="border-bottom-none" width="100">Amount</th>
									<th class="border-bottom-none" width="100">Txn Type</th>
									<th class="border-bottom-none" width="100">Status</th>
									<th class="border-bottom-none" width="100">Date</th>
								</tr>
							</thead>
							<tbody class="dataBody"></tbody>
						</table>
						<!-- /.payment-options -->
					</div>
					<!-- /.lpay_table_wrapper -->
					
				</div>
				<!-- /.detail-wrapper -->
			</div>
			<!-- /.customer-detail -->
		</div>
		<!-- /.transaction-wrapper -->
	</section>
	<!-- /.transaction-popup -->
</body>
</html>