<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Fee Detail Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all"
	href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>

<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
<!--  loader scripts -->



<script type="text/javascript">
	
	$(document).ready(function() {

		$(".blank-space").on("change", function(e){
			var _this = $(this).val();
			$(this).val(_this.trim());
		})

		$(function() {
			renderTable();
		});

		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			
			reloadTable();
			
		});

		

		$(function() {
			var datepick = $.datepicker;
			var table = $('#txnResultDataTable').DataTable();
			$('#txnResultDataTable').on('click', 'td.my_class', function() {
				var rowIndex = table.cell(this).index().row;
				var rowData = table.row(rowIndex).data();

				popup(rowData.oId);
			});
		});
	});

	function renderTable() {
		var merchantEmailId = document.getElementById("merchant").value;
		var table = new $.fn.dataTable.Api('#txnResultDataTable');

		var transFrom = $.datepicker
				.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transFrom == null || transTo == null) {
			alert('Enter date value');
			return false;
		}

		if (transFrom > transTo) {
			alert('From date must be before the to date');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		if (transTo - transFrom > 31 * 86400000) {
			alert('No. of days can not be more than 31');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		var token = document.getElementsByName("token")[0].value;

		var buttonCommon = {
			exportOptions : {
				format : {
					body : function(data, column, row, node) {
						// Strip $ from salary column to make it numeric
						return column === 0 ? "'" + data : (column === 1 ? "'"
								+ data : data);
					}
				}
			}
		};

		$('#txnResultDataTable').dataTable({
			"footerCallback" : function(row, data, start, end, display) {
				var api = this.api(), data;

				// Remove the formatting to get integer data for summation
				var intVal = function(i) {
					return typeof i === 'string' ? i.replace(
							/[\,]/g, '') * 1
							: typeof i === 'number' ? i : 0;
				};

				// Total over all pages
				total = api.column(14).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(14, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(14).footer()).html(
						'' + pageTotal.toFixed(2) + ' ' + ' ');
			
				// Total over all pages
				total = api.column(15).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(15, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(15).footer()).html('' + pageTotal.toFixed(2) + ' ' + ' ');

				// Total over all pages
				total = api.column(16).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(16, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(16).footer()).html(
						'' + pageTotal.toFixed(2) + ' ' + ' ');

						// Total over all pages
				total = api.column(17).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(17, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(17).footer()).html(
						'' + pageTotal.toFixed(2) + ' ' + ' ');

				// Total over all pages
				total = api.column(18).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(18, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);

				// Update footer
				$(api.column(18).footer()).html(
						'' + pageTotal.toFixed(2) + ' ' + ' ');
			},
							"columnDefs" : [ {
								className : "dt-body-right",
								"targets" : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
							} ],
							dom : 'BTrftlpi',
							buttons : [
									$.extend(true, {}, buttonCommon, {
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
										},
									}),
									$.extend(true, {}, buttonCommon, {
										extend : 'csvHtml5',
										title : 'Fee_Detail_Report',
										exportOptions : {

											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
										},
									}),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize : 'legal',
										//footer : true,
										title : 'Fee_Detail_Report',
										exportOptions : {
											columns : [0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
										},
										customize : function(doc) {
											doc.defaultStyle.alignment = 'center';
											doc.styles.tableHeader.alignment = 'center';
										}
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Fee_Detail_Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
										}
									},
									{
										extend : 'colvis',
										columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
									} ],

							"ajax" : {

								"url" : "viewFeeDetailReport",
								"type" : "POST",
								"data" : function(d) {
									return generatePostData(d);
								}
							},
							"columnDefs" : [
								 {
								"type" : "html-num-fmt",
								"targets" : 4,
								"orderable" : true,
								"targets" : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14,15,16,17,18]
							},
								{
								'targets': 0,
								'createdCell':  function (td, cellData, rowData, row, col) {
									$("#setGlobalData").val(rowData["glocalFlag"]);
								}}
								],
								
							"fnDrawCallback" : function() {
								
								$("#submit").removeAttr("disabled");
								$("body").addClass("loader--inactive");
							},
							 "searching" : false,
							"ordering" : false,

							"processing" : true,
							"serverSide" : true,
							"paginationType" : "full_numbers",
							"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
							"order" : [ [ 2, "desc" ] ],

							

							"columns" : [
									{
										"data" : "id",
										"className" : "txnId text-class",
										"width" : "60px !important"
									},
									{
										"data" : "PG_REF_NUM",
										"className" : "payId text-class"

									},
									{
										"data" : "SCHOOL",
										"className" : "text-class"
									},
									{
										"data" : "CREATE_DATE",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "ORDER_ID",
										"className" : "orderId text-class"
									},
									{
										"data" : "PAYMENT_TYPE",
										"className" : "orderId text-class"
									},
									{
										"data" : "MOP_TYPE",
										"className" : "orderId text-class"
									},
									{
										"data" : "CARD_MASK",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "STUDENT_NAME",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "FATHER_NAME",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "MOBILE",
										"className" : "txnType text-class"
									},
									{
										"data" : "TXNTYPE",
										"className" : "status text-class"
									},
									{
										"data" : "STATUS",
										"className" : "text-class"

									},{
										"data" : "AMOUNT",
										"className" : "text-class"
								
									},{
										"data" : "TOTAL_TDR_SC",
										"className" : "text-class"								
									},
									{
										"data" : "TOTAL_GST",
										"className" : "text-class"
									},
									{
										"data" : "TOTAL_1",
										"className" : "text-class"
									},

									{
										"data" : "TOTAL_2",
										"className" : "text-class"
									},

									{
										"data" : "TOTAL_AMOUNT",
										"className" : "text-class"
									}]
						});

	}

	function reloadTable() {
		var datepick = $.datepicker;
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());

		if (transFrom == null || transTo == null) {
			alert('Enter date value');
			return false;
		}

		if (transFrom > transTo) {
			alert('From date must be before the to date');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		if (transTo - transFrom > 31 * 86400000) {
			alert('No. of days can not be more than 31');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		$("#submit").attr("disabled", true);
		var tableObj = $('#txnResultDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
		setTimeout(function(data){

		}, 500);
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var merchant = document.getElementById("merchant").value;
		var pgRefNum = document.getElementById("pgRefNum").value;
		var orderId = document.getElementById("orderId").value;
		var regNo = document.getElementById("regNo").value;
		var mobile = document.getElementById("mobile").value;
		var paymentMethod = document.getElementById("paymentMethod").value;

		if (merchant == '') {
			merchant = 'ALL'
		}
		if (pgRefNum == '') {
			pgRefNum = 'ALL'
		}
		if (orderId == '') {
			orderId = 'ALL'
		}
		if (regNo == '') {
			regNo = 'ALL'
		}
		if (mobile == '') {
			mobile = 'ALL'
		}
		if (paymentMethod == '') {
			paymentMethod = 'ALL'
		}
		var obj = {
			merchant : merchant,
			pgRefNum : pgRefNum,
			orderId : orderId,
			regNo : regNo,
			mobile : mobile,
			paymentMethod : paymentMethod,
			dateFrom : document.getElementById("dateFrom").value,
			dateTo : document.getElementById("dateTo").value,
			draw : d.draw,
			length : d.length,
			start : d.start,
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}

</script>

<script>

	function validPhoneNumber(){
		var _custMobile = document.querySelector("#custMobile").value;
		if(_custMobile != ""){
			if(_custMobile.length == 10){
				document.getElementById("submit").disabled = false;
			}else{
				document.getElementById("submit").disabled = true;
				alert("Please enter mobile number");
			}
		}
	}

	function validPgRefNum() {
		var pgRefValue = document.getElementById("pgRefNum").value;
		var regex = /^[0-9\b]{16}$/;
		if (pgRefValue.trim() != "") {
			if (!regex.test(pgRefValue)) {
				document.getElementById("validValue").style.display = "block";
				document.getElementById("submit").disabled = true;
			} else {
				document.getElementById("submit").disabled = false;
				document.getElementById("validValue").style.display = "none";
			}
		} else {
			document.getElementById("submit").disabled = false;
			document.getElementById("validValue").style.display = "none";
		}
	}
</script>

<style type="text/css">

.form-control{
	width: 100% !important;
	margin-left: 0 !important;
}

.cust {
	width: 24% !important;
	margin: 0 5px !important; /*font: bold 10px arial !important;*/
}

.samefnew {
	width: 24% !important;
	margin: 0 5px !important;
	/*font: bold 10px arial !important;*/
}

.btn {
	padding: 3px 7px !important;
	font-size: 12px !important;
}

.samefnew-btn {
	display: inline-block;
	font: bold 11px arial;
	color: #333;
	line-height: 22px;
	margin-left: 5px;
}
/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/

.cust .form-control, .samefnew .form-control {
	margin: 0px !important;
	width: 100%;
}

.select2-container {
	width: 100% !important;
}

.clearfix:after {
	display: block;
	visibility: hidden;
	line-height: 0;
	height: 0;
	clear: both;
	content: '.';
}

#popup {
	position: fixed;
	top: 0px;
	left: 0px;
	background: rgba(0, 0, 0, 0.7);
	width: 100%;
	height: 100%;
	z-index: 999;
	display: none;
}

.innerpopupDv {
	width: 600px;
	margin: 80px auto;
	background: #fff;
	padding: 3px 10px;
	border-radius: 10px;
}

.btn-custom {
	margin-top: 5px;
	height: 27px;
	border: 1px solid #5e68ab;
	background: #5e68ab;
	padding: 5px;
	font: bold 12px Tahoma;
	color: #fff;
	cursor: pointer;
	border-radius: 5px;
}

#loader-wrapper .loader-section.section-left, #loader-wrapper .loader-section.section-right
	{
	background: rgba(225, 225, 225, 0.6) !important;
	width: 50% !important;
}

.invoicetable {
	float: none;
}

.innerpopupDv h2 {
	font-size: 12px;
	padding: 5px;
}

.text-class {
	white-space: nowrap;
	text-align: center !important;
}

.odd {
	background-color: #e6e6ff !important;
}


#loading {
	width: 100%;
	height: 100%;
	top: 0px;
	left: 0px;
	position: fixed;
	display: block;
	z-index: 99
}

#loading-image {
	position: absolute;
	top: 35%;
	left: 55%;
	z-index: 100;
	width: 10%;
}



.mt-20{
	margin-top: 20px !Important;
}

.booking-error{
	position: absolute;
	bottom: -16px;
	left: 10px;
}

</style>
<script>
	function downloadSubmit() {

		var token = document.getElementsByName("token")[0].value;
		var regNo = document.getElementById("regNo").value;
		var pgRefNum = document.getElementById("pgRefNum").value;
		var orderId = document.getElementById("orderId").value;
		var mobile = document.getElementById("mobile").value;
		var merchant = document.getElementById("merchant").value;
		var paymentMethod = document.getElementById("paymentMethod").value;
		var dateFrom = document.getElementById("dateFrom").value;
		var dateTo = document.getElementById("dateTo").value;
	
		if (regNo == '') {
			regNo = 'ALL'
		}
		if (pgRefNum == '') {
			pgRefNum = 'ALL'
		}
		if (orderId == '') {
			orderId = 'ALL'
		}
		if (mobile == '') {
			mobile = 'ALL'
		}	
		if (merchant == '') {
			merchant = 'ALL'
		}	
		
		if (paymentMethod == '') {
			paymentMethod = 'ALL'
		}
	
		document.getElementById("regNoForm").value = regNo;
		document.getElementById("pgRefNumForm").value = pgRefNum;
		document.getElementById("orderIdForm").value = orderId;
		document.getElementById("mobileForm").value = mobile;
		document.getElementById("merchantForm").value =  merchant;
		document.getElementById("paymentMethodForm").value = paymentMethod;
		document.getElementById("dateFromForm").value =  dateFrom;
		document.getElementById("dateToForm").value =  dateTo;
		document.getElementById("feeDetailsDownloadForm").submit();
		
	}
</script>

</head>
<body id="mainBody">

	<input type="hidden" id="setGlobalData">

	<section class="fee-detail lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Fee Details Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
				<label for="">PG REF Number</label>
				<s:textfield id="pgRefNum" class="lpay_input blank-space" name="pgRefNum"
				type="text" value="" autocomplete="off"
				onkeypress="javascript:return isNumber (event)" maxlength="16"></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
				<label for="">Order ID</label>
				<s:textfield id="orderId" class="lpay_input blank-space" name="orderId"
				type="text" value="" autocomplete="off"
				onkeypress="return Validate(event);"
				onblur="this.value=removeSpaces(this.value);"></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
				<label for="">Registration Number</label>
				<s:textfield id="regNo" class="lpay_input blank-space" name="regNo"
				type="text" value="" autocomplete="off"
				onkeypress="return Validate(event);"
				></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
				<label for="">Mobile</label>
				<s:textfield id="mobile" class="lpay_input blank-space"
				name="mobile" type="text" value="" autocomplete="off"
				></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<label for="">Merchant</label>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
						<s:select name="merchant" class="selectpicker adminMerchants"
							id="merchant" headerKey="" headerValue="ALL"
							list="merchantList" listKey="payId"
							listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
						<s:select name="merchant" class="selectpicker" id="merchant"
							list="merchantList" listKey="payId"
							listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<label for="">Payment Method</label>
					<s:select headerKey="" headerValue="ALL" class="selectpicker"
					list="@com.paymentgateway.commons.util.PaymentType@values()"
					listValue="name" listKey="code" name="paymentMethod"
					id="paymentMethod" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" id="dateFrom" name="dateFrom"
				class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" id="dateTo" name="dateTo"
					class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
				<input type="button" id="downloadSubmit" value="Download" onclick = "downloadSubmit()" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="fee-detail lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Fee Detail Captured</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="" cellspacing="0"
							width="100%">
							<thead class="lpay_table_head">
								<tr>
									<th style='text-align: center; text-decoration: none !important;'>Txn Id</th>
									<th style='text-align: center'>Pg Ref Num</th>
									<th style='text-align: center'>Merchant</th>
									<th style='text-align: center'>Date</th>
									<th style='text-align: center'>Order Id</th>
									<th style='text-align: center'>Payment Method</th>
									<th style='text-align: center'>Mop Type</th>
									<th style='text-align: center'>Card Mask</th>
									<th style='text-align: center'>Student Name</th>
									<th style='text-align: center'>Father Name</th>
									<th style='text-align: center'>Mobile</th>
									<th style='text-align: center'>Txn Type</th>
									<th style='text-align: center'>Status</th>
									<th style='text-align: center'>Base Amount</th>
									<th style='text-align: center'>TDR / Surcharge</th>
									<th style='text-align: center'>GST</th>
									<th style='text-align: center'>Total Part 1</th>
									<th style='text-align: center'>Total Part 2</th>
									<th style='text-align: center'>Total Amount</th>
								</tr>
							</thead>
							<tfoot class="lpay_table_head">
								<tr>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
									<th></th>
								</tr>
							</tfoot>
						</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->



	<script type="text/javascript">
		$(document).ready(function() {
			$('#closeBtn').click(function() {
				$('#popup').hide();
			});
		});
	</script>

	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}
	</script>


	<s:form name="feeDetailsDownloadForm" id="feeDetailsDownloadForm" action="downloadFeeDetails">
		<s:hidden name="regNo" id="regNoForm" value="" />
		<s:hidden name="pgRefNum" id="pgRefNumForm" value="" />
		<s:hidden name="orderId" id="orderIdForm" value="" />
		<s:hidden name="mobile" id="mobileForm" value="" />
		<s:hidden name="merchant" id="merchantForm" value="" />
		<s:hidden name="paymentMethod" id="paymentMethodForm" value="" />
		<s:hidden name="dateFrom" id="dateFromForm" value="" />
		<s:hidden name="dateTo" id="dateToForm" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
</body>
</html>