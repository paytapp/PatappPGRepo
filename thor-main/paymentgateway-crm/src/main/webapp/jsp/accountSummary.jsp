<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Account Summary</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<script src="../js/commanValidate.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	  $('#acquirer').change(function(event){  })});
</script>
</head>
<body>
<s:form action="accountSummary">
  <table width="100%"
						border="0" cellspacing="0" cellpadding="0" class="txnf">
    <tr>
      <td width="21%" height="30" align="left"><table width="100%"
									border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td align="center" width="75%" valign="middle"><h2>Select Account</h2></td>
            <td width="25%" align="left" valign="middle"><div style="padding:0 8px 0 0">
       <select name="frmAccountID" class="form-control">
                  <option value=""></option>
                  <option value="" selected="selected"><s:property value="%{#session.USER.businessName}" /></option>
                </select> 
              </div></td>
          </tr>
        </table></td>
    </tr>
    <tr>
      <td align="center">
      <div class="scrollD">
      <div class="adduT">
     <!-- IE < 10 does not like giving a tbody a height.  The workaround here applies the scrolling to a wrapped <div>. -->
<!--[if lte IE 9]>
<div class="old_ie_wrapper">
<!--<![endif]-->
 <table class="product-spec fixed_headers">
  <thead>
    <tr>
                <th height="30" align="left"><strong>Currency Code</strong></th>
                <th align="left"><strong>AcquirerName</strong></th>
                <th align="left"><strong>Payment Type
                  </strong></th>
                  <th align="left"><strong>Mop Type</strong></th>
                <th align="left"><strong>Transaction Type</strong></th>
                  <th align="left"><strong>Merchant TDR</strong></th>
                    <th align="left"><strong>Merchant FixCharge</strong></th>
                      <th align="left"><strong>Merchant ServiceTax</strong></th>
              </tr>
  </thead>
  <tbody>
    <s:iterator  value="chargingDetailsList">
		<tr>
		<td align="left" bgcolor="#f2f2f2"><s:property value="currency"/></td>
		 <td align="left" bgcolor="#f2f2f2"><s:property value="acquirerName"/></td>
		 <td align="left" bgcolor="#f2f2f2" ><s:property value="paymentType.name"/></td>
		 <td align="left" bgcolor="#f2f2f2" ><s:property value="mopType.name"/></td>
		 <td align="left" bgcolor="#f2f2f2" ><s:property value="transactionType"/></td>
	    <td align="left" bgcolor="#f2f2f2"><s:property value="merchantTDR"/></td>
	    <td align="left" bgcolor="#f2f2f2"><s:property value="merchantFixCharge"/></td>
	     <td align="left" bgcolor="#f2f2f2"><s:property value="merchantServiceTax"/></td>	
		 </tr>
	</s:iterator>
  </tbody>
</table>

<!--[if lte IE 9]>
</div>
<!--<![endif]-->            
            <div class="clear"></div>
          </div>
          </div>
          <div class="clear">&nbsp;</div>
          <div class="row adduT">
          <div class="form-group col-md-6 col-xs-12"><table border="0" cellspacing="0" cellpadding="0" width="100%" class="product-spec">
              <tr>
                <th colspan="2" align="left" bgcolor="#d1ebff">Processing	Configuration</th>
              </tr>
              <tr>
                <td width="40%" height="30" align="left"><strong>Account
                  ID:</strong></td>
                <td width="59%" align="left"><s:property
																									value="#session.USER.emailId" /></td>
              </tr>
              <tr>
                <td height="30" align="left" bgcolor="#F2F2F2"><strong>Date
                  Registered:</strong></td>
                <td align="left" bgcolor="#F2F2F2"><s:property
																									value="#session.USER.registrationDate" /></td>
              </tr>
              <tr>
                <td height="30" align="left"><strong>Currency:</strong></td>
                <td align="left">INR </td>
              </tr>
              <tr>
                <td height="30" align="left" bgcolor="#F2F2F2"><strong>Domain:</strong></td>
                <td align="left" bgcolor="#F2F2F2"><s:property
																									value="#session.USER.website" /></td>
              </tr>
              <tr>
                <td height="30" align="left"><strong>Settlement:</strong></td>
                <td align="left">T+2
                  Settlement</td>
              </tr>
              <tr>
                <td height="30" align="left" bgcolor="#F2F2F2"><strong>Live
                  Date:</strong></td>
                <td align="left" bgcolor="#F2F2F2"></td>
              </tr>
            </table></div>          
          <div class="form-group col-md-6 col-xs-12"><table border="0" cellspacing="0" cellpadding="0" width="100%" class="product-spec">
                                    <tr>
                                      <th colspan="2" align="left">Bank Info</th>
                                    </tr>
                                    <tr>
                                      <td width="40%" height="30" align="left"><strong>Account
                                        Holder Name:</strong></td>
                                      <td width="59%" align="left"><s:property value="#session.USER.accHolderName" /></td>
                                    </tr>
                                    <tr>
                                      <td height="30" align="left" bgcolor="#F2F2F2"><strong>Account
                                        No:</strong></td>
                                      <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.accountNo" /></td>
                                    </tr>
                                    <tr>
                                      <td height="30" align="left"><strong>Bank
                                        Name:</strong></td>
                                      <td align="left"><s:property value="#session.USER.bankName" /></td>
                                    </tr>
                                    <tr>
                                      <td height="30" align="left" bgcolor="#F2F2F2"><strong>IFSC Code:</strong></td>
                                      <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.ifscCode" /></td>
                                    </tr>
                                    <tr>
                                      <td height="30" align="left"><strong>Account Type:</strong></td>
                                      <td align="left"></td>
                                    </tr>
                                    <tr>
                                      <td height="30" align="left" bgcolor="#F2F2F2"><strong>Settlement Method:</strong></td>
                                      <td align="left" bgcolor="#F2F2F2"></td>
                                    </tr>
                                  </table></div>
          </div>
          <div class="row adduT">
          <div class="form-group col-md-12 col-xs-12">
          <table border="0" cellspacing="0" cellpadding="0" width="100%" class="product-spec">
                          <tr>
                            <th colspan="9" align="left" bgcolor="#d1ebff">Legal Business Info</th>
                          </tr>
                          <tr>
                            <td width="11%" height="30" align="left"><strong>Company
                              Name:</strong></td>
                            <td width="11%" align="left">                              <strong>Contact
                            Person:</strong></td>
                            <td width="11%" align="left"><strong>Email:</strong></td>
                            <td width="11%" align="left"><strong>Telephone:</strong></td>
                            <td width="11%" align="left"><strong>Address:</strong></td>
                            <td width="11%" align="left"><strong>City:</strong></td>
                            <td width="11%" align="left"><strong>County
                            / State / Province:</strong></td>
                            <td width="11%" align="left"><strong>Postal
                            Code:</strong></td>
                            <td width="11%" align="left"><strong>Country:</strong></td>
                          </tr>
                          <tr>
                            <td height="30" align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.companyName" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.contactPerson" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.contactPerson" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.telephoneNo" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.address" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.city" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.country" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.postalCode" /></td>
                            <td align="left" bgcolor="#F2F2F2"><s:property value="#session.USER.country" /></td>
                          </tr>
                        </table>
                        </div>
                        </div>
          </td>
    </tr>    
    
  </table>
  <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
</s:form>
</body>
</html>