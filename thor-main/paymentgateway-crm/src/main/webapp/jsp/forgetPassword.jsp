<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="X-UA-Compatible" content="ie=edge">
	<title>Forgot Password</title>
	<link rel="shortcut icon" href="../image/favicon-32x32.png" type="image/x-icon">
	<link rel="stylesheet" href="../css/bootstrap.min.css">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,900&display=swap">
	<link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/login.css">
	<link rel="stylesheet" href="../css/loader-animation.css">
	<link rel="stylesheet" href="../css/styles.css">

	<style type="text/css">
		/* Manual css s:actionmessage for this page */

		.position-relative{
			position: relative;
		}

		.errorMessage {
			font: normal 11px arial;
			color: #ff0000;
			display: block;
			margin: -15px 0px 3px 0px;
			padding: 0px 0px 0px 0px;
		}
		#error2 {
			color: red;
		}
		.signup-headingbg{
			background: none;
		}
		.signupbox h4 {
			margin: 0;
			padding: 14px 0 0 0;
			font-weight: 600;
			color: #002163;
			font-size: 17px;
		}
		.signupbox a:hover {
			color:#468bd8;
			opacity: 0.7;
		}
		.refreshbutton {
			margin-top: -4px;
			width: 28px;
			background: url(../image/refresh.jpg);
			height: 31px;
			border-radius: 5px;
		}
		.captchaImage {
			margin-bottom: 5px;
			width: 108px;
			height: 32px;
			border-radius: 4px;
			text-align: center;
			border: none;
			font-size: 16px;
			font-weight: 600;
			color: #333;
			letter-spacing: 1px;
			padding-left: 0;
		}
		.rederror{
			display: none;
		}
		#captcha{
			text-transform: uppercase;
		}
		.otpMsg{
			display:block;
			text-align:center;
			color:green;
		}
		.btnDisable1{
			pointer-events: none;
			background:grey;
		}
		.inactiveLink {
			pointer-events: none;
			cursor: default;
			color:#a6a6a6 !important;
		}
	</style>
</head>
<body>
	<div class="snackbar bg-snackbar-danger text-snackbar-danger" id="error-snackbar"></div>
	<div class="snackbar bg-snackbar-success text-snackbar-success" id="success-snackbar"></div>

	<!-- LOADER -->
	<div class="loader-container w-100 vh-100 lpay-center">
        <div class="loaderImage">
            <img src="../image/loader.gif" alt="Loader">
        </div>
        <div id="loaderText" class="mt-10 d-none">
            <h3>You are being redirected to Payment Page…</h3>
        </div>
    </div>

	<section class="login-wrapper">
		<header class="login-header">
			<div class="login-header-logo d-flex justify-content-center logo">
				<img src="../image/white-logo.png" alt="Payment Gateway" class="img-fluid">
			</div>
			<!-- /.login-header-logo -->
			<s:a action="index" class="lpay_button lpay_button-md">Login Here</s:a>
			<!-- <button class="lpay_button lpay_button-md lpay_button-primary">Make Account</button> -->
		</header>
		<!-- /.login-header -->
		<div class="login-form">
			<div class="login-box-inner">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Verify Number</h2>
				</div>
				<!-- /.heading_icon -->
				<s:form action="resetPin" id="validateOtpAction" autocomplete="off">
							<s:token/>
							<p id="dataValue" class="m-0 font-size-12 text-success"></p>
							<s:actionmessage />
							<div class="form-group lpay_input_group Inactive" id="resetNumber">
								<label for="resetPhoneNumber"   class="">Mobile Number*</label>
								<div class="position-relative">
									<s:textfield 
										readonly="true"
										id="resetPhoneNumber" 
										name="phoneNumber" 
										class="lpay_input" 
										maxlength="10" 
										autocomplete="off"
										
										value="%{phoneNumber}"										
									/>
									<p class="error" id="phoneError">Please enter valid phone number</p>
									<!-- <a href="#" id="generateOtp" class="color-primary font-size-14 font-weight-bold d-none">Generate OTP</a> -->
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.form-group -->
							<div class="form-group" id="passwordOtp">								
								<label for="resetOtp"  class="lable-default w-100">OTP* <p class="error" id="error-otp" style="float: right;position:static"></p></label>
								</label>
								<div class="position-relative">
									<div class="otp-pin-wrapper">
										<input type="text" class="otp-input-common font-family-password" data-id="pinBox1">
										<input type="text" class="otp-input-common font-family-password" data-id="pinBox2">
										<input type="text" class="otp-input-common font-family-password" data-id="pinBox3">
										<input type="text" class="otp-input-common font-family-password" data-id="pinBox4">
										<input type="text" class="otp-input-common font-family-password" data-id="pinBox5">
										<input type="text" class="otp-input-common font-family-password" data-id="pinBox6">
									</div>
									<s:textfield
										name="otp"
										maxlength="6"
										type="hidden"
										class="form-control font-family-password"
										id="resetOtp"
										placeholder="Enter OTP here"
										autocomplete="off"
									/>
									<div id="timer" style="float: right;
									font-weight: 400;
									font-size: 13px;
									position: absolute;
									left: 0;
									bottom: -22px;color: #fff;"></div>
									<a href="#" data-id="generateOtp" style="float: left;font-size: 13px;color: #fff;" class="font-size-14 d-none">Resend OTP</a>
								</div>
								<!-- /.position-relative -->

							</div>
							<!-- /.form-group -->
							<!-- /.form-group -->
							<div class="row justify-content-end forget-btn d-none">
								<div class="col-12">
									<s:submit
										id="submit"
										value="Submit"
										class="btn btn-primary py-10 full-width" >
									</s:submit>
								</div>
								<!-- /.col-md-6 -->
							</div>
						</s:form>
			</div>
			<!-- /.login-box-inner -->
		</div>
		<!-- /.login-form -->
		<div class="footer-logo">
			<small>Powered by</small>
          <!-- <img src="../image/login-footer-logo.png" height="40px" alt="Pg Logo">  -->
		  <span class="footer-logo_text">Paytapp</span>
		</div>
		<!-- /.footer-logo -->
	</section>
	<!-- login-wrapper -->
		
	<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
	<script src="../js/login-script.js"></script>

	<script>

		$(document).ready(function(e){
			$("#passwordOtp").find("[data-id='pinBox1']").focus();
			$("#passwordOtp").find("[data-id='pinBox1']").attr("readonly", false);
		})

	function timerFunction(){
        var sec = 50, countDiv = document.getElementById("timer"),
        secpass,
        countDown   = setInterval(function () {
            'use strict';
            
            secpass();
        }, 1000);
    
        function secpass() {
            'use strict';
            
            var min     = Math.floor(sec / 60),
                remSec  = sec % 60;
            
            if (remSec < 10) {
                remSec = '0' + remSec;
            }
            if (min < 10) {
                min = '0' + min;
            
            }
            countDiv.innerHTML = min + ":" + remSec;
            
            if (sec > 0) {
                
                sec = sec - 1;
                
            } else {
                
                clearInterval(countDown);
                $("[data-id='generateOtp']").removeClass("d-none");
                $("#resendOtp").removeClass("d-none");
                $("#resendOtpSignUp").removeClass("d-none");
                
                countDiv.innerHTML = '';
                
            }
        }
	}
	
	timerFunction();
		
		$('#emailId').keyup(function(){
			str = $(this).val();
			str = str.replace(/\s/g,'');
			$(this).val(str);
		});

		function isValidEmail(inputId) {
			var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
			var emailElement = document.getElementById(inputId);
			var emailValue = emailElement.value;
			if (emailValue.trim() !== "") {
				if (emailValue.match(emailexp)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		function removeOtpErr() {
			var otpValues = document.getElementById("otp").value;
			if(otpValues != null || otpValues != "" || otpValues.length >= 5){
				document.getElementById("otpError").style.display = "none";
			}
		}
		function checkOtp() {
			var regex = /^[0-9]+$/;
			var key = String.fromCharCode(event.charCode ? event.which : event.charCode);
			if (!regex.test(key)) {
				event.preventDefault();
				return true;
			}
		}
		function checkOtpValidation(event) {
			var emailIdVal = document.getElementById("emailId").value;
			var otpVal = document.getElementById("otp").value;
			var otpCaptcha = document.getElementById("captcha").value;
			document.getElementById("emailError").style.display = "none";
			document.getElementById("otpError").style.display = "none";
			document.getElementById("enterCaptcha").style.display = "none";
			if (emailIdVal == null || emailIdVal == "" || !isValidEmail('emailId')) {
				document.getElementById("emailError").style.display = "block";
				event.preventDefault();
			} else if(otpVal == null || otpVal == "" || otpVal.length<5) {
				document.getElementById("otpError").style.display = "block";
				event.preventDefault();
			} else if(otpCaptcha == null || otpCaptcha == "") {
				document.getElementById("enterCaptcha").style.display = "block";
				event.preventDefault();
			} else {
				document.getElementById("emailError").style.display = "none";
				document.getElementById("otpError").style.display = "none";
				document.getElementById("enterCaptcha").style.display = "none";
			}
		}
		function emailCheck() {
			var emailReg = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
			var emailElement = document.getElementById("emailId");
			var emailValue = emailElement.value;
			if (emailValue.trim() !== "") {
				if (!emailValue.match(emailReg)) {
					document.getElementById('emailError').style.display = "block";
					return false;
				} else {
					document.getElementById('emailError').style.display = "none";
					return true;
				}
			}
		}
	</script>
</body>
</html>