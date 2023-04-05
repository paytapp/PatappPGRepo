<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Admin Setup</title>
<link rel="icon" href="../image/favicon-32x32.png">
</head>
<body>
	<s:form action="adminSetupUpdate" method="post" autocomplete="off">
		<table width="100%" border="0" align="center" cellpadding="0"
			cellspacing="0">
			   <s:actionmessage class="success success-text"/>   
			<tr>
				<td align="left" valign="top" class="nfbxf"><div
						class="addfildn">
						<div class="rkb" style="width: 25%">
							<div class="addfildn">
								Status<br />
								<s:select class="textFL_merch" headerValue="ALL"
									list="#{'ACTIVE': 'ACTIVE' ,'LOGIN_BLOCKED': 'LOGIN_BLOCKED','TERMINATED': 'TERMINATED'}"
									id="status" name="userStatus" value="%{user.userStatus}" />
							</div>
							<div class="clear"></div>
						</div>
						<div class="clear"></div>
					</div></td>
				<s:div cssClass="indent">
					<table width="100%" border="0" cellspacing="0" cellpadding="7"
						class="formboxRR">
						<tr>
							<td align="left" valign="top">&nbsp;</td>
						</tr>
						<tr>
						<tr>
							<td align="left" valign="top">
								<div class="addfildn">
									<div class="rkb">
										<div class="addfildn">
											<div class="fl_wrap">
												<label class='fl_label'>Pay ID</label>
												<s:textfield id="payId" class="fl_input" name="payId"
													type="text" value="%{user.payId}" readonly="true"></s:textfield>
											</div>
										</div>
										<div class="addfildn">
											<div class="fl_wrap">
												<label class='fl_label'>Registration Date</label>
												<s:textfield class="fl_input" id="registrationDate"
													name="registrationDate" type="text"
													value="%{user.registrationDate}" readonly="true"></s:textfield>
											</div>
										</div>
										<div class="addfildn">
											<div class="fl_wrap">
												<label class='fl_label'>Activaction Date</label>
												<s:textfield class="fl_input" id="activationDate"
													name="activationDate" type="text"
													value="%{user.activationDate}" readonly="true"></s:textfield>
											</div>
										</div>


										<div class="clear"></div>
									</div>

									<div class="rkb">
										<div class="addfildn">
											<div class="fl_wrap">
												<label class='fl_label'>Business Name</label>
												<s:textfield class="fl_input" id="businessName"
													name="businessName" type="text"
													value="%{user.businessName}" readonly="true"
													autocomplete="off"
													onKeyPress="return ValidateAlpha(event);"></s:textfield>
											</div>
										</div>

										<div class="addfildn"></div>


										<div class="addfildn">
											<div class="fl_wrap">
												<label class='fl_label'>Email ID</label>
												<s:textfield class="fl_input" id="emailId" name="emailId"
													type="text" value="%{user.emailId}" readonly="true"></s:textfield>
											</div>
										</div>
									</div>
								</div>
							</td>
						<s:submit class="btn btn-success btn-md" value="Save"></s:submit>  
						</tr>

					</table>
				</s:div>
			</tr>
		</table>
	</s:form>
</body>
</html>