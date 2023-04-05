function onlyDigit(event){
	var x = event.keyCode;
	if (x > 47 && x < 58 || x == 32) {
	} else {
		event.preventDefault();
	}
}

var onlyNumberInput = function(that) {
    that.value = that.value.replace(/[^0-9]/g, '');
}

function alphaNumeric(event){
    var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9;

    if(event.key == "!" || event.key == "@" || event.key == "#" || event.key == "$" || event.key == "%" || event.key == "^" || event.key == "&" || event.key == "*" || event.key == "(" || event.key == ")"){
    return false;
  }

  return ((key >= 65 && key <= 90) || (key >= 48 && key <= 57) || (key >= 96 && key <= 105) || key == backspaceKey || key == tabKey || key == spaceKey || key == leftKey || key == rightKey || key == deleteKey);
}

var onlyAlpha = function(that) {
    if(that.value == " " || that.value == "-" || that.value == "/") {
        that.value = "";
    } else {
        that.value = that.value.replace(/[^a-zA-Z/ -]/g, '');
    }
}

// tab creation 
// $(".lpay-nav-link").on("click", function(e){
//     var _this = $(this).attr("data-id");
//     $(".lpay-nav-item").removeClass("active");
//     $(this).closest(".lpay-nav-item").addClass("active");
//     $(".lpay_tabs_content").addClass("d-none");
//     $("[data-target="+_this+"]").removeClass("d-none");
// })

function tabChange(_selector){
    $(".lpay-nav-item").removeClass("active");
    $("[data-id='"+_selector+"']").closest("li").addClass("active");
    $(".lpay_tabs_content").addClass("d-none");
    $("[data-target="+_selector+"]").removeClass("d-none");
}

var _checkNull = document.querySelector("#example");
if(_checkNull != null){
    $('#example').DataTable( {
        dom: 'B',
        buttons: [
			{
				extend: 'csv',
				text: 'Download CSV Format',
				className: 'lpay_button lpay_button-md lpay_button-primary',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
    });
}


$(".lpay_upload_input").on("change", function(e){
    var _val = $(this).val();
    var _fileSize = $(this)[0].files[0].size;
    var _tmpName = _val.replace("C:\\fakepath\\", "");
    if(_val != ""){
        $("body").removeClass("loader--inactive");
        $(".default-upload").addClass("d-none");
        $("#placeholder_img").css({"display":"none"});
        if(_fileSize < 2000000){
            $(this).closest("label").attr("data-status", "success-status");
            $("#fileName").text(_tmpName);
            $("#bulkUpdateSubmit").attr("disabled", false);
            setTimeout(function(e){
                $("body").addClass("loader--inactive");
            }, 500);
        }else{
            $(this).closest("label").attr("data-status", "error-status");
            $("#bulkUpdateSubmit").attr("disabled", true);
            setTimeout(function(e){
                $("body").addClass("loader--inactive");
            }, 500);
        }
    }
});

$("#bulkUpdateSubmit").on("click", function(e){
    $("#addBulkUsers").submit();
})

$(document).ready(function(e) {
	if (document.getElementById("userRoleType").value != "subMerchant"){
	    document.getElementById("superMerchantDiv").style.display = "none";
	}

    $("#userRoleType").on("change", function(e){
    	if($(this).val() == "reseller"){
              $("[data-id=isPartner]").removeClass("d-none");
              $(".map-verification").addClass("d-none");
        }else{
            $("[data-id=isPartner]").addClass("d-none");
            $(".map-verification").removeClass("d-none");
        }
        if (document.getElementById("userRoleType").value == "subMerchant"){
            document.getElementById("superMerchantDiv").style.display = "block";
        } else{
            document.getElementById("superMerchantDiv").style.display = "none";
        }   
        if($(this).val() == "superMerchant"){
            $(".map-verification").addClass("d-none");
        }else{
            $(".map-verification").removeClass("d-none");
            
        }
    });

    // signup button disabled 
    $(".acquirer-input").val("");
    var _checkClass = false;

    function checkBlankField() {
        setTimeout(function() {
            $(".acquirer-input").each(function(e) {
                var _thisVal = $(this).val();

                if(_thisVal == "") {
                    _checkClass = false;
                    return false;
                } else {
                    _checkClass = true;
                }
            });
    
            if(_checkClass) {
                if($(".errorSec.show").length == 0) {
                    $("#submit").attr("disabled", false);
                } else {
                    $("#submit").attr("disabled", true);
                }
            } else {
                $("#submit").attr("disabled", true);
            }            
        }, 200);
    }

    $(".acquirer-input").on("change", checkBlankField);

    $(".selectpicker").on("change", function() {
        setTimeout(function() {
            checkBlankField();            
        }, 200);
    });
    // error msg show


    // signup button disabled
    function mailId(url, dataId, successMsg, param) {
        var _parent = $(dataId);
        var _id = _parent[0].id;
        var emailId = $(dataId).val();
        var dataObj = {};
        dataObj[param] = emailId;
    
        $.ajax({
            type: "post",
            url: url,
            data: dataObj,
            success: function(data){
                var _common = $(_parent).closest(".common-validation");
                var _label = _common.find(".errorSec");
                if(data[successMsg] == "success") {
                    $(_parent).attr("readonly", true);
                    _common.removeClass("verify-denied");
                    _common.addClass("verify-success");
                    _label.removeClass("show");
                
                    if(url == "mobileNumberValidate") {
                        unlockPin();
                    }
                } else {
                    _label.text(data.response);
                    _label.addClass("show");
                    _common.addClass("verify-denied");
                }
            },
            error: function(data) {
            }
        });
    }

    $("#superMerchant").on("blur", function(e) {
        var _val = $(this).val();
        var _label = $(this).closest(".common-validation").find(".errorSec");

        if(_val == "") {
            _label.text("Please select super merchant");
            _label.addClass("show");
        }
    });

    $("#superMerchant").on("change", function(e) {
        var _val = $(this).val();
        var _label = $(this).closest(".common-validation").find(".errorSec");
        
        _label.text("");
        _label.removeClass("show");
    });

    $("#loginNumber").on("blur", function(e) {
        var _val = $(this).val();
        var _common = $(this).closest(".common-validation");
        var _label = _common.find(".errorSec");
        
        if(_val == " ") {
            $(this).val("");
        } else if(_val !== "") {
            if(_val.length < 9) {
                _label.text("Please enter valid mobile number.");
                _label.addClass("show");
            } else {
                if(!_label.hasClass("show")) {
                    if(!_common.hasClass("verify-success")) {
                        _label.text("");
                        _label.removeClass("show");
                    }
                }
            }
        } else if(_val == "") {
            _label.text("Please enter mobile number.");
            _label.addClass("show");
        }
    });

    $("#emailId").on("blur", function(e) {
        var _val = $(this).val();
        var _common = $(this).closest(".common-validation");
        var _label = _common.find(".errorSec");
        
        if(_val == " ") {
            $(this).val("");
        } else if(_val !== "") {
            if(isValidEmail(true)) {
                if(!_label.hasClass("show")) {
                    if(!_common.hasClass("verify-success")) {
                        mailId("emailAction", "#emailId", "emailSuccessStatus", "emailId");
                    }
                }
            } else {
                _label.addClass("show");
                _label.text("Please enter valid email");
            }
        } else if(_val == "") {
            _label.addClass("show");
            _label.text("Please enter email");
        }
    });

    $("#loginNumber").on("keyup", function(e) {
        var _val = $(this).val();
        var _common = $(this).closest(".common-validation");
        
        if(_val == " ") {
            $(this).val("");
        } else if(_val !== "") {
            if(_val.length > 9) {
                mailId("mobileNumberValidate", "#loginNumber", "phoneSuccessStatus", "phoneNumber");
            }
        }

        _common.removeClass("verify-denied");
        _common.find(".errorSec").removeClass("show");
    });

    $("#emailId").on("keyup", function(e) {
        var _val = $(this).val();
        var _label = $(this).closest(".common-validation").find(".errorSec");

        if(_val == " ") {
            $(this).val("");
        }

        _label.removeClass("show");
        _label.text("");

        $(this).closest(".common-validation").removeClass("verify-denied");
    });

    $("#businessName").on("keyup", function(e) {
        var _val = $(this).val();
        var _label = $(this).closest(".common-validation").find(".errorSec");
        
        if(_val == " ") {
            $(this).val("");
        }

        _label.removeClass("show");
        _label.text("");
    });

    $("#businessName").on("blur", function(e) {
        var _val = $(this).val();
        var _label = $(this).closest(".common-validation").find(".errorSec");

        if(_val == " ") {
            $(this).val("");
        } else if(_val == "") {
            _label.addClass("show");
            _label.text("Please enter business name");
        }
    });



    // pin change
    $("#pin").on("change", function(e) {
        checkBlankField();
        $(this).closest(".position-relative").find("[data-id='pinBox6']").attr("readonly", false);
        $(".confirm-pin").find("[data-id='pinBox1']").focus();
        $(".confirm-pin").find("[data-id='pinBox1']").attr("readonly", false);
    });

    // confirm pin validation
    $("#confirmPin").on("change", function(e){
        var pin = $("#pin").val();
        var confirmPin = $("#confirmPin").val();
        if(confirmPin.length == 6 && confirmPin != "") {
            if(pin == confirmPin) {
                $("#errorConfirmPassword").removeClass('show');
                // checkBlankField();
                // $(".confirm-pin").find("[data-id='pinBox6']").attr("readonly", false);
                // $(".confrim-pin").find("[data-id='pinBox6']").focus();
                // $("#submit").attr("disabled", false);
            } else {
                $("#errorConfirmPassword").addClass('show');
                $(".pin-div").find("[data-id]").val("");
                $(".new-pin").find("[data-id='pinBox1']").attr("readonly", false);
                $(".new-pin").find("[data-id='pinBox1']").focus();
                $("#submit").attr("disabled", true);
            }
        } else {
            $("#errorConfirmPassword").removeClass('show');                
        }
    });

    // if mobile and email id filled then block
    function unlockPin(){
        var _mobileNumber = $("#loginNumber").val();
        if(_mobileNumber != "" && $(".errorPhone.show").length == 0){
            $("#pin").closest(".position-relative").find("[data-id='pinBox1']").focus();
            $("#pin").closest(".position-relative").find("[data-id='pinBox1']").attr("readonly", false);
        }
    }


    $(".otp-input-common").on("keyup", function(e){
        if(e.keyCode == 08){
            $(this).prev().focus();
            if($(this).prev().length == 0){
                $(this).attr("readonly", false);
            }else{
                $(".otp-input-common").attr("readonly", true);
                $(this).prev().attr("readonly", false);
            }
        }
    });

    // create function for input otp hidden box

    $(".otp-input-common").attr("maxlength", 1);
    // $(".otp-input-common").attr("onkeypress", "onlyDigit(event)");
    $(".otp-input-common").attr("readonly", true);
    $(".otp-input-common").on("keyup keydown", function(e) {
        // onlyDigit(event);
        var code = e.keyCode || e.which;
        var _parent = $(this).closest(".position-relative");
        var otpInput1 = _parent.find("[data-id='pinBox1']").val();
        var otpInput2 = _parent.find("[data-id='pinBox2']").val();
        var otpInput3 = _parent.find("[data-id='pinBox3']").val();
        var otpInput4 = _parent.find("[data-id='pinBox4']").val();
        var otpInput5 = _parent.find("[data-id='pinBox5']").val();
        var otpInput6 = _parent.find("[data-id='pinBox6']").val();
        if(code != 9 && code != 08 && code > 47 && code < 58 ) {
            if($(this).val() != "") {
                $(this).next().focus();
                $(".otp-input-common").attr("readonly", true);
                $(this).next().attr("readonly", false);
            } else {
                $(this).focus();
            }
        } else {
            if(code == 08) {
            } else {
                $(this).focus();
                e.preventDefault();
            }
        }
        if(otpInput1 != "" && otpInput2 != "" && otpInput3 != "" && otpInput4 != "" && otpInput5 != "" && otpInput6 != "") {
            var getOtp = otpInput1 + otpInput2 + otpInput3 + otpInput4 + otpInput5 + otpInput6;
            $(this).closest(".position-relative").find("input[type='hidden']").val(getOtp).trigger("change");
        } else {
            if($("#confirmPin").val("")) {
                $("#errorConfirmPassword").removeClass("show");
                checkBlankField();
                // $("#signup-btn").addClass("d-none");
            }
            if($("#resetOtp").val("")) {
                $("#submit").attr("disabled", true);
            }
            $(this).closest(".position-relative").find("input[type='hidden']").val(""); 
        }
    });

    // function only digit
    function isValidEmail() {
        var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
        var emailValue = document.getElementById("emailId").value;
        if (emailValue.trim() && emailValue.match(emailexp)) {          
            return true;
        } else {
            //   document.getElementById("emailError").style.display = "block";
        }
    }
});