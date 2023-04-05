<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix = "s" uri = "/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>Dispatch Details</title>
    <script type="text/javascript" src="../js/jquery.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/bootstrap-select.min.js"></script>
</head>
<body>
<section class="upload_bulk_user lapy_section white-bg box-shadow-box mt-70 p20">
    <div class="row">
        <div class="col-md-9">
            <div class="heading_with_icon mb-30">
                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                <h2 class="heading_text">Upload Dispatch Slip</h2>
            </div>
            <!-- /.heading_icon -->
        </div>
        <!-- /.col-md-12 -->
        <div class="col-md-3 mb-20">
            <s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}"></s:textfield>
            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                <div class="lpay_select_group">
                    <s:select
                        name="merchantPayId"
                        class="selectpicker"
                        id="merchantPayId"
                        headerKey=""
                        data-live-search="true"
                        headerValue="Select Merchant"
                        list="merchantList"
                        listKey="payId"
                        listValue="businessName"
                        autocomplete="off"
                    />
                </div>
            </s:if>
            <s:if test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
                <div class="lpay_select_group" >
                    <s:select
                        name="merchantPayId"
                        class="selectpicker"
                        id="merchantPayId"
                        list="merchantList"
                        data-live-search="true"
                        listKey="payId"
                        listValue="businessName"
                        autocomplete="off"
                    />
                </div>
            </s:if>
        </div>
        <!-- /.col-3 -->
        <!-- /.col-md-12 -->
        <div class="col-md-4">
            <form action="saveDispatchSlip" id="saveDispatchSlip" method="post" enctype="multipart/form-data">
                <input type="hidden" name="payId" data-id="payId">
                <input type="hidden" name="fileName" id="realFileName">
                <label for="upload-input" class="lpay-upload">
                    <input type="file" name="csvfile" id="upload-input" class="lpay_upload_input">
                    <div class="default-upload">
                        <h3>Upload Your CSV File</h3>
                        <h5 class="mt-10">File size: <b>2 MB</b></h5>
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
                                    <div id="fileName" class="wrong-size">File size too Long.</div>
                                    <div id="fileName" class="wrong-format">Please upload only csv file</div>
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
                <button class="lpay_button lpay_button-md lpay_button-secondary" disabled id="dispatchSubmit">Submit</button>
                <!-- create table for download csv format -->
                <table id="example" class="display nowrap" style="display: none;">
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Invoice No. (Provided by Merchant)</th>
                            <th>Courier Name</th>
                            <th>Dispatch Slip No.</th>
                        </tr>
                    </thead>
                </table>
            </div>
            <!-- /.button-wrapper -->
        </div>
        <!-- /.col-md-12 -->
    </div>
    <!-- /.row -->
</section>

<div class="row">
    <s:if test="count != null && count != 0">
        
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Total Number of Process Data</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12 text-center lpay_xl">
                            <s:property value="count"/>
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- col-md-6 -->
    </s:if>

    <s:if test="incorrectOrderId != null && incorrectOrderId != 0">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Total Number of Incorrect Order Id</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12 text-center lpay_xl">
                            <s:property value="incorrectOrderId"/>
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- col-md-6 -->
    </s:if>

    <s:if test="blankLine != null && blankLine != 0">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Total Number of Blank Lines</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12 text-center lpay_xl">
                            <s:property value="blankLine"/>
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- col-md-6 -->
    </s:if>

    <s:if test="rowCount != null && rowCount != 0">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Total Number of Lines</h2>
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
            </div>
            <!-- col-md-6 -->
    </s:if>


    <s:if test="count != null && count == 0 && validHeader == true">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">CSV File is Empty</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <!-- <div class="col-md-12 text-center lpay_xl">
                            <s:property value="count"/>
                        </div> -->
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- col-md-6 -->
    </s:if>
    
    <s:if test="validHeader == false">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Invalid File Format Of CSV</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <!-- <div class="col-md-12 text-center lpay_xl">
                            <s:property value="validHeader"/>
                        </div> -->
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- col-md-6 -->
    </s:if>
    

    <s:if test="invalid != null && invalid != 0">
            <div class="col-md-6 text-center">
                <section class="lapy_section white-bg box-shadow-box mt-70 p20">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="heading_with_icon mb-10">
                                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                                <h2 class="heading_text">Total Number of Failed</h2>
                            </div>
                            <!-- /.heading_icon -->
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12 text-center lpay_xl">
                            <s:property value="invalid"/>
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </section>
            </div>
            <!-- col-md-6 -->
    </s:if>
    </div>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
<script src="../js/dispatch-script.js"></script>
</body>
</html> 