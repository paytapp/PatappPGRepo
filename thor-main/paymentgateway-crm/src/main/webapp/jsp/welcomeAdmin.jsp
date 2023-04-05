<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Dashboard</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<script src="../js/jquery.min.js"></script>
	<link href="../css/welcomePage.css" rel="stylesheet">
	<script src="../js/common-scripts.js"></script>
	<script src="../js/highcharts.js"></script>
	<script src="../js/exporting.js"></script>
	<script src="../js/export-data.js"></script>
	<!-- searchable select option -->
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/dashboard-style.css">
	<script src="../js/jquery-ui.js"></script>
</head>
<body style="margin:0px; padding:0px;" class="dashbaord" title="dashboard">
	<s:hidden value="%{reportPermission}" id="report-permission" />
	<s:hidden value="%{#session.USER.UserType.name()}" id="logged-usertype" />

	<s:if test="%{#session['USER_PERMISSION'].contains('Payout')}">
		<s:hidden id="subAdminTrue"></s:hidden>
	</s:if>
	<s:hidden value="%{#session['USER'].merchantInitiatedDirectFlag}" id="allowPayout"></s:hidden>
	<s:hidden value="%{#session['USER'].partnerFlag}" id="partnerFlag"></s:hidden>
	<s:if test="%{#session['USER'].superMerchant == true}">
		<s:hidden value="true" id="superMerchantLogin"></s:hidden>
	</s:if>
	<div id="page-inner">
		<div class="dashboard_div lpay_section white-bg box-shadow-box mt-70 p-15 p-md-20">
			<div class="row">
				<div class="col-md-6">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Dashboard</h2>
					</div>
				</div>
				<div class="col-md-6 d-flex justify-content-end">
					<div class="lpay_select_group form-group mw-120 w-100">
						<!-- <label class="mb-0 font-weight-bold font-size-12 d-flex align-items-center mr-15">Sale / Refund</label>
						<div class="d-flex">
							<label class="lpay_toggle lpay_toggle_on mt-2">
								<input type="checkbox" name="saleOrRfundFlag" id="saleOrRfundFlag" data-toggle="toggle" checked="true" />
							</label>
							<span class="d-flex align-items-center ml-15 txn-unit">Sale</span>
						</div> -->

						<select name="saleOrRfundFlag" id="saleOrRfundFlag" class="selectpicker" onchange="handleChange();">
							<option value="true">Sale</option>
							<option value="false">Refund</option>
							<option value="payout">Payout</option>
						</select>
					</div>
				</div>
			</div>
			<!-- /.row -->
			

			<div class="dashboard-new-container d-none">

				<div class="dashboard-containe">
					<div class="row">
						<div class="col-md-12 d-flex justify-content-center flex-wrap dashboard-filter-payout">
							<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER.UserType.name()=='RESELLER' || #session['USER'].superMerchant == true}">
								<div class="form-group mb-0 lpay_select_group w-100 w-md-200">
									<label>Select Merchant</label>
									<s:select
										name="emailId"
										data-payout="emailId"
										data-submerchant="payoutSubMerchant"
										data-user="subUser"
										class="form-control selectpicker"
										id="payoutMerchant" 
										data-live-search="true"
										headerKey="ALL MERCHANTS" headerValue="ALL MERCHANTS"
										listKey="emailId" listValue="businessName"
										list="merchantList" autocomplete="off" onchange="handleChange();"
									/>
								</div>
								<!-- /.form-group mb-0 lpay_select_group -->
							</s:if>
							<s:elseif test="%{#session.USER.UserType.name()=='MERCHANT'}">
								<div class="form-group mb-0 d-none lpay_select_group w-100 w-md-200">
									<label>Select Merchant</label>
									<s:select
										name="emailId"
										class="form-control selectpicker"
										data-payout="emailId"
										id="payoutMerchant" 
										data-submerchant="payoutSubMerchant"
										data-user="subUser"
										listKey="emailId"
										listValue="businessName"
										list="merchantList"
										autocomplete="off"
									/>
								</div>
								<!-- /.form-group mb-0 lpay_select_group -->
							</s:elseif>
		
							<s:if test="%{superMerchant == true}">
								<div class="form-group mb-0 lpay_select_group w-100 w-md-200" data-id="payoutSubMerchant">
									<label>Sub Merchant</label>
									<s:select
										name="subMerchant"
										class="form-control selectpicker"
										data-payout="subMerchant"
										id="payoutSubMerchant" 
										listKey="payId"
										headerKey=""
										headerValue="ALL"
										listValue="businessName"
										list="subMerchantList"
										autocomplete="off"
									/>
								</div>
							</s:if>
							<s:else>
								<div class="form-group mb-0 lpay_select_group w-100 w-md-200 d-none" data-target="payoutSubMerchant">
									<label>Sub Merchant</label>
									<select name="subMerchant" data-payout="subMerchant" id="payoutSubMerchant"></select>
								</div>
								<!-- /.lpay_select_group -->					
							</s:else>
							
						
							<div class="form-group mb-0 lpay_input_group w-100 w-md-150">
								<label>Date From <span class="color-red">*</span></label>
								<s:textfield
									type="text"
									id="payoutDateFrom"
									data-payout="dateFrom"
									name="dateFrom"
									class="lpay_input date-input datepick"
									autocomplete="off"
									readonly="true"
								/>
							</div>
							<!-- /.lpay_select_group -->
						
							<div class="form-group mb-0 lpay_input_group w-100 w-md-150">
								<label>Date To <span class="color-red">*</span></label>
								<s:textfield
									type="text"
									id="payoutDateTo"
									name="dateTo"
									data-payout="dateTo"
									class="lpay_input date-input datepick"
									autocomplete="off"
									readonly="true"
								/>
							</div>
							<!-- /.lpay_select_group -->
							<!-- /.lpay_select_group -->
							<button class="lpay_button lpay_button-md lpay_button-secondary px-13 mt-15 mx-0" id="btn-payoutData">Submit</button>
						</div>
					</div>
	
					<div class="row mt-20">
						<div class="col-md-12 text-center">
							<div class="payoutFilterButton mt-10 d-flex flex-wrap justify-content-center">
								<button type="button" id="payoutButtonDay" name="day" data-lastday="1" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary btnActive">Day</button>
								<button type="button" id="payoutButtonWeekly" name="week" data-lastday="7" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Week</button>
								<button type="button" id="payoutButtonMonthly" name="month" data-lastday="31" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Month</button>
								<button type="button" id="payoutButtonYearly" name="year" data-lastday="365" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Year</button>
								<!-- <button type="button" id="buttonLastMonth" name="lastMonth" data-lastday="31" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Last Month</button> -->
							</div>
						</div>
					</div>
				</div>
				<!-- /.dashboard-container -->

				<div class="dashboard-containe">
					<div class="row d-flex flex-wrap d-md-block">
						<div class="col-md-5ths statistics-box">
							<div class="heading_with_icon mb-10 ">
								<span class="heading_icon_box green"><i class="fa fa-thumbs-up"></i></span>
								<h2 class="heading_text"> <span id="payoutTotalTransaction" class="payout-data" data-pay='totalTransaction' data-download="Total">0</span>Total Transactions</h2>
							</div>
						</div>
						<div class="col-md-5ths statistics-box">
							<div class="heading_with_icon mb-10">
								<span class="heading_icon_box green"><i class="fa fa-thumbs-up"></i></span>
								<h2 class="heading_text"> <span id="payoutTotalSuccess" data-pay="totalSuccess" class="payout-data" data-download="Captured">0</span>Total Captured</h2>
							</div>
						</div>
						<div class="col-md-5ths statistics-box">
							<div class="heading_with_icon mb-10">
								<span class="heading_icon_box brown"><i class="fa fa-clock-o"></i></span>
								<h2 class="heading_text"> <span id="payoutTotalPending" data-pay="totalPending" class="payout-data" data-download="Pending">0</span>Total Pending</h2>
							</div>
						</div>
						<div class="col-md-5ths statistics-box">
							<div class="heading_with_icon mb-10">
								<span class="heading_icon_box red"><i class="fa fa-thumbs-down"></i></span>
								<h2 class="heading_text"> <span id="payoutTotalFailed" data-download="Failed" class="payout-data" data-pay="totalFailed">0</span> Total Failed</h2>
							</div>
						</div>
						
						
	
						<div class="col-md-5ths statistics-box">
							<div class="heading_with_icon mb-10">
								<span class="heading_icon_box"><i class="fa fa-check"></i></span>
								<h2 class="heading_text"> <span id="payoutTotalAmount" data-pay="totalAmount">0</span> Total Amount</h2>
							</div>
						</div>
						<div class="col-md-5ths statistics-box">
							<div class="heading_with_icon mb-10">
								<span class="heading_icon_box"><i class="fa fa-check"></i></span>
								<h2 class="heading_text"> <span id="payoutTotalCapturedAmount" data-pay="totalCapturedAmount">0</span> Total Captured Amount</h2>
							</div>
						</div>
		
					</div>
					<!-- /.row mt-10 -->
				</div>
				<!-- /.dashboard-container -->
				<div class="row ledger_dashboard-container mt-20">
					<div class="col-md-9 dashboard_graph">
                        <div class="ledger-account-div highcharts-figure-jd">
						    <div id="container_ledger" style="min-width: 270px; margin: 0 auto"></div>
							<div class="empty-graph">
                                Hi! We didn't find any payouts in last seven (7) days.
                            </div>
                            <!-- /.empty-graph -->
                        </div>
                        <!-- /.ledger-account-div -->
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-3 check-balance-div">
                        <div class="row">
							<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER.UserType.name()=='RESELLER' || #session['USER'].superMerchant == true}">
								<div class="col-md-12 mb-20 single-account-input">
									<div class="lpay_select_group">
										<label for="">Select Merchant</label>
										<s:select 
											data-required="true"
											name="merchantPayIdBulkUpi"
											data-download="merchantPayIdBulkUpi" 
											class="selectpicker"
											id="merchantPayIdLedger" 
											headerKey="ALL" 
											data-var="payId" 
											data-submerchant="subMerchantLedger" 
											data-user="subUser"  
											data-live-search="true" 
											headerValue="ALL"
											list="merchantList" 
											listKey="payId" 
											listValue="businessName" 
											autocomplete="off"
										/>
										<span class="error-field"></span>
									</div>
								</div>
							</s:if>
							<s:elseif test="%{#session.USER.UserType.name()=='MERCHANT'}">
								<div class="col-md-12 mb-20 d-none single-account-input">
									<div class="lpay_select_group">
										<label for="">Select Merchant</label>
										<s:select 
											data-required="true"
											name="merchantPayIdBulkUpi"
											data-download="merchantPayIdBulkUpi" 
											class="selectpicker"
											id="merchantPayIdLedger"
											data-var="payId" 
											data-submerchant="subMerchantLedger" 
											data-user="subUser"  
											data-live-search="true" 
											list="merchantList" 
											listKey="payId" 
											listValue="businessName" 
											autocomplete="off"
											onchange="removeError(this)"
										/>
										<span class="error-field"></span>
									</div>
								</div>
							</s:elseif>
                            <s:if test="%{superMerchant == true}">
                                <div class="col-md-12 mb-20 single-account-input" data-target="subMerchantLedger">
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <s:select 
                                            data-id="subMerchantLedger"
                                            data-download="subMerchantReporting" 
                                            data-var="subMerchantPayId" 
                                            data-submerchant="subMerchantReport" 
                                            data-user="subUser"  
                                            name="subMerchantLedger" 
                                            class="selectpicker" 
                                            id="subMerchantLedger" 
                                            list="subMerchantList" 
                                            listKey="payId"
                                            headerKey="ALL" 
                                            headerValue="ALL"
                                            listValue="businessName" 
                                            autocomplete="off" 
                                        />
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            <!-- /.col-md-3 -->	
                            </s:if>
                            <s:else>
                                <div class="col-md-12 mb-20 d-none single-account-input" data-target="subMerchantLedger"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <select name="subMerchantLedger" data-download="subMerchantLedger" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchantLedger" onchange="removeError(this)" data-user="subUser" id="subMerchantLedger" class=""></select>
                                        <span class="error-field"></span>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                            </s:else>
                            <!-- /.col-md-3 -->
                            <div class="col-md-12">
                                <div class="ledger-check_balance_div bg-gradient-primary d-flex-column box-shadow-section br-5 p20 d-flex-space-between">
                                    <div class="ledger-balance d-flex d-flex-column text-center mb-20">
                                        <span class="ledger-amount">INR <span id="ledgerAmount">0.00</span></span>
                                        <span class="ledger-text">Current Balance</span>
                                    </div>
                                    <!-- /.ledger-balance -->
                                    <div class="ledger-refresh d-flex-center br-50" id="checkBalance">
                                        <i class="fa fa-refresh" aria-hidden="true"></i>
                                    </div>
                                    <!-- /.ledger-refresh -->
                                </div>
                                <!-- /.ledger-check_balance_div -->
                            </div>
                            <!-- /.col-md-12 -->
                        </div>
                        <!-- /.row -->
                    </div>
                    <!-- /.col-md-3 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.dashboard-new-container -->
			
			<div class="dashboard-container">
				<div class="row">
					<div class="col-md-12 d-flex justify-content-center flex-wrap dashboard-filter">
						<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER.UserType.name()=='RESELLER' || #session['USER'].superMerchant == true}">
							<div class="form-group mb-0 lpay_select_group w-100 w-md-200">
								<label>Select Merchant</label>
								<s:select
									name="merchants"
									data-submerchant="subMerchant"
									data-user="subUser"
									class="form-control selectpicker"
									id="merchant" 
									data-live-search="true"
									headerKey="ALL MERCHANTS" headerValue="ALL MERCHANTS"
									listKey="emailId" listValue="businessName"
									list="merchantList" autocomplete="off" onchange="handleChange();"
								/>
							</div>
							<!-- /.form-group mb-0 lpay_select_group -->
						</s:if>
						<s:elseif test="%{#session.USER.UserType.name()=='MERCHANT'}">
							<div class="form-group mb-0 d-none lpay_select_group w-100 w-md-200">
								<label>Select Merchant</label>
								<s:select
									name="merchants"
									class="form-control selectpicker"
									id="merchant" 
									data-submerchant="subMerchant"
									data-user="subUser"
									listKey="emailId"
									listValue="businessName"
									list="merchantList"
									autocomplete="off"
								/>
							</div>
							<!-- /.form-group mb-0 lpay_select_group -->
						</s:elseif>
	
						<s:if test="%{superMerchant == true}">
							<div class="form-group mb-0 lpay_select_group w-100 w-md-200" data-id="submerchant">
								<label>Sub Merchant</label>
								<s:select
									name="subMerchant"
									class="form-control selectpicker"
									id="subMerchant" 
									listKey="payId"
									headerKey=""
									headerValue="ALL"
									listValue="businessName"
									list="subMerchantList"
									autocomplete="off"
								/>
							</div>
						</s:if>
						<s:else>
							<div class="form-group mb-0 lpay_select_group w-100 w-md-200 d-none" data-target="subMerchant">
								<label>Sub Merchant</label>
								<select name="subMerchant" id="subMerchant"></select>
							</div>
							<!-- /.lpay_select_group -->					
						</s:else>
						
						<div class="form-group mb-0 lpay_select_group w-100 w-md-80">
							<label>Currency</label>
							<s:select name="currency" id="currency" list="currencyMap" headerKey="ALL" headerValue="ALL" class="form-control selectpicker" onchange="handleChange();"/>
						</div>
						<!-- /.form-group mb-0 lpay_select_group -->
					
						<div class="form-group mb-0 lpay_input_group w-100 w-md-150">
							<label>Date From <span class="color-red">*</span></label>
							<s:textfield
								type="text"
								id="dateFrom"
								name="dateFrom"
								class="lpay_input date-input"
								autocomplete="off"
								readonly="true"
							/>
						</div>
						<!-- /.lpay_select_group -->
					
						<div class="form-group mb-0 lpay_input_group w-100 w-md-150">
							<label>Date To <span class="color-red">*</span></label>
							<s:textfield
								type="text"
								id="dateTo"
								name="dateTo"
								class="lpay_input date-input"
								autocomplete="off"
								readonly="true"
							/>
						</div>
						<!-- /.lpay_select_group -->
					
						<div class="form-group mb-0 lpay_select_group w-100 w-md-150">
							<label>TXN Region <span class="color-red">*</span></label>
							<s:select
								headerKey="ALL"
								headerValue="ALL"
								class="selectpicker"
								list="#{'INTERNATIONAL':'International','DOMESTIC':'Domestic'}"
								name="paymentsRegion"
								id="paymentsRegion"
							/>
						</div>
						<!-- /.lpay_select_group -->
					
						<button class="lpay_button lpay_button-md lpay_button-secondary px-13 mt-15 mx-0" id="btn-getData">Submit</button>
					</div>
				</div>

				<div class="row mt-20">
					<div class="col-md-12 text-center">
						<div class="newteds mt-10 d-flex flex-wrap justify-content-center">
							<button type="button" id="buttonDay" name="day" data-lastday="1" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary btnActive">Day</button>
							<button type="button" id="buttonWeekly" name="week" data-lastday="7" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Week</button>
							<button type="button" id="buttonMonthly" name="month" data-lastday="31" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Month</button>
							<button type="button" id="buttonYearly" name="year" data-lastday="365" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Year</button>
							<!-- <button type="button" id="buttonLastMonth" name="lastMonth" data-lastday="31" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Last Month</button> -->
						</div>
					</div>
				</div>
			</div>
			<!-- /.dashboard-container -->
		</div>

    	<div class="dashboard_div lpay_section white-bg box-shadow-box mt-70 p-15 p-md-20 dashboard-container">
			<div class="row">
				<div class="col-md-6">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text" id="status-text">Sale</h2>
					</div>
				</div>
			</div>
			<!-- /.row -->
			
			<div class="dashboard-container">
				<div class="row d-flex flex-wrap d-md-block">
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box green"><i class="fa fa-thumbs-up"></i></span>
							<h2 class="heading_text"> <span id="dvTotalGross" data-name="grossSuccess"><s:property value="%{statistics.totalGrossSuccess}"/></span>Gross Captured</h2>
						</div>
					</div>
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box green"><i class="fa fa-thumbs-up"></i></span>
							<h2 class="heading_text"> <span id="dvTotalSuccess" data-name="Success"><s:property value="%{statistics.totalSuccess}"/></span>Net Captured</h2>
						</div>
					</div>
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box blue"><i class="fa fa-check"></i></span>
							<h2 class="heading_text"> <span id="dvGrossApprovedAmount"><s:property value="%{statistics.grossApprovedAmount}"/></span>Gross Amount</h2>
						</div>
					</div>
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box blue"><i class="fa fa-check"></i></span>
							<h2 class="heading_text"> <span id="dvApprovedAmount"><s:property value="%{statistics.approvedAmount}"/></span>Net Amount</h2>
						</div>
					</div>
					
					
					

					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box"><i class="fa fa-reply-all"></i></span>
							<h2 class="heading_text"> <span id="dvTotalRefunded" data-name="Refunded"><s:property value="%{statistics.totalRefunded}"/></span> Total Refunded</h2>
						</div>
					</div>
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box brown"><i class="fa fa-reply"></i></span>
							<h2 class="heading_text"> <span id="dvRefundedAmount"><s:property value="%{statistics.refundedAmount}"/></span> Refunded Amt</h2>
						</div>
					</div> 
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box red"><i class="fa fa-times-circle"></i></span>
							<h2 class="heading_text"> <span id="dvTotalRejected" data-name="Rejected"><s:property value="%{statistics.totalRejectedDeclined}"/></span> Total Rejected</h2>
						</div>
					</div> 
	
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box"><i class="fa fa-line-chart"></i></span>
							<h2 class="heading_text"> <span id="dvTotalDropped" data-name="Dropped"><s:property value="%{statistics.totalDropped}"/></span> Total Dropped</h2>
						</div>
					</div>
	
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box"><i class="fa fa-line-chart"></i></span>
							<h2 class="heading_text"> <span id="dvTotalTimeout" data-name="Dropped"><s:property value="%{statistics.totalDropped}"/></span> Total Timeout</h2>
						</div>
					</div>
							
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box red"><i class="fa fa-thumbs-down"></i></span>
							<h2 class="heading_text"> <span id="dvTotalFailed" data-name="Failed"><s:property value="%{statistics.totalFailed}"/></span> Total Failed</h2>
						</div>
					</div>					
		
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box"><i class="fa fa-times"></i></span>
							<h2 class="heading_text"> <span id="dvTotalCancelled" data-name="Cancelled"><s:property value="%{statistics.totalCancelled}"/></span>User Cancelled</h2>
						</div>
					</div>
				
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box brown"><i class="fa fa-share-square-o"></i></span>
							<h2 class="heading_text"> <span id="dvTotalFraud" data-name="Fraud"><s:property value="%{statistics.totalFraud}"/></span> Total Fraud</h2>
						</div>
					</div>
		
					<div class="col-md-5ths statistics-box">
						<div class="heading_with_icon mb-10">
							<span class="heading_icon_box"><i class="fa fa-share"></i></span>
							<h2 class="heading_text"> <span id="dvTotalInvalid" data-name="Invalid"><s:property value="%{statistics.totalInvalid}"/></span> Total Invalid</h2>
						</div>
					</div> 
				</div>
				<!-- /.row mt-10 -->
			</div>
			<!-- /.dashboard-container -->
			
			<div class="dashboard-container mt-30 pt-30 border-top-grey-lighter">				
				<div class="row">
					<div class="col-md-6">
						<div class="heading_with_icon mb-30">						
							<!-- <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span> -->
							<h2 class="heading_text dashbaord_heading ml-0"><span id="transactionTime"></span> Transaction</h2>
						</div>
					</div> 
					<div class="col-md-6 d-flex justify-content-end">
						<!-- <div class="lpay_select_group w-100 mwp-100 mr-10">
							<select id="highchart-switch" class="selectpicker">
								<option value="volume">Volume</option>
								<option value="amount">Amount</option>
							</select>
						</div> -->
						<div class="lpay_select_group w-100 mwp-100">
							<select id="highchart-select" class="selectpicker">
								<option value="lineChart">Line Chart</option>
								<option value="histogram">Histogram</option>
							</select>
						</div>
					</div>
				</div>
	
				<div class="row">
					<div class="col-12">
						<div class="dashboard_graph">
							<div id="container" style="min-width: 270px; height: 300px; margin: 0 auto"></div>
						</div>
					</div>
				</div>
			</div>

			<div class="dashboard-container mt-30 pt-30 border-top-grey-lighter">
				<div class="row pt-20 pieChart">
					<div class="col-xs-12">
						<div class="heading_with_icon mb-30">
							<h2 class="heading_text dashbaord_heading ml-0">Total Transaction Volume</h2>
						</div>
					</div>
					<div class="col-xs-12">
						<div class="row">
							<div class="col-md-4">
								<div class="lpay_select_group d-flex flex-column">
									<label class="mb-10">Volume / Amount</label>
									<div class="d-flex">
										<label class="lpay_toggle lpay_toggle_on mt-2">
											<input type="checkbox" name="txnUnit" id="txnUnit" data-toggle="toggle" checked="true" />
										</label>
										<span class="d-flex align-items-center ml-15 txn-unit">Amount</span>
									</div>
								</div>
							</div>
						</div>

						<div class="row">
							<div class="col-md-6">
								<div id="txn-sale-chart"></div>
							</div>

							<div class="col-md-6">
								<table class="table mytable mb-0 table-hover lpay_table" id="sale-table">
									<thead class="lpay_table_head">
										<tr>
											<th>Payment Type</th>
											<th class="heading-value">Amount</th>
											<th>Percentage</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td>Credit Card</td>
											<td><p data-id="CreditCardValue" class="media-heading"></p></td>
											<td><p data-id="CreditCardPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>Debit Card</td>
											<td><p data-id="DebitCardValue" class="media-heading"></p></td>
											<td><p data-id="DebitCardPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>International</td>
											<td><p data-id="InternationalValue" class="media-heading"></p></td>
											<td><p data-id="InternationalPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>UPI</td>
											<td><p data-id="UPIValue" class="media-heading"></p></td>
											<td><p data-id="UPIPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>Net Banking</td>
											<td><p data-id="NetBankingValue" class="media-heading"></p></td>
											<td><p data-id="NetBankingPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>Wallet</td>
											<td><p data-id="WalletValue" class="media-heading"></p></td>
											<td><p data-id="WalletPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>EMI</td>
											<td><p data-id="EMIValue" class="media-heading"></p></td>
											<td><p data-id="EMIPercentage" class="media-heading"></p></td>
										</tr>
										<tr>
											<td>Cash on Delivery</td>
											<td><p data-id="CashOnDeliveryValue" class="media-heading"></p></td>
											<td><p data-id="CashOnDeliveryPercentage" class="media-heading"></p></td>
										</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!-- /.dashboard-container -->
			
			<s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN' || #session.USER.UserType.name() == 'RESELLER' || #session['USER'].superMerchant == true}">
				<div class="dashboard-container mt-10 pt-30 border-top-grey-lighter">
					<div class="row d-flex flex-wrap">
						<div class="col-md-6">
							<div class="row">
								<div class="col-xs-12">
									<div class="heading_with_icon mb-30">						
										<!-- <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span> -->
										<h2 class="heading_text dashbaord_heading ml-0">Highest Transacting Merchant</h2>
									</div>
								</div>
								<div class="col-xs-12">
									<div class="row">
										<!-- <div class="col-md-3 mb-10 mb-md-0">
											<div class="lpay_input_group">
												<label>Date From <span class="color-red">*</span></label>
												<s:textfield
													type="text"
													id="dateFromHighest"
													name="dateFrom"
													class="lpay_input date-input"
													autocomplete="off"
													readonly="true"
												/>
											</div>
										</div>
										
										<div class="col-md-3 mb-10 mb-md-0">
											<div class="lpay_input_group">
												<label>Date To <span class="color-red">*</span></label>
												<s:textfield
													type="text"
													id="dateToHighest"
													name="dateTo"
													class="lpay_input date-input"
													autocomplete="off"
													readonly="true"
												/>
											</div>
										</div> -->
	
										<div class="col-md-4">
											<div class="lpay_select_group d-flex flex-column">
												<label class="mb-10">Volume / Amount</label>
												<div class="d-flex">
													<label class="lpay_toggle lpay_toggle_on mt-2">
														<input type="checkbox" id="txnUnitHighest" data-toggle="toggle" checked="true" />
													</label>
													<span class="d-flex align-items-center ml-15 txn-unit">Amount</span>
												</div>
											</div>
										</div>
									</div>
		
									<div class="row mt-20" id="highest-merchant">
										<div class="col-xs-12 txn-merchant d-none">
											<div class="col-md-12 p-0" data-hieghest="amount">
												<div class="lpay_table">
													<table id="lowestMerchantTable_amount" class="table mytable mb-0 table-hover lpay_table" cellspacing="0" width="100%">
														<thead class="lpay_table_head">
															<tr>
																<th>Merchant</th>
																<th>Sub-Merchant</th>
																<th>Amount</th>
															</tr>
														</thead>	
														<tbody class="lowestMerchantBody_amount">
														</tbody>
													</table>
												</div>
												<!-- /.lpay_table -->
											</div>
											<!-- /.col-md-12 -->
											<!-- /#amountDiv -->
											<div class="col-md-12 p-0" data-hieghest="volume">
												<div class="lpay_table">
													<table id="lowestMerchantTable_volume" class="table mytable mb-0 table-hover lpay_table" cellspacing="0" width="100%">
														<thead class="lpay_table_head">
															<tr>
																<th>Merchant</th>
																<th>Sub-Merchant</th>
																<th>Volume</th>
															</tr>
														</thead>	
														<tbody class="lowestMerchantBody_volume">

														</tbody>
													</table>
												</div>
												<!-- /.lpay_table -->
											</div>
											<!-- /.col-md-12 -->
											
										</div>
		
										<div class="col-xs-12 data-unavailable d-none">
											<h3 class="font-weight-medium empty-data">No data found</h3>
										</div>
									</div>
								</div>
							</div>
						</div>
			
						<div class="col-md-6">
							<div class="row">
								<div class="col-xs-12">
									<div class="heading_with_icon mb-30">						
										<!-- <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span> -->
										<h2 class="heading_text dashbaord_heading ml-0">Lowest Transacting Merchant</h2>
									</div>
								</div>
								<div class="col-xs-12">
									<div class="row">
										<!-- <div class="col-md-3 mb-10 mb-md-0">
											<div class="lpay_input_group">
												<label>Date From <span class="color-red">*</span></label>
												<s:textfield
													type="text"
													id="dateFromLowest"
													name="dateFrom"
													class="lpay_input date-input"
													autocomplete="off"
													readonly="true"
												/>
											</div>
										</div>
										
										<div class="col-md-3 mb-10 mb-md-0">
											<div class="lpay_input_group">
												<label>Date To <span class="color-red">*</span></label>
												<s:textfield
													type="text"
													id="dateToLowest"
													name="dateTo"
													class="lpay_input date-input"
													autocomplete="off"
													readonly="true"
												/>
											</div>											
										</div> -->
	
										<div class="col-md-4">
											<div class="lpay_select_group d-flex flex-column">
												<label class="mb-10">Volume / Amount</label>
												<div class="d-flex">
													<label class="lpay_toggle lpay_toggle_on mt-2">
														<input type="checkbox" id="txnUnitLowest" data-toggle="toggle" checked="true" />
													</label>
													<span class="d-flex align-items-center ml-15 txn-unit">Amount</span>
												</div>
											</div>
										</div>
		
										<!-- <div class="col-md-2 pl-md-0 text-center text-md-left">
											<button class="lpay_button lpay_button-md lpay_button-secondary px-13 mt-15 mx-0" id="btn-merchantLowest">Submit</button>
										</div> -->
									</div>
		
									<div class="row mt-20" id="lowest-merchant">
										<div class="col-xs-12 txn-merchant d-none">
											<div class="col-md-12 p-0" data-lowest="amount">
												<div class="lpay_table">
													<table id="lowestMerchantTable_amount" class="table mytable mb-0 table-hover lpay_table" cellspacing="0" width="100%">
														<thead class="lpay_table_head">
															<tr>
																<th>Merchant</th>
																<th>Sub-Merchant</th>
																<th>Amount</th>
															</tr>
														</thead>	
														<tbody class="lowestMerchantBody_amount">

														</tbody>
													</table>
												</div>
												<!-- /.lpay_table -->
											</div>
											<!-- /.col-md-12 -->
											<!-- /#amountDiv -->
											<div class="col-md-12 p-0" data-lowest="volume">
												<div class="lpay_table">
													<table id="lowestMerchantTable_volume" class="table mytable mb-0 table-hover lpay_table" cellspacing="0" width="100%">
														<thead class="lpay_table_head">
															<tr>
																<th>Merchant</th>
																<th>Sub-Merchant</th>
																<th>Volume</th>
															</tr>
														</thead>	
														<tbody class="lowestMerchantBody_volume">

														</tbody>
													</table>
												</div>
												<!-- /.lpay_table -->
											</div>
											<!-- /.col-md-12 -->
											
										</div>
		
										<div class="col-xs-12 data-unavailable d-none">
											<h3 class="font-weight-medium empty-data">No data found</h3>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
					<!-- rowend -->
				</div>
				<!-- /.dashboard-container -->	
			</s:if>
		</div>
		<!-- /.dashboard_div lpay_section white-bg box-shadow-box mt-70 p20 -->
	 </div>

	<s:textfield type="hidden" value="%{#session.USER.UserType.name()}" id="USER_TYPE" />
	<s:textfield type="hidden" value="%{#session.USER.emailId}" id="USER_EMAIL" />

	<s:form method="POST" action="downloadTransaction" id="downloadForm">
	 	
	</s:form> 

	<s:form id="payoutDownloadForm" method="POST" action="downloadPayOutDashboardTxn">
		<s:hidden id="transactionType" name="transactionType" />
	</s:form>
	 
	 <script src="../js/dashboard-script.js"></script>
</body>
</html>