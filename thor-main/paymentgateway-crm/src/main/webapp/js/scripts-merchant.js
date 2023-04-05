var getWidth = 0;
var getTotal = $(".mpaFormBulletsBox.active").width();
var _payId = $("#merchantPayId").text();



// loader in variable
function fetchPreviousData(data){


    
    console.group("get previous data");
    console.log(data);
    console.groupEnd();

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

                if(data.typeOfEntity != "Private Limited" && data.typeOfEntity != "Public Limited" && data.typeOfEntity != "Proprietory" && data.typeOfEntity != "Partnership Firm"){
                    $("[data-type='Proprietory']").remove();
                }

                if(data.typeOfEntity == "Private Limited" || data.typeOfEntity == "Public Limited"){
                    document.querySelector("[data-type='Proprietory']").style.display = "none";
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


function defaultData(data){ 
    if(data.stage == "03" && data.annualTurnover == null){
        $(".mpa-gst-popup").fadeIn();
    }else if(data.stage == "03" && data.annualTurnover != ""){
        $(".mpa-gst-popup").fadeOut();
    }

    if(data.stage == "08"){
        var column = "";
        for(key in data){
            // data for last stage
            column = "<div class='col-md-4'>";
            column += "<p>"+key+": <span>"+ data[key] +"</span></p>";
            column += "</div>";

            $("#last").append(column);
        }
        $(".button-wrapper").remove();
        
    }else{
        $(".button-pdf").remove();
    }

    // condition base  show data

    if(!data.typeOfEntity) {

        for(key in data){
            document.getElementById(key).value = data[key];
            if(data[key] == ""){
                document.getElementById(key).removeAttribute("readonly");
            }else{
                document.getElementById(key).setAttribute("readonly", true);
            } 
           
             
        }
        
    } else {
        $(".director-address").attr("readonly", true);
        // if(data.stage == "07" && data.typeOfEntity == "Partnership Firm"){
        //     $(".proprietorship").remove();
        //     $(".limited").remove();
        //     $(".otherEntity").remove();
        // }
        if(data.typeOfEntity == "Private Limited" || data.typeOfEntity == "Public Limited"){
            $("[data-type='Proprietory']").html("");
            // $(".mpa-input-alt input[value='"+data.typeOfEntity+"']").prop("checked", true);
            $(".director-name-input").html("");
            $(".director-address").next("p").css("display","none");
            checkFieldEmpty();
            var getValue = "";
            var count = "";
            var select = "<option value=''>Select Director</option>";
            for (key in data){
                getValue += "<input type='hidden' data-id='"+ key +"' value='"+ data[key] +"'>";
                count++;    
            }
            for (var i = 0; i < count; i++){
                var totalDirector = "director"+[i]+"FullName";
                if(data[totalDirector] == undefined){
                }else{
                    select += "<option data-list='"+totalDirector+"' value='"+ data[totalDirector] +"'>"+ data[totalDirector] +"</option>";
                }
            }
            $(".director-list").append(select);
            $("#getD").append(getValue);
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
    }else{
        $(this).removeClass("mpa-input"); 
    }
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
    });

    $("#landlineNumber-pro").on("click", function(e){
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
    });

    $("#merchantSupport").on("click", function(e){
        checkAllField(this);
        if($(this).is(":checked")){
           var _email = $("#contactEmail").val();
            var _mobile = $("#contactMobile").val();
            var _landline = $("#contactLandline").val();
            if(_email != "" && _mobile != "" && _landline != ""){
                $("#merchantSupportEmailId").val(_email);
                $("#merchantSupportMobileNumber").val(_mobile);
                $("#merchantSupportLandLine").val(_landline);
            }
        }else{
            $("#merchantSupportEmailId").val("");
            $("#merchantSupportMobileNumber").val("");
            $("#merchantSupportLandLine").val("");
        }
    });

    $("#merchantSupport-pro").on("click", function(e){
        checkAllField(this);
        if($(this).is(":checked")){
           var _email = $("[data-id='director1Email']").val();
            var _mobile = $("[data-id='director1Mobile']").val();
            var _landline = $("[data-id='director1Landline']").val();
            if(_email != "" && _mobile != "" && _landline != ""){
                $("#merchantSupportEmailId").val(_email);
                $("#merchantSupportMobileNumber").val(_mobile);
                $("#merchantSupportLandLine").val(_landline);
            }
        }else{
            $("#merchantSupportEmailId").val("");
            $("#merchantSupportMobileNumber").val("");
            $("#merchantSupportLandLine").val("");
        }
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
    $(".mpa-input-alt input[type='radio']").on("change", function(){
        var entityValue = $(this).val();
        if(entityValue == "Private Limited" || entityValue == "Public Limited"){
            var childernImp = $("[data-change='cin']").find("label").children();
            $("[data-change='cin']").find("label").text("CIN");
            $("[data-change='cin']").find("label").append(childernImp);
            $("[data-change='cin']").find("input").attr("id", "cin");
            $("#companyName").addClass("companyNameApi");
            $(".mpa-input").attr("readonly", true);
            $(".mpa-input-select").attr("disabled", true);
            $("#businessEmailForCommunication").attr("readonly", true);
            $("#companyName").attr("readonly", false);
            
        }else{
            $("#companyName").removeClass("companyNameApi");
            $("[data-change='cin']").find("input").attr('id', 'registrationNumber');
            $(".mpa-input").attr("readonly", false);
            $(".mpa-input-select").attr("disabled", false); 
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
    $(".mpa-has-length").on("keyup", function(e){
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

    // get company info on call api
    function getCompanyName(){
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
        }

    // company name for not inserted limited
    // company name validator
    function NameValidater() {
        var getName = $("#companyName").val();
        var findWord = getName.match(/limited/i);
        var nameValidate = getName.lastIndexOf(findWord);
        if (nameValidate != -1 && getName.length > 9) {
            $(this).next(".error-msg").fadeOut();
            $(this).removeClass("red-line");
            getCompanyName();
            return true;
        } else {
            $(this).next(".error-msg").fadeIn();
            $(this).next(".error-msg").text("Please enter full name precisely");
            // $(this).addClass("red-line");
            return false;
        }
    }
    // company api call
    $("body").on("input",".companyNameApi", NameValidater);

    //cin validator
    // function cinValidator(event){
    //     var getCin = $("#cin").val();
    //     if(getCin.length == 21){
    //         $(this).next(".error-msg").fadeOut();
    //         $(this).removeClass("red-line");
    //         if(event.keyCode == 8){
    //             return true;
    //         }else{
    //             event.preventDefault();
    //         }
    //         return true;
    //     }else{
    //         $(this).next(".error-msg").fadeIn();
    //         $(this).next(".error-msg").text("please type correct name");
    //         $(this).addClass("red-line");
    //     }
    // }


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
        var _payId = $("#merchantPayId").text();
        $.ajax({
            type: "POST",
            url: "fetchStage",
            data: {"stage": stage, "payId": _payId },
            success: function(data){
                console.group("fetch stage dta");
                console.groupEnd();
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

    function getRegistrationDetail(){
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

    $("body").on("change", "#registrationNumber, #tradingState", getRegistrationDetail);

    $("body").on("blur", "#cin", getCinBasedData);
    function getCinBasedData() {
        var cin = document.getElementById("cin").value;
        $(".loader-container").fadeIn();
        if(cin.length > 10){
            $.ajax({
                type: "post",
                url: "searchCin",
                data: {
                    "cin": cin,
                    "payId": _payId
                },
                success: function(data) {
                    if(!data.mpaData.ERROR){
                        getCin(data.mpaData);
                    }else{
                        $("#cin").next(".error-msg").text(data.mpaData.ERROR);
                        $("#cin").next(".error-msg").css("display","block");
                    }
                    $(".loader-container").fadeOut();
                },
                error: function(data) {
                    
                }
            });
        }else{
        }
    }


    function getGst() {
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
    })

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

    $(".ip-address").on("keydown", function(e){
        var newRegExp = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
        var ipVal = $(this).val();
        if(ipVal.match(newRegExp)){
            return false;
        }else{
            return true;
        }
    })

    //pan validation
    $(".pan-validation").on("input", function(e){
        var emailRegex = /^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?$/;
        var emailVal = $(this).val();
        if(emailVal.match(emailRegex)){
            $(this).removeClass("red-line");
        }else{
            $(this).addClass("red-line");
        }
    })


    // third party store 

    $("#third-party-store input").on("change", function(e){
        var getThirdValue = $(this).val();
        if(getThirdValue == "YES"){
            $(".third-party").attr("id", "thirdPartyForCardData");
            $(".third-party").addClass("mpa-input");
            $(".third-party").attr("readonly", false);
        }else{
            $(".third-party").attr("id", "");
            $(".third-party").removeClass("mpa-input");
            $(".third-party").attr("readonly", true);
        }
    })

    // server network 
    $("#serverDetails input").on("change", function(e){
        var getInputVal = $(this).val();
        if(getInputVal == "owned"){
            $(".shared-detail").attr("readonly", true);
            $(".shared-detail").removeClass("mpa-input");
        }else{
            $(".shared-detail").attr("readonly", false);
            $(".shared-detail").addClass("mpa-input");
        }
    })

    // express pay card

    $("#expressPay input").on("change", function(e){
        var _inputValue = $(this).val();
        if(_inputValue == "true"){
            $("#expressPayParameter").attr("disabled", false);
            $("#expressPayParameter").addClass("mpa-input");
        }else{
            $("#expressPayParameter").attr("disabled", true);
            $("#expressPayParameter").removeClass("mpa-input");
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

    function lockAllField(){
        $(".stage-box.active input").attr("readonly", true);
        $(".stage-box.active input[type='radio']").attr("disabled", true);
        $(".stage-box.active select").attr("disabled", true);
        $(".stage-box.active textarea").attr("disabled", true);
    }

    
    // SUBMIT BUTTON CLICKED
    $("#btn-next-stage").on("click", function(e) {
        $(".loader-container").fadeIn();
        e.preventDefault();
        var _parent = $(".stage-box.active");
        var _eleID = _parent.attr("id");
        var _stage = _eleID.slice(6, _eleID.length);
        var _payId = $("#merchantPayId").text();
    
        var dataObj = {
            stage: _stage,
            "payId": _payId

        };

    
        _parent.find(".mpa-input").each(function(index) {
            var that = $(this);

            // if(that.hasClass("mpa-input-check")){
            //     let _key = that.attr("data-check");
            //     dataObj[_key] = refundsAllowed;
            // }

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

        console.group("Merchant Type");
        console.groupEnd();
        $.ajax({
            type: "POST",
            url: "saveStage",
            data: dataObj,
            success: function(data) {
                // lockAllField();
                if(data.mpaData.stage == "08"){
                    $(".button-wrapper").remove();
                }

                if(data.mpaData.typeOfEntity == "Proprietory"){
                    $(".merchantSupport-all").html("");
                }else{
                    
                }

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
                    if(data.mpaData.stage == "05"){
                        var newValue = data.merchantType.indexOf("Regular Merchant");
                        console.log(newValue);
                        if(newValue == -1){
                            $(".technical-detail-msg").text("not applicable for you move to the next slide");
                            $(".technical-detail-msg").addClass("mb-20");
                            $("#stage-05 input[type='radio'").attr("disabled", true);
                            $("#stage-05 input[type='text'").attr("readonly", true);
                            $("#stage-05 input[type='email'").attr("readonly", true);
                            $("#btn-next-stage").attr("disabled", false);
                        }
                    } 
                    var _prevStage = _currentStage - 1;
                    btnPrev.attr("data-stage", "0" + _prevStage); // data.mpaData.stage - 1;
                    $(".loader-container").fadeOut();
                    $("html, body").animate({ scrollTop: 0 }, 600);
                } else {
                    alert("Limit exceeds. Please redirect the page.");
                }
    
            }
        });
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
        $(".stage-box.active input[type='text']").attr("readonly", true);
        $(".stage-box.active input[type='email']").attr("readonly", true);
        $(".stage-box.active input[type='radio']").attr("disabled", true);
        $(".stage-box.active input[type='file']").attr("disabled", true);
        // $(".stage-box.active input[type='file']").closest("labeL").find("img").attr("src", "../image/ok.png");
        $(".stage-box.active input[type='file']").closest("labeL").find("span").text("Your file has been uploaded");
        // $(".stage-box.active input[type='file']").attr("src", '../image/ok.png');
        $(".stage-box.active input[type='checkbox']").attr("disabled", true);
        $(".stage-box.active select").attr("disabled", true);
        $(".stage-box.active textarea").attr("readonly", true);
        
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

    $("#verifyGo").click(function(e){
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

    $(".director-address").attr("readonly", true);

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
        if(fileSize <= 2097152 && getFileExact == ".png" || getFileExact == ".pdf" || getFileExact == ".jpeg" || getFileExact == ".jpg"){
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

    // driving licensce here
    $("#uploadDL").change(function(event){
        $(".loader-container").fadeIn();
        $(this).closest("label").find(".file-msg").fadeOut();
        event.preventDefault();
        var form = $('#fileUploadDL')[0];
        var getDirectorInfo = $(this).closest(".varification-main-div").attr("data-director");
        var data = new FormData(form);
        data.append("directorNumber", getDirectorInfo);

        console.group("director info");
        console.groupEnd();
        
        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: "processImage",
            data: data,
            processData: false,
            contentType: false,
            cache: false,
            timeout: 180000,
            success: function (data) {
                if(data.mpaData.base64 != ""){
                    $("#dl").attr("src", data.mpaData.base64);
                    $("#dl").removeAttr("width height");
                    $("#dl").css("max-width","100%");
                    $(".verify-btn").css("display","block");
                    $(".loader-container").fadeOut();
                }
            },
            error: function (e) {
                $("#uploadDL").next(".error-msg-dl").text("Server Issue Try Again Later");
                $(".loader-container").fadeOut();
            }
        });
    })

    $("#verify-go-dl").click(function(e){
        $(".loader-container").fadeIn();
        var directorNumber = $(this).closest(".varification-main-div").attr("data-director");
        $.ajax({
            url: "processDrivingLicense",
            data: {directorNumber: directorNumber, "payId": _payId},
            success: function(data){

                if(!data.mpaData.ERROR){
                    
                    for(key in data.mpaData){
                        $("[data-id='"+key+"']").val(data.mpaData[key]);
                        // document.getElementById(key).value = data.mpaData[key];
                        // $("#"+key).attr("readonly", true);
                    }
                    checkAllField(e);
                    $(".open-address-popup").fadeOut();
                    $(".stage-box.active .error-msg-global").text("");
                    $(".loader-container").fadeOut();
                }else{
                    $("#uploadDL").next(".error-msg-dl").text(data.mpaData.ERROR)
                    $(".loader-container").fadeOut();
                }
                $(".verify-btn").css("display", "none");
            }
        })
    })

    // driving liscense ends here

    // gstin otp
    $("body").on("click", "#gstin-otp", function(e){
        var thisInput = $(this);
        var gstinUsername = $("#gstinUsername").val();
        $(this).text("loading...");
        $.ajax({
            type: "post",
            url: "invokeGstrOtp",
            data: {
                gstinUsername: gstinUsername, "payId": _payId
            },
            success: function(data){
                console.group("gst otp sent group");
                    console.groupEnd();
                if(data.mpaData.SUCCESS){
                    console.group("gst otp sent group");
                    console.groupEnd();
                    $(thisInput).attr("readonly", true);
                    $(".gstin-otp").removeClass("d-none");
                    $("#appKey").val(data.mpaData.appKey);
                    $("#gstin-otp").text("submit");
                    $("#gstin-otp").attr("id", "gstin-verify");
                }else{
                    $("#error-gst").text(data.mpaData.ERROR);
                    $("#gstin-otp").text("generate otp");
                }
            }
        })
        // $.get("../js/gstin.json", function(data){
        //     if(data.mpaData.SUCCESS){
        //         $(".gstin-otp").removeClass("d-none");
        //         $("#appKey").val(data.mpaData.appKey);
        //         $("#gstin-otp").text("submit");
        //         $("#gstin-otp").attr("id", "gstin-verify");
        //     }else{
        //         $("#error-gst").text(data.mpaData.ERROR);
        //         $("#gstin-otp").text("generate otp");
        //     }
        // })
    })

    // gst verification
    $("body").on("click", "#gstin-verify", function(e){
        $(".loader-container").fadeIn();
        var gstinUsername = $("#gstinUsername").val();
        var gstinOtp = $("#gstinOtp").val();
        var appKey = $("#appKey").val();
        $.ajax({
            type: "post",
            url: "invokeGstr3b",
            data: {
                gstinUsername: gstinUsername,
                gstinOtp: gstinOtp,
                "payId": _payId,
                appKey: appKey
            },
            success: function(data){
                if(!data.mpaData.ERROR){
                    console.group("merchant Data");
                    console.groupEnd();

                    defaultData(data.mpaData);
                    $(".mpa-gst-popup").fadeOut();
                }else{
                    $("#error-gst").text(data.mpaData.ERROR);
                }
                $(".loader-container").fadeOut();
            }
        })
        // $.get("../js/gstinOtp.json", function(data){
        //     if(!data.mpaData.ERROR){
        //         console.group("gst otp");
        //         defaultData(data.mpaData);
        //         $(".mpa-gst-popup").fadeOut();
        //     }else{
        //         $("#error-gst").text(data.mpaData.ERROR);
        //     }
        //     $(".loader-container").fadeOut();
        // })
    })

    $("#uploadCheque").change(function (event) {
        $(".loader-container").fadeIn();
        $(".file-msg").fadeOut();
        event.preventDefault();
        var form = $('#fileUploadForm')[0];
        var data = new FormData(form);
        data.append("payId", _payId);
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
                if(data.mpaData.base64 != ""){
                    $("#checqueImg").attr("src", data.mpaData.base64);
                    $("#checqueImg").removeAttr("width height");
                    $("#checqueImg").css("max-width","100%");
                    $(".verify-btn").css("display","block");
                    $(".loader-container").fadeOut();
                }else{
                    $(".verify-btn").css("display","none"); 
                }
            },
            error: function (e) {
                $(".loader-container").fadeOut();
                $("#result").text(e.responseText);
                $("#btnSubmit").prop("disabled", false);

            }
        });

    });

    $("body").on("change", ".percentage-count", function(e){
        var getVal = $(this).val();
        var newVal = Number(getVal).toFixed(2);
        $(this).val(newVal);
    })

    // total six count //

    $(".count-six-input").on("change", function(e){
        var percentageCC = Number($("#percentageCC").val());
        var percentageDC = Number($("#percentageDC").val());
        var percentageNB = Number($("#percentageNB").val());
        var percentageUP = Number($("#percentageUP").val());
        var percentageWL = Number($("#percentageWL").val());
        var percentageCD = Number($("#percentageCD").val());
        var percentageNeftOrImpsOrRtgs = Number($("#percentageNeftOrImpsOrRtgs").val());
        var percentageEM = Number($("#percentageEM").val());
        if(percentageCC != "" && percentageCD != "" && percentageDC != "" && percentageNB != "" && percentageUP != "" && percentageWL != "" && percentageNeftOrImpsOrRtgs != "" && percentageEM != ""){
            var totalCount = percentageCC + percentageCD + percentageNB + percentageUP + percentageWL + percentageDC + percentageNeftOrImpsOrRtgs + percentageEM;
            if(totalCount == 100){
                $(".msg-turn-over").text("");
            }else{
                $(".count-six-input").val("");
                $('html, body').animate({scrollTop: $('.msg-turn-over').offset().top }, 1000);
                $(".msg-turn-over").text("please filled value is equal to 100");
            }
        }
    })

    // total last two div count
    $(".count-two-input").on("change", function(e){
        var percentageDomestic = Number($("#percentageDomestic").val());
        var percentageInternationald = Number($("#percentageInternational").val());
        if(percentageDomestic != "" && percentageInternationald != ""){
            var totalCount = percentageInternationald + percentageDomestic;
            if(totalCount == 100){
                $(".msg-card-turn-over").text("");
            }else{
                $(".count-two-input").val("");
                $('html, body').animate({scrollTop: $('.msg-turn-over').offset().top }, 1000);
                $(".msg-card-turn-over").text("please filled value is equal to 100");
            }
        }
    })

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

    // refunds allowed
    // $("[data-key='refundsAllowed'] input").on("change", function(e){
    //     var getRefundValue = $(this).val();
    //     if(getRefundValue == "Full Refund" || getRefundValue == "Partial Refund"){
    //         $("input[value='Exchange Only']").attr("disabled", true);
    //         $("input[value='No Refund']").attr("disabled", true);
    //     }else{
    //         $("input[value='No Refund']").removeAttr("disabled");
    //         $("input[value='Exchange Only']").removeAttr("disabled");
    //     }
    //     if(getRefundValue == "Exchange Only"){
    //         $("input[value='Full Refund']").attr("disabled", true);
    //         $("input[value='No Refund']").attr("disabled", true);
    //         $("input[value='Partial Refund']").attr("disabled", true);
    //     }
    //     if(getRefundValue == "No Refund"){
    //         $("input[value='Full Refund']").attr("disabled", true);
    //         $("input[value='Partial Refund']").attr("disabled", true);
    //         $("input[value='Exchange Only']").attr("disabled", true);
    //     }
    // })


    // $(".percentage-count").change(function(e){
    //     $(this).val()+".00";

    // })

    // function totalPercentage(){
    //     var percentageCC = $("#percentageCC").val();
    //     var percentageDC = $("#percentageDC").val();
    //     var percentageNB = $("#percentageNB").val();
    //     var percentageUP = $("#percentageUP").val();
    //     var percentageWL = $("#percentageWL").val();
    //     var percentageCD = $("#percentageCD").val();
    //     var percentageNeftOrImpsOrRtgs = $("#percentageNeftOrImpsOrRtgs").val();
    //     var percentageEM = $("#percentageEM").val();
    //     if(percentageCC != ""){
    //         percentageCC = percentageCC + ".00";
    //         $("#percentageCC").val(percentageCC);
    //     }
    //     if(percentageDC != ""){
    //         percentageDC = percentageDC + ".00";
    //         $("#percentageDC").val(percentageDC);
    //     }
    // }
    // $(".percentage-count").on("change", totalPercentage);

}) 