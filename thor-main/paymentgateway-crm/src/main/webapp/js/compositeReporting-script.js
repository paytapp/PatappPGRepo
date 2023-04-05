   

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
        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
        document.querySelector("#"+_subMerchantAttr).value = "";

    }
}




$(document).ready(function(e){

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
    
    function createJsonData(){
        var _obj = {};
        var _allInput = document.querySelectorAll("[data-var]");
        _allInput.forEach(function(index, array, element){
            var _getAttr = index.attributes['data-var'].nodeValue;
            var _closest = index.closest(".d-none");
            if(_closest == null || index.value != ""){
                _obj[_getAttr] = index.value;

                console.log(index.value);
            }
        })
        return _obj;
    }

    var json =  [
        { payId: "189039240462", merchantName: "Amitosh Aanand", txnId: 462100039700, txnRef:  9878300392800, subMerchantName: "Rajit Sharma", orderId : "LP12884948989", txnDate: "03-09-2021 12:43:00", mobileNumber: "9717478851", channel: "IMPS", RRN : "LZ940HU4345", bankAccountName: "Mohd Zakaullah", bankAccountNumber: "100039700462", bankIfsc: "INDB0000169", amount: "200.00", mode: "UPI", responseMsg: "Your transaction has been completed", status: "Timeout"},
        { payId: "100039700462", merchantName: "Mohd Zakaullah", txnId: 462100039700, txnRef:  9878300392800, subMerchantName: "Rajit Sharma", orderId : "LP12884948989", txnDate: "03-09-2021 12:43:00", mobileNumber: "9717478851", channel: "IMPS", RRN : "LZ940HU4345", bankAccountName: "Mohd Zakaullah", bankAccountNumber: "100039700462", bankIfsc: "INDB0000169", amount: "200.00", mode: "UPI", responseMsg: "Your transaction has been completed", status: "Pending"},
        { payId: "100039700462", merchantName: "Dhanpat", txnId: 462100039700, txnRef:  9878300392800, subMerchantName: "Rajit Sharma", orderId : "LP12884948989", txnDate: "03-09-2021 12:43:00", mobileNumber: "9717478851", channel: "IMPS", RRN : "LZ940HU4345", bankAccountName: "Mohd Zakaullah", bankAccountNumber: "100039700462", bankIfsc: "INDB0000169", amount: "200.00", mode: "UPI", responseMsg: "Your transaction has been completed", status: "Captured"},
    ];

    function viewData(){
        var _obj = createJsonData();
        console.log(_obj);
        $('#compositeReportTabel').dataTable( {
            "destroy": true,
            "ajax" : {

                "url" : "merchantDirectReportData",
                "type" : "POST",
                "data" : _obj,
            },
            "aoColumns": [
                { "mData": "merchantPayId" },
                { "mData": "merchant" },
                { "mData": "txnId" },
                { "mData": "orderId"},
                { "mData": "status"},
                { 
                    "mData": null,
                    "mRender" : function(row) {
                        if(row.status == "Timeout") {
                            return "<button class='lpay_button lpay_button-md lpay_button-primary payNow'>Re-Initiate</button>";
                        }else if (row.status == "Captured"){
                            return "NA";
                        }else{
                            return "<button class='lpay_button lpay_button-md lpay_button-primary payNow'>Get Status</button>";
                        }
    
                    }
                }
            ]
        });
    }

    viewData();

    document.querySelector("#view").onclick = viewData;
    document.querySelector("#download").onclick = function(){
        var _obj = createJsonData();
        var _option = "";
        for(key in _obj){
            _option += "<input type='hidden' name='"+key+"' value='"+_obj[key]+"' />"
        }
        document.querySelector("#downloadComposite").innerHTML = _option;
        document.querySelector("#downloadComposite").submit();
    }
      
    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        console.log(e.target.localName);
        _check = _currentTable;
        if(e.target.localName != "button" && e.target.localName != "input" && e.target.localName != "label"){
            var tr = $(this).closest('tr');
            var row = table.row(tr);
            if ( row.child.isShown() ) {
                tr.removeClass('shown');
                setTimeout(function(e){
                    row.child()[0].children[0].classList.remove("active-row");
                    row.child.hide();
                }, 600)
            }
            else {
                row.child( dataTableMoreData(row.data())).show();
                row.child()[0].children[0].classList.add("active-row");
                getAllData();
                tr.addClass('shown');
            }
        }
        
    })

    function getAllData(){
        var _new = document.querySelectorAll(".inner-div");
        _new.forEach(function(index, array, element){
            var _getValue = _new[array].children[1].innerText;
            if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
                _new[array].classList.add("d-none");
            }
        })
    }
    

    

    
    function dataTableMoreData ( d ) {
        var _mainDiv = "<div class='main-div'>";
        var _obj = {
            "subMerchant" : "Sub Merchant Name",
            "impsRefNum": "TXN REF Number",
            "date" : "Transaction Date",
            "userType": "Channel",
            "rrn": "RRN Number",
            "txnType": "Mode",
            "phoneNo": "Mobile Number",
            "bankAccountNumber": "Bank Account Number",
            "bankAccountName": "Bank Account Name/ Payee Name",
            "bankIfsc": "Bank IFSC",
            "amount": "Amount",
            "responseMsg": "Response Message"
        } 
        for(key in _obj){
            if(_obj[key].hasOwnProperty("className")){
                var _getKey = Object.keys(_obj[key]);
                _mainDiv += '<div class="inner-div '+_obj[key]["className"]+'">'+
                        '<span>'+_obj[key][_getKey[0]]+'</span>'+
                        '<span>'+d[_getKey[0]]+'</span>'+
                    '</div>'
            }else{
                _mainDiv += '<div class="inner-div">'+
                    '<span>'+_obj[key]+'</span>'+
                    '<span>'+d[key]+'</span>'+
                '</div>'
            }
        }
        _mainDiv += "</div>";
        return _mainDiv;
            
    }

})