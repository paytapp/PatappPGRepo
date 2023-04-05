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
    font-size:12px;
	font-weight:600;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 10px;
}
.cust {
	width: auto;
    font-size:12px;
    font-weight:600;
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
#loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}
#loading-image {position: absolute;top: 40%;left: 55%;z-index: 100; width:10%;}	

#mainTable{
	table-layout: fixed;
}

.MerchBx{
	margin: 0 !important;
	margin-top: 20px !important;
}

.primary-btn{
	height: 43px;
	margin-top: 0px !important;
	padding: 4px 31px 6px !important;
}


</style>

<title>Transaction Search</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<!-- <script src="../js/jquery-latest.min.js" type="text/javascript"></script> -->
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js" type="text/javascript"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<script src="../js/commanValidate.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">

<script type="text/javascript">
function dateToolTip(){
            $("body").removeClass("loader--inactive");
            $("td.transactionDate").each(function(e){
                var _getDate = $(this).text();
				if(_getDate != ""){
					var _getSpace = _getDate.indexOf(" ");
					var _getTime = _getDate.substring(_getSpace);
					var _getOnlyDate = _getDate.substring(0, _getSpace);
					$(this).text(_getOnlyDate);
					$(this).append("<div class='timeTip'>"+_getTime+"</div>");
				}
            })
            setTimeout(function(e){
                $("body").addClass("loader--inactive");
            }, 500);
        }

$(function() {

	function tableLoad(){
		var reservationId = document.getElementById("reservationId").value;
		var bankTxnId = document.getElementById("bankTxnId").value;
		var table = $('#searchTransactionDatatable').DataTable({
			"columnDefs": [
				        {
							"className": "dt-center", 
						"targets": "_all"
						},
						{
							type: 'tDate',
							'targets' : [5]
						}
				],
            order: [[ 5, 'desc' ]],
			
			dom: 'BTrftlpi',
	               buttons : [
									{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'csvHtml5',
										title : 'Search_Transaction',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										title : 'Search_Transaction',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'print',
										orientation : 'landscape',
										title : 'Search_Transaction',
										exportOptions : {
											columns : [':visible']
										}
									},
									{
										extend : 'colvis',
										columns : [0, 1, 2, 3, 4, 5, 6, 7, 8]
									}
								],
			"searching": false,
			"destroy": true,
            "paging": true,
            "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
            "pagingType": "full_numbers",
            "pageLength": 10,
			ajax: function (data, callback, settings) {
         $.ajax({				
				    "url": "agentReconSearchAction",
				    "type": "POST",
				    "timeout": 0,
				    "data": {
						"reservationId":reservationId,
						"bankTxnId":bankTxnId,
						"struts.token.name": "token",
						},
					
					    success:function(data){
								// document.getElementById("loading").style.display = "none";
								$("body").addClass("loader--inactive");
                            //    callback(data);
                            },
					    error:function(data) {
								// document.getElementById("loading").style.display = "none";
								$("body").addClass("loader--inactive");
					        }
		        });
				   
				  },

				  "fnDrawCallback" : function() {
								 
							},

				  "columnDefs" : [ {
								"type" : "html-num-fmt",
								"targets" : 4,
								"orderable" : true,
								"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8]
							},
							
							
						],



				  "columns": [
				        { "data": "reservationId" },
					    { "data": "bankTxnId" },
						{ "data": "acquirer" },
						{ "data": "amount" },
			            { "data": "createDate"},
						{ "data": "settlementDate" },
						{ "data": "txnType" },
						{ "data": "settlementFlag" },
						{ "data": "postSettledFlag" },
						
			        ]

        });
	}

//	tableLoad();
	
    $('#searchButton').on('click', function() {
		 var reservationId = document.getElementById("reservationId").value;
		 var banktxnId = document.getElementById("bankTxnId").value;
		 if ((reservationId == "") && (banktxnId == "")){
			 alert("Please enter atleast one value !!")
			 return false;
		 }


		 var token  = document.getElementsByName("token")[0].value;
		//  document.getElementById("loading").style.display = "block";
		$("body").removeClass("loader--inactive");
		 //table.destroy();
         //$('#gstReportDatatable').empty();
		 
		tableLoad();
	 
		dateToolTip();
    });
});

</script>
</head>
<body id="mainBody">
	<input type="hidden" id="isSuperMerchant">
	<section class="agent-search lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Agent Search</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-6 mb-20">
				<div class="lpay_input_group">
					<label for="">Reservation Id</label>
					<input type="text" id="reservationId" value="" class="lpay_input"></input>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-6 mb-20 -->
			<div class="col-md-6 mb-20">
			  <div class="lpay_input_group">
				<label for="">Bank Txn Number / ID</label>
				<input type="text" id="bankTxnId" name="bankTxnId" value="" class="lpay_input"  autocomplete="off"></input>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 mb-20 -->
			<div class="col-md-12 text-center">
				<input type="button" id="searchButton" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary"></input>
			</div>
			<!-- /.col-md-12 -->
			
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="agent-search lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Agent Search Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="searchTransactionDatatable" align="center" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th  data-orderable="false">Reservation ID</th>
								<th  data-orderable="false">Bank Txn Id</th>
								<th  data-orderable="false">Acquirer</th>
								<th  data-orderable="false">Amount</th>
								<th  data-orderable="false" nowrap>Txn Date</th>
								<th  data-orderable="false" nowrap>Settlement Date</th>
								<th  data-orderable="false">TXN Type</th>
								<th  data-orderable="false">Settlement Flag</th>
								<th  data-orderable="false">Post Settled</th>
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