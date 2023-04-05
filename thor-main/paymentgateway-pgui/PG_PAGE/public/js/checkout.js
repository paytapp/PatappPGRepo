var _id = document.getElementById.bind(document),
    _querySelector = document.querySelector.bind(document),
    _createElement = document.createElement.bind(document);

if (typeof module != 'undefined' && module.exports) module.exports = Sha256; // = export default Sha256

var genrateHash = function(form) {
    var inputElements = form.getElementsByClassName("checkout-input"),
        valueArray = new Array(),
        nameArray = [],
        inputString = "";

    for(i = 0; i < inputElements.length; i++) {
        valueArray[inputElements[i].name] = inputElements[i].value;
        nameArray[i] = inputElements[i].name;
    }

    nameArray.sort();

    for(j = 0; j < nameArray.length; j++) {
        var element = nameArray[j];
        inputString += "~";
        inputString += element;
        inputString += "="
        inputString += valueArray[element];
    }

    inputString = inputString.substr(1);
    inputString += _id("SECRET_KEY").value;

	_id("HASH").value = Sha256.hash(inputString).toUpperCase();
}

// var updateOrderId = function() {
//     _id("ORDER_ID").value = "LP" + String(new Date().getTime()); //	Autopopulating orderId
// }

var submitHandler = function(that) {
    // updateOrderId();
    genrateHash(that);

    document.querySelector("body").classList.add("checkout-popup--active");
    loaderAction({showLoader: true});
    return true;
}

// ******************************************************************
// ************************* CHECKOUT FRAME *************************
// ******************************************************************
var generateFrame = function() {
    var popupOverlay = _createElement("div");
        popupOverlay.setAttribute("id", "checkout-popup-overlay");
    
    var popupInner = _createElement("div");
        popupInner.setAttribute("id", "checkout-popup-inner");

    var checkoutLoader = _createElement("div");
        checkoutLoader.setAttribute("id", "checkout-loader");
    
    var loaderImg = _createElement("img");
        loaderImg.setAttribute("src", `${window.basePath}/img/loader.gif`);
    
    checkoutLoader.appendChild(loaderImg);
    popupOverlay.appendChild(checkoutLoader);

    var _frame = _createElement("iframe");
    _frame.setAttribute("id", "checkout-iframe");
    _frame.setAttribute("name", "checkout-iframe");
    _frame.setAttribute("frameborder", 0);
    _frame.setAttribute("allowpaymentrequest", true);

    popupInner.appendChild(_frame);

    popupOverlay.appendChild(popupInner);

    document.querySelector("body").appendChild(popupOverlay);
}

var loaderAction = function(status) {
    if(status.showLoader) {
        document.querySelector("body").classList.add("checkout-loader--active");
    } else {
        document.querySelector("body").classList.remove("checkout-loader--active");
    }
}

var iframeAction = function(status) {
    if(status.showIframe) {
        document.querySelector("body").classList.add("checkout-iframe--active");
    } else {
        document.querySelector("body").classList.remove("checkout-iframe--active");
    }
}

var checkoutHandler = function(obj) {
    if(obj.isPageVisible) {
        iframeAction({showIframe: true});
        loaderAction({showLoader: false});
    }
}

var closeIframe = function() {
    var _body = document.querySelector("body");
    _body.classList.remove("checkout-popup--active");
}

window.addEventListener("load", function() {
    generateFrame();
});

var submitResponseForm = function(obj) {
    var restextFields = "";

    for(var key in obj) {
        restextFields += '<input type="hidden" name="'+ key +'" id="'+ key +'" value="'+ obj[key] +'" />';
    }

    _id("responseForm").innerHTML = restextFields;

    _id("responseForm").submit();
}

window.addEventListener("message", function(event) {
    var obj = event.data;

    if(obj.closeIframe) {
        closeIframe();
        submitResponseForm(obj);
    } else {
        checkoutHandler(obj);
    }
});