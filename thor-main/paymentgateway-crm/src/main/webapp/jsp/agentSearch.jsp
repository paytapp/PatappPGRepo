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

.agent-search_inputs_div{ display: flex;align-items: center;justify-content: space-between;width: 100%;margin-bottom: 20px;padding: 0 15px }

</style>

<title>Agent Search</title>
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
function hideColumn(){
			var _getSuperMerchant = $("#isSuperMerchant").val();
			var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
			console.log(userType);
			var td = $("#searchTransactionDatatable").DataTable();
			if(userType == "MERCHANT"|| userType == "SUPERMERCHANT" || userType == "SUBMERCHANT" || userType == "RESELLER"){
				//td.columns(02).visible(false);
				//td.columns(08).visible(false);
				td.columns(20).visible(false);
				td.columns(21).visible(false);
				td.columns(22).visible(false);
				td.columns(23).visible(false);
				td.columns(24).visible(false);
				td.columns(25).visible(false);
				td.columns(26).visible(false);
				td.columns(27).visible(false);
				td.columns(29).visible(false);
				td.columns(30).visible(false);
			}

			if(userType == "MERCHANT"){
				td.columns(11).visible(false);
			}

			if(_getSuperMerchant == "" || _getSuperMerchant == "NA"){
				td.columns(4).visible(false);
			}else{
				td.columns(4).visible(true);
			}
		}
$(window).on("load", function(e){
		hideColumn();
	});

	
	


$(function() {
	

	function tableLoad(){
		var orderId = document.getElementById("orderid").value;
		 var pgRefId = document.getElementById("pgref").value;
		 var _rrn = document.querySelector("#rrn").value;
		 var _consumerNumber = document.querySelector("#consumerNumber").value;
		 var _acqId = document.querySelector("#acqId").value;
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
										columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16]
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
				    "url": "agentSearchAction",
				    "type": "POST",
				    "timeout": 0,
				    "data": {
						"orderId":orderId,
						"pgRefNum":pgRefId,
						"rrn" : _rrn,
						"consumerNumber" : _consumerNumber,
						"acqId" :_acqId,
						"struts.token.name": "token",
						},
					
					    success:function(data){
								// document.getElementById("loading").style.display = "none";
								$("body").addClass("loader--inactive");
                                callback(data);
                            },
					    error:function(data) {
								// document.getElementById("loading").style.display = "none";
								$("body").addClass("loader--inactive");
					        }
		        });
				   
				  },

				  "fnDrawCallback" : function() {
								hideColumn();
							},

				  "columnDefs" : [ {
								"type" : "html-num-fmt",
								"targets" : 4,
								"orderable" : true,
								"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
							},
							
							{
								'targets': 0,
								'createdCell':  function (td, cellData, rowData, row, col) {
									$("#isSuperMerchant").val(rowData["subMerchantId"]);
								}
							}
						],

					"order" : [ [ 8, "desc" ] ],

				  "columns": [
					  	{ "data": "payId" },
				        { "data": "transactionId" },
					    { "data": "pgRefNum" },
						{ "data": "merchant" },
						{ "data": "subMerchantId" },
			            { "data": "orderId"},
						{ "data": "refund_txn_id" },
			            { "data": "tDate",
                          "width": "10%",
						  "class": "transactionDate"
						},
						{ "data": "txnType" },
						{ "data": "txnSettledType"},
						{ "data": "status" },
						{ "data": "acquirerType"},
						{ "data": "acquirerMode" },
						{ "data": "paymentType" },
						{ "data": "mopType" },
						{"data":"payment_Region"},
						{"data":"card_Holder_Type"},
			            { "data": "cardNum" },
			            { "data": "custName" },
						{ "data": "amount" },
						{ "data": "totalAmount" },
						{ 
                            "data": null,
                            "mRender" : function(row){
                                var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
								if(userType == "ADMIN" || userType == "SUBADMIN"){
									return row.totalChargeTdrSc
								}else{
									return row.totalTdrSc
								}
                            }
                            
                         },
						{ "data": "totalGst" },
						{ "data": "PG_TDR_SC" },
						{ "data": "PG_GST" },
						{ "data": "ACQUIRER_TDR_SC" },
						{ "data": "ACQUIRER_GST" },
						{ "data": "resellerCharges" },
						{ "data": "resellerGst" },
						{ "data": "pgResponseMessage" },
						{ "data": "responseCode" },
					    { "data": "rrn" },
						{ "data": "acqId" },
						{ "data": "acquirerTxnMessage" },
						{ "data": "consumerNo" },
						{ "data": "udf10" }
			        ]

        });
	}

//	tableLoad();
	
    $('#searchButton').on('click', function() {
		 var orderId = document.getElementById("orderid").value;
		 var pgRefId = document.getElementById("pgref").value;
		 var _rrn = document.querySelector("#rrn").value;
		 var _consumerNumber = document.querySelector("#consumerNumber").value;
		 var _acqId  = document.querySelector("#acqId").value;
		 //console.log(_mobileNumber);
		 if ((orderId == "") && (pgRefId == "") && (_rrn == "") && (_consumerNumber == "") && (_acqId == "") ){
			 alert("Please enter atleast one value !!")
			 return false;
		 }


		 var token  = document.getElementsByName("token")[0].value;
		//  document.getElementById("loading").style.display = "block";
		$("body").removeClass("loader--inactive");
		 //table.destroy();
         //$('#gstReportDatatable').empty();
		 
		tableLoad();
		hideColumn();
		dateToolTip();
    });

	
});
function downloadAgentSearch(){
	var _allInput = document.querySelectorAll("[data-var]");
	document.querySelector("#agentSearchDownload").innerHTML = "";
	_allInput.forEach(function(index, element, array){
		// console.log(index.value);
		var _eachInput = "<input type='hidden' name='"+index.getAttribute('data-var')+"' value='"+index.value+"' />";
		document.querySelector("#agentSearchDownload").innerHTML += _eachInput;
	})
	
	document.querySelector("#agentSearchDownload").submit();
}

</script>
<script>

	// console.log("hi");

	$(document).ready(function(e){
		var _tableId;
	var _table = document.querySelectorAll(".lpay_table");
	console.log(_table);
	if(_table.length > 0){
		var _html = '<button class="arrow arrow-left"><i class="fa fa-angle-left" aria-hidden="true"></i></button><button class="arrow arrow-right"><i class="fa fa-angle-right" aria-hidden="true"></i></button>';
		for(var i = 0; i < _table.length; i++){
			_tableId = _table[i].children[0].id;
			if(_tableId.indexOf("_") == -1){
				_tableId += "_wrapper";
			}
			_table[i].classList.add("has-scroll");
			var _createDiv = document.createElement("div");
			var _divClass = document.createAttribute("class");
			_divClass.value = "table_arrow";
			_createDiv.setAttributeNode(_divClass);
			_table[i].appendChild(_createDiv);
			_table[i].querySelector(".table_arrow").innerHTML = _html;
		}
	}
	if(_table.length > 0){
		function move(e, _currentTableId){
			var _getButton = e.target.classList[1];
			var _count = 0;
			var _id = setInterval(moveScroll, 10);
			function moveScroll(){
				if(_count == 20){
					clearInterval(_id);
					_count = 0;
				}else{
					_count++;
					if(_getButton == "arrow-right"){
						document.getElementById(_currentTableId).scrollLeft += _count;
					}else{
						document.getElementById(_currentTableId).scrollLeft -= _count;

					}
				}
			}
		}
		var _getAllArrow = document.querySelectorAll(".arrow");
		_getAllArrow.forEach(function(index, element, array){
			_getAllArrow[element].addEventListener('click', function(e){
				var _currentTableId = e.target.closest(".lpay_table").children[0].id;
				move(e, _currentTableId);
			})
		})
	}
	})

window.onload = function() { 
	
}

function checkRefNo(){
	var refValue = document.getElementById("pgref").value;
	var refNoLength = refValue.length;
	if((refNoLength <16) && (refNoLength >0)){
		document.getElementById("searchButton").disabled = true;
		document.getElementById("validRefNo").style.display = "block";
	}
	else if(refNoLength == 0){
		document.getElementById("searchButton").disabled = false;
		document.getElementById("validRefNo").style.display = "none";
	}else{
		document.getElementById("searchButton").disabled = false;
        document.getElementById("validRefNo").style.display = "none";
	}
}
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
			<div class="agent-search_inputs_div">
				<div class="agent_input_box">
					<div class="lpay_input_group">
						<label for="">Order Id</label>
						<input type="text" data-var="orderId" id="orderid" value="" class="lpay_input"></input>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 mb-20 -->
				<div class="agent_input_box">
				  <div class="lpay_input_group">
					<label for="">PG REF Number</label>
					<input type="text" id="pgref" data-var="pgRefNum" value="" class="lpay_input" onblur="checkRefNo()" autocomplete="off"
					onkeypress="javascript:return isNumber (event)" maxlength="16"></input>
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 mb-20 -->
				<div class="agent_input_box">
					<div class="lpay_input_group">
						<label for="">RRN Number</label>
						<input type="text" data-var="rrn" id="rrn" value="" class="lpay_input"></input>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 mb-20 -->
				<div class="agent_input_box">
					<div class="lpay_input_group">
						<label for="">Consumer Number</label>
						<input type="text" data-var='consumerNumber' id="consumerNumber" value="" class="lpay_input"></input>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 mb-20 -->
				<div class="agent_input_box">
					<div class="lpay_input_group">
						<label for="">ACQ ID</label>
						<input type="text" data-var='acqId' id="acqId" value="" class="lpay_input"></input>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 mb-20 -->
			</div>
			<!-- /.agent-search_inputs_div -->
			
			<div class="col-md-12 text-center">
				<input type="button" id="searchButton" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary"></input>
				<span id="downloadAgentSearch" onclick="downloadAgentSearch()" class="lpay_button lpay_button-md lpay_button-primary">Download</span>
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
					<table id="searchTransactionDatatable" style="white-space: nowrap;" align="center" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th  data-orderable="false">Pay Id</th>
								<th  data-orderable="false">TXN ID</th>
								<th  data-orderable="false">PG Ref No</th>
								<th  data-orderable="false">Merchant Name</th>
								<th  data-orderable="false">Sub Merchant Name</th>
								<th  data-orderable="false">Order ID</th>
								<th  data-orderable="false">Refund Order Id</th>
								<th  data-orderable="false" nowrap>Date</th>
								<th  data-orderable="false">TXN Type</th>
								<th  data-orderable="false">Transaction Flag</th>
								<th  data-orderable="false">Status</th>
								<th  data-orderable="false">Acquirer Name</th>
								<th  data-orderable="false">Acquiring Mode</th>
								<th  data-orderable="false">Payment Type</th>
								<th  data-orderable="false">MOP</th>
								<th  data-orderable="false">Payment Region</th>
								<th  data-orderable="false">Card Holder Type</th>
								<th  data-orderable="false">Card Number</th>
								<th  data-orderable="false">Customer Name</th>
								<th  data-orderable="false">Amount</th>
								<th  data-orderable="false">Total Amount</th>
								<th  data-orderable="false">Total TDR SC</th>
								<th  data-orderable="false">Total GST</th>
								<th  data-orderable="false">PG TDR SC</th>
								<th  data-orderable="false">PG GST</th>
								<th  data-orderable="false">Acquirer TDR SC</th>
								<th  data-orderable="false">Acquirer GST</th>
								<th  data-orderable="false">Reseller TDR SC</th>
								<th  data-orderable="false">Reseller GST</th>
								<th  data-orderable="false">Payment Gateway Response MSG</th>
								<th  data-orderable="false">Payment Gateway Response Code</th>
								<th  data-orderable="false">RRN</th>
								<th  data-orderable="false">ACQ ID</th>
								<th  data-orderable="false">Acquirer Response MSG</th>
								<th  data-orderable="false">Consumer No</th>
								<th  data-orderable="false">UDF10</th>
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
	<form id="agentSearchDownload" action="downloadAgentSearchAction">

	</form>

</body>
</html>