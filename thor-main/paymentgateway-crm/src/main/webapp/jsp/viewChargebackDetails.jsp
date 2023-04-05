<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.text.SimpleDateFormat, java.util.Calendar, java.text.DateFormat"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Chargeback</title>
    <link rel="stylesheet" href="../css/view-chargeback.css">
    <script src="../js/jquery.min.js"></script>  
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
    <script src="../js/jquery.fancybox.min.js"></script>  

    <style>
        .d-flex { display: flex !important; }
        .d-block { display: block !important; }
        .flex-wrap { flex-wrap: wrap !important; }
        .vh-70 { height: calc(100% - 70px) !important; }
        .vh-35 { height: calc(100% - 35px) !important; }
        .bg-white { background-color: white !important; }
        .p-20 { padding: 20px !important; }
        .p-0 { padding-top: 0 !important; }
        .px-0 {
            padding-left: 0 !important;
            padding-right: 0 !important;
        }

        .lpay-textarea {            
            height: 100px !important;
            resize: none;
        }

        .h-185 { height: 185px !important; }
        .text-index-0 { text-indent: 0 !important; }
        .p-10 { padding: 10px !important; }
        .comment-list {
            padding: 15px;
            border: 1px solid #ddd;
            width: 100%;
            overflow-y: scroll;
            height: 150px;
        }
        .comment-list .lpay_table_wrapper:not(:last-child) {
            border-bottom: 1px solid #ddd;
            padding-bottom: 10px;
            margin-bottom: 10px;
            border-radius: 0 !important;
        }
        .position-absolute { position: absolute !important; }
        .position-relative { position: relative !important; }
        .font-size-14 { font-size: 14px !important; }
        .wrap-text { white-space: normal !important; }
        .line-height-20 { line-height: 20px !important; }
        .align-items-center { align-items: center !important; }
        .btn.active.focus, .btn.active:focus, .btn.focus, .btn:active.focus, .btn:active:focus, .btn:focus {
            outline: none;
            outline-offset: unset;
        }
        .w-100 { width: 100% !important; }
        .text-right { text-align: right !important; }
	    .font-size-12 { font-size: 12px !important; }
        .text-center { text-align: center !important; }
        .text-danger { color: red !important; }
        .mt-5 { margin-top: 5px !important; }
        .mt-15 { margin-top: 15px !important; }
        .lpay_custom_table tbody td {
            white-space: unset !important;
        }
        #reloadChargebackDetails tbody td {
            padding: 10px 0 !important;
        }
        .chargebackBack{
            position: absolute !important;
            top: 0;
            right: 0;
        }
        .chargebackBack:focus{
            color: #fff
        }
    </style>
</head>
<body>
    <div id="response"></div>
    <s:form id="files" method="post" enctype="multipart/form-data">
        <div class="row d-flex flex-wrap">
            <div class="col-md-12">
                <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Chargeback Status</h2>
                            </div>
                            <!-- /.heading_with_icon mb-30 -->
				<s:a action="viewChargeback" class="lpay_button lpay_button-md lpay_button-secondary chargebackBack">Back</s:a>

                        </div>
                        <!-- /.col-md-12 -->                        
                    </div>
                    <!-- /.row -->
                    <div class="row">
                        <div class="col-md-12 d-flex align-items-center">
                            <h2 class="font-size-14">Case ID : <s:property value="chargeback.caseId" /> - <s:property value="chargeback.chargebackStatus" /> </h2>
                            <div id="status-btn-box">
                                <s:textfield type="hidden" name="targetDate" id="targetDate" value="%{chargeback.targetDate}" />
                                <s:textfield type="hidden" name="userType" id="chargebackUserType" value="%{#session.USER.UserType.name()}" />
                                <s:textfield type="hidden" id="isSuperMerchant" value="%{#session['USER'].superMerchant}" />
                                <s:textfield type="hidden" id="superMerchantId" value="%{#session['USER'].superMerchantId}" />
                                <s:if test="%{chargeback.chargebackStatus == 'New'}">
                                    <input type="submit" id="btnAccept" name="btnAccept" value="Accept Chargeback" onclick="updateStatus(this, event)" class="btn btn-success btn-md accept-chargeback" style="margin-left: 17px;">
                                    <input type="button" value="Reject Chargeback" id="btnReject" name="btnReject" onclick="updateStatus(this, event)" class="btn btn-danger btn-md accept-chargeback" style="margin-left: 17px;">
                                </s:if>
                            </div>
                            <!-- /.col-md-12 -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Chargeback Details</h2>
                            </div>
                            <!-- /.heading_with_icon mb-30 -->
                        </div>
                        <!-- /.col-md-12 -->

                        <div class="col-md-12">
                            <div class="lpay_table_wrapper">
                                <table class="lpay_custom_table" width="100%">
                                    <tbody>
                                        <tr>
                                            <td>Case ID:</td>
                                            <td>
                                                <s:if test="%{chargeback.caseId != null}">
                                                    <s:property value="chargeback.caseId" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                                
                                                <s:textfield type="hidden" name="caseId" id="caseId" value="%{chargeback.caseId}" />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Chargeback Status:</td>
                                            <td>
                                                <s:if test="%{chargeback.chargebackStatus != null}">
                                                    <s:property value="chargeback.chargebackStatus" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Chargeback Type:</td>
                                            <td>
                                                <s:if test="%{chargeback.chargebackType != null}">
                                                    <s:property value="chargeback.chargebackType" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Target Date:</td>
                                            <!-- <td class="date-field target-date"> -->
                                            <td>
                                                <s:if test="%{chargeback.targetDate != null}">
                                                    <s:property value="chargeback.targetDate" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Last Updated Date:</td>
                                            <!-- <td class="date-field"> -->
                                            <td>
                                                <s:if test="%{chargeback.updateDateString != null}">
                                                    <s:property value="chargeback.updateDateString" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                                <!-- /.lpay_custom_table -->
                            </div>
                            <!-- /.lpay_table_wrapper -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
                <!-- /.lapy_section white-bg box-shadow-box mt-70 p-20 -->
            </div>
            <!-- /.col-md-6 -->

            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Order Details</h2>
                            </div>
                            <!-- /.heading_with_icon mb-30 -->
                        </div>
                        <!-- /.col-md-12 -->

                        <div class="col-md-12">
                            <div class="lpay_table_wrapper">
                                <table class="lpay_custom_table" width="100%">
                                    <tbody>
                                        <tr>
                                            <td>Order ID:</td>
                                            <td>
                                                <s:if test="%{chargeback.orderId !=null}">
                                                    <s:property value="chargeback.orderId" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Date:</td>
                                            <!-- <td class="date-field"> -->
                                            <td>
                                                <s:if test="%{chargeback.createDateString !=null}">
                                                    <s:property value="chargeback.createDateString" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Merchant Name:</td>
                                            <td>
                                                <s:if test="%{chargeback.businessName !=null}">
                                                    <s:property value="chargeback.businessName" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <s:if test="%{chargeback.subMerchantName !=null}">
                                            <tr>
                                                <td>Sub-Merchant Name:</td>
                                                <td>
                                                    <s:property value="chargeback.subMerchantName" />
                                                </td>
                                            </tr>
                                        </s:if>
                                        <tr>
                                            <td>Card Number Mask:</td>
                                            <td>
                                                <s:if test="%{chargeback.cardNumber !=null}">
                                                    <s:property value="chargeback.cardNumber" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Payment Method:</td>
                                            <td>
                                                <s:if test="%{chargeback.paymentType != null && chargeback.mopType != null}">
                                                    <s:property value="chargeback.paymentType" /> (<s:property value="chargeback.mopType" />)
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Card Issuer Info:</td>
                                            <td>
                                                <s:if test="%{chargeback.internalCardIssusserBank !=null}">
                                                    <s:property value="chargeback.internalCardIssusserBank" />                                        
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Customer Email:</td>
                                            <td>
                                                <s:if test="%{chargeback.custEmail !=null}">
                                                    <s:property value="chargeback.custEmail" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                                <!-- /.lpay_custom_table -->
                            </div>
                            <!-- /.lpay_table_wrapper -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
                <!-- /.lapy_section white-bg box-shadow-box mt-70 p-20 -->
            </div>
            <!-- /.col-md-6 -->

            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Account Summary</h2>
                            </div>
                            <!-- /.heading_with_icon mb-30 -->
                        </div>
                        <!-- /.col-md-12 -->

                        <div class="col-md-12">
                            <div class="lpay_table_wrapper">
                                <table class="lpay_custom_table" width="100%">
                                    <tbody>
                                        <tr>
                                            <td>Currency:</td>
                                            <td>
                                                <s:if test="%{chargeback.currencyNameCode != null}">
                                                    <s:property value="chargeback.currencyNameCode" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Sale Amount:</td>
                                            <td>
                                                <s:if test="%{chargeback.authorizedAmount != null}">
                                                    <s:property value="chargeback.authorizedAmount" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>TDR (<s:property value="chargeback.merchantTDR" />% of B + D) [F]</td>
                                            <td>
                                                <s:if test="%{chargeback.merchantTDR != null}">
                                                    <s:property value="chargeback.merchantTDR" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Refunded Amount</td>
                                            <td>
                                                <s:if test="%{chargeback.refundedAmount != null}">
                                                    <s:property value="chargeback.refundedAmount" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Available for Refund:</td>
                                            <td>
                                                <s:if test="%{chargeback.refundAvailable != null}">
                                                    <s:property value="chargeback.refundAvailable" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Total Chargeback Amount:</td>
                                            <td>
                                                <s:if test="%{chargeback.totalchargebackAmount != null}">
                                                    <s:property value="chargeback.totalchargebackAmount" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                                <!-- /.lpay_custom_table -->
                            </div>
                            <!-- /.lpay_table_wrapper -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
                <!-- /.lapy_section white-bg box-shadow-box mt-70 p-20 -->
            </div>
            <!-- /.col-md-6 -->

            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">History Details</h2>
                            </div>
                            <!-- /.heading_with_icon mb-30 -->
                        </div>
                        <!-- /.col-md-12 -->

                        <div class="col-md-12">
                            <div class="lpay_table_wrapper">
                                <table class="lpay_custom_table" width="100%">
                                    <tbody>
                                        <tr>
                                            <td>Chargeback Type:</td>
                                            <td>
                                                <s:if test="%{chargeback.chargebackType != null}">
                                                    <s:property value="chargeback.chargebackType" />
                                                </s:if>
                                                <s:else>Not applicable</s:else>

                                                <s:textfield
                                                    type="hidden"
                                                    name="subject"
                                                    value="%{chargeback.chargebackType}"
                                                    id="subject"
                                                    autocomplete="off"
                                                />
                                            </td>
                                        </tr>
                                        <s:if test="%{!chargeback.fileName == ''}">
                                            <tr>
                                                <td>Download File:</td>
                                                <td>
                                                    <a
                                                        href="#"
                                                        data-id="downloadChargeback"
                                                        class="download-file"
                                                        data-action="chargebackdownload"
                                                        data-btnname="downloadChargeback"
                                                        data-filename="<s:property value="%{chargeback.fileName}" />"
                                                        data-document="<s:property value="%{chargeback.documentId}" />">
                                                        <img src="../image/icon-download.png" height="30">
                                                    </a>
                                                </td>
                                            </tr>
                                        </s:if>
                                        <tr>
                                            <td>Message Body:</td>
                                            <td class="lpay_input_group wrap-text line-height-20">
                                                <s:property value="%{commentsString}" />
                                                <s:hidden name="subject" cols="10" rows="6" value="%{commentsString.replace(\"\n\", \" <br /> \")}" id="subject" class="lpay_input lpay-textarea" readonly="true" autocomplete="off" />
                                            </td>
                                        </tr>                                    
                                    </tbody>
                                </table>
                                <!-- /.lpay_custom_table -->
                            </div>
                            <!-- /.lpay_table_wrapper -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
                <!-- /.lapy_section white-bg box-shadow-box mt-70 p-20 -->
            </div>
            <!-- /.col-md-6 -->

            <s:if test="%{#session.USER.UserType.name() == 'ADMIN' || #session.USER.UserType.name() == 'SUBADMIN' || #session.USER.UserType.name() == 'MERCHANT' || #session.USER.UserType.name() == 'SUBUSER'}">
                <div class="col-md-6">
                    <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="heading_with_icon mb-30">
                                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                    <h2 class="heading_text">Status</h2>
                                </div>
                                <!-- /.heading_with_icon mb-30 -->
                            </div>
                            <!-- /.col-md-12 -->
        
                            <div class="col-md-12">
                                <div class="lpay_table_wrapper">
                                    <table id="reloadChargebackDetails" class="lpay_custom_table" width="100%">
                                        <thead>
                                            <tr>
                                                <th>Date</th>
                                                <th>Merchant Status</th>
                                                <th>Admin Status</th>
                                                <th>Action</th>
                                            </tr>
                                        </thead>
                                        <tbody></tbody>
                                    </table>
                                    <!-- /.lpay_custom_table -->
                                </div>
                                <!-- /.lpay_table_wrapper -->
                            </div>
                            <!-- /.col-md-12 -->
                        </div>
                        <!-- /.row -->
                    </section>
                    <!-- /.lapy_section white-bg box-shadow-box mt-70 p-20 -->
                </div>
                <!-- /.col-md-6 -->
            </s:if>

            <div class="col-md-6">
                <section class="lapy_section white-bg box-shadow-box mt-70 p-20 vh-35">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-30">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Add Details</h2>
                            </div>
                            <!-- /.heading_with_icon mb-30 -->
                        </div>
                        <!-- /.col-md-12 -->

                        <div class="col-md-12">
                            <div class="position-relative">
                                <div class="lpay_input_group">
                                    <label for="">Comment List</label>
                                    <div class="comment-list">
                                        <s:iterator value="commentList">
                                            <div class="lpay_table_wrapper">
                                                <table class="lpay_custom_table" width="100%">
                                                    <tr>
                                                        <td class="position-relative wrap-text pt-0 px-0" style="padding: 0 0 10px 0 !important;">
                                                            <span style="color:#47a447;"><s:property value="commentSenderEmailId"/></span>
                                                        
                                                            <script>
                                                                var documentId = '<s:property value="documentId" />';
                                                                var imageFileName = '<s:property value="imageFileName" />';
                                                                
                                                                if(imageFileName !== '' && imageFileName !== null) {                                                        
                                                                    document.write('<a href="#" class="download-file position-absolute top-2 right-10" data-btnname="CommentFile" data-action="chargebackfiledownload" data-filename="'+ imageFileName +'" data-id="'+ documentId +'" data-document="'+ documentId +'" title="Download File"><img src="../image/icon-download-flat.png" height="22" /></a>');
                                                                }
                                                            </script>
                                                        </td>
                                                    </tr>
                
                                                    <tr>
                                                        <td class="wrap-text">
                                                            <div style="word-break: break-all;">
                                                                <span style="color:Black;"><s:property value="commentBody"/></span>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    
                                                    <tr>
                                                        <td class="wrap-text text-right" style="padding: 10px 0 0 0 !important;">
                                                            <span style="font-size:10px;"><s:property value="commentcreateDate"/></span>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </s:iterator>
                                    </div>
                                    <!-- /.comment-list -->
                                </div>
                                <!-- /.lpay_input_group -->
                            </div>
                            <!-- /.position-relative -->
                        </div>
                        <!-- /.col-md-12 -->

                        <div class="col-md-6 mt-10">
                            <div class="lpay_input_group">
                                <label for="">Add Comment <span class="text-danger">*</span></label>
                                <s:textarea id="commentId" class="lpay_input lpay-textarea h-185 text-index-0 p-10" />
                                <span class="text-danger font-size-12 d-block w-100 text-right invisible" id="error-commentId">This field is required.</span>
                                <input type="hidden" name="comment" id="commentPost">
                            </div>
                        </div>
                        <!-- /.col-md-6 -->

                        <div class="col-md-6 mt-15">
                            <label for="upload-input" class="lpay-upload">
                                <input type="file" accept=".csv, .pdf" name="image" id="upload-input" class="lpay_upload_input w-100" multiple="true">
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

                            <!-- <div class="position-relative">
                                <div class="attachment position-relative">
                                    <s:file name="image" id="image" onchange="validateFileUpload({that: this, maxLimit: 5})" multiple="true"/>
                                    <span class="text-danger invisible d-inline-block error-filename">Please choose *.PDF or *.CSV file format</span>
                                </div>
                            </div> -->

                            <!-- /.position-relative -->
                        </div>
                        <!-- /.col-md-6 -->

                        <div class="col-md-12">
                            <input type="submit" id="btnSave" name="btnSave" class="lpay_button lpay_button-md lpay_button-secondary" value="Submit">
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
                <!-- /.lapy_section white-bg box-shadow-box mt-70 p-20 -->
            </div>
            <!-- /.col-md-6 -->
        </div>
        <!-- /.row -->
        <input type="hidden" name="token" id="tokenId">
    </s:form>

    <div class="lpay_popup_confirm"  id="fancybox">
        <div class="lpay_popup_confirm_box text-center">
            <div class="lpay_popup_box_icon">
                <span class="lpay_popup_icon">!</span>
            </div>
            <!-- /.confirm-box-icon -->
            <div class="lpay_confirm_delete_text">
                <h3>Are you sure ?</h3>
                <span>Do you really want to perform this action?</span>
            </div>
            <!-- /.confirm-delete-text -->
            <div class="confirm-delete-button">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn">No</button>
                <button  class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">Yes</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
    <!-- /.confrim-popup -->

    <s:form method="POST" id="downloadFile" action="">
        <s:textfield type="hidden" name="payId" value="%{chargeback.payId}" />
        <s:textfield type="hidden" name="caseId" value="%{chargeback.caseId}" />    
        <s:textfield type="hidden" name="documentId" />
        <s:textfield type="hidden" name="imageName" />
        <s:textfield type="hidden" name="downloadBtnName" />
        <s:textfield type="hidden" name="token" value="%{#session.customToken}" />
    </s:form>

    <s:form name="manualRefundProcessChargeback" id="manualRefundProcessChargeback" action="manualRefundProcessChargeback">
        <s:hidden name="caseId" id="caseId" value="%{chargeback.caseId}" />
        <s:hidden name="payId" id="payId" value="%{chargeback.payId}" />
        <s:hidden name="pgRefNum" id="pg-ref" value="%{chargeback.pgRefNum}" />
        <s:hidden name="refundedAmount" id="refundedAmount" value="%{chargeback.refundedAmount}" />
        <s:hidden name="refundAvailable" id="refundAvailable" value="%{chargeback.refundAvailable}" />
		<s:hidden name="chargebackAmount" id="chargebackAmount" value="%{chargeback.chargebackAmount}" />
        <s:hidden name="token" value="%{#session.customToken}" />
    </s:form>

    <script>




        function updateStatus(that, e) {
            e.preventDefault();
            var _conform = confirm("Do you really want to perform this action ?");
            if(_conform){

                var targetDate = $("#targetDate").val(),
                    userType = $("#chargebackUserType").val(),
                    btnName = that.getAttribute("name"),
                    msg = "",
                    adminStatus = "",
                    merchantStatus = "",
                    isSuperMerchant = $("#isSuperMerchant").val(),
                    superMerchantId = $("#superMerchantId").val();
                
                userType = userType.toLowerCase();
    
                if(isSuperMerchant == "false" && superMerchantId !== "") {
                    
                    userType = "SubMerchant";
                } else if(isSuperMerchant == "true") {
                    userType = "SuperMerchant";
                }
        
                if(btnName == "btnAccept") {
                    msg = "Accepted by "+ userType;
                } else if(btnName == "btnReject") {
                    msg = "Rejected by "+ userType;
                }else if(btnName == "btnClose") {
                    msg = "Closed by "+ userType;
                }
        
                if(userType == "merchant") {
                    merchantStatus = msg;
                } else if(userType == "subuser") {
                    merchantStatus = msg;
                } else {
                    adminStatus = msg;
                }
    
                if(msg !== "") {
                    $.ajax({
                        url : 'updateStatusAction',
                        type : 'post',
                        timeout: 0,
                        data : {
                            caseId : '<s:property value="chargeback.caseId" />',
                            capturedAmount: '<s:property value="chargeback.capturedAmount" />',
                            authorizedAmount: '<s:property value="chargeback.authorizedAmount" />',
                            commentedBy: '<s:property value="chargeback.commentedBy" />',
                            chargebackType: '<s:property value="chargeback.chargebackType" />',
                            payId: '<s:property value="chargeback.payId" />',
                            transactionId: '<s:property value="chargeback.transactionId" />',
                            custEmail: '<s:property value="chargeback.custEmail" />',
                            currencyNameCode: '<s:property value="chargeback.currencyNameCode" />',
                            chargebackAmount: '<s:property value="chargeback.chargebackAmount" />',
                            amount: '<s:property value="chargeback.amount" />',
                            pgRefNum:'<s:property value="chargeback.pgRefNum"/>',
                            orderId:'<s:property value="chargeback.orderId"/>',
                            status:'<s:property value="chargeback.status"/>',
                            
                            chargebackStatus : msg,
                            targetDate : targetDate,
                            actionStatus : 'ACTIVE',
                            merchantStatus : merchantStatus,
                            adminStatus : adminStatus
                            //token : token
                        },
                        success : function(data) {
                            if(data.chargebackAmount !== undefined) {
                                alert(data.chargebackStatus);
                                //alert("Details added successfully.");
                                 location.reload();
                                //  document.getElementById("saveMessage").innerHTML="Details added successfully.";
                            } else {
                                alert("Try again, Something went wrong!");
                                return false;
                            }
                        },
                        error : function(data) {
                            alert("Something went wrong, so please try again.");
                            return false;
                        }
                    });
                }
            }
    
        }


    </script>
    
    <script src="../js/view-chargeback.js"></script>
</body>
</html>