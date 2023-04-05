
function hideColumn(){
    var _userType = $("#userType").val();
    var _userLogin = $("#setSuperMerchant").val();
    var _isSuperMerchant = $("#isSuperMerchant").val();
    if(_userLogin == "true"){
        _isSuperMerchant = "Y"
    }
    console.log(_isSuperMerchant);
    var _table = $("#datatable").DataTable();
   
    if(_isSuperMerchant == "Y"){
        _table.columns(1).visible(true);
    }else{
        _table.columns(1).visible(false);
    }
    
}



$(document).ready(function(e){
    // ADD REMOVE SLIDE CLASS
    if($("#userMerchant").length > 0) {
        $("#txn-pgRefNum").removeClass("slide-form-element");
    }

    function format ( d ) {
        // `d` is the original data object for the row
            return '<div class="main-div">'+
                '<div class="inner-div">'+
                    '<span>Txn Id</span>'+
                    '<span>'+d.transactionIdString+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Sub Merchant</span>'+
                    '<span>'+d.subMerchantId+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Settled Date</span>'+
                    '<span>'+d.dateFrom+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>RRN</span>'+
                    '<span>'+d.rrn+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>MopType</span>'+
                    '<span>'+d.mopType+'</span>'+
                '</div>'+            
                '<div class="inner-div">'+
                    '<span>Card Mask</span>'+
                    '<span>'+d.cardNumber+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Cust Name</span>'+
                    '<span>'+d.customerName+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Cardholder Type</span>'+
                    '<span>'+d.cardHolderType+'</span>'+
                '</div>'+ 
                '<div class="inner-div">'+
                    '<span>Txn Type</span>'+
                    '<span>'+d.txnType+'</span>'+
                '</div>'+ 
                '<div class="inner-div">'+
                    '<span>Status</span>'+
                    '<span>'+d.status+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Payment Region</span>'+
                    '<span>'+d.paymentRegion+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Base Amount</span>'+
                    '<span>'+d.amount+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>TDR / Surcharge</span>'+
                    '<span>'+d.tdr_Surcharge+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>GST</span>'+
                    '<span>'+d.gst_charge+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Merchant Amount</span>'+
                    '<span>'+d.totalAmtPayable+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Post Settled Flag</span>'+
                    '<span>'+d.postSettledFlag+'</span>'+
                '</div>'+
                '<div class="inner-div">'+
                    '<span>Part Settled Flag</span>'+
                    '<span>'+d.partSettle+'</span>'+
            '</div>'+ 
            '</div>';
            // document.querySelector(".selector")
        }
            

    // function for super merchant 
    $("#merchantReportPayId").on("change", function(e){
    var _merchant = $(this).val();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "POST",
            url: "getSubMerchantListByPayId",
            data: {"payId": _merchant},
            success: function(data){
                console.log(data);
                $("#subMerchant").html("");
                if(data.superMerchant == true){
                    $("#isSuperMerchant").val("Y");
                    var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
                    for(var i = 0; i < data.subMerchantList.length; i++){
                        _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
                    }
                    $("[data-id=submerchant]").removeClass("d-none");
                    $("#subMerchant option[value='']").attr("selected", "selected");
                    $("#subMerchant").selectpicker();
                    $("#subMerchant").selectpicker("refresh");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                }else{
                    $("#isSuperMerchant").val("N");
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    },500);
                    $("[data-id=submerchant]").addClass("d-none");
                    $("[data-id=deliveryStatus]").addClass("d-none");
                    $("[data-id=deliveryStatus]").val("");
                    $("#subMerchant").val("");
                }
            }
    });
})

    // function for datatable hide columnd

    $(".downloadData").on("click", function(e){
        var _text = $(this).text();
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
        if (transTo - transFrom > 61 * 86400000) {
            alert('No. of days can not be more than 60 days');
            $("body").addClass("loader--inactive");
            $('#dateFrom').focus();
            return false;
        }
        if(_text == "Download"){
            if($("[data-id=reportMerchant]").val() == ""){
                $("#reportMerchant").val("All");
            }else{
                $("#reportMerchant").val($("[data-id=reportMerchant]").val());
            }
            $("#reportSubMerchant").val($("[data-id=subMerchant]").val());
            //$("#reportPaymentMode").val($("[data-id=reportPaymentMode]").val());
           // $("#subMerchant").val($("[data-id=subMerchant]").val());
           $("#reportPaymentMethod").val($("[data-id=reportPaymentMethod]").val());
           $("#reportPgRefNum").val($("[data-id=reportPgRefNum]").val());
            $("#reportOrderId").val($("[data-id=reportOrderId]").val());
            $("#reportCurrency").val($("[data-id=reportCurrency]").val());
            $("#reportPostSettleFlag").val($("[data-id=reportPostSettleFlag]").val());
            $("#reportDateFrom").val($("[data-id=reportDateFrom]").val());
            $("#reportDateTo").val($("[data-id=reportDateTo]").val());
            
            $("#capturedDataDownload").submit();
        }else{
            var _getAllInput = document.querySelectorAll("input[data-id]");
            var _getAllSelect = document.querySelectorAll("select[data-id]");
            document.querySelector("body").classList.remove("loader--inactive");
            var _obj = {};
            console.log(_getAllInput);
            console.log(_getAllSelect);
            _getAllInput.forEach(function(index, array, element){
                _obj[index.getAttribute("data-id")] = index.value;
            })
            _getAllSelect.forEach(function(index, array, element){
                _obj[index.getAttribute("data-id")] = index.value;
            })
    
            if(_obj['reportMerchant'] == ""){
                _obj['reportMerchant'] = "ALL";
            }
            _obj['reportType'] = "saleCaptured";
            $.ajax({
				type: "POST",
				url: "generateCustomCaptureFileAction",
				data: _obj,
				success: function(data){
					setTimeout(function(e){
						document.querySelector("body").classList.add("loader--inactive");
                        if(data.generateReport == true){
                            document.querySelector(".lp-success_generate").closest(".col-md-12").classList.remove("d-none");
                        }else{
                            document.querySelector(".lp-error_generate").closest(".col-md-12").classList.remove("d-none");
                        }
					}, 500)
					setTimeout(function(e){
						removeError()
					}, 4000);
				}
			})
        }
    })

    function removeError(){
		document.querySelector(".lp-error_generate").closest(".col-md-12").classList.add("d-none");
		document.querySelector(".lp-success_generate").closest(".col-md-12").classList.add("d-none");
	}

    // var today = new Date();
    // $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
    // $(".datepick").datepicker({
    //     prevText : "click for previous months",
    //     nextText : "click for next months",
    //     showOtherMonths : true,
    //     dateFormat : 'dd-mm-yy',
    //     selectOtherMonths : false,
    //     maxDate : new Date(),
    //     changeMonth: true,
    //     changeYear: true
    // });

    $(".viewData").on("click", function(e){
        $("body").removeClass("loader--inactive");
        handleChange();
        setTimeout(function(e){
            $("body").addClass("loader--inactive");
        }, 1500);
    })
    
    handleChange();

    function handleChange(){
        var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
        if (transFrom > transTo) {
            alert('From date must be before the to date');
            $("body").addClass("loader--inactive");
            $('#dateFrom').focus();
            return false;
        }
        if (transTo - transFrom > 31 * 86400000) {
            alert('No. of days can not be more than 31');
            $("body").addClass("loader--inactive");
            $('#dateFrom').focus();
            return false;
        }

        $("#datatable").dataTable({
            dom : 'BTftlpi',
            buttons: ['csv', 'print', 'pdf'],
            language: {
                search: "",
                searchPlaceholder: "Search records"
            },
            "ajax": {
                "type": "post",
                "url": "captureReportData",
                "data" : function (d){
                        return generatePostData(d);
                    }
                },
                "initComplete" : function(settings, json) {
                    $("#setSuperMerchant").val(json.flag);
                    // hideColumn();
                },

                    
                  "searching" :false,
                  "ordering" :false,
                  "destroy":true,
                  "processing" :true,
                  "serverSide" :true,


                //"destroy": true,
                "aoColumns": [
                    {
                        "mDataProp": "merchants",
                        "className": "my_class"
                    },
                {"mData" : "pgRefNum"},
                {"mData" : "orderId"}, 
                {"mData" : "transactionCaptureDate", "className": "text-center"}, 
                {"mData" : "paymentMethods"}, 
                {"mData" : "totalAmount"},
               
                        
            ]
        });

    }

    $("body").on("click", "#datatable tbody td", function(e){
		var table = new $.fn.dataTable.Api('#datatable');
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
    


    // variable sent to backend function
    function generatePostData(d) {

        var currency = document.getElementById("currency").value;
        var paymentMethod = document.getElementById("paymentMethod").value;
        var postSettleFlag = document.getElementById("postSettleFlag").value;
        var payId = $("#merchantReportPayId").val();
        var _subMerchant = $("#subMerchant").val();

        if(paymentMethod == '') {
            paymentMethod = 'ALL';
        }
        if(currency == '') {
            currency = 'ALL';
        }
        var obj = {
            payId : payId,
            subMerchantPayId : $("#subMerchant").val(),
            transactionId : document.getElementById("pgRefNum").value,
            orderId : document.getElementById("orderId").value,
            paymentType : paymentMethod,
            currency : currency,
            postSettleFlag : postSettleFlag,
            dateFrom : $("#dateFrom").val(),
            dateTo : $("#dateTo").val(),
            draw : d.draw,
            length : d.length,
            start : d.start,
            token : $("[name=token]").val(),
            "struts.token.name" : "token",

           

        };
        return obj;
    }

})


function dateBaseDownload(){

	var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
	var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    
	if (transTo - transFrom > 30 * 86400000) {
		if(checkBlankPgRefOrderId(['#pgRefNum', '#orderId'])){
			document.querySelector("#downloadButton").innerText = "Generate";
		}else{
			document.querySelector("#downloadButton").innerText = "Download";
		}
	}else{
		document.querySelector("#downloadButton").innerText = "Download";
	}

}

function checkBlankPgRefOrderId(_selector){
	var _val = _selector;
	var _checkBoolean = true;
	for(var i = 0; i < _val.length; i++){
		var _isValue = document.querySelector(_val[i]).value;
		if(_isValue != ""){
			_checkBoolean = false;
			break;
		} 
	}
	return _checkBoolean;
}