<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Download Payments Report</title>
	
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/common-scripts.js"></script>

	<style>
		.flex-column { flex-direction: column; }
		.d-flex { display: flex; }
		.lp-success_generate, .lp-error_generate {
            background-color: #c0f4b4;
            font-size: 15px;
            padding: 10px;
            text-align: center;
            margin-top: 20px;
            border-radius: 5px;
            border: 1px solid #3b9f24;
        }
    
        .lp-error_generate{
            background-color: #f79999;
            border: 1px solid #771313;
        }
    
        .lp-success_generate p{ 
            color: #326626;
        }
    
        .lp-error_generate p{
            color: #921919;
        }
	</style>
</head>
<body>
	<section class="download-report lapy_section white-bg box-shadow-box mt-70 p20">
		<form id="downloadPaymentsReportAction" name="downloadPaymentsReportAction" action="downloadPaymentsReportAction">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Download Payments Report</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
						
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select
								name="merchantPayId"
								class="selectpicker adminMerchants"
								id="merchantPayId"
								headerKey="ALL"
								data-live-search="true"
								headerValue="ALL"
								list="merchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
								data-download="merchantPayId"
							/>
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN'|| #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<div class="col-md-3 mb-20">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select
									name="merchantPayId"
									class="selectpicker adminMerchants"
									id="merchantPayId"
									
									multiple="true" data-selected-text-format="count>2" data-actions-box="true"
									data-live-search="true"
									title="ALL"
									list="merchantList"
									listKey="payId"
									listValue="businessName"
									autocomplete="off"
									data-download="merchantPayId"
								/>
							</div>
						</div>
					</s:if>
					<s:elseif test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
						<div class="col-md-3 d-none mb-20">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select
									name="merchantPayId"
									class="selectpicker"
									id="merchantPayId"
									list="subMerchantList"
									listKey="payId"
									data-live-search="true"
									listValue="businessName"
									autocomplete="off"
									data-download="merchantPayId"
								/>
							</div>
						</div>
                    </s:elseif>
					<s:else>
						<div class="col-md-3 mb-20 d-none">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select
									name="merchantPayId"
									class="selectpicker"
									id="merchantPayId"
									list="merchantList"
									listKey="payId"
									data-live-search="true"
									listValue="businessName"
									autocomplete="off"
									data-download="merchantPayId"
								/>
							</div>
						</div>
					</s:else>
				</s:else>

				<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
					<div class="col-md-3 mb-20" data-id="submerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<s:select
								data-id="subMerchant"
								name="subMerchantPayId"
								class="selectpicker"
								id="subMerchant"
								list="subMerchantList"								
								listKey="emailId"
								data-live-search="true"
								listValue="businessName"
								autocomplete="off"
								headerKey="ALL"
								headerValue="ALL"
								data-download="subMerchantPayId"
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
						<select name="subMerchantPayId" data-download="subMerchantPayId" id="subMerchant"></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>

				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Payment Method</label>
						<s:select
							headerKey="ALL"
							headerValue="ALL"
							class="selectpicker"
							list="@com.paymentgateway.commons.util.PaymentType@values()"
							listValue="name"
							listKey="code"
							data-live-search="true"
							name="paymentType"
							id="paymentType"
							autocomplete="off"
							value=""
							data-download="paymentType"
						/>
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->

				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Transaction Type</label>
						<s:select
							headerKey=""
							headerValue="ALL"
							class="selectpicker"
							list="txnTypelist"
							listValue="name"
							listKey="code"							
							id="transactionType"
							autocomplete="off"
							value="name"
							data-download="transactionType"
						/>
						<input type="hidden" name="transactionType" id="input-transactionType">
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				
				<s:if test="%{(#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER') &&  #session.USER.customTransactionStatus==true}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Status</label>
						<s:select
							title="ALL"
							class="selectpicker"
							list="#{'Pending':'Pending','Captured':'Captured','Timeout':'Timeout','Settled':'Settled','Sent to Bank':'Sent to Bank','Enrolled':'Enrolled',
							'Failed,Approved,Declined,Rejected,Error,Browser Closed,Cancelled,Denied by risk,Duplicate,Invalid,Authentication Failed,Denied due to fraud,
							Reconciled,Acquirer down,Processing,Failed at Acquirer,Timed out at Acquirer,Rejected by PG,PROCESSED':'Failed'}"							
							id="status"
							value="name"
							autocomplete="off"
							data-selected-text-format="count"
							data-actions-box="true"
							multiple="true"
							data-download='status'
						/>
						<input type="hidden" name="status" id="input-status">
					</div>
					<!-- /.lpay_select_group -->  
				</div>
			    </s:if>
			    <s:else>
                  <div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Status</label>
						<s:select
							title="ALL"
							class="selectpicker"
							list="lst"							
							id="status"
							value="name"
							listKey="name"
							listValue="name"
							autocomplete="off"
							data-selected-text-format="count"
							data-actions-box="true"
							multiple="true"
							data-download="status"
						/>
						<input type="hidden" name="status" id="input-status">
					</div>
					<!-- /.lpay_select_group -->  
				</div>
			   </s:else>
				<!-- /.col-md-3 mb-20 -->
			
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Transaction Region</label>
						<s:select
							headerKey="ALL"
							headerValue="ALL"
							class="selectpicker"
							list="#{'INTERNATIONAL':'International','DOMESTIC':'Domestic'}"							
							id="paymentsRegion"
							data-download='paymentsRegion'
						/>
						<input type="hidden" name="paymentsRegion" id="input-paymentsRegion">
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->

				<s:if test="%{#session.USER.UserType.name()=='ADMIN'|| #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Acquirer</label>
							<s:select
								title="ALL"
								list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" 
								id="acquirer"
								class="selectpicker"
								listKey="code"
								listValue="name"								
								value="acquirer"
								data-selected-text-format="count"
								data-actions-box="true"
								multiple="true"
								data-live-search="true"
								data-download='acquirer'
							/>
							<input type="hidden" name="acquirer" id="input-acquirer">
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 mb-20 -->
				</s:if>
				<s:else>
					<input data-download="acquirer" type="hidden">				
				</s:else>

				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Currency</label>
						<s:select							
							id="currency"
							headerValue="ALL"
							headerKey=""
							list="currencyMap"
							class="selectpicker"
							data-download='currency'
						/>
						<input type="hidden" name="currency" id="input-currency">
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->

				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
						<label for="">Date From</label>
						<s:textfield
							type="text"
							readonly="true"
							id="dateFrom"
							name="dateFrom"
							class="lpay_input"
							onchange="dateBaseDownload()"
							autocomplete="off"
							data-download="dateFrom"
						/>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->

				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
						<label for="">Date To</label>
						<s:textfield
							type="text"
							readonly="true"
							data-download='dateTo'
							id="dateTo"
							name="dateTo"
							class="lpay_input"
							onchange="dateBaseDownload();"
							autocomplete="off"
						/>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->

				<!-- <div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="getLatest">Select for getLatest</label>
						<select class="selectpicker" id="getLatest" autocomplete="off" onchange="getLatestDataHandler();">
							<option value="false">OFF</option>
							<option value="true">ON</option>
						</select>

						<input type="hidden" name="searchFlag" id="input-getLatest">
					</div>
				</div> -->

				<div class="col-md-3 mb-20">
					<div class="lpay_select_group d-flex flex-column">
						<label for="getLatest" class="mb-10">Latest Status</label>
						<label class="lpay_toggle lpay_toggle_on">
							<input type="checkbox" id="getLatest" data-toggle="toggle" checked="true" />
							<input type="hidden" name="searchFlag" id="input-getLatest" value="true">
						</label>
					</div>
				</div>

				<div class="col-md-12 text-center">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="btn-download-report">Download</button>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 d-none">
					<div class="lp-success_generate">
						<p>Your file has been generate successfully please see after some time</p>
					</div>
					<!-- /.lp-success_generate -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 d-none">
					<div class="lp-error_generate">
						<p>Please try again after some time</p>
					</div>
					<!-- /.lp-success_generate -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</form>
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<script src="../js/download-payment-report.js"></script>
</body>
</html>