<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Mapping</title>
<link rel="icon" href="../image/favicon-32x32.png">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/animatedcollapse.js"></script>
<script type="text/javascript">
animatedcollapse.addDiv('showmain', 'fade=4')
animatedcollapse.addDiv('divv1', 'fade=1')
animatedcollapse.addDiv('divv2', 'fade=1')
animatedcollapse.addDiv('divv3', 'fade=1')

animatedcollapse.addDiv('divvSub1', 'fade=1')
animatedcollapse.addDiv('divvSub2', 'fade=1')
animatedcollapse.addDiv('divvSub3', 'fade=1')
animatedcollapse.addDiv('divvSub4', 'fade=1')
animatedcollapse.ontoggle=function($, divobj, state){ //fires each time a DIV is expanded/contracted
	//$: Access to jQuery
	//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
	//state: "block" or "none", depending on state
}
animatedcollapse.init()
</script>
<style type="text/css">
td {font: normal 13px arial;color: #333;} td a {font: normal 13px arial;color: #333;} td a:hover {font: normal 13px arial;color: #ff0000;text-decoration: underline;
}.closeX {font: normal 12px arial;color: #ff0000;text-decoration: none;}.headingbig {font: bold 16px arial;color: #000;text-decoration: none;}
a.headingbig:hover {font: bold 16px arial;color: #ff0000;	text-decoration: underline;}</style>
</head>
<body>
	<s:form>
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td width="20%" height="40" align="left" class="headingbig"><s:select
						style="width:150px; height:30px;" headerValue="Select User Type"
						list="#{'Acquirer':'Acquirer'}" name="user" headerKey="1" value="0" /></td>
				<td width="20%" align="left" class="headingbig"><s:select style="width:150px; height:30px;"
						headerValue="ALL" headerKey="1" list="#{'FSS':'FSS','MIGS':'MIGS'}" name="acquirer" value="0" /></td>
				<td></td>
				<td width="60%" align="left" class="headingbig"><a
					href="javascript:animatedcollapse.show(['showmain'])" class="headingbig">Payment Type</a></td>
			</tr>
			<tr>
				<td height="40" align="left">&nbsp;</td>
				<td align="left">&nbsp;</td>
				<td align="left"><div id="showmain" style="width: 700px; display: none;">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							style="background: #ecf4fe; border: 1px solid #dddddd; border-radius: 10px; padding: 20px;">
							<tr>
								<td align="right"><a href="javascript:animatedcollapse.hide('showmain')" class="closeX"
									title="Close All">Close All</a></td>
							</tr>
							<tr>
								<td align="left"><table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td width="33%" align="left" valign="top"><table width="98%" align="center"
													border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td align="left" height="50"><input name="" type="checkbox" value=""> <a
															href="javascript:animatedcollapse.show('divv1');" class="headingbig">Debit Card</a>
															&nbsp;&nbsp;&nbsp;&nbsp; <a href="javascript:animatedcollapse.hide('divv1')"
															class="closeX" title="Close"><img src="../image/clsbtn.png" alt="Close"
																title="Close" border="0"></a></td>
													</tr>
												</table></td>
											<td width="33%" align="left" valign="top"><table width="98%" align="center"
													border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td align="left" height="50"><input name="" type="checkbox" value=""> <a
															href="javascript:animatedcollapse.show('divv2')" class="headingbig">Credit Card</a>
															&nbsp;&nbsp;&nbsp;&nbsp; <a href="javascript:animatedcollapse.hide('divv2')"
															title="Close"><img src="../image/clsbtn.png" alt="Close" title="Close"
																border="0"></a></td>
													</tr>
												</table></td>
											<td width="33%" align="left" valign="top"><table width="98%" align="center"
													border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td align="left" height="50"><input name="" type="checkbox" value=""> <a
															href="javascript:animatedcollapse.show('divv3')" class="headingbig">Net Banking</a>
															&nbsp;&nbsp;&nbsp;&nbsp; <a href="javascript:animatedcollapse.hide('divv3')"
															title="Close"><img src="../image/clsbtn.png" alt="Close" title="Close"
																border="0"></a></td>
													</tr>
												</table></td>
										</tr>

										<tr>
											<td align="left" valign="top"><table width="98%" align="center" border="0"
													cellspacing="0" cellpadding="0">
													<tr>
														<td colspan="3" id="divv1" style="display: none;"><table width="100%" border="0"
																cellspacing="0" cellpadding="0"
																style="background: #fff; border: 1px solid #dddddd; border-radius: 10px; padding: 20px;">
																<tr>
																	<td width="15%" height="30" align="left"><input name="" type="checkbox"
																		value=""></td>
																	<td width="85%" align="left"><table width="100%" border="0" cellspacing="0"
																			cellpadding="0">
																			<tr>
																				<td align="left"><a href="javascript:animatedcollapse.show('divvSub1')">Visa</a></td>
																				<td align="right"><a href="javascript:animatedcollapse.hide('divvSub1')"
																					title="Close"><img src="../image/clsbtn.png" alt="Close" title="Close"
																						border="0"></a></td>
																			</tr>
																		</table></td>
																</tr>
																<tr>
																	<td align="left"></td>
																	<td align="left" id="divvSub1" style="display: none;"><table width="100%"
																			border="0" cellspacing="0" cellpadding="0">
																			<s:checkboxlist name="txnType"
																				list="@com.paymentgateway.commons.util.TransactionType@values()" />
																		</table></td>
																</tr>
																<tr>
																	<td width="15%" height="30" align="left"><input name="" type="checkbox"
																		value=""></td>
																	<td width="85%" align="left"><table width="100%" border="0" cellspacing="0"
																			cellpadding="0">
																			<tr>
																				<td align="left"><a href="javascript:animatedcollapse.show('divvSub2')">Master</a></td>
																				<td align="right"><a href="javascript:animatedcollapse.hide('divvSub2')"
																					title="Close"><img src="../image/clsbtn.png" alt="Close" title="Close"
																						border="0"></a></td>
																			</tr>
																		</table></td>
																</tr>
																<tr>
																	<td align="left"></td>
																	<td align="left" id="divvSub2" style="display: none;"><table width="100%"
																			border="0" cellspacing="0" cellpadding="0">
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Master</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Master</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Master</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Master</td>
																			</tr>
																		</table></td>
																</tr>
																<tr>
																	<td width="15%" height="30" align="left"><input name="" type="checkbox"
																		value=""></td>
																	<td width="85%" align="left"><table width="100%" border="0" cellspacing="0"
																			cellpadding="0">
																			<tr>
																				<td align="left"><a href="javascript:animatedcollapse.show('divvSub3')">Maestro</a></td>
																				<td align="right"><a href="javascript:animatedcollapse.hide('divvSub3')"
																					title="Close"><img src="../image/clsbtn.png" alt="Close" title="Close"
																						border="0"></a></td>
																			</tr>
																		</table></td>
																</tr>
																<tr>
																	<td align="left"></td>
																	<td align="left" id="divvSub3" style="display: none;"><table width="100%"
																			border="0" cellspacing="0" cellpadding="0">
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Maestro</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Maestro</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Maestro</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Maestro</td>
																			</tr>
																		</table></td>
																</tr>
																<tr>
																	<td width="15%" height="30" align="left"><input name="" type="checkbox"
																		value=""></td>
																	<td width="85%" align="left"><table width="100%" border="0" cellspacing="0"
																			cellpadding="0">
																			<tr>
																				<td align="left"><a href="javascript:animatedcollapse.show('divvSub4')">Rupay</a></td>
																				<td align="right"><a href="javascript:animatedcollapse.hide('divvSub4')"
																					title="Close"><img src="../image/clsbtn.png" alt="Close" title="Close"
																						border="0"></a></td>
																			</tr>
																		</table></td>
																</tr>
																<tr>
																	<td align="left"></td>
																	<td align="left" id="divvSub4" style="display: none;"><table width="100%"
																			border="0" cellspacing="0" cellpadding="0">
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Rupay</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Rupay</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Rupay</td>
																			</tr>
																			<tr>
																				<td width="15%" align="left" height="30"><input name="" type="checkbox"
																					value=""></td>
																				<td width="85%" align="left">Rupay</td>
																			</tr>
																		</table></td>
																</tr>
															</table></td>
													</tr>
												</table></td>
											<td align="left" valign="top"><table width="98%" align="center" border="0"
													cellspacing="0" cellpadding="0">
													<tr>
														<td colspan="3" id="divv2" style="display: none;"><table width="100%" border="0"
																cellspacing="0" cellpadding="0"
																style="background: #fff; border: 1px solid #dddddd; border-radius: 10px; padding: 20px;">
																<tr>
																	<td width="15%" height="30" align="left"><input name="" type="checkbox"
																		value=""></td>
																	<td width="85%" align="left">Visa</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">Master</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">Maestro</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">Rupay</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">Amex</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">JCB</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">Discover</td>
																</tr>
															</table></td>
													</tr>
												</table></td>
											<td align="left" valign="top"><table width="98%" align="center" border="0"
													cellspacing="0" cellpadding="0">
													<tr>
														<td colspan="3" id="divv3" style="display: none;"><table width="100%" border="0"
																cellspacing="0" cellpadding="0"
																style="background: #fff; border: 1px solid #dddddd; border-radius: 10px; padding: 20px;">
																<tr>
																	<td width="15%" height="30" align="left"><input name="" type="checkbox"
																		value=""></td>
																	<td width="85%" align="left">SBI</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">HDFC</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">ICICI</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">AXIS</td>
																</tr>
																<tr>
																	<td align="left" height="30"><input name="" type="checkbox" value=""></td>
																	<td align="left">PNB</td>
																</tr>
															</table></td>
													</tr>
												</table></td>
										</tr>
									</table></td>
							</tr>
						</table>
					</div></td>
			</tr>
		</table>
	</s:form>
</body>
</html>