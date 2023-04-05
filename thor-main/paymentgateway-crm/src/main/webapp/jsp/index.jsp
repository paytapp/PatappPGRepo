<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html>
<%
	String error = request.getParameter("error");
	if (error == null || error == "null") {
		error = "";
	}
%>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="ie=edge">
<title>Login</title>
<link rel="shortcut icon" href="../image/favicon-32x32.png" type="image/x-icon">
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,900&display=swap">
<link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
<link rel="stylesheet" href="../css/common-style.css">
<link rel="stylesheet" href="../css/login.css">
<link rel="stylesheet" href="../css/loader-animation.css">
<link rel="stylesheet" href="../css/styles.css">
<link rel="stylesheet" href="../css/bootstrap-flex.css">
<style>
	.login-wrapper .error{
		left: 0;
		right: auto;
	}

	.loaderImage img{ max-width: 70px; }
	
</style>
</head>
<body>
	<div class="loader-container w-100 vh-100 lpay-center">
        <div class="loaderImage">
            <img src="../image/loader.gif" alt="Loader">
        </div>
    </div>

	<section class="login-wrapper">
		<header class="login-header">
			<div class="login-header-logo d-flex justify-content-center logo ">
				<img src="../image/white-logo.png" alt="Payment Gateway" class="img-fluid">
			</div>
			<!-- /.login-header-logo -->
			<s:a action="merchantSignup" class="lpay_button lpay_button-md register-btn">Register Yourself</s:a>
			<!-- <button class="lpay_button lpay_button-md lpay_button-primary">Make Account</button> -->
		</header>
		<!-- /.login-header -->
		<main class="login-form">
			<div class="login-box-inner px-30 py-30 d-flex flex-column align-items-center">
				<!-- <div class="login-header-logo d-flex justify-content-center">
					<img src="../image/white-logo.png" alt="Payment Gateway">
				</div> -->
				<!-- <span class="clip-shape"></span> -->
				<div class="heading-wrapper justify-content-center">
					<div class="heading_with_icon mb-30">
						<h2 class="heading_text fw-bold">Welcome Back</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.heading-wrapper -->
				<s:form action="login" method="post" id="login-form" onselectstart="return false" oncontextmenu="return false;" autocomplete="off" class="w-100">
					<input type="hidden" name="loginType" id="loginType" value="pin" />
					<s:token />
					<s:fielderror fieldName="error" />
					<s:fielderror fieldName="pinResetSuccess" />
					<s:fielderror fieldName="captcha" />
					<p id="dataValue" class="m-0 font-size-12 transition-all"></p>
					<s:actionmessage />

					<div class="lpay_input_group mobile-div">
						<label class="font-size-10 color-grey-light m-0">Mobile Number*</label>
						<span class="font-size-10 color-grey-light update-number d-none" onclick="resetField(this)">Reset</span>
						<div class="position-relative mb-10">
							<s:textfield
								type="text"
								maxlength="10"
								inputmode="numeric"
								name="phoneNumber"
								id="loginNumber"
								onkeypress="onlyDigit(event)"
								oncopy="return false"
								onpaste="return false"
								class="lpay_input mobileNumber-input"
								autocomplete="new-password"
							/>
							<!-- <s:fielderror fieldName="mobile" /> -->
							<p class="error" id="errorPhone"></p>
						</div>
					</div>
					<!-- /.form-group -->
					<div class="login-action-password d-none">
						<button class="lpay_button lpay_button-md lpay_button-primary taget-div" data-target="login-pwd">Login with Password</button>
					</div>

					<div class="login-action-btn d-none">
						<button class="lpay_button lpay_button-md lpay_button-primary taget-div" data-target="login-pin">Login with PIN</button>
						<button class="lpay_button lpay_button-md lpay_button-secondary taget-div" id="loginOtp" data-target="login-otp">Login with OTP</button>
					</div>

					<div class="form-group d-none login-common" id="login-pwd">
						<div class="position-relative">
							<label for="" class="font-size-10 color-grey-light m-0 position-relative d-flex justify-content-between">
								<span>Password*</span>
								<span class="custom-error" data-id="error-adminPassword"></span>
							</label>
							<div class="position-relative">
								<s:textfield
									type="password"
									name="password"
									id="adminPassword"
									class="form-control font-family-password admin-check"
									autocomplete="new-password"
									oncopy="return false"
									onpaste="return false"
									oninput="onlyDigit(event);"
								/>
							</div>
						</div>
						
						<div class="position-relative mt-10">
							<label for="" class="font-size-10 color-grey-light m-0 d-flex justify-content-between">
								<span>OTP*</span>
								<span class="custom-error" data-id="error-adminOtp">Invalid OTP</span>
							</label>
							<div class="position-relative">
								<s:textfield
									type="password"
									maxlength="6"
									inputmode="numeric"
									id="adminOtp"
									onkeypress="onlyDigit(event)"
									oncopy="return false"
									onpaste="return false"
									class="form-control font-family-password admin-check"
									autocomplete="off"
								/>
							</div>
						</div>
					</div>

					<div class="form-group d-none login-common" id="login-pin">
						<label for="" class="font-size-10 color-grey-light m-0">PIN*</label>
						<div class="position-relative">
							<div class="otp-pin-wrapper">
								<input type="text" inputmode="numeric" name="pinBox1" class="otp-input-common font-family-password" data-id="pinBox1">
								<input type="text" inputmode="numeric" name="pinBox2" class="otp-input-common font-family-password" data-id="pinBox2">
								<input type="text" inputmode="numeric" name="pinBox3" class="otp-input-common font-family-password" data-id="pinBox3">
								<input type="text" inputmode="numeric" name="pinBox4" class="otp-input-common font-family-password" data-id="pinBox4">
								<input type="text" inputmode="numeric" name="pinBox5" class="otp-input-common font-family-password" data-id="pinBox5">
								<input type="text" inputmode="numeric" name="pinBox6" class="otp-input-common font-family-password" data-id="pinBox6">
							</div>

							<s:textfield
								type="hidden"
								maxlength="6"
								name="pin"
								data-id="pin"
								id="loginPin"
								class="form-control font-family-password"
								autocomplete="new-password"
							/>
		
							<div class="login-quick-box">
								<a href="#" id="forget-login-otp" class="login-action">Forgot PIN</a>
								<a href="#" class="taget-div login-action" data-target="login-otp">Login with OTP</a>
							</div>
							<!-- /.login-quick-box -->
							<p class="error" id="errorPin"></p>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.form-group d-none -->

					<div class="form-group login-common d-none" id="login-otp">
						<label for="" class="font-size-10 color-grey-light m-0 w-100 position-relative">OTP
							<p class="error" id="error-otp" style="float: right; position:static">Invalid OTP</p>
						</label>
						<div id="timer" style="float: right; font-weight: 400; font-size: 11px; position: absolute;left: 0;bottom: 6px;color: #fff;"></div>
						<!-- /.font-size-12 color-grey-light m-0 -->
						<div class="position-relative">
							<div class="otp-pin-wrapper">
								<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox1">
								<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox2">
								<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox3">
								<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox4">
								<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox5">
								<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox6">
							</div>
							<!-- <s:textfield type="password" maxlength="6" name="phoneNumber" id="phoneNumber" class="otp-input font-family-password" autocomplete="off" /> -->
							<s:textfield
								type="hidden"
								maxlength="6"
								name="otp"
								id="otpLogin"
								data-id="login-otp-input"
								class="form-control font-family-password"
								autocomplete="new-password"
							/>
							
							<div class="login-quick-box opt-login-quick">
								<div id="timer" style="font-weight: 400;font-size: 11px;position: absolute;left: 0;bottom: 6px;color: #fff"></div>
								<a href="#" id="resendOtp" style="float: left;" class="login-action d-none">Resend OTP</a>
								<a href="#" class="taget-div login-action" data-target="login-pin">Login with PIN</a>
							</div>
							<!-- /.login-quick-box -->
							<p class="error" id="errorEmail"></p>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.form-group -->

					<div class="form-group d-none captcha-code-div lpay_input_group">
						<input
							type="text"
							maxlength="4"
							id="captchaLogin"
							name="captcha"
							placeholder="Enter Captcha"
							inputmode="numeric"
							class="lpay_input admin-check"
							style="width: 60%;float:left;"
							onkeypress="onlyDigit(event);"
						>

						<input type="hidden"  id="hideCaptcha">
						<p class="error" id="errorCaptcha" data-id="error-captchaLogin">Invalid Captcha</p>

						<div class="captcha-code" style="width: 40%;float:left">
							<img id="captchaImage" src="../Captcha.jpg"/>
							<!-- /#mainCaptcha -->
							<div class="refresh-icon">
								<i class="fa fa-refresh" aria-hidden="true"></i>
							</div>
							<!-- /.refresh-icon -->
						</div>
						<!-- /.captcha-code -->
					</div>
					<!-- /.form-group captch-code -->
		
					<input type="hidden" value="pwd" name="data" />
		
					<s:param>userId</s:param>
					<s:param>password</s:param>

					<s:submit key="submit" class="btn btn-primary w-100 py-10 d-none" value="Login" id="verify-login"></s:submit>
		
					<div class="login-submit-btn d-none">
						<s:submit key="submit" class="btn btn-primary w-100 py-10" id="btn-login" value="Login"></s:submit>
					</div>
				</s:form>
			</div>
			<!-- /.login-box -->
		</main>
		<!-- /.login-form -->
		<div class="footer-logo">
			<small>Powered by</small>
          <span class="footer-logo_text">Paytapp</span> 
		</div>
		<!-- /.footer-logo -->
	</section>
	<!-- /.login-wrapper -->

	<s:form method="POST" id="forgetPhoneNumber" action="forgetPassword">
        <s:hidden name="phoneNumber" />
        <!-- <s:hidden name="makerFileName" value="%{mpaMerchant.makerFileName}" /> -->
		<s:hidden  name="otp" />
		<s:hidden name="loginType" value="userOtp" />
	</s:form>
	
	<script src="../js/jquery-latest.min.js"></script>
	<script src="../js/login-script.js"></script>
</body>
</html>