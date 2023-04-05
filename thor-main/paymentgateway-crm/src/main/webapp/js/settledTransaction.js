
function removeSpaces(string) {
    return string.split(' ').join('');
}

var allowAlphaNumericSpecial = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
}

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


$(document).ready(function() {

	document.querySelector("#merchant").addEventListener("change", function(e){
		getSubMerchant(e, "getSubMerchantList", {
			isSuperMerchant : true,
			subUser : true,
			retailMerchantFlag: true,
			glocal : true
		});
	});
	document.querySelector("#subMerchant").addEventListener("change", function(e){
		getSubMerchant(e, "vendorTypeSubUserListAction", {
			subUser : true
		});
	})

	if($("#gloc").val() == "true"){
		$("[data-id=deliveryStatus]").removeClass("d-none");
		$("[data-id=deliveryStatus] select").selectpicker();
		$("[data-id=deliveryStatus] select").selectpicker('val', "All");
	}

	// var _select = "<option value='ALL'>ALL</option>"
	// $("[data-id=subMerchant]").find('option:eq(0)').before(_select);
	// $("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");

	renderTable();

	$("#submit").click(function(env) {
		$("body").removeClass("loader--inactive");
		$("#setSuperMerchant").val('');
		reloadTable();
		// hideColumn();
	});

});

function format ( d ) {
    
// `d` is the original data object for the row
	
	return '<div class="main-div">'+
		'<div class="inner-div">'+
			'<span>Sub-Merchant</span>'+
			'<span>'+d.subMerchantId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Delivery Status</span>'+
			'<span>'+d.deliveryStatus+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Payment Region</span>'+
			'<span>'+d.paymentRegion+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Transaction Mode</span>'+
			'<span>'+d.transactionMode+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Mask</span>'+
			'<span>'+d.cardNumber+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>RRN</span>'+
			'<span>'+d.rrn+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Payout Date</span>'+
			'<span>'+d.payOutDate+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Cust Name</span>'+
			'<span>'+d.customerName+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Cust Name</span>'+
			'<span>'+d.customerEmail+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Cardholder Type</span>'+
			'<span>'+d.cardHolderType+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Txn Type</span>'+
			'<span>'+d.txnType+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Status</span>'+
			'<span>'+d.status+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Base Amount</span>'+
			'<span>'+d.amount+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>TDR / Surcharge</span>'+
			'<span>'+d.tdr_Surcharge+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
		    '<span>GST</span>'+
		    '<span>'+d.gst_charge+'</span>'+
	    '</div>'+
		'<div class="inner-div">'+
			'<span>Reseller Charges</span>'+
			'<span>'+d.resellerCharges+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Reseller GST</span>'+
			'<span>'+d.resellerGST+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Merchant Amount</span>'+
			'<span>'+d.totalAmtPayable+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Doctor</span>'+
			'<span>'+d.doctor+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Glocal</span>'+
			'<span>'+d.glocal+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Partner</span>'+
			'<span>'+d.partner+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Unique ID</span>'+
			'<span>'+d.uniqueId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Transaction Flag</span>'+
			'<span>'+d.txnSettledType+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Part Settled Flag</span>'+
			'<span>'+d.partSettle+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF11</span>'+
			'<span>'+d.UDF11+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF12</span>'+
			'<span>'+d.UDF12+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF13</span>'+
			'<span>'+d.UDF13+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF14</span>'+
			'<span>'+d.UDF14+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF15</span>'+
			'<span>'+d.UDF15+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF16</span>'+
			'<span>'+d.UDF16+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF17</span>'+
			'<span>'+d.UDF17+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UDF18</span>'+
			'<span>'+d.UDF18+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Category Code</span>'+
			'<span>'+d.categoryCode+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>SKU Code</span>'+
			'<span>'+d.SKUCode+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Refund Cycle</span>'+
			'<span>'+d.refundCycle+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Refund Order ID</span>'+
			'<span>'+d.refundOrderId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Product Price</span>'+
			'<span>'+d.productPrice+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>Vendor ID</span>'+
			'<span>'+d.vendorId+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
			'<span>UTR No.</span>'+
			'<span>'+d.utrNo+'</span>'+
		'</div>'+
		'<div class="inner-div">'+
		'<span>SUF GST</span>'+
		'<span>'+d.sufGst+'</span>'+
	'</div>'+
	'<div class="inner-div">'+
		'<span>SUF TDR</span>'+
		'<span>'+d.sufTdr+'</span>'+
	'</div>'+
	'</div>';
	// document.querySelector(".selector")
}
	

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
		"columnDefs" : [ {
			className : "dt-body-right",
			"targets" : [ 1, 2, 3, 4, 5]
		}
		
			],
		dom : 'BTrftlpi',
		buttons : [
		$.extend(true, {}, buttonCommon, {
			extend : 'copyHtml5',
			exportOptions : {
				columns : [ 0, 1, 2, 3, 4]
			},
		}),
		$.extend(true, {}, buttonCommon, {
			extend : 'csvHtml5',
			title : 'Sale Transaction Report',
			exportOptions : {

				columns : [ 0, 1, 2, 3, 4]
			},
		}),
		{
			extend : 'pdfHtml5',
			orientation : 'landscape',
			pageSize : 'legal',
			//footer : true,
			title : 'Sale Transaction Report',
			exportOptions : {
				columns : [ 0, 1, 2, 3, 4]
			},
			customize : function(doc) {
				doc.defaultStyle.alignment = 'center';
				doc.styles.tableHeader.alignment = 'center';
			}
		},
		{
			extend : 'print',
			//footer : true,
			title : 'Sale Transaction Report',
			exportOptions : {
				columns : [ 0, 1, 2, 3, 4]
			}
		},
		{
			extend : 'colvis',
			columns : [ 0, 1, 2, 3, 4]
		} ],

		"ajax" : {

			"url" : "settledTransactionSearchAction",
			"type" : "POST",
			"data" : function(d) {
				return generatePostData(d);
			}
		},
		"fnDrawCallback" : function(settings, json) {
			if(settings.json != undefined){
				$("#retailMerchantFlag").val(settings.json.retailMerchantFlag);
			}
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
			"targets" : [ 0, 1, 2, 3, 4]
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
			"data" : "merchants",
			"className" : "payId text-class"

		},
		{
			"data" : "pgRefNum",
			"className" : "text-class"
		},
		{
			"data" : "orderId",
			"className" : "text-class"
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
		// {
		// 	"data" : "utrNo",
		// 	"className" : "text-class",
		// 	"width" : "10%"
		// },
		{
			"data" : "dateFrom",
			"className" : "text-class",
			"width" : "10%"
		},
		{
			"data" : "totalAmount",
			"className" : "text-class"
		},

	]
	});

}

$(document).ready(function() {
	

	$("body").on("click", "#txnResultDataTable tbody td", function(e){
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		if(e.target.localName != "button"){
			var tr = $(this).closest('tr');
			var row = table.row(tr);
			if ( row.child.isShown() ) {
				tr.removeClass('shown');
				setTimeout(function(e){
					row.child()[0].children[0].classList.remove("active-row");
					row.child.hide();
				}, 600)
			}
			else {
				row.child( format(row.data()) ).show();
				row.child()[0].children[0].classList.add("active-row");
				getAllData();
				tr.addClass('shown');
			}
		}
		
	})

	function getAllData(){
		var _new = document.querySelectorAll(".inner-div");
		_new.forEach(function(index, array, element){
			var _getValue = _new[array].children[1].innerText;
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
				_new[array].classList.add("d-none");
			}
		})
	}
});

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
	var tableObj = $('#txnResultDataTable');
	var table = tableObj.DataTable();
	table.ajax.reload();
}
	
function generatePostData(d) {

	var obj = {};

	var _getAllInput = document.querySelectorAll("[data-var]");
	_getAllInput.forEach(function(index, element, array){
	var _new =  _getAllInput[element].closest(".col-md-3").classList;
	var _newVal = _new.toString().indexOf("d-none");
	if(_newVal == -1){
		obj[_getAllInput[element].name] = _getAllInput[element].value
	}
	})

	obj.token = document.getElementsByName("token")[0].value;
	obj.draw = d.draw;
	obj.length = d.length;
	obj.start = d.start;
	obj["struts.token.name"] = "token";

	if(obj.merchantEmailId == ""){
		obj.merchantEmailId = "ALL"
	}

	if(obj.paymentType == ""){
		obj.paymentType = "ALL"
	}

	if(obj.currency == ""){
		obj.currency = "ALL";
	}

	if(obj.transactionFlag == ""){
		obj.transactionFlag = "ALL"
	}else{
		obj.transactionFlag = $("#transactionFlag").val().toString();
	}

	return obj;
}

$(document).ready(function(e){
	document.querySelector("#downloadButton").onclick = createDownloadForm;
	function createDownloadForm(e){
		var _checkButton = e.target.value;
		var _get = document.querySelectorAll("[data-download]");
		var _input = "";
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transTo - transFrom > 61 * 86400000) {
			alert('No. of days can not be more than 60 days');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		
		_get.forEach(function(index, array, element){
			var _dNone = index.closest(".d-none");
			if(_dNone == null){
				var _value = index.value;
				if(_value == "" && index.id == "merchant"){
					_value = "ALL"
				}
				_input += "<input type='hidden' name='"+index.getAttribute("data-download")+"' value='"+_value+"' />"
			}
		})
		if(_checkButton == "Download"){
			_input += "<input type='hidden' name='paymentsRegion' value='ALL' />";
			_input += "<input type='hidden' name='reportType' value='settled' />";
			document.querySelector("#downloadTransactionsReportAction").innerHTML = _input;
			document.querySelector("#downloadTransactionsReportAction").submit();	
		}else{
			document.querySelector("body").classList.remove("loader--inactive");
			var _obj = {};
			_get.forEach(function(ind, arr, ele){
				_obj[ind.getAttribute("data-download")] = ind.value;
			})
			if(_obj['merchantPayId'] == ""){
				_obj['merchantPayId'] = 'ALL'
			}
			_obj['reportType'] = "settled";
			$.ajax({
				type: "POST",
				url: "generateTxnReportFileAction",
				data: _obj,
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
})

function dateBaseDownload(){
	var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
	var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
	if (transTo - transFrom > 30 * 86400000) {
		if(checkBlankPgRefOrderId(['#pgRefNum', '#orderId'])){
			document.querySelector("#downloadButton").value = "Generate";
		}else{
			document.querySelector("#downloadButton").value = "Download";
		}
	}else{
		document.querySelector("#downloadButton").value = "Download";
	}

}

function checkBlankPgRefOrderId(_selector){
	var _val = _selector;
	var _checkBoolean = true;
	for(var i = 0; i < _val.length; i++){
		var _isValue = document.querySelector(_val[i]).value;
		if(_isValue != ""){
			_checkBoolean = false;
			break;
		} 
	}
	return _checkBoolean;
}