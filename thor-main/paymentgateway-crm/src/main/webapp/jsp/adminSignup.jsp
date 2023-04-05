<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Sign Up</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/captcha.js"></script>
<link href="../css/fonts.css"  />
<script>
	if (self == top) {
		var theBody = document.getElementsByTagName('body')[0];
		theBody.style.display = "block";
	} else {
		top.location = self.location;
	}
</script>
</head>
<body>
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td align="center" valign="top"><table width="100%" border="0"
						align="center" cellpadding="0" cellspacing="0" class="txnf">
          <tr>
            <td align="left"><h2 style="margin-bottom:0px;">Create A New Account</h2></td>
          </tr>    
          <tr>
            <td align="left"><s:actionmessage class="success success-text" theme="simple"/></td>
          </tr>
          <tr>
            <td align="left" valign="top"><s:form action="signupAdmin" id="formname" >
                  <s:token/>
                  <div class="adduR">
                  <span id="error2" style="color:#ff0000; font-size:11px;"></span>
                <div class="adduTR">Admin Name<br>
                  <div class="txtnew">
                   <s:textfield	id="businessName" name="businessName" cssClass="signuptextfield" placeholder="Business Name" autocomplete="off" onkeypress="return ValidateBussinessName(event);"/>
                  </div>
                </div>
                <div class="adduTR">Email<br>
                  <div class="txtnew">
                    <s:textfield id="emailId" name="emailId" cssClass="signuptextfield" placeholder="Email" autocomplete="off" onblur="isValidEmail()"/>
                  </div>
                </div>
                <div class="adduTR">Phone<br>
                  <div class="txtnew">
                    <s:textfield id="mobile" name="mobile" cssClass="signuptextfield" placeholder="Phone" autocomplete="off" onkeypress="javascript:return isNumber (event)"/>
                  </div>
                </div>
                <div class="adduTR">Password<br>
                  <div class="txtnew">
                    <s:textfield id="password" name="password" type="password" cssClass="signuptextfield" placeholder="Password" onblur="passCheck()" autocomplete="off"/>
                  </div>
                </div>
                <div class="adduTR">Confirm Password<br>
                  <div class="txtnew">
                    <s:textfield	id="confirmPassword" name="confirmPassword" type="password" cssClass="signuptextfield" placeholder="Confirm Password" onblur="passCheck()" autocomplete="off"/>
                  </div>
                </div>
                <div class="adduTR" style="text-align:center; padding:14px 0 0 0;">
                  <s:submit value="Sign Up" method ="submit" cssClass="signupbutton btn-primary"> </s:submit>
                  <br>
                </div>
                <div class="clear"></div>
              </div>
              </s:form></td>
          </tr>
          <tr>
            <td align="center" valign="top">&nbsp;</td>
          </tr>
        </table></td>
    </tr>
  </table>
  <script>
			$(document).ready(function(){
				
				var fields = {
						
						password : {
							tooltip: "Password must be minimum 8 and <br> maximum 32 characters long, with <br> special characters (! @ , _ + / =) , <br> at least one uppercase and  one <br>lower case alphabet.",
							position: 'right',
							backgroundColor: "#6ad0f6",
							color: '#FFFFFF'
							},
						};
				
				//Include Global Color 
				$("#formname").formtoolip(fields, { backgroundColor: "#000000" , color : "#FFFFFF", fontSize : 14, padding : 10, borderRadius :  5});
					
				});
</script>
</body>
</html>