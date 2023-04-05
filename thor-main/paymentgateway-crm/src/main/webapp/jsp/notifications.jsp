<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>notification Email</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript">
	jQuery(document).ready(function() {
		var doc = $(document);
		jQuery('a.add-type').die('click').live('click', function(e) {
			e.preventDefault();
			var content = jQuery('#type-container .type-row'), element = null;
			//for (var i = 0; i < 2; i++) {
				element = content.clone();
				var type_div = 'teams_' + jQuery.now();
				element.attr('id', type_div);
				element.find('.remove-type').attr('targetDiv', type_div);
				element.appendTo('#type_container');
			//}
		});
		jQuery(".remove-type").die('click').live('click', function(e) {
			var didConfirm = confirm("Are you sure You want to delete");
			if (didConfirm == true) {
				var id = jQuery(this).attr('data-id');
				var targetDiv = jQuery(this).attr('targetDiv');
				jQuery('#' + targetDiv).remove();
				return true;
			} else {
				return false;
			}
		});

	});
</script>
<script type="text/javascript">
$(document).ready(function() {
 $("#btnsubmit").click(function(evt) {
	 var transactionEmail = new Array;
    var token = document.getElementsByName("token")[0].value;
 	var merchants =  document.getElementById("merchant").value;
	var sendMultipleEmailer =  document.getElementById("sendMultipleEmailer").value;
	var transactionEmailerFlag =  document.getElementById("transactionEmailerFlag").checked;
	var refundTransactionMerchantEmailFlag =  document.getElementById("refundTransactionMerchantEmailFlag").checked;
	var transactionAuthenticationEmailFlag =  document.getElementById("transactionAuthenticationEmailFlag").checked;
	var transactionCustomerEmailFlag =  document.getElementById("transactionCustomerEmailFlag").checked;
	var refundTransactionCustomerEmailFlag =  document.getElementById("refundTransactionCustomerEmailFlag").checked;
	var transactionSmsFlag =  document.getElementById("transactionSmsFlag").checked;
	var merchantHostedFlag =  document.getElementById("merchantHostedFlag").checked;
	var iframePaymentFlag =  document.getElementById("iframePaymentFlag").checked;
	var surchargeFlag =  document.getElementById("surchargeFlag").checked;
	var retryTransactionCustomeFlag =  document.getElementById("retryTransactionCustomeFlag").checked;
	transactionEmail.push(sendMultipleEmailer);
	transactionEmail.push(transactionEmailerFlag);
	transactionEmail.push(refundTransactionMerchantEmailFlag);
	transactionEmail.push(transactionAuthenticationEmailFlag);
	transactionEmail.push(transactionCustomerEmailFlag);
	transactionEmail.push(refundTransactionCustomerEmailFlag);
	transactionEmail.push(transactionSmsFlag);
	transactionEmail.push(merchantHostedFlag);
	transactionEmail.push(iframePaymentFlag);
	transactionEmail.push(surchargeFlag);
	transactionEmail.push(retryTransactionCustomeFlag);
  var transactionEmailer = {sendMultipleEmailer:sendMultipleEmailer,transactionEmailerFlag:transactionEmailerFlag,refundTransactionMerchantEmailFlag:refundTransactionMerchantEmailFlag,
		  transactionAuthenticationEmailFlag:transactionAuthenticationEmailFlag,transactionCustomerEmailFlag:transactionCustomerEmailFlag,refundTransactionCustomerEmailFlag:refundTransactionCustomerEmailFlag,
		  transactionSmsFlag:transactionSmsFlag,merchantHostedFlag:merchantHostedFlag,iframePaymentFlag:iframePaymentFlag,surchargeFlag:surchargeFlag,retryTransactionCustomeFlag:retryTransactionCustomeFlag};
          transactionEmail.push(transactionEmailer);
     	$.ajax({
		type : "POST",
		url : "saveNotification",
		timeout: 0,
		data : {
			payId :merchants,
			transactionEmail :JSON.stringify(transactionEmailer),
					token:token,
				    "struts.token.name": "token",
		},
		    success : function(data) {
			     alert ( "Notications save successfully.." );
			    },
			    error : function(data) {

			    }
			   });
          });
		  });
</script>
</head>
<body>
	<%-- s:form action="saveNotification"> --%>
		<div class="col-md-2 col-xs-3">
			<s:select name="merchants" class="form-control" id="merchant"
				headerKey="ALL MERCHANTS" headerValue="ALL MERCHANTS"
				listKey="payId" listValue="businessName" list="merchantList"
				autocomplete="off"/>
		</div>
      <s:actionmessage class="success success-text" />
		<div class="tranjuctionCon">
			<h2>Notification Email</h2>
			<div id="type_container">
				<div class="row form-group" id="edit-0">
					<div class="">
						<div class="tranjuctionCon4">
							<div class="fl_wrap" style="width: 90%;">
								<label class='fl_label'>Send Emailer for Users</label>
								<s:textfield class="fl_input" type="text"  placeholder="Enter EmailId"
									id="sendMultipleEmailer" name="sendMultipleEmailer"
									value="%{notificationEmailer.sendMultipleEmailer}"></s:textfield>
							</div>
						</div>
						<div class="tranjuctionCon4">
							Merchant Transaction Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="transactionEmailerFlag" id="transactionEmailerFlag"
									value="%{notificationEmailer.transactionEmailerFlag}" />
							</div>
						</div>
						<div class="tranjuctionCon4">
							Merchant Refund Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="refundTransactionMerchantEmailFlag"  id="refundTransactionMerchantEmailFlag"
									value="%{notificationEmailer.refundTransactionMerchantEmailFlag}" />
							</div>
						</div>

						<div class="tranjuctionCon4">
							Customer Authentication Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="transactionAuthenticationEmailFlag"  id="transactionAuthenticationEmailFlag"
									value="%{notificationEmailer.transactionAuthenticationEmailFlag}" />
							</div>
						</div>
						<div class="tranjuctionCon4">
							Customer Transaction Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="transactionCustomerEmailFlag" id="transactionCustomerEmailFlag"
									value="%{notificationEmailer.transactionCustomerEmailFlag}" />
							</div>
							<div class="clear"></div>
						</div>
						<div class="tranjuctionCon4">
							Transaction Sms Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="transactionSmsFlag"  id="transactionSmsFlag"
									value="%{notificationEmailer.transactionSmsFlag}" />
							</div>
						</div>
						<div class="tranjuctionCon4">
							Transaction ExpressPay Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="expressPayFlag"  id="expressPayFlag"
									value="%{notificationEmailer.expressPayFlag}" />
							</div>
						</div>
							<div class="tranjuctionCon3.5">
							Transaction MerchantHosted payment Page Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="merchantHostedFlag"  id="merchantHostedFlag"
									value="%{notificationEmailer.merchantHostedFlag}" />
							</div>
						</div>
							<div class="tranjuctionCon3">
							Transaction Iframepayment Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="iframePaymentFlag"  id="iframePaymentFlag"
									value="%{notificationEmailer.iframePaymentFlag}" />
							</div>
						</div>
						<div class="tranjuctionCon4">
							Transaction Retry Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="retryTransactionCustomeFlag"  id="retryTransactionCustomeFlag"
									value="%{notificationEmailer.retryTransactionCustomeFlag}" />
							</div> 
						</div>
						
						<div class="tranjuctionCon4">
							Surcharge Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="surchargeFlag"  id="surchargeFlag"
									value="%{notificationEmailer.surchargeFlag}" />
							</div> 
						</div>
						<div class="tranjuctionCon4">
						Customer Refund Emailer
						<div style="float: left; margin: 0 7px 0 0">
							<s:checkbox name="refundTransactionCustomerEmailFlag" id="refundTransactionCustomerEmailFlag"
								value="%{notificationEmailer.refundTransactionCustomerEmailFlag}" />
						</div>
					</div>
					</div>
					<div class="col-md-6 clearfix">
						<a class="add-type pull-right" href="javascript: void(0)"
							title="Click to add more"><i
							class="glyphicon glyphicon-plus-sign"></i></a>
					</div>
					<s:textfield type="button"
									value="Save" class="btn btn-success btn-sm" id="btnsubmit" theme="simple" />
				<%-- 	<s:submit name="Save" class="btn btn-success btn-md"   id="save_value"  value="Save"></s:submit> --%>
					<div class="clear"></div>
					<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
				</div>
			</div>
		</div>
		<div id="type-container" class="hide">
			<div class="row form-group type-row" id="">
				<!-- <div class="col-md-3"> -->
				<div class="">
					<div class="tranjuctionCon4">
						<div class="fl_wrap" style="width: 90%;">
							<label class='fl_label'>Send Emailer for Users</label>
							<s:textfield class="fl_input" type="text" placeholder="Enter EmailId"  
								id="sendMultipleEmailer" name="sendMultipleEmailer"
								value="%{notificationEmailer.sendMultipleEmailer}"></s:textfield>
						</div>
					</div>
					<div class="tranjuctionCon4">
						Merchant Transaction Emailer
						<div style="float: left; margin: 0 7px 0 0">
							<s:checkbox name="transactionEmailerFlag"   id="transactionEmailerFlag"
								value="%{notificationEmailer.transactionEmailerFlag}" />
						</div>
					</div>
					<div class="tranjuctionCon4">
						Merchant Refund Emailer
						<div style="float: left; margin: 0 7px 0 0">
							<s:checkbox name="refundTransactionMerchantEmailFlag"   id="refundTransactionMerchantEmailFlag"
								value="%{notificationEmailer.refundTransactionMerchantEmailFlag}" />
						</div>
					</div>

					<div class="tranjuctionCon4">
						Customer Authentication Emailer
						<div style="float: left; margin: 0 7px 0 0">
							<s:checkbox name="transactionAuthenticationEmailFlag"    id="transactionAuthenticationEmailFlag"
								value="%{notificationEmailer.transactionAuthenticationEmailFlag}" />
						</div>
					</div>
					<div class="tranjuctionCon4">
						Customer Transaction Emailer
						<div style="float: left; margin: 0 7px 0 0">
							<s:checkbox name="transactionCustomerEmailFlag"   id="transactionCustomerEmailFlag"
								value="%{notificationEmailer.transactionCustomerEmailFlag}" />
						</div>
						<div class="clear"></div>
					</div>
					<div class="tranjuctionCon4">
							Transaction Sms Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="transactionSmsFlag"  id="transactionSmsFlag"
									value="%{notificationEmailer.transactionSmsFlag}" />
							</div>
						</div>
						<div class="tranjuctionCon4">
							Transaction ExpressPay Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="expressPayFlag"  id="expressPayFlag"
									value="%{notificationEmailer.expressPayFlag}" />
							</div>
						</div>
							<div class="tranjuctionCon3.5">
							Transaction MerchantHosted payment Page Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="merchantHostedFlag"  id="merchantHostedFlag"
									value="%{notificationEmailer.merchantHostedFlag}" />
							</div>
						</div>
							<div class="tranjuctionCon3">
							Transaction Iframepayment Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="iframePaymentFlag"  id="iframePaymentFlag"
									value="%{notificationEmailer.iframePaymentFlag}" />
							</div>
						</div>
						<div class="tranjuctionCon4">
							Transaction Retry Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="retryTransactionCustomeFlag"  id="retryTransactionCustomeFlag"
									value="%{notificationEmailer.retryTransactionCustomeFlag}" />
							</div> 
						</div>
					<div class="tranjuctionCon4">
						Customer Refund Emailer
						<div style="float: left; margin: 0 7px 0 0">
							<s:checkbox name="refundTransactionCustomerEmailFlag"  id="refundTransactionCustomerEmailFlag"
								value="%{notificationEmailer.refundTransactionCustomerEmailFlag}" />
						</div>
					</div>
					<div class="tranjuctionCon4">
							Surcharge Emailer
							<div style="float: left; margin: 0 7px 0 0">
								<s:checkbox name="surchargeFlag"  id="surchargeFlag"
									value="%{notificationEmailer.surchargeFlag}" />
							</div> 
						</div>
					
				</div>
				<div class="col-md-6 clearfix">
					<a class="remove-type pull-right"
						href="javascript: void(0)"><i
						class="glyphicon glyphicon-trash"></i></a>
				</div>
			</div>
		</div>
	<%-- </s:form> --%>
</body>
</html>