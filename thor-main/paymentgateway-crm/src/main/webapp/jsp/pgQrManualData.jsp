<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>PgQr Manual Data Entry</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/jquery.fancybox.min.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script src="../js/dataTables.buttons.js"></script>

    <style>
        .lp-refund_success{ margin-bottom: 10px;font-size: 11px;font-weight: 500;color: #2bb743; }
        .lp-refund_error{ margin-bottom: 10px;font-size: 11px;font-weight: 500;color:  #d20e0e; }
    </style>
  
</head>
<body>
	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">PgQr Manual Data Entry</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-4">
                <s:if test="%{response == 'success'}">
                    <div class="refund_response lp-refund_success">
                        <s:property value="%{responseMsg}" />
                    </div>
                    <!-- /.lp-refund_success -->
                </s:if>
                <s:if test="%{response == 'error'}">
                    <div class="refund_response lp-refund_error">
                        <s:property value="%{responseMsg}" />
                    </div>
                    <!-- /.lp-refund_success -->
                    </s:if>
                <form action="pgQrManualEntryAction" id="addBulkUsers" method="post" enctype="multipart/form-data">
                    <label for="upload-input" class="lpay-upload">
                        <input type="file" name="csvFile" accept=".csv" id="upload-input" class="lpay_upload_input">
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
                                    <h3>Uploaded Successfully</h3>
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
                    <div class="button-wrapper lpay-center mt-20">
                        <button class="lpay_button lpay_button-md lpay_button-secondary" disabled id="bulkUpdateSubmit">Submit</button>
                        <!-- create table for download csv format -->
                        <table id="example" class="display nowrap" style="display: none;">
                            <thead>
    
                                <tr>
                                    <th>merchantTranID</th>
                                    <th>bankTranID</th>
                                    <th>date</th>
                                    <th>time</th>
                                    <th>amount</th>
                                </tr>
                                
                            </thead>
                        </table>
                    </div>
                    <!-- /.button-wrapper -->
                </form>
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
    </section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    
    <script src="../js/downloadSearch-scripts.js"></script>
    <script type='text/javascript'>
        $(document).ready(function(e){
            setTimeout(function(e){
                $(".refund_response").fadeOut();
            }, 3000)
        })
    </script>
</body>
</html>