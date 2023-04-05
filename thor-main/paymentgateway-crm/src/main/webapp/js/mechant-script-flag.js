

function invokeAddRow(_this) {
    event.preventDefault();
    let _cloneRow = _this.closest(".row");
    let _clonedElement = _cloneRow.cloneNode(true);
    setTimeout(()=>{
        _clonedElement.querySelector("select[data-var='merchantEmailId']").setAttribute("id", new Date().getMilliseconds());
        var _selectPickerId = _clonedElement.querySelector("select[data-var='merchantEmailId']").getAttribute("id");
        $("#"+_selectPickerId).closest("div").find(".bootstrap-select").remove();
        $("#"+_selectPickerId).selectpicker(); 
        _clonedElement.querySelector("select[data-var='customerCategory']").setAttribute("id", new Date().getMilliseconds());
        var _selectPickerIdCustomer = _clonedElement.querySelector("select[data-var='customerCategory']").getAttribute("id");
        $("#"+_selectPickerIdCustomer).closest("div").find(".bootstrap-select").remove();
        $("#"+_selectPickerIdCustomer).selectpicker(); 
    }, 200)
    _clonedElement.querySelector("label[for]").setAttribute("for", new Date().getMilliseconds());
    _clonedElement.querySelector("input[type='checkbox']").setAttribute("id", _clonedElement.querySelector("label[for]").getAttribute("for"))
    _cloneRow.insertAdjacentElement("afterend", _clonedElement);
    let _isMultipleRowExist = document.querySelectorAll(".dlt-btn"); 
    if(_isMultipleRowExist.length > 1){
        $(".dlt-btn").removeClass("d-none");
    }else{
        document.querySelector(".static-mapping_parent").querySelector(".dlt-btn").classList.remove("d-none");
    }
    
}

function setLoadData (_this){
    let _value = _this.value;
    if(_value !== ''){
        _this.closest(".row").querySelector(".lpay_input").value = 0;
    }else{
        _this.closest(".row").querySelector(".lpay_input").value = '';
    }
}

function invokeDeleteRow(_this) {
    _this.closest(".row").remove();
}

function tabShow(_this, _data){
    var _allTab = document.querySelectorAll(".lpay_tabs_content");
    var _allLink = document.querySelectorAll(".lpay-nav-item");
    _allTab.forEach(function(index, element, array){
        index.classList.add("d-none");
    })
    _allLink.forEach(function(index, array, element){
        index.classList.remove("active");
    })
    _this.classList.add("active");
    document.querySelector("[data-target='"+_data+"']").classList.remove("d-none");
}

function invokeUpdateMapping() {

    var _loadDivident = {
        "DEFAULT": [],
        "SILVER" : [],
        "GOLD" : [],
        "DIAMOND" : [],
        "PLATINUM" : []
    }
    var _totalLoadCalculate = {
        "DEFAULT": null,
        "SILVER" : null,
        "GOLD" : null,
        "DIAMOND" : null,
        "PLATINUM" : null
    }

    event.preventDefault();
    var _getAllInputs = document.querySelectorAll(".payout_mapping-data");
    let _merchantNotBlank = true;
    let _obj = [];
    let _isLoadDivisionEqual = true;
    _getAllInputs.forEach((index, array, element) => {
        if(index.querySelector("input[type='checkbox']").checked === true){
            let _customerCategory = index.querySelector("[data-var='customerCategory']").value;
            _loadDivident[_customerCategory].push(index.querySelector(".lpay_input").value);
            console.log(_loadDivident[_customerCategory]);
        }
        if(index.querySelector("select").value === ''){
            _merchantNotBlank = false;
        }

        if(index.querySelector("select").value !== ''){
            _obj.push({
                merchantPayID: index.querySelector("[data-var='merchantEmailId']").value,
                customerCategory: index.querySelector("[data-var='customerCategory']").value,
                activeFlag: index.querySelector("input[type='checkbox']").checked,
                load: index.querySelector(".lpay_input").value === '' ? 0 : index.querySelector(".lpay_input").value
            })
        }
    })

    // console.log(_loadDivident);
    for(key in _loadDivident){
        let _categorizedLoadData = _loadDivident[key];
        let _loadCalculate = 0;
        if(_categorizedLoadData.length > 0){
            for(var i = 0; i < _categorizedLoadData.length; i++){
                // _loadDivident[key] += Number(_categorizedLoadData[i]);
                _loadCalculate = _loadCalculate + Number(_categorizedLoadData[i]);
            }
            _totalLoadCalculate[key] = _loadCalculate;
        }
    }

    for(key in _totalLoadCalculate){
        if(_totalLoadCalculate[key] !== null){
            if(_totalLoadCalculate[key] !== 100){
                _isLoadDivisionEqual = false;
            }
        }
    }

    if(_isLoadDivisionEqual === true && _merchantNotBlank !== ''){
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: 'POST',
            url: 'saveParentMerchantMapping',
            data: {
                "mappingData" : JSON.stringify(_obj),
                "parentPayId" : document.querySelector("[data-id='payId']").value
            },
            success: function(response){
                $(".lpay_popup-innerbox").attr("data-status", "success");
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text("Data has been saved successfully");
                $(".dlt-btn").removeClass("d-none");
                setTimeout(() => {
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500)
            }
        })
    }else{
        if(_isLoadDivisionEqual !== true){
            alert("Please divide load mulitply by 100");
        }else if( _merchantNotBlank === '' ){
            alert ("Please select any merchant");
        }
    }
}

function invokeParentMapping() {
    $.ajax({
        type: 'POST',
        url: 'fetchParentMerchantMappingList',
        data: {
            "parentPayId" : document.querySelector("[data-id='payId']").value
        },
        success: function(data){
            let _responseData = data.parentMerchantMappingList;
            let _clonedElement = document.querySelector(".payout_mapping-data").cloneNode(true);
            if(_responseData.length > 0){
                for(var i = 0; i < _responseData.length; i++){
                    console.log(_responseData[i]['activeFlag']);
                    document.querySelector(".payout_mapping-list").insertAdjacentElement('afterend', _clonedElement);
                    _clonedElement.setAttribute("id", "parentPayoutMapping"+[i]);
                    document.querySelector("#parentPayoutMapping"+[i]).querySelector(".lpay_input").value = _responseData[i]['load'];
                    _clonedElement.querySelector(".dlt-btn").classList.add("saved-delete");
                    $("#parentPayoutMapping"+[i]+" [data-var='merchantEmailId']").closest("div").find(".bootstrap-select").remove();
                    $("#parentPayoutMapping"+[i]+" [data-var='merchantEmailId']").selectpicker('refresh');
                    $("#parentPayoutMapping"+[i]+" [data-var='merchantEmailId']").selectpicker('val', _responseData[i]['merchantPayId']);
                    $("#parentPayoutMapping"+[i]+" [data-var='customerCategory']").closest("div").find(".bootstrap-select").remove();
                    $("#parentPayoutMapping"+[i]+" [data-var='customerCategory']").selectpicker('refresh');
                    $("#parentPayoutMapping"+[i]+" [data-var='customerCategory']").selectpicker('val', _responseData[i]['customerCategory']);
                    _clonedElement.querySelector("label[for]").setAttribute("for", "label"+[i]);
                    _clonedElement.querySelector("input[type='checkbox']").setAttribute("id", "lable"+[i]);
                    document.querySelector("#parentPayoutMapping"+[i]+" input[type='checkbox']").checked = _responseData[i]['activeFlag'];
                    if(document.querySelector("#parentPayoutMapping"+[i]+" input[type='checkbox']").checked == true){
                        $("#parentPayoutMapping"+[i]).find(".checkbox-label").addClass("checkbox-checked"); 
                    }else{
                        $("#parentPayoutMapping"+[i]).find(".checkbox-label").removeClass("checkbox-checked"); 
                        document.querySelector("#parentPayoutMapping"+[i]).querySelector(".lpay_input").value = 0;
                    }
                    _clonedElement.querySelector(".add-btn").classList.add("d-none");
                    _clonedElement = document.querySelector("#parentPayoutMapping"+[i]).cloneNode(true);
                }
                $(".dlt-btn").removeClass("d-none");
                document.querySelector(".dlt-btn").classList.add("d-none");
            }
        }
    })
}

$(".confirmButton").on("click", function(e){
    location.reload();
})

function invokeDeleteMapping(_this) {
    let _classList = _this.classList.toString();
    if(_classList.indexOf("saved-delete") !== -1){
        let _confirmPrompt = confirm("Are sure you want to delete this mapping.");
        if(_confirmPrompt){
            _this.closest(".row").remove();
            let _checkIfOnlyOneRow = document.querySelectorAll(".static-mapping_parent .dlt-btn");
            if(_checkIfOnlyOneRow.length > 1){
                $(".static-mapping_parent .dlt-btn").removeClass("d-none");
            }else{
                $(".static-mapping_parent .dlt-btn").addClass("d-none");
                // document.querySelector(".static-mapping_parent").querySelector(".dlt-btn").classList.add("d-none");
            }
        }
    }else{
        _this.closest(".row").remove();
        let _checkIfOnlyOneRow = document.querySelectorAll(".static-mapping_parent .dlt-btn");
            if(_checkIfOnlyOneRow.length > 1){
                $(".static-mapping_parent .dlt-btn").removeClass("d-none");
            }else{
                $(".static-mapping_parent .dlt-btn").addClass("d-none");
                // document.querySelector(".static-mapping_parent").querySelector(".dlt-btn").classList.add("d-none");
            }
    }
    // if(_classList.indexOf("saved-delete") !== -1){
    //     let _requestedParam = {
    //         "merchantPayId": _currentElement.querySelector("select").value,
    //         "parentPayId" : document.querySelector("[data-id='payId']").value
    //     }
    //     document.querySelector("body").classList.remove("loader--inactive");
    //     $.ajax({
    //         type: 'POST',
    //         url: 'deleteParentMerchantMapping',
    //         data: _requestedParam,
    //         success: function(responseJson){
    //             $(".lpay_popup-innerbox").attr("data-status", "success");
    //             $(".lpay_popup").fadeIn();
    //             $(".responseMsg").text("Record has been deleted successfully");
    //             let _checkIfMultiple = document.querySelectorAll(".payout_mapping-data");
    //             if(_checkIfMultiple.length > 1){
    //                 _currentElement.remove();
    //             }else{
    //                 var _getSelectpickerId = _currentElement.querySelector("select").id;
    //                 $("#"+_getSelectpickerId).val('default');
    //                 $("#"+_getSelectpickerId).selectpicker('refresh');
    //                 _currentElement.querySelector("input[type='checkbox']").checked = false;
    //                 _currentElement.querySelector(".checkbox-label").classList.remove("checkbox-checked");
    //                 _currentElement.querySelector(".lpay_input").value = '';
    //             }
    //             setTimeout(() => {
    //                 document.querySelector("body").classList.add("loader--inactive");
    //             }, 500)
    //             console.log(responseJson)
    //         }
    //     })
    // }else{
    //     _this.closest(".row").remove();
    //     let _checkIfOnlyOneRow = document.querySelectorAll(".static-mapping_parent .dlt-btn");
    //     if(_checkIfOnlyOneRow.length > 1){
    //         $(".static-mapping_parent .dlt-btn").removeClass("d-none");
    //     }else{
    //         $(".static-mapping_parent .dlt-btn").addClass("d-none");
    //         // document.querySelector(".static-mapping_parent").querySelector(".dlt-btn").classList.add("d-none");
    //     }
        
    // }
}

invokeParentMapping();

var invokeLoadPercentage = function(event, _this){
    _this.closest(".lpay_input_group").querySelector(".error-msg").innerHTML = '';
    var _val = Number(_this.value);
    if(_val > 100){
        _this.value = 0;
    }
}

$(document).ready(function(e) {


    function previewImg(){
        var _val = $(this).val();
        var _parent = $(this).closest("label");
        var _img = document.querySelector("#upload_img");
        var _inputFile = document.querySelector('#uploadMerchantLogo').files[0];
        var _reader = new FileReader();
        var _fileSize = $(this)[0].files[0].size;
        var _tmpName = _val.replace("C:\\fakepath\\", "");

        if(_fileSize < 2000000){
            _parent.attr("data-file", "success");
            if(_tmpName.length > 20) {
                var strStart = _tmpName.slice(0, 15),
                    strEnd = _tmpName.slice(_tmpName.length - 9, _tmpName.length);
                
                _tmpName = strStart + "..." + strEnd;
            }
            _parent.find("#merchantLogoName").text(_tmpName);
            _reader.addEventListener("load", function(){
                _img.src = _reader.result;
    
            }, false);
    
            if(_inputFile){
                _reader.readAsDataURL(_inputFile);
            }
        }else{
            _img.src = "";
            _parent.attr("data-file", "size-error");
        }
    }
    
    var checkError = function(_selector, _visibility){
        var _globalCheck = true;
        var _select = document.querySelectorAll(_selector);
        _select.forEach(function(index, array, element){
            if(_select[array].closest(_visibility) == null){
                if(_select[array].value == ""){
                    _globalCheck = false;
                    _select[array].classList.add("red-line");
                    _select[array].focus();
                }else{
                    _globalCheck = true;
                    _select[array].classList.remove("red-line");
                }
            }else{
               
            }
        })
        if(_globalCheck == false){
            return false;
        }else{
            return true;
        }
    }


    $("#btnSave").on("click", function(e) {
        var _flag =  checkError("[data-mandate]", ".d-none");
        
        if(_flag){



            var _checkError = document.querySelector(".red-line"); 
    
            var _active = document.querySelector(".merchant__tab_button.active-tab").getAttribute("data-target");
            
            localStorage.setItem("activeTab", _active);
    
            var _mandate = document.querySelectorAll("data-mandate");
    
            if(_checkError != null) {
                var _getAttribute = _checkError.closest(".merchant__forms_block").getAttribute("data-active");
    
                var _tabs = document.querySelectorAll(".merchant__forms_block");
                var _tabsLink = document.querySelectorAll(".merchant__tab_button");
    
                _tabsLink.forEach(function(index, array, element){
                    _tabsLink[array].classList.remove("active-tab");
                });
    
                _tabs.forEach(function(index, array, element){
                    _tabs[array].classList.remove("active-block");
                });            
                
                _checkError.closest(".merchant__forms_block").classList.add("active-block");
                document.querySelector("[data-target='"+_getAttribute+"']").classList.add("active-tab");
    
                _checkError.focus();
    
                return false;
            }
    
            if(document.querySelector("[data-id='oneTimeRefundAmount']") !== null) {
                var _getOneTimeRefund = document.querySelector("[data-id='oneTimeRefundAmount']").value;            
                var _checkOneTimeRefund = document.querySelector("#oneTimeRefundLimit").value;
    
                if(_getOneTimeRefund == _checkOneTimeRefund){
                    document.querySelector("#LimitChangedFlag").value = false;
                } else {
                    document.querySelector("#LimitChangedFlag").value = true;
                }
            }
    
            var isLogoFlag = $("#logoFlag").is(":checked"),
                uploadImg = $("#upload_img").attr("src");
    
            if(isLogoFlag && uploadImg == "") {
                alert("Please upload merchant logo");
                return false;
            }

            document.querySelector("body").classList.remove("loader--inactive");

        }else{
            return false;
        }

    });


    var _getRefundLimit = parseFloat($("#RefundLimitRemains").val());
    var _getAvailable = parseFloat($("#oneTimeRefundLimit").val());
    if(_getRefundLimit == 0 && _getAvailable > 0){
        $("[data-target='reniewSameLimit']").removeClass("d-none");
    }else{
        $("[data-target='reniewSameLimit']").addClass("d-none");
    }

    $("#uploadMerchantLogo").on("change", previewImg);

    // MERCHANT LOGO
    var _merchantLogoSrc = $("[data-id='merchantLogo']").val();
    if(_merchantLogoSrc !== "") {
        $("#upload_img").attr("src", "data:image/png;base64," + _merchantLogoSrc);
        $(".upload-custom-logo").removeClass("d-none");
        $("#title-upload-logo").text("Change Logo");
        $("#allow-logo-pg").removeClass("d-none");
    }

    // LOGO FLAG
    $("#logoFlag").on("change", function(e) {
        if($(this).is(":checked")) {
            $(".upload-custom-logo").removeClass("d-none");
            $(".uploaded-logo").removeClass("d-none");
            $("#allow-logo-pg").removeClass("d-none");

            if(_merchantLogoSrc !== "") {
                $("#upload_img").attr("src", "data:image/png;base64," + _merchantLogoSrc);
                $(".upload-custom-logo").removeClass("d-none");
                if($("#uploadMerchantLogo").val() == "") {
                    $("#value-merchantLogo").val(_merchantLogoSrc);
                }
            }
        } else {
            $(".uploaded-logo").addClass("d-none");
            $(".uploaded-logo img").attr("src", "");
            $(".upload-custom-logo").addClass("d-none");
            $("#allow-logo-pg").addClass("d-none");
            $("#allow-logo-pg label").removeClass("checkbox-checked");
            $("#allowLogoInPgPage").prop("checked", false);
            $("#allowLogoInPgPage").attr("value", false);
            $("#value-merchantLogo").val("");

            $("#uploadMerchantLogo").val("");
            $("#label-upload-logo").removeAttr("data-file");
            $("#merchantLogoName").text("");
        }
    });

    $("#allowLogoInPgPage").on("change", function() {
        var isChecked = $(this).is(":checked");
        if(isChecked) {
            $(this).attr("value", true);
        } else {
            $(this).attr("value", false);
        }
    });
    
    



    $(".save__button").click(function(e) {
        $("#merchantForm").submit();
    });    

    // get all input checkbox 
    var getInputRadio = $(".checkbox-label input[type='checkbox']");
    for(var i = 0; i < getInputRadio.length; i++) {
        var getId = getInputRadio[i];
        if(getId.checked == true) {   
            
            _checkCardFlag = getId.closest(".save-flag");
            _checkHostedUrl = getId.closest(".hosted-url");
            if(_checkCardFlag != null){
                _checkCardFlag.querySelector(".save-flag-select select").disabled = false;
            }
            
            if(_checkHostedUrl != null){
                _checkHostedUrl.querySelector(".lpay_input_group").classList.add("active-hosted-input");
            }
            
            if(getId.id == "oneTimeRefundAmount"){
                $("[data-target='refundAvailableLimit']").removeClass("d-none");
            }   
            $("#"+getId.id).attr("checked", true);
            $("#"+getId.id).closest("label").addClass("checkbox-checked");
        }

        $("#"+getId.id).closest("label").attr("for", getId.id);
    }

    // CHECK IF REFUND AMOUNT LABEL IS CHECKED
    var isExtraRefundChecked = $("#extraRefundLabel").hasClass("checkbox-checked");
    if(!isExtraRefundChecked) {
        $(".extra-refund").addClass("d-none");
    }
    
 // CHECK IF ONE TIME REFUND AMOUNT LABEL IS CHECKED
    var _isExtraRefundChecked = $("#oneTimeRefundLabel").hasClass("checkbox-checked");
    console.log(_isExtraRefundChecked);
    if(!_isExtraRefundChecked) {
        $(".oneTime-refund").addClass("d-none");
    }

    function whiteListUrlFlag(_selector){
        var _isChecked = _selector.checked;
        if(_isChecked == true){
            document.querySelector("#whiteListReturnUrl").closest(".lpay_input_group").classList.remove("d-none");
        }else{
            document.querySelector("#whiteListReturnUrl").closest(".lpay_input_group").classList.add("d-none");
        }
    }
    whiteListUrlFlag(document.querySelector("#whiteListReturnUrlFlag"));

    document.querySelector("#whiteListReturnUrlFlag").onchange = function(e){
        whiteListUrlFlag(e.target);
    }

});


var decimalPoint = function(that) {
    var val = that.value;
    if(val !== "") {
        val = Number(val);
        that.value = val.toFixed(1);
    }
}

var _uppercase = function(that) {
    var _val = that.value;
    _val = _val.toUpperCase();
    that.value = _val;
}