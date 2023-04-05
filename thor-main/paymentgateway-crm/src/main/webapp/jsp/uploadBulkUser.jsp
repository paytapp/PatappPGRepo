<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix = "s" uri = "/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>Upload Bulk Users</title>
    <script type="text/javascript" src="../js/jquery.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
    <style>
        .d-flex { display: flex; }
        .justify-content-center { justify-content: center !important;}
        .justify-content-right { justify-content: right !important; }
        .justify-content-right { justify-content: left !important; }

        .mt-20 { margin-top: 20px; }

        .px-10 {
            padding-left: 10px !important;
            padding-right: 10px !important;
        }

        .py-5 {
            padding-top: 5px !important;
            padding-bottom: 5px !important;
        }

        .py-10 {
            padding-top: 10px !important;
            padding-bottom: 10px !important;
        }

        .pt-20 { padding-top: 20px !important; }

        .mb-10 { margin-bottom: 10px !important; }

        .mx-5{
            margin-left: 5px !important;
            margin-right: 5px !important;
        }
        
        .font-size-14 { font-size: 14px !important; }

       

        td, th { padding: 5px !important; }

        table { border-collapse: collapse; width: 100%;}

        tr:not(:last-child) {
            border-bottom: 1px solid #ccc;
        }

        .dataTables_wrapper { margin-top: 0 !important; }

        .border-left{ border-left: 1px solid #ccc !important;}

        #response{ position: absolute;z-index: -1; }
    </style>
</head>
<body>
<div id="response"><s:property value="%{response}"></s:property></div>

            <s:if test="wrongCsv!= null">
        <s:if test="wrongCsv!= 0">
            <section class="upload_bulk_user lapy_section white-bg box-shadow-box mt-70 p20">
                <div class="row">
                    <div class="col-md-12">
            <div class="row">
                <div class="mb-20 col-xs-12 text-center ">
                <h3 class="text-white p-2 mb-2 bg-danger text-white h10 py-10 font-size-14">Wrong File Format</h3>
                </div>
            </div>
        </div>
        <!-- /.col-md-12 -->
        
    </div>
    <!-- /.row -->
</section>
        </s:if>
    </s:if>
        
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
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
<script src="../js/upload-bulk-user.js"></script>
</body>
</html>