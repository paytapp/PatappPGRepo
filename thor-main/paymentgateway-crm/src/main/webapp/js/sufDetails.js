function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    var _sufType = document.querySelector("#sufSetType").value;
    if(_merchant != "" && _merchant != "ALL" && _sufType != 'bulk'){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                var  _option = "";
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



document.querySelector("#payId").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

document.querySelector("#filter-payId").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
});

$(document).ready(function() {
    var _getAllSelect = document.querySelector("#wwctrl_payId select").children;

    // DOMESTIC PAYMENT TYPES
    var domesticObj = {
        "Credit Card" :   "CC",
        "Debit Card" : "DC",
        "Net Banking" : "NB",
        "Wallet" : "WL",
        "EMI CC" : "EMCC",
        "EMI DC" : "EMDC",
        "UPI" : "UP",
        "COD": "CD",
        "NEFT" : "NEFT",
        "IMPS" : "IMPS",
        "RTGS" : "RTGS"
    };

    $("#sufSetType").on("change", function(e){
        var _value = $(this).val();

        if(_value == ''){
            $("#payId").closest(".col-md-3").addClass("d-none");
        }else{
            $("#payId").closest(".col-md-3").removeClass("d-none");
        }

        $("[data-target='subMerchant']").addClass("d-none");
        $("#subMerchant").val('default');
        $("#subMerchant").selectpicker('refresh');
        document.querySelector("#wwctrl_payId").innerHTML = '';
        
        if(_value != 'bulk'){
            var _select = "<select id='payId' data-submerchant='subMerchant' data-live-search='true'><option value=''>Select Merchant</option></select>";
        }else{ 
            var _select = "<select id='payId' title='ALL' multiple data-submerchant='subMerchant' data-live-search='true'></select>";
        }
        document.querySelector("#wwctrl_payId").innerHTML = _select;
        for(key in _getAllSelect){
            if(_getAllSelect[key].innerText != undefined){
                var _option = "<option value='"+_getAllSelect[key].value+"'>"+_getAllSelect[key].innerText+"</option>";
                document.querySelector("#payId").innerHTML += _option;
            }
        }
        $("#payId").selectpicker();
   })

   function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    var _sufType = document.querySelector("#sufSetType").value;
    if(_merchant != "" && _merchant != "ALL" && _sufType != 'bulk'){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                var  _option = "";
                if(_object.isSuperMerchant == true){
                    if(obj.superMerchant == true){
                        document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                        // _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value="+_selectValue+">"+_selectLabel+"</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        // document.querySelector("#"+_subMerchantAttr+" option[value='"+_selectValue+"']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker('refresh');
                        $("#"+_subMerchantAttr).selectpicker();
                    }else{
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
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

   $("body").on("change", "#payId", function(e){
        getSubMerchant(e, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
   })

    // INTERNATIONAL PAYMENT TYPE
    var internationalObj = {
        "Credit Card" :   "CC",
        "Debit Card" : "DC"
    };

    var _eNACH = {
        "eNACH Registration": "eNachRegistration",
        "eNACH Transaction" : "eNachTransaction"
    }

    var fetchData = function(data) {

        let tableWrapper = $("#paymentOptions-info");
        
        tableWrapper.find("tbody").text("");
        let detailBox = "";

        if(data.length > 0) {
            // tableWrapper.find("table").removeClass("d-none");
            // errorMsgWrapper.addClass("d-none");
            
            $("#wwgrp_payId").find(".selectpicker").selectpicker("deselectAll");
            $("#wwgrp_payId").find(".selectpicker").selectpicker("refresh");

            $("#txnType").selectpicker("deselectAll");
            $("#txnType").selectpicker("refresh");
            

            for(let i = 0; i < data.length; i++) {
                detailBox += '<tr>'; // ROW START

                // MERCHANT NAME
                detailBox += '<td class=""><span>'+ data[i]["merchantName"] +'</span><input type="hidden" id="payId" name="payId" value="'+ data[i]["payId"] +'"><input type="hidden" id="merchantName" name="merchantName" value="'+ data[i]["merchantName"] +'"></td>';                

                //SubMerchantName
                var subMerchantId = data[i]["subMerchantPayId"];
                if(subMerchantId!=null)
                    detailBox += '<td class=""><span>'+ data[i]["subMerchantName"] +'</span><input type="hidden" id="subMerchantPayId" name="subMerchantPayId" value="'+ data[i]["subMerchantPayId"] +'"><input type="hidden" id="subMerchantName" name="subMerchantName" value="'+ data[i]["subMerchantName"] +'"></td>';                
                else
                    detailBox += '<td class=""><span>NA</span></td>';     
                // TXN TYPE
                detailBox += '<td class=""><span>'+ data[i]["txnType"] +'</span><input type="hidden" name="txnType" value="'+ data[i]["txnType"] +'"></td>';
                
                // MOP TYPE
                detailBox += '<td class=""><span>'+ data[i]["mopType"] +'</span><input type="hidden" name="mopType" value="'+ data[i]["mopType"] +'"></td>';

                // PAYMENT TYPE
                var paymentType = data[i]["paymentType"];
                detailBox += '<td class=""><span>'+ data[i]["paymentType"] +'</span><input type="hidden" name="paymentType" value="'+ data[i]["paymentType"] +'"></td>';

                // PAYMENT REGION
                detailBox += '<td class=""><span>'+ data[i]["paymentRegion"] +'</span><input type="hidden" name="paymentRegion" value="'+ data[i]["paymentRegion"] +'"></td>';

                //SLAB
                detailBox += '<td class=""><span>'+ data[i]["slab"] +'</span><input type="hidden" name="slab" value="'+ data[i]["slab"] +'"></td>';
                
                // FIXED CHARGES
                detailBox += '<td class=""><span>'+ data[i]["fixedCharge"] +'</span><input type="hidden" value="'+ data[i]["fixedCharge"] +'"><input type="text" onkeyup="onlyNumericKey(this, event, 2);" class="input-edit max-width-100 form-control py-5 px-5 height-auto font-size-14" name="fixedCharge" value="'+ data[i]["fixedCharge"] +'"></td>';

                // PERCENTAGE
                detailBox += '<td class=""><span>'+ data[i]["percentageAmount"] +'</span><input type="hidden" value="'+ data[i]["percentageAmount"] +'"><input type="text" name="percentageAmount" onkeyup="onlyNumericKey(this, event, 2);" class="input-edit form-control py-5 px-5 height-auto font-size-14 max-width-100" value="'+ data[i]["percentageAmount"] +'"></td>';

                // ACTION COLUMN
                detailBox += '<td><div class="actionBtns visual-mode"><a  href="#" class="btn-edit bg-color-primary-light-2 color-primary hover-color-primary border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-pencil-alt"></i></a><a href="javascript;"  class="btn-delete bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-trash"></i></a></div><div class="actionBtns edit-mode"><a href="#" class="btn-save bg-color-green-lightest color-green hover-color-green border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-check"></i></a><a href="#" class="btn-cancel bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-times"></i></a></div></td>';
                detailBox += '</tr>';
            }
        } else {
            detailBox += '<tr><td colspan="7" class="color-red text-center font-size-14">No data avaiable</td></tr>';
        }

        $(detailBox).appendTo(tableWrapper.find("tbody"));
    }

    var showAllPaymentOptions = function(payId, txnType, paymentType, paymentRegion, subMerchantPayId, url) {
        $("body").removeClass("loader--inactive");
        var _checkbox = $("#payment-options input[type='checkbox']");
        _checkbox.removeAttr("checked");

        let token  = document.getElementsByName("token")[0].value;

        $.ajax({
            type: "post",
            url: url,
            data: {
                payId: payId,
                txnType: txnType,
                paymentType: paymentType,
                paymentRegion: paymentRegion,
                subMerchantPayId : subMerchantPayId,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data){
                console.log(data);
                // showAllPaymentOptions(payId, "sufDetailAction");
                fetchData(data.aaData);
                
                setTimeout(function() {
                    $("body").addClass("loader--inactive");                    
                }, 1000);
            },
            error: function(data) {
                alert("Try again, Something went wrong!");
            }
        });
    
        // $.ajax({
        //     type: "POST",
        //     url: url,
        //     data: {
        //         payId: payId,
        //         "token": token,
        //         "struts.token.name": "token"
        //     },
        //     success: function(data) {
        //         fetchData(data.aaData);
        //         $("body").addClass("loader--inactive");
        //         // setTimeout(() => {                    
        //         //     $("body").addClass("loader--inactive");
        //         // }, 1000);
        //     },
        //     error: function(data) {
        //         alert("Something went wrong!");
        //     }
        // });

        // $.get("../js/sufDetailsJSON.json", function(data) {
        //     fetchData(data.aaData);
        // });
    }

    // showAllPaymentOptions("ALL", "sufDetailAction");

    var setData = function(reffObj) {
        let token  = document.getElementsByName("token")[0].value;

        $.ajax({
            type: "POST",
            url: reffObj.action,
            data: {
                payId: reffObj.payId,
                merchantName: reffObj.merchantName,
                txnType: reffObj.txnType,
                paymentType: reffObj.paymentType,
                paymentRegion: reffObj.paymentRegion,
                fixedCharge: reffObj.fixedCharge,
                percentageAmount: reffObj.percentageAmount,
                mopType: reffObj.mopType,
                minSlab: reffObj.minSlab,
                maxSlab: reffObj.maxSlab,
                subMerchantPayId : reffObj.subMerchantPayId,
                slab : reffObj.slab,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data) { 
                console.log(data);
                if(data.responseValue == "Success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.response);
                    fetchAllSufDetail();
                    $("#selectAll, input[name='mopType']").removeAttr('checked');
                    $("#moptype-wrapper").addClass("d-none");
                    $(".sufCharges input[type='text']").attr('value', '');
                    $(".selectpicker").selectpicker("deselectAll");
                    setInterval(function() {                    
                        $("body").addClass("loader--inactive");
                    }, 1000);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $(".responseMsg").text(data.response);
                }             
                document.querySelector("body").classList.add("loader--inactive");
            },
            error: function(data) {
                alert("Something went wrong!");
            }
        });
    }

    // CREATE / REMOVE PAYMENT OPTION
    $("#btn-sufDetails").on("click", function(e) {
        e.preventDefault();
        var parent              =   $(this).closest(".options-parent");
        var payId               =   $("#payId").val();
        var txnType             =   $("#txnType").val();
        var _subMerchantPayId   =   $("#subMerchant").val().toString();
        var paymentType         =   $("#paymentType").val();
        var paymentRegion       =   $("#paymentRegion").val();
        var fixedCharge         =   $("#charges").val();
        var percentageAmount    =   $("#percentageAmount").val();
        var merchantName        =   $("button[data-id='payId']").attr("title");
        var mopType             =   $("#payment-options").val().toString();
        var _minSlab            =   $("#minimumSlab").val();
        var _maxSlab            =   $("#maximumSlab").val();
        var _slabType           =   $("#sufSetType").val();
        var _paymentTypeVisible = document.querySelector("#paymentType").closest(".col-md-3").classList.toString();
        var _checkMopTypeVisible = document.querySelector("#payment-options").closest(".col-md-3").classList.toString();
        if(_slabType == ''){
            alert("Please select utitlity type");
            return false;
        }else if(txnType == "" || txnType == null) {
            alert("Please select Transaction Type");
            return false;
        } else if(paymentType == "" && _paymentTypeVisible.indexOf("d-none") == -1) {
            alert("Please select Payment Type");
        } else if(mopType.length == 0 && _checkMopTypeVisible.indexOf("d-none") == -1) {
            alert("Please select at least one Mop Type");
            return false;
        } else if(fixedCharge == "" && percentageAmount == "") {
            alert("Please fill any one Charges or Percentage");
            return false;
        } else if (_minSlab == "" && txnType != "eNACH" && document.querySelector("#minimumSlab").closest("d-none") != null){
            alert("Please set min slab");
        }else if (_maxSlab == "" && txnType != "eNACH" && document.querySelector("#maximumSlab").closest("d-none") != null){
            alert("Please set max slab");
        }else if (parseInt(_maxSlab) < parseInt(_minSlab) && txnType != "eNACH"){
            alert("Minimum slab should not lesser than maximum slab");
        } else {
            // $("body").removeClass("loader--inactive");

            var _checkSubMerchantVisible = document.querySelector("#subMerchant").closest(".col-md-3").classList.toString();
            if(_checkSubMerchantVisible.indexOf("d-none") == -1){
                if(_subMerchantPayId == ""){
                    _subMerchantPayId = 'ALL';
                }
            }

            if(_slabType == 'bulk'){
                if(payId == "" || payId == null) {
                    payId = "ALL";
                } else {
                    payId = payId.toString();
                }
            }else{
                if(payId == ''){
                    alert("Please select merchant");
                    return false;
                }
            }

            document.querySelector("body").classList.remove("loader--inactive");

            setData({
                action: "sufDetailSubmitAction",
                payId: payId,
                merchantName: merchantName,
                txnType: txnType,
                fixedCharge: fixedCharge,
                percentageAmount: percentageAmount,
                mopType: mopType,
                minSlab : _minSlab,
                maxSlab : _maxSlab,
                paymentType: paymentType,
                paymentRegion: paymentRegion,
                subMerchantPayId : _subMerchantPayId
            });
        }
    });

    $(".confirmButton").on("click", function(e){
        $("body").removeClass("loader--inactive");
        
        location.reload();
    })


    // CANCEL EDITABLE ROW
    $("body").on("click", ".btn-cancel", function(e) {
        e.preventDefault();
        $(this).closest("tr").removeClass("editable--active");
    });


    // SAVE ROW
    $("body").on("click", ".btn-save", function(e) {

        e.preventDefault();
        $("body").removeClass("loader--inactive");
        var parent = $(this).closest("tr");
        let payId = parent.find("input[name='payId']").val();
        let merchantName = parent.find("input[name='merchantName']").val();
        let paymentType = parent.find("input[name='paymentType']").val();
        let txnType = parent.find("input[name='txnType']").val();
        let mopType = parent.find("input[name='mopType']").val();
        let paymentRegion = parent.find("input[name='paymentRegion']").val();
        let fixedCharge = parent.find("input[name='fixedCharge']").val();
        let percentageAmount = parent.find("input[name='percentageAmount']").val();
        let slab = parent.find("input[name='slab']").val();
        let subMerchantPayId = parent.find("input[name='subMerchantPayId']").val();
        
        setData({
            payId: payId,
            merchantName: merchantName,
            subMerchantPayId : subMerchantPayId,
            txnType: txnType,
            fixedCharge: fixedCharge,
            percentageAmount: percentageAmount,
            mopType: mopType,
            action: "sufDetailEditAction",
            paymentType: paymentType,
            paymentRegion:paymentRegion,
            slab : slab
        });

        $(this).closest("tr").removeClass("editable--active");
        $("body").addClass("loader--inactive");
        
    });

    // DELETE ROW
    $("body").on("click", ".btn-delete", function(e) {
        $("#paymentOptions-info tr").removeClass("active-tr");
        $(this).closest("tr").addClass("active-tr");
        e.preventDefault();
        $("#fancybox").fancybox({
            'overlayShow': true
        }).trigger('click');

    });

    $("#confirm-btn").on("click", function(e){
        $("body").removeClass("loader--inactive");
        var parent = $(".active-tr");
        let payId = parent.find("input[name='payId']").val();
        let txnType = parent.find("input[name='txnType']").val();
        let mopType = parent.find("input[name='mopType']").val();
        let paymentType = parent.find("input[name='paymentType']").val();
        let paymentRegion = parent.find("input[name='paymentRegion']").val();
        let slab = parent.find("input[name='slab']").val();
        let token  = document.getElementsByName("token")[0].value;
        let subMerchantPayId = parent.find("input[name='subMerchantPayId']").val();
        
        
        $.ajax({
            type: "POST",
            url: "sufDetailDeleteAction",
            data: {
                payId: payId,
                subMerchantPayId : subMerchantPayId,
                txnType: txnType,
                mopType: mopType,
                paymentType: paymentType,
                paymentRegion: paymentRegion,
                slab: slab,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data) {
                fetchAllSufDetail();
                location.reload(true);
            },
            error: function(data) {
                alert("Something went wrong!");
            }
        });
    })
    

    $("body").on("click", "#cancel-btn", function(e){
        $("#paymentOptions-info tr").removeClass("active-tr");
        $.fancybox.close();
    })

    $("#saveEdit").click(function(e) {
        e.preventDefault();
        editDeleteFunc("sufDetailEditAction");
    })

    $("body").on("change", ".suf-edit-check", function(){
        if($(this).find("input[type='checkbox']").is(":checked")){
            $(this).nextAll("input").removeAttr("readonly");
        }else{
            $(this).nextAll("input").attr("readonly", true);
        }
    })


    $("body").on("click", ".btn-edit", function(e) {
        e.preventDefault();
        $(this).closest("tr").addClass("editable--active");
    })

    // FILTER DATA
    $("body").on("change", ".filter_suf", function(e) {
        var payId = $("#filter-payId").val();
        var txnType = $("#filter_txnType").val();
        var paymentType = $("#filter_paymentType").val();
        var paymentRegion = $("#filter_paymentRegion").val();
        var subMerchantPayId = $("#filter-subMerchant").val();
        if(txnType == "eNACH"){
            if(payId != "" && txnType != "" && paymentType != "") {
                showAllPaymentOptions(payId, txnType, paymentType, paymentRegion, subMerchantPayId, "sufDetailAction");
            }
        }else if(txnType != "eNACH" && txnType != "Sale" && txnType != "Refund" && txnType != ""){
            if(payId != "" && txnType != ""){
                showAllPaymentOptions(payId, txnType, paymentType, paymentRegion, subMerchantPayId, "sufDetailAction");
            }
        }
        else{
            if(payId != "" && txnType != "" && paymentType != "" && paymentRegion != "") {
                showAllPaymentOptions(payId, txnType, paymentType, paymentRegion, subMerchantPayId, "sufDetailAction");
            }
        }
    });

    $("body").on("change", ".checkbox-label input", function(e){
        if($(this).is(":checked")){
          $(this).closest("label").addClass("checkbox-checked");
        }else{
          $(this).closest("label").removeClass("checkbox-checked");
        }
      });
    
    var fetchMopList = function(mopList) {
        var mopListStr = "";
        if(mopList !== undefined) {
            if(mopList.length > 0) {
                mopList.forEach(function(mopType) {
                    mopListStr += '<option value="'+mopType+'">'+mopType+'</option>';
                    // mopListStr += '<li class="col-md-2"><label for="moptype-'+ mopType +'" class="checkbox-label unchecked"><input type="checkbox" name="mopType" id="moptype-'+ mopType +'" class="mr-5" value="'+ mopType +'" /> '+ mopType +'</label></li>'
                });
            }
        } else {
           
        }

        $("#payment-options").html("");
        $(mopListStr).appendTo("#payment-options");
        $("[data-id=mopType]").removeClass("d-none");
        $("#payment-options").selectpicker("refresh");

        setTimeout(function() {
            $("body").addClass("loader--inactive");
        }, 1000);
    }

    $("#payId, #txnType, #paymentType").on("change",function() {
        var payId = $("#payId").val();
        var paymentType = $("#paymentType").val();
        var txnType = $("#txnType").val();

        if(payId !== "" && txnType !== "" && paymentType !== "" && txnType !== "eNACH") {
            $("body").removeClass("loader--inactive");
            let token  = document.getElementsByName("token")[0].value;
    
            $.ajax({
                type: "POST",
                url: "getSufDetailMopTypeAction",
                data: {
                    paymentType : paymentType,
                    "token": token,
                    "struts.token.name": "token"
                },
                success: function(data) {
                   fetchMopList(data.mopList); 
                },
                error: function(data) {
                    alert("Something went wrong!");
                },
				input: function(data) {
                   // Do NOthing
                }
            });
        } else {
            $("#moptype-wrapper").addClass("d-none");
        }
    });

    // ADD SELECT OPTION
    var _addSelectOption = function(reffObj) {
        var opt = document.createElement("option");
        opt.appendChild(document.createTextNode(reffObj.text));
        opt.value = reffObj.value;
        document.getElementById(reffObj.element).appendChild(opt);
    }

    var updateSelection = function(obj) {
        
        _addSelectOption({
            element: obj.element,
            value: "",
            text: "Select Payment Type"
        });
        for(var key in obj.data) {
            _addSelectOption({
                element: obj.element,
                value: obj.data[key],
                text: key
            });
        }
        $("#" + obj.element).selectpicker("refresh");
    }

    $("#paymentRegion, #filter_paymentRegion").on("change", function(e) {
        var _val = $(this).val();
        var getId = $(this).attr("id");
        var pmntType = null;

        if(getId == "paymentRegion") {
            pmntType = "paymentType";
        } else if(getId == "filter_paymentRegion") {
            pmntType = "filter_paymentType"
        }
        
        if(_val !== "") {
            var _paymentType = $("#" + pmntType);
            _paymentType.html("");

            if(pmntType == "paymentType") {
                $("#suf-paymentType").removeClass("d-none");
            } else if(pmntType == "filter_paymentType") {
                $("#filter-paymentType").removeClass("d-none");
            }


            if(_val == "Domestic") {                
                updateSelection({
                    element: pmntType,
                    data: domesticObj
                });
            } else if(_val == "International") {
                updateSelection({
                    element: pmntType,
                    data: internationalObj
                });
            }

            if(pmntType == "paymentType") {
                if(_paymentType.val() == "") {
                    $("#moptype-wrapper").addClass("d-none");
                }
            }
        } else {
            $("#suf-paymentType, #moptype-wrapper, #filter-paymentType").addClass("d-none");
        }
    });

    var fetchAllSufDetail = function() {
        $("body").removeClass("loader--inactive");

        $.ajax({
            type: "GET",
            url: "fetchAllSufDetailAction",
            success: function(data) {
                fetchData(data.aaData);
                
                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                }, 1000);
            },
            error: function() {
                alert("Try Again, Something went wrong!");
                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                }, 1000);
                return false;
            }
        });
    }

    fetchAllSufDetail();

    // transaction type data
        // transaction type data
        document.querySelector("#txnType").onchange = function(e){
            $(".slab_input").val("");
            $("#paymentType").html("");
            if(e.target.value == "eNACH"){
                updateSelection({
                    element: "paymentType",
                    data: _eNACH
                });
                $("#suf-paymentType").removeClass("d-none");
                $("#paymentRegion").selectpicker('val', "");
                $("#paymentRegion").closest(".col-md-3").addClass("d-none");
                $(".slab_input").addClass("d-none");
            }else if(e.target.value == "Sale" || e.target.value == "Refund"){
                $("#paymentRegion").closest(".col-md-3").removeClass("d-none");
                $("#suf-paymentType").addClass("d-none");
                
                $(".slab_input").removeClass("d-none");
                $("#paymentType").html("");
            }else if(e.target.value != "Sale" && e.target.value != "Refund" && e.target.value != "eNACH" && e.target.value != ""){
                $("#suf-paymentType").addClass("d-none");
                $("#paymentType").html("");
                $(".slab_input").addClass("d-none");
                $("#paymentRegion").selectpicker('val', "");
                $("#paymentRegion").closest(".col-md-3").addClass("d-none");
            }

            setTimeout(function(e){
                $("[data-id='mopType']").addClass("d-none");
            }, 500);

            if(e.target.value == ""){
                $("#paymentType").html("");
            }
        }
    
        document.querySelector("#filter_txnType").onchange = function(e){
            if(e.target.value == "eNACH"){
                updateSelection({
                    element: "filter_paymentType",
                    data: _eNACH
                });
                $("#filter_paymentRegion").closest(".col-md-3").addClass("d-none");
                $("#filter-paymentType").removeClass("d-none");
            }else if(e.target.value != "Sale" && e.target.value != "Refund" && e.target.value != "eNACH" && e.target.value != ""){
                $("#filter_paymentRegion").closest(".col-md-3").addClass("d-none");
                $("#filter-paymentType").addClass("d-none");
            }  
            else{
                $("#filter-paymentType").addClass("d-none");
                $("#filter_paymentRegion").closest(".col-md-3").removeClass("d-none");
                $("#filter_paymentType").html("");  
            }
            if(e.target.value == ""){
                $("#filter-paymentType").html("");
            }
        }
    
});