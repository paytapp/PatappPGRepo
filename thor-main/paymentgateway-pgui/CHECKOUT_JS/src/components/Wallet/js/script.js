import { loaderAction, ajaxRequest } from "../../../js/script";

const wlObj = {
    startTimer: function(elementId, resendBtnId) {
        var sec = 50,
            countDiv = window.id(elementId);
    
        countDiv.classList.remove("d-none");
    
        window.otpInterval = setInterval(function () {        
            secpass();
        }, 1000);
    
        function secpass() {
            var min     = Math.floor(sec / 60),
                remSec  = sec % 60;
            
            if (remSec < 10) {
                remSec = '0' + remSec;
            }
    
            if (min < 10) {
                min = '0' + min;        
            }
    
            countDiv.querySelector("span").innerHTML = min + ":" + remSec;
            
            if (sec > 0) {
                sec = sec - 1;            
            } else {
                clearInterval(window.otpInterval);
                window.id(resendBtnId).classList.remove("d-none");
                countDiv.querySelector("span").innerHTML = '';
                countDiv.classList.add("d-none");
            }
        }
    }
};

export default wlObj;