var id = document.getElementById.bind(document);

var surchargeDatatable1 = id("surchargeDatatable1");
var surchargeDatatable2 = id("surchargeDatatable2");
var serviceTaxTable = id("serviceTaxTable");
var tdrData = id("tdrData");

$(window).on("load", function(e){

    var _getHeading = $(".headingInfo").text();
    $(".headingInfo").each(function(e){
        var _this = $(this).text();
        var _getIndex = _this.indexOf(",");
        var _getExactText = _this.substring(0, _getIndex);
        $(this).text(_getExactText);
    })

})

$(document).ready(function() {

    var _onLoadTime = {
        "paymentOption" : true,
        "chargingDetails" : true,
        "bulkUpdate" : true,
    }

    var _allTabs = document.querySelectorAll(".lpay-nav-link");
    _allTabs.forEach(function(index, array, element){
        index.addEventListener('click', function(e){
            e.preventDefault();
            var _checkDiv = e.target.innerText;
            tabShow('[data-id="'+e.target.getAttribute("data-id")+'"]');
            if(_checkDiv == "Payment Options"){
                if(_onLoadTime['paymentOption'] == true){
                    requestLoads("#paymentOptions-table", "pendingRequestMerchantMapping", "paymentOptionData", _paymentOption, {
                        "dataFor" : "paymentOption"
                    });
                    _onLoadTime['paymentOption'] = false;
                }
            }else if(_checkDiv == "Charging Details"){
                if(_onLoadTime['chargingDetails'] == true){
                    requestLoads("#chargingDetails-table", "pendingRequestMerchantMapping", "tdrData", _chargingDetails, {
                        "dataFor" : "chargingDetails"
                    });
                    _onLoadTime['chargingDetails'] = false;
                }
            }else if(_checkDiv == "Bulk Charges Update"){
                if(_onLoadTime['bulkUpdate'] == true){
                    requestLoads("#bulkUpdate-table", "pendingRequestMerchantMapping", "bulkChargesData", _bulkChargesUpdate, {
                        "dataFor" : "bulkCharges"
                    });
                    _onLoadTime['bulkUpdate'] = false;
                }
            }
        })
    })

    function tabShow(_selector){
        var _allTabsContent = document.querySelectorAll(".lpay_tabs_content");
        var _activeTab = document.querySelector(_selector).getAttribute("data-id");
        _allTabsContent.forEach(function(index, array, element){
            index.classList.add("d-none");
        })
        _allTabs.forEach(function(index, array, element){
            index.closest("li").classList.remove("active");
        })
        document.querySelector(_selector).closest("li").classList.add("active");
        document.querySelector('[data-target='+_activeTab+']').classList.remove("d-none");
    }

    var _merchantMapping = [
        { "mData" : "businessName", "width" : "27%" },
        { "mData" : "acquirer", "width" : "10%" },
        { "mData" : "currency", "width" : "8%" },
        { "mData" : "createdDate", "width" : "10%" },
        { "mData" : "status", "width" : "8%" },
        { "mData" : "requestedBy", "width" : "10%" },
        { 
            "mData" : null,
            "width" : "22%",
            "className" : "button-div",
            "mRender" : function(row){
                return '<button class="reject_btn lp-reject_btn">Reject</button><button class="accept_btn lp-accept_btn">Accept</button>';
            }
        }
    ];
    var _paymentOption = [
        { "mData" : "merchantName", "width" : "25%" },
        { "mData" : "createdDate", "width" : "15%" },
        { "mData" : "status", "width" : "15%" },
        { "mData" : "requestedBy", "width" : "15%" },
        { 
            "mData" : null,
            "width" : "30%",
            "mRender" : function(row){
                return '<button class="reject_btn lp-reject_btn">Reject</button><button class="accept_btn lp-accept_btn">Accept</button>';
            }
        }
    ];
    var _chargingDetails = [
        { "mData" : "businessName" },
        { "mData" : "acquirerName" },
        { "mData" : "currency" },
        { "mData" : "createdDate" },
        { "mData" : "status" },
        { "mData" : "requestedBy" },
        { 
            "mData" : null,
            "width" : "20%",
            "mRender" : function(row){
                return '<button class="reject_btn lp-reject_btn">Reject</button><button class="accept_btn lp-accept_btn">Accept</button>';
            }
        }
    ]
    var _bulkChargesUpdate = [
        { "mData" : "businessName" },
        { "mData" : "acquirerName" },
        { "mData" : "currency" },
        { "mData" : "createdDate" },
        { "mData" : "status" },
        { "mData" : "requestedBy" },
        { 
            "mData" : null,
            "width" : "20%",
            "mRender" : function(row){
                return '<button class="reject_btn lp-reject_btn">Reject</button><button class="accept_btn lp-accept_btn">Accept</button>';
            }
        }
    ]
    var _reportingData = [
        { "mData" : "businessName", "width" : "15%" },
        { "mData" : "acquirer", "width" : "10%" },
        { "mData" : "createdDate", "width" : "8%" },
        { 
            "mData" : null, 
            "width" : "8%" ,
            "mRender" : function(row){
                if(row.status == "REJECTED" ){
                    return "<span class='rejected-status common-status'>"+row.status+"</span>";
                }else{
                    return "<span class='active-status common-status'>"+row.status+"</span>";
                }
            }
        },
        { "mData" : "requestedBy", "width" : "10%" },
        { "mData" : "updatedDate", "width" : "10%" },
        { "mData" : "processedBy", "width" : "10%" }
        
    ]
    var _reportingDataPayId = [
        { "mData" : "businessName", "width" : "15%" },
        { "mData" : "acquirerName", "width" : "10%" },
        { "mData" : "createdDate", "width" : "8%" },
        { 
            "mData" : null, 
            "width" : "8%" ,
            "mRender" : function(row){
                if(row.status == "REJECTED" ){
                    return "<span class='rejected-status common-status'>"+row.status+"</span>";
                }else{
                    return "<span class='active-status common-status'>"+row.status+"</span>";
                }
            }
        },
        { "mData" : "requestedBy", "width" : "10%" },
        { "mData" : "updatedDate", "width" : "10%" },
        { "mData" : "updateBy", "width" : "10%" }
    ]
    
    var _reportingDataPaymentOption = [
        { "mData" : "merchantName", "width" : "15%" },
        { "mData" : "merchantName", "width" : "10%" },
        { "mData" : "createdDate", "width" : "8%" },
        { 
            "mData" : null, 
            "width" : "8%" ,
            "mRender" : function(row){
                if(row.status == "REJECTED" ){
                    return "<span class='rejected-status common-status'>"+row.status+"</span>";
                }else{
                    return "<span class='active-status common-status'>"+row.status+"</span>";
                }
            }
        },
        { "mData" : "requestedBy", "width" : "10%" },
        { "mData" : "updatedDate", "width" : "10%" },
        { "mData" : "updateBy", "width" : "10%" }
    ]
    function requestLoads(_id, _url, _data, _dataLoad, requestFor ){

        document.querySelector("body").classList.remove("loader--inactive");
        $(_id).DataTable({

            "ajax" : {
                "type" : "POST",
                "url" : _url,
                "data": requestFor
            },
            "fnDrawCallback" : function(settings, json) {
                hideColumn();
                setTimeout(function(e){
                    
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "destroy" : true,
            "sAjaxDataProp" : _data,
            "aoColumns" : _dataLoad

        })

    }

    requestLoads("#merchantMapping-table", "pendingRequestMerchantMapping", "merchantMappingData", _merchantMapping, 
    { 
        "dataFor" : "merchantMapping" 
    });

 
    function hideColumn(){
        var _table = $("#pendingRequest-table").DataTable();
        var _checkReportType = $("#reportType").val();
        if(_checkReportType == "paymentOption"){
            _table.columns(1).visible(false);
        }else{
            _table.columns(1).visible(true);
        }
    }
    
    var _check = "";
    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var _reportyType = document.querySelector("#reportType").value;
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        _check = _currentTable;
		if(e.target.localName != "button" && e.target.localName != "input" && e.target.localName != "label"){
			var tr = $(this).closest('tr');
            var _trJava = e.target.closest("tr");
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
                if(_check == "paymentOptions-table" || _check == "merchantMapping-table" || _reportyType == "merchantMapping" || _reportyType == "paymentOption"){
                    var _createdData = ['payment_type', 'mop_type'];
                    for(var j = 0; j < _createdData.length; j++){
                        var _mopTypeArray = _trJava.nextSibling.querySelector("."+_createdData[j]).childNodes[1].innerText.split(",");
                        _trJava.nextSibling.querySelector("."+_createdData[j]).childNodes[1].innerHTML = "";
                        for(var i = 0; i < _mopTypeArray.length; i++){
                            var _mopType = "<div>"+_mopTypeArray[i]+"</div>";
                            _trJava.nextSibling.querySelector("."+_createdData[j]).childNodes[1].innerHTML += _mopType;
                        }
                    }

                }
				tr.addClass('shown');
			}
		}
		
	})


    function getAllData(){
		var _new = document.querySelectorAll(".inner-div");
		_new.forEach(function(index, array, element){
			var _getValue = _new[array].children[1].innerText;
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined' || _getValue == 'NA') {
				_new[array].classList.add("d-none");
			}
		})
	}

    $("body").on("click", ".accept_btn", function(e){
        $(this).closest("tr").addClass("active-tr");
        $(this).closest("tr").attr("data-text", $(this).text());
        $("#fancybox").fancybox({
            'overlayShow': true
        }).trigger('click');
    })

    $("body").on("click", ".reject_btn", function(e){
        $(this).closest("tr").addClass("active-tr");
        $(this).closest("tr").attr("data-text", $(this).text());
        $("#fancybox").fancybox({
            'overlayShow': true
        }).trigger('click');
    })

    var _merchantMappingRequestParam = ['acquirer', 'merchantId', 'mapString', 'merchantEmailId'];
    var _paymentOptionRequestParam = ['payId','paymentTypeString', 'mopTypeString'];
    var _chargingPlatformRequestParam = ['payId', 'idString', 'aquirerName'];
    var _bulkChargesUpdateParam = ['id', 'paymentType', 'paymentsRegion'];

    function updateRequests(_this, requestParam, _url, _text){
        document.querySelector("body").classList.remove("loader--inactive");
        var _token = document.getElementsByName("token")[0].value;
        var _tableId = _this.closest("table").getAttribute("id");
        var _tableData = new $.fn.dataTable.Api('#'+_tableId);
        var _tableRowData = _this;
        var _getRowData = _tableData.rows(_tableRowData).data();
        var _obj = {};
        for(var i = 0; i < requestParam.length; i++){
            _obj[requestParam[i]] = _getRowData[0][requestParam[i]];
        }
        _obj['operation'] = _text;
        _obj['token'] = _token;
        _obj['struts.token.name'] = "token";
        $.ajax({
            type: "POST",
            url: _url,
            data: _obj,
            success: function(data){
                $(".responseMsg").text(data.response);
                $(".confirmButton").attr("data-table", _tableId);
                if(data.responseStatus == "Success"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                document.querySelector("body").classList.add("loader--inactive");
                $(".lpay_popup").fadeIn();
            }
        })
    }



    $("body").on("click", "#confirm-btn", function(e){
        var _tableId = document.querySelector(".active-tr");
        var _table = _tableId.closest("table").getAttribute("id");
        var _text = _tableId.getAttribute("data-text");
        if(_table == "merchantMapping-table"){
            updateRequests(_tableId, _merchantMappingRequestParam, "updateBankMapping", _text);
        }else if (_table == "paymentOptions-table"){
            updateRequests(_tableId, _paymentOptionRequestParam, "updatePaymentOptions", _text);
        }else if(_table == "chargingDetails-table"){
            updateRequests(_tableId, _chargingPlatformRequestParam, "updateBankTDR", _text);
        }else if(_table == "bulkUpdate-table"){
            updateRequests(_tableId, _bulkChargesUpdateParam, "bulkUpdateChargesForPending", _text);
        }
        $.fancybox.close();
    })

    $("body").on("click", "#cancel-btn-fancy", function(e){
        $(".active-tr").removeAttr("data-text");
        $(".active-tr").removeClass();
        $.fancybox.close();
    })

    $(".confirmButton").on("click", function(e){
        var _tableName = $(this).attr("data-table");
        var tableObj = $('#'+_tableName);
        var table = tableObj.DataTable();
        table.ajax.reload();
		$(".lpay_popup").fadeOut();
	})

    function format ( d ) { 

        var _getReportName = document.querySelector("#reportType").value;

        if(_check == "chargingDetails-table" || _getReportName  == "chargingDetails"){

            var _rcFixCharged = d['resellerFixChargeString'].split(",");
            var _mrTdr = d['merchantTdrString'].split(",");
            var _mrFixCharged = d['merchantFixChargeString'].split(",");
            var _rcTdr = d['resellerTDRString'].split(",");
            var _bcFixCharged = d['bankFixChargeString'].split(",");
            var _bcTdr = d['bankTDRString'].split(",");
            var _slabs = d['slabString'].split(",");

        }

        if(_check == "bulkUpdate-table" || _getReportName == "bulkCharges"){
            var _bulkUpdateTable = "<div class='lp-bulk_table mt-20 w-100'><table class='w-100 bulk-table'><thead><tr><th>Brand Card</th><th>Slab</th><th>Mop Type</th><th>Merchant TDR</th><th>Merchant FC</th><th>Bank TDR</th><th>Bank FC</th><th>Reseller TDR</th><th>Reseller FC</th><th>Max Charge Merchant</th><th>Max Charge Acquirer</th><th>Allow FC</th><th>Higher Charge</th></tr></thead><tbody>";
            var _bulk = d['allChargingDetail'].split(";");
            for(var i = 0; i < _bulk.length-1; i++){
                var _bulkArray = _bulk[i].split(",");
                _bulkUpdateTable += "<tr><td>"+_bulkArray[0]+"</td><td>"+_bulkArray[1]+"</td><td>"+_bulkArray[18]+"</td><td>"+_bulkArray[2]+"</td><td>"+_bulkArray[3]+"</td><td>"+_bulkArray[4]+"</td><td>"+_bulkArray[5]+"</td><td>"+_bulkArray[6]+"</td><td>"+_bulkArray[7]+"</td><td>"+_bulkArray[11]+"</td><td>"+_bulkArray[12]+"</td><td>"+_bulkArray[9]+"</td><td>"+_bulkArray[10]+"</td></tr>"
            }
            _bulkUpdateTable += "</tbody></table></div>";
        }


        if(_check == "chargingDetails-table" || _getReportName == "chargingDetails"){
            var _obj = {
                "paymentType" : "Payment Type",
                
                "acquiringMode" : "Acquiring Mode",
                "paymentsRegion" : "Payment Region",

            }
        }else if(_check == "merchantMapping-table"){
            var _obj = {
                "paymentType" : {
                    "paymentTypeString" : "Payment Type",
                    "className" : "w-100 payment_type"
                },
                "mopType" : {
                    "mopTypeString" : "Mop Type",
                    "className" : "w-100 mop_type"
                },
                "passwordDiv" : {
                    "password" : "Password",
                    "className" : "w-100 password"
                },
                "txnDiv" : {
                    "txnKey" : "Txn Key",
                    "className" : "w-100"
                },
                "merchantId" : "Merchant Id",
                "adf1" : "ADF 1",
                "adf2" : "ADF 2",
                "adf3" : "ADF 3",
                "adf4" : "ADF 4",
                "adf5" : "ADF 5",
                "adf6" : "ADF 6",
                "adf7" : "ADF 7",
                "adf8" : "ADF 8",
                "adf9" : "ADF 9",
                "adf10" : "ADF 10",
                "adf11" : "ADF 11"
            }
        }else if(_check == "bulkUpdate-table" || _getReportName == "bulkCharges"){
            var _obj = {
                "paymentType" : "Payment Type",
                "acquiringMode" : "Acquiring Mode",
                "paymentsRegion" : "Payment Region",
            }

        } else{
            var _obj = {
                "paymentType" : {
                    "paymentTypeString" : "Payment Type",
                    "className" : "w-100 payment_type"
                },
                "mopType" : {
                    "mopTypeString" : "Mop Type",
                    "className" : "w-100 mop_type"
                }
            }
        }
    

        _new = "<div class='main-div'>";
        
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

        if(_check == "chargingDetails-table" || _getReportName == "chargingDetails"){
            _new += "<table class='w-100 bulk-table' style='margin-top: 15px'><thead><tr><th>Slabs</th><th>Merchant TDR</th><th>Merchant FC</th><th>Bank TDR</th><th>Bank FC</th><th>Reseller TDR</th><th>Reseller FC</th><th>Max Charge Merchant</th><th>Max Charge Acquirer</th></tr></thead><tbody><tr><td>"+_slabs[0]+"</td><td>"+_mrTdr[0]+"</td><td>"+_mrFixCharged[0]+"</td><td>"+_bcTdr[0]+"</td><td>"+_bcFixCharged[0]+"</td><td>"+_rcTdr[0]+"</td><td>"+_rcFixCharged[0]+"</td><td>"+d['maxChargeMerchant']+"</td><td>"+d['maxChargeAcquirer']+"</td></tr><tr><td>"+_slabs[1]+"</td><td>"+_mrTdr[1]+"</td><td>"+_mrFixCharged[1]+"</td><td>"+_bcTdr[1]+"</td><td>"+_bcFixCharged[1]+"</td><td>"+_rcTdr[1]+"</td><td>"+_rcFixCharged[1]+"</td><td>"+d['maxChargeMerchant']+"</td><td>"+d['maxChargeAcquirer']+"</td></tr><tr><td>"+_slabs[2]+"</td><td>"+_mrTdr[2]+"</td><td>"+_mrFixCharged[2]+"</td><td>"+_bcTdr[2]+"</td><td>"+_bcFixCharged[2]+"</td><td>"+_rcTdr[2]+"</td><td>"+_rcFixCharged[2]+"</td><td>"+d['maxChargeMerchant']+"</td><td>"+d['maxChargeAcquirer']+"</td></tr></tbody></table>";
        }

        if(_check == "bulkUpdate-table" || _getReportName == "bulkCharges"){
            _new += _bulkUpdateTable
        }

         
        _new += "</div>";
    
        return _new;
       
    }


    
    $("#pendingRequest-table").dataTable();



    $("#reportType, #reportStatus").on("change", function(e){
        var _reportStatus = document.querySelector("#reportStatus").value;
        var _reportType = document.querySelector("#reportType").value;
        if(_reportStatus != "" && _reportType != ""){
            if(_reportType == "merchantMapping"){
                requestLoads("#pendingRequest-table", "pendingRequestMerchantMapping", "reportData", _reportingData, 
            { 
                "dataFor" : "reporting",
                "reportType" : _reportType,
                "reportStatus" : _reportStatus  
            }); 
            }else if(_reportType == "paymentOption"){
                requestLoads("#pendingRequest-table", "pendingRequestMerchantMapping", "reportData", _reportingDataPaymentOption, 
            { 
                "dataFor" : "reporting",
                "reportType" : _reportType,
                "reportStatus" : _reportStatus  
            });
            }else{
                requestLoads("#pendingRequest-table", "pendingRequestMerchantMapping", "reportData", _reportingDataPayId, 
                { 
                    "dataFor" : "reporting",
                    "reportType" : _reportType,
                    "reportStatus" : _reportStatus  
                });
            }
        }
    })
    
});

