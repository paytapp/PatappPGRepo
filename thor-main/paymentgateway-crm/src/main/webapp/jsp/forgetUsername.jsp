<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; X-Content-Type-Options=nosniff; charset=utf-8" />
<title>Forget Username</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/showhidenemu.js"></script>
<script type="text/javascript" src="../js/top-hide.js"></script>
<s:head />
</head>
<body class="bodyColor">
	<table width="500" border="0" align="center" cellpadding="0">
		<tr>
			<td align="center"><s:form>
					<s:token />
					<table width="100%" border="0" cellspacing="0" cellpadding="0" class="top20">
						<tr>
							<td height="35" align="right" valign="top">&nbsp;</td>
						</tr>
						<tr>
							<td align="left" valign="top" class="loginBox_new"><table width="100%" border="0"
									cellspacing="0" cellpadding="0" class="tablemargin">
									<tr>
										<td align="center" valign="top"><s:div>
												<table width="90%" align="center" border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td height="20"></td>
													</tr>
													<tr>
														<td height="30" align="left" valign="top"><img src="../image/lpay.png"
															width="250" height="46" /></td>
													</tr>
													<tr>
														<td height="30" align="left" valign="top">&nbsp;</td>
													</tr>
													<tr>
														<td align="left" valign="top"><h4>User name Retrieval</h4>
															<p>
																If you have lost or forgotten your user name please complete the form below to
																receive a reminder of your user name via email. This email will be sent to your
																verified email address.<br /> <br /> If you have not yet set up or verified your
																email address with us you will need to contact your account administrator to help
																you retrieve your user name.
															</p></td>
													</tr>
													<tr>
														<td align="left"><table width="80%" border="0" align="center" cellpadding="3"
																cellspacing="0">
																<tr>
																	<td height="10"></td>
																</tr>
																<tr>
																	<td height="30" align="left"><h5>Please enter your details</h5></td>
																</tr>
																<tr>
																	<td height="30" align="left"><s:textfield type="text" label="Merchant Name "
																			name="textfield" id="textfield" cssClass="textFLGF1" autocomplete="off" /></td>
																</tr>
																<tr>
																	<td height="30" align="left"><s:textfield type="text" label="Email "
																			name="textfield2" id="textfield2" cssClass="textFLGF1" autocomplete="off" /></td>
																</tr>
																<tr>
																	<td height="30" align="left">&nbsp;</td>
																</tr>
																<tr>
																	<td height="30" align="left"><s:submit type="submit" name="button" id="button"
																			value="retrieve username" cssClass="buttonB1" autocomplete="off" /> <!--<a href="<s:url value="index.jsp"/>">Return to login</a> --></td>
																</tr>
																<tr>
																	<td height="30" align="left">&nbsp;</td>
																</tr>
															</table></td>
													</tr>
													<tr>
														<td>&nbsp;</td>
													</tr>
												</table>
											</s:div></td>
									</tr>
								</table></td>
						</tr>
						<tr>
							<td align="left" valign="top">&nbsp;</td>
						</tr>
					</table>
				</s:form></td>
		</tr>
	</table>
</body>
</html>