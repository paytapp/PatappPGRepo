   

function getSubMerchant(_this, _url, _object){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
    if(_merchant != ""){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                console.log(obj);
                var  _option = "";
                if(_object.isSuperMerchant == true){
                    if(obj.superMerchant == true){
                        document.querySelector("[data-id='isSubMerchant']").value = "true";
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
                        document.querySelector("[data-id='isSubMerchant']").value = "false";
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
                }
                if(_object.subUser == true){
                    if(obj.subUserList.length > 0){
                        _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subUserList.length; i++){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
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
        document.querySelector("[data-id='isSubMerchant']").value = "false";
        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
        document.querySelector("#"+_subMerchantAttr).value = "";

    }
}

// document.querySelector("#merchant").addEventListener("change", function(e){
//     getSubMerchant(e, "getSubMerchantList", {
//         isSuperMerchant : true,
//         subUser : true
//     });
// });

// document.querySelector("#subMerchant").addEventListener("change", function(e){
//     getSubMerchant(e, "vendorTypeSubUserListAction", {
//         subUser : true
//     });
// })
	



$(document).ready(function(e){

    function downloadVirtualDetails(){
        document.querySelector("body").classList.remove("loader--inactive");
        var _obj = {
            "payId" : document.querySelector("#merchant").value,
            "subMerchantId" : document.querySelector("#subMerchant").value
        }
        var _option = "<input type='hidden' name='payId' value='"+_obj["payId"]+"' />";
        _option += "<input type='hidden' name='subMerchantId' value='"+_obj["subMerchantId"]+"' />";
        document.querySelector("#virtualDetailsDownloadAction").innerHTML = _option;
        document.querySelector("#virtualDetailsDownloadAction").submit();
        setTimeout(function(e){
            document.querySelector("body").classList.add("loader--inactive");
        }, 500);
    }

    document.querySelector("#virtualDetail-download").onclick = downloadVirtualDetails;
    // console.log(_obj);

    function hideColumn(){
        var _table = $("#virtualDetailsTable").DataTable();
        var _isSubMerchant = document.querySelector("[data-id='isSubMerchant']").value;
        if(_isSubMerchant == "true"){
            _table.columns(1).visible(true)
        }else{
            _table.columns(1).visible(false)
        }
        document.querySelector("body").classList.add("loader--inactive");
    }

    function virtualAccountDetails(){
        document.querySelector("body").classList.remove("loader--inactive");
        var _obj = {
            "payId" : document.querySelector("#merchant").value,
            "subMerchantId" : document.querySelector("#subMerchant").value
        }
        $("#virtualDetailsTable").dataTable({
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "columnDefs": [
                { "width": "20%", "targets": 0 }
            ],
            "destroy": true,
            "ajax" : {

                "url" : "viewVirtualDetailsAction",
                "type" : "POST",
                "data" : _obj,
            },
            "initComplete" : function(setting, json){
                hideColumn();
            },
            "aoColumns": [

                { "data": "merchantName" },
                { "data": "subMerchantName" },
                { "data": "virtualAccountNo"},
                { "data": "virtualIfscCode"},
                { "data": "virtualBeneficiaryName" },
                { "data": "createDate" }
            ]
        })
    }
    virtualAccountDetails();
    document.querySelector("#virtualDetail-submit").onclick = virtualAccountDetails;
})

