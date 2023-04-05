
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
	
	renderTable();

	

});


$("#submit").click(function(env) {
		
    // $("#setSuperMerchant").val('');
    reloadTable();
    // hideColumn();
});	

function renderTable() {

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

	$('#txnResultDataTable').dataTable({
		"ajax" : {

			"url" : "p2MPayoutSearchAction",
			"type" : "POST",
			"data" : function(d) {
				return generatePostData(d);
			}
		},
		"fnDrawCallback" : function(settings, json) {
			$("body").addClass("loader--inactive");
		},
		"ordering" : false,
		"destroy": true,
		"paginationType" : "full_numbers",
		"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
		"order" : [["1", "desc"]],


		"columns" : [
		
		{
			"data" : "merchantName",
			"className" : "payId text-class"

		},
		{
			"data" : "transactionCaptureDate",
			"className" : "text-class"
		},
		{
			"data" : "rrn",
			"className" : "text-class"
		},
		{
			"data" : "orderId",
			"className" : "text-class"
		},
		{
			"data" : "payerName",
			"className" : "text-class",
			"width" : "10%"
		},
		{
			"data" : "payeeAddress",
			"className" : "text-class",
			"width" : "10%"
		},
		{
			"data" : "amount",
			"className" : "text-class"
		},
        {
			"data" : "status",
			"className" : "text-class"
		},
    ]
	});

}


function reloadTable() {

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

	$("body").removeClass("loader--inactive");
	
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
		if(_newVal == -1 || index.value != ""){
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

    console.log(obj);

	return obj;
}

// download function

$(document).ready(function(e){
	document.querySelector("#downloadButton").onclick = createDownloadForm;
	function createDownloadForm(e){
		var _get = document.querySelectorAll("[data-download]");
		var _input = "";
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transTo - transFrom > 31 * 86400000) {
			alert('No. of days can not be more than 31');
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
		document.querySelector("#downloadp2MPayoutReportAction").innerHTML = _input;
		document.querySelector("#downloadp2MPayoutReportAction").submit();	
	}
})
