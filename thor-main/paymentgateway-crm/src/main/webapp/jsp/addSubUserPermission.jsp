<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add SubUser</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<script src="../js/commanValidate.js"></script>
<script language="JavaScript">	
$(document).ready( function () {
	handleChange();
});
	function handleChange(){

   	  var str = '<s:property value="permissionString"/>';
   	  var buttonFlag = '<s:property value="disableButtonFlag"/>';
   	  if(buttonFlag=='true'){
   		 document.getElementById('submit').style.display='none';
   	  }
   	
   	  var permissionArray = str.split("-");
   	  for(j=0;j<permissionArray.length;j++){
			selectPermissions(permissionArray[j]);
		}
    }
	
	function selectPermissions(permissionType){		
		var permissionCheckbox = document.getElementById(permissionType);
		if(permissionCheckbox==null){
			return;
		}
		permissionCheckbox.checked = true;
	}
	
	function aaa(){
		var token = document.getElementsByName("token")[0].value;
		var emailId = document.getElementById('idtext').value;
		var lastName = document.getElementById('lastnametext').value;
		var firstName = document.getElementById('firstnametext').value;
		var phoneNo = document.getElementById('phonetext').value;
		var permission = document.getElementById('').value;
		
				$.ajax({
					type: "POST",
					url:"addSubAdmin",
					timeout: 0,
					data:{"emailId":emailId,"lastName":lastName,"firstName":firstName,"phoneNo":phoneNo,"token":token,"struts.token.name": "token",},
					success:function(data){
						var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
						if(null!=response){
							alert(response);			
						}
						//TODO....clean values......using script to avoid page refresh
						window.location.reload();
				    },
					error:function(data){
						window.location.reload();
						alert("Invalid Input");
					}
				});
				
		
	}
</script>
</head>
<body>
	<table width="100%" border="0" cellspacing="0" cellpadding="0"
		class="txnf">
		<tr>
			<td align="left"><h2>Add New SubUser</h2></td>
		</tr>
		<tr>
			<td align="left" valign="top"><div id="saveMessage">
					<s:actionmessage class="success success-text" />
				</div></td>
		</tr>
		<tr>
			<td align="left" valign="top"><div class="addu">
					<s:form action="" id="frmAddUser">
						<s:token />
						<div class="adduT">
							First Name<br>
							<s:textfield name="firstName" id="firstnametext" cssClass="signuptextfield"
								autocomplete="off" />
						</div>
						<div class="adduT">
							Last Name<br>
							<s:textfield name="lastName" id="lastnametext" cssClass="signuptextfield"
								autocomplete="off" />
						</div>
						<div class="adduT">
							Phone<br>
							<s:textfield name="mobile" id="phonetext" cssClass="signuptextfield"
								autocomplete="off"
								onkeypress="javascript:return isNumber (event)" />
						</div>
						<div class="adduT">
							Email<br>
							<s:textfield name="emailId" id="idtext" cssClass="signuptextfield"
								autocomplete="off" />
						</div>

						<div class="adduT">
							Account Privileges<br>

							<table width="100%" cellpadding="0" cellspacing="0"
								class="whiteroundtble">
								<s:iterator value="listPermissionType" status="itStatus">
									<tr>
										<td width="5%" align="left">&nbsp;</td>
										<td width="5%" height="30" align="left"><s:checkbox
												name="lstPermissionType" id="%{permission}"
												fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox></td>
										<td width="85%" align="left"><label class="labelfont"
											for="1"> <s:property value="permission" />
										</label></td>
									</tr>
								</s:iterator>
							</table>
							<div class="clear"></div>
						</div>

						<div class="adduT" style="padding-top: 10px">
							<%-- <s:submit id="submit" value="Create Agent" method="submit"
								cssClass="btn btn-success btn-md">
							</s:submit> --%>
							<input type="button" value="Submit" onclick="aaa()">
						</div>

						<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
					</s:form>
					<div class="clear"></div>
				</div></td>
		</tr>
		<tr>
			<td align="left" valign="top">&nbsp;</td>
		</tr>
	</table>
</body>
</html>