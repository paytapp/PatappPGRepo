$(document).ready(function() {

    $("#dateFrom").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date()
    });
    $("#dateTo").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date()
    });
    
    var today = new Date();
    $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
    $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));

    function loadBookingRecord(){
        var _dataObj = {};
        var datepick = $.datepicker;
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
        // var _getAllInput = document.querySelectorAll(".filter_sub").length;
        $(".bookingRecordInput").each(function(e){
            var _val = $(this).val();
            var _key = $(this).attr("name");
            _dataObj[_key] = _val;
        });
    
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
        $('#myTable').DataTable( {
            destroy: true,
            // dom: 'lfrtip',
            dom : 'BTftlpi',
            buttons: ['csvHtml5', 'pdf'
            ],
            
            "ajax": {
                "type": "post",
                "url": 'viewBookingRecord',
                "data": _dataObj
            },
            // dom: 'lfrtip',
            "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
            "columns": [
                { "data": 'bookingId' },
                { "data": 'contactName' },
                { "data": 'contactNumber' },
                { "data": 'custEmail' },
                { "data": 'contactAddress' },
                { "data": 'contactEmail' },
                { "data": 'hotelName' },
                { "data": 'city' },
                { "data": 'district' },
                { "data": 'bookingDate' },
                { "data": 'checkInDate' },
                { "data": 'tariff' },
                { "data": 'roomsCategory' },
                { "data": 'guestRecord' },
                { "data": 'customFlag' },
                { "data": 'status' }
            ]
        } );
        setTimeout(function(){
            $("body").addClass("loader--inactive");
        }, 500);
    }

    $("#bookingSubmit").on("click", function(e){
        loadBookingRecord();
    });

    // datatable call default
    loadBookingRecord();

});