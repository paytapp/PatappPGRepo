<%@page import="com.paymentgateway.commons.util.SaltFactory"%>
<%@page import="com.paymentgateway.commons.user.User"%>
<%@page import="com.paymentgateway.commons.util.Currency"%>
<%@page import="com.paymentgateway.commons.util.Amount"%>
<%@page import="com.paymentgateway.commons.util.FieldType"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="com.paymentgateway.commons.util.Constants"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>User Settings</title>
	<link rel="stylesheet" href="../css/jquery-ui.css">
	<script src="../js/continents.js" type="text/javascript"></script>
	<script src="../js/jquery.minshowpop.js"></script>
	<script src="../js/jquery.formshowpop.js"></script>
	<script src="../js/jquery.min.js"></script>
	<script src="../js/follw.js"></script>
	<script src="../js/jquery.popupoverlay.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/city_state.js"></script>
	<!--  loader scripts -->
	<script src="../js/loader/modernizr-2.6.2.min.js"></script>
	<script src="../js/loader/main.js"></script>
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/horizontal-scrolling-nav.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

	<style>

		.upload-file-download{
			cursor: pointer;
		}

		.hosted-url .lpay_input_group {
			margin-left: -20px;
			width: 100%;
			display: inline-block;
			opacity: 0;
			transition: all .5s ease;
			max-height: 0;
			overflow: hidden;
		}

		.hosted-url .active-hosted-input{
			margin-left: 0;
			opacity: 1;
			max-height: 70px;
		}

		.mt-5 { margin-top: 5px !important; }
		.d-inline-flex { display: inline-flex !important; }
		.flex-column { flex-direction: column !important; }
		.align-items-center { align-items: center !important; }
		.text-secondary { color: #3C4858 !important; }
		.font-weight-medium { font-weight: 600 !important; }

		.mpaDownload:focus{
			text-decoration: none;
			color: #fff;
		}

		.upload-pic {
			height: 43px;
			width: 100%;
			position: relative;
			cursor: pointer;
		}

		.upload-pic input {
			width: 100%;
			height: 100%;
			left: 0;
			position: absolute;
			opacity: 0;
		}

		.upload-pic img {
			width: 20px;
			position: absolute;
			top: 12px;
			z-index: 1;
			left: 11px;
		}

		.upload-pic:after {
			content: " ";
			min-width: 43px;
			height: 43px;
			border-radius: 21px;
			-webkit-border-radius: 21px;
			-moz-border-radius: 21px;
			-ms-border-radius: 21px;
			-o-border-radius: 21px;
			border: 1px solid rgba(204, 204, 204, .4);
			box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
			box-sizing: border-box;
			background-color: #fff;
			position: absolute;
			left: 0;
			top: 0;
		}

		.upload-success:after {
			border-color: #68B118;
		}

		.upload-denied:after {
			border-color: #E60000;
		}

		.upload-text {
			padding-left: 55px;
			margin-top: 2px;
			display: inline-block;
			word-break: break-all;
		}

		.upload-text .doc-text {
			display: block;
		}

		.upload-text .doc-file-name {
			display: block;
			margin-top: 4px;
		}

		.pointer-none {
			pointer-events: none;
		}

		.save-flag label{
			z-index: 9999;
		}

	</style>

	<script type="text/javascript">
		
	</script>

	<style>

		div#uploaded-file {
			display: flex;
			flex-wrap: wrap;
			margin-top: 16px;
			justify-content: space-between;
		}

		#fileUploader{
			width: 100%;
			height: 100%;
			z-index: 99;
		}

		.upload-div {
			width: 49%;
			display: inline-flex;
			justify-content: space-between;
			padding: 10px;
			background-color: #f5f5f5;
			border-radius: 4px;
			margin-bottom: 15px;
		}

		span.upload-file-name {
			font-weight: 600;
			text-transform: capitalize;
		}

		.button-wrapper{
			position: absolute;
			width: auto;
			display: inline-block;
			top: 9px;
			right: 0;
		}

		.button_wrapper a:hover{
			color: #fff;
		}

		.d-none {
			display: none !important;
		}

		.errorMessage {
			color: #ff0000;
			text-align: left;
			margin-top: -15px;
			margin-left: 2px;
		}

		.var-failed {
			color: #ff0000;
			font-weight: 400;
		}

		div#saveMessage {
			text-align: center;
			background-color: #47dd4b73;
			border-radius: 5px;
		}

		div#saveMessage span {
			text-align: center;
		}

		input[type="radio"] {
			margin-left: 10px !important;
		}

		#radiodiv span {
			font-size: 14px;
			margin-left: 4px;
		}

		.textFL_merch:hover {
			color: #000 !important;
			background: #fff !important;
		}

		.lpay_select_group.save-flag-select {
			text-align: right;
			margin-top: -5px;
		}

		.textFL_merch5:hover {
			color: #000 !important;
			background: #fff !important;
		}

		.btn {
			font-size: 10pt !important;
		}

		.success-text { /* color:#ff0000!important; */
			margin: 4px auto 15px !important;
		}

		.tranjuctionSet {
			width: 98%;
			float: left;
			font: bold 12px arial;
			color: #666;
			margin: -10px 0px 10px 10px;
			padding-top: 4px;
			line-height: 22px;
			background: #fff;
			border: 1px solid #e4e4e4;
			height: 65px;
		}

		.tranjuctionCon {
			width: 98% !important;
		}

		.trans-top {
			margin-top: 10px;
		}

		.trans-btm {
			margin-bottom: 10px;
		}

		.rkb-mg {
			margin: 0px !important;
		}

		#radioError {
			color: #ff0000 !important;
			margin-left: 13px;
			font-size: 11px;
		}

		.uploaded-logo img {
			height: 50px;
		}

		.MerchBx {
			margin-top: 15px !important;
			margin-bottom: -15px !important;
		}

		button, input {
			border: none;
		}

		.margin-email {
			margin-top: 11px !important;
		}

		.error-text {
			color: #a94442;
			background-color: #f2dede;
			list-style-type: none;
			text-align: center;
			list-style-type: none;
			margin-bottom: 15px;
			border-radius: 5px;
			text-align: center;
			pointer-events: none;
		}

		.error-text span {
			text-align: center;
		}

		.error-text li {
			list-style-type: none;
		}

		#response {
			color: green;
		}

		.actionMessage li {
			padding: 0px !important;
		}

		.selctInpt:focus {
			background-color: #FFF;
			outline: 0;
			border: none;
			box-shadow: none;
		}

		.FlowupLabels .fl_input {
			top: 10px !important;
			bottom: 5px !important;
			color: #000 !important;
		}

		.FlowupLabels .fl_wrap {
			color: #000 !important;
		}

		.btn:focus {
			outline: 0 !important;
		}

		#wwerr_resellerId {
			margin-left: 20% !important;
		}

		#wwerr_ifscCode {
			margin-left: 20% !important;
		}

		#wwerr_currency {
			margin-left: 22% !important;
		}

		#wwerr_transactionEmailId {
			margin-top: 3%;
			margin-left: -1%;
		}

		.d-flex {
			display: flex;
		}

		.flex-wrap {
			flex-wrap: wrap;
		}

		.full-width {
			width: 100% !important;
		}

		.float-none {
			float: none !important;
		}

		.mt-20 {
			margin-top: 20px !important;
		}

		.allowed-boxes .tranjuctionCon4 {
			width: 25% !important;
			display: flex;
		}

		.subtract-value {
			position: absolute;
			left: 0;
			top: -25px;
			width: 100% !important;
			max-width: 200px;
		}

		.partSettle {
			position: relative;
		}

		.subTract #wwgrp_deviation {
			position: relative;
		}

		.subTract #deviation {
			text-indent: 5px;
		}

		.subTract #wwgrp_deviation::before {
			content: "-";
			position: absolute;
			top: 8px;
			left: 0;
			font-size: 14px;
		}

		.mpaDownload {
			display: inline-block !important;
			margin-left: 0 !important;
			margin-right: 15px !important;
		}

		.upload-name {
			display: none;
		}

		.upload-size-error {
			display: none;
		}

		[data-file="success"] .upload-text {
			display: none;
		}

		[data-file="success"] .upload-name {
			display: block;
			padding-left: 55px;
		}

		[data-file="size-error"] .upload-size-error {
			display: block;
			padding-left: 55px;
			color: #E60000
		}

		[data-file="size-error"] .upload-text {
			display: none;
		}

		span.error-msg {
			position: absolute;
			bottom: -16px;
			color: #f00;
			font-size: 10px;
			transition: all .5s ease;
		}

		.default-upload span {
			display: block;
			text-align: center;
			font-size: 14px;
			margin-top: 10px;
		}

		[readonly] .lpay_select_group{
			pointer-events: none;
		}

		span.heading-ribbon {
			display: inline-block;
			padding: 10px 30px;
			background-color: #0096d7;
			color: #fff;
			margin-left: -12px;
			border-radius: 4px;
			position: relative;
			z-index: 1;
			margin-bottom: 11px;
			margin-top: -11px;
		}
		span.heading-ribbon h4 {
			color: #fff;
			font-size: 14px;
		}

		.payout_mapping-data .unchecked input {
			opacity: 0;
			position: absolute;
			width: 100%;
			left: 0;
			cursor: pointer;
		}

		.flag-wrapper_button button{ padding: 7px 15px !important; }
		.payout_mapping-list{ padding: 10px 10px;background-color: #f5f5f5;border-radius: 5px;font-size: 14px;margin-bottom: 20px; }

	</style>
</head>
<body>

	<s:hidden id="activeFlag" value="%{parentMerchantFlag}"></s:hidden>

	<s:hidden value="%{userSetting.merchantLogo}" data-id="merchantLogo"></s:hidden>
	<section class="merchant-account lapy_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-4 p-0">
			<span class="heading-ribbon">
				<h4><s:property value="%{userSetting.businessName}"/></h4>
			</span>
		</div>
		<!-- /.col-md-3 -->
	</div>
	<!-- /.row -->
	<div class="row">
		<div class="col-md-12 mb-20">
			<ul class="lpay_tabs d-flex">
				<li class="lpay-nav-item active" onclick="tabShow(this, 'flagSettings')">
					<a href="#" class="lpay-nav-link" data-id="flagSettings">Flag User Settings</a>
				</li>
				<s:if test="%{parentMerchantFlag == true}">
					<li class="lpay-nav-item" onclick="tabShow(this, 'parentMapping')">
						<a href="#" class="lpay-nav-link" data-id="parentMapping">Parent Mapping</a>
					</li>
				</s:if>
			</ul>
			<!-- /.lpay_tabs -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-12">
			<div class="lpay_tabs_content w-100" data-target="flagSettings">
				<s:form action="saveUserSetting" method="post"
					autocomplete="off" enctype="multipart/form-data" class="FlowupLabels"
					id="merchantForm" theme="css_xhtml">
					<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
					<s:if test="%{responseObject.responseCode=='101'}">
						<div id="saveMessage">
							<s:actionmessage class="success success-text" />
						</div>
					</s:if>
					<s:else>
						<div class="error-text">
							<s:actionmessage theme="simple" />
						</div>
					</s:else>
					
					<div class="merchant__forms_block active-block" data-active="systemSetting">
						<s:hidden value="%{payId}" name="payId" data-id="payId"></s:hidden>
						<div class="row">
							<div class="col-md-4 mb-20">
								<div class="lpay_select_group">
									<label for="">Default Reporting Currency</label>
									<s:select name="defaultCurrency" id="defaultCurrency"
										list="currencyMap" cssClass="selectpicker" />
								</div>
								<!-- /.lpay_select_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_select_group">
									<label for="">Payment Message Slab</label>
									<s:select name="paymentMessageSlab" id="paymentMessageSlab"
										headerKey="1"
										list="#{'0':'0','1':'1','2':'2','3':'3','4':'4','5':'5'}"
										class="selectpicker" value="%{userSetting.paymentMessageSlab}">
									</s:select>
								</div>
								<!-- /.lpay_select_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							
		
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Flag Configuration</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3 mb-20">
								<label for="merchantHostedFlag" class="checkbox-label unchecked">
		
									Merchant Hosted Page <s:checkbox name="merchantHostedFlag"
										id="merchantHostedFlag" value="%{userSetting.merchantHostedFlag}" />
								</label>
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<label for="iframePaymentFlag" class="checkbox-label unchecked">
									iframe Payment <s:checkbox name="iframePaymentFlag"
										id="iframePaymentFlag" value="%{userSetting.iframePaymentFlag}" />
								</label>
							</div>
							<div class="col-md-3 mb-20">
								<label for="checkOutJsFlag" class="checkbox-label unchecked">
									Checkout JS <s:checkbox name="checkOutJsFlag"
										id="checkOutJsFlag" value="%{userSetting.checkOutJsFlag}" />
								</label>
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<label for="surchargeFlag" class="checkbox-label unchecked">
									Surcharge <s:checkbox name="surchargeFlag"
										value="%{userSetting.surchargeFlag}" id="surchargeFlag" />
								</label>
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<label for="discountFlag" class="checkbox-label unchecked">
									Discount <s:checkbox name="discountingFlag" id="discountFlag"
										value="%{userSetting.discountingFlag}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for=loadWalletFlag class="checkbox-label unchecked">
									Load Wallet <s:checkbox name="loadWalletFlag"
										id="loadWalletFlag" value="%{userSetting.loadWalletFlag}" />
								</label>
							</div>
							<div class="col-md-3 mb-20">
								<label for="eposMerchant" class="checkbox-label unchecked"> ePOS Merchant 
									<s:checkbox name="eposMerchant" id="eposMerchant" class="textFL_merch5"
											value="%{userSetting.eposMerchant}">
									</s:checkbox>
								</label>
							</div>
							
							<!-- /.col-md-2 -->
		
							<div class="col-md-3 mb-20">
								<label for="bookingRecord" class="checkbox-label unchecked">
									Booking Report <s:checkbox name="bookingRecord"
										id="bookingRecord" value="%{userSetting.bookingRecord}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for=eNachReportFlag class="checkbox-label unchecked">
									eNACH Report <s:checkbox name="eNachReportFlag"
										id="eNachReportFlag" value="%{userSetting.eNachReportFlag}" />
								</label>
							</div>
							
							 <div class="col-md-3 mb-20">
								<label for=upiAutoPayReportFlag class="checkbox-label unchecked">
									UPI AutoPay Report <s:checkbox name="upiAutoPayReportFlag"
										id="upiAutoPayReportFlag" value="%{userSetting.upiAutoPayReportFlag}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for=acceptPostSettledInEnquiry class="checkbox-label unchecked">
									Custom Status Enquiry Flag <s:checkbox name="acceptPostSettledInEnquiry"
										id="acceptPostSettledInEnquiry" value="%{userSetting.acceptPostSettledInEnquiry}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for="customTransactionStatus" class="checkbox-label unchecked">
									Custom Transaction Status <s:checkbox name="customTransactionStatus"
										id="customTransactionStatus" value="%{userSetting.customTransactionStatus}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for="capturedMerchantFlag" class="checkbox-label unchecked">
									Custom Capture Report 
									<s:if test="%{userSetting.capturedMerchantFlag == true}">
										<s:checkbox 
										name="capturedMerchantFlag"
										checked="true"
										id="capturedMerchantFlag" 
										value="%{userSetting.capturedMerchantFlag}" />
									</s:if>
									<s:else>
										<s:checkbox
											name="capturedMerchantFlag"
											id="capturedMerchantFlag"
											value="%{userSetting.capturedMerchantFlag}"
										/>
									</s:else>
								</label>
							</div>					
							
							
							<div class="col-md-3 mb-20">
								<label for="paymentAdviceFlag" class="checkbox-label unchecked">
									Payment Advice Email <s:checkbox name="paymentAdviceFlag"
										id="paymentAdviceFlag" value="%{userSetting.paymentAdviceFlag}" />
								</label>
							</div>
		
							<div class="col-md-3 mb-20">
								<label for="retailMerchantFlag" class="checkbox-label unchecked">
									Marketplace Settlement Report
									<s:if test="%{userSetting.retailMerchantFlag == true}">
										<s:checkbox 
										name="retailMerchantFlag"
										checked="true"
										id="retailMerchantFlag" 
										value="%{userSetting.retailMerchantFlag}" />
									</s:if>
									<s:else>
										<s:checkbox
											name="retailMerchantFlag"
											id="retailMerchantFlag"
											value="%{userSetting.retailMerchantFlag}"
										/>
									</s:else>
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for="lyraPay" class="checkbox-label unchecked">
									Lyra Pay <s:checkbox name="lyraPay"
										id="lyraPay" value="%{userSetting.lyraPay}" />
								</label>
							</div>
							
							 <div class="col-md-3 mb-20">
								<label for="non3dsTxn" class="checkbox-label unchecked">
									Non 3DS Txn <s:checkbox name="non3dsTxn"
										id="non3dsTxn" value="%{userSetting.non3dsTxn}" />
								</label>
							</div>
		
							<div class="col-md-3 mb-20">
								<label for="upiHostedFlag" class="checkbox-label unchecked">
									Upi Hosted Flag <s:checkbox name="upiHostedFlag"
										id="upiHostedFlag" value="%{userSetting.upiHostedFlag}" />
								</label>
							</div>
							
							<div class="clearfix"></div>
							<!-- /.clearfix -->
							<div class="col-md-3 mb-20">
								<label for="retryTransactionFlag" class="checkbox-label unchecked">
									Retry Transaction <s:checkbox name="retryTransactionCustomeFlag"
										id="retryTransactionFlag"
										value="%{userSetting.retryTransactionCustomeFlag}" />
								</label>
								<div class="lpay_select_group">
									<label for="">Number of Retry</label>
									<s:select name="attemptTrasacation" id="attemptTrasacation"
										headerKey="1" list="#{'1':'1','2':'2','3':'3','4':'4','5':'5'}"
										class="selectpicker" value="%{userSetting.attemptTrasacation}"></s:select>
								</div>
								<!-- /.lpay_select_group -->
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<label for="non3dsTxn" class="checkbox-label unchecked">
									Auto Refund(Post Settled Txn.) <s:checkbox name="autoRefund"
										id="autoRefund" value="%{userSetting.autoRefund}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for="whiteListReturnUrlFlag" class="checkbox-label unchecked">
									White List Return URL	 <s:checkbox name="whiteListReturnUrlFlag"
										id="whiteListReturnUrlFlag"
										value="%{userSetting.whiteListReturnUrlFlag}" />
								</label>
								<div class="lpay_input_group d-none">
									<s:textfield
										type="text"
										data-reg="^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$"
										id="whiteListReturnUrl"
										name="whiteListReturnUrl"
										value="%{userSetting.whiteListReturnUrl}"
										autocomplete="off"
										oninput="checkLength(this, 'Invalid URL')"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_select_group -->
							</div>
							<!-- /.col-md-3 -->
							<!-- /.col-md-3 -->
							
							
							<div class="col-md-3 mb-20">
								<label for="configurableFlag" class="checkbox-label unchecked">
									Configurable Flag <s:checkbox name="configurableFlag"
										id="configurableFlag"
										value="%{userSetting.configurableFlag}" />
								</label>
								<div class="lpay_input_group d-none">
									<s:textfield
										type="text"
										data-reg="[0-9]"
										id="configurableTime"
										name="configurableTime"
										value="%{userSetting.configurableTime}"
										autocomplete="off"
										oninput="checkLength(this, 'Invalid Configurable Time')"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_select_group -->
							</div>
		
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Vault Settings</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3 mb-20 save-flag">
								<label for="saveVpaFlag" class="checkbox-label unchecked mb-10">
									Save VPA <s:checkbox name="saveVPAFlag" id="saveVpaFlag"
										value="%{userSetting.saveVPAFlag}" />
								</label>
								<div class="lpay_select_group save-flag-select">
									<label for="" class="d-none"> VPA Save Param</label>
									<s:select data-link="saveVPAFlag" disabled="true" name="vpaSaveParam" id="saveVpaParam"
										list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
										class="selectpicker" value="%{userSetting.vpaSaveParam}">
									</s:select>
								</div>
								<!-- /.lpay_select_group -->
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20 save-flag">
								<label for="saveNBFlag" class="checkbox-label unchecked mb-10">
									<s:checkbox name="saveNBFlag" id="saveNBFlag"
										value="%{userSetting.saveNBFlag}" /> Save NB Bank <span
									class="merchant__check"></span>
								</label>
								<div class="lpay_select_group save-flag-select">
									<label for="nbSaveParam" class="d-none">NB Save Param</label>
									<s:select name="nbSaveParam" disabled="true" data-link="saveNBFlag" id="nbSaveParam" headerKey="1"
										list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
										style="height:35px; margin-left: -35%;" class="selectpicker"
										value="%{userSetting.nbSaveParam}">
									</s:select>
								</div>
								<!-- /.merchant__form_group -->
							</div>
							<!-- /.col-md-3 mb-20 -->
							<div class="col-md-3 mb-20 save-flag">
								<label for="saveWLFlag" class="checkbox-label unchecked mb-10">
									<s:checkbox name="saveWLFlag" id="saveWLFlag"
										value="%{userSetting.saveWLFlag}" /> Save wallet <span
									class="merchant__check"></span>
								</label>
								<div class="lpay_select_group save-flag-select">
									<label for="wlSaveParam" class="d-none">WL Save Param</label>
									<s:select name="wlSaveParam" disabled="true" data-link="saveWLFlag" id="wlSaveParam" headerKey="1"
										list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
										style="height:35px; margin-left: -35%;" class="selectpicker"
										value="%{userSetting.wlSaveParam}">
									</s:select>
		
								</div>
								<!-- /.merchant__form_group -->
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20 save-flag">
								<label for="cardSaveParamCheck" class="checkbox-label unchecked mb-10">
									<s:checkbox id="cardSaveParamCheck" name="expressPay"
										value="%{userSetting.expressPay}" /> Express Pay <span
									class="merchant__check"></span>
								</label>
								<div class="lpay_select_group save-flag-select">
									<label for="cardSaveParam" class="d-none">Card Save Param</label>
									<s:select disabled="true" data-link="cardSaveParam" name="cardSaveParam" id="cardSaveParam"
										headerKey="1"
										list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
										class="selectpicker" value="%{userSetting.cardSaveParam}">
									</s:select>
								</div>
								<!-- /.merchant__form_group -->
							</div>
							<!-- /.col-md-3 mb-20 -->
							
							
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Payout Flags</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
		 
							<div class="col-md-3 mb-20">
								<label for="merchantInitiatedDirectFlag" class="checkbox-label unchecked">
									Payout <s:checkbox name="merchantInitiatedDirectFlag"
										id="merchantInitiatedDirectFlag" class="impsFlag" data-id="merchantInitiatedDirectFlag" value="%{userSetting.merchantInitiatedDirectFlag}" />
								</label>
							</div>
		 
		
							<div class="col-md-3 mb-20">
								<label for="nodalReportFlag" class="checkbox-label unchecked">
									Nodal Report <s:checkbox name="nodalReportFlag"
										id="nodalReportFlag" value="%{userSetting.nodalReportFlag}" />
								</label>
							</div>
		
							<div class="col-md-3 mb-20">
								<label for="virtualAccountFlag" class="checkbox-label unchecked">
									eCollection Report <s:checkbox name="virtualAccountFlag"
										id="virtualAccountFlag" value="%{userSetting.virtualAccountFlag}" />
								</label>
							</div>
							<div class="col-md-3 mb-20">
								<label for="topupFlag" class="checkbox-label unchecked">
									Topup Flag <s:checkbox name="topupFlag"
										id="topupFlag" value="%{userSetting.topupFlag}" />
								</label>
							</div>
							<div class="col-md-3 mb-20">
								<label for="statementFlag" class="checkbox-label unchecked">
									Account Statement Report<s:checkbox name="statementFlag"
										id="statementFlag" value="%{userSetting.statementFlag}" />
								</label>
							</div>
		
							<div class="col-md-3 mb-20">
								<label for=allowNodalPayoutFlag class="checkbox-label unchecked">
									Nodal Topup Payout  <s:checkbox name="allowNodalPayoutFlag"
										id="allowNodalPayoutFlag" value="%{userSetting.allowNodalPayoutFlag}" />
								</label>
							</div>
							
							<div class="col-md-3 mb-20">
								<label for=allowPayoutUpdateStatus class="checkbox-label unchecked">
									Payout Update Status <s:checkbox name="allowPayoutUpdateStatus"
										id="allowPayoutUpdateStatus" value="%{userSetting.allowPayoutUpdateStatus}" />
								</label>
							</div>
							<div class="col-md-3 mb-20">
								<label for=allowECollectionFee class="checkbox-label unchecked">
									E-Collection Fee <s:checkbox name="allowECollectionFee"
										id="allowECollectionFee" value="%{userSetting.allowECollectionFee}" />
								</label>
							</div>
							
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Beneficiary Verification Flags</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<div class="col-md-3 mb-20">
								<label for="accountVerificationFlag" class="checkbox-label unchecked">
									Account Verification <s:checkbox name="accountVerificationFlag"
										id="accountVerificationFlag" value="%{userSetting.accountVerificationFlag}" />
								</label>
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3 mb-20">
								<label for="vpaVerificationFlag" class="checkbox-label unchecked">
									VPA Verification
									<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" 
										data-id="vpaVerificationFlag" value="%{userSetting.vpaVerificationFlag}" />
								</label>
							</div>
							
							
							<!-- <div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>ePOS Flag</h3>
								</div>
								/.inner-heading
							</div> -->
							<!-- /.col-md-12 -->
							<!-- /.col-md-4 -->
							 
							 <div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Invoice Flags</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<div class="col-md-3 mb-20">
								<label for="allowInvoiceEmail" class="checkbox-label unchecked">
									Invoice Email <s:checkbox name="allowInvoiceEmail"
										id="allowInvoiceEmail" value="%{userSetting.allowInvoiceEmail}" />
								</label>
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3 mb-20">
								<label for="allowInvoiceSms" class="checkbox-label unchecked">
									Invoice SMS
									<s:checkbox name="allowInvoiceSms" id="allowInvoiceSms" 
										data-id="allowInvoiceSms" value="%{userSetting.allowInvoiceSms}" />
								</label>
							</div>
		
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Custom Hosting Setting</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
		
							<div class="col-md-3 mb-20 hosted-url">
								<label for="allowCustomHostedUrl" class="checkbox-label unchecked mb-10"> Allow Custom Hosted URL 
									<s:checkbox name="allowCustomHostedUrl" id="allowCustomHostedUrl" class="textFL_merch5"
											value="%{userSetting.allowCustomHostedUrl}">
									</s:checkbox>
								</label>
								<div class="lpay_input_group">
									<label for="">Custom Hosted URL</label>
									<s:textfield
										type="text"
										id="allowCustomHostedText"
										name="customHostedUrl"
										value="%{userSetting.customHostedUrl}"
										autocomplete="off"
										class="lpay_input">
									</s:textfield>
									<!-- <input type="text" class="lpay_input" name="allowCustomHostedText"> -->
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Order ID Flag</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3 mb-20">
								<label for="skipOrderIdForRefund"
									class="checkbox-label unchecked mb-10"> Skip OrderId for
									Refund <s:checkbox name="skipOrderIdForRefund"
										id="skipOrderIdForRefund" headerKey="1" class="textFL_merch5"
										value="%{userSetting.skipOrderIdForRefund}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
		
								<label for="allowSaleDuplicate"
									class="checkbox-label unchecked mb-10"> Allow Duplicate
									Sale <s:checkbox name="allowSaleDuplicate"
										id="allowSaleDuplicate" headerKey="1"
										class="textFL_merch5"
										value="%{userSetting.allowSaleDuplicate}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-3 mb-20 -->
							<div class="col-md-3 mb-20">
								<label for="allowRefundDuplicate"
									class="checkbox-label unchecked mb-10"> Allow Duplicate
									Refund <s:checkbox name="allowRefundDuplicate"
										id="allowRefundDuplicate" headerKey="1"
										class="textFL_merch5"
										value="%{userSetting.allowRefundDuplicate}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-3 mb-20 -->
							<div class="col-md-3 mb-20">
								<label for="allowSaleInRefund"
									class="checkbox-label unchecked mb-10"> Allow Sale In
									Refund <s:checkbox name="allowSaleInRefund"
										id="allowSaleInRefund" headerKey="1"
										class="textFL_merch5"
										value="%{userSetting.allowSaleInRefund}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-3 mb-20 -->
							<div class="col-md-3 mb-20">
								<label for="allowRefundInSale"
									class="checkbox-label unchecked mb-10"> Allow Refund In
									Sale <s:checkbox name="allowRefundInSale"
										id="allowRefundInSale" headerKey="1"
										class="textFL_merch5"
										value="%{userSetting.allowRefundInSale}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<label for="allowDuplicateNot"
									class="checkbox-label unchecked mb-10"> Unique Order Id <s:checkbox name="allowDuplicateNot"
										id="allowDuplicateNot" headerKey="1"
										class="textFL_merch5"
										value="%{userSetting.allowDuplicateNot}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-3 -->
							
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>SMT Merchant Flag</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
		
							<div class="col-md-4 mb-20">
								<label for="smtMerchant" class="checkbox-label unchecked mb-10"> SMT Merchant 
									<s:checkbox name="smtMerchant" id="smtMerchant" class="textFL_merch5"
											value="%{userSetting.smtMerchant}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-4 -->
							
							<!-- /.col-md-3 -->
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Customize Logo</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3 mb-20">
								<label for="logoFlag" class="checkbox-label unchecked">
									Merchant Logo
									<s:if test="%{userSetting.logoFlag == true}">
										<s:checkbox
											name="logoFlag"
											checked="true"
											id="logoFlag"
											data-id="selfLogo"
											value="%{userSetting.logoFlag}"
										/>
									</s:if>
									<s:else>
										<s:checkbox
											name="logoFlag"
											id="logoFlag"
											data-id="selfLogo"
											value="%{userSetting.logoFlag}"
										/>
									</s:else>
								</label>
								<div class="uploaded-logo">
									<img
										src=""
										height="50"
										id="upload_img"
										class="img-responsive uploaded-logo"
										alt=""
									/>
								</div>
								<!-- /.uploaded-logo -->
								<s:hidden name="merchantLogo" value="%{userSetting.merchantLogo}" id="value-merchantLogo" />
								<s:hidden name="businessName" value="%{userSetting.businessName}" id="value-businessNameLogo" />
							</div>
							<!-- /.col-md-3 -->
		
							<div class="col-md-3 mb-20 d-none upload-custom-logo">
								<div class="merchant__form_group">
									<label class="lable-default" id="title-upload-logo">Upload Logo</label>
									
									<label for="uploadMerchantLogo" class="upload-pic lable-default d-flex align-items-center" id="label-upload-logo">
										<img src="../image/cloud-computing.png" alt="/">
										<div class="upload-text">
											<span class="doc-text"><b style="color: #000">File size:</b> 2 MB</span>
											<span class="doc-file-name"><b style="color: #000">Formats:</b>.png / .jpg</span>
										</div>
										<div class="upload-name">
											<span class="doc-text" id="merchantLogoName"></span>
										</div> <!-- /.upload-name -->
										<div class="upload-size-error">
											<span class="" id="upload-error">File size too long.</span>
										</div> <!-- /.upload-size-error -->
										<input
											type="file"
											name="logoImageFile"
											id="uploadMerchantLogo"
											accept=".png, .jpg"
											class="feedback"
										>
									</label>
								</div>
								<!-- /.merchant__form_group -->
								
							</div>
							<!-- /.col-md-4 mb-20 -->
							
							<div class="col-md-3 mb-20 d-none self-logo">
								<div class="lpay_input_group">
									<label for="">Self Logo</label>
									<s:textfield
										onkeypress="lettersAndAlphabet(event)"
										type="text"
										placeholder="Logo Text"
										id="selfLogoText"
										name="logoName"
										value="%{userSetting.logoName}"
										autocomplete="off"
										class="lpay_input">
									</s:textfield>
									<s:hidden type="hidden" value="%{userSetting.logoName}"></s:hidden>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20 d-none" id="allow-logo-pg">
								<label for="allowLogoInPgPage" class="checkbox-label unchecked">
									Allow Logo in PG Page
									<s:if test="%{userSetting.allowLogoInPgPage == 'true'}">
										<s:checkbox
											name="allowLogoInPgPage"
											checked="true"
											id="allowLogoInPgPage"
											value="%{userSetting.allowLogoInPgPage}"
										/>
									</s:if>
									<s:else>
										<s:checkbox
											name="allowLogoInPgPage"
											id="allowLogoInPgPage"
											value="%{userSetting.allowLogoInPgPage}"
										/>
									</s:else>
								</label>
							</div>
							<!-- /.col-md-3 -->
		
							<div class="col-md-3 mb-20">
								<div class="lpay_input_group">
									<label for="">COD Text</label>
									<s:textfield
										type="text"
										onkeypress="onlyLetters(event)"
										placeholder=""
										id="codName"
										name="codName"
										value="%{userSetting.codName}"
										autocomplete="off"
										class="lpay_input">
									</s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							
							
							
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Part Settlement</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-3"></div>
							<!-- /.col-md-3 -->
							<div class="clearfix"></div>
							<!-- /.clearfix -->
							<div class="col-md-4 mb-20">
								<label for="allowPartSettle"
									class="checkbox-label unchecked mb-10"> Allow Part Settle
									<s:checkbox name="allowPartSettle" id="allowPartSettle" value="%{userSetting.allowPartSettle}" />
								</label>
								<div class="lpay_input_group">
									<label for="">Annual Turnover</label>
									
									<s:textfield
										class="lpay_input"
										id="settleannualTurnover"
										data-id="annualTurnover"
										value="%{userSetting.partAnnualTurnover}"
										name="partAnnualTurnover"
										autocomplete="off"
										onkeypress="return isNumber(event, this.value)"
										onkeyup="checkDailyLimit(); checkpartSettle();">
									</s:textfield>
								</div>
								<!-- /.lpay_input_group -->
								<span id="annualTurnoverValid" style="color: red; display: none; margin-left: 0px;">Please Enter Annual Turnover</span>
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20 partSettle d-none">
								<label for="allowSubtractValue" class="checkbox-label unchecked mb-10"> Allow Subtract Value 
									<s:checkbox name="allowSubtractValue" id="allowSubtractValue" value="%{userSetting.allowSubtractValue}" />
								</label>
								<div class="lpay_input_group">
									<label for="">Deviation</label>
									<s:textfield type="text" class="lpay_input" id="deviation"
										name="deviation" value="%{userSetting.deviation}" placeholder="0"
										autocomplete="off"
										OnKeypress="javascript:return isNumber(event,this.value)"
										onkeyup="checkDailyLimit();"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
								<span id="deviationValid"
									style="color: red; display: none; margin-left: 10px;">Please
									Enter Annual Turnover</span>
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20 partSettle d-none mt-30">
								<div class="lpay_input_group">
									<label for="">Daily Limit</label>
									<s:textfield type="text" class="lpay_input" id="dailyLimit"
										value="0" placeholder="0" autocomplete="off" readonly="true"
										OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
								<span id="extraAnnualTurnoverPercentageValid"
									style="color: red; display: none; margin-left: 10px;">Please
									Enter Annual Turnover</span>
							</div>
							<!-- /.col-md-4 mb-20 -->
							
							<div class="clearfix"></div>
							<div class="col-md-4 mb-20 d-none" data-target="reniewSameLimit">
								<label for="reniewSameLimit" id="reniewSameLimitLabel" class="checkbox-label unchecked mb-10">ReNew Same Limit
									<s:checkbox
										name="sameLimitFlag"
										id="reniewSameLimit"
										headerKey="1"
										class="textFL_merch5"
										value="%{userSetting.sameLimitFlag}">
									</s:checkbox>
								</label>
							</div>
							<!-- /.col-md-4 -->
							<!-- <div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>AMEX Settings</h3>
								</div>
								/.inner-heading
							</div> -->
							<!-- /.col-md-12 -->
							<div class="col-md-6 mb-20 d-none">
								<div class="lpay_input_group">
									<label for="">Merchant Category Code</label>
									<s:textfield type="text" id="mCC" class="lpay_input" name="mCC"
										value="%{userSetting.mCC}" autocomplete="off"
										OnKeypress="javascript:return isNumber(event,this.value)">
									</s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
		
							<s:if test="%{userSetting.superMerchantId == null || userSetting.superMerchantId == ''}">
								<div class="col-md-12 mb-20">
									<div class="inner-heading">
										<h3>QR Code</h3>
									</div>
									<!-- /.inner-heading -->
								</div>
								<!-- /.col-md-12 -->
			
								<div class="col-md-3 mb-20">
									<div class="d-inline-flex flex-column align-items-center">
										<label for="allowQRScanFlag" class="checkbox-label unchecked mt-10">
											Allow PG QR
											<s:checkbox name="allowQRScanFlag" id="allowQRScanFlag" data-id="allowQRScanFlag" value="%{userSetting.allowQRScanFlag}" />
										</label>
										<a href="#" class="downloadQrCode" data-download="PGQR">
											<img src="../images/demo-qr-code.png" alt="" height="100">
										</a>
										<a href="#" class="downloadQrCode text-secondary font-weight-medium mt-5" data-download="PGQR">Download PG QR</a>
									</div>
		
								</div>
			
								<div class="col-md-3 mb-20">
									<div class="d-inline-flex flex-column align-items-center">
										<label for="allowUpiQRFlag" class="checkbox-label unchecked mt-10">
											Allow UPI QR
											<s:checkbox name="allowUpiQRFlag" id="allowUpiQRFlag" data-id="allowUpiQRFlag" value="%{userSetting.allowUpiQRFlag}" />
										</label>
										<a href="#" class="downloadQrCode" data-download="UPIQR">
											<img src="../images/demo-qr-code.png" alt="" height="100">
										</a>
										<a href="#" class="downloadQrCode text-secondary font-weight-medium mt-5" data-download="UPIQR">Download UPI QR</a>
									</div>
		
								</div>
								
								<div class="col-md-3 mb-20">
								<label for="customerQrFlag" class="checkbox-label unchecked">
									Static UPI QR Report<s:checkbox name="customerQrFlag"
										id="customerQrFlag" value="%{userSetting.customerQrFlag}" />
								</label>
							</div>
							</s:if>
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Callback Settings</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">PayIn Callback URL</label>
									<s:textfield id="callBackUrl" class="lpay_input"
										name="callBackUrl" type="text" value="%{userSetting.callBackUrl}" autocomplete="off"></s:textfield>
										<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mt-20">
								<label for="callBackFlag" class="checkbox-label unchecked">
									Status Enquiry Callback Flag <s:checkbox name="callBackFlag"
										id="callBackFlag" value="%{userSetting.callBackFlag}" />
								</label>
							</div>
							<div class="col-md-4 mt-20">
								<label for="allCallBackFlag" class="checkbox-label unchecked">
									All Payment Type Callback Flag <s:checkbox name="allCallBackFlag"
										id="allCallBackFlag" value="%{userSetting.allCallBackFlag}" />
								</label>
							</div>
							<div class="col-md-12"></div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Payout Callback URL</label>
									<s:textfield id="callBackUrl" class="lpay_input"
										name="payoutCallbackUrl" type="text" value="%{userSetting.payoutCallbackUrl}" autocomplete="off"></s:textfield>
										<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mt-20">
								<label for="allowPayoutStatusEnquiryCallbackFlag" class="checkbox-label unchecked">
									Payout Status Enquiry Callback Flag <s:checkbox name="allowPayoutStatusEnquiryCallbackFlag"
										id="allowPayoutStatusEnquiryCallbackFlag" value="%{userSetting.allowPayoutStatusEnquiryCallbackFlag}" />
								</label>
							</div>
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>VA Configuration</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-10">
								<label for="enabledVa" class="checkbox-label unchecked">
									Enabled VA <s:checkbox name="enabledVa"
										id="enabledVa" value="%{userSetting.enabledVa}" />
								</label>
							</div>
							<div class="col-md-4 mb-10">
								<label for="vaValidation" class="checkbox-label unchecked">
									Enabled VA Validation <s:checkbox name="vaValidation"
										id="vaValidation" value="%{userSetting.vaValidation}" />
								</label>
							</div>
							<div class="col-md-12"></div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">VA Callback URL</label>
									<s:textfield id="vaCallbackUrl" class="lpay_input"
										name="vaCallbackUrl" type="text" value="%{userSetting.vaCallbackUrl}" autocomplete="off"></s:textfield>
										<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							
							<div class="col-md-4 mt-20">
								<label for="enabledVa" class="checkbox-label unchecked">
									Credit <s:checkbox name="vaCallBackSuccess"
										id="vaCallBackSuccess" value="%{userSetting.vaCallBackSuccess}" />
								</label>
							</div>
							<div class="col-md-4 mt-20">
								<label for="enabledVa" class="checkbox-label unchecked">
									Refund <s:checkbox name="vaCallBackFail"
										id="vaCallBackFail" value="%{userSetting.vaCallBackFail}" />
								</label>
							</div>

							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>P2P</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
		
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-10">
								<label for="randomAmount" class="checkbox-label unchecked">		
									Random Amount <s:checkbox name="randomAmount"
										id="randomAmount" value="%{userSetting.randomAmount}" />
								</label>
							</div>

						</div>
						<!-- /.row -->
					</div>
					<!-- /.merchant__forms_block -->					
					
					<div class="row">
						<div class="col-md-12 text-center button-wrapper button_wrapper" style="position: absolute;top: -85px;right: 0;">
							<s:a class="lpay_button lpay_button-md lpay_button-primary" action='userSettingMerchant'>Back</s:a>
							<button id="btnSave" class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
						</div>
						<!-- /.col-md-12 -->
					</div>
					<!-- /.row -->
				</s:form>
			</div>
			<s:if test="%{parentMerchantFlag == true}">
				<div class="static-mapping_parent">
					
				</div>
				<!-- /.static-mapping_parent -->
				<div class="lpay_tabs_content w-100 d-none" data-target="parentMapping">
					<div class="col-md-12 mb-20 p-0">
						<div class="inner-heading" style="display: flex;align-items: center;justify-content: space-between;">
							<h3 style="font-size: 14px !important;">Add Parent Mapping</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12 text-center button-wrapper button_wrapper" style="position: absolute;top: -85px;right: 0;">
						<s:a class="lpay_button lpay_button-md lpay_button-secondary" action='userSettingMerchant'>Back</s:a>
						<button class="lpay_button lpay_button-md lpay_button-primary" onclick="invokeUpdateMapping()">Save</button>
					</div>
					<!-- /.col-md-12 -->
					<div class="static-mapping_parent">
						<div class="row payout_mapping-data" style="display: flex;align-items: center;width: 100%;">
							<div class="col-md-3 mb-20">
								<div class="lpay_select_group">
									<label>Select Merchant <span class="text-danger">*</span></label>
									<s:select
										name="merchantEmailId"
										data-download="merchantPayId"
										data-var="merchantEmailId"
										class="selectpicker"
										id="merchant"
										data-submerchant="subMerchant"
										data-user="subUser"
										headerKey=""
										data-live-search="true"
										headerValue="Select Merchant"
										list="merchantList"
										listKey="payId"
										listValue="businessName"
										autocomplete="off"
										onchange="setLoadData(this)"
									/>					
								</div>
							</div>
							<!-- col-md-3 -->
							<div class="col-md-2">
								<label for="isActive" class="checkbox-label unchecked mb-10"> Is Active 
									<input type="checkbox" name="isActive" id="isActive">
								</label>
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-3 mb-20">
								<div class="lpay_select_group">
								   <label for="">Customer Category</label>
								   <s:select
								   		name="customerCategory"
										data-var="customerCategory"
										list="@com.paymentgateway.commons.user.CustomerCategory@values()"
																	
										autocomplete="off"
										class="selectpicker" 
										id="customerCategory"
									/>	
								</div>
								<!-- /.lpay_select_group -->  
							</div>
							<!-- /.col-md-3 -->
							<div class="col-md-1 mb-20">
								<div class="lpay_input_group">
									<label for="">Load</label>
									<span class="error-msg"></span>
									<input type="text" oninput="invokeLoadPercentage(event, this)" onkeypress="mzDigitDot(event)" class="lpay_input">
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="col-md-2 flag-wrapper_button">
								<button class="lpay_button lpay_button-md lpay_button-secondary add-btn" onclick="invokeAddRow(this)"><i class="fa fa-plus" aria-hidden="true"></i></button>
								<button class="lpay_button lpay_button-md lpay_button-primary d-none dlt-btn" onclick="invokeDeleteMapping(this)"><i class="fa fa-trash" aria-hidden="true"></i></button>
							</div>
							<!-- /.col-md-3 -->
						</div>
						<!-- /.row -->
					</div>
					<!-- /.static-mapping_parent -->
					<div class="payout_mapping-list">
						Parent Mapping List
					</div>
					<!-- /.payout_mapping-list -->
				</div>
				<!-- /.lpay_tabs_content w-100 -->
			</s:if>
		</div>
	</div>
	<!-- /.row -->
</section>

<div class="lpay_popup">
	<div class="lpay_popup-inner">
		<div class="lpay_popup-innerbox" data-status="default">
			<div class="lpay_popup-innerbox-success lpay-center">
				<div class="lpay_popup_icon">
					<img src="../image/tick.png" alt="">
				</div>
				<!-- /.lpay_popup_icon -->
				<div class="lpay_popup-content">
					<h2 class="response-msg">Success</h2>
					<h3 class="responseMsg">Your data has been saved successfully.</h3>
				</div>
				<!-- /.lpay_popup-content -->
				<div class="lpay_popup-button">
					<button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" id="confirmButton1">Ok</button>
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
					<h2 class="response-msg">Failed</h2>
					<h3 class="responseMsg">Nothing Found Try Again.</h3>
				</div>
				<!-- /.lpay_popup-content -->
				<div class="lpay_popup-button">
					<button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" id="confirmButton2">Ok</button>
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
			<span>Do you really want to ggf gbn ? This process cannot be undone.</span>
		</div>
		<!-- /.confirm-delete-text -->
		<div class="confirm-delete-button">
			<button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn">Cancel</button>
			<button  class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">Delete</button>
		</div>
		<!-- /.confirm-delete-button -->
	</div>
	<!-- /.confirm-popup-box -->
</div>
<!-- /.confrim-popup -->
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<!-- <s:form action="downloadSingleMpaFileAction" id="downloadSingleMpaFileAction">

	</s:form> -->

	<!-- <s:form method="POST" id="checkerFileDownload"
		action="statusFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="checkerFileName" value="%{userSetting.checkerFileName}" />
		<s:hidden name="payId" value="%{userSetting.payId}" />
	</s:form> -->

	<!-- <s:form method="POST" id="makerFileDownload"
		action="statusFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="makerFileName" value="%{userSetting.makerFileName}" />
		<s:hidden name="payId" value="%{userSetting.payId}" />
	</s:form> -->

	<!-- <s:form method="post" id="mpaFileDownload"
		action="mpaMerchantFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="payId" value="%{userSetting.payId}" />
		<s:hidden name="fileNameType" id="fileType" />
	</s:form> -->

	<s:form method="POST" action="qrImageDowloadAction" id="qrCodeDownloadForm">
		<s:hidden name="payId" value="%{userSetting.payId}" />
		<s:hidden name="qrType" id="qrType" />
	</s:form>

	<script>

		$(document).ready(function() {
			
			$("body").on("click", ".downloadQrCode", function(e) {
				e.preventDefault();
				var qrType = $(this).attr("data-download");
				$("#qrType").val(qrType);
				$("#qrCodeDownloadForm").submit();
			});

			$("#configurableFlag").on("change", function(e){
				var _isChecked = $(this).is(":checked");
				if(_isChecked){
					$("#configurableTime").closest(".lpay_input_group").removeClass("d-none");
				}else{
					$("#configurableTime").closest(".lpay_input_group").addClass("d-none");
				}
			})

            $("body").on("change", ".checkbox-label input", function(e) {
				if ($(this).is(":checked")) {
					var _checkSaveFlag = $(this).closest(".save-flag");
					var _checkHosted = $(this).closest(".hosted-url");
					if(_checkSaveFlag.length > 0){
						_checkSaveFlag.find("select").prop("disabled", false);
						_checkSaveFlag.find(".selectpicker").selectpicker('refresh');
					}

					if(_checkHosted.length > 0){
						_checkHosted.find(".lpay_input_group").addClass("active-hosted-input");
						_checkHosted.find(".lpay_input_group .lpay_input").attr("data-mandate", 'on');
						_checkHosted.find(".lpay_input_group .lpay_input").prop("disabled", false);
					}
					
					$(this).closest("label").addClass("checkbox-checked");
				} else {
					var _checkSaveFlag = $(this).closest(".save-flag");
					var _checkHosted = $(this).closest(".hosted-url");

					if(_checkSaveFlag.length > 0){
						_checkSaveFlag.find("select").prop("disabled", true);
						_checkSaveFlag.find(".selectpicker").selectpicker('refresh');
					}

					if(_checkHosted.length > 0){
						_checkHosted.find(".lpay_input_group").removeClass("active-hosted-input");
						_checkHosted.find(".lpay_input_group .lpay_input").removeAttr("data-mandate");
						_checkHosted.find(".lpay_input_group .lpay_input").prop("disabled", true);
					}

					$(this).closest("label").removeClass("checkbox-checked");
				}
			});

			var _getHosted = document.querySelector("#allowCustomHostedText").value;
			if(_getHosted == ""){
				document.querySelector("#allowCustomHostedText").value = "https://";
			}



			var _getAnnualTurnover = $("[data-id=annualTurnover]").val();
			var _deviationVal = $("#deviation").val();

			if (_getAnnualTurnover != "") {
				$("[data-id=annualTurnover]").attr("readonly", true);
			}

			if ($("#allowPartSettle").is(":checked")) {
				$(".partSettle").removeClass("d-none");
			}

			if ($("#allowSubtractValue").is(":checked")) {
				$("#allowSubtractValue").closest(".partSettle").addClass("subTract");
			}

			if (_deviationVal == "") {
				$("#deviation").val("0");
			}

			$("#deviation").on("change", function(e) {
				if ($(this).val() == "") {
					$("#deviation").val("0");
				}
			});

			$("#allowPartSettle, [data-id=annualTurnover]").on("change", function(e) {
				if ($("#allowPartSettle").is(":checked")) {
					if ($("[data-id=annualTurnover]").val() != "") {
						$(".partSettle").removeClass("d-none");
						checkDailyLimit();
					} else {
						$(".partSettle").addClass("d-none");
					}
				} else {
					$(".partSettle").addClass("d-none");
				}
			});

			$("[data-id=annualTurnover]").on("keyup", function(e) {
				$("#annualTurnoverValid").css("display", "none");
			});
			
		});
		// CALL THIS FUNCTION IN ONKEYUP---FOR MERCHANT TRANSACTION EMAIL
	
		var _isConfigurableFlagTrue = document.querySelector("#configurableFlag").checked;
		if(_isConfigurableFlagTrue === true){
			document.querySelector("#configurableTime").closest(".lpay_input_group").classList.remove("d-none");
		}

		function checkpartSettle() {
			var isAllowPartSettle = document.getElementById("allowPartSettle").checked;
			var annualTurnover = document.querySelector("[data-id=annualTurnover]").value;
			if (isAllowPartSettle) {
				if ((annualTurnover == "") || (annualTurnover == null)) {
					document.getElementById("annualTurnoverValid").style.display = "block";
					document.getElementById("btnSave").disabled = true;
				} else {
					document.getElementById("annualTurnoverValid").style.display = "none";
					document.getElementById("btnSave").disabled = false;
				}
			} else {
				document.getElementById("annualTurnoverValid").style.display = "none";
				document.getElementById("btnSave").disabled = false;
			}
		}

		function checkDailyLimit() {
			var isAllowPartSettle = document.getElementById("allowPartSettle").checked;
			var annualTurnover = Number(document.querySelector("[data-id=annualTurnover]").value);
			var extraAnnualTurnoverPercentage = Number(document.getElementById("deviation").value);
			var isAllowSubtraction = document.getElementById("allowSubtractValue").checked;
			if (isAllowPartSettle) {
				if (isAllowSubtraction == false) {
					var calculateTotalAmount = annualTurnover + (annualTurnover / 100) * (extraAnnualTurnoverPercentage);
					var calculateDailyLimit = Math.round(calculateTotalAmount / 365);
					document.getElementById("dailyLimit").value = calculateDailyLimit;
				} else {
					var calculateTotalAmount = annualTurnover - (annualTurnover / 100) * (extraAnnualTurnoverPercentage);
					var calculateDailyLimit = Math.round(calculateTotalAmount / 365);
					document.getElementById("dailyLimit").value = calculateDailyLimit;
				}
			}
		}
		checkDailyLimit();
	</script>

	<style type="text/css">
		@-moz-document url-prefix () {
			#allowRefund2 {	margin-left:42.6%!important; }
		}
	</style>

	<script src="../js/main.js"></script>
	<script src="../js/decimalLimit.js"></script>
	<script src="../js/common-validations.js"></script>
	<script src="../js/merchantAccountSetup-flag.js"></script>
	<script src="../js/mechant-script-flag.js"></script>
</body>
</html>