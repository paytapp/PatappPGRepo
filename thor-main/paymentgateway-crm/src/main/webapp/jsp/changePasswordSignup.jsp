<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Change Password</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/profile-page.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery.easing.js"></script>
<script type="text/javascript" src="../js/jquery.dimensions.js"></script>
<script type="text/javascript" src="../js/jquery.accordion.js"></script>
<script>
	if (self == top) {
		var theBody = document.getElementsByTagName('body')[0];
		theBody.style.display = "block";
	} else {
		top.location = self.location;
	}
</script>
<script type="text/javascript">
	jQuery().ready(function() {
		// simple accordion
		jQuery('#list1a').accordion();
		jQuery('#list1b').accordion({
			autoheight : false
		});

		// second simple accordion with special markup
		jQuery('#navigation').accordion({
			active : false,
			header : '.head',
			navigation : true,
			event : 'click',
			fillSpace : false,
			animated : 'easeslide'
		});
	});
</script>
</head>
<body class="profilebg">

<div class="blueback">
     <div class="bluebackL"><table width="100%" border="0"
								cellspacing="0" cellpadding="0">
								<tr class="tdhide">
									<td align="center" valign="top"><br />
									<img src="../image/profile-logo.png" /></td>
								</tr>
								<tr>
									<td align="left" valign="top">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" valign="top"><div id="main">
											<div>
												<ul id="navigation">
													<li><s:a action='home' class="head1 myprofile">My Profile</s:a></li>
													<li><s:a action='passwordChangeSignUp'
															class="head1 changepassword">Change Password</s:a></li>
												</ul>
											</div>
										</div></td>
								</tr>
								<tr>
									<td align="left" valign="top">&nbsp;</td>
								</tr>
							</table></div>
     <div class="rightblu"><table width="98%" border="0"
								cellspacing="0" cellpadding="0">
								<tr>
									<td align="left" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">
											<tr>
												<td align="left" valign="top" class="welcometext">Change
													Password</td>
												<td align="right" valign="top"><s:a action="logout"
														class="btn btn-danger">
														<span class="glyphicon glyphicon-log-out"></span> Log out</s:a>

												</td>
											</tr>
										</table></td>
								</tr>
								<tr>
									<td align="left" valign="top" class="borderbottomgrey">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" valign="top">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" valign="top"><em><strong>Password
												Criteria:</strong> Password must be minimum 6 and maximum 32
											characters long, with special characters (! @ , _ + / =) , at
											least one upper case and one lower case alphabet. Your new
											password must not be the same as any of your last four
											passwords.</em></td>
								</tr>
								<tr>
									<td align="left" valign="top">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" valign="top"><s:form
											action="changePasswordSignup" autocomplete="off">
											<table width="100%" border="0" align="center" cellpadding="0"
												cellspacing="0">

												<tr>
													<td align="center">&nbsp;</td>
													<td height="10" align="center">
														<table width="100%" align="center" border="0"
															cellspacing="0" cellpadding="0" class="profilepage">
															<tr>
																<td width="24%" height="25" align="left"
																	class="bluetdbg"><strong>Old Password:</strong></td>
																<td width="76%" align="left"><s:textfield
																		name="oldPassword" type="password"
																		cssClass="inputfield"  autocomplete="off"/></td>
															</tr>
															<tr>
																<td height="30" align="left" class="bluetdbg"><strong>New
																		Password:</strong></td>
																<td align="left"><s:textfield name="newPassword"
																		type="password" cssClass="inputfield"  autocomplete="off"/></td>
															</tr>
															<tr>
																<td height="30" align="left" class="bluetdbg"><strong>Confirm
																		New Password:</strong></td>
																<td align="left"><s:textfield
																		name="confirmNewPassword" type="password"
																		cssClass="inputfield"  autocomplete="off"/></td>
															</tr>
															<tr>
																<td height="30" align="left" class="bluetdbg">&nbsp;</td>
																<td align="left" style="float: left;"><s:submit
																		value="Save" class="btn btn-success" /></td>
															</tr>
															<!--<tr>
                            <td height="30" align="right" class="text1">&nbsp;</td>
                            <td align="right">&nbsp;</td>
                            <td align="right" style="padding-right:20px">
                             <s:submit	action="checkUser" value="Edit"	cssClass="btn btn-success"> </s:submit> 
                            </td>
                          </tr>                          
                          <tr>
                            <td height="30" align="right" class="text1">&nbsp;</td>
                            <td align="right">&nbsp;</td>
                            <td align="right" class="text1"></td>
                          </tr>-->
														</table>
													</td>
												</tr>
												<tr>
													<td>&nbsp;</td>
													<td>&nbsp;</td>
												</tr>
											</table>
										</s:form></td>
								</tr>
								<tr>
									<td align="left" valign="top">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" valign="top">&nbsp;</td>
								</tr>
							</table></div>
     <div class="clear"></div>
     </div>
</body>
</html>