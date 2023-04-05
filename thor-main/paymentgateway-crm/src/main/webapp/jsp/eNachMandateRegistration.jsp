<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>

<head>
    <title>E-Mandate Registration</title>
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
					<s:elseif test="%{aaData.RESPONSE == 'Duplicate order Id'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Duplicate Request</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="duplicateOrderId" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					
					<s:elseif test="%{aaData.RESPONSE == 'Invalid End Date'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid End Date</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidEndDate" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Start Date'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Start Date</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidStartDate" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Amount'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Amount</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidAmount" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Monthly Amount'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Monthly Amount</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidMonthlyAmount" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Frequency'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Frequency</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidFrequency" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Tenure'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Tenure</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidTenure" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Merchant ID'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Merchant ID</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidMerchantID" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Customer Mobile'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Customer Mobile</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidCustomerMobile" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Customer Email'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Customer Email</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidCustomerEmail" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					<s:elseif test="%{aaData.RESPONSE == 'Invalid Request ID'}">
						<div class="lpay_popup-content">
						<h3 class="responseMsg">Invalid Request ID</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="invalidRequestID" id="confirmButton">OK</button>
					</div>
					</s:elseif>
					
				
					<%-- <s:else>
					<div class="lpay_popup-content">
						<h3 class="responseMsg">Duplicate Request</h3>
					</div>
					<!-- /.lpay_popup-content -->
					<div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="duplicateOrderId" id="confirmButton">OK</button>
					</div>
					</s:else> --%>
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
    
	<div class="eNach-div">
		<section class="eNach lapy_section white-bg box-shadow-box mt-60 p20">
			<div class="row d-flex mb-20" style="align-items: center;">
				<div class="col-md-3"> 
					<img src="../image/Pg1.png" id="logo" alt="Pg">
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
							<h1 class="heading_text">eNACH Registration</h1>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Registration Mode</label>
					   <select data-var="paymentMode" name="paymentMode" class="selectpicker" id="paymentMode">
							<option value="">Select Mode</option>
							<!-- <option value="netBanking">Net Banking</option>
							<option value="cards">Debit Card</option> -->
							<option value="netBanking">Net Banking</option>
							<option value="cards">Debit Card</option>
	
					   </select>
					   <div class="error_field">
							Please select registration mode
						</div>
						<!-- /.error_field -->
					</div>
					<!-- /.lpay_select_group -->  
				</div>
		<!-- /.col-md-3 -->
		<div class="clearfix"></div>
		<!-- /.clearfix -->
		<div class="col-md-3 mb-20 d-none" data-id="netBanking">
			<div class="lpay_select_group">
			  <label for="">Bank Name</label>
			  <s:select data-var="bankCode" name="bankCode" class="selectpicker" id="bankCode" headerKey=""
			  headerValue="Select Bank" list="@com.paymentgateway.commons.util.EnachNBIssuerType@values()"
					listValue="name" listKey="code" />
			<!--  <select data-var="bankCode" name="bankCode" class="selectpicker" id="bankCode">
				<option value="">Select Bank</option>
				<option value="470">TEST Bank</option>
				<option value="9660">HDFC BANK</option>
				<option value="11050">STATE BANK OF INDIA</option>
				<option value="9460">ICICI Bank</option>
				<option value="9480">Axis Bank</option>
				</select> -->
				<div class="error_field">
					Please select bank name
				</div>
				<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<div class="clearfix"></div>
		<!-- /.clearfix -->
		<div class="col-md-3 mb-20 d-none" data-id="cards">
			<div class="lpay_select_group">
			  <label for="">Bank Name</label>
			  <s:select data-var="bankCode" name="bankCode" class="selectpicker" id="bankCode" headerKey=""
			  headerValue="Select Bank" list="@com.paymentgateway.commons.util.EnachDCIssuerType@values()"
					listValue="name" listKey="code" />
				<div class="error_field">
					Bank name should not blank
				</div>
				
				<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<div class="clearfix"></div>
		<!-- /.clearfix -->
	
		<!-- /.col-md-3 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
			   <label for="">Account Type</label>
			   <select data-var="accountType" name="accountType" class="selectpicker" id="accountType">
					<option value="">Select Account Type</option>
					<option value="Saving">Saving</option>
					<!-- <option value="Current">Current</option> -->
			   </select>
			   <div class="error_field">
					Please select account type
				</div>
				<!-- /.error_field -->
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Account Number</label>
			  <input type="text" onpaste="return false" maxlength="20" onkeypress="onlyDigit(event)" data-var="accountNumber" class="lpay_input">
			</div>
			<!-- /.lpay_input_group -->
			<div class="error_field">
				Account number should not blank
			</div>
			<!-- /.error_field -->
		</div>
		<!-- /.col-md-3 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Account Holder Name</label>
			  <input type="text" onkeypress="onlyLetters(event)" data-var="accountHolderName" class="lpay_input">
			</div>
			<!-- /.lpay_input_group -->
			<div class="error_field">
				Account holder should not blank
			</div>
			<!-- /.error_field -->
		</div>
		<!-- /.col-md-4 -->
		<div class="col-md-3 mb-20 ">
			<div class="lpay_input_group">
			  <label for="">IFSC Code</label>
			  <input type="text" onpaste="return false" maxlength="11" onkeypress="lettersAndAlphabet(event)" data-var="ifscCode" id="ifsc" class="lpay_input">
			</div>
			<!-- /.lpay_input_group -->
			<div class="error_field">
				IFSC should not blank
			</div>
			<!-- /.error_field -->
		</div>
		<!-- /.col-md-4 -->
		<div class="clearfix"></div>
	
	
		<!-- /.col-md-4 -->
		<div class="clearfix"></div>
		<!-- /.clearfix -->
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Mobile Number</label>
			  <s:textfield type="text" data-var="consumerMobileNo" id="consumerMobileNo" name="consumerMobileNo"
					  class="lpay_input" maxlength="10" onkeypress="onlyDigit(event)" autocomplete="off" value="%{aaData.CUSTOMER_MOBILE}" readonly="true" />
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
				<s:textfield type="text" data-var="consumerEmailId" id="consumerEmailId" name="consumerEmailId"
					  class="lpay_input" autocomplete="off" value="%{aaData.CUSTOMER_EMAIL}" readonly="true" />
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
			  <label for="">Amount</label>
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
				<label for=""><s:if test="%{aaData.FREQUENCY == 'As And When Presented'}">ADHO</s:if><s:else><s:property value="%{aaData.FREQUENCY}" /></s:else> Debit Amount</label>
			  <s:textfield type="text" data-var="maxAmount" id="maxAmount" name="maxAmount"
					class="lpay_input" onkeypress="onlyDigit(event)" autocomplete="off" value="%{aaData.MONTHLY_AMOUNT}" readonly="true" />
					<div class="error_field">
						Max Amount should not blank
					</div>
					<!-- /.error_field -->
			</div>
			<!-- /.lpay_input_group -->
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
			<!-- /.lpay_input_group -->
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
			  <s:textfield type="text" data-var="debitStartDate" id="debitStartDate" name="debitStartDate"
					class="lpay_input" autocomplete="off" value="%{aaData.DEBIT_START_DATE}" readonly="true" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
		
		<div class="col-md-3 mb-20">
			<div class="lpay_input_group">
			  <label for="">Debit End Date</label>
			  <s:textfield type="text" data-var="debitEndDate" id="debitEndDate" name="debitEndDate"
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
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" data-var="registrationDate" value="%{aaData.REGISTRATION_DATE}" />
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
		
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <!-- <s:textfield type="text" data-var="merchantLogo" value="%{aaData.LOGO}" /> -->
			  <input type="hidden" data-var='mopType' id="mopType">
			</div>
			<!-- /.lpay_input_group -->
		</div>
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
		
		
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="returnUrl" data-var="returnUrl" value="%{aaData.RETURN_URL}" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
	
		<!-- /.col-md-4 -->
		
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="merchantReturnUrl" data-var="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}" />
			</div>
			<!-- /.lpay_input_group -->
		</div>
	
		<!-- /.col-md-4 -->
		
		<div class="col-md-3 mb-20 hide-input">
			<div class="lpay_input_group">
			  <s:textfield type="text" id="consumerId" data-var="consumerId" value="%{aaData.CONSUMER_ID}" />
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
		<div class="col-md-12 text-right">
			<div class="footer_logo text-right">
				<span>Powered By</span>
				<img src="../image/eNach-logo.png" alt="/">
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

<s:form action="iciciEnachResponse" method="POST" id="cancelForm">
	<s:hidden name="message" id="msg"></s:hidden>
	<s:if test="%{aaData.RESPONSE == 'INVALID REQUEST'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:if>
	<s:elseif test="%{aaData.RESPONSE == 'Duplicate order Id'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid End Date'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Start Date'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Amount'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Monthly Amount'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Frequency'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Tenure'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Merchant ID'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Customer Mobile'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Customer Email'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
	<s:elseif test="%{aaData.RESPONSE == 'Invalid Request ID'}">
		<s:hidden name="merchantReturnUrl" value="%{aaData.MERCHANT_RETURN_URL}"></s:hidden>
	</s:elseif>
</s:form>



    <script type="text/javascript" src="https://www.paynimo.com/Paynimocheckout/server/lib/checkout.js"></script>

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

		function fourDigitSpace(e) {
			var field = e.target,
				position = field.selectionEnd,
				length = field.value.length;

			field.value = field.value.replace(/[^0-9]/g, '').replace(/(.{4})/g, '$1 ').trim();
			field.selectionEnd = position += ((field.value.charAt(position - 1) === ' ' && field.value.charAt(length - 1) === ' ') ? 1 : 0);
		}



		// letters and alpabet
		function lettersAndAlphabet(event) {
			var x = event.keyCode;
			if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
			} else {
				event.preventDefault();
			}
		}

		function validCardNumber(e){
			var _this = e.value;
			console.log(_this.length);
			if(_this != "" && _this.length <= 9){
				e.closest(".col-md-3").classList.add("has-error");
				e.closest(".col-md-3").querySelector(".error_field").innerText = 'Invalid Card Details';
			}else if(_this != "" && _this.length > 9 && _this.length != 19){
				var _getError = document.querySelectorAll(".has-error");
				if(_getError.length == 0){
					e.closest(".col-md-3").classList.add("has-error");
					e.closest(".col-md-3").querySelector(".error_field").innerText = 'Invalid Card Details';
				}
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

		function checkBin(_this, _payId){
			var _subStr = _this.replace(/\s/g, '').slice(0, 9);
			var _token = $("#customToken").val();
			$.ajax({
				type: "post",
				url: "binResolverENach",
				data: { 
					bin : _subStr,
					token: _token,
					payId : _payId
					},
					success: function(data){
						if(data.response == "Success"){

						document.querySelector("#cardNumber").setAttribute("data-check", "checkAlready");
						document.querySelector("#mopTypeImg").setAttribute("src",  "../images/"+data.mopType.toLowerCase()+".png");
						document.querySelector("#mopType").value = data.mopType;
						
						}else{
						_check = true;
						document.querySelector("#mopType").value = "";
						document.querySelector("#mopTypeImg").setAttribute("src",  "../images/bc.png");
						document.querySelector("#cardNumber").closest(".col-md-3").classList.add("has-error");
						document.querySelector("#cardNumber").closest(".col-md-3").querySelector(".error_field").innerText = data.response;
						}
					}
			})
		}

		var _check = false;
		function checkCardType (e){
			var _payId = document.querySelector("[data-var='payId']").value;
			var _thisValue = e.value.replace(/\s/g, '');
			var _this = e.value;
			if(_check == true){
				document.querySelector("#cardNumber").closest(".col-md-3").classList.add("has-error");
			}
			if(_thisValue.length <= 8){
				document.querySelector("#cardNumber").setAttribute("data-check", "null");
				document.querySelector("#cardNumber").closest(".col-md-3").classList.remove("has-error");
				document.querySelector("#mopTypeImg").setAttribute("src", "../images/bc.png");
			}
			if(_thisValue.length > 8 && e.getAttribute("data-check") == 'null'){
				checkBin(_this, _payId);
			}else if(_thisValue > 10 && !e.getAttribute("data-check") == 'checkAlready'){
				checkBin(_this, _payId);
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

		function checkExpiry(e){
			var _value = e.value;
			var _date = new Date();
			var _year = _date.getFullYear();
			var _month = _date.getMonth()+1;
			var _sliceYear = Number(_value.slice(3));
			var _sliceYearDate = _year.toString().slice(2);
			if(_sliceYear >= Number(_sliceYearDate)){
				if(_sliceYear == Number(_sliceYearDate)){
					var _getDate = Number(_value.slice(0,2));
					var _getDateWithDate = _month.toString();
					if(_getDate < Number(_getDateWithDate)){
						e.closest(".col-md-3").classList.add("has-error");
						e.closest(".col-md-3").querySelector(".error_field").innerText = "Invalid Expiry Month/Year";
					}
				}
			} 
		}


        $(document).ready(function() {

			var _getHeaderHeight = $(".mpaHeader").outerHeight();
			var _getFooter = $(".lpay_footer").outerHeight();
			var _windowHeight = $(window).height();
			console.log("header height" + _getHeaderHeight);
			console.log("footer height" + _getFooter);
			console.log(_windowHeight);
			var _getExactHeight = _windowHeight - _getFooter - _getHeaderHeight;
			console.log(_getExactHeight);
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

	
			document.querySelector("#ifsc").onkeyup = function(e){
				e.target.closest(".col-md-3").querySelector(".error_field").innerText = "IFSC should not be blank";
                e.target.value = e.target.value.toUpperCase();
				
            }

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

            function handleResponse(res) {
                if (typeof res != 'undefined' && typeof res.paymentMethod != 'undefined' && typeof res.paymentMethod.paymentTransaction != 'undefined' && typeof res.paymentMethod.paymentTransaction.statusCode != 'undefined' && res.paymentMethod.paymentTransaction.statusCode == '0300') {
                    // success block
                } else if (typeof res != 'undefined' && typeof res.paymentMethod != 'undefined' && typeof res.paymentMethod.paymentTransaction != 'undefined' && typeof res.paymentMethod.paymentTransaction.statusCode != 'undefined' && res.paymentMethod.paymentTransaction.statusCode == '0398') {
                    // initiated block
                } else {
                    // error block
                }
            };

			// select on change dynamic
			document.querySelector("#paymentMode").onchange = function(e){
                var _this = e.target.value;
                if(_this == "netBanking"){
                    document.querySelector("#mopType").removeAttribute("data-var");
                }else{
                    document.querySelector("#mopType").setAttribute("data-var", "mopType");
                }
				var _getAllInput = document.querySelectorAll("[data-id]");
				_getAllInput.forEach(function(index, array, element){
					var _getClass = _getAllInput[array].classList;
					var _getClassStr = _getClass.toString();
					if(_getClassStr.indexOf("col-md-3") != -1){
						var _null = _getAllInput[array].querySelector("input");
						if(_null != null){
							_null.value = "";
						}
						_getAllInput[array].classList.remove("has-error");
						_getAllInput[array].classList.add("d-none");
					}
				})
				var _getAllActive = document.querySelectorAll("[data-id="+_this+"]");
				_getAllActive.forEach(function(index, array, element){
					_getAllActive[array].classList.remove("d-none");
				})
			}

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
						/* if(_getInput[element].value == ""){
							console.log("issue");
							console.log(_getInput[element].value);
							_getInput[element].closest(".col-md-3").classList.add("has-error");
							if(_getInput[element].closest(".col-md-3").querySelector(".error_field") !== null){
								_getInput[element].closest(".col-md-3").querySelector(".error_field").innerText = _getLabel + " Should Not Blank";
							}
							_checkError = false;
						}else{ */
							var _get = index.getAttribute("data-var");
							consumerData[_get] = index.value;
						//}
                    }
                })
				console.log(_getError.length);
				console.log(_checkError);
                if(_getError.length == 0){
                    if(_checkError == true){
                        document.querySelector("body").classList.remove("loader--inactive");
                        $.ajax({
                            type: "post",
                            url: "getEnachToken",
                            data: consumerData,
                            success: function(data){
                            	
                            	console.group("Bank Code:");
                                console.log(data.aaData.BANK_CODE);
                                console.groupEnd();
                                
                                var configJson = {
		                            'tarCall': false,
		                            'features': {
		                                'showLoader' : false,
		                                'showPGResponseMsg': true,
		                                'enableNewWindowFlow': false,    //for hybrid applications please disable this by passing false
		                                'enableExpressPay':true,
		                                'siDetailsAtMerchantEnd': true,
		                                'payDetailsAtMerchantEnd':true,
		                                'enableSI':true,
		                            },
		                            'consumerData': {
		                                'deviceId': 'WEBSH1', //possible values 'WEBSH1', 'WEBSH2' and 'WEBMD5'
		                                'token': data.aaData.TOKEN,
		                                'returnUrl': data.aaData.RETURN_URL,
		                                'responseHandler': handleResponse,
		                                'paymentMode': 'netBanking',
		                                //'paymentMode': 'all',
		                                'merchantLogoUrl': data.aaData.PAYMENTGATEWAY_LOGO,
		                                'merchantId': data.aaData.MERCHANT_ID,
		                                'currency': 'INR',
		        
		                                'bankCode' : data.aaData.BANK_CODE,
		        
		                                'consumerId': data.aaData.TXN_ID, //Your unique consumer identifier to register a SI
		                                'consumerMobileNo': data.aaData.CONSUMER_MOBILE_NO,
		                                'consumerEmailId': data.aaData.CONSUMER_EMAIL_ID,
		                                'txnId': data.aaData.PG_REF_NUM,   //Unique merchant transaction ID
		                                'items': [{ 'itemId' : data.aaData.ITEM_ID, 'amount' : data.aaData.AMOUNT, 'comAmt': '0'}],
		                                                                                                        
		                                'customStyle': {
		                                    'PRIMARY_COLOR_CODE': '#002663',   //merchant primary color code
		                                    'SECONDARY_COLOR_CODE': '#FFFFFF',   //provide merchant's suitable color code
		                                    'BUTTON_COLOR_CODE_1': '#002663',   //merchant's button background color code
		                                    'BUTTON_COLOR_CODE_2': '#FFFFFF'   //provide merchant's suitable color code for button text
		                                },
		        
		                                'accountHolderName' : data.aaData.ACCOUNT_HOLDER_NAME,
		                                'ifscCode': data.aaData.ifscCode,        //Pass this if ifscCode is captured at merchant side.                                                                                                
		                                'accountNo': data.aaData.ACCOUNT_NUMBER,
		                                'accountType': data.aaData.ACCOUNT_TYPE,
		                                'debitStartDate': data.aaData.START_DATE,
		                                'debitEndDate': data.aaData.END_DATE,
		                                'maxAmount': data.aaData.MAX_AMOUNT,
		                                'amountType': data.aaData.AMOUNT_TYPE,
		                                'frequency': data.aaData.FREQUENCY,              //  Available options DAIL, Week, MNTH, QURT, MIAN, YEAR, BIMN and ADHO
		                                //'saveInstrument': true  //mandatory to register SI
		                            }
		                        };
                                
		                        $.pnCheckout(configJson);
		
								function checkPopUp(){
									var _checkNull = document.querySelector(".modalpage");
									// console.log(_checkNull);
									if(_checkNull.style.display == 'block'){
										document.querySelector("body").classList.add("loader--inactive");
										clearInterval(_stopLoader);
									}
								}
								
								var _stopLoader = setInterval(checkPopUp, 500);
								
		                        if(configJson.features.enableNewWindowFlow){
		                            pnCheckoutShared.openNewWindow();
		                        }
                            }
                        });    
                    }
                                 
                }
                 //T557156|12345|10|96010000034256|2356|8498951813|harilakkakula28@gmail.com|13-02-2020|10-01-2027|500|M|ADHO|||||6916391056YFEHXG
              
            });
        });
    </script>
</body>

</html>
