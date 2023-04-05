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

function removeErrorLink(e){
    e.closest(".single-account-input").classList.remove("hasError");
}

// letters and alpabet
function lettersAndAlphabet(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// create function for link
function sendLink(_that){
    var _linkThroug = _that.getAttribute("data-info");
    var _obj = {};
    var _selectInput = document.querySelectorAll("[data-link]");
    _selectInput.forEach(function(index, array, element){
        if(index.value != ""){
            _obj[index.getAttribute("data-link")] = index.value;
        }else if(index.value == "" && index.closest(".d-none") == null){
            index.closest(".single-account-input").querySelector(".error-field").innerText = "Should not be blank";
            index.closest(".single-account-input").classList.add("hasError");
        }
    })
    
    var _checkError = document.querySelector(".hasError");
    if(_checkError == null){
        _obj['linkThrough'] = _linkThroug;
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type: "POST",
            url: "eNachMandateThroughLinkAction",
            data: _obj,
            success: function(data){
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.responseMessage);
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 1000)
            }
        })
    }
}

// enable/disabled button on condition
function enableDisabledButton(_that) {
    // var _checkWhatChange = _that.getAttribute("name");
    // if(_that.value != "" && _that.closest(".hasError") == null){
    //     if(_checkWhatChange == "custEmailId"){
    //         document.querySelector("[data-info='email']").disabled = false;
    //     }
    //     if(_checkWhatChange == "custMobile"){
    //         document.querySelector("[data-info='sms']").disabled = false;
    //     }
    // }else{
    //     document.querySelector("[data-connect='"+_checkWhatChange+"']").disabled = true;
    // }

    // if(document.querySelector("[name='custMobile']").value != "" && document.querySelector("[name='custEmailId']").value != "" && document.querySelector(".hasError") == null){
    //     document.querySelector("[data-info='both']").disabled = false;
    // }else{
    //     document.querySelector("[data-info='both']").disabled = true;
    // }


    var mandateLinkFields = document.querySelectorAll(".single-account-input:not(.d-none)"),
        count = 0;
    
    mandateLinkFields.forEach(function(element) {
        var field = element.querySelector(".mandate-link-field");

        if(field.tagName == "SELECT" || field.tagName == "INPUT") {
            if(field.value !== "") {
                count++;
            }
        }
    });

    if(count == mandateLinkFields.length) {
        $(".btn-mandate-link").attr("disabled", false);
    } else {
        $(".btn-mandate-link").attr("disabled", true);
    }
}

$(document).ready(function(e){

    

    $("body").on("click", "#edit-row", function(e){
        $(this).closest("tr").addClass("edit-row");
        var _debitValue = $(this).closest("tr").find(".debit-column").text();
        var _selectpicker = document.querySelector("[data-duration='debitDuration']").innerHTML;
        var _createSelect = document.createElement("select");
        var _createAttr = document.createAttribute("id");
        _createAttr.value = "debitEditDuration";
        _createSelect.setAttributeNode(_createAttr);
        _createSelect.innerHTML = _selectpicker;
        
        $(".edit-row").find(".debit-column").append(_createSelect);
        $("#debitEditDuration").selectpicker();
        $('#debitEditDuration').find('[value=ALL]').remove();
        $("#debitEditDuration").selectpicker('refresh');
        $("#debitEditDuratioin").selectpicker('val', _debitValue);

    })

    $("body").on("click", "#save-row", function(e){
        var _payId = $(this).closest("tr").find(".PayId").text();
        var _debitDuration = $(this).closest("tr").find("#debitEditDuration").val();
        console.log(_payId);
        console.log(_debitDuration);
        document.querySelector("body").classList.remove("loader--inactive");
        $(".edit-row").find(".bootstrap-select").remove()
        $("#debitEditDuration").remove();
        $(this).closest("tr").removeClass("edit-row");
        $.ajax({
            type: "post",
            url: "editDebitDuration",
            data: {
                "merchantPayId" : _payId,
                "debitDuration" : _debitDuration
            },
            success: function(data){
                if(data.responseCode == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.response);
                setTimeout(function(){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            }
        })
    })

    $("body").on("click", "#cancel-row", function(e){
        $(".edit-row").find(".bootstrap-select").remove()
        $("#debitEditDuration").remove();
        $(this).closest("tr").removeClass("edit-row");
    })

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
    document.querySelector("#merchantDebit").addEventListener("change", function(e){
		getSubMerchant(e, "getSubMerchantList", {
			isSuperMerchant : true
		}, "ALL", "ALL");
	});

    $("#datatable").dataTable();
    
      
    // dataTable("#datatable");

    function classToggle(_selector){
        console.log(_selector);
        document.querySelector(".debit_popup_container").classList[_selector]("d-none");
        document.querySelector(".button-wrapper-debit").classList[_selector]("d-none");
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

    

    // tab creation 
    $(".lpay-nav-link").on("click", function(e){
        var _this = $(this).attr("data-id");
        $(".lpay-nav-item").removeClass("active");
        $(this).closest(".lpay-nav-item").addClass("active");
        $(".lpay_tabs_content").addClass("d-none");
        $("[data-target="+_this+"]").removeClass("d-none");
    })

    function showTab(){
        var _quickLinks = document.querySelector("[data-id='quickLinks']");
        if(_quickLinks == null){
            document.querySelector("[data-id='registrationReport']").closest("li").classList.add("active");
            document.querySelector("[data-target='registrationReport']").classList.remove("d-none");
        }else{
            document.querySelector("[data-id='quickLinks']").closest("li").classList.add("active");
            document.querySelector("[data-target='quickLinks']").classList.remove("d-none");
        }
    }

    showTab();

    var _height = document.querySelector(".heading-div").clientHeight;


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

    document.querySelector("#merchantTransaction").addEventListener("change", function(_this){
		getSubMerchant(_this, "getSubMerchantList", {
            isSuperMerchant : true
        }, "ALL", "ALL");
    });

    if(document.querySelector("#merchantDebitDuration") !== null) {
        document.querySelector("#merchantDebitDuration").addEventListener("change", function(_this){
            getSubMerchant(_this, "getSubMerchantList", {
                isSuperMerchant : true
            }, "ALL", "ALL");
        });
    }
    
    if(document.querySelector("#merchantLink") != null){
        document.querySelector("#merchantLink").addEventListener("change", function(_this){
            getSubMerchant(_this, "getSubMerchantList", {
                isSuperMerchant : true
            }, "Select Sub-Merchant", "");
        })
    }

    




    function format ( d ) {
    // `d` is the original data object for the row
    d.button = function(e) {

        if(d.status == "Processing") {
            return "<button class='lpay_button lpay_button-md lpay_button-secondary checkStatus'>Get Status</button>"
        } else if (d.status == "Failure") {
            return ""
        }

    }

    _new = "<div class='main-div'>";

    if(_check == "txnResultDataTable"){
        var _obj = {
           
            "pay_id": {"payId": "Pay ID","className" : "payId"},
            "subMerchantName" : "Sub Merchant Name",
            "createDate": "Registration Date",
            "accountHolderName": "Cust Name",
            
            "custPhone": "Cust Mobile",
            "custEmail": "Cust Email",
            "paymentType": "Mode",
            "bankName": "Bank Name",
            "accountNumber": "Account Number",
            "mopType": "Mop Type",
            "frequency": "Frequency",
            "tenure": "Tenure",
            "amount_simple": {"amount":"Amount","className": "amount"},
            "startDate": "Debit Start Date",
            "endDate": "Debit End Date",
            "totalAmount": "Total Amount",
            "acquirerCharges" : "Acquirer Charges",
            "responseMessage" : "Response Message"
       }   
    }

    if(_check == "transactionReport"){
        var _obj = {
           
            "pay_id": {"payId": "Pay ID","className" : "payId"},
            "subMerchantName" : "Sub Merchant Name",
            "createDate": "Create Date",
            "debitDate" : "Debit Date",
            "dueDate" : "Due Date",
            "accountHolderName": "Cust Name",
            "custPhone": "Cust Mobile",
            "custEmail": "Cust Email",
            "paymentType": "Mode",
            "bankName": "Bank Name",
            "accountNumber": "Account Number",
            "mopType": "Mop Type",
            "frequency": "Frequency",
            "tenure": "Tenure",
            "amount_simple": {"amount":"Amount","className": "amount"},
            "startDate": "Debit Start Date",
            "endDate": "Debit End Date",
            "totalAmount": "Total Amount"
        
    
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
    _new += '<div class="inner-div" style="width: 100%;text-align: center;display: none;">'+
    '<span></span>'+'<span class="statusHash">'+d.statusEnquiryHash+'</span>'+'</div>';

    _new += '<div class="inner-div" style="width: 100%;text-align: center">'+
        '<span></span>'+'<span>'+d.button()+'</span>'+'</div>';

    _new += "</div>";

    return _new;
        
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
    
    function renderTable() {
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('[data-var="fromDate"]').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('[data-var="toDate"]').val());
        
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            return false;
        }

        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            return false;
        }

        $("body").removeClass("loader--inactive");

        $('#txnResultDataTable').dataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "destroy": true,
            "ajax" : {

                "url" : "eNachRegistrationDetailsAction",
                "type" : "POST",
                "data" : function(d) {
                    return generatePostData(d, "[data-var]");
                }
            },
            "fnDrawCallback" : function(settings, json) {
                $("body").addClass("loader--inactive");
            },
            "aoColumns": [
                { "data": "merchantName" },
                { "data": "orderId", "className" : "orderId" },
                { "data": "pgRefNum" },
                { "data": "umrnNumber" },
                { "data": "maxAmount" },
                { 
                    "data": null,
                    "mRender" : function(row){
                        if(row.status == "Cancelled"){
                            return "<span class='common-status rejected-status'>"+row.status+"</span>"
                        }else if(row.status == "Pending"){
                            return "<span class='common-status pending-status'>"+row.status+"</span>" 
                        }else if(row.status == "Failed"){
                            return "<span class='common-status terminated-status'>"+row.status+"</span>" 
                        }else if(row.status == "Captured"){
                            return "<span class='common-status active-status'>"+row.status+"</span>"
                        }
                    }
                },
                { 
                    "data" : null,
                    "mRender" : function(row){
                    	 if(row.status == "Captured" || row.status == "Failed"){
                             return "NA"
                         }else{
                        	 return "<button class='copy-btn' data-link='"+row.eMandateUrl+"'><i class='fa fa-clone' aria-hidden='true'></i> Copy Link</button>";
                         }
                    }
                },
                { 
                    "data" : null,
                    "mRender" : function(row){
                        if(row.status == "Captured" || row.status == "Failed"){
                            return "NA"
                        }else{
                            return "<button class='act-btn'>Resend Link</button>";
                        }
                    }
                }
            ]
        });

    }

    var _temp = $("<input>");

    $("body").on("click", ".copy-btn", function(e){
        $("body").append(_temp);
        var _url = $(this).attr("data-link");
        _temp.val(_url).select();
        document.execCommand("copy");
        $(".copy-btn").html("<i class='fa fa-clone' aria-hidden='true'></i> Copy Link");
        $(this).html("<i class='fa fa-clone' aria-hidden='true'></i> Copied");
        _temp.remove();
    })

    $("body").on("click", ".act-btn", function(e){
        var table = new $.fn.dataTable.Api('#txnResultDataTable');
		var _getClosestTr = $(this).closest("tr");
		var _data = table.rows(_getClosestTr).data();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "reSendEMandateLink",
            data: {
                "orderId" : _data[0]['orderId'],
                "pgRefNum" : _data[0]['pgRefNum']
            },
            success: function(data){
                console.log(data);
                if(data.response == "success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".responseMsg").text(data.responseMessage);
                $(".lpay_popup").fadeIn();
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500)
            }
        })
    })

    function transactionResult() {
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('[data-transaction="fromDate"]').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('[data-transaction="toDate"]').val());
        
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            return false;
        }

        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            return false;
        }

        $("body").removeClass("loader--inactive");

        $('#transactionReport').dataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "destroy": true,
            "ajax" : {

                "url" : "eNachTransactionDetailsAction",
                "type" : "POST",
                "data" : function(d) {
                    return generatePostData(d, "[data-transaction]");
                }
            },
            "fnDrawCallback" : function(settings, json) {
                $("body").addClass("loader--inactive");
            },
            "aoColumns": [

                { "data": "merchantName" },
                { "data": "orderId", "className" : "orderId" },
                { "data": "pgRefNum","className" : "pgRefNum" },
                { "data": "umrnNumber" },
                { "data": "maxAmount" },
                { "data": "status" },
                /* { "data": "dateFrom" },
                { "data": "dateTo" } */
            ]
        });

    }


    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        if(_currentTable != "debitDurationTabel" && _currentTable != "datatable" && _currentTable != "datatablePopup"){
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
                    row.child( format(row.data())).show();
                    row.child()[0].children[0].classList.add("active-row");
                    getAllData();
                    tr.addClass('shown');
                }
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

    renderTable();
    transactionResult();

    document.querySelector("#registrationSubmit").onclick = function(e){
        renderTable();
    }

    document.querySelector("#transactionSubmit").onclick = function(e){
        transactionResult();
    }

    function _error(_selector){
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('['+_selector+'="fromDate"]').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('['+_selector+'="toDate"]').val());
        
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            return false;
        }

        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            return false;
        }
    }

    function downloadInput(_selector){
        document.querySelector("#downloadForm").innerHTML = "";
        var _getAllInput = document.querySelectorAll(_selector);
        var _option = "";
        _getAllInput.forEach(function(index, element, array){
        var _new =  _getAllInput[element].closest(".col-md-3").classList;
        var _newVal = _new.toString().indexOf("d-none");
            if(_newVal == -1){
                _option += "<input type='hidden' name='"+_getAllInput[element].name+"' value='"+_getAllInput[element].value+"' />"; 
            }
        })
        if(_selector == "[data-var]"){
            var _checkError = _error("data-var");
            if(_checkError != false){
                document.querySelector("#downloadForm").action = "eNachRegistrationDetailsDownloadAction";
            }else{
                return false;
            }
        }else if(_selector == "[data-transaction]"){
            var _checkError2 = _error("data-transaction");
            if(_checkError2 != false){
                document.querySelector("#downloadForm").action = "eNachTransactionDetailsDownloadAction";
            }else{
                return false;
            }
        }
        document.querySelector("#downloadForm").innerHTML += _option;
        document.querySelector("#downloadForm").submit();
    }

    document.querySelector("#registrationDownload").onclick = function(e){
        downloadInput("[data-var]");
    }

    document.querySelector("#downloadTransaction").onclick = function(e){
        downloadInput("[data-transaction]");
    }

    

        
    function generatePostData(d, _selector) {

        var obj = {};

        var _getAllInput = document.querySelectorAll(_selector);
        _getAllInput.forEach(function(index, element, array){
        var _new =  _getAllInput[element].closest(".col-md-3").classList;
        var _newVal = _new.toString().indexOf("d-none");

        if(_newVal == -1 || index.value != ""){
            obj[_getAllInput[element].name] = _getAllInput[element].value
        }
        })

        obj.token = document.getElementsByName("token")[0].value;
        obj.draw = d.draw;
        obj.length = d.length;
        obj.start = d.start;
        obj["struts.token.name"] = "token";

        if(obj.merchantPayId == ""){
            obj.merchantPayId = "ALL"
        }

        if(obj.paymentType == ""){
            obj.paymentType = "ALL"
        }

        if(obj.currency == ""){
            obj.currency = "ALL";
        }

        return obj;
    }



    // action for get status
    $("body").on("click", ".checkStatus", function(e){
        var _orderId = $(this).closest("tr").prev("tr").find(".orderId").text();
        var _payId = $(this).closest("tr").find(".payId span + span").text();
        var _amount = $(this).closest("tr").find(".amount span + span").text();
        var _hash = $(this).closest("tr").find(".statusHash").text();
        var _pgRefNum = $(this).closest("tr").prev("tr").find(".pgRefNum").text();
        console.log(e);
        var _checkButton = e.target.innerText;
        if(_checkButton == "Stop"){
            var _url = "eNachRegistrationDeactivationAction"
        }else{
            var _url = "statusEnquiryDebitTransaction"
        }
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "post",
            url: _url,
            data: {
                "orderId" : _orderId,
                "merchantPayId" : _payId,
                "amount" : _amount,
                "hash" : _hash,
                "pgRefNumber" : _pgRefNum
            },
            success: function(data){
                console.log(data.response);
                if(data.response == "0300"){
                    setTimeout(function(e){
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                        $(".lpay_popup").fadeIn();
                        $(".responseMsg").text(data.responseMsg);
                        $("body").addClass("loader--inactive");
                    }, 2000);
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    $("body").addClass("loader--inactive");
                }
            }
        })
    })

   $("body").on("click", ".confirmButton", function(e){
        $(".lpay_popup").fadeOut();
        renderTable();
        if(document.querySelector("[data-target='debitDuration']") != null){
            var _checkDebitTab = document.querySelector("[data-target='debitDuration']").classList.toString();
            if(_checkDebitTab.indexOf("d-none") == -1){
                viewDebitDuration();
                $(".selectpicker").val('default');
                $(".selectpicker").selectpicker('refresh');
            }
        }
        var _checkLinkTab = document.querySelector("[data-target='quickLinks']").classList.toString();

        if(_checkLinkTab.indexOf("d-none") == -1){
            reset("quickLinks");
        }

        transactionResult()

    })

    //reset input

    function reset(_selector){
        var _parent = document.querySelector("[data-target='"+_selector+"']");
        var _input = _parent.querySelectorAll("input");
        _input.forEach(function(index, array, element){
            var _array = index.classList.toString();
            if(_array.indexOf("datepick") == -1){
                index.value = "";
            }
        })
        $(".selectpicker").val('default');
        $(".selectpicker").selectpicker('refresh');
        document.querySelector("[data-hide='subMerchant']").classList.add("d-none");
    }
    

    // $("body").on("clci")

    })
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
            { "data": "maxAmount" },
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
            { "data": "maxAmount" },
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
        console.log(_id);
        var _isSubMerchant = $("#subMerchant").closest("[data-target='subMerchant']").attr("class");
            var _table = $(_id).DataTable();
            if(_isSubMerchant.indexOf("d-none") != -1){
                _table.columns(1).visible(false);
            }else{
                _table.columns(2).visible(true);
            }
        document.querySelector("body").classList.add("loader--inactive");
        if(_id == "#datatablePopup"){
            var _subMerchant = $("#userType").val();
            console.log(_subMerchant);
            if(_subMerchant == "MERCHANT" || _subMerchant == "SUBUSER" || _subMerchant == "RESELLER" || _subMerchant == ""){
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
        var _data = document.querySelectorAll("[data-debit]");
        _data.forEach(function(index, array, element){
            _obj[index.getAttribute("data-debit")] = index.value;
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
        var _getAllInput = document.querySelectorAll("[data-debit]");
        _getAllInput.forEach(function(index, array, element){
            var _notVisible = index.closest(".d-none");
            if(_notVisible == null){
                _input += "<input type='hidden' name='"+index.getAttribute('data-debit')+"' value='"+index.value+"' />";
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
    // return false;
    document.querySelector("#downloadForm").submit();
}



document.querySelector("#view").onclick = function(e){
	createJson('data-rules', 'data-debit');
    dataTable("#datatable", "eNachRegistrationDetailsAction");
    // generateInputValue();
}

// debit duration function

function createVariable(){
    var _parent = document.querySelectorAll(".debit_duration-input [data-duration]");
    var _obj = {};
    _parent.forEach(function(index, array, element){
        var _checkNone = index.closest(".d-none");
        if(_checkNone == null || index.value != ""){
            var _getAttr = index.getAttribute("data-duration");
            _obj[_getAttr] = index.value;
            
        }
    })
    return _obj;
}


function saveDebitDuration(_selector) {
    var _json = createVariable();
    var _checkAll = true;
    for(key in _json) {
        if(_json[key] == "ALL"){
            _checkAll = false;
        }
    }
    if(_checkAll == true){
        document.querySelector("body").classList.remove("loader--inactive");
        $.ajax({
            type : "POST",
            url: "saveDebitDuration",
            data: _json,
            success: function(data) {
                if(data.responseCode == "success") {
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                $(".lpay_popup").fadeIn();
                $(".responseMsg").text(data.response);
                setTimeout(function(){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            }
        })
    } else {
        alert("Please select specific value you can't choose ALL");
    }
}

function viewDebitDuration(){
    var _json = createVariable();
    $('#debitDurationTabel').dataTable( {
        "destroy": true,
        "ajax" : {

            "url" : "viewDebitDuration",
            "type" : "POST",
            "data" : _json,
        },
        "fnDrawCallback" : function(settings, json) {
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500);
        },
        "aoColumns": [
            { "mData": "PAY_ID", "className" : "PayId" },
            { "mData": "MERCHANT_NAME" },
            { "mData": "SUB_MERCHANT_ID" },
            { "mData": "SUB_MERCHANT_NAME"},
            { "mData": "DEBIT_DAY", "className": "debit-column lpay_select_group"},
            { 
                "mData": null,
                "mRender" : function(row){
                        return "<div class='action-btn'><span class='edit-btn' id='edit-row'><i class='fa fa-pencil' aria-hidden='true'></i></span></div><div class='action-btn-edit'><span class='edit-btn' id='save-row'><i class='fa fa-check' aria-hidden='true'></i></span><span class='delete-btn' id='cancel-row'><i class='fa fa-times' aria-hidden='true'></i></span></div>"
                    
                }
            }
        ]
    });
}

viewDebitDuration();

function downloadDebitDuration(_selector){
    var _json = createVariable();
    document.querySelector("#downloadFormDebitDuration").innerHTML = "";
    for(key in _json){
        var _option = "<input type='hidden' name='"+key+"' value='"+_json[key]+"' />";
        document.querySelector("#downloadFormDebitDuration").innerHTML += _option;
    }
    document.querySelector("#downloadFormDebitDuration").submit();
}

// document.querySelector("#saveDebitDuration").onclick = function(e){
//     var _json = createVariable();
//     $.ajax({
//         type : "POST",
//         url: "saveDebitDuration",
//         data: _json,
//         success: function(data){
//             console.log(data);
//         }
//     })
// }

var amount = function(event, _this){
    var _val = _this.value;
    if(_val.length == 1){
        if(_val.indexOf("0") != -1){
            _this.value = _val.slice(0, _val.length-1);
        }
    }
    var regex = /[.]/g;
    var _getPeriod = _val.match(regex);
    if(_getPeriod != null){
        if(_getPeriod.length > 1){
            _this.value = _val.slice(0, _val.length-1);
        }
        var _getString = _val.slice(_val.indexOf("."));
        if(_getString.length > 3){
            _this.value = _val.slice(0, _val.length-1);
        }
    }
}