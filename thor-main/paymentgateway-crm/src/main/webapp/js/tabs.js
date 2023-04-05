$(document).ready(function(e){

    document.querySelector(".filter-icon").onclick = function(e){
        var _checkIfClass = e.target.closest("section").classList.toString().indexOf("active-slide-element");
        if(_checkIfClass == -1){
            e.target.closest("section").classList.add("active-slide-element");
        }else{
            e.target.closest("section").classList.remove("active-slide-element");
        }
    }



    var _orderId = document.querySelector("[name='orderId']");
    if(_orderId != null){
        _orderId.removeAttribute("onkeypress");
        _orderId.onkeyup = function(e){
            var _this = e.target.value;
            var regex = /^[a-zA-Z0-9!@\&*\)\(+=.?/':_-]+$/g;
            if(regex.test(e.target.value) == true){
            }else{
                e.target.value = _this.slice(0, _this.length-1);
            }
        }
    }

})