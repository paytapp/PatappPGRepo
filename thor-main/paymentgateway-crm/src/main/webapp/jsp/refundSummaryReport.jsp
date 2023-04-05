<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Refund Summary Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	var _select = "<option value='ALL'>ALL</option>";
			$("[data-id='subMerchant']").find('option:eq(0)').before(_select);
			$("[data-id='subMerchant'] option[value='ALL']").attr("selected", "selected");

			$("#merchants").on("change", function(e) {
				var _merchant = $(this).val();
				if(_merchant != "") {
					$("body").removeClass("loader--inactive");
					$.ajax({
						type: "POST",
						url: "getSubMerchantList",
						data: {"payId": _merchant},
						success: function(data) {						
							$("#subMerchant").html("");
							if(data.superMerchant == true){
								var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
								for(var i = 0; i < data.subMerchantList.length; i++) {
									_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
								}

								$("[data-id='submerchant']").removeClass("d-none");
								$("#subMerchant option[value='']").attr("selected", "selected");
								$("#subMerchant").selectpicker();
								$("#subMerchant").selectpicker("refresh");
								setTimeout(function(e){
									$("body").addClass("loader--inactive");
								},500);
							} else {
								setTimeout(function(e){
									$("body").addClass("loader--inactive");
								},500);

								$("[data-id='submerchant']").addClass("d-none");
								$("#subMerchant").val("");
							}
						}
					});
				}else{
					$("[data-id='submerchant']").addClass("d-none");
					$("#subMerchant").val("");	
				}
			});


	$(function() {
			$("#dateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date(),
				changeMonth : true,
        		changeYear : true
			});
	});

		$(function() {
			var today = new Date();
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			renderTable();

		});


	
	//click on submit button
	$("#submitBtn").click(function(env) {
			renderTable();
	});

	function renderTable(){
		var buttonCommon = {
			        exportOptions: {
			            format: {
			                body: function ( data, column, row, node ) {
			                    // Strip $ from salary column to make it numeric
			                    return column === 0 ? "'"+data : (column === 3 ? "'" + data: data);
			                }
			            }
			        }
			    };
		$("body").removeClass("loader--inactive");
		var table = $('#myTable').DataTable( {
			"destroy": true,
			"bSort": false,
			dom : 'BTftlpi',
			buttons : [
								$.extend( true, {}, buttonCommon, {
										extend : 'copyHtml5',
										//footer : true,
										exportOptions : {
											columns : [':visible']
										}
									}),
									$.extend( true, {}, buttonCommon, {
										extend : 'csvHtml5',
										//footer : true,
										title : 'Refund Summary Report',
										exportOptions : {
											columns : [':visible']
										}
									}),
									{
										extend : 'pdfHtml5',
										//footer : true,
										orientation : 'landscape',
										title : 'Refund Summary Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Refund Summary Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										//           collectionLayout: 'fixed two-column',
										columns : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
									} ],
									
	        "ajax": {
               "url": "refundSummaryReportAction",
  
               "data": function (d){
					return generatePostData();
				},
               /*dataSrc: function (response) {
               },*/
               "type": "POST"
           },
           "fnDrawCallback" : function() {
					 //$("#submit").removeAttr("disabled");
					 	$("body").addClass("loader--inactive");
			},
			"searching" : false,
			"processing" : true,
			"serverSide" : false,
			"paginationType" : "full_numbers",
			"lengthMenu" : [ [ 10, 25, 50, -1 ],
							[ 10, 25, 50, "All" ] ],
			"columnDefs" : [ {
								"type" : "html-num-fmt",
								"targets" : 4,
								"orderable" : false,
								"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
							},
							{ "width": "8%", "targets": 3 } 
                         ],
	        "columns": [
	             { "data": "acquirer" },
	            { "data": "paymentType", "className":"width2" },
	            { "data": "mop" },
	            { "data": "txnInitiate", "className" : "width1" },
	            { "data": "captured" },
	            { "data": "declined" },
	            { "data": "rejected" },
	            { "data": "pending" },
	            { "data": "error" },
	            { "data": "timeout" },
	            { "data": "failed" },
	            { "data": "invalid" },
	            { "data": "acqDown","className" : "width2" },
	            { "data": "failedAtAcq","className" : "width3" },
	            { "data": "acqTimeout","className" : "width4" }
	        ],
	       "footerCallback": function ( row, data, start, end, display ) {
            var api = this.api(), data;
 
            // Remove the formatting to get integer data for summation
            var intVal = function ( i ) {
                return typeof i === 'string' ?
                    i.replace(/[\$,]/g, '')*1 :
                    typeof i === 'number' ?
                        i : 0;
            };
 
          
           
            // Total over this page colomn no 3
            pageTotal = api
                .column( 3, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
					if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 3 ).footer() ).html( // Update footer
                pageTotal
            );
            // Total over this page colomn no 4
            pageTotal = api
                .column( 4, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
					if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 4 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 5
            pageTotal = api
                .column( 5, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
					if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 5 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 6
            pageTotal = api
                .column( 6, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 6 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 7
            pageTotal = api
                .column( 7, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
					if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 7 ).footer() ).html( // Update footer
                pageTotal
            );
            // Total over this page colomn no 8
            pageTotal = api
                .column( 8, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 8 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 9
            pageTotal = api
                .column( 9, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 9 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 10
            pageTotal = api
                .column( 10, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 10 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 11
            pageTotal = api
                .column( 11, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 11 ).footer() ).html( // Update footer
                pageTotal
            );
            // Total over this page colomn no 12
            pageTotal = api
                .column( 12, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 12 ).footer() ).html( // Update footer
                pageTotal
            );

            // Total over this page colomn no 13
            pageTotal = api
                .column( 13, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 13 ).footer() ).html( // Update footer
                pageTotal
            );
            // Total over this page colomn no 14
            pageTotal = api
                .column( 14, { page: 'current'} )
                .data()
                .reduce( function (a, b) {
                    if($('#mode').val() == "amount"){
                   	 return (intVal(a) + intVal(b)).toFixed(2);
					}else{
						return (intVal(a) + intVal(b)).toFixed(0);
					}
                }, 0 );
            $( api.column( 14 ).footer() ).html( // Update footer
                pageTotal
            );



        }
    	});
	}

	
});


function generatePostData() {
		var token = document.getElementsByName("token")[0].value;
		var merchants = document.getElementById("merchants").value;
		var _subMerchantEmailId = $("#subMerchant").val();
		var	dateFrom = document.getElementById("dateFrom").value;
		var	acquirer = document.getElementById('acquirer').value;
		var paymentMethod = document.getElementById("paymentMethod").value;
		var mode = document.getElementById("mode").value;
		var currency = document.querySelector("#currency").value;
		if(merchants==''){
			merchants='ALL'
		}
		if (subMerchant == '') {
            subMerchant = 'ALL'
        }
		if(paymentMethod==''){
			paymentMethod='ALL'
		}
		if(acquirer==''){
			acquirer = "ALL"
		}
		var obj = {
			merchant : merchants,
			subMerchantEmailId:  _subMerchantEmailId,
			refundRequestDate : dateFrom,
			paymentType : paymentMethod,
			acquirer : acquirer,
			mode:mode,
			currency: currency,
			token : token,
			"struts.token.name" : "token",
		}

		console.log(obj);
		return obj;
	}
</script>
</head>
<body id="mainBody">

	<section class="refund-summary lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Refund Summary Report Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
				   <s:select name="merchants" class="selectpicker"
				   id="merchants" data-live-search="true" headerKey="" headerValue="ALL"
				   list="merchantList" listKey="emailId"
				   listValue="businessName"
				   autocomplete="off" />
				</s:if>
				<s:else>
					<s:select name="merchants" class="selectpicker"
				   id="merchants" headerKey="" headerValue="ALL"
				   list="merchantList" listKey="emailId"
				   listValue="businessName"
				   autocomplete="off" />
				</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select
							data-id="subMerchant"
							name="subMerchant"
							class="selectpicker"
							id="subMerchant"
							list="subMerchantList"
							listKey="emailId"
							data-live-search="true"
							listValue="businessName"
							autocomplete="off"
						/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:if>
			<s:else>
				<div class="col-md-3 d-none mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchant" id="subMerchant"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>

			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date</label>
				<s:textfield type="text" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Acquirer</label>
				   <s:select  name="acquirer" data-live-search="true" class="selectpicker" id="acquirer" headerKey="" headerValue="All"
					list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
					listKey="code" listValue="name" autocomplete="off"/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Payment Type</label>
				   <s:select headerKey="" data-live-search="true" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.PaymentType@values()"
				   listValue="name" listKey="code" name="paymentMethod"
				   id="paymentMethod" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Currency</label>
					<s:select name="currency" data-download="currency" data-var="currency" id="currency" headerValue="ALL"
					 headerKey="" list="currencyMap" class="selectpicker" />
				 </div>
				 <!-- /.lpay_select_group --> 
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Mode</label>
				   <select class="selectpicker" id="mode">
					<option value="amount">Amount</option>
					<option value="count">Count</option>
				</select>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<input type="button" id="submitBtn" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="refund-summary lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Refund Summary Report Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="myTable" class="display" style="width:100%">
						<thead align="centre" class="lpay_table_head">
							<tr>
								<th>Acquirer</th>
								<th style='text-align: center'>Payment Type</th>
								<th style='text-align: center'>Mop</th>
								<th style='text-align: center'>Txn Initiated</th>
								<th style='text-align: center'>Captured</th>
								<th style='text-align: center'>Declined</th>
								<th style='text-align: center'>Rejected</th>
								<th style='text-align: center'>Pending</th>
								<th style='text-align: center'>Error</th>
								<th style='text-align: center'>Timeout</th>
								<th style='text-align: center'>Failed</th>
								<th style='text-align: center'>Invalid</th>
								<th style='text-align: center'>Acquirer down</th>
								<th style='text-align: center' >Failed At Acquirer</th>
								<th style='text-align: center' >Acquirer Timeout</th>
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
 
</body>
</html>