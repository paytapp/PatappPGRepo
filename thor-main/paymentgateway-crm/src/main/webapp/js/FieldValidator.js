"use strict";

function _instanceof(left, right) { if (right != null && typeof Symbol !== "undefined" && right[Symbol.hasInstance]) { return !!right[Symbol.hasInstance](left); } else { return left instanceof right; } }

function _classCallCheck(instance, Constructor) { if (!_instanceof(instance, Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function sendInvoice(){
  $("body").removeClass("loader--inactive");
  var dataObj = {};
    var token = $("[name='token']").val();
    dataObj = {
      token : token,
    }
      $(".textFL_merch").each(function(i){
        var _key = $(this).attr("name");
        if(_key == ""){
        }else{
          if($(this).val() == ""){
          }else{
            dataObj[_key] = $(this).val();
          }
        }
      })

      $.ajax({
        url : 'saveInvoice',
        type : 'post',
        timeout: 0,
        data :  dataObj,
        success : function(data) {
          console.group("invoice done");
          console.groupEnd("invoice end");
          var invoiceLink=$('#invoiceLink').val(data.shortUrl);
          if(invoiceLink!=''){
            $('#copyBtn').removeClass('d-none');
            $('#invoiceLink').removeClass('d-none');
            $('#btnSave').addClass('d-none');
            $('#btnReset').removeClass('d-none');
            $("#UDF_div").html("");
          }
          $("body").addClass("loader--inactive");
        },
        error : function(data) {
          $("body").addClass("loader--inactive");
          // alert("Something went wrong, so please try again.");
        }
      });
}

var FieldValidator =
/*#__PURE__*/
function () {
  function FieldValidator(x) {
    _classCallCheck(this, FieldValidator);

    this.x = x;
  }

  _createClass(FieldValidator, null, [{
    key: "valdInvoiceNo",
    value: function valdInvoiceNo(errMsgFlag) {
      var invoiceexp = /^[0-9a-zA-Z-/]+$/;

      var invoiceElement = _id("invoiceNo");

      var invoiceValue = invoiceElement.value;

      if (invoiceValue.trim() != "") {
        if (!invoiceValue.match(invoiceexp)) {
          FieldValidator.addFieldError("invoiceNo", "Enter valid Invoice no.", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('invoiceNo');
          return true;
        }
      } else {
        FieldValidator.addFieldError("invoiceNo", "Please enter Invoice No.", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "valdPhoneNo",
    value: function valdPhoneNo(errMsgFlag, event) {
      var _innerFunc = function() {
        var phoneElement = _id("phone");
  
        var value = phoneElement.value.trim();
  
        if (value.length > 0) {
          var phone = phoneElement.value;
          var phoneexp = /^[0][1-9]\d{9}$|^[1-9]\d{9}$/g;
  
          if (!phone.match(phoneexp)) {
            FieldValidator.addFieldError("phone", "Enter valid phone no.", errMsgFlag);
            return false;
          } else {
            FieldValidator.removeFieldError('phone');
            return true;
          }
        } else {
          FieldValidator.addFieldError("phone", "Enter phone no.", errMsgFlag);
          return false;
        }
      }

      var _emailId = _id("emailId").value;

      if(event == undefined || (event !== undefined && event.key !== "Tab")) {
        return _innerFunc();
      } else if (event !== undefined && event.key == "Tab" && _emailId == "") {
        return _innerFunc();
      }
    }
  }, {
    key: "valdProductName",
    value: function valdProductName(errMsgFlag) {
      var productNameElement = _id("productName");

      var value = productNameElement.value.trim();

      if (value.length > 0) {
        var productName = productNameElement.value;
        var regex = /^[ A-Za-z0-9@./-]*$/;

        if (!productName.match(regex)) {
          FieldValidator.addFieldError("productName", "Enter valid product name", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('productName');
          return true;
        }
      } else {
        console.log("hello sunil");
        FieldValidator.addFieldError("productName", "Enter product name", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "valdProductDesc",
    value: function valdProductDesc(errMsgFlag) {
      var productDescElement = _id("productDesc");

      var value = productDescElement.value.trim();

      if (value.length > 0) {
        var productDesc = productDescElement.value;
        var regex = /^[ A-Za-z0-9@./-]*$/;

        if (!productDesc.match(regex)) {
          FieldValidator.addFieldError("productDesc", "Enter valid product description", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('productDesc');
          return true;
        }
      } else {
        FieldValidator.removeFieldError('productDesc');
        return true;
      }
    }
  }, {
    key: "valdCurrCode",
    value: function valdCurrCode(errMsgFlag) {
      var currencyCodeElement = _id("mappedCurrency");

      if (currencyCodeElement.value == "Select Currency") {
        FieldValidator.addFieldError("mappedCurrency", "Select Currency Type", errMsgFlag);
        return false;
      } else {
        FieldValidator.removeFieldError('mappedCurrency');
        return true;
      }
    }
  }, {
    key: "valdMerchant",
    value: function valdMerchant(errMsgFlag) {

      var element = _id("merchant");

      if (element != null) {
        if (element.value != "Select Merchant") {
          FieldValidator.removeFieldError('merchant');
          return true;
        } else {
          FieldValidator.addFieldError("merchant", "Select Merchant", errMsgFlag);
          return false;
        }
      } else {
        return true;
      }
    }
  }, {
    key: "valdRecptMobileNo",
    value: function valdRecptMobileNo(errMsgFlag) {
      var recipientMobileElement = _id("recipientMobile");

      if (recipientMobileElement.value.trim().length > 0) {
        var recipientMobile = recipientMobileElement.value;
        var phoneexp = /^[0-9]{10,15}$/;

        if (!recipientMobile.match(phoneexp)) {
          FieldValidator.addFieldError("recipientMobile", "Enter valid mobile no", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('recipientMobile');
          return true;
        }
      } else {
        FieldValidator.addFieldError("recipientMobile", "Enter recipient mobile no", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "valdRecptMsg",
    value: function valdRecptMsg(errMsgFlag) {
      var messageBodyElement = _id("messageBody");

      if (messageBodyElement.value.trim().length > 0) {
        if (!messageBodyElement.value.length > 2) {
          FieldValidator.addFieldError("messageBody", "Please enter valid message", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('messageBody');
          return true;
        }
      } else {
        FieldValidator.addFieldError("messageBody", "Enter message", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "valdExpDayAndHour",
    value: function valdExpDayAndHour(errMsgFlag) {
      var expDayElement = _id("expiresDay");

      var _val = expDayElement.value; // var expHorElement = _id("expiresHour");
      // var hors = expHorElement.value.trim();

      if (_val.length == 16) {
        var strVal = _val.split(" ");

        var days = strVal[0].trim();

        if (days.length > 0 && parseInt(days) >= 0) {
          var dateTo = days.split("-"),
              myDateTo = new Date(dateTo[2], dateTo[1] - 1, dateTo[0]),
              //Year, Month, Date
          myDateFrom = new Date(),
              oneDay = 24 * 60 * 60 * 1000; // hours*minutes*seconds*milliseconds;

          var diffDays = Math.round(Math.abs((myDateFrom.getTime() - myDateTo.getTime()) / oneDay));
          /* if(diffDays > 31) {
              FieldValidator.addFieldError("expiresDay", "Enter valid no. of days (Max:31)", errMsgFlag);
              return false;
          } */

          FieldValidator.removeFieldError('expiresDay'); // if(hors.length > 0 && parseInt(hors)>=0) {
          // 	if(parseInt(hors) > 23 || parseInt(hors)<0) {
          // 		FieldValidator.addFieldError("expiresHour", "Enter valid no. of hours (Max:24)", errMsgFlag);
          // 		return false;
          // 	} else if(parseInt(days)==0 &&parseInt(hors)==0) {
          //         FieldValidator.addFieldError("expiresDay", "Enter valid no. of days (Max:31) or hours (Max:24)", errMsgFlag);
          //         return false;
          //     }
          // 	FieldValidator.removeFieldError('expiresHour');
          // 	return true;
          // } else {
          //     FieldValidator.addFieldError("expiresHour", "Enter valid no. of hours", errMsgFlag);
          //     return false;
          // }

          return true;
        }
      } else {
        FieldValidator.addFieldError("expiresDay", "Enter valid no. of days", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "valdMerchant",
    value: function valdMerchant(errMsgFlag) {
      var element = _id("merchant");

      if (element != null) {
        if (element.value != "Select Merchant") {
          FieldValidator.removeFieldError('merchant');
          return true;
        } else {
          FieldValidator.addFieldError("merchant", "Select Merchant", errMsgFlag);
          return false;
        }
      } else {
        return true;
      }
    }
  }, {
    key: "trimZero",
    value: function trimZero(_element) {
      var _splittedValue = _element.value.split("");      

      if(_element.value == "00") {
        _element.value = "0";
      } else {
        if(_splittedValue[0] == "0" && _splittedValue[1] !== "0" && _splittedValue[1] !== "." && _element.value.length == 2) {
          _element.value = _element.value.slice(1);
        }
      }
      return true;
    }
  }, {
    key: "valdSrvcChrg",
    value: function valdSrvcChrg(errMsgFlag) {
      var element = _id('serviceCharge');
      
      if(element.value !== "") {
        var result = FieldValidator.trimZero(element);

        if(result) {
          var value = parseFloat(element.value.trim());
          if (element.value.indexOf(".") > -1) {
            var index = element.value.indexOf(".");
  
            var _decimalCount = element.value.substr(index, element.value.length).length;
    
            if (_decimalCount > 3 || _decimalCount < 2) {
              FieldValidator.addFieldError("serviceCharge", "Enter valid Service Charge", errMsgFlag);
              return false;
            }
          }
          
          if (parseFloat(value) >= parseFloat(0)) {
            FieldValidator.removeFieldError('serviceCharge');
            return true;
          } else {
            FieldValidator.addFieldError("serviceCharge", "Enter valid Service Charge", errMsgFlag);
            return false;
          }
        }
      } else {
        return true;
      }
    } //valdiating the amount of the product

  }, {
    key: "valdAmount",
    value: function valdAmount(errMsgFlag) {
      var element = _id('amount');

      var result = FieldValidator.trimZero(element);

      if(result) {
        var value = parseFloat(element.value.trim());
  
        if (element.value.indexOf(".") > -1) {
          var index = element.value.indexOf(".");
  
          var _decimalCount = element.value.substr(index, element.value.length).length;
  
          if (_decimalCount > 3 || _decimalCount < 2) {
            FieldValidator.addFieldError("amount", "Enter valid amount", errMsgFlag);
            return false;
          }
        }
  
        if (!value < parseFloat(value)) {
          // change here for custom amount
          FieldValidator.removeFieldError('amount');
          return true;
        } else {
          FieldValidator.addFieldError("amount", "Enter amount", errMsgFlag);
          return false;
        }
      }
    }
  }, {
    key: "valdEmail",
    value: function valdEmail(errMsgFlag, event) {
      var _innerFunc = function() {
        var emailRegex = /^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$/;

        var element = _id("emailId");

        var value = element.value.trim();

        if (value.length > 0) {
          if (!value.match(emailRegex)) {
            FieldValidator.addFieldError('emailId', "Enter valid email address", errMsgFlag);
            return false;
          } else {
            FieldValidator.removeFieldError('emailId');
            return true;
          }
        } else {
          FieldValidator.addFieldError('emailId', "Enter email address", errMsgFlag);
          return false;
        }
      }

      var _phone = _id("phone").value;

      if(event == undefined || (event !== undefined && event.key !== "Tab")) {
        return _innerFunc();
      } else if (event !== undefined && event.key == "Tab" && _phone == "") {
        return _innerFunc();
      }
    }
  }, {
    key: "valdName",
    value: function valdName(errMsgFlag) {
      var nameRegex = /^[a-zA-Z ]+$/;

      var element = _id("name");

      var value = element.value.trim();

      if (value.length > 0) {
        if (!value.match(nameRegex)) {
          FieldValidator.addFieldError('name', "Enter valid name", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('name');
          return true;
        }
      } else {
        FieldValidator.addFieldError('name', "Enter name", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "validateEmailPhone",
    value: function validateEmailPhone(errMsgFlag, event) {
      var _innerFunc = function() {
        var _phone = _id("phone").value;
        var _email = _id("emailId").value;

        var phoneexp = /^[0-9]{10,15}$/;
  
        if(_phone == "" && _email == "") {
          error_snackbar.innerHTML = "Please fill any one email or phone number.";
          showSnackbar("error-snackbar");

          FieldValidator.valdEmail(true);
          FieldValidator.valdPhoneNo(true);

          return false;
        } else if (_phone !== "" || _email == "" && FieldValidator.valdEmail(true)) {
          FieldValidator.removeFieldError('emailId');
          
          if(_phone.length != "10") {
            FieldValidator.valdPhoneNo(true);
            return false;
          } else {
            FieldValidator.removeFieldError('phone');
            return true;
          }
        } else if (_phone == "" || _email !== "") {
          FieldValidator.removeFieldError("phone");
          return true;
        }
      }

      if(event == undefined || (event !== undefined && event.key !== "Tab")) {
        return _innerFunc();
      }

      return true;
    }
  }, {
    key: "valdCountry",
    value: function valdCountry(errMsgFlag) {
      var nameRegex = /^[a-zA-Z ]+$/;

      var element = _id("country");

      var value = element.value.trim();

      if (value.length > 0) {
        if (!value.match(nameRegex)) {
          FieldValidator.addFieldError('country', "Enter valid country", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('country');
          return true;
        }
      } else {
        FieldValidator.removeFieldError('country');
        return true;
      }
    }
  }, {
    key: "valdCity",
    value: function valdCity(errMsgFlag) {
      var nameRegex = /^[a-zA-Z ]+$/;

      var element = _id("city");

      var value = element.value.trim();

      if (value.length > 0) {
        if (!value.match(nameRegex)) {
          FieldValidator.addFieldError('city', "Enter valid city", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('city');
          return true;
        }
      } else {
        FieldValidator.removeFieldError('city');
        return true;
      }
    }
  }, {
    key: "valdState",
    value: function valdState(errMsgFlag) {
      var nameRegex = /^[a-zA-Z ]+$/;

      var element = _id("state");

      var value = element.value.trim();

      if (value.length > 0) {
        if (!value.match(nameRegex)) {
          FieldValidator.addFieldError('state', "Enter valid state", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('state');
          return true;
        }
      } else {
        FieldValidator.removeFieldError('state');
        return true;
      }
    }
  }, {
    key: "valdZip",
    value: function valdZip(errMsgFlag) {
      var zipRegex = "^[a-zA-Z0-9]{5,6}$";

      var element = _id("zip");

      var value = element.value.trim();

      if (value.length > 0) {
        if (!value.match(zipRegex)) {
          FieldValidator.addFieldError('zip', 'Enter valid zip code', errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('zip');
          return true;
        }
      } else {
        FieldValidator.removeFieldError('zip');
        return true;
      }
    }
  }, {
    key: "valdAddress",
    value: function valdAddress(errMsgFlag) {
      console.log("hello");
      
      var addRegex = /^[a-zA-Z0-9 -/() .,@;:# \r\n]+$/; //-/() .,@;:# \r\n

      var element = _id("address");

      var value = element.value.trim();

      if (value.length > 0) {
        if (!value.match(addRegex)) {
          FieldValidator.addFieldError('address', "Enter valid address", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('address');
          return true;
        }
      } else {
        FieldValidator.removeFieldError('address');
        return true;
      }
    }
  }, {
    key: "valdQty",
    value: function valdQty(errMsgFlag) {
      var qtyRgx = "^([1-9][0-9]+|[1-9])$";

      var element = _id("quantity");

      var value = element.value.trim();

      if (value == 0) {
        FieldValidator.addFieldError('quantity', "Enter valid quantity", errMsgFlag);
        return false;
      }

      if (value.length > 0) {
        if (!value.match(qtyRgx)) {
          FieldValidator.addFieldError('quantity', "Enter valid quantity", errMsgFlag);
          return false;
        } else {
          FieldValidator.removeFieldError('quantity');
          return true;
        }
      } else {
        FieldValidator.addFieldError('quantity', "Enter valid quantity", errMsgFlag);
        return false;
      }
    }
  }, {
    key: "valdAllFields",
    value: function valdAllFields() {
      var flag = FieldValidator.valdMerchant(true);
      // flag = flag && FieldValidator.valdInvoiceNo(true);
      flag = flag && FieldValidator.valdName(true);

      var emailId = $("#emailId").val();
      if(emailId != "") {
        if(!FieldValidator.valdEmail(true)){
          flag = false;
        }else {
          flag = true;
        }
      }

      flag = flag && FieldValidator.validateEmailPhone(true);


      // flag = flag && FieldValidator.valdPhoneNo(true);
      // flag = flag && FieldValidator.valdEmail(true);

      flag = flag && FieldValidator.valdAddress(true); // flag = flag && FieldValidator.valdCountry(true);
      // flag = flag && FieldValidator.valdState(true);

      flag = flag && FieldValidator.valdCity(true);
      flag = flag && FieldValidator.valdZip(true);
      flag = flag && FieldValidator.valdProductName(true);
      flag = flag && FieldValidator.valdProductDesc(true);
      flag = flag && FieldValidator.valdExpDayAndHour(true);
      flag = flag && FieldValidator.valdCurrCode(true);
      flag = flag && FieldValidator.valdQty(true);
      flag = flag && FieldValidator.valdAmount(true);
      flag = flag && FieldValidator.valdSrvcChrg(true);
      //flag = flag && FieldValidator.valdRecptMobileNo(true);
      //flag = flag && FieldValidator.valdRecptMsg(true);
      //submitting form

      if (flag ) {

        var _getList = $(".textFL_merch_invalid");
        
        if(_getList.length == 0){

          

          if($("#emailId").val() != "" && $("#phone").val() == ""){
  
            $("#footer-html").dialog({
  
              modal: true,
  
              draggable: false,
  
              resizable: false,
  
              show: 'blind',
  
              hide: 'blind',
  
              width: 290,
  
              height: 72,
  
              buttons: [{
  
                text: "Send Email",
  
                click: function click() {
  
                  _id("emailCheck").value = "true";
  
                  
  
                  sendInvoice();
  
                  // document.forms["frmInvoice"].submit();
  
                  $(this).dialog("close");
  
                }
  
              }],
  
              open: function open() {

                $('.ui-dialog-buttonpane').find('button').removeClass('ui-button ui-corner-all ui-widget');

                  $('.ui-dialog-buttonpane').find('button').addClass('lpay_button lpay_button-md lpay_button-secondary');
  
                $("#footer-html").css("overflow", "hidden");
  
                $('.ui-dialog-titlebar-close').addClass('ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only');
  
                $('.ui-dialog-titlebar-close').append('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span><span class="ui-button-text">close</span>');
  
              },
  
              dialogClass: 'ui-dialog-osx'
  
            });
  
          }
  
          else if($("#emailId").val() == "" && $("#phone").val() != ""){
  
            $("#footer-html").dialog({
  
              modal: true,
  
              draggable: false,
  
              resizable: false,
  
              show: 'blind',
  
              hide: 'blind',
  
              width: 290,
  
              height: 72,
  
              buttons: [{
  
                text: "Send SMS",
                "class": "lpay_button",

  
                click: function click() {
  
                  _id("smsCheck").value = "true";
  
                  sendInvoice();
  
                  // document.forms["frmInvoice"].submit();
  
                  $(this).dialog("close");
  
                }
  
              }],
  
              open: function open() {

                $('.ui-dialog-buttonpane').find('button').removeClass('ui-button ui-corner-all ui-widget');

                  $('.ui-dialog-buttonpane').find('button').addClass('lpay_button lpay_button-md lpay_button-secondary');
  
                $("#footer-html").css("overflow", "hidden");
  
                $('.ui-dialog-titlebar-close').addClass('ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only');
  
                $('.ui-dialog-titlebar-close').append('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span><span class="ui-button-text">close</span>');
  
              },
  
              dialogClass: 'ui-dialog-osx'
  
            });
  
          }
  
            else{
  
              $("#footer-html").dialog({
  
                modal: true,
  
                draggable: false,
  
                resizable: false,
  
                show: 'blind',
  
                hide: 'blind',
  
                width: 290,
  
                height: 72,
  
                buttons: [{
  
                  text: "Send Email",
  
                  click: function click() {
  
                    _id("emailCheck").value = "true";
  
                    
  
                    sendInvoice();
  
                    // document.forms["frmInvoice"].submit();
  
                    $(this).dialog("close");
  
                  }
  
                }, {
  
                  text: "Send SMS",
  
                  click: function click() {
  
                    _id("smsCheck").value = "true";
  
                    sendInvoice();
  
                    // document.forms["frmInvoice"].submit();
  
                    $(this).dialog("close");
  
                  }
  
                }, {
  
                  text: "Send Both",
  
                  click: function click() {
  
                    _id("emailCheck").value = "true";
  
                    _id("smsCheck").value = "true";
  
                    sendInvoice();
  
                    // document.forms["frmInvoice"].submit();
  
                    $(this).dialog("close");
  
                  }
  
                }],
  
                open: function open() {

                  $('.ui-dialog-buttonpane').find('button').removeClass('ui-button ui-corner-all ui-widget');

                  $('.ui-dialog-buttonpane').find('button').addClass('lpay_button lpay_button-md lpay_button-secondary');
  
                  $("#footer-html").css("overflow", "hidden");
  
                  $('.ui-dialog-titlebar-close').addClass('ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only');
  
                  $('.ui-dialog-titlebar-close').append('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span><span class="ui-button-text">close</span>');
  
                },
  
                dialogClass: 'ui-dialog-osx'
  
              });
  
          }
        }else{
          document.querySelector(".textFL_merch_invalid").focus();
        }

        
       }
    } //to show error in the fields

  }, {
    key: "addFieldError",
    value: function addFieldError(fieldId, errMsg, errMsgFlag) {
      var errSpanId = fieldId + "Err";

      var elmnt = _id(fieldId);

      elmnt.classList.add("textFL_merch_invalid");
      elmnt.classList.remove("textFL_merch");

      _id(errSpanId).classList.add("show");

      elmnt.focus();

      if (errMsgFlag) {
        _id(errSpanId).innerHTML = errMsg;
      }
    } // to remove the error 

  }, {
    key: "removeFieldError",
    value: function removeFieldError(fieldId) {
      var errSpanId = fieldId + "Err";

      _id(errSpanId).classList.remove("show");

      _id(errSpanId).innerHTML = "";

      _id(fieldId).classList.remove("textFL_merch_invalid");
      _id(fieldId).classList.add("textFL_merch");
    }
  }]);

  return FieldValidator;
}();