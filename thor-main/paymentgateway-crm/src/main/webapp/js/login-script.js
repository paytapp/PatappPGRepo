var error_snackbar = document.getElementById("error-snackbar");
var success_snackbar = document.getElementById("success-snackbar");


// generate captcha
// function generateCaptcha() {
//     var alpha = new Array('1','2','3','4','5','6','7','8','9','0');
//     var i;
//     for (i=0;i<4;i++){
//       var a = alpha[Math.floor(Math.random() * alpha.length)];
//       var b = alpha[Math.floor(Math.random() * alpha.length)];
//       var c = alpha[Math.floor(Math.random() * alpha.length)];
//       var d = alpha[Math.floor(Math.random() * alpha.length)];
//      }
//    var code = a + '' + b + '' + '' + c + '' + d;
//    document.getElementById("mainCaptcha").innerText = code;
//    document.getElementById("hideCaptcha").value = code;
//  }

//  $(".refresh-icon").on("click", function(e){
//     $("#captchaLogin").val("");
//     generateCaptcha();
//  })

    $(".refresh-icon").on("click", function(e){
        $("#captchaLogin").val("");
        generateCaptcha();
    })

// generateCaptcha();

var generateCaptcha = function() {
    img = $("#captchaImage").attr('src', "../Captcha.jpg/" + Math.random());
}

generateCaptcha();

// SNACKBAR
function showSnackbar(id) {
	// Get the snackbar DIV
	var x = document.getElementById(id);
  
	// Add the "show" class to DIV
	x.classList.add("show");
  
	// After 3 seconds, remove the show class from DIV
	setTimeout(function() {
		x.classList.remove("show");
	}, 3000);
}

function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

$(".mobileNumber-input").on("focus", function(e){
    $("#errorPhone").removeClass("show");
});

function numOnlyInTextInput(event) {
    var key = event.keyCode,
      spaceKey = 32,
      leftKey =37,
      rightKey = 39,
      deleteKey = 46,
      backspaceKey = 8,
      tabKey = 9;
      
    if(event.key == "!" 
        || event.key == "@" 
        || event.key == "#" 
        || event.key == "$" 
        || event.key == "%" 
        || event.key == "^" 
        || event.key == "&" 
        || event.key == "*" 
        || event.key == "(" 
        || event.key == ")") {
            return false;
    }

    return ((key >= 48 && key <= 57) 
        || (key >= 96 && key <= 105) 
        || key == backspaceKey 
        || key == tabKey 
        || key == leftKey 
        || key == rightKey 
        || key == deleteKey);
};

function businessNameValid(event){
    var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9,
    fullstopKey = 190;

    if(event.key == "!" 
        || event.key == "@" 
        || event.key == "#" 
        || event.key == "$" 
        || event.key == "%" 
        || event.key == "^" 
        || event.key == "*" 
        || event.key == "(" 
        || event.key == ")") {
            return false;
    }

  return ((key >= 65 && key <= 90) 
    || (key >= 48 && key <= 57) 
    || (key >= 96 && key <= 105) 
    || key == backspaceKey 
    || key == tabKey 
    || key == spaceKey 
    || key == leftKey 
    || key == rightKey 
    || key == deleteKey 
    || key == fullstopKey);
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



function resetField(e){
    document.querySelector(".update-number").classList.add("d-none");
    var _getAllInput = document.querySelectorAll("input");
    document.querySelector(".captcha-code-div").classList.add("d-none");
    document.querySelector("#login-pin").classList.add("d-none");
    $(".errorMessage").fadeOut();
    document.querySelector(".login-action-btn").classList.add("d-none");
    document.querySelector("#login-otp").classList.add("d-none");
    document.querySelector("#loginNumber").removeAttribute("readonly"); 
    document.querySelector("#loginNumber").focus();  
    _getAllInput.forEach(function(index, array, element){
        if(index.type != "hidden"){
            index.value = "";
        }
    })  
}   

$(document).ready(function() {
	
	$(".radio-hidden").on("change", function(E){
        var _this = $(this).val();
        if(_this == "reseller"){
            $(".mpa-verification").addClass("d-none");
            $("[data-id=partnerFlag]").removeClass("d-none");
        }else{
            $("[data-id=partnerFlag]").addClass("d-none");
            $(".mpa-verification").removeClass("d-none");
        }
    })


    // CHECK IF EMAIL IS VALIDATED
    var isValidEmail = function(inputId) {
        var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
        var emailElement = document.getElementById(inputId);
        var emailValue = emailElement.value;
        if (emailValue.trim() !== "") {
            if (emailValue.match(emailexp)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // EMAIL VALIDATION
    var validateEmail = function(e) {
        var that = $(e);
        var errorEmail = $('#errorEmail');

        if(!that.val() || that.val().length < 6 || !isValidEmail(that.attr('id'))) {
            errorEmail.text('Please enter valid email id');
            errorEmail.addClass('show');
            return false;
        } else {
            errorEmail.removeClass('show');
            errorEmail.text('');

            return true;
        }
    }

    // signup email validate action
    $("#sighnUpemailId").on("blur", function(){
        var _parent = $(this).attr("id");
        if(validateEmail(this)){
            $("body").removeClass("loader--inactive");
            var emailId = $(this).val();
            var userRoleType  = $("input[name='userRoleType']:checked").val();
            $.ajax({
                type: "post",
                url: "emailAction",
                data: {"emailId": emailId, "userRoleType": userRoleType},
                success: function(data){
                    if(data.emailSuccessStatus == "success"){
                        $("#"+_parent).attr("readonly", true);
                        $("#"+_parent).closest(".position-relative").removeClass("verify-denied");
                        $("#"+_parent).closest(".position-relative").addClass("verify-success");
                        $("#errorEmail").removeClass("show");
                        $("#errorEmail").text("");
                        $("body").addClass("loader--inactive");
                        $("#signUpPhoneNumber").attr("readonly", false);
                        $(".signup-disabled").attr("disabled", "disabled");
                    }else{
                        $("#errorEmail").addClass("show");
                        $("#errorEmail").text(data.response);
                        $("#"+_parent).closest(".position-relative").addClass("verify-denied");
                        $("body").addClass("loader--inactive");
                        $("#signUpPhoneNumber").attr("readonly", true);
                    }
                },
                error: function(data){

                }
            });
        }
    })

    // signup email id error remvoe on click
    $("#sighnUpemailId").on("keyup", function(e){
        $(this).closest(".position-relative").removeClass("verify-denied");
        $("#errorEmail").removeClass("show");
    });
    
    // signup phone number error when entered less then 10 digit
    $("#signUpPhoneNumber").on("keyup", function(e){
        var phoneNumber = $(this).val();
        if(phoneNumber.length == 10){
            $("#generateOtpBtnSignUp").removeClass("d-none");
        }else{
            $("#generateOtpBtnSignUp").addClass("d-none");
            $("#errorPhone").removeClass("show");

        }
    });

    // sign up otp send or resend 
    $("#generateOtpBtnSignUp, #resendOtpSignUp").click(function(e) {
        e.preventDefault();
        $("body").removeClass("loader--inactive");
        $("#resendOtpSignUp").addClass("d-none");
        var phoneNumber = $("#signUpPhoneNumber").val();
        if(phoneNumber.length > 9) {
            $.ajax({
                type: "post",
                url: "phoneAction",
                data: { "phoneNumber": phoneNumber },
                success: function(data){
                    if(data.otpSuccessStatus == "success") {
                        $("#otp-box").removeClass("d-none");
                        $("#errorPhone").removeClass("show"); 
                        $("#signUpPhoneNumber").attr("readonly", true);   
                        $("#generateOtpBtnSignUp").addClass("d-none");
                        $("#otp-box").find("[data-id]").val("");
                        $("#otp-box").find("[data-id]").attr("readonly", true);
                        $("#otp-box").find("[data-id='pinBox1']").attr("readonly", false);
                        $("#otp-box").find("[data-id='pinBox1']").focus();
                        timerFunction();
                        $("body").addClass("loader--inactive");
                    }else{
                        $("#errorPhone").addClass("show");
                        $("#errorPhone").text(data.response);
                        $("body").addClass("loader--inactive");
                    }
                },
                error: function(data){
                    
                }
            });
        } 
    });

    // sign up verify otp
    $("#otp").on("change", function(e) {
        var otp = $(this).val();
        var phoneNumber = $("#signUpPhoneNumber").val();
         if(otp.length == 6) {
             $.ajax({
                 type: "post",
                 url: "verifyOtp",
                 data: { "otp": otp, "phoneNumber":phoneNumber },
                 success: function(data){
                     if(data.otpSuccessStatus == "success"){
                        $(".pin-group").removeClass("d-none");
                        $("#signUpPhoneNumber").closest(".position-relative").removeClass("verify-denied");
                        $("#signUpPhoneNumber").closest(".position-relative").addClass("verify-success");
                        $("#generateOtpBtnSignUp").addClass("d-none");
                        $("#signUpPhoneNumber").attr("readonly", true);
                        $(".pin-one").find("[data-id='pinBox1']").focus();
                        $(".pin-one").find("[data-id='pinBox1']").attr("readonly", false);
                        $("#otp-box").addClass("d-none");
                    }else{
                        $("#using-otp").find("[data-id]").val("");
                        $("#using-otp").find("[data-id='pinBox1']").focus();
                        $("#using-otp").find("[data-id='pinBox1']").attr("readonly", false);
                        $("#error-otp").addClass("show");
                        $("#error-otp").text(data.response);
                        $("#signUpPhoneNumber").closest(".position-relative").addClass("verify-denied");
                    }
                 },
                 error: function(data){
                 }
             });
         }
    });

    // confirm pin signup
    $("#confirmPin").on("change", function(e){
        var pin = $("#pin").val();
        var confirmPin = $("#confirmPin").val();
            if(confirmPin.length == 6 && confirmPin != ""){
                if(pin == confirmPin){
                    $("#errorConfirmPassword").removeClass('show');
                    $("#signup-btn").removeClass("d-none");
                    $("#submit").removeClass("d-none");
                    $(".confirm-pin").find("[data-id='pinBox6']").attr("readonly", false);
                    // $("#pin").closest(".position-relative").find("[data-id='pinBox6']").attr("readonly", false);
                }else{
                    $("#errorConfirmPassword").addClass('show');
                    $("#signup-btn").addClass("d-none");
                    $("#submit").addClass("d-none");
                    $(".pin-group").find("[data-id]").val("");
                    $(".pin-group").find("[data-id]").attr("readonly", true);
                    $(".pin-group").find(".pin-one [data-id='pinBox1']").attr("readonly", false);
                    $(".pin-group").find(".pin-one [data-id='pinBox1']").focus();
                }
            }else{
                $("#errorConfirmPassword").removeClass('show');
                $("#signup-btn").addClass("d-none");
                $("#submit").addClass("d-none");
            }
    });

    $("#resetPhoneNumber").on("keyup", function(e){
        var phoneNumber = $(this).val();
        if(phoneNumber.length == 10){
            $("#generateOtp").removeClass("d-none");
            
        }else{
            $("#generateOtp").addClass("d-none");
           
        }
    });

    // forgot password otp
    $("[data-id='generateOtp']").on("click", function(e){
        var phoneNumber = $("#resetPhoneNumber").val();
        var _parent = $(this).closest(".position-relative");
        $("[data-id='generateOtp']").addClass("d-none");
        if(phoneNumber.length > 9){
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "post",
                url: "forgetPassword",
                data: { "phoneNumber": phoneNumber, "userType": "userOtp" },
                success: function(data){
                    timerFunction();
                    _parent.find("[data-id]").val("");
                    _parent.find("[data-id]").attr("readonly", true);
                    _parent.find("[data-id='pinBox1']").attr("readonly", false);
                    _parent.find("[data-id='pinBox1']").focus();
                    $("body").addClass("loader--inactive");  
                },
                error: function(data){
                }
            });
        }
    });

    // forget password action
    $("#resetOtp").on("change", function(e){
        var _getOtp = $(this).val();
        var _phoneNumber = $("#resetPhoneNumber").val();
        $.ajax({
            type: "post",
            url: "validateOtpAction",
            data: {"otp": _getOtp, "phoneNumber": _phoneNumber, "loginType": "userOtp"},
            success: function(data){
                if(data.response == "success"){
                    $("#error-otp").removeClass("show");
                    $(".forget-btn input").trigger( "click" );
                }else{
                    $("#passwordOtp").find("[data-id]").val("");
                    $("#passwordOtp").find("[data-id='pinBox1']").focus();
                    $("#passwordOtp").find("[data-id='pinBox1']").attr("readonly", false);
                    $("#error-otp").text(data.response);
                    $("#error-otp").addClass("show");
                }
            }
        })
    })

    // login password
    $("#forget-login-otp").on("click", function(e){
        e.preventDefault();
        var _phoneNumber = $("#loginNumber").val();
        var _otp = $("[data-id='login-otp-input']").val();
        $("[name='phoneNumber']").val(_phoneNumber);
        $("[name='otp']").val(_otp);
        $("#forgetPhoneNumber").submit();
    })

    // login pin 
    // $("#loginPin").on("change", function(e){
        
    // });

    $("#captchaLogin").on("keyup", function(e){
        $("#errorCaptcha").removeClass("show");
        var _that = $(this).val();
        var _pin = $("#loginPin").val();
        var _otp = $("[data-id=login-otp-input]").val();
        var _phoneNumber = $("#loginNumber").val();
        if(_that.length == 4 && _pin != ""){
            $(".login-submit-btn input").trigger( "click" );
        }
        if(_that.length == 4 && _otp != ""){
            $.ajax({
                type: "post",
                url: "verifyLoginOtp",
                data: {"otp": _otp, "phoneNumber": _phoneNumber, "loginType": "userOtp","captcha": _that},
                success: function(data){
                    if(data.otpSuccessStatus == "success"){
                        $("#error-otp").removeClass("show");
                        $(".login-submit-btn input").trigger( "click" );
                        $("body").addClass("loader--inactive");
                    }else{
                        $("#login-otp").find("input").val("");
                        $("#login-otp").find("[data-id='pinBox1']").focus();
                        $("#login-otp").find("[data-id='pinBox1']").attr("readonly", false);
                        $("#error-otp").text(data.responseMsg);
                        $(".update-number").removeClass("d-none");
                        $("#error-otp").addClass("show");
                        $("body").addClass("loader--inactive");
                        $("#captchaLogin").val("");
                    }
                }
            })
        }
    })

    $("#pin").on("change", function(e){
        $(this).closest(".position-relative").find("[data-id='pinBox6']").attr("readonly", false);
        if(e.keyCode != 32){
            $(".confirm-pin").find("[data-id='pinBox1']").focus();
            $(".confirm-pin").find("[data-id='pinBox1']").attr("readonly", false);
        }
    });

    $("[data-id='login-otp-input']").on("change", function(e){
        $("#captchaLogin").focus();
    })

    $("#businessName").on('blur', function(e) {
        if($(this).val() == '' || $(this).val().length < 2) {
            $("#errorBusninessName").addClass('show');
        } else {
            $("#errorBusninessName").removeClass('show');
        }
    });


    $("#mobile").on("blur, keyup", function(e) {
        validateMobile("#mobile");
    });

    setInterval(function() {
        $(".wwerr").css('display', 'none');
    }, 5000);


    $(".taget-div").on("click", function(e){
        e.preventDefault();
        var getPhoneNumber = $("#loginNumber").val();
        
        if(getPhoneNumber.length > 9){
            $(".captcha-code-div").removeClass("d-none");
            $(".errorMessage").fadeOut();
            var createId = $(this).attr("data-target");
            $(".login-submit-btn").addClass("d-none");
            $("#errorPhone").removeClass("show");
            $(".login-common").addClass("d-none");
            $("#"+createId).removeClass("d-none");
            // $("#"+createId).find("input").focus();
            $("[data-id='pinBox1']").attr("readonly", false);
            $("[data-id='pinBox1']").focus();
            $(this).closest(".login-action-btn").addClass("d-none");
            if(createId == "login-pin"){
                $("#loginType").val("pin");
            }else{
                $("#loginType").val("userOtp");
            }
        }else{
            $("#errorPhone").addClass("show");
        }
    })

    

    $(".mobileNumber-input").on("blur", function(e){
        var _number = $(this).val();
        if(_number != ""){
            if(_number.length < 10){
                $("#errorPhone").addClass("show");
                $("#errorPhone").text("Mobile number is invalid");
            }
        }else{
            $("#errorPhone").addClass("show");
            $("#errorPhone").text("Please enter mobile number");
        }
    })

    // phone number login check 
    $("#loginNumber").on("input", function(e){
        var phoneNumber = $(this).val();
        $("#errorPhone").removeClass("show");
        if(phoneNumber.length > 9){
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "post",
                url: "userPhone",
                data: { "phoneNumber": phoneNumber },
                success: function(data){
                    if(data.phoneSuccessStatus == "success"){
                        $(".login-action-btn").removeClass("d-none");
                        $("#errorPhone").removeClass("show");
                        $("#loginNumber").attr("readonly", true);
                    }else{
                    	$(".login-action-btn").removeClass("d-none");
                        $("#errorPhone").removeClass("show");
                        $("#loginNumber").attr("readonly", true);
                    }
                    $(".update-number").removeClass("d-none");
                    $("body").addClass("loader--inactive");
                }
            })
        }else{
            $("#errorPhone").removeClass("show");
        }
    })

    // keyup remove error

    // $("#loginNumber").on("key")
 
    $("#loginOtp, #resendOtp, [data-target='login-otp']").on("click", function(e){
        e.preventDefault();
        $(".otp-input-common").val("");
        var phoneNumber = $("#loginNumber").val();
        $("#timer").text(" ");
        if(phoneNumber.length > 9){
            $("body").removeClass("loader--inactive");
            $("#resendOtp").addClass("d-none");
            timerFunction();
            $.ajax({
                type: "post",
                url: "otpAction",
                data: { "phoneNumber": phoneNumber },
                success: function(data){
                    if(data.otpSuccessStatus == "success"){
                        $("#login-otp").find("[data-id='pinBox1']").focus();
                        $("#login-otp").removeClass("d-none");
                        $(".error").removeClass("show");
                        $("#login-otp").removeClass("d-none");
                        $("#login-otp").find("[data-id]").val("");
                        $("#login-otp").find("[data-id]").attr("readonly", true);
                        $("#login-otp").find("[data-id='pinBox1']").attr("readonly", false);
                        $("#login-otp").find("[data-id='pinBox1']").focus();
                        $(".login-action-btn").addClass("d-none");
                        $(".captcha-code-div").removeClass("d-none");
                        $("body").addClass("loader--inactive");
                    }else{
                        $("#errorPhone").addClass("show");
                        $("#errorPhone").text(data.response);
                        $("#login-otp").addClass("d-none");
                        $(".login-action-btn").addClass("d-none");
                        $(".captcha-code-div").addClass("d-none")
                        $(".captcha-code-div").addClass("d-none");;
                        $("body").addClass("loader--inactive");
                    }
                },
                error: function(data){
 
                }
 
            })
        }
    })

    //timer
    function timerFunction(){
        var sec = 50, countDiv = document.getElementById("timer"),
        secpass,
        countDown   = setInterval(function () {
            'use strict';
            
            secpass();
        }, 1000);
    
        function secpass() {
            'use strict';
            
            var min     = Math.floor(sec / 60),
                remSec  = sec % 60;
            
            if (remSec < 10) {
                remSec = '0' + remSec;
            }
            if (min < 10) {
                min = '0' + min;
            
            }
            countDiv.innerHTML = min + ":" + remSec;
            
            if (sec > 0) {
                
                sec = sec - 1;
                
            } else {
                clearInterval(countDown);
                $("[data-id='generateOtp']").removeClass("d-none");
                $("#resendOtp").removeClass("d-none");
                $("#resendOtpSignUp").removeClass("d-none");
                countDiv.innerHTML = '';
                
            }
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
    $(".otp-input-common").attr("onkeypress", "onlyDigit(event)");
    $(".otp-input-common").attr("readonly", true);
    $(".otp-input-common").on("keyup keydown", function(e){
        $(".errorMessage").fadeOut();
        var code = e.keyCode || e.which;
        var _parent = $(this).closest(".position-relative");
        var otpInput1 = _parent.find("[data-id='pinBox1']").val();
        var otpInput2 = _parent.find("[data-id='pinBox2']").val();
        var otpInput3 = _parent.find("[data-id='pinBox3']").val();
        var otpInput4 = _parent.find("[data-id='pinBox4']").val();
        var otpInput5 = _parent.find("[data-id='pinBox5']").val();
        var otpInput6 = _parent.find("[data-id='pinBox6']").val();
        if(code != 9 && code != 08 && code != 32){
            if($(this).val() != ""){
                $(".otp-input-common").attr("readonly", true);
                $(this).next().attr("readonly", false);
                $(this).next().focus();
            }else{
                $(this).focus();
            }
        }else{
            if(code == 08){
            }else{
                $(this).focus();
                e.preventDefault();
            }
        }
        if(otpInput1 != "" && otpInput2 != "" && otpInput3 != "" && otpInput4 != "" && otpInput5 != "" && otpInput6 != ""){
            var getOtp = otpInput1 + otpInput2 + otpInput3 + otpInput4 + otpInput5 + otpInput6;
            $(this).closest(".position-relative").find("input[type='hidden']").val(getOtp).trigger("change");
            if($("[data-id=pin]").val() != ""){
                $("#captchaLogin").focus();
            }
        }else{
            if($("#otp").val("")){
                $("#error-otp").removeClass("show");
                $("#signUpPhoneNumber").closest(".position-relative").removeClass("verify-denied");
            }
            if($("#confirmPin").val("")){
                $("#errorConfirmPassword").removeClass("show");
                $("#signup-btn").addClass("d-none");
            }
            if($("#resetOtp").val("")){
                $("#submit").addClass("d-none");
            }
            $(this).closest(".position-relative").find("input[type='hidden']").val(""); 
        }
    })

    // login condition based 
    var _getErrorVal = $(".errorMessage").find("span").text();
    if(_getErrorVal == "Incorrect PIN!" || _getErrorVal == "Invalid Captcha"){
        $(".update-number").removeClass("d-none");
        $("#loginNumber").attr("readonly", true);
        $("#login-pin").removeClass("d-none");
        $("#login-pin").find("[data-id='pinBox1']").focus();
        $("#login-pin").find("[data-id='pinBox1']").attr("readonly", false);
        $(".captcha-code-div").removeClass("d-none");
        $("#loginPin").val("");
    }else{
        // $(".update-number").addClass("d-none");
        $("#loginNumber").val("");
        $("#loginNumber").focus();
    }

    $("#login-form").on("submit", function() {
        $("body").removeClass("loader--inactive");
    });
    
});

$(window).on('load', function() {
    $("body").addClass("loader--inactive");
});