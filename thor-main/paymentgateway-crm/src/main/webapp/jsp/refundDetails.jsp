<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Refund Details</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/loader.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<style>
.no-close .ui-dialog-titlebar-close {
    display: none;
}
.ui-widget-header {
	border: 1px solid #4db0f4;
	background:#002163;
	color:#ffffff;
	font:bold 15px Arial;
	padding-left:10px;
	color: #ffffff;
	font-weight: bold;
}
#basicModal {
	display:none;
	font:normal 12px Arial;
}
</style>
<script>
function callChargeback(){
	document.chargeback.submit();
}

</script>
<script type="text/javascript">	
 $(document).ready( function() {
	 
	 $('#amount').on('keyup', function() {
		    if (this.value[0] === '.') {
		        this.value = '0'+this.value;
		    }
		});
    
	 var flag = document.getElementById("buttonFlag").value;
	 if(flag){
		 hideButton();
	 }
	 
	 //////for popup//////
	 function yesnodialog(btnYesText, btnNoText, element){
		  var btns = {};
		  btns[btnYesText] = function(){ 
			   var mopType = '<s:property value="transDetails.mopType" />';
				var acquirer  = '<s:property value="transDetails.acquirerCode"/>';
				
				// Direcpay Netbaning Refund Transaction Process with DirecpayRefundRequest.java
				if(acquirer == "DIRECPAY"){
					var createDate = new Date('<s:property value="transDetails.createDate"/>');
					createDate.setDate(createDate.getDate() + 2); 
					var currentDate = new Date();
					var diffDate = new Date();
					diffDate = currentDate - createDate;
		 			if(diffDate < 0) {
		 				document.getElementById('basicModalCommon').innerHTML = "Netbanking transactions can only be refunded after 2 days of authorisation !! ";
		 				Commondialog('OK', 'OK',  $(this));
						return false;
					}
				document.getElementById("netBankingRefundAmount").value = document.getElementById("amount").value;
				document.netBankingRefund.submit();
			}else{	
				// All Other Card & Netbaning Refund Transaction Process with RefundRequest.java
				var token  = document.getElementsByName("token")[0].value;
			//to show new loader -Harpreet
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
					url : 'refundRequestAction',
					type : "POST",
					timeout: 0,
						data : {
							origTxnId : '<s:property value="transDetails.origTxnId"/>' ,						
							currencyCode:'<s:property value="transDetails.currencyCode"/>',
							amount : document.getElementById("amount").value,
							payId : '<s:property value="payId"/>',
							orderId: '<s:property value="transDetails.orderId" />',
							acqId: '<s:property value="aaData[0].acqId" />',
							custEmail : '<s:property value="transDetails.custEmail" />',
							token:token,
						    "struts.token.name": "token",
						},
							
							success:function(data){		       	    		
								document.getElementById('basicModalCommon').innerHTML = data.response;
								Commondialog('OK', 'OK', $(this));	
			          		},
								error:function(data){	
									document.getElementById('basicModalCommon').innerHTML = "Network error, please try again later!! Sorry for inconvenience";
									Commondialog('OK', 'OK', $(this));	
			           	    		
								}
							});		 
			            }
		
		      $(this).dialog("close");
		  };
		  btns[btnNoText] = function(){ 
				 $(this).dialog("close");
		  };
		  $("#basicModal" ).dialog({
		    autoOpen: true, 
		    title:"Confirm ?",
		    modal:true,
		   	buttons:btns,
		    position: {
	               my: 'top',
	               at: 'top'
	             }
		  });
		}
	 
	 
	 function Commondialog(btnYesText, btnNoText, element){
		 var btns = {};
		 btns[btnYesText] = function(){ 
			 $('#basicModalCommon').remove();
			 //hotfix for firefox
			 if(navigator.userAgent.match('Firefox')!=null){
				window.location.reload();
			 }else{
				// hotfix for safari reload issue with post data
		         	var transactionId = <s:property value="transDetails.origTxnId"/>;
		         	var payId = <s:property value="payId"/>;
		         	var orderId = '<s:property value="transDetails.orderId" />';
		         	var token1 = document.getElementsByName("token")[0].value;
		         	var formHtml ='<form id="refuDetailsReload" name="refuDetailsReload"  action="refundConfirmAction" method="post"> <input type="hidden" name="transactionId" value='+transactionId+'><input type="hidden" name="payId" value='+payId+'><input type="hidden" name="orderId" value='+orderId+'><input type="hidden" name="token" value='+token1+'> <input type="submit" ></form>';
		         	$('#refuDetailsReloadDiv').append(formHtml);
		         	$(formHtml).submit();
			 }
			 
			
		 }
		  $("#basicModalCommon" ).dialog({
			    autoOpen: true, 
			    title:"Information",
			    modal:true,
			    buttons:btns,
			    dialogClass: "no-close",
			    position: {
		               my: 'top',
		               at: 'top'
		             }
			  });
		
	 }
		 
	 //refundButton
	$('#refundSubmit').click(function() {
		var availableAmount = parseFloat(parseFloat(<s:property value="transDetails.availableRefundAmount"/>).toFixed(2));
		var refundAmount = parseFloat(parseFloat(document.getElementById("amount").value).toFixed(2));	
		if(availableAmount < refundAmount) {
			document.getElementById('basicModalCommon').innerHTML = "Refund amount should be less than available amount !!";
			Commondialog('OK', 'OK', $(this));	
			return false;
		}
		else{
			var answer = "Do you want to Refund <s:property value="transDetails.currencyNameCode"/>- : "+refundAmount+" for Txn_ID # <s:property value="payId"/> ?";
			document.getElementById('basicModal').innerHTML = answer;
			yesnodialog('Yes', 'No', $(this));
		}
	 		
});
	 
	function hideButton(){
		var button = document.getElementById("refundSubmit");
		var availableAmount = parseFloat(parseFloat(<s:property value="transDetails.availableRefundAmount"/>).toFixed(2));
		var refundAmount = parseFloat(parseFloat(document.getElementById("amount").value).toFixed(2));			
		if(availableAmount < refundAmount || availableAmount==0) {
			button.style.display="none";
				}
	}
	   });
</script>
<script>
    function isNumber(evt) {
        var iKeyCode = (evt.which) ? evt.which : evt.keyCode
        if (iKeyCode != 46 && iKeyCode > 31 && (iKeyCode < 48 || iKeyCode > 57)){
            return false;
        }
        return true;
    }    
</script>

</head>
<body>

<div id="basicModal">
</div>
<div id="basicModalCommon">
</div>
	<table width="100%" border="0" align="center" cellpadding="0"
		cellspacing="0" class="txnf">
		<tr>
			<td height="20" colspan="3" align="center" valign="top"><h2>
					Order ID:
					<s:property value="orderId" />
				</h2></td>
		</tr>
		<tr>
			<td height="20" colspan="3" align="center" valign="top">
				<div class="scrollD">
					<table width="98%" border="0" cellspacing="0" cellpadding="0"
						class="product-spec">
						<tr>
							<th height="30" align="left">Transaction No.</th>
							<th height="30" align="left">Date</th>
							<th align="left">Type</th>
							<th align="left">Amount</th>
							<th align="left">Status</th>
							<th height="30" align="left">Bank Reference</th>
						</tr>
						<s:iterator value="oldTransactions">
							<tr>
								<td height="30" align="left" class="txtnew"><s:property
										value="txnId" /></td>
								<td align="left" class="txtnew"><s:property
										value="createDate" /></td>
								<td align="left" class="txtnew"><s:property value="txnType" /></td>
								<td align="left" class="txtnew"><s:property value="amount" /></td>
								<td align="left" class="txtnew"><s:property value="status" /></td>
								<td align="left" class="txtnew"><s:property value="acqId" /></td>
							</tr>
							<s:if
								test='%{txnType.equalsIgnoreCase("Refund") || status.equalsIgnoreCase("Pending")}'>
								<s:set var="refundButton" value="true" />

							</s:if>
						</s:iterator>
						<s:if test="%{refundButton}"></s:if>

						<s:else>
							<s:set var="refundButton" value="flase" />
						</s:else>

						<s:hidden name="flagValue" value="%{refundButton}" id="buttonFlag" />
					</table>
				</div>
			</td>
		</tr>
		<tr>
			<td colspan="3" height="40" align="center">&nbsp;</td>
		</tr>
		<tr>
			<td colspan="3" align="center"><table width="98%" border="0"
					cellspacing="0" cellpadding="0" class="txnf">
					<tr>

						<td width="21%" align="left"><h2>Refund</h2></td>
					</tr>
					<tr>
						<td align="center" colspan="2">
							<div class="avai">
								<table width="100%" border="0" cellspacing="0" cellpadding="0"
									class="greyroundtble">
									<tr>

										<td width="42%" height="30" align="right" valign="middle"
											class='txtnew'><b>Available refund amount:</b></td>
										<td width="3%" align="left" valign="middle">&nbsp;</td>
										<td width="27%" align="left" valign="middle"><div
												class="txtnew">
												<s:property value="transDetails.availableRefundAmount" />
											</div></td>
										<td width="23%" align="left" valign="middle">&nbsp;</td>

									</tr>
									<tr>
										<td height="30" align="right" valign="middle" class='txtnew'><b>Amount:</b></td>
										<td align="right" valign="middle">&nbsp;</td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:textfield name="amount" class="signuptextfield"
													id="amount" onkeypress="javascript:return isNumber (event)"
													value="%{transDetails.availableRefundAmount}"
													autocomplete="off"></s:textfield>
											</div></td>
										<td align="left" valign="top"><s:submit
												class="btn btn-success" align="left" id="refundSubmit" 
												value="Refund"></s:submit></td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td height="20" colspan="2" align="center" valign="top">&nbsp;&nbsp;<span
							class="categories">* Successful refund depends on
								availability of funds and settlement cycle.</span></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan="3" align="left" valign="top">&nbsp;</td>
		</tr>
		<tr>
		<td colspan="3" align="left" valign="top">
		<s:if
						test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<div class="clear"><button type="button" onclick="callChargeback();" class="btn btn-success btn-md" style="margin-left: 87%;
					    margin-top: -1px;" >Raise Chargeback</button></div>
					</s:if>
		</td>
		</tr>
		
		<tr>

			<td colspan="3" align="left" valign="top"><div class="adduT">
					<div class="readp">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							class="product-spec">
							<tr>
								<th height="30" colspan="2" align="left">Order Details</th>
							</tr>
							<tr>
								<td width="33%" height="30" align="left">Order ID:</td>
								<td width="66%" align="left" class="txtnew"><s:property
										value="transDetails.orderId" /></td>
							</tr>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">Date:</td>
								<td align="left" bgcolor="#F2F2F2"><s:property
										value="transDetails.createDate" /></td>
							</tr>
							<tr>
								<td height="30" align="left">Merchant Name:</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.businessName" /></td>
							</tr>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">Card Number
									Mask:</td>
								<td align="left" bgcolor="#F2F2F2"><s:if
										test="%{transDetails.cardNumber !=null}">
										<s:property value="transDetails.cardNumber" />
									</s:if>
									<s:else>Not applicable</s:else></td>
							</tr>
							<tr>
								<td height="30" align="left">Payment Method:</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.paymentType" />&nbsp;(<s:property
										value="transDetails.mopType" />)</td>
							</tr>
							<tr>
								<td height="30" align="left">Card Issuer Info :</td>
								<td align="left" class="txtnew"><s:if
										test="%{transDetails.internalCardIssusserBank !=null}">
										<s:property value="transDetails.internalCardIssusserBank" />
										<s:if
											test="%{transDetails.internalCardIssusserCountry !=null}">&nbsp;(<s:property
												value="transDetails.internalCardIssusserCountry" />)</s:if>
									</s:if>
									<s:else>Not applicable</s:else></td>
							</tr>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">Customer
									Email:</td>
								<td align="left" bgcolor="#F2F2F2"><s:property
										value="transDetails.custEmail" /></td>
							</tr>
							<%-- <tr>
								<td height="30" align="left">IP Address:</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.internalCustIP" /></td>
							</tr> --%>
							<%-- <tr>
								<td height="30" align="left" class='greytdbg'>Country</td>
								<td class='greytdbg' align="left"><s:property
										value="transDetails.internalCustCountryName" /></td>
							</tr> --%>
						</table>
						<div class="clear"></div>
					</div>

					<div class="readp">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							class="product-spec">
							<tr>
								<th colspan="2" height="30" align="left" bgcolor="#eef8ff">Amount
									Summary</th>
							</tr>
							<tr>
								<td width="49%" height="30" align="left">Currency:</td>
								<td width="50%" align="left" class="txtnew"><s:property
										value="transDetails.currencyNameCode" /></td>
							</tr>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">Authorized
									Amount [A]</td>
								<td align="left" bgcolor="#F2F2F2"><s:property
										value="transDetails.authorizedAmount" /></td>


							</tr>
							<tr>
								<td height="30" align="left">Captured Amount [B]</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.capturedAmount" /></td>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">Chargeback
									Amount [C]</td>
								<td align="left" bgcolor="#F2F2F2"><s:property
										value="transDetails.chargebackAmount" /></td>
							</tr>
							<tr>
								<td height="30" align="left">Fixed Txn Fee [D]</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.fixedTxnFee" /></td>
							</tr>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">TDR (<s:property
										value="transDetails.merchantTDR" />% of B + D) [F]
								</td>
								<td align="left" bgcolor="#F2F2F2"><s:property
										value="transDetails.tdr" /></td>
							</tr>
							<tr>
								<td height="30" align="left">Service Tax (<s:property
										value="transDetails.percentecServiceTax" />% of F) [G]
								</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.serviceTax" /></td>
							</tr>
							<tr>
								<td height="30" align="left" bgcolor="#F2F2F2">Net Amount[B
									- (D+F+G)]</td>
								<td align="left" bgcolor="#F2F2F2"><s:property
										value="transDetails.netAmount" /></td>
							</tr>
							<tr>
								<td height="30" align="left">Refunded Amount</td>
								<td align="left" class="txtnew"><s:property
										value="transDetails.refundedAmount" /></td>
							</tr>
						</table>
					</div>
					
					</div>
					
				</td>
		</tr>
		<tr>
			<s:form id="netBankingRefund" action="netBankingRefund"
				autocomplete="off">
				<s:hidden name="origTxnId" value="%{transDetails.origTxnId}" />
				<s:hidden name="currencyCode" value="%{transDetails.currencyCode}" />
				<s:hidden name="amount" id="netBankingRefundAmount" value="" />
				<s:hidden name="payId" value="%{payId}" />
				<s:hidden name="orderId" value="%{transDetails.orderId}" />
				<s:hidden name="createDate" value="%{transDetails.createDate}" />
				<s:hidden name="txnId" value="%{transDetails.origTxnId}" />
				<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			</s:form>
			
		<s:form name="chargeback" action="chargebackAction">
		<s:hidden name="orderId" value="%{transDetails.orderId}" />
		<s:hidden name="payId" value="%{payId}" />
		<s:hidden name="txnId" value="%{transDetails.origTxnId}" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
		</tr>
	</table>
	</body>
</html>