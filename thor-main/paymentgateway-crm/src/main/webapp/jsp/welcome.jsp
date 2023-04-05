<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Dashboard</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/welcomePage.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/dashboard-style.css">
	<style>
		.select2-container--default { display:none; }		
	</style>
</head>
<body style="margin:0px; padding:0px;">
	<div id="page-inner">
		<section class="dashboard_div lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-6">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Dashboard</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->

				<div class="col-md-6">
					<div class="row d-flex justify-content-end">
						<div class="col-md-6 col-xs-6">
							<div class="form-group lpay_select_group">
								<s:if test="%{#session.USER.UserType.name()=='MERCHANT' && #session.USER.isSuperMerchant()== false && #session.USER.getSuperMerchantId() != null}">
								<s:select
									name="merchants"
									class="form-control selectpicker"
									id="merchant" 
									listKey="emailId" listValue="businessName"
									list="merchantList" autocomplete="off" onchange="handleChange();"/>
								</s:if>
								<s:else>
									<s:if test="%{#session.USER.UserType.name()=='MERCHANT' && #session.USER.isSuperMerchant()== false}">
										<s:select
										name="merchants"
										class="form-control selectpicker"
										id="merchant" 
										listKey="emailId" listValue="businessName"
										list="merchantList" autocomplete="off" onchange="handleChange();"/>
									</s:if>
									<s:else>
										<s:select
										name="merchants"
										class="form-control selectpicker"
										id="merchant" 
										listKey="emailId" listValue="businessName"
										list="merchantList" autocomplete="off" onchange="handleChange();"
										headerKey="ALL MERCHANTS" headerValue="ALL SUB MERCHANTS"/>
									</s:else>
								</s:else>
							</div>
							<!-- /.form-group lpay_select_group -->
						</div>
						<!-- .col-md-3 -->
						<div class="col-md-3 col-xs-6">
							<div class="form-group lpay_select_group">
								<s:select name="currency" id="currency" list="currencyMap" class="form-control selectpicker" onchange="handleChange();"/>
							</div>
							<!-- /.form-group lpay_select_group -->
						</div> 
						<!-- col-md-3 -->
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-md-5">
					<div class="newteds mt-25 column-seperator">
						<button type="button" id="buttonDay" name="day" data-lastday="1" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary btnActive">Day</button>
						<button type="button" id="buttonWeekly" name="week" data-lastday="7" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Week</button>
						<button type="button" id="buttonMonthly" name="month" data-lastday="31" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Month</button>
						<button type="button" id="buttonYearly" name="year" data-lastday="365" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Year</button>
						<!-- <button type="button" id="buttonLastMonth" name="lastMonth" data-lastday="31" data-startday="1" class="newround lpay_button lpay_button-sm lpay_button-secondary">Last Month</button> -->
					</div>
				</div>

				<div class="col-md-7">
					<div class="row">
						<div class="col-md-3">
							<div class="lpay_input_group">
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
						</div>
						<!-- /.col-md-3 -->
						<div class="col-md-3">
							<div class="lpay_input_group">
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
						</div>
						<!-- /.col-md-3 -->
						<div class="col-md-4">
							<div class="lpay_select_group">
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
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-2">
							<button class="lpay_button lpay_button-md lpay_button-secondary px-13 mt-25 mx-0" id="btn-getData">Submit</button>
						</div>
						<!-- /.col-md-2 -->
					</div>
				</div>
			</div>
			<!-- row -->
			<div class="row mt-30">	
				<div class="col-md-5ths">
					<div class="heading_with_icon mb-10">
						<span class="heading_icon_box green"><i class="fa fa-thumbs-up"></i></span>
						<h2 class="heading_text"> <span id="dvTotalSuccess"><s:property value="%{statistics.totalSuccess}"/></span> Total Success</h2>
					</div>
				</div>
	
				<div class="col-md-5ths">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box red"><i class="fa fa-thumbs-down"></i></span>
						<h2 class="heading_text"> <span id="dvTotalFailed"><s:property value="%{statistics.totalFailed}"/></span> Total Failed</h2>
					</div>
				</div>
	
				<div class="col-md-5ths">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box"><i class="fa fa-reply-all"></i></span>
						<h2 class="heading_text"> <span id="dvTotalRefunded"><s:property value="%{statistics.totalRefunded}"/></span> Total Refunded</h2>
					</div>
				</div>
	
				<div class="col-md-5ths">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box blue"><i class="fa fa-check"></i></span>
						<h2 class="heading_text"> <span id="dvApprovedAmount"><s:property value="%{statistics.approvedAmount}"/></span> Approved Amount</h2>
					</div>
				</div>
	
				<div class="col-md-5ths">
					<div class="heading_with_icon mb-20">
						<span class="heading_icon_box brown"><i class="fa fa-reply"></i></span>
						<h2 class="heading_text"> <span id="dvRefundedAmount"><s:property value="%{statistics.refundedAmount}"/></span> Refunded Amount</h2>
					</div>
				</div>	
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

		<div class="dashboard_div lpay_section white-bg box-shadow mt-70 p20">
			<div class="row">
				<div class="col-md-6">
					<div class="heading_with_icon mb-30">						
						<!-- <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span> -->
						<h2 class="heading_text ml-0"><span id="transactionTime"></span> Transaction</h2>
					</div>
				</div> 
				<div class="col-md-6 d-flex justify-content-end">
					<div class="lpay_select_group w-100 mw-200">
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
						<div id="container" style="min-width: 310px; height: 200px; margin: 0 auto"></div>
					</div>
				</div>
			</div>
		</div>
		<!-- /.dashboard_div lpay_section white-bg box-shadow mt-70 p20 -->
		<script src="../js/jquery.min.js"></script>
		<script src="../js/jquery-ui.js"></script>
		<script src="../js/bootstrap-select.min.js"></script>
		<script src="../js/highcharts.js"></script>
		<script src="../js/highchart.exporting.js"></script>
		<script src="../js/merchant-dashboard-script.js"></script>

</body>
</html>