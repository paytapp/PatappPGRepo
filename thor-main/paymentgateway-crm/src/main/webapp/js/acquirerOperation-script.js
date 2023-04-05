
$(".lpay-nav-link").on("click", function(e){
    var _this = $(this).attr("data-id");
    $(".lpay-nav-item").removeClass("active");
    $(this).closest(".lpay-nav-item").addClass("active");
    $(".lpay_tabs_content").addClass("d-none");
    $("[data-target="+_this+"]").removeClass("d-none");
})

function downloadForm(_that){
    var _getInput = document.querySelectorAll("[data-var]");
    var _input = "";
    var _checkButton = _that.getAttribute("data-click");
    if(_checkButton == "download"){
        document.querySelector("#downloadSBI").setAttribute("action", "downloadOperationalSBIReportAction");
    }else{
        document.querySelector("#downloadSBI").setAttribute("action", "downloadSBISecondReportAction");
    }
    _getInput.forEach(function(index, array, element){
        _input += "<input type='hidden' name='"+index.getAttribute("data-var")+"' value='"+index.value+"' />"
    })
    document.querySelector("#downloadSBI").innerHTML = _input;

    document.querySelector("#downloadSBI").submit();
}

