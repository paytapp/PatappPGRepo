
<%@page import="com.paymentgateway.crm.action.ForwardAction"%>
<%@page import="com.paymentgateway.crm.action.GetParentDetailAction"%>
<%@page import="com.paymentgateway.commons.user.User"%>
<%@page import="com.paymentgateway.commons.util.Constants"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@page import="com.paymentgateway.commons.util.Currency"%>
<%@page import="com.paymentgateway.commons.util.Amount"%>
<%@ page import="org.owasp.esapi.ESAPI"%>
<%@page import="com.paymentgateway.commons.util.FieldType"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Admin Profile</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery.easing.js"></script>
<script type="text/javascript" src="../js/jquery.dimensions.js"></script>
<script type="text/javascript" src="../js/jquery.accordion.js"></script>
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

<script type="text/javascript">
	$(document).ready(function(e) {
		function getdata() {
			//alert("hihhhhh");   
			$.ajax({
				url : 'getParentDetail',
				type : 'post',
				success : function(data) {
					var _tr = "";
					for (key in data) {
						console.log(data[key]);
						if (data[key] != null) {
							var _tempName = key;
							var _placeHolder = "";
							var newReg = /[A-Z]/;
							for (var j = 0; j < _tempName.length; j++) {
								if (_tempName[j].match(newReg)) {
									_placeHolder += " " + _tempName[j];
								} else {
									_placeHolder += _tempName[j];
								}
							}
							// console.log(_placeHolder);
							_tr += "<tr>";
							_tr += "<td>" + _placeHolder + "</td>";
							_tr += "<td>" + data[key] + "</td>";
							_tr += "</tr>";
						}
					}
					$("#parentialDetails").append(_tr);

				},
				error : function(eresponse) {
					alert("error" + eresponse);

				}
			});
		}

		getdata();
	})
	function sendDefaultCurrency() {
		var token = document.getElementsByName("token")[0].value;
		var dropDownOption = document.getElementById("defaultCurrency").options;
		var dropDown = document.getElementById("defaultCurrency").options.selectedIndex;
		var payId = '<s:property value="#session.USER.payId" />';
		$
				.ajax({
					url : 'setDefaultCurrency',
					type : 'post',
					data : {
						payId : payId,
						defaultCurrency : document
								.getElementById("defaultCurrency").value,
						token : token
					},
					success : function(data) {
						var responseDiv = document.getElementById("response");
						responseDiv.innerHTML = data.response;
						responseDiv.style.display = "block";
						var responseData = data.response;
						if (responseData == null) {
							responseDiv.innerHTML = "Operation not successfull, please try again later!!"
							responseDiv.style.display = "block";
							responseDiv.className = "error error-new-text";
							event.preventDefault();
						}
						var currencyDropDown = document
								.getElementById("defaultCurrency");
						responseDiv.className = "success success-text";
					},
					error : function(data) {
						var responseDiv = document.getElementById("response");
						responseDiv.innerHTML = "Error updating default currency please try again later!!"
						responseDiv.style.display = "block";
						responseDiv.className = "error error-new-text";
					}
				});
	}
</script>
<style type="text/css">
.error-text {
	color: #a94442;
	font-weight: bold;
	background-color: #f2dede;
	list-style-type: none;
	text-align: center;
	list-style-type: none;
	margin-top: 10px;
}

.error-text li {
	list-style-type: none;
}

.product-specbigstripes tr td {
	color: #555;
}

.btn:focus {
	outline: 0 !important;
}

#parentialDetails td {
	text-transform: capitalize;
}
</style>
</head>
<body>
	<div class="error-text">
		<s:actionmessage />
	</div>

	<div class="row">
		<div class="col-md-6">
			<section
				class="profile lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o"
							aria-hidden="true"></i></span>
						<h2 class="heading_text">My Personal Detail</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="lpay_table_wrapper">
						<table class="lpay_custom_table" id="parentialDetails"
							width="100%">
							<tr>
								<td>Email Id</td>
								<td><s:property value="#session.USER.emailId" /></td>
							</tr>
							<tr>
								<td>Contact Name</td>
								<td><s:property value="#session.USER.contactPerson" /></td>
							</tr>
							<tr>
								<td>User Typer</td>
								<td><s:property value="#session.USER.UserType.name()" /></td>
							</tr>
							<tr>
								<td>Business Name</td>
								<td><s:property value="#session.USER.businessName" /></td>
							</tr>
						</table>
					</div>
					<!-- /.lpay_table_wrapper -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row --> </section>
			<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		</div>
		<!-- /.col-md-6 -->
		<div class="col-md-6">
			<section
				class="profile lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o"
							aria-hidden="true"></i></span>
						<h2 class="heading_text">Contact Details</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="lpay_table_wrapper">
						<table class="lpay_custom_table" width="100%">
							<tr>
								<td>Mobile Number</td>
								<td><s:property value="#session.USER.mobile" /></td>
							</tr>
							<tr>
								<td>Landline Number</td>
								<td><s:property value="#session.USER.telephoneNo" /></td>
							</tr>
							<tr>
								<td>Address</td>
								<td><s:property value="#session.USER.address" /></td>
							</tr>
							<tr>
								<td>City</td>
								<td><s:property value="#session.USER.city" /></td>
							</tr>
							<tr>
								<td>State</td>
								<td><s:property value="#session.USER.state" /></td>
							</tr>
							<tr>
								<td>Country</td>
								<td><s:property value="#session.USER.country" /></td>
							</tr>
							<tr>
								<td>Postal</td>
								<td><s:property value="#session.USER.postalCode" /></td>
							</tr>
						</table>
						<!-- /.lapy_custom_table -->
					</div>
					<!-- /.lpay_table_wrapper -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row --> </section>
			<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		</div>
		<!-- /.col-md-6 -->
	</div>
	<!-- /.row -->

	<script>
		jQuery(document).ready(function($) {
			$('#tabs').tab();
		});
		</body>
	</script>
</html>