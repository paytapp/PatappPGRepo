<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Thank You</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/login.css">
<link rel="stylesheet" href="../css/styles.css">
<style type="text/css">
.aeroplane{
  position: absolute;
  bottom: 0;
  right: 0;
  width: 400px;
  z-index: -1;
}
.train{
  position: absolute;
  bottom: 0;
  left: 0;
  width: 406px;
  z-index: -1;
}
.trackBox {
    width: 871px;
   
    border: 1px solid #ccc;
    margin: 4% auto 0;
    text-align: center;
    border-radius: 10px;
    background-color: #fff;
}
.trackBox img{
  display: block;
  margin: 0 auto;
}
.trackBox img.payLogo{
  padding-top: 47px;
  margin-bottom: 18px;
}
.trackBox h1{
  font-size: 40px;
  font-weight: 800;
  margin-bottom: 0;
  letter-spacing: 3px;
}
.trackBox h2{
  font-size: 25px;
  color: #454545;
  font-weight: 800;
  margin-top: 8px;
  letter-spacing: 3px;
}
.trackBox p{
      font-size: 15px;
    color: #979797;
    font-weight: 600;
}
.trackBox p a{
  color: #002163;
  text-decoration: none;
}
.trackBox p a:hover{
  opacity: 0.8;
}

.mb-20{
  margin-bottom: 20px !important;
}

</style>
<script>
  if (self == top) {
    var theBody = document.getElementsByTagName('body')[0];
    theBody.style.display = "block";
  } else {
    top.location = self.location;
  }
</script>

</head>
<body>


  <section class="login-wrapper">
		<header class="login-header">
			<div class="login-header-logo d-flex justify-content-center logo">
				<img src="../image/white-logo.png" alt="PG" class="img-fluid">
			</div>
			<!-- /.login-header-logo -->
			<s:a action="merchantSignup" class="lpay_button lpay_button-md">Register Yourself</s:a>
			<!-- <button class="lpay_button lpay_button-md lpay_button-primary">Make Account</button> -->
		</header>
    <!-- /.login-header -->
    <main class="login-form">
      <div class="trackBox p20">
        <!-- <img class="payLogo" src="../image/lpay2.png"/> -->
        <img src="../image/ok.png" class="mb-20" />
        <h1 class="mb-10">THANK YOU !</h1>
        <h2 class="mb-20">FOR REGISTERING WITH PAYMENT GATEWAY</h2>
        <p>To activate your account, please click on the link in activation email which has been sent to you.</p>
        <p>If you have not received the activation email, contact us at <a href="mailto:support@paymentgateway.com">support@paymentgateway.com</a></p>
        <div class="login-action-btn" style="padding-top: 10px;width: 100%">
          <s:a action="login" class="">Login</s:a>
          <!-- <button class="action-btn taget-div" data-target="login-pin">login by PIN</button>
          <button class="action-btn taget-div" id="loginOtp" data-target="login-otp">Login by OTP</button> -->
        </div>
      </div>
    </main>
    <!-- /.login-form -->
  </section>
</body>
</html>