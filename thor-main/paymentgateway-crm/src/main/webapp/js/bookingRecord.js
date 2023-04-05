$(document).ready(function() {
    // ADD REMOVE SLIDE CLASS
    if($("#userMerchant").length > 0) {
        $("#txn-pgRefNum").removeClass("slide-form-element");
    }

    document.querySelector("#merchant").addEventListener("change", function(e){
        if(e.target.value == "ALL"){
            document.querySelector("#subMerchant").closest(".col-md-3").classList.add("d-none");
            $("#subMerchant").selectpicker('val', "ALL");
        }
        getSubMerchant(e, "getSubMerchantList", {
            isSuperMerchant : true,
            subUser : true,
            retailMerchantFlag: true,
        });
    });
    document.querySelector("#subMerchant").addEventListener("change", function(e){
        getSubMerchant(e, "vendorTypeSubUserListAction", {
            subUser : true
        });
    })

})


function hideColumn(){
    var _getMerchant = $("#merchant").val();
    var _td = $("#txnResultDataTable").DataTable();
    var _glocalFlag = $("#setGlobalData").val();
    var _retailMerchantFlag = $("#retailMerchantFlag").val();
    var _getMerchantInput = $("#setSuperMerchant").val();
    var _dispatchSlipFlag = $("#dispatchSlipFlag").val();
    if(_retailMerchantFlag == "false"){
        _td.columns(30).visible(false);
        _td.columns(31).visible(false);
        _td.columns(32).visible(false);
        _td.columns(33).visible(false);
        _td.columns(34).visible(false);
    }else{
        _td.columns(30).visible(true);
        _td.columns(31).visible(true);
        _td.columns(32).visible(true);
        _td.columns(33).visible(true);
        _td.columns(34).visible(true);
    }

    if(_getMerchant != "" && _glocalFlag == "true"){
        _td.columns(24).visible(true);
        _td.columns(25).visible(true);
        _td.columns(26).visible(true);
        _td.columns(27).visible(true);
    }else{
        _td.columns(24).visible(false);
        _td.columns(25).visible(false);
        _td.columns(26).visible(false);
        _td.columns(27).visible(false);
    }

    if(_dispatchSlipFlag == "true"){
         _td.columns(2).visible(true);
         _td.columns(3).visible(true);
         _td.columns(4).visible(true);
         _td.columns(35).visible(true);
     }else{
         _td.columns(2).visible(false);
         _td.columns(3).visible(false);
         _td.columns(4).visible(false);
         _td.columns(35).visible(false);
     }
     
    
    if(_getMerchantInput == "" || _getMerchantInput == "NA"){
        _td.columns(6).visible(false);
    }else{
        _td.columns(6).visible(true);

    }
}


function format ( d ) {
    // `d` is the original data object for the row
        d.chargeRefundButton = function(){
            var userType = document.querySelector("#userType").value;		
            var chargebackBtn = "";
            if(userType == "ADMIN" || userType == "SUBADMIN") {
                if(d.btnchargebacktext !== "close") {
                    chargebackBtn = '<button class="lpay_button lpay_button-md lpay_button-secondary btnChargeBack" style="font-size:10px;" >Chargeback</button>';
                }
            }
    
            if (userType == "ADMIN"|| userType == "SUBADMIN" || userType == "MERCHANT") {
                if(d.refundBtnText == "Refunded") {
                    /* chargebackBtn = '<button class="btn btn-info btn-xs btn-block btnChargeBack" style="font-size:10px;" disabled>Chargeback</button>'; */
                    return '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;" disabled>Refunded</button>';
                } else if(d.refundBtnText == "Partial Refund") {
                    return chargebackBtn + '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;">Partial Refund</button>';
                } else {
                    return chargebackBtn + '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;">Refund</button>';
                }
            } else {
                return "";
            }
        }

        d.pdfButton = function(){
            if(d.pdfDownloadFlag == true) {
                return '<button class="lpay_button lpay_button-md lpay_button-secondary btnDispatchLink" style="font-size:10px;">Download </button>';
            } else {
                return "Not Available";
            }
        }

        d.invoiceLink = function(){
            if(d.customFlag == "Y") {
                return '<button class="lpay_button lpay_button-md lpay_button-secondary btnInvoice" style="font-size:10px;">Download </button>';
            } else {
                return "Not Available";
            }
        }

        return '<div class="main-div">'+
            '<div class="inner-div">'+
                '<span>Txn Id</span>'+
                '<span>'+d.transactionId+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Sub-Merchant</span>'+
                '<span>'+d.subMerchantId+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Invoice Number</span>'+
                '<span>'+d.invoiceNo+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Dispatch Slip ID</span>'+
                '<span>'+d.dispatchSlipNo+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Courier Name</span>'+
                '<span>'+d.courierServiceProvider+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Payment Region</span>'+
                '<span>'+d.paymentRegion+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Mask</span>'+
                '<span>'+d.cardNumber+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Cust Name</span>'+
                '<span>'+d.customerName+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Cust Email</span>'+
                '<span>'+d.customerEmail+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Customer Mobile</span>'+
                '<span>'+d.customerMobile+'</span>'+
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
                '<span>Transaction Mode</span>'+
                '<span>'+d.transactionMode+'</span>'+
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
                '<span>Product Price</span>'+
                '<span>'+d.productPrice+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Vendor ID</span>'+
                '<span>'+d.venderID+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Download Invoice</span>'+
                '<span>'+d.invoiceLink()+'</span>'+
            '</div>'+
            '<div class="inner-div" style="width: 100%;text-align: center">'+
                '<span></span>'+
                '<span>'+d.chargeRefundButton()+'</span>'+
            '</div>'+
        '</div>';
        // document.querySelector(".selector")
    }
    


$(document).ready(function() {


    $('body').on('click','#txnResultDataTable .btnRefund',function() {
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		var _btn = $(this).text();
		var _getClosestTr = $(this).closest("tr").prev("tr");
		var _data = table.rows(_getClosestTr).data();
		if(_btn !== "Refunded") {
			document.querySelector("#pg-ref").value = _data[0]['pgRefNum'];
			document.querySelector("#payId").value = _data[0]['payId'];
			document.querySelector("#refundedAmount").value = _data[0]['refundedAmount'];
			document.querySelector("#refundAvailable").value = _data[0]['refundAvailable'];
			$("body").removeClass("loader--inactive");
			$("#manualRefundProcess").submit();
		}
	});

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

	$('body').on('click','#txnResultDataTable .btnChargeBack',function() {
		var table = new $.fn.dataTable.Api('#txnResultDataTable');
		var _getClosestTr = $(this).closest("tr").prev("tr");
		var _data = table.rows(_getClosestTr).data();
		document.querySelector("#orderIdc").value = _data[0]['orderId'];
		document.querySelector("#payIdc").value = _data[0]['payId'];
		document.querySelector("#chargeback-refundedAmount").value = _data[0]['refundedAmount'];
		document.querySelector("#chargeback-pgRefNum").value = _data[0]['pgRefNum'];
		document.querySelector("#chargeback-refundAvailable").value = _data[0]['refundAvailable'];
		document.querySelector("#txnIdc").value = _data[0]['transactionId'];
		$("body").removeClass("loader--inactive");
		document.chargeback.submit();
	});

    $(".blank-space").on("change", function(e){
        var _this = $(this).val();
        $(this).val(_this.trim());
    });

    $('#txnResultDataTable').on('click','.btnInvoice',function() {
        var _parent = $(this).closest("tr");
        var orderId1 = _parent.find(".orderId").text();
        var _payId = $("#dispatchPayId").val(); 					 
        fetchInvoice(orderId1,_payId);
    });
    
    function fetchInvoice(orderId , payId) {
    
    var token = document.getElementsByName("token")[0].value;
    $.ajax({
        url : "fetchEventInvoice",
        type : "POST",
        timeout : 0,
        data : {
            orderId : orderId,
            payId : payId,
            token : token
        },
        success : function(response) {
            var base64Data = response.aaData.base64Data;
            var dataType = response.aaData.dataType;
            var linkSource = "data:application/"+dataType+";base64,"+base64Data;
            var downloadLink = document.createElement("a");
            var fileName = orderId+"_invoice."+dataType;
            downloadLink.href = linkSource;
            downloadLink.download = fileName;
            downloadLink.click();

        },
        error : function(data) {
            alert("Something went wrong!");
            setTimeout(function() {
                _body.classList.add("loader--inactive");
            }, 1000);
        }
    });
}


    $("#setGlobalData").on("change", function(e){
        hideColumn();
    });
    
    $("body").on("click", ".btnDispatchLink", function(e){
         var _parent = $(this).closest("tr");
         /*$("#dis").submit(); */
        var _orderId = _parent.find(".orderId").text();
        $.ajax({
            type: "post",
            url: "dispatchSlipPdfDownload",
            data: {"orderId": _orderId},
            success: function(data){
                $("#dispatchLink").attr("href", "data:application/pdf;base64,"+data.aaData.base64Data);
                $("#dispatchLink").attr("download", "Dispatch-Slip_"+data.aaData.ORDER_ID+".pdf");
                document.querySelector("#dispatchLink").click();
                //downloadLink();
            }
        })
    });


    function downloadLink(){
        $("a#dispatchLink").click();
        $("#dispatchLink").trigger('onclick');
    }

    $(function() {
        renderTable();
    });

    $("#submit").click(function(env) {
        $("body").removeClass("loader--inactive");
        
        reloadTable();
        //  hideColumn();
    });     


    // DISPLAY SUB MERCHANT
    // var _select = "<option value='ALL'>ALL</option>"
    // $("[data-id=subMerchant]").find('option:eq(0)').before(_select);
    // $("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");      

    $("#merchant").on("change", function(e) {
        var _merchant = $(this).val();
        if(_merchant != "") {
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "POST",
                url: "getSubMerchantList",
                data: {"payId": _merchant},
                success: function(data){
                    $("#subMerchant").html("");
                    $("#retailMerchantFlag").val(data.retailMerchantFlag);
                    if(data.superMerchant == true){
                        var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
                        for(var i = 0; i < data.subMerchantList.length; i++){
                            _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
                        }
                        $("[data-id=submerchant]").removeClass("d-none");
                        $("#subMerchant option[value='']").attr("selected", "selected");
                        $("#subMerchant").selectpicker();
                        $("#subMerchant").selectpicker("refresh");
                        setTimeout(function(e){
                            $("body").addClass("loader--inactive");
                        },500);
                    }else{
                        setTimeout(function(e){
                            $("body").addClass("loader--inactive");
                        },500);
                        $("[data-id=submerchant]").addClass("d-none");
                        $("#subMerchant").val("");
                    }
                }
            });
        } else {
            $("#retailMerchantFlag").val("true");
            $("[data-id=submerchant]").addClass("d-none");
            $("#subMerchant").val("");  
        }
    });
});

function renderTable() {
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
            "targets" : [ 1, 2, 3, 4, 5, 6]
        } ],
        dom : 'BTrftlpi',
        buttons : [
                $.extend(true, {}, buttonCommon, {
                    extend : 'copyHtml5',
                    exportOptions : {
                        columns : [ 0, 1, 2, 3, 4, 5]
                    },
                }),
                $.extend(true, {}, buttonCommon, {
                    extend : 'csvHtml5',
                    title : 'Sale Transaction Report',
                    exportOptions : {

                        columns : [ 0, 1, 2, 3, 4, 5]
                    },
                }),
                {
                    extend : 'pdfHtml5',
                    orientation : 'landscape',
                    pageSize : 'legal',
                    //footer : true,
                    title : 'Sale Transaction Report',
                    exportOptions : {
                        columns : [ 0, 1, 2, 3, 4, 5]
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
                        columns : [ 0, 1, 2, 3, 4, 5]
                    }
                },
                {
                    extend : 'colvis',
                    columns : [ 0, 1, 2, 3, 4, 5]
                } ],

        "ajax" : {

            "url" : "viewBookingRecord",
            "type" : "POST",
            "data" : function(d) {
                return generatePostData(d);
            }
        },
        "columnDefs" : [
                {
            "type" : "html-num-fmt",
            "targets" : 4,
            "orderable" : true,
            "targets" : [ 0, 1, 2, 3, 4, 5]
        },
            {
            'targets': 0,
            'createdCell':  function (td, cellData, rowData, row, col) {
                $("#setGlobalData").val(rowData["glocalFlag"]);
                $("#setSuperMerchant").val(rowData["subMerchantId"]);
                $("#dispatchSlipFlag").val(rowData["dispatchSlipFlag"]);
                $("#pdfDownloadFlag").val(rowData["pdfDownloadFlag"]);
                $("#dispatchPayId").val(rowData["payId"]);
                $("#retailMerchnatFlag").val(rowData["retailMerchantFlag"]);
            }}
            ],
            
        "fnDrawCallback" : function(settings, json) {
            if(settings.json != undefined){
                $("#retailMerchantFlag").val(settings.json.retailMerchantFlag);
            }
                // hideColumn();
            $("#submit").removeAttr("disabled");
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

        "columns" : [
                
                {
                    "data" : "merchants",
                    "className" : "text-class"
                },{
                    "data" : "pgRefNum",
                    "className" : "payId text-class"

                },
                {
                    "data" : "dateFrom",
                    "className" : "text-class",
                    "width" : "10%"
                },
                {
                    "data" : "orderId",
                    "className" : "orderId text-class"
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
                {
                    "data" : "totalAmount",
                    "className" : "text-class"
                },

        

        ]
    });
}

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

    $("#submit").attr("disabled", true);
    var tableObj = new $.fn.dataTable.Api('#txnResultDataTable');
    
    tableObj.ajax.reload();
    setTimeout(function(data){
    $("body").addClass("loader--inactive");
    hideColumn();
    }, 500);
    
}
function generatePostData(d) {
    var obj = {};

    var _getAllInput = document.querySelectorAll("[data-var]");

    _getAllInput.forEach(function(element) {
        var _new =  element.closest(".col-md-3").classList,
            _newVal = _new.toString().indexOf("d-none");

        if(_newVal == -1) {
            obj[element.name] = element.value;
        } else {
            if(element.getAttribute("id") == "merchant") {
                obj[element.name] = element.value;
            }
        }
    });

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

    return obj;
}


function dateBaseDownload(){
	var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
	var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
	if (transTo - transFrom > 30 * 86400000) {
		if(checkBlankPgRefOrderId(['#pgRefNum', '#orderId'])){
			document.querySelector("#downloadSubmit").value = "Generate";
		}else{
			document.querySelector("#downloadSubmit").value = "Download";
		}
	}else{
		document.querySelector("#downloadSubmit").value = "Download";
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

function downloadSubmit(e) {
    var _text = e.value;
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transTo - transFrom > 61 * 86400000) {
		alert('No. of days can not be more than 60 days');
		$("body").addClass("loader--inactive");
		$('#dateFrom').focus();
		return false;
	}
    var token = document.getElementsByName("token")[0].value;
    var pgRefNum = document.getElementById("pgRefNum").value;
    var orderId = document.getElementById("orderId").value;
    var merchant = document.getElementById("merchant").value;
    var subMerchant = document.getElementById("subMerchant").value;
    var paymentMethod = document.getElementById("paymentMethod").value;
    var currency = document.getElementById("currency").value;
    var partSettleFlag = document.getElementById("partSettleFlag").value;
    var dateFrom = document.getElementById("dateFrom").value;
    var dateTo = document.getElementById("dateTo").value;
    var custEmail = document.getElementById("custEmail").value.trim();
    var subUserPayId = document.getElementById("subUser").value; 
    if (merchant == '') {
        merchant = 'ALL'
    }
    if(_text == "Download"){
        document.getElementById("pgRefNumForm").value = pgRefNum;
        document.getElementById("orderIdForm").value = orderId;
        document.getElementById("merchantForm").value =  merchant;
        document.getElementById("subMerchantPayId").value =  subMerchant;
        document.getElementById("paymentMethodForm").value = paymentMethod;
        document.getElementById("currencyForm").value = currency;
        document.getElementById("partSettleFlagForm").value =  partSettleFlag;
        document.getElementById("dateFromForm").value =  dateFrom;
        document.getElementById("dateToForm").value =  dateTo;
        document.getElementById("custEmailDownload").value = custEmail;
        document.getElementById("subUserPayIdForm").value = subUserPayId;
        document.getElementById("bookingDownloadForm").submit();
    }else{
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "generateBookingReportFileAction",
            data : {
                "token": token,
                "pgRefNum" : pgRefNum,
                "subUserPayId": subUserPayId,
                "orderId" : orderId,
                "merchantPayId" : merchant,
                "subMerchantPayId" : subMerchant,
                "paymentMethod" : paymentMethod,
                "currency" : currency,
                "partSettleFlag" : partSettleFlag,
                "dateFrom" : dateFrom,
                "dateTo" : dateTo,
                "custEmail" : custEmail,
                "subUserPayId" : subUserPayId,
                "reportType" : "bookingReport"
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

var allowAlphaNumericSpecial = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
}