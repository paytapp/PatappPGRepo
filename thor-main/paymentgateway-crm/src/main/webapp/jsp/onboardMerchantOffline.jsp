<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="ie=edge">
<title>Merchant Processing Application System</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600,700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/custom.css">
<link rel="stylesheet" href="../css/jquery-ui.css">
<link rel="stylesheet" href="../fonts/css/font-awesome.css">
<link rel="stylesheet" href="../css/mpaStyle.css">
<link rel="stylesheet" href="../css/loader-animation.css">
<link rel="stylesheet" href="../css/bootstrap-flex.css">
<link rel="stylesheet" href="../css/common-style.css">
<link rel="stylesheet" href="../css/styles.css">
<link rel="stylesheet" href="../css/jquery.fancybox.min.css">

<style>

.mpaSectionData-box {
	width: 100%;
	max-width: 25%;
    padding: 10px 20px 0;
}

.mpaSectionData [data-id] {
    margin-top: 5px;
}

.mpaSectionData{
	width: 100%;
    background-color: #fff;
    border-radius: 5px;
    display: flex;
    flex-wrap: wrap;
    padding-bottom: 20px;
    margin-bottom: 20px;
    box-shadow: 0 0 5px rgb(0 0 0 / 20%);
}

.mpaSectionData-title {
    font-size: 12px;
    font-weight: 500;
}

.mpaSectionData-heading{
	width: 100%;
}

.mpaSectionData-heading h3 {
    font-size: 15px !important;
    padding: 18px 20px;
    background-color: #f5f5f5;
    border-radius: 5px;
    margin-bottom: 10px;
}

.defautl-text-color {
	color: #888888 !important;
}



#dateOfIncorporation{
	pointer-events: unset;
}

body {
	background-color: #eee;
}

.mpaFormRow input:read-only {
	background-color: #e8e8e8;
	cursor: no-drop;
}

.d-inline-block {
	display: inline-block;
}

.stage-box.active {
	display: inline-block;
}

.btn-stage.inactive {
	display: none;
}

.d-flex {
	display: flex;
}

.justify-content-center {
	justify-content: center;
}

.align-item-center {
	align-items: center;
}

label {
	margin-bottom: 0;
}

.h-40 {
	height: 40px;
}

.red-error {
	color: #f00;
}

.pull-right {
	float: right;
}

.loader--text {
	width: 15rem;
}

.redLine{
	border: 1px solid #f00;
}

.redLine .file-msg{
	color: #f00;
}

.loader--text:after {
	content: "Processing your inputs";
	font-weight: 600;
	font-size: 14px;
	animation-name: loading-text;
	animation-duration: 3s;
	animation-iteration-count: infinite;
	color: #888;
}

.mpaLogo {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.onLoad-object {
	position: absolute;
	z-index: -1;
	opacity: 0;
}

.mpaMerchant-circle {
	background-color: #002163;
	padding: 8px 11px;
	font-size: 12px;
	border: none;
	font-weight: 600;
	text-transform: uppercase;
	border-radius: 4px;
	text-align: center;
	width: 45px;
	height: 45px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	margin-top: 8px;
	justify-content: center;
	margin-right: 10px;
}

.mpaMerchant-circle img {
	height: 15px;
}

.mpaHeader{
	background-color: #1c1e22;
}

.mpaLogo{
	padding-left: 15px;
}

.mpaLogoImg img{
	width: 78px;
}

.refundPolicyButton{
	width: auto;
    padding: 7px 8px;
    margin-top: 10px;
    display: inline-block;
    font-size: 10px;
	margin-left: 48px;

}

.refundPolicyButton:hover, .refundPolicyButton:focus{
	color: #fff;
	text-decoration: none;
}

</style>
</head>

<body>

	<div class="loader-container w-100 vh-100 lpay-center">
        <div class="loaderImage">
            <img src="../image/loader.gif" alt="Loader">
        </div>
    </div>

	<s:hidden value="%{payId}" id="merchantPayId"></s:hidden>
	<header class="mpaHeader">
		<div class="container">
			<div class="mpaLogo">
				<a href="#" class="mpaLogoImg"> 
					<img src="../image/white-logo.png" alt="Payment Gateway" width="120px">
				</a>
				<s:a class="mpaNext mpaMerchant-circle" action="merchantsForMPA">
					<img src="../image/previous-arrow.png" alt="">
				</s:a>
			</div>
			<!-- /.mpaLogo -->
		</div>
	</header>
	<!-- /.mpaHeader -->
	<div style="width: 100%; max-width: 1200px; margin: auto; margin-bottom: 30px;">
		<section class="onboarding-merchant lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<h2 class="heading_text w-100 text-center">Merchant Processing Application
						System</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<section class="mpaSection">
				<div class="">
					<div class="row">
						<div class="col-md-12 p-0 mb-30" id="mpaSectionIndicator">
							<div class="progressDefault"></div>
							<!-- /.progressDefault -->
							<div id="progressStatus" style="width: 0"></div>
							<!-- /.progressStatus -->
							<div class="mpaFormBulletsBox active" data-active="stage-00">
								<span class="mpaFormBullets"></span>
								<h4>Merchant Details</h4>
							</div>
							<!-- /.mpaFormBulletsBox -->
							<div class="mpaFormBulletsBox" data-active="stage-01">
								<span class="mpaFormBullets"></span>
								<h4>Principal Information</h4>
							</div>
							<!-- /.mpaFormBulletsBox -->
							<div class="mpaFormBulletsBox" data-active="stage-02">
								<span class="mpaFormBullets"></span>
								<h4>Bank Account Details</h4>
							</div>
							<!-- /.mpaFormBulletsBox -->
							<div class="mpaFormBulletsBox" data-active="stage-03">
								<span class="mpaFormBullets"></span>
								<h4>Business Details</h4>
							</div>
							<!-- /.mpaFormBulletsBox -->
							<div class="mpaFormBulletsBox" data-active="stage-04">
								<span class="mpaFormBullets"></span>
								<h4>Uploads</h4>
							</div>
							<!-- /.mpaFormBulletsBox -->
							<div class="mpaFormBulletsBox" data-active="stage-05">
								<span class="mpaFormBullets"></span>
								<h4>Preview</h4>
							</div>
							<!-- /.mpaFormBulletsBox -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-12 p-0">
							<!-- /#loading -->
							<div class="mpaSectionFormTab stage-box active" id="stage-00">

								<input type="hidden" id="stage">
								<div class="col-md-4 mt-15 mb-20">
									<label for="" class="w-100"> Industry Category<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<select name="" id="industryCategory"
											class="mpaInput mpa-input mpa-input-select"
											onchange="checkAllField(this)">
											<!-- <option value="">Country</option>
											<option value="delhi">delhi</option> -->
										</select>
									</div>
								</div>
								<div class="col-md-12">
									<div
										class="mpaFormRow mpaFlex mpa-input mpa-input-alt align-item-center h-40"
										id="typeOfEntity" data-key="typeOfEntity">
										<span class="heading formLabel">Please select type of
											entity<b class="imp">*</b>
										</span>
										<div class="mpaFormRowRadio mb-0" id="mpaRadio">
											<label class="paymentGatewayRadio">Private Limited <input
												disabled type="radio" name="radio" value="Private Limited"
												onchange="checkAllField(this)"> <span
												class="checkmark"></span>
											</label> <label class="paymentGatewayRadio">Public Limited <input
												disabled type="radio" id="privateLimited"
												value="Public Limited" name="radio"
												onchange="checkAllField(this)"> <span
												class="checkmark"></span>
											</label> <label class="paymentGatewayRadio">Partnership Firm <input
												disabled type="radio" id="privateLimited"
												value="Partnership Firm" name="radio"
												onchange="checkAllField(this)"> <span
												class="checkmark"></span>
											</label> <label class="paymentGatewayRadio">Proprietory <input
												disabled type="radio" id="Proprietory" value="Proprietory"
												name="radio" onchange="checkAllField(this)"> <span
												class="checkmark"></span>
											</label> <label class="paymentGatewayRadio">Other <input disabled
												type="radio" id="other" value="other" name="radio"
												onchange="checkAllField(this)"> <span
												class="checkmark"></span>
											</label> <label class="mpaFormRow mb-0" id="other"> <input
												type="text" class="other" placeholder="Other"
												onchange="checkAllField(this)">
											</label>
										</div>
										<!-- /.mpaFormRowRadio -->
									</div>
									<!-- /.mpaSectionRow -->
								</div>
								<!-- /.col-md-12 -->
								<div class="col-md-3 mt-15 pr-30">
									<label for="" class="w-100"> Legal Name<b class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="companyName" data-length="50"
											class="mpaInput mpa-input companyNameApi"
											onkeypress="lettersAndAlphabet(event);"
											onchange="checkAllField(this)" readonly> <span
											class="error-msg"></span>
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15" data-change="cin">
									<label for="" class="w-100"> CIN<b class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" maxlength="21"  id="cin"
											class="mpaInput mpa-input max mpa-has-length input-caps"
											onkeypress="lettersAndAlphabet(event)"
											onchange="checkAllField(this)" readonly> <span
											class="error-msg"></span>
										<!-- /.loader -->
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> Date of Incorporation<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="dateOfIncorporation"
											class="mpaInput mpa-input" onchange="checkAllField(this)"
											readonly>
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> Registered Email<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="companyEmailId"
											onchange="checkAllField(this)" readonly
											class="mpaInput mpa-input businessPanFunc mail-validation">
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-6 mt-15">
									<label for="" class="w-100"> Registered Address<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<textarea type="text" id="companyRegisteredAddress"
											class="mpaInput mpa-input" onchange="checkAllField(this)"
											readonly></textarea>
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-6 mt-15">
									<label for="" class="w-100"> Trading Address<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<textarea type="text" id="tradingAddress1"
											class="mpaInput mpa-input" onchange="checkAllField(this)"
											readonly></textarea>
									</div>
									<!-- /.mpaFormInput -->
									<div class="row">
										<!-- <div class="col-md-4">
													<input type="text" id="mpaCity" placeholder="City" class="mpaInput mpa-input"  >
												</div> -->
										<!-- /.col-md-3 -->
										<div class="col-md-4">
											<select name="" id="tradingCountry"
												class="mpaInput mpa-input mpa-input-select" disabled>
												<!-- <option value="">Country</option>
													<option value="delhi">delhi</option> -->

											</select>
										</div>
										<div class="col-md-4">
											<select name="" id="tradingState"
												class="mpaInput mpa-input mpa-input-select businessPanFunc"
												disabled >
												<!-- <option value="">State</option>
													<option value="delhi">delhi</option> -->
											</select>
										</div>
										<!-- /.col-md-3 -->
										<div class="col-md-4">
											<input type="text" id="tradingPin" placeholder="PIN *"
												maxlength="6" oninput="" readonly class="mpaInput mpa-has-length mpa-input"
												onkeypress="onlyDigit(event)" onchange="checkAllField(this)">
										</div>
										<!-- /.col-md-3 -->
									</div>
									<!-- /.row -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> Company PAN<b class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="businessPan" maxlength="10"
											class="mpaInput mpa-input businessPanFunc input-caps pan-validation"
											onkeypress="lettersAndAlphabet(event)"
											onchange="checkAllField(this)" readonly>
										<div class="error-msg defautl-text-color"></div>
										<!-- /.error-msg -->
										<div class="status">
											<i class="fa" aria-hidden="true"></i>
										</div>
										<!-- /.status -->
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> GST<b class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="gstin"
											class="mpaInput mpa-input input-caps"
											onkeypress="lettersAndAlphabet(event)"
											onchange="checkAllField(this)" readonly>
										<div class="error-msg"></div>
										<!-- /.error-msg -->
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> Business Phone<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" maxlength="10"
											class="mpaInput mpa-input mpa-has-length" id="companyPhone"
											onkeypress="onlyDigit(event)" onchange="checkAllField(this)"
											readonly oninput="">
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->

								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> Website<b class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="companyWebsite"
											class="mpaInput mpa-input website-validator"
											onchange="checkAllField(this)" readonly>
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-15">
									<label for="" class="w-100"> Email for Communication<b
										class="imp">*</b>
									</label>
									<div class="mpaFormRow w-100">
										<input type="text" id="businessEmailForCommunication"
											class="mpaInput mpa-input mail-validation"
											onchange="checkAllField(this)" readonly>
									</div>
									<!-- /.mpaFormInput -->
								</div>
								<!-- /.col-md-6 -->
							</div>
							<!-- /.mpSectionFormTab -->
							<div class="mpaSectionFormTab stage-box" id="stage-01">
								<input type="hidden" id="stage">
								<div class="contact-detail-stage">
									<div class="col-md-3">
										<label for="" class="w-100"> Contact Name<b
											class="imp">*</b>
										</label>
										<div class="mpaFormRow w-100">
											<input type="text" id="contactName"
												class="mpaInput mpa-input input-caps"
												onchange="checkAllField(this)"
												onkeypress="onlyLetters(event)">
										</div>
										<!-- /.mpaFormInput -->
									</div>
									<!-- /.col-md-6 -->
									<div class="col-md-3">
										<label for="" class="w-100"> Mobile<b class="imp">*</b>
										</label>
										<div class="mpaFormRow w-100">
											<input type="text" id="contactMobile"
												class="mpaInput mpa-input mpa-has-length"
												onchange="checkAllField(this)" maxlength="10"
												onkeypress="onlyDigit(event)">
										</div>
										<!-- /.mpaFormInput -->
									</div>
									<!-- /.col-md-6 -->
									<div class="col-md-3">
										<label for="" class="w-100"> Email<b class="imp">*</b>
										</label>
										<div class="mpaFormRow w-100">
											<input type="email" id="contactEmail"
												class="mpaInput mpa-input mail-validation"
												onchange="checkAllField(this)">
										</div>
										<!-- /.mpaFormInput -->
									</div>
									<!-- /.col-md-6 -->
									<div class="col-md-3">
										<label for="" class="w-100"> Landline </label>
										<div class="mpaFormRow w-100">
											<input type="text" id="contactLandline"
												class="mpaInput mpa-input landline-input"
												onchange="checkAllField(this)" maxlength="11"
												onkeypress="onlyDigit(event)">
										</div>
										<!-- /.mpaFormInput -->
									</div>
									<!-- /.col-md-6 -->
								</div>
								<!-- /.contact-detail-stage -->
								<div class="merchantSupport-all">
									<div class="col-md-12">
										<div class="header_wrapper mt-20 lpay-spaceBetween">
											<h3>Merchant Support</h3>
											<div class="checkbox-div d-flex">
												<label for="merchantSupport"
													class="checkbox-label mr-20 unchecked mb-0"> <input
													type="checkbox" id="merchantSupport"> Same as above
												</label> <label for="landlineNumber"
													class="checkbox-label ml-10 unchecked mb-0"> <input
													type="checkbox" id="landlineNumber"> Landline
													Number
												</label>
											</div>
											<!-- /.checkbox-div -->
										</div>
										<!-- /.header_wrapper -->
									</div>
									<!-- /.col-md-12 -->
									<div class="col-md-4 mt-20">
										<div class="lpay_input_group">
											<label for="">Merchant Support Email<b class="imp">*</b></label>
											<input type="text" id="merchantSupportEmailId" placeholder=""
												class="mpaInput mpa-input mail-validation"
												onchange="checkAllField(this)">
										</div>
										<!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-4 mt-20 mobileNumber-div">
										<div class="lpay_input_group">
											<label for="">Merchant Support Mobile<b class="imp">*</b></label>
											<input type="text" id="merchantSupportMobileNumber"
												class="mpaInput mpa-input mpa-has-length"
												onchange="checkAllField(this)" maxlength="10"
												onkeypress="onlyDigit(event)">
										</div>
										<!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-4 mt-20 landline-div d-none">
										<div class="lpay_input_group">
											<label for="">Merchant Support Landline<b class="imp">*</b></label>
											<input type="text" id="merchantSupportLandLine"
												class="mpaInput" onchange="checkAllField(this)"
												maxlength="11" onkeypress="onlyDigit(event)">
										</div>
										<!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
								</div>
								<!-- /.merchantSupport-all -->
								<div class="col-md-12" data-type="director one">
									<input type="hidden" id="directorName"> <label for=""
										class="w-100 mt-15"> Director 1 <b class="imp">*</b>
									</label>
									<div class="w-100">
										<div class="row">
											<div class="col-md-4 director-list-name">
												<div id="getD"></div>
												<div class="director-name-input">
													<input type="text" data-id="director1FullName"
														placeholder="Full Name *"
														class="mpaInput mpa-input input-caps"
														onchange="checkAllField(this)"
														onkeypress="onlyLetters(event)">
												</div>
												<!-- /.director-name-input -->
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="text" data-id="director1Pan"
													placeholder="PAN Number *"
													class="mpaInput mpa-input input-caps pan-validation-director pan-validation"
													maxlength="10" onchange="checkAllField(this)"
													onkeypress="lettersAndAlphabet(event)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="email" data-id="director1Email"
													placeholder="Email *"
													class="mpaInput mpa-input mail-validation"
													onchange="checkAllField(this)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="text" data-id="director1Mobile"
													placeholder="Mobile *"
													class="mpaInput mpa-input mt-20 mpa-has-length"
													maxlength="10" onkeypress="onlyDigit(event)"
													onchange="checkAllField(this)"> <input type="text"
													data-id="director1Landline" placeholder="Landline"
													class="mpaInput mpa-input mt-20 landline-input"
													maxlength="11" onkeypress="onlyDigit(event)"
													onchange="checkAllField(this)">

											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-8">
												<textarea name="" data-id="director1Address" data-name="1"
													cols="30" rows="10"
													class="mpaInput mpa-input mt-20 "
													onchange="checkAllField(this)" placeholder="Address *"></textarea>
											</div>
											<!-- /.col-md-8 -->
										</div>
										<!-- /.row -->
									</div>
									<!-- /.w-70 -->
								</div>
								<!-- /.col-md-12 -->
								<div class="col-md-12" data-type="director two">
									<label for="" class="w-100 mt-15"> Director 2 <b
										class="imp">*</b>
									</label>
									<div class="w-100">
										<div class="row">
											<div class="col-md-4 director-list-name">
												<div class="director-name-input">
													<input type="text" data-id="director2FullName"
														placeholder="Full Name *"
														class="mpaInput mpa-input input-caps"
														onkeypress="onlyLetters(event)"
														onchange="checkAllField(this)">
												</div>
												<!-- /.director-name-input -->
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="text" data-id="director2Pan"
													placeholder="PAN Number *" maxlength="10"
													class="mpaInput mpa-input input-caps pan-validation-director pan-validation"
													onkeypress="lettersAndAlphabet(event)"
													onchange="checkAllField(this)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="email" data-id="director2Email"
													placeholder="Email *"
													class="mpaInput mpa-input mail-validation"
													onchange="checkAllField(this)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="text" data-id="director2Mobile"
													placeholder="Mobile *"
													class="mpaInput mpa-input mt-20 mpa-has-length"
													maxlength="10" onchange="checkAllField(this)"
													onkeypress="onlyDigit(event)"> <input type="text"
													data-id="director2Landline" placeholder="Landline"
													class="mpaInput mpa-input mt-20 landline-input"
													maxlength="11" onkeypress="onlyDigit(event)"
													onchange="checkAllField(this)" autocomplete="nope">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-8">
												<textarea name="" data-name="2" data-id="director2Address"
													cols="30" rows="10"	class="mpaInput mpa-input mt-20"
													onchange="checkAllField(this)" placeholder="Address *"></textarea>
											</div>
											<!-- /.col-md-8 -->
										</div>
										<!-- /.row -->
									</div>
									<!-- /.w-70 -->
								</div>
								<!-- /.col-md-12 -->
								<div class="col-md-12" data-type="Proprietory">
									<label for="" class="w-100"> Proprietory Name </label>
									<div class="w-100">
										<div class="row">
											<div class="col-md-6">
												<input type="text" data-id="director1FullName"
													placeholder="Full Name *"
													class="mpaInput mpa-input input-caps"
													onkeypress="onlyLetters(event)"
													onchange="checkAllField(this)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-6">
												<input type="email" data-id="director1Email"
													placeholder="Email *"
													class="mpaInput mpa-input mail-validation"
													onchange="checkAllField(this)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-4">
												<input type="text" data-id="director1Mobile"
													placeholder="Mobile *"
													class="mpaInput mpa-input mt-20 mpa-has-length"
													onkeypress="onlyDigit(event)" maxlength="10"
													onchange="checkAllField(this)"> <input type="text"
													data-id="director1Landline" placeholder="Landline"
													class="mpaInput mpa-input mt-20 landline-input"
													onkeypress="onlyDigit(event)" maxlength="11"
													onchange="checkAllField(this)">
											</div>
											<!-- /.col-md-4 -->
											<div class="col-md-8">
												<textarea name="" data-name="2" data-id="director2Address"
													cols="30" rows="10"
													class="mpaInput mpa-input mt-20"
													onchange="checkAllField(this)" placeholder="Address *"></textarea>
												<!-- <p>Please click address box to fill address</p> -->
											</div>
											<!-- /.col-md-8 -->
											<div class="merchantSupport-propriotery">
												<div class="col-md-12">
													<div class="header_wrapper mt-20 lpay-spaceBetween">
														<h3>Merchant Support</h3>
														<div class="checkbox-div d-flex">
															<label for="merchantSupport-pro"
																class="checkbox-label mr-20 unchecked mb-0"> <input
																type="checkbox" id="merchantSupport-pro"> Same
																as above
															</label> <label for="landlineNumber-pro"
																class="checkbox-label ml-10 unchecked mb-0"> <input
																type="checkbox" id="landlineNumber-pro">
																Landline Number
															</label>
														</div>
														<!-- /.checkbox-div -->
													</div>
													<!-- /.header_wrapper -->
												</div>
												<!-- /.col-md-12 -->
												<div class="col-md-4 mt-20">
													<div class="lpay_input_group">
														<label for="">Merchant Support Email<b class="imp">*</b></label>
														<input type="text" id="merchantSupportEmail"
															placeholder="" class="mpaInput mpa-input mail-validation"
															onchange="checkAllField(this)">
													</div>
													<!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-4 mt-20 mobileNumber-div">
													<div class="lpay_input_group">
														<label for="">Merchant Support Mobile<b
															class="imp">*</b></label> <input type="text"
															id="merchantSupportMobileNumber"
															class="mpaInput mpa-input mpa-has-length"
															onchange="checkAllField(this)" maxlength="10"
															onkeypress="onlyDigit(event)">
													</div>
													<!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-4 mt-20 landline-div d-none">
													<div class="lpay_input_group">
														<label for="">Merchant Support Landline<b
															class="imp">*</b></label> <input type="text"
															id="merchantSupportLandLine" class="mpaInput"
															onchange="checkAllField(this)" maxlength="11"
															onkeypress="onlyDigit(event)">
													</div>
													<!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
											</div>
											<!-- /.merchantSupport-all -->
										</div>
										<!-- /.row -->
									</div>
									<!-- /.w-70 -->
								</div>
								<!-- /.col-md-12 -->
							</div>
							<!-- /.mpSectionFormTab -->
							<div class="mpaSectionFormTab stage-box" id="stage-02">

								<div class="col-md-12">
									<form action="" enctype="multipart/form-data"
										id="fileUploadForm">
										<label class="fileUpload" for="uploadCheque"> <img
											src="../image/uploadFile.png" alt="/" width="100"
											height="100" class="upload-img" id="checqueImg"> <span
											class="file-msg">Upload Your Cheque Here <b
												class="imp">*</b><span style="display: block;">File Format: PDF/PNG/JPG</span></span> <input type="file" data-type="checque"
											id="uploadCheque" name="chequeFile" class="upload-file" > <!-- <button class="mpaNext btn-stage">Upload</button> -->
										</label>
										<!-- /.fileUpload -->
									</form>
									<div class="verify-btn">
										<button id="verifyGo" class="verify-go">verify and go</button>
									</div>
									<!-- /.verify-btn -->
								</div>
								<!-- /.col-md-12 -->
								<div class="col-md-3 mt-30">
									<label for="" class="w-100">Account Number <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="accountNumber"
											class="mpaInput mpa-input w-70" onkeypress="onlyDigit(event)"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-30">
									<label for="" class="w-100">Account Name <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="accountHolderName"
											class="mpaInput mpa-input w-70 input-caps"
											onkeypress="onlyLetters(event)"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-30">
									<label for="" class="w-100">IFSC <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" maxlength="11" id="accountIfsc"
											class="mpaInput input-caps mpa-has-length mpa-input w-70"
											onchange="checkAllField(this)"
											onkeypress="lettersAndAlphabet(event)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-3 mt-30">
									<label for="" class="w-100">Mobile Number <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="accountMobileNumber"
											class="mpaInput mpa-input mpa-has-length w-70"
											onchange="checkAllField(this)" maxlength="10"
											onkeypress="onlyDigit(event)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
							</div>
							<!-- /.mpSectionFormTab -->
							<div class="mpaSectionFormTab stage-box" id="stage-03">
								<div class="error-msg-global"></div>
								<!-- /.error-msg-global -->
								<div class="col-md-6">
									<label for="" class="w-100">Annual Turnover (Approx) <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="annualTurnover"
											class="mpaInput mpa-input w-70"
											placeholder="Total amount in INR"
											onkeypress="onlyDigit(event)" onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-6">
									<label for="" class="w-100">Annual Turnover(online) <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="annualTurnoverOnline"
											class="mpaInput mpa-input w-70 percentage-count"
											onkeypress="onlyDigit(event)"
											placeholder="Total amount in INR"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-12">
									<div class="border-bottom mt-30"></div>
									<span class="msg-turn-over"></span>
									<!-- /.border-bottom -->
								</div>
								<!-- /.col-md-12 -->
								<div class="col-md-4 mt-30">
									<label for="" class="w-100">Credit Card <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageCC"
											class="mpaInput mpa-input w-70 percentage-count count-six-input annual-turnover"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-4 mt-30">
									<label for="" class="w-100">Debit Card <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageDC"
											class="mpaInput mpa-input w-70 percentage-count count-six-input annual-turnover"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-4 mt-30">
									<label for="" class="w-100">Net Banking <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageNB"
											class="mpaInput mpa-input w-70 percentage-count count-six-input annual-turnover"
											onkeypress="onlyDigit(event)" placeholder="in %"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-4 mt-30">
									<label for="" class="w-100">UPI <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageUP"
											class="mpaInput mpa-input w-70 percentage-count count-six-input annual-turnover"
											onkeypress="onlyDigit(event)" placeholder="in %"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-4 pr-30 mt-30">
									<label for="" class="w-100">Wallets <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageWL"
											class="mpaInput mpa-input w-70 percentage-count count-six-input annual-turnover"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-4 mt-30">
									<label for="" class="w-100">EMI <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageEM"
											class="mpaInput mpa-input w-70 percentage-count count-six-input annual-turnover"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-6 mt-30">
									<label for="" class="w-100">COD/Cash <b class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageCD"
											class="mpaInput mpa-input w-70 percentage-count count-six-input"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-6 mt-30">
									<label for="" class="w-100">NEFT/IMPS/RTGS <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageNeftOrImpsOrRtgs"
											class="mpaInput mpa-input w-70 percentage-count count-six-input"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->

								<div class="col-md-12">
									<div class="border-bottom mt-30"></div>
									<!-- /.border-bottom -->
									<span class="msg-card-turn-over"></span>
								</div>
								<!-- /.col-md-12 -->
								<div class="col-md-6 mt-30">
									<label for="" class="w-100">Total Cards Domestic <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageDomestic"
											class="mpaInput mpa-input w-70 percentage-count count-two-input"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
								<div class="col-md-6 mt-30">
									<label for="" class="w-100">Total Cards International <b
										class="imp">*</b></label>
									<div class="mpaFormRow w-100">
										<input type="text" id="percentageInternational"
											class="mpaInput mpa-input w-70 percentage-count count-two-input"
											onkeypress="onlyDigit(event)"
											placeholder="Total no of transactions in percentage"
											onchange="checkAllField(this)">
									</div>
									<!-- /.mpaFormRow -->
								</div>
								<!-- /.col-md-6 -->
							</div>
							<!-- /.mpSectionFormTab -->
							

							<div class="mpaSectionFormTab stage-box" id="stage-04">
								<div class="col-md-12">
									<span>File Format: <b>PDF/PNG/JPG</b></span>
                                        <span style="display:block;margin-top: 5px">File Size: <b>2 MB</b></span>
									<form action="" method="post" enctype="multipart/form-data"
										class="upload-generic" style="margin-top: 20px">
										<div class="upload-policy mb-30">
											<div class="main-heading">Please upload your refund
												policy</div>
											<label for="" class="generic-upload"> <img
												src="../image/add-file.png" alt="" width="50"> <span>
													<b>*</b>Refund Policy
											</span> <input type="file"  data-type="RefundPolicy" name="file" class="generic-uploader mpa-input" onchange="checkAllField(this)" />
												<div class="loader-container"></div>
												
											</label>
											<a href="#" data-info="refundPolicy" class="mpaNext termPolicy refundPolicyButton">Policy Sample</a>
										</div>
										<!-- /.upload-policy -->
										<div class="limited">
											<div class="limited-inner">
												<div class="main-heading">ID Proof of All the Trustees
													(Self Attested and with company seal)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Pan Card copy of the Authorized Signatory One
												</span> <input type="file" data-type="panDirectorOne" name="file"
													accept="" class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label>
												<!-- <input type="file" data-type="PanCard" name="file" accept=".png, .jpg, .pdf" class="generic-uploader mpa-input" onchange="checkAllField(this)" Passport> -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Pan Card copy of the Authorized Signatory Two
												</span> <input type="file" data-type="panDirectorTwo" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Identification Documents of all the Authorized
														Signatories , Aadhaar Card/DL/Voter ID Card
												</span> <input type="file" data-type="KYC" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.limited-inner -->
											<div class="limited-inner mt-30">
												<div class="main-heading">Address Proof (With company
													seal and signed by authorized signatories)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Address
														Proof for the company (Offices Utility Bills or Rental
														Agreement if premises are on rent)</span> <input type="file"
													data-type="companyAddress" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.limited-inner -->
											<div class="limited-inner mt-30">
												<div class="main-heading">Company Proof</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Articles
														of Association (AOA)</span> <input type="file" data-type="AOA"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Memorandum
														of Association (MOA)</span> <input type="file" data-type="MOA"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Board
														resolution (Sample Attached)</span> <input type="file"
													data-type="BR" name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>List
														of Directors from MCA</span> <input type="file" data-type="MCA"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Certification
														of Incorporation</span> <input type="file" data-type="PanCard"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>PAN
														of the Company</span> <input type="file" data-type="panCompany"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>GST
														Registration Certificate</span> <input type="file"
													data-type="GST" name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.limited-inner -->
											<div class="limited-inner mt-30">
												<div class="main-heading">Financial Proof</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Bank
														Account Details on your company letter head (signed and
														sealed)</span> <input type="file" data-type="bankDetails"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Cancelled
														Cheque </span> <input type="file" data-type="cheque" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Bank
														statement of last 3 months/Income Tax return (signed and
														sealed)</span> <input type="file" data-type="bankStatement"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.limited-inner -->
										</div>
										<!-- /.limited -->
										<div class="partnership">
											<div class="partnership-inner">
												<div class="main-heading">ID Proof (Self Attested)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>PAN card copies of Partner one with self-attested
												</span> <input type="file" data-type="pan1" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>PAN card copies of Partner two with self-attested
												</span> <input type="file" data-type="pan2" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Aadhaar Card/Passport/Driving License/Election
														Card of Partner one self-attestation
												</span> <input type="file" data-type="KYC1" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Aadhaar Card/Passport/Driving License/Election
														Card of Partner two self-attestation
												</span> <input type="file" data-type="KYC2" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Passport size photographs One
												</span> <input type="file" data-type="photo1" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Passport size photographs Two
												</span> <input type="file" data-type="photo2" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label>
											</div>
											<!-- /.partnership-inner -->
											<div class="partnership-inner mt-30">
												<div class="main-heading">Address Proof (With sign and
													seal)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Address Proof (Utility Bills or Rental Agreement
														if premises is on rent with sign and company seal)
												</span> <input type="file" data-type="pan1" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
													<div class="loader-container"></div>
												</label>
											</div>
											<!-- /.partnership-inner -->
											<div class="partnership-inner mt-30">
												<div class="main-heading">Company Proof for
													Partnership Firm / Limited Liability Partnership (With sign
													and seal)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Company PAN Card with sign & stamp
												</span> <input type="file" data-type="panEntity" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Partnership Deed with signed and stamped by
														authorized partners
												</span> <input type="file" data-type="deed" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.partnership-inner -->
											<div class="partnership-inner mt-30">
												<div class="main-heading">Financial Proof</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Bank
														Account Details on your company letter head (signed and
														sealed)</span> <input type="file" data-type="bankDetails"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Cancelled
														Cheque </span> <input type="file" data-type="cheque" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Bank
														statement of last 3 months/Income Tax return (signed and
														sealed)</span> <input type="file" data-type="bankStatement"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.partnership-inner mt-30 -->
										</div>
										<!-- /.partnership -->
										<div class="proprietorship">
											<div class="proprietorship-inner">
												<div class="main-heading">ID Proof (Self Attested with
													company seal)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Pan Card copy of the Proprietor
												</span> <input type="file" data-type="panPropriotery" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Identification Documents of the Proprietor
														-Passport (with the address page)/Aadhaar Card/DL/Voter ID
														Card
												</span> <input type="file" data-type="identificationPropriotery"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.proprietorship-inner -->
											<div class="proprietorship-inner mt-30">
												<div class="main-heading">Address Proof (With sign and
													company seal)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Address Proof (Utility Bills or Rental Agreement
														if premises are on rent)
												</span> <input type="file" data-type="addressPropriotery"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.proprietorship-inner -->
											<div class="proprietorship-inner mt-30">
												<div class="main-heading">Proof for Sole Proprietor
													(With company seal and sign)</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>GST Registration Certificate
												</span> <input type="file" data-type="gstPropriotery" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>Shop
														Establishment Certificate / Company Registration
														Certificate/ Trade License etc</span> <input type="file"
													data-type="tradePropriotery" name="file"
													accept=".png, .jpg, .pdf" class="generic-uploader">
												</label>
											</div>
											<!-- /.proprietorship-inner -->
											<div class="proprietorship-inner mt-30">
												<div class="main-heading">Financial Proof</div>
												<!-- /.main-heading -->
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Bank
														Account Details on your company letter head (signed and
														sealed)</span> <input type="file" data-type="bankDetails"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Cancelled
														Cheque </span> <input type="file" data-type="cheque" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span><b>*</b>Bank
														statement of last 3 months/Income Tax return (signed and
														sealed)</span> <input type="file" data-type="bankStatement"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.proprietorship-inner mt-30 -->
										</div>
										<!-- /.proprietorship -->
										<div class="otherEntity">
											<div class="otherEntity-inner">
												<div class="main-heading">ID Proof of All the Trustees
													(Self Attested and with company seal)</div>
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>PAN card copy with self -attested
												</span> <input type="file" data-type="panOther" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Passport Copy with address page OR Driving License
														OR Election Card
												</span> <input type="file" data-type="identificationOther"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.otherEntity-inner -->
											<div class="otherEntity-inner mt-30">
												<div class="main-heading">Address Proof (With company
													seal and signed by authorized signatories)</div>
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b> Offices-Utility Bills or Rental Agreement if
														premises are on rent
												</span> <input type="file" data-type="adderssOther" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.otherEntity-inner -->
											<div class="otherEntity-inner mt-30">
												<div class="main-heading">Company Proof for Trust /
													Society / Education Institute / Government</div>
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Company PAN card with sign & stamp
												</span> <input type="file" data-type="companyPanOther" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Trust Deed / Memorandum of Understanding / Society
														Deed / Government Certificate etc with sign & company
														stamp
												</span> <input type="file" data-type="registrationCert" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>Resolution Deed on organization's letterhead duly
														signed & stamped by minimum 2 Trustees /members.
												</span> <input type="file" data-type="resolutionDeed" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>(KYC required who has Authorized signatories in
														Resolution Deed-Collect those members / Trustees KYC)
												</span> <input type="file" data-type="kycOther" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.otherEntity-inner -->
											<div class="otherEntity-inner mt-30">
												<div class="main-heading">Financial Proof</div>
												<label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>2-year ITR with audited balance sheet or 1 year
														current account statement
												</span> <input type="file" data-type="itrOther" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>
														<b>*</b>In case of new establishment 6 month saving
														account statements of signing authority with one
														undertaking
												</span> <input type="file" data-type="finencialProof" name="file"
													accept=".png, .jpg, .pdf"
													class="generic-uploader mpa-input"
													onchange="checkAllField(this)">
												</label> <label for="" class="generic-upload"> <img
													src="../image/add-file.png" alt="" width="50"> <span>Bank
														Account Details on your company letter head (signed and
														sealed)</span> <input type="file" data-type="bankDetails"
													name="file" accept=".png, .jpg, .pdf"
													class="generic-uploader" onchange="checkAllField(this)">
												</label>
											</div>
											<!-- /.otherEntity-inner -->
										</div>
										<!-- /.otherEntity -->
									</form>
								</div>
								<!-- /.col-md-12 -->
							</div>
							<!-- /.mpSectionFormTab -->
							<div class="mpaSectionFormTab stage-box" id="stage-05">
								<div class="mpaSectionData" >
									<div class="mpaSectionData-heading">
										<h3>Merchant Details</h3>
									</div>
									<!-- /.mapSectionData-title -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Industry Category
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="industoryCategory"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Type Of Entity
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="typeOfEntity"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Legal Name
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="companyName"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											CIN
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="cin"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Registration Number
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="registrationNumber"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Date Of Incorporation
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="dateOfIncorporation"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Registered Email
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="companyEmailId"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Trading Country
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="tradingCountry"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Trading State
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="tradingState"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											PIN
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="tradingPin"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Company PAN
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="businessPan"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											GST
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="gstin"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Business Phone
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="companyPhone"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Website
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="companyWebsite"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Email for Communication
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="businessEmailForCommunication"></div>
									</div>
									<!-- /.mpaSectionData-box -->
								</div>
								<!-- /.mpaSectionData -->
								<div class="mpaSectionData" >
									<div class="mpaSectionData-heading">
										<h3>Principal Information</h3>
									</div>
									<!-- /.mapSectionData-title -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Contact Name
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="contactName"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Mobile
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="contactMobile"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Email
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="contactEmail"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Landline
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="contactLandline"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Merchant Support Email
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="merchantSupportEmailId"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Merchant Support Mobile
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="merchantSupportMobileNumber"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Merchant Support Landline
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="merchantSupportLandLine"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-1 Name
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director1FullName"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-1 PAN
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director1Pan"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-1 Email
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director1Email"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-1 Mobile
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director1Mobile"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-1 Landline
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director1Landline"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-1 Address
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director1Address"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-2 Name
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director2FullName"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-2 PAN
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director2Pan"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-2 Email
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director2Email"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-2 Mobile
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director2Mobile"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-2 Landline
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director2Landline"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											<span data-target='attorney'>Director</span>-2 Address
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="director2Address"></div>
									</div>
									<!-- /.mpaSectionData-box -->
								</div>
								<!-- /.mpaSectionData -->
								<div class="mpaSectionData">
									<div class="mpaSectionData-heading">
										<h3>Account Details</h3>
									</div>
									<!-- /.mapSectionData-title -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Account Number
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="accountNumber"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Account Name
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="accountHolderName"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Bank IFSC
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="accountIfsc"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Mobile Number
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="accountMobileNumber"></div>
									</div>
									<!-- /.mpaSectionData-box -->
								</div>
								<!-- /.mpaSectionData -->
								<div class="mpaSectionData" >
									<div class="mpaSectionData-heading">
										<h3>Business Details</h3>
									</div>
									<!-- /.mapSectionData-title -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Annual Turnover (Approx)
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="annualTurnover"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Annual Turnover(online)
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="annualTurnoverOnline"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Credit Card
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageCC"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Debit Card 
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageDC"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Net Banking
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageNB"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											UPI
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageUP"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Wallets
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageWL"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											EMI
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageEM"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											COD/Cash
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageCD"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											NEFT/IMPS/RTGS
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageNeftOrImpsOrRtgs"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Total Cards Domestic
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageDomestic"></div>
									</div>
									<!-- /.mpaSectionData-box -->
									<div class="mpaSectionData-box">
										<div class="mpaSectionData-title">
											Total Cards International
										</div>
										<!-- /.mpaSectionData-title -->
										<div data-id="percentageInternational"></div>
									</div>
									<!-- /.mpaSectionData-box -->
								</div>
								<!-- /.mpaSectionData -->
							</div>
							<!-- mpaSectionFormTab -->
						</div>
						<!-- /.col-md-12 -->
						<!-- <button id="btn">button</button> -->
						<div
							class="col-md-12 d-flex justify-content-center mt-30 button-wrapper">
							<button id="btn-prev-stage"
								class="lpay_button lpay_button-md lpay_button-secondary btn-stage inactive"
								data-stage="00">Previous</button>
							<!-- /.mpaPrev -->
							<button id="btn-next-stage"
								class="lpay_button lpay_button-md lpay_button-primary btn-stage"
								disabled>Next</button>
							<!-- /.mpaPrev -->
						</div>
						<!-- /.col-md-12 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.container --> </section>
				<!-- /.mpaSection -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row --> </section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	</div>


	<!-- term and condition popup -->

	<!-- /.term-condition-popup -->

	<!-- <button id="button">open popup</button> -->

	<div class="invoice-popup">
		<div class="invoice-popup-wrapper">
			<div class="invoice-popup-box" tabindex="-1">
				<div class="icon-box">
					<i class="fa fa-check" aria-hidden="true"></i>
				</div>
				<!-- /.icon-box -->
				<p>Your form has been submitted successfully</p>
			</div>
			<!-- /.invoice-popup-box -->
		</div>
		<!-- /.invoice-popup-wrapper -->
	</div>
	<!-- /.invoice-popup -->

	<div class="lpay_popup_confirm"  id="fancybox">
        <div class="lpay_popup_confirm_box text-center">
            <div class="lpay_popup_box_icon">
                <span class="lpay_popup_icon">!</span>
            </div>
            <!-- /.confirm-box-icon -->
            <div class="lpay_confirm_delete_text">
                <span>Please review the details before submission</span>
            </div>
            <!-- /.confirm-delete-text -->
            <div class="confirm-delete-button">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn">Review</button>
                <button  class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">Submit</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
	<!-- /.confrim-popup -->
	
	<form name="downloadTnCPolicy" method="POST" id="downloadTnCPolicy" action="downloadTnCPolicy">
		<input type="hidden" name="docFile" value="" id="docFile" />
	</form>

			<script
				src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
			<script src="../js/jquery-ui.js"></script>
			<script src="../js/jquery.fancybox.min.js"></script>
			<script src="../js/bootstrap.min.js"></script>
			<script src="../js/city_state.js"></script>

			<script type="text/javascript"
				src="../js/scripts-merchant-offline.js"></script>
</body>
</html>