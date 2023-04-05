<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Change Password</title>
	
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link rel="stylesheet" href="../css/login.css">
	<link rel="stylesheet" href="../fonts/css/font-awesome.css">
	<link rel="stylesheet" href="../css/new-common-style.css">

	<script src="../js/jquery.minshowpop.js"></script>
	<script src="../js/jquery.formshowpop.js"></script>
	<script src="../js/loader/modernizr-2.6.2.min.js"></script>

	<style type="text/css">
		.cardPopUp {
			top: 0;
			right: 0;
			width: 100%;
			height: 100%;
		}

		.card_box {
			max-width: 300px;
			display: none;
		}

		.card_box_icon {
			width: 50px;
			height: 50px;
			background-position: center center;
			background-repeat: no-repeat;
		}

		.errorchangepass{
			margin-bottom: 20px;
			display: block;
			color: #e60000;
		}

		.successMsg .card_box_icon {
			background-color: #5CB85C;
			background-image: url("../img/check.png");
		}
		.successMsg h3 { color: #5CB85C; }
		.successMsg ul { margin-bottom: 20px !important; }

		.failedMsg .card_box_icon {
			background-color: #E60000;
			background-image: url("../img/close.png");
		}
		.failedMsg h3 { color: #E60000; }
		.failedMsg ul { margin-bottom: 0 !important; }

		#submit{
			transition: all .5s ease;
		}

		#submit:hover{
			background-color: #e60000;
			border-color: #e60000;
		}

		.buttonDownLoad { display: none; }

		.invoice-popup {
			display: none;
			position: fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background-color: rgba(0,0,0,.7);
			z-index: 999;
		}

		.invoice-popup-wrapper {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		.invoice-popup-box {
			width: 100%;
			max-width: 330px;
			background-color: #fff;
			border-radius: 5px;
			padding: 20px;
			box-sizing: border-box;
			text-align: center;
			line-height: 22px;
			font-size: 18px;
		}

		.icon-box {
			display: flex;
			width: 50px;
			height: 50px;
			background-color: #4cbb17;
			text-align: center;
			color: #fff;
			border-radius: 50%;
			align-items: center;
			justify-content: center;
			font-size: 20px;
			margin-left: auto;
			margin-right: auto;
		}


		/* Manual css s:actionmessage for this page */
		.errorMessage {
			font: normal 11px arial;
			color: #ff0000;
			display: block;
			margin: -15px 0px 3px 0px;
			padding: 0px 0px 0px 0px;
		}

		.change-password-box{
			display: inline-block;
		}

		.change-ps-box{
			margin-bottom: 15px;
		}

		.d-none{
			display: none !important;  
		}

		.position-relative{
			position: relative !important;
		}

		.change-password-box input[type="text"]:read-only {
			pointer-events: none;
		}

		#errorchangepass {
			color: #e60000;
			display: block;
			margin-bottom: 15px;
		}

		.error2 {
			color: red;
			position: absolute;
			right: 0;
			top: -15px;
			display: none;
		}

		.submit-btn > div{
			text-align: center !important;
		}

		.show{
			display: block !important;
		}
	</style>
	<script>
		var loaderAction = function(actionName) {
			if(actionName == "show") {
				document.getElementsByTagName("body")[0].classList.remove("loader--inactive");
			} else if(actionName == "hide") {
				document.getElementsByTagName("body")[0].classList.add("loader--inactive");
			}
		}

		$(document).ready(function() {
			var _token = $("[name='token']").val();

			// verify old otp pin 
			$("#oldPin").on("change", function(e) {
				var _oldPin = $(this).val();
				var _parent = $(this).closest(".position-relative");
				$.ajax({
					type: "post",
					url: "verifyOldPin",
					data: {"oldPin": _oldPin, "token": _token, "struts.token.name": "token"},
					success:function(data) {
						if(data.response == "SUCCESS"){
							$(".change-ps-new-pin").removeClass("d-none");
							$(".new-pin-one").find("[data-id='pinBox1']").focus();
							$(".new-pin-one").find("[data-id='pinBox1']").attr("readonly", false);
							$("#erroroldpass").removeClass("show");
							_parent.find("input[data-id]").attr("readonly", true);
						}else{
							_parent.find("input[data-id='pinBox6']").attr("readonly", false);
							_parent.find("input[data-id='pinBox6']").focus();
							$("#erroroldpass").text(data.response);
							$("#erroroldpass").addClass("show");
						}						
					}
				})
			})

			// new pin change

			$(".old-pin").find("[data-id='pinBox1']").focus();

			$("#newPin").on("change", function(e) {
				$(".confirm-pin").find("[data-id='pinBox1']").focus();
				$(".confirm-pin").find("[data-id='pinBox1']").attr("readonly", false);
			});

			$("#changePassword").on("click", function(e) {
				loaderAction("show");
				window.location = "loginResult";
			});

			$(".otp-input-common").on("keyup", function(e) {
				if(e.keyCode == 08){
					$(this).prev().focus();
					console.log($(this).prev().length);
					if($(this).prev().length == 0){
						$(this).attr("readonly", false);
					}else{
						$(".otp-input-common").attr("readonly", true);
						$(this).prev().attr("readonly", false);
					}
				}
			});	
			
			$(".otp-input-common").attr("maxlength", 1);
			$(".otp-input-common").attr("onkeypress", "onlyDigit(event)");
			$(".otp-input-common").attr("readonly", true);
			$(".old-pin").find("[data-id='pinBox1']").attr("readonly", false);

			$(".otp-input-common").on("keyup keydown", function(e) {
				var code = e.keyCode || e.which;
				var _parent = $(this).closest(".position-relative");
				
				var otpInput1 = _parent.find("[data-id='pinBox1']").val();
				var otpInput2 = _parent.find("[data-id='pinBox2']").val();
				var otpInput3 = _parent.find("[data-id='pinBox3']").val();
				var otpInput4 = _parent.find("[data-id='pinBox4']").val();
				var otpInput5 = _parent.find("[data-id='pinBox5']").val();
				var otpInput6 = _parent.find("[data-id='pinBox6']").val();

				if(code != 9 && code != 08) {
					if($(this).val() != "") {
						$(this).next().focus();
						$(".otp-input-common").attr("readonly", true);
						$(this).next().attr("readonly", false);
					} else {
						$(this).focus();
					}
				} else {
					if(code == 08) {
					} else {
						$(this).focus();
						e.preventDefault();
					}
				}

				if(otpInput1 != "" && otpInput2 != "" && otpInput3 != "" && otpInput4 != "" && otpInput5 != "" && otpInput6 != "") {
					var getOtp = otpInput1 + otpInput2 + otpInput3 + otpInput4 + otpInput5 + otpInput6;
					_parent.find("input[type='hidden']").val(getOtp).trigger("change");
				} else {
					if($("#confirmnewPin").val("")){
						$("#errorconfirmpass").removeClass("show");
						$(".submit-btn").addClass("d-none");
					}
				}
			});		
			// confirm new pin

			$("#confirmnewPin").on("change", function(e) {
				var pin = $("#newPin").val();
				var confirmPin = $("#confirmnewPin").val();

				if(confirmPin.length == 6 && confirmPin != "") {
					if (pin == confirmPin) {
						$("#errorconfirmpass").removeClass('show');
						$(".submit-btn").removeClass("d-none");						
					} else {
						$("#errorconfirmpass").addClass('show');
						$(".submit-btn").addClass("d-none");
						$(".change-ps-new-pin").find("[data-id]").val("");
						$(".new-pin-one").find("[data-id='pinBox1']").attr("readonly", false);
						$(".new-pin-one").find("[data-id='pinBox1']").focus();
					}
				} else {
					$("#errorconfirmpass").removeClass('show');
					$(".submit-btn").addClass("d-none");
				}
			});

			$("#submit").click(function(e) {
				e.preventDefault();
				loaderAction("show");
				
				$.ajaxSetup({
					global: false,
					beforeSend: function () {
						toggleAjaxLoader();
					},
					complete: function () {
						toggleAjaxLoader();
					}
				});
		
				$.ajax({
					type: "POST",
					url : 'changePassword',
					timeout: 0,		
					data : {
						token : document.getElementsByName("token")[0].value,
						oldPin : document.getElementById('oldPin').value,
						newPin : document.getElementById('newPin').value,
						confirmnewPin : document.getElementById('confirmnewPin').value
					},
					success : function(data) {
						var responsedata = data.response;
						var jsonObj = data["Invalid request"];
						console.log(data);
						if(jsonObj != null) {
							var oldpass = jsonObj['oldPin'];
							var newpass = jsonObj['newPin'];
							var confirmpass = jsonObj['confirmnewPin'];
								
							if(oldpass != null) {
								document.getElementById("erroroldpass").innerHTML = oldpass;
							}
							if(newpass != null) {
								document.getElementById("errornewpass").innerHTML = newpass;
							}
							if(confirmpass != null) {
								document.getElementById("errorconfirmpass").innerHTML = confirmpass;
							}
						}
						if(responsedata == "PIN reset successfully, login to continue") {
							document.getElementById("errorchangepass").innerHTML = responsedata;
							// alert(responsedata)
							$("#popupMsg").text(responsedata);
							$(".invoice-popup").fadeIn();
						} else if(responsedata == "PIN mismatch") {
							document.getElementById("errorchangepass").innerHTML=responsedata;
						} else if(responsedata == "Use a PIN which has not been used 4 recent times by you"){
							document.getElementById("errorchangepass").innerHTML=responsedata;
							$(".otp-input-common").val("");
							$(".change-ps-new-pin").addClass("d-none");
							$(".signupbutton").addClass("d-none");
							$(".old-pin").find("[data-id='pinBox1']").attr("readonly", false);
							$(".old-pin").find("[data-id='pinBox1']").focus();
						}

						setTimeout(function() {
							loaderAction("hide");
						}, 1000);
					},
					error : function(data) {
						alert('Something went wrong!');

						setTimeout(function() {
							loaderAction("hide");
						}, 1000);
					}
				});
			});	
		});

		function onlyDigit(event) {
			var x = event.keyCode;
			if (x > 47 && x < 58 || x == 32) {
			} else {
				event.preventDefault();
			}
		}
	</script>
</head>
<body>
	<div class="row">
		<div class="col-md-6">
			<section class="change-password lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Change Password</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->

					<div class="col-md-12">
						<s:actionmessage class="success success-text" theme="simple"/>
					</div>
					<!-- /.col-md-12 -->

					<div class="col-md-12">
						<div class="change-password-box">
							<span id="errorchangepass"></span>
							<div class="change-ps-box old-pin">
								Old PIN <br>
								<div class="txtnew">
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
											name="oldPin"
											maxlength="6"
											id="oldPin"
											type="hidden"
											autocomplete="off"
											cssClass="form-control"
										/>
										<span  class="error2" id="erroroldpass"></span>
									</div>
									<!-- /.positon-relative -->
								</div>
							</div>

							<div class="change-ps-box change-ps-new-pin d-none new-pin-one">New PIN<br>
								<div class="txtnew">
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
											name="newPin"
											id="newPin"
											maxlength="6"
											type="hidden"
											autocomplete="off"
											cssClass="form-control"
										/>
										<span class="error2 d-none" id="errornewpass"></span>
									</div>
									<!-- /.position-relative -->
								</div>
							</div>

							<div class="change-ps-box change-ps-new-pin confirm-pin d-none">Confirm New PIN<br>
								<div class="txtnew">
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
											name="confirmnewPin"
											id="confirmnewPin"
											maxlength="6"
											type="hidden"
											autocomplete="off"
											cssClass="form-control"
										/>
										<span class="error2" id="errorconfirmpass">PIN doesn't match</span>
									</div>
									<!-- /.position-relative -->
								</div>
							</div>

							<div class="adduTR submit-btn d-none" style="text-align:center; padding:14px 0 0 0;">
								<s:submit id="submit" value="Submit" cssClass="signupbutton lpay_button font-weight-medium lpay_button-md lpay_button-secondary"></s:submit>
							</div>
						</div>

						<div class="disclaimer mt-10">
							<em class="font-size-12"><strong>PIN Criteria:</strong> PIN must be 6 Number. Your new PIN must not be the same as any of your last four PIN.</em>
						</div>
						<!-- /.disclaimer -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
			<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		</div>
		<!-- /.col-md-6 -->
	</div>
	<!-- /.row -->

	<div class="invoice-popup">
		<div class="invoice-popup-wrapper">
			<div class="invoice-popup-box" tabindex="-1">
				<div class="icon-box">
					<i class="fa fa-check" aria-hidden="true"></i>
				</div>
				<!-- /.icon-box -->
				<p id="popupMsg" class="mb-20">Password changed successfully, login to continue</p>
				<button id="changePassword" class="lpay_button lpay_button-md lpay_button-primary">Ok</button>
			</div>
			<!-- /.invoice-popup-box -->
		</div>
		<!-- /.invoice-popup-wrapper -->
	</div>
	<!-- /.invoice-popup -->
</body>
</html>