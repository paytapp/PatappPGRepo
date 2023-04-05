function handleChange() {
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transFrom == null || transTo == null) {
        alert('Enter date value');
        return false;
    }

    if (transFrom > transTo) {
        alert('From date must be before the to date');
        $('#dateFrom').focus();
        return false;
    }
    if (transTo - transFrom > 31 * 86400000) {
        alert('No. of days can not be more than 31');
        $('#dateFrom').focus();
        return false;
    }
 }

 var downloadSummaryReport = function(that, event) {
    var _text = that.innerText;

    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transTo - transFrom > 61 * 86400000) {
        alert('No. of days can not be more than 60 days');
        $("body").addClass("loader--inactive");
        $('#dateFrom').focus();
        return false;
    }

    event.preventDefault();
    var merchants = $("#merchants").val(),
        subMerchant = $("#subMerchant").val(),
        transactionType = $("#transactionType").val(),
        paymentMethods = $("#paymentMethods").val(),
        acquirer = $("#acquirer").val().toString(),
        currency = $("#currency").val(),
        partSettleFlag = $("#partSettleFlag").val(),
        dateFrom = $("#dateFrom").val(),
        dateTo = $("#dateTo").val(),
        paymentsRegion = $("#paymentsRegion").val(),
        cardHolderType = $("#cardHolderType").val(),
        mopType = $("#mopType").val(),
        transactionFlag = $("#transactionFlag").val();

        console.log(transactionFlag);

    if(merchants == "") {
        merchants = "ALL";
    }
    if(_text == "Download"){
        $("#merchants-summary").val(merchants);
        $("#subMerchant-summary").val(subMerchant);
        $("#transactionType-summary").val(transactionType);
        $("#paymentMethods-summary").val(paymentMethods);
        $("#acquirer-summary").val(acquirer);
        $("#currency-summary").val(currency);
        $("#dateFrom-summary").val(dateFrom);
        $("#dateTo-summary").val(dateTo);
        $("#paymentsRegion-summary").val(paymentsRegion);
        $("#cardHolderType-summary").val(cardHolderType);
        $("#mopType-summary").val(mopType);
        $("#partSettleFlag-summary").val(partSettleFlag);
        $("#transactionFlag-summary").val(transactionFlag);
        $("#downloadSummaryReportAction").submit();
    }else{
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "generateSummaryReportFileAction",
            data : {
                "merchantEmailId" : merchants,
                "subMerchantEmailId" : subMerchant,
                "paymentMethods" : paymentMethods,
                "transactionType" : transactionType,
                "acquirer": acquirer,
                "currency" : currency,
                "dateFrom" : dateFrom,
                "dateTo" : dateTo,
                "paymentsRegion" : paymentsRegion,
                "partSettleflag" : partSettleFlag,
                "cardHolderType" : cardHolderType,
                "mopType" : mopType,
                "transactionFlag": transactionFlag.toString(),
                "reportType" : "summaryReport"
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
        document.querySelector("#downloadSummaryReport").innerText = "Generate";
    }else{
        document.querySelector("#downloadSummaryReport").innerText = "Download";
    }
}

function hideColumn() {
    var _getMerchantInput = $("#setSuperMerchant").val(),
        td = $("#summaryReportDataTable").DataTable();

    if(_getMerchantInput == "" || _getMerchantInput == "NA") {
        td.columns(12).visible(false);
    } else {
        td.columns(12).visible(true);
    }
}
				
function renderTable() {
    $("body").removeClass("loader--inactive");
    
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val()),
        transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());

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
                    // Strip $ from salary column to make it numeric
                    return column === 0 ? "'"+data : (column === 3 ? "'" + data: data);
                }
            }
        }
    };
            
    $('#summaryReportDataTable').dataTable({
        dom : 'BTftlpi',
        buttons: ['csv', 'print', 'pdf'],
        language: {
            search: "",
            searchPlaceholder: "Search records"
        },
        "ajax" : {
            "url" : "summaryReportAction",
            "type" : "POST",
            "data" : function(d) {
                return generatePostData(d);
            }
        },
        "drawCallback" : function() {
            $("body").addClass("loader--inactive");
        },
        // "fnDrawCallback" : function() {
        //     hideColumn();
        //     $("#submit").removeAttr("disabled");
        //     $("body").addClass("loader--inactive");
        // },
        "searching" : false,
        "destroy": true,
        "processing" : true,
        "serverSide" : true,
        "paginationType" : "full_numbers",
        "lengthMenu" : [
            [ 10, 25, 50 ],
            [ 10, 25, 50 ]
        ],
        "order" : [],
        // "columnDefs" : [
        //     {
        //         "type" : "html-num-fmt",
        //         "targets" : 4,
        //         "orderable" : false,
        //         "targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
        //     },
        //     {
        //         'targets': 0,
        //         'createdCell':  function (td, cellData, rowData, row, col) {
        //             $("#setSuperMerchant").val(rowData["subMerchantId"]);									
        //         }
        //     }
        // ],
        "columns" : [									
            { "data" : "pgRefNum" },
            { "data" : "transactionCaptureDate" },
            { "data" : "paymentMethods" },
            { "data" : "mopType" },
            { "data" : "orderId" },
            { "data" : "businessName" },
            { "data" : "amount" },            
            { "data" : "netMerchantPayableAmount" }			
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
        alert('From date must be before the  To date');
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

    var tableObj = $('#summaryReportDataTable');
    var table = tableObj.DataTable();
    table.ajax.reload();		
}
	
function reloadCountTable() {
    var datepick = $.datepicker;
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transFrom == null || transTo == null) {
        alert('Enter date value');
        return false;
    }

    if (transFrom > transTo) {
        alert('From date must be before the  To date');
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

    var tableObj = $('#summaryReportCountDataTable');
    var table = tableObj.DataTable();
    table.ajax.reload();
}
	
function generatePostData(d) {
    var token = document.getElementsByName("token")[0].value,    
    acquirer = $("#acquirer").val(),
    merchantEmailId = $("#merchants").val(),
    subMerchantEmailId = $("#subMerchant").val(),
    paymentMethods = $("#paymentMethods").val(),
    mopType = $("#mopType").val(),
    currency = $("#currency").val(),
    paymentsRegion = $("#paymentsRegion").val(),
    cardHolderType = $("#cardHolderType").val(),
    pgRefNum = $("#pgRefNum").val(),
    transactionType = $("#transactionType").val(),
    partSettleFlag = $("#partSettleFlag").val(),
    transactionFlag = $("#transactionFlag").val();

    transactionFlag = transactionFlag.join(",");

    acquirer = acquirer.join(",");
    
    if(merchantEmailId==''){
        merchantEmailId='ALL'
    }
    if(subMerchantEmailId==''){
        subMerchantEmailId='ALL'
    }
    if(paymentMethods==''){
        paymentMethods='ALL'
    }
    
    if(currency==''){
        currency='ALL'
    }
    if(paymentsRegion==''){
        paymentsRegion='ALL'
    }
    if(cardHolderType==''){
        cardHolderType='ALL'
    }
    if(transactionType==''){
        transactionType='ALL'
    }
    if(transactionFlag==''){
        transactionFlag='ALL'
    }

    var obj = {
        paymentMethods : paymentMethods,
        subMerchantEmailId : subMerchantEmailId,
        dateFrom : document.getElementById("dateFrom").value,
        dateTo : document.getElementById("dateTo").value,
        merchantEmailId : merchantEmailId,
        currency : currency,
        mopType : mopType,
        partSettleFlag : partSettleFlag,
        transactionType : transactionType,
        paymentsRegion : paymentsRegion,
        cardHolderType : cardHolderType,
        pgRefNum : pgRefNum,
        acquirer : acquirer,
        transactionFlag : transactionFlag,
        draw : d.draw,
        length :d.length,
        start : d.start, 
        token : token,
        "struts.token.name" : "token",
    };

    return obj;
}

function checkRefNo(){
    var refValue = document.getElementById("pgRefNum").value;
    var refNoLength = refValue.length;
    if((refNoLength <16) && (refNoLength >0)){
        document.getElementById("submit").disabled = true;
        document.getElementById("validRefNo").style.display = "block";
    }
    else if(refNoLength == 0){
        document.getElementById("submit").disabled = false;
        document.getElementById("validRefNo").style.display = "none";
    }else{
        document.getElementById("submit").disabled = false;
        document.getElementById("validRefNo").style.display = "none";
    }
}

function getAllData(){
    var _new = document.querySelectorAll(".inner-div");
    _new.forEach(function(index, array, element){
        var _getValue = _new[array].children[1].innerText;
        if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
            _new[array].classList.add("d-none");
        }
    });
}

function dataTableMoreData (d) {
    if(d !== undefined) {
        var _mainDiv = "<div class='main-div'>",
        _obj = {
            "dateFrom" : "Settlement Date",
            "txnType": "Transaction Type",
            "acquirerType" : "Acquirer Name",
            "acquirerMode": "Acquirer Mode",
            "transactionRegion": "Transaction Region",
            "cardHolderType": "Card Holder Type",
            "subMerchantId": "Sub Merchant",
            "currency": "Currency",
            "merchantTdrOrSc": "Surcharge (Merchant)",
            "acquirerSurchargeAmount": "Surcharge (Acquirer)",
            "pgSurchargeAmount": "Surcharge (PG)",
            "resellerCharges": "Surcharge (Reseller)",
            "merchantGst": "GST (Merchant)",
            "totalGstOnAcquirer": "GST (Acquirer)",
            "totalGstOnMerchant": "GST (PG)",
            "resellerGST": "GST (Reseller)",
            "txnSettledType":"Transaction Flag",
            "partSettle":"Part Settle Flag",
            "refundOrderId":"Refund Order ID",
            "transactionFlag" : "Transaction Flag"
        }
    
        for(key in _obj) {
            if(_obj[key].hasOwnProperty("className")) {
                var _getKey = Object.keys(_obj[key]);
                _mainDiv += '<div class="inner-div '+_obj[key]["className"]+'">'+
                        '<span>'+_obj[key][_getKey[0]]+'</span>'+
                        '<span>'+d[_getKey[0]]+'</span>'+
                    '</div>'
            }else{
                _mainDiv += '<div class="inner-div">'+
                    '<span>'+_obj[key]+'</span>'+
                    '<span>'+d[key]+'</span>'+
                '</div>'
            }
        }
        _mainDiv += "</div>";
        return _mainDiv;        
    }
}

$(document).ready(function() {
    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        if(e.target.localName != "button" && e.target.localName != "input" && e.target.localName != "label"){
            var tr = $(this).closest('tr');
            var row = table.row(tr);
            if (row.child.isShown()) {
                tr.removeClass('shown');
                setTimeout(function(e){
                    row.child()[0].children[0].classList.remove("active-row");
                    row.child.hide();
                }, 600)
            } else {
                if(row.child(dataTableMoreData(row.data())) !== undefined) {
                    row.child(dataTableMoreData(row.data())).show();
                    row.child()[0].children[0].classList.add("active-row");
                    getAllData();
                    tr.addClass('shown');
                }
            }
        }        
    });

    $('.collapseStart').slideToggle("fast");

    $('.newDiv').click(function() {
        $(this).parent().find('.collapseStart').slideToggle("fast");
    });

    var _select = "<option value='ALL'>ALL</option>"
    $("[data-id=subMerchant]").find('option:eq(0)').before(_select);
    $("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");
    
    $("#merchants").on("change", function(e) {
        var _merchant = $(this).val();
        if(_merchant != "") {
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "POST",
                url: "getSubMerchantList",
                data: {"payId": _merchant},
                success: function(data){
                    console.log(data);
                    $("#subMerchant").html("");
                    if(data.superMerchant == true) {
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
                    } else {
                        setTimeout(function(e){
                            $("body").addClass("loader--inactive");
                        },500);
                        $("[data-id=submerchant]").addClass("d-none");
                        $("#subMerchant").val("");
                    }
                }
            });
        } else {
            $("[data-id=submerchant]").addClass("d-none");
            $("#subMerchant").val("");	
        }
    });
    
    renderTable();

    $("#submit").click(function(env) {
        $("body").removeClass("loader--inactive");
        reloadTable();
    });

    $(document).click(function(){
        expanded = false;
        $('#checkboxes').hide();
    });

    $('#checkboxes').click(function(e){
        e.stopPropagation();
    });
});