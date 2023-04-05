<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

		
.heading{
   text-align: center;
    color: black;
    font-weight: bold;
    font-size: 22px;
}
.samefnew {
    width: 15.5% !important;
    float: left;
    font: bold 13px arial !important;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 10px;
}
.cust {
    width: 20% !important;
    float: left;
    font: bold 13px arial !important;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 0px !important;
}

.MerchBx {
    min-width: 92%;
    margin: 15px;
    margin-top: 25px !important;
    padding: 0;
}
		

</style>

<title>Search Transaction</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/loader.css">
	<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<script src="../js/commanValidate.js"></script>
	


<script type="text/javascript">

$(function() {
	
	$('#searchTransactionDatatable').DataTable();
	
    $('#searchButton').on('click', function() {
		 var orderId = document.getElementById("orderId").value;
		 var pgRefId = document.getElementById("pgRefId").value;
		 
		 
		 var token  = document.getElementsByName("token")[0].value;
		 
        var table = $('#searchTransactionDatatable').DataTable({
			
			"searching": false,
			"info": false,
			"paging":   false,
			"destroy": true,

			
			"ajax": {
			    "url": "searchTransactionAgentAction",
			    "type": "POST",
			    "data": {
					"orderId":orderId,
				    "pgRefNum":pgRefId,
					"struts.token.name": "token",
					"token" : token,
					},						
			  },
				  "columns": [
				        { "data": "transactionId" },
					    { "data": "pgRefNum" },
						{ "data": "merchant" },
						{ "data": "custName" },
						{ "data": "cardMask" },
			            { "data": "orderId" },
			            { "data": "tDate" },
			            { "data": "paymentMethod" },
			            { "data": "txnType" },
						{ "data": "mopType" },
			            { "data": "status" },
			            { "data": "amount" }
						
			        ]

        });
    });
});

</script>

<script>
 $(function() {
            $(':text').on('input', function() {
                if( $(':text').filter(function() { return !!this.value; }).length > 0 ) {
                     $('#searchButton').prop('disabled', false);
                } else {
                     $('#searchButton').prop('disabled', true);
                }
            });
    });
</script>

	
</head>
<body id="mainBody">

	<section class="search-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Search Transaction</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-6 mb-20">
			  <div class="lpay_input_group">
				<label for="">Order ID</label>
				<input type="text" id="orderId" value="" class="lpay_input"></input>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-6 mb-20">
			  <div class="lpay_input_group">
				<label for="">PG REF ID</label>
				<input type="text" id="pgRefId" value="" class="lpay_input" maxlength="16" oninput="this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');"></input>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<input type="button" id="searchButton" value="Submit"
				class="lpay_button lpay_button-md lpay_button-secondary submit-button" disabled="disabled">
				</input>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="search-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Search Transaction Table</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="searchTransactionDatatable" align="center" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th style="text-align:left;" data-orderable="false">Txn ID</th>
								<th style="text-align:left;" data-orderable="false">PG Ref No</th>
								<th style="text-align:left;" data-orderable="false">Merchant</th>
								<th style="text-align:left;" data-orderable="false">Customer Name</th>
								<th style="text-align:left;" data-orderable="false">Card Mask</th>
								<th style="text-align:left;" data-orderable="false">Order ID</th>
								<th style="text-align:left;" data-orderable="false">Date</th>
								<th style="text-align:left;" data-orderable="false">Payment Method</th>
								<th style="text-align:left;" data-orderable="false">Txn Type</th>
								<th style="text-align:left;" data-orderable="false">MOP</th>
								<th style="text-align:left;" data-orderable="false">Status</th>
								<th style="text-align:left;" data-orderable="false">Amount</th>
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
</body>
</html>