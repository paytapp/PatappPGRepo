<%@page import="com.paymentgateway.commons.util.SaltFactory"%>
<%@page import="com.paymentgateway.crm.action.GetParentDetailAction"%>
<%@page import="com.paymentgateway.commons.user.User"%>
<%@page import="com.paymentgateway.commons.util.Currency"%>
<%@page import="com.paymentgateway.commons.util.Amount"%>
<%@page import="com.paymentgateway.commons.util.FieldType"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="com.paymentgateway.commons.util.Constants"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Merchant Profile</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery-latest.min.js"></script>

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
		$.ajax({
			url : 'setDefaultCurrency',
			type : 'post',
			data : {
				defaultCurrency : document
						.getElementById("defaultCurrency").value,
				token : token
			},
			success : function(data) {

				var responseDiv = document.getElementById("response");
				responseDiv.innerHTML = data.response;
				responseDiv.style.display = "block";
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
.mt-5 { margin-top: 5px !important; }
.d-inline-flex { display: inline-flex !important; }
.flex-column { flex-direction: column !important; }
.align-items-center { align-items: center !important; }
.text-secondary { color: #3C4858 !important; }
.font-weight-medium { font-weight: 600 !important; }
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

#response {
	color: green;
}

table.product-specbigstripes .borderbtmleftradius {
	border-bottom-left-radius: 0px !important;
}

table.product-specbigstripes .borderbtmrightradius {
	border-bottom-right-radius: 0px !important;
}

#wwgrp_defaultCurrency {
	float: left;
}

#parentialDetails td {
	text-transform: capitalize;
}

	.qr-code-column .qr-code-box:not(:last-child) { margin-right: 30px; }
</style>
</head>

<body>

	<div class="row">
		<div class="col-md-6">
			<section
				class="profile lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i
								class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">My Personal Detail</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="lpay_table_wrapper">
							<table class="lpay_custom_table" width="100%">
								<tr>
									<td>Email Id</td>
									<td><s:property value="#session.USER.emailId" /></td>
								</tr>
								<tr>
									<td>Contact Name</td>
									<td><s:property value="#session.USER.contactPerson" /></td>
								</tr>
								
								<tr>
									<td>Company Name</td>
									<td><s:property value="#session.USER.companyName" /></td>
								</tr>
								<tr>
									<td>User Type</td>
									<td><s:property value="#session.USER.UserType.name()" /></td>
								</tr>
								<tr>
									<td>Business Name</td>
									<td><s:property value="#session.USER.businessName" /></td>
								</tr>
								
								<tbody id="parentialDetails"></tbody>

								<s:if test="%{#session.USER.UserType.name()=='MERCHANT'}">
									<s:if test="%{#session.USER.virtualAccountFlag == true}">
										<tr>
											<td>Virtual Account Number</td>
											<td><s:property value="%{#session.USER.virtualAccountNo}" /></td>
										</tr>
									</s:if>
									<s:if test="%{#session.USER.allowUpiQRFlag == true}">
									<tr>
										<td>Merchant VPA</td>
										<td><s:property value="%{#session.USER.merchantVPA}" /></td>
									</tr>
									</s:if>
									<tr>
										<s:if test="%{#session.USER.virtualAccountFlag == true}">
											<td>Virtual IFSC Code</td>
											<td><s:property value="%{#session.USER.virtualIfscCode}" /></td>
										</s:if>
									</tr>
									<tr>
										<s:if test="%{#session.USER.virtualAccountFlag == true}">
											<td>Virtual Beneficiary Name</td>
											<td><s:property
												value="%{#session.USER.virtualBeneficiaryName}" /></td>
										</s:if>
									</tr>
									<tr>
										<td>Pay Id</td>
										<td><s:property
												value="%{#session.USER.payId}" /></td>
									</tr>
									<tr>
										<td>Salt</td>
										<td><s:property
												value="%{salt}"/></td>
									</tr>
								</s:if>
								<!-- <tr>
									<td>Default Currency</td>
									<td><s:select name="defaultCurrency" id="defaultCurrency"
											list="currencyMap"
											style="width:140px;height: 43px; display:inline;"
											class="form-control" /> 
											<input type="button" id="btnSave"
										name="btnSave"
										class="lpay_button lpay_button-md lpay_button-secondary"
										value="Submit" onclick="sendDefaultCurrency()"
										style="display: inline;">
										</td>

								</tr> -->
							</table>
						</div>
						<!-- /.lpay_table_wrapper -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
			<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		</div>
		<!-- /.col-md-6 -->
		<div class="col-md-6">
			<section
				class="profile lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i
								class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
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
								<%-- <tr>
									<td>City</td>
									<td><s:property value="#session.USER.city" /></td>
								</tr> --%>
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

								<s:if test="%{(#session.USER.UserType.name()=='MERCHANT' && #session.USER.getSuperMerchantId() == null) || (#session.USER.UserType.name() == 'MERCHANT' && #session.USER.isSuperMerchant() == false && #session.USER.getSuperMerchantId() != null)}">
									<s:if test="%{#session.USER.allowQRScanFlag == true || #session.USER.allowUpiQRFlag == true}">
										<tr>
											<td colspan="2" class="qr-code-column">
												<s:if test="%{#session.USER.allowQRScanFlag == true}">
													<div class="d-inline-flex flex-column align-items-center qr-code-box">
														<a href="#" class="downloadQrCode" data-download="PGQR"><img src="../images/demo-qr-code.png" alt=""></a>
														<a href="#" class="downloadQrCode text-primary font-weight-medium mt-5" data-download="PGQR">Download PG QR</a>
													</div>
												</s:if>
	
												<s:if test="%{#session.USER.allowUpiQRFlag == true}">
													<div class="d-inline-flex flex-column align-items-center qr-code-box">
														<a href="#" class="downloadQrCode" data-download="UPIQR"><img src="../images/demo-qr-code.png" alt=""></a>
														<a href="#" class="downloadQrCode text-primary font-weight-medium mt-5" data-download="UPIQR">Download UPI QR</a>
													</div>
												</s:if>										
											</td>
										</tr>
									</s:if>
								</s:if>
							</table>
							<!-- /.lapy_custom_table -->
						</div>
						<!-- /.lpay_table_wrapper -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
			<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		</div>
		<!-- /.col-md-6 -->
	</div>
	<!-- /.row -->

	<s:form method="POST" action="qrImageDowloadAction" id="qrCodeDownloadForm">
		<s:hidden name="payId" value="%{#session.USER.payId}" />
		<s:hidden name="qrType" id="qrType" />
	</s:form>

	
	<script>
		jQuery(document).ready(function($) {
			$('#tabs').tab();

			$("body").on("click", ".downloadQrCode", function(e) {
				e.preventDefault();

				var qrType = $(this).attr("data-download");
				$("#qrType").val(qrType);
				$("#qrCodeDownloadForm").submit();
			});
		});
	</script>

</body>
</html>