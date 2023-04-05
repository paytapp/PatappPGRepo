$(document).ready(function(){

    // tab creation 
    $(".lpay-nav-link").on("click", function(e){
        var _this = $(this).attr("data-id");
        $(".lpay-nav-item").removeClass("active");
        $(this).closest(".lpay-nav-item").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+_this+"]").removeClass("d-none");
    })

    $("#consolidateDownload-table").dataTable();

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

    function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
        var _merchant = _this.target.value;
        console.log(_merchant);
        var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
        var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
        if(_merchant != "" && _merchant != "ALL"){
            document.querySelector("body").classList.remove("loader--inactive");
            var data = new FormData();
            data.append('payId', _merchant);
            var _xhr = new XMLHttpRequest();
            _xhr.open('POST', _url, true);
            _xhr.onload = function(){
                if(_xhr.status === 200){
                    var obj = JSON.parse(this.responseText);
                    var  _option = "";
                    console.log(obj);
                    if(_object.isSuperMerchant == true){
                        if(obj.superMerchant == true){
                            document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value="+_selectValue+">"+_selectLabel+"</option>";
                            for(var i = 0; i < obj.subMerchantList.length; i++){
                                _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                            }
                            document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                            document.querySelector("#"+_subMerchantAttr+" option[value='"+_selectValue+"']").selected = true;
                            $("#"+_subMerchantAttr).selectpicker('refresh');
                            $("#"+_subMerchantAttr).selectpicker();
                        }else{
                            document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                            document.querySelector("#"+_subMerchantAttr).value = "";
                        }
                    }
                    if(_object.subUser == true){
                        if(obj.subUserList.length > 0){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value=''>Select Sub-Merchant</option>";
                            for(var i = 0; i < obj.subUserList.length; i++){
                                _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
                            }
                            document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
                            document.querySelector("#"+_subUserAttr+" option[value='']").selected = true;
                            $("#"+_subUserAttr).selectpicker();
                            $("#"+_subUserAttr).selectpicker('refresh');
                        }else{
                            document.querySelector("[data-target="+_subUserAttr+"]").classList.add("d-none");
                            document.querySelector("#"+_subUserAttr).value = "";
                        }
                    }
                    if(_object.glocal == true){
                        if(obj.glocalFlag == true){
                            document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
                            $("[data-id=deliveryStatus] select").selectpicker('val', 'All');
                        }else{
                            document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
                        }
                    }
    
                    if(_object.retailMerchantFlag == true){
                        $("#retailMerchantFlag").val(data.retailMerchantFlag);
                        document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
                    }
                }
            }
            _xhr.send(data);
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 1000);
        }else{
            document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
            document.querySelector("#"+_subMerchantAttr).value = "";
    
        }
    }

    document.querySelector("#merchant").addEventListener("change", function(_this){
        getSubMerchant(_this, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });

    document.querySelector("#merchantDownload").addEventListener("change", function(_this){
        getSubMerchant(_this, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });

    

    function consolidateData(_selector){
        var _input = document.querySelectorAll(_selector);
        var _obj = {};
        var _empty = false;
        _input.forEach(function(index,array,element){
            console.log(index.id);
            _obj[index.getAttribute("data-var")] = index.value;
            if(index.value == "" && index.id != "subMerchant"){
                _empty = true;
            }
        })
        if(_empty == false){
            
            document.querySelector("body").classList.remove("loader--inactive");
            $("#consolidate-table").DataTable({
                "ajax": {
                    "type": "post",
                    "url": "settledConsolidatedDataAction",
                    "data" : _obj,
                },
                "destroy": true,
                "fnDrawCallback" : function(settings, json) {
                    document.querySelector("body").classList.add("loader--inactive");
                    hideColumn();
                },
                "aoColumns" : [
    
                    { "mData":"merchantName" },
                    { "mData":"subMerchantName" },
                    { "mData":"captureFromDate" },
                    { "mData":"captureToDate" },
                    { "mData":"payOutDate" },
                    { "mData":"saleCaptureTxn" },
                    { "mData":"saleCaptureAmnt" },
                    { "mData":"refundCaptureTxn" },
                    { "mData":"refundCaptureAmnt" },
                    { "mData":"saleSettledTxn" },
                    { "mData":"saleSettledAmnt" },
                    { "mData":"refundSettledTxn" },
                    { "mData":"refundSettledAmnt" },
                    { "mData":"chargebackCr" },
                    { "mData":"chargebackDr" },
                    { 
                        "mData":null,
                        "mRender": function(row){
                            return "<span>"+row.otherAdjustmentCr+"</span><div class='otherCr adjustment'><input name='amount' id='amountCr' /></div>"
                        }
                    },
                    { 
                        "mData":null,
                        "mRender": function(row){
                            return "<span>"+row.otherAdjustmentDr+"</span><div class='otherDr adjustment'><input name='amount' id='amountDr' /></div>"
                        }
                    },
                    { "mData":"netSettled" },
                    { 
                        "mData": null,
                        "mRender" : function(row){
                            return "<div class='edit_button-div'><button class='lpay_button lpay_button-md lpay_button-secondary' id='edit-button'>Edit</button></div><div class='save_button-div'><button class='lpay_button lpay_button-md lpay_button-secondary' id='save-btn' style='padding: 6px 10px;'><i class='fa fa-check' aria-hidden='true'></i></button><button class='lpay_button lpay_button-md lpay_button-primary' id='cancel-btn' style='padding: 6px 10px;'><i class='fa fa-times' aria-hidden='true'></i></button></div>";
                        }
                    }
    
                ]
            })
        }else{
            alert("Please select merchant");
        }
    }

    document.querySelector("#consolidate-view").onclick = function(e){
        consolidateData("[data-var]");
    }

    $("#consolidate-table").DataTable();

    function hideColumn(){
        var _userType = document.querySelector("#userType").value;
        var _table = $("#consolidate-table").DataTable();
        if(_userType == "ADMIN" || _userType == "SUBADMIN"){
            _table.columns(18).visible(true);
        }else{
            console.log(_userType);
            _table.columns(18).visible(false);
        }
    }

    function generateFile(_selector){
        var _createObj = {};
        var _getInput = document.querySelectorAll("["+_selector+"]");
        _getInput.forEach(function(index, array, element){
            _createObj[index.getAttribute("data-down")] = index.value;
        })
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "generateNetSettledReport",
            data: _createObj,
            success: function(data){
                $(".lpay_popup-innerbox").attr("data-status", "success");
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text("File is processing. Please see in downloads after some time.");
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        })
    }

    document.querySelector("#consolidateDownload-generate").onclick = function(e){
        generateFile("data-down");
    }

    function consolidateDownloadReport(){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#consolidateDownload-table").DataTable({
            "destroy": true,
            "order" : [["2", "desc"]],
            "ajax" : {
                "url" : "getNetSettledFilesList",
                "type" : "POST",
                "data" : {
                    "createDate" : $("[data-filter='createDate']").val()
                }
            },
            "sAjaxDataProp" : "netSettledDataFile",
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "aoColumns": [
                { "mData": "createdDate", "className" : "createDate"},
                { "mData": "fileName", "className" : "fileName"},
                { "mData" : "status", "className" : "status-consolidate" },
                { 
                   "mData": null,
                   "mRender" : function(row){
                    if(row.status == "Processing"){
                        return "<button class='lpay_button lpay_button-md lpay_button-primary refreshTable-data'><span class='glyphicon glyphicon-repeat'></span>Processing</button>"
                    }else if(row.status == "Failed"){
                        return "";
                    }else{
                        return "<button class='lpay_button lpay_button-md lpay_button-primary consolidate-download'>Download</button>";
                    }
                   }
                },
            ]
        })
    }

    $("body").on("click", ".refreshTable-data", function(){
        $("#consolidateDownload-view").trigger("click");
    })

    consolidateDownloadReport();



    document.querySelector("#consolidateDownload-view").onclick = function(e){
        consolidateDownloadReport();
    }


    // consolidateData("[data-var]");

    $("body").on("click", "#edit-button", function(e){
        $(this).closest("tr").addClass("edit-tr");
    })

    $("body").on("click", "#cancel-btn", function(e){
        $(this).closest("tr").removeClass("edit-tr");
        $(".adjustment").val("");
    })

    $("body").on("click", "#save-btn", function(e){
        var table = new $.fn.dataTable.Api('#consolidate-table');
		var _getClosestTr = $(this).closest("tr");
		var _data = table.rows(_getClosestTr).data();
        var _otherAdjustmentCr = document.querySelector("#amountCr").value;
        var _otherAdjustmentDr = document.querySelector("#amountDr").value;
        if(_otherAdjustmentCr != "" || _otherAdjustmentDr != ""){
            $.ajax({
                type: "POST",
                url: "editAdjustmentAnountAction",
                data: {
                    "payId": _data[0]['payId'],
                    "subMerchantId": _data[0]['subMerchantId'],
                    "payOutDate": _data[0]['payOutDate'],
                    "otherAdjustmentCr": _otherAdjustmentCr,
                    "otherAdjustmentDr": _otherAdjustmentDr
                },
                success: function(data){
                    if(data.response == "success"){
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                    }else{
                        $(".lpay_popup-innerbox").attr("data-status", "error");
                    }
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("tr").removeClass("edit-tr");
                    $(".adjustment").val("");
                }
            })
        }else{
            alert("Other adjustment(Cr) or other adjustment(Dr) should not be blank");
        }
    })

    $(".confirmButton").on("click", function(e){
        // consolidateData("[data-var]");
        $("#consolidate-table").DataTable();

        $(".lpay_popup").fadeOut();
    })

    

    $("body").on("click", ".consolidate-download", function(e){
        var _fileName = $(this).closest("tr").find(".fileName").text();
        var _createDate = $(this).closest("tr").find(".createDate").text();
        $("#payoutDate").val(_createDate);
        $("#fileNameTable").val(_fileName);
        $("#consolidateDownloadForm").submit();

    })

})