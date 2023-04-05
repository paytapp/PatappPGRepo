<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Sign Up</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- stylesheet -->
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<link href="../css/fonts.css"/>
<link rel="stylesheet" href="../css/login.css">
<!-- javascripts -->
<script src="../js/jquery.minshowpop.js"></script>
<!-- <script src="../js/jquery.formshowpop.js"></script> -->
<script src="../js/jquery.popupoverlay.js"></script>
<!-- <script src="../js/commonValidate2.js"></script> -->
<!-- <script src="../js/captcha.js"></script> -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>


<style>

.merchant__form_group{
	position: relative;
}

.position-relative{
	position: relative;
}

input[type="radio"]{margin-left: 10px!important;}
#radiodiv span{font-size :14px;margin-left: 4px;color: black !important;}
#radioError {
    color: #ff0000!important;
    margin-left: 13px;
    font-size: 11px;
}

#subcategory{
	color: #555;
	font-weight: 600;
	margin-top: 15px;
	width: 100%;
}

.mainDiv .signuptextfield{
	height: 40px !important;
	font-size: 13px;
	width: 100% !important;
}
.error-text
 {
  color:#a94442;
  font-weight:bold;
  background-color:#f2dede;
  list-style-type:none;
  text-align:center;
  list-style-type: none;
  margin-top:10px;
  }
.error-text li 
 { 
   list-style-type:none;   
 }

 .d-none{
	 display: none !important;
 }

 .merchant__form_group .errorSec{
	right: 0;
    position: absolute;
    top: 0;
    margin: 0;
 }

#response{color:green;}
.mainDiv .adduR{
	width: 402px;
	padding: 2% 0;
	border-radius: 5px;
	background: #fafafa;
	border-color:#e6e6e6;
}
.mainDiv .adduTR{
	width: 90%;
    float: none;
    margin: 0 auto;
    font-weight: 600;
    margin-bottom: 12px;
}
.mainDiv .adduTR:last-child{
	margin-bottom: 0;
}
.mainDiv .signuptextfield, .signupdropdwn{
	font-family: 'Open Sans', sans-serif;
	font-weight: 600;
}

.error{
	color: #f00;
	font-size: 11px;
	display: none;
}

.show{
	display: block;
}

.inputTitle{
color: #7a7a7a;
    font-weight: 600;
    display: none;
}
#subcategorydiv.adduTR{
	width: 100%;
}
.errorSec {
    color: red;
    text-indent: 2px;
    font-size: 11px;
    display: none;
}

</style>
</head>

<body>

	<div class="signupCrm lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">User Registration</h2>
                </div>
                <!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-12">
				<div id="saveMessage" class="lpay_success_wrapper">
					<s:if test="%{responseObject.responseCode=='000'}">
						<s:actionmessage class="lpay_success" />
					</s:if>
					<s:else>
						<!-- <class="error-text"><s:actionmessage theme="simple"/> -->
        			</s:else>
				</div>
			</div>
			<!-- /.col-md-12 -->

			<s:form action="signupMerchant" id="formname" autocomplete="off">
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					<label>Merchant/Reseller Type <span class="text-danger">*</span></label>
						<s:select
							name="userRoleType"
							id="userRoleType"
							headerKey="1"
							list="#{'subMerchant':'Create a Sub Merchant'}" class="selectpicker">
						</s:select>					
					</div>
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
					<label>MPA Verification Type <span class="text-danger">*</span></label>
						<select name="mpaOnlineOffLineFlag" class="selectpicker" id="mpaOnlineOffLineFlag">
							<option value="offline">Offline</option>
							<option value="online">Online</option>
						</select>
					</div>
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-4 mb-20 common-validation" id="superMerchantDiv">
					<div class="lpay_select_group">
					<label for="">Super Merchant Id <span class="text-danger">*</span></label>
					
						<s:select name="superMerchant" class="selectpicker" id="superMerchant" 
						list="superMerchantList" listKey="superMerchantId" listValue="businessName"
						 autocomplete="off" />
					
					</div>
					<!-- /.lpay_select_group -->  
					<p class="errorSec errorSubMerchant"></p>
				</div>
				<!-- /.col-md-3 -->
			
				<div class="col-md-4 mb-20 common-validation">
					<div class="lpay_input_group">
						<label>Business Name <span class="text-danger">*</span></label>
						<div class="txtnew" id="businessField">
							<s:textfield
								id="businessName"
								name="businessName"
								cssClass="lpay_input acquirer-input"
								autocomplete="off"
								maxlength="100"
								onkeydown="return alphaNumeric(event)"
							/>
						</div>
						<p class="errorSec errorBusninessName"></p>
					</div>
				</div>
				<!-- col-md-4 -->

				<div class="col-md-4 mb-20 common-validation">
					<div class="lpay_input_group">
						<label>Email <span class="text-danger">*</span></label>
						<div class="txtnew position-relative">
							<s:textfield id="emailId" name="emailId" cssClass="lpay_input acquirer-input" automcomplete="off" maxlength="50" />
							<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
							<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
						</div>
						<p class="errorSec errorEmail"></p>
					</div>
					<!-- /.merchant__form_group -->
				</div>
				<!-- /.col-md-4 -->
			
				<div class="col-md-4 mb-20 common-validation">
					<div class="lpay_input_group">
						<label>Mobile Number <span class="text-danger">*</span></label>
						<div class="txtnew position-relative">
							<s:textfield
								id="loginNumber"
								maxlength="10"
								name="mobile"
								cssClass="lpay_input acquirer-input"
								oninput="onlyNumberInput(this)"
								automcomplete="false"
							/>
							<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
							<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
							<!-- <a href="#" class="icon-edit status-img" data-id="loginNumber"><i class="fa fa-pencil"></i></a> -->
						</div>
						<p class="errorSec errorPhone" id="errorPhone">Please Enter Valid Mobile Number</p>
					</div>
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-4 mb-20">
					<div class="lpay_input_group pin-div">
						<label>PIN <span class="text-danger">*</span></label>
						<div class="txtnew position-relative new-pin">
							<div class="otp-pin-wrapper">
								<input type="text" class="otp-input-common font-family-password"  data-id="pinBox1">
								<input type="text" class="otp-input-common font-family-password"  data-id="pinBox2">
								<input type="text" class="otp-input-common font-family-password"  data-id="pinBox3">
								<input type="text" class="otp-input-common font-family-password"  data-id="pinBox4">
								<input type="text" class="otp-input-common font-family-password"  data-id="pinBox5">
								<input type="text" class="otp-input-common font-family-password"  data-id="pinBox6">
							</div>
							<s:textfield id="pin" name="pin" type="hidden" cssClass="signuptextfield acquirer-input" placeholder="Password*"  maxlength="32" automcomplete="false" />
						</div>
						<p class="errorSec errorPassword">Please Enter Valid Password</p>
					</div>
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-4 mb-20">
					<div class="lpay_input_group pin-div">
						<label class="font-size-12 color-grey-light m-0 font-weight-medium">Confirm PIN <span class="text-danger">*</span></label>
							<div class="txtnew position-relative confirm-pin">
								<div class="otp-pin-wrapper">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox1">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox2">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox3">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox4">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox5">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox6">
								</div>
								<s:textfield id="confirmPin" name="confirmPassword" type="hidden" cssClass="signuptextfield acquirer-input"  automcomplete="false" maxlength="32"/>
							</div>
							<p class="errorSec passwordNotMatch" id="errorConfirmPassword">PIN Doesn't Match</p>
					</div>
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-12 text-center">
					<button id="submit" class="lpay_button lpay_button-md lpay_button-secondary" disabled>Save User Data</button>
				</div>
				<!-- /.col-md-4 -->
			</s:form>
		</div>
		<!-- /.row -->
	</div>
	<!-- /.signupCrm lpay_section white-bg box-shadow-box mt-70 p20 -->
	<script src="../js/merchant-crm-signup.js"></script>

</body>
</html>