<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Sub-User List</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<link rel="stylesheet" href="../css/login.css">
	<link rel="stylesheet" href="../css/subAdmin.css">
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<!-- <script src="../js/jquery.minshowpop.js"></script> -->
	<!-- <script src="../js/jquery.formshowpop.js"></script> -->
	<script src="../js/commanValidate.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/userList.js"></script>
	<style>
		.displayNone { display: none; }
		.errorSec {
			color: red;
			text-indent: 2px;
			font-size: 11px;
			display: none;
		}
		.position-relative { position: relative; }
		.flex-wrap { flex-wrap: wrap; }
	</style>
</head>
<body>
	<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
		<input type="hidden" value="false" class="permission-flag" id="eNACH-status">
		<input type="hidden" value="false" class="permission-flag" id="merchantInitiatedDirectFlag">
		<input type="hidden" value="false" class="permission-flag" id="subMerchant-status">
		<input type="hidden" value="false" class="permission-flag" id="virtualAccountFlag">
		<input type="hidden" value="false" class="permission-flag" id="flag-customerQrFlag">
		<input type="hidden" value="false" class="permission-flag" id="flag-capturedMerchantFlag">
		<input type="hidden" value="false" class="permission-flag" id="flag-bookingReport">
		<input type="hidden" value="false" class="permission-flag" id="upiAutoPay-status">
	</s:if>
	<s:else>
		<s:textfield type="hidden" value="%{#session['USER'].eNachReportFlag}" class="permission-flag" id="eNACH-status" />
		<s:textfield type="hidden" value="%{#session['USER'].merchantInitiatedDirectFlag}" class="permission-flag" id="merchantInitiatedDirectFlag" />
		<s:textfield type="hidden" value="false" class="permission-flag" id="subMerchant-status" />
		<s:textfield type="hidden" value="%{#session['USER'].virtualAccountFlag}" class="permission-flag" id="virtualAccountFlag" />
		<s:textfield type="hidden" value="%{#session['USER'].customerQrFlag}" class="permission-flag" id="flag-customerQrFlag" />
		<s:textfield type="hidden" value="%{#session['USER'].capturedMerchantFlag}" class="permission-flag" id="flag-capturedMerchantFlag" />
		<s:textfield type="hidden" value="%{#session['USER'].bookingRecord}" class="permission-flag" id="flag-bookingReport" />
		<s:textfield type="hidden" value="%{#session['USER'].upiAutoPayReportFlag}" class="permission-flag" id="upiAutoPay-status" />
	</s:else>

	<section class="sub-merchant-list lpay_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o"
					aria-hidden="true"></i></span>
				<h2 class="heading_text">Sub-User List</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-12">
            <s:if test="%{responseObject.responseCode=='000'}">
              <div id="saveMessage" class="mb-20">
                <s:actionmessage class="success success-text" />
              </div>
            </s:if>
            <s:else>
              <div class="error-text" class="mb-20">
                <s:actionmessage/>
              </div>
            </s:else>
          </div>
          <!-- /.col-md-12 -->
		<div class="col-md-12 mb-20">
			<ul class="lpay_tabs d-flex">
				<li class="lpay-nav-item active"><a href="#"
					class="lpay-nav-link" data-id="subUserList">Sub-User List</a></li>
				<li class="lpay-nav-item"><a href="#" class="lpay-nav-link"
					data-id="addSubUser">Add Sub-User</a></li>
			</ul>
			<!-- /.lpay_tabs -->
		</div>
		<!-- /.col-md-12 -->
		<div class="lpay_tabs_content w-100" data-target="subUserList">
			<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
						<s:select name="merchantEmailId" data-var="merchantEmailId"
							class="selectpicker" id="merchantList"
							data-submerchant="subMerchantList" data-user="subUserList"
							headerKey="ALL" data-live-search="true" headerValue="ALL"
							list="merchantList" listKey="emailId"
							listValue="businessName" autocomplete="off" />
					</div>
				</div>
			</s:if>
			<s:else>
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select name="merchantEmailId" class="selectpicker"
								id="merchantList" headerKey="ALL" data-var="merchantEmailId"
								data-submerchant="subMerchantList" data-user="subUserList"
								data-live-search="true" headerValue="ALL"
								list="merchantList" listKey="emailId"
								listValue="businessName" autocomplete="off" />
						</div>
					</div>
					<!-- /.col-md-3 -->

				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none">
						<div class="lpay_select_group ">
							<label for="">Select Merchant</label>
							<s:select name="merchantEmailId" data-var="merchantEmailId"
								data-live-search="true" class="selectpicker" id="merchantList"
								list="merchantList" data-submerchant="subMerchantList"
								data-user="subUserList" listKey="emailId"
								listValue="businessName" autocomplete="off" />
						</div>
					</div>
				</s:else>
			</s:else>
			<s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-target="subMerchantList">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select data-id="subMerchant" data-var="subMerchantEmailId"
							data-submerchant="subMerchantList" data-user="subUserList"
							name="subMerchantEmailId" class="selectpicker"
							id="subMerchantList" list="subMerchantList" listKey="emailId"
							headerValue="ALL" listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 -->
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subMerchantList">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label> <select
							name="subMerchantEmailId" data-var="subMerchantEmailId"
							data-submerchant="subMerchantList" data-user="subUserList"
							id="subMerchantList" class=""></select>
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 -->
			</s:else>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Status</label>
					<s:select headerKey="" headerValue="All" class="selectpicker"
						list="#{'ACTIVE':'ACTIVE','PENDING':'PENDING','TRANSACTION_BLOCKED':'TRANSACTION_BLOCKED','SUSPENDED':'SUSPENDED','TERMINATED':'TERMINATED'}"
						name="status" id="status" value="name" autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->
			</div>
			<div class="col-md-12 text-center mb-20">
				<input type="button" id="submit" value="Submit"
					class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 text-center -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table width="100%" border="0" cellpadding="0" cellspacing="0"
						id="searchUserDataTable" class="display">
						<thead class="lpay_table_head">
							<tr>
								<th>PayId</th>
								<th>Email</th>
								<th>First Name</th>
								<th>Last Name</th>
								<th>Merchant Name</th>
								<th>Phone</th>
								<th>Creation Date</th>
								<th>Is Active</th>
								<th>Sub User Type</th>
								<th>Action</th>
								<th></th>
							</tr>
						</thead>
					</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->

		</div>
		<!-- /.lpay_tabs_content -->
		<div class="lpay_tabs_content d-none w-100" data-target="addSubUser">
			<s:form action="addUserAdminAction" id="frmAddUser">
				<s:token />
				<div class="col-md-6 mb-20">
					<div class="lpay_select_group">
						<label for="">Subuser Type</label>
						<select name="subUserType" id="subUserType" class="selectpicker acquirer-input">
							<option value="">Select SubUser Type</option>
							<option value="vendorType">Vendor Type</option>
							<option value="eposType">ePos Type</option>
							<option value="normalType">Normal Type</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 -->
				<div class="clearfix"></div>

				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-6 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select name="merchantEmailId" class="selectpicker"
								id="merchantPayId" data-submerchant="subMerchant"
								data-user="subUser" headerKey="ALL" data-live-search="true"
								headerValue="ALL" list="merchantList" listKey="emailId"
								listValue="businessName" autocomplete="off" />
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if
						test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						<div class="col-md-6 mb-20">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select name="merchantEmailId" class="selectpicker"
									id="merchantPayId" headerKey="" data-submerchant="subMerchant"
									data-user="subUser" data-live-search="true" headerValue="Select Merchant"
									list="merchantList" listKey="emailId" listValue="businessName"
									autocomplete="off" />
							</div>
						</div>
					</s:if>
					<s:else>
						<div class="col-md-6 mb-20 d-none">
							<div class="lpay_select_group ">
								<label for="">Select Merchant</label>
								<s:select name="merchantEmail" data-live-search="true"
									class="selectpicker" id="merchantPayId" list="merchantList"
									data-submerchant="subMerchant" data-user="subUser"
									listKey="emailId" listValue="businessName" autocomplete="off" />
							</div>
						</div>
					</s:else>
				</s:else>
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-6 mb-20" data-target="subMerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<s:select data-id="subMerchant" data-submerchant="subMerchant"
								data-user="subUser" name="subMerchantEmailId"
								class="selectpicker" id="subMerchant" list="subMerchantList"
								listKey="emailId" headerValue="ALL" listValue="businessName"
								autocomplete="off" />
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->
				</s:if>
				<s:else>
					<div class="col-md-6 mb-20 d-none" data-target="subMerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label> <select
								name="subMerchantEmailId" data-submerchant="subMerchant"
								data-user="subUser" id="subMerchant" class=""></select>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->
				</s:else>


				<!-- /.col-md-9 -->
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">First Name</label>
						<s:textfield name="firstName" id="fname"
							cssClass="lpay_input acquirer-input" autocomplete="off"
							onkeypress="noSpace(event,this);return isCharacterKey(event);" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 -->
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">Last Name</label>
						<s:textfield name="lastName" id="lname"
							cssClass="lpay_input acquirer-input" autocomplete="off"
							onkeypress="noSpace(event,this);return isCharacterKey(event);" />

					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 -->

				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
						<label for="">Business Name</label>
						<s:textfield name="businessName" id="businessName"
							cssClass="lpay_input acquirer-input" autocomplete="off"
							onkeypress="return lettersSpaceOnly(event, this);return isCharacterKey(event);" />

					</div>
					<!-- /.lpay_input_group -->
				</div>

				<div class="col-md-6 common-validation mb-20">
					<div class="lpay_input_group">
						<label for="">Mobile Number</label>
						<div class="position-relative">
							<s:textfield name="mobile" id="phoneNumber"
								cssClass="lpay_input acquirer-input" autocomplete="off"
								onkeypress="javascript:return isNumber (event)" maxlength="10" />
							<img src="../image/right-tick.png" alt="/"
								class="right-tick status-img"> <img
								src="../image/wrong.png" alt="/" class="wrong-tick status-img">
							</div>
							<p class="errorSec errorPhone" id="errorPhone">Please Enter Valid Mobile Number</p>
						<!-- /.position-relative -->

					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 -->
				<div class="col-md-6 mb-20 common-validation">
					<div class="lpay_input_group">
						<label for="">Email</label>
						<div class="position-relative">
							<s:textfield name="emailId" id="emailId"
								cssClass="lpay_input acquirer-input" autocomplete="off" />
							<img src="../image/right-tick.png" alt="/"
								class="right-tick status-img"> <img
								src="../image/wrong.png" alt="/" class="wrong-tick status-img">

						</div>
						<p class="errorSec errorEmail"></p>
						<!-- /.position-relative -->

					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-12 mb-20 d-none" data-id="permission">
					<div class="lpay_heading">
						<h3>Privilege Type</h3>
					</div>
					<!-- /.lpay_heading -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12" id="subUserPermission" data-id="permission">
					<div class="row">
						<s:iterator value="subUserPermissionType" status="itStatus">
							<div class="col-md-3 mb-20">
								<label class="checkbox-label unchecked" for="1"><s:property
										value="permission" /> <s:checkbox name="lstPermissionType"
										id="%{permission}" class="common" fieldValue="%{permission}"
										value="false" autocomplete="off"></s:checkbox> </label>
							</div>
							<!-- /.col-md-4 -->
						</s:iterator>
					</div>
					<!-- /.row -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 mb-20">
					<div class="lpay_heading">
						<h3>Account Privileges</h3>
					</div>
					<!-- /.lpay_heading -->
				</div>
				<!-- /.col-md-12 -->
				<div id="list-permission" class="col-xs-12">
					<div class="row d-flex flex-wrap">
						<s:iterator value="listPermissionType" status="itStatus">
							<s:if test="%{permission == 'Booking Report'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="bookingReport">
										<label class="checkbox-label unchecked" for="1">
											<s:property value="permission" />
											<s:checkbox
												name="lstPermissionType"
												id="%{permission}"
												class="common"
												disabled="true"
												fieldValue="%{permission}"
												value="false"
												autocomplete="off">
											</s:checkbox>
										</label>
										<!-- /.checkbox-subadmin -->
									</div>
								</s:if>
								<s:else>
									<s:if test="%{bookingReportFlag == true}">
										<div class="col-md-3 mb-20 d-none" id="bookingReport">
											<label class="checkbox-label unchecked" for="1">
												<s:property value="permission" />
												<s:checkbox
													name="lstPermissionType"
													id="%{permission}"
													class="common"
													disabled="true"
													fieldValue="%{permission}"
													value="false"
													autocomplete="off">
												</s:checkbox>
											</label>
											<!-- /.checkbox-subadmin -->
										</div>
									</s:if>
								</s:else>
							</s:if>
							<s:elseif test="%{permission == 'View Transaction Reports'}">
								<div class="col-md-3 mb-20" id="transactionPermission">
									<label class="checkbox-label unchecked" for="1">
										<s:property value="permission" />
										<s:checkbox
											name="lstPermissionType"
											id="%{permission}"
											class="common"
											disabled="true"
											fieldValue="%{permission}"
											value="false"
											autocomplete="off">
										</s:checkbox>
									</label>
									<!-- /.checkbox-subadmin -->
								</div>
							</s:elseif>
							<s:elseif test="%{permission == 'Payout'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER.UserType.name()=='MERCHANT'}">
									<div class="col-md-3 mb-20 d-none" id="MerchantInitiatedPermission">
										<label class="checkbox-label unchecked mb-10" for="1">
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
								<s:else>
									<s:if test="%{merchantInitiatedDirectFlag == true}">
										<div class="col-md-3 mb-20" id="MerchantInitiatedPermission">
											<label class="checkbox-label unchecked mb-10" for="1">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'Account Verification'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="AccountVerificationPermission">
										<label class="checkbox-label unchecked mb-10" for="1">
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
								<s:else>
									<s:if test="%{accountVerificationFlag == true}">
										<div class="col-md-3 mb-20" id="AccountVerificationPermission">
											<label class="checkbox-label unchecked mb-10" for="1">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'VPA Verification'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="VpaVerificationFlag">
										<label class="checkbox-label unchecked mb-10" for="1">
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
								<s:else>
									<s:if test="%{vpaVerificationFlag == true}">
										<div class="col-md-3 mb-20" id="VpaVerificationFlag">
											<label class="checkbox-label unchecked mb-10" for="1">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'eNACH Report'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="eNachReportFlag" data-id="eNACH-checkbox">
										<label class="checkbox-label unchecked mb-10" for="1">
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
								<s:else>
									<s:if test="%{eNachReportFlag == true}">
										<div class="col-md-3 mb-20" id="eNachReportFlag" data-id="eNACH-checkbox">
											<label class="checkbox-label unchecked mb-10" for="1">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'UPI AutoPay Report'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="upiAutoPayReportFlag" data-id="UpiAutoPay-checkbox">
										<label class="checkbox-label unchecked mb-10" for="1">
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
								<s:else>
									<s:if test="%{upiAutoPayReportFlag == true}">
										<div class="col-md-3 mb-20" id="upiAutoPayReportFlag" data-id="UpiAutoPay-checkbox">
											<label class="checkbox-label unchecked mb-10" for="1">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'eCollection Report'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="eCollectionReportFlag">
										<label class="checkbox-label unchecked mb-10" for="1">
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
								<s:else>
									<s:if test="%{virtualAccountFlag == true}">
										<div class="col-md-3 mb-20" id="eCollectionReportFlag">
											<label class="checkbox-label unchecked mb-10" for="1">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'Static UPI QR Report'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="customerQrFlag">
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
								<s:else>
									<s:if test="%{customerQrFlag == true}">
										<div class="col-md-3 mb-20" id="customerQrFlag">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'Custom Capture Report'}">
								<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
									<div class="col-md-3 mb-20 d-none" id="capturedMerchantFlag">
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
								<s:else>
									<s:if test="%{capturedMerchantFlag == true}">
										<div class="col-md-3 mb-20" id="capturedMerchantFlag">
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
								</s:else>
							</s:elseif>
							<s:elseif test="%{permission == 'Payment Advice'}">
								<div class="col-md-3 mb-20 d-none" id="paymentAdvice">
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
							</s:elseif>
							<s:else>
								<div class="col-md-3 mb-20">
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
							</s:else>
						</s:iterator>
					</div>
				</div>

				<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
				<div class="col-xs-12 text-center">
					<button disabled="true" id="saveButton" class="lpay_button lpay_button-md lpay_button-primary">Save User</button>
				</div>
				<!-- /.col-md-12 -->
			</s:form>
		</div>
		<!-- /.lpay_tabs_content -->
	</div>
	<!-- /.row --> </section>


	<script type="text/javascript">
		$(document).ready(function(e){
			$(".acquirer-input").val("");
   			var _checkClass = false;
			function checkBlankField(){
				$(".lpay_input").each(function(e){
					var _thisVal = $(this).val();
					if(_thisVal == ""){
					$("#saveButton").attr("disabled", true);
					_checkClass = false;
					return false;
					}else{
					_checkClass = true;
					$("#saveButton").attr("disabled", true);
					}
				});
				
				if(_checkClass == true) {
					if($("#subUserType").val() == "") {
						_checkClass = false;
					}
				}

				if(_checkClass == true) {
					if($(".verify-denied").length == 0){
					$("#saveButton").attr("disabled", false);
					}else{
					$("#saveButton").attr("disabled", true);
					}
				}else{
					$("#saveButton").attr("disabled", true);
				}
			}
  
    		$(".acquirer-input").on("change", checkBlankField);
			
			$(".checkbox-label").each(function(e){
				var _getId = $(this).find("input[type='checkbox']").attr("id");
				$(this).attr("for", _getId);
				if($(this).find("input[type='checkbox']").is(":checked")){
					$(this).addClass("checkbox-checked");
				}
			});
			$(".checkbox-label input[type='checkbox']").on("change", function(e){
				if($(this).is(":checked")){
					$(this).closest("label").addClass("checkbox-checked");
				}else{
					$(this).closest("label").removeClass("checkbox-checked");
				}
			});

			// signup button disabled
			function mailId(url, dataId, successMsg, param) {
				$("body").removeClass("loader--inactive");
				var _parent = $(dataId);
				var _id = _parent[0].id;
				var emailId = $(dataId).val();
				var dataObj = {};
				dataObj[param] = emailId;
			
				$.ajax({
					type: "post",
					url: url,
					data: dataObj,
					success: function(data){
						var _common = $(_parent).closest(".common-validation");
						var _label = _common.find(".errorSec");
						if(data[successMsg] == "success") {
							$(_parent).attr("readonly", true);
							_common.removeClass("verify-denied");
							_common.addClass("verify-success");
							_label.removeClass("show");
						} else {
							_label.text(data.response);
							_label.addClass("show");
							_common.addClass("verify-denied");
						}
						setTimeout(function(){
							$("body").addClass("loader--inactive");
						}, 1000);
					},
					error: function(data) {
					}
				});
			}

			$("#emailId").on("blur", function(e) {
				var _val = $(this).val();
				var _common = $(this).closest(".common-validation");
				var _label = _common.find(".errorSec");
				
				if(_val == " ") {
					$(this).val("");
				} else if(_val !== "") {
					if(isValidEmail(true)) {
						if(!_label.hasClass("show")) {
							if(!_common.hasClass("verify-success")) {
								mailId("emailAction", "#emailId", "emailSuccessStatus", "emailId");
							}
						}
					} else {
						_label.addClass("show");
						_label.text("Please enter valid email");
					}
				} else if(_val == "") {
					_label.addClass("show");
					_label.text("Please enter email");
				}
			});

			$("#emailId").on("keyup", function(e) {
				var _val = $(this).val();
				var _label = $(this).closest(".common-validation").find(".errorSec");

				if(_val == " ") {
					$(this).val("");
				}

				_label.removeClass("show");
				_label.text("");

				$(this).closest(".common-validation").removeClass("verify-denied");
			});

			$("#phoneNumber").on("keyup", function(e) {
				var _val = $(this).val();
				var _common = $(this).closest(".common-validation");
				
				if(_val == " ") {
					$(this).val("");
				} else if(_val !== "") {
					if(_val.length > 9) {
						mailId("mobileNumberValidate", "#phoneNumber", "phoneSuccessStatus", "phoneNumber");
					}
				}

        _common.removeClass("verify-denied");
        _common.find(".errorSec").removeClass("show");
	});
	
	function isValidEmail() {
        var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
        var emailValue = document.getElementById("emailId").value;
        if (emailValue.trim() && emailValue.match(emailexp)) {          
            return true;
        } else {
            //   document.getElementById("emailError").style.display = "block";
        }
    }

		})
	</script>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:form id="adminUserEdit" name="userDetails"
		action="editUserCallAction">
		<s:hidden name="emailId" id="emailAddress" value="" />
		<s:hidden name="firstName" id="firstName" value="" />
		<s:hidden name="lastName" id="lastName" value="" />
		<s:hidden name="mobile" id="mobile" value="" />
		<s:hidden name="createdDate" id="createdDate" value="" />
		<s:hidden name="isActive" id="isActive" value="" />
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
</body>
</html>