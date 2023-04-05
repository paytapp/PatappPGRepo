$(window).on("load", function() {
    var _id = document.getElementById.bind(document),
        response = $("#response").val(),
        RESPONSE_MESSAGE = $("#RESPONSE_MESSAGE").val();

    switch (response) {
        case "failed":
            $(".statusResult").html("Bank Account Verification Failed");
            $(".card_box").addClass("failedMsg");
        break;

        case "success":
            $(".statusResult").html("Bank Account Verification Successfull");
            $(".card_box").addClass("successMsg");
        break;

        default:
            $(".statusResult").html("Bank Account Verification Failed");
            $(".card_box").addClass("failedMsg");
    }
   
    // REMOVE LOADER
    $("body").addClass("loader--inactive");

    document.querySelector(".card_box").style.display = "block";

    var wrapperPosition = function() {
        var _screenHeight = $(window).innerHeight(),
            _container = $(".custom-container"),
            _containerHeight = _container.height();

        if(_screenHeight > _containerHeight) {
            _container.css("margin-top", (_screenHeight - _containerHeight)  / 2 + "px");
        } else {
            _container.addClass("my-30")
        }
    }

    wrapperPosition();
});