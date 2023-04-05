var _variables = {
    "chargingPlatformNav" : ".charging-platform-nav",
    "chargingPlatformDetail" : ".charging-platform-detail",
    "chargingDetailBox" : ".charging-detail-box",
}

$(document).ready(function() {

    function _tableArrow (){
		var _table = document.querySelectorAll(".lpay_table_wrapper");
		if(_table != null){
			var _html = '<button class="arrow arrow-left"><i class="fa fa-angle-left" aria-hidden="true"></i></button><button class="arrow arrow-right"><i class="fa fa-angle-right" aria-hidden="true"></i></button>';
			for(var i = 0; i < _table.length; i++){
				_table[i].classList.add("has-scroll");
				var _createDiv = document.createElement("div");
				var _divClass = document.createAttribute("class");
				_divClass.value = "table_arrow";
				_createDiv.setAttributeNode(_divClass);
				_table[i].appendChild(_createDiv);
				_table[i].querySelector(".table_arrow").innerHTML = _html;
			}
		}
		if(_table !=  null){
			function move(e, _currentTableId){
				var _getButton = e.target.classList[1];
				var _count = 0;
				var _id = setInterval(moveScroll, 10);
				function moveScroll(){
					if(_count == 20){
						clearInterval(_id);
						_count = 0;
					}else{
						_count++;
						if(_getButton == "arrow-right"){
							document.getElementById(_currentTableId).scrollLeft += _count;
						}else{
							document.getElementById(_currentTableId).scrollLeft -= _count;

						}
					}
				}
			}
			var _getAllArrow = document.querySelectorAll(".arrow");
			_getAllArrow.forEach(function(index, element, array){
				_getAllArrow[element].addEventListener('click', function(e){
                    var _currentTableId = e.target.closest(".lpay_table_wrapper").id;
					move(e, _currentTableId);
				})
			})
		}
    }

    function fetchData(data) {
        var item = null;
        var detailBox = "";
        
        $.each(data, function(key, val) {
            if(val.length > 0) {
                if(key !== "regionType") {
                    let trimmedKey = key.replace(/\s/g, '');
        
                    // item += '<li><a href="#'+ trimmedKey +'-box" class="font-size-12">'+ key +'</a></li>';
        
                    detailBox += '<div id="'+ trimmedKey +'-box" class="charging-detail-box active"><div class="lpay_table_wrapper" id="chargingData"><table class="lpay_custom_table"><thead class="lpay_table_head"><tr class="d-flex"><th width="75">Currency</th><th width="200">Mop</th><th width="98">Transaction</th><th width="88">Merchant TDR</th><th width="88">Merchant FC</th><th width="88">Bank TDR</th><th width="88">Bank FC</th><th width="88">Reseller TDR</th><th width="88">Reseller FC</th><th width="110"> GST</th>';
        
                    if(data.regionType == "DOMESTIC" || data.regionType == "INTERNATIONAL") {
                        detailBox += '<th width="172">Min / Max</th>';
                    }
                    
                    detailBox += '<th width="75">Allow FC</th><th width="90">Higher Charge</th><th width="154">Max Charge Merchant</th><th width="147">Max Charge Acquirer</th><th width="110">Same as above</th><th width="92">Actions</th><tr></thead><tbody class="d-block">';
        
                    for(let i = 0; i < val.length; i++) {
                        detailBox += '<tr>';                    
                        $.each(val[i], function(_key, _val) {
                            if(_key == "tdrFcDetail") {
                                detailBox += '<td class="px-0" data-key="'+ _key +'" colspan="8"><table class="table">';
                                for(let fcDetailKey in _val) {                                
                                    detailBox += '<tr data-key="'+ fcDetailKey +'">';
                                    for(let k = 0; k < _val[fcDetailKey].length; k++) {
                                        if(_val[fcDetailKey][k] !== null) {
                                            if(k == 6 || k == 7) {
                                                detailBox += '<td width="110" height="44"><div><span class="merchant-value">'+ _val[fcDetailKey][k] +'</span><input type="hidden" class="form-control" value="'+ _val[fcDetailKey][k] +'"></div></td>';
                                            } else {
                                                detailBox += '<td width="88" height="44"><div><span class="displayed-value">'+ _val[fcDetailKey][k] +'</span><input type="text" class="form-control" onkeypress="merchantTotal(this, 4)" onkeyup="merchantTotal(this, 4); onlyNumericKey(this, event, 4);" onfocusout="updateValue(this, 4);" onchange="merchantTotal(this, 4)" value="'+ _val[fcDetailKey][k] +'"></div></td>';
                                            }
                                        }
                                    }
                                    detailBox += "</tr>";
                                }
                                detailBox += '</table></td>';
                            } else if(_key == "limitDetail") {
                                if(data.regionType == "DOMESTIC" || data.regionType == "INTERNATIONAL") {
                                    detailBox += '<td data-key="'+ _key +'" style="padding-left: 0 !important;" class="px-0"><table class="table">';
                                    for(let fcLimitKey in _val) {
                                        detailBox += '<tr data-key="'+ fcLimitKey +'"><td class="max-charge d-flex align-items-center" height="44">';
                                        for(let j = 0; j < _val[fcLimitKey].length; j++) {
                                            detailBox += '<span class="d-inline-block position-relative">'+ _val[fcLimitKey][j] +'</span>';
                                        }
                                        detailBox += '</td></tr>';
                                    }
                                    detailBox += '</table></td>';
                                }
                            } else if(_key == "allowFC") {
                                let checked = '';
                                let active = "";
                                if(_val == true) {
                                    checked = 'checked';
                                    active = 'active';
                                }
                                
                                detailBox += '<td data-key="'+ _key +'"><div class="slideSwitch '+ active +'" title="Allow FC"><input type="checkbox" id="allowFC" name="allowFC" '+ checked +' /><label for="allowFC"></label></div></td>';
    
                            } 
							 else if(_key == "chargesFlag") {
                                let checked = '';
                                let active = "";
                                if(_val == true) {
                                    checked = 'checked';
                                    active = 'active';
                                }
                                
                                detailBox += '<td data-key="'+ _key +'"><div class="slideSwitch '+ active +'" title="Charges Flag"><input type="checkbox" id="chargesFlag" name="chargesFlag" '+ checked +' /><label for="chargesFlag"></label></div></td>';
    
                            } 
							else if(_key == "maxChargeAcquirer" || _key == "maxChargeMerchant") {
                                detailBox += '<td data-key="'+ _key +'"><div><span class="displayed-value">'+ _val +'</span><input type="text" class="form-control" onkeyup="onlyNumericKey(this, event, 0);setDecimal(this, event)" onfocusout="updateValue(this, 0);" value="'+ _val +'"></div></td>';
                            } else {
                                detailBox += '<td data-key="'+ _key +'">'+ _val +'<input type="hidden" value="'+ _val +'"></td>';
                            }
                        });
                        detailBox += '<td data-key="sameAsAbove"><div class="slideSwitch" title="Same as above"><input type="checkbox" id="sameAsAbove" name="sameAsAbove" /><label for="sameAsAbove"></label></div></td><td data-key="action"><div class="tdr-edit-btn-box"><button type="submit" onClick="editTDR(this)" class="tdr-edit-btn"><i class="fa fa-pencil-alt"></i> Edit</button></div>';
    
                        detailBox += '<div class="tdr-save-btn-box"><button type="submit" name="editSingle" class="tdr-save-btn"><i class="fa fa-check"></i></button><a href="#" class="tdr-cancel-btn"><i class="fa fa-times"></i></a></div>';
    
                        detailBox += '</td></tr>';
                    }
        
                    detailBox += '</tbody></table></div></div>';
                    
                    // if(key == "Net Banking") {
                    //     $("#netbankingAll").addClass("active");
                    //     let netBankingTable = $("#netbankingAll > .table-responsive > .table");
                    //     $("#netbankingAll .editable--active").removeClass("editable--active");
        
                    //     for(let i = 0; i < val.length; i++) {
                    //         $.each(val[i], function(_key, _val) {
                    //             if(_key == "currency" || _key == "transactionType" || _key == "merchantGST" || _key == "maxChargeMerchant" || _key == "maxChargeAcquirer") {
                    //                 netBankingTable.find("td[data-key='"+ _key +"'] span").text(val[0][_key]);
                    //                 netBankingTable.find("td[data-key='"+ _key +"'] input").val(val[0][_key]);
                    //             } else if(_key == "tdrFcDetail") {
                    //                 netBankingTable.find("td[data-key='tdrFcDetail'] td").each(function(e) {
                    //                     $(this).find("span").text("0.0");
                    //                     $(this).find("input").val("0.0");
                    //                 });
                    //             } else if(_key == "limitDetail") {
                    //                 let limitDetail = val[0].limitDetail;
                                    
                    //                 for(let fcLimitKey in limitDetail) {
                    //                     let span = netBankingTable.find("tr[data-key='"+ fcLimitKey +"'] span")
                    //                     span.eq(0).text(limitDetail[fcLimitKey][0]);
                    //                     span.eq(1).text(limitDetail[fcLimitKey][1]);
                    //                 }
                    //             } else if(_key == "allowFC") {
                    //                 netBankingTable.find("td[data-key='allowFC'] .slideSwitch").removeClass("active");
                    //                 netBankingTable.find("td[data-key='allowFC'] input").removeAttr("checked");
                    //             }
                    //         });
                    //     }
                    // } else {
                    //     $("#netbankingAll").removeClass("active");
                    // }
                }                
            } else {
                // $("#netbankingAll").removeClass("active");
                detailBox += '<h2 class="text-danger text-center">Details not available.</h2>';
            }
        });
        
        // $(item).appendTo(_variables.chargingPlatformNav);
        // console.log($(detailBox));

        // $(detailBox).appendTo(_variables.chargingPlatformDetail);
        document.querySelector(_variables.chargingPlatformDetail).innerHTML += detailBox;
        _tableArrow();

        // $(_variables.chargingPlatformNav).find("li:first-child a").addClass("active");
        // $(_variables.chargingDetailBox).eq(0).addClass("active");
    }

    $(_variables.chargingPlatformNav).on("click", "a", function(e) {
        e.preventDefault();
        if(!$(this).hasClass("active")) {
            let _elementId = $(this).attr("href");
            $(this).closest(_variables.chargingPlatformNav).find("a").removeClass("active");
            $(this).addClass("active");
            $(_variables.chargingDetailBox).removeClass("active");
            $(_elementId).addClass("active");
        }
    });

    var sendRequest = function(emailId, acquirer, paymentType, paymentRegion, acquiringMode, cardHolderType) {
        $("body").removeClass("loader--inactive");
        var token  = document.getElementsByName("token")[0].value;

        $.ajax({
            type  :   "POST",
            url   :   "chargingPlatformAction",
            data  :   {
                "emailId"           :   emailId,
                "acquirer"          :   acquirer,
                "paymentType"       :   paymentType,
                "paymentRegion"     :   paymentRegion,
                "acquiringMode"     :   acquiringMode,
                "cardHolderType"    :   cardHolderType,
                "token"             :   token,
                "struts.token.name" :   "token",
            },
            success : function(data) {
                if(data.response == null){
                    emptyData();
                    fetchData(data.aaData);
                    setTimeout(function() {
                        $("body").addClass("loader--inactive");
                    }, 1000);
                }else{
                    alert(data.response);
                    setTimeout(function() {
                        $("body").addClass("loader--inactive");
                    }, 1000);
                }
            },
            error : function(data) {
                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                    alert("Something Went Wrong!");
                }, 1000);
            }
        });
    }

    const getData = _ => {
        var emailId             =   $("#merchants").val();
        var acquirer            =   $("#acquirer").val();
        var paymentType         =   $("#payment-type").val();        
        var paymentRegion       =   $("#payment-region").val();
        var acquiringMode       =   $("#acquiring-mode").val();
        var cardHolderType      =   $("#cardholder-type").val();

        if(emailId == "" || acquirer == "" || paymentType == "") {
            return false;
        } else {
            if(paymentType !== "") {
                if(paymentType == "Net Banking" || paymentType == "Wallet" || paymentType == "UPI" || paymentType == "COD" || paymentType == "EMI") {
                    $("[data-type='paymentRegion']").addClass("d-none");
                    $("[data-type='acquiringMode']").addClass("d-none");
                    $("[data-type='cardHolderType']").addClass("d-none");
                    sendRequest(emailId, acquirer, paymentType, "DOMESTIC", "OFF_US", "CONSUMER");
                } else {
                    $("[data-type='paymentRegion']").removeClass("d-none");
                    if(paymentRegion !== "" && paymentRegion == "INTERNATIONAL") {                        
                        $("[data-type='acquiringMode']").addClass("d-none");
                        $("[data-type='cardHolderType']").addClass("d-none");
                        sendRequest(emailId, acquirer, paymentType, paymentRegion, "OFF_US", "COMMERCIAL");
                    } else if(paymentRegion !== "" && paymentRegion == "DOMESTIC") {                        
                        $("[data-type='acquiringMode']").removeClass("d-none");
                        $("[data-type='cardHolderType']").removeClass("d-none");
                        if(acquiringMode == "" || cardHolderType == "") {                            
                            return false;
                        } else {                            
                            sendRequest(emailId, acquirer, paymentType, paymentRegion, acquiringMode, cardHolderType);
                        }
                    }
                }
            }
        }
    }

    $('.selctList').change(function(e) {
        getData();
    });

    $("body").on("click", ".tdr-save-btn", function(e) {
        e.preventDefault();
        let btnName = $(this).attr("name");
        saveTDR(this, btnName);  // To send dynamic data
    });

    $("body").on("click", ".tdr-cancel-btn", function(e) {
        e.preventDefault();
        let _parent = $(this).closest('tr');
        _parent.removeClass("editable--active");

        _parent.find("> td ").each(function(e) {
            let dataKey = $(this).attr('data-key');

        });
        
        // _parent.find("td[data-key='allowFC'] input, td[data-key='sameAsAbove'] input").removeAttr("checked");
        // _parent.find("td[data-key='allowFC'] .slideSwitch, td[data-key='sameAsAbove'] .slideSwitch").removeClass("active");

        // $("td[data-key='tdrFcDetail'] td").each(function(e) {
        //     $(this).find("span").text("0.0");
        //     $(this).find("input").val("0.0");
        // });
    });

    $("body").on("click", ".slideSwitch", function(e) {
        let that = $(this);
        let checkbox = that.find("input[type='checkbox']");
        let isChecked = checkbox.is(":checked");
        if(isChecked == false) {
            that.addClass("active");
            checkbox.attr("checked", true);
            if(that.attr("title") == "Same as above") {
                sameAsAbove($(this));
            }
        } else {
            that.removeClass("active");
            checkbox.attr("checked", false);
        }
    });

    const sameAsAbove = that => {
        let parentRow = that.closest("tr.editable--active");
        let _td = parentRow.find("[data-key='tdrFcDetail']");
        let _slabInput = _td.find("[data-key='slab1'] input");
        _slabInput.each(function(el) {
            _td.find("[data-key='slab2'] input").eq(el).val($(this).val());
            _td.find("[data-key='slab3'] input").eq(el).val($(this).val());
            _td.find("[data-key='slab2'] span").eq(el).text($(this).val());
            _td.find("[data-key='slab3'] span").eq(el).text($(this).val());
        });
    }

    const emptyData = _ => {
        // document.querySelector(".charging-platform-nav").innerHTML = '';
        document.querySelector(".charging-platform-detail").innerHTML = '';
    }

    const saveTDR = (that, btnName) => {
        $("body").removeClass("loader--inactive");
        var _id = document.getElementById.bind(document);
        let token  = document.getElementsByName("token")[0].value;
        var data = {
            'currency'          :   "",
            'mopType'           :   "",
            'transactionType'   :   "",            
            "slab1"             :   "",
            "slab2"             :   "",
            "slab3"             :   "",
            "merchantGST"       :   "",
            "allowFC"           :   "",
            "chargesFlag"       :   "",
            "maxChargeMerchant" :   "",
            "maxChargeAcquirer" :   "",
            "token"             :   token,
            "struts.token.name" :   "token",
        }

        let emailId = _id("merchants").value;
        let acquirer = _id("acquirer").value;
        let paymentType = _id("payment-type").value;
        let paymentRegion = _id("payment-region").value;
        let acquiringMode = _id("acquiring-mode").value;
        let cardHolderType = _id("cardholder-type").value;

        var saveDataRequest = (emailId, acquirer, paymentType, paymentRegion, acquiringMode, cardHolderType) => {
            data.emailId        =   emailId;
            data.acquirer       =   acquirer;
            data.paymentType    =   paymentType;
            data.paymentRegion  =   paymentRegion;
            data.acquiringMode  =   acquiringMode;
            data.cardHolderType =   cardHolderType;
        }

        if(emailId == "" || acquirer == "" || paymentType == "") {
            return false;
        } else {
            if(paymentType !== "") {
                if(paymentType == "Net Banking" || paymentType == "Wallet" || paymentType == "UPI" || paymentType == "COD" || paymentType == "EMI") {
                    $("[data-type='paymentRegion']").addClass("d-none");
                    $("[data-type='acquiringMode']").addClass("d-none");
                    $("[data-type='cardHolderType']").addClass("d-none");
                    saveDataRequest(emailId, acquirer, paymentType, "DOMESTIC", "OFF_US", "CONSUMER");
                } else {
                    $("[data-type='paymentRegion']").removeClass("d-none");
                    if(paymentRegion !== "" && paymentRegion == "INTERNATIONAL") {                        
                        $("[data-type='acquiringMode']").addClass("d-none");
                        $("[data-type='cardHolderType']").addClass("d-none");
                        saveDataRequest(emailId, acquirer, paymentType, paymentRegion, "OFF_US", "COMMERCIAL");
                    } else if(paymentRegion !== "" && paymentRegion == "DOMESTIC") {                        
                        $("[data-type='acquiringMode']").removeClass("d-none");
                        $("[data-type='cardHolderType']").removeClass("d-none");
                        if(acquiringMode == "" || cardHolderType == "") {                            
                            return false;
                        } else {                            
                            saveDataRequest(emailId, acquirer, paymentType, paymentRegion, acquiringMode, cardHolderType);
                        }
                    }
                }
            }
        }
    
        var _td = that.closest("tr").querySelectorAll("td[data-key]");
    
        let k = -1;
        for(let i = 0; i < _td.length; i++) {
            var dataKey = _td[i].getAttribute("data-key");
            if(dataKey == "tdrFcDetail") {
                k++;

                let _tr = _td[i].querySelectorAll("tr");
                
                for(let j = 0; j < _tr.length; j++) {
                    let datakeyArr = [];
                    let trDataKey = _tr[j].getAttribute("data-key");
                    let _divBox = _tr[j].querySelectorAll("div");
                    
                    for(let k = 0; k < _divBox.length; k++) {
                        let _val = Number(_divBox[k].querySelector("input").value);
                        datakeyArr.push(_val.toFixed(4));
                    }
                    data[trDataKey] = datakeyArr.join(",");
                }
                
            } else if(dataKey == "allowFC") {
                data[dataKey] = _td[i].querySelector("input").checked;
            } else if(dataKey == "chargesFlag") {
                data[dataKey] = _td[i].querySelector("input").checked;
            }else if(dataKey !== "limitDetail" && dataKey !== "sameAsAbove" && dataKey !== "action") {
                let _val = _td[i].querySelector("input").value;
                data[dataKey] = _val;
            }
        }

        // SET URL
        var _url = "";
        if(btnName == "editSingle") {
            _url = "editChargingDetail";
        } else if(btnName == "editAll") {
            _url = "editAllChargingDetail";
        }

        // AJAX REQUEST
        $.ajax({
            type  :   "POST",
            url   :   _url,
            data  :   data,
            success : function(data) {
                if(data.response !== null) {
                    alert(data.response);
                    getData();
                } else {
                    alert("Something went wrong !!");
                }
            },
            error : function(data) {
                alert("Something went wrong !!");
                $("body").addClass("loader--inactive");
            }
        });
    }
});

const editTDR = that => {
    that.closest('tr').classList.add("editable--active");
    let maxChargeTD = document.getElementsByClassName("max-charge");

    for(let i = 0; i < maxChargeTD.length; i++) {
        maxChargeTD[i].style.height = "44px";
    }
}

const merchantTotal = (that, decimatLimit) => {
    let val = that.value;

    if(!isNaN(val)) {
        let _tr = that.closest("tr");
        let _element = that.closest("div");
        let _index = Array.from(_tr.querySelectorAll('div')).indexOf(_element);

        const sum = (firstIndex, secondIndex, thirdIndex, fourthIndex) => {
            let sum = 0.0;
            let _firstValue = _tr.querySelectorAll("div")[firstIndex].querySelector("input").value;
            let _secondValue = _tr.querySelectorAll("div")[secondIndex].querySelector("input").value;
            let _thirdValue = _tr.querySelectorAll("div")[thirdIndex].querySelector("input").value;

            sum = Number(_firstValue) + Number(_secondValue) + Number(_thirdValue);
            // _tr.querySelectorAll("div")[thirdIndex].querySelector("span").innerHTML = sum.toFixed(decimatLimit);
            // _tr.querySelectorAll("div")[thirdIndex].querySelector("input").value = sum.toFixed(decimatLimit);

            if(decimalCount(sum) > decimatLimit) {
                sum = sum.toFixed(decimatLimit);
            }

            _tr.querySelectorAll("div")[fourthIndex].querySelector("span").innerHTML = sum;
            _tr.querySelectorAll("div")[fourthIndex].querySelector("input").value = sum;
        }

        if(_index == 0 || _index == 2 || _index == 4) {
            sum(0, 2, 4, 6); // 0 = 1st column, 2 = 3rd column, 4 = 5th column
        } else if(_index == 1 || _index == 3 || _index == 5) {
            sum(1, 3, 5, 7); // 1 = 2nd column, 3 = 4th column, 5 = 6th column
        }
    }
}