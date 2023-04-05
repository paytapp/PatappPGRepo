var _id = document.getElementById.bind(document);

$(document).ready(function(e) {
    // validation for PAN card
    var _checkPan = document.querySelector("[data-id='director2Pan']");
    if(_checkPan != null) {
        var _selectorPan = document.querySelectorAll(".pan-validation-director");
        _selectorPan.forEach(function(index, array, element) {
            _selectorPan[array].addEventListener('blur', function(e) {
                var _pan1 = document.querySelector("[data-id='director1Pan']");
                var _pan2 = document.querySelector("[data-id='director2Pan']");
                if(!index.classList.contains("red-line")) {
                    if(_pan1.value == _pan2.value) {
                        index.classList.add("red-line");
                        index.closest(".lpay_input_group").querySelector(".error-msg").innerHTML = "Pan card cannot be same.";
                    } else {
                        _pan1.classList.remove("red-line");
                        _pan1.closest(".lpay_input_group").querySelector(".error-msg").innerHTML = "";
                        _pan2.classList.remove("red-line");
                        _pan2.closest(".lpay_input_group").querySelector(".error-msg").innerHTML = "";
                    }
                }
            });
        });
    }

    var settlementFlagHandler = function(that) {
        var _activeInput = that.getAttribute("data-id");
        if(that.checked == true) {
            document.querySelector("#impsFlag").closest("label").classList.add("checkbox-checked");
            document.querySelector("#impsFlag").closest("label").classList.add("pointer-none");
            document.querySelector("#impsFlag").checked = true;
            _id("merchantForm_impsFlag").value = "true";

            _get.forEach(function(index, array, element) {
                _get[array].closest("label").classList.remove("checkbox-checked");
                _get[array].checked = false;
                _id("merchantForm_" + _get[array].getAttribute("id")).value = "false";
            });

            document.querySelector("[data-id="+_activeInput+"]").closest("label").classList.add("checkbox-checked");
            document.querySelector("[data-id="+_activeInput+"]").checked = true;
            _id("merchantForm_" + _activeInput).value = "true";
            
        } else {
            document.querySelector("[data-id="+_activeInput+"]").closest("label").classList.remove("checkbox-checked");
            document.querySelector("#impsFlag").closest("label").classList.remove("pointer-none");
            document.querySelector("[data-id="+_activeInput+"]").checked = false;
        }
    }

    // var _get = document.querySelectorAll(".impsFlag");
    // _get.forEach(function(index, array, element) {
    //     _get[array].addEventListener("change", function(e) {
    //         settlementFlagHandler(this);
    //     });

    //     setTimeout(() => {
    //         var isChecked = _get[array].closest("label").classList.contains("checkbox-checked");
    //         if(isChecked) {
    //             settlementFlagHandler(_get[array]);
    //         }
    //     }, 500);
    // });

    var _getEntity = $("[data-id='typeOfEntity']").val();
    if(_getEntity != ""){
        $("#typeOfEntity").closest(".col-md-4").attr("readonly", true);
    }
    
    function previewImg() {
        var _val = $(this).val();
        var _parent = $(this).closest("label");
        var _img = document.querySelector("#upload_img");
        var _inputFile = document.querySelector('#uploadMerchantLogo').files[0];
        var _reader = new FileReader();
        var _fileSize = $(this)[0].files[0].size;
        var _tmpName = _val.replace("C:\\fakepath\\", "");

        if(_fileSize < 2000000){
            _parent.attr("data-file", "success");
            console.log(_tmpName.length);
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

    var validateError = function(errorClass) {
        var _checkError = document.querySelector(errorClass);
        
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

        return true;
    }

    $("#btnSave").on("click", function(e) {
        var flag = validateError(".red-line");
        flag = flag && validateError(".error.show");
        flag = flag && NameValidater(_id("companyName"));
        
        if(flag) {
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
        } else {
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

    $("#surcharge input").on("change", function(e){
        $("#surcharge input").removeAttr("checked");
        $(this).attr("checked", true);
    });

    // refund check
    $(".refund-allowed input").on("click", function(e){
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
    });

    $("#uploadMerchantLogo").on("change", previewImg);

    $("#serverDetails input").on("change", function(e){
        var getInputVal = $(this).val();
        if(getInputVal == "owned") {
            $(".share-hosting").addClass("d-none");
        } else {
            $(".share-hosting").removeClass("d-none");
        }
    });

    if($("[data-id=serverDetails]").val() == "sharedOrCloud"){
        $(".share-hosting").removeClass("d-none");
    } else {
        $(".share-hosting").addClass("d-none");
    }    

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
    
    // tab function
    function tabChange() {
        var getClickTab = $(this).attr("data-target");
        $(".merchant__tab_button").removeClass("active-tab");
        $("[data-target='"+ getClickTab +"']").addClass("active-tab");
        $(".merchant__forms_block").removeClass("active-block");
        $("[data-active='"+ getClickTab +"']").addClass("active-block");
        if(getClickTab == "maker" || getClickTab == "checker" || getClickTab == "downloads" || getClickTab == "documents" || getClickTab == "eSign"){
            $("#btnSave").addClass("d-none");
        }else{
            $("#btnSave").removeClass("d-none");
        }
        if(getClickTab == "surchargeTextTab") {
            $(".merchant__buttons").addClass("d-none");
        } else {
            $(".merchant__buttons").removeClass("d-none");
        }
    }

    $(".merchant__tab_button").on("click", tabChange);

    $(".save__button").click(function(e) {
        $("#merchantForm").submit();
    });    

    // get all input checkbox 
    var getInputRadio = $(".checkbox-label input[type='checkbox']");
    for(var i = 0; i < getInputRadio.length; i++) {
        // console.log(getInputRadio);
        var getId = getInputRadio[i];
        if(getId.checked == true) {            
            if(getId.id == "oneTimeRefundAmount"){
                $("[data-target='refundAvailableLimit']").removeClass("d-none");
            }   
            $("#"+getId.id).attr("checked", true);
            $("#"+getId.id).closest("label").addClass("checkbox-checked");
        }
        // if(getInputRadio[i].checked == true) {
        //     var getId = getInputRadio[i].id;
        //     // console.log(getId);
        //     $("#"+getId).attr("checked", true);
        //     $("#"+getId).closest("label").addClass("checkbox-checked");
        // }

        $("#"+getId.id).closest("label").attr("for", getId.id);
    }

    $(".check-box input[type='checkbox']").on("change", function(e) {
        var getInput = $(this);
        var getId = getInput[0].id;
        if(getInput[0].checked == true) {
            $("#"+getId).closest("label").addClass("check-box-checked");
            if(getId == "surcharge") {
                $("[data-target='surchargeTextTab']").removeClass("d-none");
                horizontalScrollingNav();
            }
        } else {
            $("#"+getId).closest("label").removeClass("check-box-checked");
            $("#"+getId).closest("label").attr("for", getId);

            if(getId == "surcharge") {
                $("[data-target='surchargeTextTab']").addClass("d-none");
                horizontalScrollingNav();
            }
        }
    });


    $("#makerDownload").on("click", function(e){
        $("#makerFileDownload").submit();
    });

    $("#checkerDownload").on("click", function(e){
        $("#checkerFileDownload").submit();
    });

    $(".mpaDownload").on("click", function(e){
        var _that = $(this).attr("data-info");
        $("#fileType").val(_that);
        $("#mpaFileDownload").submit();
    });
});

$(window).on("load", function() {
    var refreshReseller = function() {
        var resellerId = $("#mpaResellerId").val();				
        
        if(resellerId !== "") {
            $("#resellerId").selectpicker('val', resellerId);
        }
    }

    refreshReseller();

    // CHECK IF REFUND AMOUNT LABEL IS CHECKED
    var isExtraRefundChecked = $("#extraRefundLabel").hasClass("checkbox-checked");
    if(!isExtraRefundChecked) {
        $(".extra-refund").addClass("d-none");
    }
    
 // CHECK IF ONE TIME REFUND AMOUNT LABEL IS CHECKED
    var isExtraRefundChecked = $("#oneTimeRefundLabel").hasClass("checkbox-checked");
    if(!isExtraRefundChecked) {
        $(".oneTime-refund").addClass("d-none");
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


// ====================================
$(document).ready(function() {
    // if($("#mpaCompnayName").val() == "" || $("#directorAddress").val() == "" || $("[data-id=accountNumber]").val() == "" || $("[data-id=showDownload]").val() == "false"){
    // 	$("[data-target=preview]").addClass("d-none");
    // 	$(".lpay_error-msg").removeClass("d-none");
    // 	$("#btnSave").attr("disabled", true);
    // }

    if($("#merchantStatus").val() == "APPROVED") {
        $("#status").attr("disabled", false);
    }    

    if($("[data-id=checkerDownload]").val() == "true") {
        $(".static-download").removeClass("d-none");
    } else {
        $(".static-download-none").removeClass("d-none");
    }

    if($('[data-id="showDownload"]').val() == "true") {
        $("#download-mpa").removeClass("d-none");
    }

    $("#mpaFiletype").on("change", function(e){
        if($(this).val() == ""){
            $("#uploadFileInput").attr("disabled", true);
        }else{
            $("#uploadFileInput").attr("disabled", false);
        }
        $("#uploadFileInput").val("");
        $("#uploadFileInput").attr("data-type", $(this).val());
        $("#uploadFileInput").closest("div").attr("data-response", "default");
    });

    $("#uploadFileInput").on("change", function(e){
        var getFormClass = $(this).closest("form").attr("class");
        var _that = $(this);
        var _parent = $(this).closest("label");
        var _getFileTypeTmp = document.querySelector("#mpaFiletype");
        var _getFileType = _getFileTypeTmp.options[_getFileTypeTmp.selectedIndex].text;

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
        if(getFileExact == ".png" || getFileExact == ".pdf" || getFileExact == ".jpeg" || getFileExact == ".jpg"){
            if(fileSize <= 2097152) {
                document.querySelector("body").classList.remove("loader--inactive");
                $.ajax({
                    type: "post",
                    enctype: "multipart/form-data",
                    url: "uploadFile",
                    data: data,
                    processData: false,
                    contentType: false,
                    success: function(data) {
                        document.querySelector("[data-id=uploaded-documents]").classList.remove("d-none");
                        var _uploadedDoc = "";
                        if($("[data-active=documents] #"+_getFileTypeTmp.value).length == 0) {
                            _uploadedDoc += "<span id="+_getFileTypeTmp.value+">"+ _getFileType +"</span>";
                            $("#uploadedDocuments").append(_uploadedDoc);
                        }
                        $("#download-mpa").removeClass("d-none");
                        document.querySelector("#uploadedFileName").innerText = getName;
                        _that.closest("div").attr("data-response", "success");
                        setTimeout(function(e) {
                            document.querySelector("body").classList.add("loader--inactive");
                        }, 1000);
                    }
                })
            } else {
                $(this).closest("div").attr("data-response", "sizeError");	
            }
        }else{
            $(this).closest("div").attr("data-response", "typeError");
        }
    });
    
    $(".checkbox-label input").on("change", function(e) {
        if ($(this).is(":checked")) {
            $(this).closest("label").addClass("checkbox-checked");
        } else {
            $(this).closest("label").removeClass("checkbox-checked");
        }
    });

    $(".downloadMpa").on("click", function(e) {
        var _this = $(this).attr("data-type");
        $("#fileType").val(_this);
        // return false;
        $("#mpaFileDownload").submit();
    })

    if($("#merchantStatus").val() != "" && $("#merchantStatus").val() != "PENDING"){
        $(".merchantAssign-filter").addClass("d-none");
    }else{
        $(".merchantAssing-status").addClass("d-none");
    }

    $("#mpaSaveStatus").on("click", function(e){
        var _status = $("#Editchecker").val();
        var _comment = $("#statusComment").val();
        var _payId = $("#merchantPayId").val();
        var _token = $("#customToken").val();
        if(_status == ""){
            alert("Please select status");
            return false;
        }
        $("body").removeClass("loader--inactive");
        $.ajax({
            type : "POST",
            url: "updateSubMerchantMPAStatusAction",
            data: { "token": _token, "payId": _payId, "merchantStatus": _status, "statusComment": _comment },
            success:function(data){						
                if($("#uploadDoc").val() != ""){
                    $(".static-download").removeClass("d-none");
                    $(".static-download-none").addClass("d-none");
                }else{
                    $(".static-download-none").removeClass("d-none");
                    $(".static-download").addClass("d-none");
                }
                if(data.responseStatus == "Success"){
                    $("[data-name=status]").html(data.merchantStatus);
                    $("[data-id=comments]").html(_comment);
                    $(".merchantAssign-filter").addClass("d-none");
                    $(".merchantAssing-status").removeClass("d-none");
                    if(data.merchantStatus == "APPROVED"){
                        $("#status").attr("disabled", false);
                        $("#status").selectpicker();
                        $("#status").selectpicker("refresh");
                    }
                }
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 1000);
            }
        });
    });

    // upload file 
    function uploadFunc(){
        var _file = this.files[0].size;
        var _filePath = $(this).val();
        var names = [];
        for (var i = 0; i < $(this).get(0).files.length; ++i) {
            names.push($(this).get(0).files[i].name);
        }
        var _payId = $("#merchantPayId").val();
        $("#uploadFilePayId").val(_payId);
        var _form = $("#fileUpload")[0];
        var  data = new FormData(_form);
        var _getFileName = _filePath.replace("C:\\fakepath\\", "");
        var _getPeriodPos= _filePath.lastIndexOf(".");
        var _getExtension = _filePath.slice(_getPeriodPos);
        data.append("fileName", names);
        if(_getExtension == ".png" || _getExtension == ".pdf" || _getExtension == ".xls" || _getExtension == ".xlsx" || _getExtension == ".csv" || _getExtension == ".jpg"){
            $(".upload-pic").removeClass("upload-denied");
            $(".upload-pic").addClass("upload-success");
            if(_file < 3000000){
                $.ajax({
                    type: "post",
                    enctype: "multipart/form-data",
                    url: "uploadCheckerDocument",
                    data: data,
                    processData: false,
                    contentType: false,
                    success: function(data){
                        $(".upload-pic").removeClass("upload-denied");
                        $(".upload-pic").addClass("upload-success");
                        $(".upload-text").text(names);
                    }
                });
            }else{
                $(".upload-pic").removeClass("upload-success");
                $(".upload-pic").addClass("upload-denied");
                $(".upload-text").text("file size should not greater then 2mb");
            }
        }else{
            $(".upload-pic").removeClass("upload-success");
            $(".upload-pic").addClass("upload-denied");
            $(".upload-text").text("file format dose not match");
        }
    }

    $("#uploadDoc").on("change", uploadFunc);
});

$("#horizontal-nav-content li").on("click", function() {
    var dataAction = $(this).attr("data-target"),
        btnRow = $("#btnSave").closest(".submit-row");
    if(dataAction == "preview") {
        btnRow.addClass("d-none");
    } else {
        btnRow.removeClass("d-none");
    }
});

$(".label-config").on("change", function() {
    var isChecked = $(this).hasClass("checkbox-checked"),
        hiddenInput = $(this).find('input[name="'+ $(this).attr("for") +'"]');
    
    hiddenInput.val(isChecked);
});

// ==============================================================


$(function() {
    $("#dateOfIncorporation").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        changeMonth : true,
        changeYear : true,
        dateFormat : 'dd-mm-yy',
        yearRange: "-100:+0",
    });
    $("#requestUrl").val($(".requestUrl").text());
});

function loadSubcategory() {
    var industry = document.getElementById("industryCategory").value;
    var token = document.getElementsByName("token")[0].value;

    $.ajax({
        type : "POST",
        url : "industrySubCategory",
        timeout : 0,
        data : {
            industryCategory : industry,
            token : token,
            "struts.token.name" : "token"
        },
        success : function(data, status) {
            var subCategoryListObj = data.subCategories;
            var subCategoryList = subCategoryListObj[0].split(',');
            var radioDiv = document.getElementById("radiodiv");
            radioDiv.innerHTML = "";
            for (var i = 0; i < subCategoryList.length; i++) {
                var subcategory = subCategoryList[i];
                var radioOption = document.createElement("INPUT");
                radioOption.setAttribute("type", "radio");
                radioOption.setAttribute("value", subcategory);
                radioOption.setAttribute("name", "subcategory");
                var labelS = document.createElement("SPAN");
                labelS.innerHTML = subcategory;
                radioDiv.appendChild(radioOption);
                radioDiv.appendChild(labelS);
            }

        },
        error : function(status) {
            alert("please try again later!!");
        }
    });
}

function selectSubcategory() {
    var checkedRadio = $('input[name="subcategory"]:checked').val();

    if (checkedRadio == null) {
        document.getElementById("radioError").innerHTML = "Please select a subcategory";
        return false;
    }

    document.getElementById("radioError").innerHTML = "";

    var subCategoryText = document.getElementById("subcategory");
    subCategoryText.value = checkedRadio;

    $('#popup').popup('hide');
}

$(document).ready(function() {

    // COUNTRY DEFAULT SELECTED
    populateCountries("country", "state");
    populateStates("country", "state");
    var dataCountry = $("#dataCountry").val();
    $("#country").selectpicker();
    $("#country").selectpicker('val', $("#mpaTradingCountry").val());
    if (dataCountry !== "") {
        $("#country").val(dataCountry);
    }

    setTimeout(function(e){
        $("#country").trigger("change");
        $("#country").selectpicker('val', $("#mpaTradingCountry").val());
        populateStates("country", "state", false);
        $("#state").selectpicker('refresh');
        $("#state").selectpicker('val', $("#mpaTradingState").val());
    }, 2000);

    var dataState = $("#state").val();
    if (dataState !== "") {
        $("#state").val(dataState);
    }
    $("#country").on("change", function(e) {
        $("#state").next("div").removeClass("disabled");
        // populateStates("country", "state", true);
        $("#state").selectpicker('refresh');

    })

    /* loadSubcategory(); */
    $("#subcategory").click(function() {
        $('#popup').popup({
            'blur' : false,
            'escape' : false
        }).popup('show');
    });
});

function CollapseAll(theClass, id) {
    var alldivTags = new Array();
    alldivTags = document.getElementsByTagName("div");

    for (i = 0; i < alldivTags.length; i++) {
        if (alldivTags[i].className == theClass && alldivTags[i].id != id) {
            $('#' + alldivTags[i].id).slideUp('slow');
            document.getElementById('Head' + alldivTags[i].id).className = 'acordion-gray';
        }
    }

    if (document.getElementById('Head' + id).className
            .search('acordion-open') != -1) {
        document.getElementById('Head' + id).className = 'acordion-gray';
    } else {
        document.getElementById('Head' + id).className = 'acordion-open acordion-gray';
    }
}

function destlayer() { //v6.0
    var i, p, v, obj, args = destlayer.arguments;
    for (i = 0; i < (args.length - 2); i += 3) {
        if ((obj = MM_findObj(args[i])) != null) {
            v = args[i + 2];
            if (obj.style) {
                obj = obj.style;
                v = (v == 'show') ? 'visible' : (v == 'hide') ? 'hidden'
                        : v;
            }
            obj.visibility = v;
        }
    }
}


function saveAction(event) {
    var userStatus = document.getElementById("status").value;

    if (userStatus == "ACTIVE") {
        var setlmentNamingVal = document
                .getElementById("settlementNamingConvention").value;
        var refundNamingVal = document
                .getElementById("refundValidationNamingConvention").value;

        if (setlmentNamingVal == "" || setlmentNamingVal == null
                && refundNamingVal == "" || refundNamingVal == null) {
            alert("Please enter Settlement and Refund Validation naming convention");
            event.preventDefault();
        } else {
            document.merchantSaveAction.submit();
            $('#loader-wrapper').show();
        }
    } else {
        document.merchantSaveAction.submit();
        $('#loader-wrapper').show();
    }
}

function cancelChanges() {
    $('#loader-wrapper').show();
    window.location.reload();
}

function showDivs(prefix, chooser) {
    for (var i = 0; i < chooser.options.length; i++) {
        var div = document
                .getElementById(prefix + chooser.options[i].value);
        div.style.display = 'none';
    }

    var selectedvalue = chooser.options[chooser.selectedIndex].value;

    if (selectedvalue == "PL") {
        displayDivs(prefix, "PL");
    } else if (selectedvalue == "PF") {
        displayDivs(prefix, "PF");
    } else if (selectedvalue == "PR") {
        displayDivs(prefix, "PR");
    } else if (selectedvalue == "CSA") {
        displayDivs(prefix, "CSA");
    } else if (selectedvalue == "LLL") {
        displayDivs(prefix, "LLL");
    } else if (selectedvalue == "RI") {
        displayDivs(prefix, "RI");
    } else if (selectedvalue == "AP") {
        displayDivs(prefix, "AP");
    } else if (selectedvalue == "T") {
        displayDivs(prefix, "T");
    }
}

function displayDivs(prefix, suffix) {
    var div = document.getElementById(prefix + suffix);
    div.style.display = 'block';
}

// window.onload = function() {
//     document.getElementById('select1').value = 'a';//set value to your default
// }

var _validFileExtensions = [ ".jpg", ".pdf", ".png" ];
function Validate(oForm) {
    var arrInputs = oForm.getElementsByTagName("input");
    for (var i = 0; i < arrInputs.length; i++) {
        var oInput = arrInputs[i];
        if (oInput.type == "file") {
            var sFileName = oInput.value;
            if (sFileName.length > 0) {
                var blnValid = false;
                for (var j = 0; j < _validFileExtensions.length; j++) {
                    var sCurExtension = _validFileExtensions[j];
                    if (sFileName.substr(
                            sFileName.length - sCurExtension.length,
                            sCurExtension.length).toLowerCase() == sCurExtension
                            .toLowerCase()) {
                        blnValid = true;
                        break;
                    }
                }

                if (!blnValid) {
                    alert("Sorry, " + sFileName
                            + " is invalid, allowed extensions are: "
                            + _validFileExtensions.join(", "));
                    return false;
                } else {
                    alert("Upload Successfully");
                }
            }
        }
    }
    return true;
}

var _validFileExtensions = [ ".jpg", ".gif", ".png" ];
function Validatelogo(oForm) {
    var arrInputs = oForm.getElementsByTagName("input");
    for (var i = 0; i < arrInputs.length; i++) {
        var oInput = arrInputs[i];
        if (oInput.type == "file") {
            var sFileName = oInput.value;
            if (sFileName.length > 0) {
                var blnValid = false;
                for (var j = 0; j < _validFileExtensions.length; j++) {
                    var sCurExtension = _validFileExtensions[j];
                    if (sFileName.substr(
                            sFileName.length - sCurExtension.length,
                            sCurExtension.length).toLowerCase() == sCurExtension
                            .toLowerCase()) {
                        blnValid = true;
                        break;
                    }
                }

                if (!blnValid) {
                    alert("Sorry, " + sFileName
                            + " is invalid, allowed extensions are: "
                            + _validFileExtensions.join(", "));
                    return false;
                } else {
                    alert("Upload Successfully");
                }
            }
        }
    }
    return true;
}


// setInterval(function() {
//     console.log(document.cookie);
// }, 1000);