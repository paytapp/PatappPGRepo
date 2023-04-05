function removeError(_that) {
    _that.closest(".lpay_select_group").classList.remove("hasError");
}

$(document).ready(function(e) {
    
    // console.log(_buttonClicked);
    // return false;
    $(".lpay-nav-link").on("click", function(e) {
        e.preventDefault();

        var _this = $(this),
            dataId = _this.attr("data-id"),
            dataAction = _this.attr("data-action");

        $(".lpay-nav-item").removeClass("active");
        _this.closest(".lpay-nav-item").addClass("active");

        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+ dataId +"]").removeClass("d-none");

        $("body").removeClass("loader--inactive");
        
        fetchTableData("#" + dataId + "-table", dataAction);
    });

    

    var generateFile = function(payload) {
        var _url = "";
        if(_buttonClicked == "Generate TXT File"){
            _url = "generateB60MergedFile"
        }else{
            _url = "generateB60File"
        }
        $.ajax({
            type: "post",
            url: _url,
            data: payload,
            success: function(data) {
                $(".lpay_popup-innerbox").attr("data-status", "success");
                $(".lpay_popup").fadeIn();                    

                if(data.status == "READY") {
                    $(".responseMsg").text("File already exists. Please see in downloads. Do you want to create new file again?");
                    $(".lpay_popup-innerbox").attr("data-type", data.status);
                    $(".confirm-button").text("Yes");
                } else if(data.status == "PROCESSING") {
                    $(".responseMsg").text("File is processing. Please see in downloads after some time.");
                    $(".lpay_popup-innerbox").attr("data-type", data.status);
                    $(".confirm-button").text("Ok");
                } else {
                    $(".responseMsg").text("File is generating. Please see in downloads after some time.");
                    $(".lpay_popup-innerbox").attr("data-type", "GENERATING");
                    $(".confirm-button").text("Ok");
                }

                setTimeout(() => {
                    $("body").addClass("loader--inactive");
                }, 1000);
            },
            error: function() {
                $(".lpay_popup-innerbox").attr("data-status", "error");
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text("Something went wrong! Try again.");
            }
        });
    }

    var ccCodeValidate = function(payload) {
        if(payload.hasOwnProperty("ccCode")) {
            var ccCodeInput = $('[data-var="ccCode"]');
            if(ccCodeInput.val() == "") {
                ccCodeInput.closest(".lpay_select_group").addClass("hasError");

                return false;
            }
        }

        return true;
    }

    var generatePayload = function(_selector, newFileFlag) {
        var $input = _selector.find(".data-input"),
            payload = {};

        $input.each(function(ele) {
            if($(this).prop("nodeName") !== "DIV") {
                payload[$(this).attr("data-var")] = $(this).val();
            }
        });

        if(ccCodeValidate(payload)) {
            if(validateDate(payload.fromDate, payload.toDate)) {
                $("body").removeClass("loader--inactive");

                if(newFileFlag !== undefined) {
                    payload.newfile = newFileFlag;
                }

                generateFile(payload);
            }
        }
    }

    var _buttonClicked = "";
    $("[data-id='generate-btn'], [data-id='generate-btn-txt']").on("click", function(e) {
        e.preventDefault();
        var parentContainer = $(this).closest(".lpay_tabs_content");
        _buttonClicked = $(this).text();
        generatePayload(parentContainer);
    });

    $(".confirm-button").on("click", function(e) {
        e.preventDefault();

        var dataType = $(this).closest(".lpay_popup-innerbox").attr("data-type"),
            activeLinkId = $(".lpay-nav-item.active > a").attr("data-id");
            
        if(dataType == "READY") {
            generatePayload($('[data-target="'+ activeLinkId +'"]'), "Y");
        }

        var $innerText = $(this).text();
        if($innerText == "Ok") {
            location.reload(true);
        }

        $(".lpay_popup").fadeOut();
    });

    var fetchTableData = function(_selector, actionName) {
        var tableDataObj = [
            {
                "mData": "fromdate",
                "className": "text-center table-input"
            },
            {
                "mData": "todate",
                "className": "text-center table-input"
            },
            {
                "mData": "createdate",
                "className": "text-center"
            },
            {
                "mData": "filename",
                "className": "text-center table-input"
            },
            { 
                "mData": null,
                "className": "text-center",
                "mRender" : function(row) {
                    return "<button class='download-file lpay_button lpay_button-md lpay_button-primary'>Download</button>";
                }
            }
        ];

        var cellData = ["fromDate", "toDate", "createdate", "filename"];

        if(actionName == "gettingB60FileDataWithCcCode") {
            tableDataObj.unshift({
                "mData" : "ccCode",
                "className" : "text-center table-input"
            });

            cellData.unshift("ccCode");
        }

        $(_selector).dataTable( {
            "destroy": true,
            "order" : [["2", "desc"]],
            "ajax" : {
                "url" : actionName,
                "type" : "POST",
            },
            "aoColumns": tableDataObj,
            "createdRow": function(row, data, rowIndex) {
                $.each($('td', row), function (colIndex) {
                    $(this).attr('data-title', cellData[colIndex]);
                });
            },
            "initComplete": function() {
                setTimeout(function() {
                    $("body").addClass("loader--inactive");                    
                }, 1000);
            }
        });
    }

    fetchTableData('#b60Downlaod-table', "gettingB60FileData");

    var validateDate = function(dateFrom, dateTo) {
        var transFrom = $.datepicker.parseDate('dd-mm-yy', dateFrom),
            transTo = $.datepicker.parseDate('dd-mm-yy', dateTo);

        if (transFrom > transTo) {
            alert('From date must be less than to date');
            return false;
        }

        if(transTo - transFrom > 6 * 86400000){
            alert("Date should not be greater then 7 days");
            return false;
        }

        return true;
    }

    $(".cancel-button").on("click", function(e) {
        e.preventDefault();

        $(".lpay_popup").fadeOut();
    });

    $("body").on("click", ".download-file", function(e) {
        e.preventDefault();
        var _tableId = $(this).closest("table").attr("id");
        if(_tableId == "b60DownloadTextFile-table"){
            var _form = $("#txtFile");
        }else{
            var _form = $("#downloadB60File-form");
        }
            var tableInput = $(this).closest("tr").find(".table-input"),
            formInput = "";

        _form.html();

        tableInput.each(function() {
            var paramName = $(this).attr("data-title");

            formInput += '<input type="hidden" name="'+ paramName +'" value="'+ $(this).text() +'">';
        });

        _form.html(formInput);
        
        _form.submit();
    });

    var today = new Date();
    $(".datepick").attr("readonly", true);
    $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
    $(".datepick").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date(),
        changeMonth: true,
        changeYear: true
    });
});
