var getWidth = 0;
var _array = [];
var _data = {};
var _new = document.querySelectorAll(".mpaFormBullets");
_new.forEach(function(index, array, element){
    if(array < 2){
        var _value = element[array].offsetLeft;
        _array.push(_value); 
    }
})
var getTotal = _array[1] - _array[0];

var _prop = {
    "label": {
        "director1Label" : "Proprietary Details",
        "cin" : "Registration Number",
        "viewRegistration" : "Registration Number"
    },
    "notRequired" : ['companyWebsite'],
    "text": ['companyEmailId', 'companyRegisteredAddress'],
    "showDiv" : ['proprietorship'],
    "hide" : ['directorTwo', 'contact-detail', 'merchant-support', 'limited', 'partnership']
}

var _privatePublic = {
    "label": {
        "director1Label" : "Director-1 Details",
        "director2Label" : "Director-2 Details",
        "cin" : "CIN",
        "viewRegistration" : "CIN"
    },
    "required" : ['companyWebsite'],
    "showText" : ['companyEmailId','companyRegisteredAddress'],
    "showDiv" : ['directorTwo', 'contact-detail', 'merchant-support', 'limited'],
    "hide" : ['partnership', 'proprietorship']
}

var _partnershipFirm = {
    "label": {
        "director1Label" : "Partner-1 Details",
        "director2Label" : "Partner-2 Details",
        "cin" : "RegistrationNumber",
        "viewRegistration" : "Registration Number"
    },
    "required" : ['companyWebsite'],
    "showText" : ['companyEmailId', 'companyRegisteredAddress'],
    "showDiv" : ['directorTwo', 'contact-detail', 'merchant-support', 'partnership'],
    "hide" : ['limited', 'proprietorship']
}

function jsonLoop(_json){
    if(_json.label != undefined){
        for(key in _json.label){
            console.log(key);
            document.querySelector("[data-label='"+key+"']").innerText = _json.label[key];
        }
    }
    if(_json.text != undefined){
        for(var i = 0; i < _json.text.length; i++){
            document.querySelector("#"+_json.text[i]).closest(".col-md-3").classList.add("d-none");
        }
    }
    if(_json.hide != undefined){
        for(var i = 0; i < _json.hide.length; i++){
            document.querySelector("[data-hide="+_json.hide[i]+"]").classList.add("d-none");
        }
    }
    if(_json.showText != undefined){
        for(var i = 0; i < _json.showText.length; i++){
            document.querySelector("#"+_json.showText[i]).closest(".col-md-3").classList.remove("d-none");
        }
    }
    if(_json.showDiv != undefined){
        for(var i = 0; i < _json.showDiv.length; i++){
            document.querySelector("[data-hide="+_json.showDiv[i]+"]").classList.remove("d-none");
        }
    }

    if(_json.notRequired != undefined){
        for(var i = 0; i < _json.notRequired.length; i++){
            document.querySelector("#"+_json.notRequired[i]).closest(".col-md-3").querySelector(".imp").classList.add("d-none");
        }
    }

    if(_json.required != undefined){
        for(var i = 0; i < _json.required.length; i++){
            document.querySelector("#"+_json.required[i]).closest(".col-md-3").querySelector(".imp").classList.remove("d-none");
        }
    }

}

function removeError(_this){
    var _nullCheck = _this.closest(".mpa-red");
    if(_nullCheck != null){
        _nullCheck.classList.remove("mpa-red");
    }
}

var _getOnlineFlagStatus = document.querySelector("#onlineMpaFlag").value;
var _isOnline = null;
if(_getOnlineFlagStatus == "true"){
    _online = true;
}else{
    _online = false;
}


function removeDisabled(_value){
        var _disabled = document.querySelectorAll("select[disabled]");
        var _readonly = document.querySelectorAll("[readonly]");
        for(var i = 0; i < _disabled.length; i++){
            if(_value != ''){
                _disabled[i].disabled = false;
                $("#"+_disabled[i].id).selectpicker('refresh');
            }else{
                _disabled[i].disabled = true;
                $("#"+_disabled[i].id).selectpicker('refresh');
            }
            
        }
        for(var i = 0; i < _readonly.length; i++){
            if(_value != ''){
                _readonly[i].removeAttribute("readonly");
            }else{
                var _createAtt = document.createAttribute("readonly");
                _readonly[i].setAttributeNode(_createAtt);
            }
        }
}   

document.querySelector("#industryCategory").onchange = function(e){
    var _value = e.target.value;
    if(_value != ""){
        disabledInputs("[name='typeOfEntity']", true);
    }else{
        disabledInputs("[name='typeOfEntity']", false);
    }
    checkAllField(e);
}



var _director1Input = '<div class="lpay_input_group"><label for="">Full Name <span class="imp">*</span></label><input type="text" id="director1FullName" class="lpay_input mpa-input input-caps" onchange="checkAllField(this)" onkeypress="onlyLetters(event)"></div>';

var _director1Select = '<div class="lpay_select_group"><label for="">Full Name <span class="imp">*</span></label><select data-company="director1FullName" name="director1FullName" title="Select Director 1" class="selectpicker" id="director1FullName"></select></div>';

var _director2Input = '<div class="lpay_input_group"><label for="">Full Name <span class="imp">*</span></label><input type="text" id="director2FullName" class="lpay_input mpa-input input-caps" onchange="checkAllField(this)" onkeypress="onlyLetters(event)"></div>';

var _director2Select = '<div class="lpay_select_group"><label for="">Full Name <span class="imp">*</span></label><select data-company="director2FullName" name="director2FullName" title="Select Director 2" class="selectpicker" id="director2FullName"></select></div>';


function checkDuplicacy(_that){
    var _val = _that.value;
    var _id = _that.id;
    var _parent = document.querySelector(".stage-box.active");
    var _input = _parent.querySelectorAll("input");
    _input.forEach(function(index, array, element){
        if(index.id != _id){
            if(index.value == _val && index.id.includes("director") == true && index.value != ""){
                document.querySelector("#"+_id).closest(".lpay_input_group").classList.add("mpa-red");
                document.querySelector("#"+_id).closest(".lpay_input_group").querySelector(".mpa-msg").innerText = "Value should not be matched";
                document.querySelector("#"+_id).focus();
                $("#btn-next-stage").attr("disabled", true);
            }
        }
    })
}


function typeOfEntity(_this){
    document.querySelector("[data-id='otherEntity']").classList.add("d-none");
    // document.querySelector("[data-company='cin']").id = "registrationNumber";
    if(_online == false &&_this == "Private Limited" || _this == "Public Limited"){
        jsonLoop(_privatePublic);
        document.querySelector("[data-director='director1Detail']").innerHTML = _director1Input;
        document.querySelector("[data-director='director2Detail']").innerHTML = _director2Input;
        // document.querySelector("[data-company='cin']").id = "cin";
    }else if(_online == true && _this == "Private Limited" || _this == "Public Limited"){
        jsonLoop(_privatePublic);
        document.querySelector("[data-director='director1Detail']").innerHTML = _director1Select;
        document.querySelector("[data-director='director2Detail']").innerHTML = _director2Select;
        // document.querySelector("[data-company='cin']").id = "cin";
    }else if(_this == "Proprietory"){
        jsonLoop(_prop);
        document.querySelector("[data-director='director1Detail']").innerHTML = _director1Input;
    }else if(_this == "Partnership Firm"){
        jsonLoop(_partnershipFirm);
        document.querySelector("[data-director='director1Detail']").innerHTML = _director1Input;
        document.querySelector("[data-director='director2Detail']").innerHTML = _director2Input;
    }else if(_this == undefined){
        jsonLoop(_privatePublic);
        document.querySelector("[data-director='director1Detail']").innerHTML = _director1Input;
        document.querySelector("[data-director='director2Detail']").innerHTML = _director2Input;
    }else{
        jsonLoop(_partnershipFirm);
        document.querySelector("[data-id='otherEntity']").classList.remove("d-none");
        document.querySelector("[data-director='director1Detail']").innerHTML = _director1Input;
        document.querySelector("[data-director='director2Detail']").innerHTML = _director2Input;
    }
}

function disabledInputs(_selector, _disabled){
    var _getInputs = document.querySelectorAll(_selector);
    _getInputs.forEach(function(index, array, element){
        if(index.localName == "input"){
            if(_disabled == true){
                index.removeAttribute("readonly");   
            }else{
                var _createAtt = document.createAttribute("readonly");
                index.setAttributeNode(_createAtt);
            }
        }
        if(index.localName == "select"){
            if(_disabled == true){
                index.disabled = false;
                $("#"+index.id).selectpicker('refresh');
            }else{
                index.disabled = true;
                $("#"+index.id).selectpicker('refresh');
            }
        }
    })
}


document.querySelector("#typeOfEntity").onchange = function(e){
    var _this = e.target.value;
    typeOfEntity(_this);
    disabledInputs("[data-link]", false);
    if(_online == true && (_this == "Private Limited" || _this == "Public Limited")){
        disabledInputs("[data-company='legalName']", true);  
    }else if(_online == true && _this != "Private Limited" && _this != "Public Limited"){
        disabledInputs("[data-link]", true);
    }else if(_online == false){
        disabledInputs("[data-link]", true);
    }
}

// email validation 
function regEx(_this){
    var _reg = _this.getAttribute("data-reg");
    var _value = _this.value;
    if(_value != ""){
        var _newReg = new RegExp(_reg);
        if(_newReg.test(_value) == true){
            _this.closest(".lpay_input_group").classList.remove("mpa-red");
        }else{
            _this.closest(".lpay_input_group").classList.add("mpa-red");
            _this.closest(".lpay_input_group").querySelector(".mpa-msg").innerText = "Invalid Input";
        }
        checkAllField(_this)
    }
}


// previos data showing function
function fetchPreviousData(data){
    var _parent = document.querySelector(".stage-box.active");
    for(key in data){
        var _checkId = _parent.querySelector("#"+key);
        if(_checkId != null){
            if(_checkId.localName == "input"){
                _checkId.value = data[key];
            }else{
                _checkId.disabled = false;
                _checkId.value = data[key];
                $("#"+key).selectpicker('refresh');
                if(key == "tradingState"){
                    countryFunction(data['tradingState'], data['tradingCountry']);
                }
            }
        }
        if(data.stage == "00"){
            var _entityList = ['Private Limited', 'Public Limited', 'Partnership Firm', 'Proprietory']
            if(_entityList.indexOf(data.typeOfEntity) == -1 ){
                $("#typeOfEntity").selectpicker('val', 'Other');
                $("#typeOfEntity").selectpicker('refresh');
                document.querySelector("#otherEntity").value = data.typeOfEntity;
            }
        }
        
    }
    if(data.stage == "01"){
        merchantChecked();
    }

    if(data.stage == "00"){
        disabledInputs("[data-link]", true);
        disabledInputs("#typeOfEntity", true);
    }

}

function merchantChecked(){
    var _landlineDiv = document.querySelector("#merchantSupportLandLine");
    var _mobileNumber = document.querySelector("#merchantSupportMobileNumber");
    var _email = document.querySelector("#merchantSupportEmailId");
    var _contactEmail = document.querySelector("#contactEmail");
    var _contactMobile = document.querySelector("#contactMobile");
    var _contactLandline = document.querySelector("#contactLandline");
    if(_landlineDiv.value != "" ){
        _mobileNumber.closest(".mobileNumber-div").classList.add("d-none");
        _landlineDiv.closest(".landline-div").classList.remove("d-none");
        document.querySelector("#landlineNumber").checked = true;
        document.querySelector("#landlineNumber").closest("label").classList.add("checkbox-checked");
        if(_landlineDiv.value == _contactLandline.value){
            document.querySelector("#merchantSupport").checked = true;
            document.querySelector("#merchantSupport").closest("label").classList.add("checkbox-checked");
        }
    }
    
    if(_mobileNumber.value == _contactMobile.value && _email.value == _contactEmail.value){
        console.log("i am in");
        document.querySelector("#merchantSupport").checked = true;
        document.querySelector("#merchantSupport").closest("label").classList.add("checkbox-checked");
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

function countryFunction(_state, _country){
    $("#tradingCountry").trigger("change");
    $("#tradingCountry").selectpicker('val', _country);
    populateStates("tradingCountry", "tradingState", false);
    $("#tradingCountry").selectpicker('refresh');
    $("#tradingState").selectpicker('refresh');
    $("#tradingState").selectpicker('val', _state);
}

function defaultData(data){

    console.log(data);

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

    }
    var _count = 0;
    for(key in data){
        var _option = "";
        var _checkNull = document.getElementById(key);
        var _isEmpty = data[key];
        if(_checkNull != null && _isEmpty != "" && key != "stage"){
            var _checkElement = document.getElementById(key).localName;
            if(_checkElement == "input"){
                document.getElementById(key).value = data[key];
                document.getElementById(key).setAttributeNode(document.createAttribute("readonly"));
            }else if (_checkElement == 'select'){
                document.getElementById(key).disabled = false;
                document.getElementById(key).closest(".lpay_select_group").setAttributeNode(document.createAttribute("readonly"));
                if(key.includes("FullName")){
                    _count++;
                    _option = "<option value='"+key+"' data-pan='"+data["director"+_count+"Pan"]+"' data-address='"+data["director"+_count+"Address"]+"' data-email='"+data["director"+_count+"Email"]+"' data-address >"+data[key]+"</option>";
                    document.getElementById("director"+_count+"Address").value = '';
                    document.querySelector("#director1FullName").innerHTML += _option;
                    document.querySelector("#director2FullName").innerHTML += _option;
                    $("#director1FullName").selectpicker('refresh');
                    $("#director2FullName").selectpicker('refresh');
                }
                $("#"+key).selectpicker('refresh');
                if(key == "tradingState"){
                    countryFunction(data['tradingState'], data['tradingCountry']);
                }
            }
        }else{
            var _localeName = document.getElementById(key).localName;
            // console.log(_localeName);
            // document.getElementById(key).removeAttribute("readonly");
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


// keyup function upper case
$(".input-caps").on("keyup", function(e){
    this.value = this.value.toUpperCase();
}); 




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

// only digit with out

function onlyDigitNotSpace(event){
    var x = event.keyCode;
    if (x > 47 && x < 58) {
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

// check all mandatory 
function checkAllField(e) {
    var _parent = document.querySelector(".stage-box.active");
    var _check = true;
    var saveBtn = document.getElementById('btn-next-stage');
    var _requiredBoxes = _parent.querySelectorAll(".imp");
    
    for(var i = 0; i < _requiredBoxes.length; i++){
        if(_requiredBoxes[i].closest(".d-none") == null){
            var _getAll =  _requiredBoxes[i].closest("div").querySelector(".mpa-input");
            // console.log(_getAll);
            if(_getAll != null){
                 if(_getAll.value == ""){
                     _check = false;
                 }
            }
        }
    }
    if(_check == true){
        var _invalidError = document.querySelector(".mpa-red");
        if(_invalidError == null){
            saveBtn.disabled = false;
        }else{
            console.log("hello");
            saveBtn.disabled = true 
        }
    }else{
        saveBtn.disabled = true;
    }
} 


function fetchStage (){
    $(".loader-wrapper").fadeIn();
    var stage = "";
    var _newPayId = $("#merchantPayId").val();
    if(_newPayId == ""){
        _newPayId = null
    }
    $.ajax({
        type: "POST",
        url: "fetchStage",
        data: {"stage": stage, "payId" : _newPayId},
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
            setTimeout(function(e){
                typeOfEntity(data.mpaData.typeOfEntity);
            }, 500)
            $(".loader-wrapper").fadeOut();

        }
    })
}

fetchStage();

function categoryListing(data){
    var createOption = "";
    createOption = "<option value=''>Select Category</option>";
    for(key in data){
        createOption += "<option value='"+data[key]+"'>"+data[key]+"</option>";
    }
    $("#industryCategory").append(createOption);
    $("#industryCategory").selectpicker('refresh');

}

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


$(document).ready(function() {

    

    // console.log(window);

    document.querySelector(".mpa-main-container").style.minHeight  = window.innerHeight+"px";

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
            $("#merchantSupportLandLine").val($("#contactLandline").val());
        }else{
            $("#merchantSupportLandLine").val("");
            $(".landline-div").addClass("d-none");
            $(".landline-div").find("input").removeClass("mpa-input");
            $(".mobileNumber-div").removeClass("d-none");
            $(".mobileNumber-div").find("input").addClass("mpa-input");
            // $("#landlineNumber").val("");
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
            }
        }else{
            $("#merchantSupportEmailId").val("");
            $("#merchantSupportMobileNumber").val("");
            $("#merchantSupportLandLine").val("");
        }
        checkAllField(this);
    });

    $(".checkbox-label input").on("change", function(e){
        if($(this).is(":checked")){
          $(this).closest("label").addClass("checkbox-checked");
        }else{
          $(this).closest("label").removeClass("checkbox-checked");
        }
      });
    
    $(".term-condition").scroll(function(e){
        if($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
           $(".term-condition-text").removeClass("d-none");
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
    });


    // 
    $(".term-condition-input input").on("change", function(e){
        var _privacyPolicy = $("#privacyPolicyCheck").is(":checked");
        var _tnc = $("#termConditionCheck").is(":checked");
        if(_privacyPolicy == true && _tnc == true){
            $(".term-btn button").attr("disabled", false);
        }else{
            $(".term-btn button").attr("disabled", true);
        }
    })

    // $(".termPolicy").on("click", function(e){
    //     var _this = $(this).attr("data-info");
    //     $("#docFile").val(_this);
    //     $("#saveTermPolicy").submit();
    // })

    // term and condition box

    $(".term-btn button").click(function(e){
        $(".term-condition-popup").fadeOut();
    });


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

    document.querySelector("#tradingCountry").onchange = function(e){
        populateStates("tradingCountry", "tradingState", false);
        $("#tradingState").selectpicker('refresh');
    }


    var dataCountry = $("#tradingCountry").val();
    if (dataCountry !== "") {
        $("#tradingCountry").val(dataCountry);
    }

    // STATE DEFAULT SELECTED
    var dataState = $("#tradingState").val();
    if (dataState !== "") {
        $("#tradingState").val(dataState);
    }

    $("#tradingCountry").selectpicker();

    

    // when slected type of entity legal name
    $(".stage-box.active input[type='radio']").click(function(){
        $("#companyName").attr("readonly", false);
    });

    // company on blur add condition

    $("#companyName").on('blur', function(e){
        var _getCompanyName = $(this).val();
        if(_getCompanyName.length > 1){
            $("#typeOfEntity").find("input[type='radio']").attr("disabled", true);
            $("#industryCategory").attr("disabled", true);
        }
    })

    // get company info on call api
    // function getCompanyName(){
    //     var thisInput = $(this);
    //     document.querySelector(".loader-wrapper").style.display = "block";
    //     var getCinValue = document.getElementById("cin").value;
    //     var _industryCat = document.getElementById("industryCategory").value;
    //     var _typeOfEntity = $("#typeOfEntity").find("input:checked").val();
    //     var legalNameLength = $("#companyName").val();
    //         $.ajax({
    //             type: "post",
    //             url: "companyName",
    //             timeout: 18000,
    //             data: {
    //                 "companyName": legalNameLength,"industryCategory": _industryCat, "typeOfEntity": _typeOfEntity
    //             },
    //             success: function(data) {
    //                 if (!data.mpaData.ERROR) {
    //                     getCinValue = data.mpaData.cin;
    //                     if (getCinValue != "") {
    //                         getCin(data.mpaData);
    //                         getCinBasedData(data.mpaData);
    //                     }else{
    //                         $("#cin").removeAttr("readonly");
    //                     }
    //                     $("#companyName").next(".error-msg").text();
    //                 } else {
    //                     $("#companyName").next(".error-msg").text(data.mpaData.ERROR);
    //                     $("#companyName").next(".error-msg").fadeIn();
    //                 }
    //                 document.querySelector(".loader-wrapper").style.display = "none";
    //             },
    //             error: function(data){
    //                 $(thisInput).next(".error-msg").text("Server Issue Please Try Again");
    //                 $(".loader-wrapper").fadeOut();
    //             }

    //         })
    //     }

    // company name for not inserted limited

    // company name validator
    function NameValidater() {
        var _typeOfEntity = $("#typeOfEntity").val();
        var getName = $("#companyName").val();
        var findWord = getName.match(/limited/i);
        var nameValidate = getName.lastIndexOf(findWord);
        if (nameValidate != -1 && getName.length > 9) {
            if(_online == true && _typeOfEntity == "Private Limited" || _typeOfEntity == "Public Limited"){
                getCompanyName();
            }
        }
    }
    

    // company api call
    $("body").on("input",".companyNameApi", NameValidater);

    // create function for online 
    function createJson(_data){
        var _obj = {};
        var _createVariable = document.querySelectorAll("["+_data+"]");
        console.log(_createVariable);
        _createVariable.forEach(function(index, array, element){
            if(index.value == ""){
                _check = false;
                return false;
            }
            _obj[index.getAttribute(_data)] = index.value;
        })
        return _obj;
    }

    function getCinData(){
        console.log(_isOnline);
        if(_online == true){
            var _cin = document.querySelector("#cin").value;
            if(_cin.length > 10){
                $(".loader-wrapper").fadeIn();
                $.ajax({
                    type: "post",
                    url: "searchCin",
                    data: {
                        "cin": _cin,
                    },
                    success: function(data) {
                        console.log(data);
                        if(!data.mpaData.ERROR){
                            removeDisabled("remove");
                            defaultData(data.mpaData);
                            getGst()
                        }else{
                            $("#cin").next(".error-msg").text(data.mpaData.ERROR);
                            $("#cin").next(".error-msg").css("display","block");
                        }
                        $(".loader-wrapper").fadeOut();
                    },
                    error: function(data) {
                        
                    }
                });
            }else{
            }
           
        }
    }

    document.querySelector("#cin").addEventListener('blur', function(e){
        getCinData();
    });

    function getCompanyName(){
        var _values = createJson("data-company");
        _values['companyName'] = document.querySelector("#companyName").value;
        $(".loader-wrapper").fadeIn();
        $.ajax({
            type: 'POST',
            url: 'companyName',
            data: _values,
            success: function(data){
                if(!data.mpaData.ERROR){
                    if(data.mpaData.cin == ""){
                        document.querySelector("#cin").removeAttribute("readonly");
                        document.querySelector("#cin").focus();
                    }else{
                        document.querySelector("#cin").value = data.mpaData.cin;
                        getCinData();
                        console.log("we are inside");
                    }
                }
                $(".loader-wrapper").fadeOut();
            }
        })
    }

    var _gst = document.querySelectorAll("[data-gst]");
    _gst.forEach(function(index, array, element){
        _gst[array].addEventListener('blur', function(e){
            getGst()
        })
    })

    function getGst(){
        if(_online == true){
            var _check = true;
            var _dataObj = {};
            var _gstVariable = document.querySelectorAll("[data-gst]");
            _gstVariable.forEach(function(index, array, element){
                if(index.value == ""){
                    _check = false;
                }else{
                    _dataObj[index.getAttribute("data-gst")] = index.value;
                }
            })
            if(_check == true){
                $.get("../js/companyGst.json", function(data){
                    defaultData(data.mpaData);
                });
            }
        }
    }

    $( "#dateOfIncorporation" ).datepicker({
        dateFormat : 'mm/dd/yy',
        changeMonth : true,
        changeYear : true,
        yearRange: '-100y:c+nn',
        maxDate: '-1d',
        yearRange: "-100:+0"
    });

    
    // $("#fancybox").fancybox();

    //get category list{}
    

    // $("#industryCategory").on("change", function(e){
    //     var _val = $(this).val();
    //     if(_val == "-1"){
    //         $("#typeOfEntity").find("input").attr("disabled", true);
    //     }else{
    //         $("#typeOfEntity").find("input").attr("disabled", false);
    //     }
    // })

    

    $("input").attr("autocomplete", "nope");
    $("textarea").attr("autocomplete", "nope");



    var _checkpan = false;

  


    



    var _payId = document.querySelector("#merchantPayId").value;


    
    
    // SUBMIT BUTTON CLICKED
    $("#btn-next-stage").on("click", function(e) {
        e.preventDefault();
        var _getStage = parseInt($(this).prev("button").attr("data-stage"));
        var _stage2 = "0"+(_getStage + 1);
        if(_stage2 == "04"){
            $("#fancybox").fancybox({
                'overlayShow': true
            }).trigger('click');
        }else{
            $(".loader-wrapper").fadeIn();
            var _parent = document.querySelector(".stage-box.active");
            var _eleID = _parent.getAttribute("id");
            var _stage = _eleID.slice(6, _eleID.length);
            console.log();
            var dataObj = {};

            var _selectBox = _parent.querySelectorAll("select");
            var _inputBox = _parent.querySelectorAll("input");

            for(var i = 0; i < _selectBox.length; i++){
                if(_selectBox[i].closest(".d-none") == null){
                    dataObj[_selectBox[i].id] = _selectBox[i].value;
                }
            }
            for(var i = 0; i < _inputBox.length; i++){
                if(_inputBox[i].closest(".d-none") == null){
                    dataObj[_inputBox[i].id] = _inputBox[i].value;
                }
            }

            dataObj['stage'] = _stage;
            if(_payId != ""){
                dataObj['payId'] = _payId;
            }
            if(_stage == "00"){
                var _checkNull = document.querySelector("#otherEntity").closest(".d-none");
                
                if(_checkNull == null){
                    dataObj.typeOfEntity = dataObj['otherEntity'];
                    delete dataObj.otherEntity
                }

            }

            $.ajax({
                type: "POST",
                url: "saveStage",
                data: dataObj,
                success: function(data) {
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
                        if(data.mpaData.stage == "04"){
                            var _selector = "";
                            var _mainParent = document.querySelector(".stage-box.active");
                            var _allDiv = _mainParent.querySelectorAll("[data-hide]");
                            _allDiv.forEach(function(index, array, element){
                                var _classList = index.classList.toString();
                                if(_classList.indexOf("d-none") == -1){
                                    _selector = index.classList.toString();
                                }
                            })
                            var _allInput = document.querySelectorAll("."+_selector +" .imp");
                            if(_allInput.length == 0){
                                $("#btn-next-stage").attr("disabled", false);
                            }else{
                                $("#btn-next-stage").attr("disabled", true);
                            }
                        }

                        console.log(_data);

                        if(data.mpaData.stage != "05" && data.mpaData.stage != "04"){
                            checkAllField(this);
                        }

                        console.log(document.querySelectorAll("#director1FullName"));
    
                        $(".loader-wrapper").fadeOut();
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
        $(".loader-wrapper").fadeIn();
        var that = $(this);
        var _parent = $(".stage-box");
        var _id = $(".stage-box.active").attr("id");
        var _prevStage = that.attr("data-stage"); //01

        var _newPayId = $("#merchantPayId").val();
        if(_newPayId == ""){
            _newPayId = null;
        }

        $.ajax({
            type: "POST",
            url: "fetchStage",
            data: {
                stage: _prevStage,
                payId: _newPayId
            },
            success: function(data){

                console.log(data);

                console.log(data.mpaData.typeOfEntity);
                
                _parent.removeClass("active");
                $("#btn-next-stage").attr("disabled", false);
                $("#mpaSectionIndicator").find("[data-active='" + _id + "']").removeClass("active");
        getWidth = getWidth - getTotal;
        $("#progressStatus").animate({width: getWidth+'px'});

        $("#stage-" + _prevStage).addClass("active");
    
        typeOfEntity(data.mpaData.typeOfEntity);

        fetchPreviousData(data.mpaData);

        // all input and select box readonly
        // $(".stage-box.active input[type='file']").closest("labeL").find("img").attr("src", "../image/ok.png");
        $(".stage-box.active input[type='file']").closest("labeL").find("span").text("Your file has been uploaded");
        // $(".stage-box.active input[type='file']").attr("src", '../image/ok.png');
        var activeStage = $(".stage-box.active").attr("id");
        activeStage = activeStage.slice(6, activeStage.length);
    
        _prevStage = _prevStage - 1;
    
        that.attr("data-stage", "0" + _prevStage.toString());
    
        if (activeStage == "00") {
            that.addClass("inactive");
        }
        $(".loader-wrapper").fadeOut();
    }
        })

    });

 

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

    // function for make generic uploader
    $("body").on("change", ".generic-uploader", function(e){
        var getFormClass = $(this).closest("form").attr("class");
        var _parent = $(this).closest("label");
        var form = $("."+getFormClass)[0];
        var file = $(this).val();
        var getFilePeriod = file.lastIndexOf(".");
        var getFileExact = file.slice(getFilePeriod);
        var fileSize = $(this)[0].files[0].size;
        var data = new FormData(form);
        var getName = file.replace("C:\\fakepath\\", "");
        var fileName = $(this).attr("data-type");
        data.append("fileName", fileName);
        if(fileSize <= 2097152 && getFileExact == ".png" || getFileExact == ".PNG" || getFileExact == ".pdf" || getFileExact == ".PDF" || getFileExact == ".jpeg" || getFileExact == ".JPEG" || getFileExact == ".jpg" || getFileExact == ".JPG"){
            $(".loader-wrapper").fadeIn();
            $.ajax({
                type: "post",
                enctype: 'multipart/form-data',
                url: "uploadFile",
                data: data,
                processData: false,
                contentType: false,
                success: function(data){
                    // console.log(data);
                    $("[name='file']").val("");
                    _parent.find("span").text(getName);
                    $(_parent).removeClass("error-upload");
                    var _allInput = document.querySelectorAll(".upload-generic b");
                    if(_allInput.length == 0){
                        $("#btn-next-stage").attr("disabled", false);
                    }else{
                        $("#btn-next-stage").attr("disabled", true);
                    }
                    $(".loader-wrapper").fadeOut();
                    checkAllField(e);

                }
            })
        }else{
            _parent.find("span").text("Either file type not supported or file should not greater then 2mb");
            $("#btn-next-stage").attr("disabled", true);
            $(_parent).addClass("error-upload");
        }

    })

    

  

    $("#uploadCheque").change(function (event) {
        event.preventDefault();
        if(event.target.value != ""){
            var form = $('#fileUploadForm')[0];
            var data = new FormData(form);
            var _fileTypeIndex = event.target.files[0].type.lastIndexOf("/");
            var _fileType = event.target.files[0].type.slice(_fileTypeIndex+1);
            if(_fileType == "png" || _fileType == "jpg" || _fileType == "pdf" || _fileType == "PNG" || _fileType == "JPG" || _fileType == "PDF" || _fileType == "JPEG" || _fileType == "jpeg"){
            $(".loader-wrapper").fadeIn();
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
                        $(".loader-wrapper").fadeOut();
                        checkAllField(this);
                    },
                    error: function (e) {
                        $(".loader-wrapper").fadeOut();
                        $("#result").text(e.responseText);
                        $("#btnSubmit").prop("disabled", false);
        
                    }
                });
            }else{
                
                $(".file-msg").text("File type not supported");
                $(".upload-file").closest("label").addClass("redLine");
                $(".upload-file").addClass("mpa-input red-line");
                $(".loader-wrapper").fadeOut();
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
        // console.log(getVal);
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
                        $(_msg).closest(".col-md-12").removeClass("error-div");
                    }else{
                        $(_selector).val("");
                        $('html, body').animate({scrollTop: $(_msg).offset().top }, 1000);
                        $(_msg).closest(".col-md-12").addClass("error-div");
                        // $(_msg).text("please filled value is equal to 100");
                        $("#btn-next-stage").attr("disabled", true);
                    }
                }
            })
        })
    }


    newFunc(".count-six-input", ".msg-turn-over");
    newFunc(".count-two-input", ".msg-card-turn-over");

  
    
    
    $("#cancel-btn").on("click", function(e){
        // $("#btn-prev-stage").attr("data-stage", "04");
       var _getClass = $(".fancybox-close-small");
        // $.fancybox.close(true);
        $("#btn-prev-stage").trigger("click");
        setTimeout(function(e){
            $("button.fancybox-close-small").trigger("click");
        }, 500);
    })

    $("#confirm-btn").on("click", function(e){
        $(".loader-wrapper").fadeIn();
    	 setTimeout(function(e){
             $("button.fancybox-close-small").trigger("click");
         }, 500);
        $("#btn-prev-stage").attr("data-stage", "04");
        $("#btn-next-stage").trigger("click");
        // aml();
        
    })

    // function aml(){
    //     $.ajax({
    //         type: "POST",
    //         url: "amlRequestAction",
    //         data: {
    //             "payId" : _payId
    //         },
    //         success: function(data){
    //             if(data.result == "Success"){
    //                 $("#btn-next-stage").trigger("click");
    //                 $(".loader-wrapper").fadeOut();
    //             }else{
    //                 $("#btn-next-stage").trigger("click");
    //                 $(".loader-wrapper").fadeOut();
    //             }
    //         }
    //     })
    // }

    $(".termPolicy").on("click", function(e){
        var _this = $(this).attr("data-info");
        $("#docFile").val(_this);
        $("#downloadTnCPolicy").submit();
    })

}) 

// function to check account details
function bankDetailsVerify(){

    var _text = "<div class='loader-text'>We are vefifying you bank details...</div>";
    var _obj = {};
    var _check = true;
    var _allInput = document.querySelectorAll(".bank-details");
    _allInput.forEach(function(index, array, element){

        if(index.value == ""){
            _check = false;
        }
        _obj[index.getAttribute("data-var")] = index.value;

    })
    
    if(_check == true){

        document.querySelector(".loader-wrapper").innerHTML += _text;
        $(".loader-wrapper").fadeIn();
        $.ajax({
            type: "POST",
            url: "verifyAccountDetails",
            data: _obj,
            success: function(data){

                $(".responseMsg").text(data.responseMsg);
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    for(key in _obj){
                        var _checkIdExist = document.querySelector("[data-var='"+key+"']");
                        if(_checkIdExist != null){
                            _checkIdExist.closest("div").classList.add("bank-verified");
                            _checkIdExist.value = _obj[key];
                            var _createAttr = document.createAttribute("readonly");
                            _checkIdExist.setAttributeNode(_createAttr);
                        }  
                    }
                    $("[data-var='beneName']").val(data.beneName);
                    $(".lpay_popup").fadeIn();
                    $(".loader-wrapper").fadeOut();
                    $(".loader-text").remove();
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $(".loader-wrapper").fadeOut();
                    $(".loader-text").remove();
                    $("[data-var='beneIfsc']").val("");
                }

            }
        })
    }

    // if(_check == true){
    //     $.get("../js/imps.JSON", function(data){
    //         $(".responseMsg").text(data.mpaData.responseMsg);
    //         if(data.mpaData.response == "SUCCESS"){
    //             $(".lpay_popup-innerbox").attr("data-status", "success")
    //         }else{
    //             $(".lpay_popup-innerbox").attr("data-status", "error")
    //         }
    //         $(".lpay_popup").fadeIn();
    //         for(key in data.mpaData){
    //             var _checkIdExist = document.querySelector("#"+key);
    //             if(_checkIdExist != null){
    //                 _checkIdExist.value = data.mpaData[key];
    //                 var _createAttr = document.createAttribute("readonly");
    //                 _checkIdExist.setAttributeNode(_createAttr);
    //             }  
    //         }
    //     })
    // }


}


$(".confirmButton").on("click", function(e){
    $(".lpay_popup").fadeOut();
})