<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Bank Exception</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>

<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>

<style>
	.lp-success_generate, .lp-error_generate {
		background-color: #c0f4b4;
		font-size: 15px;
		padding: 10px;
		text-align: center;
		margin-top: 20px;
		border-radius: 5px;
		border: 1px solid #3b9f24;
	}

	.lp-error_generate{
		background-color: #f79999;
		border: 1px solid #771313;
	}

	.lp-success_generate p{ 
		color: #326626;
	}

	.lp-error_generate p{
		color: #921919;
	}
</style>



<script type="text/javascript">


	$(document).ready(function() {

		$(function() {
			renderTable();
		});

		$("#submit").click(function(env) {
			reloadTable();		
		});

		$(function(){
			var datepick = $.datepicker;
			var table = $('#txnResultDataTable').DataTable();
			$('#txnResultDataTable').on('click', 'td.my_class', function() {
				var rowIndex = table.cell(this).index().row;
				var rowData = table.row(rowIndex).data();
			});
		});
	});

	function renderTable() {
		$("body").removeClass("loader--inactive");
		  var merchantEmailId = document.getElementById("merchant").value;
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		
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
		var token = document.getElementsByName("token")[0].value;

		
		 var buttonCommon = {
        exportOptions: {
            format: {
                body: function ( data, column, row, node ) {
                    // Strip $ from digit column to make it numeric
                	return column === 0 ? "'"+data : (column === 1 ? "'" + data: column === 2 ? "'" + data:data);
                }
            }
        }
    };
	
		$('#txnResultDataTable').dataTable({
							/* "footerCallback" : function(row, data, start, end, display) {
								var api = this.api(), data;

								// Remove the formatting to get integer data for summation
								var intVal = function(i) {
									return typeof i === 'string' ? i.replace(/[\,]/g, '') * 1: typeof i === 'number' ? i : 0;
								};

								// Total over all pages
								total = api.column(13).data().reduce(
										function(a, b) {
											return intVal(a) + intVal(b);
										}, 0);

								// Total over this page
								pageTotal = api.column(13, {
									page : 'current'
								}).data().reduce(function(a, b) {
									return intVal(a) + intVal(b);
								}, 0);

								// Update footer
								$(api.column(13).footer()).html(
										'' + pageTotal.toFixed(2) + ' ' + ' ');
							}, */
							 "columnDefs": [{ 
								className: "dt-body-right",
								"targets": [1,2,3,4,5,6,7,8,9,10]
							}], 
							dom : 'BTrftlpi',
								buttons : [
										$.extend( true, {}, buttonCommon, {
											extend: 'copyHtml5',											
											exportOptions : {											
												columns : [':visible']
											},
										} ),
									$.extend( true, {}, buttonCommon, {
											extend: 'csvHtml5',
											title : 'Bank Exception',
											exportOptions : {
												
												columns : [':visible']
											},
										} ),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize: 'legal',
										//footer : true,
										title : 'Bank Exception',
										exportOptions : {
											columns: [':visible']
										},
										customize: function (doc) {
										    doc.defaultStyle.alignment = 'center';
					     					doc.styles.tableHeader.alignment = 'center';
										  }
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Bank Exception',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [ 0,1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
									} ],

							"ajax" :{
								
								"url" : "bankExceptionReportAction",
								"type" : "POST",
								"data": function (d){
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {
									 $("#submit").removeAttr("disabled");
							},
							 "searching": false,
							 "ordering": false,
							 "language": {
								"processing": `<div id="loader-wrapper">
												<div id="loader"></div>
												<div class="loader-section section-left"></div>
												<div class="loader-section section-right"></div>
												</div>`
								},
							 "processing": true,
						        "serverSide": true,
						        "paginationType": "full_numbers", 
						        "lengthMenu": [[10, 25, 50], [10, 25, 50]],
								"order" : [ [ 2, "desc" ] ],
						       
						        "columnDefs": [
						            {
						            "type": "html-num-fmt", 
						            "targets": 4,
						            "orderable": true, 
						            "targets": [0,1,2,3,4,5,6,7,8,9,10]
						            }
						        ], 

 
							"columns" : [ {
								"data" : "pgRefNo",
								"className" : "payId"
							},  {
								"data" : "txnId",
								"className" : "payId"								
							},{
								"data" : "orderId",
								"className" : "orderId"
							}, {
								"data" : "acqId"
							}, {
								"data" : "createdDate"
							},
							{
								"data" : "pgSettledAmount"
							},
							{
								"data" : "acqSettledAmount"
							},
							{
								"data" : "diffAmount"
							},
							{
								"data" : "settledFlag"
							}, {
								"data" : "status"
							}, {
								"data" : "exception"								
							}]
						});
						$("body").addClass("loader--inactive");
	}

	function reloadTable() {
		$("body").removeClass("loader--inactive");
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
		// loader show any case
		setInterval(function(e){
			$("body").addClass("loader--inactive");
		}, 500);
	}

	function generatePostData(d) {
		var merchant = document.getElementById("merchant").value;
		 var acquirer = document.getElementById("acquirer").value;
		 var settledFlag = document.getElementById("settledFlag").value;
		 var status = document.getElementById("status").value;
		 var dateFrom = document.getElementById("dateFrom").value;
		 var dateTo = document.getElementById("dateTo").value;
		 var token  = document.getElementsByName("token")[0].value;
		if(merchant==''){
			merchant='ALL'
		}
		if(acquirer==''){
			acquirer='ALL'
		}
		if(status==''){
			status='ALL'
		}
		if(settledFlag == ""){
			settledFlag = "All";
		}
		var obj = {
			merchant : merchant,
			acquirer : acquirer,
			status : status,
			dateFrom : dateFrom,
			dateTo : dateTo,
			draw : d.draw,
			settledFlag : settledFlag, 
			length :d.length,
			start : d.start, 
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
		
	}
</script>
</head>
<body id="mainBody">	

	<section class="bank-exception lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Bank Exception Report Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:if
				   test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
				   <s:select data-live-search="true" name="merchant" class="selectpicker" id="merchant"
					   headerKey="" headerValue="ALL" list="merchantList"
					   listKey="payId" listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
						<s:select name="merchant" class="selectpicker" id="merchant"
							headerKey="" headerValue="ALL" list="merchantList"
							listKey="payId" listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 mb-20 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Acquirer</label>
				   <s:select headerKey="" data-live-search="true" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
				   listValue="name" listKey="code" name="acquirer"
				   id="acquirer" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Status</label>
				   <s:select name="status" id="status" headerValue="ALL" headerKey="ALL" list="#{'PENDING':'PENDING', 'SUCCESS':'SUCCESS', 'On Hold':'ON HOLD'}" class="selectpicker" autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield type="text" id="dateFrom" name="dateFrom" onchange="dateBaseDownload()" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield type="text" id="dateTo" name="dateTo" onchange="dateBaseDownload()" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Settled</label>
				   <s:select
					name="setteled"
					id="settledFlag"
					headerValue="ALL"
					headerKey="ALL"
					list="#{'Y':'Y', 'N':'N'}"
					class="selectpicker"
					autocomplete="off"
				/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-primary" id="submit">Submit</button>
				<button onclick="downloadFile(event)" class="lpay_button lpay_button-md lpay_button-secondary" id="download_bank">Download</button>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 d-none">
				<div class="lp-success_generate">
					<p>Your file has been generate successfully please see after some time</p>
				</div>
				<!-- /.lp-success_generate -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 d-none">
				<div class="lp-error_generate">
					<p>Please try again after some time</p>
				</div>
				<!-- /.lp-success_generate -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="bank-exception lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
					  <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Bank Exception Report Data</h2>
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
									<th>Pg Ref Num</th>
									<th>Txn ID</th>
									<th>Order ID</th>
									<th>Acquirer</th>
									<th>Create Date</th>
									<th>PG Settled Amount</th>
									<th>Acquirer Settled Amount</th>
									<th>Diff. Amount</th>
									<th>Settled Flag</th>
									<th>Status</th>
									<th>Exception</th>
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
	

<script>
	var downloadFile = function(e) {

		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transTo - transFrom > 61 * 86400000) {
			alert('No. of days can not be more than 60 days');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}

		e.preventDefault();
		console.log(e.target.innerText);
		var _text = e.target.innerText;
		var _id = document.getElementById.bind(document);
		var _merchant = _id("merchant").value;
		var _acquirer = _id("acquirer").value;
		var _status = _id("status").value;
		var _dateFrom = _id("dateFrom").value;
		var _dateTo = _id("dateTo").value;
		var _settledFlag = _id("settledFlag").value;

		if(_merchant == ""){
			_merchant = "ALL";
		}

		if(_acquirer == ""){
			_acquirer = "ALL";
		}
		if(_status == ""){
			_status = "ALL";
		}

		if(_text == "Download"){
			_id("downloadMerchant").value = _merchant;
			_id("downloadAcquirer").value = _acquirer;
			_id("downloadstatus").value = _status;
			_id("downloaddateFrom").value = _dateFrom;
			_id("downloaddateTo").value = _dateTo;
			_id("downloadsettledFlag").value = _settledFlag;
			_id("downloadForm").submit();
		}else{
			$.ajax({
					type: "POST",
					url: "generateBankExceptionReportFileAction",
					data: {
						"merchant" : _merchant,
						"acquirer" : _acquirer,
						"status" : _status,
						"dateFrom" : _dateFrom,
						"dateTo" : _dateTo,
						"settledFlag" : _settledFlag,
						"token" : $("#token").val()
					},
					success: function(data){
						setTimeout(function(e){
							document.querySelector("body").classList.add("loader--inactive");
							if(data.generateReport == true){
								document.querySelector(".lp-success_generate").closest(".col-md-12").classList.remove("d-none");
							}else{
								document.querySelector(".lp-error_generate").closest(".col-md-12").classList.remove("d-none");
							}
						}, 500)
						setTimeout(function(e){
							removeError()
						}, 4000);
					}
			})
		}

	}
	  
		  function removeError(){
			  document.querySelector(".lp-error_generate").closest(".col-md-12").classList.add("d-none");
			  document.querySelector(".lp-success_generate").closest(".col-md-12").classList.add("d-none");
		  }
	  
	  function dateBaseDownload(){
		  var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		  var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		  if (transTo - transFrom > 30 * 86400000) {
			  document.querySelector("#download_bank").innerText = "Generate";
		  }else{
			  document.querySelector("#download_bank").innerText = "Download";
		  }
	  }
</script>

  <s:form method="post" id="downloadForm" action="bankExceptionDownloadReport">
	  <s:hidden name="merchant" id="downloadMerchant" />
	  <s:hidden name="acquirer" id="downloadAcquirer" />
	  <s:hidden name="status" id="downloadstatus" />
	  <s:hidden name="dateFrom" id="downloaddateFrom" />
	  <s:hidden name="dateTo" id="downloaddateTo" />
	  <s:hidden name="settledFlag" id="downloadsettledFlag" /> 
	  <s:hidden name="token" value="%{#session.customToken}" />
  </s:form>
</body>
</html>