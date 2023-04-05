
function removeError(e){
    e.closest(".single-account-input").classList.remove("hasError");
}

function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32 || x == 8) {
    } else {
        event.preventDefault();
    }
}

function checkRegEx(e){
    var _getRegEx = e.getAttribute("data-regex");
    var _newRegEx = new RegExp(_getRegEx);
    var _value = e.value;
    if(_value != ""){
        if(_newRegEx.test(_value) != true){
            var _getLabel = e.closest(".single-account-input").querySelector("label").innerText;
            e.closest(".single-account-input").querySelector(".error-field").innerText = "Invalid "+_getLabel;
            e.closest(".single-account-input").classList.add("hasError");
        }
    }else{
        e.closest(".col-md-3").classList.remove("has-error");
    }
}

$(document).ready(function(e){

    // set Attribute
    function createAttr(_selector, _attribute, _attrValue){
        var _attr = document.createAttribute(_attribute);
        _attr.value = _attrValue;
        document.querySelector(_selector).setAttributeNode(_attr);
    }


    // get sub merchant
    function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
        var _merchant = _this.target.value;
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
                    if(_object.isSuperMerchant == true){
                        if(obj.superMerchant == true){
                            document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                            createAttr("#"+_subMerchantAttr, "data-required", "true");
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

    document.querySelector("#merchant").addEventListener("change", function(e){
        getSubMerchant(e, "getSubMerchantList", {
            isSuperMerchant : true
        }, "Select Sub-Merchant", "");
    });

    document.querySelector("#merchantReporting").addEventListener("change", function(e){
        getSubMerchant(e, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });

    document.querySelector("#merchantPayIdBulk").addEventListener("change", function(e){
        getSubMerchant(e, "getSubMerchantList", {
            isSuperMerchant : true
        }, "Select Sub-Merchant", "");
    });  


    $(".lpay_upload_input").on("change", function (e) {
		var _val = $(this).val();
		var _fileSize = $(this)[0].files[0].size;
        console.log($(this)[0].files[0].type);
        var _fileType = $(this)[0].files[0].type;
		var _tmpName = _val.replace("C:\\fakepath\\", "");
		$(this).closest("label").next(".hideFields").val(_tmpName);
		if (_val != "") {
            if(_fileType == "application/vnd.ms-excel"){
                $("body").removeClass("loader--inactive");
                $(this).closest("label").find(".default-upload").addClass("d-none");
                $(this).closest("label").find("#placeholder_img").css({ "display": "none" });
                if (_fileSize < 2000000) {
                    $(this).closest("label").attr("data-status", "success-status");
                    $(this).closest("label").find(".fileName").text(_tmpName);
    
                    $("#bulkUpdateSubmit").attr("disabled", false);
                    setTimeout(function (e) {
                        $("body").addClass("loader--inactive");
                    }, 500);
                } else {
                    $(this).closest("label").attr("data-status", "error-status");
                    $("#bulkUpdateSubmit").attr("disabled", true);
                    setTimeout(function (e) {
                        $("body").addClass("loader--inactive");
                    }, 500);
                }
            }else{
                $(this).closest(".single-account-input").addClass("hasError");
                $(this).closest(".single-account-input").find(".error-field").text("Invalid file type");
            }
		}
	});

    document.querySelector("#bulkUpdateSubmit").onclick = function(e){
       createJsonData("[data-target='bulk']", ".single-account-input");
        var _checkNull = document.querySelector("[data-target='bulk'] .hasError");
        if(_checkNull == null){
            var _active = document.querySelector(".lpay-nav-item.active").querySelector("a").getAttribute("data-id");
            localStorage.setItem("active-vpa", _active);
        }else{
            return false;
        }

    }

    function tabChange(_selector){
        var _getClickTab = _selector;
        $(".lpay-nav-item").removeClass("active");
        $("[data-id='"+_getClickTab+"']").closest("li").addClass("active");
        $(".lpay_tabs_content").removeClass("active-block");
        $("[data-target='"+_getClickTab+"']").addClass("active-block");
    }

    var _allTab = document.querySelectorAll(".lpay-nav-item");
    _allTab.forEach(function(index, array, element){
        _allTab[array].addEventListener('click', function(e){
            e.preventDefault();
            var _getAttr = e.target.getAttribute("data-id");
            tabChange(_getAttr);
        })
    })

    var _checkNull = document.querySelector("#resellerTrue");

    var _getActive = localStorage.getItem("active-vpa");
    if(_getActive == "bulk" && _checkNull == null){
        tabChange(_getActive);
    }else if(_checkNull != null){
        tabChange("reporting");
    }else{
        tabChange("single");
    }

    function createJsonData(_selector, _inputDiv){
        console.log(_selector);
        var _obj = {};
        var _parent = document.querySelector(_selector);
        console.log(_parent);
        var _allInput = _parent.querySelectorAll("[data-var]");
        _allInput.forEach(function(index, array, element){
            var _getAttr = index.attributes['data-var'].nodeValue;
            var _closest = index.closest(".d-none");
            if(index.attributes['data-required'] != null){
                var _required = index.attributes['data-required'].nodeValue;
            }else{
                var _required = "false"; 
            }
            if(_closest == null || index.value != ""){
                if(index.value == "" && _required == "true"){
                    if(index.closest(_inputDiv).querySelector(".error-field") != null){
                        index.closest(_inputDiv).classList.add("hasError");
                        index.closest(_inputDiv).querySelector(".error-field").innerText = "Should not be blank";
                    }
                }else{
                    index.closest(_inputDiv).classList.remove("hasError");
                    _obj[_getAttr] = index.value;
                }
            }
        })
        return _obj;
    }

    document.querySelector("#validateVpa").onclick = function(e){
        var _obj = createJsonData("[data-target='single'].active-block", ".single-account-input");
        var _checkError = document.querySelector(".hasError");
        if(_checkError == null){
            document.querySelector("body").classList.remove("loader--inactive");
            $.ajax({
                type: "POST",
                url: "vpaVerificationAction",
                data: _obj,
                success: function(data){
                    
                    document.querySelector(".lpay_data-inner").classList = "lpay_data-inner";
                    if(data.response == "success"){
                        document.querySelector(".lpay_data-inner").classList.add("vpa-success");
                        for(key in data.impsData){
                            var _checkNull = document.querySelector("[data-vpaSuccess='"+key+"']");
                            if(_checkNull !== null){
                                _checkNull.innerText = data.impsData[key];
                            }
                        }
                        
                    }else{
                        
                        document.querySelector(".lpay_data-inner").classList.add("vpa-failed");
                        document.querySelector("#responseMsgVpa").innerText = data.responseMsg;
                        for(key in data.impsData){
                            var _checkNull = document.querySelector("[data-vpa='"+key+"']");
                            console.log(_checkNull);
                            if(_checkNull !== null){
                                _checkNull.innerText = data.impsData[key];
                            }
                        }
                    }
                    document.querySelector(".lpay_data-popup").classList.add("active-popup");
                    // document.querySelector("body").classList.add("loader--inactive");
                    document.querySelector(".lpay_data-inner").focus();
                    _stopLoader = setInterval(checkPopUp, 500);
                    resetInput("[data-target='single']");
                }
            })
        }
    }

    document.querySelector("body").onclick = function(e){
        var _closestDiv = e.target.closest(".lpay_data-inner");
        if(_closestDiv == null){
            document.querySelector(".lpay_data-popup").classList.remove("active-popup");
        }
    }

    function resetInput(_parentSelector){
        var _parent = document.querySelectorAll(_parentSelector + " [data-var]");
        var _dataHide = document.querySelectorAll(_parentSelector + " [data-hide]");
        _parent.forEach(function(index, array, element){
            var _localName = index.localName;
            if(_localName == "input" || _localName == "INPUT"){
                index.value = "";
            }else{
                $("#"+index.id).val('default');
                $("#"+index.id).selectpicker('refresh');
            }
        })
        _dataHide.forEach(function(index, array, element){
            index.classList.add("d-none");
        })
    }

    $(".selectpicker").val('default');
    $(".selectpicker").selectpicker('refresh');

    setTimeout(function(e){
        $("[data-id='responseTable']").fadeOut();
        $("[data-id='responseFileFormat']").fadeOut();
        $("[data-id='responseFileEmpty']").fadeOut();
    }, 3000)

    

    var _stopLoader = "";

    function checkPopUp(){
        var _checkNull = document.querySelector(".lpay_data-popup");
        var _checkAddClass = _checkNull.classList.toString();
        // console.log(_checkNull);
        if(_checkAddClass.indexOf("active-popup") != -1){
            document.querySelector("body").classList.add("loader--inactive");
            clearInterval(_stopLoader);
        }
    }
    

    var today = new Date();
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

    function vpaListTable(){
        
    var _obj = createJsonData("[data-target='reporting']", ".single-account-input");
        document.querySelector("body").classList.remove("loader--inactive");
        $('#vpaValidateTable').dataTable( {
            "destroy": true,
            "ajax" : {
    
                "url" : "vpaBeneVerifyReportData",
                "type" : "POST",
                "data" : _obj,
            },
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500)
            },
            "sAjaxDataProp" : "impsDataList",
            "aoColumns": [
                { "mData": "merchant" },
                { "mData": "orderId" },
                { "mData": "txnId" },
                { "mData": "date"},
                { "mData": "payerAddress"},
                { "mData": "status"}
                
            ]
        });
    }

    vpaListTable();

    function dataTableMoreData ( d ) {
        var _mainDiv = "<div class='main-div'>";
        var _obj = {
            "merchantPayId" : "Pay ID",
            "subMerchant" : "Sub Merchant Name",
            "rrn": "Txn Reference Number",
            "payerName": "VPA Name",
            "accountType": "Account Type",
            "ownerType": "Owner Type ",
            "phoneNo" : "Mobile Number"
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

    function getAllData(){
        var _new = document.querySelectorAll(".inner-div");
        _new.forEach(function(index, array, element){
            var _getValue = _new[array].children[1].innerText;
            if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
                _new[array].classList.add("d-none");
            }
        })
    }

    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
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

    document.querySelector("#view").onclick = function(e){
        vpaListTable();
    }

    function downloadVpaList(_selector, _relateSelector){
        var _obj = createJsonData("[data-target='reporting']", ".single-account-input");;
        var _option = "";
        for(key in _obj){
            _option += "<input type='hidden' name='"+key+"' value='"+_obj[key]+"' />";
        }
        document.querySelector(_selector).innerHTML = _option;
        var _getSubMerchant = document.querySelector(_selector+" [name='subMerchantPayId']");
        if(_getSubMerchant != null && _getSubMerchant == ""){
            document.querySelector(_selector+" [name='subMerchantPayId']").value = "ALL";
        }
        document.querySelector(_selector).submit();
    }

    document.querySelector("#download").onclick = function(e){
        
        downloadVpaList("#downloadVpa", "[data-target='reporting']");
        
    }

    $('#download-format').DataTable({
		dom: 'B',
		buttons: [
			{
				extend: 'csv',
				text: 'Download CSV Format',
				className: 'lpay_button lpay_button-md lpay_button-primary',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
	});

    if ($("#rowCount").val() != "" && $("#rowCount").val() != "-1" && $("#wrongCsv").val() == "0") {
		$("[data-target='impsTransfer']").addClass("d-none");
		$("[data-target='bulkAccount']").removeClass("d-none");
		$("[data-id='responseTable']").removeClass("d-none");
	}

	if ($("#wrongCsv").val() != "0" && $("#wrongCsv").val() != "") {
		$("[data-target='impsTransfer']").addClass("d-none");
		$("[data-target='bulkAccount']").removeClass("d-none");
		$("[data-id='responseFileFormat']").removeClass("d-none");
	}

	if ($("#fileIsEmpty").val() != "0" && $("#fileIsEmpty").val() != "") {
		$("[data-target='impsTransfer']").addClass("d-none");
		$("[data-target='bulkAccount']").removeClass("d-none");
		$("[data-id='responseFileEmpty']").removeClass("d-none");
	}

})
