"use strict";

function decimalCount(number) {
  // Convert to String
  var numberAsString = number.toString(); // String Contains Decimal

  if (numberAsString.indexOf('.') >=0 ) {
    return numberAsString.split('.')[1].length;
  } // String Does Not Contain Decimal

  return 0;
};

function onlyNumericKey(that, e, decimatLimit) {
    var val = that.value;
  
    if (val !== "") {
      if (isNaN(val)) {
        val = val.replace(/[^0-9\.]/g, '');
  
        if (val.split('.').length > decimatLimit) {
          val = val.replace(/\.+$/, "");
        }
      }
  
      var countDecimal = decimalCount(val);
  
      if (countDecimal > decimatLimit) {
        val = Number(val);
        var enteredVal = "0.0";
  
        if (decimatLimit > 0) {
          for (var i = 0; i < decimatLimit - 1; i++) {
            enteredVal = enteredVal + '0';
          }
        }
  
        var newEnteredVal = enteredVal + e.key;
        newEnteredVal = Number(newEnteredVal);
        enteredVal = enteredVal + '0';
  
        if (newEnteredVal >= enteredVal) {
          val = val - newEnteredVal;
        }
  
        that.value = val.toFixed(decimatLimit);
      } else {
        that.value = val;
      }
    }
  };