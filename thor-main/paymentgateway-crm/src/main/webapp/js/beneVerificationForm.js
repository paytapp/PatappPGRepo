$(window).on("load", function() {
    var wrapperPosition = function() {
        var _screenHeight = $(window).innerHeight(),
            _container = $(".container-verify-link"),
            _containerHeight = _container.height();

        if(_screenHeight > _containerHeight) {
            var marTop = (_screenHeight - _containerHeight) / 2 - 31;
            _container.css("margin-top", marTop + "px");
        } else {
            _container.addClass("my-30")
        }
    }

    wrapperPosition();
});

// REMOVE ERROR MESSAGE LABEL
var removeLabelErrorMsg = function(that) {
    var elementId = that.getAttribute("id");

    document.querySelector('[data-error="'+ elementId +'"]').innerHTML = "";
}

var onlyNumberInput = function(that) {
    that.value = that.value.replace(/[^0-9]/g, '');

    if(that.value !== "") {
        removeLabelErrorMsg(that);
    }
}

var onlyAlpha = function(that) {
    if(that.value == " ") {
        that.value = "";
    } else {
        that.value = that.value.replace(/[^a-zA-Z &.]/g, '');
    }

    if(that.value !== "") {
        removeLabelErrorMsg(that);
    }
}

var _uppercase = function(that) {
    var _val = that.value;
    _val = _val.toUpperCase();
    that.value = _val;
}

var onlyAlphaNumeric = function(that) {
    that.value = that.value.replace(/[^a-zA-Z0-9]/g, '');

    if(that.value !== "") {
        removeLabelErrorMsg(that);
    }
}

var validateVerifyAccount = {
    phoneNumber : $("#phoneNo"),
    bankAccountName : $("#bankAccountName"),
    bankAccountNumber : $("#bankAccountNumber"),
    bankIfsc : $("#bankIfsc"),
    validatePhone : function() {
        var $phoneNumber = this.phoneNumber.val();
        if($phoneNumber !== "") {
            if($phoneNumber.length !== 10) {
                $('[data-error="phoneNo"]').html("Please enter valid mobile number.");
                return false;
            }
        } else {
            $('[data-error="phoneNo"]').html("Please enter mobile number.");
            return false;
        }

        return true;
    },
    validateAccountName: function() {
        var $accountName = this.bankAccountName.val();

        if($accountName == "") {
            $('[data-error="bankAccountName"]').html("Please enter account holder name.");
            return false;
        }
        return true;
    },
    validateAccountNumber: function() {
        var $accountNumber = this.bankAccountNumber.val();

        if($accountNumber == "") {
            $('[data-error="bankAccountNumber"]').html("Please enter account number.");
            return false;
        }

        return true;
    },
    validateIfsc: function() {
        var $bankIfsc = this.bankIfsc.val();

        if($bankIfsc == "") {
            $('[data-error="bankIfsc"]').html("Please enter IFSC Code.");
            return false;
        }

        return true;
    }
}

$("#verifyBeneForm").on("submit", function(e) {
    // e.preventDefault();

    var flag = validateVerifyAccount.validatePhone();
        flag = flag && validateVerifyAccount.validateAccountName();
        flag = flag && validateVerifyAccount.validateAccountNumber();
        flag = flag && validateVerifyAccount.validateIfsc();

    if(flag) {
        return true;
    }
    
    return false;
});