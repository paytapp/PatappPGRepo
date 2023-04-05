<%@page import="com.paymentgateway.commons.util.SaltFactory"%>
<%@page import="com.paymentgateway.commons.user.User"%>
<%@page import="com.paymentgateway.commons.util.Currency"%>
<%@page import="com.paymentgateway.commons.util.Amount"%>
<%@page import="com.paymentgateway.commons.util.FieldType"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="com.paymentgateway.commons.util.Constants"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Sub Merchant Account Setup</title>
	<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
	<!-- <link href="../css/custom.css" rel="stylesheet" type="text/css" /> -->
	<link rel="stylesheet" href="../css/jquery-ui.css">
	<script src="../js/continents.js" type="text/javascript"></script>
	<script src="../js/jquery.minshowpop.js"></script>
	<script src="../js/jquery.formshowpop.js"></script>
	<script src="../js/jquery.min.js"></script>
	<script src="../js/follw.js"></script>
	<script src="../js/jquery.popupoverlay.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/city_state.js"></script>
	<!--  loader scripts -->
	<script src="../js/loader/modernizr-2.6.2.min.js"></script>
	<script src="../js/loader/main.js"></script>
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/horizontal-scrolling-nav.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/submerchant-style.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

	<s:property value="%{userSetting.logoName}"></s:property>
</head>
<body>

	<s:hidden value="%{userSetting.merchantLogo}" data-id="merchantLogo"></s:hidden>
	<s:hidden value="%{showDownload}" data-id="showDownload"></s:hidden>
	<s:hidden value="%{userSetting.payId}" id="merchantPayId" data-id="payId"></s:hidden>


	<section class="merchant-account lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-4 p-0">
				<span class="heading-ribbon">
					<h4><s:property value="%{userSetting.businessName}"></s:property></h4>
				</span>
			</div>
		</div>
		<!-- /.row -->

		<div class="merchant__forms m-0">
			<s:form action="saveSubMerchantSetting" method="post" autocomplete="off" enctype="multipart/form-data" class="FlowupLabels" id="merchantForm" theme="css_xhtml">
				<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
				<s:hidden name="emailId" value="%{userSetting.emailId}"></s:hidden>
				<s:hidden name="businessName" value="%{userSetting.businessName}"></s:hidden>
				<s:hidden name="superMerchantName" value="%{userSetting.superMerchantName}"></s:hidden>
				<s:hidden name="payId" value="%{userSetting.payId}"></s:hidden>
				<s:if test="%{responseObject.responseCode=='101'}">
					<div id="saveMessage">
						<s:actionmessage class="success success-text" />
					</div>
				</s:if>
				<s:else>
					<div class="error-text">
						<s:actionmessage theme="simple" />
					</div>
				</s:else>

				<div class="merchant__forms_block active-block" data-active="configuration">
					<div class="row">
						<div class="col-md-6">
							<label for="eposMerchant" class="checkbox-label unchecked mb-10 label-config">								
								<s:checkbox
									id="eposMerchant"
									name="eposMerchant1"
									value="%{userSetting.eposMerchant}"
									data-id="eposMerchant"
								/>
								<s:hidden name="eposMerchant" value="%{userSetting.eposMerchant}" />
								ePOS Merchant 
							</label>
						</div>
						
						<div class="col-md-6">
							<label for="bookingRecord" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="bookingRecord"
									name="bookingRecord1"
									value="%{userSetting.bookingRecord}"
									data-id="bookingRecord"
								/>
								<s:hidden name="bookingRecord" value="%{userSetting.bookingRecord}" />
								Booking Report 
							</label>
						</div>
						
						<div class="col-md-6 mt-5">
							<label for="capturedMerchantFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="capturedMerchantFlag"
									name="capturedMerchantFlag1"
									value="%{userSetting.capturedMerchantFlag}"
									data-id="capturedMerchantFlag"
								/>
								<s:hidden name="capturedMerchantFlag" value="%{userSetting.capturedMerchantFlag}" />
								Custom Capture Report
							</label>
						</div>
						
						<div class="col-md-6 mt-5">
							<label for="logoFlag" class="checkbox-label unchecked mb-10 label-config">								
								<s:checkbox										
									id="logoFlag"
									name="logoFlag1"
									data-id="selfLogo"
									value="%{userSetting.logoFlag}"
								/>								
								<s:hidden name="logoFlag" value="%{userSetting.logoFlag}" />
								Sub Merchant Logo
							</label>
						</div>
	
						<div class="col-md-6 mt-5">
							<label for="retailMerchantFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="retailMerchantFlag"
									name="retailMerchantFlag1"
									value="%{userSetting.retailMerchantFlag}"
									data-id="retailMerchantFlag"
								/>
								<s:hidden name="retailMerchantFlag" value="%{userSetting.retailMerchantFlag}" />
								Retail Sub Merchant
							</label>
						</div>
	
						<!-- <div class="col-md-6 mt-5">
							<label for="accountVerificationFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="accountVerificationFlag"
									name="accountVerificationFlag1"
									value="%{userSetting.accountVerificationFlag}"
									data-id="accountVerificationFlag"
								/>
								<s:hidden name="accountVerificationFlag" value="%{userSetting.accountVerificationFlag}" />
								Account Verification
							</label>
						</div> -->
						
						<div class="col-md-6 mt-5">
							<label for="loadWalletFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="loadWalletFlag"
									name="loadWalletFlag1"
									value="%{userSetting.loadWalletFlag}"
									data-id="loadWalletFlag"
								/>
								<s:hidden name="loadWalletFlag" value="%{userSetting.loadWalletFlag}" />
								Load Wallet
							</label>
						</div>

						<div class="col-md-6 mt-5">
							<label for="paymentAdviceFlag" class="checkbox-label unchecked mb-10 label-config">
								Payment Advice Email <s:checkbox 
									name="paymentAdviceFlag"
									id="paymentAdviceFlag" 
									value="%{userSetting.paymentAdviceFlag}" 
									data-id="paymentAdviceFlag"/>
							</label>
						</div>
						
						<div class="col-md-6 mt-5">
							<label for="checkOutJsFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="checkOutJsFlag"
									name="checkOutJsFlag1"
									value="%{userSetting.checkOutJsFlag}"
									data-id="checkOutJsFlag"
								/>
								<s:hidden name="checkOutJsFlag" value="%{userSetting.checkOutJsFlag}" />
								Checkout JS
							</label>
						</div>
						<div class="col-md-6 mt-5">
                        <label for="upiHostedFlag" class="checkbox-label unchecked">
                            Upi Hosted Flag <s:checkbox name="upiHostedFlag"
                                id="upiHostedFlag" value="%{userSetting.upiHostedFlag}" />
                        </label>
                    </div>
						<div class="col-md-6 mt-5">
						<label for="configurableFlag" class="checkbox-label unchecked">
							Configurable Flag <s:checkbox name="configurableFlag"
								id="configurableFlag"
								value="%{userSetting.configurableFlag}" />
						</label>
						<div class="lpay_input_group d-none">
							<s:textfield
								type="text"
								data-reg="[0-9]"
								id="configurableTime"
								name="configurableTime"
								value="%{userSetting.configurableTime}"
								autocomplete="off"
								oninput="checkLength(this, 'Invalid Configurable Time')"
								class="lpay_input">
							</s:textfield>
							<span class="error-msg"></span>
						</div>
						<!-- /.lpay_select_group -->
					</div>
						<s:if test="%{eNachFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="eNachReportFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="eNachReportFlag"
									name="eNachReportFlag1"
									value="%{userSetting.eNachReportFlag}"
									data-id="eNachReportFlag"
								/>
								<s:hidden name="eNachReportFlag" value="%{userSetting.eNachReportFlag}" />
								eNach Report
							</label>
						</div>
						</s:if>
						<s:hidden value="%{eCollectionFeeFlag}" id='testId' />
						
						<s:if test="%{eCollectionFeeFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="allowECollectionFee" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="allowECollectionFee"
									name="allowECollectionFee"
									value="%{userSetting.allowECollectionFee}"
									data-id="allowECollectionFee"
								/>
								<s:hidden name="allowECollectionFee" value="%{userSetting.allowECollectionFee}" />
								E-Collection Fee
							</label>
						</div>
						</s:if>
						
						<s:if test="%{upiAutoPayFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="upiAutoPayReportFlag" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="upiAutoPayReportFlag"
									name="upiAutoPayReportFlag1"
									value="%{userSetting.upiAutoPayReportFlag}"
									data-id="upiAutoPayReportFlag"
								/>
								<s:hidden name="upiAutoPayReportFlag" value="%{userSetting.upiAutoPayReportFlag}" />
								upi AutoPay Report
							</label>
						</div>
						</s:if>
						
						<s:if test="%{customStatusEnquiryFlag == true}">
						<div class="col-md-6 mt-5">
							<label for="acceptPostSettledInEnquiry" class="checkbox-label unchecked mb-10 label-config">
								<s:checkbox
									id="acceptPostSettledInEnquiry"
									name="acceptPostSettledInEnquiry"
									value="%{userSetting.acceptPostSettledInEnquiry}"
									data-id="acceptPostSettledInEnquiry"
								/>
								<s:hidden name="acceptPostSettledInEnquiry" value="%{userSetting.acceptPostSettledInEnquiry}" />
								Custom Status Enquiry Flag
							</label>
						</div>
						</s:if>
					</div>

					<div class="row d-flex align-items-center">
						<div class="col-md-12 d-none upload-custom-logo">
							<label class="lable-default" id="title-upload-logo">Upload Logo</label>
							<div class="merchant__form_group d-flex align-items-center">								
								<label for="uploadMerchantLogo" class="upload-pic lable-default d-flex align-items-center mr-50" id="label-upload-logo">
									<img src="../image/cloud-computing.png" alt="/">
									<div class="upload-text">
										<span class="doc-text"><b style="color: #000">File size:</b> 2 MB</span>
										<span class="doc-file-name"><b style="color: #000">Formats:</b>.png / .jpg</span>
									</div>
									<div class="upload-name">
										<span class="doc-text" id="merchantLogoName"></span>
									</div> <!-- /.upload-name -->
									<div class="upload-size-error">
										<span class="" id="upload-error">File size too long.</span>
									</div> <!-- /.upload-size-error -->
									<input
										type="file"
										name="logoImageFile"
										id="uploadMerchantLogo"
										accept=".png, .jpg"
										class="feedback"
									>
								</label>

								<div class="uploaded-logo">
									<img
										src=""
										height="50"
										id="upload_img"
										class="img-responsive uploaded-logo"
										alt=""
									/>
								</div>
								<!-- /.uploaded-logo -->
								<s:hidden name="merchantLogo" value="%{userSetting.merchantLogo}" id="value-merchantLogo" />
							</div>
							<!-- /.merchant__form_group -->
						</div>
						<!-- /.col-md-4 mb-20 -->
					</div>

					<div class="row mt-30">

						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Callback Settings</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->
						<div class="col-md-3 mb-20">
	                        <label for="allCallBackFlag" class="checkbox-label unchecked">
	                            All Payemnt Type Callback Flag <s:checkbox name="allCallBackFlag"
	                                id="allCallBackFlag" value="%{userSetting.allCallBackFlag}" />
	                        </label>
                   		 </div>
						<div class="col-md-3 mb-20">
							<label for="callBackFlag" class="checkbox-label unchecked">
								Status Enquiry Callback Flag <s:checkbox name="callBackFlag"
									id="callBackFlag" value="%{userSetting.callBackFlag}" />
							</label>
						</div>
						<div class="col-md-3 mb-20">
							<div class="lpay_input_group">
								<label for="">Callback URL</label>
								<s:textfield id="callBackUrl" class="lpay_input"
								name="callBackUrl" type="text" value="%{userSetting.callBackUrl}" autocomplete="off"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-12"></div>
					<!-- /.col-md-12 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Payout Callback URL</label>
							<s:textfield id="callBackUrl" class="lpay_input"
								name="payoutCallbackUrl" type="text" value="%{userSetting.payoutCallbackUrl}" autocomplete="off"></s:textfield>
								<span class="error-msg"></span>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 mb-20 -->
					<div class="col-md-4 mt-20">
                        <label for="allowPayoutStatusEnquiryCallbackFlag" class="checkbox-label unchecked">
                            Payout Status Enquiry Callback Flag <s:checkbox name="allowPayoutStatusEnquiryCallbackFlag"
                                id="allowPayoutStatusEnquiryCallbackFlag" value="%{userSetting.allowPayoutStatusEnquiryCallbackFlag}" />
                        </label>
                    </div>
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Settlement Flag</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->

					
	 
						<div class="col-md-3 mb-20">
							<label for="merchantInitiatedDirectFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="merchantInitiatedDirectFlag"
									name="merchantInitiatedDirectFlag1"
									value="%{userSetting.merchantInitiatedDirectFlag}"
									data-id="merchantInitiatedDirectFlag"
									class="impsFlag"
								/>
								<s:hidden name="merchantInitiatedDirectFlag" value="%{userSetting.merchantInitiatedDirectFlag}" />
								Payout
							</label>
						</div>
	 
						

						<div class="col-md-3 mb-20">
							<label for="nodalReportFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="nodalReportFlag"
									name="nodalReportFlag1"
									value="%{userSetting.nodalReportFlag}"
									data-id="nodalReportFlag"
								/>
								<s:hidden name="nodalReportFlag" value="%{userSetting.nodalReportFlag}" />
								CIB Report
							</label>
						</div>
						
						<div class="col-md-3 mb-20">
							<label for="virtualAccountFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="virtualAccountFlag"
									name="virtualAccountFlag1"
									value="%{userSetting.virtualAccountFlag}"
									data-id="virtualAccountFlag"
								/>
								<s:hidden name="virtualAccountFlag" value="%{userSetting.virtualAccountFlag}" />
								Virtual Account
							</label>
						</div>
						
						<div class="col-md-3 mb-20">
							<label for="netSettledFlag" class="checkbox-label unchecked label-config">								
								<s:checkbox
									id="netSettledFlag"
									name="netSettledFlag1"
									value="%{userSetting.netSettledFlag}"
									data-id="netSettledFlag"
								/>
								<s:hidden name="netSettledFlag" value="%{userSetting.netSettledFlag}" />
								Net Settled Report
							</label>
						</div>
					</div>

					<s:if test="%{#session.USER.UserType.name()=='MERCHANT'}">
					<s:if
					test="%{#session['USER'].vpaVerificationFlag == true || #session['USER'].accountVerificationFlag == true}">

					<div class="row mt-3">
						<div class="col-md-12 mb-20">
                        <div class="inner-heading">
                            <h3>Beneficiary Verification Flags</h3>
                        </div>
                        <!-- /.inner-heading -->
                    </div>
					<s:if
						test="%{#session['USER'].accountVerificationFlag == true}">
					<div class="col-md-3 mb-20">
                        <label for="accountVerificationFlag" class="checkbox-label unchecked">
                            Account Verification <s:checkbox name="accountVerificationFlag"
                                id="accountVerificationFlag" value="%{userSetting.accountVerificationFlag}" />
                        </label>
                    </div>
					</s:if>
                    <!-- /.col-md-12 -->
					<s:if
						test="%{#session['USER'].vpaVerificationFlag == true}">
                    <div class="col-md-3 mb-20">
                        <label for="vpaVerificationFlag" class="checkbox-label unchecked">
                            VPA Verification
							<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" data-id="vpaVerificationFlag" value="%{userSetting.accountVerificationFlag}" />
                        </label>
                    </div>
					</s:if>
					</div>
					</s:if>
					</s:if>
					<s:else>
						<div class="row mt-3">
							<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>Beneficiary Verification Flags</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						
						<div class="col-md-3 mb-20">
							<label for="accountVerificationFlag" class="checkbox-label unchecked">
								Account Verification <s:checkbox name="accountVerificationFlag"
									id="accountVerificationFlag" value="%{userSetting.accountVerificationFlag}" />
							</label>
						</div>
						
						<!-- /.col-md-12 -->
						
						<div class="col-md-3 mb-20">
							<label for="vpaVerificationFlag" class="checkbox-label unchecked">
								VPA Verification
								<s:checkbox name="vpaVerificationFlag" id="vpaVerificationFlag" data-id="vpaVerificationFlag" value="%{userSetting.accountVerificationFlag}" />
							</label>
						</div>
						</div>

					</s:else>
					
					<div class="row mt-3">
						<div class="col-md-12 mb-20">
							<div class="inner-heading">
								<h3>QR Code</h3>
							</div>
							<!-- /.inner-heading -->
						</div>
						<!-- /.col-md-12 -->

						<div class="col-md-3 mb-20">
							<div class="d-inline-flex flex-column align-items-center">
								<a href="#" class="downloadQrCode" data-download="PGQR">
									<img src="../images/demo-qr-code.png" alt="" height="100">
								</a>
								<a href="#" class="downloadQrCode text-secondary font-weight-medium mt-5" data-download="PGQR">Download PG QR</a>
							</div>

							<label for="allowQRScanFlag" class="checkbox-label unchecked label-config mt-10">
								<s:checkbox
									id="allowQRScanFlag"
									name="allowQRScanFlag1"
									value="%{userSetting.allowQRScanFlag}"
									data-id="allowQRScanFlag"
								/>
								<s:hidden name="allowQRScanFlag" value="%{userSetting.allowQRScanFlag}" />
								Allow PG QR
							</label>
						</div>
	
						<div class="col-md-3 mb-20">
							<div class="d-inline-flex flex-column align-items-center">
								<a href="#" class="downloadQrCode" data-download="UPIQR">
									<img src="../images/demo-qr-code.png" alt="" height="100">
								</a>
								<a href="#" class="downloadQrCode text-secondary font-weight-medium mt-5" data-download="UPIQR">Download UPI QR</a>
							</div>

							<label for="allowUpiQRFlag" class="checkbox-label unchecked label-config mt-10">
								<s:checkbox
									id="allowUpiQRFlag"
									name="allowUpiQRFlag1"
									value="%{userSetting.allowUpiQRFlag}"
									data-id="allowUpiQRFlag"
								/>
								<s:hidden name="allowUpiQRFlag" value="%{userSetting.allowUpiQRFlag}" />
								Allow UPI QR
							</label>
						</div>
						
						<div class="col-md-3 mb-20">
						<label for="customerQrFlag" class="checkbox-label unchecked">
							Static UPI QR Report<s:checkbox name="customerQrFlag"
								id="customerQrFlag" value="%{userSetting.customerQrFlag}" />
						</label>
					</div>
					
					</div>
				</div>

				<div class="row submit-row submit-shift">
					<div class="col-md-12 text-center">
						<s:a class="lpay_button lpay_button-md lpay_button-primary" action='userSettingMerchant'>Back</s:a>
						<button id="btnSave" class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</s:form>

			
		</div>
		<!-- /.merchant__forms mt-30 -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:hidden name="token" id="customToken" value="%{#session.customToken}" />


	<script src="../js/main.js"></script>
	<script src="../js/merchantAccountSetup.js"></script>
	<script src="../js/submerchantFlag-script.js"></script>

	<script>
		var _isConfigurableFlagTrue = document.querySelector("#configurableFlag").checked;
		if(_isConfigurableFlagTrue === true){
			document.querySelector("#configurableTime").closest(".lpay_input_group").classList.remove("d-none");
		}
		
		$(document).ready(function() {
			$("body").on("click", ".downloadQrCode", function(e) {
				e.preventDefault();

				var qrType = $(this).attr("data-download");
				$("#qrType").val(qrType);
				$("#qrCodeDownloadForm").submit();
			});
		});
		
		$("#configurableFlag").on("change", function(e){
			var _isChecked = $(this).is(":checked");
			if(_isChecked){
				$("#configurableTime").closest(".lpay_input_group").removeClass("d-none");
			}else{
				$("#configurableTime").closest(".lpay_input_group").addClass("d-none");
			}
		});
	</script>
</body>
</html>