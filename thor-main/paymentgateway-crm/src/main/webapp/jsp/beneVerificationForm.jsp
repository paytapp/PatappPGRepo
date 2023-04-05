<!DOCTYPE html >
<%@taglib prefix="s" uri="/struts-tags"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Account Verification</title>
	<!-- <script src="../js/user-script.js"></script> -->

	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="shortcut icon" href="../image/favicon-32x32.png" type="image/x-icon">
	<link href="../css/bootstrap.min.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/loader-animation.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<link href="../css/welcomePage.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/styles.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">

	<style>
		.container-verify-link {
			max-width: 500px !important;
			padding: 30px !important;
			box-shadow: 0 2px 2px 0 rgb(153 153 153 / 14%), 0 3px 1px -2px rgb(153 153 153 / 20%), 0 1px 5px 0 rgb(153 153 153 / 12%);
			border: 1px solid rgba(204, 204, 204, 0.4) !important;
			border-radius: 8px !important;
		}
		.text-red { color: #f00; }

		.error-msg {
			position: absolute;
			left: 0;
			bottom: -15px;
			color: #f00;
		}

		#verifyBeneForm { padding-left: 15px; padding-right: 15px; }
	</style>
</head>
<!-- /.edit-permission -->

<body class="bodyColor">
	<s:form action="verifyBeneAction" id="verifyBeneForm" method="POST" autocomplete="off">
		<s:hidden id="merchantPayId" name= "merchantPayId" value="%{merchantPayId}"></s:hidden>
		<s:hidden id="subMerchantId" name= "subMerchantId" value="%{subMerchantId}"></s:hidden>
		
		<div class="container pt-20 pb-20 container-verify-link bg-color-white">
			<div class="row">
				<div class="col-xs-12 text-center mb-30">
					<img src="../images/logo-response.png" alt="">
				</div>

				<div class="col-xs-12 text-center mb-30">
					<h3 class="font-weight-bold">Verify your Bank Account</h3>
				</div>
				<!-- /.col-md-12 -->

				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="phoneNo">Mobile Number <span class="text-red">*</span></label>
						<input type="text" name="benePhone" maxlength="10" id="phoneNo" class="lpay_input lpay-input" oninput="onlyNumberInput(this);">
						<span class="error-msg" data-error="phoneNo"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="bankAccountName">Account Holder Name <span class="text-red">*</span></label>
						<input type="text" name="beneName" id="bankAccountName" class="lpay_input lpay-input" oninput="onlyAlpha(this);">
						<span class="error-msg" data-error="bankAccountName"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="bankAccountNumber">Account Number <span class="text-red">*</span></label>
						<input type="text" maxlength="20" name="beneAccountNumber" id="bankAccountNumber" class="lpay_input lpay-input" oninput="onlyNumberInput(this);">
						<span class="error-msg" data-error="bankAccountNumber"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
	
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="bankIfsc">Bank IFSC Code <span class="text-red">*</span></label>
						<input type="text" maxlength="11" name="beneIfsc" id="bankIfsc" class="lpay_input lpay-input" oninput="onlyAlphaNumeric(this); _uppercase(this)">
						<span class="error-msg" data-error="bankIfsc"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
	
				<div class="col-md-12 text-center">
					<button id="submit" class="lpay_button lpay_button-md lpay_button-secondary mt-15">Verify</button>
				</div>
				<!-- /.col-md-12 -->
			</div>
		</div>
		<!-- /.row -->
	</s:form>
	<!-- /.lpay_popup -->

	<script src="../js/jquery.min.js"></script>	
	<script src="../js/beneVerificationForm.js"></script>
</body>
</html>