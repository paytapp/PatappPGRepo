$(document).ready(function(e){
    var _table = $("#merchantList");
    var _token = $("[name='token']").val();
    function dateToolTip(){
        $("body").removeClass("loader--inactive");
        $("td.registerDate").each(function(e){
            var _getDate = $(this).text();
            var _getSpace = _getDate.indexOf(" ");
            var _getTime = _getDate.substring(_getSpace);
            var _getOnlyDate = _getDate.substring(0, _getSpace);
            $(this).text(_getOnlyDate);
            $(this).append("<div class='timeTip'>"+_getTime+"</div>");
        })
        $("body").addClass("loader--inactive");
    }
    $(_table).dataTable({
        dom : 'BTftlpi',
        buttons: ['csv', 'print', 'pdf'],
        language: {
            search: "",
            searchPlaceholder: "Search records"
        },
        "ajax": {
            "type": "post",
            "url": "merchantCreatedBysubaAdmin",
            "data" : {"token" : _token}
            },
            "drawCallback": function( settings ) {
                dateToolTip();
            },
        "aoColumns": [
            {"mDataProp": "payId","sClass": "payId editPermission"},
            {"mData" : "businessName"},
            {"mData" : "status"},
            {"mData" : "mpaStage"},
            {"mData" : "registrationDate","sClass": "registerDate"},
            {"mData" : "createdBy"}  
            
            ]
    });
    $("body").on("click", ".editPermission", function(e){
        var _parent = $(this).closest("tr");
        var _payId = _parent.find(".payId").text();
        $("#hidden").val(_payId);
        document.merchant.submit();
    })
    $("body").on("click",".my_class", function(e){
        var _getPayId = $(this).text();
        $("#hidden").val(_getPayId);
        document.merchant.submit();
    })
});