function checkLength(e, msg){
    var _getReg = e.getAttribute("data-reg");
    var _value = e.value;
    if(_value != "") {
        var _newReg = new RegExp(_getReg);
        if(_newReg.test(_value) == true){
            e.classList.remove("red-line");
            e.removeAttribute("data-error");
        }else{
            e.classList.add("red-line");
            e.setAttribute("data-error", "Invalid "+msg);
            e.closest(".lpay_input_group").querySelector(".error-msg").innerText = ""
        }
    } else {
        e.classList.remove("red-line");
        e.removeAttribute("data-error");
        e.closest(".lpay_input_group").querySelector(".error-msg").innerText = "";
    }
}

function blurMsg(e){
    var _this = e.target.getAttribute("data-error");
    if(_this != "NA") {
        console.log(e.target.closest(".lpay_input_group"));
        e.target.closest(".lpay_input_group").querySelector(".error-msg").innerText = _this;
    }
}

function removeError(_that){
    _that.classList.remove("red-line");
}


function removeErrorBlank(_that){
    if(_that.value == ""){
        _that.classList.remove("red-line");
    }
}

// company name validator
function NameValidater(e) {
    var getName = e.value;

    if(getName !== "") {
        var _getEntity = document.querySelector("#typeOfEntity").value;
        if(_getEntity == "Private Limited" || _getEntity == "Public Limited") {
            var findWord = getName.match(/limited/i);
            var nameValidate = getName.lastIndexOf(findWord);
            if (nameValidate != -1 && getName.length > 9) {
                e.classList.remove("red-line");
                e.removeAttribute("data-error");
                e.closest(".lpay_input_group").querySelector(".error-msg").innerText = "";
                return true;
            } else {
                e.classList.add("red-line");
                e.setAttribute("data-error", "Invalid Company Name");
                e.closest(".lpay_input_group").querySelector(".error-msg").innerText = e.getAttribute("data-error");
                return false;
            }
        }
    } else {
        e.classList.remove("red-line");
        e.removeAttribute("data-error");
        e.closest(".lpay_input_group").querySelector(".error-msg").innerText = "";
    }

    return true;
}

function loopFunction(_selector, _value){
    _selector.forEach(function(index, array, element){
        index.innerText = _value
    })
}

var _partnerShip = {
    "title" : "Registration Number",
    "name" : "registrationNumber"
}

var _privateLimited = {
    "title" : "CIN",
    "name" : "cin"
}

var _proprietory = {
    "title" : "Shop Establish Number",
    "name" : "registrationNumber"
}

function forLoop(_value, _lableSelector, _nameSelector) {
    for(key in _value) {
        
        console.log(_lableSelector);

        console.log(_value);

        document.querySelector(_lableSelector).innerText = _value["title"];
        document.querySelector(_nameSelector).setAttribute("name", _value["name"]);
    }
}

function getEntity(e){
    var _var = e.value;
    var _heading = document.querySelectorAll("[data-heading]");
    if(_var == "Partnership Firm" || _var == "Other"){
        forLoop(_partnerShip, "#registerationTitle", "#cin");
        loopFunction(_heading, "Partner Details");
    } else if(_var == "Private Limited" || _var == "Public Limited"){
        forLoop(_privateLimited, "#registerationTitle", "#cin");
        loopFunction(_heading, "Director Details");
    } else if(_var == "Proprietory"){
        forLoop(_proprietory, "#registerationTitle", "#cin");
        loopFunction(_heading, "Proprietory Details");
        _heading[1].closest(".col-md-12").classList.add("d-none");
    }

    if(e.value == ''){
        $("#companyName").attr("readonly", true);
    }else{
        $("#companyName").attr("readonly", false);
    }

    if(_var == "Other"){
        forLoop(_partnerShip, "#registerationTitle", "#cin");
        document.querySelector("[data-target='otherEntity']").classList.remove("d-none");
        document.querySelector("[data-target='otherEntity']").querySelector("input").setAttribute("name", "typeOfEntity");
        document.querySelector("#typeOfEntity").removeAttribute("name");
    }else{
        document.querySelector("[data-target='otherEntity']").classList.add("d-none");
        document.querySelector("[data-target='otherEntity']").querySelector("input").removeAttribute("name");
        document.querySelector("#typeOfEntity").setAttribute("name", "typeOfEntity");
    }

    if(_var == "Proprietory"){
        // document.querySelector("[data-heading]").classList.add("d-none");
        var _getSelectorInput = document.querySelectorAll("[data-target='proprietory']");
        _getSelectorInput.forEach(function(index, array, element){
            index.closest(".col-md-4").classList.add("d-none");
            index.disabled = true;
        })
    }else{
        document.querySelector("[data-heading]").classList.remove("d-none");
        var _getSelectorInput = document.querySelectorAll("[data-target='proprietory']");
        _getSelectorInput.forEach(function(index, array, element){
            index.closest(".col-md-4").classList.remove("d-none");
            index.disabled = false;
        })
    }
}

function numeric(that){
    that.value = that.value.replace(/[^0-9]/g, '');
}

function alphaNumericAlt(e) {
    var _alpha = /^[A-Za-z0-9]/;
    var _this = e.value;
    if(!_alpha.test(_this)) {
        e.value = _this.slice(0, _this.length-1);
    }
}

// valid email 
var _validEmail = (e) => {
    console.log(e);
}

$(document).ready(function() {

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

    // $("#gstin").on("input", function(e){
    //     var _regEx = //;
    //     var _email = $(this).val();
    //     if(_email.match(_regEx)){
    //         $(this).removeClass("red-line");
    //     }else{
    //         $(this).addClass("red-line");
    //     }
    // });

    // $("#accountIfsc").on("input", function(e){
    //     var _regEx = /^[A-Z]{4}0[A-Z0-9]{6}$/;
    //     var _email = $(this).val();
    //     if(_email.match(_regEx)){
    //         $(this).removeClass("red-line");
    //     }else{
    //         $(this).addClass("red-line");
    //     }
    // })

    // function for maxlength
    
    setInterval(function(){
        $("#saveMessage").addClass("d-none")
    }, 3000);

    var _id = document.getElementById.bind(document);

    var paymentTypeObj = {
        "Credit Card" : "CC",
        "Debit Card" : "DC",
        "Net Banking" : "NB",
        "UPI" : "UP",
        "COD" : "CD",
        "EMI CC" : "EMCC",
        "EMI DC" : "EMDC",
        "Wallet" : "WL"
    }

    $("#paymentMethod").on("change", function() {
        var _val = $(this).val();
        if(_val == "UPI") {
            $("#col-paymentRegion, #col-cardHolderType, #col-mopType").css("display", "none");
        } else if(_val == "Net Banking") {
            $("#col-paymentRegion, #col-cardHolderType").css("display", "none");
            $("#col-mopType").css("display", "block");
            $("#paymentRegion, #cardHolderType").val("ALL");
        } else {
            $("#col-paymentRegion, #col-cardHolderType, #col-mopType").css("display", "block");
            $("#paymentRegion, #cardHolderType, #mopType").val("ALL");
        }
    });

    $("#paymentRegion").on("change", function() {
        var _val = $(this).val();

        if(_val == "INTERNATIONAL") {
            $("#col-cardHolderType, #col-mopType, #col-amount").css("display", "none");
            $("#cardHolderType, #mopType, #amount").val("ALL");
        } else {
            $("#col-cardHolderType, #col-mopType, #col-amount").css("display", "block");
        }
    });

    // ADD SELECT OPTION
    var _addSelectOption = function(reffObj) {
        var opt = document.createElement("option");
        opt.appendChild(document.createTextNode(reffObj.text));
        opt.value = reffObj.value;
        _id(reffObj.element).appendChild(opt);
    }

    var fetchMopList = function(mopList) {
        _id("mopType").options.length = 1;

        if(mopList !== undefined) {
            for(var i = 0; i < mopList.length; i++) {
                _addSelectOption({text: mopList[i], value: mopList[i], element: "mopType"});
            }
        }

        $("body").addClass("loader--inactive");
    }

    $("#paymentMethod").on("change", function() {
        var paymentType = $(this).val();
        var payId = $("#payId").val();
        var txnType = "SALE";

        if(payId !== "" && txnType !== "" && paymentType !== "") {
            $("body").removeClass("loader--inactive");
            let token  = document.getElementsByName("token")[0].value;

            $.ajax({
                type: "POST",
                url: "getSufDetailMopTypeAction",
                data: {
                    paymentType : paymentType,
                    txnType : txnType,
                    payId : payId,
                    "token" : token,
                    "struts.token.name" : "token"
                },
                success: function(data) {
                    console.group("getSufDetailMopTypeAction()");
                    console.log(data);
                    console.groupEnd();
                    fetchMopList(data.mopList);
                },
                error: function(data) {
                    alert("Something went wrong!");
                }
            });
        } else {
            // $("#moptype-wrapper").addClass("d-none");
            _id("mopType").options.length = 1;
        }
    });

    var fetchSurchargeTextDetail = function(data) {
        var surchargeTextTable = $("#table-surchargeText");
        $("#surchargeText, #paymentMethod").val("");
        $("#paymentRegion, #cardHolderType, #mopType, #amount").val("ALL");

        if(data !== undefined) {
            if(data.length > 0) {
                surchargeTextTable.removeClass("d-none");
                surchargeTextTable.find("tbody").html("");
                var tableData = "";
                for(var i = 0; i < data.length; i++) {
                    tableData += '<tr id="row-'+ i +'">';
                    
                        // PAYMENT TYPE
                        tableData += '<td data-name="paymentType" data-value="'+ paymentTypeObj[data[i]['paymentType']] +'">'+  data[i]['paymentType'] +'</td>';
    
                        // PAYMENT REGION
                        tableData += '<td data-name="paymentRegion" data-value="'+ data[i]['paymentRegion'] +'">'+ data[i]['paymentRegion'] +'</td>';
    
                        // CARD HOLDER TYPE
                        tableData += '<td data-name="cardHolderType" data-value="'+ data[i]['cardHolderType'] +'">'+ data[i]['cardHolderType'] +'</td>';
    
                        // MOP TYPE
                        tableData += '<td data-name="mopType" data-value="'+ data[i]['mopType'] +'">'+ data[i]['mopType'] +'</td>';
    
                        // SURCHARGE TEXT
                        tableData += '<td data-name="surchargeText" data-value="'+ data[i]['surchargeText'] +'">'+ data[i]['surchargeText'] +'</td>';
    
                        // AMOUNT SLAB
                        tableData += '<td data-name="amountSlab" data-value="'+ data[i]['minTxnAmount'] + '-' + data[i]['maxTxnAmount'] +'">'+ data[i]['minTxnAmount'] + ' - ' + data[i]['maxTxnAmount'] +'</td>';
                    
                    tableData += '<td><div class="edit-section"><a href="#" class="btn-edit-row">Edit</a></div></td>';
                    tableData += '</tr>';
                }
    
                surchargeTextTable.find("tbody").html(tableData);
            } else {
                surchargeTextTable.addClass("d-none");
            }
        } else {
            surchargeTextTable.addClass("d-none");
        }
    }

    var showAllSurchargeText = function() {
        var payId = $("#payId").val(),
            token = document.getElementsByName("token")[0].value;
        
        $.ajax({
            type: "POST",
            url: "showAllSurchargeText",
            data: {
                payId: payId,
                token: token,
                "struts.token.name": "token"
            },
            success: function(data) {
                console.log("Success ShowAllSurchargeText()");
                fetchSurchargeTextDetail(data.aaData);
            },
            error: function(data) {
                alert("Something went wrong!");
            }
        });

        // $.get("../js/merchantAccountSetup.json", function(data) {
        //     fetchSurchargeTextDetail(data.aaData);
        // });
    }

    // showAllSurchargeText();

    $("body").on("click", ".btn-edit-row", function(e) {
        e.preventDefault();

        var parentRow = $(this).closest("tr");
        var getId = parentRow.attr("id");
        var surchargeText = parentRow.find('td').eq(4).text();
        $("#surchargeText").val(surchargeText);

        $(".surchargeText-create-section").addClass("d-none");
        $(".surchargeText-edit-section").removeClass("d-none");
        $(".surchargeText-edit-section").attr("data-id", getId);

        $("#paymentMethod, #paymentRegion, #cardHolderType, #mopType, #amount").attr("disabled", true);
    });

    $("body").on("click", "#btn-delete-surchargeText", function(e) {
        e.preventDefault();

        var result = confirm("Are you sure, You want to delete the current data.");

        if(result) {
            $("#paymentMethod, #paymentRegion, #cardHolderType, #mopType, #amount").attr("disabled", false);

            var rowId = $(this).closest(".surchargeText-edit-section").attr("data-id"),
                paymentType = $("#" + rowId).find("[data-name='paymentType']").attr("data-value"),
                paymentRegion = $("#" + rowId).find("[data-name='paymentRegion']").attr("data-value"),
                cardHolderType = $("#" + rowId).find("[data-name='cardHolderType']").attr("data-value"),
                mopType = $("#" + rowId).find("[data-name='mopType']").attr("data-value"),
                surchargeText = $("#" + rowId).find("[data-name='surchargeText']").attr("data-value"),
                amountSlab = $("#" + rowId).find("[data-name='amountSlab']").attr("data-value"),
                payId = $("#payId").val(),
                businessName = $("#businessName").val(),
                token = document.getElementsByName("token")[0].value;

            $.ajax({
                type: "POST",
                url: "deleteSurchargeText",
                data: {
                    payId: payId,
                    merchantName: businessName,
                    paymentType: paymentType,
                    paymentRegion: paymentRegion,
                    cardHolderType: cardHolderType,
                    mopType: mopType,
                    amountSlab: amountSlab,
                    surchargeText: surchargeText,
                    token: token,
                    "struts.token.name": "token"
                },
                success: function(data) {
                    showAllSurchargeText();

                    $(".surchargeText-edit-section").addClass("d-none");
                    $(".surchargeText-create-section").removeClass("d-none");
                },
                error: function(data) {
                    alert("Something went wrong!");
                }
            });
        }
    });

    $("body").on("click", "#btn-cancel-surchargeText", function(e) {
        e.preventDefault();

        $(".surchargeText-create-section").removeClass("d-none");
        $(".surchargeText-edit-section").addClass("d-none");
        $("#surchargeText").val("");

        $("#paymentMethod, #paymentRegion, #cardHolderType, #mopType, #amount").attr("disabled", false);
    });

    $("#btn-submit-surchargeText").on("click", function(e) {
        e.preventDefault();

        var paymentType = $("#paymentMethod").val(),
            paymentRegion = $("#paymentRegion").val(),
            cardHolderType = $("#cardHolderType").val(),
            mopType = $("#mopType").val(),
            amountSlab = $("#amount").val(),
            surchargeText = $("#surchargeText").val(),
            token = document.getElementsByName("token")[0].value,
            payId = $("#payId").val(),
            businessName = $("#businessName").val();

        if(paymentType !== "") {
            $.ajax({
                type: "POST",
                url: "createEditSurchargeText",
                data: {
                    payId: payId,
                    merchantName: businessName,
                    paymentType: paymentType,
                    paymentRegion: paymentRegion,
                    cardHolderType: cardHolderType,
                    mopType: mopType,
                    amountSlab: amountSlab,
                    surchargeText: surchargeText,
                    token: token,
				    "struts.token.name": "token"
                },
                success: function(data) {                    
                    showAllSurchargeText();
                },
                error: function(data) {
                    alert("Something went wrong!");
                }
            });
        } else {
            alert("Please select payment type");
        }
    });

    $("#btn-edit-surchargeText").on("click", function(e) {
        e.preventDefault();

        var rowId = $(this).closest(".surchargeText-edit-section").attr("data-id"),
            paymentType = $("#" + rowId).find("[data-name='paymentType']").attr("data-value"),
            paymentRegion = $("#" + rowId).find("[data-name='paymentRegion']").attr("data-value"),
            cardHolderType = $("#" + rowId).find("[data-name='cardHolderType']").attr("data-value"),
            mopType = $("#" + rowId).find("[data-name='mopType']").attr("data-value"),
            surchargeText = $("#surchargeText").val(),
            amountSlab = $("#" + rowId).find("[data-name='amountSlab']").attr("data-value"),
            payId = $("#payId").val(),
            token = document.getElementsByName("token")[0].value,
            businessName = $("#businessName").val();

            // console.group("Delete Surcharge");
            // console.log("payid: " + payId);            
            // console.log("merchantName: " + businessName);
            // console.log("paymentType: " + paymentType);
            // console.groupEnd();

            // return false;

        $.ajax({
            type: "POST",
            url: "createEditSurchargeText",
            data: {
                payId: payId,
                merchantName: businessName,
                paymentType: paymentType,
                paymentRegion: paymentRegion,
                cardHolderType: cardHolderType,
                mopType: mopType,
                surchargeText: surchargeText,
                amountSlab: amountSlab,
                token: token,
				"struts.token.name": "token"
            },
            success: function(data) {
                showAllSurchargeText();
            },
            error: function(data) {
                alert("Something went wrong!");
            }
        });
    });

    var isSurchargeActive = $("#surcharge").is(":checked");
    if(isSurchargeActive) {
        $("[data-target='surchargeTextTab']").removeClass("d-none");
    }
});

// $(window).on("load", function() {
    
// });