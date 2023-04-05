<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Email Validation</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
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
	<table width="50%" border="0" align="center" cellpadding="0"
		cellspacing="0">
		<tr>
			<td align="center" valign="bottom" height="120">&nbsp;</td>
		</tr>
		<tr>
			<td><s:div class="signupbox">
					<table width="100%" border="0" align="center" cellpadding="0"
						cellspacing="0">
						<tr>
							<td align="center"></td>
						</tr>
						<tr>
							<td align="center">&nbsp;</td>
						</tr>
						<tr>
							<td align="center"><span
								style="font-size: 34px; font-weight: bold; color: #2b8de9; line-height: 60px;">Failed!</span><br />
								<span style="font-size: 20px; font-weight: bold; color: #333;">Your
									Email link has been expired.<br>Please <s:a action="index">click here </s:a>
									to login
							</span><br /> <span
								style="font-size: 18px; font-weight: bold; color: #333; line-height: 40px;"></span></td>
						</tr>
						<tr>
							<td align="center"><table width="98%" border="0"
									align="center" cellpadding="0" cellspacing="0">
									<tr>
										<td align="left" style="border-bottom: 1px solid #ececec;">&nbsp;</td>
									</tr>
								</table></td>
						</tr>
						<tr>
							<td align="center">&nbsp;</td>
						</tr>
						<tr>
							<td align="center"><img src="../image/lpay.png" /></td>
						</tr>
						<tr>
							<td align="center">&nbsp;</td>
						</tr>
					</table>
				</s:div>
		</tr>
	</table>
</body>
</html>
