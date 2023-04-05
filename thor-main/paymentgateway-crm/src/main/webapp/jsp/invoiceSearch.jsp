<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Invoice Search</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/messi.js"></script>
	<link href="../css/messi.css" rel="stylesheet" />

	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<script src="../js/common-scripts.js"></script>

	<script type="text/javascript">

		function hideColumn(){
            var _isSuperMerchant = $("#isSuperMerchant").val();
            var _table = $("#invoiceDataTable").DataTable();
            if(_isSuperMerchant == "N"){
                _table.columns(3).visible(false);
            }else{
                _table.columns(3).visible(true);
            }
        }

		$(document).ready(function() {

			$("#searchBtn").on("click", function(e){
				$("body").removeClass("loader--inactive");
				reloadTable();
			})

			renderTable();
						
			var table = $('#invoiceDataTable').DataTable();
			$('#invoiceDataTable tbody').on('click', 'td', function() {
				popup(table, this);			
			});

			function getSubMerchant(){
			var _merchant = $("#merchants").val();
			$("#isSuperMerchant").val("Y");
			if(_merchant != ""){
				$("body").removeClass("loader--inactive");
				$.ajax({
					type: "POST",
					url: "getSubMerchantListByPayId",
					data: {"payId": _merchant},
					success: function(data){
						console.log(data);
						$("#subMerchant").html("");
						if(data.superMerchant == true){
							$("#isSuperMerchant").val("Y");
							var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
							for(var i = 0; i < data.subMerchantList.length; i++){
								_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
							}
							$("[data-id=submerchant]").removeClass("d-none");
							$("#subMerchant option[value='']").attr("selected", "selected");
							$("#subMerchant").selectpicker();
							$("#subMerchant").selectpicker("refresh");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
						}else{
							$("#isSuperMerchant").val("N");
							// handleChange();
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-id=submerchant]").addClass("d-none");
							$("#subMerchant").val("");
						}
					}
				});
			}else{
				
				$("[data-id=submerchant]").addClass("d-none");
				$("#subMerchant").val("");	
			}
	}

	$("#merchants").on('change', function() {
		var _val = $(this).val();
		getSubMerchant();
		if(_val !== "") {
			$("#btnSave").attr('disabled', false);
		} else {
			$("#btnSave").attr('disabled', true);
			$("#currencyCode").removeClass("d-none");
			$("#mappedCurrency").addClass("d-none");
		}
		
		// $('#spanMerchant').hide();
		// $('#currencyCodeloc').hide();
	});
		});

		function renderTable() {
			//to show new loader -Harpreet
			$.ajaxSetup({
				global: false,
				beforeSend: function () {
					toggleAjaxLoader();
				},
				complete: function () {
					toggleAjaxLoader();
				}
			});

			var table = new $.fn.dataTable.Api('#invoiceDataTable');
			$.ajaxSetup({
				global: false,
				beforeSend: function () {
					$(".modal").show();
				},
				complete: function () {
					$(".modal").hide();
				}
			});

			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}
			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}
			var token = document.getElementsByName("token")[0].value;
			
			$('#invoiceDataTable').dataTable({
				language: {
					search: "",
					searchPlaceholder: "Search records"
				},
				dom : 'BTftlpi',
				buttons : [ {
					extend : 'copyHtml5',
					exportOptions : {
						columns : [':visible :not(:last-child)']
					}
				}, {
					extend : 'csvHtml5',
					title : 'Invoice Search',
					exportOptions : {
						columns : [':visible :not(:last-child)']
					}
				}, {
					extend : 'pdfHtml5',
					orientation : 'landscape',
					title : 'Search Transactions',
					exportOptions : {
						columns : [':visible :not(:last-child)']
					},
					customize: function (doc) {
						//doc.content[1].table.widths = Array(doc.content[1].table.body[0].length + 1).join('*').split('');
						doc.defaultStyle.alignment = 'center';
						doc.styles.tableHeader.alignment = 'center';
					}							
				}, {
					extend : 'print',
					title : 'Invoice Search',
					exportOptions : {
						columns : [':visible :not(:last-child)']
					}
				}, {
					extend : 'colvis',
					//           collectionLayout: 'fixed two-column',
					columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
				} ],
				"ajax" : {
					"url" : "invoiceSearchAction",
					"type" : "POST",
					"data" : function (d) {
						return generatePostData(d);
					}
				},
				"fnDrawCallback" : function(settings, json) {
					hideColumn();
					$("body").addClass("loader--inactive");
				},
				"bProcessing" : true,
				"bDestroy" : true,
				"bLengthChange" : true,
				"iDisplayLength" : 10,
				"order" : [[ 1, "desc" ]],
				"aoColumns" : [
					{
						"data" : "invoiceId",
						"sClass" : "my_class",
						"className" : "text-class"
					},
					{
						"data" : "createDate",
						"sWidth" : '15%',
						"className" : "text-class"
						/* "render" : function(
								Data) {
							var date = new Date(
									Data);
							var month = date
									.getMonth() + 1;
							var hour = date.getHours();
							hour = hour.toString();
							var mint = date.getMinutes();
							var sec = date.getSeconds();
							return (date
									.getDate()
									+ "-"
									+ (month.length > 1 ? month
											: "0"
													+ month)
									+ "-" + date
									.getFullYear()+ "T" + hour + ":" + mint + ":" + sec);
						}   */
					},
					{
						"data" : "businessName",
						"sWidth" : '15%',
						"className" : "text-class"
					},
					{"mData" : null,
					"mRender" : function(row){
							if(row.subMerchantbusinessName != null){
								return row.subMerchantbusinessName;
							}else{
								return "<span>NA</span>"
							}
						}
					},
					{
						"data" : "name",
						"sWidth" : '15%',
						"className" : "text-class"
					},
					{
						"data" : "email",
						"sWidth" : '15%',
						"className" : "text-class"
					},
					{
						"data" : "phone",
						"sWidth" : '15%',
						"className" : "text-class"
					},
					/* {
						"data" : "invoiceNo",
						"sWidth" : '10%',
						"className" : "text-class"
					}, */
					{
						"data" : "productName",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "currencyCode",
						"sWidth" : '5%',
						"className" : "text-class"
					},
					{
						"data" : "totalAmount",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF11",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF12",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF13",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF14",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF15",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF16",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF17",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "UDF18",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "invoiceType",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"data" : "status",
						"sWidth" : '10%',
						"className" : "text-class"
					},
					{
						"mData" : null,
						"sClass" : "center",
						"bSortable" : false,
						"mRender" : function(row) {
							if(row.email!=null) {
								if(row.email!='') {
									return '<button class="btn btn-info btn-xs">Email</button>';
								} else {
									return '<button class="btn btn-info btn-xs" disabled>Email</button>';
								}
							} else {
								return '<button class="btn btn-info btn-xs" disabled>Email</button>';
							}
						}
					},
					{
						"mData" : null,
						"sClass" : "center",
						"bSortable" : false,
						"mRender" : function(row) {
							if(row.phone!=null) {
								if(row.phone!='') {
									return '<button class="btn btn-info btn-xs">SMS</button>';
								} else {
									return '<button class="btn btn-info btn-xs" disabled>SMS</button>';
								}
							} else {
								return '<button class="btn btn-info btn-xs" disabled>SMS</button>';
							}
						}
					},
					/* {
						"data" : null,
						"className" : "center",
						"orderable" : false,
						"mRender" : function(row) {
							if (row.invoiceType == "INVOICE PAYMENT"){
								return '<button class="btn btn-info btn-xs">Email</button>';
							}
							else {
								return '<button  hidden="hidden"></button>';
							}
						}
					} */
					{
						"data" : null,
						"className" : "center",
						"orderable" : false,
						"mRender" : function() {
								return '<button class="btn btn-info btn-xs">Download QR</button>';
							}
					},
					{
						"data" : null,
						"sClass" : "my_class",
						"sWidth" : '10%',
						"visible" : false,
						"className" : "displayNone",
						"mRender" : function(row) {
							return "\u0027" + row.invoiceId;
					}
				}, ]
			});
		}
		function reloadTable() {		
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}

			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}

			var tableObj = $('#invoiceDataTable');
			var table = tableObj.DataTable();
			table.ajax.reload();
		}

		function popup(table, index) {
			var rows = table.rows();
			var columnVisible = table.cell(index).index().columnVisible;
			var columnNumber = table.cell(index).index().column;
			var token = document.getElementsByName("token")[0].value;
			var rowIndex = table.cell(index).index().row;
			var invId = table.cell(rowIndex, 0).data();
			var invType = table.cell(rowIndex, 8).data();
			var phone = table.cell(rowIndex, 5).data();
			var email = table.cell(rowIndex, 4).data();

			if (columnVisible == 0) {
				var token  = document.getElementsByName("token")[0].value;
				$.ajax({
					url : "invoicePopup",
					type : "POST",
					timeout: 0,
					data : {
						svalue : invId,
						token:token,
						"struts.token.name": "token",
					},
					success : function(data) {
						
						var message = data;
						new Messi(message, {
							center : true,
							buttons : [ {
								id : 0,
								label : 'Close',
								val : 'X'
							} ],
							callback : function(val) {
								$('.messi,.messi-modal').remove();
								var table = $('#invoiceDataTable')
								.DataTable();
								// table.ajax.reload();
							},
							//width : '820px',
							modal : true
						});
					}
				});
			} else if(columnVisible == 20) {
				if(email!=null) {
					if(email!='') {
						var answer = confirm("Are you sure you want to send invoice link?");
					}
				} else {
					var answer=false;
				}
				
				if (answer != true) {
					return false;
				}


				var token  = document.getElementsByName("token")[0].value;
				$.ajaxSetup({
					global: false,
					beforeSend: function () {
						$(".modal").show();
					},
					complete: function () {
						$(".modal").hide();
					}
				});
					
				$.ajax({
					url : "invoiceEmailAction",
					type : "POST",
					timeout: 0,
					data : {
						invoiceId : invId,
						token:token,
						"struts.token.name": "token",
					},
					success : function(data) {
						alert('Invoice link has been send successfully !!');																		
					},
					error : function(data) {
						alert('Unable to send invoice link !!');																		
					}
				});
				
				/* $.ajax({
					url : "invoiceQRCodeAction",
					type : "POST",
					timeout: 0,
					data : {
						invoiceId : invId,
						token:token,
						"struts.token.name": "token",
					},
					success : function(data) {
						alert('Invoice link has been send successfully through SMS !!');																		
					},
					error : function(data) {
						alert('Unable to send invoice link to Phone !!');																		
					}
				}); */
				
			} else if(columnVisible == 21) {
				if(phone!=null) {
					if(phone!='') {
						var answer = confirm("Are you sure you want to send invoice link through SMS?");
					}
				} else {
					var answer=false;
				}

				if (answer != true) {
					return false;
				}
			
				var token  = document.getElementsByName("token")[0].value;
				$.ajaxSetup({
					global: false,
					beforeSend: function () {
						$(".modal").show();
					},
					complete: function () {
						$(".modal").hide();
					}
				});

				$.ajax({
					url : "invoiceSMSAction",
					type : "POST",
					timeout: 0,
					data : {
						invoiceId : invId,
						token:token,
						"struts.token.name": "token",
					},
					success : function(data) {
						alert('Invoice link has been send successfully through SMS !!');																		
					},
					error : function(data) {
						alert('Unable to send invoice link to Phone !!');																		
					}
				});				
			} else if (columnVisible == 22) {
				var answer = confirm("Are you sure you want to download QR Code");
				if (answer != true) {
					return false;
				}
								
				document.getElementById('invoiceId').value = invId;
				document.downloadqrcode.submit();		
			}

	/* 		 else if(columnVisible == 11){
					var answer = confirm("Are you sure you want to send invoice link?");
					if (answer != true) {
						return false;
					}
					if(invType == "INVOICE PAYMENT") {
						/* var answer = confirm("Are you sure you want to send invoice link?");
						if (answer != true) {
							return false;
						} 
						var token  = document.getElementsByName("token")[0].value;
						$.ajaxSetup({
								global: false,
								beforeSend: function () {
								$(".modal").show();
								},
								complete: function () {
									$(".modal").hide();
								}
							});
						$.ajax({
							url : "invoiceEmailAction",
							type : "POST",
							timeout: 0,
							data : {
								invoiceId : invId,
								token:token,
								"struts.token.name": "token",
							},
							success : function(data) {
								alert('Invoice link has been send successfully !!');																		
							},
							error : function(data) {
								alert('Unable to send invoice link !!');																		
							}
						});
					}

				} */
		}
		
		function decodeDiv() {
			var divArray = document.getElementsByTagName('div');
			for (var i = 0; i < divArray.length; ++i) {
				var div = divArray[i];
				if (div.id.indexOf('param-') > -1) {
					var val = div.innerHTML;
					div.innerHTML = decodeVal(val);
				}
			}
		}

		function decodeVal(value) {
			var txt = document.createElement("textarea");
			txt.innerHTML = value;
			return txt.value;
		}

		function generatePostData() {
			var token = document.getElementsByName("token")[0].value;
			var obj = {
					/* invoiceNo : document.getElementById("invoiceNo").value, */
				productName : document.getElementById("productName").value,
				customerPhone : document.getElementById("customerPhone").value,
				customerEmail : document.getElementById("customerEmail").value,
				merchantPayId : document.getElementById("merchants").value,
				subMerchantId :  document.getElementById("subMerchant").value,
				currency : document.getElementById("currency").value,
				invoiceType : document.getElementById("invoiceType").value,
				status : document.getElementById("statusType").value,
				dateFrom : document.getElementById("dateFrom").value,
				dateTo : document.getElementById("dateTo").value,
				token:token,
				"struts.token.name": "token",
			};

			return obj;
		}						
	</script>

	<style>
		.ui-widget-header { color: #000; }
		.font-weight-bold { font-weight: bold !important; }
        .font-size-14 { font-size: 14px !important; }
		.mb-10 { margin-bottom: 10px !important; }
		.mb-20 { margin-bottom: 20px !important; }
		.text-white { color: white !important; }
		.bg-primary { background-color: #173a71; }
		.lpay_table td, .lpay_table th { padding: 5px; font-weight: normal !important; }
		td.my_class {
			color: #173a71;
			text-decoration: underline;
			font-weight: medium;
			cursor: pointer;
			transition: all ease .5s;
		}
		td.my_class:hover { text-decoration: none; }

		.position-relative {
			position: relative;
		}
	</style>
	
</head>
	<body id="mainBody">
		<input type="hidden" id="isSuperMerchant">
		<section class="invoice-search lpay_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Invoice Search</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_input_group">
						<label for="">Product Name</label>
						<s:textfield
							id="productName"
							class="lpay_input"
							name="productName"
							autocomplete="off"
							oninput="alphaNumericSpace(this);"
						
							onblur="trimSpace(this);">
						</s:textfield>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->
				
				<div class="col-md-3 mb-20">
					<div class="lpay_input_group position-relative">
						<label for="">Customer Phone</label>
						<s:textfield
							id="customerPhone"
							class="lpay_input"
							name="customerPhone"
							autocomplete="off"
							maxlength="10"
							onkeypress=""
							onblur="validatePhoneNumber(this); handleChange();"
							oninput="onlyNumberInput(this); removeError(this);">
						</s:textfield>
						<span data-id="customerPhone" class="error text-danger"></span>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Customer Email</label>
					<s:textfield
						id="customerEmail"
						class="lpay_input"
						name="customerEmail"
						type="text"
						value=""
						maxlength="120"
						autocomplete="off"
						onblur="handleChange(); _validateEmail(this);"
						oninput="removeError(this);">
					</s:textfield>
					<span data-id="customerEmail" class="error text-danger"></span>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
						   <label for="">Select Merchant</label>
							<s:select
								name="merchants"
								class="selectpicker"
								id="merchants"
								headerKey="ALL"
								headerValue="ALL"
								list="merchantList"
								listKey="payId"
								listValue="businessName"
								data-live-search="true"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->
				</s:if>
				<s:else>
				<div class="col-md-3 mb-20 d-none">
					<div class="lpay_select_group">
					   <label for="">Select Merchant</label>
					   <s:select name="merchants" class="selectpicker"
						id="merchants" data-live-search="true" list="merchantList" listKey="payId"
						listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				</s:else>
				<!-- /.col-3 -->
			<s:if test="%{#session['USER'].superMerchant == true || superMerchantFlag == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" name="subMerchantId" headerKey="ALL"
					   headerValue="ALL" class="selectpicker textFL_merch" id="subMerchant"
							list="subMerchantList" listKey="payId"
							listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-id="submerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantId" id="subMerchant" class="textFL_merch"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Status</label>
					   <s:select name="status"
					   id="statusType" headerValue="ALL" headerKey="ALL"
					   list="statusType"
					   listKey="key" listValue="value" class="selectpicker" autocomplete="off"/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Currency</label>
					   <s:select name="currency"
					   id="currency" headerValue="ALL" headerKey="ALL"
					   list="currencyMap" class="selectpicker" autocomplete="off"/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Payment Type</label>
					   <s:select name="invoiceType"
					   id="invoiceType" headerValue="ALL" headerKey="ALL"
					   list="invoiceSearchPromotionalPaymentType"
					   listKey="name" listValue="name" class="selectpicker" autocomplete="off"/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield type="text" id="dateFrom" name="dateFrom"
					class="lpay_input" autocomplete="off" readonly="true" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" id="dateTo" name="dateTo"
				class="lpay_input" autocomplete="off" readonly="true" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->
				<div class="col-md-12 text-center">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="searchBtn">Submit</button>
				</div>
				<!-- /.col-md-12 text-center -->
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		<section class="invoice-search lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Invoice Search Data</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="lpay_table">
						<table id="invoiceDataTable" class="display" cellspacing="0" width="100%">
							<thead class="lpay_table_head">
								<tr>
									<th class="invoice-width">Invoice Id</th>
									<th>Date</th>
									<th>Merchant</th>
									<th>Sub Merchant</th>
									<th>Name</th>
									<th>Email</th>
									<th>Phone</th>
									<th class="no-width">Product Name</th>
									<th>Currency</th>
									<th>Amount</th>
									<th>UDF11</th>
									<th>UDF12</th>
									<th>UDF13</th>
									<th>UDF14</th>
									<th>UDF15</th>
									<th>UDF16</th>
									<th>UDF17</th>
									<th>UDF18</th>
									<th>Type</th>
									<th>Status</th>
									<th>Email</th>
									<th>SMS</th>
									<th>QR</th>
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
    <s:form name="downloadqrcode" action="invoiceQRCodeAction">
		<s:hidden name="invoiceId" id="invoiceId" value="" />	
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form> 

	<script src="../js/commanValidate.js"></script>
</body>
</html>