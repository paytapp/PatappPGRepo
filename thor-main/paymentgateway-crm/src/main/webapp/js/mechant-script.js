

$(document).ready(function(e) {

    var refreshReseller = function() {
        var resellerId = $("#mpaResellerId").val();				
        
        if(resellerId !== "") {
            $("#resellerId").selectpicker('refresh');
            $("#resellerId").selectpicker('val', resellerId);
        }
    }

    refreshReseller();

    

    var _proprietory = {
        "label": {
            "director1Label" : "Proprietary Details",
            "cin" : "Shop Establishment Number",
        },
        "text": ['companyEmailId', 'companyRegisteredAddress'],
        "hide" : ['directorTwo', 'contact-detail', 'merchant-support']
    }
    
    var _privatePublic = {
        "label": {
            "director1Label" : "Director-1 Details",
            "director2Label" : "Director-2 Details",
            "cin" : "CIN",
        },
        "showText" : ['companyEmailId','companyRegisteredAddress'],
        "showDiv" : ['directorTwo', 'contact-detail', 'merchant-support']
    }
    
    var _partnershipFirm = {
        "label": {
            "director1Label" : "Partner-1 Details",
            "director2Label" : "Partner-2 Details",
            "cin" : "RegistrationNumber",
        },
        "showText" : ['companyEmailId', 'companyRegisteredAddress'],
        "showDiv" : ['directorTwo', 'contact-detail', 'merchant-support']
    }
    
    function jsonLoop(_json){
        console.log(_json.label);

        if(_json.label != undefined){
            for(key in _json.label){
                console.log(key);
                document.querySelector("[data-label='"+key+"']").innerText = _json.label[key];
            }
        }
        if(_json.text != undefined){
            for(var i = 0; i < _json.text.length; i++){
                document.querySelector("#"+_json.text[i]).closest(".col-md-4").classList.add("d-none");
            }
        }
        if(_json.hide != undefined){
            for(var i = 0; i < _json.hide.length; i++){
                document.querySelector("[data-hide="+_json.hide[i]+"]").classList.add("d-none");
            }
        }
        if(_json.showText != undefined){
            for(var i = 0; i < _json.showText.length; i++){
                document.querySelector("#"+_json.showText[i]).closest(".col-md-4").classList.remove("d-none");
            }
        }
        if(_json.showDiv != undefined){
            for(var i = 0; i < _json.showDiv.length; i++){
                document.querySelector("[data-hide="+_json.showDiv[i]+"]").classList.remove("d-none");
            }
        }
    
        if(_json.notRequired != undefined){
            for(var i = 0; i < _json.notRequired.length; i++){
                document.querySelector("#"+_json.notRequired[i]).closest(".col-md-4").querySelector(".imp").classList.add("d-none");
            }
        }
    
        if(_json.required != undefined){
            for(var i = 0; i < _json.required.length; i++){
                document.querySelector("#"+_json.required[i]).closest(".col-md-4").querySelector(".imp").classList.remove("d-none");
            }
        }
    
    }

    function getListData(){
        $.ajax({
            type : "POST",
            url: "getMpaFilesListAction",
            data: {
                "payId" : document.querySelector("#payId").value
            },
            success: function(data){
                var _uploadDiv = "";
                for(key in data.filesList){
                    var _name = data.filesList[key]['name'];
                    var _exactName = _name.slice(0, _name.lastIndexOf("_"));
                    _uploadDiv += '<div class="upload-div"><span class="upload-file-name" data-name="'+_exactName+'">'+_exactName.replaceAll("_", " ")+'</span><span data-path="'+data.filesList[key]['canonicalPath']+'" class="upload-file-download" download>Download</span></div>';
                }
                document.querySelector("#uploaded-file").innerHTML += _uploadDiv;
                if(document.querySelector(".upload-div") != null){
                    document.querySelector("#zip-download").classList.remove("d-none");
                }
            }

        })
    }

    getListData();

    var _private = {
        "articles_of_association": "Articles of Asscociation",
        "bank_account_details ": "Bank Account Details",
        "bank_statement_of_last_3_months": "Bank Statement of Last 3 Months",
        "board_resolution": "Board resolution",
        "cancelled_cheque": "Cancelled Cheque",
        "certificate_of_incorporation": "Certification of Incorporation",
        "company_address_proof": "Address Proof For The Company",
        "GST_registration_certificate": "GST Registration Certificate",
        "identification_documents_signatories": "Identification Documents Signatories",
        "list_of_director_for_MCA": "List of Director For MCA",
        "memorandom_of_association": "Memorandum of Association",
        "PAN_of_authorized_signatory_one": "PAN of authorized signatory 1",
        "PAN_of_authorized_signatory_two": "PAN of authorized signatory 2",
        "PAN_of_company": "PAN Card of Company"
    }

    var _partnership = {
        "bank_account_details ": "Bank Account Details",
        "bank_statement_of_last_3_months": "Bank Statement of Last 3 Months",
        "cancelled_cheque": "Cancelled Cheque",
        "company_address_proof": "Address Proof For The Company",
        "identification_document_of_partner_one": "identification Document Of Partner One",
        "identification_document_of_partner_two": "identification Document Of Partner Two",
        "PAN_of_company": "PAN Card of Company",
        "PAN_of_Firm": "PAN of firm",
        "PAN_of_partner_one": "PAN card of partner 1",
        "PAN_of_partner_two": "PAN card of parnter 2",
        "partnership_deed": "Partnership Deed",
        "photo_partner_one": "Passport Size Photo Partner One",
        "photo_partner_two": "Passport Size Photo Partner two",
        "proof_for_partnership_firm": "Proof For Partnership Firm"
    }

    var _prop = {
        "address_proof": "Address Proof",
        "bank_account_details ": "Bank Account Details",
        "bank_statement_of_last_3_months": "Bank Statement of Last 3 Months",
        "cancelled_cheque": "Cancelled Cheque",
        "GST_registration_certificate": "GST Registration of Certificate",
        "identification_documents_of_the_proprietor": "Identification Documents of the Proprietor",
        "PAN_of_proprietor": "PAN of Proprietor",
        "shop_establishment_certificate": "Shop Establishment Certificate / Udyog aadhar"
    }

    

    function uploaderFunction(e){
        var _getSelecteValue = document.querySelector("#mpaFileType").value;
        var _checkEntity = document.querySelector("#typeOfEntity").value;
        if(_checkEntity != ""){
            if(_getSelecteValue != ""){
                var _form = e.target.closest("form");
                var _data = new FormData(_form);
                _data.append("fileName", _getSelecteValue);
                var _fileSize = e.target.files[0]
                .size;
                var _fileName = e.target.files[0].name;
                if(e.target.value != ""){
                    document.querySelector("body").classList.remove("loader--inactive");
                    e.target.closest("label").querySelector(".default-upload").classList.add("d-none");
                    if(_fileSize < 2000000){
                        $.ajax({
                            type: "post",
                            enctype: 'multipart/form-data',
                            url: "uploadFile",
                            data: _data,
                            processData: false,
                            contentType: false,
                            success: function(data){
                                createFileUploadDiv(data);
                                e.target.closest("label").setAttribute("data-status", "success-status");
                                e.target.closest("label").querySelector(".fileName").innerText = _fileName;
                                document.querySelector("body").classList.add("loader--inactive");
                            }
                        })
                    }else{
                        e.target.closest("label").querySelector("#error-name").innerHTML = "File size too long"
                        e.target.closest("label").setAttribute("data-status", "error-status");
                        document.querySelector("body").classList.add("loader--inactive");
                    }
                }
            }else{
                alert("Please select document type first or type of entity");
            }
        }else{
            alert("Please select entity type first");
            tabChange("merchantDetails");
        }
    }

    function createFileUploadDiv(_data){
        var _checkNull = document.querySelector("[data-name='"+_data['fileName']+"']");
        if(_checkNull != null){
            _checkNull.closest("div").querySelector(".upload-file-name").innerText = _data['fileName'].replaceAll("_", " ");
            _checkNull.closest("div").querySelector(".upload-file-download").setAttribute("data-path", _data.mpaData['filePath']);

        }else{
            var _uploadDiv = '<div class="upload-div"><span class="upload-file-name" data-name="'+_data['fileName']+'">'+_data['fileName'].replaceAll("_", " ")+'</span><span data-path="'+_data.mpaData['filePath']+'" class="upload-file-download" download>Download</span></div>';
            document.querySelector("#zip-download").classList.remove("d-none");
            document.querySelector("#uploaded-file").innerHTML += _uploadDiv;
        }
    }


    document.querySelector("#fileUploader").onchange = function(e){
        uploaderFunction(e);
        setTimeout(function(e){
            $("#mpaFileType").val('default');
            $("#mpaFileType").selectpicker('refresh');
            $("#fileUploader").closest("label").attr("data-status", "");
            $(".default-upload").removeClass("d-none");
        }, 2000)
    }

    $("body").on("click", ".upload-file-download", function(e){
        var _getPath = $(this).attr("data-path");
        var _input = "<input type='hidden' value='"+_getPath+"' name='filePath' />";
        document.querySelector("#downloadSingleMpaFileAction").innerHTML = _input;
        document.querySelector("#downloadSingleMpaFileAction").submit();
    })

    

    var settlementFlagHandler = function(that) {
        var _activeInput = that.getAttribute("data-id");
        if(that.checked == true) {
            document.querySelector("#impsFlag").closest("label").classList.add("checkbox-checked");
            document.querySelector("#impsFlag").closest("label").classList.add("pointer-none");
            document.querySelector("#impsFlag").checked = true;
            // document.querySelector("#impsFlag").disabled = true;

            _get.forEach(function(index, array, element) {
                _get[array].closest("label").classList.remove("checkbox-checked");
                _get[array].checked = false;
            });

            document.querySelector("[data-id="+_activeInput+"]").closest("label").classList.add("checkbox-checked");
            document.querySelector("[data-id="+_activeInput+"]").checked = true;
        } else {
            document.querySelector("[data-id="+_activeInput+"]").closest("label").classList.remove("checkbox-checked");
            document.querySelector("#impsFlag").closest("label").classList.remove("pointer-none");
            document.querySelector("[data-id="+_activeInput+"]").checked = false; 
        // document.querySelector("#impsFlag").disabled = false;
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

    // if(_getEntity == "Proprietory"){
    //     jsonLoop(_prop);
    // }else if(_getEntity == "Partnership Firm"){
    //     jsonLoop(_partnershipFirm);
    // }else if(_getEntity == "Public Limited" || _getEntity == "Private Limited"){
    //     jsonLoop(_privatePublic);
    // }else if(_getEntity != ""){
    //     jsonLoop(_partnershipFirm);
    // }else{
    //     jsonLoop(_privatePublic);
    // }

    if(_getEntity != ""){
        $("#typeOfEntity").closest(".col-md-4").attr("readonly", true);
    }

    function loadDocument(_entity){
        if(_entity == "Private Limited" || _entity == "Public Limited"){
            dropDown(_private);
            jsonLoop(_privatePublic);
        }else if(_entity == "Partnership Firm"){
            dropDown(_partnership);
            jsonLoop(_partnershipFirm);
        }else if(_entity == "Proprietory"){
            dropDown(_prop);
            jsonLoop(_proprietory);
        }else if(_entity == ""){
            dropDown(_partnership);
            jsonLoop(_privatePublic);
        }else{
            dropDown(_private);
            jsonLoop(_privatePublic);
        }
    }

    loadDocument(_getEntity);

    document.querySelector("#typeOfEntity").onchange = function(e){
        var _data = e.target.value;
        loadDocument(_data);

    }

    function dropDown(_data){
        document.querySelector("#mpaFileType").innerHTML = "";
        var _option = "<option value=''>Select File</option>";
        for(key in _data){
            _option += "<option value='"+key+"'>"+_data[key]+"</option>"
        }

        document.querySelector("#mpaFileType").innerHTML = _option;

        $("#mpaFileType").selectpicker();
        $("#mpaFileType").selectpicker('refresh');
    }

    var _getTab = document.querySelector(".horizontal-nav-wrapper");
    var _getX = _getTab.getBoundingClientRect().y;
    window.addEventListener('scroll', function(e) {
        var _getWindow = window.scrollY;
        if(_getWindow > _getX){
            document.querySelector(".button_wrapper").classList.remove("button-wrapper");
        }else{
            document.querySelector(".button_wrapper").classList.add("button-wrapper");
        }
    });

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
    function tabChange(_selector) {
        var getClickTab = _selector;
        $(".merchant__tab_button").removeClass("active-tab");
        $("[data-target='"+ getClickTab +"']").addClass("active-tab");
        $(".merchant__forms_block").removeClass("active-block");
        $("[data-active='"+ getClickTab +"']").addClass("active-block");
        if(getClickTab == "maker" || getClickTab == "checker" || getClickTab == "downloads" || getClickTab == "documentUpload" || getClickTab == "eSign"){
            $(".button-wrapper").addClass("d-none");
        }else{
            $(".button-wrapper").removeClass("d-none");
        }
        if(getClickTab == "surchargeTextTab") {
            $(".merchant__buttons").addClass("d-none");
        } else {
            $(".merchant__buttons").removeClass("d-none");
        }
    }

    $(".merchant__tab_button").on("click", function(e){
        var _selector = e.target.getAttribute("data-target");
        tabChange(_selector);
    });

    var _getActive = localStorage.getItem("activeTab");

    if(_getActive != null && _getActive != ""){
        tabChange(_getActive);
    }

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