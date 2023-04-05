<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MPA Merchant Details</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/common-style.css">
<script src="../js/jquery.min.js"></script>
<!-- <script src="../js/assignMpaScript.js"></script> -->

<!--  loader scripts -->
<link rel="stylesheet" href="../css/subAdmin.css">
<!-- <link rel="stylesheet" href="../css/default.css"> -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<style>
    .download-button{
        padding: 12px 15px;
    }

    .merchantDetail-action{
        margin: 0;
    }

    .inner-heading{
        margin-bottom: 20px;
    }

</style>

</head>
<div class="object-data">
    <div id="merchantPayId"><s:property value="%{mpaMerchant.payId}" /></div>
    <div id="adminStatus"><s:property value="%{mpaMerchant.adminStatus}" /></div>
    <div id="userType"><s:property value="%{mpaMerchant.checkerMakerType}" /></div>
    <div id="checkerStatus"><s:property value="%{mpaMerchant.checkerStatus}" /></div>
    <div id="makerStatus"><s:property value="%{mpaMerchant.makerStatus}" /></div>
    <div id="makerFileName"><s:property value="%{mpaMerchant.makerFileName}" /></div>
    <div id="checkerFileName"><s:property value="%{mpaMerchant.checkerFileName}" /></div>
    <div id="makerId"><s:property value="%{mpaMerchant.makerPaId}" /></div>
    <div id="filePath"><s:property value="%{mpaMerchant.path}" /></div>
    <div id="adminFileName"><s:property value="%{mpaMerchant.adminFileName}" /></div>
    <div id="flag"><s:property value="%{mpaMerchant.isMpaOnlineFlag}" /></div>
</div>
<!-- /.object-data -->
<!-- /#userType -->

<!-- /#merchantMakerStatus -->

<!-- /.newVlo -->
<body class="bodyColo">
    
    <section class="merchantAssign lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">MPA Merchant Detail</h2>
				</div>
				<!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
        <div class="merchantAssing-response">
            <span id="responseText">Status updated successfully</span>
        </div>
        <!-- /.merchantAssing-response -->
        <s:if test="%{mpaMerchant.makerStatus != null}">
            <div class="merchantAssing-status">
                <div class="inner-heading position-relative">
                    <h3>Maker Status</h3>
                </div>
                <div class="merchnatStatus-head">
                    <span>Status</span>
                    <span>Comment</span>
                    <span></span>
                </div>
                <!-- /.merchnatStatus-head -->
                <div class="merchantStatus-data">
                    <div class="merchnatStatus-block">
                        <div id="maker-status">Approved</div>
                        <s:hidden data-di="maker-status" value="%{mpaMerchant.makerStatus}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                    <div class="merchnatStatus-block">
                        <div id="maker-feedback">This merchant has been approved from my side. All document of this merchant is valid.</div>
                        <s:hidden data-id="maker-feedback" value="%{mpaMerchant.makerComments}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                    <div class="merchnatStatus-block">
                        <a href="#" id="maker-action" class="download-button">Download</a>
                        <span class="not-uploaded-maker d-none">No file uploaded</span>
                        <s:hidden data-id="maker-action" value="%{mpaMerchant.makerFileName}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                </div>
                <!-- /.merchantStatus-data -->
            </div>
            <!-- /.merchantAssing-status -->
        </s:if>

        <s:if test="%{mpaMerchant.checkerStatus != null}">
            <div class="merchantAssing-status">
                <div class="inner-heading position-relative">
                    <h3>Checker Status</h3>
                </div>
                <div class="merchnatStatus-head">
                    <span>Status</span>
                    <span>Comment</span>
                    <span></span>
                </div>
                <!-- /.merchnatStatus-head -->
                <div class="merchantStatus-data">
                    <div class="merchnatStatus-block">
                        <div id="checker-status">Approved</div>
                        <s:hidden data-id="checker-status" value="%{mpaMerchant.checkerStatus}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                    <div class="merchnatStatus-block">
                        <div id="checker-feedback">This merchant has been approved from my side. All document of this merchant is valid.</div>
                        <s:hidden data-id="checker-feedback" value="%{mpaMerchant.checkerComments}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                    <div class="merchnatStatus-block">
                        <a href="#" id="checker-action" class="download-button">Download</a>
                        <span class="not-uploaded-checker d-none">No file uploaded</span>
                        <s:hidden data-di="checker-action" value="%{mpaMerchant.checkerFileName}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                </div>
                <!-- /.merchantStatus-data -->
            </div>
            <!-- /.merchantAssing-status -->
        </s:if>

        <s:if test="%{mpaMerchant.adminStatus != null}">
            <div class="merchantAssing-status">
                <div class="inner-heading position-relative">
                    <h3>Admin Status</h3>
                </div>
                <div class="merchnatStatus-head">
                    <span>Status</span>
                    <span>Comment</span>
                    <span></span>
                </div>
                <!-- /.merchnatStatus-head -->
                <div class="merchantStatus-data">
                    <div class="merchnatStatus-block">
                        <div id="admin-status">Approved</div>
                        <s:hidden data-id="admin-status" value="%{mpaMerchant.adminStatus}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                    <div class="merchnatStatus-block">
                        <div id="admin-feedback">This merchant has been approved from my side. All document of this merchant is valid.</div>
                        <s:hidden data-id="admin-feedback" value="%{mpaMerchant.adminComment}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                    <div class="merchnatStatus-block">
                        <a href="#" id="admin-action" class="download-button">Download</a>
                        <span class="not-uploaded-admin d-none">No file uploaded</span>
                        <s:hidden data-di="admin-action" value="%{mpaMerchant.adminFileName}"></s:hidden>
                    </div>
                    <!-- /.merchnatStatus-block -->
                </div>
                <!-- /.merchantStatus-data -->
            </div>
            <!-- /.merchantAssing-status -->
        </s:if>

        <div class="merchantAssign-filter d-none">
            <div class="row">
                <div class="col-md-4">
                    <div class="lpay_select_group">
                        <label for="">Status</label>
                        <select name="" class="selectpicker" id="Editchecker">
                            <option value="-1">Select Status</option>
                            <option value="Approved">Approve</option>
                            <option value="Rejected">Reject</option>
                        </select>
                    </div>
                    <!-- /.merchant__form_group -->
                </div>
                <!-- /.col-md-4 -->
                <div class="col-md-5">
                    <div class="lpay_input_group">
                        <label for="">Comments</label>
                        <input type="text" id="statusComment" class="lpay_input feedback">
                    </div>
                    <!-- /.merchant__form_group -->
                </div>
                <!-- /.col-md-6 -->
                <div class="col-md-3 upload-doc">
                    <form action="" enctype="multipart/form-data" id="fileUpload">
                    <div class="merchant__form_group">
                        <label for="" class="lable-default">Upload documents (if any)</label>
                        <label for="uploadDoc" class="upload-pic lable-default">
                            <img src="../image/cloud-computing.png" alt="/">
                            <div class="upload-text">
                                <span class="doc-text"><b style="color: #000">File size:</b> 2 MB</span>
                                <span class="doc-file-name"><b style="color: #000">Formats:</b> xls / xlsx / csv / pdf / png</span>
                            </div>
                            <input type="file" name="file" id="uploadDoc" class="feedback" accept=".png, .pdf, .xls, .xlsx, .csv" multiple>
                            <s:hidden name="payId" id="uploadFilePayId" value=""></s:hidden>
                        </label>
                    </div>
                    <!-- /.merchant__form_group -->
                </form>
                </div>
                <!-- /.col-md-2 upload-doc -->
                <div class="col-md-12 check-btn text-center mb-20">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="mpaSaveStatus">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.row -->
        </div>
        
        <!-- /.merchantAssign-filter -->
        <s:if test="%{mpaMerchant.isMpaOnlineFlag == 'YES'}">
            <div class="inner-heading position-relative">
                <h3>Merchant Checks</h3>
            </div>
            <!-- /.merchantAssign-filter -->
            <div class="merchantDetail-checks mb-20">
                <div class="company-pan-check">
                    <span>Company PAN Verification</span>
                </div>
                <!-- /.company-pan-check -->
                <div class="company-director-check">
                    <span>Company Director Verification</span>
                </div>
                <!-- /.company-pan-check -->
                <div class="company-director-check">
                    <span>GST Verification</span>
                </div>
                <!-- /.company-pan-check -->
                <div class="company-director-check">
                    <span>Company Name Verification</span>
                </div>
                <!-- /.company-pan-check -->
                <div class="company-director-check">
                    <span>CIN Verification</span>
                </div>
                <!-- /.company-pan-check -->
            </div>
        </s:if>
        <!-- /.merchantDetail-checks -->
        <div class="inner-heading">
            <h3>Merchant Details</h3>
        </div>
        <div class="merchantDetail-action">
            <div class="merchantDetail-box d-none">
                <span>Industry Category</span>
                <div id="industryCategory"></div>
                <!-- /#industry category -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Company Name</span>
                <div id="companyName">Payment Gateway Solution Private Limited</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>CIN Number</span>
                <div id="cin">UI12334GSEET445466</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Registered Email</span>
                <div id="companyEmailId">cmd@pg.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Registered Date</span>
                <div id="dateOfIncorporation">12/20/2020</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Registered Address</span>
                <div id="companyRegisteredAddress">D2/98, 3RD PUSTA SONIA VIHAR DLEHI - 110094</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Country</span>
                <div id="tradingCountry"></div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>City</span>
                <div id="tradingState">DELHI</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>PIN</span>
                <div id="tradingPin">DELHI</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Company PAN</span>
                <div id="businessPan">ACCPZ8787I</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>GST Number</span>
                <div id="gstin">GT939840029858594</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Company Phone</span>
                <div id="companyPhone">GT939840029858594</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Business Email</span>
                <div id="businessEmailForCommunication">md.zakaullah@Pg.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Website</span>
                <div id="companyWebsite">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Contact Person</span>
                <div id="contactName">GT939840029858594</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Contact Phone</span>
                <div id="contactMobile">md.zakaullah@Pg.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Contact Email</span>
                <div id="contactEmail">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
        </div>
        <!-- /.merchantDetail-action -->
        <div class="inner-heading mt-20" style="width: 100%">
            <h3>Merchant Account Detail</h3>
        </div>
        <div class="merchantDetail-action">
            <div class="merchantDetail-box d-none">
                <span>Account Holder Name</span>
                <div id="accountHolderName">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Account Number</span>
                <div id="accountNumber">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Account IFSC</span>
                <div id="accountIfsc">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Account Number</span>
                <div id="accountMobileNumber">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
        </div>
        <!-- /.merchantDetail-action -->
        <div class="inner-heading mt-20" style="width: 100%">
            <h3>Merchant Director Details One</h3>
        </div>
        <div class="merchantDetail-action">
            <div class="merchantDetail-box d-none">
                <span>Director Fullname</span>
                <div id="director1FullName">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Email</span>
                <div id="director1Email">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Mobile</span>
                <div id="director1Mobile">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Pan</span>
                <div id="director1Pan">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Address</span>
                <div id="director1Address">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
        </div>
        <!-- /.merchantDetail-action -->
        <div class="inner-heading mt-20" style="width: 100%">
            <h3>Merchant Director Details Two</h3>
        </div>
        <div class="merchantDetail-action">
            <div class="merchantDetail-box d-none">
                <span>Director Fullname</span>
                <div id="director2FullName">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Email</span>
                <div id="director2Email">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Mobile</span>
                <div id="director2Mobile">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Pan</span>
                <div id="director2Pan">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Director Address</span>
                <div id="director2Address">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
        </div>
        <!-- /.merchantDetail-action -->
            
        <div class="inner-heading mt-20" style="width: 100%">
            <h3>Business Details</h3>
        </div>
        <div class="merchantDetail-action">
            <div class="merchantDetail-box d-none">
                <span>Annual Turnover</span>
                <div id="annualTurnover">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Annual Turnover Online</span>
                <div id="annualTurnoverOnline">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Credit Card</span>
                <div id="percentageCC">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Debit Card</span>
                <div id="percentageDC">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Net Banking</span>
                <div id="percentageNB">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>UPI</span>
                <div id="percentageUP">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Wallets</span>
                <div id="percentageWL">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>EMI</span>
                <div id="percentageEM">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>COD/Cash</span>
                <div id="percentageCD">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>NEFT/IMPS/RTGS</span>
                <div id="percentageNeftOrImpsOrRtgs">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Total Cards Domestic</span>
                <div id="percentageDomestic">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Total Cards International</span>
                <div id="percentageInternational">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
        </div>
       <%--  <div class="inner-heading mt-20" style="width: 100%">
            <h3>Configuration</h3>
        </div>
        <div class="merchantDetail-action">
            <div class="merchantDetail-box d-none">
                <span>Merchant Type</span>
                <div id="annualTurnover">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Mode Type</span>
                <div id="surcharge">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Integration Type</span>
                <div id="integrationType">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Custom Invoice Design</span>
                <div id="customizedInvoiceDesign">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>International Cards</span>
                <div id="internationalCards">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Express Pay</span>
                <div id="expressPay">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Allow Duplicate Sale Order ID</span>
                <div id="allowDuplicateSaleOrderId">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Allow Duplicate Refund Order ID</span>
                <div id="allowDuplicateRefundOrderId">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Allow Duplicate Sale OrderId In Refund</span>
                <div id="allowDuplicateSaleOrderIdInRefund">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
            <div class="merchantDetail-box d-none">
                <span>Allow Duplicate Refund OrderID In Sale</span>
                <div id="allowDuplicateRefundOrderIdSale">www.example.com</div>
                <!-- /#companyName -->
            </div>
            <!-- /.merchantDetail-box d-none -->
        </div> --%>
            <%-- <div class="inner-heading mt-20" style="width: 100%">
                <h3>Technical Details</h3>
            </div>
            <div class="merchantDetail-action">
                <div class="merchantDetail-box d-none">
                    <span>Annual Turnover</span>
                    <div id="annualTurnover">www.example.com</div>
                    <!-- /#companyName -->
                </div>
                <!-- /.merchantDetail-box d-none -->
            </div> --%>
            
            <!-- merchant detail action -->
            <s:if test="%{mpaMerchant.isMpaOnlineFlag == 'YES'}">
	            <div class="inner-heading mt-20" style="width: 100%">
	                <h3>Merchant ESign Details</h3>
	            </div>
	            <div class="merchantDetail-action">
	                <div class="merchantDetail-box d-none">
	                    <span>ESign Name</span>
	                    <div id="esignName"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>YOB</span>
	                    <div id="esignYob"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>Gender</span>
	                    <div id="esignGender"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>Aadhar Type</span>
	                    <div id="esignAadharType"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>Aadhar Last Digit</span>
	                    <div id="esignUidLastFourDigits"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>Country</span>
	                    <div id="esignCountry"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>State</span>
	                    <div id="esignState"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	                <div class="merchantDetail-box d-none">
	                    <span>PIN Code</span>
	                    <div id="esignPincode"></div>
	                    <!-- /#companyName -->
	                </div>
	                <!-- /.merchantDetail-box d-none -->
	            </div>
            </s:if>
            <!-- /.merchantDetail-action -->

            <div class="inner-heading position-relative mt-20" style="width: 100%">
                <h3>Downloads</h3>
            </div>
            <div class="merchantDetail-button mb-20">
                <s:if test="%{mpaMerchant.isMpaOnlineFlag == 'YES'}">
                    <button class="lpay_button lpay_button-md lpay_button-secondary download-file" data-file="mpa">Download MPA files</button>
                    <button class="lpay_button lpay_button-md lpay_button-secondary download-file" data-file="esign">Download Agreement Copy</button>
                </s:if>
                <s:else>
                    <button class="lpay_button lpay_button-md lpay_button-secondary download-file" data-file="mpa">Download MPA files</button>
                </s:else>
            </div>
            <!-- /.merchantDetail-button -->
    </section>
    <!-- /.merchantAssign -->
    <s:form method="POST" id="checkerFileDownload" name="downloadAction" action="statusFileDownloadAction">
        <s:hidden name="token" value="%{#session.customToken}" />
        <s:hidden name="checkerFileName" value="%{mpaMerchant.checkerFileName}" />
        <s:hidden  name="payId" value="%{mpaMerchant.payId}" />
    </s:form>
 
    <s:form method="POST" id="makerFileDownload" name="downloadAction" action="statusFileDownloadAction">
        <s:hidden name="token" value="%{#session.customToken}" />
        <s:hidden name="makerFileName" value="%{mpaMerchant.makerFileName}" />
        <s:hidden  name="payId" value="%{mpaMerchant.payId}" />
    </s:form>

    <s:form method="POST" id="adminFileDownload" name="downloadAction" action="statusFileDownloadAction">
        <s:hidden name="token" value="%{#session.customToken}" />
        <s:hidden name="adminFileName" value="%{mpaMerchant.adminFileName}" />
        <s:hidden  name="payId" value="%{mpaMerchant.payId}" />
    </s:form>
    
    <s:form method="POST" id="fileDownload" action="mpaMerchantFileDownloadAction">
        <s:hidden name="token" value="%{#session.customToken}" />
        <!-- <s:hidden name="makerFileName" value="%{mpaMerchant.makerFileName}" /> -->
        <s:hidden name="fileNameType" id="fileNameType" />
        <s:hidden  name="payId" value="%{mpaMerchant.payId}" />
    </s:form>

    <s:hidden name="token" value="%{#session.customToken}" />
    <script type="text/javascript">

        $(window).on("load", function(e){
            $("body").addClass("loader--inactive"); 
        })

        $(document).ready(function(){
            $("body").removeClass("loader--inactive");
            var _token = $("[name='token']").val();

            // show filter div according to checker maker
            var _userType = $("#userType").text();
            if(_userType == "Checker"){
                var _getCheckerStatus = $("#checkerStatus").text();
                if(_getCheckerStatus == ""){
                    $(".merchantAssign-filter").removeClass("d-none");
                }else{
                    $(".merchantAssign-filter").addClass("d-none");
                }
            }

            if(_userType == "Maker"){
                var _getMakerStatus = $("#makerStatus").text();
                if(_getMakerStatus == ""){
                    $(".merchantAssign-filter").removeClass("d-none");
                }else{
                    $(".merchantAssign-filter").addClass("d-none");
                }
            }

            if(_userType == "Admin"){
               _adminStatus = $("#adminStatus").text();
               if(_adminStatus == ""){
                $(".merchantAssign-filter").removeClass("d-none");
               }else{
                $(".merchantAssign-filter").addClass("d-none");
               }
            }

            // download action creates

            // function downloadAction(){
            //     var _payId = $("#merchantPayId").text();
            //     var _getFile = 
            // }

            // download  action
            $(".download-file").on("click", function(e){
                var _fileType = $(this).attr("data-file");
                $("#fileNameType").val(_fileType);
                $("#fileDownload").submit();
            })

            // create function for download 
            $("#checker-action").on("click", function(e){
                $("#checkerFileDownload").submit();
                // $.ajax({
                //     type: "post",
                //     url: "statusFileDownloadAction",
                //     data: { "token": _token, "payId": _payId, "checkerFileName" : _getFile },
                //     success: function(data){
                //         console.log(data);
                //     }
                // })
            });

            $("#admin-action").on("click", function(e){
                $("#adminFileDownload").submit();
            })


            // create fnction download fro maker
            $("#maker-action").on("click", function(e){
                $("#makerFileDownload").submit();
                // var _parent = $(this).closest(".merchnatStatus-block");
                // var _payId = $("#merchantPayId").text();
                // var _getFile = _parent.find("input").val();
                // $.ajax({
                //     type: "post",
                //     url: "statusFileDownloadAction",
                //     data: { "token": _token, "payId": _payId, "makerFileName" : _getFile },
                //     success: function(data){
                //         console.log(data);
                //     }
                // })
 
            });

            // set value in status
            var _getPath = $("#filePath").text();
            var _checkerStatus = $("#checkerStatus").text();
            var _getCheckerFileName = $("#checkerFileName").text();
            var _getMakerFileName = $("#makerFileName").text();
            var _getAdminFileName = $("#adminFileName").text();
            var _adminStatus = $("#adminStatus").text();
            var _checkerFeedback = $("[data-id='checker-feedback']").val();
            var _adminFeedback = $("[data-id='admin-feedback']").val();
            var _makerStatus = $("#makerStatus").text();
            var _makerFeedback = $("[data-id='maker-feedback']").val();
            $("#maker-status").text(_makerStatus);
            $("#checker-status").text(_checkerStatus);
            $("#maker-feedback").text(_makerFeedback);
            $("#checker-feedback").text(_checkerFeedback);
            $("#admin-feedback").text(_adminFeedback);
            $("#admin-status").text(_adminStatus);

           /*  $("#maker-action").attr("href", _getPath+"/"+_getMakerFileName);
            $("#checker-action").attr("href", _getPath+"/"+_getCheckerFileName); */
            if(_getCheckerFileName == "" || _getCheckerFileName == undefined){
                $("#checker-action").css("display", "none");
                $(".not-uploaded-checker").removeClass("d-none");
            }
            if(_getMakerFileName == "" || _getMakerFileName == undefined){
                $("#maker-action").css("display", "none");
                $(".not-uploaded-maker").removeClass("d-none");
            }
            if(_getAdminFileName == "" || _getAdminFileName == undefined){
                $("#admin-action").css("display", "none");
                $(".not-uploaded-admin").removeClass("d-none");
            }
            // upload file 
            function uploadFunc(){
                var _file = this.files[0].size;
                var _filePath = $(this).val();
                var names = [];
                for (var i = 0; i < $(this).get(0).files.length; ++i) {
                    names.push($(this).get(0).files[i].name);
                }
                var _payId = $("#merchantPayId").text();
                $("#uploadFilePayId").val(_payId);
                var _form = $("#fileUpload")[0];
                var  data = new FormData(_form);
                var _getFileName = _filePath.replace("C:\\fakepath\\", "");
                var _getPeriodPos= _filePath.lastIndexOf(".");
                var _getExtension = _filePath.slice(_getPeriodPos);
                data.append("fileName", names);
                if(_getExtension == ".png" || _getExtension == ".pdf" || _getExtension == ".xls" || _getExtension == ".xlsx" || _getExtension == ".csv" || _getExtension == ".jpg"){
                    $(".upload-pic").removeClass("upload-denied");
                    $(".upload-pic").addClass("upload-success");
                    if(_file < 3000000){
                        $.ajax({
                            type: "post",
                            enctype: "multipart/form-data",
                            url: "uploadStatusFileAction",
                            data: data,
                            processData: false,
                            contentType: false,
                            success: function(data){
                                $(".upload-pic").removeClass("upload-denied");
                                $(".upload-pic").addClass("upload-success");
                                $(".upload-text").text(names);
                            }
                        })
                    }else{
                        $(".upload-pic").removeClass("upload-success");
                        $(".upload-pic").addClass("upload-denied");
                        $(".upload-text").text("file size should not greater then 2mb");
                    }
                }else{
                    $(".upload-pic").removeClass("upload-success");
                    $(".upload-pic").addClass("upload-denied");
                    $(".upload-text").text("file format dose not match");
                }
            }

            $("#uploadDoc").on("change", uploadFunc);

            // send status 
            function sendStatus(){
                var _payId = $("#merchantPayId").text();
                var _status = $("#Editchecker").val();
                var _statusComment = $("#statusComment").val();
                 if(_statusComment == ""){
                    _statusComment = null;
                }
               // return false;
                if(_status != "-1"){
                    $("body").removeClass("loader--inactive");
                    $.ajax({
                        type: "post",
                        url: "updateMPAStatusAction",
                        data: { "token": _token, "payId": _payId, "merchantStatus": _status, "statusComment": _statusComment },
                        success: function(data){
                            $(".merchantAssing-response").addClass("success-response");
                            $("body").addClass("loader--inactive");
                            location.reload(true);
                        },
                        error: function(data){
                            $(".merchantAssing-response").addClass("error-response");
                        }
                    });
                }else{
                     alert("Status should not blank");
                }
            };

            $("#mpaSaveStatus").on("click", sendStatus);

            function getMerchnatData(){
                var _payId = $("#merchantPayId").text();
                $.ajax({
                    type: "post",
                    url: "getAllMerchantData",
                    data: { "payId": _payId, "token": _token },
                    success: function(data){
                        for(key in data.mpaDataByPayId){
                            if(data.mpaDataByPayId[key] != null){
                                $("#"+key).closest(".merchantDetail-box").removeClass("d-none");
                                $("#"+key).text(data.mpaDataByPayId[key]);
                            }else{
                                // $("#"+key).closest(".mpa-detail-box").addClass("d-none");
                            }
                        }
                    }
                })
            }
            // merchant details 
           
            getMerchnatData();
        })
    </script>
</body>
</html>