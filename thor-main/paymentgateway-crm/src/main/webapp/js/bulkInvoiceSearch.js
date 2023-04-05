var changeDateFormat = function(arg) {
    var dateArr = arg.split('-');
    return dateArr[2] + '-' + dateArr[1] + '-' + dateArr[0];
}

function hideColumn(){
    var _isSuperMerchant = $("#isSuperMerchant").val();
    var _table = $("#bulk-invoice-search").DataTable();
    if(_isSuperMerchant == "N"){
        _table.columns(2).visible(false);
    }else{
        _table.columns(2).visible(true);
    }
}

var dateValidation = function() {
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
    
    return true;
}

function reloadTable() {
    var result = dateValidation();

    if(result) {
        document.querySelector("body").classList.remove("loader--inactive");

        var tableObj = $('#bulk-invoice-search');
        var table = tableObj.DataTable();
        table.ajax.reload();
    }
}

$(document).ready(function() {
    // WHEN PAGE IS LOADING
    var today = new Date();
    $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
    $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
    renderTable();


    $(".datepicker").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        changeMonth : true,
        changeYear : true,
        selectOtherMonths : false,
        maxDate : new Date()
    });

    $("#merchants").on('change', function() {
        var _val = $(this).val();
        getSubMerchant();
        // if(_val !== "") {
        //     $("#btnSave").attr('disabled', false);
        // } else {
        //     $("#btnSave").attr('disabled', true);
        //     $("#currencyCode").removeClass("d-none");
        //     $("#mappedCurrency").addClass("d-none");
        // }
        
        // $('#spanMerchant').hide();
        // $('#currencyCodeloc').hide();
        });
});


function getSubMerchant(){
    var _merchant = $("#merchants").val();
    $("#isSuperMerchant").val("Y");
    if(_merchant != ""){
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getSubMerchantListByPayId",
            data: {"payId": _merchant},
            success: function(data){
                console.log(data);
                $("#subMerchant").html("");
                if(data.superMerchant == true){
                    $("#isSuperMerchant").val("Y");
                    var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
                    for(var i = 0; i < data.subMerchantList.length; i++){
                        _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
                    }
                    $("[data-id=submerchant]").removeClass("d-none");
                    $("#subMerchant option[value='']").attr("selected", "selected");
                    $("#subMerchant").selectpicker();
                    $("#subMerchant").selectpicker("refresh");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                }else{
                    // handleChange();
                    $("#isSuperMerchant").val("N");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                    $("[data-id=submerchant]").addClass("d-none");
                    $("#subMerchant").val("");
                }
            }
        });
    }else{
        $("#isSuperMerchant").val("Y");
        $("[data-id=submerchant]").addClass("d-none");
        $("#subMerchant").val("");	
    }
}



function generatePostData() {
    var dateFrom = document.getElementById("dateFrom").value;
        dateFrom = changeDateFormat(dateFrom);

    var dateTo = document.getElementById("dateTo").value;
        dateTo = changeDateFormat(dateTo);
    var merchantPayId= document.getElementById("merchants").value;
    var subMerchantPayId= document.getElementById("subMerchant").value;
    var token = document.getElementsByName("token")[0].value;

    var obj = {
        dateFrom : dateFrom,
        dateTo : dateTo,
        merchantPayId : merchantPayId,
        subMerchantId : subMerchantPayId,
        token:token,
        "struts.token.name": "token",
    };

    return obj;
}

function renderTable() {
    //to show new loader -Harpreet
    $.ajaxSetup({
        global: false,
        beforeSend: function () {
            toggleAjaxLoader();
        },
        complete: function () {
            toggleAjaxLoader();
        }
    });
    var table = new $.fn.dataTable.Api('#bulk-invoice-search');
    $.ajaxSetup({
        global: false,
        beforeSend: function () {
            $(".modal").show();
            document.querySelector("body").classList.remove("loader--inactive");
        },
        complete: function () {
            $(".modal").hide();
            
            setTimeout(function() {
                document.querySelector("body").classList.add("loader--inactive");
            }, 1000);
        }
    });
    var table = $('#bulk-invoice-search').DataTable();
    // $('#bulk-invoice-search tbody').on('click', 'td', function() {
    // 	popup(table, this);
    // });	
    
    var result = dateValidation();

    if(result) {
        $('#bulk-invoice-search').dataTable({
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            dom : 'BTftlpi',
            buttons : [ {
                extend : 'copyHtml5',
                exportOptions : {
                    columns : [':visible :not(:last-child)']
                }
            }, {
                extend : 'csvHtml5',
                title : 'Invoice Search',
                exportOptions : {
                    columns : [':visible :not(:last-child)']
                }
            }, {
                extend : 'pdfHtml5',
                orientation : 'landscape',
                title : 'Search Transactions',
                exportOptions : {
                    columns : [':visible :not(:last-child)']
                },
                customize: function (doc) {						
                    doc.defaultStyle.alignment = 'center';
                    doc.styles.tableHeader.alignment = 'center';
                }					  	
            }, {
                extend : 'print',
                title : 'Invoice Search',
                exportOptions : {
                    columns : [':visible :not(:last-child)']
                }
            }, {
                extend : 'colvis',					
                columns : [0, 1, 2, 3, 4, 5, 6, 7, 8]
            } ],			
            "ajax" : {
                "url" : "bulkInvoiceSearchData",
                "type" : "POST",
                "dataSrc" : "invoiceHistories",
                "data" : function (d) {                    
                    return generatePostData(d);
                }
            },
            "fnDrawCallback" : function(settings, json) {
                hideColumn();
                // $("body").addClass("loader--inactive");
            },
            "bProcessing" : true,
            "bDestroy" : true,
            "bLengthChange" : true,
            "iDisplayLength" : 10,
            "order" : [ [
                    2,
                    "desc" ] ],
            "aoColumns" : [
                {
                    "data" : "fileName",
                    "sWidth" : '15%',
                    "className" : "color-sky-blue"
                },
                {
                    "data" : "businessName",							
                    "className" : "text-center"
                },
                {"mData" : null,
                "mRender" : function(row){
                        if(row.subMerchantbusinessName != null){
                            return row.subMerchantbusinessName;
                        }else{
                            return "<span>NA</span>"
                        }
                    }
                },
                {
                    "data" : "date",
                    "sWidth" : '15%',
                    "className" : "text-center"						
                },
                {
                    "data" : "totalRecords",
                    "sWidth" : '15%',
                    "className" : "text-center"
                },
                {
                    "data" : "success",
                    "sWidth" : '15%',
                    "className" : "text-center"
                },
                {
                    "data" : "totalUnsent",
                    "sWidth" : '15%',
                    "className" : "text-center"
                },
                {
                    "data" : "totalPending",
                    "sWidth" : '15%',
                    "className" : "text-center"
                },
                
                {
                    "mData" : null,
                    "sClass" : "text-center",
                    "bSortable" : false,
                    "mRender" : function() {
                        return '<button class="btn btn-info btn-xs download-csv">Download</button>';
                    }
                },
                
                {
                    "data" : null,
                    "sClass" : "my_class",
                    "sWidth" : '10%',
                    "visible" : false,
                    "className" : "displayNone",
                    "mRender" : function(row) {
                            return "\u0027" + row.fileName;
                    }
                }, ]
        });
    }
}

// searc design 
$("#bulk-invoice-search_filter label").addClass("search-label");


// DOWNLOAD FILE
$("body").on("click", ".download-csv", function(e) {
    e.preventDefault();
    var table = $('#bulk-invoice-search').DataTable();
    var _parent = $(this).closest("td");    
    var rowIndex = table.cell(_parent).index().row;    
    var rowData = table.row(rowIndex).data();

    $("#filename").val(rowData.fileName);

    if($("#filename").val() !== "") {
        $('#downloadFileForm').submit();
    }
});