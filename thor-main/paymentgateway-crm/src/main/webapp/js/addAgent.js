var clearFields = function() {
    var acquirerInput = document.querySelectorAll(".acquirer-input");

    acquirerInput.forEach(function(element) {
        var elementId = element.getAttribute("id");
        element.value = "";

        if(elementId == "EmailIdInpt" || elementId == "phoneIdInpt") {
            element.closest(".common-validation").classList.remove("verify-success");
            element.removeAttribute('readonly');
        }
    });

    $("#btnEditUser").attr('disabled', 'disabled');
}

$("#btnEditUser").on("click", function(e) {
    $("body").removeClass("loader--inactive");
    $.ajax({
        type: "POST",
        url: "addAgent",
        data: {
            firstName: $("#fname").val(),
            lastName: $("#lname").val(),
            businessName: $("#bname").val(),
            emailId: $("#EmailIdInpt").val(),
            mobileNumber: $("#phoneIdInpt").val(),
            token: $("#custom-token").val(),
            "struts.token.name": "token"
        },
        success: function(data) {
            var response = data.responseObject;

            $(".response-message").text("SUCCESS");
			$(".response-message").addClass("success");

            clearFields();

            setTimeout(function() {
                $("body").addClass("loader--inactive");
            }, 1000);

            setTimeout(function() {
                $("#response-block").addClass("d-none");
                $(".response-message").text("");
                $(".response-message").removeClass("success");
                $(".response-message").removeClass("error");
            }, 3000);
        },
        error: function(data) {
            alert("Try again, Something went wrong!");

            setTimeout(function() {
                $("body").addClass("loader--inactive");
            }, 1000);
        }
    });
});