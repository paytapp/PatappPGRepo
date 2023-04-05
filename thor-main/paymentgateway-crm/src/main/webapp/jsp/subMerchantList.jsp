<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
	<title>Sub Merchant List</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
	<script src="../js/jquery.min.js" type="text/javascript"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
	<script src="../js/pdfmake.js" type="text/javascript"></script>
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>

	<script type="text/javascript">	
		$(document).ready(function() {
			$(function() {			
				renderTable();
			});

			$("#submit").click(function(env) {
				$("body").removeClass("loader--inactive");
				reloadTable();
				setTimeout(function(e){
					$("body").addClass("loader--inactive");
				}, 1000);
			});	
		});

		function renderTable() {
			var table = new $.fn.dataTable.Api('#txnResultDataTable');
		
			var token = document.getElementsByName("token")[0].value;

			var buttonCommon = {
				exportOptions : {
					format : {
						body : function(data, column, row, node) {
							// Strip $ from salary column to make it numeric
							return column === 0 ? "'" + data : (column === 1 ? "'" + data : data);
						}
					}
				}
			};

			$('#txnResultDataTable').dataTable({
				"footerCallback" : function(row, data, start, end, display) {
					var api = this.api(), data;
				},
				"columnDefs" : [ {
					className : "dt-body-right",
					"targets" : [ 0,1, 2, 3]
				} ],
				dom : 'BTrftlpi',
				buttons : [
					$.extend(true, {}, buttonCommon, {
						extend : 'copyHtml5',
						exportOptions : {
							columns : [ 0,1, 2, 3]
						},
					}),
					$.extend(true, {}, buttonCommon, {
						extend : 'csvHtml5',
						title : 'SubMerchList',
						exportOptions : {

							columns : [ 0,1, 2, 3]
						},
					}),
					{
						extend : 'pdfHtml5',
						orientation : 'landscape',
						pageSize : 'legal',
						//footer : true,
						title : 'SubMerchList',
						exportOptions : {
							columns : [ 0,1, 2, 3]
						},
						customize : function(doc) {
							doc.defaultStyle.alignment = 'center';
							doc.styles.tableHeader.alignment = 'center';
						}
					},
					{
						extend : 'print',
						//footer : true,
						title : 'SubMerchList',
						exportOptions : {
							columns : [ 0,1, 2, 3]
						}
					},
					{
						extend : 'colvis',
						columns : [ 0,1, 2, 3]
					}
				],

				"ajax" : {

					"url" : "subMerchantSearchAction",
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
						"targets" : [ 0, 1, 2, 3]
					},
					{
						'targets': 0,
						'createdCell':  function (td, cellData, rowData, row, col) {
							
						}
					}
				],
					
				"fnDrawCallback" : function() {				
					$("#submit").removeAttr("disabled");
					hideColumn();
				},
				"paginationType" : "full_numbers",
				"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
				"order" : [ [ 2, "desc" ] ],
				"columns" : [
					{
						"data" : "superMerchantName"
					},
					{
						"data" : "businessName",
						"className" : "txnType"
					},
					{
						"data" : "payId",
						"className" : "status",
						"class":"payId"									
					},
					{
						"data" : "emailId",
						"className" : "status",
						"class":"emailId"									
					},
					{
						"data" : "mobile"
					},
					{
						"data" : "updatedBy",
						"className" : "status",
						"class":"updatedBy"									
					},
					{
						"data" : "updationDate",
						"className" : "status",
						"class":"updationDate"									
					},
					{
						"data" : "status"
					},							
					{
						"data" : null,
						"orderable" : false,
						"mRender" : function(row) {
							return '<button class="lpay_button lapy_button-md lpay_button-secondary btnChargeBack"  onClick = "editSubMerch(this)">Edit</button>';
						}
					},
				]
			});
		}

		function reloadTable() {
			$("#submit").attr("disabled", true);
			var tableObj = $('#txnResultDataTable');
			var table = tableObj.DataTable();
			table.ajax.reload();	
		}

		function hideColumn(){
			
			var _table = $("#txnResultDataTable").DataTable();
			var _resellerMerchant = $("#resellerMerchantSignupFlag").val();

			if(_resellerMerchant == "true"){
				_table.columns(8).visible(false);
			}
			// console.log(_getActive);
    }

		function generatePostData(d) {
			var token = document.getElementsByName("token")[0].value;
			var superMerchant = document.getElementById("merchant").value;
			var subMerchantEmail = document.getElementById("subMerchantEmail").value;
			var mobile = document.getElementById("mobile").value;
			var status = document.getElementById("status").value;

			if (superMerchant == '') {
				superMerchant = 'ALL'
			}
			if (subMerchantEmail == '') {
				subMerchantEmail = 'ALL'
			}
			if (mobile == '') {
				mobile = 'ALL'
			}
			if (status == '') {
				status = 'ALL'
			}		

			var obj = {
				superMerchant : superMerchant,
				subMerchantEmail : subMerchantEmail,
				mobile : mobile,
				status : status,
				draw : d.draw,
				length : d.length,
				start : d.start,
				token : token,
				"struts.token.name" : "token",
			};

			return obj;
		}

		function editSubMerch(val) {
			$("body").removeClass("loader--inactive");
			var payId = $(val).closest("tr").find(".payId").text();
			document.getElementById('payId').value = payId;
			document.getElementById('subMerchEditFrm').submit();	
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
		tr td.my_class {
			cursor: pointer;
		}

		tr td.my_class:hover {
			cursor: pointer !important;
		}

		tr th.my_class:hover {
			color: #fff !important;
		}

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

		table.dataTable thead th {
			padding: 10px 5px !important;
			white-space: nowrap;
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

		#mainTable{
			table-layout: fixed;
		}

		#txnResultDataTable_wrapper{
			overflow-y: auto;			
		}

		.mt-20{
			margin-top: 20px !Important;
		}

		.booking-error{
			position: absolute;
			bottom: -16px;
			left: 10px;
		}
		.lpay_table { white-space: nowrap; }
		.lpay_table .lpay_table_head th {
			padding-left: 10px !important;
			padding-right: 10px !important;
		}
		.lpay_table .lpay_button { margin: 0 !important; }

		.lpay_table .dataTables_filter{
			display: block !important;
		}

	</style>
</head>
<body id="mainBody">
	<input type="hidden" id="setGlobalData">
	<s:hidden id="resellerMerchantSignupFlag" value="%{#session['USER'].resellerMerchantSignupFlag}"></s:hidden>

	<section class="sub-merchant-list lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Sub Merchant List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Super Merchant</label>
					<s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
						<s:select
							name="merchant"
							class="selectpicker adminMerchants"
							id="merchant"
							headerKey=""
							headerValue="ALL"
							list="merchantList"
							listKey="superMerchantId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:if>
					<s:elseif test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<s:select
							name="merchant"
							class="selectpicker adminMerchants"
							id="merchant"
							headerKey=""
							headerValue="ALL"
							list="merchantList"
							listKey="superMerchantId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:elseif>
					<s:else>
						<s:select
							name="merchant"
							class="selectpicker adminMerchants"
							id="merchant"
							list="merchantList"
							listKey="superMerchantId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Status</label>
					<s:select
						headerKey=""
						headerValue="All"
						class="selectpicker"
						list="#{'ACTIVE':'ACTIVE','PENDING':'PENDING','TRANSACTION_BLOCKED':'TRANSACTION_BLOCKED','SUSPENDED':'SUSPENDED','TERMINATED':'TERMINATED'}"
						name="status"
						id="status"
						value="name"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Sub Merchant Email</label>
					<s:textfield
						id="subMerchantEmail"
						class="lpay_input"
						name="subMerchantEmail"
						type="text"
						value=""
						autocomplete="off"
						onblur="validateEmail(this);">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Sub Merchant Mobile</label>
					<s:textfield
						id="mobile"
						class="lpay_input"
						name="mobile"
						type="text"
						value=""
						maxlength="10"
						autocomplete="off">
					</s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 text-center -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="sub-merchant lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Sub Merchant List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th>Super Merchant</th>
								<th>Sub Merchant</th>
								<th>Pay Id</th>
								<th>Email Id</th>
								<th>Mobile</th>
								<th>Updated By</th>
								<th>Updation Date</th>
								<th>Status</th>
								<th>Action</th>
							</tr>
						</thead>
					</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:form name="subMerchEditFrm" id="subMerchEditFrm" action="editSubMerchCallAction">
		<s:hidden name="payId" id="payId" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
</body>
</html>