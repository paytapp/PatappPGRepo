
<%@taglib prefix="s" uri="/struts-tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
	<title>Registration</title>

	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/fonts.css"/>
	<link rel="stylesheet" href="../css/login.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/commanValidate.js"></script>
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>

	<style>
		.merchant__form_group {	position: relative; }
		.position-relative { position: relative; }
		input[type="radio"]{margin-left: 10px!important;}
		#radiodiv span { font-size : 14px; margin-left: 4px; color: black !important; }
		#radioError {
			color: #ff0000!important;
			margin-left: 13px;
			font-size: 11px;
		}

		.text-danger { color: #ff0000 !important; }

		#subcategory {
			color: #555;
			font-weight: 600;
			margin-top: 15px;
			width: 100%;
		}

		.mainDiv .signuptextfield {
			height: 40px !important;
			font-size: 13px;
			width: 100% !important;
		}
		.error-text {
		color:#a94442;
		font-weight:bold;
		background-color:#f2dede;
		list-style-type:none;
		text-align:center;
		list-style-type: none;
		margin-top:10px;
		}
		.error-text li 
		{ 
		list-style-type:none;   
		}

		.d-none{
			display: none !important;
		}

		.lpay_input_group .errorSec{
			right: 0;
			position: absolute;
			top: 0;
			margin: 0;
		}

		#response{color:green;}
		.mainDiv .adduR{
			width: 402px;
			padding: 2% 0;
			border-radius: 5px;
			background: #fafafa;
			border-color:#e6e6e6;
		}
		.mainDiv .adduTR{
			width: 90%;
			float: none;
			margin: 0 auto;
			font-weight: 600;
			margin-bottom: 12px;
		}
		.mainDiv .adduTR:last-child{
			margin-bottom: 0;
		}
		.mainDiv .signuptextfield, .signupdropdwn{
			font-family: 'Open Sans', sans-serif;
			font-weight: 600;
		}

		.error{
			color: #f00;
			font-size: 11px;
			display: none;
		}

		.show{
			display: block;
		}

		.inputTitle{
		color: #7a7a7a;
			font-weight: 600;
			display: none;
		}
		#subcategorydiv.adduTR{
			width: 100%;
		}
		.errorSec {
			color: red;
			text-indent: 2px;
			font-size: 11px;
			display: none;
		}
	</style>
</head>

<body>
	<div class="signupCrm lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">User Registration</h2>
                </div>
                <!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-12 mb-20">
				<ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active">
                        <a href="#" class="lpay-nav-link" onclick="tabChange('singleRegistration')" data-id="singleRegistration">Single Registration</a>
                    </li>
					<li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" onclick="tabChange('bulkRegistration')" data-id="bulkRegistration">Bulk Registration</a>
                    </li>
				</ul>
				<!-- lpay_tabs -->
			</div>
			<!-- /.col-md-12 -->
			<div class="lpay_tabs_content w-100 " data-target="singleRegistration">
				<div class="col-md-12">
					<div id="saveMessage" class="lpay_success_wrapper">
						<s:if test="%{responseObject.responseCode=='000'}">
							<s:actionmessage class="lpay_success" />
						</s:if>
						<s:else>
							<!-- <class="error-text"><s:actionmessage theme="simple"/> -->
						</s:else>
					</div>
				</div>
				<!-- /.col-md-12 -->
				<s:form action="signupMerchant" id="formname" autocomplete="off">
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
						<label>Merchant/Reseller Type <span class="text-danger">*</span></label>
							<s:select
								name="userRoleType"
								id="userRoleType"
								headerKey="1"
								list="#{'merchant':'Create a Merchant','parentMerchant': 'Create a Parent Merchant','reseller':'Create a Reseller','superMerchant':'Create a Super Merchant','subMerchant':'Create a Sub Merchant'}" class="selectpicker">
							</s:select>					
						</div>
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 mb-20 d-none" data-id="isPartner">
						<div class="lpay_select_group">
							<label>Is Partner <span class="text-danger">*</span></label>
							<select name="isPartner" class="selectpicker" id="isPartner">
								<option value="off">No</option>
								<option value="on">Yes</option>
							</select>
						</div>
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 mb-20 map-verification">
						<div class="lpay_select_group">
							<label>MPA Verification Type <span class="text-danger">*</span></label>
							<select name="mpaOnlineOffLineFlag" class="selectpicker" id="mpaOnlineOffLineFlag">
								<option value="offline">Offline</option>
								<option value="online">Online</option>
							</select>
						</div>
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 mb-20 common-validation" id="superMerchantDiv">
						<div class="lpay_select_group">
							<label for="">Super Merchant Id <span class="text-danger">*</span></label>
							<s:select
								name="superMerchant"
								class="selectpicker"
								id="superMerchant"
								list="superMerchantList"
								headerKey=""
								headerValue="Select Super Merchant"
								listKey="superMerchantId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
						<p class="errorSec errorSubMerchant"></p>
					</div>
					<!-- /.col-md-3 -->
				
					<div class="col-md-4 mb-20 common-validation">
						<div class="lpay_input_group">
							<label>Business Name <span class="text-danger">*</span></label>
							<div class="txtnew" id="businessField">
								<s:textfield
									id="businessName"
									name="businessName"
									cssClass="lpay_input acquirer-input"
									autocomplete="off"
									maxlength="100"
									onkeydown="return alphaNumeric(event)"
								/>
							</div>
							<p class="errorSec errorBusninessName"></p>
						</div>
					</div>
					<!-- col-md-4 -->
	
					<div class="col-md-4 mb-20 common-validation">
						<div class="lpay_input_group">
							<label>Email <span class="text-danger">*</span></label>
							<div class="txtnew position-relative">
								<s:textfield
									id="emailId"
									name="emailId"
									cssClass="lpay_input acquirer-input"
									automcomplete="off"
									maxlength="50"
								/>
								<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
								<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
							</div>
							<p class="errorSec errorEmail"></p>
						</div>
						<!-- /.merchant__form_group -->
					</div>
					<!-- /.col-md-4 -->
				
					<div class="col-md-4 mb-20 common-validation">
						<div class="lpay_input_group">
							<label>Mobile Number <span class="text-danger">*</span></label>
							<div class="txtnew position-relative">
								<s:textfield
									id="loginNumber"
									maxlength="10"
									name="mobile"
									cssClass="lpay_input acquirer-input"
									oninput="onlyNumberInput(this)"
									automcomplete="false"
								/>
								<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
								<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
								<!-- <a href="#" class="icon-edit status-img" data-id="loginNumber"><i class="fa fa-pencil"></i></a> -->
							</div>
							<p class="errorSec errorPhone" id="errorPhone">Please Enter Valid Mobile Number</p>
						</div>
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group pin-div">
							<label>PIN <span class="text-danger">*</span></label>
							<div class="txtnew position-relative new-pin">
								<div class="otp-pin-wrapper">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox1">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox2">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox3">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox4">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox5">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox6">
								</div>
								<s:textfield id="pin" name="pin" type="hidden" cssClass="signuptextfield acquirer-input" placeholder="Password*"  maxlength="32" automcomplete="false" />
							</div>
							<p class="errorSec errorPassword">Please Enter Valid Password</p>
						</div>
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group pin-div">
							<label class="font-size-12 color-grey-light m-0 font-weight-medium">Confirm PIN <span class="text-danger">*</span></label>
							<div class="txtnew position-relative confirm-pin">
								<div class="otp-pin-wrapper">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox1">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox2">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox3">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox4">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox5">
									<input type="text" class="otp-input-common font-family-password"  data-id="pinBox6">
								</div>
								<s:textfield
									id="confirmPin"
									name="confirmPassword"
									type="hidden"
									cssClass="signuptextfield acquirer-input"
									automcomplete="false"
									maxlength="32"
								/>
							</div>
							<p class="errorSec passwordNotMatch" id="errorConfirmPassword">PIN Doesn't Match</p>
						</div>
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-12 text-center">
						<button id="submit" class="lpay_button lpay_button-md lpay_button-secondary" disabled>Save User Data</button>
					</div>
					<!-- /.col-md-4 -->
				</s:form>
			</div>
			<!-- lay_tabs_content -->
			<div class="lpay_tabs_content w-100 d-none" data-target="bulkRegistration">
				<div class="col-md-4">
					<form action="addBulkUsers" id="addBulkUsers" method="post" enctype="multipart/form-data">
						<label for="upload-input" class="lpay-upload">
							<input type="file" name="csvfile" accept=".csv" id="upload-input" class="lpay_upload_input">
							<div class="default-upload">
								<h3>Upload Your CSV File</h3>
								<img src="../image/image_placeholder.png" class="img-responsive" id="placeholder_img" alt="">
							</div>
							<!-- /.default-upload -->
							<div class="upload-status">
								<div class="success-wrapper upload-status-inner d-none">
									<div class="success-icon-box status-icon-box">
										<img src="../image/tick.png" alt="">
									</div>
									<div class="success-text-box">
										<h3>Upload Successfully</h3>
										<div class="fileInfo">
											<span id="fileName"></span>
										</div>
										<!-- /.fileInfo -->
									</div>
									<!-- /.success-text-box -->
								</div>
								<!-- /.success-wraper -->
								<div class="error-wrapper upload-status-inner d-none">
									<div class="error-icon-box status-icon-box">
										<img src="../image/wrong-tick.png" alt="">
									</div>
									<div class="error-text-box">
										<h3>Upload Failed</h3>
										<div class="fileInfo">
											<div id="fileName">File size too Long.</div>
										</div>
										<!-- /.fileInfo -->
									</div>
									<!-- /.success-text-box -->
								</div>
								<!-- /.success-wraper -->
							</div>
							<!-- /.upload-success -->
						</label>
					</form>
					<div class="button-wrapper lpay-center mt-20">
						<button class="lpay_button lpay_button-md lpay_button-secondary" disabled id="bulkUpdateSubmit">Submit</button>
						<!-- create table for download csv format -->
						<table id="example" class="display nowrap" style="display: none;">
							<thead>

								<tr>
									<th>Email Id</th>
									<th>Business Name</th>
									<th>Mobile Number</th>
									<th>Usertype</th>
								</tr>
								
							</thead>
						</table>
					</div>
					<!-- /.button-wrapper -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<s:if test="rowCount != null">
        <s:if test="rowCount!=0">
        <div class="row" id="showData">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="heading_with_icon mb-10">
                                  <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                    <h2 class="heading_text">Total Numbers of Data In CSV</h2>
                                </div>
                                <!-- /.heading_icon -->
                            </div>
                            <!-- /.col-md-12 -->
                            <div class="col-md-12 text-center lpay_xl">
                                <s:property value="rowCount"/>
                            </div>
                            <!-- /.col-md-12 -->
                        </div>
                        <!-- /.row -->
                    </section>
                    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
                <!-- <h4 class="bg-info p-2 mb-2 text-white h10 py-5 font-size-14">Total Numbers of Data In CSV</h4>
                <p><s:property value="rowCount"/></p> -->
            </div>
            <!-- col-md-6 -->
            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Successfully Stored</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12 text-center lpay_xl">
                            <s:property value="storedRow" />
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
                <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
            </div>
            <!-- /.col-md-6 -->
            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Duplicate Data</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12">
                            <div class="lpay_table_wrapper">
                                <table class="lpay_custom_table">
                                    <tr class="lpay_table_head"><th >Row No.</th><th>Email Id</th></tr>
                                    <s:iterator value="duplicate">
                                        <tr>
                                            <td><s:property value="key" /></td>
                                            <td><s:property value="value" /></td>
                                        </tr>
                                    </s:iterator>
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
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Failed Data</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12">
                            <div class="lpay_table_wrapper">
                                <table class="lpay_custom_table">
                                    <tr class="lpay_table_head"><th >Row No.</th><th>Email Id</th></tr>
                                    <s:iterator value="skipedRow">    
                                        <tr>
                                            <td><s:property value="key" /></td>
                                            <td><s:property value="value" /></td>
                                        </tr>
                                    </s:iterator>
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
        </div>
        </s:if>
    </s:if>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.lpay_tabs_content w-100 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.signupCrm lpay_section white-bg box-shadow-box mt-70 p20 -->

<!-- -->


<script src="../js/merchant-crm-signup.js"></script>
</body>
</html>