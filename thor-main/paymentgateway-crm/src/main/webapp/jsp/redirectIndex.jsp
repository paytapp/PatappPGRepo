<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
<link rel="shortcut icon" href="../image/favicon-32x32.png"
	type="image/x-icon">

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,900&display=swap">
<link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
<link rel="stylesheet" href="../css/common-style.css">
<link rel="stylesheet" href="../css/login.css">
<link rel="stylesheet" href="../css/loader-animation.css">
</head>
<body>
<script type="text/javascript">
setTimeout(function() {
    	    $('#sessionWarning').fadeOut('fast');
    	}, 30000);
</script>
	<!-- LOADER -->
	<div class="loader-container">
		<div class="loader-box">
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--text"></div>
		</div>
	</div>

	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-8">

				<!-- /.logo mt-40 ml-40 -->
				<section class="login-box account-box">
					<header class="logo mt-20 mt-md-40 ml-md-25">
						<h1>
							<img src="../images/logo.png" alt="">
						</h1>
					</header>
					<div class="login-box-inner">
						<div class="session-msg mb-20 text-danger" id="sessionWarning">Your session has been expired!</div>

						<h2
							class="color-grey-dark font-size-32 font-weight-bold line-height-40">Welcome
							back,</h2>
						<p class="color-grey-light">Login to access your account.</p>

						<s:form action="login" method="post" id="login-form"
							onselectstart="return false" oncontextmenu="return false;"
							autocomplete="off">
							<input type="hidden" name="loginType" id="loginType" value="pwd" />
							<s:token />
							<s:fielderror fieldName="error" />
							<s:fielderror fieldName="pinResetSuccess" />
							<p id="dataValue" class="m-0 font-size-12 transition-all"></p>
							<s:actionmessage />
							<div class="form-group">
								<label class="font-size-12 color-grey-light m-0">Mobile Number*</label>
								<div class="position-relative mb-20">
									<s:textfield type="text" maxlength="10" name="phoneNumber"
										id="loginNumber"  onkeypress="onlyDigit(event)" class="form-control mobileNumber-input"
										autocomplete="new-password" />
									<!-- <s:fielderror fieldName="mobile" /> -->
									<p class="error" id="errorPhone"></p>

								</div>
							</div>
							<!-- /.form-group -->
							<div class="login-action-btn d-none">
								<button class="action-btn taget-div" data-target="login-pin">Login
									with PIN</button>
								<button class="action-btn taget-div" id="loginOtp"
									data-target="login-otp">Login with OTP</button>
							</div>
							<div class="form-group d-none login-common" id="login-pin">
								<label for="" class="font-size-12 color-grey-light m-0">PIN*</label>
								<div class="position-relative">
									<div class="otp-pin-wrapper">
										<input type="text"
											class="otp-input-common font-family-password"
											data-id="pinBox1"> <input type="text"
											class="otp-input-common font-family-password"
											data-id="pinBox2"> <input type="text"
											class="otp-input-common font-family-password"
											data-id="pinBox3"> <input type="text"
											class="otp-input-common font-family-password"
											data-id="pinBox4"> <input type="text"
											class="otp-input-common font-family-password"
											data-id="pinBox5"> <input type="text"
											class="otp-input-common font-family-password"
											data-id="pinBox6">
									</div>
									<s:textfield type="hidden" maxlength="6" name="pin"
										data-id="pin" id="loginPin"
										class="form-control font-family-password"
										autocomplete="new-password" />

									<div class="login-quick-box">
										<a href="#" id="forget-login-otp" class="font-size-14 color-primary font-weight-bold">Forget PIN</a>
										<a href="#" class="taget-div font-size-14 color-primary font-weight-bold"
											data-target="login-otp">Login with OTP</a>
									</div>
									<!-- /.login-quick-box -->
									<p class="error" id="errorPin"></p>
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.form-group d-none -->

							<div class="form-group login-common d-none" id="login-otp">
								<label for="" class="font-size-12 color-grey-light m-0 w-100 position-relative">OTP*
									<p class="error" id="error-otp" style="float: right;position:static">Invalid OTP</p>
									<!-- <div id="timer"
										style="float: right; font-weight: 600; font-size: 14px; position: absolute; right: 0; top: -1px;"></div> -->
								</label>
								<!-- /.font-size-12 color-grey-light m-0 -->
								<div class="position-relative">
									<div class="otp-pin-wrapper">
										<input type="text" class="otp-input-common" data-id="pinBox1">
										<input type="text" class="otp-input-common" data-id="pinBox2">
										<input type="text" class="otp-input-common" data-id="pinBox3">
										<input type="text" class="otp-input-common" data-id="pinBox4">
										<input type="text" class="otp-input-common" data-id="pinBox5">
										<input type="text" class="otp-input-common" data-id="pinBox6">
									</div>
									<!-- <s:textfield type="password" maxlength="6" name="phoneNumber" id="phoneNumber" class="otp-input font-family-password" autocomplete="off" /> -->
									<s:textfield type="hidden" maxlength="6" name="otp"
										data-id="login-otp-input"
										class="form-control font-family-password"
										autocomplete="new-password" />
									
									<div class="login-quick-box opt-login-quick">
										<div id="timer" style="font-weight: 600;font-size: 14px;position: absolute;left: 0;bottom: 5px;"></div>
										<a href="#" id="resendOtp" style="float: left;"
											class="font-size-14 color-primary font-weight-bold d-none">Resend
											OTP</a>
										<a href="#" class="taget-div font-size-14 color-primary font-weight-bold"
											data-target="login-pin">Login with PIN</a> 
									</div>
									<!-- /.login-quick-box -->


									<!-- /.login-quick-box -->
									<p class="error" id="errorEmail"></p>
								</div>
								<!-- /.position-relative -->
							</div>
							<!-- /.form-group -->


							<input type="hidden" value="pwd" name="data" />


							<!-- /.form-group -->

							<s:param>userId</s:param>
							<s:param>password</s:param>

							<div class="login-submit-btn d-none">
								<s:submit key="submit" class="btn btn-primary w-100 py-10"
									value="Login"></s:submit>
							</div>
							<!-- /.login-submit-btn -->


							<!-- <div class="text-center mt-20">
								<s:a action="forgetPassword" class="font-size-14 color-primary font-weight-bold">Forgot Password?</s:a>
							</div> -->
						</s:form>
					</div>
					<!-- /.login-box -->
				</section>
			</div>
			<!-- /.col-md-8 -->

			<div
				class="col-lg-4 signup-box d-flex align-items-center justify-content-center mt-30 mt-lg-0 sticky">
				<div
					class="signup-box-inner py-30 py-lg-0 pl-lg-30 pr-lg-30 pl-xl-35 pr-xl-35">
					<h2 class="signup-box-title text-white font-weight-light">Don't
						have an account yet?</h2>
					<p
						class="signup-box-desc text-white font-weight-light mt-sm-5 mt-lg-20 mt-xl-30">Create
						a new account now.</p>
					<s:a action="merchantSignup"
						class="btn btn-primary btn-white font-weight-medium mt-15 mt-lg-20 mt-xl-30 py-10 py-lg-15">Sign Up</s:a>
				</div>
			</div>
			<!-- /.col-md-4 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.container-fluid -->

<s:form method="POST" id="forgetPhoneNumber" action="forgetPassword">
        <s:hidden name="phoneNumber" />
        <!-- <s:hidden name="makerFileName" value="%{mpaMerchant.makerFileName}" /> -->
		<s:hidden  name="otp" />
		<s:hidden name="loginType" value="userOtp" />
    </s:form>
	<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
	<script src="../js/login-script.js"></script>
</body>
</html>