<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix = "s" uri = "/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Bulk Invoice</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/common-style.css">
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<link rel="stylesheet" href="../css/invoice.css"/>
	<!-- <script src="../js/jquery.dataTables.js"></script> -->
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>

	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/dataTables.buttons.min.js"></script>
	<script src="../js/buttons.flash.min.js"></script>
	<script src="../js/jszip.min.js"></script>
	<script src="../js/buttons.print.min.js"></script>
	<script src="../js/buttons.html5.min.js"></script>
	<!-- <script type="text/javascript" src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script> -->
	<!-- <script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/dataTables.buttons.min.js"></script> -->
	<!-- <script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.flash.min.js"></script> -->
	<!-- <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.1.3/jszip.min.js"></script> -->
	<!-- <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/pdfmake.min.js"></script>
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/vfs_fonts.js"></script> -->
	<!-- <script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.print.min.js"></script> -->
	<!-- <script type="text/javascript" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.html5.min.js"></script> -->

	<link rel="stylesheet" href="../css/bootstrap-flex.css">
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

	<script>
		var regex = new RegExp("(.*?)\.(csv)$");		

		function triggerValidation(el) {
			var getInputVal = $("#upload-input").val();
			var getNewFileName = getInputVal.replace("C:\\fakepath\\", "");
			$("#upload-file-name").html(getNewFileName);
			document.getElementById("hideFields").value = getNewFileName;

			// if (!(regex.test(el.value.toLowerCase()))) {
			// 	el.value = '';
			// 	// alert('');
			// 	$("#upload-file-name").html("Upload your csv file here");
			// 	$(".file-error").show();
			// 	$(".file-error").html("Please select correct file format");
			// }else{
			// 	var getInputVal = $("#csvFile").val();
			// 	var getNewFileName = getInputVal.replace("C:\\fakepath\\", "");
			// 	$("#upload-file-name").html(getNewFileName);
			// 	$(".file-error").hide();
			// 	document.getElementById("upload-file").value = getNewFileName;
			// 	var getExactVal = getNewFileName.replace(".csv", "");
			// 	document.getElementById("hideFields").value = getExactVal;
				
			// }
		}
		
		$(document).ready(function() {
			$("#merchantPayId").selectpicker();
			$("#merchantPayId").selectpicker("refresh");
			$(".bulk-invoice").on("change", function(e){
				var _merchant = $("#merchantPayId").val();
				var _fileChange = $("#upload-input").val();
				var _getRow = $("[data-id=submerchant]").hasClass("d-none");
				var _isSuperMerchant = $("#isSuperMerchant").val();
				var _getSubMerchant = $("#subMerchant").val();
				console.log(_getRow);
				triggerValidation(this);
				
				if(_merchant == "" || _fileChange == "" || (_isSuperMerchant == "Y" && _getSubMerchant == "")){
					$("#bulkSubmit").attr("disabled", true);
				}else{
					$("#bulkSubmit").attr("disabled", false);
				}
			});

			$("#bulkSubmit").on("click", function() {
				$("body").removeClass("loader--inactive");
			});

			$(".lpay_upload_input").on("change", function(e){
				var _val = $(this).val();
				var _fileSize = $(this)[0].files[0].size;
				var _tmpName = _val.replace("C:\\fakepath\\", "");
				if(_val != ""){
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


			$('#download-xls').DataTable( {
				dom: 'B',
				buttons: [
				{
					extend: 'excel',
					text: 'Download XLSX Format',
					className: 'lpay_button lpay_button-md lpay_button-primary',
					title: '',
				}
				]
			});

			// 		$('#download-xls').DataTable( {
			//     dom: 'Bfrtip',
			//     buttons: [
			//         'copy', 'csv', 'excel', 'pdf', 'print'
			//     ]
			// } );
			
			$('#example').DataTable( {
				dom: 'B',
				buttons: [
				{
					extend: 'csv',
					text: 'Failed Data CSV',
					exportOptions: {
						modifier: {
							search: 'none'
						}
					}
				}
				]
			});
			$("#merchantPayId").on('change', function() {
				getSubMerchant();
			});
				
			$('#download-format').DataTable( {
				dom: 'B',
				buttons: [
				{
					extend: 'csv',
					text: 'Download CSV Format',
					className: 'lpay_button lpay_button-md lpay_button-primary',
					exportOptions: {
						modifier: {
							search: 'none'
						}
					}
				}
				]
			});			
		});	
		
		function getSubMerchant(){
			var _merchant = $("#merchantPayId").val();
			if(_merchant != ""){
				$("body").removeClass("loader--inactive");
				$.ajax({
					type: "POST",
					url: "getSubMerchantListByPayId",
					data: {"payId": _merchant},
					success: function(data){
						$("#subMerchant").html("");
						if(data.superMerchant == true){
							$("#isSuperMerchant").val("Y");
							var _option = $("#subMerchant").append("<option value=''>Select Sub Merchant</option>");
							for(var i = 0; i < data.subMerchantList.length; i++){
								_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
							}
							$("[data-target=subMerchant]").attr("class", "col-md-6");
							$("[data-id=submerchant]").removeClass("d-none");
							$("#subMerchant option[value='']").attr("selected", "selected");
							$("#subMerchant").selectpicker();
							$("#subMerchant").selectpicker("refresh");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
						}else{
							$("#isSuperMerchant").val("N");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-target=subMerchant]").attr("class", "col-md-9");
							$("[data-id=submerchant]").addClass("d-none");
							$("#subMerchant").val("");
						}
					}
				});
			}else{
				$("#isSuperMerchant").val("N");
				$("[data-target=subMerchant]").attr("class", "col-md-9");
				$("[data-id=submerchant]").addClass("d-none");
				$("#subMerchant").val("");	
			}
	}
	

	</script>
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
					<h2 class="heading_text">Uplaod Bulk Invoice</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<s:form action="savebulkInvoice" method="post" enctype="multipart/form-data">
			<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
				<div class="col-md-3">
					<div class="lpay_select_group">
						<input type="hidden" name="upload-file-name" id="upload-file">
						<s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}"></s:textfield>
						<div class="position-relative">
							<s:select
								name="merchantPayId"
								class="selectpicker bulk-invoice"
								id="merchantPayId"
								headerKey=""
								data-live-search="true"
								headerValue="Select Any Merchant"
								list="merchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
							<span id="merchantPayIdErr" class="error"></span>
						</div>
					</div>
				</div>
			</s:if>
			<s:if test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
				<div class="col-md-3 d-none">
					<div class="lpay_select_group">
						<input type="hidden" name="upload-file-name" id="upload-file">
						<s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}"></s:textfield>
						<div class="position-relative">
							<s:select
								name="merchantPayId"
								class="selectpicker bulk-invoice"
								id="merchantPayId"
								list="merchantList"
								data-live-search="true"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
							<span id="merchantPayIdErr" class="error"></span>
						</div>
					</div>
				</div>
			</s:if>
			<!-- /.col-3 -->
			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
						<!-- <label for="">Sub Merchant</label> -->
						<s:select
							data-id="subMerchant"
							name="subMerchantId"
							headerKey=""
							headerValue="Select Sub Merchant"
							class="selectpicker textFL_merch"
							id="subMerchant"
							list="subMerchantList"
							listKey="payId"
							listValue="businessName"
							autocomplete="off"
						/>
					</div>
					<!-- /.lpay_select_group -->
				</div>
				<!-- /.col-md-3 -->
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-id="submerchant"> 
					<div class="lpay_select_group">
						<!-- <label for="">Sub Merchant</label> -->
						<select name="subMerchantId" id="subMerchant" class="textFL_merch"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>

				<div class="clearfix"></div>
				<!-- /.clearfix -->
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
					<div class="button-wrapper mt-20 d-flex flex-wrap justify-content-center text-center">
						<button id="bulkSubmit" class="lpay_button lpay_button-md lpay_button-secondary" disabled>Submit</button>
						<div class="d-flex mt-10 mt-md-0">
							<table id="download-format" class="display nowrap" style="display: none;">
								<thead>
									<tr>
										<th>Name</th>
										<th>Phone</th>
										<th>Email</th>
										<th>Product Name</th>
										<th>Product Description</th>
										<th>Duration From (DD-MM-YYYY)</th>
										<th>Duration To (DD-MM-YYYY)</th>
										<th>Expiry Date(DD-MM-YYYY)</th>
										<th>Expiry Time(HH:MM)</th>
										<th>Currency Type</th>
										<th>Quantity</th>
										<th>Amount</th>
										<th>Service</th>
										<th>Address</th>
										<th>Country</th>
										<th>State</th>
										<th>City</th>
										<th>PIN</th>
										<th>UDF11</th>
										<th>UDF12</th>
										<th>UDF13</th>
										<th>UDF14</th>
										<th>UDF15</th>
										<th>UDF16</th>
										<th>UDF17</th>
										<th>UDF18</th>
									</tr>
								</thead>
							</table>
							<!-- /.download-btn -->
							<table id="download-xls" class="display nowrap" style="display: none;">
								<thead>
									<tr>
										<th>Name</th>
										<th>Phone</th>
										<th>Email</th>
										<th>Product Name</th>
										<th>Product Description</th>
										<th>Duration From (DD-MM-YYYY)</th>
										<th>Duration To (DD-MM-YYYY)</th>
										<th>Expiry Date(DD-MM-YYYY)</th>
										<th>Expiry Time(HH:MM)</th>
										<th>Currency Type</th>
										<th>Quantity</th>
										<th>Amount</th>
										<th>Service</th>
										<th>Address</th>
										<th>Country</th>
										<th>State</th>
										<th>City</th>
										<th>PIN</th>
										<th>UDF11</th>
										<th>UDF12</th>
										<th>UDF13</th>
										<th>UDF14</th>
										<th>UDF15</th>
										<th>UDF16</th>
										<th>UDF17</th>
										<th>UDF18</th>
									</tr>
								</thead>
							</table>
							<!-- /.download-btn -->
						</div>
					</div>
					<!-- /.button-wrapper -->
				</div>
				<!-- /.col-md-4 -->
				<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
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
					</section>
				</div>
				<!-- /.col-md-6 -->

				<div class="col-md-6">
					<section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20">
						<div class="row">
							<div class="col-md-12">
								<div class="heading_with_icon mb-30">
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
					</section>
				</div>
				<!-- /.col-md-6 -->

				<div class="col-md-6">
					<section class="bulk-invoice lpay_section white-bg box-shadow-box mt-70 p20">
						<div class="row">
							<div class="col-md-12">
								<div class="heading_with_icon mb-30">
									<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
									<h2 class="heading_text">Failed Data</h2>
								</div>
								<!-- /.heading_icon -->
							</div>
							<!-- /.col-md-12 -->
	
							<s:if test="failedList.size != 0">
								<div class="col-md-12">
									<div class="lpay_table_wrapper">
										<table class="lpay_custom_table" style="max-height: 300px; overflow: hidden; overflow-y: scroll;">
											<tr class="lpay_table_head"><th >Row No.</th><th>Field Name</th></tr>
											<s:iterator value="failedListShow">
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

								<div class="col-md-12 mt-10">
									<table id="example" class="display nowrap" style="display: none;">
										<thead>
											<tr>
												<th>Name</th>
												<th>Phone</th>
												<th>Email</th>
												<th>Product Name</th>
												<th>Product Description</th>
												<th>Duration From</th>
												<th>Duration To</th>
												<th>Expiry Date & Time</th>
												<th>Currency Type</th>
												<th>Quantity</th>
												<th>Amount</th>
												<th>Service</th>
												<th>Address</th>
												<th>Country</th>
												<th>State</th>
												<th>City</th>
												<th>PIN</th>
												<th>UDF11</th>
												<th>UDF12</th>
												<th>UDF13</th>
												<th>UDF14</th>
												<th>UDF15</th>
												<th>UDF16</th>
												<th>UDF17</th>
												<th>UDF18</th>
											</tr>
										</thead>
										<tbody>
											<s:iterator value="failedList"> 
												<tr>
													<s:iterator value="value">
														<td><s:property value="%{value.name}"/></td>
														<td><s:property value="%{value.phone}"/></td>
														<td><s:property value="%{value.email}"/></td>
														<td><s:property value="%{value.productName}"/></td>
														<td><s:property value="%{value.productDesc}"/></td>
														<td><s:property value="%{value.durationFrom}"/></td>
														<td><s:property value="%{value.durationTo}"/></td>
														<td><s:property value="%{value.ExpiresDay}"/></td>
														<td>
															<s:set value="%{value.currencyCode}" var="CurrencyVar"/>
															<s:iterator value="currencyMap">
																<s:if test="{key == CurrencyVar}">
																<s:property value="value"/>
																</s:if>
															</s:iterator>															
														</td>
														<td><s:property value="%{value.quantity}"/></td>
														<td><s:property value="%{value.amount}"/></td>
														<td><s:property value="%{value.ServiceCharge}"/></td>
														<td><s:property value="%{value.address}"/></td>
														<td><s:property value="%{value.Country}"/></td>
														<td><s:property value="%{value.State}"/></td>
														<td><s:property value="%{value.City}"/></td>
														<td><s:property value="%{value.zip}"/></td>
														<td><s:property value="%{value.UDF11}"/></td>
														<td><s:property value="%{value.UDF12}"/></td>
														<td><s:property value="%{value.UDF13}"/></td>
														<td><s:property value="%{value.UDF14}"/></td>
														<td><s:property value="%{value.UDF15}"/></td>
														<td><s:property value="%{value.UDF16}"/></td>
														<td><s:property value="%{value.UDF17}"/></td>
														<td><s:property value="%{value.UDF18}"/></td>
													</s:iterator>
												</tr>
											</s:iterator>
										</tbody>
									</table>
								</div>
	
							</s:if>
							<s:else>								
								<div class="col-md-12 text-center lpay_xl">0</div>
								<!-- /.col-md-12 -->
							</s:else>
						</div>
					</section>
				</div>
				<!-- /.col-md-6 -->
			</s:if>
		</div>
		<!-- /.row -->
	</s:if>
	<script type="text/javascript">
		$(document).ready(function(e){
			$("#merchantPayId").selectpicker("val", "");
			$("#merchantPayId").selectpicker("refresh");
		})
	</script>
</body>
</html>	