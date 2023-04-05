
var parameterObj = {
    transactionType	:	"transactionType",
    status			:	"status",
    paymentsRegion	:	"paymentsRegion",
    acquirer		:	"acquirer",
    currency		:	"currency"
}

// var updateSubmerchant = function() {
//     $('[data-id="subMerchant"]').prepend('<option value="ALL" selected="selected">ALL</option>');
//     $('[data-id="subMerchant"]').selectpicker("refresh");
// }

$(document).ready(function() {
    // $("#merchantPayId").on("change", function(e) {
    //     var _merchant = $(this).val();
    //     if(_merchant != "") {
    //         $("body").removeClass("loader--inactive");
    //         $.ajax({
    //             type: "POST",
    //             url: "getSubMerchantListByPayId",
    //             data: {"payId": _merchant},
    //             success: function(data) {						
    //                 $("#subMerchant").html("");
    //                 if(data.superMerchant == true){
    //                     var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
    //                     for(var i = 0; i < data.subMerchantList.length; i++) {
    //                         _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
    //                     }

    //                     $("[data-id='submerchant']").removeClass("d-none");
    //                     $("#subMerchant option[value='']").attr("selected", "selected");
    //                     $("#subMerchant").selectpicker('refresh');

    //                     setTimeout(function(e){
    //                         $("body").addClass("loader--inactive");
    //                     },500);
    //                 } else {
    //                     setTimeout(function(e){
    //                         $("body").addClass("loader--inactive");
    //                     },500);

    //                     $("[data-id='submerchant']").addClass("d-none");
    //                     $("#subMerchant").val("");
    //                 }
    //             }
    //         });
    //     } else {
    //         $("[data-id='submerchant']").addClass("d-none");
    //         $("#subMerchant").val("");	
    //     }
    // });
        

    var today = new Date();
    $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
    $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));	
    $("#dateFrom").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date()
    });
    $("#dateTo").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date()
    });

    function handleChange() {
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
        if (transFrom == null || transTo == null) {
            alert('Enter date value');
            return false;
        }
    
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $('#dateFrom').focus();
            return false;
        }
        if (transTo - transFrom > 61 * 86400000) {
            alert('No. of days can not be more than 60 days');
            $('#dateFrom').focus();
            return false;
        }
    }

    $("#btn-download-report").on("click", function(e) {

        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());

        if (transTo - transFrom > 61 * 86400000) {
            alert('No. of days can not be more than 60 days');
            $('#dateFrom').focus();
            return false;
        }

        e.preventDefault();
        var _text = $(this).text();
        var _checkedFlag = $("#getLatest").is(":checked");
        $("body").removeClass("loader--inactive");
        if(_text == "Download"){
            for(var key in parameterObj) {
                var _element = $("#" + parameterObj[key]);
                if(_element.val() == "" || _element.val() == null) {
                    $("#input-" + parameterObj[key]).val("ALL");
                } else if(key == "status" || key == "acquirer") {
                    var _val = $("#" + parameterObj[key]).val().join();
                    $("#input-" + parameterObj[key]).val(_val);
                } else {
                    $("#input-" + parameterObj[key]).val($("#" + parameterObj[key]).val());
                }
            }
            $("#input-getLatest").val(_checkedFlag);
            $("#downloadPaymentsReportAction").submit();
            $("body").addClass("loader--inactive");
        }else{
            var _obj = {};
            var _input = document.querySelectorAll("[data-download]");
            _input.forEach(function(index,element,array){
                _obj[index.getAttribute("data-download")] = index.value;
            })
            _obj['acquirer'] = $("[data-download='acquirer']").val().toString();
            _obj['status'] = $("[data-download='status']").val().toString();
            _obj['searchFlag'] = _checkedFlag;
            // _obj['searchFlag'] = $("#input-getLatest").val($("#getLatest").is(":checked"));
            $.ajax({
                type: "POST",
                url: "generateSearchReportFileAction",
                data: _obj,
                success: function(data){
                    setTimeout(function(e){
                        console.log(data.generateReport);
                        document.querySelector("body").classList.add("loader--inactive");
                        if(data.generateReport == true){
                            document.querySelector(".lp-success_generate").closest(".col-md-12").classList.remove("d-none");
                        }else{
                            document.querySelector(".lp-error_generate").closest(".col-md-12").classList.remove("d-none");
                        }
                    }, 500)
                    setTimeout(function(e){
                        removeError()
                    }, 4000);
                }
            })
        }
    });

    function removeError(){
        document.querySelector(".lp-error_generate").closest(".col-md-12").classList.add("d-none");
        document.querySelector(".lp-success_generate").closest(".col-md-12").classList.add("d-none");
    }

    $(".lpay_toggle").on("change", function(e) {
        var _getChecked = $(this).find("input[type=checkbox]").is(":checked");
        var _label = $(this).closest("label");

        if(_getChecked) {
            _label.addClass("lpay_toggle_on");
            getLatestDataHandler(true);
        } else {
            _label.removeClass("lpay_toggle_on");
            getLatestDataHandler(false);
        }
    });

    var getLatestDataHandler = function(status) {
        for(var key in parameterObj) {
            var _element = document.getElementById(parameterObj[key]);

            if(_element !== null) {
                if(_element.tagName == "SELECT") {
                    $("#" + parameterObj[key]).selectpicker("val", "");
                } else {
                    _element.value = "";
                }
                $("#" + parameterObj[key]).prop("disabled", status);
                $("#" + parameterObj[key]).selectpicker("refresh");
            }
        }
    }

    getLatestDataHandler(true);
});

function dateBaseDownload(){
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transTo - transFrom > 30 * 86400000) {
        document.querySelector("#btn-download-report").innerText = "Generate";
    }else{
        document.querySelector("#btn-download-report").innerText = "Download";
    }
}

// $(window).on("load", function() {
//    updateSubmerchant();
// });

// var getLatestDataHandler = function() {
//     var count = 0;

//     var objLength = Object.keys(parameterObj).length;		

//     for(var key in parameterObj) {
//         var _element = document.getElementById(parameterObj[key]);
        
//         if(_element.value == "") {
//             count++;
//         } else {
//             $("#getLatest").selectpicker('val', 'false');
//             $("#getLatest").prop('disabled', true);
//             $("#getLatest").selectpicker('refresh');
//             return false;
//         }
//     }

//     if(count == objLength) {
//         $("#getLatest").prop('disabled', false);
//         $("#getLatest").selectpicker('refresh');
//     }
// }