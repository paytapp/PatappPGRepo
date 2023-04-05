$(document).ready(function(e){

    function tabShow(_selector){
        var _allTabsContent = document.querySelectorAll(".lpay_tabs_content");
        var _activeTab = document.querySelector(_selector).getAttribute("data-id");
        _allTabsContent.forEach(function(index, array, element){
            index.classList.add("d-none");
        })
        _allTabs.forEach(function(index, array, element){
            index.closest("li").classList.remove("active");
        })
        document.querySelector(_selector).closest("li").classList.add("active");
        document.querySelector('[data-target='+_activeTab+']').classList.remove("d-none");
    }

    var _allTabs = document.querySelectorAll(".lpay-nav-link");
    _allTabs.forEach(function(index, array, element){
        index.addEventListener('click', function(e){
            tabShow('[data-id="'+e.target.getAttribute("data-id")+'"]');
        })
    })
    
    $('#example').DataTable( {
        dom: 'B',
        buttons: [
			{
				extend: 'csv',
				text: 'Download CSV Format',
                title: "Chargeback Utility",
				className: 'lpay_button lpay_button-md lpay_button-primary',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
    });

    $('#chargebackClosure').DataTable( {
        dom: 'B',
        buttons: [
			{
				extend: 'csv',
				text: 'Download CSV Format',
                title: "Chargeback Closure",
				className: 'lpay_button lpay_button-md lpay_button-primary',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
    });

    $(".dt-buttons").addClass("d-none");

    $("#utilityType").on("change", function(e){
        var _val = $(this).val();
        $(this).closest("div").removeClass("error-utility");
        $(".dt-buttons").addClass("d-none");
        if(_val == 'chargebackRefund' || _val == 'chargebackCreation'){
            $("#example_wrapper .dt-buttons").removeClass("d-none");
        }else if(_val == 'chargebackClosure'){
            $("#chargebackClosure_wrapper .dt-buttons").removeClass("d-none"); 
        }else{
            $(".dt-buttons").addClass("d-none");
        }
    })


    $(".lpay_upload_input").on("change", function(e){
        var _val = $(this).val();
        var _fileSize = $(this)[0].files[0].size;
        var _fileType = $(this)[0].files[0].type;
        var _tmpName = _val.replace("C:\\fakepath\\", "");
        if(_val != ""){
            $("body").removeClass("loader--inactive");
            $(".default-upload").addClass("d-none");
            $("#placeholder_img").css({"display":"none"});
            if(_fileType == "application/vnd.ms-excel"){
                if(_fileSize < 2000000){
                    $(this).closest("label").attr("data-status", "success-status");
                    $("#fileName").text(_tmpName);
                    $("#bulkUpdateSubmit").attr("disabled", false);
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    }, 500);
                }else{
                    $(this).closest("label").attr("data-status", "error-status");
                    $("#bulkUpdateSubmit").attr("disabled", true);
                    $(".fileTypeError").text("File size too long");
                    $(".fileTypeError").fadeIn();
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    }, 500);
                }
            }else{
                $(this).closest("label").attr("data-status", "error-status");
                $("#bulkUpdateSubmit").attr("disabled", true);
                $(".fileTypeError").text("Invalid File Type");
                $(".fileTypeError").fadeIn();
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        }
    });

    document.querySelector("#bulkUpdateSubmit").onclick = function(e){
        var _utility = document.querySelector("#utilityType");
        if(_utility.value == ''){
            _utility.closest("div").classList.add('error-utility');
            e.preventDefault();
        }else{
            setTimeout(function(e){
                document.querySelector(".default-upload").classList.remove("d-none");
                document.querySelector("#placeholder_img").style.display = "block";
                $(".lpay_upload_input").val("");
                $(".lpay-upload").removeAttr("data-status");
            }, 1500)
        }
    }

    var _chargebackReporting = [
        { "mData" : "totalCount", "width" : "8%" },
        { "mData" : "totalValidEntryCount", "width" : "8%" },
        { "mData" : "totalInvalidEntryCount", "width" : "8%" },
        { "mData" : "totalCapture", "width" : "8%" },
        { "mData" : "totalRejected", "width" : "8%" },
        { "mData" : "totalDeclined", "width" : "8%" },
        { "mData" : "totalError", "width" : "8%"},
        { "mData" : "totalDenied", "width" : "8%"},
        { "mData" : "totalFailed", "width" : "8%"},
        { "mData" : "totalInvalid", "width" : "8%"},
        { "mData" : "totalAuthenticationFailed", "width" : "8%"},
        { "mData" : "totalAcquirerDown", "width" : "8%"},
        { "mData" : "totalFailedAtAcquirer", "width" : "15%"},
        { "mData" : "totalAcquirerTimeOut", "width" : "15%"},
        { "mData" : "createDate", "width" : "12%"},
        { 
            "mData" : null,
            "width" : "10%",
            "mRender" : function(row){
                return "<button class='lpay_button lpay_button-md lpay_button-primary download-file'>Download</button>"
            }
        }
    ];


    var _chargebackCreation = [
        { "mData" : "totalCount" },
        { "mData" : "totalValidEntryCount" },
        { "mData" : "totalInvalidEntryCount" },
        { "mData" : "totalCreated" },
        { "mData" : "totalException" },
        { 
            "mData" : null,
            "width" : "10%",
            "mRender" : function(row){
                return "<button class='lpay_button lpay_button-md lpay_button-primary download-file'>Download</button>"
            }
        }
    ];

    var _chargebackClosure = [
        { "mData" : "totalCount" },
        { "mData" : "totalValidEntryCount" },
        { "mData" : "totalInvalidEntryCount" },
        { "mData" : "totalClosed" },
        { "mData" : "totalException" },
        { 
            "mData" : null,
            "width" : "10%",
            "mRender" : function(row){
                return "<button class='lpay_button lpay_button-md lpay_button-primary download-file'>Download</button>"
            }
        }
    ];

    function refundReport(_url, _selector, _data, _tableId, _aaData){
        document.querySelector("body").classList.remove("loader--inactive");
        
        
        $(_tableId).dataTable({
            "ajax" : {
                "type" : "POST",
                "url" : _url,
                "data" : function(d) {
                    return generatePostData(d, _selector);
                }
            },
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500)
            },
            "ordering" : false,
            "destroy": true,
            "serverSide" : true,
            "sAjaxDataProp" : _aaData,
            "aoColumns" : _data
        })
    }


    var today = new Date();
    $(".date-refund_data").attr("readonly", true);
    $('.date-refund_data').val($.datepicker.formatDate('dd-mm-yy', today));
    $(".date-refund_data").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date(),
        changeMonth: true,
        changeYear: true
    });

    $("body").on("click", ".download-file", function(e){
        var _getTableId = $(this).closest("table").attr("id");
        var _table = new $.fn.dataTable.Api("#"+_getTableId);
        var _action = "";
        if(_getTableId == "chargebackCreation-table"){
            _action = "downloadChargebackCreationReportAction";
        }else if(_getTableId == "chargebackClosure-table"){
            _action = "downloadChargebackClosureReportAction";
        }else if(_getTableId == "refundReporting-table"){
            _action = "downloadChargebackRefundReportAction";
        }
		var _closestTr = $(this).closest("tr");
		var _data = _table.rows(_closestTr).data();
        document.querySelector("#lp-refund_filename").value = _data[0]['fileName'];
        document.querySelector("#lp-download_refund").setAttribute("action", _action);
        document.querySelector("#lp-download_refund").submit();
    })
    
    
    function generatePostData(d, _selector) {

        var obj = {};
        obj['dateTo'] = document.querySelector(_selector + " [data-var='dateTo']").value;
        obj['dateFrom'] = document.querySelector(_selector + " [data-var='dateFrom']").value;
    
        obj.token = document.getElementsByName("token")[0].value;
        obj.draw = d.draw;
        obj.length = d.length;
        obj.start = d.start;
        obj["struts.token.name"] = "token";

    
        return obj;
    }

   

    $("#view-chargebackReport").on("click", function(e){
        refundReport(
            "fetchChargebackRefundUtilAction",
            '[data-target="chargebackReporting"]', 
            _chargebackReporting, 
            "#refundReporting-table",
            "aaData"
        );
    })

    $("#view-chargebackCreation").on("click", function(e){
        refundReport(
            "fetchChargebackCreationReportAction", 
            '[data-target="chargebackCreation"]', 
            _chargebackCreation, 
            "#chargebackCreation-table",
            "creationData"
        );
    })

    $("#view-chargebackClosure").on("click", function(e){
        refundReport(
            "fetchChargebackClosureReportAction", 
            '[data-target="chargebackClosure"]', 
            _chargebackClosure, 
            "#chargebackClosure-table",
            "closerData"
        );
    })

    $(".confirmButton").on("click", function(e){
        loadAllReportFunction();
        $(".lpay_popup").fadeOut();
    })

    function loadAllReportFunction(){
        refundReport(
            "fetchChargebackRefundUtilAction", 
            '[data-target="chargebackReporting"]', 
            _chargebackReporting, 
            "#refundReporting-table",
            "aaData"
        );
        refundReport(
            "fetchChargebackCreationReportAction", 
            '[data-target="chargebackCreation"]', 
            _chargebackCreation, 
            "#chargebackCreation-table",
            "creationData"
        );
        refundReport(
            "fetchChargebackClosureReportAction", 
            '[data-target="chargebackClosure"]', 
            _chargebackClosure, 
            "#chargebackClosure-table",
            "closerData"
        );
    }

    loadAllReportFunction()

})
