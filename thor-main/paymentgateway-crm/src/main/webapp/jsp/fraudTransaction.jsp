<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Fraud Transaction</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../css/loader.css" rel="stylesheet" type="text/css" />
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/select2.min.js"></script>
<script type="text/javascript">
	function decodeVal(text) {
		return $('<div/>').html(text).text();
	}
	function handleChange() {
		reloadTable();
	}
	$(document).ready(function() {

		$(function() {

			$("#dateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#dateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			renderTable();
		});

		$(function() {
			var datepick = $.datepicker;
			var table = $('#fraudDataTable').DataTable();
			$('#fraudDataTable tbody').on('click', 'td', function() {

				popup(table, this);
			});
		});
	});
	function renderTable() {
		var merchantEmailId = document.getElementById("merchant").value;
		var table = new $.fn.dataTable.Api('#fraudDataTable');
		//to show new loader -Harpreet
		$.ajaxSetup({
			global : false,
			beforeSend : function() {
				toggleAjaxLoader();
			},
			complete : function() {
				toggleAjaxLoader();
			}
		});
		var transFrom = $.datepicker
				.parseDate('dd-mm-yy', $('#dateFrom').val());
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
		$('#fraudDataTable')
				.dataTable(
						{
							"footerCallback" : function(row, data, start, end,
									display) {
								var api = this.api(), data;

								// Remove the formatting to get integer data for summation
								var intVal = function(i) {
									return typeof i === 'string' ? i.replace(
											/[\,]/g, '') * 1
											: typeof i === 'number' ? i : 0;
								};

								// Total over all pages
								total = api.column(7).data().reduce(
										function(a, b) {
											return intVal(a) + intVal(b);
										}, 0);

								// Total over this page
								pageTotal = api.column(7, {
									page : 'current'
								}).data().reduce(function(a, b) {
									return intVal(a) + intVal(b);
								}, 0);

								// Update footer
								$(api.column(7).footer()).html(
										'' + pageTotal.toFixed(2) + ' ' + ' ');
							},
							"columnDefs" : [ {
								className : "dt-body-right",
								"targets" : [1,2,3,4,5,6,7]
							} ],
							dom : 'BTftlpi',
							buttons : [ {
								extend : 'copyHtml5',
								//footer : true,
								exportOptions : {
									columns : [ 14, 1, 2, 3, 4, 5, 6, 7 ]
								}
							}, {
								extend : 'csvHtml5',
								//footer : true,
								title : 'Fraud Transactions',
								exportOptions : {
									columns : [ 14, 1, 2, 3, 4, 5, 6, 7 ]
								}
							}, {
								extend : 'pdfHtml5',
								orientation : 'landscape',
								//footer : true,
								title : 'Fraud Transactions',
								exportOptions : {
									columns : [ ':visible' ]
								}
							}, {
								extend : 'print',
								//footer : true,
								title : 'Fraud Transactions',
								exportOptions : {
									columns : [ 0, 1, 2, 3, 4, 5, 6, 7 ]
								}
							}, {
								extend : 'colvis',
								columns : [ 1, 2, 3, 6, 7 ]
							} ],

							"ajax" : {
								"url" : "fraudTransactionAction",
								"type" : "POST",
								"data" : generatePostData
							},
							"processing" : true,
							"lengthMenu" : [ [ 10, 25, 50, -1 ],
									[ 10, 25, 50, "All" ] ],
							"order" : [ [ 1, "desc" ] ],
							"columns" : [

							{
								"data" : "transactionId",
								"className" : "my_class",
							}, {
								"data" : "txnDate",
								"sWidth" : '14%'
							}, {
								"data" : "orderId"
							}, {
								"data" : "businessName",
								"sWidth" : '13%'
							}, {
								"data" : "customerEmail"
							}, {
								"data" : "paymentMethod"
							},{
								"data" : "responseMsg"
							},{
								"data" : "approvedAmount",
									"width" : '9%'
							}, {
								"data" : "customerName",
								"visible" : false,
								"className" : "displayNone"
							}, {
								"data" : "status",
								"visible" : false,
								"className" : "displayNone"
							},  {
								"data" : "cardNo",
								"visible" : false,
								"className" : "displayNone"
							}, {
								"data" : "productDesc",
								"visible" : false,
								"className" : "displayNone"
							}, {
								"data" : "internalRequestDesc",
								"visible" : false,
								"className" : "displayNone"
							},{
								"mData" : "mopType",
								"visible" : false
							}, {
								"data" : null,
								"visible" : false,
								"className" : "displayNone",
								"mRender" : function(row) {
									return "\u0027" + row.transactionId;
								}
							} ]
						});
		$("#merchant").select2({
			//data: payId
			});
	}

	function reloadTable() {
		var datepick = $.datepicker;
		var transFrom = $.datepicker
				.parseDate('dd-mm-yy', $('#dateFrom').val());
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

		var tableObj = $('#fraudDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
	}

	function popup(table, index) {
		var rows = table.rows();
		var datepick = $.datepicker;
		var columnVisible = table.cell(index).index().columnVisible;
		if (columnVisible == 0) {
			var token = document.getElementsByName("token")[0].value;
			var rowIndex = table.cell(index).index().row;
			var transactionId = table.cell(rowIndex, 0).data();
			var date_from = table.cell(rowIndex, 1).data();
			var currency = "";
			var amount = table.cell(rowIndex, 7).data();
			var payment_method = table.cell(rowIndex, 5).data();
			var mopType = table.cell(rowIndex, 13).data();
			var orderId = table.cell(rowIndex, 2).data();
			var cust_email = table.cell(rowIndex, 4).data();
			if (cust_email == null) {
				cust_email = "Not available";
			}
			var cust_name = table.cell(rowIndex, 8).data();
			var status = table.cell(rowIndex, 9).data();
			var message = table.cell(rowIndex, 6).data();
			var card_number = "";
			var productDesc = table.cell(rowIndex, 11).data();
			var internalRequestDesc = table.cell(rowIndex, 12).data();

			$.post("customerAddressAction", {
				orderId : decodeVal(orderId),
				custEmail : decodeVal(cust_email),
				datefrom : decodeVal(date_from),
				amount : decodeVal(amount),
				currency : decodeVal(currency),
				productDesc : decodeVal(productDesc),
				internalRequestDesc : decodeVal(internalRequestDesc),
				transactionId : decodeVal(transactionId),
				cardNumber : decodeVal(card_number),
				mopType : decodeVal(mopType),
				status : decodeVal(status),
				responseMsg : decodeVal(message),
				token : token,
				showRequestFlag : true,
				"struts.token.name" : "token"
			}).done(function(data) {
				var popupDiv = document.getElementById("popup");
				popupDiv.innerHTML = data;
				decodeDiv();
				$('#popup').popup('show');
			});
		}
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
		var merchantEmailId = document.getElementById("merchant").value;
		if (merchantEmailId == '') {
			merchantEmailId = 'ALL'
		}
		var obj = {
			dateFrom : document.getElementById("dateFrom").value,
			dateTo : document.getElementById("dateTo").value,
			merchantEmailId : merchantEmailId,
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}
</script>
<style>
.dataTables_wrapper {
	position: relative;
	clear: both;
	*zoom: 1;
	zoom: 1;
	margin-top: -30px;
}
</style>
</head>

<body>
	<div id="popup"></div>
	<table width="100%" align="left" cellpadding="0" cellspacing="0"
		class="txnf">
		<tr>
			<td align="left"><h2>Fraud Transactions</h2>
				<div class="container">

					<div class="form-group col-md-2 txtnew col-sm-4 col-xs-6">
						<label for="merchant">Merchant:</label> <br />
						<s:if
							test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select name="merchant" class="form-control" id="merchant"
								headerKey="" headerValue="ALL" list="merchantList"
								listKey="emailId" listValue="businessName" onchange="handleChange();" autocomplete="off" />
						</s:if>
						<s:else>
							<s:select name="merchant" class="form-control" id="merchant"
								headerKey="" headerValue="ALL" list="merchantList"
								listKey="emailId" listValue="businessName" onchange="handleChange();" autocomplete="off" />
						</s:else>
					</div>


					<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
						<label for="dateFrom">Date From:</label> <br />
						<s:textfield type="text" readonly="true" id="dateFrom"
							name="dateFrom" class="form-control" onchange="handleChange();"
							 autocomplete="off" />
					</div>
					<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
						<label for="dateTo">Date To:</label> <br />
						<s:textfield type="text" readonly="true" id="dateTo" name="dateTo"
							class="form-control" onchange="handleChange();" autocomplete="off" />
					</div>
				</div></td>
		</tr>
		<tr>
			<td align="left">&nbsp;</td>
		</tr>
		<tr>
			<td align="left" style="padding: 10px;"><div class="scrollD">
					<table id="fraudDataTable" class="display" cellspacing="0"
						width="100%">
						<thead>
							<tr class="boxheadingsmall">
								<th style='text-align: center'>Txn Id</th>
								<th style='text-align: center'>Date</th>
								<th style='text-align: center'>Order Id</th>
								<th style='text-align: center'>Business Name</th>
								<th style='text-align: center'>Customer Email</th>
								<th style='text-align: center'>Payment Method</th>
								<th style='text-align: center'>Reason </th>
								<th style='text-align: right'>Txn Amount</th>
								<th style='text-align: center'></th>
								<th style='text-align: center'></th>
								<th style='text-align: center'></th>																<th style='text-align: center'></th>
								<th style='text-align: center'></th>
								<th style='text-align: center'></th>
								<th style='text-align: center'></th>
							</tr>
						</thead>
						<tfoot>
							<tr class="boxheadingsmall">
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '>Total Amount</th>
								<th style='text-align:right;  padding-right: 5px;'></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
								<th style='text-align:right; '></th>
							</tr>
						</tfoot>
					</table>
				</div></td>
		</tr>
	</table>
</body>
</html>