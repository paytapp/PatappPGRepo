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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Vendor Account Setup</title>
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<!-- <link href="../css/custom.css" rel="stylesheet" type="text/css" /> -->
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

<s:property value="%{MPAData.logoName}"></s:property>

<style>

.merchantAssign-filter {
    display: inline-block;
    width: 100%;

}

.merchantDetail-action {
    display: flex;
    flex-wrap: wrap;
    margin: 0 -10px !important;
}

.merchantDetail-action .merchantDetail-box{
    width: 100%;
    max-width: 23%;
    background-color: #f5f5f5;
    padding: 15px;
    margin: 10px;
    border-radius: 5px;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    -ms-border-radius: 5px;
    -o-border-radius: 5px;
}

.merchantDetail-action .merchantDetail-box:nth-child(4n){
    border: none;
}

.merchantDetail-box span{
    margin-bottom: 8px;
    display: block;
    padding-bottom: 10px;
    border-bottom: 1px solid #ddd;
}

.merchantDetail-box div{
    color: #000;
    font-size: 13px;
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

		// COUNTRY DEFAULT SELECTED
		populateCountries("country", "state");
		populateStates("country", "state");
		var dataCountry = $("#dataCountry").val();
		$("#country").selectpicker();
		$("#country").selectpicker('val', $("#mpaTradingCountry").val());
		if (dataCountry !== "") {
			$("#country").val(dataCountry);
		}

		setTimeout(function(e){
			$("#country").trigger("change");
			$("#country").selectpicker('val', $("#mpaTradingCountry").val());
			populateStates("country", "state", false);
			$("#state").selectpicker('refresh');
			$("#state").selectpicker('val', $("#mpaTradingState").val());
		}, 2000);

		var dataState = $("#state").val();
		if (dataState !== "") {
			$("#state").val(dataState);
		}
		$("#country").on("change", function(e) {
			$("#state").next("div").removeClass("disabled");
			// populateStates("country", "state", true);
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

	function CollapseAll(theClass, id) {
		var alldivTags = new Array();
		alldivTags = document.getElementsByTagName("div");

		for (i = 0; i < alldivTags.length; i++) {
			if (alldivTags[i].className == theClass && alldivTags[i].id != id) {
				$('#' + alldivTags[i].id).slideUp('slow');
				document.getElementById('Head' + alldivTags[i].id).className = 'acordion-gray';
			}
		}

		if (document.getElementById('Head' + id).className
				.search('acordion-open') != -1) {
			document.getElementById('Head' + id).className = 'acordion-gray';
		} else {
			document.getElementById('Head' + id).className = 'acordion-open acordion-gray';
		}
	}

	function destlayer() { //v6.0
		var i, p, v, obj, args = destlayer.arguments;
		for (i = 0; i < (args.length - 2); i += 3) {
			if ((obj = MM_findObj(args[i])) != null) {
				v = args[i + 2];
				if (obj.style) {
					obj = obj.style;
					v = (v == 'show') ? 'visible' : (v == 'hide') ? 'hidden'
							: v;
				}
				obj.visibility = v;
			}
		}
	}

	
	function saveAction(event) {
		var userStatus = document.getElementById("status").value;

		if (userStatus == "ACTIVE") {
			var setlmentNamingVal = document
					.getElementById("settlementNamingConvention").value;
			var refundNamingVal = document
					.getElementById("refundValidationNamingConvention").value;

			if (setlmentNamingVal == "" || setlmentNamingVal == null
					&& refundNamingVal == "" || refundNamingVal == null) {
				alert("Please enter Settlement and Refund Validation naming convention");
				event.preventDefault();
			} else {
				document.merchantSaveAction.submit();
				$('#loader-wrapper').show();
			}
		} else {
			document.merchantSaveAction.submit();
			$('#loader-wrapper').show();
		}
	}

	function cancelChanges() {
		$('#loader-wrapper').show();
		window.location.reload();
	}

	function showDivs(prefix, chooser) {
		for (var i = 0; i < chooser.options.length; i++) {
			var div = document
					.getElementById(prefix + chooser.options[i].value);
			div.style.display = 'none';
		}

		var selectedvalue = chooser.options[chooser.selectedIndex].value;

		if (selectedvalue == "PL") {
			displayDivs(prefix, "PL");
		} else if (selectedvalue == "PF") {
			displayDivs(prefix, "PF");
		} else if (selectedvalue == "PR") {
			displayDivs(prefix, "PR");
		} else if (selectedvalue == "CSA") {
			displayDivs(prefix, "CSA");
		} else if (selectedvalue == "LLL") {
			displayDivs(prefix, "LLL");
		} else if (selectedvalue == "RI") {
			displayDivs(prefix, "RI");
		} else if (selectedvalue == "AP") {
			displayDivs(prefix, "AP");
		} else if (selectedvalue == "T") {
			displayDivs(prefix, "T");
		}
	}

	function displayDivs(prefix, suffix) {
		var div = document.getElementById(prefix + suffix);
		div.style.display = 'block';
	}

	window.onload = function() {
		document.getElementById('select1').value = 'a';//set value to your default
	}

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

.textFL_merch5:hover {
	color: #000 !important;
	background: #fff !important;
}

.btn {
	font-size: 10pt !important;
}

.success-text { /* color:#ff0000!important; */
	margin: 4px auto 15px !important;
	padding: 10px !important;
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
.merchantStatus-data{
    width: 100%;
    border-radius: 8px;
    background-color: #FFFFFF;
    box-shadow: 0 2px 6px 0 rgba(0,0,0,0.12);
    margin-bottom: 20px;
    padding: 12px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
}

.merchantStatus-data > div{
    width: 24%;
    font-size: 12px;
    color: #888888;
}


.merchantStatus-data > div:nth-child(2){
    width: 56%;
}


.merchantStatus-data > div:nth-child(3){
    width: 20%;
}

.merchnatStatus-head {
    background-color: #f5f7f9;
    padding: 12px;
    border-radius: 4px;
    margin-bottom: 5px;
    display: flex;
    flex-wrap: wrap;
}

.merchnatStatus-head span {
    width: 24%;
}

.merchnatStatus-head span:nth-child(2){
    width: 56%;
}

.merchnatStatus-head span:nth-child(3){
    width: 20%;
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

div#uploadedDocuments span{
	padding: 10px 50px 10px 20px;
    display: inline-block;
    background-color: #f5f5f5;
    border-radius: 4px;
	margin-top: 15px;
	margin-right: 10px;
	position: relative;
}

div#uploadedDocuments span:after{
	content: "";
	width: 20px;
	height: 24px;
	background-image: url(../image/tick.png);
	position: absolute;
    right: 15px;
    z-index: 999;
    top: 8px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}

</style>

</head>
<body>

	<s:hidden value="%{MPAData.status}" id="merchantStatus"></s:hidden>
	<s:hidden value="%{MPAData.companyName}" id="mpaCompnayName"></s:hidden>
	<s:hidden value="%{MPAData.tradingCountry}" id="mpaTradingCountry"></s:hidden>
	<s:hidden value="%{MPAData.tradingState}" id="mpaTradingState"></s:hidden>
	<s:hidden value="%{MPAData.industryCategory}" id="mpaTradingIndustary"></s:hidden>
	<s:hidden value="%{merchantLogo}" data-id="merchantLogo"></s:hidden>
	<s:hidden value="%{showDownload}" data-id="showDownload"></s:hidden>
	<s:hidden value="%{MPAData.typeOfEntity}" data-id="typeOfEntity"></s:hidden>
	<s:hidden value="%{MPAData.payId}" id="merchantPayId" data-id="payId"></s:hidden>
	<s:hidden value="%{showCheckerFileDownload}" data-id="checkerDownload"></s:hidden>
	<s:hidden value="%{MPAData.paymentCycle}" data-id="paymentCycle"></s:hidden>

	<section
		class="merchant-account lapy_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o"
					aria-hidden="true"></i></span>
				<h2 class="heading_text">Vendor Account Setup</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-12">
			<div class="horizontal-nav-wrapper mb-20">
				<nav id="horizontal-nav" class="horizontal-nav">
				<ul
					class="horizontal-nav-content nav nav-tabs  list-unstyled font-size-14 merchant-config-tabs border-none mt-0"
					id="horizontal-nav-content" role="tablist">
					<li class="nav-item merchant__tab_button active-tab" data-target="merchantDetails">Merchant Details</li>
					<li class="nav-item merchant__tab_button" data-target="principleInformation">Principle Information</li>
					<li class="nav-item merchant__tab_button" data-target="bankAccountDetails">Bank Details</li>
					<li class="nav-item merchant__tab_button" data-target="onBoardDetails">Onboard Details</li>
					<li class="nav-item merchant__tab_button" data-target="documents">Documents</li>
					<li class="nav-item merchant__tab_button" data-target="preview">Merchant Status</li>
					<!--		
					<li class="nav-item merchant__tab_button"
						data-target="technicalDetails">Technical Details</li>
					<li class="nav-item merchant__tab_button"
						data-target="policyRegularity">Policy and Regularity</li>
					<li class="nav-item merchant__tab_button d-none"
						data-target="surchargeTextTab">Surcharge Text</li>
					<s:if test="%{MPAData.esignAadhaarType != null}">
						<li class="nav-item merchant__tab_button" data-target="eSign">e-Sign</li>
					</s:if>
					<li class="nav-item merchant__tab_button" data-target="action">Action</li>
					<li class="nav-item merchant__tab_button"
						data-target="onBoardDetail">Onboard Details</li>
					<li class="nav-item merchant__tab_button"
						data-target="notificationSetting">Notification Settings</li>
					<li class="nav-item merchant__tab_button"
						data-target="systemSetting">System Setting</li>
					<s:if test="%{showDownload != false}">
						<li class="nav-item merchant__tab_button" data-target="downloads">Downloads</li>
					</s:if>
					<s:if test="%{MPAData.makerStatus != null}">
						<li class="nav-item merchant__tab_button" data-target="maker">Maker</li>
					</s:if>
					<s:if test="%{MPAData.checkerStatus != null}">
						<li class="nav-item merchant__tab_button" data-target="checker">Checker</li>
					</s:if> -->
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

		<s:if test="%{responseCode == '101'}">	
				<div id="saveMessage">	
					<div class="success success-text">	
						<s:property value="%{responseMsg}"></s:property>	
					</div>	
					<!-- /.success success-text -->	
					<!-- <s:actionmessage class="success success-text" /> -->	
				</div>	
			</s:if>	
			<s:else>	
				<div class="error-text">	
					<s:property value="%{responseMsg}"></s:property>	
					<!-- <s:actionmessage theme="simple" /> -->	
				</div>	
			</s:else>

		<s:form action="khadiMerchantSetupUpdateAction" method="post"
			autocomplete="off" enctype="multipart/form-data" class="FlowupLabels"
			id="merchantForm" theme="css_xhtml">
			<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			<s:hidden name="emailId" value="%{MPAData.emailId}"></s:hidden>
			
			<!-- <s:if test="%{responseObject.responseCode=='101'}">
				<div id="saveMessage">
					<s:actionmessage class="success success-text" />
				</div>
			</s:if>
			<s:else>
				<div class="error-text">
					<s:actionmessage theme="simple" />
				</div>
			</s:else> -->
			<div class="merchant__forms_block active-block"
				data-active="merchantDetails">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Industry Category</label>
							<s:select class="selectpicker" id="industryCategory"
								name="industryCategory" list="industryTypesList"
								value="%{MPAData.industryCategory}" ></s:select>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Type Of Entity</label>
							<s:select
								name="typeOfEntity"
								id="typeOfEntity"
								headerKey="1"
								list="#{'Private Limited':'Private Limited','Public Limited':'Public Limited','Patnership':'Patnership','Proprietory':'Proprietory','Other':'Other'}"
								class="selectpicker"
								value="%{MPAData.typeOfEntity}">
							</s:select>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Legal Name</label>
							<s:textfield
								id="companyName"
								class="lpay_input"
								onkeypress="return lettersSpaceOnly(event, this);"
								name="companyName"
								type="text"
								value="%{MPAData.companyName}">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<s:if test="%{MPAData.typeOfEntity == 'Public Limited'}">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">CIN</label>
								<s:textfield
									id="cin"
									name="cin"
									type="text"
									maxlength="20"
									value="%{MPAData.cin}"
									class="lpay_input"
									autocomplete="off"
									onKeyPress="return alphanumeric(event);">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:if>
					<s:elseif test="%{MPAData.typeOfEntity == 'Private Limited'}">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">CIN</label>
								<s:textfield
									id="cin"
									name="cin"
									type="text"
									value="%{MPAData.cin}"
									class="lpay_input"
									autocomplete="off"
									onKeyPress="return alphanumeric(event);">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:elseif>
					<s:else>
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<s:if test="%{MPAData.typeOfEntity == 'Proprietory'}">
									<label for=""> Shop Establishment Number </label>
								</s:if>
								<s:else>
									<label for=""> Registration Number </label>
								</s:else>
								<s:textfield
									id="cin"
									name="registrationNumber"
									type="text"
									value="%{MPAData.registrationNumber}"
									class="lpay_input"
									autocomplete="off"
									oninput="onlyAlphaNumeric(this); _uppercase(this);">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:else>
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Date of Incorporation</label>
							<s:textfield
								id="dateOfIncorporation"
								class="lpay_input"
								name="dateOfIncorporation"
								type="text"
								readonly="true"
								value="%{MPAData.dateOfIncorporation}"
								autocomplete="off">
							</s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Registered Email</label>
								<div class="position-relative">
									<s:textfield
										id="companyEmailId"
										oninput="removeError(this);"
										onblur="_validateEmail(this);"
										class="lpay_input"
										value="%{MPAData.companyEmailId}"
										name="companyEmailId"
										type="text">
									</s:textfield>
									<span class="error" data-id="companyEmailId"></span>
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:if>
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
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
					</s:if>
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
							<label for="">Country</label> 
							
							<select class="" id="country"
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
							<s:textfield id="tradingPin" maxlength="6" class="lpay_input"
								name="tradingPin" type="text" value="%{MPAData.tradingPin}"
								autocomplete="off"
								onkeypress="javascript:return isNumber (event)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Company PAN</label>
							<s:textfield id="businessPan" class="lpay_input" maxlength="10"
								name="businessPan" type="text" value="%{MPAData.businessPan}"
								autocomplete="off" onKeyPress="return alphanumeric(event);"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">GST</label>
							<s:textfield id="gstin" class="lpay_input" maxlength="15" name="gstin"
								type="text" value="%{MPAData.gstin}" autocomplete="off"
								onKeyPress="return alphanumeric(event);"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Business Phone</label>
							<s:textfield id="companyPhone" class="lpay_input"
								name="companyPhone" type="text" maxlength="10" value="%{MPAData.companyPhone}"
								autocomplete="off"
								onkeypress="javascript:return isNumber (event)"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Company Website</label>
							<s:textfield id="companyWebsite" class="lpay_input"
								name="companyWebsite" type="text"
								value="%{MPAData.companyWebsite}" autocomplete="off"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for=""> Email for Communication </label>
								<s:textfield id="businessEmailForCommunication"
									onblur="validateEmail(this)" class="lpay_input"
									name="businessEmailForCommunication" type="text"
									value="%{MPAData.businessEmailForCommunication}"></s:textfield>
							</div>
							<!-- /.merchant__form_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:if>
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="col-md-12">
							<div class="inner-heading mb-20">
								<h3>Contact Person</h3>
							</div>
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Contact Person</label>
								<s:textfield type="text"
									onkeypress="return lettersSpaceOnly(event, this);"
									id="contactName" value="%{MPAData.contactName}"
									name="contactName" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile</label>
								<s:textfield type="text" maxlength="10"
									onkeypress="javascript:return isNumber (event)"
									id="contactMobile" value="%{MPAData.contactMobile}"
									name="contactMobile" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email</label>
								<s:textfield type="text" onblur="validateEmail(this);"
									id="contactEmail" value="%{MPAData.contactEmail}"
									name="contactEmail" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Landline</label>
								<s:textfield type="text" maxlength="11"
									onkeypress="javascript:return isNumber (event)"
									id="contactLandline" value="%{MPAData.contactLandline}"
									name="contactLandline" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:if>
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="principleInformation">
				<div class="row">
					<s:if test="%{MPAData.typeOfEntity == 'Partnership Firm'}">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">TAN Number</label>
								<s:textfield type="text"
									onkeypress="javascript:return isNumber (event)"
									id="tan" value="%{MPAData.tan}"
									name="tan" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:if>
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="col-md-12">
							<div class="inner-heading mb-20">
								<s:if test="%{MPAData.typeOfEntity == 'Public Limited'}">
									<h3>First Director Detail</h3>
								</s:if>
								<s:elseif test="%{MPAData.typeOfEntity == 'Private Limited'}">
									<h3>First Director Detail</h3>
								</s:elseif>
								<s:else>
									<h3>First Partner Detail</h3>
								</s:else>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Name</label>
								<s:textfield type="text"
									onkeypress="return lettersSpaceOnly(event, this);"
									id="director1FullName" value="%{MPAData.director1FullName}"
									name="director1FullName" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">PAN Number</label>
								<s:textfield type="text" maxlength="10"
									onKeyPress="return alphanumeric(event);" id="director1Pan"
									value="%{MPAData.director1Pan}" name="director1Pan"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email</label>
								<s:textfield type="text" onblur="validateEmail(this);"
									id="director1Email" value="%{MPAData.director1Email}"
									name="director1Email" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile Number</label>
								<s:textfield type="text" maxlength="10"
									onkeypress="javascript:return isNumber (event)"
									id="director1Mobile" value="%{MPAData.director1Mobile}"
									name="director1Mobile" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Address</label>
								<s:textfield type="text" id="director1Address"
									value="%{MPAData.director1Address}" name="director1Address"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-12">
							<div class="inner-heading mb-20">
								<s:if
									test="%{MPAData.typeOfEntity == 'Private Limited'} || %{MPAData.typeOfEntity == 'Public Limited'}">
									<h3>Second Director Detail</h3>
								</s:if>
								<s:else>
									<h3>Second Partner Detail</h3>
								</s:else>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Name</label>
								<s:textfield type="text"
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
								<s:textfield type="text" maxlength="10"
									onKeyPress="return alphanumeric(event);" id="director2Pan"
									value="%{MPAData.director2Pan}" name="director2Pan"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email</label>
								<s:textfield type="text" onblur="validateEmail(this);"
									id="director2Email" value="%{MPAData.director2Email}"
									name="director2Email" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile Number</label>
								<s:textfield type="text" maxlength="10"
									onkeypress="javascript:return isNumber (event)"
									id="director2Mobile" value="%{MPAData.director2Mobile}"
									name="director2Mobile" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Address</label>
								<s:textfield type="text" id="director2Address"
									value="%{MPAData.director2Address}" name="director2Address"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:if>
					<s:else>
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Proprietory Detail</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Name</label>
								<s:textfield type="text"
									onkeypress="return lettersSpaceOnly(event, this);"
									id="director1FullName" value="%{MPAData.director1FullName}"
									name="director1FullName" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">PAN Number</label>
								<s:textfield type="text"
									 id="director1Pan" maxlength="10"
									value="%{MPAData.director1Pan}" name="director1Pan"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email</label>
								<s:textfield type="text" onblur="validateEmail(this);"
									id="director1Email" value="%{MPAData.director1Email}"
									name="director1Email" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile Number</label>
								<s:textfield type="text"
									onkeypress="javascript:return isNumber (event)"
									id="director1Mobile" maxlength="10" value="%{MPAData.director1Mobile}"
									name="director1Mobile" class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Address</label>
								<s:textfield type="text" id="director1Address" value="%{MPAData.director1Address}" name="director1Address"
									class="lpay_input"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</s:else>
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="bankAccountDetails">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Account Number</label>
							<s:textfield type="text" id="accountNumber" maxlength="20"
								onkeypress="javascript:return isNumber (event)"
								value="%{MPAData.accountNumber}" name="accountNumber"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Virtual Account Number</label>
							<s:textfield type="text" id="virtualAccountNo"
								onkeypress="javascript:return isNumber (event)"
								value="%{MPAData.virtualAccountNo}" readonly="true" name="virtualAccountNo"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Merchant VPA</label>
							<s:textfield type="text" id="merchantVPA"
								onkeypress="javascript:return isNumber (event)"
								value="%{MPAData.merchantVPA}" readonly="true" name="merchantVPA"
								class="lpay_input"></s:textfield>
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
							<s:textfield type="text" id="accountIfsc"
								onkeypress="return alphanumeric(event);"
								value="%{MPAData.accountIfsc}" name="accountIfsc"
								class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Mobile Number</label>
							<s:textfield type="text" maxlength="10" id="accountMobileNumber"
								onkeypress="javascript:return isNumber (event)"
								value="%{MPAData.accountMobileNumber}"
								name="accountMobileNumber" class="lpay_input"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="onBoardDetails">
				<div class="row">
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Pay Id</label>
							<s:textfield id="payId" readonly="true" class="lpay_input"
								name="payId" type="text" value="%{MPAData.payId}"></s:textfield>
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
							<label for="">Super Merchant ID</label>
							<s:textfield id="superMerchantId" readonly="true" class="lpay_input"
								name="superMerchantId" type="text"
								value="%{MPAData.superMerchantId}"></s:textfield>
						</div>
						<!-- /.lpay_input_group -->
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
						<div class="lpay_input_group">
							<label for="">Payment Cycle (In days)</label>
							<s:textfield class="lpay_input" onkeypress="javascript:return isNumber (event)" id="paymentCycle" maxlength="3" name="paymentCycle" value="%{MPAData.paymentCycle}" ></s:textfield>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->

				</div>
				<!-- /.row -->
			</div>
			<!-- /.merchant__forms_block -->
			<div class="merchant__forms_block" data-active="documents">
					<div class="row">
						<div class="col-md-4">
							<div class="lpay_select_group">
							   <label for="">Select File Type</label>
							   <select name="mpaFileType" class="selectpicker" id="mpaFiletype"></select>
							</div>
							<!-- /.lpay_select_group -->  
						</div>
						<!-- /.col-md-3 -->
						<div class="col-md-4">
							<form action="" method="post" enctype="multipart/form-data" class="upload-generic">
								<div class="upload_file-wrapper">
									<label class="lable-default">Upload File</label>
									<div for="uploadFileInput" data-response="default" class="upload_file-label">
									  <img src="../image/cloud-computing.png" alt="/">
									  <input type="file" disabled name="file" data-type="checker" id="uploadFileInput" class=""> 
									  <div class="uploaded_file-info lable-default uploaded_file-default">
										<span class="d-block">File Size: <b>2 MB</b></span>
										<span>File Type: <b>PDF/PNG/JPG</b></span>
									  </div>
									  <!-- /.upload_file-info -->
									  <div class="uploaded_file-info lable-default uploaded_file-sizeError">
										<span>File size is too long</span>
									  </div>
									  <!-- /.upload_file-info -->
									  <div class="uploaded_file-info lable-default uploaded_file-typeError">
										<span>Please choose valid file type</span>
									  </div>
									  <!-- /.upload_file-info -->
									  <div class="uploaded_file-info lable-default uploaded_file-success">
										<span id="uploadedFileName"></span>
									  </div>
									  <!-- /.upload_file-info -->
									</div>
									<!-- .upload_file-label -->
								</div>
								<!-- /.upload_file-wrapper -->
							</form>
						</div>
						<!-- /.col-md-3 -->
						<div class="col-md-12 mt-15 d-none" data-id="uploaded-documents">
							<div class="inner-heading">
								<h3>Uploaded Documents</h3>
							</div>
							<!-- /.inner-heading -->
							<div class="uploaded-documents" id="uploadedDocuments">

							</div>
							<!-- /.uploaded-documents -->
						</div>
						<!-- /.col-md-12 -->
						<s:if test="%{showDownload == true}">
							<div class="col-md-12 mt-15">
								<div class="inner-heading">
									<h3>Download</h3>
								</div>
								<!-- /.inner-heading -->
								<span class="lpay_button d-inline-block lpay_button-md lpay_button-secondary mt-15 downloadMpa" data-type="MPA" style="display: inline-block;margin-top: 15px !important">Download MPA Files</span>
							</div>
							<!-- /.col-md-12 -->
						</s:if>
							
						
					</div>
				</div> 
			<!-- /.merchant__forms_block -->
			
			<div class="merchant__forms_block" data-active="preview">

				<div class="merchantAssing-status ">
					
					<div class="merchnatStatus-head">
						<span>Status</span>
						<span>Comment</span>
						<span>File</span>
					</div>
					<!-- /.merchnatStatus-head -->
					<div class="merchantStatus-data">
						<div class="merchnatStatus-block">
							<div data-name="status"><s:property value="%{MPAData.status}"></s:property></div>
							<!-- <s:hidden data-id="checker-status" value="%{mpaMerchant.checkerStatus}"></s:hidden> -->
						</div>
						<!-- /.merchnatStatus-block -->
						<div class="merchnatStatus-block">
							<div data-id="comments"><s:property value="%{MPAData.comments}"></s:property></div>
							<!-- <s:hidden data-id="checker-feedback" value="%{mpaMerchant.checkerComments}"></s:hidden> -->
						</div>
						<!-- /.merchnatStatus-block -->
						<div class="merchnatStatus-block">
							<div class="static-download d-none">
								<span class="lpay_button lpay-button-md lpay_button-secondary downloadMpa" style="display: inline-block"  data-type="CHECKER_FILE">Download</span>
							</div>
							<div class="static-download-none d-none">
								<span>No file uploaded</span>
							</div>
							<!-- /.static-download-none -->
							<!-- /.static-download -->
							<!-- <a href="#" id="checker-action" class="download-button">Download</a>
							<span class="not-uploaded-checker d-none">No file uploaded</span>
							<s:hidden data-di="checker-action" value="%{mpaMerchant.checkerFileName}"></s:hidden> -->
						</div>
						<!-- /.merchnatStatus-block -->
					</div>
					<!-- /.merchantStatus-data -->
				</div>
				<!-- /.merchantAssing-status -->

				<div class="merchantAssign-filter">
					<div class="row">
						<div class="col-md-4">
							<div class="lpay_select_group">
								<label for="">Status</label>
								<select name="" class="selectpicker" id="Editchecker">
									<option value="">Select Status</option>
									<option value="Approved">Approve</option>
									<option value="Rejected">Reject</option>
								</select>
							</div>
							<!-- /.merchant__form_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-5">
							<div class="lpay_input_group">
								<label for="">Comments</label>
								<input type="text" id="statusComment" class="lpay_input feedback">
							</div>
							<!-- /.merchant__form_group -->
						</div>
						<!-- /.col-md-6 -->
						<div class="col-md-3 upload-doc">
							<form action="" enctype="multipart/form-data" id="fileUpload">
							<div class="merchant__form_group">
								<label for="" class="lable-default">Upload documents (if any)</label>
								<label for="uploadDoc" class="upload-pic lable-default">
									<img src="../image/cloud-computing.png" alt="/">
									<div class="upload-text">
										<span class="doc-text"><b style="color: #000">File size:</b> 2 MB</span>
										<span class="doc-file-name"><b style="color: #000">Format:</b> PDF</span>
									</div>
									<input type="file" name="file" id="uploadDoc" class="feedback" accept=".pdf" multiple>
									<s:hidden  id="uploadFilePayId" name="payId" value=""></s:hidden>
									<s:hidden name="fileContentType"></s:hidden>
								</label>
							</div>
							<!-- /.merchant__form_group -->
						</form>
						</div>
						<!-- /.col-md-2 upload-doc -->
						<div class="col-md-12 check-btn text-center mb-20 mt-15">
							<span class="lpay_button lpay_button-md lpay_button-secondary" style="display: inline-block" id="mpaSaveStatus">Submit</span>
						</div>
						<!-- /.col-md-12 -->
					</div>
					<!-- /.row -->
				</div>
				<div class="inner-heading">
					<h3>Merchant Details</h3>
				</div>
				<div class="merchantDetail-action">
					<div class="merchantDetail-box">
						<span>Company Name</span>
						<div><s:property value="%{MPAData.companyName}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<s:if test="%{MPAData.typeOfEntity == 'Public Limited'}">
						<div class="merchantDetail-box">
							<span>CIN Number</span>
							<div><s:property value="%{MPAData.cin}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</s:if>
					<s:elseif test="%{MPAData.typeOfEntity == 'Private Limited'}">
						<div class="merchantDetail-box">
							<span>CIN Number</span>
							<div><s:property value="%{MPAData.cin}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</s:elseif>
					<s:else>
						<div class="merchantDetail-box">
							<s:if test="%{MPAData.typeOfEntity == 'Proprietory'}">
									<span> Shop Establishment Number </span>
								</s:if>
								<s:else>
									<span> Registration Number </span>
								</s:else>
							<div><s:property value="%{MPAData.registrationNumber}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</s:else>

					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="merchantDetail-box d-none">
							<span>Registered Email</span>
							<div><s:property value="%{MPAData.companyEmailId}"></s:property></div>
							<!-- /#companyName -->
						</div>
					</s:if>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Date of Incorporation</span>
						<div><s:property value="%{MPAData.dateOfIncorporation}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="merchantDetail-box">
							<span>Registered Address</span>
							<div><s:property value="%{MPAData.companyRegisteredAddress}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</s:if>
					<div class="merchantDetail-box">
						<span>Trading Address</span>
						<div><s:property value="%{MPAData.tradingAddress1}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Country</span>
						<div><s:property value="%{MPAData.tradingCountry}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>State</span>
						<div><s:property value="%{MPAData.tradingState}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>PIN</span>
						<div><s:property value="%{MPAData.tradingPin}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Company PAN</span>
						<div><s:property value="%{MPAData.businessPan}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>GST Number</span>
						<div><s:property value="%{MPAData.gstin}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Company Phone</span>
						<div><s:property value="%{MPAData.companyPhone}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Business Email</span>
						<div><s:property value="%{MPAData.businessEmailForCommunication}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Company Website</span>
						<div><s:property value="%{MPAData.companyWebsite}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<!-- /.merchantDetail-box d-none -->
					<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
						<div class="merchantDetail-box">
							<span>Contact Person</span>
							<div><s:property value="%{MPAData.contactName}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Contact Phone</span>
							<div><s:property value="%{MPAData.contactMobile}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Contact Email</span>
							<div><s:property value="%{MPAData.contactEmail}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Contact Landline</span>
							<div><s:property value="%{MPAData.contactLandline}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</s:if>
				</div>
				<!-- /.merchantDetail-action -->
				<div class="inner-heading mt-20" style="width: 100%">
					<h3>Merchant Account Detail</h3>
				</div>
				<div class="merchantDetail-action">
					<div class="merchantDetail-box">
						<span>Account Holder Name</span>
						<div><s:property value="%{MPAData.accountHolderName}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Account Number</span>
						<div><s:property value="%{MPAData.accountNumber}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Account IFSC</span>
						<div><s:property value="%{MPAData.accountIfsc}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
					<div class="merchantDetail-box">
						<span>Account Number</span>
						<div><s:property value="%{MPAData.accountMobileNumber}"></s:property></div>
						<!-- /#companyName -->
					</div>
					<!-- /.merchantDetail-box d-none -->
				</div>
				<!-- /.merchantDetail-action -->
				<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
					<div class="inner-heading mt-20" style="width: 100%">
						<s:if test="%{MPAData.typeOfEntity == 'Private Limited'}">
							<h3>Director-1 Details</h3>
						</s:if>
						<s:elseif test="%{MPAData.typeOfEntity == 'Public Limited'}">
							<h3>Director-1 Details</h3>
						</s:elseif>
						<s:else>
							<h3>Partner-1 Details</h3>
						</s:else>
					</div>
					<div class="merchantDetail-action">
						<div class="merchantDetail-box">
							<span>Fullname</span>
							<div><s:property value="%{MPAData.director1FullName}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Email</span>
							<div><s:property value="%{MPAData.director1Email}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Mobile</span>
							<div><s:property value="%{MPAData.director1Mobile}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>PAN Number</span>
							<div><s:property value="%{MPAData.director1Pan}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Director Address</span>
							<div><s:property value="%{MPAData.director1Address}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</div>
					<!-- /.merchantDetail-action -->
					<div class="inner-heading mt-20" style="width: 100%">
						<s:if test="%{MPAData.typeOfEntity == 'Private Limited'}">
							<h3>Director-2 Details</h3>
						</s:if>
						<s:elseif test="%{MPAData.typeOfEntity == 'Public Limited'}">
							<h3>Director-2 Details</h3>
						</s:elseif>
						<s:else>
							<h3>Partner-2 Details</h3>
						</s:else>
					</div>
					<div class="merchantDetail-action">
						<div class="merchantDetail-box">
							<span>Fullname</span>
							<div><s:property value="%{MPAData.director2FullName}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Email</span>
							<div><s:property value="%{MPAData.director2Email}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Mobile</span>
							<div><s:property value="%{MPAData.director2Mobile}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>PAN Number</span>
							<div><s:property value="%{MPAData.director2Pan}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Director Address</span>
							<div><s:property value="%{MPAData.director2Address}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</div>
					<!-- /.merchantDetail-action -->
				</s:if>
				<s:else>
					<div class="inner-heading mt-20" style="width: 100%">
						<h3>Proprietory Details</h3>
					</div>
					<div class="merchantDetail-action">
						<div class="merchantDetail-box">
							<span>Fullname</span>
							<div><s:property value="%{MPAData.director1FullName}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Email</span>
							<div><s:property value="%{MPAData.director1Email}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Mobile</span>
							<div><s:property value="%{MPAData.director1Mobile}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>PAN Number</span>
							<div><s:property value="%{MPAData.director1Pan}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
						<div class="merchantDetail-box">
							<span>Director Address</span>
							<div><s:property value="%{MPAData.director1Address}"></s:property></div>
							<!-- /#companyName -->
						</div>
						<!-- /.merchantDetail-box d-none -->
					</div>
					<!-- /.merchantDetail-action -->
				</s:else>
				
			</div>
			<!-- /.merchant__forms_block -->
			<div class="row">
				<div class="col-md-12 text-center">
					<button id="btnSave"
						class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</s:form>
	</div>
	<!-- /.merchant__forms mt-30 --> </section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<s:hidden name="token" id="customToken" value="%{#session.customToken}" />
	<s:form method="POST" id="checkerFileDownload"
		action="statusFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="checkerFileName" value="%{MPAData.checkerFileName}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
	</s:form>

	

	<s:form method="post" id="mpaFileDownload"
		action="mpaMerchantFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
		<s:hidden name="fileNameType" id="fileType" />
	</s:form>




	<script>
				
		$(document).ready(function() {
			if($("[data-id=paymentCycle]").val() == ""){
				$("#paymentCycle").val(21);
			}
					
			var _privateLimited = {
				"panDirectorOne" : "PAN of authorized signatory 1",
				"panDirectorTwo" : "PAN of authorized signatory 2",
				"GST" : "GST Registration Certificate",
				"PanCard" : "Certification of Incorporation",
				"panCompany" : "PAN of the Company",
				"checque" : "Cancelled Cheque"
			}

			var _partnerShip = {
				"pan1": "PAN card of Partner 1",
				"pan2": "PAN card of Partner 2",
				"panEntity": "PAN of firm",
				"GST": "Registration of Certificate",
				"cheque": "Cancelled Cheque",
				"deed": "Partnership Deed"
			}

			var _proprietory = {
				"panPropriotery" : "PAN of Proprietor",
				"gstPropriotery" : "GST Registration Certification",
				"cheque" : "Cancelled Cheque",
				"tradePropriotery": "Shop Establishment Certificate / Udyog aadhar"
			}

			var _getEntity = $("[data-id=typeOfEntity]").val();

			//console.log(_getEntity);

			if($("[data-id=checkerDownload]").val() == "true"){
				$(".static-download").removeClass("d-none");
			}else{
				$(".static-download-none").removeClass("d-none");
			}

			function createSelectBox(_data){
				$("#mpaFiletype").html("");
				var _createOption = "<option value=''>Select Document</option>";
				for (key in _data){
					_createOption += "<option value="+key+">"+_data[key]+"</option>";
				}
				$("#mpaFiletype").append(_createOption);
				$("#mpaFileType").selectpicker();
				$("#mpaFileType").selectpicker("refresh");
			}

			$("#mpaFiletype").on("change", function(e){
				if($(this).val() == ""){
					$("#uploadFileInput").attr("disabled", true);
				}else{
					$("#uploadFileInput").attr("disabled", false);
				}
				$("#uploadFileInput").val("");
				$("#uploadFileInput").attr("data-type", $(this).val());
				$("#uploadFileInput").closest("div").attr("data-response", "default");
			})
			
			if(_getEntity == "Private Limited" || _getEntity == "Public Limited"){
				createSelectBox(_privateLimited);
			}

			if(_getEntity == "Partnership Firm"){
				createSelectBox(_partnerShip);
			}

			if(_getEntity == "Proprietory"){
				createSelectBox(_proprietory);
			}

			if(_getEntity == ""){
				createSelectBox(_privateLimited);
			}

			$("#typeOfEntity").on("change", function(e){
				var _this = $(this).val();
				createSelectBox(_this);
			})
			

		$("#uploadFileInput").on("change", function(e){
		var getFormClass = $(this).closest("form").attr("class");
		var _that = $(this);
		var _parent = $(this).closest("label");
		var _getFileTypeTmp = document.querySelector("#mpaFiletype");
		var _getFileType = _getFileTypeTmp.options[_getFileTypeTmp.selectedIndex].text;
        var form = $("."+getFormClass)[0];
        var file = $(this).val();
        var getFilePeriod = file.lastIndexOf(".");
        var getFileExact = file.slice(getFilePeriod);
        // $('#filePHOTO')[0].files[0].size
        var fileSize = $(this)[0].files[0].size;
        var data = new FormData(form);
        var getName = file.replace("C:\\fakepath\\", "");
        var fileName = $(this).attr("data-type");
		data.append("fileName", fileName);
		if(getFileExact == ".png" || getFileExact == ".pdf" || getFileExact == ".jpeg" || getFileExact == ".jpg"){
			if(fileSize <= 2097152){
				document.querySelector("body").classList.remove("loader--inactive");
				$.ajax({
					type: "post",
					enctype: "multipart/form-data",
					url: "uploadFile",
					data: data,
					processData: false,
					contentType: false,
					success: function(data){
						document.querySelector("[data-id=uploaded-documents]").classList.remove("d-none");
						var _uploadedDoc = "";
						if($("[data-active=documents] #"+_getFileType).length == 0){
							_uploadedDoc += "<span id="+_getFileType+">"+_getFileType+"</span>";
							$("#uploadedDocuments").append(_uploadedDoc);
						}
						document.querySelector("#uploadedFileName").innerText = getName;
						_that.closest("div").attr("data-response", "success");
						setTimeout(function(e){
							document.querySelector("body").classList.add("loader--inactive");
						}, 1000);
					}
				})
			}else{
				$(this).closest("div").attr("data-response", "sizeError");	
			}
		}else{
			$(this).closest("div").attr("data-response", "typeError");
		}
    })

			$("#btnSave").on("click", function(e){
                $("#merchantForm").submit();
            })
			$(".checkbox-label input").on("change", function(e) {
				if ($(this).is(":checked")) {
					$(this).closest("label").addClass("checkbox-checked");
				} else {
					$(this).closest("label").removeClass("checkbox-checked");
				}
			});

			$(".downloadMpa").on("click", function(e){
				var _this = $(this).attr("data-type");
				$("#fileType").val(_this);
				// return false;
				$("#mpaFileDownload").submit();
			})

			if($("#merchantStatus").val() != "" && $("#merchantStatus").val() != "PENDING"){
				$(".merchantAssign-filter").addClass("d-none");
			}else{
				$(".merchantAssing-status").addClass("d-none");
			}

			$("#mpaSaveStatus").on("click", function(e){
				var _status = $("#Editchecker").val();
				var _comment = $("#statusComment").val();
				var _payId = $("#merchantPayId").val();
				var _token = $("#customToken").val();
				if(_status == ""){
					alert("Please select status");
					return false;
				}
				$("body").removeClass("loader--inactive");
				$.ajax({
					type : "POST",
					url: "updateMpaStatus",
					data: { "token": _token, "payId": _payId, "merchantStatus": _status, "statusComment": _comment },
					success:function(data){
						console.log(data);
						if($("#uploadDoc").val() != ""){
							$(".static-download").removeClass("d-none");
							$(".static-download-none").addClass("d-none");
						}else{
							$(".static-download-none").removeClass("d-none");
							$(".static-download").addClass("d-none");
						}
						if(data.responseStatus == "Success"){
							$("[data-name=status]").html(data.merchantStatus);	
							$("[data-id=comments]").html(_comment);
							$(".merchantAssign-filter").addClass("d-none");
							$(".merchantAssing-status").removeClass("d-none");
							if(data.merchantStatus == "APPROVED"){
								$("#status").attr("disabled", false);
								$("#status").selectpicker();
								$("#status").selectpicker("refresh");
							}
						}
						setTimeout(function(e){
							$("body").addClass("loader--inactive");
						}, 1000);
					}
				})
			})

			// upload file 
            function uploadFunc(){
                var _file = this.files[0].size;
                var _filePath = $(this).val();
                var names = [];
                for (var i = 0; i < $(this).get(0).files.length; ++i) {
                    names.push($(this).get(0).files[i].name);
                }
                var _payId = $("#merchantPayId").val();
                $("#uploadFilePayId").val(_payId);
                var _form = $("#fileUpload")[0];
                var  data = new FormData(_form);
                var _getFileName = _filePath.replace("C:\\fakepath\\", "");
                var _getPeriodPos= _filePath.lastIndexOf(".");
                var _getExtension = _filePath.slice(_getPeriodPos);
                data.append("fileName", names);
                if(_getExtension == ".png" || _getExtension == ".pdf" || _getExtension == ".xls" || _getExtension == ".xlsx" || _getExtension == ".csv" || _getExtension == ".jpg"){
                    $(".upload-pic").removeClass("upload-denied");
                    $(".upload-pic").addClass("upload-success");
                    if(_file < 3000000){
                        $.ajax({
                            type: "post",
                            enctype: "multipart/form-data",
                            url: "uploadCheckerDocument",
                            data: data,
                            processData: false,
                            contentType: false,
                            success: function(data){
                                $(".upload-pic").removeClass("upload-denied");
                                $(".upload-pic").addClass("upload-success");
                                $(".upload-text").text(names);
                            }
                        })
                    }else{
                        $(".upload-pic").removeClass("upload-success");
                        $(".upload-pic").addClass("upload-denied");
                        $(".upload-text").text("file size should not greater then 2mb");
                    }
                }else{
                    $(".upload-pic").removeClass("upload-success");
                    $(".upload-pic").addClass("upload-denied");
                    $(".upload-text").text("file format dose not match");
                }
            }

            $("#uploadDoc").on("change", uploadFunc);

		})
	</script>

	<style type="text/css">
		@-moz-document url-prefix () {
			#allowRefund2 {	margin-left:42.6%!important; }
		}
	</style>

	<script src="../js/main.js"></script>
	<script src="../js/merchantAccountSetup.js"></script>
	<script src="../js/mechant-script.js"></script>
	<script src="../js/horizontal-scrolling-nav.js"></script>
</body>
</html>