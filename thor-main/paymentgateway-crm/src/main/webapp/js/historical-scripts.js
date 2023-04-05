
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

function getSubMerchant(_this, _url, _object){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    if(_merchant != "" && _merchant != "ALL"){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText),
                    _option = "";

                if(_object.isSuperMerchant == true) {
                    if(obj.superMerchant == true) {
                        document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }

                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subMerchantAttr+" option[value='ALL']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker('refresh');
                        $("#"+_subMerchantAttr).selectpicker();
                    }else{
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
                }
                if(_object.subUser == true){
                    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
                    if(obj.subUserList.length > 0){
                        _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subUserList.length; i++){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["payId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subUserAttr+" option[value='ALL']").selected = true;
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

document.querySelector("#merchant").addEventListener("change", function(e){
    getSubMerchant(e, "getSubMerchantList", {
        isSuperMerchant : true,
        subUser : false,
        retailMerchantFlag: false,
        glocal : false,
        listKey: 'payId'
    });
});



document.querySelector("#subMerchant").addEventListener("change", function(e){
    // getSubMerchant(e, "vendorTypeSubUserListAction", {
    //     subUser : false
    // });
})



$(document).ready(function(E){

    function createJson (d){
        var _inputs = document.querySelectorAll("[data-var]");
        var _obj = {};
        _inputs.forEach(function(index, array, element){
            if(index.vlaue != "" && index.closest(".d-none") == null )
            _obj[index.getAttribute("data-var")] = index.value;
        });

        console.log(_obj);
        
        _obj.draw = d.draw;
        _obj.length = d.length;
        _obj.start = d.start;
        return _obj;
    }

    function historicalDataLoad(){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#historical_table").dataTable({
            "destroy": true,
            "serverSide" : true,
            "processing": true,
    
            "ajax" : {
    
                "url" : "fetchGeneratedFilesListAction",
                "type" : "POST",
                "data" : function(d){
                    return createJson(d)
                },
            },
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "sAjaxDataProp" : "reportFileDataList",
            "aoColumns": [
                { "mData": "dateFrom", "className" : "dateFrom-download" },
                { "mData": "dateTo", "className" : "dateTo-download" },
                { "mData": "createDate", "className" : "createDate-download" },
                { "mData": "reportTypeName", "className" : "reportTypeName-download" },
                { "mData": "reportFileName", "className" : "reportFileName-download" },
                {
                    "mData" : null,
                    "mRender" : function(row){
                        return "<button class='lpay_button lpay_button-md lpay_button-primary historical_report_table'>Download</button>"
                    }
                }
            ]
        })
    }
    
    historicalDataLoad();

    document.querySelector("#submit_data").onclick = function(e){
        historicalDataLoad();
    }

    $("body").on("click", ".historical_report_table", function(e){
        var _par = $(this).closest("tr");
        $("#dateFrom-download").val(_par.find(".dateFrom-download").text());
        $("#dateTo-download").val(_par.find(".dateTo-download").text());
        $("#createDate-download").val(_par.find(".createDate-download").text());
        $("#reportTypeName-download").val(_par.find(".reportTypeName-download").text());
        $("#reportFileName-download").val(_par.find(".reportFileName-download").text());
        
        $("#downlodGeneratedReportFileAction").submit();
    })

})