var updateValue = function(that, decimatLimit) {
    // let val = Number(that.value);
    // if(val !== "") {
    //     that.value = val.toFixed(decimatLimit);
    // }
}

function decimalCount(number) {
    // Convert to String
    var numberAsString = number.toString(); // String Contains Decimal
  
    if (numberAsString.indexOf('.') >=0 ) {
      return numberAsString.split('.')[1].length;
    } // String Does Not Contain Decimal
  
    return 0;
};

function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32 || x == 46) {
    } else {
        event.preventDefault();
    }
}

function onlyNumericKey(that, e, decimatLimit) {
    var val = that.value;
  
    if (val !== "") {
      if(decimatLimit > 0) {
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
      } else {
        that.value = val.replace(/[^0-9\.]/g, '');
      }
    }
};

var limitAmount = function(that, e) {
    var _val = that.value;
  
    if(Number(_val) == 0 || _val == ".") {
        that.value = "";
    } else if(Number(_val) > 9999999 || Number(_val) > 9999999.00) {
        that.value = 9999999.00
    }
}

//  METHOD FOR ALLOWING ZERO BUT NOT POINT VALUE
var limitAmount_alt = function(that, e) {
  var _val = that.value;


  if(_val == ".") {
      that.value = "";
  }
}

function setDecimal(_this, e){
  var _val = _this.value;
  if(_val.length == 1){
    if(_val == "."){
      _this.value = "0" + _val;
    }
  }
}