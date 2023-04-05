function tabShow(_this, _data, _runTim){
    var _allTab = document.querySelectorAll(".lpay_tabs_content");
    var _allLink = document.querySelectorAll(".lpay-nav-item");
    _allTab.forEach(function(index, element, array){
        index.classList.add("d-none");

    })
    _allLink.forEach(function(index, array, element){
        index.classList.remove("active");
        if(_runTim == "load"){
            index.classList.add("d-none");
        }
    })
    _this.classList.add("active");
    if(_runTim == "load"){
        _this.classList.remove("d-none");
    
    }
    document.querySelector("[data-target='"+_data+"']").classList.remove("d-none");
    _runTime = "yo";
    resetFields("isSelect");
}

function remainingLimit(){
    var _this = document.querySelector("#oneTimeRefundLimit").value;
    document.querySelector("#remainingLimit").value = _this;
}

function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
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

document.querySelector("#merchant-limit").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "Select Sub-Merchant", "");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchant-limit").closest(".col-md-3").classList.toString();
        if(_checkSubMerchant.indexOf("d-none") != -1){
            refundLimitAvailable();
        }else{
            resetFields("notSelect")
        }
    }, 500);
    
});

document.querySelector("#merchant").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
    
});

document.querySelector("#subMerchant-limit").addEventListener("change", function(_this){
    refundLimitAvailable();
})

function resetFields(_condition) {
    var _parent = document.querySelector("[data-target='refundLimitAvail']");
    var _input = _parent.querySelectorAll("input.lpay_input");
    var _checkbox = _parent.querySelectorAll("input[type='checkbox']");
    var _selectpicker = _parent.querySelectorAll("select");
    var _hide = _parent.querySelectorAll("[data-hide]");
    // var _readonly = _parent.querySelectorAll("[data-readonly]");

    _input.forEach(function(index, element, array){
        index.value = "";
        var _readonly = document.createAttribute("readonly");
        if(index.id != "remarks"){
            index.setAttributeNode(_readonly);
        }
    })

    _checkbox.forEach(function(index, element, array){
        index.checked = false;
        index.closest("label").classList.remove("checkbox-checked");
    })

    if(_condition == "isSelect"){
        _selectpicker.forEach(function(index, element, array){
            var _id = index.id;
            $("#"+_id).val('default');
            $("#"+_id).selectpicker('refresh');
        })
    
        _hide.forEach(function(index, element, array){
            index.classList.add("d-none");
        })
    }


    document.querySelector("body").classList.add("loader--inactive");
    $(".lpay_popup").fadeOut();


}

function refundLimitAvailable(){
    var _payId = document.querySelector("#merchant-limit").value;
    var _subMerchantPayId = document.querySelector("#subMerchant-limit").value;
    var _parent = document.querySelector('[data-target="refundLimitAvail"]');
    // console.log(_payId);
    if(_payId != ""){
        $.ajax({
            type: "POST",
            url: "fetchRefundLimitByPayId",
            data: {
                "payId" : _payId,
                "subMerchantId" : _subMerchantPayId
            },
            success: function(data){
                resetFields("notSelect");
                for(key in data.refundLimit){
                    var _key = document.querySelector("[data-target='refundLimitAvail']").querySelector("[data-var='"+key+"']");
                    if(_key != null){
                        if(typeof data.refundLimit[key] == "boolean"){
                            if(data.refundLimit[key] == true){
                                document.querySelector("#"+key).checked = true;
                                document.querySelector("#"+key).closest("label").classList.add("checkbox-checked");
                                document.querySelector("#"+key).closest("div").querySelector("input[type='text']").removeAttribute("readonly");
                            }
                        }else{
                            _parent.querySelector("[data-var='"+key+"']").value = data.refundLimit[key];
                            // document.querySelector("[data-var='"+key+"']").removeAttribute("readonly");
                        }
                    }
                }
                if(data.refundLimit != undefined){
                    var _attr = document.createAttribute("readonly");
                    document.querySelector("[data-var='refundLimitRemains']").setAttributeNode(_attr);
                    document.querySelector("[data-var='refundLimitRemains']").value = data.refundLimit['refundLimitRemains'];
                }
            }

        })
    }
}

$(document).ready(function(e) {
    function variableJson(_parent) {
        var _selector = document.querySelector(_parent);
        var _inputs = _selector.querySelectorAll("[data-var]");
        var _obj = {};
        _inputs.forEach(function(index, array, element) {
            _obj[index.getAttribute("data-var")] = index.value;
        });
        // console.log(_obj);
        return _obj;
    }

    function format ( d ) {
		_new = "<div class='main-div'>";
        var _userType = document.querySelector("#userType").value;
        if(_userType == "ADMIN" || _userType == "SUBADMIN"){
            var _obj = {
                "subMerchantName" : "Sub-Merchant Name",
                "assignorName": "Assigned By Name",
                "assignorEmail": "Assigned By Email",
                "assignorMobile": "Assigned By Mobile",
                "remarks" : "Remarks"
            }
        }else{
            var _obj = {
                "subMerchantName" : "Sub-Merchant Name",
                "remarks" : "Remarks"
            }
        }
		for(key in _obj){
			if(_obj[key].hasOwnProperty("className")){
				var _getKey = Object.keys(_obj[key]);
				_new += '<div class="inner-div '+_obj[key]["className"]+'">'+
						'<span>'+_obj[key][_getKey[0]]+'</span>'+
						'<span>'+d[_getKey[0]]+'</span>'+
					'</div>'
			}else{
				_new += '<div class="inner-div">'+
					'<span>'+_obj[key]+'</span>'+
					'<span>'+d[key]+'</span>'+
				'</div>'
			}
		}
		_new += "</div>";
		return _new;
	}
    
    function refundLimitTable() {
        var _objInner = variableJson("[data-target='refunLimitTrailReport']");
        // _obj['payId'] = document.querySelector("#merchant").value;

        // console.log(_obj);
        
        $("#refundLimitTrail-table").dataTable({
            "ajax": {
                "type": "POST",
                "url": "fetchRefundLimitsAction",
                "data" : _objInner
            },
            "destroy" : true,
            "aoColumns" : [
                {"mData" : "merchantName", "width" : "30%"},
                {"mData" : "date", "width" : "20%"},
                {"mData" : "credit", "width" : "15%"},
                {"mData" : "debit", "width" : "10%"},
                {"mData" : "balance", "width" : "10%"},
                {"mData" : "status", "width" : "15%"}
            ]
        });
    }

    $("body").on("click", "#refundLimitTrail-table tbody td", function(e){
		var table = new $.fn.dataTable.Api('#refundLimitTrail-table');
		if(e.target.localName != "button"){
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
				row.child( format(row.data()) ).show();
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

    $(".confirmButton").on("click", function(e) {
        refundLimitTable();
    });

    document.querySelector("#view-button").onclick = function(e) {
        refundLimitTable();
    }

    refundLimitTable();

    $(".checkbox-label input").on("change", function(e) {
        if($(this).is(":checked")) {
            $(this).closest("label").addClass("checkbox-checked");
        } else {
            $(this).closest("label").removeClass("checkbox-checked");
        }
    });

    function checkedRefundLimit(_this) {
        var _allChecks = document.querySelectorAll(".lp-limit_checks");
        _allChecks.forEach(function(index, array, element){

            index.querySelector("input").checked = false;
            index.closest("label").classList.remove("checkbox-checked");
            var _readonly = document.createAttribute("readonly");
            index.closest("div").querySelector(".lpay_input").setAttributeNode(_readonly);
            index.closest("div").querySelector(".lpay_input").value = "";
        });

        _this.target.checked = true;
        _this.target.closest("label").classList.add("checkbox-checked");
        _this.target.closest("div").querySelector(".lpay_input").removeAttribute("readonly");
    }

    var _allChecks =  document.querySelectorAll(".lp-limit_checks");
    _allChecks.forEach(function(index, array, element){
        index.addEventListener('change', function(e){
            checkedRefundLimit(e);
        })
    })

    function sendRefundLimit(){
        var _obj = variableJson("[data-target='refundLimitAvail']");
        var _clear = false;
        var _merchant = document.querySelector("#merchant-limit").value;
        var _subMerchant = document.querySelector("#subMerchant-limit").closest("[data-target]");
        for(key in _obj){
            if(_obj[key] == "on"){
                _obj[key] = document.querySelector("#"+key).checked;
                if(_obj[key] == true){
                    _clear = true;
                }
            }
        }
        var _subMerchantVisible = _subMerchant.classList.toString().indexOf('d-none');
        if(_clear == true && _merchant != ""){
            if(_subMerchantVisible == -1){
                if(document.querySelector("#subMerchant-limit").value == ""){
                    alert("Please select sub-merchant");
                    return false;
                }
            }
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "POST",
                url: "assignRefundLimitAction",
                data: _obj,
                success: function(data){

                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");

                },
                error: function(data){

                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");

                }
            })
        }else{
            alert("Please Select The Merchant");
        }
    }


    document.querySelector("#saveRefundLimit").onclick = function(e){
        sendRefundLimit();
    }
});
