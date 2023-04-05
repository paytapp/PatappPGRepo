$(window).on("load", function() {
    $("body").removeClass("loader--inactive");
    setTimeout(function() {
        var _createDiv = "<div id='registerDate'></div>";
        $("#datatable").find("tr").each(function() {
            $(this).find("td").eq(6).addClass("registerDate");
            $(this).find(".registerDate").attr("data-input", "");
            var _getThisVal = $(this).find(".registerDate").text();
            var _getIndexOf = _getThisVal.indexOf(" ");
            var _getTime = _getThisVal.slice(_getIndexOf);
            var _getDate = _getThisVal.slice(0, _getIndexOf);
            $(this).find(".registerDate").attr("data-input", _getTime);
            $(this).find(".registerDate").text(_getDate);
            var _getThisTime = $(this).find(".registerDate").attr("data-input");
            $(this).find("td").eq(6).append(_createDiv);
            $(this).find("#registerDate").text(_getThisTime);
            $("body").addClass("loader--inactive");
        });
    }, 500);
});

