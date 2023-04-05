<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Product Wise Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/common-scripts.js"></script>


<script type="text/javascript">

	// $(window).on("load", function(e){
	// 	function hideColumn(){
	// 		var _getMerchant = $("#merchant").val();
	// 		var _getMerchantInput = $("#setSuperMerchant").val();
	// 		if(_getMerchant == ""){
	// 			console.log($("#glocalFlag").text());
	// 			var td = $("#txnResultDataTable").DataTable();
	// 			td.columns(21).visible(false);
	// 			td.columns(22).visible(false);
	// 			td.columns(23).visible(false);
	// 			td.columns(24).visible(false);
	// 			td.columns(4).visible(false);
			
	// 		}
	// 		if($("#deliveryStatusFlag").val() == "false" || $("#deliveryStatusFlag").val() == ""){
	// 			td.columns(4).visible(false);

	// 		}

	// 		if(_getMerchantInput == "" || _getMerchantInput == "NA"){
	// 			td.columns(3).visible(false);
	// 		}
	// 	}
	// 	hideColumn();
	// });
	
	function hideColumn(){
		var _userType = $("#userType").val();
		var _getMerchant = $("#merchant").val();
		var _getMerchantInput = $("#setSuperMerchant").val();
		var td = $("#txnResultDataTable").DataTable();
		var _glocalFlag = $("#glocalFlag").attr("data-input");
		console.log(_glocalFlag);
		if(_userType == "SUBUSER"){
			td.columns(18).visible(false);
		}
		if(_glocalFlag != "true"){
			// console.log(_glocalFlag);
			td.columns(21).visible(false);
			td.columns(22).visible(false);
			td.columns(23).visible(false);
			td.columns(24).visible(false);
			td.columns(4).visible(false);
		}else{
			td.columns(21).visible(true);
			td.columns(22).visible(true);
			td.columns(23).visible(true);
			td.columns(24).visible(true);
			td.columns(4).visible(true);
		}
		if(_getMerchantInput == "" || _getMerchantInput == "NA"){
			td.columns(3).visible(false);
		}else{
			td.columns(3).visible(true);
		}

	}

	$(document).ready(function() {
		if($("#gloc").val() == "true"){
		$("[data-id=deliveryStatus]").removeClass("d-none");
		$("[data-id=deliveryStatus] select").selectpicker();
		$("[data-id=deliveryStatus] select").selectpicker('val', "All");
	}

		var _select = "<option value='ALL'>ALL</option>"
		$("[data-id=subMerchant]").find('option:eq(0)').before(_select);
		$("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");

		

		$("#merchant").on("change", function(e){
			var _merchant = $(this).val();
			if(_merchant != ""){
				$("body").removeClass("loader--inactive");
				$.ajax({
					type: "POST",
					url: "getSubMerchantList",
					data: {"payId": _merchant},
					success: function(data){
						$("#subMerchant").html("");
						if(data.superMerchant == true){
							var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
							for(var i = 0; i < data.subMerchantList.length; i++){
								_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
							}
							$("[data-id=submerchant]").removeClass("d-none");
							$("#subMerchant option[value='']").attr("selected", "selected");
							$("#subMerchant").selectpicker();
							$("#subMerchant").selectpicker("refresh");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
						}else{
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-id=submerchant]").addClass("d-none");
							$("[data-id=deliveryStatus]").addClass("d-none");
							$("[data-id=deliveryStatus]").val("");
							$("#subMerchant").val("");
						}
					
						if(data.glocalFlag == true){
						$("[data-id=deliveryStatus]").removeClass("d-none");
						$("[data-id=deliveryStatus] select").selectpicker('val', 'All');
						}else{
							$("[data-id=deliveryStatus]").addClass("d-none");
						}
					}
				});
			}else{
				$("[data-id=submerchant]").addClass("d-none");
				$("#subMerchant").val("");
				$("[data-id=deliveryStatus]").addClass("d-none");
				$("[data-id=deliveryStatus]").val("");	
			}
		})

		$(function() {
			renderTable();
		});

		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			$("#setSuperMerchant").val('');
			reloadTable();
			// hideColumn();
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
					return typeof i === 'string' ? i.replace(/[\,]/g, '') * 1 : typeof i === 'number' ? i : 0;
				};
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
				$(api.column(17).footer()).html('' + pageTotal.toFixed(2) + ' ' + ' ');

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

						// Total over all pages
				total = api.column(19).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(19, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);
				// Update footer
				$(api.column(19).footer()).html(
						'' + pageTotal.toFixed(2) + ' ' + ' ');

				// Total over all pages
				total = api.column(20).data().reduce(
						function(a, b) {
							return intVal(a) + intVal(b);
						}, 0);

				// Total over this page
				pageTotal = api.column(20, {
					page : 'current'
				}).data().reduce(function(a, b) {
					return intVal(a) + intVal(b);
				}, 0);
				// Update footer
				$(api.column(20).footer()).html(
						'' + pageTotal.toFixed(2) + ' ' + ' ');
			},
							"columnDefs" : [ {
								className : "dt-body-right",
								"targets" : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]
							}
							
							 ],
							dom : 'BTrftlpi',
							buttons : [
									$.extend(true, {}, buttonCommon, {
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14]
										},
									}),
									$.extend(true, {}, buttonCommon, {
										extend : 'csvHtml5',
										title : 'Product Wise Report',
										exportOptions : {

											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14]
										},
									}),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize : 'legal',
										//footer : true,
										title : 'Product Wise Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14]
										},
										customize : function(doc) {
											doc.defaultStyle.alignment = 'center';
											doc.styles.tableHeader.alignment = 'center';
										}
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Product Wise Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14]
										}
									},
									{
										extend : 'colvis',
										columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14]
									} ],

							"ajax" : {

								"url" : "productReportSearchAction",
								"type" : "POST",
								"data" : function(d) {
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {
								hideColumn();
								$("#submit").removeAttr("disabled");
								$("body").addClass("loader--inactive");
							},
							"searching" : false,
							"ordering" : false,
							"destroy": true,
							"processing" : true,
							"serverSide" : true,
							"paginationType" : "full_numbers",
							"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
							"order" : [ [ 2, "desc" ] ],

							"columnDefs" : [ {
								"type" : "html-num-fmt",
								"targets" : 4,
								"orderable" : true,
								"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
							},
							
							{
								'targets': 0,
								'createdCell':  function (td, cellData, rowData, row, col) {
									$("#setSuperMerchant").val(rowData["subMerchantId"]);
									$("#deliveryStatusFlag").val(rowData["deliveryStatus"]);
								}
							}],

							"columns" : [
									{
										"data" : null,
										"className" : "txnId my_class text-class",
										"width" : "60px !important;",
										"render" : function (data, type, row) {
										return '<span id="glocalFlag" data-input="'+data["glocalFlag"]+'">'+data["transactionId"]+'</span>'
										}
									},
									{
										"data" : "pgRefNum",
										"className" : "payId text-class"

									},
									{
										"data" : "merchants",
										"className" : "text-class"
									},

									{
										"data" : "subMerchantId",
										"className" : "text-class"
									},

									{
										"data" : "deliveryStatus",
										"className" : "text-class"
									},

									{
										"data" : "dateFrom",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "orderId",
										"className" : "orderId text-class"
									},
									{
										"data" : "paymentMethods",
										"render" : function(data, type, full) {
											return full['paymentMethods'] + ' '
													+ '-' + ' '
													+ full['mopType'];
										},
										"className" : "text-class"
									},
									{
										"data" : "paymentRegion",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "categoryCode",
										"className" : "text-center",
										"width" : "10%"
									},
									{
										"data" : "SKUCode",
										"className" : "text-center",
										"width" : "10%"
									},
									{
										"data" : "cardNumber",
										"className" : "text-class",
										"width" : "10%"
									},
									{
										"data" : "customerName",
										"className" : "text-class",
										"width" : "10%"
									},
									/* {
										"data" : "acquirerMode",
										"className" : "text-class",
										"width" : "10%"
									}, */
									
									
									
									{
										"data" : "cardHolderType",
										"className" : "text-class"								
									},
									{
										"data" : "txnType",
										"className" : "txnType text-class"
									},
									{
										"data" : "status",
										"className" : "status text-class"
									},
									{
										"data" : "amount",
										"className" : "text-class"

									},{
										"data" : "tdr_Surcharge",
										"className" : "text-class"
								
									},{
										"data" : "gst_charge",
										"className" : "text-class"								
									},
									{
										"data" : "totalAmount",
										"className" : "text-class"
									},
									{
										"data" : "totalAmtPayable",
										"className" : "text-class"
									},

									{
										"data" : "doctor",
										"className" : "text-class"
									},

									{
										"data" : "glocal",
										"className" : "text-class"
									},

									{
										"data" : "partner",
										"className" : "text-class"
									},

									{
										"data" : "uniqueId",
										"className" : "text-class"
									},

									{
										"data" : "postSettledFlag",
										"className" : "text-class",
										"width" : "5%"
                                    },
                                    {
                                        "data" : "partSettle",
                                        "className" : "text-class",
                                        "width" : "5%"
                                    },
									/* {
										"data" : null,
										"className" : "center",
										"orderable" : false,
										"mRender" : function(row) {											

											if(row.customFlag == "Y") {
													return '<button class="btn btn-info btn-xs btn-block btnInvoice" style="font-size:10px;">Download </button>';
												} else {
													return "Not Available";
												}
										}
									}, */
									{
										"data" : "payId",
										"visible" : false
									},									
									{
										"data" : null,
										"className" : "text-class",
										"mRender": function(row){
											if(row.showRefundButton == true){
												return '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;">Refund</button>';
											}else{
												return 'Refunded';
											}
										}
									},

									{
										"data" : "productDesc",
										"visible" : false
									},
									{
										"data" : null,
										"visible" : false,
										"className" : "displayNone",
										"mRender" : function(row) {
											return "\u0027" + row.transactionId;
										}
									}, {
										"data" : "internalCardIssusserBank",
										"visible" : false,
										"className" : "displayNone"
									}, {
										"data" : "internalCardIssusserCountry",
										"visible" : false,
										"className" : "displayNone"
									}, {
										"data" : "oId",
										"visible" : false,
										"className" : "displayNone"
									} ]
						});

		
		 $(document).ready(function() {
		 	var table = $('#txnResultDataTable').DataTable();
			$('#txnResultDataTable').on('click','.btnRefund',function() {
				var _btn = $(this).text();
				var _parent = $(this).closest("td");
				if(_btn !== "Refunded") {
					var columnIndex = table.cell(_parent).index().column;
					var rowIndex = table.cell(_parent).index().row;
					var rowNodes = table.row(rowIndex).node();
					var rowData = table.row(rowIndex).data();

					var txnType1 = rowData.txnType;
					var totalAmount = rowData.totalAmount;

					var _pgRefNum =  rowData.pgRefNum;
					var _payId = rowData.payId;

					var orderId1 = rowData.orderId; 					 
					var txnId1 = Number(rowData.transactionId); 

					var refundAvailable = rowData.refundAvailable;
					var refundedAmount = rowData.refundedAmount;

					$("#refundedAmount").val(refundedAmount);
					$("#refundAvailable").val(refundAvailable);

					$("#payId").val(_payId);
					$("#pg-ref").val(_pgRefNum);

					$("body").removeClass("loader--inactive");
					$("#manualRefundProcess").submit();
				}
			});


		$('#txnResultDataTable').on('click','.btnInvoice',function() {
			
			
						var _btn = $(this).text();
						var _parent = $(this).closest("td");
							var columnIndex = table.cell(_parent).index().column;
							var rowIndex = table.cell(_parent).index().row;
							var rowNodes = table.row(rowIndex).node();
							var rowData = table.row(rowIndex).data();
							var _payId = rowData.payId;
							var orderId1 = rowData.orderId; 					 

							fetchInvoice(orderId1,_payId);
					});

			$('#txnResultDataTable').on('click','.btnChargeBack',function() {
				var _parent = $(this).closest("td");
				var columnIndex = table.cell(_parent).index().column;
				var rowIndex = table.cell(_parent).index().row;
				var rowNodes = table.row(rowIndex).node();
				var rowData = table.row(rowIndex).data();
				var txnType1 = rowData.txnType;
				var status1 = rowData.status;

				var refundAvailable = rowData.refundAvailable;
				var refundedAmount = rowData.refundedAmount;
				var pgRefNum = rowData.pgRefNum;

				$("#chargeback-pgRefNum").val(pgRefNum);
				$("#chargeback-refundedAmount").val(refundedAmount);
				$("#chargeback-refundAvailable").val(refundAvailable);
				
				var payId1 =  rowData.pgRefNum;										
				var orderId1 = rowData.orderId; 					 
				var txnId1 = Number(rowData.transactionId); 
				document.getElementById('payIdc').value = payId1;
				document.getElementById('orderIdc').value = orderId1;
				document.getElementById('txnIdc').value = txnId1;

				$("body").removeClass("loader--inactive");
				document.chargeback.submit();
			});
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
	}



	function fetchInvoice(orderId , payId) {
		
		var token = document.getElementsByName("token")[0].value;
		
		$.ajax({
			url : "fetchEventInvoice",
			type : "POST",
			timeout : 0,
			data : {
				orderId : orderId,
				payId : payId,
				token : token
			},
			success : function(response) {
				
				var base64Data = response.aaData.base64Data;
				var dataType = response.aaData.dataType;
				var linkSource = "data:application/"+dataType+";base64,"+base64Data;
				var downloadLink = document.createElement("a");
				var fileName = orderId+"_invoice."+dataType;
				downloadLink.href = linkSource;
				downloadLink.download = fileName;
				downloadLink.click();

			},
			error : function(data) {
				alert("Something went wrong!");
				setTimeout(function() {
					_body.classList.add("loader--inactive");
				}, 1000);
			}
		});
	}
	
	
	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var merchantEmailId = document.getElementById("merchant").value;
		// var transactionType = document.getElementById("transactionType").value;
		var paymentType = document.getElementById("paymentMethod").value;
		// var status = document.getElementById("status").value;
		var currency = document.getElementById("currency").value;
		var deliveryStatus = $("#deliveryStatus").val();
		var partSettleFlag = document.getElementById("partSettleFlag").value;
		var _subMerchantEmailId = $("#subMerchant").val();

		if (merchantEmailId == '') {
			merchantEmailId = 'ALL'
		}
		// if (transactionType == '') {
		// 	transactionType = 'ALL'
		// }
		if (paymentType == '') {
			paymentType = 'ALL'
		}
		// if (status == '') {
		// 	status = 'ALL'
		// }
		if (currency == '') {
			currency = 'ALL'
		}

		var obj = {
			transactionId : document.getElementById("pgRefNum").value,
			orderId : document.getElementById("orderId").value,
			customerEmail : document.getElementById("customerEmail").value,
			categoryCode  : document.getElementById("categoryCode").value,
			SKUCode  : document.getElementById("SKUCode").value,
			merchantEmailId : merchantEmailId,
			// transactionType : transactionType,
			subMerchantEmailId : _subMerchantEmailId,
			paymentType : paymentType,
			deliveryStatus : deliveryStatus,
			// status : status,
			currency : currency,
			partSettleFlag : partSettleFlag,
			dateFrom : document.getElementById("dateFrom").value,
			dateTo : document.getElementById("dateTo").value,
			custId : document.getElementById("custId").value,
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
</head>
<body id="mainBody">
	<s:hidden value="%{#session.USER.UserType}" id="userType"></s:hidden>
	<input type="hidden" id="deliveryStatusFlag" />
	<input type="hidden" id="setSuperMerchant">
	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Sale Transaction Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">PG REF Number</label>
				<s:textfield id="pgRefNum" class="lpay_input" name="pgRefNum"
				type="text" value="" autocomplete="off"
				onkeypress="javascript:return isNumber (event)" maxlength="16" ></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Order ID</label>
				<s:textfield id="orderId" class="lpay_input" name="orderId"
				type="text" value="" autocomplete="off"
				onkeypress="return Validate(event);"
				onblur="this.value=removeSpaces(this.value);"></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Cust ID</label>
				<s:textfield id="custId" class="lpay_input" name="custId"
				type="text" value="" autocomplete="off"
				onkeypress="return Validate(event);"
				onblur="this.value=removeSpaces(this.value);"></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Cust Email</label>
				<s:textfield id="customerEmail" class="lpay_input"
				name="customerEmail" type="text" value="" autocomplete="off"
				onblur="validateEmail(this);"></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">SKU Code</label>
					<s:textfield
						id="SKUCode"
						class="lpay_input"
						name="SKUCode"
						maxlength="40"
						type="text"
						oninput="allowAlphaNumericSpecial(this)"
						autocomplete="off">
					</s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Category Code</label>
					<s:textfield
						id="categoryCode"
						class="lpay_input"
						name="categoryCode"
						maxlength="40"
						type="text"
						oninput="allowAlphaNumericSpecial(this)"
						autocomplete="off">
					</s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Merchant</label>

				<s:if
				   test="%{#session.USER.UserType.name()=='RESELLER'}">
				   <s:select name="merchant" class="selectpicker"
					   id="merchant" headerKey="" data-live-search="true" headerValue="ALL"
					   list="merchantList" listKey="emailId"
					   listValue="businessName" autocomplete="off" />
				</s:if>
				<s:else>
				<s:if
				   test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
				   <s:select name="merchant" class="selectpicker"
					   id="merchant" headerKey="" data-live-search="true" headerValue="ALL"
					   list="merchantList" listKey="emailId"
					   listValue="businessName" autocomplete="off" />
				</s:if>
					<s:else>
						<s:select name="merchant" data-live-search="true" class="selectpicker" id="merchant"
							list="merchantList" listKey="emailId"
							listValue="businessName" autocomplete="off" />
					</s:else>
				</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" name="subMerchant" class="selectpicker" id="subMerchant"
							list="subMerchantList" listKey="emailId"
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
					   <select name="subMerchant" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<div class="col-md-3 mb-20 d-none" data-id="deliveryStatus">
				<div class="lpay_select_group">
				   <label for="">Delivery Status</label>
				   <select class="selectpicker" name="deliveryStatus" id="deliveryStatus">
						<option value="">Select Delivery Status</option>
						<option value="All">ALL</option>
					   <option value="DELIVERED">Delivered</option>
					   <option value="NOT DELIVERED">Not Delivered</option>
					   <option value="PENDING">Pending</option>
				   </select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Payment Method</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.PaymentType@values()"
				   listValue="name" listKey="code" name="paymentMethod"
				   id="paymentMethod" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			
			<!-- <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Transaction Type</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="txnTypelist" listValue="name" listKey="code"
				   name="transactionType" id="transactionType" autocomplete="off"
				   value="name" />
				</div>				
			</div> -->
			
			
			<!-- <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Status</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="lst" name="status" id="status" value="name"
				   listKey="name" listValue="name" autocomplete="off" />
				</div>				
			</div>			 -->

			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Currency</label>
				   <s:select name="currency" id="currency" headerValue="ALL"
					headerKey="" list="currencyMap" class="selectpicker" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Settlement Type</label>
				   <s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
				   list="#{'N':'Normal','Y':'Part'}" name="partSettleFlag" id = "partSettleFlag" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" id="dateFrom" name="dateFrom"
				class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" id="dateTo" name="dateTo"
				class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary" />
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Sale Transaction Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="display" cellspacing="0"
							width="100%">
							<thead>
								<tr class="lpay_table_head">
									<th style='text-align: center; text-decoration: none !important;'>Txn Id</th>
									<th style='text-align: center'>Pg Ref Num</th>
									<th style='text-align: center'>Merchant</th>
									<th style='text-align: center'>Sub Merchant</th>
									<th style='text-align: center'>Delivery Status</th>
									<th style='text-align: center'>Date</th>
									<th style='text-align: center'>Order Id</th>
									<th style='text-align: center'>Payment Method</th>
									<th style='text-align: center'>Payment Region</th>
									<th style='text-align: center'>Category Code</th>
									<th style='text-align: center'>SKU Code</th>
									<th style='text-align: center'>Card Mask</th>
									<th style='text-align: center'>Cust Name</th>
									<th style='text-align: center'>Cardholder Type</th>
									<th style='text-align: center'>Txn Type</th>
									<th style='text-align: center'>Status</th>
									<th style='text-align: center'>Base Amount</th>
									<th style='text-align: center'>TDR / Surcharge</th>
									<th style='text-align: center'>GST</th>
									<th style='text-align: center'>Total Amount</th>
									<th style='text-align: center'>Merchant Amount</th>
									<th style='text-align: center'>Doctor</th>
									<th style='text-align: center'>Glocal</th>
									<th style='text-align: center'>Partner</th>
									<th style='text-align: center'>Unique Id</th>
									<th style='text-align: center'>Post Settled Flag</th>
									<th style='text-align: center'>Part Settled Flag</th>
									<!-- <th style='text-align: center'>Download Invoice</th> -->
									
								
									<th style='text-align: center'>Action</th>
						          
								  <s:if
									test="%{(#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER.UserType.name()=='MERCHANT')}">
									<th style="text-align:center;">Action</th>
								</s:if>
								<s:else>
									<th style='text-align: center'></th>
								</s:else>
								
								<th></th>
								
								<th style='text-align: center'>Refund</th>	
						          
								  <s:if
									test="%{(#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER.UserType.name()=='MERCHANT')}">
									<th style="text-align:center;">Refund</th>
								</s:if>
								<s:else>
									<th style='text-align: center;padding: 0;'></th>
								</s:else>
								
								<th></th>
								
								</tr>
							</thead>
							<tfoot>
								<tr class="lpay_table_head">
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
									<!-- <th></th> -->
									<!-- <th></th> -->
									<th style="text-align: right" rowspan="1" colspan="2"></th>
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

	<s:form name="chargeback" action="chargebackAction">
		<s:hidden name="orderId" id="orderIdc" value="" />
		<s:hidden name="payId" id="payIdc" value="" />
		<s:hidden name="refundedAmount" id="chargeback-refundedAmount" value="" />
		<s:hidden name="pgRefNum" id="chargeback-pgRefNum" value="" />
		<s:hidden name="refundAvailable" id="chargeback-refundAvailable" value="" />
		<s:hidden name="txnId" id="txnIdc" value="" />
		
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form> 


	<s:form name="refundDetails" action="refundConfirmAction">
		<s:hidden name="orderId" id="orderIdr" value="" />
		<s:hidden name="payId" id="payIdr" value="" />
		<s:hidden name="transactionId" id="txnIdr" value="" />
		<s:hidden name="amount" id="amountr" value="" />
		<s:hidden name="totalAmount" id="totalAmountr" value="" />
		
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>


	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}

		var allowAlphaNumericSpecial = function(that) {
			that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
		}
	</script>

	<s:form name="manualRefundProcess" id="manualRefundProcess" action="manualRefundProcess">
		<s:hidden name="payId" id="payId" value="" />
		<s:hidden name="pgRefNum" id="pg-ref" value="" />
		<s:hidden name="refundedAmount" id="refundedAmount" value="" />
		<s:hidden name="refundAvailable" id="refundAvailable" value="" />

		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>
</body>
</html>