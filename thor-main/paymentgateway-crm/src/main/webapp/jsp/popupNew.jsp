<%@ page language="java" contentType="text/html; charset=ISO-8859-1"	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<link rel="stylesheet" type="text/css" href="../css/popup.css" />
<script>
$('#amount').on('keyup', function() {
    if (this.value[0] === '.') {
        this.value = '0'+this.value;
    }
});
$('#refundSubmit').click(function() {
	var mopType = '<s:property value="transDetails.mopType" />';
	 if(mopType!="Visa" && mopType!="Diners" && mopType!="MasterCard" && mopType!="Amex" && mopType!="Maestro" && mopType!="EzeeClick" && mopType!="MobikwikWallet" ){
		var createDate = new Date('<s:property value="transDetails.createDate"/>');
		createDate.setDate(createDate.getDate() + 2); 
		var currentDate = new Date();
		var diffDate = new Date();
		diffDate = currentDate - createDate;
		if(diffDate < 0) {
			alert("Netbanking transactions can only be refunded after 2 days of authorisation !! ");
			return false;
		}
	} 			
	var availableAmount = parseFloat(parseFloat(<s:property value="transDetails.availableRefundAmount"/>).toFixed(2));
	var refundAmount = parseFloat(parseFloat(document.getElementById("amount").value).toFixed(2));			
	if(availableAmount < refundAmount) {
		alert("Refund amount should be less than available amount!!");
		return false;
	}
	else {	
		var refundAmount = parseFloat(parseFloat(document.getElementById("amount").value).toFixed(2));	
		var answer = confirm("Do you want to Refund <s:property value="transDetails.currencyNameCode"/>-"+refundAmount+" for Txn_ID # <s:property value="payId"/> ?");
		if (answer != true) {
			return false;
		}
	}

	var acquirer  = '<s:property value="transDetails.acquirerCode"/>';
	
	if(mopType!="Visa" && mopType!="Diners" && mopType!="MasterCard" && mopType!="Amex" && mopType!="Maestro" 
		&& mopType!="EzeeClick" && mopType!="MobikwikWallet" && acquirer!="KOTAK" && acquirer!="YESBANK"){
	document.getElementById("netBankingRefundAmount").value = document.getElementById("amount").value;
	document.netBankingRefund.submit();
}else{	
	var token  = document.getElementsByName("token")[0].value;
	 $.ajaxSetup({
            global: false,
            beforeSend: function () {
               $(".modal").show();
            },
            complete: function () {
                $(".modal").hide();
            }
        });
 $.ajax({
		url : 'refundRequestAction',
		timeout: 0,
		type : "POST",
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
       	   		 //var res = data.response;		       	    		
       	   		  alert("Refund processed successfully order ID:'<s:property value="transDetails.orderId" />'");
            		window.location.reload();
          		},
					error:function(data){	
           	    		alert("Refund request initiated successfully. It will process in next 8hr. Sorry for inconvenience");
           	    		window.location.reload();
					}
				});		 
            }
      });
    function isNumber(evt) {
        var iKeyCode = (evt.which) ? evt.which : evt.keyCode
        if (iKeyCode != 46 && iKeyCode > 31 && (iKeyCode < 48 || iKeyCode > 57)){
            return false;
        }
        return true;
    }    
</script>
<!-- Modal -->
<div class="modal-dialog"  style="background-color:#efeeee;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="row headgpop text-center">Transaction <s:property value="status" /></div>
		<div class="row">
		<div class="col-md-6 col-xs-12 form-group">
		<table class="detailbox table98">
							<tr>
								<th colspan="2" align="left" valign="middle" class="newpopuphead">General</th>
							</tr>
							<tr>
							  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Merchant Name :</strong></td>
							 <td bgcolor="#FFFFFF"><div id="param-custName">
										<s:property value="%{aaData.custName}" />
									</div></td>
  </tr>
						
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Status :</strong></td>
								<td bgcolor="#FFFFFF"><s:property
										value="status" /></td>
							</tr>
							<tr>
							  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Order ID:</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="orderId" /></td>
							</tr>
							<%-- <s:if test="%{transactionAuthenticationFlag == true}"> --%>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Transaction ID :</strong></td>
									<td bgcolor="#FFFFFF"><s:property
										value="transactionId" /> <s:property value="txnId" /></td>
								</tr>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Product Description :</strong></td>
								  <td bgcolor="#FFFFFF"><s:property value="productDesc" /></td>
							  </tr>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Amount :</strong></td>
								  <td bgcolor="#FFFFFF"><s:property value="amount" />-<s:property
										value="currency" /></td>
							  </tr>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Date :</strong></td>
								  <td bgcolor="#FFFFFF"><s:property value="datefrom" /></td>
							  </tr>	
							  <tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"> </td>
								  <td bgcolor="#FFFFFF">&nbsp;</td>
							  </tr>							
							<%-- </s:if> --%>
							</table>
							</div>
		<div class="col-md-6 col-xs-12 form-group">
		<table class="detailbox table98">
							<tr>
								<th colspan="2" align="left" valign="middle" class="newpopuphead">Payment Detail: </th>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Payment Mode :</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="paymentMethod" />&nbsp;(<s:property
										value="mopType" />)</td>
							</tr>
						<%-- 	<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Bank Name :</strong></td>
								<td bgcolor="#FFFFFF"><s:property
										value="internalCardIssusserBank" /></td>
							</tr> --%>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Card No. :</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="%{cardNumber}" /></td>
							</tr>
							<%-- <s:if test="%{transactionAuthenticationFlag == true}"> --%>
								<tr>
									<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Name on Card :</strong></td>
									<td bgcolor="#FFFFFF">XYZ</td>
								</tr>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Card Issuer Info :</strong></td>
								  <td bgcolor="#FFFFFF"><s:property
										value="internalCardIssusserBank" />&nbsp;(<s:property
										value="internalCardIssusserCountry" />)</td>
							  </tr>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>IP Address :</strong></td>
								  <td bgcolor="#FFFFFF">&nbsp;</td>
							  </tr>
								<tr>
								  <td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Country :</strong></td>
								  <td bgcolor="#FFFFFF">&nbsp;</td>
							  </tr>
							<%-- </s:if> --%>
							</table>
		</div>
		</div>
		<div class="row">
		<div class="col-md-4 col-xs-12 form-group"><table class="detailbox table98">
							<tr>
								<th colspan="2" align="left" valign="middle" class="newpopuphead">Customer Details</th>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Customer Name :</strong></td>
								<td bgcolor="#FFFFFF"><div id="param-custName">
										<s:property value="%{aaData.custName}" />
									</div></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Phone :</strong></td>
								<td bgcolor="#FFFFFF"><div id="param-custPhone">
										<s:property value="%{aaData.custPhone}" />
									</div></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Email. :</strong></td>
								<td bgcolor="#FFFFFF"><s:property
											value="custEmail" /></td>
							</tr>
							<s:if test="%{transactionAuthenticationFlag == true}">
								<tr>
									<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Customer
											Authentication</strong></td>
									<td bgcolor="#FFFFFF"><s:property
											value="%{aaData.internalTxnAuthentication}" /></td>
								</tr>
							</s:if>
							</table> </div>
		<div class="col-md-4 col-xs-12 form-group"><table class="detailbox table98">
							<tr>
								<th colspan="2" align="left" valign="middle" class="newpopuphead">Billing Address</th>
							</tr>
							<tr>
											<td height="90" align="left" valign="top"><div
													id="param-custStreetAddress1">
													<p>
														<s:property value="%{aaData.custStreetAddress1}" />
												</div>
												<div id="param-custStreetAddress2">
													<p>
														<s:property value="%{aaData.custStreetAddress2}" />
												</div>
												<div id="param-custCity">
													<p>
														<s:property value="%{aaData.custCity}" />
												</div>
												<div id="param-custState">
													<p>
														<s:property value="%{aaData.custState}" />
												</div>
												<div id="param-custCountry">
													<p>
														<s:property value="%{aaData.custCountry}" />
												</div>
												<div id="param-custZip">
													<p>
														<s:property value="%{aaData.custZip}" />
												</div></td>
										</tr>
						</table> </div>
		<div class="col-md-4 col-xs-12 form-group"><table class="detailbox table98">
							<tr>
								<th colspan="2" align="left" valign="middle" class="newpopuphead">Shipping Address</th>
							</tr>
							<tr>
											<td height="90" align="left" valign="top"><p>
												<div id="param-custShipStreetAddress1">
													<p>
														<s:property value="%{aaData.custShipStreetAddress1}" />
												</div>
												<div id="param-custShipStreetAddress2">
													<p>
														<s:property value="%{aaData.custShipStreetAddress2}" />
												</div>
												<div id="param-custShipCity">
													<p>
														<s:property value="%{aaData.custShipCity}" />
												</div>
												<div id="param-custShipState">
													<p>
														<s:property value="%{aaData.custShipState}" />
												</div>
												<div id="param-custShipCountry">
													<p>
														<s:property value="%{aaData.custShipCountry}" />
												</div>
												<div id="param-custShipZip">
													<p>
														<s:property value="%{aaData.custShipZip}" />
												</div></td>
										</tr>
						</table> </div>
		</div>
		<div class="row">
		<div class="col-md-12 col-lg-12 col-xs-12 form-group"><table class="detailbox table98">
							<tr>
								<th colspan="2" align="left" valign="middle" class="newpopuphead">Actions</th>
							</tr>
							<tr>
								<td align="center" valign="middle" height="50" bgcolor="#FFFFFF"><s:textfield  name="amount"  id="amount" autocomplete="off" cssClass="inputfield" theme="simple" placeholder="Refund Amount"  onkeypress="javascript:return isNumber (event)"/> &nbsp;<s:submit value="Partial Refund" id="refundSubmit" class="btn-success newround"></s:submit> <button type="button" id="buttonDay" class="btn-success newround">Full Refund</button> <button type="button" id="buttonDay" class="btn-success newround">View History</button></td>
							</tr>
							</table></div>
			<table class="table98">
				<tr>
					<td colspan="5" height="5" align="center" valign="top"></td>
				</tr>
				<tr>
					<td colspan="5" align="center" valign="top"></td>
				</tr>
				<tr>
					<td align="center" valign="top" height="5"></td>
				</tr>
				<tr>
					<td align="center" valign="top">
						<table class="table98">
							<tr>
								<td width="33%" align="center" valign="top" bgcolor="#FFFFFF">
									
								</td>
								<td width="33%" align="left" valign="top" bgcolor="#FFFFFF"></td>
								<td width="33%" align="right" valign="top" bgcolor="#FFFFFF"></td>
							</tr>
							<tr>
								<td colspan="3"><div class="button-position">
										<button class="popup_close closepopupbtn"></button>
									</div></td>
							</tr>

						</table>
					</td>
				</tr>
			</table>
		</div>
	</div>
</div></div>