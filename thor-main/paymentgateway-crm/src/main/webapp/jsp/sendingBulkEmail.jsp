<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Bulk Email</title>
<link rel="icon" href="../image/favicon-32x32.png">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/custom.css" rel="stylesheet" type="text/css" />
<script src="../js/continents.js" type="text/javascript"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/follw.js"></script>
<link href="../css/fonts.css" />
<script>
	if (self == top) {
		var theBody = document.getElementsByTagName('body')[0];
		theBody.style.display = "block";
	} else {
		top.location = self.location;
	}
</script>
<style>
.button-design {
	margin-left: 6px;
	margin-top: 20px;
	height: 25px;
	width: 10%;
	font-size: 12px;
	font-weight: 700;
	border-radius: 5px;
}

.errorMessage {
	margin-top: -32px;
}

.btn:focus {
	outline: 0 !important;
}
</style>
</head>
<body>
	<div style="display: none" id="response"></div>
	<table width="100%" border="0" align="center" cellpadding="0"
		cellspacing="0" style="margin-top: 1.5%;">
		<tr>
			<td align="left" valign="middle">
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td align="left" valign="top" bgcolor="#002163">
							<table width="100%" border="0" cellpadding="0" cellspacing="0">
								<tr>
									<td align="left" width="68%" valign="middle"
										style="font-family: 'Open Sans', sans-serif; font-size: 15px; font-weight: 700; color: #fff; padding: 4px 0 4px 8px;">Send
										Bulk Email</td>
								</tr>
							</table>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<table width="100%" border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td align="left" valign="middle"><s:form action="sendBulkEmail"
					method="post" theme="css_xhtml" class="FlowupLabels">
					<s:div cssClass="indent">
						<s:actionmessage class="success success-text" />
						<table width="100%" border="0" cellspacing="0" cellpadding="7"
							class="formboxRR">
							<tr>
								<td align="left" valign="top">&nbsp;</td>
							</tr>
							<tr>
								<td align="left" valign="top"><div>
										<div>
											<div>
												<span id="subjectValid"
													style="color: #ff0000; font-size: 11px;"></span>
												<div class="fl_wrap">
													<label class='fl_label'>Subject</label>
													<s:textfield type="text" name="subject" value=""
														id="subject" class="fl_input"></s:textfield>
												</div>
											</div>
											<div>
												<div class="">
													<div class='fl_wrap'
														style="height: 100px; margin-top: 10px;">
														<label class='fl_label'>Message Body</label>
														<s:textarea type="text" rows="5" class="fl_input"
															id="messageBody" name="messageBody" onkeyup=""
															autocomplete="off"
															style="height:95px; resize: none; outline-width:0"
															theme="simple" />
													</div>
												</div>
											</div>
										</div>
										<div>
											<div>
												<input type="submit"
													class="btn btn-success btn-md button-design"
													value="Send Email">
											</div>
											<div class="clear"></div>
										</div>
									</div></td>
							</tr>
						</table>
					</s:div>
					<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
				</s:form></td>
		</tr>
	</table>
	<script>
		$(document).on(
				'change',
				'.btn-file :file',
				function() {
					var input = $(this), numFiles = input.get(0).files ? input
							.get(0).files.length : 1, label = input.val()
							.replace(/\\/g, '/').replace(/.*\//, '');
					input.trigger('fileselect', [ numFiles, label ]);
				});

		$(document)
				.ready(
						function() {
							$('.btn-file :file')
									.on(
											'fileselect',
											function(event, numFiles, label) {

												var input = $(this).parents(
														'.input-group').find(
														':text'), log = numFiles > 1 ? numFiles
														+ ' files selected'
														: label;

												if (input.length) {
													input.val(log);
												} else {
													if (log)
														alert(log);
												}

											});
						});
	</script>
	<script src="../js/main.js"></script>
	<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
</body>
</html>