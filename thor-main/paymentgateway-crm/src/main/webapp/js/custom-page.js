_selector = document.querySelector.bind(document);

// only digit
function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// only letters
function onlyLetters(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
        
    } else {
        event.preventDefault();
    }
}

// mail validation 
var emailValidation = function(that){
    var _regEx = /^([A-Za-z0-9_\-\.\'])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,6})$/;
    var emailValue = that.value;
    if(emailValue.match(_regEx)){
        that.classList.remove("red-line");
    }else{
        that.classList.add("red-line");
    }
}

// create anchor dynamically for footer
function createAnchor(){
    document.querySelector("#footerLink").value = "";
    var _getLabel = document.querySelector("#anchorLabel").value;
    var _getFile = document.querySelector("#uploadFileInput").value;
    var _getFileName = _getFile.slice(_getFile.lastIndexOf("\\")+1);
    if(_getLabel == ""){
        document.querySelector("#anchorLabel").classList.add("red-line");
        return false;
    }
    if(_getFile == ""){
        document.querySelector("#uploadFileInput").closest("div").classList.add("red-line-upload");
        return false;
    }
    var _createAnchor = "<a href='"+_getFileName+"' class='lpay_button lpay_button-md lpay_button-tertiary'>"+_getLabel+"<span class='linkDlt ml-15' onClick='linkDelete(this)'><img src='../image/cross.png'></span></a>";
    document.querySelector(".createdLinks").innerHTML += _createAnchor;
    document.querySelector("#footerLink").value = document.querySelector(".createdLinks").innerHTML.trim();
    $("#cancelBtn").trigger("click");
    document.querySelector("#anchorLabel").value = "";
    document.querySelector("#uploadFileInput").closest("div").setAttribute("data-response", "default");
}
function linkDelete(event){
    event.closest("a").remove();
}
document.querySelector("#saveBtn").addEventListener("click", createAnchor);

var removeErrorClass = function(that){
    that.classList.remove("red-line");
}

// select box remvoe redline
_selector(".inputPriority").onchange = function(e){
    this.classList.remove("red-line");
}

// create input box dynamically

function createInput(){

    var _getLabel = document.querySelector(".inputLabel").value;
    var _getInputPriority = document.querySelector(".inputPriority").value;
    var _getCharacterLength = document.querySelector(".inputCharacter").value;
    var _getAllowInput = document.querySelector(".allowInput").value;

    if(_getLabel == ""){
        document.querySelector(".inputLabel").classList.add("red-line");
        document.querySelector(".inputLabel").focus();
        return false;
    }

    if(_getInputPriority == ""){
        document.querySelector(".inputPriority").classList.add("red-line");
        document.querySelector(".inputPriority").focus();
        return false;
    }

    var _setMax = null;
    if(_getAllowInput != ""){
        _setMax = "maxlength="+_getCharacterLength;
    }

    var _createInput = "";
    var _createTag = "";

    if(_getInputPriority == "Required"){

        _createTag = '<div class="col-md-4 mb-20"><div class="lpay_tags-wrapper"><span class="lpay_tags-text">'+_getLabel+' <span class="text-red">*</span></span><span class="linkDlt" onClick="aboutDelete(this)"><img src="../image/cross.png"></span></div></div>';
        _createInput = '<div class="col-12 col-sm-6 col-lg-12"><div class="lpay_input_group form-group mb-20 row"><label for="" class="col-12 col-lg-4 col-form-label pt-0 pt-sm-5 font-size-12 font-size-lg-14 font-weight-medium d-lg-flex align-items-lg-center">'+_getLabel+' <span class="text-red ml-5">*</span><span class="addIcon" onClick="aboutDelete(this)"><span class="linkDlt" onClick="aboutDelete(this)"><img src="../image/cross.png"></span></label><div class="col-12 col-lg-8"><input name="'+_getLabel.replace(" ", "_")+'" '+_setMax+' oninput="'+_getAllowInput+'" placeholder="'+_getLabel+'" class="lpay_input form-control" required></div></div></div>';

    }else{

        _createTag = '<div class="col-md-4 mb-20"><div class="lpay_tags-wrapper"><span class="lpay_tags-text">'+_getLabel+'</span><span class="linkDlt" onClick="aboutDelete(this)"><img src="../image/cross.png"></span></div></div>';
        _createInput = '<div class="col-12 col-sm-6 col-lg-12"><div class="lpay_input_group form-group row mb-20"><label for="" class="col-12 col-lg-4 col-form-label pt-0 pt-sm-5 font-size-12 font-size-lg-14 font-weight-medium d-lg-flex align-items-lg-center">'+_getLabel+'</label><span class="addIcon" onClick="aboutDelete(this)"><i class="fa fa-minus-circle" aria-hidden="true"></i></span><div class="col-12 col-lg-8"><input name='+_getLabel.replace(" ", "_")+' placeholder="'+_getLabel+'" class="lpay_input form-control" '+_setMax+' oninput='+_getAllowInput+'></div></div></div>';

    }

    document.querySelector("#paymentDetails").innerHTML += _createTag;
    document.querySelector("#inputElement").innerHTML += _createInput;
    document.querySelector("#inputTags").value = document.querySelector("#inputElement").innerHTML.trim();
    var _allInputPopup = document.querySelectorAll("#inputBox input");
    _allInputPopup.forEach(function(index){
        index.value = "";
    });
    $("#inputBox .selectpicker").selectpicker('val', '');
    $("#inputBox .selectpicker").selectpicker('refresh');
    $("#cancelBtnInput").trigger("click");

}

document.querySelector("#saveBtnInput").addEventListener("click", createInput);

// create about content dynamically
var _headingCount = 1;
var _createElement = "";
function addAboutContent(){

    _headingCount++;
    _createElement = '<div class="col-md-4"><div class="row"><div class="col-md-12 mb-20"><div class="lpay_input_group"><label for="">Heading '+_headingCount+'</label><input type="text" class="lpay_input"><span class="addIcon" onClick="aboutDelete(this)"><i class="fa fa-minus-circle" aria-hidden="true"></i></span></div></div><div class="col-md-12 mb-20"><div class="lpay_input_group"><label for="">About Content '+_headingCount+'</label><textarea name="" class="lpay_input_textarea lpay_input" id="'+_headingCount+'" cols="30" rows="10"></textarea></div></div></div></div>';
    document.querySelector("#aboutContent").innerHTML += _createElement;

}

function aboutDelete(event){
    event.closest(".col-md-4").remove();
}

// download Page
document.querySelector("#downloadPage").onclick = function(e){

    var _getPayId = _selector("#merchant").value;
    _selector("#downloadPayId").value = _getPayId;
    _selector("#downloadCustomePage").submit();

}

// response msg
function setResponseMsg(){

    var _getMsg = _selector("#responseMsg").value;
    if(_getMsg == "success"){
        _selector(".lpay_success-custom").classList.remove("d-none");
        _selector(".add_link-btn").classList.add("d-none");
        _selector(".inputBtn").classList.add("d-none");
        _selector(".saveBtn").classList.add("d-none");
        _selector(".viewBtn").classList.remove("d-none");
        _selector("#downloadPage").classList.remove("d-none");
    }

    if(_getMsg == "error"){
        _selector(".lpay_error-custom").classList.remove("d-none");
    }

}

setResponseMsg();

// google fonts

var _googleFonts = {
    "Roboto" : ["\'Roboto\'","https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap"],
    "Open Sans" : ["\'Open Sans\'","https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600;700&display=swap"]
}

// paragraphSize validation
var inputLimit = function(e){
    var _this = this.value;
    var _number = parseInt(_this);
    console.log(_number);
}

// select box for function
function showFonts(_id){
    var _createFontLink = "<option value=''>Select Font Family</option>";
    for(key in _googleFonts){
        _createFontLink += "<option value="+key.replace(" ", "_")+" >"+key+"</option>";
    }
    document.querySelector(_id).innerHTML = _createFontLink;
    $(_id).selectpicker();
    $(_id).selectpicker("refresh");
}
showFonts("#headingFontFamily");
showFonts("#parahFontFamily");
// create input box for values
function createInputFontFamily(_id){
    var _familyInput = _id.slice(0);
    var family = _selector("#"+_id).options[_selector("#"+_id).selectedIndex].text;
    document.querySelector("."+_familyInput).innerHTML = "";
    var _createFamilyInput = "";
    _createFamilyInput += "<input type='hidden' value="+_googleFonts[family][0]+" name="+_familyInput+">";
    _createFamilyInput += "<input type='hidden' value="+_googleFonts[family][1]+" name="+_familyInput+"_link>";
    document.querySelector("."+_familyInput).innerHTML = _createFamilyInput;
}

document.querySelector("#headingFontFamily").onchange = function(e){
    createInputFontFamily(this.id);
}

document.querySelector("#parahFontFamily").onchange = function(e){
    createInputFontFamily(this.id);
}

// validate form
var _submit = document.querySelector(".saveBtn");
_submit.onclick = function(e){
    var _errorFree = false;
    var _getRequired = document.querySelectorAll("[data-required=true]");
    _getRequired.forEach(function(index){
        var _getAttr = index.closest(".lpay_tabs_content").getAttribute("data-target");
        if(index.value == ""){
            document.querySelector("[data-id="+_getAttr+"]").closest("li").classList.add("error-bullets");
            _errorFree = false;
            e.preventDefault();
        }else{
            document.querySelector("[data-id="+_getAttr+"]").closest("li").classList.remove("error-bullets");
            _errorFree = true;
        }
    })
    if(_errorFree == true){

    }else{
        e.preventDefault();
        return false;
    }
}