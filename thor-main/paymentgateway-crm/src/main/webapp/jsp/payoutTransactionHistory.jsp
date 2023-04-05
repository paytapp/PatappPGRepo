<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

		
.heading{
   text-align: center;
    color: #002163;
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
    width: 38% !important;
    float: left;
    font: bold 13px arial !important;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 0px !important;
}
.submit-button{
	width:10% !important;
	height:28px !important;
	margin-top:-4px !important;
}
.MerchBx {
    min-width: 92%;
    margin: 15px;
    margin-top: 25px !important;
    padding: 0;
}

table.dataTable thead .sorting {
    background: none !important;
}
.sorting_asc {
    background:none !important;
}
table.dataTable thead .sorting_desc {
    background: none !important;
}
table.dataTable thead .sorting {
     cursor: default !important;
}
table.dataTable thead .sorting_desc, table.dataTable thead .sorting {
    cursor: default !important;
}
table.dataTable.display tbody tr.odd {
    background-color: #e6e6ff !important;
}
table.dataTable.display tbody tr.odd > .sorting_1{
	 background-color: #e6e6ff !important;
}		

</style>

<title>Payout Transaction</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/loader.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/moment.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<link href="../css/loader.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.popupoverlay.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<script src="../js/messi.js"></script>
	<link href="../css/messi.css" rel="stylesheet" />
	<script src="../js/commanValidate.js"></script>
	


<script type="text/javascript">

$(function() {
	
	var table = $('#searchTransactionDatatable').DataTable({
		
		dom: 'Bfrtip',
	               destroy : true,
	               buttons : [
									{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'csvHtml5',
										title : 'Search Transaction Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										title : 'Search Transaction Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'print',
										orientation : 'landscape',
										title : 'Search Transaction Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
									}
								],
				"searching": false,
	});
	
    $('#searchButton').on('click', function() {
		 var orderId = document.getElementById("orderid").value;
		 var pgRefId = document.getElementById("pgref").value;
		 
		 
		 var token  = document.getElementsByName("token")[0].value;
		 
		 //table.destroy();
         //$('#gstReportDatatable').empty();
		 
		 
        var table = $('#searchTransactionDatatable').DataTable({
			dom: 'Bfrtip',
	               buttons : [
									{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'csvHtml5',
										title : 'Search Transaction Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										title : 'Search Transaction Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'print',
										orientation : 'landscape',
										title : 'Search Transaction Report',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
									}
								],
			"searching": false,
			"destroy": true,
			"ajax": {
			    "url": "searchTransactionAction",
			    "type": "POST",
			    "data": {
					"orderId":orderId,
				    "pgRefId":pgRefId,
					"struts.token.name": "token",
					},						
			  },
				  "columns": [
				        { "data": "txnId" },
					    { "data": "pgRefNo" },
						{ "data": "merchant" },
			            { "data": "orderId" },
			            { "data": "date" },
			            { "data": "paymentMethod" },
			            { "data": "txnType" },
						{ "data": "cardNumber" },
			            { "data": "status" },
			            { "data": "amount" }
						
			        ]

        });
    });
});

</script>
<script>
function checkRefNo(){
	var refValue = document.getElementById("pgref").value;
	var refNoLength = refValue.length;
	if((refNoLength <16) && (refNoLength >0)){
		document.getElementById("searchButton").disabled = true;
		document.getElementById("searchButton").style.backgroundColor = "#b3e6b3";
		document.getElementById("validRefNo").style.display = "block";
	}
	else if(refNoLength == 0){
		document.getElementById("searchButton").disabled = false;
		document.getElementById("searchButton").style.backgroundColor = "#39ac39";
		document.getElementById("validRefNo").style.display = "none";
	}else{
		document.getElementById("searchButton").disabled = false;
		document.getElementById("searchButton").style.backgroundColor = "#39ac39";
        document.getElementById("validRefNo").style.display = "none";
	}
}
</script>
	
</head>
<body id="mainBody">

	<h2 class="heading" style="margin-bottom: -30px !important;">Search Transaction</h2>
	<br>
	<br>
	 <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
           
        </tr>
 
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    

     <table id="mainTable" width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="txnf">
		
		    <tr>
			    <td colspan="5" align="left" valign="top">
				<div class="MerchBx">
					
					<div class="cust">
						<div style="margin-left: 8px;">Order ID:</div>
						<div class="txtnew">
							<input type="text" id="orderid" value="" class="form-control">
								</input>
						</div>
					</div>
					
					<div class="cust">
						<div style="margin-left:8px;">PG Ref ID:</div>
						<div class="txtnew">
							<input type="text" id="pgref" value="" class="form-control" onblur="checkRefNo()" autocomplete="off"
								onkeypress="javascript:return isNumber (event)" maxlength="16">
							</input>
						</div>
						<span id="validRefNo" style="color:red; display:none; margin-left:5px;">Please Enter 16 Digit PG Ref No.</span>

					</div>

					<div class="samefnew-btn">
						&nbsp;<br>
						<div class="txtnew">
							<input type="button" id="searchButton" value="Submit"
								class="btn btn-sm btn-block btn-success submit-button" style="margin-left: 15px;
                                    width: 22% !important; font-size:14px;">
								</input>
						</div>
					</div>

					
				</div>
			</td>
		</tr>
		
		 <tr>
						<td align="left" valign="top" style="padding: 10px; border-right: 1px solid #e0d2d2c4;"><br>
							<div class="scrollD">
								<table id="searchTransactionDatatable" align="center" class="display" cellspacing="0" width="100%">
									<thead>
										<tr class="boxheadingsmall" style="font-size: 11px;">
										    <th style="text-align:left;" data-orderable="false">Txn ID</th>
											<th style="text-align:left;" data-orderable="false">PG Ref No</th>
											<th style="text-align:left;" data-orderable="false">Merchant</th>
											<th style="text-align:left;" data-orderable="false">Order ID</th>
											<th style="text-align:left;" data-orderable="false">Date</th>
											<th style="text-align:left;" data-orderable="false">Payment Method</th>
											<th style="text-align:left;" data-orderable="false">Txn Type</th>
											<th style="text-align:left;" data-orderable="false">Card Number</th>
											<th style="text-align:left;" data-orderable="false">Status</th>
											<th style="text-align:left;" data-orderable="false">Amount</th>
										</tr>
									</thead>
								
								</table>
							</div>
						</td>
					</tr>
		
		
	</table>
                
     
        </td>
    </table>
	

</body>
</html>