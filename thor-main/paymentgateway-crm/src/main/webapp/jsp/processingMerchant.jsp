<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="ie=edge">
<title>Processing Merchant</title>
<link
	href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600,700&display=swap"
	rel="stylesheet">
	<link rel="preconnect" href="https://fonts.gstatic.com">
<link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,300;0,400;0,500;0,600;1,100&display=swap" rel="stylesheet">
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/jquery-ui.css">
<link rel="stylesheet" href="../fonts/css/font-awesome.css">
<link rel="stylesheet" href="../css/jquery.fancybox.min.css">
<link rel="stylesheet" href="../css/styles.css">
<link rel="stylesheet" href="../css/mpaStyle.css">
<!-- <link rel="stylesheet" href="../css/mpa-stylesheet.css"> -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">

<style>





.defautl-text-color {
	color: #888888 !important;
}

.redLine{
	border: 1px solid #f00;
}

.redLine .file-msg{
	color: #f00;
}

.refundPolicyButton {
    width: auto;
    padding: 7px 20px;
    margin-top: 10px;
    display: inline-block;
    font-size: 10px;
    margin-left: 38px;
    font-size: 10px;
    font-weight: 400;
}

.refundPolicyButton:hover, .refundPolicyButton:focus{
	color: #fff;
	text-decoration: none;
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

.term-condition {
	max-height: 500px;
	overflow-y: auto;
}

.term-condition h3 {
	margin-bottom: 20px;
}

.term-condition p {
	font-size: 14px;
	line-height: 30px;
	font-weight: 300;
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



.mpaHeader{
	background-color: #1c1e22;
}

.mpaLogo{
	padding-left: 15px;
}

.mpaLogo img{
	width: 78px;
}

body {
	background-color: #eee;
}

.lpay_select_group[readonly]{	
	pointer-events: none;
}

.loader-text{
	font-size: 18px;
	width: 100%;
	text-align: center;
	margin-top: 16px;
}

</style>
</head>

<body>

	<div class="loader-wrapper">
		<div class="loader-container lpay-center" style="flex-direction: column;">
			<div class="loaderImage">
				<img src="../image/loader.gif" alt="Loader">
			</div>
		</div>
	</div>
	<!-- /.loader-wrapper -->


	<s:hidden value="%{onlineMpaFlag}" id="onlineMpaFlag"></s:hidden>
	<s:hidden value="%{payId}" id="merchantPayId"></s:hidden>
	<div class="mpa-main-container">
		<section
			class="processing-merchant lapy_section white-bg box-shadow-box">
			<div class="heading-section bg-gradient-primary">
				<div class="col-md-3" style="min-height: 32px;">
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-6">
					<div class="heading_with_icon">
						<h2 class="heading_text w-100 text-center">Merchant Processing Application
							System</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-3">
					<form id="mpaLogout" name="mpaLogout" method="post" action="logout">
						<input type="submit" id="button"
							class="lpay_button lpay_button-md lpay_button-secondary logout-btn"
							value="Logout" />
					</form>
				</div>
				<!-- /.col-md-3 -->
			</div>
			<!-- /.heading-section -->
			<div class="row">
				<div class="col-md-12">
					<section class="mpaSection">
						<div class="row">
							<div class="col-md-12 p-0" id="mpaSectionIndicator">
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
									<h4>Bank Details</h4>
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
								
									<div class="col-md-3 mb-20">
										<div class="lpay_select_group">
										   <label for="">Industry Category <span class="imp">*</span></label>
											<select data-company="industryCategory" name="industryCategory" class="selectpicker" id="industryCategory">
												
											</select>
										</div>
										<!-- /.lpay_select_group -->  
									</div>
									<!-- /.col-md-3 -->
									<div class="col-md-3 mb-20">
										<div class="lpay_select_group">
										   <label for="">Select Entity <span class="imp">*</span></label>
											<select name="typeOfEntity" data-company="typeOfEntity" disabled id="typeOfEntity" class="selectpicker">
												<option value="">Select Entity</option>
												<option value="Private Limited">Private Limited</option>
												<option value="Public Limited">Public Limited</option>
												<option value="Partnership Firm">Partnership Firm</option>
												<option value="Proprietory">Proprietary</option>
												<option value="Other">Other</option>
											</select>
										</div>
										<!-- /.lpay_select_group -->  
									</div>
									<!-- /.col-md-3 -->
									<div class="col-md-3 mb-20 d-none" data-id="otherEntity">
									  <div class="lpay_input_group">
										<label for="">Enter Entity <span class="imp">*</span></label>
										<input type="text" data-link="entity" id="otherEntity" class="lpay_input mpa-input">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Legal Name <span class="imp">*</span></label>
										<input 
											data-gst="companyName"
											data-link="entity"
											type="text" 
											id="companyName"
											data-company="legalName" 
											data-length="50"
											class="lpay_input mpa-input companyNameApi"
											onkeypress="lettersAndAlphabet(event);"
											onchange="checkAllField(this)" 
											readonly
										/>
										<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for=""><span data-label="cin">CIN</span><span class="imp">*</span></label>
										<input 
											data-link="entity"
											type="text" data-company="cin" id="cin"
											class="lpay_input mpa-input mpa-has-length input-caps"
											onkeypress="lettersAndAlphabet(event)"
											maxlength="21"
											onchange="checkAllField(this)" 
											readonly />
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Date Of Incorporation<span class="imp">*</span></label>
										<input 
											data-link="entity"
											type="text" 
											id="dateOfIncorporation"
											class="lpay_input mpa-input" 
											onchange="checkAllField(this)"
											readonly>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Registered Email <span class="imp">*</span></label>
										<input 
											data-gst="companyEmailId"
											data-link="entity"
											type="text" 
											id="companyEmailId"
											onchange="checkAllField(this);regEx(this)"
											readonly
											
											class="lpay_input mpa-input businessPanFunc" 
											data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
											/>
											<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Registered Address <span class="imp">*</span></label>
										<input 
											data-link="entity"
											type="text"
											id="companyRegisteredAddress"
											class="lpay_input mpa-input"
											onchange="checkAllField(this);"
											readonly
										/>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Trading Address <span class="imp">*</span></label>
										<input 
											data-link="entity"
											style="height: 28px;margin-bottom: 0;"
											type="text"
											id="tradingAddress1"
											class="lpay_input mpa-input"
											onchange="checkAllField(this)"
											readonly
										/>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
										<div class="lpay_select_group">
										   <label for="">Select Country <span class="imp">*</span></label>
										   <select name="" data-link="entity" data-live-search="true" id="tradingCountry" class="selectpicker mpa-input mpa-input-select" disabled >
										</select>
										</div>
										<!-- /.lpay_select_group -->  
									</div>
									<!-- /.col-md-3 -->
									<div class="col-md-3 mb-20">
										<div class="lpay_select_group">
											<label for="">Select State <span class="imp">*</span></label>
											<select name="" data-live-search="true" data-gst="tradingState" data-link="entity" id="tradingState" class="selectpicker mpa-input mpa-input-select businessPanFunc" disabled>
									   		</select>
										</div>
										<!-- /.lpay_select_group -->  
									</div>
									<!-- /.col-md-3 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">PIN <span class="imp">*</span></label>
										<input 

											type="text" 
											id="tradingPin"
											maxlength="6" 
											oninput="" 
											readonly
											data-link="entity"
											data-reg="[0-9]{6}"
											class="lpay_input mpa-input" onkeypress="onlyDigit(event)"
											onkeydown="removeError(this)"
											onchange="checkAllField(this);regEx(this)">
											<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Company PAN <span class="imp">*</span></label>
										<input type="text" data-gst="gst" id="businessPan" maxlength="10"
										data-reg="^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$"
										class="lpay_input mpa-input businessPanFunc input-caps"
										onkeypress="lettersAndAlphabet(event)"
										data-link="entity"
										onkeydown="removeError(this)"
										onchange="checkAllField(this);" onblur="regEx(this)" readonly>
										<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">GST <span class="imp">*</span></label>
										<input type="text" id="gstin" 
										data-link="entity"
										class="lpay_input mpa-input input-caps" maxlength="20"
										data-reg="[0-9]{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z0-9]{1}"
										onkeypress="lettersAndAlphabet(event)" onkeydown="removeError(this)"
										onchange="checkAllField(this)" onblur="regEx(this)" readonly>
										<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Business Phone <span class="imp">*</span></label>
										<input type="text" maxlength="10"
											class="lpay_input mpa-input" id="companyPhone"
											data-reg="[0-9]{10}"
											data-link="entity"
											onkeypress="onlyDigit(event)" onkeydown="removeError(this)" onblur="regEx(this)" onchange="checkAllField(this);"
											readonly oninput="">
											<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Website <span class="imp">*</span></label>
										<input type="text" id="companyWebsite"
										data-reg="^([A-Za-z'])+\.([A-Za-z0-9'])+\.([A-Za-z]{2,6})$"
											class="lpay_input mpa-input"
											data-link="entity"
											onchange="checkAllField(this)" onblur="regEx(this)" onkeydown="removeError(this)" readonly>
											<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Email for Communication <span class="imp">*</span></label>
										<input type="text" id="businessEmailForCommunication"
										data-link="entity"
										data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
										class="lpay_input mpa-input" onchange="checkAllField(this)" onkeydown="removeError(this)" onblur="regEx(this)" readonly>
										<p class="mpa-msg"></p>
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
								</div>
								<!-- /.mpSectionFormTab -->
								<div class="mpaSectionFormTab stage-box" id="stage-01">
									<input type="hidden" id="stage">
									<div class="contact-detail-stage w-100" data-hide="contact-detail">
										<div class="col-md-3 mb-20">
										  <div class="lpay_input_group">
											<label for="">Contact Name <span class="imp">*</span></label>
											<input type="text" id="contactName"
											class="lpay_input mpa-input input-caps"
											onchange="checkAllField(this)"
											onkeypress="onlyLetters(event)">
										  </div>
										  <!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
										<div class="col-md-3 mb-20">
										  <div class="lpay_input_group">
											<label for="">Contact Mobile <span class="imp">*</span></label>
											<input type="text" id="contactMobile"
													class="lpay_input mpa-input"
													data-reg="[0-9]{10}"
													onchange="checkAllField(this)" onblur="regEx(this)" onkeydown="removeError(this)" maxlength="10"
													onkeypress="onlyDigit(event)">
													<p class="mpa-msg"></p>
										  </div>
										  <!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
										<div class="col-md-3 mb-20">
										  <div class="lpay_input_group">
											<label for="">Contact Email <span class="imp">*</span></label>
											<input type="email" id="contactEmail"
												class="lpay_input mpa-input"
												data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
												onblur="regEx(this)"
												onkeydown="removeError(this)"
												onchange="checkAllField(this)"
											/>
											<p class="mpa-msg"></p>

										  </div>
										  <!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
										<div class="col-md-3 mb-20">
										  <div class="lpay_input_group">
											<label for="">Contact Landline</label>
											<input type="text" id="contactLandline"
													class="lpay_input mpa-input"
													data-reg="[0-9]{11}"
													onblur="regEx(this)"
													onkeydown="removeError()"
													onchange="checkAllField(this)" maxlength="11"
													onkeypress="onlyDigit(event)">
													<p class="mpa-msg"></p>
										  </div>
										  <!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
									</div>
									<!-- /.contact-detail-stage -->
									<div class="merchantSupport-all" data-hide="merchant-support">
										<div class="col-md-12">
											<div class="header_wrapper lpay-spaceBetween">
												<h3>Merchant Support</h3>
												<div class="checkbox-div d-flex">
													<label for="merchantSupport"
														class="checkbox-label mr-20 unchecked mb-0" style="margin-right: 8px;"> <input
														type="checkbox" id="merchantSupport"> Same as
														above
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
												<label for="">Merchant Support Email<span class="imp">*</span></label>
												<input type="text" id="merchantSupportEmailId"
													data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
													placeholder="" class="lpay_input mpa-input"
													onblur="regEx(this)"
													onkeydown="removeError(this)"
													onchange="checkAllField(this)"
												/>
												<p class="mpa-msg"></p>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
										<div class="col-md-4 mt-20 mobileNumber-div">
											<div class="lpay_input_group">
												<label for="">Merchant Support Mobile<span class="imp">*</span></label>
												<input type="text" id="merchantSupportMobileNumber"
													class="lpay_input mpa-input"
													data-reg="[0-9]{10}"
													onblur="regEx(this)"
													onkeydown="removeError(this)"
													onchange="checkAllField(this)" maxlength="10"
													onkeypress="onlyDigit(event)">
													<p class="mpa-msg"></p>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
										<div class="col-md-4 mt-20 landline-div d-none">
											<div class="lpay_input_group">
												<label for="">Merchant Support Landline<span
													class="imp">*</span></label> <input type="text"
													id="merchantSupportLandLine" class="lpay_input"
													data-reg="[0-9]{11}"
													onblur="regEx(this)"
													onkeydown="removeError(this)"
													onchange="checkAllField(this)" maxlength="11"
													onkeypress="onlyDigit(event)">
													<p class="mpa-msg"></p>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
									</div>
									<!-- /.merchantSupport-all -->
									<div class="col-md-12" data-type="director one">
										<input type="hidden" id="directorName"> 
										<label for=""
											class="w-100 mt-15 mb-20"> <span data-label="director1Label">Director 1</span>
										</label>
										<div class="w-100">
											<div class="row">
												<div class="col-md-4 d-none director-list-name">
													<div id="getD"></div>
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
													<div class="director-detail" data-director="director1Detail">
														
													</div>
													<!-- /.director-detail -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">PAN Number <span class="imp">*</span></label>
													<input type="text" id="director1Pan"
													data-reg="^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$"
													class="lpay_input mpa-input input-caps"
													onblur="regEx(this);checkDuplicacy(this)"
													onkeydown="removeError(this)"
													maxlength="10" onchange="checkAllField(this)"
													onkeypress="lettersAndAlphabet(event)">
													<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">Email <span class="imp">*</span></label>
													<input type="email" id="director1Email"
														class="lpay_input mpa-input"
														data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
														onblur="regEx(this);checkDuplicacy(this)"
														onkeydown="removeError(this)"
														onchange="checkAllField(this)">
														<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">Mobile <span class="imp">*</span></label>
													<input type="text" id="director1Mobile"
													class="lpay_input mpa-input"
													maxlength="10" onkeypress="onlyDigit(event)"
													data-reg="[0-9]{10}"
													onblur="regEx(this);checkDuplicacy(this)"
													onkeydown="removeError(this)"
													onchange="checkAllField(this)">
													<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">Landline</label>
													<input type="text"
													id="director1Landline"
													data-reg="[0-9]{11}"
													class="lpay_input mpa-input"
													onblur="regEx(this)"
													onkeydown="removeError(this)"
													maxlength="11" onkeypress="onlyDigit(event)"
													onchange="checkAllField(this)">
													<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-9 mb-20">
												  <div class="lpay_input_group">
													<label for="">Address <span class="imp">*</span></label>
													<input type="text"
													name="" id="director1Address" data-name="1"
														cols="30" rows="10"
														class="lpay_input mpa-input"
														onchange="checkAllField(this)"
													>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
											</div>
											<!-- /.row -->
										</div>
										<!-- /.w-70 -->
									</div>
									<!-- /.col-md-12 -->
									<div class="col-md-12" data-hide="directorTwo" data-type="director two">
										<label for="" class="w-100 mt-15 mb-20"> <span data-label="director2Label">Director 2</span>
										</label>
										<div class="w-100">
											<div class="row">
												<div class="col-md-3 mb-20">
													<div class="director-detail" data-director="director2Detail">
														
													</div>
													<!-- <div class="lpay_input_group">
														<label for="">Full Name <span class="imp">*</span></label>
														<input type="text" id="director2FullName"
																class="lpay_input mpa-input input-caps"
																onkeypress="onlyLetters(event)"
																onchange="checkAllField(this)">
													</div> -->
													<!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">PAN Number <span class="imp">*</span></label>
													<input type="text" id="director2Pan" maxlength="10"
														data-reg="^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$"
														class="lpay_input mpa-input input-caps"
														onblur="regEx(this);checkDuplicacy(this)"
														onkeydown="removeError(this)"
														onkeypress="lettersAndAlphabet(event)"
														onchange="checkAllField(this)">
														<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">Email <span class="imp">*</span></label>
													<input type="email" id="director2Email"
														data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
														class="lpay_input mpa-input"
														onblur="regEx(this);checkDuplicacy(this)"
														onkeydown="removeError(this)"
														onchange="checkAllField(this)">
														<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">Mobile <span class="imp">*</span></label>
													<input type="text" id="director2Mobile"
													class="lpay_input mpa-input"
													data-reg="[0-9]{10}"
													onblur="regEx(this);checkDuplicacy(this)"
													onkeydown="removeError(this)"
													maxlength="10" onchange="checkAllField(this)"
													onkeypress="onlyDigit(event)">
													<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-3 mb-20">
												  <div class="lpay_input_group">
													<label for="">Landline</label>
													<input type="text"
													id="director2Landline"
													class="lpay_input mpa-input landline-input"
													data-reg="[0-9]{11}"
													onblur="regEx(this)"
													onkeydown="removeError(this)"
													maxlength="11" onkeypress="onlyDigit(event)"
													onchange="checkAllField(this)" autocomplete="nope">
													<p class="mpa-msg"></p>
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
												<div class="col-md-9 mb-20">
												  <div class="lpay_input_group">
													<label for="">Address <span class="imp">*</span></label>
													<input type="text" onchange="checkAllField(this)" class="lpay_input mpa-input" id="director2Address">
												  </div>
												  <!-- /.lpay_input_group -->
												</div>
												<!-- /.col-md-4 -->
											</div>
											<!-- /.row -->
										</div>
										<!-- /.w-70 -->
									</div>
									<!-- /.col-md-12 -->
									
								</div>
								<!-- /.mpSectionFormTab -->
								<div class="mpaSectionFormTab stage-box" id="stage-02">

									<div class="col-md-12 mb-20">
										<form action="" enctype="multipart/form-data"
											id="fileUploadForm">
											<label class="fileUpload" for="uploadCheque"> <img
												src="../image/uploadFile.png" alt="/" width="100"
												height="100" class="upload-img" id="checqueImg"> <span
												class="file-msg">Upload Your Cheque Here <b
													class="imp">*</b>
													<span style="display: block;">File Format: PDF/PNG/JPG</span>
												</span>
													<input type="file" data-type="checque"
												id="uploadCheque" onchange="checkAllField(this)" name="chequeFile" class="upload-file mpa-input"
												> <!-- <button class="mpaNext btn-stage">Upload</button> -->
											</label>
											<!-- /.fileUpload -->
										</form>
									</div>
									<!-- /.col-md-12 -->
									<div class="col-md-3 mb-20">
										<div class="lpay_input_group">
											<label for="">Account Number <span class="imp">*</span></label>
											<input type="text" id="accountNumber" data-var='beneAccountNumber' class="lpay_input mpa-input w-70 bank-details" maxlength="20"
											onkeypress="onlyDigit(event)" onchange="checkAllField(this);" >
											<div class="success-tick">
												<img src="../image/right-tick.png" alt="">
											</div>
											<!-- /.success-tick -->
										</div>
									  	<!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
										<div class="lpay_input_group">
											<label for="">Account Name</label>
											<input type="text" data-var='beneName' id="accountHolderName"
											class="lpay_input mpa-input w-70 input-caps bank-details"
											onkeypress="onlyLetters(event)"
											onchange="checkAllField(this);">
											<div class="success-tick">
												<img src="../image/right-tick.png" alt="">
											</div>
											<!-- /.success-tick -->
										</div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  	<div class="lpay_input_group">
											<label for="">IFSC <span class="imp">*</span></label>
											<input type="text" data-var='beneIfsc' data-reg="[a-zA-Z0-9]{11}" id="accountIfsc" maxlength="11"
											class="lpay_input input-caps mpa-input bank-details"
											onblur="regEx(this)"
											onkeydown="removeError(this)"
											onchange="checkAllField(this);"
											onkeypress="lettersAndAlphabet(event)">
											<p class="mpa-msg"></p>
											<div class="success-tick">
												<img src="../image/right-tick.png" alt="">
											</div>
											<!-- /.success-tick -->
									  	</div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  	<div class="lpay_input_group">
											<label for="">Mobile Number <span class="imp">*</span></label>
											<input type="text" id="accountMobileNumber"
											class="lpay_input mpa-input"
											data-reg="[0-9]{10}"
											onblur="regEx(this)"
											onkeydown="removeError(this)"
											onchange="checkAllField(this);" maxlength="10"
											onkeypress="onlyDigit(event)">
											<p class="mpa-msg"></p>
									  	</div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
								</div>
								<!-- /.mpSectionFormTab -->
								<div class="mpaSectionFormTab stage-box" id="stage-03">

									<div class="error-msg-global"></div>
									<!-- /.error-msg-global -->
									<div class="col-md-6 mb-20">
									  	<div class="lpay_input_group">
											<label for="">Annual Turnover (Approx) <span class="imp">*</span></label>
											<input type="text" id="annualTurnover"
											class="lpay_input mpa-input"
											placeholder="Total amount in INR"
											onkeypress="onlyDigit(event)" onchange="checkAllField(this)">
									  	</div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-6 mb-20">
									  	<div class="lpay_input_group">
											<label for="">Annual Turnover (online) <span class="imp">*</span></label>
											<input type="text" id="annualTurnoverOnline"
											class="lpay_input mpa-input percentage-count"
											onkeypress="onlyDigit(event)"
											placeholder="Total amount in INR"
											onchange="checkAllField(this)">
									  	</div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-12">
										<div class="border-bottom"></div>
										<span class="msg-turn-over">Please filled value is equal to 100</span>
										<!-- /.border-bottom -->
									</div>
									<!-- /.col-md-12 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Credit Card <span class="imp">*</span></label>
										<input type="text" id="percentageCC"
										class="lpay_input mpa-input percentage-count count-six-input annual-turnover"
										onkeypress="onlyDigit(event)"
										placeholder="Total no of transactions in percentage"
										onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Debit Card <span class="imp">*</span></label>
										<input type="text" id="percentageDC"
										class="lpay_input mpa-input percentage-count count-six-input annual-turnover"
										onkeypress="onlyDigit(event)"
										placeholder="Total no of transactions in percentage"
										onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Net Banking <span class="imp">*</span></label>
										<input type="text" id="percentageNB"
										class="lpay_input mpa-input percentage-count count-six-input annual-turnover"
										onkeypress="onlyDigit(event)" placeholder="in %"
										onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">UPI <span class="imp">*</span></label>
										<input type="text" id="percentageUP"
										class="lpay_input mpa-input percentage-count count-six-input annual-turnover"
										onkeypress="onlyDigit(event)" placeholder="in %"
										onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">Wallets <span class="imp">*</span></label>
										<input type="text" id="percentageWL"
												class="lpay_input mpa-input percentage-count count-six-input annual-turnover"
												onkeypress="onlyDigit(event)"
												placeholder="Total no of transactions in percentage"
												onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">EMI <span class="imp">*</span></label>
										<input type="text" id="percentageEM"
												class="lpay_input mpa-input percentage-count count-six-input annual-turnover"
												onkeypress="onlyDigit(event)"
												placeholder="Total no of transactions in percentage"
												onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">COD/Cash <span class="imp">*</span></label>
										<input type="text" id="percentageCD"
										class="lpay_input mpa-input percentage-count count-six-input"
										onkeypress="onlyDigit(event)"
										placeholder="Total no of transactions in percentage"
										onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-3 mb-20">
									  <div class="lpay_input_group">
										<label for="">NEFT/IMPS/RTGS <span class="imp">*</span></label>
										<input type="text" id="percentageNeftOrImpsOrRtgs"
										class="lpay_input mpa-input percentage-count count-six-input"
										onkeypress="onlyDigit(event)"
										placeholder="Total no of transactions in percentage"
										onchange="checkAllField(this)">
									  </div> 
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->

									<div class="col-md-12">
										<div class="border-bottom"></div>
										<!-- /.border-bottom -->
										<span class="msg-card-turn-over">Please filled value is equal to 100</span>
									</div>
									<!-- /.col-md-12 -->
									<div class="col-md-6 mb-20">
									  <div class="lpay_input_group">
										<label for="">Total Cards Domestic <span class="imp">*</span></label>
										<input type="text" id="percentageDomestic"
												class="lpay_input mpa-input percentage-count count-two-input"
												onkeypress="onlyDigit(event)"
												placeholder="Total no of transactions in percentage"
												onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
									<div class="col-md-6 mb-20">
									  <div class="lpay_input_group">
										<label for="">Total Cards International <span class="imp">*</span></label>
										<input type="text" id="percentageInternational"
												class="lpay_input mpa-input w-70 percentage-count count-two-input"
												onkeypress="onlyDigit(event)"
												placeholder="Total no of transactions in percentage"
												onchange="checkAllField(this)">
									  </div>
									  <!-- /.lpay_input_group -->
									</div>
									<!-- /.col-md-4 -->
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
														<span class="imp">*</span> Refund Policy
												</span> <input type="file"  data-type="RefundPolicy" name="file" class="generic-uploader mpa-input" onchange="checkAllField(this)" />
													<div class="loader-container"></div>
													
												</label>
												<a href="#" data-info="refundPolicy" class="mpaNext termPolicy refundPolicyButton">Policy Sample</a>
											</div>
											<!-- /.upload-policy -->
											<div class="limited" data-hide="limited">
												<div class="limited-inner">
													<div class="main-heading">ID Proof of All the Trustees (Self Attested and with company seal)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Pan Card copy of the Authorized Signatory One
														</span> <input type="file" data-type="panDirectorOne" name="file"
															accept="" class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>

													<!-- <input type="file" data-type="PanCard" name="file" accept=".png, .jpg, .pdf" class="generic-uploader mpa-input" onchange="checkAllField(this)" Passport> -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Pan Card copy of the Authorized Signatory Two
														</span> <input type="file" data-type="panDirectorTwo" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label> 
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Identification Documents of all the Authorized
																Signatories , Aadhaar Card/DL/Voter ID Card
														</span> <input type="file" data-type="KYC" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
												</div>
												<!-- /.limited-inner -->
												<div class="limited-inner mt-30">
													<div class="main-heading">Address Proof (With company
														seal and signed by authorized signatories)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Address
																Proof for the company (Offices Utility Bills or Rental
																Agreement if premises are on rent)</span> <input type="file"
															data-type="companyAddress" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
												</div>
												<!-- /.limited-inner -->
												<div class="limited-inner mt-30">
													<div class="main-heading">Company Proof</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Articles
																of Association (AOA)</span> <input type="file" data-type="AOA"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label> 
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Memorandum
																of Association (MOA)</span> <input type="file" data-type="MOA"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label> 
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Board
																resolution (Sample Attached)</span> <input type="file"
															data-type="BR" name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label> 
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span> List
																of Directors from MCA</span> <input type="file" data-type="MCA"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader">
														</label> 
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Certification
																of Incorporation</span> <input type="file" data-type="CertificationoFIncorporation"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> PAN
																of the Company</span> <input type="file" data-type="panCompany"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> GST
																Registration Certificate</span> <input type="file"
															data-type="GST" name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
												</div>
												<!-- /.limited-inner -->
												<div class="limited-inner mt-30 mb-20">
													<div class="main-heading">Financial Proof</div>
													<!-- /.main-heading -->
													
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Cancelled
																Cheque/Bank Statement </span> <input type="file" data-type="cheque" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
												</div>
												<!-- /.limited-inner -->
											</div>
											<!-- /.limited -->
											<div class="partnership" data-hide="partnership">
												<div class="partnership-inner">
													<div class="main-heading">ID Proof (Self Attested)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> PAN card copies of Partner one with self-attested
														</span> <input type="file" data-type="pan1" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> PAN card copies of Partner two with self-attested
														</span> <input type="file" data-type="pan2" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>

													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Aadhaar Card/Passport/Driving License/Election
																Card of Partner one self-attestation
														</span> <input type="file" data-type="KYC1" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>

													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span>  Aadhaar Card/Passport/Driving License/Election
																Card of Partner two self-attestation
														</span> <input type="file" data-type="KYC2" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>

													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Passport size photographs One
														</span> <input type="file" data-type="photo1" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>

													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Passport size photographs Two
														</span> <input type="file" data-type="photo2" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>

												</div>
												<!-- /.partnership-inner -->
												<div class="partnership-inner mt-30">
													<div class="main-heading">Address Proof (With sign
														and seal)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Address Proof (Utility Bills or Rental Agreement
																if premises is on rent with sign and company seal)
														</span> <input type="file" data-type="addressProof" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
															<div class="loader-container"></div>
														</label>
													</div>
												</div>
												<!-- /.partnership-inner -->
												<div class="partnership-inner mt-30">
													<div class="main-heading">Company Proof for
														Partnership Firm / Limited Liability Partnership (With
														sign and seal)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Company PAN Card with sign & stamp
														</span> <input type="file" data-type="panEntity" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Partnership Deed with signed and stamped by
																authorized partners
														</span> <input type="file" data-type="deed" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
												</div>
												<!-- /.partnership-inner -->
												<div class="partnership-inner mt-30 mb-20">
													<div class="main-heading">Financial Proof</div>
													<!-- /.main-heading -->
													
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Cancelled
																Cheque/Bank Statement </span> <input type="file" data-type="cheque" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>

												</div>
												<!-- /.partnership-inner mt-30 -->
											</div>
											<!-- /.partnership -->
											<div class="proprietorship" data-hide="proprietorship">
												<div class="proprietorship-inner">
													<div class="main-heading">ID Proof (Self Attested
														with company seal)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Pan Card copy of the Proprietor
														</span> <input type="file" data-type="panPropriotery" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label> 
													</div>

													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<b>*</b>Identification Documents of the Proprietor
																-Passport (with the address page)/Aadhaar Card/DL/Voter
																ID Card
														</span> <input type="file" data-type="identificationPropriotery"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>

													
												</div>
												<!-- /.proprietorship-inner -->
												<div class="proprietorship-inner mt-30">
													<div class="main-heading">Address Proof (With sign
														and company seal)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> Address Proof (Utility Bills or Rental Agreement
																if premises are on rent)
														</span> <input type="file" data-type="addressPropriotery"
															name="file" accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>
												</div>
												<!-- /.proprietorship-inner -->
												<div class="proprietorship-inner mt-30">
													<div class="main-heading">Proof for Sole Proprietor
														(With company seal and sign)</div>
													<!-- /.main-heading -->
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>
																<span class="imp">*</span> GST Registration Certificate
														</span> <input type="file" data-type="gstPropriotery" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>

													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span>Shop
																Establishment Certificate / Company Registration
																Certificate/ Trade License etc</span> <input type="file"
															data-type="tradePropriotery" name="file"
															accept=".png, .jpg, .pdf" class="generic-uploader">
														</label>
													</div>

												</div>
												<!-- /.proprietorship-inner -->
												<div class="proprietorship-inner mt-30 mb-20">
													<div class="main-heading">Financial Proof</div>
													<!-- /.main-heading -->
												
													<div>
														<label for="" class="generic-upload"> <img
															src="../image/add-file.png" alt="" width="50"> <span><span class="imp">*</span> Cancelled
																Cheque/Bank Statement </span> <input type="file" data-type="cheque" name="file"
															accept=".png, .jpg, .pdf"
															class="generic-uploader mpa-input"
															onchange="checkAllField(this)">
														</label>
													</div>

													
												</div>
												<!-- /.proprietorship-inner mt-30 -->
											</div>
											<!-- /.proprietorship -->
											
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
											<div data-id="industryCategory"></div>
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
											<div class="mpaSectionData-title" data-label="viewRegistration">
												CIN
											</div>
											<!-- /.mpaSectionData-title -->
											<div data-id="cin"></div>
										</div>
										<!-- /.mpaSectionData-box -->
										<div class="mpaSectionData-box">
											<div class="mpaSectionData-title" data-label="viewRegistration">
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
								class="col-md-12 d-flex justify-content-center mb-20 button-wrapper">
								<button id="btn-prev-stage"
									class="lpay_button lpay_button-md lpay_button-secondary  btn-stage inactive"
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
					</section>
					<!-- /.mpaSection -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	</div>
	<!-- /.container -->



	<!-- term and condition popup -->
	<div class="term-condition-popup" style="z-index: 99999;">
		<div class="term-condition-wrapper">
			<div class="term-condition-box" id="checkTerm">
				<div class="termCondition">
					<h3 class="mb-10">TERMS AND CONDITIONS</h3>
					<p>The most important terms and conditions (MITCs)mentioned herein below are to be read and understood in conjunction with the merchants Processing Application Form as executed by you in favors of  Payment Gateway Solution Private Limited (''Application'') and transactions documents, which is comprised of the Application, The Privacy statements and consent the general terms and the operating Guide  (''Transaction Documents '') for receiving payment processing facilities and services (''services''). In the event of a conflict between any of these MITCs and the Transaction Documents, the transaction Documents Shall prevail.</p>
					<p>Term used in capital letters but not defined herein shall have the same meaning as given to them in the General Terms and /or other Transaction Documents.</p>
					<p><b>1. Transaction Procedures:</b> you must follow all procedures and requirements relating to card transactions set out in the Transaction Documents, including complying with the operating Guide and applicable card Scheme Rules, as amended from time to time </p>
					<p><b>2. Transaction Record:</b> you must provide Payment Gateway Solutions Private limited with your records, and all information and assistance that Payment Gateway Solutions Private limited may reasonably require, relating to any Card transactions when Payment Gateway Solutions Private limited requests them.</p>
					<p><b>3. Compliance of Data Security Standards:</b> you shall comply with provisions contained in payments card industry –Data Security Standards (''PCI-DSS''), PADSS and PCI PED, as published on www.paymentcardindusrty.com. As part of PCI DSS obligations among other things ,you shall not store card authentication information (Track 2 CVV,PIN and PIN Block ) and shall also eliminate /minimize storage of valid card information (Name ,Expiry date )in electronic or paper form and if absolutely necessary store the same in encrypted form, after notifying to  Payment Gateway Solutions Private limited .In addition ,you shall carry out quarterly vulnerability scans as prescribed by PCI Security Standards Council (''PCI SSC'') in Approved Scan vendor scan procedures ,and send scan  reports to Payment Gateway. As per regulations issued by VISA /Master Card /Amex /Discover /JCB , the High Risk Merchants and Merchants carrying on transactions above the limits, decided by PCI SSC will have to get their controls validated through an external audit by a Qualified Security Assessor.</p>
					<p><b>4. Fees:</b> (a) You must pay Payment Gateway Solutions Private limited the fees along with applicable indirect taxes including services tax for the services as set out in the Application ,as well as any additional fees or  pricing set out in the Transaction Documents .Those fees are payable when the services are provide . You agree that the fees for services may be adjusted by Payment Gateway Solutions Private limited upon 30 days’ notice. Fess   and other amounts paid by you to Payment Gateway Solutions Private limited for services provided by Payment Gateway Solutions Private limited, will be paid along with applicable indirect taxes including services tax and this arrangement. Any communication received by you from Payment Gateway Solutions Private limited related to pricing shall be binding on you.</p>
					<p><b>5. Chargebacks and other Liabilities:</b> you must compensate and indemnify us. that if,  Payment Gateway Solutions Private limited for any actions ,claim, costs, loss damages expenses or liability made against or suffered or incurred by us either directly or indirectly arising out of : (i) a card transaction between you and any cardholder ;(ii) all card transactions you submit that are chargeback ; (iii) your failure to produce a clear ,legible and valid card transaction record requested by us with in applicable time limits; (iv)  you or any of your Employees processing a transaction with wrong transaction information; (v) any error ,negligence, willful misconduct or fraud by you or your Employees; (vi) any dispute over goods or services between you and cardholder ; (vii) any warranty or representation whatsoever in relating to any goods or services supplied by you ; (viii) your failure to comply with any of your obligations under the terms of the transaction Documents ; (ix) Any fines or penalties imposed by the card Schemes in connection with your use of the services : (x) any losses suffered by either of us as a results of that one of us indemnifying the other for your failures to meet your obligations under the terms of the Transaction Documents </p>
					<p>Payment Gateway shall have the paramount right of combination and set- off and lien irrespective of any other lien or charge present as well as future on the deposits of any kind and nature (including fixed deposits) held /balances lying in any account that you hold with Payment Gateway towards the satisfaction of your liability under the terms of the transaction documents .Payment Gateway is entitled without any notice to you to settle any indebtedness whatsoever owed by you to it, hereunder or under any other documents relating to the services ,by adjusting ,setting –off any deposit (s) and /or transferring monies lying to the balance to any accounts (s) held by you with any bank</p>
					<p>
						<b>6. Debits and set off:</b> Payment Gateway shall itself as relevant many reserve the rights  to : (a)  debit your settlement Account  and /or  (b) deduct and set off from settlement funds due to you ;and /or  (c) invoice raise a debit note to recover from you separately ,for any amounts  then due from you to us, arising out of or in relation to the terms of the transaction Documents.
					</p>
					<p><b>7. Interest:</b> Pg shall also charge interest at the rate which is 2% above the ''Prime Lending Rate '' as published by the reserve Bank of India or such other charges as notified to you on amounts outstanding to us from you and where there were insufficient funds in your accounts to satisfy the above amounts.</p>
					<p><b>8 Security :</b> Pg Solutions Private limited may from time request security from you or a guarantor to secure performances of your obligations under the transaction Documents .you agree to do all things necessary to put in place enforceable security as requested by Payment Gateway Solutions Private limited  as laid down under the Transaction Documents.</p>
					<p><b>9. Finacial and other information:</b> upon request, you will provide us with such copies of financial accounts and other such documentation or information concerning your business as we request to assist us with our continuing evaluation of your financial and credit status. Further, you must advise us immediately of any changes in circumstances affecting your business including any insolvency Event, Change in control or change in business name, business address, legal status or other business details.</p>
					<p><b>10. your Information :</b> (a) you authorize us to obtain from third parties financial and credit information relating to you .your directors ,officers and principals in connection with our determination whether to accept the Transaction Documents and  our continuing evaluation of the financial and credit worthless of you, your directors officer and principals .( b) we will handle any information we collect about you ,your directors ,officers and principals in accordance with   Privacy Laws ,our privacy collection statements and privacy policies .we will implement all data security measures required by such laws and policies .(c ) you, your directors ,officers and principals acknowledge that information hat is collected  about you, your directors ,officers and principals or held by us may be shared between Payment Gateway Solutions Private limited (and our repective related bodies corporate which may be located inside or outside India ) in connection with the terms of the Transaction Documents and in accordance with our privacy collection statements and privacy policies ,and consent to such sharing of information. (d) you authorize us to share information from your application with our respective related entities ,services providers ,persons under a duty of confidentiality to us ,and also with the third party ,affiliates and  Associations ( which may be located overseas) as relevant to the transaction .( e) you authorize us to share information about you, your directors ,officers and principals with any court ,tribunal ,regulatory ,supervisory ,governmental or quasi- governmental authority which has jurisdiction over us or our related entities (which may be located inside or outside of India ).(f) you irrevocably authorize to discharge and /or release ,codes ,data and information of whatsoever nature which from time to time or at any time you disclose release to us and /or we may have accesses to under or by virtue of your participation in the card schemes ,any transaction contemplated in this transaction documents and /or  in relation to in connection with the transaction documents ;and /or which an card schemes may lawfully require us to provide to provide to it from time to time or at any time </p>
					<p><b>11. Return of Equipment’s:</b> upon termination /expiration of the arrangement pursuant to the transaction documents. you are obligated to return the terminal/supplied equipment’s to Payment Gateway Solutions Private limited. For each item supplied equipment that you fail to returns to Payment Gateway in accordance with the terms of the transaction documents ,you agree that Payment Gateway Solutions Private limited may retrieve the supplied Equipment from you and you authorize Payment Gateway Solution Private limited to access your premises for that purpose </p>
					<p><b>12.No Transaction charges to be levied on cardholder :</b> Pursuant to circular Nos  :CEPD.CO.PRS.NO.3732/13.13.10.001/2017-18 FROM The Reserve bank of India , On the extra charges levied by merchants on customers making payments through debit card; you agree that  you will not levy any transaction charges on debit card transaction done by cardholders at POS terminals. In this regard you also agree that you will display appropriate stickers/signage stating ''no extra charges levied on debit card usage at POS''</p>
				</div>
				<!-- /.termCondition -->
				<label for="termConditionCheck" class="term-condition-input"> <input
					type="checkbox" id="termConditionCheck"> I agree to Pg's <a
					href="#" class="termPolicy" data-info="termCondition" style="margin-left: 2px"> Terms & Conditions</a>
				</label>
				<!-- /.term-condition-input -->
				<label for="privacyPolicyCheck" class="term-condition-input"> <input
					type="checkbox" id="privacyPolicyCheck"> I agree to Pg's  <a
					href="#" class="termPolicy" data-info="privacyPolicy" style="margin-left: 2px"> Privacy Policy</a>
				</label>
				<div class="term-btn text-center">
					<button class="mpaNext" disabled>I UNDERSTAND let's
						proceed</button>
				</div>
				<!-- /.term-btn -->
			</div>
			<!-- /.term-condition-popup -->
		</div>
		<!-- /.term-condition-wrapper -->
	</div>
	<!-- /.term-condition-popup -->

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


	<script
		src="../js/jquery-latest.min.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/jquery.fancybox.min.js"></script>
	<script src="../js/city_state.js"></script>
	<script src="../js/bootstrap.min.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script type="text/javascript" src="../js/processing-merchant.js"></script>
	<form name="downloadTnCPolicy" method="POST" id="downloadTnCPolicy" action="downloadTnCPolicy">
		<input type="hidden" name="docFile" value="" id="docFile" />
	</form>

</body>
</html>