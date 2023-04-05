<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>

<head>
    <title>Upi AutoPay Mandate Registration</title>
    <meta name="viewport" content="width=device-width" />
    <link rel="stylesheet" href="../css/bootstrap.min.css">
    <link rel="stylesheet" href="../fonts/css/font-awesome.css">
    <link rel="stylesheet" href="../css/styles.css">
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="https://www.paynimo.com/paynimocheckout/client/lib/jquery.min.js" type="text/javascript"></script>
    <script src="../js/bootstrap.min.js"></script>
    <script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <link rel="stylesheet" href="../css/loader-animation.css">
	<style>

		.merchant-logo img {
			max-width: 60% !important;
		}

		.lpay_popup{
			background-color: #eee;
			z-index: 9999;
			box-shadow: 0 0 20px rgba(0,0,0,.1);
			display: flex;
		}

		body{
			overflow-x: hidden;
		}

		.lpay_footer{
			padding: 15px 0;
			left: 0;
			right: 0;
			position: fixed !important;
			z-index: 999;
		}
		input:read-only{
			background-color: #ddd !important;
		}
		.lpay_footer .row{
			margin: 0;
			display: flex;
			align-items: center;
		}
		.hide-input{
			display: none;
		}
		.mpaHeader{
			padding: 10px 10px;
			background-color: #041020;
		}
		.mpaLogo{
			display: flex;
			align-items: center;
			justify-content: space-between;
		}
		.mpaLogo img{
			max-width: 100%;
			max-height: 70px;
		}
		.error_field {
			position: absolute;
			top: -0px;
			padding: 5px;
			opacity: 0;
			z-index: -1;
			width: 100%;
			background-color: #fff;
			border-radius: 4px;
			border: 1px solid #ddd;
			color: #f00;
			transition: all .5s ease;
		}
		.error_field:after {
			content: "";
			width: 12px;
			height: 12px;
			background-color: #fff;
			position: absolute;

			bottom: -6px;
			right: 20px;
			z-index: 99;
			border: 1px solid #ddd;
			transform: rotate(45deg);
			border-left-color: transparent;
			border-top-color: transparent;
		}

		.has-error .error_field{
			opacity: 1;
			z-index: 1;
			top: -20px;
			transition: all .5s ease;
		}

		.mopType-div {
			position: absolute;
			right: 3px;
			bottom: 3px;
			z-index: 999;
        }
        
        #mopTypeImg{
            width: 36px;
        }

		.eNach-div{
			width: 100%;
			max-width: 1170px;
			margin: auto;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		.heading_with_icon .heading_text{
			margin-left: 0;
		}

		.ribbon-heading{
			background-size: 200% auto;
			background-image: linear-gradient(to right, #050c16 0%, #002663 51%, #050c16 100%);
			color: #fff;
			padding: 10px;
			position: relative;
		}

		.ribbon-heading .heading_text{
			color: #fff;
			padding-bottom: 0;
			width: 100%;
			font-size: 18px !important;
		}

		

		.ribbon-heading::after {
			content: "";
			width: 20px;
			height: 20px;
			position: absolute;
			top: 33px;
			left: 4px;
			background-color: #041228;
			z-index: -1;
			transform: rotate(61deg);
		}

		.ribbon-heading::before {
			content: "";
			width: 20px;
			height: 20px;
			position: absolute;
			top: 33px;
			right: 4px;
			background-color: #041228;
			z-index: -1;
			transform: rotate(-61deg);
		}

		.disclaimer-text {
			display: inline-block;
			padding: 6px 15px;
			border-radius: 5px;
			font-size: 12px;
		}

		.footer_logo span{ font-weight: 600; }
		.footer_logo img{ margin-left: 5px; }
		#logo{ max-height: 70px; }	

	</style>
</head>

<body>

	<s:if test="%{aaData.RESPONSE != 'success'}">
	<div class="lpay_popup">
		<div class="lpay_popup-inner">
			<div class="lpay_popup-innerbox" data-status="error">
	
				<div class="lpay_popup-innerbox-error lpay-center">
					<div class="lpay_popup_icon">
						<img src="../image/wrong-tick.png" alt="">
					</div>
					<!-- /.lpay_popup_icon -->
					<s:if test="%{aaData.RESPONSE == 'INVALID REQUEST'}">
					<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Request</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="hashFail" id="confirmButton">OK</button>
					</div>
					</s:if>
					<s:elseif test="%{aaData.RESPONSE == 'Duplicate Order ID'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Duplicate Request</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="duplicateOrderId" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<!-- <s:else>
					<div class="lpay_popup-content">
						<h3 class="responseMsg">Duplicate Request</h3>
					</div>
					
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="duplicateOrderId" id="confirmButton">OK</button>
					</div>
					</s:else> -->
					<!-- /.lpay_popup-button -->
				</div>
				<!-- /.lpay_popup-innerbox-success -->
			</div>
			<!-- /.lpay_popup-innerbox -->
		</div>
		<!-- /.lpay_popup-inner -->
	</div>
	<!-- /.lpay_popup -->
</s:if>

<s:hidden value="%{aaData.RESPONSE}" id="repon"></s:hidden>
    <div class="loader-container w-100 vh-100 lpay-center">
        <div class="loaderImage">
            <img src="../image/loader.gif" alt="Loader">
        </div>
    </div>
    <!-- <s:hidden value="%{aaData.CUSTOMER_MOBILE}" data-var="payId"></s:hidden>
    <s:hidden value="%{netBanking}" data-var="netBanking"></s:hidden> -->

	
	<div class="eNach-div">
		<section class="eNach lapy_section white-bg box-shadow-box mt-60 p20">
			<div class="row d-flex mb-20" style="align-items: center;">
				<div class="col-md-3"> 
					<img src="../image/Pg1.png" id="logo"  alt="Pg">
				</div>
				<!-- /.col-md-8 -->
				<div class="col-md-6 text-center">
					<div class="disclaimer-text">
						Please do not refresh the page or press back button
					</div>
					<!-- /.disclaimer-text -->
				</div>
				<!-- /.col-md-6 -->
				<div class="col-md-3 text-right">
					<span style="font-size: 18px;"><s:property value="%{aaData.MERCHANT_NAME}"></s:property></span>
				</div>
				<!-- /.col-md-4 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="row" style="margin: 0 -10px;">
					<div class="col-md-12 ribbon-heading mb-20">
						<div class="heading_with_icon text-center ">
							<!-- <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span> -->
							<h1 class="heading_text">UPI AutoPay Mandate Registration </h1>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
		<!-- /.col-md-3 -->
		
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Payer VPA <span class="imp">*</span></label>
			  <s:textfield type="text" data-var="payerVPA" id="payerVpa" name="payerVPA"
					  class="lpay_input" autocomplete="off"  />
					  <div class="error_field">
						Payer VPA should not blank
					</div>
					<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		</div>
		<!-- /.col-md-4 -->

		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Note <span class="imp">*</span></label>
			  <s:textfield type="text" data-var="note" id="note" name="note"
					  class="lpay_input" autocomplete="off"  />
					  <div class="error_field">
						Payer VPA should not blank
					</div>
					<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		</div>
		<!-- /.col-md-4 -->

		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Mobile Number</label>
			  <s:textfield type="text" data-var="custMobile" id="consumerMobileNo" name="custMobile"
					  class="lpay_input" maxlength="10" onkeypress="onlyDigit(event)" autocomplete="off" value="%{aaData.CUST_MOBILE}" readonly="true" />
					  <div class="error_field">
						Mobile number should not blank
					</div>
					<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		  </div>
		  <!-- /.col-md-4 -->
		  <div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Email Id</label>
				<s:textfield type="text" data-var="custEmail" id="consumerEmailId" name="custEmail"
					  class="lpay_input" autocomplete="off" value="%{aaData.CUST_EMAIL}" readonly="true" />
					  <div class="error_field">
						Email ID should not blank
					</div>
					<!-- /.error_field -->
			  </div>
			  <!-- /.lpay_input_group -->
		  </div>
		  <!-- /.col-md-4 -->
		
		
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Registration Amount</label>
			  <s:textfield type="text" data-var="amount" id="amount" name="amount"
					class="lpay_input" onkeypress="onlyDigit(event)" autocomplete="off" value="%{aaData.AMOUNT}" readonly="true" />
					<div class="error_field">
						Amount should not blank
					</div>
					<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		</div>
		<!-- /.col-md-4 -->
	
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
				<label for=""><s:if test="%{aaData.FREQUENCY == 'As And When Presented'}">ADHO -</s:if><s:else><s:property value="%{aaData.FREQUENCY}" /></s:else> Debit Amount</label>
			  <s:textfield type="text" data-var="monthlyAmount" id="monthlyAmount" name="monthlyAmount"
					class="lpay_input" onkeypress="onlyDigit(event)" autocomplete="off" value="%{aaData.MONTHLY_AMOUNT}" readonly="true" />
					<div class="error_field">
						Max Amount should not blank
					</div>
			</div>
		</div>
		<!-- /.col-md-4 -->
	
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Total Amount</label>
			  <s:textfield type="text" data-var="totalAmount" id="totalAmount" name="totalAmount"
					class="lpay_input" onkeypress="onlyDigit(event)" autocomplete="off" value="%{aaData.TOTAL_AMOUNT}" readonly="true" />
					<div class="error_field">
						Total Amount should not blank
					</div>
					<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		</div>
		<!-- /.col-md-4 -->
	
	
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <label for="">Frequency</label>
			  <s:textfield type="text" data-var="frequency" id="frequency" name="frequency"
					class="lpay_input" autocomplete="off" value="%{aaData.FREQUENCY}" readonly="true" />
			</div>
		</div>
	
		 <!-- /.col-md-4 -->
		 <div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Tenure</label>
			  <s:textfield type="text" data-var="tenure" id="tenure" name="tenure"
					class="lpay_input" autocomplete="off" value="%{aaData.TENURE}" readonly="true" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Debit Start Date</label>
			  <s:textfield type="text" data-var="startDate" id="startDate" name="startDate"
					class="lpay_input" autocomplete="off" value="%{aaData.DEBIT_START_DATE}" readonly="true" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Debit End Date</label>
			  <s:textfield type="text" data-var="endDate" id="endDate" name="endDate"
					class="lpay_input" autocomplete="off" value="%{aaData.DEBIT_END_DATE}" readonly="true" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
	
		<!-- /.col-md-4 -->
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" data-var="payId" value="%{aaData.PAY_ID}" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<!-- /.col-md-4 -->
		<s:if test="%{aaData.SUB_MERCHANT_ID != null && aaData.SUB_MERCHANT_ID != ''}">
			<div class="col-md-3 mb-20 hide-input">
				<div class="lpay_input_group">
				<s:textfield type="text" data-var="subMerchantPayId" value="%{aaData.SUB_MERCHANT_ID}" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
		</s:if>
		<!-- /.col-md-4 -->
		
		<s:if test="%{aaData.LOGO != null && aaData.LOGO != ''}">
			<div class="col-md-3 mb-20 hide-input">
				<div class="lpay_input_group">
				  <s:textfield type="text" data-var="merchantLogo" value="%{aaData.LOGO}" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
		</s:if>
		
		
		<!-- <div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="returnUrl" data-var="returnUrl" value="%{aaData.RETURN_URL}" />
			</div>
		</div> -->
	
		<!-- /.col-md-4 -->
		
	<!-- <div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="returnUrl" data-var="returnUrl" value="%{aaData.MERCHANT_RETURN_URL}" />
			</div>
		</div>-->
	
		<!-- /.col-md-4 -->
		
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="consumerId" data-var="orderId" value="%{aaData.ORDER_ID}" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="purpose" data-var="purpose" value="%{aaData.PURPOSE}" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
	
		<!-- /.col-md-4 -->
		
		<!-- <s:hidden value="%{aaData.payId}" data-var="payId"></s:hidden> -->
		<div class="col-md-12 text-center">
			<button class="lpay_button lpay_button-md lpay_button-primary" id="btnSubmit">Submit</button>
			<a href="" id="cancelButton" class="lpay_button lpay_button-md lpay_button-secondary d-none">Cancel</a>
			<button class="lpay_button lpay_button-md lpay_button-secondary cnclButton" data-text="cancel">Cancel</button>
		</div>
		<!-- /.col-md-12 -->

		<div class="col-md-12 text-right mt-15">
			<div class="footer_logo text-right">
				<span>Powered By</span>
				<img src="../image/upi-autopay-logo.png" alt="/">
				<img src="../image/NACH-Logo.png" alt="/">
			</div>
			<!-- /.footer_logo -->
		</div>
		<!-- /.footer_logo -->
	</div>
	<s:hidden name="token" id="customToken" value="%{#session.customToken}" />
	<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	</div>
	<!-- /.eNach-div -->
<s:hidden id="logoUrl" value="%{aaData.LOGO}"></s:hidden>

<s:form action="upiAutoPayResponse" method="POST" id="cancelForm">
	<s:hidden name="message" id="msg"></s:hidden>
	<s:if test="%{aaData.RESPONSE == 'INVALID REQUEST'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:if>
	<s:elseif test="%{aaData.RESPONSE == 'Duplicate order Id'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
</s:form>

<s:form id="upiAutoPay" action="upiAutoPayResponse">
	<s:hidden name="orderId" id="orderId"></s:hidden>
</s:form>


	<script type="text/javascript">
	
		function onlyLetters(event) {
			var x = event.keyCode;
			if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
				
			} else {
				event.preventDefault();
			}
		}

		function onlyDigit(event){
			var x = event.keyCode;
			if (x > 47 && x < 58 || x == 32) {
			} else {
				event.preventDefault();
			}
		}


		// letters and alpabet
		function lettersAndAlphabet(event) {
			var x = event.keyCode;
			if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
			} else {
				event.preventDefault();
			}
		}

		function checkLength(e){
			var _getMax = Number(e.getAttribute("maxLength"));
			var _getLabel = e.closest("div").querySelector("label").innerText;
			if(e.value != ""){
				if(e.value.length != _getMax){
					e.closest(".col-md-3").classList.add("has-error");
					e.closest(".col-md-3").querySelector(".error_field").innerText = 'Invalid '+_getLabel;
				}
			}else{
				e.closest(".col-md-3").classList.remove("has-error");
			}
		}

		function checkRegEx(e){
			var _getRegEx = e.getAttribute("data-regex");
			var _newRegEx = new RegExp(_getRegEx);
			var _value = e.value;
			if(_value != ""){
				if(_newRegEx.test(_value) != true){
					var _getLabel = e.closest(".col-md-3").querySelector("label").innerText;
					e.closest(".col-md-3").querySelector(".error_field").innerText = "Invalid "+_getLabel;
					e.closest(".col-md-3").classList.add("has-error");
				}
			}else{
				e.closest(".col-md-3").classList.remove("has-error");
			}
		}

		


        $(document).ready(function() {

			var _getHeaderHeight = $(".mpaHeader").outerHeight();
			var _getFooter = $(".lpay_footer").outerHeight();
			var _windowHeight = $(window).height();
			/* console.log("header height" + _getHeaderHeight);
			console.log("footer height" + _getFooter);
			console.log(_windowHeight); */
			var _getExactHeight = _windowHeight - _getFooter - _getHeaderHeight;
			/* console.log(_getExactHeight); */
			$(".eNach-div").css("min-height", _getExactHeight);
			$("#ifsc").on("change", function(e){
				var _valueLength = $(this).val().length;
				if(_valueLength == 11){
					$(this).closest(".col-md-3").removeClass("has-error");
				}else{
					$(this).closest(".col-md-3").addClass("has-error");
					$(this).closest(".col-md-3").find(".error_field").text("Invalid IFSC");
				}
			})

			$("body").addClass("loader--inactive");

			$(".cnclButton").on("click", function(e){
				$("#msg").val($(this).attr("data-text"));
				// return false;
				if($(this).attr("data-text") == "cancel"){
					var _getAllInput = document.querySelectorAll("[data-var]");
					var _option = "";
					if(_getAllInput.length != undefined){
						_getAllInput.forEach(function(index, array, element){
							var _new =  index.closest(".col-md-3").classList;
							var _newVal =  _new.toString().indexOf("d-none");
							if(_newVal == -1 && index.value != ""){
								_option += "<input name='"+index.getAttribute('data-var')+"' value='"+index.value+"'>";
							}
						})
					}
					document.querySelector("#cancelForm").innerHTML += _option;
				}
				$("#cancelForm").submit();
			})

	

            if($("#logoUrl").val() == ""){
                $("#logo").attr("src", "../image/Pg1.png");
            }else{
                $("#logo").attr("src", $("#logoUrl").val());
            }

			var _getAllSelect = document.querySelectorAll("select[data-var]");
			_getAllSelect.forEach(function(index, element, array){
				_getAllSelect[element].addEventListener('change', function(e){
					e.target.closest(".col-md-3").classList.remove("has-error");
				})
			})

			// create function for remove value 
			var _getAllInput = document.querySelectorAll(".lpay_input");
			_getAllInput.forEach(function(index, element, array){
				_getAllInput[element].addEventListener('keyup', function(e){
					e.target.closest(".col-md-3").classList.remove("has-error");
				})
				if(_getAllInput[element].value == ""){
					_getAllInput[element].removeAttribute("readonly");
				}
			})

            $(document).off('click', '#btnSubmit').on('click', '#btnSubmit', function(e) {
                e.preventDefault();
				var _checkError = true;
				var consumerData = {};
				var _getError = document.querySelectorAll(".has-error");
                var _getInput = document.querySelectorAll("[data-var]");
                _getInput.forEach(function(index, element, array){
                    var _new =  _getInput[element].closest(".col-md-3").classList;
					if(_getInput[element].closest(".col-md-3").querySelector("label") !== null){
						var _getLabel = _getInput[element].closest(".col-md-3").querySelector("label").innerText;
					}
                    var _newVal = _new.toString().indexOf("d-none");
                    if(_newVal == -1){
						if(_getInput[element].value == ""){
							_getInput[element].closest(".col-md-3").classList.add("has-error");
							if(_getInput[element].closest(".col-md-3").querySelector(".error_field") !== null){
								_getInput[element].closest(".col-md-3").querySelector(".error_field").innerText = _getLabel + " Should Not Blank";
							}
							_checkError = false;
						}else{
							var _get = index.getAttribute("data-var");
							consumerData[_get] = index.value;
						}
					}
                })
               	//return false;
				if(_checkError == true) {
					document.querySelector("body").classList.remove("loader--inactive");
					$.ajax({
						type: "POST",
						url: "getUpiAutoPayToken",
						data: consumerData,
						success: function(data){
							document.querySelector("#orderId").value = data.aaData.ORDER_ID;
							document.querySelector("#upiAutoPay").submit();
							setTimeout(function(e){
								document.querySelector("body").classList.add("loader--inactive");
							}, 1000)
						}
					})
				}
            });
        });
    </script>
</body>

</html>
