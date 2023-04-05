$(document).ready(function(e){

    function scrollTabel(){
        var _tableId;
		var _table = document.querySelector("#lpay_table_popup");
			var _html = '<button class="arrow arrow-left"><i class="fa fa-angle-left" aria-hidden="true"></i></button><button class="arrow arrow-right"><i class="fa fa-angle-right" aria-hidden="true"></i></button>';
				_table.classList.add("has-scroll");
				var _createDiv = document.createElement("div");
				var _divClass = document.createAttribute("class");
				_divClass.value = "table_arrow";
				_createDiv.setAttributeNode(_divClass);
				_table.appendChild(_createDiv);
				_table.querySelector(".table_arrow").innerHTML = _html;
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
                    
					var _currentTableId = e.target.closest(".lpay_table").children[0].id;
					move(e, _currentTableId);
				})
			})
        }

    scrollTabel();

    // merchant and submerchant call
    document.querySelector("#merchant").addEventListener("change", function(e){
		getSubMerchant(e, "getSubMerchantList", {
			isSuperMerchant : true
		});
	});

    $("#datatable").dataTable();
    
      
    // dataTable("#datatable");

    function classToggle(_selector){
        document.querySelector(".debit_popup_container").classList[_selector]("d-none");
        document.querySelector(".button-wrapper").classList[_selector]("d-none");
    }

    $("body").on("click", "#datatable tbody tr", function(e){
        if(e.target.localName != 'button'){
            document.querySelector("body").classList.remove("loader--inactive");    
            e.target.closest("tr").classList.add("edit-row");
            setTimeout(function(e){
                classToggle("remove");
                dataTable("#datatablePopup", "debitTransactionReportData");
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        }
    })

    $("body").on("click", ".close-btn", function(e){
        document.querySelector("body").classList.remove("loader--inactive");
        document.querySelector("#datatable .edit-row").classList.remove("edit-row");
        setTimeout(function(e){
            classToggle("add");
            document.querySelector("body").classList.add("loader--inactive");
        }, 500);
    });

    $("body").on('click', ".payNow", function(e) {        
        var _parent = $(this).closest("tr");
        var  _payId = _parent.find(".payId").val();
        if(_parent.find(".subMerchantPayId") != null){
            var _subMerchant = _parent.find(".subMerchantPayId").val();
        }else{
            var _subMerchant = null;
        }
        var _orderId = _parent.find(".orderId").text();
        var _pgRefNum = _parent.find(".pgRefNum").text();

        $("body").removeClass("loader--inactive");
        
        $.ajax({
            type: "POST",
            url : "debitTransactionSchedule",
            data: {
                "merchantPayId": _payId,
                "orderId": _orderId,
                "subMerchantPayId" : _subMerchant,
                "pgRefNumber": _pgRefNum
            },
            success: function(data) {

                console.log(data);

                var responseCode = data.responseCode,
                    responseIcon = "";

                if(responseCode == "0300") {
                    responseIcon = "success";
                } else if(responseCode == "0398") {
                    responseIcon = "pending";
                } else {
                    responseIcon = "failed";
                }
                
                $(".response-popup").removeClass("d-none");
                $(".response-icon").find("img").attr("src", "../image/"+ responseIcon +".png");
                $(".response-msg").text(data.response);

                $("body").addClass("loader--inactive");
            },
            error: function() {                
                $(".response-popup").removeClass("d-none");
                $(".response-icon").find("img").attr("src", "../images/failed.png");
                $(".response-msg").text("Something went wrong. Please try again!");
                $("body").addClass("loader--inactive");
            }
        });
    });

    $("body").on("click", "#btn-close-response", function(e) {
        e.preventDefault();

        $(".debit_popup_container").addClass("d-none");
        $(".button-wrapper").addClass("d-none");
        $(".response-popup").addClass("d-none");
    });
});

// remove error
function removeError(_that){
    _that.closest(".debit-report-input").classList.remove("hasError-class");
    if(_that.closest(".debit-report-input").querySelector(".error-class") != null){
        _that.closest(".debit-report-input").querySelector(".error-class").remove();
    }
}

// manage form via JSON
function customToggleClass(_selector, _closest, _work, _errorTarget, _msg){
    _selector.closest(_closest).classList[_work]("hasError-class");
    createErrorMsg(_selector, _closest, _errorTarget);
    _selector.closest(_closest).querySelector(_errorTarget).innerText = _msg;
}

function createErrorMsg(_selector, _closest, _errorClass){
    var _createSpan = document.createElement("span");
    var _createSpanAttr = document.createAttribute("class");
    _createSpanAttr.value = _errorClass.slice(1, _errorClass.length);
    _createSpan.setAttributeNode(_createSpanAttr);
    _selector.closest(_closest).appendChild(_createSpan);
}

function createJson(_parentSelector, _commonSelector){
    var _json = {};
    _selectorParent = document.querySelectorAll("["+_parentSelector+"]");
    _selectorParent.forEach((index, array, element) => {
        if(element[array].attributes[_parentSelector] != undefined){
            if(element[array].attributes[_commonSelector] != undefined){
                _json[element[array].attributes[_commonSelector].nodeValue] = {
                    text : element[array].attributes[_commonSelector].nodeValue,
                }
            }
            if(element[array].attributes["data-required"] != undefined){
                _json[element[array].attributes[_commonSelector].nodeValue].required = true;
                _json[element[array].attributes[_commonSelector].nodeValue].requiredMsg = "Should not blank"; 
            }
            if(element[array].attributes["data-reg"] != undefined){
                _json[element[array].attributes[_commonSelector].nodeValue].regular = true;
                _json[element[array].attributes[_commonSelector].nodeValue].regularMsg = "Invalid Value"; 
            }

        }
    });
    for(key in _json){
        var _selector = document.querySelector("[name='"+key+"']");
        var _errorClass = ".error-class";
        var _closestClass = ".debit-report-input";
        if(_json[key].required != undefined){
            if(_selector.value == ''){
                customToggleClass(_selector, _closestClass, "add", _errorClass, _json[key].requiredMsg);
                continue;
            }
        }
        if(_json[key].regular != undefined){
            var _newReg = new RegExp(_selector.attributes["data-reg"].nodeValue);
            if(_newReg.test(_selector.value) == false){
                customToggleClass(_selector, _closestClass, "add", _errorClass, _json[key].regularMsg);
                continue;
            }
        }
    }
}


function dataTable(_selector, _url){
    document.querySelector("body").classList.remove("loader--inactive");
    if(_selector == "#datatable"){
        var _obj = [
            { 
                "data": null,
                "mRender" : function(row){
                    return "<span>"+row.merchantName+"</span><input type='hidden' class='payId' value='"+row.payId+"' />"
                }
            },
            
            { 
                "data": null,
                "mRender" : function(row){
                    return "<span>"+row.subMerchantName+"</span><input type='hidden' class='subMerchantPayId' value='"+row.subMerchantPayId+"' />"
                }
            },
            { "data": "orderId", "className" : "orderId" },
            { "data": "pgRefNum", "className" : "pgRefNum" },
            { "data": "umrnNumber" },
            { "data": "createDate" },
            { "data": "totalAmount" },
            { 
                "data": null,
                "mRender" : function(row){
                    return "<button class='lpay_button lpay_button-md lpay_button-primary' onclick='createDownloadForm(this)'>Download</button>";
                }
            }
        ];
    }else{
        var _obj = [
            { 
                "data": null,
                "mRender" : function(row){
                    return "<span>"+row.merchantName+"</span><input type='hidden' class='payId' value='"+row.payId+"' />"
                }
            },
            
            { 
                "data": null,
                "mRender" : function(row){
                    return "<span>"+row.subMerchantName+"</span><input type='hidden' class='subMerchantPayId' value='"+row.subMerchantPayId+"' />"
                }
            },
            { "data": "orderId", "className" : "orderId" },
            { "data": "pgRefNum", "className" : "pgRefNum" },
            { "data": "regPgRefNum"},
            { "data": "umrnNumber"},
            { "data": "regDate" },
            { "data": "dueDate" },
            { "data": "accountHolderName" },
            { "data": "custEmail" },
            { "data": "custPhone" },
            { "data": "status" },
            { "data": "totalAmount" },
            { 
                "data": null,
                "mRender" : function(row) {
                    var isDisabled = 'disabled';
                    if(row.status == "Pending") {
                        isDisabled = '';
                    }

                    return "<button class='lpay_button lpay_button-md lpay_button-primary payNow' "+ isDisabled +">Pay Now</button>";
                }
            }
        ];
    }
    $(_selector).dataTable( {
        "ajax" : {

            "url" : _url,
            "type" : "POST",
            "data" : function(d) {
                return generateInputValue(d);
            }
        },
      "destroy" : true,
      "initComplete": function(settings, json) {
        return loaderInactive(settings._value, _selector);
      },
      "aoColumns": _obj,
    });
}

function loaderInactive(_value, _new){
    function newNumber(){
        var _id = _new;
        var _isSubMerchant = $("#subMerchant").closest("[data-target='subMerchant']").attr("class");
            console.log(_isSubMerchant);
            var _table = $(_id).DataTable();
            if(_isSubMerchant.indexOf("d-none") != -1){
                _table.columns(1).visible(false);
            }else{
                _table.columns(2).visible(true);
            }
        document.querySelector("body").classList.add("loader--inactive");
        if(_id == "#datatablePopup"){
            var _subMerchant = $("#userType").val();
            
            if(_subMerchant == "MERCHANT" || _subMerchant == "SUBUSER" || _subMerchant == "RESELLER"){
                _table.columns(13).visible(false);
            }
        }else{

        }
    }
    return newNumber();
}

function generateInputValue(d){
    var _obj = {};
    if(event == undefined){
        var _parent = document.querySelector(".edit-row");
        _obj["orderId"] = _parent.querySelector(".orderId").innerText;
        _obj["pgRefNum"] = _parent.querySelector(".pgRefNum").innerText;
    }else{
        var _data = document.querySelectorAll("[data-var]");
        _data.forEach(function(index, array, element){
            _obj[index.getAttribute("data-var")] = index.value;
        })
    }
    
    _obj['status'] = "Captured";

    console.log(_obj);

    return _obj;
}


function createDownloadForm(e){
    var _input = "";
    var _checkWhichClick = e.closest("tr");
    // console.log(_checkWhichClick);
    if(_checkWhichClick == null){
        document.querySelector("#downloadForm").setAttribute("action", "downloadDebitWithFilter");
        var _getAllInput = document.querySelectorAll("[data-var]");
        _getAllInput.forEach(function(index, array, element){
            var _notVisible = index.closest(".d-none");
            if(_notVisible == null){
                _input += "<input type='hidden' name='"+index.getAttribute('data-var')+"' value='"+index.value+"' />";
            }
        })
    }else{
        document.querySelector("#downloadForm").setAttribute("action", "downloadDebitIndividual");
        var _orderId = _checkWhichClick.querySelector(".orderId").innerText;
        var _merchantPayId = _checkWhichClick.querySelector(".payId").value;
        if(_checkWhichClick.querySelector(".subMerchantPayId") != null){
            var _subMerchant = _checkWhichClick.querySelector(".subMerchantPayId").value;
            _input += "<input type='hidden' name='subMerchantPayId' value='"+_subMerchant+"' />";
        }
        _input += "<input type='hidden' name='orderId' value='"+_orderId+"' />";
        _input += "<input type='hidden' name='merchantPayId' value='"+_merchantPayId+"' />";
    }
    _input += "<input type='hidden' name='status' value='Captured' />";
    document.querySelector("#downloadForm").innerHTML = _input;
    document.querySelector("#downloadForm").submit();
}



document.querySelector("#view").onclick = function(e){
	createJson('data-rules', 'data-var');
    dataTable("#datatable", "eNachRegistrationDetailsAction");
    // generateInputValue();
}