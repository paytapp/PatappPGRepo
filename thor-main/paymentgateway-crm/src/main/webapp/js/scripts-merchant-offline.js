var getWidth = 0;
var _array = [];
var _new = document.querySelectorAll(".mpaFormBullets");
_new.forEach(function(index, array, element){
    if(array < 2){
        var _value = element[array].offsetLeft;
        _array.push(_value); 
    }
})
var getTotal = _array[1] - _array[0];
var _payId = $("#merchantPayId").val();

// loader in variable
function fetchPreviousData(data){

    if(data.stage == "01"){

        if(data.merchantSupportLandLine != ""){
            $(".landline-div").find("input").val(data.merchantSupportLandLine);
        }

    }

    var _parent = document.querySelector(".stage-box.active"); // get parent
    var mpaInputBox = _parent.querySelectorAll(".mpa-input"); // get all Input boxes
    if(data.stage == "06"){
        $(".third-party").attr("id", "thirdPartyForCardData");
    }
    mpaInputBox.forEach(function(that) {
        var isMpaInputAlt = that.classList.contains("mpa-input-alt");        
        var _key = that.getAttribute("id");
        if(isMpaInputAlt) {
            var _innerInput = that.querySelectorAll("input");
            
            if(_key !== undefined) {
                var _innerInputVal = data[_key];

                if(data[_key] !== undefined) {
                    _innerInputVal = _innerInputVal.split(",");
                    
                    var count = 0;
    
                    _innerInput.forEach(function(idx) {
                        if(idx.value == _innerInputVal[count]) {
                            idx.checked = true;
                            count++;
                        }                        
                    });
                }                

            }
        } else {

            if(data.stage == "00"){
                if(data.typeOfEntity == "Private Limited" || data.typeOfEntity == "Public Limited"){
                    $("[data-change='cin']").find("label").text("CIN");
                    $("[data-change='cin']").find("input").attr("id", "cin");
                }
                else if(data.typeOfEntity == "Proprietory"){
                    $("[data-change='cin']").find("label").text("Shop Establishment Number");
                    $("[data-change='cin']").find("input").attr("id", "registrationNumber");
                    $("[data-change='cin']").find("input").addClass("registrationNumberApi");
                    $("#businessEmailForCommunication").closest(".col-md-6").hide();
                    $("#businessEmailForCommunication").removeClass("mpa-input");
                }else{
                    $("[data-change='cin']").find("label").text("Registration Number"); 
                    $("[data-change='cin']").find("input").attr("id", "registrationNumber");
                    $("[data-change='cin']").find("input").removeClass("registrationNumberApi");
                }
                if(data.typeOfEntity != "Private Limited" && data.typeOfEntity != "Public Limited" && data.typeOfEntity != "Proprietory" && data.typeOfEntity != "Partnership Firm"){
                    $(".other").val(data.typeOfEntity);
                    $(".other").attr("id", "typeOfEntity");
                    $(".other").addClass("mpa-input mpaInput");
                    $(".paymentGatewayRadio input[value='other']").attr("checked", true);
                }
            }

            if(_key !== undefined) {
                that.value = data[_key];
            }
            if(data.stage == "01") {

                if(data.merchantSupportEmailId == data.contactEmail){
                   $("[for=merchantSupport]").addClass("checkbox-checked");
                }

                if(data.merchantSupportEmailId == data.director1Email){
                    $("[for=merchantSupport-pro]").addClass("checkbox-checked");
                }

                if(data.typeOfEntity != "Proprietory"){
                    $("[data-type='Proprietory']").remove();
                }

                if(data.merchantSupportLandLine != null || data.merchantSupportLandLine != undefined){
                    $(".mobileNumber-div").addClass("d-none");
                    $(".landline-div").removeClass("d-none");
                    $("[for=landlineNumber-pro]").addClass("checkbox-checked");
                    $("[for=landlineNumber]").addClass("checkbox-checked");
                }

                if(data.typeOfEntity != "Private Limited" && data.typeOfEntity != "Public Limited" && data.typeOfEntity != "Proprietory" && data.typeOfEntity != "Partnership Firm"){
                    $("[data-type='Proprietory']").remove();
                }

                if(data.typeOfEntity == "Private Limited" || data.typeOfEntity == "Public Limited"){
                    console.log();
                    if(document.querySelector("[data-type='Proprietory']") != null){
                        document.querySelector("[data-type='Proprietory']").style.display = "none";
                    }
                    var option = "<option value='"+ data.director1FullName +"'>"+ data.director1FullName +"</option>";
                    var option2 = "<option value='"+ data.director2FullName +"'>"+ data.director2FullName +"</option>";
                    $("[data-id='director1FullName']").append(option);
                    $("[data-id='director2FullName']").append(option2);
                    $("[data-id='director1FullName'] option[value='"+ data.director1FullName +"']").attr("selected", "selected");
                    $("[data-id='director2FullName'] option[value='"+ data.director2FullName +"']").attr("selected", "selected");

                }
                // var selectBox = document.querySelectorAll(".director-name-select");
                // selectBox.forEach(function(e){
                //     e.style.display = "none";
                // })
                if(that.hasAttribute("data-id")) {
                    var _directorKey = that.getAttribute("data-id");
                    that.value = data[_directorKey];
                }
            }
        }
    }); 



    if(data.stage == "00"){
        // $("#tradingState").val("");
        var option = "<option value='"+ data.tradingState +"'>"+ data.tradingState +"</option>";
        $("#tradingState").append(option);
        $("#tradingState option[value='"+ data.tradingState +"']").attr("selected", "selected");
        if(data.typeOfEntity == "Proprietory"){
            $("#businessEmailForCommunication").closest(".col-md-3").addClass("d-none");
        }
    } 
    if(data.typeOfEntity == "Proprietory"){
        $("[data-type='director one']").remove();
        $("[data-type='director two']").remove();
        // $("[data-companytype='Proprietory']").css("display", "block");
        $(".contact-detail-stage").remove();
        $("[data-other='other']").remove();
        $(".other-div").remove();
    }  


    if(data.typeOfEntity == "Proprietory"){
        $(".merchantSupport-all").remove();
    }else{

    }

    if(data.typeOfEntity == "")
    if(data.typeOfEntity == "Partnership Firm"){
        $("[data-type='Proprietory']").remove();
        $("[data-type='director one'] > label").text("Partner One");
        $("[data-type='director two'] > label").text("Partner Two");
    }

}

function getAllData(){
    var _parent = document.querySelector("#stage-05");
    var _selector = _parent.querySelectorAll("[data-id]");
    _selector.forEach(function(index, array, element){
        if(_selector[array].innerHTML == ""){
            _selector[array].closest(".mpaSectionData-box").classList.add("d-none");
        }
    })
}

function changeLabel(_typeOfEntity){
    var _parent = document.querySelector("#stage-05");
    var _selector = _parent.querySelectorAll("[data-target]");
    if(_typeOfEntity == "Partnership Firm"){
        var _title = "Partner";
    }else if(_typeOfEntity == "Private Limited" || _typeOfEntity == "Public Limited"){
        var _title = "Director";
    }else if(_typeOfEntity == "Proprietory"){
        var _title = "Proprietor";
    }else{
        var _title = "Partner"
    }

    _selector.forEach(function(index, array, element){
        _selector[array].innerHTML = _title;
    })
}


function defaultData(data){ 
    if(data.stage == "03" && data.annualTurnover == null){
        $(".mpa-gst-popup").fadeIn();
    }else if(data.stage == "03" && data.annualTurnover != ""){
        $(".mpa-gst-popup").fadeOut();
    }

    

    if(data.stage == "05"){

        var _parent = document.querySelector("#stage-05");
        for(key in data){
            var _check = document.querySelector("[data-id='"+key+"']");
            if(_check != null){
                _parent.querySelector("[data-id='"+key+"']").innerHTML = data[key];
            }
        }

        getAllData();

        changeLabel(data.typeOfEntity);

        document.querySelector(".button-wrapper").classList.add("d-none");
        document.querySelector(".onboarding-merchant").style.backgroundColor = "#eee"; 

    }
    
    // condition base  show data

    if(!data.typeOfEntity) {

        for(key in data){
            document.getElementById(key).value = data[key];
            if(data[key] == ""){
                document.getElementById(key).removeAttribute("readonly");
            }else{
                document.getElementById(key).setAttribute("readonly", true);
                // console.log(key);
            } 
             
        }
        
    } else {
        // if(data.stage == "07" && data.typeOfEntity == "Partnership Firm"){
        //     $(".proprietorship").remove();
        //     $(".limited").remove();
        //     $(".otherEntity").remove();
        // }
        if(data.typeOfEntity == "Private Limited" || data.typeOfEntity == "Public Limited"){
            $("[data-type='Proprietory']").html("");
            // $(".mpa-input-alt input[value='"+data.typeOfEntity+"']").prop("checked", true);
            checkFieldEmpty();
            $(".partnership").remove();
            $(".proprietorship").remove();
            $(".otherEntity").remove();

        }else if(data.typeOfEntity == "Proprietory"){
            $("[data-type='director one']").remove();
            $("[data-type='director two']").remove();
            $("[data-companytype='Proprietory']").css("display", "block");
            $(".contact-detail-stage").remove();
            $("[data-other='other']").remove();
            $(".other-div").remove();
            $(".partnership").remove();
            $(".limited").remove();
            $(".otherEntity").remove();
        }
        else if( data.typeOfEntity == "Partnership Firm" ){ 
            $("[data-type='director one'] > label").text("Partner One");
            $("[data-type='director two'] > label").text("Partner Two");
            $(".limited").remove();
            $(".otherEntity").remove();
            $(".proprietorship").remove();
            $(".director-name-select").html("");
            $("[data-type='Proprietory']").html("");
        }
        else{
            $("[data-type='director one'] > label").text("Partner One");
            $("[data-type='director two'] > label").text("Partner Two");
            $(".limited").remove();
            $(".proprietorship").remove();
            $(".partnership").remove();
            $(".director-name-select").html("");
            $("[data-type='Proprietory']").html("");

        }

    }
}

$(".director-list").on("change", function(e){
    $(".row").removeClass("active-director-row");
    $(this).closest(".row").addClass("active-director-row");
    var getDataInfo = $('option:selected', this).attr('data-list'); // director1FullName;
    var getExactId = getDataInfo.replace("FullName", "");
    $("#"+getExactId+"Pan").val($("[data-id='"+getExactId+"Pan']").val());
    $(".active-director-row textarea").val($("[data-id='"+getExactId+"Address']").val());
    $(".active-director-row textarea").next("p").css("display","block");
})

var getDirectorOne = $("[data-id='director1FullName']").val();
    var getDirectorTwo = $("[data-id='director2FullName']").val();

// $(".director-list").on("change", function(e){
//     var getVal = $(this).val();
//     if(getDirectorOne == getDirectorTwo && getDirectorTwo == getDirectorTwo){
//     }else{
//     }
// })


// keyup function upper case
$(".input-caps").on("keyup", function(e){
    this.value = this.value.toUpperCase();
}); 


// check field empty or not 
function checkFieldEmpty() {
    var allInput = document.querySelectorAll(".stage-box.active .mpa-input");
    for (var i = 0; i < allInput.length; i++) {
        if (allInput[i].value == "") {
            allInput[i].removeAttribute("readonly");
            allInput[i].removeAttribute("disabled");
        }
        else{
            allInput[i].setAttribute("readonly", true); 
        }
    }
}

function getCin(data) {
    $(".loader-container").fadeIn();
    var getCinFromName = data.cin;
    if (getCinFromName != "" && getCinFromName.length > 8) {
        for (key in data) {
            document.getElementById(key).value = data[key];
        }
    }
    checkFieldEmpty(); // run function to check empty value blank
    document.getElementById("gstin").setAttribute("readonly", true);
    $(".loader-container").fadeOut();
}


// only letters
function onlyLetters(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
        
    } else {
        event.preventDefault();
    }
}

// digit backspace dot
function digitDot(event) {
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46) {
    } else {
        event.preventDefault();
    }
}

// only digit 

function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// letters and alpabet
function lettersAndAlphabet(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// $(".landline-input").val(" ");

$(".landline-input").removeClass("mpa-input");

$(".landline-input").on("blur", function(e){
    var getLandLine = $(this).val();
    if(getLandLine.length > 0){
        $(this).addClass("mpa-input");
        if(getLandLine.length < 10){
            $(this).addClass("red-line");
        }else{
            $(this).removeClass("red-line");
        }
    }else{
        $(this).removeClass("mpa-input"); 
        $(this).removeClass("red-line");
    }
    checkAllField(e)
})

function checkAllField(e) {
    var _parent = document.querySelector(".stage-box.active");
    var _inputBoxes = _parent.querySelectorAll(".mpa-input");
    var flag = false;
    var saveBtn = document.getElementById('btn-next-stage');
    // if($("#companyPhone").val == "")
    var _landLine = document.querySelector("#companyPhone").value;
    if(_landLine == ""){
        flag = true;
    }
    for(var i = 0; i < _inputBoxes.length; i++) {
        var isMpaInputAlt = _inputBoxes[i].classList.contains("mpa-input-alt");
        // var isLandLine = _inputBoxes[i].classList.contains("landline-input");
        if(isMpaInputAlt) {
            var _innerInput = _inputBoxes[i].querySelectorAll("input");
            for(var j = 0; j < _innerInput.length; j++) {
                if(_innerInput[j].checked == true) {
                    flag = true;
                    break;
                } else {
                    flag = false;
                }
            }            
        } else {
            if(_inputBoxes[i].value !== "") {
                flag = true;
            }   else {
                flag = false;
                break;
            }
        }
    }
    if(flag == true) {
        if($(".stage-box.active .red-line").length == 0){
            saveBtn.disabled = false;
        }else{
            saveBtn.disabled = true; 
        }
    } else {
        saveBtn.disabled = true;
    }
} 




$(document).ready(function() {

    // validation for PAN card
    var _checkPan = document.querySelector("[data-id='director2Pan']");
    if(_checkPan != null){
        var _selectorPan = document.querySelectorAll(".pan-validation-director");
        _selectorPan.forEach(function(index, array, element){
            _selectorPan[array].addEventListener('blur', function(e){
                var _pan1 = document.querySelector("[data-id='director1Pan']").value;
                var _pan2 = document.querySelector("[data-id='director2Pan']").value;
                if(_pan1 == _pan2){
                    index.classList.add("red-line");
                }else{
                    if(_checkpan == false){

                    }else{
                        index.classList.remove("red-line");
                    }
                }
            })
        })
    }

    $("#landlineNumber").on("click", function(e){
        if($(this).is(":checked")){
            $(".landline-div").removeClass("d-none");
            $(".landline-div").find("input").addClass("mpa-input");
            $(".mobileNumber-div").addClass("d-none");
            $(".mobileNumber-div").find("input").removeClass("mpa-input");
        }else{
            $(".landline-div").addClass("d-none");
            $(".landline-div").find("input").removeClass("mpa-input");
            $(".mobileNumber-div").removeClass("d-none");
            $(".mobileNumber-div").find("input").addClass("mpa-input");
        }
        checkAllField(this);
    });

    $("#landlineNumber-pro").on("click", function(e){
        if($(this).is(":checked")){
            $(".landline-div").removeClass("d-none");
            $(".landline-div").find("input").addClass("mpa-input");
            $(".mobileNumber-div").addClass("d-none");
            $(".mobileNumber-div").find("input").removeClass("mpa-input red-line");
        }else{
            $(".landline-div").addClass("d-none");
            $(".landline-div").find("input").removeClass("mpa-input");
            $(".mobileNumber-div").removeClass("d-none");
            $(".mobileNumber-div").find("input").addClass("mpa-input");
        }
        checkAllField(this);
    });

    $("#merchantSupport").on("click", function(e){
        
        if($(this).is(":checked")){
           var _email = $("#contactEmail").val();
            var _mobile = $("#contactMobile").val();
            var _landline = $("#contactLandline").val();
            if(_email != "" && _mobile != "" || _landline != ""){
                $("#merchantSupportEmailId").val(_email);
                $("#merchantSupportMobileNumber").val(_mobile);
                $("#merchantSupportLandLine").val(_landline);
            }else{
                alert("Please fill above field");
                return false;
            }
        }else{
            $("#merchantSupportEmailId").val("");
            $("#merchantSupportMobileNumber").val("");
            $("#merchantSupportLandLine").val("");
        }
        checkAllField(this);
    });

    $("#merchantSupport-pro").on("click", function(e){
        
        if($(this).is(":checked")){
           var _email = $("[data-id='director1Email']").val();
            var _mobile = $("[data-id='director1Mobile']").val();
            var _landline = $("[data-id='director1Landline']").val();
            if(_email != "" && _mobile != "" || _landline != ""){
                $("#merchantSupportEmail").val(_email);
                $("#merchantSupportMobileNumber").val(_mobile);
                $("#merchantSupportLandLine").val(_landline);
            }
            else{
                alert("Please fill above field");
                return false;
            }
        }else{
            $("#merchantSupportEmail").val("");
            $("#merchantSupportMobileNumber").val("");
            $("#merchantSupportLandLine").val("");
        }
        checkAllField(this);
    })

    $(".checkbox-label input").on("change", function(e){
        if($(this).is(":checked")){
          $(this).closest("label").addClass("checkbox-checked");
        }else{
          $(this).closest("label").removeClass("checkbox-checked");
        }
      });
    // logout 

    $("#logout").on("click", function(e){
        e.preventDefault();
        $.ajax({
            type: "post",
            url: "logout",
            success: function(data){
            },
            error: function(data){
            }
        })
    })

    // term and condition box
    $("#checkTerm input").on("change", function(e){
        if($(this).is(":checked")){
            $(".term-btn button").attr("disabled", false);
        }else{
            $(".term-btn button").attr("disabled", true);
        }
    })

    $(".term-btn button").click(function(e){
        $(".term-condition-popup").fadeOut();
    })

    // full refund box 

    $("#refundsAllowed input").on("click", function(e){
        var getCheckboxVal = $(this).val();
        if($(this).is(":checked")){
            if(getCheckboxVal == "Full Refund" || getCheckboxVal == "Partial Refund"){
                $("input[value='Exchange Only']").attr("disabled", true);
                $("input[value='No Refund']").attr("disabled", true); 
            }
            if(getCheckboxVal == "No Refund"){
                $("input[value='Full Refund']").attr("disabled", true);
                $("input[value='Exchange Only']").attr("disabled", true);
                $("input[value='Partial Refund']").attr("disabled", true);
            }
            if(getCheckboxVal == "Exchange Only"){
                $("input[value='Full Refund']").attr("disabled", true);
                $("input[value='No Refund']").attr("disabled", true);
                $("input[value='Partial Refund']").attr("disabled", true);
            }
        }else{
            $("input[value='No Refund']").attr("disabled", false);
            $("input[value='Full Refund']").attr("disabled", false);
            $("input[value='Partial Refund']").attr("disabled", false);
            $("input[value='Exchange Only']").attr("disabled", false);
        }
    })

    // select entity type
     // select entity type
     $(".mpa-input-alt input[type='radio']").on("change", function(){
        var entityValue = $(this).val();
        if(entityValue == "Private Limited" || entityValue == "Public Limited"){
            var childernImp = $("[data-change='cin']").find("label").children();
            $("[data-change='cin']").find("label").text("CIN");
            $("[data-change='cin']").find("input").addClass("mpa-has-length");
            $("[data-change='cin']").find("label").append(childernImp);
            $("[data-change='cin']").find("input").attr("id", "cin");
            $("#companyName").addClass("companyNameApi");
            $(".mpa-input").attr("readonly", false);
            $(".mpa-input-select").attr("disabled", false);
            $("#businessEmailForCommunication").attr("readonly", false);
            $("#companyName").attr("readonly", false);
            $("#dateOfIncorporation").attr("readonly", true);
            
        }else{
            $("#companyName").removeClass("companyNameApi");
            $("[data-change='cin']").find("input").attr('id', 'registrationNumber');
            $(".mpa-input").attr("readonly", false);
            $("[data-change='cin']").find("input").removeClass("mpa-has-length");
            $(".mpa-input-select").attr("disabled", false); 
            $("#dateOfIncorporation").attr("readonly", true);
        }
        if(entityValue == "Proprietory"){
            var childernImp = $("[data-change='cin']").find("label").children();
            $("[data-change='cin']").find("label").text("Shop Establishment Number");
            $("[data-change='cin']").find("label").append(childernImp);
            $("[data-change='cin']").find("input").addClass("registrationNumberApi");
            $("#businessPan, #tradingState, #companyEmailId").removeClass("businessPanFunc");
            $("#businessPan").attr("data-pan", "businessPanFunc");
            $("#businessEmailForCommunication").closest(".col-md-3").hide();
            $("#businessEmailForCommunication").removeClass("mpa-input");
        }else{
            $("[data-change='cin']").find("input").removeClass("registrationNumberApi");
            $("#businessPan, #tradingState, #companyEmailId").addClass("businessPanFunc");
            $("#businessPan").removeAttr("data-pan");
            $("#businessEmailForCommunication").closest(".col-md-3").show();
            $("#businessEmailForCommunication").addClass("mpa-input");

        }
        if(entityValue == "Partnership Firm" || entityValue == "other"){
            var childernImp = $("[data-change='cin']").find("label").children();
            $("[data-change='cin']").find("label").text("Registration Number");
            $("[data-change='cin']").find("label").append(childernImp);

        }
        if(entityValue == "other"){
            $(".other").attr("id", "typeOfEntity");
            $(".other").addClass("mpa-input mpaInput");
        }else{
            $(".other").attr("id", "");
            $(".other").removeClass("mpa-input mpaInput");
        }
    })

    // check length of character equal 
    $("body").on("input", ".mpa-has-length", function(e){
        var getMaxVal = $(this).attr("maxlength");
        if(getMaxVal){
            var getInputLength = $(this).val();
            if(getMaxVal == getInputLength.length){
                $(this).removeClass("red-line");
            }else{
                $(this).addClass("red-line");
            }
        }else{
        }
    })


    // COUNTRY DEFAULT SELECTED
    populateCountries("tradingCountry", "tradingState");
    populateStates("tradingCountry", "tradingState");

    $("#tradingCountry").on("change", function(e){
        checkAllField(e);
    })

    $("#tradingState").on("change", function(e){
        checkAllField(e);
    })

    var dataCountry = $("#tradingCountry").val();
    if (dataCountry !== "") {
        $("#tradingCountry").val(dataCountry);
    }

    // STATE DEFAULT SELECTED
    var dataState = $("#tradingState").val();
    if (dataState !== "") {
        $("#tradingState").val(dataState);
    }

    // when slected type of entity legal name
    $(".stage-box.active input[type='radio']").click(function(){
        $("#companyName").attr("readonly", false);
    })

    $("#companyName").on('blur', function(e){
        var _getCompanyName = $(this).val();
        if(_getCompanyName.length > 1){
            $("#typeOfEntity").find("input[type='radio']").attr("disabled", true);
            $("#industryCategory").attr("disabled", true);
        }
    })

    // get company info on call api
    /* function getCompanyName(){
        var thisInput = $(this);
        document.querySelector(".loader-container").style.display = "block";
        var getCinValue = document.getElementById("cin").value;
        var legalNameLength = $("#companyName").val();
            $.ajax({
                type: "post",
                url: "companyName",
                timeout: 18000,
                data: {
                    "companyName": legalNameLength,"payId": _payId
                },
                success: function(data) {
                    if (!data.mpaData.ERROR) {
                        getCinValue = data.mpaData.cin;
                        if (getCinValue != "") {
                            getCin(data.mpaData);
                            getCinBasedData(data.mpaData);
                        }else{
                            $("#cin").removeAttr("readonly");
                        }
                        $("#companyName").next(".error-msg").text();
                    } else {
                        $("#companyName").next(".error-msg").text(data.mpaData.ERROR);
                        $("#companyName").next(".error-msg").fadeIn();
                    }
                    document.querySelector(".loader-container").style.display = "none";
                },
                error: function(data){
                    $(thisInput).next(".error-msg").text("Server Issue Please Try Again");
                    $(".loader-container").fadeOut();
                }

            })
        } */

    // company name for not inserted limited
    // company name validator
    function NameValidater() {
        var getName = $("#companyName").val();
        var findWord = getName.match(/limited/i);
        var nameValidate = getName.lastIndexOf(findWord);
        if (nameValidate != -1 && getName.length > 9) {
            $(this).next(".error-msg").fadeOut();
            $(this).removeClass("red-line");
            // getCompanyName();
            return true;
        } else {
            $(this).next(".error-msg").fadeIn();
            $(this).next(".error-msg").text("Please enter full name precisely");
            $(this).addClass("red-line");
            return false;
        }
    }
    // company api call
    $("body").on("input",".companyNameApi", NameValidater);


    $( "#dateOfIncorporation" ).datepicker({
        dateFormat : 'mm/dd/yy',
        changeMonth : true,
        changeYear : true,
        yearRange: '-100y:c+nn',
        maxDate: '-1d',
        yearRange: "-100:+0"
    });

    function setActiveStage(stage) {
        var getAllDiv = $(".stage-box").length - 1;
        var getTotalWidth = $(".progressDefault").width();
        getWidth = getTotalWidth / getAllDiv * stage;
        $("#progressStatus").animate({width: getWidth+'px'});
        $(".stage-box").removeClass("active");
        $("#stage-" + stage).addClass("active");
        var _currentStage = $("[data-active=stage-" +stage+ "]");
        _currentStage.prevAll().addClass("active");        
        _currentStage.addClass("active");
        // CHANGE PREV BUTTON STAGE
        var btnPrev = $("#btn-prev-stage");
        var _prevStage = stage - 1;
        btnPrev.attr("data-stage", "0" + _prevStage); // data.mpaData.stage - 1;
    }


    //get category list{}
    function categoryListing(data){
        console.log(data);
        var createOption = "";
        createOption = "<option value='-1'>Select Category</option>";
        for(key in data){
            createOption += "<option value='"+data[key]+"'>"+data[key]+"</option>";
        }
        $("#industryCategory").append(createOption);
    }

    $("#industryCategory").on("change", function(e){
        var _val = $(this).val();
        if(_val == "-1"){
            $("#typeOfEntity").find("input").attr("disabled", true);
        }else{
            $("#typeOfEntity").find("input").attr("disabled", false);
        }
    })

    function fetchStage (){
        $(".loader-container").fadeIn();
        var stage = "";
        var _payId = $("#merchantPayId").val();
        $.ajax({
            type: "POST",
            url: "fetchStage",
            data: {"stage": stage, "payId": _payId },
            success: function(data){
                console.log(data);
                categoryListing(data.industryCategoryList);
                if(data.mpaData.stage == "00" && data.mpaData.companyName == null){
                    $(".term-condition-popup").fadeIn();
                }
                $(".stage-box").removeClass("active");
                $("#stage-" + "00").addClass("active");
                defaultData(data.mpaData);
                setActiveStage(data.mpaData.stage);
                var activeStage = $(".stage-box.active").attr("id");
                activeStage = activeStage.slice(6, activeStage.length);
                if(activeStage == "00"){
                    $("#btn-prev-stage").addClass("inactive");
                }else{
                    $("#btn-prev-stage").removeClass("inactive");
                }

                if(data.mpaData.typeOfEntity == "Proprietory"){
                    $(".merchantSupport-all").remove();
                }else{
                    
                }

                $(".loader-container").fadeOut();

            }
        })
    }

    fetchStage();

    /* function getRegistrationDetail(){
        var registrationNumber = $("#registrationNumber").val();
        var tradingState = $("#tradingState").val();
        if(registrationNumber != "" && registrationNumber != undefined && tradingState != ""){
            if(tradingState == "West Bengal" || tradingState == "Telangana" || tradingState == "Delhi" || tradingState == "Haryana" || tradingState == "Rajasthan" || tradingState == "Uttar Pradesh" || tradingState == "Maharashtra"){
                $(".loader-container").fadeIn();
                $.ajax({
                    type: "post",
                    url: "snecs",
                    data: {registrationNumber: registrationNumber, tradingState: tradingState, "payId": _payId},
                    success:function(data){
                        if(!data.mpaData.ERROR){
                            defaultData(data.mpaData);
                        }
                        $(".loader-container").fadeOut();
                    },
                    error: function(data){
                        $(".loader-container").fadeOut();
                    }
                })
            }
        }else{
        }
    }

    $("body").on("change", "#registrationNumber, #tradingState", getRegistrationDetail); */

    


    /* function getGst() {
        var thisInput = $(this);
        var businessPan = document.getElementById("businessPan").value;
        var tradingState = document.getElementById("tradingState").value;
        var companyEmailId = document.getElementById("companyEmailId").value;
        var companyName = document.getElementById("companyName").value;
        if (businessPan != "" && tradingState != "" && companyEmailId != "") {
            if($("#businessPan").hasClass("red-line")){
            }else{
                document.querySelector(".loader-container").style.display = "block";
                $.ajax({
                    type: "post",
                    url: "panToGst",
                    timeout: 180000,
                    data: { "businessPan": businessPan, "tradingState": tradingState, "companyEmailId": companyEmailId, "companyName": companyName, "payId": _payId },
                    success: function(data) {
                        if(!data.mpaData.ERROR){
                            document.getElementById("gstin").value = data.mpaData.gstin;
                            document.querySelector(".loader-container").style.display = "none";
                            $(".pan-validation").next(".error-msg").text("");
                        }else{
                            $(".pan-validation").next(".error-msg").text(data.mpaData.ERROR);
                            document.querySelector(".loader-container").style.display = "none";
                        }
                    },
                    error: function(data){
                        document.querySelector(".loader-container").style.display = "none";
                        $(thisInput).next(".error-msg").text("Server Issue Please Try Again");
                    }
                    
                })
            }
        }
    }

    $("body").on("change",".businessPanFunc", getGst);

    $("body").on("blur", "[data-pan='businessPanFunc']", function(e){
        var companyName = $("#companyName").val();
        var companyEmailId = $("#businessPanFunc").val();
        var businessPan = $("#businessPan").val();
        if(companyName != "" && companyEmailId != "" && businessPan != ""){
            $(".loader-container").fadeIn();
            $.ajax({
                type: "post",
                url: "businessPanVerification",
                data: { companyName: companyName, companyEmailId: companyEmailId, businessPan: businessPan, "payId": _payId },
                success: function(data){
                    if(!data.mpaData.ERROR){
                        
                    }else{
                        
                    }
                    $(".loader-container").fadeOut();
                }
            })
        }else{
        }
    }) */

    $("input").attr("autocomplete", "nope");
    $("textarea").attr("autocomplete", "nope");

    // email validation
    $(".mail-validation").on("input", function(e){
        var emailRegex = /^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$/;
        var emailVal = $(this).val();
        if(emailVal.match(emailRegex)){
            $(this).removeClass("red-line");
        }else{
            $(this).addClass("red-line");
        }
    })

    $("#gstin").on("input", function(e){
        var _regEx = /\d{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z\d]{1}/;
        var _email = $(this).val();
        if(_email.match(_regEx)){
            $(this).removeClass("red-line");
        }else{
            $(this).addClass("red-line");
        }
    })

    

    
    
    var _checkpan = false;

    //pan validation
    $(".pan-validation").on("input", function(e){
        var emailRegex = /^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$/;
        var emailVal = $(this).val();
        if(emailVal.match(emailRegex)){
            $(this).removeClass("red-line");
            _checkpan = true;
        }else{
            $(this).addClass("red-line");
            _checkpan = false;
        }
    })



    

    // website validaion
    $(".website-validator").on("input", function(){
        var emailRegex = /^([A-Za-z'])+\.([A-Za-z0-9'])+\.([A-Za-z]{2,6})$/;
        var emailVal = $(this).val();
        if(emailVal.match(emailRegex)){
            $(this).removeClass("red-line");
        }else{
            $(this).addClass("red-line");
        }
    })


    
    // SUBMIT BUTTON CLICKED
    $("#btn-next-stage").on("click", function(e) {

        // $(".loader-container").fadeIn();
        $("body").removeClass("loader--inactive");
        e.preventDefault();
        var _getStage = parseInt($(this).prev("button").attr("data-stage"));
        var _stage = "0"+(_getStage + 1);
        if(_stage == "04"){
            $("#fancybox").fancybox({
                'overlayShow': true
            }).trigger('click');
        }else{

            var _parent = $(".stage-box.active");
            var _eleID = _parent.attr("id");
            var _stage = _eleID.slice(6, _eleID.length);
            var _payId = $("#merchantPayId").val();
        
            var dataObj = {
                stage: _stage,
                payId : _payId
    
            };
    
        
            _parent.find(".mpa-input").each(function(index) {
                var that = $(this);
    
                if (that.hasClass("mpa-input-alt")) {
                    let _key = that.attr("data-key");
                    if(that.hasClass("mpa-input-check")) {
                        var checkVal = [];
            
                        that.find("input:checked").each(function(i) {
                            checkVal[i] = $(this).val();
                        });
    
                        var checkBoxValue = checkVal.join(",");
    
                        dataObj[_key] = checkBoxValue;
    
                    } else {
                        dataObj[_key] = that.find("input:checked").val();
                    }
                } else {
                    let _key = that.attr("id");
                    dataObj[_key] = that.val();
    
                    if(_stage == "01") {
                        
                        if(that.attr("data-id") !== undefined) {
                            let _innerKey = that.attr("data-id");
                            dataObj[_innerKey] = that.val();
                        }
                    }
                }
    
            });
    
            $.ajax({
                type: "POST",
                url: "saveStage",
                data: dataObj,
                success: function(data) {
                    // lockAllField();
                    var _currentStage = data.mpaData.stage;
                    setActiveStage(_currentStage);
                    if (_currentStage !== $(".stage-box").length) {
                        var btnPrev = $("#btn-prev-stage");
                        $("[data-active=stage-"+_currentStage+"]").addClass("active");
                        $(".stage-box").removeClass("active");
                        $("#stage-" + _currentStage).addClass("active");
                        $("[data-active=stage-"+_currentStage+"]").addClass("active");
                        defaultData(data.mpaData);
                        btnPrev.removeClass("inactive");
                        $("#btn-next-stage").attr("disabled", true);
                         
                        var _prevStage = _currentStage - 1;
                        btnPrev.attr("data-stage", "0" + _prevStage); // data.mpaData.stage - 1;
    
                        if(data.mpaData.typeOfEntity == "Proprietory"){
                            $(".merchantSupport-all").remove();
                        }else{
                            
                        }
    
                        if(data.mpaData.stage == "04"){
                            var _allInput = document.querySelectorAll(".upload-generic b");
                            if(_allInput.length == 0){
                                $("#btn-next-stage").attr("disabled", false);
                            }else{
                                $("#btn-next-stage").attr("disabled", true);
                            }
                        }

                        if(data.mpaData.stage != "05"){
                            checkAllField(this);
                        }
    
                        $(".loader-container").fadeOut();
                        $("html, body").animate({ scrollTop: 0 }, 600);
                    } else {
                        alert("Limit exceeds. Please redirect the page.");
                    }
        
                }
            });
        }
    });
    
    
    // PREV BUTTON CLICKED
    $("#btn-prev-stage").on("click", function(e) {
        e.preventDefault();
        $(".loader-container").fadeIn();
        var that = $(this);
        var _parent = $(".stage-box");
        var _id = $(".stage-box.active").attr("id");
        var _prevStage = that.attr("data-stage"); //01
        

        $.ajax({
            type: "POST",
            url: "fetchStage",
            data: {
                stage: _prevStage,
                "payId": _payId
            },
            success: function(data){
                console.group("previos data");
                console.groupEnd();
                _parent.removeClass("active");
                $("#btn-next-stage").attr("disabled", false);
                $("#mpaSectionIndicator").find("[data-active='" + _id + "']").removeClass("active");

        getWidth = getWidth - getTotal;
        $("#progressStatus").animate({width: getWidth+'px'});

        $("#stage-" + _prevStage).addClass("active");
    
        fetchPreviousData(data.mpaData);

        // all input and select box readonly
        $(".stage-box.active input[type='text']").attr("readonly", false);
        $(".stage-box.active input[type='email']").attr("readonly", false);
        $(".stage-box.active input[type='radio']").attr("disabled", true);
        // $(".stage-box.active input[type='file']").closest("labeL").find("img").attr("src", "../image/ok.png");
        $(".stage-box.active input[type='file']").closest("labeL").find("span").text("Your file has been uploaded");
        // $(".stage-box.active input[type='file']").attr("src", '../image/ok.png');
        $(".stage-box.active input[type='checkbox']").attr("disabled", false);
        $(".stage-box.active select").attr("disabled", false);
        $(".stage-box.active textarea").attr("readonly", false);
        
        var activeStage = $(".stage-box.active").attr("id");
        activeStage = activeStage.slice(6, activeStage.length);
    
        _prevStage = _prevStage - 1;
    
        that.attr("data-stage", "0" + _prevStage.toString());
    
        if (activeStage == "00") {
            that.addClass("inactive");
        }
        $(".loader-container").fadeOut();
    }
        })

    });

    /* $("#verifyGo").click(function(e){
        $(".loader-container").fadeIn();
        $.ajax({
            url: "processCheque",
            data: {"payId": _payId},
            timeout: 18000,
            success: function(data){
                if(!data.mpaData.ERROR){
                    defaultData(data.mpaData);
                    $(".verify-btn").css("display", "none");
                    var _inputBoxes = $(".stage-box.active .mpa-input");
                    _inputBoxes.each(function(e){
                        var getVal = $(this).val();
                        if(getVal == ""){
                            $("#btn-next-stage").attr("disabled", true);
                        }else{
                            $("#btn-next-stage").attr("disabled", false);  
                        }
                    })
                }else{
                    // $(".error-msg-global").html(data.mpaData.ERROR);
                    $(".verify-btn").css("display", "none");
                    $(".stage-box.active .mpa-input").attr("readonly", false);
                }
                $(".loader-container").fadeOut();
            },
            error: function(data){
                $(".stage-box.active .error-msg-global").text("Try Again Server Timeout");
                $(".stage-box.active .mpa-input").attr("readonly", false);
                $(".loader-container").fadeOut();
            }
        })
    })
    

    // upload dl function

    $(".director-address").attr("readonly", true); */

    $(".director-address").click(function(){
        var getDirectorInfo = $(this).attr("data-name");
        $(".varification-main-div").attr("data-director",getDirectorInfo);
        $(".open-address-popup").fadeIn();
        $("input[name='varification']").click(function(){
            var getCheckVal = $(this).val();
            $(".varification-div").hide();
            $("."+getCheckVal+"-div").show();
        })
    })
    $(".cncl").click(function(e){
        var closePopup = $(this).attr("close-model");
        $("."+closePopup).fadeOut();
        // var defaultFile = $(".upload-file").val();
        // defaultFile = "";
    })
    // other address

    $("#other-add-btn").click(function(e){
        var otherAddress = $("#other-address").val();
        var getDirectorInfo = $(this).closest(".varification-main-div").attr("data-director");
        $("[data-name='"+getDirectorInfo+"']").val(otherAddress);
        $("#other-address").val("");
        $(".open-address-popup").fadeOut();
        checkAllField(e);

    })

    // upload local file
    $("body").on("change", ".upload-local-file", function(e){
        $(".loader-container").fadeIn();
        var getFormClass = $(this).closest("form").attr("class");
        var form = $("."+getFormClass)[0];
        var data = new FormData(form);
        data.append("payId", _payId);
        // var fileName
        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: "processImage",
            data: data,
            processData: false,
            contentType: false,
            cache: false,
            timeout: 600000,
            success: function(data){
                if(!data.mpaData.ERROR){
                    $("."+getFormClass).find("img").attr("src", data.mpaData.base64)
                    $(".loader-container").fadeOut();
                }else{
                    $(".logo-upload-error").text(data.mpaData.ERROR);
                    $(".loader-container").fadeOut();
                }
                $(".upload-logo").addClass("active-logo");
            }
        })
    })

    // function for make generic uploader

    $("body").on("change", ".generic-uploader", function(e){
        var getFormClass = $(this).closest("form").attr("class");
        var _parent = $(this).closest("label");
        var form = $("."+getFormClass)[0];
        var file = $(this).val();
        var getFilePeriod = file.lastIndexOf(".");
        var getFileExact = file.slice(getFilePeriod);
        // $('#filePHOTO')[0].files[0].size
        var fileSize = $(this)[0].files[0].size;
        var data = new FormData(form);
        var getName = file.replace("C:\\fakepath\\", "");
        var fileName = $(this).attr("data-type");
        data.append("fileName", fileName);
        data.append("payId", _payId);
        if(fileSize <= 2097152 && getFileExact == ".png" || getFileExact == ".PNG" || getFileExact == ".pdf" || getFileExact == ".PDF" || getFileExact == ".jpeg" || getFileExact == ".JPEG" || getFileExact == ".jpg" || getFileExact == ".JPG"){
            $(".loader-container").fadeIn();
            $.ajax({
                type: "post",
                enctype: 'multipart/form-data',
                url: "uploadFile",
                data: data,
                processData: false,
                contentType: false,
                success: function(data){
                    _parent.find("span").text(getName);
                    $(_parent).removeClass("error-upload");
                    $(".loader-container").fadeOut();

                }
            })
        }else{
            _parent.find("span").text("Either file type not supported or file should not greater then 2mb");
            $("#btn-next-stage").attr("disabled", true);
            $(_parent).addClass("error-upload");
        }

    })


    // upload other file
    $("body").on("change", ".other-file-upload", function(e){
        var getFormClass = $(this).closest("form").attr("class");
        var form = $("."+getFormClass)[0];
        var data = new FormData(form);
        var file = $(this).val();
        var getName = file.replace("C:\\fakepath\\", "");
        var fileName = $(this).attr("data-type");
        // var fileContentType = $(this).attr("data-type");
        data.append("fileName", fileName);
        data.append("payId", _payId);
        // data.append("fileContentType", fileContentType);
        $.ajax({
            type: "post",
            enctype: 'multipart/form-data',
            url: "uploadFile",
            data: data,
            processData: false,
            contentType: false,
            success: function(data){
                $("."+getFormClass).find("span").text(getName);
            }
        })
    })

    

    $("#uploadCheque").change(function (event) {
        
        event.preventDefault();
        if(event.target.value != ""){

            var form = $('#fileUploadForm')[0];
            var data = new FormData(form);
            var _fileTypeIndex = event.target.files[0].type.lastIndexOf("/");
            data.append("payId", _payId);
            var _fileType = event.target.files[0].type.slice(_fileTypeIndex+1);
            if(_fileType == "png" || _fileType == "jpg" || _fileType == "pdf" || _fileType == "PNG" || _fileType == "JPG" || _fileType == "PDF" || _fileType == "jpeg" || _fileType == "JPEG"){
                $(".loader-container").fadeIn();
                var _fileName = event.target.files[0].name;
                $.ajax({
                    type: "POST",
                    enctype: 'multipart/form-data',
                    url: "processImage",
                    data: data, 
                    processData: false,
                    contentType: false,
                    cache: false,
                    timeout: 6000,
                    success: function (data) {
                        $(".upload-file").removeClass("mpa-input red-line");
                            $(".upload-file").closest("label").removeClass("redLine");
                            $(".file-msg").text("");
                            if(data.mpaData.base64 != ""){
                                if(_fileType == "PDF" || _fileType == "pdf"){
                                    $("#checqueImg").addClass("d-none");
                                    $(".file-msg").text(_fileName);
                                }else{
                                    $("#checqueImg").removeClass("d-none");
                                    $("#checqueImg").attr("src", data.mpaData.base64);
                                    $("#checqueImg").attr("alt", _fileName);
                                    $(".file-msg").text("");
                                }
                                $("#checqueImg").removeAttr("width height");
                                $("#checqueImg").css("max-width","100%");
                            }
                            $(".loader-container").fadeOut();
                            checkAllField(this);
                    },
                    error: function (e) {
                        $(".loader-container").fadeOut();
                        $("#result").text(e.responseText);
                        $("#btnSubmit").prop("disabled", false);
                    }
                });
            }else{
                
                $(".file-msg").text("File type not supported");
                $(".upload-file").closest("label").addClass("redLine");
                $(".upload-file").addClass("mpa-input red-line");
                $(".loader-container").fadeOut();
                checkAllField(this);
            }
        }

    });

    $("body").on("change", ".percentage-count", function(e){
        var getVal = $(this).val();
        if(getVal != ""){
            var newVal = Number(getVal).toFixed(2);
            $(this).val(newVal);
        }
    })

    $("#annualTurnover").on("change", function(e){
        var _that = Number($(this).val());
        if(_that < 10){
            $(this).val("");
            checkAllField(this);
        }else{
            // $(this).val(Number(_that).toFixed(2));
        }
    })

    // total six count //

    function newFunc(_selector, _msg){

        var _businessCount = document.querySelectorAll(_selector);
        _businessCount.forEach(function(index, array, element){
            _businessCount[array].addEventListener('blur', function(e){
                var _new = 0;
                var _check = true;
                _businessCount.forEach(function(ind, arr, ele){
                    // console.log(ind.value);
                    if(ind.value == ""){
                        _check = false;
                    }else{
                        _new = _new + parseInt(ind.value);
                        
                    }
                })
                if(_check == true){
                    console.log(_new);
                    if(_new == 100){
                        $(_msg).text("");
                    }else{
                        $(_selector).val("");
                        $('html, body').animate({scrollTop: $(_msg).offset().top }, 1000);
                        $(_msg).text("please filled value is equal to 100");
                        $("#btn-next-stage").attr("disabled", true);
                    }
                }
            })
        })
    }


    newFunc(".count-six-input", ".msg-turn-over");
    newFunc(".count-two-input", ".msg-card-turn-over");


    // annualTurnoverOnline
    $("#annualTurnoverOnline").on("blur", function(e){
        var getAnnualOnline = Number($(this).val());
        if(getAnnualOnline != "" && getAnnualOnline > 0){
            $(".annual-turnover").attr("readonly", false);
            $(".annual-turnover").addClass("mpa-input");
        }else{
            $(".annual-turnover").attr("readonly", true);
            $(".annual-turnover").removeClass("mpa-input");
        }
    })

    $(".percentage-count").change(function(e){
        $(this).val()+".00";

    })


    $("#cancel-btn").on("click", function(e){
       var _getClass = $(".fancybox-close-small");
        $("#btn-prev-stage").trigger("click");
        setTimeout(function(e){
            $("button.fancybox-close-small").trigger("click");
        }, 500);
    })

    $("#confirm-btn").on("click", function(e){
    	 setTimeout(function(e){
             $("button.fancybox-close-small").trigger("click");
         }, 500);
        $("#btn-prev-stage").attr("data-stage", "04");
        $("#btn-next-stage").trigger("click");
    })

    $(".termPolicy").on("click", function(e){
        var _this = $(this).attr("data-info");
        $("#docFile").val(_this);
        $("#downloadTnCPolicy").submit();
    })

}) 