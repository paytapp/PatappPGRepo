<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>UPI AutoPay</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<link rel="stylesheet" href="../css/singleAccount.css">
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-validations.js"></script>

<style>

	.common-status{ padding: 5px 10px 4px;border-radius: 5px;font-weight: 600;background-color:#6fa6ff;color: #082552; }
	.active-status{ background-color: #c5f196;color: #3c6411; }
	.suspended-status{ background-color: #ccc;color: #464646; }
	.pending-status{ background-color: #ffda70;color: #8f6c08 }
	.terminated-status{ background-color: #f9a7a7;color: #6a1111; }
	.rejected-status{ background-color: #9f0c0c;color: #fff; }

	button.copy-btn {
		display: inline-block;
		padding: 6px 10px 4px;
		border-radius: 5px;
		border: none;
		background-color: #ddfdf7;
		border: 1px solid #87c5b9;
		font-weight: 600;
		text-transform: uppercase;
	}

	.lpay_button.act-btn{ padding: 7px 14px;font-weight: 600 !important;color: #e7f0ff;margin-left: 0 !important; }


    button#selectAll {
        position: absolute;
        padding: 5px 10px;
        left: 10px;
        top: -24px
    }
    span#selectRow {
    padding: 5px 8px;
    background-color: #fff;
    border-radius: 5px;
    color: #000;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    }

	tr{
		cursor: pointer;
	}

	#datatable tr{
		cursor: default;
	}

	td.debit-column.lpay_select_group .btn-group {
		margin-left: -6px;
	}

	td.debit-column .bootstrap-select.open .dropdown-toggle:focus, td.debit-column.lpay_select_group .bootstrap-select .dropdown-toggle:hover{ background-color: #fff !important; }

	.response-popup {
            position: fixed;
            top: 0px;
            right: 0;
            left: 0;
            bottom: 0px;
            background-color: rgba(0, 0, 0, 0.8);
            z-index: 99;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .lpay_table{
            white-space: nowrap;
        }

		.imp{
			color: #f32a2a;
			margin-left: 2px;
		}

        .response-popup--inner {
            width: 400px;
            background-color: #fff;
            padding: 30px;
            display: flex;
            align-items: center;
            flex-direction: column;
        }

        .response-popup--inner .response-msg {
            margin-top: 20px;
            margin-bottom: 20px;
            font-size: 12px;
            text-align: center;
        }

        .response-btn a {
            background: #23527c;
            padding: 8px 18px;
            color: #fff;
            text-decoration: none;
            font-size: 14px;
            border-radius: 5px;
        }
        .debit_popup_inner{
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
        }
        .debit_popup_container .debit_popup_box{
            position: static !important;
            max-height: 100% !important;

        }
        .debit_popup_container .button-wrapper{
            top: 15px;
        }

		.debit_duration-input{
			display: inline-block;
			width: 100%;
			text-align: center;
			border-left: 15px solid #fff;
			border-right: 15px solid #fff;
			padding: 20px;
			background-color: #f5f5f5;
		}

		.debit_duration-input label{
			text-align: left;
		}

		.debit_duration-input > div{
			float: none;
			display: inline-block;
		}

		.action-btn > *, .action-btn-edit > *{
			display: inline-flex;
			width: 35px;
			height: 35px;
			align-items: center;
			justify-content: center;
			border-radius: 50%;
			background-color: #fff;
			box-shadow: 0 0 4px rgb(0 0 0 / 20%);
			font-size: 13px;
		}

		.action-btn .delete-btn, .action-btn-edit .delete-btn{
			background-color: #f32a2a;
			color: #fff;
			margin-left: 10px;
		}

		.action-btn-edit{
			display: none;
		}

		.edit-row .action-btn{
			display: none;
		}

		.edit-row .action-btn-edit{
			display: block;
		}

		button:disabled{
			opacity: .5;
		}

</style>
</head>
<!-- /.edit-permission -->
<body class="bodyColor">
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">UPI Autopay</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
					<li class="lpay-nav-item active"><a href="#" class="lpay-nav-link" data-id="quickLinks">Mandate Link</a></li>
                    <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="reporting">UPI Registration Report</a></li>
                    <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="transactionReport">UPI Transaction Report</a></li>
                    <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="debitReport">UPI Debit Report</a></li>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="debitDuration">Debit Duration</a></li>
					</s:if>					
                </ul>
            </div>
            <!-- /.col-md-12 -->

			<div class="lpay_tabs_content w-100" data-target="quickLinks">
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20 single-account-input">
						<div class="lpay_select_group">
							<label for="">Select Merchant <span class="imp">*</span></label>
							<s:select
								name="merchantEmailId"
								data-download="merchantPayId"
								data-link="merchantPayId"
								class="selectpicker mandate-link-field"
								id="merchantDebit"
								data-submerchant="subMerchantDebit"
								data-user="subUser"
								headerKey="ALL"
								data-live-search="true"
								headerValue="ALL"
								list="merchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						<div class="col-md-3 mb-20 single-account-input required">
							<div class="lpay_select_group">
								<label for="">Select Merchant <span class="imp">*</span></label>
								<s:select
									name="merchantEmailId"
									onchange="removeErrorLink(this); enableDisabledButton(this);"
									onblur="enableDisabledButton(this);"
									data-download="merchantPayId"
									class="selectpicker mandate-link-field"
									id="merchantLink"
									headerKey=""
									data-link="merchantPayId"
									data-submerchant="subMerchantLink"
									data-user="subUser"
									data-live-search="true"
									headerValue="Select Merchant"
									list="merchantList"
									listKey="payId"
									listValue="businessName"
									autocomplete="off"
								/>
								<span class="error-field"></span>
							</div>
						</div>
					</s:if>
					<s:else>
						<div class="col-md-3 mb-20 d-none debit-report-input">
							<div class="lpay_select_group ">
								<label for="">Select Merchant <span class="imp">*</span></label>
								<s:select
									name="merchantEmailId"
									data-download="merchantPayId"
									data-link="merchantEmailId"
									data-live-search="true"
									class="selectpicker mandate-link-field"
									id="merchantLink"
									list="merchantList"
									data-submerchant="subMerchantList"
									data-user="subUser"
									listKey="emailId"
									listValue="businessName"
									autocomplete="off"
									onchange="enableDisabledButton(this);"
									onblur="enableDisabledButton(this);"
								/>
							</div>
						</div>
					</s:else>
				</s:else>
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20 debit-report-input" data-target="subMerchantLink">
						<div class="lpay_select_group">
							<label for="">Sub Merchant <span class="imp">*</span></label>
							<s:select
								data-id="subMerchant"
								data-download="subMerchantPayId"
								data-link="subMerchantEmailId"
								data-submerchant="subMerchantLink"
								data-user="subUser"
								name="subMerchantEmailId"
								class="selectpicker mandate-link-field"
								id="subMerchantLink"
								list="subMerchantList"
								listKey="payId"
								headerKey="ALL"
								headerValue="ALL"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none single-account-input required" data-hide="subMerchant" data-target="subMerchantLink"> 
						<div class="lpay_select_group">
							<label for="">Sub Merchant <span class="imp">*</span></label>
							<select
								name="subMerchantEmailId"
								onchange="removeErrorLink(this); enableDisabledButton(this);"
								onblur="enableDisabledButton(this);"
								data-download="subMerchantPayId"
								headerValue="ALL"
								data-link="subMerchantEmailId"
								data-submerchant="subMerchantLink"
								data-user="subUser"
								id="subMerchantLink"
								class="mandate-link-field">
							</select>
							<span class="error-field"></span>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>
				<s:if test="%{#session.SUBUSERFLAG == true}">
					<div class="col-md-3 mb-20 single-account-input" data-target="subUser">
						<div class="lpay_select_group">
							<label for="">Sub User <span class="imp">*</span></label>
							<s:select
								data-id="subUser"
								data-download="subUserPayId"
								headerValue="ALL"
								data-link="subUserPayId"
								name="subUserPayId"
								class="selectpicker mandate-link-field"
								id="subuserLink"
								list="subUserList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
								onchange="enableDisabledButton(this);"
								onblur="enableDisabledButton(this);"
							/>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 single-account-input d-none" data-target="subUser"> 
						<div class="lpay_select_group">
							<label for="">Sub User <span class="imp">*</span></label>
							<select
								name="subUserPayId"
								data-download="subUserPayId"
								data-link="subUserPayId"
								id="subUserLink"
								onchange="enableDisabledButton(this);"
								onblur="enableDisabledButton(this);"
								class="mandate-link-field">
							</select>
							<span class="error-field"></span>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
				</s:else>
				<!-- merchant dropdown remove here -->
				<div class="col-md-3 mb-20 single-account-input required">
					<div class="lpay_input_group">
						<label for="">Email ID <span class="imp">*</span></label>
						<input
							type="text"
							onchange="checkRegEx(this);"
							oninput="removeErrorLink(this); enableDisabledButton(this);"
							onblur="enableDisabledButton(this);"
							data-regex="^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$"
							class="lpay_input mandate-link-field"
							name="custEmailId"
							data-link="custEmailId"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20 single-account-input required">
					<div class="lpay_input_group">
						<label for="">Mobile Number <span class="imp">*</span></label>
						<input
							type="text"
							onchange="checkRegEx(this);"
							oninput="removeErrorLink(this); enableDisabledButton(this);"
							onblur="enableDisabledButton(this);"
							data-regex="[0-9]{10}"
							class="lpay_input mandate-link-field"
							maxlength="10"
							onkeypress="mzOnlyNumbers(event)"
							name="custMobile"
							data-link="custMobile"
							id="custMobile"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20 single-account-input required">
					<div class="lpay_input_group">
						<label for="">Debit Amount <span class="imp">*</span></label>
						<input
							type="text"
							class="lpay_input mandate-link-field"
							oninput="removeErrorLink(this); enableDisabledButton(this); onlyNumericKey(this, event, 2);"
							onblur="enableDisabledButton(this);"
							name="monthlyAmount"
							data-link="monthlyAmount"
							autocomplete="off"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-3 mb-20 single-account-input required">
					<div class="lpay_input_group">
						<label for="">Tenure <span class="imp">*</span></label>
						<input
							type="text"
							oninput="removeErrorLink(this); enableDisabledButton(this);"
							onblur="enableDisabledButton(this);"
							onkeypress="mzDigitDot(event)"
							class="lpay_input mandate-link-field"
							name="tenure"
							data-link="tenure"
						/>
						<span class="error-field"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>

				<div class="col-md-3 single-account-input required">
					<div class="lpay_select_group">
					   <label for="">Frequency <span class="imp">*</span></label>
					   <select name="frequency" class="selectpicker mandate-link-field" onchange="removeErrorLink(this); enableDisabledButton(this);" onblur="enableDisabledButton(this);" id="frequency" data-link="frequency">
						   <option value="">Select Frequency</option>
						   <option value="OT">One Time</option>
						   <option value="DL">Daily</option>
						   <option value="WK">Weekly</option>
						   <option value="BM">Bi-Monthly</option>
						   <option value="MT">Monthly</option>
						   <option value="QT">Quarterly</option>
						   <option value="HY">Semi Annually</option>
						   <option value="YR">Yearly</option>
						   <option value="AS">As and when presented (ADHO)</option>
					   </select>
					   <span class="error-field"></span>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-12 text-center">
					<button onclick="sendLink(this)" disabled data-info="sms" data-connect="custMobile" class="lpay_button btn-mandate-link lpay_button-md lpay_button-primary">Send SMS</button>
					<button onclick="sendLink(this)" disabled data-info="email" data-connect="custEmailId" class="lpay_button btn-mandate-link lpay_button-md lpay_button-secondary">Send Email</button>
					<button onclick="sendLink(this)" disabled data-info="both" class="lpay_button btn-mandate-link lpay_button-md lpay_button-primary">Send Both</button>
				</div>
				<!-- /.col-md-12 -->
			</div>

            <div class="lpay_tabs_content w-100 d-none" data-target="reporting">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                    	<label for="">Merchant</label>
                    	<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                       		<s:select
							   name="merchant"
							   class="selectpicker lpay-input merchant-class blankInput"
							   data-get="subMerchant"
							   data-submerchant="subMerchant"
							   data-var="merchantPayId"
							   data-id="lpay-input"
							   id="merchant"
							   headerKey="ALL"
							   data-live-search="true"
							   headerValue="ALL"
							   list="merchantList"
							   listKey="payId"
							   listValue="businessName"
							   autocomplete="off"
							/>
                        </s:if>
                        <s:else>
                            <s:select
								name="merchant"
								data-get="subMerchant"
								data-live-search="true"
								class="selectpicker merchant-class lpay-input blankInput"
								id="merchant"
								list="merchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-4 mb-20 -->
                <s:if test="%{#session['USER'].superMerchant == true}">
                    <div class="col-md-3 mb-20" data-id="submerchant" data-target="subMerchant">
                        <div class="lpay_select_group">
                            <label for="">Sub Merchant</label>
                            <s:select
								data-id="subMerchant"
								data-var="subMerchantPayId"
								name="subMerchant"
								class="selectpicker"
								id="subMerchant"
								list="subMerchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
								headerKey="ALL"
								headerValue="ALL"
							/>
                        </div>
                      <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->	
                </s:if>
                <s:else>
                    <div class="col-md-3 mb-20 d-none" data-id="submerchant" data-target="subMerchant">
                    	<div class="lpay_select_group">
                        	<label for="">Sub Merchant</label>
                        	<select
								name="subMerchant"
								data-id="subMerchant"
								data-var="subMerchantPayId"
								id="subMerchant"
								class="">
							</select>
						</div>
						<!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                </s:else>
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Status</label>
                       <select name="status" id="status" data-var="status" class="selectpicker">
                            <option value="ALL">ALL</option>
                            <option value="Pending">Pending</option>
                            <option value="Failed">Failed</option>
                            <option value="Cancelled">Cancelled</option>
                            <option value="Captured">Captured</option>
							<option value="Processing">Processing</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date From</label>
                        <input
							type="text"
							class="lpay_input datepick"
							name="dateFrom"
							data-var="dateFrom"
						/>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date To</label>
                        <input
							type="text"
							class="lpay_input datepick"
							name="dateTo"
							data-var="dateTo"
						/>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Order ID</label>
                        <input
							type="text"
							class="lpay_input"
							name="orderId"
							data-var="orderId"
						/>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">UMN Number</label>
                        <input
							type="text"
							name="umnNumber"
							class="lpay_input"
							data-var="umnNumber"
						/>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>
					<button class="lpay_button lpay_button-md lpay_button-primary" id="registrationDownload">Download</button>
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 lpay_table_style-2 mt-10">
                    <div class="lpay_table">
                        <table id="upiAutopay-table" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th>Merchant Name</th>
                                    <th>Order ID</th>
                                    <th>Registration Date</th>
                                    <th>Registration Amount</th>
                                    <th>Total Amount</th>
                                    <th>Status</th>
									<th>Link</th>
									<th>Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content w-100 d-none" data-target="transactionReport">
				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">Order ID</label>
					  <s:textfield id="orderId" data-transaction="orderId" class="lpay_input" name="orderId"
					  type="text" value="" autocomplete="off"
					  onkeypress="return Validate(event);"
					  onblur="this.value=removeSpaces(this.value);"></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 mb-20 -->
				  <div class="col-md-3 mb-20">
					  <div class="lpay_input_group">
						<label for="">UMN Number</label>
						<s:textfield id="umnNumber" data-transaction="umnNumber" class="lpay_input" name="umnNumber"
						type="text" value="" autocomplete="off"
						onkeypress="javascript:return isNumberKey (event)" ></s:textfield>
					  </div>
					  <!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 mb-20 -->
				  <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					  <div class="col-md-3 mb-20">
						  <div class="lpay_select_group">
							  <label for="">Select Merchant</label>
							  <s:select name="merchantPayId" data-transaction="merchantPayId" class="selectpicker"
								  id="merchantTransaction" data-submerchant="subMerchantTransaction" data-user="subUserTransaction" headerKey="ALL" data-live-search="true" headerValue="ALL"
								  list="merchantList" listKey="payId"
								  listValue="businessName" autocomplete="off" />
						  </div>
					  </div>
				  </s:if>
				  <s:else>
					  <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						 <div class="col-md-3 mb-20">
						  <div class="lpay_select_group">
							  <label for="">Select Merchant</label>
							  <s:select name="merchantPayId" class="selectpicker"
								  id="merchantTransaction" headerKey="ALL" data-transaction="merchantPayId" data-submerchant="subMerchantTransaction" data-user="subUserTransaction"  data-live-search="true" headerValue="ALL"
								  list="merchantList" listKey="payId"
								  listValue="businessName" autocomplete="off" />
						  </div>
						 </div>
					  </s:if>
						  <s:else>
							  <div class="col-md-3 mb-20 d-none">
								  <div class="lpay_select_group ">
									  <label for="">Select Merchant</label>
									  <s:select name="merchantPayId" data-transaction="merchantPayId" data-live-search="true" class="selectpicker" id="merchantTransaction"
										  list="merchantList" data-submerchant="subMerchantTransaction" data-user="subUserTransaction" listKey="payId"
										  listValue="businessName" autocomplete="off" />
								  </div>
							  </div>
						  </s:else>
					  </s:else>
				  <s:if test="%{#session['USER'].superMerchant == true}">
					  <div class="col-md-3 mb-20" data-target="subMerchantTransaction">
						  <div class="lpay_select_group">
							 <label for="">Sub Merchant d</label>
							 <s:select data-id="subMerchant" data-transaction="subMerchantPayId" headerKey="ALL" data-submerchant="subMerchantTransaction" data-user="subUserTransaction"  name="subMerchantPayId" class="selectpicker" id="subMerchantTransaction" list="subMerchantList" listKey="payId" headerValue="ALL"
								  listValue="businessName" autocomplete="off" />
						  </div>
						  <!-- /.lpay_select_group -->  
					  </div>
					  <!-- /.col-md-3 -->	
				  </s:if>
				  <s:else>
					  <div class="col-md-3 mb-20 d-none" data-target="subMerchantTransaction"> 
						  <div class="lpay_select_group">
							 <label for="">Sub Merchant</label>
							 <select name="subMerchantPayId" headerKey="ALL" headerValue="ALL" data-transaction="subMerchantPayId" data-submerchant="subMerchantTransaction" data-user="subUserTransaction" id="subMerchantTransaction" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" headerValue="ALL"></select>
						  </div>
						  <!-- /.lpay_select_group -->  
					  </div>
					  <!-- /.col-md-3 -->							
				  </s:else>
				  <div class="col-md-3 mb-20">
					  <div class="lpay_select_group">
						 <label for="">Status</label>
						 <select class="selectpicker" data-transaction="status" data-id="reportStatus" name="status" id="status">
							<option value="ALL">ALL</option>
							<option value="Pending">Pending</option>
							<option value="Processing">Processing</option>
							<option value="Failed">Failed</option>
							<option value="Captured">Captured</option>
							<option value="Settled">Settled</option>
						 </select>
					  </div>
					  <!-- /.lpay_select_group -->  
				  </div>
				  <!-- /.col-md-3 -->
			  
				  <div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">Date From</label>
					  <s:textfield type="text" data-transaction="dateFrom" id="dateFrom" name="dateFrom"
					  class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 mb-20 -->
				  <div class="col-md-3 mb-20">
					<div class="lpay_input_group">
					  <label for="">Date To</label>
					  <s:textfield type="text" data-transaction="dateTo" id="dateTo" name="dateTo"
					  class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				  </div>
				  <!-- /.col-md-3 -->
				  <div class="col-md-12 text-center">
					  <input type="button" id="transactionSubmit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary" />
					  <button class="lpay_button lpay_button-md lpay_button-primary" id="downloadTransaction">
						  Download
					  </button>
					  <!-- /.lpay_button lpay_button-md lpay_button-primary -->
				  </div>
				  <!-- /.col-md-12 -->
				  <div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="upiAutopay-transactionReport" class="display" cellspacing="0"
								width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Merchant</th>
									<th>Order Id</th>
									<th>PG REF Number</th>
									<th>UMN Number</th>
									<th>Debit Transaction Amount</th>
									<th>Status</th>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->

			</div>
			<!-- /.lpay_tabs_content w-100 -->
            <div class="lpay_tabs_content w-100 d-none" data-target="debitReport">
				<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<div class="col-md-3 mb-20 debit-report-input">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select
								name="merchantEmailId"
								data-download="merchantPayId"
								data-debit="merchantPayId"
								class="selectpicker"
								id="merchantDebit"
								data-submerchant="subMerchantDebit"
								data-user="subUser"
								headerKey="ALL"
								data-live-search="true"
								headerValue="ALL"
								list="merchantList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:if>
				<s:else>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						<div class="col-md-3 mb-20 debit-report-input required">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select
									name="merchantEmailId"
									onchange="removeErrorLink(this)"
									data-download="merchantPayId"
									class="selectpicker"
									id="merchantDebit"
									headerKey="ALL"
									data-debit="merchantPayId"
									data-submerchant="subMerchantDebit"
									data-user="subUser"
									data-live-search="true"
									headerValue="ALL"
									list="merchantList"
									listKey="payId"
									listValue="businessName"
									autocomplete="off"
								/>
							</div>
						</div>
					</s:if>
					<s:else>
						<div class="col-md-3 mb-20 d-none debit-report-input">
							<div class="lpay_select_group ">
								<label for="">Select Merchant</label>
								<s:select
									name="merchantEmailId"
									data-download="merchantPayId"
									data-debit="merchantEmailId"
									data-live-search="true"
									class="selectpicker"
									id="merchantDebit"
									list="merchantList"
									data-submerchant="subMerchantDebit"
									data-user="subUser"
									listKey="payId"
									listValue="businessName"
									autocomplete="off"
								/>
							</div>
						</div>
					</s:else>
				</s:else>
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20 debit-report-input" data-target="subMerchantDebit">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<s:select
								data-id="subMerchant"
								data-download="subMerchantPayId"
								data-debit="subMerchantPayId"
								data-submerchant="subMerchantDebit"
								data-user="subUser"
								name="subMerchantEmailId"
								class="selectpicker"
								id="subMerchantDebit"
								list="subMerchantList"
								listKey="payId"
								headerKey="ALL"
								headerValue="ALL"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none debit-report-input" data-target="subMerchantDebit"> 
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<select
								name="subMerchantEmailId"
								data-download="subMerchantPayId"
								headerValue="ALL"
								data-debit="subMerchantPayId"
								data-submerchant="subMerchantDebit"
								data-user="subUser"
								id="subMerchantDebit"
								class="">
							</select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>
				<s:if test="%{#session.SUBUSERFLAG == true}">
					<div class="col-md-3 mb-20 debit-report-input" data-target="subUser">
						<div class="lpay_select_group">
							<label for="">Sub User</label>
							<s:select
								data-id="subUser"
								data-download="subUserPayId"
								headerValue="ALL"
								data-debit="subUserPayId"
								name="subUserPayId"
								class="selectpicker"
								id="subUser"
								list="subUserList"
								listKey="emailId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 debit-report-input d-none" data-target="subUser"> 
						<div class="lpay_select_group">
							<label for="">Sub User</label>
							<select name="subUserPayId" data-download="subUserPayId" data-debit="subUserPayId" id="subUser" class=""></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
				</s:else>
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">Date From</label>
						<s:textfield type="text" data-required="true" data-download="dateFrom" data-debit="dateFrom" id="dateFromDebit" name="dateFrom"
						class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">Date To</label>
						<s:textfield type="text" data-download="dateTo" data-debit="dateTo" id="dateToDebit" name="dateTo"
						class="lpay_input datepick" autocomplete="off" readonly="true" />
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->	
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_input_group">
						<label for="">UMN Number</label>
						<s:textfield id="umn" data-debit="umnNumber" class="lpay_input" name="umn"
						type="text" value="" autocomplete="off" ></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20 debit-report-input required">
					<div class="lpay_input_group">
						<label for="">Order ID</label>
						<s:textfield id="orderId" data-reg="^[0-9]{6}$" data-debit="orderId" class="lpay_input" name="orderId"
						type="text" value="" oninput="removeErrorLink(this)" autocomplete="off" ></s:textfield>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-12 text-center mb-10">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="view">View</button>
					<button class="lpay_button lpay_button-md lpay_button-primary" id="downlaod-button" onclick="createDownloadForm(this)">Download</button>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 lpay_table_style-2">
					<div class="lpay_table">
						<table id="datatable" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Merchant Name</th>
									<th>Sub Merchant Name</th>
									<th>Order ID</th>
									<th>Pg Ref Number</th>
									<th>UMN Number</th>
									<th>Registration Date</th>
									<th>Debit Transaction Amount</th>
									<th>Download</th>
								</tr>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->				
			</div>
			<!-- /.row -->
			<div class="lpay_tabs_content w-100 d-none" data-target="debitDuration">
				<div class="debit_duration-input">
					<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
						<div class="col-md-3 mb-20 debit-report-input">
							<div class="lpay_select_group">
								<label for="">Select Merchant</label>
								<s:select name="merchantEmailId" data-download="merchantPayId" data-duration="merchantPayId" class="selectpicker"
								id="merchantDebitDuration" data-submerchant="subMerchantDebitDuration" data-user="subUser" headerKey="ALL" data-live-search="true" headerValue="ALL"
								list="merchantList" listKey="emailId"
								listValue="businessName" autocomplete="off" />
							</div>
						</div>
					</s:if>
					<s:else>
						<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
							<div class="col-md-3 mb-20 debit-report-input required">
								<div class="lpay_select_group">
									<label for="">Select Merchant</label>
									<s:select name="merchantEmailId" onchange="removeErrorLink(this)" data-download="merchantPayId" class="selectpicker"
									id="merchantDebitDuration" headerKey="ALL" data-duration="merchantPayId" data-submerchant="subMerchantDebitDuration" data-user="subUser"  data-live-search="true" headerValue="ALL"
									list="merchantList" listKey="payId"
									listValue="businessName" autocomplete="off" />
								</div>
							</div>
						</s:if>
						<s:else>
							<div class="col-md-3 mb-20 d-none debit-report-input">
								<div class="lpay_select_group ">
									<label for="">Select Merchant</label>
									<s:select name="merchantEmailId" data-download="merchantPayId" data-duration="merchantEmailId" data-live-search="true" class="selectpicker" id="merchantDebitDuration"
									list="merchantList" data-submerchant="subMerchantDebitDuration" data-user="subUser" listKey="emailId"
									listValue="businessName" autocomplete="off" />
								</div>
							</div>
						</s:else>
					</s:else>
					<s:if test="%{#session['USER'].superMerchant == true}">
						<div class="col-md-3 mb-20 debit-report-input" data-target="subMerchantDebitDuration">
							<div class="lpay_select_group">
								<label for="">Sub Merchant</label>
								<s:select data-id="subMerchant" data-download="subMerchantPayId" data-duration="subMerchantPayId" data-submerchant="subMerchantDebitDuration" data-user="subUser"  name="subMerchantEmailId" class="selectpicker" id="subMerchantDebitDuration" list="subMerchantList" listKey="payId" headerKey="ALL" headerValue="ALL"
									listValue="businessName" autocomplete="off" />
							</div>
							<!-- /.lpay_select_group -->  
						</div>
						<!-- /.col-md-3 -->	
					</s:if>
					<s:else>
						<div class="col-md-3 mb-20 d-none debit-report-input" data-target="subMerchantDebitDuration"> 
							<div class="lpay_select_group">
								<label for="">Sub Merchant</label>
								<select name="subMerchantEmailId" data-download="subMerchantPayId" headerValue="ALL" data-duration="subMerchantPayId" data-debit="subMerchantPayId" data-submerchant="subMerchantDebitDuration" data-user="subUser" id="subMerchantDebitDuration" class=""></select>
							</div>
							<!-- /.lpay_select_group -->  
						</div>
						<!-- /.col-md-3 -->							
					</s:else>
					<s:if test="%{#session.SUBUSERFLAG == true}">
						<div class="col-md-3 mb-20 debit-report-input" data-target="subUserDuration">
							<div class="lpay_select_group">
								<label for="">Sub User</label>
								<s:select data-id="subUser" data-download="subUserPayId" headerValue="ALL" data-duration="subUserPayId" name="subUserPayId" class="selectpicker" id="subUserDuration" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
							</div>
							<!-- /.lpay_select_group -->  
						</div>
						<!-- /.col-md-3 -->	
					</s:if>
					<s:else>
						<div class="col-md-3 mb-20 debit-report-input d-none" data-target="subUserDuration"> 
							<div class="lpay_select_group">
								<label for="">Sub User</label>
								<select name="subUserPayId" data-download="subUserPayId" data-duration="subUserPayId" id="subUserDuration" class=""></select>
							</div>
							<!-- /.lpay_select_group -->  
						</div>
					</s:else>
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
						   <label for="">Debit Frequency(Days)</label>
						   <select class="selectpicker" data-duration="debitDuration" name="duration" id="duration">
								<option value="ALL">ALL</option>
								<option value="1">1</option>
								<option value="2">2</option>
								<option value="3">3</option>
								<option value="4">4</option>
								<option value="5">5</option>
								<option value="6">6</option>
								<option value="7">7</option>
								<option value="8">8</option>
								<option value="9">9</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
								<option value="16">16</option>
								<option value="17">17</option>
								<option value="18">18</option>
								<option value="19">19</option>
								<option value="20">20</option>
								<option value="21">21</option>
								<option value="22">22</option>
								<option value="23">23</option>
								<option value="24">24</option>
								<option value="25">25</option>
								<option value="26">26</option>
								<option value="27">27</option>
								<option value="28">28</option>
								<option value="29">29</option>
								<option value="30">30</option>
						   </select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->
					<div class="button-wrapper text-center" style="width: 100%">
						<button class="lpay_button lpay_button-md lpay_button-secondary debit-action-btn" onclick="saveDebitDuration(this)" id="saveDebitDuration">Save</button>
						<button class="lpay_button lpay_button-md lpay_button-primary debit-action-btn" onclick="viewDebitDuration()" id="viewDebitDuration">View</button>
						<button class="lpay_button lpay_button-md lpay_button-secondary debit-action-btn" onclick="downloadDebitDuration(this)" id="downloadDebitDuration">Download</button>
					</div>
					<!-- /.button-wrapper -->
				</div>
				<!-- /.debit_duration-input -->
				<div class="col-md-12 lpay_table_style-2" style="margin-top: 15px;">
					<div class="lpay_table">
						<table id="debitDurationTabel" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th>Merchant PayID</th>
									<th>Merchant Name</th>
									<th>Sub-Merchant PayID</th>
									<th>Sub-Merchant Name</th>
									<th>Debit Frequency (Days)</th>
									<th>Action</th>
								</tr>
							</thead>
						</table>
					</div>
					<!-- /.lpay_table -->
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.lpay_tabs_content w-100 d-none -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    
	<s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />
    <s:hidden name="vendorPayOutFlag" value="%{#session.vendorPayOutFlag}" />
	
    <div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">
                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Amount has been transferred successfully.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->

                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Nothing Found Try Again.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->

	<div class="debit_popup_container d-none">
        <div class="debit_popup_inner">
            <div class="debit_popup_box">
                <div class="default-heading">
                    <h3>Upi AutoPay Transaction Data</h3>
                </div>
                <!-- /.default-heading -->
                <div class="col-md-12">
                    <div class="lpay_table" id="lpay_table_popup">
                        <table id="datatablePopup" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th data-id="merchantName">Merchant Name</th>
                                    <th>Sub Merchant Name</th>
                                    <th data-id="orderId">Order ID</th>
                                    <th data-id="pgRefNum">Pg Ref Num</th>
                                    <th data-id="regPgRefNum">Pg Ref Num (Registration)</th>
                                    <th data-id="umnNumber">UMN Number</th>
                                    <th data-id="registrationDate">Registration Date</th>
									<th data-id="notificationDate">Notification Date</th>
                                    <th data-id="dueDate">Due Date</th>
                                    <th data-id="payerAddress">Payer VPA</th>
                                    <th data-id="email">Email</th>
                                    <th data-id="phone">Mobile</th>
                                    <th data-id="status">Status</th>
                                    <th data-id="maxAmount">Debit Amount</th>
                                    <th data-id="action">Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.debit_popup_box -->
        </div>
        <!-- /.debit_popup_inner -->
        <div class="button-wrapper button-wrapper-debit d-none">
            <button class="lpay_button lpay_button-md lpay_button-secondary close-btn">Close</button>
            <!-- /.lpay_button lpay_button-md lpay_button-primary -->
        </div>
        <!-- /.button-wrapper -->
    </div>
    <!-- /.debit_popup_container -->

	<form action="downloadUpiAutoPayDebitDuration" method="post" id="downloadFormDebitDuration"> 

    </form>

	<form action="#" id="downloadForm">

	</form>

    <script src="../js/upiAutopayReporting.js"></script>
	<script src="../js/decimalLimit.js"></script>
</body>
</html>