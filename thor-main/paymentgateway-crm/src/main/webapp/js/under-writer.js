$(window).on("load", function(e){
    
    
    // function dateToolTip(){
    //     $("body").removeClass("loader--inactive");
    //     $("td.registerDate").each(function(e){
    //         var _getDate = $(this).text();
    //         if(_getDate != ""){
    //             var _getSpace = _getDate.indexOf(" ");
    //             var _getTime = _getDate.substring(_getSpace);
    //             var _getOnlyDate = _getDate.substring(0, _getSpace);
    //             $(this).text(_getOnlyDate);
    //             $(this).append("<div class='timeTip'>"+_getTime+"</div>");
    //         }
    //     })
    //     setTimeout(function(e){
    //         $("body").addClass("loader--inactive");
    //     }, 500);
    // }
    // setTimeout(function(e){

    // dateToolTip();
    // }, 500);
});

$(document).ready(function(e){

    var _token = $("[name='token']").val();
    var _table = $("#datatable");

    var _getPermission = $(".edit-permission").text();
    // if(_getPermission == "false"){
    //     var td = $(_table).DataTable();
	// 	td.columns(13).visible(false);
    // }

    function dateToolTip(){
		$("body").removeClass("loader--inactive");
		$("td.registerDate").each(function(e){
			var _getDate = $(this).text();
			if(_getDate != ""){
			var _getSpace = _getDate.indexOf(" ");
			var _getTime = _getDate.substring(_getSpace);
			var _getOnlyDate = _getDate.substring(0, _getSpace);
			$(this).text(_getOnlyDate);
			$(this).append("<div class='timeTip'>"+_getTime+"</div>");
			}
		})
		setTimeout(function(e){
			$("body").addClass("loader--inactive");
		}, 500);
	}

    function handleChange() {
		reloadTable();
		var _merchantVal = $("#merchantStatus").val();
		console.log(_merchantVal);
		if(_merchantVal == "Approved" || _merchantVal == "Rejected"){
			$(".approver").removeClass("d-none");
		}else{
            $("#byWhom").val("ALL");
			$(".approver").addClass("d-none");
        }
        
    }
    
    $(".form-control").on("change", handleChange);

    function reloadTable() {
        var table = _table.DataTable();
        table.ajax.reload();
    }
    $("#datatable").dataTable({
        dom : 'BTftlpi',
        buttons: ['csv', 'print', 'pdf'],
        language: {
            search: "",
            searchPlaceholder: "Search records"
        },
        "destroy": true,
        "ajax": {
            "type": "post",
            "url": "merchantDetailsToSubAdminAction",
            "data" : function (d){
                    return generatePostData(d);
                }
            },
            "drawCallback": function( settings ) {
                dateToolTip();
            },
                "aoColumns": [{
                "mDataProp": "payId",
                "className": "my_class"
            },
            {"mData" : "businessName"}, 
            {"mData" : "emailId"}, 
            {"mData" : "status"}, 
            {"mData" : "userType"},
            {"mData" : "mobile"},	
            {"mData" : "registrationDate"},
            {"mData" : "makerName"}, 
            {"mData" : "makerStatus"},
            {"mData" : "makerStatusUpDate"},
            {"mData" : "checkerName"},
            {"mData" : "checkerStatus"},
            {"mData" : "checkerStatusUpDate"},
                    
        ]
    });
    

        $("body").on("click", "#editPermission", function(e){
            var _parent = $(this).closest("tr");
            var _payId = _parent.find(".my_class").text();
            $("#merchantPayIdEdit").val(_payId);
            $("body").removeClass("loader--inactive");
            $("#merchantEditForm").submit();
        })
        // $( "#merchantList tbody tr td:eq(2)" ).addClass( "color");


        $("body").on("click",".my_class", function(e){
                var _getPayId = $(this).text();
                $("#hidden").val(_getPayId);
                $("body").removeClass("loader--inactive");
                document.merchant.submit();
        })

        function generatePostData(d) {
			var businessType = null;
			var merhantStatus = null;
			var byWhom = null;
			// data: {"token": token,"merchantStatus":'ALL',"byWhom":null,"businessType":'ALL'},
			if(null != document.getElementById("merchantStatus")){
				merhantStatus = document.getElementById("merchantStatus").value;
				// $(".approver").removeClass("d-none");
			}else{
				merchantStatus = "ALL";
				// $(".approver").addClass("d-none");
			}
			if(null != document.getElementById("industryTypes")){
				businessType = document.getElementById("industryTypes").value;
			}else{
				businessType = 'ALL';
            }
            
            if(null != document.getElementById("byWhom")){
                byWhom = document.getElementById("byWhom").value;
            }else{
                byWhom = "ALL";
            }

			var obj = {				
					token : _token,
					businessType : businessType,
					merchantStatus : merhantStatus,
					byWhom : byWhom
			};

			return obj;
        }
        
})