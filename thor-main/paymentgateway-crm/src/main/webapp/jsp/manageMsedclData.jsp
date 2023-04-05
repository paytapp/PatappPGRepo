<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix = "s" uri = "/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>MSEDCL Data</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/common-style.css">
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<link rel="stylesheet" href="../css/invoice.css"/>
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>

	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/dataTables.buttons.min.js"></script>
	<script src="../js/buttons.flash.min.js"></script>
	<script src="../js/jszip.min.js"></script>
	<script src="../js/buttons.print.min.js"></script>
	<script src="../js/buttons.html5.min.js"></script>
	
	<style>		
		.d-flex { display: flex; }
		.justify-content-center { justify-content: center !important;}
		.justify-content-end { justify-content: flex-end !important; }
		.justify-content-start { justify-content: flex-start !important; }
		.justify-content-between { justify-content: space-between !important; }
		.justify-content-around { justify-content: space-around !important; }
		.justify-content-even { justify-content: space-evenly !important; }
		.d-block{display: block;}
		.text-center{text-align: center !important;}
		.mt-20 { margin-top: 20px; }
		.pos-r{position: relative;}
		.px-10 {
			padding-left: 10px !important;
			padding-right: 10px !important;
		}
		

		.csv-file{
			width: 100%;
			height: 120px;
			position: absolute;
			top: 0px;
			opacity: 0;
			cursor: pointer;
		}

		.file-error {
			color: #f00;
			margin-top: 9px;
			display: inline-block;
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
	
		.buttons-csv {
			background-color: #002163 !important;
			display: inline-block;
			padding: 8px 20px;
			font-size: 12px !important;
			line-height: 1.42857143;
			color: #fff;
			border: 1px solid #ccc;
			border-radius: 4px;
			cursor: pointer;
		}
		.buttons-csv:hover { color: #fff;text-decoration: none; }
		
		.font-size-14 { font-size: 14px !important; }
		table, th, td {
			border: 1px solid #ccc;
			text-align: center !important;
		}
		td, th { padding: 5px !important; }
		td:not(:last-child), th:not(:last-child) { border-right: 1px solid #ccc; }
		table {
			border-collapse: collapse;
			width: 100%;
		}
		tr:not(:last-child) {
			border-bottom: 1px solid #ccc;
		}
		
		.error-filename {
			position: absolute;
			top: 23px;
			left: 0;
			font-size: 12px;
		}

		.csv-upload-file {
			width: 300px;
			margin: auto;
			background-color: #ddd;
			height: 120px;
			border-radius: 4px;
			border: 1px solid #ccc;
			align-items: center;
			flex-direction: column;
			cursor: pointer;
		}

		.csv-upload-file i{
			font-size: 40px;
			margin-bottom: 8px;
		}

		.download-btn {
			background-color: #002163 !important;
			display: inline-block;
			padding: 8px 20px;
			font-size: 12px !important;
			line-height: 1.42857143;
			color: #fff;
			border: 1px solid #ccc;
			border-radius: 4px;
		}
			
		.dataTables_wrapper { margin-top: 0 !important; }
		.border-left{ border-left: 1px solid #ccc !important;}

		#example_wrapper button {
			background-size: 200% auto;
			background-image: linear-gradient(to right, #050c16 0%, #002663 51%, #050c16 100%);
			color: #fff;
			font-size: 10px !important;
			border-radius: 5px !important;
		}
		#example_wrapper button:hover {
			background-position: right center;
			text-decoration: none !important;
			color: #fff;
		}
	</style>
</head>
<body>
	<section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20">
		<s:if test="%{#session['USER'].superMerchant == true}">
			<input type="hidden" id="isSuperMerchant" value="Y" />
		</s:if>
		<s:else>
			<input type="hidden" id="isSuperMerchant" />
		</s:else>
		<div class="row">
			<div class="col-md-9" data-target="subMerchant">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">MSEDCL Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<s:form action="msedclDataUploadAction" method="post" enctype="multipart/form-data">
				<div class="col-md-6">
					<label for="upload-input" class="lpay-upload">
						<input type="file" name="csvFile" accept=".csv, .xlsx" id="upload-input" class="lpay_upload_input bulk-invoice">
						<div class="default-upload">
							<h3>Upload Your csv or xlsx File</h3>
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
					<input type="hidden" class="hidden" id="hideFields" name="fileName"/> 
					<div class="button-wrapper mt-20 d-flex justify-content-center text-center">
						<button id="bulkSubmit" class="lpay_button lpay_button-md lpay_button-secondary" disabled>Submit</button>
						<button id="downloadFile" class="lpay_button lpay_button-md lpay_button-primary">Download</button>
					</div>
					<!-- /.button-wrapper -->
				</div>
				<!-- /.col-md-4 -->
			</s:form>
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:if test="(wrongCsv != null && wrongCsv != 0) || (fileIsEmpty != null && fileIsEmpty != 0) || (rowCount != null && rowCount != 0 && rowCount != -1)">
		<div class="row" id="showData">
			<s:if test="wrongCsv != null && wrongCsv != 0">
				<div class="col-md-12">
					<section class="bulk-invoice lpay_section white-bg box-shadow-box mt-20 p20">
						<div class="row">
							<div class="col-md-12 text-center">
								<h3 class="text-snackbar-danger bg-snackbar-danger py-30 font-size-14 font-weight-medium m-0">Wrong File Format</h3>
							</div>
						</div>
					</section>
				</div>
				<!-- /.col-md-12 -->
			</s:if>

			<s:if test="fileIsEmpty != null && fileIsEmpty != 0">
				<div class="col-md-12">
					<section class="bulk-invoice lpay_section white-bg box-shadow-box mt-20 p20">
						<div class="row">
							<div class="col-md-12 text-center">
								<h3 class="text-snackbar-danger bg-snackbar-danger py-30 font-size-14 font-weight-medium m-0">File is Empty</h3>
							</div>
						</div>
					</section>
				</div>
				<!-- /.col-md-12 -->
			</s:if>

			<s:if test="rowCount != null && rowCount != 0 && rowCount != -1">
				<div class="col-md-6">
					<section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20">
						<div class="row">
							<div class="col-md-12">
								<div class="heading_with_icon mb-30">
									<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
									<h2 class="heading_text">Total numbers of data in file</h2>
								</div>
								<!-- /.heading_icon -->
							</div>
							<!-- /.col-md-12 -->
							<div class="col-md-12 text-center lpay_xl">
								<s:property value="rowCount"/>
							</div>
							<!-- /.col-md-12 -->
						</div>
					</section>
				</div>
				<!-- /.col-md-6 -->
			</s:if>
		</div>
		<!-- /.row -->
	</s:if>
	
	<s:form method="POST" action="msedclDataDownloadAction" id="downloadFileForm"></s:form>

	<script>
		function triggerValidation(el) {
			var getInputVal = $("#upload-input").val();
			var getNewFileName = getInputVal.replace("C:\\fakepath\\", "");
			$("#upload-file-name").html(getNewFileName);
			document.getElementById("hideFields").value = getNewFileName;
		}
		
		$(document).ready(function() {			
			$(".bulk-invoice").on("change", function(e){
				var _fileChange = $("#upload-input").val();
				
				triggerValidation(this);
				
				if(_fileChange == "") {
					$("#bulkSubmit").attr("disabled", true);
				}else{
					$("#bulkSubmit").attr("disabled", false);
				}
			});

			$("#bulkSubmit").on("click", function() {
				$("body").removeClass("loader--inactive");
			});

			$("#downloadFile").on("click", function(e) {
				e.preventDefault();

				$("#downloadFileForm").submit();
			});

			$(".lpay_upload_input").on("change", function(e) {
				var _val = $(this).val();
				if(_val != ""){
					var _fileSize = $(this)[0].files[0].size;
					var _tmpName = _val.replace("C:\\fakepath\\", "");
					$("body").removeClass("loader--inactive");
					$(".default-upload").addClass("d-none");
					$("#placeholder_img").css({"display":"none"});
					if(_fileSize < 2000000){
						$(this).closest("label").attr("data-status", "success-status");
						$("#fileName").text(_tmpName);
						$("#bulkUpdateSubmit").attr("disabled", false);
						setTimeout(function(e){
							$("body").addClass("loader--inactive");
						}, 500);
					}else{
						$(this).closest("label").attr("data-status", "error-status");
						$("#bulkUpdateSubmit").attr("disabled", true);
						setTimeout(function(e){
							$("body").addClass("loader--inactive");
						}, 500);
					}
				}
			});
		});
	</script>
</body>
</html>	