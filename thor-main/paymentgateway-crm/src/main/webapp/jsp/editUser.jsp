<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Modify SubUser</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script language="JavaScript">
		$(document).ready(function () {
			handleChange();
		});

		function handleChange() {
			var str = '<s:property value="permissionString"/>';
			var permissionArray = str.split("-");
			for (j = 0; j < permissionArray.length; j++) {
				selectPermissions(permissionArray[j]);
			}
		}

		function selectPermissions(permissionType) {
			var permissionCheckbox = document.getElementById(permissionType);
			if (permissionCheckbox == null) {
				return;
			}
			
			permissionCheckbox.checked = true;
		}
	</script>
	<script type="text/javascript">
		$(document).ready(function () {
			var _userType = $("[data-id=userType]").val();

			if (_userType == "vendorType") {
				$(".acquirer-input").removeAttr("readonly", false);
				$(".checkbox-label").closest(".col-md-3").addClass("d-none");
				// $(".common").attr("disabled", true);
				$("input[value='Vendor Report']").closest(".col-md-3").attr("disabled", true);
				$("input[value='Vendor Report']").closest(".col-md-3").removeClass("d-none");
				$("input[value='Vendor Report']").removeAttr("disabeld");
				$("input[value='Vendor Report']").prop("checked", true);
				$("input[value='Vendor Report']").closest("label").addClass("checkbox-checked");
				$("input[value='Static UPI QR Report']").closest(".col-md-3").addClass("d-none");
			}

			if (_userType == "eposType") {
				// $("input[value='Vendor Report']").closest(".col-md-3").addClass("d-none");
				$("input[value='Create Invoice']").closest(".col-md-3").addClass("d-none");
				$("input[value='View Invoice']").closest(".col-md-3").addClass("d-none");
				// $("input[value='Sub User All']").prop("checked", true);
				// $("input[value='Sub User All']").closest("label").addClass("checkbox-checked");
				$("input[value='Allow ePos']").closest(".col-md-3").attr("disabled", true);
				$("input[value='Allow ePos']").prop("checked", true);
				$("input[value='Allow ePos']").closest("label").addClass("checkbox-checked");
				// $("input[value='Static UPI QR Report']").closest(".col-md-3").addClass("d-none");
				$("input[value='Payout']").closest(".col-md-3").addClass("d-none");
				$("input[value='Net Settled Report']").closest(".col-md-3").addClass("d-none");
			}

			$(".checkbox-label").each(function (e) {
				var _getId = $(this).find("input[type='checkbox']").attr("id");
				$(this).attr("for", _getId);
				if ($("input[value='Vendor Report']").is(":checked")) {
					if (_userType == "vendorType") {
						$(".checkbox-label").closest(".col-md-3").addClass("d-none");
						$("input[value='Vendor Report']").closest(".col-md-3").removeClass("d-none");
					}
				}
				if ($(this).find("input[type='checkbox']").is(":checked")) {					
					$(this).addClass("checkbox-checked");
				}
			});

			// add class to on changed 
			$(".checkbox-label input").on("change", function (e) {
				if ($(this).is(":checked")) {
					$(this).closest("label").addClass("checkbox-checked");
				} else {
					$(this).closest("label").removeClass("checkbox-checked");
				}
			});

			// bookingPermission
			$("#transactionPermission input[type='checkbox']").on("change", function (e) {
				var isChecked = $(this).is(":checked"),
					userType = $("[data-id='userType']").val();
				if (isChecked) {
					$("#bookingPermission").removeClass("d-none");

					if(userType == "normalType") {
						$("#paymentAdvice").removeClass("d-none");
					}
				} else {
					if(document.getElementById("bookingPermission") !== null) {
						$("#bookingPermission").addClass("d-none");
						$("#bookingPermission label").removeClass("checkbox-checked");
						document.getElementById("bookingPermission").querySelector("input[type='checkbox']").checked = false;
					}

					if(document.getElementById("paymentAdvice") !== null) {
						$("#paymentAdvice").addClass("d-none");
						$("#paymentAdvice label").removeClass("checkbox-checked");
						document.getElementById("paymentAdvice").querySelector("input[type='checkbox']").checked = false;
					}
				}
			});

			if ($("#transactionPermission") !== null && $("#transactionPermission") !== undefined) {
				var isTransactionChecked = $("#transactionPermission input[type='checkbox']").is(":checked"),
					userType = $("[data-id='userType']").val();
				if (isTransactionChecked) {
					if ($("#bookingPermission") !== null && $("#bookingPermission") !== undefined) {
						$("#bookingPermission").removeClass("d-none");
					}

					if(userType == "normalType") {
						if($("#paymentAdvice") !== null && $("#paymentAdvice") !== undefined) {
							$("#paymentAdvice").removeClass("d-none");
						}
					}
				} else {
					if ($("#bookingPermission") !== null && $("#bookingPermission") !== undefined) {
						$("#bookingPermission").addClass("d-none");
					}

					if ($("#paymentAdvice") !== null && $("#paymentAdvice") !== undefined) {
						$("#paymentAdvice").addClass("d-none");
					}
				}
			}


			if ($("[for='eNACH Report'] input[type='checkbox']").is(":checked")) {
				$("#eNachReport").closest(".col-md-3").remove();
			} else {
				$("#eNachReport").closest(".col-md-3").remove();
			}

			if ($("[for='UPI AutoPay Report'] input[type='checkbox']").is(":checked")) {
				$("#upiAutoPayReport").closest(".col-md-3").remove();
			} else {
				$("#upiAutoPayReport").closest(".col-md-3").remove();
			}

			$('#btnEditUser').click(function () {
				var answer = confirm("Are you sure you want to edit user details?");
				if (answer != true) {
					return false;
				} else {
					/* $.ajax({
						url : 'editUserDetails',
						type : "POST",
						data : {
								firstName : document.getElementById('firstName').value ,						
								lastName : document.getElementById('lastName').value,
								mobile : document.getElementById('mobile').value,
								emailAddress : document.getElementById('emailAddress').value,
								isActive: document.getElementById('isActive').value,
							},									
								success : function(data){		       	    		       	    		
								var res = data.response;
								},
								error : function(data){	
									alert("Unable to edit user details");
								}
								}); */
					document.getElementById("frmEditUser").submit();
				}
			});
		});
	</script>
</head>
<body>
	<s:textfield type="hidden" value="%{subUserType}" data-id="userType" />
	<section class="edit-user lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Edit SubUser</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-12">
				<div id="saveMessage">
					<s:actionmessage class="success success-text" />
				</div>
			</div>
			<!-- /.col-md-12 -->

			<s:form action="editSubUserDetails" id="frmEditUser">
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">First Name <span style="color:red; margin-left:3px;">*</span></label>
						<s:textfield name="firstName" id="firstName" cssClass="lpay_input" autocomplete="off" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">Last Name<span style="color:red;">*</span></label>
						<s:textfield name="lastName" id="lastName" cssClass="lpay_input" autocomplete="off" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">Business Name</label>
						<s:textfield name="businessName" id="businessName" cssClass="lpay_input" autocomplete="off" />
						<!-- /.error-name -->
					</div>
					<!-- /.lpay_input_group -->
				</div>

				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">Mobile Number<span style="color:red; margin-left:3px;">*</span></label>
						<s:textfield name="mobile" id="mobile" cssClass="lpay_input" onkeypress="javascript:return isNumber (event)" maxlength="13" autocomplete="off" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">Email</label>
						<s:textfield name="emailId" id="emailAddress" cssClass="lpay_input" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-12">
					<label for="isActive" class="checkbox-label unchecked mb-10">
						is Active ?
						<s:checkbox name="isActive" id="isActive" />
					</label>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 mb-20" data-id="permission">
					<div class="lpay_heading">
						<h3>Privilege Type</h3>
					</div>
					<!-- /.lpay_heading -->
				</div>
				<!-- /.col-md-12 -->
				
				<div class="col-md-12 d-none" id="subUserPermission" data-id="permission">
					<div class="row">
						<div class="col-md-3 mb-20">
							<label class="checkbox-label unchecked" for="1">
								Sub User All
								<s:checkbox
									name="lstPermissionType"
									id="Sub User All"
									class="common"
									fieldValue="Sub User All"
									value="false"
									autocomplete="off">
								</s:checkbox>
							</label>
						</div>
						<!-- /.col-md-4 -->

						<div class="col-md-3 mb-20">
							<label class="checkbox-label unchecked" for="1">
								Sub User Self
								<s:checkbox
									name="lstPermissionType"
									id="Sub User Self"
									class="common"
									fieldValue="Sub User Self"
									value="false"
									autocomplete="off">
								</s:checkbox>
							</label>
						</div>
						<!-- /.col-md-4 -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.col-md-12 -->

				<div class="col-md-12">
					<s:if test="%{!listPermissionType.isEmpty()}">
						<div class="row">
							<s:iterator value="listPermissionType" status="itStatus">
								<s:if test="%{permission == 'Booking Report'}">
									<s:if test="%{bookingReportFlag == true}">
										<div class="col-md-3 d-none" id="bookingPermission">
											<label for="%{permission}" class="checkbox-label unchecked mb-10">
												<s:checkbox name="lstPermissionType" id="%{permission}"
													fieldValue="%{permission}" value="false">
												</s:checkbox>
												<s:property value="permission" />
											</label>
										</div>
										<!-- /.col-md-3 -->
									</s:if>
								</s:if>
								<s:elseif test="%{permission == 'View Transaction Reports'}">
									<div class="col-md-3" id="transactionPermission">
										<label for="%{permission}" class="checkbox-label unchecked mb-10">
											<s:checkbox name="lstPermissionType" id="%{permission}"
												fieldValue="%{permission}" value="false">
											</s:checkbox>
											<s:property value="permission" />
										</label>
									</div>
									<!-- /.col-md-3 -->
								</s:elseif>								
								<s:elseif test="%{permission == 'Payout'}">									
									<s:if test="%{merchantInitiatedDirectFlag == true}">
										<div class="col-md-3" id="MerchantInitiatedPermission">
											<label class="checkbox-label unchecked mb-10" for="1">
												<s:property value="permission" />
												<s:checkbox name="lstPermissionType" id="%{permission}"
													fieldValue="%{permission}" value="false" autocomplete="off">
												</s:checkbox>
											</label>
										</div>
									</s:if>
								</s:elseif>
								<s:elseif test="%{permission == 'Account Verification'}">
									<s:if test="%{accountVerificationFlag == true}">
										<div class="col-md-3" id="AccountVerificationPermission">
											<label class="checkbox-label unchecked mb-10" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType" id="%{permission}"														
													fieldValue="%{permission}" value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
											<!-- /.checkbox-subadmin -->
										</div>
									</s:if>
								</s:elseif>
								<s:elseif test="%{permission == 'VPA Verification'}">
									<s:if test="%{vpaVerificationFlag == true}">
										<div class="col-md-3" id="VpaVerificationFlag">
											<label class="checkbox-label unchecked mb-10" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
										</div>
									</s:if>
								</s:elseif>
								<s:elseif test="%{permission == 'eNACH Report'}">
									<s:if test="%{eNachReportFlag == true}">
										<div class="col-md-3" id="eNachReportFlag">
											<label class="checkbox-label unchecked mb-10" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
										</div>
									</s:if>
								</s:elseif>
								<s:elseif test="%{permission == 'UPI AutoPay Report'}">
									<s:if test="%{upiAutoPayReportFlag == true}">
										<div class="col-md-3" id="upiAutoPayReportFlag">
											<label class="checkbox-label unchecked mb-10" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
										</div>
									</s:if>
								</s:elseif>
								<s:elseif test="%{permission == 'eCollection Report'}">
									<s:if test="%{virtualAccountFlag == true}">
										<div class="col-md-3" id="eCollectionReportFlag">
											<label class="checkbox-label unchecked mb-10" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
										</div>
									</s:if>
								</s:elseif>
								<s:elseif test="%{permission == 'Static UPI QR Report'}">								
									<s:if test="%{customerQrFlag == true}">
										<div class="col-md-3 mb-10" id="customerQrFlag">
											<label class="checkbox-label unchecked" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													disabled="true"
													class="common"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
										</div>
									</s:if>								
								</s:elseif>
								<s:elseif test="%{permission == 'Custom Capture Report'}">								
									<s:if test="%{capturedMerchantFlag == true}">
										<div class="col-md-3 mb-10" id="capturedMerchantFlag">
											<label class="checkbox-label unchecked" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													disabled="true"
													class="common"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
											<!-- /.checkbox-subadmin -->
										</div>
									</s:if>								
								</s:elseif>
								<s:elseif test="%{permission == 'Payment Advice'}">
									<div class="col-md-3 mb-10 d-none" id="paymentAdvice">
										<label class="checkbox-label unchecked" for="1">
											<s:property value="permission" />
											<s:checkbox
												name="lstPermissionType"
												id="%{permission}"												
												class="common"
												fieldValue="%{permission}"
												value="false"
												autocomplete="off">
											</s:checkbox>
										</label>
									</div>									
								</s:elseif>
								<s:else>
									<div class="col-md-3">
										<label class="checkbox-label unchecked mb-10" for="1">
											<s:property value="permission" />
											<s:checkbox
												name="lstPermissionType"
												id="%{permission}"
												fieldValue="%{permission}"
												value="false"
												autocomplete="off">
											</s:checkbox>
										</label>
									</div>
								</s:else>
							</s:iterator>
						</div>
						<!-- /.row -->
					</s:if>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 text-center">
					<s:a class="lpay_button lpay_button-md lpay_button-primary" action='userList'>Back</s:a>
					<button id="btnEditUser" name="btnEditUser" class="lpay_button lpay_button-md lpay_button-secondary">Save User</button>
					<!-- <s:submit id="btnEditUser" name ="btnEditUser" value="Save" type="button" cssClass="btn btn-success btn-md"> </s:submit> -->
				</div>
				<!-- /.col-md-12 -->
				<s:hidden name="payId" id="payId" />
				<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			</s:form>
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
</body>
</html>