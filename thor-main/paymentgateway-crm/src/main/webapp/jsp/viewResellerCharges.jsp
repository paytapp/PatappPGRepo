<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View Reseller Charges</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<link rel="stylesheet" href="../css/subAdmin.css">
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
<!--------PDF scripts----->
<style>
#tableData table thead {
	background-color: #002163;
	color: #fff;
	/* white-space: nowrap; */
}

.table-div{
	overflow-x: auto;
	margin-bottom: 20px;
}

.table-div table{
	margin-bottom: 0;
}

.select2-selection {
	height: 43px !important;
}

.select2-container--default .select2-selection--single .select2-selection__arrow
	{
	top: 9px !important;
}

.merchant__form_control {
	font-weight: 400;
}

.select2-container--default .select2-selection--single .select2-selection__rendered
	{
	line-height: 40px !important;
	font-size: 14px;
	font-weight: 400;
}

.primary-btn {
	margin-top: 20px !important;
	padding: 13px 40px 12px;
	position: relative;
	overflow: hidden;
	z-index: 1;
}

.primary-btn:before {
	content: " ";
	width: 50%;
	left: -87px;
	top: -36px;
	background-color: #e60000;
	position: absolute;
	height: 220%;
	z-index: -1;
	transition: all .5s ease;
	transform: rotate(45deg);
}

.primary-btn:after {
	content: " ";
	width: 50%;
	right: -87px;
	bottom: -36px;
	background-color: #e60000;
	position: absolute;
	height: 220%;
	transition: all .5s ease;
	z-index: -1;
	transform: rotate(45deg);
}

span.inner-heading.main-heading {
	font-size: 18px;
	margin-bottom: 10px !important;
}

.primary-btn:hover:before {
	left: 0;
}

.primary-btn:hover:after {
	right: 0;
}

.d-none {
	display: none !important;
}

.download-btn {
	display: flex;
	align-items: center;
	justify-content: flex-start;
	margin-bottom: 10px;
}

.download-btn .primary-btn {
	padding: 8px 20px !important;
	margin-right: 8px !important;
}

.download-btn .primary-btn:after {
	display: none;
}

.download-btn .primary-btn:before {
	display: none;
}

.download-btn .primary-btn {
	margin-top: 0 !important;
}

#tableData table {
	white-space: nowrap;
}

#tableData .inner-heading.main-heading {
	margin-bottom: 15px;
}

#tableData .inner-heading {
	display: inline-block;
	width: 100%;
	margin-bottom: 0;
	background-color: transparent;
	padding: 0;
	margin-bottom: 10px;
}
</style>
</head>
<body>
	<form action="" method="post" style="display: none;"
		id="downloadFileForm">
		<input type="text" value="" name="payId" id="payId"> <input
			type="text" value="" name="resellerId" id="resellerId">
	</form>
	
	<s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>

	<section class="view-reseller lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">View Reseller Charges</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<s:if test="%{#session.USER.UserType.name()!='RESELLER'}">
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Reseller <span class="text-danger">*</span></label>
				   <s:if
					test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<s:select headerValue="Select Reseller" headerKey=""
						name="resellerId" class="selectpicker uper-input" id="resellers"
						list="listReseller" listKey="resellerId" listValue="businessName"
						autocomplete="off" />
				</s:if>
				<s:else>
					<s:select headerValue="Select Reseller" headerKey=""
						name="resellerId" class="selectpicker uper-input" id="resellers"
						list="listReseller" listKey="resellerId" listValue="businessName"
						autocomplete="off" />
				</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			</s:if>
			
			<s:if test="%{#session.USER.UserType.name()!='RESELLER'}">
                <div class="col-md-4 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Merchant</label>
                       <select id="merchant" class="selectpicker">
                           <option value="">Select Merchant</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 mb-20 -->
            </s:if>
            <s:else>
                <div class="col-md-4 mb-20">
                <div class="lpay_select_group" >
                    <label for="">Merchant</label>
					<s:select
						headerValue="Select Merchant" 
						headerKey=""
                        name="merchantPayId"
                        class="selectpicker"
                        id="merchant"
                        list="listMerchant"
                        data-live-search="true"
                        listKey="payId"
                        listValue="businessName"
                        autocomplete="off"
                    />
                </div>
            </div>
            </s:else>
			<div class="col-md-3">
				<button class="lpay_button lpay_button-md lpay_button-secondary lpay_button-with-input" id="submitBtn">Submit</button>
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12">
				<div class="download-btn mt-30 d-none">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="csv-download">XLSX</button>
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="pdf-download">PDF</button>
				</div>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div id="tableData" class="d-none">
					
				</div>
				<!-- /#tableData -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<span class="empty-data d-none mt-30">
					No Rules Found
				</span> <!-- /.noData -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->


	<!-- /.view-surcharge -->

	<script type="text/javascript">
		$(document).ready(function(e) {
			var _userType = $("#userType").val();
							// excel file download action
			$("#csv-download").on(
					"click",
					function(e) {
						var _merchant = $("#merchant").val();
						var _reseller = $("#resellers").val();
						$("#payId").val(_merchant);
						$("#resellerId").val(_reseller);
						$('#downloadFileForm').attr('action',
								'downloadResellerCharges');

						$('#downloadFileForm').submit();
					});

			// pdf download action

			 // Reseller ID
			 $("#resellers").on("change", function(e){
				var _this = $(this).val();
				if(_this != ""){
					$("boby").removeClass("loader--inactive");
					$.ajax({
						type: "post",
						url: "getMerchantListByReseller",
						data: {
							"resellerId": _this
						},
						success: function(data){
							console.log(data);
							$("#merchant").html("");
							if(data.listMerchant.length > 0){
								
								var _option = $("#merchant").append("<option value=''>Select Merchant</option>");
								for(var i = 0; i < data.listMerchant.length; i++){
									_option += $("#merchant").append("<option value="+data.listMerchant[i]["payId"]+">"+data.listMerchant[i]["businessName"]+"</option>")
								}
								$("#merchant").selectpicker("refresh");
								$("#merchant").selectpicker();
								$("boby").addClass("loader--inactive");
							}else{
								var _option = $("#merchant").append("<option value=''>No merchant exist</option>");
								$("#merchant").selectpicker("refresh");
								$("#merchant").selectpicker();
								$("boby").addClass("loader--inactive");
							}
						}
					})
				}
			})


			$("#pdf-download").on(
					"click",
					function(e) {
						var _merchant = $("#merchant").val();
						var _reseller = $("#resellers").val();
						$("#payId").val(_merchant);
						$("#resellerId").val(_reseller);
						$('#downloadFileForm').attr('action',
								'downloadResellerChargesPdf');

						$('#downloadFileForm').submit();

						/* $.ajax({
							type: "post",
							url: "downloadPdfAction",
							success:function(data){
						
							},
							error: function(data){
						
							},
						}) */
					});

			// submit button action 
			$("#submitBtn").on("click", function(e) {
				var _merchant = $("#merchant").val();
				var _reseller = $("#resellers").val();
				var _acquirer = $("#acquirer").val();
				if(_userType == "RESELLER"){
					_reseller = "ALL";
				}
				if (_merchant != "" && _reseller != "") {
					$("body").removeClass("loader--inactive");
					$.ajax({
						type : "post",
						url : "viewResellerChargesAction",
						data : {
							"payId" : _merchant,
							"resellerId" : _reseller,
							"acquirerType" : _acquirer
						},
						success : function(data) {
							$("#tableData").html("");
							$(".noData").addClass("d-none");
							$(".download-btn").removeClass("d-none");
							var table = "";
							// console.log(data.resellerChargesData["acquirer"]);
							if (Object.keys(data.resellerChargesData).length !== 0) {
								for (key in data.resellerChargesData) {
									table += "<span class='inner-heading main-heading'>" + key + "</span>";
									// console.log(data.resellerChargesData[key][0]);
									for (var i = 0; i < data.resellerChargesData[key].length; i++) {
										//console.log(data.resellerChargesData[key][i]);
										for (key2 in data.resellerChargesData[key][i]) {
											table += "<div class='table-div'><table class='table table-bordered'>";
											table += "<span class='inner-heading'>" + key2 + "</span>";
											table += "<thead><tr><th>Currency</th><th>Mop</th><th>Transaction Type</th><th>Slab</th><th>Reseller % </th><th>Reseller FC</th><th>PG % From Reseller</th><th>PG FC From Reseller</th><th>GST</th></tr></thead>";
											// console.log(data.resellerChargesData[key][i][key2].length);

											for (var j = 0; j < data.resellerChargesData[key][i][key2].length; j++) {
												table += "<tr>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["currency"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["mopType"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["transactionType"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["slabId"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["resellerPercentage"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["resellerFixedCharge"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["pgPercentage"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["pgFixedCharge"] + "</td>";
												table += "<td>" + data.resellerChargesData[key][i][key2][j]["gst"] + "</td>";
												table += "</tr>";
											}
											table += "</table></div>";
										}

									}
								}
								$("#tableData").append(table);
								$("#tableData").removeClass("d-none");
								setInterval(function(e) {
									$("body").addClass("loader--inactive");
								},500);
							} else {
								$("#tableData").html("");
								// $("#tableData").addClass("d-none");
								$(".download-btn").addClass("d-none");
								$(".empty-data").removeClass("d-none");
								setInterval(function(e) {
									$("body").addClass("loader--inactive");
								}, 500);
							}
						},
						error : function(data) {
							alert("Something went wrong!");
						}
					});
					} else {
						if(_merchant == ""){
							alert("Please select merchant.");
						}
						if(_reseller == ""){
							alert("Please select reseller.");
						}
					}
				});
		});
	</script>

</body>
</html>