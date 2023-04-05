<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>

<head>
    <title>Response Page</title>
    <meta name="viewport" content="width=device-width" />
    <link rel="stylesheet" href="../css/bootstrap.min.css">
    <link rel="stylesheet" href="../fonts/css/font-awesome.css">
    <link rel="stylesheet" href="../css/styles.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	<style>

		

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
		.success-msg {
			padding: 10px;
			display: block;
			text-align: center;
			font-size: 16px;
			color: #0dc808;
			padding-bottom: 0;
		}

		.mt-70{
			margin-top: 0px !important;
		}

		.lpay_table_wrapper{
			max-width: 600px;
			margin: auto;
		}

		.lpay_table_wrapper table{
			width: 100%;
		}

		.mpaLogo img{
			max-width: 100%;
			max-height: 70px;
		}

		.eNACH-response{
			display: flex;
			align-items: center;
			justify-content: center;
			position: relative;
			z-index: 9999;
			height: 100vh;
		}

		.eNACH-response-box {
			width: 100%;
			max-width: 650px;
			background-color: #fff;
			border-radius: 5px;
			box-shadow: 0 0 20px rgb(0 0 0 / 10%);
			padding: 20px;
			margin-top: 20px;
		}

		.eNACH-respone-data {
			display: flex;
			flex-wrap: wrap;
		}

		.eNACH-respone-data > div {
			width: 100%;
			max-width: 33.33%;
			display: flex;
			flex-direction: column;
			margin-top: 15px;
		}

		.eNACH-reponse-msg {
			padding-bottom: 10px;
			border-bottom: 1px solid #ddd;
		}

		.eNACH-respone-data-div span:last-child{
			font-weight: 600;
			font-size: 12px;
			color: #444;
		}

		.eNACH-response::before {
			content: "";
			width: 100%;
			height: 106%;
			position: fixed;
			background-color: rgba(0,0,0,.8);
			z-index: -1;
		}

		.eNACH-response-progress {
			text-align: center;
			margin-top: 17px;
			font-size: 16px;
		}

		.eNACH-response-progress .progess-bar{
			width: 100%;
			height: 4px;
			display: block;
			margin-top: 17px;
			background-color: #ccc;
			position: relative;
			overflow: hidden;
			border-radius: 4px;
		}

		.eNACH-response-progress .progess-bar::after{
			content: "";
			width: 100%;
			height: 4px;
			position: absolute;
			background-color: #002663;
			top: 0;
			right: 100%;
			transition: all 10s ease;
			border-radius: 4px;
		}

		.eNACH-response-progress.active-bar .progess-bar::after{
			right: 0;
		}

		#timeCount{
			color: #f00;
		}

	</style>
</head>

<body>
	
	<!-- /.mpaHeader -->
	<div class="eNACH-response">
		<div class="eNACH-response-box">
			<div class="eNACH-response-icon text-center">
				<s:if test="%{aaData.STATUS == 'Captured'}">
					<img src="../image/success.png" alt="">
				</s:if>
				<s:else>
					<img src="../image/failed.png" alt="">
				</s:else>
			</div>
			<!-- /.eNACH-response-icon -->
			<div class="eNACH-reponse-msg">
				<s:if test="%{aaData.STATUS == 'Captured'}">
					<div class="success-msg">
						eNACH Registration has been completed
					</div>
					<!-- /.success-msg -->
				</s:if>
				<s:else>
					<div class="success-msg" style="color: #f00">
						eNACH Registration Failed
					</div>
					<!-- /.success-msg -->
				</s:else>
			</div>
			<!-- /.eNACH-reponse-msg -->
			<div class="eNACH-respone-data">
				<s:if test="%{aaData.PAYMENT_TYPE == 'Net Banking' || aaData.PAYMENT_TYPE == 'Debit Card'}">
					<div class="eNACH-respone-data-div">
						<span>Registration Mode</span>
						<span><s:property value="%{aaData.PAYMENT_TYPE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Order ID</span>
						<span><s:property value="%{aaData.ORDER_ID}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>A/c Type</span>
						<span><s:property value="%{aaData.ACCOUNT_TYPE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>A/c Number</span>
						<span><s:property value="%{aaData.ACCOUNT_NO}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>A/c Holder Name</span>
						<span><s:property value="%{aaData.ACCOUNT_HOLDER_NAME}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Bank Name</span>
						<span><s:property value="%{aaData.BANK_NAME}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>UMRN Number</span>
						<span><s:property value="%{aaData.UMRN_NUMBER}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Mobile Number</span>
						<span><s:property value="%{aaData.CUST_PHONE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Email ID</span>
						<span><s:property value="%{aaData.CUST_EMAIL}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Registration Amount</span>
						<span><s:property value="%{aaData.AMOUNT}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Debit Amount (<s:property value="%{aaData.FREQUENCY}" />)</span>
						<span><s:property value="%{aaData.MAX_AMOUNT}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Total Amount</span>
						<span><s:property value="%{aaData.TOTAL_AMOUNT}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Status</span>
						<span><s:property value="%{aaData.STATUS}" /></span>
					</div>
					<%-- <!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Response Message</span>
						<span><s:property value="%{aaData.RESPONSE_MESSAGE}" /></span>
					</div> --%>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Frequency</span>
						<span><s:property value="%{aaData.FREQUENCY}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Tenure</span>
						<span><s:property value="%{aaData.TENURE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Debit Start Date</span>
						<span><s:property value="%{aaData.FROMDATE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Debit End Date</span>
						<span><s:property value="%{aaData.TODATE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
				</s:if>
				<s:else>
					<div class="eNACH-respone-data-div">
						<span>Status</span>
						<span><s:property value="%{aaData.STATUS}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
					<div class="eNACH-respone-data-div">
						<span>Response Message</span>
						<span><s:property value="%{aaData.RESPONSE_MESSAGE}" /></span>
					</div>
					<!-- /.eNACH-respone-data-div -->
                </s:else>
			</div>
			<!-- /.eNACH-respone-data -->
			
			<s:if test="%{aaData.RESPONSE_MESSAGE != 'Duplicate Request' 
						&& aaData.RESPONSE_MESSAGE != 'Authentication Failed'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Request ID'
						&& aaData.RESPONSE_MESSAGE != 'Invalid End Date'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Start Date'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Amount'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Monthly Amount'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Frequency'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Tenure'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Merchant ID'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Customer Mobile No'
						&& aaData.RESPONSE_MESSAGE != 'Invalid Customer Email Id'
						}">
			<div class="download-button text-center" style="width: 100%;margin-top: 20px">
				<button class="lpay_button lpay_button-md lpay_button-primary" style="display: inline-block;" id="download_pdf">Download PDF</button>
			</div>
			</s:if>
			<!-- /.download-button -->
			<div class="eNACH-response-progress">
				<!-- <span>You will be redirected to Merchant Site in <span id="timeCount">10</span> Seconds</span> -->
				<div class="progess-bar"></div>
				<!-- /.progess-bar -->
			</div>
			<!-- /.eNACH-response-progress -->
		</div>
		<!-- /.eNACH-response-box -->
		<s:hidden id="logoUrl" value="%{aaData.LOGO}"></s:hidden>
        <s:form action="downloadENachRegistrationPdf" id="downloaPdfForm">
            <s:hidden id="txnId" value="%{aaData.TXN_ID}" name="txnId"></s:hidden>
        </s:form>
	</div>
	<!-- /.eNACH-response -->
	<!-- eNach lapy_section white-bg box-shadow-box mt-70 p20 -->
   
	<s:hidden id="logoUrl" value="%{aaData.LOGO}"></s:hidden>
	<s:hidden id="returnUrl" value="%{aaData.RETURN_URL}"></s:hidden>
	<a href="#" data-id="returnUrl"></a>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->


    <script type="text/javascript">
        $(document).ready(function() {

			
			/* $("#logo").attr("src", $("#logoUrl").val()); */

			// function clickUrl(){
			// 	var _getUrl = document.querySelector("#returnUrl").value;
			// 	location.replace(_getUrl);
			// }

			// var _count = 10;
			// function count (){
			// 	if(_count == 1){
			// 		clearInterval(_runTime);
			// 		clickUrl();
			// 	}
			// 	document.querySelector("#timeCount").innerHTML = --_count;
			// }

			// var _runTime = setInterval(count, 1000);

			// $(".eNACH-response-progress").addClass("active-bar");


			$("#download_pdf").on("click", function(e){
                $("#downloaPdfForm").submit();
            })
			// create function for remove value 
			
        });
    </script>
</body>

</html>
