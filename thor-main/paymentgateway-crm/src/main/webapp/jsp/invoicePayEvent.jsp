<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>


<html>
<head>
<title>Promotional Payment</title>
<link rel="icon" href="../image/favicon-32x32.png">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/bootstrap.min.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script>
	if (self == top) {
		var theBody = document.getElementsByTagName('body')[0];
		theBody.style.display = "block";
	} else {
		top.location = self.location;
	}
	
	//to show error in the fields
	function addFieldError(fieldId, errMsg){
		var errSpanId = fieldId+"Err";
		var elmnt = document.getElementById(fieldId);
		elmnt.className = "textFL_merch_invalid";
		elmnt.focus();
		document.getElementById(errSpanId).innerHTML = errMsg;
	}
	
	// to remove the error 
	function removeFieldError(fieldId){
		var errSpanId = fieldId+"Err";
		document.getElementById(errSpanId).innerHTML = "";
		document.getElementById(fieldId).className = "textFL_merch";
	}
	
	function valdEmail(){
		
		var emailRegex = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;
		var element = document.getElementById("CUST_EMAIL");
		var value = element.value.trim();
		if(value.length>0){
			if(!value.match(emailRegex)){addFieldError('CUST_EMAIL', "Enter valid email address");
			return false;
			}else{removeFieldError('CUST_EMAIL');
			return true;
			}
		}else{addFieldError('CUST_EMAIL', "Enter valid email address");
		return false;
		}
	}
	function valdPhone(){
		var phoneElement = document.getElementById("phone");
		var value = phoneElement.value.trim();
		if ( value.length >0) {
			var phone = phoneElement.value;
			var phoneexp =  /^[0-9]{10,15}$/;
			if (!phone.match(phoneexp)) {
				addFieldError("phone", "Enter valid phone no.");
				return false;
			}else{
				removeFieldError('phone');
				return true;
			}
		}else{addFieldError("phone", "Enter valid phone no.");
			return false;
		}
	}
	function valdName(){
		var nameElement = document.getElementById("CUST_NAME");
		var value = nameElement.value.trim();
		if ( value.length >0) {
			var name = nameElement.value;
			var nameexp =  /^[a-zA-Z ]+$/;
			if (!name.match(nameexp)) {
				addFieldError("CUST_NAME", "Enter valid name");
				return false;
			}else{
				removeFieldError('CUST_NAME');
				return true;
			}
		}else{addFieldError("CUST_NAME", "Enter valid name");
			return false;
		}
	}
	function validateFields(){
		var flag = valdName();
		flag = flag && valdPhone();
		flag = flag && valdEmail();
		return flag;
	}
/* 		var nameRegex = /^[a-zA-Z ]+$/;
		var emailRegex = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;
		var phoneRegex = /^[0-9]{10,15}$/; */
</script>
<script>
	// function formLoad() {
	// 	var enablePayNow = '<s:property value="%{enablePay}" />';
	// 	/* if (enablePayNow == "TRUE") {
	// 		document.getElementById('btnPay').disabled = false;
	// 		document.getElementById('lblMsg').style.display = "none";
	// 	} else {
	// 		document.getElementById('btnPay').disabled = true;
	// 		document.getElementById('lblMsg').style.display = "block";
	// 	} */
	// }
		function submitForm() {
				var invoiceId=document.getElementById("invoice_Id").value;
				var name=document.getElementById("CUST_NAME").value;
				var email=document.getElementById("CUST_EMAIL").value;
				var phone=document.getElementById("CUST_PHONE").value;
				var payURL = document.getElementById('payUrl').innerHTML;
				var form = document.forms[0];
				
				$.ajax({
						url: "getInvoiceHash",
						type : 'post',
						data: {
							invoiceId : invoiceId,
							name : name,
							email : email,
							phone : phone
						},
						success: function(data){
							
							$("#HASH").val(data.hash);
							form.action = payURL;
							form.submit();
							
						}});

			
		
	
			

	    } 
	function resetFieldColor(fieldId) {
		document.getElementById(fieldId).style.backgroundColor = 'transparent';
	}
</script>
<style>
.invocspan {
    font: bold 11px arial;
    color: #ff0000;
}
	.textFL_merch {
    border: 1px solid #c0c0c0;
    background: #fff;
    padding: 8px;
    width: 100%;
    color: #000;
    border-radius: 3px;
}

.textFL_merch:hover {
    border: 1px solid #d5d0a3;
    background: #fffce4;
    padding: 8px;
    width: 100%;
    color: #ff0000;
    border-radius: 3px;
}
	.textFL_merch_invalid {
    border: 1px solid #c0c0c0;
    background: #fff;
    padding: 8px;
    width: 100%;
    border-color: #a94442;
    border-radius: 1px;
}
</style>
</head>
<body>

	<s:form method="post">
	<div id="payUrl" style="display:none"><%=new PropertiesManager().getSystemProperty("invoicePaymentLink")%></div>
	
		<br />
		<table width="70%" align="center" border="0" cellspacing="0"
			cellpadding="0">
			<tr>
				<td align="center" valign="middle" height="40"><img
					src="../image/lpay.png" width="220" /></td>
			</tr>
			<tr>
				<td align="center" valign="top"><table width="100%" border="0"
						align="center" cellpadding="0" cellspacing="0" class="formbox">
						<tr>
							<td colspan="5" align="left" valign="middle" style="color:#fff"
								class="boxheadingsmall" height="30">&nbsp;&nbsp;Review Information and
								Pay</td>
						</tr>
						<tr>
							<td height="30" colspan="5" align="left" valign="middle">&nbsp;&nbsp;&nbsp;&nbsp;<strong>Detail
									Information</strong></td>
						</tr>
						<tr>
							<td colspan="4" align="center" valign="top">
							<table	width="97%" border="0" cellpadding="0" cellspacing="0"
									class="invoicetable">

									<tr>
										<th width="20%" height="25" align="left" valign="middle"><strong>Invoice
												no</strong></th>
										<th width="20%" align="left" valign="middle"><strong>Name</strong></th>
										<th width="20%" align="left" valign="middle"><strong>Phone</strong></th>
										<th width="20%" align="left" valign="middle"><strong>Email</strong></th>
									</tr>
									<tr>
										<td height="25" align="left" valign="middle"><s:property
												value="%{invoice.invoiceNo}" /></td>
										<td align="left" valign="middle"><s:textfield type="text"
												name="CUST_NAME" id="CUST_NAME" autocomplete="off"
												onkeyup="valdName()" 
												class="textFL_merch invoiceField"
												></s:textfield>
												<span id="CUST_NAMEErr" class="invocspan"></span>
												</td>
										<td align="left" valign="middle"><s:textfield type="text"
												name="CUST_PHONE" id="CUST_PHONE" autocomplete="off"
												 
												class="textFL_merch invoiceField"
												></s:textfield>
												<span id="phoneErr" class="invocspan"></span>
												</td>
										<td align="left" valign="middle"><s:textfield type="text"
												name="CUST_EMAIL" id="CUST_EMAIL" autocomplete="off"
												onkeyup="valdEmail()" 
												class="textFL_merch invoiceField"
												></s:textfield>
												<span id="CUST_EMAILErr" class="invocspan"></span>
												</td>
									</tr>
</table>

<table	width="97%" border="0" cellpadding="0" cellspacing="0"		class="invoicetable">
									<tr>
										<td height="30" colspan="5" align="left" valign="middle">&nbsp;&nbsp;&nbsp;&nbsp;<strong>Merchant Information</strong></td>
									</tr>
									<tr>
										<th height="25" align="left" valign="middle"><strong><span>Business Name</span></strong></th>
										<th align="left" valign="middle"><strong>State</strong></th>
										<th align="left" valign="middle"><strong>Zip</strong></th>
										<th align="left" valign="middle"><strong><span>City</span></strong></th>
										<th align="left" valign="middle"><strong><span>Country</span></strong></th>
									</tr>
									<tr>
										<td height="25" align="left" valign="middle"><s:property
												value="%{invoice.businessName}" /></td>
										<td align="left" valign="middle"><s:property
												value="%{invoice.state}" /></td>
										<td align="left" valign="middle"><s:property
												value="%{invoice.zip}" /></td>
										<td align="left" valign="middle"><s:property
												value="%{invoice.city}" /></td>
										<td align="left" valign="middle"><s:property
												value="%{invoice.country}" /></td>
									</tr>
									</table>
									<table	width="97%" border="0" cellpadding="0" cellspacing="0"		class="invoicetable">
									<tr>
										<th height="30" colspan="4" align="left" valign="middle"><span><strong>Address</strong></span></th>
									</tr>
									<tr>
										<td height="25" colspan="4" align="left" valign="middle"><s:property
												value="%{invoice.address}" /></td>
									</tr>
								</table>
					<table	width="97%" border="0" cellpadding="0" cellspacing="0"		class="invoicetable">
						<tr>
							<td height="30" colspan="5" align="left" valign="middle">&nbsp;&nbsp;&nbsp;&nbsp;
								<strong>Product Information</strong>
							</td>
						</tr>
						<tr>
							<td align="center" valign="top"><table width="97%"
									border="0" cellpadding="0" cellspacing="0" class="invoicetable">
									<tr>
										<th width="22%" height="25" align="left" valign="middle">
											Name</th>
										<th colspan="2" align="left" valign="middle">Description</th>
										<th width="12%" align="left" valign="middle"><span>Quantity</span></th>
										<th width="16%" align="left" valign="middle"><span>Amount</span></th>
									</tr>

									<tr>
										<td height="25" align="left" valign="middle"><div
												class="txtnew">
												<s:property value="%{invoice.productName}" />
											</div></td>
										<td colspan="2" align="left" valign="middle"><div
												class="txtnew">
												<s:property value="%{invoice.productDesc}" />
											</div></td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:property value="%{invoice.quantity}" />
											</div></td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:property value="%{invoice.amount}" />
											</div></td>
									</tr>
									<tr>
										<td height="25" align="left" valign="middle">&nbsp;</td>
										<td width="23%" align="left" valign="middle">&nbsp;</td>
										<td width="27%" align="left" valign="middle">&nbsp;</td>
										<td align="left" valign="middle">&nbsp;</td>
										<th align="left" valign="middle">Service Charge</th>
									</tr>
									<tr>
										<td height="25" align="left" valign="middle">&nbsp;</td>
										<td align="left" valign="middle">&nbsp;</td>
										<td align="right" valign="middle">&nbsp;</td>
										<td align="right" valign="middle">&nbsp;</td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:property value="%{invoice.serviceCharge}" />
											</div></td>
									</tr>
									<tr>
										<th height="25" align="left" valign="middle">All prices
											are in</th>
										<th align="left" valign="middle">Expire in days</th>
										<th align="left" valign="middle">Expire in hours</th>
										<th align="right" valign="middle">&nbsp;</th>
										<th align="left" valign="middle">Total Amount</th>
									</tr>
									<tr>
										<td height="25" align="left" valign="middle"><div
												class="txtnew">
												<s:property value="%{currencyName}" />
											</div></td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:property value="%{invoice.expiresDay}" />
											</div></td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:property value="%{invoice.expiresHour}" />
											</div></td>
										<td align="right" valign="middle">&nbsp;</td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:property value="%{invoice.totalAmount}" />
											</div></td>
									</tr>
										
								</table>

					<table width="100%" border="0" cellpadding="0" cellspacing="0">
						<tr >
								<td align="center" valign="middle" style="text-align: center;">
									<input type="button" id="btnPay" class="btn btn-success btn-md btn-block" value="Pay Now" onclick="javascript:submitForm()"/>
							</tr>
						<tr style="display:none">
							<td align="right" valign="middle" style="display:none">
							<s:label id="lblMsg" class="redsmalltext" value="Link has been expired"></s:label></td>
						</tr>
						<tr>					
							<s:hidden id="PAY_ID" name="PAY_ID" class="invoiceField" value="%{invoice.payId}" />
							<s:hidden id="ORDER_ID" name="ORDER_ID" class="invoiceField"
								value="%{invoice.invoiceId}" />
							<s:hidden id="AMOUNT" name="AMOUNT" class="invoiceField" value="%{totalamount}" />
							<s:hidden id="TXNTYPE" name="TXNTYPE" class="invoiceField" value="SALE" />
							<s:hidden id="CUST_STREET_ADDRESS1" class="invoiceField" name="CUST_STREET_ADDRESS1"
								value="%{invoice.address}" />
							<s:hidden id="CUST_ZIP" name="CUST_ZIP" class="invoiceField" value="%{invoice.zip}" />
							<s:hidden id="PRODUCT_DESC" class="invoiceField" name="PRODUCT_DESC"
								value="%{invoice.productDesc}" />
							<s:hidden id="CURRENCY_CODE" class="invoiceField" name="CURRENCY_CODE"
								value="%{invoice.currencyCode}" />
							<s:hidden id="RETURN_URL" class="invoiceField" name="RETURN_URL"
								value="%{invoice.returnUrl}" />
							<s:hidden id="HASH" name="HASH" />
							<s:hidden id="invoice_Id" value="%{invoice.invoiceId}"/>
						
							
						</tr>
				</table>
		</table>
		<br />
	</s:form>
</body>
</html>