<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<title>Reseller Mapping</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script type="text/javascript">

$(document).ready(function() {
	$('#reseller').change(function(event){
		 var reseller = $("select#reseller").val();
		 if(reseller==null ||reseller==""){
			return false; 
		 }else {
		 	handleList();
		 }
	});

});
	function resellerMapping() {
		var emailId = document.getElementById("emailId").value;;
		var reseller = document.getElementById("reseller").value; ;
		var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
		var token = document.getElementsByName("token")[0].value;		
		
		 $.ajax({
			type: "POST",
			url:"resellers",
			data:{"emailId":emailId, "reseller":reseller, "userType":userType, "token":token,"struts.token.name": "token",},
			success:function(data){
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);			
				}
				//TODO....clean values......using script to avoid page refresh
				window.location.reload();
		    },
			error:function(data){
				alert("Network error, mapping may not be saved");
			}
		    
		}); 
	}
	
	function handleList(){
		var reseller = document.getElementById("reseller").value; ;
		var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
		var token = document.getElementsByName("token")[0].value;	
		
		 $.ajax({
				type: "POST",
				url:"mappedMerchantList",
				data:{"reseller":reseller, "userType":userType, "token":token,"struts.token.name": "token",},
				success:function(data){
					var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
					if(null!=response){
						//alert(response);			
					}
					//TODO....clean values......using script to avoid page refresh
					//window.location.reload();
			    },
				error:function(data){
					alert("Network error, request not processed");
				}
			    
			}); 
	}
</script>

<style>
.save-button{
    height: 38px;
    width: 100px;
}
.MerchBx{
	margin-left: 100px !important;
}
.product-spec{
	padding: 5px;
}
.persenrBtn{
	float: left;
    margin-left: 13px;
}

</style>

</head>
<body>
<div class="txnf">
 	<s:form >
		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			>
			<tr>
				<td width="21%"><h2>Reseller Mapping</h2>
					<s:actionmessage class="success success-text" /></td>
			</tr>
			<tr>
				<td align="center" valign="top"><table width="98%" border="0"
						cellspacing="0" cellpadding="0">
						<tr>
							<td align="left">&nbsp;</td>
						</tr>
						<tr>
						  <td align="left"><div class="MerchBx">
                          <div class="persenr"><div
												class="txtnew">
													<s:select headerValue="Select Reseller" headerKey=""
													name="reseller" class="form-control" id="reseller" style="height:40px;"
													list="listReseller" listKey="emailId"
													listValue="businessName" autocomplete="off"/>
											</div></div>
							<div class="persenr"><div
												class="txtnew">
													<s:select headerValue="Select Merchant" headerKey=""
													name="emailId" class="form-control" id="emailId" style="height:40px;"
													list="listMerchant" listKey="emailId"
													listValue="businessName" autocomplete="off"/>
											</div></div>					
                          <div class="persenrBtn"><div>
											<input type ="button" value="Save" class="btn btn-small btn-success save-button" onclick ="resellerMapping()" cssClass="signupbutton btn-primary" >
											</div></div>
                          <div class="clear"></div>
                          </div>
                          </td>
				  </tr>
			</table></td>
			</tr>
		</table>
	
		<h4 class="subHeading">Mapped Merchant List</h4>
												<table width="100%" border="0" align="center"
													class="product-spec">
													<tr class="boxheading">
														<th width="4%" align="left" valign="middle">Business Name</th>	
														<th width="6%" align="left" valign="middle">first Name</th>
														<th width="4%" align="left" valign="middle">Last Name</th>
														<th width="5%" align="left" valign="middle">PayId</th>
														<th width="5%" align="left" valign="middle">Email Id</th>
													</tr>
													<s:iterator value="merchantList" >
														<tr class="boxtext">
															<td align="left" valign="middle" ><s:property
																	value="businessName" /></td>
															<td align="left" valign="middle"><s:property
																	value="firstName" /></td>
															<td align="left" valign="middle"><s:property
																	value="lastName" /></td>
															<td align="left" valign="middle"><s:property
																	value="payId" /></td>
															<td align="left" valign="middle"><s:property
																	value="emailId" /></td>	
														</tr>
													</s:iterator>
												</table>
										<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
</div>
</body>
</html>