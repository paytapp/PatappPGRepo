function hideColumn() {
    var _getMerchant = $("#setData").val();
    var _userType = $("#userType").val();
    console.log(_userType);
    var td = $("#txnResultDataTable").DataTable();
    td.columns(8).visible(false);
    if(_userType == "ADMIN" || _userType == "SUBADMIN"){
        td.columns(8).visible(true);
    }
    if(_getMerchant == "" || _getMerchant == "NA") {
        td.columns(3).visible(false);
    } else {
        td.columns(3).visible(true);
    }
}

function format ( d ) {
    
    // `d` is the original data object for the row
	
	d.new = function(){
        let _button = '';
        if(d.txnType === "SALE" && (d.status !== "Pending")){
            _button += '<button class="lpay_button lpay_button-md lpay_button-primary callback-btn" style="font-size:10px;">Send Callback</button>';
        }
        if(d.txnType === "SALE" && (d.status !== "Pending" && d.status !== "Enrolled" && d.status !== "Captured")){
            _button += '<button class="lpay_button lpay_button-md lpay_button-primary status-btn" style="font-size:10px;">Status</button>';
        }
        return _button;
    }
	
        return '<div class="main-div">'+
            '<div class="inner-div">'+
                '<span>Sub Merchant ID</span>'+
                '<span>'+d.subMerchantId+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>PG REF Number</span>'+
                '<span>'+d.pgRefNum+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Cust Email</span>'+
                '<span>'+d.customerEmail+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Acquirer ID</span>'+
                '<span>'+d.acqId+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>RRN</span>'+
                '<span>'+d.rrn+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Mask</span>'+
                '<span>'+d.cardNumber+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Transaction Flag</span>'+
                '<span>'+d.txnSettledType+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Refund Order ID</span>'+
                '<span>'+d.refundOrderId+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
                '<span>Payment Region</span>'+
                '<span>'+d.paymentRegion+'</span>'+
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
                '<span>Transaction Mode</span>'+
                '<span>'+d.transactionMode+'</span>'+
            '</div>'+
            // '<div class="inner-div">'+
            //     '<span>Status</span>'+
            //     '<span>'+d.status+'</span>'+
            // '</div>'+
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
	            '<span>PG Response Message</span>'+
	            '<span>'+d.responseMessage+'</span>'+
            '</div>'+
            '<div class="inner-div">'+
		        '<span>Acquirer Response Message</span>'+
		        '<span>'+d.accqResponseMessage+'</span>'+
		    '</div>'+
		    '<div class="inner-div" style="width: 100%;text-align: center">'+
	            '<span></span>'+
	            '<span>'+d.new()+'</span>'+
            '</div>'+
        '</div>';
        // document.querySelector(".selector")
    }



$(document).ready(function() {
    // DISPLAY SUB MERCHANT
    // $('[data-id="subMerchant"]').prepend('<option value="ALL" selected="selected">ALL</option>');
	// $('[data-id="subMerchant"]').selectpicker("refresh");

    $("#merchant").on("change", function(e) {
        var _merchant = $(this).val();
        if(_merchant != "") {
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "POST",
                url: "getSubMerchantList",
                data: {"payId": _merchant},
                success: function(data) {						
                    $("#subMerchant").html("");
                    if(data.superMerchant == true){
                        var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
                        for(var i = 0; i < data.subMerchantList.length; i++) {
                            _option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
                        }

                        $("[data-id='submerchant']").removeClass("d-none");
                        $("#subMerchant option[value='']").attr("selected", "selected");
                        $("#subMerchant").selectpicker();
                        $("#subMerchant").selectpicker("refresh");
                        setTimeout(function(e){
                            $("body").addClass("loader--inactive");
                        },500);
                    } else {
                        setTimeout(function(e){
                            $("body").addClass("loader--inactive");
                        },500);

                        $("[data-id='submerchant']").addClass("d-none");
                        $("#subMerchant").val("");
                    }
                }
            });
        }else{
            $("[data-id='submerchant']").addClass("d-none");
            $("#subMerchant").val("");	
        }
    });
        
    renderTable();

    $("#submit").click(function(env) {
        var _pgRef = document.querySelector("#pgRefNum").value;
        if(_pgRef != ""){
            var letters = /^[0-9]+$/;
            var _match = letters.test(_pgRef);
            if(_match == true){
                
            }else{
                alert("Please enter valid PG REF Number")
                return false;
            }
            
        }
        $("body").removeClass("loader--inactive");
        $("#setData").val("");
        reloadTable();		
    });

    $('#closeBtn').click(function() {
				$('#popup').hide();
			});

			$("body").on("click", ".txnId", function(e) {

				$("body").removeClass("loader--inactive");
				var _parent = $(this).closest("tr");
				var _token = $("[name='token']").val();
				var _orderId = _parent.find(".orderId").text();
				var _txnType = _parent.find(".txnType").text();
				var _txnId = $(this).text();
				
				$.ajax({
					type: "POST",
					url: "transactionPopUpAction",
					data: { "txnId": _txnId, "orderId": _orderId, "txnType": _txnType, "token": _token },
					success: function(data){
						for(key in data.aaData){
							if(data.aaData[key] != null){
								$("#"+key).text(data.aaData[key]);
								$("#"+key).closest(".detail-box").removeClass("d-none");
							}else{
							}
						}
						if(data.data.length > 0){
                            $(".dataBody").html("");
                            var _td = "";
                            for(var i = 0; i < data.data.length; i++){
                                _td += "<tr>";
                                _td += "<td>"+data.data[i]["orderId2"]+"";
                                _td += "<td>"+data.data[i]["pgRefNum2"]+"";
                                _td += "<td>"+data.data[i]["amount"]+"";
                                _td += "<td>"+data.data[i]["txnType"]+"";
                                _td += "<td>"+data.data[i]["status"]+"";
                                _td += "<td>"+data.data[i]["date"]+"";  
                                _td += "</tr>";
                            }
                            $(".dataBody").append(_td);
                        }
						$("#fancybox").fancybox({
							'overlayShow': true
						}).trigger('click');
						$("body").addClass("loader--inactive");
					}
				});
			});

			$(".transaction-tab span").on("click", function(e){
				var _getClass = $(this).attr("data-src");
				$(".transaction-tab span").removeClass("active-tab");
				$(this).addClass("active-tab");
				$(".transaction-box").removeClass("active-box");
				$("."+_getClass).addClass("active-box");
			});

			$(".lpay_toggle").on("change", function(e) {
				var _getChecked = $(this).find("input[type=checkbox]").is(":checked");
				var _label = $(this).closest("label");

				if(_getChecked) {
					_label.addClass("lpay_toggle_on");
					getLatestDataHandler(true);
				} else {
					_label.removeClass("lpay_toggle_on");
					getLatestDataHandler(false);
				}
			});

			var parameterObj = {
				pgRefNum		:	"pgRefNum",
				orderId			:	"orderId",
				customerEmail	:	"customerEmail",
				paymentRegion	:	"paymentRegion",
				transactionType	:	"transactionType",
				status			:	"status",
				rrn 			:	"rrn",
				currency		:	"currency"
			}

			var getLatestDataHandler = function(status) {
				for(var key in parameterObj) {
					var _element = document.getElementById(parameterObj[key]);
					
					if(_element.tagName == "SELECT") {
						$("#" + parameterObj[key]).selectpicker("val", "");
					} else {
						_element.value = "";
					}

					$("#" + parameterObj[key]).prop("disabled", status);
					$("#" + parameterObj[key]).selectpicker("refresh");
				}
			}

			getLatestDataHandler(true);	

}); 

function renderTable() {
    var merchantEmailId = document.getElementById("merchant").value;
    var table = new $.fn.dataTable.Api('#txnResultDataTable');
    
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transFrom == null || transTo == null) {
        alert('Enter date value');
        return false;
    }

    if (transFrom > transTo) {
        $("body").addClass("loader--inactive");
        alert('From date must be before the to date');
        $('#dateFrom').focus();
        return false;
    }
    if (transTo - transFrom > 31 * 86400000) {
        $("body").addClass("loader--inactive");
        alert('No. of days can not be more than 31');
        $('#dateFrom').focus();
        return false;
    }
    var token = document.getElementsByName("token")[0].value;
    //$("body").addClass("loader--inactive");
    
    var buttonCommon = {
    exportOptions: {
        format: {
            body: function ( data, column, row, node ) {
                // Strip $ from salary column to make it numeric
                return column === 0 ? "'"+data : (column === 1 ? "'" + data: data);
            }
        }
    }
};

    $('#txnResultDataTable').dataTable(
        {
            "columnDefs": [{ 
                className: "dt-body-right",
                "targets": [1,2,3,4,5,6,7]
            }],
                dom : 'BTrftlpi',
                buttons : [
                        $.extend( true, {}, buttonCommon, {
                            extend: 'copyHtml5',											
                            exportOptions : {											
                                columns : [':visible']
                            },
                        } ),
                    $.extend( true, {}, buttonCommon, {
                            extend: 'csvHtml5', 
                            title : 'Search Transactions',
                            exportOptions : {
                                
                                columns : [':visible']
                            },
                        } ),
                    {
                        extend : 'pdfHtml5',
                        orientation : 'landscape',
                        pageSize: 'legal',
                        //footer : true,
                        title : 'Search Transactions',
                        exportOptions : {
                            columns: [':visible']
                        },
                        customize: function (doc) {
                            doc.defaultStyle.alignment = 'center';
                            doc.styles.tableHeader.alignment = 'center';
                        }
                    },
                    {
                        extend : 'print',
                        //footer : true,
                        title : 'Search Transactions',
                        exportOptions : {
                            columns : [':visible']
                        }
                    },
                    {
                        extend : 'colvis',
                        columns : [ 0, 1, 2, 3, 4, 5, 6]
                    } ],

            "ajax" :{
                
                "url" : "transactionSearchAction",
                "type" : "POST",
                "data": function (d){
                    return generatePostData(d);
                },
                "async": true,
                "error": function (xhr, error, code)
                {
                    console.log(xhr);
                    console.log(code);
                },
            },
            "fnDrawCallback" : function() {
                $("#submit").removeAttr("disabled");
                $("body").addClass("loader--inactive");
                // hideColumn();
            },
            "searching": false,
            "ordering": false,
            "processing": true,
            "serverSide": true,
            "paginationType": "full_numbers", 
            "lengthMenu": [[10, 25, 50], [10, 25, 50]],
            "order" : [ [ 2, "desc" ] ],
            
                "columnDefs": [
                    {
                    "type": "html-num-fmt", 
                    "targets": 4,
                    "orderable": true, 
                    "targets": [0,1,2,3,4,5,6]
                    },
                    {
                'targets': 0,
                'createdCell':  function (td, cellData, rowData, row, col) {
                    $("#setData").val(rowData["subMerchantId"]);
                }
                }
                ],

            "columns" : [ 
                
            {
                "data" : "txnId",
                "className" : "txnId my_class1 text-class",
                "width": "2%" 
            },  {
                "data" : "merchants",
                "className" : "payId text-class"
    
            },
            {
                "data" : "txnType",
                "className" : "text-class txnType"
            },
            {
                "data" : "orderId",
                "className" : "text-class orderId"
            },
            {
                "data" : "paymentMethods",
                "render" : function(data, type, full) {
                    return full['paymentMethods'] + ' '
                            + '-' + ' '
                            + full['mopType'];
                },
                "className" : "text-class"
            },
            {
            	"data": "acqId"
            },
            {
            	"data": "rrn"
            },
            {
                "data" : "status",
                "className" : "text-class"
            },
            {
                "data" : "dateFrom",
                "className" : "text-class",
                "width" : "10%"
            },
            {
                "data" : "totalAmount",
                "className" : "text-class"
            },
        ]
        });
    
    $(".confirmButton").on("click", function(e){

        reloadTable();

        $(".lpay_popup").fadeOut();

    })
    
    $("body").on("click", '#txnResultDataTable .callback-btn', function(e){
        var table = new $.fn.dataTable.Api('#txnResultDataTable');
        var _getClosestTr = $(this).closest("tr").prev("tr");
        var _data = table.rows(_getClosestTr).data();
        document.querySelector("body").classList.remove("loader--inactive");
        let _parentPayId = _data[0].parentPayId === null ? '' : _data[0].parentPayId 
        $.ajax({
            type: 'POST',
            url: 'sendTransactionCallback',
            data: {
                parentPayId: _parentPayId,
                payId: _data[0].payId,
                orderId: _data[0].orderId
            },
            success: function(data){
                $(".responseMsg").text(data.responseMsg);
                if(data.response == "SUCCESS"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                document.querySelector("body").classList.add("loader--inactive");
                $(".lpay_popup").fadeIn();
            }
        })
    })

    $("body").on("click", '#txnResultDataTable .status-btn', function(e){
        var table = new $.fn.dataTable.Api('#txnResultDataTable');
        var _getClosestTr = $(this).closest("tr").prev("tr");
        var _data = table.rows(_getClosestTr).data();
        document.querySelector("body").classList.remove("loader--inactive"); 
        $.ajax({
            type: 'POST',
            url: 'transactionStatusEnquiry',
            data: {
                pgRefNum: _data[0].pgRefNum,
            },
            success: function(data){
                $(".responseMsg").text(data.responseMsg);
                if(data.response == "SUCCESS"){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                }
                document.querySelector("body").classList.add("loader--inactive");
                $(".lpay_popup").fadeIn();
            }
        })
    })
                    
    $(document).ready(function() {
        var table = $('#txnResultDataTable').DataTable();
        $('#txnResultDataTable').on('click','.center',function(){
        var columnIndex = table.cell(this).index().column;
        var rowIndex = table.cell(this).index().row;
        var rowNodes = table.row(rowIndex).node();
        var rowData = table.row(rowIndex).data();
        var txnType1 = rowData.txnType;
        var status1 = rowData.status;	
    
        if ((txnType1=="SALE" && status1=="Captured")||(txnType1=="AUTHORISE" && status1=="Approved")||(txnType1=="SALE" && status1=="Settled")) {						
            var payId1 =  rowData.pgRefNum;										
            var orderId1 = rowData.orderId; 					 
            var txnId1 = Number(rowData.txnId); 
            document.getElementById('payIdc').value = payId1;
            document.getElementById('orderIdc').value = orderId1;
            document.getElementById('txnIdc').value = txnId1;
            document.chargeback.submit();
        }
        });

        $("body").on("click", "#txnResultDataTable tbody td", function(e){
            var table = new $.fn.dataTable.Api('#txnResultDataTable');
            console.log(e.target.classList[0] == "txnId");
            if(e.target.classList[0] != "txnId"){
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

    });	
	
}

function reloadTable() {
    var datepick = $.datepicker;
    var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
    var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
    if (transFrom == null || transTo == null) {
        alert('Enter date value');
        return false;
    }

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
    $("#submit").attr("disabled", true);
    var tableObj = $('#txnResultDataTable');
    var table = tableObj.DataTable();
    table.ajax.reload();
    
}

function generatePostData(d) {
    var token = document.getElementsByName("token")[0].value;
    var merchantEmailId = document.getElementById("merchant").value;
    var	transactionType = document.getElementById("transactionType").value;
    var	paymentType = document.getElementById("paymentMethod").value;
    var	paymentRegion = document.getElementById("paymentRegion").value;
    var	rrn = document.getElementById("rrn").value;

    var status = $("#status").val();
    if(status == null) {
        status = document.getElementById("status").value;
    } else {
        status = $("#status").val().join();
    }

    var currency = document.getElementById("currency").value;
    var isSearchFlag = document.getElementById("getLatest").checked;
    var _subMerchantEmailId = $("#subMerchant").val();
    
    if(merchantEmailId == '') {
        merchantEmailId = 'ALL';
    }

    if(transactionType == '') {
        transactionType = 'ALL';
    }

    if(paymentType == '') {
        paymentType = 'ALL';
    }

    if(paymentRegion == '') {
        paymentRegion = 'ALL';
    }
    
    if(status == ''){
        status = 'ALL';
    }

    if(currency == '') {
        currency = 'ALL';
    }
    
    var obj = {
        transactionId : document.getElementById("pgRefNum").value,
        orderId : document.getElementById("orderId").value,
        customerEmail : document.getElementById("customerEmail").value,
        merchantEmailId : merchantEmailId,
        rrn : rrn,
        transactionType : transactionType,
        paymentType : paymentType,
        paymentRegion : paymentRegion,
        status : status,
        currency : currency,
        searchFlag : isSearchFlag,
        dateFrom : document.getElementById("dateFrom").value,
        dateTo : document.getElementById("dateTo").value,
        draw : d.draw,
        subMerchantEmailId:  _subMerchantEmailId,
        length :d.length,
        start : d.start, 
        token : token,
        "struts.token.name" : "token",
    };

    return obj;
}