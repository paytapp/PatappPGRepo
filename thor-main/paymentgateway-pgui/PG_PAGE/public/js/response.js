$(window).on("load", function() {
    const wrapperPosition = _ => {
        let _screenHeight = $(window).innerHeight(),
            _container = $(".custom-container"),
            _containerHeight = _container.height();

        if(_screenHeight > _containerHeight) {
            _container.css("margin-top", (_screenHeight - _containerHeight) / 2 + "px");
        } else {
            _container.addClass("my-30");
        }
    }

    const fetchData = obj => {
        document.getElementById("response-container").classList.remove("d-none");
        document.querySelector("body").classList.add("loader--inactive");
        let summaryList = document.querySelectorAll(".data-display");
        
        summaryList.forEach(element => {
            let elementId = element.getAttribute("id"),
                statusResult = document.querySelector(".statusResult"),
                cardBox = document.querySelector(".card_box");

            if(obj[elementId] !== null && obj[elementId] !== "" && obj[elementId] !== undefined) {                
                if(elementId == "TOTAL_AMOUNT") {
                    element.innerHTML = (Number(obj[elementId]) / 100).toFixed(2);
                } else if(elementId == "STATUS") {
                    if(obj[elementId] == "AUTHENTICATION_FAILED" || obj[elementId] == "Cancelled" || obj[elementId] == "Invalid") {
                        statusResult.innerHTML = "Payment Failed";
                        cardBox.classList.add("failedMsg");
                    } else if(obj[elementId] == "Captured" || obj[elementId] == "Success") {
                        statusResult.innerHTML = "Payment Successful";
                        cardBox.classList.add("successMsg");
                    } else if(obj[elementId] == "Pending") {
                        statusResult.innerHTML = "Payment Pending";
                        cardBox.classList.add("pendingMsg");
                    } else {
                        statusResult.innerHTML = "Payment Failed";
                        cardBox.classList.add("failedMsg");
                    }

                    element.innerHTML = obj[elementId];
                } else {
                    element.innerHTML = obj[elementId];
                }
            } else {                
                element.closest("li").classList.add("d-none");
                element.closest("li").classList.remove("d-flex");
            }
        });

        wrapperPosition();
    }

    try {
        var responseObj = $("#responseObj").val();    
        responseObj = JSON.parse(responseObj);
    
        fetchData(responseObj);
    } catch(e) {
        console.error(e);
    }
});