<%@page import="antlr.StringUtils"%>
<%@page import="com.paymentgateway.commons.util.Amount"%>
<%@page import="com.paymentgateway.commons.util.Currency"%>
<%@page import="com.paymentgateway.commons.util.FieldType"%>
<%@page import="java.text.DecimalFormat"%>
<%@ page import="org.owasp.esapi.ESAPI" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<!DOCTYPE html>
<%@taglib prefix="s" uri="/struts-tags"%>

<html>

<head>

<meta http-equiv="refresh"
	content="10; url=<s:url includeParams="" />"></meta> 

<title>Payment Page</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/bootstrap.min.css" media="all" type="text/css"
	rel="stylesheet" />
<style>
.center-div {
	margin: auto;
	min-width: 320px;
	max-width: 500px;
	padding: 0px;
	margin-top: 15%;
}
</style>
</head>

<body>
	<div class="center-div">
		<div class="container">
			<div class="panel panel-default">
				<div class="panel-body"
					style="background-color: #ffffff; margin: 0px; padding: 6px;">
					<div class="row">
						<div class="col-md-12 text-center">
							<img src="../image/lpay.png" width="210" />
						</div>
					</div>
					<div class="row">
						<div class="col-md-12 text-center">Order ID:<%=ESAPI.encoder().encodeForHTML((String) session.getAttribute(FieldType.ORDER_ID.getName()))%>
							Amount: <%=ESAPI.encoder().encodeForHTML (Amount.toDecimal((String) session
					.getAttribute(FieldType.AMOUNT.getName()), (String) session
					.getAttribute(FieldType.CURRENCY_CODE.getName())))%></div>
					</div>
					<div class="clearfix">&nbsp;</div>
					<div class="row">
						<div class="col-md-12 text-center">
							<img src="../image/loader.gif" />
						</div>
					</div>
					<div class="clearfix">&nbsp;</div>
					<div class="row">
						<div class="col-md-12 text-muted" style="text-align: center;">
							<small>Please wait while we redirect you to bank for
								processing... Please do not press Stop, Back or Refresh button
								or Close this window</small>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="modal" style="display: none">
		<div class="center">
			<img alt="" src="../image/loader.gif" />
		</div>
	</div>
</body>

</html>