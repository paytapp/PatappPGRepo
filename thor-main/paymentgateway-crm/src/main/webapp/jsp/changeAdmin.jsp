<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<title>Change Admin</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<script type="text/javascript">
function changeAdmin(divId,curr_row,ele){
	var token = document.getElementsByName("token")[0].value;
	var adminEmailId = document.getElementById('adminEmailId').value;
	var emailId = document.getElementById('subAdminEmailId').innerHTML;
	$("body").removeClass("loader--inactive");
			$.ajax({
				type: "POST",
				url:"changeAdmin",
				timeout: 0,
				data:{"adminEmailId":adminEmailId,"emailId":emailId,"token":token,"struts.token.name": "token",},
				success: function (data) {
					var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
					if(null!=response){
						alert(response);			
					} else if(null == response){
						alert("Please enter Admin Email Id");
					}

					setTimeout(function() {
						$("body").removeClass("loader--inactive");						
					}, 1000);
					//TODO....clean values......using script to avoid page refresh
					window.location.reload();
			    },
				error:function(data){
					window.location.reload();
					alert("Network error, process may not be completed");
				}
			});
}
</script>
<style>
table tr td{color: #000;}
.txnf h2{text-align: right!important;margin-right: -29%!important;}
.btn:focus{
		outline: 0 !important;
	}
</style>
</head>
<body>
<form>
<table width="70%" border="0" cellspacing="0" cellpadding="0" align="center" class="txnf product-specbigstripes" style="margin-top:2%;">
			<tr>
				<td style="border-right: 0px solid #fff;"><div><h2>Change Admin</div></h2></td>
			</tr>
			<!--<tr>
			<td align="left">
			<div class="container">
			<div class="form-group col-md-4 txtnew col-sm-4 col-xs-6">

			</div>
			</div>
			</td>
			</tr>-->
			<tr>
			<td height="30" width="20%" align="left" valign="middle" class="greytdbg"><strong>SubAdmin Email:</strong></td>
			<td id="subAdminEmailId" align="left" width="20%" style="border-top: 1px solid #ddd;" class="greytdbg"><s:property value="%{emailId}"></s:property></td>
			</tr>
			<tr>
			<td height="30" width="20%" align="left" valign="middle"><strong>First Name:</strong></td>
			<td align="left" width="20%"><s:property value="%{firstName}"></s:property></td>
			</tr>
			<tr>
			<td height="30" width="20%" align="left" valign="middle" class="greytdbg"><strong>Last Name:</strong></td>
			<td align="left" width="20%" class="greytdbg"><s:property value="%{lastName}"></s:property></td>
			</tr>
			<tr>
			<td height="30" width="20%" align="left" valign="middle"><strong>Admin Email Id:</strong></td>
			<td align="left" width="20%"><input type="text" name="adminEmailId" id ="adminEmailId" class="form-control" style="width: 50%;"></td>		</tr>
			<tr>
			<td align="center" style="border-right: 0px solid #fff;">
			<input type="button" class="btn btn-md btn-success" value="Submit" onclick="changeAdmin()" style="text-align: left;margin-left: 93%;padding: 5px 12px 5px 12px;">
			</td>
			</tr>
  </table>
</form>
</body>
</html>