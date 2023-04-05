<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Logout</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../fonts/css/font-awesome.css" rel="stylesheet" type="text/css" />
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
	<table width="30%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr>
			<td align="center" valign="bottom" height="70">&nbsp;</td>
		</tr>
		<tr>
			<td><s:div class="signupbox">
					<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" class=""></td>
						</tr>
						<tr>
							<td align="center">&nbsp;</td>
						</tr>
						<tr>
						<td align="center"><span style="font-size: 10em; font-weight: normal; color: #f65d5d; line-height: 30px;"><i class="fa fa-exclamation-triangle" style="font-size: 0.5em;"></i></span><br />
						<span style="font-size: 26px; font-weight: normal; color: #333; line-height: 30px;"> Your session has been expired.</span>
						
						</td>
						</tr>
						<tr>
							<td align="center"><table width="98%" border="0" align="center" cellpadding="0"
									cellspacing="0">
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