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

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,900&display=swap">
	<link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/login.css">
	<link rel="stylesheet" href="../css/loader-animation.css">
</head>
<body>
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
				<header class="logo mt-20 mt-md-40 ml-md-25"><h1><img src="../images/logo.png" alt=""></h1></header>
				<!-- /.logo mt-40 ml-40 -->
				<section class="d-flex justify-content-center login-box">
					<div class="login-box-inner">
						<h2 class="color-grey-dark font-size-32 font-weight-bold line-height-40">Welcome back,</h2>
						<p class="color-grey-light">Login to access your account.</p>
						
						<s:form action="loginpos" method="post" id="login-form" onselectstart="return false" oncontextmenu="return false;">
							<input type="hidden" name="loginType" id="loginType" value="pwd"/>
							<s:token />

							<p id="dataValue" class="m-0 font-size-12 transition-all">&nbsp;</p>

							<s:actionmessage />

							<div class="form-group">
								<label for="emailId" class="font-size-12 color-grey-light m-0">Email Id</label>
								<div class="position-relative">
									<s:textfield type="text" name="emailId" id="emailId" class="form-control" onKeyDown="if(event.keyCode === 32)return false;" />
									<p class="error" id="errorEmail"></p>
								</div>
							</div>               
							<!-- /.form-group -->

							<div class="form-group mt-3 mb-20">
								<div class="alt-login-box active" id="using-password">
									<label for="password" class="font-size-12 color-grey-light m-0">Password</label>
									<div class="position-relative">
										<s:textfield type="password" name="password" id="login-password" class="form-control" maxlength="32" />
										<div class="alt-login-option position-absolute d-flex align-items-center mr-10">
											<a href="#using-otp" id="generateOtpBtn" class="color-primary font-size-14 font-weight-bold">Use OTP instead</a>
										</div>
										<p class="error" id="errorPassword"></p>
									</div>
								</div>

								<div class="alt-login-box" id="using-otp">
									<label for="password" class="font-size-12 color-grey-light m-0">Enter OTP</label>
									<div class="position-relative">
										<s:textfield type="text" name="otp" id="otp" class="form-control" />
										<div class="alt-login-option position-absolute d-flex align-items-center mr-10">
											<a href="#using-password" class="color-primary font-size-14 font-weight-bold color-primary">Use Password</a>
											<!-- <a href="#resend-otp" class="color-primary font-size-14 font-weight-bold">Resend OTP</a> -->
										</div>
										<p class="error" id="otp-msg"></p>
									</div>
									<!-- <p class="font-size-12 color-grey-light mt-8"></p> -->
								</div>
							</div>
							<!-- /.form-group -->

							<input type="hidden" value="pwd" name="data"/>
								
							<div class="form-group p-10 bg-color-primary-lightest border-radius-6">
								<div class="d-flex">
									<div class="position-relative full-width mr-20">
										<s:textfield type="text" class="form-control" autocomplete="off" name="captcha" placeholder="Enter CAPTCHA" id="captcha" maxlength="6" />
										<p class="error" id="errorCaptcha">Please enter valid captcha</p>
									</div>
									<div class="d-flex align-items-center">
										<!-- <div id="txtCaptcha" class="txtCaptcha full-width text-center"></div> -->
										<div>
											<img id="captchaImage" src="../Captcha.jpg"/>
										</div>
	
										<div class="d-flex align-items-center ml-5">
											<a href="#" id="regenrate-captcha"><img src="../images/icon-refresh.png" alt=""></a>
										</div>
									</div>
								</div>
								<!-- /.d-flex -->
							</div>
							
							<!-- /.form-group -->

							<s:param>userId</s:param>
							<s:param>password</s:param>

							<s:submit key="submit" class="btn btn-primary w-100 py-10" value="Login"></s:submit>

							<div class="text-center mt-20">
								<s:a action="forgetPassword" class="font-size-14 color-primary font-weight-bold">Forgot Password?</s:a>
							</div>
						</s:form>
					</div>
					<!-- /.login-box -->
				</section>
			</div>
			<!-- /.col-md-8 -->

			<div class="col-lg-4 signup-box d-flex align-items-center justify-content-center mt-30 mt-lg-0 sticky">
				<div class="signup-box-inner py-30 py-lg-0 pl-lg-30 pr-lg-30 pl-xl-35 pr-xl-35">
					<h2 class="signup-box-title text-white font-weight-light">Don't have an account yet?</h2>
					<p class="signup-box-desc text-white font-weight-light mt-sm-5 mt-lg-20 mt-xl-30">Create a new account now.</p>
					<s:a action="merchantSignup" class="btn btn-primary btn-white font-weight-medium mt-15 mt-lg-20 mt-xl-30 py-10 py-lg-15">Sign Up</s:a>
				</div>
			</div>
			<!-- /.col-md-4 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.container-fluid -->

	<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
	<script src="../js/login-script.js"></script>
</body>
</html>