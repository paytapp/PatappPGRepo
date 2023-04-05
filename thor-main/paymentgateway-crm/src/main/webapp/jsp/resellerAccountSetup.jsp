<%@page import="com.paymentgateway.commons.util.SaltFactory"%>
<%@page import="com.paymentgateway.commons.user.User"%>


<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Reseller Account Details</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<!-- <link href="../css/custom.css" rel="stylesheet" type="text/css" /> -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/continents.js" type="text/javascript"></script>
<script src="../js/jquery.js"></script>
<script src="../js/follw.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<style>
.textFL_merch5:hover {
	color: #000 !important;
	background: #fff !important;
}
.button-wrapper{
	position: absolute;
	width: auto;
	display: inline-block;
	top: 9px;
	right: 0;
}

.upload-text .doc-file-name {
	display: block;
	margin-top: 4px;
}

.button_wrapper a:hover{
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

.uploaded-logo img {
	height: 50px;
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

</style>
</head>
<body>

	<s:hidden id="resellerPermission" value="%{permissionString}"></s:hidden>
	<s:hidden value="%{merchantLogo}" data-id="merchantLogo"></s:hidden>

	<section
		class="reseller-setup lapy_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o"
					aria-hidden="true"></i></span>
				<h2 class="heading_text">Reseller Account Setup</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-12 mb-20">
			<ul class="lpay_tabs d-flex">
				<li class="lpay-nav-item active"><a href="#"
					class="lpay-nav-link" data-id="action">Action</a></li>
				<li class="lpay-nav-item"><a href="#" class="lpay-nav-link"
					data-id="resellerDetails">Reseller Details</a></li>
				<li class="lpay-nav-item"><a href="#" class="lpay-nav-link"
					data-id="contactDetails">Contact Details</a></li>
				<li class="lpay-nav-item"><a href="#" class="lpay-nav-link"
					data-id="bankDetails">Bank Details</a></li>
				<li class="lpay-nav-item"><a href="#" class="lpay-nav-link"
					data-id="businessDetails">Business Details</a></li>
			</ul>
			<!-- /.lpay_tabs -->
		</div>
		<!-- /.col-md-12 -->
		<s:form action="resellerSaveAction" method="post" autocomplete="off"
			class="FlowupLabels" enctype="multipart/form-data" theme="css_xhtml">
			<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			<div class="col-md-12">
				<div id="saveMessage">
					<s:actionmessage class="success success-text mb-20" />
				</div>
				<div class="lpay_tab-content" data-target="action">
					<div class="row">
						<div class="col-md-3 mb-20">
							<div class="lpay_select_group">
								<label for="">Status</label>
								<s:select class="selectpicker" headerValue="ALL"
									list="@com.paymentgateway.commons.util.UserStatusType@values()"
									id="status" name="userStatus" value="%{user.userStatus}" />
							</div>
							<!-- /.lpay_select_group -->
						</div>
						<!-- /.col-md-3 -->
						<div class="col-md-9 mb-20">
							<div class="lpay_input_group">
								<label for="">Comments</label>
								<s:textarea id="comments" class="lpay_input" rows="5"
									name="comments" type="text" value="%{user.comments}"
									autocomplete="off" theme="simple"></s:textarea>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-9 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.lpay_tab-content -->
				<div class="lpay_tab-content d-none" data-target="resellerDetails">
					<div class="row">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Pay ID</label>
								<s:textfield id="payId" class="lpay_input" name="payId"
									type="text" value="%{user.payId}" readonly="true"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Reseller ID</label>
								<s:textfield id="resellerId" class="lpay_input"
									name="resellerId" type="text" value="%{user.resellerId}"
									readonly="true"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">First Name</label>
								<s:textfield class="lpay_input" id="firstName" name="firstName"
									type="text" value="%{user.firstName}" autocomplete="off"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Last Name</label>
								<s:textfield class="lpay_input" id="lastName" name="lastName"
									type="text" value="%{user.lastName}" autocomplete="off"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Company Name</label>
								<s:textfield class="lpay_input" id="companyName"
									name="companyName" type="text" value="%{user.companyName}"
									autocomplete="off"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Business Type</label>
								<s:textfield class="lpay_input" id="businessType"
									name="businessType" type="text" value="%{user.businessType}"
									autocomplete="off" onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Email ID</label>
								<s:textfield class="lpay_input" id="emailId" name="emailId"
									type="text" value="%{user.emailId}" readonly="true"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Registration Date</label>
								<s:textfield class="lpay_input" id="registrationDate"
									name="registrationDate" type="text"
									value="%{user.registrationDate}" readonly="true"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Activation Date</label>
								<s:textfield class="lpay_input" id="activationDate"
									name="activationDate" type="text"
									value="%{user.activationDate}" readonly="true"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Salt</label>
								<s:textfield class="lpay_input" type="text" value="%{salt}"
									readonly="true"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Request URL</label>
								<div class="returnUrl d-none">
									<%=new PropertiesManager().getSystemProperty("RequestURL")%>
								</div>
								<!-- /.returnUrl -->
								<input type="text" id="returnUrl" class="lpay_input">
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<s:set var="bool_val" value= "user.partnerFlag" />
						<s:if test="%{#bool_val == false}">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>ePOS Flag</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">							
							<label for="eposMerchant" class="checkbox-label unchecked mb-10">
								ePOS Merchant Flag
								<s:checkbox name="eposMerchant" id="eposMerchant" class="textFL_merch5" value="%{user.eposMerchant}"></s:checkbox>
							</label>
						</div>
						<!-- /.col-md-4 -->
						</s:if>
						
						
						<!-- /.col-md-4 -->
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Subscription</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">							
							<label for="eNachReportFlag" class="checkbox-label unchecked mb-10">
								eNach Report Flag
								<s:checkbox name="eNachReportFlag" id="eNachReportFlag" class="textFL_merch5" value="%{user.eNachReportFlag}"></s:checkbox>
							</label>
						</div>
						<!-- /.col-md-4 -->
						
					<<s:if test="%{user.partnerFlag == false}">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Beneficiary Verification Flags</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<div class="col-md-3 mb-20">
							<label for="accountVerificationFlag" class="checkbox-label unchecked">
								Account Verification <s:checkbox name="accountVerificationFlag"
									id="accountVerificationFlag" value="%{user.accountVerificationFlag}" />
							</label>
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
							<label for="vpaVerificationFlag" class="checkbox-label unchecked">
								VPA Verification
								<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" 
									data-id="vpaVerificationFlag" value="%{user.vpaVerificationFlag}" />
							</label>
						</div>
						</s:if>
						
						<div class="col-md-12 mb-20">
                            <div class="inner-heading">
                                <h3>Merchant Flags</h3>
                            </div>
                            <!-- /.inner-heading -->
                        </div>
                        <div class="col-md-3 mb-20">
                            <label for="resellerMerchantSignupFlag" class="checkbox-label unchecked">
                                Merchant Signup <s:checkbox name="resellerMerchantSignupFlag"
                                    id="resellerMerchantSignupFlag" value="%{user.resellerMerchantSignupFlag}" />
                            </label>
                        </div>
                        <!-- /.col-md-12 -->
                        
                        <div class="col-md-3 mb-20">
                            <label for="resellerUserStatusFlag" class="checkbox-label unchecked">
                                User Status
                                <s:checkbox name="resellerUserStatusFlag" id="resellerUserStatusFlag" 
                                    data-id="resellerUserStatusFlag" value="%{user.resellerUserStatusFlag}" />
                            </label>
                        </div>
						<!-- /.col-md-4 -->
						<s:if test="%{user.partnerFlag == false}">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Payout Flags</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						
						<!-- /.col-md-12 -->
						<div class="col-md-4 mb-20">							
							<label for="merchantInitiatedDirectFlag" class="checkbox-label unchecked mb-10">
								Payout
								<s:checkbox name="merchantInitiatedDirectFlag" id="merchantInitiatedDirectFlag" class="textFL_merch5" value="%{user.merchantInitiatedDirectFlag}"></s:checkbox>
							</label>
						</div>
						</s:if>
						<!-- /.col-md-4 -->

						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Customize Logo</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->

						<div class="col-md-3 mb-20">
							<label for="logoFlag" class="checkbox-label unchecked">
								Reseller Logo
								<s:if test="%{user.logoFlag == true}">
									<s:checkbox
										name="logoFlag"
										checked="true"
										id="logoFlag"
										value="%{user.logoFlag}"
									/>
								</s:if>
								<s:else>
									<s:checkbox
										name="logoFlag"
										id="logoFlag"
										data-id="selfLogo"
										value="%{user.logoFlag}"
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
						
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Reporting Flags</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<%-- <div class="col-md-4 mb-20">							
							<label for="customerQrFlag" class="checkbox-label unchecked mb-10">
								Static UPI QR Report
								<s:checkbox name="customerQrFlag" id="customerQrFlag" class="textFL_merch5" value="%{user.customerQrFlag}"></s:checkbox>
							</label>
						</div> --%>
						<!-- /.col-md-4 -->
						<s:iterator value="listPermissionType" status="itStatus">
							<s:if test="%{permission == 'Sale Capture'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:if>
							<s:elseif test="%{permission == 'Refund Capture'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Settled'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Payment Advice'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Custom Capture Report'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Booking Report'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'eCollection Transaction'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Virtual Account List'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Static UPI QR Report'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							<s:elseif test="%{permission == 'Quick Search'}">
								<div class="col-md-3 list-permission" id="transactionPermission">
									<label for="%{permission}" class="checkbox-label unchecked mb-10">
										<s:checkbox name="lstPermissionType" id="%{permission}"
											fieldValue="%{permission}" value="false">
										</s:checkbox>
										<s:property value="permission" />
									</label>
								</div>
								<!-- /.col-md-3 -->
							</s:elseif>
							
							<!-- /.col-md-4 -->
						</s:iterator>
						
					</div>
					<!-- /.row -->
				</div>
				<!-- /.lpay_tab-content -->
				
				<div class="lpay_tab-content d-none" data-target="contactDetails">
					<div class="row">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Mobile</label>
								<s:textfield class="lpay_input" id="mobile" name="mobile"
									type="text" value="%{user.mobile}" autocomplete="off"
									readonly="true" onkeypress="javascript:return isNumber (event)"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Telephone No</label>
								<s:textfield class="lpay_input" id="telephoneNo"
									name="telephoneNo" type="text" value="%{user.telephoneNo}"
									autocomplete="off"
									onkeypress="javascript:return isNumber (event)"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">City</label>
								<s:textfield class="lpay_input" id="city" name="city"
									type="text" value="%{user.city}"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Country</label>
								<s:textfield class="lpay_input" id="country" name="country"
									type="text" value="%{user.country}"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Address</label>
								<s:textfield class="lpay_input" id="address" name="address"
									type="text" value="%{user.address}"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">State</label>
								<s:textfield class="lpay_input" id="state" name="state"
									type="text" value="%{user.state}"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Postal Code</label>
								<s:textfield class="lpay_input" id="postalCode"
									name="postalCode" type="text" value="%{user.postalCode}"
									autocomplete="off"
									onkeypress="return ValidateMerchantAccountSetup(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.lpay_tab-content -->
				<div class="lpay_tab-content d-none" data-target="bankDetails">
					<div class="row">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Bank Name</label>
								<s:textfield class="lpay_input" type="text" id="bankName"
									name="bankName" value="%{user.bankName}"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">IFSC Code</label>
								<s:textfield class="lpay_input" type="text" id="ifscCode"
									name="ifscCode" value="%{user.ifscCode}" autocomplete="off"
									onkeypress="return ValidateMerchantAccountSetup(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">AC. Holder Name</label>
								<s:textfield class="lpay_input" ttype="text" id="accHolderName"
									name="accHolderName" value="%{user.accHolderName}"
									onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_select_group">
								<label for="">Currency</label>
								<s:select name="defaultCurrency" id="defaultCurrency"
									list="currencyMap" class="selectpicker" />
							</div>
							<!-- /.lpay_select_group -->
						</div>
						<!-- /.col-md-3 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Branch Name</label>
								<s:textfield class="lpay_input" ttype="text" id="branchName"
									name="branchName" value="%{user.branchName}" autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">PAN Card</label>
								<s:textfield class="lpay_input" type="text" id="panCard"
									name="panCard" value="%{user.panCard}" autocomplete="off"
									onkeypress="return ValidateMerchantAccountSetup(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Account No</label>
								<s:textfield class="lpay_input" type="text" id="accountNo"
									name="accountNo" value="%{user.accountNo}" autocomplete="off"
									onkeypress="javascript:return isNumber (event)"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.lpay_tab-content -->

				<div class="lpay_tab-content d-none" data-target="businessDetails">

					<div class="row">
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Organisation Type</label>
								<s:textfield class="lpay_input" type="text"
									id="organisationType" name="organisationType"
									value="%{user.organisationType}" autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Multicurrency Payments Required?</label>
								<s:textfield class="lpay_input" type="text" id="multiCurrency"
									name="multiCurrency" value="%{user.multiCurrency}"
									autocomplete="off" onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Operation Address</label>
								<s:textfield class="lpay_input" ttype="text"
									id="operationAddress" name="operationAddress"
									value="%{user.operationAddress}" autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Operation Address State</label>
								<s:textfield class="lpay_input" type="text" id="operationState"
									name="operationState" value="%{user.operationState}"
									autocomplete="off" onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Operation Address City</label>
								<s:textfield class="lpay_input" type="text" id="operationCity"
									name="operationCity" value="%{user.operationCity}"
									autocomplete="off" onKeyPress="return ValidateAlpha(event);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Operation Address Pincode</label>
								<s:textfield class="lpay_input" type="text"
									id="operationPostalCode" name="operationPostalCode"
									value="%{user.operationPostalCode}"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Date Of Establishment</label>
								<s:textfield class="lpay_input" type="text"
									id="dateOfEstablishment" name="dateOfEstablishment"
									value="%{user.dateOfEstablishment}" autocomplete="off"
									onkeydown="return DateFormat(this, event.keyCode)"
									maxlength="10"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">CIN</label>
								<s:textfield class="lpay_input" type="text" id="cin" name="cin"
									value="%{user.cin}" autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">PAN</label>
								<s:textfield class="lpay_input" type="text" id="pan" name="pan"
									value="%{user.pan}" autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Name on PAN Card</label>
								<s:textfield class="lpay_input" type="text" id="panName"
									name="panName" value="%{user.panName}" autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Expected Number of Transaction</label>
								<s:textfield class="lpay_input" type="text"
									id="noOfTransactions" name="noOfTransactions"
									value="%{user.noOfTransactions}" autocomplete="off"
									onkeypress="javascript:return isNumber (event)"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Expected Amount of Transaction</label>
								<s:textfield class="lpay_input" type="text"
									id="amountOfTransactions" name="amountOfTransactions"
									value="%{user.amountOfTransactions}"
									onkeypress="javascript:return isNumber1 (event)"
									autocomplete="off"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Website URL</label>
								<s:textfield class="lpay_input" type="text" id="website"
									name="website" value="%{user.website}"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Business Model</label>
								<s:textfield class="lpay_input" type="text" id="businessModel"
									name="businessModel" value="%{user.businessModel}"
									autocomplete="off"
									OnKeypress="javascript:return isAlphaNumeric(event,this.value);"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
						<div class="col-md-4 mb-20">
							<div class="lpay_input_group">
								<label for="">Merchant Transaction Email</label>
								<s:textfield class="lpay_input" type="text"
									id="transactionEmailId" name="transactionEmailId"
									value="%{user.transactionEmailId}"></s:textfield>
							</div>
							<!-- /.lpay_input_group -->
						</div>
						<!-- /.col-md-4 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.lpay_tab-content -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 text-center button-wrapper button_wrapper">
				<s:a class="lpay_button lpay_button-md lpay_button-primary" action='resellerList'>Back</s:a>
				<button class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
			</div>
			<!-- /.col-md-12 -->
		</s:form>
	</div>
	<!-- /.row --> </section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<script type="text/javascript">
		$(document).ready(function() {

			var _merchantLogoSrc = $("[data-id='merchantLogo']").val();
			if(_merchantLogoSrc !== "") {
				$("#upload_img").attr("src", "data:image/png;base64," + _merchantLogoSrc);
				$(".upload-custom-logo").removeClass("d-none");
				$("#title-upload-logo").text("Change Logo");
				$("#allow-logo-pg").removeClass("d-none");
			}

			function previewImg(){
			var _val = $(this).val();
			var _parent = $(this).closest("label");
			var _img = document.querySelector("#upload_img");
			var _inputFile = document.querySelector('#uploadMerchantLogo').files[0];
			var _reader = new FileReader();
			var _fileSize = $(this)[0].files[0].size;
			var _tmpName = _val.replace("C:\\fakepath\\", "");

			if(_fileSize < 2000000){
				_parent.attr("data-file", "success");
				if(_tmpName.length > 20) {
					var strStart = _tmpName.slice(0, 15),
						strEnd = _tmpName.slice(_tmpName.length - 9, _tmpName.length);
					
					_tmpName = strStart + "..." + strEnd;
				}
				_parent.find("#merchantLogoName").text(_tmpName);
				_reader.addEventListener("load", function(){
					_img.src = _reader.result;
		
				}, false);
		
				if(_inputFile){
					_reader.readAsDataURL(_inputFile);
				}
			}else{
				_img.src = "";
				_parent.attr("data-file", "size-error");
			}
		}

		$("#uploadMerchantLogo").on("change", previewImg);

			$("#logoFlag").on("change", function(e) {
				if($(this).is(":checked")) {
					$(".upload-custom-logo").removeClass("d-none");
					$(".uploaded-logo").removeClass("d-none");

					if(_merchantLogoSrc !== "") {
						$("#upload_img").attr("src", "data:image/png;base64," + _merchantLogoSrc);
						$(".upload-custom-logo").removeClass("d-none");
						if($("#uploadMerchantLogo").val() == "") {
							$("#value-merchantLogo").val(_merchantLogoSrc);
						}
					}
				} else {
					$(".uploaded-logo").addClass("d-none");
					$(".uploaded-logo img").attr("src", "");
					$(".upload-custom-logo").addClass("d-none");
					$("#value-merchantLogo").val("");

					$("#uploadMerchantLogo").val("");
					$("#label-upload-logo").removeAttr("data-file");
					$("#merchantLogoName").text("");
				}
			});

			function checkResellerPermission(){
				var _permission = document.querySelector("#resellerPermission").value.split("-");
				console.log(_permission);
				for(var i = 0; i < _permission.length; i++){
					var _selector = document.querySelector(".list-permission [value='"+_permission[i]+"']");
					
					if(_selector !== null){
						_selector.checked = true;
					_selector.closest("label").classList.add("checkbox-checked");
					}
				}
			}

			checkResellerPermission();

			var _returnUrl = document.querySelector(".returnUrl").innerText.trim();
			document.querySelector("#returnUrl").value = _returnUrl;


			var _getTab = document.querySelector(".lpay_tabs");
			var _getX = _getTab.getBoundingClientRect().y;
			window.addEventListener('scroll', function(e) {
				var _getWindow = window.scrollY;
				if(_getWindow > _getX){
					document.querySelector(".button_wrapper").classList.remove("button-wrapper");
				}else{
					document.querySelector(".button_wrapper").classList.add("button-wrapper");
				}
			});

			var checkboxCollection = $(".checkbox-label input[type='checkbox']");
			for(var i = 0; i < checkboxCollection.length; i++) {
				var inputCheckbox = checkboxCollection[i];
				checkboxCollection[i].closest("label").setAttribute("for", checkboxCollection[i].id); 
				if(inputCheckbox.checked == true) {
					inputCheckbox.setAttribute("checked", true);
					inputCheckbox.closest("label").classList.add("checkbox-checked");
				}
			}
			$("body").on("change", ".checkbox-label input", function(e) {
				if ($(this).is(":checked")) {
					$(this).closest("label").addClass("checkbox-checked");
				} else {
					$(this).closest("label").removeClass("checkbox-checked");
				}
			});

			var _button = document.querySelectorAll(".lpay-nav-link");
			_button.forEach(function(e) {
				e.addEventListener("click", function(f) {
					var _getAttr = f.target.attributes["data-id"].nodeValue;
					var _getAllLink = document.querySelectorAll(".lpay-nav-link");
					var _getAll = document.querySelectorAll(".lpay_tab-content");
					_getAllLink.forEach(function(c) {
						c.closest(".lpay-nav-item").classList.remove("active");
					});
					_getAll.forEach(function(d) {
						d.classList.add("d-none");
					});
					this.closest(".lpay-nav-item").classList.add("active");
					document.querySelector("[data-target=" + _getAttr + "]").classList.remove("d-none");
				});
			});
		});
	</script>
</body>
</html>