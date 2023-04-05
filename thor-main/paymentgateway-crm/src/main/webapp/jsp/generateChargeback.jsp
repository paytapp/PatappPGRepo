<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Chargeback</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js" type="text/javascript"></script>
<script src="../js/jquery-ui.js" type="text/javascript"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<link href="../css/loader.css" rel="stylesheet" type="text/css" /> 
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>

<style>
	.d-flex { display: flex; }
	.d-block { display: block; }
	.flex-wrap { flex-wrap: wrap; }
	.vh-70 { height: calc(100% - 70px); }
	.bg-white { background-color: white !important; }
	.text-danger { color: red; }
	.text-right { text-align: right; }
	.font-size-12 { font-size: 12px; }
	#amount-summary td { width: 40%; }
	#amount-summary td + td { width: 60%; }
</style>
</head>
<body>
	<s:form id="files" method="post" enctype="multipart/form-data">
	<s:actionmessage class="success success-text" />
	<div class="row d-flex flex-wrap">
		<div class="col-md-6">
			<section class="generate-chargeback lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Order Detail</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="lpay_table_wrapper">
							<table class="lpay_custom_table" width="100%">
								<tr style="display:none">
									<td></td>
									<td>
										<s:hidden name="txnId" value="%{txnId}"  />
									</td>
								</tr>
								<tr>
									<td>Order ID</td>
									<td>
										<s:property value="transDetails.orderId"></s:property>
										<s:hidden name="orderId" value="%{transDetails.orderId}"/>
										<s:hidden name="payId" 	value="%{transDetails.payId}" />
										<s:hidden name="pgRefNum" 	value="%{transDetails.pgRefNum}" />
									</td>
								</tr>
								<tr>
									<td>Date</td>
									<td><s:property value="transDetails.createDate" /></td>
								</tr>
								<tr>
									<td>Merchant Name</td>
									<td><s:property value="businessName" /></td>
								</tr>
								<s:if test="%{subMerchantName !=null}">
									<tr>
										<td>Sub-Merchant Name</td>
										<td>
											<s:property value="subMerchantName" />
										</td>
									</tr>
								</s:if>
								
								<tr>
									<td>Card Number Mask</td>
									<td>
										<s:if test="%{transDetails.cardNumber !=null}">
											<s:property value="transDetails.cardNumber" />
										</s:if>
										<s:else>Not applicable</s:else>
									</td>
								</tr>
								<tr>
									<td>Payment Method</td>
									<td>
										<s:property value="transDetails.paymentType" />&nbsp;(<s:property value="transDetails.mopType" />)
									</td>
								</tr>
								<tr>
									<td>Card Issuer Info</td>
									<td>
										<s:if test="%{transDetails.internalCardIssusserBank !=null}">
											<s:property	value="transDetails.internalCardIssusserBank" />
										</s:if>
										<s:else>Not applicable</s:else>
									</td>
								</tr>
								<tr>
									<td>Email</td>
									<td><s:property value="transDetails.custEmail" /></td>
								</tr>
								<tr>
									<td>Country</td>
									<td>
										<s:property value="transDetails.internalCustCountryName" />
									</td>
								</tr>
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
			<section class="generate-chargeback lapy_section white-bg box-shadow-box mt-70 p20 vh-70">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Amount Summary</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="lpay_table_wrapper">
							<table class="lpay_custom_table" width="100%" id="amount-summary">
								<tr>
									<td>Currency</td>
									<td>
										<s:property value="transDetails.currencyNameCode" />
									</td>
								</tr>
								<tr>
									<td>Sale Amount</td>
									<td>
										<s:property value="transDetails.authorizedAmount" />
									</td>
								</tr>
								<tr>
									<td>TDR (<s:property value="transDetails.merchantTDR" />% of B + D) [F]</td>
									<td>
										<s:property value="transDetails.merchantTDR" />
									</td>
								</tr>
								<tr>
									<td>Refunded Amount</td>
									<td>
										<s:property value="transDetails.refundedAmount" />
									</td>
								</tr>
								<tr>
									<td>Available for Refund</td>
									<td>
										<s:property value="transDetails.refundAvailable" />
									</td>
								</tr>
								<tr style="display: none">
									<td></td>
									<td>
										<s:hidden id="refundAvailable" value="%{transDetails.refundAvailable}"></s:hidden>
									</td>
								</tr>
								<tr>
									
										<td>
											Hold Amount Flag
										</td>
										<td>
											<label for="holdAmountFlag" class="checkbox-label unchecked mb-10">
												<input type="checkbox" id="holdAmountFlag" name="holdAmountFlag">
											</label>
										</td>
								</tr>
								<tr>
									<td>Chargeback Amount</td>
									<td class="lpay_input_group" style="white-space:unset;">
										<s:textfield
											name="chargebackAmount"
											id="chargebackAmount"
											autocomplete="off"
											value="%{transDetails.chargebackAmount}"
											cssClass="lpay_input bg-white"
											placeholder="Enter chargeback amount">
										</s:textfield>
										<span id="error-chargebackAmount" class="text-danger text-right font-size-12 d-none"></span>
									</td>
								</tr>
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
			<section class="generate-chargeback lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Case Type</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-6 mb-20">
						<div class="lpay_select_group">
							<label for="">Type <span class="text-danger">*</span></label>
							<s:select
								name="chargebackType"
								onchange="updateFormEnabled();"
								class="selectpicker"
								id="chargebackType"
								headerKey=""
								headerValue="Select Chargeback Type"
								list="@com.paymentgateway.crm.chargeback.util.ChargebackType@values()"
								listKey="code"
								listValue="name"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->

					<div class="col-md-6 mb-20">
						<div class="lpay_input_group">
							<label for="">Target Date <span class="text-danger">*</span></label>
							<s:textfield
								type="text"
								readonly="true"
								id="targetDate"
								onchange="updateFormEnabled();"
								name="targetDate"
								class="lpay_input dateTargetInput"
								autocomplete="off"
								placeholder="Please select date"
							/>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->

					<div class="col-md-6 mb-20">
						<label for="upload-input" class="lpay-upload">
							<input type="file" accept=".csv, .pdf" name="image" id="upload-input" class="lpay_upload_input">
							<div class="default-upload text-center">
								<h3>PDF or CSV file format</h3>
								<img src="../image/image_placeholder.png" class="img-responsive" id="placeholder_img" alt="">
							</div>
							<!-- /.default-upload -->

							<div class="upload-status">
								<div class="success-wrapper upload-status-inner d-none">
									<div class="success-icon-box status-icon-box">
										<img src="../image/tick.png" alt="">
									</div>
									<div class="success-text-box mt-10">
										<h3>Uploaded Successfully</h3>
										<div class="fileInfo mt-5">
											<span id="filename-success" class="d-block"></span>
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
									<div class="error-text-box mt-10">
										<h3>Upload Failed</h3>
										<div class="fileInfo mt-5">
											<div id="filename-error" class="d-block"></div>
										</div>
										<!-- /.fileInfo -->
									</div>
									<!-- /.success-text-box -->
								</div>
								<!-- /.success-wraper -->
							</div>
							<!-- /.upload-success -->
						</label>
						<!-- upload labe -->
					</div>
					<!-- /.col-md-6 -->

					<div class="col-md-6">
						<div class="lpay_input_group">
							<label for="">Add Comment <span class="text-danger">*</span></label>
							<s:textarea
								type="text"
								class="lpay_input"
								id="comments"
								name="comments"
								autocomplete="off"
								cols="10"
								rows="6"
								placeholder="Please enter comment"
							/>
							<span class="text-danger invisible d-block text-right font-size-12" id="error-commentId">This field is required.</span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->

					<div class="col-md-12 text-center">
						<button class="lpay_button lpay_button-md lpay_button-secondary" id="chargebackSubmit">Generate Chargeback</button>
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
	</s:form>

	<script src="../js/generateChargeback.js"></script>
</body>
</html>