<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="X-UA-Compatible" content="ie=edge">
	<title>Sign Up</title>
	<link rel="shortcut icon" href="../image/favicon-32x32.png" type="image/x-icon">
	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,900&display=swap">
	<link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/login.css">
	<link rel="stylesheet" href="../css/loader-animation.css">
	<link rel="stylesheet" href="../css/styles.css">
	<style>

		.login-wrapper .error{
			white-space: nowrap;
		}

		.register-btn{
			font-size: 14px !important;
		}

	</style>
</head>
<body>
	
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
				<img src="../image/white-logo.png" alt="Pg" class="img-fluid">
			</div>
			<!-- /.login-header-logo -->
			<s:a action="index" class="lpay_button lpay_button-md register-btn">Login Here</s:a>
			<!-- <button class="lpay_button lpay_button-md lpay_button-primary">Make Account</button> -->
		</header>
		<main class="login-form">
			<div class="create-account-box-inner">
				<!-- <div class="login-header-logo">
					<img src="../image/white-logo.png" alt="Pg">
				</div> -->
				<!-- <span class="clip-shape"></span> -->
				<!-- /.clip-shape -->
				<div class="heading-wrapper justify-content-center">
					<div class="heading_with_icon mb-30">
						<h2 class="heading_text fw-bold">Create new one!</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.heading-wrapper -->
				
				<s:form action="signup" id="formname">
					<s:token/>

					<!-- <p id="dataValue" class="m-0 font-size-12"></p> -->

					<span id="error2"></span>

					<s:actionmessage />

					<div class="form-group f-left create-account-radio">
						<label class="lable-default">Create an account for</label>
						<div class="position-relative signup-disabled font-size-12 d-flex">
							<label for="merchant" class="position-relative d-flex">
								<input type="radio" name="userRoleType" id="merchant" value="Merchant" class="radio-hidden" checked="checked">
								<span class="icon-radio"></span>
								<span class="radio-text ml-10">Merchant</span>
							</label>
							<label for="reseller" class="position-relative d-flex ml-35">
								<input type="radio" name="userRoleType" id="reseller" value="reseller" class="radio-hidden">
								<span class="icon-radio"></span>
								<span class="radio-text ml-10">Reseller</span>
							</label>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.form-group -->

					<div class="form-group f-left mpa-verification create-account-radio">
						<label class="lable-default">MPA Verification</label>
						<div class="position-relative signup-disabled font-size-12 d-flex">
							<label for="offlineMpa" class="position-relative d-flex">
								<input type="radio" name="mpaOnlineOffLineFlag" id="offlineMpa" value="offline" class="radio-hidden" checked="checked">
								<span class="icon-radio"></span>
								<span class="radio-text ml-10">Offline</span>
							</label>
							<label for="onlineMpa" class="position-relative d-flex ml-35">
								<input type="radio" name="mpaOnlineOffLineFlag" id="onlineMpa" value="online" class="radio-hidden">
								<span class="icon-radio"></span>
								<span class="radio-text ml-10">Online</span>
							</label>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.form-group -->
					<div class="form-group w-100 lpay_input_group d-none" data-id="partnerFlag">
						<label for="partnerFlag">
							<input type="checkbox" id="partnerFlag" name="partnerFlag">
							 <span style="margin-left: 5px;">is Partner</span> 
						</label>
					</div>
					<!-- /.form-group w-100 -->
					<div class="form-group lpay_input_group">
						<label class="lable-default mb-0">Business Name*</label>
						<div class="position-relative">
							<s:textfield 
								id="businessName" 
								maxlength="100" 
								name="businessName" 
								class="lpay_input" 
								onkeydown="return alphaNumeric(event)"
								autocomplete="off" 
							/>
							<p class="error" id="errorBusninessName">Please Enter Valid Business Name</p>
						</div>
					</div>
					<!-- /.form-group -->
					
					<div class="form-group emailPhone-div">
						<div class="row">
							<div class="col-md-6 lpay_input_group">
								<label class="lable-default mb-0">Email*</label>
								<div class="position-relative">
									<s:textfield 
										id="sighnUpemailId" 
										name="emailId" 
										class="lpay_input" 
										maxlength="50" 
										autocomplete="new-password"
										style="padding-right: 40px"
									/>
									<p class="error" id="errorEmail">Please Enter Valid Email</p>
									<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
									<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.col-md-6 -->

							<div class="col-md-6 lpay_input_group">
								<label class="lable-default mb-0">Mobile Number*</label>
								<div class="position-relative">
									<s:textfield 
										id="signUpPhoneNumber" 
										onkeydown="return numOnlyInTextInput(event);" 
										name="phoneNumber" 
										cssClass="lpay_input mobileNumber-input" 
										maxlength="10" 
										autocomplete="off"
										inputmode="numeric"
										oncopy="return false" onpaste="return false"
									/>
									<p class="error" id="errorPhone"></p>
									<!-- <p class="success" id="errorPhone">Please Enter Valid Mobile Number</p> -->
									<a href="#using-otp" id="generateOtpBtnSignUp" class="d-none" style="font-size: 11px;color: #333;font-weight: 600;">Generate OTP</a>
									<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
									<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.col-md-6 -->
						</div>
						<!-- /.row -->
					</div>
					<!-- /.form-group -->
					<div class="form-group d-none" id="otp-box">
						<div class="row">
							<div class="col-md-12">
								<div class="" id="using-otp">
									<label class="font-size-12 color-grey-light m-0 w-100">Enter OTP which sent to your mobile number 
										<!-- /#timer --><p class="error" id="error-otp" style="float: right;"></p></label>
									<div class="position-relative">
										<div class="otp-pin-wrapper">
											<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox1">
											<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox2">
											<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox3">
											<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox4">
											<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox5">
											<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox6">
										</div>
										<!-- /.otp-pin-wrapper -->
										<s:textfield type="hidden" autocomplete="nope" name="otp" maxlength="6" id="otp" class="form-control font-family-password" />
										<div class="opt-login-quick">
											<div id="timer" style="float: right;
										font-weight: 600;
										font-size: 11px;
										color: #333;
										position: absolute;
										left: 0px;
										bottom: 0px;"></div>
										<a href="#" id="resendOtpSignUp" class="d-none" style="float: left;font-size: 11px;color: #333;font-weight: 600;">Resend OTP</a>
										</div>
										<!-- /.opt-login-quick -->
										<div class="alt-login-option position-absolute d-flex align-items-center mr-10">

											<!-- <a href="#using-password" class="color-primary font-size-14 font-weight-bold color-primary">Use Password</a> -->
											<!-- <a href="#resend-otp" class="color-primary font-size-14 font-weight-bold">Resend OTP</a> -->
										</div>
										
									</div>
									<!-- <p class="font-size-12 color-grey-light mt-8"></p> -->
								</div>
							</div>
							<!-- /.col-md-6 -->
						</div>
						<!-- /.row -->
					</div>
					<div class="form-group d-none  pin-group">
						<div class="row">
							<div class="col-md-6 lpay_input_group">
								<label class="lable-default mb-0">PIN*</label>
								<div class="position-relative pin-one">
									<div class="otp-pin-wrapper">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox1">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox2">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox3">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox4">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox5">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox6">
									</div>
									<input type="hidden" name="pin" id="pin" onkeypress="onlyDigit(event)" maxlength="6" class="form-control font-family-password">
									
									<!-- <s:textfield 
										type="text" 
										name="pin" 
										id="pin" 
										onkeypress="onlyDigit(event)"
										class="form-control font-family-password"
										maxlength="6"
									/> -->
									<p class="error" id="errorPassword">Please enter valid password</p>
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.col-md-6 -->
							<div class="col-md-6 lpay_input_group">
								<label class="lable-default mb-0">Confirm PIN*</label>
								<div class="position-relative confirm-pin">
									<div class="otp-pin-wrapper">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox1">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox2">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox3">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox4">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox5">
										<input type="text" inputmode="numeric" class="otp-input-common font-family-password" data-id="pinBox6">
									</div>
									<s:textfield 
										type="hidden" 
										name="confirmPin" 
										id="confirmPin" 
										class="form-control font-family-password"
										onkeypress="onlyDigit(event)"
										autocomplete="off" 
										maxlength="6"
									/>
									<p class="error" id="errorConfirmPassword">PIN doesn't match</p>
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.col-md-6 -->
						</div>
						<!-- /.row -->
					</div>
					<!-- /.form-group -->
					<!-- /.form-group -->
					<div class="row justify-content-end d-none" id="signup-btn">
						<div class="col-md-12 signup-btn">
							<s:submit value="Submit" id="signup" method="submit" cssClass="lpay_button lpay_button-md lpay_button-secondary"></s:submit>
						</div>
						<!-- /.col-md-6 -->
					</div>
<!-- 
					<div class="text-center mt-20">
						Already have an account ? <s:a action="index" class="font-size-14 color-primary font-weight-bold">Login here</s:a>
					</div> -->
				</s:form>
			</div>
			<!-- /.login-box -->
		</main>
		<!-- /.signup-box -->
		<div class="footer-logo">
			<small>Powered by</small>
          <span class="footer-logo_text">Paytapp</span> 
		</div>
		<!-- /.footer-logo -->
	</section>
	<!-- /.signup-wrapper -->

	
	<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/login-script.js"></script>
	
	<script type="text/javascript">
       
	$(document).ready(function(e){
        $("body").addClass("loader--inactive");
        $("#signup").on("click", function(e){
            if($("#partnerFlag").is(":checked")){
                $("#isPartner").val("on");
            }else{
				$("#partnerFlag").val("off");
				
            }
        });
    });
	</script>
	

	<!-- <script>
		$(document).ready(function(e){
			$("#signup").on("click", function(e){
				var formEl = document.forms.formname;
				var _newForm = new FormData(formEl);
				var _name = _newForm.get('userRoleType');
				var _mpa = _newForm.get('mpaOnlineOffLineFlag');
				console.log(_mpa);
				console.log(_name);
				return false;
			})
		})
	</script> -->

	<!-- <script>
        $(document).ready(function(e){
            $("#signup").on("click", function(e){
                var formEl = document.forms.formname;
                var _newForm = new FormData(formEl);
                var _flag = _newForm.get('isPartner');
                var _name = _newForm.get('userRoleType');
                var _mpa = _newForm.get('mpaOnlineOffLineFlag');
                console.log(_flag);
                console.log(_mpa);
                console.log(_name);
                return false;
            })
        })
    </script>-->

</body>
</html>