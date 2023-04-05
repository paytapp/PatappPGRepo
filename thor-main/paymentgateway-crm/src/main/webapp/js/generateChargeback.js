$(document).ready(function() {
    $("#chargebackAmount, #otherCharges").on("blur", function(e) {
        var val = $(this).val();
        if(val !== "") {
            val = Number(val);
            $(this).val(val.toFixed(2));
        }
    });

    $(".checkbox-label input").on("change", function(e){
        if($(this).is(":checked")){
          $(this).closest("label").addClass("checkbox-checked");
        }else{
          $(this).closest("label").removeClass("checkbox-checked");
        }
      });

    var validateChargebackAmount = function() {
        var refundAvailable = document.getElementById("refundAvailable").value,
            chargebackAmount = document.getElementById("chargebackAmount").value,
            refundAvailableFloat =  parseFloat(refundAvailable),
            chargebackAmountFloat =  parseFloat(chargebackAmount),
            errorBox = $("#error-chargebackAmount");

        if(chargebackAmount == "") {
            errorBox.text("Chargeback amount cannot be blank.");
            errorBox.removeClass("d-none");
            return false;
        } else if(chargebackAmountFloat > refundAvailableFloat) {
            errorBox.text("Chargeback amount cannot be greater than available amount.");
            errorBox.removeClass("d-none");
            return false;
        } else if(chargebackAmount <= 0) {
            errorBox.text("Chargeback amount cannot be less than or equals to zero.");
            errorBox.removeClass("d-none");
            return false;
        } else {
            errorBox.text("");
            errorBox.addClass("d-none");
            return true;
        }
    }

    $("#chargebackAmount").on("blur", function() {
        validateChargebackAmount();
    });

    var decimalCount = function(number) {
        // Convert to String
        var numberAsString = number.toString();
        // String Contains Decimal
        if (numberAsString.includes('.')) {
            return numberAsString.split('.')[1].length;
        }
        // String Does Not Contain Decimal
        return 0;
    }
    
    $("#chargebackAmount, #otherCharges").on("keyup", function(e) {
        var val = $(this).val();
        if(isNaN(val)){
            val = val.replace(/[^0-9\.]/g,'');
            if(val.split('.').length>2) 
                val = val.replace(/\.+$/,"");
        }

        var countDecimal = decimalCount(val);

        if(countDecimal > 2) {
            val = Number(val);
            var enteredVal = "0.00" + e.key;
            enteredVal = Number(enteredVal);                        
            if(enteredVal >= 0.000) {
                val = val - enteredVal;
            }

            $(this).val(val.toFixed(2));
        } else {
            $(this).val(val);
        }
    });		

    
    var dateToday = new Date(); 
    $("#targetDate").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : true,
        minDate: dateToday,			
    });    

    updateFormEnabled();

    var _result = "";

    $(".lpay_upload_input").on("change", function(e) {
        $("body").removeClass("loader--inactive");

        var _that = $(this),
            _val = _that.val(),            
            _label = _that.closest(".lpay-upload");

        if(_val != "") {
            setTimeout(function() {
                $(".default-upload").addClass("d-none");

                var result = validateFileUpload({
                    that: _that,
                    maxLimit: 5
                });

                if(result) {
                    _label.attr("data-status", "success-status");
                    $("#filename-success").text(_result);
                    $("body").addClass("loader--inactive");
                } else {                        
                    _label.attr("data-status", "error-status");
                    $("#filename-error").text(_result);
                    $("#filename-error").css("display", "block");
                    $("body").addClass("loader--inactive");
                }
            }, 500);
        } else {
            setTimeout(function() {
                $(".default-upload").removeClass("d-none");
                _label.attr("data-status", "");
                
                $("body").addClass("loader--inactive");
            }, 500);
        }
    });

    var validateFileUpload = function(obj) {
        var _that = obj.that;
        var _files = _that[0].files[0];

        if(_that.value !== "") {            
            var fileName = _files.name;
            var _size = _files.size / 1024;
            var _maxSize = 1024 * obj.maxLimit;
            var fileExtension = fileName.split('.').pop().toLowerCase();

            if(_size > _maxSize) {
                _result = "File cannot be greater than "+ _maxSize / 1024 +" mb";
                return false;
            } else if(fileExtension == "" || fileExtension == "csv" || fileExtension == "pdf") {                
                _result = fileName;
                return true;                
            } else {
                _result = "Wrong file format.";
                return false;
            }            
        } else {
            return "File not selected.";
        }
    }

    $("#comments").on("keyup", function(e) {
        var _val = $(this).val();
        
        if(_val == "") {
            $("#error-commentId").removeClass("invisible");
        } else {
            $("#error-commentId").addClass("invisible");
        }
    });

    $("form#files").submit(function(e) {
        e.preventDefault();
        
        var validateChargeback = validateChargebackAmount();
        var _comment = document.getElementById("comments").value;
        
        if(!validateChargeback) {
            return false;
        } else if(_comment == "") {
            document.getElementById("error-commentId").classList.remove("invisible");
            return false;
        } else {
            var isFileUploaded = document.querySelector(".lpay-upload").getAttribute("data-status");
            if(isFileUploaded == "error-status") {
                return false;
            } else {
                var formData = new FormData($(this)[0]);

                $("body").removeClass("loader--inactive");

                $.ajax({
                    url:'saveChargebackAction',
                    type: 'POST',
                    timeout: 0,
                    data: formData,
                    async: true,
                    success: function (data) {                        
                        // location.reload();
                        //document.saleTransactionSearch.submit();
                        setTimeout(function() {
                            alert("Successfully created." );
                            window.history.back();
                        }, 3000);
                        
                        // $("body").addClass("loader--inactive");
                        //document.getElementById("saveMessage").innerHTML="Comments added successfully.";
                    },
                    error: function (data) {
                        alert("Unable to save chargeback!" );
                        $("body").addClass("loader--inactive");
                        // location.reload();
                    },
                    cache: false,
                    contentType: false,
                    processData: false
                });
                return true;
            }
        }
    });
});

function updateFormEnabled() {
    if (verifyAdSettings()) {
        $('#chargebackSubmit').attr('disabled', false);
    } else {
        $('#chargebackSubmit').attr('disabled', true);
    }
}

function verifyAdSettings() {
    var chargeBackTyp = document.getElementById("chargebackType").value;
    // var chargeBackStats = document.getElementById("chargebackStatus").value; 
    var targtDate = document.getElementById("targetDate").value;
    
    if (chargeBackTyp != '' && targtDate != '') {
        return true;
    } else {
        return false
    }
}

// const validateFileUpload = that => {
//     let fileName = that.value;
//     let fileExtension = fileName.split('.').pop().toLowerCase();
//     let errorDiv = that.closest(".attachment").querySelector(".error-filename");
//     if(fileExtension == "" || fileExtension == "csv" || fileExtension == "pdf") {
//         errorDiv.classList.add("invisible");
//     } else {
//         errorDiv.classList.remove("invisible");
//     }
// }