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

.ag-update_wrapper{ width: 100%;height: 100%;position: fixed;top: 0;left: 0;background-color: rgba(0,0,0,.8);z-index: 999;display: none; }
.ag-update_container{ width: 100%;height: 100%;display: flex;align-items: center;justify-content: center; }
.ag-update_wrapper input:read-only{ background-color: #ddd;border: 1px solid #ccc }
.ag-update_inner{ width: 100%;max-width: 991px;background-color: #fff;padding: 20px;border-radius: 10px; }
.ag-update_data{ max-height: 400px;overflow-y: scroll; }
.ag-update_data::-webkit-scrollbar { width: 5px; }
.ag-update_data::-webkit-scrollbar-thumb { background: #002663;border-radius: 20px; }
.ag-update_data::-webkit-scrollbar-track { background: transparent;border-radius: 20px; }
.agent-search_table .heading_with_icon .heading_text{ font-size: 18px !important;font-weight: 500;margin-left: 0 !important;padding-bottom: 0; }


</style>

<title>DB Update</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<!-- <script src="../js/jquery-latest.min.js" type="text/javascript"></script> -->
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js" type="text/javascript"></script>
	<!-- <script type="text/javascript" src="../js/dataTables.buttons.js"></script> -->
	<!-- <script type="text/javascript" src="../js/pdfmake.js"></script> -->
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/commanValidate.js"></script>
    <script src="../js/bootstrap-select.min.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">

<script type="text/javascript">

	


	$(function() {

		function hideColumn(_id){
			var _getSuperMerchant = $("#isSuperMerchant").val();
			var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
			var _tables = ['paymentGatewayTransactionDatatable', 'transactionStatusDatatable'];
			var td = $("#"+_id).DataTable();
			if(userType == "MERCHANT"|| userType == "SUPERMERCHANT" || userType == "SUBMERCHANT" || userType == "RESELLER"){
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
	
			if(_getSuperMerchant == "" || _getSuperMerchant == "NA"){
				td.columns(4).visible(false);
			}else{
				td.columns(4).visible(true);
			}
		}
	

		function tableLoad(){
			var orderId = document.getElementById("orderid").value;
			var pgRefId = document.getElementById("pgref").value;
			var _rrn = document.querySelector("#rrn").value;
			var _mobileNumber = document.querySelector("#mobileNumber").value;
			var _dataTables = ['paymentGatewayTransactionDatatable','transactionStatusDatatable'];
			var _aaData = "";
			$("body").removeClass("loader--inactive");
			for(var i = 0; i < _dataTables.length; i++){

				if(_dataTables[i] == "paymentGatewayTransactionDatatable"){
					_aaData = "aaData";
				}else{
					_aaData = "aaDataStatus";
				}
				
				var table = $('#'+_dataTables[i]).DataTable({
					"ajax" : {
						"url": "agentSearchAction",
						"type": "POST",
						"timeout": 0,
						"data": {
							"orderId":orderId,
							"pgRefNum":pgRefId,
							"rrn" : _rrn,
							"mobileNumber" : _mobileNumber,
							"struts.token.name": "token",
						},
					},
					"searching": false,
					"destroy": true,
					"paging": true,
					"lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
					"pagingType": "full_numbers",
					"pageLength": 10,
					"fnDrawCallback" : function() {
						hideColumn(_dataTables[i]);
						setTimeout(function(e){

							$("body").addClass("loader--inactive");
						}, 500)
					},
					"columnDefs" : [ 
						{
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
					"sAjaxDataProp" : _aaData,
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
						{ 
							"data": null,
							"mRender" : function(row){
								return '<button class="lpay_button lpay_button-md lpay_button-secondary updateBtn">Edit</button>';
							}
						}
					]
	
				});
			}
		}

//	tableLoad();
	
		$('#searchButton').on('click', function() {
			var orderId = document.getElementById("orderid").value;
			var pgRefId = document.getElementById("pgref").value;
			var _rrn = document.querySelector("#rrn").value;
			var _mobileNumber = document.querySelector("#mobileNumber").value;
			if ((orderId == "") && (pgRefId == "") && (_rrn == "") && (_mobileNumber == "")){
				alert("Please enter atleast one value !!")
				return false;
			}
			var token  = document.getElementsByName("token")[0].value;
			
			tableLoad();
		});

		$(".confirmButton").on("click", function(e){
			tableLoad();
			$(".lpay_popup").fadeOut();
		})

		// tableLoad();
		var _loadTables = ['paymentGatewayTransactionDatatable','transactionStatusDatatable'];
		for(var i = 0; i < _loadTables.length; i++){
			$("#"+_loadTables[i]).DataTable();
		}
	
		var _closestTable = "";

		$("body").on("click", ".updateBtn", function(e){
			var _id = $(this).closest("table").attr("id");
			var _table = new $.fn.dataTable.Api('#'+_id);
			document.querySelector("body").classList.remove("loader--inactive");
			document.querySelector(".ag-update_dynamic").innerHTML = "";
			var _activeTr = $(this).closest("tr");
			_closestTable = $(this).closest("table").attr("id");
			var _fieldLock = ['txnType'];
			var _data = _table.rows(_activeTr).data();
			var _class = "";
			sendActionForMop(_data[0]['paymentType']);

			setTimeout(function(e){
				for(key in _editData){
					if(key == "amount" || key == "totalAmount" || key == "totalTdrSc" || key == "totalGst" || key == "PG_TDR_SC" || key == "PG_GST" || key == "ACQUIRER_TDR_SC" || key == "ACQUIRER_GST" || key == "resellerCharges" || key == "resellerGst"){
						_class = "numberValid";
					}else{
						_class = "";
					}
					if(key == "txnSettledType" || key == "card_Holder_Type" || key == "acquirerMode" || key == "paymentType" || key == "mopType" || key == "status" || key == "payment_Region" || key == "acquirerType"){
						$("#"+key).val(_data[0][key]);
						$("#"+key).selectpicker('refresh');
					}else{
						var _input = "<div class='col-md-3 mb-20'><div class='lpay_input_group'>"
						_input += "<label>"+_editData[key]+"</label><input data-var='"+key+"' value='"+_data[0][key]+"' class='lpay_input "+_class+"' />";
						_input += "</div></div>";
						document.querySelector(".ag-update_dynamic").innerHTML += _input;
					}
				}
				for(var i = 0; i < _fieldLock.length; i++){
					var _readonly = document.createAttribute("readonly");
					document.querySelector("[data-var='"+_fieldLock[i]+"']").setAttributeNode(_readonly);
				}
				document.querySelector("body").classList.add("loader--inactive");
				$(".ag-update_wrapper").fadeIn();
			}, 1000)
		})

		$("body").on("input", ".numberValid", function(e){
			var _val = $(this).val();
			if(_val.length == 1) {
				if(_val.indexOf(".") != -1) {
					$(this).val(_val.slice(0, _val.length - 1));
				}
			}

			var regex = /[.]/g;
			var _getPeriod = _val.match(regex);
			if(_getPeriod != null){
				if(_getPeriod.length > 1){
					$(this).val(_val.slice(0, _val.length-1));
				}
				var _getString = _val.slice(_val.indexOf("."));
				if(_getString.length > 3){
					$(this).val(_val.slice(0, _val.length-1));
				}
			}
		})

		$("body").on("keyup", ".numberValid", function(e){
			var x = e.keyCode;
			console.log(x);
			if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46 || x == 190) {
			} else {
				// return false;
				$(this).val(e.target.value.slice(0, e.target.value.length-1))
				e.preventDefault();
			}
		})
		
	

		function sendActionForMop(_data){
			$.ajax({
				type: "POST",
				url: "getSufDetailMopTypeAction",
				data: {
					paymentType : _data
				},
				success: function(data){
					fetchMopList(data.mopList); 
				}
			})
		}

		$("#paymentType").on("change", function(e){
			var _paymentType = $(this).val();
			sendActionForMop(_paymentType);
		})

		function fetchMopList(mopList) {
			var mopListStr = "";
			if(mopList !== undefined) {
				if(mopList.length > 0) {
					mopList.forEach(function(mopType) {
						mopListStr += '<option value="'+mopType+'">'+mopType+'</option>';
						// mopListStr += '<li class="col-md-2"><label for="moptype-'+ mopType +'" class="checkbox-label unchecked"><input type="checkbox" name="mopType" id="moptype-'+ mopType +'" class="mr-5" value="'+ mopType +'" /> '+ mopType +'</label></li>'
					});
				}
			}
			$("#mopType").html("");
			$(mopListStr).appendTo("#mopType");
			$("#mopType").selectpicker('refresh');
		}

		$("#update-cancel").on("click", function(e){
			$(".ag-update_wrapper").fadeOut();
		});



		$("#update-confirm").on("click", function(e){
			var _inputs = document.querySelectorAll(".ag-update_wrapper [data-var]");
			var _obj = {};
			_inputs.forEach(function(index, element, array){
				_obj[index.getAttribute("data-var")] = index.value;
			})
			if(_closestTable == "paymentGatewayTransactionDatatable"){
				_obj['dbUpdate'] = "txn";
			}else{
				_obj['dbUpdate'] = "status";
			}
			$.ajax({
				type: "POST",
				url: "agentUpdateAction",
				data: _obj,
				success: function(data){
					if(data.response == "SUCCESS"){
						$(".lpay_popup-innerbox").attr("data-status", "success");
					}else{
						$(".lpay_popup-innerbox").attr("data-status", "error");
					}
					$(".ag-update_wrapper").fadeOut();
					$(".lpay_popup").fadeIn();
				},
				error: function(data){

				}
			})
			$(".ag-update_wrapper").fadeOut();
		})

		var _editData = {
			"txnId" : "TXN ID",
			"pgRefNum" : "PG Ref No",
			"orderId" : "Order ID",
			"refund_txn_id" : "Refund Order ID",
			"tDate": "Date",
			"txnType" : "TXN Type",
			"txnSettledType" : "Transaction Flag",
			"status" : "Status",
			"acquirerType" : "Acquirer Name",
			"acquirerMode" : "Acquirer Mode",
			"paymentType" : "Payment Type",
			"mopType" : "MOP",
			"payment_Region" : "Payment Region",
			"card_Holder_Type": "Card Holder Type",
			"cardNum": "Card Number",
			"custName" : "Customer Name",
			"amount" : "Amount",
			"totalAmount" : "Total Amount",
			"totalTdrSc" : "Total TDR SC",
			"totalGst" : "Total GST",
			"PG_TDR_SC" : "PG TDR SC",
			"PG_GST" : "PG GST",
			"ACQUIRER_TDR_SC" : "Acquirer TDR SC",
			"ACQUIRER_GST" : "Acquirer GST",
			"resellerCharges" : "Reseller TDR SC",
			"resellerGst" : "Reseller GST",
			"pgResponseMessage" : "Payment Gateway Response MSG",
			"rrn" : "RRN",
			"acqId" : "ACQ ID",
			"acquirerTxnMessage" : "Acquirer Response MSG"

		}
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
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Order Id</label>
					<input type="text" data-var="orderId" id="orderid" class="lpay_input"></input>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-6 mb-20 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">PG REF Number</label>
				<input type="text" id="pgref" data-var="pgRefNum" value="" class="lpay_input" onblur="checkRefNo()" autocomplete="off"
				onkeypress="javascript:return isNumber (event)" maxlength="16"></input>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">RRN Number</label>
					<input type="text" data-var="rrn" id="rrn" value="" class="lpay_input"></input>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-6 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">Mobile Number</label>
					<input type="text" data-var='mobileNumber' id="mobileNumber" value="" class="lpay_input"></input>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-6 mb-20 -->
			<div class="col-md-12 text-center">
				<input type="button" id="searchButton" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary"></input>
				<span id="downloadAgentSearch" onclick="downloadAgentSearch()" class="lpay_button lpay_button-md lpay_button-primary">Download</span>
			</div>
			<!-- /.col-md-12 -->
			
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="agent-search agent-search_table lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<h2 class="heading_text">Payment Gateway Transaction Collection</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12" style="margin-bottom: 50px;">
				<div class="lpay_table">
					<table id="paymentGatewayTransactionDatatable" style="white-space: nowrap;" align="center" class="display" cellspacing="0" width="100%">
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
                                <th  data-orderable="false" >Action</th>
							</tr>
						</thead>
					</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<h2 class="heading_text">Transaction Status Collection</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="transactionStatusDatatable" style="white-space: nowrap;" align="center" class="display" cellspacing="0" width="100%">
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
								<th  data-orderable="false" >Action</th>
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

    <div class="ag-update_wrapper">
        <div class="ag-update_container">
            <div class="ag-update_inner">
                <div class="ag-update_heading">
                    <div class="heading_with_icon mb-30">
                        <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Edit Mode Agent Data</h2>
                    </div>
                    <!-- /.heading_icon -->
                </div>
                <!-- /.ag-update_heading -->
                <div class="ag-update_data row">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Select Acquiring Mode</label>
                            <select name="acquirerMode" data-var='acquirerMode' id="acquirerMode" class="selectpicker selctList">
                                <option value="">Select Acquiring mode</option>
                                <option value="ON_US">On Us</option>
                                <option value="OFF_US">Off Us</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Select Cardholder Type</label>
                            <select name="cardholderType" data-var='card_Holder_Type' id="card_Holder_Type" class="selectpicker selctList">
                                <option value="CONSUMER">Consumer</option>
                                <option value="COMMERCIAL">Commercial</option>
                                <option value="PREMIUM">Premium</option>
                            </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Payment Method</label>
                           <s:select data-live-search="true" data-var='paymentType' data-download="paymentType" class="selectpicker"
                           list="@com.paymentgateway.commons.util.PaymentType@values()"
                           listValue="name" listKey="code" name="paymentType"
                           id="paymentType" autocomplete="off" value="" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
					<div class="col-md-3 mb-20" data-id="mopType">
						<div class="lpay_select_group">
							<label for="">Mop Type</label>
							<select name="mopType" data-var='mopType' id="mopType" data-actions-box="true"></select>
						</div>
						<!-- /.Payment Gateway_select_group -->
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
						   <label for="">Payment Region</label>
						   <select name="paymentRegion" data-var='paymentRegion' id="paymentRegion" class="selectpicker">
							   
							   <option value="Domestic">Domestic</option>
							   <option value="International">International</option>
						   </select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="acquirer">Acquirer</label>
							<div class="position-relative">
								<s:select
									name="acquirer"
									class="selectpicker"
									data-var="acquirerType"
									id="acquirerType"
									data-live-search="true"
									list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
									listKey="code"
									listValue="name"								
									autocomplete="off"
								/>
							</div>
							<!-- /.position-relative -->
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-4 -->
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Transaction Flag</label>
                           <select class="selectpicker" data-actions-box="true" data-var='txnSettledType' data-download="transactionFlag" name="transactionFlag" id="txnSettledType">
                               <!--<option value="ALL" selected>ALL</option> -->
                               <option value="Real-Time">Real Time</option>
                               <option value="Post Captured">Post Captured</option>
                               <option value="TXN Enquiry">TXN Enquiry</option>
                           </select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->
					<div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                           <label for="">Status</label>
                           <s:select data-live-search="true" data-download="status" data-var="status" class="selectpicker"
                           list="@com.paymentgateway.commons.util.StatusType@values()"
                           listValue="name" listKey="name" name="status"
                           id="status" autocomplete="off" value="" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 mb-20 -->
					<div class="ag-update_dynamic" style="width: 100%">

					</div>
					<!-- /.ag-update_dynamic -->
                </div>
                <!-- /.ag-update_data -->
                <div class="ag-update_button text-center" style="padding-top: 20px">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="update-cancel">Cancel</button>
                    <button class="lpay_button lpay_button-md lpay_button-primary" id='update-confirm'>Update</button>
                </div>
                <!-- /.ag-update_button -->
            </div>
            <!-- /.ag-update_inner -->
        </div>
        <!-- /.ag-update_container -->
    </div>
    <!-- /.ag-update_wrapper -->

    <div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">

                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Data has been updated successfully.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->

                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Something went wrong. Please try again</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->


</body>
</html>