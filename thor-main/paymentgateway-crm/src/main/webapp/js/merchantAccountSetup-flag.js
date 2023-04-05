function checkLength(e, msg){
    var _getReg = e.getAttribute("data-reg");
    var _value = e.value;
    if(_value != "") {
        var _newReg = new RegExp(_getReg);
        if(_newReg.test(_value) == true){
            e.classList.remove("red-line");
            e.removeAttribute("data-error");
        }else{
            e.classList.add("red-line");
            e.setAttribute("data-error", "Invalid "+msg);
            e.closest(".lpay_input_group").querySelector(".error-msg").innerText = ""
        }
    } else {
        e.classList.remove("red-line");
        e.removeAttribute("data-error");
        e.closest(".lpay_input_group").querySelector(".error-msg").innerText = "";
    }
}


function removeError(_that){
    _that.classList.remove("red-line");
}


function removeErrorBlank(_that){
    if(_that.value == ""){
        _that.classList.remove("red-line");
    }
}

// company name validator

function numeric(that){
    that.value = that.value.replace(/[^0-9]/g, '');
}

function alphaNumericAlt(e) {
    var _alpha = /^[A-Za-z0-9]/;
    var _this = e.value;
    if(!_alpha.test(_this)) {
        e.value = _this.slice(0, _this.length-1);
    }
}

