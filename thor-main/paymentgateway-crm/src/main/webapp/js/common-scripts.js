$(document).ready(function(e){
    var today = new Date();
    var _isDateTo = $("#dateTo");
    var _isDateFrom = $("#dateFrom");
    if(_isDateTo.length > 0){
        $("#dateTo").datepicker({
            prevText : "click for previous months",
            nextText : "click for next months",
            showOtherMonths : true,
            changeMonth : true,
            changeYear : true,
            dateFormat : 'dd-mm-yy',
            selectOtherMonths : false,
            maxDate : new Date()
        });
	    $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
    }
    if(_isDateFrom.length){
        $("#dateFrom").datepicker({
            prevText : "click for previous months",
            nextText : "click for next months",
            showOtherMonths : true,
            changeMonth : true,
            changeYear : true,
            dateFormat : 'dd-mm-yy',
            selectOtherMonths : false,
            maxDate : new Date(),
            onClose: function(date) {
                $("#dateTo").datepicker(
                    "change",
                    { minDate: $('#dateFrom').val() }
                );
                $("#dateTo").focus();
            },
        });
        $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
    }
})