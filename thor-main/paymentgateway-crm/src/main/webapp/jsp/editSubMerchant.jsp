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
	<title>Sub Merchant Account Setup</title>
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
	<link rel="stylesheet" href="../css/submerchant-style.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

	<s:property value="%{MPAData.logoName}"></s:property>
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
	<s:hidden value="%{MPAData.director1FullName}" id="directorAddress"></s:hidden>
	<s:hidden value="%{MPAData.accountNumber}" data-id="accountNumber"></s:hidden>

	<section class="merchant-account lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-4 p-0">
				<span class="heading-ribbon">
					<h4><s:property value="%{MPAData.businessName}"></s:property></h4>
				</span>
			</div>
			
			<div class="col-md-12">
				<div class="horizontal-nav-wrapper mb-20">
					<nav id="horizontal-nav" class="horizontal-nav">
						<ul class="horizontal-nav-content nav nav-tabs list-unstyled font-size-14 merchant-config-tabs border-none mt-0" id="horizontal-nav-content" role="tablist">
							<li class="nav-item merchant__tab_button active-tab" data-target="merchantDetails">Merchant Details</li>
							<li class="nav-item merchant__tab_button" data-target="principleInformation">Principle Information</li>
							<li class="nav-item merchant__tab_button" data-target="bankAccountDetails">Bank Details</li>
							<li class="nav-item merchant__tab_button" data-target="onBoardDetails">Onboard Details</li>
							<!-- <li class="nav-item merchant__tab_button" data-target="configuration">Configuration</li> -->

							<s:if test="%{MPAData.companyName != null && MPAData.companyName != ''}">
								<li class="nav-item merchant__tab_button" data-target="documents">Documents</li>
								<li class="nav-item merchant__tab_button" data-target="preview">Merchant Status</li>
							</s:if>
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

					<button type="button" id="btn-scroll-left" class="horizontal-nav-btn horizontal-nav-btn-left m-0 bg-color-white pr-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
						<path d="M445.44 38.183L-2.53 512l447.97 473.817 85.857-81.173-409.6-433.23v81.172l409.6-433.23L445.44 38.18z" /></svg>
					</button>

					<button type="button" id="btn-scroll-right" class="horizontal-nav-btn horizontal-nav-btn-right m-0 bg-color-white pl-5">
						<svg class="horizontal-nav-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 551 1024">
						<path d="M105.56 985.817L553.53 512 105.56 38.183l-85.857 81.173 409.6 433.23v-81.172l-409.6 433.23 85.856 81.174z" /></svg>
					</button>
				</div>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->

		<div class="merchant__forms m-0">
			<div class="lpay_error-msg d-none">
				<span>Submission of  MPA is Mandatory</span>
			</div>
			<!-- /.lpay_success -->

			<!-- <s:if test="%{responseCode == '101'}">
				<div id="saveMessage">
					<div class="success success-text">
						<s:property value="%{responseMsg}"></s:property>
					</div>					
				</div>
			</s:if>
			<s:else>
				<div class="error-text">
					<s:property value="%{responseMsg}"></s:property>								
				</div>
			</s:else> -->

			<s:form action="editSubMerchDetails" method="post" autocomplete="off" enctype="multipart/form-data" class="FlowupLabels" id="merchantForm" theme="css_xhtml">
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

				<div class="merchant__forms_block active-block" data-active="merchantDetails">
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

						<s:if test="%{MPAData.typeOfEntity == 'Private Limited' || MPAData.typeOfEntity == 'Public Limited' || MPAData.typeOfEntity == 'Partnership Firm' || MPAData.typeOfEntity == 'Proprietory' || MPAData.typeOfEntity == null}">
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
										readonly="true">
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
						<s:if test="%{MPAData.typeOfEntity == 'Private Limited' || MPAData.typeOfEntity == 'Public Limited'}">
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="" id="registerationTitle">CIN</label>
									<s:textfield
										id="cin"
										name="cin"
										type="text"
										maxlength="21"
										value="%{MPAData.cin}"
										class="lpay_input"
										autocomplete="off"
										oninput="alphaNumericAlt(this);">
									</s:textfield>
								</div>
								<!-- /.lpay_input_group -->
							</div>
							<!-- /.col-md-4 mb-20 -->
						</s:if>						
						<s:else>
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<s:if test="%{MPAData.typeOfEntity == 'Proprietory'}">
										<label for="" id="registerationTitle">Shop Establishment Number </label>
									</s:if>
									<s:else>
										<label for="" id="registerationTitle">Registration Number </label>
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
								<s:textfield
									id="tradingAddress1"
									class="lpay_input"
									name="tradingAddress1"
									type="text"
									value="%{MPAData.tradingAddress1}"
									autocomplete="off"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_select_group">
								<label for="">Country</label>
								<select class="" id="country" name="tradingCountry"></select>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_select_group">
								<label for="">State</label>
								<select class="selectpicker" id="state" name="tradingState"></select>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">PIN</label>
								<s:textfield
									id="tradingPin"
									maxlength="6"
									class="lpay_input"
									name="tradingPin"
									type="text"
									value="%{MPAData.tradingPin}"
									autocomplete="off"
									onblur="blurMsg(event)"
									data-reg="^[0-9]{6}$"
									oninput="checkLength(this, 'PIN'); numeric(this);">
								</s:textfield>
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
									oninput="checkLength(this, 'PAN Number'); alphaNumericAlt(this);"
									id="businessPan"
									class="lpay_input"
									maxlength="10"
									name="businessPan"
									type="text"
									value="%{MPAData.businessPan}"
									autocomplete="off">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">GST</label>
								<s:textfield
									id="gstin"
									class="lpay_input"
									name="gstin"
									type="text"
									maxlength="15"
									data-reg="[0-9]{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z\d]{1}"
									onblur="blurMsg(event)"
									oninput="checkLength(this, 'GST Number'); alphaNumericAlt(this);_uppercase(this)"
									value="%{MPAData.gstin}"
									autocomplete="off">
								</s:textfield>
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
								<s:textfield
									id="companyWebsite"
									data-reg="^([A-Za-z'])+\.([A-Za-z0-9'])+\.([A-Za-z]{2,6})$"
									class="lpay_input"
									name="companyWebsite"
									type="text"
									onblur="blurMsg(event)" 
									oninput="checkLength(this, 'Website');"
									value="%{MPAData.companyWebsite}"
									autocomplete="off">
								</s:textfield>
								<span class="error-msg"></span>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for=""> Email for Communication </label>
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
						</s:if>
					</div>
					<!-- /.row -->
				</div>
				<!-- /.merchant__forms_block -->

				<div class="merchant__forms_block" data-active="principleInformation">
					<div class="row">
						<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Contact Person</label>
									<s:textfield
										type="text"
										data-target="proprietory"
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
										onblur="blurMsg(event)" 
										data-reg="^[0-9]{10}$" 
										oninput="checkLength(this, 'Mobile Number'); numeric(this);"
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
									<s:textfield
										type="text"
										data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
										onblur="blurMsg(event)"
										oninput="checkLength(this, 'Email ID');"
										data-target="proprietory"
										id="contactEmail"
										value="%{MPAData.contactEmail}"
										name="contactEmail"
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
						</s:if>
						<s:if test="%{MPAData.typeOfEntity != 'Proprietory'}">
							<div class="col-md-12">
								<div class="inner-heading mb-20">
									<s:if test="%{MPAData.typeOfEntity == 'Private Limited'} || %{MPAData.typeOfEntity == 'Public Limited'}">
										<h3 data-heading="entity">First Direcotor Detail</h3>
									</s:if>
									<s:else>
										<h3 data-heading="entity">First Partner Detail</h3>
									</s:else>
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
										oninput="checkLength(this, 'PAN Number'); alphaNumericAlt(this); _uppercase(this);"
										maxlength="10"
										id="director1Pan"
										data-id="director1Pan"
										value="%{MPAData.director1Pan}"
										name="director1Pan"
										class="lpay_input pan-validation-director">
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
							<div class="col-md-12">
								<div class="inner-heading mb-20">
									<s:if test="%{MPAData.typeOfEntity == 'Private Limited'} || %{MPAData.typeOfEntity == 'Public Limited'}">
										<h3 data-heading="entity">Second Director Detail</h3>
									</s:if>
									<s:else>
										<h3 data-heading="entity">Second Partner Detail</h3>
									</s:else>
								</div>
								<!-- /.inner-heading -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-4 mb-20">
								<div class="lpay_input_group">
									<label for="">Name</label>
									<s:textfield
										type="text"
										data-target="proprietory"
										onkeypress="return lettersSpaceOnly(event, this);"
										id="director2FullName"
										value="%{MPAData.director2FullName}"
										name="director2FullName"
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
										oninput="checkLength(this, 'PAN Number'); alphaNumericAlt(this); _uppercase(this);"
										maxlength="10"
										id="director2Pan"
										data-id="director2Pan"
										value="%{MPAData.director2Pan}"
										name="director2Pan"
										class="lpay_input pan-validation-director">
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
										data-target="proprietory"
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
										type="text"
										data-target="proprietory"
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
									<s:textfield
										type="text"
										data-target="proprietory"
										id="director2Address"
										value="%{MPAData.director2Address}"
										name="director2Address"
										class="lpay_input">
									</s:textfield>
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
									<label for="">Email</label>
									<s:textfield
										type="text"
										data-reg="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
										onblur="blurMsg(event);"
										oninput="checkLength(this, 'Email ID');"
										id="director1Email"
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
										maxlength="10"
										onblur="blurMsg(event)" 
										data-reg="^[0-9]{10}$" 
										oninput="checkLength(this, 'Mobile Number');numeric(this)"
										id="director1Mobile"
										value="%{MPAData.director1Mobile}"
										name="director1Mobile"
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
								<s:textfield
									type="text"
									id="accountName"
									onkeypress="return lettersSpaceOnly(event, this);"
									value="%{MPAData.accountHolderName}"
									name="accountHolderName"
									class="lpay_input">
								</s:textfield>
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
									oninput="_uppercase(this); alphaNumericAlt(this);"
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
									onblur="blurMsg(event)"
									data-reg="^[0-9]{10}$"
									oninput="checkLength(this, 'Mobile Number'); numeric(this)"
									value="%{MPAData.accountMobileNumber}"
									name="accountMobileNumber"
									class="lpay_input">
								</s:textfield>
								<span class="error-msg"></span>
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
								<s:textfield
									id="payId"
									readonly="true"
									class="lpay_input"
									name="payId"
									type="text"
									value="%{MPAData.payId}">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Salt</label>
								<s:textfield
									id="salt"
									readonly="true"
									class="lpay_input"
									name="salt"
									type="text"
									value="%{salt}">
								</s:textfield>
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

						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Super Merchant ID</label>
								<s:textfield
									id="superMerchantId"
									readonly="true"
									class="lpay_input"
									name="superMerchantId"
									type="text"
									value="%{MPAData.superMerchantId}">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Request URL</label>
								<s:textfield
									id="requestUrl"
									class="lpay_input"
									name="requestUrl"
									type="text"
									value="%{requestUrl}">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Registration Date</label>
								<s:textfield
									id="registrationDate"
									readonly="true"
									class="lpay_input"
									name="registrationDate"
									type="text"
									value="%{MPAData.registrationDate}">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Activation Date</label>
								<s:textfield
									id="activationDate"
									readonly="true"
									class="lpay_input"
									name="activationDate"
									type="text"
									value="%{MPAData.activationDate}">
								</s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_select_group">
								<label for="">Status</label>
								<s:select
									class="selectpicker"
									headerValue="ALL"
									list="@com.paymentgateway.commons.util.UserStatusType@values()"
									id="status"
									name="userStatus"
									value="%{MPAData.userStatus}"
								/>
							</div>
							<!-- /.lpay_select_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Payment Cycle (In days)</label>
								<s:textfield
									class="lpay_input"
									onkeypress="javascript:return isNumber (event)" id="paymentTurn" maxlength="3" name="paymentCycle"
									value="%{MPAData.paymentCycle}">
								</s:textfield>
							</div>
							<!-- /.lpay_select_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.merchant__forms_block -->

				<div class="merchant__forms_block" data-active="configuration">
					<div class="row">
						<div class="col-md-6">
							<label for="eposMerchant" class="checkbox-label unchecked mb-10 label-config">								
								<s:checkbox
									id="eposMerchant"
									name="eposMerchant1"
									value="%{MPAData.eposMerchant}"
									data-id="eposMerchant"
								/>
								<s:hidden name="eposMerchant" value="%{MPAData.eposMerchant}" />
								ePOS Merchant 
							</label>
						</div>
						
						<div class="col-md-6">
							<label for="bookingRecord" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="bookingRecord"
									name="bookingRecord1"
									value="%{MPAData.bookingRecord}"
									data-id="bookingRecord"
								/>
								<s:hidden name="bookingRecord" value="%{MPAData.bookingRecord}" />
								Booking Report 
							</label>
						</div>
						
						<div class="col-md-6 mt-5">
							<label for="capturedMerchantFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="capturedMerchantFlag"
									name="capturedMerchantFlag1"
									value="%{MPAData.capturedMerchantFlag}"
									data-id="capturedMerchantFlag"
								/>
								<s:hidden name="capturedMerchantFlag" value="%{MPAData.capturedMerchantFlag}" />
								Custom Capture Report
							</label>
						</div>
						
						<div class="col-md-6 mt-5">
							<label for="logoFlag" class="checkbox-label unchecked mb-10 label-config">								
								<s:checkbox										
									id="logoFlag"
									name="logoFlag1"
									data-id="selfLogo"
									value="%{MPAData.logoFlag}"
								/>								
								<s:hidden name="logoFlag" value="%{MPAData.logoFlag}" />
								Sub Merchant Logo
							</label>
						</div>
	
						<div class="col-md-6 mt-5">
							<label for="retailMerchantFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="retailMerchantFlag"
									name="retailMerchantFlag1"
									value="%{MPAData.retailMerchantFlag}"
									data-id="retailMerchantFlag"
								/>
								<s:hidden name="retailMerchantFlag" value="%{MPAData.retailMerchantFlag}" />
								Retail Sub Merchant
							</label>
						</div>
	
						<!-- <div class="col-md-6 mt-5">
							<label for="accountVerificationFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="accountVerificationFlag"
									name="accountVerificationFlag1"
									value="%{MPAData.accountVerificationFlag}"
									data-id="accountVerificationFlag"
								/>
								<s:hidden name="accountVerificationFlag" value="%{MPAData.accountVerificationFlag}" />
								Account Verification
							</label>
						</div> -->
						
						<div class="col-md-6 mt-5">
							<label for="loadWalletFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="loadWalletFlag"
									name="loadWalletFlag1"
									value="%{MPAData.loadWalletFlag}"
									data-id="loadWalletFlag"
								/>
								<s:hidden name="loadWalletFlag" value="%{MPAData.loadWalletFlag}" />
								Load Wallet
							</label>
						</div>
						
						<div class="col-md-6 mt-5">
							<label for="checkOutJsFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="checkOutJsFlag"
									name="checkOutJsFlag1"
									value="%{MPAData.checkOutJsFlag}"
									data-id="checkOutJsFlag"
								/>
								<s:hidden name="checkOutJsFlag" value="%{MPAData.checkOutJsFlag}" />
								Checkout JS
							</label>
						</div>
						<s:if test="%{eNachFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="eNachReportFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="eNachReportFlag"
									name="eNachReportFlag1"
									value="%{MPAData.eNachReportFlag}"
									data-id="eNachReportFlag"
								/>
								<s:hidden name="eNachReportFlag" value="%{MPAData.eNachReportFlag}" />
								eNach Report
							</label>
						</div>
						</s:if>
						<s:hidden value="%{eCollectionFeeFlag}" id='testId' />
						
						<s:if test="%{eCollectionFeeFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="allowECollectionFee" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="allowECollectionFee"
									name="allowECollectionFee"
									value="%{MPAData.allowECollectionFee}"
									data-id="allowECollectionFee"
								/>
								<s:hidden name="allowECollectionFee" value="%{MPAData.allowECollectionFee}" />
								E-Collection Fee
							</label>
						</div>
						</s:if>
						
						<s:if test="%{upiAutoPayFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="upiAutoPayReportFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="upiAutoPayReportFlag"
									name="upiAutoPayReportFlag1"
									value="%{MPAData.upiAutoPayReportFlag}"
									data-id="upiAutoPayReportFlag"
								/>
								<s:hidden name="upiAutoPayReportFlag" value="%{MPAData.upiAutoPayReportFlag}" />
								upi AutoPay Report
							</label>
						</div>
						</s:if>
						
						<s:if test="%{customStatusEnquiryFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="acceptPostSettledInEnquiry" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="acceptPostSettledInEnquiry"
									name="acceptPostSettledInEnquiry"
									value="%{MPAData.acceptPostSettledInEnquiry}"
									data-id="acceptPostSettledInEnquiry"
								/>
								<s:hidden name="acceptPostSettledInEnquiry" value="%{MPAData.acceptPostSettledInEnquiry}" />
								Custom Status Enquiry Flag
							</label>
						</div>
						</s:if>
					</div>

					<div class="row d-flex align-items-center">
						<div class="col-md-12 d-none upload-custom-logo">
							<label class="lable-default" id="title-upload-logo">Upload Logo</label>
							<div class="merchant__form_group d-flex align-items-center">								
								<label for="uploadMerchantLogo" class="upload-pic lable-default d-flex align-items-center mr-50" id="label-upload-logo">
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
							<!-- /.merchant__form_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</div>

					<div class="row mt-30">

						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Callback Settings</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
	                        <label for="allCallBackFlag" class="checkbox-label unchecked">
	                            All Payemnt Type Callback Flag <s:checkbox name="allCallBackFlag"
	                                id="allCallBackFlag" value="%{MPAData.allCallBackFlag}" />
	                        </label>
                   		 </div>
						<div class="col-md-3 mb-20">
							<label for="callBackFlag" class="checkbox-label unchecked">
								Status Enquiry Callback Flag <s:checkbox name="callBackFlag"
									id="callBackFlag" value="%{MPAData.callBackFlag}" />
							</label>
						</div>
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Callback URL</label>
								<s:textfield id="callBackUrl" class="lpay_input"
								name="callBackUrl" type="text" value="%{MPAData.callBackUrl}" autocomplete="off"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->

						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Settlement Flag</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->

					
	 
						<div class="col-md-3 mb-20">
							<label for="merchantInitiatedDirectFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="merchantInitiatedDirectFlag"
									name="merchantInitiatedDirectFlag1"
									value="%{MPAData.merchantInitiatedDirectFlag}"
									data-id="merchantInitiatedDirectFlag"
									class="impsFlag"
								/>
								<s:hidden name="merchantInitiatedDirectFlag" value="%{MPAData.merchantInitiatedDirectFlag}" />
								Payout
							</label>
						</div>
	 
						

						<div class="col-md-3 mb-20">
							<label for="nodalReportFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="nodalReportFlag"
									name="nodalReportFlag1"
									value="%{MPAData.nodalReportFlag}"
									data-id="nodalReportFlag"
								/>
								<s:hidden name="nodalReportFlag" value="%{MPAData.nodalReportFlag}" />
								CIB Report
							</label>
						</div>
						
						<div class="col-md-3 mb-20">
							<label for="virtualAccountFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="virtualAccountFlag"
									name="virtualAccountFlag1"
									value="%{MPAData.virtualAccountFlag}"
									data-id="virtualAccountFlag"
								/>
								<s:hidden name="virtualAccountFlag" value="%{MPAData.virtualAccountFlag}" />
								Virtual Account
							</label>
						</div>
						
						<div class="col-md-3 mb-20">
							<label for="netSettledFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="netSettledFlag"
									name="netSettledFlag1"
									value="%{MPAData.netSettledFlag}"
									data-id="netSettledFlag"
								/>
								<s:hidden name="netSettledFlag" value="%{MPAData.netSettledFlag}" />
								Net Settled Report
							</label>
						</div>
					</div>

					<s:if test="%{#session.USER.UserType.name()=='MERCHANT'}">
					<s:if
					test="%{#session['USER'].vpaVerificationFlag == true || #session['USER'].accountVerificationFlag == true}">

					<div class="row mt-3">
						<div class="col-md-12 mb-20">
                        <div class="inner-heading">
                            <h3>Beneficiary Verification Flags</h3>
                        </div>
                        <!-- /.inner-heading -->
                    </div>
					<s:if
						test="%{#session['USER'].accountVerificationFlag == true}">
					<div class="col-md-3 mb-20">
                        <label for="accountVerificationFlag" class="checkbox-label unchecked">
                            Account Verification <s:checkbox name="accountVerificationFlag"
                                id="accountVerificationFlag" value="%{MPAData.accountVerificationFlag}" />
                        </label>
                    </div>
					</s:if>
                    <!-- /.col-md-12 -->
					<s:if
						test="%{#session['USER'].vpaVerificationFlag == true}">
                    <div class="col-md-3 mb-20">
                        <label for="vpaVerificationFlag" class="checkbox-label unchecked">
                            VPA Verification
							<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" data-id="vpaVerificationFlag" value="%{MPAData.accountVerificationFlag}" />
                        </label>
                    </div>
					</s:if>
					</div>
					</s:if>
					</s:if>
					<s:else>
						<div class="row mt-3">
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
								<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" data-id="vpaVerificationFlag" value="%{MPAData.accountVerificationFlag}" />
							</label>
						</div>
						</div>

					</s:else>
					
					<div class="row mt-3">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>QR Code</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->

						<div class="col-md-3 mb-20">
							<div class="d-inline-flex flex-column align-items-center">
								<a href="#" class="downloadQrCode" data-download="PGQR">
									<img src="../images/demo-qr-code.png" alt="" height="100">
								</a>
								<a href="#" class="downloadQrCode text-secondary font-weight-medium mt-5" data-download="PGQR">Download PG QR</a>
							</div>

							<label for="allowQRScanFlag" class="checkbox-label unchecked label-config mt-10">
								<s:checkbox
									id="allowQRScanFlag"
									name="allowQRScanFlag1"
									value="%{MPAData.allowQRScanFlag}"
									data-id="allowQRScanFlag"
								/>
								<s:hidden name="allowQRScanFlag" value="%{MPAData.allowQRScanFlag}" />
								Allow PG QR
							</label>
						</div>
	
						<div class="col-md-3 mb-20">
							<div class="d-inline-flex flex-column align-items-center">
								<a href="#" class="downloadQrCode" data-download="UPIQR">
									<img src="../images/demo-qr-code.png" alt="" height="100">
								</a>
								<a href="#" class="downloadQrCode text-secondary font-weight-medium mt-5" data-download="UPIQR">Download UPI QR</a>
							</div>

							<label for="allowUpiQRFlag" class="checkbox-label unchecked label-config mt-10">
								<s:checkbox
									id="allowUpiQRFlag"
									name="allowUpiQRFlag1"
									value="%{MPAData.allowUpiQRFlag}"
									data-id="allowUpiQRFlag"
								/>
								<s:hidden name="allowUpiQRFlag" value="%{MPAData.allowUpiQRFlag}" />
								Allow UPI QR
							</label>
						</div>
						
						<div class="col-md-3 mb-20">
						<label for="customerQrFlag" class="checkbox-label unchecked">
							Static UPI QR Report<s:checkbox name="customerQrFlag"
								id="customerQrFlag" value="%{MPAData.customerQrFlag}" />
						</label>
					</div>
					
					</div>
				</div>

				<div class="row submit-row submit-shift">
					<div class="col-md-12 text-center">
						<s:a class="lpay_button lpay_button-md lpay_button-primary" action='subMerchantSearch'>Back</s:a>
						<button id="btnSave" class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</s:form>

			<div class="merchant__forms_block" data-active="preview">
				<div class="merchantAssing-status">
					<div class="merchnatStatus-head">
						<span>Status</span>
						<span>Comment</span>
						<span>Uploaded Documents</span>
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
								<span class="lpay_button lpay-button-md lpay_button-secondary downloadMpa" style="display: inline-block" data-type="CHECKER_FILE">Download</span>
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

			<div class="merchant__forms_block" data-active="documents">
				<div class="row">
					<div class="col-md-4">
						<div class="lpay_select_group">
							<label for="">Select File Type</label>

							<!-- <select name="mpaFileType" class="selectpicker" id="mpaFiletype"></select> -->

							<s:if test="%{MPAData.typeOfEntity == 'Private Limited' || MPAData.typeOfEntity == 'Public Limited'}">
								<select name="mpaFileType" class="selectpicker" id="mpaFiletype">
									<option value="">Select Document</option>
									<option value="panDirectorOne">PAN of authorized signatory 1</option>
									<option value="panDirectorTwo">PAN of authorized signatory 2</option>
									<option value="GST">GST Registration Certificate</option>
									<option value="PanCard">Certification of Incorporation</option>
									<option value="panCompany">PAN Card of Company</option>
									<option value="cheque">Cancelled Cheque</option>
								</select>
							</s:if>
							<s:elseif test="%{MPAData.typeOfEntity == 'Partnership Firm' || MPAData.typeOfEntity == 'Other'}">
								<select name="mpaFileType" class="selectpicker" id="mpaFiletype">
									<option value="">Select Document</option>
									<option value="pan1">PAN card of Partner 1</option>
									<option value="pan2">PAN card of Partner 2</option>
									<option value="panEntity">PAN of firm</option>
									<option value="GST">Registration of Certificate</option>
									<option value="cheque">Cancelled Cheque</option>
									<option value="deed">Partnership Deed</option>
								</select>
							</s:elseif>
							<s:elseif test="%{MPAData.typeOfEntity == 'Proprietory'}">
								<select name="mpaFileType" class="selectpicker" id="mpaFiletype">
									<option value="">Select Document</option>
									<option value="panPropriotery">PAN of Proprietor</option>
									<option value="gstPropriotery">GST Registration Certification</option>
									<option value="cheque">Cancelled Cheque</option>
									<option value="tradePropriotery">Shop Establishment Certificate / Udyog aadhar</option>
								</select>
							</s:elseif>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->

					<div class="col-md-4">
						<form action="" method="post" enctype="multipart/form-data" class="upload-generic">						
							<div class="upload_file-wrapper">
								<label class="lable-default">Upload File</label>

								<div for="uploadFileInput" data-response="default" class="upload_file-label d-flex align-items-center">
									<img src="../image/cloud-computing.png" alt="/">
									<input type="file" name="file" disabled data-type="checker" id="uploadFileInput" class=""> 
									<s:hidden name="payId" value="%{MPAData.payId}" id="uploadPayid" />
									
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
		</div>
		<!-- /.merchant__forms mt-30 -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:hidden name="token" id="customToken" value="%{#session.customToken}" />

	<s:form method="POST" id="checkerFileDownload" action="statusFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="checkerFileName" value="%{MPAData.checkerFileName}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
	</s:form>

	<s:form method="post" id="mpaFileDownload" action="mpaMerchantFileDownloadAction">
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="payId" value="%{MPAData.payId}" />
		<s:hidden name="fileNameType" id="fileType" />
	</s:form>

	<s:form method="POST" action="qrImageDowloadAction" id="qrCodeDownloadForm">
		<s:hidden name="payId" value="%{MPAData.payId}" />
		<s:hidden name="qrType" id="qrType" />
	</s:form>

	<script src="../js/main.js"></script>
	<script src="../js/merchantAccountSetup.js"></script>
	<script src="../js/submerchant-script.js"></script>
	<script src="../js/horizontal-scrolling-nav.js"></script>

	<script>
		$(document).ready(function() {
			$("body").on("click", ".downloadQrCode", function(e) {
				e.preventDefault();

				var qrType = $(this).attr("data-download");
				$("#qrType").val(qrType);
				$("#qrCodeDownloadForm").submit();
			});
		});
	</script>
</body>
</html>