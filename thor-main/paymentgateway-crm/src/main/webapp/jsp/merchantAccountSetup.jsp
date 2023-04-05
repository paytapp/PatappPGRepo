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
	<title>Merchant Account Setup</title>
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
		$(function() {
			$("#dateOfIncorporation").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				changeMonth : true,
				changeYear : true,
				dateFormat : 'dd-mm-yy',
				yearRange: "-100:+0",
			});
			$("#requestUrl").val($(".requestUrl").text());
		});

		function loadSubcategory() {
			var industry = document.getElementById("industryCategory").value;
			var token = document.getElementsByName("token")[0].value;

			$.ajax({
				type : "POST",
				url : "industrySubCategory",
				timeout : 0,
				data : {
					industryCategory : industry,
					token : token,
					"struts.token.name" : "token"
				},
				success : function(data, status) {
					var subCategoryListObj = data.subCategories;
					var subCategoryList = subCategoryListObj[0].split(',');
					var radioDiv = document.getElementById("radiodiv");
					radioDiv.innerHTML = "";
					for (var i = 0; i < subCategoryList.length; i++) {
						var subcategory = subCategoryList[i];
						var radioOption = document.createElement("INPUT");
						radioOption.setAttribute("type", "radio");
						radioOption.setAttribute("value", subcategory);
						radioOption.setAttribute("name", "subcategory");
						var labelS = document.createElement("SPAN");
						labelS.innerHTML = subcategory;
						radioDiv.appendChild(radioOption);
						radioDiv.appendChild(labelS);
					}

				},
				error : function(status) {
					alert("please try again later!!");
				}
			});
		}

		function selectSubcategory() {
			var checkedRadio = $('input[name="subcategory"]:checked').val();

			if (checkedRadio == null) {
				document.getElementById("radioError").innerHTML = "Please select a subcategory";
				return false;
			}

			document.getElementById("radioError").innerHTML = "";

			var subCategoryText = document.getElementById("subcategory");
			subCategoryText.value = checkedRadio;

			$('#popup').popup('hide');
		}

		$(document).ready(function() {

			// create function for selectpicker
			var _selectpicker = ['typeOfEntity', 'industryCategory', "country"];
			for(var _itiator = 0; _itiator < _selectpicker.length; _itiator++){
				var _value = $("[data-id='"+_selectpicker[_itiator]+"']").val();
				if(_value != ""){

					$("#"+_selectpicker[_itiator]).selectpicker();
					$("#"+_selectpicker[_itiator]).selectpicker('val', _value);
				//	$("#"+_selectpicker[_itiator]).attr("disabled", true);
				}
			}

			// COUNTRY DEFAULT SELECTED
			populateCountries("country", "state");
			populateStates("country", "state");
			var dataCountry = $("#dataCountry").val();
			
			if (dataCountry !== "") {
				$("#country").val(dataCountry);
			}

			setTimeout(function(e){
				$("#country").trigger("change");
				$("#country").selectpicker('val', $("#mpaTradingCountry").val());
				populateStates("country", "state", false);
				$("#country").selectpicker('refresh');
				$("#state").selectpicker('refresh');
				$("#state").selectpicker('val', $("#mpaTradingState").val());
			}, 2000);

			var dataState = $("#state").val();
			if (dataState !== "") {
				$("#state").val(dataState);
			}
			$("#country").on("change", function(e) {
				$("#state").next("div").removeClass("disabled");
				$("#state").selectpicker('refresh');
			})

			/* loadSubcategory(); */
			$("#subcategory").click(function() {
				$('#popup').popup({
					'blur' : false,
					'escape' : false
				}).popup('show');
			});
		});

	

		

		var _validFileExtensions = [ ".jpg", ".pdf", ".png" ];
		function Validate(oForm) {
			var arrInputs = oForm.getElementsByTagName("input");
			for (var i = 0; i < arrInputs.length; i++) {
				var oInput = arrInputs[i];
				if (oInput.type == "file") {
					var sFileName = oInput.value;
					if (sFileName.length > 0) {
						var blnValid = false;
						for (var j = 0; j < _validFileExtensions.length; j++) {
							var sCurExtension = _validFileExtensions[j];
							if (sFileName.substr(
									sFileName.length - sCurExtension.length,
									sCurExtension.length).toLowerCase() == sCurExtension
									.toLowerCase()) {
								blnValid = true;
								break;
							}
						}

						if (!blnValid) {
							alert("Sorry, " + sFileName
									+ " is invalid, allowed extensions are: "
									+ _validFileExtensions.join(", "));
							return false;
						} else {
							alert("Upload Successfully");
						}
					}
				}
			}
			return true;
		}

		var _validFileExtensions = [ ".jpg", ".gif", ".png" ];
		function Validatelogo(oForm) {
			var arrInputs = oForm.getElementsByTagName("input");
			for (var i = 0; i < arrInputs.length; i++) {
				var oInput = arrInputs[i];
				if (oInput.type == "file") {
					var sFileName = oInput.value;
					if (sFileName.length > 0) {
						var blnValid = false;
						for (var j = 0; j < _validFileExtensions.length; j++) {
							var sCurExtension = _validFileExtensions[j];
							if (sFileName.substr(
									sFileName.length - sCurExtension.length,
									sCurExtension.length).toLowerCase() == sCurExtension
									.toLowerCase()) {
								blnValid = true;
								break;
							}
						}

						if (!blnValid) {
							alert("Sorry, " + sFileName
									+ " is invalid, allowed extensions are: "
									+ _validFileExtensions.join(", "));
							return false;
						} else {
							alert("Upload Successfully");
						}
					}
				}
			}
			return true;
		}
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
		

	</style>
</head>
<body>
	<s:hidden value="%{MPAData.typeOfEntity}" data-id="typeOfEntity"></s:hidden>
	<s:hidden value="%{MPAData.industryCategory}" data-id="industryCategory"></s:hidden>
	<s:hidden value="%{MPAData.paymentCycle}" data-id="paymentCycle"></s:hidden>
	<s:hidden value="%{MPAData.tradingCountry}" id="mpaTradingCountry"></s:hidden>
	<s:hidden value="%{MPAData.tradingCountry}" data-id="country"></s:hidden>
	<s:hidden value="%{MPAData.tradingState}" id="mpaTradingState"></s:hidden>
	<s:hidden value="%{MPAData.serverDetails}" data-id="serverDetails"></s:hidden>
	<s:hidden value="%{merchantLogo}" data-id="merchantLogo"></s:hidden>
	<s:hidden value="%{showDownload}" data-id="showDownload"></s:hidden>
	<s:hidden value="%{MPAData.surcharge}" data-id="surcharge"></s:hidden>
	<s:hidden value="%{user.resellerId}" id="mpaResellerId"></s:hidden>
	<s:hidden value="%{MPAData.oneTimeRefundLimit}" data-id="oneTimeRefundAmount"></s:hidden>
	<s:hidden value="%{MPAData.registrationNumber}" id="registrationNumber"></s:hidden>
	<s:hidden value="%{MPAData.businessName}" data-id="businessName"></s:hidden>

	<section class="merchant-account lapy_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<!-- <div class="col-md-5">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o"
					aria-hidden="true"></i></span>
				<h2 class="heading_text">Merchant Account Setup</h2>
			</div>
		</div> -->
		<!-- /.col-md-12 -->
		<div class="col-md-4 p-0">
			<span class="heading-ribbon">
				<h4><s:property value="%{MPAData.businessName}"></s:property></h4>
			</span>
		</div>
		<!-- /.col-md-3 -->
		<div class="col-md-12">
			<div class="horizontal-nav-wrapper mb-20">
				<nav id="horizontal-nav" class="horizontal-nav">
					<ul class="horizontal-nav-content nav nav-tabs list-unstyled font-size-10 merchant-config-tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
						<li class="nav-item merchant__tab_button active-tab" data-target="merchantDetails">Merchant Details</li>
						<li class="nav-item merchant__tab_button" data-target="principleInformation">Principle Information</li>
						<li class="nav-item merchant__tab_button" data-target="bankAccountDetails">Bank Details</li>
						<li class="nav-item merchant__tab_button" data-target="businessDetails">Business Details</li>
						<!-- <li class="nav-item merchant__tab_button" data-target="configuration">Configuration</li> -->
						<!-- <li class="nav-item merchant__tab_button" data-target="technicalDetails">Technical Details</li> -->
						<!-- <li class="nav-item merchant__tab_button" data-target="policyRegularity">Policy and Regularity</li> -->
						<li class="nav-item merchant__tab_button d-none" data-target="surchargeTextTab">Surcharge Text</li>
						<s:if test="%{MPAData.esignAadhaarType != null}">
							<li class="nav-item merchant__tab_button" data-target="eSign">e-Sign</li>
						</s:if>
						<!-- <li class="nav-item merchant__tab_button" data-target="action">Action</li> -->
						<li class="nav-item merchant__tab_button" data-target="onBoardDetail">Onboard Details</li>
						<li class="nav-item merchant__tab_button" data-target="notificationSetting">Notification Settings</li>
						<!-- <li class="nav-item merchant__tab_button" data-target="systemSetting">System Setting</li> -->
						<s:if test="%{showDownload != false}">
							<li class="nav-item merchant__tab_button" data-target="downloads">Downloads</li>
						</s:if>
						<s:else>
							<li class="nav-item merchant__tab_button" data-target="documentUpload">Document</li>						
						</s:else>
						<s:if test="%{MPAData.makerStatus != null}">
							<li class="nav-item merchant__tab_button" data-target="maker">Maker</li>
						</s:if>
						<s:if test="%{MPAData.checkerStatus != null}">
							<li class="nav-item merchant__tab_button" data-target="checker">Checker</li>
						</s:if>
					</ul>
				</nav>
				<button type="button" id="btn-scroll-left"
					class="horizontal-nav-btn horizontal-nav-btn-left m-0 bg-color-white pr-5">
					<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 551 1024">
					<path
						d="M445.44 38.183L-2.53 512l447.97 473.817 85.857-81.173-409.6-433.23v81.172l409.6-433.23L445.44 38.18z" /></svg>
				</button>
				<button type="button" id="btn-scroll-right"
					class="horizontal-nav-btn horizontal-nav-btn-right m-0 bg-color-white pl-5">
					<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 551 1024">
					<path
						d="M105.56 985.817L553.53 512 105.56 38.183l-85.857 81.173 409.6 433.23v-81.172l-409.6 433.23 85.856 81.174z" /></svg>
				</button>
			</div>
		</div>
		<!-- /.col-md-12 -->
	</div>
	<!-- /.row -->
	<div class="merchant__forms m-0">
		<s:form action="merchantSetupUpdateAction" method="post"
			autocomplete="off" enctype="multipart/form-data" class="FlowupLabels"
			id="merchantForm" theme="css_xhtml">
			<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			<s:hidden name="emailId" value="%{MPAData.emailId}"></s:hidden>
			<s:hidden name="businessName" value="%{MPAData.businessName}"></s:hidden>
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
			<div class="merchant__forms_block active-block"
				data-active="merchantDetails">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Industry Category</label>
							<s:select
								class="selectpicker"
								id="industryCategory"
								name="industryCategory"
								list="industryTypesList"
								value="%{MPAData.industryCategory}"
								autocomplete="off">
							</s:select>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<s:if test="%{MPAData.typeOfEntity == 'Private Limited' || MPAData.typeOfEntity == 'Public Limited' || MPAData.typeOfEntity == 'Partnership Firm' || MPAData.typeOfEntity == 'Proprietory' || MPAData.typeOfEntity == null || MPAData.typeOfEntity == ''}">
						<div class="col-md-4 mb-20">
							<div class="lpay_select_group">
								<label for="">Type Of Entity</label>
								<s:select
									name="typeOfEntity"
									id="typeOfEntity"
									headerKey="1"
									onchange="getEntity(this)"
									list="#{'' : 'Select Entity','Private Limited':'Private Limited','Public Limited':'Public Limited','Partnership Firm':'Partnership Firm','Proprietory':'Proprietory','Other':'Other'}"
									class="selectpicker"
									value="%{MPAData.typeOfEntity}">
								</s:select>
							</div>
							<!-- /.lpay_select_group -->
						</div>
						<!-- /.col-md-3 -->
					</s:if>
					<s:else>
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Type Of Entity</label>
								<s:textfield
									id="typeOfEntity"
									class="lpay_input"
									onkeypress="return lettersSpaceOnly(event, this);"
									name="typeOfEntity"
									type="text"
									value="%{MPAData.typeOfEntity}"
									readonly="true"
									>
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:else>

					<div class="col-md-4 mb-20 d-none otherEntity" data-target="otherEntity">
						<div class="lpay_input_group">
							<label for="">Other Entity</label>
							<s:textfield
								id="otherEntity"
								oninput="removeError(this)"
								onblur="removeError(this)"
								data-mandate="on"
								class="lpay_input"
								onkeypress="return lettersSpaceOnly(event, this);"
								type="text">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->

					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Legal Name</label>
							<s:textfield
								id="companyName"
								class="lpay_input"
								onkeypress="return lettersSpaceOnly(event, this);"
								name="companyName"
								oninput="NameValidater(this)"
								type="text"
								value="%{MPAData.companyName}">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for=""><span data-label="cin">CIN</span></label>
								<s:textfield
									id="cin"
									name="cin"
									type="text"
									value="%{MPAData.cin}"
									class="lpay_input"
									autocomplete="off"
									oninput="onlyAlphaNumeric(this); _uppercase(this);">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Date of Incorporation</label>
							<s:textfield
								id="dateOfIncorporation"
								class="lpay_input"
								name="dateOfIncorporation"
								type="text"
								value="%{MPAData.dateOfIncorporation}"
								autocomplete="off">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Registered Email</label>
							<div class="position-relative">
								<s:textfield
									data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$" 
									id="companyEmailId"
									onblur="blurMsg(event)" 
									oninput="checkLength(this, 'Email ID');"
									class="lpay_input"
									value="%{MPAData.companyEmailId}"
									name="companyEmailId"
									type="text">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.position-relative -->
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Registered Address</label>
							<s:textfield
								id="companyRegisteredAddress"
								class="lpay_input"
								name="companyRegisteredAddress"
								type="text"
								value="%{MPAData.companyRegisteredAddress}">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Trading Address</label>
							<s:textfield id="tradingAddress1" class="lpay_input"
								name="tradingAddress1" type="text"
								value="%{MPAData.tradingAddress1}" autocomplete="off"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Country</label> <select class="" id="country"
								name="tradingCountry"></select>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">State</label> <select class="selectpicker"
								id="state" name="tradingState"></select>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">PIN</label>
							<s:textfield id="tradingPin" maxLength="6" class="lpay_input"
								name="tradingPin" type="text" value="%{MPAData.tradingPin}"
								autocomplete="off" onblur="blurMsg(event)" data-reg="^[0-9]{6}$" oninput="checkLength(this, 'PIN');numeric(this)"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Company PAN</label>
							<s:textfield
								data-reg="^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$"
								onblur="blurMsg(event)"
								oninput="checkLength(this, 'PAN Number'); alphaNumericAlt(this)"
								id="businessPan"
								class="lpay_input"
								name="businessPan"
								type="text"
								maxLength="11"
								value="%{MPAData.businessPan}"
								autocomplete="off"
								maxlength="10">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">GST</label>
							<s:textfield id="gstin" onblur="blurMsg(event)"
							oninput="checkLength(this, 'GST'); alphaNumericAlt(this)" data-reg="[0-9]{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z\d]{1}" class="lpay_input" name="gstin"
								type="text" maxLength="15" value="%{MPAData.gstin}" autocomplete="off"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Business Phone</label>
							<s:textfield
								id="companyPhone"
								class="lpay_input"
								name="companyPhone"
								type="text"
								onblur="blurMsg(event)" 
								data-reg="^[0-9]{10}$" 
								oninput="checkLength(this, 'Mobile Number'); numeric(this);"
								value="%{MPAData.companyPhone}"
								autocomplete="off"
								maxlength="10"
								onkeypress="return isNumber(event)">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Company Website</label>
							<s:textfield id="companyWebsite" class="lpay_input"
								name="companyWebsite" type="text" value="%{MPAData.companyWebsite}" autocomplete="off"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email for Communication</label>
								<s:textfield
									data-target="proprietory"
									data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$" 
									id="businessEmailForCommunication"
									onblur="blurMsg(event)" 
									oninput="checkLength(this, 'Email ID');" 
									class="lpay_input"
									name="businessEmailForCommunication" 
									type="text"
									value="%{MPAData.businessEmailForCommunication}">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.merchant__form_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="principleInformation">
				<div class="row">
						<div class="contact-detail-stage w-100" data-hide="contact-detail">
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Contact Person</label>
									<s:textfield
										type="text" data-target="proprietory"
										onkeypress="return lettersSpaceOnly(event, this);"
										id="contactName"
										value="%{MPAData.contactName}"
										name="contactName"
										class="lpay_input">
									</s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Mobile</label>
									<s:textfield
										type="text"
										onblur="blurMsg(event)" data-reg="^[0-9]{10}$" oninput="checkLength(this, 'Mobile Number');numeric(this)"
										id="contactMobile"
										value="%{MPAData.contactMobile}"
										name="contactMobile"
										maxlength="10"
										data-target="proprietory"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Email</label>
									<s:textfield type="text" data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$" onblur="blurMsg(event)" 
									oninput="checkLength(this, 'Email ID');" data-target="proprietory"
										id="contactEmail" value="%{MPAData.contactEmail}"
										name="contactEmail" class="lpay_input"></s:textfield>
										<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Landline</label>
									<s:textfield
										data-reg="^[0-9]{10}$"
										type="text"
										data-target="proprietory"
										maxlength="10"
										oninput="checkLength(this, 'Landline');"
										onkeypress="return isNumber(event)"
										id="contactLandline"
										value="%{MPAData.contactLandline}"
										name="contactLandline"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
						</div>
						<!-- /.w-100 -->
						<div class="col-md-12">
							<div class="inner-heading mb-20">
								<h3 data-label="director1Label">First Director Detail</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Name</label>
								<s:textfield
									type="text"
									onkeypress="return lettersSpaceOnly(event, this);"
									id="director1FullName"
									value="%{MPAData.director1FullName}"
									name="director1FullName"
									class="lpay_input">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">PAN Number</label>
								<s:textfield
									type="text"
									data-target="proprietory"
									data-reg="^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$"
									onblur="blurMsg(event)"
									oninput="checkLength(this, 'PAN Number'); alphaNumericAlt(this); _uppercase(this)"
									maxlength="10"
									id="director1Pan"
									value="%{MPAData.director1Pan}"
									name="director1Pan"
									class="lpay_input">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email</label>
								<s:textfield
									type="text"
									data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
									onblur="blurMsg(event);"
									id="director1Email"
									oninput="checkLength(this, 'Email ID');"
									value="%{MPAData.director1Email}"
									name="director1Email"
									class="lpay_input">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile Number</label>
								<s:textfield
									type="text"
									onblur="blurMsg(event)" 
									data-reg="^[0-9]{10}$" 
									oninput="checkLength(this, 'Mobile Number');numeric(this)"
									id="director1Mobile"
									value="%{MPAData.director1Mobile}"
									name="director1Mobile"
									maxlength="10"
									class="lpay_input">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Landline</label>
								<s:textfield
									data-reg="^[0-9]{10}$"
									type="text"
									maxlength="10"
									oninput="checkLength(this, 'Landline');"
									onkeypress="return isNumber(event)"
									id="director1Landline"
									value="%{MPAData.director1Landline}"
									name="director1Landline"
									class="lpay_input">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Address</label>
								<s:textfield
									type="text"
									
									id="director1Address"
									value="%{MPAData.director1Address}"
									name="director1Address"
									class="lpay_input">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="w-100" data-hide="directorTwo" data-type="director two">

							<div class="col-md-12">
								<div class="inner-heading mb-20">
									<h3  data-label="director2Label">Second Director Detail</h3>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Name</label>
									<s:textfield type="text" data-target="proprietory"
										onkeypress="return lettersSpaceOnly(event, this);"
										id="director2FullName" value="%{MPAData.director2FullName}"
										name="director2FullName" class="lpay_input"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">PAN Number</label>
									<s:textfield
										type="text" data-target="proprietory"
										data-reg="^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$"
										onblur="blurMsg(event)"
										oninput="checkLength(this, 'PAN Number'); alphaNumericAlt(this)"
										maxlength="10"
										id="director2Pan"
										value="%{MPAData.director2Pan}"
										name="director2Pan"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Email</label>
									<s:textfield
										type="text" data-target="proprietory"
										data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
										onblur="blurMsg(event)" 
										oninput="checkLength(this, 'Email ID');"
										id="director2Email"
										value="%{MPAData.director2Email}"
										name="director2Email"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Mobile Number</label>
									<s:textfield
										type="text" data-target="proprietory"
										maxlength="10"
										onblur="blurMsg(event)" data-reg="^[0-9]{10}$" oninput="checkLength(this, 'Mobile Number');numeric(this)"
										id="director2Mobile"
										value="%{MPAData.director2Mobile}"
										name="director2Mobile"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Landline</label>
									<s:textfield
										data-reg="^[0-9]{10}$"
										type="text"
										maxlength="10"
										oninput="checkLength(this, 'Landline');"
										onkeypress="return isNumber(event)"
										id="director2Landline"
										value="%{MPAData.director2Landline}"
										name="director2Landline"
										class="lpay_input">
									</s:textfield>
									<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Address</label>
									<s:textfield type="text" data-target="proprietory" id="director2Address"
										value="%{MPAData.director2Address}" name="director2Address"
										class="lpay_input"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
						</div>
						<!-- /.w-100 -->
						<div class="w-100 merchantSupport-all" data-hide="merchant-support">
							<div class="col-md-12 mb-20">
								<div class="inner-heading">
									<h3>Merchant Support Detail</h3>
								</div>
								<!-- /.lpay_heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Email</label>
									<s:textfield type="text" id="merchantEmail"
									data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
									onblur="blurMsg(event);" oninput="checkLength(this, 'Email ID');"
										value="%{MPAData.merchantSupportEmailId}"
										name="merchantSupportEmailId" class="lpay_input"></s:textfield>
										<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Mobile Number</label>
									<s:textfield type="text"
										maxlength="10"
										onblur="blurMsg(event)" 
										data-reg="^[0-9]{10}$" 
										oninput="checkLength(this, 'Mobile Number');numeric(this)"
										id="merchantMobile"
										value="%{MPAData.merchantSupportMobileNumber}"
										name="merchantSupportMobileNumber" class="lpay_input"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
							<div class="col-md-4 mb-30">
								<div class="lpay_input_group">
									<label for="">Landline</label>
									<s:textfield type="text"
										maxlength="10" data-reg="^[0-9]{10}$"
										oninput="checkLength(this, 'Landline');"
										onkeypress="return isNumber(event)"
										id="merchantSupportLandLine" value="%{MPAData.merchantSupportLandLine}"
										name="merchantSupportLandLine" class="lpay_input"></s:textfield>
										<span class="error-msg"></span>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 -->
						</div>
						<!-- /.w-100 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="bankAccountDetails">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Account Number</label>
							<s:textfield
								type="text"
								id="accountNumber"
								onkeypress="javascript:return isNumber(event)"
								value="%{MPAData.accountNumber}"
								name="accountNumber"
								maxlength="18"
								class="lpay_input">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Virtual Account Number</label>
							<s:textfield
								type="text"
								id="virtualAccountNo"
								onkeypress="javascript:return isNumber(event)"
								maxlength="18"
								value="%{MPAData.virtualAccountNo}"
								readonly="true"
								name="virtualAccountNo"
								class="lpay_input">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Merchant VPA</label>
							<s:textfield
								type="text"
								id="merchantVPA"
								onkeypress="javascript:return isNumber(event)"
								maxlength="18"
								value="%{MPAData.merchantVPA}"
								readonly="true"
								name="merchantVPA"
								class="lpay_input">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Account Name</label>
							<s:textfield type="text" id="accountName"
								onkeypress="return lettersSpaceOnly(event, this);"
								value="%{MPAData.accountHolderName}" name="accountHolderName"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">IFSC</label>
							<s:textfield
								type="text"
								onblur="removeErrorBlank(this)"
								oninput=";_uppercase(this); alphaNumericAlt(this);"
								id="accountIfsc"
								maxlength="11"
								value="%{MPAData.accountIfsc}"
								name="accountIfsc"
								class="lpay_input mpa-has-length">
								<span class="error-msg"></span>
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Mobile Number</label>
							<s:textfield
								type="text"
								maxlength="10"
								id="accountMobileNumber"
								onblur="blurMsg(event)" data-reg="^[0-9]{10}$" oninput="checkLength(this, 'Mobile Number');numeric(this)"
								value="%{MPAData.accountMobileNumber}"
								name="accountMobileNumber"
								class="lpay_input"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="businessDetails">
				<div class="row">
					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">Annual Turnover (Approx)</label>
							<s:textfield type="text" id="annualTurnover"
								value="%{MPAData.annualTurnover}" oninput="numeric(this)"  name="annualTurnover"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">Annual Turnover ( Online )</label>
							<s:textfield type="text" id="annualTurnoverOnline"
								value="%{MPAData.annualTurnoverOnline}"
								name="annualTurnoverOnline" oninput="numeric(this)" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Credit Card</label>
							<s:textfield
								type="text"
								id="percentageCC"
								value="%{MPAData.percentageCC}"
								name="percentageCC"
								class="lpay_input" oninput="numeric(this)">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Debit Card</label>
							<s:textfield
								type="text"
								id="percentageDC"
								value="%{MPAData.percentageDC}"
								name="percentageDC"
								class="lpay_input" oninput="numeric(this)">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Net Banking</label>
							<s:textfield type="text" id="percentageNB"
								value="%{MPAData.percentageNB}" name="percentageNB"
								class="lpay_input" oninput="numeric(this)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">UPI</label>
							<s:textfield type="text" id="percentageUP"
								value="%{MPAData.percentageUP}" name="percentageUP"
								class="lpay_input" oninput="numeric(this)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Wallet</label>
							<s:textfield type="text" id="percentageWL"
								value="%{MPAData.percentageWL}" name="percentageWL"
								class="lpay_input" oninput="numeric(this)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">EMI</label>
							<s:textfield type="text" id="percentageEM"
								value="%{MPAData.percentageEM}" name="percentageEM"
								class="lpay_input" oninput="numeric(this)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">COD/Cash</label>
							<s:textfield type="text" id="percentageCD"
								value="%{MPAData.percentageCD}" name="percentageCD"
								class="lpay_input" oninput="numeric(this)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-6 -->
					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">NEFT/IMPS/RTGS</label>
							<s:textfield type="text" id="percentageNeftOrImpsOrRtgs"
								value="%{MPAData.percentageNeftOrImpsOrRtgs}"
								name="percentageNeftOrImpsOrRtgs" oninput="numeric(this)" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-6 -->
					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">Total Cards Domestic</label>
							<s:textfield type="text" id="percentageDomestic"
								value="%{MPAData.percentageDomestic}" name="percentageDomestic"
								class="lpay_input" oninput="numeric(this)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">Total Cards International</label>
							<s:textfield type="text" id="percentageInternational"
								value="%{MPAData.percentageInternational}"
								name="percentageInternational" oninput="numeric(this)" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="configuration">
				<div class="row">
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>Merchant Type</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-3 mb-20">
						<label for="merchantType" class="checkbox-label unchecked mb-10">
							Regular Merchant <s:if
								test="%{MPAData.merchantType.contains('Regular Merchant')}">
								<!-- <s:checkbox id="merchantType" checked="true" name="merchantType" value="%{MPAData.merchantType}" /> -->
								<input type="checkbox" checked="true" id="merchantType"
									name="merchantType" value="Regular Merchant">
							</s:if> <s:else>
								<input type="checkbox" id="merchantType" name="merchantType"
									value="Regular Merchant">
								<!-- <s:checkbox id="merchantType" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:else>
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<label for="offlineUpi" class="checkbox-label unchecked mb-10">
							Offline UPI <s:if
								test="%{MPAData.merchantType.contains('Offline UPI')}">
								<input type="checkbox" checked="true" name="merchantType"
									id="offlineUpi" value="Offline UPI">
								<!-- <s:checkbox id="offlineUpi" checked="true" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:if> <s:else>
								<input type="checkbox" name="merchantType" id="offlineUpi"
									value="Offline UPI">
								<!-- <s:checkbox id="offlineUpi" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:else>
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<label for="invoicing" class="checkbox-label unchecked mb-10">
							Invoicing <s:if
								test="%{MPAData.merchantType.contains('Invoicing')}">
								<input checked="true" type="checkbox" id="invoicing"
									name="merchantType" value="Invoicing">
								<!-- <s:checkbox id="invoicing" checked="true" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:if> <s:else>
								<input type="checkbox" id="invoicing" name="merchantType"
									value="Invoicing">
								<!-- <s:checkbox id="invoicing" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:else>
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<label for="brandEmi" class="checkbox-label unchecked mb-10">
							<s:if test="%{MPAData.merchantType.contains('Brand EMI')}">
								<input checked="true" type="checkbox" id="brandEmi"
									name="merchantType" value="Brand EMI">
								<!-- <s:checkbox checked="true" id="brandEmi" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:if> <s:else>
								<input type="checkbox" id="brandEmi" name="merchantType"
									value="Brand EMI">
								<!-- <s:checkbox id="brandEmi" name="merchantType" value="%{MPAData.merchantType}" /> -->
							</s:else> Brand EMI <span class="merchant__check"></span>
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>Mode</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="merchant__form_group">
							<div class="mpaFormRowRadio" id="surcharge" data-key="surcharge">
								<label class="paymentGatewayRadio"> TDR <s:if
										test="%{MPAData.surcharge == false}">
										<input checked="true" type="radio" name="surcharge"
											>
									</s:if> <s:else>
										<input type="radio" name="surcharge" value="false"
											>
									</s:else> <span class="checkmark"></span>
								</label> <label class="paymentGatewayRadio"> SURCHARGE <s:if
										test="%{MPAData.surcharge == true}">
										<input checked="true" type="radio" name="surcharge"
											>
									</s:if> <s:else>
										<input type="radio" name="surcharge" value="true"
											>
									</s:else> <span class="checkmark"></span>
								</label>
							</div>
						</div>
						<!-- /.merchant__form_group -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>Integration Type</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="merchant__form_group ">
							<div class="mpaFormRowRadio" data-key="integrationType">

								<label class="paymentGatewayRadio"> Iframe <s:if
										test="%{MPAData.integrationType.contains('iFRAME')}">
										<input type="radio" checked="true" value="iFRAME"
											name="integrationType">
										<span class="checkmark"></span>
									</s:if> <s:else>
										<input type="radio" value="iFRAME" name="integrationType">
										<span class="checkmark"></span>
									</s:else>
								</label> <label class="paymentGatewayRadio"> Merchant Hosted <s:if
										test="%{MPAData.integrationType.contains('MERCHANT HOSTED')}">
										<input type="radio" checked="true" value="MERCHANT HOSTED"
											name="integrationType">
										<span class="checkmark"></span>
									</s:if> <s:else>
										<input type="radio" value="MERCHANT HOSTED"
											name="integrationType">
										<span class="checkmark"></span>
									</s:else>
								</label> <label class="paymentGatewayRadio"> PG Hosted <s:if
										test="%{MPAData.integrationType.contains('PG HOSTED')}">
										<input type="radio" value="PG HOSTED" checked="true"
											name="integrationType">
										<span class="checkmark"></span>
									</s:if> <s:else>
										<input type="radio" value="PG HOSTED" name="integrationType">
										<span class="checkmark"></span>
									</s:else>
								</label> <label class="paymentGatewayRadio"> Not Sure <s:if
										test="%{MPAData.integrationType.contains('NOT_SURE')}">
										<input type="radio" value="NOT_SURE" checked="true"
											name="integrationType">
										<span class="checkmark"></span>
									</s:if> <s:else>
										<input type="radio" value="NOT_SURE" name="integrationType">
										<span class="checkmark"></span>
									</s:else>
								</label>
							</div>
							<!-- /.mpaFormRowRadio -->
						</div>
						<!-- /.merchant__form_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<label for="customizedInvoiceDesign"
							class="checkbox-label unchecked mb-10"> Customized
							Invoice Design <s:checkbox id="customizedInvoiceDesign"
								name="customizedInvoiceDesign"
								value="%{MPAData.customizedInvoiceDesign}" />
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<label for="internationalCards"
							class="checkbox-label unchecked mb-10"> Enable
							International Card <s:checkbox id="internationalCards"
								name="internationalCards" value="%{MPAData.internationalCards}" />
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->

					
					<div class="col-md-3 mb-20">
						
					</div>
					<!-- /.col-md-3 -->
					

					
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="technicalDetails">
				<div class="row">
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">Technical Contact Name</label>
							<s:textfield type="text" id="technicalContactName"
								value="%{MPAData.technicalContactName}"
								name="technicalContactName" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">Mobile Number</label>
							<s:textfield
								maxlength="10"
								type="text"
								id="technicalContactMobile"
								value="%{MPAData.technicalContactMobile}"
								name="technicalContactMobile"
								onkeypress="return isNumber(event)"
								class="lpay_input">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">Email</label>
							<s:textfield type="text" id="technicalContactEmail"
								value="%{MPAData.technicalContactEmail}"
								name="technicalContactEmail" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">Landline</label>
							<s:textfield
								type="text"
								maxlength="10"
								id="technicalContactLandline"
								value="%{MPAData.technicalContactLandline}"
								name="technicalContactLandline"
								onkeypress="return isNumber(event)"
								class="lpay_input">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>Server Details</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="mpaFormRowRadio mpa-input mpa-input-alt"
							id="serverDetails" data-key="serverDetails">
							<label class="pr-30 paymentGatewayRadio"> Owned <s:if
									test="%{MPAData.serverDetails.contains('owned')}">
									<input type="radio" checked="true" value="owned"
										name="serverDetails" onchange="checkAllField(this)">
								</s:if> <s:else>
									<input type="radio" value="owned" name="serverDetails"
										onchange="checkAllField(this)">
								</s:else> <span class="checkmark"></span>
							</label> <label class="paymentGatewayRadio"> Shared/Cloud <s:if
									test="%{MPAData.serverDetails.contains('sharedOrCloud')}">
									<input type="radio" checked="true" value="sharedOrCloud"
										name="serverDetails" onchange="checkAllField(this)">
								</s:if> <s:else>
									<input type="radio" value="sharedOrCloud" name="serverDetails"
										onchange="checkAllField(this)">
								</s:else> <span class="checkmark"></span>
							</label>
						</div>
						<!-- /.mpaFormRowRadio -->
					</div>
					<!-- /.col-md-12 -->
					<div class="share-hosting">
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Company Name</label>
								<s:textfield type="text" id="serverCompanyName"
									value="%{MPAData.serverCompanyName}" name="serverCompanyName"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile</label>
								<s:textfield
									type="text"
									maxlength="10"
									onkeypress="return isNumber(event)"
									id="serverCompanyMobile"
									value="%{MPAData.serverCompanyMobile}"
									name="serverCompanyMobile"
									class="lpay_input">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Address</label>
								<s:textfield type="text" id="serverCompanyAddress"
									value="%{MPAData.serverCompanyAddress}"
									name="serverCompanyAddress" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Landline</label>
								<s:textfield
									type="text"
									maxlength="10"
									onkeypress="return isNumber(event)"
									id="serverCompanyLandline"
									value="%{MPAData.serverCompanyLandline}"
									name="serverCompanyLandline"
									class="lpay_input">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</div>
					<!-- /.share-hosting -->
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>Operating System</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="mpaFormRowRadio mpa-input mpa-input-alt"
							id="operatingSystem" data-key="operatingSystem">
							<label class="paymentGatewayRadio pr-30"> 32 Bit <s:if
									test="%{MPAData.operatingSystem.contains('32')}">
									<input type="radio" checked="true" value="32"
										name="operatingSystem" onchange="checkAllField(this)">
								</s:if> <input type="radio" value="32" name="operatingSystem"
								onchange="checkAllField(this)"> <span class="checkmark"></span>
							</label> <label class="paymentGatewayRadio"> 64 Bit <s:if
									test="%{MPAData.operatingSystem.contains('64')}">
									<input type="radio" checked="true" value="64"
										name="operatingSystem" onchange="checkAllField(this)">
								</s:if> <s:else>
									<input type="radio" value="64" name="operatingSystem"
										onchange="checkAllField(this)">
								</s:else> <span class="checkmark"></span>
							</label>
						</div>
						<!-- /.mpaFormRowRadio -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Backend Technology <small>(JAVA,
									PHP,DotNet etc</small></label>
							<s:textfield type="text" id="backendTechnology"
								value="%{MPAData.backendTechnology}" name="backendTechnology"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Application Server Technology</label>
							<s:textfield type="text" id="applicationServerTechnology"
								value="%{MPAData.applicationServerTechnology}"
								name="applicationServerTechnology" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Production Server IP </label>
							<s:textfield type="text" id="productionServerIp"
								value="%{MPAData.productionServerIp}" name="productionServerIp"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="policyRegularity">
				<div class="row">
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>Do you use any third party to store, transmit or process
								card holder data?</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-5">
						<div class="mpaFormRowRadio" id="third-party-store">
							<label class="paymentGatewayRadio">Yes <input type="radio"
								value="YES" name="1" onchange="checkAllField(this)"> <span
								class="checkmark"></span>
							</label> <label class="paymentGatewayRadio">No <input type="radio"
								value="NO" name="1" onchange="checkAllField(this)"> <span
								class="checkmark"></span>
							</label>
						</div>
						<!-- /.mpaFormRowRadio -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<s:textfield type="text" id="thirdPartyForCardData"
								value="%{MPAData.thirdPartyForCardData}"
								name="thirdPartyForCardData" class="lpay_input"
								placeholder="Provide Name"></s:textfield>
						</div>
						<!-- /.merchant__form_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-12 mb-20">
						<div class="inner-heading">
							<h3>What refunds you allow</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->
					<div class="refund-allowed">
						<div class="col-md-3 mb-20">
							<label class="checkbox-label unchecked mb-10" for="fullRefund">
								<s:if test="%{MPAData.refundsAllowed.contains('Full Refund')}">
									<input type="checkbox" checked="true" id="fullRefund"
										name="refundsAllowed" value="Full Refund">
									<!-- <s:checkbox id="fullRefund" checked="true" name="refundsAllowed" value="Full Refund" /> -->
								</s:if> <s:else>
									<!-- <s:checkbox id="fullRefund" name="refundsAllowed" value="Full Fefund" /> -->
									<input type="checkbox" id="fullRefund" name="refundsAllowed"
										value="Full Refund">
								</s:else> Full Refund <span class="merchant__check"></span>
							</label>
						</div>
						<!-- /.col-md-3 mb-20 -->

						<div class="col-md-3 mb-20">
							<label class="checkbox-label unchecked mb-10" for="partialRefund">
								<s:if
									test="%{MPAData.refundsAllowed.contains('Partial Refund')}">
									<input type="checkbox" checked="true" id="partialRefund"
										name="refundsAllowed" value="Partial Refund">
									<!-- <s:checkbox checked="true" id="partialRefund" name="refundsAllowed" value="Partial Refund" /> -->
								</s:if> <s:else>
									<!-- <s:checkbox id="partialRefund" name="refundsAllowed" value="Partial Refund" /> -->
									<input type="checkbox" id="partialRefund" name="refundsAllowed"
										value="Partial Refund">
								</s:else> Partial Refund <span class="merchant__check"></span>
							</label>
						</div>
						<!-- /.col-md-3 mb-20 -->

						<div class="col-md-3 mb-20">
							<label class="checkbox-label unchecked mb-10" for="invoicing">
								<s:if test="%{MPAData.refundsAllowed.contains('Exchange Only')}">
									<input type="checkbox" checked="true" id="exchangeOnly"
										name="refundsAllowed" value="Exchange Only">
									<!-- <s:checkbox id="exchangeOnly" checked="true" name="refundsAllowed" value="Exchange Only" /> -->
								</s:if> <s:else>
									<input type="checkbox" id="exchangeOnly" name="refundsAllowd"
										value="Exchange Only">
									<!-- <s:checkbox id="exchangeOnly" name="refundsAllowed" value="Exchange Only" /> -->
								</s:else> Exchange Only <span class="merchant__check"></span>
							</label>
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<label class="checkbox-label unchecked mb-10" for="noRefund">
								<s:if test="%{MPAData.refundsAllowed.contains('No Refund')}">
									<input type="checkbox" id="noRefund" checked="true"
										name="refundsAllowed" value="No Refund">
									<!-- <s:checkbox id="noRefund" checked="true" name="refundsAllowed" value="No Refund" /> -->
								</s:if> <s:else>
									<input type="checkbox" id="noRefund" name="refundsAllowed"
										value="No Refund">
									<!-- <s:checkbox id="noRefund" name="refundsAllowed" value="No Refund" /> -->
								</s:else> No Refund <span class="merchant__check"></span>
							</label>
						</div>
						<!-- /.col-md-3 -->
					</div>
					<!-- /.refund-allowed -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<!-- <div class="merchant__forms_block" data-active="uploadDocument">
					<div class="row">
						<div class="col-md-12">
							<button class="lpay_button lpay_button-md lpay_button-primary">Download MPA Upload Files</button>
						</div>
					</div>
				</div> -->
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="eSign">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">ESign Name</label>
							<s:textfield id="esignName" class="lpay_input" name="esignName"
								type="text" value="%{MPAData.esignName}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">YOB</label>
							<s:textfield id="esignYob" class="lpay_input" name="esignYob"
								type="text" value="%{MPAData.esignYOB}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Gender</label>
							<s:textfield id="esignGender" class="lpay_input"
								name="esignGender" type="text" value="%{MPAData.esignGender}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Aadhar Type</label>
							<s:textfield id="esignAadharType" class="lpay_input"
								name="esignAadharType" type="text"
								value="%{MPAData.esignAadhaarType}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Aadhar Last Digit</label>
							<s:textfield
								id="esignUidLastFourDigits"
								class="lpay_input"
								name="esignUidLastFourDigits"
								type="text"
								maxlength="4"
								onkeypress="return isNumber(event)"
								value="%{MPAData.esignUidLastFourDigits}">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Country</label>
							<s:textfield id="esignCountry" class="lpay_input"
								name="esignCountry" type="text" value="%{MPAData.esignCountry}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">State</label>
							<s:textfield id="esignState" class="lpay_input" name="esignState"
								type="text" value="%{MPAData.esignState}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">PIN Code</label>
							<s:textfield
								id="esignPincode"
								class="lpay_input"
								name="esignPincode"
								type="text"
								maxlength="6"
								onkeypress="return isNumber(event)"
								value="%{MPAData.esignPincode}">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<!-- <div class="col-md-4 mb-20">
							<button class="lpay_button lpay_button-md lpay_button-primary lpay_button-with-input">Download e-Sign File</button>
						</div> -->
					<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="action">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Status</label>
							<s:select class="selectpicker" headerValue="ALL"
								list="@com.paymentgateway.commons.util.UserStatusType@values()"
								id="status" name="userStatus" value="%{MPAData.userStatus}" />
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Processing Mode</label>
							<s:select class="selectpicker" headerValue="ALL"
								list="@com.paymentgateway.commons.util.ModeType@values()"
								id="processingmode" name="modeType" value="%{MPAData.modeType}" />
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-12 mb-20">
						<div class="lpay_input_group">
							<label for="">Comments</label>
							<s:textarea id="comments" class="lpay_input lpay_input_textarea"
								rows="5" name="comments" type="text" value="%{MPAData.comments}"
								autocomplete="off" theme="simple" maxlength="200"></s:textarea>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block active-block -->
			<div class="merchant__forms_block" data-active="onBoardDetail">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Pay Id</label>
							<s:textfield
								id="payId"
								readonly="true" class="lpay_input"
								name="payId" data-id="payId" type="text" value="%{MPAData.payId}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Salt</label>
							<s:textfield id="salt" readonly="true" class="lpay_input"
								name="salt" type="text" value="%{salt}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Encryption Key</label>
							<s:textfield id="encKey" readonly="true" class="lpay_input"
								name="encKey" type="text" value="%{encKey}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Reseller Id</label>
							<s:select
								name="resellerId"
								class="selectpicker"
								id="resellerId"
								headerKey=""
								headerValue="Select Reseller"
								list="resellerList"
								listKey="resellerId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Terminal Id</label>
							<s:textfield id="terminalId" class="lpay_input" name="terminalId"
								type="text" value="%{MPAData.terminalId}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						
						<s:if test="%{MPAData.superMerchantId != null}">
							<div class="lpay_input_group">
								<label for="">Super Merchant ID</label>
								<s:textfield id="superMerchantId" readonly="true" class="lpay_input"
									name="superMerchantId" type="text"
									value="%{MPAData.superMerchantId}"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</s:if>
						<s:else>
							<div class="lpay_select_group">
								<label for="">Super Merchant Id</label>
								<s:select
									name="superMerchantId"
									class="selectpicker"
									id="superMerchantId"
									headerKey=""
									headerValue="Select Super Merchant"
									list="superMerchantList"
									listKey="superMerchantId"
									listValue="businessName"
									autocomplete="off"
								/>
							</div>
							<!-- /.lpay_input_group -->
						</s:else>
					</div>
					<!-- /.col-md-4 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Request URL</label>
							<s:textfield id="requestUrl" class="lpay_input" name="requestUrl"
								type="text" value="%{requestUrl}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Registration Date</label>
							<s:textfield id="registrationDate" readonly="true"
								class="lpay_input" name="registrationDate" type="text"
								value="%{MPAData.registrationDate}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Activation Date</label>
							<s:textfield id="activationDate" readonly="true"
								class="lpay_input" name="activationDate" type="text"
								value="%{MPAData.activationDate}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Payment Cycle (In days)</label>
							<s:textfield class="lpay_input" onkeypress="javascript:return isNumber (event)" id="paymentCycle" maxlength="3" name="paymentCycle" value="%{MPAData.paymentCycle}" ></s:textfield>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Default Reporting Currency</label>
							<s:select name="defaultCurrency" id="defaultCurrency"
								list="currencyMap" cssClass="selectpicker" />
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="notificationSetting">
				<div class="row">
					<div class="checkbox-group">
						<div class="col-md-4">
							<label for="activeSms" class="checkbox-label unchecked mb-10">
								Active SMS Service <s:checkbox id="activeSms"
									name="transactionSmsFlag" value="%{MPAData.transactionSmsFlag}" />
							</label>
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="authenticationMailer"
								class="checkbox-label unchecked mb-10"> Customer
								Authentication Emailer <s:checkbox
									name="transactionAuthenticationEmailFlag"
									value="%{MPAData.transactionAuthenticationEmailFlag}" />
							</label>
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="customerMailer"
								class="checkbox-label unchecked mb-10"> Customer
								Transaction Emailer <s:checkbox
									name="transactionCustomerEmailFlag"
									value="%{MPAData.transactionCustomerEmailFlag}" />
							</label>
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
                            <label for="" class="checkbox-label unchecked mb-10">
                                Customer Transaction Failed Emailer <s:checkbox
                                    name="transactionFailedCustomerEmailFlag"
                                    value="%{MPAData.transactionFailedCustomerEmailFlag}" />
                            </label>
                        </div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="" class="checkbox-label unchecked mb-10">
								Customer Transaction SMS <s:checkbox
									name="transactionCustomerSMSFlag"
									value="%{MPAData.transactionCustomerSMSFlag}" />
							</label>
						</div>
						<div class="col-md-4">
                            <label for="" class="checkbox-label unchecked mb-10">
                                Customer Transaction Failed SMS <s:checkbox
                                    name="transactionFailedCustomerSMSFlag"
                                    value="%{MPAData.transactionFailedCustomerSMSFlag}" />
                            </label>
                        </div>

                        <div class="col-md-4">
                            <label for="" class="checkbox-label unchecked mb-10">
                                Customer Refund SMS <s:checkbox
                                    name="transactionRefundCustomerSMSFlag"
                                    value="%{MPAData.transactionRefundCustomerSMSFlag}" />
                            </label>
                        </div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="refundMail" class="checkbox-label unchecked mb-10">
								Customer Refund Emailer <s:checkbox
									name="refundTransactionCustomerEmailFlag"
									value="%{MPAData.refundTransactionCustomerEmailFlag}" />
							</label>
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="transMailer" class="checkbox-label unchecked mb-10">
								Merchant Transaction Emailer <s:checkbox id="transMailer"
									name="transactionEmailerFlag"
									value="%{MPAData.transactionEmailerFlag}" />
							</label>
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="" class="checkbox-label unchecked mb-10">
								Merchant Transaction SMS <s:checkbox
									name="transactionMerchantSMSFlag"
									value="%{MPAData.transactionMerchantSMSFlag}" />
							</label>
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4">
                            <label for="" class="checkbox-label unchecked mb-10">
                                Merchant Transaction Failed SMS <s:checkbox
                                    name="transactionFailedMerchantSMSFlag"
                                    value="%{MPAData.transactionFailedMerchantSMSFlag}" />
                            </label>
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-4">
                            <label for="refundMailer" class="checkbox-label unchecked mb-10">
                                Merchant Transaction Failed Emailer <s:checkbox
                                    name="transactionFailedMerchantEmailFlag"
                                    value="%{MPAData.transactionFailedMerchantEmailFlag}" />
                            </label>
                        </div>
                        <!-- /.col-md-4 -->

                        <div class="col-md-4">
                            <label for="" class="checkbox-label unchecked mb-10">
                                Merchant Refund SMS <s:checkbox
                                    name="transactionRefundMerchantSMSFlag"
                                    value="%{MPAData.transactionRefundMerchantSMSFlag}" />
                            </label>
                        </div>
                        <!-- /.col-md-4 -->
						<div class="col-md-4">
							<label for="refundMailer" class="checkbox-label unchecked mb-10">
								Merchant Refund Emailer <s:checkbox
									name="refundTransactionMerchantEmailFlag"
									value="%{MPAData.refundTransactionMerchantEmailFlag}" />
							</label>
						</div>
					
					</div>
					<!-- /.checkbox-group -->
					<div class="col-md-12"></div>
					<!-- /.col-md-12 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">Merchant Transaction Email</label>
							<s:textfield 
								type="text" 
								id="transactionEmailId"
								class="lpay_input" 
								name="transactionEmailId"
								value="%{MPAData.transactionEmailId}"
								data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
								onblur="blurMsg(event)"
								oninput="checkLength(this, 'Email ID');">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">Merchant Transaction SMS</label>
							<s:textfield type="text" maxLength="10" id="transactionSms" class="lpay_input"
								name="transactionSms" data-reg="^[0-9]{10}$"
								 value="%{MPAData.transactionSms}"
								 oninput="checkLength(this, 'Mobile Number');"
									onkeypress="return isNumber(event)"
								 >
								</s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="systemSetting">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Payment Message Slab</label>
							<s:select name="paymentMessageSlab" id="paymentMessageSlab"
								headerKey="1"
								list="#{'0':'0','1':'1','2':'2','3':'3','4':'4','5':'5'}"
								class="selectpicker" value="%{MPAData.paymentMessageSlab}">
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
								id="merchantHostedFlag" value="%{MPAData.merchantHostedFlag}" />
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<label for="iframePaymentFlag" class="checkbox-label unchecked">
							iframe Payment <s:checkbox name="iframePaymentFlag"
								id="iframePaymentFlag" value="%{MPAData.iframePaymentFlag}" />
						</label>
					</div>
					<div class="col-md-3 mb-20">
						<label for="checkOutJsFlag" class="checkbox-label unchecked">
							Checkout JS <s:checkbox name="checkOutJsFlag"
								id="checkOutJsFlag" value="%{MPAData.checkOutJsFlag}" />
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<label for="surchargeFlag" class="checkbox-label unchecked">
							Surcharge <s:checkbox name="surchargeFlag"
								value="%{MPAData.surchargeFlag}" id="surchargeFlag" />
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<label for="discountFlag" class="checkbox-label unchecked">
							Discount <s:checkbox name="discountingFlag" id="discountFlag"
								value="%{MPAData.discountingFlag}" />
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
                        <label for=loadWalletFlag class="checkbox-label unchecked">
                            Load Wallet <s:checkbox name="loadWalletFlag"
                                id="loadWalletFlag" value="%{MPAData.loadWalletFlag}" />
                        </label>
                    </div>
                    <div class="col-md-3 mb-20">
						<label for="eposMerchant" class="checkbox-label unchecked"> ePOS Merchant 
							<s:checkbox name="eposMerchant" id="eposMerchant" class="textFL_merch5"
									value="%{MPAData.eposMerchant}">
							</s:checkbox>
						</label>
					</div>
					
					<!-- /.col-md-2 -->

					<div class="col-md-3 mb-20">
						<label for="bookingRecord" class="checkbox-label unchecked">
							Booking Report <s:checkbox name="bookingRecord"
								id="bookingRecord" value="%{MPAData.bookingRecord}" />
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
						<label for=eNachReportFlag class="checkbox-label unchecked">
							eNACH Report <s:checkbox name="eNachReportFlag"
								id="eNachReportFlag" value="%{MPAData.eNachReportFlag}" />
						</label>
					</div>
					
					 <div class="col-md-3 mb-20">
						<label for=upiAutoPayReportFlag class="checkbox-label unchecked">
							UPI AutoPay Report <s:checkbox name="upiAutoPayReportFlag"
								id="upiAutoPayReportFlag" value="%{MPAData.upiAutoPayReportFlag}" />
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
						<label for=acceptPostSettledInEnquiry class="checkbox-label unchecked">
							Custom Status Enquiry Flag <s:checkbox name="acceptPostSettledInEnquiry"
								id="acceptPostSettledInEnquiry" value="%{MPAData.acceptPostSettledInEnquiry}" />
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
						<label for="customTransactionStatus" class="checkbox-label unchecked">
							Custom Transaction Status <s:checkbox name="customTransactionStatus"
								id="customTransactionStatus" value="%{MPAData.customTransactionStatus}" />
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
						<label for="capturedMerchantFlag" class="checkbox-label unchecked">
							Custom Capture Report 
							<s:if test="%{MPAData.capturedMerchantFlag == true}">
								<s:checkbox 
								name="capturedMerchantFlag"
								checked="true"
								id="capturedMerchantFlag" 
								value="%{MPAData.capturedMerchantFlag}" />
							</s:if>
							<s:else>
								<s:checkbox
									name="capturedMerchantFlag"
									id="capturedMerchantFlag"
									value="%{MPAData.capturedMerchantFlag}"
								/>
							</s:else>
						</label>
					</div>					
					
					
                    
                    <div class="col-md-3 mb-20">
                        <label for="paymentAdviceFlag" class="checkbox-label unchecked">
                            Payment Advice Email <s:checkbox name="paymentAdviceFlag"
                                id="paymentAdviceFlag" value="%{MPAData.paymentAdviceFlag}" />
                        </label>
                    </div>

					<div class="col-md-3 mb-20">
						<label for="retailMerchantFlag" class="checkbox-label unchecked">
							Marketplace Settlement Report
							<s:if test="%{MPAData.retailMerchantFlag == true}">
								<s:checkbox 
								name="retailMerchantFlag"
								checked="true"
								id="retailMerchantFlag" 
								value="%{MPAData.retailMerchantFlag}" />
							</s:if>
							<s:else>
								<s:checkbox
									name="retailMerchantFlag"
									id="retailMerchantFlag"
									value="%{MPAData.retailMerchantFlag}"
								/>
							</s:else>
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
                        <label for="lyraPay" class="checkbox-label unchecked">
                            Lyra Pay <s:checkbox name="lyraPay"
                                id="lyraPay" value="%{MPAData.lyraPay}" />
                        </label>
                    </div>
                    
                     <div class="col-md-3 mb-20">
                        <label for="non3dsTxn" class="checkbox-label unchecked">
                            Non 3DS Txn <s:checkbox name="non3dsTxn"
                                id="non3dsTxn" value="%{MPAData.non3dsTxn}" />
                        </label>
                    </div>
					
					<div class="clearfix"></div>
					<!-- /.clearfix -->
					<div class="col-md-3 mb-20">
						<label for="retryTransactionFlag" class="checkbox-label unchecked">
							Retry Transaction <s:checkbox name="retryTransactionCustomeFlag"
								id="retryTransactionFlag"
								value="%{MPAData.retryTransactionCustomeFlag}" />
						</label>
						<div class="lpay_select_group">
							<label for="">Number of Retry</label>
							<s:select name="attemptTrasacation" id="attemptTrasacation"
								headerKey="1" list="#{'1':'1','2':'2','3':'3','4':'4','5':'5'}"
								class="selectpicker" value="%{MPAData.attemptTrasacation}"></s:select>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
                        <label for="non3dsTxn" class="checkbox-label unchecked">
                            Auto Refund(Post Settled Txn.) <s:checkbox name="autoRefund"
                                id="autoRefund" value="%{MPAData.autoRefund}" />
                        </label>
                    </div>
					
					<div class="col-md-3 mb-20">
						<label for="whiteListReturnUrlFlag" class="checkbox-label unchecked">
							White List Return URL	 <s:checkbox name="whiteListReturnUrlFlag"
								id="whiteListReturnUrlFlag"
								value="%{MPAData.whiteListReturnUrlFlag}" />
						</label>
						<div class="lpay_input_group d-none">
							<s:textfield
								type="text"
								data-reg="^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$"
								id="whiteListReturnUrl"
								name="whiteListReturnUrl"
								value="%{MPAData.whiteListReturnUrl}"
								autocomplete="off"
								oninput="checkLength(this, 'Invalid URL')"
								class="lpay_input">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->

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
								value="%{MPAData.saveVPAFlag}" />
						</label>
						<div class="lpay_select_group save-flag-select">
							<label for="" class="d-none"> VPA Save Param</label>
							<s:select data-link="saveVPAFlag" disabled="true" name="vpaSaveParam" id="saveVpaParam"
								list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
								class="selectpicker" value="%{MPAData.vpaSaveParam}">
							</s:select>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20 save-flag">
						<label for="saveNBFlag" class="checkbox-label unchecked mb-10">
							<s:checkbox name="saveNBFlag" id="saveNBFlag"
								value="%{MPAData.saveNBFlag}" /> Save NB Bank <span
							class="merchant__check"></span>
						</label>
						<div class="lpay_select_group save-flag-select">
							<label for="nbSaveParam" class="d-none">NB Save Param</label>
							<s:select name="nbSaveParam" disabled="true" data-link="saveNBFlag" id="nbSaveParam" headerKey="1"
								list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
								style="height:35px; margin-left: -35%;" class="selectpicker"
								value="%{MPAData.nbSaveParam}">
							</s:select>
						</div>
						<!-- /.merchant__form_group -->
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20 save-flag">
						<label for="saveWLFlag" class="checkbox-label unchecked mb-10">
							<s:checkbox name="saveWLFlag" id="saveWLFlag"
								value="%{MPAData.saveWLFlag}" /> Save wallet <span
							class="merchant__check"></span>
						</label>
						<div class="lpay_select_group save-flag-select">
							<label for="wlSaveParam" class="d-none">WL Save Param</label>
							<s:select name="wlSaveParam" disabled="true" data-link="saveWLFlag" id="wlSaveParam" headerKey="1"
								list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
								style="height:35px; margin-left: -35%;" class="selectpicker"
								value="%{MPAData.wlSaveParam}">
							</s:select>

						</div>
						<!-- /.merchant__form_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20 save-flag">
						<label for="cardSaveParamCheck" class="checkbox-label unchecked mb-10">
							<s:checkbox id="cardSaveParamCheck" name="expressPay"
								value="%{MPAData.expressPay}" /> Express Pay <span
							class="merchant__check"></span>
						</label>
						<div class="lpay_select_group save-flag-select">
							<label for="cardSaveParam" class="d-none">Card Save Param</label>
							<s:select disabled="true" data-link="cardSaveParam" name="cardSaveParam" id="cardSaveParam"
								headerKey="1"
								list="#{'CUST_EMAIL':'CUST_EMAIL','CUST_ID':'CUST_ID','CUST_PHONE':'CUST_PHONE'}"
								class="selectpicker" value="%{MPAData.expressPayParameter}">
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
                                id="merchantInitiatedDirectFlag" class="impsFlag" data-id="merchantInitiatedDirectFlag" value="%{MPAData.merchantInitiatedDirectFlag}" />
                        </label>
                    </div>
 

					<div class="col-md-3 mb-20">
						<label for="nodalReportFlag" class="checkbox-label unchecked">
							Nodal Report <s:checkbox name="nodalReportFlag"
								id="nodalReportFlag" value="%{MPAData.nodalReportFlag}" />
						</label>
					</div>

					<div class="col-md-3 mb-20">
						<label for="virtualAccountFlag" class="checkbox-label unchecked">
							eCollection Report <s:checkbox name="virtualAccountFlag"
								id="virtualAccountFlag" value="%{MPAData.virtualAccountFlag}" />
						</label>
					</div>
					<div class="col-md-3 mb-20">
						<label for="topupFlag" class="checkbox-label unchecked">
							Topup Flag <s:checkbox name="topupFlag"
								id="topupFlag" value="%{MPAData.topupFlag}" />
						</label>
					</div>
					<div class="col-md-3 mb-20">
						<label for="statementFlag" class="checkbox-label unchecked">
							Account Statement Report<s:checkbox name="statementFlag"
								id="statementFlag" value="%{MPAData.statementFlag}" />
						</label>
					</div>

					<div class="col-md-3 mb-20">
						<label for=allowNodalPayoutFlag class="checkbox-label unchecked">
							Nodal Topup Payout  <s:checkbox name="allowNodalPayoutFlag"
								id="allowNodalPayoutFlag" value="%{MPAData.allowNodalPayoutFlag}" />
						</label>
					</div>
					
					<div class="col-md-3 mb-20">
						<label for=allowPayoutUpdateStatus class="checkbox-label unchecked">
							Payout Update Status <s:checkbox name="allowPayoutUpdateStatus"
								id="allowPayoutUpdateStatus" value="%{MPAData.allowPayoutUpdateStatus}" />
						</label>
					</div>
					<div class="col-md-3 mb-20">
						<label for=allowECollectionFee class="checkbox-label unchecked">
							E-Collection Fee <s:checkbox name="allowECollectionFee"
								id="allowECollectionFee" value="%{MPAData.allowECollectionFee}" />
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
                                id="accountVerificationFlag" value="%{MPAData.accountVerificationFlag}" />
                        </label>
                    </div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-3 mb-20">
                        <label for="vpaVerificationFlag" class="checkbox-label unchecked">
                            VPA Verification
							<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" 
								data-id="vpaVerificationFlag" value="%{MPAData.vpaVerificationFlag}" />
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
							<h3>Custom Hosting Setting</h3>
						</div>
						<!-- /.inner-heading -->
					</div>
					<!-- /.col-md-12 -->

					<div class="col-md-3 mb-20 hosted-url">
						<label for="allowCustomHostedUrl" class="checkbox-label unchecked mb-10"> Allow Custom Hosted URL 
							<s:checkbox name="allowCustomHostedUrl" id="allowCustomHostedUrl" class="textFL_merch5"
									value="%{MPAData.allowCustomHostedUrl}">
							</s:checkbox>
						</label>
						<div class="lpay_input_group">
							<label for="">Custom Hosted URL</label>
							<s:textfield
								type="text"
								id="allowCustomHostedText"
								name="customHostedUrl"
								value="%{MPAData.customHostedUrl}"
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
								value="%{MPAData.skipOrderIdForRefund}">
							</s:checkbox>
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">

						<label for="allowDuplicateSaleOrderId"
							class="checkbox-label unchecked mb-10"> Allow Duplicate
							Sale <s:checkbox name="allowDuplicateSaleOrderId"
								id="allowDuplicateSaleOrderId" headerKey="1"
								class="textFL_merch5"
								value="%{MPAData.allowDuplicateSaleOrderId}">
							</s:checkbox>
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<label for="allowDuplicateRefundOrderId"
							class="checkbox-label unchecked mb-10"> Allow Duplicate
							Refund <s:checkbox name="allowDuplicateRefundOrderId"
								id="allowDuplicateRefundOrderId" headerKey="1"
								class="textFL_merch5"
								value="%{MPAData.allowDuplicateRefundOrderId}">
							</s:checkbox>
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<label for="allowDuplicateSaleOrderIdInRefund"
							class="checkbox-label unchecked mb-10"> Allow Sale In
							Refund <s:checkbox name="allowDuplicateSaleOrderIdInRefund"
								id="allowDuplicateSaleOrderIdInRefund" headerKey="1"
								class="textFL_merch5"
								value="%{MPAData.allowDuplicateSaleOrderIdInRefund}">
							</s:checkbox>
						</label>
					</div>
					<!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20">
						<label for="allowDuplicateRefundOrderIdSale"
							class="checkbox-label unchecked mb-10"> Allow Refund In
							Sale <s:checkbox name="allowDuplicateRefundOrderIdSale"
								id="allowDuplicateRefundOrderIdSale" headerKey="1"
								class="textFL_merch5"
								value="%{MPAData.allowDuplicateRefundOrderIdSale}">
							</s:checkbox>
						</label>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<label for="allowDuplicateNotSaleOrderId"
							class="checkbox-label unchecked mb-10"> Unique Order Id <s:checkbox name="allowDuplicateNotSaleOrderId"
								id="allowDuplicateNotSaleOrderId" headerKey="1"
								class="textFL_merch5"
								value="%{MPAData.allowDuplicateNotSaleOrderId}">
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
									value="%{MPAData.smtMerchant}">
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
							<s:if test="%{MPAData.logoFlag == true}">
								<s:checkbox
									name="logoFlag"
									checked="true"
									id="logoFlag"
									data-id="selfLogo"
									value="%{MPAData.logoFlag}"
								/>
							</s:if>
							<s:else>
								<s:checkbox
									name="logoFlag"
									id="logoFlag"
									data-id="selfLogo"
									value="%{MPAData.logoFlag}"
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
						<s:hidden name="merchantLogo" value="%{merchantLogo}" id="value-merchantLogo" />
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
									name="logoImage"
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
								value="%{MPAData.logoName}"
								autocomplete="off"
								class="lpay_input">
							</s:textfield>
							<s:hidden type="hidden" value="%{MPAData.logoName}"></s:hidden>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20 d-none" id="allow-logo-pg">
						<label for="allowLogoInPgPage" class="checkbox-label unchecked">
							Allow Logo in PG Page
							<s:if test="%{MPAData.allowLogoInPgPage == 'true'}">
								<s:checkbox
									name="allowLogoInPgPage"
									checked="true"
									id="allowLogoInPgPage"
									value="%{MPAData.allowLogoInPgPage}"
								/>
							</s:if>
							<s:else>
								<s:checkbox
									name="allowLogoInPgPage"
									id="allowLogoInPgPage"
									value="%{MPAData.allowLogoInPgPage}"
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
								value="%{MPAData.codName}"
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
							<s:checkbox name="allowPartSettle" id="allowPartSettle" value="%{MPAData.allowPartSettle}" />
						</label>
						<div class="lpay_input_group">
							<label for="">Annual Turnover</label>
							<s:property value="%{MPAData.partAnnualTurnover}"></s:property>
							<s:textfield
								class="lpay_input"
								id="settleannualTurnover"
								data-id="annualTurnover"
								value="%{MPAData.partAnnualTurnover}"
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
							<s:checkbox name="allowSubtractValue" id="allowSubtractValue" value="%{MPAData.allowSubtractValue}" />
						</label>
						<div class="lpay_input_group">
							<label for="">Deviation</label>
							<s:textfield type="text" class="lpay_input" id="deviation"
								name="deviation" value="%{MPAData.deviation}" placeholder="0"
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
								value="%{MPAData.sameLimitFlag}">
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
								value="%{MPAData.mCC}" autocomplete="off"
								OnKeypress="javascript:return isNumber(event,this.value)">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->

					<s:if test="%{MPAData.superMerchantId == null || MPAData.superMerchantId == ''}">
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
									<s:checkbox name="allowQRScanFlag" id="allowQRScanFlag" data-id="allowQRScanFlag" value="%{MPAData.allowQRScanFlag}" />
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
									<s:checkbox name="allowUpiQRFlag" id="allowUpiQRFlag" data-id="allowUpiQRFlag" value="%{MPAData.allowUpiQRFlag}" />
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
								id="customerQrFlag" value="%{MPAData.customerQrFlag}" />
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
							<label for="">Callback URL</label>
							<s:textfield id="callBackUrl" class="lpay_input"
								name="callBackUrl" type="text" value="%{MPAData.callBackUrl}" autocomplete="off"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mt-20">
                        <label for="callBackFlag" class="checkbox-label unchecked">
                            Status Enquiry Callback Flag <s:checkbox name="callBackFlag"
                                id="callBackFlag" value="%{MPAData.callBackFlag}" />
                        </label>
                    </div>
					<div class="col-md-4 mt-20">
                        <label for="allCallBackFlag" class="checkbox-label unchecked">
                            All Payment Type Callback Flag <s:checkbox name="allCallBackFlag"
                                id="allCallBackFlag" value="%{MPAData.allCallBackFlag}" />
                        </label>
                    </div>
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="downloads">
				<div class="row">
					<div class="col-md-12">
						<a href="#" data-info="mpa"
							class="mpaDownload lpay_button lpay_button-md lpay_button-secondary">Download
							MPA Files</a> 
<!-- 							<a href="#" data-info="esign" -->
<!-- 							class="mpaDownload lpay_button lpay_button-md lpay_button-secondary">Download Agreement Copy</a> -->
						<s:if test="%{MPAData.makerFileName != null}">
							<a href="#" id="makerDownload"
								class="lpay_button lpay_button-md lpay_button-secondary">Download
								File</a>
						</s:if>
						<s:if test="%{MPAData.checkerFileName != null}">
							<a href="#" id="checkerDownload"
								class="lpay_button lpay_button-md lpay_button-secondary">Download
								File</a>
						</s:if>
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="maker">
				<div class="row">
					<s:if test="%{MPAData.makerStatus != null}">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Maker Status</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Status</label>
								<s:textfield type="text" readonly="true" id="makerStatus"
									class="lpay_input" name="makerStatus"
									value="%{MPAData.makerStatus}" autocomplete="off"
									OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-3 -->
						<s:if test="%{MPAData.makerComments != null}">
							<div class="col-md-9 mb-20">
								<div class="lpay_input_group">
									<label for="">Comment</label>
									<s:textfield type="text" readonly="true" id="makerComment"
										class="lpay_input" name="makerComment"
										value="%{MPAData.makerComments}" autocomplete="off"
										OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-6 -->
						</s:if>
						<s:else>
							<div class="col-md-6 mb-20">
								<div class="lpay_input_group">
									<label for="">Comment</label>
									<s:textfield type="text" readonly="true" id="makerComment"
										class="lpay_input" name="makerComment" value="None"
										autocomplete="off"
										OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-6 -->
						</s:else>
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Merchant Checks</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Company Name Verification:
									<s:if test="%{MPAData.validCompanyName == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->

						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									CIN Verification:
									<s:if test="%{MPAData.validCin == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->

						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Company PAN Verification:
									<s:if test="%{MPAData.validPan == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Director 1 PAN Verification:
									<s:if test="%{MPAData.director1PanVerified == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Director 2 PAN Verification:
									<s:if test="%{MPAData.director2PanVerified == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Bank Verification:
									<s:if test="%{MPAData.accountVerification == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									GST Verification:
									<s:if test="%{MPAData.gstVerification == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 -->

						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Download</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-12">
							<s:if test="%{MPAData.makerFileName != null}">
								<a href="#" id="makerDownload"
									class="lpay_button lpay_button-md lpay_button-secondary">Download
									File</a>
							</s:if>
						</div>
						<!-- /.col-md-3 -->
					</s:if>
				</div>
				<!-- /.row -->
			</div>
		<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block surchargeTextTab"
				data-active="checker">
				<div class="row">
					<s:if test="%{MPAData.checkerStatus != null}">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Checker Status</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Status</label>
								<s:textfield type="text" readonly="true" id="checkerStatus"
									class="lpay_input" name="checkerStatus"
									value="%{MPAData.checkerStatus}" autocomplete="off"
									OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /. mb-20 -->
						<s:if test="%{MPAData.checkerComments != null}">
							<div class="col-md-9 mb-20">
								<div class="lpay_input_group">
									<label for="">Comment</label>
									<s:textfield type="text" readonly="true" id="checkerComment"
										class="lpay_input" name="checkerComment"
										value="%{MPAData.checkerComments}" autocomplete="off"
										OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-6 -->
						</s:if>
						<s:else>
							<div class="col-md-6 mb-20">
								<div class="lpay_input_group">
									<label for="">Comment</label>
									<s:textfield type="text" readonly="true" id="checkerCommnet"
										class="lpay_input" name="checkerCommnet" value="None"
										autocomplete="off"
										OnKeypress="javascript:return isNumber(event,this.value)"></s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-6 -->
						</s:else>
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Merchant Checks</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Company Name Verification:
									<s:if test="%{MPAData.validCompanyName == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 -->

						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									CIN Verification:
									<s:if test="%{MPAData.validCin == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->


						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Company PAN Verification:
									<s:if test="%{MPAData.validPan == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Director 1 PAN Verification:
									<s:if test="%{MPAData.director1PanVerified == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Director 2 PAN Verification:
									<s:if test="%{MPAData.director2PanVerified == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									Bank Verification:
									<s:if test="%{MPAData.accountVerification == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->
						<div class="col-md-3 mb-20">
							<div class="varification-box">
								<div class="varification">
									GST Verification:
									<s:if test="%{MPAData.gstVerification == true}">
										<span class="var-success">Verified</span>
									</s:if>
									<s:else>
										<span class="var-failed">Failed</span>
									</s:else>
								</div>
								<!-- /.varification -->
							</div>
							<!-- /.varification-box -->
						</div>
						<!-- /.col-md-3 mb-20 -->

						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Downloads</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<div class="col-md-12">
							<s:if test="%{MPAData.checkerFileName != null}">
								<a href="#" id="checkerDownload"
									class="lpay_button lpay_button-md lpay_button-secondary">Download
									File</a>
							</s:if>
						</div>
						<!-- /.col-md-3 mb-20 -->
					</s:if>
				</div>
				<!-- /.row -->
			</div>
			<!-- merchant__forms_block -->
			<div class="merchant__forms_block surchargeTextTab"
				data-active="surchargeTextTab">
				<div class="row">
					<div class="col-md-3 form-group" id="col-paymentMethod">
						<label for="paymentMethod">Payment Method</label> <select
							name="paymentMethod" id="paymentMethod"
							class="merchant__form_control">
							<option value="">Select Payment Type</option>
							<option value="CC">Credit Card</option>
							<option value="DC">Debit Card</option>
							<option value="NB">Net Banking</option>
							<option value="UPI">UPI</option>
							<option value="COD">COD</option>
							<option value="EMICC">EMI CC</option>
							<option value="EMIDC">EMI DC</option>
							<option value="WL">Wallet</option>
						</select>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 form-group" id="col-paymentRegion">
						<label for="paymentRegion">Payment Region</label> <select
							name="paymentRegion" id="paymentRegion"
							class="merchant__form_control">
							<option value="ALL">ALL</option>
							<option value="DOMESTIC">Domestic</option>
							<option value="INTERNATIONAL">International</option>
						</select>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 form-group" id="col-cardHolderType">
						<label for="cardHolderType">Card Holder Type</label> <select
							name="cardHolderType" id="cardHolderType"
							class="merchant__form_control">
							<option value="ALL">ALL</option>
							<option value="CONSUMER">Consumer</option>
							<option value="COMMERCIAL">Commercial</option>
							<option value="PREMIUM">Premium</option>
						</select>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 form-group" id="col-mopType">
						<label for="mopType">Mop Type</label> <select name="mopType"
							id="mopType" class="merchant__form_control">
							<option value="ALL">ALL</option>
						</select>
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 form-group" id="col-amount">
						<label for="amount">Amount</label> <select name="amount"
							id="amount" class="merchant__form_control">
							<option value="ALL">ALL</option>
							<option value="0.01-1000.00">0.01 - 1000.00</option>
							<option value="1000.01-2000.00">1000.01 - 2000.00</option>
							<option value="2000.01-1000000.00">2000.01 - 1000000.00</option>
						</select>
					</div>
					<!-- /.col-md-3 -->
				</div>
				<!-- /.row -->
				<div class="row">
					<div class="col-xs-12 form-group">
						<textarea name="surchargeText" placeholder="Text"
							id="surchargeText" class="merchant__form_control"></textarea>
					</div>
					<!-- /.col-xs-12 -->
				</div>
				<!-- /.row -->
				<div class="row">
					<div
						class="col-xs-12 d-flex justify-content-end surchargeText-create-section">
						<input type="submit" value="Submit"
							class="btn btn-primary px-20 mr-0 mb-0"
							id="btn-submit-surchargeText">
					</div>
					<!-- /.col-xs-12 -->
					<div
						class="col-xs-12 d-flex justify-content-end surchargeText-edit-section d-none">
						<input type="submit" value="Update" id="btn-edit-surchargeText"
							class="btn btn-primary px-20 mr-0 mb-0"> <a href="#"
							id="btn-cancel-surchargeText"
							class="btn btn-primary px-20 mr-0 mb-0 d-flex align-items-center">Cancel</a>
						<a href="#" id="btn-delete-surchargeText"
							class="btn btn-primary px-20 mr-0 mb-0 d-flex align-items-center">Delete</a>
					</div>
				</div>
				<!-- /.row -->

				<div class="row mt-20">
					<div class="col-xs-12">
						<div class="surchargeText-table-wrapper">
							<table class="table full-width" id="table-surchargeText">
								<thead>
									<tr class="bg-color-primary color-white">
										<th>Payment Method</th>
										<th>Payment Region</th>
										<th>Card Holder Type</th>
										<th>MOP</th>
										<th>Text</th>
										<th>Amount</th>
										<th>Action</th>
									</tr>
								</thead>
								<tbody></tbody>
							</table>
						</div>
					</div>
					<!-- /.col-xs-12 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms mt-30 -->
			<div class="merchant__forms_block" data-active="documentUpload">
					<div class="row">

						<div class="col-md-5">
							<div class="row">
								<div class="col-md-12 mb-20">
									<div class="lpay_select_group">
										<label for="">Select File Type</label>
										<select name="mpaFileType" id="mpaFileType"></select>
									</div>
									<!-- /.lpay_select_group -->  
								</div>
								<!-- /.col-md-3 -->	
								<div class="clearfix"></div>
								<!-- /.clearfix -->
		
								<div class="col-md-12">
										<label for="upload-input" class="lpay-upload single-account-input" data-status="">
											<input type="file" accept=".pdf, .png, .jpg" name='file' class="lpay_upload_input" id="fileUploader">
											<s:hidden name="setupFlag" value="true" />
											<!-- <input type="file" data-var='csvFile' accept=".csv" onchange="removeError(this)" data-required="true" name="csvFile" id="upload-input"
												class="lpay_upload_input bulk-invoice"> -->
											<div class="default-upload">
												<h3>Upload Document PDF/JPG/PNG <span>File Size: <b>3 MB</b></span></h3>
												<img src="../image/image_placeholder.png" class="img-responsive"
													id="placeholder_img" alt="">
											</div>
											<!-- /.default-upload -->
											<div class="upload-status">
												<div class="success-wrapper upload-status-inner d-none">
													<div class="success-icon-box status-icon-box">
														<img src="../image/tick.png" alt="">
													</div>
													<div class="success-text-box">
														<h3>Upload Successfully</h3>
														<div class="fileInfo">
															<span class="fileName"></span>
														</div>
														<!-- /.fileInfo -->
													</div>
													<!-- /.success-text-box -->
												</div>
												<!-- /.success-wraper -->
												<div class="error-wrapper upload-status-inner d-none">
													<div class="error-icon-box status-icon-box">
														<img src="../image/wrong-tick.png" alt="">
													</div>
													<div class="error-text-box">
														<h3 id="error-name">Upload Failed</h3>
														<div class="fileInfo">
															<div class="fileName">File size too Long.</div>
														</div>
														<!-- /.fileInfo -->
													</div>
													<!-- /.success-text-box -->
												</div>
												<!-- /.success-wraper -->
											</div>
											<!-- /.upload-success -->
											<span class="error-field"></span>
										</label>
								</div>
								<!-- /.col-md-3 -->
							</div>
							<!-- /.row -->
						</div>
						<!-- /.col-md-6 -->

						<div class="col-md-7">
							<div id="uploaded-file">
							</div>
							<!-- /#uploaded-file -->
							<div class="zip-download d-none" id="zip-download">
								<a href="#" data-info="mpa"
								class="mpaDownload lpay_button lpay_button-md lpay_button-secondary">Download All Files</a> 
							</div>
							<!-- /.zip-download -->
						</div>
						<!-- /.col-md-6 -->
						
						<div class="col-md-12 mt-15 d-none" id="download-mpa">
							<div class="inner-heading">
								<h3>Download</h3>
							</div>
							<!-- /.inner-heading -->
							<span class="lpay_button d-inline-block lpay_button-md lpay_button-secondary mt-15 downloadMpa" data-type="MPA" style="display: inline-block;margin-top: 15px !important">Download All MPA Files</span>
						</div>
						<!-- /.col-md-12 -->					
					</div>
				
			</div>
			<!-- /.merchant__forms_block -->
			<div class="row">
				<div class="col-md-12 text-center button-wrapper button_wrapper">
					<s:a class="lpay_button lpay_button-md lpay_button-primary" action='merchantList'>Back</s:a>
					<button id="btnSave" class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</s:form>
	</div>
	<!-- /.merchant__forms mt-30 --> </section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:form action="downloadSingleMpaFileAction" id="downloadSingleMpaFileAction">

	</s:form>

	<s:form method="POST" id="checkerFileDownload"
		action="statusFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="checkerFileName" value="%{MPAData.checkerFileName}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
	</s:form>

	<s:form method="POST" id="makerFileDownload"
		action="statusFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="makerFileName" value="%{MPAData.makerFileName}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
	</s:form>

	<s:form method="post" id="mpaFileDownload"
		action="mpaMerchantFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
		<s:hidden name="fileNameType" id="fileType" />
	</s:form>

	<s:form method="POST" action="qrImageDowloadAction" id="qrCodeDownloadForm">
		<s:hidden name="payId" value="%{MPAData.payId}" />
		<s:hidden name="qrType" id="qrType" />
	</s:form>

	<script>
		$(document).on('change', '.btn-file :file', function() {
			var input = $(this),
				numFiles = input.get(0).files ? input.get(0).files.length : 1,
				label = input.val().replace(/\\/g, '/').replace(/.*\//, '');

			input.trigger('fileselect', [ numFiles, label ]);
		});

		$(document).ready(function() {

			var _getHosted = document.querySelector("#allowCustomHostedText").value;
			if(_getHosted == ""){
				document.querySelector("#allowCustomHostedText").value = "https://";
			}

			$("body").on("click", ".downloadQrCode", function(e) {
				e.preventDefault();
				
				var qrType = $(this).attr("data-download");
				$("#qrType").val(qrType);
				$("#qrCodeDownloadForm").submit();
			});

			if($("[data-id=paymentCycle]").val() == ""){
				$("#paymentCycle").val(21);
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

			$(".subtract-value input[type='checkbox']").on("change", function(e) {
				if ($(this).is(":checked")) {
					$(this).closest(".partSettle").addClass("subTract");
					checkDailyLimit();
				} else {
					$(this).closest(".partSettle").removeClass("subTract");
					checkDailyLimit();
				}
			});

			var _registration = document.querySelector("#registrationNumber").value;
			if(_registration != ''){
				document.querySelector("#cin").value = _registration;
			}

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



			var _getAllRefund = document.querySelectorAll("[data-target='refundSetting']");
			_getAllRefund.forEach(function(index, array, element){
				
				var _getNew = index.querySelector("input[type='checkbox']");
				// console.log(_getLabel);
				_getNew.addEventListener('click', function(e){
					console.log(e.target.id);
					if(e.target.id == "oneTimeRefundAmount"){
						document.querySelector("[data-target='refundAvailableLimit']").classList.remove("d-none");
					}else{
						document.querySelector("[data-target='refundAvailableLimit']").classList.add("d-none");
					}
					_getAllRefund.forEach(function(ind, arr, ele){
						
						var _getLabel = ind.querySelector("label");
						
						ind.querySelector(".lpay_input_group").classList.add("d-none");
						_getLabel.classList.remove("checkbox-checked");
						_getLabel.querySelector("input[type='checkbox']").checked = false;

					})
					var _getNewData = _getNew.closest("label");
					_getNewData.classList.add("checkbox-checked");
					_getNewData.querySelector("input[type='checkbox']").checked = true;
					_getNew.querySelector(".lpay_input_group").classList.remove("d-none");
				})
			})
			 

			$("#extraRefundAmount").on("change", function(e) {
				var _extraRefundCheck = $(this).is(":checked");
				if (_extraRefundCheck) {
					$(".extra-refund").removeClass("d-none");
					$("#extraRefundLimit").val("0.0");
				} else {
					$(".extra-refund").addClass("d-none");
					$("#extraRefundLimit").val("0.0");
				}
			});
			
			$("#oneTimeRefundAmount").on("change", function(e) {
				var _extraRefundCheck = $(this).is(":checked");
				if (_extraRefundCheck) {
					$(".oneTime-refund").removeClass("d-none");
					$("#oneTimeRefundLimit").val("0.0");
				} else {
					$(".oneTime-refund").addClass("d-none");
					$("#oneTimeRefundLimit").val("0.0");
				}
			});
			
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

			$('.btn-file :file').on('fileselect', function(event, numFiles, label) {
				var input = $(this).parents('.input-group').find(':text'),
					log = numFiles > 1 ? numFiles + ' files selected' : label;

				if (input.length) {
					input.val(log);
				} else {
					if (log)
						alert(log);
				}
			});
		});

		function checkMobLength() {
			var mobPattern = /^[7-9][0-9]{9}/;
			var mobileNo = document.getElementById("mobile").value;
			var mobileNo = mobileNo.trim();

			if (!mobileNo.match(mobPattern)) {
				document.getElementById("mobileValid").style.display = "block";
				document.getElementById("btnSave").disabled = true;
			} else {
				document.getElementById("mobileValid").style.display = "none";
				document.getElementById("btnSave").disabled = false;
			}
		}

		// CALL THIS FUNCTION IN ONKEYUP---FOR MERCHANT TRANSACTION EMAIL
		function validtransactionEmail() {
			var emailregex = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
			var transactionEmail = document.getElementById("transactionEmailId").value;
			if (transactionEmail.trim() !== "") {
				if (!transactionEmail.match(emailregex)) {
					document.getElementById("transactionError").style.display = "block";
					document.getElementById("btnSave").disabled = true;
				} else {
					document.getElementById("transactionError").style.display = "none";
					document.getElementById("btnSave").disabled = false;
				}
			} else {
				document.getElementById("transactionError").style.display = "none";
				document.getElementById("btnSave").disabled = false;
			}
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
	<script src="../js/merchantAccountSetup.js"></script>
	<script src="../js/mechant-script.js"></script>
	<script src="../js/horizontal-scrolling-nav.js"></script>
</body>
</html>