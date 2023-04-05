<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Logout</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/common-style.css">
<link rel="stylesheet" href="../css/login.css">
<link rel="stylesheet" href="../css/loader-animation.css">
<link rel="stylesheet" href="../css/styles.css">
<script>
	if (self == top) {
		var theBody = document.getElementsByTagName('body')[0];
		theBody.style.display = "block";
	} else {
		top.location = self.location;
	}
</script>
<style>
	.login-box-inner h3{
		font-size: 22px !important;
		margin-bottom: 15px;
		color: #333;
	}

	.login-box-inner h4{
		color: #333;
	}

	.login-box-inner h4 a{
		color: #1cc0d1;
	}

	.login-wrapper .login-box-inner{
		padding: 30px 20px;
	}
</style>
</head>
<body>
	<section class="login-wrapper">
		<header class="login-header">
			<div class="login-header-logo d-flex justify-content-center logo">
				<img src="../image/white-logo.png" alt="Pg" class="img-fluid">
			</div>
			<!-- /.login-header-logo -->
			<s:a action="merchantSignup" class="lpay_button fs-14">Register Yourself</s:a>
			<!-- <button class="lpay_button lpay_button-md lpay_button-primary">Make Account</button> -->
		</header>
		<!-- /.login-header -->
		<main class="login-form">
			<div class="login-box-inner text-center">
				<span class="clip-shape"></span>
				<!-- /.clip-shape -->
				<h3>You have successfully logged out!</h3>
				<h4>Click here to <s:a action="index"><span class='bluelinkbig'>login</span></s:a></h4>
			</div>
		</main>
		<div class="footer-logo">
			<small>Powered by</small>
          <span class="footer-logo_text">Paytapp</span> 
		</div>
		<!-- /.footer-logo -->
	</section>
</body>
</html>