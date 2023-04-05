var _id = document.getElementById.bind(document);

var updateOrderId = function() {
    _id("orderId").value = "LP" + String(new Date().getTime()); //	Autopopulating orderId
}

var onlyNumberInput = function(that) {
    that.value = that.value.replace(/[^0-9]/g, '');
}

var _uppercase = function(that) {
    var _val = that.value;
    _val = _val.toUpperCase();
    that.value = _val;
}

var onlyAlphaNumeric = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9]/g, '');
}

var onlyAlpha = function(that) {
    if(that.value == " ") {
        that.value = "";
    } else {
        that.value = that.value.replace(/[^a-zA-Z &.]/g, '');
    }
}

var validateEmail = function(that) {
    if(that.value.match(/\s/g)) {
        that.value = that.value.replace(/\s/g,'');
    }
}

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

if (typeof module != 'undefined' && module.exports) module.exports = Sha256; // = export default Sha256

var genrateHash = function(form) {
    var inputElements = form.getElementsByClassName("input-box");

    var valueArray = new Array();
    var sortedArray = new Array();
    var nameArray = [];

    for(i = 0; i < inputElements.length; i++) {
        valueArray[inputElements[i].name] = inputElements[i].value;
        nameArray[i] = inputElements[i].name;
    }

    nameArray.sort();
    var inputString = "";

    for(j = 0; j < nameArray.length; j++) {
        var element = nameArray[j];
        inputString += "~";
        inputString += element;
        inputString += "="
        inputString += valueArray[element];
    }

    inputString = inputString.substr(1);
    inputString += _id("tempPayId").value;
    
    var hash = Sha256.hash(inputString).toUpperCase();
	_id("hashKey").value = hash;
}

var updateAmount = function() {
    var amount = _id("amount");
    var _val = amount.value;
    _id("finalAmount").value = _val * 100;
    _id("amount").removeAttribute("name");
}

// GLOBAL VARIABLES
var _querySelector = document.querySelector.bind(document);

// LOADER ACTIVE / INACTIVE
var loaderAction = function(actionName) {
    if(actionName == "show") {
        _querySelector("body").classList.remove("loader--inactive");
    } else {
        _querySelector("body").classList.add("loader--inactive");
    }
}

// HIDE LOADER ON PAGE LOAD
window.addEventListener("load", loaderAction("hide"));
